package com.nongxinle.controller;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nongxinle.utils.aes.WXBizMsgCrypt;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;

import static com.nongxinle.utils.DateUtils.*;

/**
 * 微信授权
 */
@Slf4j
@RestController
@RequestMapping("api/yewx")
public class QyWxControlloer {

    @Autowired
    QyNxDisCorpService qyNxDisCorpService;
    @Autowired
    QyNxDisCropUserService qyNxDisCropUserService;
    @Autowired
    QyGbDisCorpUserService qyGbDisCorpUserService;
    @Autowired
    QyGbDisCorpService qyGbDisCorpService;
    @Autowired
    private NxDistributerFatherGoodsService dgfService;
    @Autowired
    private NxDistributerGoodsService dgService;
    @Autowired
    private NxDistributerPurchaseGoodsService nxDistributerPurchaseGoodsService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private NxDistributerPurchaseBatchService nxDistributerPurchaseBatchService;
    @Autowired
    private NxDepartmentBillService nxDepartmentBillService;
    @Autowired
    private NxDepartmentService nxDepartmentService;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private NxDepartmentUserService nxDepartmentUserService;

    @RequestMapping(value = "/callbackdjapp", method = RequestMethod.POST)
    public void acceptMessageDjApp(final HttpServletRequest request,
                                @RequestParam(name = "msg_signature") final String sMsgSignature,
                                @RequestParam(name = "timestamp") final String sTimestamp,
                                @RequestParam(name = "nonce") final String sNonce,
                                HttpServletResponse response) {

        System.out.println("callbackdjapp--------------------------------------POST");
        try {
            InputStream inputStream = request.getInputStream();
            String sPostData = IOUtils.toString(inputStream, "UTF-8");

            Map<String, String> stringStringMap = MessageUtil.parseXml(sPostData);
            String toUserName = stringStringMap.get("ToUserName");
            System.out.println("toUserNametoUserName===" + toUserName);
            QywechatEnum qywechatEnum = QywechatEnum.DJAPPPOST;

            qywechatEnum.setCorpid(toUserName);
            QywechatInfo qywechatInfo = new QywechatInfo();
            qywechatInfo.setMsgSignature(sMsgSignature);
            qywechatInfo.setNonce(sNonce);
            qywechatInfo.setQywechatEnum(qywechatEnum);
            qywechatInfo.setTimestamp(sTimestamp);
            qywechatInfo.setSPostData(sPostData);
            System.out.println("DJororororoorororororoSUITEIDDjSUITEIDDjKH" + sPostData);
            WXBizMsgCrypt msgCrypt = new WXBizMsgCrypt(qywechatInfo.getQywechatEnum());
            String sMsg = msgCrypt.decryptMsg(qywechatInfo);
            Map<String, String> dataMap = MessageUtil.parseXml(sMsg);
            System.out.println("callbackdjapp回调信息：=========new" + dataMap);

            if (dataMap.get("Event").equals("enter_agent")) {
                System.out.println("Event====Event" + dataMap.get("Event"));
                System.out.println("Event !!!!");
            }

            try {
                PrintWriter writer = response.getWriter();
                writer.write("success");
                writer.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/callbackdjapp", method = RequestMethod.GET)
    @ResponseBody
    public void callbackdjapp(@RequestParam(name = "msg_signature") final String msgSignature,
                              @RequestParam(name = "timestamp") final String timestamp,
                              @RequestParam(name = "nonce") final String nonce,
                              @RequestParam(name = "echostr") final String echostr,
                              final HttpServletResponse response) throws Exception {
        System.out.println("callbackdjapp------------GET");
        System.out.println("CorpId== suiteid liziqiyecorpidsuiteid-");

        String s = saveProviderAccessToken();
        System.out.println("sssss=======" + s);
        String s1 = transferCorpId(s);
        System.out.println("s1 =================" + s1);
        QywechatEnum qywechatEnum = QywechatEnum.DJAPP;
        qywechatEnum.setCorpid(s1);
        WXBizMsgCrypt wxBizMsgCrypt = new WXBizMsgCrypt(qywechatEnum);
        System.out.println("miwen========" + wxBizMsgCrypt);
        String sEchoStr = wxBizMsgCrypt.verifyURL(msgSignature, timestamp, nonce, echostr);
        PrintWriter out = response.getWriter();
        try {
            //必须要返回解密之后的明文
            if (StringUtils.isBlank(sEchoStr)) {
                log.info("callbackdjappget验签URL验证失败");
            } else {
                log.info("callbackdjappget验签验证成功!");
            }
        } catch (Exception e) {
            log.error("callbackdjappget验签报错！", e);
        }
        log.info("callbackdjappget验签的echo是{}", sEchoStr);
        out.write(sEchoStr);
        out.flush();

    }


    @RequestMapping(value = "/callbackdj", method = RequestMethod.GET)
    @ResponseBody
    public void callbackdj(@RequestParam(name = "msg_signature") final String msgSignature,
                           @RequestParam(name = "timestamp") final String timestamp,
                           @RequestParam(name = "nonce") final String nonce,
                           @RequestParam(name = "echostr") final String echostr,
                           final HttpServletResponse response) throws Exception {
        System.out.println("callbackdj------GET");
        QywechatEnum qywechatEnum = QywechatEnum.DJ;
        WXBizMsgCrypt wxBizMsgCrypt = new WXBizMsgCrypt(qywechatEnum);
        String sEchoStr = wxBizMsgCrypt.verifyURL(msgSignature, timestamp, nonce, echostr);
        PrintWriter out = response.getWriter();
        try {
            //必须要返回解密之后的明文
            if (StringUtils.isBlank(sEchoStr)) {
                log.info("callbackdjget验签URL验证失败");
            } else {
                log.info("callbackdjget验签验证成功!");
            }
        } catch (Exception e) {
            log.error("callbackdjget验签报错！", e);
        }
        log.info("callbackdjget验签的echo是{}", sEchoStr);
        out.write(sEchoStr);
        out.flush();

    }


    @RequestMapping(value = "/callbackdj", method = RequestMethod.POST)
    public void acceptMessageDj(final HttpServletRequest request,
                                @RequestParam(name = "msg_signature") final String sMsgSignature,
                                @RequestParam(name = "timestamp") final String sTimestamp,
                                @RequestParam(name = "nonce") final String sNonce,
                                HttpServletResponse response) {

        System.out.println("callbackdj--------------------------------------POST");
        QywechatEnum qywechatEnum = QywechatEnum.DJPOST;
        try {
            InputStream inputStream = request.getInputStream();
            String sPostData = IOUtils.toString(inputStream, "UTF-8");
            QywechatInfo qywechatInfo = new QywechatInfo();
            qywechatInfo.setMsgSignature(sMsgSignature);
            qywechatInfo.setNonce(sNonce);
            qywechatInfo.setQywechatEnum(qywechatEnum);
            qywechatInfo.setTimestamp(sTimestamp);
            qywechatInfo.setSPostData(sPostData);
            System.out.println("DJororororoorororororoSUITEIDDjSUITEIDDjKH" + sPostData);
            WXBizMsgCrypt msgCrypt = new WXBizMsgCrypt(qywechatInfo.getQywechatEnum());
            String sMsg = msgCrypt.decryptMsg(qywechatInfo);
            Map<String, String> dataMap = MessageUtil.parseXml(sMsg);
            if (dataMap.get("InfoType").equals("create_auth")) {
                System.out.println("infotype====create_auth" + dataMap.get("InfoType"));
                System.out.println("nothing to do !!!!");
            }

            try {
                PrintWriter writer = response.getWriter();
                writer.write("success");
                writer.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @RequestMapping(value = "/callbacksx", method = RequestMethod.GET)
    public void receiveMsgSx(@RequestParam(name = "msg_signature") final String msgSignature,
                             @RequestParam(name = "timestamp") final String timestamp,
                             @RequestParam(name = "nonce") final String nonce,
                             @RequestParam(name = "echostr") final String echostr,
                             final HttpServletResponse response) throws Exception {
        System.out.println("callbackrxcallbackrx----------------------get");
        QywechatEnum qywechatEnum = QywechatEnum.JXPPSX;
        WXBizMsgCrypt wxBizMsgCrypt = new WXBizMsgCrypt(qywechatEnum);
        String sEchoStr = wxBizMsgCrypt.verifyURL(msgSignature, timestamp, nonce, echostr);
        PrintWriter out = response.getWriter();
        try {
            //必须要返回解密之后的明文
            if (StringUtils.isBlank(sEchoStr)) {
                log.info("get验签URL验证失败");
            } else {
                log.info("get验签验证成功!");
            }
        } catch (Exception e) {
            log.error("get验签报错！", e);
        }
        log.info("get验签的echo是{}", sEchoStr);
        out.write(sEchoStr);
        out.flush();
    }

    @RequestMapping(value = "/callbacksx", method = RequestMethod.POST)
    public void acceptMessageSx(final HttpServletRequest request,
                                @RequestParam(name = "msg_signature") final String sMsgSignature,
                                @RequestParam(name = "timestamp") final String sTimestamp,
                                @RequestParam(name = "nonce") final String sNonce,
                                HttpServletResponse response) {

        System.out.println("callbackrx----------------------POST");

        try {
            InputStream inputStream = request.getInputStream();
            String sPostData = IOUtils.toString(inputStream, "UTF-8");
            Map<String, String> stringStringMap = MessageUtil.parseXml(sPostData);
            String toUserName = stringStringMap.get("ToUserName");
            System.out.println("callbackrxtoUserNametoUserName===" + toUserName);
            QywechatEnum qywechatEnum = QywechatEnum.JXPPSX;
            qywechatEnum.setCorpid(toUserName);
            QywechatInfo qywechatInfo = new QywechatInfo();
            qywechatInfo.setMsgSignature(sMsgSignature);
            qywechatInfo.setNonce(sNonce);
            qywechatInfo.setQywechatEnum(qywechatEnum);
            qywechatInfo.setTimestamp(sTimestamp);
            qywechatInfo.setSPostData(sPostData);
            WXBizMsgCrypt msgCrypt = new WXBizMsgCrypt(qywechatInfo.getQywechatEnum());
            String sMsg = msgCrypt.decryptMsg(qywechatInfo);
            Map<String, String> dataMap = MessageUtil.parseXml(sMsg);
            System.out.println("callbackrx回调信息：=========new" + dataMap);

            if(dataMap.get("InfoType") != null){
                if (dataMap.get("InfoType").equals("suite_ticket")) {
                    System.out.println("saveTokenenSXXXX----------");
                    saveSuiteTokenSx(dataMap);
                } else if (dataMap.get("InfoType").equals("create_auth")) {
                    saveAuthTokenSx(dataMap);
                } else if (dataMap.get("InfoType").equals("cancel_auth")) {
                    deleteCorpRx(dataMap);
                } else {
                    System.out.println("callbackrx!== null and else====" + dataMap.get("InfoType"));
                }
            }
            if(dataMap.get("Event") != null){
                System.out.println("Event====Event=====" + dataMap.get("Event"));
            }

            try {
                PrintWriter writer = response.getWriter();
                writer.write("success");
                writer.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    @RequestMapping(value = "/callbackrx", method = RequestMethod.GET)
    public void receiveMsgRx(@RequestParam(name = "msg_signature") final String msgSignature,
                           @RequestParam(name = "timestamp") final String timestamp,
                           @RequestParam(name = "nonce") final String nonce,
                           @RequestParam(name = "echostr") final String echostr,
                           final HttpServletResponse response) throws Exception {
        System.out.println("callbackrxcallbackrx----------------------get");
        QywechatEnum qywechatEnum = QywechatEnum.JXPPRX;
        WXBizMsgCrypt wxBizMsgCrypt = new WXBizMsgCrypt(qywechatEnum);
        String sEchoStr = wxBizMsgCrypt.verifyURL(msgSignature, timestamp, nonce, echostr);
        PrintWriter out = response.getWriter();
        try {
            //必须要返回解密之后的明文
            if (StringUtils.isBlank(sEchoStr)) {
                log.info("get验签URL验证失败");
            } else {
                log.info("get验签验证成功!");
            }
        } catch (Exception e) {
            log.error("get验签报错！", e);
        }
        log.info("get验签的echo是{}", sEchoStr);
        out.write(sEchoStr);
        out.flush();
    }

    @RequestMapping(value = "/callbackrx", method = RequestMethod.POST)
    public void acceptMessageRx(final HttpServletRequest request,
                              @RequestParam(name = "msg_signature") final String sMsgSignature,
                              @RequestParam(name = "timestamp") final String sTimestamp,
                              @RequestParam(name = "nonce") final String sNonce,
                              HttpServletResponse response) {

        System.out.println("callbackrx----------------------POST");

        try {
            InputStream inputStream = request.getInputStream();
            String sPostData = IOUtils.toString(inputStream, "UTF-8");
            Map<String, String> stringStringMap = MessageUtil.parseXml(sPostData);
            String toUserName = stringStringMap.get("ToUserName");
            System.out.println("callbackrxtoUserNametoUserName===" + toUserName);
            QywechatEnum qywechatEnum = QywechatEnum.JXPPRX;
            qywechatEnum.setCorpid(toUserName);
            QywechatInfo qywechatInfo = new QywechatInfo();
            qywechatInfo.setMsgSignature(sMsgSignature);
            qywechatInfo.setNonce(sNonce);
            qywechatInfo.setQywechatEnum(qywechatEnum);
            qywechatInfo.setTimestamp(sTimestamp);
            qywechatInfo.setSPostData(sPostData);
            WXBizMsgCrypt msgCrypt = new WXBizMsgCrypt(qywechatInfo.getQywechatEnum());
            String sMsg = msgCrypt.decryptMsg(qywechatInfo);
            Map<String, String> dataMap = MessageUtil.parseXml(sMsg);
            System.out.println("callbackrx回调信息：=========new" + dataMap);

            if(dataMap.get("InfoType") != null){
                if (dataMap.get("InfoType").equals("suite_ticket")) {
                    System.out.println("saveTokenenSXXXX----------");
                    saveSuiteTokenRx(dataMap);
                } else if (dataMap.get("InfoType").equals("create_auth")) {
                    saveAuthTokenRx(dataMap);
                } else if (dataMap.get("InfoType").equals("cancel_auth")) {
                    deleteCorpSx(dataMap);
                } else {
                    System.out.println("callbackrx!== null and else====" + dataMap.get("InfoType"));
                }
            }
            if(dataMap.get("Event") != null){
                System.out.println("Event====Event=====" + dataMap.get("Event"));
            }

            try {
                PrintWriter writer = response.getWriter();
                writer.write("success");
                writer.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    @RequestMapping(value = "/callback", method = RequestMethod.GET)
    public void receiveMsg(@RequestParam(name = "msg_signature") final String msgSignature,
                           @RequestParam(name = "timestamp") final String timestamp,
                           @RequestParam(name = "nonce") final String nonce,
                           @RequestParam(name = "echostr") final String echostr,
                           final HttpServletResponse response) throws Exception {
        System.out.println("callback----------------------get");
        QywechatEnum qywechatEnum = QywechatEnum.JXPP;
        WXBizMsgCrypt wxBizMsgCrypt = new WXBizMsgCrypt(qywechatEnum);
        String sEchoStr = wxBizMsgCrypt.verifyURL(msgSignature, timestamp, nonce, echostr);
        PrintWriter out = response.getWriter();
        try {
            //必须要返回解密之后的明文
            if (StringUtils.isBlank(sEchoStr)) {
                log.info("get验签URL验证失败");
            } else {
                log.info("get验签验证成功!");
            }
        } catch (Exception e) {
            log.error("get验签报错！", e);
        }
        log.info("get验签的echo是{}", sEchoStr);
        out.write(sEchoStr);
        out.flush();
    }


    @RequestMapping(value = "/callback", method = RequestMethod.POST)
    public void acceptMessage(final HttpServletRequest request,
                              @RequestParam(name = "msg_signature") final String sMsgSignature,
                              @RequestParam(name = "timestamp") final String sTimestamp,
                              @RequestParam(name = "nonce") final String sNonce,
                              HttpServletResponse response) {

        System.out.println("callback----------------------POST");

        try {
            InputStream inputStream = request.getInputStream();
            String sPostData = IOUtils.toString(inputStream, "UTF-8");
            Map<String, String> stringStringMap = MessageUtil.parseXml(sPostData);
            String toUserName = stringStringMap.get("ToUserName");
            System.out.println("toUserNametoUserName===" + toUserName);
            QywechatEnum qywechatEnum = QywechatEnum.JXPP;
            qywechatEnum.setCorpid(toUserName);
            QywechatInfo qywechatInfo = new QywechatInfo();
            qywechatInfo.setMsgSignature(sMsgSignature);
            qywechatInfo.setNonce(sNonce);
            qywechatInfo.setQywechatEnum(qywechatEnum);
            qywechatInfo.setTimestamp(sTimestamp);
            qywechatInfo.setSPostData(sPostData);
            WXBizMsgCrypt msgCrypt = new WXBizMsgCrypt(qywechatInfo.getQywechatEnum());
            String sMsg = msgCrypt.decryptMsg(qywechatInfo);
            Map<String, String> dataMap = MessageUtil.parseXml(sMsg);
            System.out.println("callback回调信息：=========new" + dataMap);

            if(dataMap.get("InfoType") != null){

                if (dataMap.get("InfoType").equals("suite_ticket")) {
                    saveSuiteToken(dataMap);
                } else if (dataMap.get("InfoType").equals("create_auth")) {
                    saveAuthToken(dataMap);
                } else if (dataMap.get("InfoType").equals("cancel_auth")) {
                    deleteCorp(dataMap);
                } else {
                    System.out.println("infotype!== null and else====" + dataMap.get("InfoType"));
                }
            }
            if(dataMap.get("Event") != null){
                System.out.println("Event====Event=====" + dataMap.get("Event"));
            }


            try {
                PrintWriter writer = response.getWriter();
                writer.write("success");
                writer.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void deleteCorpSx(Map<String, String> mapSuite) {

        System.out.println("delteCorp===============" + mapSuite.get("AuthCorpId"));
        String corpid = mapSuite.get("AuthCorpId");

        //1 查询企业ceshisaveTokenenneenneSSSXXXXXX
        QyGbDisCorpEntity qyNxDisCorpEntity = qyGbDisCorpService.queryQyCropByCropId(corpid);
        Integer qyNxDisCorpId = qyNxDisCorpEntity.getQyGbDisCorpId();

        //2 del user
        List<QyGbDisCorpUserEntity> qyNxDisCropUserEntities = qyGbDisCorpUserService.queryCorpUserListByCorpId(qyNxDisCorpId);
        List<Integer> aaa = new ArrayList<>();
        if (qyNxDisCropUserEntities.size() > 0) {
            for (QyGbDisCorpUserEntity corpUser : qyNxDisCropUserEntities) {
                aaa.add(corpUser.getQyGbDisCorpUserId());
            }

            Integer[] integers = aaa.toArray(new Integer[]{});
            qyGbDisCorpUserService.deleteBatch(integers);
        }

        //3 del corp
        qyGbDisCorpService.deleteCropByCropId(corpid);

    }

    private void deleteCorpRx(Map<String, String> mapSuite) {

        System.out.println("delteCorp===============" + mapSuite.get("AuthCorpId"));
        String corpid = mapSuite.get("AuthCorpId");

        //1 查询企业ceshisaveTokenenneenneSSSXXXXXX
        QyGbDisCorpEntity qyNxDisCorpEntity = qyGbDisCorpService.queryQyCropByCropId(corpid);
        Integer qyNxDisCorpId = qyNxDisCorpEntity.getQyGbDisCorpId();

        //2 del user
        List<QyGbDisCorpUserEntity> qyNxDisCropUserEntities = qyGbDisCorpUserService.queryCorpUserListByCorpId(qyNxDisCorpId);
        List<Integer> aaa = new ArrayList<>();
        if (qyNxDisCropUserEntities.size() > 0) {
            for (QyGbDisCorpUserEntity corpUser : qyNxDisCropUserEntities) {
                aaa.add(corpUser.getQyGbDisCorpUserId());
            }

            Integer[] integers = aaa.toArray(new Integer[]{});
            qyGbDisCorpUserService.deleteBatch(integers);
        }

        //3 del corp
        qyGbDisCorpService.deleteCropByCropId(corpid);

    }


    private void deleteCorpNxGoods(Integer disId){

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        //fatherGoods
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisFathersGoodsByParams(map);
        if(fatherGoodsEntities.size() > 0){
            for(NxDistributerFatherGoodsEntity fatherGoodsEntity: fatherGoodsEntities){
                dgfService.delete(fatherGoodsEntity.getNxDistributerFatherGoodsId());
            }
        }

        //goods
        List<NxDistributerGoodsEntity> nxGoodsEntities = dgService.queryDisGoodsByParams(map);
        if(nxGoodsEntities.size() > 0){
            for(NxDistributerGoodsEntity goodsEntity: nxGoodsEntities){
                //DEL goods
                dgService.delete(goodsEntity.getNxDistributerGoodsId());

                //DEL goods
                Map<String, Object> mapGoods = new HashMap<>();
                mapGoods.put("disGoodsId", goodsEntity.getNxDistributerGoodsId());
                Integer integer = nxDistributerPurchaseGoodsService.queryPurOrderCount(mapGoods);
                if(integer > 0){
                    List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(mapGoods);
                    if(purchaseGoodsEntities.size() > 0){
                        for(NxDistributerPurchaseGoodsEntity purchaseGoodsEntity: purchaseGoodsEntities){
                            nxDistributerPurchaseGoodsService.delete(purchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
                        }
                    }
                }


                //DEL ORDER
                Integer integerOrder = nxDepartmentOrdersService.queryDepOrdersAcount(mapGoods);
                if(integerOrder > 0){
                    List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(mapGoods);
                    if(ordersEntities.size() > 0){
                        for(NxDepartmentOrdersEntity ordersEntity: ordersEntities){
                            nxDepartmentOrdersService.delete(ordersEntity.getNxDepartmentOrdersId());
                        }
                    }
                }

                //DEL Batch
                int integerBatch = nxDistributerPurchaseBatchService.queryDisPurchaseBatchCount(mapGoods);
                if(integerBatch > 0){
                    List<NxDistributerPurchaseBatchEntity> batchEntities = nxDistributerPurchaseBatchService.queryDisPurchaseBatch(mapGoods);
                    if(batchEntities.size() > 0){
                        for(NxDistributerPurchaseBatchEntity batchEntity: batchEntities){
                            nxDistributerPurchaseBatchService.delete(batchEntity.getNxDistributerPurchaseBatchId());
                        }
                    }
                }

            }
        }

        //departent
        map.put("isGroup", 1);

        int countDep =  nxDepartmentService.queryDepartmentCount(map);
        if(countDep > 0){
           List<NxDepartmentEntity> departmentEntities =   nxDepartmentService.queryDepartmentListByParams(map);
           if(departmentEntities.size() > 0){
               for(NxDepartmentEntity departmentEntity: departmentEntities){

                   Map<String, Object> mapDep = new HashMap<>();
                   mapDep.put("depFatherId", departmentEntity.getNxDepartmentId() );
                   //DEL Batch
                   int wxCountAuto = nxDepartmentBillService.queryTotalByParams(mapDep);
                   if(wxCountAuto > 0){
                       List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsListByParams(mapDep);
                       if(billEntityList.size() > 0){
                           for(NxDepartmentBillEntity billEntity: billEntityList){
                               nxDepartmentBillService.delete(billEntity.getNxDepartmentBillId());
                           }
                       }
                   }

                   //DEL DepUser
                   List<NxDepartmentUserEntity> userEntities = nxDepartmentUserService.queryAllUsersByDepFatherId(departmentEntity.getNxDepartmentId());
                   if(userEntities.size() > 0){
                           for(NxDepartmentUserEntity userEntity: userEntities){
                               nxDepartmentUserService.delete(userEntity.getNxDepartmentUserId());

                       }
                   }

                   nxDepartmentService.delete(departmentEntity.getNxDepartmentId());

               }

           }
        }


    }

    private void deleteCorp(Map<String, String> mapSuite) {

        System.out.println("delteCorp===============" + mapSuite.get("AuthCorpId"));
        String corpid = mapSuite.get("AuthCorpId");

        //1 查询企业
        QyNxDisCorpEntity qyNxDisCorpEntity = qyNxDisCorpService.queryQyCropByCropId(corpid);
        Integer qyNxDisCorpId = qyNxDisCorpEntity.getQyNxDisCorpId();

        //2 del user
        List<QyNxDisCorpUserEntity> qyNxDisCropUserEntities = qyNxDisCropUserService.queryCorpUserListByCorpId(qyNxDisCorpId);
        List<Integer> aaa = new ArrayList<>();
        if (qyNxDisCropUserEntities.size() > 0) {
            for (QyNxDisCorpUserEntity corpUser : qyNxDisCropUserEntities) {
                aaa.add(corpUser.getQyNxDisCorpUserId());
                deleteCorpNxGoods(corpUser.getQyNxDistributerId());
            }

            Integer[] integers = aaa.toArray(new Integer[]{});
            qyNxDisCropUserService.deleteBatch(integers);
        }

        //3 del corp
        qyNxDisCorpService.deleteCropByCropId(corpid);

    }


    public void saveAuthTokenSx(Map<String, String> mapSuite) {
        System.out.println("saveAutSsssXxxx");
        // 获取永久授权码
        String suiteToken = getWxProperty(Constant.SUITE_TOKEN_SX);
        String authTokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/service/get_permanent_code?suite_access_token=" + suiteToken;
        System.out.println("rult" + authTokenUrl);
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("auth_code", mapSuite.get("AuthCode"));
            String body = HttpRequest.post(authTokenUrl).body(JSONUtil.toJsonStr(map), ContentType.JSON.getValue()).execute().body();
            System.out.println("authTokenUrlfanhuibody===SSSXXXXXXXX" + body);
            JSONObject corpInfo = JSONObject.parseObject(body);

//            String access_token = corpInfo.getString("access_token");
            String permanent_code = corpInfo.getString("permanent_code");
            String auth_corp_info = corpInfo.getString("auth_corp_info");
            System.out.println("whatisthisSSSSSXXXXX");
            JSONObject jsonObject = JSONObject.parseObject(auth_corp_info);
            System.out.println("diercengSXSSXXXX" + jsonObject);

            String corp_name = jsonObject.getString("corp_name");
            String corpid = jsonObject.getString("corpid");
            String corp_round_logo_url = jsonObject.getString("corp_round_logo_url");
            //
            Map<String, Object> mapCorp= new HashMap<>();
            mapCorp.put("auth_corpid", corpid);
            mapCorp.put("permanent_code", permanent_code);
            String coprTokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/service/get_corp_token?suite_access_token=" + suiteToken;

            String bodyCorp = HttpRequest.post(coprTokenUrl).body(JSONUtil.toJsonStr(mapCorp), ContentType.JSON.getValue()).execute().body();
            System.out.println("mapCorpmapCorpmapCorpmapCorpmapCorp===" + bodyCorp);
            JSONObject corpInfoCorp = JSONObject.parseObject(bodyCorp);
            String access_token = corpInfoCorp.getString("access_token");


            QyGbDisCorpEntity qyNxDisCorpEntity = new QyGbDisCorpEntity();
            qyNxDisCorpEntity.setQyGbDisQyCorpId(corpid);
            qyNxDisCorpEntity.setQyGbDisCorpName(corp_name);
            qyNxDisCorpEntity.setQyGbDisCorpRoundLogoUrl(corp_round_logo_url);
            qyNxDisCorpEntity.setQyGbDisCorpAccessToken(access_token);
            qyNxDisCorpEntity.setQyGbDisCorpPermanentCode(permanent_code);
            qyNxDisCorpEntity.setQyGbDisCorpJoinDate(formatWhatFullTime(0));
            System.out.println("saveAccessAuthCrop----SSXX" + qyNxDisCorpEntity);
            qyGbDisCorpService.save(qyNxDisCorpEntity);
            System.out.println("saveSqlok-----");
        } catch (Exception e) {
            System.out.println("woromd");
            throw new RuntimeException();
        }

    }


    public void saveAuthTokenRx(Map<String, String> mapSuite) {
        System.out.println("saveAutSsssXxxx");
        // 获取永久授权码
        String suiteToken = getWxProperty(Constant.SUITE_TOKEN_RX);
        String authTokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/service/get_permanent_code?suite_access_token=" + suiteToken;
        System.out.println("rult" + authTokenUrl);
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("auth_code", mapSuite.get("AuthCode"));
            String body = HttpRequest.post(authTokenUrl).body(JSONUtil.toJsonStr(map), ContentType.JSON.getValue()).execute().body();
            System.out.println("authTokenUrlfanhuibody===SSSXXXXXXXX" + body);
            JSONObject corpInfo = JSONObject.parseObject(body);

//            String access_token = corpInfo.getString("access_token");
            String permanent_code = corpInfo.getString("permanent_code");
            String auth_corp_info = corpInfo.getString("auth_corp_info");
            System.out.println("whatisthisSSSSSXXXXX");
            JSONObject jsonObject = JSONObject.parseObject(auth_corp_info);
            System.out.println("diercengSXSSXXXX" + jsonObject);

            String corp_name = jsonObject.getString("corp_name");
            String corpid = jsonObject.getString("corpid");
            String corp_round_logo_url = jsonObject.getString("corp_round_logo_url");
            //
            Map<String, Object> mapCorp= new HashMap<>();
            mapCorp.put("auth_corpid", corpid);
            mapCorp.put("permanent_code", permanent_code);
            String coprTokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/service/get_corp_token?suite_access_token=" + suiteToken;

            String bodyCorp = HttpRequest.post(coprTokenUrl).body(JSONUtil.toJsonStr(mapCorp), ContentType.JSON.getValue()).execute().body();
            System.out.println("mapCorpmapCorpmapCorpmapCorpmapCorp===" + bodyCorp);
            JSONObject corpInfoCorp = JSONObject.parseObject(bodyCorp);
            String access_token = corpInfoCorp.getString("access_token");


            QyGbDisCorpEntity qyNxDisCorpEntity = new QyGbDisCorpEntity();
            qyNxDisCorpEntity.setQyGbDisQyCorpId(corpid);
            qyNxDisCorpEntity.setQyGbDisCorpName(corp_name);
            qyNxDisCorpEntity.setQyGbDisCorpRoundLogoUrl(corp_round_logo_url);
            qyNxDisCorpEntity.setQyGbDisCorpAccessToken(access_token);
            qyNxDisCorpEntity.setQyGbDisCorpPermanentCode(permanent_code);
            qyNxDisCorpEntity.setQyGbDisCorpJoinDate(formatWhatFullTime(0));
            System.out.println("saveAccessAuthCrop----SSXX" + qyNxDisCorpEntity);
            qyGbDisCorpService.save(qyNxDisCorpEntity);
            System.out.println("saveSqlok-----");
        } catch (Exception e) {
            System.out.println("woromd");
            throw new RuntimeException();
        }

    }

    public void saveAuthToken(Map<String, String> mapSuite) {
        System.out.println("saveAut");
        // 获取永久授权码
        String suiteToken = getWxProperty(Constant.SUITE_TOKEN);
        String authTokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/service/get_permanent_code?suite_access_token=" + suiteToken;
        System.out.println("rult" + authTokenUrl);
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("auth_code", mapSuite.get("AuthCode"));
            String body = HttpRequest.post(authTokenUrl).body(JSONUtil.toJsonStr(map), ContentType.JSON.getValue()).execute().body();
            System.out.println("authTokenUrlfanhuibody===" + body);
            JSONObject corpInfo = JSONObject.parseObject(body);

//            String access_token = corpInfo.getString("access_token");
            String permanent_code = corpInfo.getString("permanent_code");
            String auth_corp_info = corpInfo.getString("auth_corp_info");
            System.out.println("whatisthis");
            JSONObject jsonObject = JSONObject.parseObject(auth_corp_info);
            System.out.println("dierceng" + jsonObject);

            String corp_name = jsonObject.getString("corp_name");
            String corpid = jsonObject.getString("corpid");
            String corp_round_logo_url = jsonObject.getString("corp_round_logo_url");
            System.out.println("saveAccessAuthCrop----corpid" + corpid);

            //
            Map<String, Object> mapCorp= new HashMap<>();
            mapCorp.put("auth_corpid", corpid);
            mapCorp.put("permanent_code", permanent_code);
            String bodyCorp = HttpRequest.post(authTokenUrl).body(JSONUtil.toJsonStr(mapCorp), ContentType.JSON.getValue()).execute().body();
            System.out.println("mapCorpmapCorpmapCorpmapCorpmapCorp===" + bodyCorp);
            JSONObject corpInfoCorp = JSONObject.parseObject(bodyCorp);
            String access_token = corpInfoCorp.getString("access_token");


            // get
            QyNxDisCorpEntity qyNxDisCorpEntity = new QyNxDisCorpEntity();
            qyNxDisCorpEntity.setQyNxDisQyCorpId(corpid);
            qyNxDisCorpEntity.setQyNxDisCorpName(corp_name);
            qyNxDisCorpEntity.setQyNxDisCorpRoundLogoUrl(corp_round_logo_url);
            qyNxDisCorpEntity.setQyNxDisCorpAccessToken(access_token);
            qyNxDisCorpEntity.setQyNxDisCorpPermanentCode(permanent_code);
            qyNxDisCorpEntity.setQyNxDisCorpJoinDate(formatWhatFullTime(0));
            System.out.println("saveAccessAuthCrop----" + qyNxDisCorpEntity);
            qyNxDisCorpService.save(qyNxDisCorpEntity);
            System.out.println("saveSqlok-----");
        } catch (Exception e) {
            System.out.println("woromd");
            throw new RuntimeException();
        }

    }

    public void saveAuthTokenDJ(Map<String, String> mapSuite) {
        System.out.println("saveAutDJ");
        // 获取永久授权码
        String suiteToken = getWxProperty(Constant.SUITE_TOKEN);
        String authTokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/service/get_permanent_code?suite_access_token=" + suiteToken;
        System.out.println("rult" + authTokenUrl);
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("auth_code", mapSuite.get("AuthCode"));
            String body = HttpRequest.post(authTokenUrl).body(JSONUtil.toJsonStr(map), ContentType.JSON.getValue()).execute().body();
            System.out.println("authTokenUrlfanhuibody===" + body);
            JSONObject corpInfo = JSONObject.parseObject(body);

            String access_token = corpInfo.getString("access_token");
            String permanent_code = corpInfo.getString("permanent_code");
            String auth_corp_info = corpInfo.getString("auth_corp_info");
            System.out.println("whatisthis");
            JSONObject jsonObject = JSONObject.parseObject(auth_corp_info);
            System.out.println("dierceng" + jsonObject);

            String corp_name = jsonObject.getString("corp_name");
            String corpid = jsonObject.getString("corpid");
            String corp_round_logo_url = jsonObject.getString("corp_round_logo_url");

            QyNxDisCorpEntity qyNxDisCorpEntity = new QyNxDisCorpEntity();
            qyNxDisCorpEntity.setQyNxDisQyCorpId(corpid);
            qyNxDisCorpEntity.setQyNxDisCorpName(corp_name);
            qyNxDisCorpEntity.setQyNxDisCorpRoundLogoUrl(corp_round_logo_url);
            qyNxDisCorpEntity.setQyNxDisCorpAccessToken(access_token);
            qyNxDisCorpEntity.setQyNxDisCorpPermanentCode(permanent_code);
            System.out.println("saveAccessAuthCrop----" + qyNxDisCorpEntity);
            qyNxDisCorpService.save(qyNxDisCorpEntity);
            System.out.println("saveSqlok-----");
        } catch (Exception e) {
            System.out.println("woromd");
            throw new RuntimeException();
        }
    }


    public void saveSuiteToken(Map<String, String> mapSuite) {
        System.out.println("gettokenekenekene");
        // 获取第三方应用凭证url
        String suiteTokenUrl = Constant.THIRD_BUS_WECHAT_SUITE_TOKEN;
        // 	第三方应用access_token
        String suiteToken = "";
        try {
            Map<String, Object> map = new HashMap<>();
            //以ww或wx开头应用id
            map.put("suite_id", Constant.SuiteID);
            //应用secret
            map.put("suite_secret", Constant.SuiteSecret);
            //企业微信后台推送的ticket
            map.put("suite_ticket", mapSuite.get("SuiteTicket"));
            String body = HttpRequest.post(suiteTokenUrl).body(JSONUtil.toJsonStr(map), ContentType.JSON.getValue()).execute().body();
            System.out.println("ceshisaveTokenenneenne" + body);
            WeChatSuiteReturn weChat = JSONUtil.toBean(body, WeChatSuiteReturn.class);
            if (weChat.getErrcode() == null || weChat.getErrcode() == 0) {
                // 保存suiteToken
                suiteToken = weChat.getSuite_access_token();
                saveWxProperty(Constant.SUITE_TOKEN, suiteToken);
            }
            // 打印消息
            System.out.println("获取suite token成功:" + suiteToken);
        } catch (Exception e) {
            System.out.println("获取suite token失败errcode:" + suiteToken);
            throw new RuntimeException();
        }

    }


    public void saveSuiteTokenSx(Map<String, String> mapSuite) {
        System.out.println("gettokenekenekeneSSSXXXXXXXXX");
        // 获取第三方应用凭证url
        String suiteTokenUrl = Constant.THIRD_BUS_WECHAT_SUITE_TOKEN;
        // 	第三方应用access_token
        String suiteToken = "";
        try {
            Map<String, Object> map = new HashMap<>();
            //以ww或wx开头应用id
            map.put("suite_id", Constant.SuiteIDSX);
            //应用secret
            map.put("suite_secret", Constant.SuiteSecretSx);
            //企业微信后台推送的ticket
            map.put("suite_ticket", mapSuite.get("SuiteTicket"));
            String body = HttpRequest.post(suiteTokenUrl).body(JSONUtil.toJsonStr(map), ContentType.JSON.getValue()).execute().body();
            System.out.println("ceshisaveTokenenneenneSSSXXXXXX@@@22222" + body);
            WeChatSuiteReturn weChat = JSONUtil.toBean(body, WeChatSuiteReturn.class);
            if (weChat.getErrcode() == null || weChat.getErrcode() == 0) {
                // 保存suiteToken
                suiteToken = weChat.getSuite_access_token();
                saveWxProperty(Constant.SUITE_TOKEN_SX, suiteToken);
            }
            // 打印消息
            System.out.println("获取suite token成功:" + suiteToken);
        } catch (Exception e) {
            System.out.println("获取suite token失败errcode:" + suiteToken);
            throw new RuntimeException();
        }

    }

    public void saveSuiteTokenRx(Map<String, String> mapSuite) {
        System.out.println("gettokenekenekeneSSSXXXXXXXXX");
        // 获取第三方应用凭证url
        String suiteTokenUrl = Constant.THIRD_BUS_WECHAT_SUITE_TOKEN;
        // 	第三方应用access_token
        String suiteToken = "";
        try {
            Map<String, Object> map = new HashMap<>();
            //以ww或wx开头应用id
            map.put("suite_id", Constant.SuiteIDRX);
            //应用secret
            map.put("suite_secret", Constant.SuiteSecretRx);
            //企业微信后台推送的ticket
            map.put("suite_ticket", mapSuite.get("SuiteTicket"));
            String body = HttpRequest.post(suiteTokenUrl).body(JSONUtil.toJsonStr(map), ContentType.JSON.getValue()).execute().body();
            System.out.println("ceshisaveTokenenneenneSSSXXXXXX@@@22222" + body);
            WeChatSuiteReturn weChat = JSONUtil.toBean(body, WeChatSuiteReturn.class);
            if (weChat.getErrcode() == null || weChat.getErrcode() == 0) {
                // 保存suiteToken
                suiteToken = weChat.getSuite_access_token();
                saveWxProperty(Constant.SUITE_TOKEN_RX, suiteToken);
            }
            // 打印消息
            System.out.println("获取suite token成功:" + suiteToken);
        } catch (Exception e) {
            System.out.println("获取suite token失败errcode:" + suiteToken);
            throw new RuntimeException();
        }

    }

    private String saveProviderAccessToken() {
        String suteRul = "https://qyapi.weixin.qq.com/cgi-bin/service/get_provider_token";

        Map<String, Object> map = new HashMap<>();
        //以ww或wx开头应用id
        map.put("corpid", Constant.CorpID);
        //应用secret
        map.put("provider_secret", Constant.ProviderSecret);
        System.out.println("kankanmappppapappa-----------------" + map);
        String body = HttpRequest.post(suteRul).body(JSONUtil.toJsonStr(map), ContentType.JSON.getValue()).execute().body();
        System.out.println("provider_access_token body===" + body);
        JSONObject jsonObject = JSONObject.parseObject(body);
        String provider_access_token = jsonObject.getString("provider_access_token");
        saveWxProperty("provider_access_token", provider_access_token);
        return provider_access_token;
    }

    private String transferCorpId(String s) {
        System.out.println("transferCorpIdtransferCorpId");

        String transferRul = "https://qyapi.weixin.qq.com/cgi-bin/service/corpid_to_opencorpid?provider_access_token=" + s;
        System.out.println("transferRultransferRul============" + transferRul);
        Map<String, Object> map = new HashMap<>();
        //以ww或wx开头应用id
        map.put("corpid", Constant.CORPIDDjTRS);
        String body = HttpRequest.post(transferRul).body(JSONUtil.toJsonStr(map), ContentType.JSON.getValue()).execute().body();
        JSONObject jsonObject = JSONObject.parseObject(body);
        String open_corpid = jsonObject.getString("open_corpid");
        saveWxProperty("open_corpid", open_corpid);
        return open_corpid;

    }


    private void saveWxProperty(String key, String value) {
        System.out.println("savekkekekeykey====" + key + "value==" + value);
        PropertiesConfiguration configuration = null;
        try {
            configuration = new PropertiesConfiguration("wx.properties");
            configuration.setProperty(key, value);
            configuration.save();
            System.out.println("condarrrr" + configuration.getKeys());
            System.out.println("saveWxProperty---------------" + key + "===" + configuration.getString(key));
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

    }

    private String getWxProperty(String key) {
        Properties pps = new Properties();
        InputStream is = QyWxControlloer.class.getClassLoader().getResourceAsStream("wx.properties");//假设当前这个方法是在CommonUtils类下面
        try {
            pps.load(is);
            String value = pps.getProperty(key);
            System.out.println(key + " = " + value);
            is.close();
            System.out.println("getWxProperty---------------" + key + "===" + pps.get(key));
            return value;

        } catch (IOException e) {
            e.printStackTrace();
            return "-1";
        }

    }


}
