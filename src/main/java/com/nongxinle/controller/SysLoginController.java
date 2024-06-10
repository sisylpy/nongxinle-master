package com.nongxinle.controller;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.nongxinle.entity.SysUserEntity;
import com.nongxinle.service.SysUserService;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.R;
import com.nongxinle.utils.ShiroUtils;
import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;

/**
 * 登录相关
 * 
 */
@Controller
@CrossOrigin
public class SysLoginController {
	@Autowired
	private Producer producer;
	@Autowired
	private SysUserService sysUserService;



	@RequestMapping("captcha.jpg")
	public void captcha(HttpServletResponse response, HttpSession session)throws ServletException, IOException {

        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");

        //生成文字验证码
        String text = producer.createText();
        //生成图片验证码
        BufferedImage image = producer.createImage(text);
        //保存到shiro session
        ShiroUtils.setSessionAttribute(Constants.KAPTCHA_SESSION_KEY, text);
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "jpg", out);
	}


	
	/**
	 * 登录
	 */
	@ResponseBody
    @CrossOrigin
	@RequestMapping(value = "/sys/login", method = RequestMethod.POST)
	public R login(String username, String password, String captcha, HttpSession session)throws IOException {
        System.out.println(captcha);
		System.out.println(username);
        System.out.println(password);
		String kaptcha = ShiroUtils.getKaptcha(Constants.KAPTCHA_SESSION_KEY);
		if(!captcha.equalsIgnoreCase(kaptcha)){
            System.out.println("验证码不正确");
			return R.error("验证码不正确");
		}
		
		try{
			Subject subject = ShiroUtils.getSubject();
			//sha256加密
			password = new Sha256Hash(password).toHex();
			UsernamePasswordToken token = new UsernamePasswordToken(username, password);
			subject.login(token);
			System.out.println("subssslogingginggnigingii!!!!!!!!!!!!");
		}catch (UnknownAccountException e) {
			return R.error(e.getMessage());
		}catch (IncorrectCredentialsException e) {
			return R.error(e.getMessage());
		}catch (LockedAccountException e) {
			return R.error(e.getMessage());
		}catch (AuthenticationException e) {
			return R.error("账户验证失败");
		}
		if(!username.equals("admin")){
			System.out.println("heereisssnotmamdmdmdmd1111" + username);
			Map<String, Object> map = new HashMap<>();
			map.put("username", username);
			SysUserEntity sysUserEntity = sysUserService.queryGbUserByUserName(map);
			return R.ok().put("data", sysUserEntity);
		}
		return R.ok().put("session",session);
	}




	@ResponseBody
	@CrossOrigin
	@RequestMapping(value = "/sys/loginNx", method = RequestMethod.POST)
	public R loginNx(String username, String password, String captcha, HttpSession session)throws IOException {
		System.out.println(captcha);
		System.out.println(username);
		System.out.println(password);
		System.out.println("logignginigngingingnxxxnxnxnxnxnxnxnxnnxnnxnxn");
		String kaptcha = ShiroUtils.getKaptcha(Constants.KAPTCHA_SESSION_KEY);
		if(!captcha.equalsIgnoreCase(kaptcha)){
			System.out.println("验证码不正确");
			return R.error("验证码不正确");
		}

		try{
			Subject subject = ShiroUtils.getSubject();
			//sha256加密
			password = new Sha256Hash(password).toHex();
			UsernamePasswordToken token = new UsernamePasswordToken(username, password);
			subject.login(token);
		}catch (UnknownAccountException e) {
			return R.error(e.getMessage());
		}catch (IncorrectCredentialsException e) {
			return R.error(e.getMessage());
		}catch (LockedAccountException e) {
			return R.error(e.getMessage());
		}catch (AuthenticationException e) {
			return R.error("账户验证失败");
		}
		if(!username.equals("admin")){
			SysUserEntity sysUserEntity = sysUserService.queryNxUserByUserName(username);
			Map<String, Object> map = new HashMap<>();
			map.put("session",session );
			map.put("user",sysUserEntity );
			System.out.println("sessisons=======" + session);
			return R.ok().put("data", map);
		}

		return R.ok().put("session",session);
	}


	/**
	 * 退出
	 */
	@ResponseBody
	@CrossOrigin
	@RequestMapping("/sys/out")
	public R logout() {
		System.out.println("logouttt");
		ShiroUtils.logout();
		return R.ok();
	}


	 
	
	
	
	
}
