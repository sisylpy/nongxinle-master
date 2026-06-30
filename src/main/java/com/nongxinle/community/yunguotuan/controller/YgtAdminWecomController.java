package com.nongxinle.community.yunguotuan.controller;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.QyGbDisCorpEntity;
import com.nongxinle.service.QyGbDisCorpService;
import com.nongxinle.utils.Constant;
import com.nongxinle.utils.R;
import com.nongxinle.utils.WxTokenManager;
import com.nongxinle.community.yunguotuan.entity.YgtWecomGroupEntity;
import com.nongxinle.community.yunguotuan.service.YgtArchiveIngestService;
import com.nongxinle.community.yunguotuan.service.YgtWecomGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/ygt/admin/wecom")
public class YgtAdminWecomController {
    @Autowired
    private YgtWecomGroupService ygtWecomGroupService;

    @Autowired
    private YgtArchiveIngestService ygtArchiveIngestService;

    @Autowired
    private WxTokenManager wxTokenManager;

    @Autowired
    private QyGbDisCorpService qyGbDisCorpService;

    @RequestMapping(value = "/groups/sync", method = RequestMethod.POST)
    @ResponseBody
    public R syncGroups(@RequestBody(required = false) Map<String, Object> params) {
        try {
            Map<String, Object> body = params == null ? new HashMap<String, Object>() : params;
            // macOS mock 模式：默认使用服务商 corpid
            if (!body.containsKey("corpId") || body.get("corpId") == null) {
                body.put("corpId", com.nongxinle.utils.Constant.CorpID);
            }
            return R.ok().put("data", ygtWecomGroupService.syncGroups(body));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/groups", method = RequestMethod.GET)
    @ResponseBody
    public R groups(@RequestParam(required = false) String corpId,
                    @RequestParam(required = false) Integer status,
                    @RequestParam(required = false) Integer nxCommunityId,
                    @RequestParam(defaultValue = "0") Integer offset,
                    @RequestParam(defaultValue = "100") Integer limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("corpId", corpId);
        params.put("status", status);
        params.put("nxCommunityId", nxCommunityId);
        params.put("offset", offset);
        params.put("limit", limit);
        List<YgtWecomGroupEntity> groups = ygtWecomGroupService.queryGroups(params);
        return R.ok().put("data", groups);
    }

    @RequestMapping(value = "/groups/{id}/enable", method = RequestMethod.POST)
    @ResponseBody
    public R enableGroup(@PathVariable Long id) {
        ygtWecomGroupService.enableGroup(id);
        return R.ok();
    }

    @RequestMapping(value = "/groups/{id}/disable", method = RequestMethod.POST)
    @ResponseBody
    public R disableGroup(@PathVariable Long id) {
        ygtWecomGroupService.disableGroup(id);
        return R.ok();
    }

    @RequestMapping(value = "/archive/pull", method = RequestMethod.POST)
    @ResponseBody
    public R pullArchive(@RequestBody(required = false) Map<String, Object> params) {
        try {
            Map<String, Object> body = params == null ? new HashMap<String, Object>() : params;
            String corpId = body.get("corpId") == null ? com.nongxinle.utils.Constant.CorpID : String.valueOf(body.get("corpId"));
            Integer limit = body.get("limit") == null ? null : Integer.valueOf(String.valueOf(body.get("limit")));
            return R.ok().put("data", ygtArchiveIngestService.pullArchive(corpId, limit));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/archive/status", method = RequestMethod.GET)
    @ResponseBody
    public R archiveStatus(@RequestParam(required = false) String corpId) {
        return R.ok().put("data", ygtArchiveIngestService.archiveStatus(corpId));
    }

    /**
     * 生成企业安装授权链接
     * GET /api/ygt/admin/wecom/auth/install-url
     *
     * @param redirectUri 授权完成后跳转地址（可选，默认 grainservice.club）
     */
    @RequestMapping(value = "/auth/install-url", method = RequestMethod.GET)
    @ResponseBody
    public R installUrl(@RequestParam(required = false) String redirectUri) {
        try {
            String suiteToken = wxTokenManager.getWxProperty(Constant.SUITE_TOKEN_YGT);
            if (suiteToken == null || "-1".equals(suiteToken) || suiteToken.isEmpty()) {
                return R.error("suite_access_token 未获取到，请先确认 suite_ticket 已推送");
            }

            // 1. 获取 pre_auth_code
            String preAuthUrl = "https://qyapi.weixin.qq.com/cgi-bin/service/get_pre_auth_code?suite_access_token=" + suiteToken;
            String body = HttpRequest.get(preAuthUrl).execute().body();
            System.out.println("get_pre_auth_code response: " + body);
            JSONObject json = JSONObject.parseObject(body);
            Integer errcode = json.getInteger("errcode");
            if (errcode != null && errcode != 0) {
                return R.error("获取 pre_auth_code 失败: " + json.getString("errmsg"));
            }
            String preAuthCode = json.getString("pre_auth_code");
            int expiresIn = json.getIntValue("expires_in");

            // 2. 拼接授权链接
            if (redirectUri == null || redirectUri.isEmpty()) {
                redirectUri = "https://grainservice.club:8443/nongxinle/";
            }
            String encodedRedirect = URLEncoder.encode(redirectUri, "UTF-8");
            String installUrl = "https://open.work.weixin.qq.com/3rdapp/install?"
                    + "suite_id=" + Constant.SuiteIDYGT
                    + "&pre_auth_code=" + preAuthCode
                    + "&redirect_uri=" + encodedRedirect
                    + "&state=ygt_install";

            Map<String, Object> result = new HashMap<>();
            result.put("installUrl", installUrl);
            result.put("preAuthCode", preAuthCode);
            result.put("expiresIn", expiresIn);
            result.put("suiteId", Constant.SuiteIDYGT);
            System.out.println("YGT installUrl=" + installUrl);
            return R.ok().put("data", result);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("生成安装链接失败: " + e.getMessage());
        }
    }

    /**
     * 查看已授权企业列表
     * GET /api/ygt/admin/wecom/auth/corps
     */
    @RequestMapping(value = "/auth/corps", method = RequestMethod.GET)
    @ResponseBody
    public R authCorps() {
        Map<String, Object> params = new HashMap<>();
        List<QyGbDisCorpEntity> corps = qyGbDisCorpService.queryList(params);
        return R.ok().put("data", corps);
    }
}
