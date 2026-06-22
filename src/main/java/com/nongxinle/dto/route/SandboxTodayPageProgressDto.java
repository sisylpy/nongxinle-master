package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

/** 今日派车页头部进度（正式 pageViewModel 合同）。 */
@Getter
@Setter
public class SandboxTodayPageProgressDto {
    /** 完整进度文案，如「已确认 0/1 站 · 全部确认后司机可出发」 */
    private String mainLine;
    /** 需高亮片段，如「0/1」 */
    private String highlightText;
}
