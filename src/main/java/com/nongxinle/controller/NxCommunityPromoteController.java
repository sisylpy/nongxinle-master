package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 05-25 08:54
 */

import java.util.ArrayList;
import java.util.List;

import com.nongxinle.entity.NxCommunityGoodsEntity;
import com.nongxinle.service.NxCommunityGoodsService;
import com.nongxinle.utils.UploadFile;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCommunityPromoteEntity;
import com.nongxinle.service.NxCommunityPromoteService;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;


@RestController
@RequestMapping("api/nxpromote")
public class NxCommunityPromoteController {
	@Autowired
	private NxCommunityPromoteService nxCommunityPromoteService;

	@Autowired
	private NxCommunityGoodsService nxCommunityGoodsService;


	@RequestMapping(value = "/getListByCommunityId/{communityId}")
	@ResponseBody
	public R getListByCommunityId(@PathVariable Integer communityId) {
		System.out.println("lailema?" );
		List<NxCommunityPromoteEntity> promoteEntities = nxCommunityPromoteService.getListByCommunityId(communityId);

	    return R.ok().put("data", promoteEntities);
	}



	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping(value = "/save", produces = "text/html;charset=UTF-8")
//    @RequiresPermissions("nxgoods:save")
	public R save(@RequestParam("file") MultipartFile file,
				  @RequestParam("nxPromoteCgId") Integer nxPromoteCgId,
				  @RequestParam("nxOrignalPrice") String nxOrignalPrice,
				  @RequestParam("nxPromotePrice") String nxPromotePrice,
				  @RequestParam("nxPromoteStandard") String nxPromoteStandard,
				  @RequestParam("nxPromoteWeight") String nxPromoteWeight,
				  @RequestParam("nxPromoteExpired") String nxPromoteExpired,
				  @RequestParam("nxPromoteStorage") String nxPromoteStorage,
				  @RequestParam("nxPromoteWords") String nxPromoteWords,
				  @RequestParam("nxPromoteCommunityId") Integer nxPromoteCommunityId,
				  @RequestParam("nxPromoteRecommandGoods") String nxPromoteRecommandGoods,
				  @RequestParam("nxPromoteCgFatherId") Integer nxPromoteCgFatherId,
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

		NxCommunityPromoteEntity nxCommunityPromoteEntity = new NxCommunityPromoteEntity();
		nxCommunityPromoteEntity.setNxPromoteCgId(nxPromoteCgId);
		nxCommunityPromoteEntity.setNxPromoteFilePath(filePath);
		nxCommunityPromoteEntity.setNxOrignalPrice(nxOrignalPrice);
		nxCommunityPromoteEntity.setNxPromotePrice(nxPromotePrice);
		nxCommunityPromoteEntity.setNxPromoteStandard(nxPromoteStandard);
		nxCommunityPromoteEntity.setNxPromoteWeight(nxPromoteWeight);
		nxCommunityPromoteEntity.setNxPromoteExpired(nxPromoteExpired);
		nxCommunityPromoteEntity.setNxPromoteStorage(nxPromoteStorage);
		nxCommunityPromoteEntity.setNxPromoteWords(nxPromoteWords);
		nxCommunityPromoteEntity.setNxPromoteCommunityId(nxPromoteCommunityId);
		nxCommunityPromoteEntity.setNxPromoteRecommandGoods(nxPromoteRecommandGoods);
		nxCommunityPromoteEntity.setNxPromoteCgFatherId(nxPromoteCgFatherId);

		nxCommunityPromoteService.save(nxCommunityPromoteEntity);





		return R.ok();
	}



	
	/**
	 * 信息
	 */
	@ResponseBody
	@RequestMapping("/info/{nxPromoteId}")
//	@RequiresPermissions("nxpromote:info")
	public R info(@PathVariable("nxPromoteId") Integer nxPromoteId){
		NxCommunityPromoteEntity nxPromote = nxCommunityPromoteService.queryObject(nxPromoteId);
		String nxPromoteRecommandGoods = nxPromote.getNxPromoteRecommandGoods();
		String[] split = nxPromoteRecommandGoods.split(",");
		List<NxCommunityGoodsEntity> goodslist = new ArrayList<>();
		for (String s : split) {
			if(s != null ){
				Integer integer = Integer.valueOf(s);
				System.out.println(integer + "integer!!!!");
				NxCommunityGoodsEntity communityGoodsEntity = nxCommunityGoodsService.queryObject(integer);
				goodslist.add(communityGoodsEntity);
			}
		}

		nxPromote.setNxCommunityGoodsEntities(goodslist);

		return R.ok().put("data", nxPromote);
	}
	

	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("nxpromote:update")
	public R update(@RequestBody NxCommunityPromoteEntity nxPromote){
		nxCommunityPromoteService.update(nxPromote);
		
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete")
	@RequiresPermissions("nxpromote:delete")
	public R delete(@RequestBody Integer[] nxPromoteIds){
		nxCommunityPromoteService.deleteBatch(nxPromoteIds);
		
		return R.ok();
	}
	
}
