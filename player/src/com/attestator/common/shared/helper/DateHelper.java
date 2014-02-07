package com.attestator.common.shared.helper;

import java.util.Date;

public class DateHelper {
    public static final long MILLISECONDS_IN_DAY = 1000 * 60 * 60 * 24; 
    
    public static boolean beforeOrEqualOrNull(Date before, Date when) {
        if (before == null) {
            return true;
        }
        return before.before(when) || before.equals(when);
    }

    public static boolean beforeOrNull(Date before, Date when) {
        if (before == null) {
            return true;
        }
        return before.before(when);
    }

    public static boolean afterOrEqualOrNull(Date after, Date when) {
        if (after == null) {
            return true;
        }
        return after.after(when) || after.equals(when);
    }
    
    public static boolean afterOrNull(Date after, Date when) {
        if (after == null) {
            return true;
        }
        return after.after(when);
    }
    
    public static boolean isTheSameDate(Date d1, Date d2) {
        if (d1 == null || d2 == null) {
            return false;
        }
        
        long d1DatePart = d1.getTime() / MILLISECONDS_IN_DAY;
        long d2DatePart = d2.getTime() / MILLISECONDS_IN_DAY;
        
        return d1DatePart == d2DatePart;
    }

    public static String formatTimeValue(long sec) {
        long h = sec / (60 * 60);
        long m = (sec - h * 60 * 60) / 60;
        long s = sec % 60;                
        
        return
                StringHelper.prependUpToLen('0', 2, "" + h) + ":"
              + StringHelper.prependUpToLen('0', 2, "" + m) + ":"
              + StringHelper.prependUpToLen('0', 2, "" + s);
    }
    
    public static long getDatePart(Date date) {
        return date.getTime() - getTimePart(date);
    }
    
    public static long getTimePart(Date date) {
        return date.getTime() % MILLISECONDS_IN_DAY;
    }
    
}
