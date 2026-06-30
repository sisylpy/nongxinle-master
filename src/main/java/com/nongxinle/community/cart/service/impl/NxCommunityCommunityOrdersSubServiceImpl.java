package com.nongxinle.community.cart.service.impl;

import com.nongxinle.dto.coupon.CartLineSnapshot;
import com.nongxinle.dto.coupon.CartSnapshot;
import com.nongxinle.entity.NxCommunityOrdersEntity;
import com.nongxinle.entity.NxCommunityOrdersSubEntity;
import com.nongxinle.entity.NxCommunityPrintOrdersSubEntity;
import com.nongxinle.community.cart.service.NxCommunityOrdersSubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunityOrdersSubDao;


@Service("nxOrdersSubService")
public class NxCommunityCommunityOrdersSubServiceImpl implements NxCommunityOrdersSubService {
	@Autowired
	private NxCommunityOrdersSubDao nxCommunityOrdersSubDao;
	
	@Override
	public NxCommunityOrdersSubEntity queryObject(Integer nxOrdersSubId){
		return nxCommunityOrdersSubDao.queryObject(nxOrdersSubId);
	}
	
	@Override
	public List<NxCommunityOrdersSubEntity> queryList(Map<String, Object> map){
		return nxCommunityOrdersSubDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunityOrdersSubDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCommunityOrdersSubEntity nxOrdersSub){
		nxCommunityOrdersSubDao.save(nxOrdersSub);
	}
	
	@Override
	public void update(NxCommunityOrdersSubEntity nxOrdersSub){
		nxCommunityOrdersSubDao.update(nxOrdersSub);
	}
	
	@Override
	public void delete(Integer nxOrdersSubId){
		nxCommunityOrdersSubDao.delete(nxOrdersSubId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxOrdersSubIds){
		nxCommunityOrdersSubDao.deleteBatch(nxOrdersSubIds);
	}



	@Override
	public List<NxCommunityOrdersSubEntity> querySubOrdersByCustomerUserId(Map<String, Object> map) {
		return nxCommunityOrdersSubDao.querySubOrdersByCustomerUserId(map);
	}

    @Override
    public List<NxCommunityOrdersEntity> queryOutGoodsByType(Map<String, Object> map) {
        return nxCommunityOrdersSubDao.queryOutGoodsByType(map);
    }

	@Override
	public List<NxCommunityOrdersSubEntity> querySubOrdersByParams(Map<String, Object> map) {
		return nxCommunityOrdersSubDao.querySubOrdersByParams(map);
	}

    @Override
    public List<NxCommunityPrintOrdersSubEntity> queryPrintSubOrders(Map<String, Object> map) {

		return nxCommunityOrdersSubDao.queryPrintSubOrders(map);
    }

    @Override
    public NxCommunityOrdersSubEntity queryChangeSubOrderByParams(Map<String, Object> map) {

		return nxCommunityOrdersSubDao.queryChangeSubOrderByParams(map);
    }

    @Override
    public int querySubOrderTotalHuaxianQuantity(Map<String, Object> mapT) {

		return nxCommunityOrdersSubDao.querySubOrderTotalHuaxianQuantity(mapT);
    }

	@Override
	public int querySubOrderCount(Map<String, Object> mapT) {
		return nxCommunityOrdersSubDao.querySubOrderCount(mapT);
	}

    @Override
    public double queryHuaxianTotal(Map<String, Object> map) {

		return nxCommunityOrdersSubDao.queryHuaxianTotal(map);
    }

    @Override
    public int queryTodayHuaxianCount(Map<String, Object> mapOrderQuantity) {

		return nxCommunityOrdersSubDao.queryTodayHuaxianCount(mapOrderQuantity);

    }

    @Override
    public CartSnapshot buildCartSnapshot(Integer communityId, Integer userId) {
        return buildCartSnapshot(communityId, userId, 0, null, -1);
    }

    @Override
    public CartSnapshot buildCartSnapshot(Integer communityId, Integer userId,
                                          Integer orderType, Integer serviceType, Integer spId) {
        CartSnapshot snapshot = new CartSnapshot();
        snapshot.setCommunityId(communityId);
        snapshot.setUserId(userId);
        snapshot.setOrderType(orderType == null ? 0 : orderType);
        snapshot.setServiceType(serviceType);
        snapshot.setSpId(spId == null ? -1 : spId);
        snapshot.setLines(loadDraftCartLines(communityId, userId, snapshot.getOrderType(),
                serviceType, snapshot.getSpId()));
        return snapshot;
    }

    @Override
    public CartSnapshot buildCartSnapshotFromOrder(Integer communityId, Integer customerUserId, Integer orderId) {
        CartSnapshot snapshot = new CartSnapshot();
        snapshot.setCommunityId(communityId);
        snapshot.setUserId(customerUserId);
        snapshot.setLines(loadOrderCartLines(communityId, orderId));
        return snapshot;
    }

    private List<CartLineSnapshot> loadOrderCartLines(Integer communityId, Integer orderId) {
        if (communityId == null || orderId == null) {
            return new ArrayList<>();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", orderId);
        List<NxCommunityOrdersSubEntity> subs = nxCommunityOrdersSubDao.querySubOrdersByParams(map);
        List<CartLineSnapshot> lines = new ArrayList<>();
        if (subs == null) {
            return lines;
        }
        for (NxCommunityOrdersSubEntity sub : subs) {
            if (!communityId.equals(sub.getNxCosCommunityId())) {
                continue;
            }
            if (sub.getNxCosCommunityGoodsId() == null) {
                continue;
            }
            CartLineSnapshot line = toCartLine(sub);
            if (line != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private List<CartLineSnapshot> loadDraftCartLines(Integer communityId, Integer userId,
                                                      Integer orderType, Integer serviceType,
                                                      Integer spId) {
        if (communityId == null || userId == null || userId <= 0) {
            return new ArrayList<>();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("orderUserId", userId);
        map.put("status", -1);
        map.put("orderId", -1);
        map.put("orderType", orderType);
        map.put("splicingOrderId", spId);
        if (serviceType != null) {
            map.put("serviceType", serviceType);
        }
        List<NxCommunityOrdersSubEntity> subs = nxCommunityOrdersSubDao.querySubOrdersByParams(map);
        List<CartLineSnapshot> lines = new ArrayList<>();
        if (subs == null) {
            return lines;
        }
        for (NxCommunityOrdersSubEntity sub : subs) {
            if (!communityId.equals(sub.getNxCosCommunityId())) {
                continue;
            }
            if (!isRealGoodsLine(sub)) {
                continue;
            }
            CartLineSnapshot line = toCartLine(sub);
            if (line != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private boolean isRealGoodsLine(NxCommunityOrdersSubEntity sub) {
        if (sub.getNxCosCommunityGoodsId() == null) {
            return false;
        }
        Integer ordersId = sub.getNxCosOrdersId();
        if (ordersId != null && ordersId > 0) {
            return false;
        }
        return true;
    }

    private CartLineSnapshot toCartLine(NxCommunityOrdersSubEntity sub) {
        if (sub.getNxCosSubtotal() == null || sub.getNxCosSubtotal().trim().isEmpty()) {
            return null;
        }
        BigDecimal subtotal = new BigDecimal(sub.getNxCosSubtotal());
        if (subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        CartLineSnapshot line = new CartLineSnapshot();
        line.setGoodsId(sub.getNxCosCommunityGoodsId());
        line.setCategoryId(sub.getNxCosCommunityGoodsFatherId());
        if (sub.getNxCosPrice() != null && !sub.getNxCosPrice().isEmpty()) {
            line.setPrice(new BigDecimal(sub.getNxCosPrice()));
        }
        if (sub.getNxCosQuantity() != null && !sub.getNxCosQuantity().isEmpty()) {
            line.setQuantity(Integer.parseInt(sub.getNxCosQuantity()));
        }
        line.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        return line;
    }


}
