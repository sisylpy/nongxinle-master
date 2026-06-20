package com.nongxinle.service.platform;

import com.nongxinle.dao.NxDepartmentOrdersDao;
import com.nongxinle.dto.platform.PlatformSubmitLineRequest;
import com.nongxinle.dto.platform.PlatformSubmitLineResponse;
import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.NxGoodsEntity;
import com.nongxinle.service.GbDepartmentService;
import com.nongxinle.service.NxGoodsService;
import com.nongxinle.service.PlatformMarketDepartmentService;
import com.nongxinle.utils.GbBillPlatformConstants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static com.nongxinle.utils.DateUtils.formatWhatDate;
import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.DateUtils.formatWhatTime;
import static com.nongxinle.utils.DateUtils.formatWhatYearDayTime;
import static com.nongxinle.utils.DateUtils.getWeek;
import static com.nongxinle.utils.DateUtils.getWeekOfYear;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDepOrderBuyStatusUnPurchase;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxOrderStatusGouwu;

@Service
public class PlatformCustomerSubmitLineServiceImpl implements PlatformCustomerSubmitLineService {

    private static final Logger log = LoggerFactory.getLogger(PlatformCustomerSubmitLineServiceImpl.class);
    private static final int NX_DO_GOODS_NAME_DB_MAX_CHARS = 50;

    @Autowired
    private PlatformMarketDepartmentService platformMarketDepartmentService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private NxGoodsService nxGoodsService;
    @Autowired
    private NxDepartmentOrdersDao nxDepartmentOrdersDao;
    @Autowired
    private PlatformDistributerIdResolver platformDistributerIdResolver;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlatformSubmitLineResponse submitLine(PlatformSubmitLineRequest request) {
        validateRequest(request);
        Integer gbDepartmentId = request.getDepartmentId();
        Integer marketId = request.getMarketId();

        log.info("[platform/cart/addWithoutSupplier] gbDepartmentId={} marketId={} nxGoodsId={} qty={} std={}",
                gbDepartmentId, marketId, request.getNxGoodsId(), request.getQuantity(), request.getStandard());

        platformMarketDepartmentService.ensureActiveForGbCustomer(marketId, gbDepartmentId);

        GbDepartmentEntity gbDepartment = gbDepartmentService.queryObject(gbDepartmentId);
        if (gbDepartment == null) {
            throw new IllegalArgumentException("客户不存在: departmentId=" + gbDepartmentId);
        }

        NxGoodsEntity goods = nxGoodsService.queryObject(request.getNxGoodsId());
        if (goods == null) {
            throw new IllegalArgumentException("标准商品不存在: nxGoodsId=" + request.getNxGoodsId());
        }

        String goodsName = StringUtils.isNotBlank(request.getGoodsName())
                ? request.getGoodsName().trim()
                : goods.getNxGoodsName();

        NxDepartmentOrdersEntity order = buildCartOrder(request, gbDepartment, goods, goodsName);
        nxDepartmentOrdersDao.save(order);
        // 来源 A：购物车临时阶段仅 NX 临时行（status=-1）；GB order 在 checkoutConfirm 新建并挂 bill（§1.5 不可追加旧 bill）。

        PlatformSubmitLineResponse response = new PlatformSubmitLineResponse();
        response.setOrderId(order.getNxDepartmentOrdersId());
        response.setNxGoodsId(request.getNxGoodsId());
        response.setGoodsName(order.getNxDoGoodsName());
        response.setQuantity(order.getNxDoQuantity());
        response.setStandard(order.getNxDoStandard());
        response.setPriceConfirmStatus(GbBillPlatformConstants.PRICE_CONFIRM_PENDING);
        response.setLineSubtotal(null);
        response.setNxDoDistributerId(order.getNxDoDistributerId());
        response.setNxDoDisGoodsId(order.getNxDoDisGoodsId());
        return response;
    }

    private void validateRequest(PlatformSubmitLineRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        if (request.getMarketId() == null) {
            throw new IllegalArgumentException("marketId 不能为空");
        }
        if (request.getDepartmentId() == null) {
            throw new IllegalArgumentException("departmentId 不能为空");
        }
        if (request.getNxGoodsId() == null) {
            throw new IllegalArgumentException("nxGoodsId 不能为空");
        }
        if (StringUtils.isBlank(request.getQuantity())) {
            throw new IllegalArgumentException("quantity 不能为空");
        }
    }

    private NxDepartmentOrdersEntity buildCartOrder(
            PlatformSubmitLineRequest request,
            GbDepartmentEntity gbDepartment,
            NxGoodsEntity goods,
            String goodsName) {

        Integer gbDepartmentId = request.getDepartmentId();
        Integer depFatherId = gbDepartment.getGbDepartmentFatherId();
        if (depFatherId == null || depFatherId <= 0) {
            depFatherId = gbDepartmentId;
        }
        Integer gbDisId = gbDepartment.getGbDepartmentDisId();

        NxDepartmentOrdersEntity order = new NxDepartmentOrdersEntity();
        order.setNxDoNxGoodsId(request.getNxGoodsId());
        order.setNxDoNxGoodsFatherId(goods.getNxGoodsFatherId());
        order.setNxDoDisGoodsId(null);
        order.setNxDoDepDisGoodsId(null);
        order.setNxDoQuantity(request.getQuantity().trim());
        order.setNxDoStandard(StringUtils.isNotBlank(request.getStandard()) ? request.getStandard().trim() : "");
        order.setNxDoRemark(StringUtils.isNotBlank(request.getRemark()) ? request.getRemark().trim() : "");
        order.setNxDoGoodsName(truncateGoodsName(goodsName));
        order.setNxDoGoodsOriginalName(truncateGoodsName(goodsName));

        order.setNxDoDepartmentId(gbDepartmentId);
        order.setNxDoDepartmentFatherId(depFatherId);
        order.setNxDoGbDepartmentId(gbDepartmentId);
        order.setNxDoGbDepartmentFatherId(depFatherId);
        order.setNxDoGbDistributerId(gbDisId != null ? gbDisId : -1);

        order.setNxDoDistributerId(platformDistributerIdResolver.resolvePendingDistributerId(request.getMarketId()));
        order.setNxDoStatus(getNxOrderStatusGouwu());
        order.setNxDoOrderUserId(request.getOrderUserId());
        order.setNxDoCollaborativeNxDisId(-1);

        order.setNxDoApplyDate(formatWhatDay(0));
        order.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        order.setNxDoApplyOnlyTime(formatWhatTime(0));
        order.setNxDoArriveDate(formatWhatDay(0));
        order.setNxDoArriveOnlyDate(formatWhatDate(0));
        order.setNxDoArriveWeeksYear(getWeekOfYear(0));
        order.setNxDoArriveWhatDay(getWeek(0));
        order.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        order.setNxDoPurchaseGoodsId(-1);
        order.setNxDoCostPriceLevel("1");
        order.setNxDoNxCommunityId(-1);
        order.setNxDoNxCommRestrauntId(-1);
        order.setNxDoNxCommRestrauntFatherId(-1);
        order.setNxDoTodayOrder(nextTodayOrder(gbDepartmentId));
        order.setNxDoSubtotal("0");
        return order;
    }

    private int nextTodayOrder(Integer departmentId) {
        Map<String, Object> params = new HashMap<>();
        params.put("depId", departmentId);
        params.put("status", 3);
        params.put("todayOrder", 1);
        return nxDepartmentOrdersDao.queryDepOrdersAcount(params) + 1;
    }

    private String truncateGoodsName(String name) {
        if (name == null) {
            return null;
        }
        if (name.length() <= NX_DO_GOODS_NAME_DB_MAX_CHARS) {
            return name;
        }
        return name.substring(0, NX_DO_GOODS_NAME_DB_MAX_CHARS);
    }
}
