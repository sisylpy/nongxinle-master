package com.nongxinle.controller;

/**
 * @author lpy
 * @date 10-12 11:38
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.NxJrdhTypeUtils.*;


@RestController
@RequestMapping("api/nxjrdhuser")
public class NxJrdhUserController {
    @Autowired
    private NxJrdhUserService nxJrdhUserService;
    @Autowired
    private NxDistributerPurchaseBatchService nxDisPurchaseBatchService;

    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private NxJrdhSupplierService nxJrdhSupplierService;
    @Autowired
    private GbDistributerService gbDistributerService;
    @Autowired
    GbDistributerPurchaseBatchService gbDisPurchaseBatchService;
    @Autowired
    NxDistributerGbDistributerService nxDisGbDisService;
    @Autowired
    GbDepartmentUserService gbDepartmentUserService;

    @Autowired
    NxDistributerPurchaseGoodsService nxDistributerPurchaseGoodsService;



    @RequestMapping(value = "/sellerUpdateSupplier", method = RequestMethod.POST)
    @ResponseBody
    public R sellerUpdateSupplier(Integer supplierId, Integer sellerId) {
        NxJrdhSupplierEntity supplierEntity = nxJrdhSupplierService.queryObject(supplierId);
        supplierEntity.setNxJrdhsUserId(sellerId);
        System.out.println("updaupsososoekrkek");
        nxJrdhSupplierService.update(supplierEntity);
        return R.ok();
    }

    @RequestMapping(value = "/deleteJrdhUser/{id}")
    @ResponseBody
    public R deleteJrdhUser(@PathVariable Integer id) {

        Map<String, Object> map = new HashMap<>();
        map.put("buyerId", id);
        map.put("status", 3);
        System.out.println("mmmmmm" + map);
        int i = nxDisPurchaseBatchService.queryDisPurchaseBatchCount(map);
        if (i > 0) {
            return R.error(-1, "有未完成订货");
        } else {
            nxJrdhUserService.delete(id);
            return R.ok();
        }

    }

    @RequestMapping(value = "/getJrdhUser/{userId}")
    @ResponseBody
    public R getJrdhUser(@PathVariable Integer userId) {
        NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(userId);

        return R.ok().put("data", nxJrdhUserEntity);
    }

    @RequestMapping(value = "/updateJrdhUserDeviceId", method = RequestMethod.POST)
    @ResponseBody
    public R updateJrdhUserDeviceId(@RequestBody NxJrdhUserEntity userEntity) {
        nxJrdhUserService.update(userEntity);
        return R.ok();
    }


    @RequestMapping(value = "/updateJrdhUser", method = RequestMethod.POST)
    @ResponseBody
    public R updateJrdhUser(String userName, Integer userId) {
        NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(userId);
        nxJrdhUserEntity.setNxJrdhWxNickName(userName);
        nxJrdhUserService.update(nxJrdhUserEntity);

        List<NxJrdhSupplierEntity> supplierEntities = nxJrdhSupplierService.querySupplierByUserId(userId);
        if (supplierEntities.size() > 0) {
            for (NxJrdhSupplierEntity supplierEntity : supplierEntities) {
                supplierEntity.setNxJrdhsSupplierName(userName);
                nxJrdhSupplierService.update(supplierEntity);
            }
        }

        NxJrdhUserEntity nxJrdhUserEntity1 = nxJrdhUserService.queryObject(nxJrdhUserEntity.getNxJrdhUserId());
        return R.ok().put("data", nxJrdhUserEntity1);
    }


    @RequestMapping(value = "/updateJrdhUserWithFile", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R updateJrdhUserWithFile(@RequestParam("file") MultipartFile file,
                                    @RequestParam("userName") String userName,
                                    @RequestParam("userId") Integer userId,
                                    HttpSession session) {
        //1,上传图片
        String newUploadName = "uploadImage";
        String realPath = UploadFile.upload(session, newUploadName, file);

        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + filename;
        NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(userId);
        nxJrdhUserEntity.setNxJrdhWxNickName(userName);
        nxJrdhUserEntity.setNxJrdhWxAvartraUrl(filePath);
        nxJrdhUserEntity.setNxJrdhUrlChange(1);
        nxJrdhUserService.update(nxJrdhUserEntity);

        List<NxJrdhSupplierEntity> supplierEntities = nxJrdhSupplierService.querySupplierByUserId(userId);
        if (supplierEntities.size() > 0) {
            for (NxJrdhSupplierEntity supplierEntity : supplierEntities) {
                supplierEntity.setNxJrdhsSupplierName(userName);
                nxJrdhSupplierService.update(supplierEntity);
            }
        }
        return R.ok();

    }


    @RequestMapping(value = "/jrdhPurchaserUserLogin", method = RequestMethod.POST)
    @ResponseBody
    public R jrdhPurchaserUserLogin(String code, Integer gbDisId, Integer nxDisId, Integer commId) {

        System.out.println("jrdhPurchaserUserLoginjrdhPurchaserUserLoginjrdhPurchaserUserLogin");
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String maimaiAppID = myAPPIDConfig.getJinriDinghuoAppId();
        String maimaiScreat = myAPPIDConfig.getJinriDinghuoScreat();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + maimaiAppID + "&secret=" +
                maimaiScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        String str = WeChatUtil.httpRequest(url, "GET", null);
        JSONObject jsonObject = JSONObject.parseObject(str);
        String openId = jsonObject.get("openid").toString();
        if (openId != null) {
            System.out.println("opeieieiei" + openId);
            Map<String, Object> map = new HashMap<>();
            map.put("openId", openId);
            map.put("gbDisId", gbDisId);
            map.put("nxDisId", nxDisId);
            map.put("commId", commId);
//            map.put("admin", 1);
            System.out.println("newlogogoogogogogogo" + map);
            NxJrdhUserEntity nxBuyUserEntity1 = nxJrdhUserService.queryJrdhUserById(map);
            if (nxBuyUserEntity1 != null) {
                NxDistributerEntity distributerEntity = nxDistributerService.queryObject(nxDisId);
                Map<String, Object> mapRe = new HashMap<>();
                mapRe.put("userInfo",nxBuyUserEntity1 );
                mapRe.put("nxDisInfo", distributerEntity);
                return R.ok().put("data", mapRe);
            } else {
                return R.error(-1, "请进行注册");
            }

        } else {
            return R.error(-1, "请进行注册");
        }
    }


    @RequestMapping(value = "/jrdhUserLogin", method = RequestMethod.POST)
    @ResponseBody
    public R jrdhUserLogin(String code, Integer disId) {

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String maimaiAppID = myAPPIDConfig.getJinriDinghuoAppId();
        String maimaiScreat = myAPPIDConfig.getJinriDinghuoScreat();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + maimaiAppID + "&secret=" +
                maimaiScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        String str = WeChatUtil.httpRequest(url, "GET", null);
        JSONObject jsonObject = JSONObject.parseObject(str);
        String openId = jsonObject.get("openid").toString();
        if (openId != null) {
            NxJrdhUserEntity nxBuyUserEntity1 = nxJrdhUserService.queryWhichUserByOpenId(openId);
            System.out.println("jeihuoufdoufodasufdosau");
            System.out.println(nxBuyUserEntity1);
            if (nxBuyUserEntity1 != null) {
                System.out.println("ussuus" + nxBuyUserEntity1);

                return R.ok().put("data", nxBuyUserEntity1);
            } else {
                return R.error(-1, "请进行注册");
            }

        } else {
            return R.error(-1, "请进行注册");
        }
    }


    @RequestMapping(value = "/indexJrdhUserLogin/{code}")
    @ResponseBody
    public R indexJrdhUserLogin(@PathVariable String code) {

        System.out.println("urllrlrlcode" + code);

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String maimaiAppID = myAPPIDConfig.getJinriDinghuoAppId();
        String maimaiScreat = myAPPIDConfig.getJinriDinghuoScreat();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + maimaiAppID + "&secret=" +
                maimaiScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        System.out.println("urllrlrl" + url);

        String str = WeChatUtil.httpRequest(url, "GET", null);
        JSONObject jsonObject = JSONObject.parseObject(str);
        String openId = jsonObject.get("openid").toString();

        Map<String, Object> dataMap = new HashMap<>();

        List<Map<String, Object>> nxBuyerData = new ArrayList<>();
        List<Map<String, Object>> gbBuyerData = new ArrayList<>();
        List<NxJrdhSupplierEntity> mySupplierEntities = new ArrayList<>();
//        NxJrdhUserEntity nxJrdhUserEntity1 = nxJrdhUserService.queryWhichUserByOpenId(openId);
        Map<String, Object> map = new HashMap<>();
        map.put("openId", openId);
        List<NxJrdhUserEntity> nxJrdhUserEntities = nxJrdhUserService.queryJrdhUserByParams(map);
        if (nxJrdhUserEntities.size() > 0) {
            Map<String, Object> mapU = new HashMap<>();
            mapU.put("openId", openId);
            mapU.put("admin", getNxJrdhUserAdminNxPurchaser()); //nxDistributer 采购员
            List<NxJrdhUserEntity> nxDisBuyerEntityList = nxJrdhUserService.queryJrdhUserByParams(mapU);
            if (nxDisBuyerEntityList.size() > 0) {
                for (NxJrdhUserEntity userEntity : nxDisBuyerEntityList) {
                    if (userEntity.getNxJrdhNxDistributerId() != -1) {
                        Map<String, Object> oneDisBuyer = new HashMap<>();
                        oneDisBuyer.put("nxBuyerInfo", userEntity);
                        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(userEntity.getNxJrdhNxDistributerId());
                        oneDisBuyer.put("nxDisInfo", nxDistributerEntity);
                        Map<String, Object> mapS = new HashMap<>();
                        mapS.put("nxDisId", userEntity.getNxJrdhNxDistributerId());
                        System.out.println("mySuplller" + mapS);
                        List<NxJrdhSupplierEntity> supplierEntities = nxJrdhSupplierService.queryJrdhSupplerByParams(mapS);
                        oneDisBuyer.put("nxSupplierArr", supplierEntities);
                        nxBuyerData.add(oneDisBuyer);
                    }

                }
                dataMap.put("nx", nxBuyerData);
                dataMap.put("supplierUserList", new ArrayList<>());

//                mapU.put("admin", getNxJrdhUserAdminGbPurchaser()); //gbDistributer 采购员
//                List<NxJrdhUserEntity> gbDisBuyerEntityList = nxJrdhUserService.queryJrdhUserByParams(mapU);
//                if (gbDisBuyerEntityList.size() > 0) {
//                    for (NxJrdhUserEntity userEntity : gbDisBuyerEntityList) {
//                        if (userEntity.getNxJrdhGbDistributerId() != -1) {
//                            Map<String, Object> oneGbDisBuyer = new HashMap<>();
//                            oneGbDisBuyer.put("gbBuyerInfo", userEntity);
//                            GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(userEntity.getNxJrdhGbDistributerId());
//                            oneGbDisBuyer.put("gbDisInfo", gbDistributerEntity);
//                            Map<String, Object> mapS = new HashMap<>();
//                            mapS.put("gbDisId", userEntity.getNxJrdhGbDistributerId());
////                            mapS.put("userId", -1);
//                            System.out.println("mapssss" + mapS);
//                            List<NxJrdhSupplierEntity> supplierEntities = nxJrdhSupplierService.queryJrdhSupplerByParams(mapS);
//                            oneGbDisBuyer.put("gbSupplierArr", supplierEntities);
//                            gbBuyerData.add(oneGbDisBuyer);
//                        }
//                    }
//                }
//
//                dataMap.put("gb", gbBuyerData);
            }

            mapU.put("admin", getNxJrdhUserAdminNxSeller());
            System.out.println("dwhwhhhwhwhhhwhw" + mapU);
            NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryJrdhUserById(mapU);
            if (nxJrdhUserEntity != null) {
                Map<String, Object> mapS = new HashMap<>();
                mapS.put("userId", nxJrdhUserEntity.getNxJrdhUserId());
                mySupplierEntities = nxJrdhSupplierService.queryJrdhSupplerByParams(mapS);
                dataMap.put("supplierUserList", mySupplierEntities);
                dataMap.put("sellerUserInfo", nxJrdhUserEntity);
            }

            return R.ok().put("data", dataMap);
        } else {
            return R.error(-1, "注册失败");
        }

    }



    @RequestMapping(value = "/nxSellerUserLogin")
    @ResponseBody
    public R nxSellerUserLogin(String code, Integer supplierId, Integer buyUserId) {

        System.out.println("urllrlrlcodenxSeelerUserLogin" + code);

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String maimaiAppID = myAPPIDConfig.getJinriDinghuoAppId();
        String maimaiScreat = myAPPIDConfig.getJinriDinghuoScreat();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + maimaiAppID + "&secret=" +
                maimaiScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        String str = WeChatUtil.httpRequest(url, "GET", null);
        JSONObject jsonObject = JSONObject.parseObject(str);
        String openId = jsonObject.get("openid").toString();

        Map<String, Object> dataMap = new HashMap<>();

        if (openId != null) {
            NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryWhichUserByOpenId(openId);
            if(nxJrdhUserEntity != null){
                Map<String, Object> map = new HashMap<>();
                map.put("userId", nxJrdhUserEntity.getNxJrdhUserId());
                map.put("supplierId", supplierId);
                System.out.println("newiseelerloginfg" + map);
                List<NxJrdhSupplierEntity> nxJrdhSupplierEntities = nxJrdhSupplierService.queryJrdhSupplerByParams(map);
                if(nxJrdhSupplierEntities.size() == 0){
                    NxJrdhSupplierEntity nxJrdhSupplierEntity = nxJrdhSupplierService.queryObject(supplierId);
                    nxJrdhSupplierEntity.setNxJrdhsUserId(nxJrdhUserEntity.getNxJrdhUserId());
                    nxJrdhSupplierEntity.setNxJrdhsNxJrdhBuyUserId(buyUserId);
                    nxJrdhSupplierService.update(nxJrdhSupplierEntity);
                }
                dataMap.put("userInfo", nxJrdhUserEntity);
                return R.ok().put("data", dataMap);
            }else {
                return R.error(-1, "注册失败");
            }

//            dataMap.put("arr",supplierEntities);

        } else {
            return R.error(-1, "注册失败");
        }

    }


    @RequestMapping(value = "/indexJrdhUserLoginJj/{code}")
    @ResponseBody
    public R indexJrdhUserLoginJj(@PathVariable String code) {

        System.out.println("urllrlrlcode" + code);

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String maimaiAppID = myAPPIDConfig.getTexiansongCaigouAppId();
        String maimaiScreat = myAPPIDConfig.getTexiansongCaigouScreat();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + maimaiAppID + "&secret=" +
                maimaiScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        String str = WeChatUtil.httpRequest(url, "GET", null);
        JSONObject jsonObject = JSONObject.parseObject(str);
        String openId = jsonObject.get("openid").toString();

        Map<String, Object> dataMap = new HashMap<>();

        if (openId != null) {
            NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryWhichUserByOpenId(openId);
            if(nxJrdhUserEntity != null){
                Map<String, Object> map = new HashMap<>();
                map.put("userId", nxJrdhUserEntity.getNxJrdhUserId());
                System.out.println("whhwehepururr" + map);
                List<NxJrdhSupplierEntity> supplierEntities = nxJrdhSupplierService.queryJrdhSupplerWithDisByUserId(map);
                dataMap.put("userInfo", nxJrdhUserEntity);
                dataMap.put("arr", supplierEntities);
                return R.ok().put("data", dataMap);
            }else{
                return R.error(-1, "注册失败");
            }
           
        } else {
            return R.error(-1, "注册失败");
        }

    }




    @RequestMapping(value = "/whichJJCGUserLoginDis/{code}")
    @ResponseBody
    public R whichJJCGUserLoginDis(@PathVariable String code) {
        System.out.println("coododoe" + code);
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String maimaiAppID = myAPPIDConfig.getTexiansongCaigouAppId();
        String maimaiScreat = myAPPIDConfig.getTexiansongCaigouScreat();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + maimaiAppID + "&secret=" +
                maimaiScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        String str = WeChatUtil.httpRequest(url, "GET", null);
        JSONObject jsonObject = JSONObject.parseObject(str);
        String openId = jsonObject.get("openid").toString();
        System.out.println("whichJJCGUserLoginDis");
        if (openId != null) {
            NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryWhichUserByOpenId(openId);
            if(nxJrdhUserEntity != null){
                return R.ok().put("data", "nx");
            }else{
                GbDepartmentUserEntity gbDepartmentUserEntity = gbDepartmentUserService.queryDepUserByOpenId(openId);
                if(gbDepartmentUserEntity != null){
                    return R.ok().put("data", "gb");
                }
            }
            return R.error(-1, "登录失败");
        } else {
            return R.error(-1, "登录失败");
        }
    }


    @RequestMapping(value = "/whichJrdhUserLoginDis", method = RequestMethod.POST)
    @ResponseBody
    public R whichJrdhUserLoginDis(String code, Integer nxDisId, Integer batchId) {
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String maimaiAppID = myAPPIDConfig.getJinriDinghuoAppId();
        String maimaiScreat = myAPPIDConfig.getJinriDinghuoScreat();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + maimaiAppID + "&secret=" +
                maimaiScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        String str = WeChatUtil.httpRequest(url, "GET", null);
        JSONObject jsonObject = JSONObject.parseObject(str);
        String openId = jsonObject.get("openid").toString();
        if (openId != null) {
            NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(nxDisId);
            Map<String, Object> map = new HashMap<>();
            map.put("disInfo", nxDistributerEntity);

            //首先判断是不是dis 用户
            Map<String, Object> mapB = new HashMap<>();
            mapB.put("batchId", batchId);
            System.out.println("mapbBbBBBBwww" + mapB);
            NxDistributerPurchaseBatchEntity batchEntity = nxDisPurchaseBatchService.queryBatchItemByParams(mapB);
            if(batchEntity == null){
                map.put("billCancle", true);
            }else{
                map.put("billCancle", false);
            }

            Map<String, Object> mapUser = new HashMap<>();
            mapUser.put("openId", openId);
            mapUser.put("nxDisId", nxDisId);
            mapUser.put("admin", 1);
            NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryJrdhUserById(mapUser);
            if (nxJrdhUserEntity  != null) {
                map.put("jrdhUserInfo", nxJrdhUserEntity);
                map.put("buyUser", true);
                map.put("supplierInfo", null);
                System.out.println("woshiscuao=ogugdmmdmamfamdsf" + map);
                return R.ok().put("data", map);
            }
               else {
                    Map<String, Object> mapUserSell = new HashMap<>();
                    mapUserSell.put("openId", openId);
                    mapUserSell.put("admin", 3);
                    System.out.println("selelemapap" + mapUserSell);
                    NxJrdhUserEntity jrdhUserEntitySell = nxJrdhUserService.queryJrdhUserById(mapUserSell);
                    if (jrdhUserEntitySell != null) {
                        Map<String, Object> mapS = new HashMap<>();
                        mapS.put("nxDisId", nxDisId);
                        mapS.put("userId", jrdhUserEntitySell.getNxJrdhUserId());
                        System.out.println("mapssssaaa" + mapS);
                        NxJrdhSupplierEntity nxJrdhSupplierEntity = nxJrdhSupplierService.querySellUserSupplier(mapS);
                        if (nxJrdhSupplierEntity == null) {
                            NxDistributerPurchaseBatchEntity nxPurchaseBatchEntity = nxDisPurchaseBatchService.queryObject(batchId);
                            NxJrdhSupplierEntity supplierEntity = saveJrdhSupplerDis(jrdhUserEntitySell, nxDisId, nxPurchaseBatchEntity.getNxDpbBuyUserId());
                            System.out.println("xingonghusososos" + supplierEntity.getNxJrdhsSupplierName());

                            map.put("supplierInfo", supplierEntity);
                        } else {
                            map.put("supplierInfo", nxJrdhSupplierEntity);
                        }
                        map.put("buyUser", false);
                        map.put("jrdhUserInfo", jrdhUserEntitySell);
                        System.out.println("reeurururururr" + map);
                        map.put("code", 0);
                        return R.ok().put("data", map);
                    }else{
                        return R.error(-1, "登录失败");
                    }
                }
            }

         else {
            return R.error(-1, "登录失败");
        }
    }


    @RequestMapping(value = "/whichJrdhUserLoginGb", method = RequestMethod.POST)
    @ResponseBody
    public R whichJrdhUserLoginGb(String code, Integer gbDisId, Integer batchId,
                                  Integer gbDepId, Integer purUserId) {
        System.out.println("whiciciiicic");
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String maimaiAppID = myAPPIDConfig.getJinriDinghuoAppId();
        String maimaiScreat = myAPPIDConfig.getJinriDinghuoScreat();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + maimaiAppID + "&secret=" +
                maimaiScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        String str = WeChatUtil.httpRequest(url, "GET", null);
        JSONObject jsonObject = JSONObject.parseObject(str);
        String openId = jsonObject.get("openid").toString();
        if (openId != null) {
            System.out.println("openeieieieiieie===aaaaaa" + openId);
            GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(gbDisId);
            Map<String, Object> map = new HashMap<>();
            map.put("disInfo", gbDistributerEntity);

            //首先判断是不是dis 用户
            Map<String, Object> mapB = new HashMap<>();
            mapB.put("batchId", batchId);
            mapB.put("buyUserOpenId", openId);
            GbDistributerPurchaseBatchEntity batchEntity = gbDisPurchaseBatchService.queryBatchItemByParams(mapB);

            Map<String, Object> mapUser = new HashMap<>();
            mapUser.put("openId", openId);
            if (batchEntity != null) {
                mapUser.put("admin", 2);
                mapUser.put("gbDisId", gbDisId);
                System.out.println("mapuusueur" + mapUser);
                NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryJrdhUserById(mapUser);
                if (nxJrdhUserEntity != null) {
                    map.put("userInfo", nxJrdhUserEntity);
                    map.put("buyUser", true);
                    map.put("supplierInfo", null);
                    map.put("code", 1);
                }
            } else {
                Map<String, Object> mapUserSell = new HashMap<>();
                mapUserSell.put("openId", openId);
                mapUserSell.put("admin", 3);
                System.out.println("mapuusuer33333" + mapUser);
                NxJrdhUserEntity jrdhUserEntitySell = nxJrdhUserService.queryJrdhUserById(mapUserSell);
                if (jrdhUserEntitySell != null) {
                    Map<String, Object> mapS = new HashMap<>();
                    mapS.put("gbDisId", gbDisId);
                    mapS.put("userId", jrdhUserEntitySell.getNxJrdhUserId());
                    System.out.println("gbsuppsosoeri" + mapS);
                    NxJrdhSupplierEntity nxJrdhSupplierEntity = nxJrdhSupplierService.querySellUserSupplier(mapS);
                    if (nxJrdhSupplierEntity == null) {
                        GbDistributerPurchaseBatchEntity gbPurchaseBatchEntity = gbDisPurchaseBatchService.queryObject(batchId);
                        NxJrdhSupplierEntity supplierEntity = saveJrdhSupplerGb(jrdhUserEntitySell, gbDisId, gbPurchaseBatchEntity.getGbDpbBuyUserId(), gbDepId);
                        map.put("supplierInfo", supplierEntity);
                    } else {
                        map.put("supplierInfo", nxJrdhSupplierEntity);
                    }
                    map.put("buyUser", false);
                    map.put("userInfo", jrdhUserEntitySell);
                    map.put("code", 1);
                } else {
                    map.put("code", -1);
                    return R.ok().put("data", map);
                }
            }
            System.out.println("shosusososoosos" + map);
            return R.ok().put("data", map);

        } else {
            return R.error(-1, "登录失败");
        }
    }

    @RequestMapping(value = "/jrdhAppointSellerRegisterWithFile", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R jrdhAppointSellerRegisterWithFile(@RequestParam("file") MultipartFile file,
                                               @RequestParam("userName") String userName,
                                               @RequestParam("code") String code,
                                               @RequestParam("admin") Integer admin,
                                               @RequestParam("gbDisId") Integer gbDisId,
                                               @RequestParam("nxDisId") Integer nxDisId,
                                               @RequestParam("supplierId") Integer supplierId,
                                               @RequestParam("depId") Integer depId,
                                               @RequestParam("purUserId") Integer purUserId,
                                               @RequestParam("buyUserId") Integer buyUserId,
                                               HttpSession session) {
        System.out.println("aavvvvv" + admin);
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getJinriDinghuoAppId() +
                "&secret=" + myAPPIDConfig.getJinriDinghuoScreat() + "&js_code=" + code + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);

        // 转成Json对象 获取openidjrdhUserRegister
        JSONObject jsonObject = JSONObject.parseObject(str);
        System.out.println(jsonObject);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        //判断是否suppler
        Map<String, Object> map = new HashMap<>();
        map.put("openId", openid);
        map.put("nxDisId", nxDisId);
        map.put("gbDisId", gbDisId);
        System.out.println("chchchchcchchhc" + map);
        boolean yizhuce;
        List<NxJrdhUserEntity> userEntityList = nxJrdhUserService.queryJrdhUserByParams(map);
        if (userEntityList.size() > 0) {
            yizhuce = true;
        } else {

            //添加新用户
            NxJrdhUserEntity jrdhUserEntity = new NxJrdhUserEntity();
            jrdhUserEntity.setNxJrdhWxOpenId(openid);
            jrdhUserEntity.setNxJrdhJoinDate(formatWhatDay(0));
            //1,上传图片
            String newUploadName = "uploadImage";
            String realPath = UploadFile.upload(session, newUploadName, file);

            String filename = file.getOriginalFilename();
            String filePath = newUploadName + "/" + filename;
            jrdhUserEntity.setNxJrdhWxNickName(userName);
            jrdhUserEntity.setNxJrdhWxAvartraUrl(filePath);
            jrdhUserEntity.setNxJrdhUrlChange(1);
            jrdhUserEntity.setNxJrdhNxDistributerId(nxDisId);
            jrdhUserEntity.setNxJrdhNxPurchaserUserId(-1);
            jrdhUserEntity.setNxJrdhNxCommunityId(-1);
            jrdhUserEntity.setNxJrdhCommPurchaserUserId(purUserId);
            jrdhUserEntity.setNxJrdhGbDistributerId(gbDisId);
            jrdhUserEntity.setNxJrdhGbDepartmentId(depId);
            jrdhUserEntity.setNxJrdhGbDepartmentUserId(-1);
            jrdhUserEntity.setNxJrdhAdmin(admin);
            jrdhUserEntity.setNxJrdhDeviceId("-1");
            jrdhUserEntity.setNxJrdhDevicePrintId("-1");
            nxJrdhUserService.save(jrdhUserEntity);
            yizhuce = false;

            NxJrdhSupplierEntity nxJrdhSupplierEntity = nxJrdhSupplierService.queryObject(supplierId);
            nxJrdhSupplierEntity.setNxJrdhsUserId(jrdhUserEntity.getNxJrdhUserId());
            nxJrdhSupplierEntity.setNxJrdhsNxJrdhBuyUserId(buyUserId);
            nxJrdhSupplierService.update(nxJrdhSupplierEntity);
        }
        if (yizhuce) {
            return R.error("-1");
        } else {
            return R.ok();
        }

    }


    @RequestMapping(value = "/jrdhSellerRegisterWithFileGb", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R jrdhSellerRegisterWithFileGb(@RequestParam("file") MultipartFile file,
                                          @RequestParam("userName") String userName,
                                          @RequestParam("code") String code,
                                          @RequestParam("admin") Integer admin,
                                          @RequestParam("gbDisId") Integer gbDisId,
                                          @RequestParam("buyUserId") Integer buyUserId,
                                          @RequestParam("gbDepId") Integer gbDepId,
                                          @RequestParam("purUserId") Integer purUserId,
                                          HttpSession session) {
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getJinriDinghuoAppId() +
                "&secret=" + myAPPIDConfig.getJinriDinghuoScreat() + "&js_code=" + code + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);

        // 转成Json对象 获取openidjrdhUserRegister
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();

        boolean yizhuce;

        Map<String, Object> map = new HashMap<>();
        map.put("gbDisId", gbDisId);
        map.put("admin", 3);
        map.put("openId", openid);
        NxJrdhUserEntity jrdhUserEntity1 = nxJrdhUserService.queryJrdhUserByAdmin(map);
        if (jrdhUserEntity1 != null) {
            System.out.println("yizhucueueueueueue");
            yizhuce = true;
            NxJrdhSupplierEntity supplierEntity = saveJrdhSupplerGb(jrdhUserEntity1, gbDisId, buyUserId, gbDepId);
        } else {

            //添加新用户
            NxJrdhUserEntity jrdhUserEntity = new NxJrdhUserEntity();
            jrdhUserEntity.setNxJrdhWxOpenId(openid);
            jrdhUserEntity.setNxJrdhJoinDate(formatWhatDay(0));
            //1,上传图片
            String newUploadName = "uploadImage";
            String realPath = UploadFile.upload(session, newUploadName, file);

            String filename = file.getOriginalFilename();
            String filePath = newUploadName + "/" + filename;
            jrdhUserEntity.setNxJrdhWxNickName(userName);
            jrdhUserEntity.setNxJrdhWxAvartraUrl(filePath);
            jrdhUserEntity.setNxJrdhUrlChange(1);
            jrdhUserEntity.setNxJrdhNxDistributerId(-1);
            jrdhUserEntity.setNxJrdhNxPurchaserUserId(-1);
            jrdhUserEntity.setNxJrdhNxCommunityId(-1);
            jrdhUserEntity.setNxJrdhCommPurchaserUserId(-1);
            jrdhUserEntity.setNxJrdhGbDistributerId(-1);
            jrdhUserEntity.setNxJrdhGbDepartmentId(-1);
            jrdhUserEntity.setNxJrdhGbDepartmentUserId(-1);
            jrdhUserEntity.setNxJrdhAdmin(admin);
            jrdhUserEntity.setNxJrdhDeviceId("-1");
            jrdhUserEntity.setNxJrdhDevicePrintId("-1");
            nxJrdhUserService.save(jrdhUserEntity);

            saveJrdhSupplerGb(jrdhUserEntity, gbDisId, buyUserId, gbDepId);

            yizhuce = false;
        }

        if (yizhuce) {
            return R.error("-1");
        } else {
            return R.ok();
        }

    }


    @RequestMapping(value = "/jjshUserRegisterWithFileGbJj", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R jjshUserRegisterWithFileGbJj(@RequestParam("file") MultipartFile file,
                                            @RequestParam("userName") String userName,
                                            @RequestParam("code") String code,
                                          @RequestParam("admin") Integer admin,
                                          @RequestParam("nxDisId") Integer nxDisId,
                                            HttpSession session) {
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getTexiansongCaigouAppId() +
                "&secret=" + myAPPIDConfig.getTexiansongCaigouScreat() + "&js_code=" + code + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);

        // 转成Json对象 获取openidjrdhUserRegister
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();

        boolean yizhuce;
        Map<String, Object> map = new HashMap<>();
        map.put("admin", admin);
        map.put("openId", openid);
        NxJrdhUserEntity jrdhUserEntity1 = nxJrdhUserService.queryJrdhUserByAdmin(map);
        if (jrdhUserEntity1 == null) {
            NxJrdhUserEntity jrdhUserEntity = new NxJrdhUserEntity();
            jrdhUserEntity.setNxJrdhWxOpenId(openid);
            jrdhUserEntity.setNxJrdhJoinDate(formatWhatDay(0));
            //1,上传图片
            String newUploadName = "uploadImage";
            String realPath = UploadFile.upload(session, newUploadName, file);

            String filename = file.getOriginalFilename();
            String filePath = newUploadName + "/" + filename;
            jrdhUserEntity.setNxJrdhWxNickName(userName);
            jrdhUserEntity.setNxJrdhWxAvartraUrl(filePath);
            jrdhUserEntity.setNxJrdhUrlChange(1);
            jrdhUserEntity.setNxJrdhNxDistributerId(nxDisId);
            jrdhUserEntity.setNxJrdhNxPurchaserUserId(-1);
            jrdhUserEntity.setNxJrdhNxCommunityId(-1);
            jrdhUserEntity.setNxJrdhCommPurchaserUserId(-1);
            jrdhUserEntity.setNxJrdhGbDistributerId(-1);
            jrdhUserEntity.setNxJrdhGbDepartmentId(-1);
            jrdhUserEntity.setNxJrdhGbDepartmentUserId(-1);
            jrdhUserEntity.setNxJrdhAdmin(admin);
            jrdhUserEntity.setNxJrdhDeviceId("-1");
            jrdhUserEntity.setNxJrdhDevicePrintId("-1");
            nxJrdhUserService.save(jrdhUserEntity);
        }

       return R.ok();

    }


    @RequestMapping(value = "/jrdhSellerRegisterWithFileGbJj", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R jrdhSellerRegisterWithFileGbJj(@RequestParam("file") MultipartFile file,
                                            @RequestParam("userName") String userName,
                                            @RequestParam("code") String code,
                                            @RequestParam("admin") Integer admin,
                                            @RequestParam("gbDisId") Integer gbDisId,
                                            @RequestParam("buyUserId") Integer buyUserId,
                                            @RequestParam("gbDepId") Integer gbDepId,
                                            HttpSession session) {
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getTexiansongCaigouAppId() +
                "&secret=" + myAPPIDConfig.getTexiansongCaigouScreat() + "&js_code=" + code + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);

        // 转成Json对象 获取openidjrdhUserRegister
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();

        boolean yizhuce;
        Map<String, Object> map = new HashMap<>();
        map.put("gbDisId", gbDisId);
        map.put("admin", admin);
        map.put("openId", openid);
        NxJrdhUserEntity jrdhUserEntity1 = nxJrdhUserService.queryJrdhUserByAdmin(map);
        if (jrdhUserEntity1 != null) {
            System.out.println("yizhucueueueueueue");
            yizhuce = true;
            saveJrdhSupplerGb(jrdhUserEntity1, gbDisId, buyUserId, gbDepId);
        } else {
            //添加新用户
            NxJrdhUserEntity jrdhUserEntity = new NxJrdhUserEntity();
            jrdhUserEntity.setNxJrdhWxOpenId(openid);
            jrdhUserEntity.setNxJrdhJoinDate(formatWhatDay(0));
            //1,上传图片
            String newUploadName = "uploadImage";
            String realPath = UploadFile.upload(session, newUploadName, file);

            String filename = file.getOriginalFilename();
            String filePath = newUploadName + "/" + filename;
            jrdhUserEntity.setNxJrdhWxNickName(userName);
            jrdhUserEntity.setNxJrdhWxAvartraUrl(filePath);
            jrdhUserEntity.setNxJrdhUrlChange(1);
            jrdhUserEntity.setNxJrdhNxDistributerId(-1);
            jrdhUserEntity.setNxJrdhNxPurchaserUserId(-1);
            jrdhUserEntity.setNxJrdhNxCommunityId(-1);
            jrdhUserEntity.setNxJrdhCommPurchaserUserId(-1);
            jrdhUserEntity.setNxJrdhGbDistributerId(-1);
            jrdhUserEntity.setNxJrdhGbDepartmentId(-1);
            jrdhUserEntity.setNxJrdhGbDepartmentUserId(-1);
            jrdhUserEntity.setNxJrdhAdmin(admin);
            jrdhUserEntity.setNxJrdhDeviceId("-1");
            jrdhUserEntity.setNxJrdhDevicePrintId("-1");
            nxJrdhUserService.save(jrdhUserEntity);

            saveJrdhSupplerGb(jrdhUserEntity, gbDisId, buyUserId, gbDepId);

            yizhuce = false;
        }

        if (yizhuce) {
            return R.error("-1");
        } else {
            return R.ok();
        }

    }

    @RequestMapping(value = "/jrdhSellerRegisterWithFileGbJjNx", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R jrdhSellerRegisterWithFileGbJjNx(@RequestParam("file") MultipartFile file,
                                            @RequestParam("userName") String userName,
                                            @RequestParam("code") String code,
                                            @RequestParam("admin") Integer admin,
                                            @RequestParam("gbDisId") Integer gbDisId,
                                            @RequestParam("buyUserId") Integer buyUserId,
                                            @RequestParam("gbDepId") Integer gbDepId,
                                            @RequestParam("purUserId") Integer purUserId,
                                            @RequestParam("nxDisId") Integer nxDisId,
                                            HttpSession session) {
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getTexiansongCaigouAppId() +
                "&secret=" + myAPPIDConfig.getTexiansongCaigouScreat() + "&js_code=" + code + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);

        // 转成Json对象 获取openidjrdhUserRegister
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();

        boolean yizhuce;
        Map<String, Object> map = new HashMap<>();
        map.put("gbDisId", gbDisId);
        map.put("admin", admin);
        map.put("openId", openid);
        NxJrdhUserEntity jrdhUserEntity1 = nxJrdhUserService.queryJrdhUserByAdmin(map);
        if (jrdhUserEntity1 != null) {
            System.out.println("yizhucueueueueueue");
            yizhuce = true;
            saveJrdhSupplerGbWithNxBusiness(jrdhUserEntity1, gbDisId, buyUserId, gbDepId, purUserId, nxDisId);
        } else {
            //添加新用户
            NxJrdhUserEntity jrdhUserEntity = new NxJrdhUserEntity();
            jrdhUserEntity.setNxJrdhWxOpenId(openid);
            jrdhUserEntity.setNxJrdhJoinDate(formatWhatDay(0));
            //1,上传图片
            String newUploadName = "uploadImage";
            String realPath = UploadFile.upload(session, newUploadName, file);

            String filename = file.getOriginalFilename();
            String filePath = newUploadName + "/" + filename;
            jrdhUserEntity.setNxJrdhWxNickName(userName);
            jrdhUserEntity.setNxJrdhWxAvartraUrl(filePath);
            jrdhUserEntity.setNxJrdhUrlChange(1);
            jrdhUserEntity.setNxJrdhNxDistributerId(-1);
            jrdhUserEntity.setNxJrdhNxPurchaserUserId(-1);
            jrdhUserEntity.setNxJrdhNxCommunityId(-1);
            jrdhUserEntity.setNxJrdhCommPurchaserUserId(-1);
            jrdhUserEntity.setNxJrdhGbDistributerId(-1);
            jrdhUserEntity.setNxJrdhGbDepartmentId(-1);
            jrdhUserEntity.setNxJrdhGbDepartmentUserId(-1);
            jrdhUserEntity.setNxJrdhAdmin(admin);
            jrdhUserEntity.setNxJrdhDeviceId("-1");
            jrdhUserEntity.setNxJrdhDevicePrintId("-1");
            nxJrdhUserService.save(jrdhUserEntity);

            saveJrdhSupplerGbWithNxBusiness(jrdhUserEntity, gbDisId, buyUserId, gbDepId, purUserId,nxDisId);

            yizhuce = false;
        }

        if (yizhuce) {
            return R.error("-1");
        } else {
            return R.ok();
        }

    }

    @RequestMapping(value = "/jrdhSellerRegisterWithFile", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R jrdhSellerRegisterWithFile(@RequestParam(value = "file", required = false) MultipartFile file,
                                        @RequestParam(value = "userName", required = false) String userName,
                                        @RequestParam(value = "code", required = false) String code,
                                        @RequestParam(value = "admin", required = false) Integer admin,
                                        @RequestParam(value = "nxDisId", required = false) Integer nxDisId,
                                        @RequestParam(value = "gbDisId", required = false) Integer gbDisId,
                                        @RequestParam(value = "commId", required = false) Integer commId,
                                        @RequestParam(value = "buyUserId", required = false) Integer buyUserId,
                                        @RequestParam(value = "buyerUserId", required = false) Integer buyerUserId,
                                        HttpSession session) {
        try {
            System.out.println("========== jrdhSellerRegisterWithFile 开始 ==========");
            System.out.println("接收到的参数:");
            System.out.println("  - userName: " + userName);
            System.out.println("  - code: " + code);
            System.out.println("  - admin: " + admin);
            System.out.println("  - nxDisId: " + nxDisId);
            System.out.println("  - gbDisId: " + gbDisId);
            System.out.println("  - commId: " + commId);
            System.out.println("  - buyUserId: " + buyUserId);
            System.out.println("  - buyerUserId: " + buyerUserId);
            System.out.println("  - file: " + (file != null ? file.getOriginalFilename() : "null"));
            System.out.println("  - file size: " + (file != null ? file.getSize() : 0) + " bytes");
            
            // 兼容前端传入的 buyerUserId 或 buyUserId
            Integer finalBuyUserId = buyUserId != null ? buyUserId : buyerUserId;
            System.out.println("  - 最终使用的buyUserId: " + finalBuyUserId);
            
            // 参数校验
            if (file == null) {
                System.out.println("错误: 文件不能为空");
                return R.error(-1, "文件不能为空");
            }
            if (userName == null || userName.trim().isEmpty()) {
                System.out.println("错误: 用户名不能为空");
                return R.error(-1, "用户名不能为空");
            }
            if (code == null || code.trim().isEmpty()) {
                System.out.println("错误: code不能为空");
                return R.error(-1, "code不能为空");
            }
            
            MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

            String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getJinriDinghuoAppId() +
                    "&secret=" + myAPPIDConfig.getJinriDinghuoScreat() + "&js_code=" + code + "&grant_type=authorization_code";
            System.out.println("微信登录URL: " + url);
            
            // 发送请求，返回Json字符串
            String str = WeChatUtil.httpRequest(url, "GET", null);
            System.out.println("微信接口返回: " + str);

            // 转成Json对象 获取openidjrdhUserRegister
            JSONObject jsonObject = JSONObject.parseObject(str);
            System.out.println("解析后的JSON: " + jsonObject);

            // 检查是否有错误
            if (jsonObject.containsKey("errcode")) {
                System.out.println("错误: 微信接口返回错误码 - " + jsonObject.get("errcode"));
                return R.error(-1, "微信登录失败: " + jsonObject.get("errmsg"));
            }

            // 我们需要的openid，在一个小程序中，openid是唯一的
            if (!jsonObject.containsKey("openid")) {
                System.out.println("错误: 微信接口未返回openid");
                return R.error(-1, "获取openid失败");
            }
            String openid = jsonObject.get("openid").toString();
            System.out.println("获取到的openid: " + openid);

            boolean yizhuce;

            Map<String, Object> map = new HashMap<>();
            map.put("admin", 3);
            map.put("openId", openid);
            map.put("nxDisId", nxDisId);
            map.put("gbDisId", gbDisId);
            map.put("commId", commId);
            System.out.println("查询用户参数map: " + map);
            
            NxJrdhUserEntity jrdhUserEntity1 = nxJrdhUserService.queryJrdhUserByAdmin(map);
            System.out.println("查询到的用户: " + (jrdhUserEntity1 != null ? jrdhUserEntity1.getNxJrdhUserId() : "null"));
            
            if (jrdhUserEntity1 != null) {
                System.out.println("用户已注册，创建供应商关联");
                yizhuce = true;
                NxJrdhSupplierEntity supplierEntity = saveJrdhSupplerDis(jrdhUserEntity1, nxDisId, finalBuyUserId);
                System.out.println("供应商创建成功: " + (supplierEntity != null ? supplierEntity.getNxJrdhsSupplierName() : "null"));
            } else {
                System.out.println("用户未注册，开始注册新用户");
                
                //添加新用户
                NxJrdhUserEntity jrdhUserEntity = new NxJrdhUserEntity();
                jrdhUserEntity.setNxJrdhWxOpenId(openid);
                jrdhUserEntity.setNxJrdhJoinDate(formatWhatDay(0));
                
                //1,上传图片
                System.out.println("开始上传图片...");
                String newUploadName = "uploadImage";
                String realPath = UploadFile.upload(session, newUploadName, file);
                System.out.println("图片上传完成，路径: " + realPath);

                String filename = file.getOriginalFilename();
                String filePath = newUploadName + "/" + filename;
                System.out.println("文件路径: " + filePath);
                
                jrdhUserEntity.setNxJrdhWxNickName(userName);
                jrdhUserEntity.setNxJrdhWxAvartraUrl(filePath);
                jrdhUserEntity.setNxJrdhUrlChange(1);
                jrdhUserEntity.setNxJrdhNxDistributerId(-1);
                jrdhUserEntity.setNxJrdhNxPurchaserUserId(-1);
                jrdhUserEntity.setNxJrdhNxCommunityId(-1);
                jrdhUserEntity.setNxJrdhCommPurchaserUserId(-1);
                jrdhUserEntity.setNxJrdhGbDistributerId(gbDisId);
                jrdhUserEntity.setNxJrdhGbDepartmentId(-1);
                jrdhUserEntity.setNxJrdhGbDepartmentUserId(-1);
                jrdhUserEntity.setNxJrdhAdmin(admin);
                jrdhUserEntity.setNxJrdhDeviceId("-1");
                jrdhUserEntity.setNxJrdhDevicePrintId("-1");
                
                System.out.println("准备保存用户信息...");
                nxJrdhUserService.save(jrdhUserEntity);
                System.out.println("用户保存成功，用户ID: " + jrdhUserEntity.getNxJrdhUserId());

                System.out.println("创建供应商关联...");
                saveJrdhSupplerDis(jrdhUserEntity, nxDisId, finalBuyUserId);
                System.out.println("供应商关联创建成功");

                yizhuce = false;
            }

            if (yizhuce) {
                System.out.println("返回: 用户已注册错误");
                return R.error("-1");
            } else {
                System.out.println("返回: 注册成功");
                return R.ok();
            }
            
        } catch (Exception e) {
            System.out.println("========== 发生异常 ==========");
            System.out.println("异常类型: " + e.getClass().getName());
            System.out.println("异常信息: " + e.getMessage());
            e.printStackTrace();
            return R.error(-1, "注册失败: " + e.getMessage());
        }

    }


    private NxJrdhSupplierEntity saveJrdhSupplerGb(NxJrdhUserEntity jrdhUserEntity, Integer gbDisId, Integer buyUserId,
                                                   Integer gbDepId) {
        NxJrdhSupplierEntity supplierEntity = new NxJrdhSupplierEntity();
        supplierEntity.setNxJrdhsUserId(jrdhUserEntity.getNxJrdhUserId());
        supplierEntity.setNxJrdhsGbDistributerId(gbDisId);
        supplierEntity.setNxJrdhsSupplierName(jrdhUserEntity.getNxJrdhWxNickName());
        supplierEntity.setNxJrdhsNxCommunityId(-1);
        supplierEntity.setNxJrdhsNxPurUserId(-1);
        supplierEntity.setNxJrdhsCommPurUserId(-1);
        supplierEntity.setNxJrdhsNxJrdhBuyUserId(buyUserId);
        supplierEntity.setNxJrdhsGbDepartmentId(gbDepId);
        supplierEntity.setNxJrdhsNxDistributerId(-1);
        supplierEntity.setNxJrdhsStatus(0);
        supplierEntity.setNxJrdhsSysCityId(6);
        supplierEntity.setNxJrdhsSysMarketId(-1);
        nxJrdhSupplierService.save(supplierEntity);



        return supplierEntity;
    }



    private NxJrdhSupplierEntity saveJrdhSupplerGbWithNxBusiness(NxJrdhUserEntity jrdhUserEntity, Integer gbDisId, Integer buyUserId,
                                                   Integer gbDepId, Integer purUserId, Integer nxDisId) {
        NxJrdhSupplierEntity supplierEntity = new NxJrdhSupplierEntity();
        supplierEntity.setNxJrdhsUserId(jrdhUserEntity.getNxJrdhUserId());
        supplierEntity.setNxJrdhsGbDistributerId(gbDisId);
        supplierEntity.setNxJrdhsSupplierName(jrdhUserEntity.getNxJrdhWxNickName());
        supplierEntity.setNxJrdhsNxCommunityId(-1);
        supplierEntity.setNxJrdhsNxPurUserId(-1);
        supplierEntity.setNxJrdhsCommPurUserId(-1);
        supplierEntity.setNxJrdhsNxJrdhBuyUserId(buyUserId);
        supplierEntity.setNxJrdhsGbPurUserId(purUserId);
        supplierEntity.setNxJrdhsGbDepartmentId(gbDepId);
        supplierEntity.setNxJrdhsNxDistributerId(nxDisId);
        supplierEntity.setNxJrdhsStatus(0);
        supplierEntity.setNxJrdhsSysCityId(6);
        supplierEntity.setNxJrdhsSysMarketId(-1);
        nxJrdhSupplierService.save(supplierEntity);



        NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity = new NxDistributerGbDistributerEntity();

        nxDistributerGbDistributerEntity.setNxDgdGbDistributerId(gbDisId);
        nxDistributerGbDistributerEntity.setNxDgdNxDistributerId(nxDisId);
        nxDistributerGbDistributerEntity.setNxDgdGbDepId(gbDepId);
        nxDistributerGbDistributerEntity.setNxDgdGbGoodsPrice(1);
        nxDistributerGbDistributerEntity.setNxDgdStatus(0);
        nxDistributerGbDistributerEntity.setNxDgdGbPayPeriodWeek(4);
        nxDistributerGbDistributerEntity.setNxDgdFromNxDepId(-1);
        nxDisGbDisService.save(nxDistributerGbDistributerEntity);


        return supplierEntity;
    }


    private NxJrdhSupplierEntity saveJrdhSupplerDis(NxJrdhUserEntity jrdhUserEntity, Integer nxDisId, Integer buyUserId) {
        System.out.println("savesuplierr");
        NxJrdhSupplierEntity supplierEntity = new NxJrdhSupplierEntity();
        supplierEntity.setNxJrdhsUserId(jrdhUserEntity.getNxJrdhUserId());
        supplierEntity.setNxJrdhsNxDistributerId(nxDisId);
        supplierEntity.setNxJrdhsSupplierName(jrdhUserEntity.getNxJrdhWxNickName());
        supplierEntity.setNxJrdhsNxCommunityId(-1);
        supplierEntity.setNxJrdhsNxJrdhBuyUserId(buyUserId);
        supplierEntity.setNxJrdhsGbDistributerId(-1);
        supplierEntity.setNxJrdhsGbDepartmentId(-1);
        supplierEntity.setNxJrdhsNxPurUserId(-1);
        supplierEntity.setNxJrdhsGbPurUserId(-1);
        supplierEntity.setNxJrdhsCommPurUserId(-1);
        supplierEntity.setNxJrdhsStatus(0);
        supplierEntity.setNxJrdhsSysCityId(6);
        supplierEntity.setNxJrdhsSysMarketId(-1);
        nxJrdhSupplierService.save(supplierEntity);
        return supplierEntity;
    }

    @RequestMapping(value = "/jrdhUserPurchaserRegisterWithFile", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R jrdhUserPurchaserRegisterWithFile(@RequestParam("file") MultipartFile file,
                                               @RequestParam("userName") String userName,
                                               @RequestParam("code") String code,
                                               @RequestParam("admin") Integer admin,
                                               @RequestParam("nxDisId") Integer nxDisId,
                                               @RequestParam("nxDisPurUserId") Integer nxDisPurUserId,
                                               @RequestParam("commId") Integer commId,
                                               @RequestParam("commPurUserId") Integer commPurUserId,
                                               @RequestParam("gbDisId") Integer gbDisId,
                                               @RequestParam("gbDepId") Integer gbDepId,
                                               @RequestParam("gbDepUserId") Integer gbDepUserId,
                                               HttpSession session) {
        System.out.println("aa" + nxDisId);
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getJinriDinghuoAppId() +
                "&secret=" + myAPPIDConfig.getJinriDinghuoScreat() + "&js_code=" + code + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);

        // 转成Json对象 获取openidjrdhUserRegister
        JSONObject jsonObject = JSONObject.parseObject(str);
        System.out.println(jsonObject);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        Map<String, Object> map = new HashMap<>();
        map.put("openId", openid);
        map.put("nxDisId", nxDisId);
        map.put("gbDisId", gbDisId);
        List<NxJrdhUserEntity> userEntityList = nxJrdhUserService.queryJrdhUserByParams(map);
        if (userEntityList.size() > 0) {

            return R.error("-1");
        } else {

            //添加新用户
            NxJrdhUserEntity jrdhUserEntity = new NxJrdhUserEntity();
            jrdhUserEntity.setNxJrdhWxOpenId(openid);
            jrdhUserEntity.setNxJrdhJoinDate(formatWhatDay(0));
            //1,上传图片
            String newUploadName = "uploadImage";
            String realPath = UploadFile.upload(session, newUploadName, file);

            String filename = file.getOriginalFilename();
            String filePath = newUploadName + "/" + filename;
            jrdhUserEntity.setNxJrdhWxNickName(userName);
            jrdhUserEntity.setNxJrdhWxAvartraUrl(filePath);
            jrdhUserEntity.setNxJrdhUrlChange(1);
            jrdhUserEntity.setNxJrdhNxDistributerId(nxDisId);
            jrdhUserEntity.setNxJrdhNxPurchaserUserId(nxDisPurUserId);
            jrdhUserEntity.setNxJrdhNxCommunityId(commId);
            jrdhUserEntity.setNxJrdhCommPurchaserUserId(commPurUserId);
            jrdhUserEntity.setNxJrdhGbDistributerId(gbDisId);
            jrdhUserEntity.setNxJrdhGbDepartmentId(gbDepId);
            jrdhUserEntity.setNxJrdhGbDepartmentUserId(gbDepUserId);
            jrdhUserEntity.setNxJrdhAdmin(admin);
            jrdhUserEntity.setNxJrdhDeviceId("-1");
            jrdhUserEntity.setNxJrdhDevicePrintId("-1");
            nxJrdhUserService.save(jrdhUserEntity);

            Map<String, Object> mapRe = new HashMap<>();
            mapRe.put("disInfo", nxDistributerService.queryObject(nxDisId));
            mapRe.put("userInfo", jrdhUserEntity);
            return R.ok().put("data", mapRe);
        }

    }


}


//    @RequestMapping(value = "/jrdhUserRegister", method = RequestMethod.POST)
//    @ResponseBody
//    public R jrdhUserRegister(@RequestBody NxJrdhUserEntity jrdhUserEntity) {
//
//        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
//
//        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getJinriDinghuoAppId() +
//                "&secret=" + myAPPIDConfig.getJinriDinghuoScreat() + "&js_code=" + jrdhUserEntity.getNxJrdhCode() + "&grant_type=authorization_code";
//        // 发送请求，返回Json字符串
//        String str = WeChatUtil.httpRequest(url, "GET", null);
//
//        // 转成Json对象 获取openid
//        JSONObject jsonObject = JSONObject.parseObject(str);
//        System.out.println(jsonObject);
//
//        // 我们需要的openid，在一个小程序中，openid是唯一的
//        String openid = jsonObject.get("openid").toString();
//        //判断是否suppler
//
//        //添加新用户
//        jrdhUserEntity.setNxJrdhWxOpenId(openid);
//        jrdhUserEntity.setNxJrdhJoinDate(formatWhatDay(0));
//        nxJrdhUserService.save(jrdhUserEntity);
//        NxDistributerSupplierEntity nxDistributerSupplierEntity = jrdhUserEntity.getNxDistributerSupplierEntity();
//        if (nxDistributerSupplierEntity != null) {
//            nxDistributerSupplierEntity.setNxDsJrdhUserId(jrdhUserEntity.getNxJrdhUserId());
//            nxDistributerSupplierService.save(nxDistributerSupplierEntity);
//        }
//
//        NxJrdhUserEntity buyUserEntity1 = nxJrdhUserService.queryWhichUserByOpenId(openid);
//
//        return R.ok().put("data", buyUserEntity1);
//
//    }
//
//}
