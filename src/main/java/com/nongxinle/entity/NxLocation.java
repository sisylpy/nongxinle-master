package com.nongxinle.entity;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class NxLocation {

        private Double lng;  // 经度
        private Double lat;  // 纬度

        // 构造函数
        public NxLocation(Double lng, Double lat) {
            this.lng = lng;
            this.lat = lat;
        }



}
