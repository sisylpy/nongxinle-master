package com.nongxinle.community.marketing.ai.taildeal.gate;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class TailDealAdsenseConfirmSafetyGate {

    private static final Logger log = LoggerFactory.getLogger(TailDealAdsenseConfirmSafetyGate.class);

    public static final String CONFIRM_PUBLISH = "CONFIRM_PUBLISH";
    public static final String CONFIRM_SAVE = "CONFIRM_SAVE";
    public static final String CONFIRM_SAVE_ADSENSE = "CONFIRM_SAVE_ADSENSE";

    private static final Set<String> ALLOWED_ACTIONS = new HashSet<>(Arrays.asList(
            CONFIRM_PUBLISH, CONFIRM_SAVE, CONFIRM_SAVE_ADSENSE
    ));

    private static final Set<String> ALLOWED_CONFIRM_TEXT = new HashSet<>(Arrays.asList(
            "确认发布", "确认保存", "发布这个抢购", "发布尾货广告", "发首页", "开始抢购"
    ));

    private static final Set<String> REJECTED_CONFIRM_TEXT = new HashSet<>(Arrays.asList(
            "可以", "好的", "看着不错", "行吧", "ok", "OK", "嗯", "行"
    ));

    public GateResult validate(String confirmAction, String confirmText) {
        if (confirmAction == null || !ALLOWED_ACTIONS.contains(confirmAction.trim())) {
            log.warn("[TailDealAI] gateReject invalidAction={}", confirmAction);
            return GateResult.rejected("未检测到明确确认动作，禁止发布");
        }
        if (confirmText != null) {
            String trimmed = confirmText.trim();
            if (REJECTED_CONFIRM_TEXT.contains(trimmed)) {
                log.warn("[TailDealAI] gateReject fuzzyText={}", trimmed);
                return GateResult.rejected("确认文案过于模糊，请使用「确认保存」或「确认发布」");
            }
            if (!trimmed.isEmpty() && !ALLOWED_CONFIRM_TEXT.contains(trimmed)) {
                log.warn("[TailDealAI] gateReject unknownText={}", trimmed);
                return GateResult.rejected("确认文案不在白名单，请使用明确的保存或发布按钮");
            }
        }
        String normalized = normalizeAction(confirmAction);
        log.info("[TailDealAI] gatePass action={}, normalized={}", confirmAction, normalized);
        return GateResult.ok(normalized);
    }

    public static String normalizeAction(String confirmAction) {
        if (CONFIRM_SAVE_ADSENSE.equals(confirmAction)) {
            return CONFIRM_SAVE;
        }
        return confirmAction;
    }

    public static boolean isPublishAction(String normalizedAction) {
        return CONFIRM_PUBLISH.equals(normalizedAction);
    }

    public static class GateResult {
        private final boolean allowed;
        private final String message;
        private final String normalizedAction;

        private GateResult(boolean allowed, String message, String normalizedAction) {
            this.allowed = allowed;
            this.message = message;
            this.normalizedAction = normalizedAction;
        }

        public static GateResult ok(String normalizedAction) {
            return new GateResult(true, null, normalizedAction);
        }

        public static GateResult rejected(String message) {
            return new GateResult(false, message, null);
        }

        public boolean isAllowed() {
            return allowed;
        }

        public String getMessage() {
            return message;
        }

        public String getNormalizedAction() {
            return normalizedAction;
        }
    }
}
