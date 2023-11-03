package org.ocpt.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileUtils {

    private static final Logger LOGGER = Logger.getLogger(FileUtils.class.getName());

    private FileUtils() {}

    public static List<String[]> csvHandler(String path, String splitter) {
        List<String[]> table = null;
        try {
            table = Files.readAllLines(Paths.get(path)).stream()
                    .map(line -> line.split(splitter, -1))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return table;
    }

    public static String replacePlaceholder(String str, Map<String, String> vars) {
        // Use a regular expression to find all occurrences of {{placeholder}}
        Pattern pattern = Pattern.compile("\\{\\{([^}]+)}}");
        Matcher matcher = pattern.matcher(str);

        // Replace each match with the corresponding value from the map
        return matcher.replaceAll(match -> vars.getOrDefault(match.group(1), "{{NOT FOUND}}"));
    }

    public static String readFileAsString(String path) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded);
        } catch (IOException e) {
            LOGGER.warning("Error reading file: " + e.getMessage());
            return null; // or throw an exception if you want to handle it elsewhere
        }
    }

    public static String replaceStringVar(String inputString, String searchString, String replacementString) {
        return inputString.replace("{{" + searchString + "}}", replacementString);
    }

    public static String getSubstring(String input, String start, String finish) {
        // Define a regex pattern to match the substring between "TEST" and "@"
        Pattern pattern = Pattern.compile(start+"\\s*(.*?)\\s*"+finish, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);

        // Check if the pattern is found
        if (matcher.find()) {
            // Extract the matched group
            return matcher.group(1).trim();
        } else {
            // Handle the case when the pattern is not found
            return "Substring not found";
        }
    }
}
