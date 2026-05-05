package com.nongxinle.controller;

/**
 * @author lpy
 * @date 07-30 23:58
 */

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private NxDistributerDepartmentService nxDisDepartmentService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private NxDepartmentService nxDepartmentService;
    @Autowired
    private NxDepartmentOrdersHistoryService nxDepartmentOrdersHistoryService;
    @Autowired
    private NxDistributerFatherGoodsService nxDistributerFatherGoodsService;

    private static final Logger log = LoggerFactory.getLogger(NxDepartmentDisGoodsController.class);



    @RequestMapping(value = "/disGetSubDepAiOrder", method = RequestMethod.POST)
    @ResponseBody
    public R disGetSubDepAiOrder(Integer depId,
                                 Integer page,
                                 Integer limit
    ) {
        System.out.println("abccldid" + limit);
        PageUtils pageUtil  = nxDepartmentDisGoodsService.computeReorder(depId, page, limit);

        return R.ok().put("page", pageUtil);
    }

    @RequestMapping(value = "/disSaveDepGoodsName", method = RequestMethod.POST)
    @ResponseBody
    public R disSaveDepGoodsName (Integer depId, String goodsName, Integer disGoodsId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", disGoodsId);
        map.put("depId", depId);
        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
        if(departmentDisGoodsEntity != null){
            departmentDisGoodsEntity.setNxDdgOrderGoodsName(goodsName);
            nxDepartmentDisGoodsService.update(departmentDisGoodsEntity);
        }
        return R.ok();
    }

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

                NxDistributerFatherGoodsEntity fatherGoodsEntity = nxDistributerFatherGoodsService.queryObject(disGoods.getNxDgDfgGoodsGrandId());
                Integer greatFatherId = fatherGoodsEntity.getNxDfgFathersFatherId();
                disGoodsEntity.setNxDdgDisGoodsGreatId(greatFatherId);

                disGoodsEntity.setNxDdgNxDistributerId(disGoods.getNxDgDistributerId());
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
            NxDistributerFatherGoodsEntity fatherGoodsEntity = nxDistributerFatherGoodsService.queryObject(disGoods.getNxDgDfgGoodsGrandId());
            Integer greatFatherId = fatherGoodsEntity.getNxDfgFathersFatherId();
            disGoodsEntity.setNxDdgDisGoodsGreatId(greatFatherId);
            disGoodsEntity.setNxDdgNxDistributerId(disGoods.getNxDgDistributerId());
            nxDepartmentDisGoodsService.save(disGoodsEntity);
        }

        return R.ok();
    }




    @RequestMapping(value = "/updateDepGoodsSellingPrice", method = RequestMethod.POST)
    @ResponseBody
    public R updateDepGoodsSellingPrice (Integer depGoodsId, String sellingPrice, String pickDetail, String orderName ) {
        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryObject(depGoodsId);

        departmentDisGoodsEntity.setNxDdgOrderPrice(sellingPrice);
        departmentDisGoodsEntity.setNxDdgPickDetail(pickDetail);
        departmentDisGoodsEntity.setNxDdgOrderGoodsName(orderName);
        nxDepartmentDisGoodsService.update(departmentDisGoodsEntity);

        return R.ok().put("data", departmentDisGoodsEntity);
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
                map1.put("disId", department.getNxDepartmentDisId());
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
                    historyEntity.setNxDohDepDisGoodsId(-1);
                    nxDepartmentOrdersHistoryService.update(historyEntity);
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
    @RequestMapping(value = "/depGetDepDisGoodsCata")
    @ResponseBody
    public R depGetDepDisGoodsCata(Integer depId, Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("disId", disId);
        map.put("notLinshi", 1);
        System.out.println("cattaktktktkktk");
        List<NxDistributerFatherGoodsEntity> disGoodsEntities = nxDepartmentDisGoodsService.depGetDepDisGoodsCata(map);


        List<Integer > departmentDisGoodsEntities =   nxDepartmentDisGoodsService.queryOnlyDepGoodsIds(map);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("cataArr",disGoodsEntities);
        mapR.put("depGoodsArr", departmentDisGoodsEntities);

        return R.ok().put("data", mapR);
    }

    


    @RequestMapping(value = "/depGetDepGoodsByIds", method = RequestMethod.POST)
    @ResponseBody
    public R depGetDepGoodsByIds(String depGoodsIds) {
        System.out.println("depGodosiIdss" + depGoodsIds);
        if(depGoodsIds.startsWith("[") && depGoodsIds.endsWith("]")) {
            depGoodsIds = depGoodsIds.substring(1, depGoodsIds.length() - 1);
        }

        String[] split = depGoodsIds.split(",");  //  ["8251", "8252", ...]
        List<Integer> idList = Arrays.stream(split)
                .map(Integer::valueOf)
                .collect(Collectors.toList());
        Map<String, Object> map = new HashMap<>();
        map.put("status", 4);
        map.put("idList",idList);

        System.out.println("depididiididiidididd" + map);
        List<NxDepartmentDisGoodsEntity> currentPageList = nxDepartmentDisGoodsService.queryDepDisGoodsOrders(map);

        // 5. 返回分页数据
        return R.ok().put("page", currentPageList);
    }

    @RequestMapping(value = "/depGetDepGoodsPage")
    @ResponseBody
    public R depGetDepGoodsPage(Integer limit, Integer page, Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("notLinshi", 1);

        // 1. 获取总数
        int total = nxDepartmentDisGoodsService.queryDepGoodsCount(map);
        log.info("总记录数: {}", total);

        // 2. 获取当前页数据
        map.put("purStatus", 5);
        map.put("limit", limit);
        map.put("offset", (page - 1) * limit);
        log.info("查询参数: limit={}, offset={}", limit, (page - 1) * limit);
        log.info("map查询orderordedrei999988888888888888888888888888888: {}", map);
        System.out.println("");
        List<NxDepartmentDisGoodsEntity> currentPageList = nxDepartmentDisGoodsService.queryDepDisGoodsOrdersForAi(map);
        log.info("当前页数据量: {}", currentPageList.size());

        // 4. 处理每个商品的提示文本
//        for(NxDepartmentDisGoodsEntity departmentDisGoodsEntity: currentPageList){
//             nxDepartmentDisGoodsService.getTipText(departmentDisGoodsEntity);
//        }
        // 5. 返回分页数据
        PageUtils pageUtil = new PageUtils(currentPageList, total, limit, page);
        log.info("返回: {}", pageUtil);
        return R.ok().put("page", pageUtil);
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

            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDdgDisGoodsId);
            nxDepartmentDisGoods.setNxDdgNxDistributerId(distributerGoodsEntity.getNxDgDistributerId());
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
        System.out.println("whwhwhhwhhwhwhhwh" + map);
        List<NxDistributerFatherGoodsEntity> goodsEntities = nxDepartmentDisGoodsService.depQueryDepGoodsWithOrder(map);
        if(goodsEntities.size() > 0){
            for(NxDistributerFatherGoodsEntity fatherGoodsEntity: goodsEntities){
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("grandId", fatherGoodsEntity.getNxDistributerFatherGoodsId());
                mapDep.put("depId", depId);
                mapDep.put("status", 3);
                System.out.println("baudydepdgoodos" + mapDep);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcountByDepGoods(mapDep);
                fatherGoodsEntity.setNewOrderCount(integer);
            }
        }


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
        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depFatherId);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsWithOrders(map);
        return R.ok().put("data",fatherGoodsEntities);
    }

//    @RequestMapping(value = "/disGetDepGoodsAi/{depFatherId}")
//    @ResponseBody
//    public R disGetDepGoodsAi(@PathVariable Integer depFatherId) {
//        System.out.println(depFatherId + "depeppepepepe");
//        Map<String, Object> map = new HashMap<>();
//        map.put("depFatherId", depFatherId);
//        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsWithOrders(map);
//        if(fatherGoodsEntities.size() > 0){
//            for(NxDistributerFatherGoodsEntity fatherGoodsEntity:  fatherGoodsEntities){
//                if(fatherGoodsEntity.getNxDistributerPurchaseGoodsEntities().size() > 0){
//
//                    for(NxDistributerGoodsEntity distributerGoodsEntity: fatherGoodsEntity.getNxDistributerGoodsEntities()){
//                        Integer nxDistributerGoodsId = distributerGoodsEntity.getNxDistributerGoodsId();
//                        Map<String, Object> mapD = new HashMap<>();
//                        mapD.put("disGoodsid", nxDistributerGoodsId);
//                        mapD.put("depId", depFatherId);
//                        List<NxDepartmentOrderHistoryEntity> nxDepartmentOrderHistoryEntities = historyService.queryDisHistoryOrdersByParams(mapD);
//
//
//                    }
//                }
//
//
//
//            }
//        }
//
//        return R.ok().put("data",fatherGoodsEntities);
//    }

    @RequestMapping(value = "/disGetGbDepGoods/{depFatherId}")
    @ResponseBody
    public R disGetGbDepGoods(@PathVariable Integer depFatherId) {

        Map<String, Object> map = new HashMap<>();
        map.put("gbDepFatherId", depFatherId);
        System.out.println(depFatherId + "depeppepepepemap" + map);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsWithOrders(map);
        return R.ok().put("data",fatherGoodsEntities);
    }


    @RequestMapping(value = "/disGetGbDisGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disGetGbDisGoods(Integer disId, Integer gbDisId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("gbDisId", gbDisId);
        System.out.println(gbDisId + "depeppepepepemaddddp" + map);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentDisGoodsService.queryGbDisGbDepGoods(map);
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
