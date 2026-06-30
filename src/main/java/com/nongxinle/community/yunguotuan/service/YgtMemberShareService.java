package com.nongxinle.community.yunguotuan.service;

import java.util.List;
import java.util.Map;

public interface YgtMemberShareService {
    Map<String, Object> createShare(Map<String, Object> body);

    Map<String, Object> landing(String shareCode);

    Map<String, Object> registerFromShare(Map<String, Object> body);

    Map<String, Object> registrations(Map<String, Object> params);

    Map<String, Object> groupMemberOverview(Map<String, Object> params);

    Map<String, Object> confirmGroupIdentity(Map<String, Object> body);
}
