package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SandboxDriverRouteEditConfirmRequest extends SandboxDriverRouteEditBaseRequest {
    private List<String> stopKeys = new ArrayList<String>();
    private String confirmReason;
}
