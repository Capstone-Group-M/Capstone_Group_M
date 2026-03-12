package cli.src;

import java.util.HashMap;
import java.util.Map;

import cli.src.commands.commands;
import cli.src.commands.exit_command;
import cli.src.commands.fetch_notams;

public class command_router {

    private final Map<String, commands> commands = new HashMap<>();

    public command_router() {
        commands.put("fetch", new fetch_notams());
        commands.put("exit", new exit_command());
    }

    public void route(String inputLine) {

        if (inputLine == null || inputLine.trim().isEmpty()) {
            return;
        }
        String[] tokens = inputLine.trim().split("\\s+");
        String commandName = tokens[0];
        String[] args = new String[tokens.length - 1];
        if (tokens.length > 1) {
            System.arraycopy(tokens, 1, args, 0, args.length);
        }
        commands command = commands.get(commandName);

        if (command == null) {
            System.out.println("Unknown command: " + commandName);
            if (commands.containsKey("help")) {
                commands.get("help").execute(new String[]{});
            }
            return;
        }
        command.execute(args);
    }
}