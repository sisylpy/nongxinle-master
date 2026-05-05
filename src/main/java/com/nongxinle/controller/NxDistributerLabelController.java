package com.nongxinle.controller;

import com.nongxinle.entity.NxDistributerLabelEntity;
import com.nongxinle.service.NxDistributerLabelService;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/nxdistributerlabel")
public class NxDistributerLabelController {

    @Autowired
    private NxDistributerLabelService nxDistributerLabelService;

    @RequestMapping(value = "/disGetLabels/{disId}", method = RequestMethod.GET)
    @ResponseBody
    public R disGetLabels(@PathVariable Integer disId) {
        List<NxDistributerLabelEntity> labels = nxDistributerLabelService.queryLabelsByDisId(disId);
        return R.ok().put("data", labels);
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public R save(@RequestBody NxDistributerLabelEntity labelEntity) {
        nxDistributerLabelService.save(labelEntity);
        return R.ok().put("data", labelEntity);
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public R update(@RequestBody NxDistributerLabelEntity labelEntity) {
        nxDistributerLabelService.update(labelEntity);
        return R.ok();
    }

    @RequestMapping(value = "/delete/{labelId}", method = RequestMethod.POST)
    @ResponseBody
    public R delete(@PathVariable Integer labelId) {
        nxDistributerLabelService.delete(labelId);
        return R.ok();
    }
}
