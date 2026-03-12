package cli.src.commands;

public class help_command implements commands {

    @Override
    public void execute(String[] args) {

        System.out.println("Available commands:");
        System.out.println("  get-notams <airportCode>  - Fetch NOTAMs for airport");
        System.out.println("  help                      - Show this help message");
    }
}