package com.nongxinle.controller;

import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.NxDistributerFatherGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsShelfStockEntity;
import com.nongxinle.service.NxDepartmentOrdersService;
import com.nongxinle.service.NxDepartmentService;
import com.nongxinle.service.NxDistributerFatherGoodsService;
import com.nongxinle.service.NxDistributerGoodsService;
import com.nongxinle.service.NxDistributerGoodsShelfStockService;
import com.nongxinle.service.NxDistributerGoodsShelfGoodsService;
import com.nongxinle.utils.R;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.ocr.v20181119.OcrClient;
import com.tencentcloudapi.ocr.v20181119.models.GeneralAccurateOCRRequest;
import com.tencentcloudapi.ocr.v20181119.models.GeneralAccurateOCRResponse;
import com.tencentcloudapi.ocr.v20181119.models.TextDetection;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.getTimeStamp;
import static com.nongxinle.utils.PinYin4jUtils.hanziToPinyin;
import static com.nongxinle.utils.PinYin4jUtils.getHeadStringByString;

/**
 * OCR 识别接口 Controller
 * 
 * @author lpy
 * @date 2025-12-07
 */
@RestController
@RequestMapping("api/ocr")
public class OcrController {
    private static final Logger logger = LoggerFactory.getLogger(OcrController.class);

    @Value("${tencentcloud.ocr.secret.id}")
    private String secretId;

    @Value("${tencentcloud.ocr.secret.key}")
    private String secretKey;

    @Value("${tencentcloud.ocr.region:ap-beijing}")
    private String region;

    @Value("${deepseek.api.key}")
    private String deepSeekApiKey;

    @Value("${deepseek.api.url}")
    private String deepSeekApiUrl;

    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;

    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;

    @Autowired
    private NxDistributerGoodsShelfStockService nxDistributerGoodsShelfStockService;

    @Autowired
    private NxDistributerFatherGoodsService nxDistributerFatherGoodsService;

    @Autowired
    private NxDepartmentService nxDepartmentService;

    @Autowired
    private NxDistributerGoodsShelfGoodsService nxDistributerGoodsShelfGoodsService;

    /**
     * OCR 识别接口（自动调用 DeepSeek 解析）
     * 
     * @param request 请求参数，包含 ImageBase64（图片的Base64编码字符串，不含data:image前缀）
     * @return 识别结果和解析后的订单商品列表
     */
    @RequestMapping(value = "/recognize", method = RequestMethod.POST)
    @ResponseBody
    public R recognize(@RequestBody Map<String, String> request) {
        try {
            // 获取图片 Base64
            String imageBase64 = request.get("ImageBase64");
            if (imageBase64 == null || imageBase64.isEmpty()) {
                logger.warn("[OCR] 图片数据为空");
                return R.error("图片数据不能为空");
            }

            // 处理 Base64 前缀（如果前端传了 data:image 前缀，需要去掉）
            if (imageBase64.contains(",")) {
                imageBase64 = imageBase64.substring(imageBase64.indexOf(",") + 1);
            }

            logger.info("[OCR+DeepSeek] 开始识别和解析，图片大小: {} bytes", imageBase64.length());

            // 初始化认证对象
            Credential cred = new Credential(secretId, secretKey);
            
            // 实例化http选项
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("ocr.tencentcloudapi.com");
            // 设置超时时间（单位：秒）
            httpProfile.setConnTimeout(60);  // 连接超时 60 秒
            httpProfile.setReadTimeout(60);   // 读取超时 60 秒
            httpProfile.setWriteTimeout(60);  // 写入超时 60 秒
            
            // 实例化client选项
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            
            // 实例化要请求产品的client对象
            OcrClient client = new OcrClient(cred, region, clientProfile);
            
            // 实例化请求对象
            GeneralAccurateOCRRequest req = new GeneralAccurateOCRRequest();
            req.setImageBase64(imageBase64);
            
            // 可选参数
            // req.setIsPdf(false);
            // req.setPdfPageNumber(1);
            // req.setIsWords(false);
            
            // 调用 OCR API（带重试机制）
            GeneralAccurateOCRResponse resp = null;
            int maxRetries = 3;
            int retryCount = 0;
            Exception lastException = null;
            
            while (retryCount < maxRetries) {
                try {
                    logger.info("[OCR+DeepSeek] 尝试调用 OCR API (第 {} 次)", retryCount + 1);
                    resp = client.GeneralAccurateOCR(req);
                    break; // 成功则跳出循环
                } catch (TencentCloudSDKException e) {
                    lastException = e;
                    String errorCode = e.getErrorCode();
                    String errorMessage = e.getMessage();
                    
                    // 如果是网络错误，尝试重试
                    if (errorMessage != null && (errorMessage.contains("IOException") || 
                                                 errorMessage.contains("unexpected end of stream") ||
                                                 errorMessage.contains("timeout") ||
                                                 errorMessage.contains("Connection"))) {
                        retryCount++;
                        if (retryCount < maxRetries) {
                            logger.warn("[OCR+DeepSeek] OCR API 调用失败，准备重试 (第 {} 次): {}", retryCount, errorMessage);
                            try {
                                Thread.sleep(1000 * retryCount); // 等待后重试，递增等待时间
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                            continue;
                        } else {
                            logger.error("[OCR+DeepSeek] OCR API 调用失败，已达到最大重试次数: {}", errorMessage);
                            throw e;
                        }
                    } else {
                        // 非网络错误，直接抛出
                        throw e;
                    }
                }
            }
            
            if (resp == null && lastException != null) {
                throw lastException;
            }
            
            // 打印识别到的所有文本内容
            TextDetection[] textDetections = resp.getTextDetections();
            int textCount = textDetections != null ? textDetections.length : 0;
            logger.info("[OCR+DeepSeek] OCR识别成功，识别到 {} 条文本", textCount);
            
            if (textDetections == null || textDetections.length == 0) {
                logger.warn("[OCR+DeepSeek] 未识别到任何文本");
                return R.error("未识别到任何文本");
            }
            
            if (textDetections != null && textDetections.length > 0) {
                logger.info("========== OCR 识别结果 ==========");
                for (int i = 0; i < textDetections.length; i++) {
                    TextDetection detection = textDetections[i];
                    String detectedText = detection.getDetectedText();
                    Long confidence = detection.getConfidence();
                    logger.info("[{}] 文本: {} | 置信度: {}", i + 1, detectedText, confidence);
                }
                logger.info("====================================");
            }
            
            // 自动调用 DeepSeek API 进行解析
            logger.info("[OCR+DeepSeek] 开始调用 DeepSeek 进行解析...");
            
            // 拼接所有文本
            StringBuilder ocrText = new StringBuilder();
            for (TextDetection detection : textDetections) {
                String text = detection.getDetectedText();
                if (text != null && !text.trim().isEmpty()) {
                    ocrText.append(text).append("\n");
                }
            }
            
            String ocrTextContent = ocrText.toString().trim();
            logger.info("[OCR+DeepSeek] OCR识别到的文本内容:\n{}", ocrTextContent);
            
            // 调用 DeepSeek API 解析订单
            String parsedResult = callDeepSeekAPI(ocrTextContent);
            logger.info("[OCR+DeepSeek] DeepSeek 解析完成，结果: {}", parsedResult);
            
            // 解析 DeepSeek 返回的 JSON
            JSONObject resultJson = new JSONObject(parsedResult);
            Map<String, Object> resultMap = jsonObjectToMap(resultJson);
            
            // 提取 items 列表
            Object itemsObj = resultMap.get("items");
            List<Map<String, Object>> itemsList = null;
            if (itemsObj instanceof List) {
                itemsList = (List<Map<String, Object>>) itemsObj;
                logger.info("[OCR+DeepSeek] 解析到 {} 个商品", itemsList.size());
            }
            
            // 返回 OCR 结果和解析后的商品列表（使用链式调用）
            logger.info("[OCR+DeepSeek] 返回给前端，商品数量: {}", itemsList != null ? itemsList.size() : 0);
            return R.ok().put("ocrText", ocrTextContent)
                         .put("items", itemsList != null ? itemsList : new ArrayList<>())
                         .put("parsedResult", resultMap); // 保留完整解析结果，便于调试
        } catch (TencentCloudSDKException e) {
            // 处理腾讯云 SDK 异常
            String errorCode = e.getErrorCode();
            String errorMessage = e.getMessage();
            logger.error("[OCR] 腾讯云 SDK 异常 - Code: {}, Message: {}", errorCode, errorMessage, e);
            
            // 针对不同错误类型返回更友好的提示
            if ("ResourceUnavailable.ResourcePackageRunOut".equals(errorCode)) {
                // 资源包耗尽错误
                return R.error("OCR识别失败: 账号资源包已耗尽。请前往腾讯云控制台检查资源包状态，或购买新的资源包，或开启后付费模式。数据统计可能存在10-20分钟延迟。");
            } else if (errorCode != null && errorCode.contains("AuthFailure")) {
                // 认证失败
                return R.error("OCR识别失败: 认证失败，请检查 SecretId 和 SecretKey 配置是否正确。");
            } else if (errorCode != null && errorCode.contains("InvalidParameter")) {
                // 参数错误
                return R.error("OCR识别失败: 请求参数错误 - " + errorMessage);
            } else {
                // 其他错误
                return R.error("OCR识别失败: " + errorMessage + " (错误代码: " + errorCode + ")");
            }
        } catch (Exception e) {
            // 处理其他异常
            logger.error("[OCR] 系统错误: {}", e.getMessage(), e);
            return R.error("系统错误: " + e.getMessage());
        }
    }
//
//    /**
//     * OCR 识别并解析订单接口
//     *
//     * @param request 请求参数，包含 ImageBase64（图片的Base64编码字符串，不含data:image前缀）
//     * @return 解析后的订单商品列表
//     */
//    @RequestMapping(value = "/recognizeAndParse", method = RequestMethod.POST)
//    @ResponseBody
//    public R recognizeAndParse(@RequestBody Map<String, String> request) {
//        try {
//            // 获取图片 Base64
//            String imageBase64 = request.get("ImageBase64");
//            if (imageBase64 == null || imageBase64.isEmpty()) {
//                logger.warn("[OCR] 图片数据为空");
//                return R.error("图片数据不能为空");
//            }
//
//            // 处理 Base64 前缀（如果前端传了 data:image 前缀，需要去掉）
//            if (imageBase64.contains(",")) {
//                imageBase64 = imageBase64.substring(imageBase64.indexOf(",") + 1);
//            }
//
//            logger.info("[OCR+DeepSeek] 开始识别和解析，图片大小: {} bytes", imageBase64.length());
//
//            // 1. 调用 OCR API 识别文字
//            Credential cred = new Credential(secretId, secretKey);
//            HttpProfile httpProfile = new HttpProfile();
//            httpProfile.setEndpoint("ocr.tencentcloudapi.com");
//            ClientProfile clientProfile = new ClientProfile();
//            clientProfile.setHttpProfile(httpProfile);
//            OcrClient client = new OcrClient(cred, region, clientProfile);
//            GeneralAccurateOCRRequest req = new GeneralAccurateOCRRequest();
//            req.setImageBase64(imageBase64);
//            GeneralAccurateOCRResponse resp = client.GeneralAccurateOCR(req);
//
//            // 2. 提取所有识别到的文本
//            TextDetection[] textDetections = resp.getTextDetections();
//            if (textDetections == null || textDetections.length == 0) {
//                logger.warn("[OCR+DeepSeek] 未识别到任何文本");
//                return R.error("未识别到任何文本");
//            }
//
//            logger.info("[OCR+DeepSeek] OCR识别成功，识别到 {} 条文本", textDetections.length);
//
//            // 拼接所有文本
//            StringBuilder ocrText = new StringBuilder();
//            for (TextDetection detection : textDetections) {
//                String text = detection.getDetectedText();
//                if (text != null && !text.trim().isEmpty()) {
//                    ocrText.append(text).append("\n");
//                }
//            }
//
//            String ocrTextContent = ocrText.toString().trim();
//            logger.info("[OCR+DeepSeek] OCR识别到的文本内容:\n{}", ocrTextContent);
//
//            // 3. 调用 DeepSeek API 解析订单
//            logger.info("[OCR+DeepSeek] 开始调用 DeepSeek API 进行解析...");
//            String parsedResult = callDeepSeekAPI(ocrTextContent);
//            logger.info("[OCR+DeepSeek] DeepSeek 解析完成，结果: {}", parsedResult);
//
//            // 4. 解析 DeepSeek 返回的 JSON
//            JSONObject resultJson = new JSONObject(parsedResult);
//
//            // 构造返回结果（将 JSONObject 转换为 Map）
//            Map<String, Object> resultMap = jsonObjectToMap(resultJson);
//
//            return R.ok().put("ocrText", ocrTextContent).put("parsedResult", resultMap);
//
//        } catch (TencentCloudSDKException e) {
//            String errorCode = e.getErrorCode();
//            String errorMessage = e.getMessage();
//            logger.error("[OCR] 腾讯云 SDK 异常 - Code: {}, Message: {}", errorCode, errorMessage, e);
//
//            if ("ResourceUnavailable.ResourcePackageRunOut".equals(errorCode)) {
//                return R.error("OCR识别失败: 账号资源包已耗尽。请前往腾讯云控制台检查资源包状态，或购买新的资源包，或开启后付费模式。数据统计可能存在10-20分钟延迟。");
//            } else if (errorCode != null && errorCode.contains("AuthFailure")) {
//                return R.error("OCR识别失败: 认证失败，请检查 SecretId 和 SecretKey 配置是否正确。");
//            } else if (errorCode != null && errorCode.contains("InvalidParameter")) {
//                return R.error("OCR识别失败: 请求参数错误 - " + errorMessage);
//            } else {
//                return R.error("OCR识别失败: " + errorMessage + " (错误代码: " + errorCode + ")");
//            }
//        } catch (Exception e) {
//            logger.error("[OCR] 系统错误: {}", e.getMessage(), e);
//            return R.error("系统错误: " + e.getMessage());
//        }
//    }

    /**
     * 调用 DeepSeek API 解析订单文本
     * "拆分计算规则：\n\n" +
     *                 "firstPartAmount = amount 的 50% + 运费 + 服务费 + 金融手续费的全部。小数点后保留 1 位，四舍五入。\n\n" +
     *                 "secondPartAmount = amount 的 50% * 75%。小数点后保留 1 位，四舍五入。必须大于 0，不能为 0。\n\n" +
     *                 "secondPartPoints = amount 的 50% * 75%。小数点后保留 1 位，四舍五入。与 secondPartAmount 相同。\n\n" +
     */
    private String callDeepSeekAPI(String ocrText) throws IOException {
        // 创建带超时设置的 HTTP 客户端
        org.apache.http.client.config.RequestConfig requestConfig = org.apache.http.client.config.RequestConfig.custom()
                .setConnectTimeout(30000)      // 连接超时 30 秒
                .setSocketTimeout(120000)       // Socket 超时 120 秒（DeepSeek 可能需要较长时间）
                .setConnectionRequestTimeout(30000) // 从连接池获取连接超时 30 秒
                .build();
        
        CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
        HttpPost httpPost = new HttpPost(deepSeekApiUrl);

        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "deepseek-chat");
        
        JSONArray messages = new JSONArray();
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个生鲜订单智能解析助手。输入为订单截图的 OCR 文本，包含多个商品。\n\n" +
                "你的任务：\n\n" +
                "仅解析那些在截图中明确包含了 \"运费\" 和 \"服务费\" 的商品订单。\n\n" +
                "如果某个商品订单没有运费和服务费信息，可忽略不解析。\n\n" +
                "对于有运费和服务费的商品订单，提取以下字段：\n\n" +
                "goodsName: 商品名称（原文）\n\n" +
                "searchGoodsName: 从 goodsName 中提取一个简化的可搜索名称（例如如果原名称是 \"河南三级盒口蘑盒12盒\"，则提取 \"口蘑\"；如果原名称是 \"河北一级小件平菇泡沫箱4.5斤\"，则提取 \"平菇\"）\n\n" +
                "count: 商品数量（例如 \"1件\"）\n\n" +
                "amount: 商品总金额数字（例如 47.00，这是商品本身的金额，不包括运费、服务费、金融手续费）\n\n" +
                "weight: 净重数字和单位（例如 \"5.50斤\"）\n\n" +
                "originalWeight: 原订单净重量（与 weight 相同，保留原始净重信息）\n\n" +
                "注意：amount 必须是明细总金额（明细总金额 = 商品总金额 + 运费 + 服务费 + 金融手续费）。\n" +
                "忽略毛重、单价等无关字段，只保留净重。\n\n" +
                "拆分计算规则：\n\n" +
                "firstPartAmount = amount 的 50%。小数点后保留 1 位，四舍五入。\n\n" +
                "secondPartAmount = amount 的 50%。小数点后保留 1 位，四舍五入。必须大于 0，不能为 0。\n\n" +
                "secondPartPoints = amount 的 50%。小数点后保留 1 位，四舍五入。与 secondPartAmount 相同。\n\n" +
                "净重拆分规则：\n" +
                "  - 净重也对半拆分成两部分，如净重 \"5.50斤\"，则第一份净重数量为 2.75，规格为 \"斤\"；第二份净重数量为 2.75，规格为 \"斤\"。\n" +
                "  - firstPartWeightNumber: 第一份净重的数量（数字，保留 1 位小数）\n" +
                "  - firstPartWeightUnit: 第一份净重的规格单位（例如 \"斤\"、\"kg\"、\"g\"）\n" +
                "  - secondPartWeightNumber: 第二份净重的数量（数字，保留 1 位小数）\n" +
                "  - secondPartWeightUnit: 第二份净重的规格单位（与第一份相同）\n\n" +
                "数量也对半拆分，保留单位。例如 \"1件\" 拆分为 \"0.5件\" 和 \"0.5件\"。\n\n" +
                "商品名称中的数字也要对半拆分（数字保留 1 位小数）。例如 \"12盒\" 拆分为 \"6.0盒\"，\"五斤四包\" 拆分为 \"2.5斤2.0包\"。\n\n" +
                "输出 JSON：\n\n" +
                "{\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"goodsName\": \"...\",\n" +
                "      \"searchGoodsName\": \"...\",\n" +
                "      \"count\": \"...\",\n" +
                "      \"amount\": 0,\n" +
                "      \"weight\": \"...\",\n" +
                "      \"originalWeight\": \"...\",\n" +
                "      \"firstPartGoodsName\": \"...\",\n" +
                "      \"firstPartCount\": \"...\",\n" +
                "      \"firstPartAmount\": 0,\n" +
                "      \"firstPartWeightNumber\": 0,\n" +
                "      \"firstPartWeightUnit\": \"...\",\n" +
                "      \"secondPartGoodsName\": \"...\",\n" +
                "      \"secondPartCount\": \"...\",\n" +
                "      \"secondPartAmount\": 0,\n" +
                "      \"secondPartWeightNumber\": 0,\n" +
                "      \"secondPartWeightUnit\": \"...\",\n" +
                "      \"secondPartPoints\": 0\n" +
                "    }\n" +
                "  ]\n" +
                "}\n\n" +
                "请按照以上要求处理，确保 secondPartAmount 不为 0。");
        
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", ocrText);
        
        messages.put(systemMessage);
        messages.put(userMessage);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.3);

        // 设置请求头
        StringEntity entity = new StringEntity(requestBody.toString(), "UTF-8");
        entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + deepSeekApiKey);

        logger.info("[DeepSeek] 准备发送请求到 DeepSeek API");
        logger.info("[DeepSeek] 请求 URL: {}", deepSeekApiUrl);
        logger.info("[DeepSeek] API Key: {}...{}", deepSeekApiKey.substring(0, Math.min(10, deepSeekApiKey.length())), 
                deepSeekApiKey.length() > 10 ? deepSeekApiKey.substring(deepSeekApiKey.length() - 4) : "");
        logger.info("[DeepSeek] 请求体长度: {} 字符", requestBody.toString().length());
        logger.debug("[DeepSeek] 请求体内容: {}", requestBody.toString());

        // 执行请求
        logger.info("[DeepSeek] 正在执行 HTTP 请求...");
        long startTime = System.currentTimeMillis();
        
        CloseableHttpResponse response = null;
        try {
            logger.info("[DeepSeek] 开始调用 client.execute()...");
            response = client.execute(httpPost);
            long elapsedTime = System.currentTimeMillis() - startTime;
            logger.info("[DeepSeek] HTTP 请求完成，耗时: {} ms ({} 秒)", elapsedTime, elapsedTime / 1000.0);
            
            if (response == null) {
                logger.error("[DeepSeek] HTTP 响应为 null");
                throw new IOException("DeepSeek API 返回空响应");
            }
            
            int statusCode = response.getStatusLine().getStatusCode();
            logger.info("[DeepSeek] HTTP 响应状态码: {}", statusCode);
            
            if (statusCode != 200) {
                String errorContent = EntityUtils.toString(response.getEntity(), "UTF-8");
                logger.error("[DeepSeek] HTTP 错误响应: {}", errorContent);
                throw new IOException("DeepSeek API 返回错误状态码: " + statusCode + ", 响应: " + errorContent);
            }
            
            HttpEntity responseEntity = response.getEntity();
            String responseContent = EntityUtils.toString(responseEntity, "UTF-8");
            logger.info("[DeepSeek] 响应内容长度: {} 字符", responseContent.length());
            logger.info("[DeepSeek] 响应内容: {}", responseContent);

            // 解析响应
            JSONObject responseJson = new JSONObject(responseContent);
            JSONArray choices = responseJson.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                String content = message.getString("content");
                
                logger.info("[DeepSeek] 提取到的内容长度: {} 字符", content.length());
                
                // 提取 JSON 部分（去除可能的 markdown 代码块标记）
                content = content.trim();
                if (content.startsWith("```json")) {
                    content = content.substring(7);
                }
                if (content.startsWith("```")) {
                    content = content.substring(3);
                }
                if (content.endsWith("```")) {
                    content = content.substring(0, content.length() - 3);
                }
                content = content.trim();
                
                logger.info("[DeepSeek] 清理后的内容长度: {} 字符", content.length());
                logger.info("[DeepSeek] 清理后的内容: {}", content);
                
                EntityUtils.consume(responseEntity);
                response.close();
                client.close();
                
                return content;
            } else {
                EntityUtils.consume(responseEntity);
                response.close();
                client.close();
                throw new IOException("DeepSeek API 返回空结果");
            }
        } catch (org.apache.http.conn.ConnectTimeoutException e) {
            logger.error("[DeepSeek] 连接超时: {}", e.getMessage(), e);
            if (response != null) {
                try {
                    response.close();
                } catch (Exception ex) {
                    // ignore
                }
            }
            try {
                client.close();
            } catch (Exception ex) {
                // ignore
            }
            throw new IOException("DeepSeek API 连接超时: " + e.getMessage(), e);
        } catch (org.apache.http.conn.HttpHostConnectException e) {
            logger.error("[DeepSeek] 连接失败: {}", e.getMessage(), e);
            if (response != null) {
                try {
                    response.close();
                } catch (Exception ex) {
                    // ignore
                }
            }
            try {
                client.close();
            } catch (Exception ex) {
                // ignore
            }
            throw new IOException("DeepSeek API 连接失败: " + e.getMessage(), e);
        } catch (java.net.SocketTimeoutException e) {
            logger.error("[DeepSeek] Socket 超时: {}", e.getMessage(), e);
            if (response != null) {
                try {
                    response.close();
                } catch (Exception ex) {
                    // ignore
                }
            }
            try {
                client.close();
            } catch (Exception ex) {
                // ignore
            }
            throw new IOException("DeepSeek API 请求超时: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("[DeepSeek] 调用异常: {}", e.getMessage(), e);
            if (response != null) {
                try {
                    response.close();
                } catch (Exception ex) {
                    logger.error("[DeepSeek] 关闭响应失败", ex);
                }
            }
            try {
                client.close();
            } catch (Exception ex) {
                logger.error("[DeepSeek] 关闭客户端失败", ex);
            }
            throw new IOException("DeepSeek API 调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将 JSONObject 转换为 Map
     */
    private Map<String, Object> jsonObjectToMap(JSONObject jsonObject) {
        Map<String, Object> map = new HashMap<>();
        if (jsonObject != null) {
            for (String key : jsonObject.keySet()) {
                Object value = jsonObject.get(key);
                if (value instanceof JSONObject) {
                    map.put(key, jsonObjectToMap((JSONObject) value));
                } else if (value instanceof JSONArray) {
                    map.put(key, jsonArrayToList((JSONArray) value));
                } else {
                    map.put(key, value);
                }
            }
        }
        return map;
    }

    /**
     * 将 JSONArray 转换为 List
     */
    private List<Object> jsonArrayToList(JSONArray jsonArray) {
        List<Object> list = new ArrayList<>();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                Object value = jsonArray.get(i);
                if (value instanceof JSONObject) {
                    list.add(jsonObjectToMap((JSONObject) value));
                } else if (value instanceof JSONArray) {
                    list.add(jsonArrayToList((JSONArray) value));
                } else {
                    list.add(value);
                }
            }
        }
        return list;
    }

    /**
     * OCR 识别后保存订单和库存接口
     * 
     * @param request 请求参数，包含 items（商品列表）、depId（部门ID）、disId（分销商ID）
     * @return 保存结果
     */
    @RequestMapping(value = "/saveOrdersAndStock", method = RequestMethod.POST)
    @ResponseBody
    public R saveOrdersAndStock(@RequestBody Map<String, Object> request) {
        try {
            // 获取参数
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemsList = (List<Map<String, Object>>) request.get("items");
            Integer depId = (Integer) request.get("depId");
            Integer disId = (Integer) request.get("disId");

            if (itemsList == null || itemsList.isEmpty()) {
                return R.error("商品列表不能为空");
            }
            if (depId == null) {
                return R.error("部门ID不能为空");
            }
            if (disId == null) {
                return R.error("分销商ID不能为空");
            }

            logger.info("[OCR保存订单和库存] 开始处理，商品数量: {}, 部门ID: {}, 分销商ID: {}", 
                    itemsList.size(), depId, disId);

            // 获取部门信息
            NxDepartmentEntity depInfo = nxDepartmentService.queryDepInfo(depId);
            Integer depFatherId = depInfo != null ? depInfo.getNxDepartmentFatherId() : null;
            // 如果 depFatherId 是 0 或 null，则使用 depId
            if (depFatherId == null || depFatherId == 0) {
                depFatherId = depId;
            }

            List<Map<String, Object>> resultList = new ArrayList<>();
            
            // 累加所有订单的等待积分，最后一次性更新
            BigDecimal totalWaitingPoints = BigDecimal.ZERO;

            for (Map<String, Object> item : itemsList) {
                try {
                    String searchGoodsName = (String) item.get("searchGoodsName");
                    if (searchGoodsName == null || searchGoodsName.trim().isEmpty()) {
                        logger.warn("[OCR保存订单和库存] 跳过商品：searchGoodsName 为空");
                        continue;
                    }

                    logger.info("[OCR保存订单和库存] 处理商品: {}", searchGoodsName);

                    Map<String, Object> itemResult = new HashMap<>();
                    itemResult.put("searchGoodsName", searchGoodsName);
                    itemResult.put("status", "success");
                    itemResult.put("message", "");

                    // 统一流程：创建临时商品 → 保存订单 → 保存库存
                    NxDepartmentOrdersEntity order = saveOrderFromOcr(item, depId, disId, depFatherId);
                    
                    // 获取临时商品（订单中包含的商品信息）
                    NxDistributerGoodsEntity tempGoods = nxDistributerGoodsService.queryObject(order.getNxDoDisGoodsId());
                    if (tempGoods != null) {
                        // 保存库存（第二部分数据）
                        NxDistributerGoodsShelfStockEntity stock = saveStockFromOcr(item, tempGoods, disId, depFatherId);
                        itemResult.put("stockId", stock.getNxDistributerGoodsShelfStockId());
                    }
                    
                    // 累加等待积分（根据 secondPartPoints）
                    Object secondPartPointsObj = item.get("secondPartPoints");
                    if (secondPartPointsObj != null) {
                        try {
                            BigDecimal secondPartPoints = new BigDecimal(secondPartPointsObj.toString());
                            totalWaitingPoints = totalWaitingPoints.add(secondPartPoints);
                            logger.info("[OCR保存订单和库存] 累加等待积分: {} + {} = {}", 
                                    totalWaitingPoints.subtract(secondPartPoints), secondPartPoints, totalWaitingPoints);
                        } catch (Exception e) {
                            logger.warn("[OCR保存订单和库存] 解析等待积分失败", e);
                        }
                    }
                    
                    itemResult.put("orderId", order.getNxDepartmentOrdersId());
                    itemResult.put("goodsId", order.getNxDoDisGoodsId());
                    itemResult.put("type", "temp");
                    itemResult.put("message", "已保存为临时订单和库存");

                    resultList.add(itemResult);

                } catch (Exception e) {
                    logger.error("[OCR保存订单和库存] 处理商品时出错: {}", item, e);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("searchGoodsName", item.get("searchGoodsName"));
                    errorResult.put("status", "error");
                    errorResult.put("message", "处理失败: " + e.getMessage());
                    resultList.add(errorResult);
                }
            }
            
            // 所有订单处理完成后，一次性更新部门等待积分
            if (totalWaitingPoints.compareTo(BigDecimal.ZERO) > 0 && depInfo != null) {
                try {
                    // 重新从数据库加载最新的部门信息，确保获取到最新的等待积分
                    NxDepartmentEntity currentDepInfo = nxDepartmentService.queryDepInfo(depId);
                    if (currentDepInfo != null) {
                        // 获取当前的等待积分
                        String currentWaitingPoints = currentDepInfo.getNxDepartmentWaitingPoints();
                        BigDecimal currentWaitingPointsDecimal = (currentWaitingPoints != null && !currentWaitingPoints.isEmpty()) 
                            ? new BigDecimal(currentWaitingPoints) : BigDecimal.ZERO;
                        // 累加所有订单的等待积分
                        BigDecimal newWaitingPoints = currentWaitingPointsDecimal.add(totalWaitingPoints).setScale(1, BigDecimal.ROUND_HALF_UP);
                        currentDepInfo.setNxDepartmentWaitingPoints(newWaitingPoints.toString());
                        nxDepartmentService.update(currentDepInfo);
                        logger.info("[OCR保存订单和库存] 更新部门 {} 等待积分: {} + {} = {}", 
                                depId, currentWaitingPointsDecimal, totalWaitingPoints, newWaitingPoints);
                        // 更新内存中的 depInfo 对象，以便返回给前端
                        depInfo = currentDepInfo;
                    }
                } catch (Exception e) {
                    logger.warn("[OCR保存订单和库存] 更新部门等待积分失败", e);
                }
            }

            return R.ok().put("data", resultList)
                         .put("depInfo", depInfo)
                         .put("total", itemsList.size())
                         .put("success", resultList.stream().filter(r -> "success".equals(r.get("status"))).count());

        } catch (Exception e) {
            logger.error("[OCR保存订单和库存] 系统错误", e);
            return R.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 保存订单（统一流程：创建临时商品 → 保存订单）
     */
    private NxDepartmentOrdersEntity saveOrderFromOcr(Map<String, Object> item, Integer depId, Integer disId, Integer depFatherId) {
        logger.info("[OCR保存订单] 开始保存订单，depId: {}, disId: {}", depId, disId);
        
        // 商品名称（使用 searchGoodsName）
        String searchGoodsName = (String) item.get("searchGoodsName");
        String goodsName = searchGoodsName != null ? searchGoodsName : (String) item.get("goodsName");
        logger.info("[OCR保存临时订单] 商品名称: {}", goodsName);
        
        // 创建临时商品
        logger.info("[OCR保存订单] 准备创建临时商品...");
        NxDistributerGoodsEntity tempGoods = createTempGoods(goodsName, disId);
        logger.info("[OCR保存订单] 临时商品创建成功，商品ID: {}", tempGoods.getNxDistributerGoodsId());
        
        // 创建订单
        NxDepartmentOrdersEntity order = new NxDepartmentOrdersEntity();
        
        // 基本信息
        order.setNxDoDepartmentId(depId);
        order.setNxDoDepartmentFatherId(depFatherId);
        order.setNxDoDistributerId(disId);
        order.setNxDoStatus(0);
        
        // 设置商品相关信息（关联临时商品）
        order.setNxDoDisGoodsId(tempGoods.getNxDistributerGoodsId());
        order.setNxDoDisGoodsFatherId(tempGoods.getNxDgDfgGoodsFatherId());
        order.setNxDoDisGoodsGrandId(tempGoods.getNxDgDfgGoodsGrandId());
        order.setNxDoNxGoodsId(null); // 临时商品没有 nxGoodsId
        order.setNxDoNxGoodsFatherId(null);
        order.setNxDoGoodsName(goodsName);
        order.setNxDoGoodsType(-9);

        // 第一部分数据作为订单信息
        Object firstPartAmountObj = item.get("firstPartAmount");
        if (firstPartAmountObj != null) {
            BigDecimal firstPartAmount = new BigDecimal(firstPartAmountObj.toString());
            order.setNxDoSubtotal(firstPartAmount.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        }
        
        Object firstPartWeightNumberObj = item.get("firstPartWeightNumber");
        Object firstPartWeightUnitObj = item.get("firstPartWeightUnit");
        
        if (firstPartWeightNumberObj != null) {
            // 只使用数字，不带单位
            String weight = firstPartWeightNumberObj.toString();
            order.setNxDoWeight(weight);
            
            // 数量 = firstPartWeightNumber
            order.setNxDoQuantity(weight);
            
            // 计算单价：金额 / 重量
            if (firstPartAmountObj != null) {
                try {
                    BigDecimal amount = new BigDecimal(firstPartAmountObj.toString());
                    BigDecimal weightNum = new BigDecimal(firstPartWeightNumberObj.toString());
                    if (weightNum.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal price = amount.divide(weightNum, 2, BigDecimal.ROUND_HALF_UP);
                        order.setNxDoPrice(price.toString());
                    }
                } catch (Exception e) {
                    logger.warn("[OCR保存订单] 计算单价失败", e);
                }
            }
        }
        
        // 规格 = firstPartWeightUnit
        if (firstPartWeightUnitObj != null) {
            String unit = firstPartWeightUnitObj.toString();
            order.setNxDoStandard(unit);
            order.setNxDoPrintStandard(unit);
        } else if (tempGoods.getNxDgGoodsStandardname() != null) {
            // 如果没有单位，使用临时商品的默认规格作为后备
            order.setNxDoStandard(tempGoods.getNxDgGoodsStandardname());
            order.setNxDoPrintStandard(tempGoods.getNxDgGoodsStandardname());
        }


        String firstPartGoodsName = (String) item.get("firstPartGoodsName");
        if (firstPartGoodsName != null) {
            order.setNxDoRemark(firstPartGoodsName);
        }
        // 设置时间
        order.setNxDoArriveDate(formatWhatDate(0));
        order.setNxDoApplyDate(formatWhatDay(0));
        order.setNxDoArriveOnlyDate(formatWhatDate(0));
        order.setNxDoArriveWeeksYear(getWeekOfYear(0));
        order.setNxDoArriveWhatDay(getWeek(0));
        order.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        order.setNxDoApplyOnlyTime(formatWhatTime(0));
        order.setNxDoCostPriceLevel("1");
        
        // 其他字段
        order.setNxDoGbDistributerId(-1);
        order.setNxDoGbDepartmentId(-1);
        order.setNxDoGbDepartmentFatherId(-1);
        order.setNxDoPurchaseStatus(0); // 假设 1 是待采购状态
        order.setNxDoNxCommunityId(-1);
        order.setNxDoNxCommRestrauntId(-1);
        order.setNxDoNxCommRestrauntFatherId(-1);
        
        logger.info("[OCR保存订单] 准备保存订单到数据库...");
        logger.info("[OCR保存订单] 订单信息 - 商品ID: {}, 商品名称: {}, 部门ID: {}, 分销商ID: {}, 重量: {}, 金额: {}", 
                order.getNxDoDisGoodsId(), order.getNxDoGoodsName(), order.getNxDoDepartmentId(), 
                order.getNxDoDistributerId(), order.getNxDoWeight(), order.getNxDoSubtotal());
        nxDepartmentOrdersService.save(order);
        logger.info("[OCR保存订单] 订单保存成功！订单ID: {}", order.getNxDepartmentOrdersId());
        return order;
    }

    /**
     * 创建临时商品
     */
    private NxDistributerGoodsEntity createTempGoods(String goodsName, Integer disId) {
        logger.info("[OCR创建临时商品] 开始创建，商品名称: {}, 分销商ID: {}", goodsName, disId);
        
        // 查询临时父类（goodsLevel=2, nxGoodsId=-1）
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("nxGoodsId", -1);
        map.put("goodsLevel", 2);
        logger.info("[OCR创建临时商品] 查询临时父类，查询参数: {}", map);
        
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
        logger.info("[OCR创建临时商品] 查询到临时父类数量: {}", fatherGoodsEntities != null ? fatherGoodsEntities.size() : 0);
        
        if (fatherGoodsEntities == null || fatherGoodsEntities.isEmpty()) {
            logger.error("[OCR创建临时商品] 未找到临时商品父类，disId: {}", disId);
            throw new RuntimeException("未找到临时商品父类，请确保分销商已初始化临时商品分类");
        }
        
        NxDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
        logger.info("[OCR创建临时商品] 使用临时父类ID: {}", fatherGoodsEntity.getNxDistributerFatherGoodsId());
        
        // 创建临时商品
        NxDistributerGoodsEntity goods = new NxDistributerGoodsEntity();
        goods.setNxDgDfgGoodsFatherId(fatherGoodsEntity.getNxDistributerFatherGoodsId());
        goods.setNxDgDfgGoodsGrandId(fatherGoodsEntity.getNxDfgFathersFatherId());
        goods.setNxDgPurchaseAuto(-1);
        goods.setNxDgGoodsName(goodsName != null ? goodsName : "临时商品");
        
        // 生成拼音
        String finalGoodsName = goodsName != null ? goodsName : "临时商品";
        String pinyin = hanziToPinyin(finalGoodsName);
        String headPinyin = getHeadStringByString(finalGoodsName, false, null);
        goods.setNxDgGoodsPinyin(pinyin);
        goods.setNxDgGoodsPy(headPinyin);
        logger.info("[OCR创建临时商品] 拼音: {}, 简拼: {}", pinyin, headPinyin);
        
        goods.setNxDgDistributerId(disId);
        goods.setNxDgBuyingPriceIsGrade(0);
        goods.setNxDgBuyingPrice("0.1");
        goods.setNxDgWillPrice("0.1");
        goods.setNxDgBuyingPriceOne("0.1");
        goods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
        goods.setNxDgWillPriceOneStandard("斤"); // 默认规格
        goods.setNxDgWillPriceOne("0.1");
        goods.setNxDgWillPriceOneAboutPrice("0.1");
        goods.setNxDgGoodsIsHidden(0);
        goods.setNxDgGoodsStandardname("斤"); // 默认规格
        goods.setNxDgBuyingPriceUpdate(formatWhatDay(0));
        goods.setNxDgOutTotalWeight("0");
        goods.setNxDgGoodsStatus(0);
        
        logger.info("[OCR创建临时商品] 准备保存临时商品到数据库...");
        nxDistributerGoodsService.save(goods);
        logger.info("[OCR创建临时商品] 临时商品保存成功！商品ID: {}, 商品名称: {}", goods.getNxDistributerGoodsId(), goods.getNxDgGoodsName());
        
        // 更新父类商品数量
        Integer goodsAmount = fatherGoodsEntity.getNxDfgGoodsAmount();
        if (goodsAmount == null) {
            goodsAmount = 0;
        }
        logger.info("[OCR创建临时商品] 更新父类商品数量: {} -> {}", goodsAmount, goodsAmount + 1);
        fatherGoodsEntity.setNxDfgGoodsAmount(goodsAmount + 1);
        nxDistributerFatherGoodsService.update(fatherGoodsEntity);
        logger.info("[OCR创建临时商品] 父类商品数量更新完成");
        
        return goods;
    }


    /**
     * 保存库存（第二部分数据）
     */
    private NxDistributerGoodsShelfStockEntity saveStockFromOcr(Map<String, Object> item, NxDistributerGoodsEntity goodsEntity,Integer disId, Integer depFatherId) {
        NxDistributerGoodsShelfStockEntity stock = new NxDistributerGoodsShelfStockEntity();
        
        // 基本信息
        stock.setNxDgssNxDistributerId(disId);
        stock.setNxDgssNxDisGoodsId(goodsEntity.getNxDistributerGoodsId());
        stock.setNxDgssNxDisGoodsFatherId(goodsEntity.getNxDgDfgGoodsFatherId());
        stock.setNxDgssNxPurGoodsId(-1); // 无采购商品ID
        stock.setNxDgssNxDepartmentFatherId(depFatherId); // 设置部门父ID

        // 第二部分数据
        Object secondPartWeightNumberObj = item.get("secondPartWeightNumber");
        Object secondPartWeightUnitObj = item.get("secondPartWeightUnit");
        if (secondPartWeightNumberObj != null && secondPartWeightUnitObj != null) {
            String weight = secondPartWeightNumberObj.toString();
            stock.setNxDgssWeight(weight);
            stock.setNxDgssRestWeight(weight);
        }
        
        Object secondPartAmountObj = item.get("secondPartAmount");
        if (secondPartAmountObj != null) {
            BigDecimal secondPartAmount = new BigDecimal(secondPartAmountObj.toString());
            stock.setNxDgssSubtotal(secondPartAmount.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            stock.setNxDgssRestSubtotal(secondPartAmount.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            
            // 计算单价：金额 / 重量
            if (secondPartWeightNumberObj != null) {
                try {
                    BigDecimal amount = new BigDecimal(secondPartAmountObj.toString());
                    BigDecimal weightNum = new BigDecimal(secondPartWeightNumberObj.toString());
                    if (weightNum.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal price = amount.divide(weightNum, 2, BigDecimal.ROUND_HALF_UP);
                        stock.setNxDgssPrice(price.toString());
                    }
                } catch (Exception e) {
                    logger.warn("[OCR保存库存] 计算单价失败", e);
                }
            }
        }
        
        // 库存备注字段（使用 secondPartGoodsName）
        String secondPartGoodsName = (String) item.get("secondPartGoodsName");
        if (secondPartGoodsName != null) {
            stock.setNxDgssStockRemark(secondPartGoodsName);
        }
        
        // 设置时间
        stock.setNxDgssDate(formatWhatDay(0));
        stock.setNxDgssTimeStamp(getTimeStamp());
        stock.setNxDgssWeek(getWeek(0));
        stock.setNxDgssMonth(formatWhatMonth(0));
        stock.setNxDgssYear(formatWhatYear(0));
        stock.setNxDgssInventoryMonth(formatWhatMonth(0));
        stock.setNxDgssInventoryYear(formatWhatYear(0));
        
        // 其他字段
        stock.setNxDgssStatus(-1);
        stock.setNxDgssLossWeight("0");
        stock.setNxDgssLossSubtotal("0");
        stock.setNxDgssReturnWeight("0");
        stock.setNxDgssReturnSubtotal("0");
        stock.setNxDgssWasteWeight("0");
        stock.setNxDgssWasteSubtotal("0");
        stock.setNxDgssProduceWeight("0");
        stock.setNxDgssProduceSubtotal("0");
        
        nxDistributerGoodsShelfStockService.save(stock);
        return stock;
    }
}

