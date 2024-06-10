package com.nongxinle.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.SysGeneratorService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

/**
 * 代码生成器
 * 
 */
@Controller
@RequestMapping("/sys/generator")
public class SysGeneratorController {
	@Autowired
	private SysGeneratorService sysGeneratorService;
	
	/**
	 * 列表
	 */
	@ResponseBody
	@RequestMapping("/list")
	@RequiresPermissions("sys:generator:list")
	public R list(String tableName, Integer page, Integer limit){
		Map<String, Object> map = new HashMap<>();
		map.put("tableName", tableName);
		map.put("offset", (page - 1) * limit);
		map.put("limit", limit);
		
		//查询列表数据
		List<Map<String, Object>> list = sysGeneratorService.queryList(map);
		int total = sysGeneratorService.queryTotal(map);
		
		PageUtils pageUtil = new PageUtils(list, total, limit, page);
		
		return R.ok().put("page", pageUtil);
	}

//	/**
//	 * 生成代码
//	 */
//	@RequestMapping("/code/{tables}")
//	@RequiresPermissions("sys:generator:code")
//	public void code(@PathVariable  String tables, HttpServletResponse response) throws IOException{
//		System.out.println(tables);
//		System.out.println("????????");
//		String[] tableNames = new String[]{};
//		tableNames = JSON.parseArray(tables).toArray(tableNames);
//
//		byte[] data = sysGeneratorService.generatorCode(tableNames);
//
//		response.reset();
//		response.setHeader("Content-Disposition", "attachment; filename=\"code.zip\"");
//		response.addHeader("Content-Length", "" + data.length);
//		response.setContentType("application/octet-stream; charset=UTF-8");
//
//		IOUtils.write(data, response.getOutputStream());
//	}
//
//
//
	/**
	 * 生成代码
	 */
	@RequestMapping("/code")
	@RequiresPermissions("sys:generator:code")
	public void code(@RequestBody String[] tableNames, HttpServletResponse response, HttpSession session ) throws IOException{

		System.out.println("----0000000=--------");
//		String[] tableNames = new String[]{};
//		tableNames = JSON.parseArray(tables).toArray(tableNames);

		byte[] data = sysGeneratorService.generatorCode(tableNames);
		System.out.println("data:" + data.length);

		//1，保存文件
		ServletContext servletContext = session.getServletContext();
		String realPath = servletContext.getRealPath("generator");


		File codeFile = new File(realPath);
		if(!codeFile.exists()) {
			codeFile.mkdirs();
		}
        codeFile = new File(codeFile + "/" + "code.zip");


        OutputStream output = new FileOutputStream(codeFile);
        output.write(data);
        output.close();


		response.reset();
		response.setHeader("Content-Disposition", "attachment; filename=\"code.zip\"");
		response.addHeader("Content-Length", "" + data.length);
		response.setContentType("application/octet-stream; charset=UTF-8");

		IOUtils.write(data, response.getOutputStream());

//		HttpHeaders httpHeaders = new HttpHeaders();
//		httpHeaders.add("content-Disposition","attachment;filename=aa.zip" );
//
//
//		ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(data, httpHeaders, HttpStatus.OK);
//		return responseEntity;


	}

}
