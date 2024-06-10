package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 06-18 21:32
 */

import java.math.BigDecimal;
import java.util.*;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.sun.tools.internal.xjc.reader.dtd.bindinfo.BIAttribute;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.PinYin4jUtils.hanziToPinyin;


@RestController
@RequestMapping("api/gbdepartmentdisgoods")
public class GbDepartmentDisGoodsController {
	@Autowired
	private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
	@Autowired
	private GbDistributerGoodsService gbDistributerGoodsService;
	@Autowired
	private GbDepartmentGoodsStockService gbDepGoodsStockService;
	@Autowired
	private GbDepartmentService gbDepartmentService;


	@RequestMapping(value = "/getDepartmentFreshGoods/{depId}")
	@ResponseBody
	public R getDepartmentFreshGoods(@PathVariable  Integer depId) {
		String s = formatWhatFullTime(0);
		Map<String, Object> map = new HashMap<>();
		map.put("depId", depId);
		map.put("fresh", 1);
		map.put("dayuStatus", -1);
		map.put("restWeight", 1);
		List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(map);

		return R.ok().put("data", departmentDisGoodsEntities);

	}
	
	@RequestMapping(value = "/getDepGoodsStockList", method = RequestMethod.POST)
	@ResponseBody
	public R getDepGoodsStockList (Integer disGoodsId, Integer depId,
								   String startDate, String stopDate) {

		Map<String, Object> map = new HashMap<>();
		map.put("disGoodsId", disGoodsId);
		map.put("stockDepId", depId);
		map.put("startDate", startDate);
		map.put("stopDate", stopDate);

		List<GbDepartmentGoodsStockEntity> stockEntities = gbDepGoodsStockService.queryGoodsStockByParams(map);


		return R.ok().put("data", stockEntities);
	}



	@RequestMapping(value = "/addDepartmentDisGoods", method = RequestMethod.POST)
	@ResponseBody
	public R addDepartmentDisGoods (Integer disGoodsId, Integer depId, String sellingPrice) {
		GbDistributerGoodsEntity disGoods = gbDistributerGoodsService.queryObject(disGoodsId);

		List<GbDepartmentEntity> departmentEntities = gbDepartmentService.querySubDepartments(depId);
		if(departmentEntities.size() > 0){
			for (GbDepartmentEntity subDeps : departmentEntities) {
				//添加部门商品
				GbDepartmentDisGoodsEntity disGoodsEntity = new GbDepartmentDisGoodsEntity();
				disGoodsEntity.setGbDdgDepGoodsName(disGoods.getGbDgGoodsName());
				disGoodsEntity.setGbDdgDisGoodsId(disGoods.getGbDistributerGoodsId());
				disGoodsEntity.setGbDdgDisGoodsFatherId(disGoods.getGbDgDfgGoodsFatherId());
				disGoodsEntity.setGbDdgDepGoodsPinyin(disGoods.getGbDgGoodsPinyin());
				disGoodsEntity.setGbDdgDepGoodsPy(disGoods.getGbDgGoodsPy());
				disGoodsEntity.setGbDdgDepGoodsStandardname(disGoods.getGbDgGoodsStandardname());
				disGoodsEntity.setGbDdgDepartmentId(subDeps.getGbDepartmentId());
				disGoodsEntity.setGbDdgDepartmentFatherId(depId);
				disGoodsEntity.setGbDdgGbDepartmentId(disGoods.getGbDgGbDepartmentId());
				disGoodsEntity.setGbDdgGbDisId(disGoods.getGbDgDistributerId());
				disGoodsEntity.setGbDdgGoodsType(disGoods.getGbDgGoodsType());
				disGoodsEntity.setGbDdgStockTotalWeight("0.0");
				disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
				disGoodsEntity.setGbDdgShowStandardId(-1);
				disGoodsEntity.setGbDdgShowStandardName(disGoods.getGbDgGoodsStandardname());
				disGoodsEntity.setGbDdgShowStandardScale("-1");
				disGoodsEntity.setGbDdgShowStandardWeight("0");
				disGoodsEntity.setGbDdgSellingPrice(sellingPrice);
				gbDepartmentDisGoodsService.save(disGoodsEntity);
			}
		}else{
			//添加部门商品
			GbDepartmentDisGoodsEntity disGoodsEntity = new GbDepartmentDisGoodsEntity();
			disGoodsEntity.setGbDdgDepGoodsName(disGoods.getGbDgGoodsName());
			disGoodsEntity.setGbDdgDisGoodsId(disGoods.getGbDistributerGoodsId());
			disGoodsEntity.setGbDdgDisGoodsFatherId(disGoods.getGbDgDfgGoodsFatherId());
			disGoodsEntity.setGbDdgDepGoodsPinyin(disGoods.getGbDgGoodsPinyin());
			disGoodsEntity.setGbDdgDepGoodsPy(disGoods.getGbDgGoodsPy());
			disGoodsEntity.setGbDdgDepGoodsStandardname(disGoods.getGbDgGoodsStandardname());
			disGoodsEntity.setGbDdgDepartmentId(depId);
			disGoodsEntity.setGbDdgDepartmentFatherId(depId);
			disGoodsEntity.setGbDdgGbDepartmentId(disGoods.getGbDgGbDepartmentId());
			disGoodsEntity.setGbDdgGbDisId(disGoods.getGbDgDistributerId());
			disGoodsEntity.setGbDdgGoodsType(disGoods.getGbDgGoodsType());
			disGoodsEntity.setGbDdgStockTotalWeight("0.0");
			disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
			disGoodsEntity.setGbDdgShowStandardId(-1);
			disGoodsEntity.setGbDdgShowStandardName(disGoods.getGbDgGoodsStandardname());
			disGoodsEntity.setGbDdgShowStandardScale("-1");
			disGoodsEntity.setGbDdgShowStandardWeight("0");
			disGoodsEntity.setGbDdgSellingPrice(sellingPrice);
			gbDepartmentDisGoodsService.save(disGoodsEntity);
		}

	    return R.ok();
	}



	@RequestMapping(value = "/getDepGoodsDepartment",  method = RequestMethod.POST)
	@ResponseBody
	public R getDepGoodsDepartment(Integer disGoodsId, Integer disId) {

		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("depType", getGbDepartmentTypeMendian());
		List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryGroupDepsByDisId(map);

		if(departmentEntities.size() > 0){
			for (GbDepartmentEntity department : departmentEntities) {
				Integer gbDepartmentId = department.getGbDepartmentId();
				Map<String, Object> map1 = new HashMap<>();
				map1.put("depFatherId", gbDepartmentId);
				map1.put("disGoodsId", disGoodsId);
				System.out.println("map1111" + map1);
				List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(map1);
				if(departmentDisGoodsEntities.size() > 0){
					department.setIsSelected(true);
					department.setGbDepartmentDisGoodsEntity(departmentDisGoodsEntities.get(0));

				}
			}
		}

		return R.ok().put("data", departmentEntities);
	}


	@RequestMapping(value = "/depPurchserGetDepGoodsGb", method = RequestMethod.POST)
	@ResponseBody
	public R depPurchserGetDepGoodsGb(Integer fatherId, Integer depFatherId) {
		Map<String, Object> mapTotal = new HashMap<>();

		Map<String, Object> mapD = new HashMap<>();
		mapD.put("depFatherId", depFatherId);
		mapD.put("fatherId", fatherId);
		mapD.put("type", getGbDisGoodsTypeZicai());
		System.out.println("purrjrreee" + mapD);
		List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepartmentDisGoodsService.depGetDepsGoodsGb(mapD);

		double aDouble = 0.0;
		if(departmentDisGoodsEntities.size() > 0){
			Map<String, Object> map = new HashMap<>();
//			map.put("depFatherId", depFatherId);
			aDouble = gbDepGoodsStockService.queryDepGoodsRestTotal(map);
		}
//		}
		mapTotal.put("stockTotal", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
		mapTotal.put("arr", departmentDisGoodsEntities);
		return R.ok().put("data", mapTotal);
	}

	@RequestMapping(value = "/depPurchserGetDepGoodsCataGb/{depFatherId}")
	@ResponseBody
	public R depPurchserGetDepGoodsCataGb(@PathVariable Integer depFatherId) {
		Map<String, Object> mapTotal = new HashMap<>();

		Map<String, Object> mapD = new HashMap<>();
		mapD.put("depFatherId", depFatherId);
		mapD.put("type", getGbDisGoodsTypeZicai());
		System.out.println("purrjrreee" + mapD);
		List<GbDistributerFatherGoodsEntity> disGoodsEntities = gbDepartmentDisGoodsService.queryDepTypeFatherGoods(mapD);

		double aDouble = 0.0;
		if(disGoodsEntities.size() > 0){
			Map<String, Object> map = new HashMap<>();
			map.put("depFatherId", depFatherId);
			aDouble = gbDepGoodsStockService.queryDepGoodsRestTotal(map);
		}
//		}
		mapTotal.put("stockTotal", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
		mapTotal.put("arr", disGoodsEntities);
		return R.ok().put("data", mapTotal);
	}


	@RequestMapping(value = "/disGetDepGoodsCataGb/{depFatherId}")
	@ResponseBody
	public R disGetDepGoodsCataGb(@PathVariable Integer depFatherId) {
		Map<String, Object> mapTotal = new HashMap<>();

		List<GbDistributerFatherGoodsEntity> disGoodsEntities = gbDepartmentDisGoodsService.disGetDepDisGoodsCataGb(depFatherId);
		TreeSet<GbDistributerFatherGoodsEntity> grandTree = new TreeSet<>();
		TreeSet<GbDistributerFatherGoodsEntity> greatGrandTree = new TreeSet<>();

		double aDouble = 0.0;
		if(disGoodsEntities.size() > 0){
			Map<String, Object> map = new HashMap<>();
			map.put("depFatherId", depFatherId);
			aDouble = gbDepGoodsStockService.queryDepGoodsRestTotal(map);

//			for (GbDistributerFatherGoodsEntity greatGrandGoods : disGoodsEntities) {
//				TreeSet<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandGoods.getFatherGoodsEntities();
//				if(grandGoodsEntities.size() > 0){
//					BigDecimal greatGrandTotal = new BigDecimal(0);
//
//					for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
//						TreeSet<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
//						if(fatherGoodsEntities.size() > 0){
//							BigDecimal grandDouble = new BigDecimal(0);
//							for (GbDistributerFatherGoodsEntity fatherGoodsEntity: fatherGoodsEntities) {
//								Integer gbFatherGoodsId = fatherGoodsEntity.getGbDistributerFatherGoodsId();
//								double add = 0.0;
//								map.put("disGoodsFatherId", gbFatherGoodsId);
//								Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);
//								if(integer > 0){
//									Double stockTotal = gbDepGoodsStockService.queryDepStockWeightTotal(map);
//									add = add + stockTotal;
//								}
//
//								fatherGoodsEntity.setFatherStockTotal(add);
//								fatherGoodsEntity.setFatherStockTotalString(new BigDecimal(add).setScale(1,BigDecimal.ROUND_HALF_UP).toString());
//								grandDouble = grandDouble.add(new BigDecimal(add));
//								greatGrandTotal = grandDouble.add(new BigDecimal(add));
//
//							}
//							grandFather.setFatherGoodsEntities(abcFatherGoodsStockEvery(fatherGoodsEntities));
//							grandFather.setFatherStockTotal(grandDouble.doubleValue());
//							grandFather.setFatherStockTotalString(grandDouble.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//							grandTree.add(grandFather);
//
//						}
//						greatGrandTree.add(greatGrandGoods);
//						greatGrandGoods.setFatherGoodsEntities(grandTree);
//						greatGrandGoods.setFatherStockTotal(greatGrandTotal.doubleValue());
//						greatGrandGoods.setFatherStockTotalString(greatGrandTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//					}
//				}

			}
//		}
//		grandTree = abcFatherGoodsStockEvery(grandTree);
		mapTotal.put("stockTotal", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
		mapTotal.put("arr", disGoodsEntities);
		return R.ok().put("data", mapTotal);
	}


	private TreeSet<GbDistributerFatherGoodsEntity> getStockGoodsFatherRestSubTotal(Map<String, Object> map0) {
		TreeSet<GbDistributerFatherGoodsEntity> stockAndRecordFatherGoodsTreeSet = getStockFatherGoodsTreeSet(map0);
		TreeSet<GbDistributerFatherGoodsEntity> stockFatherGoodsRestSubtotal = getStockFatherGoodsRestSubtotal(stockAndRecordFatherGoodsTreeSet, map0);
		return stockFatherGoodsRestSubtotal;
	}

	private TreeSet<GbDistributerFatherGoodsEntity> getStockFatherGoodsTreeSet(Map<String, Object> map0) {
		TreeSet<GbDistributerFatherGoodsEntity> stockGoodsEntities = new TreeSet<>();
		Integer integerStock = gbDepGoodsStockService.queryGoodsStockCount(map0);
		if (integerStock > 0) {
			List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDepGoodsStockService.queryDepStockDisFatherGoodsFather(map0);
			stockGoodsEntities.addAll(fatherGoodsEntities);
		}
		return stockGoodsEntities;
	}


	private TreeSet<GbDistributerFatherGoodsEntity> getStockFatherGoodsRestSubtotal
			(TreeSet<GbDistributerFatherGoodsEntity> treeSet, Map<String, Object> map0) {
		TreeSet<GbDistributerFatherGoodsEntity> grandTree = new TreeSet<>();

		for (GbDistributerFatherGoodsEntity greatGrandFather : treeSet) {
			List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
			for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
				BigDecimal grandDouble = new BigDecimal(0);
				List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
				for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
					map0.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
					double add = 0.0;
					System.out.println("---------------------------" + map0);
					Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map0);
					if (integer > 0) {
						Double stockTotal = gbDepGoodsStockService.queryDepGoodsRestTotal(map0);
						System.out.println("kakfdsalkfjdsalfdslafjd;slak;flk" + stockTotal);
						add = add + stockTotal;

					}
					father.setFatherStockTotal(add);
					father.setFatherStockTotalString(new BigDecimal(add).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
					grandDouble = grandDouble.add(new BigDecimal(add));
				}

//				grandFather.setFatherGoodsEntities(abcFatherGoodsStockEvery(fatherGoodsEntities));
				grandFather.setFatherStockTotal(grandDouble.doubleValue());
				grandFather.setFatherStockTotalString(grandDouble.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
				grandTree.add(grandFather);
			}
//            greatGrandFather.setFatherGoodsEntities(grandTree);
//            greatGrandFather.setFatherStockTotal(greatGrandTotal.doubleValue());
//            greatGrandFather.setFatherStockTotalString(greatGrandTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());

		}
		grandTree = abcFatherGoodsStockEvery(grandTree);

		return grandTree;
	}



	private TreeSet<GbDistributerFatherGoodsEntity> abcFatherGoodsStockEvery(TreeSet<GbDistributerFatherGoodsEntity> goodsEntities) {
		TreeSet<GbDistributerFatherGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerFatherGoodsEntity>() {
			@Override
			public int compare(GbDistributerFatherGoodsEntity o1, GbDistributerFatherGoodsEntity o2) {
				int result;

				if (o2.getFatherStockTotal() - o1.getFatherStockTotal() < 0) {
					result = -1;
				} else if (o2.getFatherStockTotal() - o1.getFatherStockTotal() > 0) {
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


	@RequestMapping(value = "/getDepDisGoodsGb/{id}")
	@ResponseBody
	public R getDepDisGoodsGb(@PathVariable Integer id) {
		GbDepartmentDisGoodsEntity gbDepartmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(id);
		return R.ok().put("data", gbDepartmentDisGoodsEntity);
	}

	@RequestMapping(value = "/changeShowStandard", method = RequestMethod.POST)
	@ResponseBody
	public R changeShowStandard (Integer depDisGoodsId, Integer showStandardId, String showStandardName, String showStandardScale) {

		GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depDisGoodsId);
		if(showStandardId == -1){
			departmentDisGoodsEntity.setGbDdgShowStandardWeight(null);
			departmentDisGoodsEntity.setGbDdgShowStandardScale(null);

		}else{
			BigDecimal standScale = new BigDecimal(showStandardScale);
			BigDecimal weight = new BigDecimal(departmentDisGoodsEntity.getGbDdgStockTotalWeight());
			BigDecimal showWeight = weight.divide(standScale, 2, BigDecimal.ROUND_HALF_UP);
			departmentDisGoodsEntity.setGbDdgShowStandardWeight(showWeight.toString());
		}
		departmentDisGoodsEntity.setGbDdgShowStandardScale(showStandardScale);
		departmentDisGoodsEntity.setGbDdgShowStandardName(showStandardName);
		departmentDisGoodsEntity.setGbDdgShowStandardId(showStandardId);
		gbDepartmentDisGoodsService.update(departmentDisGoodsEntity);

			//changeStock
			Map<String, Object> map = new HashMap<>();
			map.put("depGoodsId", depDisGoodsId);
			map.put("restWeight", 0 );
			List<GbDepartmentGoodsStockEntity> stockEntities = gbDepGoodsStockService.queryGoodsStockByParams(map);
			if(stockEntities.size() > 0){
				for (GbDepartmentGoodsStockEntity stock : stockEntities) {
					BigDecimal decimal = new BigDecimal(stock.getGbDgsRestWeight()).divide(new BigDecimal(showStandardScale), 1, BigDecimal.ROUND_HALF_UP);
					if(showStandardScale.equals("-1")){
						stock.setGbDgsRestWeightShowStandard(null);
						stock.setGbDgsRestWeightShowStandardName(null);
					}else{
						stock.setGbDgsRestWeightShowStandard(decimal.setScale(1, BigDecimal.ROUND_HALF_UP).toString() );
						stock.setGbDgsRestWeightShowStandardName(showStandardName);
					}
					gbDepGoodsStockService.update(stock);
				}
			}
		return R.ok();
	}


	@RequestMapping(value = "/updateDepGoodsGb", method = RequestMethod.POST)
	@ResponseBody
	public R updateDepGoodsGb (@RequestBody GbDepartmentDisGoodsEntity depDisGoods) {
	    gbDepartmentDisGoodsService.update(depDisGoods);
	    return R.ok();
	}


//
//	@RequestMapping(value = "/depFatherGetSubDepsGoodsGb/{depFatherId}")
//	@ResponseBody
//	public R depFatherGetSubDepsGoodsGb(@PathVariable Integer depFatherId) {
//		Map<String, Object> map = new HashMap<>();
//		map.put("depFatherId", depFatherId);
//		map.put("status", 1);
//		GbDepartmentEntity departmentEntities = gbDepartmentDisGoodsService.depFatherGetSubDepsGoodsGb(map);
//		return R.ok().put("data", departmentEntities);
//	}

	@RequestMapping(value = "/updateDepGoodsSellingPrice", method = RequestMethod.POST)
	@ResponseBody
	public R updateDepGoodsSellingPrice (Integer depGoodsId, String sellingPrice ) {
		GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depGoodsId);
		departmentDisGoodsEntity.setGbDdgSellingPrice(sellingPrice);
		gbDepartmentDisGoodsService.update(departmentDisGoodsEntity);

		return R.ok();
	}

	@RequestMapping(value = "/deleteDepGoods/{depGoodsId}")
	@ResponseBody
	public R deleteDepGoods(@PathVariable Integer depGoodsId) {
		Map<String, Object> map = new HashMap<>();
		map.put("depGoodsId", depGoodsId);
		map.put("restWeight", 0);
		List<GbDepartmentGoodsStockEntity> stockEntities = gbDepGoodsStockService.queryGoodsStockByParams(map);

		if(stockEntities.size() > 0){
			return R.error(-1,"有库存，不能删除");
		}else{
			gbDepartmentDisGoodsService.delete(depGoodsId);
			return R.ok();
		}
	}



	/**
	 * lscgdh
	 * @param depId
	 * @return
	 */
	@RequestMapping(value = "/stockDepGetDepGoodsGb/{depId}")
	@ResponseBody
	public R stockDepGetDepGoodsGb(@PathVariable Integer depId) {
		Map<String, Object> map = new HashMap<>();
		map.put("depId", depId);
		map.put("status", 4);
		map.put("date", formatWhatDay(0));
		System.out.println("guanndn" + map);
		List<GbDistributerFatherGoodsEntity> goodsEntities = gbDepartmentDisGoodsService.depQueryDepGoodsWithOrderGb(map);

		return R.ok().put("data", goodsEntities);
	}


	/**
	 * lscgdh
	 * @param depId
	 * @return
	 */
	@RequestMapping(value = "/depGetDepGoodsGb/{depId}")
	@ResponseBody
	public R depGetDepGoodsGb(@PathVariable Integer depId) {
		Map<String, Object> map = new HashMap<>();
		map.put("depId", depId);
		map.put("status", 4);
		map.put("date", formatWhatDay(0));
		map.put("pull", 0);
		map.put("notLinshi", 1);
		System.out.println("guanndn" + map);
		List<GbDistributerFatherGoodsEntity> goodsEntities = gbDepartmentDisGoodsService.depQueryDepGoodsWithOrderGb(map);
		map.put("notLinshi", -1);
		List<GbDepartmentDisGoodsEntity> goodsEntities1 = gbDepartmentDisGoodsService.depQueryDepGoodsWithOrderDepGoods(map);

		Map<String, Object> mapR = new HashMap<>();
		mapR.put("arr", goodsEntities);
		mapR.put("linshi", goodsEntities1);
		return R.ok().put("data", mapR);
	}

	/**
	 * lscgdh
	 * @param depId
	 * @return
	 */
	@RequestMapping(value = "/depGetDepGoodsGbCata", method = RequestMethod.POST)
	@ResponseBody
	public R depGetDepGoodsGbCata(Integer depId, Integer fatherId) {
		Map<String, Object> map = new HashMap<>();
		map.put("depId", depId);
		List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = new ArrayList<>();
		List<GbDistributerFatherGoodsEntity> cataList = gbDepartmentDisGoodsService.queryDepTypeFatherGoods(map);
		if(cataList.size() > 0){
			if(fatherId == -1){
				map.put("greatGrandId", cataList.get(0).getGbDistributerFatherGoodsId());
			}else{
				map.put("greatGrandId", fatherId);
			}

			map.put("status", 4);
			map.put("date", formatWhatDay(0));
			map.put("pull", 0);
			map.put("notLinshi", 1);
			System.out.println("mappapapappapa" + map);
			fatherGoodsEntities	 = gbDepartmentDisGoodsService.depQueryDepGoodsWithOrderGb(map);
		}

		Map<String, Object> mapR = new HashMap<>();
		mapR.put("cataList", cataList);
		mapR.put("arr", fatherGoodsEntities);
		return R.ok().put("data", mapR);
	}


	/**
	 * vue后台接口
	 * @param limit
	 * @param page
	 * @param fatherId
	 * @param depFatherId
	 * @param disId
	 * @return
	 */
	@RequestMapping(value = "/depGetDepsGoodsGbPage")
	@ResponseBody
	public R depGetDepsGoodsGbPage(Integer limit, Integer page, Integer fatherId, Integer depFatherId, Integer disId) {

		Map<String, Object> map = new HashMap<>();
		map.put("offset", (page - 1) * limit);
		map.put("limit", limit);
		map.put("depFatherId", depFatherId);
		map.put("status", 1);

		List<GbDepartmentDisGoodsEntity> depDisGoodsEntities = gbDepartmentDisGoodsService.depGetDepsGoodsGb(map);
		if(depDisGoodsEntities != null){
			Map<String, Object> map3 = new HashMap<>();
			map3.put("depFatherId", fatherId );
			int total = gbDepartmentDisGoodsService.queryGbDisGoodsTotal(map3);

			PageUtils pageUtil = new PageUtils(depDisGoodsEntities, total, limit, page);
			return R.ok().put("page", pageUtil);
		}else{
			return R.error(-1,"meiyou");
		}

	}



	
}
