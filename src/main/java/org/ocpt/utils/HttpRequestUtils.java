package org.ocpt.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;

public class HttpRequestUtils {

    private HttpRequestUtils() {}

    private static final Logger LOGGER = Logger.getLogger(HttpRequestUtils.class.getName());

    public static String makeBasicAuthRequest(String username, String password, String method, String urlString,
                                                  String requestBody) throws IOException {
        URL url = new URL(urlString);
        // Open a connection to the URL
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // Set Basic Authentication header
        String authString = username + ":" + password;
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
        connection.setRequestProperty("Authorization", authHeader);

        return makeRequest(connection, method, requestBody);
    }

    public static String makeRequest(String method, String urlString,
                                     String requestBody) throws IOException {
        URL url = new URL(urlString);
        // Open a connection to the URL
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        return makeRequest(connection, method, requestBody);
    }

    private static String makeRequest(HttpURLConnection connection, String method, String requestBody) throws IOException {

        // Set the request method
        connection.setRequestMethod(method);

        // Enable input/output streams
        connection.setDoOutput(true);
        connection.setDoInput(true);

        // Set other headers if needed
        connection.setRequestProperty("Content-Type", "application/json");

        if(requestBody != null) {
            // Write the request body to the connection
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.writeBytes(requestBody);
                wr.flush();
            }
        }


        // Get the response code
        int responseCode = connection.getResponseCode();
        LOGGER.info("Response Code: " + responseCode);

        // Read the response
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        // Close the connection
        connection.disconnect();

        return response.toString();
    }
}
