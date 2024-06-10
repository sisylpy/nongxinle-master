package com.nongxinle.controller;

/**
 * @author lpy
 * @date 03-31 10:13
 */

import java.util.*;

import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import com.nongxinle.service.GbDepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbDepFatherGoodsSettleEntity;
import com.nongxinle.service.GbDepFatherGoodsSettleService;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.GbTypeUtils.getGbDepartmentTypeJiameng;
import static com.nongxinle.utils.GbTypeUtils.getGbDepartmentTypeMendian;


@RestController
@RequestMapping("api/gbdistributerfathergoodssettle")
public class GbDepFatherGoodsSettleController {
    @Autowired
    private GbDepFatherGoodsSettleService gbDisFatherGoodsSettleService;
    @Autowired
    private GbDepartmentService gbDepartmentService;


    @RequestMapping(value = "/disGetPankuGoodsFatherData", method = RequestMethod.POST)
    @ResponseBody
    public R disGetPankuGoodsFatherData(Integer disId, String month) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("month", month);
        TreeSet<GbDistributerFatherGoodsEntity> distributerFatherGoodsEntities = gbDisFatherGoodsSettleService.queryPankuFatherGoods(map);
         List<Map<String, Object>> result  = new ArrayList<>();
		for (GbDistributerFatherGoodsEntity fatherGoods : distributerFatherGoodsEntities) {
			Map<String, Object> mapGoods = new HashMap<>();
			mapGoods.put("goodsName", fatherGoods.getGbDfgFatherGoodsName());
			List<Map<String,Object>> goodsDepList = new ArrayList<>();

			Map<String, Object> mapDis = new HashMap<>();
            mapDis.put("disId", disId);
            mapDis.put("depType", getGbDepartmentTypeMendian());
//            mapDis.put("orDepType", getGbDepartmentTypeJiameng());

            List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryGroupDepsByDisId(mapDis);
			for (GbDepartmentEntity departmentEntity : departmentEntities) {
                //查询部门的4种total值
                Integer gbDepartmentId = departmentEntity.getGbDepartmentId();
				Map<String, Object> depMap = new HashMap<>();
				depMap.put("dep", departmentEntity.getGbDepartmentName());
				// 1，cost
                Map<String, Object> mapSettleCost = new HashMap<>();
				mapSettleCost.put("depId",gbDepartmentId);
				mapSettleCost.put("month", month);
				mapSettleCost.put("type", 1);
				mapSettleCost.put("disGoodsId", fatherGoods.getGbDistributerFatherGoodsId());
				Double aDoubleCost = 0.0;
                List<GbDepFatherGoodsSettleEntity> costList = gbDisFatherGoodsSettleService.queryDisFatherGoodsSettleTotalByParams(mapSettleCost);
                if(costList.size() > 0){
					aDoubleCost =  gbDisFatherGoodsSettleService.queryPankuFatherGoodsTypeTotal(mapSettleCost);
				}
                depMap.put("cost", aDoubleCost);

                // 2，loss
				Map<String, Object> mapSettleLoss = new HashMap<>();
				mapSettleLoss.put("depId",gbDepartmentId);
				mapSettleLoss.put("month", month);
				mapSettleLoss.put("type", 2);
				mapSettleLoss.put("disGoodsId", fatherGoods.getGbDistributerFatherGoodsId());
				Double aDoubleLoss = 0.0;
				List<GbDepFatherGoodsSettleEntity> lossList = gbDisFatherGoodsSettleService.queryDisFatherGoodsSettleTotalByParams(mapSettleLoss);
				if(lossList.size() > 0){
					aDoubleLoss =  gbDisFatherGoodsSettleService.queryPankuFatherGoodsTypeTotal(mapSettleLoss);
				}
				depMap.put("loss", aDoubleLoss);

				// 3,waste
				Map<String, Object> mapSettleWaste = new HashMap<>();
				mapSettleWaste.put("depId",gbDepartmentId);
				mapSettleWaste.put("month", month);
				mapSettleWaste.put("type", 3);
				mapSettleWaste.put("disGoodsId", fatherGoods.getGbDistributerFatherGoodsId());
				Double aDoubleWaste = 0.0;
				List<GbDepFatherGoodsSettleEntity> wasteList = gbDisFatherGoodsSettleService.queryDisFatherGoodsSettleTotalByParams(mapSettleWaste);
				if(wasteList.size() > 0){
					aDoubleWaste =  gbDisFatherGoodsSettleService.queryPankuFatherGoodsTypeTotal(mapSettleWaste);
				}
				depMap.put("waste", aDoubleWaste);

				// 3,return
				Map<String, Object> mapSettleReturn = new HashMap<>();
				mapSettleReturn.put("depId",gbDepartmentId);
				mapSettleReturn.put("month", month);
				mapSettleReturn.put("type", 4);
				mapSettleReturn.put("disGoodsId", fatherGoods.getGbDistributerFatherGoodsId());
				Double aDoubleReturn = 0.0;
				List<GbDepFatherGoodsSettleEntity> returnList = gbDisFatherGoodsSettleService.queryDisFatherGoodsSettleTotalByParams(mapSettleReturn);
				if(returnList.size() > 0){
					aDoubleReturn =  gbDisFatherGoodsSettleService.queryPankuFatherGoodsTypeTotal(mapSettleReturn);
				}
				depMap.put("return", aDoubleReturn);

				goodsDepList.add(depMap);
			}
			mapGoods.put("list", goodsDepList);
			result.add(mapGoods);

		}

        return R.ok().put("data", result);
    }


}
