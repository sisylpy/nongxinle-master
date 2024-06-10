package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDepFoodSalesDao;
import com.nongxinle.entity.GbDepFoodSalesEntity;
import com.nongxinle.service.GbDepFoodSalesService;



@Service("gbDepFoodSalesService")
public class GbDepFoodSalesServiceImpl implements GbDepFoodSalesService {
	@Autowired
	private GbDepFoodSalesDao gbDepFoodSalesDao;
	
	@Override
	public GbDepFoodSalesEntity queryObject(Integer gbDepFoodSalesId){
		return gbDepFoodSalesDao.queryObject(gbDepFoodSalesId);
	}
	
	@Override
	public List<GbDepFoodSalesEntity> queryList(Map<String, Object> map){
		return gbDepFoodSalesDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepFoodSalesDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepFoodSalesEntity gbDepFoodSales){
		gbDepFoodSalesDao.save(gbDepFoodSales);
	}
	
	@Override
	public void update(GbDepFoodSalesEntity gbDepFoodSales){
		gbDepFoodSalesDao.update(gbDepFoodSales);
	}
	
	@Override
	public void delete(Integer gbDepFoodSalesId){
		gbDepFoodSalesDao.delete(gbDepFoodSalesId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDepFoodSalesIds){
		gbDepFoodSalesDao.deleteBatch(gbDepFoodSalesIds);
	}

    @Override
    public List<GbDepFoodSalesEntity> queryDepFoodSalesByParams(Map<String, Object> mapFoodSales) {

		return gbDepFoodSalesDao.queryDepFoodSalesByParams(mapFoodSales);
    }



}
