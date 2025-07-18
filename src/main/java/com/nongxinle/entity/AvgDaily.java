package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;


@Setter
@Getter
@ToString
public class AvgDaily  implements Serializable {

    private Integer goodsId;
    private Double avgDailyQty;
}
