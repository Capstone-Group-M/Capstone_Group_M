package com.notam.client.new_client;

import com.solacesystems.jms.SupportedProperty;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.Config;

import org.json.JSONObject;
import org.json.XML;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

/**
 * Consumer for the FAA SWIM (System Wide Information Management) NOTAM feed.
 * Connects to the FAA's Solace JMS broker over TLS and receives real-time NOTAM
 * messages in AIXM 5.1 XML format. Messages can optionally be converted to JSON
 * and are filtered to include only US NOTAMs before being written to an output file.
 * 
 * <p>This class replaces the deprecated FAA REST API (sunset in April 2026) and provides
 * a push-based feed instead of polling.
 * 
 * <p>US NOTAMs are identified by their ICAO location prefix:
 * <ul>
 *   <li>K  - Continental United States (CONUS)</li>
 *   <li>PA - Alaska</li>
 *   <li>PH - Hawaii</li>
 *   <li>PG - Guam</li>
 *   <li>TJ - Puerto Rico</li>
 * </ul>
 */
public class SWIMConsumer implements MessageListener {

    private final String providerUrl;
    private final String queue;
    private final String connectionFactory;
    private final String username;
    private final String password;
    private final String vpn;
    private final String outputFile;
    private final boolean json;

    /**
     * Creates a new SWIMConsumer with connection and output settings.
     *
     * @param providerUrl the Solace broker URL (e.g. "tcps://ems1.swim.faa.gov:55443")
     * @param queue the JNDI queue name assigned to the user's SWIM account
     * @param connectionFactory the JNDI connection factory name
     * @param username the SWIM account username
     * @param password the SWIM account password
     * @param vpn the Solace message VPN (typically "AIM_FNS")
     * @param outputFile the path to the file where received NOTAMs will be appended
     * @param json if true, converts AIXM XML to JSON before writing; if false, writes raw XML
     */
    public SWIMConsumer(String providerUrl, String queue, String connectionFactory,
                        String username, String password, String vpn, String outputFile, Boolean json) {
        this.providerUrl = providerUrl;
        this.queue = queue;
        this.connectionFactory = connectionFactory;
        this.username = username;
        this.password = password;
        this.vpn = vpn;
        this.outputFile = outputFile;
        this.json = json;
    }

    /**
     * Opens a TLS connection to the Solace broker, looks up the configured queue,
     * and starts listening for messages. Incoming messages are delivered to
     * {@link #onMessage(Message)}.
     *
     * <p>Uses the bundled {@code cacerts} truststore from the classpath for TLS
     * certificate validation against the FAA SWIM broker.
     *
     * @throws Exception if the connection, JNDI lookup, or session creation fails
     */
    public void connect() throws Exception {
        // Load the truststore used to validate the Solace broker's TLS certificate
        String truststorePath = getClass().getClassLoader()
                .getResource("cacerts").toExternalForm();

        // Configure JNDI environment for Solace
        Hashtable<String, Object> env = new Hashtable<>();
        env.put(InitialContext.INITIAL_CONTEXT_FACTORY,
                "com.solacesystems.jndi.SolJNDIInitialContextFactory");
        env.put(InitialContext.PROVIDER_URL, providerUrl);
        env.put(Context.SECURITY_PRINCIPAL, username);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(SupportedProperty.SOLACE_JMS_SSL_VALIDATE_CERTIFICATE, true);
        env.put(SupportedProperty.SOLACE_JMS_VPN, vpn);
        env.put(SupportedProperty.SOLACE_JMS_SSL_TRUST_STORE, truststorePath);

        InitialContext context = new InitialContext(env);

        // Look up the JMS connection factory and queue from JNDI
        ConnectionFactory conFactory = (ConnectionFactory) context.lookup(connectionFactory);
        Connection connection = conFactory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue dest = (Queue) context.lookup(queue);

        // Register this class as the message listener and start receiving
        MessageConsumer consumer = session.createConsumer(dest);
        consumer.setMessageListener(this);
        connection.start();

        System.out.println("Connected! Listening for NOTAMs...");
    }

    /**
     * Callback invoked by the JMS provider each time a new NOTAM message arrives.
     * Extracts the message body (supports both {@link BytesMessage} and
     * {@link TextMessage}), converts the AIXM XML to JSON, filters for US-only
     * NOTAMs, and appends the result to the output file.
     *
     * <p>Non-US NOTAMs and malformed messages are silently skipped.
     *
     * @param message the incoming JMS message from the SWIM broker
     */
    @Override
    public void onMessage(Message message) {
        try {
            // Extract the raw XML payload from the JMS message
            String content = "";
            if (message instanceof BytesMessage) {
                BytesMessage byteMsg = (BytesMessage) message;
                byte[] bytes = new byte[(int) byteMsg.getBodyLength()];
                byteMsg.readBytes(bytes);
                content = new String(bytes);
            } else if (message instanceof TextMessage) {
                content = ((TextMessage) message).getText();
            }

            // Parse the XML into JSON for both filtering and optional output
            JSONObject jsonObj = XML.toJSONObject(content);

            // Filter: only keep NOTAMs for US locations
            String icao = extractIcaoLocation(jsonObj);
            if (icao == null || !isUSNotam(icao)) {
                return;
            }

            // Write either JSON (pretty-printed) or raw XML depending on config
            String output;
            if (this.json) {
                output = jsonObj.toString(2);
            } else {
                output = content;
            }

            writeToFile(output);
            System.out.println("US NOTAM received: " + icao);

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    /**
     * Extracts an ICAO location code from a parsed NOTAM JSON document. Tries
     * three sources in order of preference:
     * <ol>
     *   <li>{@code fnse:icaoLocation} from the FAA event extension (most reliable)</li>
     *   <li>{@code event:location} with a "K" prefix (works for CONUS airports reporting only the 3-letter code)</li>
     *   <li>{@code event:affectedFIR} as a last resort (covers FIR-level NOTAMs)</li>
     * </ol>
     * Also handles the case where {@code hasMember} is a JSON array containing
     * multiple AIXM objects, searching for the one containing the Event.
     *
     * @param json the parsed AIXM NOTAM as a {@link JSONObject}
     * @return the ICAO code, or {@code null} if none of the three sources are available
     */
    String extractIcaoLocation(JSONObject json) {
        try {
            JSONObject msg = json.getJSONObject("AIXMBasicMessage");
            Object member = msg.get("hasMember");

            // hasMember can be either a single object or an array of AIXM features
            JSONObject event;
            if (member instanceof JSONObject) {
                event = ((JSONObject) member).getJSONObject("event:Event");
            } else if (member instanceof org.json.JSONArray) {
                org.json.JSONArray arr = (org.json.JSONArray) member;
                event = null;
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    if (obj.has("event:Event")) {
                        event = obj.getJSONObject("event:Event");
                        break;
                    }
                }
                if (event == null) return null;
            } else {
                return null;
            }

            JSONObject timeSlice = event
                .getJSONObject("event:timeSlice")
                .getJSONObject("event:EventTimeSlice");

            // 1. Preferred source: fnse:icaoLocation from the FAA extension
            try {
                return timeSlice
                    .getJSONObject("event:extension")
                    .getJSONObject("fnse:EventExtension")
                    .getString("fnse:icaoLocation");
            } catch (Exception ignored) {}

            // 2. Fallback: prepend "K" to the event:location (works for CONUS)
            JSONObject notam = timeSlice
                .getJSONObject("event:textNOTAM")
                .getJSONObject("event:NOTAM");

            try {
                String loc = notam.getString("event:location");
                if (loc != null && !loc.isEmpty()) {
                    return "K" + loc;
                }
            } catch (Exception ignored) {}

            // 3. Fallback: use the affected FIR (catches FIR-level NOTAMs)
            try {
                return notam.getString("event:affectedFIR");
            } catch (Exception ignored) {}

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Determines whether the given ICAO code belongs to a US location by checking
     * for one of the US ICAO prefixes: K, PA, PH, PG, or TJ.
     *
     * @param icao the ICAO code to check (must not be null)
     * @return true if the code starts with a US prefix, false otherwise
     */
    boolean isUSNotam(String icao) {
        return icao.startsWith("K")
            || icao.startsWith("PA")
            || icao.startsWith("PH")
            || icao.startsWith("PG")
            || icao.startsWith("TJ");
    }

    /**
     * Appends a NOTAM to the output file with delimiter lines for readability.
     * This method is synchronized to prevent concurrent writes from interleaving
     * since messages may arrive from multiple JMS threads.
     *
     * @param content the NOTAM content (either JSON or XML) to append
     */
    private synchronized void writeToFile(String content) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, true))) {
            writer.println("=== NOTAM ===");
            writer.println(content);
            writer.println("=============\n");
        } catch (IOException e) {
            System.err.println("Failed to write: " + e.getMessage());
        }
    }

    /**
     * Entry point for running the SWIM consumer as a standalone application.
     * Loads all connection settings from {@code application.conf} on the
     * classpath, connects to the broker, and blocks indefinitely while
     * messages are received on a JMS thread.
     *
     * @param args command-line arguments (unused)
     * @throws Exception if the connection fails or the configuration is invalid
     */
    public static void main(String[] args) throws Exception {
        Config config = ConfigFactory.load();

        SWIMConsumer consumer = new SWIMConsumer(
            config.getString("providerUrl"),
            config.getString("queue"),
            config.getString("connectionFactory"),
            config.getString("username"),
            config.getString("password"),
            config.getString("vpn"),
            config.getString("output"),
            config.getBoolean("json")
        );
        consumer.connect();

        // Keep the main thread alive so the JMS consumer threads can run
        Thread.currentThread().join();
    }
}