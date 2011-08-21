package org.measureit.androet.util;

import java.util.Calendar;

/**
 *
 * @author ezsi
 */
public class Helper {
    
    public static double parseDouble(String value, double defaultValue){
        try{
            return Double.parseDouble(value);
        }catch (NumberFormatException handled) {
            return defaultValue;
        }
    }
    
    public static int calendarToSeconds(Calendar calendar){
        return (int)(calendar.getTimeInMillis()/1000);
    }
    
}
