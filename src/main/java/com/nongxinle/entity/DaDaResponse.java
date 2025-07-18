package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.poi.ss.formula.functions.T;

@Setter
@Getter
@ToString

public class DaDaResponse<T>  {

    private String status;
    private T result;
    private Integer code;
    private String msg;
}
