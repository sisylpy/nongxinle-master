package com.nongxinle.community.yunguotuan.service;

import java.util.List;
import java.util.Map;

public interface YgtArchiveIngestService {
    Map<String, Object> pullArchive(String corpId, Integer limit);

    List<Map<String, Object>> archiveStatus(String corpId);
}
