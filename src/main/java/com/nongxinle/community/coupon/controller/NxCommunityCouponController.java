package com.nongxinle.community.coupon.controller;

/**
 * 
 *
 * @author lpy
 * @date 05-15 08:33
 */

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.*;
import com.nongxinle.community.catalog.service.NxCommunityGoodsService;
import com.nongxinle.community.customer.service.NxCustomerUserCardService;
import com.nongxinle.community.coupon.service.NxCustomerUserCouponService;
import com.nongxinle.community.coupon.service.NxCommunityCouponRuleSaveService;
import com.nongxinle.utils.Constant;
import com.nongxinle.utils.CouponRuleValidator;
import com.nongxinle.utils.UploadFile;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.community.coupon.service.NxCommunityCouponService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;


@RestController
@RequestMapping("api/nxcommunitycoupon")
public class NxCommunityCouponController {
	@Autowired
	private NxCommunityCouponService nxCommunityCouponService;
	@Autowired
	private NxCommunityGoodsService nxCommunityGoodsService;
	@Autowired
	private NxCustomerUserCouponService nxCustomerUserCouponService;
	@Autowired
	private NxCommunityCouponRuleSaveService nxCommunityCouponRuleSaveService;


	@RequestMapping(value = "/comGetCouponDetail/{id}")
	@ResponseBody
	public R comGetCouponDetail(@PathVariable Integer id) {
	    Map<String, Object> map = new HashMap<>();
	    map.put("id", id);
		NxCommunityCouponEntity communityCouponEntity = nxCommunityCouponService.queryCouponDetail(map);
		System.out.println("edeetktkkt" + communityCouponEntity);
		return R.ok().put("data", communityCouponEntity);
	}

//	@RequestMapping(value = "/comSaveOneCoupon", method = RequestMethod.POST)
//	@ResponseBody
//	public R comSaveOneCoupon (@RequestBody NxCommunityCouponEntity  coupon) {
//		System.out.println("safnfnff" + coupon);
//		coupon.setNxCpStatus(0);
//		nxCommunityCouponService.save(coupon);
//		return R.ok();
//	}


	@RequestMapping(value = "/delComCoupon", method = RequestMethod.POST)
	@ResponseBody
	public R delComCoupon (Integer id,HttpSession session) {

		Map<String, Object> map = new HashMap<>();
		map.put("coupId", id);
		List<NxCustomerUserCouponEntity> nxCustomerUserCouponEntities = nxCustomerUserCouponService.queryUserCouponListByParams(map);
		if(nxCustomerUserCouponEntities.size() > 0){
			return R.error(-1,"有用户已购买，不能删除");
		}else{
			nxCommunityCouponService.delte(id);
			return R.ok();
		}

	}

	@RequestMapping(value = "/comGetConponList", method = RequestMethod.POST)
	@ResponseBody
	public R comGetConponList(Integer commId, Integer status) {
		Map<String, Object> map = new HashMap<>();
		map.put("commId", commId);
//		map.put("status", 0);
	    List<NxCommunityCouponEntity> communityCouponEntities =  nxCommunityCouponService.queryCouponListByParams(map);
	    return R.ok().put("data", communityCouponEntities);
	}

	@RequestMapping(value = "/saveRuleCoupon", method = RequestMethod.POST)
	@ResponseBody
	public R saveRuleCoupon(@RequestBody NxCommunityCouponEntity coupon) {
		try {
			NxCommunityCouponEntity saved = nxCommunityCouponRuleSaveService.saveRuleCoupon(coupon);
			return R.ok().put("data", saved);
		} catch (IllegalArgumentException e) {
			return R.error(e.getMessage());
		}
	}

	@RequestMapping(value = "/comUpdateCoupon", method = RequestMethod.POST)
	@ResponseBody
	public R comUpdateCoupon(@RequestBody NxCommunityCouponEntity goodsCoupon) {
		try {
			if (goodsCoupon.getNxCommunityCouponId() == null) {
				return R.error("优惠券ID不能为空");
			}
			CouponRuleValidator.normalizeDefaults(goodsCoupon);
			CouponRuleValidator.validateRuleCoupon(goodsCoupon);
			CouponRuleValidator.applyFixedDateTimeZones(goodsCoupon);
			nxCommunityCouponService.update(goodsCoupon);
			return R.ok().put("data", goodsCoupon);
		} catch (IllegalArgumentException e) {
			return R.error(e.getMessage());
		}
	}


	@RequestMapping(value = "/updateCouponWithFile", method = RequestMethod.POST)
	@ResponseBody
	public R updateCouponWithFile(@RequestParam("file") MultipartFile file,
									   @RequestParam("id") Integer id,
									   HttpSession session) {
		//1,上传图片
		String newUploadName = "goodsImage";
		String realPath = UploadFile.upload(session, newUploadName, file);

		String filename = file.getOriginalFilename();
		String filePath = newUploadName + "/" + filename;

		System.out.println("nefifiififiififpapapa" + filePath);


		NxCommunityCouponEntity communityCouponEntity = nxCommunityCouponService.queryObject(id);
		String oldPath = communityCouponEntity.getNxCpFilePath() ;
		if (oldPath != null && !oldPath.trim().isEmpty()) {
			String oldAbsolutePath = Constant.EXTERNAL_IMAGE_DIR + oldPath;
			File file1 = new File(oldAbsolutePath);
			if (file1.exists()) {
				file1.delete();
			}
		}

		communityCouponEntity.setNxCpFilePath(filePath);

		nxCommunityCouponService.update(communityCouponEntity);

		return R.ok().put("data", communityCouponEntity);
	}


	
}
