package com.nongxinle.controller;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@RestController
public class DownloadController {




    @RequestMapping(value = "/wx/downLoadImg/{value}")
    public ResponseEntity downLoadImg (@PathVariable String value, HttpSession session) throws Exception {

        System.out.println("nihao");

        //1,获取文件路径
        ServletContext servletContext = session.getServletContext();
        String realPath = servletContext.getRealPath("uploadImage/r.jpg");


        //2,把文件读取程序当中
        InputStream io = new FileInputStream(realPath);
        byte[] body = new byte[io.available()];
        io.read(body);
      
        //3,创建相应头
        HttpHeaders httpHeaders = new HttpHeaders();
        System.out.println(httpHeaders);

        httpHeaders.add("Content-Disposition","attachment; filename=" +  value +".jpg");
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);

        return responseEntity;
    }



}
