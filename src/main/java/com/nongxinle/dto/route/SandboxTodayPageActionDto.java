package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class SandboxTodayPageActionDto {
    private String actionType;
    private String label;
    private Boolean enabled;
    private String disabledReason;
    private Map<String, Object> payload;
}
