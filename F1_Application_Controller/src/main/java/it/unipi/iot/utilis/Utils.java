package it.unipi.iot.utilis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.WebLink;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

public final class Utils
{
    public static Map<String, Object> jsonParser(String requestText)
    {
        Map<String, Object> responseJsonObject = new HashMap<String, Object>();

        try
        {
            responseJsonObject = (Map<String, Object>) new Gson().fromJson(requestText, responseJsonObject.getClass());
        }
        catch (JsonParseException exception)
        {
            System.out.println("JSON parsing error.");
            exception.printStackTrace();
            return null;
        }
        return responseJsonObject;
    }

    public static String jsonToString(Map<String, Object> map)
    {
        String res;
        res = (String) new Gson().toJson(map);
        return res;
    }
}
