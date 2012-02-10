package com.kokakiwi.mclauncher.utils.java;

import java.util.Map;
import java.util.regex.Pattern;

public class StringFormatter
{
    public static String format(String from, Map<String, String> keys)
    {
        String finalString = from;
        for (final String key : keys.keySet())
        {
            final String value = keys.get(key);
            
            if(value != null)
            {
                finalString = finalString.replaceAll("\\[(.*)\\{" + key.toUpperCase() + "\\}(.*)\\]", "$1{" + key.toUpperCase() + "}$2");
                finalString = finalString.replaceAll("\\{" + key.toUpperCase()
                        + "\\}", value);
            }
            else
            {
                finalString = finalString.replaceAll("\\[(.*)\\{" + key.toUpperCase() + "\\}(.*)\\]", "");
            }
        }
        return finalString;
    }
}
