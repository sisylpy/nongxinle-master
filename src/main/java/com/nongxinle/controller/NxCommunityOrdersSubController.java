
package com.nongxinle.controller;

/**
 * @author lpy
 * @date 2020-03-22 18:07:28
 */


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.DadaSignUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.R;

import javax.servlet.http.HttpServletRequest;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.NxCommunityTypeUtils.*;


@RestController
@RequestMapping("api/nxorderssub")
public class NxCommunityOrdersSubController {
    @Autowired
    private NxCommunityOrdersSubService nxCommunityOrdersSubService;
    @Autowired
    private NxCommunityGoodsService nxCommunityGoodsService;
    @Autowired
    private NxCommunityOrdersService nxCommunityOrdersService;
    @Autowired
    private NxCustomerUserCouponService nxCustomerUserCouponService;
    @Autowired
    private NxCommunityCardService nxCommunityCardService;
    @Autowired
    private NxCustomerUserCardService nxCustomerUserCardService;
    @Autowired
    private NxCommunitySplicingOrdersService nxCommunitySplicingOrdersService;
    @Autowired
    private NxECommerceCommunityService nxECommerceCommunityService;
    @Autowired
    private NxCommunityService nxCommunityService;
    @Autowired
    private NxCustomerUserAddressService nxCustomerUserAddressService;
    @Autowired
    private NxCustomerUserService nxCustomerUserService;
    @Autowired
    private NxCommunityDeskService nxCommunityDeskService;


    private static String appKey = "dada43105baba0717e2";
    private static String appSecret = "c36d1c12cb549a911458601c12e221bf";
    private static String sourceId = "73753";
    private static String shopNo = "887737-22356381";

    public static final String URL = "http://api.feieyun.cn/Api/Open/";//不需要修改

    public static final String USER = "454926763@qq.com";//*必填*：账号名
    public static final String UKEY = "bDI55xfWTv5Fu6KU";//*必填*: 飞鹅云后台注册账号后生成的UKEY 【备注：这不是填打印机的KEY】
    public static final String SN = "924610660";//*必填*：打印机编号，必须要在管理后台里添加打印机或调用API接口添加之后，才能调用API



    @RequestMapping(value = "/editOrderSubOrders", method = RequestMethod.POST)
    @ResponseBody
    public R editOrderSubOrders(Integer orderType, Integer nxOrdersId) {
        if (orderType == 1) {
            Map<String, Object> mapS = new HashMap<>();
            mapS.put("id", nxOrdersId);
            List<NxCommunitySplicingOrdersEntity> nxCommunitySplicingOrdersEntities = nxCommunitySplicingOrdersService.querySplicingListByParams(mapS);
            if (nxCommunitySplicingOrdersEntities.size() > 0) {
                for (NxCommunitySplicingOrdersEntity splicingOrdersEntity : nxCommunitySplicingOrdersEntities) {
                    splicingOrdersEntity.setNxCsoStatus(2);
                    nxCommunitySplicingOrdersService.update(splicingOrdersEntity);

                    Map<String, Object> map = new HashMap<>();
                    map.put("splicingOrderId", splicingOrdersEntity.getNxCommunitySplicingOrdersId());
                    List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
                    if (subEntities.size() > 0) {
                        for (NxCommunityOrdersSubEntity subEntity : subEntities) {
                            subEntity.setNxCosStatus(-1);
                            subEntity.setNxCosOrdersId(null);
                            nxCommunityOrdersSubService.update(subEntity);
                        }
                    }
                }
            }

            NxCommunityOrdersEntity ordersEntity = nxCommunityOrdersService.queryObject(nxOrdersId);
            ordersEntity.setNxCoDate(null);
            ordersEntity.setNxCoStatus(-1);
            ordersEntity.setNxCoWeighNumber(null);
            ordersEntity.setNxCoServiceTime(null);
            ordersEntity.setNxCoDate(null);
            nxCommunityOrdersService.update(ordersEntity);
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", nxOrdersId);
            System.out.println("zheicororororye" + orderType);
            List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
            if (subEntities.size() > 0) {
                for (NxCommunityOrdersSubEntity subEntity : subEntities) {
                    subEntity.setNxCosStatus(-1);
                    subEntity.setNxCosOrdersId(null);
                    nxCommunityOrdersSubService.update(subEntity);
                }
            }
            nxCommunityOrdersService.delete(nxOrdersId);
        }
        return R.ok();
    }

    @RequestMapping(value = "/checkPindanAdsenseGoodsSubOrders", method = RequestMethod.POST)
    @ResponseBody
    public R checkPindanAdsenseGoodsSubOrders(Integer pindanId) {
        System.out.println("checkckckkckckkcc");
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", pindanId);
        List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
        if (subEntities.size() > 0) {
            for (NxCommunityOrdersSubEntity subEntity : subEntities) {

                Integer nxCosCommunityGoodsId = subEntity.getNxCosCommunityGoodsId();
                NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(nxCosCommunityGoodsId);
                if (goodsEntity.getNxCgIsOpenAdsense() == 1) {
                    BigDecimal orderOrderQuantity = new BigDecimal(subEntity.getNxCosQuantity());
                    int wxCountAuto = new BigDecimal(goodsEntity.getNxCgAdsenseRestQuantity()).compareTo(orderOrderQuantity);
                    System.out.println("duibdididiididi==wxCountAuto===" + wxCountAuto + "goensresq=" + goodsEntity.getNxCgAdsenseRestQuantity() + "oroeqnn" + orderOrderQuantity);
                    int nowMinute = getNowMinute();
                    int nxCgAdsenseStartTimeZone = Integer.parseInt(goodsEntity.getNxCgAdsenseStartTimeZone());
                    int nxCgAdsenseStopTimeZone = Integer.parseInt(goodsEntity.getNxCgAdsenseStopTimeZone());

                    if (wxCountAuto < 0 && nowMinute > nxCgAdsenseStartTimeZone && nowMinute < nxCgAdsenseStopTimeZone) {
                        return R.error(-1, goodsEntity.getNxCgGoodsName() + "剩余库存不足");
                    }
                }

            }
        }

        return R.ok();
    }

    @RequestMapping(value = "/checkAdsenseGoodsSubOrders", method = RequestMethod.POST)
    @ResponseBody
    public R checkAdsenseGoodsSubOrders(@RequestBody List<NxCommunityOrdersSubEntity> subList) {
        System.out.println("checkckckkckckkcc");
        for (NxCommunityOrdersSubEntity subEntity : subList) {

            Integer nxCosCommunityGoodsId = subEntity.getNxCosCommunityGoodsId();
            NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(nxCosCommunityGoodsId);
            if (goodsEntity.getNxCgIsOpenAdsense() == 1) {
                BigDecimal orderOrderQuantity = new BigDecimal(subEntity.getNxCosQuantity());
                int wxCountAuto = new BigDecimal(goodsEntity.getNxCgAdsenseRestQuantity()).compareTo(orderOrderQuantity);
                System.out.println("duibdididiididi==wxCountAuto===" + wxCountAuto + "goensresq=" + goodsEntity.getNxCgAdsenseRestQuantity() + "oroeqnn" + orderOrderQuantity);
                int nowMinute = getNowMinute();
                int nxCgAdsenseStartTimeZone = Integer.parseInt(goodsEntity.getNxCgAdsenseStartTimeZone());
                int nxCgAdsenseStopTimeZone = Integer.parseInt(goodsEntity.getNxCgAdsenseStopTimeZone());

                if (wxCountAuto < 0 && nowMinute > nxCgAdsenseStartTimeZone && nowMinute < nxCgAdsenseStopTimeZone) {
                    return R.error(-1, goodsEntity.getNxCgGoodsName() + "剩余库存不足");
                }
            }

        }
        return R.ok();
    }


    @RequestMapping(value = "/printSubOrders", method = RequestMethod.POST)
    @ResponseBody
    public R printSubOrders(Integer subOrderId, Integer status) {

        NxCommunityOrdersSubEntity subEntity = nxCommunityOrdersSubService.queryObject(subOrderId);
        subEntity.setNxCosStatus(status);
        subEntity.setNxCosPrintTime(formatFullTime());
        System.out.println("printSubOrdersprintSubOrders" + formatWhatTime(0) + "orderid=" + subEntity.getNxCommunityOrdersSubId());
//        System.out.println("orderid=" + subEntity.getNxCommunityOrdersSubId() + "timemeemmelog111" + testPrintTime());
        nxCommunityOrdersSubService.update(subEntity);

        Integer nxCosOrdersId = subEntity.getNxCosOrdersId();
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", nxCosOrdersId);
        map.put("status", 1);
        System.out.println("orderid=" + subEntity.getNxCommunityOrdersSubId() + "ordsusssnummmmmmmmmmm" + map);
        List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
        System.out.println("sisisisiisisisisiziziz" + subEntities.size());
        if (subEntities.size() == 0) {
            NxCommunityOrdersEntity nxCommunityOrdersEntity = nxCommunityOrdersService.queryObject(nxCosOrdersId);
            nxCommunityOrdersEntity.setNxCoStatus(3);
            nxCommunityOrdersService.update(nxCommunityOrdersEntity);
        }
        return R.ok();
    }

    @RequestMapping(value = "/unLock", method = RequestMethod.POST)
    @ResponseBody
    public R unLock ( Integer commId, Integer status ) {
        Map<String, Object> map = new HashMap<>();
        map.put("commId", commId);
        map.put("status", 9);
        List<NxCommunityPrintOrdersSubEntity> subEntities = nxCommunityOrdersSubService.queryPrintSubOrders(map);
        if(subEntities.size() > 0){
            NxCommunityOrdersSubEntity entity = null;
            for(NxCommunityPrintOrdersSubEntity sub: subEntities){
                sub.setNxCospStatus(status);
                entity = new NxCommunityOrdersSubEntity();
                entity.setNxCommunityOrdersSubId(sub.getNxCommunityOrdersPrintSubId());
                entity.setNxCosStatus(status); //锁定状态
                nxCommunityOrdersSubService.update(entity);
            }
        }

        return R.ok().put("data", subEntities);
    }
    @RequestMapping(value = "/getLockPrintSubOrders", method = RequestMethod.POST)
    @ResponseBody
    public R getLockPrintSubOrders ( Integer commId, Integer status ) {
        Map<String, Object> map = new HashMap<>();
        map.put("commId", commId);
        map.put("status", 9);
        List<NxCommunityPrintOrdersSubEntity> subEntities = nxCommunityOrdersSubService.queryPrintSubOrders(map);

        return R.ok().put("data", subEntities);
    }

    @RequestMapping(value = "/getUnPrintSubOrders", method = RequestMethod.POST)
    @ResponseBody
    public R getUnPrintSubOrders(Integer commId, Integer status) {

        Map<String, Object> map = new HashMap<>();
        map.put("commId", commId);
        map.put("status", status);
        map.put("service", 0);
        System.out.println("mappaaunns" + map);
        List<NxCommunityPrintOrdersSubEntity> subEntities = nxCommunityOrdersSubService.queryPrintSubOrders(map);


        NxCommunityEntity communityEntity = nxCommunityService.queryObject(commId);
        String nxCommunityPrePrintTimes = communityEntity.getNxCommunityPrePrintTimes();
        Integer integer = Integer.valueOf(nxCommunityPrePrintTimes);
        map.put("service", 1);
        map.put("date", formatWhatDay(0));
        map.put("time", formatWhatDayMinute(integer));
        System.out.println("abcckckckckc" + map);
        List<NxCommunityPrintOrdersSubEntity> subEntities1 = nxCommunityOrdersSubService.queryPrintSubOrders(map);
        subEntities.addAll(subEntities1);

        NxCommunityOrdersSubEntity entity = null;
        for (NxCommunityPrintOrdersSubEntity pEntity : subEntities) {
            entity = new NxCommunityOrdersSubEntity();
            entity.setNxCommunityOrdersSubId(pEntity.getNxCommunityOrdersPrintSubId());
            entity.setNxCosStatus(9); //锁定状态
            nxCommunityOrdersSubService.update(entity);
        }


        return R.ok().put("data", subEntities);
    }


    @RequestMapping(value = "/changeMemberCard", method = RequestMethod.POST)
    @ResponseBody
    public R changeMemberCard(Integer orderUserId, Integer status, Integer orderType, Integer userCardId,
                              Integer cardId, Integer serviceType) {

        if (status == 1) {
            Map<String, Object> map = new HashMap<>();
            map.put("orderUserId", orderUserId);
            map.put("orderType", orderType);
            map.put("status", -1);
            map.put("dayuDiffPrice", 0);
            map.put("cardId", cardId);
            map.put("serviceType", serviceType);
            //查询
            List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
            //status = 1 ,则取消会员卡功能
            if (subEntities.size() > 0) {
                for (NxCommunityOrdersSubEntity subEntity : subEntities) {
                    //先查是否有一样的没有优惠价格的订单

                    Integer nxCosCommunityGoodsId = subEntity.getNxCosCommunityGoodsId();
                    Map<String, Object> mapSame = new HashMap<>();
                    mapSame.put("orderUserId", orderUserId);
                    mapSame.put("orderType", orderType);
                    mapSame.put("status", -1);
                    mapSame.put("diffPrice", 0);
                    mapSame.put("serviceType", serviceType);
                    mapSame.put("goodsId", nxCosCommunityGoodsId);
                    if (subEntity.getNxCosRemark() != null) {
                        mapSame.put("remark", subEntity.getNxCosRemark());
                    }
                    NxCommunityOrdersSubEntity sameGoodsOrder = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapSame);
                    if (sameGoodsOrder != null) {
                        //jiashulaing
                        BigDecimal changeQuantity = new BigDecimal(subEntity.getNxCosQuantity());
                        BigDecimal add = new BigDecimal(sameGoodsOrder.getNxCosQuantity()).add(changeQuantity);
                        BigDecimal decimal = new BigDecimal(sameGoodsOrder.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                        sameGoodsOrder.setNxCosQuantity(add.toString());
                        sameGoodsOrder.setNxCosSubtotal(decimal.toString());
                        nxCommunityOrdersSubService.update(sameGoodsOrder);

                        nxCommunityOrdersSubService.delete(subEntity.getNxCommunityOrdersSubId());

                    } else {
                        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(nxCosCommunityGoodsId);
                        subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
//                        BigDecimal orderQuantity = new BigDecimal(subEntity.getNxCosQuantity());
                        Map<String, Object> mapOrderQuantity = new HashMap<>();
                        mapOrderQuantity.put("date", formatWhatDay(0));
                        mapOrderQuantity.put("orderUserId", orderUserId);
                        mapOrderQuantity.put("goodsId", subEntity.getNxCosCommunityGoodsId());
                        int  orderQuantityInt =  nxCommunityOrdersSubService.queryTodayHuaxianCount(mapOrderQuantity);
                        BigDecimal orderQuantity = new BigDecimal(orderQuantityInt);
                        BigDecimal decimal = new BigDecimal(goodsEntity.getNxCgGoodsHuaxianPrice()).multiply(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                        subEntity.setNxCosSubtotal(decimal.toString());
                        subEntity.setNxCosHuaxianDifferentPrice("0");
                        nxCommunityOrdersSubService.update(subEntity);

                    }
                }
            }

            //xiugai
            NxCustomerUserCardEntity userCardEntity = nxCustomerUserCardService.queryObject(userCardId);
            userCardEntity.setNxCucaIsSelected(0);
            nxCustomerUserCardService.update(userCardEntity);


        }


        //
        if (status == 0) {
            Map<String, Object> map = new HashMap<>();
            map.put("orderUserId", orderUserId);
            map.put("orderType", orderType);
            map.put("status", -1);
            map.put("diffPrice", 0);
            map.put("cardId", cardId);
            map.put("serviceType", serviceType);
            System.out.println("stssssssss00000000" + map);
            //查询
            List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
            //status = 1 ,则取消会员卡功能
            if (subEntities.size() > 0) {
                for (NxCommunityOrdersSubEntity subEntity : subEntities) {
                    //先查是否有一样的没有优惠价格的订单
                    Integer nxCosCommunityGoodsId = subEntity.getNxCosCommunityGoodsId();
                    NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(nxCosCommunityGoodsId);
                    //只有划线商品的订单才改动
                    if (goodsEntity.getNxCgGoodsHuaxianPrice() != null) {
                        BigDecimal huaxianQuantity = new BigDecimal(goodsEntity.getNxCgGoodsHuaxianQuantity());
                        Map<String, Object> mapOrderQuantity = new HashMap<>();
                        mapOrderQuantity.put("date", formatWhatDay(0));
                        mapOrderQuantity.put("orderUserId", orderUserId);
                        mapOrderQuantity.put("goodsId", nxCosCommunityGoodsId);
                        int orderQuantityInt = nxCommunityOrdersSubService.queryTodayHuaxianCount(mapOrderQuantity);
                        BigDecimal orderQuantity = new BigDecimal(orderQuantityInt);
                        BigDecimal moreQuantity = huaxianQuantity.subtract(orderQuantity);
                        //划线优惠数量没有用完，直接修改订单
                        System.out.println("huaxianqqqqqq" + huaxianQuantity);
                        System.out.println("moreQuantitymoreQuantitymoreQuantity" + moreQuantity);
                        if (moreQuantity.compareTo(new BigDecimal(0)) == 1) {

                            BigDecimal decimal = new BigDecimal(goodsEntity.getNxCgGoodsPrice()).multiply(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                            subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsPrice());
                            subEntity.setNxCosHuaxianDifferentPrice(goodsEntity.getNxCgGoodsHuaxianPriceDifferent());
                            subEntity.setNxCosSubtotal(decimal.toString());
                            nxCommunityOrdersSubService.update(subEntity);

                        } else {
                            //划线优惠数量用完了
                            //查询是否有同样商品的没有优惠的订单
                            Map<String, Object> mapSame = new HashMap<>();
                            mapSame.put("orderUserId", orderUserId);
                            mapSame.put("orderType", orderType);
                            mapSame.put("status", -1);
                            mapSame.put("diffPrice", 0);
                            mapSame.put("goodsId", nxCosCommunityGoodsId);
                            mapSame.put("serviceType", serviceType);
                            if (subEntity.getNxCosRemark() != null) {
                                mapSame.put("remark", subEntity.getNxCosRemark());
                            }
                            System.out.println("mapososososos" + mapSame);
                            NxCommunityOrdersSubEntity sameGoodsOrder = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapSame);

                            if (sameGoodsOrder != null) {

                                if (moreQuantity.compareTo(new BigDecimal(0)) == 0) {
                                    BigDecimal decimal = new BigDecimal(goodsEntity.getNxCgGoodsPrice()).multiply(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                                    subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsPrice());
                                    subEntity.setNxCosHuaxianDifferentPrice(goodsEntity.getNxCgGoodsHuaxianPriceDifferent());
                                    subEntity.setNxCosSubtotal(decimal.toString());
                                    nxCommunityOrdersSubService.update(subEntity);
                                } else {
                                    System.out.println("hasamdmmdmdmdmd" + sameGoodsOrder.getNxCosQuantity());

                                    //有相同的没有优惠的订单,把一个订房分成 2 部分
                                    NxCommunityOrdersSubEntity limitOrder = new NxCommunityOrdersSubEntity();
                                    limitOrder.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                                    Integer communityId = goodsEntity.getNxCgCommunityId();
                                    NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
                                    limitOrder.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());

                                    limitOrder.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                                    limitOrder.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                                    limitOrder.setNxCosOrderUserId(orderUserId);
                                    limitOrder.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                                    limitOrder.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                                    limitOrder.setNxCosQuantity(goodsEntity.getNxCgGoodsHuaxianQuantity().toString());
                                    limitOrder.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                                    limitOrder.setNxCosPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
                                    BigDecimal decimal = new BigDecimal(goodsEntity.getNxCgGoodsPrice()).multiply(new BigDecimal(goodsEntity.getNxCgGoodsHuaxianQuantity())).setScale(1, BigDecimal.ROUND_HALF_UP);
                                    limitOrder.setNxCosSubtotal(decimal.toString());
                                    limitOrder.setNxCosHuaxianDifferentPrice(goodsEntity.getNxCgGoodsHuaxianPriceDifferent());
                                    limitOrder.setNxCosStatus(-1);
                                    limitOrder.setNxCosRemark(subEntity.getNxCosRemark());
                                    limitOrder.setNxCosType(orderType);
                                    limitOrder.setNxCosServiceType(serviceType);
                                    limitOrder.setNxCosSplicingOrdersId(subEntity.getNxCosSplicingOrdersId());
                                    limitOrder.setNxCosServiceDate(formatWhatDay(0));
                                    nxCommunityOrdersSubService.save(limitOrder);


                                    BigDecimal restQuantity = new BigDecimal(sameGoodsOrder.getNxCosQuantity()).subtract(new BigDecimal(goodsEntity.getNxCgGoodsHuaxianQuantity()));
                                    BigDecimal decimal1 = restQuantity.multiply(new BigDecimal(goodsEntity.getNxCgGoodsHuaxianPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                                    sameGoodsOrder.setNxCosPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
                                    sameGoodsOrder.setNxCosQuantity(restQuantity.toString());
                                    sameGoodsOrder.setNxCosHuaxianDifferentPrice("0");
                                    sameGoodsOrder.setNxCosSubtotal(decimal1.toString());
                                    nxCommunityOrdersSubService.update(sameGoodsOrder);
                                    System.out.println("ovdiireiieiieeiieeieie" + sameGoodsOrder.getNxCosQuantity());
                                }


                            } else {

                                BigDecimal decimal = new BigDecimal(goodsEntity.getNxCgGoodsPrice()).multiply(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                                subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsPrice());
                                subEntity.setNxCosHuaxianDifferentPrice(goodsEntity.getNxCgGoodsHuaxianPriceDifferent());
                                subEntity.setNxCosSubtotal(decimal.toString());
                                nxCommunityOrdersSubService.update(subEntity);
                            }
                        }
                    }

                }
            }

            NxCustomerUserCardEntity userCardEntity = nxCustomerUserCardService.queryObject(userCardId);
            userCardEntity.setNxCucaIsSelected(1);
            nxCustomerUserCardService.update(userCardEntity);


        }


        Map<String, Object> mapA = new HashMap<>();
        mapA.put("orderUserId", orderUserId);
        mapA.put("status", -1);
        mapA.put("orderType", orderType);
        mapA.put("serviceType", serviceType);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);

        Map<String, Object> mapC = new HashMap<>();
        mapC.put("userId", orderUserId);
        mapC.put("status", -1);
        mapC.put("type", orderType);
        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("subOrders", nxCommunityOrdersSubEntities);
        mapR.put("cardList", cardEntities);
        return R.ok().put("data", mapR);
    }


    /**
     * @Title: callBack
     * @Description: 支付完成的回调函数
     * @param:
     * @return:
     */
    @RequestMapping(value = "/notifyDadaCustomer")
    public String notifyDadaCustomer(HttpServletRequest request, String rpcResult) {
        System.out.println("请求头信息: " + request.getHeaderNames());
        System.out.println("请求体信息: " + rpcResult);
        System.out.println("dada的callback信息,request0000:" + rpcResult);
        System.out.println("dada的callback信息,request123:" + JSON.parseObject(rpcResult));
        DaDaResponse<?> response = JSON.parseObject(rpcResult, new TypeReference<DaDaResponse<?>>() {
        });
        System.out.println("执行成功responseresponse，响应结果：{}" + response);

        if (Objects.nonNull(response) && Objects.equals(response.getStatus(), "success")) {
            System.out.println("执行成功，响应结果：{}" + JSON.toJSONString(response.getResult()));
        } else {
            System.out.println("执行失败，响应结果：{}" + rpcResult);
        }

        return null;
    }


//    @RequestMapping(value = "/testDada")
//    @ResponseBody
//    public R testDada() {
//
//        Integer userid = 1;
//        String rpcResult = queryOrderDeliver(appKey, appSecret, userid, 1);
//        // 创建订单
//        System.out.println("创建订单结果：" + rpcResult);
//
//        DaDaResponse<?> response = JSON.parseObject(rpcResult, new TypeReference<DaDaResponse<?>>() {
//        });
//        if (Objects.nonNull(response) && Objects.equals(response.getStatus(), "success")) {
//            System.out.println("执行成功，响应结果：{}" + JSON.toJSONString(response.getResult()));
//            if (response.getResult() instanceof Map) {
//                Map<String, Object> resultMap = (Map<String, Object>) response.getResult();
//                Object deliverFee = resultMap.get("deliverFee");
//                System.out.println("deliverFee==123" + deliverFee);
//            } else {
//                System.out.println("result 不是 Map 类型");
//            }
//        } else {
//            System.out.println("执行失败，响应结果：{}" + rpcResult);
//        }
//        return R.ok();
//    }

    private  String queryOrderDeliver(String appKey, String appSecret, Integer userId, Integer addressId) {
        String CREATE_ORDER_URL = "https://newopen.imdada.cn/api/order/queryDeliverFee";
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println("abccreateOrder" + objectMapper);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("app_key", appKey);
            params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
            params.put("format", "json");
            params.put("v", "1.0");
            params.put("source_id", "887737");

            // 构建body参数
            Map<String, Object> body = new HashMap<>();
            body.put("origin_id", "originId-" + System.currentTimeMillis());
            body.put("shop_no", "887737-22356381");
            body.put("transporter_id", "1");

            NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(userId);
            NxCustomerUserAddressEntity addressEntity = nxCustomerUserAddressService.queryObject(addressId);
            body.put("receiver_name", userEntity.getNxCuWxNickName());
            body.put("receiver_phone", userEntity.getNxCuWxPhoneNumber());
            body.put("receiver_address", addressEntity.getNxCuaAddressBuildingName() + addressEntity.getNxCuaAddressDetail());
            body.put("receiver_lng", addressEntity.getNxCuaLng());
            body.put("receiver_lat", addressEntity.getNxCuaLat());
            body.put("callback", "https://grainservice.club:8443/nongxinle/api/nxorderssub/notifyDadaCustomer");
            body.put("cargo_weight", 3.2);

            // 将body转换为JSON字符串坐标：116.462282,39.890496
            String bodyJson = objectMapper.writeValueAsString(body);

            // 将body参数加入到params中
            params.put("body", bodyJson);

            // 生成签名
            String signature = DadaSignUtils.generateSignature(appSecret, params);
            params.put("signature", signature);  // 在params中加入签名

            // 发送POST请求
            HttpPost httpPost = new HttpPost(CREATE_ORDER_URL);
            httpPost.setHeader("Content-Type", "application/json");

            // 将整个params（包含body的内容）转换为JSON字符串
            String jsonBody = objectMapper.writeValueAsString(params);
            httpPost.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));
            System.out.println("请求体----123456：" + jsonBody);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return EntityUtils.toString(response.getEntity());
            }


        } catch (Exception e) {
            e.printStackTrace();
            return "{\"code\":\"ERROR\", \"msg\":\"请求异常\"}";
        }
    }

    @RequestMapping(value = "/orderUserGetApply", method = RequestMethod.POST)
    @ResponseBody
    public R orderUserGetApply(Integer userId, Integer serviceType, Integer addressId) {

        Map<String, Object> mapA = new HashMap<>();
        mapA.put("orderUserId", userId);
        mapA.put("serviceType", serviceType);
        mapA.put("status", -1);
        mapA.put("orderType", 0);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);

        Map<String, Object> mapC = new HashMap<>();
        mapC.put("userId", userId);
        mapC.put("status", -1);
        mapC.put("isSelected", 1);
        mapC.put("type", 0);
        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("subOrders", nxCommunityOrdersSubEntities);
        mapR.put("cardList", cardEntities);

        if(addressId != -1){
            String rpcResult = queryOrderDeliver(appKey, appSecret, userId, addressId);
            // 创建订单
            System.out.println("创建订单结果：" + rpcResult);

            DaDaResponse<?> response = JSON.parseObject(rpcResult, new TypeReference<DaDaResponse<?>>() {
            });
            if (Objects.nonNull(response) && Objects.equals(response.getStatus(), "success")) {
                System.out.println("执行成功，响应结果：{}" + JSON.toJSONString(response.getResult()));
                if (response.getResult() instanceof Map) {
                    Map<String, Object> resultMap = (Map<String, Object>) response.getResult();
                    Object deliverFee = resultMap.get("deliverFee");
                    mapR.put("delivery", resultMap);
                    System.out.println("deliverFee==123" + deliverFee);
                } else {
                    System.out.println("result 不是 Map 类型");
                }
            } else {
                mapR.put("delivery", null);
                System.out.println("执行失败，响应结果：{}" + rpcResult);
            }
        }else{
            mapR.put("delivery", null);
        }

        return R.ok().put("data", mapR);
    }




    @RequestMapping(value = "/deskGetApply", method = RequestMethod.POST)
    @ResponseBody
    public R deskGetApply(Integer deskId) {

        Map<String, Object> mapA = new HashMap<>();
        mapA.put("deskId", deskId);
        mapA.put("xiaoyuStatus", 1);
        mapA.put("orderType", 0);
        System.out.println("zaahsuiss" + mapA);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);

//        Map<String, Object> mapC = new HashMap<>();
//        mapC.put("deskId", deskId);
//        mapC.put("status", -1);
//        mapC.put("isSelected", 1);
//        mapC.put("type", 0);
//        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);

        Map<String, Object> map = new HashMap<>();
        map.put("deskId", deskId);
        map.put("status", 3);
        System.out.println("mapsppspspds" + map);
        NxCommunityDeskEntity deskEntity =  nxCommunityDeskService.queryDeskWithOrders(map);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("subOrders", nxCommunityOrdersSubEntities);
        mapR.put("cardList", new ArrayList<>());
        mapR.put("deskInfo", deskEntity);
        if(nxCommunityOrdersSubEntities.size() > 0){
            return R.ok().put("data", mapR);
        }else{
            return R.error(-1,"已支付");
        }

    }

    @RequestMapping(value = "/comDeskGetApply", method = RequestMethod.POST)
    @ResponseBody
    public R comDeskGetApply(Integer deskId, String startDate, String stopDate) {

        Map<String, Object> mapA = new HashMap<>();
        mapA.put("deskId", deskId);
        System.out.println("zaahsuiss" + mapA);
        List<NxCommunityOrdersEntity> ordersEntities = nxCommunityOrdersService.queryCustomerOrder(mapA);

        return R.ok().put("data", ordersEntities);

    }



    @RequestMapping(value = "/reduceSubOrderRemark", method = RequestMethod.POST)
    @ResponseBody
    public R reduceSubOrderRemark(Integer goodsId, Integer orderUserId, Integer deskId, String remark, Integer orderType, Integer spId, Integer pindanId) {
        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
        if (goodsEntity.getNxCgGoodsHuaxianQuantity() == null) {
            this.reduceSubOrderRemarkNoQuantity(goodsId, orderUserId, remark, orderType, spId, deskId);
        } else {
            this.reduceSubOrderRemarkQuantity(goodsId, orderUserId, remark, orderType, spId, deskId);
        }


        Map<String, Object> mapA = new HashMap<>();
        mapA.put("orderUserId", orderUserId);
        mapA.put("deskId", deskId);
        mapA.put("status", -1);
        mapA.put("orderType", orderType);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);

        Map<String, Object> mapCard = new HashMap<>();
        mapCard.put("status", -1);
        mapCard.put("userId", orderUserId);
        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapCard);

        Map<String, Object> mapData = new HashMap<>();
        mapData.put("arr", nxCommunityOrdersSubEntities);
        mapData.put("cardList", cardEntities);

        return R.ok().put("data", mapData);

    }

    private void reduceSubOrderRemarkNoQuantity(Integer goodsId, Integer orderUserId, String remark, Integer orderType, Integer spId, Integer deskId) {
        //common -
        Map<String, Object> map = new HashMap<>();
        map.put("orderUserId", orderUserId);
        map.put("deskId", deskId);
        map.put("goodsId", goodsId);
        map.put("status", -1);
        map.put("orderType", orderType);
        map.put("splicingOrderId", spId);
        map.put("remark", remark);
        NxCommunityOrdersSubEntity commSubOrderEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(map);
        if (commSubOrderEntity != null) {
            BigDecimal orderQuantity = new BigDecimal(commSubOrderEntity.getNxCosQuantity());
            if (orderQuantity.compareTo(new BigDecimal(1)) == 0) {
                //1，删除订单
                nxCommunityOrdersSubService.delete(commSubOrderEntity.getNxCommunityOrdersSubId());
            } else {
                BigDecimal quantity = new BigDecimal(commSubOrderEntity.getNxCosQuantity()).subtract(new BigDecimal(1));
                BigDecimal decimal = new BigDecimal(commSubOrderEntity.getNxCosPrice()).multiply(quantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                commSubOrderEntity.setNxCosQuantity(quantity.toString());
                commSubOrderEntity.setNxCosSubtotal(decimal.toString());
                if (commSubOrderEntity.getNxCosHuaxianPrice() != null) {
                    BigDecimal decimal1 = new BigDecimal(commSubOrderEntity.getNxCosHuaxianPrice()).multiply(quantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                    commSubOrderEntity.setNxCosHuaxianSubtotal(decimal1.toString());
                }
                nxCommunityOrdersSubService.update(commSubOrderEntity);
            }
        }
    }


    private void reduceSubOrderRemarkQuantity(Integer goodsId, Integer orderUserId,String remark, Integer orderType, Integer spId,Integer deskId) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderUserId", orderUserId);
        map.put("deskId", deskId);
        map.put("goodsId", goodsId);
        map.put("status", -1);
        map.put("diffPrice", 0);
        map.put("orderType", orderType);
        map.put("remark", remark);
        map.put("splicingOrderId", spId);
        NxCommunityOrdersSubEntity diffZeroSubOrderEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(map);
        if (diffZeroSubOrderEntity != null) {
            BigDecimal orderQuantity = new BigDecimal(diffZeroSubOrderEntity.getNxCosQuantity());
            if (orderQuantity.compareTo(new BigDecimal(1)) == 0) {
                //1，删除订单
                nxCommunityOrdersSubService.delete(diffZeroSubOrderEntity.getNxCommunityOrdersSubId());
            } else {
                BigDecimal quantity = new BigDecimal(diffZeroSubOrderEntity.getNxCosQuantity()).subtract(new BigDecimal(1));
                BigDecimal decimal = new BigDecimal(diffZeroSubOrderEntity.getNxCosPrice()).multiply(quantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                diffZeroSubOrderEntity.setNxCosQuantity(quantity.toString());
                diffZeroSubOrderEntity.setNxCosSubtotal(decimal.toString());
                if (diffZeroSubOrderEntity.getNxCosHuaxianPrice() != null) {
                    BigDecimal decimal1 = new BigDecimal(diffZeroSubOrderEntity.getNxCosHuaxianPrice()).multiply(quantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                    diffZeroSubOrderEntity.setNxCosHuaxianSubtotal(decimal1.toString());
                }
                nxCommunityOrdersSubService.update(diffZeroSubOrderEntity);
            }
        } else {
            Map<String, Object> mapD = new HashMap<>();
            mapD.put("orderUserId", orderUserId);
            mapD.put("deskId", deskId);
            mapD.put("goodsId", goodsId);
            mapD.put("status", -1);
            mapD.put("dayuDiffPrice", 0);
            mapD.put("orderType", orderType);
            mapD.put("splicingOrderId", spId);
            mapD.put("remark", remark);
            System.out.println("dayoouuiiuiououoaa" + mapD);
            //有优惠的订单
            NxCommunityOrdersSubEntity diffSubOrderEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapD);
            if (diffSubOrderEntity != null) {
                BigDecimal orderQuantity = new BigDecimal(diffSubOrderEntity.getNxCosQuantity());
                if (orderQuantity.compareTo(new BigDecimal(1)) == 0) {
                    //1，删除订单
                    nxCommunityOrdersSubService.delete(diffSubOrderEntity.getNxCommunityOrdersSubId());
                } else {
                    BigDecimal quantity = new BigDecimal(diffSubOrderEntity.getNxCosQuantity()).subtract(new BigDecimal(1));
                    BigDecimal decimal = new BigDecimal(diffSubOrderEntity.getNxCosPrice()).multiply(quantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                    diffSubOrderEntity.setNxCosQuantity(quantity.toString());
                    diffSubOrderEntity.setNxCosSubtotal(decimal.toString());
                    if (diffSubOrderEntity.getNxCosHuaxianPrice() != null) {
                        BigDecimal decimal1 = new BigDecimal(diffSubOrderEntity.getNxCosHuaxianPrice()).multiply(quantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                        diffSubOrderEntity.setNxCosHuaxianSubtotal(decimal1.toString());
                    }
                    nxCommunityOrdersSubService.update(diffSubOrderEntity);
                }
            }
        }
    }

    @RequestMapping(value = "/saveSubOrderRemark", method = RequestMethod.POST)
    @ResponseBody
    public R saveSubOrderRemark(Integer goodsId, Integer orderUserId, Integer deskId,String remark, Integer orderType,
                                Integer spId, Integer pindanId, String subPrice, String subHuaxianPrice, Integer serviceType) {
        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
        if (goodsEntity.getNxCgGoodsHuaxianQuantity() == null) {
            this.saveSubOrderRemarkNoQuantity(goodsId, orderUserId, remark, orderType, spId, pindanId, subPrice,serviceType, deskId);
        } else {
            this.saveSubOrderRemarkQuantity(goodsId, orderUserId, remark, orderType, spId, pindanId, subPrice, subHuaxianPrice, serviceType, deskId);
        }
        //giveapply
        Map<String, Object> mapA = new HashMap<>();
        mapA.put("orderUserId", orderUserId);
        mapA.put("deskId", deskId);
        mapA.put("status", -1);
        mapA.put("orderType", orderType);
        mapA.put("splicingOrderId", spId);
        System.out.println("apappapap" + mapA);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);

        return R.ok().put("data", nxCommunityOrdersSubEntities);

    }


    private void saveSubOrderRemarkNoQuantity(Integer goodsId, Integer orderUserId, String remark, Integer orderType, Integer spId,
                                              Integer pindanId, String subPrice, Integer serviceType, Integer deskId) {

        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
        Map<String, Object> mapC = new HashMap<>();
        mapC.put("orderUserId", orderUserId);
        mapC.put("deskId", deskId);
        mapC.put("goodsId", goodsId);
        mapC.put("status", -1);
        mapC.put("remark", remark);
        mapC.put("orderType", orderType);
        mapC.put("splicingOrderId", spId);
        mapC.put("serviceType", serviceType);
        //1.1.1 先查询优惠订单是否超过数量
        System.out.println("ccccccccccaaaammmmmmmmmmNOqunaitytyyty" + mapC);
        NxCommunityOrdersSubEntity communityOrdersSubEntityC = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapC);
        if (communityOrdersSubEntityC != null) {
            BigDecimal add = new BigDecimal(communityOrdersSubEntityC.getNxCosQuantity()).add(new BigDecimal(1));
            BigDecimal subtotal = new BigDecimal(communityOrdersSubEntityC.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
            communityOrdersSubEntityC.setNxCosQuantity(add.toString());
            communityOrdersSubEntityC.setNxCosSubtotal(subtotal.toString());
            nxCommunityOrdersSubService.update(communityOrdersSubEntityC);
        } else {


            //添加信息普通订单
            NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
            subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
            Integer communityId = goodsEntity.getNxCgCommunityId();
            NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
            subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());
            subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
            subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
            subEntity.setNxCosOrderUserId(orderUserId);
            subEntity.setNxCosDeskId(deskId);
            subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
            subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
            subEntity.setNxCosQuantity("1");
            subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
            subEntity.setNxCosPrice(subPrice);
            subEntity.setNxCosSubtotal(subPrice);
            subEntity.setNxCosHuaxianDifferentPrice("0");
            subEntity.setNxCosStatus(-1);
            subEntity.setNxCosRemark(remark);
            subEntity.setNxCosType(orderType);
            subEntity.setNxCosServiceType(serviceType);
            subEntity.setNxCosSplicingOrdersId(spId);
            subEntity.setNxCosOrdersId(pindanId);
            subEntity.setNxCosServiceDate(formatWhatDay(0));
            System.out.println("dkfadfjalfahuaxx" + goodsEntity.getNxCgGoodsHuaxianPriceDifferent());
            nxCommunityOrdersSubService.save(subEntity);

        }

    }


    private void saveSubOrderRemarkQuantity(Integer goodsId, Integer orderUserId, String remark, Integer orderType,
                                            Integer spId, Integer pindanId, String subPrice, String subHuaxianPrice, Integer serviceType, Integer deskId) {
        BigDecimal diffPrice = new BigDecimal(subHuaxianPrice).subtract(new BigDecimal(subPrice)).setScale(1, BigDecimal.ROUND_HALF_UP);

        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
        Map<String, Object> mapC = new HashMap<>();
        mapC.put("orderUserId", orderUserId);
        mapC.put("deskId", deskId);
        mapC.put("goodsId", goodsId);
        mapC.put("status", -1);
        mapC.put("diffPrice", 0);
        mapC.put("remark", remark);
        mapC.put("orderType", orderType);
        mapC.put("splicingOrderId", spId);
        mapC.put("serviceType", serviceType);
        //1.1.1 先查询优惠订单是否超过数量
        NxCommunityOrdersSubEntity communityOrdersSubEntityC = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapC);
        if (communityOrdersSubEntityC != null) {
            BigDecimal add = new BigDecimal(communityOrdersSubEntityC.getNxCosQuantity()).add(new BigDecimal(1));
            BigDecimal subtotal = new BigDecimal(communityOrdersSubEntityC.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
            communityOrdersSubEntityC.setNxCosQuantity(add.toString());
            communityOrdersSubEntityC.setNxCosSubtotal(subtotal.toString());
            nxCommunityOrdersSubService.update(communityOrdersSubEntityC);
        } else {
            //huaxianQuantity

            Map<String, Object> map = new HashMap<>();
            map.put("orderUserId", orderUserId);
            map.put("deskId", deskId);
            map.put("goodsId", goodsId);
            map.put("status", -1);
            map.put("dayuDiffPrice", 0);
            map.put("remark", remark);
            map.put("orderType", orderType);
            map.put("splicingOrderId", spId);
            map.put("serviceType", serviceType);
            NxCommunityOrdersSubEntity communityOrdersSubEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(map);
            if (communityOrdersSubEntity != null ) {
                BigDecimal huaxianQuantity = new BigDecimal(goodsEntity.getNxCgGoodsHuaxianQuantity());
                Map<String, Object> mapOrderQuantity = new HashMap<>();
                mapOrderQuantity.put("date", formatWhatDay(0));
                mapOrderQuantity.put("orderUserId", orderUserId);
                mapOrderQuantity.put("goodsId", goodsId);
                int orderQuantityInt = nxCommunityOrdersSubService.queryTodayHuaxianCount(mapOrderQuantity);
                BigDecimal orderQuantity = new BigDecimal(orderQuantityInt);
                Map<String, Object> mapT = new HashMap<>();
                mapT.put("orderUserId", orderUserId);
                mapT.put("deskId", deskId);
                mapT.put("goodsId", goodsId);
                mapT.put("status", -1);
                mapT.put("dayuDiffPrice", 0);
                mapT.put("orderType", orderType);
                mapT.put("splicingOrderId", spId);
                mapT.put("serviceType", serviceType);
                int total = nxCommunityOrdersSubService.querySubOrderTotalHuaxianQuantity(mapT);
                BigDecimal restQuantity = huaxianQuantity.subtract(new BigDecimal(total)); //剩余可用划线后优惠价格的数量
                //1.1.1.1剩余数量大于 1，则加1
                System.out.println("restttootototsaveSubOrderRemarkQuantity" + restQuantity);
                if (restQuantity.compareTo(new BigDecimal(0)) == 1) {
                    BigDecimal add = orderQuantity.add(new BigDecimal(1));
                    BigDecimal subtotal = new BigDecimal(communityOrdersSubEntity.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal huaxianSubtotal = new BigDecimal(communityOrdersSubEntity.getNxCosHuaxianPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                    communityOrdersSubEntity.setNxCosQuantity(add.toString());
                    communityOrdersSubEntity.setNxCosSubtotal(subtotal.toString());
                    communityOrdersSubEntity.setNxCosHuaxianSubtotal(huaxianSubtotal.toString());
                    nxCommunityOrdersSubService.update(communityOrdersSubEntity);
                } else {
                    //1.1.1.2保存普通订单之前先查是否有同样的订单
                    Map<String, Object> mapZ = new HashMap<>();
                    mapZ.put("orderUserId", orderUserId);
                    mapZ.put("deskId", deskId);
                    mapZ.put("goodsId", goodsId);
                    mapZ.put("status", -1);
                    mapZ.put("diffPrice", 0);
                    mapZ.put("remark", remark);
                    mapZ.put("orderType", orderType);
                    mapZ.put("splicingOrderId", spId);
                    mapZ.put("serviceType", serviceType);
                    System.out.println("ccccccccccaaaasaveSubOrderRemarkQuantity" + mapZ);
                    NxCommunityOrdersSubEntity communityOrdersSubZero = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapZ);
                    //已经有了普通订单，则修改数量
                    if (communityOrdersSubZero != null) {
                        System.out.println("meiyouzuozuozzlididiididmsaveSubOrderRemarkQuantity" + communityOrdersSubZero.getNxCosQuantity());
                        BigDecimal nxCosQuantity = new BigDecimal(communityOrdersSubZero.getNxCosQuantity());
                        BigDecimal add = nxCosQuantity.add(new BigDecimal(1));
                        BigDecimal subtotal = new BigDecimal(communityOrdersSubZero.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                        communityOrdersSubZero.setNxCosQuantity(add.toString());
                        communityOrdersSubZero.setNxCosSubtotal(subtotal.toString());
                        System.out.println("updddateeeee" + communityOrdersSubZero.getNxCosQuantity());
                        nxCommunityOrdersSubService.update(communityOrdersSubZero);

                    } else {
                        //添加信息普通订单
                        NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                        subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                        Integer communityId = goodsEntity.getNxCgCommunityId();
                        NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
                        subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());

                        subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                        subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                        subEntity.setNxCosOrderUserId(orderUserId);
                        subEntity.setNxCosDeskId(deskId);
                        subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                        subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                        subEntity.setNxCosQuantity("1");
                        subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                        subEntity.setNxCosPrice(subHuaxianPrice);
                        subEntity.setNxCosSubtotal(subHuaxianPrice);
                        subEntity.setNxCosHuaxianDifferentPrice("0");
                        subEntity.setNxCosStatus(-1);
                        subEntity.setNxCosRemark(remark);
                        subEntity.setNxCosType(orderType);
                        subEntity.setNxCosServiceType(serviceType);
                        subEntity.setNxCosSplicingOrdersId(spId);
                        subEntity.setNxCosOrdersId(pindanId);
                        subEntity.setNxCosServiceDate(formatWhatDay(0));
                        System.out.println("newsavveeee" + subEntity);
                        nxCommunityOrdersSubService.save(subEntity);
                    }
                }


            } else {

                BigDecimal huaxianQuantity = new BigDecimal(goodsEntity.getNxCgGoodsHuaxianQuantity());
                Map<String, Object> mapT = new HashMap<>();
                mapT.put("orderUserId", orderUserId);
                mapT.put("deskId", deskId);
                mapT.put("goodsId", goodsId);
                mapT.put("status", -1);
                mapT.put("orderType", orderType);
                mapT.put("splicingOrderId", spId);
                mapT.put("serviceType", serviceType);
                int count = nxCommunityOrdersSubService.querySubOrderCount(mapT);
                if (count > 0) {
                    //diyige
                    int total = nxCommunityOrdersSubService.querySubOrderTotalHuaxianQuantity(mapT);
                    //如果商品的订购数量大于划线数量，则按划线订单
                    NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                    subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                    Integer communityId = goodsEntity.getNxCgCommunityId();
                    NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
                    subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());
                    subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                    subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                    subEntity.setNxCosOrderUserId(orderUserId);
                    subEntity.setNxCosDeskId(deskId);
                    subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                    subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                    subEntity.setNxCosQuantity("1");
                    subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                    if (new BigDecimal(total).compareTo(huaxianQuantity) > -1) {
                        subEntity.setNxCosPrice(subPrice);
                        subEntity.setNxCosSubtotal(subPrice);
                        subEntity.setNxCosHuaxianDifferentPrice("0");
                    } else {
                        subEntity.setNxCosPrice(subPrice);
                        subEntity.setNxCosSubtotal(subPrice);
                        subEntity.setNxCosHuaxianDifferentPrice(diffPrice.toString());
                        subEntity.setNxCosHuaxianPrice(subHuaxianPrice);
                        subEntity.setNxCosHuaxianSubtotal(subHuaxianPrice);
                    }
                    subEntity.setNxCosStatus(-1);
                    subEntity.setNxCosRemark(remark);
                    subEntity.setNxCosType(orderType);
                    subEntity.setNxCosSplicingOrdersId(spId);
                    subEntity.setNxCosOrdersId(pindanId);
                    subEntity.setNxCosServiceDate(formatWhatDay(0));
                    System.out.println("fakdfalfjaslfjsafahfahef");
                    nxCommunityOrdersSubService.save(subEntity);
                } else {

                    //tianajia

                    //添加信息普通订单
                    NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                    subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                    Integer communityId = goodsEntity.getNxCgCommunityId();
                    NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
                    subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());

                    subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                    subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                    subEntity.setNxCosOrderUserId(orderUserId);
                    subEntity.setNxCosDeskId(deskId);
                    subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                    subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                    subEntity.setNxCosQuantity("1");
                    subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                    subEntity.setNxCosPrice(subPrice);
                    subEntity.setNxCosHuaxianPrice(subHuaxianPrice);
                    subEntity.setNxCosHuaxianSubtotal(subHuaxianPrice);
                    subEntity.setNxCosSubtotal(subPrice);
                    subEntity.setNxCosHuaxianDifferentPrice(diffPrice.toString());
                    subEntity.setNxCosStatus(-1);
                    subEntity.setNxCosRemark(remark);
                    subEntity.setNxCosType(orderType);
                    subEntity.setNxCosServiceType(serviceType);
                    subEntity.setNxCosSplicingOrdersId(spId);
                    subEntity.setNxCosOrdersId(pindanId);
                    subEntity.setNxCosServiceDate(formatWhatDay(0));
                    System.out.println("abccccc" + subEntity);
                    nxCommunityOrdersSubService.save(subEntity);

                }
            }
        }
    }


    @RequestMapping(value = "/memberSaveMemberOrderRemark", method = RequestMethod.POST)
    @ResponseBody
    public R memberSaveMemberOrderRemark(Integer goodsId, Integer orderUserId,Integer deskId,  String remark, Integer orderType,
                                         Integer spId, Integer pindanId,String subPrice, String subHuaxianPrice, Integer serviceType) {

        BigDecimal differPrice = new BigDecimal(subHuaxianPrice).subtract(new BigDecimal(subPrice)).setScale(1,BigDecimal.ROUND_HALF_UP);
        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
        Map<String, Object> mapC = new HashMap<>();
        mapC.put("orderUserId", orderUserId);
        mapC.put("deskId", deskId);
        mapC.put("goodsId", goodsId);
        mapC.put("status", -1);
        mapC.put("diffPrice", 0);
        mapC.put("remark", remark);
        mapC.put("orderType", orderType);
        mapC.put("splicingOrderId", spId);
        mapC.put("serviceType", serviceType);
        //1.1.1 先查询优惠订单是否超过数量
        System.out.println("ccccccccccaaaammmmmmmmmmmemberSaveMemberOr" + mapC);
        NxCommunityOrdersSubEntity communityOrdersSubEntityC = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapC);
        if (communityOrdersSubEntityC != null) {
            BigDecimal add = new BigDecimal(communityOrdersSubEntityC.getNxCosQuantity()).add(new BigDecimal(1));
            BigDecimal subtotal = new BigDecimal(communityOrdersSubEntityC.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
            communityOrdersSubEntityC.setNxCosQuantity(add.toString());
            communityOrdersSubEntityC.setNxCosSubtotal(subtotal.toString());
            nxCommunityOrdersSubService.update(communityOrdersSubEntityC);
        } else {
            //huaxianQuantity

            Map<String, Object> map = new HashMap<>();
            map.put("orderUserId", orderUserId);
            map.put("deskId", deskId);
            map.put("goodsId", goodsId);
            map.put("status", -1);
            map.put("dayuDiffPrice", 0);
            map.put("remark", remark);
            map.put("orderType", orderType);
            map.put("splicingOrderId", spId);
            map.put("serviceType", serviceType);
            System.out.println("ccccccccccaaaamemberSaveMemberOrderR" + map);
            NxCommunityOrdersSubEntity communityOrdersSubEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(map);
            if (communityOrdersSubEntity != null && goodsEntity.getNxCgGoodsHuaxianPrice() != null) {
                BigDecimal huaxianQuantity = new BigDecimal(goodsEntity.getNxCgGoodsHuaxianQuantity());
                Map<String, Object> mapOrderQuantity = new HashMap<>();
                mapOrderQuantity.put("date", formatWhatDay(0));
                mapOrderQuantity.put("orderUserId", orderUserId);
                mapOrderQuantity.put("goodsId", goodsId);
                int orderQuantityInt = nxCommunityOrdersSubService.queryTodayHuaxianCount(mapOrderQuantity);
                BigDecimal orderQuantity = new BigDecimal(orderQuantityInt);
                Map<String, Object> mapT = new HashMap<>();
                mapT.put("orderUserId", orderUserId);
                mapT.put("deskId", deskId);
                mapT.put("goodsId", goodsId);
                mapT.put("status", -1);
                mapT.put("dayuDiffPrice", 0);
                mapT.put("orderType", orderType);
                mapT.put("splicingOrderId", spId);
                mapT.put("serviceType", serviceType);

                int total = nxCommunityOrdersSubService.querySubOrderTotalHuaxianQuantity(mapT);
                BigDecimal restQuantity = huaxianQuantity.subtract(new BigDecimal(total)); //剩余可用划线后优惠价格的数量
                //1.1.1.1剩余数量大于 1，则加1
                System.out.println("restttootototmemberSaveMemberOrderRek" + restQuantity);
                if (restQuantity.compareTo(new BigDecimal(0)) == 1) {
                    BigDecimal add = orderQuantity.add(new BigDecimal(1));
                    BigDecimal subtotal = new BigDecimal(communityOrdersSubEntity.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal huaxianSubtotal = new BigDecimal(communityOrdersSubEntity.getNxCosHuaxianPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                    communityOrdersSubEntity.setNxCosQuantity(add.toString());
                    communityOrdersSubEntity.setNxCosSubtotal(subtotal.toString());
                    communityOrdersSubEntity.setNxCosHuaxianSubtotal(huaxianSubtotal.toString());
                    nxCommunityOrdersSubService.update(communityOrdersSubEntity);
                } else {
                    //1.1.1.2保存普通订单之前先查是否有同样的订单
                    Map<String, Object> mapZ = new HashMap<>();
                    mapZ.put("orderUserId", orderUserId);
                    mapZ.put("deskId", deskId);
                    mapZ.put("goodsId", goodsId);
                    mapZ.put("status", -1);
                    mapZ.put("diffPrice", 0);
                    mapZ.put("remark", remark);
                    mapZ.put("orderType", orderType);
                    mapZ.put("splicingOrderId", spId);
                    mapZ.put("serviceType", serviceType);
                    System.out.println("mappppzzzzzzRRRRRRRRRR" + mapZ);
                    NxCommunityOrdersSubEntity communityOrdersSubZero = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapZ);
                    //已经有了普通订单，则修改数量
                    if (communityOrdersSubZero != null) {
                        System.out.println("meiyouzuozuozzlididiididm" + communityOrdersSubZero.getNxCosQuantity());
                        BigDecimal nxCosQuantity = new BigDecimal(communityOrdersSubZero.getNxCosQuantity());
                        BigDecimal add = nxCosQuantity.add(new BigDecimal(1));
                        BigDecimal subtotal = new BigDecimal(communityOrdersSubZero.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                        communityOrdersSubZero.setNxCosQuantity(add.toString());
                        communityOrdersSubZero.setNxCosSubtotal(subtotal.toString());
                        System.out.println("updddateeeee" + communityOrdersSubZero.getNxCosQuantity());
                        nxCommunityOrdersSubService.update(communityOrdersSubZero);

                    } else {
                        //添加信息普通订单
                        NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                        subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                        Integer communityId = goodsEntity.getNxCgCommunityId();
                        NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
                        subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());

                        subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                        subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                        subEntity.setNxCosOrderUserId(orderUserId);
                        subEntity.setNxCosDeskId(deskId);
                        subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                        subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                        subEntity.setNxCosQuantity("1");
                        subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                        subEntity.setNxCosPrice(subHuaxianPrice);
                        subEntity.setNxCosSubtotal(subHuaxianPrice);
                        subEntity.setNxCosHuaxianDifferentPrice("0");
                        subEntity.setNxCosStatus(-1);
                        subEntity.setNxCosRemark(remark);
                        subEntity.setNxCosType(orderType);
                        subEntity.setNxCosServiceType(serviceType);
                        subEntity.setNxCosSplicingOrdersId(spId);
                        subEntity.setNxCosOrdersId(pindanId);
                        subEntity.setNxCosServiceDate(formatWhatDay(0));
                        nxCommunityOrdersSubService.save(subEntity);
                    }
                }
            } else {
                Map<String, Object> mapT = new HashMap<>();
                mapT.put("orderUserId", orderUserId);
                mapT.put("deskId", deskId);
                mapT.put("goodsId", goodsId);
                mapT.put("status", -1);
                mapT.put("orderType", orderType);
                mapT.put("splicingOrderId", spId);
                mapT.put("serviceType", serviceType);
                int count = nxCommunityOrdersSubService.querySubOrderCount(mapT);
                if (count > 0) {
                    //diyige
                    BigDecimal huaxianQuantity = new BigDecimal(goodsEntity.getNxCgGoodsHuaxianQuantity());
                    int total = nxCommunityOrdersSubService.querySubOrderTotalHuaxianQuantity(mapT);
                    //如果商品的订购数量大于划线数量，则按划线订单
                    NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                    subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                    Integer communityId = goodsEntity.getNxCgCommunityId();
                    NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
                    subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());
                    subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                    subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                    subEntity.setNxCosOrderUserId(orderUserId);
                    subEntity.setNxCosDeskId(deskId);
                    subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                    subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                    subEntity.setNxCosQuantity("1");
                    subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                    if (new BigDecimal(total).compareTo(huaxianQuantity) > -1) {
                        subEntity.setNxCosPrice(subHuaxianPrice);
                        subEntity.setNxCosSubtotal(subHuaxianPrice);
                        subEntity.setNxCosHuaxianDifferentPrice("0");
                    } else {
                        subEntity.setNxCosPrice(subPrice);
                        subEntity.setNxCosSubtotal(subPrice);
                        subEntity.setNxCosHuaxianDifferentPrice(differPrice.toString());
                    }

                    subEntity.setNxCosStatus(-1);
                    subEntity.setNxCosRemark(remark);
                    subEntity.setNxCosType(orderType);
                    subEntity.setNxCosServiceType(serviceType);
                    subEntity.setNxCosSplicingOrdersId(spId);
                    subEntity.setNxCosOrdersId(pindanId);
                    subEntity.setNxCosServiceDate(formatWhatDay(0));
                    System.out.println("fakdfalfjaslfjsafahfahef");
                    nxCommunityOrdersSubService.save(subEntity);
                } else {
                    NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                    subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                    Integer communityId = goodsEntity.getNxCgCommunityId();
                    NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
                    subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());
                    subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                    subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                    subEntity.setNxCosOrderUserId(orderUserId);
                    subEntity.setNxCosDeskId(deskId);
                    subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                    subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                    subEntity.setNxCosQuantity("1");
                    subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                    subEntity.setNxCosPrice(subPrice);
                    subEntity.setNxCosSubtotal(subPrice);
                    subEntity.setNxCosHuaxianDifferentPrice(differPrice.toString());
                    subEntity.setNxCosHuaxianSubtotal(subHuaxianPrice);
                    subEntity.setNxCosHuaxianPrice(subHuaxianPrice);
                    subEntity.setNxCosStatus(-1);
                    subEntity.setNxCosRemark(remark);
                    subEntity.setNxCosType(orderType);
                    subEntity.setNxCosServiceType(serviceType);
                    subEntity.setNxCosSplicingOrdersId(spId);
                    subEntity.setNxCosOrdersId(pindanId);
                    subEntity.setNxCosServiceDate(formatWhatDay(0));
                    nxCommunityOrdersSubService.save(subEntity);
                }
            }
        }
        //giveapply
        Map<String, Object> mapA = new HashMap<>();
        mapA.put("orderUserId", orderUserId);
        mapA.put("deskId", deskId);
        mapA.put("status", -1);
        mapA.put("orderType", orderType);
        mapA.put("splicingOrderId", spId);
        System.out.println("apappapap" + mapA);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);


        return R.ok().put("data", nxCommunityOrdersSubEntities);

//        }


    }


    @ResponseBody
    @RequestMapping(value = "/saveSubOrderRemarkCard", method = RequestMethod.POST)
    public R saveSubOrderRemarkCard(Integer goodsId, Integer orderUserId, Integer deskId, String remark, Integer orderType,
                                    Integer spId, Integer pindanId,String subPrice,String subHuaxianPrice, Integer serviceType) {

        BigDecimal differPrice = new BigDecimal(subHuaxianPrice).subtract(new BigDecimal(subPrice)).setScale(1, BigDecimal.ROUND_HALF_UP);
        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
        Map<String, Object> map = new HashMap<>();
        map.put("cardId", goodsEntity.getNxCgCardId());
        map.put("stopTime", formatWhatDay(0));
        map.put("userId", orderUserId);
        map.put("deskId", deskId);
//        map.put("goodsId", goodsId);
//        map.put("status", -1);
//        map.put("type", orderType);
        System.out.println("wkwkjekejrelqelwqrekrremakrekkk???" + map);
        NxCustomerUserCardEntity userCardEntitys = nxCustomerUserCardService.queryUserGoodsCard(map);
        System.out.println("zahuisshsisisiiisi????" + userCardEntitys);
        // 一，如果已经有会员卡
        if (userCardEntitys != null) {
            //1.1 如果会员卡被选择，则继续查询是否可以按照优惠价格保存订单
            if (userCardEntitys.getNxCucaIsSelected() == 1) {
                Map<String, Object> mapC = new HashMap<>();
                mapC.put("orderUserId", orderUserId);
                mapC.put("deskId", deskId);
                mapC.put("goodsId", goodsId);
                mapC.put("status", -1);
                mapC.put("dayuDiffPrice", 0);
                mapC.put("remark", remark);
                mapC.put("orderType", orderType);
                mapC.put("splicingOrderId", spId);
                mapC.put("serviceType", serviceType);
                //1.1.1 先查询优惠订单是否超过数量
                System.out.println("ccccccccccaaaaRemarkkekeek" + mapC);
                NxCommunityOrdersSubEntity communityOrdersSubEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapC);
                if (communityOrdersSubEntity != null && goodsEntity.getNxCgGoodsHuaxianPrice() != null) {
                    BigDecimal huaxianQuantity = new BigDecimal(goodsEntity.getNxCgGoodsHuaxianQuantity());
                    Map<String, Object> mapOrderQuantity = new HashMap<>();
                    mapOrderQuantity.put("date", formatWhatDay(0));
                    mapOrderQuantity.put("orderUserId", orderUserId);
                    mapOrderQuantity.put("goodsId", goodsId);
                    int orderQuantityInt = nxCommunityOrdersSubService.queryTodayHuaxianCount(mapOrderQuantity);
                    BigDecimal orderQuantity = new BigDecimal(orderQuantityInt);
                    Map<String, Object> mapT = new HashMap<>();
                    mapT.put("orderUserId", orderUserId);
                    mapT.put("deskId", deskId);
                    mapT.put("goodsId", goodsId);
                    mapT.put("status", -1);
                    mapT.put("dayuDiffPrice", 0);
                    mapT.put("orderType", orderType);
                    mapT.put("splicingOrderId", spId);
                    mapT.put("serviceType", serviceType);
                    int count = nxCommunityOrdersSubService.querySubOrderCount(mapT);
                    System.out.println("akankankandocut" + count);
                    if (count > 0) {
                        int total = nxCommunityOrdersSubService.querySubOrderTotalHuaxianQuantity(mapT);
                        BigDecimal restQuantity = huaxianQuantity.subtract(new BigDecimal(total)); //剩余可用划线后优惠价格的数量
                        //1.1.1.1剩余数量大于 1，则加1
                        if (restQuantity.compareTo(new BigDecimal(0)) == 1) {
                            BigDecimal add = orderQuantity.add(new BigDecimal(1));
                            BigDecimal subtotal = new BigDecimal(communityOrdersSubEntity.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                            BigDecimal huaxianSubtotal = new BigDecimal(subHuaxianPrice).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                            communityOrdersSubEntity.setNxCosQuantity(add.toString());
                            communityOrdersSubEntity.setNxCosSubtotal(subtotal.toString());
                            communityOrdersSubEntity.setNxCosHuaxianSubtotal(huaxianSubtotal.toString());
                            nxCommunityOrdersSubService.update(communityOrdersSubEntity);
                        } else {
                            //1.1.1.2保存普通订单之前先查是否有同样的订单
                            Map<String, Object> mapZ = new HashMap<>();
                            mapZ.put("orderUserId", orderUserId);
                            mapZ.put("deskId", deskId);
                            mapZ.put("goodsId", goodsId);
                            mapZ.put("status", -1);
                            mapZ.put("diffPrice", 0);
                            mapZ.put("remark", remark);
                            mapZ.put("orderType", orderType);
                            mapZ.put("splicingOrderId", spId);
                            mapZ.put("serviceType", serviceType);
                            System.out.println("mappppzzzzzzRRRRRRRRRRRemarkkeke" + mapZ);
                            NxCommunityOrdersSubEntity communityOrdersSubZero = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapZ);
                            //已经有了普通订单，则修改数量
                            if (communityOrdersSubZero != null) {
                                System.out.println("meiyouzuozuozzlididiididm" + communityOrdersSubZero.getNxCosQuantity());
                                BigDecimal nxCosQuantity = new BigDecimal(communityOrdersSubZero.getNxCosQuantity());
                                BigDecimal add = nxCosQuantity.add(new BigDecimal(1));
                                BigDecimal subtotal = new BigDecimal(communityOrdersSubZero.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                                communityOrdersSubZero.setNxCosQuantity(add.toString());
                                communityOrdersSubZero.setNxCosSubtotal(subtotal.toString());
                                System.out.println("updddateeeee" + communityOrdersSubZero.getNxCosQuantity());
                                nxCommunityOrdersSubService.update(communityOrdersSubZero);

                            } else {
                                //添加信息普通订单
                                System.out.println("whgoodsnewCommmonssssss111Remakkke" + goodsEntity.getNxCgGoodsName());
                                NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                                subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                                Integer communityId = goodsEntity.getNxCgCommunityId();
                                NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
                                subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());

                                subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                                subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                                subEntity.setNxCosOrderUserId(orderUserId);
                                subEntity.setNxCosDeskId(deskId);
                                subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                                subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                                subEntity.setNxCosQuantity("1");
                                subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                                subEntity.setNxCosPrice(subPrice);
                                subEntity.setNxCosSubtotal(subPrice);
                                subEntity.setNxCosHuaxianDifferentPrice("0");
                                subEntity.setNxCosStatus(-1);
                                subEntity.setNxCosRemark(remark);
                                subEntity.setNxCosType(orderType);
                                subEntity.setNxCosServiceType(serviceType);
                                subEntity.setNxCosSplicingOrdersId(spId);
                                subEntity.setNxCosOrdersId(pindanId);
                                subEntity.setNxCosServiceDate(formatWhatDay(0));
                                nxCommunityOrdersSubService.save(subEntity);
                            }
                        }
                    }


                } else {
                    //2，如果

                    Map<String, Object> mapZ = new HashMap<>();
                    mapZ.put("orderUserId", orderUserId);
                    mapZ.put("deskId", deskId);
                    mapZ.put("goodsId", goodsId);
                    mapZ.put("status", -1);
                    mapZ.put("diffPrice", 0);
                    mapZ.put("remark", remark);
                    mapZ.put("orderType", orderType);
                    mapZ.put("splicingOrderId", spId);
                    mapZ.put("serviceType", serviceType);
                    System.out.println("mappppzzzzzz" + mapZ);
                    NxCommunityOrdersSubEntity communityOrdersSubZero = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapZ);
                    if (communityOrdersSubZero != null) {
                        System.out.println("meiyouzuozuozzlididiididm" + communityOrdersSubZero.getNxCosQuantity());
                        BigDecimal nxCosQuantity = new BigDecimal(communityOrdersSubZero.getNxCosQuantity());
                        BigDecimal add = nxCosQuantity.add(new BigDecimal(1));
                        BigDecimal subtotal = new BigDecimal(communityOrdersSubZero.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                        communityOrdersSubZero.setNxCosQuantity(add.toString());
                        communityOrdersSubZero.setNxCosSubtotal(subtotal.toString());
                        System.out.println("updddateeeee" + communityOrdersSubZero.getNxCosQuantity());
                        nxCommunityOrdersSubService.update(communityOrdersSubZero);

                    } else {
                        Map<String, Object> mapT = new HashMap<>();
                        mapT.put("orderUserId", orderUserId);
                        mapT.put("deskId", deskId);
                        mapT.put("goodsId", goodsId);
                        mapT.put("status", -1);
                        mapT.put("dayuDiffPrice", 0);
                        mapT.put("orderType", orderType);
                        mapT.put("splicingOrderId", spId);
                        mapT.put("serviceType", serviceType);
                        int count = nxCommunityOrdersSubService.querySubOrderCount(mapT);
                        if (count > 0) {
                            int total = nxCommunityOrdersSubService.querySubOrderTotalHuaxianQuantity(mapT);
                            BigDecimal huaxianQuantity = new BigDecimal(goodsEntity.getNxCgGoodsHuaxianQuantity());
                            BigDecimal restQuantity = huaxianQuantity.subtract(new BigDecimal(total)); //剩余可用划线后优惠价格的数量
                            //1.1.1.1剩余数量大于 1，则加1
                            if (restQuantity.compareTo(new BigDecimal(0)) == 1) {
                                NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                                subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                                Integer communityId = goodsEntity.getNxCgCommunityId();
                                NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
                                subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());
                                subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                                subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                                subEntity.setNxCosOrderUserId(orderUserId);
                                subEntity.setNxCosDeskId(deskId);
                                subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                                subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                                subEntity.setNxCosQuantity("1");
                                subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                                subEntity.setNxCosPrice(subPrice);
                                subEntity.setNxCosSubtotal(subPrice);
                                subEntity.setNxCosHuaxianPrice(subHuaxianPrice);
                                subEntity.setNxCosHuaxianDifferentPrice(differPrice.toString());
                                subEntity.setNxCosHuaxianSubtotal(subHuaxianPrice);
                                subEntity.setNxCosStatus(-1);
                                subEntity.setNxCosType(orderType);
                                subEntity.setNxCosServiceType(serviceType);
                                subEntity.setNxCosSplicingOrdersId(spId);
                                subEntity.setNxCosRemark(remark);
                                subEntity.setNxCosOrdersId(pindanId);
                                subEntity.setNxCosServiceDate(formatWhatDay(0));
                                nxCommunityOrdersSubService.save(subEntity);
                            } else {
                                NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                                subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                                Integer communityId = goodsEntity.getNxCgCommunityId();
                                NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
                                subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());

                                subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                                subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                                subEntity.setNxCosOrderUserId(orderUserId);
                                subEntity.setNxCosDeskId(deskId);
                                subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                                subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                                subEntity.setNxCosQuantity("1");
                                subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                                subEntity.setNxCosPrice(subHuaxianPrice);
                                subEntity.setNxCosSubtotal(subHuaxianPrice);
                                subEntity.setNxCosHuaxianDifferentPrice("0");
                                subEntity.setNxCosStatus(-1);
                                subEntity.setNxCosRemark(remark);
                                subEntity.setNxCosType(orderType);
                                subEntity.setNxCosServiceType(serviceType);
                                subEntity.setNxCosSplicingOrdersId(spId);
                                subEntity.setNxCosOrdersId(pindanId);
                                subEntity.setNxCosServiceDate(formatWhatDay(0));
                                nxCommunityOrdersSubService.save(subEntity);
                            }

                        } else {
                            //diyige
                            NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                            subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                            Integer communityId = goodsEntity.getNxCgCommunityId();
                            NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
                            subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());
                            subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                            subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                            subEntity.setNxCosOrderUserId(orderUserId);
                            subEntity.setNxCosDeskId(deskId);
                            subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                            subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                            subEntity.setNxCosQuantity("1");
                            subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                            subEntity.setNxCosPrice(subPrice);
                            subEntity.setNxCosSubtotal(subPrice);
                            subEntity.setNxCosHuaxianPrice(subHuaxianPrice);
                            subEntity.setNxCosHuaxianDifferentPrice(differPrice.toString());
                            subEntity.setNxCosHuaxianSubtotal(subHuaxianPrice);
                            subEntity.setNxCosStatus(-1);
                            subEntity.setNxCosType(orderType);
                            subEntity.setNxCosServiceType(serviceType);
                            subEntity.setNxCosSplicingOrdersId(spId);
                            subEntity.setNxCosRemark(remark);
                            subEntity.setNxCosOrdersId(pindanId);
                            subEntity.setNxCosServiceDate(formatWhatDay(0));
                            nxCommunityOrdersSubService.save(subEntity);
                        }
                    }
                }

            } else {
                //如果会员卡没有被选择，则按照划线价格添加订单
                Map<String, Object> mapS = new HashMap<>();
                mapS.put("orderUserId", orderUserId);
                mapS.put("deskId", deskId);
                mapS.put("goodsId", goodsId);
                mapS.put("status", -1);
                mapS.put("remark", remark);
                mapS.put("diffPrice", 0);
                mapS.put("orderType", orderType);
                mapS.put("splicingOrderId", spId);
                mapS.put("serviceType", serviceType);
                NxCommunityOrdersSubEntity subOrderEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapS);
                if (subOrderEntity != null) {
                    BigDecimal orderQuantity = new BigDecimal(subOrderEntity.getNxCosQuantity());
                    BigDecimal add = orderQuantity.add(new BigDecimal(1));
                    BigDecimal subtotal = new BigDecimal(subPrice).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                    subOrderEntity.setNxCosQuantity(add.toString());
                    subOrderEntity.setNxCosSubtotal(subtotal.toString());
                    nxCommunityOrdersSubService.update(subOrderEntity);
                } else {

                    System.out.println("whgoodsnewCommmon222222" + goodsEntity.getNxCgGoodsName());
                    NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                    subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                    Integer communityId = goodsEntity.getNxCgCommunityId();
                    NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
                    subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());
                    subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                    subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                    subEntity.setNxCosOrderUserId(orderUserId);
                    subEntity.setNxCosDeskId(deskId);
                    subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                    subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                    subEntity.setNxCosQuantity("1");
                    subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                    subEntity.setNxCosPrice(subHuaxianPrice);
                    subEntity.setNxCosSubtotal(subHuaxianPrice);
                    subEntity.setNxCosHuaxianDifferentPrice("0");
                    subEntity.setNxCosStatus(-1);
                    subEntity.setNxCosRemark(remark);
                    subEntity.setNxCosType(orderType);
                    subEntity.setNxCosServiceType(serviceType);
                    subEntity.setNxCosSplicingOrdersId(spId);
                    subEntity.setNxCosOrdersId(pindanId);
                    subEntity.setNxCosServiceDate(formatWhatDay(0));
                    nxCommunityOrdersSubService.save(subEntity);
                }
            }
        } else {
            //第一次保存订单
            System.out.println("dyiicbaocincindnd");

            NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
            subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
            Integer communityId = goodsEntity.getNxCgCommunityId();
            NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
            subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());
            subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
            subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
            subEntity.setNxCosOrderUserId(orderUserId);
            subEntity.setNxCosDeskId(deskId);
            subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
            subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
            subEntity.setNxCosQuantity("1");
            subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
            subEntity.setNxCosPrice(subPrice);
            subEntity.setNxCosSubtotal(subPrice);

            subEntity.setNxCosHuaxianPrice(subHuaxianPrice);
            subEntity.setNxCosHuaxianDifferentPrice(differPrice.toString());
            subEntity.setNxCosHuaxianSubtotal(subHuaxianPrice);
            subEntity.setNxCosStatus(-1);
            subEntity.setNxCosRemark(remark);
            subEntity.setNxCosType(orderType);
            subEntity.setNxCosServiceType(serviceType);
            subEntity.setNxCosSplicingOrdersId(spId);
            subEntity.setNxCosOrdersId(pindanId);
            subEntity.setNxCosServiceDate(formatWhatDay(0));
            nxCommunityOrdersSubService.save(subEntity);
            System.out.println("isshhdhdhhddhd" + goodsEntity.getNxCgCardId());
            if (goodsEntity.getNxCgCardId() != null) {
                NxCommunityCardEntity cardEntity = nxCommunityCardService.queryObject(goodsEntity.getNxCgCardId());
                NxCustomerUserCardEntity userCardEntity = new NxCustomerUserCardEntity();
                userCardEntity.setNxCucaStatus(-1);
                userCardEntity.setNxCucaCustomerUserId(orderUserId);
                userCardEntity.setNxCucaStartDate(formatWhatDay(0));
                userCardEntity.setNxCucaStopDate(formatWhatDay(Integer.valueOf(cardEntity.getNxCcEffectiveDays())));
                userCardEntity.setNxCucaCardId(cardEntity.getNxCommunityCardId());
                userCardEntity.setNxCucaCommunityId(cardEntity.getNxCcCommunityId());
                userCardEntity.setNxCucaIsSelected(1);
                userCardEntity.setNxCucaType(orderType);
                userCardEntity.setNxCucaComSplicingOrderId(spId);
                userCardEntity.setNxCucaComOrderId(pindanId);
                System.out.println("nimeiieyeoeueoueoeu" + userCardEntity.getNxCucaIsSelected());
                nxCustomerUserCardService.save(userCardEntity);
            }
        }


        //giveapply
        Map<String, Object> mapA = new HashMap<>();
        mapA.put("orderUserId", orderUserId);
        mapA.put("deskId", deskId);
        mapA.put("status", -1);
        mapA.put("orderType", orderType);
        mapA.put("splicingOrderId", spId);
        mapA.put("serviceType", serviceType);
        System.out.println("apappapap" + mapA);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);
        return R.ok().put("data", nxCommunityOrdersSubEntities);

    }

    @ResponseBody
    @RequestMapping(value = "/saveSubOrderRemarkHuaxianCard", method = RequestMethod.POST)
    public R saveSubOrderRemarkHuaxianCard(Integer goodsId, Integer orderUserId,Integer deskId,  String remark, Integer orderType, Integer spId,
                                           Integer pindanId, String subPrice, String subHuaxianPrice, Integer serviceType) {
        BigDecimal differPrice = new BigDecimal(subPrice).subtract(new BigDecimal(subHuaxianPrice)).setScale(1,BigDecimal.ROUND_HALF_UP);
        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
        Map<String, Object> map = new HashMap<>();
        map.put("cardId", goodsEntity.getNxCgCardId());
        map.put("stopTime", formatWhatDay(0));
        map.put("userId", orderUserId);
//        map.put("goodsId", goodsId);
//        map.put("status", -1);
//        map.put("type", orderType);
        map.put("splicingOrderId", spId);
        map.put("deskId", deskId);
        System.out.println("zehlieyoeucarddddddd" + map);
        NxCustomerUserCardEntity userCardEntitys = nxCustomerUserCardService.queryUserGoodsCard(map);
        // 一，如果已经有会员卡
        if (userCardEntitys != null) {

            //1.1 如果会员卡被选择，则继续查询是否可以按照优惠价格保存订单
            if (userCardEntitys.getNxCucaIsSelected() == 1) {

                Map<String, Object> mapC = new HashMap<>();
                mapC.put("orderUserId", orderUserId);
                mapC.put("deskId", deskId);
                mapC.put("goodsId", goodsId);
                mapC.put("status", -1);
                mapC.put("dayuDiffPrice", 0);
                mapC.put("remark", remark);
                mapC.put("orderType", orderType);
                mapC.put("splicingOrderId", spId);
                mapC.put("serviceType", serviceType);
                //1.1.1 先查询优惠订单是否超过数量
                NxCommunityOrdersSubEntity communityOrdersSubEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapC);
                System.out.println("wwwhwiiwiwiwiwwiiwiiwiiw" + communityOrdersSubEntity);

                if (communityOrdersSubEntity != null && goodsEntity.getNxCgGoodsHuaxianPrice() != null) {

                    BigDecimal huaxianQuantity = new BigDecimal(goodsEntity.getNxCgGoodsHuaxianQuantity());
                    Map<String, Object> mapOrderQuantity = new HashMap<>();
                    mapOrderQuantity.put("date", formatWhatDay(0));
                    mapOrderQuantity.put("orderUserId", orderUserId);
                    mapOrderQuantity.put("goodsId", goodsId);
                    int orderQuantityInt = nxCommunityOrdersSubService.queryTodayHuaxianCount(mapOrderQuantity);
                    BigDecimal orderQuantity = new BigDecimal(orderQuantityInt);
                    Map<String, Object> mapT = new HashMap<>();
                    mapT.put("orderUserId", orderUserId);
                    mapT.put("deskId", deskId);
                    mapT.put("goodsId", goodsId);
                    mapT.put("status", -1);
                    mapT.put("dayuDiffPrice", 0);
                    mapT.put("orderType", orderType);
                    mapT.put("splicingOrderId", spId);
                    mapT.put("serviceType", serviceType);
                    int total = nxCommunityOrdersSubService.querySubOrderTotalHuaxianQuantity(mapT);
                    BigDecimal restQuantity = huaxianQuantity.subtract(new BigDecimal(total)); //剩余可用划线后优惠价格的数量
                    //1.1.1.1剩余数量大于 1，则加1
                    if (restQuantity.compareTo(new BigDecimal(0)) == 1) {
                        BigDecimal add = orderQuantity.add(new BigDecimal(1));
                        BigDecimal subtotal = new BigDecimal(subPrice).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                        BigDecimal huaxianSubtotal = new BigDecimal(communityOrdersSubEntity.getNxCosHuaxianPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                        communityOrdersSubEntity.setNxCosQuantity(add.toString());
                        communityOrdersSubEntity.setNxCosSubtotal(subtotal.toString());
                        communityOrdersSubEntity.setNxCosHuaxianSubtotal(huaxianSubtotal.toString());
                        nxCommunityOrdersSubService.update(communityOrdersSubEntity);
                    } else {
                        //1.1.1.2保存普通订单之前先查是否有同样的订单
                        Map<String, Object> mapZ = new HashMap<>();
                        mapZ.put("orderUserId", orderUserId);
                        mapZ.put("deskId", deskId);
                        mapZ.put("goodsId", goodsId);
                        mapZ.put("status", -1);
                        mapZ.put("diffPrice", 0);
                        mapZ.put("remark", remark);
                        mapZ.put("orderType", orderType);
                        mapZ.put("splicingOrderId", spId);
                        mapZ.put("serviceType", serviceType);
                        System.out.println("mappppzzzzzz" + mapZ);
                        NxCommunityOrdersSubEntity communityOrdersSubZero = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapZ);
                        //已经有了普通订单，则修改数量
                        if (communityOrdersSubZero != null) {
                            BigDecimal nxCosQuantity = new BigDecimal(communityOrdersSubZero.getNxCosQuantity());
                            BigDecimal add = nxCosQuantity.add(new BigDecimal(1));
                            BigDecimal subtotal = new BigDecimal(subPrice).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                            communityOrdersSubZero.setNxCosQuantity(add.toString());
                            communityOrdersSubZero.setNxCosSubtotal(subtotal.toString());
                            System.out.println("updddateeeee" + communityOrdersSubZero.getNxCosQuantity());
                            nxCommunityOrdersSubService.update(communityOrdersSubZero);

                        } else {
                            //添加信息普通订单
                            //----
                            System.out.println("whgoodsnewCommmonssssss" + goodsEntity.getNxCgGoodsName());
                            NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                            subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                            Integer communityId = goodsEntity.getNxCgCommunityId();
                            NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
                            subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());
                            subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                            subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                            subEntity.setNxCosOrderUserId(orderUserId);
                            subEntity.setNxCosDeskId(deskId);
                            subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                            subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                            subEntity.setNxCosQuantity("1");
                            subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                            subEntity.setNxCosPrice(subHuaxianPrice);
                            subEntity.setNxCosSubtotal(subHuaxianPrice);
                            subEntity.setNxCosHuaxianDifferentPrice("0");
                            subEntity.setNxCosStatus(-1);
                            subEntity.setNxCosRemark(remark);
                            subEntity.setNxCosType(orderType);
                            subEntity.setNxCosServiceType(serviceType);
                            subEntity.setNxCosSplicingOrdersId(spId);
                            subEntity.setNxCosOrdersId(pindanId);
                            subEntity.setNxCosServiceDate(formatWhatDay(0));
                            nxCommunityOrdersSubService.save(subEntity);
                        }
                    }

                } else {
                    Map<String, Object> mapT = new HashMap<>();
                    mapT.put("orderUserId", orderUserId);
                    mapT.put("deskId", deskId);
                    mapT.put("goodsId", goodsId);
                    mapT.put("status", -1);
                    mapT.put("dayuDiffPrice", 0);
                    mapT.put("orderType", orderType);
                    mapT.put("splicingOrderId", spId);
                    mapT.put("serviceType", serviceType);
                    int count = nxCommunityOrdersSubService.querySubOrderCount(mapT);
                    if (count > 0) {
                        int total = nxCommunityOrdersSubService.querySubOrderTotalHuaxianQuantity(mapT);
                        BigDecimal huaxianQuantity = new BigDecimal(goodsEntity.getNxCgGoodsHuaxianQuantity());
                        BigDecimal restQuantity = huaxianQuantity.subtract(new BigDecimal(total)); //剩余可用划线后优惠价格的数量
                        //1.1.1.1剩余数量大于 1，则加1
                        if (restQuantity.compareTo(new BigDecimal(0)) == 1) {
                            System.out.println("nanndndndnddnzleiie");
                            NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                            subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                            Integer communityId = goodsEntity.getNxCgCommunityId();
                            NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
                            subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());
                            subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                            subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                            subEntity.setNxCosOrderUserId(orderUserId);
                            subEntity.setNxCosDeskId(deskId);
                            subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                            subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                            subEntity.setNxCosQuantity("1");
                            subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                            subEntity.setNxCosPrice(subPrice);
                            subEntity.setNxCosSubtotal(subPrice);
                            subEntity.setNxCosHuaxianPrice(subHuaxianPrice);
                            subEntity.setNxCosHuaxianDifferentPrice(differPrice.toString());
                            subEntity.setNxCosHuaxianSubtotal(subHuaxianPrice);
                            subEntity.setNxCosStatus(-1);
                            subEntity.setNxCosRemark(remark);
                            subEntity.setNxCosType(orderType);
                            subEntity.setNxCosServiceType(serviceType);
                            subEntity.setNxCosSplicingOrdersId(spId);
                            subEntity.setNxCosOrdersId(pindanId);
                            subEntity.setNxCosServiceDate(formatWhatDay(0));
                            nxCommunityOrdersSubService.save(subEntity);
                        } else {

                            System.out.println("jianchaputoggnfndndndndn");
                            Map<String, Object> mapS = new HashMap<>();
                            mapS.put("orderUserId", orderUserId);
                            mapS.put("deskId", deskId);
                            mapS.put("goodsId", goodsId);
                            mapS.put("status", -1);
                            mapS.put("remark", remark);
                            mapS.put("diffPrice", 0);
                            mapS.put("orderType", orderType);
                            mapS.put("splicingOrderId", spId);
                            mapS.put("serviceType", serviceType);
                            NxCommunityOrdersSubEntity subOrderEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapS);
                            if (subOrderEntity != null) {
                                BigDecimal orderQuantity = new BigDecimal(subOrderEntity.getNxCosQuantity());
                                BigDecimal add = orderQuantity.add(new BigDecimal(1));
                                BigDecimal subtotal = new BigDecimal(subPrice).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                                subOrderEntity.setNxCosQuantity(add.toString());
                                subOrderEntity.setNxCosSubtotal(subtotal.toString());
                                nxCommunityOrdersSubService.update(subOrderEntity);

                            } else {

                                System.out.println("whgoodsnewCommmon222222" + goodsEntity.getNxCgGoodsName());
                                NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                                subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                                Integer communityId = goodsEntity.getNxCgCommunityId();
                                NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
                                subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());
                                subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                                subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                                subEntity.setNxCosOrderUserId(orderUserId);
                                subEntity.setNxCosDeskId(deskId);
                                subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                                subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                                subEntity.setNxCosQuantity("1");
                                subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                                subEntity.setNxCosPrice(subHuaxianPrice);
                                subEntity.setNxCosSubtotal(subHuaxianPrice);
                                subEntity.setNxCosHuaxianDifferentPrice("0");
                                subEntity.setNxCosStatus(-1);
                                subEntity.setNxCosRemark(remark);
                                subEntity.setNxCosType(orderType);
                                subEntity.setNxCosServiceType(serviceType);
                                subEntity.setNxCosSplicingOrdersId(spId);
                                subEntity.setNxCosOrdersId(pindanId);
                                subEntity.setNxCosServiceDate(formatWhatDay(0));
                                nxCommunityOrdersSubService.save(subEntity);
                            }
                        }
                    } else {

                        //todo kk
                        System.out.println("whgoodsnewCommmon222222" + goodsEntity.getNxCgGoodsName());
                        NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                        subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                        Integer communityId = goodsEntity.getNxCgCommunityId();
                        NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
                        subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());
                        subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                        subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                        subEntity.setNxCosOrderUserId(orderUserId);
                        subEntity.setNxCosDeskId(deskId);
                        subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                        subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                        subEntity.setNxCosQuantity("1");
                        subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                        subEntity.setNxCosPrice(subPrice);
                        subEntity.setNxCosSubtotal(subPrice);
                        subEntity.setNxCosHuaxianPrice(subHuaxianPrice);
                        subEntity.setNxCosHuaxianDifferentPrice(differPrice.toString());
                        subEntity.setNxCosHuaxianSubtotal(subHuaxianPrice);
                        subEntity.setNxCosStatus(-1);
                        subEntity.setNxCosRemark(remark);
                        subEntity.setNxCosType(orderType);
                        subEntity.setNxCosServiceType(serviceType);
                        subEntity.setNxCosSplicingOrdersId(spId);
                        subEntity.setNxCosOrdersId(pindanId);
                        subEntity.setNxCosServiceDate(formatWhatDay(0));
                        nxCommunityOrdersSubService.save(subEntity);
                    }
                }


            } else {
                //如果会员卡没有被选择，则按照划线价格添加订单
                Map<String, Object> mapS = new HashMap<>();
                mapS.put("orderUserId", orderUserId);
                mapS.put("deskId", deskId);
                mapS.put("goodsId", goodsId);
                mapS.put("status", -1);
                mapS.put("remark", remark);
                mapS.put("diffPrice", 0);
                mapS.put("orderType", orderType);
                mapS.put("splicingOrderId", spId);
                mapS.put("serviceType", serviceType);
                NxCommunityOrdersSubEntity subOrderEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapS);
                if (subOrderEntity != null) {
                    System.out.println("neexxxx222");
                    BigDecimal orderQuantity = new BigDecimal(subOrderEntity.getNxCosQuantity());
                    BigDecimal add = orderQuantity.add(new BigDecimal(1));
                    BigDecimal subtotal = new BigDecimal(subPrice).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                    subOrderEntity.setNxCosQuantity(add.toString());
                    subOrderEntity.setNxCosSubtotal(subtotal.toString());
                    nxCommunityOrdersSubService.update(subOrderEntity);

                } else {

                    System.out.println("whgoodsnewCommmon222222" + goodsEntity.getNxCgGoodsName());
                    NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                    subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                    Integer communityId = goodsEntity.getNxCgCommunityId();
                    NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
                    subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());
                    subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                    subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                    subEntity.setNxCosOrderUserId(orderUserId);
                    subEntity.setNxCosDeskId(deskId);
                    subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                    subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                    subEntity.setNxCosQuantity("1");
                    subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                    subEntity.setNxCosPrice(subHuaxianPrice);
                    subEntity.setNxCosSubtotal(subHuaxianPrice);
                    subEntity.setNxCosHuaxianDifferentPrice("0");
                    subEntity.setNxCosStatus(-1);
                    subEntity.setNxCosRemark(remark);
                    subEntity.setNxCosType(orderType);
                    subEntity.setNxCosServiceType(serviceType);
                    subEntity.setNxCosSplicingOrdersId(spId);
                    subEntity.setNxCosOrdersId(pindanId);
                    subEntity.setNxCosServiceDate(formatWhatDay(0));
                    nxCommunityOrdersSubService.save(subEntity);
                }
            }


        } else {


            //二，如果没有会员卡，则按照普通订单保存，并添加未选择会员卡
            System.out.println("whgoodsnewCommmon222222333" + goodsEntity.getNxCgGoodsName());
            NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
            subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
            Integer communityId = goodsEntity.getNxCgCommunityId();
            NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
            subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());
            subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
            subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
            subEntity.setNxCosOrderUserId(orderUserId);
            subEntity.setNxCosDeskId(deskId);
            subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
            subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
            subEntity.setNxCosQuantity("1");
            subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
            subEntity.setNxCosPrice(subHuaxianPrice);
            subEntity.setNxCosSubtotal(subHuaxianPrice);
            subEntity.setNxCosHuaxianDifferentPrice("0");
            subEntity.setNxCosHuaxianSubtotal(subHuaxianPrice);
            subEntity.setNxCosStatus(-1);
            subEntity.setNxCosRemark(remark);
            subEntity.setNxCosType(orderType);
            subEntity.setNxCosServiceType(serviceType);
            subEntity.setNxCosSplicingOrdersId(spId);
            subEntity.setNxCosOrdersId(pindanId);
            subEntity.setNxCosServiceDate(formatWhatDay(0));
            nxCommunityOrdersSubService.save(subEntity);

            NxCommunityCardEntity cardEntity = nxCommunityCardService.queryObject(goodsEntity.getNxCgCardId());
            NxCustomerUserCardEntity userCardEntity = new NxCustomerUserCardEntity();
            userCardEntity.setNxCucaStatus(-1);
            userCardEntity.setNxCucaCustomerUserId(orderUserId);
            userCardEntity.setNxCucaStartDate(formatWhatDay(0));
            userCardEntity.setNxCucaStopDate(formatWhatDay(Integer.valueOf(cardEntity.getNxCcEffectiveDays())));
            userCardEntity.setNxCucaCardId(cardEntity.getNxCommunityCardId());
            userCardEntity.setNxCucaCommunityId(cardEntity.getNxCcCommunityId());
            userCardEntity.setNxCucaIsSelected(0);
            userCardEntity.setNxCucaType(orderType);
            userCardEntity.setNxCucaComSplicingOrderId(spId);
            userCardEntity.setNxCucaComOrderId(pindanId);
            System.out.println("nimeiieyeoeueoueoeu" + userCardEntity.getNxCucaIsSelected());
            nxCustomerUserCardService.save(userCardEntity);
        }

        return R.ok();

    }


    @ResponseBody
    @RequestMapping(value = "/reduceSubOrderRemarkCard", method = RequestMethod.POST)
    public R reduceSubOrderRemarkCard(Integer goodsId, Integer orderUserId, Integer deskId, String remark, Integer orderType) {

        Map<String, Object> map = new HashMap<>();
        map.put("orderUserId", orderUserId);
        map.put("deskId", deskId);
        map.put("goodsId", goodsId);
        map.put("status", -1);
        map.put("diffPrice", 0);
        map.put("remark", remark);
        map.put("orderType", orderType);
        System.out.println("reeeeke" + map);
        NxCommunityOrdersSubEntity diffZeroSubOrderEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(map);
        if (diffZeroSubOrderEntity != null) {
            BigDecimal orderQuantity = new BigDecimal(diffZeroSubOrderEntity.getNxCosQuantity());
            if (orderQuantity.compareTo(new BigDecimal(1)) == 0) {
                //1，删除订单
                nxCommunityOrdersSubService.delete(diffZeroSubOrderEntity.getNxCommunityOrdersSubId());
            } else {
                BigDecimal quantity = new BigDecimal(diffZeroSubOrderEntity.getNxCosQuantity()).subtract(new BigDecimal(1));
                BigDecimal decimal = new BigDecimal(diffZeroSubOrderEntity.getNxCosPrice()).multiply(quantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                diffZeroSubOrderEntity.setNxCosQuantity(quantity.toString());
                diffZeroSubOrderEntity.setNxCosSubtotal(decimal.toString());
                if (diffZeroSubOrderEntity.getNxCosHuaxianPrice() != null) {
                    BigDecimal decimal1 = new BigDecimal(diffZeroSubOrderEntity.getNxCosHuaxianPrice()).multiply(quantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                    diffZeroSubOrderEntity.setNxCosHuaxianSubtotal(decimal1.toString());
                }
                nxCommunityOrdersSubService.update(diffZeroSubOrderEntity);
            }
        } else {
            Map<String, Object> mapD = new HashMap<>();
            mapD.put("orderUserId", orderUserId);
            mapD.put("deskId", deskId);
            mapD.put("goodsId", goodsId);
            mapD.put("status", -1);
            mapD.put("dayuDiffPrice", 0);
            mapD.put("remark", remark);
            mapD.put("orderType", orderType);
            System.out.println("dayoouuiiuiououo" + mapD);
            //有优惠的订单
            NxCommunityOrdersSubEntity diffSubOrderEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapD);
            if (diffSubOrderEntity != null) {
                BigDecimal orderQuantity = new BigDecimal(diffSubOrderEntity.getNxCosQuantity());
                if (orderQuantity.compareTo(new BigDecimal(1)) == 0) {
                    //1，删除订单
                    nxCommunityOrdersSubService.delete(diffSubOrderEntity.getNxCommunityOrdersSubId());
                } else {
                    BigDecimal quantity = new BigDecimal(diffSubOrderEntity.getNxCosQuantity()).subtract(new BigDecimal(1));
                    BigDecimal decimal = new BigDecimal(diffSubOrderEntity.getNxCosPrice()).multiply(quantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                    diffSubOrderEntity.setNxCosQuantity(quantity.toString());
                    diffSubOrderEntity.setNxCosSubtotal(decimal.toString());
                    if (diffSubOrderEntity.getNxCosHuaxianPrice() != null) {
                        BigDecimal decimal1 = new BigDecimal(diffSubOrderEntity.getNxCosHuaxianPrice()).multiply(quantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                        diffSubOrderEntity.setNxCosHuaxianSubtotal(decimal1.toString());
                    }
                    nxCommunityOrdersSubService.update(diffSubOrderEntity);
                }

            }
        }

        //giveapply
        Map<String, Object> mapA = new HashMap<>();
        mapA.put("orderUserId", orderUserId);
        mapA.put("deskId", deskId);
        mapA.put("status", -1);
        mapA.put("orderType", orderType);
        System.out.println("apappapap" + mapA);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);


        if (nxCommunityOrdersSubEntities.size() > 0) {

            Map<String, Object> mapCard = new HashMap<>();
            mapCard.put("status", -1);
            mapCard.put("userId", orderUserId);
            mapCard.put("deskId", deskId);
            List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapCard);
            int hasApply = 0;
            for (NxCustomerUserCardEntity userCardEntity : cardEntities) {

                NxCommunityCardEntity cardEntity = nxCommunityCardService.queryObject(userCardEntity.getNxCucaCardId());
                Integer nxCommunityCardId = cardEntity.getNxCommunityCardId();
                for (NxCommunityOrdersSubEntity subEntity : nxCommunityOrdersSubEntities) {
                    Integer nxCosCommunityGoodsId = subEntity.getNxCosCommunityGoodsId();
                    NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(nxCosCommunityGoodsId);
                    if (goodsEntity.getNxCgCardId() != null) {
                        if (goodsEntity.getNxCgCardId().equals(nxCommunityCardId)) {
                            hasApply = 1;
                        }
                    }
                }
                if (hasApply == 0) {
                    nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
                }
            }

        } else {
            Map<String, Object> mapCard = new HashMap<>();
            mapCard.put("status", -1);
            mapCard.put("userId", orderUserId);
            mapCard.put("deskId", deskId);
            List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapCard);
            for (NxCustomerUserCardEntity userCardEntity : cardEntities) {
                nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
            }

        }


        Map<String, Object> mapCard = new HashMap<>();
        mapCard.put("status", -1);
        mapCard.put("userId", orderUserId);
        mapCard.put("deskId", deskId);
        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapCard);

        Map<String, Object> mapData = new HashMap<>();
        mapData.put("arr", nxCommunityOrdersSubEntities);
        mapData.put("cardList", cardEntities);


        return R.ok().put("data", mapData);

    }


    @ResponseBody
    @RequestMapping(value = "/addSubOrder", method = RequestMethod.POST)
    public R addSubOrder(Integer goodsId, Integer orderUserId, Integer deskId, Integer orderType, Integer spId, Integer pindanId,Integer serviceType) {

        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);

        if (goodsEntity.getNxCgGoodsHuaxianPrice() == null) {
            //commAdd
            Map<String, Object> map = new HashMap<>();
            map.put("orderUserId", orderUserId);
            map.put("deskId", deskId);
            map.put("goodsId", goodsId);
            map.put("orderType", orderType);
            map.put("status", -1);
            map.put("splicingOrderId", spId);
            map.put("serviceType", serviceType);
            NxCommunityOrdersSubEntity subOrderEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(map);
            if (subOrderEntity != null) {
                BigDecimal orderQuantity = new BigDecimal(subOrderEntity.getNxCosQuantity());
                BigDecimal add = orderQuantity.add(new BigDecimal(1));
                BigDecimal subtotal = new BigDecimal(subOrderEntity.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                subOrderEntity.setNxCosQuantity(add.toString());
                subOrderEntity.setNxCosSubtotal(subtotal.toString());
                subOrderEntity.setNxCosServiceDate(formatWhatDay(0));
                nxCommunityOrdersSubService.update(subOrderEntity);
            }

        } else {
            //pandan
            Map<String, Object> map = new HashMap<>();
            map.put("orderUserId", orderUserId);
            map.put("deskId", deskId);
            map.put("goodsId", goodsId);
            map.put("orderType", orderType);
            map.put("status", -1);
            map.put("diffPrice", 0);
            map.put("splicingOrderId", spId);
            map.put("serviceType", serviceType);
            //先查是否有普通订单
            System.out.println("addddddd" + map);
            NxCommunityOrdersSubEntity putongSubOrder = nxCommunityOrdersSubService.queryChangeSubOrderByParams(map);
            if (putongSubOrder != null) {
                //普通订单加 1
                BigDecimal orderQuantity = new BigDecimal(putongSubOrder.getNxCosQuantity());
                BigDecimal add = orderQuantity.add(new BigDecimal(1));
                BigDecimal subtotal = new BigDecimal(putongSubOrder.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                putongSubOrder.setNxCosQuantity(add.toString());
                putongSubOrder.setNxCosSubtotal(subtotal.toString());
                putongSubOrder.setNxCosServiceDate(formatWhatDay(0));
                nxCommunityOrdersSubService.update(putongSubOrder);

            } else {
                //查询优惠订单
                Map<String, Object> mapC = new HashMap<>();
                mapC.put("orderUserId", orderUserId);
                mapC.put("deskId", deskId);
                mapC.put("goodsId", goodsId);
                mapC.put("status", -1);
                mapC.put("orderType", orderType);
                mapC.put("dayuDiffPrice", 0);
                mapC.put("splicingOrderId", spId);
                mapC.put("serviceType", serviceType);
                //先查询优惠订单是否超过数量
                System.out.println("youhuidiadddddd" + mapC);
                NxCommunityOrdersSubEntity communityOrdersSubEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapC);
                if (communityOrdersSubEntity != null && goodsEntity.getNxCgGoodsHuaxianPrice() != null) {

                    Map<String, Object> mapOrderQuantity = new HashMap<>();
                    mapOrderQuantity.put("date", formatWhatDay(0));
                    mapOrderQuantity.put("orderUserId", orderUserId);
                    mapOrderQuantity.put("goodsId", goodsId);
                    System.out.println("chaxiniorderh" + mapOrderQuantity);
                    int orderQuantityInt = nxCommunityOrdersSubService.queryTodayHuaxianCount(mapOrderQuantity);
                    BigDecimal orderQuantity = new BigDecimal(orderQuantityInt);
                    //huxianQuantity
                    if (goodsEntity.getNxCgGoodsHuaxianQuantity() != null) {
                        BigDecimal huaxianQuantity = new BigDecimal(goodsEntity.getNxCgGoodsHuaxianQuantity());
                        //剩余可用划线后优惠价格的数量
                        BigDecimal restQuantity = huaxianQuantity.subtract(orderQuantity);
                        System.out.println("resssddfsadsfdafafaaa" + restQuantity);
                        //剩余数量大于 1，则加1
                        if (restQuantity.compareTo(new BigDecimal(0)) == 1) {
                            BigDecimal add = orderQuantity.add(new BigDecimal(1));
                            BigDecimal subtotal = new BigDecimal(communityOrdersSubEntity.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                            BigDecimal huaxianSubtotal = new BigDecimal(communityOrdersSubEntity.getNxCosHuaxianPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                            communityOrdersSubEntity.setNxCosQuantity(add.toString());
                            communityOrdersSubEntity.setNxCosSubtotal(subtotal.toString());
                            communityOrdersSubEntity.setNxCosHuaxianSubtotal(huaxianSubtotal.toString());
                            nxCommunityOrdersSubService.update(communityOrdersSubEntity);
                        } else {
                            System.out.println("whgoodsnewCommmon" + goodsEntity.getNxCgGoodsName());
                            NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                            subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                            Integer communityId = goodsEntity.getNxCgCommunityId();
                            NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
                            subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());
                            subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                            subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                            subEntity.setNxCosOrderUserId(orderUserId);
                            subEntity.setNxCosDeskId(deskId);
                            subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                            subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                            subEntity.setNxCosQuantity("1");
                            subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                            subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
                            subEntity.setNxCosSubtotal(goodsEntity.getNxCgGoodsHuaxianPrice());
                            subEntity.setNxCosHuaxianDifferentPrice("0");
                            subEntity.setNxCosStatus(-1);
                            subEntity.setNxCosType(orderType);
                            subEntity.setNxCosServiceType(serviceType);
                            subEntity.setNxCosSplicingOrdersId(spId);
                            subEntity.setNxCosOrdersId(pindanId);
                            subEntity.setNxCosServiceDate(formatWhatDay(0));
                            nxCommunityOrdersSubService.save(subEntity);
                        }
                    } else {
                        //update
                        BigDecimal add = orderQuantity.add(new BigDecimal(1));
                        BigDecimal subtotal = new BigDecimal(communityOrdersSubEntity.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                        BigDecimal huaxianSubtotal = new BigDecimal(communityOrdersSubEntity.getNxCosHuaxianPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                        communityOrdersSubEntity.setNxCosQuantity(add.toString());
                        communityOrdersSubEntity.setNxCosSubtotal(subtotal.toString());
                        communityOrdersSubEntity.setNxCosHuaxianSubtotal(huaxianSubtotal.toString());
                        nxCommunityOrdersSubService.update(communityOrdersSubEntity);

                    }
                }
            }
        }

        //giveapply
        Map<String, Object> mapA = new HashMap<>();
        mapA.put("orderUserId", orderUserId);
        mapA.put("deskId", deskId);
        mapA.put("status", -1);
        mapA.put("oderType", orderType);
        mapA.put("splicingOrderId", spId);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);

        return R.ok().put("data", nxCommunityOrdersSubEntities);

    }


    @ResponseBody
    @RequestMapping(value = "/addSubOrderReduce", method = RequestMethod.POST)
    public R addSubOrderReduce(Integer goodsId, Integer orderUserId, Integer orderId, Integer deskId,   Integer number) {

        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
        NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
        subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
        Integer communityId = goodsEntity.getNxCgCommunityId();
        NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
        subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());
        subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
        subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
        subEntity.setNxCosOrderUserId(orderUserId);
        subEntity.setNxCosDeskId(deskId);
        subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
        subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
        subEntity.setNxCosQuantity(number.toString());
        subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsPrice());
        subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
        subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsPrice());
        subEntity.setNxCosSubtotal(goodsEntity.getNxCgGoodsHuaxianPrice());
        subEntity.setNxCosHuaxianDifferentPrice("0");
        BigDecimal multiply = new BigDecimal(number).multiply(new BigDecimal(goodsEntity.getNxCgGoodsPrice()));
        subEntity.setNxCosSubtotal("-" + multiply.toString());
        subEntity.setNxCosStatus(-1);
        subEntity.setNxCosType(9);
        subEntity.setNxCosServiceType(0);
        subEntity.setNxCosSplicingOrdersId(-1);
        subEntity.setNxCosOrdersId(orderId);
        subEntity.setNxCosServiceDate(formatWhatDay(0));
        nxCommunityOrdersSubService.save(subEntity);

        NxCommunityOrdersEntity ordersEntity = nxCommunityOrdersService.queryObject(orderId);
        BigDecimal decimal = new BigDecimal(ordersEntity.getNxCoTotal()).subtract(multiply).setScale(1, BigDecimal.ROUND_HALF_UP);
        ordersEntity.setNxCoTotal(decimal.toString());
        nxCommunityOrdersService.update(ordersEntity);

        String content;
        content = "<CB>退货</CB><BR>";
        content = goodsEntity.getNxCgGoodsName() +  " " + number + "<BR>";

        //通过POST请求，发送打印信息到服务器
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(30000)//读取超时
                .setConnectTimeout(30000)//连接超时
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpPost post = new HttpPost(URL);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("user", USER));
        String STIME = String.valueOf(System.currentTimeMillis() / 1000);
        nvps.add(new BasicNameValuePair("stime", STIME));
        nvps.add(new BasicNameValuePair("sig", signature(USER, UKEY, STIME)));
        nvps.add(new BasicNameValuePair("apiname", "Open_printMsg"));//固定值,不需要修改
        nvps.add(new BasicNameValuePair("sn", "924610660"));
        nvps.add(new BasicNameValuePair("content", content));
        nvps.add(new BasicNameValuePair("times", "1"));//打印联数

        CloseableHttpResponse response = null;
        String result = null;
        try {
            post.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
            response = httpClient.execute(post);
            int statecode = response.getStatusLine().getStatusCode();
            if (statecode == 200) {
                HttpEntity httpentity = response.getEntity();
                if (httpentity != null) {
                    //服务器返回的JSON字符串，建议要当做日志记录起来
                    result = EntityUtils.toString(httpentity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                post.abort();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("resulslsl" + result);



        return R.ok().put("data", subEntity);


    }

    //生成签名字符串
    private static String signature(String USER,String UKEY,String STIME){
        String s = DigestUtils.sha1Hex(USER+UKEY+STIME);
        return s;
    }
//
//    @ResponseBody
//    @RequestMapping(value = "/addSubOrderDesk", method = RequestMethod.POST)
//    public R addSubOrderDesk(Integer goodsId, Integer deskId, Integer orderType, Integer spId, Integer pindanId,Integer serviceType) {
//
//        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
//
//        if (goodsEntity.getNxCgGoodsHuaxianPrice() == null) {
//            //commAdd
//            Map<String, Object> map = new HashMap<>();
//            map.put("deskId", deskId);
//            map.put("goodsId", goodsId);
//            map.put("orderType", orderType);
//            map.put("status", -1);
//            map.put("splicingOrderId", spId);
//            map.put("serviceType", serviceType);
//            NxCommunityOrdersSubEntity subOrderEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(map);
//            if (subOrderEntity != null) {
//                BigDecimal orderQuantity = new BigDecimal(subOrderEntity.getNxCosQuantity());
//                BigDecimal add = orderQuantity.add(new BigDecimal(1));
//                BigDecimal subtotal = new BigDecimal(subOrderEntity.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
//                subOrderEntity.setNxCosQuantity(add.toString());
//                subOrderEntity.setNxCosSubtotal(subtotal.toString());
//                nxCommunityOrdersSubService.update(subOrderEntity);
//            }
//
//        } else {
//            //pandan
//            Map<String, Object> map = new HashMap<>();
//            map.put("deskId", deskId);
//            map.put("goodsId", goodsId);
//            map.put("orderType", orderType);
//            map.put("status", -1);
//            map.put("diffPrice", 0);
//            map.put("splicingOrderId", spId);
//            map.put("serviceType", serviceType);
//            //先查是否有普通订单
//            System.out.println("addddddd" + map);
//            NxCommunityOrdersSubEntity putongSubOrder = nxCommunityOrdersSubService.queryChangeSubOrderByParams(map);
//            if (putongSubOrder != null) {
//                //普通订单加 1
//                BigDecimal orderQuantity = new BigDecimal(putongSubOrder.getNxCosQuantity());
//                BigDecimal add = orderQuantity.add(new BigDecimal(1));
//                BigDecimal subtotal = new BigDecimal(putongSubOrder.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
//                putongSubOrder.setNxCosQuantity(add.toString());
//                putongSubOrder.setNxCosSubtotal(subtotal.toString());
//                nxCommunityOrdersSubService.update(putongSubOrder);
//
//            } else {
//                //查询优惠订单
//                Map<String, Object> mapC = new HashMap<>();
//                mapC.put("deskId", deskId);
//                mapC.put("goodsId", goodsId);
//                mapC.put("status", -1);
//                mapC.put("orderType", orderType);
//                mapC.put("dayuDiffPrice", 0);
//                mapC.put("splicingOrderId", spId);
//                mapC.put("serviceType", serviceType);
//                //先查询优惠订单是否超过数量
//                System.out.println("youhuidiadddddd" + mapC);
//                NxCommunityOrdersSubEntity communityOrdersSubEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapC);
//                if (communityOrdersSubEntity != null) {
//
//                    BigDecimal orderQuantity = new BigDecimal(communityOrdersSubEntity.getNxCosQuantity());
//                    //huxianQuantity
//                    if (goodsEntity.getNxCgGoodsHuaxianQuantity() != null) {
//                        BigDecimal huaxianQuantity = new BigDecimal(goodsEntity.getNxCgGoodsHuaxianQuantity());
//                        //剩余可用划线后优惠价格的数量
//                        BigDecimal restQuantity = huaxianQuantity.subtract(orderQuantity);
//                        System.out.println("resssddfsadsfdafafaaa" + restQuantity);
//                        //剩余数量大于 1，则加1
//                        if (restQuantity.compareTo(new BigDecimal(0)) == 1) {
//                            BigDecimal add = orderQuantity.add(new BigDecimal(1));
//                            BigDecimal subtotal = new BigDecimal(communityOrdersSubEntity.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
//                            BigDecimal huaxianSubtotal = new BigDecimal(communityOrdersSubEntity.getNxCosHuaxianPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
//                            communityOrdersSubEntity.setNxCosQuantity(add.toString());
//                            communityOrdersSubEntity.setNxCosSubtotal(subtotal.toString());
//                            communityOrdersSubEntity.setNxCosHuaxianSubtotal(huaxianSubtotal.toString());
//                            nxCommunityOrdersSubService.update(communityOrdersSubEntity);
//                        } else {
//                            System.out.println("whgoodsnewCommmon" + goodsEntity.getNxCgGoodsName());
//                            NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
//                            subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
//                            Integer communityId = goodsEntity.getNxCgCommunityId();
//                            NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
//                            subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());
//                            subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
//                            subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
//                            subEntity.setNxCosOrderUserId(-1);
//                            subEntity.setNxCosDeskId(deskId);
//                            subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
//                            subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
//                            subEntity.setNxCosQuantity("1");
//                            subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
//                            subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
//                            subEntity.setNxCosSubtotal(goodsEntity.getNxCgGoodsHuaxianPrice());
//                            subEntity.setNxCosHuaxianDifferentPrice("0");
//                            subEntity.setNxCosStatus(-1);
//                            subEntity.setNxCosType(orderType);
//                            subEntity.setNxCosServiceType(serviceType);
//                            subEntity.setNxCosSplicingOrdersId(spId);
//                            subEntity.setNxCosOrdersId(pindanId);
//                            nxCommunityOrdersSubService.save(subEntity);
//                        }
//                    } else {
//                        //update
//                        BigDecimal add = orderQuantity.add(new BigDecimal(1));
//                        BigDecimal subtotal = new BigDecimal(communityOrdersSubEntity.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
//                        BigDecimal huaxianSubtotal = new BigDecimal(communityOrdersSubEntity.getNxCosHuaxianPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
//                        communityOrdersSubEntity.setNxCosQuantity(add.toString());
//                        communityOrdersSubEntity.setNxCosSubtotal(subtotal.toString());
//                        communityOrdersSubEntity.setNxCosHuaxianSubtotal(huaxianSubtotal.toString());
//                        nxCommunityOrdersSubService.update(communityOrdersSubEntity);
//
//                    }
//                }
//            }
//        }
//
//        //giveapply
//        Map<String, Object> mapA = new HashMap<>();
//        mapA.put("deskId", deskId);
//        mapA.put("status", -1);
//        mapA.put("oderType", orderType);
//        mapA.put("splicingOrderId", spId);
//        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);
//
//        return R.ok().put("data", nxCommunityOrdersSubEntities);
//
////        }
//
//    }




    @ResponseBody
    @RequestMapping(value = "/reduceSubOrder", method = RequestMethod.POST)
    public R reduceSubOrder(Integer goodsId, Integer orderUserId, Integer orderType, Integer spId) {

        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
        if (goodsEntity.getNxCgGoodsHuaxianPrice() != null) {

            Map<String, Object> map = new HashMap<>();
            map.put("orderUserId", orderUserId);
            map.put("goodsId", goodsId);
            map.put("status", -1);
            map.put("diffPrice", 0);
            map.put("orderType", orderType);
            map.put("splicingOrderId", spId);
            NxCommunityOrdersSubEntity diffZeroSubOrderEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(map);
            if (diffZeroSubOrderEntity != null) {
                BigDecimal orderQuantity = new BigDecimal(diffZeroSubOrderEntity.getNxCosQuantity());
                if (orderQuantity.compareTo(new BigDecimal(1)) == 0) {
                    //1，删除订单
                    nxCommunityOrdersSubService.delete(diffZeroSubOrderEntity.getNxCommunityOrdersSubId());
                } else {
                    BigDecimal quantity = new BigDecimal(diffZeroSubOrderEntity.getNxCosQuantity()).subtract(new BigDecimal(1));
                    BigDecimal decimal = new BigDecimal(diffZeroSubOrderEntity.getNxCosPrice()).multiply(quantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                    diffZeroSubOrderEntity.setNxCosQuantity(quantity.toString());
                    diffZeroSubOrderEntity.setNxCosSubtotal(decimal.toString());
                    if (diffZeroSubOrderEntity.getNxCosHuaxianPrice() != null) {
                        BigDecimal decimal1 = new BigDecimal(diffZeroSubOrderEntity.getNxCosHuaxianPrice()).multiply(quantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                        diffZeroSubOrderEntity.setNxCosHuaxianSubtotal(decimal1.toString());
                    }
                    nxCommunityOrdersSubService.update(diffZeroSubOrderEntity);
                }
            } else {
                Map<String, Object> mapD = new HashMap<>();
                mapD.put("orderUserId", orderUserId);
                mapD.put("goodsId", goodsId);
                mapD.put("status", -1);
                mapD.put("dayuDiffPrice", 0);
                mapD.put("orderType", orderType);
                mapD.put("splicingOrderId", spId);
                System.out.println("dayoouuiiuiououoaa" + mapD);
                //有优惠的订单
                NxCommunityOrdersSubEntity diffSubOrderEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapD);
                if (diffSubOrderEntity != null) {
                    BigDecimal orderQuantity = new BigDecimal(diffSubOrderEntity.getNxCosQuantity());
                    if (orderQuantity.compareTo(new BigDecimal(1)) == 0) {
                        //1，删除订单
                        nxCommunityOrdersSubService.delete(diffSubOrderEntity.getNxCommunityOrdersSubId());
                    } else {
                        BigDecimal quantity = new BigDecimal(diffSubOrderEntity.getNxCosQuantity()).subtract(new BigDecimal(1));
                        BigDecimal decimal = new BigDecimal(diffSubOrderEntity.getNxCosPrice()).multiply(quantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                        diffSubOrderEntity.setNxCosQuantity(quantity.toString());
                        diffSubOrderEntity.setNxCosSubtotal(decimal.toString());
                        if (diffSubOrderEntity.getNxCosHuaxianPrice() != null) {
                            BigDecimal decimal1 = new BigDecimal(diffSubOrderEntity.getNxCosHuaxianPrice()).multiply(quantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                            diffSubOrderEntity.setNxCosHuaxianSubtotal(decimal1.toString());
                        }
                        nxCommunityOrdersSubService.update(diffSubOrderEntity);
                    }
                }
            }

        } else {

            //common -
            Map<String, Object> map = new HashMap<>();
            map.put("orderUserId", orderUserId);
            map.put("goodsId", goodsId);
            map.put("status", -1);
            map.put("orderType", orderType);
            map.put("splicingOrderId", spId);
            NxCommunityOrdersSubEntity commSubOrderEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(map);
            if (commSubOrderEntity != null) {
                BigDecimal orderQuantity = new BigDecimal(commSubOrderEntity.getNxCosQuantity());
                if (orderQuantity.compareTo(new BigDecimal(1)) == 0) {
                    //1，删除订单
                    nxCommunityOrdersSubService.delete(commSubOrderEntity.getNxCommunityOrdersSubId());
                } else {
                    BigDecimal quantity = new BigDecimal(commSubOrderEntity.getNxCosQuantity()).subtract(new BigDecimal(1));
                    BigDecimal decimal = new BigDecimal(commSubOrderEntity.getNxCosPrice()).multiply(quantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                    commSubOrderEntity.setNxCosQuantity(quantity.toString());
                    commSubOrderEntity.setNxCosSubtotal(decimal.toString());
                    if (commSubOrderEntity.getNxCosHuaxianPrice() != null) {
                        BigDecimal decimal1 = new BigDecimal(commSubOrderEntity.getNxCosHuaxianPrice()).multiply(quantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                        commSubOrderEntity.setNxCosHuaxianSubtotal(decimal1.toString());
                    }
                    nxCommunityOrdersSubService.update(commSubOrderEntity);
                }
            }

        }

        //giveapply
        Map<String, Object> mapA = new HashMap<>();
        mapA.put("orderUserId", orderUserId);
        mapA.put("status", -1);
        mapA.put("orderType", orderType);
        mapA.put("splicingOrderId", spId);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);
        if (nxCommunityOrdersSubEntities.size() > 0) {

            Map<String, Object> mapCard = new HashMap<>();
            mapCard.put("status", -1);
            mapCard.put("userId", orderUserId);
            List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapCard);

            for (NxCustomerUserCardEntity userCardEntity : cardEntities) {
                int hasApply = 0;
                NxCommunityCardEntity cardEntity = nxCommunityCardService.queryObject(userCardEntity.getNxCucaCardId());
                Integer nxCommunityCardId = cardEntity.getNxCommunityCardId();
                for (NxCommunityOrdersSubEntity subEntity : nxCommunityOrdersSubEntities) {
                    Integer nxCosCommunityGoodsId = subEntity.getNxCosCommunityGoodsId();
                    NxCommunityGoodsEntity goodsEntityS = nxCommunityGoodsService.queryObject(nxCosCommunityGoodsId);
                    if (goodsEntityS.getNxCgCardId() != null) {
                        if (goodsEntityS.getNxCgCardId().equals(nxCommunityCardId)) {
                            hasApply = 1;
                        }
                    }
                }
                if (hasApply == 0) {
                    nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
                }
            }

        } else {
            Map<String, Object> mapCard = new HashMap<>();
            mapCard.put("status", -1);
            mapCard.put("userId", orderUserId);
            List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapCard);
            for (NxCustomerUserCardEntity userCardEntity : cardEntities) {
                nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
            }
        }

        Map<String, Object> mapCard = new HashMap<>();
        mapCard.put("status", -1);
        mapCard.put("userId", orderUserId);
        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapCard);

        Map<String, Object> mapData = new HashMap<>();
        mapData.put("arr", nxCommunityOrdersSubEntities);
        mapData.put("cardList", cardEntities);


        return R.ok().put("data", mapData);


    }




    @RequestMapping(value = "/userCheckAdsenseQuantity", method = RequestMethod.POST)
    @ResponseBody
    public R userCheckAdsenseQuantity(Integer goodsId, Integer orderUserId) {
        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);


        boolean canSave = true;
        Map<String, Object> mapQ = new HashMap<>();
        mapQ.put("goodsId", goodsId);
        mapQ.put("status", -1);
        mapQ.put("orderUserId", orderUserId);
        System.out.println("chhchchchchchchirooororoorroor");
        int countOrder = nxCommunityOrdersSubService.querySubOrderCount(mapQ);
        if (countOrder > 0) {
            int userOrderQuantity = nxCommunityOrdersSubService.querySubOrderTotalHuaxianQuantity(mapQ);
            BigDecimal orderOrderQuantity = new BigDecimal(userOrderQuantity);
            BigDecimal restQuantity = new BigDecimal(goodsEntity.getNxCgAdsenseRestQuantity());
            System.out.println("comapapp==??" + orderOrderQuantity.compareTo(restQuantity));
            if (orderOrderQuantity.compareTo(restQuantity) > -1) {
                canSave = false;
            }

        }
        int nowMinute = getNowMinute();
        int nxCgAdsenseStartTimeZone = Integer.parseInt(goodsEntity.getNxCgAdsenseStartTimeZone());
        int nxCgAdsenseStopTimeZone = Integer.parseInt(goodsEntity.getNxCgAdsenseStopTimeZone());
        System.out.println("wxcoudndorororor" + canSave);
        System.out.println("wxcoudndorororor" + nowMinute + "start" + nxCgAdsenseStartTimeZone + "stop" + nxCgAdsenseStopTimeZone);
        if (!canSave && nowMinute > nxCgAdsenseStartTimeZone && nowMinute < nxCgAdsenseStopTimeZone) {
            return R.error(-1, "剩余份数不足");
        } else {
            return R.ok();

        }
    }

    @ResponseBody
    @RequestMapping(value = "/saveFirstSubOrder", method = RequestMethod.POST)
    public R saveFirstSubOrder(Integer goodsId, Integer orderUserId, Integer deskId, Integer spId, Integer orderType, Integer pindanId, Integer serviceType) {

        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);

        NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
        subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
        System.out.println("gogogoidiididdidid" + goodsEntity.getNxCgCommunityId());
        Integer communityId = goodsEntity.getNxCgCommunityId();
        NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
        subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());

        subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
        subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
        subEntity.setNxCosOrderUserId(orderUserId);
        subEntity.setNxCosDeskId(deskId);
        subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
        subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
        subEntity.setNxCosQuantity("1");
        subEntity.setNxCosServiceType(serviceType);
        subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());

        if (goodsEntity.getNxCgPromotionType() == getNxCommunityGoodsPromotionTypeHuaxian()) {
            subEntity.setNxCosHuaxianPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
            subEntity.setNxCosHuaxianDifferentPrice(goodsEntity.getNxCgGoodsHuaxianPriceDifferent());
            subEntity.setNxCosHuaxianSubtotal(goodsEntity.getNxCgGoodsHuaxianPrice());
        } else {
            subEntity.setNxCosHuaxianDifferentPrice("0");
        }

        System.out.println("pindanndsavekekeekeke" + pindanId);

        subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsPrice());
        subEntity.setNxCosSubtotal(goodsEntity.getNxCgGoodsPrice());

        if (pindanId != -1) {
            if (goodsEntity.getNxCgPromotionType() == getNxCommunityGoodsPromotionTypePindanManjian()) {
                Map<String, Object> map = new HashMap<>();
                map.put("orderId", pindanId);
                System.out.println("pindanndidididididiidiidiaaaa " + map);
                List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
                BigDecimal totalAmount = new BigDecimal(String.valueOf(subEntities.size())).add(new BigDecimal(1));

                if (totalAmount.compareTo(new BigDecimal(goodsEntity.getNxCgPromotionAmount())) > -1) {
                    subEntity.setNxCosPrice(goodsEntity.getNxCgPromotionPrice());
                    subEntity.setNxCosSubtotal(goodsEntity.getNxCgPromotionPrice());

                    for (NxCommunityOrdersSubEntity ordersSubEntity : subEntities) {
                        BigDecimal nxCosQuantity = new BigDecimal(ordersSubEntity.getNxCosQuantity());
                        if (nxCosQuantity.compareTo(new BigDecimal(1)) == 0) {
                            ordersSubEntity.setNxCosPrice(goodsEntity.getNxCgPromotionPrice());
                            ordersSubEntity.setNxCosSubtotal(goodsEntity.getNxCgPromotionPrice());
                        } else {

                            //添加一个 porotionSubOrder
                            NxCommunityOrdersSubEntity youhuiSubOrder = new NxCommunityOrdersSubEntity();
                            try {
                                BeanUtils.copyProperties(youhuiSubOrder, ordersSubEntity);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }

                            youhuiSubOrder.setNxCosQuantity("1");
                            youhuiSubOrder.setNxCommunityOrdersSubId(null);
                            youhuiSubOrder.setNxCosCucId(null);
                            youhuiSubOrder.setNxCosPrice(goodsEntity.getNxCgPromotionPrice());
                            youhuiSubOrder.setNxCosSubtotal(goodsEntity.getNxCgPromotionPrice());
                            youhuiSubOrder.setNxCosServiceDate(formatWhatDay(0));
                            nxCommunityOrdersSubService.save(youhuiSubOrder);

                            //修改subOrder Quanity, subtotal减1
                            BigDecimal restQuantity = nxCosQuantity.subtract(new BigDecimal(1));
                            BigDecimal multiply = restQuantity.multiply(new BigDecimal(goodsEntity.getNxCgGoodsPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                            System.out.println("youhuiuiuiuiuiu==" + multiply);
                            ordersSubEntity.setNxCosQuantity(restQuantity.toString());
                            ordersSubEntity.setNxCosSubtotal(multiply.toString());
                            System.out.println("afabbdbbfbabdfb" + ordersSubEntity.getNxCosQuantity());
                            nxCommunityOrdersSubService.update(ordersSubEntity);
                        }
                    }
                }
            }
        }


        subEntity.setNxCosStatus(-1);
        subEntity.setNxCosType(orderType);
        subEntity.setNxCosSplicingOrdersId(spId);
        subEntity.setNxCosOrdersId(pindanId);
        subEntity.setNxCosServiceDate(formatWhatDay(0));
        nxCommunityOrdersSubService.save(subEntity);

        System.out.println("savememe" + deskId);

        if(deskId != -1 && deskId != 99){
            NxCommunityDeskEntity deskEntity = nxCommunityDeskService.queryObject(deskId);
            deskEntity.setNxCdStatus(1);
            nxCommunityDeskService.update(deskEntity);
        }

        //判断是否是会员卡商品
        if (goodsEntity.getNxCgCardId() != null) {

            Map<String, Object> map = new HashMap<>();
            map.put("cardId", goodsEntity.getNxCgCardId());
            map.put("userId", orderUserId);
//            map.put("status", -1);
            map.put("isSelected", 1);
            map.put("stopTime", formatWhatDay(0));
            System.out.println("checkckckckdkusrcarr-1-1--1-1-1-1-1" + map);
            List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(map);
            if (cardEntities.size() == 0) {
                NxCommunityCardEntity cardEntity = nxCommunityCardService.queryObject(goodsEntity.getNxCgCardId());
                NxCustomerUserCardEntity userCardEntity = new NxCustomerUserCardEntity();
                userCardEntity.setNxCucaStatus(-1);
                userCardEntity.setNxCucaCustomerUserId(orderUserId);
                userCardEntity.setNxCucaStartDate(formatWhatDay(0));
                userCardEntity.setNxCucaStopDate(formatWhatDay(Integer.valueOf(cardEntity.getNxCcEffectiveDays())));
                userCardEntity.setNxCucaCardId(cardEntity.getNxCommunityCardId());
                userCardEntity.setNxCucaCommunityId(cardEntity.getNxCcCommunityId());
                userCardEntity.setNxCucaIsSelected(1);
                userCardEntity.setNxCucaType(orderType);
                userCardEntity.setNxCucaComSplicingOrderId(spId);
                userCardEntity.setNxCucaComOrderId(pindanId);
                nxCustomerUserCardService.save(userCardEntity);
            }
        }

        return R.ok().put("data", subEntity);

    }


//    @ResponseBody
//    @RequestMapping(value = "/deskSaveFirstSubOrder", method = RequestMethod.POST)
//    public R deskSaveFirstSubOrder(Integer goodsId, Integer deskId,  Integer spId, Integer orderType, Integer pindanId, Integer serviceType) {
//
//        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
//
//        NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
//        subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
//        System.out.println("gogogoidiididdidid" + goodsEntity.getNxCgCommunityId());
//        Integer communityId = goodsEntity.getNxCgCommunityId();
//        NxECommerceCommunityEntity eCommerceCommunityEntity =  nxECommerceCommunityService.queryByCommunityId(communityId);
//        subEntity.setNxCosCommerceId(eCommerceCommunityEntity.getNxEccEId());
//
//        subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
//        subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
//        subEntity.setNxCosOrderUserId(-1);
//        subEntity.setNxCosDeskId(deskId);
//        subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
//        subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
//        subEntity.setNxCosQuantity("1");
//        subEntity.setNxCosServiceType(serviceType);
//        subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
//
//        if (goodsEntity.getNxCgPromotionType() == getNxCommunityGoodsPromotionTypeHuaxian()) {
//            subEntity.setNxCosHuaxianPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
//            subEntity.setNxCosHuaxianDifferentPrice(goodsEntity.getNxCgGoodsHuaxianPriceDifferent());
//            subEntity.setNxCosHuaxianSubtotal(goodsEntity.getNxCgGoodsHuaxianPrice());
//        } else {
//            subEntity.setNxCosHuaxianDifferentPrice("0");
//        }
//
//        System.out.println("pindanndsavekekeekeke" + pindanId);
//
//        subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsPrice());
//        subEntity.setNxCosSubtotal(goodsEntity.getNxCgGoodsPrice());
//        subEntity.setNxCosStatus(-1);
//        subEntity.setNxCosType(orderType);
//        subEntity.setNxCosSplicingOrdersId(spId);
//        subEntity.setNxCosOrdersId(pindanId);
//        nxCommunityOrdersSubService.save(subEntity);
//
//
//        NxCommunityDeskEntity deskEntity = nxCommunityDeskService.queryObject(deskId);
//        deskEntity.setNxCdStatus(1);
//        nxCommunityDeskService.update(deskEntity);
//
//        return R.ok().put("data", subEntity);
//
//    }

    @ResponseBody
    @RequestMapping("/deleteSubOrders")
    public R deleteSubOrders(@RequestBody Integer[] nxOrdersSubIds) {
        Integer userId = 0;
        for (Integer i : nxOrdersSubIds) {
            NxCommunityOrdersSubEntity subEntity = nxCommunityOrdersSubService.queryObject(i);
            userId = subEntity.getNxCosOrderUserId();
            if (subEntity.getNxCosCucId() != null) {
                NxCustomerUserCouponEntity customerUserCouponEntity = nxCustomerUserCouponService.equalObject(subEntity.getNxCosCucId());
                customerUserCouponEntity.setNxCucSubOrderId(null);
                customerUserCouponEntity.setNxCucStatus(0);
                nxCustomerUserCouponService.update(customerUserCouponEntity);
            }
        }
        nxCommunityOrdersSubService.deleteBatch(nxOrdersSubIds);


        Map<String, Object> mapC = new HashMap<>();
        mapC.put("status", -1);
        mapC.put("type", 0);
        mapC.put("userId", userId);
        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);
        if (cardEntities.size() > 0) {
            for (NxCustomerUserCardEntity userCardEntity : cardEntities) {
                nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
            }
        }
        return R.ok();
    }


    @ResponseBody
    @RequestMapping("/deleteSubOrdersPindan")
    public R deleteSubOrdersPindan(@RequestBody Integer[] nxOrdersSubIds) {
        Integer userId = 0;
        for (Integer i : nxOrdersSubIds) {
            NxCommunityOrdersSubEntity subEntity = nxCommunityOrdersSubService.queryObject(i);
            userId = subEntity.getNxCosOrderUserId();
            if (subEntity.getNxCosCucId() != null) {
                NxCustomerUserCouponEntity customerUserCouponEntity = nxCustomerUserCouponService.equalObject(subEntity.getNxCosCucId());
                customerUserCouponEntity.setNxCucSubOrderId(null);
                customerUserCouponEntity.setNxCucStatus(0);
                nxCustomerUserCouponService.update(customerUserCouponEntity);
            }
        }
        nxCommunityOrdersSubService.deleteBatch(nxOrdersSubIds);


        Map<String, Object> mapC = new HashMap<>();
        mapC.put("status", -1);
        mapC.put("type", 1);
        mapC.put("userId", userId);

        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);
        if (cardEntities.size() > 0) {
            for (NxCustomerUserCardEntity userCardEntity : cardEntities) {
                nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
            }
        }
        return R.ok();
    }

    @ResponseBody
    @RequestMapping("/saveSubOrderCoupon")
    public R saveSubOrderCoupon(@RequestBody NxCommunityOrdersSubEntity nxOrdersSub) {

        nxOrdersSub.setNxCosServiceDate(formatWhatDay(0));
        nxCommunityOrdersSubService.save(nxOrdersSub);
        Integer nxCosCucId = nxOrdersSub.getNxCosCucId();
        NxCustomerUserCouponEntity userCouponEntity = nxCustomerUserCouponService.equalObject(nxCosCucId);
        userCouponEntity.setNxCucStatus(1);
        userCouponEntity.setNxCucSubOrderId(nxOrdersSub.getNxCommunityOrdersSubId());
        nxCustomerUserCouponService.update(userCouponEntity);

        return R.ok();
    }


}
