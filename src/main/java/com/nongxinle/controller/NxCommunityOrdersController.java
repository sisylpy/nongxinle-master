package com.nongxinle.controller;

/**
 * @author lpy
 * @date 2020-03-22 18:07:28
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.github.wxpay.sdk.WXPay;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.nongxinle.utils.CommonUtils.generateBillTradeNo;
import static com.nongxinle.utils.DateUtils.*;


@RestController
@RequestMapping("api/nxorders")
public class NxCommunityOrdersController {
    @Autowired
    private NxCommunityOrdersService nxCommunityOrdersService;

    @Autowired
    private NxDistributerUserService nxDistributerUserService;
    @Autowired
    private NxCommunityAdsenseService nxCommunityAdsenseService;
    @Autowired
    private NxCommunityOrdersSubService nxCommunityOrdersSubService;
    @Autowired
    private NxCustomerService nxCustomerService;
    @Autowired
    private NxCommunitySplicingOrdersService nxCommunitySplicingOrdersService;
    @Autowired
    private NxCustomerUserService nxCustomerUserService;
    private static  final WebSocketEndPoint webSocketEndPoint = new WebSocketEndPoint();



    @RequestMapping(value = "/getCommOrder/{comId}")
    @ResponseBody
    public R getCommOrder(@PathVariable String comId) {
        Map<String, Object> map = new HashMap<>();
        map.put("commId", comId);
        map.put("xiaoyuStatus", 3);
        map.put("type", 0);
        System.out.println("cociicigetororororoor"+ map);
        List<NxCommunityOrdersEntity> entities = nxCommunityOrdersService.queryOrdersDetail(map);
        System.out.println("fdafsaasfdas" + entities.size());
        for(int i = 0 ; i < entities.size();i++){
            List<NxCommunityOrdersSubEntity> nxOrdersSubEntities = entities.get(0).getNxOrdersSubEntities();
            System.out.println("nxddfasdfasfas" + nxOrdersSubEntities);
//            if(entities.get(i).getNxOrdersSubEntities().size() > 0){
//                for(NxCommunityOrdersSubEntity subEntity: entities.get(i).getNxOrdersSubEntities()){
//                    System.out.println("nanna======" + subEntity.getNxCommunityGoodsEntity().getNxCgGoodsName());
//
//                }
//            }
        }

        return R.ok().put("data", entities);
    }



    @RequestMapping(value = "/delPindan/{id}")
    @ResponseBody
    public R delPindan(@PathVariable Integer id) {
        nxCommunityOrdersService.delete(id);
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        List<NxCommunitySplicingOrdersEntity> splicingOrdersEntities =  nxCommunitySplicingOrdersService.querySplicingListByParams(map);
        if(splicingOrdersEntities.size() > 0){
            for(NxCommunitySplicingOrdersEntity splicingOrdersEntity: splicingOrdersEntities){
                nxCommunitySplicingOrdersService.delete(splicingOrdersEntity.getNxCommunitySplicingOrdersId());
            }
        }
        return R.ok();
    }

    @RequestMapping(value = "/getMyPindanDetail", method = RequestMethod.POST)
    @ResponseBody
    public R getMyPindanDetail(Integer pindanId, Integer orderUserId) {

        NxCommunitySplicingOrdersEntity splicingOrdersEntity = new NxCommunitySplicingOrdersEntity();
        Map<String, Object> map = new HashMap<>();
        map.put("id", pindanId);
        NxCommunityOrdersEntity nxCommunityOrdersEntity = nxCommunityOrdersService.queryPindanDetail(map);
        if(nxCommunityOrdersEntity !=  null){
            Map<String, Object> mapO = new HashMap<>();
            mapO.put("id", pindanId);
            mapO.put("orderUserId", orderUserId);
            System.out.println("orsususuusuusuus" + mapO);
            NxCommunitySplicingOrdersEntity splicingOrders = nxCommunitySplicingOrdersService.queryNewPindan(mapO);
            if (splicingOrders == null) {
                NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(orderUserId);
                splicingOrdersEntity.setNxCsoCustomerId(userEntity.getNxCuCustomerId());
                splicingOrdersEntity.setNxCsoUserId(orderUserId);
                splicingOrdersEntity.setNxCsoCoOrderId(pindanId);
                splicingOrdersEntity.setNxCsoStatus(0);
                splicingOrdersEntity.setNxCsoYouhuiTotal("0");
                nxCommunitySplicingOrdersService.save(splicingOrdersEntity);

                splicingOrdersEntity.setOrderUser(userEntity);


                System.out.println("mappapdpdp222222" + map);
                nxCommunityOrdersEntity = nxCommunityOrdersService.queryPindanDetail(map);
                nxCommunityOrdersEntity.setOrderUserSplicingOrder(splicingOrdersEntity);

            } else {
                NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(orderUserId);
                splicingOrders.setOrderUser(userEntity);
                nxCommunityOrdersEntity.setOrderUserSplicingOrder(splicingOrders);
            }


            return R.ok().put("data", nxCommunityOrdersEntity);
        }else{
            return R.error(-1,"pindanyiquxiao");
        }


    }


    @RequestMapping(value = "/customerIndexData", method = RequestMethod.POST)
    @ResponseBody
    public R customerIndexData(Integer commId, Integer orderUserId) {

//        List<NxCommunityFatherGoodsEntity> entities = cfgService.queryCataListByCommunityId(commId);
//        List<NxCommunityFatherGoodsEntity> fatherGoodsEntities = new ArrayList<>();
//        for (NxCommunityFatherGoodsEntity father : entities) {
//            Integer nxCommunityFatherGoodsId = father.getNxCommunityFatherGoodsId();
//            List<NxCommunityPromoteEntity> nxPromoteEntities = nxCommunityPromoteService.queryPromoteByFatherId(nxCommunityFatherGoodsId);
//            father.setNxPromoteEntities(nxPromoteEntities);
//            fatherGoodsEntities.add(father);
//        }


        List<NxCommunityAdsenseEntity> adsenseEntities = nxCommunityAdsenseService.queryAdsenseByNxCommunityId(commId);
        List<NxCommunityOrdersEntity> nxCommunityOrdersEntities = new ArrayList<>();
        if (orderUserId != -1) {
            Map<String, Object> mapU = new HashMap<>();
            mapU.put("orderUserId", orderUserId);
            mapU.put("xiaoyuStatus", 3);
//            mapU.put("type", 0);
            System.out.println("mdidifuuduuududuud" + mapU);
            nxCommunityOrdersEntities = nxCommunityOrdersService.queryOrderWithUserInfo(mapU);

        }
//        List<NxCommunityOrdersEntity> friendCommunityOrdersEntities = new ArrayList<>();
//        if (orderUserId != -1) {
//            Map<String, Object> mapU = new HashMap<>();
//            mapU.put("deliveryUserId", orderUserId);
//            mapU.put("status", 2);
//            mapU.put("type", 0);
//            System.out.println("aaaaa" + mapU);
//            friendCommunityOrdersEntities = nxCommunityOrdersService.queryOrderWithUserInfo(mapU);
//
//        }
//
        NxCommunityOrdersEntity pindan = new NxCommunityOrdersEntity();
        if (orderUserId != -1) {
            Map<String, Object> mapU = new HashMap<>();
            mapU.put("deliveryUserId", orderUserId);
            mapU.put("status", 0);
            mapU.put("type", 4);
            System.out.println("pindanCommunityOrdersEntities" + mapU);
            pindan = nxCommunityOrdersService.queryPindanDetail(mapU);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("adsense", adsenseEntities);
//        map.put("deliverys", friendCommunityOrdersEntities);
        map.put("orders", nxCommunityOrdersEntities);
//        map.put("pindan", pindan);

        return R.ok().put("data", map);
    }


    @RequestMapping(value = "/deliverySavePindan", method = RequestMethod.POST)
    @ResponseBody
    public R deliverySavePindan(Integer commId, Integer deliveryUserId) {

        NxCommunityOrdersEntity ordersEntity = new NxCommunityOrdersEntity();
        ordersEntity.setNxCoUserId(deliveryUserId);
        ordersEntity.setNxCoDeliveryUserId(deliveryUserId);
        ordersEntity.setNxCoCommunityId(commId);
        ordersEntity.setNxCoType(4);
        ordersEntity.setNxCoStatus(0);
        nxCommunityOrdersService.justSave(ordersEntity);


        NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(deliveryUserId);
        NxCommunitySplicingOrdersEntity splicingOrdersEntity = new NxCommunitySplicingOrdersEntity();
        splicingOrdersEntity.setNxCsoUserId(deliveryUserId);
        splicingOrdersEntity.setNxCsoCoOrderId(ordersEntity.getNxCommunityOrdersId());
        splicingOrdersEntity.setNxCsoStatus(0);
        splicingOrdersEntity.setNxCsoCustomerId(userEntity.getNxCuCustomerId());
        splicingOrdersEntity.setNxCsoTotal("0");
        splicingOrdersEntity.setNxCsoYouhuiTotal("0");
        nxCommunitySplicingOrdersService.save(splicingOrdersEntity);
        splicingOrdersEntity.setOrderUser(userEntity);

        Map<String, Object> map = new HashMap<>();
        map.put("id", ordersEntity.getNxCommunityOrdersId());
        System.out.println("dfadfasfsa" + map);
        ordersEntity = nxCommunityOrdersService.queryPindanDetail(map);
        ordersEntity.setOrderUserSplicingOrder(splicingOrdersEntity);

        System.out.println("fdkfalskfjas;f" + ordersEntity.getAllSplicingOrders().size()) ;
        webSocketEndPoint.sendWarnToComAllUser(ordersEntity.getNxCoCommunityId().toString(), ordersEntity.getNxCommunityOrdersId().toString());
        return R.ok().put("data", ordersEntity);
    }


    /**
     * 删除订单
     *
     * @param nxOrdersId 订单id
     * @return o
     */
    @RequestMapping(value = "/deleteOrder/{nxOrdersId}")
    @ResponseBody
    public R deleteOrder(@PathVariable Integer nxOrdersId) {
        nxCommunityOrdersService.delete(nxOrdersId);


        return R.ok();
    }


    @RequestMapping(value = "cust/customerGetOrders", method = RequestMethod.POST)
    @ResponseBody
    public R customerGetOrders(Integer nxOrdersUserId, Integer page, Integer limit, Integer status) {
        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("nxOrdersUserId", nxOrdersUserId);
        map.put("status", status);
        List<NxCommunityOrdersEntity> ordersEntityList = nxCommunityOrdersService.queryCustomerOrder(map);
        int total = nxCommunityOrdersService.queryTotal(map);

        PageUtils pageUtil = new PageUtils(ordersEntityList, total, limit, page);

        return R.ok().put("page", pageUtil);

    }


    /**
     * 以下是订单接口
     */


    @RequestMapping(value = "/getIsDeliveryOrders/{deliveryUserId}")
    @ResponseBody
    public R getIsDeliveryOrders(@PathVariable Integer deliveryUserId) {
        Map<String, Object> map = new HashMap<>();
        map.put("deliveryUserId", deliveryUserId);
        map.put("status", 4);
        List<NxCommunityOrdersEntity> ordersEntityList = nxCommunityOrdersService.queryDeliveryOrder(map);

        return R.ok().put("data", ordersEntityList);
    }


    /**
     * 支付与配送接口
     *
     * @param disId       批发商id
     * @param orderStatus 支付状态 0 未支付，1 请求支付
     * @return 支付订单列表
     */
    @RequestMapping(value = "/getPaymentAndDeliveryOrder", method = RequestMethod.POST)
    @ResponseBody
    public R getPaymentAndDeliveryOrder(Integer disId, Integer orderStatus, Integer paymentStatus) {
        System.out.println("pppppp:::::" + paymentStatus);

        int noCare = -1;
        int Zero = 0;
        int One = 1;
        int Two = 2;
        int Three = 3;
        int Four = 4;


        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("orderStatus", orderStatus);

        map.put("paymentStatus", paymentStatus);
        List<NxCommunityOrdersEntity> entities = nxCommunityOrdersService.queryOrdersPaymentInformation(map);

        //未付款订单
        if (orderStatus.equals(Two) && paymentStatus.equals(Zero)) {

            return R.ok().put("data", entities);
        }
        //付款中订单
        if (orderStatus.equals(Three) && paymentStatus.equals(One)) {

            return R.ok().put("data", entities);
        }
        // 未配送订单
        if (orderStatus.equals(Three) && paymentStatus.equals(noCare)) {
            Map<String, Object> map2 = new HashMap<>();
            map2.put("disId", disId);
            map2.put("orderStatus", orderStatus);
            map2.put("paymentStatus", null);
            List<NxCommunityOrdersEntity> entities2 = nxCommunityOrdersService.queryOrdersPaymentInformation(map2);

            //带路线的订单列表
            List<Map<String, Object>> list = new ArrayList<>();


            //路线列表
//            List<NxRouteEntity> routeEntities = nxRouteService.queryRoute();
//
//            for (NxRouteEntity route : routeEntities) {
//                //一个路线的订单map
//                Map<String, Object> map1 = new HashMap<>();
//                //新路线订单列表
//                List<NxCommunityOrdersEntity> entityList = new ArrayList<>();
//
//                String nxRouteName = route.getNxRouteName();
//                map1.put("route", nxRouteName);
//                for (NxCommunityOrdersEntity order : entities2) {
//                    System.out.println("orderkkkkccc" + order.getNxCustomerEntity().getNxCommunityEntity());
//
//                    if (order.getNxCustomerEntity().getNxCommunityEntity().getNxCommunityRouteId().equals(route.getNxRouteId())) {
//                        entityList.add(order);
//                    }
//                }
//                map1.put("arr", entityList);
//                if (entityList.size() > 0) {
//                    list.add(map1);
//                }
//            }

            return R.ok().put("data", entities2);
        }
        // 配送中订单
        if (orderStatus.equals(Four) && paymentStatus.equals(noCare)) {

            map.put("paymentStatus", null);
            List<NxCommunityOrdersEntity> entities3 = nxCommunityOrdersService.queryOrdersPaymentInformation(map);

            Set<NxDistributerUserEntity> userEntitySet = new TreeSet<>();

            for (NxCommunityOrdersEntity order : entities3) {
                Integer nxOrdersDeliveryUserId = order.getNxCoDeliveryUserId();
                System.out.println(order);
                System.out.println("ididiid::::" + nxOrdersDeliveryUserId);
                NxDistributerUserEntity nxDistributerUserEntity = nxDistributerUserService.queryObject(nxOrdersDeliveryUserId);
                System.out.println("sss:" + nxDistributerUserEntity);

                userEntitySet.add(nxDistributerUserEntity);
            }


            return R.ok().put("data", userEntitySet);
        }

        return R.ok().put("data", "no");


    }


    /**
     * 称重中
     *
     * @param disId 批发商id
     * @return 称重中订单
     */
    @RequestMapping(value = "/getWeighingOrder/{disId}")
    @ResponseBody
    public R getWeighingOrder(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 1);
        List<NxCommunityOrdersEntity> entities = nxCommunityOrdersService.queryOrdersDetail(map);

        return R.ok().put("data", entities);
    }

    /**
     * 未称重
     *
     * @param disId 批发商id
     * @return 未称重订单
     */
    @RequestMapping(value = "/getUnWeightOrder", method = RequestMethod.POST)
    @ResponseBody
    public R getOrderList(Integer disId, String serviceDate) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("serviceDate", serviceDate);
        map.put("status", 0);
        List<NxCommunityOrdersEntity> entities = nxCommunityOrdersService.queryOrdersDetail(map);
        return R.ok().put("data", entities);
    }


    /**
     * 以下称重接口
     * <p>
     * /**
     * 提交称重
     *
     * @param arr 订单
     * @return 成功
     */
    @RequestMapping(value = "/saveSubOrderWeight", method = RequestMethod.POST)
    @ResponseBody
    public R saveSubOrderWeight(@RequestBody NxCommunityOrdersEntity arr) {
        System.out.println("arrr" + arr);
        nxCommunityOrdersService.updateSub(arr);

//		JSONArray jsonArray = JSONArray.fromObject(arr);
//		System.out.println("00000");
//		for (Object obj: jsonArray){
//			System.out.println("11111");
//			JSONObject jsonObject2 = JSONObject.fromObject(obj);
//			NxCommunityOrdersSubEntity subEntity = (NxCommunityOrdersSubEntity) JSONObject.toBean(jsonObject2, NxCommunityOrdersSubEntity.class);
//			subEntity.setNxOsStatus(1);
////			nxCommunityOrdersSubService.update(subEntity);
//
//		}


        return R.ok();
    }


    /**
     * 获取订单详细
     *
     * @param orderId 订单ids
     * @return 订单
     */
    @RequestMapping(value = "/getOrderDetail/{orderId}")
    @ResponseBody
    public R getOrderDetail(@PathVariable Integer orderId) {

        NxCommunityOrdersEntity ordersEntity = nxCommunityOrdersService.queryObject(orderId);
        return R.ok().put("data", ordersEntity);
    }

    @RequestMapping(value = "/getOrderDetailPindan/{orderId}")
    @ResponseBody
    public R getOrderDetailPindan(@PathVariable Integer orderId) {

        Map<String, Object> map = new HashMap<>();
        map.put("id", orderId);
        NxCommunityOrdersEntity ordersEntity = nxCommunityOrdersService.queryPindanDetail(map);
        return R.ok().put("data", ordersEntity);
    }


    /**
     * 获取拣货员的订单
     *
     * @param pickUserId 拣货id
     * @param status     订单状态
     * @return
     */
    @RequestMapping(value = "/getOrdersToWeigh", method = RequestMethod.POST)
    @ResponseBody
    public R getOrdersToWeigh(Integer pickUserId, Integer status) {
        System.out.println(pickUserId);
        Map<String, Object> map = new HashMap<>();
        map.put("pickerUserId", pickUserId);
        map.put("status", status);

        List<NxCommunityOrdersEntity> entities = nxCommunityOrdersService.queryOrdersToWeigh(map);

        return R.ok().put("data", entities);
    }


    /**
     * 批发商首页接口
     *
     * @param disId 批发商id
     * @return 分配拣货员列表
     */
//    @RequestMapping(value = "/disGetIndexData/{disId}")
//    @ResponseBody
//    public R disGetIndexData(@PathVariable Integer disId) {
//        Map<String, Object> list = nxCommunityOrdersService.queryDistributerIndexData(disId);
//        return R.ok().put("data", list);
//    }


    /**
     * 配送商分配称重客户给拣货员
     *
     * @param deliveryUserId 拣货员id
     * @param ordersEntities 客户订单id
     * @return 0
     */
    @RequestMapping(value = "/distributionDelivery", method = RequestMethod.POST)
    @ResponseBody
    public R distributionDelivery(Integer deliveryUserId, String ordersEntities) {


        JSONArray jsonArray = JSONArray.fromObject(ordersEntities);
        System.out.println("00000");
        for (Object obj : jsonArray) {
            JSONObject jsonObject2 = JSONObject.fromObject(obj);
            NxCommunityOrdersEntity ordersEntity = (NxCommunityOrdersEntity) JSONObject.toBean(jsonObject2, NxCommunityOrdersEntity.class);

            ordersEntity.setNxCoStatus(4);
            ordersEntity.setNxCoDeliveryUserId(deliveryUserId);
            nxCommunityOrdersService.update(ordersEntity);

        }


//		 String replace = ordersEntities.replace("[", "");
//		 String replace1 = replace.replace("]", "");
//
//		 String[] split = replace1.split(",");
//		 for (String str : split) {
//			 System.out.println(str);
//			 String replace2 = str.replace("\"", "");
//			 NxCommunityOrdersEntity ordersEntity = new NxCommunityOrdersEntity();
//			 Integer i = null;
//			 if(str != null){
//				i = Integer.valueOf(replace2);
//				 ordersEntity.setNxOrdersId(i);
//				 ordersEntity.setNxOrdersStatus(1);
//				 ordersEntity.setNxOrdersWeighUserId(pickUserId);
//				 nxCommunityOrdersService.update(ordersEntity);
//			 }
//		 }
        return R.ok();
    }


    @ResponseBody
    @RequestMapping(value = "/customerCashPayPindan", method = RequestMethod.POST)
    public R customerCashPayPindan(@RequestBody NxCommunityOrdersEntity nxOrders) {


        System.out.println(nxOrders);
        String memberTime = "-1";

        if (nxOrders.getAllSplicingOrders().size() > 0) {
            nxOrders.setNxCoDate(formatWhatDayTime(0));
            nxOrders.setNxCoStatus(0);
            nxOrders.setNxCoPaymentStatus(0);
            nxOrders.setNxCoWeighNumber(generateBillTradeNo(nxOrders.getNxCoCommunityId().toString()));
            nxCommunityOrdersService.update(nxOrders);
        }

        List<NxCommunitySplicingOrdersEntity> allSplicingOrders = nxOrders.getAllSplicingOrders();
        if(allSplicingOrders.size() > 0){
            for(NxCommunitySplicingOrdersEntity splicingOrdersEntity: allSplicingOrders){

                splicingOrdersEntity.setNxCsoDate(formatWhatDayTime(0));
                splicingOrdersEntity.setNxCsoPaymentStatus(0);
                if (splicingOrdersEntity.getNxCoBuyMemberOrderTime() > 0) {
                    String s = formatWhatDay(splicingOrdersEntity.getNxCoBuyMemberOrderTime() * 30);
                    memberTime = s;
                    Integer nxCoCustomerId = splicingOrdersEntity.getNxCsoCustomerId();
                    NxCustomerEntity nxCustomerEntity = nxCustomerService.queryObject(nxCoCustomerId);
                    nxCustomerEntity.setNxCustomerCardWasteDate(s);
                    nxCustomerService.update(nxCustomerEntity);
                    System.out.println("ndnnndndnnd" + nxCustomerEntity.getNxCustomerCardWasteDate());
                }

            }
        }




        MyWxShixianliliPayConfig config = new MyWxShixianliliPayConfig();

        String nxRbTotal = nxOrders.getNxCoTotal();
        Double aDouble = Double.parseDouble(nxRbTotal) * 100;
        int i = aDouble.intValue();
        System.out.println("dfdafkdaksfas" + nxOrders.getNxCoTotal());
        String s1 = String.valueOf(i);
        String tradeNo = CommonUtils.generateOutTradeNo();
        SortedMap<String, String> params = new TreeMap<>();
        params.put("appid", config.getAppID());
        params.put("mch_id", config.getMchID());
        params.put("nonce_str", CommonUtils.generateUUID());
        params.put("body", "订单支付");
        params.put("out_trade_no", tradeNo);
        params.put("fee_type", "CNY");
        params.put("total_fee", s1);
        params.put("spbill_create_ip", "101.42.222.149");
        params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/nxorders/notify");
        params.put("trade_type", "JSAPI");
        params.put("openid", nxOrders.getNxCoUserOpenId());

        //map转xml
        try {
            WXPay wxpay = new WXPay(config);
            long time = System.currentTimeMillis();
            String tString = String.valueOf(time / 1000);
            Map<String, String> resp = wxpay.unifiedOrder(params);
            System.out.println(resp);
            System.out.println("enennenen3333");
            SortedMap<String, String> reMap = new TreeMap<>();
            reMap.put("appId", config.getAppID());
            reMap.put("nonceStr", resp.get("nonce_str"));
            reMap.put("package", "prepay_id=" + resp.get("prepay_id"));
            reMap.put("signType", "MD5");
            reMap.put("timeStamp", tString);
            String s = WxPayUtils.creatSign(reMap, config.getKey());
            reMap.put("paySign", s);

            nxOrders.setNxCoPaymentStatus(1);
            nxOrders.setNxCoWxOutTradeNo(tradeNo);
            nxCommunityOrdersService.update(nxOrders);

            Map<String, Object> map = new HashMap<>();
            map.put("id",nxOrders.getNxCommunityOrdersId() );

            List<NxCommunitySplicingOrdersEntity> nxCommunitySplicingOrdersEntities = nxCommunitySplicingOrdersService.querySplicingListByParams(map);
            if (nxCommunitySplicingOrdersEntities.size() > 0) {
                for (NxCommunitySplicingOrdersEntity splicingOrdersEntity : nxCommunitySplicingOrdersEntities) {

                    splicingOrdersEntity.setNxCsoPaymentStatus(1);
                    splicingOrdersEntity.setNxCsoWxOutTradeNo(tradeNo);
                    nxCommunitySplicingOrdersService.update(splicingOrdersEntity);

                    List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = splicingOrdersEntity.getNxCommunityOrdersSubEntities();
                    if(nxCommunityOrdersSubEntities.size() > 0){
                        for(NxCommunityOrdersSubEntity subEntity: nxCommunityOrdersSubEntities){
                            subEntity.setNxCosBuyStatus(3);
                            subEntity.setNxCosStatus(3);
                            nxCommunityOrdersSubService.update(subEntity);
                        }
                    }
                }
            }
            reMap.put("orderId", nxOrders.getNxCommunityOrdersId().toString());
            reMap.put("memberTime", memberTime);

            return R.ok().put("map", reMap);


        } catch (Exception e) {
            e.printStackTrace();
        }


        return R.ok();

    }


    @ResponseBody
    @RequestMapping(value = "/customerCashPay", method = RequestMethod.POST)
    public R customerCashPay(@RequestBody NxCommunityOrdersEntity nxOrders) {
        System.out.println(nxOrders);
        String memberTime = "-1";

        if (nxOrders.getNxOrdersSubEntities().size() > 0) {
            nxOrders.setNxCoDate(formatWhatDayTime(0));
            nxOrders.setNxCoStatus(0);
            nxOrders.setNxCoPaymentStatus(0);
            nxOrders.setNxCoWeighNumber(generateBillTradeNo(nxOrders.getNxCoCommunityId().toString()));
            nxCommunityOrdersService.save(nxOrders);
        }

        if (nxOrders.getNxCoBuyMemberOrderTime() > 0) {
            String s = formatWhatDay(nxOrders.getNxCoBuyMemberOrderTime() * 30);
            memberTime = s;
            Integer nxCoCustomerId = nxOrders.getNxCoCustomerId();
            NxCustomerEntity nxCustomerEntity = nxCustomerService.queryObject(nxCoCustomerId);
            nxCustomerEntity.setNxCustomerCardWasteDate(s);
            nxCustomerService.update(nxCustomerEntity);
            System.out.println("ndnnndndnnd" + nxCustomerEntity.getNxCustomerCardWasteDate());
        }


        MyWxShixianliliPayConfig config = new MyWxShixianliliPayConfig();

        String nxRbTotal = nxOrders.getNxCoTotal();
        Double aDouble = Double.parseDouble(nxRbTotal) * 100;
        int i = aDouble.intValue();
        System.out.println("dfdafkdaksfas" + nxOrders.getNxCoTotal());
        String s1 = String.valueOf(i);
        String tradeNo = CommonUtils.generateOutTradeNo();
        SortedMap<String, String> params = new TreeMap<>();
        params.put("appid", config.getAppID());
        params.put("mch_id", config.getMchID());
        params.put("nonce_str", CommonUtils.generateUUID());
        params.put("body", "订单支付");
        params.put("out_trade_no", tradeNo);
        params.put("fee_type", "CNY");
        params.put("total_fee", s1);
        params.put("spbill_create_ip", "101.42.222.149");
        params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/nxorders/notify");
        params.put("trade_type", "JSAPI");
        params.put("openid", nxOrders.getNxCoUserOpenId());

        //map转xml
        try {
            WXPay wxpay = new WXPay(config);
            long time = System.currentTimeMillis();
            String tString = String.valueOf(time / 1000);
            Map<String, String> resp = wxpay.unifiedOrder(params);
            System.out.println(resp);
            System.out.println("enennenen3333");
            SortedMap<String, String> reMap = new TreeMap<>();
            reMap.put("appId", config.getAppID());
            reMap.put("nonceStr", resp.get("nonce_str"));
            reMap.put("package", "prepay_id=" + resp.get("prepay_id"));
            reMap.put("signType", "MD5");
            reMap.put("timeStamp", tString);
            String s = WxPayUtils.creatSign(reMap, config.getKey());
            reMap.put("paySign", s);

            System.out.println("ordupddiidididi" + nxOrders.getNxCommunityOrdersId());
            nxOrders.setNxCoPaymentStatus(1);
            nxOrders.setNxCoWxOutTradeNo(tradeNo);
            nxCommunityOrdersService.update(nxOrders);

            Map<String, Object> map = new HashMap<>();
            map.put("orderId",nxOrders.getNxCommunityOrdersId() );

            List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
            if (nxCommunityOrdersSubEntities.size() > 0) {
                for (NxCommunityOrdersSubEntity subEntity : nxCommunityOrdersSubEntities) {
                    subEntity.setNxCosBuyStatus(3);
                    subEntity.setNxCosStatus(3);
                    nxCommunityOrdersSubService.update(subEntity);
                }
            }
            reMap.put("orderId", nxOrders.getNxCommunityOrdersId().toString());
            reMap.put("memberTime", memberTime);

            return R.ok().put("map", reMap);


        } catch (Exception e) {
            e.printStackTrace();
        }


        return R.ok();

    }


//
//    @ResponseBody
//    @RequestMapping(value = "/customerCashPay",method = RequestMethod.POST)
//    public R customerCashPay(@RequestBody NxDepartmentBillEntity billEntity) {
//        System.out.println("billl" + billEntity);
//
//        //转换总金额
//        String nxRbTotal = billEntity.getNxDbTotal();
//        Double aDouble = Double.parseDouble(nxRbTotal) * 100;
//        int i = aDouble.intValue();
//        String s1 = String.valueOf(i);
//
//        //订单号
//        String tradeNo = CommonUtils.generateOutTradeNo();
//        //餐馆支付配置
//        MyWxShixianliliPayConfig config = new MyWxShixianliliPayConfig();
//        SortedMap<String, String> params = new TreeMap<>();
//        params.put("appid", config.getAppID());
//        params.put("mch_id", config.getMchID());
//        params.put("nonce_str", CommonUtils.generateUUID());
//        params.put("body",  "订单支付");
//        params.put("out_trade_no", tradeNo);
//        params.put("fee_type", "CNY");
//        params.put("total_fee", s1);
//        params.put("spbill_create_ip", "101.42.222.149");
//        params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/nxdepartmentbill/notify");
//        params.put("trade_type", "JSAPI");
//        params.put("openid", billEntity.getNxUserOpenId());
//
//        //map转xml
//        try {
//
//            WXPay wxpay = new WXPay(config);
//            long time = System.currentTimeMillis();
//            String tString = String.valueOf(time/1000);
//            Map<String, String> resp = wxpay.unifiedOrder(params);
//            System.out.println(resp);
//            SortedMap<String, String> reMap = new TreeMap<>();
//            reMap.put("appId", config.getAppID());
//            reMap.put("nonceStr", resp.get("nonce_str"));
//            reMap.put("package", "prepay_id=" + resp.get("prepay_id"));
//            reMap.put("signType", "MD5");
//            reMap.put("timeStamp", tString);
//            String s = WxPayUtils.creatSign(reMap, config.getKey());
//            reMap.put("paySign", s);
//
//            billEntity.setNxDbWxOutTradeNo(tradeNo);
//            nxDepartmentBillService.update(billEntity);
//
//            return R.ok().put("map", reMap);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return R.ok();
//    }


    /**
     * @Title: callBack
     * @Description: 支付完成的回调函数
     * @param:
     * @return:
     */
    @RequestMapping("/notify")
    public String callBack(HttpServletRequest request, HttpServletResponse response) {
        // System.out.println("微信支付成功,微信发送的callback信息,请注意修改订单信息");
        InputStream is = null;
        try {

            is = request.getInputStream();// 获取请求的流信息(这里是微信发的xml格式所有只能使用流来读)
            String xml = WxPayUtils.InputStream2String(is);
            Map<String, String> notifyMap = WxPayUtils.xmlToMap(xml);// 将微信发的xml转map
            System.out.println("微信返回给回调函数的信息为：" + xml);
            if (notifyMap.get("result_code").equals("SUCCESS")) {
                /*
                 * 以下是自己的业务处理------仅做参考 更新order对应字段/已支付金额/状态码
                 * 更新bill支付状态
                 */
                System.out.println("===notify===回调方法已经被调！！！");
                String ordersSn = notifyMap.get("out_trade_no");// 商户订单号
                NxCommunityOrdersEntity billEntity = nxCommunityOrdersService.queryOrderByTradeNo(ordersSn);
                billEntity.setNxCoStatus(2);
                nxCommunityOrdersService.update(billEntity);

                webSocketEndPoint.sendWarnToComAllUser(billEntity.getNxCoCommunityId().toString(), billEntity.getNxCommunityOrdersId().toString());
            }

            // 告诉微信服务器收到信息了，不要在调用回调action了========这里很重要回复微信服务器信息用流发送一个xml即可
            response.getWriter().write("<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }


    /**
     * wait
     * 列表
     */
    @ResponseBody
    @RequestMapping("/list")
    @RequiresPermissions("nxorders:list")
    public R list(Integer page, Integer limit) {
        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);

        //查询列表数据
        List<NxCommunityOrdersEntity> nxOrdersList = nxCommunityOrdersService.queryList(map);
        int total = nxCommunityOrdersService.queryTotal(map);

        PageUtils pageUtil = new PageUtils(nxOrdersList, total, limit, page);

        return R.ok().put("page", pageUtil);
    }


    /**
     * wait
     * 信息
     */
    @ResponseBody
    @RequestMapping("/info/{nxOrdersId}")
    @RequiresPermissions("nxorders:info")
    public R info(@PathVariable("nxOrdersId") Integer nxOrdersId) {
        NxCommunityOrdersEntity nxOrders = nxCommunityOrdersService.queryObject(nxOrdersId);

        return R.ok().put("nxOrders", nxOrders);
    }


    /**
     * wait
     * 修改
     */
    @ResponseBody
    @RequestMapping("/update")
    @RequiresPermissions("nxorders:update")
    public R update(@RequestBody NxCommunityOrdersEntity nxOrders) {
        nxCommunityOrdersService.update(nxOrders);

        return R.ok();
    }

    /**
     * wait
     * 删除
     */
    @ResponseBody
    @RequestMapping("/delete")
    @RequiresPermissions("nxorders:delete")
    public R delete(@RequestBody Integer[] nxOrdersIds) {
        nxCommunityOrdersService.deleteBatch(nxOrdersIds);

        return R.ok();
    }

}
