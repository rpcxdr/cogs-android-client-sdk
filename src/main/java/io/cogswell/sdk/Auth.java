package io.cogswell.sdk;

import android.util.Base64;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class Auth {
    private static final String validKeyParts = "RWA";
    private static TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
    private static SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    static {
        iso8601Format.setTimeZone(utcTimeZone);
    }

    public static class AuthKeyError extends Exception {
        AuthKeyError(String message) {
            super(message);
        }
        AuthKeyError(Exception cause) {
            super(cause);
        }
    }

    public static class PayloadHeaders {
        final public String payloadBase64;
        final public String payloadHmac;

        public PayloadHeaders(String payloadBase64, String payloadHmac) {
            this.payloadBase64 = payloadBase64;
            this.payloadHmac = payloadHmac;
        }
    }

    private static class Key {
        final public String perm;
        final public String identity;
        final public String permKey;

        /**
         * Parse and validate a project key.
         *
         * @param keyString the key.
         * @throws AuthKeyError if the key is has an invalid format.
         */
        public Key(String keyString) throws AuthKeyError {
            if (keyString == null) {
                throw new AuthKeyError("The key string cannot be null");
            }

            String[] keyParts = keyString.split("-");
            if (keyParts.length !=3 ) {
                throw new AuthKeyError("Invalid format for project key.");
            }

            if (!validKeyParts.contains(keyParts[0])) {
                throw new AuthKeyError("Invalid permission prefix for project key. The valid prefixes are "+validKeyParts);
            }

            this.perm = keyParts[0];
            this.identity = keyParts[1];
            this.permKey = keyParts[2];
        }
    }

    /**
     * Compute the auth hmac for the specified keys.
     *
     * @param keys
     * @return the auth hmac
     * @throws AuthKeyError
     */
    public static PayloadHeaders socketAuth(String[] keys) throws AuthKeyError {
        if (keys.length == 0) {
            throw new AuthKeyError("No keys supplied.");
        }
        // NOTE: For backward compatibility, we cannot use Java's map/reduce streams.

        // Remove any duplicate keys: last in wins.
        Map<String, Key> permissionToKeyMap = new HashMap<>();
        for (String keyString : keys) {
            Key key = new Key(keyString);
            permissionToKeyMap.put(key.perm, key);
        }

        // Concatenate the list of requested permissions.
        String perms = "";
        for (Key key : permissionToKeyMap.values()) {
            perms += key.perm;
        }

        // All key identities sholud be the same, so just get the first one.
        String identity = permissionToKeyMap.values().iterator().next().identity;
        String timeISO8601 = iso8601Format.format(new Date());;

        String payload = String.format(
            "{"+
                    "\"identity\":\"%s\","+
                    "\"permissions\":\"%s\","+
                    "\"security_timestamp\":\"%s\""+
             "}",
             identity, perms, timeISO8601);

        // Compute and xor the hmacs.
        byte[] hmacXored = new byte[32];
        for (Key key : permissionToKeyMap.values()) {
            try {
                String hmac = Methods.getHmac(payload, key.permKey);

                byte[] hmacHex = Hex.decodeHex(hmac.toCharArray());
                mutateXor(hmacXored, hmacHex);
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException | DecoderException e) {
                throw new AuthKeyError( e );
            }
        }

        try {
            String payloadBase64 = Base64.encodeToString(payload.getBytes("UTF-8"), Base64.NO_WRAP);
            String payloadHmac = new String(Hex.encodeHex(hmacXored));
            PayloadHeaders ph = new PayloadHeaders(payloadBase64, payloadHmac);
            return ph;
        } catch (UnsupportedEncodingException e) {
            throw new AuthKeyError( e );
        }
    }

    /**
     * Mutate the target array by xoring it with the source array.  The arrays must be the same length.
     *
     * @param target array to be changed
     * @param source array the target will be xored with
     */
    private static void mutateXor(byte[] target, byte[] source) {
        if (target == null || source == null || target.length != source.length) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < target.length; i++) {
            target[i] = (byte) (target[i] ^ source[i]);
        }
    }
}
