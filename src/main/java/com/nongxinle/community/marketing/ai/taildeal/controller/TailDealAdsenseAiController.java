package com.nongxinle.community.marketing.ai.taildeal.controller;

import com.alibaba.fastjson.JSON;
import com.nongxinle.community.marketing.ai.taildeal.service.TailDealAdsenseParseService;
import com.nongxinle.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/nxadsense/ai")
public class TailDealAdsenseAiController {

    private static final Logger log = LoggerFactory.getLogger(TailDealAdsenseAiController.class);

    @Autowired
    private TailDealAdsenseParseService parseService;

    @RequestMapping(value = "/parseTailDealDraft", method = RequestMethod.POST)
    @ResponseBody
    public R parseTailDealDraft(@RequestBody TailDealAdsenseParseService.ParseRequest request) {
        log.info("[TailDealAI] parseTailDealDraft req communityId={}, commerceId={}, rawText={}",
                request.getCommunityId(), request.getCommerceId(), request.getRawText());
        try {
            Map<String, Object> data = parseService.parseTailDealDraft(request);
            R response = R.ok().put("data", data);
            log.info("[TailDealAI] parseTailDealDraft ok draftId={}, flowState={}, responseBytes={}",
                    data.get("draftId"), data.get("flowState"), JSON.toJSONString(response).length());
            return response;
        } catch (IllegalArgumentException e) {
            log.warn("[TailDealAI] parseTailDealDraft badRequest: {}", e.getMessage());
            return R.error(-1, e.getMessage());
        } catch (Exception e) {
            log.error("[TailDealAI] parseTailDealDraft failed", e);
            return R.error(-1, "解析失败: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/confirmTailDeal", method = RequestMethod.POST)
    @ResponseBody
    public R confirmTailDeal(@RequestBody TailDealAdsenseParseService.ConfirmRequest request) {
        log.info("[TailDealAI] confirmTailDeal req draftId={}, action={}, selectedGoodsId={}",
                request.getDraftId(), request.getConfirmAction(), request.getSelectedGoodsId());
        try {
            Map<String, Object> data = parseService.confirmTailDeal(request);
            log.info("[TailDealAI] confirmTailDeal ok goodsId={}, adsenseId={}, nxCaStatus={}, status={}",
                    data.get("goodsId"), data.get("adsenseId"), data.get("nxCaStatus"), data.get("status"));
            return R.ok().put("data", data);
        } catch (TailDealAdsenseParseService.ConfirmRejectedException e) {
            log.warn("[TailDealAI] confirmTailDeal rejected: {}", e.getMessage());
            return R.error(-1, e.getMessage()).put("data", e.getData());
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("[TailDealAI] confirmTailDeal invalid: {}", e.getMessage());
            return R.error(-1, e.getMessage());
        } catch (Exception e) {
            log.error("[TailDealAI] confirmTailDeal failed draftId={}", request.getDraftId(), e);
            return R.error(-1, "确认失败: " + e.getMessage());
        }
    }
}
