package notam.service;

import java.util.*;

import notam.model.Notam;

public class NotamFilter {

    public List<Notam> removeDuplicates(List<Notam> notams) {

        Set<String> seenIds = new HashSet<>();
        List<Notam> uniqueNotams = new ArrayList<>();

        for (Notam notam : notams) {
            if (!seenIds.contains(notam.getId())) {
                seenIds.add(notam.getId());
                uniqueNotams.add(notam);
            }
        }

        return uniqueNotams;
    }
}