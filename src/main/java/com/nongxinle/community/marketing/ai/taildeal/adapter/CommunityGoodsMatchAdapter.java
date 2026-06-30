package com.nongxinle.community.marketing.ai.taildeal.adapter;

import com.nongxinle.community.catalog.service.NxCommunityGoodsService;
import com.nongxinle.entity.NxCommunityGoodsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.utils.PinYin4jUtils.hanziToPinyin;

@Component
public class CommunityGoodsMatchAdapter {

    private static final Logger log = LoggerFactory.getLogger(CommunityGoodsMatchAdapter.class);

    @Autowired
    private NxCommunityGoodsService nxCommunityGoodsService;

    public List<Map<String, Object>> matchGoods(String goodsName, Integer communityId) {
        if (goodsName == null || goodsName.trim().isEmpty() || communityId == null) {
            return new ArrayList<>();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("commId", communityId.toString());
        map.put("searchStr", goodsName.trim());
        map.put("searchStrPinyin", hanziToPinyin(goodsName.trim()));
        List<NxCommunityGoodsEntity> list = nxCommunityGoodsService.queryComGoodsQuickSearchStr(map);
        List<Map<String, Object>> candidates = new ArrayList<>();
        for (NxCommunityGoodsEntity g : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("goodsId", g.getNxCommunityGoodsId());
            item.put("goodsName", g.getNxCgGoodsName());
            item.put("confidence", scoreConfidence(goodsName, g.getNxCgGoodsName()));
            candidates.add(item);
        }
        candidates.sort((a, b) -> Double.compare(
                (Double) b.get("confidence"), (Double) a.get("confidence")));
        log.info("[TailDealAI] goodsMatch communityId={}, query={}, hitCount={}",
                communityId, goodsName, candidates.size());
        return candidates;
    }

    private double scoreConfidence(String query, String candidateName) {
        if (candidateName == null) {
            return 0.5;
        }
        String q = query.trim();
        String c = candidateName.trim();
        if (q.equals(c)) {
            return 0.98;
        }
        if (c.contains(q) || q.contains(c)) {
            return 0.82;
        }
        return 0.68;
    }
}
