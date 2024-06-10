package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 04-14 12:45
 */

import java.util.*;

import com.nongxinle.entity.NxOrderTemplateEntity;
import com.nongxinle.service.NxOrderTemplateService;
import com.nongxinle.utils.UploadFile;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;


@RestController
@RequestMapping("api/nxordertemplate")
public class NxOrderTemplateController {
	@Autowired
	private NxOrderTemplateService nxOrderTemplateService;



	@RequestMapping(value = "/getTemplate/{nxTemplateId}")
	@ResponseBody
	public R getTemplate(@PathVariable Integer nxTemplateId) {
		Map<String, Object> map = new HashMap<>();
		map.put("nxTemplateId", nxTemplateId);
		NxOrderTemplateEntity templateEntity =  nxOrderTemplateService.queryTemplate(map);


		return R.ok().put("data",templateEntity);
	}


    @RequestMapping(value = "/getTemplateList/{customerUserId}")
    @ResponseBody
    public R getTemplateList(@PathVariable Integer customerUserId) {
		Map<String, Object> map = new HashMap<>();
		map.put("customerUserId", customerUserId);
    	List<NxOrderTemplateEntity> templateEntities =  nxOrderTemplateService.queryList(map);


        return R.ok().put("data",templateEntities);
    }

	@RequestMapping(value = "/addNewTemplate", produces = "text/html;charset=UTF-8")
	@ResponseBody
	public R addNewStudy(@RequestParam("file") MultipartFile file, @RequestParam("templateName") String templateName,
						 @RequestParam("customerUserId") Integer customerUserId,  HttpSession session) {
		System.out.println("hhahaha");
		System.out.println(file);



		//1,上传图片
		String newUploadName = "uploadImage";
		String realPath = UploadFile.upload(session, newUploadName, file);

		String filename = file.getOriginalFilename();
		String filePath = newUploadName + "/" + filename;

		NxOrderTemplateEntity templateEntity = new NxOrderTemplateEntity();
		templateEntity.setNxOdFilePath(filePath);
		templateEntity.setNxOdName(templateName);
		templateEntity.setNxOdCustomerUserId(customerUserId);

		System.out.println("eeee" + templateEntity);
		templateEntity.setNxOdItemAmount(0);


		nxOrderTemplateService.save(templateEntity);


		return R.ok();
	}
	
	/**
	 * 列表
	 */
	@ResponseBody
	@RequestMapping("/list")
	@RequiresPermissions("nxodertemplate:list")
	public R list(Integer page, Integer limit){
		Map<String, Object> map = new HashMap<>();
		map.put("offset", (page - 1) * limit);
		map.put("limit", limit);
		
		//查询列表数据
		List<NxOrderTemplateEntity> nxOderTemplateList = nxOrderTemplateService.queryList(map);
		int total = nxOrderTemplateService.queryTotal(map);
		
		PageUtils pageUtil = new PageUtils(nxOderTemplateList, total, limit, page);
		
		return R.ok().put("page", pageUtil);
	}
	
	
	/**
	 * 信息
	 */
	@ResponseBody
	@RequestMapping("/info/{nxOrderTemplateId}")
	@RequiresPermissions("nxodertemplate:info")
	public R info(@PathVariable("nxOrderTemplateId") Integer nxOrderTemplateId){
		NxOrderTemplateEntity nxOderTemplate = nxOrderTemplateService.queryObject(nxOrderTemplateId);
		
		return R.ok().put("nxOderTemplate", nxOderTemplate);
	}
	
	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping("/save")
	@RequiresPermissions("nxodertemplate:save")
	public R save(@RequestBody NxOrderTemplateEntity nxOderTemplate){
		nxOrderTemplateService.save(nxOderTemplate);
		
		return R.ok();
	}
	
	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("nxodertemplate:update")
	public R update(@RequestBody NxOrderTemplateEntity nxOderTemplate){
		nxOrderTemplateService.update(nxOderTemplate);
		
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete")
	@RequiresPermissions("nxodertemplate:delete")
	public R delete(@RequestBody Integer[] nxOrderTemplateIds){
		nxOrderTemplateService.deleteBatch(nxOrderTemplateIds);
		
		return R.ok();
	}
	
}
