package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class DispatchWorkbenchActionDto {
    private String action;
    private String label;
    private Boolean enabled;
    private String hint;
}
