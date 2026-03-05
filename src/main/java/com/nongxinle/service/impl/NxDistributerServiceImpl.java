package com.nongxinle.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerDao;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDisUserAdmin;


@Service("nxDistributerService")
public class NxDistributerServiceImpl implements NxDistributerService {
	@Autowired
	private NxDistributerDao nxDistributerDao;
	@Autowired
	private NxDistributerUserService nxDistributerUserService;
	@Autowired
	private SysUserService sysUserService;
	@Autowired
	private SysCityMarketService sysCityMarketService;
	@Autowired
	private QyNxDisCropUserService qyNxDisCropUserService;
	@Autowired
	private QyNxDisCorpService qyNxDisCorpService;
	@Autowired
	private NxDistributerFatherGoodsService dgfService;
	@Autowired
	private NxDistributerGoodsService dgService;
	@Autowired
	private NxGoodsService nxGoodsService;
	@Autowired
	private NxDistributerInviteService nxDistributerInviteService;


	@Override
	public void update(NxDistributerEntity nxDistributer){
		nxDistributerDao.update(nxDistributer);
	}

    @Override
    public NxDistributerEntity gbDepQueryDistributerInfo(Map<String, Object> map) {

		return nxDistributerDao.gbDepQueryDistributerInfo(map);
    }

    @Override
    public Integer saveNewNxDistributerWrok(NxDistributerEntity distributerEntity, JSONObject jsonObject) {

		//1.保存distributer
		distributerEntity.setNxDistributerBuyQuantity("0");
		distributerEntity.setNxDistributerShelfQuantity(0);
		distributerEntity.setNxDistributerSysCityId(5);
		distributerEntity.setNxDistributerSysMarketId(1);
		distributerEntity.setNxDistributerAppId("wx159c5a46d80e4500"); // 默认值
		distributerEntity.setNxDistributerPayUrl("MyWxJjdhPayConfig"); // 默认值
		nxDistributerDao.save(distributerEntity);

		//3，保存Dis用户
		Integer distributerId = distributerEntity.getNxDistributerId();
		NxDistributerUserEntity nxDistributerUserEntity = distributerEntity.getNxDistributerUserEntity();
		nxDistributerUserEntity.setNxDiuDistributerId(distributerId);
		nxDistributerUserEntity.setNxDiuPrintDeviceId("-1");
		nxDistributerUserEntity.setNxDiuPrintBillDeviceId("-1");
		nxDistributerUserEntity.setNxDiuLoginTimes(0);
		nxDistributerUserService.save(nxDistributerUserEntity);


		// 3，如果没有注册过
		String corpId = jsonObject.getString("corpid");
		String sessionKey = jsonObject.getString("session_key");
		String openUserId = jsonObject.getString("open_userid");
		String userPinyin = jsonObject.getString("userid");
		QyNxDisCorpEntity qyNxDisCorpEntity =  qyNxDisCorpService.queryQyCropByCropId(corpId);
		if (qyNxDisCorpEntity == null) {
			// 企业不存在，创建新的企业记录
			qyNxDisCorpEntity = new QyNxDisCorpEntity();
			qyNxDisCorpEntity.setQyNxDisQyCorpId(corpId);  // 从前端获取的 corpid

			// 从前端的 qyNxDisCorpEntity 获取其他信息
			if (distributerEntity.getQyNxDisCorpEntity() != null) {
//				qyNxDisCorpEntity.setQyNxDisCorpName(distributerEntity.getQyNxDisCorpEntity().getQyNxDisCorpName());
//				qyNxDisCorpEntity.setQyNxDisCorpRoundLogoUrl(corp_round_logo_url);
//				qyNxDisCorpEntity.setQyNxDisCorpAccessToken(access_token);
//				qyNxDisCorpEntity.setQyNxDisCorpPermanentCode(permanent_code);
//				qyNxDisCorpEntity.setQyNxDisCorpJoinDate(formatWhatFullTime(0));
//				System.out.println("saveAccessAuthCrop----" + qyNxDisCorpEntity);
			}

			// 保存新企业
			qyNxDisCorpService.save(qyNxDisCorpEntity);
		}else{
			Integer qyNxDisQyCorpId = qyNxDisCorpEntity.getQyNxDisCorpId();
			QyNxDisCorpUserEntity userEntity = new QyNxDisCorpUserEntity();
			userEntity.setQyNxDisCorpOpenUserId(openUserId);
			userEntity.setQyNxDisCorpQyCorpId(qyNxDisQyCorpId);
			userEntity.setQyNxDisCorpSessionKey(sessionKey);
			userEntity.setQyNxDistributerId(distributerEntity.getNxDistributerId());
			userEntity.setQyNxDisCorpUserName(userPinyin);
			userEntity.setQyNxDisCorpUserUrl(nxDistributerUserEntity.getNxDiuWxAvartraUrl());
			userEntity.setQyNxDisCorpUserJoinDate(formatWhatFullTime(0));
			qyNxDisCropUserService.save(userEntity);
			nxDistributerUserEntity.setNxDiuQyCorpUserId(userEntity.getQyNxDisCorpUserId());
			nxDistributerUserService.update(nxDistributerUserEntity);
		}

		//linshi
		Integer nxDistributerId = distributerEntity.getNxDistributerId();

		NxDistributerFatherGoodsEntity greatGrand = new NxDistributerFatherGoodsEntity();
		greatGrand.setNxDfgDistributerId(nxDistributerId);
		greatGrand.setNxDfgFatherGoodsLevel(0);
		greatGrand.setNxDfgFatherGoodsImg("goodsImage/logo.jpg");
		greatGrand.setNxDfgFatherGoodsName("临时添加");
		dgfService.save(greatGrand);


		NxDistributerFatherGoodsEntity grand = new NxDistributerFatherGoodsEntity();
		grand.setNxDfgDistributerId(nxDistributerId);
		grand.setNxDfgFatherGoodsLevel(1);
		grand.setNxDfgFatherGoodsName("临时添加");
		grand.setNxDfgGoodsAmount(0);
		grand.setNxDfgFathersFatherId(greatGrand.getNxDistributerFatherGoodsId());

		dgfService.save(grand);
		NxDistributerFatherGoodsEntity father = new NxDistributerFatherGoodsEntity();
		father.setNxDfgDistributerId(nxDistributerId);
		father.setNxDfgFatherGoodsLevel(2);
		father.setNxDfgFatherGoodsName("临时添加");
		father.setNxDfgGoodsAmount(0);
		father.setNxDfgFathersFatherId(grand.getNxDistributerFatherGoodsId());
		dgfService.save(father);

		List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisGoodsCataLinshi(nxDistributerId);
		NxDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
		distributerEntity.setLinshiFather(fatherGoodsEntity);

		if(distributerEntity.getIsSelected()){
			System.out.println("xiaziaiiaiiaiaii");
			postNewDisGoodsAndDisFatherGoods(distributerEntity);
		}

		return nxDistributerUserEntity.getNxDistributerUserId();

    }

    @Override
    public List<NxDistributerEntity> queryNxDisCustomerBySellerOpenId(String openId) {

		return nxDistributerDao.queryNxDisCustomerBySellerOpenId(openId);
    }

    @Override
    public NxDistributerEntity queryNxDisInfo(Integer id) {

		return nxDistributerDao.queryNxDisInfo(id);
    }

    @Override
    public List<NxDistributerEntity> queryAllTypeOne() {

		return nxDistributerDao.queryAllTypeOne();
    }

    @Override
    public void delteNxDis(Integer id) {
        nxDistributerDao.delete(id);
    }

    @Override
    public NxDistributerEntity queryDistributerInfo(Integer disId) {

		return nxDistributerDao.queryDistributerInfo(disId);
    }


    @Override
	public void save(NxDistributerEntity nxDistributer){
		nxDistributer.setNxDistributerSysCityId(5);
		nxDistributer.setNxDistributerSysMarketId(1);
		nxDistributerDao.save(nxDistributer);
	}

	@Override
	public Integer saveNewNxDistributer(NxDistributerEntity distributerEntity) {
		//1.保存distributer

		distributerEntity.setNxDistributerShelfQuantity(0);
		distributerEntity.setNxDistributerBusinessTypeId(0);
		Integer marketId = distributerEntity.getNxDistributerSysMarketId();
		
		// 如果marketId为空，使用默认值1
		if (marketId == null) {
			marketId = 1;
			distributerEntity.setNxDistributerSysMarketId(marketId);
		}
		
		// 获取市场配置
		SysCityMarketEntity sysCityMarketEntity = sysCityMarketService.queryObject(marketId);
		
		// 如果查询不到市场配置，使用默认配置
		if (sysCityMarketEntity == null) {
			System.out.println("警告：未找到市场ID=" + marketId + "的配置，使用默认配置");
			sysCityMarketEntity = new SysCityMarketEntity();
			sysCityMarketEntity.setSysCityMarketId(marketId);
			sysCityMarketEntity.setSysCmMarketName("默认市场");
			sysCityMarketEntity.setSysCmRegisterGiftPoints(1000);
		}
		
		if(marketId != 1){
			distributerEntity.setNxDistributerType(-1);
			
			// 根据市场配置设置小程序AppID和支付配置类
//			if (sysCityMarketEntity.getSysCmMiniAppId() != null) {
//				distributerEntity.setNxDistributerAppId(sysCityMarketEntity.getSysCmMiniAppId());
//			} else {
//				distributerEntity.setNxDistributerAppId("wx159c5a46d80e4500"); // 默认值
//			}
			distributerEntity.setNxDistributerAppId("-1");
			
			if (sysCityMarketEntity.getSysCmPayConfigClass() != null) {
				distributerEntity.setNxDistributerPayUrl(sysCityMarketEntity.getSysCmPayConfigClass());
			} else {
				distributerEntity.setNxDistributerPayUrl("MyWxJjdhPayConfig"); // 默认值
			}
			
			distributerEntity.setNxDistributerSysCityId(sysCityMarketEntity.getSysCmCityId());
			distributerEntity.setNxDistributerSysMarketId(marketId);
			
			// 根据市场配置设置注册赠送点数
			if (sysCityMarketEntity.getSysCmRegisterGiftPoints() != null) {
				distributerEntity.setNxDistributerBuyQuantity(String.valueOf(sysCityMarketEntity.getSysCmRegisterGiftPoints()));
			} else {
				distributerEntity.setNxDistributerBuyQuantity("0"); // 默认赠送1000点
			}

		}else{
			distributerEntity.setNxDistributerType(1);
			
			// 市场1也使用市场配置
			if (sysCityMarketEntity.getSysCmMiniAppId() != null) {
				distributerEntity.setNxDistributerAppId(sysCityMarketEntity.getSysCmMiniAppId());
			}
			
			if (sysCityMarketEntity.getSysCmPayConfigClass() != null) {
				distributerEntity.setNxDistributerPayUrl(sysCityMarketEntity.getSysCmPayConfigClass());
			}
			
			// 根据市场配置设置注册赠送点数
			if (sysCityMarketEntity.getSysCmRegisterGiftPoints() != null) {
				distributerEntity.setNxDistributerBuyQuantity(String.valueOf(sysCityMarketEntity.getSysCmRegisterGiftPoints()));
			} else {
				distributerEntity.setNxDistributerBuyQuantity("0"); // 市场1默认不赠送
			}
		}

		distributerEntity.setNxDistributerMarketName(sysCityMarketEntity.getSysCmMarketName());

//		String inputInviteCode = distributerEntity.getInviteCode();
//		distributerEntity.setInviteCode(null);
		nxDistributerDao.save(distributerEntity);


		//3，保存Dis用户
		Integer distributerId = distributerEntity.getNxDistributerId();
		NxDistributerUserEntity nxDistributerUserEntity = distributerEntity.getNxDistributerUserEntity();
		nxDistributerUserEntity.setNxDiuDistributerId(distributerId);
		nxDistributerUserEntity.setNxDiuPrintDeviceId("-1");
		nxDistributerUserEntity.setNxDiuPrintBillDeviceId("-1");
		nxDistributerUserEntity.setNxDiuLoginTimes(0);
		nxDistributerUserEntity.setNxDiuAdmin(getNxDisUserAdmin());
		nxDistributerUserService.save(nxDistributerUserEntity);

		//linshi
		Integer nxDistributerId = distributerEntity.getNxDistributerId();
		NxDistributerFatherGoodsEntity greatGrand = new NxDistributerFatherGoodsEntity();
		greatGrand.setNxDfgDistributerId(nxDistributerId);
		greatGrand.setNxDfgFatherGoodsLevel(0);
		greatGrand.setNxDfgFatherGoodsImg("goodsImage/logo.jpg");
		greatGrand.setNxDfgFatherGoodsName("临时添加");
		dgfService.save(greatGrand);


		NxDistributerFatherGoodsEntity grand = new NxDistributerFatherGoodsEntity();
		grand.setNxDfgDistributerId(nxDistributerId);
		grand.setNxDfgFatherGoodsLevel(1);
		grand.setNxDfgFatherGoodsName("临时添加");
		grand.setNxDfgGoodsAmount(0);
		grand.setNxDfgFathersFatherId(greatGrand.getNxDistributerFatherGoodsId());

		dgfService.save(grand);
		NxDistributerFatherGoodsEntity father = new NxDistributerFatherGoodsEntity();
		father.setNxDfgDistributerId(nxDistributerId);
		father.setNxDfgFatherGoodsLevel(2);
		father.setNxDfgFatherGoodsName("临时添加");
		father.setNxDfgGoodsAmount(0);
		father.setNxDfgFathersFatherId(grand.getNxDistributerFatherGoodsId());
		dgfService.save(father);

		List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisGoodsCataLinshi(nxDistributerId);
		NxDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
		distributerEntity.setLinshiFather(fatherGoodsEntity);

		return nxDistributerUserEntity.getNxDistributerUserId();
	}


	private void postNewDisGoodsAndDisFatherGoods(NxDistributerEntity distributerEntity){
		Integer nxDistributerId = distributerEntity.getNxDistributerId();
		//postGoods
		if(distributerEntity.getNxDistributerType() > 0){
			saveDisGoodsOLdSon(nxDistributerId);
		}

	}


	private void saveDisGoodsOLdSon(Integer disId) {
		Map<String, Object> mapAll = new HashMap<>();
		mapAll.put("level", 3);
//		mapAll.put("isOldestSon", 1);
		List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.queryNxGoodsByParams(mapAll);

		for(NxGoodsEntity nxGoodsEntity1: nxGoodsEntities){
			NxDistributerGoodsEntity cgnGoods = new NxDistributerGoodsEntity();
			Integer nxGoodsId = nxGoodsEntity1.getNxGoodsId();
			NxGoodsEntity nxGoodsEntity = nxGoodsService.queryObject(nxGoodsId);
			cgnGoods.setNxDgDistributerId(disId);
			cgnGoods.setNxDgNxGoodsId(nxGoodsEntity.getNxGoodsId());
			cgnGoods.setNxDgGoodsName(nxGoodsEntity.getNxGoodsName());
			cgnGoods.setNxDgGoodsStandardname(nxGoodsEntity.getNxGoodsStandardname());
			cgnGoods.setNxDgGoodsDetail(nxGoodsEntity.getNxGoodsDetail());
			cgnGoods.setNxDgGoodsPlace(nxGoodsEntity.getNxGoodsPlace());
			cgnGoods.setNxDgGoodsBrand(nxGoodsEntity.getNxGoodsBrand());
			cgnGoods.setNxDgGoodsStandardWeight(nxGoodsEntity.getNxGoodsStandardWeight());
			cgnGoods.setNxDgGoodsPinyin(nxGoodsEntity.getNxGoodsPinyin());
			cgnGoods.setNxDgGoodsPy(nxGoodsEntity.getNxGoodsPy());
			cgnGoods.setNxDgPullOff(0);
			cgnGoods.setNxDgGoodsStatus(1);
			cgnGoods.setNxDgNxFatherId(nxGoodsEntity.getNxGoodsFatherId());
			cgnGoods.setNxDgNxFatherName(nxGoodsEntity.getFatherGoods().getNxGoodsName());
			cgnGoods.setNxDgNxFatherImg(nxGoodsEntity.getFatherGoods().getNxGoodsFile());
			cgnGoods.setNxDgGoodsFile(nxGoodsEntity.getNxGoodsFile());
			cgnGoods.setNxDgGoodsFileLarge(nxGoodsEntity.getNxGoodsFileBig());
			cgnGoods.setNxDgIsOldestSon(nxGoodsEntity.getNxGoodsIsOldestSon());
			cgnGoods.setNxDgBuyingPrice("0.1");
			cgnGoods.setNxDgWillPrice("0.1");
			cgnGoods.setNxDgBuyingPriceIsGrade(0);
			cgnGoods.setNxDgBuyingPriceUpdate(formatWhatDay(0));
			cgnGoods.setNxDgGoodsSort(nxGoodsEntity.getNxGoodsSort());
			cgnGoods.setNxDgGoodsSonsSort(nxGoodsEntity.getNxGoodsSonsSort());
			cgnGoods.setNxDgPurchaseAuto(-1);
			cgnGoods.setNxDgGoodsIsHidden(0);

			//queryGrandFatherId
			NxGoodsEntity fatherEntity = nxGoodsService.queryObject(nxGoodsEntity.getNxGoodsFatherId());
			Integer grandFatherId = fatherEntity.getNxGoodsFatherId();
			cgnGoods.setNxDgNxGrandId(grandFatherId);
			NxGoodsEntity grandEntity = nxGoodsService.queryObject(fatherEntity.getNxGoodsFatherId());
			cgnGoods.setNxDgNxGrandName(grandEntity.getNxGoodsName());

			//queryGreatGrandFatherId
			Integer greatGrandFatherId = grandEntity.getNxGoodsFatherId();
			if (greatGrandFatherId.equals(1)) {
				cgnGoods.setNxDgNxGoodsFatherColor("#20afb8");
			} if (greatGrandFatherId.equals(2)) {
				cgnGoods.setNxDgNxGoodsFatherColor("#f5c832");
			} if (greatGrandFatherId.equals(3)) {
				cgnGoods.setNxDgNxGoodsFatherColor("#3cc36e");
			} if (greatGrandFatherId.equals(4)) {
				cgnGoods.setNxDgNxGoodsFatherColor("#f09628");
			} if (greatGrandFatherId.equals(5)) {
				cgnGoods.setNxDgNxGoodsFatherColor("#1ebaee");
			} if (greatGrandFatherId.equals(6)) {
				cgnGoods.setNxDgNxGoodsFatherColor("#f05a32");
			} if (greatGrandFatherId.equals(7)) {
				cgnGoods.setNxDgNxGoodsFatherColor("#c0a6dd");
			} if (greatGrandFatherId.equals(8)) {
				cgnGoods.setNxDgNxGoodsFatherColor("#969696");
			}if (greatGrandFatherId.equals(9)) {
				cgnGoods.setNxDgNxGoodsFatherColor("#318666");
			}if (greatGrandFatherId.equals(10)) {
				cgnGoods.setNxDgNxGoodsFatherColor("#026bc2");
			}if (greatGrandFatherId.equals(11)) {
				cgnGoods.setNxDgNxGoodsFatherColor("#06eb6d");
			}if (greatGrandFatherId.equals(12)) {
				cgnGoods.setNxDgNxGoodsFatherColor("#0690eb");
			}

			cgnGoods.setNxDgNxGreatGrandId(greatGrandFatherId);
			cgnGoods.setNxDgNxGreatGrandName(nxGoodsService.queryObject(greatGrandFatherId).getNxGoodsName());

			Integer nxDgDistributerId = cgnGoods.getNxDgDistributerId();

			// 3， 查询父类
			Integer nxDgNxFatherId = cgnGoods.getNxDgNxFatherId();
			Map<String, Object> map = new HashMap<>();
			map.put("disId", nxDgDistributerId);
			map.put("nxFatherId", nxDgNxFatherId);
//			NxDistributerGoodsEntity  disGoodsEntity = dgService.queryOneGoodsAboutNxGoods(map);

			List<NxDistributerGoodsEntity>  distributerGoodsEntities = dgService.queryDisGoodsByParams(map);

			if (distributerGoodsEntities.size() > 0) {
				//直接加disGoods和disStandard,不需要加disFatherGoods
				//1，给父类商品的字段商品数量加1
				NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntities.get(0);
				Integer nxDgDfgGoodsFatherId1 = disGoodsEntity.getNxDgDfgGoodsFatherId();
				Integer nxDgDfgGoodsGrandId = disGoodsEntity.getNxDgDfgGoodsGrandId();

				NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = dgfService.queryObject(nxDgDfgGoodsFatherId1);
				Integer nxDfgGoodsAmount = nxDistributerFatherGoodsEntity.getNxDfgGoodsAmount();
				nxDistributerFatherGoodsEntity.setNxDfgGoodsAmount(nxDfgGoodsAmount + 1);
				dgfService.update(nxDistributerFatherGoodsEntity);

				//2，保存disId商品
				Integer nxDgDfgGoodsFatherId = disGoodsEntity.getNxDgDfgGoodsFatherId();
				cgnGoods.setNxDgDfgGoodsFatherId(nxDgDfgGoodsFatherId);
				cgnGoods.setNxDgDfgGoodsGrandId(nxDgDfgGoodsGrandId);

				//1 ，先保存disGoods
				dgService.save(cgnGoods);
				//
			} else {
				//添加fatherGoods的第一个级别
				NxDistributerFatherGoodsEntity dgf = new NxDistributerFatherGoodsEntity();
				dgf.setNxDfgDistributerId(cgnGoods.getNxDgDistributerId());
				dgf.setNxDfgFatherGoodsName(cgnGoods.getNxDgNxFatherName());
				dgf.setNxDfgFatherGoodsLevel(2);
				dgf.setNxDfgGoodsAmount(1);
				dgf.setNxDfgFatherGoodsImg(cgnGoods.getNxDgNxFatherImg());
				dgf.setNxDfgFatherGoodsColor(cgnGoods.getNxDgNxGoodsFatherColor());
				dgf.setNxDfgNxGoodsId(cgnGoods.getNxDgNxFatherId());
				dgf.setNxDfgFatherGoodsSort(fatherEntity.getNxGoodsSort());
				dgfService.save(dgf);

				//继续查询是否有GrandFather
				String grandName = cgnGoods.getNxDgNxGrandName();
				Map<String, Object> map2 = new HashMap<>();
				map2.put("disId", nxDgDistributerId);
				map2.put("fathersFatherName", grandName);
				List<NxDistributerFatherGoodsEntity> grandGoodsFather = dgfService.queryHasDisFathersFather(map2);
				if (grandGoodsFather.size() > 0) {
					NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = grandGoodsFather.get(0);
					dgf.setNxDfgFathersFatherId(nxDistributerFatherGoodsEntity.getNxDistributerFatherGoodsId());
					dgfService.update(dgf);
					//更新disGoods的fatherGoodsId
					Integer distributerFatherGoodsId = dgf.getNxDistributerFatherGoodsId();
					Integer nxDfgFathersFatherId = dgf.getNxDfgFathersFatherId();
					cgnGoods.setNxDgDfgGoodsFatherId(distributerFatherGoodsId);
					cgnGoods.setNxDgDfgGoodsGrandId(nxDfgFathersFatherId);
					dgService.save(cgnGoods);

				} else {
					//tianjiaGrand
					NxDistributerFatherGoodsEntity grand = new NxDistributerFatherGoodsEntity();
					String nxCgGrandFatherName = cgnGoods.getNxDgNxGrandName();
					grand.setNxDfgFatherGoodsName(nxCgGrandFatherName);
					grand.setNxDfgDistributerId(cgnGoods.getNxDgDistributerId());
					grand.setNxDfgFatherGoodsLevel(1);
					grand.setNxDfgFatherGoodsColor(cgnGoods.getNxDgNxGoodsFatherColor());
					grand.setNxDfgNxGoodsId(cgnGoods.getNxDgNxGrandId());
					Integer grandId = cgnGoods.getNxDgNxGrandId();
					NxGoodsEntity grandNX = nxGoodsService.queryObject(grandId);
					grand.setNxDfgFatherGoodsSort(grandNX.getNxGoodsSort());
					grand.setNxDfgFatherGoodsImg(grandNX.getNxGoodsFile());
					dgfService.save(grand);

					dgf.setNxDfgFathersFatherId(grand.getNxDistributerFatherGoodsId());
					dgfService.update(dgf);
					//更新disGoods的fatherGoodsId
					Integer distributerFatherGoodsId = dgf.getNxDistributerFatherGoodsId();
					Integer nxDfgFathersFatherId = dgf.getNxDfgFathersFatherId();
					cgnGoods.setNxDgDfgGoodsFatherId(distributerFatherGoodsId);
					cgnGoods.setNxDgDfgGoodsGrandId(nxDfgFathersFatherId);
					dgService.save(cgnGoods);
					//查询是否有greatGrand
					String greatGrandName = cgnGoods.getNxDgNxGreatGrandName();
					Map<String, Object> map3 = new HashMap<>();
					map3.put("disId", nxDgDistributerId);
					map3.put("fathersFatherName", greatGrandName);
					List<NxDistributerFatherGoodsEntity> greatGrandGoodsFather = dgfService.queryHasDisFathersFather(map3);
					if (greatGrandGoodsFather.size() > 0) {
						NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = greatGrandGoodsFather.get(0);
						Integer disFatherId = nxDistributerFatherGoodsEntity.getNxDistributerFatherGoodsId();
						grand.setNxDfgFathersFatherId(disFatherId);
						dgfService.update(grand);
					} else {
						NxDistributerFatherGoodsEntity greatGrand = new NxDistributerFatherGoodsEntity();
						String greatGrandName1 = cgnGoods.getNxDgNxGreatGrandName();
						greatGrand.setNxDfgFatherGoodsName(greatGrandName1);
						greatGrand.setNxDfgDistributerId(cgnGoods.getNxDgDistributerId());
						greatGrand.setNxDfgFatherGoodsLevel(0);
						greatGrand.setNxDfgFatherGoodsColor(cgnGoods.getNxDgNxGoodsFatherColor());
						greatGrand.setNxDfgNxGoodsId(cgnGoods.getNxDgNxGreatGrandId());

						Integer greatGrandId = cgnGoods.getNxDgNxGreatGrandId();
						NxGoodsEntity greatGrandNX = nxGoodsService.queryObject(greatGrandId);
						greatGrand.setNxDfgFatherGoodsSort(greatGrandNX.getNxGoodsSort());
						greatGrand.setNxDfgFatherGoodsImg(greatGrandNX.getNxGoodsFile());

						dgfService.save(greatGrand);
						grand.setNxDfgFathersFatherId(greatGrand.getNxDistributerFatherGoodsId());
						dgfService.update(grand);
					}
				}
			}

		}
	}


    @Override
	public NxDistributerEntity queryObject(Integer distributerId){
		return nxDistributerDao.queryObject(distributerId);
	}
//
//	@Override
//	public List<NxDistributerEntity> queryList(Map<String, Object> map){
//		return nxDistributerDao.queryList(map);
//	}
//
//	@Override
//	public int queryTotal(Map<String, Object> map){
//		return nxDistributerDao.queryTotal(map);
//	}
//

	
//	@Override
//	public void update(NxDistributerEntity nxDistributer){
//		nxDistributerDao.update(nxDistributer);
//	}
//
//	@Override
//	public void delete(Integer distributerId){
//		nxDistributerDao.delete(distributerId);
//	}
//
//	@Override
//	public void deleteBatch(Integer[] distributerIds){
//		nxDistributerDao.deleteBatch(distributerIds);
//	}

	@Override
	public List<NxDistributerEntity> queryByMarketId(Integer marketId) {
		return nxDistributerDao.queryByMarketId(marketId);
	}


}
