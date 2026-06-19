package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@ToString
public class PlatformDistributerIdSchemaProbeResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 是否允许 NULL；探测失败时为 null */
    private Boolean nullable;
    /** 实际匹配到的列名 */
    private String columnName;
    /** 表是否存在 */
    private Boolean tableExists;
    /** 当前连接 catalog / database */
    private String catalog;
    /** 探测来源：config_override | information_schema | show_columns | database_metadata | cached */
    private String probeSource;
    private String note;
    /** 探测失败时的可读原因 */
    private String error;
    /** 底层异常类名 + message（便于对照后端日志） */
    private String errorCause;
}
