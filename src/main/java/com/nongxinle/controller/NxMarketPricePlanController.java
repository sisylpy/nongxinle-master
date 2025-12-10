package com.nongxinle.controller;

import com.nongxinle.entity.NxMarketPricePlanEntity;
import com.nongxinle.service.NxMarketPricePlanService;
import com.nongxinle.utils.Constant;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import com.nongxinle.utils.UploadFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 市场价格方案管理Controller
 * @author lpy
 * @date 2025-01-09
 */
@RestController
@RequestMapping("/api/marketpriceplan")
public class NxMarketPricePlanController {

    @Autowired
    private NxMarketPricePlanService nxMarketPricePlanService;

    /**
     * 分页查询价格方案列表（管理端）
     * @param params 查询参数
     * @return 价格方案列表
     */
    @ResponseBody
    @RequestMapping(value = "/admin/list", method = RequestMethod.GET)
    public R adminList(@RequestParam Map<String, Object> params) {
        // 分页查询
        PageUtils page = nxMarketPricePlanService.queryPage(params);
        return R.ok().put("page", page);
    }

    /**
     * 获取市场价格方案列表（小程序端）
     * @param marketId 市场ID
     * @param type 方案类型（0-流量 1-设备）
     * @return 价格方案列表
     */
    @ResponseBody
    @RequestMapping(value = "/miniprogram/getMarketPlans", method = RequestMethod.GET)
    public R getMarketPlans(@RequestParam("marketId") Integer marketId,
                           @RequestParam(value = "type", required = false) Integer type) {
        try {
            List<NxMarketPricePlanEntity> list;
            if (type != null) {
                list = nxMarketPricePlanService.queryByMarketAndType(marketId, type);
            } else {
                // 查询所有类型
                Map<String, Object> params = new java.util.HashMap<>();
                params.put("marketId", marketId);
                params.put("status", 1); // 只查询启用的
                list = nxMarketPricePlanService.queryList(params);
            }
            return R.ok().put("data", list);
        } catch (Exception e) {
            return R.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取价格方案详情
     * @param id 价格方案ID
     * @return 价格方案详情
     */
    @ResponseBody
    @RequestMapping(value = "/admin/getPlanDetail/{id}", method = RequestMethod.GET)
    public R getPlanDetail(@PathVariable("id") Integer id) {
        NxMarketPricePlanEntity plan = nxMarketPricePlanService.queryObject(id);
        return R.ok().put("data", plan);
    }

    /**
     * 新增价格方案（JSON方式，不带图片）
     * @param plan 价格方案实体
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/admin/addPlan", method = RequestMethod.POST)
    public R addPlan(@RequestBody NxMarketPricePlanEntity plan) {
        try {
            // 参数验证
            if (plan.getNxMppMarketId() == null) {
                return R.error("市场ID不能为空");
            }
            if (plan.getNxMppType() == null) {
                return R.error("方案类型不能为空");
            }
            if (plan.getNxMppPlanName() == null || plan.getNxMppPlanName().trim().isEmpty()) {
                return R.error("方案名称不能为空");
            }
            if (plan.getNxMppPrice() == null) {
                return R.error("价格不能为空");
            }
            
            // 设置默认值
            if (plan.getNxMppStatus() == null) {
                plan.setNxMppStatus(1); // 默认启用
            }
            if (plan.getNxMppSortOrder() == null) {
                plan.setNxMppSortOrder(999); // 默认最后排序
            }
            
            nxMarketPricePlanService.save(plan);
            return R.ok("新增成功");
        } catch (Exception e) {
            return R.error("新增失败：" + e.getMessage());
        }
    }

    /**
     * 新增价格方案（带图片上传，用于设备类型）
     * @param file 图片文件
     * @param nxMppMarketId 市场ID
     * @param nxMppType 方案类型
     * @param nxMppPlanName 方案名称
     * @param nxMppQuantity 数量/规格
     * @param nxMppPrice 价格
     * @param nxMppUnitPrice 单价说明
     * @param nxMppDescription 方案描述
     * @param nxMppSortOrder 排序
     * @param nxMppStatus 状态
     * @param session HttpSession
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/admin/addPlanWithImage", method = RequestMethod.POST)
    public R addPlanWithImage(@RequestParam("file") MultipartFile file,
                              @RequestParam("nxMppMarketId") Integer nxMppMarketId,
                              @RequestParam("nxMppType") Integer nxMppType,
                              @RequestParam("nxMppPlanName") String nxMppPlanName,
                              @RequestParam(value = "nxMppQuantity", required = false) String nxMppQuantity,
                              @RequestParam("nxMppPrice") Integer nxMppPrice,
                              @RequestParam(value = "nxMppUnitPrice", required = false) String nxMppUnitPrice,
                              @RequestParam(value = "nxMppDescription", required = false) String nxMppDescription,
                              @RequestParam(value = "nxMppSortOrder", required = false) Integer nxMppSortOrder,
                              @RequestParam(value = "nxMppStatus", required = false) Integer nxMppStatus,
                              HttpSession session) {
        try {
            // 检查文件是否为空
            if (file == null || file.isEmpty()) {
                return R.error("上传文件不能为空");
            }
            
            System.out.println("======== 开始上传图片 ========");
            System.out.println("文件名: " + file.getOriginalFilename());
            System.out.println("文件大小: " + file.getSize() + " bytes");
            System.out.println("Content-Type: " + file.getContentType());
            
            // 上传图片到服务器
            String uploadDir = "uploadImage";
            String absolutePath = UploadFile.upload(session, uploadDir, file);
            System.out.println("文件上传到: " + absolutePath);
            
            // 获取上传后的文件名（保持原始文件名）
            String filename = file.getOriginalFilename();
            String filePath = uploadDir + "/" + filename;
            
            // 验证文件是否真的存在
            java.io.File uploadedFile = new java.io.File(absolutePath);
            if (!uploadedFile.exists()) {
                System.err.println("警告：文件上传后不存在！路径：" + absolutePath);
                return R.error("文件上传失败，请检查服务器权限");
            }
            System.out.println("文件验证成功，大小: " + uploadedFile.length() + " bytes");

            // 创建实体
            NxMarketPricePlanEntity plan = new NxMarketPricePlanEntity();
            plan.setNxMppMarketId(nxMppMarketId);
            plan.setNxMppType(nxMppType);
            plan.setNxMppPlanName(nxMppPlanName);
            plan.setNxMppQuantity(nxMppQuantity);
            plan.setNxMppPrice(nxMppPrice);
            plan.setNxMppUnitPrice(nxMppUnitPrice);
            plan.setNxMppDescription(nxMppDescription);
            plan.setNxMppImageUrl(filePath);
            plan.setNxMppSortOrder(nxMppSortOrder != null ? nxMppSortOrder : 999);
            plan.setNxMppStatus(nxMppStatus != null ? nxMppStatus : 1);

            nxMarketPricePlanService.save(plan);
            System.out.println("======== 图片上传完成 ========");
            return R.ok("新增成功").put("data", plan);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("新增失败：" + e.getMessage());
        }
    }

    /**
     * 修改价格方案（JSON方式，不带图片）
     * @param plan 价格方案实体
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/admin/editPlan", method = RequestMethod.POST)
    public R editPlan(@RequestBody NxMarketPricePlanEntity plan) {
        try {
            if (plan.getNxMppId() == null) {
                return R.error("价格方案ID不能为空");
            }
            
            nxMarketPricePlanService.update(plan);
            return R.ok("修改成功");
        } catch (Exception e) {
            return R.error("修改失败：" + e.getMessage());
        }
    }

    /**
     * 修改价格方案（带图片上传，用于设备类型）
     * @param file 图片文件
     * @param nxMppId 价格方案ID
     * @param nxMppPlanName 方案名称
     * @param nxMppQuantity 数量/规格
     * @param nxMppPrice 价格
     * @param nxMppUnitPrice 单价说明
     * @param nxMppDescription 方案描述
     * @param nxMppSortOrder 排序
     * @param nxMppStatus 状态
     * @param session HttpSession
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/admin/editPlanWithImage", method = RequestMethod.POST)
    public R editPlanWithImage(@RequestParam("file") MultipartFile file,
                               @RequestParam("nxMppId") Integer nxMppId,
                               @RequestParam(value = "nxMppPlanName", required = false) String nxMppPlanName,
                               @RequestParam(value = "nxMppQuantity", required = false) String nxMppQuantity,
                               @RequestParam(value = "nxMppPrice", required = false) Integer nxMppPrice,
                               @RequestParam(value = "nxMppUnitPrice", required = false) String nxMppUnitPrice,
                               @RequestParam(value = "nxMppDescription", required = false) String nxMppDescription,
                               @RequestParam(value = "nxMppSortOrder", required = false) Integer nxMppSortOrder,
                               @RequestParam(value = "nxMppStatus", required = false) Integer nxMppStatus,
                               HttpSession session) {
        try {
            // 检查文件是否为空
            if (file == null || file.isEmpty()) {
                return R.error("上传文件不能为空");
            }
            
            // 查询原记录
            NxMarketPricePlanEntity plan = nxMarketPricePlanService.queryObject(nxMppId);
            if (plan == null) {
                return R.error("价格方案不存在");
            }

            System.out.println("======== 开始修改价格方案图片 ========");
            System.out.println("方案ID: " + nxMppId);
            System.out.println("文件名: " + file.getOriginalFilename());
            System.out.println("文件大小: " + file.getSize() + " bytes");

            // 删除旧图片
            String oldImageUrl = plan.getNxMppImageUrl();
            if (oldImageUrl != null && !oldImageUrl.trim().isEmpty()) {
                String oldAbsolutePath = Constant.EXTERNAL_IMAGE_DIR + oldImageUrl;
                File oldFile = new File(oldAbsolutePath);
                if (oldFile.exists()) {
                    oldFile.delete();
                    System.out.println("已删除旧图片: " + oldAbsolutePath);
                }
            }

            // 上传新图片到服务器
            String uploadDir = "uploadImage";
            String absolutePath = UploadFile.upload(session, uploadDir, file);
            System.out.println("新图片上传到: " + absolutePath);
            
            // 获取上传后的文件名（保持原始文件名）
            String filename = file.getOriginalFilename();
            String filePath = uploadDir + "/" + filename;
            
            // 验证文件是否真的存在
            java.io.File uploadedFile = new java.io.File(absolutePath);
            if (!uploadedFile.exists()) {
                System.err.println("警告：文件上传后不存在！路径：" + absolutePath);
                return R.error("文件上传失败，请检查服务器权限");
            }
            System.out.println("文件验证成功，大小: " + uploadedFile.length() + " bytes");

            // 更新实体
            plan.setNxMppImageUrl(filePath);
            if (nxMppPlanName != null) plan.setNxMppPlanName(nxMppPlanName);
            if (nxMppQuantity != null) plan.setNxMppQuantity(nxMppQuantity);
            if (nxMppPrice != null) plan.setNxMppPrice(nxMppPrice);
            if (nxMppUnitPrice != null) plan.setNxMppUnitPrice(nxMppUnitPrice);
            if (nxMppDescription != null) plan.setNxMppDescription(nxMppDescription);
            if (nxMppSortOrder != null) plan.setNxMppSortOrder(nxMppSortOrder);
            if (nxMppStatus != null) plan.setNxMppStatus(nxMppStatus);

            nxMarketPricePlanService.update(plan);
            System.out.println("======== 价格方案图片修改完成 ========");
            return R.ok("修改成功").put("data", plan);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("修改失败：" + e.getMessage());
        }
    }

    /**
     * 删除价格方案
     * @param id 价格方案ID
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/admin/deletePlan", method = RequestMethod.POST)
    public R deletePlan(@RequestParam("id") Integer id) {
        try {
            nxMarketPricePlanService.delete(id);
            return R.ok("删除成功");
        } catch (Exception e) {
            return R.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除价格方案
     * @param ids 价格方案ID数组
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/admin/batchDeletePlans", method = RequestMethod.POST)
    public R batchDeletePlans(@RequestBody Integer[] ids) {
        try {
            nxMarketPricePlanService.deleteBatch(ids);
            return R.ok("批量删除成功");
        } catch (Exception e) {
            return R.error("批量删除失败：" + e.getMessage());
        }
    }

    /**
     * 启用/禁用价格方案
     * @param id 价格方案ID
     * @param status 状态（1-启用 0-禁用）
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/admin/togglePlanStatus", method = RequestMethod.POST)
    public R togglePlanStatus(@RequestParam("id") Integer id, @RequestParam("status") Integer status) {
        try {
            NxMarketPricePlanEntity plan = nxMarketPricePlanService.queryObject(id);
            if (plan == null) {
                return R.error("价格方案不存在");
            }
            
            plan.setNxMppStatus(status);
            nxMarketPricePlanService.update(plan);
            
            String statusText = status == 1 ? "启用" : "禁用";
            return R.ok(statusText + "成功");
        } catch (Exception e) {
            return R.error("状态更新失败：" + e.getMessage());
        }
    }

    /**
     * 更新价格方案排序
     * @param id 价格方案ID
     * @param sortOrder 排序值
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/admin/updatePlanSort", method = RequestMethod.POST)
    public R updatePlanSort(@RequestParam("id") Integer id, @RequestParam("sortOrder") Integer sortOrder) {
        try {
            NxMarketPricePlanEntity plan = nxMarketPricePlanService.queryObject(id);
            if (plan == null) {
                return R.error("价格方案不存在");
            }
            
            plan.setNxMppSortOrder(sortOrder);
            nxMarketPricePlanService.update(plan);
            
            return R.ok("排序更新成功");
        } catch (Exception e) {
            return R.error("排序更新失败：" + e.getMessage());
        }
    }

    /**
     * 复制价格方案到其他市场
     * @param sourceMarketId 源市场ID
     * @param targetMarketId 目标市场ID
     * @param type 方案类型（可选）
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/admin/copyPlansToMarket", method = RequestMethod.POST)
    public R copyPlansToMarket(@RequestParam("sourceMarketId") Integer sourceMarketId,
                              @RequestParam("targetMarketId") Integer targetMarketId,
                              @RequestParam(value = "type", required = false) Integer type) {
        try {
            List<NxMarketPricePlanEntity> sourcePlans;
            if (type != null) {
                sourcePlans = nxMarketPricePlanService.queryByMarketAndType(sourceMarketId, type);
            } else {
                Map<String, Object> params = new java.util.HashMap<>();
                params.put("marketId", sourceMarketId);
                params.put("status", 1);
                sourcePlans = nxMarketPricePlanService.queryList(params);
            }
            
            int copyCount = 0;
            for (NxMarketPricePlanEntity sourcePlan : sourcePlans) {
                // 检查目标市场是否已存在相同方案
                List<NxMarketPricePlanEntity> existingPlans = 
                    nxMarketPricePlanService.queryByMarketAndType(targetMarketId, sourcePlan.getNxMppType());
                
                boolean exists = existingPlans.stream()
                    .anyMatch(p -> p.getNxMppPlanName().equals(sourcePlan.getNxMppPlanName()));
                
                if (!exists) {
                    // 创建新方案
                    NxMarketPricePlanEntity newPlan = new NxMarketPricePlanEntity();
                    newPlan.setNxMppMarketId(targetMarketId);
                    newPlan.setNxMppType(sourcePlan.getNxMppType());
                    newPlan.setNxMppPlanName(sourcePlan.getNxMppPlanName());
                    newPlan.setNxMppQuantity(sourcePlan.getNxMppQuantity());
                    newPlan.setNxMppPrice(sourcePlan.getNxMppPrice());
                    newPlan.setNxMppUnitPrice(sourcePlan.getNxMppUnitPrice());
                    newPlan.setNxMppDescription(sourcePlan.getNxMppDescription());
                    newPlan.setNxMppImageUrl(sourcePlan.getNxMppImageUrl());
                    newPlan.setNxMppSortOrder(sourcePlan.getNxMppSortOrder());
                    newPlan.setNxMppStatus(1); // 默认启用
                    
                    nxMarketPricePlanService.save(newPlan);
                    copyCount++;
                }
            }
            
            return R.ok("复制成功，共复制" + copyCount + "个方案");
        } catch (Exception e) {
            return R.error("复制失败：" + e.getMessage());
        }
    }
}
