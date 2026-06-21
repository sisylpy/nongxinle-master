package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class DispatchWorkbenchIssueDto {
    private String type;
    private String severity;
    private String title;
    private String description;
    private String suggestion;
}
