package io.cogswell.sdk.pubsub;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.cogswell.sdk.pubsub.exceptions.RuntimeParseException;

public class Utils {

    public static Calendar toCalendar(String dateString) {
        try {
            // Android does not support the X timezone flag: http://stackoverflow.com/questions/28373610/android-parse-string-to-date-unknown-pattern-character-x

            //https://code.google.com/p/android/issues/detail?id=8258
            Calendar calendar = GregorianCalendar.getInstance();
            String s = dateString.replace("Z", "+0000");
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(s);
            calendar.setTime(date);
            return calendar;
        } catch (Throwable e) {
            throw new RuntimeParseException("Invalid date format", e);

        }
    }

    /**
     * Simple method to spin up new thread that calls provided Runnable no sooner than the given delay in ms.
     * @param runnable The runnable that will be called after the given delay
     * @param delay The time in milliseconds to wait before calling the given runnable
     * @throws InterruptedException
     */
    public static void setTimeout(final Runnable runnable, final long delay)
    {
        new Thread( new Runnable() {
            public void run() {
                try {
                    Thread.sleep(delay);
                    runnable.run();
                } catch (InterruptedException e) {
                    Log.d("Utils","setTimeout for "+delay+"ms was interrupted", e);
                }
            }
        }).start();
    }
}
