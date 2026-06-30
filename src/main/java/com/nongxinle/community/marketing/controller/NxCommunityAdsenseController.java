package com.nongxinle.community.marketing.controller;

/**
 * 
 *
 * @author lpy
 * @date 05-26 16:23
 */

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxCommunityCardEntity;
import com.nongxinle.entity.NxCommunityGoodsEntity;
import com.nongxinle.community.catalog.service.NxCommunityGoodsService;
import com.nongxinle.utils.Constant;
import com.nongxinle.utils.UploadFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCommunityAdsenseEntity;
import com.nongxinle.community.marketing.service.NxCommunityAdsenseService;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;


@RestController
@RequestMapping("api/nxadsense")
public class NxCommunityAdsenseController {
	@Autowired
	private NxCommunityAdsenseService nxCommunityAdsenseService;
	@Autowired
	private NxCommunityGoodsService nxCommunityGoodsService;



	@RequestMapping(value = "/closeGoodsAdsense", method = RequestMethod.POST)
	@ResponseBody
	public R closeGoodsAdsense (Integer goodsId ) {
		Map<String, Object> map = new HashMap<>();
		map.put("goodsId", goodsId);
		NxCommunityAdsenseEntity communityAdsenseEntity = nxCommunityAdsenseService.queryGoodsAdsenseByParams(map);
		if(communityAdsenseEntity != null){
			nxCommunityAdsenseService.delete(communityAdsenseEntity.getNxCommunityAdsenseId());
			if(communityAdsenseEntity.getNxCaCgGoodsId() != null){
				NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(communityAdsenseEntity.getNxCaCgGoodsId());
				goodsEntity.setNxCgAdsenseStartTime("00:00");
				goodsEntity.setNxCgAdsenseStopTime("00:00");
				goodsEntity.setNxCgAdsenseStartTimeZone("0");
				goodsEntity.setNxCgAdsenseStopTimeZone("0");
				goodsEntity.setNxCgAdsenseStockQuantity(0);
				goodsEntity.setNxCgAdsenseRestQuantity(0);
				goodsEntity.setNxCgIsOpenAdsense(0);
				nxCommunityGoodsService.update(goodsEntity);
			}
		}


		return R.ok();
	}


	@RequestMapping(value = "/comSaveGoodsAdsense", method = RequestMethod.POST)
	@ResponseBody
	public R comSaveGoodsAdsense(@RequestBody NxCommunityGoodsEntity goodsEntity) {
		Integer nxCgGoodsType = goodsEntity.getNxCgGoodsType();
		Integer nxCgCardId = goodsEntity.getNxCgCardId();

		String path = "";
		String url = "?nxCommunityGoodsId=" + goodsEntity.getNxCommunityGoodsId()
				+ "&from=index&orderType=0&spId=-1&pindanId=-1"
				+ "&serviceType=" + (goodsEntity.getNxCgServiceType() != null ? goodsEntity.getNxCgServiceType() : 0);
		if(nxCgGoodsType == 0){
			if(nxCgCardId != null){
				path = "zeroGoodsCardPage/zeroGoodsCardPage";
			}else{
				path = "zeroGoodsPage/zeroGoodsPage";
			}
		}else if(nxCgGoodsType == 1){
			if(nxCgCardId != null){
				path = "oneGoodsCardPage/oneGoodsCardPage";
			}else{
				path = "oneGoodsPage/oneGoodsPage";
			}
		}else if(nxCgGoodsType == 2){
			if(nxCgCardId != null){
				path = "twoGoodsCardPage/twoGoodsCardPage";
			}else{
				path = "twoGoodsPage/twoGoodsPage";
			}
		}
		else if(nxCgGoodsType == 3){
			if(nxCgCardId != null){
				path = "threeGoodsCardPage/threeGoodsCardPage";
			}else{
				path = "threeGoodsPage/threeGoodsPage";
			}
		}
		String cgStartTime = goodsEntity.getNxCgAdsenseStartTime();
		String startHour = cgStartTime.substring(0, 2);
		String startMinute = cgStartTime.substring(3, 5);
		BigDecimal hourMinuteStart = new BigDecimal(startHour).multiply(new BigDecimal(60));
		BigDecimal decimalStart = hourMinuteStart.add(new BigDecimal(startMinute)).setScale(0, BigDecimal.ROUND_HALF_UP);
		goodsEntity.setNxCgAdsenseStartTimeZone(decimalStart.toString());

		String cgStopTime = goodsEntity.getNxCgAdsenseStopTime();
		String stopHour = cgStopTime.substring(0, 2);
		String stopMinute = cgStopTime.substring(3, 5);
		BigDecimal hourMinuteStop = new BigDecimal(stopHour).multiply(new BigDecimal(60));
		BigDecimal decimalStop = hourMinuteStop.add(new BigDecimal(stopMinute)).setScale(0, BigDecimal.ROUND_HALF_UP);
		goodsEntity.setNxCgAdsenseStopTimeZone(decimalStop.toString());

		NxCommunityAdsenseEntity adsenseEntity = new NxCommunityAdsenseEntity();
		adsenseEntity.setNxCaStatus(0);
		adsenseEntity.setNxCaCommunityId(goodsEntity.getNxCgCommunityId());
		adsenseEntity.setNxCommunityAdsenseName(goodsEntity.getNxCgGoodsName());
		adsenseEntity.setNxCaClickTo(path + url);
		adsenseEntity.setNxCaCgGoodsId(goodsEntity.getNxCommunityGoodsId());
		adsenseEntity.setNxCaFilePath(goodsEntity.getNxCgNxGoodsTopFilePath());
		adsenseEntity.setNxCaStartTimeZone(decimalStart.toString());
		adsenseEntity.setNxCaStopTimeZone(decimalStop.toString());
		adsenseEntity.setNxCaStartTime(goodsEntity.getNxCgAdsenseStartTime());
		adsenseEntity.setNxCaStopTime(goodsEntity.getNxCgAdsenseStopTime());

		nxCommunityAdsenseService.save(adsenseEntity);

		nxCommunityGoodsService.update(goodsEntity);

		return R.ok();
	}



	@RequestMapping(value = "/comGetAdsenseDetail/{id}")
	@ResponseBody
	public R comGetAdsenseDetail(@PathVariable Integer id) {
		System.out.println("abcmcmcmc");
		NxCommunityAdsenseEntity nxCommunityAdsenseEntity = nxCommunityAdsenseService.queryObject(id);

		return R.ok().put("data", nxCommunityAdsenseEntity);
	}


	@RequestMapping(value = "/getListByCommunityId/{communityId}")
	@ResponseBody
	public R getListByCommunityId(@PathVariable Integer communityId) {
		Map<String, Object> map = new HashMap<>();
		map.put("commId", communityId);
		List<NxCommunityAdsenseEntity> adsenseEntities = nxCommunityAdsenseService.queryAdsenseByParams(map);
	    return R.ok().put("data", adsenseEntities);
	}


	@RequestMapping(value = "/comSaveOneAdsense", method = RequestMethod.POST)
	@ResponseBody
	public R comSaveOneAdsense(@RequestBody NxCommunityAdsenseEntity card) {
		System.out.println("safnfnff" + card);
		nxCommunityAdsenseService.save(card);
		return R.ok();
	}



	@RequestMapping(value = "/delComAdsense", method = RequestMethod.POST)
	@ResponseBody
	public R delComAdsense(Integer id, HttpSession session) {

		NxCommunityAdsenseEntity communityCardEntity = nxCommunityAdsenseService.queryObject(id);

		Integer nxCaCgGoodsId = communityCardEntity.getNxCaCgGoodsId();
		NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(nxCaCgGoodsId);
		goodsEntity.setNxCgAdsenseStartTime("00:00");
		goodsEntity.setNxCgAdsenseStopTime("00:00");
		goodsEntity.setNxCgAdsenseStartTimeZone("0");
		goodsEntity.setNxCgAdsenseStopTimeZone("0");
		goodsEntity.setNxCgAdsenseStockQuantity(0);
		goodsEntity.setNxCgAdsenseRestQuantity(0);
		goodsEntity.setNxCgIsOpenAdsense(0);
		nxCommunityGoodsService.update(goodsEntity);

		String oldPath = communityCardEntity.getNxCaFilePath();

		if (oldPath != null && !oldPath.trim().isEmpty()) {
			String oldAbsolutePath = Constant.EXTERNAL_IMAGE_DIR + oldPath;
			File file1 = new File(oldAbsolutePath);
			if (file1.exists()) {
				file1.delete();
			}
		}
		nxCommunityAdsenseService.delete(id);
		return R.ok();
	}



	@RequestMapping(value = "/comUpdateAdsense", method = RequestMethod.POST)
	@ResponseBody
	public R comUpdateAdsense(@RequestBody NxCommunityAdsenseEntity goodsCard) {

		nxCommunityAdsenseService.update(goodsCard);

		return R.ok().put("data", goodsCard);
	}


	@RequestMapping(value = "/updateAdsenseWithFile", method = RequestMethod.POST)
	@ResponseBody
	public R updateAdsenseWithFile(@RequestParam("file") MultipartFile file,
								@RequestParam("id") Integer id,
								HttpSession session) {
		//1,上传图片
		String newUploadName = "goodsImage";
		String realPath = UploadFile.upload(session, newUploadName, file);

		String filename = file.getOriginalFilename();
		String filePath = newUploadName + "/" + filename;

		System.out.println("nefifiififiififpapapa" + filePath);


		NxCommunityAdsenseEntity communityCardEntity = nxCommunityAdsenseService.queryObject(id);
		String oldPath = communityCardEntity.getNxCaFilePath();
		if (oldPath != null && !oldPath.trim().isEmpty()) {
			String oldAbsolutePath = Constant.EXTERNAL_IMAGE_DIR + oldPath;
			File file1 = new File(oldAbsolutePath);
			if (file1.exists()) {
				file1.delete();
			}
		}

		communityCardEntity.setNxCaFilePath(filePath);

		nxCommunityAdsenseService.update(communityCardEntity);

		return R.ok();
	}

	
}
