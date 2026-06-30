package com.nongxinle.controller;

import com.nongxinle.dto.DistributerGoodsCandidateDTO;
import com.nongxinle.dto.NxDepartmentOrdersSimpleDTO;
import com.nongxinle.dto.NxGoodsCandidateDTO;
import com.nongxinle.dto.PasteSearchGoodsResponseDTO;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.R;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.ShiroUtils;
import com.alibaba.fastjson.JSON;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.ocr.v20181119.OcrClient;
import com.tencentcloudapi.ocr.v20181119.models.GeneralAccurateOCRRequest;
import com.tencentcloudapi.ocr.v20181119.models.GeneralAccurateOCRResponse;
import com.tencentcloudapi.ocr.v20181119.models.TextDetection;
import com.tencentcloudapi.ocr.v20181119.models.Coord;
import com.tencentcloudapi.tts.v20190823.TtsClient;
import com.tencentcloudapi.tts.v20190823.models.TextToVoiceRequest;
import com.tencentcloudapi.tts.v20190823.models.TextToVoiceResponse;
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
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.getTimeStamp;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDepOrderBuyStatusUnPurchase;
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

    /**
     * 与表 nx_department_orders 中 nx_DO_goods_name / nx_DO_goods_original_name 列长度一致（按常见库表为 varchar(200)，若实际更短请改小或 ALTER 扩列）。
     * OCR 可能解析出整行超长文本，截断避免 MysqlDataTruncation。
     */
    private static final int NX_DO_GOODS_NAME_DB_MAX_CHARS = 200;

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
    
    @Value("${external.images.path:file:///opt/tomcat/latest/app-data/images/}")
    private String externalImagesPath;

    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;

    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    
    @Autowired
    private com.nongxinle.dao.NxDepartmentOrdersDao nxDepartmentOrdersDao;

    @Autowired
    private NxDistributerGoodsShelfStockService nxDistributerGoodsShelfStockService;

    @Autowired
    private NxDistributerFatherGoodsService nxDistributerFatherGoodsService;

    @Autowired
    private NxDepartmentService nxDepartmentService;

    @Autowired
    private NxDepartmentDisGoodsService nxDepartmentDisGoodsService;
    @Autowired
    private com.nongxinle.service.NxOrderOcrTrainingDataService nxOrderOcrTrainingDataService;

    @Autowired
    private com.nongxinle.service.NxPromptService nxPromptService;
    
    @Autowired
    private NxOcrTaskService nxOcrTaskService;
    @Autowired
    private NxGoodsService nxGoodsService;
    @Autowired
    private  NxDistributerUserService nxDistributerUserService;
    @Autowired
    private NxDistributerNxDistributerService nxDistributerNxDistributerService;
    
    @Autowired
    private OcrClient ocrClient; // 注入单例 OCR 客户端
    
    // 线程池，用于异步处理订单识别
    // 优化：增加线程池大小，添加队列限制和拒绝策略，防止任务堆积导致服务器瘫痪
    private final ExecutorService executorService = new ThreadPoolExecutor(
            10,                          // 核心线程数：10
            20,                          // 最大线程数：20
            60L,                         // 空闲线程存活时间：60秒
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(50), // 任务队列：最多50个任务
            new ThreadFactory() {
                private int counter = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "OCR-Async-" + (++counter));
                    t.setDaemon(false);
                    return t;
                }
            },
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    logger.error("[recognizeOrderAsync] 线程池已满，任务被拒绝！当前活跃线程数: {}, 队列大小: {}, 已完成任务数: {}", 
                            executor.getActiveCount(), executor.getQueue().size(), executor.getCompletedTaskCount());
                    // 尝试获取任务ID并更新状态为失败
                    try {
                        // 这里无法直接获取任务ID，所以只记录日志
                        logger.error("[recognizeOrderAsync] 请检查服务器负载，考虑增加线程池大小或优化处理逻辑");
                    } catch (Exception e) {
                        logger.error("[recognizeOrderAsync] 处理拒绝任务时出错", e);
                    }
                }
            }
    );

    /**
     * 关闭线程池（应用停止时调用）
     * 优雅关闭：等待正在执行的任务完成，但最多等待30秒
     */
    public void shutdownThreadPool() {
        if (executorService != null && !executorService.isShutdown()) {
            logger.info("[OcrController] 开始关闭线程池...");
            executorService.shutdown(); // 不再接受新任务，等待现有任务完成
            
            try {
                // 等待正在执行的任务完成，最多等待30秒
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    logger.warn("[OcrController] 线程池在30秒内未完全关闭，强制关闭...");
                    executorService.shutdownNow(); // 强制关闭
                    
                    // 再等待5秒
                    if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                        logger.error("[OcrController] 线程池未能正常关闭");
                    } else {
                        logger.info("[OcrController] 线程池已强制关闭");
                    }
                } else {
                    logger.info("[OcrController] 线程池已优雅关闭");
                }
            } catch (InterruptedException e) {
                logger.error("[OcrController] 等待线程池关闭时被中断", e);
                executorService.shutdownNow();
                Thread.currentThread().interrupt(); // 恢复中断状态
            }
        }
    }

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
     * @param goodsName 商品名称（原始名称 rawName）
     * @param deepseekRecommendedName DeepSeek 推荐的商品名称（纠错后的名称 name，可为 null）
     * @param quantity 数量
     * @param spec 规格
     * @param standardWeight 规格重量
     * @param note 备注
     * @param userId 用户ID
     * @return 训练数据实体
     */
    private NxOrderOcrTrainingDataEntity createOrQueryTrainingData(
            Integer depId, Integer depFatherId, Integer disId,
            String goodsName, String deepseekRecommendedName, String quantity, String spec, String standardWeight, String note, String originalText, Integer userId) {
        // 查询是否已有训练数据（使用传入的 goodsName，归一化去空格以便匹配「西红柿 15斤」与「西红柿15斤」）
        Map<String, Object> matchParams = new HashMap<>();
        matchParams.put("depId", depId);
        String goodsNameNorm = (goodsName != null ? goodsName.trim().replaceAll("\\s+", "") : "");
        matchParams.put("goodsName", goodsNameNorm.isEmpty() ? goodsName : goodsNameNorm);
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
        // 存归一化后的商品名，便于与「西红柿 15斤」「西红柿15斤」等不同空格格式互匹配
        trainingData.setNxOtdOriginalGoodsName(goodsNameNorm.isEmpty() ? goodsName : goodsNameNorm);
        // 设置 DeepSeek 推荐的商品名称
        trainingData.setNxOtdDeepseekRecommendedName(deepseekRecommendedName);
        trainingData.setNxOtdOriginalQuantity(quantity);
        trainingData.setNxOtdOriginalStandard(spec);
        trainingData.setNxOtdOriginalStandardWeight(standardWeight);
        trainingData.setNxOtdOriginalRemark(note);
        // 保存OCR原始文本（已在提取时去除所有空格）
        trainingData.setNxOtdOcrText(originalText != null ? originalText : "");
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
     * 将订单实体转换为响应DTO（带包装结构字段）
     * 
     * @param order 订单实体
     * @param note 备注（使用原始备注，而不是订单中的备注）
     * @param standardWeight 规格重量
     * @param itemUnit 最小包装单位
     * @param itemsPerCarton 每个大包装内的小包装数量
     * @param cartonUnit 大包装单位
     * @return 响应DTO
     */
    private PasteSearchGoodsResponseDTO convertOrderToResponseDTO(
            NxDepartmentOrdersEntity order, 
            String note,
            String standardWeight,
            String itemUnit,
            String itemsPerCarton,
            String cartonUnit) {
        PasteSearchGoodsResponseDTO responseDTO = new PasteSearchGoodsResponseDTO();
        responseDTO.setNxDepartmentOrdersId(order.getNxDepartmentOrdersId());
        responseDTO.setNxDoGoodsName(order.getNxDoGoodsName());
        responseDTO.setNxDoGoodsNameOriginal(order.getNxDoGoodsName());
        responseDTO.setNxDoQuantity(order.getNxDoQuantity());
        responseDTO.setNxDoStandard(order.getNxDoStandard() != null && !order.getNxDoStandard().trim().isEmpty()
                ? order.getNxDoStandard() : "个");
        responseDTO.setNxDoRemark(note);
        responseDTO.setNxDoAddRemark(note != null && !note.trim().isEmpty());
        responseDTO.setNxDoStatus(order.getNxDoStatus());
        responseDTO.setNxDoDepartmentId(order.getNxDoDepartmentId());
        responseDTO.setNxDoPurchaseStatus(order.getNxDoPurchaseStatus());
        responseDTO.setNxDoDepartmentFatherId(order.getNxDoDepartmentFatherId());
        responseDTO.setNxDoDisGoodsId(order.getNxDoDisGoodsId());
        responseDTO.setNxDoDistributerId(order.getNxDoDistributerId());
        responseDTO.setNxDoOrderUserId(order.getNxDoOrderUserId());
        responseDTO.setNxDoPurchaseUserId(order.getNxDoPurchaseUserId() != null ? order.getNxDoPurchaseUserId() : -1);
        responseDTO.setNxDoIsAgent(order.getNxDoIsAgent() != null ? order.getNxDoIsAgent() : -1);
        responseDTO.setNxDoTodayOrder(order.getNxDoTodayOrder());
        responseDTO.setNxDoTrainingDataId(order.getNxDoTrainingDataId());
        responseDTO.setNxDoOcrTaskId(order.getNxDoOcrTaskId());
        responseDTO.setNxDoGoodsType(order.getNxDoGoodsType());
        responseDTO.setNxDoStandardWarn(0);
        responseDTO.setGoodsNameWarn(0);
        responseDTO.setNxDoCollaborativeNxDisId(order.getNxDoCollaborativeNxDisId());
        responseDTO.setNxDoCollaborativeDistributerName(order.getNxDoCollaborativeDistributerName());

        // 非-2订单：设置已匹配的单个分销商商品，供前端判断协作配送商（wx:if="{{item.nxDistributerGoodsEntity.nxDgDistributerId !== disId}}"）
        if (order.getNxDoStatus() != null && order.getNxDoStatus() != -2
                && order.getNxDistributerGoodsEntity() != null) {
            NxDistributerGoodsEntity goods = order.getNxDistributerGoodsEntity();
            DistributerGoodsCandidateDTO singleDto = new DistributerGoodsCandidateDTO();
            singleDto.setNxDistributerGoodsId(goods.getNxDistributerGoodsId());
            singleDto.setNxDgGoodsName(goods.getNxDgGoodsName());
            singleDto.setNxDgGoodsStandardname(goods.getNxDgGoodsStandardname());
            singleDto.setNxDgGoodsStandardWeight(goods.getNxDgGoodsStandardWeight());
            singleDto.setNxDgCartonUnit(goods.getNxDgCartonUnit());
            singleDto.setNxDgGoodsBrand(goods.getNxDgGoodsBrand());
            singleDto.setNxDgGoodsFile(goods.getNxDgGoodsFile());
            singleDto.setNxDgNxGoodsId(goods.getNxDgNxGoodsId());
            singleDto.setNxDgDistributerId(goods.getNxDgDistributerId());
            singleDto.setNxDgDistributerName(goods.getNxDgDistributerName() != null
                    ? goods.getNxDgDistributerName() : goods.getGoodsNxDistributerName());
            if (goods.getDepartmentDisGoodsEntity() != null) {
                NxDepartmentDisGoodsEntity nddg = goods.getDepartmentDisGoodsEntity();
                DistributerGoodsCandidateDTO.DepartmentDisGoodsCandidateDTO depDto =
                        new DistributerGoodsCandidateDTO.DepartmentDisGoodsCandidateDTO();
                depDto.setNxDdgOrderDate(nddg.getNxDdgOrderDate());
                depDto.setNxDdgOrderPrice(nddg.getNxDdgOrderPrice());
                depDto.setNxDdgOrderQuantity(nddg.getNxDdgOrderQuantity());
                depDto.setNxDdgOrderStandard(nddg.getNxDdgOrderStandard());
                singleDto.setDepartmentDisGoodsEntity(depDto);
            }
            responseDTO.setNxDistributerGoodsEntity(singleDto);
        }
        
        // 设置包装结构字段（确保字段始终存在，即使为空字符串）
        logger.info("[convertOrderToResponseDTO] 设置包装结构字段: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}", 
                standardWeight, itemUnit, itemsPerCarton, cartonUnit);
        responseDTO.setStandardWeight(standardWeight != null ? standardWeight : "");
        responseDTO.setItemUnit(itemUnit != null ? itemUnit : "");
        responseDTO.setItemsPerCarton(itemsPerCarton != null ? itemsPerCarton : "");
        responseDTO.setCartonUnit(cartonUnit != null ? cartonUnit : "");
        logger.info("[convertOrderToResponseDTO] 设置后的DTO字段值: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}", 
                responseDTO.getStandardWeight(), responseDTO.getItemUnit(), responseDTO.getItemsPerCarton(), responseDTO.getCartonUnit());
        
        // 处理候选商品列表（推荐商品）
        // 分销商商品候选列表（优先显示）
        if (order.getNxDistributerGoodsEntityList() != null && !order.getNxDistributerGoodsEntityList().isEmpty()) {
            logger.info("[convertOrderToResponseDTO] 转换分销商商品候选列表，数量: {}", order.getNxDistributerGoodsEntityList().size());
            List<DistributerGoodsCandidateDTO> distributerGoodsList = new ArrayList<>();
            for (NxDistributerGoodsEntity goods : order.getNxDistributerGoodsEntityList()) {
                DistributerGoodsCandidateDTO candidateDTO = new DistributerGoodsCandidateDTO();
                candidateDTO.setNxDistributerGoodsId(goods.getNxDistributerGoodsId());
                candidateDTO.setNxDgGoodsName(goods.getNxDgGoodsName());
                candidateDTO.setNxDgGoodsStandardname(goods.getNxDgGoodsStandardname());
                candidateDTO.setNxDgGoodsStandardWeight(goods.getNxDgGoodsStandardWeight());
                candidateDTO.setNxDgCartonUnit(goods.getNxDgCartonUnit());
                candidateDTO.setNxDgGoodsBrand(goods.getNxDgGoodsBrand());
                candidateDTO.setNxDgGoodsFile(goods.getNxDgGoodsFile());
                candidateDTO.setNxDgNxGoodsId(goods.getNxDgNxGoodsId());
                candidateDTO.setNxDgDistributerId(goods.getNxDgDistributerId());
                candidateDTO.setNxDgDistributerName(goods.getNxDgDistributerName() != null ? goods.getNxDgDistributerName() : goods.getGoodsNxDistributerName());
                if (goods.getDepartmentDisGoodsEntity() != null) {
                    NxDepartmentDisGoodsEntity nddg = goods.getDepartmentDisGoodsEntity();
                    DistributerGoodsCandidateDTO.DepartmentDisGoodsCandidateDTO depDto = new DistributerGoodsCandidateDTO.DepartmentDisGoodsCandidateDTO();
                    depDto.setNxDdgOrderDate(nddg.getNxDdgOrderDate());
                    depDto.setNxDdgOrderPrice(nddg.getNxDdgOrderPrice());
                    depDto.setNxDdgOrderQuantity(nddg.getNxDdgOrderQuantity());
                    depDto.setNxDdgOrderStandard(nddg.getNxDdgOrderStandard());
                    candidateDTO.setDepartmentDisGoodsEntity(depDto);
                }
                distributerGoodsList.add(candidateDTO);
            }
            responseDTO.setNxDistributerGoodsEntityList(distributerGoodsList);
            logger.info("[convertOrderToResponseDTO] 分销商商品候选列表转换完成，返回数量: {}", distributerGoodsList.size());
        }
        
        // 系统商品候选列表（即使有分销商商品候选列表，也要显示系统商品，但已去除重复的）
        if (order.getNxGoodsEntities() != null && !order.getNxGoodsEntities().isEmpty()) {
            logger.info("[convertOrderToResponseDTO] 转换系统商品候选列表，数量: {}", order.getNxGoodsEntities().size());
            List<NxGoodsCandidateDTO> nxGoodsList = new ArrayList<>();
            for (NxGoodsEntity goods : order.getNxGoodsEntities()) {
                NxGoodsCandidateDTO candidateDTO = new NxGoodsCandidateDTO();
                candidateDTO.setNxGoodsId(goods.getNxGoodsId());
                candidateDTO.setNxGoodsName(goods.getNxGoodsName());
                candidateDTO.setNxGoodsStandardname(goods.getNxGoodsStandardname());
                candidateDTO.setNxGoodsStandardWeight(goods.getNxGoodsStandardWeight());
                candidateDTO.setNxGoodsCartonUnit(goods.getNxGoodsCartonUnit());
                candidateDTO.setNxGoodsBrand(goods.getNxGoodsBrand());
                candidateDTO.setNxGoodsFile(goods.getNxGoodsFile());
                candidateDTO.setNxGoodsFatherId(goods.getNxGoodsFatherId());
                nxGoodsList.add(candidateDTO);
            }
            responseDTO.setNxGoodsEntities(nxGoodsList);
            logger.info("[convertOrderToResponseDTO] 系统商品候选列表转换完成，返回数量: {}", nxGoodsList.size());
        }
        
        return responseDTO;
    }

    /**
     * 将订单实体转换为OCR任务订单简洁DTO（用于getTaskOrders接口，减少数据传输量）
     * 复用 PasteSearchGoodsResponseDTO，推荐商品使用 DistributerGoodsCandidateDTO / NxGoodsCandidateDTO
     *
     * @param order 订单实体
     * @return PasteSearchGoodsResponseDTO
     */
    private PasteSearchGoodsResponseDTO convertOrderToOcrTaskSimpleDTO(NxDepartmentOrdersEntity order) {
        return convertOrderToResponseDTO(
                order,
                order.getNxDoRemark(),
                order.getStandardWeight(),
                order.getItemUnit(),
                order.getItemsPerCarton(),
                order.getCartonUnit()
        );
    }

    /**
     * 根据数量和规格是否改变设置订单状态
     * 
     * @param orderBasic 订单实体
     * @param specIsChange 规格是否改变
     * @param quantityIsChange 数量是否改变
     */
    private void setOrderStatusByChangeFlag(NxDepartmentOrdersEntity orderBasic, Boolean specIsChange, Boolean quantityIsChange) {
        // 如果 specIsChange 或 quantityIsChange 改变了，订单状态是-2（待修正），否则是 0（已完成）
        if (specIsChange || quantityIsChange) {
            orderBasic.setNxDoStatus(-2);
            logger.info("[recognizeOrder] 数量或规格已改变，订单状态设置为 -2（待修正）: quantityIsChange={}, specIsChange={}", 
                    quantityIsChange, specIsChange);
        } else {
            orderBasic.setNxDoStatus(0);
            logger.info("[recognizeOrder] 数量或规格未改变，订单状态设置为 0（已完成）");
        }
    }

    /**
     * 判断字符串是否为数字（支持整数和小数）
     * 
     * @param str 待判断的字符串
     * @return 如果是数字返回 true，否则返回 false
     */
    private boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }



    private void saveStrongQueryOrderAndConvert(
            NxDepartmentOrdersEntity orderBasic,
            NxDistributerGoodsEntity distributerGoodsEntity,
            Integer originalIndex,
            Map<Integer, NxDepartmentOrdersEntity> responseMap

    ) {
        applyNxDepartmentOrderGoodsNameDbLimit(orderBasic);
        // 协作伙伴商品判别与保存已统一在 saveOrderWithGoods 中处理
        NxDepartmentOrdersEntity savedOrder = nxDepartmentOrdersService.saveOrderWithGoods(orderBasic, distributerGoodsEntity);
        // 确保保存后的订单实体包含包装结构字段和任务ID（从orderBasic中复制）
        savedOrder.setStandardWeight(orderBasic.getStandardWeight());
        savedOrder.setItemUnit(orderBasic.getItemUnit());
        savedOrder.setItemsPerCarton(orderBasic.getItemsPerCarton());
        savedOrder.setCartonUnit(orderBasic.getCartonUnit());
        // 确保任务ID被保留
        if (orderBasic.getNxDoOcrTaskId() != null) {
            savedOrder.setNxDoOcrTaskId(orderBasic.getNxDoOcrTaskId());
            // 如果保存后的订单任务ID丢失，需要更新
            if (savedOrder.getNxDoOcrTaskId() == null) {
                nxDepartmentOrdersService.update(savedOrder);
            }
        }
        
        // 如果订单状态为 -2，需要添加推荐商品
        if (savedOrder.getNxDoStatus() != null && savedOrder.getNxDoStatus() == -2) {
            logger.info("[saveStrongQueryOrderAndConvert] 订单状态为-2，添加推荐商品: 订单ID={}, 商品ID={}", 
                    savedOrder.getNxDepartmentOrdersId(), savedOrder.getNxDoDisGoodsId());
            savedOrder = nxDepartmentOrdersService.addCommentsGoodsForOrder(savedOrder);
            // 确保推荐商品添加后，包装结构字段和任务ID仍然存在
            savedOrder.setStandardWeight(orderBasic.getStandardWeight());
            savedOrder.setItemUnit(orderBasic.getItemUnit());
            savedOrder.setItemsPerCarton(orderBasic.getItemsPerCarton());
            savedOrder.setCartonUnit(orderBasic.getCartonUnit());
            if (orderBasic.getNxDoOcrTaskId() != null) {
                savedOrder.setNxDoOcrTaskId(orderBasic.getNxDoOcrTaskId());
            }
            logger.info("[saveStrongQueryOrderAndConvert] 推荐商品添加完成: 订单ID={}, 推荐商品数量={}", 
                    savedOrder.getNxDepartmentOrdersId(),
                    savedOrder.getNxDistributerGoodsEntityList() != null ? savedOrder.getNxDistributerGoodsEntityList().size() : 0);
        }
        
        // 直接返回订单实体（订单实体已包含所有DTO字段）
        responseMap.put(originalIndex, savedOrder);
        logger.info("[saveStrongQueryOrderAndConvert] 保存到responseMap[{}]完成，订单实体字段值: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}, ocrTaskId={}", 
                originalIndex, savedOrder.getStandardWeight(), savedOrder.getItemUnit(), savedOrder.getItemsPerCarton(), savedOrder.getCartonUnit(), savedOrder.getNxDoOcrTaskId());
    }


    private void saveWeakQueryOrderWithTrainingData(
            NxDepartmentOrdersEntity orderBasic,
            NxDistributerGoodsEntity distributerGoodsEntity,
            NxOrderOcrTrainingDataEntity trainingData,
            String note,
            Integer originalIndex,
            Map<Integer, NxDepartmentOrdersEntity> responseMap,
            String standardWeight,
            String itemUnit,
            String itemsPerCarton,
            String cartonUnit) {
        // 弱查询找到商品，订单状态统一为 -2（不更新训练数据，等待人工确认）
        orderBasic.setNxDoStatus(-2);
        applyNxDepartmentOrderGoodsNameDbLimit(orderBasic);
        
        NxDepartmentOrdersEntity savedOrder = nxDepartmentOrdersService.saveOrderWithGoods(orderBasic, distributerGoodsEntity);
        // 确保保存后的订单实体包含包装结构字段和任务ID（从orderBasic中复制）
        savedOrder.setStandardWeight(orderBasic.getStandardWeight());
        savedOrder.setItemUnit(orderBasic.getItemUnit());
        savedOrder.setItemsPerCarton(orderBasic.getItemsPerCarton());
        savedOrder.setCartonUnit(orderBasic.getCartonUnit());
        // 确保任务ID被保留
        if (orderBasic.getNxDoOcrTaskId() != null) {
            savedOrder.setNxDoOcrTaskId(orderBasic.getNxDoOcrTaskId());
        }
        // 关联训练数据ID
        savedOrder.setNxDoTrainingDataId(trainingData.getNxOtdId());
        nxDepartmentOrdersService.update(savedOrder);
        
        // 更新训练数据的订单ID（保存订单后，训练数据可以关联到订单）
        if (savedOrder.getNxDepartmentOrdersId() != null && trainingData.getNxOtdOrderId() == null) {
            trainingData.setNxOtdOrderId(savedOrder.getNxDepartmentOrdersId());
            nxOrderOcrTrainingDataService.update(trainingData);
            logger.info("[saveWeakQueryOrderWithTrainingData] 已更新训练数据的订单ID: 训练数据ID={}, 订单ID={}", 
                    trainingData.getNxOtdId(), savedOrder.getNxDepartmentOrdersId());
        }

        // 给订单添加推荐商品（参考 pasteSearchGoods 的查询方式，不改变订单状态）
        NxDepartmentOrdersEntity lastOrder = nxDepartmentOrdersService.addCommentsGoodsForOrder(savedOrder);
        // 确保返回的订单实体包含包装结构字段和任务ID
        lastOrder.setStandardWeight(orderBasic.getStandardWeight());
        lastOrder.setItemUnit(orderBasic.getItemUnit());
        lastOrder.setItemsPerCarton(orderBasic.getItemsPerCarton());
        lastOrder.setCartonUnit(orderBasic.getCartonUnit());
        if (orderBasic.getNxDoOcrTaskId() != null) {
            lastOrder.setNxDoOcrTaskId(orderBasic.getNxDoOcrTaskId());
        }

        // 直接返回订单实体（订单实体已包含所有DTO字段）
        responseMap.put(originalIndex, lastOrder);
    }



    @RequestMapping(value = "/correctOrder", method = RequestMethod.POST)
    @ResponseBody
    public R correctOrder (@RequestBody NxDepartmentOrdersEntity  ordersEntity) {


        Integer nxDoStatus = ordersEntity.getNxDoStatus();

        Integer disId = ordersEntity.getNxDoDistributerId();
        Integer depId = ordersEntity.getNxDoDepartmentId();
        String goodsName = ordersEntity.getNxDoGoodsName();
        String spec = ordersEntity.getNxDoStandard();
         Boolean findGoods = false;
        // 1级优先匹配：首先查询部门商品历史记录，如果订货商品名称和规格完全匹配，则直接推断为该商品
        Map<String, Object> depGoodsMap = new HashMap<>();
        depGoodsMap.put("disId", disId);
        depGoodsMap.put("depId", depId);
        depGoodsMap.put("name", goodsName);
        depGoodsMap.put("standard", spec);
        List<NxDepartmentDisGoodsEntity> departmentDisGoodsList = nxDepartmentDisGoodsService.queryDepartmentGoods(depGoodsMap);
        logger.info("[correctOrder] 1级优先匹配-部门商品历史记录查询结果数量: {}", departmentDisGoodsList.size());
        // 如果部门商品历史记录中有完全匹配的（名称+规格），则直接使用该商品
        if (departmentDisGoodsList.size() == 1) {
            NxDepartmentDisGoodsEntity departmentDisGoodsEntity = departmentDisGoodsList.get(0);
            // 复制粘贴业务没有数量/规格改变的概念，订单状态统一设为0（已完成）
            ordersEntity.setNxDoStatus(0);
            ordersEntity.setNxDoDisGoodsId(departmentDisGoodsEntity.getNxDdgDisGoodsId());
            findGoods = true;
        }

        // 2级优先匹配：商品库，如果订货商品名称和规格完全匹配，则直接推断为该商品
            Map<String, Object> mapZero = new HashMap<>();
            mapZero.put("disId", disId);
            mapZero.put("searchStr", goodsName);
            mapZero.put("standard", spec);
            logger.info("[correctOrder] 2级优先匹配-商品名称+规格查询参数: {}", mapZero);
            List<NxDistributerGoodsEntity> distributerGoodsEntitiesZero = nxDistributerGoodsService.queryDisGoodsByName(mapZero);
            // 如果商品历史记录中有完全匹配的（名称+规格），则直接使用该商品
            if (distributerGoodsEntitiesZero.size() == 1) {
                NxDistributerGoodsEntity distributerGoodsEntity = distributerGoodsEntitiesZero.get(0);
                logger.info("[correctOrder] 2级优先匹配：商品库，直接保存订单，disGoodsId={}",
                        distributerGoodsEntity.getNxDistributerGoodsId());

                // 复制粘贴业务没有数量/规格改变的概念，订单状态统一设为0（已完成）
                ordersEntity.setNxDoStatus(0);
                ordersEntity.setNxDoDisGoodsId(distributerGoodsEntity.getNxDistributerGoodsId());
                findGoods = true;

            }


        // 3级优先匹配：商品库别名精准查询，如果订货商品名称和规格完全匹配，则直接推断为该商品
            Map<String, Object> mapA = new HashMap<>();
            mapA.put("disId", disId);
            mapA.put("alias", goodsName);
            mapA.put("standard", spec);
            mapA.put("depId", depId);
            List<NxDistributerGoodsEntity> distributerGoodsEntitiesAlias = nxDistributerGoodsService.queryDisGoodsByAlias(mapA);
            // 如果商品历史记录中有完全匹配的（名称+规格），则直接使用该商品
            if (distributerGoodsEntitiesAlias.size() == 1) {
                NxDistributerGoodsEntity distributerGoodsEntity = distributerGoodsEntitiesAlias.get(0);
                logger.info("[correctOrder] 3级优先匹配-商品库别名精准查询，直接保存订单，disGoodsId={}",
                        distributerGoodsEntity.getNxDistributerGoodsId());
                // 复制粘贴业务没有数量/规格改变的概念，订单状态统一设为0（已完成）
                ordersEntity.setNxDoStatus(0);
                ordersEntity.setNxDoDisGoodsId(distributerGoodsEntity.getNxDistributerGoodsId());
            }


        // 4级优先匹配：查询训练表中是否有相同内容（匹配：部门ID + 商品名称）
            Map<String, Object> matchParams = new HashMap<>();
            matchParams.put("depId", depId);
            matchParams.put("goodsName", goodsName);
            matchParams.put("disGoodsId", 1);
            NxOrderOcrTrainingDataEntity matchedTrainingData = nxOrderOcrTrainingDataService.queryByMatchFields(matchParams);

            if (matchedTrainingData != null && matchedTrainingData.getNxOtdDisGoodsId() != null) {
                // 训练数据中有商品ID，直接创建订单
                logger.info("[correctOrder] 4级优先匹配-找到匹配的训练数据，商品ID: {}, 直接创建订单",
                        matchedTrainingData.getNxOtdDisGoodsId());

                // 优先使用训练数据的 final_goods_name（如果存在），否则使用原始商品名称
                String finalGoodsName = matchedTrainingData.getNxOtdFinalGoodsName();
                String orderGoodsName = (finalGoodsName != null && !finalGoodsName.trim().isEmpty())
                        ? finalGoodsName
                        : goodsName;
                ordersEntity.setNxDoGoodsName(orderGoodsName);

                // 复制粘贴业务没有数量/规格改变的概念，订单状态统一设为0（已完成）
                ordersEntity.setNxDoStatus(0);
                ordersEntity.setNxDoDisGoodsId(matchedTrainingData.getNxOtdDisGoodsId());
                findGoods = true;

        }

            if(findGoods){

                if (ordersEntity.getNxDoTrainingDataId() != null) {
                    NxOrderOcrTrainingDataEntity trainingData = nxOrderOcrTrainingDataService.queryObject(ordersEntity.getNxDoTrainingDataId());

                    if (trainingData != null) {
                        // 手动标注标志设为 2（自动识别是 1，手动识别是 2）
                        trainingData.setNxOtdDisGoodsId(ordersEntity.getNxDoDisGoodsId());
                        trainingData.setNxOtdOrderId(ordersEntity.getNxDepartmentOrdersId());

                        // 填充最终确认字段（手动标注）
                        trainingData.setNxOtdFinalGoodsName(ordersEntity.getNxDoGoodsName());
                        trainingData.setNxOtdFinalQuantity(ordersEntity.getNxDoQuantity());
                        trainingData.setNxOtdFinalStandard(ordersEntity.getNxDoStandard());
                        trainingData.setNxOtdFinalRemark(ordersEntity.getNxDoRemark() != null ? ordersEntity.getNxDoRemark() : "");

                        // 手动标注标志设为 2
                        trainingData.setNxOtdIsNameManuallyAnnotated(2);
                        trainingData.setNxOtdIsQuantityManuallyAnnotated(2);
                        trainingData.setNxOtdIsStandardManuallyAnnotated(2);
                        trainingData.setNxOtdIsStandardWeightManuallyAnnotated(2);
                        trainingData.setNxOtdIsRemarkManuallyAnnotated(2);

                        // 更新标准重量（如果有的话，从原始数据中获取）
                        if (trainingData.getNxOtdOriginalStandardWeight() != null) {
                            trainingData.setNxOtdFinalStandardWeight(trainingData.getNxOtdOriginalStandardWeight());
                        }

                        trainingData.setNxOtdUpdateDate(formatWhatYearDayTime(0));

                        // 更新训练数据
                        nxOrderOcrTrainingDataService.update(trainingData);

                        logger.info("[correctOrder] 训练数据已更新（手动标注），训练数据ID: {}, 订单ID: {}, 商品ID: {}",
                                trainingData.getNxOtdId(), ordersEntity.getNxDepartmentOrdersId(), ordersEntity.getNxDoDisGoodsId());
                    }
                }

                Integer nxDoOcrTaskId = ordersEntity.getNxDoOcrTaskId();
                NxOcrTaskEntity nxOcrTaskEntity = nxOcrTaskService.queryObject(nxDoOcrTaskId);
                if(nxOcrTaskEntity != null && nxDoStatus == -2){
                    nxOcrTaskEntity.setNxOcrTaskCompletedOrders(nxOcrTaskEntity.getNxOcrTaskCompletedOrders() + 1);
                    nxOcrTaskEntity.setNxOcrTaskPendingOrders(nxOcrTaskEntity.getNxOcrTaskPendingOrders() - 1);
                    int newPendingOrders = nxOcrTaskEntity.getNxOcrTaskPendingOrders();
                    logger.info("[correctOrder] OCR任务更新后 - 已完成订单: {}, 待修正订单: {}",
                            nxOcrTaskEntity.getNxOcrTaskCompletedOrders(), newPendingOrders);
                    // 如果待修正订单数为0，设置任务状态为已完成（状态1）
                    if(newPendingOrders == 0){
                        nxOcrTaskEntity.setNxOcrTaskStatus(2);
                    }
                    nxOcrTaskService.update(nxOcrTaskEntity);
                }

                Integer nxDoDisGoodsId = ordersEntity.getNxDoDisGoodsId();
                NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDoDisGoodsId);
                nxDepartmentOrdersService.updateOneOrderForChoice(ordersEntity,nxDistributerGoodsEntity);
                return R.ok().put("data",ordersEntity);
            }else{
                ordersEntity.setNxDoStatus(-2);
                nxDepartmentOrdersService.addCommentsGoodsForOrder(ordersEntity);
                nxDepartmentOrdersService.update(ordersEntity);

                Integer nxDoOcrTaskId = ordersEntity.getNxDoOcrTaskId();
                NxOcrTaskEntity nxOcrTaskEntity = nxOcrTaskService.queryObject(nxDoOcrTaskId);
                if(nxOcrTaskEntity != null && nxDoStatus == 0){
                    nxOcrTaskEntity.setNxOcrTaskCompletedOrders(nxOcrTaskEntity.getNxOcrTaskCompletedOrders() - 1);
                    nxOcrTaskEntity.setNxOcrTaskPendingOrders(nxOcrTaskEntity.getNxOcrTaskPendingOrders() + 1);
                    int newPendingOrders = nxOcrTaskEntity.getNxOcrTaskPendingOrders();
                    logger.info("[correctOrder] OCR任务更新后 - 已完成订单: {}, 待修正订单: {}",
                            nxOcrTaskEntity.getNxOcrTaskCompletedOrders(), newPendingOrders);
                    // 如果待修正订单数为0，设置任务状态为已完成（状态1）
                    if(newPendingOrders == 1){
                        nxOcrTaskEntity.setNxOcrTaskStatus(1);
                    }
                    nxOcrTaskService.update(nxOcrTaskEntity);
                }
                return R.ok().put("data",ordersEntity);
            }
    }
    
//
@RequestMapping(value = "/pasteSearchGoods", method = RequestMethod.POST)
@ResponseBody
public R pasteSearchGoods(@RequestBody Map<String, Object> request) {
    Integer ocrTaskId = null;
    try {
        // 从请求中提取订单列表和粘贴文字内容
        List<NxDepartmentOrdersEntity> orderList = null;
        Object orderListObj = request.get("orderList");
        if (orderListObj != null) {
            // 如果是字符串，先解析为JSON
            if (orderListObj instanceof String) {
                orderList = JSON.parseArray((String) orderListObj, NxDepartmentOrdersEntity.class);
            } else if (orderListObj instanceof List) {
                // 如果是List，需要转换为JSON字符串再解析，确保类型正确
                String orderListJson = JSON.toJSONString(orderListObj);
                orderList = JSON.parseArray(orderListJson, NxDepartmentOrdersEntity.class);
            } else {
                // 其他类型，尝试直接转换
                String orderListJson = JSON.toJSONString(orderListObj);
                orderList = JSON.parseArray(orderListJson, NxDepartmentOrdersEntity.class);
            }
        }
        String pasteText = request.get("pasteText") != null ? request.get("pasteText").toString() : null;

        if (orderList == null || orderList.isEmpty()) {
            logger.warn("[pasteSearchGoods] 订单列表为空");
            return R.error("订单列表不能为空");
        }

        logger.info("[pasteSearchGoods] 开始处理订单列表，订单数量: {}, 粘贴文字内容长度: {}",
                orderList.size(), pasteText != null ? pasteText.length() : 0);

        // 从第一个订单中获取基本信息（假设所有订单属于同一个部门）
        Integer depId = null;
        Integer depFatherId = null;
        Integer disId = null;
        Integer userId = null;
        if (!orderList.isEmpty()) {
            NxDepartmentOrdersEntity firstOrder = orderList.get(0);
            depId = firstOrder.getNxDoDepartmentId();
            depFatherId = firstOrder.getNxDoDepartmentFatherId();
            disId = firstOrder.getNxDoDistributerId();
            userId = firstOrder.getNxDoOrderUserId();
        }

        // ========== 创建OCR任务记录 ==========
        logger.info("[pasteSearchGoods] 开始创建OCR任务记录...");

        // 获取用户信息（上传用户）
        NxDistributerUserEntity uploadUser = null;
        String uploadUserName = "未知用户";
        Integer uploadUserId = null;
        if (userId != null) {
            try {
                uploadUser = nxDistributerUserService.queryObject(userId);
                if (uploadUser != null) {
                    uploadUserId = uploadUser.getNxDistributerUserId().intValue();
                    uploadUserName = uploadUser.getNxDiuWxNickName() != null ? uploadUser.getNxDiuWxNickName() : "未知用户";
                }
            } catch (Exception e) {
                logger.warn("[pasteSearchGoods] 获取上传用户信息失败: {}", e.getMessage());
            }
        }

        // 获取文件名（使用时间戳生成）
        String fileName = "paste_order_" + System.currentTimeMillis();

        // 创建OCR任务记录
        NxOcrTaskEntity ocrTask = new NxOcrTaskEntity();
        NxDepartmentEntity nxDepartmentEntity = nxDepartmentService.queryObject(depFatherId);
        String  depName = nxDepartmentEntity.getNxDepartmentAttrName();
        System.out.println("depnidd====" + nxDepartmentEntity.getNxDepartmentFatherId());
        if(nxDepartmentEntity.getNxDepartmentFatherId() != 0){
            NxDepartmentEntity subDepartmentEntity = nxDepartmentService.queryObject(depId);
            depName = depName + '.' + subDepartmentEntity.getNxDepartmentAttrName();
        }
        //查询这个 depFatherId 今日的第几个图片任务
        Map<String, Object> mapTask = new HashMap<>();
        mapTask.put("departmentFatherId", depFatherId);
        mapTask.put("date", formatWhatDay(0));  // 使用 date 参数，格式：yyyy-MM-dd
        mapTask.put("type", 1);
        int count = nxOcrTaskService.queryTotalByDepartmentAndStatus(mapTask);
        // count 是今日已有的任务数量，count + 1 就是今日第几个任务
        int todayTaskNumber = count + 1;
        logger.info("[recognizeOrderAsync] 查询今日任务数量: {}, 当前任务是今日第 {} 个", count, todayTaskNumber);
        String first5 = pasteText.substring(0, Math.min(5, pasteText.length()));
        ocrTask.setNxOcrTaskFileName(first5 + "...");
        ocrTask.setNxOcrTaskTotalOrders(0);
        ocrTask.setNxOcrTaskCompletedOrders(0);
        ocrTask.setNxOcrTaskPendingOrders(0);
        ocrTask.setNxOcrTaskUploadTime(formatWhatYearDayTime(0));
        ocrTask.setNxOcrTaskUploadUserId(uploadUserId);
        ocrTask.setNxOcrTaskUploadUserName(uploadUserName);
        ocrTask.setNxOcrTaskProcessorUserId(uploadUserId);
        ocrTask.setNxOcrTaskProcessorUserName(uploadUserName);
        ocrTask.setNxOcrTaskStatus(0); // 0=处理中
        ocrTask.setNxOcrTaskCreateDate(formatWhatYearDayTime(0));
        ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
        ocrTask.setNxOcrTaskDistributerId(disId);
        ocrTask.setNxOcrTaskDepartmentId(depId);
        ocrTask.setNxOcrTaskDepartmentFatherId(depFatherId);
        ocrTask.setNxOcrTaskOcrText(pasteText != null ? pasteText : ""); // 保存粘贴的文字内容
        // 尝试从请求中获取 type，有则使用，没有则默认 3（文字 pasteSearchGoods）
        int ocrTaskType = 3;
        Object typeObj = request.get("type");
        if (typeObj != null) {
            if (typeObj instanceof Number) {
                ocrTaskType = ((Number) typeObj).intValue();
            } else {
                try {
                    ocrTaskType = Integer.parseInt(typeObj.toString());
                } catch (NumberFormatException ignored) { }
            }
        }
        ocrTask.setNxOcrTaskType(ocrTaskType);

        // 保存任务记录（获取任务ID）
        nxOcrTaskService.save(ocrTask);
        ocrTaskId = ocrTask.getNxOcrTaskId();
        logger.info("[pasteSearchGoods] OCR任务记录创建成功，任务ID: {}", ocrTaskId);

        // 先查询当前最大的 today_order，为所有订单统一设置递增的 nxDoTodayOrder
        Integer depIdForTodayOrder = depId;
        int currentMaxOrder = 0;
        int todayOrderCounter = 0; // 用于跟踪当前订单的序号
        if (depIdForTodayOrder != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("depFatherId", depIdForTodayOrder);
            Integer integer = nxDepartmentOrdersService.queryOrderGoodsCount(map);
            if(integer > 0){
                currentMaxOrder = nxDepartmentOrdersService.queryMaxTodayOrder(depIdForTodayOrder);
                logger.info("[pasteSearchGoods] 当前部门最大 today_order: {}, 即将处理 {} 个订单", currentMaxOrder, orderList.size());
            }
        }

        // 处理备注字段：如果备注为 "-1"，则设置为 null
        for (NxDepartmentOrdersEntity ordersEntity : orderList) {
            if (ordersEntity.getNxDoRemark() != null && ordersEntity.getNxDoRemark().equals("-1")) {
                ordersEntity.setNxDoRemark(null);
            }
            // 关联OCR任务ID
            ordersEntity.setNxDoOcrTaskId(ocrTaskId);
        }

        // 用于保存处理结果的 Map，key 为订单索引，value 为订单实体
        Map<Integer, NxDepartmentOrdersEntity> responseMap = new HashMap<>();
        List<NxDepartmentOrdersEntity> orderListForSearch = new ArrayList<>();
        // 保存 orderListForSearch 中每个订单对应的原始订单索引
        List<Integer> orderIndexList = new ArrayList<>();

        // 遍历订单列表，进行四级强搜索
        for (int i = 0; i < orderList.size(); i++) {
            NxDepartmentOrdersEntity ordersEntity = orderList.get(i);
            String goodsName = ordersEntity.getNxDoGoodsName();
            String spec = ordersEntity.getNxDoStandard();
            String note = ordersEntity.getNxDoRemark();
            String quantityStr = ordersEntity.getNxDoQuantity();

            // 直接从订单实体中获取包装结构字段
            String standardWeight = ordersEntity.getStandardWeight() != null ? ordersEntity.getStandardWeight().trim() : "";
            String itemUnit = ordersEntity.getItemUnit() != null ? ordersEntity.getItemUnit().trim() : "";
            String itemsPerCarton = ordersEntity.getItemsPerCarton() != null ? ordersEntity.getItemsPerCarton().trim() : "";
            String cartonUnit = ordersEntity.getCartonUnit() != null ? ordersEntity.getCartonUnit().trim() : "";

            if (goodsName == null || goodsName.trim().isEmpty()) {
                logger.warn("[pasteSearchGoods] 跳过订单：商品名称为空，索引: {}", i);
                continue;
            }
            // 判断是否缺少数量或规格（3个必备条件：商品名称、数量、规格）
            boolean isDataMissing = (quantityStr.trim().isEmpty()) && (spec.trim().isEmpty());

            // 预先设置 nxDoTodayOrder，确保顺序正确
            int todayOrder = currentMaxOrder + todayOrderCounter + 1;
            ordersEntity.setNxDoTodayOrder(todayOrder);
            logger.info("[pasteSearchGoods] 设置订单 todayOrder: 商品名称={}, 原始索引={}, todayOrder={}, currentMaxOrder={}, counter={}",
                    goodsName, i, todayOrder, currentMaxOrder, todayOrderCounter);
            todayOrderCounter++;

            // 如果订单缺少3个必备条件（商品名称、数量、规格），设置状态为-2，保存订单，跳过后续处理
            if (isDataMissing) {
                logger.info("[recognizeOrderFast] 订单缺少数量或规格，设置状态为-2并保存订单，跳过后续商品搜索和训练数据添加: goodsName={}, quantity={}, spec={}",
                        goodsName, quantityStr, spec);
                ordersEntity.setNxDoStatus(-2);
                saveOrderWithoutGoods(ordersEntity);
                // 将订单添加到响应Map
                responseMap.put(i, ordersEntity);
                continue;
            }


            // 1级优先匹配：首先查询部门商品历史记录，如果订货商品名称和规格完全匹配，则直接推断为该商品
            Map<String, Object> depGoodsMap = new HashMap<>();
            depGoodsMap.put("disId", disId);
            depGoodsMap.put("depId", depId);
            depGoodsMap.put("name", goodsName);
            depGoodsMap.put("standard", spec);
            List<NxDepartmentDisGoodsEntity> departmentDisGoodsList = nxDepartmentDisGoodsService.queryDepartmentGoods(depGoodsMap);
            logger.info("[pasteSearchGoods] 1级优先匹配-部门商品历史记录查询结果数量: {}", departmentDisGoodsList.size());
            // 如果部门商品历史记录中有完全匹配的（名称+规格），则直接使用该商品
            if (departmentDisGoodsList.size() == 1) {
                NxDepartmentDisGoodsEntity departmentDisGoodsEntity = departmentDisGoodsList.get(0);
                NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(departmentDisGoodsEntity.getNxDdgDisGoodsId());
                if (distributerGoodsEntity != null) {
                    // 复制粘贴业务没有数量/规格改变的概念，订单状态统一设为0（已完成）
                    ordersEntity.setNxDoStatus(0);
                    logger.info("[pasteSearchGoods] 1级优先匹配成功，订单状态设置为 0（已完成）");
                    // 保存订单并转换为响应DTO（传递包装结构字段）
                    logger.info("[pasteSearchGoods] 1级匹配-调用saveStrongQueryOrderAndConvert[{}]: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}",
                            i, standardWeight, itemUnit, itemsPerCarton, cartonUnit);

                    saveStrongQueryOrderAndConvert(ordersEntity, distributerGoodsEntity,  i, responseMap);
                } else {
                    logger.warn("[pasteSearchGoods] 1级匹配-部门商品引用的配送商商品已不存在，商品ID: {}，跳过", departmentDisGoodsEntity.getNxDdgDisGoodsId());
                }
            }

            // 2级优先匹配：查询训练表中是否有相同内容（匹配：部门ID + 商品名称）
            // 注：商品库名称+规格、别名匹配已移至 Service 层，避免重复查询
            if (responseMap.get(i) == null) {
                Map<String, Object> matchParams = new HashMap<>();
                matchParams.put("depId", depId);
                matchParams.put("goodsName", goodsName);
                matchParams.put("disGoodsId", 1);
                NxOrderOcrTrainingDataEntity matchedTrainingData = nxOrderOcrTrainingDataService.queryByMatchFields(matchParams);

                if (matchedTrainingData != null && matchedTrainingData.getNxOtdDisGoodsId() != null )  {


                    // 训练数据中有商品ID，直接创建订单
                    logger.info("[pasteSearchGoods] 4级优先匹配-找到匹配的训练数据，商品ID: {}, 直接创建订单",
                            matchedTrainingData.getNxOtdDisGoodsId());

                    // 查询商品信息
                    NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(
                            matchedTrainingData.getNxOtdDisGoodsId());
                    if( disGoodsEntity != null){
                        // 训练数据中有商品ID，直接创建订单
                        logger.info("[pasteSearchGoods] 4级优先匹配-找到匹配的训练数据，商品ID: {}, 直接创建订单",
                                matchedTrainingData.getNxOtdDisGoodsId());
                        // 优先使用训练数据的 final_goods_name（如果存在），否则使用原始商品名称
                        String finalGoodsName = matchedTrainingData.getNxOtdFinalGoodsName();
                        String orderGoodsName = (finalGoodsName != null && !finalGoodsName.trim().isEmpty())
                                ? finalGoodsName
                                : goodsName;
                        if (!orderGoodsName.equals(goodsName)) {
                            logger.info("[pasteSearchGoods] 使用训练数据的 final_goods_name='{}' 替代原始商品名称='{}'",
                                    orderGoodsName, goodsName);
                        }
                        ordersEntity.setNxDoGoodsName(orderGoodsName);
                        ordersEntity.setNxDoGoodsOriginalName(orderGoodsName);

                        // 复制粘贴业务没有数量/规格改变的概念，订单状态统一设为0（已完成）
                        ordersEntity.setNxDoStatus(0);
                        logger.info("[pasteSearchGoods] 4级优先匹配成功，订单状态设置为 0（已完成）");
                        // 保存订单并转换为响应DTO（传递包装结构字段）
                        logger.info("[pasteSearchGoods] 4级匹配-调用saveStrongQueryOrderAndConvert[{}]: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}",
                                i, standardWeight, itemUnit, itemsPerCarton, cartonUnit);

                        saveStrongQueryOrderAndConvert(ordersEntity, disGoodsEntity,  i, responseMap);
                    }

                }
            }

            // 以上是2种强查询（1级部门商品历史、2级训练数据），商品库匹配已移至 Service 层
            if (responseMap.get(i) == null) {
                logger.info("[pasteSearchGoods] 2种强查询都未命中，创建训练数据并交由 Service 搜索: goodsName={}", goodsName);

                // 构建 originalText：商品名称 + 订货数量 + 订货规格（拼接后的内容）
                // 格式：商品名 数量规格（如："香叶 2两" 或 "香叶 2 两"）
                StringBuilder originalTextBuilder = new StringBuilder();
                if (goodsName != null && !goodsName.trim().isEmpty()) {
                    originalTextBuilder.append(goodsName.trim());
                }
                if (quantityStr != null && !quantityStr.trim().isEmpty()) {
                    if (originalTextBuilder.length() > 0) {
                        originalTextBuilder.append(" ");
                    }
                    originalTextBuilder.append(quantityStr.trim());
                }
                if (spec != null && !spec.trim().isEmpty()) {
                    // 如果数量不为空，规格直接跟在数量后面（如："2斤"），否则加空格
                    if (quantityStr == null || quantityStr.trim().isEmpty()) {
                        if (originalTextBuilder.length() > 0) {
                            originalTextBuilder.append(" ");
                        }
                    }
                    originalTextBuilder.append(spec.trim());
                }
                String originalText = originalTextBuilder.toString();
                logger.info("[pasteSearchGoods] 构建 originalText: 商品名={}, 数量={}, 规格={}, 结果={}",
                        goodsName, quantityStr, spec, originalText);

                // 创建训练数据：复制粘贴业务没有 rawName 和 name 的概念，deepseekRecommendedName 传 null
                NxOrderOcrTrainingDataEntity trainingData = createOrQueryTrainingData(
                        depId, depFatherId, disId, goodsName, null, quantityStr, spec, null, note, originalText, userId);

                // 如果是新创建的训练数据（数据源为 OCR_IMAGE），更新为 PASTE（复制粘贴）
                if ("OCR_IMAGE".equals(trainingData.getNxOtdDataSource())) {
                    trainingData.setNxOtdDataSource("PASTE");
                    nxOrderOcrTrainingDataService.update(trainingData);
                }
                logger.info("[pasteSearchGoods] 训练数据创建成功，训练数据ID: {}", trainingData.getNxOtdId());

                ordersEntity.setNxDoStatus(-2);
                ordersEntity.setNxDoTrainingDataId(trainingData.getNxOtdId());
                ordersEntity.setNxDoQuantity(quantityStr);
                ordersEntity.setNxDoStandard(spec);
                // 设置订单用户ID，确保订单不会被判定为临时订单而跳过保存
                // 如果 userId 为 null，设置默认值 -1，避免订单被判定为临时订单
                Integer finalUserId = userId != null ? userId : -1;
                ordersEntity.setNxDoOrderUserId(finalUserId);
                if (userId != null) {
                    logger.info("[pasteSearchGoods] 设置订单用户ID: userId={}, 商品名称={}, 订单索引={}",
                            userId, goodsName, i);
                } else {
                    logger.info("[pasteSearchGoods] 订单用户ID为空，设置默认值 -1: 商品名称={}, 订单索引={}",
                            goodsName, i);
                }

                logger.info("[pasteSearchGoods] 准备将订单添加到搜索列表: 商品名称={}, 状态={}, 训练数据ID={}, 订单索引={}",
                        goodsName, ordersEntity.getNxDoStatus(), trainingData.getNxOtdId(), i);

                orderListForSearch.add(ordersEntity);
                orderIndexList.add(i);
            }
        }

        if (!orderListForSearch.isEmpty()) {
            logger.info("[pasteSearchGoods] 开始调用 searchAndSaveOrdersFromOcr 进行搜索和保存，待搜索订单数量: {}",
                    orderListForSearch.size());
            for (int idx = 0; idx < orderListForSearch.size(); idx++) {
                NxDepartmentOrdersEntity order = orderListForSearch.get(idx);
                logger.info("[pasteSearchGoods] 待搜索订单[{}]: 商品名称={}, 状态={}, 用户ID={}, 训练数据ID={}, todayOrder={}",
                        idx, order.getNxDoGoodsName(), order.getNxDoStatus(),
                        order.getNxDoOrderUserId(), order.getNxDoTrainingDataId(), order.getNxDoTodayOrder());
            }

            List<NxDepartmentOrdersEntity> searchResults = nxDepartmentOrdersService.searchAndSaveOrdersFromOcr(orderListForSearch);
            logger.info("[pasteSearchGoods] searchAndSaveOrdersFromOcr 返回结果数量: {}",
                    searchResults != null ? searchResults.size() : 0);

            // 将搜索结果按原始索引保存到 responseMap 中（直接使用返回的订单实体，已包含候选商品列表）
            for (int j = 0; j < searchResults.size() && j < orderIndexList.size(); j++) {
                Integer originalIndex = orderIndexList.get(j);
                NxDepartmentOrdersEntity order = searchResults.get(j);
                if (order != null && originalIndex != null) {
                    logger.info("[pasteSearchGoods] 保存搜索结果到 responseMap: 原始索引={}, 订单ID={}, 状态={}, 商品ID={}, 候选商品数={}",
                            originalIndex, order.getNxDepartmentOrdersId(), order.getNxDoStatus(), order.getNxDoDisGoodsId(),
                            order.getNxDistributerGoodsEntityList() != null ? order.getNxDistributerGoodsEntityList().size() : 0);
                    responseMap.put(originalIndex, order);
                } else {
                    logger.warn("[pasteSearchGoods] 搜索结果为空或索引无效: j={}, originalIndex={}, order={}",
                            j, originalIndex, order != null ? "not null" : "null");
                }
            }
        } else {
            logger.info("[pasteSearchGoods] orderListForSearch 为空，无需调用 searchAndSaveOrdersFromOcr");
        }

        // 按照原始顺序组装响应列表（直接返回订单实体）
        List<NxDepartmentOrdersEntity> responseList = new ArrayList<>();
        for (int i = 0; i < orderList.size(); i++) {
            NxDepartmentOrdersEntity order = responseMap.get(i);
            if (order != null) {
                responseList.add(order);
            }
        }

        // ========== 更新OCR任务状态 ==========
        if (ocrTaskId != null) {
            try {
                ocrTask = nxOcrTaskService.queryObject(ocrTaskId);
                if (ocrTask != null) {
                    // 一次性查询各状态订单数量（避免遍历内存列表）
                    int[] statusCounts = nxDepartmentOrdersService.queryCountByOcrTaskIdGroupByStatus(ocrTaskId);
                    int completedOrders = statusCounts[0];
                    int pendingOrders = statusCounts[1];

                    // 更新任务状态
                    ocrTask.setNxOcrTaskTotalOrders(responseList.size());
                    ocrTask.setNxOcrTaskCompletedOrders(completedOrders);
                    ocrTask.setNxOcrTaskPendingOrders(pendingOrders);
                    if (pendingOrders == 0) {
                        ocrTask.setNxOcrTaskStatus(2); // 2=已完成
                    } else if (completedOrders > 0) {
                        ocrTask.setNxOcrTaskStatus(1); // 1=部分完成
                    } else {
                        ocrTask.setNxOcrTaskStatus(1); // 1=部分完成（所有订单都是待修正）
                    }
                    ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
                    nxOcrTaskService.update(ocrTask);
                    logger.info("[pasteSearchGoods] 更新任务状态完成，任务ID: {}, 总订单数: {}, 已完成订单: {}, 待修正订单: {}, 任务状态: {}",
                            ocrTaskId, responseList.size(), completedOrders, pendingOrders, ocrTask.getNxOcrTaskStatus());
                }
            } catch (Exception e) {
                logger.error("[pasteSearchGoods] 更新任务状态失败，任务ID: {}", ocrTaskId, e);
            }
        }

        logger.info("[pasteSearchGoods] 订单处理完成，共 {} 条订单", responseList.size());

        return R.ok().put("data", responseList)
                .put("taskId", ocrTaskId)
                .put("task", ocrTask);
    } catch (Exception e) {
        logger.error("[pasteSearchGoods] 处理订单时发生错误", e);
        // 更新任务状态为失败
        if (ocrTaskId != null) {
            try {
                NxOcrTaskEntity ocrTask = nxOcrTaskService.queryObject(ocrTaskId);
                if (ocrTask != null) {
                    ocrTask.setNxOcrTaskStatus(-1); // -1=失败
                    ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
                    nxOcrTaskService.update(ocrTask);
                    logger.info("[pasteSearchGoods] 任务状态已更新为失败，任务ID: {}", ocrTaskId);
                }
            } catch (Exception updateException) {
                logger.error("[pasteSearchGoods] 更新任务状态失败", updateException);
            }
        }
        return R.error("处理订单时发生错误: " + e.getMessage());
    }
}


//    精简前
//    @RequestMapping(value = "/pasteSearchGoods1", method = RequestMethod.POST)
//    @ResponseBody
//    public R pasteSearchGoods1(@RequestBody Map<String, Object> request) {
//        Integer ocrTaskId = null;
//        try {
//            // 从请求中提取订单列表和粘贴文字内容
//            List<NxDepartmentOrdersEntity> orderList = null;
//            Object orderListObj = request.get("orderList");
//            if (orderListObj != null) {
//                // 如果是字符串，先解析为JSON
//                if (orderListObj instanceof String) {
//                    orderList = JSON.parseArray((String) orderListObj, NxDepartmentOrdersEntity.class);
//                } else if (orderListObj instanceof List) {
//                    // 如果是List，需要转换为JSON字符串再解析，确保类型正确
//                    String orderListJson = JSON.toJSONString(orderListObj);
//                    orderList = JSON.parseArray(orderListJson, NxDepartmentOrdersEntity.class);
//                } else {
//                    // 其他类型，尝试直接转换
//                    String orderListJson = JSON.toJSONString(orderListObj);
//                    orderList = JSON.parseArray(orderListJson, NxDepartmentOrdersEntity.class);
//                }
//            }
//            String pasteText = request.get("pasteText") != null ? request.get("pasteText").toString() : null;
//
//            if (orderList == null || orderList.isEmpty()) {
//                logger.warn("[pasteSearchGoods] 订单列表为空");
//                return R.error("订单列表不能为空");
//            }
//
//            logger.info("[pasteSearchGoods] 开始处理订单列表，订单数量: {}, 粘贴文字内容长度: {}",
//                    orderList.size(), pasteText != null ? pasteText.length() : 0);
//
//            // 从第一个订单中获取基本信息（假设所有订单属于同一个部门）
//            Integer depId = null;
//            Integer depFatherId = null;
//            Integer disId = null;
//            Integer userId = null;
//            if (!orderList.isEmpty()) {
//                NxDepartmentOrdersEntity firstOrder = orderList.get(0);
//                depId = firstOrder.getNxDoDepartmentId();
//                depFatherId = firstOrder.getNxDoDepartmentFatherId();
//                disId = firstOrder.getNxDoDistributerId();
//                userId = firstOrder.getNxDoOrderUserId();
//            }
//
//            // ========== 创建OCR任务记录 ==========
//            logger.info("[pasteSearchGoods] 开始创建OCR任务记录...");
//
//            // 获取用户信息（上传用户）
//            NxDistributerUserEntity uploadUser = null;
//            String uploadUserName = "未知用户";
//            Integer uploadUserId = null;
//            if (userId != null) {
//                try {
//                    uploadUser = nxDistributerUserService.queryObject(userId);
//                    if (uploadUser != null) {
//                        uploadUserId = uploadUser.getNxDistributerUserId().intValue();
//                        uploadUserName = uploadUser.getNxDiuWxNickName() != null ? uploadUser.getNxDiuWxNickName() : "未知用户";
//                    }
//                } catch (Exception e) {
//                    logger.warn("[pasteSearchGoods] 获取上传用户信息失败: {}", e.getMessage());
//                }
//            }
//
//            // 获取文件名（使用时间戳生成）
//            String fileName = "paste_order_" + System.currentTimeMillis();
//
//            // 创建OCR任务记录
//            NxOcrTaskEntity ocrTask = new NxOcrTaskEntity();
//            NxDepartmentEntity nxDepartmentEntity = nxDepartmentService.queryObject(depFatherId);
//            String  depName = nxDepartmentEntity.getNxDepartmentAttrName();
//            System.out.println("depnidd====" + nxDepartmentEntity.getNxDepartmentFatherId());
//            if(nxDepartmentEntity.getNxDepartmentFatherId() != 0){
//                NxDepartmentEntity subDepartmentEntity = nxDepartmentService.queryObject(depId);
//                depName = depName + '.' + subDepartmentEntity.getNxDepartmentAttrName();
//            }
//            //查询这个 depFatherId 今日的第几个图片任务
//            Map<String, Object> mapTask = new HashMap<>();
//            mapTask.put("departmentFatherId", depFatherId);
//            mapTask.put("date", formatWhatDay(0));  // 使用 date 参数，格式：yyyy-MM-dd
//            mapTask.put("type", 1);
//            int count = nxOcrTaskService.queryTotalByDepartmentAndStatus(mapTask);
//            // count 是今日已有的任务数量，count + 1 就是今日第几个任务
//            int todayTaskNumber = count + 1;
//            logger.info("[recognizeOrderAsync] 查询今日任务数量: {}, 当前任务是今日第 {} 个", count, todayTaskNumber);
//            String first5 = pasteText.substring(0, Math.min(5, pasteText.length()));
//            ocrTask.setNxOcrTaskFileName(first5 + "...");
//            ocrTask.setNxOcrTaskTotalOrders(0);
//            ocrTask.setNxOcrTaskCompletedOrders(0);
//            ocrTask.setNxOcrTaskPendingOrders(0);
//            ocrTask.setNxOcrTaskUploadTime(formatWhatYearDayTime(0));
//            ocrTask.setNxOcrTaskUploadUserId(uploadUserId);
//            ocrTask.setNxOcrTaskUploadUserName(uploadUserName);
//            ocrTask.setNxOcrTaskProcessorUserId(uploadUserId);
//            ocrTask.setNxOcrTaskProcessorUserName(uploadUserName);
//            ocrTask.setNxOcrTaskStatus(0); // 0=处理中
//            ocrTask.setNxOcrTaskCreateDate(formatWhatYearDayTime(0));
//            ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
//            ocrTask.setNxOcrTaskDistributerId(disId);
//            ocrTask.setNxOcrTaskDepartmentId(depId);
//            ocrTask.setNxOcrTaskDepartmentFatherId(depFatherId);
//            ocrTask.setNxOcrTaskOcrText(pasteText != null ? pasteText : ""); // 保存粘贴的文字内容
//            // 尝试从请求中获取 type，有则使用，没有则默认 3（文字 pasteSearchGoods）
//            int ocrTaskType = 3;
//            Object typeObj = request.get("type");
//            if (typeObj != null) {
//                if (typeObj instanceof Number) {
//                    ocrTaskType = ((Number) typeObj).intValue();
//                } else {
//                    try {
//                        ocrTaskType = Integer.parseInt(typeObj.toString());
//                    } catch (NumberFormatException ignored) { }
//                }
//            }
//            ocrTask.setNxOcrTaskType(ocrTaskType);
//
//            // 保存任务记录（获取任务ID）
//            nxOcrTaskService.save(ocrTask);
//            ocrTaskId = ocrTask.getNxOcrTaskId();
//            logger.info("[pasteSearchGoods] OCR任务记录创建成功，任务ID: {}", ocrTaskId);
//
//            // 先查询当前最大的 today_order，为所有订单统一设置递增的 nxDoTodayOrder
//            Integer depIdForTodayOrder = depId;
//            int currentMaxOrder = 0;
//            int todayOrderCounter = 0; // 用于跟踪当前订单的序号
//            if (depIdForTodayOrder != null) {
//                Map<String, Object> map = new HashMap<>();
//                map.put("depFatherId", depIdForTodayOrder);
//                Integer integer = nxDepartmentOrdersService.queryOrderGoodsCount(map);
//                if(integer > 0){
//                    currentMaxOrder = nxDepartmentOrdersService.queryMaxTodayOrder(depIdForTodayOrder);
//                    logger.info("[pasteSearchGoods] 当前部门最大 today_order: {}, 即将处理 {} 个订单", currentMaxOrder, orderList.size());
//                }
//            }
//
//            // 处理备注字段：如果备注为 "-1"，则设置为 null
//            for (NxDepartmentOrdersEntity ordersEntity : orderList) {
//                if (ordersEntity.getNxDoRemark() != null && ordersEntity.getNxDoRemark().equals("-1")) {
//                    ordersEntity.setNxDoRemark(null);
//                }
//                // 关联OCR任务ID
//                ordersEntity.setNxDoOcrTaskId(ocrTaskId);
//            }
//
//            // 用于保存处理结果的 Map，key 为订单索引，value 为订单实体
//            Map<Integer, NxDepartmentOrdersEntity> responseMap = new HashMap<>();
//            List<NxDepartmentOrdersEntity> orderListForSearch = new ArrayList<>();
//            // 保存 orderListForSearch 中每个订单对应的原始订单索引
//            List<Integer> orderIndexList = new ArrayList<>();
//
//            // 遍历订单列表，进行四级强搜索
//            for (int i = 0; i < orderList.size(); i++) {
//                NxDepartmentOrdersEntity ordersEntity = orderList.get(i);
//                String goodsName = ordersEntity.getNxDoGoodsName();
//                String spec = ordersEntity.getNxDoStandard();
//                String note = ordersEntity.getNxDoRemark();
//                String quantityStr = ordersEntity.getNxDoQuantity();
//
//                // 直接从订单实体中获取包装结构字段
//                String standardWeight = ordersEntity.getStandardWeight() != null ? ordersEntity.getStandardWeight().trim() : "";
//                String itemUnit = ordersEntity.getItemUnit() != null ? ordersEntity.getItemUnit().trim() : "";
//                String itemsPerCarton = ordersEntity.getItemsPerCarton() != null ? ordersEntity.getItemsPerCarton().trim() : "";
//                String cartonUnit = ordersEntity.getCartonUnit() != null ? ordersEntity.getCartonUnit().trim() : "";
//
//                if (goodsName == null || goodsName.trim().isEmpty()) {
//                    logger.warn("[pasteSearchGoods] 跳过订单：商品名称为空，索引: {}", i);
//                    continue;
//                }
//                // 判断是否缺少数量或规格（3个必备条件：商品名称、数量、规格）
//                boolean isDataMissing = (quantityStr.trim().isEmpty()) && (spec.trim().isEmpty());
//
//                // 预先设置 nxDoTodayOrder，确保顺序正确
//                int todayOrder = currentMaxOrder + todayOrderCounter + 1;
//                ordersEntity.setNxDoTodayOrder(todayOrder);
//                logger.info("[pasteSearchGoods] 设置订单 todayOrder: 商品名称={}, 原始索引={}, todayOrder={}, currentMaxOrder={}, counter={}",
//                        goodsName, i, todayOrder, currentMaxOrder, todayOrderCounter);
//                todayOrderCounter++;
//
//                // 如果订单缺少3个必备条件（商品名称、数量、规格），设置状态为-2，保存订单，跳过后续处理
//                if (isDataMissing) {
//                    logger.info("[recognizeOrderFast] 订单缺少数量或规格，设置状态为-2并保存订单，跳过后续商品搜索和训练数据添加: goodsName={}, quantity={}, spec={}",
//                            goodsName, quantityStr, spec);
//                    ordersEntity.setNxDoStatus(-2);
//                    saveOrderWithoutGoods(ordersEntity);
//                    // 将订单添加到响应Map
//                    responseMap.put(i, ordersEntity);
//                    continue;
//                }
//
//
//                // 1级优先匹配：首先查询部门商品历史记录，如果订货商品名称和规格完全匹配，则直接推断为该商品
//                Map<String, Object> depGoodsMap = new HashMap<>();
//                depGoodsMap.put("depId", depId);
//                depGoodsMap.put("name", goodsName);
//                depGoodsMap.put("standard", spec);
//                List<NxDepartmentDisGoodsEntity> departmentDisGoodsList = nxDepartmentDisGoodsService.queryDepartmentGoods(depGoodsMap);
//                logger.info("[pasteSearchGoods] 1级优先匹配-部门商品历史记录查询结果数量: {}", departmentDisGoodsList.size());
//                // 如果部门商品历史记录中有完全匹配的（名称+规格），则直接使用该商品
//                if (departmentDisGoodsList.size() == 1) {
//                    NxDepartmentDisGoodsEntity departmentDisGoodsEntity = departmentDisGoodsList.get(0);
//                    NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(departmentDisGoodsEntity.getNxDdgDisGoodsId());
//
//                    // 复制粘贴业务没有数量/规格改变的概念，订单状态统一设为0（已完成）
//                    ordersEntity.setNxDoStatus(0);
//                    logger.info("[pasteSearchGoods] 1级优先匹配成功，订单状态设置为 0（已完成）");
//                    // 保存订单并转换为响应DTO（传递包装结构字段）
//                    logger.info("[pasteSearchGoods] 1级匹配-调用saveStrongQueryOrderAndConvert[{}]: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}",
//                            i, standardWeight, itemUnit, itemsPerCarton, cartonUnit);
//
//                    saveStrongQueryOrderAndConvert(ordersEntity, distributerGoodsEntity,  i, responseMap);
//
//                }
//
//                // 2级优先匹配：商品库，如果订货商品名称和规格完全匹配，则直接推断为该商品
//                if (responseMap.get(i) == null) {
//                    Map<String, Object> mapZero = new HashMap<>();
//                    mapZero.put("disId", disId);
//                    mapZero.put("searchStr", goodsName);
//                    mapZero.put("standard", spec);
//                    logger.info("[pasteSearchGoods] 2级优先匹配-商品名称+规格查询参数: {}", mapZero);
//                    List<NxDistributerGoodsEntity> distributerGoodsEntitiesZero = nxDistributerGoodsService.queryDisGoodsByName(mapZero);
//                    // 如果商品历史记录中有完全匹配的（名称+规格），则直接使用该商品
//                    if (distributerGoodsEntitiesZero.size() == 1) {
//                        NxDistributerGoodsEntity distributerGoodsEntity = distributerGoodsEntitiesZero.get(0);
//                        logger.info("[pasteSearchGoods] 2级优先匹配：商品库，直接保存订单，disGoodsId={}",
//                                distributerGoodsEntity.getNxDistributerGoodsId());
//
//                        // 复制粘贴业务没有数量/规格改变的概念，订单状态统一设为0（已完成）
//                        ordersEntity.setNxDoStatus(0);
//                        logger.info("[pasteSearchGoods] 2级优先匹配成功，订单状态设置为 0（已完成）");
//                        // 保存订单并转换为响应DTO（传递包装结构字段）
//                        logger.info("[pasteSearchGoods] 2级匹配-调用saveStrongQueryOrderAndConvert[{}]: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}",
//                                i, standardWeight, itemUnit, itemsPerCarton, cartonUnit);
//
//                        saveStrongQueryOrderAndConvert(ordersEntity, distributerGoodsEntity,  i, responseMap);
//
//                    }
//                }
//
//                // 3级优先匹配：商品库别名精准查询，如果订货商品名称和规格完全匹配，则直接推断为该商品
//                if (responseMap.get(i) == null) {
//                    Map<String, Object> mapA = new HashMap<>();
//                    mapA.put("disId", disId);
//                    mapA.put("alias", goodsName);
//                    mapA.put("standard", spec);
//                    mapA.put("depId", depId);
//                    List<NxDistributerGoodsEntity> distributerGoodsEntitiesAlias = nxDistributerGoodsService.queryDisGoodsByAlias(mapA);
//                    // 如果商品历史记录中有完全匹配的（名称+规格），则直接使用该商品
//                    if (distributerGoodsEntitiesAlias.size() == 1) {
//                        NxDistributerGoodsEntity distributerGoodsEntity = distributerGoodsEntitiesAlias.get(0);
//                        logger.info("[pasteSearchGoods] 3级优先匹配-商品库别名精准查询，直接保存订单，disGoodsId={}",
//                                distributerGoodsEntity.getNxDistributerGoodsId());
//
//                        // 复制粘贴业务没有数量/规格改变的概念，订单状态统一设为0（已完成）
//                        ordersEntity.setNxDoStatus(0);
//                        logger.info("[pasteSearchGoods] 3级优先匹配成功，订单状态设置为 0（已完成）");
//
//                        // 保存订单并转换为响应DTO（传递包装结构字段）
//                        logger.info("[pasteSearchGoods] 3级匹配-调用saveStrongQueryOrderAndConvert[{}]: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}",
//                                i, standardWeight, itemUnit, itemsPerCarton, cartonUnit);
//
//                        saveStrongQueryOrderAndConvert(ordersEntity, distributerGoodsEntity,  i, responseMap);
//
//                    }
//                }
//
//                // 4级优先匹配：查询训练表中是否有相同内容（匹配：部门ID + 商品名称）
//                if (responseMap.get(i) == null) {
//                    Map<String, Object> matchParams = new HashMap<>();
//                    matchParams.put("depId", depId);
//                    matchParams.put("goodsName", goodsName);
//                    matchParams.put("disGoodsId", 1);
//                    NxOrderOcrTrainingDataEntity matchedTrainingData = nxOrderOcrTrainingDataService.queryByMatchFields(matchParams);
//
//                    if (matchedTrainingData != null && matchedTrainingData.getNxOtdDisGoodsId() != null) {
//                        // 训练数据中有商品ID，直接创建订单
//                        logger.info("[pasteSearchGoods] 4级优先匹配-找到匹配的训练数据，商品ID: {}, 直接创建订单",
//                                matchedTrainingData.getNxOtdDisGoodsId());
//
//                        // 查询商品信息
//                        NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(
//                                matchedTrainingData.getNxOtdDisGoodsId());
//                        if (disGoodsEntity == null) {
//                            logger.error("[pasteSearchGoods] 商品不存在，商品ID: {}",
//                                    matchedTrainingData.getNxOtdDisGoodsId());
//                            throw new RuntimeException("商品不存在，商品ID: " + matchedTrainingData.getNxOtdDisGoodsId());
//                        }
//
//                        // 优先使用训练数据的 final_goods_name（如果存在），否则使用原始商品名称
//                        String finalGoodsName = matchedTrainingData.getNxOtdFinalGoodsName();
//                        String orderGoodsName = (finalGoodsName != null && !finalGoodsName.trim().isEmpty())
//                                ? finalGoodsName
//                                : goodsName;
//                        if (!orderGoodsName.equals(goodsName)) {
//                            logger.info("[pasteSearchGoods] 使用训练数据的 final_goods_name='{}' 替代原始商品名称='{}'",
//                                    orderGoodsName, goodsName);
//                        }
//                        ordersEntity.setNxDoGoodsName(orderGoodsName);
//
//                        // 复制粘贴业务没有数量/规格改变的概念，订单状态统一设为0（已完成）
//                        ordersEntity.setNxDoStatus(0);
//                        logger.info("[pasteSearchGoods] 4级优先匹配成功，订单状态设置为 0（已完成）");
//                        // 保存订单并转换为响应DTO（传递包装结构字段）
//                        logger.info("[pasteSearchGoods] 4级匹配-调用saveStrongQueryOrderAndConvert[{}]: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}",
//                                i, standardWeight, itemUnit, itemsPerCarton, cartonUnit);
//
//                        saveStrongQueryOrderAndConvert(ordersEntity, disGoodsEntity,  i, responseMap);
//
//                    }
//                }
//
//                // 以上是用 goodsName 进行4种强查询（部门商品历史记录、商品库名称、商品库别名、训练数据）
//                if (responseMap.get(i) == null) {
//                    logger.info("[pasteSearchGoods] goodsName 的4种强查询都没有找到商品，创建训练数据: goodsName={}", goodsName);
//
//                    // 构建 originalText：商品名称 + 订货数量 + 订货规格（拼接后的内容）
//                    // 格式：商品名 数量规格（如："香叶 2两" 或 "香叶 2 两"）
//                    StringBuilder originalTextBuilder = new StringBuilder();
//                    if (goodsName != null && !goodsName.trim().isEmpty()) {
//                        originalTextBuilder.append(goodsName.trim());
//                    }
//                    if (quantityStr != null && !quantityStr.trim().isEmpty()) {
//                        if (originalTextBuilder.length() > 0) {
//                            originalTextBuilder.append(" ");
//                        }
//                        originalTextBuilder.append(quantityStr.trim());
//                    }
//                    if (spec != null && !spec.trim().isEmpty()) {
//                        // 如果数量不为空，规格直接跟在数量后面（如："2斤"），否则加空格
//                        if (quantityStr == null || quantityStr.trim().isEmpty()) {
//                            if (originalTextBuilder.length() > 0) {
//                                originalTextBuilder.append(" ");
//                            }
//                        }
//                        originalTextBuilder.append(spec.trim());
//                    }
//                    String originalText = originalTextBuilder.toString();
//                    logger.info("[pasteSearchGoods] 构建 originalText: 商品名={}, 数量={}, 规格={}, 结果={}",
//                            goodsName, quantityStr, spec, originalText);
//
//                    // 创建训练数据：复制粘贴业务没有 rawName 和 name 的概念，deepseekRecommendedName 传 null
//                    NxOrderOcrTrainingDataEntity trainingData = createOrQueryTrainingData(
//                            depId, depFatherId, disId, goodsName, null, quantityStr, spec, null, note, originalText, userId);
//
//                    // 如果是新创建的训练数据（数据源为 OCR_IMAGE），更新为 PASTE（复制粘贴）
//                    if ("OCR_IMAGE".equals(trainingData.getNxOtdDataSource())) {
//                        trainingData.setNxOtdDataSource("PASTE");
//                        nxOrderOcrTrainingDataService.update(trainingData);
//                    }
//                    logger.info("[pasteSearchGoods] 训练数据创建成功，训练数据ID: {}", trainingData.getNxOtdId());
//
//                    ordersEntity.setNxDoStatus(-2);
//                    ordersEntity.setNxDoTrainingDataId(trainingData.getNxOtdId());
//                    ordersEntity.setNxDoQuantity(quantityStr);
//                    ordersEntity.setNxDoStandard(spec);
//                    // 设置订单用户ID，确保订单不会被判定为临时订单而跳过保存
//                    // 如果 userId 为 null，设置默认值 -1，避免订单被判定为临时订单
//                    Integer finalUserId = userId != null ? userId : -1;
//                    ordersEntity.setNxDoOrderUserId(finalUserId);
//                    if (userId != null) {
//                        logger.info("[pasteSearchGoods] 设置订单用户ID: userId={}, 商品名称={}, 订单索引={}",
//                                userId, goodsName, i);
//                    } else {
//                        logger.info("[pasteSearchGoods] 订单用户ID为空，设置默认值 -1: 商品名称={}, 订单索引={}",
//                                goodsName, i);
//                    }
//
//                    logger.info("[pasteSearchGoods] 准备将订单添加到搜索列表: 商品名称={}, 状态={}, 训练数据ID={}, 订单索引={}",
//                            goodsName, ordersEntity.getNxDoStatus(), trainingData.getNxOtdId(), i);
//
//                    orderListForSearch.add(ordersEntity);
//                    orderIndexList.add(i);
//                }
//            }
//
//            if (!orderListForSearch.isEmpty()) {
//                logger.info("[pasteSearchGoods] 开始调用 searchAndSaveOrdersFromOcr 进行搜索和保存，待搜索订单数量: {}",
//                        orderListForSearch.size());
//                for (int idx = 0; idx < orderListForSearch.size(); idx++) {
//                    NxDepartmentOrdersEntity order = orderListForSearch.get(idx);
//                    logger.info("[pasteSearchGoods] 待搜索订单[{}]: 商品名称={}, 状态={}, 用户ID={}, 训练数据ID={}, todayOrder={}",
//                            idx, order.getNxDoGoodsName(), order.getNxDoStatus(),
//                            order.getNxDoOrderUserId(), order.getNxDoTrainingDataId(), order.getNxDoTodayOrder());
//                }
//
//                List<NxDepartmentOrdersEntity> searchResults = nxDepartmentOrdersService.searchAndSaveOrdersFromOcr(orderListForSearch);
//                logger.info("[pasteSearchGoods] searchAndSaveOrdersFromOcr 返回结果数量: {}",
//                        searchResults != null ? searchResults.size() : 0);
//
//                // 将搜索结果按原始索引保存到 responseMap 中（直接使用返回的订单实体，已包含候选商品列表）
//                for (int j = 0; j < searchResults.size() && j < orderIndexList.size(); j++) {
//                    Integer originalIndex = orderIndexList.get(j);
//                    NxDepartmentOrdersEntity order = searchResults.get(j);
//                    if (order != null && originalIndex != null) {
//                        logger.info("[pasteSearchGoods] 保存搜索结果到 responseMap: 原始索引={}, 订单ID={}, 状态={}, 商品ID={}, 候选商品数={}",
//                                originalIndex, order.getNxDepartmentOrdersId(), order.getNxDoStatus(), order.getNxDoDisGoodsId(),
//                                order.getNxDistributerGoodsEntityList() != null ? order.getNxDistributerGoodsEntityList().size() : 0);
//                        responseMap.put(originalIndex, order);
//                    } else {
//                        logger.warn("[pasteSearchGoods] 搜索结果为空或索引无效: j={}, originalIndex={}, order={}",
//                                j, originalIndex, order != null ? "not null" : "null");
//                    }
//                }
//            } else {
//                logger.info("[pasteSearchGoods] orderListForSearch 为空，无需调用 searchAndSaveOrdersFromOcr");
//            }
//
//            // 按照原始顺序组装响应列表（直接返回订单实体）
//            List<NxDepartmentOrdersEntity> responseList = new ArrayList<>();
//            for (int i = 0; i < orderList.size(); i++) {
//                NxDepartmentOrdersEntity order = responseMap.get(i);
//                if (order != null) {
//                    responseList.add(order);
//                }
//            }
//
//            // ========== 更新OCR任务状态 ==========
//            if (ocrTaskId != null) {
//                try {
//                     ocrTask = nxOcrTaskService.queryObject(ocrTaskId);
//                    if (ocrTask != null) {
//                        // 统计订单状态
//                        int completedOrders = 0;
//                        int pendingOrders = 0;
//                        for (NxDepartmentOrdersEntity order : responseList) {
//                            if (order.getNxDoStatus() != null) {
//                                if (order.getNxDoStatus() == 0) {
//                                    completedOrders++;
//                                } else if (order.getNxDoStatus() == -2) {
//                                    pendingOrders++;
//                                }
//                            }
//                        }
//
//                        // 更新任务状态
//                        ocrTask.setNxOcrTaskTotalOrders(responseList.size());
//                        ocrTask.setNxOcrTaskCompletedOrders(completedOrders);
//                        ocrTask.setNxOcrTaskPendingOrders(pendingOrders);
//                        if (pendingOrders == 0) {
//                            ocrTask.setNxOcrTaskStatus(2); // 2=已完成
//                        } else if (completedOrders > 0) {
//                            ocrTask.setNxOcrTaskStatus(1); // 1=部分完成
//                        } else {
//                            ocrTask.setNxOcrTaskStatus(1); // 1=部分完成（所有订单都是待修正）
//                        }
//                        ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
//                        nxOcrTaskService.update(ocrTask);
//                        logger.info("[pasteSearchGoods] 更新任务状态完成，任务ID: {}, 总订单数: {}, 已完成订单: {}, 待修正订单: {}, 任务状态: {}",
//                                ocrTaskId, responseList.size(), completedOrders, pendingOrders, ocrTask.getNxOcrTaskStatus());
//                    }
//                } catch (Exception e) {
//                    logger.error("[pasteSearchGoods] 更新任务状态失败，任务ID: {}", ocrTaskId, e);
//                }
//            }
//
//            logger.info("[pasteSearchGoods] 订单处理完成，共 {} 条订单", responseList.size());
//
//            return R.ok().put("data", responseList)
//                    .put("taskId", ocrTaskId)
//                    .put("task", ocrTask);
//        } catch (Exception e) {
//            logger.error("[pasteSearchGoods] 处理订单时发生错误", e);
//            // 更新任务状态为失败
//            if (ocrTaskId != null) {
//                try {
//                    NxOcrTaskEntity ocrTask = nxOcrTaskService.queryObject(ocrTaskId);
//                    if (ocrTask != null) {
//                        ocrTask.setNxOcrTaskStatus(-1); // -1=失败
//                        ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
//                        nxOcrTaskService.update(ocrTask);
//                        logger.info("[pasteSearchGoods] 任务状态已更新为失败，任务ID: {}", ocrTaskId);
//                    }
//                } catch (Exception updateException) {
//                    logger.error("[pasteSearchGoods] 更新任务状态失败", updateException);
//                }
//            }
//            return R.error("处理订单时发生错误: " + e.getMessage());
//        }
//    }
//    


//    @RequestMapping(value = "/pasteSearchGoods", method = RequestMethod.POST)
//    @ResponseBody
//    public R pasteSearchGoods(@RequestBody List<Map<String, Object>> orderMapList) {
//        try {
//            if (orderMapList == null || orderMapList.isEmpty()) {
//                logger.warn("[pasteSearchGoods] 订单列表为空");
//                return R.error("订单列表不能为空");
//            }
//
//            logger.info("[pasteSearchGoods] 开始处理订单列表，订单数量: {}", orderMapList.size());
//
//            // 先查询当前最大的 today_order，为所有订单统一设置递增的 nxDoTodayOrder
//            // 需要从第一个订单中获取 depId（假设所有订单属于同一个部门）
//            Integer depIdForTodayOrder = null;
//            if (!orderMapList.isEmpty()) {
//                Map<String, Object> firstOrder = orderMapList.get(0);
//                Object depIdObj = firstOrder.get("nxDoDepartmentId");
//                if (depIdObj != null) {
//                    if (depIdObj instanceof Number) {
//                        depIdForTodayOrder = ((Number) depIdObj).intValue();
//                    } else {
//                        depIdForTodayOrder = Integer.parseInt(depIdObj.toString());
//                    }
//                }
//            }
//
//            int currentMaxOrder = 0;
//            int todayOrderCounter = 0; // 用于跟踪当前订单的序号
//            Map<String, Object> map = new HashMap<>();
//            map.put("depFatherId", depIdForTodayOrder);
//            Integer integer = nxDepartmentOrdersService.queryOrderGoodsCount(map);
//            if(integer > 0){
//                if (depIdForTodayOrder != null) {
//                    currentMaxOrder = nxDepartmentOrdersService.queryMaxTodayOrder(depIdForTodayOrder);
//                    logger.info("[pasteSearchGoods] 当前部门最大 today_order: {}, 即将处理 {} 个订单", currentMaxOrder, orderMapList.size());
//                }
//            }
//
//
//            // 保存原始订单JSON字符串的列表，索引与orderList对应
//            List<String> originalOrderJsonList = new ArrayList<>();
//            // 保存原始商品名称的列表，索引与orderList对应（客户录入的原始内容，不可修改）
//            List<String> originalGoodsNameList = new ArrayList<>();
//            // 转换为实体列表
//            List<NxDepartmentOrdersEntity> orderList = new ArrayList<>();
//
//            // 处理备注字段：如果备注为 "-1"，则设置为 null，并保存原始JSON
//            for (int idx = 0; idx < orderMapList.size(); idx++) {
//                Map<String, Object> orderMap = orderMapList.get(idx);
//                // 保存原始订单的JSON字符串（在处理前保存，包含所有字段）
//                String originalOrderJson = JSON.toJSONString(orderMap);
//                originalOrderJsonList.add(originalOrderJson);
//                logger.info("[pasteSearchGoods] 保存原始订单JSON[{}]: {}", idx, originalOrderJson);
//
//                // 转换为实体对象
//                NxDepartmentOrdersEntity ordersEntity = JSON.parseObject(originalOrderJson, NxDepartmentOrdersEntity.class);
//                orderList.add(ordersEntity);
//
//                // 保存原始商品名称（客户录入的原始内容，在处理前保存，不可修改）
//                String originalGoodsName = ordersEntity.getNxDoGoodsName();
//                originalGoodsNameList.add(originalGoodsName);
//
//                if (ordersEntity.getNxDoRemark() != null && ordersEntity.getNxDoRemark().equals("-1")) {
//                    ordersEntity.setNxDoRemark(null);
//                }
//            }
//
//            // 用于保存处理结果的 Map，key 为订单索引，value 为订单实体
//            Map<Integer, NxDepartmentOrdersEntity> responseMap = new HashMap<>();
//            List<NxDepartmentOrdersEntity> orderListForSearch = new ArrayList<>();
//            // 保存 orderListForSearch 中每个订单对应的原始订单索引
//            List<Integer> orderIndexList = new ArrayList<>();
//
//            // 遍历订单列表，进行四级强搜索
//            for (int i = 0; i < orderList.size(); i++) {
//                NxDepartmentOrdersEntity ordersEntity = orderList.get(i);
//                String goodsName = ordersEntity.getNxDoGoodsName();
//                String spec = ordersEntity.getNxDoStandard();
//                Integer depId = ordersEntity.getNxDoDepartmentId();
//                Integer depFatherId = ordersEntity.getNxDoDepartmentFatherId();
//                Integer disId = ordersEntity.getNxDoDistributerId();
//                Integer userId = ordersEntity.getNxDoOrderUserId();
//                String note = ordersEntity.getNxDoRemark();
//                String quantityStr = ordersEntity.getNxDoQuantity();
//
//                // 从原始JSON中提取包装结构字段（使用 org.json.JSONObject，与图片识别代码保持一致）
//                String standardWeight = "";
//                String itemUnit = "";
//                String itemsPerCarton = "";
//                String cartonUnit = "";
//                if (i < originalOrderJsonList.size()) {
//                    try {
//                        String jsonStr = originalOrderJsonList.get(i);
//                        logger.info("[pasteSearchGoods] 开始解析原始订单JSON[{}]: {}", i, jsonStr);
//                        JSONObject originalOrderJson = new JSONObject(jsonStr);
//                        standardWeight = originalOrderJson.optString("standardWeight", "");
//                        itemUnit = originalOrderJson.optString("itemUnit", "");
//                        itemsPerCarton = originalOrderJson.optString("itemsPerCarton", "");
//                        cartonUnit = originalOrderJson.optString("cartonUnit", "");
//
//                        logger.info("[pasteSearchGoods] 解析出的字段值[{}]: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}",
//                                i, standardWeight, itemUnit, itemsPerCarton, cartonUnit);
//
//                        // 处理空值
//                        standardWeight = (standardWeight != null) ? standardWeight.trim() : "";
//                        itemUnit = (itemUnit != null) ? itemUnit.trim() : "";
//                        itemsPerCarton = (itemsPerCarton != null) ? itemsPerCarton.trim() : "";
//                        cartonUnit = (cartonUnit != null) ? cartonUnit.trim() : "";
//
//                        logger.info("[pasteSearchGoods] 处理后的字段值[{}]: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}",
//                                i, standardWeight, itemUnit, itemsPerCarton, cartonUnit);
//                    } catch (Exception e) {
//                        logger.error("[pasteSearchGoods] 解析原始订单JSON时出错，索引: {}, JSON字符串: {}, 错误: {}",
//                                i, originalOrderJsonList.get(i), e.getMessage(), e);
//                    }
//                } else {
//                    logger.warn("[pasteSearchGoods] 索引{}超出原始JSON列表范围，列表大小: {}", i, originalOrderJsonList.size());
//                }
//
//                if (goodsName == null || goodsName.trim().isEmpty()) {
//                    logger.warn("[pasteSearchGoods] 跳过订单：商品名称为空，索引: {}", i);
//                    continue;
//                }
//
//
//                // 预先设置 nxDoTodayOrder，确保顺序正确
//                int todayOrder = currentMaxOrder + todayOrderCounter + 1;
//                ordersEntity.setNxDoTodayOrder(todayOrder);
//                logger.info("[pasteSearchGoods] 设置订单 todayOrder: 商品名称={}, 原始索引={}, todayOrder={}, currentMaxOrder={}, counter={}",
//                        goodsName, i, todayOrder, currentMaxOrder, todayOrderCounter);
//                todayOrderCounter++;
//
//
//                // 1级优先匹配：首先查询部门商品历史记录，如果订货商品名称和规格完全匹配，则直接推断为该商品
//                Map<String, Object> depGoodsMap = new HashMap<>();
//                depGoodsMap.put("depId", depId);
//                depGoodsMap.put("name", goodsName);
//                depGoodsMap.put("standard", spec);
//                List<NxDepartmentDisGoodsEntity> departmentDisGoodsList = nxDepartmentDisGoodsService.queryDepartmentGoods(depGoodsMap);
//                logger.info("[pasteSearchGoods] 1级优先匹配-部门商品历史记录查询结果数量: {}", departmentDisGoodsList.size());
//                // 如果部门商品历史记录中有完全匹配的（名称+规格），则直接使用该商品
//                if (departmentDisGoodsList.size() == 1) {
//                    NxDepartmentDisGoodsEntity departmentDisGoodsEntity = departmentDisGoodsList.get(0);
//                    NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(departmentDisGoodsEntity.getNxDdgDisGoodsId());
//
//                    // 复制粘贴业务没有数量/规格改变的概念，订单状态统一设为0（已完成）
//                    ordersEntity.setNxDoStatus(0);
//                    logger.info("[pasteSearchGoods] 1级优先匹配成功，订单状态设置为 0（已完成）");
//                    // 保存订单并转换为响应DTO（传递包装结构字段）
//                    logger.info("[pasteSearchGoods] 1级匹配-调用saveStrongQueryOrderAndConvert[{}]: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}",
//                            i, standardWeight, itemUnit, itemsPerCarton, cartonUnit);
//
//                    saveStrongQueryOrderAndConvert(ordersEntity, distributerGoodsEntity,  i, responseMap);
//
//                }
//
//                // 2级优先匹配：商品库，如果订货商品名称和规格完全匹配，则直接推断为该商品
//                if (responseMap.get(i) == null) {
//                    Map<String, Object> mapZero = new HashMap<>();
//                    mapZero.put("disId", disId);
//                    mapZero.put("searchStr", goodsName);
//                    mapZero.put("standard", spec);
//                    logger.info("[pasteSearchGoods] 2级优先匹配-商品名称+规格查询参数: {}", mapZero);
//                    List<NxDistributerGoodsEntity> distributerGoodsEntitiesZero = nxDistributerGoodsService.queryDisGoodsByName(mapZero);
//                    // 如果商品历史记录中有完全匹配的（名称+规格），则直接使用该商品
//                    if (distributerGoodsEntitiesZero.size() == 1) {
//                        NxDistributerGoodsEntity distributerGoodsEntity = distributerGoodsEntitiesZero.get(0);
//                        logger.info("[pasteSearchGoods] 2级优先匹配：商品库，直接保存订单，disGoodsId={}",
//                                distributerGoodsEntity.getNxDistributerGoodsId());
//
//                        // 复制粘贴业务没有数量/规格改变的概念，订单状态统一设为0（已完成）
//                        ordersEntity.setNxDoStatus(0);
//                        logger.info("[pasteSearchGoods] 2级优先匹配成功，订单状态设置为 0（已完成）");
//                        // 保存订单并转换为响应DTO（传递包装结构字段）
//                        logger.info("[pasteSearchGoods] 2级匹配-调用saveStrongQueryOrderAndConvert[{}]: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}",
//                                i, standardWeight, itemUnit, itemsPerCarton, cartonUnit);
//
//                        saveStrongQueryOrderAndConvert(ordersEntity, distributerGoodsEntity,  i, responseMap);
//
//                    }
//                }
//
//                // 3级优先匹配：商品库别名精准查询，如果订货商品名称和规格完全匹配，则直接推断为该商品
//                if (responseMap.get(i) == null) {
//                    Map<String, Object> mapA = new HashMap<>();
//                    mapA.put("disId", disId);
//                    mapA.put("alias", goodsName);
//                    mapA.put("standard", spec);
//                    mapA.put("depId", depId);
//                    List<NxDistributerGoodsEntity> distributerGoodsEntitiesAlias = nxDistributerGoodsService.queryDisGoodsByAlias(mapA);
//                    // 如果商品历史记录中有完全匹配的（名称+规格），则直接使用该商品
//                    if (distributerGoodsEntitiesAlias.size() == 1) {
//                        NxDistributerGoodsEntity distributerGoodsEntity = distributerGoodsEntitiesAlias.get(0);
//                        logger.info("[pasteSearchGoods] 3级优先匹配-商品库别名精准查询，直接保存订单，disGoodsId={}",
//                                distributerGoodsEntity.getNxDistributerGoodsId());
//
//                        // 复制粘贴业务没有数量/规格改变的概念，订单状态统一设为0（已完成）
//                        ordersEntity.setNxDoStatus(0);
//                        logger.info("[pasteSearchGoods] 3级优先匹配成功，订单状态设置为 0（已完成）");
//
//                        // 保存订单并转换为响应DTO（传递包装结构字段）
//                        logger.info("[pasteSearchGoods] 3级匹配-调用saveStrongQueryOrderAndConvert[{}]: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}",
//                                i, standardWeight, itemUnit, itemsPerCarton, cartonUnit);
//
//                        saveStrongQueryOrderAndConvert(ordersEntity, distributerGoodsEntity,  i, responseMap);
//
//                    }
//                }
//
//                // 4级优先匹配：查询训练表中是否有相同内容（匹配：部门ID + 商品名称）
//                if (responseMap.get(i) == null) {
//                    Map<String, Object> matchParams = new HashMap<>();
//                    matchParams.put("depId", depId);
//                    matchParams.put("goodsName", goodsName);
//                    matchParams.put("disGoodsId", 1);
////                    matchParams.put("dataSource", "PASTE"); // 粘贴场景：查询 PASTE 类型的数据
//                    NxOrderOcrTrainingDataEntity matchedTrainingData = nxOrderOcrTrainingDataService.queryByMatchFields(matchParams);
//
//                    if (matchedTrainingData != null && matchedTrainingData.getNxOtdDisGoodsId() != null) {
//                        // 训练数据中有商品ID，直接创建订单
//                        logger.info("[pasteSearchGoods] 4级优先匹配-找到匹配的训练数据，商品ID: {}, 直接创建订单",
//                                matchedTrainingData.getNxOtdDisGoodsId());
//
//                        // 查询商品信息
//                        NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(
//                                matchedTrainingData.getNxOtdDisGoodsId());
//                        if (disGoodsEntity == null) {
//                            logger.error("[pasteSearchGoods] 商品不存在，商品ID: {}",
//                                    matchedTrainingData.getNxOtdDisGoodsId());
//                            throw new RuntimeException("商品不存在，商品ID: " + matchedTrainingData.getNxOtdDisGoodsId());
//                        }
//
//                        // 优先使用训练数据的 final_goods_name（如果存在），否则使用原始商品名称
//                        String finalGoodsName = matchedTrainingData.getNxOtdFinalGoodsName();
//                        String orderGoodsName = (finalGoodsName != null && !finalGoodsName.trim().isEmpty())
//                                ? finalGoodsName
//                                : goodsName;
//                        if (!orderGoodsName.equals(goodsName)) {
//                            logger.info("[pasteSearchGoods] 使用训练数据的 final_goods_name='{}' 替代原始商品名称='{}'",
//                                    orderGoodsName, goodsName);
//                        }
//                        ordersEntity.setNxDoGoodsName(orderGoodsName);
//
//                        // 复制粘贴业务没有数量/规格改变的概念，订单状态统一设为0（已完成）
//                        ordersEntity.setNxDoStatus(0);
//                        logger.info("[pasteSearchGoods] 4级优先匹配成功，订单状态设置为 0（已完成）");
//                        // 保存订单并转换为响应DTO（传递包装结构字段）
//                        logger.info("[pasteSearchGoods] 4级匹配-调用saveStrongQueryOrderAndConvert[{}]: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}",
//                                i, standardWeight, itemUnit, itemsPerCarton, cartonUnit);
//
//                        saveStrongQueryOrderAndConvert(ordersEntity, disGoodsEntity,  i, responseMap);
//
//                    }
//                }
//
//                // 以上是用 goodsName 进行4种强查询（部门商品历史记录、商品库名称、商品库别名、训练数据）
//                if (responseMap.get(i) == null) {
//                    logger.info("[pasteSearchGoods] goodsName 的4种强查询都没有找到商品，创建训练数据: goodsName={}", goodsName);
//
//                    // 创建训练数据：复制粘贴业务没有 rawName 和 name 的概念，deepseekRecommendedName 传 null，没有 OCR 原文，originalText 传空字符串
//                    NxOrderOcrTrainingDataEntity trainingData = createOrQueryTrainingData(
//                            depId, depFatherId, disId, goodsName, null, quantityStr, spec, null, note, "", userId);
//
//                    // 如果是新创建的训练数据（数据源为 OCR_IMAGE），更新为 PASTE（复制粘贴）
//                    if ("OCR_IMAGE".equals(trainingData.getNxOtdDataSource())) {
//                        trainingData.setNxOtdDataSource("PASTE");
//                        nxOrderOcrTrainingDataService.update(trainingData);
//                    }
//                    logger.info("[pasteSearchGoods] 训练数据创建成功，训练数据ID: {}", trainingData.getNxOtdId());
//
//                    ordersEntity.setNxDoStatus(-2);
//                    ordersEntity.setNxDoTrainingDataId(trainingData.getNxOtdId());
//                    ordersEntity.setNxDoQuantity(quantityStr);
//                    ordersEntity.setNxDoStandard(spec);
//                    // 设置订单用户ID，确保订单不会被判定为临时订单而跳过保存
//                    // 如果 userId 为 null，设置默认值 -1，避免订单被判定为临时订单
//                    Integer finalUserId = userId != null ? userId : -1;
//                    ordersEntity.setNxDoOrderUserId(finalUserId);
//                    if (userId != null) {
//                        logger.info("[pasteSearchGoods] 设置订单用户ID: userId={}, 商品名称={}, 订单索引={}",
//                                userId, goodsName, i);
//                    } else {
//                        logger.info("[pasteSearchGoods] 订单用户ID为空，设置默认值 -1: 商品名称={}, 订单索引={}",
//                                goodsName, i);
//                    }
//
//                    logger.info("[pasteSearchGoods] 准备将订单添加到搜索列表: 商品名称={}, 状态={}, 训练数据ID={}, 订单索引={}",
//                            goodsName, ordersEntity.getNxDoStatus(), trainingData.getNxOtdId(), i);
//
//                    orderListForSearch.add(ordersEntity);
//                    orderIndexList.add(i);
//                }
//            }
//
//            if (!orderListForSearch.isEmpty()) {
//                logger.info("[pasteSearchGoods] 开始调用 searchAndSaveOrdersFromOcr 进行搜索和保存，待搜索订单数量: {}",
//                        orderListForSearch.size());
//                for (int idx = 0; idx < orderListForSearch.size(); idx++) {
//                    NxDepartmentOrdersEntity order = orderListForSearch.get(idx);
//                    logger.info("[pasteSearchGoods] 待搜索订单[{}]: 商品名称={}, 状态={}, 用户ID={}, 训练数据ID={}, todayOrder={}",
//                            idx, order.getNxDoGoodsName(), order.getNxDoStatus(),
//                            order.getNxDoOrderUserId(), order.getNxDoTrainingDataId(), order.getNxDoTodayOrder());
//                }
//
//                List<PasteSearchGoodsResponseDTO> searchResults = nxDepartmentOrdersService.searchAndSaveOrdersFromOcr(orderListForSearch);
//                logger.info("[pasteSearchGoods] searchAndSaveOrdersFromOcr 返回结果数量: {}",
//                        searchResults != null ? searchResults.size() : 0);
//
//                // 将搜索结果按原始索引保存到 responseMap 中（从DTO查询订单实体）
//                for (int j = 0; j < searchResults.size() && j < orderIndexList.size(); j++) {
//                    Integer originalIndex = orderIndexList.get(j);
//                    PasteSearchGoodsResponseDTO dto = searchResults.get(j);
//                    if (dto != null && originalIndex != null) {
//                        // 从DTO查询订单实体
//                        NxDepartmentOrdersEntity order = nxDepartmentOrdersService.queryObject(dto.getNxDepartmentOrdersId());
//                        if (order == null) {
//                            logger.warn("[pasteSearchGoods] 订单实体不存在，订单ID: {}", dto.getNxDepartmentOrdersId());
//                            continue;
//                        }
//
//                        // 从原始JSON中提取并设置包装结构字段（使用 org.json.JSONObject，与图片识别代码保持一致）
//                        if (originalIndex < originalOrderJsonList.size()) {
//                            try {
//                                String jsonStr = originalOrderJsonList.get(originalIndex);
//                                logger.info("[pasteSearchGoods] searchAndSaveOrdersFromOcr返回结果-解析原始订单JSON[{}]: {}", originalIndex, jsonStr);
//                                JSONObject originalOrderJson = new JSONObject(jsonStr);
//                                String standardWeight = originalOrderJson.optString("standardWeight", "");
//                                String itemUnit = originalOrderJson.optString("itemUnit", "");
//                                String itemsPerCarton = originalOrderJson.optString("itemsPerCarton", "");
//                                String cartonUnit = originalOrderJson.optString("cartonUnit", "");
//
//                                logger.info("[pasteSearchGoods] searchAndSaveOrdersFromOcr返回结果-解析出的字段值[{}]: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}",
//                                        originalIndex, standardWeight, itemUnit, itemsPerCarton, cartonUnit);
//
//                                // 设置字段值到订单实体（处理空值）
//                                order.setStandardWeight((standardWeight != null) ? standardWeight.trim() : "");
//                                order.setItemUnit((itemUnit != null) ? itemUnit.trim() : "");
//                                order.setItemsPerCarton((itemsPerCarton != null) ? itemsPerCarton.trim() : "");
//                                order.setCartonUnit((cartonUnit != null) ? cartonUnit.trim() : "");
//
//                                logger.info("[pasteSearchGoods] searchAndSaveOrdersFromOcr返回结果-设置后的订单实体字段值[{}]: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}",
//                                        originalIndex, order.getStandardWeight(), order.getItemUnit(), order.getItemsPerCarton(), order.getCartonUnit());
//                            } catch (Exception e) {
//                                logger.error("[pasteSearchGoods] 解析原始订单JSON时出错，索引: {}, JSON字符串: {}, 错误: {}",
//                                        originalIndex, originalOrderJsonList.get(originalIndex), e.getMessage(), e);
//                            }
//                        } else {
//                            logger.warn("[pasteSearchGoods] searchAndSaveOrdersFromOcr返回结果-索引{}超出原始JSON列表范围，列表大小: {}", originalIndex, originalOrderJsonList.size());
//                        }
//                        logger.info("[pasteSearchGoods] 保存搜索结果到 responseMap: 原始索引={}, 订单ID={}, 状态={}, 商品ID={}",
//                                originalIndex, order.getNxDepartmentOrdersId(), order.getNxDoStatus(), order.getNxDoDisGoodsId());
//                        responseMap.put(originalIndex, order);
//                    } else {
//                        logger.warn("[pasteSearchGoods] 搜索结果为空或索引无效: j={}, originalIndex={}, dto={}",
//                                j, originalIndex, dto);
//                    }
//                }
//            } else {
//                logger.info("[pasteSearchGoods] orderListForSearch 为空，无需调用 searchAndSaveOrdersFromOcr");
//            }
//
//            // 按照原始顺序组装响应列表（直接返回订单实体）
//            List<NxDepartmentOrdersEntity> responseList = new ArrayList<>();
//            for (int i = 0; i < orderList.size(); i++) {
//                NxDepartmentOrdersEntity order = responseMap.get(i);
//                if (order != null) {
//                    // 注意：订单实体中没有 nxDoGoodsNameOriginal 字段，如果需要可以添加到订单实体中
//                    // 或者前端可以直接使用 nxDoGoodsName 字段
//                    // 确保包装结构字段被设置（如果之前没有设置，则从原始JSON中提取，使用 org.json.JSONObject，与图片识别代码保持一致）
//                    logger.info("[pasteSearchGoods] 最终组装响应列表[{}]: 当前订单实体字段值: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}",
//                            i, order.getStandardWeight(), order.getItemUnit(), order.getItemsPerCarton(), order.getCartonUnit());
//                    if ((order.getStandardWeight() == null || order.getStandardWeight().isEmpty())
//                            && i < originalOrderJsonList.size()) {
//                        try {
//                            String jsonStr = originalOrderJsonList.get(i);
//                            logger.info("[pasteSearchGoods] 最终组装响应列表-解析原始订单JSON[{}]: {}", i, jsonStr);
//                            JSONObject originalOrderJson = new JSONObject(jsonStr);
//                            String standardWeight = originalOrderJson.optString("standardWeight", "");
//                            String itemUnit = originalOrderJson.optString("itemUnit", "");
//                            String itemsPerCarton = originalOrderJson.optString("itemsPerCarton", "");
//                            String cartonUnit = originalOrderJson.optString("cartonUnit", "");
//
//                            logger.info("[pasteSearchGoods] 最终组装响应列表-解析出的字段值[{}]: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}",
//                                    i, standardWeight, itemUnit, itemsPerCarton, cartonUnit);
//
//                            // 设置字段值（处理空值）
//                            order.setStandardWeight((standardWeight != null) ? standardWeight.trim() : "");
//                            order.setItemUnit((itemUnit != null) ? itemUnit.trim() : "");
//                            order.setItemsPerCarton((itemsPerCarton != null) ? itemsPerCarton.trim() : "");
//                            order.setCartonUnit((cartonUnit != null) ? cartonUnit.trim() : "");
//
//                            logger.info("[pasteSearchGoods] 最终组装响应列表-设置后的订单实体字段值[{}]: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}",
//                                    i, order.getStandardWeight(), order.getItemUnit(), order.getItemsPerCarton(), order.getCartonUnit());
//                        } catch (Exception e) {
//                            logger.error("[pasteSearchGoods] 最终组装响应列表时解析原始订单JSON出错，索引: {}, JSON字符串: {}, 错误: {}",
//                                    i, originalOrderJsonList.get(i), e.getMessage(), e);
//                        }
//                    }
//                    logger.info("[pasteSearchGoods] 最终响应列表[{}]: 最终订单实体字段值: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}",
//                            i, order.getStandardWeight(), order.getItemUnit(), order.getItemsPerCarton(), order.getCartonUnit());
//                    responseList.add(order);
//                }
//            }
//
//            logger.info("[pasteSearchGoods] 订单处理完成，共 {} 条订单", responseList.size());
//
//            return R.ok().put("data", responseList);
//        } catch (Exception e) {
//            logger.error("[pasteSearchGoods] 处理订单时发生错误", e);
//            return R.error("处理订单时发生错误: " + e.getMessage());
//        }
//    }

    /**
     * 异步订单识别接口（只进行强查询，不查询推荐商品）
     * 接收图片 -> OCR识别 -> DeepSeek转换为订单 -> 只进行强查询 -> 快速返回任务ID
     * 
     * @param request 请求参数：
     *                - ImageBase64: 图片的Base64编码（必填）
     *                - depId: 部门ID（必填）
     *                - disId: 分销商ID（必填）
     *                - depFatherId: 部门父ID（必填）
     *                - userId: 用户ID（必填）
     *                - processorUserId: 处理人ID（可选）
     *                - fileName: 文件名（可选）
     * @return 任务ID，前端可通过任务ID查询处理进度和结果
     */
    @RequestMapping(value = "/recognizeOrderAsync", method = RequestMethod.POST)
    @ResponseBody
    public R recognizeOrderAsync(@RequestBody Map<String, Object> request) {
        Integer ocrTaskId = null;
        try {
            // ========== 第一步：验证必填参数 ==========
            Integer depId, disId, depFatherId, userId;
            try {
                depId = getIntegerParam(request, "depId", true);
                disId = getIntegerParam(request, "disId", true);
                depFatherId = getIntegerParam(request, "depFatherId", true);
                userId = getIntegerParam(request, "userId", true);
            } catch (IllegalArgumentException e) {
                return R.error(e.getMessage());
            }
            
            logger.info("[recognizeOrderAsync] 接收参数：depId={}, disId={}, depFatherId={}, userId={}", 
                    depId, disId, depFatherId, userId);
            
            // ========== 第二步：创建OCR任务记录 ==========
            logger.info("[recognizeOrderAsync] 开始创建OCR任务记录...");
            
            // 获取用户信息（上传用户）
            SysUserEntity uploadUser = null;
            String uploadUserName = "未知用户";
            Integer uploadUserId = null;
            try {
                uploadUser = ShiroUtils.getUserEntity();
                if (uploadUser != null) {
                    uploadUserId = uploadUser.getUserId().intValue();
                    uploadUserName = uploadUser.getUsername() != null ? uploadUser.getUsername() : "未知用户";
                }
            } catch (Exception e) {
                logger.warn("[recognizeOrderAsync] 获取上传用户信息失败: {}", e.getMessage());
            }
            
            // 获取处理人信息（从请求参数中获取，如果没有则使用上传用户）
            Integer processorUserId = getIntegerParam(request, "processorUserId", false);
            if (processorUserId == null) {
                processorUserId = uploadUserId;
            }
            String processorUserName = uploadUserName; // TODO: 根据processorUserId查询用户名称
            
            // 获取文件名（如果有）
            Object fileNameObj = request.get("fileName");
            String fileName = fileNameObj != null ? fileNameObj.toString() : "ocr_image_" + System.currentTimeMillis();
            
            // 创建OCR任务记录
            NxOcrTaskEntity ocrTask = new NxOcrTaskEntity();
            NxDepartmentEntity nxDepartmentEntity = nxDepartmentService.queryObject(depFatherId);
            String  depName = nxDepartmentEntity.getNxDepartmentAttrName();
            System.out.println("depnidd====" + nxDepartmentEntity.getNxDepartmentFatherId());
            if(nxDepartmentEntity.getNxDepartmentFatherId() != 0){
                NxDepartmentEntity subDepartmentEntity = nxDepartmentService.queryObject(depId);
                depName = depName + '.' + subDepartmentEntity.getNxDepartmentAttrName();
            }
            //查询这个 depFatherId 今日的第几个图片任务
             Map<String, Object> map = new HashMap<>();
             map.put("departmentFatherId", depFatherId);
             map.put("date", formatWhatDay(0));  // 使用 date 参数，格式：yyyy-MM-dd
             map.put("type", 1);
             int count = nxOcrTaskService.queryTotalByDepartmentAndStatus(map);
             // count 是今日已有的任务数量，count + 1 就是今日第几个任务
             int todayTaskNumber = count + 1;
             logger.info("[recognizeOrderAsync] 查询今日任务数量: {}, 当前任务是今日第 {} 个", count, todayTaskNumber);
            ocrTask.setNxOcrTaskFileName(depName + "第" + todayTaskNumber + "个图片订单");
            ocrTask.setNxOcrTaskTotalOrders(0);
            ocrTask.setNxOcrTaskCompletedOrders(0);
            ocrTask.setNxOcrTaskPendingOrders(0);
            ocrTask.setNxOcrTaskUploadTime(formatWhatYearDayTime(0));
            ocrTask.setNxOcrTaskUploadUserId(uploadUserId);
            ocrTask.setNxOcrTaskUploadUserName(uploadUserName);
            ocrTask.setNxOcrTaskProcessorUserId(processorUserId);
            ocrTask.setNxOcrTaskProcessorUserName(processorUserName);
            ocrTask.setNxOcrTaskStatus(0); // 0=处理中
            ocrTask.setNxOcrTaskCreateDate(formatWhatYearDayTime(0));
            ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
            ocrTask.setNxOcrTaskDistributerId(disId);
            ocrTask.setNxOcrTaskDepartmentId(depId);
            ocrTask.setNxOcrTaskDepartmentFatherId(depFatherId);
            ocrTask.setNxOcrTaskType(1); // 1=图片（recognizeOrderAsync）
            
            // 保存任务记录（获取任务ID）
            nxOcrTaskService.save(ocrTask);
            ocrTaskId = ocrTask.getNxOcrTaskId();
            logger.info("[recognizeOrderAsync] OCR任务记录创建成功，任务ID: {}", ocrTaskId);
            
            // ========== 第三步：提取ImageBase64，避免lambda捕获整个request对象 ==========
            // ✅ 内存优化：在提交到线程池之前提取ImageBase64，避免lambda捕获整个request对象
            // request对象包含完整的Base64图片数据（1-5MB），如果被lambda捕获，会导致内存泄漏
            Object imageBase64Obj = request.get("ImageBase64");
            String imageBase64 = null;
            if (imageBase64Obj != null) {
                imageBase64 = imageBase64Obj.toString();
                // 处理Base64前缀（如果前端传了data:image前缀，需要去掉）
                if (imageBase64.contains(",")) {
                    imageBase64 = imageBase64.substring(imageBase64.indexOf(",") + 1);
                }
            }
            
            // 创建 final 变量副本，供 lambda 表达式使用
            final Integer finalOcrTaskId = ocrTaskId;
            final NxOcrTaskEntity finalOcrTask = ocrTask;
            final String finalImageBase64 = imageBase64; // ✅ 只传递ImageBase64字符串，不传递整个request对象
            
            // 检查线程池状态，防止任务堆积导致服务器瘫痪
            if (executorService instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor tpe = (ThreadPoolExecutor) executorService;
                int activeCount = tpe.getActiveCount();
                int queueSize = tpe.getQueue().size();
                int poolSize = tpe.getPoolSize();
                long completedTaskCount = tpe.getCompletedTaskCount();
                int maxQueueSize = 50; // 队列最大容量
                
                logger.info("[recognizeOrderAsync] 线程池状态 - 活跃线程: {}/{}, 队列任务: {}/{}, 已完成任务: {}, 任务ID: {}", 
                        activeCount, poolSize, queueSize, maxQueueSize, completedTaskCount, ocrTaskId);
                
                // 如果队列已满，拒绝新任务，防止服务器瘫痪
                if (queueSize >= maxQueueSize) {
                    logger.error("[recognizeOrderAsync] 🚨 线程池队列已满！拒绝新任务。队列大小: {}/{}, 活跃线程: {}, 任务ID: {}", 
                            queueSize, maxQueueSize, activeCount, ocrTaskId);
                    // 更新任务状态为失败
                    try {
                        ocrTask.setNxOcrTaskStatus(-1); // -1=失败
                        ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
                        nxOcrTaskService.update(ocrTask);
                    } catch (Exception updateException) {
                        logger.error("[recognizeOrderAsync] 更新任务状态失败", updateException);
                    }
                    return R.error("服务器负载过高，请稍后重试。当前队列已满（" + queueSize + "/" + maxQueueSize + "），活跃线程数: " + activeCount);
                }
                
                // 如果队列接近满载，发出警告
                if (queueSize > maxQueueSize * 0.8) {
                    logger.warn("[recognizeOrderAsync] ⚠️ 线程池队列接近满载！队列大小: {}/{}, 活跃线程: {}, 任务ID: {}", 
                            queueSize, maxQueueSize, activeCount, ocrTaskId);
                }
            }
            
            // 使用线程池异步处理，避免阻塞用户请求
            Future<?> future = null;
            try {
                future = executorService.submit(() -> {
                long taskStartTime = System.currentTimeMillis();
                logger.info("[recognizeOrderAsync] 异步任务开始执行，任务ID: {}, 线程: {}", 
                        finalOcrTaskId, Thread.currentThread().getName());
                try {
                    // ✅ 传递ImageBase64字符串，而不是整个request对象，避免内存泄漏
                    processOrderRecognitionAsync(finalOcrTaskId, finalImageBase64, depId, disId, depFatherId, userId, finalOcrTask);
                    long taskElapsedTime = System.currentTimeMillis() - taskStartTime;
                    logger.info("[recognizeOrderAsync] 异步任务执行完成，任务ID: {}, 耗时: {} ms ({} 秒)", 
                            finalOcrTaskId, taskElapsedTime, taskElapsedTime / 1000.0);
                } catch (Exception e) {
                    long taskElapsedTime = System.currentTimeMillis() - taskStartTime;
                    logger.error("[recognizeOrderAsync] 异步处理订单识别失败，任务ID: {}, 耗时: {} ms ({} 秒)", 
                            finalOcrTaskId, taskElapsedTime, taskElapsedTime / 1000.0, e);
                    // 更新任务状态为失败
                    try {
                        finalOcrTask.setNxOcrTaskStatus(-1); // -1=失败
                        finalOcrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
                        nxOcrTaskService.update(finalOcrTask);
                    } catch (Exception updateException) {
                        logger.error("[recognizeOrderAsync] 更新任务状态失败", updateException);
                    }
                } finally {
                    // 记录线程池状态
                    if (executorService instanceof ThreadPoolExecutor) {
                        ThreadPoolExecutor tpe = (ThreadPoolExecutor) executorService;
                        logger.debug("[recognizeOrderAsync] 任务完成后线程池状态 - 活跃线程: {}/{}, 队列任务: {}, 已完成任务: {}", 
                                tpe.getActiveCount(), tpe.getPoolSize(), tpe.getQueue().size(), tpe.getCompletedTaskCount());
                    }
                }
                });
                
                // 立即返回任务ID，不等待处理完成
                logger.info("[recognizeOrderAsync] 任务已提交异步处理，返回任务ID: {}", ocrTaskId);
                return R.ok().put("taskId", ocrTaskId);
            } catch (RejectedExecutionException e) {
                // 线程池拒绝执行任务（队列已满或线程池已关闭）
                logger.error("[recognizeOrderAsync] 🚨 线程池拒绝执行任务！任务ID: {}, 错误: {}", ocrTaskId, e.getMessage(), e);
                // 更新任务状态为失败
                try {
                    ocrTask.setNxOcrTaskStatus(-1); // -1=失败
                    ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
                    nxOcrTaskService.update(ocrTask);
                } catch (Exception updateException) {
                    logger.error("[recognizeOrderAsync] 更新任务状态失败", updateException);
                }
                return R.error("服务器负载过高，无法处理新任务，请稍后重试");
            }
            
        } catch (Exception e) {
            logger.error("[recognizeOrderAsync] 创建任务失败: {}", e.getMessage(), e);
            return R.error("创建任务失败: " + e.getMessage());
        }
    }



    /**
     * 异步处理订单识别和保存（只进行强查询）
     * 
     * @param ocrTaskId OCR任务ID
     * @param imageBase64 图片的Base64编码字符串（已处理前缀）
     * @param depId 部门ID
     * @param disId 分销商ID
     * @param depFatherId 部门父ID
     * @param userId 用户ID
     * @param ocrTask OCR任务实体
     */
    private void processOrderRecognitionAsync(Integer ocrTaskId, String imageBase64,
            Integer depId, Integer disId, Integer depFatherId, Integer userId, NxOcrTaskEntity ocrTask) {
        try {
            logger.info("[processOrderRecognitionAsync] 开始异步处理订单识别，任务ID: {}", ocrTaskId);
            
            // ========== 第一步：验证图片数据 ==========
            if (imageBase64 == null || imageBase64.isEmpty()) {
                logger.error("[processOrderRecognitionAsync] 图片数据为空，任务ID: {}", ocrTaskId);
                ocrTask.setNxOcrTaskStatus(-1); // -1=失败
                ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
                nxOcrTaskService.update(ocrTask);
                return;
            }
            
            // 验证图片大小
            if (imageBase64.length() > MAX_IMAGE_BASE64_SIZE) {
                logger.error("[processOrderRecognitionAsync] 图片过大: {} bytes (限制: {} bytes)，任务ID: {}", 
                    imageBase64.length(), MAX_IMAGE_BASE64_SIZE, ocrTaskId);
                ocrTask.setNxOcrTaskStatus(-1); // -1=失败
                ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
                nxOcrTaskService.update(ocrTask);
                return;
            }
            
            // 保存图片到服务器并更新任务图片路径
            try {
                String imagePath = saveBase64Image(imageBase64, ocrTaskId);
                ocrTask.setNxOcrTaskImagePath(imagePath);
                ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
                nxOcrTaskService.update(ocrTask);
                logger.info("[processOrderRecognitionAsync] 图片保存成功，路径: {}，任务ID: {}", imagePath, ocrTaskId);
            } catch (Exception e) {
                logger.error("[processOrderRecognitionAsync] 保存图片失败，任务ID: {}", ocrTaskId, e);
                // 图片保存失败不影响OCR识别，继续处理
            }
            
            // ========== 第二步：OCR识别 ==========
            // OCR识别（复用原有逻辑）
            String ocrTextContent = performOcrRecognition(imageBase64);

            if (ocrTextContent == null || ocrTextContent.trim().isEmpty()) {
                logger.error("[processOrderRecognitionAsync] OCR识别结果为空，任务ID: {}", ocrTaskId);
                ocrTask.setNxOcrTaskStatus(-1); // 3=失败
                ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
                nxOcrTaskService.update(ocrTask);
                return;
            }
            
            // ========== 第二步：DeepSeek解析 ==========
            // 获取并打印系统 prompt
            String systemPromptContent = nxPromptService.getPromptContentByKey("OCR_IMAGE");
            String prompt;
            if (systemPromptContent != null && !systemPromptContent.trim().isEmpty()) {
                prompt = systemPromptContent;
//                logger.info("[processOrderRecognitionAsync] prompt===={}", prompt);
                logger.info("[processOrderRecognitionAsync] 使用数据库中的系统 prompt (OCR_IMAGE)");
            } else {
                logger.warn("[processOrderRecognitionAsync] 数据库中未找到 OCR_IMAGE prompt，使用默认 prompt");
                prompt = "默认 prompt（见 callDeepSeekAPIForOrder 方法）";
            }
            
            String parsedResult = callDeepSeekApi(ocrTextContent, depId, disId);
            if (parsedResult == null || parsedResult.trim().isEmpty()) {
                logger.error("[processOrderRecognitionAsync] DeepSeek解析结果为空，任务ID: {}", ocrTaskId);
                ocrTask.setNxOcrTaskStatus(-1); // 3=失败
                ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
                nxOcrTaskService.update(ocrTask);
                return;
            }
            
            // 解析DeepSeek返回的JSON（使用 com.alibaba.fastjson.JSON）
            List<Map<String, Object>> itemsList = new ArrayList<>();
            try {
                logger.info("[processOrderRecognitionAsync] 开始解析 DeepSeek 返回的 JSON，内容长度: {} 字符", parsedResult.length());
                logger.info("[processOrderRecognitionAsync] DeepSeek 返回内容的前500字符: {}", 
                        parsedResult.length() > 500 ? parsedResult.substring(0, 500) : parsedResult);
                
                com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(parsedResult);
                logger.info("[processOrderRecognitionAsync] JSON 解析成功，JSON 对象的所有键: {}", jsonObject.keySet());
                
                // 支持两种字段名：items 和 orderItems
                com.alibaba.fastjson.JSONArray itemsArray = jsonObject.getJSONArray("items");
                if (itemsArray == null) {
                    itemsArray = jsonObject.getJSONArray("orderItems");
                    logger.info("[processOrderRecognitionAsync] 未找到 'items' 字段，尝试使用 'orderItems' 字段");
                }
                logger.info("[processOrderRecognitionAsync] items/orderItems 数组是否存在: {}, 数组长度: {}", 
                        itemsArray != null, itemsArray != null ? itemsArray.size() : 0);
                
                if (itemsArray != null && itemsArray.size() > 0) {
                    for (int i = 0; i < itemsArray.size(); i++) {
                        com.alibaba.fastjson.JSONObject itemObj = itemsArray.getJSONObject(i);
                        Map<String, Object> item = new HashMap<>();
                        // 将 fastjson JSONObject 转换为 Map
                        for (String key : itemObj.keySet()) {
                            item.put(key, itemObj.get(key));
                        }
                        itemsList.add(item);
                        logger.info("[processOrderRecognitionAsync] 解析到商品[{}]: name={}, quantity={}, spec={}", 
                                i, item.get("name"), item.get("quantity"), item.get("spec"));
                    }
                } else {
                    logger.warn("[processOrderRecognitionAsync] items 数组为空或不存在，尝试检查 JSON 结构");
                    // 尝试输出完整的 JSON 内容用于调试
                    logger.info("[processOrderRecognitionAsync] 完整的 JSON 内容: {}", jsonObject.toJSONString());
                }
                
                // 异步处理数量和规格字段映射
                for (Map<String, Object> item : itemsList) {
                    String quantity = item.get("quantity") != null ? item.get("quantity").toString().trim() : "";
                    String spec = item.get("spec") != null ? item.get("spec").toString().trim() : "";

                    // 手写体识别：如果规格是 "h"，转换为 "个"
                    if ("h".equalsIgnoreCase(spec)) {
                        spec = "个";
                        item.put("spec", spec);
                        item.put("specIsChange", true);
                        logger.info("[recognizeOrder] 手写体识别：规格 'h' 转换为 '个'");
                    }

                    // 如果规格为空，并且数量是2位数或3位数
                    if (spec.isEmpty() && quantity.matches("^\\d{2,3}$")) {
                        String newQuantity;
                        if (quantity.length() == 2) {
                            // 2位数：取第1位作为数量，规格设为"个"
                            newQuantity = quantity.substring(0, 1);
                        } else {
                            // 3位数：取前2位作为数量，规格设为"个"
                            newQuantity = quantity.substring(0, 2);
                        }
                        item.put("quantity", newQuantity);
                        item.put("spec", "");
                        item.put("quantityIsChange", true);
                        item.put("specIsChange", true);
                        logger.info("[recognizeOrder] 拆分{}位数数量: quantity={} -> quantity={}, spec={}",
                                quantity.length(), quantity, newQuantity, "个");
                        // 更新变量以便后续使用
                        quantity = newQuantity;
                        spec = "";
                    }

                    // 字段映射：将 DeepSeek 返回的字段映射到前端期望的字段格式
                    // DeepSeek 返回：quantity, spec
                    // 前端期望：qty, unit
                    // 若 DeepSeek 未返回 originalText，则构建完整 OCR 原文（商品名+数量+规格）
                    // rawName 可能是完整行（如「西红柿15斤」）或仅商品名（如「大白菜」），需判断是否已含 quantity+spec
                    if (item.get("originalText") == null || item.get("originalText").toString().trim().isEmpty()) {
                        String rn = item.get("rawName") != null ? item.get("rawName").toString().trim() : "";
                        String rnNorm = (rn != null ? rn.replaceAll("\\s+", "") : "");
                        String qsSuffix = (quantity != null ? quantity.trim() : "") + (spec != null ? spec.trim() : "");
                        String qsSuffixNorm = qsSuffix.replaceAll("\\s+", "");
                        // 若 rawName 已以 quantity+spec 结尾（如「西红柿15斤」），则直接使用；否则拼接
                        boolean rawNameAlreadyHasQtySpec = !qsSuffixNorm.isEmpty() && rnNorm.endsWith(qsSuffixNorm);
                        if (rawNameAlreadyHasQtySpec) {
                            item.put("originalText", rn);
                        } else {
                            StringBuilder sb = new StringBuilder();
                            String base = !rn.isEmpty() ? rn : (item.get("name") != null ? item.get("name").toString().trim() : "");
                            if (!base.isEmpty()) sb.append(base);
                            if (!quantity.isEmpty()) { if (sb.length() > 0) sb.append(" "); sb.append(quantity); }
                            if (!spec.isEmpty()) { if (sb.length() > 0) sb.append(" "); sb.append(spec); }
                            item.put("originalText", sb.toString());
                        }
                    }
                    // 字段映射
                    item.put("qty", quantity);
                    item.put("unit", spec);
                    item.put("remark", item.get("note") != null ? item.get("note").toString().trim() : "");
                }
                
                logger.info("[processOrderRecognitionAsync] DeepSeek 解析成功，解析到 {} 个商品", itemsList.size());
            } catch (Exception e) {
                logger.error("[processOrderRecognitionAsync] DeepSeek 返回结果解析失败，任务ID: {}", ocrTaskId, e);
                ocrTask.setNxOcrTaskStatus(-1); // 3=失败
                ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
                nxOcrTaskService.update(ocrTask);
                return;
            }
            
            // 验证解析结果
            if (itemsList.isEmpty()) {
                logger.error("[processOrderRecognitionAsync] DeepSeek 解析结果为空，任务ID: {}", ocrTaskId);
                ocrTask.setNxOcrTaskStatus(-1); // 3=失败
                ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
                nxOcrTaskService.update(ocrTask);
                return;
            }
            
            // ========== 第三步：更新任务总订单数 ==========
            ocrTask.setNxOcrTaskTotalOrders(itemsList.size());
            ocrTask.setNxOcrTaskDistributerId(disId);
            ocrTask.setNxOcrTaskDepartmentId(depId);
            ocrTask.setNxOcrTaskDepartmentFatherId(depFatherId);
            nxOcrTaskService.update(ocrTask);
            logger.info("[processOrderRecognitionAsync] 更新OCR任务总订单数: {}", itemsList.size());
            
            // ========== 第四步：处理订单（只进行强查询） ==========
            // 先查询当前最大的 today_order
            int currentMaxOrder = 0;
            Map<String, Object> map = new HashMap<>();
            map.put("depFatherId", depId);
            Integer integer = nxDepartmentOrdersService.queryOrderGoodsCount(map);
            if(integer > 0){
                if (depId != null) {
                    currentMaxOrder = nxDepartmentOrdersService.queryMaxTodayOrder(depId);
                    logger.info("[processOrderRecognitionAsync] 当前部门最大 today_order: {}", currentMaxOrder);
                }
            }
            int todayOrderCounter = 0;
            
            int completedOrders = 0;
            int pendingOrders = 0;
            
            // 处理每条识别数据（只进行强查询）
            for (int i = 0; i < itemsList.size(); i++) {
                Map<String, Object> item = itemsList.get(i);
                
                // 订单中的商品名称使用 name（纠错后的名称）
                String goodsName = item.get("name") != null ? item.get("name").toString().trim() : "";
                // 训练数据使用 rawName（原始OCR识别的名称），如果没有 rawName 则使用 name
                String rawName = item.get("rawName") != null && !item.get("rawName").toString().trim().isEmpty()
                        ? item.get("rawName").toString().trim() : goodsName;
                String quantity = item.get("quantity") != null ? item.get("quantity").toString().trim() : "";
                String spec = item.get("spec") != null ? item.get("spec").toString().trim() : "";
                Boolean quantityIsChange = item.get("quantityIsChange") != null 
                        ? (Boolean) item.get("quantityIsChange") : false;
                Boolean specIsChange = item.get("specIsChange") != null 
                        ? (Boolean) item.get("specIsChange") : false;
                String standardWeight = item.get("standardWeight") != null ? item.get("standardWeight").toString().trim() : "";
                String itemUnit = item.get("itemUnit") != null ? item.get("itemUnit").toString().trim() : "";
                String itemsPerCarton = item.get("itemsPerCarton") != null ? item.get("itemsPerCarton").toString().trim() : "";
                String cartonUnit = item.get("cartonUnit") != null ? item.get("cartonUnit").toString().trim() : "";
                String note = item.get("note") != null ? item.get("note").toString().trim() : "";
                String isNotice = item.get("isNotice") != null ? item.get("isNotice").toString().trim() : "";
                // OCR原始文本，去除所有空格以便后续匹配
                String originalText = item.get("originalText") != null 
                        ? item.get("originalText").toString().trim().replaceAll("\\s+", "") : "";
                // rawName 归一化（去空格），用于训练数据 goodsName 查询，使「西红柿 15斤」与「西红柿15斤」可互匹配
                String rawNameNorm = (rawName != null ? rawName.trim().replaceAll("\\s+", "") : "");
                if (rawNameNorm.isEmpty()) rawNameNorm = rawName;
                
                if (goodsName.isEmpty()) {
                    logger.warn("[processOrderRecognitionAsync] 跳过商品：名称为空，任务ID: {}", ocrTaskId);
                    continue;
                }
                
                // 创建基本订单实体
                NxDepartmentOrdersEntity orderBasic = new NxDepartmentOrdersEntity();
                orderBasic.setNxDoDepartmentId(depId);
                orderBasic.setNxDoDepartmentFatherId(depFatherId);
                orderBasic.setNxDoDistributerId(disId);
                orderBasic.setNxDoOrderUserId(userId);
                orderBasic.setNxDoGoodsName(goodsName);
                orderBasic.setNxDoGoodsOriginalName(goodsName);
                orderBasic.setNxDoRemark(note);
                orderBasic.setOrcNotice(isNotice);
                orderBasic.setNxDoPurchaseUserId(-1);
                orderBasic.setNxDoIsAgent(-1);
                orderBasic.setNxDoQuantity(quantity);
                orderBasic.setNxDoStandard(spec);
                orderBasic.setNxDoOcrTaskId(ocrTaskId);
                // 设置包装结构字段
                orderBasic.setStandardWeight(standardWeight);
                orderBasic.setItemUnit(itemUnit);
                orderBasic.setItemsPerCarton(itemsPerCarton);
                orderBasic.setCartonUnit(cartonUnit);
                // 预先设置 nxDoTodayOrder
                int todayOrder = currentMaxOrder + todayOrderCounter + 1;
                orderBasic.setNxDoTodayOrder(todayOrder);
                todayOrderCounter++;
                
                boolean orderSaved = false;
                
                // 判断是否缺少数量或规格（3个必备条件：商品名称、数量、规格）
                boolean isDataMissing = (quantity.trim().isEmpty()) && (spec.trim().isEmpty());
                
                // 优先按 nx_otd_ocr_text 匹配（无论是否 isDataMissing 都先尝试）：条件 originalText 长度至少 4 字符
                if (originalText != null && originalText.length() >= 4) {
                    Map<String, Object> traGoodsMapOcr = new HashMap<>();
                    traGoodsMapOcr.put("depId", depId);
                    traGoodsMapOcr.put("otdOcrText", originalText);
                    traGoodsMapOcr.put("disGoodsId", 1);
                    logger.info("[processOrderRecognitionAsync] 按nx_otd_ocr_text查询（含isDataMissing）: originalText='{}', depId={}", originalText, depId);
                    NxOrderOcrTrainingDataEntity dataEntityOcr = nxOrderOcrTrainingDataService.queryByMatchFields(traGoodsMapOcr);
                    if (dataEntityOcr != null && dataEntityOcr.getNxOtdDisGoodsId() != null) {
                        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(dataEntityOcr.getNxOtdDisGoodsId());
                        if (distributerGoodsEntity != null) {
                            logger.info("[processOrderRecognitionAsync] nx_otd_ocr_text匹配命中: 训练数据ID={}, 商品ID={}", dataEntityOcr.getNxOtdId(), dataEntityOcr.getNxOtdDisGoodsId());
                            String finalGoodsName = dataEntityOcr.getNxOtdFinalGoodsName();
                            String orderGoodsName = (finalGoodsName != null && !finalGoodsName.trim().isEmpty()) ? finalGoodsName : goodsName;
                            orderBasic.setNxDoGoodsName(orderGoodsName);
                            orderBasic.setNxDoStatus(0);
                            String finalQty = dataEntityOcr.getNxOtdFinalQuantity();
                            if (finalQty != null && !finalQty.trim().isEmpty()) {
                                orderBasic.setNxDoQuantity(finalQty);
                            }
                            orderBasic.setNxDoStandard(dataEntityOcr.getNxOtdFinalStandard());
                            orderBasic.setStandardWeight(dataEntityOcr.getNxOtdFinalStandardWeight());
                            orderBasic.setNxDoRemark(dataEntityOcr.getNxOtdFinalRemark());
                            NxDepartmentOrdersEntity savedOrder = saveOrderWithGoodsForAsync(orderBasic, distributerGoodsEntity);
                            orderSaved = true;
                            if (savedOrder.getNxDoStatus() != null && savedOrder.getNxDoStatus() == -2) {
                                pendingOrders++;
                            } else {
                                completedOrders++;
                            }
                            continue;
                        }
                    }
                }
                
                // 如果订单缺少数量或规格，创建训练数据并保存订单
                if (isDataMissing) {
                    logger.info("[processOrderRecognitionAsync] 订单缺少数量或规格，创建训练数据并保存订单: goodsName={}, quantity={}, spec={}", goodsName, quantity, spec);
                    NxOrderOcrTrainingDataEntity trainingData = createOrQueryTrainingData(
                            depId, depFatherId, disId, rawName, goodsName, quantity, spec, standardWeight, note, originalText, userId);
                    orderBasic.setNxDoStatus(-2);
                    orderBasic.setNxDoTrainingDataId(trainingData.getNxOtdId());
                    orderBasic.setNxDoDisGoodsId(null);
                    saveOrderWithoutGoods(orderBasic);
                    pendingOrders++;
                    continue;
                }
                
                // 0级匹配：按 nx_otd_ocr_text 查询
                Map<String, Object> traGoodsMap = new HashMap<>();
                traGoodsMap.put("depId", depId);
                traGoodsMap.put("otdOcrText", originalText);
                traGoodsMap.put("disGoodsId", 1);
                NxOrderOcrTrainingDataEntity dataEntity = nxOrderOcrTrainingDataService.queryByMatchFields(traGoodsMap);
                if (dataEntity != null && dataEntity.getNxOtdDisGoodsId() != null) {
                    NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(dataEntity.getNxOtdDisGoodsId());
                    if (distributerGoodsEntity != null) {
                        logger.info("[processOrderRecognitionAsync] 0级匹配命中: nx_otd_ocr_text='{}', 训练数据ID={}", dataEntity.getNxOtdOcrText(), dataEntity.getNxOtdId());
                        orderBasic.setNxDoStatus(0);
                        orderBasic.setNxDoStandard(dataEntity.getNxOtdFinalStandard());
                        orderBasic.setStandardWeight(dataEntity.getNxOtdFinalStandardWeight());
                        orderBasic.setNxDoRemark(dataEntity.getNxOtdFinalRemark());
                        NxDepartmentOrdersEntity savedOrder = saveOrderWithGoodsForAsync(orderBasic, distributerGoodsEntity);
                        orderSaved = true;
                        if (savedOrder.getNxDoStatus() != null && savedOrder.getNxDoStatus() == -2) {
                            pendingOrders++;
                        } else {
                            completedOrders++;
                        }
                        continue;
                    }
                }
                
                // 1级优先匹配：部门商品历史记录（用 goodsName，部门历史存的是商品名如「西红柿」而非完整行「西红柿 15斤」）
                Map<String, Object> depGoodsMap = new HashMap<>();
                depGoodsMap.put("disId", disId);
                depGoodsMap.put("depId", depId);
                depGoodsMap.put("name", goodsName);
                depGoodsMap.put("standard", spec);
                List<NxDepartmentDisGoodsEntity> departmentDisGoodsList = nxDepartmentDisGoodsService.queryDepartmentGoods(depGoodsMap);
                if (departmentDisGoodsList.size() == 1) {
                    NxDepartmentDisGoodsEntity departmentDisGoodsEntity = departmentDisGoodsList.get(0);
                    NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(departmentDisGoodsEntity.getNxDdgDisGoodsId());
                    
                    // 根据数量和规格是否改变设置订单状态
                    setOrderStatusByChangeFlag(orderBasic, specIsChange, quantityIsChange);
                    // 保存订单（不查询推荐商品）
                    NxDepartmentOrdersEntity savedOrder = saveOrderWithGoodsForAsync(orderBasic, distributerGoodsEntity);
                    orderSaved = true;
                    // 检查保存后的订单状态，如果状态是-2，应该计入pendingOrders而不是completedOrders
                    if (savedOrder.getNxDoStatus() != null && savedOrder.getNxDoStatus() == -2) {
                        pendingOrders++;
                        logger.info("[processOrderRecognitionAsync] 1级匹配成功，但订单状态为-2（待修正），任务ID: {}, 商品名称: {}", ocrTaskId, goodsName);
                    } else {
                        completedOrders++;
                        logger.info("[processOrderRecognitionAsync] 1级匹配成功，订单已保存，任务ID: {}, 商品名称: {}", ocrTaskId, goodsName);
                    }
                }
                
                // 2级优先匹配：商品库名称查询（用 goodsName，商品库存的是标准名如「西红柿」而非完整行「西红柿 15斤」）
                if (!orderSaved) {
                    Map<String, Object> mapZero = new HashMap<>();
                    mapZero.put("disId", disId);
                    mapZero.put("searchStr", goodsName);
                    mapZero.put("standard", spec);
                    List<NxDistributerGoodsEntity> distributerGoodsEntitiesZero = nxDistributerGoodsService.queryDisGoodsByName(mapZero);
                    if (distributerGoodsEntitiesZero.size() == 1) {
                        NxDistributerGoodsEntity distributerGoodsEntity = distributerGoodsEntitiesZero.get(0);
                        
                        // 根据数量和规格是否改变设置订单状态
                        setOrderStatusByChangeFlag(orderBasic, specIsChange, quantityIsChange);
                        // 保存订单（不查询推荐商品）
                        NxDepartmentOrdersEntity savedOrder = saveOrderWithGoodsForAsync(orderBasic, distributerGoodsEntity);
                        orderSaved = true;
                        // 检查保存后的订单状态，如果状态是-2，应该计入pendingOrders而不是completedOrders
                        if (savedOrder.getNxDoStatus() != null && savedOrder.getNxDoStatus() == -2) {
                            pendingOrders++;
                            logger.info("[processOrderRecognitionAsync] 2级匹配成功，但订单状态为-2（待修正），任务ID: {}, 商品名称: {}", ocrTaskId, goodsName);
                        } else {
                            completedOrders++;
                            logger.info("[processOrderRecognitionAsync] 2级匹配成功，订单已保存，任务ID: {}, 商品名称: {}", ocrTaskId, goodsName);
                        }
                    }
                }
                
                // 3级优先匹配：商品库别名查询
                if (!orderSaved) {
                    Map<String, Object> mapA = new HashMap<>();
                    mapA.put("disId", disId);
                    mapA.put("alias", goodsName);
                    mapA.put("standard", spec);
                    List<NxDistributerGoodsEntity> distributerGoodsEntitiesAlias = nxDistributerGoodsService.queryDisGoodsByAlias(mapA);
                    if (distributerGoodsEntitiesAlias.size() == 1) {
                        NxDistributerGoodsEntity distributerGoodsEntity = distributerGoodsEntitiesAlias.get(0);
                        
                        // 根据数量和规格是否改变设置订单状态
                        setOrderStatusByChangeFlag(orderBasic, specIsChange, quantityIsChange);
                        // 保存订单（不查询推荐商品）
                        NxDepartmentOrdersEntity savedOrder = saveOrderWithGoodsForAsync(orderBasic, distributerGoodsEntity);
                        orderSaved = true;
                        // 检查保存后的订单状态，如果状态是-2，应该计入pendingOrders而不是completedOrders
                        if (savedOrder.getNxDoStatus() != null && savedOrder.getNxDoStatus() == -2) {
                            pendingOrders++;
                            logger.info("[processOrderRecognitionAsync] 3级匹配成功，但订单状态为-2（待修正），任务ID: {}, 商品名称: {}", ocrTaskId, goodsName);
                        } else {
                            completedOrders++;
                            logger.info("[processOrderRecognitionAsync] 3级匹配成功，订单已保存，任务ID: {}, 商品名称: {}", ocrTaskId, goodsName);
                        }
                    }
                }
                
                // 4级优先匹配：训练数据查询（用 rawNameNorm 归一化匹配）
                if (!orderSaved) {
                    Map<String, Object> matchParams = new HashMap<>();
                    matchParams.put("depId", depId);
                    matchParams.put("goodsName", rawNameNorm);
                    matchParams.put("disGoodsId", 1);
                    NxOrderOcrTrainingDataEntity matchedTrainingData = nxOrderOcrTrainingDataService.queryByMatchFields(matchParams);
                    
                    if (matchedTrainingData != null && matchedTrainingData.getNxOtdDisGoodsId() != null) {
                        NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(
                                matchedTrainingData.getNxOtdDisGoodsId());
                        if (disGoodsEntity == null) {
                            logger.error("[processOrderRecognitionAsync] 商品不存在，商品ID: {}, 任务ID: {}",
                                    matchedTrainingData.getNxOtdDisGoodsId(), ocrTaskId);
                        } else {
                            // 优先使用训练数据的 final_goods_name（如果存在）
                            String finalGoodsName = matchedTrainingData.getNxOtdFinalGoodsName();
                            String orderGoodsName = (finalGoodsName != null && !finalGoodsName.trim().isEmpty()) 
                                    ? finalGoodsName : goodsName;
                            if (!orderGoodsName.equals(goodsName)) {
                                logger.info("[processOrderRecognitionAsync] 使用训练数据的 final_goods_name='{}' 替代原始名称='{}'", 
                                        orderGoodsName, goodsName);
                            }
                            orderBasic.setNxDoGoodsName(orderGoodsName);
                            
                            // 根据数量和规格是否改变设置订单状态
                            setOrderStatusByChangeFlag(orderBasic, specIsChange, quantityIsChange);
                            // 保存订单（不查询推荐商品）
                            NxDepartmentOrdersEntity savedOrder = saveOrderWithGoodsForAsync(orderBasic, disGoodsEntity);
                            orderSaved = true;
                            // 检查保存后的订单状态，如果状态是-2，应该计入pendingOrders而不是completedOrders
                            if (savedOrder.getNxDoStatus() != null && savedOrder.getNxDoStatus() == -2) {
                                pendingOrders++;
                                logger.info("[processOrderRecognitionAsync] 4级匹配成功，但订单状态为-2（待修正），任务ID: {}, 商品名称: {}", ocrTaskId, goodsName);
                            } else {
                                completedOrders++;
                                logger.info("[processOrderRecognitionAsync] 4级匹配成功，订单已保存，任务ID: {}, 商品名称: {}", ocrTaskId, goodsName);
                            }
                        }
                    }
                }
                
                // 5级匹配：按 nx_otd_ocr_text 兜底查询
                if (!orderSaved) {
                    Map<String, Object> matchParamsText = new HashMap<>();
                    matchParamsText.put("depId", depId);
                    matchParamsText.put("disGoodsId", 1);
                    matchParamsText.put("otdOcrText", originalText);
                    NxOrderOcrTrainingDataEntity matchedTrainingDataText = nxOrderOcrTrainingDataService.queryByMatchFields(matchParamsText);
                    if (matchedTrainingDataText != null && matchedTrainingDataText.getNxOtdDisGoodsId() != null) {
                        NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(matchedTrainingDataText.getNxOtdDisGoodsId());
                        if (disGoodsEntity != null) {
                            logger.info("[processOrderRecognitionAsync] 5级匹配命中: nx_otd_ocr_text='{}', 训练数据ID={}", matchedTrainingDataText.getNxOtdOcrText(), matchedTrainingDataText.getNxOtdId());
                            String finalGoodsName = matchedTrainingDataText.getNxOtdFinalGoodsName();
                            String orderGoodsName = (finalGoodsName != null && !finalGoodsName.trim().isEmpty()) ? finalGoodsName : goodsName;
                            orderBasic.setNxDoGoodsName(orderGoodsName);
                            orderBasic.setNxDoStatus(0);
                            String finalQty = matchedTrainingDataText.getNxOtdFinalQuantity();
                            if (finalQty != null && !finalQty.trim().isEmpty()) {
                                orderBasic.setNxDoQuantity(finalQty);
                            }
                            orderBasic.setNxDoStandard(matchedTrainingDataText.getNxOtdFinalStandard());
                            orderBasic.setStandardWeight(matchedTrainingDataText.getNxOtdFinalStandardWeight());
                            orderBasic.setNxDoRemark(matchedTrainingDataText.getNxOtdFinalRemark());
                            NxDepartmentOrdersEntity savedOrder = saveOrderWithGoodsForAsync(orderBasic, disGoodsEntity);
                            orderSaved = true;
                            if (savedOrder.getNxDoStatus() != null && savedOrder.getNxDoStatus() == -2) {
                                pendingOrders++;
                            } else {
                                completedOrders++;
                            }
                            continue;
                        }
                    }
                }
                
                // 如果所有强查询都失败，创建训练数据并保存订单（状态=-2，商品ID=null）
                if (!orderSaved) {
                    logger.info("[processOrderRecognitionAsync] 所有强查询都失败，创建训练数据并保存订单，任务ID: {}, rawName: {}, name: {}", 
                            ocrTaskId, rawName, goodsName);
                    
                    // 创建训练数据
                    NxOrderOcrTrainingDataEntity trainingData = createOrQueryTrainingData(
                            depId, depFatherId, disId, rawName, goodsName, quantity, spec, standardWeight, note, originalText, userId);
                    logger.info("[processOrderRecognitionAsync] 训练数据创建成功，训练数据ID: {}, 任务ID: {}", trainingData.getNxOtdId(), ocrTaskId);
                    
                    // 保存订单（状态=-2，商品ID=null，关联训练数据ID）
                    orderBasic.setNxDoStatus(-2);
                    orderBasic.setNxDoTrainingDataId(trainingData.getNxOtdId());
                    orderBasic.setNxDoDisGoodsId(null); // 不创建临时商品，商品ID为null
                    saveOrderWithoutGoods(orderBasic);
                    logger.info("[processOrderRecognitionAsync] 订单已保存（无商品ID），任务ID: {}, 商品名称: {}, 订单ID: {}", 
                            ocrTaskId, goodsName, orderBasic.getNxDepartmentOrdersId());
                    
                    pendingOrders++;
                }
            }
            
            // ========== 第五步：更新任务状态 ==========
            ocrTask.setNxOcrTaskCompletedOrders(completedOrders);
            ocrTask.setNxOcrTaskPendingOrders(pendingOrders);
            if (pendingOrders == 0) {
                ocrTask.setNxOcrTaskStatus(2); // 2=已完成
            } else if (completedOrders > 0) {
                ocrTask.setNxOcrTaskStatus(1); // 1=部分完成
            } else {
                ocrTask.setNxOcrTaskStatus(1); // 1=部分完成（所有订单都是待修正）
            }
            ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
            nxOcrTaskService.update(ocrTask);
            
            logger.info("[processOrderRecognitionAsync] 异步处理完成，任务ID: {}, 已完成订单: {}, 待修正订单: {}", 
                    ocrTaskId, completedOrders, pendingOrders);
            
        } catch (Exception e) {
            logger.error("[processOrderRecognitionAsync] 异步处理订单识别失败，任务ID: {}", ocrTaskId, e);
            // 更新任务状态为失败
            try {
                ocrTask.setNxOcrTaskStatus(3); // 3=失败
                ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
                nxOcrTaskService.update(ocrTask);
            } catch (Exception updateException) {
                logger.error("[processOrderRecognitionAsync] 更新任务状态失败", updateException);
            }
        }
    }

    /**
     * 保存订单（简化版，用于异步处理，不查询推荐商品）
     * 
     * @param orderBasic 订单实体
     * @param distributerGoodsEntity 分销商商品实体
     * @return 保存后的订单实体
     */
    private NxDepartmentOrdersEntity saveOrderWithGoodsForAsync(
            NxDepartmentOrdersEntity orderBasic, NxDistributerGoodsEntity distributerGoodsEntity) {
        // 复用 saveOrderWithGoods 方法，但不调用 addCommentsGoodsForOrder
        return nxDepartmentOrdersService.saveOrderWithGoods(orderBasic, distributerGoodsEntity);
    }

    /**
     * 执行 OCR 识别，将图片 Base64 转换为文本内容
     * 
     * @param imageBase64 图片的 Base64 编码字符串（不含 data:image 前缀）
     * @return OCR 识别后的文本内容（按行组织）
     */
    private String performOcrRecognition(String imageBase64) {
        try {
            // 处理 Base64 前缀（如果前端传了 data:image 前缀，需要去掉）
            if (imageBase64.contains(",")) {
                imageBase64 = imageBase64.substring(imageBase64.indexOf(",") + 1);
            }

            // 验证图片大小
            if (imageBase64.length() > MAX_IMAGE_BASE64_SIZE) {
                logger.warn("[performOcrRecognition] 图片过大: {} bytes (限制: {} bytes)", 
                    imageBase64.length(), MAX_IMAGE_BASE64_SIZE);
                throw new RuntimeException("图片大小超过限制，最大支持 10MB（Base64 编码后）");
            }

            logger.info("[performOcrRecognition] 开始 OCR 识别，图片大小: {} bytes", imageBase64.length());

            // 使用注入的单例 OCR 客户端
            GeneralAccurateOCRRequest req = new GeneralAccurateOCRRequest();
            req.setImageBase64(imageBase64);

            // 调用 OCR API（带重试机制）
            GeneralAccurateOCRResponse resp = null;
            int maxRetries = 3;
            Exception lastException = null;
            long ocrStartTime = System.currentTimeMillis();

            for (int retryCount = 0; retryCount < maxRetries; retryCount++) {
                try {
                    logger.info("[performOcrRecognition] 尝试调用 OCR API (第 {} 次)", retryCount + 1);
                    long ocrCallStartTime = System.currentTimeMillis();
                    resp = ocrClient.GeneralAccurateOCR(req);
                    long ocrCallElapsedTime = System.currentTimeMillis() - ocrCallStartTime;
                    logger.info("[performOcrRecognition] OCR API 调用成功，耗时: {} ms ({} 秒)", ocrCallElapsedTime, ocrCallElapsedTime / 1000.0);
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
                        if (retryCount < maxRetries - 1) {
                            logger.warn("[performOcrRecognition] OCR API 调用失败，准备重试 (第 {} 次): {}", retryCount + 1, errorMessage);
                            try {
                                Thread.sleep(1000 * (retryCount + 1)); // 等待后重试，递增等待时间
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                break; // 中断时跳出循环
                            }
                            continue; // 继续重试
                        } else {
                            logger.error("[performOcrRecognition] OCR API 调用失败，已达到最大重试次数: {}", errorMessage);
                            break; // 达到最大重试次数，跳出循环，让后续代码处理
                        }
                    } else {
                        // 非网络错误，直接抛出异常（这会跳出循环）
                        throw e;
                    }
                }
            }

            // 如果循环结束后仍然没有成功响应，抛出异常
            if (resp == null && lastException != null) {
                throw lastException;
            }

            // 记录 OCR 总耗时（包括重试）
            long ocrTotalTime = System.currentTimeMillis() - ocrStartTime;
            logger.info("[performOcrRecognition] OCR 总耗时（包括重试）: {} ms ({} 秒)", ocrTotalTime, ocrTotalTime / 1000.0);

            // 获取识别结果
            TextDetection[] textDetections = resp.getTextDetections();
            int textCount = textDetections != null ? textDetections.length : 0;
            logger.info("[performOcrRecognition] OCR识别成功，识别到 {} 条文本", textCount);

            if (textDetections == null || textDetections.length == 0) {
                logger.warn("[performOcrRecognition] 未识别到任何文本");
                return "";
            }

            // 打印识别到的所有文本内容
            if (textDetections != null && textDetections.length > 0) {
                logger.info("========== [performOcrRecognition] OCR 识别结果 ==========");
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
                    logger.info("[performOcrRecognition] 过滤噪声: {} (置信度: {})", trimmedText, confidence);
                    continue;
                }
                if (isIrrelevantText(trimmedText)) {
                    logger.info("[performOcrRecognition] 过滤无关文本: {} (置信度: {})", trimmedText, confidence);
                    continue;
                }
                
                // 放宽置信度阈值：只过滤极低置信度的文本（低于50%），保留商品名
                // 注意：商品名即使置信度较低（如 57-80）也应该保留，让后续聚类和解析处理
                if (confidence != null && confidence < 50) {
                    logger.info("[performOcrRecognition] 过滤极低置信度文本: {} (置信度: {})", trimmedText, confidence);
                    continue;
                }
                
                // 获取坐标信息
                Coord[] polygon = detection.getPolygon();
                if (polygon == null || polygon.length == 0) {
                    logger.debug("[performOcrRecognition] 跳过无坐标文本: {}", trimmedText);
                    continue;
                }
                
                boxes.add(new OcrBox(trimmedText, confidence != null ? confidence : 0, polygon));
            }
            
            logger.info("[performOcrRecognition] 有效 OCR 框数量: {}", boxes.size());
            
            // 打印所有有效框的详细信息（用于调试）
            for (int i = 0; i < boxes.size(); i++) {
                OcrBox box = boxes.get(i);
                logger.info("[performOcrRecognition] 有效框[{}]: text={}, conf={}, cx={}, cy={}, height={}", 
                    i + 1, box.text, box.conf, box.cx, box.cy, box.height);
            }
            
            // 按 y 坐标聚类成行
            List<List<OcrBox>> rows = groupByRows(boxes);
            logger.info("[performOcrRecognition] 聚类后行数: {}", rows.size());
            
            // 打印每一行的详细内容（用于调试）
            for (int i = 0; i < rows.size(); i++) {
                List<OcrBox> row = rows.get(i);
                StringBuilder rowText = new StringBuilder();
                for (OcrBox box : row) {
                    rowText.append("[").append(box.text).append("(y=").append(box.cy).append(")] ");
                }
                logger.info("[performOcrRecognition] Row#{}: {}", i + 1, rowText.toString().trim());
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
            logger.info("[performOcrRecognition] 构建的 OCR 文本（按行组织）:\n{}", ocrTextContent);
            
            return ocrTextContent;
            
        } catch (Exception e) {
            logger.error("[performOcrRecognition] OCR 识别失败: {}", e.getMessage(), e);
            throw new RuntimeException("OCR 识别失败: " + e.getMessage(), e);
        }
    }

    /**
     * 调用 DeepSeek API 解析订单文本
     * 
     * @param ocrTextContent OCR 识别后的文本内容
     * @param depId 部门ID（用于获取部门特定的修正规则）
     * @param disId 分销商ID（暂未使用）
     * @return DeepSeek 解析后的 JSON 字符串
     */
    private String callDeepSeekApi(String ocrTextContent, Integer depId, Integer disId) {
        try {
            // 获取部门特定的修正规则
            String departmentPrompt = null;
            logger.info("[callDeepSeekApi] 开始获取部门特定修正规则，depId: {}", depId);
            if (depId != null && depId > 0) {
                NxDepartmentEntity department = nxDepartmentService.queryObject(depId);
                if (department != null) {
                    // 图片识别使用 image prompt
                    String prompt = department.getNxDepartmentOcrPromptImage();
                    if (prompt != null && !prompt.trim().isEmpty()) {
                        departmentPrompt = prompt.trim();
                        logger.info("[callDeepSeekApi] ✅ 获取到部门图片修正规则，部门ID: {}, 规则长度: {} 字符", 
                                depId, departmentPrompt.length());
//                        logger.debug("[callDeepSeekApi] 部门修正规则内容: {}", departmentPrompt);
                    } else {
                        logger.info("[callDeepSeekApi] 部门ID: {} 存在，但图片 OCR prompt 为空", depId);
                    }
                } else {
                    logger.warn("[callDeepSeekApi] 未找到部门信息，部门ID: {}", depId);
                }
            }
            
            // 调用 DeepSeek API 解析订单
            logger.info("[callDeepSeekApi] ========== 开始调用 DeepSeek API 解析订单 ==========");
            logger.info("[callDeepSeekApi] OCR 文本长度: {} 字符", ocrTextContent != null ? ocrTextContent.length() : 0);
            String parsedResult;
            long deepSeekStartTime = System.currentTimeMillis();
            try {
                parsedResult = callDeepSeekAPIForOrder(ocrTextContent, departmentPrompt);
                long deepSeekElapsedTime = System.currentTimeMillis() - deepSeekStartTime;
                logger.info("[callDeepSeekApi] ========== DeepSeek API 调用完成 ==========");
                logger.info("[callDeepSeekApi] DeepSeek 耗时: {} ms ({} 秒)", deepSeekElapsedTime, deepSeekElapsedTime / 1000.0);
                logger.info("[callDeepSeekApi] 返回结果长度: {} 字符", parsedResult != null ? parsedResult.length() : 0);
                logger.info("[callDeepSeekApi] ================================================");
                return parsedResult;
            } catch (Exception e) {
                long deepSeekElapsedTime = System.currentTimeMillis() - deepSeekStartTime;
                logger.error("[callDeepSeekApi] ========== DeepSeek API 调用失败 ==========");
                logger.error("[callDeepSeekApi] DeepSeek 耗时: {} ms ({} 秒)", deepSeekElapsedTime, deepSeekElapsedTime / 1000.0);
                logger.error("[callDeepSeekApi] 错误信息: {}", e.getMessage(), e);
                logger.error("[callDeepSeekApi] ==========================================");
                throw new RuntimeException("DeepSeek API 调用失败: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            logger.error("[callDeepSeekApi] 调用 DeepSeek API 失败: {}", e.getMessage(), e);
            throw new RuntimeException("调用 DeepSeek API 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 部门订单商品名字段入库长度保护（与 {@link #NX_DO_GOODS_NAME_DB_MAX_CHARS} 一致）。
     */
    private void applyNxDepartmentOrderGoodsNameDbLimit(NxDepartmentOrdersEntity order) {
        if (order == null) {
            return;
        }
        String name = order.getNxDoGoodsName();
        if (name != null && name.length() > NX_DO_GOODS_NAME_DB_MAX_CHARS) {
            logger.warn("[applyNxDepartmentOrderGoodsNameDbLimit] nxDoGoodsName 过长 ({} 字符)，截断为 {} 字符",
                    name.length(), NX_DO_GOODS_NAME_DB_MAX_CHARS);
            order.setNxDoGoodsName(name.substring(0, NX_DO_GOODS_NAME_DB_MAX_CHARS));
        }
        String orig = order.getNxDoGoodsOriginalName();
        if (orig != null && orig.length() > NX_DO_GOODS_NAME_DB_MAX_CHARS) {
            logger.warn("[applyNxDepartmentOrderGoodsNameDbLimit] nxDoGoodsOriginalName 过长 ({} 字符)，截断为 {} 字符",
                    orig.length(), NX_DO_GOODS_NAME_DB_MAX_CHARS);
            order.setNxDoGoodsOriginalName(orig.substring(0, NX_DO_GOODS_NAME_DB_MAX_CHARS));
        }
    }

    /**
     * 保存订单（无商品ID，用于强查询失败的情况）
     * 
     * @param orderBasic 订单实体（已设置状态=-2，商品ID=null，关联训练数据ID）
     */
    private void saveOrderWithoutGoods(NxDepartmentOrdersEntity orderBasic) {
        // 设置订单的基本字段
        orderBasic.setNxDoArriveDate(formatWhatDate(0));
        orderBasic.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        orderBasic.setNxDoApplyDate(formatWhatDay(0));
        orderBasic.setNxDoArriveOnlyDate(formatWhatDate(0));
        orderBasic.setNxDoArriveWeeksYear(getWeekOfYear(0));
        orderBasic.setNxDoArriveDate(formatWhatDay(0));
        orderBasic.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        orderBasic.setNxDoApplyOnlyTime(formatWhatTime(0));
        orderBasic.setNxDoGbDistributerId(-1);
        orderBasic.setNxDoGbDepartmentFatherId(-1);
        orderBasic.setNxDoGbDepartmentId(-1);
        orderBasic.setNxDoNxCommunityId(-1);
        orderBasic.setNxDoNxCommRestrauntFatherId(-1);
        orderBasic.setNxDoNxCommRestrauntId(-1);
        orderBasic.setNxDoArriveWhatDay(getWeek(0));
        orderBasic.setNxDoCostPriceLevel("1");
        orderBasic.setNxDoPurchaseGoodsId(-1);
        orderBasic.setNxDoCollaborativeNxDisId(-1);
        
        // 设置 todayOrder（如果还没有设置）
        if (orderBasic.getNxDoTodayOrder() == null) {
            Map<String, Object> mapss = new HashMap<>();
            mapss.put("depId", orderBasic.getNxDoDepartmentId());
            mapss.put("status", 3);
            mapss.put("todayOrder", 1);
            int orderOrder = nxDepartmentOrdersDao.queryDepOrdersAcount(mapss);
            int todayOrder = orderOrder + 1;
            orderBasic.setNxDoTodayOrder(todayOrder);
            logger.info("[saveOrderWithoutGoods] 设置订单 todayOrder: 商品名称={}, todayOrder={}", 
                    orderBasic.getNxDoGoodsName(), todayOrder);
        }
        
        // 确保 ocrTaskId 已设置（如果还没有设置）
        if (orderBasic.getNxDoOcrTaskId() == null) {
            logger.warn("[saveOrderWithoutGoods] 警告：订单的 ocrTaskId 为空，订单可能未关联OCR任务");
        } else {
            logger.info("[saveOrderWithoutGoods] 订单关联OCR任务ID: {}", orderBasic.getNxDoOcrTaskId());
        }

        applyNxDepartmentOrderGoodsNameDbLimit(orderBasic);
        
        // 保存订单
        nxDepartmentOrdersDao.save(orderBasic);
        logger.info("[saveOrderWithoutGoods] 订单已保存（无商品ID），订单ID: {}, 商品名称: {}, OCR任务ID: {}", 
                orderBasic.getNxDepartmentOrdersId(), orderBasic.getNxDoGoodsName(), orderBasic.getNxDoOcrTaskId());
        
        // 更新训练数据的订单ID（保存订单后，训练数据可以关联到订单）
        if (orderBasic.getNxDepartmentOrdersId() != null && orderBasic.getNxDoTrainingDataId() != null) {
            try {
                NxOrderOcrTrainingDataEntity trainingData = nxOrderOcrTrainingDataService.queryObject(orderBasic.getNxDoTrainingDataId());
                if (trainingData != null && trainingData.getNxOtdOrderId() == null) {
                    trainingData.setNxOtdOrderId(orderBasic.getNxDepartmentOrdersId());
                    nxOrderOcrTrainingDataService.update(trainingData);
                    logger.info("[saveOrderWithoutGoods] 已更新训练数据的订单ID: 训练数据ID={}, 订单ID={}", 
                            trainingData.getNxOtdId(), orderBasic.getNxDepartmentOrdersId());
                }
            } catch (Exception e) {
                logger.warn("[saveOrderWithoutGoods] 更新训练数据订单ID失败: 训练数据ID={}, 订单ID={}, 错误: {}", 
                        orderBasic.getNxDoTrainingDataId(), orderBasic.getNxDepartmentOrdersId(), e.getMessage());
            }
        }
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
     * @return 识别结果和解析后的订单商品列表，如果提供了depId和disI，d，则返回查询和保存结果
     */
    @RequestMapping(value = "/recognizeOrder", method = RequestMethod.POST)
    @ResponseBody
    public R recognizeOrder(@RequestBody Map<String, Object> request) {
        // 记录整个方法开始时间
        long methodStartTime = System.currentTimeMillis();
        String ocrTextContent = null;
        try {
            // ========== 第一步：检查是否有任务ID，决定使用哪种模式 ==========
            Object taskIdObj = request.get("taskId");
            Integer taskId = null;
            if (taskIdObj != null) {
                try {
                    if (taskIdObj instanceof Number) {
                        taskId = ((Number) taskIdObj).intValue();
                    } else {
                        taskId = Integer.parseInt(taskIdObj.toString());
                    }
                } catch (Exception e) {
                    logger.warn("[recognizeOrder] 任务ID格式错误: {}", taskIdObj);
                }
            }
            
            // ========== 第二步：验证必填参数 ==========
            Integer depId, disId, depFatherId, userId;
            
            // 如果提供了任务ID，从任务中获取参数（如果请求中没有提供）
            if (taskId != null && taskId > 0) {
                logger.info("[recognizeOrder] 使用任务ID模式，任务ID: {}", taskId);
                
                // 查询任务记录
                NxOcrTaskEntity ocrTask = nxOcrTaskService.queryObject(taskId);
                if (ocrTask == null) {
                    logger.error("[recognizeOrder] 任务不存在，任务ID: {}", taskId);
                    return R.error("任务不存在，任务ID: " + taskId);
                }
                
                // 获取OCR文本
                ocrTextContent = ocrTask.getNxOcrTaskOcrText();
                if (ocrTextContent == null || ocrTextContent.trim().isEmpty()) {
                    logger.error("[recognizeOrder] 任务中OCR文本为空，任务ID: {}", taskId);
                    return R.error("任务中OCR文本为空，任务ID: " + taskId);
                }
                
                logger.info("[recognizeOrder] 从任务获取OCR文本，任务ID: {}, 文本长度: {} 字符", taskId, ocrTextContent.length());
                
                // 从任务中获取参数（如果请求中没有提供）
                try {
                    depId = getIntegerParam(request, "depId", false);
                    if (depId == null) {
                        depId = ocrTask.getNxOcrTaskDepartmentId();
                    }
                    
                    disId = getIntegerParam(request, "disId", false);
                    if (disId == null) {
                        disId = ocrTask.getNxOcrTaskDistributerId();
                    }
                    
                    depFatherId = getIntegerParam(request, "depFatherId", false);
                    if (depFatherId == null) {
                        depFatherId = ocrTask.getNxOcrTaskDepartmentFatherId();
                    }
                    
                    userId = getIntegerParam(request, "userId", false);
                    if (userId == null) {
                        userId = ocrTask.getNxOcrTaskUploadUserId();
                    }
                    
                    // 验证必填参数
                    if (depId == null || disId == null || depFatherId == null || userId == null) {
                        return R.error("缺少必填参数：depId、disId、depFatherId、userId（可从任务中获取或请求中提供）");
                    }
                } catch (IllegalArgumentException e) {
                    return R.error(e.getMessage());
                }
                
                logger.info("[recognizeOrder] 从任务获取参数：depId={}, disId={}, depFatherId={}, userId={}", 
                        depId, disId, depFatherId, userId);
                
                // 跳过OCR识别步骤，直接使用保存的OCR文本
            } else {
                // ========== 原有模式：从图片进行OCR识别 ==========
                logger.info("[recognizeOrder] 使用图片模式");
                
                try {
                    depId = getIntegerParam(request, "depId", true);
                    disId = getIntegerParam(request, "disId", true);
                    depFatherId = getIntegerParam(request, "depFatherId", true);
                    userId = getIntegerParam(request, "userId", true);
                } catch (IllegalArgumentException e) {
                    return R.error(e.getMessage());
                }
                
                logger.info("[recognizeOrder] 接收参数：depId={}, disId={}, depFatherId={}, userId={}", 
                        depId, disId, depFatherId, userId);
                
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
                Exception lastException = null;
                long ocrStartTime = System.currentTimeMillis();

                for (int retryCount = 0; retryCount < maxRetries; retryCount++) {
                    try {
                        logger.info("[recognizeOrder] 尝试调用 OCR API (第 {} 次)", retryCount + 1);
                        long ocrCallStartTime = System.currentTimeMillis();
                        resp = ocrClient.GeneralAccurateOCR(req);
                        long ocrCallElapsedTime = System.currentTimeMillis() - ocrCallStartTime;
                        logger.info("[recognizeOrder] OCR API 调用成功，耗时: {} ms ({} 秒)", ocrCallElapsedTime, ocrCallElapsedTime / 1000.0);
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
                            if (retryCount < maxRetries - 1) {
                                logger.warn("[recognizeOrder] OCR API 调用失败，准备重试 (第 {} 次): {}", retryCount + 1, errorMessage);
                                try {
                                    Thread.sleep(1000 * (retryCount + 1)); // 等待后重试，递增等待时间
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    break; // 中断时跳出循环
                                }
                                continue; // 继续重试
                            } else {
                                logger.error("[recognizeOrder] OCR API 调用失败，已达到最大重试次数: {}", errorMessage);
                                break; // 达到最大重试次数，跳出循环，让后续代码处理
                            }
                        } else {
                            // 非网络错误，直接抛出异常（这会跳出循环）
                            throw e;
                        }
                    }
                }

                // 如果循环结束后仍然没有成功响应，抛出异常
                if (resp == null && lastException != null) {
                    throw lastException;
                }

                // 记录 OCR 总耗时（包括重试）
                long ocrTotalTime = System.currentTimeMillis() - ocrStartTime;
                logger.info("[recognizeOrder] OCR 总耗时（包括重试）: {} ms ({} 秒)", ocrTotalTime, ocrTotalTime / 1000.0);

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

                //gpt1
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

                ocrTextContent = ocrText.toString().trim();
                logger.info("[recognizeOrder] 构建的 OCR 文本（按行组织）:\n{}", ocrTextContent);
            }
            
            // ========== 第三步：统一处理OCR文本，调用DeepSeek解析 ==========
            // 此时 ocrTextContent 已经准备好（要么从任务获取，要么从OCR识别得到）
            if (ocrTextContent == null || ocrTextContent.trim().isEmpty()) {
                logger.error("[recognizeOrder] OCR文本为空");
                return R.error("OCR文本为空");
            }
            
            // 获取部门特定的修正规则
            String departmentPrompt = null;
            logger.info("[recognizeOrder] 开始获取部门特定修正规则，depId: {}", depId);
            if (depId != null && depId > 0) {
                NxDepartmentEntity department = nxDepartmentService.queryObject(depId);
                if (department != null) {
                    // 图片识别使用 image prompt
                    String prompt = department.getNxDepartmentOcrPromptImage();
                    if (prompt != null && !prompt.trim().isEmpty()) {
                        departmentPrompt = prompt.trim();
                        logger.info("[recognizeOrder] ✅ 获取到部门图片修正规则，部门ID: {}, 规则长度: {} 字符", 
                                depId, departmentPrompt.length());
//                        logger.debug("[recognizeOrder] 部门修正规则内容: {}", departmentPrompt);
                    } else {
                        logger.info("[recognizeOrder] 部门ID: {} 存在，但图片 OCR prompt 为空", depId);
                    }
                } else {
                    logger.warn("[recognizeOrder] 未找到部门信息，部门ID: {}", depId);
                }
            }
            
            // 调用 DeepSeek API 解析订单
            logger.info("[recognizeOrder] ========== 开始调用 DeepSeek API 解析订单 ==========");
            logger.info("[recognizeOrder] OCR 文本长度: {} 字符", ocrTextContent != null ? ocrTextContent.length() : 0);
            String parsedResult;
            long deepSeekStartTime = System.currentTimeMillis();
            try {
                parsedResult = callDeepSeekAPIForOrder(ocrTextContent, departmentPrompt);
                long deepSeekElapsedTime = System.currentTimeMillis() - deepSeekStartTime;
                logger.info("[recognizeOrder] ========== DeepSeek API 调用完成 ==========");
                logger.info("[recognizeOrder] DeepSeek 耗时: {} ms ({} 秒)", deepSeekElapsedTime, deepSeekElapsedTime / 1000.0);
                logger.info("[recognizeOrder] 返回结果长度: {} 字符", parsedResult != null ? parsedResult.length() : 0);
                logger.info("[recognizeOrder] ================================================");
            } catch (Exception e) {
                long deepSeekElapsedTime = System.currentTimeMillis() - deepSeekStartTime;
                long methodTotalTime = System.currentTimeMillis() - methodStartTime;
                logger.error("[recognizeOrder] ========== DeepSeek API 调用失败 ==========");
                logger.error("[recognizeOrder] DeepSeek 耗时: {} ms ({} 秒)", deepSeekElapsedTime, deepSeekElapsedTime / 1000.0);
                logger.error("[recognizeOrder] 方法总耗时: {} ms ({} 秒)", methodTotalTime, methodTotalTime / 1000.0);
                logger.error("[recognizeOrder] 错误信息: {}", e.getMessage(), e);
                logger.error("[recognizeOrder] ==========================================");
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
                        // 判断订货数量是否为数字，如果不是数字则赋值 "1"
                        String quantity = item.optString("quantity", "");
                        itemMap.put("quantity", isNumeric(quantity) ? quantity : "1");
                        itemMap.put("spec", item.optString("spec", ""));
                        itemMap.put("quantityIsChange", false);
                        itemMap.put("specIsChange", false);
                        itemMap.put("standardWeight", item.optString("standardWeight", ""));
                        itemMap.put("itemUnit", item.optString("itemUnit", ""));
                        // itemsPerCarton 和 cartonUnit 只能是数字，如果不是数字则设置为空字符串
                        String itemsPerCarton = item.optString("itemsPerCarton", "");
                        String cartonUnit = item.optString("cartonUnit", "");
                        itemMap.put("itemsPerCarton", isNumeric(itemsPerCarton) ? itemsPerCarton : "");
                        itemMap.put("cartonUnit", isNumeric(cartonUnit) ? cartonUnit : "");
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
                            itemMap.put("originalText", item.optString("originalText", "")); // OCR原始文本（清洗后）
                            // 判断订货数量是否为数字，如果不是数字则赋值 "1"
                            String quantity = item.optString("quantity", "");
                            itemMap.put("quantity", isNumeric(quantity) ? quantity : "1");
                            itemMap.put("spec", item.optString("spec", ""));
                            itemMap.put("quantityIsChange", false);
                            itemMap.put("specIsChange", false);
                            itemMap.put("standardWeight", item.optString("standardWeight", ""));
                            itemMap.put("itemUnit", item.optString("itemUnit", ""));
                            String itemsPerCarton = item.optString("itemsPerCarton", "");
                            itemMap.put("itemsPerCarton", isNumeric(itemsPerCarton) ? itemsPerCarton : "");
                            itemMap.put("cartonUnit", item.optString("cartonUnit", ""));
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
                    
                    // 手写体识别：如果规格是 "h"，转换为 "个"
                    if ("h".equalsIgnoreCase(spec)) {
                        spec = "个";
                        item.put("spec", spec);
                        item.put("specIsChange", true);
                        logger.info("[recognizeOrder] 手写体识别：规格 'h' 转换为 '个'");
                    }
                    
                    // 如果规格为空，并且数量是2位数或3位数
                    if (spec.isEmpty() && quantity.matches("^\\d{2,3}$")) {
                        String newQuantity;
                        if (quantity.length() == 2) {
                            // 2位数：取第1位作为数量，规格设为"个"
                            newQuantity = quantity.substring(0, 1);
                        } else {
                            // 3位数：取前2位作为数量，规格设为"个"
                            newQuantity = quantity.substring(0, 2);
                        }
                        item.put("quantity", newQuantity);
                        item.put("spec", "");
                        item.put("quantityIsChange", true);
                        item.put("specIsChange", true);
                        logger.info("[recognizeOrder] 拆分{}位数数量: quantity={} -> quantity={}, spec={}", 
                            quantity.length(), quantity, newQuantity, "个");
                        // 更新变量以便后续使用
                        quantity = newQuantity;
                        spec = "";
                    }
                    
                    // 字段映射：将 DeepSeek 返回的字段映射到前端期望的字段格式
                    // DeepSeek 返回：quantity, spec
                    // 前端期望：qty, unit
                    item.put("qty", quantity);
                    item.put("unit", spec);
                    logger.info("[recognizeOrderspecspecspec]");

                    // 保留原始字段以便兼容
                    item.put("remark", item.get("note") != null ? item.get("note").toString().trim() : "");
                }
                
                logger.info("[recognizeOrder] DeepSeek 解析成功，解析到 {} 个商品", itemsList.size());
            } catch (Exception e) {
                logger.error("[recognizeOrder] DeepSeek 返回结果解析失败: {}", e.getMessage(), e);
                if (parsedResult != null) {
                    logger.error("[recognizeOrder] 响应内容（长度: {}）: {}", parsedResult.length(), 
                            parsedResult.length() > 500 ? parsedResult.substring(0, 500) + "..." : parsedResult);
                }
                return R.error("订单解析失败：DeepSeek 返回结果解析失败 - " + e.getMessage());
            }
            
            // 验证解析结果
            if (itemsList.isEmpty() || !isValidParseResult(itemsList)) {
                logger.error("[recognizeOrder] DeepSeek 解析结果无效（为空或格式不正确）");
                return R.error("订单解析失败：DeepSeek 解析结果无效");
            }



            // 如果提供了 depId 和 disId，则调用 pasteSearchGoods 方法查询商品并保存订单
            if (!itemsList.isEmpty()) {
                try {
                    // 记录订单处理开始时间
                    long orderProcessStartTime = System.currentTimeMillis();
                    // 先查询当前最大的 today_order，为所有订单统一设置递增的 nxDoTodayOrder
                    int currentMaxOrder = 0;
                    Map<String, Object> map = new HashMap<>();
                    map.put("depFatherId", depId);
                    Integer integer = nxDepartmentOrdersService.queryOrderGoodsCount(map);
                    if(integer > 0){
                        if (depId != null) {
                            currentMaxOrder = nxDepartmentOrdersService.queryMaxTodayOrder(depId);
                            logger.info("[pasteSearchGoods] 当前部门最大 today_order: {}, 即将处理 {} 个订单", currentMaxOrder);
                        }
                    }
                    logger.info("[recognizeOrder] 当前部门最大 today_order: {}, 即将处理 {} 个订单", currentMaxOrder, itemsList.size());
                    int todayOrderCounter = 0; // 用于跟踪当前订单的序号

                    // 处理每条识别数据
                    List<NxDepartmentOrdersEntity> orderList = new ArrayList<>();
                    // 使用 Map 保存原始索引对应的响应，以保持顺序
                    Map<Integer, NxDepartmentOrdersEntity> responseMap = new HashMap<>();
                    List<Integer> orderIndexList = new ArrayList<>(); // 保存 orderList 中每个订单对应的原始索引
                    // 保存原始索引对应的包装结构字段，用于 searchGoods 返回后设置到订单实体
                    Map<Integer, Map<String, String>> orderIndexToPackagingFields = new HashMap<>();

                    for (int i = 0; i < itemsList.size(); i++) {
                        Map<String, Object> item = itemsList.get(i);

                        // 订单中的商品名称使用 name（纠错后的名称）
                        String goodsName = item.get("name") != null ? item.get("name").toString().trim() : "";
                        // 训练数据使用 rawName（原始OCR识别的名称），如果没有 rawName 则使用 name
                        String rawName = item.get("rawName") != null && !item.get("rawName").toString().trim().isEmpty()
                                ? item.get("rawName").toString().trim() : goodsName;
                        String quantity = item.get("quantity") != null ? item.get("quantity").toString().trim() : "";
                        String spec = item.get("spec") != null ? item.get("spec").toString().trim() : "";
                        // 获取数量和规格是否改变（用于判断订单状态）
                        Boolean quantityIsChange = item.get("quantityIsChange") != null 
                                ? (Boolean) item.get("quantityIsChange") : false;
                        Boolean specIsChange = item.get("specIsChange") != null 
                                ? (Boolean) item.get("specIsChange") : false;
                        String standardWeight = item.get("standardWeight") != null ? item.get("standardWeight").toString().trim() : "";
                        String itemUnit = item.get("itemUnit") != null ? item.get("itemUnit").toString().trim() : "";
                        String itemsPerCarton = item.get("itemsPerCarton") != null ? item.get("itemsPerCarton").toString().trim() : "";
                        String cartonUnit = item.get("cartonUnit") != null ? item.get("cartonUnit").toString().trim() : "";
                        String note = item.get("note") != null ? item.get("note").toString().trim() : "";
                        String isNotice = item.get("isNotice") != null ? item.get("isNotice").toString().trim() : "";
                        // OCR原始文本，去除所有空格以便后续匹配
                        String originalText = item.get("originalText") != null 
                                ? item.get("originalText").toString().trim().replaceAll("\\s+", "") : "";

                        if (goodsName.isEmpty()) {
                            logger.warn("[recognizeOrder] 跳过商品：名称为空");
                            continue;
                        }

                        // 判断是否缺少数量或规格
                        boolean isDataMissing = (quantity.trim().isEmpty()) && (spec.trim().isEmpty());
                        if (isDataMissing) {
                            logger.info("[recognizeOrder] 订单缺少数量或规格，创建订单并查询商品: goodsName={}, quantity={}, spec={}",
                                goodsName, quantity, spec);
                        }

                        //0创建基本订单实体
                        NxDepartmentOrdersEntity orderBasic = new NxDepartmentOrdersEntity();
                        orderBasic.setNxDoDepartmentId(depId);
                        orderBasic.setNxDoDepartmentFatherId(depFatherId);
                        orderBasic.setNxDoDistributerId(disId);
                        orderBasic.setNxDoOrderUserId(userId);
                        orderBasic.setNxDoGoodsName(goodsName);
                        orderBasic.setNxDoGoodsOriginalName(goodsName);
                        orderBasic.setNxDoRemark(note);
                        orderBasic.setOrcNotice(isNotice);
                        orderBasic.setNxDoPurchaseUserId(-1);
                        orderBasic.setNxDoIsAgent(-1);
                        orderBasic.setNxDoQuantity(quantity);
                        orderBasic.setNxDoStandard(spec);
                        // 注意：同步接口不需要关联OCR任务ID
                        // 设置包装结构字段
                        orderBasic.setStandardWeight(standardWeight);
                        orderBasic.setItemUnit(itemUnit);
                        orderBasic.setItemsPerCarton(itemsPerCarton);
                        orderBasic.setCartonUnit(cartonUnit);
                        // 预先设置 nxDoTodayOrder，确保顺序正确
                        int todayOrder = currentMaxOrder + todayOrderCounter + 1;
                        orderBasic.setNxDoTodayOrder(todayOrder);
                        logger.info("[recognizeOrder] 设置订单 todayOrder: 商品名称={}, 原始索引={}, todayOrder={}, currentMaxOrder={}, counter={}",
                                goodsName, i, todayOrder, currentMaxOrder, todayOrderCounter);
                        todayOrderCounter++;
                        // 如果订单缺少3个必备条件（商品名称、数量、规格），设置状态为-2，保存订单，跳过后续处理
                        if (isDataMissing) {
                            logger.info("[recognizeOrderFast] 订单缺少数量或规格，设置状态为-2并保存订单，跳过后续商品搜索和训练数据添加: goodsName={}, quantity={}, spec={}",
                                    goodsName, quantity, spec);
                            orderBasic.setNxDoStatus(-2);
                            saveOrderWithoutGoods(orderBasic);
                            // 将订单添加到响应Map
                            responseMap.put(i, orderBasic);
                            continue;
                        }



                        //0级有限匹配： 查询训练数据有goodsId的解析原文，如果当前原文是否完全匹配，则直接推断为改商品；
                        Map<String, Object> traGoodsMap = new HashMap<>();
                        traGoodsMap.put("depId", depId);
                        traGoodsMap.put("otdOcrText", originalText);
                        traGoodsMap.put("disGoodsId", 1);
                        logger.info("[ocrController] 0级匹配-按nx_otd_ocr_text查询: originalText='{}', depId={}", originalText, depId);
                        NxOrderOcrTrainingDataEntity dataEntity = nxOrderOcrTrainingDataService.queryByMatchFields(traGoodsMap);
                        if(dataEntity != null){
                            logger.info("[ocrController] 0级匹配命中: nx_otd_ocr_text='{}', 训练数据ID={}, 商品ID={}, 纠正数量={}, 纠正规格={}",
                                    dataEntity.getNxOtdOcrText(), dataEntity.getNxOtdId(), dataEntity.getNxOtdDisGoodsId(),
                                    dataEntity.getNxOtdFinalQuantity(), dataEntity.getNxOtdFinalStandard());
                            Integer nxOtdDisGoodsId = dataEntity.getNxOtdDisGoodsId();
                            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(nxOtdDisGoodsId);

                            // 根据数量和规格是否改变设置订单状态
                            orderBasic.setNxDoStatus(0);
                            orderBasic.setNxDoStandard(dataEntity.getNxOtdFinalStandard());
                            orderBasic.setStandardWeight(dataEntity.getNxOtdFinalStandardWeight());
                            orderBasic.setNxDoRemark(dataEntity.getNxOtdFinalRemark());
                            saveStrongQueryOrderAndConvert(orderBasic, distributerGoodsEntity,  i, responseMap);
                            continue;
                        }

                        // 1级优先匹配：首先查询部门商品历史记录，如果订货商品名称和规格完全匹配，则直接推断为该商品
                        Map<String, Object> depGoodsMap = new HashMap<>();
                        depGoodsMap.put("disId", disId);
                        depGoodsMap.put("depId", depId);
                        depGoodsMap.put("name", rawName);
                        depGoodsMap.put("standard", spec);
                        List<NxDepartmentDisGoodsEntity> departmentDisGoodsList = nxDepartmentDisGoodsService.queryDepartmentGoods(depGoodsMap);
                        logger.info("[ocrController] 优先匹配-部门商品历史记录查询结果数量: {}", departmentDisGoodsList.size());
                        // 如果部门商品历史记录中有完全匹配的（名称+规格），则直接使用该商品
                        if (departmentDisGoodsList.size() == 1) {
                            NxDepartmentDisGoodsEntity departmentDisGoodsEntity = departmentDisGoodsList.get(0);
                            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(departmentDisGoodsEntity.getNxDdgDisGoodsId());
                            if (distributerGoodsEntity == null) {
                                logger.warn("[ocrController] 1级匹配-部门商品引用的配送商商品已不存在，商品ID: {}，跳过此匹配，继续后续流程", departmentDisGoodsEntity.getNxDdgDisGoodsId());
                            } else {
                                // 根据数量和规格是否改变设置订单状态
                                setOrderStatusByChangeFlag(orderBasic, specIsChange, quantityIsChange);
                                // 保存订单并转换为响应DTO（传递包装结构字段）
                                saveStrongQueryOrderAndConvert(orderBasic, distributerGoodsEntity,  i, responseMap);
                                continue;
                            }
                        }

                        // 2级优先匹配：商品库，如果订货商品名称和规格完全匹配，则直接推断为该商品
                        Map<String, Object> mapZero = new HashMap<>();
                        mapZero.put("disId", disId);
                        mapZero.put("searchStr", rawName);
                        mapZero.put("standard", spec);
                        logger.info("[recognizeOrder] 一级匹配-商品名称+规格查询参数: {}", mapZero);
                        List<NxDistributerGoodsEntity> distributerGoodsEntitiesZero = nxDistributerGoodsService.queryDisGoodsByName(mapZero);
                        // 如果商品历史记录中有完全匹配的（名称+规格），则直接使用该商品
                        if (distributerGoodsEntitiesZero.size() == 1) {
                            NxDistributerGoodsEntity distributerGoodsEntity = distributerGoodsEntitiesZero.get(0);
                            logger.info("[recognizeOrder] 一级匹配：商品库，，直接保存订单，disGoodsId={}",
                                    distributerGoodsEntity.getNxDistributerGoodsId());

                            // 根据数量和规格是否改变设置订单状态
                            setOrderStatusByChangeFlag(orderBasic, specIsChange, quantityIsChange);

                            // 保存订单并转换为响应DTO（传递包装结构字段）
                            saveStrongQueryOrderAndConvert(orderBasic, distributerGoodsEntity,  i, responseMap);

                            continue;
                        }


                        // 3级优先匹配：商品库别名精准查询，如果订货商品名称和规格完全匹配，则直接推断为该商品
                        Map<String, Object> mapA = new HashMap<>();
                        mapA.put("disId", disId);
                        mapA.put("alias", goodsName);
                        mapA.put("standard", spec);
                        List<NxDistributerGoodsEntity> distributerGoodsEntitiesAlias = nxDistributerGoodsService.queryDisGoodsByAlias(mapA);
                        // 如果商品历史记录中有完全匹配的（名称+规格），则直接使用该商品
                        if (distributerGoodsEntitiesAlias.size() == 1) {
                            NxDistributerGoodsEntity distributerGoodsEntity = distributerGoodsEntitiesAlias.get(0);
                            logger.info("[pasteSearchGoods] 3级优先匹配-商品库别名精准查询，直接保存订单，disGoodsId={}",
                                    distributerGoodsEntity.getNxDistributerGoodsId());
                            // 根据数量和规格是否改变设置订单状态
                            setOrderStatusByChangeFlag(orderBasic, specIsChange, quantityIsChange);
                            // 保存订单并转换为响应DTO（传递包装结构字段）
                            saveStrongQueryOrderAndConvert(orderBasic, distributerGoodsEntity,  i, responseMap);
                            continue;
                        }

                        // 4级优先匹配：查询训练表中是否有相同内容（匹配：部门ID + 原始商品名称 rawName）
                        Map<String, Object> matchParams = new HashMap<>();
                        matchParams.put("depId", depId);
                        matchParams.put("goodsName", rawName); // 使用 rawName 查询训练数据
                        matchParams.put("disGoodsId", 1); // 先查询有商品ID的
                        NxOrderOcrTrainingDataEntity matchedTrainingData = nxOrderOcrTrainingDataService.queryByMatchFields(matchParams);

                        if (matchedTrainingData != null && matchedTrainingData.getNxOtdDisGoodsId() != null) {
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
                                orderBasic.setNxDoGoodsName(orderGoodsName);

                            System.out.println("再次确认规格" + matchedTrainingData.getNxOtdOriginalStandard() + "specshi" + spec);
                                if(matchedTrainingData.getNxOtdOriginalStandard().equals(spec)){
                                    orderBasic.setNxDoStatus(0);
                                    orderBasic.setNxDoStandard(matchedTrainingData.getNxOtdFinalStandard());
                                    orderBasic.setStandardWeight(matchedTrainingData.getNxOtdFinalStandardWeight());
                                    orderBasic.setNxDoRemark(matchedTrainingData.getNxOtdFinalRemark());
                                }else{
                                    // 根据数量和规格是否改变设置订单状态
                                    setOrderStatusByChangeFlag(orderBasic, specIsChange, quantityIsChange);
                                }

                                // 保存订单并转换为响应DTO（传递包装结构字段）
                            saveStrongQueryOrderAndConvert(orderBasic, disGoodsEntity,  i, responseMap);
                            continue;
                        }

                        // 以上是用 rawName 进行4种强查询（0级：otdOcrText、1级：部门商品历史记录、2级：商品库、3级：商品库别名、4级：训练数据rawName有商品ID）
                        // 如果以上查询都没有找到商品，则创建或查询训练数据（无商品ID限制）
                        if (responseMap.get(i) == null) {
                            logger.info("[recognizeOrder] rawName 的4种强查询都没有找到商品，创建或查询训练数据: rawName={}, name={}", 
                                    rawName, goodsName);
                            
                            // 创建或查询训练数据：original_goods_name = rawName, deepseek_recommended_name = name
                            // 注意：createOrQueryTrainingData 内部会查询一次（无商品ID限制），但第2596行已经查询过有商品ID的了
                            // 为了避免重复查询，可以先查询一次无商品ID限制的，如果找到了就直接使用
                            Map<String, Object> matchParamsNoGoodsId = new HashMap<>();
                            matchParamsNoGoodsId.put("depId", depId);
                            matchParamsNoGoodsId.put("goodsName", rawName);
                            // 不设置 disGoodsId，查询所有训练数据（包括没有商品ID的）
                            NxOrderOcrTrainingDataEntity existingTrainingData = nxOrderOcrTrainingDataService.queryByMatchFields(matchParamsNoGoodsId);
                            
                            NxOrderOcrTrainingDataEntity trainingData;
                            if (existingTrainingData != null) {
                                // 找到了已有的训练数据（没有商品ID），直接使用
                                logger.info("[recognizeOrder] 找到已有的训练数据（无商品ID），训练数据ID: {}", existingTrainingData.getNxOtdId());
                                trainingData = existingTrainingData;
                            } else {
                                // 没有找到，创建新的训练数据
                                trainingData = new NxOrderOcrTrainingDataEntity();
                                trainingData.setNxOtdDepartmentId(depId);
                                trainingData.setNxOtdDepartmentFatherId(depFatherId);
                                trainingData.setNxOtdDistributerId(disId);
                                trainingData.setNxOtdOriginalGoodsName(rawName);
                                trainingData.setNxOtdDeepseekRecommendedName(goodsName);
                                trainingData.setNxOtdOriginalQuantity(quantity);
                                trainingData.setNxOtdOriginalStandard(spec);
                                trainingData.setNxOtdOriginalStandardWeight(standardWeight);
                                trainingData.setNxOtdOriginalRemark(note);
                                trainingData.setNxOtdOcrText(originalText != null ? originalText : "");
                                trainingData.setNxOtdIsNameManuallyAnnotated(0);
                                trainingData.setNxOtdIsQuantityManuallyAnnotated(0);
                                trainingData.setNxOtdIsStandardManuallyAnnotated(0);
                                trainingData.setNxOtdIsStandardWeightManuallyAnnotated(0);
                                trainingData.setNxOtdIsRemarkManuallyAnnotated(0);
                                trainingData.setNxOtdFinalGoodsName(null);
                                trainingData.setNxOtdFinalQuantity(null);
                                trainingData.setNxOtdFinalStandard(null);
                                trainingData.setNxOtdFinalStandardWeight(null);
                                trainingData.setNxOtdFinalRemark(null);
                                trainingData.setNxOtdDataSource("OCR_IMAGE");
                                trainingData.setNxOtdCreateDate(formatWhatYearDayTime(0));
                                trainingData.setNxOtdUpdateDate(formatWhatYearDayTime(0));
                                trainingData.setNxOtdCreateUserId(userId);
                                nxOrderOcrTrainingDataService.save(trainingData);
                                logger.info("[recognizeOrder] 训练数据创建成功，训练数据ID: {}", trainingData.getNxOtdId());
                            }
                            
                            // 以下用 name 进行3种弱查询，订单状态统一为 -2（因为属于弱查询，需要人工确认）
                            // 1级弱查询：部门商品历史记录（名称+规格）
                            Map<String, Object> depGoodsMapWeak = new HashMap<>();
                            depGoodsMap.put("disId", disId);
                            depGoodsMapWeak.put("depId", depId);
                            depGoodsMapWeak.put("name", goodsName);
                            depGoodsMapWeak.put("standard", spec);
                            List<NxDepartmentDisGoodsEntity> departmentDisGoodsListWeak = nxDepartmentDisGoodsService.queryDepartmentGoods(depGoodsMapWeak);
                            logger.info("[recognizeOrder] 弱查询-部门商品历史记录查询结果数量: {}", departmentDisGoodsListWeak.size());
                            
                            if (departmentDisGoodsListWeak.size() == 1) {
                                NxDepartmentDisGoodsEntity departmentDisGoodsEntity = departmentDisGoodsListWeak.get(0);
                                NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(departmentDisGoodsEntity.getNxDdgDisGoodsId());
                                
                                logger.info("[recognizeOrder] 弱查询找到商品，订单状态设置为 -2（待修正）: disGoodsId={}", 
                                        distributerGoodsEntity.getNxDistributerGoodsId());

                                // 保存弱查询订单（关联训练数据、添加推荐商品）并转换为响应DTO（传递包装结构字段）
                                saveWeakQueryOrderWithTrainingData(orderBasic, distributerGoodsEntity, trainingData, note, i, responseMap,
                                        standardWeight, itemUnit, itemsPerCarton, cartonUnit);
                                continue;
                            }

                            // 2级弱查询：商品库（名称+规格）
                            Map<String, Object> mapZeroWeak = new HashMap<>();
                            mapZeroWeak.put("disId", disId);
                            mapZeroWeak.put("searchStr", goodsName);
                            mapZeroWeak.put("standard", spec);
                            logger.info("[recognizeOrder] 弱查询-商品名称+规格查询参数: {}", mapZeroWeak);
                            List<NxDistributerGoodsEntity> distributerGoodsEntitiesZeroWeak = nxDistributerGoodsService.queryDisGoodsByName(mapZeroWeak);
                            
                            if (distributerGoodsEntitiesZeroWeak.size() == 1) {
                                NxDistributerGoodsEntity distributerGoodsEntity = distributerGoodsEntitiesZeroWeak.get(0);
                                logger.info("[recognizeOrder] 弱查询找到商品，订单状态设置为 -2（待修正）: disGoodsId={}", 
                                        distributerGoodsEntity.getNxDistributerGoodsId());

                                // 保存弱查询订单（关联训练数据、添加推荐商品）并转换为响应DTO（传递包装结构字段）
                                saveWeakQueryOrderWithTrainingData(orderBasic, distributerGoodsEntity, trainingData, note, i, responseMap,
                                        standardWeight, itemUnit, itemsPerCarton, cartonUnit);
                                continue;
                            }

                            // 3级弱查询：训练数据（部门ID + name 或 deepseek_recommended_name）
                            Map<String, Object> matchParamsWeak = new HashMap<>();
                            matchParamsWeak.put("depId", depId);
                            matchParamsWeak.put("goodsName", goodsName); // 使用 name 查询训练数据
                            matchParamsWeak.put("disGoodsId", 1);
                            NxOrderOcrTrainingDataEntity matchedTrainingDataWeak = nxOrderOcrTrainingDataService.queryByMatchFields(matchParamsWeak);
                            
                            if (matchedTrainingDataWeak != null && matchedTrainingDataWeak.getNxOtdDisGoodsId() != null) {
                                // 训练数据中有商品ID，直接创建订单
                                logger.info("[recognizeOrder] 弱查询-找到匹配的训练数据，商品ID: {}, 订单状态设置为 -2（待修正）",
                                        matchedTrainingDataWeak.getNxOtdDisGoodsId());
                                
                                // 查询商品信息
                                NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(
                                        matchedTrainingDataWeak.getNxOtdDisGoodsId());
                                if (disGoodsEntity == null) {
                                    logger.error("[recognizeOrder] 商品不存在，商品ID: {}",
                                            matchedTrainingDataWeak.getNxOtdDisGoodsId());
                                    throw new RuntimeException("商品不存在，商品ID: " + matchedTrainingDataWeak.getNxOtdDisGoodsId());
                                }
                                
                                // 保存弱查询订单（关联训练数据、添加推荐商品）并转换为响应DTO（传递包装结构字段）
                                // 注意：使用新创建的 trainingData，而不是 matchedTrainingDataWeak
                                saveWeakQueryOrderWithTrainingData(orderBasic, disGoodsEntity, trainingData, note, i, responseMap,
                                        standardWeight, itemUnit, itemsPerCarton, cartonUnit);
                                continue;
                            }

                            // name 的3种弱查询都没找到，需要调用 searchGoods 进行模糊搜索
                            logger.info("[recognizeOrder] name 的3种弱查询都没有找到商品，将调用 searchGoods 进行模糊搜索: name={}", goodsName);
                            
                            // 创建订单实体，用于 searchGoods
                            orderBasic.setNxDoStatus(-2);
                            orderBasic.setNxDoTrainingDataId(trainingData.getNxOtdId());
                            orderBasic.setNxDoQuantity(quantity);
                            orderBasic.setNxDoStandard(spec);
                            
                            // 保存订单到 orderList，后续统一调用 searchGoods
                            // 同时保存包装结构字段，以便后续转换为 DTO 时使用
                            orderList.add(orderBasic);
                            orderIndexList.add(i);
                            // 保存包装结构字段到 Map，key 为原始索引
                            Map<String, String> packagingFields = new HashMap<>();
                            packagingFields.put("standardWeight", standardWeight);
                            packagingFields.put("itemUnit", itemUnit);
                            packagingFields.put("itemsPerCarton", itemsPerCarton);
                            packagingFields.put("cartonUnit", cartonUnit);
                            orderIndexToPackagingFields.put(i, packagingFields);
                        }
                    }

                    if (!orderList.isEmpty()) {
                        logger.info("[recognizeOrder] 需要调用 searchGoods 的订单数量: {}", orderList.size());
                        logger.info("[recognizeOrder] orderIndexList 大小: {}", orderIndexList.size());
                        
                        List<NxDepartmentOrdersEntity> searchResults = nxDepartmentOrdersService.searchAndSaveOrdersFromOcr(orderList);
                        logger.info("[recognizeOrder] searchAndSaveOrdersFromOcr 返回结果数量: {}", 
                                searchResults != null ? searchResults.size() : 0);

                        int maxSize = Math.min(searchResults != null ? searchResults.size() : 0, orderIndexList.size());
                        logger.info("[recognizeOrder] 开始处理搜索结果，最大处理数量: {}", maxSize);
                        for (int j = 0; j < maxSize; j++) {
                            Integer originalIndex = orderIndexList.get(j);
                            NxDepartmentOrdersEntity order = searchResults.get(j);
                            logger.info("[recognizeOrder] 处理搜索结果[{}]: originalIndex={}, order={}", 
                                    j, originalIndex, order != null ? "not null" : "null");
                            if (order != null && originalIndex != null) {
                                Map<String, String> packagingFields = orderIndexToPackagingFields.get(originalIndex);
                                String standardWeight = packagingFields != null ? packagingFields.get("standardWeight") : "";
                                String itemUnit = packagingFields != null ? packagingFields.get("itemUnit") : "";
                                String itemsPerCarton = packagingFields != null ? packagingFields.get("itemsPerCarton") : "";
                                String cartonUnit = packagingFields != null ? packagingFields.get("cartonUnit") : "";
                                
                                order.setStandardWeight(standardWeight != null ? standardWeight : "");
                                order.setItemUnit(itemUnit != null ? itemUnit : "");
                                order.setItemsPerCarton(itemsPerCarton != null ? itemsPerCarton : "");
                                order.setCartonUnit(cartonUnit != null ? cartonUnit : "");
                                
                                if (order.getNxDoStatus() != null && order.getNxDoStatus() == -2) {
                                    if (order.getNxDistributerGoodsEntityList() == null || order.getNxDistributerGoodsEntityList().isEmpty()) {
                                        order = nxDepartmentOrdersService.addCommentsGoodsForOrder(order);
                                        logger.info("[recognizeOrder] 无候选列表，调用addCommentsGoodsForOrder: 订单ID={}, 推荐商品数量={}", 
                                                order.getNxDepartmentOrdersId(),
                                                order.getNxDistributerGoodsEntityList() != null ? order.getNxDistributerGoodsEntityList().size() : 0);
                                    } else {
                                        for (NxDistributerGoodsEntity goods : order.getNxDistributerGoodsEntityList()) {
                                            if (goods.getDepartmentDisGoodsEntity() == null) {
                                                Map<String, Object> mapDep = new HashMap<>();
                                                mapDep.put("disId", disId);
                                                mapDep.put("depId", order.getNxDoDepartmentId());
                                                mapDep.put("disGoodsId", goods.getNxDistributerGoodsId());
                                                NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(mapDep);
                                                if (departmentDisGoodsEntity != null) {
                                                    goods.setDepartmentDisGoodsEntity(departmentDisGoodsEntity);
                                                }
                                            }
                                        }
                                        logger.info("[recognizeOrder] 订单已有候选列表，补全部门商品信息: 订单ID={}, 候选数量={}", 
                                                order.getNxDepartmentOrdersId(), order.getNxDistributerGoodsEntityList().size());
                                    }
                                }
                                
                                responseMap.put(originalIndex, order);
                            } else {
                                logger.warn("[recognizeOrder] 跳过无效的索引或订单: j={}, originalIndex={}, order={}", 
                                        j, originalIndex, order != null ? "not null" : "null");
                            }
                        }
                        
                        // 检查是否有未处理的订单
                        if (searchResults != null && searchResults.size() > orderIndexList.size()) {
                            logger.warn("[recognizeOrder] 警告：searchResults 数量({})大于 orderIndexList 数量({})，可能有订单未被处理", 
                                    searchResults.size(), orderIndexList.size());
                        } else if (orderIndexList.size() > (searchResults != null ? searchResults.size() : 0)) {
                            logger.warn("[recognizeOrder] 警告：orderIndexList 数量({})大于 searchResults 数量({})，可能有订单未被处理", 
                                    orderIndexList.size(), searchResults != null ? searchResults.size() : 0);
                        }
                        
                        logger.info("[recognizeOrder] searchGoods 处理完成，responseMap 大小: {}", responseMap.size());
                    }

                    // 按照原始顺序组装最终的响应列表（直接返回订单实体）
                    List<NxDepartmentOrdersEntity> finalResponseList = new ArrayList<>();
                    for (int i = 0; i < itemsList.size(); i++) {
                        NxDepartmentOrdersEntity order = responseMap.get(i);
                        if (order != null) {
                            finalResponseList.add(order);
                        }
                    }
                    logger.info("[recognizeOrder] 订单处理完成，共 {} 条订单", finalResponseList.size());

                    // 记录订单处理耗时
                    long orderProcessElapsedTime = System.currentTimeMillis() - orderProcessStartTime;
                    logger.info("[recognizeOrder] 订单处理耗时: {} ms ({} 秒)", orderProcessElapsedTime, orderProcessElapsedTime / 1000.0);

                    // 记录总耗时
                    long methodTotalTime = System.currentTimeMillis() - methodStartTime;
                    logger.info("[recognizeOrder] ========== 方法总耗时统计 ==========");
                    logger.info("[recognizeOrder] 总耗时: {} ms ({} 秒)", methodTotalTime, methodTotalTime / 1000.0);
                    logger.info("[recognizeOrder] ====================================");

                    // 返回处理结果（同步接口不需要返回taskId和imageUrl）
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
            
            // 记录总耗时
            long methodTotalTime = System.currentTimeMillis() - methodStartTime;
            logger.info("[recognizeOrder] ========== 方法总耗时统计 ==========");
            logger.info("[recognizeOrder] 总耗时: {} ms ({} 秒)", methodTotalTime, methodTotalTime / 1000.0);
            logger.info("[recognizeOrder] ====================================");
            
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


    @RequestMapping(value = "/recognizeOrderFast", method = RequestMethod.POST)
    @ResponseBody
    public R recognizeOrderFast(@RequestBody Map<String, Object> request) {
        // 记录整个方法开始时间
        long methodStartTime = System.currentTimeMillis();
        Integer ocrTaskId = null;
        String ocrTextContent = null;
        List<Map<String, Object>> itemsList = null;
        NxOcrTaskEntity ocrTask = null;
        try {
            // ========== 第一步：验证必填参数 ==========
            Integer depId, disId, depFatherId, userId;
            try {
                depId = getIntegerParam(request, "depId", true);
                disId = getIntegerParam(request, "disId", true);
                depFatherId = getIntegerParam(request, "depFatherId", true);
                userId = getIntegerParam(request, "userId", true);
            } catch (IllegalArgumentException e) {
                return R.error(e.getMessage());
            }

            logger.info("[recognizeOrderFast] 接收参数：depId={}, disId={}, depFatherId={}, userId={}",
                    depId, disId, depFatherId, userId);

            // ========== 第二步：创建OCR任务记录 ==========
            logger.info("[recognizeOrderFast] 开始创建OCR任务记录...");

            // 获取用户信息（上传用户）
            NxDistributerUserEntity uploadUser = null;
            String uploadUserName = "未知用户";
            Integer uploadUserId = null;
            try {
                uploadUser = nxDistributerUserService.queryObject(userId);
                if (uploadUser != null) {
                    uploadUserId = uploadUser.getNxDistributerUserId().intValue();
                    uploadUserName = uploadUser.getNxDiuWxNickName() != null ? uploadUser.getNxDiuWxNickName() : "未知用户";
                }
            } catch (Exception e) {
                logger.warn("[recognizeOrderFast] 获取上传用户信息失败: {}", e.getMessage());
            }


            // 获取文件名（如果有）
            Object fileNameObj = request.get("fileName");
            String fileName = fileNameObj != null ? fileNameObj.toString() : "ocr_image_" + System.currentTimeMillis();
            
            // 创建OCR任务记录
            ocrTask = new NxOcrTaskEntity();

            NxDepartmentEntity nxDepartmentEntity = nxDepartmentService.queryObject(depFatherId);
            String  depName = nxDepartmentEntity.getNxDepartmentAttrName();
            if(nxDepartmentEntity.getNxDepartmentFatherId() != 0){
                NxDepartmentEntity subDepartmentEntity = nxDepartmentService.queryObject(depId);
                depName = depName + '.' + subDepartmentEntity.getNxDepartmentAttrName();
            }
            //查询这个 depFatherId 今日的第几个图片任务
            Map<String, Object> mapTask = new HashMap<>();
            mapTask.put("departmentFatherId", depFatherId);
            mapTask.put("date", formatWhatDay(0));  // 使用 date 参数，格式：yyyy-MM-dd
            mapTask.put("type", 1);
            int count = nxOcrTaskService.queryTotalByDepartmentAndStatus(mapTask);
            // count 是今日已有的任务数量，count + 1 就是今日第几个任务
            int todayTaskNumber = count + 1;
            logger.info("[recognizeOrderFast] 查询今日任务数量: {}, 当前任务是今日第 {} 个", count, todayTaskNumber);
            ocrTask.setNxOcrTaskFileName(depName + "第" + todayTaskNumber + "个图片订单");
            ocrTask.setNxOcrTaskTotalOrders(0);
            ocrTask.setNxOcrTaskCompletedOrders(0);
            ocrTask.setNxOcrTaskPendingOrders(0);
            ocrTask.setNxOcrTaskUploadTime(formatWhatYearDayTime(0));
            ocrTask.setNxOcrTaskUploadUserId(uploadUserId);
            ocrTask.setNxOcrTaskUploadUserName(uploadUserName);
            ocrTask.setNxOcrTaskStatus(0); // 0=处理中
            ocrTask.setNxOcrTaskCreateDate(formatWhatYearDayTime(0));
            ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
            ocrTask.setNxOcrTaskDistributerId(disId);
            ocrTask.setNxOcrTaskDepartmentId(depId);
            ocrTask.setNxOcrTaskDepartmentFatherId(depFatherId);
            ocrTask.setNxOcrTaskType(1); // 1=图片（recognizeOrderFast）

            // 保存任务记录（获取任务ID）
            nxOcrTaskService.save(ocrTask);
            ocrTaskId = ocrTask.getNxOcrTaskId();
            logger.info("[recognizeOrderFast] OCR任务记录创建成功，任务ID: {}", ocrTaskId);

            // ========== 第三步：获取图片并进行OCR识别 ==========
            // 获取图片 Base64
            Object imageBase64Obj = request.get("ImageBase64");
            if (imageBase64Obj == null) {
                logger.warn("[recognizeOrderFast] 图片数据为空");
                return R.error("图片数据不能为空");
            }
            String imageBase64 = imageBase64Obj.toString();
            if (imageBase64.isEmpty()) {
                logger.warn("[recognizeOrderFast] 图片数据为空");
                return R.error("图片数据不能为空");
            }

            // 处理 Base64 前缀（如果前端传了 data:image 前缀，需要去掉）
            if (imageBase64.contains(",")) {
                imageBase64 = imageBase64.substring(imageBase64.indexOf(",") + 1);
            }

            // 验证图片大小
            if (imageBase64.length() > MAX_IMAGE_BASE64_SIZE) {
                logger.warn("[recognizeOrderFast] 图片过大: {} bytes (限制: {} bytes)",
                        imageBase64.length(), MAX_IMAGE_BASE64_SIZE);
                return R.error("图片大小超过限制，最大支持 10MB（Base64 编码后）");
            }

            logger.info("[recognizeOrderFast] 开始识别和解析，图片大小: {} bytes", imageBase64.length());
            
            // ========== 第四步：OCR识别 ==========
            ocrTextContent = performOcrRecognition(imageBase64);
            
            if (ocrTextContent == null || ocrTextContent.trim().isEmpty()) {
                logger.error("[recognizeOrderFast] OCR识别结果为空，任务ID: {}", ocrTaskId);
                ocrTask.setNxOcrTaskStatus(-1); // -1=失败
                ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
                nxOcrTaskService.update(ocrTask);
                return R.error("未识别到任何文本");
            }
            
            // ========== 第五步：保存图片到服务器并更新任务图片路径 ==========
            try {
                String imagePath = saveBase64Image(imageBase64, ocrTaskId);
                ocrTask.setNxOcrTaskImagePath(imagePath);
                ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
                nxOcrTaskService.update(ocrTask);
                logger.info("[recognizeOrderFast] 图片保存成功，路径: {}，任务ID: {}", imagePath, ocrTaskId);
            } catch (Exception e) {
                logger.error("[recognizeOrderFast] 保存图片失败，任务ID: {}", ocrTaskId, e);
                // 图片保存失败不影响OCR识别，继续处理
            }
            
            // ✅ 内存优化：OCR识别完成后立即清空Base64图片数据引用，释放内存
            // Base64图片数据通常占用1-5MB内存，清空后可以立即被GC回收
            imageBase64 = null;
            
            // ========== 第六步：保存OCR文本到任务表 ==========
            ocrTask.setNxOcrTaskOcrText(ocrTextContent);
            ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
            nxOcrTaskService.update(ocrTask);
            logger.info("[recognizeOrderFast] OCR文本已保存到任务表，任务ID: {}, 文本长度: {} 字符", ocrTaskId, ocrTextContent.length());
            
            // ========== 第七步：使用规则解析（不使用DeepSeek） ==========
            logger.info("[recognizeOrderFast] 开始使用规则解析订单文本...");
            itemsList = parseOrderTextByRule(ocrTextContent);
            
            // 验证解析结果
            if (itemsList.isEmpty()) {
                logger.error("[recognizeOrderFast] 规则解析结果为空，任务ID: {}", ocrTaskId);
                ocrTask.setNxOcrTaskStatus(-1); // -1=失败
                ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
                nxOcrTaskService.update(ocrTask);
                return R.error("订单解析失败：规则解析结果为空，请检查图片格式是否符合要求（商品名+数量+单位）");
            }
            
            logger.info("[recognizeOrderFast] 规则解析成功，解析到 {} 个商品", itemsList.size());
            
            // ========== 第八步：更新任务总订单数 ==========
            ocrTask.setNxOcrTaskTotalOrders(itemsList.size());
            ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
            nxOcrTaskService.update(ocrTask);
            logger.info("[recognizeOrderFast] 更新OCR任务总订单数: {}", itemsList.size());




            // 如果提供了 depId 和 disId，则调用 pasteSearchGoods 方法查询商品并保存订单
            if (!itemsList.isEmpty()) {
                try {
                    // 记录订单处理开始时间
                    long orderProcessStartTime = System.currentTimeMillis();
                    // 先查询当前最大的 today_order，为所有订单统一设置递增的 nxDoTodayOrder
                    int currentMaxOrder = 0;
                    Map<String, Object> map = new HashMap<>();
                    map.put("depFatherId", depId);
                    Integer integer = nxDepartmentOrdersService.queryOrderGoodsCount(map);
                    if(integer > 0){
                        if (depId != null) {
                            currentMaxOrder = nxDepartmentOrdersService.queryMaxTodayOrder(depId);
                            logger.info("[recognizeOrderFast] 当前部门最大 today_order: {}, 即将处理 {} 个订单", currentMaxOrder, itemsList.size());
                        }
                    }
                    logger.info("[recognizeOrderFast] 当前部门最大 today_order: {}, 即将处理 {} 个订单", currentMaxOrder, itemsList.size());
                    int todayOrderCounter = 0; // 用于跟踪当前订单的序号

                    // 处理每条识别数据
                    List<NxDepartmentOrdersEntity> orderList = new ArrayList<>();
                    // 使用 Map 保存原始索引对应的响应，以保持顺序
                    Map<Integer, NxDepartmentOrdersEntity> responseMap = new HashMap<>();
                    List<Integer> orderIndexList = new ArrayList<>(); // 保存 orderList 中每个订单对应的原始索引
                    // 保存原始索引对应的包装结构字段，用于 searchGoods 返回后设置到订单实体
                    Map<Integer, Map<String, String>> orderIndexToPackagingFields = new HashMap<>();

                    for (int i = 0; i < itemsList.size(); i++) {
                        try {
                        Map<String, Object> item = itemsList.get(i);

                        // 订单中的商品名称使用 name（纠错后的名称）
                        String goodsName = item.get("name") != null ? item.get("name").toString().trim() : "";
                        // 训练数据使用 rawName（原始OCR识别的名称），如果没有 rawName 则使用 name
                        String rawName = item.get("rawName") != null && !item.get("rawName").toString().trim().isEmpty()
                                ? item.get("rawName").toString().trim() : goodsName;
                        String quantity = item.get("quantity") != null ? item.get("quantity").toString().trim() : "";
                        String spec = item.get("spec") != null ? item.get("spec").toString().trim() : "";
                        // 获取数量和规格是否改变（用于判断订单状态）
                        Boolean quantityIsChange = item.get("quantityIsChange") != null
                                ? (Boolean) item.get("quantityIsChange") : false;
                        Boolean specIsChange = item.get("specIsChange") != null
                                ? (Boolean) item.get("specIsChange") : false;
                        String standardWeight = item.get("standardWeight") != null ? item.get("standardWeight").toString().trim() : "";
                        String itemUnit = item.get("itemUnit") != null ? item.get("itemUnit").toString().trim() : "";
                        String itemsPerCarton = item.get("itemsPerCarton") != null ? item.get("itemsPerCarton").toString().trim() : "";
                        String cartonUnit = item.get("cartonUnit") != null ? item.get("cartonUnit").toString().trim() : "";
                        String note = item.get("note") != null ? item.get("note").toString().trim() : "";
                        String isNotice = item.get("isNotice") != null ? item.get("isNotice").toString().trim() : "";
                        // OCR原始文本，去除所有空格以便后续匹配
                        String originalText = item.get("originalText") != null
                                ? item.get("originalText").toString().trim().replaceAll("\\s+", "") : "";
                        // rawName 归一化（去空格），用于训练数据 goodsName 查询
                        String rawNameNorm = (rawName != null ? rawName.trim().replaceAll("\\s+", "") : "");
                        if (rawNameNorm.isEmpty()) rawNameNorm = rawName;

                        if (goodsName.isEmpty()) {
                            logger.warn("[recognizeOrder] 跳过商品：名称为空");
                            continue;
                        }

                        // 判断是否缺少数量或规格（3个必备条件：商品名称、数量、规格）
                        boolean isDataMissing = (quantity.trim().isEmpty()) && (spec.trim().isEmpty());
                        
                        //0创建基本订单实体
                        NxDepartmentOrdersEntity orderBasic = new NxDepartmentOrdersEntity();
                        orderBasic.setNxDoDepartmentId(depId);
                        orderBasic.setNxDoDepartmentFatherId(depFatherId);
                        orderBasic.setNxDoDistributerId(disId);
                        orderBasic.setNxDoOrderUserId(userId);
                        orderBasic.setNxDoGoodsName(goodsName);
                        orderBasic.setNxDoGoodsOriginalName(goodsName);
                        orderBasic.setNxDoRemark(note);
                        orderBasic.setOrcNotice(isNotice);
                        orderBasic.setNxDoPurchaseUserId(-1);
                        orderBasic.setNxDoIsAgent(-1);
                        orderBasic.setNxDoQuantity(quantity);
                        orderBasic.setNxDoStandard(spec);
                        // 关联OCR任务ID
                        orderBasic.setNxDoOcrTaskId(ocrTaskId);
                        // 设置包装结构字段
                        orderBasic.setStandardWeight(standardWeight);
                        orderBasic.setItemUnit(itemUnit);
                        orderBasic.setItemsPerCarton(itemsPerCarton);
                        orderBasic.setCartonUnit(cartonUnit);
                        // 预先设置 nxDoTodayOrder，确保顺序正确
                        int todayOrder = currentMaxOrder + todayOrderCounter + 1;
                        orderBasic.setNxDoTodayOrder(todayOrder);
                        logger.info("[recognizeOrderFast] 设置订单 todayOrder: 商品名称={}, 原始索引={}, todayOrder={}, currentMaxOrder={}, counter={}",
                                goodsName, i, todayOrder, currentMaxOrder, todayOrderCounter);
                        todayOrderCounter++;

                        // 优先按 nx_otd_ocr_text 匹配（无论是否 isDataMissing 都先尝试）：若解析的订单内容与训练数据完全一致，则使用训练数据的纠正数量、规格等
                        // 条件：originalText 长度至少 4 字符，否则视为不完整（如「香叶」可能是「香叶2两」的截断），不进行优先查询
                        if (originalText != null && originalText.length() >= 4) {
                            Map<String, Object> traGoodsMapOcr = new HashMap<>();
                            traGoodsMapOcr.put("depId", depId);
                            traGoodsMapOcr.put("otdOcrText", originalText);
                            traGoodsMapOcr.put("disGoodsId", 1);
                            logger.info("[recognizeOrderFast] 按nx_otd_ocr_text查询（含isDataMissing）: originalText='{}', depId={}", originalText, depId);
                            NxOrderOcrTrainingDataEntity dataEntityOcr = nxOrderOcrTrainingDataService.queryByMatchFields(traGoodsMapOcr);
                            if (dataEntityOcr != null && dataEntityOcr.getNxOtdDisGoodsId() != null) {
                                logger.info("[recognizeOrderFast] nx_otd_ocr_text匹配命中: 训练数据ID={}, 商品ID={}, 纠正数量={}, 纠正规格={}",
                                        dataEntityOcr.getNxOtdId(), dataEntityOcr.getNxOtdDisGoodsId(),
                                        dataEntityOcr.getNxOtdFinalQuantity(), dataEntityOcr.getNxOtdFinalStandard());
                                NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(dataEntityOcr.getNxOtdDisGoodsId());
                                if (distributerGoodsEntity != null) {
                                    String finalGoodsName = dataEntityOcr.getNxOtdFinalGoodsName();
                                    String orderGoodsName = (finalGoodsName != null && !finalGoodsName.trim().isEmpty()) ? finalGoodsName : goodsName;
                                    orderBasic.setNxDoGoodsName(orderGoodsName);
                                    orderBasic.setNxDoStatus(0);
                                    String finalQty = dataEntityOcr.getNxOtdFinalQuantity();
                                    if (finalQty != null && !finalQty.trim().isEmpty()) {
                                        orderBasic.setNxDoQuantity(finalQty);
                                    }
                                    orderBasic.setNxDoStandard(dataEntityOcr.getNxOtdFinalStandard());
                                    orderBasic.setStandardWeight(dataEntityOcr.getNxOtdFinalStandardWeight());
                                    orderBasic.setNxDoRemark(dataEntityOcr.getNxOtdFinalRemark());
                                    saveStrongQueryOrderAndConvert(orderBasic, distributerGoodsEntity, i, responseMap);
                                    continue;
                                }
                            }
                        }

                        // 如果订单缺少数量或规格（只有商品名称），仍创建训练数据以便后续人工标注，设置状态为-2并保存订单
                        if (isDataMissing) {
                            logger.info("[recognizeOrderFast] 订单缺少数量或规格，创建训练数据并保存订单: goodsName={}, quantity={}, spec={}",
                                    goodsName, quantity, spec);
                            orderBasic.setNxDoStatus(-2);
                            // 创建或查询训练数据（即使缺少数量和规格也保存，便于后续人工标注学习）
                            NxOrderOcrTrainingDataEntity trainingData = createOrQueryTrainingData(
                                    depId, depFatherId, disId, rawName, goodsName, quantity, spec, standardWeight, note, originalText, userId);
                            orderBasic.setNxDoTrainingDataId(trainingData.getNxOtdId());
                            saveOrderWithoutGoods(orderBasic);
                            // 将订单添加到响应Map
                            responseMap.put(i, orderBasic);
                            continue;
                        }

                        //0级有限匹配： 查询训练数据有goodsId的解析原文，如果当前原文是否完全匹配，则直接推断为改商品；
                        Map<String, Object> traGoodsMap = new HashMap<>();
                        traGoodsMap.put("depId", depId);
                        traGoodsMap.put("otdOcrText", originalText);
                        traGoodsMap.put("disGoodsId", 1);
                        logger.info("[recognizeOrderFast] 0级匹配-按nx_otd_ocr_text查询: originalText='{}', depId={}", originalText, depId);
                        NxOrderOcrTrainingDataEntity dataEntity = nxOrderOcrTrainingDataService.queryByMatchFields(traGoodsMap);
                        if(dataEntity != null){
                            logger.info("[recognizeOrderFast] 0级匹配命中: nx_otd_ocr_text='{}', 训练数据ID={}, 商品ID={}, 纠正数量={}, 纠正规格={}",
                                    dataEntity.getNxOtdOcrText(), dataEntity.getNxOtdId(), dataEntity.getNxOtdDisGoodsId(),
                                    dataEntity.getNxOtdFinalQuantity(), dataEntity.getNxOtdFinalStandard());
                            Integer nxOtdDisGoodsId = dataEntity.getNxOtdDisGoodsId();
                            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(nxOtdDisGoodsId);
                            if (distributerGoodsEntity == null) {
                                logger.warn("[recognizeOrderFast] 0级匹配-训练数据引用的商品已不存在，商品ID: {}，跳过此匹配，继续后续流程", nxOtdDisGoodsId);
                            } else {
                                // 根据数量和规格是否改变设置订单状态
                                orderBasic.setNxDoStatus(0);
                                orderBasic.setNxDoStandard(dataEntity.getNxOtdFinalStandard());
                                orderBasic.setStandardWeight(dataEntity.getNxOtdFinalStandardWeight());
                                orderBasic.setNxDoRemark(dataEntity.getNxOtdFinalRemark());
                                saveStrongQueryOrderAndConvert(orderBasic, distributerGoodsEntity,  i, responseMap);
                                continue;
                            }
                        }

                        // 1级优先匹配：首先查询部门商品历史记录（用 goodsName，部门历史存的是商品名如「西红柿」）
                        Map<String, Object> depGoodsMap = new HashMap<>();
                        depGoodsMap.put("disId", disId);
                        depGoodsMap.put("depId", depId);
                        depGoodsMap.put("name", goodsName);
                        depGoodsMap.put("standard", spec);
                        List<NxDepartmentDisGoodsEntity> departmentDisGoodsList = nxDepartmentDisGoodsService.queryDepartmentGoods(depGoodsMap);
                        logger.info("[recognizeOrderFast] 优先匹配-部门商品历史记录查询结果数量: {}", departmentDisGoodsList.size());
                        // 如果部门商品历史记录中有完全匹配的（名称+规格），则直接使用该商品
                        if (departmentDisGoodsList.size() == 1) {
                            NxDepartmentDisGoodsEntity departmentDisGoodsEntity = departmentDisGoodsList.get(0);
                            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(departmentDisGoodsEntity.getNxDdgDisGoodsId());
                            if (distributerGoodsEntity == null) {
                                logger.warn("[recognizeOrderFast] 1级匹配-部门商品引用的配送商商品已不存在，商品ID: {}，跳过此匹配，继续后续流程", departmentDisGoodsEntity.getNxDdgDisGoodsId());
                            } else {
                                // 根据数量和规格是否改变设置订单状态
                                setOrderStatusByChangeFlag(orderBasic, specIsChange, quantityIsChange);
                                // 保存订单并转换为响应DTO（传递包装结构字段）
                                saveStrongQueryOrderAndConvert(orderBasic, distributerGoodsEntity,  i, responseMap);
                                continue;
                            }
                        }

                        // 2级、3级商品库匹配已移除，交由 Service（searchAndSaveOrdersFromOcr）统一处理，避免重复查询

                        // 4级优先匹配：查询训练表中是否有相同内容（匹配：部门ID + 原始商品名称 rawNameNorm 归一化）
                        Map<String, Object> matchParams = new HashMap<>();
                        matchParams.put("depId", depId);
                        matchParams.put("disGoodsId", 1); // 先查询有商品ID的
                        matchParams.put("goodsName", rawNameNorm);
                        NxOrderOcrTrainingDataEntity matchedTrainingData = nxOrderOcrTrainingDataService.queryByMatchFields(matchParams);

                        if (matchedTrainingData != null && matchedTrainingData.getNxOtdDisGoodsId() != null) {
                            // 训练数据中有商品ID，直接创建订单
                            logger.info("[recognizeOrderFast] 找到匹配的训练数据，商品ID: {}, 直接创建订单",
                                    matchedTrainingData.getNxOtdDisGoodsId());

                            // 查询商品信息
                            NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(
                                    matchedTrainingData.getNxOtdDisGoodsId());
                            if (disGoodsEntity == null) {
                                logger.warn("[recognizeOrderFast] 训练数据引用的商品已不存在，商品ID: {}，跳过此匹配，继续后续流程",
                                        matchedTrainingData.getNxOtdDisGoodsId());
                                // 不抛异常，让流程继续到后续逻辑（创建训练数据、searchAndSaveOrdersFromOcr 等）
                            } else {
                            // 优先使用训练数据的 final_goods_name（如果存在），否则使用规则解析的 name
                            String finalGoodsName = matchedTrainingData.getNxOtdFinalGoodsName();
                            String orderGoodsName = (finalGoodsName != null && !finalGoodsName.trim().isEmpty())
                                    ? finalGoodsName
                                    : goodsName;
                            if (!orderGoodsName.equals(goodsName)) {
                                logger.info("[recognizeOrderFast] 使用训练数据的 final_goods_name='{}' 替代规则解析的 name='{}'",
                                        orderGoodsName, goodsName);
                            }
                            orderBasic.setNxDoGoodsName(orderGoodsName);

                            if(matchedTrainingData.getNxOtdOriginalStandard().equals(spec)){
                                orderBasic.setNxDoStatus(0);
                                orderBasic.setNxDoStandard(matchedTrainingData.getNxOtdFinalStandard());
                                orderBasic.setStandardWeight(matchedTrainingData.getNxOtdFinalStandardWeight());
                                orderBasic.setNxDoRemark(matchedTrainingData.getNxOtdFinalRemark());
                            }else{
                                // 根据数量和规格是否改变设置订单状态
                                setOrderStatusByChangeFlag(orderBasic, specIsChange, quantityIsChange);
                            }

                            // 保存订单并转换为响应DTO（传递包装结构字段）
                            saveStrongQueryOrderAndConvert(orderBasic, disGoodsEntity,  i, responseMap);
                            continue;
                            }
                        }


                        //5级 快速图片解析训练查询满足订单内容完全匹配训练数据nx_otd_ocr_text，则视为找到唯一商品

                        Map<String, Object> matchParamsText = new HashMap<>();
                        matchParamsText.put("depId", depId);
                        matchParamsText.put("disGoodsId", 1); // 先查询有商品ID的
                        matchParamsText.put("otdOcrText", originalText); // 去除空格后的原始订单文本
                        logger.info("[recognizeOrderFast] 5级匹配-按nx_otd_ocr_text查询: originalText='{}', depId={}", originalText, depId);
                        NxOrderOcrTrainingDataEntity matchedTrainingDataText = nxOrderOcrTrainingDataService.queryByMatchFields(matchParamsText);

                        if (matchedTrainingDataText != null && matchedTrainingDataText.getNxOtdDisGoodsId() != null) {
                            logger.info("[recognizeOrderFast] 5级匹配命中: nx_otd_ocr_text='{}', 训练数据ID={}, 商品ID={}, 纠正数量={}, 纠正规格={}",
                                    matchedTrainingDataText.getNxOtdOcrText(), matchedTrainingDataText.getNxOtdId(),
                                    matchedTrainingDataText.getNxOtdDisGoodsId(), matchedTrainingDataText.getNxOtdFinalQuantity(),
                                    matchedTrainingDataText.getNxOtdFinalStandard());

                            // 查询商品信息
                            NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(
                                    matchedTrainingDataText.getNxOtdDisGoodsId());
                            if (disGoodsEntity == null) {
                                logger.warn("[recognizeOrderFast] 训练数据引用的商品已不存在，商品ID: {}，跳过此匹配，继续后续流程",
                                        matchedTrainingDataText.getNxOtdDisGoodsId());
                                // 不抛异常，让流程继续到后续逻辑
                            } else {
                            // 优先使用训练数据的 final_goods_name（如果存在），否则使用规则解析的 name
                            String finalGoodsName = matchedTrainingDataText.getNxOtdFinalGoodsName();
                            String orderGoodsName = (finalGoodsName != null && !finalGoodsName.trim().isEmpty())
                                    ? finalGoodsName
                                    : goodsName;
                            if (!orderGoodsName.equals(goodsName)) {
                                logger.info("[recognizeOrderFast] 使用训练数据的 final_goods_name='{}' 替代规则解析的 name='{}'",
                                        orderGoodsName, goodsName);
                            }
                            orderBasic.setNxDoGoodsName(orderGoodsName);

                            // 5级匹配：originalText 与 nx_otd_ocr_text 完全一致，使用训练数据的纠正字段（数量、规格等）
                            orderBasic.setNxDoStatus(0);
                            String finalQty = matchedTrainingDataText.getNxOtdFinalQuantity();
                            if (finalQty != null && !finalQty.trim().isEmpty()) {
                                orderBasic.setNxDoQuantity(finalQty);
                            }
                            orderBasic.setNxDoStandard(matchedTrainingDataText.getNxOtdFinalStandard());
                            orderBasic.setStandardWeight(matchedTrainingDataText.getNxOtdFinalStandardWeight());
                            orderBasic.setNxDoRemark(matchedTrainingDataText.getNxOtdFinalRemark());

                            // 保存订单并转换为响应DTO（传递包装结构字段）
                            saveStrongQueryOrderAndConvert(orderBasic, disGoodsEntity,  i, responseMap);
                            continue;
                            }
                        }

                        // 以上是用 rawName 进行强查询（0级：otdOcrText、1级：部门商品历史记录、4级：训练数据rawName、5级：训练数据otdOcrText），商品库匹配交由 Service 统一处理
                        // 如果以上查询都没有找到商品，则创建或查询训练数据（无商品ID限制）
                        if (responseMap.get(i) == null) {
                            logger.info("[recognizeOrderFast] rawName 的强查询都没有找到商品，创建或查询训练数据: rawName={}, name={}",
                                    rawName, goodsName);

                            // 创建或查询训练数据：original_goods_name = rawName, deepseek_recommended_name = name
                            // 注意：createOrQueryTrainingData 内部会查询一次（无商品ID限制），但第2596行已经查询过有商品ID的了
                            // 为了避免重复查询，可以先查询一次无商品ID限制的，如果找到了就直接使用（用 rawNameNorm 归一化）
                            Map<String, Object> matchParamsNoGoodsId = new HashMap<>();
                            matchParamsNoGoodsId.put("depId", depId);
                            matchParamsNoGoodsId.put("goodsName", rawNameNorm);
                            // 不设置 disGoodsId，查询所有训练数据（包括没有商品ID的）
                            NxOrderOcrTrainingDataEntity existingTrainingData = nxOrderOcrTrainingDataService.queryByMatchFields(matchParamsNoGoodsId);

                            NxOrderOcrTrainingDataEntity trainingData;
                            if (existingTrainingData != null) {
                                // 找到了已有的训练数据（没有商品ID），直接使用
                                logger.info("[recognizeOrderFast] 找到已有的训练数据（无商品ID），训练数据ID: {}", existingTrainingData.getNxOtdId());
                                trainingData = existingTrainingData;
                            } else {
                                // 没有找到，创建新的训练数据
                                trainingData = new NxOrderOcrTrainingDataEntity();
                                trainingData.setNxOtdDepartmentId(depId);
                                trainingData.setNxOtdDepartmentFatherId(depFatherId);
                                trainingData.setNxOtdDistributerId(disId);
                                trainingData.setNxOtdOriginalGoodsName(rawName);
                                trainingData.setNxOtdDeepseekRecommendedName(goodsName);
                                trainingData.setNxOtdOriginalQuantity(quantity);
                                trainingData.setNxOtdOriginalStandard(spec);
                                trainingData.setNxOtdOriginalStandardWeight(standardWeight);
                                trainingData.setNxOtdOriginalRemark(note);
                                trainingData.setNxOtdOcrText(originalText != null ? originalText : "");
                                trainingData.setNxOtdIsNameManuallyAnnotated(0);
                                trainingData.setNxOtdIsQuantityManuallyAnnotated(0);
                                trainingData.setNxOtdIsStandardManuallyAnnotated(0);
                                trainingData.setNxOtdIsStandardWeightManuallyAnnotated(0);
                                trainingData.setNxOtdIsRemarkManuallyAnnotated(0);
                                trainingData.setNxOtdFinalGoodsName(null);
                                trainingData.setNxOtdFinalQuantity(null);
                                trainingData.setNxOtdFinalStandard(null);
                                trainingData.setNxOtdFinalStandardWeight(null);
                                trainingData.setNxOtdFinalRemark(null);
                                trainingData.setNxOtdDataSource("OCR_IMAGE");
                                trainingData.setNxOtdCreateDate(formatWhatYearDayTime(0));
                                trainingData.setNxOtdUpdateDate(formatWhatYearDayTime(0));
                                trainingData.setNxOtdCreateUserId(userId);
                                nxOrderOcrTrainingDataService.save(trainingData);
                                logger.info("[recognizeOrderFast] 训练数据创建成功，训练数据ID: {}", trainingData.getNxOtdId());
                            }

                            // 以下用 name 进行3种弱查询，订单状态统一为 -2（因为属于弱查询，需要人工确认）
                            // 1级弱查询：部门商品历史记录（名称+规格）
                            Map<String, Object> depGoodsMapWeak = new HashMap<>();
                            depGoodsMapWeak.put("disId", disId);
                            depGoodsMapWeak.put("depId", depId);
                            depGoodsMapWeak.put("name", goodsName);
                            depGoodsMapWeak.put("standard", spec);
                            List<NxDepartmentDisGoodsEntity> departmentDisGoodsListWeak = nxDepartmentDisGoodsService.queryDepartmentGoods(depGoodsMapWeak);
                            logger.info("[recognizeOrderFast] 弱查询-部门商品历史记录查询结果数量: {}", departmentDisGoodsListWeak.size());

                            if (departmentDisGoodsListWeak.size() == 1) {
                                NxDepartmentDisGoodsEntity departmentDisGoodsEntity = departmentDisGoodsListWeak.get(0);
                                NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(departmentDisGoodsEntity.getNxDdgDisGoodsId());
                                if (distributerGoodsEntity != null) {
                                    logger.info("[recognizeOrderFast] 弱查询找到商品，订单状态设置为 -2（待修正）: disGoodsId={}",
                                            distributerGoodsEntity.getNxDistributerGoodsId());

                                    // 保存弱查询订单（关联训练数据、添加推荐商品）并转换为响应DTO（传递包装结构字段）
                                    saveWeakQueryOrderWithTrainingData(orderBasic, distributerGoodsEntity, trainingData, note, i, responseMap,
                                            standardWeight, itemUnit, itemsPerCarton, cartonUnit);
                                    continue;
                                }
                                logger.warn("[recognizeOrderFast] 弱查询-部门商品历史引用的商品已不存在，disGoodsId={}，继续后续流程",
                                        departmentDisGoodsEntity.getNxDdgDisGoodsId());
                            }

                            // 2级弱查询（商品库名称+规格）已移除，交由 Service 统一处理，避免重复查询

                            // 3级弱查询：训练数据（部门ID + name 或 deepseek_recommended_name）
                            Map<String, Object> matchParamsWeak = new HashMap<>();
                            matchParamsWeak.put("depId", depId);
                            matchParamsWeak.put("goodsName", goodsName); // 使用 name 查询训练数据
                            matchParamsWeak.put("disGoodsId", 1);
                            NxOrderOcrTrainingDataEntity matchedTrainingDataWeak = nxOrderOcrTrainingDataService.queryByMatchFields(matchParamsWeak);

                            if (matchedTrainingDataWeak != null && matchedTrainingDataWeak.getNxOtdDisGoodsId() != null) {
                                // 训练数据中有商品ID，直接创建订单
                                logger.info("[recognizeOrderFast] 弱查询-找到匹配的训练数据，商品ID: {}, 订单状态设置为 -2（待修正）",
                                        matchedTrainingDataWeak.getNxOtdDisGoodsId());

                                // 查询商品信息
                                NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(
                                        matchedTrainingDataWeak.getNxOtdDisGoodsId());
                                if (disGoodsEntity == null) {
                                    logger.warn("[recognizeOrderFast] 弱查询-训练数据引用的商品已不存在，商品ID: {}，跳过此匹配",
                                            matchedTrainingDataWeak.getNxOtdDisGoodsId());
                                    // 不抛异常，继续到 searchGoods
                                } else {
                                // 保存弱查询订单（关联训练数据、添加推荐商品）并转换为响应DTO（传递包装结构字段）
                                // 注意：使用新创建的 trainingData，而不是 matchedTrainingDataWeak
                                saveWeakQueryOrderWithTrainingData(orderBasic, disGoodsEntity, trainingData, note, i, responseMap,
                                        standardWeight, itemUnit, itemsPerCarton, cartonUnit);
                                continue;
                                }
                            }

                            // name 的弱查询（部门历史、训练数据）都没找到，需要调用 searchGoods 进行模糊搜索
                            logger.info("[recognizeOrderFast] name 的弱查询都没有找到商品，将调用 searchGoods 进行模糊搜索: name={}", goodsName);

                            // 创建订单实体，用于 searchGoods
                            orderBasic.setNxDoStatus(-2);
                            orderBasic.setNxDoTrainingDataId(trainingData.getNxOtdId());
                            orderBasic.setNxDoQuantity(quantity);
                            orderBasic.setNxDoStandard(spec);

                            // 保存订单到 orderList，后续统一调用 searchGoods
                            // 同时保存包装结构字段，以便后续转换为 DTO 时使用
                            applyNxDepartmentOrderGoodsNameDbLimit(orderBasic);
                            orderList.add(orderBasic);
                            orderIndexList.add(i);
                            // 保存包装结构字段到 Map，key 为原始索引
                            Map<String, String> packagingFields = new HashMap<>();
                            packagingFields.put("standardWeight", standardWeight);
                            packagingFields.put("itemUnit", itemUnit);
                            packagingFields.put("itemsPerCarton", itemsPerCarton);
                            packagingFields.put("cartonUnit", cartonUnit);
                            orderIndexToPackagingFields.put(i, packagingFields);
                        }
                        } catch (Throwable lineEx) {
                            logger.error("[recognizeOrderFast] 第 {}/{} 条解析行处理失败，已跳过: {}",
                                    i + 1, itemsList.size(), lineEx.getMessage(), lineEx);
                        }
                    }

                    if (!orderList.isEmpty()) {
                        logger.info("[recognizeOrderFast] 需要调用 searchGoods 的订单数量: {}", orderList.size());
                        logger.info("[recognizeOrderFast] orderIndexList 大小: {}", orderIndexList.size());

                        List<NxDepartmentOrdersEntity> searchResults = nxDepartmentOrdersService.searchAndSaveOrdersFromOcr(orderList);
                        logger.info("[recognizeOrderFast] searchAndSaveOrdersFromOcr 返回结果数量: {}",
                                searchResults != null ? searchResults.size() : 0);

                        int maxSize = Math.min(searchResults != null ? searchResults.size() : 0, orderIndexList.size());
                        logger.info("[recognizeOrderFast] 开始处理搜索结果，最大处理数量: {}", maxSize);
                        for (int j = 0; j < maxSize; j++) {
                            Integer originalIndex = orderIndexList.get(j);
                            NxDepartmentOrdersEntity order = searchResults.get(j);
                            logger.info("[recognizeOrderFast] 处理搜索结果[{}]: originalIndex={}, order={}",
                                    j, originalIndex, order != null ? "not null" : "null");
                            if (order != null && originalIndex != null) {
                                Map<String, String> packagingFields = orderIndexToPackagingFields.get(originalIndex);
                                String standardWeight = packagingFields != null ? packagingFields.get("standardWeight") : "";
                                String itemUnit = packagingFields != null ? packagingFields.get("itemUnit") : "";
                                String itemsPerCarton = packagingFields != null ? packagingFields.get("itemsPerCarton") : "";
                                String cartonUnit = packagingFields != null ? packagingFields.get("cartonUnit") : "";

                                order.setStandardWeight(standardWeight != null ? standardWeight : "");
                                order.setItemUnit(itemUnit != null ? itemUnit : "");
                                order.setItemsPerCarton(itemsPerCarton != null ? itemsPerCarton : "");
                                order.setCartonUnit(cartonUnit != null ? cartonUnit : "");

                                if (order.getNxDoStatus() != null && order.getNxDoStatus() == -2) {
                                    if (order.getNxDistributerGoodsEntityList() == null || order.getNxDistributerGoodsEntityList().isEmpty()) {
                                        order = nxDepartmentOrdersService.addCommentsGoodsForOrder(order);
                                        logger.info("[recognizeOrderFast] 无候选列表，调用addCommentsGoodsForOrder: 订单ID={}, 推荐商品数量={}",
                                                order.getNxDepartmentOrdersId(),
                                                order.getNxDistributerGoodsEntityList() != null ? order.getNxDistributerGoodsEntityList().size() : 0);
                                    } else {
                                        for (NxDistributerGoodsEntity goods : order.getNxDistributerGoodsEntityList()) {
                                            if (goods.getDepartmentDisGoodsEntity() == null) {
                                                Map<String, Object> mapDep = new HashMap<>();
                                                mapDep.put("disId", disId);
                                                mapDep.put("depId", order.getNxDoDepartmentId());
                                                mapDep.put("disGoodsId", goods.getNxDistributerGoodsId());
                                                NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(mapDep);
                                                if (departmentDisGoodsEntity != null) {
                                                    goods.setDepartmentDisGoodsEntity(departmentDisGoodsEntity);
                                                }
                                            }
                                        }
                                        logger.info("[recognizeOrderFast] 订单已有候选列表，补全部门商品信息: 订单ID={}, 候选数量={}",
                                                order.getNxDepartmentOrdersId(), order.getNxDistributerGoodsEntityList().size());
                                    }
                                }

                                responseMap.put(originalIndex, order);
                            } else {
                                logger.warn("[recognizeOrderFast] 跳过无效的索引或订单: j={}, originalIndex={}, order={}",
                                        j, originalIndex, order != null ? "not null" : "null");
                            }
                        }

                        // 检查是否有未处理的订单
                        if (searchResults != null && searchResults.size() > orderIndexList.size()) {
                            logger.warn("[recognizeOrderFast] 警告：searchResults 数量({})大于 orderIndexList 数量({})，可能有订单未被处理",
                                    searchResults.size(), orderIndexList.size());
                        } else if (orderIndexList.size() > (searchResults != null ? searchResults.size() : 0)) {
                            logger.warn("[recognizeOrderFast] 警告：orderIndexList 数量({})大于 searchResults 数量({})，可能有订单未被处理",
                                    orderIndexList.size(), searchResults != null ? searchResults.size() : 0);
                        }

                        logger.info("[recognizeOrderFast] searchGoods 处理完成，responseMap 大小: {}", responseMap.size());
                    }

                    // 按照原始顺序组装最终的响应列表（直接返回订单实体）
                    List<NxDepartmentOrdersEntity> finalResponseList = new ArrayList<>();
                    for (int i = 0; i < itemsList.size(); i++) {
                        NxDepartmentOrdersEntity order = responseMap.get(i);
                        if (order != null) {
                            finalResponseList.add(order);
                        }
                    }
                    logger.info("[recognizeOrderFast] 订单处理完成，共 {} 条订单", finalResponseList.size());

                    // ========== 第九步：统计订单状态并更新任务 ==========
                    int[] statusCounts = nxDepartmentOrdersService.queryCountByOcrTaskIdGroupByStatus(ocrTaskId);
                    int completedOrders = statusCounts[0];
                    int pendingOrders = statusCounts[1];

                    // 更新任务状态
                    ocrTask.setNxOcrTaskCompletedOrders(completedOrders);
                    ocrTask.setNxOcrTaskPendingOrders(pendingOrders);
                    if (pendingOrders == 0) {
                        ocrTask.setNxOcrTaskStatus(2); // 2=已完成
                    } else if (completedOrders > 0) {
                        ocrTask.setNxOcrTaskStatus(1); // 1=部分完成
                    } else {
                        ocrTask.setNxOcrTaskStatus(1); // 1=部分完成（所有订单都是待修正）
                    }
                    ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
                    nxOcrTaskService.update(ocrTask);
                    logger.info("[recognizeOrderFast] 更新任务状态完成，任务ID: {}, 已完成订单: {}, 待修正订单: {}, 任务状态: {}", 
                            ocrTaskId, completedOrders, pendingOrders, ocrTask.getNxOcrTaskStatus());

                    // 记录订单处理耗时
                    long orderProcessElapsedTime = System.currentTimeMillis() - orderProcessStartTime;
                    logger.info("[recognizeOrderFast] 订单处理耗时: {} ms ({} 秒)", orderProcessElapsedTime, orderProcessElapsedTime / 1000.0);

                    // 记录总耗时
                    long methodTotalTime = System.currentTimeMillis() - methodStartTime;
                    logger.info("[recognizeOrderFast] ========== 方法总耗时统计 ==========");
                    logger.info("[recognizeOrderFast] 总耗时: {} ms ({} 秒)", methodTotalTime, methodTotalTime / 1000.0);
                    logger.info("[recognizeOrderFast] ====================================");

                    // 返回处理结果（包含任务ID，方便后续使用DeepSeek重新解析）
                    return R.ok().put("items", finalResponseList)
                            .put("taskId", ocrTaskId)
                            .put("task", ocrTask);

                } catch (Throwable e) {
                    logger.error("[recognizeOrderFast] 处理订单失败: {}", e.getMessage(), e);
                    if (ocrTask != null && ocrTaskId != null) {
                        try {
                            ocrTask.setNxOcrTaskStatus(-1);
                            ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
                            nxOcrTaskService.update(ocrTask);
                        } catch (Exception ex) {
                            logger.warn("[recognizeOrderFast] 更新任务为失败状态时出错: {}", ex.getMessage());
                        }
                    }
                    // 即使处理失败，也返回识别结果和任务ID（保证接口始终有 JSON 响应）
                    return R.ok()
                            .put("ocrText", ocrTextContent != null ? ocrTextContent : "")
                            .put("items", itemsList != null ? itemsList : new ArrayList<>())
                            .put("taskId", ocrTaskId)
                            .put("task", ocrTask)
                            .put("error", "处理订单失败: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
                }
            }

            // 返回 OCR 结果和解析后的商品列表
            logger.info("[recognizeOrderFast] 返回给前端，商品数量: {}", itemsList != null ? itemsList.size() : 0);

            // 记录总耗时
            long methodTotalTime = System.currentTimeMillis() - methodStartTime;
            logger.info("[recognizeOrderFast] ========== 方法总耗时统计 ==========");
            logger.info("[recognizeOrderFast] 总耗时: {} ms ({} 秒)", methodTotalTime, methodTotalTime / 1000.0);
            logger.info("[recognizeOrderFast] ====================================");

            return R.ok().put("ocrText", ocrTextContent != null ? ocrTextContent : "")
                    .put("items", itemsList != null ? itemsList : new ArrayList<>())
                    .put("task", ocrTask)
                    .put("taskId", ocrTaskId);
        } catch (Throwable e) {
            logger.error("[recognizeOrderFast] 系统错误: {}", e.getMessage(), e);
            String msg = "系统错误: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            R r = R.error(msg);
            if (ocrTaskId != null) {
                r.put("taskId", ocrTaskId);
            }
            if (ocrTextContent != null) {
                r.put("ocrText", ocrTextContent);
            }
            if (itemsList != null) {
                r.put("items", itemsList);
            }
            if (ocrTask != null) {
                r.put("task", ocrTask);
            }
            return r;
        }
    }

    
    //精简之前
//    @RequestMapping(value = "/recognizeOrderFast", method = RequestMethod.POST)
//    @ResponseBody
//    public R recognizeOrderFast(@RequestBody Map<String, Object> request) {
//        // 记录整个方法开始时间
//        long methodStartTime = System.currentTimeMillis();
//        Integer ocrTaskId = null;
//        try {
//            // ========== 第一步：验证必填参数 ==========
//            Integer depId, disId, depFatherId, userId;
//            try {
//                depId = getIntegerParam(request, "depId", true);
//                disId = getIntegerParam(request, "disId", true);
//                depFatherId = getIntegerParam(request, "depFatherId", true);
//                userId = getIntegerParam(request, "userId", true);
//            } catch (IllegalArgumentException e) {
//                return R.error(e.getMessage());
//            }
//
//            logger.info("[recognizeOrderFast] 接收参数：depId={}, disId={}, depFatherId={}, userId={}",
//                    depId, disId, depFatherId, userId);
//
//            // ========== 第二步：创建OCR任务记录 ==========
//            logger.info("[recognizeOrderFast] 开始创建OCR任务记录...");
//
//            // 获取用户信息（上传用户）
//            NxDistributerUserEntity uploadUser = null;
//            String uploadUserName = "未知用户";
//            Integer uploadUserId = null;
//            try {
//                uploadUser = nxDistributerUserService.queryObject(userId);
//                if (uploadUser != null) {
//                    uploadUserId = uploadUser.getNxDistributerUserId().intValue();
//                    uploadUserName = uploadUser.getNxDiuWxNickName() != null ? uploadUser.getNxDiuWxNickName() : "未知用户";
//                }
//            } catch (Exception e) {
//                logger.warn("[recognizeOrderAsync] 获取上传用户信息失败: {}", e.getMessage());
//            }
//
//
//            // 获取文件名（如果有）
//            Object fileNameObj = request.get("fileName");
//            String fileName = fileNameObj != null ? fileNameObj.toString() : "ocr_image_" + System.currentTimeMillis();
//
//            // 创建OCR任务记录
//            NxOcrTaskEntity ocrTask = new NxOcrTaskEntity();
//
//            NxDepartmentEntity nxDepartmentEntity = nxDepartmentService.queryObject(depFatherId);
//            String  depName = nxDepartmentEntity.getNxDepartmentAttrName();
//            System.out.println("depnidd====" + nxDepartmentEntity.getNxDepartmentFatherId());
//            if(nxDepartmentEntity.getNxDepartmentFatherId() != 0){
//                NxDepartmentEntity subDepartmentEntity = nxDepartmentService.queryObject(depId);
//                depName = depName + '.' + subDepartmentEntity.getNxDepartmentAttrName();
//            }
//            //查询这个 depFatherId 今日的第几个图片任务
//            Map<String, Object> mapTask = new HashMap<>();
//            mapTask.put("departmentFatherId", depFatherId);
//            mapTask.put("date", formatWhatDay(0));  // 使用 date 参数，格式：yyyy-MM-dd
//            mapTask.put("type", 1);
//            int count = nxOcrTaskService.queryTotalByDepartmentAndStatus(mapTask);
//            // count 是今日已有的任务数量，count + 1 就是今日第几个任务
//            int todayTaskNumber = count + 1;
//            logger.info("[recognizeOrderAsync] 查询今日任务数量: {}, 当前任务是今日第 {} 个", count, todayTaskNumber);
//            ocrTask.setNxOcrTaskFileName(depName + "第" + todayTaskNumber + "个图片订单");
//            ocrTask.setNxOcrTaskTotalOrders(0);
//            ocrTask.setNxOcrTaskCompletedOrders(0);
//            ocrTask.setNxOcrTaskPendingOrders(0);
//            ocrTask.setNxOcrTaskUploadTime(formatWhatYearDayTime(0));
//            ocrTask.setNxOcrTaskUploadUserId(uploadUserId);
//            ocrTask.setNxOcrTaskUploadUserName(uploadUserName);
//            ocrTask.setNxOcrTaskStatus(0); // 0=处理中
//            ocrTask.setNxOcrTaskCreateDate(formatWhatYearDayTime(0));
//            ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
//            ocrTask.setNxOcrTaskDistributerId(disId);
//            ocrTask.setNxOcrTaskDepartmentId(depId);
//            ocrTask.setNxOcrTaskDepartmentFatherId(depFatherId);
//            ocrTask.setNxOcrTaskType(1); // 1=图片（recognizeOrderFast）
//
//            // 保存任务记录（获取任务ID）
//            nxOcrTaskService.save(ocrTask);
//            ocrTaskId = ocrTask.getNxOcrTaskId();
//            logger.info("[recognizeOrderFast] OCR任务记录创建成功，任务ID: {}", ocrTaskId);
//
//            // ========== 第三步：获取图片并进行OCR识别 ==========
//            // 获取图片 Base64
//            Object imageBase64Obj = request.get("ImageBase64");
//            if (imageBase64Obj == null) {
//                logger.warn("[recognizeOrderFast] 图片数据为空");
//                return R.error("图片数据不能为空");
//            }
//            String imageBase64 = imageBase64Obj.toString();
//            if (imageBase64.isEmpty()) {
//                logger.warn("[recognizeOrderFast] 图片数据为空");
//                return R.error("图片数据不能为空");
//            }
//
//            // 处理 Base64 前缀（如果前端传了 data:image 前缀，需要去掉）
//            if (imageBase64.contains(",")) {
//                imageBase64 = imageBase64.substring(imageBase64.indexOf(",") + 1);
//            }
//
//            // 验证图片大小
//            if (imageBase64.length() > MAX_IMAGE_BASE64_SIZE) {
//                logger.warn("[recognizeOrderFast] 图片过大: {} bytes (限制: {} bytes)",
//                        imageBase64.length(), MAX_IMAGE_BASE64_SIZE);
//                return R.error("图片大小超过限制，最大支持 10MB（Base64 编码后）");
//            }
//
//            logger.info("[recognizeOrderFast] 开始识别和解析，图片大小: {} bytes", imageBase64.length());
//
//            // ========== 第四步：OCR识别 ==========
//            String ocrTextContent = performOcrRecognition(imageBase64);
//
//            if (ocrTextContent == null || ocrTextContent.trim().isEmpty()) {
//                logger.error("[recognizeOrderFast] OCR识别结果为空，任务ID: {}", ocrTaskId);
//                ocrTask.setNxOcrTaskStatus(-1); // -1=失败
//                ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
//                nxOcrTaskService.update(ocrTask);
//                return R.error("未识别到任何文本");
//            }
//
//            // ========== 第五步：保存图片到服务器并更新任务图片路径 ==========
//            try {
//                String imagePath = saveBase64Image(imageBase64, ocrTaskId);
//                ocrTask.setNxOcrTaskImagePath(imagePath);
//                ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
//                nxOcrTaskService.update(ocrTask);
//                logger.info("[recognizeOrderFast] 图片保存成功，路径: {}，任务ID: {}", imagePath, ocrTaskId);
//            } catch (Exception e) {
//                logger.error("[recognizeOrderFast] 保存图片失败，任务ID: {}", ocrTaskId, e);
//                // 图片保存失败不影响OCR识别，继续处理
//            }
//
//            // ✅ 内存优化：OCR识别完成后立即清空Base64图片数据引用，释放内存
//            // Base64图片数据通常占用1-5MB内存，清空后可以立即被GC回收
//            imageBase64 = null;
//
//            // ========== 第六步：保存OCR文本到任务表 ==========
//            ocrTask.setNxOcrTaskOcrText(ocrTextContent);
//            ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
//            nxOcrTaskService.update(ocrTask);
//            logger.info("[recognizeOrderFast] OCR文本已保存到任务表，任务ID: {}, 文本长度: {} 字符", ocrTaskId, ocrTextContent.length());
//
//            // ========== 第七步：使用规则解析（不使用DeepSeek） ==========
//            logger.info("[recognizeOrderFast] 开始使用规则解析订单文本...");
//            List<Map<String, Object>> itemsList = parseOrderTextByRule(ocrTextContent);
//
//            // 验证解析结果
//            if (itemsList.isEmpty()) {
//                logger.error("[recognizeOrderFast] 规则解析结果为空，任务ID: {}", ocrTaskId);
//                ocrTask.setNxOcrTaskStatus(-1); // -1=失败
//                ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
//                nxOcrTaskService.update(ocrTask);
//                return R.error("订单解析失败：规则解析结果为空，请检查图片格式是否符合要求（商品名+数量+单位）");
//            }
//
//            logger.info("[recognizeOrderFast] 规则解析成功，解析到 {} 个商品", itemsList.size());
//
//            // ========== 第八步：更新任务总订单数 ==========
//            ocrTask.setNxOcrTaskTotalOrders(itemsList.size());
//            ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
//            nxOcrTaskService.update(ocrTask);
//            logger.info("[recognizeOrderFast] 更新OCR任务总订单数: {}", itemsList.size());
//
//
//
//
//            // 如果提供了 depId 和 disId，则调用 pasteSearchGoods 方法查询商品并保存订单
//            if (!itemsList.isEmpty()) {
//                try {
//                    // 记录订单处理开始时间
//                    long orderProcessStartTime = System.currentTimeMillis();
//                    // 先查询当前最大的 today_order，为所有订单统一设置递增的 nxDoTodayOrder
//                    int currentMaxOrder = 0;
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("depFatherId", depId);
//                    Integer integer = nxDepartmentOrdersService.queryOrderGoodsCount(map);
//                    if(integer > 0){
//                        if (depId != null) {
//                            currentMaxOrder = nxDepartmentOrdersService.queryMaxTodayOrder(depId);
//                            logger.info("[pasteSearchGoods] 当前部门最大 today_order: {}, 即将处理 {} 个订单", currentMaxOrder);
//                        }
//                    }
//                    logger.info("[recognizeOrder] 当前部门最大 today_order: {}, 即将处理 {} 个订单", currentMaxOrder, itemsList.size());
//                    int todayOrderCounter = 0; // 用于跟踪当前订单的序号
//
//                    // 处理每条识别数据
//                    List<NxDepartmentOrdersEntity> orderList = new ArrayList<>();
//                    // 使用 Map 保存原始索引对应的响应，以保持顺序
//                    Map<Integer, NxDepartmentOrdersEntity> responseMap = new HashMap<>();
//                    List<Integer> orderIndexList = new ArrayList<>(); // 保存 orderList 中每个订单对应的原始索引
//                    // 保存原始索引对应的包装结构字段，用于 searchGoods 返回后设置到订单实体
//                    Map<Integer, Map<String, String>> orderIndexToPackagingFields = new HashMap<>();
//
//                    for (int i = 0; i < itemsList.size(); i++) {
//                        Map<String, Object> item = itemsList.get(i);
//
//                        // 订单中的商品名称使用 name（纠错后的名称）
//                        String goodsName = item.get("name") != null ? item.get("name").toString().trim() : "";
//                        // 训练数据使用 rawName（原始OCR识别的名称），如果没有 rawName 则使用 name
//                        String rawName = item.get("rawName") != null && !item.get("rawName").toString().trim().isEmpty()
//                                ? item.get("rawName").toString().trim() : goodsName;
//                        String quantity = item.get("quantity") != null ? item.get("quantity").toString().trim() : "";
//                        String spec = item.get("spec") != null ? item.get("spec").toString().trim() : "";
//                        // 获取数量和规格是否改变（用于判断订单状态）
//                        Boolean quantityIsChange = item.get("quantityIsChange") != null
//                                ? (Boolean) item.get("quantityIsChange") : false;
//                        Boolean specIsChange = item.get("specIsChange") != null
//                                ? (Boolean) item.get("specIsChange") : false;
//                        String standardWeight = item.get("standardWeight") != null ? item.get("standardWeight").toString().trim() : "";
//                        String itemUnit = item.get("itemUnit") != null ? item.get("itemUnit").toString().trim() : "";
//                        String itemsPerCarton = item.get("itemsPerCarton") != null ? item.get("itemsPerCarton").toString().trim() : "";
//                        String cartonUnit = item.get("cartonUnit") != null ? item.get("cartonUnit").toString().trim() : "";
//                        String note = item.get("note") != null ? item.get("note").toString().trim() : "";
//                        String isNotice = item.get("isNotice") != null ? item.get("isNotice").toString().trim() : "";
//                        // OCR原始文本，去除所有空格以便后续匹配
//                        String originalText = item.get("originalText") != null
//                                ? item.get("originalText").toString().trim().replaceAll("\\s+", "") : "";
//
//                        if (goodsName.isEmpty()) {
//                            logger.warn("[recognizeOrder] 跳过商品：名称为空");
//                            continue;
//                        }
//
//                        // 判断是否缺少数量或规格（3个必备条件：商品名称、数量、规格）
//                        boolean isDataMissing = (quantity.trim().isEmpty()) && (spec.trim().isEmpty());
//
//                        //0创建基本订单实体
//                        NxDepartmentOrdersEntity orderBasic = new NxDepartmentOrdersEntity();
//                        orderBasic.setNxDoDepartmentId(depId);
//                        orderBasic.setNxDoDepartmentFatherId(depFatherId);
//                        orderBasic.setNxDoDistributerId(disId);
//                        orderBasic.setNxDoOrderUserId(userId);
//                        orderBasic.setNxDoGoodsName(goodsName);
//                        orderBasic.setNxDoRemark(note);
//                        orderBasic.setOrcNotice(isNotice);
//                        orderBasic.setNxDoPurchaseUserId(-1);
//                        orderBasic.setNxDoIsAgent(-1);
//                        orderBasic.setNxDoQuantity(quantity);
//                        orderBasic.setNxDoStandard(spec);
//                        // 关联OCR任务ID
//                        orderBasic.setNxDoOcrTaskId(ocrTaskId);
//                        // 设置包装结构字段
//                        orderBasic.setStandardWeight(standardWeight);
//                        orderBasic.setItemUnit(itemUnit);
//                        orderBasic.setItemsPerCarton(itemsPerCarton);
//                        orderBasic.setCartonUnit(cartonUnit);
//                        // 预先设置 nxDoTodayOrder，确保顺序正确
//                        int todayOrder = currentMaxOrder + todayOrderCounter + 1;
//                        orderBasic.setNxDoTodayOrder(todayOrder);
//                        logger.info("[recognizeOrderFast] 设置订单 todayOrder: 商品名称={}, 原始索引={}, todayOrder={}, currentMaxOrder={}, counter={}",
//                                goodsName, i, todayOrder, currentMaxOrder, todayOrderCounter);
//                        todayOrderCounter++;
//
//                        // 如果订单缺少3个必备条件（商品名称、数量、规格），设置状态为-2，保存订单，跳过后续处理
//                        if (isDataMissing) {
//                            logger.info("[recognizeOrderFast] 订单缺少数量或规格，设置状态为-2并保存订单，跳过后续商品搜索和训练数据添加: goodsName={}, quantity={}, spec={}",
//                                    goodsName, quantity, spec);
//                            orderBasic.setNxDoStatus(-2);
//                            saveOrderWithoutGoods(orderBasic);
//                            // 将订单添加到响应Map
//                            responseMap.put(i, orderBasic);
//                            continue;
//                        }
//
//                        //0级有限匹配： 查询训练数据有goodsId的解析原文，如果当前原文是否完全匹配，则直接推断为改商品；
//                        Map<String, Object> traGoodsMap = new HashMap<>();
//                        traGoodsMap.put("depId", depId);
//                        traGoodsMap.put("otdOcrText", originalText);
//                        traGoodsMap.put("disGoodsId", 1);
//                        System.out.println("trramapap" + traGoodsMap);
//                        NxOrderOcrTrainingDataEntity dataEntity = nxOrderOcrTrainingDataService.queryByMatchFields(traGoodsMap);
//                        if(dataEntity != null){
//                            Integer nxOtdDisGoodsId = dataEntity.getNxOtdDisGoodsId();
//                            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(nxOtdDisGoodsId);
//
//                            // 根据数量和规格是否改变设置订单状态
//                            orderBasic.setNxDoStatus(0);
//                            orderBasic.setNxDoStandard(dataEntity.getNxOtdFinalStandard());
//                            orderBasic.setStandardWeight(dataEntity.getNxOtdFinalStandardWeight());
//                            orderBasic.setNxDoRemark(dataEntity.getNxOtdFinalRemark());
//                            saveStrongQueryOrderAndConvert(orderBasic, distributerGoodsEntity,  i, responseMap);
//                            continue;
//                        }
//
//                        // 1级优先匹配：首先查询部门商品历史记录，如果订货商品名称和规格完全匹配，则直接推断为该商品
//                        Map<String, Object> depGoodsMap = new HashMap<>();
//                        depGoodsMap.put("depId", depId);
//                        depGoodsMap.put("name", rawName);
//                        depGoodsMap.put("standard", spec);
//                        List<NxDepartmentDisGoodsEntity> departmentDisGoodsList = nxDepartmentDisGoodsService.queryDepartmentGoods(depGoodsMap);
//                        logger.info("[recognizeOrderFast] 优先匹配-部门商品历史记录查询结果数量: {}", departmentDisGoodsList.size());
//                        // 如果部门商品历史记录中有完全匹配的（名称+规格），则直接使用该商品
//                        if (departmentDisGoodsList.size() == 1) {
//                            NxDepartmentDisGoodsEntity departmentDisGoodsEntity = departmentDisGoodsList.get(0);
//                            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(departmentDisGoodsEntity.getNxDdgDisGoodsId());
//
//                            // 根据数量和规格是否改变设置订单状态
//                            setOrderStatusByChangeFlag(orderBasic, specIsChange, quantityIsChange);
//                            // 保存订单并转换为响应DTO（传递包装结构字段）
//                            saveStrongQueryOrderAndConvert(orderBasic, distributerGoodsEntity,  i, responseMap);
//                            continue;
//                        }
//
//                        // 2级优先匹配：商品库，如果订货商品名称和规格完全匹配，则直接推断为该商品
//                        Map<String, Object> mapZero = new HashMap<>();
//                        mapZero.put("disId", disId);
//                        mapZero.put("searchStr", rawName);
//                        mapZero.put("standard", spec);
//                        logger.info("[recognizeOrderFast] 一级匹配-商品名称+规格查询参数: {}", mapZero);
//                        List<NxDistributerGoodsEntity> distributerGoodsEntitiesZero = nxDistributerGoodsService.queryDisGoodsByName(mapZero);
//                        // 如果商品历史记录中有完全匹配的（名称+规格），则直接使用该商品
//                        if (distributerGoodsEntitiesZero.size() == 1) {
//                            NxDistributerGoodsEntity distributerGoodsEntity = distributerGoodsEntitiesZero.get(0);
//                            logger.info("[recognizeOrderFast] 一级匹配：商品库，，直接保存订单，disGoodsId={}",
//                                    distributerGoodsEntity.getNxDistributerGoodsId());
//
//                            // 根据数量和规格是否改变设置订单状态
//                            setOrderStatusByChangeFlag(orderBasic, specIsChange, quantityIsChange);
//
//                            // 保存订单并转换为响应DTO（传递包装结构字段）
//                            saveStrongQueryOrderAndConvert(orderBasic, distributerGoodsEntity,  i, responseMap);
//
//                            continue;
//                        }
//
//                        // 3级优先匹配：商品库别名精准查询，如果订货商品名称和规格完全匹配，则直接推断为该商品
//                        Map<String, Object> mapA = new HashMap<>();
//                        mapA.put("disId", disId);
//                        mapA.put("alias", goodsName);
//                        mapA.put("standard", spec);
//                        List<NxDistributerGoodsEntity> distributerGoodsEntitiesAlias = nxDistributerGoodsService.queryDisGoodsByAlias(mapA);
//                        // 如果商品历史记录中有完全匹配的（名称+规格），则直接使用该商品
//                        if (distributerGoodsEntitiesAlias.size() == 1) {
//                            NxDistributerGoodsEntity distributerGoodsEntity = distributerGoodsEntitiesAlias.get(0);
//                            logger.info("[recognizeOrderFast] 3级优先匹配-商品库别名精准查询，直接保存订单，disGoodsId={}",
//                                    distributerGoodsEntity.getNxDistributerGoodsId());
//                            // 根据数量和规格是否改变设置订单状态
//                            setOrderStatusByChangeFlag(orderBasic, specIsChange, quantityIsChange);
//                            // 保存订单并转换为响应DTO（传递包装结构字段）
//                            saveStrongQueryOrderAndConvert(orderBasic, distributerGoodsEntity,  i, responseMap);
//                            continue;
//                        }
//
//                        // 4级优先匹配：查询训练表中是否有相同内容（匹配：部门ID + 原始商品名称 rawName）
//                        Map<String, Object> matchParams = new HashMap<>();
//                        matchParams.put("depId", depId);
//                        matchParams.put("disGoodsId", 1); // 先查询有商品ID的
//                        matchParams.put("goodsName", rawName); //
//                        NxOrderOcrTrainingDataEntity matchedTrainingData = nxOrderOcrTrainingDataService.queryByMatchFields(matchParams);
//
//                        if (matchedTrainingData != null && matchedTrainingData.getNxOtdDisGoodsId() != null) {
//                            // 训练数据中有商品ID，直接创建订单
//                            logger.info("[recognizeOrderFast] 找到匹配的训练数据，商品ID: {}, 直接创建订单",
//                                    matchedTrainingData.getNxOtdDisGoodsId());
//
//                            // 查询商品信息
//                            NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(
//                                    matchedTrainingData.getNxOtdDisGoodsId());
//                            if (disGoodsEntity == null) {
//                                logger.error("[recognizeOrderFast] 商品不存在，商品ID: {}",
//                                        matchedTrainingData.getNxOtdDisGoodsId());
//                                throw new RuntimeException("商品不存在，商品ID: " + matchedTrainingData.getNxOtdDisGoodsId());
//                            }
//
//                            // 优先使用训练数据的 final_goods_name（如果存在），否则使用规则解析的 name
//                            String finalGoodsName = matchedTrainingData.getNxOtdFinalGoodsName();
//                            String orderGoodsName = (finalGoodsName != null && !finalGoodsName.trim().isEmpty())
//                                    ? finalGoodsName
//                                    : goodsName;
//                            if (!orderGoodsName.equals(goodsName)) {
//                                logger.info("[recognizeOrderFast] 使用训练数据的 final_goods_name='{}' 替代规则解析的 name='{}'",
//                                        orderGoodsName, goodsName);
//                            }
//                            orderBasic.setNxDoGoodsName(orderGoodsName);
//
//                            System.out.println("再次确认规格" + matchedTrainingData.getNxOtdOriginalStandard() + "specshi" + spec);
//                            if(matchedTrainingData.getNxOtdOriginalStandard().equals(spec)){
//                                orderBasic.setNxDoStatus(0);
//                                orderBasic.setNxDoStandard(matchedTrainingData.getNxOtdFinalStandard());
//                                orderBasic.setStandardWeight(matchedTrainingData.getNxOtdFinalStandardWeight());
//                                orderBasic.setNxDoRemark(matchedTrainingData.getNxOtdFinalRemark());
//                            }else{
//                                // 根据数量和规格是否改变设置订单状态
//                                setOrderStatusByChangeFlag(orderBasic, specIsChange, quantityIsChange);
//                            }
//
//                            // 保存订单并转换为响应DTO（传递包装结构字段）
//                            saveStrongQueryOrderAndConvert(orderBasic, disGoodsEntity,  i, responseMap);
//                            continue;
//                        }
//
//
//                        //5级 快速图片解析训练查询满足订单内容完全匹配训练数据订单内容，则视为找到唯一商品
//
//                        Map<String, Object> matchParamsText = new HashMap<>();
//                        matchParamsText.put("depId", depId);
//                        matchParamsText.put("disGoodsId", 1); // 先查询有商品ID的
////                        matchParamsText.put("goodsName", rawName); //
//                        // 添加 orderText 参数：图片解析的每一行的订单内容去除空格后作为 orderText
//                        matchParamsText.put("otdOcrText", originalText); // 去除空格后的原始订单文本
//                        logger.info("[recognizeOrderFast] matchedTrainingDataText找到匹配的训练数据Map，商品ID: {}, 直接创建订单",matchParamsText);
//                        NxOrderOcrTrainingDataEntity matchedTrainingDataText = nxOrderOcrTrainingDataService.queryByMatchFields(matchParamsText);
//
//                        if (matchedTrainingDataText != null && matchedTrainingDataText.getNxOtdDisGoodsId() != null) {
//                            // 训练数据中有商品ID，直接创建订单
//                            logger.info("[recognizeOrderFast] matchedTrainingDataText找到匹配的训练数据，商品ID: {}, 直接创建订单",
//                                    matchedTrainingDataText.getNxOtdDisGoodsId());
//
//                            // 查询商品信息
//                            NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(
//                                    matchedTrainingDataText.getNxOtdDisGoodsId());
//                            if (disGoodsEntity == null) {
//                                logger.error("[recognizeOrderFast] 商品不存在，商品ID: {}",
//                                        matchedTrainingDataText.getNxOtdDisGoodsId());
//                                throw new RuntimeException("商品不存在，商品ID: " + matchedTrainingDataText.getNxOtdDisGoodsId());
//                            }
//
//                            // 优先使用训练数据的 final_goods_name（如果存在），否则使用规则解析的 name
//                            String finalGoodsName = matchedTrainingDataText.getNxOtdFinalGoodsName();
//                            String orderGoodsName = (finalGoodsName != null && !finalGoodsName.trim().isEmpty())
//                                    ? finalGoodsName
//                                    : goodsName;
//                            if (!orderGoodsName.equals(goodsName)) {
//                                logger.info("[recognizeOrderFast] 使用训练数据的 final_goods_name='{}' 替代规则解析的 name='{}'",
//                                        orderGoodsName, goodsName);
//                            }
//                            orderBasic.setNxDoGoodsName(orderGoodsName);
//
//                            orderBasic.setNxDoStatus(0);
//                            orderBasic.setNxDoStandard(matchedTrainingDataText.getNxOtdFinalStandard());
//                            orderBasic.setStandardWeight(matchedTrainingDataText.getNxOtdFinalStandardWeight());
//                            orderBasic.setNxDoRemark(matchedTrainingDataText.getNxOtdFinalRemark());
//
//                            // 保存订单并转换为响应DTO（传递包装结构字段）
//                            saveStrongQueryOrderAndConvert(orderBasic, disGoodsEntity,  i, responseMap);
//                            continue;
//                        }
//
//                        // 以上是用 rawName 进行4种强查询（0级：otdOcrText、1级：部门商品历史记录、2级：商品库、3级：商品库别名、4级：训练数据rawName有商品ID）
//                        // 如果以上查询都没有找到商品，则创建或查询训练数据（无商品ID限制）
//                        if (responseMap.get(i) == null) {
//                            logger.info("[recognizeOrderFast] rawName 的4种强查询都没有找到商品，创建或查询训练数据: rawName={}, name={}",
//                                    rawName, goodsName);
//
//                            // 创建或查询训练数据：original_goods_name = rawName, deepseek_recommended_name = name
//                            // 注意：createOrQueryTrainingData 内部会查询一次（无商品ID限制），但第2596行已经查询过有商品ID的了
//                            // 为了避免重复查询，可以先查询一次无商品ID限制的，如果找到了就直接使用
//                            Map<String, Object> matchParamsNoGoodsId = new HashMap<>();
//                            matchParamsNoGoodsId.put("depId", depId);
//                            matchParamsNoGoodsId.put("goodsName", rawName);
//                            // 不设置 disGoodsId，查询所有训练数据（包括没有商品ID的）
//                            NxOrderOcrTrainingDataEntity existingTrainingData = nxOrderOcrTrainingDataService.queryByMatchFields(matchParamsNoGoodsId);
//
//                            NxOrderOcrTrainingDataEntity trainingData;
//                            if (existingTrainingData != null) {
//                                // 找到了已有的训练数据（没有商品ID），直接使用
//                                logger.info("[recognizeOrderFast] 找到已有的训练数据（无商品ID），训练数据ID: {}", existingTrainingData.getNxOtdId());
//                                trainingData = existingTrainingData;
//                            } else {
//                                // 没有找到，创建新的训练数据
//                                trainingData = new NxOrderOcrTrainingDataEntity();
//                                trainingData.setNxOtdDepartmentId(depId);
//                                trainingData.setNxOtdDepartmentFatherId(depFatherId);
//                                trainingData.setNxOtdDistributerId(disId);
//                                trainingData.setNxOtdOriginalGoodsName(rawName);
//                                trainingData.setNxOtdDeepseekRecommendedName(goodsName);
//                                trainingData.setNxOtdOriginalQuantity(quantity);
//                                trainingData.setNxOtdOriginalStandard(spec);
//                                trainingData.setNxOtdOriginalStandardWeight(standardWeight);
//                                trainingData.setNxOtdOriginalRemark(note);
//                                trainingData.setNxOtdOcrText(originalText != null ? originalText : "");
//                                trainingData.setNxOtdIsNameManuallyAnnotated(0);
//                                trainingData.setNxOtdIsQuantityManuallyAnnotated(0);
//                                trainingData.setNxOtdIsStandardManuallyAnnotated(0);
//                                trainingData.setNxOtdIsStandardWeightManuallyAnnotated(0);
//                                trainingData.setNxOtdIsRemarkManuallyAnnotated(0);
//                                trainingData.setNxOtdFinalGoodsName(null);
//                                trainingData.setNxOtdFinalQuantity(null);
//                                trainingData.setNxOtdFinalStandard(null);
//                                trainingData.setNxOtdFinalStandardWeight(null);
//                                trainingData.setNxOtdFinalRemark(null);
//                                trainingData.setNxOtdDataSource("OCR_IMAGE");
//                                trainingData.setNxOtdCreateDate(formatWhatYearDayTime(0));
//                                trainingData.setNxOtdUpdateDate(formatWhatYearDayTime(0));
//                                trainingData.setNxOtdCreateUserId(userId);
//                                nxOrderOcrTrainingDataService.save(trainingData);
//                                logger.info("[recognizeOrderFast] 训练数据创建成功，训练数据ID: {}", trainingData.getNxOtdId());
//                            }
//
//                            // 以下用 name 进行3种弱查询，订单状态统一为 -2（因为属于弱查询，需要人工确认）
//                            // 1级弱查询：部门商品历史记录（名称+规格）
//                            Map<String, Object> depGoodsMapWeak = new HashMap<>();
//                            depGoodsMapWeak.put("depId", depId);
//                            depGoodsMapWeak.put("name", goodsName);
//                            depGoodsMapWeak.put("standard", spec);
//                            List<NxDepartmentDisGoodsEntity> departmentDisGoodsListWeak = nxDepartmentDisGoodsService.queryDepartmentGoods(depGoodsMapWeak);
//                            logger.info("[recognizeOrderFast] 弱查询-部门商品历史记录查询结果数量: {}", departmentDisGoodsListWeak.size());
//
//                            if (departmentDisGoodsListWeak.size() == 1) {
//                                NxDepartmentDisGoodsEntity departmentDisGoodsEntity = departmentDisGoodsListWeak.get(0);
//                                NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(departmentDisGoodsEntity.getNxDdgDisGoodsId());
//
//                                logger.info("[recognizeOrderFast] 弱查询找到商品，订单状态设置为 -2（待修正）: disGoodsId={}",
//                                        distributerGoodsEntity.getNxDistributerGoodsId());
//
//                                // 保存弱查询订单（关联训练数据、添加推荐商品）并转换为响应DTO（传递包装结构字段）
//                                saveWeakQueryOrderWithTrainingData(orderBasic, distributerGoodsEntity, trainingData, note, i, responseMap,
//                                        standardWeight, itemUnit, itemsPerCarton, cartonUnit);
//                                continue;
//                            }
//
//                            // 2级弱查询：商品库（名称+规格）
//                            Map<String, Object> mapZeroWeak = new HashMap<>();
//                            mapZeroWeak.put("disId", disId);
//                            mapZeroWeak.put("searchStr", goodsName);
//                            mapZeroWeak.put("standard", spec);
//                            logger.info("[recognizeOrderFast] 弱查询-商品名称+规格查询参数: {}", mapZeroWeak);
//                            List<NxDistributerGoodsEntity> distributerGoodsEntitiesZeroWeak = nxDistributerGoodsService.queryDisGoodsByName(mapZeroWeak);
//
//                            if (distributerGoodsEntitiesZeroWeak.size() == 1) {
//                                NxDistributerGoodsEntity distributerGoodsEntity = distributerGoodsEntitiesZeroWeak.get(0);
//                                logger.info("[recognizeOrderFast] 弱查询找到商品，订单状态设置为 -2（待修正）: disGoodsId={}",
//                                        distributerGoodsEntity.getNxDistributerGoodsId());
//
//                                // 保存弱查询订单（关联训练数据、添加推荐商品）并转换为响应DTO（传递包装结构字段）
//                                saveWeakQueryOrderWithTrainingData(orderBasic, distributerGoodsEntity, trainingData, note, i, responseMap,
//                                        standardWeight, itemUnit, itemsPerCarton, cartonUnit);
//                                continue;
//                            }
//
//                            // 3级弱查询：训练数据（部门ID + name 或 deepseek_recommended_name）
//                            Map<String, Object> matchParamsWeak = new HashMap<>();
//                            matchParamsWeak.put("depId", depId);
//                            matchParamsWeak.put("goodsName", goodsName); // 使用 name 查询训练数据
//                            matchParamsWeak.put("disGoodsId", 1);
//                            NxOrderOcrTrainingDataEntity matchedTrainingDataWeak = nxOrderOcrTrainingDataService.queryByMatchFields(matchParamsWeak);
//
//                            if (matchedTrainingDataWeak != null && matchedTrainingDataWeak.getNxOtdDisGoodsId() != null) {
//                                // 训练数据中有商品ID，直接创建订单
//                                logger.info("[recognizeOrderFast] 弱查询-找到匹配的训练数据，商品ID: {}, 订单状态设置为 -2（待修正）",
//                                        matchedTrainingDataWeak.getNxOtdDisGoodsId());
//
//                                // 查询商品信息
//                                NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(
//                                        matchedTrainingDataWeak.getNxOtdDisGoodsId());
//                                if (disGoodsEntity == null) {
//                                    logger.error("[recognizeOrderFast] 商品不存在，商品ID: {}",
//                                            matchedTrainingDataWeak.getNxOtdDisGoodsId());
//                                    throw new RuntimeException("商品不存在，商品ID: " + matchedTrainingDataWeak.getNxOtdDisGoodsId());
//                                }
//
//                                // 保存弱查询订单（关联训练数据、添加推荐商品）并转换为响应DTO（传递包装结构字段）
//                                // 注意：使用新创建的 trainingData，而不是 matchedTrainingDataWeak
//                                saveWeakQueryOrderWithTrainingData(orderBasic, disGoodsEntity, trainingData, note, i, responseMap,
//                                        standardWeight, itemUnit, itemsPerCarton, cartonUnit);
//                                continue;
//                            }
//
//                            // name 的3种弱查询都没找到，需要调用 searchGoods 进行模糊搜索
//                            logger.info("[recognizeOrderFast] name 的3种弱查询都没有找到商品，将调用 searchGoods 进行模糊搜索: name={}", goodsName);
//
//                            // 创建订单实体，用于 searchGoods
//                            orderBasic.setNxDoStatus(-2);
//                            orderBasic.setNxDoTrainingDataId(trainingData.getNxOtdId());
//                            orderBasic.setNxDoQuantity(quantity);
//                            orderBasic.setNxDoStandard(spec);
//
//                            // 保存订单到 orderList，后续统一调用 searchGoods
//                            // 同时保存包装结构字段，以便后续转换为 DTO 时使用
//                            orderList.add(orderBasic);
//                            orderIndexList.add(i);
//                            // 保存包装结构字段到 Map，key 为原始索引
//                            Map<String, String> packagingFields = new HashMap<>();
//                            packagingFields.put("standardWeight", standardWeight);
//                            packagingFields.put("itemUnit", itemUnit);
//                            packagingFields.put("itemsPerCarton", itemsPerCarton);
//                            packagingFields.put("cartonUnit", cartonUnit);
//                            orderIndexToPackagingFields.put(i, packagingFields);
//                        }
//                    }
//
//                    if (!orderList.isEmpty()) {
//                        logger.info("[recognizeOrderFast] 需要调用 searchGoods 的订单数量: {}", orderList.size());
//                        logger.info("[recognizeOrderFast] orderIndexList 大小: {}", orderIndexList.size());
//
//                        List<NxDepartmentOrdersEntity> searchResults = nxDepartmentOrdersService.searchAndSaveOrdersFromOcr(orderList);
//                        logger.info("[recognizeOrderFast] searchAndSaveOrdersFromOcr 返回结果数量: {}",
//                                searchResults != null ? searchResults.size() : 0);
//
//                        int maxSize = Math.min(searchResults != null ? searchResults.size() : 0, orderIndexList.size());
//                        logger.info("[recognizeOrderFast] 开始处理搜索结果，最大处理数量: {}", maxSize);
//                        for (int j = 0; j < maxSize; j++) {
//                            Integer originalIndex = orderIndexList.get(j);
//                            NxDepartmentOrdersEntity order = searchResults.get(j);
//                            logger.info("[recognizeOrderFast] 处理搜索结果[{}]: originalIndex={}, order={}",
//                                    j, originalIndex, order != null ? "not null" : "null");
//                            if (order != null && originalIndex != null) {
//                                Map<String, String> packagingFields = orderIndexToPackagingFields.get(originalIndex);
//                                String standardWeight = packagingFields != null ? packagingFields.get("standardWeight") : "";
//                                String itemUnit = packagingFields != null ? packagingFields.get("itemUnit") : "";
//                                String itemsPerCarton = packagingFields != null ? packagingFields.get("itemsPerCarton") : "";
//                                String cartonUnit = packagingFields != null ? packagingFields.get("cartonUnit") : "";
//
//                                order.setStandardWeight(standardWeight != null ? standardWeight : "");
//                                order.setItemUnit(itemUnit != null ? itemUnit : "");
//                                order.setItemsPerCarton(itemsPerCarton != null ? itemsPerCarton : "");
//                                order.setCartonUnit(cartonUnit != null ? cartonUnit : "");
//
//                                if (order.getNxDoStatus() != null && order.getNxDoStatus() == -2) {
//                                    if (order.getNxDistributerGoodsEntityList() == null || order.getNxDistributerGoodsEntityList().isEmpty()) {
//                                        order = nxDepartmentOrdersService.addCommentsGoodsForOrder(order);
//                                        logger.info("[recognizeOrderFast] 无候选列表，调用addCommentsGoodsForOrder: 订单ID={}, 推荐商品数量={}",
//                                                order.getNxDepartmentOrdersId(),
//                                                order.getNxDistributerGoodsEntityList() != null ? order.getNxDistributerGoodsEntityList().size() : 0);
//                                    } else {
//                                        for (NxDistributerGoodsEntity goods : order.getNxDistributerGoodsEntityList()) {
//                                            if (goods.getDepartmentDisGoodsEntity() == null) {
//                                                Map<String, Object> mapDep = new HashMap<>();
//                                                mapDep.put("depId", order.getNxDoDepartmentId());
//                                                mapDep.put("disGoodsId", goods.getNxDistributerGoodsId());
//                                                NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(mapDep);
//                                                if (departmentDisGoodsEntity != null) {
//                                                    goods.setDepartmentDisGoodsEntity(departmentDisGoodsEntity);
//                                                }
//                                            }
//                                        }
//                                        logger.info("[recognizeOrderFast] 订单已有候选列表，补全部门商品信息: 订单ID={}, 候选数量={}",
//                                                order.getNxDepartmentOrdersId(), order.getNxDistributerGoodsEntityList().size());
//                                    }
//                                }
//
//                                responseMap.put(originalIndex, order);
//                            } else {
//                                logger.warn("[recognizeOrderFast] 跳过无效的索引或订单: j={}, originalIndex={}, order={}",
//                                        j, originalIndex, order != null ? "not null" : "null");
//                            }
//                        }
//
//                        // 检查是否有未处理的订单
//                        if (searchResults != null && searchResults.size() > orderIndexList.size()) {
//                            logger.warn("[recognizeOrderFast] 警告：searchResults 数量({})大于 orderIndexList 数量({})，可能有订单未被处理",
//                                    searchResults.size(), orderIndexList.size());
//                        } else if (orderIndexList.size() > (searchResults != null ? searchResults.size() : 0)) {
//                            logger.warn("[recognizeOrderFast] 警告：orderIndexList 数量({})大于 searchResults 数量({})，可能有订单未被处理",
//                                    orderIndexList.size(), searchResults != null ? searchResults.size() : 0);
//                        }
//
//                        logger.info("[recognizeOrderFast] searchGoods 处理完成，responseMap 大小: {}", responseMap.size());
//                    }
//
//                    // 按照原始顺序组装最终的响应列表（直接返回订单实体）
//                    List<NxDepartmentOrdersEntity> finalResponseList = new ArrayList<>();
//                    for (int i = 0; i < itemsList.size(); i++) {
//                        NxDepartmentOrdersEntity order = responseMap.get(i);
//                        if (order != null) {
//                            finalResponseList.add(order);
//                        }
//                    }
//                    logger.info("[recognizeOrderFast] 订单处理完成，共 {} 条订单", finalResponseList.size());
//
//                    // ========== 第九步：统计订单状态并更新任务 ==========
//                    int completedOrders = 0;
//                    int pendingOrders = 0;
//                    for (NxDepartmentOrdersEntity order : finalResponseList) {
//                        if (order.getNxDoStatus() != null) {
//                            if (order.getNxDoStatus() == 0) {
//                                completedOrders++;
//                            } else if (order.getNxDoStatus() == -2) {
//                                pendingOrders++;
//                            }
//                        }
//                    }
//
//                    // 更新任务状态
//                    ocrTask.setNxOcrTaskCompletedOrders(completedOrders);
//                    ocrTask.setNxOcrTaskPendingOrders(pendingOrders);
//                    if (pendingOrders == 0) {
//                        ocrTask.setNxOcrTaskStatus(2); // 2=已完成
//                    } else if (completedOrders > 0) {
//                        ocrTask.setNxOcrTaskStatus(1); // 1=部分完成
//                    } else {
//                        ocrTask.setNxOcrTaskStatus(1); // 1=部分完成（所有订单都是待修正）
//                    }
//                    ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
//                    nxOcrTaskService.update(ocrTask);
//                    logger.info("[recognizeOrderFast] 更新任务状态完成，任务ID: {}, 已完成订单: {}, 待修正订单: {}, 任务状态: {}",
//                            ocrTaskId, completedOrders, pendingOrders, ocrTask.getNxOcrTaskStatus());
//
//                    // 记录订单处理耗时
//                    long orderProcessElapsedTime = System.currentTimeMillis() - orderProcessStartTime;
//                    logger.info("[recognizeOrderFast] 订单处理耗时: {} ms ({} 秒)", orderProcessElapsedTime, orderProcessElapsedTime / 1000.0);
//
//                    // 记录总耗时
//                    long methodTotalTime = System.currentTimeMillis() - methodStartTime;
//                    logger.info("[recognizeOrderFast] ========== 方法总耗时统计 ==========");
//                    logger.info("[recognizeOrderFast] 总耗时: {} ms ({} 秒)", methodTotalTime, methodTotalTime / 1000.0);
//                    logger.info("[recognizeOrderFast] ====================================");
//
//                    // 返回处理结果（包含任务ID，方便后续使用DeepSeek重新解析）
//                    return R.ok().put("items", finalResponseList)
//                            .put("taskId", ocrTaskId)
//                            .put("task", ocrTask);
//
//                } catch (Exception e) {
//                    logger.error("[recognizeOrderFast] 处理订单失败: {}", e.getMessage(), e);
//                    // 即使处理失败，也返回识别结果和任务ID
//                    return R.ok().put("ocrText", ocrTextContent)
//                            .put("items", itemsList != null ? itemsList : new ArrayList<>())
//                            .put("taskId", ocrTaskId)
//                            .put("error", "处理订单失败: " + e.getMessage());
//                }
//            }
//
//            // 返回 OCR 结果和解析后的商品列表
//            logger.info("[recognizeOrderFast] 返回给前端，商品数量: {}", itemsList != null ? itemsList.size() : 0);
//
//            // 记录总耗时
//            long methodTotalTime = System.currentTimeMillis() - methodStartTime;
//            logger.info("[recognizeOrderFast] ========== 方法总耗时统计 ==========");
//            logger.info("[recognizeOrderFast] 总耗时: {} ms ({} 秒)", methodTotalTime, methodTotalTime / 1000.0);
//            logger.info("[recognizeOrderFast] ====================================");
//
//            return R.ok().put("ocrText", ocrTextContent)
//                    .put("items", itemsList != null ? itemsList : new ArrayList<>())
//                    .put("task", ocrTask)
//                    .put("taskId", ocrTaskId);
//        }
//        catch (Exception e) {
//            // 处理其他异常
//            logger.error("[OCR] 系统错误: {}", e.getMessage(), e);
//            return R.error("系统错误: " + e.getMessage());
//        }
//    }

    /**
     * 简单规则解析OCR文本为订单格式（不使用DeepSeek）
     * 支持格式：每行一个商品，商品名+数量+单位（如：西红柿5斤、土豆10袋）
     * 
     * @param ocrTextContent OCR识别的文本内容（按行组织）
     * @return 解析后的订单项列表，格式与DeepSeek返回一致
     */
    private List<Map<String, Object>> parseOrderTextByRule(String ocrTextContent) {
        List<Map<String, Object>> itemsList = new ArrayList<>();
        
        if (ocrTextContent == null || ocrTextContent.trim().isEmpty()) {
            logger.warn("[parseOrderTextByRule] OCR文本为空");
            return itemsList;
        }
        
        // 按行拆分
        String[] lines = ocrTextContent.split("\n");
        logger.info("[parseOrderTextByRule] 开始解析，共 {} 行", lines.length);
        
        // OCR识别错误的单位映射
        Map<String, String> unitCorrectionMap = new HashMap<>();
        unitCorrectionMap.put("化", "个"); // "5化" -> "5个"
        unitCorrectionMap.put("h", "个"); // 手写体识别
        unitCorrectionMap.put("量", "斤"); // "5量" -> "5斤"
        unitCorrectionMap.put("代", "袋"); // OCR识别错误："酒精拉1代" -> "酒精拉1袋"
        
        // 匹配模式：商品名+数量+单位（如：西红柿5斤、土豆10袋）
        // 支持一行中查找所有"数量+单位"模式，确保不丢失数量
        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)(斤|个|包|根|棵|条|盒|捆|袋|块|瓶|罐|桶|箱|件|把|化|h|量|两|代)");
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            
            // 移除OCR文本中的提示文字
            if (line.contains("以下是 OCR 识别到的订单文本") || line.contains("已按行组织")) {
                continue;
            }
            
            logger.debug("[parseOrderTextByRule] 处理行: {}", line);
            
            // 保存原始行文本（用于 rawName 和 originalText）
            String originalLine = line;
            
            // 查找所有"数量+单位"模式
            Matcher matcher = pattern.matcher(line);
            List<String> quantities = new ArrayList<>();
            List<String> units = new ArrayList<>();
            int lastMatchEnd = 0;
            
            while (matcher.find()) {
                String quantity = matcher.group(1);
                String unit = matcher.group(2);
                quantities.add(quantity);
                units.add(unit);
                lastMatchEnd = matcher.end();
            }
            
            // 提取商品名：从行首到第一个数量+单位之前的内容
            String rawGoodsName = "";
            if (!quantities.isEmpty()) {
                // 如果有数量+单位，从行首到第一个数量之前的内容作为商品名
                Matcher firstMatcher = pattern.matcher(line);
                if (firstMatcher.find()) {
                    rawGoodsName = line.substring(0, firstMatcher.start()).trim();
                }
            } else {
                // 如果没有数量+单位，整行作为商品名
                rawGoodsName = line;
                logger.debug("[parseOrderTextByRule] 未找到数量+单位，整行作为商品名: {}", line);
            }
            
            // 如果商品名为空，使用整行作为商品名
            if (rawGoodsName.isEmpty()) {
                rawGoodsName = line;
            }
            
            // 保存原始商品名（在清理和纠错之前）
            String originalGoodsName = rawGoodsName;
            
            // 清理商品名（去除多余空格）
            String goodsName = rawGoodsName.trim().replaceAll("\\s+", "");
            
            // 商品名称纠错：OCR识别错误纠正
            if (goodsName.contains("白带")) {
                goodsName = goodsName.replace("白带", "白菜");
                logger.debug("[parseOrderTextByRule] 商品名纠错: 白带 -> 白菜");
            }
            
            // 验证商品名不为空
            if (goodsName.isEmpty()) {
                logger.warn("[parseOrderTextByRule] 跳过：商品名为空，行: {}", line);
                continue;
            }
            
            // 处理数量+单位（如果没有找到，数量为空）
            String quantity = "";
            String unit = "";
            boolean quantityIsChange = false;
            boolean specIsChange = false;
            if (!quantities.isEmpty()) {
                // 如果一行有多个数量+单位，只取第一个作为主数量
                quantity = quantities.get(0);
                unit = units.get(0);
                
                // 处理OCR识别错误的单位
                String originalUnit = unit;
                if (unitCorrectionMap.containsKey(unit)) {
                    unit = unitCorrectionMap.get(unit);
                    specIsChange = true;
                    logger.debug("[parseOrderTextByRule] 单位纠错: {} -> {}", originalUnit, unit);
                }
                
                // 特殊处理："两"转换为"斤"，需要换算数量（1斤=10两）
                if ("两".equals(originalUnit)) {
                    try {
                        double qtyValue = Double.parseDouble(quantity);
                        double qtyInJin = qtyValue / 10.0; // 2两 = 0.2斤
                        // 保留1位小数，如果小数部分为0则不显示小数
                        if (qtyInJin == (int)qtyInJin) {
                            quantity = String.valueOf((int)qtyInJin);
                        } else {
                            quantity = String.format("%.1f", qtyInJin);
                        }
                        unit = "斤";
                        quantityIsChange = true;
                        specIsChange = true;
                        logger.info("[parseOrderTextByRule] 两转斤换算: {}两 -> {}斤", qtyValue, quantity);
                    } catch (NumberFormatException e) {
                        logger.warn("[parseOrderTextByRule] 数量格式错误，无法换算: {}", quantity);
                    }
                }
            } else {
                // 如果没有找到数量+单位，检查行尾是否是2位数或3位数（如"陈酷17"）
                // 匹配行尾的数字（2-3位）
                Pattern numberPattern = Pattern.compile("(\\d{2,3})$");
                Matcher numberMatcher = numberPattern.matcher(line);
                if (numberMatcher.find()) {
                    String numberStr = numberMatcher.group(1);
                    String newQuantity;
                    if (numberStr.length() == 2) {
                        // 2位数：取第1位作为数量，规格设为"个"
                        // 如"陈酷17" -> 商品名=陈酷, 数量=1, 单位=个
                        newQuantity = numberStr.substring(0, 1);
                        unit = "个";
                        // 重新提取商品名：从行首到数字之前
                        rawGoodsName = line.substring(0, numberMatcher.start()).trim();
                        originalGoodsName = rawGoodsName;
                        goodsName = rawGoodsName.trim().replaceAll("\\s+", "");
                        // 重新应用商品名纠错
                        if (goodsName.contains("白带")) {
                            goodsName = goodsName.replace("白带", "白菜");
                            logger.debug("[parseOrderTextByRule] 商品名纠错: 白带 -> 白菜");
                        }
                        quantity = newQuantity;
                        quantityIsChange = true;
                        specIsChange = true;
                        logger.info("[parseOrderTextByRule] 拆分2位数: 行={}, 商品名={}, 数量={}, 单位={}", 
                                line, goodsName, quantity, unit);
                    } else if (numberStr.length() == 3) {
                        // 3位数：取前2位作为数量，规格设为"斤"（参考 recognizeOrder 的逻辑）
                        // 如"陈酷123" -> 商品名=陈酷, 数量=12, 单位=斤
                        newQuantity = numberStr.substring(0, 2);
                        unit = "个";
                        // 重新提取商品名：从行首到数字之前
                        rawGoodsName = line.substring(0, numberMatcher.start()).trim();
                        originalGoodsName = rawGoodsName;
                        goodsName = rawGoodsName.trim().replaceAll("\\s+", "");
                        // 重新应用商品名纠错
                        if (goodsName.contains("白带")) {
                            goodsName = goodsName.replace("白带", "白菜");
                            logger.debug("[parseOrderTextByRule] 商品名纠错: 白带 -> 白菜");
                        }
                        quantity = newQuantity;
                        quantityIsChange = true;
                        specIsChange = true;
                        logger.info("[parseOrderTextByRule] 拆分3位数: 行={}, 商品名={}, 数量={}, 单位={}", 
                                line, goodsName, quantity, unit);
                    }
                } else {
                    logger.info("[parseOrderTextByRule] 未找到数量+单位，创建无数量订单: 商品名={}", goodsName);
                }
            }
            
            // 如果一行有多个数量+单位，记录到 note 中（避免丢失信息）
            String note = "";
            if (quantities.size() > 1) {
                StringBuilder noteBuilder = new StringBuilder();
                for (int i = 0; i < quantities.size(); i++) {
                    if (i > 0) {
                        noteBuilder.append(" ");
                    }
                    String q = quantities.get(i);
                    String u = units.get(i);
                    // 处理"两"转"斤"的换算
                    if ("两".equals(u)) {
                        try {
                            double qtyValue = Double.parseDouble(q);
                            double qtyInJin = qtyValue / 10.0;
                            if (qtyInJin == (int)qtyInJin) {
                                q = String.valueOf((int)qtyInJin);
                            } else {
                                q = String.format("%.1f", qtyInJin);
                            }
                            u = "斤";
                        } catch (NumberFormatException e) {
                            // 如果转换失败，保持原样
                        }
                    } else if (unitCorrectionMap.containsKey(u)) {
                        u = unitCorrectionMap.get(u);
                    }
                    noteBuilder.append(q).append(u);
                }
                note = noteBuilder.toString();
                logger.info("[parseOrderTextByRule] 检测到多个数量+单位，主数量: {}{}, 其他: {}", quantity, unit, note);
            }
            
            Map<String, Object> item = new HashMap<>();
            item.put("name", goodsName); // 纠错后的商品名
            item.put("rawName", originalGoodsName); // 原始商品名称（OCR原文，未清理）
            item.put("quantity", quantity);
            item.put("spec", unit);
            item.put("quantityIsChange", quantityIsChange);
            item.put("specIsChange", specIsChange);
            item.put("standardWeight", "");
            item.put("itemUnit", "");
            item.put("itemsPerCarton", "");
            item.put("cartonUnit", "");
            item.put("note", note);
            item.put("isNotice", "");
            item.put("originalText", originalLine);
            // 字段映射
            item.put("qty", quantity);
            item.put("unit", unit);
            item.put("remark", note);
            
            itemsList.add(item);
            logger.info("[parseOrderTextByRule] 解析成功: rawName={}, name={}, 数量={}, 单位={}, note={}", 
                    originalGoodsName, goodsName, quantity, unit, note);
        }
        
        logger.info("[parseOrderTextByRule] 解析完成，共解析到 {} 个商品", itemsList.size());
        return itemsList;
    }

    /**
     * 根据部门ID和状态查询OCR任务列表
     * 
     * @param request 请求参数：
     *                - departmentId: 部门ID（必填）
     *                - status: 任务状态（可选，0=处理中，1=已完成，2=部分完成）
     *                - page: 页码（可选，默认1）
     *                - limit: 每页数量（可选，默认10）
     * @return OCR任务列表
     */
    @RequestMapping(value = "/task/list", method = RequestMethod.POST)
    @ResponseBody
    public R getTaskList(@RequestBody Map<String, Object> request) {
        try {
            // 获取请求参数
            Object departmentIdObj = request.get("departmentId");
            Object statusObj = request.get("status");
            Object pageObj = request.get("page");
            Object limitObj = request.get("limit");
            
            if (departmentIdObj == null) {
                return R.error("部门ID不能为空");
            }
            
            Integer departmentId = null;
            if (departmentIdObj instanceof Number) {
                departmentId = ((Number) departmentIdObj).intValue();
            } else {
                departmentId = Integer.parseInt(departmentIdObj.toString());
            }
            
            Integer status = null;
            if (statusObj != null) {
                if (statusObj instanceof Number) {
                    status = ((Number) statusObj).intValue();
                } else {
                    status = Integer.parseInt(statusObj.toString());
                }
            }
            
            // 分页参数
            int page = 1;
            int limit = 10;
            if (pageObj != null) {
                if (pageObj instanceof Number) {
                    page = ((Number) pageObj).intValue();
                } else {
                    page = Integer.parseInt(pageObj.toString());
                }
            }
            if (limitObj != null) {
                if (limitObj instanceof Number) {
                    limit = ((Number) limitObj).intValue();
                } else {
                    limit = Integer.parseInt(limitObj.toString());
                }
            }
            
            // 构建查询条件
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("departmentId", departmentId);
            if (status != null) {
                queryMap.put("status", status);
            }
            queryMap.put("offset", (page - 1) * limit);
            queryMap.put("limit", limit);
            
            // 查询任务列表
            List<NxOcrTaskEntity> taskList = nxOcrTaskService.queryTasksByDepartmentAndStatus(queryMap);
            
            // 查询总数
            Map<String, Object> countMap = new HashMap<>();
            countMap.put("departmentId", departmentId);
            if (status != null) {
                countMap.put("status", status);
            }
            int total = nxOcrTaskService.queryTotalByDepartmentAndStatus(countMap);
            
            logger.info("[getTaskList] 查询成功，部门ID: {}, 状态: {}, 总数: {}, 当前页: {}", 
                    departmentId, status, total, page);
            
            // 返回结果
            return R.ok().put("list", taskList)
                    .put("total", total)
                    .put("page", page)
                    .put("limit", limit);
                    
        } catch (Exception e) {
            logger.error("[getTaskList] 查询OCR任务列表失败: {}", e.getMessage(), e);
            return R.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据任务ID分页查询任务状态和订单列表（合并了getTaskOrders和getTaskOrdersDetail）
     * 
     * @param taskId 任务ID（路径参数）
     * @param page 页码，从1开始，默认为1
     * @param limit 每页大小，默认为10
     * @param processStatus 是否处理状态为-2的订单（添加推荐商品），默认为true
     * @return 任务信息和分页订单列表，对状态为-2的订单添加推荐商品
     */
    @RequestMapping(value = "/getTaskOrders/{taskId}", method = RequestMethod.GET)
    @ResponseBody
    public R getTaskOrders(@PathVariable Integer taskId,
                          @RequestParam(required = false, defaultValue = "1") Integer page,
                          @RequestParam(required = false, defaultValue = "10") Integer limit,
                          @RequestParam(required = false, defaultValue = "true") Boolean processStatus) {
        // 记录方法开始时间
        long methodStartTime = System.currentTimeMillis();
        logger.info("[getTaskOrders] ========== 开始分页查询任务订单，任务ID: {}, page: {}, limit: {}, processStatus: {} ==========", 
                taskId, page, limit, processStatus);

        try {
            // 参数校验和默认值设置
            if (page == null || page < 1) {
                page = 1;
            }
            if (limit == null || limit < 1) {
                limit = 10;
            }
            if (processStatus == null) {
                processStatus = true;
            }

            // 步骤1：查询任务信息
            long step1StartTime = System.currentTimeMillis();
            logger.info("[getTaskOrders] [步骤1] 开始查询任务信息，任务ID: {}", taskId);
            NxOcrTaskEntity task = nxOcrTaskService.queryObject(taskId);
            long step1EndTime = System.currentTimeMillis();
            long step1Duration = step1EndTime - step1StartTime;
            logger.info("[getTaskOrders] [步骤1] 查询任务信息完成，耗时: {}ms", step1Duration);

            if (task == null) {
                logger.warn("[getTaskOrders] 任务不存在，任务ID: {}", taskId);
                return R.error("任务不存在");
            }

            // 步骤2：查询订单总数
            long step2StartTime = System.currentTimeMillis();
            logger.info("[getTaskOrders] [步骤2] 开始查询订单总数，任务ID: {}", taskId);
            Integer totalCount = nxDepartmentOrdersService.queryTotalByOcrTaskId(taskId);
            long step2EndTime = System.currentTimeMillis();
            long step2Duration = step2EndTime - step2StartTime;
            logger.info("[getTaskOrders] [步骤2] 查询订单总数完成，订单总数: {}, 耗时: {}ms", totalCount, step2Duration);

            // 步骤3：分页查询订单列表
            long step3StartTime = System.currentTimeMillis();
            logger.info("[getTaskOrders] [步骤3] 开始分页查询订单列表，任务ID: {}, 当前页: {}, 每页大小: {}", 
                    taskId, page, limit);
            
            // 计算分页参数
            int offset = (page - 1) * limit;
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("ocrTaskId", taskId);
            queryMap.put("offset", offset);
            queryMap.put("limit", limit);


            
            List<NxDepartmentOrdersEntity> pageOrders = nxDepartmentOrdersService.queryListByOcrTaskIdWithPage(queryMap);
            long step3EndTime = System.currentTimeMillis();
            long step3Duration = step3EndTime - step3StartTime;
            logger.info("[getTaskOrders] [步骤3] 分页查询订单列表完成，当前页订单数: {}, 耗时: {}ms", pageOrders.size(), step3Duration);

            // 步骤4：处理订单列表，为状态为-2的订单添加推荐商品
            long step4StartTime = System.currentTimeMillis();
            logger.info("[getTaskOrders] [步骤4] 开始处理订单列表，当前页订单数: {}", pageOrders.size());
            
            int statusMinus2Count = 0; // 统计需要添加推荐商品的订单数量
            long totalAddCommentsTime = 0; // 统计添加推荐商品的总耗时
            
            // 处理当前页的订单
            for (int i = 0; i < pageOrders.size(); i++) {
                NxDepartmentOrdersEntity order = pageOrders.get(i);
                long orderProcessStartTime = System.currentTimeMillis();

                // 如果订单状态为-2且需要处理，添加推荐商品
                if (processStatus && order.getNxDoStatus() != null && order.getNxDoStatus() == -2) {
                    statusMinus2Count++;
                    logger.info("[getTaskOrders] [步骤4] 订单[{}/{}] 状态为-2，开始添加推荐商品，订单ID: {}, 商品名称: {}",
                            i + 1, pageOrders.size(), order.getNxDepartmentOrdersId(), order.getNxDoGoodsName());

                    long addCommentsStartTime = System.currentTimeMillis();
                    // 添加推荐商品（不改变订单状态）
                    NxDepartmentOrdersEntity orderWithComments = nxDepartmentOrdersService.addCommentsGoodsForOrder(order);
                    long addCommentsEndTime = System.currentTimeMillis();
                    long addCommentsDuration = addCommentsEndTime - addCommentsStartTime;
                    totalAddCommentsTime += addCommentsDuration;

//                    logger.info("[getTaskOrders] [步骤4] 订单[{}/{}] 添加推荐商品完成，订单ID: {}, 耗时: {}ms, 推荐商品数量: {}",
//                            i + 1, pageOrders.size(), order.getNxDepartmentOrdersId(), addCommentsDuration,
//                            orderWithComments.getNxDistributerGoodsEntityList() != null ?
//                                    orderWithComments.getNxDistributerGoodsEntityList().size() : 0);

                    pageOrders.set(i, orderWithComments);
                }

                long orderProcessEndTime = System.currentTimeMillis();
                long orderProcessDuration = orderProcessEndTime - orderProcessStartTime;
                if (orderProcessDuration > 100) { // 只记录处理时间超过100ms的订单
                    logger.debug("[getTaskOrders] [步骤4] 订单[{}/{}] 处理完成，订单ID: {}, 总耗时: {}ms",
                            i + 1, pageOrders.size(), order.getNxDepartmentOrdersId(), orderProcessDuration);
                }
            }

            long step4EndTime = System.currentTimeMillis();
            long step4Duration = step4EndTime - step4StartTime;
            logger.info("[getTaskOrders] [步骤4] 处理订单列表完成，总耗时: {}ms, 状态为-2的订单数: {}, 添加推荐商品总耗时: {}ms, 平均每个订单耗时: {}ms",
                    step4Duration, statusMinus2Count, totalAddCommentsTime,
                    statusMinus2Count > 0 ? totalAddCommentsTime / statusMinus2Count : 0);

            // 步骤5：构建分页结果（转换为简洁DTO减少数据传输量）
            long step5StartTime = System.currentTimeMillis();
            logger.info("[getTaskOrders] [步骤5] 开始构建响应结果");
            List<PasteSearchGoodsResponseDTO> simpleOrderList = new ArrayList<>();
            for (NxDepartmentOrdersEntity order : pageOrders) {
                simpleOrderList.add(convertOrderToOcrTaskSimpleDTO(order));
            }
            // 打印前2个订单的字段内容（便于调试）
            for (int i = 0; i < Math.min(2, simpleOrderList.size()); i++) {
                PasteSearchGoodsResponseDTO dto = simpleOrderList.get(i);
                logger.info("[getTaskOrders] [步骤5] 订单[{}] 字段内容: {}", i + 1, dto);
            }
            PageUtils pageUtil = new PageUtils(simpleOrderList, totalCount, limit, page);
            
            R result = R.ok().put("task", task)
                    .put("page", pageUtil)
                    .put("imagePath", task.getNxOcrTaskImagePath())
                    .put("totalOrders", task.getNxOcrTaskTotalOrders())
                    .put("completedOrders", task.getNxOcrTaskCompletedOrders())
                    .put("pendingOrders", task.getNxOcrTaskPendingOrders());
            long step5EndTime = System.currentTimeMillis();
            long step5Duration = step5EndTime - step5StartTime;
            logger.info("[getTaskOrders] [步骤5] 构建响应结果完成，耗时: {}ms", step5Duration);

            // 记录方法总耗时
            long methodEndTime = System.currentTimeMillis();
            long methodTotalDuration = methodEndTime - methodStartTime;
            logger.info("[getTaskOrders] ========== 分页查询任务订单完成，任务ID: {}, 总耗时: {}ms ==========", taskId, methodTotalDuration);
            logger.info("[getTaskOrders] 性能统计 - 步骤1(查询任务): {}ms, 步骤2(查询总数): {}ms, 步骤3(分页查询): {}ms, 步骤4(处理订单): {}ms, 步骤5(构建响应): {}ms",
                    step1Duration, step2Duration, step3Duration, step4Duration, step5Duration);

            return result;

        } catch (Exception e) {
            long methodEndTime = System.currentTimeMillis();
            long methodTotalDuration = methodEndTime - methodStartTime;
            logger.error("[getTaskOrders] ========== 分页查询任务订单失败，任务ID: {}, 总耗时: {}ms ==========", taskId, methodTotalDuration, e);
            return R.error("查询失败: " + e.getMessage());
        }
    }
    /**
     * 根据任务ID查询任务状态和订单列表（已废弃，功能已合并到getTaskOrders分页接口）
     * 请使用 /getTaskOrders/{taskId}?page=1&limit=全部数量 来获取所有订单
     *
     * @param taskId 任务ID（路径参数）
     * @return 任务信息和订单列表，对状态为-2且有商品ID的订单添加推荐商品
     * @deprecated 请使用 getTaskOrders 分页接口替代
     */
    @Deprecated
    @RequestMapping(value = "/getTaskOrdersDetail/{taskId}", method = RequestMethod.GET)
    @ResponseBody
    public R getTaskOrdersDetail(@PathVariable Integer taskId) {
        // 记录方法开始时间
        long methodStartTime = System.currentTimeMillis();
        logger.info("[getTaskOrders] ========== 开始查询任务订单，任务ID: {} ==========", taskId);

        try {
            // 步骤1：查询任务信息
            long step1StartTime = System.currentTimeMillis();
            logger.info("[getTaskOrders] [步骤1] 开始查询任务信息，任务ID: {}", taskId);
            NxOcrTaskEntity task = nxOcrTaskService.queryObject(taskId);
            long step1EndTime = System.currentTimeMillis();
            long step1Duration = step1EndTime - step1StartTime;
            logger.info("[getTaskOrders] [步骤1] 查询任务信息完成，耗时: {}ms", step1Duration);

            if (task == null) {
                logger.warn("[getTaskOrders] 任务不存在，任务ID: {}", taskId);
                return R.error("任务不存在");
            }

            // 步骤2：查询订单列表
            long step2StartTime = System.currentTimeMillis();
            logger.info("[getTaskOrders] [步骤2] 开始查询订单列表，任务ID: {}", taskId);
            List<NxDepartmentOrdersEntity> orderList = nxDepartmentOrdersService.queryListByOcrTaskId(taskId);
            long step2EndTime = System.currentTimeMillis();
            long step2Duration = step2EndTime - step2StartTime;
            logger.info("[getTaskOrders] [步骤2] 查询订单列表完成，订单数量: {}, 耗时: {}ms", orderList.size(), step2Duration);

            // 步骤3：处理订单列表，为状态为-2的订单添加推荐商品
            long step3StartTime = System.currentTimeMillis();
            logger.info("[getTaskOrders] [步骤3] 开始处理订单列表，订单总数: {}", orderList.size());
            List<NxDepartmentOrdersEntity> responseList = new ArrayList<>();

            int statusMinus2Count = 0; // 统计需要添加推荐商品的订单数量
            long totalAddCommentsTime = 0; // 统计添加推荐商品的总耗时

            for (int i = 0; i < orderList.size(); i++) {
                NxDepartmentOrdersEntity order = orderList.get(i);
                long orderProcessStartTime = System.currentTimeMillis();

                // 如果订单状态为-2，添加推荐商品
                if (order.getNxDoStatus() != null && order.getNxDoStatus() == -2) {
                    statusMinus2Count++;
                    logger.info("[getTaskOrders] [步骤3] 订单[{}/{}] 状态为-2，开始添加推荐商品，订单ID: {}, 商品名称: {}",
                            i + 1, orderList.size(), order.getNxDepartmentOrdersId(), order.getNxDoGoodsName());

                    long addCommentsStartTime = System.currentTimeMillis();
                    // 添加推荐商品（不改变订单状态）
                    NxDepartmentOrdersEntity orderWithComments = nxDepartmentOrdersService.addCommentsGoodsForOrder(order);
                    long addCommentsEndTime = System.currentTimeMillis();
                    long addCommentsDuration = addCommentsEndTime - addCommentsStartTime;
                    totalAddCommentsTime += addCommentsDuration;

                    logger.info("[getTaskOrders] [步骤3] 订单[{}/{}] 添加推荐商品完成，订单ID: {}, 耗时: {}ms, 推荐商品数量: {}",
                            i + 1, orderList.size(), order.getNxDepartmentOrdersId(), addCommentsDuration,
                            orderWithComments.getNxDistributerGoodsEntityList() != null ?
                                    orderWithComments.getNxDistributerGoodsEntityList().size() : 0);

                    responseList.add(orderWithComments);
                } else {
                    responseList.add(order);
                }

                long orderProcessEndTime = System.currentTimeMillis();
                long orderProcessDuration = orderProcessEndTime - orderProcessStartTime;
                if (orderProcessDuration > 100) { // 只记录处理时间超过100ms的订单
                    logger.debug("[getTaskOrders] [步骤3] 订单[{}/{}] 处理完成，订单ID: {}, 总耗时: {}ms",
                            i + 1, orderList.size(), order.getNxDepartmentOrdersId(), orderProcessDuration);
                }
            }

            long step3EndTime = System.currentTimeMillis();
            long step3Duration = step3EndTime - step3StartTime;
            logger.info("[getTaskOrders] [步骤3] 处理订单列表完成，总耗时: {}ms, 状态为-2的订单数: {}, 添加推荐商品总耗时: {}ms, 平均每个订单耗时: {}ms",
                    step3Duration, statusMinus2Count, totalAddCommentsTime,
                    statusMinus2Count > 0 ? totalAddCommentsTime / statusMinus2Count : 0);

            // 步骤4：构建响应结果
            long step4StartTime = System.currentTimeMillis();
            logger.info("[getTaskOrders] [步骤4] 开始构建响应结果");
            R result = R.ok().put("task", task)
                    .put("orders", responseList)
                    .put("imagePath", task.getNxOcrTaskImagePath())
                    .put("totalOrders", task.getNxOcrTaskTotalOrders())
                    .put("completedOrders", task.getNxOcrTaskCompletedOrders())
                    .put("pendingOrders", task.getNxOcrTaskPendingOrders());
            long step4EndTime = System.currentTimeMillis();
            long step4Duration = step4EndTime - step4StartTime;
            logger.info("[getTaskOrders] [步骤4] 构建响应结果完成，耗时: {}ms", step4Duration);

            // 记录方法总耗时
            long methodEndTime = System.currentTimeMillis();
            long methodTotalDuration = methodEndTime - methodStartTime;
            logger.info("[getTaskOrders] ========== 查询任务订单完成，任务ID: {}, 总耗时: {}ms ==========", taskId, methodTotalDuration);
            logger.info("[getTaskOrders] 性能统计 - 步骤1(查询任务): {}ms, 步骤2(查询订单列表): {}ms, 步骤3(处理订单): {}ms, 步骤4(构建响应): {}ms",
                    step1Duration, step2Duration, step3Duration, step4Duration);

            return result;

        } catch (Exception e) {
            long methodEndTime = System.currentTimeMillis();
            long methodTotalDuration = methodEndTime - methodStartTime;
            logger.error("[getTaskOrders] ========== 查询任务订单失败，任务ID: {}, 总耗时: {}ms ==========", taskId, methodTotalDuration, e);
            return R.error("查询失败: " + e.getMessage());
        }
    }

//    @RequestMapping(value = "depGetTaskList/{depId}")
//    @ResponseBody
//    public R depGetTaskList(@PathVariable Integer depId) {
//        System.out.println("talsllsls");
//
//
//            // 构建查询条件
//            Map<String, Object> queryMap = new HashMap<>();
//            queryMap.put("departmentId", depId);
////            queryMap.put("status", 2);
//
//            // 查询任务列表
//            List<NxOcrTaskEntity> taskList = nxOcrTaskService.queryTasksByDepartmentAndStatus(queryMap);
//            // 返回结果
//        System.out.println("dkkdkkdkd" + taskList);
//           return  R.ok();
//
//    }
    
    @RequestMapping(value = "/depGetTaskList", method = RequestMethod.POST)
    @ResponseBody
    public R depGetTaskList(Integer depId, Integer type) {
        Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("type", type);
            queryMap.put("departmentId", depId);
            queryMap.put("xiaoyuStatus", 3);
//            // 查询任务列表
        System.out.println("depgelis" + queryMap);
            List<NxOcrTaskEntity> taskList = nxOcrTaskService.queryTasksByDepartmentAndStatus(queryMap);
        return R.ok().put("data", taskList);
    }


    @RequestMapping(value = "/depFatherGetTaskList/{depFatherId}")
    @ResponseBody
    public R depFatherGetTaskList(@PathVariable Integer depFatherId) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("departmentFatherId", depFatherId);
        queryMap.put("xiaoyuStatus", 3);
        List<NxOcrTaskEntity> nxOcrTaskEntities = nxOcrTaskService.queryTasksByDepartmentAndStatus(queryMap);
        return R.ok().put("data",  nxOcrTaskEntities);
    }

    @RequestMapping(value = "/depFatherGetTaskListJustCount/{depFatherId}")
    @ResponseBody
    public R depFatherGetTaskListJustCount(@PathVariable Integer depFatherId) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("departmentFatherId", depFatherId);
        queryMap.put("xiaoyuStatus", 3);
        System.out.println("gaiwiixixiaoy3333");
        List<NxOcrTaskEntity> nxOcrTaskEntities = nxOcrTaskService.queryTasksByDepartmentAndStatus(queryMap);
//            // 查询任务列表

//        queryMap.put("type", 1);
//        int count =  nxOcrTaskService.queryTotalByDepartmentAndStatus(queryMap);
//        int count3 =  nxOcrTaskService.queryTotalByDepartmentAndStatus(queryMap);
//        Map<String, Object> map = new HashMap<>();
//        map.put("imageTaskCount", count);
//        map.put("pastTaskCount", count3 );
        return R.ok().put("data",  nxOcrTaskEntities);
    }



    @RequestMapping(value = "/finishTask/{taskId}")
    @ResponseBody
    public R finishTask(@PathVariable Integer taskId) {

        NxOcrTaskEntity taskEntity = nxOcrTaskService.queryObject(taskId);
        taskEntity.setNxOcrTaskStatus(3);
        nxOcrTaskService.update(taskEntity);
        return R.ok();
    }



    @RequestMapping(value = "/disGetTaskList/{disId}")
    @ResponseBody
    public R disGetTaskList(@PathVariable Integer disId) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("disId", disId);
        queryMap.put("xiaoyuStatus", 3);
//            // 查询任务列表
        List<NxOcrTaskEntity> taskList = nxOcrTaskService.queryTasksByDepartmentAndStatus(queryMap);
        return R.ok().put("data", taskList);
    }

    /**
     * 根据分销商ID查询有OCR任务的部门父列表
     * 
     * @param disId 分销商ID（路径参数）
     * @return 部门父列表（去重，只返回有任务的部门父）
     */
    @RequestMapping(value = "/disGetTaskDepFatherList/{disId}", method = RequestMethod.GET)
    @ResponseBody
    public R disGetTaskDepFatherList(@PathVariable Integer disId) {
        try {
            logger.info("[disGetTaskDepFatherList] 开始查询有任务的部门父列表，分销商ID: {}", disId);
            
            if (disId == null) {
                logger.warn("[disGetTaskDepFatherList] 分销商ID为空");
                return R.error("分销商ID不能为空");
            }
            
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("disId", disId);
            queryMap.put("xiaoyuStatus", 3);  // 查询状态小于2的任务（0=处理中，1=部分完成）
            System.out.println("maumapap" + queryMap);
            // 查询有任务的部门父列表（去重）
            List<NxDepartmentEntity> departmentEntityList = nxOcrTaskService.queryTasksDepartmentByDisId(queryMap);
            
            logger.info("[disGetTaskDepFatherList] 查询完成aaa，分销商ID: {}, 部门父数量: {}", disId, departmentEntityList.size());
            
            return R.ok().put("data", departmentEntityList);
            
        } catch (Exception e) {
            logger.error("[disGetTaskDepFatherList] 查询失败，分销商ID: {}", disId, e);
            return R.error("查询失败: " + e.getMessage());
        }
    }



    /**
     * 订单修正接口
     * 接收已解析的 orderItems 和用户修正指令，调用 DeepSeek 进行修正，然后重新查询商品并更新订单
     * 
     * @param request 请求参数：
     *                - orderItems: 已解析的订单数组（必填，包含订单ID）
     *                - userInstructions: 用户修正指令文本（必填）
     * @return 修正并更新后的订单列表
     */
    @RequestMapping(value = "/correction", method = RequestMethod.POST)
    @ResponseBody
    public R correction(@RequestBody Map<String, Object> request) {
        try {
            // 获取请求参数
            Object orderItemsObj = request.get("orderItems");
            Object userInstructionsObj = request.get("userInstructions");
            
            if (orderItemsObj == null) {
                logger.warn("[correction] orderItems 为空");
                return R.error("orderItems 不能为空");
            }
            
            if (userInstructionsObj == null || userInstructionsObj.toString().trim().isEmpty()) {
                logger.warn("[correction] userInstructions 为空");
                return R.error("userInstructions 不能为空");
            }
            
            String userInstructions = userInstructionsObj.toString().trim();
            
            // 解析 orderItems
            List<Map<String, Object>> orderItemsList = new ArrayList<>();
            if (orderItemsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> items = (List<Object>) orderItemsObj;
                for (Object item : items) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> itemMap = (Map<String, Object>) item;
                        orderItemsList.add(itemMap);
                    }
                }
            } else {
                return R.error("orderItems 格式错误，必须是数组");
            }
            
            if (orderItemsList.isEmpty()) {
                logger.warn("[correction] orderItems 数组为空");
                return R.error("orderItems 数组不能为空");
            }
            
            logger.info("[correction] 接收到修正请求，订单数量: {}, 用户指令: {}", orderItemsList.size(), userInstructions);
            
            // 从第一个订单项中提取 depId, disId, depFatherId, userId
            Map<String, Object> firstItem = orderItemsList.get(0);
            Integer depId = null;
            Integer disId = null;
            Integer depFatherId = null;
            Integer userId = null;
            
            // 尝试从订单项中获取这些字段（如果 orderItems 中包含的话）
            // 或者从订单ID查询订单获取这些信息
            Object nxDepartmentOrdersIdObj = firstItem.get("nxDepartmentOrdersId");
            if (nxDepartmentOrdersIdObj != null) {
                Integer orderId = null;
                if (nxDepartmentOrdersIdObj instanceof Number) {
                    orderId = ((Number) nxDepartmentOrdersIdObj).intValue();
                } else {
                    try {
                        orderId = Integer.parseInt(nxDepartmentOrdersIdObj.toString());
                    } catch (NumberFormatException e) {
                        logger.warn("[correction] 订单ID格式错误: {}", nxDepartmentOrdersIdObj);
                    }
                }
                
                if (orderId != null && orderId > 0) {
                    // 查询订单获取部门ID等信息
                    NxDepartmentOrdersEntity existingOrder = nxDepartmentOrdersService.queryObject(orderId);
                    if (existingOrder != null) {
                        depId = existingOrder.getNxDoDepartmentId();
                        disId = existingOrder.getNxDoDistributerId();
                        depFatherId = existingOrder.getNxDoDepartmentFatherId();
                        userId = existingOrder.getNxDoOrderUserId();
                        logger.info("[correction] 从订单查询到信息: depId={}, disId={}, depFatherId={}, userId={}", 
                                depId, disId, depFatherId, userId);
                    }
                }
            }
            
            // 如果无法从订单获取，尝试从请求参数中获取
            if (depId == null) {
                Object depIdObj = request.get("depId");
                if (depIdObj != null) {
                    if (depIdObj instanceof Number) {
                        depId = ((Number) depIdObj).intValue();
                    } else {
                        depId = Integer.parseInt(depIdObj.toString());
                    }
                }
            }
            
            if (disId == null) {
                Object disIdObj = request.get("disId");
                if (disIdObj != null) {
                    if (disIdObj instanceof Number) {
                        disId = ((Number) disIdObj).intValue();
                    } else {
                        disId = Integer.parseInt(disIdObj.toString());
                    }
                }
            }
            
            if (depId == null || disId == null) {
                logger.error("[correction] 无法获取 depId 或 disId");
                return R.error("无法获取部门ID或分销商ID，请确保订单ID有效或在请求中提供这些参数");
            }
            
            // 获取输入类型（image/excel/paste），默认为 image
            Object inputTypeObj = request.get("inputType");
            String inputType = "image"; // 默认值
            if (inputTypeObj != null) {
                inputType = inputTypeObj.toString().trim().toLowerCase();
                if (!"image".equals(inputType) && !"excel".equals(inputType) && !"paste".equals(inputType)) {
                    logger.warn("[correction] 无效的 inputType: {}，使用默认值 image", inputTypeObj);
                    inputType = "image";
                }
            }
            logger.info("[correction] 输入类型: {}", inputType);
            
            // 将用户指令更新到部门的 OCR prompt 字段中（根据输入类型更新对应字段）
            try {
                NxDepartmentEntity department = nxDepartmentService.queryObject(depId);
                if (department != null) {
                    String trimmedInstructions = userInstructions.trim();
                    
                    // 根据输入类型更新对应的 prompt 字段
                    if ("excel".equals(inputType)) {
                        department.setNxDepartmentOcrPromptExcel(trimmedInstructions);
                        logger.info("[correction] 用户指令已更新到部门 Excel prompt，部门ID: {}, prompt 长度: {}", 
                                depId, trimmedInstructions.length());
                    } else if ("paste".equals(inputType)) {
                        department.setNxDepartmentOcrPromptPaste(trimmedInstructions);
                        logger.info("[correction] 用户指令已更新到部门粘贴 prompt，部门ID: {}, prompt 长度: {}", 
                                depId, trimmedInstructions.length());
                    } else {
                        // 默认为 image
                        department.setNxDepartmentOcrPromptImage(trimmedInstructions);
                        logger.info("[correction] 用户指令已更新到部门图片 prompt，部门ID: {}, prompt 长度: {}", 
                                depId, trimmedInstructions.length());
                    }
                    
                    nxDepartmentService.update(department);
                } else {
                    logger.warn("[correction] 未找到部门信息，部门ID: {}", depId);
                }
            } catch (Exception e) {
                logger.error("[correction] 更新用户指令到部门 prompt 失败: {}", e.getMessage(), e);
                // 不中断流程，继续执行修正
            }
            
            // 调用 DeepSeek 进行修正
            logger.info("[correction] 开始调用 DeepSeek 进行修正...");
            String correctedResult;
            try {
                correctedResult = callDeepSeekAPIForCorrection(orderItemsList, userInstructions);
                logger.info("[correction] DeepSeek 修正完成");
            } catch (Exception e) {
                logger.error("[correction] DeepSeek API 调用失败: {}", e.getMessage(), e);
                return R.error("修正失败：DeepSeek API 调用失败 - " + e.getMessage());
            }
            
            // 解析 DeepSeek 返回的修正结果
            List<Map<String, Object>> correctedItemsList = new ArrayList<>();
            try {
                String cleanedResult = correctedResult.trim();
                logger.debug("[correction] DeepSeek 返回的修正结果: {}", cleanedResult);
                
                JSONArray jsonArray = null;
                
                // 尝试提取 JSON 数组部分（如果有其他文本）
                int jsonStart = cleanedResult.indexOf('[');
                int jsonEnd = cleanedResult.lastIndexOf(']');
                
                if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    cleanedResult = cleanedResult.substring(jsonStart, jsonEnd + 1);
                    logger.debug("[correction] 提取的 JSON 数组部分: {}", cleanedResult);
                }
                
                if (cleanedResult.startsWith("[")) {
                    jsonArray = new JSONArray(cleanedResult);
                    logger.info("[correction] 解析为 JSON 数组，数量: {}", jsonArray.length());
                } else {
                    JSONObject jsonObj = new JSONObject(cleanedResult);
                    if (jsonObj.has("orderItems")) {
                        jsonArray = jsonObj.getJSONArray("orderItems");
                        logger.info("[correction] 检测到 orderItems 字段，商品数量: {}", jsonArray.length());
                    } else if (jsonObj.has("items")) {
                        jsonArray = jsonObj.getJSONArray("items");
                        logger.info("[correction] 检测到 items 字段，商品数量: {}", jsonArray.length());
                    }
                }
                
                if (jsonArray == null) {
                    logger.error("[correction] DeepSeek 返回结果格式错误，无法解析为 JSON 数组或对象");
                    logger.error("[correction] 原始内容: {}", correctedResult);
                    return R.error("修正失败：DeepSeek 返回结果格式错误，无法解析为有效的 JSON");
                }
                
                // 输出前3个订单项的原始 JSON，用于调试
                for (int i = 0; i < Math.min(3, jsonArray.length()); i++) {
                    JSONObject item = jsonArray.getJSONObject(i);
                    logger.info("[correction] 第 {} 个订单项原始 JSON: {}", i, item.toString());
                }
                
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject item = jsonArray.getJSONObject(i);
                    Map<String, Object> itemMap = new HashMap<>();
                    
                    // 先尝试获取所有键，看看实际有哪些字段
                    if (i == 0) {
                        @SuppressWarnings("unchecked")
                        Iterator<String> keys = item.keys();
                        List<String> keyList = new ArrayList<>();
                        while (keys.hasNext()) {
                            keyList.add(keys.next());
                        }
                        logger.info("[correction] 第 {} 个订单项的所有字段名: {}", i, keyList);
                    }
                    
                    String name = item.optString("name", "");
                    String rawName = item.optString("rawName", "");
                    String quantity = item.optString("quantity", "");
                    String spec = item.optString("spec", "");
                    String standardWeight = item.optString("standardWeight", "");
                    String itemUnit = item.optString("itemUnit", "");
                    String itemsPerCarton = item.optString("itemsPerCarton", "");
                    String cartonUnit = item.optString("cartonUnit", "");
                    String note = item.optString("note", "");
                    String isNotice = item.optString("isNotice", "");
                    
                    logger.debug("[correction] 第 {} 个订单项提取结果: name={}, rawName={}, quantity={}, spec={}, standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}, note={}, isNotice={}", 
                            i, name, rawName, quantity, spec, standardWeight, itemUnit, itemsPerCarton, cartonUnit, note, isNotice);
                    
                    itemMap.put("name", name);
                    itemMap.put("rawName", rawName);
                    itemMap.put("quantity", quantity);
                    itemMap.put("spec", spec);
                    itemMap.put("standardWeight", standardWeight);
                    itemMap.put("itemUnit", itemUnit);
                    itemMap.put("itemsPerCarton", itemsPerCarton);
                    itemMap.put("cartonUnit", cartonUnit);
                    itemMap.put("note", note);
                    itemMap.put("isNotice", isNotice);
                    
                    // 保留原始订单ID
                    if (i < orderItemsList.size()) {
                        Object originalOrderId = orderItemsList.get(i).get("nxDepartmentOrdersId");
                        if (originalOrderId != null) {
                            itemMap.put("nxDepartmentOrdersId", originalOrderId);
                        }
                    }
                    correctedItemsList.add(itemMap);
                }
                
                logger.info("[correction] 修正结果解析成功，修正后订单数量: {}", correctedItemsList.size());
            } catch (Exception e) {
                logger.error("[correction] 修正结果解析失败: {}", e.getMessage(), e);
                return R.error("修正失败：修正结果解析失败 - " + e.getMessage());
            }
            
            // 根据修正后的数据重新查询商品并更新订单
            Map<Integer, NxDepartmentOrdersEntity> responseMap = new HashMap<>();
            
            for (int i = 0; i < correctedItemsList.size(); i++) {
                Map<String, Object> item = correctedItemsList.get(i);
                
                // 提取字段
                String goodsName = item.get("name") != null ? item.get("name").toString().trim() : "";
                String rawName = item.get("rawName") != null && !item.get("rawName").toString().trim().isEmpty()
                        ? item.get("rawName").toString().trim() : goodsName;
                String quantity = item.get("quantity") != null ? item.get("quantity").toString().trim() : "";
                String spec = item.get("spec") != null ? item.get("spec").toString().trim() : "";
                
                // 提取包装结构字段，如果 DeepSeek 返回为空，则使用原始订单中的值
                String standardWeight = item.get("standardWeight") != null ? item.get("standardWeight").toString().trim() : "";
                String itemUnit = item.get("itemUnit") != null ? item.get("itemUnit").toString().trim() : "";
                String itemsPerCarton = item.get("itemsPerCarton") != null ? item.get("itemsPerCarton").toString().trim() : "";
                String cartonUnit = item.get("cartonUnit") != null ? item.get("cartonUnit").toString().trim() : "";
                
                // 如果 DeepSeek 返回的包装结构字段为空，尝试从原始订单项中获取
                if (i < orderItemsList.size()) {
                    Map<String, Object> originalItem = orderItemsList.get(i);
                    if (standardWeight.isEmpty()) {
                        standardWeight = getFieldValue(originalItem, "standardWeight", "");
                    }
                    if (itemUnit.isEmpty()) {
                        itemUnit = getFieldValue(originalItem, "itemUnit", "");
                    }
                    if (itemsPerCarton.isEmpty()) {
                        itemsPerCarton = getFieldValue(originalItem, "itemsPerCarton", "");
                    }
                    if (cartonUnit.isEmpty()) {
                        cartonUnit = getFieldValue(originalItem, "cartonUnit", "");
                    }
                }
                
                String note = item.get("note") != null ? item.get("note").toString().trim() : "";
                String isNotice = item.get("isNotice") != null ? item.get("isNotice").toString().trim() : "";
                
                logger.info("[correction] 第 {} 个订单项提取结果: name={}, rawName={}, quantity={}, spec={}, standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}, note={}, isNotice={}", 
                        i, goodsName, rawName, quantity, spec, standardWeight, itemUnit, itemsPerCarton, cartonUnit, note, isNotice);
                
                // 获取订单ID
                Object orderIdObj = item.get("nxDepartmentOrdersId");
                Integer orderId = null;
                if (orderIdObj != null) {
                    if (orderIdObj instanceof Number) {
                        orderId = ((Number) orderIdObj).intValue();
                    } else {
                        try {
                            orderId = Integer.parseInt(orderIdObj.toString());
                        } catch (NumberFormatException e) {
                            logger.warn("[correction] 订单ID格式错误: {}", orderIdObj);
                        }
                    }
                }
                
                if (orderId == null || orderId <= 0) {
                    logger.warn("[correction] 订单ID无效，跳过: orderId={}", orderId);
                    continue;
                }
                
                // 查询现有订单
                NxDepartmentOrdersEntity existingOrder = nxDepartmentOrdersService.queryObject(orderId);
                if (existingOrder == null) {
                    logger.warn("[correction] 订单不存在，跳过: orderId={}", orderId);
                    continue;
                }
                
                logger.info("[correction] 更新订单 {}: 原商品名={}, 原数量={}, 原规格={}, 新商品名={}, 新数量={}, 新规格={}", 
                        orderId, existingOrder.getNxDoGoodsName(), existingOrder.getNxDoQuantity(), existingOrder.getNxDoStandard(),
                        goodsName, quantity, spec);
                
                // 更新订单基本信息
                existingOrder.setNxDoGoodsName(goodsName);
                existingOrder.setNxDoQuantity(quantity);
                existingOrder.setNxDoStandard(spec);
                existingOrder.setNxDoRemark(note);
                existingOrder.setOrcNotice(isNotice);
                
                // 复用商品查询逻辑（与 recognizeOrder 相同）
                // 1级优先匹配：部门商品历史记录
                Map<String, Object> depGoodsMap = new HashMap<>();
                depGoodsMap.put("disId", disId);
                depGoodsMap.put("depId", depId);
                depGoodsMap.put("name", rawName);
                depGoodsMap.put("standard", spec);
                List<NxDepartmentDisGoodsEntity> departmentDisGoodsList = nxDepartmentDisGoodsService.queryDepartmentGoods(depGoodsMap);
                
                if (departmentDisGoodsList.size() == 1) {
                    NxDepartmentDisGoodsEntity departmentDisGoodsEntity = departmentDisGoodsList.get(0);
                    NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(
                            departmentDisGoodsEntity.getNxDdgDisGoodsId());
                    
                    existingOrder.setNxDoDisGoodsId(distributerGoodsEntity.getNxDistributerGoodsId());
                    nxDepartmentOrdersService.update(existingOrder);
                    
                    // 重新查询订单并添加推荐商品
                    NxDepartmentOrdersEntity orderWithRecommendations = nxDepartmentOrdersService.queryObject(existingOrder.getNxDepartmentOrdersId());
                    if (orderWithRecommendations != null) {
                        orderWithRecommendations = nxDepartmentOrdersService.addCommentsGoodsForOrder(orderWithRecommendations);
                    } else {
                        orderWithRecommendations = existingOrder;
                    }
                    
                    // 设置包装结构字段到订单实体
                    orderWithRecommendations.setStandardWeight(standardWeight != null ? standardWeight : "");
                    orderWithRecommendations.setItemUnit(itemUnit != null ? itemUnit : "");
                    orderWithRecommendations.setItemsPerCarton(itemsPerCarton != null ? itemsPerCarton : "");
                    orderWithRecommendations.setCartonUnit(cartonUnit != null ? cartonUnit : "");
                    
                    // 直接返回订单实体（订单实体已包含所有DTO字段）
                    responseMap.put(i, orderWithRecommendations);
                    continue;
                }
                
                // 2级优先匹配：商品库
                Map<String, Object> mapZero = new HashMap<>();
                mapZero.put("disId", disId);
                mapZero.put("searchStr", rawName);
                mapZero.put("standard", spec);
                List<NxDistributerGoodsEntity> distributerGoodsEntitiesZero = nxDistributerGoodsService.queryDisGoodsByName(mapZero);
                
                if (distributerGoodsEntitiesZero.size() == 1) {
                    NxDistributerGoodsEntity distributerGoodsEntity = distributerGoodsEntitiesZero.get(0);
                    existingOrder.setNxDoDisGoodsId(distributerGoodsEntity.getNxDistributerGoodsId());
                    nxDepartmentOrdersService.update(existingOrder);
                    
                    // 重新查询订单并添加推荐商品
                    NxDepartmentOrdersEntity orderWithRecommendations = nxDepartmentOrdersService.queryObject(existingOrder.getNxDepartmentOrdersId());
                    if (orderWithRecommendations != null) {
                        orderWithRecommendations = nxDepartmentOrdersService.addCommentsGoodsForOrder(orderWithRecommendations);
                    } else {
                        orderWithRecommendations = existingOrder;
                    }
                    
                    // 设置包装结构字段到订单实体
                    orderWithRecommendations.setStandardWeight(standardWeight != null ? standardWeight : "");
                    orderWithRecommendations.setItemUnit(itemUnit != null ? itemUnit : "");
                    orderWithRecommendations.setItemsPerCarton(itemsPerCarton != null ? itemsPerCarton : "");
                    orderWithRecommendations.setCartonUnit(cartonUnit != null ? cartonUnit : "");
                    
                    // 直接返回订单实体（订单实体已包含所有DTO字段）
                    responseMap.put(i, orderWithRecommendations);
                    continue;
                }
                
                // 3级优先匹配：商品库别名
                Map<String, Object> mapA = new HashMap<>();
                mapA.put("disId", disId);
                mapA.put("alias", goodsName);
                mapA.put("standard", spec);
                List<NxDistributerGoodsEntity> distributerGoodsEntitiesAlias = nxDistributerGoodsService.queryDisGoodsByAlias(mapA);
                
                if (distributerGoodsEntitiesAlias.size() == 1) {
                    NxDistributerGoodsEntity distributerGoodsEntity = distributerGoodsEntitiesAlias.get(0);
                    existingOrder.setNxDoDisGoodsId(distributerGoodsEntity.getNxDistributerGoodsId());
                    nxDepartmentOrdersService.update(existingOrder);
                    
                    // 重新查询订单并添加推荐商品
                    NxDepartmentOrdersEntity orderWithRecommendations = nxDepartmentOrdersService.queryObject(existingOrder.getNxDepartmentOrdersId());
                    if (orderWithRecommendations != null) {
                        orderWithRecommendations = nxDepartmentOrdersService.addCommentsGoodsForOrder(orderWithRecommendations);
                    } else {
                        orderWithRecommendations = existingOrder;
                    }
                    
                    // 设置包装结构字段到订单实体
                    orderWithRecommendations.setStandardWeight(standardWeight != null ? standardWeight : "");
                    orderWithRecommendations.setItemUnit(itemUnit != null ? itemUnit : "");
                    orderWithRecommendations.setItemsPerCarton(itemsPerCarton != null ? itemsPerCarton : "");
                    orderWithRecommendations.setCartonUnit(cartonUnit != null ? cartonUnit : "");
                    
                    // 直接返回订单实体（订单实体已包含所有DTO字段）
                    responseMap.put(i, orderWithRecommendations);
                    continue;
                }
                
                // 4级优先匹配：训练数据
                Map<String, Object> matchParams = new HashMap<>();
                matchParams.put("depId", depId);
                matchParams.put("goodsName", rawName);
                matchParams.put("disGoodsId", 1);
                NxOrderOcrTrainingDataEntity matchedTrainingData = nxOrderOcrTrainingDataService.queryByMatchFields(matchParams);
                
                if (matchedTrainingData != null && matchedTrainingData.getNxOtdDisGoodsId() != null) {
                    NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(
                            matchedTrainingData.getNxOtdDisGoodsId());
                    if (disGoodsEntity != null) {
                        existingOrder.setNxDoDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
                        nxDepartmentOrdersService.update(existingOrder);
                        
                        // 重新查询订单并添加推荐商品
                        NxDepartmentOrdersEntity orderWithRecommendations = nxDepartmentOrdersService.queryObject(existingOrder.getNxDepartmentOrdersId());
                        if (orderWithRecommendations != null) {
                            orderWithRecommendations = nxDepartmentOrdersService.addCommentsGoodsForOrder(orderWithRecommendations);
                        } else {
                            orderWithRecommendations = existingOrder;
                        }
                        
                        // 设置包装结构字段到订单实体
                        orderWithRecommendations.setStandardWeight(standardWeight != null ? standardWeight : "");
                        orderWithRecommendations.setItemUnit(itemUnit != null ? itemUnit : "");
                        orderWithRecommendations.setItemsPerCarton(itemsPerCarton != null ? itemsPerCarton : "");
                        orderWithRecommendations.setCartonUnit(cartonUnit != null ? cartonUnit : "");
                        
                        // 直接返回订单实体（订单实体已包含所有DTO字段）
                        responseMap.put(i, orderWithRecommendations);
                        continue;
                    }
                }
                
                // 如果都没找到，直接更新订单（不关联商品）
                nxDepartmentOrdersService.update(existingOrder);
                
                // 设置包装结构字段到订单实体
                existingOrder.setStandardWeight(standardWeight != null ? standardWeight : "");
                existingOrder.setItemUnit(itemUnit != null ? itemUnit : "");
                existingOrder.setItemsPerCarton(itemsPerCarton != null ? itemsPerCarton : "");
                existingOrder.setCartonUnit(cartonUnit != null ? cartonUnit : "");
                
                // 直接返回订单实体（订单实体已包含所有DTO字段）
                responseMap.put(i, existingOrder);
            }
            
            // 按照原始顺序组装响应列表（直接返回订单实体）
            List<NxDepartmentOrdersEntity> finalResponseList = new ArrayList<>();
            for (int i = 0; i < correctedItemsList.size(); i++) {
                NxDepartmentOrdersEntity order = responseMap.get(i);
                if (order != null) {
                    finalResponseList.add(order);
                }
            }
            
            logger.info("[correction] 修正完成，共更新 {} 条订单", finalResponseList.size());
            return R.ok().put("data", finalResponseList);
            
        } catch (Exception e) {
            logger.error("[correction] 系统错误: {}", e.getMessage(), e);
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

            // 获取部门特定的修正规则（Excel 类型）
            String departmentPrompt = null;
            if (depId != null && depId > 0) {
                try {
                    NxDepartmentEntity department = nxDepartmentService.queryObject(depId);
                    if (department != null) {
                        String prompt = department.getNxDepartmentOcrPromptExcel();
                        if (prompt != null && !prompt.trim().isEmpty()) {
                            departmentPrompt = prompt.trim();
                            logger.info("[recognizeOrderFromExcel] ✅ 获取到部门 Excel 修正规则，部门ID: {}, 规则长度: {} 字符", 
                                    depId, departmentPrompt.length());
//                            logger.debug("[recognizeOrderFromExcel] 部门修正规则内容: {}", departmentPrompt);
                        } else {
                            logger.info("[recognizeOrderFromExcel] 部门ID: {} 存在，但 Excel OCR prompt 为空", depId);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("[recognizeOrderFromExcel] 获取部门修正规则失败: {}", e.getMessage());
                }
            }

            // 调用 DeepSeek API 解析 Excel 表格（新方法，返回列名映射和结构化数据）
            logger.info("[recognizeOrderFromExcel] 开始调用 DeepSeek 进行解析...");
            String parsedResult = null;
            JSONObject parsedJson = null;
            List<Map<String, Object>> itemsList = new ArrayList<>();
            List<Map<String, Object>> originalItemsList = new ArrayList<>(); // 保存原始数据用于训练数据采集

            try {
                // 调用新的 DeepSeek API 解析 Excel
                parsedResult = callDeepSeekAPIForExcelOrder(excelText, departmentPrompt);
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
                    
                    // 记录原始数据中的包装结构字段
                    String name = originalItem.get("name") != null ? originalItem.get("name").toString() : "";
                    String standardWeight = originalItem.get("standardWeight") != null ? originalItem.get("standardWeight").toString() : "";
                    String itemUnit = originalItem.get("itemUnit") != null ? originalItem.get("itemUnit").toString() : "";
                    String itemsPerCarton = originalItem.get("itemsPerCarton") != null ? originalItem.get("itemsPerCarton").toString() : "";
                    String cartonUnit = originalItem.get("cartonUnit") != null ? originalItem.get("cartonUnit").toString() : "";
                    logger.info("[recognizeOrderFromExcel] 解析第 {} 个商品，保存到 originalItemsList: name={}, standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}", 
                            i + 1, name, standardWeight, itemUnit, itemsPerCarton, cartonUnit);

                    // 字段映射：将 DeepSeek 返回的字段映射到内部使用的字段
                    // DeepSeek 返回：name, quantity, spec, standardWeight, note
                    // 内部使用：name, qty, unit, remark
                    Map<String, Object> mappedItem = mapDeepSeekFieldsToInternalFields(item);
                    itemsList.add(mappedItem);
                }

                logger.info("[recognizeOrderFromExcel] DeepSeek 解析成功，解析到 {} 个商品", itemsList.size());
                
                // 验证两个列表大小是否一致
                if (originalItemsList.size() != itemsList.size()) {
                    logger.error("[recognizeOrderFromExcel] 列表大小不一致：originalItemsList.size()={}, itemsList.size()={}", 
                            originalItemsList.size(), itemsList.size());
                    return R.error("订单解析失败：数据列表大小不一致");
                }

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
                Integer ocrTaskId = null;
                try {
                    // ========== 创建OCR任务记录 ==========
                    // 获取用户信息
                    Integer uploadUserId = userId != null ? userId : -1;
                    String uploadUserName = "未知用户";
                    if (uploadUserId != null && uploadUserId > 0) {
                        try {
                            NxDistributerUserEntity uploadUser = nxDistributerUserService.queryObject(uploadUserId);
                            if (uploadUser != null && uploadUser.getNxDiuWxNickName() != null) {
                                uploadUserName = uploadUser.getNxDiuWxNickName();
                            }
                        } catch (Exception e) {
                            logger.warn("[recognizeOrderFromExcel] 获取上传用户信息失败: {}", e.getMessage());
                        }
                    }

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

                    // 创建OCR任务记录
                    NxOcrTaskEntity ocrTask = new NxOcrTaskEntity();
                    NxDepartmentEntity nxDepartmentEntity = nxDepartmentService.queryObject(depFatherId);
                    String  depName = nxDepartmentEntity.getNxDepartmentAttrName();
                    System.out.println("depnidd====" + nxDepartmentEntity.getNxDepartmentFatherId());
                    if(nxDepartmentEntity.getNxDepartmentFatherId() != 0){
                        NxDepartmentEntity subDepartmentEntity = nxDepartmentService.queryObject(depId);
                        depName = depName + '.' + subDepartmentEntity.getNxDepartmentAttrName();
                    }
                    //查询这个 depFatherId 今日的第几个图片任务
                    Map<String, Object> mapTask = new HashMap<>();
                    mapTask.put("departmentFatherId", depFatherId);
                    mapTask.put("date", formatWhatDay(0));  // 使用 date 参数，格式：yyyy-MM-dd
                    mapTask.put("type", 1);
                    int count = nxOcrTaskService.queryTotalByDepartmentAndStatus(mapTask);
                    int todayTaskNumber = count + 1;
                    ocrTask.setNxOcrTaskFileName(depName + "第" + todayTaskNumber +"excel订单");
                    ocrTask.setNxOcrTaskTotalOrders(0);
                    ocrTask.setNxOcrTaskCompletedOrders(0);
                    ocrTask.setNxOcrTaskPendingOrders(0);
                    ocrTask.setNxOcrTaskUploadTime(formatWhatYearDayTime(0));
                    ocrTask.setNxOcrTaskUploadUserId(uploadUserId);
                    ocrTask.setNxOcrTaskUploadUserName(uploadUserName);
                    ocrTask.setNxOcrTaskProcessorUserId(uploadUserId);
                    ocrTask.setNxOcrTaskProcessorUserName(uploadUserName);
                    ocrTask.setNxOcrTaskStatus(0); // 0=处理中
                    ocrTask.setNxOcrTaskCreateDate(formatWhatYearDayTime(0));
                    ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
                    ocrTask.setNxOcrTaskDistributerId(disId);
                    ocrTask.setNxOcrTaskDepartmentId(depId);
                    ocrTask.setNxOcrTaskDepartmentFatherId(finalDepFatherId);
                    ocrTask.setNxOcrTaskType(2); // 2=Excel（recognizeOrderFromExcel）
                    ocrTask.setNxOcrTaskOcrText(excelText); // 保存Excel文本内容
                    
                    // 保存任务记录（获取任务ID）
                    nxOcrTaskService.save(ocrTask);
                    ocrTaskId = ocrTask.getNxOcrTaskId();
                    logger.info("[recognizeOrderFromExcel] OCR任务记录创建成功，任务ID: {}", ocrTaskId);

                    logger.info("[recognizeOrderFromExcel] 开始处理订单，depId: {}, disId: {}, depFatherId: {}, userId: {}",
                            depId, disId, finalDepFatherId, finalUserId);

                    // 先查询当前最大的 today_order，为所有订单统一设置递增的 nxDoTodayOrder
//                    int currentMaxOrder = nxDepartmentOrdersService.queryMaxTodayOrder(finalDepFatherId);
                    int currentMaxOrder = 0;
                    Map<String, Object> map = new HashMap<>();
                    map.put("depFatherId", depId);
                    Integer integer = nxDepartmentOrdersService.queryOrderGoodsCount(map);
                    if(integer > 0){
                        if (depId != null) {
                            currentMaxOrder = nxDepartmentOrdersService.queryMaxTodayOrder(depId);
                            logger.info("[pasteSearchGoods] 当前部门最大 today_order: {}, 即将处理 {} 个订单", currentMaxOrder);
                        }
                    }
                    logger.info("[recognizeOrderFromExcel] 当前部门最大 today_order: {}, 即将处理 {} 个订单", currentMaxOrder, originalItemsList.size());
                    int todayOrderCounter = 0; // 用于跟踪当前订单的序号

                    // 处理每条识别数据
                    List<NxDepartmentOrdersEntity> orderList = new ArrayList<>();
                    // 使用 Map 保存原始索引对应的响应，以保持顺序
                    Map<Integer, NxDepartmentOrdersEntity> responseMap = new HashMap<>();
                    List<Integer> orderIndexList = new ArrayList<>(); // 保存 orderList 中每个订单对应的原始索引

                    logger.info("[recognizeOrderFromExcel] 开始处理订单列表，originalItemsList.size()={}, itemsList.size()={}", 
                            originalItemsList.size(), itemsList.size());

                    for (int i = 0; i < originalItemsList.size(); i++) {
                        // 安全检查：确保索引不越界
                        if (i >= itemsList.size()) {
                            logger.error("[recognizeOrderFromExcel] 索引越界：i={}, itemsList.size()={}, originalItemsList.size()={}", 
                                    i, itemsList.size(), originalItemsList.size());
                            break; // 跳出循环，避免死循环或越界异常
                        }
                        
                        logger.debug("[recognizeOrderFromExcel] 处理第 {} 个订单项，共 {} 个", i + 1, originalItemsList.size());
                        
                        Map<String, Object> originalItem = originalItemsList.get(i);
                        Map<String, Object> mappedItem = itemsList.get(i);

                        // 提取字段（包括包装结构字段）
                        String goodsName = originalItem.get("name") != null ? originalItem.get("name").toString().trim() : "";
                        String quantity = originalItem.get("quantity") != null ? originalItem.get("quantity").toString().trim() : "";
                        String spec = originalItem.get("spec") != null ? originalItem.get("spec").toString().trim() : "";
                        String standardWeight = originalItem.get("standardWeight") != null ? originalItem.get("standardWeight").toString().trim() : "";
                        String itemUnit = originalItem.get("itemUnit") != null ? originalItem.get("itemUnit").toString().trim() : "";
                        String itemsPerCarton = originalItem.get("itemsPerCarton") != null ? originalItem.get("itemsPerCarton").toString().trim() : "";
                        String cartonUnit = originalItem.get("cartonUnit") != null ? originalItem.get("cartonUnit").toString().trim() : "";
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

                        // 创建基本订单实体
                        NxDepartmentOrdersEntity orderBasic = new NxDepartmentOrdersEntity();
                        orderBasic.setNxDoDepartmentId(depId);
                        orderBasic.setNxDoDepartmentFatherId(finalDepFatherId);
                        orderBasic.setNxDoDistributerId(disId);
                        orderBasic.setNxDoOrderUserId(finalUserId);
                        orderBasic.setNxDoGoodsName(goodsName);
                        orderBasic.setNxDoGoodsOriginalName(goodsName);
                        orderBasic.setNxDoRemark(note);
                        orderBasic.setOrcNotice(null); // Excel 没有 isNotice
                        orderBasic.setNxDoPurchaseUserId(-1);
                        orderBasic.setNxDoIsAgent(-1);
                        orderBasic.setNxDoQuantity(quantity);
                        orderBasic.setNxDoStandard(spec);
                        // 关联OCR任务ID
                        orderBasic.setNxDoOcrTaskId(ocrTaskId);

                        // 预先设置 nxDoTodayOrder，确保顺序正确
                        int todayOrder = currentMaxOrder + todayOrderCounter + 1;
                        orderBasic.setNxDoTodayOrder(todayOrder);
                        logger.info("[recognizeOrderFromExcel] 设置订单 todayOrder: 商品名称={}, 原始索引={}, todayOrder={}, currentMaxOrder={}, counter={}",
                                goodsName, i, todayOrder, currentMaxOrder, todayOrderCounter);
                        todayOrderCounter++;

                        // 如果订单缺少3个必备条件（商品名称、数量、规格），设置状态为-2，保存订单，跳过后续处理
                        if (isDataMissing) {
                            logger.info("[recognizeOrderFast] 订单缺少数量或规格，设置状态为-2并保存订单，跳过后续商品搜索和训练数据添加: goodsName={}, quantity={}, spec={}",
                                    goodsName, quantity, spec);
                            orderBasic.setNxDoStatus(-2);
                            saveOrderWithoutGoods(orderBasic);
                            // 将订单添加到响应Map
                            responseMap.put(i, orderBasic);
                            continue;
                        }

                        // 1级优先匹配：首先查询部门商品历史记录，如果订货商品名称和规格完全匹配，则直接推断为该商品
                        Map<String, Object> depGoodsMap = new HashMap<>();
                        depGoodsMap.put("disId", disId);
                        depGoodsMap.put("depId", depId);
                        depGoodsMap.put("name", goodsName); // Excel没有rawName，使用name
                        depGoodsMap.put("standard", spec);
                        List<NxDepartmentDisGoodsEntity> departmentDisGoodsList = nxDepartmentDisGoodsService.queryDepartmentGoods(depGoodsMap);
                        logger.info("[recognizeOrderFromExcel] 1级优先匹配-部门商品历史记录查询结果数量: {}", departmentDisGoodsList.size());
                        // 如果部门商品历史记录中有完全匹配的（名称+规格），则直接使用该商品
                        if (departmentDisGoodsList.size() == 1) {
                            NxDepartmentDisGoodsEntity departmentDisGoodsEntity = departmentDisGoodsList.get(0);
                            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(departmentDisGoodsEntity.getNxDdgDisGoodsId());
                            if (distributerGoodsEntity != null) {
                                // Excel没有数量/规格改变的概念，订单状态统一设为0（已完成）
                                orderBasic.setNxDoStatus(0);
                                logger.info("[recognizeOrderFromExcel] 1级优先匹配成功，订单状态设置为 0（已完成）");
                                // 保存订单并转换为响应DTO（传递包装结构字段）
                                saveStrongQueryOrderAndConvert(orderBasic, distributerGoodsEntity,  i, responseMap);
                            } else {
                                logger.warn("[recognizeOrderFromExcel] 1级匹配-部门商品引用的配送商商品已不存在，商品ID: {}，跳过", departmentDisGoodsEntity.getNxDdgDisGoodsId());
                            }
                        }

                        // 2级优先匹配：商品库，如果订货商品名称和规格完全匹配，则直接推断为该商品
                        if (responseMap.get(i) == null) {
                            Map<String, Object> mapZero = new HashMap<>();
                            mapZero.put("disId", disId);
                            mapZero.put("searchStr", goodsName); // Excel没有rawName，使用name
                            mapZero.put("standard", spec);
                            logger.info("[recognizeOrderFromExcel] 2级优先匹配-商品名称+规格查询参数: {}", mapZero);
                            List<NxDistributerGoodsEntity> distributerGoodsEntitiesZero = nxDistributerGoodsService.queryDisGoodsByName(mapZero);
                            // 如果商品历史记录中有完全匹配的（名称+规格），则直接使用该商品
                            if (distributerGoodsEntitiesZero.size() == 1) {
                                NxDistributerGoodsEntity distributerGoodsEntity = distributerGoodsEntitiesZero.get(0);
                                logger.info("[recognizeOrderFromExcel] 2级优先匹配：商品库，直接保存订单，disGoodsId={}",
                                        distributerGoodsEntity.getNxDistributerGoodsId());

                                // Excel没有数量/规格改变的概念，订单状态统一设为0（已完成）
                                orderBasic.setNxDoStatus(0);
                                logger.info("[recognizeOrderFromExcel] 2级优先匹配成功，订单状态设置为 0（已完成）");
                                // 保存订单并转换为响应DTO（传递包装结构字段）
                                saveStrongQueryOrderAndConvert(orderBasic, distributerGoodsEntity,  i, responseMap);

                            }
                        }

                        // 3级优先匹配：商品库别名精准查询，如果订货商品名称和规格完全匹配，则直接推断为该商品
                        if (responseMap.get(i) == null) {
                            Map<String, Object> mapA = new HashMap<>();
                            mapA.put("disId", disId);
                            mapA.put("alias", goodsName); // Excel没有rawName，使用name
                            mapA.put("standard", spec);
                            List<NxDistributerGoodsEntity> distributerGoodsEntitiesAlias = nxDistributerGoodsService.queryDisGoodsByAlias(mapA);
                            // 如果商品历史记录中有完全匹配的（名称+规格），则直接使用该商品
                            if (distributerGoodsEntitiesAlias.size() == 1) {
                                NxDistributerGoodsEntity distributerGoodsEntity = distributerGoodsEntitiesAlias.get(0);
                                logger.info("[recognizeOrderFromExcel] 3级优先匹配-商品库别名精准查询，直接保存订单，disGoodsId={}",
                                        distributerGoodsEntity.getNxDistributerGoodsId());

                                // Excel没有数量/规格改变的概念，订单状态统一设为0（已完成）
                                orderBasic.setNxDoStatus(0);
                                logger.info("[recognizeOrderFromExcel] 3级优先匹配成功，订单状态设置为 0（已完成）");

                                // 保存订单并转换为响应DTO（传递包装结构字段）
                                saveStrongQueryOrderAndConvert(orderBasic, distributerGoodsEntity,  i, responseMap);

                            }
                        }

                        // 4级优先匹配：查询训练表中是否有相同内容（匹配：部门ID + 商品名称 name）
                        if (responseMap.get(i) == null) {
                            Map<String, Object> matchParams = new HashMap<>();
                            matchParams.put("depId", depId);
                            matchParams.put("goodsName", goodsName); // Excel没有rawName，使用name查询训练数据
                            matchParams.put("disGoodsId", 1);
                            NxOrderOcrTrainingDataEntity matchedTrainingData = nxOrderOcrTrainingDataService.queryByMatchFields(matchParams);

                            if (matchedTrainingData != null && matchedTrainingData.getNxOtdDisGoodsId() != null) {
                                // 训练数据中有商品ID，直接创建订单
                                logger.info("[recognizeOrderFromExcel] 4级优先匹配-找到匹配的训练数据，商品ID: {}, 直接创建订单",
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
                                orderBasic.setNxDoGoodsName(orderGoodsName);

                                // Excel没有数量/规格改变的概念，订单状态统一设为0（已完成）
                                orderBasic.setNxDoStatus(0);
                                logger.info("[recognizeOrderFromExcel] 4级优先匹配成功，订单状态设置为 0（已完成）");

                                // 保存订单并转换为响应DTO（传递包装结构字段）

                                saveStrongQueryOrderAndConvert(orderBasic, disGoodsEntity,  i, responseMap);

                            }
                        }

                        // 以上是用 name 进行4种强查询（部门商品历史记录、商品库名称、商品库别名、训练数据）
                        if (responseMap.get(i) == null) {
                            logger.info("[recognizeOrderFromExcel] name 的4种强查询都没有找到商品，创建训练数据: name={}", goodsName);
                            
                            // 构建包含包装结构信息的备注（如果存在包装结构字段，追加到备注中）
                            String noteWithPackaging = note;
                            if ((standardWeight != null && !standardWeight.isEmpty()) ||
                                (itemUnit != null && !itemUnit.isEmpty()) ||
                                (itemsPerCarton != null && !itemsPerCarton.isEmpty()) ||
                                (cartonUnit != null && !cartonUnit.isEmpty())) {
                                StringBuilder packagingInfo = new StringBuilder();
                                if (standardWeight != null && !standardWeight.isEmpty()) {
                                    packagingInfo.append("规格重量:").append(standardWeight).append(";");
                                }
                                if (itemUnit != null && !itemUnit.isEmpty()) {
                                    packagingInfo.append("最小包装单位:").append(itemUnit).append(";");
                                }
                                if (itemsPerCarton != null && !itemsPerCarton.isEmpty()) {
                                    packagingInfo.append("每箱数量:").append(itemsPerCarton).append(";");
                                }
                                if (cartonUnit != null && !cartonUnit.isEmpty()) {
                                    packagingInfo.append("大包装单位:").append(cartonUnit).append(";");
                                }
                                if (noteWithPackaging != null && !noteWithPackaging.isEmpty()) {
                                    noteWithPackaging = noteWithPackaging + " | " + packagingInfo.toString();
                                } else {
                                    noteWithPackaging = packagingInfo.toString();
                                }
                                logger.info("[recognizeOrderFromExcel] 将包装结构信息追加到备注: {}", noteWithPackaging);
                            }
                            
                            // 创建训练数据：Excel 上传没有 rawName 和 name 的概念，deepseekRecommendedName 传 null，没有 OCR 原文，originalText 传空字符串
                            // 将包装结构信息保存到备注字段中
                            NxOrderOcrTrainingDataEntity trainingData = createOrQueryTrainingData(
                                    depId, finalDepFatherId, disId, goodsName, null, quantity, spec, standardWeight, noteWithPackaging, "", finalUserId);
                            
                            // 如果是新创建的训练数据（数据源为 OCR_IMAGE），更新为 EXCEL
                            if ("OCR_IMAGE".equals(trainingData.getNxOtdDataSource())) {
                                trainingData.setNxOtdDataSource("EXCEL");
                                nxOrderOcrTrainingDataService.update(trainingData);
                            }
                            logger.info("[recognizeOrderFromExcel] 训练数据创建成功，训练数据ID: {}, 备注: {}", 
                                    trainingData.getNxOtdId(), trainingData.getNxOtdOriginalRemark());
                            
                            orderBasic.setNxDoStatus(-2);
                            orderBasic.setNxDoTrainingDataId(trainingData.getNxOtdId());
                            orderBasic.setNxDoQuantity(quantity);
                            orderBasic.setNxDoStandard(spec);
                            // 将包装结构信息也保存到订单备注中
                            orderBasic.setNxDoRemark(noteWithPackaging);
                            
                            orderList.add(orderBasic);
                            orderIndexList.add(i);
                        }
                    }

                    if (!orderList.isEmpty()) {
                        logger.info("[recognizeOrderFromExcel] 调用 searchAndSaveOrdersFromOcr，订单数量: {}, orderIndexList.size()={}", 
                                orderList.size(), orderIndexList.size());
                        
                        List<NxDepartmentOrdersEntity> searchResults = nxDepartmentOrdersService.searchAndSaveOrdersFromOcr(orderList);
                        logger.info("[recognizeOrderFromExcel] searchAndSaveOrdersFromOcr 返回结果数量: {}", 
                                searchResults != null ? searchResults.size() : 0);

                        if (searchResults != null && !searchResults.isEmpty() && !orderIndexList.isEmpty()) {
                            int maxLoopCount = Math.min(searchResults.size(), orderIndexList.size());
                            logger.info("[recognizeOrderFromExcel] 开始处理搜索结果，循环次数: {}", maxLoopCount);
                            for (int j = 0; j < maxLoopCount; j++) {
                                logger.debug("[recognizeOrderFromExcel] 处理搜索结果第 {} 个，共 {} 个", j + 1, maxLoopCount);
                                
                                if (j >= orderIndexList.size() || j >= searchResults.size()) {
                                    logger.error("[recognizeOrderFromExcel] 搜索结果处理时索引越界：j={}, orderIndexList.size()={}, searchResults.size()={}", 
                                            j, orderIndexList.size(), searchResults.size());
                                    break;
                                }
                                
                                Integer originalIndex = orderIndexList.get(j);
                                NxDepartmentOrdersEntity order = searchResults.get(j);

                                if (order == null || originalIndex == null) {
                                    logger.warn("[recognizeOrderFromExcel] 搜索结果或索引为null，跳过：j={}, order={}, originalIndex={}", 
                                            j, order, originalIndex);
                                    continue;
                                }

                                // 获取原始订单的数量、规格和包装结构字段
                            if (originalIndex < originalItemsList.size()) {
                                Map<String, Object> originalItem = originalItemsList.get(originalIndex);
                                String quantity = originalItem.get("quantity") != null ? originalItem.get("quantity").toString().trim() : "";
                                String spec = originalItem.get("spec") != null ? originalItem.get("spec").toString().trim() : "";
                                    String standardWeight = originalItem.get("standardWeight") != null ? originalItem.get("standardWeight").toString().trim() : "";
                                    String itemUnit = originalItem.get("itemUnit") != null ? originalItem.get("itemUnit").toString().trim() : "";
                                    String itemsPerCarton = originalItem.get("itemsPerCarton") != null ? originalItem.get("itemsPerCarton").toString().trim() : "";
                                    String cartonUnit = originalItem.get("cartonUnit") != null ? originalItem.get("cartonUnit").toString().trim() : "";

                                    logger.info("[recognizeOrderFromExcel] 设置包装结构字段到订单实体: originalIndex={}, goodsName={}, standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}", 
                                            originalIndex, order.getNxDoGoodsName(), standardWeight, itemUnit, itemsPerCarton, cartonUnit);

                                    // 设置包装结构字段到订单实体
                                    order.setStandardWeight(standardWeight != null && !standardWeight.isEmpty() ? standardWeight : "");
                                    order.setItemUnit(itemUnit != null && !itemUnit.isEmpty() ? itemUnit : "");
                                    order.setItemsPerCarton(itemsPerCarton != null && !itemsPerCarton.isEmpty() ? itemsPerCarton : "");
                                    order.setCartonUnit(cartonUnit != null && !cartonUnit.isEmpty() ? cartonUnit : "");

                                    logger.info("[recognizeOrderFromExcel] 设置后的订单实体包装结构字段: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}", 
                                            order.getStandardWeight(), order.getItemUnit(), order.getItemsPerCarton(), order.getCartonUnit());

                                // 如果缺少数量或规格，状态必须是-2（无论是否匹配到商品）
                                if ((quantity == null || quantity.isEmpty()) || (spec == null || spec.isEmpty())) {
                                    order.setNxDoStatus(-2);
                                    logger.info("[recognizeOrderFromExcel] 订单缺少数量或规格，强制设置状态为-2: goodsName={}, quantity={}, spec={}",
                                        order.getNxDoGoodsName(), quantity, spec);
                                }
                                } else {
                                    logger.warn("[recognizeOrderFromExcel] originalIndex ({}) 超出 originalItemsList 大小 ({})，无法设置包装结构字段", 
                                            originalIndex, originalItemsList.size());
                            }

                            responseMap.put(originalIndex, order);
                            }
                        } else {
                            logger.warn("[recognizeOrderFromExcel] searchResults 或 orderIndexList 为空，跳过处理");
                        }
                    }

                    // 按照原始顺序组装响应列表（直接返回订单实体）
                    List<NxDepartmentOrdersEntity> responseList = new ArrayList<>();
                    for (int i = 0; i < originalItemsList.size(); i++) {
                        NxDepartmentOrdersEntity order = responseMap.get(i);
                        if (order != null) {
                            // 确保包装结构字段被设置（总是从 originalItemsList 中获取并设置）
                            // 从 originalItemsList 中获取包装结构字段
                            if (i < originalItemsList.size()) {
                                Map<String, Object> originalItem = originalItemsList.get(i);
                                String standardWeight = originalItem.get("standardWeight") != null ? originalItem.get("standardWeight").toString().trim() : "";
                                String itemUnit = originalItem.get("itemUnit") != null ? originalItem.get("itemUnit").toString().trim() : "";
                                String itemsPerCarton = originalItem.get("itemsPerCarton") != null ? originalItem.get("itemsPerCarton").toString().trim() : "";
                                String cartonUnit = originalItem.get("cartonUnit") != null ? originalItem.get("cartonUnit").toString().trim() : "";
                                
                                logger.info("[recognizeOrderFromExcel] 组装响应列表时，从 originalItemsList 获取包装结构字段: i={}, goodsName={}, standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}", 
                                        i, order.getNxDoGoodsName(), standardWeight, itemUnit, itemsPerCarton, cartonUnit);
                                
                                // 总是设置包装结构字段（确保字段存在，即使为空字符串）
                                order.setStandardWeight(standardWeight);
                                order.setItemUnit(itemUnit);
                                order.setItemsPerCarton(itemsPerCarton);
                                order.setCartonUnit(cartonUnit);
                                // 确保任务ID被设置
                                if (ocrTaskId != null) {
                                    order.setNxDoOcrTaskId(ocrTaskId);
                                }
                                
                                logger.info("[recognizeOrderFromExcel] 设置后的订单实体包装结构字段: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}", 
                                        order.getStandardWeight(), order.getItemUnit(), order.getItemsPerCarton(), order.getCartonUnit());
                            } else {
                                logger.warn("[recognizeOrderFromExcel] 索引 {} 超出 originalItemsList 大小 {}，无法获取包装结构字段", i, originalItemsList.size());
                            }
                            
                            responseList.add(order);
                        } else {
                            logger.warn("[recognizeOrderFromExcel] responseMap 中索引 {} 的订单实体为 null，跳过", i);
                        }
                    }

                    logger.info("[recognizeOrderFromExcel] 订单处理完成，共 {} 条订单", responseList.size());
                    
                    // ========== 更新OCR任务状态 ==========
                    if (ocrTaskId != null) {
                        try {
                            NxOcrTaskEntity taskToUpdate = nxOcrTaskService.queryObject(ocrTaskId);
                            if (taskToUpdate != null) {
                                int completedOrders = 0;
                                int pendingOrders = 0;
                                for (NxDepartmentOrdersEntity order : responseList) {
                                    if (order.getNxDoStatus() != null) {
                                        if (order.getNxDoStatus() == 0) {
                                            completedOrders++;
                                        } else if (order.getNxDoStatus() == -2) {
                                            pendingOrders++;
                                        }
                                    }
                                }
                                taskToUpdate.setNxOcrTaskTotalOrders(responseList.size());
                                taskToUpdate.setNxOcrTaskCompletedOrders(completedOrders);
                                taskToUpdate.setNxOcrTaskPendingOrders(pendingOrders);
                                if (pendingOrders == 0) {
                                    taskToUpdate.setNxOcrTaskStatus(2); // 2=已完成
                                } else if (completedOrders > 0) {
                                    taskToUpdate.setNxOcrTaskStatus(1); // 1=部分完成
                                } else {
                                    taskToUpdate.setNxOcrTaskStatus(1); // 1=部分完成（所有订单都是待修正）
                                }
                                taskToUpdate.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
                                nxOcrTaskService.update(taskToUpdate);
                                logger.info("[recognizeOrderFromExcel] 更新任务状态完成，任务ID: {}, 总订单数: {}, 已完成订单: {}, 待修正订单: {}, 任务状态: {}",
                                        ocrTaskId, responseList.size(), completedOrders, pendingOrders, taskToUpdate.getNxOcrTaskStatus());
                            }
                        } catch (Exception e) {
                            logger.error("[recognizeOrderFromExcel] 更新任务状态失败，任务ID: {}", ocrTaskId, e);
                        }
                    }
                    
                    // 最终检查：记录所有订单的包装结构字段
//                    for (int i = 0; i < responseList.size(); i++) {
//                        PasteSearchGoodsResponseDTO dto = responseList.get(i);
//                        logger.info("[recognizeOrderFromExcel] 最终响应 DTO[{}]: goodsName={}, standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}",
//                                i, dto.getNxDoGoodsName(), dto.getStandardWeight(), dto.getItemUnit(), dto.getItemsPerCarton(), dto.getCartonUnit());
//
//                        // 验证字段是否真的被设置了
//                        if (dto.getStandardWeight() == null) {
//                            logger.error("[recognizeOrderFromExcel] ⚠️ DTO[{}] 的 standardWeight 为 null！", i);
//                        }
//                        if (dto.getItemUnit() == null) {
//                            logger.error("[recognizeOrderFromExcel] ⚠️ DTO[{}] 的 itemUnit 为 null！", i);
//                        }
//                        if (dto.getItemsPerCarton() == null) {
//                            logger.error("[recognizeOrderFromExcel] ⚠️ DTO[{}] 的 itemsPerCarton 为 null！", i);
//                        }
//                        if (dto.getCartonUnit() == null) {
//                            logger.error("[recognizeOrderFromExcel] ⚠️ DTO[{}] 的 cartonUnit 为 null！", i);
//                        }
//                    }
                    
                    // 序列化前再次检查：将 responseList 转换为 JSON 字符串，检查字段是否存在
                    try {
                        String jsonString = JSON.toJSONString(responseList);
                        logger.info("[recognizeOrderFromExcel] 序列化后的 JSON 字符串长度: {}", jsonString.length());
                        logger.debug("[recognizeOrderFromExcel] 序列化后的 JSON 字符串: {}", jsonString);
                        // 检查 JSON 中是否包含包装结构字段
                        if (!jsonString.contains("standardWeight")) {
                            logger.error("[recognizeOrderFromExcel] ⚠️ JSON 序列化后不包含 standardWeight 字段！");
                        }
                        if (!jsonString.contains("itemUnit")) {
                            logger.error("[recognizeOrderFromExcel] ⚠️ JSON 序列化后不包含 itemUnit 字段！");
                        }
                        if (!jsonString.contains("itemsPerCarton")) {
                            logger.error("[recognizeOrderFromExcel] ⚠️ JSON 序列化后不包含 itemsPerCarton 字段！");
                        }
                        if (!jsonString.contains("cartonUnit")) {
                            logger.error("[recognizeOrderFromExcel] ⚠️ JSON 序列化后不包含 cartonUnit 字段！");
                        }
                    } catch (Exception e) {
                        logger.error("[recognizeOrderFromExcel] 序列化检查失败: {}", e.getMessage(), e);
                    }

                    // 返回前最后一次确保所有 DTO 都有包装结构字段（防止序列化时丢失）
//                    for (PasteSearchGoodsResponseDTO dto : responseList) {
//                        if (dto != null) {
//                            // 如果字段为 null，设置为空字符串（确保字段存在）
//                            if (dto.getStandardWeight() == null) {
//                                dto.setStandardWeight("");
//                            }
//                            if (dto.getItemUnit() == null) {
//                                dto.setItemUnit("");
//                            }
//                            if (dto.getItemsPerCarton() == null) {
//                                dto.setItemsPerCarton("");
//                            }
//                            if (dto.getCartonUnit() == null) {
//                                dto.setCartonUnit("");
//                            }
//                        }
//                    }
                    
                    logger.info("[recognizeOrderFromExcel] 返回前最终检查：responseList 大小={}", responseList.size());
//                    if (!responseList.isEmpty()) {
//                        PasteSearchGoodsResponseDTO firstDto = responseList.get(0);
//                        logger.info("[recognizeOrderFromExcel] 第一个 DTO 的包装结构字段: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}",
//                                firstDto.getStandardWeight(), firstDto.getItemUnit(), firstDto.getItemsPerCarton(), firstDto.getCartonUnit());
//                    }

                    // 返回处理结果
                    return R.ok().put("data", responseList).put("taskId", ocrTaskId);

                } catch (Exception e) {
                    logger.error("[recognizeOrderFromExcel] 处理订单失败: {}", e.getMessage(), e);
                    // 更新任务状态为失败
                    if (ocrTaskId != null) {
                        try {
                            NxOcrTaskEntity ocrTask = nxOcrTaskService.queryObject(ocrTaskId);
                            if (ocrTask != null) {
                                ocrTask.setNxOcrTaskStatus(-1); // -1=失败
                                ocrTask.setNxOcrTaskUpdateDate(formatWhatYearDayTime(0));
                                nxOcrTaskService.update(ocrTask);
                                logger.info("[recognizeOrderFromExcel] 任务状态已更新为失败，任务ID: {}", ocrTaskId);
                            }
                        } catch (Exception updateException) {
                            logger.error("[recognizeOrderFromExcel] 更新任务状态失败", updateException);
                        }
                    }
                    // 即使处理失败，也返回识别结果
                    return R.ok().put("excelText", excelText)
                            .put("items", itemsList != null ? itemsList : new ArrayList<>())
                            .put("parsedResult", parsedResult)
                            .put("error", "处理订单失败: " + e.getMessage())
                            .put("taskId", ocrTaskId);
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
     * 商品信息补全与结构化接口
     * 上传 Excel 文件，对商品原始文本进行信息补全和结构化处理
     * Excel 格式：不是固定列，但保证一行是一个商品
     * 
     * @param file Excel 文件（必填，支持 .xls 和 .xlsx）
     * @param disId 分销商ID（必填）
     * @return 补全后的商品信息 JSON 数组
     */
    @RequestMapping(value = "/enrichGoodsFromExcel", method = RequestMethod.POST)
    @ResponseBody
    public R enrichGoodsFromExcel(@RequestParam("file") MultipartFile file,
                                  @RequestParam("disId") Integer disId) {
        try {
            // 验证参数
            if (disId == null) {
                logger.warn("[enrichGoodsFromExcel] disId 参数为空");
                return R.error("disId 参数不能为空");
            }
            
            // 验证文件
            if (file == null || file.isEmpty()) {
                logger.warn("[enrichGoodsFromExcel] 文件为空");
                return R.error("Excel文件不能为空");
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.toLowerCase().endsWith(".xls") && !fileName.toLowerCase().endsWith(".xlsx"))) {
                logger.warn("[enrichGoodsFromExcel] 文件格式不支持: {}", fileName);
                return R.error("文件格式不支持，请上传 .xls 或 .xlsx 格式的 Excel 文件");
            }

            // 验证文件大小（10MB）
            long fileSize = file.getSize();
            if (fileSize > MAX_IMAGE_BASE64_SIZE) {
                logger.warn("[enrichGoodsFromExcel] 文件过大: {} bytes (限制: {} bytes)", fileSize, MAX_IMAGE_BASE64_SIZE);
                return R.error("文件大小超过限制，最大支持 10MB");
            }

            logger.info("[enrichGoodsFromExcel] 开始解析 Excel 文件: {}, 大小: {} bytes, disId: {}", fileName, fileSize, disId);

            // 读取 Excel 文件并转换为文本
            String excelText = readExcelToText(file);
            if (excelText == null || excelText.trim().isEmpty()) {
                logger.warn("[enrichGoodsFromExcel] Excel 文件内容为空");
                return R.error("Excel 文件中没有可读取的数据");
            }

            logger.info("[enrichGoodsFromExcel] Excel 读取完成，文本内容:\n{}", excelText);

            // 调用 DeepSeek API 进行商品信息补全和结构化
            logger.info("[enrichGoodsFromExcel] 开始调用 DeepSeek 进行商品信息补全...");
            String parsedResult = null;
            List<Map<String, Object>> goodsList = new ArrayList<>();

            try {
                parsedResult = callDeepSeekAPIForGoodsEnrichment(excelText, disId);
                logger.info("[enrichGoodsFromExcel] DeepSeek API 调用成功");
                logger.debug("[enrichGoodsFromExcel] DeepSeek 返回的原始结果: {}", parsedResult);

                // 解析返回的 JSON（使用 fastjson）
                if (parsedResult != null && !parsedResult.trim().isEmpty()) {
                    com.alibaba.fastjson.JSONArray jsonArray = JSON.parseArray(parsedResult);
                    if (jsonArray != null && jsonArray.size() > 0) {
                        for (int i = 0; i < jsonArray.size(); i++) {
                            com.alibaba.fastjson.JSONObject item = jsonArray.getJSONObject(i);
                            Map<String, Object> goodsMap = new HashMap<>();
                            
                            // 提取字段
                            goodsMap.put("商品名称", item.getString("商品名称"));

                            Map<String, Object> map = new HashMap<>();
                            map.put("searchStr", item.getString("商品名称"));
                            map.put("disId", disId);
                            System.out.println("mapappa" + map);
                            List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.queryQuickSearchNxGoodsWithNxDis(map);
                            if(nxGoodsEntities.size() > 0){
                                goodsMap.put("goodsList", nxGoodsEntities);

                            }else{
                                goodsMap.put("goodsList", new ArrayList<>());
                            }

                            goodsMap.put("规格", item.getString("规格"));
                            // 规格重量可能是数字或 null
                            Object specWeight = item.get("规格重量");
                            goodsMap.put("规格重量", specWeight);
                            
                            goodsMap.put("大包装名称", item.getString("大包装名称"));
                            
                            // 大包装数量可能是数字或 null
                            Object packCount = item.get("大包装数量");
                            goodsMap.put("大包装数量", packCount);
                            
                            goodsList.add(goodsMap);
                        }
                        logger.info("[enrichGoodsFromExcel] DeepSeek 解析成功，解析到 {} 个商品", goodsList.size());
                    } else {
                        logger.error("[enrichGoodsFromExcel] DeepSeek 返回的数据格式不正确（不是数组或数组为空）");
                    }
                } else {
                    logger.error("[enrichGoodsFromExcel] DeepSeek 返回的数据为空");
                }
            } catch (Exception e) {
                logger.error("[enrichGoodsFromExcel] DeepSeek 解析失败: {}", e.getMessage(), e);
                return R.error("商品信息补全失败: " + e.getMessage())
                        .put("excelText", excelText)
                        .put("parsedResult", parsedResult);
            }

            // 返回结果
            logger.info("[enrichGoodsFromExcel] 返回给前端，商品数量: {}, disId: {}", goodsList.size(), disId);
            return R.ok()
                    .put("goodsList", goodsList)
                    .put("disId", disId)
                    .put("excelText", excelText)
                    .put("parsedResult", parsedResult);

        } catch (Exception e) {
            logger.error("[enrichGoodsFromExcel] 系统错误: {}", e.getMessage(), e);
            return R.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 调用 DeepSeek API 进行商品信息补全和结构化
     * 
     * @param excelText Excel 表格的文本内容（一行一个商品）
     * @param disId 分销商ID
     * @return DeepSeek返回的JSON字符串，包含补全后的商品信息数组
     * @throws IOException
     */
    private String callDeepSeekAPIForGoodsEnrichment(String excelText, Integer disId) throws IOException {
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

        // 构建系统提示词（写死在方法里）
        String prompt = String.join("\n",
            "你是一个【商品信息结构化整理工具】。",
            "本次处理的商品均用于【饭馆 / 餐饮订货场景】，以餐饮后厨常用商品规格为准。\n",
            "【输入说明】",
            "输入是 CSV 格式的 Excel 表格文本（固定列格式）：",
            "- 第一行是表头（列名）",
            "- 从第二行开始是数据行，每行对应一个商品",
            "- 列之间用逗号分隔",
            "",
            "【你的任务】",
            "1. 识别列映射：根据表头识别哪些列对应哪些字段",
            "2. 提取数据：从每行数据中提取商品信息",
            "3. 仅根据原文信息进行整理，不强制要求联网搜索",
            "4. 除【规格】字段外，如果原文中无法判断的字段，填 null，不要猜",
            "",
            "【列映射识别规则】",
            "你需要识别以下字段对应的列（表头可能的关键词）：",
            "",
            "- 商品名称列（必选）：",
            "  可能的表头关键词：\"商品名称\"、\"商品\"、\"品名\"、\"名称\"、\"物料\"、\"货品\"、\"货名\"",
            "",

            "- 规格列（比选）：",
            "  可能的表头关键词：\"规格\"、\"单位\"、\"包装\"、\"规格单位\"、\"计量单位\"",
            "",
                "- 品牌列（可选）：",
                "  可能的表头关键词：\"品牌\"、\"商标\"、\"厂家\"",
                "",
            "- 规格重量列（可选）：",
            "  可能的表头关键词：\"规格重量\"、\"容量\"、\"重量\"、\"净含量\"、\"规格容量\"",
            "",
            "- 大包装名称列（可选）：",
            "  可能的表头关键词：\"大包装\"、\"外包装\"、\"包装单位\"、\"箱装\"",
            "",
            "- 大包装数量列（可选）：",
            "  可能的表头关键词：\"大包装数量\"、\"装箱数量\"、\"每箱数量\"、\"件装数量\"",
            "",
            "注意：",
            "- 如果某个字段没有对应的列，从商品名称或其他列中尝试提取",
            "- 如果商品名称列中包含品牌、规格等信息，需要拆分提取",
            "- 如果规格列中包含\"数量+单位\"（如\"15斤\"、\"6箱\"），需要拆分",
            "",
            "【只输出以下字段】",
            "",
            "- 商品名称：",
            "  尽量保持原有文字内容，不需要去掉品牌，但是一定不显示规格，规格重量等修饰词",
            "  例如：海天海鲜酱油、山西陈醋",
            "",

            "- 规格：",
            "  最小销售单位的包装形式",
            "  例如：瓶、袋、桶、包",
            "  说明：\n" +
                    "  - 规格为必填字段\n" +
                    "  - 如果 CSV 中没有规格列，且无法从其他列中直接提取，\n" +
                    "    允许根据【商品名称】进行合理推断\n" +
                    "  - 推断需符合【饭馆 / 餐饮订货场景】下的常见包装形式\n" +
                    "  - 不允许使用零售小包装规格（如 小袋、独立装）" +
                    "- 特别约束：\n" +
                    "  - 如果原文中已经明确给出规格（如：斤、袋、桶、箱等），\n" +
                    "    规格字段必须严格保留原文含义，不得替换、不得语义转换\n" +
                    "  - 不允许将“斤”自动替换为“袋 / 包 / 瓶”等其他规格\n",
            "",
            "- 规格重量：",
            "  最小销售单位对应的容量或重量",
            "  必须统一换算为标准单位：",
            "  * 重量单位：斤 → kg（100斤 = 50kg），克 → g",
            "  * 容量单位：毫升 → ml，升 → L",
            "  例如：500ml、1.9L、1kg、500g",
            "  无法判断填 null",
            "",
                "- 大包装数量：",
                "  一个大包装中包含的最小销售单位数量",
                "  例如：24（表示一箱 24 瓶）",
                "  无法判断填 null" +
                        "- 特别说明：\n" +
                        "  - 如果规格为“斤”，表示按重量散装销售的最小销售单位\n" +
                        "  - 此时【规格重量】必须填 null\n" +
                        "  - 不允许根据“斤”联想、推断、补全为 1kg、0.5kg 等重量\n"
                ,
                "",
            "- 大包装名称：\n" +
                    "  外层包装形式\n" +
                    "\n" +
                    "  统一规则：\n" +
                    "  1. 如果原文或列中出现“大包装名称”为“件”，\n" +
                    "     输出时必须统一替换为“箱”\n" +
                    "  2. 如果存在【大包装数量】，但没有明确的大包装名称，\n" +
                    "     默认将【大包装名称】设为“箱”\n" +
                    "\n" +
                    "  例如：箱、包\n" +
                    "  如果既没有大包装数量，也无法判断，则填 null\n",
            "",

            "【规则】",
            "1. 不要扩展字段",
            "2. 不要解释",
            "3. 不要合并商品",
            "4. 每行数据对应一个商品，输出一个 JSON 对象",
            "5. 输出 JSON 数组格式",
            "6. 规格与规格重量之间不得相互纠正、替换或推断\n" +
                    "   - 已明确的规格不得因规格重量缺失而被修改\n" +
                    "   - 已明确的重量不得反向修改规格\n",
            "",
            "【输出格式】",
            "你必须只输出 JSON 数组，不得输出任何解释、分析、注释文字。",
            "输出格式示例：",
            "[",
            "  {",
            "    \"商品名称\": \"海天海鲜酱油\",",
            "    \"规格\": \"瓶\",",
            "    \"规格重量\": \"500ml\",",
            "    \"大包装名称\": \"箱\",",
            "    \"大包装数量\": 24",
            "  }",
            "]",
            "",
            "注意：",
            "- 如果某个字段无法确定，必须填 null（JSON null）",
            "- 规格重量必须是字符串格式，且必须统一换算为标准单位：",
            "  * 重量：斤 → kg，克 → g",
            "  * 容量：毫升 → ml，升 → L",
            "  例如：\"500ml\"、\"1.9L\"、\"1kg\"、\"500g\"",
            "- 如果原文是\"500毫升\"，必须转换为\"500ml\"",
            "- 如果原文是\"1升\"，必须转换为\"1L\"",
            "- 如果原文是\"500克\"，必须转换为\"500g\""
        );

        // 添加系统消息
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", prompt);
        messages.put(systemMessage);

        // 添加用户消息
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", "下面是 Excel 表格内容（固定列格式，第一行是表头）：\n<<<\n" + excelText + "\n>>>\n\n请识别列映射关系，然后对每一行数据（每个商品）分别进行信息提取和结构化处理。");
        messages.put(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.2);
        requestBody.put("stream", false); // 禁用流式响应

        // 设置请求头
        StringEntity entity = new StringEntity(requestBody.toString(), "UTF-8");
        entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + deepSeekApiKey);

        logger.info("[DeepSeek GoodsEnrichment] 准备发送请求到 DeepSeek API");

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
            logger.info("[DeepSeek GoodsEnrichment] 响应内容: {}", responseContent);

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
                String content = message.getString("content");

                logger.info("[DeepSeek GoodsEnrichment] 提取到的内容长度: {} 字符", content.length());

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

                logger.info("[DeepSeek GoodsEnrichment] 清理后的内容长度: {} 字符", content.length());
                logger.debug("[DeepSeek GoodsEnrichment] 清理后的内容: {}", content);

                EntityUtils.consume(responseEntity);
                response.close();
                client.close();

                return content;
            } else {
                EntityUtils.consume(responseEntity);
                response.close();
                client.close();
                throw new IOException("DeepSeek API 返回的 choices 为空");
            }
        } catch (Exception e) {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException ex) {
                    logger.warn("[DeepSeek GoodsEnrichment] 关闭响应异常: {}", ex.getMessage());
                }
            }
            try {
                client.close();
            } catch (IOException ex) {
                logger.warn("[DeepSeek GoodsEnrichment] 关闭客户端异常: {}", ex.getMessage());
            }
            throw e;
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
        requestBody.put("temperature", 0.2);
        requestBody.put("stream", false); // 禁用流式响应，避免读取响应实体时长时间等待

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
     * 从 Map 中获取字段值，支持多个字段名（按优先级）
     * 
     * @param item Map 对象
     * @param primaryField 主要字段名
     * @param fallbackField 备用字段名（可选）
     * @return 字段值，如果不存在则返回空字符串
     */
    private String getFieldValue(Map<String, Object> item, String primaryField, String fallbackField) {
        Object value = item.get(primaryField);
        if (value != null && !value.toString().trim().isEmpty()) {
            return value.toString().trim();
        }
        if (fallbackField != null && !fallbackField.isEmpty()) {
            value = item.get(fallbackField);
            if (value != null && !value.toString().trim().isEmpty()) {
                return value.toString().trim();
            }
        }
        return "";
    }

    /**
     * 调用 DeepSeek API 进行订单修正
     * 
     * @param orderItems 已解析的订单数组
     * @param userInstructions 用户修正指令
     * @return DeepSeek返回的修正后的JSON字符串
     * @throws IOException
     */
    private String callDeepSeekAPIForCorrection(List<Map<String, Object>> orderItems, String userInstructions) throws IOException {
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
        
        // 构建 prompt
        String prompt = String.join("\n",
                "你是一个【订单解析结果的受控修正引擎】。",
                "",
                "你将收到两部分输入：",
                "1️⃣ 已解析完成的 orderItems JSON（来自 OCR + 主解析）",
                "2️⃣ 用户在前端输入的【人工修正指令文本】",
                "",
                "⚠️ 注意：你【不再做 OCR，不再做识别】，",
                "你只能在【已有解析结果】基础上进行【受限修改】。",
                "",
                "==============================",
                "【P0 级｜绝对禁止规则】",
                "==============================",
                "",
                "1. ❌ 严禁删除订单条目（除非用户明确说\"删除/不要/取消\"）",
                "2. ❌ 严禁修改 quantity / spec（除非用户明确指定）",
                "3. ❌ 严禁新增凭空商品",
                "4. ❌ 严禁重新推理图片内容",
                "5. ❌ 严禁改变 rawName",
                "",
                "==============================",
                "【P1 级｜允许修改的字段】",
                "==============================",
                "",
                "在用户明确指示的前提下，你【仅允许】修改：",
                "",
                "- name（最终商品名）",
                "- standardWeight（规格重量）",
                "- itemUnit（最小包装单位）",
                "- itemsPerCarton（每箱数量）",
                "- cartonUnit（大包装单位）",
                "- note（备注，用户说\"不要备注\"时清空）",
                "- isNotice（追加\"人工修正说明\"）",
                "",
                "⚠️ rawName 永远保持不变",
                "",
                "==============================",
                "【P1 级｜用户指令理解规则】",
                "==============================",
                "",
                "- 用户输入视为【人工确认】，优先级高于 AI 推断",
                "- 若用户明确否定原结论（如\"不是 X，是 Y\"），必须立刻替换",
                "- 若用户只是补充信息（如规格），只补充，不推翻其他字段",
                "- 若指令模糊，必须在 isNotice 标注\"需人工确认\"",
                "",
                "==============================",
                "【P0 级｜修改方式规则】",
                "==============================",
                "",
                "- 不要重写整个 orderItems",
                "- 只修改必要字段",
                "- 其他字段原样返回",
                "",
                "==============================",
                "【P0 级｜输出格式】",
                "==============================",
                "",
                "- 返回完整的 JSON",
                "- orderItems 数量必须与输入一致",
                "- JSON 必须可直接用于下单",
                "",
                "==============================",
                "【格式化规则】",
                "==============================",
                "",
                "1. note 字段：如果用户说\"不要备注\"，则清空 note 字段（设为空字符串）",
                "2. quantity 字段：如果小数部分是 0（如 40.000），则只显示整数部分（如 40）",
                "   如果有小数部分（如 40.5），则保留小数",
                "",
                "==============================",
                "【示例】",
                "==============================",
                "",
                "原解析：",
                "{",
                "  \"rawName\": \"口套\",",
                "  \"name\": \"口罩\",",
                "  \"quantity\": \"1\",",
                "  \"spec\": \"代\"",
                "}",
                "",
                "用户输入：",
                "\"这是口蘑，不是口罩\"",
                "",
                "修正结果：",
                "{",
                "  \"rawName\": \"口套\",",
                "  \"name\": \"口蘑\",",
                "  \"quantity\": \"1\",",
                "  \"spec\": \"代\",",
                "  \"isNotice\": \"人工修正：用户确认这是口蘑，不是口罩\"",
                "}"
        );
        
        systemMessage.put("content", prompt);
        messages.put(systemMessage);
        
        // 构建用户消息：包含 orderItems 和 userInstructions
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        
        // 将 orderItems 转换为 JSON 字符串
        // 注意：前端传入的字段名可能是 nxDoGoodsName、nxDoQuantity 等，需要映射为 DeepSeek 期望的字段名
        JSONArray orderItemsJson = new JSONArray();
        for (int idx = 0; idx < orderItems.size(); idx++) {
            Map<String, Object> item = orderItems.get(idx);
            JSONObject itemJson = new JSONObject();
            
            // 调试：输出第一个订单项的所有字段名和值
            if (idx == 0) {
                logger.info("[callDeepSeekAPIForCorrection] 第 0 个订单项的所有字段: {}", item.keySet());
                for (Map.Entry<String, Object> entry : item.entrySet()) {
                    logger.info("[callDeepSeekAPIForCorrection] 字段 {} = {}", entry.getKey(), entry.getValue());
                }
            }
            
            // 字段映射：前端字段名 -> DeepSeek 期望的字段名
            // 优先使用标准字段名（name, quantity 等），如果没有则尝试前端字段名（nxDoGoodsName, nxDoQuantity 等）
            String rawName = getFieldValue(item, "rawName", "nxDoGoodsOriginalName");
            String name = getFieldValue(item, "name", "nxDoGoodsName");
            String quantity = getFieldValue(item, "quantity", "nxDoQuantity");
            String spec = getFieldValue(item, "spec", "nxDoStandard");
            String standardWeight = getFieldValue(item, "standardWeight", "");
            String itemUnit = getFieldValue(item, "itemUnit", "");
            String itemsPerCarton = getFieldValue(item, "itemsPerCarton", "");
            String cartonUnit = getFieldValue(item, "cartonUnit", "");
            String note = getFieldValue(item, "note", "nxDoRemark");
            String isNotice = getFieldValue(item, "isNotice", "orcNotice");
            
            // 调试：输出提取的字段值
            if (idx == 0) {
                logger.info("[callDeepSeekAPIForCorrection] 提取的字段值: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}", 
                        standardWeight, itemUnit, itemsPerCarton, cartonUnit);
            }
            
            // 如果 rawName 为空，使用 name
            if ((rawName == null || rawName.trim().isEmpty()) && !name.trim().isEmpty()) {
                rawName = name;
            }
            
            itemJson.put("rawName", rawName != null ? rawName : "");
            itemJson.put("name", name != null ? name : "");
            itemJson.put("quantity", quantity != null ? quantity : "");
            itemJson.put("spec", spec != null ? spec : "");
            itemJson.put("standardWeight", standardWeight != null ? standardWeight : "");
            itemJson.put("itemUnit", itemUnit != null ? itemUnit : "");
            itemJson.put("itemsPerCarton", itemsPerCarton != null ? itemsPerCarton : "");
            itemJson.put("cartonUnit", cartonUnit != null ? cartonUnit : "");
            itemJson.put("note", note != null ? note : "");
            itemJson.put("isNotice", isNotice != null ? isNotice : "");
            
            orderItemsJson.put(itemJson);
        }
        
        logger.debug("[callDeepSeekAPIForCorrection] 构建的 orderItems JSON: {}", orderItemsJson.toString(2));
        
        String userContent = String.join("\n",
                "请根据以下用户指令修正订单：",
                "",
                "【用户指令】：",
                userInstructions,
                "",
                "【当前订单数据】：",
                orderItemsJson.toString(2),
                "",
                "请返回修正后的完整 orderItems JSON 数组。"
        );
        
        userMessage.put("content", userContent);
        messages.put(userMessage);
        
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.1); // 降低随机性，确保修正准确
        requestBody.put("stream", false); // 禁用流式响应，避免读取响应实体时长时间等待
        
        // 设置请求头
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + deepSeekApiKey);
        
        // 设置请求体
        StringEntity entity = new StringEntity(requestBody.toString(), "UTF-8");
        httpPost.setEntity(entity);
        
        logger.info("[callDeepSeekAPIForCorrection] 请求 DeepSeek API，订单数量: {}, 用户指令: {}", 
                orderItems.size(), userInstructions);
        
        // 发送请求
        CloseableHttpResponse response = null;
        try {
            response = client.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == 200) {
                HttpEntity responseEntity = response.getEntity();
                String responseContent = EntityUtils.toString(responseEntity, "UTF-8");
                EntityUtils.consume(responseEntity);
                response.close();
                client.close();
                
                logger.info("[callDeepSeekAPIForCorrection] DeepSeek API 调用成功，原始响应长度: {}", responseContent.length());
                
                // 解析 JSON 响应，提取 content
                JSONObject responseJson = new JSONObject(responseContent);
                JSONArray choices = responseJson.getJSONArray("choices");
                if (choices.length() > 0) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject message = choice.getJSONObject("message");
                    String content = message.getString("content");
                    
                    logger.info("[callDeepSeekAPIForCorrection] 提取到的内容长度: {} 字符", content.length());
                    logger.debug("[callDeepSeekAPIForCorrection] 提取到的原始内容: {}", content);
                    
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
                    
                    logger.info("[callDeepSeekAPIForCorrection] 清理后的内容长度: {} 字符", content.length());
                    logger.debug("[callDeepSeekAPIForCorrection] 清理后的内容: {}", content);
                    
                    return content;
                } else {
                    throw new IOException("DeepSeek API 返回空结果");
                }
            } else {
                EntityUtils.consume(response.getEntity());
                response.close();
                client.close();
                throw new IOException("DeepSeek API 返回错误状态码: " + statusCode);
            }
        } catch (org.apache.http.conn.ConnectTimeoutException e) {
            logger.error("[callDeepSeekAPIForCorrection] 连接超时: {}", e.getMessage(), e);
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
        } catch (Exception e) {
            logger.error("[callDeepSeekAPIForCorrection] 调用异常: {}", e.getMessage(), e);
            if (response != null) {
                try {
                    response.close();
                } catch (Exception ex) {
                    logger.error("[callDeepSeekAPIForCorrection] 关闭响应失败", ex);
                }
            }
            try {
                client.close();
            } catch (Exception ex) {
                logger.error("[callDeepSeekAPIForCorrection] 关闭客户端失败", ex);
            }
            throw new IOException("DeepSeek API 调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 调用 DeepSeek API 解析订单文本
     * 
     * @param ocrText OCR 识别的订单文本（按行组织）
     * @param departmentPrompt 部门特定的修正规则（可选，来自用户历史修正要求）
     * @return DeepSeek返回的JSON字符串（应该是纯JSON数组）
     * @throws IOException
     */
    private String callDeepSeekAPIForOrder(String ocrText, String departmentPrompt) throws IOException {
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
        
        // 从数据库读取系统 prompt，如果不存在则使用默认值
        String systemPromptContent = nxPromptService.getPromptContentByKey("OCR_IMAGE");
        String prompt;
        if (systemPromptContent != null && !systemPromptContent.trim().isEmpty()) {
            prompt = systemPromptContent;
//            logger.info("[callDeepSeekAPIForOrder] promp===="+ prompt);
            logger.info("[callDeepSeekAPIForOrder] 使用数据库中的系统 prompt (OCR_IMAGE)");
        } else {
            // 如果数据库中没有，使用默认的硬编码 prompt（向后兼容）
            logger.warn("[callDeepSeekAPIForOrder] 数据库中未找到 OCR_IMAGE prompt，使用默认 prompt");
            prompt = String.join("\n",
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
                "【P1.5 级｜包装规格与大包装结构解析（必须执行）】",
                "==============================",
                "",
                "当 rawName 或 name 中包含\"商品包装结构\"时，你必须识别并拆解以下字段：",
                "- standardWeight：规格重量（如 250ml、1.9L、5L、500g、10kg）",
                "- itemUnit：最小包装单位（如 盒、瓶、袋、听、包）",
                "- itemsPerCarton：每个大包装中包含的小包装数量（如 36、24、12）",
                "- cartonUnit：大包装单位（如 箱、件、提、包）",
                "",
                "【典型结构模式（只允许按字形与结构匹配，原样提取，禁止猜测）】",
                "",
                "A) 重量/容量 + /小单位 + *数量 + /大单位",
                "   示例：伊利优酸乳250ml/盒*36/箱",
                "   -> standardWeight = \"250ml\"",
                "   -> itemUnit = \"盒\"",
                "   -> itemsPerCarton = \"36\"",
                "   -> cartonUnit = \"箱\"",
                "",
                "B) 重量/容量 + 小单位 + *数量 + 大单位（斜杠可能缺失）",
                "   示例：250ml盒*36箱",
                "",
                "C) 乘号的多种 OCR 形式必须识别为包装结构：",
                "   \"*\", \"x\", \"X\", \"×\", \"乘\"",
                "",
                "【名称清洗规则（必须执行）】",
                "- 输出 name 时，必须尽量为\"纯商品名\"，将包装结构从名称中剥离：",
                "  rawName = \"伊利优酸乳250ml/盒*36/箱\"",
                "  name = \"伊利优酸乳\"",
                "- rawName 永远保持 OCR 原文，不得清洗。",
                "",
                "【与下单数量的关系（重要）】",
                "- quantity/spec 只表示\"本行订货数量与订货单位\"，不得被包装结构覆盖。",
                "- 包装结构 ≠ 下单数量。",
                "- 若本行未出现明确的订货数量（如仅出现商品名+包装结构），",
                "  仍需输出条目，quantity/spec 可为空，但包装字段必须提取。",
                "",
                "【无法识别时】",
                "- 若不满足上述结构模式，standardWeight / itemUnit / itemsPerCarton / cartonUnit 统一输出空字符串。",
                "- 严禁根据常识或经验推测包装数量。",
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
                "- rawName：【OCR 原始商品名】（仅商品名部分，原样保留不纠错。如「气椒」「大白菜」）",
                "- name：【最终下单名称】",
                "  - **严禁**填入 OCR 错误原文（除非完全无法纠错）。",
                "  - **必须**填入你纠错后的结果。",
                "- originalText：【OCR 完整原始行】（**必须**返回，用于训练数据匹配）",
                "  - **固定格式**：商品名 + 数量 + 规格，如「大白菜 2 个」「气椒 20 斤」「西红柿 15 斤」",
                "  - 若 quantity、spec 非空，则 originalText 必须包含它们，不得只返回商品名",
                "  - 若 quantity、spec 为空（如仅包装结构），则 originalText = 商品名或整行",
                "- quantity：订货数量",
                "- spec：订货单位（斤 / 把 / 箱 / 件 等）",
                "- standardWeight：规格重量（如 250ml / 1.9L / 500g）",
                "- itemUnit：最小包装单位（如 盒 / 瓶 / 袋）",
                "- itemsPerCarton：每个大包装内的小包装数量",
                "- cartonUnit：大包装单位（如 箱 / 件）",
                "- note：原始备注（括号内容、去皮、空心等）",
                "- isNotice：系统提示（纠错说明、包装结构解析说明）",
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
                "伊利优酸乳250ml/盒*36/箱",
                "",
                "正确输出（请严格模仿此逻辑）：",
                "{",
                "  \"orderItems\": [",
                "    {",
                "      \"rawName\": \"气椒\",",
                "      \"name\": \"青椒\",",
                "      \"originalText\": \"气椒 20 斤\",",
                "      \"quantity\": \"20\",",
                "      \"spec\": \"斤\",",
                "      \"standardWeight\": \"\",",
                "      \"itemUnit\": \"\",",
                "      \"itemsPerCarton\": \"\",",
                "      \"cartonUnit\": \"\",",
                "      \"note\": \"\",",
                "      \"isNotice\": \"原名=气椒；纠错=青椒；可信度=高\"",
                "    },",
                "    {",
                "      \"rawName\": \"毛白带\",",
                "      \"name\": \"小白菜\",",
                "      \"originalText\": \"毛白带 5 把\",",
                "      \"quantity\": \"5\",",
                "      \"spec\": \"把\",",
                "      \"standardWeight\": \"\",",
                "      \"itemUnit\": \"\",",
                "      \"itemsPerCarton\": \"\",",
                "      \"cartonUnit\": \"\",",
                "      \"note\": \"\",",
                "      \"isNotice\": \"原名=毛白带；纠错=小白菜；可信度=中\"",
                "    },",
                "    {",
                "      \"rawName\": \"伊利优酸乳250ml/盒*36/箱\",",
                "      \"name\": \"伊利优酸乳\",",
                "      \"originalText\": \"伊利优酸乳250ml/盒*36/箱\",",
                "      \"quantity\": \"\",",
                "      \"spec\": \"\",",
                "      \"standardWeight\": \"250ml\",",
                "      \"itemUnit\": \"盒\",",
                "      \"itemsPerCarton\": \"36\",",
                "      \"cartonUnit\": \"箱\",",
                "      \"note\": \"\",",
                "      \"isNotice\": \"包装结构识别：250ml/盒*36/箱\"",
                "    }",
                "  ]",
                "}",
                ""
            );
        }
        
        // 如果有部门特定的修正规则，添加到 prompt 中（优先级高于通用规则）
        if (departmentPrompt != null && !departmentPrompt.trim().isEmpty()) {
            prompt += String.join("\n",
                    "",
                    "==============================",
                    "【P-1 级｜部门特定修正规则（最高优先级，必须严格遵守）】",
                    "==============================",
                    "",
                    "以下规则来自该部门用户的历史修正要求，**优先级高于上述所有通用规则**。",
                    "请严格按照以下规则执行：",
                    "",
                    departmentPrompt.trim(),
                    "",
                    "==============================",
                    ""
            );
        }
        
        // 以下【字段格式强制要求】和【最终铁律】无论 prompt 来自数据库或默认值，均会追加
        prompt += String.join("\n",
                "",
                "==============================",
                "【字段格式强制要求（必须遵守）】",
                "==============================",
                "",
                "originalText 必须为该订单项的完整 OCR 行，格式：商品名 + 数量 + 规格。",
                "示例：OCR 行为「大白菜 2 个」时，originalText 必须为「大白菜 2 个」，不得仅为「大白菜」。",
                "示例：OCR 行为「西红柿 15 斤」时，originalText 必须为「西红柿 15 斤」，不得仅为「西红柿」。",
                "rawName 为仅商品名部分（如「大白菜」「西红柿」）。",
                "",
                "==============================",
                "【最终铁律】",
                "==============================",
                "",
                "rawName = OCR 原始商品名（仅名称，不纠错）。",
                "originalText = 完整行（商品名+数量+规格），**必须**包含 quantity 和 spec，不得只写商品名。",
                "name = 最终下单商品（纠错后）。",
                "isNotice = OCR 历史记录。切勿搞反。"
        );

        systemMessage.put("content", prompt);
        
        // 打印完整的 prompt 内容（用于调试）
        logger.info("[callDeepSeekAPIForOrder] ========== 发送给 DeepSeek 的完整 Prompt ==========");
        logger.info("[callDeepSeekAPIForOrder] Prompt 总长度: {} 字符", prompt.length());
        if (departmentPrompt != null && !departmentPrompt.trim().isEmpty()) {
            logger.info("[callDeepSeekAPIForOrder] 包含部门特定修正规则: 是");
        } else {
            logger.info("[callDeepSeekAPIForOrder] 包含部门特定修正规则: 否");
        }
        logger.info("[callDeepSeekAPIForOrder] 完整 Prompt 内容:\n{}", prompt);
        logger.info("[callDeepSeekAPIForOrder] ====================================================");


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
        requestBody.put("stream", false); // 禁用流式响应，避免读取响应实体时长时间等待
        requestBody.put("max_tokens", 4000); // 限制最大 token 数，避免响应被截断（19个商品大约需要3000+ tokens）

        // 设置请求头
        StringEntity entity = new StringEntity(requestBody.toString(), "UTF-8");
        entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + deepSeekApiKey);

        logger.info("[DeepSeek Order] 准备发送请求到 DeepSeek API");
        logger.info("[DeepSeek Order] 请求体长度: {} 字符", requestBody.toString().length());
        logger.debug("[DeepSeek Order] 请求体内容: {}", requestBody.toString());
        logger.info("[DeepSeek Order] stream 参数: {}", requestBody.optBoolean("stream", true));

        // 执行请求
        logger.info("[DeepSeek Order] 正在执行 HTTP 请求...");
        long httpRequestStartTime = System.currentTimeMillis();
        
        CloseableHttpResponse response = null;
        try {
            response = client.execute(httpPost);
            long httpRequestElapsedTime = System.currentTimeMillis() - httpRequestStartTime;
            logger.info("[DeepSeek Order] HTTP 请求完成，耗时: {} ms ({} 秒)", httpRequestElapsedTime, httpRequestElapsedTime / 1000.0);
            
            if (response == null) {
                logger.error("[DeepSeek Order] HTTP 响应为 null");
                throw new IOException("DeepSeek API 返回空响应");
            }
            
            int statusCode = response.getStatusLine().getStatusCode();
            logger.info("[DeepSeek Order] HTTP 响应状态码: {}", statusCode);
            
            if (statusCode != 200) {
                long errorReadStartTime = System.currentTimeMillis();
                String errorContent = EntityUtils.toString(response.getEntity(), "UTF-8");
                long errorReadElapsedTime = System.currentTimeMillis() - errorReadStartTime;
                logger.error("[DeepSeek Order] HTTP 错误响应读取耗时: {} ms ({} 秒), 内容: {}", errorReadElapsedTime, errorReadElapsedTime / 1000.0, errorContent);
                throw new IOException("DeepSeek API 返回错误状态码: " + statusCode + ", 响应: " + errorContent);
            }
            
            // 读取响应实体（这一步可能很慢，因为可能是流式响应）
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity == null) {
                logger.error("[DeepSeek Order] 响应实体为 null");
                throw new IOException("DeepSeek API 返回空响应实体");
            }
            logger.info("[DeepSeek Order] 开始读取响应实体...");
            long readEntityStartTime = System.currentTimeMillis();
            String responseContent = EntityUtils.toString(responseEntity, "UTF-8");
            long readEntityElapsedTime = System.currentTimeMillis() - readEntityStartTime;
            logger.info("[DeepSeek Order] 读取响应实体耗时: {} ms ({} 秒)", readEntityElapsedTime, readEntityElapsedTime / 1000.0);
            logger.info("[DeepSeek Order] 响应内容长度: {} 字符", responseContent != null ? responseContent.length() : 0);
            if (responseContent == null || responseContent.trim().isEmpty()) {
                logger.error("[DeepSeek Order] 响应内容为空");
                throw new IOException("DeepSeek API 返回空内容");
            }
            logger.debug("[DeepSeek Order] 响应内容: {}", responseContent);

            // 解析响应 JSON
            logger.info("[DeepSeek Order] 开始解析响应 JSON...");
            long parseJsonStartTime = System.currentTimeMillis();
            JSONObject responseJson;
            try {
                responseJson = new JSONObject(responseContent);
            } catch (Exception e) {
                logger.error("[DeepSeek Order] JSON 解析失败，响应内容: {}", responseContent);
                throw new IOException("DeepSeek API 返回的 JSON 格式错误: " + e.getMessage(), e);
            }
            JSONArray choices = responseJson.getJSONArray("choices");
            logger.info("[DeepSeek Order] JSON 解析成功，choices 数量: {}", choices != null ? choices.length() : 0);
            if (choices.length() > 0) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                String content = message.getString("content");
                long parseJsonElapsedTime = System.currentTimeMillis() - parseJsonStartTime;
                logger.info("[DeepSeek Order] JSON 解析耗时: {} ms ({} 秒)", parseJsonElapsedTime, parseJsonElapsedTime / 1000.0);
                
                logger.info("[DeepSeek Order] 提取到的内容长度: {} 字符", content.length());
                
                // 提取 JSON 部分（去除可能的 markdown 代码块标记）
                long cleanContentStartTime = System.currentTimeMillis();
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
                long cleanContentElapsedTime = System.currentTimeMillis() - cleanContentStartTime;
                logger.info("[DeepSeek Order] 内容清理耗时: {} ms ({} 秒)", cleanContentElapsedTime, cleanContentElapsedTime / 1000.0);
                
                logger.info("[DeepSeek Order] 清理后的内容长度: {} 字符", content.length());
                // 显示清理后的内容（前1000字符，便于调试）
                int previewLength = Math.min(1000, content.length());
                logger.info("[DeepSeek Order] 清理后的内容（前{}字符）: {}", previewLength, 
                        content.length() > previewLength ? content.substring(0, previewLength) + "..." : content);
                logger.debug("[DeepSeek Order] 清理后的完整内容: {}", content);
                
                EntityUtils.consume(responseEntity);
                
                return content;
            } else {
                EntityUtils.consume(responseEntity);
                throw new IOException("DeepSeek API 返回空结果");
            }
        } catch (Exception e) {
            logger.error("[DeepSeek Order] 调用异常: {}", e.getMessage(), e);
            throw new IOException("DeepSeek API 调用失败: " + e.getMessage(), e);
        } finally {
            // 确保资源正确释放，避免资源泄漏导致服务器瘫痪
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity()); // 确保响应实体被消费
                    response.close();
                } catch (Exception ex) {
                    logger.error("[DeepSeek Order] 关闭响应失败", ex);
                }
            }
            if (client != null) {
                try {
                    client.close();
                } catch (Exception ex) {
                    logger.error("[DeepSeek Order] 关闭客户端失败", ex);
                }
            }
        }
    }




    /**
     * 调用 DeepSeek API 解析 Excel 表格，返回列名映射和结构化数据
     *
     * @param excelText Excel 表格的文本内容
     * @param departmentPrompt 部门特定的修正规则（可选，来自用户历史修正要求）
     * @return DeepSeek返回的JSON字符串，包含列名映射和数据
     * @throws IOException
     */
    private String callDeepSeekAPIForExcelOrder(String excelText, String departmentPrompt) throws IOException {
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

        // 从数据库读取系统 prompt，如果不存在则使用默认值
        String systemPromptContent = nxPromptService.getPromptContentByKey("OCR_EXCEL");
        String prompt;
        if (systemPromptContent != null && !systemPromptContent.trim().isEmpty()) {
            prompt = systemPromptContent;
            logger.info("[callDeepSeekAPIForExcelOrder] 使用数据库中的系统 prompt (OCR_EXCEL)");
        } else {
            // 如果数据库中没有，使用默认的硬编码 prompt（向后兼容）
            logger.warn("[callDeepSeekAPIForExcelOrder] 数据库中未找到 OCR_EXCEL prompt，使用默认 prompt");
            // 构建系统提示词（根据用户提供的详细 prompt）
            prompt = String.join("\n",
                "你是一个【生鲜订单 Excel（CSV）解析引擎】。",
                "输入为 CSV 格式的 Excel 表格文本（第一行通常是表头）。你的输出将直接用于真实下单，请严格遵守以下规则。",
                "",
                "==============================",
                "【P0 级｜绝对禁止规则（最高优先级）】",
                "==============================",
                "",
                "1) 你【绝对不允许】因为不确定、列名混乱、内容不规范等原因，丢弃任何可能的订单行。",
                "2) Excel 数据被视为\"人工输入/导出的结构化数据\"，你【不需要 OCR 纠错】，也【不需要 rawName】。",
                "3) 你必须只输出 JSON，不得输出任何解释、分析、注释文字。",
                "",
                "==============================",
                "【P1 级｜输入与识别任务】",
                "==============================",
                "",
                "- 输入是 CSV 文本：用逗号分隔列；第一行通常是表头。",
                "- 你要做两件事：",
                "  A) 识别列映射 columnMapping：哪一列是商品名称、订货数量、订货单位/规格、备注（可能不存在）。",
                "  B) 将每一行转换为结构化订单条目 data[]。",
                "",
                "==============================",
                "【P1.1 级｜列映射规则】",
                "==============================",
                "",
                "你需要尽量识别以下字段对应的列（列字母从左到右 A、B、C、D...）：",
                "",
                "- 商品名称列（必选）：可能的表头关键词：",
                "  \"商品\", \"商品名称\", \"品名\", \"名称\", \"物料\", \"货品\", \"货名\"",
                "",
                "- 订货数量列（尽量识别）：可能关键词：",
                "  \"数量\", \"订货数量\", \"下单数量\", \"订购数量\", \"要货\", \"件数\", \"数\"",
                "",
                "- 订货单位/规格列（尽量识别）：可能关键词：",
                "  \"单位\", \"规格\", \"订货规格\", \"订货单位\", \"计量单位\", \"规格单位\"",
                "",
                "- 备注列（可选）：可能关键词：",
                "  \"备注\", \"说明\", \"要求\", \"备注信息\"",
                "",
                "注意：",
                "- 有些表会把\"数量+单位\"合在一列，例如 \"15斤\"、\"6箱\"。你必须能识别这种情况，并拆分 quantity/spec。",
                "- 有些表会把包装结构写在\"商品名称列\"或\"规格列\"里（例如：伊利优酸乳250ml/盒*36/箱），你必须解析。",
                "",
                "==============================",
                "【P1.2 级｜行有效性规则（不丢行）】",
                "==============================",
                "",
                "- 如果某一行商品名称为空，但其他列有内容：仍输出该行，name 置为空字符串，note 写明\"商品名缺失\"。",
                "- 如果某一行明显是表头重复/小计/合计/空行：",
                "  - 允许跳过\"完全空行\"",
                "  - 但若含商品相关文本则不得跳过（宁可输出并在 note 标注\"疑似非订单行\"）",
                "",
                "==============================",
                "【P1.5 级｜包装规格与大包装结构解析（必须执行）】",
                "==============================",
                "",
                "当\"商品名称\"或\"规格/单位\"字段中包含包装结构时，你必须识别并拆解以下字段：",
                "",
                "- standardWeight：规格重量（如 250ml、1.9L、5L、500g、10kg）",
                "- itemUnit：最小包装单位（如 盒、瓶、袋、听、包、杯）",
                "- itemsPerCarton：每个大包装中包含的小包装数量（如 36、24、12）",
                "- cartonUnit：大包装单位（如 箱、件、提、包）",
                "",
                "【典型结构模式（只允许按字形与结构匹配，原样提取，禁止猜测）】",
                "",
                "A) 重量/容量 + /小单位 + *数量 + /大单位",
                "   示例：伊利优酸乳250ml/盒*36/箱",
                "   -> standardWeight=\"250ml\"",
                "   -> itemUnit=\"盒\"",
                "   -> itemsPerCarton=\"36\"",
                "   -> cartonUnit=\"箱\"",
                "",
                "B) 重量/容量 + 小单位 + *数量 + 大单位（斜杠可能缺失）",
                "   示例：250ml盒*36箱",
                "",
                "C) 乘号的多种 OCR/文本形式必须识别为包装结构：",
                "   \"*\", \"x\", \"X\", \"×\", \"乘\"",
                "",
                "【名称清洗规则（必须执行）】",
                "- 输出 name 时，必须尽量为\"纯商品名\"，将包装结构从名称中剥离：",
                "  原：伊利优酸乳250ml/盒*36/箱",
                "  name：伊利优酸乳",
                "【与下单数量的关系（重要）】",
                "- quantity/spec 表示\"本行订货数量与订货单位\"，不得被包装结构覆盖。",
                "- 包装结构 ≠ 下单数量。",
                "- 若本行没有明确的订货数量（只有商品名+包装结构），仍输出条目，quantity/spec 允许为空，但包装字段必须提取。",
                "",
                "【无法识别时】",
                "- 若不满足上述结构模式，standardWeight/itemUnit/itemsPerCarton/cartonUnit 全部输出空字符串。",
                "- 严禁根据常识推测\"36/箱\"之类的值。",
                        "// 【P1.6 级｜商品名称清洗规则（数据库搜索专用｜必须执行）】\n" +
                        "// ==============================\n" +
                        "\"【一】括号内容处理规则（强制）\",\n" +
                        "\"\",\n" +
                        "\"1. name 字段中【绝对不允许】出现任何括号及括号内内容。\",\n" +
                        "\"   包括但不限于：() （） [] 【】。\",\n" +
                        "\"\",\n" +
                        "\"2. 业务约束（不可忽略）：\",\n" +
                        "\"   - name 字段将用于数据库商品精确搜索；\",\n" +
                        "\"   - 括号内容会导致搜索失败，必须清洗。\",\n" +
                        "\"\",\n" +
                        "\"示例：\",\n" +
                        "\"- name = \\\"香蕉(进口)\\\"\",\n" +
                        "\"  -> name = \\\"香蕉\\\"\",\n" +
                        "\"\",\n" +
                        "\"- name = \\\"柠檬(进口) Lemon\\\"\",\n" +
                        "\"  -> note = \\\"进口\\\"\",\n" +
                        "\"\",\n" +
                        "\"\",\n" +
                        "\"【二】英文内容去留判定规则（必须判断）\",\n" +
                        "\"\",\n" +
                        "\"当商品名称中包含英文或英文字母时，你必须判断其业务含义，仅允许以下两种情况之一：\",\n" +
                        "\"\",\n" +
                        "\"A) 英文仅为中文翻译 / 说明（必须删除）\",\n" +
                        "\"\",\n" +
                        "\"满足以下任一条件时：\",\n" +
                        "\"- 英文与中文语义一致，仅为翻译；\",\n" +
                        "\"- 英文是类别或说明性词汇（如 Vegetable / Fruit / Fungus / Lettuce 等）；\",\n" +
                        "\"- 删除英文不会影响商品在数据库中的唯一性。\",\n" +
                        "\"\",\n" +
                        "\"处理规则：\",\n" +
                        "\"- 英文必须从 name 中删除；\",\n" +
                        "\"- 不要求写入 note（除非英文本身有额外业务含义）。\",\n" +
                        "\"\",\n" +
                        "\"示例：\",\n" +
                        "\"- \\\"黄瓜 Cucumber\\\" -> name = \\\"黄瓜\\\"\",\n" +
                        "\"- \\\"彩椒 Vegetable\\\" -> name = \\\"彩椒\\\"\",\n" +
                        "\"- \\\"杏鲍菇 Fungus\\\" -> name = \\\"杏鲍菇\\\"\",\n" +
                        "\"- \\\"罗马生菜 Lettuce\\\" -> name = \\\"罗马生菜\\\"\",\n" +
                        "\"\",\n" +
                        "\"\",\n" +
                        "\"B) 英文 / 字母代表型号 / 等级 / 系列 / 业务区分（必须保留）\",\n" +
                        "\"\",\n" +
                        "\"满足以下任一条件时：\",\n" +
                        "\"- 英文用于区分型号、等级、系列、版本；\",\n" +
                        "\"- 删除英文会导致商品无法唯一识别；\",\n" +
                        "\"- 英文不是翻译，而是商品业务属性的一部分。\",\n" +
                        "\"\",\n" +
                        "\"处理规则：\",\n" +
                        "\"- 英文必须保留在 name 中。\",\n" +
                        "\"\",\n" +
                        "\"示例：\",\n" +
                        "\"- \\\"V9酸奶\\\" -> name = \\\"V9酸奶\\\"\",\n" +
                        "\"- \\\"牛奶 UHT\\\" -> name = \\\"牛奶UHT\\\"\",\n" +
                        "\"- \\\"A果苹果\\\" -> name = \\\"A果苹果\\\"\",\n" +
                        "\"- \\\"AB级牛肉\\\" -> name = \\\"AB级牛肉\\\"\",\n" +
                "",
                "==============================",
                "【P2 级｜字段输出规范（与 OCR 输出对齐）】",
                "==============================",
                "",
                "data[] 中每条订单对象必须包含字段：",
                "",
                "- name：最终下单商品名（尽量为纯商品名）",
                "- quantity：订货数量（字符串）",
                "- spec：订货单位（斤/把/箱/件/袋/瓶...）",
                "- standardWeight：规格重量（字符串）",
                "- itemUnit：最小包装单位（字符串）",
                "- itemsPerCarton：每箱小包装数量（字符串）",
                "- cartonUnit：大包装单位（字符串）",
                "- note：备注（只放 Excel 原始备注或额外说明；不输出系统推理）",
                "",
                "补充规则：",
                "- 如果\"数量列\"里是 15斤 / 6箱 这种：拆分 quantity=\"15\" spec=\"斤\"",
                "- 如果 spec 列里出现 \"桶(1.9L)\" 这种：spec=\"桶\"，standardWeight=\"1.9L\"",
                "- 如果备注列不存在：note 输出空字符串",
                "",
                "==============================",
                "【P0 级｜JSON 返回格式要求（必须严格遵守）】",
                "==============================",
                "",
                "你必须返回标准 JSON 对象，格式如下（字段名必须一致）：",
                "",
                "{",
                "  \"columnMapping\": {",
                "    \"商品名称\": \"A\",",
                "    \"订货数量\": \"B\",",
                "    \"订货规格\": \"C\",",
                "    \"备注\": \"D\"",
                "  },",
                "  \"data\": [",
                "    {",
                "      \"name\": \"\",",
                "      \"quantity\": \"\",",
                "      \"spec\": \"\",",
                "      \"standardWeight\": \"\",",
                "      \"itemUnit\": \"\",",
                "      \"itemsPerCarton\": \"\",",
                "      \"cartonUnit\": \"\",",
                "      \"note\": \"\"",
                "    }",
                "  ]",
                "}",
                "",
                "要求：",
                "- columnMapping 必须给出列字母（A/B/C...），如果某字段找不到，对应值输出空字符串 \"\"。",
                "- data 数组必须包含所有有效订单行。",
                "- 只输出 JSON，不要输出任何其他文字。"
            );
        }

        // 如果有部门特定的修正规则，添加到 prompt 中（优先级高于通用规则）
        if (departmentPrompt != null && !departmentPrompt.trim().isEmpty()) {
            prompt += String.join("\n",
                    "",
                    "==============================",
                    "【P-1 级｜部门特定修正规则（最高优先级，必须严格遵守）】",
                    "==============================",
                    "",
                    "以下规则来自该部门用户的历史修正要求，**优先级高于上述所有通用规则**。",
                    "请严格按照以下规则执行：",
                    "",
                    departmentPrompt.trim(),
                    "",
                    "=============================="
            );
        }

        // 添加系统消息
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        String finalPrompt = prompt;
        systemMessage.put("content", finalPrompt);
        messages.put(systemMessage);

        // 打印完整的 prompt 内容（用于调试）
        logger.info("[callDeepSeekAPIForExcelOrder] ========== 发送给 DeepSeek 的完整 Prompt ==========");
        logger.info("[callDeepSeekAPIForExcelOrder] Prompt 总长度: {} 字符", finalPrompt.length());
        if (departmentPrompt != null && !departmentPrompt.trim().isEmpty()) {
            logger.info("[callDeepSeekAPIForExcelOrder] 包含部门特定修正规则: 是");
        } else {
            logger.info("[callDeepSeekAPIForExcelOrder] 包含部门特定修正规则: 否");
        }
        logger.debug("[callDeepSeekAPIForExcelOrder] 完整 Prompt 内容:\n{}", finalPrompt);
        logger.info("[callDeepSeekAPIForExcelOrder] ====================================================");

        // 添加用户消息
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", "下面是 Excel 表格内容：\n<<<\n" + excelText + "\n>>>");
        messages.put(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.2);
        requestBody.put("stream", false); // 禁用流式响应，避免读取响应实体时长时间等待

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
                String content = message.getString("content");

                logger.info("[DeepSeek ExcelOrder] 提取到的内容长度: {} 字符", content.length());

                // 提取 JSON 部分（去除可能的 markdown 代码块标记）
                // 与图片上传的处理方式保持一致
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

                logger.info("[DeepSeek ExcelOrder] 清理后的内容长度: {} 字符", content.length());
                logger.debug("[DeepSeek ExcelOrder] 清理后的内容: {}", content);

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


//    private String callDeepSeekAPIForExcelOrder(String excelText, String departmentPrompt) throws IOException {
//        // 创建带超时设置的 HTTP 客户端
//        org.apache.http.client.config.RequestConfig requestConfig = org.apache.http.client.config.RequestConfig.custom()
//                .setConnectTimeout(30000)      // 连接超时 30 秒
//                .setSocketTimeout(120000)       // Socket 超时 120 秒
//                .setConnectionRequestTimeout(30000) // 从连接池获取连接超时 30 秒
//                .build();
//
//        CloseableHttpClient client = HttpClients.custom()
//                .setDefaultRequestConfig(requestConfig)
//                .build();
//        HttpPost httpPost = new HttpPost(deepSeekApiUrl);
//
//        // 构建请求体
//        JSONObject requestBody = new JSONObject();
//        requestBody.put("model", "deepseek-chat");
//
//        JSONArray messages = new JSONArray();
//
//        // 从数据库读取系统 prompt，如果不存在则使用默认值
//        String systemPromptContent = nxPromptService.getPromptContentByKey("OCR_EXCEL");
//        String prompt;
//        if (systemPromptContent != null && !systemPromptContent.trim().isEmpty()) {
//            prompt = systemPromptContent;
//            logger.info("[callDeepSeekAPIForExcelOrder] 使用数据库中的系统 prompt (OCR_EXCEL)");
//        } else {
//            // 如果数据库中没有，使用默认的硬编码 prompt（向后兼容）
//            logger.warn("[callDeepSeekAPIForExcelOrder] 数据库中未找到 OCR_EXCEL prompt，使用默认 prompt");
//            // 构建系统提示词（根据用户提供的详细 prompt）
//            prompt = String.join("\n",
//                    "你是一个【生鲜订单 Excel（CSV）解析引擎】。",
//                    "输入为 CSV 格式的 Excel 表格文本（第一行通常是表头）。你的输出将直接用于真实下单，请严格遵守以下规则。",
//                    "",
//                    "==============================",
//                    "【P0 级｜绝对禁止规则（最高优先级）】",
//                    "==============================",
//                    "",
//                    "1) 你【绝对不允许】因为不确定、列名混乱、内容不规范等原因，丢弃任何可能的订单行。",
//                    "2) Excel 数据被视为\"人工输入/导出的结构化数据\"，你【不需要 OCR 纠错】，也【不需要 rawName】。",
//                    "3) 你必须只输出 JSON，不得输出任何解释、分析、注释文字。",
//                    "",
//                    "==============================",
//                    "【P1 级｜输入与识别任务】",
//                    "==============================",
//                    "",
//                    "- 输入是 CSV 文本：用逗号分隔列；第一行通常是表头。",
//                    "- 你要做两件事：",
//                    "  A) 识别列映射 columnMapping：哪一列是商品名称、订货数量、订货单位/规格、备注（可能不存在）。",
//                    "  B) 将每一行转换为结构化订单条目 data[]。",
//                    "",
//                    "==============================",
//                    "【P1.1 级｜列映射规则】",
//                    "==============================",
//                    "",
//                    "你需要尽量识别以下字段对应的列（列字母从左到右 A、B、C、D...）：",
//                    "",
//                    "- 商品名称列（必选）：可能的表头关键词：",
//                    "  \"商品\", \"商品名称\", \"品名\", \"名称\", \"物料\", \"货品\", \"货名\"",
//                    "",
//                    "- 订货数量列（尽量识别）：可能关键词：",
//                    "  \"数量\", \"订货数量\", \"下单数量\", \"订购数量\", \"要货\", \"件数\", \"数\"",
//                    "",
//                    "- 订货单位/规格列（尽量识别）：可能关键词：",
//                    "  \"单位\", \"规格\", \"订货规格\", \"订货单位\", \"计量单位\", \"规格单位\"",
//                    "",
//                    "- 备注列（可选）：可能关键词：",
//                    "  \"备注\", \"说明\", \"要求\", \"备注信息\"",
//                    "",
//                    "注意：",
//                    "- 有些表会把\"数量+单位\"合在一列，例如 \"15斤\"、\"6箱\"。你必须能识别这种情况，并拆分 quantity/spec。",
//                    "- 有些表会把包装结构写在\"商品名称列\"或\"规格列\"里（例如：伊利优酸乳250ml/盒*36/箱），你必须解析。",
//                    "",
//                    "==============================",
//                    "【P1.2 级｜行有效性规则（不丢行）】",
//                    "==============================",
//                    "",
//                    "- 如果某一行商品名称为空，但其他列有内容：仍输出该行，name 置为空字符串，note 写明\"商品名缺失\"。",
//                    "- 如果某一行明显是表头重复/小计/合计/空行：",
//                    "  - 允许跳过\"完全空行\"",
//                    "  - 但若含商品相关文本则不得跳过（宁可输出并在 note 标注\"疑似非订单行\"）",
//                    "",
//                    "==============================",
//                    "【P1.5 级｜包装规格与大包装结构解析（必须执行）】",
//                    "==============================",
//                    "",
//                    "当\"商品名称\"或\"规格/单位\"字段中包含包装结构时，你必须识别并拆解以下字段：",
//                    "",
//                    "- standardWeight：规格重量（如 250ml、1.9L、5L、500g、10kg）",
//                    "- itemUnit：最小包装单位（如 盒、瓶、袋、听、包、杯）",
//                    "- itemsPerCarton：每个大包装中包含的小包装数量（如 36、24、12）",
//                    "- cartonUnit：大包装单位（如 箱、件、提、包）",
//                    "",
//                    "【典型结构模式（只允许按字形与结构匹配，原样提取，禁止猜测）】",
//                    "",
//                    "A) 重量/容量 + /小单位 + *数量 + /大单位",
//                    "   示例：伊利优酸乳250ml/盒*36/箱",
//                    "   -> standardWeight=\"250ml\"",
//                    "   -> itemUnit=\"盒\"",
//                    "   -> itemsPerCarton=\"36\"",
//                    "   -> cartonUnit=\"箱\"",
//                    "",
//                    "B) 重量/容量 + 小单位 + *数量 + 大单位（斜杠可能缺失）",
//                    "   示例：250ml盒*36箱",
//                    "",
//                    "C) 乘号的多种 OCR/文本形式必须识别为包装结构：",
//                    "   \"*\", \"x\", \"X\", \"×\", \"乘\"",
//                    "",
//                    "【名称清洗规则（必须执行）】",
//                    "- 输出 name 时，必须尽量为\"纯商品名\"，将包装结构从名称中剥离：",
//                    "  原：伊利优酸乳250ml/盒*36/箱",
//                    "  name：伊利优酸乳",
//                    "【与下单数量的关系（重要）】",
//                    "- quantity/spec 表示\"本行订货数量与订货单位\"，不得被包装结构覆盖。",
//                    "- 包装结构 ≠ 下单数量。",
//                    "- 若本行没有明确的订货数量（只有商品名+包装结构），仍输出条目，quantity/spec 允许为空，但包装字段必须提取。",
//                    "",
//                    "【无法识别时】",
//                    "- 若不满足上述结构模式，standardWeight/itemUnit/itemsPerCarton/cartonUnit 全部输出空字符串。",
//                    "- 严禁根据常识推测\"36/箱\"之类的值。",
//                    "// 【P1.6 级｜商品名称清洗规则（数据库搜索专用｜必须执行）】\n" +
//                            "// ==============================\n" +
//                            "\"【一】括号内容处理规则（强制）\",\n" +
//                            "\"\",\n" +
//                            "\"1. name 字段中【绝对不允许】出现任何括号及括号内内容。\",\n" +
//                            "\"   包括但不限于：() （） [] 【】。\",\n" +
//                            "\"\",\n" +
//                            "\"2. 业务约束（不可忽略）：\",\n" +
//                            "\"   - name 字段将用于数据库商品精确搜索；\",\n" +
//                            "\"   - 括号内容会导致搜索失败，必须清洗。\",\n" +
//                            "\"\",\n" +
//                            "\"示例：\",\n" +
//                            "\"- name = \\\"香蕉(进口)\\\"\",\n" +
//                            "\"  -> name = \\\"香蕉\\\"\",\n" +
//                            "\"\",\n" +
//                            "\"- name = \\\"柠檬(进口) Lemon\\\"\",\n" +
//                            "\"  -> note = \\\"进口\\\"\",\n" +
//                            "\"\",\n" +
//                            "\"\",\n" +
//                            "\"【二】英文内容去留判定规则（必须判断）\",\n" +
//                            "\"\",\n" +
//                            "\"当商品名称中包含英文或英文字母时，你必须判断其业务含义，仅允许以下两种情况之一：\",\n" +
//                            "\"\",\n" +
//                            "\"A) 英文仅为中文翻译 / 说明（必须删除）\",\n" +
//                            "\"\",\n" +
//                            "\"满足以下任一条件时：\",\n" +
//                            "\"- 英文与中文语义一致，仅为翻译；\",\n" +
//                            "\"- 英文是类别或说明性词汇（如 Vegetable / Fruit / Fungus / Lettuce 等）；\",\n" +
//                            "\"- 删除英文不会影响商品在数据库中的唯一性。\",\n" +
//                            "\"\",\n" +
//                            "\"处理规则：\",\n" +
//                            "\"- 英文必须从 name 中删除；\",\n" +
//                            "\"- 不要求写入 note（除非英文本身有额外业务含义）。\",\n" +
//                            "\"\",\n" +
//                            "\"示例：\",\n" +
//                            "\"- \\\"黄瓜 Cucumber\\\" -> name = \\\"黄瓜\\\"\",\n" +
//                            "\"- \\\"彩椒 Vegetable\\\" -> name = \\\"彩椒\\\"\",\n" +
//                            "\"- \\\"杏鲍菇 Fungus\\\" -> name = \\\"杏鲍菇\\\"\",\n" +
//                            "\"- \\\"罗马生菜 Lettuce\\\" -> name = \\\"罗马生菜\\\"\",\n" +
//                            "\"\",\n" +
//                            "\"\",\n" +
//                            "\"B) 英文 / 字母代表型号 / 等级 / 系列 / 业务区分（必须保留）\",\n" +
//                            "\"\",\n" +
//                            "\"满足以下任一条件时：\",\n" +
//                            "\"- 英文用于区分型号、等级、系列、版本；\",\n" +
//                            "\"- 删除英文会导致商品无法唯一识别；\",\n" +
//                            "\"- 英文不是翻译，而是商品业务属性的一部分。\",\n" +
//                            "\"\",\n" +
//                            "\"处理规则：\",\n" +
//                            "\"- 英文必须保留在 name 中。\",\n" +
//                            "\"\",\n" +
//                            "\"示例：\",\n" +
//                            "\"- \\\"V9酸奶\\\" -> name = \\\"V9酸奶\\\"\",\n" +
//                            "\"- \\\"牛奶 UHT\\\" -> name = \\\"牛奶UHT\\\"\",\n" +
//                            "\"- \\\"A果苹果\\\" -> name = \\\"A果苹果\\\"\",\n" +
//                            "\"- \\\"AB级牛肉\\\" -> name = \\\"AB级牛肉\\\"\",\n" +
//                            "",
//                    "==============================",
//                    "【P2 级｜字段输出规范（与 OCR 输出对齐）】",
//                    "==============================",
//                    "",
//                    "data[] 中每条订单对象必须包含字段：",
//                    "",
//                    "- name：最终下单商品名（尽量为纯商品名）",
//                    "- quantity：订货数量（字符串）",
//                    "- spec：订货单位（斤/把/箱/件/袋/瓶...）",
//                    "- standardWeight：规格重量（字符串）",
//                    "- itemUnit：最小包装单位（字符串）",
//                    "- itemsPerCarton：每箱小包装数量（字符串）",
//                    "- cartonUnit：大包装单位（字符串）",
//                    "- note：备注（只放 Excel 原始备注或额外说明；不输出系统推理）",
//                    "",
//                    "补充规则：",
//                    "- 如果\"数量列\"里是 15斤 / 6箱 这种：拆分 quantity=\"15\" spec=\"斤\"",
//                    "- 如果 spec 列里出现 \"桶(1.9L)\" 这种：spec=\"桶\"，standardWeight=\"1.9L\"",
//                    "- 如果备注列不存在：note 输出空字符串",
//                    "",
//                    "==============================",
//                    "【P0 级｜JSON 返回格式要求（必须严格遵守）】",
//                    "==============================",
//                    "",
//                    "你必须返回标准 JSON 对象，格式如下（字段名必须一致）：",
//                    "",
//                    "{",
//                    "  \"columnMapping\": {",
//                    "    \"商品名称\": \"A\",",
//                    "    \"订货数量\": \"B\",",
//                    "    \"订货规格\": \"C\",",
//                    "    \"备注\": \"D\"",
//                    "  },",
//                    "  \"data\": [",
//                    "    {",
//                    "      \"name\": \"\",",
//                    "      \"quantity\": \"\",",
//                    "      \"spec\": \"\",",
//                    "      \"standardWeight\": \"\",",
//                    "      \"itemUnit\": \"\",",
//                    "      \"itemsPerCarton\": \"\",",
//                    "      \"cartonUnit\": \"\",",
//                    "      \"note\": \"\"",
//                    "    }",
//                    "  ]",
//                    "}",
//                    "",
//                    "要求：",
//                    "- columnMapping 必须给出列字母（A/B/C...），如果某字段找不到，对应值输出空字符串 \"\"。",
//                    "- data 数组必须包含所有有效订单行。",
//                    "- 只输出 JSON，不要输出任何其他文字。"
//            );
//        }
//
//        // 如果有部门特定的修正规则，添加到 prompt 中（优先级高于通用规则）
//        if (departmentPrompt != null && !departmentPrompt.trim().isEmpty()) {
//            prompt += String.join("\n",
//                    "",
//                    "==============================",
//                    "【P-1 级｜部门特定修正规则（最高优先级，必须严格遵守）】",
//                    "==============================",
//                    "",
//                    "以下规则来自该部门用户的历史修正要求，**优先级高于上述所有通用规则**。",
//                    "请严格按照以下规则执行：",
//                    "",
//                    departmentPrompt.trim(),
//                    "",
//                    "=============================="
//            );
//        }
//
//        // 添加系统消息
//        JSONObject systemMessage = new JSONObject();
//        systemMessage.put("role", "system");
//        String finalPrompt = prompt;
//        systemMessage.put("content", finalPrompt);
//        messages.put(systemMessage);
//
//        // 打印完整的 prompt 内容（用于调试）
//        logger.info("[callDeepSeekAPIForExcelOrder] ========== 发送给 DeepSeek 的完整 Prompt ==========");
//        logger.info("[callDeepSeekAPIForExcelOrder] Prompt 总长度: {} 字符", finalPrompt.length());
//        if (departmentPrompt != null && !departmentPrompt.trim().isEmpty()) {
//            logger.info("[callDeepSeekAPIForExcelOrder] 包含部门特定修正规则: 是");
//        } else {
//            logger.info("[callDeepSeekAPIForExcelOrder] 包含部门特定修正规则: 否");
//        }
//        logger.debug("[callDeepSeekAPIForExcelOrder] 完整 Prompt 内容:\n{}", finalPrompt);
//        logger.info("[callDeepSeekAPIForExcelOrder] ====================================================");
//
//        // 添加用户消息
//        JSONObject userMessage = new JSONObject();
//        userMessage.put("role", "user");
//        userMessage.put("content", "下面是 Excel 表格内容：\n<<<\n" + excelText + "\n>>>");
//        messages.put(userMessage);
//
//        requestBody.put("messages", messages);
//        requestBody.put("temperature", 0.2);
//
//        // 设置请求头
//        StringEntity entity = new StringEntity(requestBody.toString(), "UTF-8");
//        entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
//        httpPost.setEntity(entity);
//        httpPost.setHeader("Content-Type", "application/json");
//        httpPost.setHeader("Authorization", "Bearer " + deepSeekApiKey);
//
//        logger.info("[DeepSeek ExcelOrder] 准备发送请求到 DeepSeek API");
//
//        // 执行请求
//        CloseableHttpResponse response = null;
//        try {
//            response = client.execute(httpPost);
//
//            if (response == null) {
//                throw new IOException("DeepSeek API 返回空响应");
//            }
//
//            int statusCode = response.getStatusLine().getStatusCode();
//            if (statusCode != 200) {
//                String errorContent = EntityUtils.toString(response.getEntity(), "UTF-8");
//                throw new IOException("DeepSeek API 返回错误状态码: " + statusCode + ", 响应: " + errorContent);
//            }
//
//            HttpEntity responseEntity = response.getEntity();
//            String responseContent = EntityUtils.toString(responseEntity, "UTF-8");
//            logger.info("[DeepSeek ExcelOrder] 响应内容: {}", responseContent);
//
//            // 解析响应
//            JSONObject responseJson = new JSONObject(responseContent);
//
//            if (responseJson.has("error")) {
//                JSONObject errorObj = responseJson.getJSONObject("error");
//                String errorMessage = errorObj.optString("message", "未知错误");
//                throw new IOException("DeepSeek API 返回错误: " + errorMessage);
//            }
//
//            JSONArray choices = responseJson.getJSONArray("choices");
//            if (choices.length() > 0) {
//                JSONObject choice = choices.getJSONObject(0);
//                JSONObject message = choice.getJSONObject("message");
//                String content = message.getString("content");
//
//                logger.info("[DeepSeek ExcelOrder] 提取到的内容长度: {} 字符", content.length());
//
//                // 提取 JSON 部分（去除可能的 markdown 代码块标记）
//                // 与图片上传的处理方式保持一致
//                content = content.trim();
//                if (content.startsWith("```json")) {
//                    content = content.substring(7);
//                }
//                if (content.startsWith("```")) {
//                    content = content.substring(3);
//                }
//                if (content.endsWith("```")) {
//                    content = content.substring(0, content.length() - 3);
//                }
//                content = content.trim();
//
//                logger.info("[DeepSeek ExcelOrder] 清理后的内容长度: {} 字符", content.length());
//                logger.debug("[DeepSeek ExcelOrder] 清理后的内容: {}", content);
//
//                EntityUtils.consume(responseEntity);
//                response.close();
//                client.close();
//
//                return content;
//            } else {
//                EntityUtils.consume(responseEntity);
//                response.close();
//                client.close();
//                throw new IOException("DeepSeek API 返回空结果");
//            }
//        } catch (Exception e) {
//            if (response != null) {
//                try {
//                    response.close();
//                } catch (Exception ex) {
//                    // ignore
//                }
//            }
//            try {
//                client.close();
//            } catch (Exception ex) {
//                // ignore
//            }
//            throw new IOException("DeepSeek API 调用失败: " + e.getMessage(), e);
//        }
//    }

    /**
     * 转换单位（将kg、g等转换为中文单位）
     */
    private String convertUnit(String unitStr) {
        if (unitStr == null || unitStr.trim().isEmpty()) {
            return "个";
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
        return "个";
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
        String unit = "个"; // 默认单位
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
    

    private String buildStructuredOrderText(List<List<OcrBox>> rows) {
        if (rows == null || rows.isEmpty()) return "";

        // 1) 估算页面右侧阈值：取所有 box 的 maxX
        float maxCx = 0f;
        for (List<OcrBox> row : rows) {
            for (OcrBox b : row) {
                maxCx = Math.max(maxCx, b.cx);
            }
        }
        // cx 是中心点，不是真实 maxX，但足够做列阈值（你表格右列很靠右）
        float qtyColThresh = maxCx * 0.70f;

        // 2) 遍历每行，找“商品名框”和“数量框”
        //    输出：每个数量框尽量配到一个商品名
        List<String> outLines = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            List<OcrBox> row = rows.get(i);

            List<OcrBox> qtyBoxes = new ArrayList<>();
            List<OcrBox> nameBoxes = new ArrayList<>();

            for (OcrBox b : row) {
                String t = b.text != null ? b.text.trim() : "";
                if (t.isEmpty()) continue;

                // 数量列：纯数字 + 位置靠右
                if (isQtyText(t) && b.cx >= qtyColThresh) {
                    qtyBoxes.add(b);
                    continue;
                }

                // 商品名候选：含中文且不是编码/价格类
                if (isGoodsNameCandidate(t)) {
                    nameBoxes.add(b);
                }
            }

            // 本行有“商品名 + 数量” -> 直接输出（最理想）
            if (!qtyBoxes.isEmpty() && !nameBoxes.isEmpty()) {
                for (OcrBox q : qtyBoxes) {
                    // 选最靠右的商品名（通常商品名在左列，但也可能多个框）
                    OcrBox bestName = nameBoxes.get(0);
                    for (OcrBox nb : nameBoxes) {
                        if (nb.cx > bestName.cx) bestName = nb;
                    }
                    outLines.add(normalizeOcrLine(bestName.text) + " " + normalizeOcrLine(q.text));
                }
                continue;
            }

            // 本行只有商品名（没有数量）-> 先暂存，等待下一行出现数量/价行时匹配
            if (qtyBoxes.isEmpty() && !nameBoxes.isEmpty()) {
                // 只取一个主商品名
                OcrBox bestName = nameBoxes.get(0);
                for (OcrBox nb : nameBoxes) {
                    if (nb.cx > bestName.cx) bestName = nb;
                }

                // lookahead：向下找1~2行内的“右侧数量”
                String foundQty = "";
                for (int k = 1; k <= 2 && (i + k) < rows.size(); k++) {
                    for (OcrBox b2 : rows.get(i + k)) {
                        String t2 = b2.text != null ? b2.text.trim() : "";
                        if (t2.isEmpty()) continue;
                        if (isQtyText(t2) && b2.cx >= qtyColThresh) {
                            // 但要确保这两行不是另外一个商品名行（避免跨商品错配）
                            boolean nextHasGoodsName = false;
                            for (OcrBox chk : rows.get(i + k)) {
                                if (isGoodsNameCandidate(chk.text)) { nextHasGoodsName = true; break; }
                            }
                            // 如果同一行已经出现新商品名，就不要用它的数量
                            if (!nextHasGoodsName) {
                                foundQty = t2;
                                break;
                            }
                        }
                    }
                    if (!foundQty.isEmpty()) break;
                }

                if (!foundQty.isEmpty()) {
                    outLines.add(normalizeOcrLine(bestName.text) + " " + normalizeOcrLine(foundQty));
                } else {
                    // 找不到数量，也输出商品名（不丢信息）
                    outLines.add(normalizeOcrLine(bestName.text));
                }
                continue;
            }

            // 本行是编码/价格等噪声：不输出给 DeepSeek（但你可以另存 meta）
            // if (row里全是 isPriceLike/isCodeLike/isQtyText) -> ignore
        }

        // 3) 最终拼成文本
        StringBuilder sb = new StringBuilder();
        sb.append("以下是 OCR 识别到的订单文本，已按行组织：\n\n");
        for (String line : outLines) {
            String cleaned = normalizeOcrLine(line);
            if (cleaned.isEmpty()) continue;
            sb.append(cleaned).append("\n");
        }
        return sb.toString().trim();
    }

    // 右侧数量框：纯数字或数字小数
    private boolean isQtyText(String t) {
        if (t == null) return false;
        String s = t.trim();
        return s.matches("^\\d+(?:\\.\\d+)?$");
    }

    // 明显价格/进价行
    private boolean isPriceLike(String t) {
        if (t == null) return false;
        String s = t.trim();
        return s.contains("最后进价") || s.contains("最后进") || s.startsWith("价:") || s.contains("单价") || s.contains("金额");
    }

    // 明显编码行（你这种 0 开头 6位以上非常典型：0162030、0140201、0130002...）
    private boolean isCodeLike(String t) {
        if (t == null) return false;
        String s = t.trim();
        // 纯编码
        if (s.matches("^0\\d{5,}$")) return true;
        // 编码 + (斤/500g) 或 (箱/Case) 之类
        if (s.matches("^0\\d{5,}.*\\(.*\\).*$")) return true;
        // 你 normalize 后可能变成 "0162030 (箱Case) 最后进"
        if (s.matches("^0\\d{5,}.*$") && (s.contains("箱") || s.contains("斤") || s.contains("Case") || s.contains("Bag"))) {
            return true;
        }
        return false;
    }

    // 可能是商品名：必须包含中文，且不是价格/编码
    private boolean isGoodsNameCandidate(String t) {
        if (t == null) return false;
        String s = t.trim();
        if (!s.matches(".*[\\u4e00-\\u9fa5].*")) return false;
        if (isPriceLike(s)) return false;
        if (isCodeLike(s)) return false;
        return true;
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
        order.setNxDoCollaborativeNxDisId(-1);
        
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
        
        // 0) 只要包含中文，默认不是无关文本（避免误杀中英混合商品名）
        if (trimmedText.matches(".*[\\u4e00-\\u9fa5].*")) {
            return false;
        }

        // 1) 过滤纯符号行（不包含任何字母/中文/数字）
        // 例如：---、|、====、****、() 等
        if (trimmedText.matches("^[\\p{Punct}\\s]+$")) {
            return true;
        }
        if (trimmedText.matches("^-{2,}$")) {
            return true;
        }
        
        // 2) 过滤单字母（如 "B"）——这个保留
        if (trimmedText.matches("^[A-Za-z]$")) {
            return true;
        }
        
        // 3) 过滤日历/页码相关文本（保留）
        if (upperText.matches(".*\\b(MON|TUE|WED|THU|FRI|SAT|SUN|MO|TU|WE|TH|FR|SA|SU)\\b.*")) {
            return true;
        }
        if (upperText.matches(".*\\b\\d{1,2}/\\d{1,2}\\b.*")) {
            return true;
        }
        if (upperText.matches(".*(PAGE|页|第\\s*\\d+\\s*页).*")) {
            return true;
        }
        
        return false;
    }

//    private boolean isIrrelevantText(String text) {
//        if (text == null || text.trim().isEmpty()) {
//            return true;
//        }
//
//        String trimmedText = text.trim();
//        String upperText = trimmedText.toUpperCase();
//
//        // 1. 过滤纯符号行（不包含任何中文和数字）
//        if (trimmedText.matches("^-{1,}$")) {
//            return true; // 纯横线，如 "--"
//        }
//        if (trimmedText.matches("^[^\\u4e00-\\u9fa5\\d]+$")) {
//            // 不包含中文和数字的纯符号行（如 "|"、"---" 等）
//            return true;
//        }
//
//        // 2. 过滤单字母（如 "B"）
//        if (trimmedText.matches("^[A-Za-z]$")) {
//            return true;
//        }
//
//        // 3. 过滤日历相关文本
//        if (upperText.matches(".*(MON|TUE|WED|THU|FRI|SAT|SUN|MO|TU|WE|TH|FR|SA|SU).*")) {
//            return true; // 星期
//        }
//        if (upperText.matches(".*\\d{1,2}/\\d{1,2}.*")) {
//            return true; // 日期格式
//        }
//        if (upperText.matches(".*(PAGE|页|第.*页).*")) {
//            return true; // 页码
//        }
//
//        // 4. 注意：不再过滤单字符中文和短数字
//        // 单字符中文（如 "好"、"红"、"蜂"）可能是商品名的误识别，让后续解析和 needConfirm 机制处理
//        // 短数字（如 "29"、"17"）是商品数量，需要在聚类中与商品名配对
//
//        return false;
//    }
    
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
        
        // 常见误识别：1h / 15h （手写"个"容易被识别成 h）
        // 先在这里归一化：把 h 当斤
        t = t.replaceAll("(?i)h$", "个");
        
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
        
        // 2. h -> 斤（手写"个"容易被识别成 h，已在 isPureQuantityToken 中处理，这里也统一处理）
        normalized = normalized.replaceAll("(\\d+(\\.\\d+)?)h", "$1个");
        normalized = normalized.replaceAll("(\\d+(\\.\\d+)?)H", "$1个");
        
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
     * 订单朗读接口：将订单文本转换为语音
     * 支持批量合成，每条商品生成一个音频文件
     * 
     * @param request 请求参数，包含：
     *                - orderItems: List<Map<String, Object>> 订单商品列表，每个商品包含 name, qty, unit 等字段
     *                - sessionId: String (可选) 会话ID，用于标识一次朗读请求
     * @return 返回音频文件URL列表，每个商品对应一个音频URL
     */
    @RequestMapping(value = "/textToSpeech", method = RequestMethod.POST)
    @ResponseBody
    public R textToSpeech(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        long methodStartTime = System.currentTimeMillis();
        logger.info("[textToSpeech] ========== 开始订单语音合成 ==========");
        
        try {
            // 获取订单商品列表
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> orderItems = (List<Map<String, Object>>) request.get("orderItems");
            String sessionId = (String) request.get("sessionId");
        
            // 获取分页参数（可选，用于批量处理优化）
            Integer startIndex = null;
            Integer limit = null;
            Object startIndexObj = request.get("startIndex");
            Object limitObj = request.get("limit");
            if (startIndexObj != null) {
                if (startIndexObj instanceof Number) {
                    startIndex = ((Number) startIndexObj).intValue();
                } else if (startIndexObj instanceof String) {
                    try {
                        startIndex = Integer.parseInt((String) startIndexObj);
                    } catch (NumberFormatException e) {
                        logger.warn("[textToSpeech] startIndex 格式错误: {}", startIndexObj);
                    }
                }
            }
            if (limitObj != null) {
                if (limitObj instanceof Number) {
                    limit = ((Number) limitObj).intValue();
                } else if (limitObj instanceof String) {
                    try {
                        limit = Integer.parseInt((String) limitObj);
                    } catch (NumberFormatException e) {
                        logger.warn("[textToSpeech] limit 格式错误: {}", limitObj);
                    }
                }
            }
            
            if (orderItems == null || orderItems.isEmpty()) {
                logger.warn("[textToSpeech] 订单商品列表为空");
                return R.error("订单商品列表不能为空");
            }
            
            // 生成会话ID（如果未提供）
            if (sessionId == null || sessionId.trim().isEmpty()) {
                sessionId = "tts_" + System.currentTimeMillis();
            }
            
            // 确定处理范围
            int totalCount = orderItems.size();
            int actualStartIndex = 0;
            int actualEndIndex = totalCount;
            boolean isBatchMode = (startIndex != null && limit != null && startIndex >= 0 && limit > 0);
            
            if (isBatchMode) {
                // 批量处理模式：前端已经筛选了需要处理的订单项，直接处理 orderItems 中的所有项
                // 但序号需要基于 startIndex 来计算
                actualStartIndex = 0; // 从 orderItems 的第一个开始
                actualEndIndex = totalCount; // 处理 orderItems 中的所有项
                logger.info("[textToSpeech] 批量处理模式 - 收到订单项数量: {}, 起始索引: {}, 限制数量: {}, 将处理所有收到的订单项", 
                        totalCount, startIndex, limit);
            } else {
                logger.info("[textToSpeech] 全量处理模式 - 订单商品数量: {}, sessionId: {}", totalCount, sessionId);
            }
            
            // 获取音频保存目录（自动适配开发和生产环境）
            String audioDir = getAudioDirectory();
            File audioDirFile = new File(audioDir);
            if (!audioDirFile.exists()) {
                boolean created = audioDirFile.mkdirs();
                if (!created) {
                    logger.error("[textToSpeech] 创建音频目录失败: {}", audioDir);
                    return R.error("创建音频目录失败: " + audioDir);
                }
                logger.info("[textToSpeech] 创建音频目录: {}", audioDir);
            }
            
            // 构建基础URL（用于返回音频文件URL）
            String baseUrl = buildBaseUrl(httpRequest);
            
            // 判断是否为开发环境（如果路径包含项目根目录，说明是开发环境）
            boolean isDevEnvironment = audioDir.contains(System.getProperty("user.dir"));
            
            // 批量合成语音
            List<Map<String, Object>> audioResults = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;
            
            // 只处理指定范围的数据
            for (int i = actualStartIndex; i < actualEndIndex; i++) {
                // 计算实际序号：批量模式下使用 startIndex + i，否则使用 i
                int actualIndex = isBatchMode ? (startIndex + i) : i;
                Map<String, Object> item = orderItems.get(i);
                try {
                    
                    // 构建朗读文本（格式：商品名称，数量单位，备注）
                    String goodsName = getFieldValue(item, "name", "goodsName");
                    String qty = getFieldValue(item, "qty", "quantity");
                    String unit = getFieldValue(item, "unit", "");
                    String spec = getFieldValue(item, "spec", ""); // 规格字段
                    String remark = getFieldValue(item, "remark", "note"); // 备注字段
                    
                    // 处理数量：如果是纯数字，转换为中文数字（口语化，2读作"两"）
//                    String qtyText = convertNumberToChinese(qty);
                    
                    // 构建朗读文本（格式：第X条，商品名称，数量单位，备注）
                    // 注意：序号基于实际索引 actualIndex
                    StringBuilder textBuilder = new StringBuilder();
                    // 添加序号：第X条
                    textBuilder.append("第").append(actualIndex + 1).append("条");
                    textBuilder.append(goodsName);
                    if (qty != null && !qty.isEmpty()) {
                        textBuilder.append(qty);
                    }
                    // 优先使用 unit，如果 unit 为空则使用 spec
                    String actualUnit = (unit != null && !unit.isEmpty()) ? unit : spec;
                    if (actualUnit != null && !actualUnit.isEmpty()) {
                        textBuilder.append(actualUnit);
                    }
                    // 如果有备注，添加到朗读文本中
                    if (remark != null && !remark.trim().isEmpty() && !remark.equals("null")) {
                        textBuilder.append(remark.trim());
                    }
                    textBuilder.append("。");
                    
                    // 检查是否缺少数量或规格
                    boolean isQtyMissing = (qty == null || qty.trim().isEmpty());
                    boolean isSpecMissing = ((unit == null || unit.trim().isEmpty()) && (spec == null || spec.trim().isEmpty()));
                    
                    // 如果缺少数量或规格，在朗读内容后面增加提示
                    if (isQtyMissing || isSpecMissing) {
                        textBuilder.append("*****，前先确定数量和规格");
                    }
                    
                    String textToSpeak = textBuilder.toString();
                    logger.info("[textToSpeech] 商品[{}] 朗读文本: {}", actualIndex + 1, textToSpeak);
                    
                    // 调用腾讯云 TTS API
                    String audioBase64 = callTencentCloudTTS(textToSpeak, sessionId + "_" + actualIndex);
                    
                    if (audioBase64 == null || audioBase64.isEmpty()) {
                        logger.warn("[textToSpeech] 商品[{}] TTS 返回为空", actualIndex + 1);
                        failCount++;
                        continue;
                    }
                    
                    // 保存音频文件
                    String fileName = sessionId + "_" + actualIndex + ".mp3";
                    String filePath = audioDir + fileName;
                    File audioFile = new File(filePath);
                    
                    // 解码 Base64 并保存
                    byte[] audioBytes = Base64.getDecoder().decode(audioBase64);
                    try (FileOutputStream fos = new FileOutputStream(audioFile)) {
                        fos.write(audioBytes);
                        fos.flush();
                    }
                    
                    logger.info("[textToSpeech] 商品[{}] 音频文件保存成功: {}, 大小: {} bytes", 
                            actualIndex + 1, filePath, audioBytes.length);
                    
                    // 构建音频URL
                    // 开发环境下，如果文件保存在项目根目录，URL 路径保持不变（spring-mvc.xml 已配置）
                    // 生产环境下，使用标准路径
                    String audioUrl = baseUrl + "/ttsAudio/" + fileName;
                    
                    // 构建返回结果
                    Map<String, Object> audioResult = new HashMap<>();
                    audioResult.put("index", actualIndex);
                    audioResult.put("indexText", "第" + (actualIndex + 1) + "条"); // 序号文本
                    audioResult.put("goodsName", goodsName);
                    audioResult.put("qty", qty);
                    audioResult.put("unit", unit);
                    audioResult.put("remark", remark != null ? remark : ""); // 备注字段
                    audioResult.put("text", textToSpeak);
                    audioResult.put("audioUrl", audioUrl);
                    audioResult.put("fileName", fileName);
                    audioResults.add(audioResult);
                    
                    successCount++;
                    
                } catch (Exception e) {
                    logger.error("[textToSpeech] 商品[{}] 语音合成失败: {}", actualIndex + 1, e.getMessage(), e);
                    failCount++;
                }
            }
            
            long methodTotalTime = System.currentTimeMillis() - methodStartTime;
            logger.info("[textToSpeech] ========== 订单语音合成完成 ==========");
            logger.info("[textToSpeech] 成功: {}, 失败: {}, 总耗时: {} ms ({} 秒)", 
                    successCount, failCount, methodTotalTime, methodTotalTime / 1000.0);
            logger.info("[textToSpeech] ======================================");
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("sessionId", sessionId);
            result.put("totalCount", totalCount);
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("audioList", audioResults);
            
            // 如果是批量处理模式，添加分页信息和处理后的订单项
            if (isBatchMode) {
                result.put("startIndex", startIndex);
                result.put("limit", limit);
                // 返回处理后的订单项（只包含本次处理的项）
                List<Map<String, Object>> processedOrderItems = new ArrayList<>();
                for (int i = actualStartIndex; i < actualEndIndex; i++) {
                    processedOrderItems.add(orderItems.get(i));
                }
                result.put("orderItems", processedOrderItems);
            }
            
            return R.ok().put("data", result);
            
        } catch (Exception e) {
            long methodTotalTime = System.currentTimeMillis() - methodStartTime;
            logger.error("[textToSpeech] ========== 订单语音合成失败 ==========");
            logger.error("[textToSpeech] 方法总耗时: {} ms ({} 秒)", methodTotalTime, methodTotalTime / 1000.0);
            logger.error("[textToSpeech] 错误信息: {}", e.getMessage(), e);
            logger.error("[textToSpeech] ====================================");
            return R.error("语音合成失败: " + e.getMessage());
        }
    }
    
    /**
     * 调用腾讯云 TTS API 进行语音合成
     * 
     * @param text 要合成的文本
     * @param sessionId 会话ID
     * @return Base64 编码的音频数据
     */
    private String callTencentCloudTTS(String text, String sessionId) throws TencentCloudSDKException {
        logger.info("[callTencentCloudTTS] 开始调用腾讯云 TTS API，文本长度: {}, sessionId: {}", text.length(), sessionId);
        
        // 实例化认证对象
        Credential cred = new Credential(secretId, secretKey);
        
        // 实例化 HTTP 选项
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("tts.tencentcloudapi.com");
        httpProfile.setConnTimeout(30);
        httpProfile.setReadTimeout(30);
        
        // 实例化客户端配置对象
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        clientProfile.setSignMethod("TC3-HMAC-SHA256");
        
        // 实例化 TTS 客户端
        TtsClient client = new TtsClient(cred, region, clientProfile);
        
        // 实例化请求对象
        TextToVoiceRequest req = new TextToVoiceRequest();
        req.setText(text);
        req.setSessionId(sessionId);
        req.setModelType(2L); // 1-基础音色，2-精品音色
        req.setVolume(0.0f); // 音量，范围[-10, 10]，默认0
        req.setSpeed(1.5f); // 语速，范围[-2, 2]，默认0
        req.setProjectId(0L);
        req.setVoiceType(1001L); // 音色类型，1001-智逍遥（亲和）
        req.setPrimaryLanguage(1L); // 主语言类型，1-中文
        req.setSampleRate(16000L); // 采样率，16000
        req.setCodec("mp3"); // 音频格式，mp3
        
        // 调用 API
        long startTime = System.currentTimeMillis();
        TextToVoiceResponse resp = client.TextToVoice(req);
        long elapsedTime = System.currentTimeMillis() - startTime;
        
        String audioBase64 = resp.getAudio();
        logger.info("[callTencentCloudTTS] 腾讯云 TTS API 调用成功，耗时: {} ms, 音频 Base64 长度: {}", 
                elapsedTime, audioBase64 != null ? audioBase64.length() : 0);
        
        return audioBase64;
    }
    
    /**
     * 将数字转换为中文数字（简化版，支持 1-99）
     * 
     * @param numberStr 数字字符串
     * @return 中文数字字符串
     */
    private String convertNumberToChinese(String numberStr) {
        if (numberStr == null || numberStr.trim().isEmpty()) {
            return "";
        }
        
        // 移除所有非数字字符
        String cleanNumber = numberStr.replaceAll("[^0-9.]", "");
        if (cleanNumber.isEmpty()) {
            return numberStr; // 如果没有数字，返回原字符串
        }
        
        try {
            // 尝试解析为数字
            double num = Double.parseDouble(cleanNumber);
            
            // 如果是整数
            if (num == (int) num) {
                return numberToChinese((int) num);
            } else {
                // 小数，保留两位小数
                return String.format("%.2f", num);
            }
        } catch (NumberFormatException e) {
            // 解析失败，返回原字符串
            return numberStr;
        }
    }
    
    /**
     * 将整数转换为中文数字（口语化，支持 0-99）
     * 注意：数字2在口语中读作"两"而不是"二"
     */
    private String numberToChinese(int num) {
        if (num == 0) {
            return "零";
        }
        
        // 口语化数字：2读作"两"，其他数字正常
        String[] digits = {"", "一", "两", "三", "四", "五", "六", "七", "八", "九"};
        String[] digitsFormal = {"", "一", "二", "三", "四", "五", "六", "七", "八", "九"}; // 用于十位和百位
        
        if (num < 10) {
            return digits[num];
        } else if (num < 20) {
            if (num == 10) {
                return "十";
            }
            int ones = num % 10;
            // 11-19：十位用"十"，个位用口语化（2读"两"）
            return "十" + digits[ones];
        } else if (num < 100) {
            int tens = num / 10;
            int ones = num % 10;
            if (ones == 0) {
                // 20, 30, 40...：十位用正式数字（二、三、四...），后面加"十"
                return digitsFormal[tens] + "十";
            }
            // 21-99：十位用正式数字，个位用口语化（2读"两"）
            return digitsFormal[tens] + "十" + digits[ones];
        } else {
            // 超过99，直接返回数字字符串
            return String.valueOf(num);
        }
    }
    
    /**
     * 获取音频保存目录（自动适配开发和生产环境）
     * 优先级：
     * 1. 配置文件中的路径（生产环境）
     * 2. 项目根目录下的 ttsAudio 文件夹（开发环境）
     * 3. 用户主目录下的临时目录（备用方案）
     * 
     * @return 音频目录路径
     */
    private String getAudioDirectory() {
        // 1. 尝试使用配置文件中的路径（生产环境）
        if (externalImagesPath != null && !externalImagesPath.trim().isEmpty()) {
            // 移除 file:// 前缀（如果存在）
            String cleanPath = externalImagesPath.replace("file://", "").trim();
            if (!cleanPath.endsWith("/")) {
                cleanPath += "/";
            }
            String productionPath = cleanPath + "ttsAudio/";
            
            File prodDir = new File(productionPath);
            // 检查父目录是否存在（如果父目录存在，说明可能是生产环境）
            File parentDir = prodDir.getParentFile();
            if (parentDir != null && parentDir.exists()) {
                logger.info("[getAudioDirectory] 使用生产环境路径: {}", productionPath);
                return productionPath;
            }
        }
        
        // 2. 尝试使用项目根目录（开发环境）
        // 获取项目根目录（通过类路径推断）
        String projectRoot = System.getProperty("user.dir");
        if (projectRoot != null) {
            String devPath = projectRoot + "/ttsAudio/";
            File devDir = new File(devPath);
            // 尝试创建目录，如果成功则使用
            if (devDir.exists() || devDir.mkdirs()) {
                logger.info("[getAudioDirectory] 使用开发环境路径: {}", devPath);
                return devPath;
            }
        }
        
        // 3. 备用方案：使用用户主目录下的临时目录
        String userHome = System.getProperty("user.home");
        String fallbackPath = userHome + "/nongxinle_ttsAudio/";
        logger.info("[getAudioDirectory] 使用备用路径: {}", fallbackPath);
        return fallbackPath;
    }
    
    /**
     * 构建基础URL（协议 + 域名 + 端口 + 上下文路径）
     */
    private String buildBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme(); // http 或 https
        String serverName = request.getServerName(); // 服务器名称或IP
        int serverPort = request.getServerPort(); // 端口号
        String contextPath = request.getContextPath(); // 上下文路径
        
        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(scheme).append("://").append(serverName);
        
        // 如果是标准端口（80或443），不需要添加端口号
        if ((scheme.equals("http") && serverPort != 80) || 
            (scheme.equals("https") && serverPort != 443)) {
            baseUrl.append(":").append(serverPort);
        }
        
        baseUrl.append(contextPath);
        
        return baseUrl.toString();
    }
    
    /**
     * 从请求Map中获取Integer类型参数（简化参数解析）
     * 
     * @param request 请求Map
     * @param paramName 参数名
     * @param required 是否必填
     * @return Integer值，如果参数不存在且非必填则返回null，如果必填但不存在则抛出异常
     */
    private Integer getIntegerParam(Map<String, Object> request, String paramName, boolean required) {
        Object value = request.get(paramName);
        if (value == null) {
            if (required) {
                throw new IllegalArgumentException("参数 " + paramName + " 不能为空");
            }
            return null;
        }
        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            } else {
                return Integer.parseInt(value.toString());
            }
        } catch (Exception e) {
            if (required) {
                throw new IllegalArgumentException("参数 " + paramName + " 格式错误: " + e.getMessage());
            }
            logger.warn("[getIntegerParam] 解析参数 {} 失败: {}", paramName, e.getMessage());
            return null;
        }
    }

    /**
     * 获取OCR图片保存目录（自动适配开发和生产环境）
     * 优先级：
     * 1. 配置文件中的路径（生产环境）
     * 2. 项目根目录下的 ocrImages 文件夹（开发环境）
     * 3. 用户主目录下的临时目录（备用方案）
     * 
     * @return OCR图片目录路径
     */
    private String getOcrImageDirectory() {
        // 1. 尝试使用配置文件中的路径（生产环境）
        if (externalImagesPath != null && !externalImagesPath.trim().isEmpty()) {
            // 移除 file:// 前缀（如果存在）
            String cleanPath = externalImagesPath.replace("file://", "").trim();
            if (!cleanPath.endsWith("/")) {
                cleanPath += "/";
            }
            String productionPath = cleanPath + "ocrImages/";
            
            File prodDir = new File(productionPath);
            // 检查父目录是否存在（如果父目录存在，说明可能是生产环境）
            File parentDir = prodDir.getParentFile();
            if (parentDir != null && parentDir.exists()) {
                logger.info("[getOcrImageDirectory] 使用生产环境路径: {}", productionPath);
                return productionPath;
            }
        }
        
        // 2. 尝试使用项目根目录（开发环境）
        // 获取项目根目录（通过类路径推断）
        String projectRoot = System.getProperty("user.dir");
        if (projectRoot != null) {
            String devPath = projectRoot + "/ocrImages/";
            File devDir = new File(devPath);
            // 尝试创建目录，如果成功则使用
            if (devDir.exists() || devDir.mkdirs()) {
                logger.info("[getOcrImageDirectory] 使用开发环境路径: {}", devPath);
                return devPath;
            }
        }
        
        // 3. 备用方案：使用用户主目录下的临时目录
        String userHome = System.getProperty("user.home");
        String fallbackPath = userHome + "/nongxinle_ocrImages/";
        logger.info("[getOcrImageDirectory] 使用备用路径: {}", fallbackPath);
        return fallbackPath;
    }

    /**
     * 保存Base64图片到服务器
     * 
     * @param imageBase64 Base64编码的图片字符串
     * @param taskId OCR任务ID
     * @return 图片相对路径（如：ocrImages/20250124/xxx.jpg）
     */
    private String saveBase64Image(String imageBase64, Integer taskId) {
        try {
            // 解码Base64
            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
            
            // 获取OCR图片目录（自动适配开发和生产环境）
            String ocrImageBaseDir = getOcrImageDirectory();
            
            // 创建目录：ocrImages/yyyyMMdd/
            String dateDir = formatWhatDay(0).replace("-", ""); // 格式：20250124
            String fullDir = ocrImageBaseDir + dateDir;
            
            File dir = new File(fullDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    throw new RuntimeException("无法创建目录: " + fullDir);
                }
            }
            
            // 生成文件名：{taskId}_{timestamp}.jpg
            String fileName = taskId + "_" + System.currentTimeMillis() + ".jpg";
            String filePath = fullDir + "/" + fileName;
            File imageFile = new File(filePath);
            
            // 保存文件
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                fos.write(imageBytes);
                fos.flush();
            }
            
            logger.info("[saveBase64Image] 图片保存成功，路径: {}", filePath);
            
            // 返回相对路径（用于数据库存储，前端访问时使用 /ocrImages/ 路径）
            String relativePath = "ocrImages/" + dateDir + "/" + fileName;
            return relativePath;
            
        } catch (Exception e) {
            logger.error("[saveBase64Image] 保存图片失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存图片失败: " + e.getMessage(), e);
        }
    }
}
    




