package com.nongxinle.dispatch.core.view;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/** 页面主操作按钮（确认分派、发车、送达等）。 */
@Getter
@Setter
public class DispatchPrimaryAction {

    private String actionType;
    private String label;
    private boolean enabled;
    private String disabledReason;
    private String toneClass;
    private Map<String, Object> payload = new LinkedHashMap<String, Object>();
}
