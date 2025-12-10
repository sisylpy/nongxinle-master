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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.PinYin4jUtils.*;
import static com.nongxinle.utils.PinYin4jUtils.getXiegang;


@RestController
@RequestMapping("api/gbdepartmentdisgoods")
public class GbDepartmentDisGoodsController {
    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private GbDepartmentGoodsStockService gbDepGoodsStockService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private GbDistributerFatherGoodsService gbDistributerFatherGoodsService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepDisGoodsService;
    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;

   @Autowired
   private GbDepartmentGoodsDailyService gbDepartmentGoodsDailyService;

    private static final Logger log = LoggerFactory.getLogger(GbDepartmentDisGoodsController.class);



    @RequestMapping(value = "/disGetSubDepAiOrder",method = RequestMethod.POST)
    @ResponseBody
    public R disGetSubDepAiOrder(Integer depId,
                                 Integer page,
                                 Integer limit) {
        System.out.println("abcncnc" + depId);
       PageUtils list = gbDepartmentDisGoodsService.computeReorder(depId, page, limit);
        System.out.println("paoosisiiss"  + list);
        return R.ok().put("page", list);
    }

//
//    @RequestMapping(value = "/getDepGoodsOrderHistory", method = RequestMethod.POST)
//    @ResponseBody
//    public R getDepGoodsOrderHistory (Integer depGoodsId, String startDate, String stopDate) {
//
//        GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depGoodsId);
//
//        List<Map<String, Object>> dayList = new ArrayList<>();
//        Integer gbDdgDisGoodsId = departmentDisGoodsEntity.getGbDdgDisGoodsId();
//        Map<String, Object> map = new HashMap<>();
//        map.put("disGoodsId", gbDdgDisGoodsId);
//
//        Integer howManyDaysInPeriod = 0;
//        if (!startDate.equals(stopDate)) {
//            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
//        }
//        if (howManyDaysInPeriod > 0) {
//
//            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
//                // dateList
//                String whichDay = "";
//                if (i == 0) {
//                    whichDay = startDate;
//                } else {
//                    whichDay = afterWhatDay(startDate, i);
//                }
//                map.put("arriveDate", whichDay);
//                map.put("dayuStatus", 1);
//                map.put("depId", departmentDisGoodsEntity.getGbDdgDepartmentId());
//                map.put("orderTypeNotEqual", 9);
//                System.out.println("whdaydmamm" + map);
//                List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
//                Map<String, Object> mapDay = new HashMap<>();
//                String substring = whichDay.substring(8, 10);
//                mapDay.put("day",  substring);
//                mapDay.put("arr", gbDepartmentOrdersEntities);
//                dayList.add(mapDay);
//            }
//        }
//
//        return R.ok().put("data", dayList);
//    }


    @RequestMapping(value = "/departmentSaveLinshiDisGoods", method = RequestMethod.POST)
    @ResponseBody
    public R departmentSaveLinshiDisGoods(@RequestBody GbDepartmentDisGoodsEntity disGoodsEntity) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disGoodsEntity.getGbDdgGbDisId());
        map.put("nxGoodsId", -1);
        map.put("goodsLevel", 2);
        GbDistributerFatherGoodsEntity fatherGoodsEntity = gbDistributerFatherGoodsService.queryDisGoodsCataLinshiFatherGoods(map);

        GbDistributerGoodsEntity cgnGoods  = new GbDistributerGoodsEntity();
        cgnGoods.setGbDgDfgGoodsFatherId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
        cgnGoods.setGbDgDfgGoodsGrandId(fatherGoodsEntity.getGbDfgFathersFatherId());
        GbDistributerFatherGoodsEntity grandFather = gbDistributerFatherGoodsService.queryObject(fatherGoodsEntity.getGbDfgFathersFatherId());
        cgnGoods.setGbDgDfgGoodsGreatId(grandFather.getGbDfgFathersFatherId());

        cgnGoods.setGbDgGoodsStatus(0);
        cgnGoods.setGbDgGoodsIsWeight(0);
        cgnGoods.setGbDgPullOff(0);
        cgnGoods.setGbDgGoodsType(getGbDisGoodsTypeZicai());
        cgnGoods.setGbDgNxDistributerId(-1);
        cgnGoods.setGbDgNxDistributerGoodsId(-1);
        cgnGoods.setGbDgNxDistributerGoodsPrice("0.1");


        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disGoodsEntity.getGbDdgGbDisId());
        mapDep.put("goodsType", 5);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(mapDep);
        GbDepartmentEntity appDepartmentEntity = departmentEntities.get(0);
        cgnGoods.setGbDgGoodsName(disGoodsEntity.getGbDdgDepGoodsName());
        cgnGoods.setGbDgGoodsStandardname(disGoodsEntity.getGbDdgDepGoodsStandardname());
        cgnGoods.setGbDgGbDepartmentId(appDepartmentEntity.getGbDepartmentId());
        cgnGoods.setGbDgControlFresh(0);
        cgnGoods.setGbDgControlPrice(0);
        cgnGoods.setGbDgGoodsInventoryType(1);
        cgnGoods.setGbDgIsFranchisePrice(0);
        cgnGoods.setGbDgIsSelfControl(0);

        String pinyin = hanziToPinyin(disGoodsEntity.getGbDdgDepGoodsName());
        String headPinyin = getHeadStringByString(disGoodsEntity.getGbDdgDepGoodsName(), false, null);
        cgnGoods.setGbDgGoodsPinyin(pinyin);
        cgnGoods.setGbDgGoodsPy(headPinyin);
        cgnGoods.setGbDgGoodsStandardname(disGoodsEntity.getGbDdgDepGoodsStandardname());
        cgnGoods.setGbDgGoodsDetail(disGoodsEntity.getGbDdgDepGoodsDetail());
        gbDistributerGoodsService.save(cgnGoods);

        Integer gbDfgGoodsAmount = fatherGoodsEntity.getGbDfgGoodsAmount();
        fatherGoodsEntity.setGbDfgGoodsAmount(gbDfgGoodsAmount + 1);
        gbDistributerFatherGoodsService.update(fatherGoodsEntity);


        //添加部门商品
        disGoodsEntity.setGbDdgDisGoodsId(cgnGoods.getGbDistributerGoodsId());
        disGoodsEntity.setGbDdgDisGoodsFatherId(cgnGoods.getGbDgDfgGoodsFatherId());
        disGoodsEntity.setGbDdgDisGoodsGrandId(cgnGoods.getGbDgDfgGoodsGrandId());
        disGoodsEntity.setGbDdgDisGoodsGreatId(cgnGoods.getGbDgDfgGoodsGreatId());
        disGoodsEntity.setGbDdgDepGoodsPinyin(cgnGoods.getGbDgGoodsPinyin());
        disGoodsEntity.setGbDdgDepGoodsPy(cgnGoods.getGbDgGoodsPy());
        disGoodsEntity.setGbDdgDepGoodsStandardname(cgnGoods.getGbDgGoodsStandardname());
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(disGoodsEntity.getGbDdgDepartmentId());
        disGoodsEntity.setGbDdgDepartmentFatherId(disGoodsEntity.getGbDdgDepartmentId());
        if(departmentEntity.getGbDepartmentFatherId() != 0){
            disGoodsEntity.setGbDdgDepartmentFatherId(departmentEntity.getGbDepartmentFatherId());
        }
        disGoodsEntity.setGbDdgGbDepartmentId(cgnGoods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgGbDisId(cgnGoods.getGbDgDistributerId());
        disGoodsEntity.setGbDdgGoodsType(cgnGoods.getGbDgGoodsType());
        disGoodsEntity.setGbDdgStockTotalWeight("0.0");
        disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
        disGoodsEntity.setGbDdgShowStandardId(-1);
        disGoodsEntity.setGbDdgShowStandardName(cgnGoods.getGbDgGoodsStandardname());
        disGoodsEntity.setGbDdgShowStandardScale("-1");
        disGoodsEntity.setGbDdgShowStandardWeight(null);
        disGoodsEntity.setGbDdgNxDistributerGoodsId(-1);
        disGoodsEntity.setGbDdgOrderStandard(disGoodsEntity.getGbDdgDepGoodsStandardname());

        gbDepDisGoodsService.save(disGoodsEntity);

        //todo
//        cgnGoods.setGbDepartmentDisGoodsEntity(disGoodsEntity);
        return R.ok().put("data", cgnGoods);
    }


    @RequestMapping(value = "/jjdhSaveGbLinshiGoodsWithFile", produces = "text/html;charset=UTF-8")
    public  GbDistributerGoodsEntity jjdhSaveGbLinshiGoodsWithFile(@RequestParam("file") MultipartFile file,
                                               @RequestParam("goodsName") String goodsName,
                                               @RequestParam("standard") String standard,
                                               @RequestParam("detail") String detail,
                                               @RequestParam("disId") Integer disId,
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

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("nxGoodsId", -1);
        map.put("goodsLevel", 2);
        GbDistributerFatherGoodsEntity fatherGoodsEntity = gbDistributerFatherGoodsService.queryDisGoodsCataLinshiFatherGoods(map);

        GbDistributerGoodsEntity cgnGoods  = new GbDistributerGoodsEntity();
        cgnGoods.setGbDgDfgGoodsFatherId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
        cgnGoods.setGbDgDfgGoodsGrandId(fatherGoodsEntity.getGbDfgFathersFatherId());
        GbDistributerFatherGoodsEntity grandFather = gbDistributerFatherGoodsService.queryObject(fatherGoodsEntity.getGbDfgFathersFatherId());
        cgnGoods.setGbDgDfgGoodsGreatId(grandFather.getGbDfgFathersFatherId());

        cgnGoods.setGbDgGoodsStatus(0);
        cgnGoods.setGbDgGoodsIsWeight(0);
        cgnGoods.setGbDgPullOff(0);
        cgnGoods.setGbDgGoodsType(getGbDisGoodsTypeZicai());
        cgnGoods.setGbDgNxDistributerId(-1);
        cgnGoods.setGbDgNxDistributerGoodsId(-1);
        cgnGoods.setGbDgNxDistributerGoodsPrice("0.1");
        cgnGoods.setGbDgNxFatherImg(realPath);
        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disId);
        mapDep.put("goodsType", 5);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(mapDep);
        GbDepartmentEntity appDepartmentEntity = departmentEntities.get(0);
        cgnGoods.setGbDgGbDepartmentId(appDepartmentEntity.getGbDepartmentId());
        cgnGoods.setGbDgControlFresh(0);
        cgnGoods.setGbDgControlPrice(0);
        cgnGoods.setGbDgGoodsInventoryType(1);
        cgnGoods.setGbDgIsFranchisePrice(0);
        cgnGoods.setGbDgIsSelfControl(0);
        String pinyin = hanziToPinyin(goodsName);
        String headPinyin = getHeadStringByString(goodsName, false, null);
        cgnGoods.setGbDgGoodsPinyin(pinyin);
        cgnGoods.setGbDgGoodsPy(headPinyin);
        cgnGoods.setGbDgGoodsName(goodsName);
        cgnGoods.setGbDgDistributerId(disId);
        cgnGoods.setGbDgGoodsStandardname(standard);
        cgnGoods.setGbDgGoodsDetail(detail);
        gbDistributerGoodsService.save(cgnGoods);

        Integer gbDfgGoodsAmount = fatherGoodsEntity.getGbDfgGoodsAmount();
        fatherGoodsEntity.setGbDfgGoodsAmount(gbDfgGoodsAmount + 1);
        gbDistributerFatherGoodsService.update(fatherGoodsEntity);


        //添加部门商品
        GbDepartmentDisGoodsEntity disGoodsEntity = new GbDepartmentDisGoodsEntity();
        disGoodsEntity.setGbDdgDepGoodsName(cgnGoods.getGbDgGoodsName());
        disGoodsEntity.setGbDdgDisGoodsId(cgnGoods.getGbDistributerGoodsId());
        disGoodsEntity.setGbDdgDisGoodsFatherId(cgnGoods.getGbDgDfgGoodsFatherId());
        disGoodsEntity.setGbDdgDisGoodsGrandId(cgnGoods.getGbDgDfgGoodsGrandId());
        disGoodsEntity.setGbDdgDisGoodsGreatId(cgnGoods.getGbDgDfgGoodsGreatId());
        disGoodsEntity.setGbDdgDepGoodsPinyin(cgnGoods.getGbDgGoodsPinyin());
        disGoodsEntity.setGbDdgDepGoodsPy(cgnGoods.getGbDgGoodsPy());
        disGoodsEntity.setGbDdgDepGoodsStandardname(cgnGoods.getGbDgGoodsStandardname());
        disGoodsEntity.setGbDdgDepartmentId(depId);
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(depId);
        disGoodsEntity.setGbDdgDepartmentFatherId(depId);
        if(departmentEntity.getGbDepartmentFatherId() != 0){
            disGoodsEntity.setGbDdgDepartmentFatherId(departmentEntity.getGbDepartmentFatherId());
        }
        disGoodsEntity.setGbDdgGbDepartmentId(cgnGoods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgGbDisId(cgnGoods.getGbDgDistributerId());
        disGoodsEntity.setGbDdgGoodsType(cgnGoods.getGbDgGoodsType());
        disGoodsEntity.setGbDdgStockTotalWeight("0.0");
        disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
        disGoodsEntity.setGbDdgShowStandardId(-1);
        disGoodsEntity.setGbDdgShowStandardName(cgnGoods.getGbDgGoodsStandardname());
        disGoodsEntity.setGbDdgShowStandardScale("-1");
        disGoodsEntity.setGbDdgShowStandardWeight(null);
        disGoodsEntity.setGbDdgNxDistributerGoodsId(-1);
        gbDepDisGoodsService.save(disGoodsEntity);

        return cgnGoods;

    }


    @RequestMapping(value = "/depSaveGbLinshiGoods", method = RequestMethod.POST)
    @ResponseBody
    public R depSaveGbLinshiGoods(@RequestBody GbDepartmentOrdersEntity ordersEntity) {

        GbDistributerGoodsEntity cgnGoods = ordersEntity.getGbDistributerGoodsEntity();

        System.out.println("ordereorororo" + ordersEntity.getGbDoDepartmentId());
        Map<String, Object> map = new HashMap<>();
        map.put("disId", ordersEntity.getGbDoDistributerId()); //固定 nxDisId==1
        map.put("goodsLevel", 2);
        System.out.println("abcccc" + map);
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDistributerFatherGoodsService.queryDisFathersGoodsByParamsGb(map);
        GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
        cgnGoods.setGbDgDfgGoodsFatherId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
        cgnGoods.setGbDgDfgGoodsGrandId(fatherGoodsEntity.getGbDfgFathersFatherId());
        GbDistributerFatherGoodsEntity grandFather = gbDistributerFatherGoodsService.queryObject(fatherGoodsEntity.getGbDfgFathersFatherId());
        cgnGoods.setGbDgDfgGoodsGreatId(grandFather.getGbDfgFathersFatherId());

        cgnGoods.setGbDgGoodsStatus(0);
        cgnGoods.setGbDgGoodsIsWeight(0);
        cgnGoods.setGbDgPullOff(0);
        cgnGoods.setGbDgGoodsType(getGbDisGoodsTypeZicai());
        cgnGoods.setGbDgNxDistributerId(-1);
        cgnGoods.setGbDgNxDistributerGoodsId(-1);
        cgnGoods.setGbDgNxDistributerGoodsPrice("0.1");
        cgnGoods.setGbDgGbDepartmentId(ordersEntity.getGbDoDepartmentId());
        cgnGoods.setGbDgControlFresh(0);
        cgnGoods.setGbDgControlPrice(0);
        cgnGoods.setGbDgGoodsInventoryType(1);
        cgnGoods.setGbDgIsFranchisePrice(0);
        cgnGoods.setGbDgIsSelfControl(0);
        String goodsName = cgnGoods.getGbDgGoodsName();
        String pinyin = hanziToPinyin(goodsName);
        String headPinyin = getHeadStringByString(goodsName, false, null);
        cgnGoods.setGbDgGoodsPinyin(pinyin);
        cgnGoods.setGbDgGoodsPy(headPinyin);
        gbDistributerGoodsService.save(cgnGoods);

        saveDepDisGoods(cgnGoods);


        Integer gbDfgGoodsAmount = fatherGoodsEntity.getGbDfgGoodsAmount();
        fatherGoodsEntity.setGbDfgGoodsAmount(gbDfgGoodsAmount + 1);
        gbDistributerFatherGoodsService.update(fatherGoodsEntity);

        return R.ok().put("data", cgnGoods);

    }

    private void saveDepDisGoods(GbDistributerGoodsEntity cgnGoods) {
//
        //添加部门商品
        GbDepartmentDisGoodsEntity disGoodsEntity = new GbDepartmentDisGoodsEntity();
        disGoodsEntity.setGbDdgDepGoodsName(cgnGoods.getGbDgGoodsName());
        disGoodsEntity.setGbDdgDisGoodsId(cgnGoods.getGbDistributerGoodsId());
        disGoodsEntity.setGbDdgDisGoodsFatherId(cgnGoods.getGbDgDfgGoodsFatherId());
        disGoodsEntity.setGbDdgDisGoodsGrandId(cgnGoods.getGbDgDfgGoodsGrandId());
        disGoodsEntity.setGbDdgDisGoodsGreatId(cgnGoods.getGbDgDfgGoodsGreatId());
        disGoodsEntity.setGbDdgDepGoodsPinyin(cgnGoods.getGbDgGoodsPinyin());
        disGoodsEntity.setGbDdgDepGoodsPy(cgnGoods.getGbDgGoodsPy());
        disGoodsEntity.setGbDdgDepGoodsStandardname(cgnGoods.getGbDgGoodsStandardname());
        disGoodsEntity.setGbDdgDepartmentId(cgnGoods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgDepartmentFatherId(cgnGoods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgGbDepartmentId(cgnGoods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgGbDisId(cgnGoods.getGbDgDistributerId());
        disGoodsEntity.setGbDdgGoodsType(cgnGoods.getGbDgGoodsType());
        disGoodsEntity.setGbDdgStockTotalWeight("0.0");
        disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
        disGoodsEntity.setGbDdgShowStandardId(-1);
        disGoodsEntity.setGbDdgShowStandardName(cgnGoods.getGbDgGoodsStandardname());
        disGoodsEntity.setGbDdgShowStandardScale("-1");
        disGoodsEntity.setGbDdgShowStandardWeight(null);
        disGoodsEntity.setGbDdgNxDistributerGoodsId(-1);
        gbDepDisGoodsService.save(disGoodsEntity);

    }


    @RequestMapping(value = "/queryDepGoodsQuickSearchWithDepOrder", method = RequestMethod.POST)
    @ResponseBody
    public R queryDepGoodsQuickSearchWithDepOrder(String searchStr, Integer gbDisId, Integer depId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", gbDisId);
        map.put("depId", depId);
        map.put("searchStr", searchStr);
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
        map.put("status", 4);
        map.put("date", formatWhatDay(0));
        map.put("pull", 0);
        map.put("notLinshi", 1);
        System.out.println("depserardkddkdkdk" + map);
        TreeSet<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepartmentDisGoodsService.queryDepDisGoodsQuickSearchStrGb(map);

        return R.ok().put("data", departmentDisGoodsEntities);
    }


    @RequestMapping(value = "/getDepartmentFreshGoods/{depId}")
    @ResponseBody
    public R getDepartmentFreshGoods(@PathVariable Integer depId) {
        String s = formatWhatFullTime(0);
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("fresh", 1);
        map.put("dayuStatus", -1);
        map.put("restWeight", 1);
        List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(map);

        return R.ok().put("data", departmentDisGoodsEntities);

    }

    @RequestMapping(value = "/getDepGoodsStockList", method = RequestMethod.POST)
    @ResponseBody
    public R getDepGoodsStockList(Integer disGoodsId, Integer depId,
                                  String startDate, String stopDate) {

        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", disGoodsId);
        map.put("stockDepId", depId);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);

        List<GbDepartmentGoodsStockEntity> stockEntities = gbDepGoodsStockService.queryGoodsStockByParams(map);


        return R.ok().put("data", stockEntities);
    }


    @RequestMapping(value = "/addDepartmentDisGoods", method = RequestMethod.POST)
    @ResponseBody
    public R addDepartmentDisGoods(Integer disGoodsId, Integer depId, String sellingPrice) {
        GbDistributerGoodsEntity disGoods = gbDistributerGoodsService.queryObject(disGoodsId);

        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.querySubDepartments(depId);
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity subDeps : departmentEntities) {
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
                disGoodsEntity.setGbDdgDepartmentId(subDeps.getGbDepartmentId());
                disGoodsEntity.setGbDdgDepartmentFatherId(depId);
                disGoodsEntity.setGbDdgGbDepartmentId(disGoods.getGbDgGbDepartmentId());
                disGoodsEntity.setGbDdgGbDisId(disGoods.getGbDgDistributerId());
                disGoodsEntity.setGbDdgGoodsType(disGoods.getGbDgGoodsType());
                disGoodsEntity.setGbDdgStockTotalWeight("0.0");
                disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
                disGoodsEntity.setGbDdgShowStandardId(-1);
                disGoodsEntity.setGbDdgShowStandardName(disGoods.getGbDgGoodsStandardname());
                disGoodsEntity.setGbDdgShowStandardScale("-1");
                disGoodsEntity.setGbDdgShowStandardWeight("0");
                disGoodsEntity.setGbDdgSellingPrice(sellingPrice);
                gbDepartmentDisGoodsService.save(disGoodsEntity);
            }
        } else {
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
            disGoodsEntity.setGbDdgDepartmentId(depId);
            disGoodsEntity.setGbDdgDepartmentFatherId(depId);
            disGoodsEntity.setGbDdgGbDepartmentId(disGoods.getGbDgGbDepartmentId());
            disGoodsEntity.setGbDdgGbDisId(disGoods.getGbDgDistributerId());
            disGoodsEntity.setGbDdgGoodsType(disGoods.getGbDgGoodsType());
            disGoodsEntity.setGbDdgStockTotalWeight("0.0");
            disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
            disGoodsEntity.setGbDdgShowStandardId(-1);
            disGoodsEntity.setGbDdgShowStandardName(disGoods.getGbDgGoodsStandardname());
            disGoodsEntity.setGbDdgShowStandardScale("-1");
            disGoodsEntity.setGbDdgShowStandardWeight("0");
            disGoodsEntity.setGbDdgSellingPrice(sellingPrice);
            gbDepartmentDisGoodsService.save(disGoodsEntity);
        }

        return R.ok();
    }


    @RequestMapping(value = "/getDepGoodsDepartment", method = RequestMethod.POST)
    @ResponseBody
    public R getDepGoodsDepartment(Integer disGoodsId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("depType", getGbDepartmentTypeMendian());
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryGroupDepsByDisId(map);

        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity department : departmentEntities) {
                Integer gbDepartmentId = department.getGbDepartmentId();
                Map<String, Object> map1 = new HashMap<>();
                map1.put("depFatherId", gbDepartmentId);
                map1.put("disGoodsId", disGoodsId);
                System.out.println("map1111" + map1);
                List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(map1);
                if (departmentDisGoodsEntities.size() > 0) {
                    department.setIsSelected(true);
                    department.setGbDepartmentDisGoodsEntity(departmentDisGoodsEntities.get(0));

                }
            }
        }

        return R.ok().put("data", departmentEntities);
    }


    @RequestMapping(value = "/depPurchserGetDepGoodsGb", method = RequestMethod.POST)
    @ResponseBody
    public R depPurchserGetDepGoodsGb(Integer fatherId, Integer depFatherId) {
        Map<String, Object> mapTotal = new HashMap<>();

        Map<String, Object> mapD = new HashMap<>();
        mapD.put("depFatherId", depFatherId);
        mapD.put("fatherId", fatherId);
        mapD.put("goodsType", getGbDisGoodsTypeZicai());
        System.out.println("purrjrreee" + mapD);
        List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepartmentDisGoodsService.depGetDepsGoodsGb(mapD);

        double aDouble = 0.0;
        if (departmentDisGoodsEntities.size() > 0) {
            Map<String, Object> map = new HashMap<>();
//			map.put("depFatherId", depFatherId);
            aDouble = gbDepGoodsStockService.queryDepGoodsRestTotal(map);
        }
//		}
        mapTotal.put("stockTotal", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        mapTotal.put("arr", departmentDisGoodsEntities);
        return R.ok().put("data", mapTotal);
    }

    @RequestMapping(value = "/mendianPurchserGetDepGoodsCataGb/{depFatherId}")
    @ResponseBody
    public R mendianPurchserGetDepGoodsCataGb(@PathVariable Integer depFatherId) {
        Map<String, Object> mapTotal = new HashMap<>();

        Map<String, Object> mapD = new HashMap<>();
        mapD.put("depFatherId", depFatherId);
        mapD.put("goodsType", getGbDisGoodsTypeZicai());
        System.out.println("purrjrreee" + mapD);
        List<GbDistributerFatherGoodsEntity> disGoodsEntities = gbDepartmentDisGoodsService.queryDepTypeFatherGoods(mapD);
        mapTotal.put("arr", disGoodsEntities);
        return R.ok().put("data", mapTotal);
    }


    @RequestMapping(value = "/depStockGetDepGoodsCataGb/{depFatherId}")
    @ResponseBody
    public R depStockGetDepGoodsCataGb(@PathVariable Integer depFatherId) {
        Map<String, Object> mapTotal = new HashMap<>();

        Map<String, Object> mapD = new HashMap<>();
        mapD.put("depFatherId", depFatherId);
        mapD.put("goodsType", getGbDisGoodsTypeChuku());
        System.out.println("purrjrreee" + mapD);
        List<GbDistributerFatherGoodsEntity> disGoodsEntities = gbDepartmentDisGoodsService.queryDepTypeFatherGoods(mapD);

        double aDouble = 0.0;
        if (disGoodsEntities.size() > 0) {
            Map<String, Object> map = new HashMap<>();
            map.put("depFatherId", depFatherId);
            aDouble = gbDepGoodsStockService.queryDepGoodsRestTotal(map);
        }
//		}
        mapTotal.put("stockTotal", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        mapTotal.put("arr", disGoodsEntities);
        return R.ok().put("data", mapTotal);
    }


    @RequestMapping(value = "/depJicaiGetDepGoodsCataGb/{depFatherId}")
    @ResponseBody
    public R depJicaiGetDepGoodsCataGb(@PathVariable Integer depFatherId) {
        Map<String, Object> mapTotal = new HashMap<>();

        Map<String, Object> mapD = new HashMap<>();
        mapD.put("depFatherId", depFatherId);
        mapD.put("goodsType", getGbDisGoodsTypeJicai());
        System.out.println("purrjrreee" + mapD);
        List<GbDistributerFatherGoodsEntity> disGoodsEntities = gbDepartmentDisGoodsService.queryDepTypeFatherGoods(mapD);

        double aDouble = 0.0;
        if (disGoodsEntities.size() > 0) {
            Map<String, Object> map = new HashMap<>();
            map.put("depFatherId", depFatherId);
            aDouble = gbDepGoodsStockService.queryDepGoodsRestTotal(map);
        }
//		}
        mapTotal.put("stockTotal", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        mapTotal.put("arr", disGoodsEntities);
        return R.ok().put("data", mapTotal);
    }


    @RequestMapping(value = "/disGetDepGoodsCataGb")
    @ResponseBody
    public R disGetDepGoodsCataGb(Integer disId, Integer goodsType) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        if(goodsType < 99){
            map.put("goodsType", goodsType);
        }else{
            if(goodsType == 101){
                map.put("fresh", 1);

            }else if(goodsType == 102){
                map.put("pull", 1);
            }
        }
        System.out.println("cattaktktktkktk");
        List<GbDistributerFatherGoodsEntity> disGoodsEntities = gbDepartmentDisGoodsService.disGetDepDisGoodsCataGb(map);

        System.out.println("iddmdpdpddpdpd" + map);
        List<Integer > disGoodsIds =   gbDepartmentDisGoodsService.queryOnlyDisGoodsIds(map);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("cataArr",disGoodsEntities);
        mapR.put("disGoodsArr", disGoodsIds);

        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/depGetDepGoodsCataGb")
    @ResponseBody
    public R depGetDepDisGoodsCata(Integer depId, Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("disId", disId);
        map.put("pull", 0);
        System.out.println("cattaktktktkktk");
        List<GbDistributerFatherGoodsEntity> disGoodsEntities = gbDepartmentDisGoodsService.disGetDepDisGoodsCataGb(map);
        // 1. 获取总数
        List<Integer > departmentDisGoodsEntities =   gbDepartmentDisGoodsService.queryOnlyDepGoodsIds(map);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("cataArr",disGoodsEntities);
        mapR.put("depGoodsArr", departmentDisGoodsEntities);

        return R.ok().put("data", mapR);
    }



    @RequestMapping(value = "/selfMendianGetDepGoodsCataGb", method = RequestMethod.POST)
    @ResponseBody
    public R selfMendianGetDepGoodsCataGb(Integer depFatherId, String controlString, String goodsType) {

        System.out.println("depfaieieieiei" + depFatherId);

        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depFatherId);
        if (goodsType.equals("appSupplier")) {
            map.put("goodsType", getGbDisGoodsTypeAppSupplier());
        }
        if (controlString.equals("price")) {
            map.put("price", "1");
        }
        if (controlString.equals("fresh")) {
            map.put("fresh", "1");
        }

        List<GbDistributerFatherGoodsEntity> cataList = gbDepartmentDisGoodsService.selfMendiainGetDepDisGoodsCataWithGoods(map);


        return R.ok().put("data", cataList);
    }

    @RequestMapping(value = "/selfMendianGetDepGoodsByGreatGrandId", method = RequestMethod.POST)
    @ResponseBody
    public R selfMendianGetDepGoodsByGreatGrandId(Integer depFatherId, String controlString, String goodsType, Integer fatherId) {

        System.out.println("depfaieieieiei" + depFatherId);

        Map<String, Object> map = new HashMap<>();
        map.put("controlString", controlString);
        map.put("depFatherId", depFatherId);
        if (goodsType.equals("appSupplier")) {
            map.put("goodsType", getGbDisGoodsTypeAppSupplier());
        }
        if (controlString.equals("price")) {
            map.put("price", "1");
        }
        if (controlString.equals("fresh")) {
            map.put("fresh", "1");
        }
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = new ArrayList<>();

        List<GbDistributerFatherGoodsEntity> cataList = gbDepartmentDisGoodsService.selfMendiainGetDepDisGoodsCata(map);

        if (cataList.size() > 0) {
            if (fatherId == -1) {
                map.put("greatGrandId", cataList.get(0).getGbDistributerFatherGoodsId());
            } else {
                map.put("greatGrandId", fatherId);
            }

            map.put("status", 4);
            map.put("date", formatWhatDay(0));
            map.put("pull", 0);
            map.put("notLinshi", 1);
            System.out.println("mappapapappapa" + map);
            fatherGoodsEntities = gbDepartmentDisGoodsService.depQueryDepGoodsWithOrderGb(map);

        }
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("cataList", cataList);
        mapR.put("arr", fatherGoodsEntities);
        return R.ok().put("data", mapR);
    }

    private TreeSet<GbDistributerFatherGoodsEntity> getStockGoodsFatherRestSubTotal(Map<String, Object> map0) {
        TreeSet<GbDistributerFatherGoodsEntity> stockAndRecordFatherGoodsTreeSet = getStockFatherGoodsTreeSet(map0);
        TreeSet<GbDistributerFatherGoodsEntity> stockFatherGoodsRestSubtotal = getStockFatherGoodsRestSubtotal(stockAndRecordFatherGoodsTreeSet, map0);
        return stockFatherGoodsRestSubtotal;
    }

    private TreeSet<GbDistributerFatherGoodsEntity> getStockFatherGoodsTreeSet(Map<String, Object> map0) {
        TreeSet<GbDistributerFatherGoodsEntity> stockGoodsEntities = new TreeSet<>();
        Integer integerStock = gbDepGoodsStockService.queryGoodsStockCount(map0);
        if (integerStock > 0) {
            List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map0);
            stockGoodsEntities.addAll(fatherGoodsEntities);
        }
        return stockGoodsEntities;
    }


    private TreeSet<GbDistributerFatherGoodsEntity> getStockFatherGoodsRestSubtotal
            (TreeSet<GbDistributerFatherGoodsEntity> treeSet, Map<String, Object> map0) {
        TreeSet<GbDistributerFatherGoodsEntity> grandTree = new TreeSet<>();

        for (GbDistributerFatherGoodsEntity greatGrandFather : treeSet) {
            List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
            for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                BigDecimal grandDouble = new BigDecimal(0);
                List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                    map0.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
                    double add = 0.0;
                    System.out.println("---------------------------" + map0);
                    Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map0);
                    if (integer > 0) {
                        Double stockTotal = gbDepGoodsStockService.queryDepGoodsRestTotal(map0);
                        System.out.println("kakfdsalkfjdsalfdslafjd;slak;flk" + stockTotal);
                        add = add + stockTotal;

                    }
                    father.setFatherStockTotal(add);
                    father.setFatherStockTotalString(new BigDecimal(add).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    grandDouble = grandDouble.add(new BigDecimal(add));
                }

                grandFather.setFatherStockTotal(grandDouble.doubleValue());
                grandFather.setFatherStockTotalString(grandDouble.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                grandTree.add(grandFather);
            }

        }
        grandTree = abcFatherGoodsStockEvery(grandTree);

        return grandTree;
    }


    private TreeSet<GbDistributerFatherGoodsEntity> abcFatherGoodsStockEvery(TreeSet<GbDistributerFatherGoodsEntity> goodsEntities) {
        TreeSet<GbDistributerFatherGoodsEntity> ts = new TreeSet<>(new Comparator<GbDistributerFatherGoodsEntity>() {
            @Override
            public int compare(GbDistributerFatherGoodsEntity o1, GbDistributerFatherGoodsEntity o2) {
                int result;

                if (o2.getFatherStockTotal() - o1.getFatherStockTotal() < 0) {
                    result = -1;
                } else if (o2.getFatherStockTotal() - o1.getFatherStockTotal() > 0) {
                    result = 1;
                } else {
                    result = 1;
                }

                return result;
            }
        });

        ts.addAll(goodsEntities);

        return ts;

    }


    @RequestMapping(value = "/getDepDisGoodsGb/{id}")
    @ResponseBody
    public R getDepDisGoodsGb(@PathVariable Integer id) {
        GbDepartmentDisGoodsEntity gbDepartmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(id);
        return R.ok().put("data", gbDepartmentDisGoodsEntity);
    }

    @RequestMapping(value = "/changeShowStandard", method = RequestMethod.POST)
    @ResponseBody
    public R changeShowStandard(Integer depDisGoodsId, Integer showStandardId, String showStandardName, String showStandardScale) {

        GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depDisGoodsId);
        if (showStandardId == -1) {
            departmentDisGoodsEntity.setGbDdgShowStandardWeight(null);
            departmentDisGoodsEntity.setGbDdgShowStandardScale(null);

        } else {
            BigDecimal standScale = new BigDecimal(showStandardScale);
            BigDecimal weight = new BigDecimal(departmentDisGoodsEntity.getGbDdgStockTotalWeight());
            BigDecimal showWeight = weight.divide(standScale, 2, BigDecimal.ROUND_HALF_UP);
            departmentDisGoodsEntity.setGbDdgShowStandardWeight(showWeight.toString());
        }
        departmentDisGoodsEntity.setGbDdgShowStandardScale(showStandardScale);
        departmentDisGoodsEntity.setGbDdgShowStandardName(showStandardName);
        departmentDisGoodsEntity.setGbDdgShowStandardId(showStandardId);
        gbDepartmentDisGoodsService.update(departmentDisGoodsEntity);

        //changeStock
        Map<String, Object> map = new HashMap<>();
        map.put("depGoodsId", depDisGoodsId);
        map.put("restWeight", 0);
        List<GbDepartmentGoodsStockEntity> stockEntities = gbDepGoodsStockService.queryGoodsStockByParams(map);
        if (stockEntities.size() > 0) {
            for (GbDepartmentGoodsStockEntity stock : stockEntities) {
                BigDecimal decimal = new BigDecimal(stock.getGbDgsRestWeight()).divide(new BigDecimal(showStandardScale), 1, BigDecimal.ROUND_HALF_UP);
                if (showStandardScale.equals("-1")) {
                    stock.setGbDgsRestWeightShowStandard(null);
                    stock.setGbDgsRestWeightShowStandardName(null);
                } else {
                    stock.setGbDgsRestWeightShowStandard(decimal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    stock.setGbDgsRestWeightShowStandardName(showStandardName);
                }
                gbDepGoodsStockService.update(stock);
            }
        }
        return R.ok();
    }


    @RequestMapping(value = "/updateDepGoodsGb", method = RequestMethod.POST)
    @ResponseBody
    public R updateDepGoodsGb(@RequestBody GbDepartmentDisGoodsEntity depDisGoods) {
        gbDepartmentDisGoodsService.update(depDisGoods);
        return R.ok();
    }

    @RequestMapping(value = "/updateDepGoodsSellingPrice", method = RequestMethod.POST)
    @ResponseBody
    public R updateDepGoodsSellingPrice(Integer depGoodsId, String sellingPrice) {
        GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(depGoodsId);
        departmentDisGoodsEntity.setGbDdgSellingPrice(sellingPrice);
        gbDepartmentDisGoodsService.update(departmentDisGoodsEntity);

        return R.ok();
    }

    @RequestMapping(value = "/deleteDepGoods/{depGoodsId}")
    @ResponseBody
    public R deleteDepGoods(@PathVariable Integer depGoodsId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depGoodsId", depGoodsId);
        map.put("restWeight", 0);
        List<GbDepartmentGoodsStockEntity> stockEntities = gbDepGoodsStockService.queryGoodsStockByParams(map);
        if (stockEntities.size() > 0) {
            return R.error(-1, "有库存，不能删除");
        } else {
            map.put("restWeight",null);
            map.put("date",formatWhatDay(0));
            System.out.println("mapmamaamama" + map);
            GbDepartmentGoodsDailyEntity gbDepartmentGoodsDailyEntity = gbDepartmentGoodsDailyService.queryDepGoodsDailyItem(map);
            if(gbDepartmentGoodsDailyEntity != null){
                gbDepartmentGoodsDailyEntity.setGbDgdStatus(-1);
                gbDepartmentGoodsDailyService.update(gbDepartmentGoodsDailyEntity);
            }
            gbDepartmentDisGoodsService.delete(depGoodsId);
            return R.ok();
        }
    }


    /**
     * lscgdh
     * @param depId
     * @return
     */
    @RequestMapping(value = "/stockDepGetDepGoodsGb/{depId}")
    @ResponseBody
    public R stockDepGetDepGoodsGb(@PathVariable Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("status", 4);
        map.put("date", formatWhatDay(0));
        System.out.println("guanndn" + map);
        List<GbDistributerFatherGoodsEntity> goodsEntities = gbDepartmentDisGoodsService.depQueryDepGoodsWithOrderGb(map);

        return R.ok().put("data", goodsEntities);
    }


    @RequestMapping(value = "/depGetDepGoodsGbPage")
    @ResponseBody
    public R depGetDepGoodsGbPage(Integer limit, Integer page, Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("pull", 0);

        // 1. 获取总数
        int total = gbDepartmentDisGoodsService.queryDepGoodsCount(map);
        log.info("总记录数: {}", total);

        // 2. 获取当前页数据
        map.put("status", 4);
        map.put("date", formatWhatDay(0));
        map.put("limit", limit);
        map.put("offset", (page - 1) * limit);
        System.out.println("mapaopapapa" + map);
        List<GbDepartmentDisGoodsEntity> currentPageList = gbDepartmentDisGoodsService.depQueryDepGoodsWithOrderForAi(map);

        // 5. 返回分页数据
        PageUtils pageUtil = new PageUtils(currentPageList, total, limit, page);
        return R.ok().put("page", pageUtil);
    }


    @RequestMapping(value = "/disGetDepGoodsGbPage")
    @ResponseBody
    public R disGetDepGoodsGbPage(Integer limit, Integer page, Integer disId, Integer goodsType) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        if(goodsType < 99){
            map.put("goodsType", goodsType);
        }else{
            if(goodsType == 101){
                map.put("fresh", 1);

            }else if(goodsType == 102){
                map.put("pull", 1);
            }
        }

        // 1. 获取总数
        List<Integer > disGoodsIds =   gbDepartmentDisGoodsService.queryOnlyDisGoodsIds(map);
//        log.info("总记录数: {}", disGoodsIds.size());

        // 2. 获取当前页数据
        map.put("status", 4);
        map.put("date", formatWhatDay(0));
        map.put("limit", limit);
        map.put("offset", (page - 1) * limit);
        log.info("查询参数: limit={}, offset={}", limit, (page - 1) * limit);
        log.info("map查询: {}", map);
        TreeSet<GbDistributerGoodsEntity> currentPageSet = gbDepartmentDisGoodsService.disQueryDisGoodsWithOrderForAiTree(map);
        log.info("当前页数据量Tree: {}", currentPageSet.size());

        // 4. 处理每个商品的提示文本
        for(GbDistributerGoodsEntity distributerGoodsEntity: currentPageSet){
//            gbDistributerGoodsService.getTipText(distributerGoodsEntity);
            gbDistributerGoodsService.getStockTotal(distributerGoodsEntity);
        }
        log.info("最终返回数据量: {}", currentPageSet.size());


        // 5. 返回分页数据
        List<GbDistributerGoodsEntity> currentPageList = new ArrayList<>(currentPageSet);

        PageUtils pageUtil = new PageUtils(currentPageList, disGoodsIds.size(), limit, page);
        return R.ok().put("page", pageUtil);
    }


//    @RequestMapping(value = "/depGetDepGoodsGbCata/{depId}")
//    @ResponseBody
//    public R depGetDepGoodsGbCata(@PathVariable Integer depId) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("depId", depId);
//        map.put("status", 4);
//        map.put("date", formatWhatDay(0));
//        map.put("pull", 0);
//        map.put("notLinshi", 1);
//        System.out.println("newnwenennee" + map);
//        List<GbDistributerFatherGoodsEntity> goodsEntities = gbDepartmentDisGoodsService.depQueryDepGoodsWithOrderGbNew(map);
//
//        return R.ok().put("data", goodsEntities);
//    }

    /**
     * lscgdh
     * @param depId
     * @return
     */
    @RequestMapping(value = "/depGetDepGoodsGb/{depId}")
    @ResponseBody
    public R depGetDepGoodsGb(@PathVariable Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("status", 4);
        map.put("date", formatWhatDay(0));
        map.put("pull", 0);
        map.put("notLinshi", 1);
        System.out.println("newnwenennee" + map);
        List<GbDistributerFatherGoodsEntity> goodsEntities = gbDepartmentDisGoodsService.depQueryDepGoodsWithOrderGbNew(map);

        return R.ok().put("data", goodsEntities);
    }

    /**
     * lscgdh
     * @param depId
     * @return
     */
    @RequestMapping(value = "/depGetDepGoodsGbCata", method = RequestMethod.POST)
    @ResponseBody
    public R depGetDepGoodsGbCata(Integer depId, Integer fatherId,String controlString, Integer isPrice) {

        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        if(controlString.equals("price")){
            map.put("price", "1");
        }

        if(controlString.equals("fresh")){
            map.put("fresh", "1");
        }
        if(controlString.equals("isNotSelf")){
            map.put("isSelf",0);
        }
        if(controlString.equals("isSelf")){
            map.put("isSelf",1);
        }
        if(controlString.equals("autoSupplier")){
            map.put("supplierId",1);
        }
        if(isPrice != 0){
            map.put("isPrice", isPrice);
        }
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = new ArrayList<>();
//        List<GbDistributerFatherGoodsEntity> result = new ArrayList<>();

        System.out.println("quiciciciiciiicicici" + map);
        List<GbDistributerFatherGoodsEntity> cataList = gbDepartmentDisGoodsService.queryDepTypeFatherGoods(map);
        if (cataList.size() > 0) {
            if (fatherId == -1) {
                map.put("greatGrandId", cataList.get(0).getGbDistributerFatherGoodsId());
            } else {
                map.put("greatGrandId", fatherId);
            }

            map.put("status", 4);
            map.put("date", formatWhatDay(0));
            map.put("pull", 0);
            map.put("notLinshi", 1);
            System.out.println("mappapapappapa" + map);
            fatherGoodsEntities = gbDepartmentDisGoodsService.depQueryDepGoodsWithOrderGb(map);
            if(fatherGoodsEntities.size() > 0){
//                for(GbDistributerFatherGoodsEntity grand: fatherGoodsEntities){

//                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities1 = grand.getFatherGoodsEntities();
//                    for(GbDistributerFatherGoodsEntity fatherGoodsEntity: fatherGoodsEntities1){
//                        System.out.println("fatherGoodsene" + fatherGoodsEntity.getGbDfgFatherGoodsName() + "id=" + fatherGoodsEntity.getGbDistributerFatherGoodsId());
//                        int total = 0;
//                        if(fatherGoodsEntities1.size() > 0){
//                            for(GbDistributerFatherGoodsEntity orderFather: fatherGoodsEntity.getFatherGoodsEntities()){
//                                Integer gbDistributerFatherGoodsId1 = orderFather.getGbDistributerFatherGoodsId();
//                                Map<String, Object> mapF = new HashMap<>();
//                                mapF.put("fathersFatherId",gbDistributerFatherGoodsId1 );
//                                mapF.put("status", 1);
//                                mapF.put("depId", depId);
//                                System.out.println("mapffifffff" + mapF);
//                                Integer orderAmount = gbDepartmentOrdersService.queryGbDepartmentOrderAmount(mapF);
//
//                                total = total + orderAmount;
//                                System.out.println("orderrmr" + total);
//                            }
//                        }
//                        fatherGoodsEntity.setOrderAmount(total);
//                        System.out.println("aijaoellelelleleel" + total + "name===" + fatherGoodsEntity.getGbDfgFatherGoodsName());
//                    }
//                }

            }

        }

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("cataList", cataList);
        mapR.put("arr", fatherGoodsEntities);
        return R.ok().put("data", mapR);
    }



//
    @RequestMapping(value = "/depGetDepGoodsByFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R depGetDepGoodsByFatherId(Integer depId, Integer fatherId,String controlString, Integer isPrice) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("depFatherId", depId);
        map.put("fatherId", fatherId);
        map.put("status", 4);
        map.put("date", formatWhatDay(0));
        if(controlString.equals("price")){
            map.put("price", "1");
        }
        if(controlString.equals("fresh")){
            map.put("fresh", "1");
        }
        if(controlString.equals("isNotSelf")){
            map.put("isSelf",0);
            map.put("goodsType", getGbDisGoodsTypeChuku());
        }
        if(controlString.equals("isSelf")){
            map.put("isSelf",1);
        }
        if(controlString.equals("autoSupplier")){
            map.put("supplierId",1);
        }

        if(isPrice != 0){
            map.put("isPrice", isPrice);
        }

        System.out.println("mappapapappapa" + map);
        List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepartmentDisGoodsService.depGetDepsGoodsGb(map);

		GbDistributerFatherGoodsEntity fatherGoodsEntity = gbDistributerFatherGoodsService.queryObject(fatherId);
		Integer gbDfgFathersFatherId = fatherGoodsEntity.getGbDfgFathersFatherId();
		Map<String, Object> mapG = new HashMap<>();
		mapG.put("fathersFatherId", gbDfgFathersFatherId);
        mapG.put("depId", depId);
        mapG.put("depFatherId", depId);
        if(controlString.equals("price")){
            mapG.put("price", "1");
        }
        if(controlString.equals("fresh")){
            mapG.put("fresh", "1");
        }
        if(controlString.equals("isNotSelf")){
            mapG.put("isSelf",0);
            mapG.put("goodsType", getGbDisGoodsTypeChuku());
        }
        if(controlString.equals("isSelf")){
            mapG.put("isSelf",1);
        }if(controlString.equals("autoSupplier")){
            mapG.put("supplierId",1);
        }

        if(isPrice != 0){
            mapG.put("isPrice", isPrice);
        }
        System.out.println("mapgggggg" + mapG);
		List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDepartmentDisGoodsService.queryDepFatherGoodsByParams(mapG);

		Map<String, Object> mapResult = new HashMap<>();
        mapResult.put("goodsArr", departmentDisGoodsEntities);
        mapResult.put("fatherArr",  fatherGoodsEntities);
        System.out.println("marmrmmr" + mapResult);
		return R.ok().put("data", mapResult);
    }


    /**
     * vue后台接口
     * @param limit
     * @param page
     * @param fatherId
     * @param depFatherId
     * @param disId
     * @return
     */
    @RequestMapping(value = "/depGetDepsGoodsGbPage")
    @ResponseBody
    public R depGetDepsGoodsGbPage(Integer limit, Integer page, Integer fatherId, Integer depFatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("depFatherId", depFatherId);
        map.put("status", 1);

        List<GbDepartmentDisGoodsEntity> depDisGoodsEntities = gbDepartmentDisGoodsService.depGetDepsGoodsGb(map);
        if (depDisGoodsEntities != null) {
            Map<String, Object> map3 = new HashMap<>();
            map3.put("depFatherId", fatherId);
            int total = gbDepartmentDisGoodsService.queryGbDisGoodsTotal(map3);

            PageUtils pageUtil = new PageUtils(depDisGoodsEntities, total, limit, page);
            return R.ok().put("page", pageUtil);
        } else {
            return R.error(-1, "meiyou");
        }
    }

}
