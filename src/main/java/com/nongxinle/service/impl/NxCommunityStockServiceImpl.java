package com.nongxinle.service.impl;

import com.nongxinle.dao.NxCommunityStockSubDao;
import com.nongxinle.entity.NxCommunityGoodsEntity;
import com.nongxinle.entity.NxCommunityStockSubEntity;
import com.nongxinle.service.NxCommunityGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunityStockDao;
import com.nongxinle.entity.NxCommunityStockEntity;
import com.nongxinle.service.NxCommunityStockService;

import static com.nongxinle.utils.DateUtils.formatWhatDay;


@Service("nxCommunityStockService")
public class NxCommunityStockServiceImpl implements NxCommunityStockService {
	@Autowired
	private NxCommunityStockDao nxCommunityStockDao;

	@Autowired
	private NxCommunityStockSubDao nxCommunityStockSubDao;

	@Autowired
	private NxCommunityGoodsService nxCommunityGoodsService;
	
	@Override
	public NxCommunityStockEntity queryObject(Integer nxCommunityStockId){
		return nxCommunityStockDao.queryObject(nxCommunityStockId);
	}
	
	@Override
	public List<NxCommunityStockEntity> queryList(Map<String, Object> map){
		return nxCommunityStockDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunityStockDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCommunityStockEntity nxCommunityStock){

		//1.保存库存数据，获取库存id
		nxCommunityStock.setNxStockRequierDate(formatWhatDay(0));
		nxCommunityStock.setNxStockStatus(0);
		nxCommunityStockDao.save(nxCommunityStock);

		Integer nxCommunityStockId = nxCommunityStock.getNxCommunityStockId();

		List<NxCommunityStockSubEntity> entityList = nxCommunityStock.getEntityList();
		for (NxCommunityStockSubEntity sub : entityList) {

			//2，更新communityGoods总库存数量
			NxCommunityGoodsEntity communityGoodsEntity = nxCommunityGoodsService.queryObject(sub.getNxCssCgId());
			System.out.println(sub.getNxCssCgId() + "ididididiididiid") ;
			String nxCgGoodsStock = communityGoodsEntity.getNxCgGoodsStock();
			System.out.println(communityGoodsEntity);
			System.out.println("communityGoodsEntity");
			System.out.println(nxCgGoodsStock + "nxCgGoodsStocknxCgGoodsStocknxCgGoodsStock");
			if(nxCgGoodsStock != null){
				Integer integer = Integer.valueOf(nxCgGoodsStock);
				String nxCssEntryAmount = sub.getNxCssEntryAmount();
				System.out.println(integer + "integer======");
				if(nxCssEntryAmount != null){
					Integer integer1 = Integer.valueOf(nxCssEntryAmount);
					System.out.println(integer1 + "integer111111");
					 Integer t = integer + integer1;
					String s = String.valueOf(t);
					System.out.println(s);
					communityGoodsEntity.setNxCgGoodsStock(s);
					 nxCommunityGoodsService.update(communityGoodsEntity);
				}
			}

			//3,保存子库存
			sub.setNxCssEntryDate(formatWhatDay(0));
			sub.setNxCssEntryAmount(sub.getNxCssEntryAmount());
			sub.setNxCssStockAmount(sub.getNxCssEntryAmount());
			sub.setNxCsId(nxCommunityStockId);
			nxCommunityStockSubDao.save(sub);
		}

	}
	
	@Override
	public void update(NxCommunityStockEntity nxCommunityStock){
		nxCommunityStockDao.update(nxCommunityStock);
	}
	
	@Override
	public void delete(Integer nxCommunityStockId){
		nxCommunityStockDao.delete(nxCommunityStockId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxCommunityStockIds){
		nxCommunityStockDao.deleteBatch(nxCommunityStockIds);
	}
	
}
