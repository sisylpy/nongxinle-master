package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Phase 3D：老板确认司机整车出发 */
@Setter
@Getter
@ToString
public class DriverRouteDepartRequest {
    private Integer operatorUserId;
    private Integer disId;
    private Integer planId;
    private String routeDate;
    private String batchCode;
    private Integer driverUserId;
    /** yyyy-MM-dd HH:mm:ss，空则取服务器当前时间 */
    private String departAt;
    private String remark;
}
