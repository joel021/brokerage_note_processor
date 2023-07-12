package com.api.calculator.stockprice;

import java.sql.Date;
import java.util.Calendar;
import java.util.TimeZone;

public class DateUtils {

    public static Date thirdDayOfWeek(int month, int year, TimeZone timeZone) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(timeZone);
        calendar.set(Calendar.DAY_OF_WEEK, 6);
        calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, 3);
        calendar.set(Calendar.MONTH, month-1);
        calendar.set(Calendar.YEAR, year);
        return new Date(calendar.getTime().getTime());
    }
}
