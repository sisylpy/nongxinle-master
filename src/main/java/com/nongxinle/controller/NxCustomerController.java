package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 2020-02-10 19:43:11
 */

import java.text.SimpleDateFormat;
import java.util.*;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.NxCommunityEntity;
import com.nongxinle.entity.NxCustomerUserEntity;
import com.nongxinle.service.NxCommunityService;
import com.nongxinle.service.NxCustomerUserService;
import com.nongxinle.utils.*;
import net.sf.json.JSONArray;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCustomerEntity;
import com.nongxinle.service.NxCustomerService;
import org.springframework.web.multipart.MultipartFile;
import sun.tools.jconsole.JConsole;

import javax.servlet.http.HttpSession;


@RestController
@RequestMapping("api/nxcustomer")
public class NxCustomerController {
	@Autowired
	private NxCustomerService nxCustomerService;

	@Autowired
	private NxCommunityService nxCommunityService;

	@Autowired
	private NxCustomerUserService customerUserService;
//
//	private static String APP_ID = "wx87baf9dcf935518a";
//
//	private  static  String SECRET = "a7e380c56222dfbd5377aeea6bb1eba2";


	 @RequestMapping(value = "/getCommunityCustomers", method = RequestMethod.POST)
	  @ResponseBody
	  public R getCommunityCustomers (Integer page, Integer limit, Integer nxCommunityId ) {

		 Map<String, Object> map = new HashMap<>();
		 map.put("offset", (page - 1) * limit);
		 map.put("limit", limit);
		 map.put("nxCommunityId", nxCommunityId);

		 //查询列表数据
		 List<NxCustomerEntity> nxCustomerList = nxCustomerService.queryCommunityCustomers(map);
		 int total = nxCustomerService.queryCustomerOfCommunityTotal(map);

		 PageUtils pageUtil = new PageUtils(nxCustomerList, total, limit, page);

		 return R.ok().put("page", pageUtil);


	  }




	/**
	 * 列表
	 */
	@ResponseBody
	@RequestMapping("/list")
	@RequiresPermissions("nxcustomer:list")
	public R list(Integer page, Integer limit){
		Map<String, Object> map = new HashMap<>();
		map.put("offset", (page - 1) * limit);
		map.put("limit", limit);
		
		//查询列表数据
		List<NxCustomerEntity> nxCustomerList = nxCustomerService.queryList(map);
		int total = nxCustomerService.queryTotal(map);
		
		PageUtils pageUtil = new PageUtils(nxCustomerList, total, limit, page);
		
		return R.ok().put("page", pageUtil);
	}
	
	
	/**
	 * 信息
	 */
	@ResponseBody
	@RequestMapping("/info/{customerId}")
	@RequiresPermissions("nxcustomer:info")
	public R info(@PathVariable("customerId") Integer customerId){
		NxCustomerEntity nxCustomer = nxCustomerService.queryObject(customerId);
		
		return R.ok().put("nxCustomer", nxCustomer);
	}




	/**
	 * 保存
	 */
	@RequestMapping(value = "/saveCustomerNew",produces = "text/html;charset=UTF-8")
	@ResponseBody
	public R saveCustomerNew(@RequestParam("file") MultipartFile file,
						  @RequestParam("userCode") String userCode,
						  @RequestParam("commId") Integer commId,
						  @RequestParam("phoneCode") String phoneCode,
						  HttpSession session){
		System.out.println("dfdnennenenennenenen" + file +"a" + userCode + "b=" + commId + "c=" + phoneCode);

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String appId = myAPPIDConfig.getShixianLiliAppId();
		String secret  = myAPPIDConfig.getShixianLiliScreat();
		System.out.println("dkafkdjasdfkas" + userCode + "ddfkkkk" + commId);


		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appId + "&secret=" + secret + "&js_code=" + userCode + "&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);
		System.out.println("jsodnbdbbdbd"+ jsonObject);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();



		NxCustomerEntity nxCustomer  = new NxCustomerEntity();
		nxCustomer.setNxCustomerCommunityId(commId);
		nxCustomerService.save(nxCustomer);
		//添加新用户


		NxCustomerUserEntity userEntity = new NxCustomerUserEntity();
		System.out.println("nusenenene" + userEntity);
		//1,上传图片
		String newUploadName = "uploadImage";
		String realPath = UploadFile.upload(session, newUploadName, file);

		String filename = file.getOriginalFilename();
		String filePath = newUploadName + "/" + filename;
		userEntity.setNxCuWxAvatarUrl(filePath);
		userEntity.setNxCuCustomerId(nxCustomer.getNxCustomerId());
		userEntity.setNxCuWxOpenId(openid);
		System.out.println("ususuusususu" + userEntity);
		customerUserService.save(userEntity);


//

//		System.out.println("codeee" + code);
		String urlPhone = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appId, secret);
		String strPhone = WeChatUtil.httpRequest(urlPhone, "GET", null);
		System.out.println("str=====>>>>" + strPhone);
		// 转成Json对象 获取openid
		JSONObject jsonObjectPhone = JSONObject.parseObject(strPhone);
		System.out.println("jsonObject" + jsonObjectPhone);
		String accessToken = jsonObjectPhone.getString("access_token");
		//通过token和code来获取用户手机号
		String urlP = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=" + accessToken + "&code=" + phoneCode;
//		JSONObject jsonObjectP = new JSONObject();
		Map<String, Object> map = new HashMap<>();
		map.put("code", phoneCode);
		String body = HttpRequest.post(urlP).body(JSONUtil.toJsonStr(map), ContentType.JSON.getValue()).execute().body();

		JSONObject jsonObjectP = JSONObject.parseObject(body);

		String phoneI = jsonObjectP.getString("phone_info");
		JSONObject jsonObjectPInfo = JSONObject.parseObject(phoneI);
		String phone = jsonObjectPInfo.getString("phoneNumber");


		userEntity.setNxCuWxPhoneNumber(phone);

		customerUserService.update(userEntity);
		return R.ok();

	}

	/**
	 * 保存
	 */
	@RequestMapping(value = "/saveCustomer",produces = "text/html;charset=UTF-8")
	@ResponseBody
	public R saveCustomer(@RequestParam("file") MultipartFile file,
						  @RequestParam("code") String code,
						  @RequestParam("commId") Integer commId,
						  HttpSession session){

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String appId = myAPPIDConfig.getShixianLiliAppId();
		String secret  = myAPPIDConfig.getShixianLiliScreat();
		System.out.println("dkafkdjasdfkas" + code + "ddfkkkk" + commId);


		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appId + "&secret=" + secret + "&js_code=" + code + "&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);
		System.out.println("jsodnbdbbdbd"+ jsonObject);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();
		NxCustomerEntity nxCustomer  = new NxCustomerEntity();
		nxCustomer.setNxCustomerCommunityId(commId);
		nxCustomerService.save(nxCustomer);
		//添加新用户



		NxCustomerUserEntity userEntity = new NxCustomerUserEntity();
		System.out.println("nusenenene" + userEntity);
		//1,上传图片
		String newUploadName = "uploadImage";
		String realPath = UploadFile.upload(session, newUploadName, file);

		String filename = file.getOriginalFilename();
		String filePath = newUploadName + "/" + filename;
		userEntity.setNxCuWxAvatarUrl(filePath);
		userEntity.setNxCuCustomerId(nxCustomer.getNxCustomerId());
		userEntity.setNxCuWxOpenId(openid);
		System.out.println("ususuusususu" + userEntity);
		customerUserService.save(userEntity);
		return R.ok();

	}



	
	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping("/update")
//	@RequiresPermissions("nxcustomer:update")
	public R update(@RequestBody NxCustomerEntity nxCustomer){
		System.out.println("iamupate");
		nxCustomerService.update(nxCustomer);
		
		return R.ok().put("data", nxCustomer);
	}
	
	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete")
	@RequiresPermissions("nxcustomer:delete")
	public R delete(@RequestBody Integer[] customerIds){
		nxCustomerService.deleteBatch(customerIds);
		
		return R.ok();
	}
	
}
