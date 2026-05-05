package com.nongxinle.controller;

import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDepartmentLabelEntity;
import com.nongxinle.service.NxDepartmentLabelService;
import com.nongxinle.service.NxDepartmentService;
import com.nongxinle.service.NxDistributerLabelService;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/nxdepartmentlabel")
public class NxDepartmentLabelController {

    @Autowired
    private NxDepartmentLabelService nxDepartmentLabelService;
    @Autowired
    private NxDepartmentService nxDepartmentService;
    @Autowired
    private NxDistributerLabelService nxDistributerLabelService;

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public R save(Integer depId, Integer labelId) {
        NxDepartmentEntity dep = nxDepartmentService.queryObject(depId);
        NxDepartmentLabelEntity entity = new NxDepartmentLabelEntity();
        entity.setNxDdlDepartmentId(depId);
        entity.setNxDdlDistributerLabelId(labelId);
        if (dep != null) {
            entity.setNxDdlDistributerId(dep.getNxDepartmentDisId());
        }
        nxDepartmentLabelService.save(entity);
        return R.ok();
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public R delete(Integer depId, Integer labelId) {
        nxDepartmentLabelService.deleteByDepartmentIdAndLabelId(depId, labelId);
        return R.ok();
    }

    @RequestMapping(value = "/saveDepartmentLabels", method = RequestMethod.POST)
    @ResponseBody
    public R saveDepartmentLabels(@RequestBody Map<String, Object> body) {
        Integer depId = body.get("depId") == null ? null : Integer.parseInt(body.get("depId").toString());
        Integer disId = body.get("disId") == null ? null : Integer.parseInt(body.get("disId").toString());
        List<Integer> labelIds = (List<Integer>) body.get("labelIds");
        nxDepartmentLabelService.saveDepartmentLabels(depId, disId, labelIds);
        return R.ok();
    }

    @RequestMapping(value = "/getLabelIds/{depId}", method = RequestMethod.GET)
    @ResponseBody
    public R getLabelIds(@PathVariable Integer depId) {
        List<Integer> labelIds = nxDepartmentLabelService.queryLabelIdsByDepartmentId(depId);
        return R.ok().put("data", labelIds);
    }

    /**
     * 批发商编辑客户标签时，一次返回：
     * 1) 当前批发商可用标签列表
     * 2) 该客户已选标签id列表
     */
    @RequestMapping(value = "/disGetLabelData", method = RequestMethod.GET)
    @ResponseBody
    public R disGetLabelData(Integer disId, Integer depFatherId) {
        List<Integer> selectedLabelIds = nxDepartmentLabelService.queryLabelIdsByDepartmentId(depFatherId);
        return R.ok()
                .put("disId", disId)
                .put("labelList", nxDistributerLabelService.queryLabelsByDisId(disId))
                .put("selectedLabelIds", selectedLabelIds);
    }

    /**
     * 批发商对比标签后提交，服务端按差异增删，不做全量清空重建。
     */
    @RequestMapping(value = "/disSyncDepartmentLabels", method = RequestMethod.POST)
    @ResponseBody
    public R disSyncDepartmentLabels(@RequestBody Map<String, Object> body) {
        Integer depFatherId = body.get("depFatherId") == null ? null : Integer.parseInt(body.get("depFatherId").toString());
        Integer disId = body.get("disId") == null ? null : Integer.parseInt(body.get("disId").toString());

        List<Integer> labelIds = new ArrayList<>();
        Object labelsObj = body.get("labelIds");
        if (labelsObj instanceof List) {
            List<?> rawList = (List<?>) labelsObj;
            for (Object raw : rawList) {
                if (raw != null) {
                    labelIds.add(Integer.parseInt(raw.toString()));
                }
            }
        }

        nxDepartmentLabelService.syncDepartmentLabelsByDiff(depFatherId, disId, labelIds);
        return R.ok();
    }
}
