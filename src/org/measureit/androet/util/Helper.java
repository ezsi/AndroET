package org.measureit.androet.util;

import java.util.Calendar;

/**
 *
 * @author ezsi
 */
public class Helper {
    
    public static Double parseDouble(String value, Double defaultValue){
        try{
            return Double.parseDouble(value);
        }catch (NumberFormatException handled) {
            return defaultValue;
        }
    }
    
    public static int calendarToSeconds(Calendar calendar){
        return (int)(calendar.getTimeInMillis()/1000);
    }

    /**
     * Resets the date, hour, minute, second values in a calendar object.
     */
    public static Calendar resetDate(Calendar calendar){
        calendar.set(Calendar.DATE, 0);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }

}
