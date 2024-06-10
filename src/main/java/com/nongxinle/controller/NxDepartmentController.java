package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 06-16 11:26
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
//import com.nongxinle.service.NxDepartmentOrdersService;
import com.nongxinle.service.*;
import com.nongxinle.utils.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.formatFullTime;
import static com.nongxinle.utils.PinYin4jUtils.*;


@RestController
@RequestMapping("api/nxdepartment")
public class NxDepartmentController {
	@Autowired
	private NxDepartmentService nxDepartmentService;
	@Autowired
	private NxDepartmentUserService nxDepartmentUserService;
	@Autowired
	private NxDepartmentDisGoodsService nxDepartmentDisGoodsService;
	@Autowired
	private NxDepartmentBillService nxDepartmentBillService;
	@Autowired
	private NxDistributerCommunityService nxDisCommunityService;
	@Autowired
	private NxDistributerService nxDistributerService;




	@ResponseBody
	@RequestMapping(value = "/addDepPciture", produces = "text/html;charset=UTF-8")
	public R addDepPciture(@RequestParam("file") MultipartFile file,
						@RequestParam("disId") Integer disId,
						   @RequestParam("name") String name,
						HttpSession session) {

		//1,上传图片
		String newUploadName = "uploadImage";
		NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
		String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), false, null);

		String lastFileName = getXiegang(headPinyin) + formatFullTime();
		String realPath = UploadFile.uploadFileName(session, newUploadName, file, lastFileName);
		String filename = file.getOriginalFilename();
		String filePath = newUploadName + "/" + lastFileName + ".jpg";

		NxDepartmentEntity nxDepartmentEntity = new NxDepartmentEntity();
		nxDepartmentEntity.setNxDepartmentFilePath(filePath);
		nxDepartmentEntity.setNxDepartmentName(name);
		nxDepartmentEntity.setNxDepartmentAttrName(name);
		nxDepartmentEntity.setNxDepartmentDisId(disId);
		nxDepartmentEntity.setNxDepartmentFatherId(0);
		nxDepartmentEntity.setNxDepartmentSubAmount(0);
		nxDepartmentEntity.setNxDepartmentIsGroupDep(1);
		nxDepartmentEntity.setNxDepartmentPrintName("ApplyFiftyPanel");
		nxDepartmentEntity.setNxDepartmentSettleType(99);
		nxDepartmentEntity.setNxDepartmentWorkingStatus(0);
		nxDepartmentService.saveJustDepartment(nxDepartmentEntity);

		return R.ok();
	}


	/**
	 * 获取批发商客户列表
	 * @param disId 批发商id
	 * @return 客户列表
	 */
	@RequestMapping(value = "/disGetAllCustomer/{disId}")
	@ResponseBody
	public R disGetAllDisDepartments(@PathVariable Integer disId) {

		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("type", 0);
		System.out.println("selelelelllemmama" + map);
		List<NxDepartmentEntity> entities = nxDepartmentService.queryDepartmentBySettleType(map);
		map.put("type", 1);
		List<NxDepartmentEntity> entities2 = nxDepartmentService.queryDepartmentBySettleType(map);
		List<NxCommunityEntity> nxCommunityEntities = nxDisCommunityService.queryAllNxCommunity(disId);

		Map<String, Object> mapData = new HashMap<>();
		mapData.put("settleTypeOne", entities);
		mapData.put("settleTypeTwo", entities2);
		mapData.put("settleTypeThree", nxCommunityEntities);
		return R.ok().put("data", mapData);
	}

	/**
	 * 批发商添加客户
	 * @param distributerDepartmentEntity 客户
	 * @return 0
	 */
	@RequestMapping(value = "/saveOneCustomer", method = RequestMethod.POST)
	@ResponseBody
	public R saveOneCustomer (@RequestBody NxDistributerDepartmentEntity distributerDepartmentEntity)  {

		NxDepartmentEntity nxDepartmentEntity = distributerDepartmentEntity.getNxDepartmentEntity();

		//1,保存部门
		nxDepartmentService.saveJustDepartment(nxDepartmentEntity);

//		//2，保存批发商部门
		Integer nxDepartmentId = nxDepartmentEntity.getNxDepartmentId();

		//3，如果有子部门，则保存子部门nxDepartmentEntityList
		List<NxDepartmentEntity> nxDepartmentEntities = nxDepartmentEntity.getNxSubDepartments();

		if(nxDepartmentEntity.getNxSubDepartments().size() > 0){
			for (NxDepartmentEntity sub : nxDepartmentEntities) {
				sub.setNxDepartmentFatherId(nxDepartmentId);
				sub.setNxDepartmentSettleType(nxDepartmentEntity.getNxDepartmentSettleType());
				sub.setNxDepartmentDisId(nxDepartmentEntity.getNxDepartmentDisId());
				sub.setNxDepartmentType(nxDepartmentEntity.getNxDepartmentType());
				sub.setNxDepartmentAttrName(sub.getNxDepartmentName());
				sub.setNxDepartmentWorkingStatus(0);
				nxDepartmentService.saveJustDepartment(sub);
			}
		}
		return R.ok();
	}


	@RequestMapping(value = "/deleteGroupDep", method = RequestMethod.POST)
	@ResponseBody
	public R deleteGroupDep (@RequestBody NxDepartmentEntity   dep) {
		Integer departmentId = dep.getNxDepartmentId();


		//depUser
		List<NxDepartmentUserEntity> userEntities = nxDepartmentUserService.queryAllUsersByDepId(departmentId);
		if(userEntities.size() > 0){
			for (NxDepartmentUserEntity user : userEntities) {
				nxDepartmentUserService.delete(user.getNxDepartmentUserId());
			}
		}

		//depGoods
		Map<String, Object> map = new HashMap<>();
		map.put("depId", departmentId);
		List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(map);
		if(departmentDisGoodsEntities.size() > 0){
			for (NxDepartmentDisGoodsEntity disGoods : departmentDisGoodsEntities) {
				nxDepartmentDisGoodsService.delete(disGoods.getNxDepartmentDisGoodsId());
			}
		}
		//depBill
		Map<String, Object> map1 = new HashMap<>();
		map1.put("depFatherId",departmentId );
		List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(map1);
		if(billEntityList.size() > 0){
			for (NxDepartmentBillEntity bill : billEntityList) {
				nxDepartmentBillService.delete(bill.getNxDepartmentBillId());
			}
		}

		nxDepartmentService.delete(departmentId);

		return R.ok();
	}



	@RequestMapping(value = "/deletePictureDep/{id}")
	@ResponseBody
	public R deletePictureDep(@PathVariable Integer id) {
	    nxDepartmentService.delete(id);
	    return R.ok();
	}



	/**
	 * PURCHASE
	 * 修改群名称
	 * @param departmentEntity 群
	 * @return ok
	 */
	@RequestMapping(value = "/updateGroupName", method = RequestMethod.POST)
	@ResponseBody
	public R updateGroupName (@RequestBody  NxDepartmentEntity departmentEntity ) {
		nxDepartmentService.update(departmentEntity);
	    return R.ok();
	}

	/**
	 * 微信小程序扫描二维码校验文件
	 * @return 校验内容
	 */
	@RequestMapping(value = "/cash/rO1D9uJkqM.txt")
	@ResponseBody
	public String nxDepRegistCash( ) {
		return "33ccef96a8d5e4ac24783c37abf8ac13";
	}


	@RequestMapping(value = "/GyhokS8ddA.txt")
	@ResponseBody
	public String nxDepRegist( ) {
		return "9dac34a5cde884212f36c70694dec25f";
	}


	/**
	 * PURCHASE
	 * 采购员注册
	 * @param dep 订货群restrauntRegist
	 * @return 群信息
	 */
	@RequestMapping(value = "/cashRegister", method = RequestMethod.POST)
	@ResponseBody
	public R cashRegister (@RequestBody NxDepartmentEntity dep) {

	//wxApp
		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String purchaseAppID = myAPPIDConfig.getShixianLiliAppId();
		String purchaseScreat = myAPPIDConfig.getShixianLiliScreat();

		NxDepartmentUserEntity nxDepartmentUserEntity = dep.getNxDepartmentUserEntity();
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + purchaseAppID + "&secret=" + purchaseScreat + "&js_code=" + nxDepartmentUserEntity.getNxDuCode()+ "&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();
		NxDepartmentUserEntity nxDepartmentUserEntity1 = nxDepartmentUserService.queryDepUserByOpenId(openid);
		if(nxDepartmentUserEntity1 == null){
			dep.getNxDepartmentUserEntity().setNxDuWxOpenId(openid);
			Integer depUserId = nxDepartmentService.saveNewDepartment(dep);
			if (depUserId != null){
				Map<String, Object> stringObjectMap = nxDepartmentService.queryDepAndUserInfo(depUserId);
				return R.ok().put("data", stringObjectMap);
			}
			return R.error(-1,"注册失败");
		}else {
			return R.error(-1,"此微信号已注册过采购员");
		}



	}

	/**
	 * PURCHASE
	 * 采购员注册
	 * @param dep 订货群restrauntRegist
	 * @return 群信息
	 */
	@RequestMapping(value = "/departmentRegister", method = RequestMethod.POST)
	@ResponseBody
	public R departmentRegister (@RequestBody NxDepartmentEntity dep) {

//wxApp
		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String purchaseAppID = myAPPIDConfig.getPurchaseAppID();
		String purchaseScreat = myAPPIDConfig.getPurchaseScreat();

		NxDepartmentUserEntity nxDepartmentUserEntity = dep.getNxDepartmentUserEntity();
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + purchaseAppID + "&secret=" + purchaseScreat + "&js_code=" + nxDepartmentUserEntity.getNxDuCode()+ "&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();
		NxDepartmentUserEntity nxDepartmentUserEntity1 = nxDepartmentUserService.queryDepUserByOpenId(openid);
		if(nxDepartmentUserEntity1 == null){
			dep.getNxDepartmentUserEntity().setNxDuWxOpenId(openid);
			dep.setNxDepartmentWorkingStatus(0);
			dep.setNxDepartmentOweBoxNumber(0);
			dep.setNxDepartmentDeliveryBoxNumber(0);
			dep.setNxDepartmentUnPayTotal("0");
			dep.setNxDepartmentAddCount(0);
			dep.setNxDepartmentPayTotal("0");
			dep.setNxDepartmentProfitTotal("0");
			dep.setNxDepartmentType("unFixed");
			dep.setNxDepartmentPrintName("ApplyPanel");
			Integer depUserId = nxDepartmentService.saveNewDepartment(dep);
			if (depUserId != null){
				Map<String, Object> stringObjectMap = nxDepartmentService.queryDepAndUserInfo(depUserId);
				return R.ok().put("data", stringObjectMap);
			}
			return R.error(-1,"注册失败");
		}else {
			return R.error(-1,"此微信号已注册过采购员");
		}



	}


	/**
	 * PURCHASE
	 * 采购员注册
	 * @param dep 订货群
	 * @return 群信息
	 */
	@RequestMapping(value = "/chainDepartmentRegister", method = RequestMethod.POST)
	@ResponseBody
	public R chainDepartmentRegister(@RequestBody NxDepartmentEntity dep) {
		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String purchaseAppID = myAPPIDConfig.getPurchaseAppID();
		String purchaseScreat = myAPPIDConfig.getPurchaseScreat();

		NxDepartmentUserEntity nxDepartmentUserEntity = dep.getNxDepartmentUserEntity();
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + purchaseAppID + "&secret=" + purchaseScreat + "&js_code=" + nxDepartmentUserEntity.getNxDuCode()+ "&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();
		NxDepartmentUserEntity nxDepartmentUserEntity1 = nxDepartmentUserService.queryDepUserByOpenId(openid);
		if(nxDepartmentUserEntity1 == null){
			dep.getNxDepartmentUserEntity().setNxDuWxOpenId(openid);
			Integer depUserId = nxDepartmentService.saveNewChainDepartment(dep);
			if (depUserId != null){
				Map<String, Object> stringObjectMap = nxDepartmentService.queryDepAndUserInfo(depUserId);
				return R.ok().put("data", stringObjectMap);
			}
			return R.error(-1,"注册失败");
		}else {
			return R.error(-1,"此微信号已注册过采购员");
		}

	}


	/**
	 * ORDER
	 * 获取群的子部门
	 * @param depId 群id
	 * @return 子部门列表
	 */
	@RequestMapping(value = "/getSubDepartments/{depId}")
	@ResponseBody
	public R getSubDepartments(@PathVariable Integer depId) {
		System.out.println(depId);
		List<NxDepartmentEntity> departmentEntities =   nxDepartmentService.querySubDepartments(depId);
		return R.ok().put("data", departmentEntities);
	}



	/**
	 * ORDER
	 * @param depId
	 * @return
	 */
	@RequestMapping(value = "/getDepInfo/{depId}")
	@ResponseBody
	public R getDepInfo(@PathVariable Integer depId) {
		System.out.println(depId + "idiid");
		NxDepartmentEntity nxDepartmentEntity = nxDepartmentService.queryDepInfo(depId);
		return R.ok().put("data", nxDepartmentEntity);
	}

	@RequestMapping(value = "/getGroupInfo/{depId}")
	@ResponseBody
	public R getGroupInfo(@PathVariable Integer depId) {
		NxDepartmentEntity nxDepartmentEntity = nxDepartmentService.queryGroupInfo(depId);
		return R.ok().put("data", nxDepartmentEntity);
	}




//	//////////////////


//
//	@RequestMapping(value = "/getFatherDep/{depId}")
//	@ResponseBody
//	public R getFatherDep(@PathVariable Integer depId) {
//		List<NxDepartmentEntity> departmentEntities = nxDepartmentService.queryFatherDep(depId);
//	    return R.ok().put("data", departmentEntities.get(0));
//	}


//
//	/**
//	 * 保存
//	 */
//	@ResponseBody
//	@RequestMapping("/saveSubDepartment")
//	public R saveSubDepartment(@RequestBody NxDepartmentEntity nxDepartment){
//		List<NxDepartmentEntity> nxDepartmentEntities = nxDepartment.getNxDepartmentEntities();
//		for (NxDepartmentEntity dep : nxDepartmentEntities) {
//		nxDepartmentService.saveSubDepartment(dep);
//		}
//		Integer nxDepartmentId = nxDepartment.getNxDepartmentId();
//		NxDepartmentEntity nxDepartmentEntity = nxDepartmentService.queryObject(nxDepartmentId);
//		Integer nxDepartmentSubAmount = nxDepartmentEntity.getNxDepartmentSubAmount();
//		nxDepartmentEntity.setNxDepartmentSubAmount(nxDepartmentSubAmount + nxDepartment.getNxDepartmentEntities().size());
//		nxDepartmentService.update(nxDepartmentEntity);
//		return R.ok();
//	}


//
//	 @RequestMapping(value = "/getDisDepartments", method = RequestMethod.POST)
//	  @ResponseBody
//	  public R getDisDepartments (Integer disId, String type) {
//
//		 Map<String, Object> map = new HashMap<>();
//		 map.put("disId", disId);
//		 map.put("type", type);
//		 List<NxDepartmentEntity> list =  nxDepartmentService.queryDisDepartments(map);
//		 return R.ok().put("data", list);
//	  }


	/**
	 * 保存
	 */
//	@ResponseBody
//	@RequestMapping("/save")
//	public R save(@RequestBody NxDepartmentEntity nxDepartment){
//		nxDepartmentService.save(nxDepartment);
//		return R.ok();
//	}


	

}
