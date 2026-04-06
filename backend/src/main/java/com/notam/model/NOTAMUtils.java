package com.notam.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NOTAMUtils {

    /**
     * This function takes in a list of NOTAMs, processes them 
     * and removes duplicates, then returns a new list with all
     * unique NOTAMs.
     * @param notams
     * @return a new list with duplicates removed
     */

    public static List<NOTAM> removeDuplicates(List<NOTAM> notams) {
        Set<NOTAM> seen = new HashSet<>();
        List<NOTAM> result = new ArrayList<>();

        for (NOTAM notam : notams) {
            if (seen.add(notam)) {
                result.add(notam);
            }
        }
        return result;
    }
}