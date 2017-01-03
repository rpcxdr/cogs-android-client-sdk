package io.cogswell.sdk;

//import org.junit.*;
import junit.framework.TestCase;

public class AuthTest extends TestCase {
    //public AuthTest() {super();}
//    @Test
    public void testSocketAuth() throws Exception {
        String[] keys = {
                "A-*-*",
                "R-*-*",
                "W-*-*"
        };

        Auth.socketAuth(keys);
    }

}