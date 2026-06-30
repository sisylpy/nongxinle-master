package com.nongxinle.community.marketing.ai.taildeal.session;

import com.nongxinle.community.marketing.ai.taildeal.draft.TailDealAdsenseDraft;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TailDealAdsenseDraftSessionStore {

    private static final Logger log = LoggerFactory.getLogger(TailDealAdsenseDraftSessionStore.class);

    private static final long TTL_MS = 30L * 60L * 1000L;

    private final Map<String, TailDealAdsenseDraft> store = new ConcurrentHashMap<>();

    public String save(TailDealAdsenseDraft draft) {
        if (draft.getDraftId() == null) {
            draft.setDraftId("draft_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8));
        }
        draft.setCreatedAtMs(System.currentTimeMillis());
        draft.setExpiresAtMs(draft.getCreatedAtMs() + TTL_MS);
        store.put(draft.getDraftId(), draft);
        log.info("[TailDealAI] draftSaved draftId={}, flowState={}, expireAt={}",
                draft.getDraftId(), draft.getFlowState(), draft.getExpiresAtMs());
        return draft.getDraftId();
    }

    public TailDealAdsenseDraft get(String draftId) {
        TailDealAdsenseDraft draft = store.get(draftId);
        if (draft == null) {
            log.warn("[TailDealAI] draftGetMiss draftId={}", draftId);
            return null;
        }
        if (System.currentTimeMillis() > draft.getExpiresAtMs()) {
            store.remove(draftId);
            log.warn("[TailDealAI] draftExpired draftId={}", draftId);
            return null;
        }
        log.info("[TailDealAI] draftGetHit draftId={}, flowState={}", draftId, draft.getFlowState());
        return draft;
    }

    public void markPublished(TailDealAdsenseDraft draft, Integer goodsId, Integer adsenseId) {
        draft.setPublished(true);
        draft.setPublishedGoodsId(goodsId);
        draft.setPublishedAdsenseId(adsenseId);
        store.put(draft.getDraftId(), draft);
        log.info("[TailDealAI] draftMarkedPublished draftId={}, goodsId={}, adsenseId={}",
                draft.getDraftId(), goodsId, adsenseId);
    }
}
