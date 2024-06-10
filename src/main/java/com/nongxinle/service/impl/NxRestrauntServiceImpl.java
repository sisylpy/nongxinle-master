package com.nongxinle.service.impl;

import com.nongxinle.dao.NxRestrauntUserDao;
import com.nongxinle.entity.*;
import com.nongxinle.service.NxCommunityRestrauantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxRestrauntDao;
import com.nongxinle.service.NxRestrauntService;

import static com.nongxinle.utils.DateUtils.formatWhatDay;


@Service("nxRestrauntService")
public class NxRestrauntServiceImpl implements NxRestrauntService {
    @Autowired
    private NxRestrauntDao nxRestrauntDao;
    @Autowired
    private NxRestrauntUserDao nxRestrauntUserDao;
    @Autowired
    private NxCommunityRestrauantService nxCommunityRestrauantService;


    @Override
    public NxRestrauntEntity queryObject(Integer nxRestrauntId) {
        return nxRestrauntDao.queryObject(nxRestrauntId);
    }

    @Override
    public List<NxRestrauntEntity> queryList(Map<String, Object> map) {
        return nxRestrauntDao.queryList(map);
    }

    @Override
    public int queryTotal(Map<String, Object> map) {
        return nxRestrauntDao.queryTotal(map);
    }

    @Override
    public void save(NxRestrauntEntity nxRestraunt) {
        nxRestrauntDao.save(nxRestraunt);
    }

    @Override
    public void update(NxRestrauntEntity nxRestraunt) {
        nxRestrauntDao.update(nxRestraunt);
    }

    @Override
    public void delete(Integer nxRestrauntId) {
        nxRestrauntDao.delete(nxRestrauntId);
    }

    @Override
    public void deleteBatch(Integer[] nxRestrauntIds) {
        nxRestrauntDao.deleteBatch(nxRestrauntIds);
    }

    @Override
    public Integer saveNewRestraunt(NxRestrauntEntity res) {
        //1.保存餐馆
        nxRestrauntDao.save(res);

        String nxRestrauntIdStr = res.getNxRestrauntId().toString();
        int strLen = nxRestrauntIdStr.length();
        if (strLen < 4) {
            while (strLen < 4) {
                StringBuffer sb = new StringBuffer();
                sb.append("0").append(nxRestrauntIdStr);// 左补0
				nxRestrauntIdStr = sb.toString();
                strLen = nxRestrauntIdStr.length();
            }
        }
        res.setNxRestrauntClickCount(0);
        res.setNxRestrauntAddCount(0);
        res.setNxRestrauntNumber(nxRestrauntIdStr);
        res.setNxRestrauntPayTotal("0");
        res.setNxRestrauntProfitTotal("0");
        res.setNxRestrauntProfitPercent("0");
        nxRestrauntDao.update(res);


//		//2，保存用户
		Integer nxRestrauntId = res.getNxRestrauntId();
		NxRestrauntUserEntity nxRestrauntUserEntity = res.getNxRestrauntUserEntity();
        nxRestrauntUserEntity.setNxRuRestaurantId(nxRestrauntId);
        nxRestrauntUserEntity.setNxRuRestaurantFatherId(nxRestrauntId);
        nxRestrauntUserEntity.setNxRuJoinDate(formatWhatDay(0));
        nxRestrauntUserEntity.setNxRuComId(res.getNxRestrauntComId());
        nxRestrauntUserDao.save(nxRestrauntUserEntity);

        if (res.getNxRestrauntEntities().size() > 0) {
            //3,保存部门
            List<NxRestrauntEntity> nxRestrauntEntities = res.getNxRestrauntEntities();
            for (NxRestrauntEntity subRes : nxRestrauntEntities) {
                subRes.setNxRestrauntFatherId(nxRestrauntId);
                subRes.setNxRestrauntSettleType(res.getNxRestrauntSettleType());
                subRes.setNxRestrauntType(res.getNxRestrauntType());
                subRes.setNxRestrauntServiceLevel(res.getNxRestrauntServiceLevel());
                subRes.setNxRestrauntSettleType(res.getNxRestrauntSettleType());
                subRes.setNxRestrauntComId(res.getNxRestrauntComId());
                nxRestrauntDao.save(subRes);

            }
        }

        //3, 保存订货群的批发商
        Integer nxRestrauntComId = res.getNxRestrauntComId();
        NxCommunityRestrauantEntity entity = new NxCommunityRestrauantEntity();
        entity.setNxCrCommunityId(nxRestrauntComId);
        entity.setNxCrRestaruantId(nxRestrauntId);
        nxCommunityRestrauantService.save(entity);

        return nxRestrauntUserEntity.getNxRestrauntUserId();
    }

    @Override
    public Map<String, Object> queryResAndUserInfo(Integer resUserId) {

        //订货群信息
        NxRestrauntUserEntity nxRestrauntUserEntity = nxRestrauntUserDao.queryObject(resUserId);
        //用户信息
        Integer restaurantId = nxRestrauntUserEntity.getNxRuRestaurantId();
        NxRestrauntEntity nxRestrauntEntity = nxRestrauntDao.queryResInfo(restaurantId);

        //返回
        Map<String, Object> map = new HashMap<>();
        map.put("userInfo", nxRestrauntUserEntity);
        map.put("resInfo", nxRestrauntEntity);
        return map;
    }

    @Override
    public List<NxRestrauntEntity> queryPrepareDeliveryRestraunts(Integer comId) {

        return nxRestrauntDao.queryPrepareDeliveryRestraunts(comId);
    }

    @Override
    public List<NxRestrauntEntity> queryDriverRestraunts(Integer userId) {

        return nxRestrauntDao.queryDriverRestraunts(userId);
    }

    @Override
    public NxRestrauntEntity queryResInfo(Integer valueOf) {

        return nxRestrauntDao.queryResInfo(valueOf);
    }

    @Override
    public Integer saveNewChainRestraunt(NxRestrauntEntity res) {
        //1.保存餐馆
        nxRestrauntDao.save(res);

        String nxRestrauntIdStr = res.getNxRestrauntId().toString();
        int strLen = nxRestrauntIdStr.length();
        if (strLen < 4) {
            while (strLen < 4) {
                StringBuffer sb = new StringBuffer();
                sb.append("0").append(nxRestrauntIdStr);// 左补0
                nxRestrauntIdStr = sb.toString();
                strLen = nxRestrauntIdStr.length();
            }
        }
        res.setNxRestrauntNumber(nxRestrauntIdStr);
        res.setNxRestrauntPayTotal("0");
        res.setNxRestrauntProfitTotal("0");
        res.setNxRestrauntProfitPercent("0");
        nxRestrauntDao.update(res);


//		//2，保存用户
        Integer nxRestrauntId = res.getNxRestrauntId();
        NxRestrauntUserEntity nxRestrauntUserEntity = res.getNxRestrauntUserEntity();
        nxRestrauntUserEntity.setNxRuRestaurantId(nxRestrauntId);
        nxRestrauntUserEntity.setNxRuRestaurantFatherId(nxRestrauntId);
        nxRestrauntUserEntity.setNxRuJoinDate(formatWhatDay(0));
        nxRestrauntUserEntity.setNxRuComId(res.getNxRestrauntComId());

        nxRestrauntUserDao.save(nxRestrauntUserEntity);

        if (res.getNxRestrauntEntities().size() > 0) {
            //3,保存部门
            List<NxRestrauntEntity> nxRestrauntEntities = res.getNxRestrauntEntities();
            for (NxRestrauntEntity subRes : nxRestrauntEntities) {
                subRes.setNxRestrauntFatherId(nxRestrauntId);
                subRes.setNxRestrauntComId(res.getNxRestrauntComId());
                nxRestrauntDao.save(subRes);
                //
                String nxsubRestrauntIdStr = subRes.getNxRestrauntId().toString();
                int strsubLen = nxsubRestrauntIdStr.length();
                if (strsubLen < 4) {
                    while (strsubLen < 4) {
                        StringBuffer sbs = new StringBuffer();
                        sbs.append("0").append(nxsubRestrauntIdStr);// 左补0
                        nxsubRestrauntIdStr = sbs.toString();
                        strsubLen = nxsubRestrauntIdStr.length();
                    }
                }
                subRes.setNxRestrauntNumber(nxsubRestrauntIdStr);
                subRes.setNxRestrauntServiceLevel(res.getNxRestrauntServiceLevel());
                subRes.setNxRestrauntAttrName(subRes.getNxRestrauntName());
                subRes.setNxRestrauntSettleType(res.getNxRestrauntSettleType());
                subRes.setNxRestrauntWorkingStatus(0);
                subRes.setNxRestrauntOweBoxNumber(0);
                subRes.setNxRestrauntDeliveryBoxNumber(0);
                subRes.setNxRestrauntDeliveryCost("15");
                subRes.setNxRestrauntDeliveryLimit("200");
                subRes.setNxRestrauntMinTime("8:00");
                subRes.setNxRestrauntMaxTime("10:00");
                subRes.setNxRestrauntClickCount(0);
                subRes.setNxRestrauntAddCount(0);
                subRes.setNxRestrauntNumber(nxRestrauntIdStr);
                subRes.setNxRestrauntPayTotal("0");
                subRes.setNxRestrauntProfitTotal("0");
                subRes.setNxRestrauntProfitPercent("0");
                nxRestrauntDao.update(subRes);

                if(subRes.getNxRestrauntEntities().size() > 0){
                    List<NxRestrauntEntity> nxRestrauntEntitiessub = subRes.getNxRestrauntEntities();
                    for(NxRestrauntEntity sss: nxRestrauntEntitiessub){
                        sss.setNxRestrauntFatherId(subRes.getNxRestrauntId());
                        sss.setNxRestrauntComId(subRes.getNxRestrauntComId());
                        sss.setNxRestrauntServiceLevel(subRes.getNxRestrauntServiceLevel());
                        sss.setNxRestrauntClickCount(0);
                        sss.setNxRestrauntAddCount(0);
                        nxRestrauntDao.save(sss);
                    }

                }
            }
        }

        //3, 保存订货群的批发商
        Integer nxRestrauntComId = res.getNxRestrauntComId();
        NxCommunityRestrauantEntity entity = new NxCommunityRestrauantEntity();
        entity.setNxCrCommunityId(nxRestrauntComId);
        entity.setNxCrRestaruantId(nxRestrauntId);
        nxCommunityRestrauantService.save(entity);

        return nxRestrauntUserEntity.getNxRestrauntUserId();
    }

    @Override
    public List<NxRestrauntEntity> queryChainRestrauntsByResId(Integer resId) {
        return nxRestrauntDao.queryChainRestrauntsByResId(resId);
    }


}
