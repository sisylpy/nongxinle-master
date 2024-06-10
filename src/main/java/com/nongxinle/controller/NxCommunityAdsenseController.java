package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 05-26 16:23
 */

import java.util.List;

import com.nongxinle.utils.UploadFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCommunityAdsenseEntity;
import com.nongxinle.service.NxCommunityAdsenseService;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;


@RestController
@RequestMapping("api/nxadsense")
public class NxCommunityAdsenseController {
	@Autowired
	private NxCommunityAdsenseService nxCommunityAdsenseService;




	@RequestMapping(value = "/getListByCommunityId/{communityId}")
	@ResponseBody
	public R getListByCommunityId(@PathVariable Integer communityId) {
		List<NxCommunityAdsenseEntity> adsenseEntities = nxCommunityAdsenseService.getListByCommunityId(communityId);


	    return R.ok().put("data", adsenseEntities);
	}

	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping(value = "/save", produces = "text/html;charset=UTF-8")
	public R save(@RequestParam("file") MultipartFile file,
				  @RequestParam("nxAdsenseCommunityId") Integer nxAdsenseCommunityId,
				  HttpSession session) {
		System.out.println("hahhah");
		System.out.println(file);


		//1,上传图片
		String newUploadName = "uploadImage";
		String realPath = UploadFile.upload(session, newUploadName, file);

		String filename = file.getOriginalFilename();
		String filePath = newUploadName + "/" + filename;

		System.out.println(filePath);
		System.out.println("filebpathth");

		NxCommunityAdsenseEntity adsenseEntity = new NxCommunityAdsenseEntity();
		adsenseEntity.setNxCaFilePath(filePath);
		adsenseEntity.setNxCaCommunityId(nxAdsenseCommunityId);

		nxCommunityAdsenseService.save(adsenseEntity);


		return R.ok();
	}

	
}
