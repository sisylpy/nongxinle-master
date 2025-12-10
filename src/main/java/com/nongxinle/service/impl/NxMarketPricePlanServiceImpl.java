package com.nongxinle.service.impl;

import com.nongxinle.dao.NxMarketPricePlanDao;
import com.nongxinle.entity.NxMarketPricePlanEntity;
import com.nongxinle.service.NxMarketPricePlanService;
import com.nongxinle.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 市场价格方案Service实现
 * @author lpy
 * @date 2025-01-09
 */
@Service("nxMarketPricePlanService")
public class NxMarketPricePlanServiceImpl implements NxMarketPricePlanService {

    @Autowired
    private NxMarketPricePlanDao nxMarketPricePlanDao;

    @Override
    public List<NxMarketPricePlanEntity> queryByMarketAndType(Integer marketId, Integer type) {
        return nxMarketPricePlanDao.queryByMarketAndType(marketId, type);
    }

    @Override
    public List<NxMarketPricePlanEntity> queryList(Map<String, Object> map) {
        return nxMarketPricePlanDao.queryList(map);
    }

    @Override
    public int queryTotal(Map<String, Object> map) {
        return nxMarketPricePlanDao.queryTotal(map);
    }

    @Override
    public NxMarketPricePlanEntity queryObject(Integer id) {
        return nxMarketPricePlanDao.queryObject(id);
    }

    @Override
    public void save(NxMarketPricePlanEntity entity) {
        nxMarketPricePlanDao.save(entity);
    }

    @Override
    public void update(NxMarketPricePlanEntity entity) {
        nxMarketPricePlanDao.update(entity);
    }

    @Override
    public void delete(Integer id) {
        nxMarketPricePlanDao.delete(id);
    }

    @Override
    public void deleteBatch(Integer[] ids) {
        nxMarketPricePlanDao.deleteBatch(ids);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 获取分页参数
        String page = (String) params.get("page");
        String limit = (String) params.get("limit");
        
        // 设置默认值
        int currPage = 1;
        int pageSize = 10;
        
        if (page != null) {
            currPage = Integer.parseInt(page);
        }
        if (limit != null) {
            pageSize = Integer.parseInt(limit);
        }
        
        // 计算偏移量
        int offset = (currPage - 1) * pageSize;
        params.put("offset", offset);
        params.put("limit", pageSize);
        
        // 查询列表
        List<NxMarketPricePlanEntity> list = nxMarketPricePlanDao.queryList(params);
        
        // 查询总数
        int total = nxMarketPricePlanDao.queryTotal(params);
        
        // 创建分页对象
        PageUtils pageUtil = new PageUtils((List<?>) list, total, pageSize, currPage);
        
        return pageUtil;
    }
}