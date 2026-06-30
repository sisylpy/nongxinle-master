package com.nongxinle.community.catalog.controller;

/**
 * 
 *
 * @author lpy
 * @date 04-06 00:18
 */

import com.nongxinle.entity.NxCommunityGoodsEntity;
import com.nongxinle.utils.Constant;
import com.nongxinle.utils.UploadFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCommunityGoodsSetItemEntity;
import com.nongxinle.community.catalog.service.NxCommunityGoodsSetItemService;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.io.File;


@RestController
@RequestMapping("api/nxcommunitygoodssetitem")
public class NxCommunityGoodsSetItemController {
	@Autowired
	private NxCommunityGoodsSetItemService nxCommunityGoodsSetItemService;

	@RequestMapping(value = "/clearItemGoodsId", method = RequestMethod.POST)
	@ResponseBody
	public R clearItemGoodsId (Integer id) {
		NxCommunityGoodsSetItemEntity itemEntity = nxCommunityGoodsSetItemService.queryObject(id);
		itemEntity.setNxCgsiCgGoodsId(null);
		nxCommunityGoodsSetItemService.update(itemEntity);
		return R.ok();
	}

	@RequestMapping(value = "/updateCgSetItem", method = RequestMethod.POST)
	@ResponseBody
	public R updateCgSetItem (@RequestBody NxCommunityGoodsSetItemEntity itemEntity ) {
		System.out.println("whwhwwhwmammamamma");
		nxCommunityGoodsSetItemService.update(itemEntity);
	    return R.ok();
	}
//	@RequestMapping(value = "/updateCgSetItem", method = RequestMethod.POST)
//	@ResponseBody
//	public R updateCgSetItem (@RequestBody NxCommunityGoodsSetItemEntity  item) {
//
//		System.out.println("whwhwwhwmammamamma");
//		nxCommunityGoodsSetItemService.update(item);
//		return R.ok();
//	}

	@RequestMapping(value = "/delSetItem/{id}")
	@ResponseBody
	public R delSetItem (@PathVariable Integer id) {
		nxCommunityGoodsSetItemService.delete(id);
		return R.ok();
	}


	@RequestMapping(value = "/saveSetItem", method = RequestMethod.POST)
	@ResponseBody
	public R saveSetItem (@RequestBody NxCommunityGoodsSetItemEntity  item) {
		item.setNxCgsiItemStatus(0);
	    nxCommunityGoodsSetItemService.save(item);
	    return R.ok();
	}


	@RequestMapping(value = "/updateCgSetItemWithFile", method = RequestMethod.POST)
	@ResponseBody
	public R updateCgSetItemWithFile(@RequestParam("file") MultipartFile file,
									@RequestParam("id") Integer id,
									@RequestParam("name") String name,
									@RequestParam("price") String price,
									@RequestParam("quantity") String quantity,
									@RequestParam("type") Integer type,
									HttpSession session) {

		System.out.println("fdafadsfadasfa");
		NxCommunityGoodsSetItemEntity setItemEntity = nxCommunityGoodsSetItemService.queryObject(id);
		String oldPath = setItemEntity.getNxCgsiItemFilePath();
		if (oldPath != null && !oldPath.trim().isEmpty()) {
			String oldAbsolutePath = Constant.EXTERNAL_IMAGE_DIR + oldPath;
			File file1 = new File(oldAbsolutePath);
			if (file1.exists()) {
				file1.delete();
			}
		}

		//1,上传图片
		String newUploadName = "goodsImage";
		String realPath = UploadFile.upload(session, newUploadName, file);

		String filename = file.getOriginalFilename();
		String filePath = newUploadName + "/" + filename;

		setItemEntity.setNxCgsiItemName(name);
		setItemEntity.setNxCgsiItemPrice(price);
		setItemEntity.setNxCgsiItemQuantity(quantity);
		setItemEntity.setNxCgsiItemFilePath(filePath);
		setItemEntity.setNxCgsiItemType(type);

		nxCommunityGoodsSetItemService.update(setItemEntity);

		return R.ok();
	}

	
}
