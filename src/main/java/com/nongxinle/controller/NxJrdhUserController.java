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
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import sun.tools.jconsole.JConsole;

import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.formatWhatDay;


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


    @RequestMapping(value = "/sellerUpdateSupplier", method = RequestMethod.POST)
    @ResponseBody
    public R sellerUpdateSupplier (Integer supplierId, Integer sellerId) {
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
            map.put("admin", 1);
            System.out.println("newlogogoogogogogogo" + map);
            NxJrdhUserEntity nxBuyUserEntity1 = nxJrdhUserService.queryJrdhUserById(map);
            if (nxBuyUserEntity1 != null) {
                return R.ok().put("data", nxBuyUserEntity1);
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

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String maimaiAppID = myAPPIDConfig.getJinriDinghuoAppId();
        String maimaiScreat = myAPPIDConfig.getJinriDinghuoScreat();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + maimaiAppID + "&secret=" +
                maimaiScreat + "&js_code=" + code +
                "&grant_type=authorization_code";
        String str = WeChatUtil.httpRequest(url, "GET", null);
        JSONObject jsonObject = JSONObject.parseObject(str);
        String openId = jsonObject.get("openid").toString();
        Map<String, Object> map = new HashMap<>();
        map.put("nxDisInfo", null);
        map.put("gbDisInfo", null);
        map.put("buyerInfo", null);
        map.put("sellerInfo", null);
        map.put("nxDisArr", null);
        map.put("gbDisArr", null);
        map.put("supplierArr", new ArrayList<>());

        if (openId != null) {

            Map<String, Object> mapU = new HashMap<>();
            mapU.put("openId", openId);
            mapU.put("admin", 1);
            NxJrdhUserEntity jrdhUserEntity = nxJrdhUserService.queryJrdhUserById(mapU);
            if (jrdhUserEntity != null) {
                map.put("buyerInfo", jrdhUserEntity);
                if (jrdhUserEntity.getNxJrdhNxDistributerId() != -1) {
                    NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(jrdhUserEntity.getNxJrdhNxDistributerId());
                    map.put("nxDisInfo", nxDistributerEntity);
                    map.put("gbDisInfo", null);
                    Map<String, Object> mapS = new HashMap<>();
                    mapS.put("nxDisId", jrdhUserEntity.getNxJrdhNxDistributerId());
                    mapS.put("userId", -1);
                    List<NxJrdhSupplierEntity> supplierEntities = nxJrdhSupplierService.queryJrdhSupplerByParams(mapS);
                    map.put("supplierArr", supplierEntities);
                }
                if (jrdhUserEntity.getNxJrdhGbDistributerId() != -1) {
                    GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(jrdhUserEntity.getNxJrdhGbDistributerId());
                    map.put("nxDisInfo", null);
                    map.put("gbDisInfo", gbDistributerEntity);
                    Map<String, Object> mapS = new HashMap<>();
                    mapS.put("gbDisId", jrdhUserEntity.getNxJrdhGbDistributerId());
                    mapS.put("userId", -1);
                    List<NxJrdhSupplierEntity> supplierEntities = nxJrdhSupplierService.queryJrdhSupplerByParams(mapS);
                    map.put("supplierArr", supplierEntities);
                }

                mapU.put("admin", 3);
                NxJrdhUserEntity jrdhUserEntitySellTo = nxJrdhUserService.queryJrdhUserById(mapU);
                if(jrdhUserEntitySellTo != null){
                    Map<String, Object> mapS = new HashMap<>();
                    mapS.put("userId", jrdhUserEntitySellTo.getNxJrdhUserId());
                    mapS.put("gbDisId", -1);
                    List<NxJrdhSupplierEntity> supplierEntities = nxJrdhSupplierService.queryJrdhSupplerWithDisByUserId(mapS);

                    Map<String, Object> mapSG = new HashMap<>();
                    mapSG.put("nxDisId", -1);
                    mapSG.put("userId", jrdhUserEntitySellTo.getNxJrdhUserId());
                    System.out.println("akdfaflkjas;fkdsaf;" + mapSG);
                    List<NxJrdhSupplierEntity> supplierEntitiesG = nxJrdhSupplierService.queryJrdhSupplerWithDisByUserId(mapSG);
                    map.put("gbDisArr", supplierEntitiesG);
                    map.put("nxDisArr", supplierEntities);
                    map.put("sellerInfo", jrdhUserEntitySellTo);
                }

                return R.ok().put("data", map);
            } else {

                mapU.put("admin", 3);
                NxJrdhUserEntity jrdhUserEntitySell = nxJrdhUserService.queryJrdhUserById(mapU);
                if (jrdhUserEntitySell != null) {
                    Map<String, Object> mapS = new HashMap<>();
                    mapS.put("userId", jrdhUserEntitySell.getNxJrdhUserId());
                    mapS.put("gbDisId", -1);
                    List<NxJrdhSupplierEntity> supplierEntities = nxJrdhSupplierService.queryJrdhSupplerWithDisByUserId(mapS);

                    Map<String, Object> mapSG = new HashMap<>();
                    mapSG.put("nxDisId", -1);
                    mapSG.put("userId", jrdhUserEntitySell.getNxJrdhUserId());
                    System.out.println("akdfaflkjas;fkdsaf;" + mapSG);
                    List<NxJrdhSupplierEntity> supplierEntitiesG = nxJrdhSupplierService.queryJrdhSupplerWithDisByUserId(mapSG);
                    map.put("gbDisArr", supplierEntitiesG);
                    map.put("nxDisArr", supplierEntities);
                    map.put("sellerInfo", jrdhUserEntitySell);

                    return R.ok().put("data", map);
                } else {
                    return R.error(-1, "登录失败");
                }
            }

        } else {
            return R.error(-1, "注册失败");
        }
    }


    @RequestMapping(value = "/whichJrdhUserLogin", method = RequestMethod.POST)
    @ResponseBody
    public R whichJrdhUserLogin(String code, Integer nxDisId, Integer batchId,Integer gbDisId,Integer commId) {
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
            mapB.put("buyUserOpenId", openId);
            System.out.println("mapbBbBBBB" + mapB);
            NxDistributerPurchaseBatchEntity batchEntity = nxDisPurchaseBatchService.queryBatchItemByParams(mapB);
            if (batchEntity != null) {
                Map<String, Object> mapUser = new HashMap<>();
                mapUser.put("openId", openId);
                mapUser.put("nxDisId", nxDisId);
                mapUser.put("gbDisId", gbDisId);
                mapUser.put("commId", commId);
                mapUser.put("admin", 1);
                NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryJrdhUserById(mapUser);
                if (nxJrdhUserEntity != null) {
                    map.put("userInfo", nxJrdhUserEntity);
                    map.put("buyUser", true);
                    map.put("supplierInfo", null);
                }
            } else {
                Map<String, Object> mapUserSell = new HashMap<>();
                mapUserSell.put("openId", openId);
                mapUserSell.put("admin", 3);
                NxJrdhUserEntity jrdhUserEntitySell = nxJrdhUserService.queryJrdhUserById(mapUserSell);
                if (jrdhUserEntitySell != null) {
                    Map<String, Object> mapS = new HashMap<>();
                    mapS.put("nxDisId", nxDisId);
                    mapS.put("gbDisId", gbDisId);
                    mapS.put("commId", commId);
                    mapS.put("userId", jrdhUserEntitySell.getNxJrdhUserId());
                    System.out.println("mapssss" + mapS);
                    NxJrdhSupplierEntity nxJrdhSupplierEntity = nxJrdhSupplierService.querySellUserSupplier(mapS);
                    if (nxJrdhSupplierEntity == null) {
                        NxDistributerPurchaseBatchEntity nxPurchaseBatchEntity = nxDisPurchaseBatchService.queryObject(batchId);
                        NxJrdhSupplierEntity supplierEntity = saveJrdhSuppler(jrdhUserEntitySell, nxDisId, nxPurchaseBatchEntity.getNxDpbBuyUserId(), gbDisId, commId);
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
            return R.ok().put("data", map);

        } else {
            return R.error(-1, "登录失败");
        }
    }


//

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
                mapUser.put("admin", 1);
                mapUser.put("gbDisId", gbDisId);
                System.out.println("mapuusueur" + mapUser);
                NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryJrdhUserById(mapUser);
                if (nxJrdhUserEntity != null) {
                    map.put("userInfo", nxJrdhUserEntity);
                    map.put("buyUser", true);
                    map.put("supplierInfo", null);
                }
            } else {
                Map<String, Object> mapUserSell = new HashMap<>();
                mapUserSell.put("openId", openId);
                mapUserSell.put("admin", 3);
                System.out.println("mapuusuer" + mapUser);
                NxJrdhUserEntity jrdhUserEntitySell = nxJrdhUserService.queryJrdhUserById(mapUserSell);
                if (jrdhUserEntitySell != null) {
                    Map<String, Object> mapS = new HashMap<>();
                    mapS.put("gbDisId", gbDisId);
                    mapS.put("userId", jrdhUserEntitySell.getNxJrdhUserId());
                    System.out.println("gbsuppsosoeri" + mapS);
                    NxJrdhSupplierEntity nxJrdhSupplierEntity = nxJrdhSupplierService.querySellUserSupplier(mapS);
                    if (nxJrdhSupplierEntity == null) {
                        GbDistributerPurchaseBatchEntity gbPurchaseBatchEntity = gbDisPurchaseBatchService.queryObject(batchId);
                        NxJrdhSupplierEntity supplierEntity = saveJrdhSupplerGb(jrdhUserEntitySell, gbDisId, gbPurchaseBatchEntity.getGbDpbBuyUserId(), gbDepId, purUserId);
                        map.put("supplierInfo", supplierEntity);
                    } else {
                        map.put("supplierInfo", nxJrdhSupplierEntity);
                    }
                    map.put("buyUser", false);
                    map.put("userInfo", jrdhUserEntitySell);
                } else {
                    map.put("code", -1);
                    return R.ok().put("data", map);
                }
            }
            return R.ok().put("data", map);

        } else {
            return R.error(-1, "注册失败");
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


    @RequestMapping(value = "/jrdhSupplierSellerRegisterWithFile", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R jrdhSupplierSellerRegisterWithFile(@RequestParam("file") MultipartFile file,
                                                @RequestParam("userName") String userName,
                                                @RequestParam("code") String code,
                                                @RequestParam("admin") Integer admin,
                                                @RequestParam("nxDisId") Integer nxDisId,
                                                @RequestParam("buyerUserId") Integer buyerUserId,
                                                @RequestParam("gbDisId") Integer gbDisId,
                                                @RequestParam("commId") Integer commId,
                                                @RequestParam("supplierId") Integer supplierId,
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
        map.put("admin", 3);
        map.put("openId", openid);
        NxJrdhUserEntity jrdhUserEntity1 = nxJrdhUserService.queryJrdhUserByAdmin(map);
        if (jrdhUserEntity1 != null) {
            yizhuce = true;
            System.out.println("savememmemmeaupsps" + nxDisId);
            NxJrdhSupplierEntity supplierEntity = saveJrdhSuppler(jrdhUserEntity1, nxDisId, buyerUserId,gbDisId, commId);
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


            NxJrdhSupplierEntity supplierEntity = nxJrdhSupplierService.queryObject(supplierId);
            supplierEntity.setNxJrdhsNxJrdhBuyUserId(buyerUserId);
            supplierEntity.setNxJrdhsUserId(jrdhUserEntity.getNxJrdhUserId());
            nxJrdhSupplierService.update(supplierEntity);

            yizhuce = false;
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
                                          @RequestParam("buyerUserId") Integer buyerUserId,
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
            NxJrdhSupplierEntity supplierEntity = saveJrdhSupplerGb(jrdhUserEntity1, gbDisId, buyerUserId, gbDepId, purUserId);
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

            NxJrdhSupplierEntity supplierEntity = saveJrdhSupplerGb(jrdhUserEntity, gbDisId, buyerUserId, gbDepId, purUserId);

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
    public R jrdhSellerRegisterWithFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("userName") String userName,
                                        @RequestParam("code") String code,
                                        @RequestParam("admin") Integer admin,
                                        @RequestParam("nxDisId") Integer nxDisId,
                                        @RequestParam("gbDisId") Integer gbDisId,
                                        @RequestParam("commId") Integer commId,
                                        @RequestParam("buyerUserId") Integer buyerUserId,
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
        map.put("admin", 3);
        map.put("openId", openid);
        map.put("nxDisId", nxDisId);
        map.put("gbDisId", gbDisId);
        map.put("commId", commId);
        NxJrdhUserEntity jrdhUserEntity1 = nxJrdhUserService.queryJrdhUserByAdmin(map);
        if (jrdhUserEntity1 != null) {
            System.out.println("yizhucueueueueueue");
            yizhuce = true;
            NxJrdhSupplierEntity supplierEntity = saveJrdhSuppler(jrdhUserEntity1, nxDisId, buyerUserId, gbDisId, commId);
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

            NxJrdhSupplierEntity supplierEntity = saveJrdhSuppler(jrdhUserEntity, nxDisId, buyerUserId, gbDisId, commId);

            yizhuce = false;
        }

        if (yizhuce) {
            return R.error("-1");
        } else {
            return R.ok();
        }

    }


    private NxJrdhSupplierEntity saveJrdhSupplerGb(NxJrdhUserEntity jrdhUserEntity, Integer gbDisId, Integer buyerUserId,
                                                   Integer gbDepId, Integer purUserId) {
        NxJrdhSupplierEntity supplierEntity = new NxJrdhSupplierEntity();
        supplierEntity.setNxJrdhsUserId(jrdhUserEntity.getNxJrdhUserId());
        supplierEntity.setNxJrdhsGbDistributerId(gbDisId);
        supplierEntity.setNxJrdhsSupplierName(jrdhUserEntity.getNxJrdhWxNickName());
        supplierEntity.setNxJrdhsNxCommunityId(-1);
        supplierEntity.setNxJrdhsNxPurUserId(-1);
        supplierEntity.setNxJrdhsCommPurUserId(-1);
        supplierEntity.setNxJrdhsNxJrdhBuyUserId(buyerUserId);
        supplierEntity.setNxJrdhsGbPurUserId(purUserId);
        supplierEntity.setNxJrdhsGbDepartmentId(gbDepId);
        supplierEntity.setNxJrdhsNxDistributerId(-1);
        nxJrdhSupplierService.save(supplierEntity);
        return supplierEntity;
    }


    private NxJrdhSupplierEntity saveJrdhSuppler(NxJrdhUserEntity jrdhUserEntity, Integer nxDisId, Integer buyerUserId, Integer gbDisId, Integer commId) {
        System.out.println("savesuplierr");
        NxJrdhSupplierEntity supplierEntity = new NxJrdhSupplierEntity();
        supplierEntity.setNxJrdhsUserId(jrdhUserEntity.getNxJrdhUserId());
        supplierEntity.setNxJrdhsNxDistributerId(nxDisId);
        supplierEntity.setNxJrdhsSupplierName(jrdhUserEntity.getNxJrdhWxNickName());
        supplierEntity.setNxJrdhsNxCommunityId(commId);
        supplierEntity.setNxJrdhsNxJrdhBuyUserId(buyerUserId);
        supplierEntity.setNxJrdhsGbDistributerId(gbDisId);
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
        boolean yizhuce;
        Map<String, Object> map = new HashMap<>();
        map.put("openId", openid);
        map.put("nxDisId", nxDisId);
        map.put("gbDisId", gbDisId);
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


            yizhuce = false;
        }

        if (yizhuce) {
            return R.error("-1");
        } else {
            return R.ok();
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
