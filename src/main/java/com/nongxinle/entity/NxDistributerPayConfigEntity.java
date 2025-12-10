package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 商家支付配置实体类
 * @author lpy
 * @date 2024-01-01
 */
@Setter
@Getter
@ToString
public class NxDistributerPayConfigEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 配置ID
     */
    private Integer nxDpcId;
    
    /**
     * 商家ID (nxDisId)
     */
    private Integer nxDpcNxDisId;
    
    /**
     * 微信AppID
     */
    private String nxDpcWxAppId;
    
    /**
     * 微信商户号
     */
    private String nxDpcWxMchId;
    
    /**
     * 微信支付密钥
     */
    private String nxDpcWxKey;
    
    /**
     * 配置状态 (1:启用, 0:禁用)
     */
    private Integer nxDpcStatus;
    
    /**
     * 创建时间
     */
    private String nxDpcCreateTime;
    
    /**
     * 更新时间
     */
    private String nxDpcUpdateTime;
    
    /**
     * 备注
     */
    private String nxDpcRemark;
} 