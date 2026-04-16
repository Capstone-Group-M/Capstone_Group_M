package com.notam.client.new_client;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Simple file-based output for writing received NOTAM messages to a text file.
 * Each message is appended to the file with delimiter lines for readability.
 *
 * <p>This class is thread-safe — the {@link #write(String)} method is
 * synchronized to prevent interleaved output from concurrent JMS threads.
 */
public class TxtOutput {
    private final String filePath;

    /**
     * Creates a new TxtOutput that appends to the specified file.
     *
     * @param filePath the path to the output file (created if it does not exist)
     */
    public TxtOutput(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Appends a NOTAM message to the output file wrapped in delimiter lines.
     * The file is opened in append mode so previous content is preserved.
     *
     * <p>Synchronized to prevent concurrent writes from interleaving since
     * multiple JMS message listener threads may call this method simultaneously.
     *
     * @param message the NOTAM content to write (either JSON or raw XML)
     */
    public synchronized void write(String message) {
        try (PrintWriter writer = new PrintWriter(
                new FileWriter(filePath, true))) {
            writer.println("=== NOTAM RECEIVED ===");
            writer.println(message);
            writer.println("======================\n");
        } catch (IOException e) {
            System.err.println("Failed to write: " + e.getMessage());
        }
    }
}