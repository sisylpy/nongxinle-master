package com.nongxinle.controller;

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
import com.nongxinle.service.NxCommunityGoodsService;
import com.nongxinle.service.NxCustomerUserCardService;
import com.nongxinle.service.NxCustomerUserCouponService;
import com.nongxinle.utils.Constant;
import com.nongxinle.utils.UploadFile;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.NxCommunityCouponService;
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
			NxCommunityCouponEntity communityCouponEntity = nxCommunityCouponService.queryObject(id);
			Integer nxCpCgGoodsId = communityCouponEntity.getNxCpCgGoodsId();
			NxCommunityGoodsEntity nxCommunityGoodsEntity = nxCommunityGoodsService.queryObject(nxCpCgGoodsId);

			String oldPath = nxCommunityGoodsEntity.getNxCgNxGoodsFilePath();
			if (oldPath != null && !oldPath.trim().isEmpty()) {
				String oldAbsolutePath = Constant.EXTERNAL_IMAGE_DIR + oldPath;
				File file1 = new File(oldAbsolutePath);
				if (file1.exists()) {
					file1.delete();
				}
			}

			nxCommunityGoodsService.delete(nxCpCgGoodsId);
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

	@RequestMapping(value = "/comUpdateCoupon", method = RequestMethod.POST)
	@ResponseBody
	public R comUpdateCoupon(@RequestBody NxCommunityCouponEntity goodsCoupon) {

		NxCommunityGoodsEntity nxCommunityGoodsEntity = goodsCoupon.getNxCommunityGoodsEntity();
		goodsCoupon.setNxCommunityCouponName(nxCommunityGoodsEntity.getNxCgGoodsName());

//        String nxCpStopTimeZone = "2024-05-01-00-00-00";
		String startDate =  goodsCoupon.getNxCpStartDate();
		String stopDate =  goodsCoupon.getNxCpStopDate();
		String couponStartTime =  goodsCoupon.getNxCpStartTime();
		String couponStopTime =  goodsCoupon.getNxCpStopTime();
//
		String replaceStart = couponStartTime.replace(":", "-");
		String replaceStop = couponStopTime.replace(":", "-");
		String start = startDate + "-" + replaceStart;
		String  stop = stopDate + "-" + replaceStop;
		System.out.println("dadfaf" + start + "stop=====" + stop);

		String[] splitStart = start.split("-");
		int year = Integer.parseInt(splitStart[0]);
		int month = Integer.parseInt(splitStart[1]);
		int day = Integer.parseInt(splitStart[2]);
		int hour = Integer.parseInt(splitStart[3]);
		int minute = Integer.parseInt(splitStart[4]);
		int haomiao = Integer.parseInt(splitStart[5]);
		LocalDateTime beginTime = LocalDateTime.of(year, month, day, hour, minute, haomiao);

		String[] splitStop = stop.split("-");
		int yearS = Integer.parseInt(splitStop[0]);
		int monthS = Integer.parseInt(splitStop[1]);
		int dayS = Integer.parseInt(splitStop[2]);
		int hourS = Integer.parseInt(splitStop[3]);
		int minuteS = Integer.parseInt(splitStop[4]);
		int haomiaoS = Integer.parseInt(splitStop[5]);
		LocalDateTime stopTime = LocalDateTime.of(yearS, monthS, dayS, hourS, minuteS, haomiaoS);
		System.out.println("adafasd" + beginTime + "stttt" + stopTime);

		goodsCoupon.setNxCpStartTimeZone(beginTime);
		goodsCoupon.setNxCpStopTimeZone(stopTime);
		goodsCoupon.setNxCpPrice(nxCommunityGoodsEntity.getNxCgGoodsPrice());
		goodsCoupon.setNxCpOriginalPrice(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice());
		goodsCoupon.setNxCpQuantity(nxCommunityGoodsEntity.getNxCgGoodsHuaxianQuantity());
		nxCommunityCouponService.update(goodsCoupon);

		BigDecimal goodsPrice = new BigDecimal(nxCommunityGoodsEntity.getNxCgGoodsPrice());
		BigDecimal fractionalPart = goodsPrice.subtract(goodsPrice.setScale(0, RoundingMode.DOWN)).multiply(new BigDecimal(10)).setScale(0,BigDecimal.ROUND_HALF_UP);
		BigDecimal integerPart = goodsPrice.setScale(0, RoundingMode.DOWN);
		nxCommunityGoodsEntity.setNxCgGoodsPriceInteger(integerPart.toString());
		nxCommunityGoodsEntity.setNxCgGoodsPriceDecimal(fractionalPart.toString());

		if(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice() != null && nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice().length() > 0){
			BigDecimal huaxianPrice = new BigDecimal(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice());
			BigDecimal difDec = huaxianPrice.subtract(goodsPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
			nxCommunityGoodsEntity.setNxCgGoodsHuaxianPriceDifferent(difDec.toString());
			System.out.println("indiddiidDDD"+ nxCommunityGoodsEntity.getNxCgGoodsPriceDecimal());
			if(nxCommunityGoodsEntity.getNxCgGoodsType() == 2){
				nxCommunityGoodsEntity.setNxCgBuyingPrice(nxCommunityGoodsEntity.getNxCgGoodsPrice());
				nxCommunityGoodsEntity.setNxCgBuyingPriceExchange(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice());
			}
		}else{
			nxCommunityGoodsEntity.setNxCgGoodsHuaxianPriceDifferent(null);
			nxCommunityGoodsEntity.setNxCgGoodsHuaxianPrice(null);
			nxCommunityGoodsEntity.setNxCgGoodsHuaxianQuantity(null);

		}


//		if(nxCommunityGoodsEntity.getNxCgSellType() == 1){
//			String cgStartTime = nxCommunityGoodsEntity.getNxCgStartTime();
//			String startHour = cgStartTime.substring(0, 2);
//			String startMinute = cgStartTime.substring(3, 5);
//			BigDecimal hourMinuteStart = new BigDecimal(startHour).multiply(new BigDecimal(60));
//			BigDecimal decimalStart = hourMinuteStart.add(new BigDecimal(startMinute)).setScale(0, BigDecimal.ROUND_HALF_UP);
//			nxCommunityGoodsEntity.setNxCgStartTimeZone(decimalStart.toString());
//
//			String cgStopTime = nxCommunityGoodsEntity.getNxCgStopTime();
//			String stopHour = cgStopTime.substring(0, 2);
//			String stopMinute = cgStopTime.substring(3, 5);
//			BigDecimal hourMinuteStop = new BigDecimal(stopHour).multiply(new BigDecimal(60));
//			BigDecimal decimalStop = hourMinuteStop.add(new BigDecimal(stopMinute)).setScale(0, BigDecimal.ROUND_HALF_UP);
//			nxCommunityGoodsEntity.setNxCgStopTimeZone(decimalStop.toString());
//		}else{
//			BigDecimal multiply = new BigDecimal(24).multiply(new BigDecimal(60));
//			nxCommunityGoodsEntity.setNxCgStopTimeZone(multiply.toString());
//			nxCommunityGoodsEntity.setNxCgStartTimeZone("0");
//			nxCommunityGoodsEntity.setNxCgStartTime("00:00");
//			nxCommunityGoodsEntity.setNxCgStopTime("23:59");
//
//		}

		nxCommunityGoodsService.update(nxCommunityGoodsEntity);

		return R.ok().put("data", goodsCoupon);
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
