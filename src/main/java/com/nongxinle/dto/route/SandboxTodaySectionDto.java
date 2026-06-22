package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SandboxTodaySectionDto {
    private String sectionKey;
    private String title;
    private String description;
    private List<SandboxTodaySectionCardDto> cards = new ArrayList<SandboxTodaySectionCardDto>();
}
