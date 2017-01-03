package io.cogswell.sdk;

//import org.junit.*;
import junit.framework.TestCase;

public class AuthTest extends TestCase {
    //public AuthTest() {super();}
//    @Test
    public void testSocketAuth() throws Exception {
        String[] keys = {
            "R-2cf87f44ca4b21a1fdd38e7553022075-b351e21fc356b9af2897886210fb6b72cfdfa8e013a9c38c7ff238dc8b804f71",
            "W-2cf87f44ca4b21a1fdd38e7553022075-c2fe40dc8c31cde3f04eb29337661d5969edb5c9b70f4107c04ea7ff9eaf2a3c",
            "A-2cf87f44ca4b21a1fdd38e7553022075-35b8aa058118f1a26f779b55390b3b78faa3c8bd408e4cfb10ebc9fb1090f94940310556ecfbdc7a4af41b63cb2359413cf300fac56d7443c0c47612655ab825"
        };

        Auth.socketAuth(keys);
    }

}