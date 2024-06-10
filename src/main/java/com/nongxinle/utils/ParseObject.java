/**
 * com.nongxinle.utils class
 *
 * @Author: peiyi li
 * @Date: 2019-10-19 12:56
 */

package com.nongxinle.utils;


import java.io.UnsupportedEncodingException;
import java.util.Random;

/**
 *@author lpy
 *@date 2019-10-19 12:56
 */


public class ParseObject {

    public static String parseObj (String obj) {
        String replace = obj.replace("=", "");
        try {
            return java.net.URLDecoder.decode(replace, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

   return  null;
    }

    public static String myRandom () {
        Random random = new Random();
        String randomLetters = "";
        for(int i = 0; i < 4; i++){
            int randomIndex = random.nextInt(26) + 'A';
            char randomLetter  = (char) randomIndex;
            randomLetters += randomLetter;
        }
        return randomLetters;

    }



}
