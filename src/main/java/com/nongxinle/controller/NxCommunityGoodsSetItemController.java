package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 04-06 00:18
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxCommunityGoodsEntity;
import com.nongxinle.utils.UploadFile;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCommunityGoodsSetItemEntity;
import com.nongxinle.service.NxCommunityGoodsSetItemService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;


@RestController
@RequestMapping("api/nxcommunitygoodssetitem")
public class NxCommunityGoodsSetItemController {
	@Autowired
	private NxCommunityGoodsSetItemService nxCommunityGoodsSetItemService;



	@RequestMapping(value = "/delSetItem/{id}")
	@ResponseBody
	public R delSetItem (@PathVariable Integer id) {
		nxCommunityGoodsSetItemService.delete(id);
		return R.ok();
	}


	@RequestMapping(value = "/saveSetItem", method = RequestMethod.POST)
	@ResponseBody
	public R saveSetItem (@RequestBody NxCommunityGoodsSetItemEntity  item) {
	    nxCommunityGoodsSetItemService.save(item);
	    return R.ok();
	}


	@RequestMapping(value = "/updateCgSetItem", method = RequestMethod.POST)
	@ResponseBody
	public R updateCgSetItem(@RequestParam("file") MultipartFile file,
									@RequestParam("id") Integer id,
									@RequestParam("name") String name,
									@RequestParam("price") String price,
									@RequestParam("quantity") String quantity,
									@RequestParam("type") Integer type,
									HttpSession session) {
		//1,上传图片
		String newUploadName = "uploadImage";
		String realPath = UploadFile.upload(session, newUploadName, file);

		String filename = file.getOriginalFilename();
		String filePath = newUploadName + "/" + filename;


		NxCommunityGoodsSetItemEntity communityGoodsEntity = nxCommunityGoodsSetItemService.queryObject(id);
        communityGoodsEntity.setNxCgsiItemName(name);
		communityGoodsEntity.setNxCgsiItemPrice(price);
		communityGoodsEntity.setNxCgsiItemPrice(quantity);
		communityGoodsEntity.setNxCgsiItemFilePath(filePath);
		communityGoodsEntity.setNxCgsiItemType(type);

		nxCommunityGoodsSetItemService.update(communityGoodsEntity);

		return R.ok();
	}

	
}
