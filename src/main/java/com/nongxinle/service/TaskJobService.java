package com.nongxinle.service;

import com.nongxinle.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.getWeek;
import static com.nongxinle.utils.GbTypeUtils.getGbDepartmentTypeMendian;

@Service
public class TaskJobService extends TimerTask {
    @Autowired
    GbDepartmentGoodsDailyService gbDepartmentGoodsDailyService;
    @Autowired
    GbDepartmentService gbDepartmentService;
    @Autowired
    GbDistributerService gbDistributerService;
    @Autowired
    GbDistributerGoodsService gbDistributerGoodsService;

    @Override
    public void run() {

        List<GbDistributerEntity> gbDistributerEntities = gbDistributerService.queryListAll();
        if(gbDistributerEntities.size() > 0){
            for (GbDistributerEntity dis : gbDistributerEntities) {
                Map<String, Object> map = new HashMap<>();
                map.put("disId", dis.getGbDistributerId());
                map.put("depType", getGbDepartmentTypeMendian());
                map.put("date", formatWhatDay(-1));
                System.out.println("TaskMapappapapap" + map);
                TreeSet<GbDepartmentEntity> departmentEntities = gbDepartmentGoodsDailyService.queryWhichDepsHasProduceDepGoodsDailyNew(map);
                System.out.println("deng" + departmentEntities.size());
                if (departmentEntities.size() > 0) {
                    int i = 0;
                    for (GbDepartmentEntity department : departmentEntities) {
                        i = i + 1;
                        System.out.println("depaatment" + i);
                        System.out.println("depaatmentnameme" + department.getGbDepartmentName());
                        saveDaily(department.getGbDepartmentId());
                    }
                }
            }
        }
    }

    void saveDaily(Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("date", formatWhatDay(-1));
        Integer integer = gbDepartmentGoodsDailyService.queryDepGoodsDailyCount(map);
        if(integer > 0){
            TreeSet<GbDepartmentDisGoodsEntity> gbDistributerGoodsEntities = gbDepartmentGoodsDailyService.queryDepDisGoodsTreeByParams(map);
            if(gbDistributerGoodsEntities.size() > 0){
                for (GbDepartmentDisGoodsEntity goods : gbDistributerGoodsEntities) {
                    Map<String, Object> map1 = new HashMap<>();
                    map1.put("depGoodsId", goods.getGbDepartmentDisGoodsId());
                    map1.put("date", formatWhatDay(-1));
                    System.out.println("beforoemaapappapapaa" + map1);
                    Integer integer1 = gbDepartmentGoodsDailyService.queryDepGoodsDailyCount(map1);
                    if(integer1 > 0){
                        GbDepartmentGoodsDailyEntity dailyEntity = gbDepartmentGoodsDailyService.queryDepGoodsDailyItem(map1);
                        GbDepartmentGoodsDailyEntity newDailyEntity = new GbDepartmentGoodsDailyEntity();
                        newDailyEntity.setGbDgdGbDistributerId(dailyEntity.getGbDgdGbDistributerId());
                        newDailyEntity.setGbDgdGbDepartmentId(depId);
                        newDailyEntity.setGbDgdGbDepartmentFatherId(depId);
                        newDailyEntity.setGbDgdGbDisGoodsId(goods.getGbDdgDisGoodsId());
                        System.out.println("faieiieiieieididid" + goods.getGbDdgDisGoodsFatherId());
                        newDailyEntity.setGbDgdGbDisGoodsFatherId(goods.getGbDdgDisGoodsFatherId());
                        newDailyEntity.setGbDgdGbDepDisGoodsId(goods.getGbDepartmentDisGoodsId());
                        newDailyEntity.setGbDgdFullTime(formatFullTime());
                        newDailyEntity.setGbDgdTaskTime(formatFullTime());
                        newDailyEntity.setGbDgdDate(formatWhatDay(0));
                        newDailyEntity.setGbDgdWeek(getWeekOfYear(0).toString());
                        newDailyEntity.setGbDgdMonth(formatWhatMonth(0));
                        newDailyEntity.setGbDgdYear(formatWhatYear(0));
                        newDailyEntity.setGbDgdDay(getWeek(0));
                        newDailyEntity.setGbDgdWeight("0");
                        newDailyEntity.setGbDgdSubtotal("0");
                        newDailyEntity.setGbDgdLossWeight("0");
                        newDailyEntity.setGbDgdLossSubtotal("0");
                        newDailyEntity.setGbDgdProduceWeight("0");
                        newDailyEntity.setGbDgdProduceSubtotal("0");
                        newDailyEntity.setGbDgdProfitSubtotal("0");
                        newDailyEntity.setGbDgdSalesSubtotal("0");
                        newDailyEntity.setGbDgdAfterProfitSubtotal("0");
                        newDailyEntity.setGbDgdWasteWeight("0");
                        newDailyEntity.setGbDgdWasteSubtotal("0");
                        newDailyEntity.setGbDgdReturnWeight("0");
                        newDailyEntity.setGbDgdReturnSubtotal("0");
                        newDailyEntity.setGbDgdSellClearHour("-1");
                        newDailyEntity.setGbDgdSellClearMinute("-1");
                        newDailyEntity.setGbDgdLastProduceWeight("0");
                        newDailyEntity.setGbDgdLastWeight(dailyEntity.getGbDgdRestWeight());
                        newDailyEntity.setGbDgdRestWeight(dailyEntity.getGbDgdRestWeight());
                        GbDistributerGoodsEntity distributerGoodsEntity = gbDistributerGoodsService.queryObject(dailyEntity.getGbDgdGbDisGoodsId());
                        if(distributerGoodsEntity.getGbDgControlFresh() == 1){
                            if( new BigDecimal(dailyEntity.getGbDgdRestWeight()).compareTo(BigDecimal.ZERO) == 1){
                                newDailyEntity.setGbDgdFreshRate("0");
                            }else{
                                newDailyEntity.setGbDgdFreshRate("100");
                            }
                        }else{
                            newDailyEntity.setGbDgdFreshRate("100");
                        }
                        System.out.println("saveE");
                        gbDepartmentGoodsDailyService.save(newDailyEntity);
                    }
                }
            }
        }
    }


}
