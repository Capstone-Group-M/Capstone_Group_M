package com.notam.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.auth.oauth2.GoogleCredentials;
import com.notam.model.NOTAM;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
@ConditionalOnProperty(name = "firestore.enabled", havingValue = "true")
public class FirestoreNotamRepository implements NotamRepository {
    private static final String COLLECTION = "notams";
    private final Firestore firestore;

    public FirestoreNotamRepository() {
        this.firestore = initializeFirestore();
    }

    public FirestoreNotamRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public void save(NOTAM notam) {
        if (notam == null) {
            return;
        }

        String documentId = documentIdFor(notam);
        try {
            notamsCollection()
                    .document(documentId)
                    .set(toFirestoreMap(notam), SetOptions.merge())
                    .get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while saving NOTAM to Firestore", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to save NOTAM to Firestore", e);
        }
    }

 @Override
public List<NOTAM> findAll() {
    try {
        List<NOTAM> results = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = notamsCollection().get();

        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
            results.add(fromDocument(doc));
        }

        return results;
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Interrupted while reading all NOTAMs from Firestore", e);
    } catch (ExecutionException e) {
        throw new IllegalStateException("Failed to read all NOTAMs from Firestore", e);
    }
}

    @Override
    public void saveAll(List<NOTAM> notams) {
        if (notams == null || notams.isEmpty()) {
            return;
        }
        for (NOTAM notam : notams) {
            save(notam);
        }
    }

    @Override
    public List<NOTAM> findByIcaoLocation(String icaoLocation) {
        try {
            ApiFuture<QuerySnapshot> future = notamsCollection()
                    .whereEqualTo("icaoLocation", normalizeIcao(icaoLocation))
                    .get();

            List<NOTAM> results = new ArrayList<>();
            for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
                results.add(fromDocument(doc));
            }
            return results;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while reading NOTAMs from Firestore", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to read NOTAMs from Firestore", e);
        }
    }

    private CollectionReference notamsCollection() {
        return firestore.collection(COLLECTION);
    }

    private static Firestore initializeFirestore() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .build();
                FirebaseApp.initializeApp(options);
            }
            return FirestoreClient.getFirestore();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to initialize Firestore. Set GOOGLE_APPLICATION_CREDENTIALS to a Firebase service account JSON file.",
                    e
            );
        }
    }

    private static String documentIdFor(NOTAM notam) {
        String id = firstNonBlank(notam.getId(), notam.getNumber());
        if (id == null) {
            id = (firstNonBlank(notam.getIcaoLocation(), "UNKNOWN") + "-" + notam.getText()).hashCode() + "";
        }
        return id.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private static Map<String, Object> toFirestoreMap(NOTAM notam) {
        Map<String, Object> map = new HashMap<>();
        put(map, "id", notam.getId());
        put(map, "number", notam.getNumber());
        put(map, "series", notam.getSeries());
        put(map, "type", notam.getType());
        put(map, "accountId", notam.getAccountId());
        put(map, "icaoLocation", normalizeIcao(notam.getIcaoLocation()));
        put(map, "issued", toTimestamp(notam.getIssued()));
        put(map, "effectiveStart", toTimestamp(notam.getEffectiveStart()));
        put(map, "effectiveEnd", toTimestamp(notam.getEffectiveEnd()));
        put(map, "lastUpdated", toTimestamp(notam.getLastUpdated()));
        put(map, "location", notam.getLocation());
        put(map, "minimumFL", notam.getMinimumFL());
        put(map, "maximumFL", notam.getMaximumFL());
        put(map, "coordinates", notam.getCoordinates());
        put(map, "classification", notam.getClassification());
        put(map, "traffic", notam.getTraffic());
        put(map, "purpose", notam.getPurpose());
        put(map, "scope", notam.getScope());
        put(map, "selectionCode", notam.getSelectionCode());
        put(map, "text", notam.getText());
        put(map, "storedAt", Timestamp.now());
        return map;
    }

    private static NOTAM fromDocument(DocumentSnapshot doc) {
        NOTAM notam = new NOTAM();
        notam.setId(doc.getString("id"));
        notam.setNumber(doc.getString("number"));
        notam.setSeries(doc.getString("series"));
        notam.setType(doc.getString("type"));
        notam.setAccountId(doc.getString("accountId"));
        notam.setIcaoLocation(doc.getString("icaoLocation"));
        notam.setIssued(fromTimestamp(doc.getTimestamp("issued")));
        notam.setEffectiveStart(fromTimestamp(doc.getTimestamp("effectiveStart")));
        notam.setEffectiveEnd(fromTimestamp(doc.getTimestamp("effectiveEnd")));
        notam.setLastUpdated(fromTimestamp(doc.getTimestamp("lastUpdated")));
        notam.setLocation(doc.getString("location"));
        notam.setMinimumFL(doc.getString("minimumFL"));
        notam.setMaximumFL(doc.getString("maximumFL"));
        notam.setCoordinates(doc.getString("coordinates"));
        notam.setClassification(doc.getString("classification"));
        notam.setTraffic(doc.getString("traffic"));
        notam.setPurpose(doc.getString("purpose"));
        notam.setScope(doc.getString("scope"));
        notam.setSelectionCode(doc.getString("selectionCode"));
        notam.setText(doc.getString("text"));
        return notam;
    }

    private static void put(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private static Timestamp toTimestamp(ZonedDateTime dateTime) {
        return dateTime == null ? null : Timestamp.ofTimeSecondsAndNanos(dateTime.toEpochSecond(), dateTime.getNano());
    }

    private static ZonedDateTime fromTimestamp(Timestamp timestamp) {
        return timestamp == null ? null : ZonedDateTime.ofInstant(timestamp.toDate().toInstant(), ZoneOffset.UTC);
    }

    private static String normalizeIcao(String icaoLocation) {
        return icaoLocation == null ? null : icaoLocation.trim().toUpperCase();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
