package com.notam.controller;

import com.notam.model.NOTAM;
import com.notam.repository.FirestoreNotamRepository;

import java.time.ZonedDateTime;
import java.util.List;

public class FirestoreMockTest {
    public static void main(String[] args) {
        FirestoreNotamRepository repo = new FirestoreNotamRepository();

        NOTAM mock = new NOTAM();
        mock.setId("MOCK-KOKC-001");
        mock.setNumber("05/001");
        mock.setSeries("A");
        mock.setType("D");
        mock.setIcaoLocation("KOKC");
        mock.setIssued(ZonedDateTime.now());
        mock.setEffectiveStart(ZonedDateTime.now());
        mock.setEffectiveEnd(ZonedDateTime.now().plusDays(7));
        mock.setLocation("OKLAHOMA CITY");
        mock.setClassification("DOMESTIC");
        mock.setText("MOCK NOTAM: RUNWAY 17L/35R CLOSED FOR TESTING.");

        repo.save(mock);

        List<NOTAM> saved = repo.findByIcaoLocation("KOKC");

        System.out.println("Saved mock NOTAM to Firestore.");
        System.out.println("Found " + saved.size() + " NOTAM(s) for KOKC.");
        saved.forEach(n -> System.out.println(n.getId() + " - " + n.getText()));
    }
}