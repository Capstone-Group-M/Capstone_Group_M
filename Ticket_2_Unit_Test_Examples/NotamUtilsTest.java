import java.util.*;

public class NotamUtilsTest {

    public static void main(String[] args) {

        var notams = NotamUtils.fetchNotams();
        assert notams.size() == 2;
        assert notams.get(0).id.equals("A1");
        assert notams.get(1).urgency.equals("high");

        var raw = new HashMap<String, String>();
        raw.put("id", "C3");
        raw.put("type", "Runway Maintenance");
        raw.put("location", "KJFK");

        var processed = NotamUtils.processNotam(raw);
        assert processed.urgency.equals("normal");

        System.out.println("Tests passed.");
    }
}
