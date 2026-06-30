package com.nongxinle.community.promotion.controller;

import com.nongxinle.community.promotion.service.NxCustomerPromotionCodeAttemptService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/nxcustomerpromotioncodeattempt")
public class NxCustomerPromotionCodeAttemptController {

    @Autowired
    private NxCustomerPromotionCodeAttemptService nxCustomerPromotionCodeAttemptService;

    /**
     * 我推广码下的无效注册尝试（审计列表，C 端推广人查看）
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public R list(Integer userId, Integer page, Integer limit, Integer communityId, Integer commId) {
        if (userId == null) {
            return R.error(-1, "userId不能为空");
        }
        int pageSize = limit != null && limit > 0 ? limit : 20;
        int pageNo = page != null && page > 0 ? page : 1;
        int offset = (pageNo - 1) * pageSize;
        Integer filterCommunityId = communityId != null ? communityId : commId;

        List<Map<String, Object>> list = nxCustomerPromotionCodeAttemptService.queryMyAttemptList(
                userId, filterCommunityId, offset, pageSize);
        int total = nxCustomerPromotionCodeAttemptService.queryMyAttemptTotal(userId, filterCommunityId);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("page", new PageUtils(list, total, pageSize, pageNo));
        return R.ok().put("data", data);
    }
}
