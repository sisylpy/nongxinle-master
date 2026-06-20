package com.nongxinle.dto.platform.distributer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class PlatformDistributerOrderLinesRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer disId;
    /** GB 平台客户：-1 */
    private Integer depFatherId;
    /** GB 平台客户：routeGbDepFatherId；NX 平台：-1 */
    private Integer gbDepFatherId;
    private Integer resFatherId;
}
