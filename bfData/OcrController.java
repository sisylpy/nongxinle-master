package com.nongxinle.controller;

import com.nongxinle.dto.PasteSearchGoodsResponseDTO;
import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.NxOrderOcrTrainingDataEntity;
import com.nongxinle.entity.NxDistributerFatherGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsShelfStockEntity;
import com.nongxinle.service.NxDepartmentOrdersService;
import com.nongxinle.service.NxDepartmentService;
import com.nongxinle.service.NxDistributerFatherGoodsService;
import com.nongxinle.service.NxDistributerGoodsService;
import com.nongxinle.service.NxDistributerGoodsShelfStockService;
import com.nongxinle.utils.R;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ocr.v20181119.OcrClient;
import com.tencentcloudapi.ocr.v20181119.models.GeneralAccurateOCRRequest;
import com.tencentcloudapi.ocr.v20181119.models.GeneralAccurateOCRResponse;
import com.tencentcloudapi.ocr.v20181119.models.TextDetection;
import com.tencentcloudapi.ocr.v20181119.models.Coord;
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
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // 图片大小限制：Base64 字符串最大 10MB（约对应 7.5MB 原图）
    private static final int MAX_IMAGE_BASE64_SIZE = 10 * 1024 * 1024; // 10MB

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
    private com.nongxinle.service.NxOrderOcrTrainingDataService nxOrderOcrTrainingDataService;

    
    @Autowired
    private OcrClient ocrClient; // 注入单例 OCR 客户端

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

            // 验证图片大小
            if (imageBase64.length() > MAX_IMAGE_BASE64_SIZE) {
                logger.warn("[OCR+DeepSeek] 图片过大: {} bytes (限制: {} bytes)", 
                    imageBase64.length(), MAX_IMAGE_BASE64_SIZE);
                return R.error("图片大小超过限制，最大支持 10MB（Base64 编码后）");
            }

            logger.info("[OCR+DeepSeek] 开始识别和解析，图片大小: {} bytes", imageBase64.length());

            // 使用注入的单例 OCR 客户端（不再每次创建新实例）
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
                    resp = ocrClient.GeneralAccurateOCR(req);
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


    /**
     * 创建或查询训练数据
     * 
     * @param depId 部门ID
     * @param depFatherId 部门父ID
     * @param disId 分销商ID
     * @param goodsName 商品名称
     * @param quantity 数量
     * @param spec 规格
     * @param standardWeight 规格重量
     * @param note 备注
     * @param userId 用户ID
     * @return 训练数据实体
     */
    private NxOrderOcrTrainingDataEntity createOrQueryTrainingData(
            Integer depId, Integer depFatherId, Integer disId,
            String goodsName, String quantity, String spec, String standardWeight, String note, Integer userId) {
        // 查询是否已有训练数据（使用传入的 goodsName）
        Map<String, Object> matchParams = new HashMap<>();
        matchParams.put("departmentId", depId);
        matchParams.put("goodsName", goodsName);
        NxOrderOcrTrainingDataEntity matchedTrainingData = nxOrderOcrTrainingDataService.queryByMatchFields(matchParams);

        if (matchedTrainingData != null) {
            logger.info("[recognizeOrder] 找到匹配的训练数据，训练数据ID: {}", matchedTrainingData.getNxOtdId());
            return matchedTrainingData;
        }
        
        // 注意：这个方法通常传入的是 rawName，如果需要在方法内部也尝试用 name 查询，
        // 需要修改方法签名增加 name 参数。目前双重查询逻辑已在调用处（recognizeOrder）实现。

        // 没有找到训练数据，创建新的训练数据
        logger.info("[recognizeOrder] 未找到匹配的训练数据，创建训练数据");

        NxOrderOcrTrainingDataEntity trainingData = new NxOrderOcrTrainingDataEntity();
        trainingData.setNxOtdDepartmentId(depId);
        trainingData.setNxOtdDepartmentFatherId(depFatherId);
        trainingData.setNxOtdDistributerId(disId);
        trainingData.setNxOtdOriginalGoodsName(goodsName);
        trainingData.setNxOtdOriginalQuantity(quantity);
        trainingData.setNxOtdOriginalStandard(spec);
        trainingData.setNxOtdOriginalStandardWeight(standardWeight);
        trainingData.setNxOtdOriginalRemark(note);
        trainingData.setNxOtdIsNameManuallyAnnotated(0);
        trainingData.setNxOtdIsQuantityManuallyAnnotated(0);
        trainingData.setNxOtdIsStandardManuallyAnnotated(0);
        trainingData.setNxOtdIsStandardWeightManuallyAnnotated(0);
        trainingData.setNxOtdIsRemarkManuallyAnnotated(0);
        // 最终确认字段不提前赋值，等待用户手动标注
        trainingData.setNxOtdFinalGoodsName(null);
        trainingData.setNxOtdFinalQuantity(null);
        trainingData.setNxOtdFinalStandard(null);
        trainingData.setNxOtdFinalStandardWeight(null);
        trainingData.setNxOtdFinalRemark(null);
        trainingData.setNxOtdDataSource("OCR_IMAGE");
        trainingData.setNxOtdCreateDate(formatWhatYearDayTime(0));
        trainingData.setNxOtdUpdateDate(formatWhatYearDayTime(0));
        trainingData.setNxOtdCreateUserId(userId);

        // 保存训练数据
        nxOrderOcrTrainingDataService.save(trainingData);
        logger.info("[recognizeOrder] 训练数据创建成功，训练数据ID: {}", trainingData.getNxOtdId());
        return trainingData;
    }

    /**
     * 创建订单实体（用于查询商品）
     * 
     * @param depId 部门ID
     * @param depFatherId 部门父ID
     * @param disId 分销商ID
     * @param userId 用户ID
     * @param goodsName 商品名称
     * @param quantity 数量（如果为空，使用默认值或训练数据的标注值）
     * @param spec 规格（如果为空，使用默认值或训练数据的标注值）
     * @param note 备注
     * @param orcNotice OCR识别提示信息（isNotice字段）
     * @param trainingDataId 训练数据ID
     * @param matchedTrainingData 匹配的训练数据（可为null，用于获取标注值）
     * @return 订单实体
     */
    private NxDepartmentOrdersEntity createOrderForGoodsSearch(
            Integer depId, Integer depFatherId, Integer disId, Integer userId,
            String goodsName, String quantity, String spec, String note, String orcNotice,
            Integer trainingDataId, NxOrderOcrTrainingDataEntity matchedTrainingData) {
        NxDepartmentOrdersEntity order = new NxDepartmentOrdersEntity();
        order.setNxDoDepartmentId(depId);
        order.setNxDoDepartmentFatherId(depFatherId);
        order.setNxDoDistributerId(disId);
        order.setNxDoOrderUserId(userId);
        order.setNxDoGoodsName(goodsName);
        order.setNxDoRemark(note != null ? note : "");
        order.setOrcNotice(orcNotice != null && !orcNotice.trim().isEmpty() ? orcNotice : null);
        order.setNxDoStatus(-2);
        order.setNxDoDisGoodsId(null);
        order.setNxDoPurchaseUserId(-1);
        order.setNxDoIsAgent(-1);
        order.setNxDistributerGoodsEntityList(new ArrayList<>());
        order.setNxDoTrainingDataId(trainingDataId);

        // 处理数量和规格：如果 deepseek 返回的规格和数量同时为空，则使用训练数据的标注后的数量和规格
        boolean isQuantityEmpty = (quantity == null || quantity.trim().isEmpty());
        boolean isSpecEmpty = (spec == null || spec.trim().isEmpty());

        if (isQuantityEmpty && isSpecEmpty && matchedTrainingData != null) {
            // 使用训练数据的标注值
            String finalQuantity = matchedTrainingData.getNxOtdFinalQuantity();
            String finalStandard = matchedTrainingData.getNxOtdFinalStandard();
            order.setNxDoQuantity(finalQuantity != null && !finalQuantity.trim().isEmpty() ? finalQuantity : "1");
            order.setNxDoStandard(finalStandard != null && !finalStandard.trim().isEmpty() ? finalStandard : "");
        } else {
            // 使用 deepseek 返回的值
            order.setNxDoQuantity(isQuantityEmpty ? "1" : quantity);
            order.setNxDoStandard(isSpecEmpty ? "斤" : spec);
        }

        return order;
    }

    /**
     * 将订单实体转换为响应DTO
     * 
     * @param order 订单实体
     * @param note 备注（使用原始备注，而不是订单中的备注）
     * @return 响应DTO
     */
    private PasteSearchGoodsResponseDTO convertOrderToResponseDTO(NxDepartmentOrdersEntity order, String note) {
        PasteSearchGoodsResponseDTO responseDTO = new PasteSearchGoodsResponseDTO();
        responseDTO.setNxDepartmentOrdersId(order.getNxDepartmentOrdersId());
        responseDTO.setNxDoGoodsName(order.getNxDoGoodsName());
        responseDTO.setNxDoQuantity(order.getNxDoQuantity());
        responseDTO.setNxDoStandard(order.getNxDoStandard());
        responseDTO.setNxDoRemark(note);
        responseDTO.setNxDoStatus(order.getNxDoStatus());
        responseDTO.setNxDoDepartmentId(order.getNxDoDepartmentId());
        responseDTO.setNxDoDepartmentFatherId(order.getNxDoDepartmentFatherId());
        responseDTO.setNxDoDisGoodsId(order.getNxDoDisGoodsId());
        responseDTO.setNxDoDistributerId(order.getNxDoDistributerId());
        responseDTO.setNxDoOrderUserId(order.getNxDoOrderUserId());
        return responseDTO;
    }

    /**
     * 语音识别转订单接口
     * 接收图片 -> OCR识别 -> DeepSeek转换为订单
     * 
     * @param request 请求参数：
     *                - ImageBase64: 图片的Base64编码（必填）
     *                - depId: 部门ID（可选，如果提供则自动查询商品并保存订单）
     *                - disId: 分销商ID（可选，如果提供则自动查询商品并保存订单）
     *                - depFatherId: 部门父ID（可选）
     *                - userId: 用户ID（可选）
     * @return 识别结果和解析后的订单商品列表，如果提供了depId和disId，则返回查询和保存结果
     */
    @RequestMapping(value = "/recognizeOrder", method = RequestMethod.POST)
    @ResponseBody
    public R recognizeOrder(@RequestBody Map<String, Object> request) {
        try {
            // 获取图片 Base64
            Object imageBase64Obj = request.get("ImageBase64");
            if (imageBase64Obj == null) {
                logger.warn("[recognizeOrder] 图片数据为空");
                return R.error("图片数据不能为空");
            }
            String imageBase64 = imageBase64Obj.toString();
            if (imageBase64.isEmpty()) {
                logger.warn("[recognizeOrder] 图片数据为空");
                return R.error("图片数据不能为空");
            }

            // 处理 Base64 前缀（如果前端传了 data:image 前缀，需要去掉）
            if (imageBase64.contains(",")) {
                imageBase64 = imageBase64.substring(imageBase64.indexOf(",") + 1);
            }

            // 验证图片大小
            if (imageBase64.length() > MAX_IMAGE_BASE64_SIZE) {
                logger.warn("[recognizeOrder] 图片过大: {} bytes (限制: {} bytes)", 
                    imageBase64.length(), MAX_IMAGE_BASE64_SIZE);
                return R.error("图片大小超过限制，最大支持 10MB（Base64 编码后）");
            }

            logger.info("[recognizeOrder] 开始识别和解析，图片大小: {} bytes", imageBase64.length());

            // 使用注入的单例 OCR 客户端（不再每次创建新实例）
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
                    logger.info("[recognizeOrder] 尝试调用 OCR API (第 {} 次)", retryCount + 1);
                    resp = ocrClient.GeneralAccurateOCR(req);
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
                            logger.warn("[recognizeOrder] OCR API 调用失败，准备重试 (第 {} 次): {}", retryCount, errorMessage);
                            try {
                                Thread.sleep(1000 * retryCount); // 等待后重试，递增等待时间
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                            continue;
                        } else {
                            logger.error("[recognizeOrder] OCR API 调用失败，已达到最大重试次数: {}", errorMessage);
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
            logger.info("[recognizeOrder] OCR识别成功，识别到 {} 条文本", textCount);

            if (textDetections == null || textDetections.length == 0) {
                logger.warn("[recognizeOrder] 未识别到任何文本");
                return R.error("未识别到任何文本");
            }

            if (textDetections != null && textDetections.length > 0) {
                logger.info("========== [recognizeOrder] OCR 识别结果 ==========");
                for (int i = 0; i < textDetections.length; i++) {
                    TextDetection detection = textDetections[i];
                    String detectedText = detection.getDetectedText();
                    Long confidence = detection.getConfidence();
                    
                    // 获取坐标信息并正确打印
                    Coord[] polygon = detection.getPolygon();
                    String coordInfo = polygonToString(polygon);
                    
                    logger.info("[{}] 文本: {} | 置信度: {} | 坐标: {}", i + 1, detectedText, confidence, coordInfo);
                }
                logger.info("====================================");
            }

            // 自动调用 DeepSeek API 进行解析
            logger.info("[OCR+DeepSeek] 开始调用 DeepSeek 进行解析...");

            // ========== 按坐标分行分列预处理 ==========
            List<OcrBox> boxes = new ArrayList<>();
            for (TextDetection detection : textDetections) {
                String text = detection.getDetectedText();
                if (text == null || text.trim().isEmpty()) {
                    continue;
                }
                
                String trimmedText = text.trim();
                
                Long confidence = detection.getConfidence();
                
                // 过滤噪声和无关文本（只过滤真正的噪声，不要过滤商品名）
                if (isNoise(trimmedText)) {
                    logger.info("[recognizeOrder] 过滤噪声: {} (置信度: {})", trimmedText, confidence);
                    continue;
                }
                if (isIrrelevantText(trimmedText)) {
                    logger.info("[recognizeOrder] 过滤无关文本: {} (置信度: {})", trimmedText, confidence);
                    continue;
                }
                
                // 放宽置信度阈值：只过滤极低置信度的文本（低于50%），保留商品名
                // 注意：商品名即使置信度较低（如 57-80）也应该保留，让后续聚类和解析处理
                if (confidence != null && confidence < 50) {
                    logger.info("[recognizeOrder] 过滤极低置信度文本: {} (置信度: {})", trimmedText, confidence);
                    continue;
                }
                
                // 获取坐标信息
                Coord[] polygon = detection.getPolygon();
                if (polygon == null || polygon.length == 0) {
                    logger.debug("[recognizeOrder] 跳过无坐标文本: {}", trimmedText);
                    continue;
                }
                
                boxes.add(new OcrBox(trimmedText, confidence != null ? confidence : 0, polygon));
            }
            
            logger.info("[recognizeOrder] 有效 OCR 框数量: {}", boxes.size());
            
            // 打印所有有效框的详细信息（用于调试）
            for (int i = 0; i < boxes.size(); i++) {
                OcrBox box = boxes.get(i);
                logger.info("[recognizeOrder] 有效框[{}]: text={}, conf={}, cx={}, cy={}, height={}", 
                    i + 1, box.text, box.conf, box.cx, box.cy, box.height);
            }
            
            // 按 y 坐标聚类成行
            List<List<OcrBox>> rows = groupByRows(boxes);
            logger.info("[recognizeOrder] 聚类后行数: {}", rows.size());
            
            // 打印每一行的详细内容（用于调试）
            for (int i = 0; i < rows.size(); i++) {
                List<OcrBox> row = rows.get(i);
                StringBuilder rowText = new StringBuilder();
                for (OcrBox box : row) {
                    rowText.append("[").append(box.text).append("(y=").append(box.cy).append(")] ");
                }
                logger.info("[recognizeOrder] Row#{}: {}", i + 1, rowText.toString().trim());
            }
            
            // 构建纯文本内容，按行组织，发送给 DeepSeek 解析
            StringBuilder ocrText = new StringBuilder();
            ocrText.append("以下是 OCR 识别到的订单文本，已按行组织：\n\n");
            
            for (int i = 0; i < rows.size(); i++) {
                List<OcrBox> row = rows.get(i);
                // 将同一行的文本用空格连接
                StringBuilder rowText = new StringBuilder();
                for (OcrBox box : row) {
                    if (rowText.length() > 0) {
                        rowText.append(" ");
                    }
                    rowText.append(box.text);
                }
                if (rowText.length() > 0) {
                    // 清洗 OCR 文本行
                    String cleanedLine = normalizeOcrLine(rowText.toString());
                    ocrText.append(cleanedLine).append("\n");
                }
            }

            String ocrTextContent = ocrText.toString().trim();
            logger.info("[recognizeOrder] 构建的 OCR 文本（按行组织）:\n{}", ocrTextContent);
            
            // 调用 DeepSeek API 解析订单
            logger.info("[recognizeOrder] 开始调用 DeepSeek API 解析订单...");
            String parsedResult;
            try {
                parsedResult = callDeepSeekAPIForOrder(ocrTextContent);
                logger.info("[recognizeOrder] DeepSeek 解析完成，结果: {}", parsedResult);
            } catch (Exception e) {
                logger.error("[recognizeOrder] DeepSeek API 调用失败: {}", e.getMessage(), e);
                return R.error("订单解析失败：DeepSeek API 调用失败 - " + e.getMessage());
            }
            
            // 解析 DeepSeek 返回的 JSON（直接使用原始数据，不做额外转换）
            List<Map<String, Object>> itemsList = new ArrayList<>();
            
            try {
                // 清理并解析 JSON
                String cleanedResult = parsedResult.trim();
                if (cleanedResult.startsWith("[")) {
                    // 直接是 JSON 数组
                    JSONArray jsonArray = new JSONArray(cleanedResult);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject item = jsonArray.getJSONObject(i);
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("name", item.optString("name", ""));
                        itemMap.put("rawName", item.optString("rawName", "")); // 原始商品名称（OCR识别的原始名称）
                        itemMap.put("quantity", item.optString("quantity", ""));
                        itemMap.put("spec", item.optString("spec", ""));
                        itemMap.put("standardWeight", item.optString("standardWeight", ""));
                        itemMap.put("note", item.optString("note", ""));
                        itemMap.put("isNotice", item.optString("isNotice", ""));
                        itemsList.add(itemMap);
                    }
                    } else {
                    // 可能是包含 items 或 orderItems 字段的对象
                    JSONObject jsonObj = new JSONObject(cleanedResult);
                    JSONArray jsonArray = null;
                    
                    // 优先检查 orderItems 字段（DeepSeek 新格式）
                    if (jsonObj.has("orderItems")) {
                        jsonArray = jsonObj.getJSONArray("orderItems");
                        logger.info("[recognizeOrder] 检测到 orderItems 字段，商品数量: {}", jsonArray.length());
                    } else if (jsonObj.has("items")) {
                        // 兼容旧格式 items 字段
                        jsonArray = jsonObj.getJSONArray("items");
                        logger.info("[recognizeOrder] 检测到 items 字段，商品数量: {}", jsonArray.length());
                    }
                    
                    if (jsonArray != null) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject item = jsonArray.getJSONObject(i);
                            Map<String, Object> itemMap = new HashMap<>();
                            itemMap.put("name", item.optString("name", ""));
                            itemMap.put("rawName", item.optString("rawName", "")); // 原始商品名称（OCR识别的原始名称）
                            itemMap.put("quantity", item.optString("quantity", ""));
                            itemMap.put("spec", item.optString("spec", ""));
                            itemMap.put("standardWeight", item.optString("standardWeight", ""));
                            itemMap.put("note", item.optString("note", ""));
                            itemMap.put("isNotice", item.optString("isNotice", ""));
                            itemsList.add(itemMap);
                        }
                    } else {
                        logger.warn("[recognizeOrder] JSON 对象中未找到 items 或 orderItems 字段");
                    }
                }
                
                // 本地判断：如果规格为空，并且数量是2位数，则拆分数量和规格
                // 同时进行字段映射：将 DeepSeek 返回的字段映射到前端期望的字段格式
                for (Map<String, Object> item : itemsList) {
                    String spec = item.get("spec") != null ? item.get("spec").toString().trim() : "";
                    String quantity = item.get("quantity") != null ? item.get("quantity").toString().trim() : "";
                    
                    // 手写体识别：如果规格是 "h"，转换为 "斤"
                    if ("h".equalsIgnoreCase(spec)) {
                        spec = "斤";
                        item.put("spec", spec);
                        logger.info("[recognizeOrder] 手写体识别：规格 'h' 转换为 '斤'");
                    }
                    
                    // 如果规格为空，并且数量是2位数（10-99）
                    if (spec.isEmpty() && quantity.matches("^\\d{2}$")) {
                        // 把第一位数字作为订货数量，第二位数字作为订货规格"个"
                        String firstDigit = quantity.substring(0, 1);
                        item.put("quantity", firstDigit);
                        item.put("spec", "个");
                        logger.info("[recognizeOrder] 拆分2位数数量: quantity={} -> quantity={}, spec={}", 
                            quantity, firstDigit, "个");
                        // 更新变量以便后续使用
                        quantity = firstDigit;
                        spec = "个";
                    }
                    
                    // 字段映射：将 DeepSeek 返回的字段映射到前端期望的字段格式
                    // DeepSeek 返回：quantity, spec
                    // 前端期望：qty, unit
                    item.put("qty", quantity);
                    item.put("unit", spec);
                    // 保留原始字段以便兼容
                    item.put("remark", item.get("note") != null ? item.get("note").toString().trim() : "");
                }
                
                logger.info("[recognizeOrder] DeepSeek 解析成功，解析到 {} 个商品", itemsList.size());
            } catch (Exception e) {
                logger.error("[recognizeOrder] DeepSeek 返回结果解析失败: {}", e.getMessage(), e);
                return R.error("订单解析失败：DeepSeek 返回结果解析失败 - " + e.getMessage());
            }
                
                // 验证解析结果
                if (itemsList == null || itemsList.isEmpty() || !isValidParseResult(itemsList)) {
                logger.error("[recognizeOrder] DeepSeek 解析结果无效（为空或格式不正确）");
                return R.error("订单解析失败：DeepSeek 解析结果无效");
            }



            // 如果提供了 depId 和 disId，则调用 pasteSearchGoods 方法查询商品并保存订单
            Object depIdObj = request.get("depId");
            Object disIdObj = request.get("disId");
            
            if (depIdObj != null && disIdObj != null  && !itemsList.isEmpty()) {
                try {
                    Integer depId = null;
                    Integer disId = null;
                    Integer depFatherId = null;
                    Integer userId = null;

                    // 解析参数
                    if (depIdObj instanceof Number) {
                        depId = ((Number) depIdObj).intValue();
                    } else {
                        depId = Integer.parseInt(depIdObj.toString());
                    }

                    if (disIdObj instanceof Number) {
                        disId = ((Number) disIdObj).intValue();
                    } else {
                        disId = Integer.parseInt(disIdObj.toString());
                    }

                    Object depFatherIdObj = request.get("depFatherId");
                    if (depFatherIdObj != null) {
                        if (depFatherIdObj instanceof Number) {
                            depFatherId = ((Number) depFatherIdObj).intValue();
                        } else {
                            depFatherId = Integer.parseInt(depFatherIdObj.toString());
                        }
                    } else {
                        // 如果没有提供 depFatherId，从部门信息中获取
                        NxDepartmentEntity depInfo = nxDepartmentService.queryDepInfo(depId);
                        if (depInfo != null) {
                            depFatherId = depInfo.getNxDepartmentFatherId();
                            if (depFatherId == null || depFatherId == 0) {
                                depFatherId = depId;
                            }
                        } else {
                            depFatherId = depId;
                        }
                    }

                    Object userIdObj = request.get("userId");
                    if (userIdObj != null) {
                        if (userIdObj instanceof Number) {
                            userId = ((Number) userIdObj).intValue();
                        } else {
                            userId = Integer.parseInt(userIdObj.toString());
                        }
                    } else {
                        userId = -1; // 默认值
                    }

                    logger.info("[recognizeOrder] 开始处理订单，depId: {}, disId: {}, depFatherId: {}, userId: {}",
                            depId, disId, depFatherId, userId);

                    // 处理每条识别数据
                    List<NxDepartmentOrdersEntity> orderList = new ArrayList<>();
                    // 使用 Map 保存原始索引对应的响应，以保持顺序
                    Map<Integer, PasteSearchGoodsResponseDTO> responseMap = new HashMap<>();
                    List<Integer> orderIndexList = new ArrayList<>(); // 保存 orderList 中每个订单对应的原始索引
                    // 保存订单索引和 rawName 的对应关系，用于在 searchGoods 后找不到商品时创建训练数据
                    Map<Integer, String> orderIndexToRawNameMap = new HashMap<>();
                    // 保存订单索引和 matchedByRawName 的对应关系
                    Map<Integer, Boolean> orderIndexToMatchedByRawNameMap = new HashMap<>();

                    for (int i = 0; i < itemsList.size(); i++) {
                        Map<String, Object> item = itemsList.get(i);

                        // 提取字段（从 itemsList 中提取，包含 quantity、spec 和 isNotice 字段）
                        // 订单中的商品名称使用 name（纠错后的名称）
                        String goodsName = item.get("name") != null ? item.get("name").toString().trim() : "";
                        // 训练数据使用 rawName（原始OCR识别的名称），如果没有 rawName 则使用 name
                        String rawName = item.get("rawName") != null && !item.get("rawName").toString().trim().isEmpty()
                                ? item.get("rawName").toString().trim() : goodsName;
                        String quantity = item.get("quantity") != null ? item.get("quantity").toString().trim() : "";
                        String spec = item.get("spec") != null ? item.get("spec").toString().trim() : "";
                        String standardWeight = item.get("standardWeight") != null ? item.get("standardWeight").toString().trim() : "";
                        String note = item.get("note") != null ? item.get("note").toString().trim() : "";
                        String isNotice = item.get("isNotice") != null ? item.get("isNotice").toString().trim() : "";

                        if (goodsName.isEmpty()) {
                            logger.warn("[recognizeOrder] 跳过商品：名称为空");
                            continue;
                        }

                        // 判断是否缺少数量或规格
                        boolean isDataMissing = (quantity == null || quantity.trim().isEmpty()) || (spec == null || spec.trim().isEmpty());
                        if (isDataMissing) {
                            logger.info("[recognizeOrder] 订单缺少数量或规格，创建订单并查询商品: goodsName={}, quantity={}, spec={}",
                                goodsName, quantity, spec);
                        }

                        // 查询训练表中是否有相同内容（匹配：部门ID + 原始商品名称 rawName）
                        Map<String, Object> matchParams = new HashMap<>();
                        matchParams.put("departmentId", depId);
                        matchParams.put("goodsName", rawName); // 使用 rawName 查询训练数据
                            NxOrderOcrTrainingDataEntity matchedTrainingData = nxOrderOcrTrainingDataService.queryByMatchFields(matchParams);

                        // 记录是否用 rawName 找到的训练数据（检查训练数据的 original_goods_name 是否等于 rawName）
                        boolean matchedByRawName = (matchedTrainingData != null && 
                                matchedTrainingData.getNxOtdOriginalGoodsName() != null && 
                                matchedTrainingData.getNxOtdOriginalGoodsName().equals(rawName));

                        // 如果使用 rawName 没找到，且 rawName 和 name 不同，再用 name 查询一次（避免创建冗余训练数据）
                        if (matchedTrainingData == null && !rawName.equals(goodsName)) {
                            logger.info("[recognizeOrder] 使用 rawName='{}' 未找到训练数据，尝试使用 name='{}' 查询", rawName, goodsName);
                            matchParams.put("goodsName", goodsName);
                            matchedTrainingData = nxOrderOcrTrainingDataService.queryByMatchFields(matchParams);
                            if (matchedTrainingData != null) {
                                logger.info("[recognizeOrder] 使用 name='{}' 找到匹配的训练数据，训练数据ID: {}", goodsName, matchedTrainingData.getNxOtdId());
                            }
                        }

                        if (matchedTrainingData != null) {
                            // 找到了训练数据，使用已有的训练数据，不创建新的训练数据
                            if (matchedTrainingData.getNxOtdDisGoodsId() != null) {
                                // 训练数据中有商品ID，直接创建订单
                                logger.info("[recognizeOrder] 找到匹配的训练数据，商品ID: {}, 直接创建订单",
                                        matchedTrainingData.getNxOtdDisGoodsId());

                                // 查询商品信息
                                NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(
                                        matchedTrainingData.getNxOtdDisGoodsId());
                                if (disGoodsEntity == null) {
                                    logger.error("[recognizeOrder] 商品不存在，商品ID: {}",
                                            matchedTrainingData.getNxOtdDisGoodsId());
                                    throw new RuntimeException("商品不存在，商品ID: " + matchedTrainingData.getNxOtdDisGoodsId());
                                }

                                // 优先使用训练数据的 final_goods_name（如果存在），否则使用 DeepSeek 纠错后的 name
                                String finalGoodsName = matchedTrainingData.getNxOtdFinalGoodsName();
                                String orderGoodsName = (finalGoodsName != null && !finalGoodsName.trim().isEmpty()) 
                                        ? finalGoodsName 
                                        : goodsName;
                                if (!orderGoodsName.equals(goodsName)) {
                                    logger.info("[recognizeOrder] 使用训练数据的 final_goods_name='{}' 替代 DeepSeek 的 name='{}'", 
                                            orderGoodsName, goodsName);
                                }
                                
                                // 创建基本订单实体
                                NxDepartmentOrdersEntity order = createOrderForGoodsSearch(
                                        depId, depFatherId, disId, userId,
                                        orderGoodsName, quantity, spec, note, isNotice,
                                        matchedTrainingData.getNxOtdId(), matchedTrainingData);

                                // 根据匹配情况设置订单状态：
                                // 1. 如果训练数据是用 rawName 的 original_goods_name 匹配到的，且 rawName == name，说明没有纠错，状态应该是 0（已完成）
                                // 2. 如果训练数据不是用 rawName 的 original_goods_name 匹配到的，且 rawName != name，说明可能存在 DeepSeek 纠错错误，状态应该是 -2（待修正）
                                if (matchedByRawName && rawName.equals(goodsName)) {
                                    // 正常情况：rawName 匹配到训练数据，且没有纠错，状态设置为 0
                                    logger.info("[recognizeOrder] rawName='{}' 匹配到训练数据，且没有纠错，订单状态设置为 0（已完成）", rawName);
                                    order.setNxDoStatus(0);
                                } else if (!matchedByRawName && !rawName.equals(goodsName)) {
                                    // 异常情况：rawName 未匹配到训练数据，但 name 匹配到了，可能存在 DeepSeek 纠错错误，状态保持为 -2
                                    logger.warn("[recognizeOrder] rawName='{}' 在训练数据中未找到匹配的 original_goods_name，但 name='{}' 找到了商品，可能存在 DeepSeek 纠错错误，订单状态保持为 -2（待修正）", 
                                            rawName, goodsName);
                                    order.setNxDoStatus(-2);
                                }
                                // 其他情况（如 matchedByRawName == true 但 rawName != name，或 matchedByRawName == false 但 rawName == name）保持默认状态 -2

                                // 使用 saveOrderWithGoods 保存订单（包含完整的价格处理、部门商品等逻辑）
                                order = nxDepartmentOrdersService.saveOrderWithGoods(order, disGoodsEntity);
                                
                                // 兜底逻辑：如果训练数据不是用 rawName 的 original_goods_name 匹配到的，且 rawName != name
                                // 再次确认订单状态为 -2（防止 saveOrderWithGoods 内部逻辑覆盖）
                                if (!matchedByRawName && !rawName.equals(goodsName)) {
                                    if (order.getNxDoStatus() != null && order.getNxDoStatus() != -2) {
                                        logger.warn("[recognizeOrder] 订单状态被覆盖，重新设置为 -2（待修正），订单ID: {}", order.getNxDepartmentOrdersId());
                                        order.setNxDoStatus(-2);
                                        nxDepartmentOrdersService.update(order);
                                    }
                                    // 注意：不在这里创建训练数据，因为已经找到商品了，不需要创建训练数据
                                }

                                // 转换为响应DTO
                                PasteSearchGoodsResponseDTO responseDTO = convertOrderToResponseDTO(order, note);
                                // 保存到 Map 中，使用原始索引作为 key，保持顺序
                                responseMap.put(i, responseDTO);
                            } else {
                                // 训练数据中没有商品ID，使用已有的训练数据，创建订单并查询商品
                                logger.info("[recognizeOrder] 找到匹配的训练数据（无商品ID），训练数据ID: {}, 创建订单并查询商品",
                                        matchedTrainingData.getNxOtdId());

                                // 创建订单实体，关联已有的训练数据ID
                                NxDepartmentOrdersEntity order = createOrderForGoodsSearch(
                                        depId, depFatherId, disId, userId,
                                        goodsName, quantity, spec, note, isNotice,
                                        matchedTrainingData.getNxOtdId(), matchedTrainingData);

                                // 如果训练数据不是用 rawName 的 original_goods_name 匹配到的，且 rawName != name
                                // 保存 rawName 信息，用于在 searchGoods 后找不到商品时创建训练数据
                                if (!matchedByRawName && !rawName.equals(goodsName)) {
                                    orderIndexToRawNameMap.put(i, rawName);
                                    orderIndexToMatchedByRawNameMap.put(i, false);
                                }

                                // 添加到临时订单列表，后续统一调用 pasteSearchGoods
                                orderList.add(order);
                                orderIndexList.add(i); // 保存对应的原始索引
                            }
                        } else {
                            // 没有找到训练数据，不立即创建训练数据，等待 searchGoods 后根据结果决定是否创建
                            logger.info("[recognizeOrder] 未找到匹配的训练数据，创建订单并查询商品，等待 searchGoods 结果后再决定是否创建训练数据: goodsName={}, rawName={}",
                                    goodsName, rawName);

                            // 创建订单实体，不关联训练数据ID（设为 null），等待 searchGoods 后根据结果创建
                            NxDepartmentOrdersEntity order = createOrderForGoodsSearch(
                                    depId, depFatherId, disId, userId,
                                    goodsName, quantity, spec, note, isNotice,
                                    null, null); // trainingDataId 设为 null

                            // 保存 rawName 信息，用于在 searchGoods 后找不到商品时创建训练数据
                            orderIndexToRawNameMap.put(i, rawName);
                            orderIndexToMatchedByRawNameMap.put(i, false); // 没有找到训练数据，matchedByRawName 为 false

                            // 添加到临时订单列表，后续统一调用 pasteSearchGoods
                        orderList.add(order);
                            orderIndexList.add(i); // 保存对应的原始索引
                        }
                    }

                    // 如果有需要查询商品的订单，调用 searchAndSaveOrdersFromOcr
                    if (!orderList.isEmpty()) {
                        logger.info("[recognizeOrder] 开始调用 searchAndSaveOrdersFromOcr 查询商品并保存订单，订单数量: {}", orderList.size());
                        List<PasteSearchGoodsResponseDTO> searchResults = nxDepartmentOrdersService.searchAndSaveOrdersFromOcr(orderList);

                        // 将搜索结果按原始索引保存到 responseMap 中，并检查如果缺少数量或规格，状态必须是-2
                        for (int j = 0; j < searchResults.size() && j < orderIndexList.size(); j++) {
                            Integer originalIndex = orderIndexList.get(j);
                            PasteSearchGoodsResponseDTO dto = searchResults.get(j);

                            // 获取原始订单的数量和规格
                            if (originalIndex < itemsList.size()) {
                                Map<String, Object> item = itemsList.get(originalIndex);
                                String quantity = item.get("quantity") != null ? item.get("quantity").toString().trim() : "";
                                String spec = item.get("spec") != null ? item.get("spec").toString().trim() : "";

                                // 如果缺少数量或规格，状态必须是-2（无论是否匹配到商品）
                                if ((quantity == null || quantity.isEmpty()) || (spec == null || spec.isEmpty())) {
                                    dto.setNxDoStatus(-2);
                                    logger.info("[recognizeOrder] 订单缺少数量或规格，强制设置状态为-2: goodsName={}, quantity={}, spec={}",
                                        dto.getNxDoGoodsName(), quantity, spec);
                                }
                            }

                            // 如果订单状态是 -2（未找到商品），创建训练数据
                            if (dto.getNxDoStatus() != null && dto.getNxDoStatus() == -2) {
                                String rawName = orderIndexToRawNameMap.get(originalIndex);
                                Boolean matchedByRawName = orderIndexToMatchedByRawNameMap.get(originalIndex);
                                
                                // 查询订单，检查是否已经关联了训练数据
                                if (dto.getNxDepartmentOrdersId() != null) {
                                    NxDepartmentOrdersEntity order = nxDepartmentOrdersService.queryObjectNew(dto.getNxDepartmentOrdersId());
                                    if (order != null) {
                                        // 如果订单没有关联训练数据ID，或者关联的训练数据的 original_goods_name 不等于 rawName，则创建训练数据
                                        boolean needCreateTrainingData = false;
                                        if (order.getNxDoTrainingDataId() == null) {
                                            // 订单没有关联训练数据ID，需要创建
                                            needCreateTrainingData = true;
                                        } else {
                                            // 订单已关联训练数据ID，检查 original_goods_name 是否等于 rawName
                                            NxOrderOcrTrainingDataEntity existingTrainingData = nxOrderOcrTrainingDataService.queryObject(order.getNxDoTrainingDataId());
                                            if (existingTrainingData != null && rawName != null && !rawName.equals(existingTrainingData.getNxOtdOriginalGoodsName())) {
                                                // 训练数据的 original_goods_name 不等于 rawName，需要创建新的训练数据
                                                needCreateTrainingData = true;
                                            }
                                        }
                                        
                                        if (needCreateTrainingData && rawName != null) {
                                            // 创建训练数据，记录 rawName 和 name 的映射关系
                                            logger.info("[recognizeOrder] searchGoods 后未找到商品，创建训练数据记录 rawName='{}' 和 name='{}' 的映射关系",
                                                    rawName, dto.getNxDoGoodsName());
                                            
                                            NxOrderOcrTrainingDataEntity newTrainingData = new NxOrderOcrTrainingDataEntity();
                                            newTrainingData.setNxOtdDepartmentId(order.getNxDoDepartmentId());
                                            newTrainingData.setNxOtdDepartmentFatherId(order.getNxDoDepartmentFatherId());
                                            newTrainingData.setNxOtdDistributerId(order.getNxDoDistributerId());
                                            newTrainingData.setNxOtdOriginalGoodsName(rawName); // 使用 rawName 作为原始商品名称
                                            newTrainingData.setNxOtdOriginalQuantity(order.getNxDoQuantity());
                                            newTrainingData.setNxOtdOriginalStandard(order.getNxDoStandard());
                                            newTrainingData.setNxOtdOriginalStandardWeight(null);
                                            newTrainingData.setNxOtdOriginalRemark(order.getNxDoRemark());
                                            newTrainingData.setNxOtdDisGoodsId(null); // 未找到商品，设为 null
                                            newTrainingData.setNxOtdOrderId(order.getNxDepartmentOrdersId());
                                            newTrainingData.setNxOtdFinalGoodsName(dto.getNxDoGoodsName()); // 使用 name 作为最终商品名称
                                            newTrainingData.setNxOtdFinalQuantity(null);
                                            newTrainingData.setNxOtdFinalStandard(null);
                                            newTrainingData.setNxOtdFinalStandardWeight(null);
                                            newTrainingData.setNxOtdFinalRemark(null);
                                            // 自动识别标志设为 0（因为还没有找到商品）
                                            newTrainingData.setNxOtdIsNameManuallyAnnotated(0);
                                            newTrainingData.setNxOtdIsQuantityManuallyAnnotated(0);
                                            newTrainingData.setNxOtdIsStandardManuallyAnnotated(0);
                                            newTrainingData.setNxOtdIsStandardWeightManuallyAnnotated(0);
                                            newTrainingData.setNxOtdIsRemarkManuallyAnnotated(0);
                                            newTrainingData.setNxOtdDataSource("OCR_IMAGE");
                                            newTrainingData.setNxOtdCreateDate(formatWhatYearDayTime(0));
                                            newTrainingData.setNxOtdUpdateDate(formatWhatYearDayTime(0));
                                            newTrainingData.setNxOtdCreateUserId(order.getNxDoOrderUserId() != null ? order.getNxDoOrderUserId() : -1);
                                            
                                            nxOrderOcrTrainingDataService.save(newTrainingData);
                                            logger.info("[recognizeOrder] 创建新的训练数据，记录 rawName='{}' 和 name='{}' 的映射关系，训练数据ID: {}",
                                                    rawName, dto.getNxDoGoodsName(), newTrainingData.getNxOtdId());
                                            
                                            // 更新订单关联的训练数据ID为新创建的训练数据
                                            order.setNxDoTrainingDataId(newTrainingData.getNxOtdId());
                                            nxDepartmentOrdersService.update(order);
                                        }
                                    }
                                }
                            }

                            responseMap.put(originalIndex, dto);
                        }
                        // 注意：训练数据的更新已经在 searchAndSaveOrdersFromOcr 方法内部完成，这里不需要重复更新
                    }

                    // 按照原始顺序组装最终的响应列表
                    List<PasteSearchGoodsResponseDTO> finalResponseList = new ArrayList<>();
                    for (int i = 0; i < itemsList.size(); i++) {
                        PasteSearchGoodsResponseDTO dto = responseMap.get(i);
                        if (dto != null) {
                            finalResponseList.add(dto);
                        }
                    }
                    logger.info("[recognizeOrder] 订单处理完成，共 {} 条订单", finalResponseList.size());

                    // 返回处理结果
                    return R.ok().put("data", finalResponseList);

                } catch (Exception e) {
                    logger.error("[recognizeOrder] 处理订单失败: {}", e.getMessage(), e);
                    // 即使处理失败，也返回识别结果
                    return R.ok().put("ocrText", ocrTextContent)
                            .put("items", itemsList != null ? itemsList : new ArrayList<>())
//                            .put("parsedResult", finalResponseList)
                            .put("error", "处理订单失败: " + e.getMessage());
                }
            }
            
            // 返回 OCR 结果和解析后的商品列表
            logger.info("[recognizeOrder] 返回给前端，商品数量: {}", itemsList != null ? itemsList.size() : 0);
            
            return R.ok().put("ocrText", ocrTextContent)
                    .put("items", itemsList != null ? itemsList : new ArrayList<>())
                    .put("parsedResult", parsedResult); // 保留原始解析结果，便于调试
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

    /**
     * Excel 表格识别转订单接口
     * 接收 Excel 文件 -> 读取单元格数据 -> DeepSeek转换为订单
     * 
     * @param file Excel 文件（必填，支持 .xls 和 .xlsx）
     * @param depId 部门ID（可选，如果提供则自动查询商品并保存订单）
     * @param disId 分销商ID（可选，如果提供则自动查询商品并保存订单）
     * @param depFatherId 部门父ID（可选）
     * @param userId 用户ID（可选）
     * @return 识别结果和解析后的订单商品列表，如果提供了depId和disId，则返回查询和保存结果
     */
    @RequestMapping(value = "/recognizeOrderFromExcel", method = RequestMethod.POST)
    @ResponseBody
    public R recognizeOrderFromExcel(@RequestParam("file") MultipartFile file,
                                     @RequestParam(value = "depId", required = false) Integer depId,
                                     @RequestParam(value = "disId", required = false) Integer disId,
                                     @RequestParam(value = "depFatherId", required = false) Integer depFatherId,
                                     @RequestParam(value = "userId", required = false) Integer userId) {
        try {
            // 验证文件
            if (file == null || file.isEmpty()) {
                logger.warn("[recognizeOrderFromExcel] 文件为空");
                return R.error("Excel文件不能为空");
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.toLowerCase().endsWith(".xls") && !fileName.toLowerCase().endsWith(".xlsx"))) {
                logger.warn("[recognizeOrderFromExcel] 文件格式不支持: {}", fileName);
                return R.error("文件格式不支持，请上传 .xls 或 .xlsx 格式的 Excel 文件");
            }

            // 验证文件大小（10MB）
            long fileSize = file.getSize();
            if (fileSize > MAX_IMAGE_BASE64_SIZE) {
                logger.warn("[recognizeOrderFromExcel] 文件过大: {} bytes (限制: {} bytes)", fileSize, MAX_IMAGE_BASE64_SIZE);
                return R.error("文件大小超过限制，最大支持 10MB");
            }

            logger.info("[recognizeOrderFromExcel] 开始解析 Excel 文件: {}, 大小: {} bytes", fileName, fileSize);

            // 读取 Excel 文件并转换为文本
            String excelText = readExcelToText(file);
            if (excelText == null || excelText.trim().isEmpty()) {
                logger.warn("[recognizeOrderFromExcel] Excel 文件内容为空");
                return R.error("Excel 文件中没有可读取的数据");
            }

            logger.info("[recognizeOrderFromExcel] Excel 读取完成，文本内容:\n{}", excelText);

            // 调用 DeepSeek API 解析 Excel 表格（新方法，返回列名映射和结构化数据）
            logger.info("[recognizeOrderFromExcel] 开始调用 DeepSeek 进行解析...");
            String parsedResult = null;
            JSONObject parsedJson = null;
            List<Map<String, Object>> itemsList = new ArrayList<>();
            List<Map<String, Object>> originalItemsList = new ArrayList<>(); // 保存原始数据用于训练数据采集

            try {
                // 调用新的 DeepSeek API 解析 Excel
                parsedResult = callDeepSeekAPIForExcelOrder(excelText);
                logger.info("[recognizeOrderFromExcel] DeepSeek API 调用成功");
                logger.debug("[recognizeOrderFromExcel] DeepSeek 返回的原始结果: {}", parsedResult);

                // 解析 JSON 对象（包含 columnMapping 和 data）
                parsedJson = new JSONObject(parsedResult);
                logger.info("[recognizeOrderFromExcel] DeepSeek 返回结果解析完成");

                // 提取 data 数组
                JSONArray dataArray = parsedJson.getJSONArray("data");
                if (dataArray == null || dataArray.length() == 0) {
                    logger.error("[recognizeOrderFromExcel] DeepSeek 返回的数据为空");
                    return R.error("订单解析失败：DeepSeek 返回数据为空");
                }

                // 处理每条数据
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject itemObj = dataArray.getJSONObject(i);
                    Map<String, Object> item = jsonObjectToMap(itemObj);
                    
                    // 保存原始数据（深拷贝）
                    Map<String, Object> originalItem = new HashMap<>(item);
                    originalItemsList.add(originalItem);

                    // 字段映射：将 DeepSeek 返回的字段映射到内部使用的字段
                    // DeepSeek 返回：name, quantity, spec, standardWeight, note
                    // 内部使用：name, qty, unit, remark
                    Map<String, Object> mappedItem = mapDeepSeekFieldsToInternalFields(item);
                    itemsList.add(mappedItem);
                }

                logger.info("[recognizeOrderFromExcel] DeepSeek 解析成功，解析到 {} 个商品", itemsList.size());

                // 验证解析结果
                if (itemsList == null || itemsList.isEmpty() || !isValidParseResult(itemsList)) {
                    logger.error("[recognizeOrderFromExcel] DeepSeek 解析结果无效（为空或格式不正确）");
                    return R.error("订单解析失败：DeepSeek 返回结果无效");
                }

            } catch (Exception e) {
                logger.error("[recognizeOrderFromExcel] DeepSeek 解析失败: {}", e.getMessage(), e);
                return R.error("订单解析失败：" + e.getMessage());
            }

            // 如果提供了 depId 和 disId，则处理订单
            if (depId != null && disId != null && itemsList != null && !itemsList.isEmpty()) {
                try {
                    // 如果没有提供 depFatherId，从部门信息中获取
                    Integer finalDepFatherId = depFatherId;
                    if (finalDepFatherId == null) {
                        NxDepartmentEntity depInfo = nxDepartmentService.queryDepInfo(depId);
                        if (depInfo != null) {
                            finalDepFatherId = depInfo.getNxDepartmentFatherId();
                            if (finalDepFatherId == null || finalDepFatherId == 0) {
                                finalDepFatherId = depId;
                            }
                        } else {
                            finalDepFatherId = depId;
                        }
                    }

                    Integer finalUserId = userId != null ? userId : -1; // 默认值

                    logger.info("[recognizeOrderFromExcel] 开始处理订单，depId: {}, disId: {}, depFatherId: {}, userId: {}",
                            depId, disId, finalDepFatherId, finalUserId);

                    // 处理每条识别数据
                    List<NxDepartmentOrdersEntity> orderList = new ArrayList<>();
                    // 使用 Map 保存原始索引对应的响应，以保持顺序
                    Map<Integer, PasteSearchGoodsResponseDTO> responseMap = new HashMap<>();
                    List<Integer> orderIndexList = new ArrayList<>(); // 保存 orderList 中每个订单对应的原始索引

                    for (int i = 0; i < originalItemsList.size(); i++) {
                        Map<String, Object> originalItem = originalItemsList.get(i);
                        Map<String, Object> mappedItem = itemsList.get(i);

                        // 提取字段
                        String goodsName = originalItem.get("name") != null ? originalItem.get("name").toString().trim() : "";
                        String quantity = originalItem.get("quantity") != null ? originalItem.get("quantity").toString().trim() : "";
                        String spec = originalItem.get("spec") != null ? originalItem.get("spec").toString().trim() : "";
                        String standardWeight = originalItem.get("standardWeight") != null ? originalItem.get("standardWeight").toString().trim() : "";
                        String note = originalItem.get("note") != null ? originalItem.get("note").toString().trim() : "";

                        if (goodsName.isEmpty()) {
                            logger.warn("[recognizeOrderFromExcel] 跳过商品：名称为空");
                            continue;
                        }

                        // 判断是否缺少数量或规格
                        boolean isDataMissing = (quantity == null || quantity.trim().isEmpty()) || (spec == null || spec.trim().isEmpty());
                        if (isDataMissing) {
                            logger.info("[recognizeOrderFromExcel] 订单缺少数量或规格，创建订单并查询商品: goodsName={}, quantity={}, spec={}",
                                goodsName, quantity, spec);
                        }

                        // 查询训练表中是否有相同内容（匹配：部门ID + 商品名称）
                        Map<String, Object> matchParams = new HashMap<>();
                        matchParams.put("departmentId", depId);
                        matchParams.put("goodsName", goodsName);
                        NxOrderOcrTrainingDataEntity matchedTrainingData = nxOrderOcrTrainingDataService.queryByMatchFields(matchParams);

                        if (matchedTrainingData != null) {
                            // 找到了训练数据，使用已有的训练数据，不创建新的训练数据
                            if (matchedTrainingData.getNxOtdDisGoodsId() != null) {
                                // 训练数据中有商品ID，直接创建订单
                                logger.info("[recognizeOrderFromExcel] 找到匹配的训练数据，商品ID: {}, 直接创建订单",
                                        matchedTrainingData.getNxOtdDisGoodsId());

                                // 查询商品信息
                                NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(
                                        matchedTrainingData.getNxOtdDisGoodsId());
                                if (disGoodsEntity == null) {
                                    logger.error("[recognizeOrderFromExcel] 商品不存在，商品ID: {}",
                                            matchedTrainingData.getNxOtdDisGoodsId());
                                    throw new RuntimeException("商品不存在，商品ID: " + matchedTrainingData.getNxOtdDisGoodsId());
                                }

                                // 优先使用训练数据的 final_goods_name（如果存在），否则使用 Excel 中的商品名称
                                String finalGoodsName = matchedTrainingData.getNxOtdFinalGoodsName();
                                String orderGoodsName = (finalGoodsName != null && !finalGoodsName.trim().isEmpty()) 
                                        ? finalGoodsName 
                                        : goodsName;
                                if (!orderGoodsName.equals(goodsName)) {
                                    logger.info("[recognizeOrderFromExcel] 使用训练数据的 final_goods_name='{}' 替代 Excel 中的 goodsName='{}'", 
                                            orderGoodsName, goodsName);
                                }
                                
                                // 创建基本订单实体（Excel 没有 isNotice，传 null）
                                NxDepartmentOrdersEntity order = createOrderForGoodsSearch(
                                        depId, finalDepFatherId, disId, finalUserId,
                                        orderGoodsName, quantity, spec, note, null,
                                        matchedTrainingData.getNxOtdId(), matchedTrainingData);

                                // 使用 saveOrderWithGoods 保存订单（包含完整的价格处理、部门商品等逻辑）
                                order = nxDepartmentOrdersService.saveOrderWithGoods(order, disGoodsEntity);

                                // 转换为响应DTO
                                PasteSearchGoodsResponseDTO responseDTO = convertOrderToResponseDTO(order, note);
                                // 保存到 Map 中，使用原始索引作为 key，保持顺序
                                responseMap.put(i, responseDTO);
                            } else {
                                // 训练数据中没有商品ID，使用已有的训练数据，创建订单并查询商品
                                logger.info("[recognizeOrderFromExcel] 找到匹配的训练数据（无商品ID），训练数据ID: {}, 创建订单并查询商品",
                                        matchedTrainingData.getNxOtdId());

                                // 创建订单实体，关联已有的训练数据ID（Excel 没有 isNotice，传 null）
                                NxDepartmentOrdersEntity order = createOrderForGoodsSearch(
                                        depId, finalDepFatherId, disId, finalUserId,
                                        goodsName, quantity, spec, note, null,
                                        matchedTrainingData.getNxOtdId(), matchedTrainingData);

                                // 添加到临时订单列表，后续统一调用 pasteSearchGoods
                                orderList.add(order);
                                orderIndexList.add(i); // 保存对应的原始索引
                            }
                        } else {
                            // 没有找到训练数据，创建新的训练数据
                            NxOrderOcrTrainingDataEntity trainingData = createOrQueryTrainingData(
                                    depId, finalDepFatherId, disId,
                                    goodsName, quantity, spec, standardWeight, note, finalUserId);
                            
                            // 如果是新创建的训练数据（数据源为 OCR_IMAGE），更新为 EXCEL
                            if ("OCR_IMAGE".equals(trainingData.getNxOtdDataSource())) {
                            trainingData.setNxOtdDataSource("EXCEL");
                                nxOrderOcrTrainingDataService.update(trainingData);
                            }

                            // 创建订单实体，并关联训练数据ID（Excel 没有 isNotice，传 null）
                            NxDepartmentOrdersEntity order = createOrderForGoodsSearch(
                                    depId, finalDepFatherId, disId, finalUserId,
                                    goodsName, quantity, spec, note, null,
                                    trainingData.getNxOtdId(), null);

                            // 添加到临时订单列表，后续统一调用 pasteSearchGoods
                        orderList.add(order);
                            orderIndexList.add(i); // 保存对应的原始索引
                        }
                    }

                    // 如果有需要查询商品的订单，调用 searchAndSaveOrdersFromOcr
                    if (!orderList.isEmpty()) {
                        logger.info("[recognizeOrderFromExcel] 开始调用 searchAndSaveOrdersFromOcr 查询商品并保存订单，订单数量: {}", orderList.size());
                        List<PasteSearchGoodsResponseDTO> searchResults = nxDepartmentOrdersService.searchAndSaveOrdersFromOcr(orderList);

                        // 将搜索结果按原始索引保存到 responseMap 中，并检查如果缺少数量或规格，状态必须是-2
                        for (int j = 0; j < searchResults.size() && j < orderIndexList.size(); j++) {
                            Integer originalIndex = orderIndexList.get(j);
                            PasteSearchGoodsResponseDTO dto = searchResults.get(j);

                            // 获取原始订单的数量和规格
                            if (originalIndex < originalItemsList.size()) {
                                Map<String, Object> originalItem = originalItemsList.get(originalIndex);
                                String quantity = originalItem.get("quantity") != null ? originalItem.get("quantity").toString().trim() : "";
                                String spec = originalItem.get("spec") != null ? originalItem.get("spec").toString().trim() : "";

                                // 如果缺少数量或规格，状态必须是-2（无论是否匹配到商品）
                                if ((quantity == null || quantity.isEmpty()) || (spec == null || spec.isEmpty())) {
                                    dto.setNxDoStatus(-2);
                                    logger.info("[recognizeOrderFromExcel] 订单缺少数量或规格，强制设置状态为-2: goodsName={}, quantity={}, spec={}",
                                        dto.getNxDoGoodsName(), quantity, spec);
                                }
                            }

                            responseMap.put(originalIndex, dto);
                        }
                        // 注意：训练数据的更新已经在 searchAndSaveOrdersFromOcr 方法内部完成，这里不需要重复更新
                    }

                    // 按照原始顺序组装响应列表
                    List<PasteSearchGoodsResponseDTO> responseList = new ArrayList<>();
                    for (int i = 0; i < originalItemsList.size(); i++) {
                        PasteSearchGoodsResponseDTO dto = responseMap.get(i);
                        if (dto != null) {
                            responseList.add(dto);
                        }
                    }

                    logger.info("[recognizeOrderFromExcel] 订单处理完成，共 {} 条订单", responseList.size());

                    // 返回处理结果
                    return R.ok().put("data", responseList);

                } catch (Exception e) {
                    logger.error("[recognizeOrderFromExcel] 处理订单失败: {}", e.getMessage(), e);
                    // 即使处理失败，也返回识别结果
                    return R.ok().put("excelText", excelText)
                            .put("items", itemsList != null ? itemsList : new ArrayList<>())
                            .put("parsedResult", parsedResult)
                            .put("error", "处理订单失败: " + e.getMessage());
                }
            }

            // 返回 Excel 读取结果和解析后的商品列表
            logger.info("[recognizeOrderFromExcel] 返回给前端，商品数量: {}", itemsList != null ? itemsList.size() : 0);
            return R.ok().put("excelText", excelText)
                    .put("items", itemsList != null ? itemsList : new ArrayList<>())
                    .put("parsedResult", parsedResult); // 保留原始解析结果，便于调试

        } catch (Exception e) {
            logger.error("[recognizeOrderFromExcel] 系统错误: {}", e.getMessage(), e);
            return R.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 读取 Excel 文件并转换为文本
     * 支持多个 Sheet，灵活格式，不识别表头
     * 
     * @param file Excel 文件
     * @return 格式化后的文本内容
     */
    private String readExcelToText(MultipartFile file) throws IOException {
        StringBuilder textBuilder = new StringBuilder();
        DataFormatter formatter = new DataFormatter();
        Workbook workbook = null;

        try {
            // 使用 WorkbookFactory 自动识别 .xls 和 .xlsx 格式
            workbook = WorkbookFactory.create(file.getInputStream());

            logger.info("[readExcelToText] Excel 文件包含 {} 个 Sheet", workbook.getNumberOfSheets());

            // 遍历所有 Sheet
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                if (sheet == null) {
                    continue;
                }

                String sheetName = sheet.getSheetName();
                logger.info("[readExcelToText] 处理 Sheet: {} (索引: {})", sheetName, sheetIndex);

                int lastRowNum = sheet.getLastRowNum();
                if (lastRowNum < 0) {
                    logger.debug("[readExcelToText] Sheet {} 为空，跳过", sheetName);
                    continue;
                }

                // 遍历所有行（不跳过表头，让 DeepSeek 自己识别）
                // 先确定最大列数，确保所有行的列对齐
                int maxColumnCount = 0;
                for (int rowIndex = 0; rowIndex <= lastRowNum; rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row != null) {
                        int cellCount = row.getLastCellNum();
                        if (cellCount > maxColumnCount) {
                            maxColumnCount = cellCount;
                        }
                    }
                }

                for (int rowIndex = 0; rowIndex <= lastRowNum; rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) {
                        // 空行也保留，用空 CSV 行表示
                        if (maxColumnCount > 0) {
                            // 生成空行（全为空的 CSV 行）
                            StringBuilder emptyRow = new StringBuilder();
                            for (int i = 0; i < maxColumnCount; i++) {
                                if (i > 0) {
                                    emptyRow.append(",");
                                }
                            }
                            textBuilder.append(emptyRow.toString()).append("\n");
                        }
                        continue;
                    }

                    // 读取该行的所有单元格（使用 CSV 格式，保留列位置信息）
                    StringBuilder rowText = new StringBuilder();
                    int lastCellNum = Math.max(row.getLastCellNum(), maxColumnCount);

                    for (int cellIndex = 0; cellIndex < lastCellNum; cellIndex++) {
                        Cell cell = row.getCell(cellIndex);
                        
                        // CSV 格式：每个单元格都用逗号分隔
                        if (cellIndex > 0) {
                            rowText.append(",");
                        }

                        // 使用 DataFormatter 格式化单元格内容（自动处理数字、日期等）
                        if (cell != null) {
                        String cellValue = formatter.formatCellValue(cell);
                        if (cellValue != null && !cellValue.trim().isEmpty()) {
                                String trimmedValue = cellValue.trim();
                                // 处理 CSV 中的特殊字符（如果包含逗号、引号、换行符，需要用引号包裹）
                                if (trimmedValue.contains(",") || trimmedValue.contains("\"") || trimmedValue.contains("\n") || trimmedValue.contains("\r")) {
                                    trimmedValue = "\"" + trimmedValue.replace("\"", "\"\"") + "\"";
                                }
                                rowText.append(trimmedValue);
                            }
                            // 空单元格不添加内容，但保留逗号分隔符
                        }
                        // 如果单元格为 null，也不添加内容，保留逗号分隔符
                    }

                    // 添加该行到文本中
                        textBuilder.append(rowText.toString()).append("\n");
                }
            }

            String result = textBuilder.toString().trim();
            logger.info("[readExcelToText] Excel 读取完成，共 {} 行文本", result.split("\n").length);
            return result;

        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    logger.warn("[readExcelToText] 关闭工作簿异常: {}", e.getMessage());
                }
            }
        }
    }

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
     * 调用 DeepSeek API 解析订单文本
     * 
     * @param ocrText OCR 识别的订单文本（按行组织）
     * @return DeepSeek返回的JSON字符串（应该是纯JSON数组）
     * @throws IOException
     */
    private String callDeepSeekAPIForOrder(String ocrText) throws IOException {
        // 创建带超时设置的 HTTP 客户端
        org.apache.http.client.config.RequestConfig requestConfig = org.apache.http.client.config.RequestConfig.custom()
                .setConnectTimeout(30000)      // 连接超时 30 秒
                .setSocketTimeout(120000)       // Socket 超时 120 秒
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
        String prompt = String.join("\n",
                "你是一个【生鲜订单 OCR 解析与纠错引擎】。",
                "输入为 OCR 识别得到的订单文本，已按行组织。",
                "你的输出将直接用于真实下单，请严格遵守以下规则。",
                "",
                "==============================",
                "【P0 级｜绝对禁止规则（最高优先级）】",
                "==============================",
                "",
                "你【绝对不允许】因为不确定、推理冲突或格式混乱而【吞掉】任何一行订单条目。",
                "",
                "关于“不丢信息”的最高定义：",
                "1. 【数量/单位】必须严格保留，一个都不能少。",
                "2. 【商品名称】必须“取其意而忘其形”。",
                "   - 若 OCR 识别错误（如“气椒”），**必须**用正确商品名（“青椒”）覆盖它。",
                "   - **保留错误的 OCR 原文被视为一种严重的“未完成工作”**。",
                "",
                "针对同一行内的【同一商品】，若出现多组数量（通过“+”连接或并列出现）：",
                "",
                "1. 【强制计算】：",
                "   - 必须将数值相加，quantity 字段只输出最终结果数字。note 字段输出“10斤+10斤”。",
                "   - 严禁在 quantity 字段保留算式（如 \"10+10\" 是错的，\"20\" 是对的）。",
                "",
                "2. 【触发条件】：",
                "   - 显式加法：“大葱 10+20” -> 30",
                "   - 隐式累加：“大葱 10斤 20斤” -> 30斤（需单位一致）",
                "",
                "3. 【例外情况】：",
                "   - 若单位不同（如 10斤+5个），**禁止合并**，必须拆分为两条。",
                "",
                "4. 【溯源要求】：",
                "   - 输出：note=“10斤+10斤”。",
                "",
                "示例：",
                "输入：“大葱 10斤+10斤”",
                "输出：name=\"大葱\", quantity=\"20\", spec=\"斤\", note=\"10斤+10斤\"",
                "",
                "==============================",
                "【P1 级｜适用范围与目标】",
                "==============================",
                "",
                "适用范围：",
                "- 仅对 name（商品名称）进行纠错",
                "- quantity / spec 必须严格按原始文本解析",
                "",
                "目标：",
                "- name 应尽量落在以下领域：生鲜食材 / 餐饮原料 / 调料 / 粮油 / 冻品",
                "",
                "==============================",
                "【P1 级｜纠错触发条件】",
                "==============================",
                "",
                "A. 包含明显非生鲜词（器官、辱骂、违禁品等）",
                "B. 语境不合理（生鲜语境中不可能成立的词）",
                "C. 常见 OCR 错字（形近、连笔误识）",
                "",
                "==============================",
                "【P1 级｜生鲜纠错原则】",
                "==============================",
                "",
                "1) 最小改动：优先替换局部，而非整词重写",
                "2) OCR 相似度（强制）：【字形优先，音近仅限备选】\n" +
                        "   - 必须优先按【手写汉字形体】纠错：偏旁部首、笔画结构、连笔形态、整体轮廓相似度。\n" +
                        "   - 【禁止】仅凭读音相近/拼音相近进行纠错（除非字形也高度相似）。\n" +
                        "   - 读音相近只能作为“候选解释”，写在 isNotice 的候选中，不能作为最终纠错依据。",
                "3) 高频优先：优先匹配常见菜品（葱姜蒜、青椒、白菜、土豆等）",
                "4) 上下文一致：同类商品优先",
                "",
                "==============================",
                "【P0 级｜纠错结果强制写回规则（必须执行）】" +
                        "- 本系统输入来自【手写图片 OCR】，纠错必须以【汉字字形相似】为第一依据。\n" +
                        "- 【严禁】使用“语音相似/读音联想/语义联想”把词硬拉到另一个商品（例如：酒精拉 -> 九层塔 是错误示范）。\n" +
                        "- 若原词包含“酒精/消毒/84/洗手液/口罩”等明显非食材清洁用品词：\n" +
                        "  - 优先在同类清洁用品中做字形纠错（如：酒精块/酒精棉/酒精片），不得纠错成香料/蔬菜。",
                "==============================",
                "",
                "一旦识别出错误（如“气椒”、“毛白带”）：",
                "",
                "1. 【立刻替换】：在生成 name 字段的值时，**必须直接输出修正后的词**。",
                "1.1 【原名保留】：同时必须输出 rawName，用于保存 OCR 原始商品名（原样保留，不纠错）。",
                "2. 【禁止犹豫】：不要输出原始错误词，错误词只能出现在 isNotice 中。",
                "3. 【特定案例】：",
                "   - 看到“气椒”，name 必须写“青椒”",
                "   - 看到“毛白带”，name 必须写“小白菜”",
                "",
                "isNotice 格式：",
                "- 原名=XXX；纠错=YYY；可信度=高/中/低；[候选=...]",
                "",
                "==============================",
                "【P0 级｜特殊条目保留规则】",
                "==============================",
                "",
                "1. 疑似编码/规格（如 32板、15件）：",
                "   - 若后跟明确单位，name 原样保留，不强行纠错。",
                "2. 多商品同行：",
                "   - 必须拆分为独立条目，禁止合并。",
                "",
                "==============================",
                "【P2 级｜字段说明与使用规范】",
                "==============================",
                "",
                "orderItems 包含字段：",
                "- rawName：【OCR 原始商品名】（必须保留原样）",
                "- name：【最终下单名称】",
                "  - **严禁**填入 OCR 错误原文（除非完全无法纠错）。",
                "  - **必须**填入你纠错后的结果。",
                "- quantity：数量",
                "- spec：单位",
                "- note：原始备注（括号内容、去皮、空心等）",
                "- isNotice：系统提示（记录原名、纠错逻辑、候选词）",
                "",
                "==============================",
                "【P0 级｜JSON 返回格式要求】",
                "==============================",
                "",
                "必须返回标准 JSON 对象，包含 orderItems 数组。",
                "",
                "==============================",
                "【标准纠错样本（Strict Format Reference）】",
                "==============================",
                "",
                "输入：",
                "气椒 20 斤",
                "毛白带 5 把",
                "",
                "正确输出（请严格模仿此逻辑）：",
                "{",
                "  \"orderItems\": [",
                "    {",
                "      \"rawName\": \"气椒\",",
                "      \"name\": \"青椒\",",
                "      \"quantity\": \"20\",",
                "      \"spec\": \"斤\",",
                "      \"note\": \"\",",
                "      \"isNotice\": \"原名=气椒；纠错=青椒；可信度=高\"",
                "    },",
                "    {",
                "      \"rawName\": \"毛白带\",",
                "      \"name\": \"小白菜\",",
                "      \"quantity\": \"5\",",
                "      \"spec\": \"把\",",
                "      \"note\": \"\",",
                "      \"isNotice\": \"原名=毛白带；纠错=小白菜；可信度=中\"",
                "    },",
                "    {",
                "      \"rawName\": \"酒精拉\",",
                "      \"name\": \"酒精块\",",
                "      \"quantity\": \"1\",",
                "      \"spec\": \"代\",",
                "      \"note\": \"\",",
                "      \"isNotice\": \"原名=酒精拉；纠错=酒精块；可信度=中\"",
                "    },",
                "  ]",
                "}",
                "",
                "==============================",
                "【最终铁律】",
                "==============================",
                "",
                "rawName 永远等于 OCR 解析得到的原始商品名（原样保留，不纠错），",
                "name 字段代表【最终下单商品】，",
                "isNotice 字段代表【OCR 历史记录】。",
                "切勿搞反。"
        );

        systemMessage.put("content", prompt);


//        systemMessage.put("content", "你是一个【生鲜订单 OCR 解析与纠错引擎】。\\n\" +\n" +
//                "        \"输入为 OCR 识别得到的订单文本，已按行组织。\\n\" +\n" +
//                "        \"你的输出将直接用于真实下单，请严格遵守以下规则。\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"==============================\\n\" +\n" +
//                "        \"【P0 级｜绝对禁止规则（最高优先级）】\\n\" +\n" +
//                "        \"==============================\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"你【绝对不允许】因为不确定、推理冲突或格式混乱而【吞掉】任何一行订单条目。\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"关于“不丢信息”的最高定义：\\n\" +\n" +
//                "        \"1. 【数量/单位】必须严格保留，一个都不能少。\\n\" +\n" +
//                "        \"2. 【商品名称】必须“取其意而忘其形”。\\n\" +\n" +
//                "        \"   - 若 OCR 识别错误（如“气椒”），**必须**用正确商品名（“青椒”）覆盖它。\\n\" +\n" +
//                "        \"   - **保留错误的 OCR 原文被视为一种严重的“未完成工作”**。\\n\" " +
//                "针对同一行内的【同一商品】，若出现多组数量（通过“+”连接或并列出现）：\n" +
//                "\n" +
//                "1. 【强制计算】：\n" +
//                "   - 必须将数值相加，quantity 字段只输出最终结果数字。note 字段输出“10斤+10斤\n" +
//                "   - 严禁在 quantity 字段保留算式（如 \"10+10\" 是错的，\"20\" 是对的）。\n" +
//                "\n" +
//                "2. 【触发条件】：\n" +
//                "   - 显式加法：“大葱 10+20” -> 30\n" +
//                "   - 隐式累加：“大葱 10斤 20斤” -> 30斤（需单位一致）\n" +
//                "\n" +
//                "3. 【例外情况】：\n" +
//                "   - 若单位不同（如 10斤+5个），**禁止合并**，必须拆分为两条。\n" +
//                "\n" +
//                "4. 【溯源要求】：\n" +
//                "   - 输出：note=“10斤+10斤”。\n" +
//                "\n" +
//                "示例：\n" +
//                "输入：“大葱 10斤+10斤”\n" +
//                "输出：name=\"大葱\", quantity=\"20\", spec=\"斤\", note=\"10斤+10斤\"+\n" +
//                "        \"\\n\" +\n" +
//                "        \"==============================\\n\" +\n" +
//                "        \"【P1 级｜适用范围与目标】\\n\" +\n" +
//                "        \"==============================\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"适用范围：\\n\" +\n" +
//                "        \"- 仅对 name（商品名称）进行纠错\\n\" +\n" +
//                "        \"- quantity / spec 必须严格按原始文本解析\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"目标：\\n\" +\n" +
//                "        \"- name 应尽量落在以下领域：生鲜食材 / 餐饮原料 / 调料 / 粮油 / 冻品\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"==============================\\n\" +\n" +
//                "        \"【P1 级｜纠错触发条件】\\n\" +\n" +
//                "        \"==============================\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"A. 包含明显非生鲜词（器官、辱骂、违禁品等）\\n\" +\n" +
//                "        \"B. 语境不合理（生鲜语境中不可能成立的词）\\n\" +\n" +
//                "        \"C. 常见 OCR 错字（形近、音近、连笔误识）\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"==============================\\n\" +\n" +
//                "        \"【P1 级｜生鲜纠错原则】\\n\" +\n" +
//                "        \"==============================\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"1) 最小改动：优先替换局部，而非整词重写\\n\" +\n" +
//                "        \"2) OCR 相似度：优先考虑形近/音近字\\n\" +\n" +
//                "        \"3) 高频优先：优先匹配常见菜品（葱姜蒜、青椒、白菜、土豆等）\\n\" +\n" +
//                "        \"4) 上下文一致：同类商品优先\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"==============================\\n\" +\n" +
//                "        \"【P0 级｜纠错结果强制写回规则（必须执行）】\\n\" +\n" +
//                "        \"==============================\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"一旦识别出错误（如“气椒”、“毛白带”）：\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"1. 【立刻替换】：在生成 name 字段的值时，**必须直接输出修正后的词**。\\n\" +\n" +
//                "        \"2. 【禁止犹豫】：不要输出原始错误词，错误词只能出现在 isNotice 中。\\n\" +\n" +
//                "        \"3. 【特定案例】：\\n\" +\n" +
//                "        \"   - 看到“气椒”，name 必须写“青椒”\\n\" +\n" +
//                "        \"   - 看到“毛白带”，name 必须写“小白菜”\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"isNotice 格式：\\n\" +\n" +
//                "        \"- 原名=XXX；纠错=YYY；可信度=高/中/低；[候选=...]\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"==============================\\n\" +\n" +
//                "        \"【P0 级｜特殊条目保留规则】\\n\" +\n" +
//                "        \"==============================\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"1. 疑似编码/规格（如 32板、15件）：\\n\" +\n" +
//                "        \"   - 若后跟明确单位，name 原样保留，不强行纠错。\\n\" +\n" +
//                "        \"2. 多商品同行：\\n\" +\n" +
//                "        \"   - 必须拆分为独立条目，禁止合并。\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"==============================\\n\" +\n" +
//                "        \"【P2 级｜字段说明与使用规范】\\n\" +\n" +
//                "        \"==============================\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"orderItems 包含字段：\\n\" +\n" +
//                "        \"- rawName：【OCR 原始商品名】（必须保留原样）\\n\" +\n" +
//                "        \"- name：【最终下单名称】\\n\" +\n" +
//                "        \"  - **严禁**填入 OCR 错误原文（除非完全无法纠错）。\\n\" +\n" +
//                "        \"  - **必须**填入你纠错后的结果。\\n\" +\n" +
//                "        \"- quantity：数量\\n\" +\n" +
//                "        \"- spec：单位\\n\" +\n" +
//                "        \"- note：原始备注（括号内容、去皮、空心等）\\n\" +\n" +
//                "        \"- isNotice：系统提示（记录原名、纠错逻辑、候选词）\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"==============================\\n\" +\n" +
//                "        \"【P0 级｜JSON 返回格式要求】\\n\" +\n" +
//                "        \"==============================\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"必须返回标准 JSON 对象，包含 orderItems 数组。\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"==============================\\n\" +\n" +
//                "        \"【标准纠错样本（Strict Format Reference）】\\n\" +\n" +
//                "        \"==============================\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"输入：\\n\" +\n" +
//                "        \"气椒 20 斤\\n\" +\n" +
//                "        \"毛白带 5 把\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"正确输出（请严格模仿此逻辑）：\\n\" +\n" +
//                "        \"{\\n\" +\n" +
//                "        \"  \\\"orderItems\\\": [\\n\" +\n" +
//                "        \"    {\\n\" +\n" +
//                "        \"      \\\"rawName\\\": \\\"气椒\\\", 【OCR 原始商品名】（必须保留原样）”\\n\" +\n" +
//                "        \"      \\\"name\\\": \\\"青椒\\\",  <-- 注意：直接修正，不写“气椒”\\n\" +\n" +
//                "        \"      \\\"quantity\\\": \\\"20\\\",\\n\" +\n" +
//                "        \"      \\\"spec\\\": \\\"斤\\\",\\n\" +\n" +
//                "        \"      \\\"note\\\": \\\"\\\",\\n\" +\n" +
//                "        \"      \\\"isNotice\\\": \\\"原名=气椒；纠错=青椒；可信度=高\\\"\\n\" +\n" +
//                "        \"    },\\n\" +\n" +
//                "        \"    {\\n\" +\n" +
//                "        \"      \\\"rawName\\\": \\\"毛白带\\\", 【OCR 原始商品名】（必须保留原样）”\\n\" +\n" +
//                "        \"      \\\"name\\\": \\\"小白菜\\\", <-- 注意：直接修正，不写“毛白带”\\n\" +\n" +
//                "        \"      \\\"quantity\\\": \\\"5\\\",\\n\" +\n" +
//                "        \"      \\\"spec\\\": \\\"把\\\",\\n\" +\n" +
//                "        \"      \\\"note\\\": \\\"\\\",\\n\" +\n" +
//                "        \"      \\\"isNotice\\\": \\\"原名=毛白带；纠错=小白菜；可信度=中\\\"\\n\" +\n" +
//                "        \"    }\\n\" +\n" +
//                "        \"  ]\\n\" +\n" +
//                "        \"}\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"==============================\\n\" +\n" +
//                "        \"【最终铁律】\\n\" +\n" +
//                "        \"==============================\\n\" +\n" +
//                "        \"\\n\" +\n" +
//                "        \"rawName 永远等于 OCR 解析得到的原始商品名（原样保留，不纠错），\\n\" +\n" +
//                "        \"name 字段代表【最终下单商品】，\\n\" +\n" +
//                "        \"isNotice 字段代表【OCR 历史记录】。\\n\" +\n" +
//                "        \"切勿搞反。\\n\");");

//        systemMessage.put("content", "" +
//                "你是一个【生鲜订单 OCR 解析与纠错引擎】。\n" +
//                "输入为 OCR 识别得到的订单文本，已按行组织。\n" +
//                "你的输出将直接用于真实下单，请严格遵守以下规则。\n" +
//                "\n" +
//                "==============================\n" +
//                "【P0 级｜绝对禁止规则（最高优先级，不可违反）】\n" +
//                "==============================\n" +
//                "\n" +
//                "你【绝对不允许】因为不确定、推理冲突、上下文不清、格式混乱、领域纠错等原因，\n" +
//                "而【删除、忽略、合并或吞掉】任何一行中出现的订单信息。\n" +
//                "\n" +
//                "原始 OCR 文本中：\n" +
//                "- 只要出现了“商品样式 + 数量/单位”的组合，\n" +
//                "- 即使你无法判断其真实含义，\n" +
//                "- 也【必须】输出为一个独立的订单条目。\n" +
//                "\n" +
//                "宁可不确定、宁可标注异常，\n" +
//                "也【绝对不允许】不输出。\n" +
//                "\n" +
//                "“不丢信息”指的是：\n" +
//                "- 每一个商品条目必须输出\n" +
//                "- 每一个 quantity 和 spec 必须保留\n" +
//                "- 不是指 name 必须原样不改\n" +
//                "\n" +
//                "==============================\n" +
//                "【P1 级｜适用范围与目标】\n" +
//                "==============================\n" +
//                "\n" +
//                "适用范围：\n" +
//                "- 仅对每个订单条目的 name（商品名称）进行判断与纠错\n" +
//                "- quantity / spec / standardWeight 必须严格按原始文本解析\n" +
//                "- 【禁止】为了纠错 name 而改动数量或单位\n" +
//                "\n" +
//                "目标：\n" +
//                "- 每个订单条目必须完整输出：name + quantity + spec\n" +
//                "- name 应尽量落在以下领域之一：\n" +
//                "  「生鲜食材 / 餐饮原料 / 调料 / 粮油 / 冻品 / 日配」\n" +
//                "\n" +
//                "==============================\n" +
//                "【P1 级｜纠错触发条件（仅针对 name）】\n" +
//                "==============================\n" +
//                "\n" +
//                "当且仅当出现以下任一情况时，才允许对 name 进行纠错：\n" +
//                "\n" +
//                "A. name 包含明显非生鲜词\n" +
//                "   （如人体器官、性相关、疾病、辱骂、违法物品等）\n" +
//                "\n" +
//                "B. name 在生鲜语境中明显不可能成立\n" +
//                "   （词性不像商品、组合怪异、常见离题词）\n" +
//                "\n" +
//                "C. name 含有 OCR 常见错字形态\n" +
//                "   （字形相近 / 读音相近 / 偏旁部首相近 / 手写连笔易混）\n" +
//                "\n" +
//                "==============================\n" +
//                "【P1 级｜生鲜领域纠错原则（必须同时满足）】\n" +
//                "==============================\n" +
//                "\n" +
//                "1) 最小改动原则  \n" +
//                "   - 尽量少改字\n" +
//                "   - 优先替换局部片段，而不是整词重写\n" +
//                "\n" +
//                "2) OCR 相似度原则  \n" +
//                "   - 优先选择手写 OCR 场景下最可能写错/看错的替代\n" +
//                "   - 允许基于字形、读音、结构相似进行推断\n" +
//                "\n" +
//                "3) 生鲜高频优先  \n" +
//                "   - 优先使用常见高频商品\n" +
//                "     （白菜、菜心、油麦菜、生菜、菠菜、芹菜、香菜、\n" +
//                "      葱、姜、蒜、土豆、胡萝卜、洋葱等）\n" +
//                "   - 禁止臆造冷门商品\n" +
//                "\n" +
//                "4) 上下文一致性  \n" +
//                "   - 若同一订单中已出现某一商品大类（如蔬菜、调料），\n" +
//                "     纠错时优先选择同类商品\n" +
//                "\n" +
//                "5) 禁止编造  \n" +
//                "   - 不得输出生鲜语境中不常见、无法解释来源的名称\n" +
//                "\n" +
//                "==============================\n" +
//                "【P0 级｜纠错结果强制写回规则（必须执行）】\n" +
//                "==============================\n" +
//                "一旦触发 name 纠错（满足 A/B/C 任一条件）：\n" +
//                "\n" +
//                "1. 【思维链强制前置】：在生成 JSON 的 name 字段之前，必须先在后台确定 correctedName。\n" +
//                "2. 【替换原则】：\n" +
//                "   - 最终输出的 name 值 === correctedName\n" +
//                "   - 严禁将错误的 originalName 写入 name 字段。\n" +
//                "   - 只有在完全无法纠错时，才保留原文。\n" +
//                "\n" +
//                "3. 【高频错例警示】：\n" +
//                "   - 若 OCR 识别为“毛白带”，name 必须输出“小白菜”\n" +
//                "   - 若 OCR 识别为“西兰花”，name 必须输出“西兰花”（无错则保留）\n" +
//                "   - 若 OCR 识别为“生姜”，name 必须输出“生姜”\n" +
//                "\n" +
//                "isNotice 必须包含：\n" +
//                "- 原名=XXX\n" +
//                "- 纠错=YYY\n" +
//                "...（保持你原有的 isNotice 规则）" +
//                "\n" +
//                "一旦触发 name 纠错（满足 A/B/C 任一条件）：\n" +
//                "\n" +
//                "- 你必须先确定一个最终纠错结果 correctedName\n" +
//                "- 【强制写回】：最终输出的 name 字段必须等于 correctedName\n" +
//                "- 原始识别词 originalName 【禁止】继续出现在 name 中\n" +
//                "- originalName 只能出现在 isNotice 中用于说明\n" +
//                "\n" +
//                "isNotice 必须包含：\n" +
//                "- 原名=XXX\n" +
//                "- 纠错=YYY\n" +
//                "- 可信度=高 / 中 / 低\n" +
//                "\n" +
//                "若可信度为 中 或 低：\n" +
//                "- isNotice 中必须追加 1~3 个候选：\n" +
//                "  “候选=A/B/C（需人工确认）”\n" +
//                "\n" +
//                "若无法给出任何合理纠错：\n" +
//                "- name 必须输出为：“【待确认生鲜商品】”\n" +
//                "- 但该条目【仍然必须输出】\n" +
//                "- isNotice 说明：原名=XXX；无法可靠纠错；需人工确认\n" +
//                "\n" +
//                "==============================\n" +
//                "【P0 级｜编码 / 规格类条目保留规则】\n" +
//                "==============================\n" +
//                "\n" +
//                "若 name 类似商品编码或规格标记（如：32板、15件、8箱），\n" +
//                "且后面跟随明确数量单位（如：4斤、2包）：\n" +
//                "\n" +
//                "- 必须视为有效订单条目\n" +
//                "- name 必须原样保留\n" +
//                "- 禁止强行纠错成具体生鲜商品\n" +
//                "- isNotice 标记：\n" +
//                "  “疑似商品编码或规格，含义不明，按原文保留”\n" +
//                "\n" +
//                "==============================\n" +
//                "【P0 级｜多商品拆分规则】\n" +
//                "==============================\n" +
//                "\n" +
//                "同一行中若出现多个“商品名称 + 数量单位”组合：\n" +
//                "- 每一个组合都必须拆分为【独立订单条目】\n" +
//                "- 禁止合并\n" +
//                "- 禁止遗漏其中任何一个\n" +
//                "\n" +
//                "==============================\n" +
//                "【P2 级｜字段说明与使用规范】\n" +
//                "==============================\n" +
//                "\n" +
//                "每个订单条目允许包含以下字段：\n" +
//                "- name：【最终修正值】（若触发纠错，此处必须直接填写入纠错后的正确名称，严禁保留错误的 OCR 原始文本；若无须纠错，则保留原文）\n" +
//                "- quantity：数量\n" +
//                "- spec：数量单位\n" +
//                "- note：订单备注（来自原始文本）\n" +
//                "- isNotice：系统提示信息（OCR / 纠错 / 推理）\n" +
//                "\n" +
//                "------------------------------\n" +
//                "note 字段使用规则（业务字段）：\n" +
//                "------------------------------\n" +
//                "- 仅用于承载【原始订单文本中的真实备注信息】\n" +
//                "- 包括：括号内容、附加说明、商品特征\n" +
//                "  （如：空心、偏小、去皮、不带根）\n" +
//                "- 若原始文本中出现括号内容（如“(空心)”）：\n" +
//                "  - 必须提取到 note\n" +
//                "- note 中【禁止】出现任何系统解释或推理说明\n" +
//                "\n" +
//                "------------------------------\n" +
//                "isNotice 字段使用规则（系统字段）：\n" +
//                "------------------------------\n" +
//                "- 仅用于系统层面的提示信息，包括：\n" +
//                "  - OCR 误识别说明\n" +
//                "  - 生鲜领域纠错说明\n" +
//                "  - 数量/单位合理性推断\n" +
//                "  - 不确定性提示\n" +
//                "- isNotice 不属于订单本身内容\n" +
//                "- 若无系统提示，可置为空字符串\n" +
//                "\n" +
//                "==============================\n" +
//                "【P0 级｜返回格式要求（必须严格遵守）】\n" +
//                "==============================\n" +
//                "\n" +
//                "- 必须返回标准 JSON 对象\n" +
//                "- 必须包含 orderItems 字段\n" +
//                "- 禁止返回纯数组格式（如 [...]）\n" +
//                "- 禁止使用其他字段名（items / data / result 等）\n" +
//                "\n" +
//                "返回格式示例：\n" +
//                "\n" +
//                "{\n" +
//                "  \"orderItems\": [\n" +
//                "    {\n" +
//                "      \"name\": \"商品名称\",\n" +
//                "      \"quantity\": \"数量\",\n" +
//                "      \"spec\": \"规格单位\",\n" +
//                "      \"note\": \"备注\",\n" +
//                "      \"isNotice\": \"系统提示\"\n" +
//                "    }\n" +
//                "  ]\n" +
//                "}\n" +
//                "\n" +
//                "==============================\n" +
//                "【最终铁律】\n" +
//                "==============================\n" +
//                "\n" +
//                "宁可输出一个“含义不明但数量完整”的订单项，\n" +
//                "也绝对不允许丢失任何商品、数量或单位。\n");

//        systemMessage.put("content","绝对禁止规则（最高优先级，不可违反）：\n" +
//                "\n" +
//                "你【绝对不允许】因为不确定、推理冲突、上下文不清、格式混乱、领域纠错等原因，\n" +
//                "而【删除、忽略、合并或吞掉】任何一行中出现的订单信息。\n" +
//                "\n" +
//                "原始 OCR 文本中：\n" +
//                "- 只要出现了“商品样式 + 数量/单位”的组合，\n" +
//                "- 即使你无法判断其真实含义，\n" +
//                "- 也【必须】输出为一个独立的订单条目。\n" +
//                "\n" +
//                "宁可不确定、宁可标注异常，\n" +
//                "也【绝对不允许】不输出。\n\n" +
//                "\n" +
//                "适用范围：\n" +
//                "- 仅对每个订单条目的 name（商品名称）进行判断与纠错。\n" +
//                "- quantity / spec / standardWeight 仍按原规则解析，禁止为了纠错而改动数量单位。\n" +
//                "\n" +
//                "目标：\n" +
//                "- name 必须落在「生鲜食材/餐饮原料/调料/粮油/冻品/日配」领域内。\n" +
//                "- 若原 name 明显不属于上述领域或包含尴尬/敏感/离题词，禁止原样输出，必须纠错到生鲜领域。\n" +
//                "\n" +
//                "触发条件（任一命中即触发纠错）：\n" +
//                "A. name 包含明显非生鲜词（如人体器官/性相关/疾病/辱骂/违法物品等）\n" +
//                "B. name 在生鲜语境中明显不可能成立（例如：词性不像商品、组合怪异、常见离题词）\n" +
//                "C. name 含有 OCR 常见错字形态导致离题（字形/读音近似造成的错误词）\n" +
//                "\n" +
//                "纠错原则（必须同时满足）：\n" +
//                "1) 最小改动原则：尽量少改字、优先替换局部片段而不是整词重写。\n" +
//                "2) OCR 相似度原则：优先选择“手写 OCR 场景下最可能写错/看错”的替代（字形相近、读音相近、偏旁部首相近、连笔易混）。\n" +
//                "3) 生鲜高频优先：优先选常见高频商品（白菜/菜心/油麦菜/生菜/菠菜/芹菜/香菜/葱姜蒜/土豆/胡萝卜/洋葱等），避免冷门臆造。\n" +
//                "4) 上下文一致：若同单中已出现某类商品（蔬菜/调料等），纠错时优先选择同类更合理的名称。\n" +
//                "5) 禁止编造冷门：不得输出生鲜语境中不常见、无法解释来源的名称。\n" +
//                "\n" +
//                "输出规范（纠错后的结构要求）：\n" +
//                "字段说明与使用规范（重要）：\n" +
//                "\n" +
//                "每个订单条目允许包含以下字段：\n" +
//                "- name：商品名称\n" +
//                "- quantity：数量\n" +
//                "- spec：数量单位\n" +
//                "- note：订单备注（来自原始文本内容）\n" +
//                "- isNotice：系统提示或说明（OCR纠错、推理说明、合理性判断等）\n" +
//                "\n" +
//                "note 字段使用规则：\n" +
//                "- 仅用于承载【原始订单文本中的真实备注信息】。\n" +
//                "- 包括但不限于：括号内容、附加说明、规格补充（如“空心”“偏小”“去皮”“不带根”）。\n" +
//                "- 如果原始文本中出现括号内容（如“(空心)”），必须提取到 note 中，原样保留或轻微清洗。\n" +
//                "- note 中【禁止】出现任何系统解释、OCR识别说明或推理描述。\n" +
//                "\n" +
//                "isNotice 字段使用规则：\n" +
//                "- 用于承载【系统层面的提示信息】，包括：\n" +
//                "  - OCR 误识别说明\n" +
//                "  - 生鲜领域纠错说明\n" +
//                "  - 数量/单位合理性推断\n" +
//                "  - 不确定性提示（需人工确认）\n" +
//                "- isNotice 面向系统和前端提示用户，不属于订单本身内容。\n" +
//                "- 若无系统提示，可省略 isNotice 或置为空字符串。\n" +
//                "\n" +
//                "【重要】返回格式要求（必须严格遵守）：\\n\" +\n" +
//                "                \"- 必须返回标准的 JSON 对象格式，包含 orderItems 字段\\n\" +\n" +
//                "                \"- 返回格式示例：\\n\" +\n" +
//                "                \"{\\n\" +\n" +
//                "                \"  \\\"orderItems\\\": [\\n\" +\n" +
//                "                \"    {\\n\" +\n" +
//                "                \"      \\\"name\\\": \\\"商品名称\\\",\\n\" +\n" +
//                "                \"      \\\"quantity\\\": \\\"数量\\\",\\n\" +\n" +
//                "                \"      \\\"spec\\\": \\\"规格单位\\\",\\n\" +\n" +
//                "                \"      \\\"note\\\": \\\"备注\\\",\\n\" +\n" +
//                "                \"      \\\"isNotice\\\": \\\"系统提示\\\"\\n\" +\n" +
//                "                \"    }\\n\" +\n" +
//                "                \"  ]\\n\" +\n" +
//                "                \"}\\n\" +\n" +
//                "                \"- 禁止返回纯数组格式（如 [...]）\\n\" +\n" +
//                "                \"- 禁止使用其他字段名（如 items、data、result 等）\\n\" +\n" +
//                "                \"- 必须使用 orderItems 作为数组字段名\\n\" +\n" +
//                "                \"- 返回的 JSON 必须是有效的、可解析的 JSON 格式\\n" +
//                ""  );







//        systemMessage.put("content", "你是一个订单解析助手。输入为 OCR 识别得到的订单文本，已按行组织。\n\n" +
//                "请根据以下要求解析订单：\n\n" +
//                "0. 领域安全与脱敏（最高优先级，覆盖其他规则）：\n" +
//                        "\n" +
//                        "订单仅允许输出与「生鲜食材/餐饮原料/调料/粮油/冻品/日配」相关的内容。\n" +
//                        "\n" +
//                        "如果某个商品名或词片段包含明显非生鲜领域且可能引发尴尬/敏感的词（如：生殖器官、性行为、性病、妇科疾病、辱骂词等），你必须执行脱敏：\n" +
//                        "\n" +
//                        "在 name 中将该敏感片段替换为 【已屏蔽】（只替换敏感片段，其他字原样保留）。\n" +
//                        "\n" +
//                        "在 note 中追加：\"疑似OCR误识别，包含敏感/非生鲜词，已脱敏\"。\n" +
//                        "\n" +
//                        "不允许因为脱敏而删除该条目或整行信息；仍需按行输出成订单条目。\n" +
//                        "\n" +
//                        "若整行只有敏感词或完全不成商品，仍输出一条：name=\"【已屏蔽】\"，并写明 note。\n" +
//                        "\n" +
//                        "该规则优先级高于“原样输出”。\n\n" +
//                "1. 不要丢失任何订单信息：所有识别到的文本都必须转化为订单条目，不能遗漏。\n\n" +
//                "2. 原样输出（重要）：除“数量合并计算规则”明确允许的计算外，不对识别出的文字进行任何主观改动或联想。\n\n\n" +
//                "3. 按行解析：文本已按行组织，请根据每行的内容来识别订单条目。同一行的文本通常属于同一个订单项或多个订单项。\n\n" +
//                "4. 商品名称识别规则：\n" +
//                "   - 商品名称可能包含数字和单位，如 \"32板\"、\"10包\" 等\n" +
//                "   - 如果一行中有多个文本块，需要正确配对商品名和数量单位\n" +
//                "   - 重要：不要将单独的数量单位（如 \"4斤\"、\"10包\"、\"2个\"）解析为商品名称\n" +
//                "   - 如果商品名中包含数字和单位（如 \"32板\"），应该保留完整的商品名，数量单独提取\n\n" +
//                "5. 数量单位配对规则（重要）：\n" +
//                "   - 商品名称后面的数字+单位通常是该商品的数量和规格\n" +
//                "   - 示例1：\"大白菜 2个\" → name=\"大白菜\", quantity=\"2\", spec=\"个\"\n" +
//                "   - 示例2：\"32板 4斤\" → name=\"32板\", quantity=\"4\", spec=\"斤\"（注意：\"32板\"是商品名，\"4斤\"是数量和规格）\n" +
//                "   - 示例3：\"32板 4斤 土豆粉 10包\" → 两个商品：\n" +
//                "     * 商品1：name=\"32板\", quantity=\"4\", spec=\"斤\"\n" +
//                "     * 商品2：name=\"土豆粉\", quantity=\"10\", spec=\"包\"\n" +
//                "   - 示例4：\"大白菜 2个 馒头 木个\" → 两个商品：\n" +
//                "     * 商品1：name=\"大白菜\", quantity=\"2\", spec=\"个\"\n" +
//                "     * 商品2：name=\"馒头\", quantity=\"木个\", spec=\"\"（\"木个\"可能是OCR误识别，保持原样）\n" +
//                "   - 关键：如果商品名本身包含数量单位（如 \"32板\"），后面的数字+单位是该商品的数量和规格，不要将后面的数字+单位当作另一个商品\n\n" +
//                "6. 数量合并计算规则（允许且必须执行）：\n" +
//                "   - 如果同一商品的数量以加法形式出现，且单位一致（如：10斤+10斤、5个+3个、2包+2包+1包），你必须进行数学合并，输出合并后的最终数量。\n" +
//                "   - 合并后 quantity 只输出计算结果（如 20），spec 输出单位（如 斤）。\n" +
//                "   - 加号表达式不要放到 note；note 仅用于真正备注（括号内容/说明文字）。\n" +
//                "   - 若加法两侧单位不一致（如 10斤+1包），不得合并：要拆成多个订单条目，分别保留原样。\n\n" +
//                "7. 仅匹配订单格式：你的任务是将这些文字匹配成一个个订单条目，包括商品名称、数量、单位和备注。不要对文本内容做额外推测，只需按文本内容来分组。\n\n" +
//                "输出格式为 JSON 数组，每个元素包含以下字段：\n" +
//                "{\n" +
//                "  \"name\": \"商品名称（原样输出，包含商品名中的数字和单位，如 \\\"32板\\\"）\",\n" +
//                "  \"quantity\": \"数量（如果有，原样输出，不包括商品名中的数字）\",\n" +
//                "  \"spec\": \"规格单位（如果有，如：斤、个、把、包、听、板、袋、瓶、箱、件、捆、条、代等）\",\n" +
//                "  \"standardWeight\": \"规格重量（如果有）\",\n" +
//                "  \"note\": \"备注（如果有，如括号内的内容）\"\n" +
//                "}\n\n" +
//                "请严格按照以上要求进行解析，确保不丢失任何订单信息，并正确配对商品名和数量单位。");
        
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", ocrText);
        
        messages.put(systemMessage);
        messages.put(userMessage);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.1); // 降低温度，确保输出更稳定

        // 设置请求头
        StringEntity entity = new StringEntity(requestBody.toString(), "UTF-8");
        entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + deepSeekApiKey);

        logger.info("[DeepSeek Order] 准备发送请求到 DeepSeek API");
        logger.info("[DeepSeek Order] 请求体长度: {} 字符", requestBody.toString().length());

        // 执行请求
        logger.info("[DeepSeek Order] 正在执行 HTTP 请求...");
        long startTime = System.currentTimeMillis();
        
        CloseableHttpResponse response = null;
        try {
            response = client.execute(httpPost);
            long elapsedTime = System.currentTimeMillis() - startTime;
            logger.info("[DeepSeek Order] HTTP 请求完成，耗时: {} ms ({} 秒)", elapsedTime, elapsedTime / 1000.0);
            
            if (response == null) {
                logger.error("[DeepSeek Order] HTTP 响应为 null");
                throw new IOException("DeepSeek API 返回空响应");
            }
            
            int statusCode = response.getStatusLine().getStatusCode();
            logger.info("[DeepSeek Order] HTTP 响应状态码: {}", statusCode);
            
            if (statusCode != 200) {
                String errorContent = EntityUtils.toString(response.getEntity(), "UTF-8");
                logger.error("[DeepSeek Order] HTTP 错误响应: {}", errorContent);
                throw new IOException("DeepSeek API 返回错误状态码: " + statusCode + ", 响应: " + errorContent);
            }
            
            HttpEntity responseEntity = response.getEntity();
            String responseContent = EntityUtils.toString(responseEntity, "UTF-8");
            logger.info("[DeepSeek Order] 响应内容长度: {} 字符", responseContent.length());
            logger.debug("[DeepSeek Order] 响应内容: {}", responseContent);

            // 解析响应
            JSONObject responseJson = new JSONObject(responseContent);
            JSONArray choices = responseJson.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                String content = message.getString("content");
                
                logger.info("[DeepSeek Order] 提取到的内容长度: {} 字符", content.length());
                
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
                
                logger.info("[DeepSeek Order] 清理后的内容长度: {} 字符", content.length());
                logger.debug("[DeepSeek Order] 清理后的内容: {}", content);
                
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
        } catch (Exception e) {
            logger.error("[DeepSeek Order] 调用异常: {}", e.getMessage(), e);
            if (response != null) {
                try {
                    response.close();
                } catch (Exception ex) {
                    logger.error("[DeepSeek Order] 关闭响应失败", ex);
                }
            }
            try {
                client.close();
            } catch (Exception ex) {
                logger.error("[DeepSeek Order] 关闭客户端失败", ex);
            }
            throw new IOException("DeepSeek API 调用失败: " + e.getMessage(), e);
        }
    }

    
    /**
     * 解析表格格式的行，如 "12.炒菜辣椒面 订:1.5kg"、"14.湘香园酱板鸭订:10只"
     */
    private void parseTableFormatLine(String line, Map<String, Object> item) {
        // 匹配模式：序号.商品名 订:数量单位
        // 例如："12.炒菜辣椒面 订:1.5kg"、"14.湘香园酱板鸭订:10只"
        java.util.regex.Pattern tablePattern = java.util.regex.Pattern.compile("(\\d+)\\.?\\s*(.+?)\\s*[订]\\s*[:：]\\s*(\\d+(?:\\.\\d+)?)(\\w+)");
        java.util.regex.Matcher matcher = tablePattern.matcher(line);
        
        if (matcher.find()) {
            // 提取商品名（去掉序号部分）
            String name = matcher.group(2).trim();
            // 提取数量
            String qty = matcher.group(3);
            // 提取单位
            String unitStr = matcher.group(4).toLowerCase();
            String unit = convertUnit(unitStr);
            
            item.put("name", name);
            item.put("qty", qty);
            item.put("unit", unit);
            item.put("remark", "");
            
            logger.debug("[parseTableFormatLine] 解析成功 - 商品名: {}, 数量: {}, 单位: {}", name, qty, unit);
        } else {
            // 如果正则匹配失败，尝试更宽松的匹配
            // 例如："12.炒菜辣椒面 订:1.5kg" 可能被识别为 "12.炒菜辣椒面订:1.5kg"（没有空格）
            java.util.regex.Pattern loosePattern = java.util.regex.Pattern.compile("(\\d+)\\.?\\s*(.+?)[订]\\s*[:：]\\s*(\\d+(?:\\.\\d+)?)(\\w*)");
            java.util.regex.Matcher looseMatcher = loosePattern.matcher(line);
            
            if (looseMatcher.find()) {
                String name = looseMatcher.group(2).trim();
                String qty = looseMatcher.group(3);
                String unitStr = looseMatcher.group(4).toLowerCase();
                String unit = convertUnit(unitStr);
                
                item.put("name", name);
                item.put("qty", qty);
                item.put("unit", unit);
                item.put("remark", "");
                
                logger.debug("[parseTableFormatLine] 宽松匹配成功 - 商品名: {}, 数量: {}, 单位: {}", name, qty, unit);
            } else {
                // 如果还是匹配失败，尝试手动分割
                int orderIndex = line.indexOf("订:");
                if (orderIndex < 0) {
                    orderIndex = line.indexOf("订：");
                }
                
                if (orderIndex > 0) {
                    // 提取商品名部分（去掉序号）
                    String namePart = line.substring(0, orderIndex).trim();
                    namePart = namePart.replaceFirst("^\\d+\\.?\\s*", "").trim();
                    
                    // 提取数量单位部分
                    String qtyUnitPart = line.substring(orderIndex + 2).trim(); // "订:" 或 "订：" 都是2个字符
                    
                    // 提取数量和单位
                    java.util.regex.Pattern qtyPattern = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)(\\w*)");
                    java.util.regex.Matcher qtyMatcher = qtyPattern.matcher(qtyUnitPart);
                    
                    if (qtyMatcher.find()) {
                        String qty = qtyMatcher.group(1);
                        String unitStr = qtyMatcher.group(2).toLowerCase();
                        String unit = convertUnit(unitStr);
                        
                        item.put("name", namePart);
                        item.put("qty", qty);
                        item.put("unit", unit);
                        item.put("remark", "");
                        
                        logger.debug("[parseTableFormatLine] 手动分割成功 - 商品名: {}, 数量: {}, 单位: {}", namePart, qty, unit);
                    } else {
                        // 如果连数量都提取不出来，整个作为商品名
                        item.put("name", namePart);
                        item.put("qty", "1");
                        item.put("unit", "斤");
                        item.put("remark", "");
                    }
                } else {
                    // 如果找不到"订:"，按简单格式处理
                    logger.warn("[parseTableFormatLine] 无法解析表格格式行: {}", line);
                }
            }
        }
    }
    
    /**
     * 转换单位（将kg、g等转换为中文单位）
     */
    private String convertUnit(String unitStr) {
        if (unitStr == null || unitStr.trim().isEmpty()) {
            return "斤";
        }
        
        unitStr = unitStr.toLowerCase().trim();
        
        // 英文单位转换
        if (unitStr.equals("kg") || unitStr.equals("公斤") || unitStr.equals("千克")) {
            return "斤";
        }
        if (unitStr.equals("g") || unitStr.equals("克")) {
            return "斤";
        }
        
        // 中文单位直接返回
        String[] chineseUnits = {"个", "斤", "把", "包", "根", "棵", "条", "盒", "捆", "袋", "瓶", "罐", "桶", "箱", "只", "支", "片", "块", "张", "件", "套", "组", "对", "双", "打"};
        for (String unit : chineseUnits) {
            if (unitStr.equals(unit) || unitStr.startsWith(unit)) {
                return unit;
            }
        }
        
        // 默认返回斤
        return "斤";
    }

    /**
     * 清洗 OCR 文本，去除噪音字符，规范化格式
     * 
     * @param ocrText OCR 识别的原始文本
     * @return 清洗后的文本
     */
    private String cleanOcrText(String ocrText) {
        if (ocrText == null || ocrText.trim().isEmpty()) {
            return "";
        }
        
        StringBuilder cleaned = new StringBuilder();
        String[] lines = ocrText.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            
            // 过滤明显无关的文本行（表格字段、低置信度的噪音等）
            // 跳过纯符号、纯数字序号、表格字段等
            if (line.matches("^[\\s\\-\\|:：]+$") ||  // 纯符号
                line.matches("^\\d+\\.?$") ||        // 纯数字序号
                line.matches(".*[出货单价小计合计总计]:.*") ||  // 表格字段
                line.matches(".*[出货单价小计合计总计]：.*") ||
                line.equals("出") || line.equals("单") || line.equals("小计") ||
                line.length() < 2) {  // 过短的行（可能是噪音）
                logger.debug("[cleanOcrText] 过滤噪音行: {}", line);
                continue;
            }
            
            // 保留有效行
            if (cleaned.length() > 0) {
                cleaned.append("\n");
            }
            cleaned.append(line);
        }
        
        return cleaned.toString();
    }
    
    /**
     * 清洗 DeepSeek 返回的 JSON 结果，去除代码块标记等
     * 
     * @param response DeepSeek 返回的原始响应
     * @return 清洗后的 JSON 字符串
     */
    private String cleanDeepSeekResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            throw new IllegalArgumentException("DeepSeek 返回结果为空");
        }
        
        String cleaned = response.trim();
        
        // 去除 Markdown 代码块标记
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7).trim();
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3).trim();
        }
        
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
        }
        
        // 尝试提取 JSON 数组部分（如果有其他文本）
        int jsonStart = cleaned.indexOf('[');
        int jsonEnd = cleaned.lastIndexOf(']');
        
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            cleaned = cleaned.substring(jsonStart, jsonEnd + 1);
        }
        
        cleaned = cleaned.trim();
        
        logger.debug("[cleanDeepSeekResponse] 清洗后的 JSON: {}", cleaned);
        
        return cleaned;
    }
    
    /**
     * 清洗 DeepSeek 返回的 JSON 对象结果，去除代码块标记等
     * 用于处理 JSON 对象（而不是数组）
     * 
     * @param response DeepSeek 返回的原始响应
     * @return 清洗后的 JSON 字符串
     */
    private String cleanDeepSeekJsonResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            throw new IllegalArgumentException("DeepSeek 返回结果为空");
        }
        
        String cleaned = response.trim();
        
        // 去除 Markdown 代码块标记
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7).trim();
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3).trim();
        }
        
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
        }
        
        // 尝试提取 JSON 对象部分（如果有其他文本）
        int jsonStart = cleaned.indexOf('{');
        int jsonEnd = cleaned.lastIndexOf('}');
        
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            cleaned = cleaned.substring(jsonStart, jsonEnd + 1);
        } else if (jsonStart < 0 || jsonEnd < 0) {
            // 如果找不到 { 或 }，说明格式可能有问题，记录警告但返回原始内容
            logger.warn("[cleanDeepSeekJsonResponse] 无法找到 JSON 对象的开始或结束标记，jsonStart: {}, jsonEnd: {}, 原始内容: {}", 
                    jsonStart, jsonEnd, cleaned);
        }
        
        cleaned = cleaned.trim();
        
        // 验证结果是否以 { 开头
        if (!cleaned.startsWith("{")) {
            logger.error("[cleanDeepSeekJsonResponse] 清洗后的内容不是以 '{' 开头: {}", cleaned);
            throw new IllegalArgumentException("清洗后的 JSON 格式不正确，不是以 '{' 开头: " + cleaned.substring(0, Math.min(50, cleaned.length())));
        }
        
        logger.debug("[cleanDeepSeekJsonResponse] 清洗后的 JSON 长度: {}", cleaned.length());
        
        return cleaned;
    }
    
    /**
     * 将 DeepSeek 返回的字段映射到内部使用的字段
     * DeepSeek 返回：name, quantity, spec, note
     * 内部使用：name, qty, unit, remark
     * 
     * 包含兜底逻辑：如果 name 中包含括号，自动清理并补充到 note
     * 
     * @param deepSeekItem DeepSeek 返回的商品项
     * @return 映射后的商品项
     */
    private Map<String, Object> mapDeepSeekFieldsToInternalFields(Map<String, Object> deepSeekItem) {
        Map<String, Object> mappedItem = new HashMap<>();
        
        // name 处理（包含兜底逻辑：清理括号内容）
        Object nameObj = deepSeekItem.get("name");
        String originalName = nameObj != null ? nameObj.toString().trim() : "";
        String cleanName = normalizeName(originalName);
        mappedItem.put("name", cleanName);
        
        // quantity -> qty
        Object quantity = deepSeekItem.get("quantity");
        if (quantity != null) {
            mappedItem.put("qty", quantity.toString().trim());
        } else {
            mappedItem.put("qty", "1");
        }
        
        // spec -> unit（需要转换为标准单位）
        Object spec = deepSeekItem.get("spec");
        String unit = "斤"; // 默认单位
        if (spec != null) {
            String specStr = spec.toString().trim().toLowerCase();
            unit = convertUnit(specStr);
        }
        mappedItem.put("unit", unit);
        
        // note -> remark（如果 name 被清理，将括号内容补充到 note）
        Object noteObj = deepSeekItem.get("note");
        String note = noteObj != null ? noteObj.toString().trim() : "";
        
        // 兜底逻辑：如果 name 中包含括号，提取括号内容并补充到 note
        if (!originalName.equals(cleanName)) {
            String bracketContent = extractBracketContent(originalName);
            if (!bracketContent.isEmpty()) {
                // 如果 note 为空，直接使用括号内容；否则追加
                if (note.isEmpty()) {
                    note = bracketContent;
                } else {
                    note = bracketContent + " " + note;
                }
                logger.debug("[mapDeepSeekFieldsToInternalFields] 兜底逻辑：从 name 中提取括号内容到 note，name: {} -> {}, note: {}", 
                    originalName, cleanName, note);
            }
        }
        
        mappedItem.put("remark", note);
        
        logger.debug("[mapDeepSeekFieldsToInternalFields] 字段映射: {} -> {}", deepSeekItem, mappedItem);
        
        return mappedItem;
    }
    
    /**
     * 规范化商品名称：去掉所有括号及括号内容
     * 这是兜底逻辑，确保即使 DeepSeek 返回的 name 包含括号，也能被正确清理
     * 
     * @param name 原始商品名称
     * @return 清理后的商品名称（不包含括号及括号内容）
     */
    /**
     * 清洗 OCR 识别到的文本行
     * 
     * @param s OCR 文本行
     * @return 清洗后的文本
     */
    private String normalizeOcrLine(String s) {
        if (s == null) return "";
        String t = s.trim();

        // 统一各种"点"符号
        t = t.replace('．', '.')
             .replace('。', '.')
             .replace('·', '.');

        // 1) 删除"中文后面的特殊符号"：黄瓜. -> 黄瓜，黄瓜，-> 黄瓜，黄瓜/ -> 黄瓜，黄豆芽、-> 黄豆芽
        //    只要这些符号前面是汉字，就删掉这些符号
        //    特殊符号：. , ，、-/ （注意：。已经被转换为.，- 需要放在字符类末尾或转义）
        t = t.replaceAll("(?<=\\p{IsHan})[\\.，,、/\\-]+", "");

        // 2) 删除"非数字小数点"的孤立点（可选，偏激进）
        //    解释：把不是"数字.数字"的点干掉
        // t = t.replaceAll("(?<!\\d)\\.(?!\\d)|(?<=\\D)\\.(?=\\D)", "");

        // 3) 删除末尾残留的特殊符号：10斤. -> 10斤，10斤，-> 10斤，10斤/ -> 10斤，黄豆芽、-> 黄豆芽
        //    特殊符号：. , ，、-/ （注意：。已经被转换为.，- 需要放在字符类末尾或转义）
        t = t.replaceAll("[\\.，,、/\\-]+$", "");

        // 4) 多空格压缩
        t = t.replaceAll("\\s+", " ").trim();

        return t;
    }

    private String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }
        
        // 去掉中英文括号及内容：()（）【】[]
        String cleanName = name
            .replaceAll("（.*?）", "")  // 中文括号
            .replaceAll("\\(.*?\\)", "")  // 英文括号
            .replaceAll("【.*?】", "")  // 中文方括号
            .replaceAll("\\[.*?\\]", "")  // 英文方括号
            .trim();
        
        return cleanName;
    }
    
    /**
     * 提取括号中的内容
     * 
     * @param name 包含括号的商品名称
     * @return 括号中的内容（多个括号内容用空格连接）
     */
    private String extractBracketContent(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }
        
        List<String> contents = new ArrayList<>();
        
        // 提取中文括号内容
        java.util.regex.Pattern chinesePattern = java.util.regex.Pattern.compile("（(.*?)）");
        java.util.regex.Matcher chineseMatcher = chinesePattern.matcher(name);
        while (chineseMatcher.find()) {
            contents.add(chineseMatcher.group(1));
        }
        
        // 提取英文括号内容
        java.util.regex.Pattern englishPattern = java.util.regex.Pattern.compile("\\((.*?)\\)");
        java.util.regex.Matcher englishMatcher = englishPattern.matcher(name);
        while (englishMatcher.find()) {
            contents.add(englishMatcher.group(1));
        }
        
        // 提取中文方括号内容
        java.util.regex.Pattern chineseBracketPattern = java.util.regex.Pattern.compile("【(.*?)】");
        java.util.regex.Matcher chineseBracketMatcher = chineseBracketPattern.matcher(name);
        while (chineseBracketMatcher.find()) {
            contents.add(chineseBracketMatcher.group(1));
        }
        
        // 提取英文方括号内容
        java.util.regex.Pattern englishBracketPattern = java.util.regex.Pattern.compile("\\[(.*?)\\]");
        java.util.regex.Matcher englishBracketMatcher = englishBracketPattern.matcher(name);
        while (englishBracketMatcher.find()) {
            contents.add(englishBracketMatcher.group(1));
        }
        
        return String.join(" ", contents);
    }
    
    /**
     * 验证解析结果是否有效
     * 
     * @param itemsList 解析后的商品列表
     * @return true 如果结果有效，false 如果结果无效
     */
    private boolean isValidParseResult(List<Map<String, Object>> itemsList) {
        if (itemsList == null || itemsList.isEmpty()) {
            return false;
        }
        
        // 检查每个商品是否有有效的名称
        int validItemCount = 0;
        for (Map<String, Object> item : itemsList) {
            Object nameObj = item.get("name");
            if (nameObj != null) {
                String name = nameObj.toString().trim();
                // 跳过明显无效的商品名（如"出货:"、"单价:"等表格字段）
                if (!name.isEmpty() && 
                    !name.matches(".*[出货单价小计]:.*") && 
                    !name.matches(".*[出货单价小计]：.*") &&
                    !name.equals("出") && 
                    !name.equals("单") &&
                    !name.matches("^\\d+\\.?$")) { // 跳过纯数字（序号）
                    validItemCount++;
                }
            }
        }
        
        // 至少要有1个有效商品
        boolean isValid = validItemCount > 0;
        if (!isValid) {
            logger.warn("[isValidParseResult] 解析结果无效：{} 个商品中只有 {} 个有效", itemsList.size(), validItemCount);
        }
        
        return isValid;
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
    
    /**
     * 判断是否为无关文本（日历、星期、页码等）
     */
    /**
     * 判断是否为无关文本（垃圾行）
     * 过滤：日历、星期、单字符、纯符号等
     */
    private boolean isIrrelevantText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return true;
        }
        
        String trimmedText = text.trim();
        String upperText = trimmedText.toUpperCase();
        
        // 1. 过滤纯符号行（不包含任何中文和数字）
        if (trimmedText.matches("^-{1,}$")) {
            return true; // 纯横线，如 "--"
        }
        if (trimmedText.matches("^[^\\u4e00-\\u9fa5\\d]+$")) {
            // 不包含中文和数字的纯符号行（如 "|"、"---" 等）
            return true;
        }
        
        // 2. 过滤单字母（如 "B"）
        if (trimmedText.matches("^[A-Za-z]$")) {
            return true;
        }
        
        // 3. 过滤日历相关文本
        if (upperText.matches(".*(MON|TUE|WED|THU|FRI|SAT|SUN|MO|TU|WE|TH|FR|SA|SU).*")) {
            return true; // 星期
        }
        if (upperText.matches(".*\\d{1,2}/\\d{1,2}.*")) {
            return true; // 日期格式
        }
        if (upperText.matches(".*(PAGE|页|第.*页).*")) {
            return true; // 页码
        }
        
        // 4. 注意：不再过滤单字符中文和短数字
        // 单字符中文（如 "好"、"红"、"蜂"）可能是商品名的误识别，让后续解析和 needConfirm 机制处理
        // 短数字（如 "29"、"17"）是商品数量，需要在聚类中与商品名配对
        
        return false;
    }
    
    /**
     * 判断是否为数量文本（包含数字和单位）
     */
    /**
     * 判断是否为纯数量 token（不允许出现中文）
     * 只接受：29 / 1 / 14 / 5斤 / 3个 / 3把 / 3卷 / 1.5斤
     * 不接受：白沙拉5斤（包含中文）
     */
    private boolean isPureQuantityToken(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        String t = text.trim();
        
        // 有中文就不是"纯数量行"
        if (t.matches(".*[\\u4e00-\\u9fa5].*")) {
            return false;
        }
        
        // 常见误识别：1h / 15h （手写"斤"容易被识别成 h）
        // 先在这里归一化：把 h 当斤
        t = t.replaceAll("(?i)h$", "斤");
        
        // 纯数字
        if (t.matches("^\\d+(\\.\\d+)?$")) {
            return true;
        }
        
        // 数字 + 单位（斤/个/把/卷/袋/箱/包/瓶/桶/件/盒/板/条/提/支/张/根/只）
        return t.matches("^\\d+(\\.\\d+)?(斤|个|把|卷|袋|包|箱|瓶|桶|件|盒|板|条|提|支|张|根|只)$");
    }
    
    /**
     * 单位纠错：将常见误识别的单位转换为标准单位
     * @param text 原始文本
     * @return 纠错后的文本
     */
    private String normalizeUnit(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        String normalized = text.trim();
        
        // 1. 代 -> 袋（常见误识别）
        normalized = normalized.replaceAll("(\\d+(\\.\\d+)?)代", "$1袋");
        
        // 2. h -> 斤（手写"斤"容易被识别成 h，已在 isPureQuantityToken 中处理，这里也统一处理）
        normalized = normalized.replaceAll("(\\d+(\\.\\d+)?)h", "$1斤");
        normalized = normalized.replaceAll("(\\d+(\\.\\d+)?)H", "$1斤");
        
        // 3. O/〇 -> 0（数字识别错误）
        normalized = normalized.replaceAll("^O$", "0");
        normalized = normalized.replaceAll("^〇$", "0");
        
        return normalized;
    }
    
    /**
     * 将坐标数组转换为字符串（用于日志输出）
     */
    private String polygonToString(Coord[] polygon) {
        if (polygon == null || polygon.length == 0) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < polygon.length; i++) {
            Coord c = polygon[i];
            if (c == null) continue;
            sb.append("{x=").append(c.getX()).append(",y=").append(c.getY()).append("}");
            if (i < polygon.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * OCR 文本框（包含坐标信息）
     */
    static class OcrBox {
        String text;
        long conf;
        float cx, cy;     // 中心点
        float height;     // 盒子高度（用于动态阈值）
        Coord[] polygon;

        OcrBox(String text, long conf, Coord[] polygon) {
            this.text = text;
            this.conf = conf;
            this.polygon = polygon;
            float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
            if (polygon != null && polygon.length > 0) {
                for (Coord c : polygon) {
                    if (c == null) continue;
                    float x = c.getX();
                    float y = c.getY();
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
            this.cx = (minX + maxX) / 2f;
            this.cy = (minY + maxY) / 2f;
            this.height = Math.max(1f, (maxY - minY));
        }
    }
    
    /**
     * 解析后的订单行
     */
    static class ParsedLine {
        String name;
        String qty;   // 数量
        String unit;  // 单位
        String note;  // 备注
        boolean needConfirm;
        String rawLine; // 便于前端展示/回溯
    }
    
    /**
     * 判断是否为噪声文本（垃圾行）
     * 注意：不要过滤中文商品名
     */
    private boolean isNoise(String s) {
        if (s == null) return true;
        String t = s.trim();
        if (t.isEmpty()) return true;
        if (t.matches("^-{1,}$")) return true;      // ----
        if (t.matches("^[A-Za-z]$")) return true;   // 单个字母
        // 修复：\W 会匹配中文字符，导致中文商品名被误过滤
        // 改为：只过滤不包含中文、数字、字母的纯符号
        if (t.matches("^[^\\u4e00-\\u9fa5a-zA-Z0-9]+$")) {
            return true; // 全是符号（不包含中文、字母、数字）
        }
        return false;
    }
    
    /**
     * 判断是否为纯数字
     */
    private boolean isPureNumber(String t) {
        return t != null && t.trim().matches("^\\d+(?:\\.\\d+)?$");
    }
    
    /**
     * 判断是否为带单位的数量
     */
    private boolean isQtyWithUnit(String t) {
        // 支持 10斤 / 1袋 / 2个 / 0.5斤
        return t != null && t.trim().matches("^\\d+(?:\\.\\d+)?(斤|个|把|袋|包|卷|箱|桶|件|盒|代)$");
    }
    
    /**
     * 按 y 坐标聚类成行（阈值用"高度自适应"）
     */
    private List<List<OcrBox>> groupByRows(List<OcrBox> boxes) {
        if (boxes == null || boxes.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 先按 y 排序
        boxes.sort((b1, b2) -> Float.compare(b1.cy, b2.cy));

        List<List<OcrBox>> rows = new ArrayList<>();
        for (OcrBox b : boxes) {
            boolean placed = false;

            // 行阈值：使用固定阈值 25 像素（根据你的图片，商品名和数量 y 坐标差通常在 10-20px）
            // 如果使用高度自适应，对于高度较大的框（如 100px），阈值会变成 70px，太大
            float yThresh = 25f; // 固定阈值，更稳定

            for (List<OcrBox> row : rows) {
                // 用行的平均 y
                float avgY = 0f;
                for (OcrBox rb : row) avgY += rb.cy;
                avgY /= row.size();

                float yDiff = Math.abs(b.cy - avgY);
                if (yDiff <= yThresh) {
                    row.add(b);
                    placed = true;
                    logger.debug("[recognizeOrder] 聚类: {} (y={}) 加入行 (avgY={}, diff={})", 
                        b.text, b.cy, avgY, yDiff);
                    break;
                }
            }

            if (!placed) {
                List<OcrBox> newRow = new ArrayList<>();
                newRow.add(b);
                rows.add(newRow);
            }
        }

        // 行内按 x 排序
        for (List<OcrBox> row : rows) {
            row.sort((r1, r2) -> Float.compare(r1.cx, r2.cx));
        }
        return rows;
    }
    
    /**
     * 一行按 x 坐标排序（不再进行分列判断）
     * 
     * @param row 一行的所有 OCR 框
     * @return 排序后的块列表（整行作为一个块）
     */
    private List<List<OcrBox>> splitRowByX(List<OcrBox> row) {
        List<List<OcrBox>> blocks = new ArrayList<>();
        if (row == null || row.isEmpty()) {
            return blocks;
        }

        // 按 x 坐标排序
        row.sort(Comparator.comparingDouble(b -> b.cx));

        // 直接返回整行作为一个块，不再进行分列判断
            blocks.add(new ArrayList<>(row));
        
        return blocks;
    }
    
    /**
     * 数量单位匹配模式（支持常见单位）
     * 注意：使用非贪婪匹配，避免把 "12斤" 拆成 "1斤" 和 "2斤"
     * 注意：不包含带括号的单位（如 "个(空心)"），带括号的情况由 QTY_UNIT_WITH_BRACKET_PATTERN 处理
     */
    private static final Pattern QTY_UNIT_PATTERN = Pattern.compile(
        "^(\\d+(?:\\.\\d+)?)(斤|个|把|包|听|板|袋|瓶|箱|件|捆|条|代)$"
    );
    
    /**
     * 匹配带括号的数量单位（如 "20个(空心)"）
     */
    private static final Pattern QTY_UNIT_WITH_BRACKET_PATTERN = Pattern.compile(
        "^(\\d+(?:\\.\\d+)?)(个|把|包|听|板|袋|瓶|箱|件|捆|条|代)\\((.+?)\\)$"
    );
    
    /**
     * 匹配完整的"商品名+数量+单位"（如 "面条5斤"、"圆白菜20个(空心)"）
     */
    private static final Pattern NAME_QTY_UNIT_PATTERN = Pattern.compile(
        "^(.+?)(\\d+(?:\\.\\d+)?)(斤|个|把|包|听|板|袋|瓶|箱|件|捆|条|代)$"
    );
    
    /**
     * 匹配完整的"商品名+数量+单位(备注)"（如 "圆白菜20个(空心)"）
     */
    private static final Pattern NAME_QTY_UNIT_BRACKET_PATTERN = Pattern.compile(
        "^(.+?)(\\d+(?:\\.\\d+)?)(个|把|包|听|板|袋|瓶|箱|件|捆|条|代)\\((.+?)\\)$"
    );
    
    /**
     * 解析一个"商品块"成为多个 ParsedLine（支持 block 内多个独立商品）
     * 
     * @param block 一个商品块的所有 OCR 框（通常是左列或右列）
     * @return 解析后的订单行列表
     */
    private List<ParsedLine> blockToParsedLines(List<OcrBox> block) {
        List<ParsedLine> result = new ArrayList<>();
        if (block == null || block.isEmpty()) {
            return result;
        }
        
        // 如果只有一个框，使用原来的逻辑
        if (block.size() == 1) {
            ParsedLine pl = blockToParsedLine(block);
            if (pl != null) {
                result.add(pl);
            }
            return result;
        }
        
        // 按 x 坐标排序
        block.sort(Comparator.comparingDouble(b -> b.cx));
        
        // 识别 block 内的多个独立商品
        // 策略：
        // 1. 先识别完整的"商品名+数量+单位"框（如 "面条5斤"）
        // 2. 对于剩余框，尝试配对：商品名框 + 数量单位框
        
        List<OcrBox> processedBoxes = new ArrayList<>();
        List<OcrBox> remainingBoxes = new ArrayList<>(block);
        
        // 第一步：识别完整的"商品名+数量+单位"框
        for (OcrBox box : block) {
            String text = box.text != null ? box.text.trim() : "";
            if (text.isEmpty()) {
                continue;
            }
            
            String compact = text.replaceAll("\\s+", "");
            
            // 先尝试匹配"商品名+数量+单位(备注)"
            Matcher mBracket = NAME_QTY_UNIT_BRACKET_PATTERN.matcher(compact);
            if (mBracket.find()) {
                String name = mBracket.group(1);
                String qty = mBracket.group(2);
                String unit = mBracket.group(3);
                String note = mBracket.group(4);
                
                ParsedLine pl = new ParsedLine();
                pl.rawLine = text;
                pl.name = name;
                pl.qty = qty;
                pl.unit = unit;
                pl.note = note != null ? note : "";
                pl.needConfirm = false;
                result.add(pl);
                processedBoxes.add(box);
                continue;
            }
            
            // 再尝试匹配"商品名+数量+单位"
            Matcher m = NAME_QTY_UNIT_PATTERN.matcher(compact);
            if (m.find()) {
                String name = m.group(1);
                String qty = m.group(2);
                String unit = m.group(3);
                
                ParsedLine pl = new ParsedLine();
                pl.rawLine = text;
                pl.name = name;
                pl.qty = qty;
                pl.unit = unit;
                pl.note = "";
                pl.needConfirm = false;
                result.add(pl);
                processedBoxes.add(box);
                continue;
            }
        }
        
        // 移除已处理的框
        remainingBoxes.removeAll(processedBoxes);
        
        // 第二步：对剩余框进行配对（商品名 + 数量单位）
        if (!remainingBoxes.isEmpty()) {
            // 识别数量单位框和商品名框，并按 x 坐标排序
            List<OcrBox> qtyUnitBoxes = new ArrayList<>();
            List<OcrBox> nameBoxes = new ArrayList<>();
            
            for (OcrBox box : remainingBoxes) {
                String text = box.text != null ? box.text.trim() : "";
                if (text.isEmpty()) {
                    continue;
                }
                
                String compact = text.replaceAll("\\s+", "");
                
                // 尝试匹配数量单位
                Matcher mBracket = QTY_UNIT_WITH_BRACKET_PATTERN.matcher(compact);
                if (mBracket.find()) {
                    qtyUnitBoxes.add(box);
                    continue;
                }
                
                Matcher m = QTY_UNIT_PATTERN.matcher(compact);
                if (m.find()) {
                    qtyUnitBoxes.add(box);
                    continue;
                }
                
                // 不是数量单位框，当作商品名框
                nameBoxes.add(box);
            }
            
            // 按 x 坐标排序
            qtyUnitBoxes.sort(Comparator.comparingDouble(b -> b.cx));
            nameBoxes.sort(Comparator.comparingDouble(b -> b.cx));
            
            // 配对：每个数量单位框尝试和前面最近的商品名框配对
            Set<OcrBox> usedNameBoxes = new HashSet<>();
            for (OcrBox qtyUnitBox : qtyUnitBoxes) {
                String qtyUnitText = qtyUnitBox.text != null ? qtyUnitBox.text.trim() : "";
                String compact = qtyUnitText.replaceAll("\\s+", "");
                
                String qty = "";
                String unit = "";
                String note = "";
                
                // 提取数量和单位
                Matcher mBracket = QTY_UNIT_WITH_BRACKET_PATTERN.matcher(compact);
                if (mBracket.find()) {
                    qty = mBracket.group(1);
                    unit = mBracket.group(2);
                    note = mBracket.group(3) != null ? mBracket.group(3) : "";
                } else {
                    Matcher m = QTY_UNIT_PATTERN.matcher(compact);
                    if (m.find()) {
                        qty = m.group(1);
                        unit = m.group(2);
                    }
                }
                
                // 找到对应的商品名框（取最近的、在数量单位框之前的、未使用的商品名框）
                String name = "";
                String rawLine = qtyUnitText;
                
                // 从后往前找，找到最近的、在数量单位框之前的商品名框
                for (int i = nameBoxes.size() - 1; i >= 0; i--) {
                    OcrBox nameBox = nameBoxes.get(i);
                    if (!usedNameBoxes.contains(nameBox) && nameBox.cx < qtyUnitBox.cx) {
                        name = nameBox.text != null ? nameBox.text.trim() : "";
                        rawLine = name + " " + qtyUnitText;
                        usedNameBoxes.add(nameBox);
                        break;
                    }
                }
                
                // 如果没找到商品名框，尝试从数量单位框的文本中提取（可能是 "32板" 这种情况）
                if (name.isEmpty()) {
                    // 这种情况应该已经被第一步识别了，但为了安全起见，还是处理一下
                    name = compact;
                    rawLine = qtyUnitText;
                }
                
                ParsedLine pl = new ParsedLine();
                pl.rawLine = rawLine;
                pl.name = name;
                pl.qty = qty;
                pl.unit = unit;
                pl.note = note;
                pl.needConfirm = qty.isEmpty();
                result.add(pl);
            }
            
            // 处理剩余的商品名框（没有配对的）
            for (OcrBox nameBox : nameBoxes) {
                if (!usedNameBoxes.contains(nameBox)) {
                    String name = nameBox.text != null ? nameBox.text.trim() : "";
                    
                    ParsedLine pl = new ParsedLine();
                    pl.rawLine = name;
                    pl.name = name;
                    pl.qty = "";
                    pl.unit = "";
                    pl.note = "";
                    pl.needConfirm = true;
                    result.add(pl);
                }
            }
        }
        
        // 如果没有任何结果，使用原来的逻辑作为兜底
        if (result.isEmpty()) {
            ParsedLine pl = blockToParsedLine(block);
            if (pl != null) {
                result.add(pl);
            }
        }
        
        return result;
    }
    
    /**
     * 解析一个"商品块"成为 ParsedLine（block 内配对商品名和数量单位）
     * 
     * @param block 一个商品块的所有 OCR 框（通常是左列或右列）
     * @return 解析后的订单行
     */
    private ParsedLine blockToParsedLine(List<OcrBox> block) {
        if (block == null || block.isEmpty()) {
            return null;
        }

        // 按 x 坐标排序
        block.sort(Comparator.comparingDouble(b -> b.cx));

        // 先拼接所有文本（用于整体匹配和 rawLine）
        StringBuilder rawBuilder = new StringBuilder();
        for (OcrBox b : block) {
            String t = b.text != null ? b.text.trim() : "";
            if (!t.isEmpty()) {
                if (rawBuilder.length() > 0) {
                    rawBuilder.append(" ");
                }
                rawBuilder.append(t);
            }
        }
        String raw = rawBuilder.toString().trim();
        
        if (raw.isEmpty()) {
            return null;
        }

        String name = "";
        String qty = "";
        String unit = "";
        String note = "";

        // 如果 block 内只有一个框，先尝试整体匹配"商品名+数量+单位(备注)"模式（如 "圆白菜20个(空心)"）
        if (block.size() == 1) {
            String singleText = block.get(0).text != null ? block.get(0).text.trim() : "";
            if (!singleText.isEmpty()) {
                String compactSingle = singleText.replaceAll("\\s+", "");
                // 先尝试匹配"商品名+数量+单位(备注)"
                Pattern pNameQtyUnitBracket = Pattern.compile("^(.+?)(\\d+(?:\\.\\d+)?)(个|把|包|听|板|袋|瓶|箱|件|捆|条|代)\\((.+?)\\)$");
                Matcher mNameQtyUnitBracket = pNameQtyUnitBracket.matcher(compactSingle);
                if (mNameQtyUnitBracket.find()) {
                    name = mNameQtyUnitBracket.group(1);
                    qty = mNameQtyUnitBracket.group(2);
                    unit = mNameQtyUnitBracket.group(3);
                    String bracketNote = mNameQtyUnitBracket.group(4);
                    if (bracketNote != null && !bracketNote.isEmpty()) {
                        note = bracketNote;
                    }
                    logger.debug("[blockToParsedLine] 单个框整体匹配成功: text={}, name={}, qty={}, unit={}, note={}", 
                        singleText, name, qty, unit, note);
                    // 如果整体匹配成功，直接返回
                    if (!name.isEmpty() && !qty.isEmpty()) {
                        ParsedLine pl = new ParsedLine();
                        pl.rawLine = raw;
                        pl.name = name;
                        pl.qty = qty;
                        pl.unit = unit;
                        pl.note = note != null ? note : "";
                        pl.needConfirm = false;
                        return pl;
                    }
                }
            }
        }

        // 遍历 block 内的每个框，先识别数量单位框，再识别商品名框
        // 注意：如果 block 内只有一个框，且匹配到数量单位模式，应该将其作为商品名（如 "32板"）
        boolean isSingleBox = block.size() == 1;
        
        for (OcrBox b : block) {
            String t = b.text != null ? b.text.trim() : "";
            if (t.isEmpty()) {
                continue;
            }
            
            // 保存原始文本（用于后续兜底）
            String originalText = t;
            
            // 去掉空格
            t = t.replaceAll("\\s+", "");
            
            // 先尝试匹配带括号的数量单位（如 "20个(空心)"）
            // 注意：在 normalizeUnit 之前匹配，避免括号被处理掉
            Matcher mBracket = QTY_UNIT_WITH_BRACKET_PATTERN.matcher(t);
            if (mBracket.find()) {
                // 匹配成功：这是数量单位框（带备注）
                // 注意：即使 block 内只有一个框，如果匹配到带括号的数量单位，也应该提取数量和备注
                // 因为带括号的数量单位通常表示完整的数量信息，不应该被当作商品名
                qty = mBracket.group(1);
                unit = mBracket.group(2);
                String bracketNote = mBracket.group(3);
                if (bracketNote != null && !bracketNote.isEmpty()) {
                    // 备注去掉括号，直接使用括号内的内容
                    note = bracketNote;
                    logger.debug("[blockToParsedLine] 提取到备注: text={}, qty={}, unit={}, note={}", 
                        originalText, qty, unit, note);
                } else {
                    logger.debug("[blockToParsedLine] 匹配到带括号模式但备注为空: text={}, qty={}, unit={}", 
                        originalText, qty, unit);
                }
                continue;
            }
            
            // 再尝试匹配普通数量单位（如 "12斤"、"2个"）
            Matcher m = QTY_UNIT_PATTERN.matcher(t);
            if (m.find()) {
                // 如果 block 内只有一个框，且 name 为空，应该将其作为商品名（如 "32板"）
                // 但如果 name 不为空，说明这是数量单位框
                if (isSingleBox && name.isEmpty()) {
                    // 将整个文本作为商品名，不提取数量单位
                    name += originalText.replaceAll("\\s+", "");
                } else {
                    // 匹配成功：这是数量单位框
                    qty = m.group(1);
                    unit = m.group(2);
                    // 注意：带括号的情况（如 "20个(空心)"）应该已经被 QTY_UNIT_WITH_BRACKET_PATTERN 匹配了
                }
                continue;
            }
            
            // 匹配失败：尝试匹配纯数字（可能是数量，但没有单位）
            if (isPureNumber(t)) {
                // 如果 block 内只有一个框，且是纯数字，应该将其作为商品名的一部分
                if (isSingleBox && name.isEmpty()) {
                    name += originalText.replaceAll("\\s+", "");
                } else {
                    // 这是纯数字框，可能是数量（但没有单位）
                    // 先保存，如果后面没有匹配到数量单位，就使用这个
                    if (qty.isEmpty()) {
                        // 修复两位数字：29->2个，17->1个，37->3个
                        String fixedQty = fixQtyAsGeIfNeeded(t);
                        if (!fixedQty.equals(t)) {
                            // 修复成功：如 "29" -> "2个"
                            qty = fixedQty.substring(0, 1); // "2个" -> "2"
                            unit = "个";
                        } else {
                            // 未修复：保持原样
                            qty = t;
                            unit = "";
                        }
                    }
                }
                continue;
            }
            
            // 都不匹配：这是商品名框
            name += originalText.replaceAll("\\s+", "");
        }
        
        // 如果 name 不为空但 qty 为空，尝试从 name 中提取数量单位（处理粘连情况，如 "麻辣鲜露1个"、"圆白菜20个(空心)"）
        if (!name.isEmpty() && qty.isEmpty()) {
            String compactName = name.replaceAll("\\s+", "");
            compactName = normalizeUnit(compactName);
            compactName = compactName.replace("木个", "1个")
                            .replace("未个", "1个")
                            .replace("本个", "1个");
            
            // 先尝试匹配"商品名+数量+单位(备注)"（如 "圆白菜20个(空心)"）
            Pattern pNameQtyUnitBracket = Pattern.compile("^(.+?)(\\d+(?:\\.\\d+)?)(个|把|包|听|板|袋|瓶|箱|件|捆|条|代)\\((.+?)\\)$");
            Matcher mNameQtyUnitBracket = pNameQtyUnitBracket.matcher(compactName);
            if (mNameQtyUnitBracket.find()) {
                name = mNameQtyUnitBracket.group(1);
                qty = mNameQtyUnitBracket.group(2);
                unit = mNameQtyUnitBracket.group(3);
                String bracketNote = mNameQtyUnitBracket.group(4);
                if (bracketNote != null && !bracketNote.isEmpty()) {
                    note = bracketNote;
                }
            } else {
                // 再尝试匹配"商品名+数量+单位"（如 "麻辣鲜露1个"）
                Pattern pNameQtyUnit = Pattern.compile("^(.+?)(\\d+(?:\\.\\d+)?)(斤|个|把|包|听|板|袋|瓶|箱|件|捆|条|代)$");
                Matcher mNameQtyUnit = pNameQtyUnit.matcher(compactName);
                if (mNameQtyUnit.find()) {
                    name = mNameQtyUnit.group(1);
                    qty = mNameQtyUnit.group(2);
                    unit = mNameQtyUnit.group(3);
                } else {
                    // 再尝试匹配"商品名+纯数字"（如 "白胡椒面29"）
                    Pattern pNameQty = Pattern.compile("^(.+?)(\\d+(?:\\.\\d+)?)$");
                    Matcher mNameQty = pNameQty.matcher(compactName);
                    if (mNameQty.find()) {
                        name = mNameQty.group(1);
                        String qtyRaw = mNameQty.group(2);
                        
                        // 修复两位数字：29->2个，17->1个，37->3个
                        String fixedQty = fixQtyAsGeIfNeeded(qtyRaw);
                        if (!fixedQty.equals(qtyRaw)) {
                            // 修复成功：如 "29" -> "2个"
                            qty = fixedQty.substring(0, 1); // "2个" -> "2"
                            unit = "个";
                        } else {
                            // 未修复：保持原样
                            qty = qtyRaw;
                            unit = "";
                        }
                    }
                }
            }
        }
        
        // 如果 name 为空且 qty 为空，尝试整体匹配
        if (name.isEmpty() && qty.isEmpty()) {
            // 尝试整体匹配
            String compact = raw.replaceAll("\\s+", "");
            compact = normalizeUnit(compact);
            compact = compact.replace("木个", "1个")
                            .replace("未个", "1个")
                            .replace("本个", "1个");
            
            // 先尝试匹配"商品名+数量+单位"
            Pattern pNameQtyUnit = Pattern.compile("^(.+?)(\\d+(?:\\.\\d+)?)(斤|个|把|包|听|板|袋|瓶|箱|件|捆|条|代)$");
            Matcher mNameQtyUnit = pNameQtyUnit.matcher(compact);
            if (mNameQtyUnit.find()) {
                name = mNameQtyUnit.group(1);
                qty = mNameQtyUnit.group(2);
                unit = mNameQtyUnit.group(3);
            } else {
                // 再尝试匹配"商品名+纯数字"（如 "白胡椒面29"）
                Pattern pNameQty = Pattern.compile("^(.+?)(\\d+(?:\\.\\d+)?)$");
                Matcher mNameQty = pNameQty.matcher(compact);
                if (mNameQty.find()) {
                    name = mNameQty.group(1);
                    String qtyRaw = mNameQty.group(2);
                    
                    // 修复两位数字：29->2个，17->1个，37->3个
                    String fixedQty = fixQtyAsGeIfNeeded(qtyRaw);
                    if (!fixedQty.equals(qtyRaw)) {
                        // 修复成功：如 "29" -> "2个"
                        qty = fixedQty.substring(0, 1); // "2个" -> "2"
                        unit = "个";
                    } else {
                        // 未修复：保持原样
                        qty = qtyRaw;
                        unit = "";
                    }
                } else {
                    // 完全匹配不到：整个 block 当作商品名
                    name = compact;
                    qty = "";
                    unit = "";
                }
            }
        }
        
        // 如果 name 为空但有 qty/unit，说明整个 block 被识别为数量单位（如 "32板"、"4斤"）
        // 按照用户要求：不要过滤，将整个 block 的原始文本作为商品名，但需要清理掉数量单位部分
        if ((name == null || name.isEmpty()) && (qty != null && !qty.isEmpty())) {
            // 将整个 block 的原始文本作为商品名，但需要去掉数量单位部分
            String rawCompact = raw.replaceAll("\\s+", "");
            // 如果 rawCompact 就是 qty+unit，说明这是孤立数量，保留整个文本作为商品名
            String qtyUnitStr = qty + unit;
            if (rawCompact.equals(qtyUnitStr)) {
                // 整个文本就是数量单位，保留作为商品名（如 "32板"）
                name = rawCompact;
            } else {
                // 去掉数量单位部分（如 "32板 4斤" -> "32板"）
                name = rawCompact.replace(qtyUnitStr, "").trim();
                if (name.isEmpty()) {
                    name = rawCompact;
                }
            }
        }

        // 如果 name 和 qty 都为空，将整个 block 的原始文本作为商品名
        if ((name == null || name.isEmpty()) && (qty == null || qty.isEmpty())) {
            name = raw.replaceAll("\\s+", "");
            qty = "";
            unit = "";
        }
        
        // 确保 name 不为空（至少要有原始文本）
        if (name == null || name.isEmpty()) {
            name = raw.replaceAll("\\s+", "");
        }

        ParsedLine pl = new ParsedLine();
        pl.rawLine = raw;
        pl.name = name;
        pl.qty = qty;
        pl.unit = unit;
        pl.note = note != null ? note : "";  // 使用提取到的 note，而不是硬编码为空字符串
        
        // 没数量就需要确认
        pl.needConfirm = (qty == null || qty.isEmpty());
        
        // 调试日志：打印解析结果（特别是备注字段）
        if (raw.contains("(空心)") || raw.contains("空心")) {
            logger.info("[blockToParsedLine] 圆白菜相关解析: raw={}, name={}, qty={}, unit={}, note={}, needConfirm={}", 
                raw, name, qty, unit, pl.note, pl.needConfirm);
        }

        return pl;
    }
    
    /**
     * 将一行 OCR 框解析为订单行（支持左右两列布局）
     * @deprecated 已替换为 splitRowIntoBlocks + blockToOrderLine，保留此方法用于兼容
     */
    @Deprecated
    private ParsedLine rowToOrderLine(List<OcrBox> row) {
        ParsedLine pl = new ParsedLine();
        if (row == null || row.isEmpty()) {
            return null;
        }

        // 把一行里所有文本拼个 rawLine
        StringBuilder raw = new StringBuilder();
        for (OcrBox b : row) {
            raw.append(b.text).append(" ");
        }
        pl.rawLine = raw.toString().trim();

        // 典型：两列（左品名，右数量）
        String left = normalizeUnit(row.get(0).text.trim());
        String right = row.size() >= 2 ? normalizeUnit(row.get(row.size() - 1).text.trim()) : "";

        pl.name = left;

        // 右侧是 "纯数字"
        if (isPureNumber(right)) {
            // 修复两位数字：29->2个，17->1个，37->3个（手写时"个"字和数字连在一起）
            String fixedQty = fixQtyAsGeIfNeeded(right);
            if (!fixedQty.equals(right)) {
                // 修复成功：如 "29" -> "2个"
                pl.qty = fixedQty.substring(0, 1); // "2个" -> "2"
                pl.unit = "个";
                pl.note = "";
                pl.needConfirm = false;
            } else {
                // 未修复：保持原样
                pl.qty = right;
                pl.unit = "";
                pl.note = "";
                pl.needConfirm = true; // 没单位，必须确认
            }
            return pl;
        }

        // 右侧是 "数字+单位"
        if (isQtyWithUnit(right)) {
            // 拆 qty + unit
            String normalizedRight = normalizeUnit(right);
            pl.qty = normalizedRight.replaceAll("(斤|个|把|袋|包|卷|箱|桶|件|盒)$", "");
            pl.unit = normalizedRight.replaceAll("^\\d+(?:\\.\\d+)?", "");
            pl.note = "";
            pl.needConfirm = false;
            return pl;
        }

        // 如果右侧不是数量，说明这一行可能"写在一起"：例如 白胡椒面29
        // 尝试从左/整行里提取尾部数字
        String merged = pl.rawLine.replace(" ", "");
        merged = normalizeUnit(merged);
        // 例：白胡椒面29
        if (merged.matches("^.+\\d+(?:\\.\\d+)?$")) {
            pl.name = merged.replaceAll("\\d+(?:\\.\\d+)?$", "");
            String qtyMatch = merged.replaceAll("^.+?(\\d+(?:\\.\\d+)?)$", "$1");
            if (qtyMatch.matches("^\\d+(?:\\.\\d+)?$")) {
                // 修复两位数字：29->2个，17->1个，37->3个
                String fixedQty = fixQtyAsGeIfNeeded(qtyMatch);
                if (!fixedQty.equals(qtyMatch)) {
                    // 修复成功：如 "29" -> "2个"
                    pl.qty = fixedQty.substring(0, 1); // "2个" -> "2"
                    pl.unit = "个";
                    pl.note = "";
                    pl.needConfirm = false;
                } else {
                    // 未修复：保持原样
                    pl.qty = qtyMatch;
                    pl.unit = "";
                    pl.note = "";
                    pl.needConfirm = true;
                }
                return pl;
            }
        }

        // 兜底：只有名字，没有数量
        // 注意：不要默认设置 qty="1"，让前端或用户确认
        pl.qty = "";
        pl.unit = "";
        pl.note = "";
        pl.needConfirm = true;
        return pl;
    }
    
    /**
     * 修复两位数字的数量识别（手写时"个"字和数字连在一起）
     * 29 -> 2个，17 -> 1个，37 -> 3个
     * 
     * @param qtyRaw 原始数量字符串
     * @return 修复后的字符串（如 "2个"），如果不需要修复则返回原字符串
     */
    private String fixQtyAsGeIfNeeded(String qtyRaw) {
        if (qtyRaw == null || qtyRaw.trim().isEmpty()) {
            return qtyRaw;
        }
        String s = qtyRaw.trim();
        
        // 只处理两位纯数字
        if (!s.matches("^\\d{2}$")) {
            return s;
        }
        
        char last = s.charAt(1);
        // 只处理个位是 7 或 9 的情况（手写时容易被识别成"个"）
        if (last != '7' && last != '9') {
            return s;
        }
        
        char tens = s.charAt(0);
        // 十位必须 1-9
        if (tens < '1' || tens > '9') {
            return s;
        }
        
        // 修复：如 "29" -> "2个"，"17" -> "1个"，"37" -> "3个"
        return tens + "个";
    }
    
    /**
     * 判断是否为商品名称文本（通常是中文，不包含数字和单位）
     */
    private boolean isGoodsNameText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        // 如果是纯数量 token，不是商品名
        if (isPureQuantityToken(text)) {
            return false;
        }
        
        // 商品名通常是中文，长度在2-10个字符之间
        String chinesePattern = ".*[\\u4e00-\\u9fa5].*";
        if (text.matches(chinesePattern) && text.length() >= 2 && text.length() <= 10) {
            return true;
        }
        
        return false;
    }
    

    
    /**
     * 从规格字符串中提取规格重量
     * 例如：桶(1.9L) -> 1.9L
     *      桶（5L） -> 5L
     * 
     * @param spec 规格字符串
     * @return 规格重量，如果未找到则返回空字符串
     */
    private String extractStandardWeightFromSpec(String spec) {
        if (spec == null || spec.trim().isEmpty()) {
            return "";
        }
        
        // 提取括号中的内容（包括中英文括号）
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[\\(（]([^）\\)]*)[）\\)]");
        java.util.regex.Matcher matcher = pattern.matcher(spec);
        
        if (matcher.find()) {
            String weight = matcher.group(1).trim();
            // 检查是否包含重量相关的单位（L、ml、kg、g等）
            if (weight.matches(".*\\d+.*(L|ml|kg|g|克|升|毫升|千克|公斤).*")) {
                return weight;
            }
        }
        
        return "";
    }
    
    /**
     * 调用 DeepSeek API 解析 Excel 表格，返回列名映射和结构化数据
     * 
     * @param excelText Excel 表格的文本内容
     * @return DeepSeek返回的JSON字符串，包含列名映射和数据
     * @throws IOException
     */
    private String callDeepSeekAPIForExcelOrder(String excelText) throws IOException {
        // 创建带超时设置的 HTTP 客户端
        org.apache.http.client.config.RequestConfig requestConfig = org.apache.http.client.config.RequestConfig.custom()
                .setConnectTimeout(30000)      // 连接超时 30 秒
                .setSocketTimeout(120000)       // Socket 超时 120 秒
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
        
        // 构建系统提示词
        StringBuilder systemPrompt = new StringBuilder();
        systemPrompt.append("请帮我从上传的 Excel 表格（CSV 格式）中提取特定的订单信息。这个 Excel 表可能有多余的列，但我只需要以下这些字段：商品名称、订货数量、订货规格、备注，以及订货规格中的重量（例如 1.9 升、5 升等）。\n\n");
        systemPrompt.append("请按照以下要求来提取数据：\n\n");
        systemPrompt.append("1. 输入是 CSV 格式的表格数据，列之间用逗号分隔。第一行通常是表头。列的位置从左到右依次是 A、B、C、D...\n");
        systemPrompt.append("2. 找出哪一列（A、B、C...）对应商品名称，哪一列对应订货数量，哪一列对应订货规格，哪一列对应备注。\n");
        systemPrompt.append("3. 在订货规格中，如果存在重量信息（比如 1.9 升、5 升等），请将这部分识别为规格重量。\n");
        systemPrompt.append("4. 返回时，告诉我每个字段对应的列名（A、B、C 等），并提取这些列的值。\n\n");
        systemPrompt.append("请严格按照以下 JSON 格式返回结果：\n");
        systemPrompt.append("{\n");
        systemPrompt.append("  \"columnMapping\": {\n");
        systemPrompt.append("    \"商品名称\": \"A\",\n");
        systemPrompt.append("    \"订货数量\": \"B\",\n");
        systemPrompt.append("    \"订货规格\": \"C\",\n");
        systemPrompt.append("    \"备注\": \"D\"\n");
        systemPrompt.append("  },\n");
        systemPrompt.append("  \"data\": [\n");
        systemPrompt.append("    {\n");
        systemPrompt.append("      \"name\": \"商品名称\",\n");
        systemPrompt.append("      \"quantity\": \"数量\",\n");
        systemPrompt.append("      \"spec\": \"规格（如：桶、斤、袋等）\",\n");
        systemPrompt.append("      \"standardWeight\": \"规格重量（如：1.9L、5L等，如果没有则为空字符串）\",\n");
        systemPrompt.append("      \"note\": \"备注\"\n");
        systemPrompt.append("    }\n");
        systemPrompt.append("  ]\n");
        systemPrompt.append("}\n\n");
        systemPrompt.append("重要说明：\n");
        systemPrompt.append("- columnMapping 中的值应该是 Excel 列名（如 A、B、C 等）\n");
        systemPrompt.append("- data 数组中的每个对象代表一行数据\n");
        systemPrompt.append("- standardWeight 字段：如果规格中包含重量信息（如\"桶(1.9L)\"），则提取重量部分（\"1.9L\"），如果没有重量信息则为空字符串\n");
        systemPrompt.append("- spec 字段：只提取规格单位（如\"桶\"、\"斤\"、\"袋\"等），不包含重量信息\n");
        systemPrompt.append("- 只输出 JSON，不要输出任何其他文字或解释\n");
        
        // 添加系统消息
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt.toString());
        messages.put(systemMessage);
        
        // 添加用户消息
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", "下面是 Excel 表格内容：\n<<<\n" + excelText + "\n>>>");
        messages.put(userMessage);
        
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.2);

        // 设置请求头
        StringEntity entity = new StringEntity(requestBody.toString(), "UTF-8");
        entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + deepSeekApiKey);

        logger.info("[DeepSeek ExcelOrder] 准备发送请求到 DeepSeek API");

        // 执行请求
        CloseableHttpResponse response = null;
        try {
            response = client.execute(httpPost);
            
            if (response == null) {
                throw new IOException("DeepSeek API 返回空响应");
            }
            
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                String errorContent = EntityUtils.toString(response.getEntity(), "UTF-8");
                throw new IOException("DeepSeek API 返回错误状态码: " + statusCode + ", 响应: " + errorContent);
            }
            
            HttpEntity responseEntity = response.getEntity();
            String responseContent = EntityUtils.toString(responseEntity, "UTF-8");
            logger.info("[DeepSeek ExcelOrder] 响应内容: {}", responseContent);

            // 解析响应
            JSONObject responseJson = new JSONObject(responseContent);
            
            if (responseJson.has("error")) {
                JSONObject errorObj = responseJson.getJSONObject("error");
                String errorMessage = errorObj.optString("message", "未知错误");
                throw new IOException("DeepSeek API 返回错误: " + errorMessage);
            }
            
            JSONArray choices = responseJson.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                String content = message.getString("content").trim();
                
                // 清洗响应内容（去除 markdown 代码块标记等）
                // 注意：这里返回的是 JSON 对象，不是数组，所以需要清理但保留对象格式
                logger.debug("[DeepSeek ExcelOrder] 清洗前的内容: {}", content);
                content = cleanDeepSeekJsonResponse(content);
                logger.debug("[DeepSeek ExcelOrder] 清洗后的内容: {}", content);
                
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
        } catch (Exception e) {
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
            throw new IOException("DeepSeek API 调用失败: " + e.getMessage(), e);
        }
    }
}

