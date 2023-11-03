package org.ocpt;


import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.ocpt.poc.ITextPdf;
import org.ocpt.utils.FileUtils;
import org.ocpt.utils.HttpRequestUtils;
import org.ocpt.utils.JsonPathUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    private static final List<String> HANDICAPMARKETS = List.of(
            "|Total Points (Over/Under)|",
            "|First Half Total|",
            "|1st Quarter Total|",
            "|2nd Quarter Total|"
    );
    private static final String baseTeamJsonPath = "$.oxip.response.event.@name";
    private static Map<String, String> jsonPathConfig = new HashMap<>();
    private static final String JSONPATH_HANDICAP_FORMAT = "$.oxip.response.event.market[?(@.@marketTemplateName == '%1$s')].@handicap";

    public static void main(String[] args) throws Exception {

        /*
         INPUT
         */

        List<String> inplayEventIDs = List.of("11277770");
        List<String> cleanEventIDs = List.of("11231", "12314245", "12314245", "12314245", "12314245", "12314245", "12314245");

        String state = "az";

        List<String> markets = List.of(
                "|Money Line|",
                "|Match Handicap (2-Way)|",
                "|Alternate Handicap|",
                "|Handicap - Second Half|",
                "|Total Points (Over/Under)|",
                "|First Half Total|",
                "|1st Quarter Total|",
                "|2nd Quarter Total|"
        );


        String alternateSelectionsCSV = "/Users/miguel.pires/Downloads/Files/Inplay/MA/settlement/market_info/preplay_sgpp_markets_info.csv";


        //Optional
        List<String> phases = List.of(
                "Phase 1",
                "Phase 2",
                "Phase 3",
                "Clean Up"
        );

        /*
         INPUT
         */

        List<String> preplaySGPPSelections = FileUtils.csvHandler(alternateSelectionsCSV, ",").stream()
                .filter(l -> l[1].contains("Alternate Spread"))
                .map(l -> l[2])
                .collect(Collectors.toList());

        String alternateSelectionStr = toString(preplaySGPPSelections);

        for (String handicapMarket : HANDICAPMARKETS){
            if(markets.contains(handicapMarket)) {
                jsonPathConfig.put(handicapMarket + "_handicap",
                        String.format(JSONPATH_HANDICAP_FORMAT, handicapMarket));
            }
        }

        String urlFormat = "http://%1$s-obpubfd-prd%1$s.prd.fndlsb.net/dbPublishLatest?template=getEventDetails&system=feeds&displayed=ALL&output=JSON&feedName=POWERS_FEED&returnExternalFeedReferences=Y&settled=ALL&displayed=ALL&event=%2$s";
        String url = String.format(urlFormat, state, inplayEventIDs.get(0));
        String response = HttpRequestUtils.makeRequest("GET", url, null);
        DocumentContext documentContext = JsonPath.parse(response);
        jsonPathConfig.forEach((k,v) -> ITextPdf.CONFIGS.put(k,JsonPathUtils.getNode(documentContext,v)));

        String baseTeam = FileUtils.getSubstring(JsonPathUtils.getNode(documentContext,baseTeamJsonPath), "TEST", "@");
        ITextPdf.CONFIGS.put("alternate_selections", alternateSelectionStr);
        ITextPdf.CONFIGS.put("base_team",baseTeam);
        ITextPdf.CONFIGS.put("inplay_events", toString(inplayEventIDs));
        ITextPdf.CONFIGS.put("clean_events", toString(cleanEventIDs));
        ITextPdf.createPdf(markets, phases);

    }

    private static String toString(List<String> lst){
        return String.join("\n", lst);
    }
}