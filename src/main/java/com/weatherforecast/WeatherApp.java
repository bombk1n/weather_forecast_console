package com.weatherforecast;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class WeatherApp {


    public static void main(String[] args) {
        try {
            Scanner sca = new Scanner(System.in);
            String city;

            while (true) {
                System.out.println("======================================");
                System.out.println("Enter the city name(print 'exit' to exit): ");
                city = sca.nextLine();
                if (city.equalsIgnoreCase("exit")) {
                    break;
                }
                JSONObject cityLocationData = (JSONObject) getLocationData(city);

                double latitude = 0;
                double longitude = 0;
                try {
                    latitude = (double) cityLocationData.get("latitude");
                    longitude = (double) cityLocationData.get("longitude");
                } catch (NullPointerException e) {
                    continue;
                }

                displayWeatherData(latitude, longitude);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void displayWeatherData(double latitude, double longitude) {
        String urlString = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude + "&longitude=" + longitude + "&current=temperature_2m,relative_humidity_2m,wind_speed_10m,wind_direction_10m";
        try {
            HttpURLConnection conn = fetchApiResponse(urlString);

            if (conn.getResponseCode() != 200) {
                System.out.println("Error: couldn't fetch api response");
                return;
            }
            String jsonResponse = readApiResponse(conn);

            JSONParser parser = new JSONParser();
            JSONObject jsonObj = (JSONObject) parser.parse(jsonResponse);
            JSONObject currentWeatherJson = (JSONObject) jsonObj.get("current");

            String time = (String) currentWeatherJson.get("time");
            System.out.println("Current Time: " + time);

            double temperature = (double) currentWeatherJson.get("temperature_2m");
            System.out.println("Current Temperature (C): " + temperature);

            long relativeHumidity = (long) currentWeatherJson.get("relative_humidity_2m");
            System.out.println("Relative Humidity: " + relativeHumidity);

            double windSpeed = (double) currentWeatherJson.get("wind_speed_10m");
            System.out.println("Weather Description: " + windSpeed);

        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static JSONObject getLocationData(String city) {

        city = city.replaceAll(" ", "+");
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" + city + "&count=1&language=en&format=json";

        try {
            HttpURLConnection conn = fetchApiResponse(urlString);

            if (conn == null) {
                System.out.println("Error: couldn't establish connection to the API.");
                return null;
            }

            if (conn.getResponseCode() != 200) {
                System.out.println("Error: couldn't fetch API response. Response code: " + conn.getResponseCode());
                return null;
            }

            String jsonResponse = readApiResponse(conn);
            if (jsonResponse == null) {
                System.out.println("Error: failed to read API response.");
                return null;
            }

            JSONParser parser = new JSONParser();
            JSONObject resultsJsonObj = (JSONObject) parser.parse(jsonResponse);
            if (resultsJsonObj == null || !resultsJsonObj.containsKey("results")) {
                System.out.println("Error: no results found in the API response.");
                return null;
            }

            JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
            if (locationData == null || locationData.isEmpty()) {
                System.out.println("Error: location data is empty or null.");
                return null;
            }

            JSONObject cityObject = (JSONObject) locationData.get(0);
            if (cityObject == null) {
                System.out.println("Error: city object is null.");
                return null;
            }

            System.out.println("City: " + cityObject.get("name") + ", Country: " + cityObject.get("country") + ", Admin1: " + cityObject.get("admin1") + ", Population: " + cityObject.get("population"));
            return cityObject;

        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readApiResponse(HttpURLConnection conn) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static HttpURLConnection fetchApiResponse(String urlString) {

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            return conn;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

