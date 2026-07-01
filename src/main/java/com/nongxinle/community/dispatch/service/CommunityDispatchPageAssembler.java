package com.nongxinle.community.dispatch.service;

import com.nongxinle.community.dispatch.model.CommunityDispatchSandboxResult;
import com.nongxinle.dispatch.adapter.community.CommunityDispatchPageViewAdapter;
import com.nongxinle.dispatch.core.view.DispatchPageViewModel;
import com.nongxinle.dispatch.core.view.DispatchPageViewModelMaps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * nxCommunity pageViewModel 出口：经 dispatch-core adapter 组装后序列化为 API Map。
 */
@Service
public class CommunityDispatchPageAssembler {

    @Autowired
    private CommunityDispatchPageViewAdapter communityDispatchPageViewAdapter;

    public Map<String, Object> assemble(CommunityDispatchSandboxResult result) {
        return assemble(result, CommunityDispatchPageViewAdapter.AdapterOptions.defaults());
    }

    public Map<String, Object> assemble(
            CommunityDispatchSandboxResult result,
            CommunityDispatchPageViewAdapter.AdapterOptions options) {
        DispatchPageViewModel vm = communityDispatchPageViewAdapter.assemble(result, options);
        return DispatchPageViewModelMaps.toMap(vm);
    }
}
