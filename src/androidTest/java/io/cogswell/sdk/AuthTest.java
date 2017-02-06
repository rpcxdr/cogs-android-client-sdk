package io.cogswell.sdk;

//import org.junit.*;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class AuthTest extends TestCase {
    //public AuthTest() {super();}
//    @Test
    public void testSocketAuth() throws Exception {
        List<String> keys = new ArrayList<>();
        keys.add("A-*-*");
        keys.add("R-*-*");
        keys.add("W-*-*");

        Auth.socketAuth(keys);
    }

}