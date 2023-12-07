package org.ocpt;


import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.StringUtils;
import org.ocpt.poc.ITextPdf;
import org.ocpt.utils.FileUtils;
import org.ocpt.utils.HttpRequestUtils;
import org.ocpt.utils.JsonPathUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        List<String> inplayEventIDs = List.of("11367291","11367292","11367293","11367294","11367295","11367296","11367297","11367299","11367300","11367301");
        List<String> inplayEventIDs2 = List.of("11367302","11367303","11367306","11367307","11367308","11367310","11367311");
        List<String> cleanEventIDs = List.of("11367302","11367303","11367306","11367307","11367308","11367310","11367311","11367312","11367313","11367314","11367315","11367316","11367332");

        String state = "az";

        List<String> markets = List.of(
                "|Money Line|",
                "|Match Handicap (2-Way)|",
                "|Total Points (Over/Under)|",
                "|First Half Total|",
                "|Alternate Handicap|",
                "|Second Half Handicap|",
                "|1st Quarter Total|",
                "|2nd Quarter Total|"
        );

        //String alternateSelectionsCSV = "/Users/miguel.pires/Documents/GitHub/pe-tests-archive/sportsbook/prod_utils/FILES/inPlay/MA/settlement/market_info/preplay_sgpp_markets_info.csv";
        String alternateSelectionsCSV = "/Users/miguel.pires/Downloads/preplay_sgpp_markets_info.csv";


        //Optional
        List<String> phases = List.of(
                "Phase 0",
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
                .sorted()
                .distinct()
                .collect(Collectors.toList());

        //String alternateSelectionStr = "Baltimore Ravens (-10.5)\nBaltimore Ravens (-14.5)\nBaltimore Ravens (-15.5)\nBaltimore Ravens (-18.5)\nBaltimore Ravens (-20.5)\nBaltimore Ravens (-21.5)\nBaltimore Ravens (-22.5)\nBaltimore Ravens (-23.5)\nBaltimore Ravens (-24.5)\nBuffalo Bills (-10.5)\nBuffalo Bills (-14.5)\nBuffalo Bills (-15.5)\nBuffalo Bills (-18.5)\nBuffalo Bills (-20.5)\nBuffalo Bills (-21.5)\nBuffalo Bills (-22.5)\nBuffalo Bills (-23.5)\nBuffalo Bills (-24.5)\nCincinnati Bengals (-10.5)\nCincinnati Bengals (-14.5)\nCincinnati Bengals (-15.5)\nCincinnati Bengals (-18.5)\nCincinnati Bengals (-20.5)\nCincinnati Bengals (-21.5)\nCincinnati Bengals (-22.5)\nCincinnati Bengals (-23.5)\nCincinnati Bengals (-24.5)\nCleveland Browns (-10.5)\nCleveland Browns (-14.5)\nCleveland Browns (-15.5)\nCleveland Browns (-18.5)\nCleveland Browns (-20.5)\nCleveland Browns (-21.5)\nCleveland Browns (-22.5)\nCleveland Browns (-23.5)\nCleveland Browns (-24.5)\nHouston Texans (-10.5)\nHouston Texans (-14.5)\nHouston Texans (-15.5)\nHouston Texans (-18.5)\nHouston Texans (-20.5)\nHouston Texans (-21.5)\nHouston Texans (-22.5)\nHouston Texans (-23.5)\nHouston Texans (-24.5)\nIndianapolis Colts (-10.5)\nIndianapolis Colts (-14.5)\nIndianapolis Colts (-15.5)\nIndianapolis Colts (-18.5)\nIndianapolis Colts (-20.5)\nIndianapolis Colts (-21.5)\nIndianapolis Colts (-22.5)\nIndianapolis Colts (-23.5)\nIndianapolis Colts (-24.5)\nJacksonville Jaguars (-10.5)\nJacksonville Jaguars (-14.5)\nJacksonville Jaguars (-15.5)\nJacksonville Jaguars (-18.5)\nJacksonville Jaguars (-20.5)\nJacksonville Jaguars (-21.5)\nJacksonville Jaguars (-22.5)\nJacksonville Jaguars (-23.5)\nJacksonville Jaguars (-24.5)\nMiami Dolphins (-10.5)\nMiami Dolphins (-14.5)\nMiami Dolphins (-15.5)\nMiami Dolphins (-18.5)\nMiami Dolphins (-20.5)\nMiami Dolphins (-21.5)\nMiami Dolphins (-22.5)\nMiami Dolphins (-23.5)\nMiami Dolphins (-24.5)\nNew England Patriots (-10.5)\nNew England Patriots (-14.5)\nNew England Patriots (-15.5)\nNew England Patriots (-18.5)\nNew England Patriots (-20.5)\nNew England Patriots (-21.5)\nNew England Patriots (-22.5)\nNew England Patriots (-23.5)\nNew England Patriots (-24.5)\nNew York Jets (-10.5)\nNew York Jets (-14.5)\nNew York Jets (-15.5)\nNew York Jets (-18.5)\nNew York Jets (-20.5)\nNew York Jets (-21.5)\nNew York Jets (-22.5)\nNew York Jets (-23.5)\nNew York Jets (-24.5)";
        String alternateSelectionStr = toString(preplaySGPPSelections);

        for (String handicapMarket : HANDICAPMARKETS){
            if(markets.contains(handicapMarket)) {
                jsonPathConfig.put(handicapMarket + "_handicap",
                        String.format(JSONPATH_HANDICAP_FORMAT, handicapMarket));
            }
        }

        String urlFormat = "http://%1$s-obpubfd-prd%1$s.prd.fndlsb.net/dbPublishLatest?template=getEventDetails&system=feeds&displayed=ALL&output=JSON&feedName=POWERS_FEED&returnExternalFeedReferences=Y&settled=ALL&displayed=ALL&event=%2$s";
        String url = String.format(urlFormat, state, inplayEventIDs.get(1));
        String response = HttpRequestUtils.makeRequest("GET", url, null);
        DocumentContext documentContext = JsonPath.parse(response);
        jsonPathConfig.forEach((k,v) -> ITextPdf.CONFIGS.put(k,JsonPathUtils.getNode(documentContext,v)));

        String baseTeam = FileUtils.getSubstring(JsonPathUtils.getNode(documentContext,baseTeamJsonPath), "TEST ", " At");
        ITextPdf.CONFIGS.put("alternate_selections", alternateSelectionStr);
        ITextPdf.CONFIGS.put("base_team",baseTeam);
        ITextPdf.CONFIGS.put("inplay_events", toString(inplayEventIDs));
        ITextPdf.CONFIGS.put("inplay_events2", toString(inplayEventIDs2));
        ITextPdf.CONFIGS.put("clean_events", toString(cleanEventIDs));
        ITextPdf.createPdf(markets, phases);
/*
        List<String> newList = Stream.concat(inplayEventIDs.stream(), cleanEventIDs.stream())
                .sorted()
                .collect(Collectors.toList());

        //newList = Stream.of("11367294","11367296","11367302","11367300","11367315","11367308","11367299","11367293","11367292","11367295","11367306","11367314","11367312","11367316","11367310","11367297","11367303","11367301","11367307","11367291","11367313","11367311","11367332").sorted().collect(Collectors.toList());
        generateList(state, newList);
*/
    }

    private static String toString(List<String> lst){
        return String.join("\n", lst);
    }

    private static void generateList(String state, List<String> lst) throws IOException {
        String urlFormat = "http://%1$s-obpubfd-prd%1$s.prd.fndlsb.net/dbPublishLatest?template=getEventDetails&system=feeds&displayed=ALL&output=JSON&feedName=POWERS_FEED&returnExternalFeedReferences=Y&settled=ALL&displayed=ALL&event=%2$s";
        String jPath = "$.oxip.response.event.externalReference[?(@.@name == 'BETFAIR')].@id";
        String jFormat = "{\n  betfairId : \"%1$s\",\n  rampId: \"%2$s\"\n}";
        List<String> str = new ArrayList<>();
        List<String> str2 = new ArrayList<>();
        for(String s : lst){
            String url = String.format(urlFormat, state, s);
            String response = HttpRequestUtils.makeRequest("GET", url, null);
            DocumentContext documentContext = JsonPath.parse(response);
            String betfairId = JsonPathUtils.getNode(documentContext,jPath);
            str.add(String.format(jFormat,betfairId,s));
            str2.add("- "+betfairId);
        }
        System.out.println(StringUtils.join(str,",\n"));
        System.out.println("\n\n\n");
        System.out.println(StringUtils.join(str2,"\n"));
    }
}