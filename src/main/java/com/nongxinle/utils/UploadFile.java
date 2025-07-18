/**
 * com.nongxinle.utils class
 *
 * @Author: peiyi li
 * @Date: 2019-06-04 09:05
 */

package com.nongxinle.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *@author lpy
 *@date 2019-06-04 09:05
 */


public class UploadFile {

//

    /**
     * 保存文件
     * @param session
     * @param newFileName
     * @param file
     */

    private static final String EXTERNAL_IMAGE_DIR = "/opt/tomcat/latest/app-data/images/";


    public static String uploadFileName(HttpSession session, String newFileName, MultipartFile file, String saveFileName){
        System.out.println("11111111");
        String realPath = EXTERNAL_IMAGE_DIR + newFileName;

        //1，保存文件
//        ServletContext servletContext = session.getServletContext();
//        String realPath = servletContext.getRealPath(newFileName);


        File uploadDir = new File(realPath);
        if(!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        String filename = file.getOriginalFilename();
        File destination  = new File(uploadDir + "/" + saveFileName + ".jpg");
        System.out.println("updalfiififfneneen" + destination);
        try {
            file.transferTo(destination);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return destination.getAbsolutePath();

    }

    public static String uploadClock(HttpSession session, String newFileName, MultipartFile file){

        //1，保存文件
//        ServletContext servletContext = session.getServletContext();
//        String realPath = servletContext.getRealPath(newFileName);
        String realPath = EXTERNAL_IMAGE_DIR + newFileName;


        File uploadClock = new File(realPath);
        if(!uploadClock.exists()) {
            uploadClock.mkdirs();
        }
        String filename = file.getOriginalFilename();
        uploadClock = new File(uploadClock + "/" + filename);
        try {
            file.transferTo(uploadClock);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return realPath;

    }

    /**
     * 上传文件
     * @param session HttpSession（如果不用也可去掉）
     * @param subDir  子目录（如 "goodsImage"）
     * @param file    上传文件
     * @return        文件最终保存的绝对路径
     */
    public static String upload(HttpSession session, String subDir, MultipartFile file) {
        // 拼接最终的目录：/opt/tomcat/latest/app-data/images/goodsImage
        String realPath = EXTERNAL_IMAGE_DIR + subDir;

        File uploadDir = new File(realPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 获取原始文件名
        String filename = file.getOriginalFilename();
        File destination = new File(uploadDir, filename);

        try {
            file.transferTo(destination);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 返回完整的绝对路径，如 /opt/tomcat/latest/app-data/images/goodsImage/xxx.jpg
        return destination.getAbsolutePath();
    }

  public  static ResponseEntity downLoadFile(HttpSession session) throws Exception {
      //1,获取文件路径
      ServletContext servletContext = session.getServletContext();
      String realPathImage = servletContext.getRealPath("/static/images/mo2.png");

      //2,把文件读取程序当中
      InputStream io = new FileInputStream(realPathImage);
      byte[] body = new byte[io.available()];
      io.read(body);

      //3,创建相应头
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add("Content-Disposition","attachment; filename=" + "image.png");
      ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
      return responseEntity;
  }







}
