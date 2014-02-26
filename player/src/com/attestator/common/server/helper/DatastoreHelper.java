package com.attestator.common.server.helper;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.mongodb.morphia.query.CriteriaContainer;
import org.mongodb.morphia.query.Query;

import com.attestator.common.shared.SharedConstants;
import com.attestator.common.shared.helper.DateHelper;
import com.attestator.common.shared.helper.NullHelper;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.ModificationDateAwareVO;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;

public class DatastoreHelper {
    private final static Pattern OR_REGEX  = Pattern.compile("(?i)\\s+or\\s+");
    private final static Pattern AND_REGEX = Pattern.compile("(?i)\\s+and\\s+");    
    private final static Pattern NOT_ALNUM_REGEX = Pattern.compile("[^\\p{L}\\d]+");

    private static final Logger logger = Logger.getLogger(DatastoreHelper.class);
    
    public static <T> void addOrders(Query<T> q, List<? extends SortInfo> orders) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        boolean idAdded = false;
        for (SortInfo order: orders) {
            if (StringHelper.isEmptyOrNull(order.getSortField())) {
                logger.warn("Incorrect SortInfo");
                continue;
            }
            if (i > 0) {
                sb.append(",");
            }
            if (order.getSortDir() == SortDir.DESC) {
                sb.append("-");
            }
            sb.append(order.getSortField());
            
            if ("_id".equals(order.getSortField())) {
                idAdded = true;
            }
            
            i++;
        }
        
        // To prevent order of equal elements
        if (!idAdded) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("_id");
        }
        
        q.order(sb.toString());
    }
    
    public static <T> void addDefaultOrder(Class<T> clazz, Query<T> q) {
        if (ModificationDateAwareVO.class.isAssignableFrom(clazz)) {
            q.order("created, _id");
        }
        else {
            q.order("_id");
        }
    }
    
    public static <T> void addFilters(Query<T> q, List<FilterConfig> filters) {        
        Class<T> clazz = q.getEntityClass();
        
        for (FilterConfig filter: filters) {
            
            if (StringHelper.isEmptyOrNull(filter.getValue())
            ||  StringHelper.isEmptyOrNull(filter.getField())) {
                logger.warn("Incorrect FilterConfig");
                continue;
            }
            
            CriteriaContainer container = null;
            String[] fields = null;
            if (OR_REGEX.matcher(filter.getField()).find()) {
                container = q.or();
                fields = OR_REGEX.split(filter.getField());
            }
            else if (AND_REGEX.matcher(filter.getField()).find()) {
                container = q.and();
                fields = AND_REGEX.split(filter.getField());
            }            
            else {
                container = q.and();
                fields = new String[] {filter.getField()};
            }
                        
            for (String field: fields) {
                Object filterValue = getNatveFilterValue(clazz, field, filter.getValue());                
                
                if (filterValue == null) {
                    continue;
                }
                
                if (filter.getComparison() == null 
                || "eq".equals(filter.getComparison())) {
                    if (Boolean.FALSE.equals(filterValue)) {
                        container.or(
                            container.criteria(field).equal(filterValue),
                            container.criteria(field).doesNotExist()
                        );
                    }
                    else {
                        container.criteria(field).equal(filterValue);
                    }
                }
                else if ("notEq".equals(filter.getComparison())) {
                    container.criteria(field).notEqual(filterValue);
                }
                else if ("before".equals(filter.getComparison())) {
                    container.criteria(field).lessThan(filterValue);
                }
                else if ("after".equals(filter.getComparison())) {
                    if (filterValue instanceof Date) {
                        filterValue = new Date(((Date) filterValue).getTime() + DateHelper.MILLISECONDS_IN_DAY);
                    }
                    container.criteria(field).greaterThan(filterValue);
                }
                else if ("on".equals(filter.getComparison())) {                    
                    if (filterValue instanceof Date) {
                        Date fromTime = (Date)filterValue;
                        Date toTime = new Date(((Date) filterValue).getTime() + DateHelper.MILLISECONDS_IN_DAY);
                        container.and(
                            container.criteria(field).greaterThan(fromTime),
                            container.criteria(field).lessThan(toTime)
                        );
                    }
                    else {
                        container.criteria(field).equal(filterValue);
                    }
                }
                else if ("lt".equals(filter.getComparison())) {                    
                    container.criteria(field).lessThan(filterValue);
                }
                else if ("gt".equals(filter.getComparison())) {                    
                    container.criteria(field).greaterThan(filterValue);
                }
                else if ("contains".equals(filter.getComparison())) {
                    String strFilterValue = (String)filterValue;
                    //Replace NON unicode letters or digits to spaces 
                    strFilterValue = strFilterValue.trim().toLowerCase();
                    strFilterValue = NOT_ALNUM_REGEX.matcher(strFilterValue).replaceAll(" ");  
                    strFilterValue = StringHelper.escapeRegexpLiteral(strFilterValue);
                    
                    String[] keywords = strFilterValue.split("\\s+");
                    List<Pattern> keywordPatterns = new ArrayList<Pattern>();
                    for (String keyword: keywords) {
                        if (StringHelper.isEmptyOrNull(keyword)) {
                            continue;
                        }
                        keywordPatterns.add(Pattern.compile(keyword, Pattern.CASE_INSENSITIVE));                    
                    }
                    
                    if (!keywordPatterns.isEmpty()) {
                        container.criteria(field).hasAllOf(keywordPatterns);
                    }
                }
                else if ("notIn".equals(filter.getComparison())) {
                    List<String> values = StringHelper.splitBySeparatorToList(filter.getValue(), ", ");
                    
                    if (!NullHelper.isEmptyOrNull(values)) {
                        container.criteria(field).notIn(values);
                    }
                }
            }
        }
    }
    
    public static <T> void addLoadConfig(Query<T> q, FilterPagingLoadConfig loadConfig) {
        Class<T> clazz = q.getEntityClass();
        
        // Add filters
        if (!NullHelper.nullSafeIsEmpty(loadConfig.getFilters())) {
            addFilters(q, loadConfig.getFilters());
        }
        
        // Add order
        if (!NullHelper.nullSafeIsEmpty(loadConfig.getSortInfo())) {
            addOrders(q, loadConfig.getSortInfo());
        }
        else {
            addDefaultOrder(clazz, q);
        }

    }    
    
    public static <T> Object getNatveFilterValue(Class<T> clazz, String filterFieldName, String stringFilterValue) {
        try {
            if ("_id".equals(filterFieldName)) {
                filterFieldName = "id";
            }
            
            Field nativeField = ReflectionHelper.getDeclaredField(clazz, filterFieldName, true);
            
            if (Integer.class.isAssignableFrom(nativeField.getType())) {
                return new Integer(stringFilterValue);
            }
            else if (Long.class.isAssignableFrom(nativeField.getType())) {
                return new Long(stringFilterValue);
            }
            else if (Double.class.isAssignableFrom(nativeField.getType())) {
                return new Double(stringFilterValue);
            }
            else if (Boolean.class.isAssignableFrom(nativeField.getType())) {
                return Boolean.valueOf(stringFilterValue);
            }
            else if (Date.class.isAssignableFrom(nativeField.getType())) {
                return (new SimpleDateFormat(SharedConstants.DATE_TRANSFER_FORMAT)).parse(stringFilterValue);
            }
            return stringFilterValue;
        }
        catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            return null;
        }
    }
}
