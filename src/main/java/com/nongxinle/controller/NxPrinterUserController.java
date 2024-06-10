package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 05-25 15:21
 */


import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.NxDistributerUserEntity;
import com.nongxinle.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxPrinterUserEntity;
import com.nongxinle.service.NxPrinterUserService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;

import java.util.Map;

import static com.nongxinle.utils.DateUtils.formatWhatDay;


@RestController
@RequestMapping("api/nxprinteruser")
public class NxPrinterUserController {
	@Autowired
	private NxPrinterUserService nxPrinterUserService;



	@RequestMapping(value = "/getPrinterUserInfo/{userId}")
	@ResponseBody
	public R getPrinterUserInfo(@PathVariable Integer userId) {
		NxPrinterUserEntity printerUserEntity = nxPrinterUserService.queryObject(userId);
		return R.ok().put("data", printerUserEntity);
	}

	@RequestMapping(value = "/updatePrinterUserWithFile", produces = "text/html;charset=UTF-8")
	@ResponseBody
	public R updatePrinterUserWithFile(@RequestParam("file") MultipartFile file,
										 @RequestParam("userName") String userName,
									   @RequestParam("userId") Integer userId,
										 HttpSession session) {
		//1,上传图片
		String newUploadName = "uploadImage";
		String realPath = UploadFile.upload(session, newUploadName, file);


		String filename = file.getOriginalFilename();
		String filePath = newUploadName + "/" + filename;
		NxPrinterUserEntity printerUserEntity = nxPrinterUserService.queryObject(userId);
		printerUserEntity.setNxPrinterWxNickName(userName);
		printerUserEntity.setNxPrinterWxAvartraUrl(filePath);
		nxPrinterUserService.update(printerUserEntity);

		return R.ok();

	}

	@RequestMapping(value = "/printerUserRegisterWithFile", produces = "text/html;charset=UTF-8")
	@ResponseBody
	public R printerUserRegisterWithFile(@RequestParam("file") MultipartFile file,
											   @RequestParam("userName") String userName,
											   @RequestParam("code") String code,
											   @RequestParam("admin") Integer admin,
											   @RequestParam("nxDisId") Integer nxDisId,
											   @RequestParam("nxDisPurUserId") Integer nxDisPurUserId,
											   @RequestParam("commId") Integer commId,
											   @RequestParam("commPurUserId") Integer commPurUserId,
											   @RequestParam("gbDisId") Integer gbDisId,
											   @RequestParam("gbDepId") Integer gbDepId,
											   @RequestParam("gbDepUserId") Integer gbDepUserId,
											   HttpSession session) {
		System.out.println("aa" + nxDisId);
		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getLiziDriverAppID() +
				"&secret=" + myAPPIDConfig.getLiziDriverScreat() + "&js_code=" + code + "&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);

		// 转成Json对象 获取openidPrinterUserRegister
		JSONObject jsonObject = JSONObject.parseObject(str);
		System.out.println(jsonObject);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();
		//判断是否suppler


		//添加新用户
		NxPrinterUserEntity printerUserEntity = new NxPrinterUserEntity();
		printerUserEntity.setNxPrinterWxOpenId(openid);
		printerUserEntity.setNxPrinterJoinDate(formatWhatDay(0));
		//1,上传图片
		String newUploadName = "uploadImage";
		String realPath = UploadFile.upload(session, newUploadName, file);

		String filename = file.getOriginalFilename();
		String filePath = newUploadName + "/" + filename;
		printerUserEntity.setNxPrinterWxNickName(userName);
		printerUserEntity.setNxPrinterWxAvartraUrl(filePath);
		printerUserEntity.setNxPrinterUrlChange(1);
		printerUserEntity.setNxPrinterNxDistributerId(nxDisId);
		printerUserEntity.setNxPrinterNxPurchaserUserId(nxDisPurUserId);
		printerUserEntity.setNxPrinterNxCommunityId(commId);
		printerUserEntity.setNxPrinterGbDistributerId(gbDisId);
		printerUserEntity.setNxPrinterGbDepartmentId(gbDepId);
		printerUserEntity.setNxPrinterGbDepartmentUserId(gbDepUserId);
		printerUserEntity.setNxPrinterAdmin(admin);
		printerUserEntity.setNxPrinterDeviceId("-1");
		printerUserEntity.setNxPrinterDeviceBillId("-1");
		nxPrinterUserService.save(printerUserEntity);

		return R.ok();

	}

	@RequestMapping(value = "/printerUserLogin",method = RequestMethod.POST)
	@ResponseBody
	public R printerUserLogin(String code, Integer disId) {

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String maimaiAppID = myAPPIDConfig.getLiziDriverAppID();
		String maimaiScreat = myAPPIDConfig.getLiziDriverScreat();
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + maimaiAppID + "&secret=" +
				maimaiScreat + "&js_code=" + code +
				"&grant_type=authorization_code";
		String str = WeChatUtil.httpRequest(url, "GET", null);
		JSONObject jsonObject = JSONObject.parseObject(str);
		String openId = jsonObject.get("openid").toString();
		if (openId != null) {
			NxPrinterUserEntity nxBuyUserEntity1 = nxPrinterUserService.queryPrinterUserByOpenId(openId);

			if(nxBuyUserEntity1 != null){
				return R.ok().put("data", nxBuyUserEntity1);
			}else {
				return R.error(-1, "请进行注册");
			}
		} else {
			return R.error(-1, "请进行注册");
		}
	}


	@RequestMapping(value = "/updateNxPrinterUserDeviceId", method = RequestMethod.POST)
	@ResponseBody
	public R updateNxPrinterUserDeviceId(@RequestBody NxPrinterUserEntity userEntity) {
		nxPrinterUserService.update(userEntity);
		return R.ok().put("data", userEntity);
	}
	
	
}
