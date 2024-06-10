/**
 * com.nongxinle.utils class
 *
 * @Author: peiyi li
 * @Date: 2020-05-23 09:34
 */

package com.nongxinle.utils;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

/**
 *@author lpy
 *@date 2020-05-23 09:34
 */


public class CommonUtils {


    public static String generateUUID (){
        return UUID.randomUUID().toString().replace("-", "")
                 .substring(0, 32);
    }

    public static String generateBillTradeNo (String areaCode){
        Random random = new Random();
        int rannum= (int)(random.nextDouble()*(99999-10000 + 1))+ 10000;
        String s1 = new StringBuilder(areaCode).append(rannum).toString();
        return s1;
    }

    public static String generateOutTradeNo (){

        String s = new SimpleDateFormat("yyyyMMdd").format(new Date()).toString();

        String substring = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 24);
        StringBuilder sb = new StringBuilder(s);
        String s1 = new StringBuilder(substring).append(sb).toString();
        return s1;
    }


    public static String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest
                    .getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
                        .substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }

}
