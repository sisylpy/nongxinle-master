/**
 * com.nongxinle.utils class
 *
 * @Author: peiyi li
 * @Date: 2019-05-14 18:35
 */

package com.nongxinle.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *@author lpy
 *@date 2019-05-14 18:35
 */


public class FFMpegUtil {

    /**
     * /usr/bin/ffmpeg
     * 借鉴网站： https://blog.csdn.net/ajklaclk/article/details/80753302
     * 百度提供：ffmpeg -y  -i /Users/lpy/Documents/javaWeb/Study/hanyu/target/hanyu/upload/blob  -acodec pcm_s16le -f s16le -ac 1 -ar 16000 /Users/lpy/Documents/javaWeb/Study/hanyu/target/hanyu/upload/blob.pcm
     */

//    private static String ffmgegEXE = "/usr/bin/ffmpeg";

    private static String ffmgegEXE = "/Users/lpy/Documents/maven/ffmpeg";


    public static void convetor(String audioInputPath, String audioOutPath) throws IOException {


        List<String> command = new ArrayList<>();
        command.add(ffmgegEXE);
        command.add("-y");
        command.add("-i");
        command.add(audioInputPath);
        command.add("-acodec");
        command.add("pcm_s16le");
        command.add("-f");
        command.add("s16le");
        command.add("-ac");
        command.add("1");
        command.add("-ar");
        command.add("16000");
        command.add(audioOutPath);

        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = null;

        try {
            process = builder.start();

        } catch (IOException e) {
            e.printStackTrace();
        }


        /**
         * 使用这种方式会在瞬间大量消耗CPU和内存等系统资源，所以这里我们需要对流进行处理
         */
        InputStream errorStream = process.getErrorStream();
        InputStreamReader inputStreamReader = new InputStreamReader(errorStream);
        BufferedReader br = new BufferedReader(inputStreamReader);

        String line = "";
        while ((line = br.readLine()) != null) {

        }
        if (br != null) {
            br.close();
        }
        if (inputStreamReader != null) {
            inputStreamReader.close();
        }
        if (errorStream != null) {
            errorStream.close();
        }


    }
}
