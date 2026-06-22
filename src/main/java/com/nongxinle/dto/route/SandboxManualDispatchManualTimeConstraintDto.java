package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 老板人工时间约束（与客户原时间窗、系统模拟 ETA 区分）。
 * 编辑页每个店卡下预留；未设置时字段可为 null / false。
 */
@Setter
@Getter
@ToString
public class SandboxManualDispatchManualTimeConstraintDto {
    /** 是否人工指定送达时间 */
    private Boolean manualArrivalSpecified;
    /** 必须几点前到，如 2026-06-22 15:00:00 */
    private String requiredLatestArrivalAt;
    private String requiredLatestArrivalLabel;
    /** 最好几点到（非“几点前”） */
    private String preferredArrivalAt;
    private String preferredArrivalLabel;
    /** 是否允许晚于必须送达时间 */
    private Boolean allowLate;
    /** 备注原因 */
    private String remarkReason;
    /** 一行摘要，供编辑页 / 分店卡展示 */
    private String summaryLabel;
}
