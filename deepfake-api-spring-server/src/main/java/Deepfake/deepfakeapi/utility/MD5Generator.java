package Deepfake.deepfakeapi.utility;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Generator {

    private String result;

    /*
        문자열을 MD5 체크섬으로 변환하여 서버에 저장되도록 함
     */
    public MD5Generator(String input) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(input.getBytes(StandardCharsets.UTF_8));
        byte[] md5Hash = md5.digest();
        StringBuilder hexMd5Hash = new StringBuilder();
        for(byte b : md5Hash){
            String hexString = String.format("%02x", b);
            hexMd5Hash.append(hexString);
        }
        result =  hexMd5Hash.toString();

    }

    public String toString(){
        return result;
    }
}
