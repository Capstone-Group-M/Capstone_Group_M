package notam.service;

import java.util.List;

import notam.client.FaaNotamClient;
import notam.model.Notam;



public class NotamService {

    private final FaaNotamClient client = new FaaNotamClient();
    private final NotamFilter filter = new NotamFilter();

    public List<Notam> getFilteredNotams(String airportCode) {

        List<Notam> rawNotams = client.fetchNotams(airportCode);

        return filter.removeDuplicates(rawNotams);
    }
}