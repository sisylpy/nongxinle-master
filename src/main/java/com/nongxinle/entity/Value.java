/**
 * com.nongxinle.entity class
 *
 * @Author: peiyi li
 * @Date: 2020-03-11 16:34
 */

package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;

/**
 *@author lpy
 *@date 2020-03-11 16:34
 */

@Setter@Getter
public class Value {

    private String value;

    private Float amount;

    private Integer id;

    public Value(String value) {
        this.value = value;

    };


//    public Value(Float amount){
//        this.amount = amount;
//    }
//
//    private Value(Integer id) {
//        this.id = id;
//    }
}
