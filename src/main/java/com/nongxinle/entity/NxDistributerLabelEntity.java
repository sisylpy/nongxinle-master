package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 配送商私有标签
 *
 * @author lpy
 * @date 2026-04-16
 */
@Setter
@Getter
@ToString
public class NxDistributerLabelEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxDistributerLabelId;
    private Integer nxDlDistributerId;
    private String nxDlName;
    private Integer nxDlSort;
    private Integer nxDlStatus;
}
