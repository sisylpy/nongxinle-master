package com.nongxinle.service.impl;

import com.nongxinle.dao.NxRetailerUserDao;
import com.nongxinle.entity.*;
import com.nongxinle.service.NxRetailerGoodsShelfService;
import com.nongxinle.service.NxRetailerUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxRetailerDao;
import com.nongxinle.service.NxRetailerService;

import static com.nongxinle.utils.DateUtils.formatWhatDay;


@Service("nxRetailerService")
public class NxRetailerServiceImpl implements NxRetailerService {
	@Autowired
	private NxRetailerDao nxRetailerDao;
	@Autowired
    private NxRetailerUserService nxRetailerUserService;
	@Autowired
	private NxRetailerGoodsShelfService nxRetailerGoodsShelfService;

	@Override
	public NxRetailerEntity queryObject(Integer nxRetailerId){
		return nxRetailerDao.queryObject(nxRetailerId);
	}

    @Override
    public Integer saveNewRestailer(NxRetailerEntity retailerEntity) {

		//1.保存餐馆
		nxRetailerDao.save(retailerEntity);

//		//2，保存用户
		Integer nxRetailerId = retailerEntity.getNxRetailerId();
		NxRetailerUserEntity nxRetailerUserEntity = retailerEntity.getNxRetailerUserEntity();
		nxRetailerUserEntity.setNxRetuRetailerId(nxRetailerId);
		nxRetailerUserEntity.setNxRetuJoinDate(formatWhatDay(0));
		nxRetailerUserService.save(nxRetailerUserEntity);
		String name = "";

//		//3，添加货架
//		for(int i = 0 ; i < 3; i++){
//			if(i == 0){
//				name = "# 一号货架";
//			}
//			if(i == 1){
//				name = "# 二号货架";
//			}
//			if(i == 2){
//				name = "# 三号货架";
//			}
//			NxRetailerGoodsShelfEntity shelfEntity = new NxRetailerGoodsShelfEntity();
//			shelfEntity.setNxRetailerGoodsShelfRetailerId(retailerEntity.getNxRetailerId());
//			shelfEntity.setNxRetailerGoodsShelfName(name);
//			shelfEntity.setNxRetailerGoodsShelfSort(i + 1);
//			nxRetailerGoodsShelfService.save(shelfEntity);
//		}

		return nxRetailerUserEntity.getNxRetailerUserId();
    }

	@Override
	public Map<String, Object> queryRetailerAndUserInfo(Integer restailerUserId) {
		return nxRetailerUserService.queryRetailerAndUserInfo(restailerUserId);
	}

}
