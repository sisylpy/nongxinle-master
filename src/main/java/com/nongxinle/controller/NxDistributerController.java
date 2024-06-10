package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 2020-02-10 19:43:11
 */

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.*;
import com.nongxinle.utils.aes.WXBizMsgCrypt;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;


@Controller
@RequestMapping("api/nxdistributer")
public class NxDistributerController {
	@Autowired
	private NxDistributerService nxDistributerService;
	@Autowired
	private NxDistributerUserService nxDistributerUserService;
	@Autowired
	private SysUserService sysUserService;

	@RequestMapping(value = "/getDisInfo/{id}")
	@ResponseBody
	public R getDisInfo(@PathVariable Integer id) {
		NxDistributerEntity nxDistributerEntity = nxDistributerService.queryNxDisInfo(id);
		return R.ok().put("data", nxDistributerEntity);
	}

	@RequestMapping(value = "/gbDepGetDistibuterInfo",method = RequestMethod.POST)
	@ResponseBody
	public R gbDepGetDistibuterInfo(Integer nxDisId, Integer gbDepId, Integer gbDisId) {
		Map<String, Object> map = new HashMap<>();
		map.put("nxDisId", nxDisId);
		map.put("gbDisId", gbDisId);
		map.put("gbDepId", gbDepId);
		System.out.println("mapapappapa" + map);

	    NxDistributerEntity nxDistributerEntity =  nxDistributerService.gbDepQueryDistributerInfo(map);
	    return R.ok().put("data", nxDistributerEntity);
	}

	@RequestMapping(value = "/updateDisInfoWithFile", produces = "text/html;charset=UTF-8")
	@ResponseBody
	public R updateDisInfoWithFile(@RequestParam("file") MultipartFile file,
								   @RequestParam("name") String name,
								   @RequestParam("address") String address,
								   @RequestParam("id") Integer id,
								   HttpSession session) {
		//1,上传图片
		String newUploadName = "uploadImage";
		String realPath = UploadFile.upload(session, newUploadName, file);
		String filename = file.getOriginalFilename();
		String filePath = newUploadName + "/" + filename;
		NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(id);
		nxDistributerEntity.setNxDistributerName(name);
		nxDistributerEntity.setNxDistributerImg(filePath);
		nxDistributerEntity.setNxDistributerAddress(address);
		nxDistributerService.update(nxDistributerEntity);
		return R.ok();

	}


	@RequestMapping(value = "/updateDisInfo", method = RequestMethod.POST)
	@ResponseBody
	public R updateDisInfo (@RequestBody NxDistributerEntity nxDis) {
		Integer nxDistributerId = nxDis.getNxDistributerId();
		NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(nxDistributerId);
		if(!nxDistributerEntity.getNxDistributerPhone().equals(nxDis.getNxDistributerPhone())){
			SysUserEntity sysUserEntity = sysUserService.queryNxUserByUserName(nxDistributerEntity.getNxDistributerPhone());
			sysUserEntity.setUsername(nxDis.getNxDistributerPhone());
			sysUserService.justUpdate(sysUserEntity);
		}
		nxDistributerService.update(nxDis);

	    return R.ok();
	}
	/**
	 * 批发商注册
	 * @param distributerEntity 批发商
	 * @return 批发商
	 */
	@RequestMapping(value = "/disAndUserSave", method = RequestMethod.POST)
	@ResponseBody
	public R disAndUserSave (@RequestBody NxDistributerEntity distributerEntity) {

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

		// 1, 先检查微信号是否以前注册过
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getTexiansongAppID() + "&secret=" +
				myAPPIDConfig.getTexiansongScreat() + "&js_code=" + distributerEntity.getNxDistributerUserEntity().getNxDiuCode() +
				"&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();
		System.out.println(openid + "openididiiddi");

		NxDistributerUserEntity distributerUser = nxDistributerUserService.queryUserByOpenId(openid);
		//2，如果注册过，则返回提示。
		if(distributerUser != null){
			return R.error(-1,"微信号已注册!");
		}else {

			// 3，如果没有注册过
			distributerEntity.getNxDistributerUserEntity().setNxDiuWxOpenId(openid);
			Integer userId =  nxDistributerService.saveNewNxDistributer(distributerEntity);
			if (userId != null) {
				distributerEntity.getNxDistributerUserEntity().setNxDiuWxOpenId(openid);
				Map<String, Object> stringObjectMap = nxDistributerUserService.queryNxDisAndUserInfo(userId);

				return R.ok().put("data", stringObjectMap);
			}else{
				return R.error(-1, "注册失败");
			}

		}
	}

	@RequestMapping(value = "/disAndUserSaveOld", method = RequestMethod.POST)
	@ResponseBody
	public R disAndUserSaveOld (@RequestBody NxDistributerEntity distributerEntity) {

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

		// 1, 先检查微信号是否以前注册过
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getLiansuocaigouguanliduanAppId() + "&secret=" +
				myAPPIDConfig.getLiansuocaigouguanliduanScreat() + "&js_code=" + distributerEntity.getNxDistributerUserEntity().getNxDiuCode() +
				"&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();
		System.out.println(openid + "openididiiddi");

		NxDistributerUserEntity distributerUser = nxDistributerUserService.queryUserByOpenId(openid);
		//2，如果注册过，则返回提示。
		if(distributerUser != null){
			return R.error(-1,"微信号已注册!");
		}else {

			// 3，如果没有注册过
			distributerEntity.getNxDistributerUserEntity().setNxDiuWxOpenId(openid);
			Integer userId =  nxDistributerService.saveNewNxDistributer(distributerEntity);
			if (userId != null) {
				distributerEntity.getNxDistributerUserEntity().setNxDiuWxOpenId(openid);
				Map<String, Object> stringObjectMap = nxDistributerUserService.queryNxDisAndUserInfo(userId);

				return R.ok().put("data", stringObjectMap);
			}else{
				return R.error(-1, "注册失败");
			}

		}
	}


	@RequestMapping(value = "/disAndUserSaveWork", method = RequestMethod.POST)
	@ResponseBody
	public R disAndUserSaveWork (@RequestBody NxDistributerEntity distributerEntity) {

		String suiteToken = getWxProperty(Constant.SUITE_TOKEN);
		String code = distributerEntity.getNxDistributerUserEntity().getNxDiuCode();
		String userCropUrl = "https://qyapi.weixin.qq.com/cgi-bin/service/miniprogram/jscode2session?suite_access_token="+suiteToken
				+"&js_code="+code+"&grant_type=authorization_code";
//
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(userCropUrl, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);
		System.out.println("jsonObjectjsonObjectwwwww" + jsonObject);
		String openUserId = jsonObject.getString("open_userid");
		System.out.println("openeidiid" + openUserId);
		distributerEntity.getNxDistributerUserEntity().setNxDiuWxOpenId(openUserId);
		NxDistributerUserEntity distributerUser = nxDistributerUserService.queryUserByOpenId(openUserId);
		//2，如果注册过，则返回提示。
		if(distributerUser != null){
			return R.error(-1,"微信号已注册!");
		}else {
			// 3.1保存批发商
			Integer userId =  nxDistributerService.saveNewNxDistributerWrok(distributerEntity, jsonObject);
			if (userId != null) {

				Map<String, Object> stringObjectMap = nxDistributerUserService.queryNxDisAndUserInfo(userId);

				return R.ok().put("data", stringObjectMap);
			}else{
				return R.error(-1, "注册失败");
			}

		}
	}


	private   String getWxProperty(String key) {
		Properties pps = new Properties();
		InputStream is = NxDistributerUserController.class.getClassLoader().getResourceAsStream("wx.properties");
		try {
			pps.load(is);
			String value = pps.getProperty(key);
			System.out.println(key + " = " + value);
			is.close();
			System.out.println("getWxProperty---------------" + key + "===" + pps.get(key));
			return value;

		} catch (IOException e) {
			e.printStackTrace();
			return "-1";
		}

	}


//	/////////////////////////////////////////



	@RequestMapping(value = "/downLoadFragment/{fragmentId}")
	public ResponseEntity downLoadFragment (@PathVariable Integer fragmentId, HttpSession session) throws Exception {

		//1,获取文件路径
		ServletContext servletContext = session.getServletContext();
		String realPath = servletContext.getRealPath("numberRecord/ling.mp3");

		System.out.println("kaknakreailpath");
		System.out.println(realPath);

		//2,把文件读取程序当中
		InputStream io = new FileInputStream(realPath);
		byte[] body2 = new byte[io.available()];
		io.read(body2);
		System.out.println("openitititii");
		System.out.println(io.read());
		System.out.println(body2.length);

		//3,创建相应头
		HttpHeaders httpHeaders = new HttpHeaders();
		System.out.println(httpHeaders);

		httpHeaders.add("Content-Disposition","attachment; filename=" + "abc1"+".mp3");
		httpHeaders.add("Content-Type","audio/mpeg");
		ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(body2, httpHeaders, HttpStatus.OK);
		System.out.println("---0000000=-========");
		System.out.println(responseEntity);
		return responseEntity;
	}


///////









	
	/**
	 * 列表
	 */
//	@ResponseBody
//	@RequestMapping("/list")
//	@RequiresPermissions("nxdistributer:list")
//	public R list(Integer page, Integer limit){
//		Map<String, Object> map = new HashMap<>();
//		map.put("offset", (page - 1) * limit);
//		map.put("limit", limit);
//
//		//查询列表数据
//		List<NxDistributerEntity> nxDistributerList = nxDistributerService.queryList(map);
//		int total = nxDistributerService.queryTotal(map);
//
//		PageUtils pageUtil = new PageUtils(nxDistributerList, total, limit, page);
//
//		return R.ok().put("page", pageUtil);
//	}
//
//
//	/**
//	 * 保存
//	 */
//	@ResponseBody
//	@RequestMapping("/save")
//	@RequiresPermissions("nxdistributer:save")
//	public R save(@RequestBody NxDistributerEntity nxDistributer){
//		nxDistributerService.save(nxDistributer);
//
//		return R.ok();
//	}
//
//	/**
//	 * 修改
//	 */
//	@ResponseBody
//	@RequestMapping("/update")
//	@RequiresPermissions("nxdistributer:update")
//	public R update(@RequestBody NxDistributerEntity nxDistributer){
//		nxDistributerService.update(nxDistributer);
//
//		return R.ok();
//	}
//
//	/**
//	 * 删除
//	 */
//	@ResponseBody
//	@RequestMapping("/delete")
//	@RequiresPermissions("nxdistributer:delete")
//	public R delete(@RequestBody Integer[] distributerIds){
//		nxDistributerService.deleteBatch(distributerIds);
//
//		return R.ok();
//	}
	
}
