package io.cogswell.sdk.pubsub;

import junit.framework.TestCase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UtilsTest extends TestCase {
    public void testToCalendar() throws Exception {
        // This parse should not throw an exception.
        Utils.toCalendar("2016-02-08T23:31:45.317Z");
    }
}