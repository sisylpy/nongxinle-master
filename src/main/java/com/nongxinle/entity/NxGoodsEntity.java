package com.nongxinle.entity;

/**
 * @author lpy
 * @date 2020-02-10 19:43:11
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter
@Getter
@ToString

public class NxGoodsEntity implements Serializable, Comparable {
    private static final long serialVersionUID = 1L;

    /**
     *  商品id
     */
    private Integer nxGoodsId;
    /**
     *  商品名称
     */
    private String nxGoodsName;
    /**
     *  商品规格
     */
    private String nxGoodsStandardname;
    /**
     *  商品品牌
     */
    private String nxGoodsBrand;
    /**
     *  商品产地
     */
    private String nxGoodsPlace;
    /**
     *  拼音
     */
    private String nxGoodsPinyin;
    /**
     *  简拼
     */
    private String nxGoodsPy;
    /**
     *  父级id
     */
    private Integer nxGoodsFatherId;
    /**
     *  商品排序
     */
    private Integer nxGoodsSort;
    private Integer nxGoodsIsOldestSon;
    private Integer nxGoodsGrandId;
    private Integer nxGoodsGreatGrandId;

    /**
     * lujing
     */
    private String nxGoodsFile;

    /**
     * yanse
     */
    private String color;
    private Integer nxGoodsApplyNxDistributerId;
    private Integer nxGoodsLevel;
    private Integer nxGoodsIsHidden;

    /**
     * zigoods
     */
    private List<NxGoodsEntity> nxGoodsEntityList;
    private List<NxGoodsEntity> nxGoodsFatherEntityList;
    private List<NxGoodsEntity> nxGoodsGrandEntityList;


    private List<NxDistributerStandardEntity> nxDisStandardEntities;
    private List<NxDistributerAliasEntity> nxDistributerAliasEntities;
    private List<NxDepartmentStandardEntity> nxDepStandardEntities;
    private NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity;
    private NxDistributerGoodsEntity supplierGoods;
    private List<GbDepartmentGoodsStockEntity> gbDepartmentGoodsStockEntities;

    /**
     * 子名称
     */
    private String nxGoodsSubNames;

    /**
     * isDown
     */
    private Integer isDownload;
    private Integer nxGoodsSonsSort;

    private Integer nxDepartmentGoodsId;
    private String nxDepartmentGoodsPrice;


    private List<NxStandardEntity> nxGoodsStandardEntities;
    private List<NxAliasEntity> nxAliasEntities;

    private String nxGoodsDetail;
    private String nxGoodsFileBig;

    private NxGoodsEntity fatherGoods;

    private NxGoodsEntity grandGoods;

    private NxGoodsEntity greatGrandGoods;

    private Integer nxGoodsStandardAmount;

    //规格重量 例如 500克
    private String nxGoodsStandardWeight;

    private Integer subAmount;
    private Integer nxGoodsQuantityDays;

    /**
     * 外箱名称
     */
    private String nxGoodsCartonUnit;
    /**
     * 外箱装数量（与库 varchar 一致，如 9.5）
     */
    private String nxGoodsItemsPerCarton;

    private NxDistributerGoodsEntity nxDistributerGoodsEntity;


    private NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity;

    private Boolean isShow = false;

    private NxCommunityGoodsEntity nxCommunityGoodsEntity;
    private NxCommunityFatherGoodsEntity nxCommunityFatherGoodsEntity;
    private GbDistributerGoodsEntity gbDistributerGoodsEntity;

    private List<NxDistributerEntity> nxDistributerEntities;

    private GbDepartmentOrdersEntity gbDepartmentOrdersEntity;
    private GbDepartmentDisGoodsEntity gbDepartmentDisGoodsEntity;

    private int gbDepOrderCount = 0;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NxGoodsEntity that = (NxGoodsEntity) o;
        // 唯一标识：仅通过 ID 判断相等性
        return Objects.equals(nxGoodsId, that.nxGoodsId);
    }

    @Override
    public int hashCode() {
        // 仅哈希 ID
        return Objects.hash(nxGoodsId);
    }

    public int compareTo(Object o) {

        if (o instanceof NxGoodsEntity) {

            NxGoodsEntity e = (NxGoodsEntity) o;

            return this.nxGoodsId.compareTo(e.nxGoodsId);

        }

        return 0;

    }

}
