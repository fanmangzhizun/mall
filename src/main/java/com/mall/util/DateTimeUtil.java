package com.mall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * Created by faithpercious on 2017/10/19.
 */
public class DateTimeUtil {
    public static final String STANDAD_FORMAT="yyyy-MM-dd HH:mm:ss";
    public static Date strToDate(String dataTimeStr,String formatStr){
        DateTimeFormatter dateTimeFormatter= DateTimeFormat.forPattern(formatStr);
        DateTime dateTime=dateTimeFormatter.parseDateTime(dataTimeStr);
        return dateTime.toDate();
    }
    public static String  DateTostr(Date date,String formatStr){
        if (date==null){
            return StringUtils.EMPTY;
        }
        DateTime dateTime=new DateTime(date);
        return dateTime.toString(formatStr);
    }
    public static Date strToDate(String dataTimeStr){
        DateTimeFormatter dateTimeFormatter= DateTimeFormat.forPattern(STANDAD_FORMAT);
        DateTime dateTime=dateTimeFormatter.parseDateTime(dataTimeStr);
        return dateTime.toDate();
    }
    public static String  DateTostr(Date date){
        if (date==null){
            return StringUtils.EMPTY;
        }
        DateTime dateTime=new DateTime(date);
        return dateTime.toString(STANDAD_FORMAT);
    }
}
