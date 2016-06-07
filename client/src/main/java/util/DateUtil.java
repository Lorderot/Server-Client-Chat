package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    private static SimpleDateFormat formatter
            = new SimpleDateFormat("HH:mm:ss");

    public static String toString(Date time) {
        return formatter.format(time);
    }

    public static Date fromString(String time) throws ParseException {
        return formatter.parse(time);
    }
}
