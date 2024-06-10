package com.nongxinle.controller;

/**
 * @author lpy
 * @date 07-30 23:58
 */

import java.math.BigDecimal;
import java.util.*;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import static com.nongxinle.utils.DateUtils.formatWhatDay;


@RestController
@RequestMapping("api/nxdepartmentdisgoods")
public class NxDepartmentDisGoodsController {
    @Autowired
    private NxDepartmentDisGoodsService nxDepartmentDisGoodsService;
    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;
    @Autowired
    private NxDepartmentStandardService nxDepartmentStandardService;
    @Autowired
    private NxDistributerStandardService nxDistributerStandardService;
    @Autowired
    private NxDistributerDepartmentService nxDisDepartmentService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private NxDepartmentService nxDepartmentService;
    @Autowired
    private NxDepartmentOrdersHistoryService nxDepartmentOrdersHistoryService;





    @RequestMapping(value = "/addDepartmentDisGoods", method = RequestMethod.POST)
    @ResponseBody
    public R addDepartmentDisGoods (Integer disGoodsId, Integer depId, String sellingPrice) {
        NxDistributerGoodsEntity disGoods = nxDistributerGoodsService.queryObject(disGoodsId);

        List<NxDepartmentEntity> departmentEntities = nxDepartmentService.querySubDepartments(depId);
        if(departmentEntities.size() > 0){
            for (NxDepartmentEntity subDeps : departmentEntities) {
                //添加部门商品
                NxDepartmentDisGoodsEntity disGoodsEntity = new NxDepartmentDisGoodsEntity();
                disGoodsEntity.setNxDdgDepGoodsName(disGoods.getNxDgGoodsName());
                disGoodsEntity.setNxDdgDisGoodsId(disGoods.getNxDistributerGoodsId());
                disGoodsEntity.setNxDdgDisGoodsFatherId(disGoods.getNxDgDfgGoodsFatherId());
                disGoodsEntity.setNxDdgDepGoodsPinyin(disGoods.getNxDgGoodsPinyin());
                disGoodsEntity.setNxDdgDepGoodsPy(disGoods.getNxDgGoodsPy());
                disGoodsEntity.setNxDdgDepGoodsStandardname(disGoods.getNxDgGoodsStandardname());
                disGoodsEntity.setNxDdgDepartmentId(subDeps.getNxDepartmentId());
                disGoodsEntity.setNxDdgDepartmentFatherId(depId);
                disGoodsEntity.setNxDdgDisGoodsId(disGoods.getNxDgDistributerId());
                disGoodsEntity.setNxDdgOrderPrice(sellingPrice);
                disGoodsEntity.setNxDdgDisGoodsGrandId(disGoods.getNxDgDfgGoodsGrandId());
                nxDepartmentDisGoodsService.save(disGoodsEntity);
            }
        }else{
            //添加部门商品
            NxDepartmentDisGoodsEntity disGoodsEntity = new NxDepartmentDisGoodsEntity();
            disGoodsEntity.setNxDdgDepGoodsName(disGoods.getNxDgGoodsName());
            disGoodsEntity.setNxDdgDisGoodsId(disGoods.getNxDistributerGoodsId());
            disGoodsEntity.setNxDdgDisGoodsFatherId(disGoods.getNxDgDfgGoodsFatherId());
            disGoodsEntity.setNxDdgDepGoodsPinyin(disGoods.getNxDgGoodsPinyin());
            disGoodsEntity.setNxDdgDepGoodsPy(disGoods.getNxDgGoodsPy());
            disGoodsEntity.setNxDdgDepGoodsStandardname(disGoods.getNxDgGoodsStandardname());
            disGoodsEntity.setNxDdgDepartmentId(depId);
            disGoodsEntity.setNxDdgDepartmentFatherId(depId);
            disGoodsEntity.setNxDdgOrderPrice(sellingPrice);
            disGoodsEntity.setNxDdgDisGoodsGrandId(disGoods.getNxDgDfgGoodsGrandId());
            nxDepartmentDisGoodsService.save(disGoodsEntity);
        }

        return R.ok();
    }




    @RequestMapping(value = "/updateDepGoodsSellingPrice", method = RequestMethod.POST)
    @ResponseBody
    public R updateDepGoodsSellingPrice (Integer depGoodsId, String sellingPrice ) {
        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryObject(depGoodsId);

        departmentDisGoodsEntity.setNxDdgOrderPrice(sellingPrice);
        nxDepartmentDisGoodsService.update(departmentDisGoodsEntity);

        return R.ok();
    }




    @RequestMapping(value = "/getDepGoodsDepartment",  method = RequestMethod.POST)
    @ResponseBody
    public R getDepGoodsDepartment(Integer disGoodsId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("isGroup", 1);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentService.queryDepartmentBySettleType(map);

        if(departmentEntities.size() > 0){
            for (NxDepartmentEntity department : departmentEntities) {
                Integer gbDepartmentId = department.getNxDepartmentId();
                Map<String, Object> map1 = new HashMap<>();
                map1.put("depFatherId", gbDepartmentId);
                map1.put("disGoodsId", disGoodsId);
                System.out.println("depdidgoods" + map1);
                List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(map1);
                if(departmentDisGoodsEntities.size() > 0){
                    department.setIsSelected(true);
                    department.setNxDepartmentDisGoodsEntity(departmentDisGoodsEntities.get(0));
                }
            }
        }

        return R.ok().put("data", departmentEntities);
    }



    @RequestMapping(value = "/deleteDepGoods/{id}" )
    @ResponseBody
    public R deleteDepGoods (@PathVariable Integer id) {
        Map<String, Object> map = new HashMap<>();
        map.put("depDisGoodsId", id);
        map.put("status", 3);
        map.put("arriveDateDayu", formatWhatDay(-1));
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        if(ordersEntities.size() > 0) {
            return R.error(-1,"有未完成订单，暂不能删除");
        }else {
            Map<String, Object> mapDep = new HashMap<>();
            mapDep.put("depDisGoodsId", id);
            List<NxDepartmentOrdersHistoryEntity> nxDepartmentOrdersHistoryEntities = nxDepartmentOrdersHistoryService.queryDepHistoryOrdersByParams(mapDep);
            if(nxDepartmentOrdersHistoryEntities.size() > 0){
                for(NxDepartmentOrdersHistoryEntity historyEntity: nxDepartmentOrdersHistoryEntities){
                    nxDepartmentOrdersHistoryService.delete(historyEntity.getNxDepartmentOrdersHistoryId());
                }
            }

            nxDepartmentDisGoodsService.delete(id);
        }
        return R.ok();

    }

    @RequestMapping(value = "/deleteDepGoodsArr/{str}" )
    @ResponseBody
    public R deleteDepGoodsArr (@PathVariable String str) {
        String[] arr = str.split(",");
        for (String s : arr) {
            Integer depGoodsId = Integer.valueOf(s);
            Map<String, Object> map = new HashMap<>();
            map.put("depDisGoodsId", depGoodsId);
            map.put("status", 3);
            map.put("arriveDateDayu", formatWhatDay(-1));
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
            if(ordersEntities.size() > 0) {
                return R.error(-1,"有未完成订单，暂不能删除");
            }else {
                nxDepartmentDisGoodsService.delete(depGoodsId);
            }
        }
        return R.ok();

    }

    @RequestMapping(value = "/deleteDepGoodsArr1", method = RequestMethod.POST)
    @ResponseBody
    public R deleteDepGoodsArr1 (@RequestBody List<NxDepartmentDisGoodsEntity> depGoodsArr  ) {
        for (NxDepartmentDisGoodsEntity goods : depGoodsArr) {
            nxDepartmentDisGoodsService.delete(goods.getNxDepartmentDisGoodsId());
        }
        return R.ok();
    }

    /**
     * DISTRIBUTER
     * 获取不是批发商商品的客户列表
     * @param disGoodsId 批发商商品id
     * @param disId 批发商id
     * @return 客户列表
     */
    @RequestMapping(value = "/getUnDisGoodsDepartments", method = RequestMethod.POST)
    @ResponseBody
    public R getUnDisGoodsDepartments(Integer disGoodsId, Integer disId) {
        //查询已经添加disGoods的客户
        List<NxDepartmentEntity> addGoodsCustomer = nxDepartmentDisGoodsService.queryDepartmentsByDisGoodsId(disGoodsId);
        //查询批发商的全部客户
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        List<NxDepartmentEntity> allCustomer = nxDisDepartmentService.queryDisDepartmentsBySettleType(map);
        //去除已经添加disGoods的客户
        allCustomer.removeAll(addGoodsCustomer);
        //返回没有添加disGoods的客户
        return R.ok().put("data", allCustomer);
    }



    /**
     * PURCHASE,ORDER,DISTRIBUTER
     * 订货群获取自己群商品类别
     * @param depFatherId 订货群id
     * @return 订货群商品类别列表
     */
    @RequestMapping(value = "/disGetDepGoodsCata/{depFatherId}")
    @ResponseBody
    public R disGetDepGoodsCata(@PathVariable Integer depFatherId) {
        System.out.println(depFatherId+ "newkekkeke");
        List<NxDistributerFatherGoodsEntity> disGoodsEntities = nxDepartmentDisGoodsService.disGetDepDisGoodsCata(depFatherId);
        return R.ok().put("data", disGoodsEntities);
    }



    /**
     * PURCHASE,ORDER,DISTRIBUTER
     * 订货群获取自己群商品类别
     * @param depId 订货部门id
     * @return 订货群商品类别列表
     */
    @RequestMapping(value = "/depGetDepDisGoodsCata/{depId}")
    @ResponseBody
    public R depGetDepDisGoodsCata(@PathVariable Integer depId) {
        List<NxDistributerFatherGoodsEntity> disGoodsEntities = nxDepartmentDisGoodsService.depGetDepDisGoodsCata(depId);
        return R.ok().put("data", disGoodsEntities);
    }



    /**
     * PURCHASER
     * 采购员获取批发商商品，显示群是否添加

     * @return 批发商商品包含是否订货群下载
     */
//    @RequestMapping(value = "/depGetDisGoods", method = RequestMethod.POST)
//    @ResponseBody
//    public R depGetDisGoods(Integer limit, Integer page, Integer fatherId, Integer depId, Integer disId) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("offset", (page - 1) * limit);
//        map.put("limit", limit);
//        map.put("fatherId", fatherId);
//        map.put("depId", depId);
//        map.put("status", 1);
//        System.out.println(depId +"deppepeepepeppe");
//
//        List<NxDistributerGoodsEntity> disGoods = nxDistributerGoodsService.depQueryDisGoodsWithOrdersByFatherId(map);
//
//        Map<String, Object> map3 = new HashMap<>();
//        map3.put("disId", disId );
//        map3.put("fatherId", fatherId );
//		int total = nxDistributerGoodsService.queryDisGoodsTotal(map3);
//
//        Map<String, Object> map5 = new HashMap<>();
//        map5.put("arr", disGoods);
//        List<Map<String, Object>> mapList = new ArrayList<>();
//        mapList.add(map5);
//
//        PageUtils pageUtil = new PageUtils(mapList, total, limit, page);
//        return R.ok().put("page", pageUtil);
//    }


    @RequestMapping(value = "/deleteDepDisGoods/{depDisGoodsId}")
    @ResponseBody
    public R deleteDepDisGoods(@PathVariable Integer depDisGoodsId) {
        System.out.println(depDisGoodsId + "depdisgoodsid....");
        Map<String, Object> map = new HashMap<>();
        map.put("depDisGoodsId", depDisGoodsId);
        map.put("status", 4 );
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        if (ordersEntities.size() > 0){
            return R.error(-1,"此商品下有订单");
        }else {
            nxDepartmentDisGoodsService.delete(depDisGoodsId);
            return R.ok();
        }
    }

    /**
     * PURCHASE
     * 保存群商品
     * @param nxDepartmentDisGoods 群商品
     * @return ok
     */

    @RequestMapping("/saveDepDisGoods")
    public R saveDepDisGoods(@RequestBody NxDepartmentDisGoodsEntity nxDepartmentDisGoods) {

        //判断是否已经下载nxDdgDepartmentFatherId
        Integer nxDdgDepartmentId = nxDepartmentDisGoods.getNxDdgDepartmentFatherId();
        Integer nxDdgDisGoodsId = nxDepartmentDisGoods.getNxDdgDisGoodsId();
        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", nxDdgDepartmentId);
        map.put("disGoodsId", nxDdgDisGoodsId);
        List<NxDepartmentDisGoodsEntity> disGoodsEntities =   nxDepartmentDisGoodsService.queryDepDisGoodsByParams(map);

        if(disGoodsEntities.size() > 0){
            return R.error(-1, "已经下载");
        }else{
            nxDepartmentDisGoodsService.save(nxDepartmentDisGoods);

            Integer nxDepDisGoodsId = nxDepartmentDisGoods.getNxDepartmentDisGoodsId();
            List<NxDepartmentStandardEntity> nxDepStandardEntities = nxDepartmentDisGoods.getNxDepStandardEntities();
            if(nxDepStandardEntities.size() > 0){
                for (NxDepartmentStandardEntity standard : nxDepStandardEntities) {
                    standard.setNxDdsDdsGoodsId(nxDepDisGoodsId);
                    nxDepartmentStandardService.save(standard);
                }
            }
            return R.ok().put("data", nxDepartmentDisGoods);
        }

    }

    /**
     * DISTRIBUTE,
     * 批发商添加disGoods的订货群
     * @param depDisGoods 批发商商品
     * @return ok
     */
//    @RequestMapping(value = "/disSaveDepartDisGoods", method = RequestMethod.POST)
//    @ResponseBody
//    public R disSaveDepartDisGoods (@RequestBody NxDepartmentDisGoodsEntity depDisGoods  ) {
//        Integer nxDdgDisGoodsId = depDisGoods.getNxDdgDisGoodsId();
//        NxDistributerGoodsEntity nxDisGoodsEntity = nxDistributerGoodsService.queryObject(nxDdgDisGoodsId);
//        depDisGoods.setNxDdgDepGoodsName(nxDisGoodsEntity.getNxDgGoodsName());
//        depDisGoods.setNxDdgDepGoodsDetail(nxDisGoodsEntity.getNxDgGoodsDetail());
//        depDisGoods.setNxDdgDepGoodsBrand(nxDisGoodsEntity.getNxDgGoodsBrand());
//        depDisGoods.setNxDdgDepGoodsPinyin(nxDisGoodsEntity.getNxDgGoodsPinyin());
//        depDisGoods.setNxDdgDepGoodsPy(nxDisGoodsEntity.getNxDgGoodsPy());
//        depDisGoods.setNxDdgDisGoodsFatherId(nxDisGoodsEntity.getNxDgDfgGoodsFatherId());
//
//        nxDepartmentDisGoodsService.save(depDisGoods);
//
//        Integer nxDepartmentDisGoodsId = depDisGoods.getNxDepartmentDisGoodsId();
//        //批发商订货规格
//        List<NxDistributerStandardEntity>  standardEntities = nxDistributerStandardService.queryDisStandardByDisGoodsId(depDisGoods.getNxDdgDisGoodsId());
//        if(standardEntities.size() > 0){
//            for (NxDistributerStandardEntity disEntities : standardEntities) {
//                NxDepartmentStandardEntity depstandard = new NxDepartmentStandardEntity();
//                depstandard.setNxDdsStandardName(disEntities.getNxDsStandardName());
//                depstandard.setNxDdsDdsGoodsId(nxDepartmentDisGoodsId);
//                nxDepartmentStandardService.save(depstandard);
//            }
//        }
//        return R.ok();
//    }

    //
    /**
     * ORDER
     * 订货员获取配送商品
     * @param depId 群id
     * @return 群商品列表
     */
    @RequestMapping(value = "/depGetDepGoods/{depId}")
    @ResponseBody
    public R depGetDepGoods(@PathVariable Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("status", 3);
        List<NxDistributerFatherGoodsEntity> goodsEntities = nxDepartmentDisGoodsService.depQueryDepGoodsWithOrder(map);
        if(goodsEntities.size() > 0){
            for(NxDistributerFatherGoodsEntity fatherGoodsEntity: goodsEntities){
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("greatGrandId", fatherGoodsEntity.getNxDistributerFatherGoodsId());
                mapDep.put("depId", depId);
                mapDep.put("status", 3);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                fatherGoodsEntity.setNewOrderCount(integer);
            }
        }

//        NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depId);
//        Integer status = departmentEntity.getNxDepartmentWorkingStatus();
//        departmentEntity.setNxDepartmentWorkingStatus(status + 1);
//        nxDepartmentService.update(departmentEntity);
        return R.ok().put("data", goodsEntities);
    }


    @RequestMapping(value = "/depGetDepsGoods/{depFatherId}")
    @ResponseBody
    public R depGetDepsGoods(@PathVariable Integer depFatherId) {

        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depFatherId);
        map.put("status", 1);

        List<NxDepartmentDisGoodsEntity> departmentEntity = nxDepartmentDisGoodsService.depGetDepsGoods(map);
        if(departmentEntity != null){
            return R.ok().put("data", departmentEntity);
        }else{
            return R.error(-1,"meiyou");
        }


    }

    /**
     * 多部门获取部门商品
     * @param depFatherId
     * @return
     */
    @RequestMapping(value = "/depFatherGetSubDepsGoods/{depFatherId}")
    @ResponseBody
    public R depFatherGetSubDepsGoods(@PathVariable Integer depFatherId) {


        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depFatherId);
        map.put("status", 1);

        NxDepartmentEntity departmentEntities = nxDepartmentDisGoodsService.depFatherGetSubDepsGoods(map);

        return R.ok().put("data", departmentEntities);
    }

    /**
     * ORDER
     * 订货员获取配送商品

     * @return 群商品列表
     */
    @RequestMapping(value = "/disGetDepGoods/{depFatherId}")
    @ResponseBody
    public R disGetDepGoods(@PathVariable Integer depFatherId) {
        System.out.println(depFatherId + "depeppepepepe");
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsWithOrders(depFatherId);
        if(fatherGoodsEntities.size() > 0){
            for(NxDistributerFatherGoodsEntity fatherGoodsEntity: fatherGoodsEntities){
                Map<String, Object> map = new HashMap<>();
                map.put("disGrandId", fatherGoodsEntity.getNxDistributerFatherGoodsId());
                map.put("status", 3);
                map.put("depFatherId", depFatherId);
                String names = "";
                List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
                if(ordersEntities.size() > 0 ){
                    for(NxDepartmentOrdersEntity ordersEntity: ordersEntities){
                        String nxDgGoodsName = ordersEntity.getNxDistributerGoodsEntity().getNxDgGoodsName();
                        names = names  + nxDgGoodsName + ",";
                    }
                }
                fatherGoodsEntity.setDgGoodsSubNames(names);
            }
        }
        return R.ok().put("data",fatherGoodsEntities);
    }




    @RequestMapping(value = "/disGetDepGoods1", method = RequestMethod.POST)
    @ResponseBody
    public R disGetDepGoods1(Integer limit, Integer page,Integer depFatherId, Integer fatherId) {
        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("depFatherId", depFatherId);
        map.put("fatherId", fatherId);
        List<NxDepartmentDisGoodsEntity> goodsEntities = nxDepartmentDisGoodsService.queryDepGoodsByFatherId(map);

        for (NxDepartmentDisGoodsEntity disGoods : goodsEntities) {
            Integer nxDepartmentDisGoodsId = disGoods.getNxDepartmentDisGoodsId();
            List<NxDepartmentStandardEntity> standardEntities = nxDepartmentStandardService.queryDepGoodsStandards(nxDepartmentDisGoodsId);
            disGoods.setNxDepStandardEntities(standardEntities);
        }

        Map<String, Object> map3 = new HashMap<>();
        map3.put("depFatherId", depFatherId);
        map3.put("fatherId", fatherId );
        int total = nxDepartmentDisGoodsService.queryDepGoodsTotal(map3);
        PageUtils pageUtil = new PageUtils(goodsEntities, total, limit, page);
        return R.ok().put("page", pageUtil);
    }










}
