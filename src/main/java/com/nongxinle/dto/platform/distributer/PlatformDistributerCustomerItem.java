package com.nongxinle.dto.platform.distributer;

import com.nongxinle.entity.NxDepartmentEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 配送商端「平台客户」列表项（与 GB 饭店客户、配送商自有 nx 客户隔离）。
 */
@Getter
@Setter
@ToString
public class PlatformDistributerCustomerItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private NxDepartmentEntity dep;
    private Integer totalCount;
    private Integer finishCount;
    private Integer hasPrice;
    private Integer hasWeight;
    private Integer unDo;
    private String twoSubtotal;

    /** 固定 1 */
    private Integer isPlatformCustomer;
    /** PLATFORM */
    private String orderSource;
    /** 平台客户 */
    private String platformLabel;
    private Integer platformSort;

    /** 京采市场饭店 gb_department_id（展示用） */
    private Integer gbDepartmentId;
    /** 京采市场饭店名称 */
    private String gbDepartmentName;
    /** 所属批发市场 */
    private Integer marketId;

    /** GB=京采饭店平台单；NX=nx_department 平台单 */
    private String customerSource;
    /** 跳转 orderPage 时 depFatherId（NX 客户） */
    private Integer routeDepFatherId;
    /** 跳转 orderPage 时 gbDepFatherId（GB 京采客户） */
    private Integer routeGbDepFatherId;
    /** 列表展示名（优先饭店名） */
    private String displayName;
}
