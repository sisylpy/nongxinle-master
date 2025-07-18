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
import com.nongxinle.service.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.GbTypeUtils.getGbDepartmentTypeAppSupplier;


@RestController
@RequestMapping("api/nxdistributergbdistributer")
public class NxDistributerGbDistributerController {

    @Autowired
    private NxDistributerGbDistributerService nxDisGbDisService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private NxJrdhSupplierService nxJrdhSupplierService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private GbDistributerService gbDistributerService;



    @RequestMapping(value = "/queryDisInfoBusiness",method = RequestMethod.POST)
    @ResponseBody
    public R queryDisInfoBusiness(Integer gbDisId, Integer nxDisId) {

        Map<String, Object> map = new HashMap<>();
        map.put("nxDisId", nxDisId);
        map.put("gbDisId", gbDisId);
        NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity = nxDisGbDisService.queryObjectByParams(map);

        if(nxDistributerGbDistributerEntity != null){
            return R.ok().put("data", "ok");
        }else{
            return R.ok().put("data",gbDistributerService.queryDistributerInfo(gbDisId));
        }

    }

    @RequestMapping(value = "/gbDisSaveBusiness")
    @ResponseBody
    public R gbDisSaveBusiness(Integer gbDisId, Integer nxDisId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", gbDisId);
        map.put("type",getGbDepartmentTypeAppSupplier() );
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
        NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity = new NxDistributerGbDistributerEntity();
        nxDistributerGbDistributerEntity.setNxDgdGbPayPeriodWeek(4);
        nxDistributerGbDistributerEntity.setNxDgdFromNxDepId(-1);
        nxDistributerGbDistributerEntity.setNxDgdStatus(0);
        nxDistributerGbDistributerEntity.setNxDgdGbGoodsPrice(0);
        nxDistributerGbDistributerEntity.setNxDgdGbPayMethod(1);
        nxDistributerGbDistributerEntity.setNxDgdGbDepId(departmentEntities.get(0).getGbDepartmentId());
        nxDistributerGbDistributerEntity.setNxDgdGbDistributerId(gbDisId);
        nxDistributerGbDistributerEntity.setNxDgdNxDistributerId(nxDisId);
        nxDisGbDisService.save(nxDistributerGbDistributerEntity);
        return R.ok();
    }


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
         nxDistributerGbDistributerEntity.setNxDgdGbPayPeriodWeek(4);
         nxDistributerGbDistributerEntity.setNxDgdFromNxDepId(-1);
         nxDisGbDisService.save(nxDistributerGbDistributerEntity);
        return R.ok();
    }
    @RequestMapping(value = "/disGetAllGbDistributer/{disId}")
    @ResponseBody
    public R disGetAllGbDistributer(@PathVariable Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("nxDisId", disId);
        map.put("isSupplierId", -1);
        System.out.println("abdbdbbdd" + map);
        List<GbDistributerEntity> shixianArr = nxDisGbDisService.queryGbDistributerByParams(map);
        map.put("isSupplierId", 0);
        System.out.println("abdbdbbdd" + map);

        List<GbDistributerEntity> yishangArr = nxDisGbDisService.queryGbDistributerByParams(map);

        System.out.println("zhelieieieieieie" + shixianArr);
        Map<String, Object> mapRes = new HashMap<>();
        mapRes.put("yishangArr",yishangArr);
        mapRes.put("shixianArr", shixianArr);
        return R.ok().put("data", mapRes);
    }

    @RequestMapping(value = "/gbPeisongDepGetAllNxDistributer/{gbDepId}")
    @ResponseBody
    public R gbPeisongDepGetAllNxDistributer(@PathVariable Integer gbDepId) {

        List<NxDistributerGbDistributerEntity> nxDistributerEntities = nxDisGbDisService.queryGbDistributerNxDistribtuer(gbDepId);
        return R.ok().put("data", nxDistributerEntities);
    }

    @RequestMapping(value = "/gbDisGetAllNxDistributer/{gbDisId}")
    @ResponseBody
    public R gbDisGetAllNxDistributer(@PathVariable Integer gbDisId) {
        List<NxDistributerEntity> nxDistributerEntities = nxDisGbDisService.queryAllNxDistribtuer(gbDisId);
        return R.ok().put("data", nxDistributerEntities);
    }
    @RequestMapping(value = "/gbDisGetAllNxDistributerAndSupplier/{gbDisId}")
    @ResponseBody
    public R gbDisGetAllNxDistributerAndSupplier(@PathVariable Integer gbDisId) {
        List<NxDistributerEntity> nxDistributerEntities = nxDisGbDisService.queryAllNxDistribtuer(gbDisId);

        Map<String, Object> map = new HashMap<>();
        map.put("gbDisId", gbDisId);
        List<NxJrdhSupplierEntity> supplierEntities = nxJrdhSupplierService.queryJrdhSupplerByParams(map);


        Map<String, Object> mapR = new HashMap<>();
        mapR.put("nx", nxDistributerEntities);
        mapR.put("supplier", supplierEntities);
        return R.ok().put("data", mapR);
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
