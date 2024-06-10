package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 05-09 21:11
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.*;
import com.nongxinle.service.GbDistributerGoodsService;
import com.nongxinle.service.NxDepartmentService;
import com.nongxinle.service.NxDistributerGbDistributerService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.NxDistributerDepartmentService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxdistributergbdistributer")
public class NxDistributerGbDistributerController {

    @Autowired
    private NxDistributerGbDistributerService nxDisGbDisService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;

    @RequestMapping(value = "/delteNxAndGbBusiness/{id}")
    @ResponseBody
    public R delteNxAndGbBusiness(@PathVariable Integer id) {
        nxDisGbDisService.delete(id);
        return R.ok();
    }


    @RequestMapping(value = "/changeBusinessStatus", method = RequestMethod.POST)
    @ResponseBody
    public R changeBusinessStatus (@RequestParam Integer id, @RequestParam Integer status) {

        NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity = nxDisGbDisService.queryObject(id);
        nxDistributerGbDistributerEntity.setNxDgdStatus(status);
        nxDisGbDisService.update(nxDistributerGbDistributerEntity);
        return R.ok();
    }
    

    @RequestMapping(value = "/addNxAndGbBusiness", method = RequestMethod.POST)
    @ResponseBody
    public R addNxAndGbBusiness (@RequestBody NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity ) {
         nxDisGbDisService.save(nxDistributerGbDistributerEntity);
        return R.ok();
    }
    @RequestMapping(value = "/disGetAllGbDistributer/{disId}")
    @ResponseBody
    public R disGetAllGbDistributer(@PathVariable Integer disId) {
        List<GbDistributerEntity> gbDistributerEntities = nxDisGbDisService.queryAllGbDistribtuer(disId);
        return R.ok().put("data", gbDistributerEntities);
    }

    @RequestMapping(value = "/gbPeisongDepGetAllNxDistributer/{gbDepId}")
    @ResponseBody
    public R gbPeisongDepGetAllNxDistributer(@PathVariable Integer gbDepId) {

        List<NxDistributerEntity> nxDistributerEntities = nxDisGbDisService.queryGbDistributerNxDistribtuer(gbDepId);
        return R.ok().put("data", nxDistributerEntities);
    }

    @RequestMapping(value = "/gbDisGetAllNxDistributer/{gbDisId}")
    @ResponseBody
    public R gbDisGetAllNxDistributer(@PathVariable Integer gbDisId) {
        List<NxDistributerEntity> nxDistributerEntities = nxDisGbDisService.queryAllNxDistribtuer(gbDisId);
        return R.ok().put("data", nxDistributerEntities);
    }

    @RequestMapping(value = "/gbDisGetNxDistributerBindGoods")
    @ResponseBody
    public R gbDisGetNxDistributerBindGoods(Integer gbDisId, Integer nxDisId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", gbDisId);
        map.put("nxDisId", nxDisId);
        List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDistributerGoodsService.queryDisGoodsByParams(map);
        return R.ok().put("data", gbDistributerGoodsEntities);
    }







}
