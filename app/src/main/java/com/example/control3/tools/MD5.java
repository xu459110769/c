package com.example.control3.tools;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
    public static String md5(String plainText) {
        byte[] secretBytes = null;
        String md5code=null;
        for(int j=0;j<10;j++)
        {
            try {
                secretBytes = MessageDigest.getInstance("md5").digest(
                        plainText.getBytes());
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("没有md5这个算法！");
            }
            plainText = new BigInteger(1, secretBytes).toString(16);// 16进制数字
            // 如果生成数字未满32位，需要前面补0
            for (int i = 0; i < 32 - plainText.length(); i++) {
                plainText = "0" + plainText;
            }
        }

        return plainText;
    }
}
