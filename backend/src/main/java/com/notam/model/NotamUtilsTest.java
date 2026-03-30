package com.notam.model;

import java.util.List;

public class NotamUtilsTest {

    public static void main(String[] args) {
        NOTAM a = new NOTAM("1", "A1234/26", "KOKC");
        NOTAM b = new NOTAM("2", "A1234/26", "KOKC");
        NOTAM c = new NOTAM("3", "B5678/26", "KDFW");

        List<NOTAM> result = NOTAMUtils.removeDuplicates(List.of(a, b, c));

        if (result.size() == 2) {
            System.out.println("PASS: duplicates removed correctly");
        } else {
            System.out.println("FAIL: expected 2, got " + result.size());
        }
    }
}