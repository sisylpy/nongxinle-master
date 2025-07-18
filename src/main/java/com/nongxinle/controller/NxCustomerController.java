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
import com.nongxinle.entity.NxECommerceEntity;
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

import static com.nongxinle.utils.DateUtils.formatWhatDay;


@RestController
@RequestMapping("api/nxcustomer")
public class NxCustomerController {
	@Autowired
	private NxCustomerService nxCustomerService;

	@Autowired
	private NxCommunityService nxCommunityService;

	@Autowired
	private NxCustomerUserService customerUserService;

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
	@RequestMapping(value = "/saveNewCustomer",method = RequestMethod.POST)
	@ResponseBody
	public R saveNewCustomer( Integer commId, String phoneCode, String openId, String commName){

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String appId = myAPPIDConfig.getShixianLiliAppId();
		String secret  = myAPPIDConfig.getShixianLiliScreat();

		NxCustomerEntity nxCustomer  = new NxCustomerEntity();
		nxCustomer.setNxCustomerCommunityId(commId);
		nxCustomerService.save(nxCustomer);

		//添加新用户


		String urlPhone = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appId, secret);
		String strPhone = WeChatUtil.httpRequest(urlPhone, "GET", null);
		System.out.println("str=====>>>>" + strPhone);
		// 转成Json对象 获取openid
		JSONObject jsonObjectPhone = JSONObject.parseObject(strPhone);
		System.out.println("jsonObject" + jsonObjectPhone);
		String accessToken = jsonObjectPhone.getString("access_token");
		//通过token和code来获取用户手机号
		String urlP = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=" + accessToken + "&code=" + phoneCode;
		Map<String, Object> map = new HashMap<>();
		map.put("code", phoneCode);
		String body = HttpRequest.post(urlP).body(JSONUtil.toJsonStr(map), ContentType.JSON.getValue()).execute().body();

		JSONObject jsonObjectP = JSONObject.parseObject(body);

		String phoneI = jsonObjectP.getString("phone_info");
		JSONObject jsonObjectPInfo = JSONObject.parseObject(phoneI);
		String phone = jsonObjectPInfo.getString("phoneNumber");
		NxCustomerUserEntity userEntity = new NxCustomerUserEntity();
		userEntity.setNxCuCustomerId(nxCustomer.getNxCustomerId());
		userEntity.setNxCuWxOpenId(openId);
		userEntity.setNxCuCommunityId(nxCustomer.getNxCustomerCommunityId());
		userEntity.setNxCuWxPhoneNumber(phone);
		userEntity.setNxCuWxNickName(commName + phone.substring(7,11));
		userEntity.setNxCuWxAvatarUrl("userImage/myUrl.png");
		userEntity.setNxCuJoinDate(formatWhatDay(0));
		userEntity.setNxCuWxGender(0);
		userEntity.setNxCuOrderAmount("0");
		userEntity.setNxCuOrderTimes(0);
		customerUserService.save(userEntity);
		Map<String, Object> mapR = new HashMap<>();
		mapR.put("userInfo",userEntity);
		mapR.put("customerInfo", nxCustomer);
		return R.ok().put("data",mapR);

	}

    /**
     * 保存
     */
    @RequestMapping(value = "/saveNewCustomerMix",method = RequestMethod.POST)
    @ResponseBody
    public R saveNewCustomerMix( String phoneCode, String openId, Integer commerceId, Integer commId){

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String appId = myAPPIDConfig.getQingqingxiangAppId();
        String secret  = myAPPIDConfig.getQingqingxiangScreat();
        NxCustomerEntity nxCustomer  = new NxCustomerEntity();
        nxCustomer.setNxCustomerCommerceId(commerceId);
        nxCustomerService.save(nxCustomer);

        //添加新用户


        String urlPhone = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appId, secret);
        String strPhone = WeChatUtil.httpRequest(urlPhone, "GET", null);
        System.out.println("str=====>>>>" + strPhone);
        // 转成Json对象 获取openid
        JSONObject jsonObjectPhone = JSONObject.parseObject(strPhone);
        System.out.println("jsonObject" + jsonObjectPhone);
        String accessToken = jsonObjectPhone.getString("access_token");
        //通过token和code来获取用户手机号
        String urlP = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=" + accessToken + "&code=" + phoneCode;
        Map<String, Object> map = new HashMap<>();
        map.put("code", phoneCode);
        String body = HttpRequest.post(urlP).body(JSONUtil.toJsonStr(map), ContentType.JSON.getValue()).execute().body();

        JSONObject jsonObjectP = JSONObject.parseObject(body);

        String phoneI = jsonObjectP.getString("phone_info");
        JSONObject jsonObjectPInfo = JSONObject.parseObject(phoneI);
        String phone = jsonObjectPInfo.getString("phoneNumber");
        NxCustomerUserEntity userEntity = new NxCustomerUserEntity();
        userEntity.setNxCuCustomerId(nxCustomer.getNxCustomerId());
        userEntity.setNxCuWxOpenId(openId);
        userEntity.setNxCuWxPhoneNumber(phone);
		NxECommerceEntity nxECommerceEntity = nxCommunityService.queryCommunityByECommerceId(commerceId);
		String nxECommerceName = nxECommerceEntity.getNxECommerceName();
		userEntity.setNxCuWxNickName(nxECommerceName + phone.substring(7,11));
        userEntity.setNxCuWxAvatarUrl("userImage/myUrl.png");
        userEntity.setNxCuJoinDate(formatWhatDay(0));
        userEntity.setNxCuWxGender(0);
        userEntity.setNxCuOrderAmount("0");
        userEntity.setNxCuOrderTimes(0);
        userEntity.setNxCuCommerceId(commerceId);
        userEntity.setNxCuCommunityId(commId);
        customerUserService.save(userEntity);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("userInfo",userEntity);
        mapR.put("customerInfo", nxCustomer);
        return R.ok().put("data",mapR);

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
