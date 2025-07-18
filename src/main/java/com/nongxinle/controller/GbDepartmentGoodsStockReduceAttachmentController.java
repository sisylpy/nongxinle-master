package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 05-29 16:35
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.MyAPPIDConfig;
import com.nongxinle.utils.UploadFile;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.formatWhatDate;
import static com.nongxinle.utils.DateUtils.formatWhatYearDayTime;


@RestController
@RequestMapping("api/gbdepartmentgoodsstockreduceattachment")
public class GbDepartmentGoodsStockReduceAttachmentController {
	@Autowired
	private GbDepartmentGoodsStockReduceAttachmentService gbDepGoodsStockReduceAttachmentService;
	@Autowired
	private GbDepartmentGoodsStockService gbDepartmentGoodsStockService;
	@Autowired
	private GbDepartmentGoodsStockReduceService gbDepartmentGoodsStockReduceService;
	@Autowired
	private GbDistributerGoodsService gbDistributerGoodsService;
	@Autowired
	private GbDepartmentUserService gbDepartmentUserService;
	@Autowired
	private NxJrdhSupplierService jrdhSupplierService;
	@Autowired
	private NxJrdhUserService nxJrdhUserService;
	@Autowired
	private GbDistributerPurchaseGoodsService gbDisPurchaseGoodsService;
	@Autowired
	private GbDistributerService gbDistributerService;






	@RequestMapping(value = "/reduceAttachmentSaveWithFileStar", produces = "text/html;charset=UTF-8")
	@ResponseBody
	public R reduceAttachmentSaveWithFileStar (@RequestParam("file") MultipartFile file,
											   @RequestParam("userName") String userName,
											   @RequestParam("id") Integer id,
											   @RequestParam("stars") Integer stars,
											   HttpSession session  ) {
		System.out.println("akfak;fla" + id);
		// 检查上传的文件是否为空
		if (file == null || file.isEmpty()) {
			System.out.println("文件为空！");
			return R.error("文件为空！");
		}
		GbDepartmentGoodsStockEntity stockEntity = gbDepartmentGoodsStockService.queryObject(id);
		stockEntity.setGbDgsStars(stars);
		GbDepartmentGoodsStockReduceAttachmentEntity attachmentEntity = new GbDepartmentGoodsStockReduceAttachmentEntity();

		String newUploadName = "uploadImage";
		String realPath = UploadFile.upload(session, newUploadName, file);

		String filename = file.getOriginalFilename();
		String filePath = newUploadName + "/" + filename;
		attachmentEntity.setGbDgsraFilePath(filePath);
		//添加新用户
		attachmentEntity.setGbDgsraGbDgsrId(id);
		attachmentEntity.setGbDgsraContent(userName);
		attachmentEntity.setGbDgsraNxSupplierId(stockEntity.getGbDgsNxSupplierId());
		attachmentEntity.setGbDgsraNxDistributerId(stockEntity.getGbDgsNxDistributerId());
		attachmentEntity.setGbDgsraStars(stars);
		System.out.println("starrrr" + stars);
		gbDepGoodsStockReduceAttachmentService.save(attachmentEntity);

		return R.ok();
	}





	@RequestMapping(value = "/reduceAttachmentSaveWithFileStar1", produces = "text/html;charset=UTF-8")
	@ResponseBody
	public R reduceAttachmentSaveWithFileStar1 (@RequestParam("file") MultipartFile[] files,
										   @RequestParam("userName") String userName,
										   @RequestParam("id") Integer id,
										   @RequestParam("stars") Integer stars,
										   HttpSession session  ) {
		System.out.println("akfak;fla" + id);
		GbDepartmentGoodsStockReduceAttachmentEntity attachmentEntity = new GbDepartmentGoodsStockReduceAttachmentEntity();
		// 循环处理每个文件
		for (MultipartFile file : files) {
			if (!file.isEmpty()) {
				String newUploadName = "uploadImage";
				String realPath = UploadFile.upload(session, newUploadName, file);

				String filename = file.getOriginalFilename();
				String filePath = newUploadName + "/" + filename;
				attachmentEntity.setGbDgsraFilePath(filePath);
			} else {
				return R.error("上传的文件为空");
			}
		}

		//添加新用户
		attachmentEntity.setGbDgsraGbDgsrId(id);
		attachmentEntity.setGbDgsraContent(userName);

		GbDepartmentGoodsStockReduceEntity reduceEntity = gbDepartmentGoodsStockReduceService.queryObject(id);
		Integer gbDgsrGbGoodsStockId = reduceEntity.getGbDgsrGbGoodsStockId();
		GbDepartmentGoodsStockEntity stockEntity = gbDepartmentGoodsStockService.queryObject(gbDgsrGbGoodsStockId);
		attachmentEntity.setGbDgsraNxSupplierId(stockEntity.getGbDgsNxSupplierId());
		attachmentEntity.setGbDgsraNxDistributerId(stockEntity.getGbDgsNxDistributerId());
		attachmentEntity.setGbDgsraStars(stars);
		System.out.println("starrrr" + stars);
		gbDepGoodsStockReduceAttachmentService.save(attachmentEntity);

	    return R.ok();
	}

	@RequestMapping(value = "/reduceAttachmentSaveWithFile", produces = "text/html;charset=UTF-8")
	@ResponseBody
	public R reduceAttachmentSaveWithFile (@RequestParam("file") MultipartFile file,
										   @RequestParam("userName") String userName,
										   @RequestParam("id") Integer id,
										   HttpSession session  ) {
		System.out.println("akfak;fla" + id);
		System.out.println("接收到的 id: " + id);

		// 检查上传的文件是否为空
		if (file == null || file.isEmpty()) {
			System.out.println("文件为空！");
			return R.error("文件为空！");
		}
		GbDepartmentGoodsStockReduceAttachmentEntity attachmentEntity = new GbDepartmentGoodsStockReduceAttachmentEntity();

		String newUploadName = "uploadImage";
		String realPath = UploadFile.upload(session, newUploadName, file);

		String filename = file.getOriginalFilename();
		String filePath = newUploadName + "/" + filename;
		attachmentEntity.setGbDgsraFilePath(filePath);

		//添加新用户
		attachmentEntity.setGbDgsraGbDgsrId(id);
		attachmentEntity.setGbDgsraContent(userName);

		GbDepartmentGoodsStockReduceEntity reduceEntity = gbDepartmentGoodsStockReduceService.queryObject(id);
		Integer gbDgsrGbGoodsStockId = reduceEntity.getGbDgsrGbGoodsStockId();
		GbDepartmentGoodsStockEntity stockEntity = gbDepartmentGoodsStockService.queryObject(gbDgsrGbGoodsStockId);
		attachmentEntity.setGbDgsraNxSupplierId(stockEntity.getGbDgsNxSupplierId());
		attachmentEntity.setGbDgsraNxDistributerId(stockEntity.getGbDgsNxDistributerId());
		attachmentEntity.setGbDgsraType("4");
		gbDepGoodsStockReduceAttachmentService.save(attachmentEntity);


		//
		Integer gbDgsrGbDisGoodsId = reduceEntity.getGbDgsrGbDisGoodsId();
		GbDistributerGoodsEntity goodsEntity = gbDistributerGoodsService.queryObject(gbDgsrGbDisGoodsId);

		Map<String, TemplateData> mapNotice = new HashMap<>();
		mapNotice.put("thing5", new TemplateData(goodsEntity.getGbDgGoodsName()));
		mapNotice.put("thing6", new TemplateData(attachmentEntity.getGbDgsraContent()));
		mapNotice.put("time7", new TemplateData(stockEntity.getGbDgsDate()));

		GbDepartmentUserEntity gbDepartmentUserEntity = gbDepartmentUserService.queryObject(reduceEntity.getGbDgsrDoUserId());

		mapNotice.put("thing10", new TemplateData(gbDepartmentUserEntity.getGbDuWxNickName()));
		mapNotice.put("time11", new TemplateData(formatWhatYearDayTime(0)));

		System.out.println("nociiciiiicicautotootototoototo" + mapNotice);
		if(stockEntity.getGbDgsNxSupplierId() != -1){
			NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(stockEntity.getGbDgsNxSupplierId());
			Integer nxJrdhsUserId = supplierEntity.getNxJrdhsUserId();
			NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(nxJrdhsUserId);
			Integer gbDgsrGbPurGoodsId = reduceEntity.getGbDgsrGbPurGoodsId();
			GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDisPurchaseGoodsService.queryObject(gbDgsrGbPurGoodsId);
			Integer gbDpgBatchId = purchaseGoodsEntity.getGbDpgBatchId();
			Integer gbDgsGbDistributerId = purchaseGoodsEntity.getGbDpgDistributerId();
			GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(gbDgsGbDistributerId);

			StringBuilder pathBuilder = new StringBuilder("subPackage/pages/gbMarket/gbOrderBatchReturn/gbOrderBatchReturn");
			pathBuilder.append("?batchId=").append(gbDpgBatchId);
			pathBuilder.append("&retName=").append(gbDistributerEntity.getGbDistributerName());
			pathBuilder.append("&disId=").append(gbDistributerEntity.getGbDistributerId());
			Integer nxJrdhsNxJrdhBuyUserId = supplierEntity.getNxJrdhsNxJrdhBuyUserId();
			pathBuilder.append("&buyerUserId=").append(nxJrdhsNxJrdhBuyUserId);
			pathBuilder.append("&fromBuyer=1");
			pathBuilder.append("&depId=").append(supplierEntity.getNxJrdhsGbDepartmentId());

			String path = pathBuilder.toString();
			System.out.println("Encoded URLARRRRRR: " + path);

			WeNoticeService.tuihuoGbSuppliertixingMessageJj(nxJrdhUserEntity.getNxJrdhWxOpenId(), path, mapNotice);

			Map<String, Object> map = new HashMap<>();
			map.put("disId", gbDgsGbDistributerId);
			map.put("admin", 2);
			List<GbDepartmentUserEntity> userEntities = gbDepartmentUserService.queryDepUsersByDepIdAndAdmin(map);
			if(userEntities.size() > 0){
				StringBuilder pathBuilderPur = new StringBuilder("pages/index/index");
				String pathPur = pathBuilderPur.toString();
				for(GbDepartmentUserEntity departmentUserEntity: userEntities){
					// 检查 nxDisId
					System.out.println("deurururrnamemmemempathBuilderPur" + pathPur);
					WeNoticeService.tuihuoGbSuppliertixingMessageJj(departmentUserEntity.getGbDuWxOpenId(), pathPur, mapNotice);
				}
			}

		}


		return R.ok();
	}




	
}
