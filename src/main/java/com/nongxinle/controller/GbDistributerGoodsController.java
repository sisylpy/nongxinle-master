package com.nongxinle.controller;

/**
 * @author lpy
 * @date 06-18 21:32
 */

import java.math.BigDecimal;
import java.util.*;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.UploadFile;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbDepartmentTypeMendian;
import static com.nongxinle.utils.NxDistributerTypeUtils.*;
import static com.nongxinle.utils.PinYin4jUtils.*;


@RestController
@RequestMapping("api/gbdistributergoods")
public class GbDistributerGoodsController {

    @Autowired
    private GbDistributerGoodsService gbDgService;
    @Autowired
    private GbDepartmentOrdersService depOrdersService;
    @Autowired
    private NxGoodsService nxGoodsService;
    @Autowired
    private NxAliasService nxAliasService;
    @Autowired
    private NxStandardService nxStandardService;
    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;
    @Autowired
    private GbDistributerFatherGoodsService dgfService;
    @Autowired
    private GbDistributerAliasService gbDistributerAliasService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepDisGoodsService;
    @Autowired
    private GbDepartmentGoodsStockService gbDepGoodsStockService;
    @Autowired
    private GbDistributerGoodsShelfGoodsService gbDistributerGoodsShelfGoodsService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private GbDistributerService gbDistributerService;
    @Autowired
    private GbDepartmentGoodsDailyService gbDepGoodsDailyService;
    @Autowired
    private GbDepartmentGoodsStockReduceService gbDepGoodsStockReduceService;
    @Autowired
    private GbDistributerPurchaseGoodsService gbDisPurchaseGoodsService;
    @Autowired
    private NxDistributerStandardService nxDistributerStandardService;
    @Autowired
    private GbDistributerStandardService gbDistributerStandardService;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;

    @Autowired
    private NxDistributerPurchaseGoodsService nxDistributerPurchaseGoodsService;
    @Autowired
    private NxDistributerPurchaseBatchService nxDPBService;
    @Autowired
    private NxJrdhSupplierService jrdhSupplierService;
    @Autowired
    private NxJrdhUserService nxJrdhUserService;
    @Autowired
    private GbDistributerPayListService payListService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
    @Autowired
    private GbPlatformOrderBridgeService gbPlatformOrderBridgeService;


    @RequestMapping(value = "/addAutoOrderGoods", method = RequestMethod.POST)
    @ResponseBody
    public R addAutoOrderGoods(Integer supplierId, Integer goodsId) {
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDgService.queryObject(goodsId);

        gbDistributerGoodsEntity.setGbDgGbSupplierId(supplierId);
        if(supplierId == -1){
            gbDistributerGoodsEntity.setGbDgGoodsType(2);
        }else{
            gbDistributerGoodsEntity.setGbDgGoodsType(21);
        }
        gbDgService.update(gbDistributerGoodsEntity);
        return R.ok().put("data", gbDistributerGoodsEntity);
    }


    @RequestMapping(value = "/queryDisGoodsByQuickSearchWithDepId", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisGoodsByQuickSearchWithDepId(String searchStr, Integer disId, Integer depId) {
        System.out.println(searchStr);
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("depId", depId);
        map.put("searchStr", searchStr);
//        map.put("isHidden", 0);


        List<NxDistributerGoodsEntity> all = new ArrayList<>();

        String pinyinString = searchStr;
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                pinyinString = hanziToPinyin(searchStr);
            }
        }
        map.put("searchPinyin", pinyinString);
        System.out.println("sttuutnxdididisisiisisisisiisisi" + map);
        List<GbDistributerGoodsEntity> goodsEntities = gbDgService.queryDisGoodsQuickSearchStrWithDepOrders(map);
        System.out.println("goennennenssss" + goodsEntities.size());
        if (goodsEntities.size() < 100) {
            Map<String, Object> mapR = new HashMap<>();
            if (goodsEntities.size() == 0) {
                List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.queryQuickSearchNxGoods(map);
                if (nxGoodsEntities.size() < 100) {
                    if (nxGoodsEntities.size() == 0) {
                        mapR.put("nxArr", -2);
                    } else {
                        mapR.put("nxArr", nxGoodsEntities);
                    }
                }
            } else {
                mapR.put("nxArr", -1);
                mapR.put("disArr", goodsEntities);
            }
            return R.ok().put("data", mapR);
        } else {
            return R.error(-1, "jixu");
        }

    }


    @RequestMapping(value = "/getLinshiAll")
    @ResponseBody
    public R getLinshiAll() {
        List<GbDistributerEntity> gbDistributerEntities = gbDistributerService.queryListAll();
        if (gbDistributerEntities.size() > 0) {
            for (GbDistributerEntity distributerEntity : gbDistributerEntities) {
                int count = gbDgService.queryLinshiGoodsAcount(distributerEntity.getGbDistributerId());
                distributerEntity.setLinshiCout(count);
            }
        }
        return R.ok().put("data", gbDistributerEntities);

    }

    @RequestMapping(value = "/getDisGoodsByGrandIdGb", method = RequestMethod.POST)
    @ResponseBody
    public R getDisGoodsByGrandIdGb(Integer fatherId, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("grandId", fatherId);
        List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDgService.queryGoodsByParamsGb(map);
        return R.ok().put("data", distributerGoodsEntities);
    }


    @RequestMapping(value = "/saveGbDisGoodsLinshi", method = RequestMethod.POST)
    @ResponseBody
    public R saveGbDisGoodsLinshi(@RequestBody GbDistributerGoodsEntity goods) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", goods.getGbDgDistributerId());
        map.put("nxGoodsId", -1);
        map.put("goodsLevel", 2);

        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisFathersGoodsByParamsGb(map);

        GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
        System.out.println("fathhff" + fatherGoodsEntity.getGbDfgFatherGoodsName());
        goods.setGbDgDfgGoodsFatherId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
        goods.setGbDgDfgGoodsGrandId(fatherGoodsEntity.getGbDfgFathersFatherId());

        GbDistributerFatherGoodsEntity grandFather = dgfService.queryObject(fatherGoodsEntity.getGbDfgFathersFatherId());
        goods.setGbDgDfgGoodsGreatId(grandFather.getGbDfgFathersFatherId());

        String goodsName = goods.getGbDgGoodsName();
        String pinyin = hanziToPinyin(goodsName);
        String headPinyin = getHeadStringByString(goodsName, false, null);
        goods.setGbDgGoodsPinyin(pinyin);
        goods.setGbDgGoodsPy(headPinyin);
        goods.setGbDgGoodsIsHidden(0);
        goods.setGbDgGbSupplierId(-1);
        goods.setGbDgNxDistributerId(-1);
        goods.setGbDgNxDistributerGoodsId(-1);
        goods.setGbDgNxFatherImg("userImage/logo.jpg");
        goods.setGbDgGoodsIsWeight(0);
        goods.setGbDgControlPrice(0);
        goods.setGbDgControlFresh(0);
        System.out.println("savegoogog" + goods);
        gbDgService.save(goods);

        Integer gbDfgGoodsAmount = fatherGoodsEntity.getGbDfgGoodsAmount();
        fatherGoodsEntity.setGbDfgGoodsAmount(gbDfgGoodsAmount + 1);
        dgfService.update(fatherGoodsEntity);

        //savePurDepGoods
        GbDepartmentDisGoodsEntity disGoodsEntity = new GbDepartmentDisGoodsEntity();
        disGoodsEntity.setGbDdgDepGoodsName(goods.getGbDgGoodsName());
        disGoodsEntity.setGbDdgDisGoodsId(goods.getGbDistributerGoodsId());
        disGoodsEntity.setGbDdgDisGoodsFatherId(goods.getGbDgDfgGoodsFatherId());
        disGoodsEntity.setGbDdgDisGoodsGrandId(goods.getGbDgDfgGoodsGrandId());
        disGoodsEntity.setGbDdgDisGoodsGreatId(goods.getGbDgDfgGoodsGreatId());
        disGoodsEntity.setGbDdgDepGoodsPinyin(goods.getGbDgGoodsPinyin());
        disGoodsEntity.setGbDdgDepGoodsPy(goods.getGbDgGoodsPy());
        disGoodsEntity.setGbDdgDepGoodsStandardname(goods.getGbDgGoodsStandardname());
        disGoodsEntity.setGbDdgDepartmentId(goods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgDepartmentFatherId(goods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgGbDepartmentId(goods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgGoodsType(goods.getGbDgGoodsType());
        disGoodsEntity.setGbDdgStockTotalWeight("0.0");
        disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
        disGoodsEntity.setGbDdgShowStandardId(-1);
        disGoodsEntity.setGbDdgShowStandardName(goods.getGbDgGoodsStandardname());
        disGoodsEntity.setGbDdgShowStandardScale("-1");
        disGoodsEntity.setGbDdgGbDisId(goods.getGbDgDistributerId());
        disGoodsEntity.setGbDdgNxDistributerId(-1);
        disGoodsEntity.setGbDdgNxDistributerGoodsId(-1);
        disGoodsEntity.setGbDdgPrintStandard(goods.getGbDgGoodsStandardname());
        gbDepDisGoodsService.save(disGoodsEntity);


        return R.ok().put("data", goods);
    }


    @ResponseBody
    @RequestMapping(value = "/saveLinshiGoodsGb", produces = "text/html;charset=UTF-8")
    public GbDistributerGoodsEntity saveFatherGb(@RequestParam("file") MultipartFile file,
                                                 @RequestParam("goodsName") String goodsName,
                                                 @RequestParam("standard") String standard,
                                                 @RequestParam("detail") String detail,
                                                 @RequestParam("disId") Integer disId,
                                                 @RequestParam("toDepId") Integer toDepId,
                                                 @RequestParam("depId") Integer depId,
                                                 HttpSession session) {

        //1,上传图片
        String newUploadName = "goodsImage";
        String originalName = goodsName;
        originalName = originalName.replaceAll("[\\\\/:*?\"<>|]", "");
        String englishKuohao = getEnglishKuohao(originalName);
        String lastFileName = getXiegang(englishKuohao) + formatFullTime();

        String realPath = UploadFile.uploadFileName(session, newUploadName, file, lastFileName);
        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + lastFileName + ".jpg";

        GbDistributerGoodsEntity goods = new GbDistributerGoodsEntity();
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("nxGoodsId", -1);
        map.put("goodsLevel", 2);

        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisFathersGoodsByParamsGb(map);

        GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
        System.out.println("fathhff" + fatherGoodsEntity.getGbDfgFatherGoodsName());
        goods.setGbDgDfgGoodsFatherId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
        goods.setGbDgDfgGoodsGrandId(fatherGoodsEntity.getGbDfgFathersFatherId());

        GbDistributerFatherGoodsEntity grandFather = dgfService.queryObject(fatherGoodsEntity.getGbDfgFathersFatherId());
        goods.setGbDgDfgGoodsGreatId(grandFather.getGbDfgFathersFatherId());

        Integer depFatherId = goods.getGbDgGoodsSort();
        Integer depSonsId = goods.getGbDgGoodsSonsSort();

        goods.setGbDgGoodsType(2);
        goods.setGbDgGbDepartmentId(toDepId);
        goods.setGbDgDistributerId(disId);
        goods.setGbDgNxFatherImgLarge(filePath);
        goods.setGbDgNxFatherImg(filePath);
        goods.setGbDgGoodsName(goodsName);
        String pinyin = hanziToPinyin(goodsName);
        String headPinyin = getHeadStringByString(goodsName, false, null);
        goods.setGbDgGoodsPinyin(pinyin);
        goods.setGbDgGoodsPy(headPinyin);
        goods.setGbDgDistributerId(disId);
        goods.setGbDgGoodsIsHidden(0);
        goods.setGbDgGoodsStandardname(standard);
        goods.setGbDgGoodsDetail(detail);
        goods.setGbDgNxDistributerId(-1);
        goods.setGbDgNxDistributerGoodsId(-1);
        goods.setGbDgGoodsStatus(0);
        goods.setGbDgGoodsIsWeight(0);
        goods.setGbDgGoodsIsHidden(0);
        goods.setGbDgPullOff(0);
        goods.setGbDgGoodsType(2);
        goods.setGbDgGbSupplierId(-1);
        goods.setGbDgNxDistributerId(-1);
        goods.setGbDgNxDistributerGoodsId(-1);
        goods.setGbDgNxDistributerGoodsPrice("0.1");
        goods.setGbDgGbDepartmentId(toDepId);
        goods.setGbDgControlFresh(0);
        goods.setGbDgControlPrice(0);
        goods.setGbDgGoodsInventoryType(1);
        goods.setGbDgIsFranchisePrice(0);
        goods.setGbDgIsSelfControl(0);
        System.out.println("savegoogog" + goods);
        gbDgService.save(goods);

        Integer gbDfgGoodsAmount = fatherGoodsEntity.getGbDfgGoodsAmount();
        fatherGoodsEntity.setGbDfgGoodsAmount(gbDfgGoodsAmount + 1);
        dgfService.update(fatherGoodsEntity);

        //savePurDepGoods
        GbDepartmentDisGoodsEntity disGoodsEntity = new GbDepartmentDisGoodsEntity();
        disGoodsEntity.setGbDdgDepGoodsName(goods.getGbDgGoodsName());
        disGoodsEntity.setGbDdgDisGoodsId(goods.getGbDistributerGoodsId());
        disGoodsEntity.setGbDdgDisGoodsFatherId(goods.getGbDgDfgGoodsFatherId());
        disGoodsEntity.setGbDdgDisGoodsGrandId(goods.getGbDgDfgGoodsGrandId());
        disGoodsEntity.setGbDdgDisGoodsGrandId(goods.getGbDgDfgGoodsGrandId());
        disGoodsEntity.setGbDdgDepGoodsPinyin(goods.getGbDgGoodsPinyin());
        disGoodsEntity.setGbDdgDepGoodsPy(goods.getGbDgGoodsPy());
        disGoodsEntity.setGbDdgDepGoodsStandardname(goods.getGbDgGoodsStandardname());
        disGoodsEntity.setGbDdgDepartmentId(toDepId);
        disGoodsEntity.setGbDdgDepartmentFatherId(toDepId);
        disGoodsEntity.setGbDdgGbDepartmentId(goods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgGoodsType(goods.getGbDgGoodsType());
        disGoodsEntity.setGbDdgStockTotalWeight("0.0");
        disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
        disGoodsEntity.setGbDdgShowStandardId(-1);
        disGoodsEntity.setGbDdgShowStandardName(goods.getGbDgGoodsStandardname());
        disGoodsEntity.setGbDdgShowStandardScale("-1");
        disGoodsEntity.setGbDdgGbDisId(goods.getGbDgDistributerId());
        disGoodsEntity.setGbDdgPrintStandard(goods.getGbDgGoodsStandardname());
        gbDepDisGoodsService.save(disGoodsEntity);

        //saveDepGoods
        GbDepartmentDisGoodsEntity disGoodsEntityDep = new GbDepartmentDisGoodsEntity();
        disGoodsEntityDep.setGbDdgDepGoodsName(goods.getGbDgGoodsName());
        disGoodsEntityDep.setGbDdgDisGoodsId(goods.getGbDistributerGoodsId());
        disGoodsEntityDep.setGbDdgDisGoodsFatherId(goods.getGbDgDfgGoodsFatherId());
        disGoodsEntityDep.setGbDdgDisGoodsGrandId(goods.getGbDgDfgGoodsGrandId());
        disGoodsEntityDep.setGbDdgDisGoodsGreatId(goods.getGbDgDfgGoodsGreatId());
        disGoodsEntityDep.setGbDdgDepGoodsPinyin(goods.getGbDgGoodsPinyin());
        disGoodsEntityDep.setGbDdgDepGoodsPy(goods.getGbDgGoodsPy());
        disGoodsEntityDep.setGbDdgDepGoodsStandardname(goods.getGbDgGoodsStandardname());
        disGoodsEntityDep.setGbDdgDepartmentId(depSonsId);
        disGoodsEntityDep.setGbDdgDepartmentFatherId(depFatherId);
        disGoodsEntityDep.setGbDdgGbDepartmentId(goods.getGbDgGbDepartmentId());
        disGoodsEntityDep.setGbDdgGoodsType(goods.getGbDgGoodsType());
        disGoodsEntityDep.setGbDdgStockTotalWeight("0.0");
        disGoodsEntityDep.setGbDdgStockTotalSubtotal("0.0");
        disGoodsEntityDep.setGbDdgShowStandardId(-1);
        disGoodsEntityDep.setGbDdgShowStandardName(goods.getGbDgGoodsStandardname());
        disGoodsEntityDep.setGbDdgShowStandardScale("-1");
        disGoodsEntityDep.setGbDdgGbDisId(goods.getGbDgDistributerId());
        disGoodsEntity.setGbDdgPrintStandard(goods.getGbDgGoodsStandardname());
        gbDepDisGoodsService.save(disGoodsEntityDep);

        return goods;
    }


    @ResponseBody
    @RequestMapping("/saveOrdersGbJjAndSaveGoods")
    public R saveOrdersGbJjAndSaveGoods(@RequestBody GbDepartmentOrdersEntity gbDepartmentOrders) {

        Integer gbDoDepartmentId1 = gbDepartmentOrders.getGbDoToDepartmentId();
        Integer gbDoDistributerId1 = gbDepartmentOrders.getGbDoDistributerId();
        Integer nxGoodsId1 = gbDepartmentOrders.getGbDoNxGoodsId();
        GbDistributerGoodsEntity gbNewGoods = postDgnGbGoods(gbDoDistributerId1, gbDoDepartmentId1, nxGoodsId1);
        GbDistributerFatherGoodsEntity grandGoods = dgfService.queryObject(gbNewGoods.getGbDgDfgGoodsGrandId());
        Integer greatFatherGoodsId = grandGoods.getGbDfgFathersFatherId();
        //添加caigou部门商品
        GbDepartmentDisGoodsEntity disGoodsEntity = new GbDepartmentDisGoodsEntity();
        disGoodsEntity.setGbDdgDepGoodsName(gbNewGoods.getGbDgGoodsName());
        disGoodsEntity.setGbDdgDisGoodsId(gbNewGoods.getGbDistributerGoodsId());
        disGoodsEntity.setGbDdgDisGoodsFatherId(gbNewGoods.getGbDgDfgGoodsFatherId());
        disGoodsEntity.setGbDdgDisGoodsGrandId(gbNewGoods.getGbDgDfgGoodsGrandId());
        disGoodsEntity.setGbDdgDisGoodsGreatId(greatFatherGoodsId);
        disGoodsEntity.setGbDdgDepGoodsPinyin(gbNewGoods.getGbDgGoodsPinyin());
        disGoodsEntity.setGbDdgDepGoodsPy(gbNewGoods.getGbDgGoodsPy());
        disGoodsEntity.setGbDdgDepGoodsStandardname(gbNewGoods.getGbDgGoodsStandardname());
        disGoodsEntity.setGbDdgDepartmentId(gbNewGoods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgDepartmentFatherId(gbNewGoods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgGbDepartmentId(gbNewGoods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgGbDisId(gbNewGoods.getGbDgDistributerId());
        disGoodsEntity.setGbDdgGoodsType(gbNewGoods.getGbDgGoodsType());
        disGoodsEntity.setGbDdgStockTotalWeight("0.0");
        disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
        disGoodsEntity.setGbDdgShowStandardId(-1);
        disGoodsEntity.setGbDdgShowStandardName(gbNewGoods.getGbDgGoodsStandardname());
        disGoodsEntity.setGbDdgShowStandardScale("-1");
        disGoodsEntity.setGbDdgShowStandardWeight(null);
        disGoodsEntity.setGbDdgNxDistributerGoodsId(gbNewGoods.getGbDgNxDistributerGoodsId());
        disGoodsEntity.setGbDdgNxDistributerId(-1);
        disGoodsEntity.setGbDdgPrintStandard(gbNewGoods.getGbDgGoodsStandardname());
        gbDepDisGoodsService.save(disGoodsEntity);

        //添加部门商品
        GbDepartmentDisGoodsEntity mendianDisGoodsEntity = new GbDepartmentDisGoodsEntity();

        String gbDoGoodsName = gbDepartmentOrders.getGbDoGoodsName();
        mendianDisGoodsEntity.setGbDdgDepGoodsName(gbDoGoodsName);
        mendianDisGoodsEntity.setGbDdgDisGoodsId(gbNewGoods.getGbDistributerGoodsId());
        mendianDisGoodsEntity.setGbDdgDisGoodsFatherId(gbNewGoods.getGbDgDfgGoodsFatherId());
        mendianDisGoodsEntity.setGbDdgDisGoodsGrandId(gbNewGoods.getGbDgDfgGoodsGrandId());
        mendianDisGoodsEntity.setGbDdgDisGoodsGreatId(greatFatherGoodsId);

        String pinyin = hanziToPinyin(gbDoGoodsName);
        String headPinyin = getHeadStringByString(gbDoGoodsName, false, null);
        mendianDisGoodsEntity.setGbDdgDepGoodsPinyin(pinyin);
        mendianDisGoodsEntity.setGbDdgDepGoodsPy(headPinyin);
        mendianDisGoodsEntity.setGbDdgDepGoodsStandardname(gbNewGoods.getGbDgGoodsStandardname());
        mendianDisGoodsEntity.setGbDdgDepartmentId(gbDepartmentOrders.getGbDoDepartmentId());
        mendianDisGoodsEntity.setGbDdgDepartmentFatherId(gbDepartmentOrders.getGbDoDepartmentFatherId());

        mendianDisGoodsEntity.setGbDdgGbDepartmentId(gbNewGoods.getGbDgGbDepartmentId());
        mendianDisGoodsEntity.setGbDdgGbDisId(gbNewGoods.getGbDgDistributerId());
        mendianDisGoodsEntity.setGbDdgGoodsType(gbNewGoods.getGbDgGoodsType());
        mendianDisGoodsEntity.setGbDdgStockTotalWeight("0.0");
        mendianDisGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
        mendianDisGoodsEntity.setGbDdgShowStandardId(-1);
        mendianDisGoodsEntity.setGbDdgShowStandardName(gbNewGoods.getGbDgGoodsStandardname());
        mendianDisGoodsEntity.setGbDdgOrderStandard(gbDepartmentOrders.getGbDoStandard());
        mendianDisGoodsEntity.setGbDdgShowStandardScale("-1");
        mendianDisGoodsEntity.setGbDdgShowStandardWeight(null);
        mendianDisGoodsEntity.setGbDdgNxDistributerGoodsId(gbNewGoods.getGbDgNxDistributerGoodsId());
        mendianDisGoodsEntity.setGbDdgNxDistributerId(-1);
        disGoodsEntity.setGbDdgPrintStandard(gbNewGoods.getGbDgGoodsStandardname());
        gbDepDisGoodsService.save(mendianDisGoodsEntity);


        // add purchaseGoods
        gbDepartmentOrders.setGbDoPurchaseGoodsId(-1);
        gbDepartmentOrders.setGbDoApplyDate(formatWhatDay(0));
        gbDepartmentOrders.setGbDoApplyFullTime(formatWhatYearDayTime(0));
        gbDepartmentOrders.setGbDoApplyOnlyTime(formatWhatTime(0));
        gbDepartmentOrders.setGbDoArriveOnlyDate(formatWhatDate(0));
        gbDepartmentOrders.setGbDoArriveWeeksYear(getWeekOfYear(0));
        gbDepartmentOrders.setGbDoGoodsType(2);
        gbDepartmentOrders.setGbDoOrderType(2);
        gbDepartmentOrders.setGbDoDisGoodsId(gbNewGoods.getGbDistributerGoodsId());
        Integer gbDgDfgGoodsFatherId = gbNewGoods.getGbDgDfgGoodsFatherId();

        GbDistributerFatherGoodsEntity fatherGoodsEntity = dgfService.queryObject(gbDgDfgGoodsFatherId);
        Integer gbDfgFathersFatherId = fatherGoodsEntity.getGbDfgFathersFatherId();
        GbDistributerFatherGoodsEntity grandFather = dgfService.queryObject(gbDfgFathersFatherId);
        Integer greatFatherId = grandFather.getGbDfgFathersFatherId();
        GbDistributerFatherGoodsEntity greatFather = dgfService.queryObject(greatFatherId);
        System.out.println("greattttt" + greatFather);
        System.out.println("greattttt" + greatFather.getGbDfgNxGoodsId());
        gbDepartmentOrders.setGbDoDisGoodsFatherId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
        gbDepartmentOrders.setGbDoDisGoodsGrandId(fatherGoodsEntity.getGbDfgFathersFatherId());
        gbDepartmentOrders.setGbDoDisGoodsGreatId(grandFather.getGbDfgFathersFatherId());
        gbDepartmentOrders.setGbDoNxDistributerGoodsId(-1);
        gbDepartmentOrders.setGbDoNxDistributerId(-1);
        gbDepartmentOrders.setGbDoDepDisGoodsId(mendianDisGoodsEntity.getGbDepartmentDisGoodsId());

        gbDepartmentOrders.setGbDoNxGoodsGrandId(grandFather.getGbDfgNxGoodsId());
        gbDepartmentOrders.setGbDoNxGoodsGreatId(greatFather.getGbDfgNxGoodsId());

        depOrdersService.save(gbDepartmentOrders);

        //是个新采购商品
        GbDistributerPurchaseGoodsEntity gbPurchaseGoodsEntity = new GbDistributerPurchaseGoodsEntity();
        gbPurchaseGoodsEntity.setGbDpgDisGoodsFatherId(gbDepartmentOrders.getGbDoDisGoodsFatherId());
        gbPurchaseGoodsEntity.setGbDpgDisGoodsGrandId(gbDepartmentOrders.getGbDoDisGoodsGrandId());
        gbPurchaseGoodsEntity.setGbDpgDisGoodsGreatId(gbDepartmentOrders.getGbDoDisGoodsGreatId());
        gbPurchaseGoodsEntity.setGbDpgDisGoodsId(gbDepartmentOrders.getGbDoDisGoodsId());
        gbPurchaseGoodsEntity.setGbDpgDistributerId(gbDepartmentOrders.getGbDoDistributerId());
        gbPurchaseGoodsEntity.setGbDpgApplyDate(formatWhatDay(0));
        gbPurchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusNew());
        gbPurchaseGoodsEntity.setGbDpgOrdersAmount(1);
        gbPurchaseGoodsEntity.setGbDpgOrdersFinishAmount(0);
        gbPurchaseGoodsEntity.setGbDpgOrdersWeightAmount(0);
        gbPurchaseGoodsEntity.setGbDpgOrdersBillAmount(0);
        gbPurchaseGoodsEntity.setGbDpgStandard(gbDepartmentOrders.getGbDoStandard());
        gbPurchaseGoodsEntity.setGbDpgQuantity(gbDepartmentOrders.getGbDoQuantity());
        gbPurchaseGoodsEntity.setGbDpgBuyScale(gbDepartmentOrders.getGbDoDsStandardScale());
        gbPurchaseGoodsEntity.setGbDpgPurchaseDepartmentId(gbDepartmentOrders.getGbDoToDepartmentId());
        gbPurchaseGoodsEntity.setGbDpgPurchaseNxDistributerId(gbDepartmentOrders.getGbDoNxDistributerId());
        gbPurchaseGoodsEntity.setGbDpgPurchaseType(2);
        gbPurchaseGoodsEntity.setGbDpgPurchaseNxDistributerId(-1);
        gbPurchaseGoodsEntity.setGbDpgPurchaseNxSupplierId(-1);
        //standard Same
        if(gbNewGoods.getGbDgGoodsStandardname().equals(gbDepartmentOrders.getGbDoStandard())){
            gbDepartmentOrders.setGbDoWeight(gbDepartmentOrders.getGbDoQuantity());
            gbPurchaseGoodsEntity.setGbDpgBuyQuantity(gbDepartmentOrders.getGbDoQuantity());
        }
        gbDisPurchaseGoodsService.save(gbPurchaseGoodsEntity);
        Integer gbDistributerPurchaseGoodsId = gbPurchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
        gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
        depOrdersService.update(gbDepartmentOrders);
        Integer gbDistributerGoodsId = gbNewGoods.getGbDistributerGoodsId();
        List<GbDistributerStandardEntity> standardEntityList = gbDistributerStandardService.queryDisStandardByDisGoodsIdGb(gbDistributerGoodsId);
        gbNewGoods.setGbDistributerStandardEntities(standardEntityList);
        gbDepartmentOrders.setGbDistributerGoodsEntity(gbNewGoods);

        return R.ok().put("data", gbDepartmentOrders);
    }




    @ResponseBody
    @RequestMapping("/saveOrdersGbJjAndSaveGoodsSx")
    @Transactional(rollbackFor = Exception.class)
    public R saveOrdersGbJjAndSaveGoodsSx(@RequestBody GbDepartmentOrdersEntity gbDepartmentOrders) {

        Integer toDepartmentId = gbDepartmentOrders.getGbDoToDepartmentId();
        Integer gbDoDistributerId = gbDepartmentOrders.getGbDoDistributerId();
        Integer gbDoNxDistributerId = gbDepartmentOrders.getGbDoNxDistributerId();

        Integer nxGoodsId = gbDepartmentOrders.getGbDoNxGoodsId();
        //添加 gbDisGoods
        GbDistributerGoodsEntity gbNewGoods = postDgnGbGoods(gbDoDistributerId, toDepartmentId, nxGoodsId);

        gbDepartmentOrders.setGbDistributerGoodsEntity(gbNewGoods);

        if (gbDepartmentOrders.getGbDoStandard().equals(gbNewGoods.getGbDgGoodsStandardname()) && gbDepartmentOrders.getGbDoPrice() != null && !gbDepartmentOrders.getGbDoPrice().trim().isEmpty()) {
            BigDecimal subtotal = new BigDecimal(gbDepartmentOrders.getGbDoQuantity()).multiply(new BigDecimal(gbDepartmentOrders.getGbDoPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
            gbDepartmentOrders.setGbDoSubtotal(subtotal.toString());
        }
        gbDepartmentOrders.setGbDoApplyDate(formatWhatDay(0));
        gbDepartmentOrders.setGbDoApplyFullTime(formatWhatYearDayTime(0));
        gbDepartmentOrders.setGbDoApplyOnlyTime(formatWhatTime(0));
        gbDepartmentOrders.setGbDoArriveOnlyDate(formatWhatDate(0));
        gbDepartmentOrders.setGbDoArriveWeeksYear(getWeekOfYear(0));
        gbDepartmentOrders.setGbDoGoodsType(2);
        gbDepartmentOrders.setGbDoOrderType(5);
        gbDepartmentOrders.setGbDoDisGoodsId(gbNewGoods.getGbDistributerGoodsId());
        Integer gbDgDfgGoodsFatherId = gbNewGoods.getGbDgDfgGoodsFatherId();

        GbDistributerFatherGoodsEntity fatherGoodsEntity = dgfService.queryObject(gbDgDfgGoodsFatherId);

        Integer nxDoDistributerId = gbDepartmentOrders.getGbDoNxDistributerId();
        Map<String, Object> map = new HashMap<>();
        map.put("disId", nxDoDistributerId);
        map.put("nxGoodsId", nxGoodsId);
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryOneGoodsAboutNxGoods(map);

        //添加caigou部门商品
//        GbDepartmentDisGoodsEntity disGoodsEntity = new GbDepartmentDisGoodsEntity();
//        disGoodsEntity.setGbDdgDepGoodsName(gbNewGoods.getGbDgGoodsName());
//        disGoodsEntity.setGbDdgDisGoodsId(gbNewGoods.getGbDistributerGoodsId());
//        disGoodsEntity.setGbDdgDisGoodsFatherId(gbNewGoods.getGbDgDfgGoodsFatherId());
//        disGoodsEntity.setGbDdgDisGoodsGrandId(gbNewGoods.getGbDgDfgGoodsGrandId());
//        disGoodsEntity.setGbDdgDisGoodsGreatId(gbNewGoods.getGbDgDfgGoodsGreatId());
//        disGoodsEntity.setGbDdgDepGoodsPinyin(gbNewGoods.getGbDgGoodsPinyin());
//        disGoodsEntity.setGbDdgDepGoodsPy(gbNewGoods.getGbDgGoodsPy());
//        disGoodsEntity.setGbDdgDepGoodsStandardname(gbNewGoods.getGbDgGoodsStandardname());
//        disGoodsEntity.setGbDdgDepartmentId(toDepartmentId);
//        disGoodsEntity.setGbDdgDepartmentFatherId(toDepartmentId);
//        disGoodsEntity.setGbDdgGbDisId(gbNewGoods.getGbDgDistributerId());
//        disGoodsEntity.setGbDdgGoodsType(gbNewGoods.getGbDgGoodsType());
//        disGoodsEntity.setGbDdgStockTotalWeight("0.0");
//        disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
//        disGoodsEntity.setGbDdgShowStandardId(-1);
//        disGoodsEntity.setGbDdgShowStandardName(gbNewGoods.getGbDgGoodsStandardname());
//        disGoodsEntity.setGbDdgShowStandardScale("-1");
//        disGoodsEntity.setGbDdgShowStandardWeight(null);
//        disGoodsEntity.setGbDdgNxDistributerGoodsId(gbNewGoods.getGbDgNxDistributerGoodsId());
//        disGoodsEntity.setGbDdgNxDistributerId(-1);
//        disGoodsEntity.setGbDdgPrintStandard(gbDepartmentOrders.getGbDoPrintStandard());
//        gbDepDisGoodsService.save(disGoodsEntity);

        //添加部门商品
        GbDepartmentDisGoodsEntity mendianDisGoodsEntity = new GbDepartmentDisGoodsEntity();
        mendianDisGoodsEntity.setGbDdgDepGoodsName(gbNewGoods.getGbDgGoodsName());
        mendianDisGoodsEntity.setGbDdgDisGoodsId(gbNewGoods.getGbDistributerGoodsId());
        mendianDisGoodsEntity.setGbDdgDisGoodsFatherId(gbNewGoods.getGbDgDfgGoodsFatherId());
        mendianDisGoodsEntity.setGbDdgDisGoodsGrandId(gbNewGoods.getGbDgDfgGoodsGrandId());
        mendianDisGoodsEntity.setGbDdgDisGoodsGreatId(gbNewGoods.getGbDgDfgGoodsGreatId());
        mendianDisGoodsEntity.setGbDdgDepGoodsPinyin(gbNewGoods.getGbDgGoodsPinyin());
        mendianDisGoodsEntity.setGbDdgDepGoodsPy(gbNewGoods.getGbDgGoodsPy());
        mendianDisGoodsEntity.setGbDdgDepGoodsStandardname(gbNewGoods.getGbDgGoodsStandardname());

        mendianDisGoodsEntity.setGbDdgDepartmentId(gbDepartmentOrders.getGbDoDepartmentId());
        mendianDisGoodsEntity.setGbDdgDepartmentFatherId(gbDepartmentOrders.getGbDoDepartmentFatherId());
        mendianDisGoodsEntity.setGbDdgGbDepartmentId(toDepartmentId);
        mendianDisGoodsEntity.setGbDdgGbDisId(gbNewGoods.getGbDgDistributerId());
        mendianDisGoodsEntity.setGbDdgGoodsType(gbNewGoods.getGbDgGoodsType());
        mendianDisGoodsEntity.setGbDdgStockTotalWeight("0.0");
        mendianDisGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
        mendianDisGoodsEntity.setGbDdgShowStandardId(-1);
        mendianDisGoodsEntity.setGbDdgShowStandardName(gbNewGoods.getGbDgGoodsStandardname());
        mendianDisGoodsEntity.setGbDdgOrderStandard(gbDepartmentOrders.getGbDoStandard());
        mendianDisGoodsEntity.setGbDdgShowStandardScale("-1");
        mendianDisGoodsEntity.setGbDdgShowStandardWeight(null);
        mendianDisGoodsEntity.setGbDdgNxDistributerGoodsId(gbNewGoods.getGbDgNxDistributerGoodsId());
        mendianDisGoodsEntity.setGbDdgNxDistributerId(-1);
        mendianDisGoodsEntity.setGbDdgPrintStandard(gbDepartmentOrders.getGbDoPrintStandard());
        mendianDisGoodsEntity.setGbDdgOrderPriceLevel(gbDepartmentOrders.getGbDoCostPriceLevel().toString());
        gbDepDisGoodsService.save(mendianDisGoodsEntity);

        // add purchaseGoods

        Integer gbDfgFathersFatherId = fatherGoodsEntity.getGbDfgFathersFatherId();
        GbDistributerFatherGoodsEntity grandFather = dgfService.queryObject(gbDfgFathersFatherId);
        Integer greatFatherId = grandFather.getGbDistributerFatherGoodsId();
        GbDistributerFatherGoodsEntity greatFather = dgfService.queryObject(greatFatherId);

        gbDepartmentOrders.setGbDoDisGoodsFatherId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
        gbDepartmentOrders.setGbDoDisGoodsGrandId(fatherGoodsEntity.getGbDfgFathersFatherId());
        gbDepartmentOrders.setGbDoDisGoodsGreatId(greatFatherId);

        gbDepartmentOrders.setGbDoNxGoodsGrandId(grandFather.getGbDfgNxGoodsId());
        gbDepartmentOrders.setGbDoNxGoodsGreatId(greatFather.getGbDfgNxGoodsId());

        gbDepartmentOrders.setGbDoDepDisGoodsId(mendianDisGoodsEntity.getGbDepartmentDisGoodsId());
        depOrdersService.save(gbDepartmentOrders);


        //是个新采购商品
        GbDistributerPurchaseGoodsEntity gbPurchaseGoodsEntity = new GbDistributerPurchaseGoodsEntity();
        gbPurchaseGoodsEntity.setGbDpgDisGoodsFatherId(gbDepartmentOrders.getGbDoDisGoodsFatherId());
        gbPurchaseGoodsEntity.setGbDpgDisGoodsGrandId(gbDepartmentOrders.getGbDoDisGoodsGrandId());
        gbPurchaseGoodsEntity.setGbDpgDisGoodsGreatId(gbDepartmentOrders.getGbDoDisGoodsGreatId());
        gbPurchaseGoodsEntity.setGbDpgDisGoodsId(gbDepartmentOrders.getGbDoDisGoodsId());
        gbPurchaseGoodsEntity.setGbDpgDistributerId(gbDepartmentOrders.getGbDoDistributerId());
        gbPurchaseGoodsEntity.setGbDpgApplyDate(formatWhatDay(0));
        gbPurchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusProcurement());
        gbPurchaseGoodsEntity.setGbDpgOrdersAmount(1);
        gbPurchaseGoodsEntity.setGbDpgOrdersFinishAmount(0);
        gbPurchaseGoodsEntity.setGbDpgOrdersWeightAmount(0);
        gbPurchaseGoodsEntity.setGbDpgOrdersBillAmount(0);
        gbPurchaseGoodsEntity.setGbDpgStandard(gbDepartmentOrders.getGbDoStandard());
        gbPurchaseGoodsEntity.setGbDpgQuantity(gbDepartmentOrders.getGbDoQuantity());
        gbPurchaseGoodsEntity.setGbDpgBuyScale(gbDepartmentOrders.getGbDoDsStandardScale());
        gbPurchaseGoodsEntity.setGbDpgBuyPrice(gbDepartmentOrders.getGbDoPrice());
        gbPurchaseGoodsEntity.setGbDpgPurchaseDepartmentId(gbDepartmentOrders.getGbDoToDepartmentId());
        gbPurchaseGoodsEntity.setGbDpgPurchaseNxDistributerId(gbDepartmentOrders.getGbDoNxDistributerId());
        gbPurchaseGoodsEntity.setGbDpgPurchaseType(5);
        gbPurchaseGoodsEntity.setGbDpgPurchaseNxSupplierId(-1);
        gbPurchaseGoodsEntity.setGbDpgBatchId(-1);
        gbDisPurchaseGoodsService.save(gbPurchaseGoodsEntity);
        Integer gbDistributerPurchaseGoodsId = gbPurchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
        gbDepartmentOrders.setGbDoPurchaseGoodsId(gbDistributerPurchaseGoodsId);
        depOrdersService.update(gbDepartmentOrders);
//        gbDepartmentOrders.setGbDepartmentDisGoodsEntity(disGoodsEntity);

        gbDepartmentOrders.setGbDoApplyDate(formatWhatDay(0));
        gbDepartmentOrders.setGbDoApplyFullTime(formatWhatYearDayTime(0));
        gbDepartmentOrders.setGbDoApplyOnlyTime(formatWhatTime(0));
        gbDepartmentOrders.setGbDoArriveOnlyDate(formatWhatDate(0));
        gbDepartmentOrders.setGbDoArriveWeeksYear(getWeekOfYear(0));
        gbDepartmentOrders.setGbDoNxDistributerGoodsId(nxDistributerGoodsEntity.getNxDistributerGoodsId());
        String gbDoApplyArriveDate = gbDepartmentOrders.getGbDoApplyArriveDate();
        Integer nxDgDfgGoodsFatherId = nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId();
        Integer nxDgDfgGoodsGrandId = nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId();
        //
        NxDepartmentOrdersEntity ordersEntity = new NxDepartmentOrdersEntity();
        ordersEntity.setNxDoDistributerId(nxDoDistributerId);
        ordersEntity.setNxDoDisGoodsId(nxDistributerGoodsEntity.getNxDistributerGoodsId());
        ordersEntity.setNxDoQuantity(gbDepartmentOrders.getGbDoQuantity());
        ordersEntity.setNxDoStandard(gbDepartmentOrders.getGbDoStandard());
        ordersEntity.setNxDoRemark(gbDepartmentOrders.getGbDoRemark());
        ordersEntity.setNxDoPrice(gbDepartmentOrders.getGbDoPrice());
        ordersEntity.setNxDoPriceDifferent("0");
        ordersEntity.setNxDoExpectPrice(gbDepartmentOrders.getGbDoPrice());
        ordersEntity.setNxDoApplyDate(formatWhatDay(0));
        ordersEntity.setNxDoArriveOnlyDate(formatWhatDay(0));
        ordersEntity.setNxDoArriveWeeksYear(getWeekOfYear(0));
        ordersEntity.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        ordersEntity.setNxDoApplyOnlyTime(formatWhatTime(0));
        ordersEntity.setNxDoArriveDate(gbDoApplyArriveDate);
        ordersEntity.setNxDoGbDistributerId(gbDoDistributerId);
        ordersEntity.setNxDoGbDepartmentId(gbDepartmentOrders.getGbDoDepartmentId());
        ordersEntity.setNxDoGbDepartmentFatherId(gbDepartmentOrders.getGbDoDepartmentFatherId());
        ordersEntity.setNxDoDepartmentId(-1);
        ordersEntity.setNxDoDepartmentFatherId(-1);
        ordersEntity.setNxDoNxCommunityId(-1);
        ordersEntity.setNxDoNxCommRestrauntFatherId(-1);
        ordersEntity.setNxDoNxCommRestrauntId(-1);
        ordersEntity.setNxDoNxGoodsId(nxDistributerGoodsEntity.getNxDgNxGoodsId());
        ordersEntity.setNxDoNxGoodsFatherId(nxDistributerGoodsEntity.getNxDgNxFatherId());
        ordersEntity.setNxDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
        ordersEntity.setNxDoDisGoodsGrandId(nxDgDfgGoodsGrandId);
        ordersEntity.setNxDoDepDisGoodsId(-1);
        ordersEntity.setNxDoArriveWhatDay(getWeek(0));
        ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        ordersEntity.setNxDoGoodsType(nxDistributerGoodsEntity.getNxDgPurchaseAuto());
        ordersEntity.setNxDoIsAgent(-1);
        ordersEntity.setNxDoPrintStandard(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
        ordersEntity.setNxDoCostPrice(nxDistributerGoodsEntity.getNxDgBuyingPrice());
        ordersEntity.setNxDoCostPriceUpdate(nxDistributerGoodsEntity.getNxDgBuyingPriceUpdate());
        ordersEntity.setNxDoCostPriceLevel(gbDepartmentOrders.getGbDoCostPriceLevel().toString());
        ordersEntity.setNxDoPurchaseUserId(-1);
        ordersEntity.setNxDoCollaborativeNxDisId(-1);
        ordersEntity.setNxDoGbDepartmentOrderId(gbDepartmentOrders.getGbDepartmentOrdersId());
        System.out.println("oreiidid==" + ordersEntity.getNxDoGbDepartmentOrderId());
        if (nxDistributerGoodsEntity.getNxDgPurchaseAuto() == -1) {
            ordersEntity.setNxDoPurchaseGoodsId(nxDistributerGoodsEntity.getNxDgPurchaseAuto());
        } else {
            savePurGoodsAuto(ordersEntity);
        }

        if (ordersEntity.getNxDoStandard().equals(nxDistributerGoodsEntity.getNxDgGoodsStandardname())) {
            ordersEntity.setNxDoWeight(gbDepartmentOrders.getGbDoQuantity());
            gbDepartmentOrders.setGbDoWeight(gbDepartmentOrders.getGbDoQuantity());
            BigDecimal orderWeight = new BigDecimal(gbDepartmentOrders.getGbDoWeight());

            BigDecimal willPrice = new BigDecimal(0);
            BigDecimal buyingPrice = new BigDecimal(0);
            String update = "";
            String buyingPriceLevel = "";
            if (gbDepartmentOrders.getGbDoCostPriceLevel() == 1) {
                willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOne());
                buyingPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgBuyingPriceOne());
                update = nxDistributerGoodsEntity.getNxDgBuyingPriceOneUpdate();
                buyingPriceLevel = "1";
            } else if (gbDepartmentOrders.getGbDoCostPriceLevel() == 2) {
                willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwo());
                buyingPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgBuyingPriceTwo());
                update = nxDistributerGoodsEntity.getNxDgBuyingPriceTwoUpdate();
                buyingPriceLevel = "2";
            }

            BigDecimal orderSubtotal = willPrice.multiply(new BigDecimal(ordersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoSubtotal(orderSubtotal.toString());
            ordersEntity.setNxDoCostPriceLevel(buyingPriceLevel);
            ordersEntity.setNxDoCostPrice(buyingPrice.toString());
            ordersEntity.setNxDoCostPriceUpdate(update);
            BigDecimal multiply = buyingPrice.multiply(new BigDecimal(ordersEntity.getNxDoWeight()));
            ordersEntity.setNxDoCostSubtotal(multiply.toString());

            //updateGbPurGoods
            gbPurchaseGoodsEntity.setGbDpgBuyPrice(willPrice.toString());
            gbPurchaseGoodsEntity.setGbDpgBuyQuantity(gbPurchaseGoodsEntity.getGbDpgQuantity());
            BigDecimal quantity = new BigDecimal(gbPurchaseGoodsEntity.getGbDpgQuantity());
            BigDecimal subtotal = quantity.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
            gbPurchaseGoodsEntity.setGbDpgBuySubtotal(subtotal.toString());
            gbDisPurchaseGoodsService.update(gbPurchaseGoodsEntity);

        } else {
            ordersEntity.setNxDoCostSubtotal("0");
            ordersEntity.setNxDoProfitSubtotal("0");
            ordersEntity.setNxDoCostPrice("0");
        }


        ordersEntity.setNxDoStatus(getNxOrderStatusNew());
        nxDepartmentOrdersService.saveForGb(ordersEntity);
        Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();
        gbDepartmentOrders.setGbDoNxDepartmentOrderId(nxDepartmentOrdersId);

        depOrdersService.update(gbDepartmentOrders);

        NxDepartmentOrdersEntity savedNxOrder = nxDepartmentOrdersService.queryObject(nxDepartmentOrdersId);
        gbPlatformOrderBridgeService.onNxOrderCreatedFromGb(gbDepartmentOrders, savedNxOrder);

        return R.ok().put("data", savedNxOrder);
    }


    private void savePurGoodsAuto(NxDepartmentOrdersEntity ordersEntity) {

        NxDistributerPurchaseGoodsEntity resultPurGoods = new NxDistributerPurchaseGoodsEntity();

        //判断是否有已经分的
        Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
        NxDistributerGoodsEntity disGoods = nxDistributerGoodsService.queryObject(doDisGoodsId);
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", doDisGoodsId);
        map.put("status", 1);
        map.put("standard", ordersEntity.getNxDoStandard());
        System.out.println("purgogogo" + map);
        NxDistributerPurchaseGoodsEntity havePurGoods = nxDistributerPurchaseGoodsService.queryIfHavePurGoods(map);
        if (havePurGoods != null) {
            resultPurGoods = havePurGoods;
            havePurGoods.setNxDpgOrdersAmount(resultPurGoods.getNxDpgOrdersAmount() + 1);
            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(doDisGoodsId);
            if (ordersEntity.getNxDoStandard().equals(distributerGoodsEntity.getNxDgGoodsStandardname())) {
                if (havePurGoods.getNxDpgBuyQuantity() != null && !havePurGoods.getNxDpgBuyQuantity().trim().isEmpty()) {
                    BigDecimal decimal = new BigDecimal(havePurGoods.getNxDpgQuantity());
                    BigDecimal decimal1 = new BigDecimal(ordersEntity.getNxDoQuantity());
                    BigDecimal totaoWeight = decimal.add(decimal1).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(havePurGoods.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    resultPurGoods.setNxDpgQuantity(totaoWeight.toString());
                    resultPurGoods.setNxDpgBuyQuantity(totaoWeight.toString());
                    resultPurGoods.setNxDpgBuySubtotal(decimal2.toString());
                }

            }
//            else {
//                BigDecimal decimal = new BigDecimal(resultPurGoods.getNxDpgQuantity());
//                BigDecimal purQuantity = new BigDecimal(resultPurGoods.getNxDpgQuantity());
//                BigDecimal add = decimal.add(purQuantity);
//                resultPurGoods.setNxDpgQuantity(add.toString());
//                resultPurGoods.setNxDpgBuyQuantity(add.toString());
//            }
            nxDistributerPurchaseGoodsService.update(resultPurGoods);

        } else {
            resultPurGoods.setNxDpgDisGoodsFatherId(disGoods.getNxDgDfgGoodsFatherId());
            resultPurGoods.setNxDpgDisGoodsGrandId(disGoods.getNxDgDfgGoodsGrandId());
            resultPurGoods.setNxDpgDistributerId(disGoods.getNxDgDistributerId());
            resultPurGoods.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
            resultPurGoods.setNxDpgApplyDate(formatWhatYearDayTime(0));
            resultPurGoods.setNxDpgOrdersAmount(1);
            resultPurGoods.setNxDpgFinishAmount(0);
            resultPurGoods.setNxDpgPurchaseType(1);
            resultPurGoods.setNxDpgExpectPrice(disGoods.getNxDgBuyingPrice());
            resultPurGoods.setNxDpgBuyPrice(disGoods.getNxDgBuyingPrice());
            resultPurGoods.setNxDpgDisGoodsId(doDisGoodsId);
            resultPurGoods.setNxDpgInputType(disGoods.getNxDgPurchaseAuto());
            resultPurGoods.setNxDpgStandard(ordersEntity.getNxDoStandard());
            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(doDisGoodsId);
            if (ordersEntity.getNxDoStandard().equals(distributerGoodsEntity.getNxDgGoodsStandardname())) {
                BigDecimal totaoWeight = new BigDecimal(ordersEntity.getNxDoQuantity());
                BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(resultPurGoods.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                resultPurGoods.setNxDpgBuyQuantity(totaoWeight.toString());
                resultPurGoods.setNxDpgQuantity(totaoWeight.toString());
                resultPurGoods.setNxDpgBuySubtotal(decimal2.toString());
            }
            nxDistributerPurchaseGoodsService.save(resultPurGoods);

        }

        NxDistributerPurchaseBatchEntity batchEntity = new NxDistributerPurchaseBatchEntity();

        //给autoBatch更新gbDepartmentOrderid
        System.out.println("suplieridid" + disGoods.getNxDgGoodsName() + disGoods.getNxDgSupplierId());
        if (disGoods.getNxDgSupplierId() != null) {
            //
            Map<String, Object> mapBatch = new HashMap<>();
            Integer gbDgGbSupplierId = disGoods.getNxDgSupplierId();
            mapBatch.put("supplierId", gbDgGbSupplierId);
            mapBatch.put("status", 1);
            mapBatch.put("purchaseType", 2);

            List<NxDistributerPurchaseBatchEntity> entities = nxDPBService.queryDisPurchaseBatch(mapBatch);
            if (entities.size() == 0) {
                //
                batchEntity.setNxDpbDate(formatWhatDay(0));
                batchEntity.setNxDpbTime(formatWhatTime(0));
                batchEntity.setNxDpbMonth(formatWhatMonth(0));
                batchEntity.setNxDpbPruchaseWeek(getWeek(0));
                batchEntity.setNxDpbYear(formatWhatYear(0));
                batchEntity.setNxDpbPayFullTime(formatWhatYearDayTime(0));
                batchEntity.setNxDpbStatus(-1);
                batchEntity.setNxDpbPurchaseType(2);
                batchEntity.setNxDpbSupplierId(gbDgGbSupplierId);
                NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(gbDgGbSupplierId);
                batchEntity.setNxDpbSellUserId(supplierEntity.getNxJrdhsUserId());
                batchEntity.setNxDpbDistributerId(ordersEntity.getNxDoDistributerId());
                batchEntity.setNxDpbPurUserId(supplierEntity.getNxJrdhsNxPurUserId());
                batchEntity.setNxDpbBuyUserId(supplierEntity.getNxJrdhsNxJrdhBuyUserId());
                NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(supplierEntity.getNxJrdhsNxJrdhBuyUserId());
                batchEntity.setNxDpbBuyUserOpenId(nxJrdhUserEntity.getNxJrdhWxOpenId());
                nxDPBService.save(batchEntity);

                resultPurGoods.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
                resultPurGoods.setNxDpgPurchaseDate(formatWhatDay(0));
                resultPurGoods.setNxDpgTime(formatWhatYearDayTime(0));
                resultPurGoods.setNxDpgDistributerId(batchEntity.getNxDpbDistributerId());
                resultPurGoods.setNxDpgStatus(getNxDisPurchaseGoodsWithBatch());
                nxDistributerPurchaseGoodsService.update(resultPurGoods);
            } else {
                batchEntity = entities.get(0);
                resultPurGoods.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
                nxDistributerPurchaseGoodsService.update(resultPurGoods);
            }

            Map<String, TemplateData> mapNotice = new HashMap<>();
            mapNotice.put("thing1", new TemplateData(disGoods.getNxDgGoodsName()));
            mapNotice.put("time6", new TemplateData(formatWhatDay(0)));
            Integer nxDpbDistributerId = batchEntity.getNxDpbDistributerId();
            NxDistributerEntity distributerEntity = nxDistributerService.queryObject(nxDpbDistributerId);
            mapNotice.put("thing7", new TemplateData(distributerEntity.getNxDistributerName()));
            mapNotice.put("thing8", new TemplateData(distributerEntity.getNxDistributerPhone()));
//
            StringBuilder pathBuilder = new StringBuilder("pages/txs/disOrderBatch/disOrderBatch");
            pathBuilder.append("?batchId=").append(batchEntity.getNxDistributerPurchaseBatchId());
            pathBuilder.append("&retName=").append(nxDistributerService.queryObject(batchEntity.getNxDpbDistributerId()).getNxDistributerName());
            pathBuilder.append("&disId=").append(batchEntity.getNxDpbDistributerId());
            pathBuilder.append("&buyUserId=").append(batchEntity.getNxDpbBuyUserId());
            pathBuilder.append("&purUserId=").append(batchEntity.getNxDpbPurUserId());
            pathBuilder.append("&fromBuyer=1");
            String path = pathBuilder.toString();
            System.out.println("paththhththt" + path);

            NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(disGoods.getNxDgSupplierId());
            Integer nxJrdhsUserId = supplierEntity.getNxJrdhsUserId();
            NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(nxJrdhsUserId);
            System.out.println("suppsleir" + path);
            WeNoticeService.nxSupplierOrderSave(nxJrdhUserEntity.getNxJrdhWxOpenId(), path, mapNotice);
            ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        }

        ordersEntity.setNxDoPurchaseGoodsId(resultPurGoods.getNxDistributerPurchaseGoodsId());
        nxDepartmentOrdersService.update(ordersEntity);

    }


//    private void savePurGoodsAuto(NxDepartmentOrdersEntity ordersEntity) {
//
//
//        Integer nxDistributerPurchaseGoodsId = 0;
//        //判断是否有已经分的
//
//        Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
//        NxDistributerGoodsEntity disGoods = nxDistributerGoodsService.queryObject(doDisGoodsId);
//        Map<String, Object> map = new HashMap<>();
//        map.put("disGoodsId", doDisGoodsId);
//        map.put("status", 2);
//        NxDistributerPurchaseGoodsEntity havePurGoods = nxDistributerPurchaseGoodsService.queryIfHavePurGoods(map);
//        if (havePurGoods != null) {
//            havePurGoods.setNxDpgOrdersAmount(havePurGoods.getNxDpgOrdersAmount() + 1);
//            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(doDisGoodsId);
//            if (ordersEntity.getNxDoStandard().equals(distributerGoodsEntity.getNxDgGoodsStandardname())) {
//                if (havePurGoods.getNxDpgBuyQuantity() != null) {
//                    BigDecimal decimal = new BigDecimal(havePurGoods.getNxDpgQuantity());
//                    BigDecimal decimal1 = new BigDecimal(ordersEntity.getNxDoQuantity());
//                    BigDecimal totaoWeight = decimal.add(decimal1).setScale(1, BigDecimal.ROUND_HALF_UP);
//                    BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(havePurGoods.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
//                    havePurGoods.setNxDpgQuantity(totaoWeight.toString());
//                    havePurGoods.setNxDpgBuyQuantity(totaoWeight.toString());
//                    havePurGoods.setNxDpgBuySubtotal(decimal2.toString());
//                }
//            }
//
//            nxDistributerPurchaseGoodsService.update(havePurGoods);
//            nxDistributerPurchaseGoodsId = havePurGoods.getNxDistributerPurchaseGoodsId();
//
//        } else {
//
//            NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = new NxDistributerPurchaseGoodsEntity();
//            purchaseGoodsEntity.setNxDpgDisGoodsFatherId(disGoods.getNxDgDfgGoodsFatherId());
//            purchaseGoodsEntity.setNxDpgDisGoodsGrandId(disGoods.getNxDgDfgGoodsGrandId());
//            purchaseGoodsEntity.setNxDpgDistributerId(disGoods.getNxDgDistributerId());
//            purchaseGoodsEntity.setNxDpgApplyDate(formatWhatDay(0));
//            purchaseGoodsEntity.setNxDpgCostLevel(disGoods.getNxDgBuyingPriceIsGrade());
//            purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
//            purchaseGoodsEntity.setNxDpgApplyDate(formatWhatYearDayTime(0));
//            purchaseGoodsEntity.setNxDpgOrdersAmount(1);
//            purchaseGoodsEntity.setNxDpgFinishAmount(0);
//            purchaseGoodsEntity.setNxDpgPurchaseType(1);
//            purchaseGoodsEntity.setNxDpgExpectPrice(disGoods.getNxDgBuyingPrice());
//            purchaseGoodsEntity.setNxDpgBuyPrice(disGoods.getNxDgBuyingPrice());
//            purchaseGoodsEntity.setNxDpgDisGoodsId(doDisGoodsId);
//            purchaseGoodsEntity.setNxDpgInputType(disGoods.getNxDgPurchaseAuto());
//            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(doDisGoodsId);
//            if (ordersEntity.getNxDoStandard().equals(distributerGoodsEntity.getNxDgGoodsStandardname())) {
//                purchaseGoodsEntity.setNxDpgQuantity(ordersEntity.getNxDoQuantity());
//                BigDecimal totaoWeight = new BigDecimal(ordersEntity.getNxDoQuantity());
//                purchaseGoodsEntity.setNxDpgStandard(ordersEntity.getNxDoStandard());
//                BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(purchaseGoodsEntity.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
//                purchaseGoodsEntity.setNxDpgQuantity(totaoWeight.toString());
//                purchaseGoodsEntity.setNxDpgBuyQuantity(totaoWeight.toString());
//                purchaseGoodsEntity.setNxDpgBuySubtotal(decimal2.toString());
//            }
//            nxDistributerPurchaseGoodsService.save(purchaseGoodsEntity);
//            nxDistributerPurchaseGoodsId = purchaseGoodsEntity.getNxDistributerPurchaseGoodsId();
//
//            //给autoBatch更新gbDepartmentOrderid
//            if (disGoods.getNxDgSupplierId() != null) {
//                NxDistributerPurchaseBatchEntity noticeBatch = new NxDistributerPurchaseBatchEntity();
//                //
//                Map<String, Object> mapBatch = new HashMap<>();
//                Integer gbDgGbSupplierId = disGoods.getNxDgSupplierId();
//                mapBatch.put("supplierId", gbDgGbSupplierId);
//                mapBatch.put("status", 1);
//                mapBatch.put("purchaseType", 2);
//
//                List<NxDistributerPurchaseBatchEntity> entities = nxDPBService.queryDisPurchaseBatch(mapBatch);
//                System.out.println("enenennennenennene" + entities.size() + "map" + mapBatch );
//                if (entities.size() == 0) {
//                    //
//                    NxDistributerPurchaseBatchEntity batchEntity = new NxDistributerPurchaseBatchEntity();
//                    batchEntity.setNxDpbDate(formatWhatDay(0));
//                    batchEntity.setNxDpbTime(formatWhatTime(0));
//                    batchEntity.setNxDpbMonth(formatWhatMonth(0));
//                    batchEntity.setNxDpbPruchaseWeek(getWeek(0));
//                    batchEntity.setNxDpbYear(formatWhatYear(0));
//                    batchEntity.setNxDpbPayFullTime(formatWhatYearDayTime(0));
//                    batchEntity.setNxDpbStatus(-1);
//                    batchEntity.setNxDpbPurchaseType(2);
//                    batchEntity.setNxDpbSupplierId(-1);
//                    NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(gbDgGbSupplierId);
//                    batchEntity.setNxDpbSellUserId(supplierEntity.getNxJrdhsUserId());
//                    batchEntity.setNxDpbDistributerId(ordersEntity.getNxDoDistributerId());
//                    batchEntity.setNxDpbPurUserId(supplierEntity.getNxJrdhsNxPurUserId());
//                    batchEntity.setNxDpbBuyUserId(supplierEntity.getNxJrdhsNxJrdhBuyUserId());
//                    NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(supplierEntity.getNxJrdhsNxJrdhBuyUserId());
//                    batchEntity.setNxDpbBuyUserOpenId(nxJrdhUserEntity.getNxJrdhWxOpenId());
//                    nxDPBService.save(batchEntity);
//
//                    purchaseGoodsEntity.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
//                    purchaseGoodsEntity.setNxDpgStatus(getGbPurchaseGoodsStatusProcurement());
//                    purchaseGoodsEntity.setNxDpgPurchaseDate(formatWhatDay(0));
//                    purchaseGoodsEntity.setNxDpgTime(formatWhatYearDayTime(0));
//                    purchaseGoodsEntity.setNxDpgDistributerId(batchEntity.getNxDpbDistributerId());
//                    nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
//                    noticeBatch = batchEntity;
//                } else {
//                    NxDistributerPurchaseBatchEntity batchEntity = entities.get(0);
//                    purchaseGoodsEntity.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
//                    purchaseGoodsEntity.setNxDpgStatus(getGbPurchaseGoodsStatusProcurement());
//                    nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
//                    noticeBatch = batchEntity;
//                }
//                ordersEntity.setNxDoPurchaseStatus(1);
//
//                //todo
//                Map<String, TemplateData> mapNotice = new HashMap<>();
//                mapNotice.put("thing1", new TemplateData(disGoods.getNxDgGoodsName()));
//                mapNotice.put("time6", new TemplateData(formatWhatDay(0)));
//                Integer nxDpbDistributerId = noticeBatch.getNxDpbDistributerId();
//                NxDistributerEntity distributerEntity = nxDistributerService.queryObject(nxDpbDistributerId);
//                mapNotice.put("thing7", new TemplateData(distributerEntity.getNxDistributerName()));
//                mapNotice.put("thing8", new TemplateData(distributerEntity.getNxDistributerPhone()));
//                StringBuilder pathBuilder = new StringBuilder("pages/txs/disOrderBatch/disOrderBatch");
//                pathBuilder.append("?batchId=").append(noticeBatch.getNxDistributerPurchaseBatchId());
//                pathBuilder.append("&retName=").append(distributerEntity.getNxDistributerName());
//                pathBuilder.append("&disId=").append(noticeBatch.getNxDpbDistributerId());
//                pathBuilder.append("&buyUserId=").append(noticeBatch.getNxDpbBuyUserId());
//                pathBuilder.append("&purUserId=").append(noticeBatch.getNxDpbPurUserId());
//                pathBuilder.append("&fromBuyer=1");
//                String path = pathBuilder.toString();
//                System.out.println("paththhththt" + path);
//
//                NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(disGoods.getNxDgSupplierId());
//                Integer nxJrdhsUserId = supplierEntity.getNxJrdhsUserId();
//                NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(nxJrdhsUserId);
//                System.out.println("suppsleir" + path) ;
//                WeNoticeService.nxSupplierOrderSave(nxJrdhUserEntity.getNxJrdhWxOpenId(), path, mapNotice);
//
//
//            }
//
//        }
//        ordersEntity.setNxDoPurchaseGoodsId(nxDistributerPurchaseGoodsId);
//        nxDepartmentOrdersService.update(ordersEntity);
//
//
//    }



    public GbDistributerGoodsEntity postDgnGbGoods(Integer gbDisId, Integer depId, Integer nxGoodsId) {

        NxGoodsEntity nxGoodsEntity = nxGoodsService.queryObject(nxGoodsId);
        GbDistributerGoodsEntity cgnGoods = new GbDistributerGoodsEntity();
        cgnGoods.setGbDgGoodsName(nxGoodsEntity.getNxGoodsName());
        cgnGoods.setGbDgGoodsStandardname(nxGoodsEntity.getNxGoodsStandardname());
        cgnGoods.setGbDgGoodsPy(nxGoodsEntity.getNxGoodsPy());
        cgnGoods.setGbDgGoodsPinyin(nxGoodsEntity.getNxGoodsPinyin());
        cgnGoods.setGbDgGoodsStandardWeight(nxGoodsEntity.getNxGoodsStandardWeight());
        cgnGoods.setGbDgGoodsDetail(nxGoodsEntity.getNxGoodsDetail());
        cgnGoods.setGbDgGoodsBrand(nxGoodsEntity.getNxGoodsBrand());
        cgnGoods.setGbDgGoodsPlace(nxGoodsEntity.getNxGoodsPlace());
        cgnGoods.setGbDgGoodsSort(nxGoodsEntity.getNxGoodsSort());
        cgnGoods.setGbDgGoodsSonsSort(nxGoodsEntity.getNxGoodsSonsSort());
        cgnGoods.setGbDgNxFatherImgLarge(nxGoodsEntity.getNxGoodsFileBig());
        cgnGoods.setGbDgNxFatherImg(nxGoodsEntity.getNxGoodsFile());
        cgnGoods.setGbDgNxGoodsFatherColor(nxGoodsEntity.getColor());
        cgnGoods.setGbDgDistributerId(gbDisId);
        cgnGoods.setGbDgGoodsStatus(0);
        cgnGoods.setGbDgGoodsIsWeight(0);
        cgnGoods.setGbDgGoodsIsHidden(0);
        cgnGoods.setGbDgNxGoodsId(nxGoodsEntity.getNxGoodsId());
        cgnGoods.setGbDgNxFatherId(nxGoodsEntity.getNxGoodsFatherId());
        cgnGoods.setGbDgNxGrandId(nxGoodsEntity.getNxGoodsGrandId());
        cgnGoods.setGbDgPullOff(0);
        cgnGoods.setGbDgGoodsType(2);
        cgnGoods.setGbDgGbSupplierId(-1);
        cgnGoods.setGbDgNxDistributerId(-1);
        cgnGoods.setGbDgNxDistributerGoodsId(-1);
        cgnGoods.setGbDgNxDistributerGoodsPrice("0.1");
        cgnGoods.setGbDgGbDepartmentId(depId);
        cgnGoods.setGbDgControlFresh(0);
        cgnGoods.setGbDgControlPrice(0);
        cgnGoods.setGbDgGoodsInventoryType(1);
        cgnGoods.setGbDgIsFranchisePrice(0);
        cgnGoods.setGbDgIsSelfControl(0);
        cgnGoods.setGbDgNxFatherImg(nxGoodsEntity.getNxGoodsFile());
        cgnGoods.setGbDgNxFatherImgLarge(nxGoodsEntity.getNxGoodsFileBig());

        GbDistributerGoodsEntity disGoods = saveDisGoods(cgnGoods);

        //2.2
        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", nxGoodsId);
        List<NxAliasEntity> aliasEntities = nxAliasService.queryNxAliasList(map);
        if (aliasEntities.size() > 0) {
            for (NxAliasEntity aliasEntity : aliasEntities) {
                GbDistributerAliasEntity disAlias = new GbDistributerAliasEntity();
                disAlias.setGbDaDisGoodsId(disGoods.getGbDistributerGoodsId());
                disAlias.setGbDaAliasName(aliasEntity.getNxAliasName());
                gbDistributerAliasService.save(disAlias);
            }
        }

        List<NxStandardEntity> nxStandardEntities = nxStandardService.queryGoodsStandardListByGoodId(nxGoodsId);
        if (nxStandardEntities.size() > 0) {
            for (NxStandardEntity standardEntity : nxStandardEntities) {
                GbDistributerStandardEntity distributerStandardEntity = new GbDistributerStandardEntity();
                distributerStandardEntity.setGbDsDisGoodsId(disGoods.getGbDistributerGoodsId());
                distributerStandardEntity.setGbDsStandardName(standardEntity.getNxStandardName());
                gbDistributerStandardService.save(distributerStandardEntity);
            }
        }

        return disGoods;
    }


    @RequestMapping(value = "/getDisLinshiGoodsGb/{disId}")
    @ResponseBody
    public R getDisLinshiGoodsGb(@PathVariable Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("level", 2);
        map.put("disId", disId);
        map.put("nxGoodsId", 0);

        GbDistributerFatherGoodsEntity fatherGoodsEntity = dgfService.queryAppFatherGoods(map);

        Map<String, Object> mapG = new HashMap<>();
        mapG.put("dgFatherId", fatherGoodsEntity.getGbDistributerFatherGoodsId());
        List<GbDistributerGoodsEntity> goodsEntities = gbDgService.queryDisGoodsByParams(mapG);
        return R.ok().put("data", goodsEntities);
    }


    @RequestMapping(value = "/disExchangeDisGoodsGb", method = RequestMethod.POST)
    @ResponseBody
    public R disExchangeDisGoodsGb(Integer lsGoodsId, Integer nxGoodsId, Integer disId) {

        GbDistributerGoodsEntity nxDistributerGoodsEntity = gbDgService.queryObject(nxGoodsId);
        GbDistributerGoodsEntity linshiGoods = gbDgService.queryObject(lsGoodsId);


        Integer nxDgDfgGoodsFatherId = nxDistributerGoodsEntity.getGbDgDfgGoodsFatherId();
        Integer nxDgDfgGoodsGrandId = nxDistributerGoodsEntity.getGbDgDfgGoodsGrandId();
        Integer nxDgDfgGoodsGreatId = nxDistributerGoodsEntity.getGbDgDfgGoodsGreatId();

        Integer nxDgNxGoodsId = nxDistributerGoodsEntity.getGbDgNxGoodsId();
        Integer nxDgNxFatherId = nxDistributerGoodsEntity.getGbDgNxFatherId();

        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", lsGoodsId);
        List<GbDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(map);
        if (ordersEntities.size() > 0) {
            for (GbDepartmentOrdersEntity ordersEntity : ordersEntities) {
                ordersEntity.setGbDoDisGoodsId(nxGoodsId);
                ordersEntity.setGbDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
                ordersEntity.setGbDoDisGoodsGrandId(nxDgDfgGoodsGrandId);
                ordersEntity.setGbDoDisGoodsGreatId(nxDgDfgGoodsGreatId);

                ordersEntity.setGbDoNxGoodsId(nxDgNxGoodsId);
                ordersEntity.setGbDoNxGoodsFatherId(nxDgNxFatherId);
                depOrdersService.update(ordersEntity);

            }

            List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepDisGoodsService.queryGbDepDisGoodsByParams(map);
            if (departmentDisGoodsEntities.size() > 0) {
                for (GbDepartmentDisGoodsEntity depGoodsEntity : departmentDisGoodsEntities) {
                    System.out.println("delelleledepgodiss" + depGoodsEntity.getGbDdgDepGoodsName());
                    depGoodsEntity.setGbDdgDisGoodsId(nxGoodsId);
                    depGoodsEntity.setGbDdgDisGoodsFatherId(nxDgDfgGoodsFatherId);
                    depGoodsEntity.setGbDdgDisGoodsGrandId(nxDgDfgGoodsGrandId);
                    depGoodsEntity.setGbDdgDisGoodsGreatId(nxDgDfgGoodsGreatId);
                    gbDepDisGoodsService.update(depGoodsEntity);

                }
            }


            Map<String, Object> mapNx = new HashMap<>();
            mapNx.put("disGoodsId", nxGoodsId);
            int same = 0;
            List<GbDistributerStandardEntity> nxStandardEntities = gbDistributerStandardService.queryDisStandardByParams(mapNx);
            if (nxStandardEntities.size() > 0) {
                for (GbDistributerStandardEntity nxDistributerStandardEntity : nxStandardEntities) {
                    if (linshiGoods.getGbDgGoodsStandardname().equals(nxDistributerStandardEntity.getGbDsStandardName())) {
                        same = same + 1;
                    }
                }
                if (nxDistributerGoodsEntity.getGbDgGoodsStandardname().equals(linshiGoods.getGbDgGoodsStandardname())) {
                    same = same + 1;
                }
            }
            String nxDgGoodsStandardname = nxDistributerGoodsEntity.getGbDgGoodsStandardname();
            String nxDgGoodsStandardname1 = linshiGoods.getGbDgGoodsStandardname();
            if (nxDgGoodsStandardname.equals(nxDgGoodsStandardname1)) {
                same = same + 1;
            }
            if (same == 0) {
                GbDistributerStandardEntity nxDistributerStandardEntity = new GbDistributerStandardEntity();
                nxDistributerStandardEntity.setGbDsStandardName(linshiGoods.getGbDgGoodsStandardname());
                nxDistributerStandardEntity.setGbDsDisGoodsId(nxGoodsId);
                nxDistributerStandardEntity.setGbDsStandardSort(nxStandardEntities.size());
                gbDistributerStandardService.save(nxDistributerStandardEntity);
            }


            List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDisPurchaseGoodsService.queryOnlyPurGoods(map);
            if (purchaseGoodsEntities.size() > 0) {
                for (GbDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                    purchaseGoodsEntity.setGbDpgDisGoodsId(nxGoodsId);
                    purchaseGoodsEntity.setGbDpgDisGoodsFatherId(nxDgDfgGoodsFatherId);
                    purchaseGoodsEntity.setGbDpgDisGoodsGrandId(nxDgDfgGoodsGrandId);
                    purchaseGoodsEntity.setGbDpgDisGoodsGreatId(nxDgDfgGoodsGreatId);
                    System.out.println("purgogogoooogoggo" + purchaseGoodsEntity.getGbDistributerPurchaseGoodsId());
                    gbDisPurchaseGoodsService.update(purchaseGoodsEntity);
                }
            }

            List<GbDepartmentGoodsStockEntity> departmentGoodsStockEntities = gbDepGoodsStockService.queryGoodsStockByParams(map);
            if (departmentGoodsStockEntities.size() > 0) {
                for (GbDepartmentGoodsStockEntity stockEntity : departmentGoodsStockEntities) {
                    stockEntity.setGbDgsGbDisGoodsId(nxGoodsId);
                    stockEntity.setGbDgsGbDisGoodsFatherId(nxDgDfgGoodsFatherId);
                    stockEntity.setGbDgsGbDisGoodsGrandId(nxDgDfgGoodsGrandId);
                    stockEntity.setGbDgsGbDisGoodsGreatId(nxDistributerGoodsEntity.getGbDgDfgGoodsGreatId());
                    gbDepGoodsStockService.update(stockEntity);
                }
            }
            List<GbDepartmentGoodsStockReduceEntity> reduceEntities = gbDepGoodsStockReduceService.queryStockReduceListByParams(map);
            if (reduceEntities.size() > 0) {
                for (GbDepartmentGoodsStockReduceEntity reduceEntity : reduceEntities) {
                    reduceEntity.setGbDgsrGbDisGoodsId(nxGoodsId);
                    reduceEntity.setGbDgsrGbDisGoodsFatherId(nxDgDfgGoodsFatherId);
                    reduceEntity.setGbDgsrGbDisGoodsGrandId(nxDgDfgGoodsGrandId);
                    reduceEntity.setGbDgsrGbDisGoodsGreatId(nxDgDfgGoodsGreatId);
                    gbDepGoodsStockReduceService.update(reduceEntity);
                }
            }

            List<GbDepartmentGoodsDailyEntity> departmentGoodsDailyEntities = gbDepGoodsDailyService.queryDepGoodsDailyListByParams(map);

            if (departmentGoodsDailyEntities.size() > 0) {
                for (GbDepartmentGoodsDailyEntity dailyEntity : departmentGoodsDailyEntities) {
                    dailyEntity.setGbDgdGbDisGoodsId(nxGoodsId);
                    dailyEntity.setGbDgdGbDisGoodsFatherId(nxDgDfgGoodsFatherId);
                    dailyEntity.setGbDgdGbDisGoodsGrandId(nxDgDfgGoodsGrandId);
                    GbDistributerFatherGoodsEntity fatherGoodsEntity = dgfService.queryObject(nxDgDfgGoodsGrandId);
                    dailyEntity.setGbDgdGbDisGoodsGreatGrandId(fatherGoodsEntity.getGbDfgFathersFatherId());
                    System.out.println("dailsisss" + dailyEntity.getGbDepartmentGoodsDailyId());
                    gbDepGoodsDailyService.update(dailyEntity);
                }
            }
        }


        nxDistributerGoodsEntity.setGbDgNxDistributerId(linshiGoods.getGbDgNxDistributerId());
        nxDistributerGoodsEntity.setGbDgNxDistributerGoodsId(linshiGoods.getGbDgNxDistributerGoodsId());
        nxDistributerGoodsEntity.setGbDgGbSupplierId(linshiGoods.getGbDgGbSupplierId());
        nxDistributerGoodsEntity.setGbDgNxDistributerGoodsPrice(linshiGoods.getGbDgNxDistributerGoodsPrice());
        nxDistributerGoodsEntity.setGbDgGoodsType(linshiGoods.getGbDgGoodsType());
        nxDistributerGoodsEntity.setGbDgGbDepartmentId(linshiGoods.getGbDgGbDepartmentId());
        nxDistributerGoodsEntity.setGbDgFreshWarnHour(linshiGoods.getGbDgFreshWarnHour());
        nxDistributerGoodsEntity.setGbDgFreshWasteHour(linshiGoods.getGbDgFreshWasteHour());
        gbDgService.update(nxDistributerGoodsEntity);

        GbDistributerAliasEntity aliasEntity = new GbDistributerAliasEntity();
        aliasEntity.setGbDaAliasName(linshiGoods.getGbDgGoodsName());
        aliasEntity.setGbDaDisGoodsId(nxDistributerGoodsEntity.getGbDistributerGoodsId());
        gbDistributerAliasService.save(aliasEntity);

        gbDgService.delete(lsGoodsId);


        System.out.println("mapppapap" + map);
        GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(disId);
        GbDistributerPayListEntity payListEntity = new GbDistributerPayListEntity();
        payListEntity.setGbNdplPaySubtotal("10");
        payListEntity.setGbNdplPayTime(formatFullTime());
        payListEntity.setGbNdplPayDate(formatWhatDay(0));
        payListEntity.setGbNdplPayMonth(formatWhatMonth(0));
        payListEntity.setGbNdplPayYear(formatWhatYear(0));
        payListEntity.setGbNdplStatus(0);
        payListEntity.setGbNdplType(getGbDisPayGoodsAdd());
        payListEntity.setGbNdplRestPoints(gbDistributerEntity.getGbDistributerBuyQuantity());
        payListEntity.setGbNdplGbDisId(disId);
        payListEntity.setGbNdplNxSupplierId(-1);
        payListEntity.setGbNdplGbPbId(-1);
        payListEntity.setGbNdplGbDisId(nxGoodsId);
        payListService.save(payListEntity);

        BigDecimal decimal = new BigDecimal(gbDistributerEntity.getGbDistributerBuyQuantity());
        System.out.println("decimdall========" + decimal);
        BigDecimal decimal1 = new BigDecimal(10);
        System.out.println("nxdkkddkdk00" + gbDistributerEntity.getGbDistributerBuyQuantity());
        BigDecimal add = decimal.subtract(decimal1);
        gbDistributerEntity.setGbDistributerBuyQuantity(add.toString());
        System.out.println("nxdkkddkdk11" + gbDistributerEntity.getGbDistributerBuyQuantity());
        gbDistributerService.update(gbDistributerEntity);

        return R.ok();
    }

    @RequestMapping(value = "/depGetGbAppointSupplierGoods/{supplierId}")
    @ResponseBody
    public R depGetGbAppointSupplierGoods(@PathVariable Integer supplierId) {
  Map<String, Object> map = new HashMap<>();
        map.put("supplierId", supplierId);
        map.put("limit", 10000);
        map.put("offset", 0);
        TreeSet<GbDistributerGoodsEntity> goodsEntities = gbDepartmentDisGoodsService.disQueryDisGoodsWithOrderForAiTree(map);

        return R.ok().put("data", goodsEntities);
    }


    @RequestMapping(value = "/purchaserGetGoodsList", method = RequestMethod.POST)
    @ResponseBody
    public R purchaserGetGoodsList(Integer fatherId, Integer inventoryType,
                                   Integer limit, Integer page,
                                   Integer toDepId, Integer type) {
        Map<String, Object> map = new HashMap<>();
        map.put("dgFatherId", fatherId);
        if (inventoryType != -1) {
            map.put("inventoryType", inventoryType);
        }
        if (toDepId != -1) {
            map.put("toDepId", toDepId);
        }
        if (type != -1) {
            map.put("type", 3);
        }
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        System.out.println(map);
        List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDgService.queryPurchaserDisGoodsByParams(map);
        int total = gbDgService.queryDisGoodsCount(map);
        PageUtils pageUtil = new PageUtils(gbDistributerGoodsEntities, total, limit, page);
        return R.ok().put("page", pageUtil);
    }

    @RequestMapping(value = "/purchaserGetGoodsFatherWithSub", method = RequestMethod.POST)
    @ResponseBody
    public R purchaserGetGoodsFatherWithSub(Integer disId, Integer toDepId, Integer type) {

        Map<String, Object> map = new HashMap<>();
        if (type != -1) {
            map.put("type", 3);
        }
        if (toDepId != -1) {
            map.put("toDepId", toDepId);
        }
        if (disId != -1) {
            map.put("disId", disId);
        }
        System.out.println(map);
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDgService.queryDisFatherGoodsByParams(map);


        if (fatherGoodsEntities.size() > 0) {
            List<GbDistributerFatherGoodsEntity> newList = new ArrayList<>();
            for (GbDistributerFatherGoodsEntity greatGrandGoods : fatherGoodsEntities) {
                for (GbDistributerFatherGoodsEntity grandGoods : greatGrandGoods.getFatherGoodsEntities()) {
                    for (GbDistributerFatherGoodsEntity fatherGoods : grandGoods.getFatherGoodsEntities()) {
                        StringBuilder builder = new StringBuilder();
                        List<GbDistributerGoodsEntity> goodsEntities = gbDgService.queryDgSubNameByFatherIdGb(fatherGoods.getGbDistributerFatherGoodsId());
                        for (GbDistributerGoodsEntity goods : goodsEntities) {
                            String nxGoodsName = goods.getGbDgGoodsName();
                            builder.append(nxGoodsName);
                            builder.append(',');
                        }
                        fatherGoods.setGbDgGoodsSubNames(builder.toString());
                        newList.add(fatherGoods);
                    }
                }
            }

            return R.ok().put("data", fatherGoodsEntities);
        } else {
            return R.error(-1, "没有商品");
        }
    }


    @RequestMapping(value = "/purchaserGetGoodsFather", method = RequestMethod.POST)
    @ResponseBody
    public R purchaserGetGoodsFather(Integer disId, Integer toDepId, Integer type) {

        Map<String, Object> map = new HashMap<>();
        if (type != -1) {
            map.put("type", 3);
        }
        if (toDepId != -1) {
            map.put("toDepId", toDepId);
        }
        if (disId != -1) {
            map.put("disId", disId);
        }
        System.out.println(map);
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDgService.queryDisFatherGoodsByParams(map);
        return R.ok().put("data", fatherGoodsEntities);
    }

//    @RequestMapping(value = "/getMendianFatherGoods", method = RequestMethod.POST)
//    @ResponseBody
//    public R getMendianFatherGoods(Integer disId) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("type", 3);
//        map.put("disId", disId);
//        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDgService.queryDisFatherGoodsByParams(map);
//        return R.ok().put("data", fatherGoodsEntities);
//    }


    @RequestMapping(value = "/getToDepartmentFatherGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getToDepartmentFatherGoods(Integer toDepId, String controlString, Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("toDepId", toDepId);
        map.put("disId", disId);
        if (controlString.equals("price")) {
            map.put("price", "1");
        }
        if (controlString.equals("fresh")) {
            map.put("fresh", "1");
        }

        if (controlString.equals("isNotSelf")) {
            map.put("isSelf", "0");
        }

        if (controlString.equals("isSelf")) {
            map.put("isSelf", "1");
        }

        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDgService.queryDisFatherGoodsByParams(map);

        return R.ok().put("data", fatherGoodsEntities);
    }


    @RequestMapping(value = "/getToDepartmentGoodsList", method = RequestMethod.POST)
    @ResponseBody
    public R getToDepartmentGoodsList(Integer toDepId, Integer fatherId, String goodsType,
                                      Integer limit, Integer page, Integer nxDisId) {
        Map<String, Object> map = new HashMap<>();
        map.put("toDepId", toDepId);
        map.put("dgFatherId", fatherId);
        map.put("nxDisId", nxDisId);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        if (goodsType.equals("price")) {
            map.put("price", 1);
        }
        if (goodsType.equals("fresh")) {
            map.put("fresh", 1);
        }
        List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDgService.queryDisGoodsByParams(map);
        int total = gbDgService.queryDisGoodsCount(map);
        PageUtils pageUtil = new PageUtils(gbDistributerGoodsEntities, total, limit, page);
        return R.ok().put("page", pageUtil);
    }


    @RequestMapping(value = "/getToDepartmentGoodsListWithShelf", method = RequestMethod.POST)
    @ResponseBody
    public R getToDepartmentGoodsListWithShelf(Integer toDepId, Integer fatherId, String goodsType,
                                               Integer limit, Integer page, String inventoryType, String controlString) {
        Map<String, Object> map = new HashMap<>();
        System.out.println("afdaf" + inventoryType);

        map.put("toDepId", toDepId);
        map.put("dgFatherId", fatherId);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        if (!inventoryType.equals("-1")) {
            map.put("inventoryType", inventoryType);
        }
        if (goodsType.equals("jicai")) {
            map.put("type", getGbDisGoodsTypeJicai());
        }

        if (goodsType.equals("chuku")) {
            map.put("type", getGbDisGoodsTypeChuku());
        }
        if (goodsType.equals("zicai")) {
            map.put("type", getGbDisGoodsTypeZicai());
        }
        if (controlString.equals("price")) {
            map.put("price", "1");
        }
        if (controlString.equals("fresh")) {
            map.put("fresh", "1");
        }

        if (controlString.equals("isNotSelf")) {
            map.put("isSelf", "0");
        }

        if (controlString.equals("isSelf")) {
            map.put("isSelf", "1");
        }

        List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDgService.queryDisShelfGoodsWithParams(map);
        int total = gbDgService.queryDisGoodsCount(map);
        PageUtils pageUtil = new PageUtils(gbDistributerGoodsEntities, total, limit, page);


        return R.ok().put("page", pageUtil);
    }


    @RequestMapping(value = "/getMendianGoodsList", method = RequestMethod.POST)
    @ResponseBody
    public R getMendianGoodsList(Integer fatherId, String inventoryType,
                                 Integer limit, Integer page, Integer depFatherId) {
        Map<String, Object> map = new HashMap<>();
//        map.put("type", 3);
        map.put("disGoodsFatherId", fatherId);
        map.put("inventoryType", inventoryType);
        map.put("depFatherId", depFatherId);
        System.out.println("inenetytyyty==" + map);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        List<GbDepartmentDisGoodsEntity> gbDistributerGoodsEntities = gbDepDisGoodsService.queryGbDepDisGoodsByParams(map);
        int total = gbDgService.queryDisGoodsCount(map);
        PageUtils pageUtil = new PageUtils(gbDistributerGoodsEntities, total, limit, page);
        return R.ok().put("page", pageUtil);
    }


    @RequestMapping(value = "/disCancleSupplierGoods/{id}")
    @ResponseBody
    public R disCancleSupplierGoods(@PathVariable Integer id) {
        GbDistributerGoodsEntity goodsEntity = gbDgService.queryObject(id);
        goodsEntity.setGbDgGbSupplierId(null);
        goodsEntity.setGbDgGoodsType(2);
        gbDgService.update(goodsEntity);
        return R.ok();
    }


    @RequestMapping(value = "/disUpdateDisGoodsGb", method = RequestMethod.POST)
    @ResponseBody
    public R disUpdateDisGoodsGb(@RequestBody GbDistributerGoodsEntity gbGoods) {

        //old
        Integer gbDistributerGoodsId = gbGoods.getGbDistributerGoodsId();
        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDgService.queryObject(gbDistributerGoodsId);
        Integer oldDepartmentId = gbDistributerGoodsEntity.getGbDgGbDepartmentId();

        Integer nowDepartmentId = gbGoods.getGbDgGbDepartmentId();
        GbDistributerGoodsEntity oldGoodsEntity = gbDgService.queryObject(gbDistributerGoodsId);
        Integer oldGoodsType = oldGoodsEntity.getGbDgGoodsType();
        Integer gbDgGoodsType = gbGoods.getGbDgGoodsType();

        // 修改商品采购方式
        if (!oldDepartmentId.equals(nowDepartmentId) || !oldGoodsType.equals(gbDgGoodsType)) {
            //查询是否有未完成订单
            Map<String, Object> map = new HashMap<>();
            map.put("disGoodsId", gbDistributerGoodsId);
            map.put("status", 3);
            List<GbDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(map);
            if (ordersEntities.size() > 0) {
                return R.error(-1, "有未完成订单");
            }

            //查询是否有库存
            Map<String, Object> map1 = new HashMap<>();
            map1.put("stockDepId", oldDepartmentId);
            map1.put("disGoodsId", gbDistributerGoodsId);
            map1.put("restWeight", 0);
            List<GbDepartmentGoodsStockEntity> stockEntities = gbDepGoodsStockService.queryGoodsStockByParams(map1);
            if (stockEntities.size() > 0) {
                return R.error(-1, "有库存,不能改为非库存商品.");
            }

            //删除原来部门的部门商品
            Map<String, Object> mapOld = new HashMap<>();
            mapOld.put("depFatherId", oldGoodsEntity.getGbDgGbDepartmentId());
            mapOld.put("disGoodsId", gbGoods.getGbDistributerGoodsId());
            GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepDisGoodsService.queryDepGoodsItemByParams(mapOld);
            if (departmentDisGoodsEntity != null) {
                Map<String, Object> mapD = new HashMap<>();
                mapD.put("depGoodsId", departmentDisGoodsEntity.getGbDepartmentDisGoodsId());
                mapD.put("date", formatWhatDay(0));
                System.out.println("dkakdkfkadjfdasf" + mapD);
                GbDepartmentGoodsDailyEntity dailyEntity = gbDepGoodsDailyService.queryDepGoodsDailyItem(mapD);
                if (dailyEntity != null) {
                    dailyEntity.setGbDgdStatus(-1);
                    gbDepGoodsDailyService.update(dailyEntity);
                }

                gbDepDisGoodsService.delete(departmentDisGoodsEntity.getGbDepartmentDisGoodsId());

            }

            Map<String, Object> map2 = new HashMap<>();
            map2.put("stockDepId", oldGoodsEntity.getGbDgGbDepartmentId());
            map2.put("disGoodsId", gbDistributerGoodsId);
            List<GbDistributerGoodsShelfGoodsEntity> shelfGoodsEntities = gbDistributerGoodsShelfGoodsService.queryShelfGoodsByParams(map2);
            System.out.println("deletDepdistoosssSehelff" + shelfGoodsEntities.size());
            if (shelfGoodsEntities.size() > 0) {
                for (GbDistributerGoodsShelfGoodsEntity shelfGoods : shelfGoodsEntities) {
                    gbDistributerGoodsShelfGoodsService.delete(shelfGoods.getGbDistributerGoodsShelfGoodsId());
                }
            }

        }


        if (!gbGoods.getGbDgGoodsType().equals(getGbDisGoodsTypeZicai())) {
            //对比 old-ToDepId 和新 todepId 是否一样
            if (!gbDistributerGoodsEntity.getGbDgGbDepartmentId().equals(gbGoods.getGbDgGbDepartmentId())) {
                Map<String, Object> map1 = new HashMap<>();
                map1.put("depFatherId", gbGoods.getGbDgGbDepartmentId());
                map1.put("disGoodsId", gbGoods.getGbDistributerGoodsId());
                List<GbDepartmentDisGoodsEntity> newDepartmentDisGoodsEntities = gbDepDisGoodsService.queryGbDepDisGoodsByParams(map1);
                System.out.println("dkajsfkaslfjas;lfjsa" + newDepartmentDisGoodsEntities.size());
                if (newDepartmentDisGoodsEntities.size() == 0) {
                    //添加部门商品
                    GbDepartmentDisGoodsEntity disGoodsEntity = new GbDepartmentDisGoodsEntity();
                    disGoodsEntity.setGbDdgDepGoodsName(gbGoods.getGbDgGoodsName());
                    disGoodsEntity.setGbDdgDisGoodsId(gbGoods.getGbDistributerGoodsId());
                    disGoodsEntity.setGbDdgDisGoodsFatherId(gbGoods.getGbDgDfgGoodsFatherId());
                    disGoodsEntity.setGbDdgDisGoodsGrandId(gbGoods.getGbDgDfgGoodsGrandId());
                    disGoodsEntity.setGbDdgDisGoodsGreatId(gbGoods.getGbDgDfgGoodsGreatId());
                    disGoodsEntity.setGbDdgDepGoodsPinyin(gbGoods.getGbDgGoodsPinyin());
                    disGoodsEntity.setGbDdgDepGoodsPy(gbGoods.getGbDgGoodsPy());
                    disGoodsEntity.setGbDdgDepGoodsStandardname(gbGoods.getGbDgGoodsStandardname());
                    disGoodsEntity.setGbDdgDepartmentId(gbGoods.getGbDgGbDepartmentId());
                    disGoodsEntity.setGbDdgDepartmentFatherId(gbGoods.getGbDgGbDepartmentId());
                    disGoodsEntity.setGbDdgGbDepartmentId(gbGoods.getGbDgGbDepartmentId());
                    disGoodsEntity.setGbDdgGbDisId(gbGoods.getGbDgDistributerId());
                    disGoodsEntity.setGbDdgGoodsType(gbGoods.getGbDgGoodsType());
                    disGoodsEntity.setGbDdgStockTotalWeight("0.0");
                    disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
                    disGoodsEntity.setGbDdgShowStandardId(-1);
                    disGoodsEntity.setGbDdgShowStandardName(gbGoods.getGbDgGoodsStandardname());
                    disGoodsEntity.setGbDdgShowStandardScale("-1");
                    disGoodsEntity.setGbDdgShowStandardWeight(null);
                    disGoodsEntity.setGbDdgPrintStandard(gbGoods.getGbDgGoodsStandardname());
                    gbDepDisGoodsService.save(disGoodsEntity);

                }
            }
        }


        //gengxin
        //判断是否是nxDistributer商品
        Integer nxGoodsId = gbGoods.getGbDgNxGoodsId();
        System.out.println("nxgoidisisisi" + gbGoods.getGbDgNxGoodsId());
        if (nxGoodsId != null && nxGoodsId != -1 && gbGoods.getGbDgGoodsType() == 5) {
            System.out.println("-1--1-1-1--1--1-1-1-1--1-1--1-1-1-");
//            Integer gbDgNxGoodsId = gbGoods.getGbDgNxGoodsId();
            Integer gbDgNxDistributerId = gbGoods.getGbDgNxDistributerId();
            Map<String, Object> map = new HashMap<>();
            map.put("disId", gbDgNxDistributerId);
            map.put("nxGoodsId", nxGoodsId);
            List<NxDistributerGoodsEntity> distributerGoodsEntities1 = nxDistributerGoodsService.queryDisGoodsByParams(map);
            if (distributerGoodsEntities1.size() == 0) {
                return R.error(-1, "供货商没有这个商品");
            } else {
                Integer distributerGoodsId = distributerGoodsEntities1.get(0).getNxDistributerGoodsId();
                gbGoods.setGbDgNxDistributerGoodsId(distributerGoodsId);
            }
        }

        String goodsName = gbGoods.getGbDgGoodsName();
        String pinyin = hanziToPinyin(goodsName);
        String headPinyin = getHeadStringByString(goodsName, false, null);
        gbGoods.setGbDgGoodsPinyin(pinyin);
        gbGoods.setGbDgGoodsPy(headPinyin);

        System.out.println("pdadafasfa" + gbGoods.getGbDgNxDistributerGoodsId());

        gbDgService.update(gbGoods);


        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", gbDistributerGoodsId);
        List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepDisGoodsService.queryGbDepDisGoodsByParams(map);
        System.out.println("changedepdisgooodss" + departmentDisGoodsEntities.size());
        if (departmentDisGoodsEntities.size() > 0) {
            for (GbDepartmentDisGoodsEntity disGoodsEntity : departmentDisGoodsEntities) {
                disGoodsEntity.setGbDdgDepGoodsName(gbGoods.getGbDgGoodsName());
                disGoodsEntity.setGbDdgDepGoodsPinyin(gbGoods.getGbDgGoodsPinyin());
                disGoodsEntity.setGbDdgDepGoodsPy(gbGoods.getGbDgGoodsPy());
                disGoodsEntity.setGbDdgDepGoodsStandardname(gbGoods.getGbDgGoodsStandardname());
                disGoodsEntity.setGbDdgGbDepartmentId(gbGoods.getGbDgGbDepartmentId());
                disGoodsEntity.setGbDdgGbDisId(gbGoods.getGbDgDistributerId());
                disGoodsEntity.setGbDdgGoodsType(gbGoods.getGbDgGoodsType());
                disGoodsEntity.setGbDdgShowStandardName(gbGoods.getGbDgGoodsStandardname());
                gbDepDisGoodsService.update(disGoodsEntity);
            }
        }

        return R.ok();
    }


    /**
     * 批发商商品列表
     * Integer inventoryType,
     *
     * @param fatherId 父类id
     * @return 批发商商品列表
     */
    @RequestMapping(value = "/disGetDisTypeGoodsListByFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R disGetDisTypeGoodsListByFatherId(Integer fatherId, Integer inventoryType, String controlString,
                                              Integer limit, Integer page, String goodsType) {

        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("dgFatherId", fatherId);
        map.put("inventoryType", inventoryType);
        if (goodsType.equals("jicai")) {
            map.put("type", getGbDisGoodsTypeJicai());
        }
        if (goodsType.equals("kitchen")) {
            map.put("type", getGbDisGoodsTypeKitchen());
        }

        if (goodsType.equals("chuku")) {
            map.put("type", getGbDisGoodsTypeChuku());
        }
        if (goodsType.equals("zicai")) {
            map.put("type", getGbDisGoodsTypeZicai());
        }
        if (controlString.equals("price")) {
            map.put("price", "1");
        }
        if (controlString.equals("fresh")) {
            map.put("fresh", "1");
        }

        if (controlString.equals("isNotSelf")) {
            map.put("isSelf", "0");
            map.put("type", getGbDisGoodsTypeChuku());
        }

        if (controlString.equals("isSelf")) {
            map.put("isSelf", "1");
        }
        if (goodsType.equals("appSupplier")) {
            map.put("type", getGbDisGoodsTypeAppSupplier());
        }
        List<GbDistributerGoodsEntity> goodsEntities1;
        goodsEntities1 = gbDgService.queryDisGoodsByParams(map);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("fatherId", fatherId);
        map3.put("inventoryType", inventoryType);
        if (goodsType.equals("jicai")) {
            map3.put("type", getGbDisGoodsTypeJicai());
        }

        if (goodsType.equals("chuku")) {
            map3.put("type", getGbDisGoodsTypeChuku());
        }
        if (goodsType.equals("zicai")) {
            map3.put("type", getGbDisGoodsTypeZicai());
        }
        int total = gbDgService.queryGbGoodsTotal(map3);
        PageUtils pageUtil = new PageUtils(goodsEntities1, total, limit, page);

        return R.ok().put("page", pageUtil);
    }

    @RequestMapping(value = "/getDgCataGoodsWithSubNamesGb/{disId}")
    @ResponseBody
    public R getDgCataGoodsWithSubNamesGb(@PathVariable Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        List<GbDistributerFatherGoodsEntity> goodsEntities1 = dgfService.queryDisGoodsCataWithGoods(map);

        if (goodsEntities1.size() > 0) {
            List<GbDistributerFatherGoodsEntity> newList = new ArrayList<>();
            for (GbDistributerFatherGoodsEntity greatGrandGoods : goodsEntities1) {
                for (GbDistributerFatherGoodsEntity grandGoods : greatGrandGoods.getFatherGoodsEntities()) {
                    System.out.println("grandGoodsgrandGoodssororororor=========" + grandGoods.getGbDfgFatherGoodsSort());

                    for (GbDistributerFatherGoodsEntity fatherGoods : grandGoods.getFatherGoodsEntities()) {
                        System.out.println("fathsororororor=========" + fatherGoods.getGbDfgFatherGoodsSort());
                        StringBuilder builder = new StringBuilder();
                        List<GbDistributerGoodsEntity> goodsEntities = gbDgService.queryDgSubNameByFatherIdGb(fatherGoods.getGbDistributerFatherGoodsId());
                        for (GbDistributerGoodsEntity goods : goodsEntities) {
                            String nxGoodsName = goods.getGbDgGoodsName();
                            builder.append(nxGoodsName);
                            builder.append(',');
                        }
                        fatherGoods.setGbDgGoodsSubNames(builder.toString());
                        newList.add(fatherGoods);
                    }
                }
            }

            return R.ok().put("data", goodsEntities1);
        } else {
            return R.error(-1, "没有商品");
        }
    }


    @RequestMapping(value = "/postDgnGoodsForNx", method = RequestMethod.POST)
    @ResponseBody
    public R postDgnGoodsForNx(Integer disGoodsId, Integer gbDisId, Integer depId) {

        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(disGoodsId);
        Integer GbDgNxGoodsId = nxDistributerGoodsEntity.getNxDgNxGoodsId();

        //判断是否已经下载
        Map<String, Object> map7 = new HashMap<>();
        map7.put("disId", gbDisId);
        map7.put("goodsId", GbDgNxGoodsId);
        List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDgService.queryDisGoodsByParams(map7);

        if (distributerGoodsEntities.size() > 0) {
            return R.error(-1, "已经下载");
        } else {
            GbDistributerGoodsEntity cgnGoods = new GbDistributerGoodsEntity();
            cgnGoods.setGbDgGoodsName(nxDistributerGoodsEntity.getNxDgGoodsName());
            cgnGoods.setGbDgGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
            cgnGoods.setGbDgGoodsPy(nxDistributerGoodsEntity.getNxDgGoodsPy());
            cgnGoods.setGbDgGoodsPinyin(nxDistributerGoodsEntity.getNxDgGoodsPinyin());
            cgnGoods.setGbDgGoodsStandardWeight(nxDistributerGoodsEntity.getNxDgGoodsStandardWeight());
            cgnGoods.setGbDgGoodsDetail(nxDistributerGoodsEntity.getNxDgGoodsDetail());
            cgnGoods.setGbDgGoodsBrand(nxDistributerGoodsEntity.getNxDgGoodsBrand());
            cgnGoods.setGbDgGoodsPlace(nxDistributerGoodsEntity.getNxDgGoodsPlace());
            cgnGoods.setGbDgGoodsSort(nxDistributerGoodsEntity.getNxDgGoodsSort());
            cgnGoods.setGbDgDistributerId(gbDisId);
            cgnGoods.setGbDgGoodsStatus(0);
            cgnGoods.setGbDgGoodsIsWeight(0);
            cgnGoods.setGbDgNxGoodsId(nxDistributerGoodsEntity.getNxDgNxGoodsId());
            cgnGoods.setGbDgNxFatherId(nxDistributerGoodsEntity.getNxDgNxFatherId());
            cgnGoods.setGbDgNxGrandId(nxDistributerGoodsEntity.getNxDgNxGrandId());
            cgnGoods.setGbDgNxGreatGrandId(nxDistributerGoodsEntity.getNxDgNxGreatGrandId());
            cgnGoods.setGbDgNxDistributerGoodsPrice(nxDistributerGoodsEntity.getNxDgWillPrice());
            cgnGoods.setGbDgPullOff(0);
            cgnGoods.setGbDgGoodsType(5);
            cgnGoods.setGbDgNxDistributerId(nxDistributerGoodsEntity.getNxDgDistributerId());
            cgnGoods.setGbDgNxDistributerGoodsId(disGoodsId);
            cgnGoods.setGbDgGbDepartmentId(depId);
            cgnGoods.setGbDgControlFresh(0);
            cgnGoods.setGbDgControlPrice(0);
            cgnGoods.setGbDgGoodsInventoryType(1);
            cgnGoods.setGbDgIsFranchisePrice(0);
            cgnGoods.setGbDgIsSelfControl(0);
            cgnGoods.setGbDgNxFatherImg(nxDistributerGoodsEntity.getNxDgNxFatherImg());
            cgnGoods.setGbDgNxFatherImgLarge(nxDistributerGoodsEntity.getNxDgGoodsFileLarge());
//            GbDistributerGoodsEntity disGoods = saveDisGoodsForNx(cgnGoods);
            GbDistributerGoodsEntity disGoods = saveDisGoods(cgnGoods);

            //添加部门商品
            GbDepartmentDisGoodsEntity disGoodsEntity = new GbDepartmentDisGoodsEntity();
            disGoodsEntity.setGbDdgDepGoodsName(disGoods.getGbDgGoodsName());
            disGoodsEntity.setGbDdgDisGoodsId(disGoods.getGbDistributerGoodsId());
            disGoodsEntity.setGbDdgDisGoodsFatherId(disGoods.getGbDgDfgGoodsFatherId());
            disGoodsEntity.setGbDdgDisGoodsGrandId(disGoods.getGbDgDfgGoodsGrandId());
            disGoodsEntity.setGbDdgDisGoodsGreatId(disGoods.getGbDgDfgGoodsGreatId());
            disGoodsEntity.setGbDdgDepGoodsPinyin(disGoods.getGbDgGoodsPinyin());
            disGoodsEntity.setGbDdgDepGoodsPy(disGoods.getGbDgGoodsPy());
            disGoodsEntity.setGbDdgDepGoodsStandardname(disGoods.getGbDgGoodsStandardname());
            disGoodsEntity.setGbDdgDepartmentId(disGoods.getGbDgGbDepartmentId());
            disGoodsEntity.setGbDdgDepartmentFatherId(disGoods.getGbDgGbDepartmentId());
            disGoodsEntity.setGbDdgGbDepartmentId(disGoods.getGbDgGbDepartmentId());
            disGoodsEntity.setGbDdgGbDisId(disGoods.getGbDgDistributerId());
            disGoodsEntity.setGbDdgGoodsType(disGoods.getGbDgGoodsType());
            disGoodsEntity.setGbDdgStockTotalWeight("0.0");
            disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
            disGoodsEntity.setGbDdgShowStandardId(-1);
            disGoodsEntity.setGbDdgShowStandardName(disGoods.getGbDgGoodsStandardname());
            disGoodsEntity.setGbDdgShowStandardScale("-1");
            disGoodsEntity.setGbDdgShowStandardWeight(null);
            disGoodsEntity.setGbDdgNxDistributerGoodsId(disGoodsId);
            disGoodsEntity.setGbDdgPrintStandard(disGoods.getGbDgGoodsStandardname());
            gbDepDisGoodsService.save(disGoodsEntity);

            //2.2
            Integer nxCgGoodsId = disGoods.getGbDistributerGoodsId();

            Map<String, Object> mapA = new HashMap<>();
            mapA.put("nxDisId", disGoods.getGbDgNxGoodsId());
            List<NxAliasEntity> aliasEntities = nxAliasService.queryNxAliasList(mapA);


            if (aliasEntities.size() > 0) {
                for (NxAliasEntity aliasEntity : aliasEntities) {
                    GbDistributerAliasEntity disAlias = new GbDistributerAliasEntity();
                    disAlias.setGbDaDisGoodsId(nxCgGoodsId);
                    disAlias.setGbDaAliasName(aliasEntity.getNxAliasName());
                    gbDistributerAliasService.save(disAlias);
                }
            }

            Integer gbDgNxDistributerGoodsId = cgnGoods.getGbDgNxDistributerGoodsId();
            List<NxDistributerStandardEntity> distributerStandardEntities = nxDistributerStandardService.queryDisStandardByDisGoodsId(gbDgNxDistributerGoodsId);
            if (distributerStandardEntities.size() > 0) {
                for (NxDistributerStandardEntity standardEntity : distributerStandardEntities) {
                    GbDistributerStandardEntity distributerStandardEntity = new GbDistributerStandardEntity();
                    distributerStandardEntity.setGbDsDisGoodsId(nxCgGoodsId);
                    distributerStandardEntity.setGbDsStandardName(standardEntity.getNxDsStandardName());
                    gbDistributerStandardService.save(distributerStandardEntity);
                }
            }


            //添加给门店
            //如果是餐饮商品，自动给门店添加部门商品
            if (disGoods.getGbDgGoodsType() < 20) {
                Map<String, Object> map = new HashMap<>();
                map.put("disId", disGoods.getGbDgDistributerId());
                map.put("type", getGbDepartmentTypeMendian());
                List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
                if (departmentEntities.size() > 0) {
                    for (GbDepartmentEntity dep : departmentEntities) {
                        GbDepartmentDisGoodsEntity disGoodsEntityDep = new GbDepartmentDisGoodsEntity();
                        disGoodsEntityDep.setGbDdgDepGoodsName(disGoods.getGbDgGoodsName());
                        disGoodsEntityDep.setGbDdgDisGoodsId(disGoods.getGbDistributerGoodsId());
                        disGoodsEntityDep.setGbDdgDisGoodsFatherId(disGoods.getGbDgDfgGoodsFatherId());
                        disGoodsEntityDep.setGbDdgDisGoodsGrandId(disGoods.getGbDgDfgGoodsGrandId());
                        disGoodsEntityDep.setGbDdgDisGoodsGreatId(disGoods.getGbDgDfgGoodsGreatId());
                        disGoodsEntityDep.setGbDdgDepGoodsPinyin(disGoods.getGbDgGoodsPinyin());
                        disGoodsEntityDep.setGbDdgDepGoodsPy(disGoods.getGbDgGoodsPy());
                        disGoodsEntityDep.setGbDdgDepGoodsStandardname(disGoods.getGbDgGoodsStandardname());
                        disGoodsEntityDep.setGbDdgDepartmentId(dep.getGbDepartmentId());
                        disGoodsEntityDep.setGbDdgDepartmentFatherId(dep.getGbDepartmentId());
                        disGoodsEntityDep.setGbDdgGbDepartmentId(disGoods.getGbDgGbDepartmentId());
                        disGoodsEntityDep.setGbDdgGbDisId(disGoods.getGbDgDistributerId());
                        disGoodsEntityDep.setGbDdgGoodsType(disGoods.getGbDgGoodsType());
                        disGoodsEntityDep.setGbDdgStockTotalWeight("0.0");
                        disGoodsEntityDep.setGbDdgStockTotalSubtotal("0.0");
                        disGoodsEntityDep.setGbDdgShowStandardId(-1);
                        disGoodsEntityDep.setGbDdgShowStandardName(disGoods.getGbDgGoodsStandardname());
                        disGoodsEntityDep.setGbDdgShowStandardScale("-1");
                        disGoodsEntityDep.setGbDdgShowStandardWeight(null);
                        disGoodsEntityDep.setGbDdgNxDistributerGoodsId(disGoodsId);
                        disGoodsEntity.setGbDdgPrintStandard(disGoods.getGbDgGoodsStandardname());
                        gbDepDisGoodsService.save(disGoodsEntityDep);
                    }
                }
            }

            return R.ok().put("data", disGoods);
        }
    }

    //


    /**
     * 添加批发商商品
     *
     * @param cgnGoods 批发商商品
     * @return ok
     */
    @RequestMapping(value = "/postDgnGoodsGb", method = RequestMethod.POST)
    @ResponseBody
    public R postDgnGoodsGb(@RequestBody GbDistributerGoodsEntity cgnGoods) {

        Integer GbDgNxGoodsId = cgnGoods.getGbDgNxGoodsId();

        //判断是否已经下载
        Integer GbDgDistributerId1 = cgnGoods.getGbDgDistributerId();
        Map<String, Object> map7 = new HashMap<>();
        map7.put("disId", GbDgDistributerId1);
        map7.put("goodsId", GbDgNxGoodsId);
        List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDgService.queryDisGoodsByParams(map7);

        if (distributerGoodsEntities.size() > 0) {
            return R.error(-1, "已经下载");
        } else {
            cgnGoods.setGbDgControlFresh(0);
            cgnGoods.setGbDgControlPrice(0);
            Integer gbDgDistributerId = cgnGoods.getGbDgDistributerId();
            Map<String, Object> map = new HashMap<>();
            map.put("disId", gbDgDistributerId);
            map.put("type", 2);
            List<GbDepartmentEntity> gbDepartmentEntityList = gbDepartmentService.queryDepByDepType(map);
            if (gbDepartmentEntityList.size() > 0) {
                cgnGoods.setGbDgGoodsType(1);
                cgnGoods.setGbDgGbDepartmentId(gbDepartmentEntityList.get(0).getGbDepartmentId());
            }


            GbDistributerGoodsEntity disGoods = saveDisGoods(cgnGoods);
//            GbDepartmentDisGoodsEntity disGoodsEntity = new GbDepartmentDisGoodsEntity();
//            disGoodsEntity.setGbDdgDepGoodsName(disGoods.getGbDgGoodsName());
//            disGoodsEntity.setGbDdgDisGoodsId(disGoods.getGbDistributerGoodsId());
//            disGoodsEntity.setGbDdgDisGoodsFatherId(disGoods.getGbDgDfgGoodsFatherId());
//            disGoodsEntity.setGbDdgDepGoodsPinyin(disGoods.getGbDgGoodsPinyin());
//            disGoodsEntity.setGbDdgDepGoodsPy(disGoods.getGbDgGoodsPy());
//            disGoodsEntity.setGbDdgDepGoodsStandardname(disGoods.getGbDgGoodsStandardname());
//            disGoodsEntity.setGbDdgDepartmentId(disGoods.getGbDgGbDepartmentId());
//            disGoodsEntity.setGbDdgDepartmentFatherId(disGoods.getGbDgGbDepartmentId());
//            disGoodsEntity.setGbDdgGbDepartmentId(disGoods.getGbDgGbDepartmentId());
//            disGoodsEntity.setGbDdgGbDisId(disGoods.getGbDgDistributerId());
//            disGoodsEntity.setGbDdgGoodsType(disGoods.getGbDgGoodsType());
//            disGoodsEntity.setGbDdgStockTotalWeight("0.0");
//            disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
//            disGoodsEntity.setGbDdgShowStandardId(-1);
//            disGoodsEntity.setGbDdgShowStandardName(disGoods.getGbDgGoodsStandardname());
//            disGoodsEntity.setGbDdgShowStandardScale("-1");
//            disGoodsEntity.setGbDdgShowStandardWeight(null);
//            disGoodsEntity.setGbDdgNxDistributerGoodsId(GbDgNxGoodsId);
//            disGoodsEntity.setGbDdgDisGoodsGrandId(disGoodsEntity.getGbDdgDisGoodsGrandId());
//            disGoodsEntity.setGbDdgDisGoodsGreatId(disGoodsEntity.getGbDdgDisGoodsGreatId());
//
//            gbDepDisGoodsService.save(disGoodsEntity);

            //2.2
            Integer nxCgGoodsId = cgnGoods.getGbDistributerGoodsId();
            List<NxAliasEntity> aliasEntities = cgnGoods.getNxAliasEntities();
            if (aliasEntities.size() > 0) {
                for (NxAliasEntity aliasEntity : aliasEntities) {
                    GbDistributerAliasEntity disAlias = new GbDistributerAliasEntity();
                    disAlias.setGbDaDisGoodsId(nxCgGoodsId);
                    disAlias.setGbDaAliasName(aliasEntity.getNxAliasName());
                    gbDistributerAliasService.save(disAlias);
                }
            }

            return R.ok().put("data", disGoods);
        }
    }


    private GbDistributerGoodsEntity saveDisGoodsForNx(GbDistributerGoodsEntity cgnGoods) {


        Integer gbDgDistributerId = cgnGoods.getGbDgDistributerId();
        Integer gbDgNxGrandId = cgnGoods.getGbDgNxGrandId(); //nxGrand 是gb的father 101

        if (gbDgNxGrandId != -1) {

        }
        //queryGrandFatherId
        NxGoodsEntity fatherEntity = nxGoodsService.queryObject(gbDgNxGrandId); //叶花菜
        Integer grandFatherId = fatherEntity.getNxGoodsFatherId();
        NxGoodsEntity grandEntity = nxGoodsService.queryObject(grandFatherId);
        cgnGoods.setGbDgNxGrandName(grandEntity.getNxGoodsName());
        cgnGoods.setGbDgNxFatherName(fatherEntity.getNxGoodsName());

        Map<String, Object> map = new HashMap<>();
        map.put("color", "#187e6e");
        map.put("disId", gbDgDistributerId);
        ;
        GbDistributerFatherGoodsEntity greatGrand = dgfService.queryAppFatherGoods(map);
        cgnGoods.setGbDgNxGreatGrandId(greatGrand.getGbDistributerFatherGoodsId());
        cgnGoods.setGbDgNxGreatGrandName(greatGrand.getGbDfgFatherGoodsName());

        // 3， 查询父类
        Map<String, Object> map11 = new HashMap<>();
        map11.put("nxGoodsId", fatherEntity.getNxGoodsId());
        map11.put("disId", gbDgDistributerId);
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities1 = dgfService.queryDisFathersGoodsByParamsGb(map11);

        if (fatherGoodsEntities1.size() > 0) {
            //直接加disGoods和disStandard,不需要加disFatherGoods
            //1，给父类商品的字段商品数量加1
            GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities1.get(0);
            Integer nxDfgGoodsAmount = fatherGoodsEntity.getGbDfgGoodsAmount();
            fatherGoodsEntity.setGbDfgGoodsAmount(nxDfgGoodsAmount + 1);
            dgfService.update(fatherGoodsEntity);

            //2，保存disId商品
            cgnGoods.setGbDgDfgGoodsFatherId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
            cgnGoods.setGbDgDfgGoodsGrandId(fatherGoodsEntity.getGbDfgFathersFatherId());
            GbDistributerFatherGoodsEntity grandFather = dgfService.queryObject(fatherGoodsEntity.getGbDfgFathersFatherId());
            cgnGoods.setGbDgDfgGoodsGreatId(grandFather.getGbDfgFathersFatherId());

            //1 ，先保存disGoods
            gbDgService.save(cgnGoods);
            //
        } else {
            //添加fatherGoods的第一个级别 是nxGrand
            GbDistributerFatherGoodsEntity dgf = new GbDistributerFatherGoodsEntity();
            dgf.setGbDfgDistributerId(cgnGoods.getGbDgDistributerId());
            dgf.setGbDfgFatherGoodsName(fatherEntity.getNxGoodsName());
            dgf.setGbDfgFatherGoodsLevel(2);
            dgf.setGbDfgGoodsAmount(1);
            dgf.setGbDfgPriceAmount(0);
            dgf.setGbDfgPriceTwoAmount(0);
            dgf.setGbDfgPriceThreeAmount(0);
            dgf.setGbDfgNxGoodsId(cgnGoods.getGbDgNxGrandId());
            dgf.setGbDfgFatherGoodsImg(fatherEntity.getNxGoodsFile());
            dgf.setGbDfgFatherGoodsSort(fatherEntity.getNxGoodsSort());
            dgfService.save(dgf);
            //更新disGoods的fatherGoodsId
            Integer distributerFatherGoodsId = dgf.getGbDistributerFatherGoodsId();
            cgnGoods.setGbDgDfgGoodsFatherId(distributerFatherGoodsId);
            cgnGoods.setGbDgDfgGoodsGrandId(dgf.getGbDfgFathersFatherId());

            GbDistributerFatherGoodsEntity grandFather = dgfService.queryObject(dgf.getGbDfgFathersFatherId());
            cgnGoods.setGbDgDfgGoodsGreatId(grandFather.getGbDfgFathersFatherId());


            gbDgService.save(cgnGoods);
            //继续查询是否有GrandFather
            String fatherName = cgnGoods.getGbDgNxGrandName();
            Map<String, Object> map2 = new HashMap<>();
            map2.put("disId", gbDgDistributerId);
            map2.put("fathersFatherName", fatherName);
            map2.put("goodsLevel", 1);
            List<GbDistributerFatherGoodsEntity> grandGoodsFather = dgfService.queryHasDisFathersFather(map2);
            if (grandGoodsFather.size() > 0) {
                GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity = grandGoodsFather.get(0);
                dgf.setGbDfgFathersFatherId(gbDistributerFatherGoodsEntity.getGbDistributerFatherGoodsId());
                dgf.setGbDfgGoodsAmount(gbDistributerFatherGoodsEntity.getGbDfgGoodsAmount() + 1);
                dgfService.update(dgf);
            } else {
                //tianjiaGrand
                GbDistributerFatherGoodsEntity grand = new GbDistributerFatherGoodsEntity();
                grand.setGbDfgFatherGoodsName(grandEntity.getNxGoodsName());
                grand.setGbDfgDistributerId(gbDgDistributerId);
                grand.setGbDfgFatherGoodsLevel(1);
                grand.setGbDfgGoodsAmount(1);
                grand.setGbDfgFatherGoodsSort(grandEntity.getNxGoodsSort());
                grand.setGbDfgFatherGoodsColor(grandEntity.getColor());
                grand.setGbDfgFatherGoodsImg(grandEntity.getNxGoodsFile());
                grand.setGbDfgNxGoodsId(grandEntity.getNxGoodsId());
                Map<String, Object> mapApp = new HashMap<>();
                mapApp.put("color", "#187e6e");
                mapApp.put("disId", gbDgDistributerId);
                GbDistributerFatherGoodsEntity app = dgfService.queryAppFatherGoods(mapApp);
                grand.setGbDfgFathersFatherId(app.getGbDistributerFatherGoodsId());
                dgfService.save(grand);

                dgf.setGbDfgFathersFatherId(grand.getGbDistributerFatherGoodsId());
                dgfService.update(dgf);

            }
        }


        return cgnGoods;
    }


    private GbDistributerGoodsEntity saveDisGoods(GbDistributerGoodsEntity cgnGoods) {

        Integer nxDgNxGoodsId = cgnGoods.getGbDgNxGoodsId();
        NxGoodsEntity nxGoodsEntity = nxGoodsService.queryObject(nxDgNxGoodsId);

        cgnGoods.setGbDgGoodsName(nxGoodsEntity.getNxGoodsName());
        cgnGoods.setGbDgNxFatherImg(nxGoodsEntity.getNxGoodsFile());
        cgnGoods.setGbDgGoodsStandardname(nxGoodsEntity.getNxGoodsStandardname());
        cgnGoods.setGbDgGoodsDetail(nxGoodsEntity.getNxGoodsDetail());
        cgnGoods.setGbDgGoodsPlace(nxGoodsEntity.getNxGoodsPlace());
        cgnGoods.setGbDgGoodsBrand(nxGoodsEntity.getNxGoodsBrand());
        cgnGoods.setGbDgGoodsStandardWeight(nxGoodsEntity.getNxGoodsStandardWeight());
        cgnGoods.setGbDgGoodsPinyin(nxGoodsEntity.getNxGoodsPinyin());
        cgnGoods.setGbDgGoodsPy(nxGoodsEntity.getNxGoodsPy());
        cgnGoods.setGbDgPullOff(0);
        cgnGoods.setGbDgGoodsStatus(1);
        cgnGoods.setNxStandardEntities(nxGoodsEntity.getNxGoodsStandardEntities());
        cgnGoods.setGbDgNxFatherId(nxGoodsEntity.getNxGoodsFatherId());
        cgnGoods.setGbDgNxFatherName(nxGoodsEntity.getFatherGoods().getNxGoodsName());
        cgnGoods.setGbDgNxFatherImg(nxGoodsEntity.getFatherGoods().getNxGoodsFile());
        cgnGoods.setGbDgNxGrandName(nxGoodsEntity.getGrandGoods().getNxGoodsName());
        cgnGoods.setGbDgNxGrandId(nxGoodsEntity.getGrandGoods().getNxGoodsId());
        cgnGoods.setGbDgNxFatherImg(nxGoodsEntity.getNxGoodsFile());
        cgnGoods.setGbDgNxFatherImgLarge(nxGoodsEntity.getNxGoodsFileBig());
        cgnGoods.setGbDgGoodsSort(nxGoodsEntity.getNxGoodsSort());
        cgnGoods.setGbDgGoodsSonsSort(nxGoodsEntity.getNxGoodsSonsSort());
        cgnGoods.setGbDgIsFranchisePrice(0);
        cgnGoods.setGbDgIsSelfControl(0);
        cgnGoods.setGbDgGoodsIsHidden(0);
        cgnGoods.setGbDgGoodsType(2);
        cgnGoods.setGbDgGoodsIsWeight(0);
        cgnGoods.setGbDgNxDistributerId(-1);
        cgnGoods.setGbDgNxDistributerGoodsId(-1);
        cgnGoods.setGbDgGoodsInventoryType(1);
        cgnGoods.setGbDgGbSupplierId(-1);
        cgnGoods.setGbDgNxDistributerGoodsPrice("0.1");
        cgnGoods.setGbDgQuantityDays(nxGoodsEntity.getNxGoodsQuantityDays());


        //queryGrandFatherId
        NxGoodsEntity fatherEntity = nxGoodsService.queryObject(cgnGoods.getGbDgNxFatherId());
        Integer grandFatherId = fatherEntity.getNxGoodsFatherId();
        cgnGoods.setGbDgNxGrandId(grandFatherId);
        NxGoodsEntity grandEntity = nxGoodsService.queryObject(grandFatherId);
        cgnGoods.setGbDgNxGrandName(grandEntity.getNxGoodsName());

        //queryGreatGrandFatherId
        Integer nxGreatGrandFatherId = grandEntity.getNxGoodsFatherId();
        if (nxGreatGrandFatherId.equals(1)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#20afb8");
        }
        if (nxGreatGrandFatherId.equals(2)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#f5c832");
        }
        if (nxGreatGrandFatherId.equals(3)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#3cc36e");
        }
        if (nxGreatGrandFatherId.equals(4)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#f09628");
        }
        if (nxGreatGrandFatherId.equals(5)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#1ebaee");
        }
        if (nxGreatGrandFatherId.equals(6)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#f05a32");
        }
        if (nxGreatGrandFatherId.equals(7)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#c0a6dd");
        }
        if (nxGreatGrandFatherId.equals(8)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#969696");
        }
        if (nxGreatGrandFatherId.equals(9)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#318666");
        }
        if (nxGreatGrandFatherId.equals(10)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#026bc2");
        }
        if (nxGreatGrandFatherId.equals(11)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#06eb6d");
        }
        if (nxGreatGrandFatherId.equals(12)) {
            cgnGoods.setGbDgNxGoodsFatherColor("#0690eb");
        }

        cgnGoods.setGbDgNxGreatGrandId(nxGreatGrandFatherId);
        cgnGoods.setGbDgNxGreatGrandName(nxGoodsService.queryObject(nxGreatGrandFatherId).getNxGoodsName());

        Integer GbDgDistributerId = cgnGoods.getGbDgDistributerId();

        // 3， 查询父类
        Integer GbDgNxFatherId = cgnGoods.getGbDgNxFatherId();
        Map<String, Object> map11 = new HashMap<>();
        map11.put("disId", GbDgDistributerId);
        map11.put("nxFatherId", GbDgNxFatherId);
        System.out.println("faehrhrhehehemap" + map11);
        List<GbDistributerGoodsEntity> nxDistributerGoodsEntities = gbDgService.queryDisGoodsByParams(map11);

        if (nxDistributerGoodsEntities.size() > 0) {
            //直接加disGoods和disStandard,不需要加disFatherGoods
            //1，给父类商品的字段商品数量加1
            GbDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsEntities.get(0);
            Integer nxDgDfgGoodsFatherId1 = disGoodsEntity.getGbDgDfgGoodsFatherId();
            Integer nxDgDfgGoodsGrandId = disGoodsEntity.getGbDgDfgGoodsGrandId();
            Integer nxDgDfgGoodsGreatId = disGoodsEntity.getGbDgDfgGoodsGreatId();

            GbDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = dgfService.queryObject(nxDgDfgGoodsFatherId1);
            Integer nxDfgGoodsAmount = nxDistributerFatherGoodsEntity.getGbDfgGoodsAmount();
            nxDistributerFatherGoodsEntity.setGbDfgGoodsAmount(nxDfgGoodsAmount + 1);
            dgfService.update(nxDistributerFatherGoodsEntity);

            //2，保存disId商品
            Integer nxDgDfgGoodsFatherId = disGoodsEntity.getGbDgDfgGoodsFatherId();
            cgnGoods.setGbDgDfgGoodsFatherId(nxDgDfgGoodsFatherId);
            cgnGoods.setGbDgDfgGoodsGrandId(nxDgDfgGoodsGrandId);
            cgnGoods.setGbDgDfgGoodsGreatId(nxDgDfgGoodsGreatId);

            System.out.println("cngnndg" + cgnGoods.getGbDgDfgGoodsGrandId());
            //1 ，先保存disGoods
            gbDgService.save(cgnGoods);
            //
        } else {
            //添加fatherGoods的第一个级别
            GbDistributerFatherGoodsEntity dgf = new GbDistributerFatherGoodsEntity();
            dgf.setGbDfgDistributerId(cgnGoods.getGbDgDistributerId());
            dgf.setGbDfgFatherGoodsName(cgnGoods.getGbDgNxFatherName());
            dgf.setGbDfgFatherGoodsLevel(2);
            dgf.setGbDfgGoodsAmount(1);
            dgf.setGbDfgPriceAmount(0);
            dgf.setGbDfgPriceTwoAmount(0);
            dgf.setGbDfgPriceThreeAmount(0);
            dgf.setGbDfgFatherGoodsColor(cgnGoods.getGbDgNxGoodsFatherColor());
            dgf.setGbDfgNxGoodsId(cgnGoods.getGbDgNxFatherId());
            dgf.setGbDfgFatherGoodsImg(cgnGoods.getGbDgNxFatherImg());
            dgf.setGbDfgFatherGoodsImgLarge(cgnGoods.getGbDgNxFatherImgLarge());
            dgf.setGbDfgFatherGoodsSort(nxGoodsEntity.getFatherGoods().getNxGoodsSort());
            dgf.setGbDfgNxGoodsId(cgnGoods.getGbDgNxFatherId());
            dgfService.save(dgf);
            //更新disGoods的fatherGoodsId
            Integer distributerFatherGoodsId = dgf.getGbDistributerFatherGoodsId();
            cgnGoods.setGbDgDfgGoodsFatherId(distributerFatherGoodsId);

            //todo
//            cgnGoods.setGbDgDfgGoodsGrandId(dgf.getGbDfgFathersFatherId());
////
//            GbDistributerFatherGoodsEntity grandFather = dgfService.queryObject(dgf.getGbDfgFathersFatherId());
//            cgnGoods.setGbDgDfgGoodsGreatId(grandFather.getGbDfgFathersFatherId());

            System.out.println("zizin" + dgf.getGbDfgFathersFatherId());
            gbDgService.save(cgnGoods);
            //继续查询是否有GrandFather

            String grandName = cgnGoods.getGbDgNxGrandName();
            Map<String, Object> map2 = new HashMap<>();
            map2.put("disId", GbDgDistributerId);
            map2.put("fathersFatherName", grandName);
            List<GbDistributerFatherGoodsEntity> grandGoodsFather = dgfService.queryHasDisFathersFather(map2);
            if (grandGoodsFather.size() > 0) {
                GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity = grandGoodsFather.get(0);
                Integer nxDfgGoodsAmount = dgf.getGbDfgGoodsAmount();
                dgf.setGbDfgGoodsAmount(nxDfgGoodsAmount + 1);
                dgf.setGbDfgFathersFatherId(gbDistributerFatherGoodsEntity.getGbDistributerFatherGoodsId());
                dgfService.update(dgf);

                Integer nxDfgFathersFatherId = gbDistributerFatherGoodsEntity.getGbDistributerFatherGoodsId();
                cgnGoods.setGbDgDfgGoodsGrandId(nxDfgFathersFatherId);
                GbDistributerFatherGoodsEntity great = dgfService.queryObject(nxDfgFathersFatherId);
                cgnGoods.setGbDgDfgGoodsGreatId(great.getGbDfgFathersFatherId());
                gbDgService.update(cgnGoods);



            } else {
                //tianjiaGrand
                GbDistributerFatherGoodsEntity grand = new GbDistributerFatherGoodsEntity();
                String nxCgGrandFatherName = cgnGoods.getGbDgNxGrandName();
                grand.setGbDfgFatherGoodsName(nxCgGrandFatherName);
                grand.setGbDfgDistributerId(cgnGoods.getGbDgDistributerId());
                grand.setGbDfgFatherGoodsLevel(1);
                grand.setGbDfgGoodsAmount(1);
                grand.setGbDfgFatherGoodsColor(cgnGoods.getGbDgNxGoodsFatherColor());
                grand.setGbDfgNxGoodsId(cgnGoods.getGbDgNxGrandId());
                NxGoodsEntity nxGrand = nxGoodsService.queryObject(cgnGoods.getGbDgNxGrandId());
                grand.setGbDfgFatherGoodsImg(nxGrand.getNxGoodsFile());
                grand.setGbDfgFatherGoodsImgLarge(nxGrand.getNxGoodsFileBig());
                System.out.println("nxgoodsnxgoods====" + nxGrand.getNxGoodsId() + "sort==" + nxGrand.getNxGoodsSort());
                grand.setGbDfgFatherGoodsSort(nxGrand.getNxGoodsSort());
                dgfService.save(grand);

                dgf.setGbDfgFathersFatherId(grand.getGbDistributerFatherGoodsId());
                dgfService.update(dgf);

                cgnGoods.setGbDgDfgGoodsGrandId(grand.getGbDistributerFatherGoodsId());
                gbDgService.update(cgnGoods);

                //查询是否有greatGrand
                Map<String, Object> map3 = new HashMap<>();
                map3.put("disId", GbDgDistributerId);
                String greatGrandName = cgnGoods.getGbDgNxGreatGrandName();
                map3.put("fathersFatherName", greatGrandName);
                List<GbDistributerFatherGoodsEntity> greatGrandGoodsFather = dgfService.queryHasDisFathersFather(map3);

                if (greatGrandGoodsFather.size() > 0) {
                    GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity = greatGrandGoodsFather.get(0);
                    Integer disFatherId = gbDistributerFatherGoodsEntity.getGbDistributerFatherGoodsId();
                    grand.setGbDfgFathersFatherId(disFatherId);
                    Integer gbDfgGoodsAmount = grand.getGbDfgGoodsAmount();
                    grand.setGbDfgGoodsAmount(gbDfgGoodsAmount + 1);
                    dgfService.update(grand);

                    cgnGoods.setGbDgDfgGoodsGreatId(disFatherId);
                    gbDgService.update(cgnGoods);

                } else {
                    GbDistributerFatherGoodsEntity greatGrand = new GbDistributerFatherGoodsEntity();
                    NxGoodsEntity greatGrandEntity = nxGoodsService.queryObject(cgnGoods.getGbDgNxGreatGrandId());
                    String greatGrandName1 = cgnGoods.getGbDgNxGreatGrandName();
                    greatGrand.setGbDfgFatherGoodsName(greatGrandName1);
                    greatGrand.setGbDfgDistributerId(cgnGoods.getGbDgDistributerId());
                    greatGrand.setGbDfgFatherGoodsImg(greatGrandEntity.getNxGoodsFile());
                    greatGrand.setGbDfgFatherGoodsImgLarge(greatGrandEntity.getNxGoodsFileBig());
                    greatGrand.setGbDfgDistributerId(cgnGoods.getGbDgDistributerId());
                    greatGrand.setGbDfgFatherGoodsLevel(0);
                    greatGrand.setGbDfgFathersFatherId(0);
                    greatGrand.setGbDfgFatherGoodsColor(cgnGoods.getGbDgNxGoodsFatherColor());
                    greatGrand.setGbDfgNxGoodsId(cgnGoods.getGbDgNxGreatGrandId());
                    greatGrand.setGbDfgFatherGoodsSort(greatGrandEntity.getNxGoodsSort());
                    greatGrand.setGbDfgGoodsAmount(1);
                    dgfService.save(greatGrand);

                    grand.setGbDfgFathersFatherId(greatGrand.getGbDistributerFatherGoodsId());
                    dgfService.update(grand);

                    cgnGoods.setGbDgDfgGoodsGreatId(greatGrand.getGbDistributerFatherGoodsId());
                    gbDgService.update(cgnGoods);
                }
            }
        }


        return cgnGoods;
    }


    //
//
//
//
//	/**
//	 * 批发商商品列表
//	 * @param fatherId 父类id
//	 * @return 批发商商品列表
//	 */
    @RequestMapping(value = "/disGetDisGoodsListByFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R disGetDisGoodsListByFatherId(Integer fatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("dgFatherId", fatherId);
        map.put("disId", disId);
        List<GbDistributerGoodsEntity> goodsEntities1 = gbDgService.queryDisGoodsByParams(map);
        return R.ok().put("data", goodsEntities1);
    }

    @RequestMapping(value = "/disGetShowGoodsListByFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R disGetShowGoodsListByFatherId(Integer fatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("dgFatherId", fatherId);
        map.put("disId", disId);
        List<GbDistributerGoodsEntity> goodsEntities1 = gbDgService.queryDisGoodsByParams(map);

        GbDistributerFatherGoodsEntity fatherGoodsEntity = dgfService.queryObject(fatherId);
        Integer gbDfgFathersFatherId = fatherGoodsEntity.getGbDfgFathersFatherId();
        Map<String, Object> mapF = new HashMap<>();
        mapF.put("fathersFatherId", gbDfgFathersFatherId);
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisFathersGoodsByParamsGb(mapF);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("goodsArr", goodsEntities1);
        mapR.put("fatherArr", fatherGoodsEntities);
        return R.ok().put("data", mapR);
    }

    //
//	/**
//	 * ibook获取含有批发商信息的商品列表
//	 * @param limit 每页商品数量
//	 * @param page 第几页
//	 * @param fatherId 商品父级id
//	 * @param disId 批发商id
//	 * @return ibook商品列表
//	 */
    @RequestMapping(value = "/disGetIbookGoodsGb", method = RequestMethod.POST)
    @ResponseBody
    public R disGetIbookGoodsGb(Integer limit, Integer page, Integer fatherId, Integer disId) {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("offset", (page - 1) * limit);
        map1.put("limit", limit);
        map1.put("fatherId", fatherId);
        List<NxGoodsEntity> nxGoodsEntities1 = nxGoodsService.queryNxGoodsByParams(map1);

        List<NxGoodsEntity> goodsEntities = new ArrayList<>();

        for (NxGoodsEntity goods : nxGoodsEntities1) {
            Map<String, Object> map = new HashMap<>();
            map.put("disId", disId);
            map.put("goodsId", goods.getNxGoodsId());
            List<GbDistributerGoodsEntity> dgGoods = gbDgService.queryAddDistributerNxGoods(map);

            if (dgGoods.size() > 0) {
                goods.setIsDownload(1);
                goods.setGbDistributerGoodsEntity(dgGoods.get(0));
                goodsEntities.add(goods);
            } else {
                goods.setIsDownload(0);
                goodsEntities.add(goods);
            }
        }

        int total = nxGoodsService.queryTotalByFatherId(fatherId);
        PageUtils pageUtil = new PageUtils(goodsEntities, total, limit, page);
        return R.ok().put("page", pageUtil);
    }


    @RequestMapping(value = "/depGetGbDisGoodsDetail", method = RequestMethod.POST)
    @ResponseBody
    public R depGetGbDisGoodsDetail(Integer disGoodsId, Integer depId) {

        //商品信息

        Map<String, Object> map4 = new HashMap<>();
        map4.put("disGoodsId", disGoodsId);
        map4.put("depId", depId);
        GbDistributerGoodsEntity disGoods = gbDgService.queryDisGoodsWithDepDisGoods(map4);

        //3ri订单
        List<Map<String, Object>> orderList = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disGoods.getGbDgDistributerId());
        map1.put("disGoodsId", disGoodsId);
        map1.put("orderType", disGoods.getGbDgGoodsType());
        map1.put("applyDate", formatWhatDay(0));
        map1.put("toDepId", depId);
        List<GbDepartmentOrdersEntity> departmentOrdersEntities = depOrdersService.queryOrdersForDisGoods(map1);
        Map<String, Object> mapone = new HashMap<>();
        mapone.put("date", formatWhatDayString(0));
        mapone.put("order", departmentOrdersEntities);
        orderList.add(mapone);

        map1.put("applyDate", formatWhatDay(-1));
        List<GbDepartmentOrdersEntity> departmentOrdersEntities2 = depOrdersService.queryOrdersForDisGoods(map1);
        Map<String, Object> maptwo = new HashMap<>();
        maptwo.put("date", formatWhatDayString(-1));
        maptwo.put("order", departmentOrdersEntities2);
        orderList.add(maptwo);

        map1.put("applyDate", formatWhatDay(-2));
        List<GbDepartmentOrdersEntity> departmentOrdersEntities3 = depOrdersService.queryOrdersForDisGoods(map1);
        Map<String, Object> mapthree = new HashMap<>();
        mapthree.put("date", formatWhatDayString(-2));
        mapthree.put("order", departmentOrdersEntities3);
        orderList.add(mapthree);


        //进货
        //进货
        Map<String, Object> map2 = new HashMap<>();
        map2.put("disGoodsId", disGoodsId);
        map2.put("startDate", formatWhatDay(-2));

        List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDisPurchaseGoodsService.queryOnlyPurGoods(map2);


        //客户
        Map<String, Object> map41 = new HashMap<>();
        map41.put("disGoodsId", disGoodsId);
        map41.put("depType", getGbDepartmentTypeMendian());
        System.out.println("41141" + map41);
        TreeSet<GbDepartmentEntity> departmentEntities = gbDepGoodsStockService.queryDepGoodsTreeDepartments(map41);
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity department : departmentEntities) {
                double depDoutbleRest = 0;
                double depDoutbleRestV = 0;
                Map<String, Object> mapDepStock = new HashMap<>();
                mapDepStock.put("disGoodsId", disGoodsId);
                mapDepStock.put("depFatherId", department.getGbDepartmentId());
                Integer integer = gbDepGoodsStockService.queryGoodsStockCount(mapDepStock);
                if (integer > 0) {
                    depDoutbleRest = gbDepGoodsStockService.queryDepStockRestWeightTotal(mapDepStock);
                    depDoutbleRestV = gbDepGoodsStockService.queryDepGoodsRestTotal(mapDepStock);
                }
                department.setDepRestGoodsTotalString(new BigDecimal(depDoutbleRestV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                department.setDepRestGoodsWeightTotalString(new BigDecimal(depDoutbleRest).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("orderArr", orderList);
        map.put("purchaseArr", goodsEntities);
        map.put("goodsInfo", disGoods);
        map.put("depGoodArr", departmentEntities);
        return R.ok().put("data", map);
    }

    //
//	/**
//	 * 批发商商品详细
//	 * @param disGoodsId 批发商商品id
//	 * @return 含有客户的商品
//	 */
    @RequestMapping(value = "/gbDisGetGoodsDetail/{disGoodsId}")
    @ResponseBody
    public R gbDisGetGoodsDetail(@PathVariable Integer disGoodsId) {

        //商品信息
        GbDistributerGoodsEntity disGoods = gbDgService.queryGbDisGoodsDetail(disGoodsId);

        //3ri订单
        List<Map<String, Object>> orderList = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disGoods.getGbDgDistributerId());
        map1.put("disGoodsId", disGoodsId);
        map1.put("orderType", disGoods.getGbDgGoodsType());
        map1.put("applyDate", formatWhatDay(0));
        System.out.println("abdddnnddmd111" + map1);
        List<GbDepartmentOrdersEntity> departmentOrdersEntities = depOrdersService.queryOrdersForDisGoods(map1);
        Map<String, Object> mapone = new HashMap<>();
        mapone.put("date", formatWhatDayString(0));
        mapone.put("order", departmentOrdersEntities);
        orderList.add(mapone);

        map1.put("applyDate", formatWhatDay(-1));
        System.out.println("abdddnnddmd222" + map1);
        List<GbDepartmentOrdersEntity> departmentOrdersEntities2 = depOrdersService.queryOrdersForDisGoods(map1);
        Map<String, Object> maptwo = new HashMap<>();
        maptwo.put("date", formatWhatDayString(-1));
        maptwo.put("order", departmentOrdersEntities2);
        orderList.add(maptwo);

        map1.put("applyDate", formatWhatDay(-2));
        List<GbDepartmentOrdersEntity> departmentOrdersEntities3 = depOrdersService.queryOrdersForDisGoods(map1);
        Map<String, Object> mapthree = new HashMap<>();
        mapthree.put("date", formatWhatDayString(-2));
        mapthree.put("order", departmentOrdersEntities3);
        orderList.add(mapthree);


        //进货
        Map<String, Object> map2 = new HashMap<>();
        map2.put("disGoodsId", disGoodsId);
        map2.put("startDate", formatWhatDay(-2));
//        map2.put("equalStatus", 3);
        System.out.println("purgooddd" + map2);
        List<GbDistributerPurchaseGoodsEntity> goodsEntities = gbDisPurchaseGoodsService.queryOnlyPurGoods(map2);

        //客户
        Map<String, Object> map41 = new HashMap<>();
        map41.put("disGoodsId", disGoodsId);
        map41.put("depType", getGbDepartmentTypeMendian());
        System.out.println("41141" + map41);
        TreeSet<GbDepartmentEntity> departmentEntities = gbDepGoodsStockService.queryDepGoodsTreeDepartments(map41);
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity department : departmentEntities) {
                double depDoutbleRest = 0;
                double depDoutbleRestV = 0;
                Map<String, Object> mapDepStock = new HashMap<>();
                mapDepStock.put("disGoodsId", disGoodsId);
                mapDepStock.put("depId", department.getGbDepartmentId());
                Integer integer = gbDepGoodsStockService.queryGoodsStockCount(mapDepStock);
                if (integer > 0) {
                    depDoutbleRest = gbDepGoodsStockService.queryDepStockRestWeightTotal(mapDepStock);
                    depDoutbleRestV = gbDepGoodsStockService.queryDepGoodsRestTotal(mapDepStock);
                }
                department.setDepRestGoodsTotalString(new BigDecimal(depDoutbleRestV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                department.setDepRestGoodsWeightTotalString(new BigDecimal(depDoutbleRest).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            }
        }

        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disGoodsId", disGoodsId);
        mapDep.put("depId", disGoods.getGbDgGbDepartmentId());
        GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepDisGoodsService.queryDepGoodsItemByParams(mapDep);

        Map<String, Object> map = new HashMap<>();
        map.put("orderArr", orderList);
        map.put("purchaseArr", goodsEntities);
        map.put("goodsInfo", disGoods);
        map.put("depGoodArr", departmentEntities);
        System.out.println("depgppd" + departmentDisGoodsEntity);
        map.put("depGoods", departmentDisGoodsEntity);
        return R.ok().put("data", map);
    }

    /**
     * @param searchStr 搜索字符串
     * @param disId     批发商id
     * @return 搜索结果
     */
    @RequestMapping(value = "/queryDisDepGoodsByQuickSearchGb", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisDepGoodsByQuickSearchGb(String searchStr, String disId, String depId, Integer isSelf) {

        Map<String, Object> map = new HashMap<>();

        map.put("disId", disId);
        map.put("depId", depId);
        map.put("isSelf", isSelf);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map.put("searchPinyin", pinyin);
            } else {
                map.put("searchPinyin", searchStr);
            }
        }

        List<GbDistributerGoodsEntity> goodsEntities = gbDgService.queryGbDisUnShlefGoodsQuickSearchStr(map);

        return R.ok().put("data", goodsEntities);
    }

    @RequestMapping(value = "/queryIsSelfGoodsByQuickSearchGb", method = RequestMethod.POST)
    @ResponseBody
    public R queryIsSelfGoodsByQuickSearchGb(String searchStr, String disId, String depId, Integer isSelf) {

        Map<String, Object> map = new HashMap<>();

        map.put("disId", disId);
        map.put("depId", depId);
        map.put("isSelf", isSelf);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map.put("searchPinyin", pinyin);
            } else {
                map.put("searchPinyin", searchStr);
            }
        }

        List<GbDistributerGoodsEntity> goodsEntities = gbDgService.queryGbDisUnShlefGoodsQuickSearchStr(map);

        return R.ok().put("data", goodsEntities);
    }

    /**
     * @param searchStr 搜索字符串
     * @param disId     批发商id
     * @return 搜索结果
     */
    @RequestMapping(value = "/queryDisGoodsByQuickSearchGb", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisGoodsByQuickSearchGb(String searchStr, String disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map.put("searchPinyin", pinyin);

                System.out.println("hahhaizizizi" + map);
            } else {
                map.put("searchPinyin", searchStr);
                System.out.println("searchPinyinsearchPinyin" + map);
            }
        }
        List<GbDistributerGoodsEntity> goodsEntities = gbDgService.queryGbDisGoodsQuickSearchStr(map);
        System.out.println("goodsssiziz" + goodsEntities.size());
        List<GbDistributerGoodsEntity> list = new ArrayList<>();
        if(goodsEntities.size() > 0){
            for(GbDistributerGoodsEntity gbDistributerGoodsEntity : goodsEntities){
                gbDgService.getStockTotal(gbDistributerGoodsEntity);
                list.add(gbDistributerGoodsEntity);
            }

        }
        return R.ok().put("data", list);
    }


//    @RequestMapping(value = "/queryNxAppGoodsQuickSearchWithDepOrder", method = RequestMethod.POST)
//    @ResponseBody
//    public R queryNxAppGoodsQuickSearchWithDepOrder(String searchStr, Integer nxDisId, Integer gbDisId,Integer depId) {
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("disId", gbDisId);
//        map.put("nxDisId", nxDisId);
//        map.put("depId", depId);
//
//        Map<String, Object> mapNx = new HashMap<>();
//        mapNx.put("disId", nxDisId);
//        map.put("searchStr", searchStr);
//        for (int i = 0; i < searchStr.length(); i++) {
//            String str = searchStr.substring(i, i + 1);
//            if (str.matches("[\u4E00-\u9FFF]")) {
//                String pinyin = hanziToPinyin(searchStr);
//                map.put("searchStr", searchStr);
//                map.put("searchPinyin", pinyin);
//                mapNx.put("searchStr", searchStr);
//                mapNx.put("searchPinyin", pinyin);
//            } else {
//                map.put("searchPinyin", searchStr);
//                mapNx.put("searchPinyin", searchStr);
//            }
//        }
//        System.out.println("depserardkddkdkdk" + map);
//        List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDgService.queryDisGoodsQuickSearchStrWithDepOrdersGb(map);
//        Map<String, Object> map3 = new HashMap<>();
//        map3.put("disArr", distributerGoodsEntities);
//        if (distributerGoodsEntities.size() > 0) {
//            if (distributerGoodsEntities.size() < 10) {
//                List<NxDistributerGoodsEntity> nxDisGoodsEntities = nxDistributerGoodsService.queryDisGoodsQuickSearchStr(mapNx);
//                if (nxDisGoodsEntities.size() > 0) {
//                    for (GbDistributerGoodsEntity disGoods : distributerGoodsEntities) {
//                        Integer GbDgNxGoodsId = disGoods.getGbDgNxDistributerGoodsId();
//                        for (int i = 0; i < nxDisGoodsEntities.size(); i++) {
//                            Integer nxGoodsId = nxDisGoodsEntities.get(i).getNxDistributerGoodsId();
//                            if (GbDgNxGoodsId.equals(nxGoodsId)) {
//                                nxDisGoodsEntities.remove(i);
//                            }
//                        }
//                    }
//                    map3.put("nxDisArr", nxDisGoodsEntities);
//                } else {
//                    map3.put("nxDisArr", new ArrayList<>());
//                }
//            }
//        } else {
//            map3.put("disArr", new ArrayList<>());
//            List<NxDistributerGoodsEntity> nxDisGoodsEntities = nxDistributerGoodsService.queryDisGoodsQuickSearchStr(mapNx);
//            if (nxDisGoodsEntities.size() > 0) {
//                map3.put("nxDisArr", nxDisGoodsEntities);
//            } else {
//                map3.put("nxDisArr", new ArrayList<>());
//            }
//
//        }
//
//        return R.ok().put("data", map3);
//    }


//    @RequestMapping(value = "/queryNxAppGoodsQuickSearch", method = RequestMethod.POST)
//    @ResponseBody
//    public R queryNxAppGoodsQuickSearch(String searchStr, Integer nxDisId, Integer gbDisId) {
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("disId", gbDisId);
//        map.put("nxDisId", nxDisId);
//
//        Map<String, Object> mapNx = new HashMap<>();
//        mapNx.put("disId", nxDisId);
//        map.put("searchStr", searchStr);
//        for (int i = 0; i < searchStr.length(); i++) {
//            String str = searchStr.substring(i, i + 1);
//            if (str.matches("[\u4E00-\u9FFF]")) {
//                String pinyin = hanziToPinyin(searchStr);
//                map.put("searchStr", searchStr);
//                map.put("searchPinyin", pinyin);
//                mapNx.put("searchStr", searchStr);
//                mapNx.put("searchPinyin", pinyin);
//            } else {
//                map.put("searchPinyin", searchStr);
//                mapNx.put("searchPinyin", searchStr);
//            }
//        }
//        List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDgService.queryGbDisGoodsQuickSearchStr(map);
//
//        Map<String, Object> map3 = new HashMap<>();
//        map3.put("disArr", distributerGoodsEntities);
//
//
//        if (distributerGoodsEntities.size() > 0) {
//            if (distributerGoodsEntities.size() < 10) {
//                List<NxDistributerGoodsEntity> nxDisGoodsEntities = nxDistributerGoodsService.queryDisGoodsQuickSearchStr(mapNx);
//                if (nxDisGoodsEntities.size() > 0) {
//                    for (GbDistributerGoodsEntity disGoods : distributerGoodsEntities) {
//                        Integer GbDgNxGoodsId = disGoods.getGbDgNxDistributerGoodsId();
//                        for (int i = 0; i < nxDisGoodsEntities.size(); i++) {
//                            Integer nxGoodsId = nxDisGoodsEntities.get(i).getNxDistributerGoodsId();
//                            if (GbDgNxGoodsId.equals(nxGoodsId)) {
//                                nxDisGoodsEntities.remove(i);
//                            }
//                        }
//                    }
//                    map3.put("nxDisArr", nxDisGoodsEntities);
//                } else {
//                    map3.put("nxDisArr", new ArrayList<>());
//                }
//            }
//        } else {
//            map3.put("disArr", new ArrayList<>());
//            List<NxDistributerGoodsEntity> nxDisGoodsEntities = nxDistributerGoodsService.queryDisGoodsQuickSearchStr(mapNx);
//            if (nxDisGoodsEntities.size() > 0) {
//                map3.put("nxDisArr", nxDisGoodsEntities);
//            } else {
//                map3.put("nxDisArr", new ArrayList<>());
//            }
//
//        }
//
//        return R.ok().put("data", map3);
//    }

    /**
     * @param searchStr 搜索字符串
     * @param disId     批发商id
     * @return 搜索结果
     */
    @RequestMapping(value = "/queryDisFatherGoodsByQuickSearchGb", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisFatherGoodsByQuickSearchGb(String searchStr, String disId, Integer fatherId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("fatherId", fatherId);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map.put("searchPinyin", pinyin);
            } else {
                map.put("searchPinyin", searchStr);
            }
        }
        List<GbDistributerGoodsEntity> goodsEntities = gbDgService.queryGbDisGoodsQuickSearchStr(map);
        return R.ok().put("data", goodsEntities);
    }

//    @RequestMapping(value = "/queryStockGoodsByQuickSearchGb", method = RequestMethod.POST)
//    @ResponseBody
//    public R queryStockGoodsByQuickSearchGb(String searchStr, String disId, String depId) {
//
//        Map<String, Object> map = new HashMap<>();
//        Map<String, Object> map1 = new HashMap<>();
//        map.put("disId", disId);
//        map.put("toDepId", depId);
//        for (int i = 0; i < searchStr.length(); i++) {
//            String str = searchStr.substring(i, i + 1);
//            if (str.matches("[\u4E00-\u9FFF]")) {
//                String pinyin = hanziToPinyin(searchStr);
//                map.put("searchStr", searchStr);
//                map.put("searchPinyin", pinyin);
//            } else {
//                map.put("searchPinyin", searchStr);
//            }
//        }
//        System.out.println("mapappaa" + map);
//
////        List<GbDistributerGoodsEntity> goodsEntities = gbDgService.queryDisGoodsQuickSearchStrWithDepOrdersGb(map);
//        TreeSet<GbDepartmentDisGoodsEntity> goodsEntities = gbDepDisGoodsService.queryDepDisGoodsQuickSearchStrGb(map);
//
//        return R.ok().put("data", goodsEntities);
//    }

    @RequestMapping(value = "/queryDepartmentGoodsByQuickSearchGb", method = RequestMethod.POST)
    @ResponseBody
    public R queryDepartmentGoodsByQuickSearchGb(String searchStr, String isSelf, String depId) {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("depId", depId);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map1.put("searchStr", searchStr);
                map1.put("searchPinyin", pinyin);
            } else {
                map1.put("searchPinyin", searchStr);
            }
        }

        System.out.println("mpa11111111" + map1);

        TreeSet<GbDepartmentDisGoodsEntity> disGoodsEntityTreeSet = gbDepDisGoodsService.queryDepDisGoodsQuickSearchStrGb(map1);


        return R.ok().put("data", disGoodsEntityTreeSet);
    }


    @RequestMapping(value = "/queryDepartmentIsSelfGoodsByQuickSearchGb", method = RequestMethod.POST)
    @ResponseBody
    public R queryDepartmentIsSelfGoodsByQuickSearchGb(String searchStr, Integer isSelf, String depId) {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("depId", depId);
//        map1.put("isSelf", isSelf);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map1.put("searchStr", searchStr);
                map1.put("searchPinyin", pinyin);
            } else {
                map1.put("searchPinyin", searchStr);
            }
        }

        TreeSet<GbDepartmentDisGoodsEntity> disGoodsEntityTreeSet = gbDepDisGoodsService.queryDepDisGoodsQuickSearchStrGb(map1);

        return R.ok().put("data", disGoodsEntityTreeSet);
    }

    @RequestMapping(value = "/getStockDepartmentGoodsPage")
    @ResponseBody
    public R getStockDepartmentGoodsPage(Integer depId, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("toDepId", depId);
        List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDgService.queryDisGoodsByParams(map);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("toDepId", depId);
        int total = gbDgService.queryGbStockGoodsTotal(map3);
        PageUtils pageUtil = new PageUtils(gbDistributerGoodsEntities, total, limit, page);
        return R.ok().put("page", pageUtil);
    }

    @RequestMapping(value = "/getGbGoodsListByFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R getGbGoodsListByFatherId(Integer fatherId, Integer limit, Integer page) {

        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("gbFatherId", fatherId);
        List<GbDistributerGoodsEntity> goodsEntities1 = gbDgService.queryGoodsByParamsGb(map);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("fatherId", fatherId);
        int total = gbDgService.queryGbGoodsTotal(map3);
        PageUtils pageUtil = new PageUtils(goodsEntities1, total, limit, page);
        return R.ok().put("page", pageUtil);
    }


    @RequestMapping(value = "/changeDisGoodsFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R changeDisGoodsFatherId(Integer disGoodsId, Integer fatherId) {

        //old
        GbDistributerGoodsEntity goodsEntity = gbDgService.queryObject(disGoodsId);
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", disGoodsId);

        //depDisGoods
        List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepDisGoodsService.queryGbDepDisGoodsByParams(map);
        if (departmentDisGoodsEntities.size() > 0) {
            for (GbDepartmentDisGoodsEntity depDisGoods : departmentDisGoodsEntities) {
                depDisGoods.setGbDdgDisGoodsFatherId(fatherId);
                GbDistributerFatherGoodsEntity fatherGoodsEntity = dgfService.queryObject(fatherId);
                depDisGoods.setGbDdgDisGoodsGrandId(fatherGoodsEntity.getGbDfgFathersFatherId());
                GbDistributerFatherGoodsEntity grand = dgfService.queryObject(fatherGoodsEntity.getGbDfgFathersFatherId());
                depDisGoods.setGbDdgDisGoodsGreatId(grand.getGbDfgFathersFatherId());

                gbDepDisGoodsService.update(depDisGoods);
            }
        }

        //depStock
        List<GbDepartmentGoodsStockEntity> stockEntities = gbDepGoodsStockService.queryGoodsStockByParams(map);
        if (stockEntities.size() > 0) {
            for (GbDepartmentGoodsStockEntity stockEntity : stockEntities) {
                stockEntity.setGbDgsGbDisGoodsFatherId(fatherId);
                gbDepGoodsStockService.update(stockEntity);
            }
        }

        //depDaily
        List<GbDepartmentGoodsDailyEntity> departmentGoodsDailyEntities = gbDepGoodsDailyService.queryDepGoodsDailyListByParams(map);
        if (departmentGoodsDailyEntities.size() > 0) {
            for (GbDepartmentGoodsDailyEntity dailyEntity : departmentGoodsDailyEntities) {
                dailyEntity.setGbDgdGbDisGoodsFatherId(fatherId);
                gbDepGoodsDailyService.update(dailyEntity);
            }
        }
        //depReduce
        List<GbDepartmentGoodsStockReduceEntity> reduceEntities = gbDepGoodsStockReduceService.queryStockReduceListByParams(map);
        if (reduceEntities.size() > 0) {
            for (GbDepartmentGoodsStockReduceEntity reduceEntity : reduceEntities) {
                reduceEntity.setGbDgsrGbDisGoodsFatherId(fatherId);
                gbDepGoodsStockReduceService.update(reduceEntity);
            }
        }


        Integer oldfgGoodsFatherId1 = goodsEntity.getGbDgDfgGoodsFatherId();
        GbDistributerFatherGoodsEntity fatherGoodsEntity = dgfService.queryObject(oldfgGoodsFatherId1);
        fatherGoodsEntity.setGbDfgGoodsAmount(fatherGoodsEntity.getGbDfgGoodsAmount() - 1);
        dgfService.update(fatherGoodsEntity);
        if (fatherGoodsEntity.getGbDfgGoodsAmount() == 0) {
            Integer grandId = fatherGoodsEntity.getGbDfgFathersFatherId();
            GbDistributerFatherGoodsEntity grandFather = dgfService.queryObject(grandId);
            if (grandFather.getGbDfgGoodsAmount() == 0) {
                Integer greatGrandId = grandFather.getGbDfgFathersFatherId();
                GbDistributerFatherGoodsEntity greatGrandFather = dgfService.queryObject(greatGrandId);
                if (greatGrandFather.getGbDfgGoodsAmount() == 0) {
                    dgfService.delete(greatGrandId);
                }
                dgfService.delete(grandId);
            }
            dgfService.delete(oldfgGoodsFatherId1);
        }

        //new
        GbDistributerFatherGoodsEntity fatherGoodsEntity1 = dgfService.queryObject(fatherId);
        fatherGoodsEntity1.setGbDfgGoodsAmount(fatherGoodsEntity1.getGbDfgGoodsAmount() + 1);
        dgfService.update(fatherGoodsEntity1);

        goodsEntity.setGbDgDfgGoodsFatherId(fatherId);
        goodsEntity.setGbDgDfgGoodsGrandId(fatherGoodsEntity1.getGbDfgFathersFatherId());

        GbDistributerFatherGoodsEntity grandFather = dgfService.queryObject(fatherGoodsEntity.getGbDfgFathersFatherId());
        goodsEntity.setGbDgDfgGoodsGreatId(grandFather.getGbDfgFathersFatherId());

        gbDgService.update(goodsEntity);

        return R.ok();
    }


    @RequestMapping(value = "/changeGbGoodsFresh", method = RequestMethod.POST)
    @ResponseBody
    public R changeGbGoodsFresh(@RequestBody GbDistributerGoodsEntity gbGoods) {
        Integer gbDistributerGoodsId = gbGoods.getGbDistributerGoodsId();

        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", gbDistributerGoodsId);
        map.put("date", formatWhatDay(0));
        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);

        if (integer > 0) {
            List<GbDepartmentGoodsDailyEntity> departmentGoodsDailyEntities = gbDepGoodsDailyService.queryDepGoodsDailyListByParams(map);
            for (GbDepartmentGoodsDailyEntity dailyEntity : departmentGoodsDailyEntities) {
                BigDecimal lastWeight = new BigDecimal(dailyEntity.getGbDgdLastWeight());
                BigDecimal todayWeight = new BigDecimal(dailyEntity.getGbDgdWeight());
                BigDecimal produceWeight = new BigDecimal(dailyEntity.getGbDgdProduceWeight());
                if (lastWeight.compareTo(BigDecimal.ZERO) == 0) {//没有剩余都是新鲜的
                    dailyEntity.setGbDgdFreshRate("100");
                } else {
                    if (todayWeight.compareTo(BigDecimal.ZERO) == 0) {//没有进货都是旧的
                        dailyEntity.setGbDgdFreshRate("0");
                    } else {
                        //有进新货，对比produce
                        //1, 没有销售
                        if (produceWeight.compareTo(BigDecimal.ZERO) == 0) {
                            dailyEntity.setGbDgdFreshRate("0");
                        } else {
                            //如果销售大于剩余量
                            if (produceWeight.compareTo(lastWeight) == 1) {
                                //销售有新货数量 12 有1斤是新的 8.33
                                BigDecimal subtract = produceWeight.subtract(lastWeight);
                                BigDecimal decimal = subtract.divide(produceWeight, 4, BigDecimal.ROUND_HALF_UP);
                                BigDecimal decimal1 = decimal.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                dailyEntity.setGbDgdFreshRate(decimal1.toString());
                            } else {
                                //销售的数量是剩余数量
                                dailyEntity.setGbDgdFreshRate("0");
                            }
                        }
                    }
                }
                gbDepGoodsDailyService.update(dailyEntity);
            }
        }

        gbDgService.update(gbGoods);

        return R.ok();
    }


    @RequestMapping(value = "/updateGbGoods", method = RequestMethod.POST)
    @ResponseBody
    public R updateGbGoods(@RequestBody GbDistributerGoodsEntity gbGoods) {

        Integer departmentId = gbGoods.getGbDgGbDepartmentId();

        //修改部门商品
        String goodsName = gbGoods.getGbDgGoodsName();
        Map<String, Object> map = new HashMap<>();
        map.put("disId", gbGoods.getGbDgDistributerId());
        map.put("goodsName", goodsName);
        map.put("standardName", gbGoods.getGbDgGoodsStandardname());
        map.put("detail", gbGoods.getGbDgGoodsDetail());
        map.put("brand", gbGoods.getGbDgGoodsBrand());
        map.put("place", gbGoods.getGbDgGoodsPlace());
        map.put("notEqualDisGoodsId", gbGoods.getGbDistributerGoodsId());
        System.out.println("mappa" + map);
        List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDgService.queryUpdateGoodsByParams(map);
        if (distributerGoodsEntities.size() > 1) {
            return R.error(-1, "存在相同商品");
        } else {
            String pinyin = hanziToPinyin(goodsName);
            String headPinyin = getHeadStringByString(goodsName, false, null);
            gbGoods.setGbDgGoodsPinyin(pinyin);
            gbGoods.setGbDgGoodsPy(headPinyin);
            gbDgService.update(gbGoods);

            Map<String, Object> mapG = new HashMap<>();
            mapG.put("disGoodsId", gbGoods.getGbDistributerGoodsId());
            List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepDisGoodsService.queryGbDepDisGoodsByParams(mapG);
            if (departmentDisGoodsEntities.size() > 0) {
                for (GbDepartmentDisGoodsEntity departmentDisGoodsEntity : departmentDisGoodsEntities) {
                    departmentDisGoodsEntity.setGbDdgDepGoodsPy(pinyin);
                    departmentDisGoodsEntity.setGbDdgDepGoodsPy(headPinyin);
                    departmentDisGoodsEntity.setGbDdgDepGoodsName(gbGoods.getGbDgGoodsName());
                    departmentDisGoodsEntity.setGbDdgShowStandardName(gbGoods.getGbDgGoodsStandardname());
                    departmentDisGoodsEntity.setGbDdgShowStandardWeight(gbGoods.getGbDgGoodsStandardWeight());
                    departmentDisGoodsEntity.setGbDdgDepGoodsDetail(gbGoods.getGbDgGoodsDetail());
                    departmentDisGoodsEntity.setGbDdgDepGoodsBrand(gbGoods.getGbDgGoodsBrand());
                    departmentDisGoodsEntity.setGbDdgDepGoodsPlace(gbGoods.getGbDgGoodsPlace());
                    gbDepDisGoodsService.update(departmentDisGoodsEntity);
                }
            }
            return R.ok().put("data", gbGoods);
        }
    }


    @RequestMapping(value = "/updateGbGoodsPullOff", method = RequestMethod.POST)
    @ResponseBody
    public R updateGbGoodsPullOff(@RequestBody GbDistributerGoodsEntity gbGoods) {

        if (gbGoods.getGbDgPullOff() == 1) {
            Map<String, Object> map = new HashMap<>();
            map.put("disGoodsId", gbGoods.getGbDistributerGoodsId());
            map.put("restWeight", 0);
            Integer stockCount = gbDepGoodsStockService.queryGoodsStockCount(map);
            if (stockCount > 0) {
                return R.error(-1, "有库存，不能暂停订货");
            } else {
                gbDgService.update(gbGoods);
                return R.ok().put("data", gbGoods);
            }
        } else {
            gbDgService.update(gbGoods);
            return R.ok().put("data", gbGoods);
        }

    }

    @RequestMapping(value = "/saveGbGoods", method = RequestMethod.POST)
    @ResponseBody
    public R saveGbGoods(@RequestBody GbDistributerGoodsEntity goods) {

        Integer gbDgNxDistributerId = goods.getGbDgNxDistributerId();
        if (gbDgNxDistributerId != -1) {
            Integer gbDgNxGoodsId = goods.getGbDgNxGoodsId();
            Map<String, Object> map = new HashMap<>();
            map.put("disId", gbDgNxDistributerId);
            map.put("goodsId", gbDgNxGoodsId);
            List<NxDistributerGoodsEntity> distributerGoodsEntities = nxDistributerGoodsService.queryDisGoodsByParams(map);
            if (distributerGoodsEntities.size() == 0) {
//                return R.error(-1, "供货商没有这个商品");
            } else {
                Integer distributerGoodsId = distributerGoodsEntities.get(0).getNxDistributerGoodsId();
                goods.setGbDgNxDistributerGoodsId(distributerGoodsId);
            }
        } else {
            goods.setGbDgNxDistributerId(-1);
            goods.setGbDgNxDistributerGoodsId(-1);
            goods.setGbDgNxDistributerGoodsPrice("0.1");
        }

        String goodsName = goods.getGbDgGoodsName();
        String pinyin = hanziToPinyin(goodsName);
        String headPinyin = getHeadStringByString(goodsName, false, null);
        goods.setGbDgGoodsPinyin(pinyin);
        goods.setGbDgGoodsPy(headPinyin);
        goods.setGbDgControlFresh(0);
        goods.setGbDgControlPrice(0);


        gbDgService.save(goods);


        //addDepDisGoods
        //添加部门商品
        GbDepartmentDisGoodsEntity disGoodsEntity = new GbDepartmentDisGoodsEntity();
        disGoodsEntity.setGbDdgDepGoodsName(goods.getGbDgGoodsName());
        disGoodsEntity.setGbDdgDisGoodsId(goods.getGbDistributerGoodsId());
        disGoodsEntity.setGbDdgDisGoodsFatherId(goods.getGbDgDfgGoodsFatherId());
        disGoodsEntity.setGbDdgDisGoodsGrandId(goods.getGbDgDfgGoodsGrandId());
        disGoodsEntity.setGbDdgDisGoodsGreatId(goods.getGbDgDfgGoodsGreatId());
        disGoodsEntity.setGbDdgDepGoodsPinyin(goods.getGbDgGoodsPinyin());
        disGoodsEntity.setGbDdgDepGoodsPy(goods.getGbDgGoodsPy());
        disGoodsEntity.setGbDdgDepGoodsStandardname(goods.getGbDgGoodsStandardname());
        disGoodsEntity.setGbDdgDepartmentId(goods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgDepartmentFatherId(goods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgGbDepartmentId(goods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgGbDisId(goods.getGbDgDistributerId());
        disGoodsEntity.setGbDdgGoodsType(goods.getGbDgGoodsType());
        disGoodsEntity.setGbDdgStockTotalWeight("0.0");
        disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
        disGoodsEntity.setGbDdgShowStandardId(-1);
        disGoodsEntity.setGbDdgShowStandardName(goods.getGbDgGoodsStandardname());
        disGoodsEntity.setGbDdgShowStandardScale("-1");
        disGoodsEntity.setGbDdgShowStandardWeight(null);
        disGoodsEntity.setGbDdgPrintStandard(goods.getGbDgGoodsStandardname());
        gbDepDisGoodsService.save(disGoodsEntity);

        //如果是餐饮商品，自动给门店添加部门商品
        Integer gbDgDistributerId = goods.getGbDgDistributerId();
        GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(gbDgDistributerId);

        if (gbDistributerEntity.getGbDistributerBusinessType() < 20) {
            Map<String, Object> map = new HashMap<>();
            map.put("disId", goods.getGbDgDistributerId());
            map.put("type", getGbDepartmentTypeMendian());
            List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
            if (departmentEntities.size() > 0) {
                for (GbDepartmentEntity dep : departmentEntities) {
                    GbDepartmentDisGoodsEntity disGoodsEntityDep = new GbDepartmentDisGoodsEntity();
                    disGoodsEntityDep.setGbDdgDepGoodsName(goods.getGbDgGoodsName());
                    disGoodsEntityDep.setGbDdgDisGoodsId(goods.getGbDistributerGoodsId());
                    disGoodsEntityDep.setGbDdgDisGoodsFatherId(goods.getGbDgDfgGoodsFatherId());
                    disGoodsEntity.setGbDdgDisGoodsGrandId(goods.getGbDgDfgGoodsGrandId());
                    disGoodsEntity.setGbDdgDisGoodsGreatId(goods.getGbDgDfgGoodsGreatId());
                    disGoodsEntityDep.setGbDdgDepGoodsPinyin(goods.getGbDgGoodsPinyin());
                    disGoodsEntityDep.setGbDdgDepGoodsPy(goods.getGbDgGoodsPy());
                    disGoodsEntityDep.setGbDdgDepGoodsStandardname(goods.getGbDgGoodsStandardname());
                    disGoodsEntityDep.setGbDdgDepartmentId(dep.getGbDepartmentId());
                    disGoodsEntityDep.setGbDdgDepartmentFatherId(dep.getGbDepartmentId());
                    disGoodsEntityDep.setGbDdgGbDepartmentId(goods.getGbDgGbDepartmentId());
                    disGoodsEntity.setGbDdgGbDisId(goods.getGbDgDistributerId());
                    disGoodsEntityDep.setGbDdgGoodsType(goods.getGbDgGoodsType());
                    disGoodsEntityDep.setGbDdgStockTotalWeight("0.0");
                    disGoodsEntityDep.setGbDdgStockTotalSubtotal("0.0");
                    disGoodsEntityDep.setGbDdgShowStandardId(-1);
                    disGoodsEntityDep.setGbDdgShowStandardName(goods.getGbDgGoodsStandardname());
                    disGoodsEntityDep.setGbDdgShowStandardScale("-1");
                    disGoodsEntityDep.setGbDdgGbDisId(goods.getGbDgDistributerId());
                    disGoodsEntity.setGbDdgPrintStandard(goods.getGbDgGoodsStandardname());
                    gbDepDisGoodsService.save(disGoodsEntityDep);
                }
            }
        }

        Integer GbDgDfgGoodsFatherId1 = goods.getGbDgDfgGoodsFatherId();

        GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity = dgfService.queryObject(GbDgDfgGoodsFatherId1);
        Integer gbDfgGoodsAmount = gbDistributerFatherGoodsEntity.getGbDfgGoodsAmount();
        gbDistributerFatherGoodsEntity.setGbDfgGoodsAmount(gbDfgGoodsAmount + 1);
        dgfService.update(gbDistributerFatherGoodsEntity);
        return R.ok().put("data", goods);
    }


    @RequestMapping(value = "/selfMendainSaveGbGoods", method = RequestMethod.POST)
    @ResponseBody
    public R selfMendainSaveGbGoods(@RequestBody GbDistributerGoodsEntity goods) {

        Integer gbDgNxDistributerId = goods.getGbDgNxDistributerId();
        if (gbDgNxDistributerId != -1) {
            Map<String, Object> map = new HashMap<>();
            map.put("disId", gbDgNxDistributerId);
            map.put("goodsName", goods.getGbDgGoodsName());
            map.put("goodsStandard", goods.getGbDgGoodsStandardname());
            List<NxDistributerGoodsEntity> distributerGoodsEntities = nxDistributerGoodsService.queryIfHasSameDisGoods(map);
            if (distributerGoodsEntities.size() == 0) {

//                return R.error(-1, "供货商没有这个商品");
            } else {
                Integer distributerGoodsId = distributerGoodsEntities.get(0).getNxDistributerGoodsId();
                goods.setGbDgNxDistributerGoodsId(distributerGoodsId);
            }
        } else {

            goods.setGbDgNxDistributerId(-1);
            goods.setGbDgNxDistributerGoodsId(-1);
            goods.setGbDgNxDistributerGoodsPrice("0.1");
        }

        String goodsName = goods.getGbDgGoodsName();
        String pinyin = hanziToPinyin(goodsName);
        String headPinyin = getHeadStringByString(goodsName, false, null);
        goods.setGbDgGoodsPinyin(pinyin);
        goods.setGbDgGoodsPy(headPinyin);
        goods.setGbDgControlFresh(0);
        goods.setGbDgControlPrice(0);
        gbDgService.save(goods);


        //addDepDisGoods
        //添加部门商品
        Integer gbDgDistributerId = goods.getGbDgDistributerId();

        Map<String, Object> map = new HashMap<>();
        map.put("disId", gbDgDistributerId);
        GbDepartmentEntity dep = gbDepartmentService.queryDepInfoByDisId(map);

        GbDepartmentDisGoodsEntity disGoodsEntityDep = new GbDepartmentDisGoodsEntity();
        disGoodsEntityDep.setGbDdgDepGoodsName(goods.getGbDgGoodsName());
        disGoodsEntityDep.setGbDdgDisGoodsId(goods.getGbDistributerGoodsId());
        disGoodsEntityDep.setGbDdgDisGoodsFatherId(goods.getGbDgDfgGoodsFatherId());
        disGoodsEntityDep.setGbDdgDisGoodsGrandId(goods.getGbDgDfgGoodsGrandId());
        disGoodsEntityDep.setGbDdgDisGoodsGreatId(goods.getGbDgDfgGoodsGreatId());
        disGoodsEntityDep.setGbDdgDepGoodsPinyin(goods.getGbDgGoodsPinyin());
        disGoodsEntityDep.setGbDdgDepGoodsPy(goods.getGbDgGoodsPy());
        disGoodsEntityDep.setGbDdgDepGoodsStandardname(goods.getGbDgGoodsStandardname());
        disGoodsEntityDep.setGbDdgDepartmentId(dep.getGbDepartmentId());
        disGoodsEntityDep.setGbDdgDepartmentFatherId(dep.getGbDepartmentId());
        disGoodsEntityDep.setGbDdgGbDepartmentId(goods.getGbDgGbDepartmentId());
        disGoodsEntityDep.setGbDdgGoodsType(goods.getGbDgGoodsType());
        disGoodsEntityDep.setGbDdgStockTotalWeight("0.0");
        disGoodsEntityDep.setGbDdgStockTotalSubtotal("0.0");
        disGoodsEntityDep.setGbDdgShowStandardId(-1);
        disGoodsEntityDep.setGbDdgShowStandardName(goods.getGbDgGoodsStandardname());
        disGoodsEntityDep.setGbDdgShowStandardScale("-1");
        disGoodsEntityDep.setGbDdgGbDisId(goods.getGbDgDistributerId());
        disGoodsEntityDep.setGbDdgPrintStandard(goods.getGbDgGoodsStandardname());
        gbDepDisGoodsService.save(disGoodsEntityDep);
        Integer GbDgDfgGoodsFatherId1 = goods.getGbDgDfgGoodsFatherId();

        GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity = dgfService.queryObject(GbDgDfgGoodsFatherId1);
        Integer gbDfgGoodsAmount = gbDistributerFatherGoodsEntity.getGbDfgGoodsAmount();
        gbDistributerFatherGoodsEntity.setGbDfgGoodsAmount(gbDfgGoodsAmount + 1);
        dgfService.update(gbDistributerFatherGoodsEntity);
        return R.ok().put("data", goods);
    }

    @RequestMapping(value = "/deleteGbGoods/{goodsId}")
    @ResponseBody
    public R deleteGbGoods(@PathVariable Integer goodsId) {
        gbDgService.delete(goodsId);
        return R.ok();
    }

    @RequestMapping(value = "/getGoodsSubNamesByFatherIdGb/{fatherId}")
    @ResponseBody
    public R getGoodsSubNamesByFatherIdGb(@PathVariable Integer fatherId) {


        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.querySubFatherGoods(fatherId);

        List<GbDistributerFatherGoodsEntity> newList = new ArrayList<>();

        for (GbDistributerFatherGoodsEntity fatherGoods : fatherGoodsEntities) {
            StringBuilder builder = new StringBuilder();

            List<GbDistributerGoodsEntity> goodsEntities = gbDgService.querySubNameByFatherId(fatherGoods.getGbDistributerFatherGoodsId());
            for (GbDistributerGoodsEntity goods : goodsEntities) {
                String nxGoodsName = goods.getGbDgGoodsName();
                builder.append(nxGoodsName);
                builder.append(',');
            }
            fatherGoods.setGbDgGoodsSubNames(builder.toString());
            newList.add(fatherGoods);
        }

        return R.ok().put("data", newList);
    }


    @RequestMapping(value = "/canclePostDgnGoodsGb", method = RequestMethod.POST)
    @ResponseBody
    public R canclePostDgnGoodsGb(Integer disGoodsId, Integer disGoodsFatherId, Integer disId) {
        Map<String, Object> map5 = new HashMap<>();
        map5.put("disGoodsId", disGoodsId);
        System.out.println("mapapaap" + map5);
        Integer orderAmount = depOrdersService.queryGbDepartmentOrderAmount(map5);
        Integer stockCount = gbDepGoodsStockService.queryGoodsStockCount(map5);
        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map5);
        if (orderAmount > 0 || stockCount > 0 || integer > 0) {
            return R.error(-1, "此商品在使用中");
        } else {

            GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity = dgfService.queryObject(disGoodsFatherId);
            Integer gbDfgGoodsAmount = gbDistributerFatherGoodsEntity.getGbDfgGoodsAmount();
            gbDistributerFatherGoodsEntity.setGbDfgGoodsAmount(gbDfgGoodsAmount - 1);
            dgfService.update(gbDistributerFatherGoodsEntity);

            Map<String, Object> map1 = new HashMap<>();
            map1.put("disId", disId);
            map1.put("dgFatherId", disGoodsFatherId);
            //搜索fatherId下有几个disGoods
            List<GbDistributerGoodsEntity> totalDisGoods = gbDgService.queryDisGoodsByParams(map1);
            //如果disGoods的父类只有一个商品
            if (totalDisGoods.size() == 1 && totalDisGoods.get(0).getGbDgNxGoodsId() != null) {
                //父类Entity
                GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity0 = dgfService.queryObject(disGoodsFatherId);
                //disGoods的grandId
                Integer grandId = gbDistributerFatherGoodsEntity0.getGbDfgFathersFatherId();
                Map<String, Object> mapGrand = new HashMap<>();
                mapGrand.put("fathersFatherId", grandId);
                //搜索grand有几个兄弟
                List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisFathersGoodsByParamsGb(mapGrand);
                if (fatherGoodsEntities.size() == 1) {
                    Integer gbDfgFathersFatherId = fatherGoodsEntities.get(0).getGbDfgFathersFatherId();
                    GbDistributerFatherGoodsEntity grandEntity = dgfService.queryObject(gbDfgFathersFatherId);
                    Integer greatGrandId = grandEntity.getGbDfgFathersFatherId();
                    Map<String, Object> map = new HashMap<>();
                    map.put("fathersFatherId", greatGrandId);
                    List<GbDistributerFatherGoodsEntity> grandGoodsEntities = dgfService.queryDisFathersGoodsByParamsGb(map);

                    //如果grandFather也是只有一个，则删除greatGrandFather
                    if (grandGoodsEntities.size() == 1) {
                        dgfService.delete(greatGrandId);
                    }
                    dgfService.delete(grandId);
                }
                dgfService.delete(disGoodsFatherId);
            } else {
                //父类商品数量减去1
                GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity1 = dgfService.queryObject(disGoodsFatherId);
                Integer gbDfgGoodsAmount1 = gbDistributerFatherGoodsEntity.getGbDfgGoodsAmount();
                gbDistributerFatherGoodsEntity.setGbDfgGoodsAmount(gbDfgGoodsAmount1 - 1);
                dgfService.update(gbDistributerFatherGoodsEntity1);
            }

            //删除订货单位
            List<GbDistributerStandardEntity> standardEntities = gbDistributerStandardService.queryDisStandardByDisGoodsIdGb(disGoodsId);
            if (standardEntities.size() > 0) {
                for (GbDistributerStandardEntity disStandard : standardEntities) {
                    gbDistributerStandardService.delete(disStandard.getGbDistributerStandardId());
                }
            }

            int i = gbDgService.delete(disGoodsId);

            Map<String, Object> map = new HashMap<>();
            map.put("disGoodsId", disGoodsId);
            List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities1 = gbDepDisGoodsService.queryGbDepDisGoodsByParams(map);
            if (departmentDisGoodsEntities1.size() > 0) {
                for (GbDepartmentDisGoodsEntity disGoodsEntity : departmentDisGoodsEntities1) {
                    gbDepDisGoodsService.delete(disGoodsEntity.getGbDepartmentDisGoodsId());
                }
            }

            if (i == 1) {
                return R.ok();
            } else {
                return R.error(-1, "删除失败");
            }

        }

    }

//    @RequestMapping(value = "/canclePostDgnGoodsGb", method = RequestMethod.POST)
//    @ResponseBody
//    public R canclePostDgnGoodsGb(Integer disGoodsId, Integer disGoodsFatherId, Integer disId) {
//        Map<String, Object> map5 = new HashMap<>();
//        map5.put("disGoodsId", disGoodsId);
//        System.out.println("mapapaap" + map5);
//        Integer orderAmount = depOrdersService.queryGbDepartmentOrderAmount(map5);
//        Integer stockCount = gbDepGoodsStockService.queryGoodsStockCount(map5);
//        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map5);
//        if (orderAmount > 0 || stockCount > 0 || integer > 0) {
//            return R.error(-1, "此商品在使用中");
//        } else {
//
//            GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity = dgfService.queryObject(disGoodsFatherId);
//            Integer gbDfgGoodsAmount = gbDistributerFatherGoodsEntity.getGbDfgGoodsAmount();
//            gbDistributerFatherGoodsEntity.setGbDfgGoodsAmount(gbDfgGoodsAmount - 1);
//            dgfService.update(gbDistributerFatherGoodsEntity);
//
//            Map<String, Object> map1 = new HashMap<>();
//            map1.put("disId", disId);
//            map1.put("dgFatherId", disGoodsFatherId);
//            //搜索fatherId下有几个disGoods
//            List<GbDistributerGoodsEntity> totalDisGoods = gbDgService.queryDisGoodsByParams(map1);
//            //如果disGoods的父类只有一个商品
//            if (totalDisGoods.size() == 1 && totalDisGoods.get(0).getGbDgNxGoodsId() != null) {
//                //父类Entity
//                GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity0 = dgfService.queryObject(disGoodsFatherId);
//                //disGoods的grandId
//                Integer grandId = gbDistributerFatherGoodsEntity0.getGbDfgFathersFatherId();
//                Map<String, Object> mapGrand = new HashMap<>();
//                mapGrand.put("fathersFatherId", grandId);
//                //搜索grand有几个兄弟
//                List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisFathersGoodsByParamsGb(mapGrand);
//                if (fatherGoodsEntities.size() == 1) {
//                    Integer gbDfgFathersFatherId = fatherGoodsEntities.get(0).getGbDfgFathersFatherId();
//                    GbDistributerFatherGoodsEntity grandEntity = dgfService.queryObject(gbDfgFathersFatherId);
//                    Integer greatGrandId = grandEntity.getGbDfgFathersFatherId();
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("fathersFatherId", greatGrandId);
//                    List<GbDistributerFatherGoodsEntity> grandGoodsEntities = dgfService.queryDisFathersGoodsByParamsGb(map);
//
//                    //如果grandFather也是只有一个，则删除greatGrandFather
//                    if (grandGoodsEntities.size() == 1) {
//                        dgfService.delete(greatGrandId);
//                    }
//                    dgfService.delete(grandId);
//                }
//                dgfService.delete(disGoodsFatherId);
//            } else {
//                //父类商品数量减去1
//                GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity1 = dgfService.queryObject(disGoodsFatherId);
//                Integer gbDfgGoodsAmount1 = gbDistributerFatherGoodsEntity.getGbDfgGoodsAmount();
//                gbDistributerFatherGoodsEntity.setGbDfgGoodsAmount(gbDfgGoodsAmount1 - 1);
//                dgfService.update(gbDistributerFatherGoodsEntity1);
//            }
//
//            //删除订货单位
//            List<GbDistributerStandardEntity> standardEntities = gbDistributerStandardService.queryDisStandardByDisGoodsIdGb(disGoodsId);
//            if (standardEntities.size() > 0) {
//                for (GbDistributerStandardEntity disStandard : standardEntities) {
//                    gbDistributerStandardService.delete(disStandard.getGbDistributerStandardId());
//                }
//            }
//
//            int i = gbDgService.delete(disGoodsId);
//
//            Map<String, Object> map = new HashMap<>();
//            map.put("disGoodsId", disGoodsId);
//            List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities1 = gbDepDisGoodsService.queryGbDepDisGoodsByParams(map);
//            if (departmentDisGoodsEntities1.size() > 0) {
//                for (GbDepartmentDisGoodsEntity disGoodsEntity : departmentDisGoodsEntities1) {
//                    gbDepDisGoodsService.delete(disGoodsEntity.getGbDepartmentDisGoodsId());
//                }
//            }
//
//            if (i == 1) {
//                return R.ok();
//            } else {
//                return R.error(-1, "删除失败");
//            }
//
//        }
//
//    }
    @RequestMapping(value = "/singleMendainCanclePostDgnGoodsGb", method = RequestMethod.POST)
    @ResponseBody
    public R singleMendainCanclePostDgnGoodsGb(Integer disGoodsId, Integer disGoodsFatherId, Integer disId) {
        Map<String, Object> map5 = new HashMap<>();
        map5.put("disGoodsId", disGoodsId);
        Integer orderAmount = depOrdersService.queryGbDepartmentOrderAmount(map5);

        Integer stockCount = gbDepGoodsStockService.queryGoodsStockCount(map5);

        Map<String, Object> mapDisGoods = new HashMap<>();
        mapDisGoods.put("disGoodsId", disGoodsId);
        mapDisGoods.put("depType", getGbDepartmentTypeMendian());
        List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepDisGoodsService.queryGbDepDisGoodsByParams(mapDisGoods);

        if (departmentDisGoodsEntities.size() > 0) {
            for (GbDepartmentDisGoodsEntity departmentDisGoodsEntity : departmentDisGoodsEntities) {
                gbDepDisGoodsService.delete(departmentDisGoodsEntity.getGbDepartmentDisGoodsId());
            }
        }

        if (orderAmount > 0 || stockCount > 0) {
            return R.error(-1, "此商品在使用中");
        } else {

            GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity = dgfService.queryObject(disGoodsFatherId);
            Integer gbDfgGoodsAmount = gbDistributerFatherGoodsEntity.getGbDfgGoodsAmount();
            gbDistributerFatherGoodsEntity.setGbDfgGoodsAmount(gbDfgGoodsAmount - 1);
            dgfService.update(gbDistributerFatherGoodsEntity);

            Map<String, Object> map1 = new HashMap<>();
            map1.put("disId", disId);
            map1.put("dgFatherId", disGoodsFatherId);
            //搜索fatherId下有几个disGoods
            List<GbDistributerGoodsEntity> totalDisGoods = gbDgService.queryDisGoodsByParams(map1);
            //如果disGoods的父类只有一个商品
            if (totalDisGoods.size() == 1) {
                //父类Entity
                GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity0 = dgfService.queryObject(disGoodsFatherId);
                //disGoods的grandId
                Integer grandId = gbDistributerFatherGoodsEntity0.getGbDfgFathersFatherId();
                Map<String, Object> mapGrand = new HashMap<>();
                mapGrand.put("fathersFatherId", grandId);
                //搜索grand有几个兄弟
                List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisFathersGoodsByParamsGb(mapGrand);
                if (fatherGoodsEntities.size() == 1) {
                    Integer gbDfgFathersFatherId = fatherGoodsEntities.get(0).getGbDfgFathersFatherId();
                    GbDistributerFatherGoodsEntity grandEntity = dgfService.queryObject(gbDfgFathersFatherId);
                    Integer greatGrandId = grandEntity.getGbDfgFathersFatherId();
                    Map<String, Object> map = new HashMap<>();
                    map.put("fathersFatherId", greatGrandId);
                    List<GbDistributerFatherGoodsEntity> grandGoodsEntities = dgfService.queryDisFathersGoodsByParamsGb(map);

                    //如果grandFather也是只有一个，则删除greatGrandFather
                    if (grandGoodsEntities.size() == 1) {
                        dgfService.delete(greatGrandId);
                    }
                    dgfService.delete(grandId);
                }
                dgfService.delete(disGoodsFatherId);
            } else {
                //父类商品数量减去1
                GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity1 = dgfService.queryObject(disGoodsFatherId);
                Integer gbDfgGoodsAmount1 = gbDistributerFatherGoodsEntity.getGbDfgGoodsAmount();
                gbDistributerFatherGoodsEntity.setGbDfgGoodsAmount(gbDfgGoodsAmount1 - 1);
                dgfService.update(gbDistributerFatherGoodsEntity1);
            }

            //删除订货单位
            List<GbDistributerStandardEntity> standardEntities = gbDistributerStandardService.queryDisStandardByDisGoodsIdGb(disGoodsId);
            if (standardEntities.size() > 0) {
                for (GbDistributerStandardEntity disStandard : standardEntities) {
                    gbDistributerStandardService.delete(disStandard.getGbDistributerStandardId());
                }
            }

            int i = gbDgService.delete(disGoodsId);

            Map<String, Object> map = new HashMap<>();
            map.put("disGoodsId", disGoodsId);
            List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities1 = gbDepDisGoodsService.queryGbDepDisGoodsByParams(map);
            if (departmentDisGoodsEntities1.size() > 0) {
                for (GbDepartmentDisGoodsEntity disGoodsEntity : departmentDisGoodsEntities1) {
                    gbDepDisGoodsService.delete(disGoodsEntity.getGbDepartmentDisGoodsId());
                }
            }

            if (i == 1) {
                return R.ok();
            } else {
                return R.error(-1, "删除失败");
            }

        }

    }


    //
    @RequestMapping("/downloadExcelGbDep")
    @ResponseBody
    public void downloadExcelGbDep(HttpServletResponse response, HttpServletRequest request) {
        String depId = request.getParameter("depId");
        String fatherName = request.getParameter("depName");
        System.out.println("deelleleleleelleel" + depId);
        try {
            HSSFWorkbook wb = new HSSFWorkbook();
            BigDecimal total = new BigDecimal(0);
            Map<String, Object> map1 = new HashMap<>();
            map1.put("toDepId", depId);
            System.out.println(map1);
            List<GbDistributerFatherGoodsEntity> greatGrandEntities = gbDgService.queryDisFatherGoodsByParams(map1);
            for (GbDistributerFatherGoodsEntity great : greatGrandEntities) {
//                Map<String, Object> map = new HashMap<>();
//                map.put("fathersFatherId", great.getGbDistributerFatherGoodsId() );
//                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = dgfService.queryDisFathersGoodsByParamsGb(map);

                for (GbDistributerFatherGoodsEntity grand : great.getFatherGoodsEntities()) {

//                    Map<String, Object> map2 = new HashMap<>();
//                    map2.put("fathersFatherId", grand.getGbDistributerFatherGoodsId() );
//                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisFathersGoodsByParamsGb(map2);

//                    for (GbDistributerFatherGoodsEntity fatherGoodsEntity : fatherGoodsEntities) {
//                    TreeSet<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();

//                    for (int m = 0; m < fatherGoodsEntities.size(); m++) {
//                        String nxGoodsName = fatherGoodsEntities.get(m).getGbDfgFatherGoodsName();
//                        System.out.println("nxGoodsName" + nxGoodsName);
//                        String replace = nxGoodsName.replace("/", "-");
//                        total = total.add(new BigDecimal(1));
//
//                        HSSFSheet sheet = wb.createSheet(total + " " + replace);
////                        //设置表头
////                        HSSFRow row = sheet.createRow(0);
////                        row.createCell(0).setCellValue(total + " " + replace);
//
//                        //设置表头
//                        HSSFRow row1 = sheet.createRow(0);
//
//                        row1.createCell(0).setCellValue("序号");
//                        row1.createCell(1).setCellValue("goodsId");
//                        row1.createCell(2).setCellValue("goodsFatherId");
//                        row1.createCell(3).setCellValue("商品名称");
//                        row1.createCell(4).setCellValue("规格");
//                        row1.createCell(5).setCellValue("品牌");
//                        row1.createCell(6).setCellValue("产地");
//                        row1.createCell(7).setCellValue("库存");
//                        row1.createCell(8).setCellValue("单价");
//
//
//                        Map<String, Object> map21 = new HashMap<>();
//                        map21.put("dgFatherId", fatherGoodsEntities.get(m).getGbDistributerFatherGoodsId());
//                        map21.put("toDepId", depId);
//                        List<GbDistributerGoodsEntity> goodsEntities = gbDgService.queryDisGoodsByParams(map21);
//                        //设置表体
//                        HSSFRow goodsRow = null;
//                        for (int i = 0; i < goodsEntities.size(); i++) {
//                            GbDistributerGoodsEntity ckGoodsEntity = goodsEntities.get(i);
//                            goodsRow = sheet.createRow(i + 2);
//                            goodsRow.createCell(0).setCellValue(i + 1);
//                            goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDistributerGoodsId());
//                            goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgDfgGoodsFatherId());
//                            goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsName());
//                            goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
//                            goodsRow.createCell(5).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
//                            goodsRow.createCell(6).setCellValue(ckGoodsEntity.getGbDgGoodsPlace());
//                        }
//
//                    }

//                    }
                }


            }


            String fileName = new String("导出商品.xls".getBytes("utf-8"), "iso8859-1");
            response.setHeader("content-Disposition", "attachment; filename =" + fileName);
            wb.write(response.getOutputStream());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @RequestMapping("/downloadExcelGb")
    @ResponseBody
    public void downloadExcelGb(HttpServletResponse response, HttpServletRequest request) {
        String fatherId = request.getParameter("fatherId");
        String fatherName = request.getParameter("fatherName");
        try {
            HSSFWorkbook wb = new HSSFWorkbook();
            BigDecimal total = new BigDecimal(0);
            Map<String, Object> map1 = new HashMap<>();
            map1.put("fathersFatherId", fatherId);
            List<GbDistributerFatherGoodsEntity> grandEntities = dgfService.queryDisFathersGoodsByParamsGb(map1);
            for (int m = 0; m < grandEntities.size(); m++) {
                String nxGoodsName = grandEntities.get(m).getGbDfgFatherGoodsName();
                String replace = nxGoodsName.replace("/", "-");
                total = total.add(new BigDecimal(1));

                HSSFSheet sheet = wb.createSheet(total + " " + replace);
                //设置表头
                HSSFRow row = sheet.createRow(0);
                row.createCell(0).setCellValue(total + " " + replace);

                //设置表头
                HSSFRow row1 = sheet.createRow(1);

                row1.createCell(0).setCellValue("序号");
                row1.createCell(1).setCellValue("商品名称");
                row1.createCell(2).setCellValue("规格");
                row1.createCell(3).setCellValue("品牌");
                row1.createCell(4).setCellValue("产地");
                row1.createCell(5).setCellValue("介绍");


                Map<String, Object> map2 = new HashMap<>();
                map2.put("dgFatherId", grandEntities.get(m).getGbDistributerFatherGoodsId());
                List<GbDistributerGoodsEntity> goodsEntities = gbDgService.queryDisGoodsByParams(map2);
                //设置表体
                HSSFRow goodsRow = null;
                for (int i = 0; i < goodsEntities.size(); i++) {
                    GbDistributerGoodsEntity ckGoodsEntity = goodsEntities.get(i);
                    goodsRow = sheet.createRow(i + 2);
                    goodsRow.createCell(0).setCellValue(i + 1);
                    goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                    goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                    goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                    goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsPlace());
                    goodsRow.createCell(5).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
                }


            }

            String fileName = new String("导出商品.xls".getBytes("utf-8"), "iso8859-1");
            response.setHeader("content-Disposition", "attachment; filename =" + fileName);
            wb.write(response.getOutputStream());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


//    private void changeInventoryDailyData(GbDistributerGoodsEntity gbGoods) {
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("settleId", -1);
//        map1.put("disGoodsId", gbGoods.getGbDistributerGoodsId());
//        List<GbDepInventoryGoodsDailyEntity> dailyEntities = gbDepInventoryGoodsDailyService.queryDailyStockListByParams(map1);
//        if (dailyEntities.size() > 0) {
//            for (GbDepInventoryGoodsDailyEntity dailyEntity : dailyEntities) {
//                //update Week, Month
//                Map<String, Object> map2 = new HashMap<>();
//                map2.put("depId", dailyEntity.getGbIgdDepartmentId());
//                Integer newGoodsInventoryType = gbGoods.getGbDgGoodsInventoryType();
//                if (newGoodsInventoryType.equals(getDISGoodsInventroyWeek())) {
//                    // 1, 添加新weekGoods
//                    GbDepInventoryGoodsWeekEntity weekGoodsEntity = new GbDepInventoryGoodsWeekEntity();
//                    weekGoodsEntity.setGbIgwReturnWeight(dailyEntity.getGbIgdReturnWeight());
//                    weekGoodsEntity.setGbIgwReturnSubtotal(dailyEntity.getGbIgdReturnSubtotal());
//                    weekGoodsEntity.setGbIgwStatus(dailyEntity.getGbIgdStatus());
//                    weekGoodsEntity.setGbIgwWeek(getWeekOfYear(0).toString());
//                    weekGoodsEntity.setGbIgwYear(formatWhatYear(0));
//                    weekGoodsEntity.setGbIgwLossWeight(dailyEntity.getGbIgdLossWeight());
//                    weekGoodsEntity.setGbIgwLossSubtotal(dailyEntity.getGbIgdLossSubtotal());
//                    weekGoodsEntity.setGbIgwWasteSubtotal(dailyEntity.getGbIgdWasteSubtotal());
//                    weekGoodsEntity.setGbIgwWasteWeight(dailyEntity.getGbIgdWasteWeight());
//                    weekGoodsEntity.setGbIgwProduceWeight(dailyEntity.getGbIgdProduceWeight());
//                    weekGoodsEntity.setGbIgwProduceSubtotal(dailyEntity.getGbIgdProduceSubtotal());
//                    weekGoodsEntity.setGbIgwReturnWeight(dailyEntity.getGbIgdReturnWeight());
//                    weekGoodsEntity.setGbIgwReturnSubtotal(dailyEntity.getGbIgdReturnSubtotal());
//
//                    weekGoodsEntity.setGbIgwDepartmentFatherId(dailyEntity.getGbIgdDepartmentFatherId());
//                    weekGoodsEntity.setGbIgwDepartmentId(dailyEntity.getGbIgdDepartmentId());
//                    weekGoodsEntity.setGbIgwDisGoodsFatherId(dailyEntity.getGbIgdDisGoodsFatherId());
//                    weekGoodsEntity.setGbIgwDisGoodsId(dailyEntity.getGbIgdDisGoodsId());
//                    weekGoodsEntity.setGbIgwDistributerId(dailyEntity.getGbIgdDistributerId());
//                    weekGoodsEntity.setGbIgwGbDepStockId(dailyEntity.getGbIgdGbDepStockId());
//                    weekGoodsEntity.setGbIgwFullTime(dailyEntity.getGbIgdFullTime());
//                    weekGoodsEntity.setGbIgwDepDisGoodsId(dailyEntity.getGbIgdDepDisGoodsId());
//                    gbDepInventoryGoodsWeekService.save(weekGoodsEntity);
//
//
//                    GbDepInventoryWeekEntity inventoryWeekEntity = gbDepInventoryWeekService.queryInventoryWeek(map2);
//
//                    BigDecimal wasteTotal = new BigDecimal(inventoryWeekEntity.getGbDiwWasteTotal()).add(new BigDecimal(dailyEntity.getGbIgdWasteSubtotal()));
//                    BigDecimal returnTotal = new BigDecimal(inventoryWeekEntity.getGbDiwReturnTotal()).add(new BigDecimal(dailyEntity.getGbIgdReturnSubtotal()));
//                    BigDecimal lossTotal = new BigDecimal(inventoryWeekEntity.getGbDiwLossTotal()).add(new BigDecimal(dailyEntity.getGbIgdLossSubtotal()));
//                    BigDecimal subTotal = new BigDecimal(inventoryWeekEntity.getGbDiwProduceTotal()).add(new BigDecimal(dailyEntity.getGbIgdProduceSubtotal()));
//                    inventoryWeekEntity.setGbDiwWasteTotal(wasteTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryWeekEntity.setGbDiwReturnTotal(returnTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryWeekEntity.setGbDiwLossTotal(lossTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryWeekEntity.setGbDiwProduceTotal(subTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    gbDepInventoryWeekService.update(inventoryWeekEntity);
//
//                }
//                if (newGoodsInventoryType.equals(getDISGoodsInventroyMonth())) {
//                    // 1, 添加新weekGoods
//                    GbDepInventoryGoodsMonthEntity monthGoodsEntity = new GbDepInventoryGoodsMonthEntity();
//                    monthGoodsEntity.setGbIgmReturnSubtotal(dailyEntity.getGbIgdReturnSubtotal());
//                    monthGoodsEntity.setGbIgmReturnSubtotal(dailyEntity.getGbIgdReturnSubtotal());
//                    monthGoodsEntity.setGbIgmMonth(formatWhatMonth(0));
//                    monthGoodsEntity.setGbIgmStatus(dailyEntity.getGbIgdStatus());
//                    monthGoodsEntity.setGbIgmLossSubtotal(dailyEntity.getGbIgdLossSubtotal());
//                    monthGoodsEntity.setGbIgmLossSubtotal(dailyEntity.getGbIgdLossSubtotal());
//                    monthGoodsEntity.setGbIgmWasteSubtotal(dailyEntity.getGbIgdWasteSubtotal());
//                    monthGoodsEntity.setGbIgmWasteWeight(dailyEntity.getGbIgdWasteWeight());
//                    monthGoodsEntity.setGbIgmReturnWeight(dailyEntity.getGbIgdReturnWeight());
//                    monthGoodsEntity.setGbIgmReturnSubtotal(dailyEntity.getGbIgdReturnSubtotal());
//                    monthGoodsEntity.setGbIgmProduceWeight(dailyEntity.getGbIgdProduceWeight());
//                    monthGoodsEntity.setGbIgmProduceSubtotal(dailyEntity.getGbIgdProduceSubtotal());
//                    monthGoodsEntity.setGbIgmDepartmentFatherId(dailyEntity.getGbIgdDepartmentFatherId());
//                    monthGoodsEntity.setGbIgmDepartmentId(dailyEntity.getGbIgdDepartmentId());
//                    monthGoodsEntity.setGbIgmDisGoodsFatherId(dailyEntity.getGbIgdDisGoodsFatherId());
//                    monthGoodsEntity.setGbIgmDisGoodsId(dailyEntity.getGbIgdDisGoodsId());
//                    monthGoodsEntity.setGbIgmDistributerId(dailyEntity.getGbIgdDistributerId());
//                    monthGoodsEntity.setGbIgmGbDepStockId(dailyEntity.getGbIgdGbDepStockId());
//                    monthGoodsEntity.setGbIgmFullTime(dailyEntity.getGbIgdFullTime());
//                    gbDepInventoryGoodsMonthService.save(monthGoodsEntity);
//
//
//                    GbDepInventoryMonthEntity inventoryMonthEntity = gbDepInventoryMonthService.queryInventoryMonth(map2);
//                    System.out.println(inventoryMonthEntity.getGbImWasteTotal() + "111111111111");
//                    System.out.println(dailyEntity.getGbIgdWasteSubtotal() + "222222222222");
//
//                    BigDecimal wasteTotal = new BigDecimal(inventoryMonthEntity.getGbImWasteTotal()).add(new BigDecimal(dailyEntity.getGbIgdWasteSubtotal()));
//                    BigDecimal returnTotal = new BigDecimal(inventoryMonthEntity.getGbImReturnTotal()).add(new BigDecimal(dailyEntity.getGbIgdReturnSubtotal()));
//                    BigDecimal lossTotal = new BigDecimal(inventoryMonthEntity.getGbImLossTotal()).add(new BigDecimal(dailyEntity.getGbIgdLossSubtotal()));
//                    BigDecimal subTotal = new BigDecimal(inventoryMonthEntity.getGbImProduceTotal()).add(new BigDecimal(dailyEntity.getGbIgdProduceSubtotal()));
//                    inventoryMonthEntity.setGbImWasteTotal(wasteTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryMonthEntity.setGbImReturnTotal(returnTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryMonthEntity.setGbImLossTotal(lossTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryMonthEntity.setGbImProduceTotal(subTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    gbDepInventoryMonthService.update(inventoryMonthEntity);
//
//
//                }
//
//                GbDepInventoryDailyEntity inventoryDailyEntity = gbDepInventoryDailyService.queryInventoryDaily(map2);
//                BigDecimal subtotal = new BigDecimal(inventoryDailyEntity.getGbIdProduceTotal()).subtract(new BigDecimal(dailyEntity.getGbIgdProduceSubtotal()));
//                BigDecimal loss = new BigDecimal(inventoryDailyEntity.getGbIdLossTotal()).subtract(new BigDecimal(dailyEntity.getGbIgdLossSubtotal()));
//                BigDecimal returnTotal1 = new BigDecimal(inventoryDailyEntity.getGbIdReturnTotal()).subtract(new BigDecimal(dailyEntity.getGbIgdReturnSubtotal()));
//                BigDecimal waste = new BigDecimal(inventoryDailyEntity.getGbIdWasteTotal()).subtract(new BigDecimal(dailyEntity.getGbIgdWasteSubtotal()));
//                inventoryDailyEntity.setGbIdProduceTotal(subtotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                inventoryDailyEntity.setGbIdLossTotal(loss.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                inventoryDailyEntity.setGbIdReturnTotal(returnTotal1.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                inventoryDailyEntity.setGbIdWasteTotal(waste.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                gbDepInventoryDailyService.update(inventoryDailyEntity);
//
//                // 3, delete weekGoods
//                gbDepInventoryGoodsDailyService.delete(dailyEntity.getGbInventoryGoodsDailyId());
//            }
//        }
//
//    }
//
//
//    private void changeInventoryWeekData(GbDistributerGoodsEntity gbGoods) {
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("settleId", -1);
//        map1.put("disGoodsId", gbGoods.getGbDistributerGoodsId());
//        List<GbDepInventoryGoodsWeekEntity> weekEntities = gbDepInventoryGoodsWeekService.queryWeekStockListByParams(map1);
//        if (weekEntities.size() > 0) {
//            for (GbDepInventoryGoodsWeekEntity weekEntity : weekEntities) {
//                //update Week, Daily
//                Map<String, Object> map2 = new HashMap<>();
//                map2.put("depId", weekEntity.getGbIgwDepartmentId());
//                Integer newGoodsInventoryType = gbGoods.getGbDgGoodsInventoryType();
//                if (newGoodsInventoryType.equals(getDISGoodsInventroyDaily())) {
//                    // 1, 添加新weekGoods
//                    GbDepInventoryGoodsDailyEntity dailyGoodsEntity = new GbDepInventoryGoodsDailyEntity();
//                    dailyGoodsEntity.setGbIgdReturnWeight(weekEntity.getGbIgwReturnWeight());
//                    dailyGoodsEntity.setGbIgdReturnSubtotal(weekEntity.getGbIgwReturnSubtotal());
//                    dailyGoodsEntity.setGbIgdStatus(weekEntity.getGbIgwStatus());
//                    dailyGoodsEntity.setGbIgdLossWeight(weekEntity.getGbIgwLossWeight());
//                    dailyGoodsEntity.setGbIgdLossSubtotal(weekEntity.getGbIgwLossSubtotal());
//                    dailyGoodsEntity.setGbIgdWasteSubtotal(weekEntity.getGbIgwWasteSubtotal());
//                    dailyGoodsEntity.setGbIgdWasteWeight(weekEntity.getGbIgwWasteWeight());
//                    dailyGoodsEntity.setGbIgdProduceWeight(weekEntity.getGbIgwProduceWeight());
//                    dailyGoodsEntity.setGbIgdProduceSubtotal(weekEntity.getGbIgwProduceSubtotal());
//                    dailyGoodsEntity.setGbIgdReturnWeight(weekEntity.getGbIgwReturnWeight());
//                    dailyGoodsEntity.setGbIgdReturnSubtotal(weekEntity.getGbIgwReturnSubtotal());
//                    dailyGoodsEntity.setGbIgdDepartmentFatherId(weekEntity.getGbIgwDepartmentFatherId());
//                    dailyGoodsEntity.setGbIgdDepartmentId(weekEntity.getGbIgwDepartmentId());
//                    dailyGoodsEntity.setGbIgdDisGoodsFatherId(weekEntity.getGbIgwDisGoodsFatherId());
//                    dailyGoodsEntity.setGbIgdDisGoodsId(weekEntity.getGbIgwDisGoodsId());
//                    dailyGoodsEntity.setGbIgdDistributerId(weekEntity.getGbIgwDistributerId());
//                    dailyGoodsEntity.setGbIgdGbDepStockId(weekEntity.getGbIgwGbDepStockId());
//                    dailyGoodsEntity.setGbIgdFullTime(weekEntity.getGbIgwFullTime());
//                    dailyGoodsEntity.setGbIgdDepDisGoodsId(weekEntity.getGbIgwDepDisGoodsId());
//                    gbDepInventoryGoodsDailyService.save(dailyGoodsEntity);
//
//
//                    GbDepInventoryDailyEntity inventoryDailyEntity = gbDepInventoryDailyService.queryInventoryDaily(map2);
//                    BigDecimal wasteTotal = new BigDecimal(inventoryDailyEntity.getGbIdWasteTotal()).add(new BigDecimal(weekEntity.getGbIgwWasteSubtotal()));
//                    BigDecimal returnTotal = new BigDecimal(inventoryDailyEntity.getGbIdReturnTotal()).add(new BigDecimal(weekEntity.getGbIgwReturnSubtotal()));
//                    BigDecimal lossTotal = new BigDecimal(inventoryDailyEntity.getGbIdLossTotal()).add(new BigDecimal(weekEntity.getGbIgwLossSubtotal()));
//                    BigDecimal subTotal = new BigDecimal(inventoryDailyEntity.getGbIdProduceTotal()).add(new BigDecimal(weekEntity.getGbIgwSubtotal()));
//                    inventoryDailyEntity.setGbIdWasteTotal(wasteTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryDailyEntity.setGbIdReturnTotal(returnTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryDailyEntity.setGbIdLossTotal(lossTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryDailyEntity.setGbIdProduceTotal(subTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    gbDepInventoryDailyService.update(inventoryDailyEntity);
//
//                }
//                if (newGoodsInventoryType.equals(getDISGoodsInventroyMonth())) {
//                    // 1, 添加新weekGoods
//                    GbDepInventoryGoodsMonthEntity monthGoodsEntity = new GbDepInventoryGoodsMonthEntity();
//                    monthGoodsEntity.setGbIgmReturnWeight(weekEntity.getGbIgwReturnWeight());
//                    monthGoodsEntity.setGbIgmReturnSubtotal(weekEntity.getGbIgwReturnSubtotal());
//                    monthGoodsEntity.setGbIgmMonth(formatWhatMonth(0));
//                    monthGoodsEntity.setGbIgmYear(weekEntity.getGbIgwYear());
//                    monthGoodsEntity.setGbIgmStatus(weekEntity.getGbIgwStatus());
//                    monthGoodsEntity.setGbIgmLossWeight(weekEntity.getGbIgwLossWeight());
//                    monthGoodsEntity.setGbIgmLossSubtotal(weekEntity.getGbIgwLossSubtotal());
//                    monthGoodsEntity.setGbIgmWasteSubtotal(weekEntity.getGbIgwWasteSubtotal());
//                    monthGoodsEntity.setGbIgmWasteWeight(weekEntity.getGbIgwWasteWeight());
//                    monthGoodsEntity.setGbIgmProduceWeight(weekEntity.getGbIgwWeight());
//                    monthGoodsEntity.setGbIgmProduceSubtotal(weekEntity.getGbIgwSubtotal());
//                    monthGoodsEntity.setGbIgmDepartmentFatherId(weekEntity.getGbIgwDepartmentFatherId());
//                    monthGoodsEntity.setGbIgmDepartmentId(weekEntity.getGbIgwDepartmentId());
//                    monthGoodsEntity.setGbIgmDisGoodsFatherId(weekEntity.getGbIgwDisGoodsFatherId());
//                    monthGoodsEntity.setGbIgmDisGoodsId(weekEntity.getGbIgwDisGoodsId());
//                    monthGoodsEntity.setGbIgmDistributerId(weekEntity.getGbIgwDistributerId());
//                    monthGoodsEntity.setGbIgmGbDepStockId(weekEntity.getGbIgwGbDepStockId());
//                    monthGoodsEntity.setGbIgmFullTime(weekEntity.getGbIgwFullTime());
//                    gbDepInventoryGoodsMonthService.save(monthGoodsEntity);
//
//
//                    GbDepInventoryMonthEntity inventoryMonthEntity = gbDepInventoryMonthService.queryInventoryMonth(map2);
//                    BigDecimal wasteTotal = new BigDecimal(inventoryMonthEntity.getGbImWasteTotal()).add(new BigDecimal(weekEntity.getGbIgwSubtotal()));
//                    BigDecimal returnTotal = new BigDecimal(inventoryMonthEntity.getGbImReturnTotal()).add(new BigDecimal(weekEntity.getGbIgwReturnSubtotal()));
//                    BigDecimal lossTotal = new BigDecimal(inventoryMonthEntity.getGbImLossTotal()).add(new BigDecimal(weekEntity.getGbIgwLossSubtotal()));
//                    BigDecimal subTotal = new BigDecimal(inventoryMonthEntity.getGbImProduceTotal()).add(new BigDecimal(weekEntity.getGbIgwSubtotal()));
//                    inventoryMonthEntity.setGbImWasteTotal(wasteTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryMonthEntity.setGbImReturnTotal(returnTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryMonthEntity.setGbImLossTotal(lossTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    inventoryMonthEntity.setGbImProduceTotal(subTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                    gbDepInventoryMonthService.update(inventoryMonthEntity);
//
//
//                }
//
//                GbDepInventoryWeekEntity inventoryWeekEntity = gbDepInventoryWeekService.queryInventoryWeek(map2);
//                BigDecimal subtotal = new BigDecimal(inventoryWeekEntity.getGbDiwProduceTotal()).subtract(new BigDecimal(weekEntity.getGbIgwSubtotal()));
//                BigDecimal loss = new BigDecimal(inventoryWeekEntity.getGbDiwLossTotal()).subtract(new BigDecimal(weekEntity.getGbIgwLossSubtotal()));
//                BigDecimal returnTotal1 = new BigDecimal(inventoryWeekEntity.getGbDiwReturnTotal()).subtract(new BigDecimal(weekEntity.getGbIgwReturnSubtotal()));
//                BigDecimal waste = new BigDecimal(inventoryWeekEntity.getGbDiwWasteTotal()).subtract(new BigDecimal(weekEntity.getGbIgwWasteSubtotal()));
//                inventoryWeekEntity.setGbDiwProduceTotal(subtotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                inventoryWeekEntity.setGbDiwLossTotal(loss.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                inventoryWeekEntity.setGbDiwReturnTotal(returnTotal1.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                inventoryWeekEntity.setGbDiwWasteTotal(waste.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                gbDepInventoryWeekService.update(inventoryWeekEntity);
//
//                // 3, delete weekGoods
//                gbDepInventoryGoodsWeekService.delete(weekEntity.getGbInventoryGoodsWeekId());
//            }
//        }
//
//    }
//
//
//    private void changeInventoryMonthData(GbDistributerGoodsEntity gbGoods) {
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("settleId", -1);
//        map1.put("disGoodsId", gbGoods.getGbDistributerGoodsId());
//        List<GbDepInventoryGoodsMonthEntity> monthEntities = gbDepInventoryGoodsMonthService.queryMonthStockListByParams(map1);
//        if (monthEntities.size() > 0) {
//            for (GbDepInventoryGoodsMonthEntity month : monthEntities) {
//                //update Week, Daily
//                Map<String, Object> map2 = new HashMap<>();
//                map2.put("depId", month.getGbIgmDepartmentId());
//                Integer newGoodsInventoryType = gbGoods.getGbDgGoodsInventoryType();
//                if (newGoodsInventoryType.equals(getDISGoodsInventroyDaily())) {
//                    // 1, 添加新weekGoods
//                    GbDepInventoryGoodsDailyEntity dailyGoodsEntity = new GbDepInventoryGoodsDailyEntity();
//                    dailyGoodsEntity.setGbIgdReturnSubtotal(month.getGbIgmReturnSubtotal());
//                    dailyGoodsEntity.setGbIgdReturnSubtotal(month.getGbIgmReturnSubtotal());
//                    dailyGoodsEntity.setGbIgdStatus(month.getGbIgmStatus());
//                    dailyGoodsEntity.setGbIgdLossSubtotal(month.getGbIgmLossSubtotal());
//                    dailyGoodsEntity.setGbIgdLossSubtotal(month.getGbIgmLossSubtotal());
//                    dailyGoodsEntity.setGbIgdWasteSubtotal(month.getGbIgmWasteSubtotal());
//                    dailyGoodsEntity.setGbIgdWasteWeight(month.getGbIgmWasteWeight());
//                    dailyGoodsEntity.setGbIgdProduceWeight(month.getGbIgmProduceWeight());
//                    dailyGoodsEntity.setGbIgdProduceSubtotal(month.getGbIgmProduceSubtotal());
//                    dailyGoodsEntity.setGbIgdDepartmentFatherId(month.getGbIgmDepartmentFatherId());
//                    dailyGoodsEntity.setGbIgdDepartmentId(month.getGbIgmDepartmentId());
//                    dailyGoodsEntity.setGbIgdDisGoodsFatherId(month.getGbIgmDisGoodsFatherId());
//                    dailyGoodsEntity.setGbIgdDisGoodsId(month.getGbIgmDisGoodsId());
//                    dailyGoodsEntity.setGbIgdDistributerId(month.getGbIgmDistributerId());
//                    dailyGoodsEntity.setGbIgdGbDepStockId(month.getGbIgmGbDepStockId());
//                    dailyGoodsEntity.setGbIgdFullTime(month.getGbIgmFullTime());
//                    gbDepInventoryGoodsDailyService.save(dailyGoodsEntity);
//
//                    //
//                    GbDepInventoryDailyEntity inventoryDailyEntity = gbDepInventoryDailyService.queryInventoryDaily(map2);
//                    if (inventoryDailyEntity == null) {
//                        GbDepInventoryDailyEntity dailyEntity1 = new GbDepInventoryDailyEntity();
//                        dailyEntity1.setGbIdDate(formatWhatDay(0));
//                        dailyEntity1.setGbIdProduceTotal(month.getGbIgmSubtotal());
//                        dailyEntity1.setGbIdDepartmentId(month.getGbIgmDepartmentId());
//                        dailyEntity1.setGbIdDepartmentFatherId(month.getGbIgmDepartmentFatherId());
//                        dailyEntity1.setGbIdDistributerId(month.getGbIgmDistributerId());
//                        dailyEntity1.setGbIdWeek(getWeek(0));
//                        dailyEntity1.setGbIdYear(formatWhatYear(0));
//                        dailyEntity1.setGbIdProduceTotal("0");
//                        dailyEntity1.setGbIdWasteTotal("0");
//                        dailyEntity1.setGbIdLossTotal("0");
//                        dailyEntity1.setGbIdReturnTotal("0");
//                        dailyEntity1.setGbIdStatus(0);
//                        gbDepInventoryDailyService.save(dailyEntity1);
//
//                    } else {
//                        BigDecimal wasteTotal = new BigDecimal(inventoryDailyEntity.getGbIdWasteTotal()).add(new BigDecimal(month.getGbIgmWasteSubtotal()));
//                        BigDecimal returnTotal = new BigDecimal(inventoryDailyEntity.getGbIdReturnTotal()).add(new BigDecimal(month.getGbIgmReturnSubtotal()));
//                        BigDecimal lossTotal = new BigDecimal(inventoryDailyEntity.getGbIdLossTotal()).add(new BigDecimal(month.getGbIgmLossSubtotal()));
//                        BigDecimal subTotal = new BigDecimal(inventoryDailyEntity.getGbIdProduceTotal()).add(new BigDecimal(month.getGbIgmSubtotal()));
//                        inventoryDailyEntity.setGbIdWasteTotal(wasteTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        inventoryDailyEntity.setGbIdReturnTotal(returnTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        inventoryDailyEntity.setGbIdLossTotal(lossTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        inventoryDailyEntity.setGbIdProduceTotal(subTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        gbDepInventoryDailyService.update(inventoryDailyEntity);
//                    }
//                }
//
//                if (newGoodsInventoryType.equals(getDISGoodsInventroyWeek())) {
//                    // 1, 添加新weekGoods
//                    GbDepInventoryGoodsWeekEntity weekGoodsEntity = new GbDepInventoryGoodsWeekEntity();
//                    weekGoodsEntity.setGbIgwReturnSubtotal(month.getGbIgmReturnSubtotal());
//                    weekGoodsEntity.setGbIgwReturnWeight(month.getGbIgmReturnWeight());
//                    weekGoodsEntity.setGbIgwWeek(getWeekOfYear(0).toString());
//                    weekGoodsEntity.setGbIgwYear(month.getGbIgmYear());
//                    weekGoodsEntity.setGbIgwStatus(month.getGbIgmStatus());
//                    weekGoodsEntity.setGbIgwLossWeight(month.getGbIgmLossWeight());
//                    weekGoodsEntity.setGbIgwLossSubtotal(month.getGbIgmLossSubtotal());
//                    weekGoodsEntity.setGbIgwWasteSubtotal(month.getGbIgmWasteSubtotal());
//                    weekGoodsEntity.setGbIgwWasteWeight(month.getGbIgmWasteWeight());
//                    weekGoodsEntity.setGbIgwProduceWeight(month.getGbIgmProduceWeight());
//                    weekGoodsEntity.setGbIgwProduceSubtotal(month.getGbIgmProduceSubtotal());
//                    weekGoodsEntity.setGbIgwDepartmentFatherId(month.getGbIgmDepartmentFatherId());
//                    weekGoodsEntity.setGbIgwDepartmentId(month.getGbIgmDepartmentId());
//                    weekGoodsEntity.setGbIgwDisGoodsFatherId(month.getGbIgmDisGoodsFatherId());
//                    weekGoodsEntity.setGbIgwDisGoodsId(month.getGbIgmDisGoodsId());
//                    weekGoodsEntity.setGbIgwDistributerId(month.getGbIgmDistributerId());
//                    weekGoodsEntity.setGbIgwGbDepStockId(month.getGbIgmGbDepStockId());
//                    weekGoodsEntity.setGbIgwFullTime(month.getGbIgmFullTime());
//                    weekGoodsEntity.setGbIgwDepDisGoodsId(month.getGbIgmDepDisGoodsId());
//                    gbDepInventoryGoodsWeekService.save(weekGoodsEntity);
//
//
//                    GbDepInventoryWeekEntity inventoryWeekEntity = gbDepInventoryWeekService.queryInventoryWeek(map2);
//                    if (inventoryWeekEntity == null) {
//                        GbDepInventoryWeekEntity weekEntity1 = new GbDepInventoryWeekEntity();
//                        weekEntity1.setGbDiwProduceTotal(month.getGbIgmWasteSubtotal());
//                        weekEntity1.setGbDiwDepartmentId(month.getGbIgmDepartmentId());
//                        weekEntity1.setGbDiwDepartmentFatherId(month.getGbIgmDepartmentFatherId());
//                        weekEntity1.setGbDiwDistributerId(month.getGbIgmDistributerId());
//                        weekEntity1.setGbDiwWeek(getWeekOfYear(0).toString());
//                        weekEntity1.setGbDiwYear(formatWhatYear(0));
//                        weekEntity1.setGbDiwWasteTotal("0");
//                        weekEntity1.setGbDiwLossTotal("0");
//                        weekEntity1.setGbDiwReturnTotal("0");
//                        weekEntity1.setGbDiwProduceTotal("0");
//                        weekEntity1.setGbDiwStatus(0);
//                        gbDepInventoryWeekService.save(weekEntity1);
//                    } else {
//                        BigDecimal wasteTotal = new BigDecimal(inventoryWeekEntity.getGbDiwWasteTotal()).add(new BigDecimal(month.getGbIgmWasteSubtotal()));
//                        BigDecimal returnTotal = new BigDecimal(inventoryWeekEntity.getGbDiwReturnTotal()).add(new BigDecimal(month.getGbIgmReturnSubtotal()));
//                        BigDecimal lossTotal = new BigDecimal(inventoryWeekEntity.getGbDiwLossTotal()).add(new BigDecimal(month.getGbIgmLossSubtotal()));
//                        BigDecimal subTotal = new BigDecimal(inventoryWeekEntity.getGbDiwProduceTotal()).add(new BigDecimal(month.getGbIgmSubtotal()));
//                        inventoryWeekEntity.setGbDiwWasteTotal(wasteTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        inventoryWeekEntity.setGbDiwReturnTotal(returnTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        inventoryWeekEntity.setGbDiwLossTotal(lossTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        inventoryWeekEntity.setGbDiwProduceTotal(subTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                        gbDepInventoryWeekService.update(inventoryWeekEntity);
//                    }
//                }
//
//                GbDepInventoryMonthEntity inventoryMonthEntity = gbDepInventoryMonthService.queryInventoryMonth(map2);
//                BigDecimal subtotal = new BigDecimal(inventoryMonthEntity.getGbImProduceTotal()).subtract(new BigDecimal(month.getGbIgmSubtotal()));
//                BigDecimal loss = new BigDecimal(inventoryMonthEntity.getGbImLossTotal()).subtract(new BigDecimal(month.getGbIgmLossSubtotal()));
//                BigDecimal returnTotal1 = new BigDecimal(inventoryMonthEntity.getGbImReturnTotal()).subtract(new BigDecimal(month.getGbIgmReturnSubtotal()));
//                BigDecimal waste = new BigDecimal(inventoryMonthEntity.getGbImWasteTotal()).subtract(new BigDecimal(month.getGbIgmWasteSubtotal()));
//                inventoryMonthEntity.setGbImProduceTotal(subtotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                inventoryMonthEntity.setGbImLossTotal(loss.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                inventoryMonthEntity.setGbImReturnTotal(returnTotal1.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                inventoryMonthEntity.setGbImWasteTotal(waste.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//                gbDepInventoryMonthService.update(inventoryMonthEntity);
//
//                // 3, delete month
//                gbDepInventoryGoodsMonthService.delete(month.getGbInventoryGoodsMonthId());
//            }
//        }
//
//    }


}





