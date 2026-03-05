package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 配送商屏蔽实体：配送商 A 屏蔽配送商 B 后，A 查询商品时不会看到 B 的商品
 *
 * @author lpy
 */
@Setter
@Getter
@ToString
public class NxDistributerBlockEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Integer nxDistributerBlockId;
    /**
     * 屏蔽者配送商id（谁不想看）
     */
    private Integer blockerNxDistributerId;
    /**
     * 被屏蔽的配送商id（谁的商品被屏蔽）
     */
    private Integer blockedNxDistributerId;
}
