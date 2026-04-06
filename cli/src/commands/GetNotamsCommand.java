package cli.src.commands;

import notam.service.NotamService;
import notam.model.Notam;

import java.util.List;

public class GetNotamsCommand implements commands {

    @Override
    public void execute(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: get-notams <airportCode>");
            return;
        }

        String airportCode = args[0];

        NotamService service = new NotamService();
        List<Notam> notams = service.getFilteredNotams(airportCode);

        if (notams.isEmpty()) {
            System.out.println("No NOTAMs found.");
            return;
        }

        System.out.println("NOTAMs for " + airportCode + ":");
        System.out.println("----------------------------");

        for (Notam n : notams) {
            System.out.println(n.getId() + " | " + n.getDescription());
        }

        System.out.println("----------------------------");
        System.out.println("Total: " + notams.size());
    }
}