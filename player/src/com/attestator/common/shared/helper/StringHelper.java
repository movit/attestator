package com.attestator.common.shared.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("rawtypes")
public final class StringHelper {

    public static String capitalize(String str){
        if( isEmptyOrNull(str) ){
            return str;
        }
        if( str.length()==1 ){
            return str.toUpperCase();
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
    
    public static String capitalizeAll(String str){
        if( isEmptyOrNull(str) ){
            return str;
        }
        
        StringBuilder result = new StringBuilder();
        
        for(String strPart : str.split(" ")) {
            result.append(capitalize(strPart.toLowerCase())).append(" ");
        }
        
        return result.toString().trim();
    }

    public static String stripHtmlTags(String str) {
        if (str != null) {
            return str.replaceAll("<[^>]+>", " ");
        }
        return str;
    }

    public static String stripPrefix(String str, String prefix) {
        if (str != null && prefix != null && str.startsWith(prefix)) {
            return str.substring(prefix.length());
        }
        return str;
    }

    public static String stripSuffix(String str, String suffix) {
        if (str != null && suffix != null && str.endsWith(suffix)) {
            return str.substring(0, str.length() - suffix.length());
        }
        return str;
    }

    public static boolean isEmptyOrNull(String arg) {
        return arg == null || arg.trim().isEmpty();
    }

    /**
     * The same as {@link #isEmptyOrNull(String)}, but also returns true if
     * string is equals to <tt>"null"</tt> string.
     *
     * @see #isEmptyOrNull(String)
     */
    public static boolean isEmptyOrNullString(String arg) {
        return isEmptyOrNull(arg) || "null".equalsIgnoreCase(arg);
    }

    public static String nullToEmptyString(Object arg) {
        return arg != null ? arg.toString() : "";
    }

    public static boolean nullSafeEndsWith(String str, String subStr) {
        return str != null && subStr != null && str.endsWith(subStr);
    }


    public static String combine(String[] strs, String delim, int from, int to) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < to; i++) {
            if (i > from && delim != null) {
                sb.append(delim);
            }
            sb.append(strs[i]);
        }
        return sb.toString();
    }

    public static String combine(String[] strings, String delimiter) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (String s : strings) {
            if (s == null) {
                continue;
            }
            if (first) {
                first = false;
            } else {
                result.append(delimiter);
            }
            result.append(s);
        }
        return result.toString();
    }

    public static String combine(String[] strings, String delimiter, String wrapper) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (String s : strings) {
            if (s == null) {
                continue;
            }
            if (first) {
                first = false;
            } else {
                result.append(delimiter);
            }
            result.append(wrapper).append(s).append(wrapper);
        }
        return result.toString();
    }

    public static String firstNotNull(String... args) {
        if (args != null) {
            for (String arg : args) {
                if (arg != null) {
                    return arg;
                }
            }
        }
        return null;
    }

    public static String formatAddress(boolean isHtml, 
            String addr1, String addr2, String city, String state, String zip){
        String res = "";
        String newline = isHtml ? "<br/>" : "\n";
        if(!isEmptyOrNull(addr1)){
            res += addr1 + newline;
        }
        if(!isEmptyOrNull(addr2)){
            res += addr2 + newline;
        }
        if(!isEmptyOrNull(city)){
            res += city + ", ";
        }
        if(!isEmptyOrNull(state)){
            res += state + " ";
        }
        if(!isEmptyOrNull(zip)){
            res += zip;
        }
        return res;
    }
    
    public static String firstNotEmptyOrNull(String... args) {
        if (args != null) {
            for (String arg : args) {
                if (!isEmptyOrNull(arg)) {
                    return arg;
                }
            }
        }
        return null;
    }

    public static String concatAllNotEmpty(String separator, String... args) {
        if (args == null || args.length == 0) {
            return null;
        }
        if (separator == null) {
            separator = "";
        }
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            if (!isEmptyOrNull(arg)) {
                if (sb.length() > 0) {
                    sb.append(separator);
                }
                sb.append(arg);
            }
        }
        return sb.toString();
    }

    public static String concatAllNotEmpty(String separator, Collection<String> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        String[] array = collection.toArray(new String[collection.size()]);
        return concatAllNotEmpty(separator, array);
    }

    public static String prefixIfNotEmpty(String prefix, String arg) {
        if (isEmptyOrNull(arg)) {
            return arg;
        } else {
            return prefix + arg;
        }
    }

    public static String suffixIfNotEmpty(String suffix, String arg) {
        if (isEmptyOrNull(arg)) {
            return arg;
        } else {
            return arg + suffix;
        }
    }

    public static String encloseIfNotEmpty(String prefix, String suffix, String arg) {
        if (isEmptyOrNull(arg)) {
            return nullToEmptyString(arg);
        } else {
            return nullToEmptyString(prefix) + nullToEmptyString(arg) 
                    + nullToEmptyString(suffix);
        }
    }

    public static String displayFileLength(Long length) {
        if (length == null) {
            return "n/a";
        }
        if (length.longValue() <= 1024) {
            return length + " B";
        }
        if (length.longValue() <= 1024 * 1024) {
            return length.longValue() / 1024 + " KB";
        }
        if (length.longValue() <= 1024 * 1024 * 1024) {
            return length.longValue() / (1024 * 1024) + " MB";
        } else {
            return length.longValue() / (1024 * 1024 * 1024) + " GB";
        }
    }

    public static String notNullOrUnknown(String string) {
        return StringHelper.isEmptyOrNull(string) ? "Unknown" : string;
    }

    public static Double parseDouble(String string) {
        if (string == null) {
            return null;
        }
        try {
            return Double.valueOf(string);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Similar to {@link String#split(java.lang.String)} but unlike it returns
     * all items (does not remove trailing empty items).
     *
     * @param string the string to split.
     * @param separator a literal separator, not a regular expression.
     */
    public static String[] splitBySeparatorToArray(String string, String separator) {
        final List<String> res = splitBySeparatorToList(string, separator);
        if (res == null) {
            return null;
        }
        return res.toArray(new String[res.size()]);
    }
    
    /**
     * Similar to {@link String#split(java.lang.String)} but unlike it returns
     * all items (does not remove trailing empty items).
     *
     * @param string the string to split.
     * @param separator a literal separator, not a regular expression.
     */
    public static List<String> splitBySeparatorToList(String string, String separator) {
        if (string == null) {
            return null;
        }
        final ArrayList<String> res = new ArrayList<String>();
        int startInd = 0;
        int endInd;
        while ((endInd = string.indexOf(separator, startInd)) != -1) {
            res.add(string.substring(startInd, endInd));
            startInd = endInd + separator.length();
        }
        res.add(string.substring(startInd));
        return res;
    }

    public static boolean between(String arg, String boundary1, String boundary2) {
        if (isEmptyOrNull(arg) || isEmptyOrNull(boundary1) || isEmptyOrNull(boundary2)) {
            return false; // anything more meaningful?
        }
        return (arg.compareTo(boundary1) >= 0 && arg.compareTo(boundary2) <= 0)
                || (arg.compareTo(boundary2) >= 0 && arg.compareTo(boundary1) <= 0);
    }

    public static boolean containsIgnoreCase(String string, String subString) {
        if (isEmptyOrNull(string) || isEmptyOrNull(subString)) {
            return NullHelper.nullSafeEquals(string, subString);
        }
        return string.toLowerCase().contains(subString.toLowerCase());
    }
    
    public static boolean containsDigits(String string) {
        if (string != null) {
            // [az] had to do it this way since GWT doesnt't support Pattern
            for (int i = 0; i < string.length(); i++) {
                if (Character.isDigit(string.charAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String emptyToNull(String string) {
        return string != null && string.isEmpty() ? null : string;
    }

    public static String escapeBackslash(String string) {
        if (isEmptyOrNull(string)) {
            return string;
        }
        return string.replace("\\", "\\\\");
    }

    public static boolean nullSafeIn(String what, String... inWhat) {
        if (inWhat == null) {
            return false;
        }
        for (String string : inWhat) {
            if (NullHelper.nullSafeEquals(what, string)) {
                return true;
            }
        }
        return false;
    }

    public static String getHead(String string, int headLength) {
        if (string == null || string.length() <= headLength) {
            return string;
        }
        return string.substring(0, headLength);
    }

    public static String getTail(String string, int tailLength) {
        if (string == null || string.length() <= tailLength) {
            return string;
        }
        return string.substring(string.length() - tailLength);
    }

    public static String nullSafeTrim(String string) {
        if (string == null) {
            return null;
        }
        return string.trim();
    }

    public static String nullSafeToLowerCase(String string) {
        if (string == null) {
            return null;
        }
        return string.toLowerCase();
    }

    public static String encodeToOutlookSafe(String string) {
        int interval = 30;
        StringBuilder b = new StringBuilder(string);
        for (int i = 0; i < b.length(); i += interval) {
            b.insert(i, "-");
        }
        b.append("-");
        return b.toString();
    }

    public static String dencodeFromOutlookSafe(String string) {
        int interval = 30;
        StringBuilder b = new StringBuilder(string);
        for (int i = 0; i < b.length(); i += interval) {
            b.delete(i, i + 1);
            i = i - 1;
        }
        b.delete(b.length() - 1, b.length());
        return b.toString();
    }

    public static String trim(String str) {
        if (str == null) {
            return str;
        } else {
            str = str.trim();
            if (str.isEmpty()) {
                return null;
            } else {
                return str;
            }
        }
    }

    public static boolean nullAndEmptySafeEquals(String arg1, String arg2) {
        String str1 = trim(arg1);
        String str2 = trim(arg2);
        if (str1 == null) {
            return str2 == null;
        } else {
            return str1.equals(str2);
        }
    }

    public static boolean isNotEmptyOrNull(String arg) {
        return !isEmptyOrNull(arg);
    }

    public static String concatAll(String separator, List<String> args) {
        if (NullHelper.isEmptyOrNull(args)) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        String separator2 = null;
        for (String arg : args) {
            if (arg == null) {
                arg = "";
            }
            if (separator2 == null) {
                separator2 = separator;
            } else {
                sb.append(separator2);
            }
            sb.append(arg);
        }
        return sb.toString();
    }

    public static void addNotEmptyOrNull(Collection<String> collection, String string) {
        if (collection != null && isNotEmptyOrNull(string)) {
            collection.add(string);
        }
    }

    public static String nullOrEmptyToValue(String arg, String value) {
        return isEmptyOrNull(arg) ? value : arg;
    }

    public static boolean allEmptyOrNull(String... args) {
        for (String arg : args) {
            if (isNotEmptyOrNull(arg)) {
                return false;
            }
        }
        return true;
    }

    public static boolean allNotEmptyOrNull(String... args) {
        for (String arg : args) {
            if (isEmptyOrNull(arg)) {
                return false;
            }
        }
        return true;
    }

    public static boolean atLeastOneEmptyOrNull(String... args) {
        for (String arg : args) {
            if (isEmptyOrNull(arg)) {
                return true;
            }
        }
        return false;
    }

    public static String escapeRegexpLiteral(String string) {
        if (isEmptyOrNull(string)) {
            return string;
        }
        // Pattern.quote(string) doesn't work in GWT
        final String[] charsToEscape = new String[] {
            /* IMPORTANT: the backslash must be the first in the list; */ 
            /* otherwise some escaping backslashes will be double escaped */ "\\", 
            "-", "[", "]", "/", "{", "}", "(", ")", "*", "+", "?", ".", "^", "$", "|"};
        for (String charToEscape : charsToEscape) {
            string = string.replace(charToEscape, "\\" + charToEscape);
        }
        return string;
    }

    public static void escapeRegexpLiteral(String[] strings) {
        if (NullHelper.isEmptyOrNull(strings)) {
            return;
        }
        for (int i = 0; i < strings.length; i++) {
            strings[i] = escapeRegexpLiteral(strings[i]);
        }
    }

    public static boolean parseBool(String arg) {
        return "true".equalsIgnoreCase(arg);
    }

    public static List<String> trimAndRemoveEmpty(List<String> list) {
        if (list == null) {
            return null;
        }
        final List<String> res = new ArrayList<String>();
        for (String item : list) {
            item = nullSafeTrim(item);
            if (isNotEmptyOrNull(item)) {
                res.add(item);
            }
        }
        return res;
    }
    
    public static String generateTimeZoneId(int offsetMinutes) {
        if (offsetMinutes == 0) {
            return "GMT";
        }
        String str = "";
        if (offsetMinutes < 0) {
            offsetMinutes = -offsetMinutes;
            str = "GMT-";
        } else {
            str = "GMT+";
        }
        int hour = offsetMinutes / 60;
        int mins = offsetMinutes % 60;
        if (mins == 0) {
          return str + Integer.toString(hour);
        }
        return str + Integer.toString(hour) + ":" + Integer.toString(mins);
    }
    
    public static String normalizeTimeZoneAbbr(String dateStr) {
        if (dateStr == null) {
            return null;
        }
        
        dateStr = dateStr.replaceAll("(?i)(HNP|PT)", "PST");
        dateStr = dateStr.replaceAll("(?i)(HNE|ET)", "EST");
        dateStr = dateStr.replaceAll("(?i)(HAA)",    "ADT");
        dateStr = dateStr.replaceAll("(?i)(HAY)",    "AKDT");
        dateStr = dateStr.replaceAll("(?i)(HNY)",    "AKST");
        dateStr = dateStr.replaceAll("(?i)(HNA)",    "AST");
        dateStr = dateStr.replaceAll("(?i)(HAC)",    "CDT");
        dateStr = dateStr.replaceAll("(?i)(HNC)",    "CST");
        dateStr = dateStr.replaceAll("(?i)(HAE)",    "EDT");
        dateStr = dateStr.replaceAll("(?i)(HAR)",    "MDT");
        dateStr = dateStr.replaceAll("(?i)(HNR)",    "MST");
        dateStr = dateStr.replaceAll("(?i)(HAT)",    "NDT");
        dateStr = dateStr.replaceAll("(?i)(HNT)",    "NST");
        dateStr = dateStr.replaceAll("(?i)(HAP)",    "PDT");
        
        return dateStr;
    }

    public static boolean nullSafeMatches(String string, String regex) {
        if (string == null || regex == null) {
            return false;
        }
        return string.matches(regex);
    }

    public static boolean nullSafeDoesNotMatch(String string, String regex) {
        return !nullSafeMatches(string, regex);
    }

    public static String trimAndShorten(String string, int maxLength) {
        if (string == null) {
            return null;
        }
        string = string.trim();
        return string.length() <= maxLength ? string : string.substring(0, maxLength);
    }

    public static int nullSafeLength(String string){
        return string==null ? 0 : string.length();
    }

    public static <E extends Enum> E parseEnum(String kindStr, Class<E> enumClass) {
        kindStr = nullSafeTrim(kindStr);
        if (isEmptyOrNull(kindStr) || enumClass == null) {
            return null;
        }
        for (E e : enumClass.getEnumConstants()) {
            if (e.name().equalsIgnoreCase(kindStr)) {
                return e;
            }
        }
        return null;
    }
    
    public static String prependUpToLen(char c, int len, String str) {
        if (str == null) {
            str = "";
        }
        while (str.length() < len) {
            str = c + str;
        }
        return str;
    }
    
    private StringHelper() {
    }    
}
