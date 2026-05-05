package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 10-14 12:03
 */

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.nongxinle.entity.NxDistributerFatherGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsShelfGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsShelfStockEntity;
import com.nongxinle.entity.NxDistributerGoodsShelfStockReduceEntity;
import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.NxDepartmentDisGoodsEntity;
import com.nongxinle.entity.NxDepartmentBillEntity;
import com.nongxinle.service.NxDistributerGoodsService;
import com.nongxinle.service.NxDistributerGoodsShelfGoodsService;
import com.nongxinle.service.NxDistributerGoodsShelfStockService;
import com.nongxinle.service.NxDistributerGoodsShelfStockReduceService;
import com.nongxinle.service.NxDepartmentService;
import com.nongxinle.service.NxDepartmentOrdersService;
import com.nongxinle.service.NxDepartmentDisGoodsService;
import com.nongxinle.service.NxDepartmentBillService;
import com.nongxinle.utils.Constant;
import com.nongxinle.utils.DateUtils;
import com.nongxinle.utils.PinYin4jUtils;
import com.nongxinle.utils.R;
import com.nongxinle.utils.RRException;
import com.nongxinle.utils.UploadFile;


@RestController
@RequestMapping("api/nxdistributergoodsshelfstock")
public class NxDistributerGoodsShelfStockController {
	@Autowired
	private NxDistributerGoodsShelfGoodsService nxDistributerGoodsShelfGoodsService;
	@Autowired
	private NxDistributerGoodsService nxDistributerGoodsService;
	@Autowired
	private NxDistributerGoodsShelfStockService nxDistributerGoodsShelfStockService;
	@Autowired
	private NxDepartmentService nxDepartmentService;
	@Autowired
	private NxDepartmentOrdersService nxDepartmentOrdersService;
	@Autowired
	private NxDepartmentDisGoodsService nxDepartmentDisGoodsService;
	@Autowired
	private NxDistributerGoodsShelfStockReduceService nxDistributerGoodsShelfStockReduceService;
	@Autowired
	private NxDepartmentBillService nxDepartmentBillService;



	@RequestMapping(value = "/updateDisStock", method = RequestMethod.POST, consumes = "application/json")
	@ResponseBody
	public R updateDisStock(@RequestBody Map<String, Object> payload) {
		if (payload == null) {
			return R.error("请求体不能为空");
		}

		String stockIdStr = toStringValue(payload.get("stockId"));
		if (isBlank(stockIdStr)) {
			return R.error("缺少库存批次ID");
		}

		Integer stockId;
		try {
			stockId = parseInteger(stockIdStr, "库存批次ID");
		} catch (IllegalArgumentException ex) {
			return R.error(ex.getMessage());
		}

		NxDistributerGoodsShelfStockEntity oldStockEntity = nxDistributerGoodsShelfStockService.queryObject(stockId);
		if (oldStockEntity == null) {
			return R.error("库存批次不存在");
		}

		String restWeightStr = toStringValue(payload.get("restWeight"));
		if (isBlank(restWeightStr)) {
			return R.error("剩余数量不能为空");
		}

		try {
			BigDecimal restWeight = parseBigDecimal(restWeightStr, "剩余数量", true);
			if (restWeight.compareTo(BigDecimal.ZERO) < 0) {
				return R.error("剩余数量不能为负数");
			}

			BigDecimal sellingPrice = parseBigDecimal(toStringValue(payload.get("sellingPrice")), "建议售价单价", false);
			if (sellingPrice != null && sellingPrice.compareTo(BigDecimal.ZERO) < 0) {
				return R.error("建议售价单价不能为负数");
			}

			BigDecimal purchasePrice = parseBigDecimal(oldStockEntity.getNxDgssPrice(), "采购单价", false);
			BigDecimal restSubtotal = null;
			if (purchasePrice != null) {
				restSubtotal = restWeight.multiply(purchasePrice).setScale(1, RoundingMode.HALF_UP);
			}

			BigDecimal sellingSubtotal = null;
			if (sellingPrice != null) {
				sellingSubtotal = restWeight.multiply(sellingPrice).setScale(1, RoundingMode.HALF_UP);
			}

			oldStockEntity.setNxDgssRestWeight(toPlainString(restWeight));
			oldStockEntity.setNxDgssRestWeightShowStandard(toPlainString(restWeight));
			oldStockEntity.setNxDgssRestSubtotal(restSubtotal == null ? null : toPlainString(restSubtotal));

			if (sellingPrice == null) {
				oldStockEntity.setNxDgssSellingPrice(null);
				oldStockEntity.setNxDgssSellingSubtotal(null);
			} else {
				oldStockEntity.setNxDgssSellingPrice(toPlainString(sellingPrice));
				oldStockEntity.setNxDgssSellingSubtotal(toPlainString(sellingSubtotal));
			}

			if (payload.containsKey("nxDgssProduceDate")) {
				String d = toStringValue(payload.get("nxDgssProduceDate"));
				oldStockEntity.setNxDgssProduceDate(isBlank(d) ? null : d);
			}
			if (payload.containsKey("nxDgssExpiryDate")) {
				String d = toStringValue(payload.get("nxDgssExpiryDate"));
				oldStockEntity.setNxDgssExpiryDate(isBlank(d) ? null : d);
			}
			if (payload.containsKey("nxDgssShelfLife")) {
				String sl = toStringValue(payload.get("nxDgssShelfLife"));
				if (isBlank(sl)) {
					oldStockEntity.setNxDgssShelfLife(null);
				} else {
					oldStockEntity.setNxDgssShelfLife(parseInteger(sl, "保质期"));
				}
			}

			System.out.println("sosuusow"+ oldStockEntity.getNxDgssSellingPrice());
			nxDistributerGoodsShelfStockService.update(oldStockEntity);

			Map<String, Object> response = new HashMap<>();
			response.put("stockId", oldStockEntity.getNxDistributerGoodsShelfStockId());
			response.put("restWeight", oldStockEntity.getNxDgssRestWeight());
			response.put("sellingPrice", oldStockEntity.getNxDgssSellingPrice());
			response.put("nxDgssProduceDate", oldStockEntity.getNxDgssProduceDate());
			response.put("nxDgssExpiryDate", oldStockEntity.getNxDgssExpiryDate());
			response.put("nxDgssShelfLife", oldStockEntity.getNxDgssShelfLife());
			return R.ok().put("data", response);
		} catch (IllegalArgumentException ex) {
			return R.error(ex.getMessage());
		}
	}

	/**
	 * 首衡项目：修改库存批次（包含库存图片和说明字段）- JSON格式（不传文件）
	 */
	@RequestMapping(value = "/updateDisStockSunHola", method = RequestMethod.POST, consumes = "application/json")
	@ResponseBody
	public R updateDisStockSunHola(@RequestBody Map<String, Object> payload) {
		return updateDisStockSunHolaInternal(payload, null, null);
	}

	/**
	 * 首衡项目：修改库存批次（包含库存图片和说明字段）- Multipart格式（传文件）
	 */
	@RequestMapping(value = "/updateDisStockSunHolaWithFile", method = RequestMethod.POST)
	@ResponseBody
	public R updateDisStockSunHolaWithFile(@RequestParam(value = "file", required = false) MultipartFile file,
	                                        @RequestParam("stockId") String stockIdStr,
	                                        @RequestParam("restWeight") String restWeightStr,
	                                        @RequestParam(value = "sellingPrice", required = false) String sellingPriceStr,
	                                        @RequestParam(value = "nxDgssStockRemark", required = false) String nxDgssStockRemark,
	                                        HttpSession session) {
		Map<String, Object> payload = new HashMap<>();
		payload.put("stockId", stockIdStr);
		payload.put("restWeight", restWeightStr);
		payload.put("sellingPrice", sellingPriceStr);
		payload.put("nxDgssStockRemark", nxDgssStockRemark);
		return updateDisStockSunHolaInternal(payload, file, session);
	}

	/**
	 * 内部处理方法：统一处理库存批次更新逻辑
	 */
	private R updateDisStockSunHolaInternal(Map<String, Object> payload, MultipartFile file, HttpSession session) {
		if (payload == null) {
			return R.error("请求体不能为空");
		}

		String stockIdStr = toStringValue(payload.get("stockId"));
		if (isBlank(stockIdStr)) {
			return R.error("缺少库存批次ID");
		}

		Integer stockId;
		try {
			stockId = parseInteger(stockIdStr, "库存批次ID");
		} catch (IllegalArgumentException ex) {
			return R.error(ex.getMessage());
		}

		NxDistributerGoodsShelfStockEntity oldStockEntity = nxDistributerGoodsShelfStockService.queryObject(stockId);
		if (oldStockEntity == null) {
			return R.error("库存批次不存在");
		}

		String restWeightStr = toStringValue(payload.get("restWeight"));
		if (isBlank(restWeightStr)) {
			return R.error("剩余数量不能为空");
		}

		try {
			BigDecimal restWeight = parseBigDecimal(restWeightStr, "剩余数量", true);
			if (restWeight.compareTo(BigDecimal.ZERO) < 0) {
				return R.error("剩余数量不能为负数");
			}

			BigDecimal sellingPrice = parseBigDecimal(toStringValue(payload.get("sellingPrice")), "建议售价单价", false);
			if (sellingPrice != null && sellingPrice.compareTo(BigDecimal.ZERO) < 0) {
				return R.error("建议售价单价不能为负数");
			}

			BigDecimal purchasePrice = parseBigDecimal(oldStockEntity.getNxDgssPrice(), "采购单价", false);
			BigDecimal restSubtotal = null;
			if (purchasePrice != null) {
				restSubtotal = restWeight.multiply(purchasePrice).setScale(1, RoundingMode.HALF_UP);
			}

			BigDecimal sellingSubtotal = null;
			if (sellingPrice != null) {
				sellingSubtotal = restWeight.multiply(sellingPrice).setScale(1, RoundingMode.HALF_UP);
			}

			oldStockEntity.setNxDgssRestWeight(toPlainString(restWeight));
			oldStockEntity.setNxDgssRestWeightShowStandard(toPlainString(restWeight));
			oldStockEntity.setNxDgssRestSubtotal(restSubtotal == null ? null : toPlainString(restSubtotal));

			if (sellingPrice == null) {
				oldStockEntity.setNxDgssSellingPrice(null);
				oldStockEntity.setNxDgssSellingSubtotal(null);
			} else {
				oldStockEntity.setNxDgssSellingPrice(toPlainString(sellingPrice));
				oldStockEntity.setNxDgssSellingSubtotal(toPlainString(sellingSubtotal));
			}

			// 首衡项目：处理库存图片上传
			if (file != null && !file.isEmpty()) {
				// 获取原来的图片路径
				String oldImagePath = oldStockEntity.getNxDgssStockImage();
				
				// 如果原来有图片，先删除旧图片
				if (oldImagePath != null && !oldImagePath.trim().isEmpty()) {
					try {
						String oldAbsolutePath = Constant.EXTERNAL_IMAGE_DIR + oldImagePath;
						File oldFile = new File(oldAbsolutePath);
						if (oldFile.exists()) {
							boolean deleted = oldFile.delete();
							if (deleted) {
								System.out.println("[updateDisStockSunHola] 已删除旧图片: " + oldAbsolutePath);
							} else {
								System.out.println("[updateDisStockSunHola] 删除旧图片失败: " + oldAbsolutePath);
							}
						} else {
							System.out.println("[updateDisStockSunHola] 旧图片文件不存在: " + oldAbsolutePath);
						}
					} catch (Exception e) {
						System.err.println("[updateDisStockSunHola] 删除旧图片时发生异常: " + e.getMessage());
						e.printStackTrace();
					}
				}
				
				// 上传新图片到 stockImages 文件夹
				String newUploadName = "stockImages";
				String originalName = "stock_" + stockId;
				originalName = originalName.replaceAll("[\\\\/:*?\"<>|]", "");
				// 生成文件名，去掉空格和冒号等特殊字符，便于 URL 使用
				String timeStr = DateUtils.formatFullTime().replaceAll("[\\s:-]", "");
				String lastFileName = originalName + timeStr;
				String realPath = UploadFile.uploadFileName(session, newUploadName, file, lastFileName);
				String filePath = newUploadName + "/" + lastFileName + ".jpg";
				
				oldStockEntity.setNxDgssStockImage(filePath);
				System.out.println("[updateDisStockSunHola] 上传新图片: " + filePath);
			} else {
				// 如果没有上传文件，但传入了图片路径字符串，则更新图片路径（用于JSON请求）
				String nxDgssStockImage = toStringValue(payload.get("nxDgssStockImage"));
				if (!isBlank(nxDgssStockImage)) {
					// 处理 HTTP/HTTPS URL：从中提取相对路径
					String normalizedPath = nxDgssStockImage;
					if (normalizedPath.startsWith("http://") || normalizedPath.startsWith("https://")) {
						// 从 HTTP URL 中提取相对路径（查找 stockImages/ 后面的部分）
						if (normalizedPath.contains("stockImages/")) {
							int index = normalizedPath.indexOf("stockImages/");
							normalizedPath = normalizedPath.substring(index);
							System.out.println("[updateDisStockSunHola] 从 HTTP URL 提取相对路径: " + nxDgssStockImage + " -> " + normalizedPath);
						} else {
							System.err.println("[updateDisStockSunHola] HTTP URL 中不包含 stockImages/，忽略: " + nxDgssStockImage);
							normalizedPath = null; // 无效的 URL，忽略
						}
					} else {
						// 如果不是 HTTP URL，确保路径格式正确（应该以 stockImages/ 开头）
						if (!normalizedPath.startsWith("stockImages/")) {
							// 如果不是以 stockImages/ 开头，尝试修正
							if (normalizedPath.contains("stockImages/")) {
								// 如果包含 stockImages/，提取后面的部分
								int index = normalizedPath.indexOf("stockImages/");
								normalizedPath = normalizedPath.substring(index);
							} else {
								// 如果完全不包含，添加 stockImages/ 前缀
								normalizedPath = "stockImages/" + normalizedPath;
							}
							System.out.println("[updateDisStockSunHola] 修正图片路径: " + nxDgssStockImage + " -> " + normalizedPath);
						}
					}
					
					// 如果 normalizedPath 有效，则更新图片路径
					if (normalizedPath != null && !normalizedPath.trim().isEmpty()) {
						// 获取原来的图片路径
						String oldImagePath = oldStockEntity.getNxDgssStockImage();
						
						// 如果新图片和原来的图片不一样，删除原来的图片
						if (oldImagePath != null && !oldImagePath.trim().isEmpty() 
								&& !oldImagePath.equals(normalizedPath)) {
							try {
								// 如果旧路径是 HTTP URL，也需要提取相对路径
								String oldPathForDelete = oldImagePath;
								if (oldPathForDelete.startsWith("http://") || oldPathForDelete.startsWith("https://")) {
									if (oldPathForDelete.contains("stockImages/")) {
										int index = oldPathForDelete.indexOf("stockImages/");
										oldPathForDelete = oldPathForDelete.substring(index);
									} else {
										oldPathForDelete = null; // 无效路径，跳过删除
									}
								}
								
								if (oldPathForDelete != null) {
									String oldAbsolutePath = Constant.EXTERNAL_IMAGE_DIR + oldPathForDelete;
									File oldFile = new File(oldAbsolutePath);
									if (oldFile.exists()) {
										boolean deleted = oldFile.delete();
										if (deleted) {
											System.out.println("[updateDisStockSunHola] 已删除旧图片: " + oldAbsolutePath);
										} else {
											System.out.println("[updateDisStockSunHola] 删除旧图片失败: " + oldAbsolutePath);
										}
									} else {
										System.out.println("[updateDisStockSunHola] 旧图片文件不存在: " + oldAbsolutePath);
									}
								}
							} catch (Exception e) {
								System.err.println("[updateDisStockSunHola] 删除旧图片时发生异常: " + e.getMessage());
								e.printStackTrace();
							}
						}
						
						oldStockEntity.setNxDgssStockImage(normalizedPath);
						System.out.println("[updateDisStockSunHola] 更新库存图片路径: " + normalizedPath);
					}
				}
			}

			// 处理库存说明字段
			String nxDgssStockRemark = toStringValue(payload.get("nxDgssStockRemark"));
			if (!isBlank(nxDgssStockRemark)) {
				oldStockEntity.setNxDgssStockRemark(nxDgssStockRemark);
				System.out.println("[updateDisStockSunHola] 更新库存说明: " + nxDgssStockRemark);
			}

			System.out.println("[updateDisStockSunHola] 更新库存批次，库存ID: " + oldStockEntity.getNxDistributerGoodsShelfStockId());
			//
			if(oldStockEntity.getNxDgssStatus() == -1){
				//把部门的等待积分 减去 库存 subtotal，给部门的积分加 库存subtotal * 0.75,只给他真正返回百分之 75.
				Integer nxDgssNxDepartmentFatherId = oldStockEntity.getNxDgssNxDepartmentFatherId();
				NxDepartmentEntity supplierDepartmentEntity = nxDepartmentService.queryObject(nxDgssNxDepartmentFatherId);
				
				if (supplierDepartmentEntity != null) {
					try {
						// 获取库存金额（批次成本）
						String stockSubtotal = oldStockEntity.getNxDgssSubtotal();
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
							
							// 计算75%的积分（库存金额 * 0.75）
							BigDecimal pointsToAdd = stockSubtotalDecimal.multiply(new BigDecimal("0.75")).setScale(1, BigDecimal.ROUND_HALF_UP);
							
							// 将75%的库存金额加到积分中
							BigDecimal newPoints = currentPointsDecimal.add(pointsToAdd).setScale(1, BigDecimal.ROUND_HALF_UP);
							
							// 更新部门实体
							supplierDepartmentEntity.setNxDepartmentWaitingPoints(newWaitingPoints.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
							supplierDepartmentEntity.setNxDepartmentPoints(newPoints.toString());
							nxDepartmentService.update(supplierDepartmentEntity);
							
							// 保存返还积分和返还积分时间到库存实体
							oldStockEntity.setNxDgssReturnPoints(pointsToAdd.toString());
							oldStockEntity.setNxDgssReturnPointsTime(DateUtils.formatWhatYearDayTime(0)); // 精确到分钟
							
							System.out.println("[updateDisStockSunHola] 更新部门积分: 等待积分 " + currentWaitingPointsDecimal + " - " + stockSubtotalDecimal + " = " + newWaitingPoints 
								+ ", 积分 " + currentPointsDecimal + " + " + pointsToAdd + " = " + newPoints
								+ ", 返还积分时间: " + oldStockEntity.getNxDgssReturnPointsTime());
						}
					} catch (Exception e) {
						// 记录错误但不影响主流程
						System.err.println("[updateDisStockSunHola] 更新部门积分失败: " + e.getMessage());
						e.printStackTrace();
					}
				}

				oldStockEntity.setNxDgssStatus(0);
			}

			nxDistributerGoodsShelfStockService.update(oldStockEntity);

			// 规范化返回的图片路径（确保返回的是相对路径，不是 HTTP URL）
			String responseImagePath = oldStockEntity.getNxDgssStockImage();
			if (responseImagePath != null && !responseImagePath.trim().isEmpty()) {
				if (responseImagePath.startsWith("http://") || responseImagePath.startsWith("https://")) {
					// 如果返回的是 HTTP URL，提取相对路径
					if (responseImagePath.contains("stockImages/")) {
						int index = responseImagePath.indexOf("stockImages/");
						responseImagePath = responseImagePath.substring(index);
						System.out.println("[updateDisStockSunHola] 规范化返回路径: " + oldStockEntity.getNxDgssStockImage() + " -> " + responseImagePath);
					}
				}
			}
			
			Map<String, Object> response = new HashMap<>();
			response.put("stockId", oldStockEntity.getNxDistributerGoodsShelfStockId());
			response.put("restWeight", oldStockEntity.getNxDgssRestWeight());
			response.put("sellingPrice", oldStockEntity.getNxDgssSellingPrice());
			response.put("nxDgssStockImage", responseImagePath);
			response.put("nxDgssStockRemark", oldStockEntity.getNxDgssStockRemark());
			return R.ok().put("data", response);
		} catch (IllegalArgumentException ex) {
			return R.error(ex.getMessage());
		} catch (Exception ex) {
			System.err.println("[updateDisStockSunHola] 处理异常: " + ex.getMessage());
			ex.printStackTrace();
			return R.error("更新库存批次失败: " + ex.getMessage());
		}
	}




	@RequestMapping(value = "/downloadShelfStockTemplate", method = RequestMethod.GET)
	public void downloadShelfStockTemplate(@RequestParam(value = "shelfId", required = false) Integer shelfId,
	                                       HttpServletResponse response) {
		System.out.println("[downloadShelfStockTemplate] 请求下载模板 shelfId=" + shelfId);
		if (shelfId == null) {
			throw new RRException("参数错误，缺少 shelfId");
		}

		Map<String, Object> params = new HashMap<>();
		params.put("shelfId", shelfId);
		List<NxDistributerGoodsShelfGoodsEntity> shelfGoodsList = nxDistributerGoodsShelfGoodsService.queryShelfForGoodsByParams(params);
		System.out.println("[downloadShelfStockTemplate] 货架商品数量=" + (shelfGoodsList == null ? 0 : shelfGoodsList.size()));

		HSSFWorkbook workbook = new HSSFWorkbook();
		try {
			HSSFSheet sheet = workbook.createSheet("库存初始化模板");
			sheet.setDefaultColumnWidth(20);

			HSSFCellStyle headerStyle = workbook.createCellStyle();
			HSSFFont headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerStyle.setFont(headerFont);

			int rowIndex = 0;
			// 提示行
			HSSFRow tipRow = sheet.createRow(rowIndex++);
			tipRow.createCell(0).setCellValue("提示：请填写批次剩余数量（必填），其余信息可根据需要填写。");

			// 表头
			HSSFRow headerRow = sheet.createRow(rowIndex++);
			String[] headers = new String[]{
					"货架商品ID(勿改)",
					"配送商商品ID",
					"商品名称",
					"规格",
					"规格重量",
					"批次剩余数量(必填)",
					"采购单价",
					"建议售价单价",
			};
			for (int i = 0; i < headers.length; i++) {
				HSSFCell cell = headerRow.createCell(i);
				cell.setCellValue(headers[i]);
				cell.setCellStyle(headerStyle);
			}

			if (shelfGoodsList != null && !shelfGoodsList.isEmpty()) {
				for (NxDistributerGoodsShelfGoodsEntity shelfGoods : shelfGoodsList) {
					HSSFRow row = sheet.createRow(rowIndex++);
					int col = 0;
					row.createCell(col++).setCellValue(
							shelfGoods.getNxDistributerGoodsShelfGoodsId() == null
									? ""
									: String.valueOf(shelfGoods.getNxDistributerGoodsShelfGoodsId()));
					row.createCell(col++).setCellValue(
							shelfGoods.getNxDgsgDisGoodsId() == null
									? ""
									: String.valueOf(shelfGoods.getNxDgsgDisGoodsId()));

					NxDistributerGoodsEntity goodsEntity = shelfGoods.getNxDistributerGoodsEntity();
					if (goodsEntity != null) {
						row.createCell(col++).setCellValue(
								goodsEntity.getNxDgGoodsName() == null ? "" : goodsEntity.getNxDgGoodsName());
						row.createCell(col++).setCellValue(
								goodsEntity.getNxDgGoodsStandardname() == null ? "" : goodsEntity.getNxDgGoodsStandardname());
						row.createCell(col++).setCellValue(
								goodsEntity.getNxDgGoodsStandardWeight() == null ? "" : goodsEntity.getNxDgGoodsStandardWeight());
					} else {
						row.createCell(col++).setCellValue("");
						row.createCell(col++).setCellValue("");
						row.createCell(col++).setCellValue("");
					}

					// 预留填写项
					row.createCell(col++).setCellValue("");
					row.createCell(col++).setCellValue("");
					row.createCell(col++).setCellValue("");
					row.createCell(col++).setCellValue("");
					row.createCell(col).setCellValue("");
				}
			} else {
				System.out.println("[downloadShelfStockTemplate] 货架下暂无商品，模板仅包含表头");
			}

			String fileName = "货架库存初始化模板_" + shelfId + ".xls";
			fileName = URLEncoder.encode(fileName, "UTF-8");
			response.setContentType("application/vnd.ms-excel");
			response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
			workbook.write(response.getOutputStream());
			response.flushBuffer();
			System.out.println("[downloadShelfStockTemplate] 模板生成完成，已输出响应");
		} catch (IOException e) {
			System.err.println("[downloadShelfStockTemplate] 生成模板异常: " + e.getMessage());
			throw new RRException("生成模板失败");
		} finally {
			try {
				workbook.close();
			} catch (IOException e) {
				System.err.println("[downloadShelfStockTemplate] 关闭工作簿异常: " + e.getMessage());
			}
		}
	}

	
	@RequestMapping(value = "/uploadShelfStock", method = RequestMethod.POST)
	public R uploadShelfStock(@RequestParam(value = "shelfId", required = false) Integer shelfId,
	                          @RequestParam(value = "file", required = false) MultipartFile file,
	                          @RequestParam(value = "operatorId", required = false) Integer operatorId) {
		System.out.println("[uploadShelfStock] 请求导入库存 shelfId=" + shelfId
				+ ", operatorId=" + operatorId
				+ ", fileName=" + (file == null ? "null" : file.getOriginalFilename()));
		if (shelfId == null) {
			return R.error(-1, "参数错误: 缺少 shelfId");
		}
		if (file == null || file.isEmpty()) {
			return R.error(-1, "文件不能为空");
		}

		DataFormatter formatter = new DataFormatter();
		List<Map<String, Object>> failItems = new ArrayList<>();
		int total = 0;
		int success = 0;

		Workbook workbook = null;
		try {
			workbook = WorkbookFactory.create(file.getInputStream());
			for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
				org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(sheetIndex);
				if (sheet == null) {
					continue;
				}
				int lastRowNum = sheet.getLastRowNum();
				for (int rowIndex = 2; rowIndex <= lastRowNum; rowIndex++) {
					Row row = sheet.getRow(rowIndex);
					if (row == null) {
						continue;
					}
					String shelfGoodsIdStr = getCellString(row, 0, formatter);
					String restWeightStr = getCellString(row, 5, formatter);
					if (isBlank(shelfGoodsIdStr) && isBlank(restWeightStr)) {
						continue;
					}

					total++;
					try {
						Integer shelfGoodsId = parseInteger(shelfGoodsIdStr, "货架商品ID");
						NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity = nxDistributerGoodsShelfGoodsService.queryObject(shelfGoodsId);
						if (shelfGoodsEntity == null) {
							throw new IllegalArgumentException("货架商品不存在");
						}
						if (shelfGoodsEntity.getNxDgsgShelfId() == null || !shelfGoodsEntity.getNxDgsgShelfId().equals(shelfId)) {
							throw new IllegalArgumentException("货架商品不属于当前货架");
						}

						Integer disGoodsId = shelfGoodsEntity.getNxDgsgDisGoodsId();
						if (disGoodsId == null) {
							throw new IllegalArgumentException("缺少配送商商品ID");
						}

						NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsService.queryObject(disGoodsId);
						if (disGoodsEntity == null) {
							throw new IllegalArgumentException("配送商商品不存在");
						}

						BigDecimal restWeight = parseBigDecimal(restWeightStr, "批次剩余数量", true);
						if (restWeight.compareTo(BigDecimal.ZERO) <= 0) {
							throw new IllegalArgumentException("批次剩余数量必须大于 0");
						}

						BigDecimal batchWeight = restWeight;

						BigDecimal purchasePrice = parseBigDecimal(getCellString(row, 6, formatter), "采购单价", false);
						BigDecimal subtotal = null;
						if (purchasePrice != null) {
							subtotal = purchasePrice.multiply(batchWeight).setScale(1, RoundingMode.HALF_UP);
						}

						BigDecimal restSubtotal = null;
						if (purchasePrice != null) {
							restSubtotal = purchasePrice.multiply(restWeight).setScale(1, RoundingMode.HALF_UP);
						}

						BigDecimal sellingPrice = parseBigDecimal(getCellString(row, 7, formatter), "建议售价单价", false);

						String dateCellValue = getCellString(row, 8, formatter);
						Date stockDate = resolveDate(dateCellValue);
						String stockDateStr = DateUtils.format(stockDate);
						String monthStr = new SimpleDateFormat(DateUtils.MONTH_TIME_PATTERN).format(stockDate);
						String yearStr = new SimpleDateFormat(DateUtils.Year_TIME_PATTERN).format(stockDate);
						String weekStr = String.valueOf(DateUtils.getWeekDate(stockDateStr));
						String fullTime = DateUtils.format(stockDate, DateUtils.FULL_TIME_PATTERN);

						NxDistributerGoodsShelfStockEntity stockEntity = new NxDistributerGoodsShelfStockEntity();
						stockEntity.setNxDgssNxDistributerId(disGoodsEntity.getNxDgDistributerId());
						stockEntity.setNxDgssNxDisGoodsId(disGoodsId);
						stockEntity.setNxDgssNxDisGoodsFatherId(disGoodsEntity.getNxDgDfgGoodsFatherId());
						stockEntity.setNxDgssNxShelfGoodsId(shelfGoodsId);
						stockEntity.setNxDgssWeight(toPlainString(batchWeight));
						stockEntity.setNxDgssRestWeight(toPlainString(restWeight));
						stockEntity.setNxDgssRestWeightShowStandard(toPlainString(restWeight));
						stockEntity.setNxDgssRestWeightShowStandardName(disGoodsEntity.getNxDgGoodsStandardname());
						stockEntity.setNxDgssPrice(toPlainString(purchasePrice));
						stockEntity.setNxDgssSubtotal(toPlainString(subtotal));
						stockEntity.setNxDgssRestSubtotal(toPlainString(restSubtotal));
						stockEntity.setNxDgssSellingPrice(toPlainString(sellingPrice));
						stockEntity.setNxDgssStatus(0);
						stockEntity.setNxDgssReceiveUserId(operatorId);
						stockEntity.setNxDgssDate(stockDateStr);
						stockEntity.setNxDgssMonth(monthStr);
						stockEntity.setNxDgssYear(yearStr);
						stockEntity.setNxDgssWeek(weekStr);
						stockEntity.setNxDgssFullTime(fullTime);
						stockEntity.setNxDgssTimeStamp(String.valueOf(System.currentTimeMillis()));
						stockEntity.setNxDgssReturnWeight("0");
						stockEntity.setNxDgssReturnSubtotal("0");
						stockEntity.setNxDgssProduceWeight("0");
						stockEntity.setNxDgssProduceSubtotal("0");
						stockEntity.setNxDgssLossWeight("0");
						stockEntity.setNxDgssLossSubtotal("0");
						stockEntity.setNxDgssWasteWeight("0");
						stockEntity.setNxDgssWasteSubtotal("0");
						stockEntity.setNxDgssProfitSubtotal("0");
						stockEntity.setNxDgssProfitWeight("0");
						stockEntity.setNxDgssSellingSubtotal("0");
						stockEntity.setNxDgssAfterProfitSubtotal("0");
						stockEntity.setNxDgssProduceSellingSubtotal("0");

						nxDistributerGoodsShelfStockService.save(stockEntity);
						success++;
						System.out.println("[uploadShelfStock] 导入成功 sheet=" + sheet.getSheetName()
								+ ", row=" + (rowIndex + 1) + ", shelfGoodsId=" + shelfGoodsId
								+ ", restWeight=" + stockEntity.getNxDgssRestWeight());
					} catch (Exception rowEx) {
						Map<String, Object> fail = new HashMap<>();
						fail.put("sheet", sheet.getSheetName());
						fail.put("row", rowIndex + 1);
						fail.put("shelfGoodsId", shelfGoodsIdStr);
						fail.put("message", rowEx.getMessage());
						failItems.add(fail);
						System.err.println("[uploadShelfStock] 行导入失败 sheet=" + sheet.getSheetName()
								+ ", row=" + (rowIndex + 1) + ", 原因=" + rowEx.getMessage());
					}
				}
			}
		} catch (IOException e) {
			System.err.println("[uploadShelfStock] 读取文件失败: " + e.getMessage());
			throw new RRException("文件读取失败");
		} catch (Exception e) {
			System.err.println("[uploadShelfStock] 解析文件失败: " + e.getMessage());
			throw new RRException("文件格式不支持，请使用模板导出的 Excel");
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					System.err.println("[uploadShelfStock] 关闭工作簿异常: " + e.getMessage());
				}
			}
		}

		System.out.println("[uploadShelfStock] 导入完成 total=" + total + ", success=" + success + ", failed=" + failItems.size());
		Map<String, Object> result = new HashMap<>();
		result.put("total", total);
		result.put("success", success);
		result.put("failed", failItems.size());
		result.put("failItems", failItems);
		return R.ok().put("result", result);
	}

	private String getCellString(Row row, int cellIndex, DataFormatter formatter) {
		if (row == null) {
			return null;
		}
		Cell cell = row.getCell(cellIndex);
		if (cell == null) {
			return null;
		}
		String value = formatter.formatCellValue(cell);
		return value == null ? null : value.trim();
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private String toStringValue(Object value) {
		if (value == null) {
			return null;
		}
		String str = value.toString();
		return str == null ? null : str.trim();
	}

	private Integer parseInteger(String value, String fieldName) {
		if (isBlank(value)) {
			throw new IllegalArgumentException(fieldName + "不能为空");
		}
		try {
			BigDecimal decimal = new BigDecimal(value.trim().replace(",", ""));
			return decimal.intValueExact();
		} catch (NumberFormatException | ArithmeticException e) {
			throw new IllegalArgumentException(fieldName + "格式错误: " + value);
		}
	}

	private BigDecimal parseBigDecimal(String value, String fieldName, boolean required) {
		if (isBlank(value)) {
			if (required) {
				throw new IllegalArgumentException(fieldName + "不能为空");
			}
			return null;
		}
		try {
			String normalized = value.trim().replace(",", "");
			return new BigDecimal(normalized);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(fieldName + "格式错误: " + value);
		}
	}

	private String toPlainString(BigDecimal value) {
		if (value == null) {
			return null;
		}
		return value.stripTrailingZeros().toPlainString();
	}

	private Date resolveDate(String cellValue) {
		if (isBlank(cellValue)) {
			return new Date();
		}
		String normalized = cellValue.trim()
				.replace("年", "-")
				.replace("月", "-")
				.replace("日", "")
				.replace("/", "-")
				.replace(".", "-");
		if (normalized.matches("\\d+(\\.\\d+)?")) {
			try {
				double numericValue = Double.parseDouble(normalized);
				return DateUtil.getJavaDate(numericValue);
			} catch (NumberFormatException ignored) {
				// fall through to pattern parsing
			}
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setLenient(false);
		try {
			return sdf.parse(normalized);
		} catch (ParseException e) {
			throw new IllegalArgumentException("批次日期格式不正确，应为 yyyy-MM-dd");
		}
	}

	/**
	 * 获取门店库存类型周期统计
	 * 参考GB项目的getMendianStockTypePeriod接口
	 * @param disId 批发商ID
	 * @param whichDay 查询天数（0-今天，1-昨天，2-前天，3-3天前，4-4天前，5-超过4天）
	 * @param searchDepIds 部门ID列表（逗号分隔，"-1"表示全部）
	 * @param searchDepId 单个部门ID（"-1"表示全部）
	 * @return 库存统计数据
	 */
	@RequestMapping(value = "/getMendianStockTypePeriod", method = RequestMethod.POST)
	@ResponseBody
	public R getMendianStockTypePeriod(Integer disId, Integer whichDay, String searchDepIds, String searchDepId) {
		Map<String, Object> mapResult = new HashMap<>();

		List<String> idsNx = new ArrayList<>();

		if (searchDepIds != null && !searchDepIds.equals("-1")) {
			String[] arrNx = searchDepIds.split(",");
			for (String idNx : arrNx) {
				idsNx.add(idNx);
			}
		}

		// 保存原始whichDay值，因为后面可能会修改
		Integer originalWhichDay = whichDay;

		//按商品显示
		List<NxDistributerFatherGoodsEntity> greatGrandGoods = new ArrayList<>();
		Map<String, Object> map0 = new HashMap<>();
		map0.put("disId", disId);
		map0.put("status", 0); // NX项目使用status=0表示正常状态
		map0.put("restWeight", 0); // restWeight=0表示只查询有剩余库存的（restWeight > 0）

		Map<String, Object> map0W = new HashMap<>();
		map0W.put("disId", disId);
		map0W.put("status", 0); // NX项目使用status=0表示正常状态
		map0W.put("restWeight", 0);

		//条件判断 1
		if (searchDepId != null && !searchDepId.equals("-1")) {
			map0.put("depFatherId", searchDepId);
			map0W.put("depFatherId", searchDepId);
		} else {
			if (searchDepIds != null && !searchDepIds.equals("-1")) {
				map0.put("depFatherIds", idsNx);
				map0W.put("depFatherIds", idsNx);
			}
		}

		//1，stock 剩余金额总额
		// 【重要】总库存应该始终查询全部数据，不受whichDay影响，所以创建一个专门用于查询总库存的Map
		Map<String, Object> map0ForTotal = new HashMap<>(map0);
		// 确保总库存查询不包含日期条件
		map0ForTotal.remove("date");
		map0ForTotal.remove("stopDate");
		map0ForTotal.remove("startDate");
		Integer integer = nxDistributerGoodsShelfStockService.queryStockGoodsCount(map0ForTotal);
		if (integer > 0) {
			Map<String, Object> mapTotal = new HashMap<>();
			// 【修复】使用map0ForTotal查询总库存，确保查询的是全部数据
			System.out.println("【总库存SQL查询】调用queryShelfStockRestTotal，参数: " + map0ForTotal);
			System.out.println("【总库存SQL查询】参数中的日期条件: " + 
				(map0ForTotal.containsKey("date") ? "date=" + map0ForTotal.get("date") : "无date") + 
				(map0ForTotal.containsKey("stopDate") ? ", stopDate=" + map0ForTotal.get("stopDate") : "") +
				(map0ForTotal.containsKey("startDate") ? ", startDate=" + map0ForTotal.get("startDate") : ""));
			System.out.println("【总库存SQL查询】SQL参数详情 - disId: " + map0ForTotal.get("disId") + 
				", status: " + map0ForTotal.get("status") +
				", restWeight: " + map0ForTotal.get("restWeight"));
			Double aDouble = nxDistributerGoodsShelfStockService.queryShelfStockRestTotal(map0ForTotal);
			System.out.println("【总库存SQL查询】查询到的总库存金额: " + aDouble);
			mapTotal.put("dateString", "全部");
			mapTotal.put("restTotal", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
			mapResult.put("total", mapTotal);

			// 先计算各期间的总额，以便后续根据whichDay选择使用哪个总额
			//IN - 今天入库
			Map<String, Object> mapRenZero = new HashMap<>();
			mapRenZero.put("disId", disId);
			mapRenZero.put("status", 0);
			mapRenZero.put("date", DateUtils.formatWhatDay(0));
			mapRenZero.put("restWeight", 0);
			if (searchDepId != null && !searchDepId.equals("-1")) {
				mapRenZero.put("depFatherId", searchDepId);
			} else {
				if (searchDepIds != null && !searchDepIds.equals("-1")) {
					mapRenZero.put("depFatherIds", idsNx);
				}
			}
			Double zeroTotal = 0.0;
			Integer integerzero = nxDistributerGoodsShelfStockService.queryStockGoodsCount(mapRenZero);
			if (integerzero > 0) {
				zeroTotal = nxDistributerGoodsShelfStockService.queryShelfStockRestTotal(mapRenZero);
			}
			Map<String, Object> mapIn = new HashMap<>();
			String zeroDateString = DateUtils.formatWhatDayString(0);
			mapIn.put("dateString", zeroDateString);
			mapIn.put("zeroTotal", new BigDecimal(zeroTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
			mapResult.put("in", mapIn);

			//ONE - 昨天入库
			Map<String, Object> mapRenOne = new HashMap<>();
			mapRenOne.put("disId", disId);
			mapRenOne.put("status", 0);
			mapRenOne.put("date", DateUtils.formatWhatDay(-1));
			mapRenOne.put("restWeight", 0);
			if (searchDepId != null && !searchDepId.equals("-1")) {
				mapRenOne.put("depFatherId", searchDepId);
			} else {
				if (searchDepIds != null && !searchDepIds.equals("-1")) {
					mapRenOne.put("depFatherIds", idsNx);
				}
			}
			Double oneTotal = 0.0;
			Integer integerone = nxDistributerGoodsShelfStockService.queryStockGoodsCount(mapRenOne);
			if (integerone > 0) {
				oneTotal = nxDistributerGoodsShelfStockService.queryShelfStockRestTotal(mapRenOne);
			}
			Map<String, Object> mapOne = new HashMap<>();
			String oneDateString = DateUtils.formatWhatDayString(-1);
			mapOne.put("dateString", oneDateString);
			mapOne.put("oneTotal", new BigDecimal(oneTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
			mapResult.put("one", mapOne);

			//TWO - 前天入库
			Map<String, Object> mapRenTwo = new HashMap<>();
			mapRenTwo.put("disId", disId);
			mapRenTwo.put("status", 0);
			mapRenTwo.put("date", DateUtils.formatWhatDay(-2));
			mapRenTwo.put("restWeight", 0);
			if (searchDepId != null && !searchDepId.equals("-1")) {
				mapRenTwo.put("depFatherId", searchDepId);
			} else {
				if (searchDepIds != null && !searchDepIds.equals("-1")) {
					mapRenTwo.put("depFatherIds", idsNx);
				}
			}
			Double twoTotal = 0.0;
			Integer integetwo = nxDistributerGoodsShelfStockService.queryStockGoodsCount(mapRenTwo);
			if (integetwo > 0) {
				twoTotal = nxDistributerGoodsShelfStockService.queryShelfStockRestTotal(mapRenTwo);
			}
			Map<String, Object> mapTwo = new HashMap<>();
			String twoDateString = DateUtils.formatWhatDayString(-2);
			mapTwo.put("dateString", twoDateString);
			mapTwo.put("twoTotal", new BigDecimal(twoTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
			mapResult.put("two", mapTwo);

			//THREE - 3天前入库
			Map<String, Object> mapRenThree = new HashMap<>();
			mapRenThree.put("disId", disId);
			mapRenThree.put("status", 0);
			mapRenThree.put("date", DateUtils.formatWhatDay(-3));
			mapRenThree.put("restWeight", 0);
			if (searchDepId != null && !searchDepId.equals("-1")) {
				mapRenThree.put("depFatherId", searchDepId);
			} else {
				if (searchDepIds != null && !searchDepIds.equals("-1")) {
					mapRenThree.put("depFatherIds", idsNx);
				}
			}
			Double threeTotal = 0.0;
			Integer integethree = nxDistributerGoodsShelfStockService.queryStockGoodsCount(mapRenThree);
			if (integethree > 0) {
				threeTotal = nxDistributerGoodsShelfStockService.queryShelfStockRestTotal(mapRenThree);
			}
			Map<String, Object> mapThree = new HashMap<>();
			String threeDateString = DateUtils.formatWhatDayString(-3);
			mapThree.put("dateString", threeDateString);
			mapThree.put("threeTotal", new BigDecimal(threeTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
			mapResult.put("three", mapThree);

			//EXCEED - 超过3天的入库数据
			Map<String, Object> mapRen4 = new HashMap<>();
			mapRen4.put("disId", disId);
			mapRen4.put("status", 0);
			mapRen4.put("stopDate", DateUtils.formatWhatDay(-4));
			mapRen4.put("restWeight", 0);
			if (searchDepId != null && !searchDepId.equals("-1")) {
				mapRen4.put("depFatherId", searchDepId);
			} else {
				if (searchDepIds != null && !searchDepIds.equals("-1")) {
					mapRen4.put("depFatherIds", idsNx);
				}
			}

			Map<String, Object> mapRen4W = new HashMap<>();
			mapRen4W.put("disId", disId);
			mapRen4W.put("status", 0);
			mapRen4W.put("stopDate", DateUtils.formatWhatDay(-4));
			mapRen4W.put("restWeight", 0);
			if (searchDepId != null && !searchDepId.equals("-1")) {
				mapRen4W.put("depFatherId", searchDepId);
			} else {
				if (searchDepIds != null && !searchDepIds.equals("-1")) {
					mapRen4W.put("depFatherIds", idsNx);
				}
			}

			Double exceedThreeTotal = 0.0;
			Integer integer33 = nxDistributerGoodsShelfStockService.queryStockGoodsCount(mapRen4);
			List<NxDistributerFatherGoodsEntity> exceedArr = new ArrayList<>();
			if (integer33 > 0) {
				exceedThreeTotal = nxDistributerGoodsShelfStockService.queryShelfStockRestTotal(mapRen4);
				// 查询exceed期间的商品数据
				exceedArr = getStockGoodsFatherRestSubTotal(mapRen4, exceedThreeTotal, mapRen4W);
			}

			Map<String, Object> mapExceed = new HashMap<>();
			String exceedDateString = DateUtils.formatWhatDayString(-4) + "以前";
			mapExceed.put("dateString", exceedDateString);
			mapExceed.put("exceedThreeTotal", new BigDecimal(exceedThreeTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
			mapResult.put("exceed", mapExceed);
			
			// 添加exceed商品数组
			mapResult.put("exceedArr", exceedArr);
			mapResult.put("processedExceedArr", exceedArr); // processedExceedArr通常与exceedArr相同，或经过额外处理

			//条件判断 2 - 根据whichDay设置查询参数和选择总额
			// whichDay = 99 表示查询全部，不设置日期条件
			// whichDay = 0 表示查询今天的数据
			// whichDay = 1-4 表示查询对应天数前的数据
			Double selectedTotal = aDouble; // 默认使用全部总额
			if (originalWhichDay != null && originalWhichDay >= 0 && originalWhichDay < 5) {
				if (originalWhichDay == 0) {
					// whichDay = 0 时查询今天的数据
					String todayDate = DateUtils.formatWhatDay(0);
					map0.put("date", todayDate);
					map0W.put("date", todayDate);
					selectedTotal = zeroTotal;
				} else {
					// whichDay = 1-4 时查询对应天数前的数据
					// whichDay = 1 表示昨天，应该使用 DateUtils.formatWhatDay(-1)
					// whichDay = 2 表示前天，应该使用 DateUtils.formatWhatDay(-2)
					// 所以 dayOffset = originalWhichDay
					int dayOffset = originalWhichDay;
					String targetDate = DateUtils.formatWhatDay(-dayOffset);
					map0.put("date", targetDate);
					map0W.put("date", targetDate);
					// 根据whichDay选择对应的总额
					if (originalWhichDay == 1) {
						selectedTotal = oneTotal;
					} else if (originalWhichDay == 2) {
						selectedTotal = twoTotal;
					} else if (originalWhichDay == 3) {
						selectedTotal = threeTotal;
					} else if (originalWhichDay == 4) {
						selectedTotal = exceedThreeTotal;
					}
				}
			}
			
			//1，获取商品
			// whichDay = null 或 99 时，查询全部数据（使用全部总额aDouble）
			// whichDay = 0-4 时，查询对应期间的数据（使用对应期间的总额）
			// whichDay >= 5 时，查询超过4天的数据
			if (originalWhichDay == null || originalWhichDay == 99) {
				// 查询全部，使用全部总额，不设置日期条件
				Map<String, Object> map0All = new HashMap<>(map0);
				map0All.remove("date"); // 移除日期条件
				Map<String, Object> map0WAll = new HashMap<>(map0W);
				map0WAll.remove("date"); // 移除日期条件
				greatGrandGoods = getStockGoodsFatherRestSubTotal(map0All, aDouble, map0WAll);
			} else if (originalWhichDay >= 0 && originalWhichDay < 5) {
				// 查询指定期间的数据
				greatGrandGoods = getStockGoodsFatherRestSubTotal(map0, selectedTotal, map0W);
			} else if (originalWhichDay >= 5) {
				greatGrandGoods = queryExceedData(disId, searchDepId, searchDepIds, idsNx);
			} else {
				// 其他情况，使用全部总额
				Map<String, Object> map0All = new HashMap<>(map0);
				map0All.remove("date"); // 移除日期条件
				Map<String, Object> map0WAll = new HashMap<>(map0W);
				map0WAll.remove("date"); // 移除日期条件
				greatGrandGoods = getStockGoodsFatherRestSubTotal(map0All, aDouble, map0WAll);
			}
			mapResult.put("arr", greatGrandGoods);
			
			// 再次确认exceedArr和processedExceedArr仍然存在（防止被覆盖）
			Object exceedArrCheck = mapResult.get("exceedArr");
			if (exceedArrCheck == null) {
				List<NxDistributerFatherGoodsEntity> exceedArrRecheck = new ArrayList<>();
				// 重新查询exceed数据
				Map<String, Object> mapRen4Recheck = new HashMap<>();
				mapRen4Recheck.put("disId", disId);
				mapRen4Recheck.put("status", 0);
				mapRen4Recheck.put("stopDate", DateUtils.formatWhatDay(-4));
				mapRen4Recheck.put("restWeight", 0);
				if (searchDepId != null && !searchDepId.equals("-1")) {
					mapRen4Recheck.put("depFatherId", searchDepId);
				} else {
					if (searchDepIds != null && !searchDepIds.equals("-1")) {
						mapRen4Recheck.put("depFatherIds", idsNx);
					}
				}
				Map<String, Object> mapRen4WRecheck = new HashMap<>(mapRen4Recheck);
				Integer integer33Recheck = nxDistributerGoodsShelfStockService.queryStockGoodsCount(mapRen4Recheck);
				if (integer33Recheck > 0) {
					Double exceedThreeTotalRecheck = nxDistributerGoodsShelfStockService.queryShelfStockRestTotal(mapRen4Recheck);
					exceedArrRecheck = getStockGoodsFatherRestSubTotal(mapRen4Recheck, exceedThreeTotalRecheck, mapRen4WRecheck);
				}
				mapResult.put("exceedArr", exceedArrRecheck);
				mapResult.put("processedExceedArr", exceedArrRecheck);
			}

		} else {
			Map<String, Object> map = new HashMap<>();
			map.put("restTotal", "0");
			mapResult.put("total", map);
			
			// 即使没有库存，也要设置exceedArr和processedExceedArr为空数组，避免前端收到undefined
			List<NxDistributerFatherGoodsEntity> exceedArr = new ArrayList<>();
			mapResult.put("exceedArr", exceedArr);
			mapResult.put("processedExceedArr", exceedArr);
		}
		
		// 查询部门列表
		List<NxDepartmentEntity> list = new ArrayList<>();
		if (idsNx != null && !idsNx.isEmpty()) {
			for (String depId : idsNx) {
				NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(Integer.valueOf(depId));
				if (departmentEntity != null) {
					Map<String, Object> mapDep = new HashMap<>();
					mapDep.put("disId", disId);
					mapDep.put("status", 0);
					mapDep.put("restWeight", 0);
					mapDep.put("depFatherId", departmentEntity.getNxDepartmentId());
					Integer integerDep = nxDistributerGoodsShelfStockService.queryStockGoodsCount(mapDep);
					if (integerDep > 0) {
						Double depTotal = nxDistributerGoodsShelfStockService.queryShelfStockRestTotal(mapDep);
						Map<String, Object> depMap = new HashMap<>();
						depMap.put("depId", departmentEntity.getNxDepartmentId());
						depMap.put("depName", departmentEntity.getNxDepartmentName());
						depMap.put("restTotal", new BigDecimal(depTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
						list.add(departmentEntity);
					}
				}
			}
		}
		mapResult.put("depArr", list);
		
		// 最终确认：确保exceedArr和processedExceedArr一定存在
		if (!mapResult.containsKey("exceedArr")) {
			mapResult.put("exceedArr", new ArrayList<>());
		}
		if (!mapResult.containsKey("processedExceedArr")) {
			mapResult.put("processedExceedArr", new ArrayList<>());
		}
		
		return R.ok().put("data", mapResult);
	}

	/**
	 * 获取库存商品树结构（带剩余金额小计）
	 * @param map0 查询参数
	 * @param total 总金额
	 * @param map0W 废弃查询参数（暂不使用）
	 * @return 商品树结构列表
	 */
	private List<NxDistributerFatherGoodsEntity> getStockGoodsFatherRestSubTotal(Map<String, Object> map0, Double total, Map<String, Object> map0W) {
		// 【修复】创建新的Map对象，避免修改原始map0，确保日期条件不被覆盖
		Map<String, Object> map0Copy = new HashMap<>(map0);
		map0Copy.put("restWeight", 0);
		List<NxDistributerFatherGoodsEntity> stockAndRecordFatherGoodsTreeSet = getStockFatherGoodsTreeSet(map0Copy);
		// 【修复】传递map0Copy而不是map0，确保日期条件不被后续操作覆盖
		List<NxDistributerFatherGoodsEntity> stockFatherGoodsRestSubtotal = getStockFatherGoodsRestSubtotal(stockAndRecordFatherGoodsTreeSet, map0Copy, total);
		return stockFatherGoodsRestSubtotal;
	}

	/**
	 * 获取库存商品树结构
	 * @param map0 查询参数
	 * @return 商品树结构列表
	 */
	private List<NxDistributerFatherGoodsEntity> getStockFatherGoodsTreeSet(Map<String, Object> map0) {
		Integer integerStock = nxDistributerGoodsShelfStockService.queryStockGoodsCount(map0);
		if (integerStock > 0) {
			List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerGoodsShelfStockService.queryStockTreeFatherGoodsByParams(map0);
			return fatherGoodsEntities;
		} else {
			return new ArrayList<>();
		}
	}

	/**
	 * 计算商品树结构的剩余金额小计
	 * @param treeSet 商品树结构
	 * @param map0 查询参数
	 * @param total 总金额
	 * @return 计算后的商品树结构
	 */
	private List<NxDistributerFatherGoodsEntity> getStockFatherGoodsRestSubtotal(
			List<NxDistributerFatherGoodsEntity> treeSet, Map<String, Object> map0, Double total) {

		for (NxDistributerFatherGoodsEntity greatGrandFather : treeSet) {
			List<NxDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
			BigDecimal greatGrandTotal = new BigDecimal(0);

			if (grandGoodsEntities != null) {
				for (NxDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
					BigDecimal stockTotalValue = new BigDecimal(0);
					// 【修复】创建新的Map对象，避免污染原始map0，确保日期条件正确传递
					Map<String, Object> mapForGrand = new HashMap<>(map0);
					mapForGrand.put("disGoodsGrandId", grandFather.getNxDistributerFatherGoodsId());
					Integer integer = nxDistributerGoodsShelfStockService.queryStockGoodsCount(mapForGrand);
					if (integer > 0) {
						System.out.println("【二级分类总额SQL查询】调用queryShelfStockRestTotal，参数: " + mapForGrand);
						System.out.println("【二级分类总额SQL查询】参数中的日期条件: " + 
							(mapForGrand.containsKey("date") ? "date=" + mapForGrand.get("date") : "无date") + 
							(mapForGrand.containsKey("stopDate") ? ", stopDate=" + mapForGrand.get("stopDate") : "") +
							(mapForGrand.containsKey("startDate") ? ", startDate=" + mapForGrand.get("startDate") : ""));
						System.out.println("【二级分类总额SQL查询】SQL参数详情 - disId: " + mapForGrand.get("disId") + 
							", disGoodsGrandId: " + mapForGrand.get("disGoodsGrandId") + 
							", status: " + mapForGrand.get("status") +
							", restWeight: " + mapForGrand.get("restWeight"));
						Double stockTotal = nxDistributerGoodsShelfStockService.queryShelfStockRestTotal(mapForGrand);
						stockTotalValue = new BigDecimal(stockTotal).setScale(1, BigDecimal.ROUND_HALF_UP);
						System.out.println("【二级分类总额SQL查询】二级分类ID: " + grandFather.getNxDistributerFatherGoodsId() + 
							", 查询到的原始总额: " + stockTotal + ", 格式化后: " + stockTotalValue);
						greatGrandTotal = greatGrandTotal.add(stockTotalValue);
					}

					// 清空子节点，避免数据冗余
					grandFather.setFatherGoodsEntities(null);
					// 设置库存金额和商品数量
					grandFather.setFatherStockTotal(stockTotalValue.doubleValue());
					grandFather.setFatherStockTotalString(stockTotalValue.toString());
					grandFather.setFatherStockMany(integer != null ? integer.doubleValue() : 0.0);
					grandFather.setFatherStockManyString(integer != null ? integer.toString() : "0");
				}
		}

			// 查询该一级分类的商品数量
			Map<String, Object> map0ForGreat = new HashMap<>(map0);
			map0ForGreat.put("disGoodsGreatId", greatGrandFather.getNxDistributerFatherGoodsId());
			map0ForGreat.remove("disGoodsGrandId"); // 移除二级分类ID，使用一级分类ID
			Integer greatInteger = nxDistributerGoodsShelfStockService.queryStockGoodsCount(map0ForGreat);

			// 计算百分比
			BigDecimal divide = new BigDecimal(0);
			if (total != null && total > 0) {
				divide = greatGrandTotal.divide(new BigDecimal(total), 2, BigDecimal.ROUND_HALF_UP)
						.multiply(new BigDecimal(100)).setScale(1, BigDecimal.ROUND_HALF_UP);
			}
			// 设置库存金额、百分比和商品数量
			greatGrandFather.setFatherStockTotalPercent(divide.toString());
			greatGrandFather.setFatherStockTotal(greatGrandTotal.doubleValue());
			greatGrandFather.setFatherStockTotalString(greatGrandTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
			greatGrandFather.setFatherStockMany(greatInteger != null ? greatInteger.doubleValue() : 0.0);
			greatGrandFather.setFatherStockManyString(greatInteger != null ? greatInteger.toString() : "0");
		}
		return treeSet;
	}

	/**
	 * 查询超过3天的入库数据
	 * @param disId 批发商ID
	 * @param searchDepId 单个部门ID
	 * @param searchDepIds 部门ID列表
	 * @param idsNx 部门ID数组
	 * @return 商品树结构列表
	 */
	private List<NxDistributerFatherGoodsEntity> queryExceedData(Integer disId, String searchDepId, String searchDepIds, List<String> idsNx) {
		//1,超过3天的入库数据
		Map<String, Object> mapRen4 = new HashMap<>();
		mapRen4.put("disId", disId);
		mapRen4.put("status", 0);
		mapRen4.put("stopDate", DateUtils.formatWhatDay(-4));
		mapRen4.put("restWeight", 0);

		Map<String, Object> mapRen4W = new HashMap<>();
		mapRen4W.put("disId", disId);
		mapRen4W.put("dayuStatus", -1);
		mapRen4W.put("stopDate", DateUtils.formatWhatDay(-4));
		mapRen4W.put("restWeight", 0);

		if (searchDepId != null && !searchDepId.equals("-1")) {
			mapRen4.put("depFatherId", searchDepId);
			mapRen4W.put("depFatherId", searchDepId);
		} else {
			if (searchDepIds != null && !searchDepIds.equals("-1")) {
				mapRen4.put("depFatherIds", idsNx);
				mapRen4W.put("depFatherIds", idsNx);
			}
		}

		System.out.println("超过3天查询参数: " + mapRen4);
		Double exceedThreeTotal = 0.0;
		Integer integer33 = nxDistributerGoodsShelfStockService.queryStockGoodsCount(mapRen4);
		List<NxDistributerFatherGoodsEntity> recentlyStockDayuThree = new ArrayList<>();
		if (integer33 > 0) {
			exceedThreeTotal = nxDistributerGoodsShelfStockService.queryShelfStockRestTotal(mapRen4);
			recentlyStockDayuThree = getStockGoodsFatherRestSubTotal(mapRen4, exceedThreeTotal, mapRen4W);
		}
		return recentlyStockDayuThree;
	}

	/**
	 * 根据商品大类ID查询按时间段分类的库存统计
	 * 参考GB项目的disGetDayStockByGreatId接口
	 * @param disId 批发商ID
	 * @param searchDepId 部门ID（"-1"表示全部）
	 * @param greatId 商品大类ID
	 * @param whichDay 查询天数（0-今天，1-昨天，2-前天，3-3天前，4-4天前，99-全部）
	 * @param type 查询类型（0-按天，1-按周，2-按月）
	 * @return 库存统计数据
	 */
	@RequestMapping(value = "/disGetDayStockByGreatId", method = RequestMethod.POST)
	@ResponseBody
	public R disGetDayStockByGreatId(Integer disId, String searchDepId, String greatId, Integer whichDay, Integer type) {
		System.out.println("=== 开始查询商品库存按时间段分类统计 ===");
		System.out.println("请求参数 - disId: " + disId + ", searchDepId: " + searchDepId + ", greatId: " + greatId + ", whichDay: " + whichDay + ", type: " + type);
		
		// 参数验证和默认值处理
		if (disId == null) {
			return R.error("disId不能为空");
		}
		if (greatId == null || greatId.trim().isEmpty()) {
			return R.error("greatId不能为空");
		}
		
		// 处理whichDay默认值
		if (whichDay == null) {
			whichDay = 99; // 默认查询全部
		}
		
		// 处理type默认值（前端可能传递NaN或其他无效值）
		if (type == null || type < 0 || type > 2) {
			type = 0; // 默认按天查询
		}
		
		System.out.println("处理后的参数 - disId: " + disId + ", greatId: " + greatId + ", whichDay: " + whichDay + ", type: " + type);
		
		Map<String, Object> map = new HashMap<>();

		if (whichDay == 99 || whichDay == 0) {
			map.put("oneDay", disGetStockDayStockByGreatId(disId, greatId, whichDay, type));
		} else {
			map.put("oneDay", disGetStockDayStockByGreatId(disId, greatId, -whichDay, type));
		}

		return R.ok().put("data", map);
	}

	/**
	 * 根据商品大类ID查询库存商品列表
	 * @param disId 批发商ID
	 * @param greatId 商品大类ID
	 * @param which 查询天数偏移量（99-全部，0-今天，-1-昨天，-2-前天，-3-3天前，-4-4天前）
	 * @param type 查询类型（0-按天，1-按周，2-按月）
	 * @return 库存商品列表及统计信息
	 */
	private Map<String, Object> disGetStockDayStockByGreatId(Integer disId, String greatId, Integer which, Integer type) {
		System.out.println("查询商品库存 - disId: " + disId + ", greatId: " + greatId + ", which: " + which + ", type: " + type);
		
		List<NxDistributerGoodsEntity> stockGoodsList = new ArrayList<>();
		Map<String, Object> result = new HashMap<>();
		double total = 0.0;
		Map<String, Object> map = new HashMap<>();
		map.put("disId", disId);
		map.put("disGoodsGreatId", greatId);
		map.put("restWeight", 0);
		map.put("status", 0); // NX项目使用status=0表示正常状态

		if (which != null && which != 99) {
			// 根据type选择不同的日期计算方式
			if (type == null) {
				type = 0; // 默认按天
			}
			
			switch (type) {
				case 0: // 按天
					if (which == -4) {
						map.put("stopDate", DateUtils.formatWhatDay(which));
						System.out.println("按天查询 - stopDate: " + DateUtils.formatWhatDay(which));
					} else {
						// queryGoodsStockList 需要 startDate 和 stopDate，而不是 date
						String dateStr = DateUtils.formatWhatDay(which);
						map.put("startDate", dateStr);
						map.put("stopDate", dateStr);
						// 同时设置 date 用于 queryStockGoodsCount
						map.put("date", dateStr);
						System.out.println("按天查询 - startDate: " + dateStr + ", stopDate: " + dateStr);
					}
					break;
				case 1: // 按周
					if (which == -4) {
						// exceed情况：使用stopDate查询
						String[] weekRange = getDateRange(-4, type);
						map.put("stopDate", weekRange[1]);
						System.out.println("按周查询exceed - stopDate: " + weekRange[1]);
					} else {
						String[] weekRange = getDateRange(which, type);
						map.put("startDate", weekRange[0]);
						map.put("stopDate", weekRange[1]);
						System.out.println("按周查询 - startDate: " + weekRange[0] + ", stopDate: " + weekRange[1]);
					}
					break;
				case 2: // 按月
					if (which == -4) {
						// exceed情况：使用stopDate查询
						String[] monthRange = getDateRange(-4, type);
						map.put("stopDate", monthRange[1]);
						System.out.println("按月查询exceed - stopDate: " + monthRange[1]);
					} else {
						String[] monthRange = getDateRange(which, type);
						map.put("startDate", monthRange[0]);
						map.put("stopDate", monthRange[1]);
						System.out.println("按月查询 - startDate: " + monthRange[0] + ", stopDate: " + monthRange[1]);
					}
					break;
				default:
					// 默认按天处理
					if (which == -4) {
						map.put("stopDate", DateUtils.formatWhatDay(which));
					} else {
						map.put("date", DateUtils.formatWhatDay(which));
					}
					break;
			}
		} else {
			System.out.println("查询全部库存，无日期限制");
		}

		System.out.println("查询参数: " + map);
		Integer integerIn = nxDistributerGoodsShelfStockService.queryStockGoodsCount(map);
		System.out.println("查询到记录数: " + integerIn);
		
		if (integerIn > 0) {
			map.put("orderByGoodsStockTotal", 1);
			System.out.println("查询商品列表参数: " + map);
			stockGoodsList = nxDistributerGoodsShelfStockService.queryGoodsStockList(map);
			total = nxDistributerGoodsShelfStockService.queryShelfStockRestTotal(map);
			
			// 设置每个商品的库存总额字符串格式
			for (NxDistributerGoodsEntity goods : stockGoodsList) {
				if (goods.getGoodsStockTotal() != null) {
					goods.setGoodsStockTotalString(new BigDecimal(goods.getGoodsStockTotal())
						.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
				} else {
					goods.setGoodsStockTotal(0.0);
					goods.setGoodsStockTotalString("0.0");
				}
				if (goods.getGoodsStockWeightTotal() != null) {
					goods.setGoodsStockWeightTotalString(new BigDecimal(goods.getGoodsStockWeightTotal())
						.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
				} else {
					goods.setGoodsStockWeightTotal(0.0);
					goods.setGoodsStockWeightTotalString("0.0");
				}
			}
			
			result.put("arr", stockGoodsList);
			System.out.println("获取到商品数量: " + stockGoodsList.size());
		} else {
			result.put("arr", stockGoodsList);
			System.out.println("无商品数据");
		}

		// 注意：NX项目暂未实现废弃金额查询，如果需要可以后续添加
		// double greatWasteValue = 0.0;
		// Integer wasteGoodsCount = 0;
		// int count = nxDistributerGoodsShelfStockService.queryStockGoodsCount(map);
		// if (count > 0) {
		//     greatWasteValue = nxDistributerGoodsShelfStockService.queryShelfStockWasteTotal(map);
		//     wasteGoodsCount = nxDistributerGoodsShelfStockService.queryStockGoodsCount(map);
		// }
		// result.put("wasteGoodsCount", wasteGoodsCount);
		// result.put("wasteSubtotal", new BigDecimal(greatWasteValue).setScale(1, BigDecimal.ROUND_HALF_UP));
		
		// 根据type设置不同的日期说明
		String dateString = getDateString(which == null ? 99 : which, type == null ? 0 : type, 0);
		result.put("dateString", dateString);
		result.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
		
		System.out.println("查询完成 - 总额: " + total + ", 日期说明: " + dateString);
		return result;
	}

	/**
	 * 根据偏移量和查询类型获取日期范围
	 * @param offset 偏移量（负数表示往前推）
	 * @param type 查询类型（0-按天，1-按周，2-按月）
	 * @return 日期范围数组 [startDate, stopDate]
	 */
	private String[] getDateRange(int offset, Integer type) {
		if (type == null) {
			type = 0;
		}
		
		switch (type) {
			case 0: // 按天
				String date = DateUtils.formatWhatDay(-offset);
				return new String[]{date, date}; // 同一天
			case 1: // 按周（7天周期）
				if (offset == -4) {
					// exceed: 查询3周以前的数据，使用stopDate查询
					return new String[]{null, getWeekStartDate(-3)}; // startDate为null，stopDate为3周前的开始时间
				}
				return new String[]{getWeekStartDate(offset), getWeekStopDate(offset)};
			case 2: // 按月（30天周期）
				if (offset == -4) {
					// exceed: 查询3个月以前的数据，使用stopDate查询
					return new String[]{null, getMonthStartDate(-3)}; // startDate为null，stopDate为3个月前的开始时间
				}
				return new String[]{getMonthStartDate(offset), getMonthStopDate(offset)};
			default:
				throw new IllegalArgumentException("不支持的查询类型: " + type);
		}
	}

	/**
	 * 根据偏移量和查询类型获取汉字说明
	 * @param offset 偏移量
	 * @param type 查询类型（0-按天，1-按周，2-按月）
	 * @param index 索引位置（用于判断是否是exceed）
	 * @return 汉字说明
	 */
	private String getDateString(int offset, Integer type, int index) {
		if (type == null) {
			type = 0;
		}
		
		// exceed 特殊处理
		if (index == 4) {
			switch (type) {
				case 0: return "3天以前";
				case 1: return "3周以前";
				case 2: return "3个月以前";
				default: return "更早";
			}
		}
		
		// 全部查询
		if (offset == 99) {
			switch (type) {
				case 0: return "全部";
				case 1: return "全部（按周）";
				case 2: return "全部（按月）";
				default: return "全部";
			}
		}
		
		switch (type) {
			case 0: // 按天
				switch (offset) {
					case 0: return "今天";
					case -1: return "昨天";
					case -2: return "前天";
					case -3: return "大前天";
					default: return Math.abs(offset) + "天前";
				}
			case 1: // 按周
				switch (offset) {
					case 0: return "本周";
					case -1: return "1周";
					case -2: return "2周";
					case -3: return "3周";
					default: return Math.abs(offset) + "周以前";
				}
			case 2: // 按月
				switch (offset) {
					case 0: return "本月";
					case -1: return "1个月";
					case -2: return "2个月";
					case -3: return "3个月";
					default: return Math.abs(offset) + "个月以前";
				}
			default:
				return "未知";
		}
	}

	/**
	 * 按周查询：7天为一个周期
	 * @param weekOffset 周偏移量（0-本周，-1-1周前，-2-2周前）
	 * @return 周开始日期
	 */
	private String getWeekStartDate(int weekOffset) {
		// weekOffset=0: 今天往前推7天
		// weekOffset=-1: 7天前往前推7天
		Calendar cal = Calendar.getInstance();
		// 修正计算逻辑：weekOffset是负数，需要正确计算往前推的天数
		int daysToSubtract = Math.abs(weekOffset) * 7 + 6;
		cal.add(Calendar.DAY_OF_YEAR, -daysToSubtract);
		String result = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
		System.out.println("计算周开始日期 - weekOffset: " + weekOffset + ", 往前推天数: " + daysToSubtract + ", result: " + result);
		return result;
	}

	/**
	 * 获取周结束日期
	 * @param weekOffset 周偏移量
	 * @return 周结束日期
	 */
	private String getWeekStopDate(int weekOffset) {
		// weekOffset=0: 今天
		// weekOffset=-1: 7天前
		Calendar cal = Calendar.getInstance();
		// 修正计算逻辑：weekOffset是负数，需要正确计算往前推的天数
		int daysToSubtract = Math.abs(weekOffset) * 7;
		cal.add(Calendar.DAY_OF_YEAR, -daysToSubtract);
		String result = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
		System.out.println("计算周结束日期 - weekOffset: " + weekOffset + ", 往前推天数: " + daysToSubtract + ", result: " + result);
		return result;
	}

	/**
	 * 按月查询：30天为一个周期
	 * @param monthOffset 月偏移量（0-本月，-1-1个月前，-2-2个月前）
	 * @return 月开始日期
	 */
	private String getMonthStartDate(int monthOffset) {
		// monthOffset=0: 今天往前推30天
		// monthOffset=-1: 30天前往前推30天
		Calendar cal = Calendar.getInstance();
		// 修正计算逻辑：monthOffset是负数，需要正确计算往前推的天数
		int daysToSubtract = Math.abs(monthOffset) * 30 + 29;
		cal.add(Calendar.DAY_OF_YEAR, -daysToSubtract);
		String result = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
		System.out.println("计算月开始日期 - monthOffset: " + monthOffset + ", 往前推天数: " + daysToSubtract + ", result: " + result);
		return result;
	}

	/**
	 * 获取月结束日期
	 * @param monthOffset 月偏移量
	 * @return 月结束日期
	 */
	private String getMonthStopDate(int monthOffset) {
		// monthOffset=0: 今天
		// monthOffset=-1: 30天前
		Calendar cal = Calendar.getInstance();
		// 修正计算逻辑：monthOffset是负数，需要正确计算往前推的天数
		int daysToSubtract = Math.abs(monthOffset) * 30;
		cal.add(Calendar.DAY_OF_YEAR, -daysToSubtract);
		String result = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
		System.out.println("计算月结束日期 - monthOffset: " + monthOffset + ", 往前推天数: " + daysToSubtract + ", result: " + result);
		return result;
	}

	/**
	 * 根据部门父ID查询库存批次（带分页）
	 * @param request 请求参数，包含 depFatherId（必填）、disId（可选）、status（可选）、restWeight（可选）、startDate（可选）、stopDate（可选）、page（可选，默认1）、limit（可选，默认20）
	 * @return 库存批次列表（带分页信息）
	 */
	@RequestMapping(value = "/queryStockListByDepFatherId", method = RequestMethod.POST)
	@ResponseBody
	public R queryStockListByDepFatherId(@RequestBody Map<String, Object> request) {
		try {
			// 获取部门父ID（必填参数）
			Object depFatherIdObj = request.get("depFatherId");
			if (depFatherIdObj == null) {
				return R.error("部门父ID不能为空");
			}
			
			Integer depFatherId;
			try {
				depFatherId = Integer.parseInt(depFatherIdObj.toString());
			} catch (NumberFormatException e) {
				return R.error("部门父ID格式错误");
			}
			
			// 获取分页参数
			Object pageObj = request.get("page");
			Object limitObj = request.get("limit");
			
			int page = 1;
			int limit = 20;
			
			if (pageObj != null) {
				try {
					page = Integer.parseInt(pageObj.toString());
					if (page < 1) {
						page = 1;
					}
				} catch (NumberFormatException e) {
					// 使用默认值
				}
			}
			
			if (limitObj != null) {
				try {
					limit = Integer.parseInt(limitObj.toString());
					if (limit < 1) {
						limit = 20;
					}
					// 限制每页最大数量，防止查询过大
					if (limit > 100) {
						limit = 100;
					}
				} catch (NumberFormatException e) {
					// 使用默认值
				}
			}
			
			// 计算偏移量
			int offset = (page - 1) * limit;
			
			// 构建查询参数
			Map<String, Object> queryMap = new HashMap<>();
			queryMap.put("depFatherId", depFatherId);
			queryMap.put("offset", offset);
			queryMap.put("limit", limit);
			
			// 可选参数：分销商ID
			Object disIdObj = request.get("disId");
			if (disIdObj != null) {
				try {
					Integer disId = Integer.parseInt(disIdObj.toString());
					queryMap.put("disId", disId);
				} catch (NumberFormatException e) {
					// 忽略格式错误的可选参数
				}
			}
			
			// 查询状态大于 -1 的库存（排除待确认状态）
			queryMap.put("statusGreaterThan", -1);
			
			// 可选参数：状态（如果提供了，会覆盖上面的默认条件）
			Object statusObj = request.get("status");
			if (statusObj != null) {
				try {
					Integer status = Integer.parseInt(statusObj.toString());
					queryMap.remove("statusGreaterThan"); // 移除大于条件
					queryMap.put("status", status);
				} catch (NumberFormatException e) {
					// 忽略格式错误的可选参数
				}
			}
			
			// 可选参数：剩余重量（只查询剩余重量大于此值的批次）
			Object restWeightObj = request.get("restWeight");
			if (restWeightObj != null) {
				try {
					String restWeight = restWeightObj.toString();
					queryMap.put("restWeight", restWeight);
				} catch (Exception e) {
					// 忽略格式错误的可选参数
				}
			}
			
			// 可选参数：开始日期
			Object startDateObj = request.get("startDate");
			if (startDateObj != null) {
				queryMap.put("startDate", startDateObj.toString());
			}
			
			// 可选参数：结束日期
			Object stopDateObj = request.get("stopDate");
			if (stopDateObj != null) {
				queryMap.put("stopDate", stopDateObj.toString());
			}
			
			// 查询总数
			Integer total = nxDistributerGoodsShelfStockService.queryStockCountByDepFatherId(queryMap);
			
			// 查询库存批次列表
			List<NxDistributerGoodsShelfStockEntity> stockList = nxDistributerGoodsShelfStockService.queryStockListByDepFatherId(queryMap);
			
			// 计算总页数
			int totalPages = (int) Math.ceil((double) total / limit);
			
			return R.ok()
				.put("data", stockList)
				.put("total", total)
				.put("page", page)
				.put("limit", limit)
				.put("totalPages", totalPages);
			
		} catch (Exception e) {
			return R.error("查询库存批次失败: " + e.getMessage());
		}
	}

	/**
	 * 获取有库存的商品的祖父分类列表（带配送商品列表用于排序）
	 * 参考 depGetDepDisGoodsCata 的返回格式，但 disGoodsArr 返回配送商品列表而不是ID列表
	 * @param nxDisId 分销商ID（必填）
	 * @param depFatherId 部门父ID（可选），如果提供，会返回该部门status=0的订单列表
	 * @return 包含分类列表和配送商品列表，如果提供了depFatherId，还包含订单列表
	 */
	@RequestMapping(value = "/getStockFatherGoodsCata", method = RequestMethod.GET)
	@ResponseBody
	public R getStockFatherGoodsCata(@RequestParam Integer nxDisId,
	                                  @RequestParam(required = false) Integer depFatherId) {
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("disId", nxDisId);
			map.put("status", -1); // 待确认状态
			map.put("restWeight", "0"); // 剩余重量大于0
			
			// 查询有库存的商品的分类树（祖父分类）
			List<NxDistributerFatherGoodsEntity> fatherGoodsList = nxDistributerGoodsShelfStockService.queryStockTreeFatherGoodsByParams(map);
			
			// 查询有库存的配送商品列表（用于页面排序）
			List<NxDistributerGoodsEntity> disGoodsList = nxDistributerGoodsShelfStockService.queryGoodsStockList(map);
			
			Map<String, Object> mapR = new HashMap<>();
			mapR.put("cataArr", fatherGoodsList);
			mapR.put("disGoodsArr", disGoodsList); // 配送商品列表，而不是ID列表
			
			// 如果提供了部门父ID，查询该部门status=0的订单列表，并创建或更新bill
			if (depFatherId != null) {
				Map<String, Object> orderQueryMap = new HashMap<>();
				orderQueryMap.put("depFatherId", depFatherId);
				orderQueryMap.put("equalStatus", -1); // 订单状态等于0
				orderQueryMap.put("isPurType", true); // purGoodsId is not null
				List<NxDepartmentOrdersEntity> orderList = nxDepartmentOrdersService.queryDisOrdersByParams(orderQueryMap);
				mapR.put("orderArr", orderList != null ? orderList : new ArrayList<>());
				
				// 只有在有订单的情况下，才创建或更新bill
				if (orderList != null && !orderList.isEmpty()) {
					// 计算订单subtotal合计
					Double ordersSubtotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(orderQueryMap);
					if (ordersSubtotal == null) {
						ordersSubtotal = 0.0;
					}
					
					// 查询部门信息，获取积分
					// 先尝试用depFatherId查询（如果depFatherId本身就是一个部门ID）
					NxDepartmentEntity depInfo = nxDepartmentService.queryDepInfo(depFatherId);
					// 如果查询不到，尝试从订单列表中获取depId
					if (depInfo == null) {
						// 从第一个订单获取depId
						Integer depId = orderList.get(0).getNxDoDepartmentId();
						if (depId != null) {
							depInfo = nxDepartmentService.queryDepInfo(depId);
						}
					}
					
					if (depInfo == null) {
						// 如果仍然查询不到部门信息，记录日志但不创建bill
						System.out.println("警告：无法查询到部门信息，depFatherId=" + depFatherId);
					} else {
						// 获取部门积分
						String departmentPointsStr = depInfo.getNxDepartmentPoints();
						System.out.println("depooonsos" + depInfo.getNxDepartmentPoints());
						BigDecimal departmentPoints = (departmentPointsStr != null && !departmentPointsStr.trim().isEmpty()) 
							? new BigDecimal(departmentPointsStr) : BigDecimal.ZERO;
						
						// 计算bill total（订单subtotal合计）
						BigDecimal billTotal = new BigDecimal(ordersSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
						
						// 计算支付积分和支付现金
						BigDecimal payPoints = BigDecimal.ZERO;
						BigDecimal payCash = BigDecimal.ZERO;
						
						if (billTotal.compareTo(BigDecimal.ZERO) > 0) {
							// 如果部门积分够支付，全部用积分支付
							if (departmentPoints.compareTo(billTotal) >= 0) {
								payPoints = billTotal;
								payCash = BigDecimal.ZERO;
							} else {
								// 积分不够，用全部积分支付，剩余用现金
								payPoints = departmentPoints;
								payCash = billTotal.subtract(departmentPoints).setScale(1, BigDecimal.ROUND_HALF_UP);
							}
						}
						
						// 查询是否已存在bill（根据depFatherId和status=0）
						Map<String, Object> billQueryMap = new HashMap<>();
						billQueryMap.put("depFatherId", depFatherId);
						billQueryMap.put("equalStatus", -1);
						List<NxDepartmentBillEntity> existingBills = nxDepartmentBillService.queryBillsByParams(billQueryMap);
						
						NxDepartmentBillEntity bill;
						if (existingBills != null && !existingBills.isEmpty()) {
							// 如果已经存在bill，更新total
							bill = existingBills.get(0);
							bill.setNxDbTotal(billTotal.toString());
							bill.setNxDbPayPoints(payPoints.toString());
							bill.setNxDbPayCash(payCash.toString());
							nxDepartmentBillService.update(bill);
						} else {
							// 只有当不存在bill时才创建新bill
							bill = new NxDepartmentBillEntity();
							bill.setNxDbDepFatherId(depFatherId);
							bill.setNxDbDepId(depInfo.getNxDepartmentId());
							bill.setNxDbDisId(depInfo.getNxDepartmentDisId());
							bill.setNxDbTotal(billTotal.toString());
							bill.setNxDbStatus(-1);
							bill.setNxDbDate(DateUtils.formatWhatDay(0));
							bill.setNxDbTime(DateUtils.formatWhatYearDayTime(0));
							bill.setNxDbMonth(DateUtils.formatWhatMonth(0));
							bill.setNxDbWeek(DateUtils.getWeekOfYear(0).toString());
							bill.setNxDbDay(DateUtils.getWeek(0));
							bill.setNxDbYear(DateUtils.formatWhatYear(0));
							bill.setNxDbPayPoints(payPoints.toString());
							bill.setNxDbPayCash(payCash.toString());
							bill.setNxDbGbDisId(-1);
							bill.setNxDbGbDepId(-1);
							bill.setNxDbGbDepFatherId(-1);
							bill.setNxDbNxRestrauntId(-1);
							bill.setNxDbNxCommunityId(-1);
							nxDepartmentBillService.save(bill);
						}
						
						mapR.put("bill", bill);
					}
				}
			}
			
			return R.ok().put("data", mapR);
		} catch (Exception e) {
			return R.error("查询失败: " + e.getMessage());
		}
	}

	/**
	 * 使用部门积分完成支付（不需要微信支付）
	 * 扣除部门积分，更新订单状态和bill状态
	 * @param request 请求参数，包含 billId
	 * @return 处理结果
	 */
	@RequestMapping(value = "/toFinishBill", method = RequestMethod.POST)
	@ResponseBody
	public R toFinishBill(@RequestBody Map<String, Object> request) {
		try {
			// 获取参数
			Object billIdObj = request.get("billId");
			
			if (billIdObj == null) {
				return R.error("账单ID不能为空");
			}
			
			Integer billId;
			try {
				billId = Integer.parseInt(billIdObj.toString());
			} catch (NumberFormatException e) {
				return R.error("账单ID格式错误");
			}
			
			// 查询bill
			NxDepartmentBillEntity bill = nxDepartmentBillService.queryObject(billId);
			if (bill == null) {
				return R.error("账单不存在");
			}
			
			// 检查bill状态，只有status=0的bill才能完成支付
			if (bill.getNxDbStatus() == null || bill.getNxDbStatus() != -1) {
				return R.error("账单状态不正确，无法完成支付");
			}
			
			// 从bill中获取depFatherId
			Integer depFatherId = bill.getNxDbDepFatherId();
			if (depFatherId == null) {
				return R.error("账单中缺少部门父ID信息");
			}
			
			// 查询该部门status=0的订单列表
			Map<String, Object> orderQueryMap = new HashMap<>();
			orderQueryMap.put("depFatherId", depFatherId);
			orderQueryMap.put("equalStatus", -1);
			List<NxDepartmentOrdersEntity> orderList = nxDepartmentOrdersService.queryDisOrdersByParams(orderQueryMap);
			
			if (orderList == null || orderList.isEmpty()) {
				return R.error("没有待支付的订单");
			}
			
			// 查询部门信息
			NxDepartmentEntity depInfo = nxDepartmentService.queryDepInfo(depFatherId);
			if (depInfo == null && !orderList.isEmpty()) {
				Integer depId = orderList.get(0).getNxDoDepartmentId();
				if (depId != null) {
					depInfo = nxDepartmentService.queryDepInfo(depId);
				}
			}
			
			if (depInfo == null) {
				return R.error("部门信息不存在");
			}
			
			// 获取bill的支付积分
			String payPointsStr = bill.getNxDbPayPoints();
			BigDecimal payPoints = (payPointsStr != null && !payPointsStr.trim().isEmpty()) 
				? new BigDecimal(payPointsStr) : BigDecimal.ZERO;
			
			// 获取部门当前积分
			String departmentPointsStr = depInfo.getNxDepartmentPoints();
			BigDecimal departmentPoints = (departmentPointsStr != null && !departmentPointsStr.trim().isEmpty()) 
				? new BigDecimal(departmentPointsStr) : BigDecimal.ZERO;
			
			// 检查部门积分是否足够
			if (departmentPoints.compareTo(payPoints) < 0) {
				return R.error("部门积分不足，无法完成支付");
			}
			
			// 扣除部门积分
			BigDecimal newDepartmentPoints = departmentPoints.subtract(payPoints).setScale(1, BigDecimal.ROUND_HALF_UP);
			depInfo.setNxDepartmentPoints(newDepartmentPoints.toString());
			nxDepartmentService.update(depInfo);
			
			// 更新订单：设置billId、状态=2、采购状态=3
			for (NxDepartmentOrdersEntity order : orderList) {
				order.setNxDoBillId(billId);
				order.setNxDoStatus(0);
				order.setNxDoPurchaseStatus(0);
				nxDepartmentOrdersService.update(order);
				
				// 查询并更新与该订单关联的库存扣减记录（reduce）状态为0，并更新库存批次剩余数量为0
				Map<String, Object> reduceQueryMap = new HashMap<>();
				reduceQueryMap.put("orderId", order.getNxDepartmentOrdersId());
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
									stock.setNxDgssRestWeight("0");
									stock.setNxDgssRestSubtotal("0");
									nxDistributerGoodsShelfStockService.update(stock);
								}
							}
						}
					}
				}
			}
			
			// 更新bill状态为2（已完成）
			bill.setNxDbStatus(2);
			nxDepartmentBillService.update(bill);
			
			Map<String, Object> result = new HashMap<>();
			result.put("billId", billId);
			result.put("orderCount", orderList.size());
			result.put("payPoints", payPoints.toString());
			result.put("remainingPoints", newDepartmentPoints.toString());
			
			return R.ok().put("data", result);
		} catch (Exception e) {
			return R.error("完成支付失败: " + e.getMessage());
		}
	}

	/**
	 * 获取有库存的配送商品分页列表（带库存批次）
	 * 参考 depGetDepGoodsPage 的分页返回格式
	 * @param limit 每页数量
	 * @param page 页码
	 * @param nxDisId 分销商ID（必填）
	 * @param depFatherId 部门父ID（可选），如果提供，会在返回的库存批次中显示该部门有订单的reduce记录（status=-1）
	 * @return 分页的配送商品列表（带库存批次）
	 */
	@RequestMapping(value = "/getStockGoodsPage", method = RequestMethod.GET)
	@ResponseBody
	public R getStockGoodsPage(@RequestParam Integer limit, 
	                           @RequestParam Integer page, 
	                           @RequestParam Integer nxDisId,
	                           @RequestParam(required = false) Integer depFatherId) {
		try {
			// 设置默认分页参数
			if (page == null || page < 1) {
				page = 1;
			}
			if (limit == null || limit < 1) {
				limit = 20;
			}
			
			// 计算偏移量
			int offset = (page - 1) * limit;
			
			Map<String, Object> map = new HashMap<>();
			map.put("disId", nxDisId);
			map.put("status", -1); // 待确认状态
			map.put("restWeight", "0"); // 剩余重量大于0
			map.put("offset", offset);
			map.put("limit", limit);
			
			// 如果提供了部门父ID，添加到查询条件中（用于reduce查询）
			if (depFatherId != null) {
				map.put("depFatherId", depFatherId);
			}
			
			// 查询总数
			Integer total = nxDistributerGoodsShelfStockService.queryGoodsStockCount(map);
			
			// 查询当前页数据（包含库存批次信息）
			// 如果提供了depFatherId，SQL查询会自动过滤reduce记录，只显示该部门有订单的（status=-1）
			System.out.println("chakanadepidiid" + map);
			List<NxDistributerGoodsEntity> currentPageList = nxDistributerGoodsShelfStockService.queryGoodsStockList(map);
			
			// 返回分页数据
			com.nongxinle.utils.PageUtils pageUtil = new com.nongxinle.utils.PageUtils(currentPageList, total, limit, page);
			return R.ok().put("page", pageUtil);
		} catch (Exception e) {
			return R.error("查询失败: " + e.getMessage());
		}
	}

	/**
	 * 根据库存批次保存订单
	 * 将库存批次的数量等信息转换为部门订单
	 * @param request 请求参数，包含 stockId（库存批次ID，必填）、depId（部门ID，必填）、depFatherId（部门父ID，可选）
	 * @return 保存的订单信息
	 */
	@RequestMapping(value = "/saveOrdersFromStock", method = RequestMethod.POST)
	@ResponseBody
	public R saveOrdersFromStock(@RequestBody Map<String, Object> request) {
		try {
			// 获取参数
			Object stockIdObj = request.get("stockId");
			Object depIdObj = request.get("depId");
			Object depFatherIdObj = request.get("depFatherId");
			
			if (stockIdObj == null) {
				return R.error("库存批次ID不能为空");
			}
			if (depIdObj == null) {
				return R.error("部门ID不能为空");
			}
			
			Integer stockId;
			try {
				stockId = Integer.parseInt(stockIdObj.toString());
			} catch (NumberFormatException e) {
				return R.error("库存批次ID格式错误");
			}
			
			Integer depId;
			try {
				depId = Integer.parseInt(depIdObj.toString());
			} catch (NumberFormatException e) {
				return R.error("部门ID格式错误");
			}
			
			// 查询部门信息
			NxDepartmentEntity depInfo = nxDepartmentService.queryDepInfo(depId);
			if (depInfo == null) {
				return R.error("部门不存在");
			}
			
			// 处理部门父ID：如果前端提供了，优先使用；否则从数据库查询
			Integer depFatherId = null;
			if (depFatherIdObj != null) {
				try {
					depFatherId = Integer.parseInt(depFatherIdObj.toString());
					if (depFatherId == 0) {
						depFatherId = depId;
					}
				} catch (NumberFormatException e) {
					// 格式错误，忽略，继续使用数据库查询的值
				}
			}
			
			// 如果前端未提供或格式错误，从数据库查询
			if (depFatherId == null) {
				depFatherId = depInfo.getNxDepartmentFatherId();
				if (depFatherId == null || depFatherId == 0) {
					depFatherId = depId;
				}
			}
			
			Integer disId = depInfo.getNxDepartmentDisId();
			if (disId == null) {
				return R.error("部门未关联分销商");
			}
			
			// 获取今日订单序号
			Map<String, Object> orderCountMap = new HashMap<>();
			orderCountMap.put("depId", depId);
			orderCountMap.put("status", 3);
			orderCountMap.put("todayOrder", 1);
			int todayOrderCount = nxDepartmentOrdersService.queryDepOrdersAcount(orderCountMap);
			
			// 查询库存批次信息
			NxDistributerGoodsShelfStockEntity stock = nxDistributerGoodsShelfStockService.queryObject(stockId);
			if (stock == null) {
				return R.error("库存批次不存在");
			}
			
			// 检查库存批次是否有剩余
			if (stock.getNxDgssRestWeight() == null || 
			    stock.getNxDgssRestWeight().trim().isEmpty() ||
			    new BigDecimal(stock.getNxDgssRestWeight()).compareTo(BigDecimal.ZERO) <= 0) {
				return R.error("库存批次剩余数量不足");
			}
			
			// 查询商品信息
			Integer disGoodsId = stock.getNxDgssNxDisGoodsId();
			if (disGoodsId == null) {
				return R.error("库存批次未关联商品");
			}
			
			NxDistributerGoodsEntity goodsEntity = nxDistributerGoodsService.queryObject(disGoodsId);
			if (goodsEntity == null) {
				return R.error("商品不存在");
			}
			
			// 创建订单
			NxDepartmentOrdersEntity order = new NxDepartmentOrdersEntity();
			
			// 设置基本信息
			order.setNxDoDepartmentId(depId);
			order.setNxDoDepartmentFatherId(depFatherId);
			order.setNxDoDistributerId(disId);
			order.setNxDoDisGoodsId(disGoodsId);
			order.setNxDoDisGoodsFatherId(goodsEntity.getNxDgDfgGoodsFatherId());
			order.setNxDoDisGoodsGrandId(goodsEntity.getNxDgDfgGoodsGrandId());
			order.setNxDoNxGoodsId(goodsEntity.getNxDgNxGoodsId());
			order.setNxDoNxGoodsFatherId(goodsEntity.getNxDgNxFatherId());
			order.setNxDoGoodsType(goodsEntity.getNxDgPurchaseAuto());
			order.setNxDoStatus(-1); // 订单状态：待处理
			order.setNxDoPurchaseStatus(-1); // 采购状态：待采购
			order.setNxDoTodayOrder(todayOrderCount + 1);

			// 从库存批次获取数量和金额信息
			// 使用剩余数量作为订单数量
			String restWeight = stock.getNxDgssRestWeight();
			order.setNxDoWeight(restWeight);
			order.setNxDoQuantity(restWeight);
			
			// 从库存批次获取单价和金额
			String stockPrice = stock.getNxDgssPrice();
			if (stockPrice != null && !stockPrice.trim().isEmpty()) {
				order.setNxDoPrice(stockPrice);
			} else {
				// 如果没有单价，尝试计算：剩余成本 / 剩余数量
				try {
					String restSubtotal = stock.getNxDgssRestSubtotal();
					if (restSubtotal != null && !restSubtotal.trim().isEmpty()) {
						BigDecimal subtotal = new BigDecimal(restSubtotal);
						BigDecimal weight = new BigDecimal(restWeight);
						if (weight.compareTo(BigDecimal.ZERO) > 0) {
							BigDecimal price = subtotal.divide(weight, 2, BigDecimal.ROUND_HALF_UP);
							order.setNxDoPrice(price.toString());
						}
					}
				} catch (Exception e) {
					// 计算失败，使用默认值或商品价格
				}
			}
			
			// 设置订单金额（使用库存批次的剩余成本）
			String restSubtotal = stock.getNxDgssRestSubtotal();
			if (restSubtotal != null && !restSubtotal.trim().isEmpty()) {
				order.setNxDoSubtotal(restSubtotal);
			} else if (stockPrice != null && !stockPrice.trim().isEmpty() && restWeight != null) {
				// 如果没有剩余成本，计算：单价 * 数量
				try {
					BigDecimal price = new BigDecimal(stockPrice);
					BigDecimal weight = new BigDecimal(restWeight);
					BigDecimal subtotal = price.multiply(weight).setScale(1, BigDecimal.ROUND_HALF_UP);
					order.setNxDoSubtotal(subtotal.toString());
				} catch (Exception e) {
					// 计算失败
				}
			}
			
			// 设置规格（从商品获取，如果库存批次有备注可以加到备注里）
			String goodsStandard = goodsEntity.getNxDgGoodsStandardname();
			if (goodsStandard != null) {
				order.setNxDoStandard(goodsStandard);
				order.setNxDoPrintStandard(goodsStandard);
			}
			
			// 设置商品名称
			order.setNxDoGoodsName(goodsEntity.getNxDgGoodsName());
			
			// 设置库存批次备注到订单备注
			String stockRemark = stock.getNxDgssStockRemark();
			if (stockRemark != null && !stockRemark.trim().isEmpty()) {
				order.setNxDoRemark(stockRemark);
			}
			
			// 设置时间字段
			order.setNxDoArriveDate(DateUtils.formatWhatDate(0));
			order.setNxDoApplyDate(DateUtils.formatWhatDay(0));
			order.setNxDoArriveOnlyDate(DateUtils.formatWhatDate(0));
			order.setNxDoArriveWeeksYear(DateUtils.getWeekOfYear(0));
			order.setNxDoArriveWhatDay(DateUtils.getWeek(0));
			order.setNxDoApplyFullTime(DateUtils.formatWhatYearDayTime(0));
			order.setNxDoApplyOnlyTime(DateUtils.formatWhatTime(0));
			
			// 设置其他字段
			order.setNxDoPurchaseGoodsId(-1);
			order.setNxDoCostPriceLevel("1");
			order.setNxDoGbDistributerId(-1);
			order.setNxDoGbDepartmentId(-1);
			order.setNxDoGbDepartmentFatherId(-1);
			order.setNxDoNxCommunityId(-1);
			order.setNxDoNxCommRestrauntId(-1);
			order.setNxDoNxCommRestrauntFatherId(-1);
			order.setNxDoCollaborativeNxDisId(-1);
			// 查询部门商品信息（用于设置部门商品ID和价格）
			Map<String, Object> depGoodsMap = new HashMap<>();
			depGoodsMap.put("depId", depId);
			depGoodsMap.put("disGoodsId", disGoodsId);
			depGoodsMap.put("status", 3);
			if (goodsStandard != null) {
				depGoodsMap.put("standard", goodsStandard);
			}
			NxDepartmentDisGoodsEntity depGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(depGoodsMap);
			if (depGoodsEntity != null) {
				order.setNxDoDepDisGoodsId(depGoodsEntity.getNxDepartmentDisGoodsId());
				// 如果部门商品有价格，使用部门商品价格
				if (depGoodsEntity.getNxDdgOrderPrice() != null && !depGoodsEntity.getNxDdgOrderPrice().trim().isEmpty()) {
					order.setNxDoPrice(depGoodsEntity.getNxDdgOrderPrice());
					// 重新计算小计
					try {
						BigDecimal price = new BigDecimal(depGoodsEntity.getNxDdgOrderPrice());
						BigDecimal weight = new BigDecimal(restWeight);
						BigDecimal subtotal = price.multiply(weight).setScale(1, BigDecimal.ROUND_HALF_UP);
						order.setNxDoSubtotal(subtotal.toString());
					} catch (Exception e) {
						// 计算失败
					}
				}
			} else {
				// 如果没有匹配规格，尝试查询不指定规格的部门商品
				depGoodsMap.remove("standard");
				NxDepartmentDisGoodsEntity depGoodsEntity2 = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(depGoodsMap);
				if (depGoodsEntity2 != null) {
					order.setNxDoDepDisGoodsId(depGoodsEntity2.getNxDepartmentDisGoodsId());
				}
			}
			
			// 保存订单
			nxDepartmentOrdersService.save(order);
			
			// 创建库存扣减记录
			NxDistributerGoodsShelfStockReduceEntity reduceEntity = new NxDistributerGoodsShelfStockReduceEntity();
			reduceEntity.setNxDgssrNxDistributerId(disId);
			reduceEntity.setNxDgssrNxDisGoodsId(disGoodsId);
			reduceEntity.setNxDgssrNxDisGoodsFatherId(goodsEntity.getNxDgDfgGoodsFatherId());
			reduceEntity.setNxDgssrNxStockId(stockId);
			reduceEntity.setNxDgssrDate(DateUtils.formatWhatDay(0));
			reduceEntity.setNxDgssrWeek(DateUtils.getWeek(0));
			reduceEntity.setNxDgssrMonth(DateUtils.formatWhatMonth(0));
			reduceEntity.setNxDgssrFullTime(DateUtils.formatWhatYearDayTime(0));
			reduceEntity.setNxDgssrType(0); // 0=销售扣减
			// 设置扣减重量和金额（使用订单的数量和金额）
			reduceEntity.setNxDgssrCostWeight(restWeight);
			String orderSubtotal = order.getNxDoSubtotal();
			if (orderSubtotal != null && !orderSubtotal.trim().isEmpty()) {
				reduceEntity.setNxDgssrCostSubtotal(orderSubtotal);
			} else {
				// 如果没有订单金额，计算：单价 * 数量
				try {
					String orderPrice = order.getNxDoPrice();
					if (orderPrice != null && !orderPrice.trim().isEmpty()) {
						BigDecimal price = new BigDecimal(orderPrice);
						BigDecimal weight = new BigDecimal(restWeight);
						BigDecimal subtotal = price.multiply(weight).setScale(1, BigDecimal.ROUND_HALF_UP);
						reduceEntity.setNxDgssrCostSubtotal(subtotal.toString());
					}
				} catch (Exception e) {
					// 计算失败，设置为0
					reduceEntity.setNxDgssrCostSubtotal("0");
				}
			}
			// 设置采购商品ID（如果有）
			if (stock.getNxDgssNxPurGoodsId() != null) {
				reduceEntity.setNxDgssrNxPurGoodsId(stock.getNxDgssNxPurGoodsId());
			}
			// 关联订单ID
			reduceEntity.setNxDgssrNxDepOrderId(order.getNxDepartmentOrdersId());
			// 设置状态为 -1
			reduceEntity.setNxDgssrStatus(-1);
			// 保存库存扣减记录
			nxDistributerGoodsShelfStockReduceService.save(reduceEntity);
			
			Map<String, Object> result = new HashMap<>();
			result.put("stockId", stockId);
			result.put("orderId", order.getNxDepartmentOrdersId());
			result.put("reduceId", reduceEntity.getNxDistributerGoodsShelfStockReduceId());
			result.put("message", "订单保存成功");
			
			return R.ok().put("data", result);
			
		} catch (Exception e) {
			return R.error("保存订单失败: " + e.getMessage());
		}
	}

}
