package com.nongxinle.service.impl;

import com.nongxinle.entity.GbDepFoodSalesEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDepFoodGoodsSalesDao;
import com.nongxinle.entity.GbDepFoodGoodsSalesEntity;
import com.nongxinle.service.GbDepFoodGoodsSalesService;



@Service("gbDepFoodGoodsSalesService")
public class GbDepFoodGoodsSalesServiceImpl implements GbDepFoodGoodsSalesService {
	@Autowired
	private GbDepFoodGoodsSalesDao gbDepFoodGoodsSalesDao;
	
	@Override
	public GbDepFoodGoodsSalesEntity queryObject(Integer gbDepFoodGoodsSalesId){
		return gbDepFoodGoodsSalesDao.queryObject(gbDepFoodGoodsSalesId);
	}
	
	@Override
	public List<GbDepFoodGoodsSalesEntity> queryList(Map<String, Object> map){
		return gbDepFoodGoodsSalesDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepFoodGoodsSalesDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepFoodGoodsSalesEntity gbDepFoodGoodsSales){
		gbDepFoodGoodsSalesDao.save(gbDepFoodGoodsSales);
	}
	
	@Override
	public void update(GbDepFoodGoodsSalesEntity gbDepFoodGoodsSales){
		gbDepFoodGoodsSalesDao.update(gbDepFoodGoodsSales);
	}
	
	@Override
	public void delete(Integer gbDepFoodGoodsSalesId){
		gbDepFoodGoodsSalesDao.delete(gbDepFoodGoodsSalesId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDepFoodGoodsSalesIds){
		gbDepFoodGoodsSalesDao.deleteBatch(gbDepFoodGoodsSalesIds);
	}

    @Override
    public List<GbDepFoodGoodsSalesEntity> queryDepFoodGoodsSalesByParams(Map<String, Object> mapFoodSales) {

		return gbDepFoodGoodsSalesDao.queryDepFoodGoodsSalesByParams(mapFoodSales);
    }

    @Override
    public List<GbDepFoodSalesEntity> queryDepFoodsWithGoods(Map<String, Object> map) {

		return gbDepFoodGoodsSalesDao.queryDepFoodsWithGoods(map);
    }

}
