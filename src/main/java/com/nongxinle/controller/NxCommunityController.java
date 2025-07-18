package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 2020-03-04 17:57:31
 */

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.NxCommunityCardEntity;
import com.nongxinle.entity.NxCommunityFatherGoodsEntity;
import com.nongxinle.entity.NxCommunityUserEntity;
import com.nongxinle.service.NxCommunityFatherGoodsService;
import com.nongxinle.service.NxCommunityUserService;
import com.nongxinle.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCommunityEntity;
import com.nongxinle.service.NxCommunityService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;


@RestController
@RequestMapping("api/nxcommunity")
public class NxCommunityController {
	@Autowired
	private NxCommunityService nxCommunityService;
	@Autowired
	private NxCommunityUserService nxCommunityUserService;
	@Autowired
	private NxCommunityFatherGoodsService nxCommunityFatherGoodsService;



	@RequestMapping(value = "/getCommunityByCityId/{id}")
	@ResponseBody
	public R getCommunityByCityId(@PathVariable Integer id) {
		Map<String, Object> map = new HashMap<>();
		map.put("cityId", id);
//	    List<>  nxCommunityService.querycommunityByParams(map);
	    return R.ok();
	}

	@RequestMapping(value = "/getCommunityInfo/{id}")
	@ResponseBody
	public R getCommunityInfo(@PathVariable Integer id) {
		System.out.println("commdinf" + id);
	    return R.ok().put("data",nxCommunityService.queryObject(id));
	}


	/**
	 * 保存
	 */
	@RequestMapping(value = "/comAndUserSave", method = RequestMethod.POST)
	@ResponseBody
	public R comAndUserSave(@RequestBody NxCommunityEntity nxCommunity){

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

		// 1, 先检查微信号是否以前注册过
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getCommunityAppID() + "&secret=" +
				myAPPIDConfig.getCommunityScreat() + "&js_code=" + nxCommunity.getNxCommunityUserEntity().getNxCouCode() +
				"&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();
		Map<String, Object> map = new HashMap<>();
		map.put("openId", openid);
		map.put("roleId", 0);
		NxCommunityUserEntity communityUserEntity = nxCommunityUserService.queryComUserByOpenId(map);
		//2，如果注册过，则返回提示。
		if(communityUserEntity != null){
			return R.error(-1,"微信号已注册!");
		}else {

			// 3，如果没有注册过
			// 3.1保存批发商

//			nxCommunity.set
			nxCommunityService.saveWithEcommerce(nxCommunity);

			// 3.2，保存批发商用户
			Integer communityId = nxCommunity.getNxCommunityId();
			System.out.println(nxCommunity + "ididiidiididid");

			NxCommunityUserEntity nxCommunityUserEntity = nxCommunity.getNxCommunityUserEntity();
			nxCommunityUserEntity.setNxCouCommunityId(communityId);
			nxCommunityUserEntity.setNxCouWxOpenId(openid);
			nxCommunityUserEntity.setNxCouDeviceId("-1");
			nxCommunityUserEntity.setNxCouUrlIsChange(0);
			nxCommunityUserService.save(nxCommunityUserEntity);


			NxCommunityFatherGoodsEntity fatherGoodsEntity = new NxCommunityFatherGoodsEntity();
			fatherGoodsEntity.setNxCfgOrderRank(0);
			fatherGoodsEntity.setNxCfgCommunityId(communityId);
			fatherGoodsEntity.setNxCfgFathersFatherId(0);
			fatherGoodsEntity.setNxCfgFatherGoodsLevel(0);
			fatherGoodsEntity.setNxCfgFatherGoodsName("商品");
			fatherGoodsEntity.setNxCfgFatherGoodsColor("#3cc36e");

			nxCommunityFatherGoodsService.save(fatherGoodsEntity);

			//3..3 返回用户id
			Integer nxCommunityUserId = nxCommunityUserEntity.getNxCommunityUserId();
			Map<String, Object> map1 = new HashMap<>();
			map1.put("userId", nxCommunityUserId);
			map1.put("roleId", 0 );
			NxCommunityUserEntity nxCommunityUserEntity1 = nxCommunityUserService.queryComUserInfo(map1);

			return R.ok().put("data", nxCommunityUserEntity1);
		}
	}




//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


	/**
	 * 暂时不用社区居民订货业务
	 * 微信二维码扫描校验文件内容
	 * @return 文件内容
	 */
	@RequestMapping(value = "/customerRegist/i7948FzJJ6.txt")
	@ResponseBody

	public String customerRegist( ) {
		return "bb7a0c73e61112c45ebd6ad3743bb05e"; }


	/**
	 * 二维码扫描打开固定页面
	 * @param communityId 社区id
	 * @return 社区列表
	 */
	@RequestMapping(value = "/customerRegist/{communityId}")
	@ResponseBody
	public R customerRegist(@PathVariable Integer communityId) {
		NxCommunityEntity nxCommunity = nxCommunityService.queryObject(communityId);

		return R.ok().put("data", nxCommunity);
	}


	/**
	 * 微信二维码扫描校验文件内容
	 * @return 文件内容
	 */
	@RequestMapping(value = "/i7948FzJJ6.txt")
	@ResponseBody
	public String newCustomerRegist( ) { return "bb7a0c73e61112c45ebd6ad3743bb05e"; }


	@RequestMapping(value = "/updateCommWxWithFile", method = RequestMethod.POST)
	@ResponseBody
	public R updateCommWxWithFile(@RequestParam("file") MultipartFile file,
								@RequestParam("id") Integer id,
								  @RequestParam("type") String type,
								HttpSession session) {
		//1,上传图片
		String newUploadName = "userImage";
		String realPath = UploadFile.upload(session, newUploadName, file);
		String filename = file.getOriginalFilename();
		String filePath = newUploadName + "/" + filename;

		System.out.println("updfiieelellelelele" + file);
		System.out.println("updfiieelellelelele" + type);

		NxCommunityEntity communityCardEntity = nxCommunityService.queryObject(id);
		if(type.equals("door")){

			String oldPath = communityCardEntity.getNxCommunityDoorFile();
			if (oldPath != null && !oldPath.trim().isEmpty()) {
				String oldAbsolutePath = Constant.EXTERNAL_IMAGE_DIR + oldPath;
				File file1 = new File(oldAbsolutePath);
				if (file1.exists()) {
					file1.delete();
				}
			}

			communityCardEntity.setNxCommunityDoorFile(filePath);
		}else{

			String oldPath = communityCardEntity.getNxCommunityWxFile();
			if (oldPath != null && !oldPath.trim().isEmpty()) {
				String oldAbsolutePath = Constant.EXTERNAL_IMAGE_DIR + oldPath;
				File file1 = new File(oldAbsolutePath);
				if (file1.exists()) {
					file1.delete();
				}
			}

			communityCardEntity.setNxCommunityWxFile(filePath);
		}

		nxCommunityService.update(communityCardEntity);

		return R.ok();
	}

	@RequestMapping(value = "/updateCommunity", method = RequestMethod.POST)
	@ResponseBody
	public R updateCommunity (@RequestBody NxCommunityEntity comm) {
	    nxCommunityService.update(comm);
	    return R.ok().put("data", comm);
	}



	
}
