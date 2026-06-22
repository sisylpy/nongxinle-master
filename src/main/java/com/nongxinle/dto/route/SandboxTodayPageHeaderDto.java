package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SandboxTodayPageHeaderDto {
    private String title;
    private String subtitle;
    private String progressLine;
    private SandboxTodayPageProgressDto progress;
    private String operationHint;
    private String statusLabel;
    private String statusTone;
    private String scheduleBannerLine;
    private String heroImageType;
    private List<SandboxTodayPageActionDto> nextActions = new ArrayList<SandboxTodayPageActionDto>();
}
