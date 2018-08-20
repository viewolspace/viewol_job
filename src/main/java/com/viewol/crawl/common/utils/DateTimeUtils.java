package com.viewol.crawl.common.utils;

import org.apache.commons.lang3.time.DateFormatUtils;

public class DateTimeUtils {

    public static String parseDate(String timemillis) {
        return DateFormatUtils.format(Long.parseLong(timemillis),
                "yyyy-MM-dd hh:mm:ss");
    }
}
