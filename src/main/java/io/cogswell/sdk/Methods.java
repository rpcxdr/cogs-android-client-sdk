package io.cogswell.sdk;


import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Methods {
    public static final Charset UTF_8 = Charset.forName("utf-8");

    private static final String ALGORITHM_HMAC_SHA256 = "HmacSHA256";
    private static final ThreadLocal<SimpleDateFormat> formatters = new ThreadLocal<>();

    private static SimpleDateFormat getIsoFormatter() {
        SimpleDateFormat formatter = formatters.get();

        if (formatter == null) {
            formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            formatters.set(formatter);
        }

        return formatter;
    }

    public static String isoNow() {
        return getIsoFormatter().format(new Date());
    }

    public static String _printBase64Binary(byte[] input) {
        return _printBase64Binary(input, 0, input.length);
    }

    public static String _printBase64Binary(byte[] input, int offset, int len) {
        char[] buf = new char[((len+2)/3)*4];
        int ptr = _printBase64Binary(input,offset,len,buf,0);
        assert ptr==buf.length;
        return new String(buf);
    }

    public static int _printBase64Binary(byte[] input, int offset, int len, char[] buf, int ptr) {
        for( int i=offset; i<len; i+=3 ) {
            switch( len-i ) {
                case 1:
                    buf[ptr++] = encode(input[i]>>2);
                    buf[ptr++] = encode(((input[i])&0x3)<<4);
                    buf[ptr++] = '=';
                    buf[ptr++] = '=';
                    break;
                case 2:
                    buf[ptr++] = encode(input[i]>>2);
                    buf[ptr++] = encode(
                            ((input[i]&0x3)<<4) |
                                    ((input[i+1]>>4)&0xF));
                    buf[ptr++] = encode((input[i+1]&0xF)<<2);
                    buf[ptr++] = '=';
                    break;
                default:
                    buf[ptr++] = encode(input[i]>>2);
                    buf[ptr++] = encode(
                            ((input[i]&0x3)<<4) |
                                    ((input[i+1]>>4)&0xF));
                    buf[ptr++] = encode(
                            ((input[i+1]&0xF)<<2)|
                                    ((input[i+2]>>6)&0x3));
                    buf[ptr++] = encode(input[i+2]&0x3F);
                    break;
            }
        }
        return ptr;
    }

    public static char encode( int i ) {
        return encodeMap[i&0x3F];
    }

    private static final char[] encodeMap = initEncodeMap();

    private static char[] initEncodeMap() {
        char[] map = new char[64];
        int i;
        for( i= 0; i<26; i++ )        map[i] = (char)('A'+i);
        for( i=26; i<52; i++ )        map[i] = (char)('a'+(i-26));
        for( i=52; i<62; i++ )        map[i] = (char)('0'+(i-52));
        map[62] = '+';
        map[63] = '/';

        return map;
    }

    /**
     * Trim all adjacent occurrences of <tt>character</tt> from the right side (end) of <tt>text</tt>
     *
     * @param text the test to trim
     * @param character the character to remove
     *
     * @return a {@link String} which is guaranteed not to have <tt>character</tt>
     * as its final character
     */
    public static String trimRight(String text, int character) {
        if (text == null)
            return null;

        int trimOffset = -1;
        for (int i = text.length() - 1; i >= 0; i--) {
            if (text.codePointAt(i) == character)
                trimOffset = i;
            else
                i = -1;
        }

        if (trimOffset < 0)
            return text;
        else if (trimOffset == 0)
            return "";
        else
            return text.substring(0, trimOffset);
    }

    /**
     * Calculate HMAC-SHA256 hash for a given content and a signing key.
     *
     * @param content The content to be signed
     * @param key The key used for signing
     *
     * @return HMAC-SHA256 BASE64 Encoded ASCII String
     *
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     */
    public static String getHmac(String content, String key) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        Mac sha256_HMAC = Mac.getInstance(ALGORITHM_HMAC_SHA256);

        byte[] key_hex;

        try {
            key_hex = Hex.decodeHex(key.toCharArray());
        } catch (DecoderException e) {
            e.printStackTrace();
            key_hex = new byte[0];
        }

        SecretKeySpec keySpec = new SecretKeySpec(key_hex, ALGORITHM_HMAC_SHA256);

        sha256_HMAC.init(keySpec);
        byte[] hmac = sha256_HMAC.doFinal(content.getBytes(UTF_8));

        String hmacHex = new String(Hex.encodeHex(hmac));

        return hmacHex;
    }
}
