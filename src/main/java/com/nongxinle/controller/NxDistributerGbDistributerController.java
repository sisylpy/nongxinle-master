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

import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.GbTypeUtils.getGbDepartmentTypeAppSupplier;
import static com.nongxinle.utils.GbTypeUtils.getGbDepartmentTypeJicai;


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
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private NxDepartmentService nxDepartmentService;





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

        Integer nxDgdNxDistributerId = nxDistributerGbDistributerEntity.getNxDgdNxDistributerId();
        Integer nxDgdGbDistributerId = nxDistributerGbDistributerEntity.getNxDgdGbDistributerId();

        NxDistributerEntity distributerEntity = nxDistributerService.queryObject(nxDgdNxDistributerId);

        Map<String, Object> mapS = new HashMap<>();
        mapS.put("nxDisId", nxDgdNxDistributerId);
        mapS.put("gbDisId", nxDgdGbDistributerId);
        System.out.println("msssss" + mapS);
        List<NxJrdhSupplierEntity> nxJrdhSupplierEntities = nxJrdhSupplierService.queryJrdhSupplerByParams(mapS);
        if(nxJrdhSupplierEntities.size() == 0){
            Map<String, Object> map = new HashMap<>();
            map.put("disId", nxDgdGbDistributerId);
            map.put("type",getGbDepartmentTypeJicai() );
            System.out.println("mapapapap" + map);
            List<GbDepartmentEntity> gbDepartmentEntities = gbDepartmentService.queryDepByDepType(map);
            NxJrdhSupplierEntity supplierEntity = new NxJrdhSupplierEntity();
            supplierEntity.setNxJrdhsGbDistributerId(nxDgdGbDistributerId);
            supplierEntity.setNxJrdhsNxDistributerId(nxDgdNxDistributerId);
            supplierEntity.setNxJrdhsSupplierName(distributerEntity.getNxDistributerName());
            supplierEntity.setNxJrdhsSysMarketId(-1);
            supplierEntity.setNxJrdhsSysCityId(-1);
            supplierEntity.setNxJrdhsGbDepartmentId(gbDepartmentEntities.get(0).getGbDepartmentId());

            nxJrdhSupplierService.save(supplierEntity);
            nxDistributerGbDistributerEntity.setNxDgdNxSupplierId(supplierEntity.getNxJrdhSupplierId());
            nxDisGbDisService.update(nxDistributerGbDistributerEntity);

            GbDistributerEntity gbDistributerEntity = gbDistributerService.queryDistributerInfo(nxDgdGbDistributerId);

            NxDepartmentEntity departmentEntity = new NxDepartmentEntity();
            departmentEntity.setNxDepartmentName(gbDistributerEntity.getGbDistributerName());
            departmentEntity.setNxDepartmentDisId(nxDgdNxDistributerId);
            departmentEntity.setNxDepartmentGbDistributerId(nxDgdGbDistributerId);
            departmentEntity.setNxDepartmentSettleType(nxDistributerGbDistributerEntity.getNxDgdGbPayMethod());
            departmentEntity.setNxDepartmentFatherId(0);
            departmentEntity.setNxDepartmentSubAmount(0);
            departmentEntity.setNxDepartmentIsGroupDep(1);
            departmentEntity.setNxDepartmentPrintName("ApplyFiftyPanel");
            departmentEntity.setNxDepartmentSettleType(0);
            departmentEntity.setNxDepartmentType("unFixed");
            departmentEntity.setNxDepartmentShowWeeks(1);
            departmentEntity.setNxDepartmentWorkingStatus(-1);
            departmentEntity.setNxDepartmentOweBoxNumber(0);
            departmentEntity.setNxDepartmentDeliveryBoxNumber(0);
            departmentEntity.setNxDepartmentUnPayTotal("0");
            departmentEntity.setNxDepartmentAddCount(0);
            departmentEntity.setNxDepartmentPayTotal("0");
            departmentEntity.setNxDepartmentProfitTotal("0");
            departmentEntity.setNxDepartmentAttrName(gbDistributerEntity.getGbDistributerName());
            departmentEntity.setNxDepartmentOrderCode(gbDistributerEntity.getGbDistributerName());
            departmentEntity.setNxDepartmentRecordMinutes(30);
            departmentEntity.setNxDepartmentJoinDate(formatWhatDay(0));
            departmentEntity.setNxDepartmentOrderTotal(0);
            departmentEntity.setNxDepartmentRecordMinutes(0);

            nxDepartmentService.saveJustDepartment(departmentEntity);
        }


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
        map.put("isSupplierId", 1);
        System.out.println("abdbdbbdd" + map);
        List<GbDistributerEntity>  yishangArr = nxDisGbDisService.queryGbDistributerByParams(map);
        map.put("isSupplierId", -1);
        System.out.println("abdbdbbdd" + map);

        List<GbDistributerEntity> shixianArr = nxDisGbDisService.queryGbDistributerByParams(map);

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
