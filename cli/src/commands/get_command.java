package cli.src.commands;

public class get_command implements commands {

    @Override
    public void execute(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: get-notams <airportCode>");
            return;
        }

        String airportCode = args[0];

        System.out.println("Fetching NOTAMs for " + airportCode + "...");
        System.out.println("(Need to figure out API)");
    }
}