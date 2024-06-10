package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 03-26 18:31
 */

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.*;
import com.nongxinle.service.GbDepFoodService;
import com.nongxinle.service.GbDistributerFoodGoodsService;
import com.nongxinle.utils.UploadFile;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbDistributerFoodEntity;
import com.nongxinle.service.GbDistributerFoodService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.PinYin4jUtils.hanziToPinyin;


@RestController
@RequestMapping("api/gbdistributerfood")
public class GbDistributerFoodController {
	@Autowired
	private GbDistributerFoodService gbDistributerFoodService;
	@Autowired
	private GbDistributerFoodGoodsService gbDistributerFoodGoodsService;
	@Autowired
	private GbDepFoodService gbDepFoodService;


	@RequestMapping(value = "/depGetAllFood", method = RequestMethod.POST)
	@ResponseBody
	public R depGetAllFood (Integer disId, Integer depFatherId) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("depFatherId", depFatherId);
		List<GbDistributerFoodEntity> foodEntities = gbDistributerFoodService.queryDisAllFood(map);
		return R.ok().put("data", foodEntities);

	}


	@RequestMapping(value = "/getDisAllFood/{disId}")
	@ResponseBody
	public R getDisAllFood(@PathVariable Integer disId) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		List<GbDistributerFoodEntity> foodEntities = gbDistributerFoodService.queryDisAllFood(map);
		return R.ok().put("data", foodEntities);
	}


	@RequestMapping(value = "/getFoodList/{fatherId}")
	@ResponseBody
	public R getFoodList(@PathVariable Integer fatherId) {
		System.out.println(fatherId + "fatheridiiddid");
		Map<String, Object> map = new HashMap<>();
		map.put("fatherId", fatherId);
		List<GbDistributerFoodEntity> foodEntities = gbDistributerFoodService.queryFoodByParams(map);
		return R.ok().put("data", foodEntities);
	}


	@RequestMapping(value = "/deleteFood",  method = RequestMethod.POST)
	@ResponseBody
	public R deleteFood(Integer id, HttpSession session) {
		Map<String, Object> map = new HashMap<>();
		map.put("foodId", id);
		List<GbDepFoodEntity> depFoodEntities = gbDepFoodService.queryDepFoodByParams(map);
		if(depFoodEntities.size() > 0){
			return R.error(-1, "有门店下载了菜品");
		}else{

			GbDistributerFoodEntity foodEntity = gbDistributerFoodService.queryObject(id);
			String gbDistributerFoodImg = foodEntity.getGbDistributerFoodImg();
			ServletContext servletContext = session.getServletContext();
			String realPath1 = servletContext.getRealPath(gbDistributerFoodImg);
			System.out.println("zhelishisrellalpathhth0000000" + realPath1);
			File file1 = new File(realPath1);
			if(file1.exists()) {
				file1.delete();
			}
			gbDistributerFoodService.delete(id);
			return R.ok();
		}

	}
	@RequestMapping(value = "/updateFood", method = RequestMethod.POST)
	@ResponseBody
	public R updateFood (@RequestBody GbDistributerFoodEntity food,HttpSession session  ) {
		Map<String, Object> map = new HashMap<>();
		map.put("foodName", food.getGbDistributerFoodName());
		map.put("dayuFatherId", 0);
		List<GbDistributerFoodEntity> foodEntities = gbDistributerFoodService.queryFoodByParams(map);
		System.out.println(foodEntities.size() + "fadklfasklfjd;alsj;df");
		if(foodEntities.size() > 0 && foodEntities.get(0).getGbDistributerFoodId() != food.getGbDistributerFoodId()){
			return R.error(-1, "名称重复了");
		}else{
			// old
			GbDistributerFoodEntity oldFoodEntity = gbDistributerFoodService.queryObject(food.getGbDistributerFoodId());
			String gbDistributerFoodImgOld = oldFoodEntity.getGbDistributerFoodImg();
			ServletContext servletContext = session.getServletContext();
			String oldRealPath = servletContext.getRealPath(gbDistributerFoodImgOld);
			File fileOld = new File(oldRealPath);

			//new
			String gbDistributerFoodName = food.getGbDistributerFoodName();
			String newUploadName = "foodImage";
			String headByString = hanziToPinyin(gbDistributerFoodName);
			String newRealPath = servletContext.getRealPath(newUploadName+ "/" + headByString + ".jpg");
			File newFile = new File(newRealPath);
			if(fileOld.exists()) {
				fileOld.renameTo(newFile);
			}

            food.setGbDistributerFoodImg(newUploadName+ "/" + headByString + ".jpg");
			gbDistributerFoodService.update(food);
			return R.ok();
		}

	}

	@RequestMapping(value = "/updateFoodWithFile", method = RequestMethod.POST)
	@ResponseBody
	public R updateFoodWithFile (@RequestParam("file") MultipartFile file,
								 @RequestParam("foodName") String foodName,
								 @RequestParam("id") Integer id,
								 @RequestParam("price") String price,
								 @RequestParam("method") String method,
								 HttpSession session
	) {
		Map<String, Object> map = new HashMap<>();
		map.put("foodName", foodName);
		map.put("dayuFatherId", 0);
		System.out.println("-------------");
		System.out.println(map);
		List<GbDistributerFoodEntity> foodEntities = gbDistributerFoodService.queryFoodByParams(map);
		System.out.println("neww" + id);
		if(foodEntities.size() > 0 && foodEntities.get(0).getGbDistributerFoodId() != id){
			return R.error(-1, "名称重复了");
		}else{

			GbDistributerFoodEntity foodEntity = gbDistributerFoodService.queryObject(id);
			String gbDistributerFoodImg = foodEntity.getGbDistributerFoodImg();
			ServletContext servletContext = session.getServletContext();
			String realPath1 = servletContext.getRealPath(gbDistributerFoodImg);
			File file1 = new File(realPath1);
			if(file1.exists()) {
				file1.delete();
			}

//1,上传图片
			String filePath = "";
			String newUploadName = "foodImage";
			String headByString = hanziToPinyin(foodName);
			String realPath = UploadFile.uploadFileName(session, newUploadName, file, headByString);
			filePath = newUploadName + "/" + headByString + ".jpg";
			System.out.println("222222222222");
			foodEntity.setGbDistributerFoodImg(filePath);
			foodEntity.setGbDistributerFoodMethod(method);
			foodEntity.setGbDistributerFoodName(foodName);
			foodEntity.setGbDistributerFoodPrice(price);
			gbDistributerFoodService.update(foodEntity);

			return R.ok();
		}


	}

	@RequestMapping(value = "/saveGbFood",  produces = "text/html;charset=UTF-8")
	@ResponseBody
	public R saveGbFood(@RequestParam("file") MultipartFile file,
						@RequestParam("foodName") String foodName,
						@RequestParam("fatherId") Integer fatherId,
						@RequestParam("disId") Integer disId,
						@RequestParam("price") String price,
						@RequestParam("method") String method,
						HttpSession session) {

		Map<String, Object> map = new HashMap<>();
		map.put("foodName", foodName);
		map.put("dayuFatherId", 0);
		List<GbDistributerFoodEntity> foodEntities = gbDistributerFoodService.queryFoodByParams(map);
		if(foodEntities.size() > 0){
			return R.error();
		}else{

			//1,上传图片
			String newUploadName = "foodImage";
			String headByString = hanziToPinyin(foodName);

			String realPath = UploadFile.uploadFileName(session, newUploadName, file, headByString);

			String filename = file.getOriginalFilename();
			String filePath = newUploadName + "/" + headByString + ".jpg";
			GbDistributerFoodEntity foodEntity = new GbDistributerFoodEntity();

			foodEntity.setGbDfGbDistributerId(disId);
			foodEntity.setGbDistributerFoodImg(filePath);
			foodEntity.setGbDistributerFoodMethod(method);
			foodEntity.setGbDfGbDistributerId(disId);
			foodEntity.setGbDistributerFoodFatherId(fatherId);
			foodEntity.setGbDistributerFoodName(foodName);
			foodEntity.setGbDistributerFoodPrice(price);
			gbDistributerFoodService.save(foodEntity);
			return R.ok();
		}

	}


	@RequestMapping(value = "/deleteGbSupplierFather/{id}")
	@ResponseBody
	public R deleteGbSupplierFather(@PathVariable Integer id) {
		Map<String, Object> map = new HashMap<>();
		map.put("fatherId", id);
		List<GbDistributerFoodEntity> supplierEntities = gbDistributerFoodService.queryFoodByParams(map);
		if(supplierEntities.size() > 0){
			return R.error(-1,"类别下有供货商，不能删除");
		}else{
			gbDistributerFoodService.delete(id);
			return R.ok();
		}
	}

	@RequestMapping(value = "/deleteGbSupplier/{id}")
	@ResponseBody
	public R deleteGbSupplier(@PathVariable Integer id) {
		Map<String, Object> map = new HashMap<>();
		map.put("supplierId", id);
		map.put("status", 4);
//		List<GbDistributerPurchaseBatchEntity> entities = gbDisPurchaseBatchService.queryList(map);
//		if(entities.size() > 0){
//			return R.error(-1,"有未结账单");
//		}else{
//			gbDistributerFoodService.delete(id);
//			return R.ok();
//		}
		return R.ok();
	}

	@RequestMapping(value = "/updateGbFood", method = RequestMethod.POST)
	@ResponseBody
	public R updateGbFood(@RequestBody GbDistributerFoodEntity suppler) {
		gbDistributerFoodService.update(suppler);
		return R.ok();
	}
	@RequestMapping(value = "/saveGbFoodFather", method = RequestMethod.POST)
	@ResponseBody
	public R saveGbFoodFather(@RequestBody GbDistributerFoodEntity foodEntity) {
		String gbDistributerFoodName = foodEntity.getGbDistributerFoodName();
		Map<String, Object> map = new HashMap<>();
		map.put("foodName", gbDistributerFoodName);
		map.put("fatherId", 0);
		List<GbDistributerFoodEntity> foodEntities = gbDistributerFoodService.queryFoodByParams(map);
		if(foodEntities.size() > 0){
			return R.error(-1, "名称重复了");
		}else{
			foodEntity.setGbDistributerFoodFatherId(0);
			gbDistributerFoodService.save(foodEntity);
			return R.ok();
		}
	}

	@RequestMapping(value = "/disGetFood/{disId}")
	@ResponseBody
	public R disGetFood(@PathVariable Integer disId) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("fatherId", 0);
		List<GbDistributerFoodEntity> supplierEntities = gbDistributerFoodService.queryFoodByParams(map);
		return R.ok().put("data", supplierEntities);
	}

	
}
