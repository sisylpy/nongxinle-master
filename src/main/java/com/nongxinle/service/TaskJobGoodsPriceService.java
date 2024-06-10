package com.nongxinle.service;

import com.nongxinle.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbDepartmentTypeMendian;

@Service
public class TaskJobGoodsPriceService extends TimerTask {
    @Autowired
    private NxDistributerGoodsService goodsService;

    @Override
    public void run() {

        List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = goodsService.queryListGoodsAll();
        if (nxDistributerGoodsEntities.size() > 0) {
            for (NxDistributerGoodsEntity dis : nxDistributerGoodsEntities) {
                updateGoods(dis);
            }
        }
    }

    void updateGoods(NxDistributerGoodsEntity goodsEntity) {
        Integer howManyDaysInPeriod = getHowManyDaysInPeriod(formatWhatDay(0), goodsEntity.getNxDgWillPriceUpdate() );
        if(howManyDaysInPeriod < 3){
            String nxDgPriceSecondDay = goodsEntity.getNxDgPriceSecondDay();
            String nxDgPriceFirstDay = goodsEntity.getNxDgPriceFirstDay();
            String nxDgWillPrice = goodsEntity.getNxDgWillPrice();
            if(goodsEntity.getNxDgWillPriceUpdate().equals(formatWhatDay(-1))){
                goodsEntity.setNxDgPriceThirdDay(nxDgPriceSecondDay);
                goodsEntity.setNxDgPriceSecondDay(nxDgPriceFirstDay);
                goodsEntity.setNxDgPriceFirstDay(nxDgWillPrice);
                System.out.println("goodsupsd"+ goodsEntity.getNxDgGoodsName());
            }
            if(goodsEntity.getNxDgWillPriceUpdate().equals(formatWhatDay(-2))){
                goodsEntity.setNxDgPriceThirdDay(nxDgPriceSecondDay);
                goodsEntity.setNxDgPriceSecondDay(nxDgPriceFirstDay);
                goodsEntity.setNxDgPriceFirstDay("0.1");
                System.out.println("goodsupsd-222222"+ formatWhatDay(-2));
            } if(goodsEntity.getNxDgWillPriceUpdate().equals(formatWhatDay(-3))){
                goodsEntity.setNxDgPriceThirdDay(nxDgPriceSecondDay);
                goodsEntity.setNxDgPriceSecondDay("0.1");
                goodsEntity.setNxDgPriceFirstDay("0.1");
                System.out.println("goodsupsd-33333"+ formatWhatDay(-3));
            }


            goodsService.update(goodsEntity);
        }


    }


}
