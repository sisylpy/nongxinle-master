package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SandboxDriverRouteEditPreviewRequest extends SandboxDriverRouteEditBaseRequest {
    private List<String> stopKeys = new ArrayList<String>();
}
