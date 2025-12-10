package com.nongxinle.dto;

import lombok.Data;
import java.util.List;

/**
 * ADP 解析响应
 */
@Data
public class AdpParseResponse {
    private String searchStr;                      // 主关键词
    private List<String> searchStrCandidates;      // 候选关键词数组
}

