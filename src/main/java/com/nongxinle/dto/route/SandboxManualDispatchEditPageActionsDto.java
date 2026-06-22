package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 编辑页后续动作路径（本阶段不做 confirm 落库）。 */
@Setter
@Getter
@ToString
public class SandboxManualDispatchEditPageActionsDto {
    private String editPagePath;
    private String simulatePath;
    private String confirmPath;
    private Boolean confirmEnabled;
}
