package com.notam.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.notam.model.*;


public class AirportDataUtil {
    public static Coordinate getCoordinatesFromIdent(String airportId) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                AirportDataUtil.class.getClassLoader().getResourceAsStream("AirportCodes.csv")))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                String airportIdUppercase = airportId.trim().toUpperCase();
                if (fields[0].equals(airportIdUppercase)) {
                    String coordString = fields[12].replace("\"", "").trim();
                    String[] coordList = coordString.split(",");
                    if (coordList.length == 2) {
                        return new Coordinate(
                            Double.parseDouble(coordList[0].trim()),
                            Double.parseDouble(coordList[1].trim())
                        );
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to read AirportCodes.csv: " + e.getMessage());
        }
        return null; // airport not found
    }
}
