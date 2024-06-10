package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 06-18 21:32
 */

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

import com.nongxinle.entity.*;
import com.nongxinle.service.GbDepartmentDisGoodsService;
import com.nongxinle.service.GbDepartmentGoodsStockService;
import com.nongxinle.service.GbDistributerGoodsService;
import com.nongxinle.utils.UploadFile;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.GbDistributerFatherGoodsService;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.PinYin4jUtils.hanziToPinyin;


@RestController
@RequestMapping("api/gbdistributerfathergoods")
public class GbDistributerFatherGoodsController {
	@Autowired
	private GbDistributerFatherGoodsService gbDistributerFatherGoodsService;
	@Autowired
	private GbDistributerGoodsService gbDistributerGoodsService;
	@Autowired
	private GbDepartmentGoodsStockService gbDepGoodsStockService;
	@Autowired
	private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;


	/**
	 * 管理端，门店端-获取部门库存商品父级列表
	 *
	 * @param depId
	 * @return
	 */
	@RequestMapping(value = "/getDepartmentStockGoodsFatherIsSelf",method = RequestMethod.POST)
	@ResponseBody
	public R getDepartmentStockGoodsFatherIsSelf(String depId, Integer isSelf) {
		Map<String, Object> map = new HashMap<>();
		map.put("depId", depId);
		map.put("isSelf", isSelf);

		List<GbDistributerFatherGoodsEntity> greatGrandGoods =  gbDistributerFatherGoodsService.queryDisGoodsCataWithGoods(map);
		for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandGoods) {
			BigDecimal greatGrandTotal = new BigDecimal(0);
			List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
			for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
				BigDecimal grandDouble = new BigDecimal(0);
				List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
				for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
					Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();

					map.put("disGoodsFatherId", gbDistributerFatherGoodsId);
					Integer stockCount = gbDepGoodsStockService.queryGoodsStockCount(map);
					Double fatherDouble = 0.0;
					if(stockCount > 0){
						 fatherDouble = gbDepGoodsStockService.queryDepGoodsRestTotal(map);
					}
					father.setFatherProduceTotal(fatherDouble);
					father.setFatherProduceTotalString(new BigDecimal(fatherDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

					List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(map);
					father.setSubGoodsCount(Integer.toString(departmentDisGoodsEntities.size()));
					grandDouble = grandDouble.add(new BigDecimal(fatherDouble));
					greatGrandTotal = greatGrandTotal.add(new BigDecimal(fatherDouble));
				}
				grandFather.setFatherProduceTotal(grandDouble.doubleValue());
				grandFather.setFatherProduceTotalString(grandDouble.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
			}
			greatGrandFather.setFatherProduceTotal(greatGrandTotal.doubleValue());
			greatGrandFather.setFatherProduceTotalString(greatGrandTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
		}

		return R.ok().put("data", greatGrandGoods);
	}



	@RequestMapping(value = "/getNxDistributerFatherGoodsPeisong", method = RequestMethod.POST)
	@ResponseBody
	public R getNxDistributerFatherGoodsPeisong (Integer depId, Integer nxDisId) {
		Map<String, Object> map = new HashMap<>();
		map.put("depId", depId);
		map.put("nxDisId", nxDisId);

		List<GbDistributerFatherGoodsEntity> disGoods =  gbDistributerFatherGoodsService.queryDisGoodsCataWithGoods(map);
		if(disGoods.size() > 0){
			return R.ok().put("data", disGoods);
		}
		List<GbDistributerFatherGoodsEntity> zero = new ArrayList<>();
		return R.ok().put("data", zero);
	}
	@RequestMapping(value = "/getNxDistributerFatherGoods", method = RequestMethod.POST)
	@ResponseBody
	public R getNxDistributerFatherGoods (Integer disId, Integer nxDisId) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("nxDisId", nxDisId);

		List<GbDistributerFatherGoodsEntity> disGoods =  gbDistributerFatherGoodsService.queryDisGoodsCataWithGoods(map);
		if(disGoods.size() > 0){
			return R.ok().put("data", disGoods);
		}
		List<GbDistributerFatherGoodsEntity> zero = new ArrayList<>();
		return R.ok().put("data", zero);
	}

	@RequestMapping(value = "/getDisStockOrdersFatherGoods")
	@ResponseBody
	public R getDisStockOrdersFatherGoods(String[] ids , Integer depId) {
		Map<String, Object> map = new HashMap<>();
		map.put("ids",ids);
		map.put("depId",depId);
		List<GbDistributerFatherGoodsEntity> fatherGoodsEntities =
				gbDistributerFatherGoodsService.queryDisStockOrdersFatherGoods(map);

	    return R.ok().put("data", fatherGoodsEntities);
	}




	/**
	 * 获取批发商商品的父类列表
	 * @param disId 批发商id
	 * @return 批发商父类列表
	 */
	@RequestMapping(value = "/disGetDisCataGoods", method = RequestMethod.POST)
	@ResponseBody
	public R disGetDisCataGoods(Integer disId, String goodsType, String controlString, Integer isPrice) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		if(goodsType.equals("jicai")){
			map.put("type", getGbDisGoodsTypeJicai());
		}

		if(goodsType.equals("chuku")){
			map.put("type", getGbDisGoodsTypeChuku());
		}
		if(goodsType.equals("zicai")){
			map.put("type", getGbDisGoodsTypeZicai());
		}
		if(goodsType.equals("kitchen")){
			map.put("type", getGbDisGoodsTypeKitchen());
		}
		if(goodsType.equals("appSupplier")){
			map.put("type", getGbDisGoodsTypeAppSupplier());
		}
		if(controlString.equals("price")){
			map.put("price", "1");
		}
		if(controlString.equals("fresh")){
			map.put("fresh", "1");
		}
		if(controlString.equals("isNotSelf")){
			map.put("isSelf",0);
			map.put("type", getGbDisGoodsTypeChuku());
		}
		if(controlString.equals("isSelf")){
			map.put("isSelf",1);
		}

		if(isPrice != 0){
			map.put("isPrice", isPrice);
		}

		List<GbDistributerFatherGoodsEntity> disGoods =  gbDistributerFatherGoodsService.queryDisGoodsCataWithGoods(map);
		if(disGoods.size() > 0){
//			List<GbDistributerFatherGoodsEntity> result = new ArrayList<>();
//			for(GbDistributerFatherGoodsEntity fatherGoodsEntity: disGoods){
//				System.out.println("dfadfasfdadfalidiidididi");
//				List<GbDistributerFatherGoodsEntity> grandFatherGoodsEntities = abcFatherGoodsSort(fatherGoodsEntity.getFatherGoodsEntities());
//				fatherGoodsEntity.setFatherGoodsEntities(grandFatherGoodsEntities);
//				for (GbDistributerFatherGoodsEntity father: grandFatherGoodsEntities){
//					TreeSet<GbDistributerFatherGoodsEntity> fatherGoodsEntities = abcFatherGoodsSort(father.getFatherGoodsEntities());
//					father.setFatherGoodsEntities(fatherGoodsEntities);
//				}

//			}

			return R.ok().put("data", disGoods);
		}
		List<GbDistributerFatherGoodsEntity> zero = new ArrayList<>();
		return R.ok().put("data", zero);
	}

	@RequestMapping(value = "/disGetDisCataGoodsByGreatGrandId", method = RequestMethod.POST)
	@ResponseBody
	public R disGetDisCataGoodsByGreatGrandId(Integer disId, String goodsType,
											  String controlString, Integer isPrice, Integer greatGrandId) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		if(greatGrandId != -1){
			map.put("greatGrandId", greatGrandId);
		}
		if(goodsType.equals("jicai")){
			map.put("type", getGbDisGoodsTypeJicai());
		}

		if(goodsType.equals("chuku")){
			map.put("type", getGbDisGoodsTypeChuku());
		}
		if(goodsType.equals("zicai")){
			map.put("type", getGbDisGoodsTypeZicai());
		}
		if(goodsType.equals("kitchen")){
			map.put("type", getGbDisGoodsTypeKitchen());
		}
		if(goodsType.equals("appSupplier")){
			map.put("type", getGbDisGoodsTypeAppSupplier());
		}
		if(controlString.equals("price")){
			map.put("price", "1");
		}
		if(controlString.equals("fresh")){
			map.put("fresh", "1");
		}
		if(controlString.equals("isNotSelf")){
			map.put("isSelf",0);
			map.put("type", getGbDisGoodsTypeChuku());
		}
		if(controlString.equals("isSelf")){
			map.put("isSelf",1);
		}

		if(isPrice != 0){
			map.put("isPrice", isPrice);
		}

		System.out.println("catawithththgooogdsss"  + map);

		List<GbDistributerFatherGoodsEntity> disGoods =  gbDistributerFatherGoodsService.queryDisGoodsCataWithGoods(map);
		if(disGoods.size() > 0){
//			List<GbDistributerFatherGoodsEntity> result = new ArrayList<>();
//			for(GbDistributerFatherGoodsEntity fatherGoodsEntity: disGoods){
//				System.out.println("dfadfasfdadfalidiidididi");
//				List<GbDistributerFatherGoodsEntity> grandFatherGoodsEntities = abcFatherGoodsSort(fatherGoodsEntity.getFatherGoodsEntities());
//				fatherGoodsEntity.setFatherGoodsEntities(grandFatherGoodsEntities);
//				for (GbDistributerFatherGoodsEntity father: grandFatherGoodsEntities){
//					TreeSet<GbDistributerFatherGoodsEntity> fatherGoodsEntities = abcFatherGoodsSort(father.getFatherGoodsEntities());
//					father.setFatherGoodsEntities(fatherGoodsEntities);
//				}

//			}

			return R.ok().put("data", disGoods);
		}
		List<GbDistributerFatherGoodsEntity> zero = new ArrayList<>();
		return R.ok().put("data", zero);
	}


	private TreeSet<GbDistributerFatherGoodsEntity> abcFatherGoodsSort(TreeSet<GbDistributerFatherGoodsEntity> goodsEntities) {

		TreeSet<GbDistributerFatherGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerFatherGoodsEntity>() {
			@Override
			public int compare(GbDistributerFatherGoodsEntity o1, GbDistributerFatherGoodsEntity o2) {
				int result;
				System.out.println("sorororro" + o1.getGbDfgFatherGoodsSort());

				if (o2.getGbDfgFatherGoodsSort() - o1.getGbDfgFatherGoodsSort() > 0) {
					System.out.println("sorororro" + o1.getGbDfgFatherGoodsSort());
					result = -1;
				} else if (o2.getGbDfgFatherGoodsSort() - o1.getGbDfgFatherGoodsSort() < 0) {
					result = 1;
				} else {
					result = 1;
				}

				return result;
			}
		});

		ts.addAll(goodsEntities);

		return ts;

	}

	/**
	 * 获取批发商商品的父类列表
	 * @param disId 批发商id
	 * @return 批发商父类列表
	 */
	@RequestMapping(value = "/getDisGoodsCata/{disId}")
	@ResponseBody
	public R getDisGoodsCata(@PathVariable Integer disId) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		List<GbDistributerFatherGoodsEntity> disGoods =  gbDistributerFatherGoodsService.queryDisGoodsCata(map);

		if(disGoods.size() > 0){
			List<GbDistributerFatherGoodsEntity> result = new ArrayList<>();
			for(GbDistributerFatherGoodsEntity fatherGoodsEntity: disGoods){
				System.out.println("dfadfasfdadfalidiidididi");
//				List<GbDistributerFatherGoodsEntity> grandFatherGoodsEntities = abcFatherGoodsSort(fatherGoodsEntity.getFatherGoodsEntities());
//				fatherGoodsEntity.setFatherGoodsEntities(grandFatherGoodsEntities);
//				for (GbDistributerFatherGoodsEntity father: grandFatherGoodsEntities){
//					TreeSet<GbDistributerFatherGoodsEntity> fatherGoodsEntities = abcFatherGoodsSort(father.getFatherGoodsEntities());
//					father.setFatherGoodsEntities(fatherGoodsEntities);
//				}

			}
		}

		System.out.println("wokk" + disGoods);
		GbDistributerFatherGoodsEntity fatherGoodsEntity = new GbDistributerFatherGoodsEntity();
		fatherGoodsEntity.setGbDfgFatherGoodsName("添加新类别");
		fatherGoodsEntity.setGbDfgFatherGoodsColor("#969696");
		disGoods.add(fatherGoodsEntity);
			return R.ok().put("data", disGoods);
	}

	@RequestMapping(value = "/getLevelOneGoods/{disId}")
	@ResponseBody
	public R getLevelOneGoods(@PathVariable Integer disId) {

		Map<String, Object> map = new HashMap<>();
		map.put("disId",disId);
		map.put("goodsLevel", 0);
		List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDistributerFatherGoodsService.queryDisFathersGoodsByParamsGb(map);
		return R.ok().put("data", fatherGoodsEntities);
	}

    @RequestMapping(value = "/getFatherGoods/{id}")
    @ResponseBody
    public R getFatherGoods(@PathVariable Integer id) {
		Map<String, Object> map = new HashMap<>();
		map.put("fathersFatherId",id);
		List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDistributerFatherGoodsService.queryDisFathersGoodsByParamsGb(map);
		return R.ok().put("data", fatherGoodsEntities);
    }






	@RequestMapping(value = "/getAllFatherGoods/{disId}")
	@ResponseBody
	public R getAllFatherGoods(@PathVariable Integer disId) {
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		List<GbDistributerFatherGoodsEntity> goodsEntities = gbDistributerFatherGoodsService.queryDisAll(map);

		return R.ok().put("data", goodsEntities);
	}


	@RequestMapping(value = "/saveFatherGoods", method = RequestMethod.POST)
	@ResponseBody
	public R saveFatherGoods (@RequestBody GbDistributerFatherGoodsEntity fatherGoods) {
		Integer gbDfgDistributerId = fatherGoods.getGbDfgDistributerId();
		Map<String, Object> map5 = new HashMap<>();
		map5.put("disId", gbDfgDistributerId);
		map5.put("goodsLevel", 1);
		map5.put("fathersFatherId", fatherGoods.getGbDfgFathersFatherId());
		List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDistributerFatherGoodsService.queryDisFathersGoodsByParamsGb(map5);
		if(fatherGoodsEntities.size() > 0){
			fatherGoods.setGbDfgFatherGoodsSort(fatherGoodsEntities.size() + 1);
			Integer grandid = fatherGoods.getGbDfgFathersFatherId();
			GbDistributerFatherGoodsEntity greatGrand = gbDistributerFatherGoodsService.queryObject(grandid);
			greatGrand.setGbDfgGoodsAmount(greatGrand.getGbDfgGoodsAmount() + 1);
			gbDistributerFatherGoodsService.update(greatGrand);
		}else{
			fatherGoods.setGbDfgFatherGoodsSort(1);
			fatherGoods.setGbDfgGoodsAmount(0);
		}
		fatherGoods.setGbDfgNxGoodsId(-1);
		fatherGoods.setGbDfgGoodsAmount(0);
	    gbDistributerFatherGoodsService.save(fatherGoods);
	    return R.ok();
	}

	@RequestMapping(value = "/updateFatherGoodsSort", method = RequestMethod.POST)
	@ResponseBody
	public R updateFatherGoodsSort (@RequestBody List<GbDistributerFatherGoodsEntity> fatherGoodsEntityList) {
		for (GbDistributerFatherGoodsEntity fatherGoods : fatherGoodsEntityList) {
			gbDistributerFatherGoodsService.update(fatherGoods);
		}
	    return R.ok();
	}
	@RequestMapping(value = "/updateFatherGoods", method = RequestMethod.POST)
	@ResponseBody
	public R updateFatherGoods (@RequestBody GbDistributerFatherGoodsEntity fatherGoods) {
		gbDistributerFatherGoodsService.update(fatherGoods);

		return R.ok();
	}

	@RequestMapping(value = "/deleteFatherGoods/{goodsId}")
	@ResponseBody
	public R deleteFatherGoods(@PathVariable Integer goodsId) {
		Map<String, Object> map = new HashMap<>();
		map.put("gbFatherId",goodsId );
		List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDistributerGoodsService.queryGoodsByParamsGb(map);
		List<GbDistributerFatherGoodsEntity> fatherGoodsEntities =  gbDistributerFatherGoodsService.querySubFatherGoods(goodsId);
		if(gbDistributerGoodsEntities.size() > 0 || fatherGoodsEntities.size() > 0){
			return R.error(-1,"有商品不能删除");
		}else{

			Integer grandid = gbDistributerFatherGoodsService.queryObject(goodsId).getGbDfgFathersFatherId();
			GbDistributerFatherGoodsEntity greatGrand = gbDistributerFatherGoodsService.queryObject(grandid);
			greatGrand.setGbDfgGoodsAmount(greatGrand.getGbDfgGoodsAmount() - 1);
			gbDistributerFatherGoodsService.update(greatGrand);

			gbDistributerFatherGoodsService.delete(goodsId);

			return R.ok();
		}
	}



	@ResponseBody
	@RequestMapping(value = "/updateFatherGoodsGb", produces = "text/html;charset=UTF-8")
	public R updateFatherGoodsGb(@RequestParam("file") MultipartFile file,
							   @RequestParam("goodsName") String goodsName,
							   @RequestParam("goodsId") Integer goodsId,
							   HttpSession session) {
		GbDistributerFatherGoodsEntity gbDisFatherGoodsEntity = gbDistributerFatherGoodsService.queryObject(goodsId);
		GbDistributerFatherGoodsEntity fatherGoodsEntity = gbDistributerFatherGoodsService.queryObject(goodsId);
		String gbDistributerFoodImg = fatherGoodsEntity.getGbDfgFatherGoodsImg();
		ServletContext servletContext = session.getServletContext();
		String realPath1 = servletContext.getRealPath(gbDistributerFoodImg);
		File file1 = new File(realPath1);
		if(file1.exists()) {
			file1.delete();
		}


		//1,上传图片
		String newUploadName = "goodsImage";
		String headByString = hanziToPinyin(goodsName);

		String realPath = UploadFile.uploadFileName(session, newUploadName, file, headByString);
		String filename = file.getOriginalFilename();
		String filePath = newUploadName + "/" + headByString + ".jpg";
		gbDisFatherGoodsEntity.setGbDfgFatherGoodsImg(filePath);
		gbDisFatherGoodsEntity.setGbDfgFatherGoodsName(goodsName);

		gbDistributerFatherGoodsService.update(gbDisFatherGoodsEntity);
		return R.ok();
	}

	@ResponseBody
	@RequestMapping(value = "/saveFatherGoodsGb", produces = "text/html;charset=UTF-8")
	public R saveFatherGoodsGb(@RequestParam("file") MultipartFile file,
						@RequestParam("goodsName") String goodsName,
						@RequestParam("fatherId") Integer fatherId,
							   @RequestParam("disId") Integer disId,
							   @RequestParam("color") String color,
							   HttpSession session) {

		//1,上传图片
		String newUploadName = "goodsImage";
		String headByString = hanziToPinyin(goodsName);

		String realPath = UploadFile.uploadFileName(session, newUploadName, file, headByString);
		String filename = file.getOriginalFilename();
		String filePath = newUploadName + "/" + headByString + ".jpg";
		Map<String, Object> map = new HashMap<>();
		map.put("fathersFatherId", fatherId);
		List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDistributerFatherGoodsService.queryDisFathersGoodsByParamsGb(map);
		GbDistributerFatherGoodsEntity goodsEntity = new GbDistributerFatherGoodsEntity();
		goodsEntity.setGbDfgFatherGoodsImg(filePath);
		goodsEntity.setGbDfgFatherGoodsName(goodsName);
		goodsEntity.setGbDfgFathersFatherId(fatherId);
		goodsEntity.setGbDfgDistributerId(disId);
		goodsEntity.setGbDfgFatherGoodsSort(fatherGoodsEntities.size() + 1);
		goodsEntity.setGbDfgFatherGoodsLevel(2);
		goodsEntity.setGbDfgGoodsAmount(0);
		goodsEntity.setGbDfgNxGoodsId(-1);
		goodsEntity.setGbDfgPriceAmount(0);
		goodsEntity.setGbDfgPriceTwoAmount(0);
		goodsEntity.setGbDfgPriceThreeAmount(0);
		goodsEntity.setGbDfgFatherGoodsColor(color);

		gbDistributerFatherGoodsService.save(goodsEntity);


		return R.ok();
	}



}
