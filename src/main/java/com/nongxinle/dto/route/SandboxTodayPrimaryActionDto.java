package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class SandboxTodayPrimaryActionDto {
    private String actionType;
    private String action;
    private String label;
    private Boolean enabled;
    private String disabledReason;
    private Map<String, Object> payload;
}
