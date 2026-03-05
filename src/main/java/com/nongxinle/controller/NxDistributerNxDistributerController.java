package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 02-19 20:22
 */

import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nongxinle.entity.NxDistributerEntity;
import com.nongxinle.entity.NxDistributerInviteEntity;
import com.nongxinle.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxDistributerNxDistributerEntity;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxdistributernxdistributer")
public class NxDistributerNxDistributerController {
	@Autowired
	private NxDistributerNxDistributerService nxDisNxDisService;
	@Autowired
	private NxDistributerBillService nxDistributerBillService;
	@Autowired
	private NxDistributerBlockService nxDistributerBlockService;
	@Autowired
	private NxDistributerService nxDistributerService;
	@Autowired
	private NxDistributerInviteService nxDistributerInviteService;
	private static final Logger logger = LoggerFactory.getLogger(NxDistributerNxDistributerController.class);


	@RequestMapping(value = "/nxDisGetAllOfferNxDis", method = RequestMethod.POST)
	@ResponseBody
	public R nxDisGetAllOfferNxDis(Integer nxDisId, Integer userId, String startDate, String stopDate) {
		logger.info("[nxDisGetAllOfferNxDis] 开始查询协作伙伴列表，nxDisId={}, startDate={}, stopDate={}",
				nxDisId, startDate, stopDate);

		Map<String, Object> map = new HashMap<>();
		map.put("disId", nxDisId);
		List<NxDistributerEntity> partnerList = nxDisNxDisService.queryOfferNxDisByParams(map);
		logger.info("[nxDisGetAllOfferNxDis] 查询到{}个协作伙伴", partnerList.size());

		// 当前配送商屏蔽的协作伙伴 id 集合（我不看他的商品）
		Set<Integer> blockedByMeIds = new HashSet<>(nxDistributerBlockService.queryBlockedDisIdsByBlocker(nxDisId));
		// 谁屏蔽了我（他不看我的商品）
		Set<Integer> blockedMeIds = new HashSet<>(nxDistributerBlockService.queryBlockerDisIdsByBlocked(nxDisId));

		if (partnerList.size() > 0) {
			for (NxDistributerEntity partnerEntity : partnerList) {
				Integer partnerId = partnerEntity.getNxDistributerId();
				Map<String, Object> billParams = new HashMap<>();
				billParams.put("disId1", nxDisId);
				billParams.put("disId2", partnerId);
				if (startDate != null && !startDate.isEmpty()) {
					billParams.put("startDate", startDate);
				}
				if (stopDate != null && !stopDate.isEmpty()) {
					billParams.put("stopDate", stopDate);
				}

				// 双向账单合计：他欠的 + 合作伙伴欠他的
				int billCount = 0;
				double billTotal = 0.0;
				int unPayCount = 0;
				int unConfirmCount = 0;
				int unConfirmCount1 = 0;
				double unPayTotal = 0.0;
				double unConfirmTotal = 0.0;
				double unConfirmTotal1 = 0.0;
				int havePayCount = 0;
				double havePayTotal = 0.0;

				if (startDate != null && stopDate != null) {
					// 全部账单（双向）
					System.out.println("mapappa1111" + billParams);
					billCount = nxDistributerBillService.queryPartnerMutualBillCount(billParams);
					billTotal = nxDistributerBillService.queryPartnerMutualBillTotal(billParams);

					// 待确认账单收货
					billParams.put("equalStatus", 0);
					System.out.println("mapappa22222" + billParams);
					unConfirmCount = nxDistributerBillService.queryPartnerMutualBillCount(billParams);
					unConfirmTotal = nxDistributerBillService.queryPartnerMutualBillTotal(billParams);
					// 待确认账单付款
					billParams.put("equalStatus", 2);
					System.out.println("mapappa22222" + billParams);
					unConfirmCount1 = nxDistributerBillService.queryPartnerMutualBillCount(billParams);
					unConfirmTotal1 = nxDistributerBillService.queryPartnerMutualBillTotal(billParams);

					// 未结账账单
					billParams.put("equalStatus", 1);
					unPayCount = nxDistributerBillService.queryPartnerMutualBillCount(billParams);
					unPayTotal = nxDistributerBillService.queryPartnerMutualBillTotal(billParams);

					// 已结账账单
					billParams.put("equalStatus", 3);
					havePayCount = nxDistributerBillService.queryPartnerMutualBillCount(billParams);
					havePayTotal = nxDistributerBillService.queryPartnerMutualBillTotal(billParams);
				} else {
					// 无日期时也查询全部
					billCount = nxDistributerBillService.queryPartnerMutualBillCount(billParams);
					billTotal = nxDistributerBillService.queryPartnerMutualBillTotal(billParams);
					billParams.put("equalStatus", 0);
					unPayCount = nxDistributerBillService.queryPartnerMutualBillCount(billParams);
					unPayTotal = nxDistributerBillService.queryPartnerMutualBillTotal(billParams);
					billParams.put("equalStatus", 3);
					havePayCount = nxDistributerBillService.queryPartnerMutualBillCount(billParams);
					havePayTotal = nxDistributerBillService.queryPartnerMutualBillTotal(billParams);
				}

				Map<String, Object> mapDataOne = new HashMap<>();
				mapDataOne.put("blockedByMe", blockedByMeIds.contains(partnerId));  // 我不看他的商品
				mapDataOne.put("blockedMe", blockedMeIds.contains(partnerId));     // 他不看我的商品
				mapDataOne.put("billCount", billCount);
				mapDataOne.put("billTotal", String.format("%.1f", billTotal));
				mapDataOne.put("unPayCount", unPayCount);
				mapDataOne.put("unPayTotal", String.format("%.1f", unPayTotal));
				mapDataOne.put("havePayCount", havePayCount);
				mapDataOne.put("havePayTotal", String.format("%.1f", havePayTotal));
				mapDataOne.put("unConfirmCount", unConfirmCount + unConfirmCount1);
				mapDataOne.put("unConfirmTotal", String.format("%.1f", unConfirmTotal + unConfirmTotal1));

				partnerEntity.setItemData(mapDataOne);
			}
			// 拉黑的显示在后面
			partnerList.sort(Comparator.comparing(p -> blockedByMeIds.contains(p.getNxDistributerId())));
			logger.info("[nxDisGetAllOfferNxDis] 协作伙伴账单统计完成");
		}

		logger.info("[nxDisGetAllOfferNxDis] 查询完成，返回{}个协作伙伴", partnerList.size());
		return R.ok().put("data", partnerList);
	}


	/**
	 * 建立协作关系
	 * inviteType: 1=申请成为供货商，2=申请采购我的商品
	 * inviterNxDistributerId: 发起邀请的配送商 id（申请方），需与 id1 或 id2 之一相同
	 *
	 * inviteType=1 结果：申请方能看到同意方商品；申请方不给同意方看自己的商品；同意方默认不看申请方商品
	 * inviteType=2 结果：同意方不给申请方看商品；申请方不看同意方的商品
	 */
	@RequestMapping(value = "/saveBusiness", method = RequestMethod.POST)
	@ResponseBody
	public R saveBusiness (@RequestBody NxDistributerNxDistributerEntity nxDis) {
		Integer id1 = nxDis.getNxDistributerId1();
		Integer id2 = nxDis.getNxDistributerId2();
		Integer inviteType = nxDis.getInviteType();
		Integer inviterId = nxDis.getInviterNxDistributerId();
		if (id1 == null || id2 == null) {
			return R.error(-1, "请选择两个配送商");
		}
		if (id1.equals(id2)) {
			return R.error(-1, "不能选择相同的配送商");
		}
		// 约定 id_1 < id_2 存储
		if (id1 > id2) {
			nxDis.setNxDistributerId1(id2);
			nxDis.setNxDistributerId2(id1);
			id1 = nxDis.getNxDistributerId1();
			id2 = nxDis.getNxDistributerId2();
		}

		Map<String, Object> map = new HashMap<>();
		map.put("disId", id1);
		List<NxDistributerEntity> partners = nxDisNxDisService.queryOfferNxDisByParams(map);
		final Integer partnerIdToCheck = id2;
		boolean exists = partners.stream().anyMatch(p -> p.getNxDistributerId().equals(partnerIdToCheck));
		if (!exists) {
			// 校验 inviter 必须是协作方之一
			if (inviterId != null && !inviterId.equals(id1) && !inviterId.equals(id2)) {
				return R.error(-1, "发起邀请的配送商必须是协作方之一");
			}
			nxDis.setInviterNxDistributerId(inviterId);
			nxDisNxDisService.save(nxDis);
			if (inviterId != null) {
				Integer partnerId = inviterId.equals(id1) ? id2 : id1;
				if (Integer.valueOf(1).equals(inviteType)) {
					// 申请成为供货商：申请方不给同意方看自己的商品 + 同意方默认不看申请方商品，block(blocker=同意方, blocked=申请方)
					nxDistributerBlockService.blockPartner(partnerId, inviterId);
				} else if (Integer.valueOf(2).equals(inviteType)) {
					// 申请采购我的商品：同意方不给申请方看商品 + 申请方不看同意方的商品，block(blocker=申请方, blocked=同意方)
					nxDistributerBlockService.blockPartner(inviterId, partnerId);
				}
			}
			return R.ok();
		} else {
			return R.error(-1, "已经是合作关系");
		}
	}

	/**
	 * 配送商拉黑协作伙伴（拉黑后查询商品时将看不到该伙伴的商品）
	 * @param blockerDisId 拉黑者配送商 id
	 * @param blockedDisId 被拉黑的协作伙伴 id
	 */
	@RequestMapping(value = "/blockPartner", method = RequestMethod.POST)
	@ResponseBody
	public R blockPartner(Integer blockerDisId, Integer blockedDisId) {
		if (blockerDisId == null || blockedDisId == null) {
			return R.error(-1, "参数不能为空");
		}
		boolean ok = nxDistributerBlockService.blockPartner(blockerDisId, blockedDisId);
		if (ok) {
			return R.ok();
		}
		return R.error(-1, "拉黑失败，请确认对方是您的协作伙伴");
	}

	/**
	 * 配送商取消拉黑协作伙伴（不看他的商品）
	 */
	@RequestMapping(value = "/unblockPartner", method = RequestMethod.POST)
	@ResponseBody
	public R unblockPartner(Integer blockerDisId, Integer blockedDisId) {
		if (blockerDisId == null || blockedDisId == null) {
			return R.error(-1, "参数不能为空");
		}
		boolean ok = nxDistributerBlockService.unblockPartner(blockerDisId, blockedDisId);
		if (ok) {
			return R.ok();
		}
		return R.ok().put("msg", "未找到拉黑记录或已取消");
	}

	/**
	 * 设置「不给他看我的商品」：对方看不到我的商品
	 */
	@RequestMapping(value = "/setHideMyCatalogFromPartner", method = RequestMethod.POST)
	@ResponseBody
	public R setHideMyCatalogFromPartner(Integer myDisId, Integer partnerDisId) {
		if (myDisId == null || partnerDisId == null) {
			return R.error(-1, "参数不能为空");
		}
		boolean ok = nxDistributerBlockService.setHideMyCatalogFromPartner(myDisId, partnerDisId);
		if (ok) {
			return R.ok();
		}
		return R.error(-1, "设置失败，请确认对方是您的协作伙伴");
	}

	/**
	 * 取消「不给他看我的商品」
	 */
	@RequestMapping(value = "/unsetHideMyCatalogFromPartner", method = RequestMethod.POST)
	@ResponseBody
	public R unsetHideMyCatalogFromPartner(Integer myDisId, Integer partnerDisId) {
		if (myDisId == null || partnerDisId == null) {
			return R.error(-1, "参数不能为空");
		}
		nxDistributerBlockService.unsetHideMyCatalogFromPartner(myDisId, partnerDisId);
		return R.ok();
	}

	/**
	 * 解除协作关系（同时清理双向 block 记录）
	 */
	@RequestMapping(value = "/removePartner", method = RequestMethod.POST)
	@ResponseBody
	public R removePartner(Integer myDisId, Integer partnerDisId) {
		if (myDisId == null || partnerDisId == null) {
			return R.error(-1, "参数不能为空");
		}
		NxDistributerNxDistributerEntity coll = nxDisNxDisService.queryByPartnerIds(myDisId, partnerDisId);
		if (coll == null) {
			return R.error(-1, "未找到协作关系");
		}
		nxDistributerBlockService.deleteByPartnerPair(myDisId, partnerDisId);
		nxDisNxDisService.delete(coll.getNxDistributerNxDistributerId());
		return R.ok();
	}

	/**
	 * 创建邀请（A 生成邀请链接）
	 */
	@RequestMapping(value = "/createPartnerInvite", method = RequestMethod.POST)
	@ResponseBody
	public R createPartnerInvite(Integer inviterDisId, Integer inviteType, String inviteePhone) {
		if (inviterDisId == null) {
			return R.error(-1, "邀请人配送商ID不能为空");
		}
		NxDistributerInviteEntity invite = nxDistributerInviteService.createInvite(inviterDisId, inviteType, inviteePhone);
		if (invite == null) {
			return R.error(-1, "创建邀请失败");
		}
		Map<String, Object> data = new HashMap<>();
		data.put("inviteCode", invite.getInviteCode());
		data.put("inviteUrl", "pages/register/register?inviteCode=" + invite.getInviteCode());
		data.put("expireDays", 7);
		return R.ok().put("data", data);
	}

	/**
	 * 校验邀请码（B 注册前）
	 */
	@RequestMapping(value = "/validateInviteCode", method = RequestMethod.POST)
	@ResponseBody
	public R validateInviteCode(String inviteCode) {
		if (inviteCode == null || inviteCode.trim().isEmpty()) {
			return R.error(-1, "邀请码不能为空");
		}
		NxDistributerInviteEntity invite = nxDistributerInviteService.queryByInviteCode(inviteCode.trim());
		if (invite == null) {
			return R.error(-1, "邀请码无效或已使用");
		}
		NxDistributerEntity inviter = nxDistributerService.queryObject(invite.getInviterNxDistributerId());
		Map<String, Object> data = new HashMap<>();
		data.put("inviteCode", invite.getInviteCode());
		data.put("inviteType", invite.getInviteType());
		data.put("inviterName", inviter != null ? inviter.getNxDistributerShowName() : null);
		data.put("inviterDisId", invite.getInviterNxDistributerId());
		return R.ok().put("data", data);
	}

	/**
	 * 我发出的邀请列表
	 */
	@RequestMapping(value = "/myInviteList", method = RequestMethod.POST)
	@ResponseBody
	public R myInviteList(Integer inviterDisId, Integer offset, Integer limit) {
		if (inviterDisId == null) {
			return R.error(-1, "配送商ID不能为空");
		}
		Map<String, Object> map = new HashMap<>();
		map.put("inviterNxDistributerId", inviterDisId);
		map.put("offset", offset);
		map.put("limit", limit != null ? limit : 20);
		List<NxDistributerInviteEntity> list = nxDistributerInviteService.queryList(map);
		int total = nxDistributerInviteService.queryTotal(map);
		return R.ok().put("data", list).put("total", total);
	}

	/**
	 * 发放奖励（后台用）
	 */
	@RequestMapping(value = "/grantReward", method = RequestMethod.POST)
	@ResponseBody
	public R grantReward(Integer inviteId, Integer rewardPoints) {
		if (inviteId == null) {
			return R.error(-1, "邀请记录ID不能为空");
		}
		NxDistributerInviteEntity invite = nxDistributerInviteService.queryObject(inviteId);
		if (invite == null) {
			return R.error(-1, "邀请记录不存在");
		}
		if (invite.getStatus() != NxDistributerInviteEntity.STATUS_REGISTERED) {
			return R.error(-1, "仅可对已注册成功的邀请发放奖励");
		}
		if (invite.getRewardStatus() == NxDistributerInviteEntity.REWARD_GRANTED) {
			return R.error(-1, "奖励已发放");
		}
		int points = rewardPoints != null && rewardPoints > 0 ? rewardPoints : 10;
		NxDistributerEntity inviter = nxDistributerService.queryObject(invite.getInviterNxDistributerId());
		if (inviter != null && inviter.getNxDistributerBuyQuantity() != null) {
			try {
				int current = Integer.parseInt(inviter.getNxDistributerBuyQuantity());
				inviter.setNxDistributerBuyQuantity(String.valueOf(current + points));
				nxDistributerService.update(inviter);
			} catch (NumberFormatException ignored) {
			}
		}
		invite.setRewardStatus(NxDistributerInviteEntity.REWARD_GRANTED);
		invite.setRewardAmount(java.math.BigDecimal.valueOf(points));
		invite.setRewardType("points");
		nxDistributerInviteService.update(invite);
		return R.ok();
	}

	@RequestMapping(value = "/disGetOfferDis/{id}")
	@ResponseBody
	public R disGetOfferDis(@PathVariable Integer id) {
		Map<String, Object> map = new HashMap<>();
		map.put("orderDisId", id);
		List<NxDistributerEntity> nxDistributerEntities = nxDisNxDisService.queryOfferNxDisByParams(map);
		if(nxDistributerEntities.size() > 0){
			Set<Integer> blockedByMeIds = new HashSet<>(nxDistributerBlockService.queryBlockedDisIdsByBlocker(id));
			Set<Integer> blockedMeIds = new HashSet<>(nxDistributerBlockService.queryBlockerDisIdsByBlocked(id));
			for (NxDistributerEntity entity : nxDistributerEntities) {
				Map<String, Object> itemData = new HashMap<>();
				itemData.put("blockedByMe", blockedByMeIds.contains(entity.getNxDistributerId()));
				itemData.put("blockedMe", blockedMeIds.contains(entity.getNxDistributerId()));
				entity.setItemData(itemData);
			}
			return R.ok().put("data", nxDistributerEntities);
		}else{
			return R.error(-1,"没有");
		}

	}





}
