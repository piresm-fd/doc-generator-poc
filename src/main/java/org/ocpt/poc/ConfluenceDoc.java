package org.ocpt.poc;

import org.apache.commons.text.StringEscapeUtils;
import org.ocpt.Main;
import org.ocpt.utils.FileUtils;
import org.ocpt.utils.HttpRequestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ConfluenceDoc {
    private static final Logger LOGGER = Logger.getLogger(ConfluenceDoc.class.getName());

    public static void generate() throws IOException {
        String table1TemplatePath = "src/main/resources/confluence/configs/table/Table1_Template.csv";
        String table2TemplatePath = "src/main/resources/confluence/configs/table/Table2_Template.csv";
        String postDocumentPath = "src/main/resources/json/confluence/body/post_document.json";
        String docBodyPath = "src/main/resources/confluence/json/doc/body.json";
        String tableRowPath = "src/main/resources/confluence/json/doc/table_row.json";
        String tableCellPath = "src/main/resources/confluence/json/doc/table_cell.json";

        String confUsername = "miguel.pires@fanduel.com";
        String confPassword = "ATATT3xFfGF0YITHah_S8hhorZEZpfktZqJMy5veLy81TB-3cojBMFMG9ehTpJE9FMjlKuad_QiOrRJt2tX088YxMnVSI0BnnDH3QzaHOHEBNCFOIsWZ0Y23af0OiMYbT2A7h97BRH0lX2cbgygJwQr4llPYlo26vPBUXKtBw8yseWyWQHdYqLA=B4A402BB";
        String reqMethod = "POST";
        String reqURL = "https://fanduel.atlassian.net/wiki/rest/api/content";

        Map<String,String> mapVariables = Map.of(
                "ancestor","307566936986",
                "space","~6319c15462fe1e6eac6dfd7d",
                "title","This is my page",
                "empty", "%s",
                "cell","{\"type\":\"text\",\"text\": \"%s\"}",
                "header", "{\"type\":\"text\",\"text\": \"%s\",\"marks\":[{\"type\":\"strong\"}]}",
                "checkmark_emoji","{\"type\":\"emoji\",\"attrs\":{\"shortName\": \":white_check_mark:\",\"id\": \"2705\"}}");


        String postDocument = Objects.requireNonNull(FileUtils.readFileAsString(postDocumentPath));
        String docBody = Objects.requireNonNull(FileUtils.readFileAsString(docBodyPath));

        String[] tableRow = Objects.requireNonNull(FileUtils.readFileAsString(tableRowPath)).split("\\{\\{text}}");
        String[] tableCell = Objects.requireNonNull(FileUtils.readFileAsString(tableCellPath)).split("\\{\\{text}}");

        List<List<String>> table1 = csvHandler(table1TemplatePath, tableCell, mapVariables);
        String formattedTable1 = table1.stream()
                .map(rows -> tableRow[0] + String.join(",", rows) + tableRow[1])
                .collect(Collectors.joining(","));

        docBody = FileUtils.replaceStringVar(docBody, "table1", formattedTable1);

        List<List<String>> table2 = csvHandler(table2TemplatePath, tableCell, mapVariables);
        String formattedTable2 = table2.stream()
                .map(rows -> tableRow[0] + String.join(",", rows) + tableRow[1])
                .collect(Collectors.joining(","));

        docBody = FileUtils.replaceStringVar(docBody, "table2", formattedTable2);

        // This can be optimized
        docBody = docBody.replace(" ","");
        docBody = docBody.replace("\n","");
        docBody = StringEscapeUtils.escapeJava(docBody);
        // ---------------------

        postDocument = FileUtils.replaceStringVar(postDocument, "document", docBody);

        String response = HttpRequestUtils.makeBasicAuthRequest(confUsername, confPassword,
                reqMethod, reqURL, postDocument);

        LOGGER.info(response);
    }

    private static List<List<String>> csvHandler(String path, String[] tableCell, Map<String,String> mapVariables) {
        List<List<String>> table = new ArrayList<>();
        String headerMarkup = mapVariables.get("header");
        String cellMarkup = mapVariables.get("cell");
        try {
            List<String> lines = Files.readAllLines(Paths.get(path));
            String[] headers = lines.get(0).split(";", -1);
            for (int i = 0; i < headers.length; i++) {
                headers[i] = tableCell[0] + String.format(headerMarkup, headers[i]) + tableCell[1];
            }
            table.add(List.of(headers));

            for (int i = 1; i < lines.size(); i++) {
                String[] data = lines.get(i).split(";", -1);
                List<String> row = new ArrayList<>();
                for (String datum : data) {
                    datum = datum.strip();
                    String markupFormat;
                    if (datum.contains("emoji")) {
                        markupFormat = "%s";
                        datum = mapVariables.get("checkmark_emoji");
                    }
                    else if ("".equals(datum)){
                        markupFormat = "%s";
                    }
                    else {
                        markupFormat = cellMarkup;
                    }
                    row.add(tableCell[0] + String.format(markupFormat, datum) + tableCell[1]);
                }
                table.add(row);
            }
        } catch (IOException e) {
            LOGGER.warning("Error reading CSV file: " + e.getMessage());
        }
        return table;
    }
}
