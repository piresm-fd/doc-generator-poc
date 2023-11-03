package org.ocpt.utils;

import com.jayway.jsonpath.DocumentContext;
import net.minidev.json.JSONArray;

public class JsonPathUtils {

    private JsonPathUtils() {}

    public static String getNode(DocumentContext documentContext, String jsonPath){
        Object node = documentContext.read(jsonPath);
        String nodeStr = null;
        if (node instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) node;
            nodeStr = jsonArray.get(0).toString();
        }
        else{
            nodeStr = node.toString();
        }
        return nodeStr;
    }
}
