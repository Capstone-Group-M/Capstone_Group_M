package cli.src.commands;

public class fetch_notams implements commands {

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: fetch <airport_code>");
            return;
        }

        String airportCode = args[0];

        System.out.println("Fetching NOTAMs for airport: " + airportCode);
        System.out.println("(API call)");
    }
}