package com.nongxinle.controller;

/**
 * @author lpy
 * @date 07-27 21:41
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.service.NxDistributerGoodsService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxDistributerStandardEntity;
import com.nongxinle.service.NxDistributerStandardService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxdistributerstandard")
public class NxDistributerStandardController {
    @Autowired
    private NxDistributerStandardService nxDistributerStandardService;
    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;


    @RequestMapping(value = "/disDeleteStandard/{id}")
    @ResponseBody
    public R disDeleteStandard(@PathVariable Integer id) {

        NxDistributerStandardEntity standardEntity = nxDistributerStandardService.queryObject(id);
        Integer nxDsDisGoodsId = standardEntity.getNxDsDisGoodsId();
        nxDistributerStandardService.delete(id);
        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(nxDsDisGoodsId);

        return R.ok().put("data", distributerGoodsEntity);
    }

    /**
     * 批发商修改订货单位
     * @param standard 单位
     * @return ok
     */
    @RequestMapping(value = "/disUpdateStandard", method = RequestMethod.POST)
    @ResponseBody
    public R disUpdateStandard(@RequestBody NxDistributerStandardEntity standard) {
        System.out.println("update");
        nxDistributerStandardService.update(standard);
        return R.ok().put("data", standard);
    }

    /**
     * 批发商添加订货规格
     * @param standard 订货规格
     * @return 规格
     */
    @RequestMapping(value = "/disSaveStandard", method = RequestMethod.POST)
    @ResponseBody
    public R disSaveStandard(@RequestBody NxDistributerStandardEntity standard) {
        Map<String, Object> map = new HashMap<>();
        map.put("standardName",standard.getNxDsStandardName());
        map.put("disGoodsId", standard.getNxDsDisGoodsId());

        List<NxDistributerStandardEntity> distributerStandardEntities = nxDistributerStandardService.queryDisStandardByParams(map);
        Integer nxDsDisGoodsId = standard.getNxDsDisGoodsId();
        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDsDisGoodsId);
        String nxDgGoodsStandardname = distributerGoodsEntity.getNxDgGoodsStandardname();
        if(distributerStandardEntities.size() == 0 && !nxDgGoodsStandardname.equals(standard.getNxDsStandardName()) ){
            nxDistributerStandardService.save(standard);
            return R.ok().put("data", standard);
        }else{

            return R.error(-1,"有相同的规格");
        }

    }







}
