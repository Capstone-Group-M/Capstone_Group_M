package com.notam.parser;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notam.model.NOTAM;


public class NotamParser {
    private final ObjectMapper mapper;

    //constructor when you create NotamParser Object.
    public NotamParser(ObjectMapper mapper){
        this.mapper = mapper;
    }

    public List<NOTAM> parsePage(HttpResponse<String> response) {
        /**
        * Parses a single page of the FAA NOTAM API response into a list of NOTAM objects.
        * 
        * @param response the HTTP response from the FAA NOTAM API
        * @return a list of NOTAM objects parsed from the response
        */

        //get to items node in json tree
        JsonNode items = traverseTreeToItems(response);

        //creat list to store notams
        List<NOTAM> allNotams = new ArrayList<>(items.size());

        //loop through all items and create notams one at a time
        for (JsonNode item : items) {
            //go to notam path in jsonnode
            JsonNode JSONnotam = item.path("properties")
                                    .path("coreNOTAMData")
                                    .path("notam");
            
            //creates Notam objects from jsonNode at notam                        
            NOTAM notam = JsonNotamToNotamObject(JSONnotam);

            //append notam to list of notams
            allNotams.add(notam);
        }
        return allNotams;
    }


    public ZonedDateTime parseDate(String dateStr) {
        /**
        * Parses a date string from the FAA NOTAM API into a ZonedDateTime object.
        * Handles special FAA values that indicate no expiry date.
        * 
        * @param dateStr the date string to parse (e.g. "2025-08-19T17:47:00.000Z", "PERM", "UFN")
        * @return a ZonedDateTime object, or null if the date is permanent, unknown, or unparseable
        */
        if (dateStr == null || 
            dateStr.equalsIgnoreCase("PERM") || 
            dateStr.equalsIgnoreCase("UFN")) {
            return null;
        }
        try {
            return ZonedDateTime.parse(dateStr);
        } catch (Exception e) {
            System.err.println("Failed to parse date: " + dateStr);
            return null;
        }
    }

    public JsonNode traverseTreeToItems(HttpResponse<String> response){
        /**
        * Traverses the FAA NOTAM API response JSON tree and extracts the "items" array.
        * Validates the HTTP response code and JSON structure before returning.
        *
        * @param response the HTTP response from the FAA NOTAM API
        * @return a JsonNode array of NOTAM items, or an empty array if none are found
        * @throws IllegalStateException if the HTTP response code is not 2xx or JSON parsing fails
        */
        int code = response.statusCode();
        if (code < 200 || code >= 300) {
            throw new IllegalStateException("FAA NOTAM API returned HTTP " + code + ": " + response.body());
        }

        final JsonNode root;
        try {
            root = mapper.readTree(response.body());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse NOTAM JSON", e);
        }

        JsonNode items = root.path("items");
        if (!items.isArray() || items.isEmpty()) {
            return mapper.createArrayNode();
        }
        return items;
    }

    public NOTAM JsonNotamToNotamObject(JsonNode JSONnotam){
        /**
        * Converts a JsonNode representing a single NOTAM into a NOTAM object.
        * Uses safe path calls so missing fields default to null rather than throwing.
        *
        * @param JSONnotam the JsonNode containing the core NOTAM data
        * @return a NOTAM object populated with the parsed fields
        */
        String id = JSONnotam.path("id").asText(null);
        String number = JSONnotam.path("number").asText(null);
        String series = JSONnotam.path("series").asText(null);
        String type = JSONnotam.path("type").asText(null);
        String accountId = JSONnotam.path("accountId").asText(null);
        String icaoLocation = JSONnotam.path("icaoLocation").asText(null);
        ZonedDateTime issued = parseDate(JSONnotam.path("issued").asText(null));
        ZonedDateTime effectiveStart = parseDate(JSONnotam.path("effectiveStart").asText(null));
        ZonedDateTime effectiveEnd = parseDate(JSONnotam.path("effectiveEnd").asText(null));
        ZonedDateTime lastUpdated = parseDate(JSONnotam.path("lastUpdated").asText(null));
        String location = JSONnotam.path("location").asText(null);
        String minimumFL = JSONnotam.path("minimumFL").asText(null);
        String maximumFL = JSONnotam.path("maximumFL").asText(null);
        String coordinates = JSONnotam.path("coordinates").asText(null);
        String classification = JSONnotam.path("classification").asText(null);
        String traffic = JSONnotam.path("traffic").asText(null);
        String purpose = JSONnotam.path("purpose").asText(null);
        String scope = JSONnotam.path("scope").asText(null);
        String selectionCode = JSONnotam.path("selectionCode").asText(null);
        String text = JSONnotam.path("text").asText(null);

        NOTAM notam = new NOTAM();
        notam.setId(id);
        notam.setNumber(number);
        notam.setSeries(series);
        notam.setType(type);
        notam.setAccountId(accountId);
        notam.setIcaoLocation(icaoLocation);
        notam.setIssued(issued);
        notam.setEffectiveEnd(effectiveEnd);
        notam.setEffectiveStart(effectiveStart);
        notam.setLastUpdated(lastUpdated);
        notam.setLocation(location);
        notam.setMinimumFL(minimumFL);
        notam.setMaximumFL(maximumFL);
        notam.setCoordinates(coordinates);
        notam.setClassification(classification);
        notam.setTraffic(traffic);
        notam.setPurpose(purpose);
        notam.setScope(scope);
        notam.setSelectionCode(selectionCode);
        notam.setText(text);
        return notam;
    }
    public List<NOTAM> parseCGIPage(HttpResponse<String> response) {
        int code = response.statusCode();
        if (code < 200 || code >= 300) {
            throw new IllegalStateException("CGI NOTAM API returned HTTP " + code + ": " + response.body());
        }

        final JsonNode root;
        try {
            root = mapper.readTree(response.body());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse CGI NOTAM JSON", e);
        }

        JsonNode geojson = root.path("data").path("geojson");
        if (!geojson.isArray() || geojson.isEmpty()) {
            return new ArrayList<>();
        }

        List<NOTAM> allNotams = new ArrayList<>(geojson.size());
        for (JsonNode item : geojson) {
            JsonNode JSONnotam = item.path("properties")
                                    .path("coreNOTAMData")
                                    .path("notam");
            allNotams.add(JsonNotamToNotamObject(JSONnotam));
        }
        return allNotams;
    }
    
    public NOTAM parseSWIMNotam(JSONObject json) {
        try {
            JSONObject msg = json.getJSONObject("AIXMBasicMessage");
            Object member = msg.get("hasMember");

            // Find the event:Event object
            JSONObject event;
            if (member instanceof JSONObject) {
                event = ((JSONObject) member).getJSONObject("event:Event");
            } else if (member instanceof org.json.JSONArray) {
                org.json.JSONArray arr = (org.json.JSONArray) member;
                event = null;
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    if (obj.has("event:Event")) {
                        event = obj.getJSONObject("event:Event");
                        break;
                    }
                }
                if (event == null) return null;
            } else {
                return null;
            }

            JSONObject timeSlice = event
                .getJSONObject("event:timeSlice")
                .getJSONObject("event:EventTimeSlice");

            JSONObject extension = timeSlice
                .getJSONObject("event:extension")
                .getJSONObject("fnse:EventExtension");

            JSONObject notamData = timeSlice
                .getJSONObject("event:textNOTAM")
                .getJSONObject("event:NOTAM");

            JSONObject timePeriod = timeSlice
                .getJSONObject("gml:validTime")
                .getJSONObject("gml:TimePeriod");

            // Map to your existing NOTAM object
            NOTAM notam = new NOTAM();

            notam.setId(event.getJSONObject("gml:identifier")
                .optString("content", null));
            notam.setNumber(String.valueOf(notamData.opt("event:number")));
            notam.setSeries(notamData.optString("event:series", null));
            notam.setType(notamData.optString("event:type", null));
            notam.setAccountId(extension.optString("fnse:accountId", null));
            notam.setIcaoLocation(extension.optString("fnse:icaoLocation", null));
            notam.setIssued(parseDate(notamData.optString("event:issued", null)));
            notam.setEffectiveStart(parseDate(timePeriod.optString("gml:beginPosition", null)));
            notam.setEffectiveEnd(parseSWIMEndDate(timePeriod.opt("gml:endPosition")));
            notam.setLastUpdated(parseDate(extension.optString("fnse:lastUpdated", null)));
            notam.setLocation(notamData.optString("event:location", null));
            notam.setMinimumFL(notamData.optString("event:minimumFL", null));
            notam.setMaximumFL(String.valueOf(notamData.opt("event:maximumFL")));
            notam.setCoordinates(notamData.optString("event:coordinates", null));
            notam.setClassification(extension.optString("fnse:classification", null));
            notam.setTraffic(notamData.optString("event:traffic", null));
            notam.setPurpose(notamData.optString("event:purpose", null));
            notam.setScope(notamData.optString("event:scope", null));
            notam.setSelectionCode(notamData.optString("event:selectionCode", null));
            notam.setText(notamData.optString("event:text", null));

            return notam;
        } catch (Exception e) {
            System.err.println("Failed to parse SWIM NOTAM: " + e.getMessage());
            return null;
        }
    }

    // Handle SWIM end dates which can be a string or an object with "content" field
    private ZonedDateTime parseSWIMEndDate(Object endPosition) {
        if (endPosition == null) return null;

        String dateStr;
        if (endPosition instanceof JSONObject) {
            dateStr = ((JSONObject) endPosition).optString("content", null);
        } else {
            dateStr = endPosition.toString();
        }

        // Strip EST/EDT suffix that SWIM sometimes appends
        if (dateStr != null) {
            dateStr = dateStr.replaceAll("EST$|EDT$", "").trim();
        }

        return parseDate(dateStr);
    }
    
}

