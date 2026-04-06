package notam;

import java.util.List;

import notam.model.Notam;
import notam.service.NotamService;

public class DemoRunner {

    public static void main(String[] args) {

        NotamService service = new NotamService();

        List<Notam> notams = service.getFilteredNotams("KJFK");

        System.out.println("Filtered NOTAM Results:");
        System.out.println("------------------------");

        for (Notam n : notams) {
            System.out.println(n.getId() + " | " + n.getDescription());
        }

        System.out.println("------------------------");
        System.out.println("Total after duplicate removal: " + notams.size());
    }
}