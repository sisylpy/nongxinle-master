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

    public static String uploadFileName(HttpSession session, String newFileName, MultipartFile file, String saveFileName){
        System.out.println("11111111");
        //1，保存文件
        ServletContext servletContext = session.getServletContext();
        String realPath = servletContext.getRealPath(newFileName);


        File upload = new File(realPath);
        if(!upload.exists()) {
            upload.mkdirs();
        }
        String filename = file.getOriginalFilename();
        upload = new File(upload + "/" + saveFileName + ".jpg");
        try {
            file.transferTo(upload);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return realPath;

    }

    public static String uploadClock(HttpSession session, String newFileName, MultipartFile file){

        //1，保存文件
        ServletContext servletContext = session.getServletContext();
        String realPath = servletContext.getRealPath(newFileName);


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
     * 保存文件
     * @param session
     * @param newFileName
     * @param file
     */

  public static String upload(HttpSession session, String newFileName, MultipartFile file){

      //1，保存文件
      ServletContext servletContext = session.getServletContext();
      String realPath = servletContext.getRealPath(newFileName);


      File upload = new File(realPath);
      if(!upload.exists()) {
          upload.mkdirs();
      }
      String filename = file.getOriginalFilename();
      upload = new File(upload + "/" + filename);
      try {
          file.transferTo(upload);
      } catch (IOException e) {
          e.printStackTrace();
      }

      return realPath;

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
