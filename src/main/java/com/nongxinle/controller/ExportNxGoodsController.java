package com.nongxinle.controller;

import com.nongxinle.entity.NxGoodsEntity;
import com.nongxinle.service.NxGoodsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 导出 nxGoods 商品分类库供 AI 训练
 */
@RestController
@RequestMapping("api/export")
public class ExportNxGoodsController {
    private static final Logger logger = LoggerFactory.getLogger(ExportNxGoodsController.class);

    @Autowired
    private NxGoodsService nxGoodsService;

    /**
     * 导出 nxGoods 商品分类库为 CSV 文件
     * 格式：full_path, level0, level1, level2, level3, l2_id
     * 访问地址：GET /api/export/nxGoodsForAI
     */
    @RequestMapping(value = "/nxGoodsForAI", method = RequestMethod.GET)
    public void exportNxGoodsForAI(HttpServletResponse response) {
        PrintWriter writer = null;
        try {
            // 设置响应头
            response.setContentType("text/csv;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            String fileName = "nxGoods_for_AI.csv";
            try {
                fileName = URLEncoder.encode(fileName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.error("文件名编码失败", e);
            }
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.setHeader("Content-Type", "text/csv;charset=UTF-8");

            writer = response.getWriter();

            // 写入 CSV 表头（BOM 标记，确保 Excel 正确识别 UTF-8）
            writer.write('\ufeff');
            writer.println("full_path,level0,level1,level2,level3,l2_id");

            // 查询所有 level=3 的商品（包括隐藏的）
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("level", 3);
            List<NxGoodsEntity> goodsList = nxGoodsService.queryNxGoodsByParams(queryMap);

            logger.info("开始导出商品分类数据，共{}条", goodsList.size());

            int count = 0;
            int errorCount = 0;
            for (NxGoodsEntity goods : goodsList) {
                try {
                    // 获取父级（level=2）
                    NxGoodsEntity father = null;
                    if (goods.getNxGoodsFatherId() != null) {
                        father = nxGoodsService.queryObject(goods.getNxGoodsFatherId());
                    }

                    // 获取祖父级（level=1）
                    NxGoodsEntity grand = null;
                    if (father != null && father.getNxGoodsFatherId() != null) {
                        grand = nxGoodsService.queryObject(father.getNxGoodsFatherId());
                    }

                    // 获取曾祖父级（level=0）
                    NxGoodsEntity greatGrand = null;
                    if (grand != null && grand.getNxGoodsFatherId() != null) {
                        greatGrand = nxGoodsService.queryObject(grand.getNxGoodsFatherId());
                    }

                    // 构建完整路径
                    StringBuilder fullPath = new StringBuilder();
                    if (greatGrand != null && greatGrand.getNxGoodsName() != null) {
                        fullPath.append(greatGrand.getNxGoodsName());
                    }
                    if (grand != null && grand.getNxGoodsName() != null) {
                        if (fullPath.length() > 0) fullPath.append("/");
                        fullPath.append(grand.getNxGoodsName());
                    }
                    if (father != null && father.getNxGoodsName() != null) {
                        if (fullPath.length() > 0) fullPath.append("/");
                        fullPath.append(father.getNxGoodsName());
                    }
                    if (goods.getNxGoodsName() != null) {
                        if (fullPath.length() > 0) fullPath.append("/");
                        fullPath.append(goods.getNxGoodsName());
                    }

                    // 获取各层级名称
                    String level0 = (greatGrand != null && greatGrand.getNxGoodsName() != null) ? greatGrand.getNxGoodsName() : "";
                    String level1 = (grand != null && grand.getNxGoodsName() != null) ? grand.getNxGoodsName() : "";
                    String level2 = (father != null && father.getNxGoodsName() != null) ? father.getNxGoodsName() : "";
                    String level3 = goods.getNxGoodsName() != null ? goods.getNxGoodsName() : "";
                    String l2Id = (father != null && father.getNxGoodsId() != null) ? father.getNxGoodsId().toString() : "";

                    // 写入 CSV 行（处理包含逗号、引号、换行符的情况）
                    writer.print(escapeCsv(fullPath.toString()));
                    writer.print(",");
                    writer.print(escapeCsv(level0));
                    writer.print(",");
                    writer.print(escapeCsv(level1));
                    writer.print(",");
                    writer.print(escapeCsv(level2));
                    writer.print(",");
                    writer.print(escapeCsv(level3));
                    writer.print(",");
                    writer.println(escapeCsv(l2Id));

                    count++;
                    if (count % 500 == 0) {
                        logger.info("已导出{}条数据", count);
                    }
                } catch (Exception e) {
                    errorCount++;
                    logger.error("导出商品失败，商品ID: {}, 错误: {}", goods.getNxGoodsId(), e.getMessage(), e);
                }
            }

            writer.flush();
            logger.info("导出完成，成功: {}条，失败: {}条，总计: {}条", count, errorCount, goodsList.size());
        } catch (IOException e) {
            logger.error("导出 CSV 文件失败", e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * CSV 字段转义处理
     * 如果字段包含逗号、引号或换行符，需要用双引号包裹，并转义内部的双引号
     */
    private String escapeCsv(String field) {
        if (field == null) {
            return "";
        }
        // 如果包含逗号、引号或换行符，需要用双引号包裹
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            // 转义内部的双引号（用两个双引号表示一个双引号）
            field = field.replace("\"", "\"\"");
            return "\"" + field + "\"";
        }
        return field;
    }
}

