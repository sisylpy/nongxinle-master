package com.nongxinle.controller;

/**
 * @author lpy
 * @date 10-11 17:01
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import com.github.wxpay.sdk.WXPay;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.nongxinle.service.NxDistributerGoodsShelfStockReduceService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbOrderBuyStatusHavePayFinish;
import static com.nongxinle.utils.NxDistributerTypeUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDisPayListPrinter;
import static com.nongxinle.utils.ParseObject.myRandom;
import static com.nongxinle.utils.PinYin4jUtils.getHeadStringByString;


@RestController
@RequestMapping("api/nxdepartmentbill")
public class NxDepartmentBillController {
    @Autowired
    private NxDepartmentBillService nxDepartmentBillService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private NxDepartmentOrderHistoryService historyService;
    @Autowired
    private NxDepartmentDisGoodsService nxDepartmentDisGoodsService;
    @Autowired
    private NxDepartmentService nxDepartmentService;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private GbDepartmentBillService gbDepartmentBillService;
    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;
    @Autowired
    private NxDistributerGbDistributerService nxDisGbDisService;
    @Autowired
    private NxDistributerGoodsService dgService;
    @Autowired
    private NxDistributerFatherGoodsService distributerFatherGoodsService;
    @Autowired
    private NxDistributerPurchaseGoodsService nxDistributerPurchaseGoodsService;
    @Autowired
    private GbDistributerPurchaseBatchService gbDisPurBatchService;

    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
    @Autowired
    private GbDepartmentGoodsStockService gbDepartmentGoodsStockService;
    @Autowired
    private GbDistributerGoodsPriceService goodsPriceService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private GbDistributerFatherGoodsService gbDistributerFatherGoodsService;
    @Autowired
    private GbDistributerPurchaseGoodsService gbDistributerPurchaseGoodsService;
    @Autowired
    private GbDepartmentGoodsDailyService gbDepGoodsDailyService;
    @Autowired
    private NxDistributerPayListService payListService;
    @Autowired
    private GbDepartmentGoodsStockReduceService gbDepartmentStockReduceService;
    @Autowired
    private GbDistributerPurchaseGoodsService gbDPGService;
    @Autowired
    private NxGbDistibuterUserCouponService nxGbDistibuterUserCouponService;
    @Autowired
    private GbDistributerService gbDistributerService;
    @Autowired
    private GbDistributerStandardService gbDistributerStandardService;
    @Autowired
    private NxMachinePrinterDeviceService nxMachinePrinterDeviceService;
    @Autowired
    private NxMachinePrintRecordService nxMachinePrintRecordService;
    @Autowired
    private PrinterAlertService printerAlertService;
    @Autowired
    private NxDistributerGoodsShelfStockService nxDistributerGoodsShelfStockService;
    @Autowired
    private NxDistributerGoodsShelfStockReduceService nxDistributerGoodsShelfStockReduceService;

    private static final Logger logger = LoggerFactory.getLogger(NxDepartmentBillController.class);

    private static String appKey = "dada43105baba0717e2";
    private static String appSecret = "c36d1c12cb549a911458601c12e221bf";
    private static String sourceId = "73753";
    private static String shopNo = "887737-22356381";


    public static final String URL = "http://api.feieyun.cn/Api/Open/";//不需要修改

    public static final String USER = "454926763@qq.com";//*必填*：账号名
    public static final String UKEY = "bDI55xfWTv5Fu6KU";//*必填*: 飞鹅云后台注册账号后生成的UKEY 【备注：这不是填打印机的KEY】
    public static final String SN = "924610660";//*必填*：打印机编号，必须要在管理后台里添加打印机或调用API接口添加之后，才能调用API


    /**
     * @Title: callBack 达达快递
     * @Description: 支付完成的回调函数
     * @param:
     * @return:
     */
    @RequestMapping("/notifyDada")
    public String callBackDada(String rpcResult) {
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


    @RequestMapping(value = "/testDada")
    @ResponseBody
    public R testDada() {

        String rpcResult = queryOrderDeliver(appKey, appSecret, sourceId);
        // 创建订单
        System.out.println("创建订单结果：" + rpcResult);

        DaDaResponse<?> response = JSON.parseObject(rpcResult, new TypeReference<DaDaResponse<?>>() {
        });
        if (Objects.nonNull(response) && Objects.equals(response.getStatus(), "success")) {
            System.out.println("执行成功，响应结果：{}" + JSON.toJSONString(response.getResult()));
            if (response.getResult() instanceof Map) {
                Map<String, Object> resultMap = (Map<String, Object>) response.getResult();
                Object deliverFee = resultMap.get("deliverFee");
                System.out.println("deliverFee==123" + deliverFee);
            } else {
                System.out.println("result 不是 Map 类型");
            }
        } else {
            System.out.println("执行失败，响应结果：{}" + rpcResult);
        }
        return R.ok();
    }

    private static String queryOrderDeliver(String appKey, String appSecret, String sourceId) {
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
            body.put("receiver_name", "张三");
            body.put("receiver_phone", "13900000000");
            body.put("receiver_address", "上海市浦东新区东方渔人码头");
            body.put("receiver_lng", 116.462282);
            body.put("receiver_lat", 39.890496);
            body.put("callback", "https://grainservice.club:8443/nongxinle/api/nxdepartmentbill/notifyDada");
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

    private static String createOrder(String appKey, String appSecret, String sourceId) {
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
            body.put("receiver_name", "张三");
            body.put("receiver_phone", "13900000000");
            body.put("receiver_address", "上海市浦东新区东方渔人码头");
            body.put("receiver_lng", 116.462282);
            body.put("receiver_lat", 39.890496);
            body.put("callback", "https://grainservice.club:8443/nongxinle/api/nxdepartmentbill/notifyDada");
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

    @Transactional
    @RequestMapping(value = "/saveGbReturn/{id}")
    @ResponseBody
    public R saveGbReturn(@PathVariable Integer id) {
        NxDepartmentBillEntity nxDepartmentBill = nxDepartmentBillService.queryObject(id);
        nxDepartmentBill.setNxDbStatus(0);

        GbDepartmentBillEntity gbDepartmentBill = new GbDepartmentBillEntity();
        gbDepartmentBill.setGbDbIssueNxDisId(nxDepartmentBill.getNxDbDisId());
        gbDepartmentBill.setGbDbDisId(nxDepartmentBill.getNxDbGbDisId());
        gbDepartmentBill.setGbDbDepId(nxDepartmentBill.getNxDbGbDepId());
        gbDepartmentBill.setGbDbTotal(nxDepartmentBill.getNxDbTotal());
        gbDepartmentBill.setGbDbPayTotal(nxDepartmentBill.getNxDbTotal());
        gbDepartmentBill.setGbDbPrintTimes(1);
        gbDepartmentBill.setGbDbIssueOrderType(5);
        gbDepartmentBill.setGbDbOrderAmount(1);
        gbDepartmentBill.setGbDbStatus(0);
        gbDepartmentBill.setGbDbDate(formatWhatDay(0));
        gbDepartmentBill.setGbDbTime(formatWhatYearDayTime(0));
        gbDepartmentBill.setGbDbMonth(formatWhatMonth(0));
        gbDepartmentBill.setGbDbWeek(getWeekOfYear(0).toString());
        gbDepartmentBill.setGbDbDay(getWeek(0));
        gbDepartmentBill.setGbDbTradeNo(nxDepartmentBill.getNxDbTradeNo());
        gbDepartmentBill.setGbDbPayTotal(nxDepartmentBill.getNxDbTotal());
        gbDepartmentBillService.save(gbDepartmentBill);

        nxDepartmentBill.setNxDbGbDepartmentBillId(gbDepartmentBill.getGbDepartmentBillId());
        nxDepartmentBillService.update(nxDepartmentBill);

        Map<String, Object> mapO = new HashMap<>();
        mapO.put("billId", nxDepartmentBill.getNxDepartmentBillId());
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(mapO);
        for (NxDepartmentOrdersEntity nxOrderEntity : ordersEntities) {
            //迁移
            nxDepartmentOrdersService.moveOrderToHistory(nxOrderEntity);  // ✅ 迁移

            Integer nxDoGbDepartmentOrderId = nxOrderEntity.getNxDoGbDepartmentOrderId();

            GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(nxDoGbDepartmentOrderId);
            gbDepartmentOrdersEntity.setGbDoBillId(gbDepartmentBill.getGbDepartmentBillId());
            gbDepartmentOrdersEntity.setGbDoStatus(4);
            gbDepartmentOrdersEntity.setGbDoBuyStatus(5);
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);

            Integer purchaseGoodsId = gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId();
            GbDistributerPurchaseGoodsEntity disPurGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(purchaseGoodsId);
            disPurGoodsEntity.setGbDpgPurchaseDate(formatWhatDay(0));
            disPurGoodsEntity.setGbDpgPurchaseMonth(formatWhatMonth(0));
            disPurGoodsEntity.setGbDpgPurchaseYear(formatWhatYear(0));
            disPurGoodsEntity.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
            disPurGoodsEntity.setGbDpgPurchaseWeek(getWeek(0));
            disPurGoodsEntity.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
            disPurGoodsEntity.setGbDpgBatchId(-1);
            disPurGoodsEntity.setGbDpgPurchaseWeek(getWeek(0));
            disPurGoodsEntity.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
            disPurGoodsEntity.setGbDpgStatus(3);
            gbDistributerPurchaseGoodsService.update(disPurGoodsEntity);

            Integer gbDoDgsrReturnId = gbDepartmentOrdersEntity.getGbDoDgsrReturnId();
            GbDepartmentGoodsStockReduceEntity reduceEntity = gbDepartmentStockReduceService.queryObject(gbDoDgsrReturnId);
            reduceEntity.setGbDgsrStatus(0);
            gbDepartmentStockReduceService.update(reduceEntity);

        }

        Integer nxDbGbDisId = nxDepartmentBill.getNxDbGbDisId();
        Map<String, Object> map = new HashMap<>();
        map.put("type", getGbDepartmentTypeAppSupplier());
        map.put("disId", nxDbGbDisId);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
        gbDepartmentBill.setGbDbIssueDepId(departmentEntities.get(0).getGbDepartmentId());
        gbDepartmentBillService.update(gbDepartmentBill);

        /*
         * 参数说明：
         * - transactionId：微信支付订单号，可选；如果有则优先使用
         * - outTradeNo：商户订单号（支付时的订单号），可选；如果transactionId为空，则必须提供outTradeNo
         * - refundAmount：退款金额（单位：元）
         * - totalAmount：原订单总金额（单位：元）
         *
         * 注意：退款请求中金额（total和refund）单位均为分，所以需要进行换算
         */

        // 生成退款单号（确保唯一，可以参考支付接口中生成商户订单号的方式）
        String outRefundNo = CommonUtils.generateOutTradeNo();

        // 构造退款请求 JSON
        JSONObject reqJson = new JSONObject();

        // 如果提供了微信支付订单号则优先使用
        String outTradeNo = nxDepartmentBill.getNxDbWxOutTradeNo();
        reqJson.put("out_trade_no", outTradeNo);
        GbDepartmentBillEntity gbDepartmentBillEntity = gbDepartmentBillService.queryDepartBillByTradeNo(outTradeNo);

//        String transactionId = null;
//        if (transactionId != null && !transactionId.trim().isEmpty()) {
//            reqJson.put("transaction_id", transactionId);
//        } else if (outTradeNo != null && !outTradeNo.trim().isEmpty()) {
//            reqJson.put("out_trade_no", outTradeNo);
//        } else {
//            return R.error("交易ID和商户订单号不能同时为空");
//        }

        reqJson.put("out_refund_no", outRefundNo);
        // 退款结果异步通知的URL，需要保证该地址可公网访问
        reqJson.put("notify_url", "https://grainservice.club:8443/nongxinle/api/nxdepartmentbill/refundGbBill"); // 请替换为你自己的退款通知地址

        // 构造金额对象（单位：分）
        String refundAmountString = gbDepartmentBillEntity.getGbDbTotal();
        String nxDbTotal = nxDepartmentBill.getNxDbTotal();
        nxDbTotal = nxDbTotal.replace("-", "");
        refundAmountString = refundAmountString.replace("-", "");
        double doubleVal = Double.parseDouble(refundAmountString);          // => 18.0
        double doubleValF = Double.parseDouble(nxDbTotal);          // => 18.0
        int totalFen = (int) Math.round(doubleVal * 100);                   // => 1800
        int funFen = (int) Math.round(doubleValF * 100);                   // => 1800
        JSONObject amount = new JSONObject();
        amount.put("total", totalFen);   // 原订单总金额: 1800 分
        amount.put("refund", funFen);  // 退款金额: 1800 分（全额退款）
        amount.put("currency", "CNY");
        reqJson.put("amount", amount);

        System.out.println("退款请求JSON: " + reqJson.toString());

        try {
            // 微信退款V3接口地址
            String url = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds";

            // 发起退款请求，使用与支付相同的带证书Post请求工具方法
            String response = WxV3UtilLd.sendPostWithCert(url, reqJson.toString());
            System.out.println("微信退款返回内容Ld: " + response);

            // 将响应结果解析成JSON对象（可根据微信返回的实际字段进行处理）
            JSONObject respJson = JSON.parseObject(response);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("refund_id", respJson.getString("refund_id"));
            resultMap.put("out_refund_no", outRefundNo);

            gbDepartmentBill.setGbDbWxOutTradeNo(outRefundNo);
            gbDepartmentBillService.update(gbDepartmentBill);

            // 此处可加入更新订单退款状态的代码
            return R.ok().put("map", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("退款失败");
        }

//        return R.ok();
    }


    @RequestMapping("/refundGbBill")
    public Map<String, String> refundGbBill(HttpServletRequest request) {
        System.out.println("refundGbBillrefundGbBillrefundGbBillrefundGbBillrefundGbBill");
        try {
            // 1. 读取微信回调的 JSON 字符串
            String body = WxV3UtilLd.readRequestBody(request);
            System.out.println("【微信V3退款回调】收到的原始JSON: " + body);

            // 2. 验签（强烈建议一定要做！），此处只是示例调用
            //    你需要提供正确的微信平台证书内容（platformCertificate）
            //    或者先在 WxV3UtilLd 里真正实现 verifySignature 逻辑
            boolean verified = WxV3UtilLd.verifySignature(body, request, "YOUR_PLATFORM_CERT_CONTENT");
            if (!verified) {
                System.out.println("【微信V3退款回调】签名验签失败！");
                return WxV3UtilLd.errorReturn("Signature verification failed");
            }

            // 3. 解析 JSON
            JSONObject jsonObj = JSON.parseObject(body);

            // 3.1 可判断一下 event_type，通常是 REFUND.SUCCESS / REFUND.ABNORMAL / REFUND.CLOSED 等
            String eventType = jsonObj.getString("event_type");
            System.out.println("【微信V3退款回调】event_type = " + eventType);

            // 3.2 拿到 resource 对象
            JSONObject resource = jsonObj.getJSONObject("resource");
            if (resource == null) {
                System.out.println("【微信V3退款回调】resource 字段为空，无法解密退款信息");
                return WxV3UtilLd.errorReturn("Resource is null");
            }
            String ciphertext = resource.getString("ciphertext");
            String nonce = resource.getString("nonce");
            String associatedData = resource.getString("associated_data");

            // 4. 使用 APIv3Key 解密 ciphertext 得到退款明文信息
            //    注意：一定要保证与商户平台上的 APIv3 Key 一致
            String apiV3Key = "sisy112578sisy112578sisy112578cf";
            String plainText = WxV3UtilLd.aesGcmDecrypt(apiV3Key, associatedData, nonce, ciphertext);
            System.out.println("【微信V3退款回调】解密得到的退款信息明文: " + plainText);

            // 5. 解析退款明文 JSON，根据字段做业务处理
            JSONObject refundInfo = JSON.parseObject(plainText);

            // 微信退款通知里会包含 out_refund_no、refund_id、refund_status 等
            String outRefundNo = refundInfo.getString("out_refund_no");
            String refundId = refundInfo.getString("refund_id");
            String refundStatus = refundInfo.getString("refund_status"); // SUCCESS、CLOSED等

            System.out.println("【微信V3退款回调】out_refund_no = " + outRefundNo
                    + ", refund_id = " + refundId
                    + ", refund_status = " + refundStatus);

            // 6. 如果 refundStatus = "SUCCESS" 表示退款成功，可更新数据库订单状态
            if ("SUCCESS".equalsIgnoreCase(refundStatus)) {

                // =========================
                // 下面是你原先 V2 回调中的业务逻辑，根据 out_refund_no 查询订单并更新
                // 例如：

                GbDepartmentBillEntity gbDepartmentBillEntity = gbDepartmentBillService.queryDepartBillByTradeNo(outRefundNo);
                gbDepartmentBillEntity.setGbDbStatus(3);
                gbDepartmentBillService.update(gbDepartmentBillEntity);

                Map<String, Object> mapGO = new HashMap<>();
                mapGO.put("billId", gbDepartmentBillEntity.getGbDepartmentBillId());
                List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(mapGO);
                if (gbDepartmentOrdersEntities.size() > 0) {
                    for (GbDepartmentOrdersEntity gbDepartmentOrdersEntity : gbDepartmentOrdersEntities) {
                        gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderBuyStatusHavePayFinish());
                        gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
                    }
                }

                NxDepartmentBillEntity nxDepartmentBill = nxDepartmentBillService.queryItemByGbDepBillId(gbDepartmentBillEntity.getGbDepartmentBillId());
                nxDepartmentBill.setNxDbStatus(4);
                nxDepartmentBillService.update(nxDepartmentBill);

                Map<String, Object> map = new HashMap<>();
                map.put("billId", nxDepartmentBill.getNxDepartmentBillId());
                List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
                if (ordersEntities.size() > 0) {
                    for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                        ordersEntity.setNxDoStatus(4);
                        nxDepartmentOrdersService.update(ordersEntity);
                    }
                }
            }

            // 7. 回应微信：收到通知，处理成功（注意 v3 必须返回 JSON）
            Map<String, String> successResp = new HashMap<>();
            successResp.put("code", "SUCCESS");
            successResp.put("message", "成功");
            return successResp;

        } catch (Exception e) {
            e.printStackTrace();
            // 如果解析或处理失败，返回错误信息
            return WxV3UtilLd.errorReturn("Exception: " + e.getMessage());
        }
    }


    private void nxDisWaitingGbReceive(NxDepartmentBillEntity nxDepartmentBill, GbDepartmentBillEntity gbDepartmentBillEntity) {

        System.out.println("savesonnsuusbsilllGGGBBB" + nxDepartmentBill);
        BigDecimal billTotal = new BigDecimal(0);
        Map<String, Object> map = new HashMap<>();
        map.put("gbDepFatherId", nxDepartmentBill.getNxDbGbDepFatherId());
        if (!nxDepartmentBill.getNxDbGbDepFatherId().equals(nxDepartmentBill.getNxDbGbDepId())) {
            map.put("gbDepId", nxDepartmentBill.getNxDbGbDepId());
        }

        map.put("status", 3);
//        map.put("equalPurStatus", 4);
        map.put("orderDisId", nxDepartmentBill.getNxDbDisId());
        System.out.println("nxoroossoss" + map);

        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        System.out.println("nxoroossoss" + ordersEntities.size());

        int totalAuto1 = 0;
        for (NxDepartmentOrdersEntity orders : ordersEntities) {
            //0 subtotal
            billTotal = billTotal.add(new BigDecimal(orders.getNxDoSubtotal()));
            orders.setNxDoStatus(3);
            orders.setNxDoPurchaseStatus(getNxDisPurchaseGoodsFinishPay());
            orders.setNxDoBillId(nxDepartmentBill.getNxDepartmentBillId());
            nxDepartmentOrdersService.update(orders);

            Integer nxDoPurchaseGoodsId = orders.getNxDoPurchaseGoodsId();
            if (nxDoPurchaseGoodsId != -1) {
                NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
                    purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                    purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());

                } else {
                    purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
                }
                nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
            }


            //todo

            System.out.println("depdidiisis" + orders.getNxDoDepDisGoodsId());
            Map<String, Object> mapDep = new HashMap<>();
            mapDep.put("disGoodsId", orders.getNxDoDisGoodsId());
            mapDep.put("gbDepId", orders.getNxDoGbDepartmentId());
            System.out.println("mapdee" + mapDep);
            NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(mapDep);
            if (departmentDisGoodsEntity == null) {
                System.out.println("new Depdiiddid");
                NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(orders.getNxDoDisGoodsId());
                String nxDgGoodsName = nxDistributerGoodsEntity.getNxDgGoodsName();
                NxDepartmentDisGoodsEntity disGoodsEntity = new NxDepartmentDisGoodsEntity();
                disGoodsEntity.setNxDdgDepGoodsName(nxDgGoodsName);
                disGoodsEntity.setNxDdgDisGoodsId(orders.getNxDoDisGoodsId());
                disGoodsEntity.setNxDdgDisGoodsFatherId(nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId());
                disGoodsEntity.setNxDdgDisGoodsGrandId(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());

                NxDistributerFatherGoodsEntity fatherGoodsEntity = distributerFatherGoodsService.queryObject(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
                Integer greatFatherId = fatherGoodsEntity.getNxDfgFathersFatherId();
                disGoodsEntity.setNxDdgDisGoodsGreatId(greatFatherId);

                disGoodsEntity.setNxDdgDepGoodsPinyin(nxDistributerGoodsEntity.getNxDgGoodsPinyin());
                disGoodsEntity.setNxDdgDepGoodsPy(nxDistributerGoodsEntity.getNxDgGoodsPy());
                disGoodsEntity.setNxDdgDepGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
                disGoodsEntity.setNxDdgDepartmentId(orders.getNxDoDepartmentId());
                disGoodsEntity.setNxDdgDepartmentFatherId(orders.getNxDoDepartmentFatherId());
                disGoodsEntity.setNxDdgGbDepartmentFatherId(orders.getNxDoGbDepartmentFatherId());
                disGoodsEntity.setNxDdgGbDepartmentId(orders.getNxDoGbDepartmentId());
                disGoodsEntity.setNxDdgNxDistributerId(orders.getNxDoDistributerId());
                disGoodsEntity.setNxDdgIsGbDepartment(1);
                //orderData
                disGoodsEntity.setNxDdgOrderPrice(orders.getNxDoPrice());
                disGoodsEntity.setNxDdgOrderCostPrice(orders.getNxDoCostPrice());
                disGoodsEntity.setNxDdgOrderQuantity(orders.getNxDoQuantity());
                disGoodsEntity.setNxDdgOrderRemark(orders.getNxDoRemark());
                disGoodsEntity.setNxDdgOrderStandard(orders.getNxDoStandard());
                disGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
                disGoodsEntity.setNxDdgGbDistributerId(orders.getNxDoGbDistributerId());
                disGoodsEntity.setNxDdgGoodsNxDistributerId(nxDistributerGoodsEntity.getNxDgDistributerId());
                if (orders.getNxDoGoodsOriginalName() != null) {
                    disGoodsEntity.setNxDdgOrderGoodsName(orders.getNxDoGoodsOriginalName());
                }
                disGoodsEntity.setNxDdgOrderPriceLevel(orders.getNxDoCostPriceLevel());
                nxDepartmentDisGoodsService.save(disGoodsEntity);
                orders.setNxDoDepDisGoodsId(disGoodsEntity.getNxDepartmentDisGoodsId());

            } else {

                departmentDisGoodsEntity.setNxDdgOrderPrice(orders.getNxDoPrice());
                departmentDisGoodsEntity.setNxDdgOrderCostPrice(orders.getNxDoCostPrice());
                departmentDisGoodsEntity.setNxDdgOrderQuantity(orders.getNxDoQuantity());
                departmentDisGoodsEntity.setNxDdgOrderRemark(orders.getNxDoRemark());
                departmentDisGoodsEntity.setNxDdgOrderStandard(orders.getNxDoStandard());
                departmentDisGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
                if (orders.getNxDoGoodsName() != null) {
                    departmentDisGoodsEntity.setNxDdgOrderGoodsName(orders.getNxDoGoodsName());
                }
                departmentDisGoodsEntity.setNxDdgOrderPriceLevel(orders.getNxDoCostPriceLevel());

                nxDepartmentDisGoodsService.update(departmentDisGoodsEntity);

            }


            GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(orders.getNxDoGbDepartmentOrderId());
            gbDepartmentOrdersEntity.setGbDoPrice(orders.getNxDoPrice());
            gbDepartmentOrdersEntity.setGbDoPriceDifferent(orders.getNxDoPriceDifferent());
            NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(orders.getNxDoDisGoodsId());
            gbDepartmentOrdersEntity.setGbDoWeight(orders.getNxDoWeight());

            if (orders.getNxDoCostPriceLevel().equals("2")) {
                Map<String, Object> mapGS = new HashMap<>();
                mapGS.put("disGoodsId", gbDepartmentOrdersEntity.getGbDoDisGoodsId());
                mapGS.put("standardName", gbDepartmentOrdersEntity.getGbDoStandard());
                List<GbDistributerStandardEntity> gbDistributerStandardEntities = gbDistributerStandardService.queryDisStandardByParams(mapGS);
                if (gbDistributerStandardEntities.size() == 0) {
                    GbDistributerStandardEntity gbDistributerStandardEntity = new GbDistributerStandardEntity();
                    gbDistributerStandardEntity.setGbDsDisGoodsId(gbDepartmentOrdersEntity.getGbDoDisGoodsId());
                    gbDistributerStandardEntity.setGbDsStandardName(gbDepartmentOrdersEntity.getGbDoStandard());
                    gbDistributerStandardEntity.setGbDsStandardWeight(distributerGoodsEntity.getNxDgGoodsStandardWeight());
                    gbDistributerStandardEntity.setGbDsStandardSort(gbDistributerStandardEntities.size());
                    gbDistributerStandardEntity.setGbDsStandardScale(distributerGoodsEntity.getNxDgWillPriceTwoWeight());
                    System.out.println("newSavememstantnt" + gbDistributerStandardEntity);
                    gbDistributerStandardService.save(gbDistributerStandardEntity);
                }
            }

            gbDepartmentOrdersEntity.setGbDoSubtotal(orders.getNxDoSubtotal());
            gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderBuyStatusHasFinishPurGoods());
            gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusHasFinished());
            gbDepartmentOrdersEntity.setGbDoBillId(gbDepartmentBillEntity.getGbDepartmentBillId());
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);

            Integer gbDoPurchaseGoodsId = gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId();
            if (gbDoPurchaseGoodsId != -1) {
                GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(gbDoPurchaseGoodsId);
                System.out.println("quayeyyeweieiidiid");
               double quantityTotal =  gbDepartmentOrdersService.queryOrderWeightTotalByPurGoodsId(gbDoPurchaseGoodsId);
                BigDecimal price = new BigDecimal(orders.getNxDoPrice());
                BigDecimal bigDecimal = BigDecimal.valueOf(quantityTotal).multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
                purchaseGoodsEntity.setGbDpgBuyPrice(orders.getNxDoPrice());
                purchaseGoodsEntity.setGbDpgBuyQuantity(String.valueOf(quantityTotal));
                purchaseGoodsEntity.setGbDpgBuySubtotal(bigDecimal.toString());

                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
            }


//            Integer gbDoDisGoodsId = gbDepartmentOrdersEntity.getGbDoDisGoodsId();
//            GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
//            System.out.println("gbdigeenne" + gbDistributerGoodsEntity.getGbDgNxDistributerGoodsId());
//            System.out.println("gbdigeenne" + orders.getNxDoDisGoodsId());
//            if (gbDistributerGoodsEntity.getGbDgNxDistributerGoodsId().equals(orders.getNxDoDisGoodsId())) {
//                totalAuto = totalAuto + 1;
//            }


            //updata weight
            BigDecimal weight = new BigDecimal(distributerGoodsEntity.getNxDgOutTotalWeight());
            BigDecimal orderWeight = new BigDecimal(orders.getNxDoWeight());
            BigDecimal add = weight.add(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            distributerGoodsEntity.setNxDgOutTotalWeight(add.toString());
            dgService.update(distributerGoodsEntity);


            //迁移
            System.out.println("gborororororoorqinayiqianyia");
            nxDepartmentOrdersService.moveOrderToHistory(orders);


        }

        nxDepartmentBill.setNxDbStatus(0);
        nxDepartmentBill.setNxDbTotal(billTotal.toString());
        nxDepartmentBillService.update(nxDepartmentBill);

//        System.out.println("autotototo0011111111" + totalAuto);
//        if (totalAuto == ordersEntities.size()) {
//            gbDepartmentBillEntity.setGbDbSetAutoGoods(1);
//        } else {
//            gbDepartmentBillEntity.setGbDbSetAutoGoods(0);
//        }
//        gbDepartmentBillService.update(gbDepartmentBillEntity);


    }


    private void nxDisWaitingGbReceiveAndPrint(NxDepartmentBillEntity nxDepartmentBill, GbDepartmentBillEntity gbDepartmentBillEntity) {

        System.out.println("savesonnsuusbsilllGGGBBB" + nxDepartmentBill);
        BigDecimal billTotal = new BigDecimal(0);
        Map<String, Object> map = new HashMap<>();
        map.put("gbDepFatherId", nxDepartmentBill.getNxDbGbDepFatherId());
        if (!nxDepartmentBill.getNxDbGbDepFatherId().equals(nxDepartmentBill.getNxDbGbDepId())) {
            map.put("gbDepId", nxDepartmentBill.getNxDbGbDepId());
        }

        map.put("status", 3);
//        map.put("equalPurStatus", 4);
        map.put("orderDisId", nxDepartmentBill.getNxDbDisId());

        System.out.println("nxoroossoss" + map);

        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        System.out.println("nxoroossoss" + ordersEntities.size());

        int totalAuto = 0;
        for (NxDepartmentOrdersEntity orders : ordersEntities) {
            //0 subtotal
            billTotal = billTotal.add(new BigDecimal(orders.getNxDoSubtotal()));
            orders.setNxDoStatus(3);
            orders.setNxDoPurchaseStatus(getNxDisPurchaseGoodsFinishPay());
            orders.setNxDoBillId(nxDepartmentBill.getNxDepartmentBillId());
            nxDepartmentOrdersService.update(orders);

            nxDepartmentOrdersService.moveOrderToHistory(orders);
            //todo

            NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(orders.getNxDoDisGoodsId());

            System.out.println("depdidiisis" + orders.getNxDoDepDisGoodsId());
            Map<String, Object> mapDep = new HashMap<>();
            mapDep.put("disGoodsId", orders.getNxDoDisGoodsId());
            mapDep.put("gbDepId", orders.getNxDoGbDepartmentId());
            System.out.println("mapdee" + mapDep);
            NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(mapDep);
            if (departmentDisGoodsEntity == null) {
                System.out.println("new Depdiiddid");
                String nxDgGoodsName = nxDistributerGoodsEntity.getNxDgGoodsName();
                NxDepartmentDisGoodsEntity disGoodsEntity = new NxDepartmentDisGoodsEntity();
                disGoodsEntity.setNxDdgDepGoodsName(nxDgGoodsName);
                disGoodsEntity.setNxDdgDisGoodsId(orders.getNxDoDisGoodsId());
                disGoodsEntity.setNxDdgDisGoodsFatherId(nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId());
                disGoodsEntity.setNxDdgDisGoodsGrandId(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());

                NxDistributerFatherGoodsEntity fatherGoodsEntity = distributerFatherGoodsService.queryObject(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
                Integer greatFatherId = fatherGoodsEntity.getNxDfgFathersFatherId();
                disGoodsEntity.setNxDdgDisGoodsGreatId(greatFatherId);

                disGoodsEntity.setNxDdgDepGoodsPinyin(nxDistributerGoodsEntity.getNxDgGoodsPinyin());
                disGoodsEntity.setNxDdgDepGoodsPy(nxDistributerGoodsEntity.getNxDgGoodsPy());
                disGoodsEntity.setNxDdgDepGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
                disGoodsEntity.setNxDdgDepartmentId(orders.getNxDoDepartmentId());
                disGoodsEntity.setNxDdgDepartmentFatherId(orders.getNxDoDepartmentFatherId());
                disGoodsEntity.setNxDdgGbDepartmentFatherId(orders.getNxDoGbDepartmentFatherId());
                disGoodsEntity.setNxDdgGbDepartmentId(orders.getNxDoGbDepartmentId());
                disGoodsEntity.setNxDdgNxDistributerId(orders.getNxDoDistributerId());
                disGoodsEntity.setNxDdgIsGbDepartment(1);
                //orderData
                disGoodsEntity.setNxDdgOrderPrice(orders.getNxDoPrice());
                disGoodsEntity.setNxDdgOrderCostPrice(orders.getNxDoCostPrice());
                disGoodsEntity.setNxDdgOrderQuantity(orders.getNxDoQuantity());
                disGoodsEntity.setNxDdgOrderRemark(orders.getNxDoRemark());
                disGoodsEntity.setNxDdgOrderStandard(orders.getNxDoStandard());
                disGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
                disGoodsEntity.setNxDdgGbDistributerId(orders.getNxDoGbDistributerId());
                disGoodsEntity.setNxDdgGoodsNxDistributerId(nxDistributerGoodsEntity.getNxDgDistributerId());
                if (orders.getNxDoGoodsName() != null) {
                    disGoodsEntity.setNxDdgOrderGoodsName(orders.getNxDoGoodsName());
                }
                disGoodsEntity.setNxDdgOrderPriceLevel(orders.getNxDoCostPriceLevel());
                nxDepartmentDisGoodsService.save(disGoodsEntity);
                orders.setNxDoDepDisGoodsId(disGoodsEntity.getNxDepartmentDisGoodsId());

            } else {

                departmentDisGoodsEntity.setNxDdgOrderPrice(orders.getNxDoPrice());
                departmentDisGoodsEntity.setNxDdgOrderCostPrice(orders.getNxDoCostPrice());
                departmentDisGoodsEntity.setNxDdgOrderQuantity(orders.getNxDoQuantity());
                departmentDisGoodsEntity.setNxDdgOrderRemark(orders.getNxDoRemark());
                departmentDisGoodsEntity.setNxDdgOrderStandard(orders.getNxDoStandard());
                departmentDisGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
                if (orders.getNxDoGoodsOriginalName() != null) {
                    departmentDisGoodsEntity.setNxDdgOrderGoodsName(orders.getNxDoGoodsOriginalName());
                }
                departmentDisGoodsEntity.setNxDdgOrderPriceLevel(orders.getNxDoCostPriceLevel());
                nxDepartmentDisGoodsService.update(departmentDisGoodsEntity);

            }

            GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(orders.getNxDoGbDepartmentOrderId());
            gbDepartmentOrdersEntity.setGbDoPrice(orders.getNxDoPrice());
            gbDepartmentOrdersEntity.setGbDoPriceDifferent(orders.getNxDoPriceDifferent());
            System.out.println("wokkksiissiisgetNxDoCostPriceLevelgetNxDoCostPriceLevel" + orders.getNxDoCostPriceLevel());
            if (orders.getNxDoCostPriceLevel().equals("2")) {
                Map<String, Object> mapGS = new HashMap<>();
                mapGS.put("disGoodsId", gbDepartmentOrdersEntity.getGbDoDisGoodsId());
                mapGS.put("standardName", gbDepartmentOrdersEntity.getGbDoStandard());
                List<GbDistributerStandardEntity> gbDistributerStandardEntities = gbDistributerStandardService.queryDisStandardByParams(mapGS);
                if (gbDistributerStandardEntities.size() == 0) {
                    GbDistributerStandardEntity gbDistributerStandardEntity = new GbDistributerStandardEntity();
                    gbDistributerStandardEntity.setGbDsDisGoodsId(gbDepartmentOrdersEntity.getGbDoDisGoodsId());
                    gbDistributerStandardEntity.setGbDsStandardName(gbDepartmentOrdersEntity.getGbDoStandard());
                    gbDistributerStandardEntity.setGbDsStandardWeight(nxDistributerGoodsEntity.getNxDgGoodsStandardWeight());
                    gbDistributerStandardEntity.setGbDsStandardSort(gbDistributerStandardEntities.size());
                    gbDistributerStandardEntity.setGbDsStandardScale(nxDistributerGoodsEntity.getNxDgWillPriceTwoWeight());
                    System.out.println("newSavememstantnt" + gbDistributerStandardEntity);
                    gbDistributerStandardService.save(gbDistributerStandardEntity);
                }

            } else {
                gbDepartmentOrdersEntity.setGbDoWeight(orders.getNxDoWeight());
            }

            gbDepartmentOrdersEntity.setGbDoSubtotal(orders.getNxDoSubtotal());
            gbDepartmentOrdersEntity.setGbDoBuyStatus(3);
            gbDepartmentOrdersEntity.setGbDoStatus(3);
            gbDepartmentOrdersEntity.setGbDoBillId(gbDepartmentBillEntity.getGbDepartmentBillId());
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);

            Integer gbDoDisGoodsId = gbDepartmentOrdersEntity.getGbDoDisGoodsId();
            GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
            System.out.println("gbdigeenne" + gbDistributerGoodsEntity.getGbDgNxDistributerGoodsId());
            System.out.println("gbdigeenne" + orders.getNxDoDisGoodsId());
            if (gbDistributerGoodsEntity.getGbDgNxDistributerGoodsId().equals(orders.getNxDoDisGoodsId())) {
                totalAuto = totalAuto + 1;
                System.out.println("autotototo0000" + totalAuto);
            }


            //updata weight
            Integer doDisGoodsId = orders.getNxDoDisGoodsId();
            NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
            BigDecimal weight = new BigDecimal(distributerGoodsEntity.getNxDgOutTotalWeight());
            BigDecimal orderWeight = new BigDecimal(orders.getNxDoWeight());
            BigDecimal add = weight.add(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            distributerGoodsEntity.setNxDgOutTotalWeight(add.toString());
            dgService.update(distributerGoodsEntity);

            Integer nxDoPurchaseGoodsId = orders.getNxDoPurchaseGoodsId();
            if (nxDoPurchaseGoodsId != -1) {
                NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
                    purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                    purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
                } else {
                    purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
                }
                nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }

        nxDepartmentBill.setNxDbStatus(0);
        nxDepartmentBill.setNxDbTotal(billTotal.toString());
        nxDepartmentBillService.update(nxDepartmentBill);
        System.out.println("autotototo0011111111" + totalAuto);
        if (totalAuto == ordersEntities.size()) {
            gbDepartmentBillEntity.setGbDbSetAutoGoods(1);
        } else {
            gbDepartmentBillEntity.setGbDbSetAutoGoods(0);
        }
        gbDepartmentBillService.update(gbDepartmentBillEntity);

        printFeiEBillDataGb(nxDepartmentBill);

    }


    private void printFeiEBillDataGb(NxDepartmentBillEntity nxDepartmentBill) {


        System.out.println("gbDepartmentBillEntitygbDepartmentBillEntity!!!!!");
        Integer nxDbGbDisId = nxDepartmentBill.getNxDbGbDisId();
        GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(nxDbGbDisId);
        String gbDistributerName = gbDistributerEntity.getGbDistributerName();
        String content;
        content = "<BR>";
        content = "<BR>";
        content = "<BR>";
        content = "<BR>";
        content = "<BR>";
        content = "<CB>" + gbDistributerName + "</CB><BR>";
        content += "名称　　　　　 单价  数量 金额<BR>";
        content += "--------------------------------<BR>";
        Map<String, Object> map = new HashMap<>();
        map.put("billId", nxDepartmentBill.getNxDepartmentBillId());
        List<NxDepartmentOrderHistoryEntity> ordersEntities = historyService.queryDisHistoryOrdersByParams(map);
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrderHistoryEntity ordersEntity : ordersEntities) {
                Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
                NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
                String nxDgGoodsName = distributerGoodsEntity.getNxDgGoodsName();
                String nxDoPrice = ordersEntity.getNxDoPrice();
                String nxDoWeight = ordersEntity.getNxDoWeight();
                String nxDoSubtotal = ordersEntity.getNxDoSubtotal();
                // 格式化这一行
                content += formatLine(nxDgGoodsName, nxDoPrice, nxDoWeight, nxDoSubtotal) + "<BR>";
                String nxDoRemark = ordersEntity.getNxDoRemark();
                if (nxDoRemark != null && !nxDoRemark.isEmpty()) {
                    content += "备注：" + nxDoRemark + "<BR>";
                }

            }
        }

        Integer nxDbDisId = nxDepartmentBill.getNxDbDisId();
        NxDistributerEntity distributerEntity = nxDistributerService.queryObject(nxDbDisId);
        String nxDistributerShowName = distributerEntity.getNxDistributerShowName();
        String nxDistributerAddress = distributerEntity.getNxDistributerAddress();
        String nxDistributerPhone = distributerEntity.getNxDistributerPhone();

        content += "--------------------------------<BR>";
        content += "供货商：" + nxDistributerShowName + "<BR>";
        content += "合计：" + nxDepartmentBill.getNxDbTotal() + "元<BR>";
        content += "送货地点：" + nxDistributerAddress + "<BR>";
        content += "联系电话：" + nxDistributerPhone + "<BR>";
        content += "送货时间：" + nxDepartmentBill.getNxDbDate() + "<BR>";
//        content += "<QR>http://www.dzist.com</QR>";

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
    }

    private void printFeiEBillData(NxDepartmentBillEntity nxDepartmentBill) {


        System.out.println("printFeiEBillDataprintFeiEBillData!!!!!");
        Integer depId = nxDepartmentBill.getNxDbDepId();
        NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depId);
        String departmentName = departmentEntity.getNxDepartmentName();
        String content;
        content = "<BR>";
        content = "<BR>";
        content = "<BR>";
        content = "<BR>";
        content = "<BR>";
        content = "<CB>" + departmentName + "</CB><BR>";
        content += "名称　　　　　 单价  数量 金额<BR>";
        content += "--------------------------------<BR>";
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryReturnOrdersByBillId(nxDepartmentBill.getNxDepartmentBillId());
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
                NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
                String nxDgGoodsName = distributerGoodsEntity.getNxDgGoodsName();
                String nxDoPrice = ordersEntity.getNxDoPrice();
                String nxDoWeight = ordersEntity.getNxDoWeight();
                String nxDoSubtotal = ordersEntity.getNxDoSubtotal();
                // 格式化这一行
                content += formatLine(nxDgGoodsName, nxDoPrice, nxDoWeight, nxDoSubtotal) + "<BR>";
                String nxDoRemark = ordersEntity.getNxDoRemark();
                if (nxDoRemark != null && !nxDoRemark.isEmpty()) {
                    content += "备注：" + nxDoRemark + "<BR>";
                }

            }
        }

        Integer nxDbDisId = nxDepartmentBill.getNxDbDisId();
        NxDistributerEntity distributerEntity = nxDistributerService.queryObject(nxDbDisId);
        String nxDistributerShowName = distributerEntity.getNxDistributerShowName();
        String nxDistributerAddress = distributerEntity.getNxDistributerAddress();
        String nxDistributerPhone = distributerEntity.getNxDistributerPhone();

        content += "--------------------------------<BR>";
        content += "供货商：" + nxDistributerShowName + "<BR>";
        content += "合计：" + nxDepartmentBill.getNxDbTotal() + "元<BR>";
        content += "送货地点：" + nxDistributerAddress + "<BR>";
        content += "联系电话：" + nxDistributerPhone + "<BR>";
        content += "送货时间：" + nxDepartmentBill.getNxDbDate() + "<BR>";
//        content += "<QR>http://www.dzist.com</QR>";

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
    }

    public static String formatLine(String name, String price, String weight, String subtotal) {
        int nameWidth = 16;
        int priceWidth = 5;
        int weightWidth = 5;
        int subtotalWidth = 6;

        StringBuilder line = new StringBuilder();

        // 名称字段
        line.append(padRight(name, nameWidth));
        line.append(padRight(price, priceWidth));
        line.append(padRight(weight, weightWidth));
        line.append(padRight(subtotal, subtotalWidth));

        return line.toString();
    }

    // 按字节长度补空格（注意汉字是2个宽度）
    public static String padRight(String str, int totalByteLength) {
        int len = getDisplayLength(str);
        StringBuilder sb = new StringBuilder(str);
        while (len < totalByteLength) {
            sb.append(" ");
            len++;
        }
        return sb.toString();
    }

    public static int getDisplayLength(String str) {
        int length = 0;
        for (char c : str.toCharArray()) {
            // 中文字符
            if (String.valueOf(c).matches("[\u4e00-\u9fa5]")) {
                length += 2;
            } else {
                length += 1;
            }
        }
        return length;
    }

    public R saveDepStockDataByPurchase(@RequestBody GbDepartmentOrdersEntity order) {
        System.out.println("upddodididufidfuaisf");
        Integer gbDepartmentOrdersId = order.getGbDepartmentOrdersId();
        Integer gbDoDepDisGoodsId = order.getGbDoDepDisGoodsId();
        GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(gbDoDepDisGoodsId);

        GbDepartmentOrdersEntity ordersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrdersId);
        Integer gbDoStatus = ordersEntity.getGbDoStatus();
        //0,修改订单上次价格涨幅
        if (departmentDisGoodsEntity.getGbDdgOrderDate() != null) {
            if (order.getGbDoPrice() != null) {
                BigDecimal decimal = new BigDecimal(departmentDisGoodsEntity.getGbDdgOrderPrice());
                BigDecimal decimal1 = new BigDecimal(order.getGbDoPrice());
                BigDecimal subtract1 = decimal1.subtract(decimal);
                order.setGbDoPriceDifferent(subtract1.toString());
            } else {
                order.setGbDoPriceDifferent("0");
            }
        }


        GbDepartmentGoodsStockEntity stockEntity = new GbDepartmentGoodsStockEntity();
        stockEntity.setGbDgsGbDepartmentId(order.getGbDoDepartmentId());
        stockEntity.setGbDgsGbDepartmentFatherId(order.getGbDoDepartmentFatherId());
        stockEntity.setGbDgsGbPurGoodsId(order.getGbDoPurchaseGoodsId());
        stockEntity.setGbDgsGbDistributerId(order.getGbDoDistributerId());
        stockEntity.setGbDgsWeight(order.getGbDoWeight());
        stockEntity.setGbDgsPrice(order.getGbDoPrice());
        stockEntity.setGbDgsSubtotal(order.getGbDoSubtotal());
        stockEntity.setGbDgsRestWeight(order.getGbDoWeight());
        stockEntity.setGbDgsRestSubtotal(order.getGbDoSubtotal());
        stockEntity.setGbDgsGbDisGoodsId(order.getGbDoDisGoodsId());

        Integer gbDoDisGoodsId = order.getGbDoDisGoodsId();
        GbDistributerGoodsEntity goodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
        stockEntity.setGbDgsGbDisGoodsFatherId(goodsEntity.getGbDgDfgGoodsFatherId());
        stockEntity.setGbDgsGbDisGoodsGrandId(goodsEntity.getGbDgDfgGoodsGrandId());
        stockEntity.setGbDgsGbDisGoodsGreatId(goodsEntity.getGbDgDfgGoodsGreatId());
        stockEntity.setGbDgsGbDepDisGoodsId(order.getGbDoDepDisGoodsId());
        stockEntity.setGbDgsDate(formatWhatDay(0));
        stockEntity.setGbDgsTimeStamp(getTimeStamp());
        stockEntity.setGbDgsWeek(getWeek(0));
        stockEntity.setGbDgsMonth(formatWhatMonth(0));
        stockEntity.setGbDgsYear(formatWhatYear(0));
        stockEntity.setGbDgsFullTime(formatFullTime());
        stockEntity.setGbDgsLossWeight("0");
        stockEntity.setGbDgsLossSubtotal("0");
        stockEntity.setGbDgsReturnWeight("0");
        stockEntity.setGbDgsReturnSubtotal("0");
        stockEntity.setGbDgsProduceWeight("0");
        stockEntity.setGbDgsProduceSubtotal("0");
        stockEntity.setGbDgsWasteWeight("0");
        stockEntity.setGbDgsWasteSubtotal("0");
        stockEntity.setGbDgsNxSupplierId(-1);
        String gbDdgSellingPrice = departmentDisGoodsEntity.getGbDdgSellingPrice();
        if (gbDdgSellingPrice != null && new BigDecimal(gbDdgSellingPrice).compareTo(new BigDecimal(0)) == 1) {
            stockEntity.setGbDgsAfterProfitSubtotal("0");
            stockEntity.setGbDgsBetweenPrice("0");
            stockEntity.setGbDgsCostRate("0");
            stockEntity.setGbDgsSellingSubtotal("0");
            stockEntity.setGbDgsProduceSellingSubtotal("0");
            stockEntity.setGbDgsProfitSubtotal("0");
            stockEntity.setGbDgsProfitWeight("0");
            stockEntity.setGbDgsSellingPrice(gbDdgSellingPrice);
        } else {
            stockEntity.setGbDgsSellingPrice("-1");
        }

        // showStandard
        if (departmentDisGoodsEntity.getGbDdgShowStandardId() != -1) {
            String gbDdgShowStandardScale = departmentDisGoodsEntity.getGbDdgShowStandardScale();
            BigDecimal divide = new BigDecimal(order.getGbDoWeight()).divide(new BigDecimal(gbDdgShowStandardScale), 1, BigDecimal.ROUND_HALF_UP);
            stockEntity.setGbDgsRestWeightShowStandard(divide.toString());
            stockEntity.setGbDgsRestWeightShowStandardName(departmentDisGoodsEntity.getGbDdgShowStandardName());
        }

        //判断是否有保鲜时间参数

        System.out.println("purgepfenennee" + goodsEntity.getGbDgControlFresh());
        if (goodsEntity.getGbDgControlFresh() != null && goodsEntity.getGbDgControlFresh() == 1) {
            int wasteHour = Integer.parseInt(goodsEntity.getGbDgFreshWasteHour());
            Integer purchaseGoodsId = order.getGbDoPurchaseGoodsId();
            GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(purchaseGoodsId);
            purchaseGoodsEntity.setGbDpgWasteFullTime(formatWhatFullTime(wasteHour));
            gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

            stockEntity.setGbDgsWasteFullTime(purchaseGoodsEntity.getGbDpgWasteFullTime());
            String gbDpgWasteFullTime = purchaseGoodsEntity.getGbDpgWasteFullTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            // 设置日期字符串
            // 解析日期字符串为Date对象
            Date dateWaste = null;
            try {
                if (gbDpgWasteFullTime != null && !gbDpgWasteFullTime.trim().isEmpty()) {
                    dateWaste = dateFormat.parse(gbDpgWasteFullTime);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
            // 获取时间戳
            long timestampWaste = 0;
            long timestampWarn = 0;
            if (dateWaste != null) {
                timestampWaste = dateWaste.getTime();
            }

            // 输出时间戳
            stockEntity.setGbDgsWasteTimeQuantumName(String.valueOf(timestampWaste));
//            }
            //判断是否价格异常商品
            if (purchaseGoodsEntity.getGbDpgDisGoodsPriceId() != null) {
                GbDistributerGoodsPriceEntity goodsPriceEntity = goodsPriceService.queryObject(purchaseGoodsEntity.getGbDpgDisGoodsPriceId());
                String doWeight = order.getGbDoWeight();
                Integer gbDgpPurWhat = goodsPriceEntity.getGbDgpPurWhat();
                String whatSubtotal = "";
                if (gbDgpPurWhat == 1) {
                    String gbDgpGoodsHighestPrice = goodsPriceEntity.getGbDgpGoodsHighestPrice();
                    String purPrice = goodsPriceEntity.getGbDgpPurPrice();
                    BigDecimal diffPrice = new BigDecimal(purPrice).subtract(new BigDecimal(gbDgpGoodsHighestPrice));
                    BigDecimal subtotal = diffPrice.multiply(new BigDecimal(doWeight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    whatSubtotal = subtotal.toString();
                }
                if (gbDgpPurWhat == -1) {
                    String lowestPrice = goodsPriceEntity.getGbDgpGoodsLowestPrice();
                    String purPrice = goodsPriceEntity.getGbDgpPurPrice();
                    BigDecimal diffPrice = new BigDecimal(purPrice).subtract(new BigDecimal(lowestPrice));
                    BigDecimal subtotal = diffPrice.multiply(new BigDecimal(doWeight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    whatSubtotal = subtotal.toString();
                }

                //价控最低价的成本
                //实际成本与最低成本的差价
                stockEntity.setGbDgsGbPriceSubtotal(whatSubtotal); // 相差了多少成本
                stockEntity.setGbDgsGbPriceGoodsId(purchaseGoodsEntity.getGbDpgDisGoodsPriceId());
                stockEntity.setGbDgsGbPriceSubtotalScale(goodsPriceEntity.getGbDgpPurScale());
            }

        }

        stockEntity.setGbDgsStatus(0);
        stockEntity.setGbDgsGbDepartmentOrderId(order.getGbDepartmentOrdersId());
        stockEntity.setGbDgsGbGoodsStockId(-1);
        stockEntity.setGbDgsGbFromDepartmentId(order.getGbDoToDepartmentId());
        stockEntity.setGbDgsNxDistributerId(order.getGbDoNxDistributerId());
        stockEntity.setGbDgsReceiveUserId(order.getGbDoReceiveUserId());
        stockEntity.setGbDgsInventoryDate(formatWhatDay(0));
        stockEntity.setGbDgsInventoryWeek(getWeekOfYear(0).toString());
        stockEntity.setGbDgsInventoryMonth(formatWhatMonth(0));
        stockEntity.setGbDgsInventoryYear(formatWhatYear(0));
        stockEntity.setGbDgsStars(5);
        gbDepartmentGoodsStockService.save(stockEntity);


        orderAddDepDisGoods(order, stockEntity, gbDoDepDisGoodsId);
        updateDepGoodsDailyBusiness(stockEntity);

        //2，修改订单状态
        order.setGbDoStatus(getGbOrderStatusReceived());
        gbDepartmentOrdersService.update(order);

        //3，修改送货单收货单子数量
        if (order.getGbDoPurchaseGoodsId() != -1) {
            GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDPGService.queryObject(order.getGbDoPurchaseGoodsId());
            BigDecimal gbPgOrderAmount = new BigDecimal(purchaseGoodsEntity.getGbDpgOrdersAmount());
            BigDecimal gbDbFinishAmount = new BigDecimal(purchaseGoodsEntity.getGbDpgOrdersFinishAmount());
            if (gbDbFinishAmount.add(new BigDecimal(1)).compareTo(gbPgOrderAmount) == 0) {
                purchaseGoodsEntity.setGbDpgOrdersFinishAmount(purchaseGoodsEntity.getGbDpgOrdersAmount());
                purchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusWaitReceive());
            } else {
                BigDecimal add = gbDbFinishAmount.add(new BigDecimal(1));
                purchaseGoodsEntity.setGbDpgOrdersFinishAmount(add.intValue());
            }
            gbDPGService.update(purchaseGoodsEntity);

        }

        return R.ok();

    }

    private void orderAddDepDisGoods(GbDepartmentOrdersEntity ordersEntity, GbDepartmentGoodsStockEntity stockEntity, Integer depDisGoodsId) {

        BigDecimal stockSubtotal = new BigDecimal(stockEntity.getGbDgsSubtotal());
        BigDecimal stockWeight = new BigDecimal(stockEntity.getGbDgsWeight());
        BigDecimal subTotal = new BigDecimal(0);
        BigDecimal weight = new BigDecimal(0);
        GbDepartmentDisGoodsEntity depDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depDisGoodsId);
        subTotal = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalSubtotal()).add(stockSubtotal);
        weight = new BigDecimal(depDisGoodsEntity.getGbDdgStockTotalWeight()).add(stockWeight);
        //updateOrder
        depDisGoodsEntity.setGbDdgOrderDate(formatWhatDay(0));
        depDisGoodsEntity.setGbDdgOrderPrice(ordersEntity.getGbDoPrice());
        depDisGoodsEntity.setGbDdgOrderQuantity(ordersEntity.getGbDoQuantity());
        depDisGoodsEntity.setGbDdgOrderRemark(ordersEntity.getGbDoRemark());
        depDisGoodsEntity.setGbDdgOrderStandard(ordersEntity.getGbDoStandard());
        depDisGoodsEntity.setGbDdgOrderWeight(ordersEntity.getGbDoWeight());


        if (new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale()).compareTo(new BigDecimal(0)) == 1) {
            BigDecimal showScale = new BigDecimal(depDisGoodsEntity.getGbDdgShowStandardScale());
            BigDecimal standardWeight = weight.divide(showScale, 1, BigDecimal.ROUND_HALF_UP);
            depDisGoodsEntity.setGbDdgShowStandardWeight(standardWeight.toString());
        }

        depDisGoodsEntity.setGbDdgStockTotalSubtotal(subTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgStockTotalWeight(weight.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        depDisGoodsEntity.setGbDdgInventoryDate(formatWhatDay(0));
        depDisGoodsEntity.setGbDdgInventoryFullTime(formatWhatFullTime(0));
        gbDepartmentDisGoodsService.update(depDisGoodsEntity);

    }

    private void updateDepGoodsDailyBusiness(GbDepartmentGoodsStockEntity stock) {

        Map<String, Object> map = new HashMap<>();
        map.put("depGoodsId", stock.getGbDgsGbDepDisGoodsId());
        map.put("date", formatWhatDay(0));
        GbDepartmentGoodsDailyEntity depGoodsDailyItem = gbDepGoodsDailyService.queryDepGoodsDailyItem(map);
        if (depGoodsDailyItem != null) {
            BigDecimal weight = new BigDecimal(depGoodsDailyItem.getGbDgdWeight());
            BigDecimal total = new BigDecimal(depGoodsDailyItem.getGbDgdSubtotal());
            BigDecimal restWeight = new BigDecimal(depGoodsDailyItem.getGbDgdRestWeight());
            BigDecimal restSubtotal = new BigDecimal(depGoodsDailyItem.getGbDgdRestSubtotal());
            BigDecimal totalWeight = new BigDecimal(stock.getGbDgsWeight()).add(weight).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalSubtotal = new BigDecimal(stock.getGbDgsSubtotal()).add(total).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalRestWeight = new BigDecimal(stock.getGbDgsWeight()).add(restWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalRestSubtotal = new BigDecimal(stock.getGbDgsSubtotal()).add(restSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
            depGoodsDailyItem.setGbDgdWeight(totalWeight.toString());
            depGoodsDailyItem.setGbDgdSubtotal(totalSubtotal.toString());
            depGoodsDailyItem.setGbDgdRestWeight(totalRestWeight.toString());
            depGoodsDailyItem.setGbDgdRestSubtotal(totalRestSubtotal.toString());
            depGoodsDailyItem.setGbDgdSellClearHour("-1");
            depGoodsDailyItem.setGbDgdSellClearMinute("-1");
            depGoodsDailyItem.setGbDgdStatus(0);
            gbDepGoodsDailyService.update(depGoodsDailyItem);

        } else {
            GbDepartmentGoodsDailyEntity dailyEntity = new GbDepartmentGoodsDailyEntity();
            dailyEntity.setGbDgdGbDistributerId(stock.getGbDgsGbDistributerId());
            dailyEntity.setGbDgdGbDepartmentId(stock.getGbDgsGbDepartmentId());
            dailyEntity.setGbDgdGbDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
            dailyEntity.setGbDgdGbDisGoodsId(stock.getGbDgsGbDisGoodsId());
            dailyEntity.setGbDgdGbDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
            dailyEntity.setGbDgdGbDisGoodsGrandId(stock.getGbDgsGbDisGoodsGrandId());
            GbDistributerFatherGoodsEntity fatherGoodsEntity = gbDistributerFatherGoodsService.queryObject(stock.getGbDgsGbDisGoodsGrandId());
            dailyEntity.setGbDgdGbDisGoodsGreatGrandId(fatherGoodsEntity.getGbDfgFathersFatherId());
            dailyEntity.setGbDgdGbDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
            dailyEntity.setGbDgdDate(formatWhatDay(0));
            dailyEntity.setGbDgdWeek(getWeekOfYear(0).toString());
            dailyEntity.setGbDgdMonth(formatWhatMonth(0));
            dailyEntity.setGbDgdYear(formatWhatYear(0));
            dailyEntity.setGbDgdDay(getWeek(0));
            dailyEntity.setGbDgdWeight(stock.getGbDgsWeight());
            dailyEntity.setGbDgdRestWeight(stock.getGbDgsWeight());
            dailyEntity.setGbDgdRestSubtotal(stock.getGbDgsSubtotal());
            dailyEntity.setGbDgdSubtotal(stock.getGbDgsSubtotal());
            dailyEntity.setGbDgdProduceWeight("0");
            dailyEntity.setGbDgdProduceSubtotal("0");
            dailyEntity.setGbDgdLossWeight("0");
            dailyEntity.setGbDgdLossSubtotal("0");
            dailyEntity.setGbDgdReturnWeight("0");
            dailyEntity.setGbDgdReturnSubtotal("0");
            dailyEntity.setGbDgdWasteWeight("0");
            dailyEntity.setGbDgdWasteSubtotal("0");
            dailyEntity.setGbDgdSalesSubtotal("0");
            dailyEntity.setGbDgdProfitSubtotal("0");
            dailyEntity.setGbDgdAfterProfitSubtotal("0");
            dailyEntity.setGbDgdSellClearHour("-1");
            dailyEntity.setGbDgdSellClearMinute("-1");
            dailyEntity.setGbDgdLastWeight("0");
            dailyEntity.setGbDgdLastSubtotal("0");
            dailyEntity.setGbDgdLastProduceWeight("0");
            Integer gbDgdGbDisGoodsId = dailyEntity.getGbDgdGbDisGoodsId();
            GbDistributerGoodsEntity distributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDgdGbDisGoodsId);
            if (distributerGoodsEntity.getGbDgControlFresh() == 1) {
                dailyEntity.setGbDgdFreshRate("100");
            } else {
                dailyEntity.setGbDgdFreshRate("0");
            }
            dailyEntity.setGbDgdFullTime(formatFullTime());
            dailyEntity.setGbDgdStatus(0);
            gbDepGoodsDailyService.save(dailyEntity);
        }
    }


    private void updatePhoneBillData(NxDepartmentBillEntity nxDepartmentBill) {

        System.out.println("savesonnsuusbsilllnwnewenewwnwnennwnwn" + nxDepartmentBill);
        BigDecimal billTotal = new BigDecimal(0);
        BigDecimal billProfit = new BigDecimal(0);

        Map<String, Object> map = new HashMap<>();
        map.put("orderDisId", nxDepartmentBill.getNxDbDisId());
        map.put("depFatherId", nxDepartmentBill.getNxDbDepFatherId());
        if (!nxDepartmentBill.getNxDbDepFatherId().equals(nxDepartmentBill.getNxDbDepId())) {
            map.put("depId", nxDepartmentBill.getNxDbDepId());
        }
        map.put("equalStatus", 2);
//        map.put("equalPurStatus", 4);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        for (NxDepartmentOrdersEntity orders : ordersEntities) {
            //0 subtotal
            billTotal = billTotal.add(new BigDecimal(orders.getNxDoSubtotal()));

            Map<String, Object> mapDG = new HashMap<>();
            mapDG.put("disId", orders.getNxDoDistributerId());
            mapDG.put("disGoodsId", orders.getNxDoDisGoodsId());
            mapDG.put("depId", orders.getNxDoDepartmentId());
            System.out.println("dedigodo" + mapDG);
            NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(mapDG);
            NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(orders.getNxDoDisGoodsId());

            //1，配送商自己的客户
            if (nxDepartmentDisGoodsEntity != null) {
                orders.setNxDoDepDisGoodsId(nxDepartmentDisGoodsEntity.getNxDepartmentDisGoodsId());
                nxDepartmentDisGoodsEntity.setNxDdgOrderPrice(orders.getNxDoPrice());
                nxDepartmentDisGoodsEntity.setNxDdgOrderCostPrice(orders.getNxDoCostPrice());
                nxDepartmentDisGoodsEntity.setNxDdgOrderQuantity(orders.getNxDoQuantity());
                nxDepartmentDisGoodsEntity.setNxDdgOrderRemark(orders.getNxDoRemark());
                nxDepartmentDisGoodsEntity.setNxDdgOrderStandard(orders.getNxDoStandard());
                nxDepartmentDisGoodsEntity.setNxDdgDepGoodsName(orders.getNxDoGoodsName());
                nxDepartmentDisGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
                if (orders.getNxDoGoodsOriginalName() != null) {
                    nxDepartmentDisGoodsEntity.setNxDdgOrderGoodsName(orders.getNxDoGoodsOriginalName());
                }

                nxDepartmentDisGoodsEntity.setNxDdgOrderPriceLevel(orders.getNxDoCostPriceLevel());
                nxDepartmentDisGoodsService.update(nxDepartmentDisGoodsEntity);

            } else {
                System.out.println("new Depdiiddid");
                NxDepartmentDisGoodsEntity disGoodsEntity = new NxDepartmentDisGoodsEntity();
                disGoodsEntity.setNxDdgDepGoodsName(orders.getNxDoGoodsName());
                disGoodsEntity.setNxDdgDisGoodsId(orders.getNxDoDisGoodsId());
                disGoodsEntity.setNxDdgDisGoodsFatherId(nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId());
                disGoodsEntity.setNxDdgDisGoodsGrandId(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());

                NxDistributerFatherGoodsEntity fatherGoodsEntity = distributerFatherGoodsService.queryObject(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
                Integer greatFatherId = fatherGoodsEntity.getNxDfgFathersFatherId();
                disGoodsEntity.setNxDdgDisGoodsGreatId(greatFatherId);

                disGoodsEntity.setNxDdgDepGoodsPinyin(nxDistributerGoodsEntity.getNxDgGoodsPinyin());
                disGoodsEntity.setNxDdgDepGoodsPy(nxDistributerGoodsEntity.getNxDgGoodsPy());
                disGoodsEntity.setNxDdgDepGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
                disGoodsEntity.setNxDdgDepartmentId(orders.getNxDoDepartmentId());
                disGoodsEntity.setNxDdgDepartmentFatherId(orders.getNxDoDepartmentFatherId());
                disGoodsEntity.setNxDdgNxDistributerId(orders.getNxDoDistributerId());
                //orderData
                disGoodsEntity.setNxDdgOrderPrice(orders.getNxDoPrice());
                disGoodsEntity.setNxDdgOrderCostPrice(orders.getNxDoCostPrice());
                disGoodsEntity.setNxDdgOrderQuantity(orders.getNxDoQuantity());
                disGoodsEntity.setNxDdgOrderRemark(orders.getNxDoRemark());
                disGoodsEntity.setNxDdgOrderStandard(orders.getNxDoStandard());
                disGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
                disGoodsEntity.setNxDdgGbDistributerId(orders.getNxDoGbDistributerId());
                disGoodsEntity.setNxDdgGoodsNxDistributerId(nxDistributerGoodsEntity.getNxDgDistributerId());

                if (orders.getNxDoGoodsOriginalName() != null) {
                    disGoodsEntity.setNxDdgOrderGoodsName(orders.getNxDoGoodsOriginalName());
                }
                disGoodsEntity.setNxDdgOrderPriceLevel(orders.getNxDoCostPriceLevel());
                nxDepartmentDisGoodsService.save(disGoodsEntity);
                orders.setNxDoDepDisGoodsId(disGoodsEntity.getNxDepartmentDisGoodsId());
            }


            orders.setNxDoStatus(3);
            orders.setNxDoPurchaseStatus(getNxDisPurchaseGoodsFinishPay());
            orders.setNxDoBillId(nxDepartmentBill.getNxDepartmentBillId());
            nxDepartmentOrdersService.update(orders);
            //迁移
            nxDepartmentOrdersService.moveOrderToHistory(orders);  // ✅ 迁移

            //updata weight
            Integer doDisGoodsId = orders.getNxDoDisGoodsId();
            NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
            BigDecimal weight = new BigDecimal(distributerGoodsEntity.getNxDgOutTotalWeight());
            BigDecimal orderWeight = new BigDecimal(orders.getNxDoWeight());
            BigDecimal add = weight.add(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
            distributerGoodsEntity.setNxDgOutTotalWeight(add.toString());
            dgService.update(distributerGoodsEntity);


        }

        nxDepartmentBill.setNxDbTotal(billTotal.toString());
        nxDepartmentBill.setNxDbProfitTotal(billProfit.toString());
        BigDecimal decimal = billProfit.divide(billTotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
        nxDepartmentBill.setNxDbProfitScale(decimal.toString());
        nxDepartmentBillService.update(nxDepartmentBill);

        Integer nxDbDisId = nxDepartmentBill.getNxDbDisId();
        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(nxDbDisId);
        NxDistributerPayListEntity payListEntity = new NxDistributerPayListEntity();
        payListEntity.setNxNdplNxDisId(nxDbDisId);
        payListEntity.setNxNdplNxDepartmentId(nxDepartmentBill.getNxDbDepId());
        payListEntity.setNxNdplNxDepartmentFatherId(nxDepartmentBill.getNxDbDepFatherId());
        int size = ordersEntities.size();
        payListEntity.setNxNdplPaySubtotal(Integer.valueOf(size).toString());
        payListEntity.setNxNdplPayTime(formatFullTime());
        payListEntity.setNxNdplPayDate(formatWhatDay(0));
        payListEntity.setNxNdplPayMonth(formatWhatMonth(0));
        payListEntity.setNxNdplPayYear(formatWhatYear(0));
        payListEntity.setNxNdplStatus(0);
        payListEntity.setNxNdplType(getNxDisPayListWeb());
        payListEntity.setNxNdplRestPoints(nxDistributerEntity.getNxDistributerBuyQuantity());
        payListEntity.setNxNdplNxDbId(nxDepartmentBill.getNxDepartmentBillId());
        payListService.save(payListEntity);

        BigDecimal decimal0 = new BigDecimal(nxDistributerEntity.getNxDistributerBuyQuantity());
        BigDecimal decimal1 = new BigDecimal(ordersEntities.size());
        BigDecimal restPoints = decimal0.subtract(decimal1).setScale(0, BigDecimal.ROUND_HALF_UP);
        nxDistributerEntity.setNxDistributerBuyQuantity(restPoints.toString());

        nxDistributerService.update(nxDistributerEntity);

    }

    /**
     * 京京送货 部门保存订单
     *
     * @param depFatherId
     * @param disId
     * @return
     */
    @Transactional
    @RequestMapping(value = "/saveAccountBillPhoneSub", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountBillPhoneSub(Integer depFatherId, Integer disId) {
        List<NxDepartmentEntity> departmentEntities = nxDepartmentService.querySubDepartments(depFatherId);
        if (departmentEntities.size() > 0) {
            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                Map<String, Object> map = new HashMap<>();
                map.put("depId", departmentEntity.getNxDepartmentId());
                map.put("equalStatus", 2);
//                map.put("equalPurStatus", 4);
                System.out.println("susnamap" + map);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                if (integer > 0) {
                    Double aDouble = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
                    NxDepartmentBillEntity nxDepartmentBill = new NxDepartmentBillEntity();
                    nxDepartmentBill.setNxDbDepId(departmentEntity.getNxDepartmentId());
                    nxDepartmentBill.setNxDbDepFatherId(departmentEntity.getNxDepartmentFatherId());
                    nxDepartmentBill.setNxDbDisId(departmentEntity.getNxDepartmentDisId());
                    nxDepartmentBill.setNxDbTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    nxDepartmentBill.setNxDbStatus(0);
                    nxDepartmentBill.setNxDbDate(formatWhatDay(0));
                    nxDepartmentBill.setNxDbTime(formatWhatYearDayTime(0));
                    nxDepartmentBill.setNxDbMonth(formatWhatMonth(0));
                    nxDepartmentBill.setNxDbWeek(getWeekOfYear(0).toString());
                    nxDepartmentBill.setNxDbDay(getWeek(0));
                    nxDepartmentBill.setNxDbYear(formatWhatYear(0));
                    nxDepartmentBill.setNxDbGbDisId(-1);
                    nxDepartmentBill.setNxDbGbDepId(-1);
                    nxDepartmentBill.setNxDbGbDepFatherId(-1);
                    NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
                    String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
                    System.out.println("disnamme" + nxDistributerEntity.getNxDistributerName());
                    String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();
                    nxDepartmentBill.setNxDbTradeNo(s);
                    nxDepartmentBillService.save(nxDepartmentBill);

                    updatePhoneBillData(nxDepartmentBill);

                }

            }
        }


        return R.ok();
    }

    /**
     * 京京送货 gb部门保存订单
     *
     * @param depFatherId
     * @param disId
     * @return
     */
    @Transactional
    @RequestMapping(value = "/saveAccountBillPhoneSubGb", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountBillPhoneSubGb(Integer depFatherId, Integer disId) {
        System.out.println("depid======Gb" + depFatherId + "disid" + disId);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.querySubDepartments(depFatherId);
        if (departmentEntities.size() > 0) {
            System.out.println("deppsisiiissisisii" + departmentEntities.size());
            for (GbDepartmentEntity departmentEntity : departmentEntities) {
                Map<String, Object> map = new HashMap<>();
                map.put("gbDepId", departmentEntity.getGbDepartmentId());
                map.put("equalStatus", 2);
//                map.put("equalPurStatus", 4);
                System.out.println("susnamap" + map);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                if (integer > 0) {

                    Double aDouble = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
                    NxDepartmentBillEntity nxDepartmentBill = new NxDepartmentBillEntity();
                    nxDepartmentBill.setNxDbDepId(-1);
                    nxDepartmentBill.setNxDbDepFatherId(-1);
                    nxDepartmentBill.setNxDbDisId(disId);
                    nxDepartmentBill.setNxDbGbDepId(departmentEntity.getGbDepartmentId());
                    nxDepartmentBill.setNxDbGbDepFatherId(departmentEntity.getGbDepartmentFatherId());
                    nxDepartmentBill.setNxDbGbDisId(departmentEntity.getGbDepartmentDisId());
                    nxDepartmentBill.setNxDbTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    nxDepartmentBill.setNxDbStatus(-1);
                    nxDepartmentBill.setNxDbPrintTimes(0);
                    nxDepartmentBill.setNxDbNxCommunityId(-1);
                    nxDepartmentBill.setNxDbNxRestrauntId(-1);
                    nxDepartmentBill.setNxDbDate(formatWhatDay(0));
                    nxDepartmentBill.setNxDbTime(formatWhatYearDayTime(0));
                    nxDepartmentBill.setNxDbMonth(formatWhatMonth(0));
                    nxDepartmentBill.setNxDbWeek(getWeekOfYear(0).toString());
                    nxDepartmentBill.setNxDbDay(getWeek(0));
                    nxDepartmentBill.setNxDbYear(formatWhatYear(0));

                    NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
                    String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
                    System.out.println("disnamme" + nxDistributerEntity.getNxDistributerName());
                    String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();

                    System.out.println("savbesub" + nxDepartmentBill);
                    nxDepartmentBill.setNxDbTradeNo(s);
                    nxDepartmentBillService.save(nxDepartmentBill);
                    System.out.println("dfadkfaslfjsalfjdlasfjalsfjdalsfjdasl");


                    Map<String, Object> mapO = new HashMap<>();
                    mapO.put("gbDepId", nxDepartmentBill.getNxDbGbDepId());
                    mapO.put("status", 3);
                    mapO.put("disId", nxDepartmentBill.getNxDbDisId());
                    System.out.println("ororororooroor" + mapO);
                    Integer integers = nxDepartmentOrdersService.queryReturnOrderCount(mapO);

                    GbDepartmentBillEntity gbDepartmentBillEntity = savePhoneGbBillByNxBIll(nxDepartmentBill, integers);

                    nxDisWaitingGbReceive(nxDepartmentBill, gbDepartmentBillEntity);

                }

            }
        }

        return R.ok();
    }

    /**
     * 京京送货 gb门店保存saveAccountBillPhoneGb
     *
     * @param nxDepartmentBill
     * @return
     */
    @Transactional
    @RequestMapping(value = "/saveAccountBillPhoneGb", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountBillPhoneGb(@RequestBody NxDepartmentBillEntity nxDepartmentBill) {

        System.out.println("prinsislGGGGGGGGGGG");
        nxDepartmentBill.setNxDbStatus(-1);
        nxDepartmentBill.setNxDbDate(formatWhatDay(0));
        nxDepartmentBill.setNxDbTime(formatWhatYearDayTime(0));
        nxDepartmentBill.setNxDbMonth(formatWhatMonth(0));
        nxDepartmentBill.setNxDbWeek(getWeekOfYear(0).toString());
        nxDepartmentBill.setNxDbDay(getWeek(0));
        nxDepartmentBill.setNxDbYear(formatWhatYear(0));
        nxDepartmentBillService.save(nxDepartmentBill);

        System.out.println("dfadkfaslfjsalfjdlasfjalsfjdalsfjdasl");
        Map<String, Object> mapO = new HashMap<>();
        mapO.put("gbDepFatherId", nxDepartmentBill.getNxDbGbDepId());
        mapO.put("equalStatus", 2);
        mapO.put("disId", nxDepartmentBill.getNxDbDisId());
        System.out.println("ororororooroor" + mapO);
        Integer integer = nxDepartmentOrdersService.queryReturnOrderCount(mapO);

        GbDepartmentBillEntity gbDepartmentBillEntity = savePhoneGbBillByNxBIll(nxDepartmentBill, integer);

        nxDisWaitingGbReceive(nxDepartmentBill, gbDepartmentBillEntity);

        return R.ok();
    }

    /**
     * 京京送货 gb门店保存saveAccountBillPhoneGb
     *
     * @param nxDepartmentBill
     * @return
     */
    @Transactional
    @RequestMapping(value = "/saveAccountBillPhoneGbAndPrint", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountBillPhoneGbAndPrint(@RequestBody NxDepartmentBillEntity nxDepartmentBill) {

        System.out.println("prinsislGGGGGGGGGGG");
        nxDepartmentBill.setNxDbStatus(-1);
        nxDepartmentBill.setNxDbDate(formatWhatDay(0));
        nxDepartmentBill.setNxDbTime(formatWhatYearDayTime(0));
        nxDepartmentBill.setNxDbMonth(formatWhatMonth(0));
        nxDepartmentBill.setNxDbWeek(getWeekOfYear(0).toString());
        nxDepartmentBill.setNxDbDay(getWeek(0));
        nxDepartmentBill.setNxDbYear(formatWhatYear(0));
        nxDepartmentBillService.save(nxDepartmentBill);
        System.out.println("dfadkfaslfjsalfjdlasfjalsfjdalsfjdasl");
        Map<String, Object> mapO = new HashMap<>();
        mapO.put("gbDepId", nxDepartmentBill.getNxDbGbDepId());
        mapO.put("status", 3);
        mapO.put("disId", nxDepartmentBill.getNxDbDisId());
        System.out.println("ororororooroor" + mapO);
        Integer integer = nxDepartmentOrdersService.queryReturnOrderCount(mapO);

        GbDepartmentBillEntity gbDepartmentBillEntity = savePhoneGbBillByNxBIll(nxDepartmentBill, integer);

        nxDisWaitingGbReceiveAndPrint(nxDepartmentBill, gbDepartmentBillEntity);


        return R.ok();
    }


    //生成签名字符串
    private static String signature(String USER, String UKEY, String STIME) {
        String s = DigestUtils.sha1Hex(USER + UKEY + STIME);
        return s;
    }

    /**
     * 京京送货 门店保存
     *
     * @param
     * @return
     */
    @Transactional
    @RequestMapping(value = "/saveAccountBillPhone", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountBillPhone(@RequestBody NxDepartmentBillEntity nxDepartmentBill) {
        System.out.println("savephoneacoucnccucnncn");
        nxDepartmentBill.setNxDbStatus(0);
        nxDepartmentBill.setNxDbDate(formatWhatDay(0));
        nxDepartmentBill.setNxDbTime(formatWhatYearDayTime(0));
        nxDepartmentBill.setNxDbMonth(formatWhatMonth(0));
        nxDepartmentBill.setNxDbWeek(getWeekOfYear(0).toString());
        nxDepartmentBill.setNxDbDay(getWeek(0));
        nxDepartmentBill.setNxDbYear(formatWhatYear(0));
        nxDepartmentBill.setNxDbGbDisId(-1);
        nxDepartmentBill.setNxDbGbDepId(-1);
        nxDepartmentBill.setNxDbGbDepFatherId(-1);
        nxDepartmentBillService.save(nxDepartmentBill);

        updatePhoneBillData(nxDepartmentBill);

        return R.ok();
    }


    @Transactional
    @RequestMapping(value = "/saveAccountBillPhoneFeiE", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountBillPhoneFeiE(@RequestBody NxDepartmentBillEntity nxDepartmentBill) {
        System.out.println("savephoneacoucnccucnncn");
        nxDepartmentBill.setNxDbStatus(0);
        nxDepartmentBill.setNxDbDate(formatWhatDay(0));
        nxDepartmentBill.setNxDbTime(formatWhatYearDayTime(0));
        nxDepartmentBill.setNxDbMonth(formatWhatMonth(0));
        nxDepartmentBill.setNxDbWeek(getWeekOfYear(0).toString());
        nxDepartmentBill.setNxDbDay(getWeek(0));
        nxDepartmentBill.setNxDbYear(formatWhatYear(0));
        nxDepartmentBill.setNxDbGbDisId(-1);
        nxDepartmentBill.setNxDbGbDepId(-1);
        nxDepartmentBill.setNxDbGbDepFatherId(-1);

        nxDepartmentBillService.save(nxDepartmentBill);

        updatePhoneBillData(nxDepartmentBill);

        printFeiEBillData(nxDepartmentBill);

        return R.ok();
    }


    /**
     * 自助打印机门店保存
     *
     * @param gbDepFatherId
     * @param gbDepId
     * @param tradeNo
     * @param userId
     * @param nxDisId
     * @return
     */
    @Transactional
    @RequestMapping(value = "/saveAccountBillPrinterGb", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountBillPrinterGb(Integer gbDepFatherId, Integer gbDepId, String tradeNo, Integer userId, Integer nxDisId) {

        Map<String, Object> nxMap = new HashMap<>();
        nxMap.put("gbDepFatherId", gbDepFatherId);
        nxMap.put("status", 3);
        if (!gbDepFatherId.equals(gbDepId)) {
            nxMap.put("gbDepId", gbDepId);
        }
        NxDepartmentBillEntity nxDepartmentBill = new NxDepartmentBillEntity();
        nxDepartmentBill.setNxDbDepId(gbDepId);
        nxDepartmentBill.setNxDbDepFatherId(gbDepFatherId);
        nxDepartmentBill.setNxDbDisId(nxDisId);
        Double aDouble = nxDepartmentOrdersService.queryDepOrdersSubtotal(nxMap);
        nxDepartmentBill.setNxDbTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        nxDepartmentBill.setNxDbStatus(0);
        nxDepartmentBill.setNxDbDate(formatWhatDay(0));
        nxDepartmentBill.setNxDbTime(formatWhatYearDayTime(0));
        nxDepartmentBill.setNxDbMonth(formatWhatMonth(0));
        nxDepartmentBill.setNxDbWeek(getWeekOfYear(0).toString());
        nxDepartmentBill.setNxDbDay(getWeek(0));
        nxDepartmentBill.setNxDbYear(formatWhatYear(0));
        nxDepartmentBill.setNxDbTradeNo(tradeNo);
        nxDepartmentBill.setNxDbPrintTimes(1);
        nxDepartmentBill.setNxDbGbDisId(-1);
        nxDepartmentBill.setNxDbGbDepId(-1);
        nxDepartmentBill.setNxDbGbDepFatherId(-1);
        nxDepartmentBill.setNxDbNxRestrauntId(-1);
        nxDepartmentBill.setNxDbNxCommunityId(-1);
        nxDepartmentBill.setNxDbIssueUserId(userId);
        nxDepartmentBillService.save(nxDepartmentBill);
        System.out.println("dfadkfaslfjsalfjdlasfjalsfjdalsfjdasl");
        Map<String, Object> mapO = new HashMap<>();
        mapO.put("gbDepId", nxDepartmentBill.getNxDbGbDepId());
        mapO.put("status", 3);
        mapO.put("disId", nxDepartmentBill.getNxDbDisId());
        System.out.println("ororororooroor" + mapO);
        Integer integer = nxDepartmentOrdersService.queryReturnOrderCount(mapO);

        GbDepartmentBillEntity gbDepartmentBillEntity = savePhoneGbBillByNxBIll(nxDepartmentBill, integer);

        BigDecimal billTotal = new BigDecimal(0);
        BigDecimal billProfit = new BigDecimal(0);

        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(nxMap);

        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity orders : ordersEntities) {
                //0 subtotal
                billTotal = billTotal.add(new BigDecimal(orders.getNxDoSubtotal()));

                orders.setNxDoStatus(3);
                orders.setNxDoPurchaseStatus(getNxDisPurchaseGoodsFinishPay());
                orders.setNxDoBillId(nxDepartmentBill.getNxDepartmentBillId());
                nxDepartmentOrdersService.update(orders);

                GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(orders.getNxDoGbDepartmentOrderId());
                gbDepartmentOrdersEntity.setGbDoPrice(orders.getNxDoPrice());
                gbDepartmentOrdersEntity.setGbDoPriceDifferent(orders.getNxDoPriceDifferent());
                gbDepartmentOrdersEntity.setGbDoWeight(orders.getNxDoWeight());
                gbDepartmentOrdersEntity.setGbDoSubtotal(orders.getNxDoSubtotal());
                gbDepartmentOrdersEntity.setGbDoBuyStatus(3);
                gbDepartmentOrdersEntity.setGbDoBillId(gbDepartmentBillEntity.getGbDepartmentBillId());
                gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);


                //updata weight
                Integer doDisGoodsId = orders.getNxDoDisGoodsId();
                NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
                BigDecimal weight = new BigDecimal(distributerGoodsEntity.getNxDgOutTotalWeight());
                BigDecimal orderWeight = new BigDecimal(orders.getNxDoWeight());
                BigDecimal add = weight.add(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                distributerGoodsEntity.setNxDgOutTotalWeight(add.toString());
                dgService.update(distributerGoodsEntity);

                Integer nxDoPurchaseGoodsId = orders.getNxDoPurchaseGoodsId();
                if (nxDoPurchaseGoodsId != -1) {
                    NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                    Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                    Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                    if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
                        purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                        purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
                    } else {
                        purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
                    }
                    nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                }
            }
        }

        nxDepartmentBill.setNxDbTotal(billTotal.toString());
        nxDepartmentBill.setNxDbProfitTotal(billProfit.toString());
        BigDecimal decimal = billProfit.divide(billTotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
        nxDepartmentBill.setNxDbProfitScale(decimal.toString());
        nxDepartmentBillService.update(nxDepartmentBill);

        gbDepartmentBillEntity.setGbDbPayTotal(nxDepartmentBill.getNxDbTotal());
        gbDepartmentBillEntity.setGbDbTotal(billTotal.toString());
        gbDepartmentBillService.update(gbDepartmentBillEntity);
        return R.ok();
    }

    @Transactional
    @RequestMapping(value = "/saveAccountBillPrinter", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountBillPrinter(Integer depFatherId, Integer depId, String tradeNo, Integer userId) {

        NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depId);
        Integer nxDepartmentDisId = departmentEntity.getNxDepartmentDisId();
        NxDepartmentBillEntity nxDepartmentBill = new NxDepartmentBillEntity();
        nxDepartmentBill.setNxDbDepId(depId);
        nxDepartmentBill.setNxDbDepFatherId(depFatherId);
        nxDepartmentBill.setNxDbDisId(nxDepartmentDisId);
        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depFatherId);
        if (!depFatherId.equals(depId)) {
            map.put("depId", depId);
        }
        map.put("status", 3);
        map.put("orderDisId", nxDepartmentBill.getNxDbDisId());
        Double aDouble = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
        nxDepartmentBill.setNxDbTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        nxDepartmentBill.setNxDbStatus(0);
        nxDepartmentBill.setNxDbDate(formatWhatDay(0));
        nxDepartmentBill.setNxDbTime(formatWhatYearDayTime(0));
        nxDepartmentBill.setNxDbMonth(formatWhatMonth(0));
        nxDepartmentBill.setNxDbWeek(getWeekOfYear(0).toString());
        nxDepartmentBill.setNxDbDay(getWeek(0));
        nxDepartmentBill.setNxDbYear(formatWhatYear(0));
        nxDepartmentBill.setNxDbTradeNo(tradeNo);
        nxDepartmentBill.setNxDbPrintTimes(1);
        nxDepartmentBill.setNxDbGbDisId(-1);
        nxDepartmentBill.setNxDbGbDepId(-1);
        nxDepartmentBill.setNxDbGbDepFatherId(-1);
        nxDepartmentBill.setNxDbNxRestrauntId(-1);
        nxDepartmentBill.setNxDbNxCommunityId(-1);
        nxDepartmentBill.setNxDbIssueUserId(userId);
        nxDepartmentBillService.save(nxDepartmentBill);

        BigDecimal billTotal = new BigDecimal(0);
        BigDecimal billProfit = new BigDecimal(0);

        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        System.out.println("ordersizizizizizizi" + ordersEntities.size());
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity orders : ordersEntities) {
                //0 subtotal
                billTotal = billTotal.add(new BigDecimal(orders.getNxDoSubtotal()));

                Map<String, Object> mapDG = new HashMap<>();
                mapDG.put("disGoodsId", orders.getNxDoDisGoodsId());
                mapDG.put("depId", orders.getNxDoDepartmentId());
                System.out.println("dedigodo" + mapDG);
                NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(mapDG);
                NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(orders.getNxDoDisGoodsId());

                String nxDgGoodsName = nxDistributerGoodsEntity.getNxDgGoodsName();

                //1，配送商自己的客户
                if (nxDepartmentDisGoodsEntity != null) {
                    orders.setNxDoDepDisGoodsId(nxDepartmentDisGoodsEntity.getNxDepartmentDisGoodsId());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderPrice(orders.getNxDoPrice());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderCostPrice(orders.getNxDoCostPrice());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderQuantity(orders.getNxDoQuantity());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderRemark(orders.getNxDoRemark());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderStandard(orders.getNxDoStandard());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
                    if (orders.getNxDoGoodsOriginalName() != null) {
                        nxDepartmentDisGoodsEntity.setNxDdgOrderGoodsName(orders.getNxDoGoodsOriginalName());
                    }
                    nxDepartmentDisGoodsEntity.setNxDdgOrderPriceLevel(orders.getNxDoCostPriceLevel());
                    nxDepartmentDisGoodsService.update(nxDepartmentDisGoodsEntity);

                } else {
                    System.out.println("new Depdiiddid");
                    NxDepartmentDisGoodsEntity disGoodsEntity = new NxDepartmentDisGoodsEntity();
                    disGoodsEntity.setNxDdgDepGoodsName(nxDgGoodsName);
                    disGoodsEntity.setNxDdgDisGoodsId(orders.getNxDoDisGoodsId());
                    disGoodsEntity.setNxDdgDisGoodsFatherId(nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId());
                    disGoodsEntity.setNxDdgDisGoodsGrandId(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());

                    NxDistributerFatherGoodsEntity fatherGoodsEntity = distributerFatherGoodsService.queryObject(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
                    Integer greatFatherId = fatherGoodsEntity.getNxDfgFathersFatherId();
                    disGoodsEntity.setNxDdgDisGoodsGreatId(greatFatherId);


                    disGoodsEntity.setNxDdgDepGoodsPinyin(nxDistributerGoodsEntity.getNxDgGoodsPinyin());
                    disGoodsEntity.setNxDdgDepGoodsPy(nxDistributerGoodsEntity.getNxDgGoodsPy());
                    disGoodsEntity.setNxDdgDepGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
                    disGoodsEntity.setNxDdgDepartmentId(orders.getNxDoDepartmentId());
                    disGoodsEntity.setNxDdgDepartmentFatherId(orders.getNxDoDepartmentFatherId());
                    disGoodsEntity.setNxDdgNxDistributerId(orders.getNxDoDistributerId());
                    //orderData
                    disGoodsEntity.setNxDdgOrderPrice(orders.getNxDoPrice());
                    disGoodsEntity.setNxDdgOrderCostPrice(orders.getNxDoCostPrice());
                    disGoodsEntity.setNxDdgOrderQuantity(orders.getNxDoQuantity());
                    disGoodsEntity.setNxDdgOrderRemark(orders.getNxDoRemark());
                    disGoodsEntity.setNxDdgOrderStandard(orders.getNxDoStandard());
                    disGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
                    disGoodsEntity.setNxDdgGbDistributerId(orders.getNxDoGbDistributerId());
                    disGoodsEntity.setNxDdgGoodsNxDistributerId(nxDistributerGoodsEntity.getNxDgDistributerId());

                    if (orders.getNxDoGoodsOriginalName() != null) {
                        disGoodsEntity.setNxDdgOrderGoodsName(orders.getNxDoGoodsOriginalName());
                    }
                    disGoodsEntity.setNxDdgOrderPriceLevel(orders.getNxDoCostPriceLevel());
                    nxDepartmentDisGoodsService.save(disGoodsEntity);
                    orders.setNxDoDepDisGoodsId(disGoodsEntity.getNxDepartmentDisGoodsId());
                }


                //1，配送商自己的客户
                Integer nxDoDepDisGoodsId = checkDepDisGoods(orders);
                orders.setNxDoDepDisGoodsId(nxDoDepDisGoodsId);
                orders.setNxDoStatus(3);
                orders.setNxDoPurchaseStatus(getNxDisPurchaseGoodsFinishPay());
                orders.setNxDoBillId(nxDepartmentBill.getNxDepartmentBillId());

                nxDepartmentOrdersService.update(orders);

                //迁移
                System.out.println("saveAccountBillPrintersaveAccountBillPrinter" + orders.getNxDoGbDepartmentOrderId());
                nxDepartmentOrdersService.moveOrderToHistory(orders);  // ✅ 迁移

                //updata weight
                Integer doDisGoodsId = orders.getNxDoDisGoodsId();
                NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
                BigDecimal weight = new BigDecimal(distributerGoodsEntity.getNxDgOutTotalWeight());
                BigDecimal orderWeight = new BigDecimal(orders.getNxDoWeight());
                BigDecimal add = weight.add(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                distributerGoodsEntity.setNxDgOutTotalWeight(add.toString());
                dgService.update(distributerGoodsEntity);

                Integer nxDoPurchaseGoodsId = orders.getNxDoPurchaseGoodsId();
                if (nxDoPurchaseGoodsId != -1) {
                    NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                    if (purchaseGoodsEntity != null) {
                        Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                        Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                        if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
                            purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                            purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
                        } else {
                            purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
                        }
                        nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                    }
                }
            }
        }


        nxDepartmentBill.setNxDbTotal(billTotal.toString());
        nxDepartmentBill.setNxDbProfitTotal(billProfit.toString());
        BigDecimal decimal = billProfit.divide(billTotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
        nxDepartmentBill.setNxDbProfitScale(decimal.toString());
        nxDepartmentBillService.update(nxDepartmentBill);

        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(nxDepartmentDisId);
        BigDecimal disBuyQuantity = new BigDecimal(nxDistributerEntity.getNxDistributerBuyQuantity());

        final int ORDERS_PER_SHEET = 24;
        final int SHEET_PRICE = 24;


//        int sheetCount = (int) Math.ceil((double) ordersEntities.size() / ORDERS_PER_SHEET);
//        System.out.println("kanakankayigongduoshs" + sheetCount);
//        int total = sheetCount * SHEET_PRICE;
        BigDecimal restQuantity = disBuyQuantity.subtract(new BigDecimal(ordersEntities.size())).setScale(0, BigDecimal.ROUND_HALF_UP);

        NxDistributerPayListEntity payListEntity = new NxDistributerPayListEntity();
        payListEntity.setNxNdplNxDisId(nxDepartmentDisId);
        payListEntity.setNxNdplNxDepartmentId(nxDepartmentBill.getNxDbDepId());
        payListEntity.setNxNdplNxDepartmentFatherId(nxDepartmentBill.getNxDbDepFatherId());
        payListEntity.setNxNdplPaySubtotal(String.valueOf(ordersEntities.size()));

        payListEntity.setNxNdplPayTime(formatFullTime());
        payListEntity.setNxNdplPayDate(formatWhatDay(0));
        payListEntity.setNxNdplPayMonth(formatWhatMonth(0));
        payListEntity.setNxNdplPayYear(formatWhatYear(0));
        payListEntity.setNxNdplStatus(0);
        payListEntity.setNxNdplType(getNxDisPayListPrinter());
        payListEntity.setNxNdplRestPoints(nxDistributerEntity.getNxDistributerBuyQuantity());
        payListEntity.setNxNdplNxDbId(nxDepartmentBill.getNxDepartmentBillId());
        payListService.save(payListEntity);

        nxDistributerEntity.setNxDistributerBuyQuantity(restQuantity.toString());
        System.out.println("dispayddyd" + restQuantity);

        BigDecimal decimal0 = new BigDecimal(nxDistributerEntity.getNxDistributerBuyQuantity());
        BigDecimal decimal1 = new BigDecimal(ordersEntities.size());
        BigDecimal restPoints = decimal0.subtract(decimal1).setScale(0, BigDecimal.ROUND_HALF_UP);
        nxDistributerEntity.setNxDistributerBuyQuantity(restPoints.toString());

//
//        Map<String, Object> mapA = new HashMap<>();
//        mapA.put("billId", nxDepartmentBill.getNxDepartmentBillId());
//        mapA.put("isAgent", -1);
//        System.out.println("anemen" + mapA);
//        Integer integer = historyService.queryDepOrdersAcount(mapA);
//        if(integer > 0){
//
//            BigDecimal decimal00 = new BigDecimal(nxDistributerEntity.getNxDistributerBuyQuantity());
//            BigDecimal decimal11 = new BigDecimal(integer).multiply(new BigDecimal(25));
//            BigDecimal restPoints1 = decimal00.subtract(decimal11).setScale(0, BigDecimal.ROUND_HALF_UP);
//            nxDistributerEntity.setNxDistributerBuyQuantity(restPoints1.toString());
//
//            NxDistributerPayListEntity payListEntityAgent = new NxDistributerPayListEntity();
//            payListEntityAgent.setNxNdplNxDisId(nxDepartmentDisId);
//            payListEntityAgent.setNxNdplNxDepartmentId(nxDepartmentBill.getNxDbDepId());
//            payListEntityAgent.setNxNdplNxDepartmentFatherId(nxDepartmentBill.getNxDbDepFatherId());
//            payListEntityAgent.setNxNdplPaySubtotal(decimal11.toString());
//            payListEntityAgent.setNxNdplPayTime(formatFullTime());
//            payListEntityAgent.setNxNdplPayDate(formatWhatDay(0));
//            payListEntityAgent.setNxNdplPayMonth(formatWhatMonth(0));
//            payListEntityAgent.setNxNdplPayYear(formatWhatYear(0));
//            payListEntityAgent.setNxNdplStatus(0);
//            payListEntityAgent.setNxNdplType(getNxDisPayListOrder());
//            payListEntityAgent.setNxNdplRestPoints(Integer.valueOf(nxDistributerEntity.getNxDistributerBuyQuantity()));
//            payListEntityAgent.setNxNdplNxDbId(nxDepartmentBill.getNxDepartmentBillId());
//            payListService.save(payListEntityAgent);
//        }


        nxDistributerService.update(nxDistributerEntity);


        return R.ok();
    }


    @Transactional
    @RequestMapping(value = "/saveAccountBillPrinterSelf", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountBillPrinterSelf(Integer depFatherId, Integer depId, String tradeNo,
     Integer userId, Integer deviceId, String pricePerSheet, Integer marketId,  Integer paperCount

     ) {

        NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depId);
        Integer nxDepartmentDisId = departmentEntity.getNxDepartmentDisId();
        NxDepartmentBillEntity nxDepartmentBill = new NxDepartmentBillEntity();
        nxDepartmentBill.setNxDbDepId(depId);
        nxDepartmentBill.setNxDbDepFatherId(depFatherId);
        nxDepartmentBill.setNxDbDisId(nxDepartmentDisId);
        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depFatherId);
        if (!depFatherId.equals(depId)) {
            map.put("depId", depId);
        }
        map.put("status", 3);
        map.put("orderDisId", nxDepartmentBill.getNxDbDisId());
        Double aDouble = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
        nxDepartmentBill.setNxDbTotal(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        nxDepartmentBill.setNxDbStatus(0);
        nxDepartmentBill.setNxDbDate(formatWhatDay(0));
        nxDepartmentBill.setNxDbTime(formatWhatYearDayTime(0));
        nxDepartmentBill.setNxDbMonth(formatWhatMonth(0));
        nxDepartmentBill.setNxDbWeek(getWeekOfYear(0).toString());
        nxDepartmentBill.setNxDbDay(getWeek(0));
        nxDepartmentBill.setNxDbYear(formatWhatYear(0));
        nxDepartmentBill.setNxDbTradeNo(tradeNo);
        nxDepartmentBill.setNxDbPrintTimes(1);
        nxDepartmentBill.setNxDbGbDisId(-1);
        nxDepartmentBill.setNxDbGbDepId(-1);
        nxDepartmentBill.setNxDbGbDepFatherId(-1);
        nxDepartmentBill.setNxDbNxRestrauntId(-1);
        nxDepartmentBill.setNxDbNxCommunityId(-1);
        nxDepartmentBill.setNxDbIssueUserId(userId);
        nxDepartmentBillService.save(nxDepartmentBill);

        BigDecimal billTotal = new BigDecimal(0);
        BigDecimal billProfit = new BigDecimal(0);

        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        System.out.println("ordersizizizizizizi" + ordersEntities.size());
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity orders : ordersEntities) {
                //0 subtotal
                billTotal = billTotal.add(new BigDecimal(orders.getNxDoSubtotal()));

                Map<String, Object> mapDG = new HashMap<>();
                mapDG.put("disGoodsId", orders.getNxDoDisGoodsId());
                mapDG.put("depId", orders.getNxDoDepartmentId());
                System.out.println("dedigodo" + mapDG);
                NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(mapDG);
                NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(orders.getNxDoDisGoodsId());

                String nxDgGoodsName = nxDistributerGoodsEntity.getNxDgGoodsName();

                //1，配送商自己的客户
                if (nxDepartmentDisGoodsEntity != null) {
                    orders.setNxDoDepDisGoodsId(nxDepartmentDisGoodsEntity.getNxDepartmentDisGoodsId());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderPrice(orders.getNxDoPrice());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderCostPrice(orders.getNxDoCostPrice());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderQuantity(orders.getNxDoQuantity());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderRemark(orders.getNxDoRemark());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderStandard(orders.getNxDoStandard());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
                    if (orders.getNxDoGoodsOriginalName() != null) {
                        nxDepartmentDisGoodsEntity.setNxDdgOrderGoodsName(orders.getNxDoGoodsOriginalName());
                    }
                    nxDepartmentDisGoodsEntity.setNxDdgOrderPriceLevel(orders.getNxDoCostPriceLevel());
                    nxDepartmentDisGoodsService.update(nxDepartmentDisGoodsEntity);

                } else {
                    System.out.println("new Depdiiddid");
                    NxDepartmentDisGoodsEntity disGoodsEntity = new NxDepartmentDisGoodsEntity();
                    disGoodsEntity.setNxDdgDepGoodsName(nxDgGoodsName);
                    disGoodsEntity.setNxDdgDisGoodsId(orders.getNxDoDisGoodsId());
                    disGoodsEntity.setNxDdgDisGoodsFatherId(nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId());
                    disGoodsEntity.setNxDdgDisGoodsGrandId(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());

                    NxDistributerFatherGoodsEntity fatherGoodsEntity = distributerFatherGoodsService.queryObject(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
                    Integer greatFatherId = fatherGoodsEntity.getNxDfgFathersFatherId();
                    disGoodsEntity.setNxDdgDisGoodsGreatId(greatFatherId);


                    disGoodsEntity.setNxDdgDepGoodsPinyin(nxDistributerGoodsEntity.getNxDgGoodsPinyin());
                    disGoodsEntity.setNxDdgDepGoodsPy(nxDistributerGoodsEntity.getNxDgGoodsPy());
                    disGoodsEntity.setNxDdgDepGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
                    disGoodsEntity.setNxDdgDepartmentId(orders.getNxDoDepartmentId());
                    disGoodsEntity.setNxDdgDepartmentFatherId(orders.getNxDoDepartmentFatherId());
                    disGoodsEntity.setNxDdgNxDistributerId(orders.getNxDoDistributerId());
                    //orderData
                    disGoodsEntity.setNxDdgOrderPrice(orders.getNxDoPrice());
                    disGoodsEntity.setNxDdgOrderCostPrice(orders.getNxDoCostPrice());
                    disGoodsEntity.setNxDdgOrderQuantity(orders.getNxDoQuantity());
                    disGoodsEntity.setNxDdgOrderRemark(orders.getNxDoRemark());
                    disGoodsEntity.setNxDdgOrderStandard(orders.getNxDoStandard());
                    disGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
                    disGoodsEntity.setNxDdgGbDistributerId(orders.getNxDoGbDistributerId());
                    disGoodsEntity.setNxDdgGoodsNxDistributerId(nxDistributerGoodsEntity.getNxDgDistributerId());

                    if (orders.getNxDoGoodsOriginalName() != null) {
                        disGoodsEntity.setNxDdgOrderGoodsName(orders.getNxDoGoodsOriginalName());
                    }
                    disGoodsEntity.setNxDdgOrderPriceLevel(orders.getNxDoCostPriceLevel());
                    nxDepartmentDisGoodsService.save(disGoodsEntity);
                    orders.setNxDoDepDisGoodsId(disGoodsEntity.getNxDepartmentDisGoodsId());
                }


                //1，配送商自己的客户
                Integer nxDoDepDisGoodsId = checkDepDisGoods(orders);
                orders.setNxDoDepDisGoodsId(nxDoDepDisGoodsId);
                orders.setNxDoStatus(3);
                orders.setNxDoPurchaseStatus(getNxDisPurchaseGoodsFinishPay());
                orders.setNxDoBillId(nxDepartmentBill.getNxDepartmentBillId());

                nxDepartmentOrdersService.update(orders);

                //迁移
                System.out.println("saveAccountBillPrintersaveAccountBillPrinterSeelfselff" + orders.getNxDoGbDepartmentOrderId());
                nxDepartmentOrdersService.moveOrderToHistory(orders);  // ✅ 迁移


                //updata weight
                Integer doDisGoodsId = orders.getNxDoDisGoodsId();
                NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
                BigDecimal weight = new BigDecimal(distributerGoodsEntity.getNxDgOutTotalWeight());
                BigDecimal orderWeight = new BigDecimal(orders.getNxDoWeight());
                BigDecimal add = weight.add(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                distributerGoodsEntity.setNxDgOutTotalWeight(add.toString());
                dgService.update(distributerGoodsEntity);


                Integer nxDoPurchaseGoodsId = orders.getNxDoPurchaseGoodsId();
                if (nxDoPurchaseGoodsId != -1) {
                    NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                    if (purchaseGoodsEntity != null) {
                        Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                        Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                        if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
                            purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                            purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
                        } else {
                            purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
                        }
                        nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                    }
                }
            }
        }


        nxDepartmentBill.setNxDbTotal(billTotal.toString());
        nxDepartmentBill.setNxDbProfitTotal(billProfit.toString());
        BigDecimal decimal = billProfit.divide(billTotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
        nxDepartmentBill.setNxDbProfitScale(decimal.toString());
        nxDepartmentBillService.update(nxDepartmentBill);



        // ========== 自助打印机逻辑（新增） ==========
        // 1. 获取打印机设备信息并计算打印费用
        NxMachinePrinterDeviceEntity device = null;
        BigDecimal printFee = BigDecimal.ZERO;
        BigDecimal actualPricePerSheet = BigDecimal.ZERO;
        
        if (deviceId != null) {
            device = nxMachinePrinterDeviceService.queryObject(deviceId);
            if (device == null) {
                return R.error("打印机设备不存在");
            }
            
            // 2. 获取打印单价（优先使用设备配置的单价，其次使用传入的单价）
            if (device.getNxPdPrintPrice() != null) {
                actualPricePerSheet = device.getNxPdPrintPrice();
            } else if (pricePerSheet != null) {
                actualPricePerSheet = new BigDecimal(pricePerSheet);
            }
            
            // 3. 计算打印费用
            if (paperCount != null) {
                printFee = actualPricePerSheet.multiply(new BigDecimal(paperCount));
                System.out.println("打印费用计算：单价=" + actualPricePerSheet + "元/张，张数=" + paperCount + "，总费用=" + printFee + "元");
            }
        }

        // ========== 扣减配送商点数 ==========
        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(nxDepartmentDisId);
        BigDecimal disBuyQuantity = new BigDecimal(nxDistributerEntity.getNxDistributerBuyQuantity());
        
        // 计算扣减点数
        BigDecimal deductPoints;
        if (deviceId != null && paperCount != null) {
            // 自助打印：按打印费用×100计算点数（1元=100个点，1分钱=1个点）
            deductPoints = printFee.multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_HALF_UP);
            System.out.println("自助打印扣点计算：打印费=" + printFee + "元，扣减点数=" + deductPoints + "个点");
        } else {
            // 小程序打印：按订单数扣点（1条订单=1个点）
            deductPoints = new BigDecimal(ordersEntities.size());
        }
        
        // 检查点数是否充足
        if (disBuyQuantity.compareTo(deductPoints) < 0) {
            return R.error("点数不足，当前剩余" + disBuyQuantity.intValue() + "个点，需要" + deductPoints.intValue() + "个点");
        }
        
        // 计算剩余点数
        BigDecimal restPoints = disBuyQuantity.subtract(deductPoints).setScale(0, BigDecimal.ROUND_HALF_UP);

        // ========== 记录点数扣减日志 ==========
        NxDistributerPayListEntity payListEntity = new NxDistributerPayListEntity();
        payListEntity.setNxNdplNxDisId(nxDepartmentDisId);
        payListEntity.setNxNdplNxDepartmentId(nxDepartmentBill.getNxDbDepId());
        payListEntity.setNxNdplNxDepartmentFatherId(nxDepartmentBill.getNxDbDepFatherId());
        payListEntity.setNxNdplPaySubtotal(String.valueOf(deductPoints.intValue()));  // 扣减点数
        payListEntity.setNxNdplPayTime(formatFullTime());
        payListEntity.setNxNdplPayDate(formatWhatDay(0));
        payListEntity.setNxNdplPayMonth(formatWhatMonth(0));
        payListEntity.setNxNdplPayYear(formatWhatYear(0));
        payListEntity.setNxNdplStatus(0);
        payListEntity.setNxNdplType(getNxDisPayListPrinter());
        payListEntity.setNxNdplRestPoints(String.valueOf(restPoints.intValue()));  // 剩余点数（String类型）
        payListEntity.setNxNdplNxDbId(nxDepartmentBill.getNxDepartmentBillId());
        payListService.save(payListEntity);

        // ========== 更新配送商点数余额 ==========
        nxDistributerEntity.setNxDistributerBuyQuantity(restPoints.toString());
        nxDistributerService.update(nxDistributerEntity);

        // ========== 自助打印机：扣减纸张并保存打印记录（新增） ==========
        if (deviceId != null && paperCount != null) {
            try {
                // 3. 扣减打印机纸张数量
                Integer remainingPaper = printerAlertService.printAndCheckAlert(deviceId, paperCount);
                if (remainingPaper == null) {
                    return R.error("打印机纸张不足，无法完成打印");
                }
                
                // 4. 保存打印记录
                NxMachinePrintRecordEntity printRecord = new NxMachinePrintRecordEntity();
                printRecord.setNxPrBillId(nxDepartmentBill.getNxDepartmentBillId());
                printRecord.setNxPrDeviceId(deviceId);
                printRecord.setNxPrMarketId(marketId);
                printRecord.setNxPrDistributerId(nxDepartmentDisId);
                
                // ⭐ 关键：保存打印时配送商的状态快照
                printRecord.setNxPrDistributerType(nxDistributerEntity.getNxDistributerType());
                
                printRecord.setNxPrPaperType(device.getNxPdPaperType());
                printRecord.setNxPrPaperCount(paperCount);
                printRecord.setNxPrBillTotal(printFee);  // 打印费用（纸张数×单价）
                printRecord.setNxPrBillTradeNo(tradeNo);
                printRecord.setNxPrBillDate(new java.util.Date());
                printRecord.setNxPrPrintTime(new java.util.Date());
                printRecord.setNxPrPrintStatus(1); // 打印成功
                printRecord.setNxPrOperatorId(userId);
                
                // 备注区分体验和正式打印
                if (nxDistributerEntity.getNxDistributerType() == -1) {
                    printRecord.setNxPrRemark("【体验】单价=" + actualPricePerSheet + "元/张，打印" + paperCount + "张，扣减" + deductPoints.intValue() + "点");
                } else {
                    printRecord.setNxPrRemark("自助打印，单价=" + actualPricePerSheet + "元/张，打印" + paperCount + "张，费用" + printFee + "元，扣减" + deductPoints.intValue() + "点");
                }
                
                nxMachinePrintRecordService.save(printRecord);
                
                System.out.println("自助打印记录已保存：打印费=" + printFee + "元，扣减点数=" + deductPoints + 
                    "，剩余纸张=" + remainingPaper + "张");
                
                // 返回打印详情
                return R.ok()
                    .put("billId", nxDepartmentBill.getNxDepartmentBillId())
                    .put("printFee", printFee.doubleValue())
                    .put("deductPoints", deductPoints.intValue())
                    .put("remainingPoints", restPoints.intValue())
                    .put("remainingPaper", remainingPaper);
                    
            } catch (Exception e) {
                System.out.println("打印机操作失败：" + e.getMessage());
                return R.error("打印机操作失败：" + e.getMessage());
            }
        }

        // 普通小程序打印返回
        return R.ok()
            .put("billId", nxDepartmentBill.getNxDepartmentBillId())
            .put("deductPoints", deductPoints.intValue())
            .put("remainingPoints", restPoints.intValue());
    }


    @Transactional
    @RequestMapping(value = "/disGetUnPayAccountBills/{id}")
    @ResponseBody
    public R disGetUnPayAccountBills(@PathVariable Integer id) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", id);
        map.put("settleType", 0);
        map.put("equalStatus", 0);
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(map);
        return R.ok().put("data", billEntityList);
    }

    @Transactional
    @RequestMapping(value = "/disGetUnPayAccountBillsGb/{id}")
    @ResponseBody
    public R disGetUnPayAccountBillsGb(@PathVariable Integer id) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", id);
        map.put("gbDepFatherIdNotEqual", -1);
        map.put("status", 3);
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(map);
        return R.ok().put("data", billEntityList);
    }


    @ResponseBody
    @RequestMapping(value = "/restrauntCashPayLaodu", method = RequestMethod.POST)
    public R restrauntCashPayLaodu(@RequestBody NxDepartmentBillEntity billEntity) {
        System.out.println("billl" + billEntity);

        //转换总金额
        String nxRbTotal = billEntity.getNxDbTotal();
        Double aDouble = Double.parseDouble(nxRbTotal) * 100;
        int i = aDouble.intValue();
        String s1 = String.valueOf(i);

        //订单号
        String tradeNo = CommonUtils.generateOutTradeNo();
        //餐馆支付配置
        MyWxLaoduPayConfig config = new MyWxLaoduPayConfig();
        SortedMap<String, String> params = new TreeMap<>();
        params.put("appid", config.getAppID());
        params.put("mch_id", config.getMchID());
        params.put("nonce_str", CommonUtils.generateUUID());
        params.put("body", "订单支付");
        params.put("out_trade_no", tradeNo);
        params.put("fee_type", "CNY");
        params.put("total_fee", s1);
        params.put("spbill_create_ip", "101.42.222.149");
        params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/nxdepartmentbill/notify");
        params.put("trade_type", "JSAPI");
        params.put("openid", billEntity.getNxUserOpenId());

        //map转xml
        try {

            WXPay wxpay = new WXPay(config);
            long time = System.currentTimeMillis();
            String tString = String.valueOf(time / 1000);
            Map<String, String> resp = wxpay.unifiedOrder(params);
            System.out.println(resp);
            SortedMap<String, String> reMap = new TreeMap<>();
            reMap.put("appId", config.getAppID());
            reMap.put("nonceStr", resp.get("nonce_str"));
            reMap.put("package", "prepay_id=" + resp.get("prepay_id"));
            reMap.put("signType", "MD5");
            reMap.put("timeStamp", tString);
            String s = WxPayUtils.creatSign(reMap, config.getKey());
            reMap.put("paySign", s);

            billEntity.setNxDbWxOutTradeNo(tradeNo);
            nxDepartmentBillService.update(billEntity);

            return R.ok().put("map", reMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.ok();
    }


    @ResponseBody
    @RequestMapping(value = "/restrauntCashPaySun", method = RequestMethod.POST)
    public R restrauntCashPaySun(@RequestBody NxDepartmentBillEntity billEntity) {
        System.out.println("billl" + billEntity);

        //转换总金额
        String nxRbTotal = billEntity.getNxDbPayCash();
        Double aDouble = Double.parseDouble(nxRbTotal) * 100;
        int i = aDouble.intValue();
        String s1 = String.valueOf(i);

        //订单号
        String tradeNo = CommonUtils.generateOutTradeNo();
        //餐馆支付配置
        MyWxShixianliliPayConfig config = new MyWxShixianliliPayConfig();
        SortedMap<String, String> params = new TreeMap<>();
        params.put("appid", config.getAppID());
        params.put("mch_id", config.getMchID());
        params.put("nonce_str", CommonUtils.generateUUID());
        params.put("body", "订单支付");
        params.put("out_trade_no", tradeNo);
        params.put("fee_type", "CNY");
        params.put("total_fee", s1);
        params.put("spbill_create_ip", "101.42.222.149");
        params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/nxdepartmentbill/notifySun");
        params.put("trade_type", "JSAPI");
        params.put("openid", billEntity.getNxUserOpenId());

        //map转xml
        try {

            WXPay wxpay = new WXPay(config);
            long time = System.currentTimeMillis();
            String tString = String.valueOf(time / 1000);
            Map<String, String> resp = wxpay.unifiedOrder(params);
            System.out.println(resp);
            SortedMap<String, String> reMap = new TreeMap<>();
            reMap.put("appId", config.getAppID());
            reMap.put("nonceStr", resp.get("nonce_str"));
            reMap.put("package", "prepay_id=" + resp.get("prepay_id"));
            reMap.put("signType", "MD5");
            reMap.put("timeStamp", tString);
            String s = WxPayUtils.creatSign(reMap, config.getKey());
            reMap.put("paySign", s);

            billEntity.setNxDbWxOutTradeNo(tradeNo);
            nxDepartmentBillService.update(billEntity);

            return R.ok().put("map", reMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.ok();
    }



    @ResponseBody
    @RequestMapping(value = "/restrauntCashPay", method = RequestMethod.POST)
    public R restrauntCashPay(@RequestBody NxDepartmentBillEntity billEntity) {

        Integer nxDbDisId = billEntity.getNxDbDisId();
        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(nxDbDisId);
        String nxDistributerPayUrl = nxDistributerEntity.getNxDistributerPayUrl();

        System.out.println("billl" + billEntity);

        //转换总金额
        String nxRbTotal = billEntity.getNxDbTotal();
        Double aDouble = Double.parseDouble(nxRbTotal) * 100;
        int i = aDouble.intValue();
        String s1 = String.valueOf(i);

        //订单号
        String tradeNo = CommonUtils.generateOutTradeNo();
        //餐馆支付配置
        MyWxShixianliliPayConfig config = new MyWxShixianliliPayConfig();

        SortedMap<String, String> params = new TreeMap<>();
        params.put("appid", config.getAppID());
        params.put("mch_id", config.getMchID());
        params.put("nonce_str", CommonUtils.generateUUID());
        params.put("body", "订单支付");
        params.put("out_trade_no", tradeNo);
        params.put("fee_type", "CNY");
        params.put("total_fee", s1);
        params.put("spbill_create_ip", "101.42.222.149");
        params.put("notify_url", "https://grainservice.club:8443/nongxinle/api/nxdepartmentbill/notify");
        params.put("trade_type", "JSAPI");
        params.put("openid", billEntity.getNxUserOpenId());

        //map转xml
        try {

            WXPay wxpay = new WXPay(config);
            long time = System.currentTimeMillis();
            String tString = String.valueOf(time / 1000);
            Map<String, String> resp = wxpay.unifiedOrder(params);
            System.out.println(resp);
            SortedMap<String, String> reMap = new TreeMap<>();
            reMap.put("appId", config.getAppID());
            reMap.put("nonceStr", resp.get("nonce_str"));
            reMap.put("package", "prepay_id=" + resp.get("prepay_id"));
            reMap.put("signType", "MD5");
            reMap.put("timeStamp", tString);
            String s = WxPayUtils.creatSign(reMap, config.getKey());
            reMap.put("paySign", s);

            billEntity.setNxDbWxOutTradeNo(tradeNo);
            nxDepartmentBillService.update(billEntity);

            return R.ok().put("map", reMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.ok();
    }


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
                NxDepartmentBillEntity billEntity = nxDepartmentBillService.queryDepartBillByTradeNo(ordersSn);
                billEntity.setNxDbStatus(4);
                nxDepartmentBillService.update(billEntity);

                Integer nxDbDepId = billEntity.getNxDbDepId();
                NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(nxDbDepId);

//                savePromotion(departmentEntity);

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
     * @Title: callBack
     * @Description: 支付完成的回调函数
     * @param:
     * @return:
     */
    @RequestMapping("/notifySun")
    public String callBackSun(HttpServletRequest request, HttpServletResponse response) {
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
                System.out.println("===notifySun===回调方法已经被调！！！");
                String ordersSn = notifyMap.get("out_trade_no");// 商户订单号
                NxDepartmentBillEntity billEntity = nxDepartmentBillService.queryDepartBillByTradeNo(ordersSn);
                billEntity.setNxDbStatus(4);
                nxDepartmentBillService.update(billEntity);

                Integer billId = billEntity.getNxDepartmentBillId();
                Map<String, Object> orderQueryMap = new HashMap<>();

                // 获取bill的支付积分
                String payPointsStr = billEntity.getNxDbPayPoints();
                BigDecimal payPoints = (payPointsStr != null && !payPointsStr.trim().isEmpty())
                        ? new BigDecimal(payPointsStr) : BigDecimal.ZERO;

                // 获取部门当前积分
                NxDepartmentEntity depInfo = nxDepartmentService.queryDepInfo(billEntity.getNxDbDepFatherId());
                String departmentPointsStr = depInfo.getNxDepartmentPoints();
                BigDecimal departmentPoints = (departmentPointsStr != null && !departmentPointsStr.trim().isEmpty())
                        ? new BigDecimal(departmentPointsStr) : BigDecimal.ZERO;

                // 扣除部门积分
                BigDecimal newDepartmentPoints = departmentPoints.subtract(payPoints).setScale(1, BigDecimal.ROUND_HALF_UP);
                depInfo.setNxDepartmentPoints(newDepartmentPoints.toString());
                nxDepartmentService.update(depInfo);



                System.out.println("fnirsunoreder" + orderQueryMap);
                orderQueryMap.put("depFatherId", billEntity.getNxDbDepFatherId());
                orderQueryMap.put("equalStatus", -1);
                orderQueryMap.put("orderDisId", billEntity.getNxDbDisId());
                List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(orderQueryMap);
                if(nxDepartmentOrdersEntities.size() > 0){
                    for(NxDepartmentOrdersEntity ordersEntity: nxDepartmentOrdersEntities){
                        ordersEntity.setNxDoStatus(0);
                        nxDepartmentOrdersService.update(ordersEntity);

                        // 查询并更新与该订单关联的库存扣减记录（reduce）状态为0，并更新库存批次剩余数量为0
                        Map<String, Object> reduceQueryMap = new HashMap<>();
                        reduceQueryMap.put("orderId", ordersEntity.getNxDepartmentOrdersId());
                        List<NxDistributerGoodsShelfStockReduceEntity> reduceList = nxDistributerGoodsShelfStockReduceService.queryReduceListByParams(reduceQueryMap);
                        if (reduceList != null && !reduceList.isEmpty()) {
                            for (NxDistributerGoodsShelfStockReduceEntity reduce : reduceList) {
                                // 只更新状态为 -1 的记录
                                if (reduce.getNxDgssrStatus() != null && reduce.getNxDgssrStatus() == -1) {
                                    reduce.setNxDgssrStatus(0);
                                    nxDistributerGoodsShelfStockReduceService.update(reduce);

                                    // 更新库存批次的剩余数量为0，这样别人就不能再订购这个库存了
                                    Integer stockId = reduce.getNxDgssrNxStockId();
                                    if (stockId != null) {
                                        NxDistributerGoodsShelfStockEntity stock = nxDistributerGoodsShelfStockService.queryObject(stockId);
                                        if (stock != null) {

                                            Integer nxDgssNxDepartmentFatherId = stock.getNxDgssNxDepartmentFatherId();
                                            NxDepartmentEntity supplierDepartmentEntity = nxDepartmentService.queryObject(nxDgssNxDepartmentFatherId);

                                            //说明supplierDepartmentEntity 的货品卖出了，可以把他等待积分stock.getNxDgssSubtotal()改为积分了。
                                            if (supplierDepartmentEntity != null) {
                                                try {
                                                    // 获取库存金额（等待积分）
                                                    String stockSubtotal = stock.getNxDgssSubtotal();
                                                    if (stockSubtotal != null && !stockSubtotal.isEmpty()) {
                                                        BigDecimal stockSubtotalDecimal = new BigDecimal(stockSubtotal);
                                                        
                                                        // 获取当前积分和等待积分
                                                        String currentPoints = supplierDepartmentEntity.getNxDepartmentPoints();
                                                        String currentWaitingPoints = supplierDepartmentEntity.getNxDepartmentWaitingPoints();
                                                        
                                                        // 转换为 BigDecimal，如果为空则默认为0
                                                        BigDecimal currentPointsDecimal = (currentPoints != null && !currentPoints.isEmpty()) 
                                                            ? new BigDecimal(currentPoints) : BigDecimal.ZERO;
                                                        BigDecimal currentWaitingPointsDecimal = (currentWaitingPoints != null && !currentWaitingPoints.isEmpty()) 
                                                            ? new BigDecimal(currentWaitingPoints) : BigDecimal.ZERO;
                                                        
                                                        // 从等待积分中减去库存金额
                                                        BigDecimal newWaitingPoints = currentWaitingPointsDecimal.subtract(stockSubtotalDecimal);
                                                        if (newWaitingPoints.compareTo(BigDecimal.ZERO) < 0) {
                                                            newWaitingPoints = BigDecimal.ZERO; // 确保不为负数
                                                        }
                                                        
                                                        // 将库存金额加到积分中
                                                        BigDecimal newPoints = currentPointsDecimal.add(stockSubtotalDecimal).setScale(1, BigDecimal.ROUND_HALF_UP);
                                                        
                                                        // 更新部门实体
                                                        supplierDepartmentEntity.setNxDepartmentWaitingPoints(newWaitingPoints.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                                        supplierDepartmentEntity.setNxDepartmentPoints(newPoints.toString());
                                                        nxDepartmentService.update(supplierDepartmentEntity);
                                                  

                                                        // 保存返还积分和返还积分时间到库存实体
                                                        stock.setNxDgssReturnPoints(stockSubtotal);
                                                        stock.setNxDgssReturnPointsTime(DateUtils.formatWhatYearDayTime(0)); // 精确到分钟
                                                        stock.setNxDgssStatus(0);
                                                        nxDistributerGoodsShelfStockService.update(stock);

                                                        System.out.println("[callBackSun] 更新库存返还积分: 返还积分 " + newPoints
                                                            + ", 返还积分时间: " + stock.getNxDgssReturnPointsTime());
                                                    }
                                                } catch (Exception e) {
                                                    // 记录错误但不影响主流程
                                                    e.printStackTrace();
                                                }
                                            }

                                            stock.setNxDgssRestWeight("0");
                                            stock.setNxDgssRestSubtotal("0");
                                            nxDistributerGoodsShelfStockService.update(stock);
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
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


    @RequestMapping(value = "/comReceivedepBill", method = RequestMethod.POST)
    @ResponseBody
    public R comReceivedepBill(@RequestBody NxDepartmentBillEntity bill) {
        bill.setNxDbStatus(0);
        nxDepartmentBillService.update(bill);
        return R.ok();
    }


    @RequestMapping(value = "/deleteBillReturn/{id}")
    @ResponseBody
    public R deleteBillReturn(@PathVariable Integer id) {
        Map<String, Object> map = new HashMap<>();
        map.put("returnBillId", id);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        for (NxDepartmentOrdersEntity orders : ordersEntities) {
            orders.setNxDoReturnWeight(null);
            orders.setNxDoReturnSubtotal(null);
            orders.setNxDoReturnStatus(null);
            nxDepartmentOrdersService.update(orders);
        }

        nxDepartmentBillService.delete(id);
        return R.ok();
    }
    //

    @RequestMapping(value = "/deleteBillAgain/{billId}")
    @ResponseBody
    public R deleteBillAgain(@PathVariable Integer billId) {

        //order
        Map<String, Object> map = new HashMap<>();
        map.put("billId", billId);
        List<NxDepartmentOrderHistoryEntity> ordersEntities = historyService.queryDisHistoryOrdersByParams(map);
        for (NxDepartmentOrderHistoryEntity orders : ordersEntities) {
            //迁移
            historyService.moveOrderFromHistory(orders);  // ✅ 迁移
            NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orders.getNxDepartmentOrdersId());
            ordersEntity.setNxDoStatus(2);
            ordersEntity.setNxDoBillId(null);
            nxDepartmentOrdersService.update(ordersEntity);

        }

        nxDepartmentBillService.delete(billId);
        return R.ok();

    }

    @RequestMapping(value = "/deleteBillAgainGb/{billId}")
    @ResponseBody
    public R deleteBillAgainGb(@PathVariable Integer billId) {

        //order
        Map<String, Object> map = new HashMap<>();
        map.put("billId", billId);
        List<NxDepartmentOrderHistoryEntity> ordersEntities = historyService.queryDisHistoryOrdersByParams(map);
        for (NxDepartmentOrderHistoryEntity historyEntity : ordersEntities) {

            historyService.moveOrderFromHistory(historyEntity);

            NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(historyEntity.getNxDepartmentOrdersId());
            ordersEntity.setNxDoStatus(2);
            ordersEntity.setNxDoBillId(null);
            nxDepartmentOrdersService.update(ordersEntity);
            ;

            Integer gbDepartmentOrderId = ordersEntity.getNxDoGbDepartmentOrderId();
            GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrderId);
            gbDepartmentOrdersEntity.setGbDoStatus(2);
            gbDepartmentOrdersEntity.setGbDoBillId(null);
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
        }

        NxDepartmentBillEntity billEntity = nxDepartmentBillService.queryObject(billId);
        Integer nxDbGbDepartmentBillId = billEntity.getNxDbGbDepartmentBillId();
        GbDepartmentBillEntity gbDepartmentBillEntity = gbDepartmentBillService.queryObject(nxDbGbDepartmentBillId);
        if (gbDepartmentBillEntity.getGbDbUserCouponId() != null) {
            Integer gbDbUserCouponId = gbDepartmentBillEntity.getGbDbUserCouponId();
            NxGbDistibuterUserCouponEntity userCouponEntity = nxGbDistibuterUserCouponService.queryObject(gbDbUserCouponId);
            userCouponEntity.setNxGducStatus(0);
            nxGbDistibuterUserCouponService.update(userCouponEntity);
        }
        gbDepartmentBillService.delete(nxDbGbDepartmentBillId);
        nxDepartmentBillService.delete(billId);
        return R.ok();

    }

    @RequestMapping(value = "/deleteBillGb/{billId}")
    @ResponseBody
    public R deleteBillGb(@PathVariable Integer billId) {

        NxDepartmentBillEntity billEntity = nxDepartmentBillService.queryObject(billId);
        Integer depId = billEntity.getNxDbGbDepId();
        Map<String, Object> mapO = new HashMap<>();
        mapO.put("depId", depId);
        mapO.put("status", 3);
        mapO.put("orderDisId", billEntity.getNxDbDisId());
        System.out.println("mapoooo" + mapO);
        List<NxDepartmentOrdersEntity> ordersEntities1 = nxDepartmentOrdersService.queryDisOrdersByParams(mapO);
        if (ordersEntities1.size() > 0) {
            return R.error(-1, "删除账单的订单将和现有订单混在一起");
        } else {
            //order
            Map<String, Object> map = new HashMap<>();
            map.put("billId", billId);
            List<NxDepartmentOrderHistoryEntity> ordersEntities = historyService.queryDisHistoryOrdersByParams(map);
            for (NxDepartmentOrderHistoryEntity historyEntity : ordersEntities) {

                historyService.moveOrderFromHistory(historyEntity);

                NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(historyEntity.getNxDepartmentOrdersId());
                ordersEntity.setNxDoStatus(2);
                ordersEntity.setNxDoBillId(null);
                nxDepartmentOrdersService.update(ordersEntity);

                Integer gbDepartmentOrderId = historyEntity.getNxDoGbDepartmentOrderId();
                GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrderId);
                gbDepartmentOrdersEntity.setGbDoStatus(1);
                gbDepartmentOrdersEntity.setGbDoBuyStatus(2);
                gbDepartmentOrdersEntity.setGbDoBillId(null);
                gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);

            }
            Integer nxDbGbDepartmentBillId = billEntity.getNxDbGbDepartmentBillId();
            GbDepartmentBillEntity gbDepartmentBillEntity = gbDepartmentBillService.queryObject(nxDbGbDepartmentBillId);
            if (gbDepartmentBillEntity.getGbDbUserCouponId() != null) {
                Integer gbDbUserCouponId = gbDepartmentBillEntity.getGbDbUserCouponId();
                NxGbDistibuterUserCouponEntity userCouponEntity = nxGbDistibuterUserCouponService.queryObject(gbDbUserCouponId);
                userCouponEntity.setNxGducStatus(0);
                nxGbDistibuterUserCouponService.update(userCouponEntity);
            }
            System.out.println("debllllllllllll");
            gbDepartmentBillService.delete(nxDbGbDepartmentBillId);
            nxDepartmentBillService.delete(billId);

            return R.ok();
        }

    }

    @Transactional
    @RequestMapping(value = "/deleteBill/{billId}")
    @ResponseBody
    public R deleteBill(@PathVariable Integer billId) {

        NxDepartmentBillEntity billEntity = nxDepartmentBillService.queryObject(billId);
        Integer depId = billEntity.getNxDbDepId();
        Map<String, Object> mapO = new HashMap<>();
        mapO.put("depId", depId);
        mapO.put("status", 3);
        mapO.put("orderDisId", billEntity.getNxDbDisId());
        List<NxDepartmentOrdersEntity> ordersEntities1 = nxDepartmentOrdersService.queryDisOrdersByParams(mapO);
        if (ordersEntities1.size() > 0) {
            return R.error(-1, "删除账单的订单将和现有订单混在一起");
        } else {
            //order
            Map<String, Object> map = new HashMap<>();
            map.put("billId", billId);
            List<NxDepartmentOrderHistoryEntity> ordersEntities = historyService.queryDisHistoryOrdersByParams(map);
            for (NxDepartmentOrderHistoryEntity orders : ordersEntities) {
                //迁移
                historyService.moveOrderFromHistory(orders);  // ✅ 迁移
                NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orders.getNxDepartmentOrdersId());
                ordersEntity.setNxDoStatus(2);
                ordersEntity.setNxDoBillId(null);
                nxDepartmentOrdersService.update(ordersEntity);

            }

            nxDepartmentBillService.delete(billId);
            return R.ok();
        }

    }


    @RequestMapping(value = "/finishBill/{billId}")
    @ResponseBody
    public R finishBill(@PathVariable Integer billId) {
        //order
        NxDepartmentBillEntity billEntity = nxDepartmentBillService.queryObject(billId);
        GbDepartmentBillEntity gbDepartmentBillEntity = gbDepartmentBillService.queryDepartBillByTsxTradeNo(billEntity.getNxDbTradeNo());
        gbDepartmentBillEntity.setGbDbStatus(4);
        gbDepartmentBillService.update(gbDepartmentBillEntity);

        return R.ok();
    }


    @RequestMapping(value = "/printBillMoreTimes/{billId}")
    @ResponseBody
    public R printBillMoreTimes(@PathVariable Integer billId) {
        NxDepartmentBillEntity billEntity = nxDepartmentBillService.queryObject(billId);
        billEntity.setNxDbPrintTimes(billEntity.getNxDbPrintTimes() + 1);
        nxDepartmentBillService.update(billEntity);
        return R.ok();
    }


    @RequestMapping(value = "/getBillApplysDetail/{billId}")
    @ResponseBody
    public R getBillApplysDetail(@PathVariable Integer billId) {

        System.out.println("getBillApplysDetail" + billId);
        NxDepartmentBillEntity billEntity = nxDepartmentBillService.querySalesBillApplys(billId);
        return R.ok().put("data", billEntity);
    }

    @RequestMapping(value = "/getReturnBillApplys/{billId}")
    @ResponseBody
    public R getReturnBillApplys(@PathVariable Integer billId) {

        System.out.println("rerrururuuuruur" + billId);
        NxDepartmentBillEntity billEntity = nxDepartmentBillService.queryReturnBillOrdersByBillId(billId);
        return R.ok().put("data", billEntity);
    }


    @RequestMapping(value = "/saveAccountReturnBill", method = RequestMethod.POST)
    @ResponseBody
    public R saveAccountReturnBill(@RequestBody NxDepartmentBillEntity bill) {
        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(bill.getNxDbDisId());
        String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
        String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();
        bill.setNxDbStatus(0);
        bill.setNxDbDate(formatWhatDay(0));
        bill.setNxDbTime(formatWhatYearDayTime(0));
        bill.setNxDbMonth(formatWhatMonth(0));
        bill.setNxDbWeek(getWeekOfYear(0).toString());
        bill.setNxDbDay(getWeek(0));
        bill.setNxDbTradeNo(s);
        bill.setNxDbYear(formatWhatYear(0));
        nxDepartmentBillService.save(bill);
        Integer nxDepartmentBillId = bill.getNxDepartmentBillId();

        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = bill.getNxDepartmentOrdersEntities();

        for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
            orders.setNxDoReturnBillId(nxDepartmentBillId);
            orders.setNxDoReturnStatus(1);
            nxDepartmentOrdersService.update(orders);
        }

        return R.ok();
    }

    /**
     * 结账
     *
     * @param bills
     * @return
     */
    @RequestMapping(value = "/settleDepBills", method = RequestMethod.POST)
    @ResponseBody
    public R settleDepBills(@RequestBody List<NxDepartmentBillEntity> bills) {
        for (NxDepartmentBillEntity bill : bills) {
            bill.setNxDbStatus(1);
            nxDepartmentBillService.update(bill);
            Map<String, Object> map = new HashMap<>();
            map.put("billId", bill.getNxDepartmentBillId());
            map.put("depId", bill.getNxDbDepFatherId());
            map.put("orderDisId", bill.getNxDbDisId());
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
            if (ordersEntities.size() > 0) {
                for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                    ordersEntity.setNxDoStatus(4);
                    nxDepartmentOrdersService.update(ordersEntity);
                }
            }
        }
        return R.ok();
    }

    @RequestMapping(value = "/settleDepBillsGb", method = RequestMethod.POST)
    @ResponseBody
    public R settleDepBillsGb(@RequestBody List<NxDepartmentBillEntity> bills) {
        for (NxDepartmentBillEntity bill : bills) {
            NxDepartmentBillEntity billEntity = nxDepartmentBillService.queryObject(bill.getNxDepartmentBillId());

            billEntity.setNxDbStatus(1);
            nxDepartmentBillService.update(billEntity);
            Map<String, Object> map = new HashMap<>();
            map.put("billId", billEntity.getNxDepartmentBillId());
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
            if (ordersEntities.size() > 0) {
                for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                    ordersEntity.setNxDoStatus(4);
                    nxDepartmentOrdersService.update(ordersEntity);
                }
            }
            //updateGbBill
            Integer nxDbGbDepartmentBillId = bill.getNxDbGbDepartmentBillId();
            GbDepartmentBillEntity gbDepartmentBillEntity = gbDepartmentBillService.queryObject(nxDbGbDepartmentBillId);
            gbDepartmentBillEntity.setGbDbStatus(1);
            gbDepartmentBillService.update(gbDepartmentBillEntity);
            Map<String, Object> mapB = new HashMap<>();
            mapB.put("billId", gbDepartmentBillEntity.getGbDepartmentBillId());
            List<GbDistributerPurchaseBatchEntity> gbDistributerPurchaseBatchEntities = gbDisPurBatchService.queryDisPurchaseBatch(mapB);
            if(gbDistributerPurchaseBatchEntities.size() > 0){
                for(GbDistributerPurchaseBatchEntity batchEntity: gbDistributerPurchaseBatchEntities){
                    batchEntity.setGbDpbStatus(getGbDisPurchaseBatchDisUserFinishPay());
                    batchEntity.setGbDpbFinishFullTime(formatFullTime());
                    gbDisPurBatchService.update(batchEntity);
                    Map<String, Object> mapPG = new HashMap<>();
                    mapPG.put("batchId", batchEntity.getGbDistributerPurchaseBatchId());
                    List<GbDistributerPurchaseGoodsEntity> gbDistributerPurchaseGoodsEntities = gbDistributerPurchaseGoodsService.queryOnlyPurGoods(mapPG);
                    if(gbDistributerPurchaseGoodsEntities.size() > 0){
                        for(GbDistributerPurchaseGoodsEntity purchaseGoodsEntity: gbDistributerPurchaseGoodsEntities){
                            purchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusPayFinish());
                            gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                        }
                    }

                }
            }

            Map<String, Object> mapGO = new HashMap<>();
            mapGO.put("billId", gbDepartmentBillEntity.getGbDepartmentBillId());
            List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(mapGO);
            if (gbDepartmentOrdersEntities.size() > 0) {
                for (GbDepartmentOrdersEntity gbDepartmentOrdersEntity : gbDepartmentOrdersEntities) {
//                    gbDepartmentOrdersEntity.setGbDoStatus(4);
                    gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderBuyStatusHavePayFinish());
                    gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
                }
            }

        }
        return R.ok();
    }

    /**
     * 批发商获取未结账账单
     *
     * @param depId kehu_id
     * @return 订单列表
     */
    @RequestMapping(value = "/disGetUnSettleAccountBills/{depId}")
    @ResponseBody
    public R disGetUnSettleAccountBills(@PathVariable Integer depId) {

        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();


        map.put("depFatherId", depId);
        map.put("month", formatWhatMonth(0));
        map.put("year", formatWhatYear(0));
        map.put("status", 1);
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsListByParams(map);
        Map<String, Object> mapOne = new HashMap<>();
        mapOne.put("month", formatWhatMonth(0));
        mapOne.put("year", formatWhatYear(0));
        mapOne.put("arr", billEntityList);
        mapOne.put("choice", false);
        list.add(mapOne);

        map.put("month", getLastMonth());
        List<NxDepartmentBillEntity> billEntityListLast = nxDepartmentBillService.queryBillsListByParams(map);
        Map<String, Object> mapLst = new HashMap<>();
        mapLst.put("month", getLastMonth());
        mapLst.put("arr", billEntityListLast);
        mapLst.put("choice", false);
        list.add(mapLst);


        map.put("depFatherId", depId);
        map.put("equalStatus", 0);
        map.put("month", getLastTwoMonth());

        List<NxDepartmentBillEntity> billEntityListTwo = nxDepartmentBillService.queryBillsListByParams(map);
        Map<String, Object> mapTwo = new HashMap<>();
        mapTwo.put("month", getLastTwoMonth());
        mapTwo.put("arr", billEntityListTwo);
        mapTwo.put("choice", false);
        list.add(mapTwo);

        System.out.println("billlls");
        return R.ok().put("data", list);

    }

    @RequestMapping(value = "/disGetGbDistributerBills")
    @ResponseBody
    public R disGetGbDistributerBills(Integer nxDisId, Integer gbDisId) {

        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();

        map.put("disId", nxDisId);
        map.put("gbDisId", gbDisId);
        map.put("month", formatWhatMonth(0));
        map.put("year", formatWhatYear(0));
        System.out.println("mapappapapapp" + map);
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsListByParams(map);
        Map<String, Object> mapOne = new HashMap<>();
        mapOne.put("month", formatWhatMonth(0));
        mapOne.put("year", formatWhatYear(0));
        mapOne.put("arr", billEntityList);
        mapOne.put("choice", false);
        list.add(mapOne);

        map.put("month", getLastMonth());
        List<NxDepartmentBillEntity> billEntityListLast = nxDepartmentBillService.queryBillsListByParams(map);
        Map<String, Object> mapLst = new HashMap<>();
        mapLst.put("month", getLastMonth());
        mapLst.put("arr", billEntityListLast);
        mapLst.put("choice", false);
        list.add(mapLst);

        map.put("month", getLastTwoMonth());

        List<NxDepartmentBillEntity> billEntityListTwo = nxDepartmentBillService.queryBillsListByParams(map);
        Map<String, Object> mapTwo = new HashMap<>();
        mapTwo.put("month", getLastTwoMonth());
        mapTwo.put("arr", billEntityListTwo);
        mapTwo.put("choice", false);
        list.add(mapTwo);

        System.out.println("billlls");
        return R.ok().put("data", list);

    }

    @RequestMapping(value = "/disGetUnSettleAccountBillsGb", method = RequestMethod.POST)
    @ResponseBody
    public R disGetUnSettleAccountBillsGb(Integer gbDisId, Integer nxDisId) {

        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();

        map.put("disId", nxDisId);
        map.put("gbDisId", gbDisId);
        map.put("month", formatWhatMonth(0));
        map.put("year", formatWhatYear(0));
        map.put("status", 1);
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsListByParams(map);
        Map<String, Object> mapOne = new HashMap<>();
        mapOne.put("month", formatWhatMonth(0));
        mapOne.put("year", formatWhatYear(0));
        mapOne.put("arr", billEntityList);
        mapOne.put("choice", false);
        list.add(mapOne);

        map.put("month", getLastMonth());
        List<NxDepartmentBillEntity> billEntityListLast = nxDepartmentBillService.queryBillsListByParams(map);
        Map<String, Object> mapLst = new HashMap<>();
        mapLst.put("month", getLastMonth());
        mapLst.put("arr", billEntityListLast);
        mapLst.put("choice", false);
        list.add(mapLst);


        map.put("equalStatus", 0);
        map.put("month", getLastTwoMonth());

        List<NxDepartmentBillEntity> billEntityListTwo = nxDepartmentBillService.queryBillsListByParams(map);
        Map<String, Object> mapTwo = new HashMap<>();
        mapTwo.put("month", getLastTwoMonth());
        mapTwo.put("arr", billEntityListTwo);
        mapTwo.put("choice", false);
        list.add(mapTwo);

        System.out.println("billlls");
        return R.ok().put("data", list);

    }


    @RequestMapping(value = "/gbDisGetAllAccountBillsGb", method = RequestMethod.POST)
    @ResponseBody
    public R gbDisGetAllAccountBillsGb(Integer gbDisId, Integer disId, Integer nxCommId, Integer nxDepFatherId) {

        Map<String, Object> stringObjectMap = disQueryAccountBillByMonthGb(disId, gbDisId, nxCommId, nxDepFatherId, 2);
        Map<String, Object> lastObjectMap = disQueryAccountBillByMonthGb(disId, gbDisId, nxCommId, nxDepFatherId, 1);
        Map<String, Object> lastTwoObjectMap = disQueryAccountBillByMonthGb(disId, gbDisId, nxCommId, nxDepFatherId, 0);
        List<Map<String, Object>> dataMap = new ArrayList<>();
        dataMap.add(lastTwoObjectMap);
        dataMap.add(lastObjectMap);
        dataMap.add(stringObjectMap);


        //查询总账款金额
//
        //本月的账单
        Map<String, Object> map = new HashMap<>();
        map.put("nx_DB_dis_id", disId);
        map.put("nx_DB_gb_dis_id", gbDisId);
        map.put("nx_DB_nx_community_id", nxCommId);
        map.put("nx_DB_status", 0);

        Map<String, Object> mapz = new HashMap<>();
        mapz.put("nx_DB_dis_id", disId);
        mapz.put("nx_DB_gb_dis_id", -1);
        mapz.put("nx_DB_nx_community_id", -1);
        mapz.put("nx_DB_dep_father_id", nxDepFatherId);
        mapz.put("nx_DB_status", 0);
        Map<String, Object> params = new HashMap<>();
        params.put("map", map);
        params.put("map1", mapz);
        int total = nxDepartmentBillService.queryCountBindMap(params);
        double subtotal = 0.0;
        if (total > 0) {
            subtotal = nxDepartmentBillService.querySubtoalBindMap(params);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("arr", dataMap);
        resultMap.put("total", new BigDecimal(subtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        return R.ok().put("data", resultMap);
    }

    @RequestMapping(value = "/sellerAndBuyerGetAccountBillsGb", method = RequestMethod.POST)
    @ResponseBody
    public R sellerAndBuyerGetAccountBillsGb(Integer gbDisId, Integer disId, Integer nxCommId) {

        Map<String, Object> stringObjectMap = queryAccountBillByMonthGb(disId, gbDisId, nxCommId, 0);
        Map<String, Object> lastObjectMap = queryAccountBillByMonthGb(disId, gbDisId, nxCommId, 1);
        Map<String, Object> lastTwoObjectMap = queryAccountBillByMonthGb(disId, gbDisId, nxCommId, 2);
        List<Map<String, Object>> dataMap = new ArrayList<>();
        dataMap.add(lastTwoObjectMap);
        dataMap.add(lastObjectMap);
        dataMap.add(stringObjectMap);


        //查询总账款金额
        Map<String, Object> map = new HashMap<>();
        map.put("gbDisId", gbDisId);
        map.put("disId", disId);
        map.put("nxCommId", nxCommId);
        map.put("equalStatus", 0);
        System.out.println("subtma" + map);
        int total = nxDepartmentBillService.queryTotalByParams(map);
        Double subtotal = 0.0;
        if (total > 0) {
            subtotal = nxDepartmentBillService.queryBillCostSubtotalByParams(map);
//            subtotal = nxDepartmentBillService.queryTotal(map);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("arr", dataMap);
        resultMap.put("total", new BigDecimal(subtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        return R.ok().put("data", resultMap);
    }


    @RequestMapping(value = "/sellerAndBuyerGetAccountBills", method = RequestMethod.POST)
    @ResponseBody
    public R sellerAndBuyerGetAccountBills(Integer depFatherId, Integer disId) {
        System.out.println("getabilllss");
        Map<String, Object> lastTwoObjectMap = queryAccountBillByMonth(disId, depFatherId, 2);
        Map<String, Object> lastObjectMap = queryAccountBillByMonth(disId, depFatherId, 1);
        Map<String, Object> stringObjectMap = queryAccountBillByMonth(disId, depFatherId, 0);

        List<Map<String, Object>> dataMap = new ArrayList<>();
        dataMap.add(stringObjectMap);
        dataMap.add(lastObjectMap);
        dataMap.add(lastTwoObjectMap);


        //查询总账款金额
        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depFatherId);
        map.put("equalStatus", 0);
        map.put("dayuMonth", getLastTwoMonth());
        map.put("xiaoyuMonth", formatWhatMonth(0));
        System.out.println("mappsoosososoosso" + map);
        int wxCountAuto = nxDepartmentBillService.queryBillsCount(map);
        double aDouble = 0.0;
        if (wxCountAuto > 0) {
            aDouble = nxDepartmentBillService.queryBillSubtotalByParams(map);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("arr", dataMap);
        resultMap.put("total", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP));
        return R.ok().put("data", resultMap);
    }


    @RequestMapping(value = "/getBillApplysGbDep")
    @ResponseBody
    public R getBillApplysGbDep(Integer billId, Integer depFatherId) {

        System.out.println("whefeedepfiid" + depFatherId);
        //billRetrunNumber
        Integer count = nxDepartmentBillService.queryReturnNumberByBillId(billId);

        NxDepartmentBillEntity salesBill = nxDepartmentBillService.querySalesBillApplys(billId);

        Map<String, Object> map = new HashMap<>();
        map.put("billId", billId);
        System.out.println("mappabdiidididi" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);

        if (salesBill.getNxDbGbDepId().equals(salesBill.getNxDbGbDepFatherId())) {
            List<Map<String, Object>> mapList = new ArrayList<>();
            List<GbDepartmentEntity> entities = gbDepartmentService.querySubDepartments(depFatherId);
            if (entities.size() > 0) {
                for (GbDepartmentEntity dep : entities) {
                    Map<String, Object> mapDep = new HashMap<>();
                    mapDep.put("gbDepId", dep.getGbDepartmentId());
                    mapDep.put("depName", dep.getGbDepartmentName());
                    Map<String, Object> map1 = new HashMap<>();
                    map1.put("billId", billId);
                    map1.put("gbDepId", dep.getGbDepartmentId());
                    System.out.println("mapp111111" + map1);
                    List<NxDepartmentOrdersEntity> depOrders = nxDepartmentOrdersService.queryDisOrdersByParams(map1);
                    mapDep.put("depOrders", depOrders);
                    if (depOrders.size() > 0) {
                        mapList.add(mapDep);
                    }
                }

                Map<String, Object> map3 = new HashMap<>();
                map3.put("arr", mapList);
                map3.put("bill", salesBill);
                map3.put("returnNumber", count);
                return R.ok().put("data", map3);
            } else {
                Map<String, Object> map4 = new HashMap<>();
                map4.put("arr", ordersEntities);
                map4.put("bill", salesBill);
                map4.put("returnNumber", count);
                return R.ok().put("data", map4);
            }
        } else {
            Map<String, Object> map4 = new HashMap<>();
            map4.put("arr", ordersEntities);
            map4.put("bill", salesBill);
            map4.put("returnNumber", count);
            return R.ok().put("data", map4);
        }

    }

    @RequestMapping(value = "/getBillApplysGb")
    @ResponseBody
    public R getBillApplysGb(Integer billId, Integer depFatherId) {

        //billRetrunNumber
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("billId", billId);
        mapR.put("equalReturnStatus", 0);
        double toReturnSubtotal = 0.0;
        Integer count = nxDepartmentOrdersService.queryReturnOrderCount(mapR);
        if (count > 0) {
            toReturnSubtotal = nxDepartmentOrdersService.queryReturnSubtotal(mapR);
        }
        double haveReturnSubtotal = 0.0;
        mapR.put("equalReturnStatus", 1);
        Integer countR = nxDepartmentOrdersService.queryReturnOrderCount(mapR);
        if (countR > 0) {
            haveReturnSubtotal = nxDepartmentOrdersService.queryReturnSubtotal(mapR);
        }

        System.out.println("gbbgbgbgorororooror" + depFatherId);

        NxDepartmentBillEntity salesBill = nxDepartmentBillService.querySalesBillApplys(billId);

        Map<String, Object> map = new HashMap<>();
        map.put("billId", billId);
        map.put("orderBy", "time");
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);

        List<Map<String, Object>> mapList = new ArrayList<>();
        List<GbDepartmentEntity> entities = gbDepartmentService.querySubDepartments(depFatherId);

        System.out.println("gbdnenen" + entities.size());
        if (entities.size() > 0) {
            for (GbDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("gbDepId", dep.getGbDepartmentId());
                mapDep.put("depName", dep.getGbDepartmentName());
                Map<String, Object> map1 = new HashMap<>();
                map1.put("billId", billId);
                map1.put("gbDepId", dep.getGbDepartmentId());
                List<NxDepartmentOrdersEntity> depOrders = nxDepartmentOrdersService.queryDisOrdersByParams(map1);

                mapDep.put("depOrders", depOrders);

                if (depOrders.size() > 0) {
                    mapList.add(mapDep);
                }
            }

            Map<String, Object> map3 = new HashMap<>();
            map3.put("arr", mapList);
            map3.put("bill", salesBill);
            map3.put("returnNumber", count);
            map3.put("toReturnSubtotal", toReturnSubtotal);
            map3.put("haveReturnSubtotal", haveReturnSubtotal);

            return R.ok().put("data", map3);
        } else {
            Map<String, Object> map4 = new HashMap<>();
            map4.put("arr", ordersEntities);
            map4.put("bill", salesBill);
            map4.put("returnNumber", count);
            map4.put("toReturnSubtotal", toReturnSubtotal);
            map4.put("haveReturnSubtotal", haveReturnSubtotal);

            return R.ok().put("data", map4);
        }


    }


    @RequestMapping(value = "/getBillApplys")
    @ResponseBody
    public R getBillApplys(Integer billId, Integer depFatherId) {

        System.out.println("billid=" + billId + "depFathtId==" + depFatherId);

        //billRetrunNumber
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("billId", billId);
        mapR.put("equalReturnStatus", 0);
        double toReturnSubtotal = 0.0;
//        Integer count = nxDepartmentOrdersService.queryReturnOrderCount(mapR);
        Integer count = historyService.queryReturnOrderCount(mapR);
        if (count > 0) {
            toReturnSubtotal = historyService.queryReturnSubtotal(mapR);
        }
        double haveReturnSubtotal = 0.0;
        mapR.put("equalReturnStatus", 1);
        Integer countR = historyService.queryReturnOrderCount(mapR);
        if (countR > 0) {
            haveReturnSubtotal = historyService.queryReturnSubtotal(mapR);
        }


        NxDepartmentBillEntity salesBill = nxDepartmentBillService.querySalesBillApplys(billId);

        Map<String, Object> map = new HashMap<>();
        map.put("billId", billId);
        map.put("orderBy", "time");
        System.out.println("newoorororororoororro" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);

        List<Map<String, Object>> mapList = new ArrayList<>();
        List<NxDepartmentEntity> entities = nxDepartmentService.querySubDepartments(depFatherId);

        if (entities.size() > 0) {
            for (NxDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getNxDepartmentId());
                mapDep.put("depName", dep.getNxDepartmentName());
                Map<String, Object> map1 = new HashMap<>();
                map1.put("billId", billId);
                map1.put("depId", dep.getNxDepartmentId());
                List<NxDepartmentOrderHistoryEntity> depOrders = historyService.queryDisHistoryOrdersByParams(map1);

                mapDep.put("depOrders", depOrders);

                if (depOrders.size() > 0) {
                    mapList.add(mapDep);
                }
            }

            Map<String, Object> map3 = new HashMap<>();
            map3.put("arr", mapList);
            map3.put("bill", salesBill);
            map3.put("returnNumber", count);
            map3.put("toReturnSubtotal", toReturnSubtotal);
            map3.put("haveReturnSubtotal", haveReturnSubtotal);

            return R.ok().put("data", map3);
        } else {
            Map<String, Object> map4 = new HashMap<>();
            map4.put("arr", ordersEntities);
            map4.put("bill", salesBill);
            map4.put("returnNumber", count);
            map4.put("toReturnSubtotal", toReturnSubtotal);
            map4.put("haveReturnSubtotal", haveReturnSubtotal);

            return R.ok().put("data", map4);
        }


    }


    private Map<String, Object> disQueryAccountBillByMonthGb(Integer disId, Integer gbDisId, Integer nxCommId, Integer nxDepFatherId, int which) {

        LocalDate today = LocalDate.now();
        today = today.minusMonths(which);
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("MM");
        String format = formatters.format(today);
        //本月的账单
        Map<String, Object> map = new HashMap<>();
        map.put("nx_DB_dis_id", disId);
        map.put("nx_DB_gb_dis_id", gbDisId);
        map.put("nx_DB_nx_community_id", nxCommId);
        map.put("nx_DB_month", format);

        Map<String, Object> mapz = new HashMap<>();
        mapz.put("nx_DB_dis_id", disId);
        mapz.put("nx_DB_gb_dis_id", -1);
        mapz.put("nx_DB_nx_community_id", -1);
        mapz.put("nx_DB_month", format);
        if (nxDepFatherId != -1) {
            mapz.put("nx_DB_dep_father_id", nxDepFatherId);
        } else {
            mapz.put("nx_DB_dep_father_id", nxDepFatherId);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("map", map);
        params.put("map1", mapz);
        System.out.println("gbdiidiidsaaalistlist" + map);

        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBindMap(params);
        System.out.println("billist" + billEntityList.size());

        //本月的未结账单数量
        map.put("nx_DB_status", 0);
        mapz.put("nx_DB_status", 0);
        Map<String, Object> paramsUn = new HashMap<>();
        paramsUn.put("map", map);
        paramsUn.put("map1", mapz);
        System.out.println("gbdiidiidsaaa" + map);
        int unSettle = nxDepartmentBillService.queryCountBindMap(paramsUn);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("arr", billEntityList);
        dataMap.put("unSettleTotal", unSettle);
        dataMap.put("month", format);

        return dataMap;
    }

    private Map<String, Object> queryAccountBillByMonthGb(Integer disId, Integer gbDisId, Integer nxCommId, int which) {

        LocalDate today = LocalDate.now();
        today = today.minusMonths(which);
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("MM");
        String format = formatters.format(today);
        //本月的账单
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("gbDisId", gbDisId);
        map.put("nxCommId", nxCommId);
        map.put("month", format);
        map.put("status", 5);
        System.out.println("gbgbbgbgbgbgbgbg" + map);
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryGbDepBillsByParams(map);

        //本月的未结账单数量
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("gbDisId", gbDisId);
        map1.put("nxCommId", nxCommId);
        map1.put("equalStatus", 0);
        map1.put("month", format);
        double whichMonthUnSettleTotal = 0.0;
        int wxCountAuto = nxDepartmentBillService.queryBillsCount(map1);
        if (wxCountAuto > 0) {
            whichMonthUnSettleTotal = nxDepartmentBillService.queryBillSubtotalByParams(map1);
        }

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("arr", billEntityList);
        dataMap.put("unSettleTotal", String.format("%.1f", whichMonthUnSettleTotal));
        dataMap.put("month", format);

        return dataMap;
    }

    private Map<String, Object> queryAccountBillByMonth(Integer disId, Integer depFatherId, int which) {

        LocalDate today = LocalDate.now();
        today = today.minusMonths(which);
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("MM");
        DateTimeFormatter year = DateTimeFormatter.ofPattern("yyyy");
        String format = formatters.format(today);
        System.out.println("formaaaa" + format);
        String yearString = year.format(today);
        //本月的账单
        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depFatherId);
        map.put("month", format);
        map.put("year", yearString);
        map.put("status", 3);
        System.out.println("yeeeyeyee" + map);
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsListByParams(map);

        //本月的未结账单数量
        Map<String, Object> map1 = new HashMap<>();
        map1.put("depFatherId", depFatherId);
        map1.put("equalStatus", 0);
        map1.put("month", format);
        map1.put("year", yearString);
        System.out.println("whaaiaimap" + map);
        int wxCountAuto = nxDepartmentBillService.queryBillsCount(map1);
        Double aDouble = 0.0;
        if (wxCountAuto > 0) {
            aDouble = nxDepartmentBillService.queryBillSubtotalByParams(map1);
        }

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("arr", billEntityList);
        dataMap.put("unSettleTotal", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP));
        dataMap.put("month", format);

        NxDepartmentEntity nxDepartmentEntity = nxDepartmentService.queryDepInfoAll(depFatherId);
        List<Map<String, Object>> depList = new ArrayList<>();
        if (nxDepartmentEntity.getNxDepartmentEntities().size() > 0) {

            for (NxDepartmentEntity subDep : nxDepartmentEntity.getNxDepartmentEntities()) {
                map1.put("depFatherId", null);

                map1.put("depId", subDep.getNxDepartmentId());

                System.out.println("susmapapapa" + map1);
                int wxCountAutoS = nxDepartmentBillService.queryBillsCount(map1);
                Double aDoubleS = 0.0;
                if (wxCountAutoS > 0) {
                    aDoubleS = nxDepartmentBillService.queryBillSubtotalByParams(map1);
                }
                Map<String, Object> mapS = new HashMap<>();
                mapS.put("subDepName", subDep.getNxDepartmentName());

                mapS.put("subDepSubtotal", new BigDecimal(aDoubleS).setScale(1, BigDecimal.ROUND_HALF_UP));
                depList.add(mapS);
            }


            map1.put("depId", depFatherId);

            System.out.println("susmapapapa" + map1);
            int wxCountAutoST = nxDepartmentBillService.queryBillsCount(map1);
            Double aDoubleST = 0.0;
            if (wxCountAutoST > 0) {
                aDoubleST = nxDepartmentBillService.queryBillSubtotalByParams(map1);
            }
            Map<String, Object> mapS = new HashMap<>();
            mapS.put("subDepName", "总单子");

            mapS.put("subDepSubtotal", new BigDecimal(aDoubleST).setScale(1, BigDecimal.ROUND_HALF_UP));
            depList.add(mapS);
            dataMap.put("subDeps", depList);
        }

        return dataMap;
    }


    /**
     * 现金账单
     *
     * @param disId
     * @return
     */
    @RequestMapping(value = "/sellerAndBuyerGetSalesBills", method = RequestMethod.POST)
    @ResponseBody
    public R sellerAndBuyerGetSalesBills(Integer depFatherId, Integer disId) {

        Map<String, Object> stringObjectMap = querySalesBillByMonth(disId, depFatherId, 0);
        Map<String, Object> lastObjectMap = querySalesBillByMonth(disId, depFatherId, 1);
        Map<String, Object> lastTwoObjectMap = querySalesBillByMonth(disId, depFatherId, 2);
        List<Map<String, Object>> dataMap = new ArrayList<>();
        dataMap.add(lastTwoObjectMap);
        dataMap.add(lastObjectMap);
        dataMap.add(stringObjectMap);
        return R.ok().put("data", dataMap);

    }

    private Map<String, Object> querySalesBillByMonth(Integer disId, Integer depFatherId, int which) {

        LocalDate today = LocalDate.now();
        today = today.minusMonths(which);
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("MM");
        String format = formatters.format(today);
        //本月的账单
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("depFatherId", depFatherId);
        map.put("month", format);
        System.out.println("whwhwwmondne" +  map);
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsListByParams(map);
        int total = nxDepartmentBillService.queryTotalByParams(map);
        Double aDouble = 0.0;
        if (total > 0) {
            aDouble = nxDepartmentBillService.queryBillSubtotalByParams(map);
        }

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("arr", billEntityList);
        dataMap.put("month", format);
        dataMap.put("total", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP));

        return dataMap;
    }

    public GbDepartmentBillEntity savePhoneGbBillByNxBIll(@RequestBody NxDepartmentBillEntity nxDepartmentBill, Integer integer) {


        GbDepartmentBillEntity gbDepartmentBill = new GbDepartmentBillEntity();
        gbDepartmentBill.setGbDbIssueNxDisId(nxDepartmentBill.getNxDbDisId());
        gbDepartmentBill.setGbDbDisId(nxDepartmentBill.getNxDbGbDisId());
        gbDepartmentBill.setGbDbDepId(nxDepartmentBill.getNxDbGbDepId());
        gbDepartmentBill.setGbDbDepFatherId(nxDepartmentBill.getNxDbGbDepFatherId());
        gbDepartmentBill.setGbDbYear(formatWhatYear(0));
        gbDepartmentBill.setGbDbTotal(nxDepartmentBill.getNxDbTotal());
        gbDepartmentBill.setGbDbPayTotal(nxDepartmentBill.getNxDbTotal());
        gbDepartmentBill.setGbDbPrintTimes(0);
        gbDepartmentBill.setGbDbIssueOrderType(5);
        gbDepartmentBill.setGbDbOrderAmount(integer);

        gbDepartmentBill.setGbDbDate(formatWhatDay(0));
        gbDepartmentBill.setGbDbTime(formatWhatYearDayTime(0));
        gbDepartmentBill.setGbDbMonth(formatWhatMonth(0));
        gbDepartmentBill.setGbDbWeek(getWeekOfYear(0).toString());
        gbDepartmentBill.setGbDbDay(getWeek(0));
        gbDepartmentBill.setGbDbTradeNo(nxDepartmentBill.getNxDbTradeNo());
        Map<String, Object> mapg = new HashMap<>();
        mapg.put("nxDisId", nxDepartmentBill.getNxDbDisId());
        mapg.put("gbDisId", nxDepartmentBill.getNxDbGbDisId());
        NxDistributerGbDistributerEntity entity = nxDisGbDisService.queryObjectByParams(mapg);
        Integer nxDgdGbPayMethod = entity.getNxDgdGbPayMethod();
        if (nxDgdGbPayMethod == 1) {
            Integer nxDgdGbPayPeriodWeek = entity.getNxDgdGbPayPeriodWeek();
            String willPayDate = getWillPayDate(nxDgdGbPayPeriodWeek, Calendar.WEDNESDAY);
            gbDepartmentBill.setGbDbWillPayDate(willPayDate);
            gbDepartmentBill.setGbDbStatus(getGbDepBillNew());
        } else {
            gbDepartmentBill.setGbDbWillPayDate(formatWhatDate(1));
            gbDepartmentBill.setGbDbStatus(getGbDepBillHavePay());
        }

        System.out.println("oreeramdiod" + gbDepartmentBill.getGbDbOrderAmount());
        gbDepartmentBillService.save(gbDepartmentBill);

        nxDepartmentBill.setNxDbGbDepartmentBillId(gbDepartmentBill.getGbDepartmentBillId());
        nxDepartmentBillService.update(nxDepartmentBill);

        return gbDepartmentBill;

    }


    private Integer checkDepDisGoods(NxDepartmentOrdersEntity nxDepartmentOrders) {

        System.out.println("chehchcchchhchchcdididigogood");

        Integer depDisGoodsId = 0;
        //判断是否是部门商品
        Integer doDisGoodsId = nxDepartmentOrders.getNxDoDisGoodsId();
        Integer nxDoDepartmentId1 = nxDepartmentOrders.getNxDoDepartmentId();
        //查询部门还是订货群是否添加过此商品
        Map<String, Object> map = new HashMap<>();
        map.put("depId", nxDoDepartmentId1);
        map.put("disGoodsId", doDisGoodsId);
        List<NxDepartmentDisGoodsEntity> disGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(map);
        if (disGoodsEntities.size() == 0) {
            //添加部门商品
            NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(doDisGoodsId);
            String nxDgGoodsName = nxDistributerGoodsEntity.getNxDgGoodsName();
            NxDepartmentDisGoodsEntity disGoodsEntity = new NxDepartmentDisGoodsEntity();
            disGoodsEntity.setNxDdgDepGoodsName(nxDgGoodsName);
            disGoodsEntity.setNxDdgDisGoodsId(doDisGoodsId);
            disGoodsEntity.setNxDdgDisGoodsFatherId(nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId());

            disGoodsEntity.setNxDdgDisGoodsGrandId(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
            NxDistributerFatherGoodsEntity fatherGoodsEntity = distributerFatherGoodsService.queryObject(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
            Integer greatFatherId = fatherGoodsEntity.getNxDfgFathersFatherId();
            disGoodsEntity.setNxDdgDisGoodsGreatId(greatFatherId);

            disGoodsEntity.setNxDdgDepGoodsPinyin(nxDistributerGoodsEntity.getNxDgGoodsPinyin());
            disGoodsEntity.setNxDdgDepGoodsPy(nxDistributerGoodsEntity.getNxDgGoodsPy());
            disGoodsEntity.setNxDdgDepGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
            disGoodsEntity.setNxDdgDepartmentId(nxDoDepartmentId1);
            disGoodsEntity.setNxDdgDepartmentFatherId(nxDepartmentOrders.getNxDoDepartmentFatherId());
            //orderData
            disGoodsEntity.setNxDdgOrderPrice(nxDepartmentOrders.getNxDoPrice());
            disGoodsEntity.setNxDdgOrderCostPrice(nxDepartmentOrders.getNxDoCostPrice());
            disGoodsEntity.setNxDdgOrderQuantity(nxDepartmentOrders.getNxDoQuantity());
            disGoodsEntity.setNxDdgOrderRemark(nxDepartmentOrders.getNxDoRemark());
            disGoodsEntity.setNxDdgOrderStandard(nxDepartmentOrders.getNxDoStandard());
            disGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
            disGoodsEntity.setNxDdgGbDistributerId(nxDepartmentOrders.getNxDoGbDistributerId());
            disGoodsEntity.setNxDdgGoodsNxDistributerId(nxDistributerGoodsEntity.getNxDgDistributerId());
            if (nxDepartmentOrders.getNxDoGoodsOriginalName() != null) {
                disGoodsEntity.setNxDdgOrderGoodsName(nxDepartmentOrders.getNxDoGoodsOriginalName());
            }
            disGoodsEntity.setNxDdgOrderPriceLevel(nxDepartmentOrders.getNxDoCostPriceLevel());
            nxDepartmentDisGoodsService.save(disGoodsEntity);
            depDisGoodsId = disGoodsEntity.getNxDepartmentDisGoodsId();

        } else {

            depDisGoodsId = disGoodsEntities.get(0).getNxDepartmentDisGoodsId();
            NxDepartmentDisGoodsEntity disGoodsEntity = nxDepartmentDisGoodsService.queryObject(depDisGoodsId);
            disGoodsEntity.setNxDdgOrderPrice(nxDepartmentOrders.getNxDoPrice());
            disGoodsEntity.setNxDdgOrderCostPrice(nxDepartmentOrders.getNxDoCostPrice());
            disGoodsEntity.setNxDdgOrderQuantity(nxDepartmentOrders.getNxDoQuantity());
            disGoodsEntity.setNxDdgOrderRemark(nxDepartmentOrders.getNxDoRemark());
            disGoodsEntity.setNxDdgOrderStandard(nxDepartmentOrders.getNxDoStandard());
            disGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
            NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDepartmentOrders.getPurchaseGoodsEntity();
            if (purchaseGoodsEntity != null) {
                if (purchaseGoodsEntity.getNxDpgSellUserId() != null) {
                    disGoodsEntity.setNxDdgOrderSellerUserId(purchaseGoodsEntity.getNxDpgSellUserId());
                }
                disGoodsEntity.setNxDdgOrderBuyerUserId(purchaseGoodsEntity.getNxDpgBuyUserId());
            }

            nxDepartmentDisGoodsService.update(disGoodsEntity);
        }
        return depDisGoodsId;
    }


    @RequestMapping(value = "/updateBillOrders", method = RequestMethod.POST)
    @ResponseBody
    public R updateBillOrders(Integer billId, Integer orderId, String billSubtotal,
                              String orderWeight, String orderPrice, String orderSubtotal) {
        NxDepartmentOrderHistoryEntity ordersEntity = historyService.queryObject(orderId);
        ordersEntity.setNxDoWeight(orderWeight);
        ordersEntity.setNxDoPrice(orderPrice);
        ordersEntity.setNxDoSubtotal(orderSubtotal);
        System.out.println("whhwhwh" + ordersEntity.getNxDoExpectPrice());
        if (ordersEntity.getNxDoPriceDifferent() != null) {
            BigDecimal weightB = new BigDecimal(orderWeight);
            BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
            BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal profitB = new BigDecimal(orderSubtotal).subtract(decimal).setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal scaleB = profitB.divide(new BigDecimal(orderSubtotal), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            ordersEntity.setNxDoProfitScale(scaleB.toString());
            ordersEntity.setNxDoProfitSubtotal(profitB.toString());
            BigDecimal expectPrice = new BigDecimal(ordersEntity.getNxDoExpectPrice());
            BigDecimal doPrice = new BigDecimal(ordersEntity.getNxDoPrice());
            BigDecimal subtract = doPrice.subtract(expectPrice);
            System.out.println("exxxxxxxx" + subtract);
            ordersEntity.setNxDoPriceDifferent(subtract.toString());
        }

        historyService.update(ordersEntity);
        NxDepartmentBillEntity nxDepartmentBillEntity = nxDepartmentBillService.queryObject(billId);
        nxDepartmentBillEntity.setNxDbTotal(billSubtotal);
        nxDepartmentBillService.update(nxDepartmentBillEntity);
        return R.ok();
    }


}


//
//    @RequestMapping(value = "/nxDisFinishPurchaseGoodsBatchGb1")
//    @ResponseBody
//    public R nxDisFinishPurchaseGoodsBatchGb1(@RequestBody GbDistributerPurchaseBatchEntity batchEntity) {
//
//        List<GbDistributerPurchaseGoodsEntity> nxDPBEntities = batchEntity.getGbDPGEntities();
//        int orderTotal = 0;
//        for (GbDistributerPurchaseGoodsEntity purGoods : nxDPBEntities) {
//            purGoods.setGbDpgStatus(2);
//            purGoods.setGbDpgPurchaseNxDistributerId(batchEntity.getGbDpbDistributerId());
//            gbDPGService.update(purGoods);
//
//            Map<String, Object> map = new HashMap<>();
//            map.put("purGoodsId", purGoods.getGbDistributerPurchaseGoodsId());
//            List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
//            if(gbDepartmentOrdersEntities.size() > 0) {
//                for(GbDepartmentOrdersEntity gbDepartmentOrdersEntity: gbDepartmentOrdersEntities){
//                    orderTotal = orderTotal +1;
//
//                    Integer gbDoNxDepartmentOrderId = gbDepartmentOrdersEntity.getGbDoNxDepartmentOrderId();
//
//                    NxDepartmentOrdersEntity orders =  nxDepartmentOrdersService.queryObject(gbDoNxDepartmentOrderId);
//                    orders.setNxDoStatus(2);
//                    orders.setNxDoPurchaseStatus(getNxDisPurchaseGoodsFinishPay());
//                    nxDepartmentOrdersService.update(orders);
//
//                    gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusReceived());
//                    gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderBuyStatusUnPayFinish());
//                    gbDepartmentOrdersEntity.setGbDoArriveWeeksYear(getWeekOfYear(0));
//                    gbDepartmentOrdersEntity.setGbDoArriveWhatDay(getWeek(0));
//                    gbDepartmentOrdersEntity.setGbDoArriveOnlyDate(formatWhatDate(0));
//                    gbDepartmentOrdersEntity.setGbDoArriveDate(formatWhatDay(0));
//                    gbDepartmentOrdersEntity.setGbDoWeight(orders.getNxDoWeight());
//                    gbDepartmentOrdersEntity.setGbDoSubtotal(orders.getNxDoSubtotal());
//                    gbDepartmentOrdersEntity.setGbDoPrice(orders.getNxDoPrice());
//                    gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
//
//
//                    Integer doDisGoodsId = orders.getNxDoDisGoodsId();
//                    NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
//                    BigDecimal weight = new BigDecimal(distributerGoodsEntity.getNxDgOutTotalWeight());
//                    BigDecimal orderWeight = new BigDecimal(orders.getNxDoWeight());
//                    BigDecimal add = weight.add(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
//                    distributerGoodsEntity.setNxDgOutTotalWeight(add.toString());
//                    dgService.update(distributerGoodsEntity);
//
//                    Integer nxDoPurchaseGoodsId = orders.getNxDoPurchaseGoodsId();
//                    if (nxDoPurchaseGoodsId != -1) {
//                        NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
//                        Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
//                        Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
//                        if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
//                            purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
//                            purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
//                        } else {
//                            purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
//                        }
//
//                        nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
//                    }
//                }
//
//            }
//
//            BigDecimal decimal1 = new BigDecimal(orderTotal);
//            Map<String, Object> mapNG = new HashMap<>();
//            mapNG.put("gbDisId", batchEntity.getGbDpbDistributerId());
//            mapNG.put("nxDisId", batchEntity.getGbDpbNxDistributerId());
//            NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity = nxDisGbDisService.queryObjectByParams(mapNG);
//            if(nxDistributerGbDistributerEntity.getNxDgdFromNxDisId() != null && !nxDistributerGbDistributerEntity.getNxDgdFromNxDisId().equals(batchEntity.getGbDpbNxDistributerId())){
//                NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(batchEntity.getGbDpbNxDistributerId());
//                NxDistributerPayListEntity payListEntity = new NxDistributerPayListEntity();
//                payListEntity.setNxNdplNxDisId(batchEntity.getGbDpbNxDistributerId());
//                payListEntity.setNxNdplNxDepartmentId(-1);
//                payListEntity.setNxNdplNxDepartmentFatherId(-1);
//                payListEntity.setNxNdplPaySubtotal(Integer.valueOf(orderTotal).toString());
//                payListEntity.setNxNdplPayTime(formatFullTime());
//                payListEntity.setNxNdplPayDate(formatWhatDay(0));
//                payListEntity.setNxNdplPayMonth(formatWhatMonth(0));
//                payListEntity.setNxNdplPayYear(formatWhatYear(0));
//                payListEntity.setNxNdplStatus(0);
//                payListEntity.setNxNdplType(0);
//                payListEntity.setNxNdplRestPoints(Integer.valueOf(nxDistributerEntity.getNxDistributerBuyQuantity()));
//                payListService.save(payListEntity);
//
//                BigDecimal decimal0 = new BigDecimal(nxDistributerEntity.getNxDistributerBuyQuantity());
//                BigDecimal restPoints = decimal0.subtract(decimal1).setScale(0,BigDecimal.ROUND_HALF_UP);
//                nxDistributerEntity.setNxDistributerBuyQuantity(restPoints.toString());
//                nxDistributerService.update(nxDistributerEntity);
//            }
//
//
//
//            GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(batchEntity.getGbDpbDistributerId());
//            BigDecimal decimal = new BigDecimal(gbDistributerEntity.getGbDistributerBuyQuantity());
//            BigDecimal add = decimal.subtract(decimal1).setScale(0, BigDecimal.ROUND_HALF_UP);
//            gbDistributerEntity.setGbDistributerBuyQuantity(add.toString());
//            System.out.println("updabbbgbgb" + add);
//            gbDistributerService.update(gbDistributerEntity);
//        }
//
////        batchEntity.setGbDpbStatus(2);
//        batchEntity.setGbDpbSellerReplyFullTime(formatFullTime());
//        gbDPBService.update(batchEntity);
//        return R.ok();
//    }


//    @RequestMapping(value = "/saveAccountBillPrinterGb1", method = RequestMethod.POST)
//    @ResponseBody
//    public R saveAccountBillPrinterGb1(Integer gbDepFatherId,Integer gbDepId, String tradeNo, Integer userId, Integer nxDisId) {
//
//        System.out.println("whwhhwwhw" + gbDepFatherId);
//        System.out.println("whwhhwwhw" + gbDepId);
//
//        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(gbDepFatherId);
//        Integer gbDepartmentDisId = departmentEntity.getGbDepartmentDisId();
//        GbDepartmentBillEntity gbDepartmentBill = new GbDepartmentBillEntity();
//        gbDepartmentBill.setGbDbDepId(gbDepId);
//        gbDepartmentBill.setGbDbDepFatherId(gbDepFatherId);
//        gbDepartmentBill.setGbDbDisId(gbDepartmentDisId);
//        gbDepartmentBill.setGbDbIssueNxDisId(nxDisId);
//        gbDepartmentBill.setGbDbIssueOrderType(5);
//        gbDepartmentBill.setGbDbStatus(0);
//        gbDepartmentBill.setGbDbDate(formatWhatDay(0));
//        gbDepartmentBill.setGbDbTime(formatWhatYearDayTime(0));
//        gbDepartmentBill.setGbDbMonth(formatWhatMonth(0));
//        gbDepartmentBill.setGbDbWeek(getWeekOfYear(0).toString());
//        gbDepartmentBill.setGbDbDay(getWeek(0));
//        gbDepartmentBill.setGbDbYear(formatWhatYear(0));
//        gbDepartmentBill.setGbDbTradeNo(tradeNo);
//        gbDepartmentBill.setGbDbPrintTimes(1);
//        gbDepartmentBill.setGbDbSellingTotal("0");
//        Map<String, Object> map = new HashMap<>();
//        map.put("type", getGbDepartmentTypeAppSupplier());
//        map.put("disId", gbDepartmentDisId);
//        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
//        gbDepartmentBill.setGbDbIssueDepId(departmentEntities.get(0).getGbDepartmentId());
//        BigDecimal billTotal = new BigDecimal(0);
//        Map<String, Object> nxMap = new HashMap<>();
//        nxMap.put("gbDepFatherId", gbDepFatherId);
//        if(!gbDepFatherId.equals(gbDepId)){
//            nxMap.put("gbDepId", gbDepId);
//        }
//        nxMap.put("status", 3);
//        nxMap.put("disId", nxDisId);
//        System.out.println("niamamammamammam" + nxMap);
//        Double aDouble = nxDepartmentOrdersService.queryDepOrdersSubtotal(nxMap);
//        gbDepartmentBill.setGbDbTotal(new BigDecimal(aDouble).setScale(1,BigDecimal.ROUND_HALF_UP).toString());
//        gbDepartmentBillService.save(gbDepartmentBill);
//
//        NxDepartmentBillEntity nxDepartmentBill = new NxDepartmentBillEntity();
//        String ss = generateBillTradeNo(gbDepartmentBill.getGbDbTradeNo());
//        nxDepartmentBill.setNxDbTradeNo(ss);
//        nxDepartmentBill.setNxDbDisId(nxDisId);
//        nxDepartmentBill.setNxDbDepId(-1);
//        nxDepartmentBill.setNxDbDepFatherId(-1);
//        nxDepartmentBill.setNxDbTotal(gbDepartmentBill.getGbDbTotal());
//        nxDepartmentBill.setNxDbPrintTimes(1);
//        nxDepartmentBill.setNxDbGbDisId(gbDepartmentBill.getGbDbDisId());
//        nxDepartmentBill.setNxDbGbDepId(gbDepartmentBill.getGbDbDepId());
//        nxDepartmentBill.setNxDbGbDepFatherId(gbDepartmentBill.getGbDbDepFatherId());
//        nxDepartmentBill.setNxDbNxCommunityId(-1);
//        nxDepartmentBill.setNxDbNxRestrauntId(-1);
//        nxDepartmentBill.setNxDbStatus(0);
//        nxDepartmentBill.setNxDbDate(gbDepartmentBill.getGbDbDate());
//        nxDepartmentBill.setNxDbTime(gbDepartmentBill.getGbDbTime());
//        nxDepartmentBill.setNxDbMonth(gbDepartmentBill.getGbDbMonth());
//        nxDepartmentBill.setNxDbWeek(gbDepartmentBill.getGbDbYear());
//        nxDepartmentBill.setNxDbDay(gbDepartmentBill.getGbDbDay());
//        nxDepartmentBill.setNxDbYear(gbDepartmentBill.getGbDbYear());
//        nxDepartmentBill.setNxDbProfitScale("0");
//        nxDepartmentBill.setNxDbProfitTotal("0");
//        nxDepartmentBill.setNxDbIssueUserId(userId);
//        nxDepartmentBill.setNxDbGbDepartmentBillId(gbDepartmentBill.getGbDepartmentBillId());
//        nxDepartmentBillService.save(nxDepartmentBill);
//
//
//        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(nxMap);
//
//        if(ordersEntities.size() > 0){
//            for (NxDepartmentOrdersEntity orders : ordersEntities) {
//                //0 subtotal
//                billTotal = billTotal.add(new BigDecimal(orders.getNxDoSubtotal()));
//
//                orders.setNxDoStatus(3);
//                orders.setNxDoPurchaseStatus(getNxDisPurchaseGoodsFinishPay());
//                orders.setNxDoBillId(nxDepartmentBill.getNxDepartmentBillId());
//                nxDepartmentOrdersService.update(orders);
//
//                //updata weight
//                Integer doDisGoodsId = orders.getNxDoDisGoodsId();
//                NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
//                BigDecimal weight = new BigDecimal(distributerGoodsEntity.getNxDgOutTotalWeight());
//                BigDecimal orderWeight = new BigDecimal(orders.getNxDoWeight());
//                BigDecimal add = weight.add(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
//                distributerGoodsEntity.setNxDgOutTotalWeight(add.toString());
//                dgService.update(distributerGoodsEntity);
//
//                Integer nxDoPurchaseGoodsId = orders.getNxDoPurchaseGoodsId();
//                if (nxDoPurchaseGoodsId != -1) {
//                    NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
//                    Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
//                    Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
//                    if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
//                        purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
//                        purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
//                    } else {
//                        purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
//                    }
//                    nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
//                }
//            }
//        }
//
//        nxDepartmentBill.setNxDepartmentOrdersEntities(ordersEntities);
//        saveAccountBillGbDis(nxDepartmentBill);
//
//
//        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(nxDisId);
//        BigDecimal disBuyQuantity = new BigDecimal(nxDistributerEntity.getNxDistributerBuyQuantity());
//
//        final int ORDERS_PER_SHEET = 24;
//        final int SHEET_PRICE = 24;
//
//        int sheetCount = (int) Math.ceil((double) ordersEntities.size() / ORDERS_PER_SHEET);
//        System.out.println("kanakankayigongduoshs" + sheetCount);
//        int total = sheetCount * SHEET_PRICE;
//        BigDecimal restQuantity = disBuyQuantity.subtract(new BigDecimal(ordersEntities.size())).setScale(0,BigDecimal.ROUND_HALF_UP);
//
//        NxDistributerPayListEntity payListEntity = new NxDistributerPayListEntity();
//        payListEntity.setNxNdplNxDisId(nxDisId);
//        payListEntity.setNxNdplGbDepartmentId(gbDepartmentBill.getGbDbDepId());
//        payListEntity.setNxNdplGbDepartmentFatherId(gbDepartmentBill.getGbDbDepFatherId());
//        payListEntity.setNxNdplPaySubtotal(String.valueOf(String.valueOf(ordersEntities.size())));
//
//        payListEntity.setNxNdplPayTime(formatFullTime());
//        payListEntity.setNxNdplPayDate(formatWhatDay(0));
//        payListEntity.setNxNdplPayMonth(formatWhatMonth(0));
//        payListEntity.setNxNdplPayYear(formatWhatYear(0));
//        payListEntity.setNxNdplStatus(0);
//        payListEntity.setNxNdplType(1);
//        payListEntity.setNxNdplRestPoints(Integer.valueOf(nxDistributerEntity.getNxDistributerBuyQuantity()));
//        payListEntity.setNxNdplGbDbId(gbDepartmentBill.getGbDepartmentBillId());
//        payListService.save(payListEntity);
//
//        nxDistributerEntity.setNxDistributerBuyQuantity(restQuantity.toString());
//        System.out.println("dispayddyd" + restQuantity);
//        nxDistributerService.update(nxDistributerEntity);
//
//        return R.ok();
//    }
//}
