package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

@Setter
@Getter
@ToString
public class DailyUsage implements Serializable {

    private String date;   // 直接映射成 "2025-05-29" 这样的字符串
    private double qty;
    private Integer gid;   // 商品ID

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public double getQty() { return qty; }
    public void setQty(double qty) { this.qty = qty; }

    public Integer getGid() { return gid; }
    public void setGid(Integer gid) { this.gid = gid; }
}
