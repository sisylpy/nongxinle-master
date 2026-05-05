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
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;

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

    // 服务器路径：/opt/tomcat/latest/app-data/images/
    // 本地开发路径（根据实际项目路径修改）
    // 本地测试路径（测试时修改为）：
    
    // 服务器图片存储根目录
    private static final String SERVER_IMAGE_DIR = "/opt/tomcat/latest/app-data/images/";
    // 本地开发图片存储根目录
    private static final String LOCAL_IMAGE_DIR = "/Users/lpy/Documents/javaWeb/kuangjia/nongxinle-master/stockImagesMac/";
    
    // 根据环境自动选择路径：生产环境使用 SERVER_IMAGE_DIR，开发环境使用 LOCAL_IMAGE_DIR
    // 检测方式：生产环境路径以 /opt 开头
    private static String getImageBaseDir() {
        // 可以通过系统属性或配置文件来控制，这里简单判断是否为 Mac 本地路径
        String javaHome = System.getProperty("user.home");
        // Mac 本地开发环境判断
        if (javaHome != null && javaHome.contains("/Users/")) {
            return LOCAL_IMAGE_DIR;
        }
        return SERVER_IMAGE_DIR;
    }

    /**
     * Nginx 常用 www-data 以 other 身份读静态文件；Tomcat umask 可能导致新建文件为 640。
     * 上传成功后设为 644（非 POSIX 文件系统上静默跳过）。
     */
    private static void ensureWorldReadableFile(File f) {
        if (f == null || !f.exists() || !f.isFile()) {
            return;
        }
        try {
            Files.setPosixFilePermissions(f.toPath(), PosixFilePermissions.fromString("rw-r--r--"));
        } catch (UnsupportedOperationException ignored) {
        } catch (Exception e) {
            System.err.println("[UploadFile] ensureWorldReadableFile: " + f.getAbsolutePath() + " - " + e.getMessage());
        }
    }


    public static String uploadFileName(HttpSession session, String newFileName, MultipartFile file, String saveFileName){
        System.out.println("[UploadFile.uploadFileName] 开始上传文件，文件夹: " + newFileName + ", 文件名: " + saveFileName);
        // 使用动态路径
        String baseDir = getImageBaseDir();
        String realPath = baseDir + newFileName;
        System.out.println("[UploadFile.uploadFileName] 完整路径: " + realPath);

        //1，保存文件
//        ServletContext servletContext = session.getServletContext();
//        String realPath = servletContext.getRealPath(newFileName);


        File uploadDir = new File(realPath);
        if(!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            System.out.println("[UploadFile.uploadFileName] 创建文件夹: " + realPath + ", 结果: " + created);
            if (!created) {
                // 检查父目录是否存在
                File parentDir = uploadDir.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    System.err.println("[UploadFile.uploadFileName] 父目录不存在: " + parentDir.getAbsolutePath());
                }
                // 检查权限
                if (parentDir != null && parentDir.exists() && !parentDir.canWrite()) {
                    System.err.println("[UploadFile.uploadFileName] 父目录无写权限: " + parentDir.getAbsolutePath());
                }
                throw new RuntimeException("无法创建目录: " + realPath + "，请检查权限");
            }
        } else {
            System.out.println("[UploadFile.uploadFileName] 文件夹已存在: " + realPath);
        }
        
        // 验证目录是否可写
        if (!uploadDir.canWrite()) {
            System.err.println("[UploadFile.uploadFileName] 目录无写权限: " + realPath);
            System.err.println("[UploadFile.uploadFileName] 目录权限检查: exists=" + uploadDir.exists() + ", canWrite=" + uploadDir.canWrite() + ", canRead=" + uploadDir.canRead());
            throw new RuntimeException("目录无写权限: " + realPath);
        }
        
        System.out.println("[UploadFile.uploadFileName] 目录权限验证通过: " + realPath);
        
        String filename = file.getOriginalFilename();
        // 使用 File 构造函数确保路径正确
        File destination = new File(uploadDir, saveFileName + ".jpg");
        System.out.println("[UploadFile.uploadFileName] 目标文件路径: " + destination.getAbsolutePath());
        System.out.println("[UploadFile.uploadFileName] 原始文件名: " + filename);
        System.out.println("[UploadFile.uploadFileName] 文件大小: " + file.getSize() + " bytes");
        
        // 确保父目录存在且可写（destination.getParentFile() 应该就是 uploadDir）
        File parentFile = destination.getParentFile();
        if (parentFile != null) {
            System.out.println("[UploadFile.uploadFileName] 父目录路径: " + parentFile.getAbsolutePath());
            System.out.println("[UploadFile.uploadFileName] 父目录是否存在: " + parentFile.exists());
            System.out.println("[UploadFile.uploadFileName] 父目录是否可写: " + parentFile.canWrite());
            
            if (!parentFile.exists()) {
                boolean parentCreated = parentFile.mkdirs();
                System.out.println("[UploadFile.uploadFileName] 创建父目录: " + parentFile.getAbsolutePath() + ", 结果: " + parentCreated);
                if (!parentCreated) {
                    throw new RuntimeException("无法创建父目录: " + parentFile.getAbsolutePath());
                }
            }
            // 验证父目录可写
            if (!parentFile.canWrite()) {
                System.err.println("[UploadFile.uploadFileName] 父目录无写权限: " + parentFile.getAbsolutePath());
                throw new RuntimeException("父目录无写权限: " + parentFile.getAbsolutePath());
            }
        }
        
        try {
            // 如果目标文件已存在，先删除
            if (destination.exists()) {
                boolean deleted = destination.delete();
                System.out.println("[UploadFile.uploadFileName] 删除已存在的文件: " + destination.getAbsolutePath() + ", 结果: " + deleted);
            }
            
            System.out.println("[UploadFile.uploadFileName] 准备写入文件: " + destination.getAbsolutePath());
            file.transferTo(destination);
            ensureWorldReadableFile(destination);
            System.out.println("[UploadFile.uploadFileName] 文件上传成功: " + destination.getAbsolutePath());
            // 验证文件是否真的存在
            if (destination.exists()) {
                System.out.println("[UploadFile.uploadFileName] 文件验证成功，文件大小: " + destination.length() + " bytes");
            } else {
                System.err.println("[UploadFile.uploadFileName] 警告：文件上传后不存在！");
            }
        } catch (IOException e) {
            System.err.println("[UploadFile.uploadFileName] 文件上传失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }

        return destination.getAbsolutePath();

    }

    public static String uploadClock(HttpSession session, String newFileName, MultipartFile file){

        //1，保存文件
//        ServletContext servletContext = session.getServletContext();
//        String realPath = servletContext.getRealPath(newFileName);
        // 使用动态路径
        String baseDir = getImageBaseDir();
        String realPath = baseDir + newFileName;


        File uploadClock = new File(realPath);
        if(!uploadClock.exists()) {
            uploadClock.mkdirs();
        }
        String filename = file.getOriginalFilename();
        uploadClock = new File(uploadClock + "/" + filename);
        try {
            file.transferTo(uploadClock);
            ensureWorldReadableFile(uploadClock);
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
        // 自动选择开发或生产环境的图片存储路径
        String baseDir = getImageBaseDir();
        System.out.println("[UploadFile.upload] 使用图片存储目录: " + baseDir);
        
        // 拼接最终的目录
        String realPath = baseDir + subDir;
        System.out.println("[UploadFile.upload] 完整上传路径: " + realPath);

        File uploadDir = new File(realPath);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            System.out.println("[UploadFile.upload] 创建目录: " + realPath + ", 结果: " + created);
        }

        // 获取原始文件名
        String filename = file.getOriginalFilename();
        File destination = new File(uploadDir, filename);

        try {
            file.transferTo(destination);
            ensureWorldReadableFile(destination);
            System.out.println("[UploadFile.upload] 文件上传成功: " + destination.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }

        // 返回完整的绝对路径
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
