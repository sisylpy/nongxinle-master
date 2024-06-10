package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 05-29 16:35
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.utils.MyAPPIDConfig;
import com.nongxinle.utils.UploadFile;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbDepartmentGoodsStockReduceAttachmentEntity;
import com.nongxinle.service.GbDepartmentGoodsStockReduceAttachmentService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;


@RestController
@RequestMapping("api/gbdepartmentgoodsstockreduceattachment")
public class GbDepartmentGoodsStockReduceAttachmentController {
	@Autowired
	private GbDepartmentGoodsStockReduceAttachmentService gbDepartmentGoodsStockReduceAttachmentService;


	@RequestMapping(value = "/reduceAttachmentSaveWithFile", produces = "text/html;charset=UTF-8")
	@ResponseBody
	public R reduceAttachmentSaveWithFile (@RequestParam("file") MultipartFile file,
										   @RequestParam("userName") String userName,
										   @RequestParam("id") Integer id,
										   HttpSession session  ) {
		System.out.println("akfak;fla" + id);
		GbDepartmentGoodsStockReduceAttachmentEntity attachmentEntity = new GbDepartmentGoodsStockReduceAttachmentEntity();

		//添加新用户
		//1,上传图片
		String newUploadName = "uploadImage";
		String realPath = UploadFile.upload(session, newUploadName, file);

		String filename = file.getOriginalFilename();
		String filePath = newUploadName + "/" + filename;
		attachmentEntity.setGbDgsraFilePath(filePath);
		attachmentEntity.setGbDgsraGbDgsrId(id);
		attachmentEntity.setGbDgsraContent(userName);
		gbDepartmentGoodsStockReduceAttachmentService.save(attachmentEntity);


		System.out.println(file);

	    return R.ok();
	}


	
}
