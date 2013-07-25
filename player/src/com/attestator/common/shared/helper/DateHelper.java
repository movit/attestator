package com.attestator.common.shared.helper;

import java.util.Date;

public class DateHelper {
    public static boolean isTheSameDate(Date d1, Date d2) {
        if (d1 == null || d2 == null) {
            return false;
        }
        
        long d1DatePart = d1.getTime() / (1000 * 60 * 60 * 24);
        long d2DatePart = d2.getTime() / (1000 * 60 * 60 * 24);
        
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
}
