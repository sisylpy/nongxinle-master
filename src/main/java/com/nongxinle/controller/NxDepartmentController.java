package com.nongxinle.controller;

/**
 * @author lpy
 * @date 06-16 11:26
 */

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.temporal.ChronoUnit;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
//import com.nongxinle.service.NxDepartmentOrdersService;
import com.nongxinle.service.*;
import com.nongxinle.utils.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import sun.tools.jconsole.JConsole;

import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.PinYin4jUtils.*;


@RestController
@RequestMapping("api/nxdepartment")
public class NxDepartmentController {
    @Autowired
    private NxDepartmentService nxDepartmentService;
    @Autowired
    private NxDepartmentUserService nxDepartmentUserService;
    @Autowired
    private NxDepartmentDisGoodsService nxDepartmentDisGoodsService;
    @Autowired
    private NxDepartmentBillService nxDepartmentBillService;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private NxDistributerGbDistributerService nxDisGbDisService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private NxDepartmentDisGoodsService departmentDisGoodsService;
    @Autowired
    private NxDistributerUserService nxDistributerUserService;
    @Autowired
    private NxDepartmentOrderHistoryService historyService;
    @Autowired
    private NxDistributerGbDistributerService nxDistributerGbDistributerService;
    @Autowired
    private NxOcrTaskService nxOcrTaskService;

    private Map<String, Boolean> tokenStatus = new HashMap<>();


    @RequestMapping(value = "/changeDeps", method = RequestMethod.POST)
    @ResponseBody
    public R changeDeps(Integer oldDepId, Integer newDepId) {
        NxDepartmentEntity newDepartmentEntity = nxDepartmentService.queryObject(newDepId);
        Integer newFatherId = newDepartmentEntity.getNxDepartmentFatherId();
        if (newDepartmentEntity.getNxDepartmentFatherId() == 0) {
            newFatherId = newDepId;
        }

        //order
        Map<String, Object> map = new HashMap<>();
        map.put("depId", oldDepId);
        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        if (nxDepartmentOrdersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : nxDepartmentOrdersEntities) {
                ordersEntity.setNxDoDepartmentId(newDepId);
                ordersEntity.setNxDoDepartmentFatherId(newFatherId);
                nxDepartmentOrdersService.update(ordersEntity);
            }
        }


        //orderHistory
        List<NxDepartmentOrderHistoryEntity> historyEntities = historyService.queryDisHistoryOrdersByParams(map);
        if (nxDepartmentOrdersEntities.size() > 0) {
            for (NxDepartmentOrderHistoryEntity ordersEntity : historyEntities) {
                ordersEntity.setNxDoDepartmentId(newDepId);
                ordersEntity.setNxDoDepartmentFatherId(newFatherId);
                historyService.update(ordersEntity);
            }
        }

        //depDisGoods

        List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = departmentDisGoodsService.queryDepDisGoodsByParams(map);
        if (departmentDisGoodsEntities.size() > 0) {
            for (NxDepartmentDisGoodsEntity departmentDisGoodsEntity : departmentDisGoodsEntities) {
                departmentDisGoodsEntity.setNxDdgDepartmentId(newDepId);
                departmentDisGoodsEntity.setNxDdgDepartmentFatherId(newFatherId);
                departmentDisGoodsService.update(departmentDisGoodsEntity);
            }
        }

        List<NxDepartmentBillEntity> nxDepartmentBillEntities = nxDepartmentBillService.queryBillsListByParams(map);
        if (nxDepartmentBillEntities.size() > 0) {
            for (NxDepartmentBillEntity billEntity : nxDepartmentBillEntities) {
                billEntity.setNxDbDepId(newDepId);
                billEntity.setNxDbDepFatherId(newFatherId);
                nxDepartmentBillService.update(billEntity);
            }
        }


        return R.ok();
    }


    @RequestMapping(value = "/getPrepareDeliveryDepartment/{disId}")
    @ResponseBody
    public R getPrepareDeliveryDepartment(@PathVariable Integer disId) {

        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("equalStatus", 2);
        System.out.println("dapapfpa" + map1);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryOrderDepartmentList(map1);

        return R.ok().put("data", departmentEntities);
    }


    @RequestMapping(value = "/updateDepPickName", method = RequestMethod.POST)
    @ResponseBody
    public R updateDepPickName(Integer depId, String pickName) {
        NxDepartmentEntity nxDepartmentEntity = nxDepartmentService.queryObject(depId);
        nxDepartmentEntity.setNxDepartmentPickName(pickName);
        nxDepartmentService.update(nxDepartmentEntity);
        return R.ok();
    }

    // 模拟生成二维码时调用，记录 token 的状态
    @RequestMapping(value = "/scan", method = RequestMethod.GET)
    public String scan(@RequestParam String token) {
        // 存储 token 状态，初始为未登录
        tokenStatus.put(token, false);
        return "二维码已生成，请扫描";
    }


    // 模拟扫码后验证 token 登录状态
//    @RequestMapping(value = "/status", method = RequestMethod.GET)
//    public Map<String, Object> checkStatus(@RequestParam String token) {
//        Map<String, Object> response = new HashMap<>();
//        System.out.println("stusutususd----" + token);
//        if (tokenStatus.containsKey(token)) {
//            // 模拟扫码成功
//            boolean loggedIn = new Random().nextBoolean();
//            tokenStatus.put(token, loggedIn);
//            response.put("loggedIn", loggedIn);
//        } else {
//            response.put("error", "Token 不存在");
//        }
//        return response;
//    }


    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public Map<String, Object> getStatus(@RequestParam String token) {
        System.out.println("toemmene123" + token);
        Map<String, Object> response = new HashMap<>();
        if ("uniqueToken123".equals(token)) {
            response.put("loggedIn", true);
        } else {
            response.put("loggedIn", false);
        }
        return response;
    }


    @RequestMapping(value = "/changeMultiDeps", method = RequestMethod.POST)
    @ResponseBody
    public R changeMultiDeps(@RequestBody NxDepartmentEntity depart) {
        Integer nxDepartmentId = depart.getNxDepartmentId();
        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", nxDepartmentId);
        map.put("status", 3);
        System.out.println("mapapa"  + map);
        Integer integer = nxDepartmentOrdersService.queryOrderGoodsCount(map);
        if(integer > 0){
            return R.error(-1,"有未完成订单，不能修改部门");
        }

        List<NxDepartmentEntity> nxDepartmentEntities = depart.getNxDepartmentEntities();
        if (nxDepartmentEntities.size() > 0) {
            for (NxDepartmentEntity departmentEntity : nxDepartmentEntities) {
                departmentEntity.setNxDepartmentAttrName(departmentEntity.getNxDepartmentName());
                departmentEntity.setNxDepartmentRecordMinutes(30);
                nxDepartmentService.saveJustDepartment(departmentEntity);
            }
        }

        depart.setNxDepartmentSubAmount(depart.getNxDepartmentEntities().size());
        nxDepartmentService.update(depart);

        return R.ok().put("data", depart);
    }

    @RequestMapping(value = "/delSubDep/{delId}")
    @ResponseBody
    public R delSubDep(@PathVariable Integer delId) {

        NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(delId);
        Integer fatherId = departmentEntity.getNxDepartmentFatherId();
        //bill
        Map<String, Object> map = new HashMap<>();
        map.put("depId", departmentEntity.getNxDepartmentId());
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(map);
        if (billEntityList.size() > 0) {
            for (NxDepartmentBillEntity billEntity : billEntityList) {
                billEntity.setNxDbDepId(fatherId);
                nxDepartmentBillService.update(billEntity);
            }
        }

        //order
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                ordersEntity.setNxDoDepartmentId(fatherId);
                nxDepartmentOrdersService.update(ordersEntity);
            }
        }

        //depGoods
        List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = departmentDisGoodsService.queryDepDisGoodsByParams(map);
        if (departmentDisGoodsEntities.size() > 0) {
            for (NxDepartmentDisGoodsEntity departmentDisGoodsEntity : departmentDisGoodsEntities) {
                departmentDisGoodsService.delete(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
            }
        }

        nxDepartmentService.delete(delId);
        NxDepartmentEntity fatherDepart = nxDepartmentService.queryObject(fatherId);

        Integer nxDepartmentSubAmount = fatherDepart.getNxDepartmentSubAmount();
        int wxCountAuto = nxDepartmentSubAmount - 1;
        fatherDepart.setNxDepartmentSubAmount(wxCountAuto);
        nxDepartmentService.update(fatherDepart);

        return R.ok();
    }

    @RequestMapping(value = "/disDeleteSubDeps/{id}")

    @ResponseBody
    public R disDeleteSubDeps(@PathVariable Integer id) {

        List<NxDepartmentEntity> departmentEntities = nxDepartmentService.querySubDepartments(id);
        if (departmentEntities.size() > 0) {
            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                if (departmentEntity.getNxDepartmentFatherId().equals(id)) {
                    //bill
                    Map<String, Object> map = new HashMap<>();
                    map.put("depId", departmentEntity.getNxDepartmentId());
                    List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(map);
                    if (billEntityList.size() > 0) {
                        for (NxDepartmentBillEntity billEntity : billEntityList) {
                            billEntity.setNxDbDepId(id);
                            nxDepartmentBillService.update(billEntity);
                        }
                    }

                    //order
                    List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
                    if (ordersEntities.size() > 0) {
                        for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                            ordersEntity.setNxDoDepartmentId(id);
                            nxDepartmentOrdersService.update(ordersEntity);
                        }
                    }

                    //depGoods
                    List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = departmentDisGoodsService.queryDepDisGoodsByParams(map);
                    TreeSet<NxDepartmentDisGoodsEntity> list = new TreeSet<>();
                    if (departmentDisGoodsEntities.size() > 0) {
                        for (NxDepartmentDisGoodsEntity departmentDisGoodsEntity : departmentDisGoodsEntities) {
                            departmentDisGoodsEntity.setNxDdgDepartmentId(id);
                            list.add(departmentDisGoodsEntity);
                            departmentDisGoodsService.delete(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
                        }
                    }

                    if (list.size() > 0) {
                        for (NxDepartmentDisGoodsEntity disGoodsEntity : list) {
                            departmentDisGoodsService.save(disGoodsEntity);
                        }
                    }

                    nxDepartmentService.delete(departmentEntity.getNxDepartmentId());
                }
            }
            NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(id);
            departmentEntity.setNxDepartmentSubAmount(0);
            nxDepartmentService.update(departmentEntity);
        }

        return R.ok();
    }

    @RequestMapping(value = "/updateNxDep", method = RequestMethod.POST)
    @ResponseBody
    public R updateNxDep(@RequestBody NxDepartmentEntity department) {
        department.setNxDepartmentPinyin(hanziToPinyin(department.getNxDepartmentName()));
        nxDepartmentService.update(department);
        return R.ok();
    }


    @RequestMapping(value = "/disGetPayDepList/{disId}")
    @ResponseBody
    public R disGetPayDepList(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("type", 1);
        map.put("isGroup", 1);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentService.queryDepartmentBySettleType(map);


        return R.ok().put("data", departmentEntities);
    }

    @RequestMapping(value = "/cashRegisterLaodu", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public R cashRegisterLaodu(@RequestParam("file") MultipartFile file,
                               @RequestParam("depName") String depName,
                               @RequestParam("code") String code,
                               @RequestParam("disId") Integer disId,
                               HttpSession session) {


        System.out.println("dkfdjasflasf" + file);
        System.out.println("dkfdjasflasf" + depName);


        //wxApp
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String purchaseAppID = myAPPIDConfig.getLaoDuDinghuoAppID();
        String purchaseScreat = myAPPIDConfig.getLaoDuDinghuoScreat();


        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + purchaseAppID + "&secret=" + purchaseScreat + "&js_code=" + code + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        NxDepartmentUserEntity nxDepartmentUserEntity1 = nxDepartmentUserService.queryDepUserByOpenId(openid);
        if (nxDepartmentUserEntity1 == null) {
            NxDepartmentEntity departmentEntity = new NxDepartmentEntity();
            departmentEntity.setNxDepartmentDisId(disId);
            departmentEntity.setNxDepartmentName(depName);
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
            departmentEntity.setNxDepartmentAttrName(depName);
            departmentEntity.setNxDepartmentOrderCode(depName);
            departmentEntity.setNxDepartmentRecordMinutes(30);
            departmentEntity.setNxDepartmentJoinDate(formatWhatDay(0));
            departmentEntity.setNxDepartmentOrderTotal(0);
            departmentEntity.setNxDepartmentRecordMinutes(0);
            nxDepartmentService.saveJustDepartment(departmentEntity);

            //1,上传图片
            String newUploadName = "uploadImage";
            String realPath = UploadFile.upload(session, newUploadName, file);

            String filename = file.getOriginalFilename();
            String filePath = newUploadName + "/" + filename;

            NxDepartmentUserEntity userEntity = new NxDepartmentUserEntity();
            userEntity.setNxDuDistributerId(disId);
            userEntity.setNxDuDepartmentId(departmentEntity.getNxDepartmentId());
            userEntity.setNxDuDepartmentFatherId(departmentEntity.getNxDepartmentId());
            userEntity.setNxDuWxAvartraUrl(filePath);
            userEntity.setNxDuUrlChange(1);
            userEntity.setNxDuWxOpenId(openid);
            userEntity.setNxDuWxNickName(depName);
            userEntity.setNxDuAdmin(1);
            userEntity.setNxDuJoinDate(formatWhatDay(0));
            userEntity.setNxDuLoginTimes(0);

            nxDepartmentUserService.save(userEntity);


            Map<String, Object> stringObjectMap = nxDepartmentService.queryDepAndUserInfo(userEntity.getNxDepartmentUserId());
            return R.ok();
        } else {
            return R.error(-1, "此微信号已注册过采购员");
        }


    }

    /**
     * 保存
     */
    @RequestMapping(value = "/saveNewCustomerLaoDu", method = RequestMethod.POST)
    @ResponseBody
    public R saveNewCustomerLaoDu(String phoneCode, Integer depUserId, String machineId) {

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String appId = myAPPIDConfig.getLaoDuDinghuoAppID();
        String secret = myAPPIDConfig.getLaoDuDinghuoScreat();


        //添加新用户

        String urlPhone = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appId, secret);
        String strPhone = WeChatUtil.httpRequest(urlPhone, "GET", null);
        System.out.println("str=====>>>>" + strPhone);
        // 转成Json对象 获取openid
        JSONObject jsonObjectPhone = JSONObject.parseObject(strPhone);
        System.out.println("jsonObject" + jsonObjectPhone);
        String accessToken = jsonObjectPhone.getString("access_token");
        //通过token和code来获取用户手机号
        String urlP = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=" + accessToken + "&code=" + phoneCode;
        Map<String, Object> map = new HashMap<>();
        map.put("code", phoneCode);
        String body = HttpRequest.post(urlP).body(JSONUtil.toJsonStr(map), ContentType.JSON.getValue()).execute().body();

        JSONObject jsonObjectP = JSONObject.parseObject(body);

        String phoneI = jsonObjectP.getString("phone_info");
        JSONObject jsonObjectPInfo = JSONObject.parseObject(phoneI);
        String phone = jsonObjectPInfo.getString("phoneNumber");

        NxDepartmentUserEntity nxDepartmentUserEntity = nxDepartmentUserService.queryObject(depUserId);
        nxDepartmentUserEntity.setNxDuWxPhone(phone);
        nxDepartmentUserEntity.setNxDuLoginCode(machineId);
        nxDepartmentUserService.update(nxDepartmentUserEntity);


        Map<String, TemplateData> mapNotice = new HashMap<>();
        mapNotice.put("thing2", new TemplateData(nxDepartmentUserEntity.getNxDuWxNickName()));
        mapNotice.put("time3", new TemplateData(formatWhatDayTime(0)));
        mapNotice.put("phrase5", new TemplateData("待审核"));
        mapNotice.put("phrase9", new TemplateData("待审核"));
        mapNotice.put("phone_number6", new TemplateData(nxDepartmentUserEntity.getNxDuWxPhone()));
        System.out.println("nociiciiiicicautotootototoototoRRRRR" + mapNotice);
        Map<String, Object> mapU = new HashMap<>();
        mapU.put("disId", nxDepartmentUserEntity.getNxDuDistributerId());
        mapU.put("admin", 0);
        List<NxDistributerUserEntity> userEntities = nxDistributerUserService.queryRoleNxDisRoleUserList(mapU);
        if (userEntities.size() > 0) {
            for (NxDistributerUserEntity userEntity : userEntities) {
                System.out.println("diusern" + userEntity.getNxDiuWxNickName());
                WeNoticeService.nxCashDepSave(userEntity.getNxDiuWxOpenId(), "subPackage/pages/customer/index/index", mapNotice);

            }

        }


        return R.ok().put("data", nxDepartmentUserEntity);

    }


    @ResponseBody
    @RequestMapping(value = "/addDepPciture", produces = "text/html;charset=UTF-8")
    public R addDepPciture(@RequestParam("file") MultipartFile file,
                           @RequestParam("disId") Integer disId,
                           @RequestParam("name") String name,
                           HttpSession session) {

        //1,上传图片
        String newUploadName = "uploadImage";
        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
        String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), false, null);

        String lastFileName = getXiegang(headPinyin) + formatFullTime();
        String realPath = UploadFile.uploadFileName(session, newUploadName, file, lastFileName);
        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + lastFileName + ".jpg";

        NxDepartmentEntity nxDepartmentEntity = new NxDepartmentEntity();
        nxDepartmentEntity.setNxDepartmentFilePath(filePath);
        nxDepartmentEntity.setNxDepartmentName(name);
        nxDepartmentEntity.setNxDepartmentAttrName(name);
        nxDepartmentEntity.setNxDepartmentOrderCode(name);
        nxDepartmentEntity.setNxDepartmentRecordMinutes(30);
        nxDepartmentEntity.setNxDepartmentDisId(disId);
        nxDepartmentEntity.setNxDepartmentFatherId(0);
        nxDepartmentEntity.setNxDepartmentSubAmount(0);
        nxDepartmentEntity.setNxDepartmentIsGroupDep(1);
        nxDepartmentEntity.setNxDepartmentPrintName("ApplyFiftyPanel");
        nxDepartmentEntity.setNxDepartmentSettleType(99);
        nxDepartmentEntity.setNxDepartmentWorkingStatus(0);
        nxDepartmentEntity.setNxDepartmentJoinDate(formatWhatDay(0));
        nxDepartmentEntity.setNxDepartmentOrderTotal(0);
        nxDepartmentService.saveJustDepartment(nxDepartmentEntity);

        return R.ok();
    }


   /**
    * 配送商获取客户列表
    * @param disId 配送商id
    * @return
    * @date 2026-01-10
    */
    @RequestMapping(value = "/disGetAllCustomer/{disId}")
    @ResponseBody
    public R disGetAllDisDepartments(@PathVariable Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("type", 0);
        map.put("gbDisId", -1);
        List<NxDepartmentEntity> entities = nxDepartmentService.queryDepartmentBySettleType(map);
        map.put("type", 1);
        List<NxDepartmentEntity> entities2 = nxDepartmentService.queryDepartmentBySettleType(map);

        Map<String, Object> mapData = new HashMap<>();
        mapData.put("settleTypeOne", entities);
        mapData.put("settleTypeTwo", entities2);

        Map<String, Object> mapTask = new HashMap<>();
        mapTask.put("disId", disId);
        mapTask.put("xiaoyuStatus", 2);
        int total = nxOcrTaskService.queryTotalByDepartmentAndStatus(mapTask);

        return R.ok().put("data", mapData)
                .put("taskCount", total);
    }

    @RequestMapping(value = "/disGetAllCustomerWeb/{disId}")
    @ResponseBody
    public R disGetAllDisDepartmentsWeb(@PathVariable Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("type", 1);
        List<NxDepartmentEntity> entities = nxDepartmentService.queryDepartmentBySettleType(map);

        return R.ok().put("data", entities);
    }

    @RequestMapping(value = "/disGetAllGbDistributerWeb/{disId}")
    @ResponseBody
    public R disGetAllGbDistributerWeb(@PathVariable Integer disId) {

        Map<String, Object> mapG = new HashMap<>();
        mapG.put("nxDisId", disId);
        mapG.put("isSupplierId", -1);
        System.out.println("abdbdbbdd" + mapG);
        List<GbDistributerEntity> shixianArr = nxDisGbDisService.queryGbDistributerByParams(mapG);

        return R.ok().put("data", shixianArr);
    }

    /**
     * 批发商添加客户
     *
     * @param
     * @return 0
     */
    @RequestMapping(value = "/saveOneCustomerDesk", method = RequestMethod.POST)
    @ResponseBody
    public R saveOneCustomerDesk(@RequestBody NxDepartmentEntity departmentEntity) {
        //1,保存部
        NxDepartmentEntity entity = nxDepartmentService.saveJustDepartment(departmentEntity);

        Integer fatherId = departmentEntity.getNxDepartmentFatherId();
        NxDepartmentEntity father = nxDepartmentService.queryObject(fatherId);
        father.setNxDepartmentSubAmount(new BigDecimal(father.getNxDepartmentSubAmount() + 1).intValue());
        nxDepartmentService.update(father);

        return R.ok().put("data", entity);
    }

    /**
     * 配送商添加客户
     * @param distributerDepartmentEntity 客户
     * @return
     * @date 2026-01-10
     */
    @RequestMapping(value = "/saveOneCustomer", method = RequestMethod.POST)
    @ResponseBody
    public R saveOneCustomer(@RequestBody NxDistributerDepartmentEntity distributerDepartmentEntity) {

        NxDepartmentEntity nxDepartmentEntity = distributerDepartmentEntity.getNxDepartmentEntity();

        //1,保存部门
        nxDepartmentService.saveJustDepartment(nxDepartmentEntity);

//		//2，保存批发商部门
        Integer nxDepartmentId = nxDepartmentEntity.getNxDepartmentId();

        //3，如果有子部门，则保存子部门nxDepartmentEntityList
        List<NxDepartmentEntity> nxDepartmentEntities = nxDepartmentEntity.getNxSubDepartments();

        if (nxDepartmentEntity.getNxSubDepartments().size() > 0) {
            for (NxDepartmentEntity sub : nxDepartmentEntities) {
                sub.setNxDepartmentFatherId(nxDepartmentId);
                sub.setNxDepartmentSettleType(nxDepartmentEntity.getNxDepartmentSettleType());
                sub.setNxDepartmentDisId(nxDepartmentEntity.getNxDepartmentDisId());
                sub.setNxDepartmentType(nxDepartmentEntity.getNxDepartmentType());
                sub.setNxDepartmentAttrName(sub.getNxDepartmentName());
                sub.setNxDepartmentOrderCode(sub.getNxDepartmentName());
                sub.setNxDepartmentRecordMinutes(30);
                sub.setNxDepartmentWorkingStatus(0);
                sub.setNxDepartmentJoinDate(formatWhatDay(0));
                sub.setNxDepartmentOrderTotal(0);
                nxDepartmentService.saveJustDepartment(sub);
            }
        }
        return R.ok();
    }


    @RequestMapping(value = "/deleteGroupDep", method = RequestMethod.POST)
    @ResponseBody
    public R deleteGroupDep(@RequestBody NxDepartmentEntity dep) {
        Integer departmentId = dep.getNxDepartmentId();

        //depUser
        List<NxDepartmentUserEntity> userEntities = nxDepartmentUserService.queryAllUsersByDepId(departmentId);
        if (userEntities.size() > 0 ) {
            for (NxDepartmentUserEntity user : userEntities) {
                if(user.getNxDuDepartmentId().equals(departmentId)){
                    nxDepartmentUserService.delete(user.getNxDepartmentUserId());
                }
            }
        }

        //depGoods
        Map<String, Object> map = new HashMap<>();
        map.put("depId", departmentId);
        List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(map);
        if (departmentDisGoodsEntities.size() > 0) {
            for (NxDepartmentDisGoodsEntity disGoods : departmentDisGoodsEntities) {
                if(disGoods.getNxDdgDepartmentId().equals(departmentId)){
                    nxDepartmentDisGoodsService.delete(disGoods.getNxDepartmentDisGoodsId());
                }

            }
        }
        //depBill
        Map<String, Object> map1 = new HashMap<>();
        map1.put("depFatherId", departmentId);
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(map1);
        if (billEntityList.size() > 0) {
            for (NxDepartmentBillEntity bill : billEntityList) {
                if(bill.getNxDbDepId().equals(departmentId)){
                    nxDepartmentBillService.delete(bill.getNxDepartmentBillId());
                }
            }
        }

        nxDepartmentService.delete(departmentId);

        return R.ok();
    }


    @RequestMapping(value = "/deletePictureDep/{id}")
    @ResponseBody
    public R deletePictureDep(@PathVariable Integer id) {
        nxDepartmentService.delete(id);
        return R.ok();
    }


    /**
     * PURCHASE
     * 修改群名称
     *
     * @param departmentEntity 群
     * @return ok
     */
    @RequestMapping(value = "/updateGroupName", method = RequestMethod.POST)
    @ResponseBody
    public R updateGroupName(@RequestBody NxDepartmentEntity departmentEntity) {

        departmentEntity.setNxDepartmentPinyin(hanziToPinyin(departmentEntity.getNxDepartmentName()));

        nxDepartmentService.update(departmentEntity);
        if (departmentEntity.getNxDepartmentEntities().size() > 0) {
            for (NxDepartmentEntity subdep : departmentEntity.getNxDepartmentEntities()) {
                Integer nxDepartmentId = subdep.getNxDepartmentId();
                NxDepartmentEntity nxDepartmentEntity = nxDepartmentService.queryObject(nxDepartmentId);
                nxDepartmentEntity.setNxDepartmentPrintName(departmentEntity.getNxDepartmentPrintName());
                nxDepartmentEntity.setNxDepartmentSettleType(departmentEntity.getNxDepartmentSettleType());
                nxDepartmentEntity.setNxDepartmentType(departmentEntity.getNxDepartmentType());
                nxDepartmentEntity.setNxDepartmentPinyin(hanziToPinyin(subdep.getNxDepartmentName()));

                nxDepartmentService.update(nxDepartmentEntity);
            }

        }
        return R.ok();
    }

    @RequestMapping(value = "/updateGroupNameWithNxDisBusiness", method = RequestMethod.POST)
    @ResponseBody
    public R updateGroupNameWithNxDisBusiness(@RequestBody NxDepartmentEntity departmentEntity) {


        departmentEntity.setNxDepartmentPinyin(hanziToPinyin(departmentEntity.getNxDepartmentName()));
        nxDepartmentService.update(departmentEntity);
        Map<String, Object> map = new HashMap<>();
        map.put("gbDisId", departmentEntity.getNxDepartmentGbDistributerId());
        map.put("nxDisId", departmentEntity.getNxDepartmentDisId());
        NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity = nxDistributerGbDistributerService.queryObjectByParams(map);

        if (nxDistributerGbDistributerEntity != null) {
            nxDistributerGbDistributerEntity.setNxDgdGbPayMethod(departmentEntity.getNxDepartmentSettleType());
            nxDistributerGbDistributerService.update(nxDistributerGbDistributerEntity);
        }
        return R.ok();
    }



    /**
     * 微信小程序扫描二维码校验文件
     *
     * @return 校验内容
     */
    @RequestMapping(value = "/cash/rO1D9uJkqM.txt")
    @ResponseBody
    public String nxDepRegistCash() {
        return "33ccef96a8d5e4ac24783c37abf8ac13";
    }


    @RequestMapping(value = "/GyhokS8ddA.txt")
    @ResponseBody
    public String nxDepRegist() {
        return "9dac34a5cde884212f36c70694dec25f";
    }

    @RequestMapping(value = "/GyhokS8ddA1.txt")
    @ResponseBody
    public String nxDepRegistLaoDuCustomer() {
        return "9dac34a5cde884212f36c70694dec25f";
    }


    /**
     * PURCHASE
     * 采购员注册
     *
     * @param dep 订货群restrauntRegist
     * @return 群信息
     */
    @RequestMapping(value = "/cashRegister", method = RequestMethod.POST)
    @ResponseBody
    public R cashRegister(@RequestBody NxDepartmentEntity dep) {

        //wxApp
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String purchaseAppID = myAPPIDConfig.getShixianLiliAppId();
        String purchaseScreat = myAPPIDConfig.getShixianLiliScreat();

        NxDepartmentUserEntity nxDepartmentUserEntity = dep.getNxDepartmentUserEntity();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + purchaseAppID + "&secret=" + purchaseScreat + "&js_code=" + nxDepartmentUserEntity.getNxDuCode() + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        NxDepartmentUserEntity nxDepartmentUserEntity1 = nxDepartmentUserService.queryDepUserByOpenId(openid);
        if (nxDepartmentUserEntity1 == null) {
            dep.getNxDepartmentUserEntity().setNxDuWxOpenId(openid);
            Integer depUserId = nxDepartmentService.saveNewDepartment(dep);
            if (depUserId != null) {
                Map<String, Object> stringObjectMap = nxDepartmentService.queryDepAndUserInfo(depUserId);
                return R.ok().put("data", stringObjectMap);
            }
            return R.error(-1, "注册失败");
        } else {
            return R.error(-1, "此微信号已注册过采购员");
        }


    }

    /**
     * PURCHASE
     * 采购员注册
     *
     * @param dep 订货群restrauntRegist
     * @return 群信息
     */
    @RequestMapping(value = "/departmentRegister", method = RequestMethod.POST)
    @ResponseBody
    public R departmentRegister(@RequestBody NxDepartmentEntity dep) {

//wxApp
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String purchaseAppID = myAPPIDConfig.getPurchaseAppID();
        String purchaseScreat = myAPPIDConfig.getPurchaseScreat();

        NxDepartmentUserEntity nxDepartmentUserEntity = dep.getNxDepartmentUserEntity();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + purchaseAppID + "&secret=" + purchaseScreat + "&js_code=" + nxDepartmentUserEntity.getNxDuCode() + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        NxDepartmentUserEntity nxDepartmentUserEntity1 = nxDepartmentUserService.queryDepUserByOpenId(openid);
        if (nxDepartmentUserEntity1 == null) {
            dep.getNxDepartmentUserEntity().setNxDuWxOpenId(openid);
            dep.setNxDepartmentWorkingStatus(0);
            dep.setNxDepartmentOweBoxNumber(0);
            dep.setNxDepartmentDeliveryBoxNumber(0);
            dep.setNxDepartmentUnPayTotal("0");
            dep.setNxDepartmentAddCount(0);
            dep.setNxDepartmentPayTotal("0");
            dep.setNxDepartmentProfitTotal("0");
            dep.setNxDepartmentType("unFixed");
            dep.setNxDepartmentPrintName("ApplyFiftyPanel");
            Integer depUserId = nxDepartmentService.saveNewDepartment(dep);
            if (depUserId != null) {
                Map<String, Object> stringObjectMap = nxDepartmentService.queryDepAndUserInfo(depUserId);
                return R.ok().put("data", stringObjectMap);
            }
            return R.error(-1, "注册失败");
        } else {
            return R.error(-1, "此微信号已注册过采购员");
        }


    }


    /**
     * PURCHASE
     * 采购员注册
     *
     * @param dep 订货群
     * @return 群信息
     */
    @RequestMapping(value = "/chainDepartmentRegister", method = RequestMethod.POST)
    @ResponseBody
    public R chainDepartmentRegister(@RequestBody NxDepartmentEntity dep) {
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String purchaseAppID = myAPPIDConfig.getPurchaseAppID();
        String purchaseScreat = myAPPIDConfig.getPurchaseScreat();

        NxDepartmentUserEntity nxDepartmentUserEntity = dep.getNxDepartmentUserEntity();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + purchaseAppID + "&secret=" + purchaseScreat + "&js_code=" + nxDepartmentUserEntity.getNxDuCode() + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);
        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();
        NxDepartmentUserEntity nxDepartmentUserEntity1 = nxDepartmentUserService.queryDepUserByOpenId(openid);
        if (nxDepartmentUserEntity1 == null) {
            dep.getNxDepartmentUserEntity().setNxDuWxOpenId(openid);
            Integer depUserId = nxDepartmentService.saveNewChainDepartment(dep);
            if (depUserId != null) {
                Map<String, Object> stringObjectMap = nxDepartmentService.queryDepAndUserInfo(depUserId);
                return R.ok().put("data", stringObjectMap);
            }
            return R.error(-1, "注册失败");
        } else {
            return R.error(-1, "此微信号已注册过采购员");
        }

    }


    /**
     * ORDER
     * 获取群的子部门
     *
     * @param depId 群id
     * @return 子部门列表
     */
    @RequestMapping(value = "/getSubDepartments/{depId}")
    @ResponseBody
    public R getSubDepartments(@PathVariable Integer depId) {
        System.out.println(depId);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentService.querySubDepartments(depId);
        return R.ok().put("data", departmentEntities);
    }


    /**
     * ORDER
     *
     * @param depId
     * @return
     */
    @RequestMapping(value = "/getDepInfo/{depId}")
    @ResponseBody
    public R getDepInfo(@PathVariable Integer depId) {
        System.out.println(depId + "idiidgetDepInfo");
        NxDepartmentEntity nxDepartmentEntity = nxDepartmentService.queryDepInfo(depId);
        return R.ok().put("data", nxDepartmentEntity);
    }

    @RequestMapping(value = "/getGbDisDepInfo/{disId}")
    @ResponseBody
    public R getGbDisDepInfo(@PathVariable Integer disId) {

        NxDepartmentEntity nxDepartmentEntity = nxDepartmentService.queryDepInfoByGbDisId(disId);
        return R.ok().put("data", nxDepartmentEntity);
    }


    @RequestMapping(value = "/getGroupInfo/{depId}")
    @ResponseBody
    public R getGroupInfo(@PathVariable Integer depId) {
        NxDepartmentEntity nxDepartmentEntity = nxDepartmentService.queryGroupInfo(depId);
        return R.ok().put("data", nxDepartmentEntity);
    }


//	//////////////////


//
//	@RequestMapping(value = "/getFatherDep/{depId}")
//	@ResponseBody
//	public R getFatherDep(@PathVariable Integer depId) {
//		List<NxDepartmentEntity> departmentEntities = nxDepartmentService.queryFatherDep(depId);
//	    return R.ok().put("data", departmentEntities.get(0));
//	}


//
//	/**
//	 * 保存
//	 */
//	@ResponseBody
//	@RequestMapping("/saveSubDepartment")
//	public R saveSubDepartment(@RequestBody NxDepartmentEntity nxDepartment){
//		List<NxDepartmentEntity> nxDepartmentEntities = nxDepartment.getNxDepartmentEntities();
//		for (NxDepartmentEntity dep : nxDepartmentEntities) {
//		nxDepartmentService.saveSubDepartment(dep);
//		}
//		Integer nxDepartmentId = nxDepartment.getNxDepartmentId();
//		NxDepartmentEntity nxDepartmentEntity = nxDepartmentService.queryObject(nxDepartmentId);
//		Integer nxDepartmentSubAmount = nxDepartmentEntity.getNxDepartmentSubAmount();
//		nxDepartmentEntity.setNxDepartmentSubAmount(nxDepartmentSubAmount + nxDepartment.getNxDepartmentEntities().size());
//		nxDepartmentService.update(nxDepartmentEntity);
//		return R.ok();
//	}


//
//	 @RequestMapping(value = "/getDisDepartments", method = RequestMethod.POST)
//	  @ResponseBody
//	  public R getDisDepartments (Integer disId, String type) {
//
//		 Map<String, Object> map = new HashMap<>();
//		 map.put("disId", disId);
//		 map.put("type", type);
//		 List<NxDepartmentEntity> list =  nxDepartmentService.queryDisDepartments(map);
//		 return R.ok().put("data", list);
//	  }


    /**
     * 保存
     */
//	@ResponseBody
//	@RequestMapping("/save")
//	public R save(@RequestBody NxDepartmentEntity nxDepartment){
//		nxDepartmentService.save(nxDepartment);
//		return R.ok();
//	}


}
