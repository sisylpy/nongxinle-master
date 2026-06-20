package com.nongxinle.service.platform;

import com.nongxinle.dto.platform.GbPlatformOrderBridgeResult;
import com.nongxinle.dto.platform.LineAmountConfirmResult;
import com.nongxinle.dto.platform.PlatformCartLineCreateCommand;
import com.nongxinle.dto.platform.PlatformCartLineCreateResult;
import com.nongxinle.dto.platform.customer.PlatformCartSubmitItemRequest;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.GbBillPlatformConstants;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.*;

/**
 * 平台购物车单行：购物车临时阶段写 GB+NX（status=-1）；checkoutConfirm 才挂 bill / 转正 / assign。
 * bill 一旦生成不可追加新商品；后续差额支付为补款（paySupplement），不是往旧 bill 加行。
 */
@Service
public class PlatformGbNxOrderLineServiceImpl implements PlatformGbNxOrderLineService {

    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private GbDistributerFatherGoodsService gbDistributerFatherGoodsService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;
    @Autowired
    private GbDistributerPurchaseGoodsService gbDistributerPurchaseGoodsService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private GbPlatformOrderBridgeService gbPlatformOrderBridgeService;
    @Autowired
    private PlatformLineAmountConfirmService platformLineAmountConfirmService;
    @Autowired
    private PlatformGbGoodsEnsureService platformGbGoodsEnsureService;

    @Override
    public PlatformCartLineCreateResult createPlatformCartLine(PlatformCartLineCreateCommand command) {
        if (command.isCartOnly() && command.getBillId() != null) {
            throw new IllegalArgumentException("购物车临时行不得挂已有 bill");
        }
        PlatformCartSubmitItemRequest item = command.getItem();
        NxDistributerGoodsEntity nxGoods = nxDistributerGoodsService.queryObject(item.getNxDistributerGoodsId());
        if (nxGoods == null) {
            throw new IllegalArgumentException("批发商商品不存在: " + item.getNxDistributerGoodsId());
        }
        if (!command.getNxDistributerId().equals(nxGoods.getNxDgDistributerId())) {
            throw new IllegalArgumentException("商品不属于当前批发商: nxDistributerGoodsId=" + item.getNxDistributerGoodsId());
        }

        GbDistributerGoodsEntity gbGoods = platformGbGoodsEnsureService.ensureForNxDisGoods(
                command.getGbDistributerId(),
                resolveGbCatalogDepartmentId(command),
                nxGoods);
        LineAmountConfirmResult confirm = platformLineAmountConfirmService.isLineAmountConfirmable(
                item.getQuantity(), item.getStandard(), nxGoods);

        GbDepartmentOrdersEntity gbOrder = buildGbOrderSkeleton(command, item, gbGoods, nxGoods, confirm);
        if (!command.isCartOnly()) {
            saveDepDisGoodsAndPurchase(gbOrder, gbGoods);
        }
        gbDepartmentOrdersService.save(gbOrder);

        NxDepartmentOrdersEntity nxOrder = buildNxOrder(command, item, gbOrder, nxGoods, confirm);
        if (!command.isCartOnly()) {
            if (nxGoods.getNxDgPurchaseAuto() != null && nxGoods.getNxDgPurchaseAuto() == -1) {
                nxOrder.setNxDoPurchaseGoodsId(-1);
            } else {
                nxDepartmentOrdersService.savePurGoodsAuto(nxOrder, -1, 1);
            }
        }
        nxDepartmentOrdersService.saveForGb(nxOrder);

        gbOrder.setGbDoNxDepartmentOrderId(nxOrder.getNxDepartmentOrdersId());
        if (!command.isCartOnly() && command.getBillId() != null) {
            gbOrder.setGbDoBillId(command.getBillId());
        }
        gbDepartmentOrdersService.update(gbOrder);

        NxDepartmentOrdersEntity savedNx = nxDepartmentOrdersService.queryObject(nxOrder.getNxDepartmentOrdersId());
        GbPlatformOrderBridgeResult bridge = null;
        if (!command.isCartOnly()) {
            bridge = gbPlatformOrderBridgeService.onNxOrderCreatedFromGb(gbOrder, savedNx);
        }

        PlatformCartLineCreateResult result = new PlatformCartLineCreateResult();
        result.setGbOrderId(gbOrder.getGbDepartmentOrdersId());
        result.setNxOrderId(savedNx.getNxDepartmentOrdersId());
        if (bridge != null) {
            result.setPlatformAssignId(bridge.getPlatformAssignId());
            result.setFulfillmentId(bridge.getFulfillmentId());
        }
        result.setGoodsName(StringUtils.isNotBlank(item.getGoodsName()) ? item.getGoodsName() : nxGoods.getNxDgGoodsName());
        result.setQuantity(item.getQuantity());
        result.setStandard(item.getStandard());
        result.setPriceConfirmStatus(confirm.getPriceConfirmStatus());
        result.setLineSubtotal(formatMoney(confirm.isConfirmable() ? confirm.getLineSubtotal() : BigDecimal.ZERO));
        return result;
    }

    @Override
    public void formalizeCartLineAtCheckout(GbDepartmentOrdersEntity gbOrder, NxDepartmentOrdersEntity nxOrder) {
        if (gbOrder == null || nxOrder == null) {
            return;
        }
        if (!PlatformCartLineSupport.hasCustomerSelectedSupplier(nxOrder)) {
            return;
        }
        GbDistributerGoodsEntity gbGoods = gbDistributerGoodsService.queryObject(gbOrder.getGbDoDisGoodsId());
        if (gbGoods == null) {
            throw new IllegalStateException("GB 商品不存在: gbDoDisGoodsId=" + gbOrder.getGbDoDisGoodsId());
        }
        if (PlatformCartLineSupport.needsDepDisGoods(gbOrder.getGbDoDepDisGoodsId())) {
            saveDepDisGoodsAndPurchase(gbOrder, gbGoods);
            gbDepartmentOrdersService.update(gbOrder);
        }
        NxDistributerGoodsEntity nxGoods = nxDistributerGoodsService.queryObject(nxOrder.getNxDoDisGoodsId());
        if (nxGoods != null && PlatformCartLineSupport.needsNxPurchaseGoods(nxOrder)) {
            if (nxGoods.getNxDgPurchaseAuto() != null && nxGoods.getNxDgPurchaseAuto() == -1) {
                nxOrder.setNxDoPurchaseGoodsId(-1);
            } else {
                nxDepartmentOrdersService.savePurGoodsAuto(nxOrder, -1, 1);
            }
            nxDepartmentOrdersService.update(nxOrder);
        }
    }

    private Integer resolveGbCatalogDepartmentId(PlatformCartLineCreateCommand command) {
        if (command.getGbDepartmentFatherId() != null && command.getGbDepartmentFatherId() > 0) {
            return command.getGbDepartmentFatherId();
        }
        return command.getGbDepartmentId();
    }

    private GbDepartmentOrdersEntity buildGbOrderSkeleton(
            PlatformCartLineCreateCommand command,
            PlatformCartSubmitItemRequest item,
            GbDistributerGoodsEntity gbGoods,
            NxDistributerGoodsEntity nxGoods,
            LineAmountConfirmResult confirm) {

        Integer fatherId = command.getGbDepartmentFatherId();
        if (fatherId == null || fatherId == 0) {
            fatherId = command.getGbDepartmentId();
        }

        GbDepartmentOrdersEntity gbOrder = new GbDepartmentOrdersEntity();
        gbOrder.setGbDoOrderUserId(command.getGbOrderUserId());
        gbOrder.setGbDoDepDisGoodsId(-1);
        gbOrder.setGbDoDisGoodsId(gbGoods.getGbDistributerGoodsId());
        gbOrder.setGbDoDepartmentId(command.getGbDepartmentId());
        gbOrder.setGbDoDepartmentFatherId(fatherId);
        gbOrder.setGbDoToDepartmentId(command.getGbDepartmentId());
        gbOrder.setGbDoDistributerId(command.getGbDistributerId());
        gbOrder.setGbDoNxDistributerId(command.getNxDistributerId());
        gbOrder.setGbDoNxDistributerGoodsId(nxGoods.getNxDistributerGoodsId());
        gbOrder.setGbDoNxGoodsId(nxGoods.getNxDgNxGoodsId());
        gbOrder.setGbDoNxGoodsFatherId(nxGoods.getNxDgNxFatherId());
        gbOrder.setGbDoQuantity(item.getQuantity());
        gbOrder.setGbDoStandard(item.getStandard());
        gbOrder.setGbDoRemark(StringUtils.defaultString(item.getRemark(), command.getRemark()));
        gbOrder.setGbDoApplyArriveDate(command.getDeliveryDate());
        gbOrder.setGbDoApplyDate(formatWhatDay(0));
        gbOrder.setGbDoApplyFullTime(formatWhatYearDayTime(0));
        gbOrder.setGbDoApplyOnlyTime(formatWhatTime(0));
        gbOrder.setGbDoArriveOnlyDate(formatWhatDate(0));
        gbOrder.setGbDoArriveWeeksYear(getWeekOfYear(0));
        gbOrder.setGbDoArriveWhatDay(getWeek(0));
        gbOrder.setGbDoGoodsType(gbGoods.getGbDgGoodsType());
        gbOrder.setGbDoOrderType(5);
        gbOrder.setGbDoCostPriceLevel(1);
        gbOrder.setGbDoPrintStandard(item.getStandard());
        gbOrder.setGbDoDsStandardScale("-1");
        gbOrder.setGbDoPriceConfirmStatus(confirm.getPriceConfirmStatus());
        if (command.isCartOnly()) {
            gbOrder.setGbDoStatus(getGbOrderStatusGouwu());
        }

        GbDistributerFatherGoodsEntity fatherGoods = gbDistributerFatherGoodsService.queryObject(gbGoods.getGbDgDfgGoodsFatherId());
        GbDistributerFatherGoodsEntity grandFather = gbDistributerFatherGoodsService.queryObject(fatherGoods.getGbDfgFathersFatherId());
        GbDistributerFatherGoodsEntity greatFather = gbDistributerFatherGoodsService.queryObject(grandFather.getGbDfgFathersFatherId());
        gbOrder.setGbDoDisGoodsFatherId(fatherGoods.getGbDistributerFatherGoodsId());
        gbOrder.setGbDoDisGoodsGrandId(fatherGoods.getGbDfgFathersFatherId());
        gbOrder.setGbDoDisGoodsGreatId(grandFather.getGbDfgFathersFatherId());
        gbOrder.setGbDoNxGoodsGrandId(grandFather.getGbDfgNxGoodsId());
        gbOrder.setGbDoNxGoodsGreatId(greatFather.getGbDfgNxGoodsId());

        if (confirm.isConfirmable()) {
            gbOrder.setGbDoPrice(confirm.getResolvedUnitPrice().toPlainString());
            gbOrder.setGbDoSubtotal(confirm.getLineSubtotal().toPlainString());
            gbOrder.setGbDoWeight(item.getQuantity());
        } else {
            gbOrder.setGbDoSubtotal("0");
        }
        return gbOrder;
    }

    private void saveDepDisGoodsAndPurchase(GbDepartmentOrdersEntity gbOrder, GbDistributerGoodsEntity gbGoods) {
        GbDepartmentDisGoodsEntity depGoods = new GbDepartmentDisGoodsEntity();
        depGoods.setGbDdgDepGoodsName(gbGoods.getGbDgGoodsName());
        depGoods.setGbDdgDisGoodsId(gbGoods.getGbDistributerGoodsId());
        depGoods.setGbDdgDisGoodsFatherId(gbGoods.getGbDgDfgGoodsFatherId());
        depGoods.setGbDdgDisGoodsGrandId(gbGoods.getGbDgDfgGoodsGrandId());
        depGoods.setGbDdgDisGoodsGreatId(gbGoods.getGbDgDfgGoodsGreatId());
        depGoods.setGbDdgDepGoodsPinyin(gbGoods.getGbDgGoodsPinyin());
        depGoods.setGbDdgDepGoodsPy(gbGoods.getGbDgGoodsPy());
        depGoods.setGbDdgDepGoodsStandardname(gbGoods.getGbDgGoodsStandardname());
        depGoods.setGbDdgDepartmentId(gbOrder.getGbDoDepartmentId());
        depGoods.setGbDdgDepartmentFatherId(gbOrder.getGbDoDepartmentFatherId());
        depGoods.setGbDdgGbDepartmentId(gbGoods.getGbDgGbDepartmentId());
        depGoods.setGbDdgGbDisId(gbGoods.getGbDgDistributerId());
        depGoods.setGbDdgGoodsType(gbGoods.getGbDgGoodsType());
        depGoods.setGbDdgStockTotalWeight("0.0");
        depGoods.setGbDdgStockTotalSubtotal("0.0");
        depGoods.setGbDdgShowStandardId(-1);
        depGoods.setGbDdgShowStandardName(gbGoods.getGbDgGoodsStandardname());
        depGoods.setGbDdgShowStandardScale("-1");
        depGoods.setGbDdgNxDistributerGoodsId(gbGoods.getGbDgNxDistributerGoodsId());
        depGoods.setGbDdgNxDistributerId(-1);
        gbDepartmentDisGoodsService.save(depGoods);
        gbOrder.setGbDoDepDisGoodsId(depGoods.getGbDepartmentDisGoodsId());

        GbDistributerPurchaseGoodsEntity purchase = new GbDistributerPurchaseGoodsEntity();
        purchase.setGbDpgPurchaseType(5);
        purchase.setGbDpgDisGoodsFatherId(gbOrder.getGbDoDisGoodsFatherId());
        purchase.setGbDpgDisGoodsId(gbOrder.getGbDoDisGoodsId());
        purchase.setGbDpgDistributerId(gbOrder.getGbDoDistributerId());
        purchase.setGbDpgApplyDate(formatWhatDay(0));
        purchase.setGbDpgStatus(getGbPurchaseGoodsStatusNew());
        purchase.setGbDpgOrdersAmount(1);
        purchase.setGbDpgOrdersFinishAmount(0);
        purchase.setGbDpgOrdersWeightAmount(0);
        purchase.setGbDpgOrdersBillAmount(0);
        purchase.setGbDpgStandard(gbOrder.getGbDoStandard());
        purchase.setGbDpgQuantity(gbOrder.getGbDoQuantity());
        purchase.setGbDpgBuyScale(gbOrder.getGbDoDsStandardScale());
        purchase.setGbDpgPurchaseDepartmentId(gbOrder.getGbDoToDepartmentId());
        purchase.setGbDpgPurchaseNxDistributerId(gbOrder.getGbDoNxDistributerId());
        if (confirmablePrice(gbOrder.getGbDoPrice())) {
            purchase.setGbDpgBuyPrice(gbOrder.getGbDoPrice());
            purchase.setGbDpgBuyQuantity(gbOrder.getGbDoQuantity());
            purchase.setGbDpgBuySubtotal(gbOrder.getGbDoSubtotal());
        }
        gbDistributerPurchaseGoodsService.save(purchase);
        gbOrder.setGbDoPurchaseGoodsId(purchase.getGbDistributerPurchaseGoodsId());
    }

    private NxDepartmentOrdersEntity buildNxOrder(
            PlatformCartLineCreateCommand command,
            PlatformCartSubmitItemRequest item,
            GbDepartmentOrdersEntity gbOrder,
            NxDistributerGoodsEntity nxGoods,
            LineAmountConfirmResult confirm) {

        Integer fatherId = command.getGbDepartmentFatherId();
        if (fatherId == null || fatherId == 0) {
            fatherId = command.getGbDepartmentId();
        }

        NxDepartmentOrdersEntity nxOrder = new NxDepartmentOrdersEntity();
        nxOrder.setNxDoDistributerId(command.getNxDistributerId());
        nxOrder.setNxDoDisGoodsId(nxGoods.getNxDistributerGoodsId());
        nxOrder.setNxDoQuantity(item.getQuantity());
        nxOrder.setNxDoStandard(item.getStandard());
        nxOrder.setNxDoRemark(gbOrder.getGbDoRemark());
        nxOrder.setNxDoApplyDate(formatWhatDay(0));
        nxOrder.setNxDoArriveOnlyDate(formatWhatDay(0));
        nxOrder.setNxDoArriveWeeksYear(getWeekOfYear(0));
        nxOrder.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        nxOrder.setNxDoApplyOnlyTime(formatWhatTime(0));
        nxOrder.setNxDoArriveDate(command.getDeliveryDate());
        nxOrder.setNxDoGbDistributerId(command.getGbDistributerId());
        nxOrder.setNxDoGbDepartmentId(command.getGbDepartmentId());
        nxOrder.setNxDoGbDepartmentFatherId(fatherId);
        nxOrder.setNxDoDepartmentId(-1);
        nxOrder.setNxDoDepartmentFatherId(-1);
        nxOrder.setNxDoNxCommunityId(-1);
        nxOrder.setNxDoNxCommRestrauntFatherId(-1);
        nxOrder.setNxDoNxCommRestrauntId(-1);
        nxOrder.setNxDoNxGoodsId(nxGoods.getNxDgNxGoodsId());
        nxOrder.setNxDoNxGoodsFatherId(nxGoods.getNxDgNxFatherId());
        nxOrder.setNxDoDisGoodsFatherId(nxGoods.getNxDgDfgGoodsFatherId());
        nxOrder.setNxDoDisGoodsGrandId(nxGoods.getNxDgDfgGoodsGrandId());
        nxOrder.setNxDoDepDisGoodsId(-1);
        nxOrder.setNxDoArriveWhatDay(getWeek(0));
        nxOrder.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        nxOrder.setNxDoGoodsType(nxGoods.getNxDgPurchaseAuto());
        nxOrder.setNxDoIsAgent(-1);
        nxOrder.setNxDoCollaborativeNxDisId(-1);
        nxOrder.setNxDoPrintStandard(nxGoods.getNxDgGoodsStandardname());
        nxOrder.setNxDoCostPrice(nxGoods.getNxDgBuyingPrice());
        nxOrder.setNxDoCostPriceUpdate(nxGoods.getNxDgBuyingPriceUpdate());
        nxOrder.setNxDoPurchaseUserId(-1);
        nxOrder.setNxDoGbDepartmentOrderId(gbOrder.getGbDepartmentOrdersId());
        nxOrder.setNxDoStatus(command.isCartOnly() ? getNxOrderStatusGouwu() : getNxOrderStatusNew());

        if (confirm.isConfirmable()) {
            nxOrder.setNxDoPrice(confirm.getResolvedUnitPrice().toPlainString());
            nxOrder.setNxDoWeight(item.getQuantity());
            nxOrder.setNxDoSubtotal(confirm.getLineSubtotal().toPlainString());
            nxOrder.setNxDoCostPriceLevel("1");
            if (StringUtils.isNotBlank(nxGoods.getNxDgBuyingPriceOne())) {
                nxOrder.setNxDoCostPrice(nxGoods.getNxDgBuyingPriceOne());
                nxOrder.setNxDoCostPriceUpdate(nxGoods.getNxDgBuyingPriceOneUpdate());
            }
            BigDecimal buySub = parseDecimal(nxOrder.getNxDoCostPrice())
                    .multiply(parseDecimal(item.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            nxOrder.setNxDoCostSubtotal(buySub.toPlainString());
        } else {
            nxOrder.setNxDoSubtotal("0");
            nxOrder.setNxDoCostSubtotal("0");
            nxOrder.setNxDoProfitSubtotal("0");
        }
        return nxOrder;
    }

    private static boolean confirmablePrice(String price) {
        return StringUtils.isNotBlank(price) && parseDecimal(price).compareTo(new BigDecimal("0.1")) > 0;
    }

    private static BigDecimal parseDecimal(String value) {
        if (StringUtils.isBlank(value)) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private static String formatMoney(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
