package com.nongxinle.controller;

/**
 * @author lpy
 * @date 07-27 17:38
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.UploadFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbDepartmentTypeMendian;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDepOrderBuyStatusWithPurchase;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxOrderStatusNew;
import static com.nongxinle.utils.PinYin4jUtils.*;
import static com.nongxinle.utils.PinYin4jUtils.getXiegang;


@RestController
@RequestMapping("api/nxdistributergoods")
public class NxDistributerGoodsController {
    @Autowired
    private NxDistributerGoodsService dgService;

    @Autowired
    private NxDepartmentOrdersService depOrdersService;

    @Autowired
    private NxGoodsService nxGoodsService;

    @Autowired
    private NxDistributerFatherGoodsService dgfService;

    @Autowired
    private NxDistributerStandardService dsService;
    @Autowired
    private NxDistributerAliasService disAliasService;

    @Autowired
    private NxDepartmentDisGoodsService nxDepDisGoodsService;

    @Autowired
    private NxDistributerPurchaseGoodsService nxDisPurchaseGoodsService;

    @Autowired
    private NxDepartmentDisGoodsService nxDepartmentDisGoodsService;
    @Autowired
    private NxDistributerFatherGoodsService nxDistributerFatherGoodsService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepDisGoodsService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private GbDistributerFatherGoodsService gbDgfService;
    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;
    @Autowired
    private NxDistributerStandardService nxDistributerStandardService;


    private static final String KEY = "C5HBZ-KEIW2-JXXUJ-COLGS-FQO47-WWFAK";






    @RequestMapping(value = "/abcdefgh")
    @ResponseBody
    public R abcdefg() {

//           for(int i = 1; i < 13; i++){
//               Map<String, Object> map = new HashMap<>();
//               map.put("disId", 1);
//               map.put("nxGreatGrandFatherId", i);
//               List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = dgService.queryDisGoodsByParams(map);
//               if(nxDistributerGoodsEntities.size() > 0){
//                   for(NxDistributerGoodsEntity distributerGoodsEntity: nxDistributerGoodsEntities){
//                       Map<String, Object> mapG = new HashMap<>();
//                       mapG.put("disGoodsId", distributerGoodsEntity.getNxDistributerGoodsId());
//                       List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(mapG);
//                       if(ordersEntities.size() == 0){
//                           dgService.delete(distributerGoodsEntity.getNxDistributerGoodsId());
////                           Integer nxDgDfgGoodsFatherId = distributerGoodsEntity.getNxDgDfgGoodsFatherId();
////                           nxDistributerFatherGoodsService.delete(nxDgDfgGoodsFatherId);
//                       }
//                   }
//               }
//           }


        Map<String, Object> map = new HashMap<>();
        map.put("goodsLevel", 2);
        map.put("disId", 1);

        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);

        if (fatherGoodsEntities.size() > 0) {
            for (NxDistributerFatherGoodsEntity fatherGoodsEntity : fatherGoodsEntities) {
                Map<String, Object> mapD = new HashMap<>();
                mapD.put("dgFatherId", fatherGoodsEntity.getNxDistributerFatherGoodsId());
                List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = dgService.queryDisGoodsByParams(mapD);
                if (nxDistributerGoodsEntities.size() == 0) {
                    nxDistributerFatherGoodsService.delete(fatherGoodsEntity.getNxDistributerFatherGoodsId());
                }
            }
        }

        return R.ok();
    }


    @RequestMapping(value = "/saveSupplierGoods", method = RequestMethod.POST)
    @ResponseBody
    public R saveSupplierGoods(String ids, Integer supplierId) {
        String[] arr = ids.split(",");
        if (arr.length > 0) {
            for (String id : arr) {
                NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(Integer.valueOf(id));
                distributerGoodsEntity.setNxDgSupplierId(supplierId);
                distributerGoodsEntity.setNxDgPurchaseAuto(2);
                dgService.update(distributerGoodsEntity);
            }
        }

        return R.ok();
    }


    @RequestMapping(value = "/supplierGetDisGoodsFather", method = RequestMethod.POST)
    @ResponseBody
    public R supplierGetDisGoodsFather(Integer disId, Integer supplierId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("supplierId", supplierId);
        map.put("autoType", 2);
        System.out.println("damfdanfas" + map);
        List<NxGoodsEntity> fatherGoodsEntities = dgService.querySupplierFather(map);
        return R.ok().put("data", fatherGoodsEntities);
    }


    @RequestMapping(value = "/supplierGetDisGoodsCata", method = RequestMethod.POST)
    @ResponseBody
    public R supplierGetDisGoodsCata(Integer disId, Integer supplierId) {

//        NxJrdhSupplierEntity supplierEntity = nxJrdhSupplierService.querySupplierByUserId(sellerId);
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("supplierId", supplierId);
        map.put("autoType", 2);
        map.put("isHidden", 0);

        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = dgService.querySupplierGrand(map);
        return R.ok().put("data", fatherGoodsEntities);
    }


    @RequestMapping(value = "/supplierGetGoodsListByFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R supplierGetGoodsListByFatherId(Integer fatherId, Integer supplierId,
                                            Integer limit, Integer page) {


        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("grandId", fatherId);
        map.put("supplierId", supplierId);
        map.put("autoType", 2);
        map.put("isHidden", 0);
        List<NxDistributerGoodsEntity> goodsEntities1 = dgService.queryDisGoodsByParams(map);


        Map<String, Object> map3 = new HashMap<>();
        map3.put("grandId", fatherId);
        map3.put("supplierId", supplierId);
        map3.put("isHidden", 0);
        map3.put("autoType", 2);
        System.out.println(map3 + "map3333");
        int total = dgService.queryDisGoodsTotal(map3);
        PageUtils pageUtil = new PageUtils(goodsEntities1, total, limit, page);
        return R.ok().put("page", pageUtil);
    }


    @RequestMapping(value = "/saveNxDisGoodsForCash", method = RequestMethod.POST)
    @ResponseBody
    public R saveNxDisGoodsForCash(@RequestBody NxDistributerGoodsEntity goods) {

        String goodsName = goods.getNxDgGoodsName();

        Map<String, Object> map = new HashMap<>();
        map.put("disId", goods.getNxDgDistributerId());
        map.put("nx", -1);
        map.put("goodsLevel", 2);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
        NxDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
        goods.setNxDgDfgGoodsFatherId(fatherGoodsEntity.getNxDistributerFatherGoodsId());
        goods.setNxDgDfgGoodsGrandId(fatherGoodsEntity.getNxDfgFathersFatherId());
        goods.setNxDgPurchaseAuto(-1);
        String pinyin = hanziToPinyin(goodsName);
        String headPinyin = getHeadStringByString(goodsName, false, null);
        goods.setNxDgGoodsPinyin(pinyin);
        goods.setNxDgGoodsPy(headPinyin);
        goods.setNxDgBuyingPrice("0.1");
        goods.setNxDgWillPrice("0.1");
        goods.setNxDgGoodsIsHidden(0);
        dgService.save(goods);

        Integer gbDfgGoodsAmount = fatherGoodsEntity.getNxDfgGoodsAmount();
        fatherGoodsEntity.setNxDfgGoodsAmount(gbDfgGoodsAmount + 1);
        dgfService.update(fatherGoodsEntity);

        return R.ok().put("data", goods);
    }


    @ResponseBody
    @RequestMapping(value = "/updateFatherNx", produces = "text/html;charset=UTF-8")
    public R updateFatherNx(@RequestParam("file") MultipartFile file,
                            @RequestParam("goodsName") String goodsName,
                            @RequestParam("id") Integer id,
                            HttpSession session) {

        NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(id);
        String gbDistributerFoodImg = distributerGoodsEntity.getNxDgGoodsFile();
        if (distributerGoodsEntity.getNxDgGoodsFile() != null) {
            ServletContext servletContext = session.getServletContext();
            String realPath1 = servletContext.getRealPath(gbDistributerFoodImg);
            File file1 = new File(realPath1);
            if (file1.exists()) {
                file1.delete();
            }
        }


        //1,上传图片
        String newUploadName = "goodsImage";
        String englishKuohao = getEnglishKuohao(goodsName);
        String lastFileName = goodsName + formatFullTime() + "large";
        String pinyin = hanziToPinyin(englishKuohao);
        String headPinyin = getHeadStringByString(englishKuohao, false, null);
        distributerGoodsEntity.setNxDgGoodsPinyin(pinyin);
        distributerGoodsEntity.setNxDgGoodsPy(headPinyin);
        String realPath = UploadFile.uploadFileName(session, newUploadName, file, lastFileName);
        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + lastFileName + ".jpg";
        distributerGoodsEntity.setNxDgGoodsFile(filePath);
        distributerGoodsEntity.setNxDgGoodsName(englishKuohao);
        dgService.update(distributerGoodsEntity);

        return R.ok();
    }


    @ResponseBody
    @RequestMapping(value = "/updateFatherBigNx", produces = "text/html;charset=UTF-8")
    public R updateFatherBigNx(@RequestParam("file") MultipartFile file,
                               @RequestParam("goodsName") String goodsName,
                               @RequestParam("id") Integer id,
                               HttpSession session) {

        NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(id);
        String gbDistributerFoodImg = distributerGoodsEntity.getNxDgGoodsFileLarge();
        if (distributerGoodsEntity.getNxDgGoodsFileLarge() != null) {
            ServletContext servletContext = session.getServletContext();
            String realPath1 = servletContext.getRealPath(gbDistributerFoodImg);
            File file1 = new File(realPath1);
            if (file1.exists()) {
                file1.delete();
            }
        }


        //1,上传图片
        String newUploadName = "goodsImage";
        String englishKuohao = getEnglishKuohao(goodsName);
        String lastFileName = goodsName + formatFullTime() + "large";
        String pinyin = hanziToPinyin(englishKuohao);
        String headPinyin = getHeadStringByString(englishKuohao, false, null);
        distributerGoodsEntity.setNxDgGoodsPinyin(pinyin);
        distributerGoodsEntity.setNxDgGoodsPy(headPinyin);
        String realPath = UploadFile.uploadFileName(session, newUploadName, file, lastFileName);
        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + lastFileName + ".jpg";
        distributerGoodsEntity.setNxDgGoodsFileLarge(filePath);
        distributerGoodsEntity.setNxDgGoodsName(englishKuohao);
        dgService.update(distributerGoodsEntity);

        return R.ok();
    }


    @ResponseBody
    @RequestMapping(value = "/saveLinshiGoods", produces = "text/html;charset=UTF-8")
    public NxDistributerGoodsEntity saveFather(@RequestParam("file") MultipartFile file,
                                               @RequestParam("goodsName") String goodsName,
                                               @RequestParam("standard") String standard,
                                               @RequestParam("detail") String detail,
                                               @RequestParam("disId") Integer disId,
                                               HttpSession session) {

        //1,上传图片
        String newUploadName = "goodsImage";
        String englishKuohao = getEnglishKuohao(goodsName);
        String lastFileName = getXiegang(englishKuohao) + formatFullTime();

        String realPath = UploadFile.uploadFileName(session, newUploadName, file, lastFileName);
        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + lastFileName + ".jpg";

        NxDistributerGoodsEntity goods = new NxDistributerGoodsEntity();
        Map<String, Object> map = new HashMap<>();
        map.put("disId", goods.getNxDgDistributerId());
        map.put("nx", -1);
        map.put("goodsLevel", 2);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
        NxDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
        goods.setNxDgDfgGoodsFatherId(fatherGoodsEntity.getNxDistributerFatherGoodsId());
        goods.setNxDgDfgGoodsGrandId(fatherGoodsEntity.getNxDfgFathersFatherId());
        goods.setNxDgPurchaseAuto(-1);
        goods.setNxDgGoodsFile(filePath);
        goods.setNxDgGoodsFileLarge(filePath);
        goods.setNxDgGoodsName(goodsName);
        String pinyin = hanziToPinyin(goodsName);
        String headPinyin = getHeadStringByString(goodsName, false, null);
        goods.setNxDgGoodsPinyin(pinyin);
        goods.setNxDgGoodsPy(headPinyin);
        goods.setNxDgDistributerId(disId);
        goods.setNxDgBuyingPriceIsGrade(0);
        goods.setNxDgBuyingPrice("0.1");
        goods.setNxDgWillPrice("0.1");
        goods.setNxDgGoodsIsHidden(0);
        goods.setNxDgGoodsStandardname(standard);
        goods.setNxDgGoodsDetail(detail);
        goods.setNxDgBuyingPriceUpdate(formatWhatDate(0));
        System.out.println("savegoogog" + goods);
        dgService.save(goods);

        Integer gbDfgGoodsAmount = fatherGoodsEntity.getNxDfgGoodsAmount();
        fatherGoodsEntity.setNxDfgGoodsAmount(gbDfgGoodsAmount + 1);
        dgfService.update(fatherGoodsEntity);

        return goods;
    }


    @RequestMapping(value = "/disExchangeDisGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disExchangeDisGoods(Integer lsGoodsId, Integer nxGoodsId, Integer disId) {

        NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(nxGoodsId);
        Integer nxDgDfgGoodsFatherId = nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId();
        Integer nxDgDfgGoodsGrandId = nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId();
        Integer nxDgNxGoodsId = nxDistributerGoodsEntity.getNxDgNxGoodsId();
        Integer nxDgNxFatherId = nxDistributerGoodsEntity.getNxDgNxFatherId();

        Map<String, Object> linshiMap = new HashMap<>();
        linshiMap.put("disGoodsId", lsGoodsId);
        List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(linshiMap);
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                ordersEntity.setNxDoDisGoodsId(nxGoodsId);
                ordersEntity.setNxDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
                ordersEntity.setNxDoDisGoodsGrandId(nxDgDfgGoodsGrandId);
                ordersEntity.setNxDoNxGoodsId(nxDgNxGoodsId);
                ordersEntity.setNxDoNxGoodsFatherId(nxDgNxFatherId);
                depOrdersService.update(ordersEntity);
                nxDistributerGoodsEntity.setNxDgWillPrice(ordersEntity.getNxDoPrice());
                nxDistributerGoodsEntity.setNxDgBuyingPriceOne(ordersEntity.getNxDoPrice());
                nxDistributerGoodsEntity.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
                dgService.update(nxDistributerGoodsEntity);
            }
        }

        List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(linshiMap);
        if (departmentDisGoodsEntities.size() > 0) {
            for (NxDepartmentDisGoodsEntity depGoodsEntity : departmentDisGoodsEntities) {
                depGoodsEntity.setNxDdgDisGoodsId(nxGoodsId);
                depGoodsEntity.setNxDdgDisGoodsFatherId(nxDgDfgGoodsFatherId);
                depGoodsEntity.setNxDdgDisGoodsGrandId(nxDgDfgGoodsGrandId);
                nxDepartmentDisGoodsService.update(depGoodsEntity);
            }
        }

        List<NxDistributerStandardEntity> distributerStandardEntities = nxDistributerStandardService.queryDisStandardByParams(linshiMap);
        if (distributerStandardEntities.size() > 0) {
            for (NxDistributerStandardEntity standardEntity : distributerStandardEntities) {
                standardEntity.setNxDsDisGoodsId(nxGoodsId);
                nxDistributerStandardService.update(standardEntity);
            }
        }

        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDisPurchaseGoodsService.queryPurchaseGoodsByParams(linshiMap);
        if (purchaseGoodsEntities.size() > 0) {
            for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                purchaseGoodsEntity.setNxDpgDisGoodsId(nxDgNxGoodsId);
                purchaseGoodsEntity.setNxDpgDisGoodsFatherId(nxDgDfgGoodsFatherId);
                purchaseGoodsEntity.setNxDpgDisGoodsGrandId(nxDgDfgGoodsGrandId);
                nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }

        System.out.println("lishsisi" + lsGoodsId);
        List<GbDistributerEntity> distributerEntities = gbDistributerGoodsService.queryGbDisByNxGoodsId(lsGoodsId);
        if (distributerEntities.size() > 0) {
            for (GbDistributerEntity gbDistributerEntity : distributerEntities) {
                updateGbDisGoods(lsGoodsId, nxDistributerGoodsEntity.getNxDistributerGoodsId(), disId, gbDistributerEntity);
            }
        }


        dgService.delete(lsGoodsId);

        return R.ok();
    }

    @RequestMapping(value = "/exchangeDisGoods", method = RequestMethod.POST)
    @ResponseBody
    public R exchangeDisGoods(Integer lsGoodsId, Integer nxGoodsId, Integer disId) {

        Map<String, Object> mapG = new HashMap<>();
        mapG.put("disId", disId);
        mapG.put("nxGoodsId", nxGoodsId);
        NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryOneGoodsAboutNxGoods(mapG);
        Integer nxDgDfgGoodsFatherId = nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId();
        Integer nxDgDfgGoodsGrandId = nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId();
        Integer distributerGoodsId = nxDistributerGoodsEntity.getNxDistributerGoodsId();
        Integer nxDgNxGoodsId = nxDistributerGoodsEntity.getNxDgNxGoodsId();
        Integer nxDgNxFatherId = nxDistributerGoodsEntity.getNxDgNxFatherId();


        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", lsGoodsId);
        List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(map);
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                ordersEntity.setNxDoDisGoodsId(distributerGoodsId);
                ordersEntity.setNxDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
                ordersEntity.setNxDoDisGoodsGrandId(nxDgDfgGoodsGrandId);
                ordersEntity.setNxDoNxGoodsId(nxDgNxGoodsId);
                ordersEntity.setNxDoNxGoodsFatherId(nxDgNxFatherId);
                depOrdersService.update(ordersEntity);
                nxDistributerGoodsEntity.setNxDgWillPrice(ordersEntity.getNxDoPrice());
                nxDistributerGoodsEntity.setNxDgBuyingPriceOne(ordersEntity.getNxDoPrice());
                nxDistributerGoodsEntity.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
                dgService.update(nxDistributerGoodsEntity);
            }
        }

        List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(map);
        if (departmentDisGoodsEntities.size() > 0) {
            for (NxDepartmentDisGoodsEntity depGoodsEntity : departmentDisGoodsEntities) {
                depGoodsEntity.setNxDdgDisGoodsId(distributerGoodsId);
                depGoodsEntity.setNxDdgDisGoodsFatherId(nxDgDfgGoodsFatherId);
                depGoodsEntity.setNxDdgDisGoodsGrandId(nxDgDfgGoodsGrandId);
                nxDepartmentDisGoodsService.update(depGoodsEntity);
            }
        }

        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDisPurchaseGoodsService.queryPurchaseGoodsByParams(map);
        if (purchaseGoodsEntities.size() > 0) {
            for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                purchaseGoodsEntity.setNxDpgDisGoodsId(distributerGoodsId);
                purchaseGoodsEntity.setNxDpgDisGoodsFatherId(nxDgDfgGoodsFatherId);
                purchaseGoodsEntity.setNxDpgDisGoodsGrandId(nxDgDfgGoodsGrandId);
                nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }

        System.out.println("lishsisi" + lsGoodsId);
        List<GbDistributerEntity> distributerEntities = gbDistributerGoodsService.queryGbDisByNxGoodsId(lsGoodsId);
        if (distributerEntities.size() > 0) {
            for (GbDistributerEntity gbDistributerEntity : distributerEntities) {
                updateGbDisGoods(lsGoodsId, nxDistributerGoodsEntity.getNxDistributerGoodsId(), disId, gbDistributerEntity);
            }
        }

        dgService.delete(lsGoodsId);

        return R.ok();
    }

    private void updateGbDisGoods(Integer lsGoodsId, Integer nxGoodsId, Integer disId, GbDistributerEntity distributerEntity) {

        NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(nxGoodsId);

        GbDistributerGoodsEntity cgnGoods = new GbDistributerGoodsEntity();
        cgnGoods.setGbDgControlFresh(0);
        cgnGoods.setGbDgControlPrice(0);
        cgnGoods.setGbDgGoodsName(nxDistributerGoodsEntity.getNxDgGoodsName());
        cgnGoods.setGbDgGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
        cgnGoods.setGbDgGoodsPy(nxDistributerGoodsEntity.getNxDgGoodsPy());
        cgnGoods.setGbDgGoodsPinyin(nxDistributerGoodsEntity.getNxDgGoodsPinyin());
        cgnGoods.setGbDgGoodsStandardWeight(nxDistributerGoodsEntity.getNxDgGoodsStandardWeight());
        cgnGoods.setGbDgGoodsDetail(nxDistributerGoodsEntity.getNxDgGoodsDetail());
        cgnGoods.setGbDgGoodsBrand(nxDistributerGoodsEntity.getNxDgGoodsBrand());
        cgnGoods.setGbDgGoodsPlace(nxDistributerGoodsEntity.getNxDgGoodsPlace());
        cgnGoods.setGbDgGoodsSort(nxDistributerGoodsEntity.getNxDgGoodsSort());

        cgnGoods.setGbDgNxDistributerId(disId);
        cgnGoods.setGbDgGoodsStatus(0);
        cgnGoods.setGbDgGoodsIsWeight(0);
        cgnGoods.setGbDgNxGoodsId(nxDistributerGoodsEntity.getNxDgNxGoodsId());
        cgnGoods.setGbDgNxFatherId(nxDistributerGoodsEntity.getNxDgNxFatherId());
        cgnGoods.setGbDgNxGrandId(nxDistributerGoodsEntity.getNxDgNxGrandId());
        cgnGoods.setGbDgNxGreatGrandId(nxDistributerGoodsEntity.getNxDgNxGreatGrandId());
        cgnGoods.setGbDgPullOff(0);
        cgnGoods.setGbDgGoodsType(5);
        cgnGoods.setGbDgNxDistributerId(nxDistributerGoodsEntity.getNxDgDistributerId());
        cgnGoods.setGbDgNxDistributerGoodsId(nxGoodsId);
        cgnGoods.setGbDgControlFresh(0);
        cgnGoods.setGbDgControlPrice(0);
        cgnGoods.setGbDgGoodsInventoryType(1);
        cgnGoods.setGbDgIsFranchisePrice(0);
        cgnGoods.setGbDgIsSelfControl(0);
        cgnGoods.setGbDgDistributerId(distributerEntity.getGbDistributerId());

        Integer distributerId = distributerEntity.getGbDistributerId();
        Map<String, Object> map = new HashMap<>();
        map.put("disId", distributerId);
        map.put("type", 5);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
        Integer gbDepartmentId = departmentEntities.get(0).getGbDepartmentId();
        cgnGoods.setGbDgGbDepartmentId(gbDepartmentId);
        GbDistributerGoodsEntity disGoods = saveDisGoodsForNx(cgnGoods);

        Map<String, Object> mapDepGoods = new HashMap<>();
        mapDepGoods.put("nxGoodsId", lsGoodsId);
        System.out.println("depGodoodoasodaf" + mapDepGoods);

        List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepDisGoodsService.queryGbDepDisGoodsByParams(mapDepGoods);
        if (departmentDisGoodsEntities.size() > 0) {
            for (GbDepartmentDisGoodsEntity departmentDisGoods : departmentDisGoodsEntities) {
                GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepDisGoodsService.queryObject(departmentDisGoods.getGbDepartmentDisGoodsId());
                departmentDisGoodsEntity.setGbDdgNxDistributerGoodsId(nxGoodsId);
                departmentDisGoodsEntity.setGbDdgDisGoodsId(disGoods.getGbDistributerGoodsId());
                departmentDisGoodsEntity.setGbDdgDisGoodsFatherId(disGoods.getGbDgDfgGoodsFatherId());
                gbDepDisGoodsService.update(departmentDisGoodsEntity);
            }
        }

        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(mapDepGoods);
        if (ordersEntities.size() > 0) {
            for (GbDepartmentOrdersEntity order : ordersEntities) {
                GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(order.getGbDepartmentOrdersId());
                gbDepartmentOrdersEntity.setGbDoDisGoodsId(disGoods.getGbDistributerGoodsId());
                gbDepartmentOrdersEntity.setGbDoDisGoodsFatherId(disGoods.getGbDgDfgGoodsFatherId());
                gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
            }
        }

        gbDistributerGoodsService.delete(lsGoodsId);

    }


    @RequestMapping(value = "/exchangeNewGoodsDis", method = RequestMethod.POST)
    @ResponseBody
    public R exchangeNewGoodsDis(Integer changeId, Integer toGoodsId) {

        NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(changeId);
        NxDistributerGoodsEntity toNxGoods = dgService.queryObject(toGoodsId);
        //order
        Map<String, Object> mapSearch = new HashMap<>();
        mapSearch.put("disGoodsId", changeId);
        List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(mapSearch);
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                ordersEntity.setNxDoDisGoodsId(toNxGoods.getNxDistributerGoodsId());
                ordersEntity.setNxDoDisGoodsGrandId(toNxGoods.getNxDgDfgGoodsGrandId());
                ordersEntity.setNxDoDisGoodsFatherId(toNxGoods.getNxDgDfgGoodsFatherId());
                ordersEntity.setNxDoNxGoodsId(toGoodsId);
                ordersEntity.setNxDoNxGoodsFatherId(toNxGoods.getNxDgNxFatherId());
                depOrdersService.update(ordersEntity);
            }
        }


        //depGoods
        List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(mapSearch);
        if (departmentDisGoodsEntities.size() > 0) {
            for (NxDepartmentDisGoodsEntity depGoodsEntity : departmentDisGoodsEntities) {
                depGoodsEntity.setNxDdgDepGoodsName(toNxGoods.getNxDgGoodsName());
                depGoodsEntity.setNxDdgDepGoodsStandardname(toNxGoods.getNxDgGoodsStandardname());
                depGoodsEntity.setNxDdgDepGoodsDetail(toNxGoods.getNxDgGoodsDetail());
                depGoodsEntity.setNxDdgGoodsPlace(toNxGoods.getNxDgGoodsPlace());
                depGoodsEntity.setNxDdgDepGoodsBrand(toNxGoods.getNxDgGoodsBrand());
                depGoodsEntity.setNxDdgDisGoodsId(toNxGoods.getNxDistributerGoodsId());
                depGoodsEntity.setNxDdgDisGoodsGrandId(toNxGoods.getNxDgDfgGoodsGrandId());
                depGoodsEntity.setNxDdgDisGoodsFatherId(toNxGoods.getNxDgDfgGoodsFatherId());
                nxDepartmentDisGoodsService.update(depGoodsEntity);
            }
        }

        //purGoods
        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDisPurchaseGoodsService.queryPurchaseGoodsByParams(mapSearch);

        if (purchaseGoodsEntities.size() > 0) {
            for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                purchaseGoodsEntity.setNxDpgDisGoodsId(toNxGoods.getNxDistributerGoodsId());
                purchaseGoodsEntity.setNxDpgDisGoodsGrandId(toNxGoods.getNxDgDfgGoodsGrandId());
                purchaseGoodsEntity.setNxDpgDisGoodsFatherId(toNxGoods.getNxDgDfgGoodsFatherId());
                nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }

        dgService.delete(changeId);

        Map<String, Object> map1 = new HashMap<>();
        map1.put("dgFatherId", distributerGoodsEntity.getNxDgDfgGoodsFatherId());
        //搜索fatherId下有几个disGoods
        List<NxDistributerGoodsEntity> totalDisGoods = dgService.queryDisGoodsByParams(map1);
        //如果disGoods的父类只有一个商品
        if (totalDisGoods.size() == 1) {
            //父类Entity
            NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = dgfService.queryObject(distributerGoodsEntity.getNxDgDfgGoodsFatherId());
            //disGoods的grandId
            Integer grandId = nxDistributerFatherGoodsEntity.getNxDfgFathersFatherId();
            Map<String, Object> mapGrand = new HashMap<>();
            mapGrand.put("fathersFatherId", grandId);
            //搜索grand有几个兄弟
            List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisFathersGoodsByParams(mapGrand);
            if (fatherGoodsEntities.size() == 1) {
                Integer nxDfgFathersFatherId = fatherGoodsEntities.get(0).getNxDfgFathersFatherId();
                NxDistributerFatherGoodsEntity grandEntity = dgfService.queryObject(nxDfgFathersFatherId);
                Integer greatGrandId = grandEntity.getNxDfgFathersFatherId();
                Map<String, Object> map = new HashMap<>();
                map.put("fathersFatherId", greatGrandId);
                List<NxDistributerFatherGoodsEntity> grandGoodsEntities = dgfService.queryDisFathersGoodsByParams(map);

                //如果grandFather也是只有一个，则删除greatGrandFather
                if (grandGoodsEntities.size() == 1) {
                    dgfService.delete(greatGrandId);
                }
                dgfService.delete(grandId);
            }
            dgfService.delete(distributerGoodsEntity.getNxDgDfgGoodsFatherId());
        } else {
            //父类商品数量减去1
            NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = dgfService.queryObject(distributerGoodsEntity.getNxDgDfgGoodsFatherId());
            Integer nxDfgGoodsAmount = nxDistributerFatherGoodsEntity.getNxDfgGoodsAmount();
            nxDistributerFatherGoodsEntity.setNxDfgGoodsAmount(nxDfgGoodsAmount - 1);
            dgfService.update(nxDistributerFatherGoodsEntity);
        }

        return R.ok();
    }


    @RequestMapping(value = "/exchangeNewGoods", method = RequestMethod.POST)
    @ResponseBody
    public R exchangeNewGoods(Integer deleteId, Integer toGoodsId) {

        NxGoodsEntity nxGoodsEntity = nxGoodsService.queryObject(toGoodsId);
        List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.querydisGoodsByNxGoodsId(deleteId);
        if (distributerGoodsEntities.size() > 0) {
            for (NxDistributerGoodsEntity delGoods : distributerGoodsEntities) {
                Map<String, Object> mapDepGoods = new HashMap<>();
                mapDepGoods.put("nxGoodsId", toGoodsId);
                mapDepGoods.put("disId", delGoods.getNxDgDistributerId());
                NxDistributerGoodsEntity toGoods = dgService.queryOneGoodsAboutNxGoods(mapDepGoods);
                //order
                Map<String, Object> mapSearch = new HashMap<>();
                mapSearch.put("disGoodsId", delGoods.getNxDistributerGoodsId());
                System.out.println("mapsere" + mapSearch);
                List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(mapSearch);
                if (ordersEntities.size() > 0) {
                    for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                        ordersEntity.setNxDoDisGoodsId(toGoods.getNxDistributerGoodsId());
                        ordersEntity.setNxDoDisGoodsGrandId(toGoods.getNxDgDfgGoodsGrandId());
                        ordersEntity.setNxDoDisGoodsFatherId(toGoods.getNxDgDfgGoodsFatherId());
                        ordersEntity.setNxDoNxGoodsId(toGoodsId);
                        ordersEntity.setNxDoNxGoodsFatherId(nxGoodsEntity.getNxGoodsFatherId());
                        depOrdersService.update(ordersEntity);
                    }
                }

                //depGoods
                List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(mapSearch);
                if (departmentDisGoodsEntities.size() > 0) {
                    for (NxDepartmentDisGoodsEntity depGoodsEntity : departmentDisGoodsEntities) {
                        depGoodsEntity.setNxDdgDepGoodsName(toGoods.getNxDgGoodsName());
                        depGoodsEntity.setNxDdgDepGoodsStandardname(toGoods.getNxDgGoodsStandardname());
                        depGoodsEntity.setNxDdgDepGoodsDetail(toGoods.getNxDgGoodsDetail());
                        depGoodsEntity.setNxDdgGoodsPlace(toGoods.getNxDgGoodsPlace());
                        depGoodsEntity.setNxDdgDepGoodsBrand(toGoods.getNxDgGoodsBrand());
                        depGoodsEntity.setNxDdgDisGoodsId(toGoods.getNxDistributerGoodsId());
                        depGoodsEntity.setNxDdgDisGoodsGrandId(toGoods.getNxDgDfgGoodsGrandId());
                        depGoodsEntity.setNxDdgDisGoodsFatherId(toGoods.getNxDgDfgGoodsFatherId());
                        nxDepartmentDisGoodsService.update(depGoodsEntity);
                    }
                }

                //purGoods
                List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDisPurchaseGoodsService.queryPurchaseGoodsByParams(mapSearch);
                if (purchaseGoodsEntities.size() > 0) {
                    for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                        purchaseGoodsEntity.setNxDpgDisGoodsId(toGoods.getNxDistributerGoodsId());
                        purchaseGoodsEntity.setNxDpgDisGoodsGrandId(toGoods.getNxDgDfgGoodsGrandId());
                        purchaseGoodsEntity.setNxDpgDisGoodsFatherId(toGoods.getNxDgDfgGoodsFatherId());
                        nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
                    }
                }

                dgService.delete(delGoods.getNxDistributerGoodsId());

                Map<String, Object> map1 = new HashMap<>();
                map1.put("disId", delGoods.getNxDgDistributerId());
                map1.put("dgFatherId", delGoods.getNxDgDfgGoodsFatherId());
                //搜索fatherId下有几个disGoods
                List<NxDistributerGoodsEntity> totalDisGoods = dgService.queryDisGoodsByParams(map1);
                //如果disGoods的父类只有一个商品
                if (totalDisGoods.size() == 1) {
                    //父类Entity
                    NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = dgfService.queryObject(delGoods.getNxDgDfgGoodsFatherId());
                    //disGoods的grandId
                    Integer grandId = nxDistributerFatherGoodsEntity.getNxDfgFathersFatherId();
                    Map<String, Object> mapGrand = new HashMap<>();
                    mapGrand.put("fathersFatherId", grandId);
//                    mapGrand.put("disId", distributerGoodsEntity.getNxDgDfgGoodsFatherId());
                    //搜索grand有几个兄弟
                    List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisFathersGoodsByParams(mapGrand);
                    if (fatherGoodsEntities.size() == 1) {
                        Integer nxDfgFathersFatherId = fatherGoodsEntities.get(0).getNxDfgFathersFatherId();
                        NxDistributerFatherGoodsEntity grandEntity = dgfService.queryObject(nxDfgFathersFatherId);
                        Integer greatGrandId = grandEntity.getNxDfgFathersFatherId();
                        Map<String, Object> map = new HashMap<>();
                        map.put("fathersFatherId", greatGrandId);
                        List<NxDistributerFatherGoodsEntity> grandGoodsEntities = dgfService.queryDisFathersGoodsByParams(map);

                        //如果grandFather也是只有一个，则删除greatGrandFather
                        if (grandGoodsEntities.size() == 1) {
                            dgfService.delete(greatGrandId);
                        }
                        dgfService.delete(grandId);
                    }
                    dgfService.delete(delGoods.getNxDgDfgGoodsFatherId());
                } else {
                    //父类商品数量减去1
                    NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = dgfService.queryObject(delGoods.getNxDgDfgGoodsFatherId());
                    Integer nxDfgGoodsAmount = nxDistributerFatherGoodsEntity.getNxDfgGoodsAmount();
                    nxDistributerFatherGoodsEntity.setNxDfgGoodsAmount(nxDfgGoodsAmount - 1);
                    dgfService.update(nxDistributerFatherGoodsEntity);
                }
            }
        }

        NxGoodsEntity delGoodsEntity = nxGoodsService.queryObject(deleteId);
        Integer nxGoodsFatherId = delGoodsEntity.getNxGoodsFatherId();
        if (delGoodsEntity.getNxGoodsIsOldestSon() == 1) {
            int total = nxGoodsService.queryTotalByFatherId(nxGoodsFatherId);
            if (total > 1) {
                return R.error(-1, "不能删除根商品");
            } else {
                NxGoodsEntity father = nxGoodsService.queryObject(nxGoodsFatherId);
                nxGoodsService.delete(father.getNxGoodsId());
            }
        }

        nxGoodsService.delete(deleteId);
        return R.ok();
    }

    @RequestMapping(value = "/getDisLinshiGoods/{disId}")
    @ResponseBody
    public R getDisLinshiGoods(@PathVariable Integer disId) {
        List<NxDistributerGoodsEntity> goodsEntities = dgService.queryLinshiGoods(disId);
//        List<NxDistributerGoodsEntity> goodsEntities = dgService.queryAllLinshiGoods();
        return R.ok().put("data", goodsEntities);
    }

    @RequestMapping(value = "/supplierUpdateDisGoodsWillPrice", method = RequestMethod.POST)
    @ResponseBody
    public R supplierUpdateDisGoodsWillPrice(Integer goodsId, String value, String buyPrice) {
        NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(goodsId);
        nxDistributerGoodsEntity.setNxDgWillPrice(value);
        nxDistributerGoodsEntity.setNxDgBuyingPrice(buyPrice);
        nxDistributerGoodsEntity.setNxDgBuyingPriceUpdate(formatWhatDate(0));
        dgService.update(nxDistributerGoodsEntity);

        //update PurGoods
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", goodsId);
        map.put("status", 2);
        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDisPurchaseGoodsService.queryPurchaseGoodsByParams(map);
        if (purchaseGoodsEntities.size() > 0) {
            for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                purchaseGoodsEntity.setNxDpgBuyPrice(buyPrice);
                if (purchaseGoodsEntity.getNxDpgBuyQuantity() != null) {
                    BigDecimal quantity = new BigDecimal(purchaseGoodsEntity.getNxDpgBuyQuantity());
                    BigDecimal decimal = new BigDecimal(buyPrice).multiply(quantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                    purchaseGoodsEntity.setNxDpgBuySubtotal(decimal.toString());
                }
                nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }

        System.out.println("ordoroeoeoeoeoeooe" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(map);

        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                BigDecimal orderPrice = new BigDecimal(ordersEntity.getNxDoPrice());
                BigDecimal different = new BigDecimal(value).subtract(orderPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal orderWeight = new BigDecimal(ordersEntity.getNxDoWeight());
                BigDecimal newCostPrice = new BigDecimal(buyPrice);
                BigDecimal newPrice = new BigDecimal(value);
                BigDecimal newSubtotal = orderWeight.multiply(newPrice);
                BigDecimal newScale = newPrice.subtract(newCostPrice).divide(newPrice, 2, BigDecimal.ROUND_HALF_UP);
                BigDecimal newCostSubtotal = orderWeight.multiply(newCostPrice);
                BigDecimal newProfit = newSubtotal.subtract(newCostSubtotal);

                ordersEntity.setNxDoPrice(value);
                ordersEntity.setNxDoSubtotal(newSubtotal.toString());
                ordersEntity.setNxDoCostPrice(buyPrice);
                ordersEntity.setNxDoCostSubtotal(newCostSubtotal.toString());
                ordersEntity.setNxDoProfitSubtotal(newProfit.toString());
                ordersEntity.setNxDoProfitScale(newScale.toString());
                ordersEntity.setNxDoPriceDifferent(different.toString());
                depOrdersService.update(ordersEntity);

            }
        }

        return R.ok();
    }

    @RequestMapping(value = "/openGoodsToOrder/{id}")
    @ResponseBody
    public R openGoodsToOrder(@PathVariable Integer id) {
        NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(id);
        distributerGoodsEntity.setNxDgGoodsStatus(1);
        dgService.update(distributerGoodsEntity);
        return R.ok();
    }


//    @RequestMapping(value = "/disUpdateDisGoodsWillPrice", method = RequestMethod.POST)
//    @ResponseBody
//    public R disUpdateDisGoodsWillPrice(Integer goodsId, String buyingPrice,
//                                        String willPrice, String weight,
//                                        Integer level, String profit) {

//        NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(goodsId);
//        if(level == 1){
//            nxDistributerGoodsEntity.setNxDgPriceProfitOne(profit);
//            nxDistributerGoodsEntity.setNxDgBuyingPriceOne(buyingPrice);
//            nxDistributerGoodsEntity.setNxDgWillPriceOne(willPrice);
//            nxDistributerGoodsEntity.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
//            nxDistributerGoodsEntity.setNxDgWillPriceOneWeight(weight);
//        }
//        if(level == 2){
//            nxDistributerGoodsEntity.setNxDgPriceProfitTwo(profit);
//            nxDistributerGoodsEntity.setNxDgBuyingPriceTwo(buyingPrice);
//            nxDistributerGoodsEntity.setNxDgWillPriceTwo(willPrice);
//            nxDistributerGoodsEntity.setNxDgBuyingPriceTwoUpdate(formatWhatDate(0));
//            nxDistributerGoodsEntity.setNxDgWillPriceTwoWeight(weight);
//        }
//        if(level == 3){
//            nxDistributerGoodsEntity.setNxDgPriceProfitThree(profit);
//            nxDistributerGoodsEntity.setNxDgBuyingPriceThree(buyingPrice);
//            nxDistributerGoodsEntity.setNxDgWillPriceThree(willPrice);
//            nxDistributerGoodsEntity.setNxDgBuyingPriceThreeUpdate(formatWhatDate(0));
//            nxDistributerGoodsEntity.setNxDgWillPriceThreeWeight(weight);
//        }
//
//        dgService.update(nxDistributerGoodsEntity);
//        return R.ok().put("data", nxDistributerGoodsEntity);
//    }


    @RequestMapping(value = "/saveNxDisLinshiGoods", method = RequestMethod.POST)
    @ResponseBody
    public R saveNxDisLinshiGoods(@RequestBody NxDistributerGoodsEntity goods) {


        Map<String, Object> mapG = new HashMap<>();
        mapG.put("disId", goods.getNxDgDistributerId());
        mapG.put("searchStr", goods.getNxDgGoodsName());
        mapG.put("standard", goods.getNxDgGoodsStandardname());
        System.out.println("smappgpgpgpg" + mapG);
        List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.queryDisGoodsByName(mapG);
        if(distributerGoodsEntities.size() > 0){
            return R.error(-1,"商品已存在");
        }else{
            String goodsName = goods.getNxDgGoodsName();
            Map<String, Object> map = new HashMap<>();
            map.put("disId", goods.getNxDgDistributerId());
            map.put("nx", -1);
            map.put("goodsLevel", 2);
            List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
            NxDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
            goods.setNxDgDfgGoodsFatherId(fatherGoodsEntity.getNxDistributerFatherGoodsId());
            goods.setNxDgDfgGoodsGrandId(fatherGoodsEntity.getNxDfgFathersFatherId());
            goods.setNxDgPurchaseAuto(-1);
            goods.setNxDgWillPrice("0.1");
            goods.setNxDgBuyingPrice("0.1");
            goods.setNxDgGoodsIsHidden(0);
            goods.setNxDgPurchaseAuto(-1);
            String englishKuohao = getEnglishKuohao(goodsName);
            String pinyin = hanziToPinyin(englishKuohao);
            String headPinyin = getHeadStringByString(englishKuohao, false, null);
            goods.setNxDgGoodsPinyin(pinyin);
            goods.setNxDgGoodsPy(headPinyin);
            dgService.save(goods);

            Integer gbDfgGoodsAmount = fatherGoodsEntity.getNxDfgGoodsAmount();
            fatherGoodsEntity.setNxDfgGoodsAmount(gbDfgGoodsAmount + 1);
            dgfService.update(fatherGoodsEntity);

            return R.ok().put("data", goods);
        }
    }


    @RequestMapping(value = "/saveNxDisGoodsAndDown", method = RequestMethod.POST)
    @ResponseBody
    public R saveNxDisGoodsAndDown(@RequestBody NxDepartmentOrdersEntity ordersEntity) {
        NxDistributerGoodsEntity goods = ordersEntity.getNxDistributerGoodsEntity();

        Map<String, Object> map = new HashMap<>();
        map.put("disId", 1); //固定 nxDisId==1
        map.put("nx", -1);
        map.put("goodsLevel", 2);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
        NxDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
        goods.setNxDgDfgGoodsFatherId(fatherGoodsEntity.getNxDistributerFatherGoodsId());
        goods.setNxDgDfgGoodsGrandId(fatherGoodsEntity.getNxDfgFathersFatherId());
        goods.setNxDgPurchaseAuto(-1);
        dgService.save(goods);

        downDisGoods(goods, ordersEntity);

        Integer gbDfgGoodsAmount = fatherGoodsEntity.getNxDfgGoodsAmount();
        fatherGoodsEntity.setNxDfgGoodsAmount(gbDfgGoodsAmount + 1);
        dgfService.update(fatherGoodsEntity);
        return R.ok().put("data", goods);

    }


    private void downDisGoods(NxDistributerGoodsEntity nxDistributerGoodsEntity, NxDepartmentOrdersEntity ordersEntity) {
        GbDistributerGoodsEntity cgnGoods = new GbDistributerGoodsEntity();
        cgnGoods.setGbDgGoodsName(nxDistributerGoodsEntity.getNxDgGoodsName());
        cgnGoods.setGbDgGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
        cgnGoods.setGbDgGoodsPy(nxDistributerGoodsEntity.getNxDgGoodsPy());
        cgnGoods.setGbDgGoodsPinyin(nxDistributerGoodsEntity.getNxDgGoodsPinyin());
        cgnGoods.setGbDgGoodsStandardWeight(nxDistributerGoodsEntity.getNxDgGoodsStandardWeight());
        cgnGoods.setGbDgGoodsDetail(nxDistributerGoodsEntity.getNxDgGoodsDetail());
        cgnGoods.setGbDgGoodsBrand(nxDistributerGoodsEntity.getNxDgGoodsBrand());
        cgnGoods.setGbDgGoodsPlace(nxDistributerGoodsEntity.getNxDgGoodsPlace());

        cgnGoods.setGbDgDistributerId(ordersEntity.getNxDoGbDistributerId());
        cgnGoods.setGbDgGoodsStatus(0);
        cgnGoods.setGbDgGoodsIsWeight(0);
        cgnGoods.setGbDgNxGoodsId(nxDistributerGoodsEntity.getNxDgNxGoodsId());
        cgnGoods.setGbDgNxFatherId(nxDistributerGoodsEntity.getNxDgNxFatherId());
        cgnGoods.setGbDgNxGrandId(nxDistributerGoodsEntity.getNxDgNxGrandId());
        cgnGoods.setGbDgNxGreatGrandId(nxDistributerGoodsEntity.getNxDgNxGreatGrandId());
        cgnGoods.setGbDgPullOff(0);
        cgnGoods.setGbDgGoodsType(5);
        cgnGoods.setGbDgNxDistributerId(nxDistributerGoodsEntity.getNxDgDistributerId());
        cgnGoods.setGbDgNxDistributerGoodsId(nxDistributerGoodsEntity.getNxDistributerGoodsId());
        cgnGoods.setGbDgGbDepartmentId(ordersEntity.getNxDoGbDepartmentId());
        cgnGoods.setGbDgControlFresh(0);
        cgnGoods.setGbDgControlPrice(0);
        cgnGoods.setGbDgGoodsInventoryType(1);
        cgnGoods.setGbDgIsFranchisePrice(0);
        cgnGoods.setGbDgIsSelfControl(0);
        gbDistributerGoodsService.save(cgnGoods);

        //添加部门商品
        GbDepartmentDisGoodsEntity disGoodsEntity = new GbDepartmentDisGoodsEntity();
        disGoodsEntity.setGbDdgDepGoodsName(cgnGoods.getGbDgGoodsName());
        disGoodsEntity.setGbDdgDisGoodsId(cgnGoods.getGbDistributerGoodsId());
        disGoodsEntity.setGbDdgDisGoodsFatherId(cgnGoods.getGbDgDfgGoodsFatherId());
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
        disGoodsEntity.setGbDdgNxDistributerGoodsId(cgnGoods.getGbDgNxDistributerGoodsId());
        gbDepDisGoodsService.save(disGoodsEntity);


        //添加给门店
        //如果是餐饮商品，自动给门店添加部门商品
        Map<String, Object> map = new HashMap<>();
        map.put("disId", cgnGoods.getGbDgDistributerId());
        map.put("type", getGbDepartmentTypeMendian());
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity dep : departmentEntities) {
                GbDepartmentDisGoodsEntity disGoodsEntityDep = new GbDepartmentDisGoodsEntity();
                disGoodsEntityDep.setGbDdgDepGoodsName(cgnGoods.getGbDgGoodsName());
                disGoodsEntityDep.setGbDdgDisGoodsId(cgnGoods.getGbDistributerGoodsId());
                disGoodsEntityDep.setGbDdgDisGoodsFatherId(cgnGoods.getGbDgDfgGoodsFatherId());
                disGoodsEntityDep.setGbDdgDepGoodsPinyin(cgnGoods.getGbDgGoodsPinyin());
                disGoodsEntityDep.setGbDdgDepGoodsPy(cgnGoods.getGbDgGoodsPy());
                disGoodsEntityDep.setGbDdgDepGoodsStandardname(cgnGoods.getGbDgGoodsStandardname());
                disGoodsEntityDep.setGbDdgDepartmentId(dep.getGbDepartmentId());
                disGoodsEntityDep.setGbDdgDepartmentFatherId(dep.getGbDepartmentId());
                disGoodsEntityDep.setGbDdgGbDepartmentId(cgnGoods.getGbDgGbDepartmentId());
                disGoodsEntityDep.setGbDdgGbDisId(cgnGoods.getGbDgDistributerId());
                disGoodsEntityDep.setGbDdgGoodsType(cgnGoods.getGbDgGoodsType());
                disGoodsEntityDep.setGbDdgStockTotalWeight("0.0");
                disGoodsEntityDep.setGbDdgStockTotalSubtotal("0.0");
                disGoodsEntityDep.setGbDdgShowStandardId(-1);
                disGoodsEntityDep.setGbDdgShowStandardName(cgnGoods.getGbDgGoodsStandardname());
                disGoodsEntityDep.setGbDdgShowStandardScale("-1");
                disGoodsEntityDep.setGbDdgShowStandardWeight(null);
                disGoodsEntityDep.setGbDdgNxDistributerGoodsId(cgnGoods.getGbDgNxDistributerGoodsId());
                gbDepDisGoodsService.save(disGoodsEntityDep);
            }
        }
    }


    private GbDistributerGoodsEntity saveDisGoodsForNx(GbDistributerGoodsEntity cgnGoods) {

        System.out.println("saeeeforNxxxnnxnxnxnxnxnnxn");

        Integer gbDgDistributerId = cgnGoods.getGbDgDistributerId();
        Integer gbDgNxGrandId = cgnGoods.getGbDgNxGrandId(); //nxGrand 是gb的father 101

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
        GbDistributerFatherGoodsEntity greatGrand = gbDgfService.queryAppFatherGoods(map);
        cgnGoods.setGbDgNxGreatGrandId(greatGrand.getGbDistributerFatherGoodsId());
        cgnGoods.setGbDgNxGreatGrandName(greatGrand.getGbDfgFatherGoodsName());

        // 3， 查询父类
        Map<String, Object> map11 = new HashMap<>();
        map11.put("nxGoodsId", fatherEntity.getNxGoodsId());
        map11.put("disId", gbDgDistributerId);
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities1 = gbDgfService.queryDisFathersGoodsByParamsGb(map11);

        if (fatherGoodsEntities1.size() > 0) {
            //直接加disGoods和disStandard,不需要加disFatherGoods
            //1，给父类商品的字段商品数量加1
            GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities1.get(0);
            Integer nxDfgGoodsAmount = fatherGoodsEntity.getGbDfgGoodsAmount();
            fatherGoodsEntity.setGbDfgGoodsAmount(nxDfgGoodsAmount + 1);
            gbDgfService.update(fatherGoodsEntity);

            //2，保存disId商品
            cgnGoods.setGbDgDfgGoodsFatherId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
            //1 ，先保存disGoods
            gbDistributerGoodsService.save(cgnGoods);
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
            gbDgfService.save(dgf);
            //更新disGoods的fatherGoodsId
            Integer distributerFatherGoodsId = dgf.getGbDistributerFatherGoodsId();
            cgnGoods.setGbDgDfgGoodsFatherId(distributerFatherGoodsId);
            gbDistributerGoodsService.save(cgnGoods);
            //继续查询是否有GrandFather
            String fatherName = cgnGoods.getGbDgNxGrandName();
            Map<String, Object> map2 = new HashMap<>();
            map2.put("disId", gbDgDistributerId);
            map2.put("fathersFatherName", fatherName);
            map2.put("goodsLevel", 1);
            List<GbDistributerFatherGoodsEntity> grandGoodsFather = gbDgfService.queryHasDisFathersFather(map2);
            if (grandGoodsFather.size() > 0) {
                GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity = grandGoodsFather.get(0);
                dgf.setGbDfgFathersFatherId(gbDistributerFatherGoodsEntity.getGbDistributerFatherGoodsId());
                Map<String, Object> mapFather = new HashMap<>();
                mapFather.put("fathersFatherId", gbDistributerFatherGoodsEntity.getGbDistributerFatherGoodsId());
                List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDgfService.queryDisFathersGoodsByParamsGb(mapFather);
                gbDgfService.update(dgf);
            } else {
                //tianjiaGrand
                GbDistributerFatherGoodsEntity grand = new GbDistributerFatherGoodsEntity();
                grand.setGbDfgFatherGoodsName(grandEntity.getNxGoodsName());
                grand.setGbDfgDistributerId(gbDgDistributerId);
                grand.setGbDfgFatherGoodsLevel(1);
                grand.setGbDfgFatherGoodsColor(grandEntity.getColor());
                grand.setGbDfgFatherGoodsImg(grandEntity.getNxGoodsFile());
                grand.setGbDfgFatherGoodsSort(grandEntity.getNxGoodsSort());
                grand.setGbDfgNxGoodsId(grandEntity.getNxGoodsId());
                Map<String, Object> mapApp = new HashMap<>();
                mapApp.put("color", "#187e6e");
                mapApp.put("disId", gbDgDistributerId);
                GbDistributerFatherGoodsEntity app = gbDgfService.queryAppFatherGoods(mapApp);
                grand.setGbDfgFathersFatherId(app.getGbDistributerFatherGoodsId());
                gbDgfService.save(grand);
                dgf.setGbDfgFathersFatherId(grand.getGbDistributerFatherGoodsId());
                gbDgfService.update(dgf);

            }
        }


        return cgnGoods;
    }


    @RequestMapping(value = "/delNxDisGoods/{id}")
    @ResponseBody
    public R delNxDisGoods(@PathVariable Integer id) {
        Map<String, Object> map4 = new HashMap<>();
        map4.put("disGoodsId", id);
        List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(map4);
        if (ordersEntities.size() > 0) {
            return R.error(-1, "有客户使用此商品");
        } else {
            int i = dgService.delete(id);
            if (i == 1) {
                return R.ok();
            } else {
                return R.error(-1, "删除失败");
            }
        }
    }


    @RequestMapping(value = "/changeDisGoodsFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R changeDisGoodsFatherId(Integer disGoodsId, Integer fatherId, Integer grandId) {

        //old
        NxDistributerGoodsEntity goodsEntity = dgService.queryObject(disGoodsId);
        Integer oldfgGoodsFatherId1 = goodsEntity.getNxDgDfgGoodsFatherId();
        NxDistributerFatherGoodsEntity fatherGoodsEntity = dgfService.queryObject(oldfgGoodsFatherId1);
        fatherGoodsEntity.setNxDfgGoodsAmount(fatherGoodsEntity.getNxDfgGoodsAmount() - 1);
        dgfService.update(fatherGoodsEntity);

        //new
        NxDistributerFatherGoodsEntity fatherGoodsEntity1 = dgfService.queryObject(fatherId);
        fatherGoodsEntity1.setNxDfgGoodsAmount(fatherGoodsEntity1.getNxDfgGoodsAmount() + 1);
        dgfService.update(fatherGoodsEntity1);

        goodsEntity.setNxDgDfgGoodsFatherId(fatherId);
        goodsEntity.setNxDgDfgGoodsGrandId(grandId);
        dgService.update(goodsEntity);

        return R.ok();
    }


    @RequestMapping(value = "/getGoodsSubNamesByFatherIdNx/{fatherId}")
    @ResponseBody
    public R getGoodsSubNamesByFatherIdNx(@PathVariable Integer fatherId) {


        Map<String, Object> map = new HashMap<>();
        map.put("fathersFatherId", fatherId);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisFathersGoodsByParams(map);

        List<NxDistributerFatherGoodsEntity> newList = new ArrayList<>();

        for (NxDistributerFatherGoodsEntity fatherGoods : fatherGoodsEntities) {
            StringBuilder builder = new StringBuilder();

            Map<String, Object> map1 = new HashMap<>();
            map1.put("dgFatherId", fatherGoods.getNxDistributerFatherGoodsId());
            List<NxDistributerGoodsEntity> goodsEntities = dgService.queryDisGoodsByParams(map1);
            for (NxDistributerGoodsEntity goods : goodsEntities) {
                String nxGoodsName = goods.getNxDgGoodsName();
                builder.append(nxGoodsName);
                builder.append(',');
            }
            fatherGoods.setDgGoodsSubNames(builder.toString());
            newList.add(fatherGoods);
        }

        return R.ok().put("data", newList);
    }


    @RequestMapping(value = "/getNxDistributerGoodsDetail/{disId}")
    @ResponseBody
    public R getNxDistributerGoodsDetail(@PathVariable Integer disId) {

        List<NxDistributerFatherGoodsEntity> greatGrandGoodsEntities = nxDistributerFatherGoodsService.queryDisGoodsCata(disId);
        if (greatGrandGoodsEntities.size() > 0) {
            for (NxDistributerFatherGoodsEntity greatGrandGoodsEntity : greatGrandGoodsEntities) {
                if (greatGrandGoodsEntity.getFatherGoodsEntities().size() > 0) {
                    for (NxDistributerFatherGoodsEntity grandGoodsEntity : greatGrandGoodsEntity.getFatherGoodsEntities()) {
                        System.out.println("grandGoodsEntity==" + grandGoodsEntity.getNxDfgFatherGoodsName());
                        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = grandGoodsEntity.getFatherGoodsEntities();
                        if (fatherGoodsEntities.size() > 0) {
                            for (NxDistributerFatherGoodsEntity fatherGoods : fatherGoodsEntities) {
                                Integer distributerFatherGoodsId = fatherGoods.getNxDistributerFatherGoodsId();

//                                Map<String, Object> map1 = new HashMap<>();
//                                System.out.println("grandGoodsEntity.getNxDistributerFatherGoodsId()" +fatherGoods.getNxDistributerFatherGoodsId());
//                                map.put("dgFatherId", distributerFatherGoodsId);
                                List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.queryDgSubNameByFatherId(distributerFatherGoodsId);
                                fatherGoods.setNxDistributerGoodsEntities(distributerGoodsEntities);
                                System.out.println(fatherGoodsEntities.size() + "sisziiziziz");
                                System.out.println("fatherNameaaaa==" + fatherGoods.getNxDfgFatherGoodsName());
                            }
                        }
                    }
                }
            }

        }

        return R.ok().put("data", greatGrandGoodsEntities);
    }


    @RequestMapping(value = "/getMarketNxGoodsDistributers", method = RequestMethod.POST)
    @ResponseBody
    public R getMarketNxGoodsDistributers(Integer nxGoodsId, String fromLat, String fromLng) {


        List<NxDistributerEntity> distributerEntities = dgService.queryMarketDistributerByNxGoodsId(nxGoodsId);

        if (distributerEntities.size() > 0) {

            //获取出发点坐标
            StringBuilder stringBuilder = new StringBuilder();
            for (NxDistributerEntity distributerEntity : distributerEntities) {
                String nxDistributerLan = distributerEntity.getNxDistributerLan();
                String nxDistributerLun = distributerEntity.getNxDistributerLun();
                String item = nxDistributerLan + "," + nxDistributerLun;
                stringBuilder.append(item + ";");
            }
            String substring = stringBuilder.substring(0, stringBuilder.length() - 1);
            String from = fromLat + "," + fromLng;
            String urlString = "http://apis.map.qq.com/ws/distance/v1/optimal_order?mode=driving&from="
                    + from + "&to=" + substring + "&key=" + KEY;
            // 发送请求，返回Json字符串
            String result = "";
            try {
                URL url = new URL(urlString);
                System.out.println(url);
                System.out.println("----");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                // 腾讯地图使用GET
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String line;
                // 获取地址解析结果
                System.out.println(in);
                while ((line = in.readLine()) != null) {
                    result += line + "\n";
                }
                in.close();
            } catch (Exception e) {
                e.getMessage();
            }


//		// 转JSON格式
            JSONObject jsonObject = JSONObject.parseObject(result);
            System.out.println(result);
            System.out.println("resutltltltltl");
            String optimal_order = (String) jsonObject.getString("result");

            //获取排序
            String order = JSONObject.parseObject(optimal_order).getString("optimal_order");

            System.out.println(order + "  ====thisisiorder");
            String substring3 = order.substring(1, order.length() - 1);
            System.out.println(substring3 + "  ==stustring33333");

            String[] split = substring3.split(",");

            List<NxDistributerEntity> treeSet = new ArrayList<>();

            String elements = JSONObject.parseObject(optimal_order).getString("elements");
            List<NxDistributerEntity> list = JSONObject.parseArray(elements, NxDistributerEntity.class);

            System.out.println(list + "  ====list");

            for (int i = 0; i < split.length; i++) {
                System.out.println(split[i] + "spiidititiiti");
                Integer integer = Integer.valueOf(split[i]);
                System.out.println(integer + "    === inteterrerereggggg");

                NxDistributerEntity nxDistributerEntity = distributerEntities.get(integer - 1);
                NxDistributerEntity listEnitity = list.get(i);

                System.out.println(listEnitity.getDistance() + "distancececicicicici");
                System.out.println(listEnitity.getDuration() + "durationtitonttino");
                String distance = listEnitity.getDistance();
                String duration = listEnitity.getDuration();
                nxDistributerEntity.setDistance(distance);
                nxDistributerEntity.setDuration(duration);
                treeSet.add(nxDistributerEntity);
            }

            return R.ok().put("data", treeSet);
        } else {
            return R.error(-1, "没有订单");
        }
    }


    /**
     * 批发商商品列表
     *
     * @param fatherId 父类id
     * @return 批发商商品列表
     */
    @RequestMapping(value = "/disGetDisTypeGoodsListByFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R disGetDisTypeGoodsListByFatherId(Integer fatherId, Integer type,
                                              Integer limit, Integer page) {
        System.out.println(fatherId + "fatherididiid");

        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("dgFatherId", fatherId);
        map.put("type", type);
        List<NxDistributerGoodsEntity> goodsEntities1;
        if (type.equals(4)) {
//            goodsEntities1 = dgService.queryDisGoodsWithSupplierByParams(map);
            goodsEntities1 = dgService.queryDisGoodsByParams(map);

        } else {
            goodsEntities1 = dgService.queryDisGoodsByParams(map);
        }


        Map<String, Object> map3 = new HashMap<>();
        map3.put("fatherId", fatherId);
        map3.put("type", type);
        System.out.println(map3 + "map3333");
        int total = dgService.queryDisGoodsTotal(map3);
        PageUtils pageUtil = new PageUtils(goodsEntities1, total, limit, page);
        return R.ok().put("page", pageUtil);
    }


    @RequestMapping(value = "/getDgCataGoodsWithSubNames/{disId}")
    @ResponseBody
    public R getDgCataGoodsWithSubNames(@PathVariable Integer disId) {

        List<NxDistributerFatherGoodsEntity> goodsEntities1 = dgfService.queryDisGoodsCata(disId);
        if (goodsEntities1.size() > 0) {
            for (NxDistributerFatherGoodsEntity greatGrandGoods : goodsEntities1) {
                for (NxDistributerFatherGoodsEntity grandGoods : greatGrandGoods.getFatherGoodsEntities()) {
                    for (NxDistributerFatherGoodsEntity fatherGoods : grandGoods.getFatherGoodsEntities()) {
                        StringBuilder builder = new StringBuilder();
                        List<NxDistributerGoodsEntity> goodsEntities = dgService.queryDgSubNameByFatherId(fatherGoods.getNxDistributerFatherGoodsId());
                        for (int i = 0; i < goodsEntities.size(); i++) {
                            String nxGoodsName = goodsEntities.get(i).getNxDgGoodsName();
                            if (i == 0) {
                                builder.append(nxGoodsName);
                                builder.append(",");
                            } else {
                                String lastName = goodsEntities.get(i - 1).getNxDgGoodsName();
                                if (!lastName.equals(nxGoodsName)) {
                                    builder.append(nxGoodsName);
                                    builder.append(",");
                                }
                            }
                        }
                        fatherGoods.setDgGoodsSubNames(builder.toString());
                    }
                }
            }
            return R.ok().put("data", goodsEntities1);
        } else {
            return R.error(-1, "没有商品");
        }
    }


    @RequestMapping(value = "/canclePostDgnGoods", method = RequestMethod.POST)
    @ResponseBody
    public R canclePostDgnGoods(Integer disGoodsId, Integer disGoodsFatherId, Integer disId) {
        //判断此商品下是否有客户

        Map<String, Object> map4 = new HashMap<>();
        map4.put("disGoodsId", disGoodsId);
        List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(map4);
        if (ordersEntities.size() > 0) {
            return R.error(-1, "有客户使用此商品");
        } else {

            Map<String, Object> map1 = new HashMap<>();
            map1.put("disId", disId);
            map1.put("dgFatherId", disGoodsFatherId);
            //搜索fatherId下有几个disGoods
            List<NxDistributerGoodsEntity> totalDisGoods = dgService.queryDisGoodsByParams(map1);
            //如果disGoods的父类只有一个商品
            if (totalDisGoods.size() == 1) {
                //父类Entity
                NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = dgfService.queryObject(disGoodsFatherId);
                //disGoods的grandId
                Integer grandId = nxDistributerFatherGoodsEntity.getNxDfgFathersFatherId();
                Map<String, Object> mapGrand = new HashMap<>();
                mapGrand.put("fathersFatherId", grandId);
                mapGrand.put("disId", disId);
                //搜索grand有几个兄弟
                List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisFathersGoodsByParams(mapGrand);
                if (fatherGoodsEntities.size() == 1) {
                    Integer nxDfgFathersFatherId = fatherGoodsEntities.get(0).getNxDfgFathersFatherId();
                    NxDistributerFatherGoodsEntity grandEntity = dgfService.queryObject(nxDfgFathersFatherId);
                    Integer greatGrandId = grandEntity.getNxDfgFathersFatherId();
                    Map<String, Object> map = new HashMap<>();
                    map.put("disId", disId);
                    map.put("fathersFatherId", greatGrandId);
                    List<NxDistributerFatherGoodsEntity> grandGoodsEntities = dgfService.queryDisFathersGoodsByParams(map);

                    //如果grandFather也是只有一个，则删除greatGrandFather
                    if (grandGoodsEntities.size() == 1) {
                        dgfService.delete(greatGrandId);
                    }
                    dgfService.delete(grandId);
                }
                dgfService.delete(disGoodsFatherId);
            } else {
                //父类商品数量减去1
                NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = dgfService.queryObject(disGoodsFatherId);
                Integer nxDfgGoodsAmount = nxDistributerFatherGoodsEntity.getNxDfgGoodsAmount();
                nxDistributerFatherGoodsEntity.setNxDfgGoodsAmount(nxDfgGoodsAmount - 1);
                dgfService.update(nxDistributerFatherGoodsEntity);
            }

            //删除订货单位
            List<NxDistributerStandardEntity> standardEntities = dsService.queryDisStandardByDisGoodsId(disGoodsId);
            if (standardEntities.size() > 0) {
                for (NxDistributerStandardEntity disStandard : standardEntities) {
                    dsService.delete(disStandard.getNxDistributerStandardId());
                }
            }


            int i = dgService.delete(disGoodsId);

            if (i == 1) {
                return R.ok();
            } else {
                return R.error(-1, "删除失败");
            }
        }
    }


    /**
     * 添加批发商商品
     *
     * @param cgnGoods 批发商商品
     * @return ok
     */
    @RequestMapping(value = "/postDgnGoods", method = RequestMethod.POST)
    @ResponseBody
    public R postDgnGoods(@RequestBody NxDistributerGoodsEntity cgnGoods) {

        //判断是否已经下载
        Integer nxDgNxGoodsId = cgnGoods.getNxDgNxGoodsId();
        Integer nxDgDistributerId1 = cgnGoods.getNxDgDistributerId();
        Map<String, Object> map7 = new HashMap<>();
        map7.put("disId", nxDgDistributerId1);
        map7.put("goodsId", nxDgNxGoodsId);
        List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.queryDisGoodsByParams(map7);

        if (distributerGoodsEntities.size() > 0) {
            return R.error(-1, "已经下载");
        } else {

            NxDistributerGoodsEntity nxDistributerGoodsEntity = saveDisGoods(cgnGoods);

            //2，保存dis规格bieming
            Integer nxCgGoodsId = cgnGoods.getNxDistributerGoodsId();
            //2.1
            List<NxStandardEntity> ncsEntities = cgnGoods.getNxStandardEntities();
            if (ncsEntities.size() > 0) {
                for (NxStandardEntity standard : ncsEntities) {
                    NxDistributerStandardEntity disStandards = new NxDistributerStandardEntity();
                    disStandards.setNxDsDisGoodsId(nxCgGoodsId);
                    disStandards.setNxDsStandardName(standard.getNxStandardName());
                    disStandards.setNxDsStandardError(standard.getNxStandardError());
                    disStandards.setNxDsStandardScale(standard.getNxStandardScale());
                    disStandards.setNxDsStandardFilePath(standard.getNxStandardFilePath());
                    disStandards.setNxDsStandardSort(standard.getNxStandardSort());
                    dsService.save(disStandards);
                }
            }

            //2.2
            List<NxAliasEntity> aliasEntities = cgnGoods.getNxAliasEntities();
            if (aliasEntities.size() > 0) {
                for (NxAliasEntity aliasEntity : aliasEntities) {
                    NxDistributerAliasEntity disAlias = new NxDistributerAliasEntity();
                    disAlias.setNxDaDisGoodsId(nxCgGoodsId);
                    disAlias.setNxDaAliasName(aliasEntity.getNxAliasName());
                    disAliasService.save(disAlias);
                }
            }

            return R.ok().put("data", nxDistributerGoodsEntity);
        }
    }


    private NxDistributerGoodsEntity saveDisGoods(NxDistributerGoodsEntity cgnGoods) {
        Integer nxDgNxGoodsId = cgnGoods.getNxDgNxGoodsId();
        NxGoodsEntity nxGoodsEntity = nxGoodsService.queryObject(nxDgNxGoodsId);
        cgnGoods.setNxDgGoodsName(nxGoodsEntity.getNxGoodsName());
        cgnGoods.setNxDgNxFatherImg(nxGoodsEntity.getNxGoodsFile());
        cgnGoods.setNxDgGoodsStandardname(nxGoodsEntity.getNxGoodsStandardname());
        cgnGoods.setNxDgGoodsDetail(nxGoodsEntity.getNxGoodsDetail());
        cgnGoods.setNxDgGoodsPlace(nxGoodsEntity.getNxGoodsPlace());
        cgnGoods.setNxDgGoodsBrand(nxGoodsEntity.getNxGoodsBrand());
        cgnGoods.setNxDgGoodsStandardWeight(nxGoodsEntity.getNxGoodsStandardWeight());
        cgnGoods.setNxDgGoodsPinyin(nxGoodsEntity.getNxGoodsPinyin());
        cgnGoods.setNxDgGoodsPy(nxGoodsEntity.getNxGoodsPy());
        cgnGoods.setNxDgPullOff(0);
        cgnGoods.setNxDgGoodsStatus(0);
        cgnGoods.setNxStandardEntities(nxGoodsEntity.getNxGoodsStandardEntities());
        cgnGoods.setNxDistributerAliasEntities(nxGoodsEntity.getNxDistributerAliasEntities());
        cgnGoods.setNxDgNxFatherId(nxGoodsEntity.getNxGoodsFatherId());
        cgnGoods.setNxDgNxFatherName(nxGoodsEntity.getFatherGoods().getNxGoodsName());
        cgnGoods.setNxDgNxFatherImg(nxGoodsEntity.getFatherGoods().getNxGoodsFile());
        cgnGoods.setNxDgNxGrandName(nxGoodsEntity.getGrandGoods().getNxGoodsName());
        cgnGoods.setNxDgNxGrandId(nxGoodsEntity.getGrandGoods().getNxGoodsId());
        cgnGoods.setNxDgGoodsFile(nxGoodsEntity.getNxGoodsFile());
        cgnGoods.setNxDgGoodsFileLarge(nxGoodsEntity.getNxGoodsFileBig());
        cgnGoods.setNxDgIsOldestSon(nxGoodsEntity.getNxGoodsIsOldestSon());
        cgnGoods.setNxDgGoodsSort(nxGoodsEntity.getNxGoodsSort());
        cgnGoods.setNxDgGoodsSonsSort(nxGoodsEntity.getNxGoodsSonsSort());
        cgnGoods.setNxDgGoodsIsHidden(0);
        cgnGoods.setNxDgBuyingPriceIsGrade(0);
        cgnGoods.setNxDgBuyingPriceUpdate(formatWhatDay(0));
        cgnGoods.setNxDgWillPrice("0.1");
        cgnGoods.setNxDgBuyingPrice("0.1");
        cgnGoods.setNxDgPurchaseAuto(-1);

        //queryGrandFatherId
        NxGoodsEntity fatherEntity = nxGoodsService.queryObject(cgnGoods.getNxDgNxFatherId());
        Integer grandFatherId = fatherEntity.getNxGoodsFatherId();
        cgnGoods.setNxDgNxGrandId(grandFatherId);
        NxGoodsEntity grandEntity = nxGoodsService.queryObject(grandFatherId);
        cgnGoods.setNxDgNxGrandName(grandEntity.getNxGoodsName());


        //queryGreatGrandFatherId
        Integer greatGrandFatherId = grandEntity.getNxGoodsFatherId();
        if (greatGrandFatherId.equals(1)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#20afb8");
        }
        if (greatGrandFatherId.equals(2)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#f5c832");
        }
        if (greatGrandFatherId.equals(3)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#3cc36e");
        }
        if (greatGrandFatherId.equals(4)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#f09628");
        }
        if (greatGrandFatherId.equals(5)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#1ebaee");
        }
        if (greatGrandFatherId.equals(6)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#f05a32");
        }
        if (greatGrandFatherId.equals(7)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#c0a6dd");
        }
        if (greatGrandFatherId.equals(8)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#969696");
        }
        if (greatGrandFatherId.equals(9)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#318666");
        }
        if (greatGrandFatherId.equals(10)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#026bc2");
        }
        if (greatGrandFatherId.equals(11)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#06eb6d");
        }
        if (greatGrandFatherId.equals(12)) {
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
//        NxDistributerGoodsEntity disGoodsEntity = dgService.queryOneGoodsAboutNxGoods(map);
        List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = dgService.queryDisGoodsByParams(map);

        if (nxDistributerGoodsEntities.size() > 0) {
            //直接加disGoods和disStandard,不需要加disFatherGoods
            //1，给父类商品的字段商品数量加1
            NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsEntities.get(0);
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
            dgf.setNxDfgFatherGoodsSort(nxGoodsEntity.getFatherGoods().getNxGoodsSort());
            dgf.setNxDfgNxGoodsId(cgnGoods.getNxDgNxFatherId());
            dgfService.save(dgf);
//            //更新disGoods的fatherGoodsId
//            Integer distributerFatherGoodsId = dgf.getNxDistributerFatherGoodsId();
//            Integer nxDfgFathersFatherId = dgf.getNxDfgFathersFatherId();
//            cgnGoods.setNxDgDfgGoodsFatherId(distributerFatherGoodsId);
//            cgnGoods.setNxDgDfgGoodsGrandId(nxDfgFathersFatherId);
//            dgService.save(cgnGoods);
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
                grand.setNxDfgFatherGoodsSort(nxGoodsEntity.getGrandGoods().getNxGoodsSort());
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
                    greatGrand.setNxDfgFatherGoodsImg(grandEntity.getFatherGoods().getNxGoodsFile());
                    greatGrand.setNxDfgFatherGoodsSort(grandEntity.getFatherGoods().getNxGoodsSort());
                    dgfService.save(greatGrand);
                    grand.setNxDfgFathersFatherId(greatGrand.getNxDistributerFatherGoodsId());
                    dgfService.update(grand);
                }
            }
        }
        return cgnGoods;
    }

    /**
     * 批发商商品列表
     *
     * @param fatherId 父类id
     * @return 批发商商品列表
     */
    @RequestMapping(value = "/disGetDisGoodsListByFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R disGetDisGoodsListByFatherId(Integer fatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("grandId", fatherId);
//        map.put("disId", disId);
        List<NxDistributerGoodsEntity> goodsEntities1 = dgService.queryDisGoodsByParams(map);
        return R.ok().put("data", goodsEntities1);
    }




    /**
     * ibook获取含有批发商信息的商品列表
     *
     * @param limit    每页商品数量
     * @param page     第几页
     * @param fatherId 商品父级id
     * @param disId    批发商id
     * @return ibook商品列表
     */
    @RequestMapping(value = "/disGetIbookGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disGetIbookGoods(Integer limit, Integer page, Integer fatherId, Integer disId) {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("offset", (page - 1) * limit);
        map1.put("limit", limit);
        map1.put("fatherId", fatherId);
        map1.put("isHidden", 0);
        List<NxGoodsEntity> nxGoodsEntities1 = nxGoodsService.queryNxGoodsByParams(map1);

        List<NxGoodsEntity> goodsEntities = new ArrayList<>();

        for (NxGoodsEntity goods : nxGoodsEntities1) {
            Map<String, Object> map = new HashMap<>();
            map.put("disId", disId);
            map.put("nxGoodsId", goods.getNxGoodsId());
            NxDistributerGoodsEntity dgGoods = dgService.queryOneGoodsAboutNxGoods(map);

            if (dgGoods != null) {
                goods.setIsDownload(1);
                goods.setNxDistributerGoodsEntity(dgGoods);
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

    @RequestMapping(value = "/getDisGoodsOrdersHistory", method = RequestMethod.POST)
    @ResponseBody
    public R getDisGoodsOrdersHistory(Integer disGoodsId, String startDate, String stopDate) {
        System.out.println("abcck");

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }
        List<Map<String, Object>> orderList = new ArrayList<>();

        List<Map<String, Object>> list = new ArrayList<>();
        if (howManyDaysInPeriod > 0) {
            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }

                double total = 0;
                Map<String, Object> mapDisGoods = new HashMap<>();
                mapDisGoods.put("disGoodsId", disGoodsId);
                mapDisGoods.put("arriveDate", whichDay);
                System.out.println("abdbdbfaf" + mapDisGoods);
                List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryOrdersForDisGoods(mapDisGoods);

                if (ordersEntities.size() > 0) {
                    total = depOrdersService.queryDisGoodsOrderWeightTotal(mapDisGoods);
                    Map<String, Object> mapthree = new HashMap<>();
                    mapthree.put("date", whichDay);
                    mapthree.put("order", ordersEntities);
                    mapthree.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
                    orderList.add(mapthree);
                }
            }
        }
        return R.ok().put("data", orderList);
    }


    /**
     * 批发商商品详细
     *
     * @param disGoodsId 批发商商品id
     * @return 含有客户的商品
     */
    @RequestMapping(value = "/disGetGoodsDetail/{disGoodsId}")
    @ResponseBody
    public R disGetGoodsDetail(@PathVariable Integer disGoodsId) {

        //商品信息
        NxDistributerGoodsEntity disGoods = dgService.queryDisGoodsDetail(disGoodsId);

        //3ri订单
        List<Map<String, Object>> orderList = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disGoodsId", disGoodsId);
        map1.put("arriveDate", formatWhatDay(0));

        List<NxDepartmentOrdersEntity> departmentOrdersEntities = depOrdersService.queryOrdersForDisGoods(map1);
        map1.put("hasWeight", 1);
        Integer integer = depOrdersService.queryDepOrdersAcount(map1);
        double weightTotal = 0.0;
        if (integer > 0) {
            weightTotal = depOrdersService.queryDisGoodsOrderWeightTotal(map1);
        }
        Map<String, Object> mapone = new HashMap<>();
        mapone.put("date", formatWhatDayString(0));
        mapone.put("order", departmentOrdersEntities);
        mapone.put("total", new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP));
        orderList.add(mapone);
        map1.put("arriveDate", formatWhatDay(-1));
        List<NxDepartmentOrdersEntity> departmentOrdersEntities2 = depOrdersService.queryOrdersForDisGoods(map1);
        Integer integer2 = depOrdersService.queryDepOrdersAcount(map1);
        double weightTotalTwo = 0.0;
        if (integer2 > 0) {
            weightTotalTwo = depOrdersService.queryDisGoodsOrderWeightTotal(map1);
        }

        Map<String, Object> maptwo = new HashMap<>();
        maptwo.put("date", formatWhatDayString(-1));
        maptwo.put("order", departmentOrdersEntities2);
        maptwo.put("total", new BigDecimal(weightTotalTwo).setScale(1, BigDecimal.ROUND_HALF_UP));
        orderList.add(maptwo);
        map1.put("arriveDate", formatWhatDay(-2));
        List<NxDepartmentOrdersEntity> departmentOrdersEntities3 = depOrdersService.queryOrdersForDisGoods(map1);
        double weightTotal3 = 0.0;
        Integer integer3 = depOrdersService.queryDepOrdersAcount(map1);
        if (integer3 > 0) {
            weightTotal3 = depOrdersService.queryDisGoodsOrderWeightTotal(map1);
        }
        Map<String, Object> mapthree = new HashMap<>();
        mapthree.put("date", formatWhatDayString(-2));
        mapthree.put("order", departmentOrdersEntities3);
        mapthree.put("total", new BigDecimal(weightTotal3).setScale(1, BigDecimal.ROUND_HALF_UP));
        orderList.add(mapthree);

        //进货
        Map<String, Object> map2 = new HashMap<>();
        map2.put("disGoodsId", disGoodsId);
//        map2.put("dayuStatus", 1);
        List<NxDistributerPurchaseGoodsEntity> disPurchaseGoods = nxDisPurchaseGoodsService.queryForDisGoods(map2);

        //客户
        List<NxDepartmentEntity> entities = nxDepDisGoodsService.queryDepartmentsByDisGoodsId(disGoodsId);
        List<GbDepartmentEntity> gbEntities = nxDepDisGoodsService.queryGbDepartmentsByDisGoodsId(disGoodsId);
        Map<String, Object> map = new HashMap<>();
        map.put("orderArr", orderList);
        map.put("purchaseArr", disPurchaseGoods);
        map.put("goodsInfo", disGoods);
        map.put("departmentArr", entities);
        map.put("gbDepartmentArr", gbEntities);

        return R.ok().put("data", map);
    }

    @RequestMapping(value = "/queryDisGoodsByQuickSearchWithDepId", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisGoodsByQuickSearchWithDepId(String searchStr, Integer disId, Integer depId) {
        System.out.println(searchStr);
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("depId", depId);
        map.put("searchStr", searchStr);
        List<NxDistributerGoodsEntity> all = new ArrayList<>();

        String pinyinString = searchStr;
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                pinyinString = hanziToPinyin(searchStr);
            }
        }
        map.put("searchPinyin", pinyinString);
        List<NxDistributerGoodsEntity> goodsEntities = dgService.queryDisGoodsQuickSearchStrWithDepOrders(map);
        if (goodsEntities.size() < 100) {
            return R.ok().put("data", goodsEntities);
        } else {
            return R.error(-1, "jixu");
        }

    }


    @RequestMapping(value = "/queryDisGoodsByQuickSearch", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisGoodsByQuickSearch(String searchStr, String disId) {
        System.out.println(searchStr);
        Map<String, Object> map = new HashMap<>();
        String pinyinString = searchStr;

        map.put("disId", disId);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                pinyinString = hanziToPinyin(searchStr);
            }
        }
        map.put("searchStr", searchStr);
        List<NxDistributerGoodsEntity> goodsEntities = dgService.queryDisGoodsQuickSearchStr(map);
        map.put("searchPinyin", pinyinString);
        map.put("searchStr", null);

        List<NxDistributerGoodsEntity> goodsEntitiesPiyin = dgService.queryDisGoodsQuickSearchStr(map);
        goodsEntitiesPiyin.removeAll(goodsEntities);
        List<NxDistributerGoodsEntity> all = new ArrayList<>();
        all.addAll(goodsEntities);
        all.addAll(goodsEntitiesPiyin);
        return R.ok().put("data", all);
    }

    /**
     * @param searchStr 搜索字符串
     * @param disId     批发商id
     * @return 搜索结果
     */
    @RequestMapping(value = "/queryDisGoodsByQuickSearch1", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisGoodsByQuickSearch1(String searchStr, String disId, String depId) {
        System.out.println(searchStr);
        Map<String, Object> map = new HashMap<>();

        map.put("disId", disId);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);

            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map.put("searchPinyin", pinyin);
            } else {
                map.put("searchStr", searchStr);
                map.put("searchPinyin", searchStr);
            }
        }
        System.out.println("mapappaSSSS" + map);
        List<NxDistributerGoodsEntity> goodsEntities = dgService.queryDisGoodsQuickSearchStr(map);

        return R.ok().put("data", goodsEntities);
    }

    @RequestMapping(value = "/queryDisFatherGoodsByQuickSearch", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisFatherGoodsByQuickSearch(String searchStr, String disId, Integer fatherId) {

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
        System.out.println("fafas" + map);
        List<NxDistributerGoodsEntity> goodsEntities = dgService.queryDisGoodsQuickSearchStrByFatherId(map);
        return R.ok().put("data", goodsEntities);
    }

    @RequestMapping(value = "/queryDisGoodsAndNxGoodsByQuickSearch", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisGoodsAndNxGoodsByQuickSearch(String searchStr, String disId, String depId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        String pinyinString = searchStr;
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                pinyinString = hanziToPinyin(searchStr);
            }
        }
        map.put("searchStr", searchStr);
        map.put("searchPinyin", null);
        List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.queryDisGoodsQuickSearchStr(map);
        map.put("searchStr", null);
        map.put("searchPinyin", pinyinString);
        System.out.println("fafnbiibii" + map);
        List<NxDistributerGoodsEntity> distributerGoodsEntities1 = dgService.queryDisGoodsQuickSearchStr(map);
        distributerGoodsEntities1.removeAll(distributerGoodsEntities);
        List<NxDistributerGoodsEntity> all = new ArrayList<>();
        all.addAll(distributerGoodsEntities);
        all.addAll(distributerGoodsEntities1);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("disArr", all);
        if (all.size() > 0) {
            List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.queryQuickSearchNxGoods(map);
            if (nxGoodsEntities.size() > 0) {
                for (NxDistributerGoodsEntity disGoods : all) {
                    if(disGoods.getNxDgNxGoodsId() != null){
                        Integer nxDgNxGoodsId = disGoods.getNxDgNxGoodsId();
                        for (int i = 0; i < nxGoodsEntities.size(); i++) {
                            Integer nxGoodsId = nxGoodsEntities.get(i).getNxGoodsId();
                            if (nxDgNxGoodsId.equals(nxGoodsId)) {
                                nxGoodsEntities.remove(i);
                            }
                        }
                    }

                }
                map3.put("nxArr", nxGoodsEntities);
            } else {
                map3.put("nxArr", new ArrayList<>());
            }
        } else {
            map3.put("disArr", new ArrayList<>());
            List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.queryQuickSearchNxGoods(map);
            if (nxGoodsEntities.size() > 0) {
                map3.put("nxArr", nxGoodsEntities);
            } else {
                map3.put("nxArr", new ArrayList<>());
            }

        }

        return R.ok().put("data", map3);
    }


    /**
     * @param searchStr 搜索字符串
     * @param disId     批发商id
     * @return 搜索结果 queryGbDepartmentGoodsByQuickSearchGb
     */
    @RequestMapping(value = "/queryDepDisGoodsByQuickSearch", method = RequestMethod.POST)
    @ResponseBody
    public R queryDepDisGoodsByQuickSearch(String searchStr, Integer disId, String depId) {

        Map<String, Object> map = new HashMap<>();
        Map<String, Object> map1 = new HashMap<>();
        map.put("disId", disId);
        map1.put("depId", depId);
        map.put("depId", depId);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map1.put("searchStr", searchStr);
                map.put("searchPinyin", pinyin);
                map1.put("searchPinyin", pinyin);
            } else {
                map.put("searchPinyin", searchStr);
                map1.put("searchPinyin", searchStr);
            }
        }

        System.out.println("shaiqntgkkkdkdk" + map);
        List<NxDistributerGoodsEntity> goodsEntities = dgService.queryDisGoodsQuickSearchStrWithDepOrders(map);
        TreeSet<NxDepartmentDisGoodsEntity> disGoodsEntityTreeSet = nxDepartmentDisGoodsService.queryDepDisGoodsQuickSearchStr(map1);
        Map<String, Object> map3 = new HashMap<>();
        if (goodsEntities.size() > 0) {
            map3.put("dis", goodsEntities);
        } else {
            map3.put("dis", new ArrayList<>());
        }

        if (goodsEntities.size() > 0) {
            map3.put("dep", disGoodsEntityTreeSet);
        } else {
            map3.put("dep", new ArrayList<>());
        }
        return R.ok().put("data", map3);
    }


    @ResponseBody
    @RequestMapping("/disGoodsUpdate")
    public R update(@RequestBody NxDistributerGoodsEntity nxDistributerGoods) {
        String pinyin = hanziToPinyin(nxDistributerGoods.getNxDgGoodsName());
        String headPinyin = getHeadStringByString(nxDistributerGoods.getNxDgGoodsName(), false, null);
        nxDistributerGoods.setNxDgGoodsPinyin(pinyin);
        nxDistributerGoods.setNxDgGoodsPy(headPinyin);
        dgService.update(nxDistributerGoods);

        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryLinshiGoods(nxDistributerGoods.getNxDistributerGoodsId());
        if(gbDistributerGoodsEntity != null){
            gbDistributerGoodsEntity.setGbDgGoodsName(nxDistributerGoods.getNxDgGoodsName());
            gbDistributerGoodsEntity.setGbDgGoodsBrand(nxDistributerGoods.getNxDgGoodsBrand());
            gbDistributerGoodsEntity.setGbDgGoodsStandardname(nxDistributerGoods.getNxDgGoodsStandardname());
            gbDistributerGoodsEntity.setGbDgGoodsPy(nxDistributerGoods.getNxDgGoodsPy());
            gbDistributerGoodsEntity.setGbDgGoodsPinyin(nxDistributerGoods.getNxDgGoodsPinyin());
            gbDistributerGoodsEntity.setGbDgGoodsDetail(nxDistributerGoods.getNxDgGoodsDetail());
            gbDistributerGoodsEntity.setGbDgGoodsPlace(nxDistributerGoods.getNxDgGoodsPlace());
            gbDistributerGoodsEntity.setGbDgGoodsStandardWeight(nxDistributerGoods.getNxDgGoodsStandardWeight());
            gbDistributerGoodsService.update(gbDistributerGoodsEntity);
            Map<String, Object> map = new HashMap<>();
            map.put("disGoodsId", gbDistributerGoodsEntity.getGbDistributerGoodsId());
            List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepDisGoodsService.queryGbDepDisGoodsByParams(map);
            if(departmentDisGoodsEntities.size() > 0){
                for(GbDepartmentDisGoodsEntity departmentDisGoodsEntity: departmentDisGoodsEntities){
                    departmentDisGoodsEntity.setGbDdgDepGoodsName(nxDistributerGoods.getNxDgGoodsName());
                    departmentDisGoodsEntity.setGbDdgDepGoodsBrand(nxDistributerGoods.getNxDgGoodsBrand());
                    departmentDisGoodsEntity.setGbDdgShowStandardName(nxDistributerGoods.getNxDgGoodsStandardname());
                    departmentDisGoodsEntity.setGbDdgDepGoodsDetail(nxDistributerGoods.getNxDgGoodsDetail());
                    departmentDisGoodsEntity.setGbDdgDepGoodsPlace(nxDistributerGoods.getNxDgGoodsPlace());
                    departmentDisGoodsEntity.setGbDdgShowStandardWeight(nxDistributerGoods.getNxDgGoodsStandardWeight());

                    gbDepDisGoodsService.update(departmentDisGoodsEntity);
                }
            }

        }


        return R.ok().put("data", nxDistributerGoods);
    }


    @RequestMapping(value = "/disSaveApplyGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disSaveApplyGoods(@RequestBody NxDistributerGoodsEntity applyGoods) {

        //保存nxgoods
        NxGoodsEntity nxGoodsEntity = new NxGoodsEntity();
        nxGoodsEntity.setNxGoodsName(applyGoods.getNxDgGoodsName());
        nxGoodsEntity.setNxGoodsDetail(applyGoods.getNxDgGoodsDetail());
        nxGoodsEntity.setNxGoodsBrand(applyGoods.getNxDgGoodsBrand());
        nxGoodsEntity.setNxGoodsPlace(applyGoods.getNxDgGoodsPlace());
        nxGoodsEntity.setNxGoodsStandardname(applyGoods.getNxDgGoodsStandardname());
        nxGoodsEntity.setNxGoodsStandardWeight(applyGoods.getNxDgGoodsStandardWeight());
        String pinyin = hanziToPinyin(applyGoods.getNxDgGoodsName());
        String headPinyin = getHeadStringByString(applyGoods.getNxDgGoodsName(), false, null);
        nxGoodsEntity.setNxGoodsPinyin(pinyin);
        nxGoodsEntity.setNxGoodsPy(headPinyin);
        nxGoodsEntity.setNxGoodsFatherId(-1);
        nxGoodsEntity.setNxGoodsApplyNxDistributerId(applyGoods.getNxDgDistributerId());
        nxGoodsService.save(nxGoodsEntity);
        return R.ok().put("data", nxGoodsEntity.getNxGoodsId());
    }


    @ResponseBody
    @RequestMapping("/disSaveDisGoods")
    public R disSaveDisGoods(@RequestBody NxDistributerGoodsEntity nxDistributerGoods) {

        String goodsName = nxDistributerGoods.getNxDgGoodsName();
        String nxGoodsDetail = nxDistributerGoods.getNxDgGoodsDetail();
        String nxGoodsBrand = nxDistributerGoods.getNxDgGoodsBrand();
        String nxDgGoodsStandardname = nxDistributerGoods.getNxDgGoodsStandardname();
        String nxDgGoodsStandardWeight = nxDistributerGoods.getNxDgGoodsStandardWeight();

        Map<String, Object> map = new HashMap<>();
        map.put("goodsName", goodsName);
        map.put("goodsStandard", nxDgGoodsStandardname);
        map.put("goodsDetail", nxGoodsDetail);
        map.put("goodsBrand", nxGoodsBrand);
        map.put("standardWeight", nxDgGoodsStandardWeight);
        map.put("disId", nxDistributerGoods.getNxDgDistributerId());
        map.put("fatherId", nxDistributerGoods.getNxDgDfgGoodsFatherId());
        List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = dgService.queryIfHasSameDisGoods(map);
        if (nxDistributerGoodsEntities.size() > 1) {

            return R.error(-1, "已有相同商品");

        } else {

            nxDistributerGoods.setNxDgGoodsStatus(0);
            String pinyin = hanziToPinyin(goodsName);
            String headPinyin = getHeadStringByString(goodsName, false, null);
            nxDistributerGoods.setNxDgGoodsPy(headPinyin);
            nxDistributerGoods.setNxDgGoodsPinyin(pinyin);
            nxDistributerGoods.setNxDgNxGoodsId(-1);
            nxDistributerGoods.setNxDgNxFatherId(-1);
            nxDistributerGoods.setNxDgNxGrandId(-1);
            nxDistributerGoods.setNxDgNxGreatGrandId(-1);
            nxDistributerGoods.setNxDgGoodsFile("goodsImage/logo.jpg");
            nxDistributerGoods.setNxDgGoodsFileLarge("goodsImage/logo.jpg");
            nxDistributerGoods.setNxDgIsOldestSon(1);
            nxDistributerGoods.setNxDgGoodsIsHidden(0);
            nxDistributerGoods.setNxDgWillPrice("0.1");
            nxDistributerGoods.setNxDgBuyingPrice("0.1");
            nxDistributerGoods.setNxDgBuyingPriceIsGrade(0);
            dgService.save(nxDistributerGoods);

            Integer nxDgDfgGoodsFatherId = nxDistributerGoods.getNxDgDfgGoodsFatherId();
            NxDistributerFatherGoodsEntity fatherGoodsEntity = nxDistributerFatherGoodsService.queryObject(nxDgDfgGoodsFatherId);
            fatherGoodsEntity.setNxDfgGoodsAmount(fatherGoodsEntity.getNxDfgGoodsAmount() + 1);
            nxDistributerFatherGoodsService.update(fatherGoodsEntity);

            return R.ok().put("data", nxDistributerGoods.getNxDistributerGoodsId());

        }

    }

    @RequestMapping(value = "/getFatherGoods/{id}")
    @ResponseBody
    public R getFatherGoods(@PathVariable Integer id) {
        Map<String, Object> map = new HashMap<>();
        map.put("fathersFatherId", id);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
        return R.ok().put("data", fatherGoodsEntities);
    }

    @RequestMapping(value = "/getGoodsListByFatherIdNx", method = RequestMethod.POST)
    @ResponseBody
    public R getGoodsListByFatherIdNx(Integer fatherId, Integer limit, Integer page) {

        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("dgFatherId", fatherId);
        List<NxDistributerGoodsEntity> goodsEntities1 = dgService.queryDisGoodsByParams(map);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("fatherId", fatherId);
        int total = dgService.queryDisGoodsTotal(map3);
        PageUtils pageUtil = new PageUtils(goodsEntities1, total, limit, page);
        return R.ok().put("page", pageUtil);
    }

    @RequestMapping(value = "/getGoodsSubNamesByFatherId/{fatherId}")
    @ResponseBody
    public R getGoodsSubNamesByFatherId(@PathVariable Integer fatherId) {

        Map<String, Object> map = new HashMap<>();
        map.put("fatherId", fatherId);
        System.out.println("fathehrididiidididi" + map);
        List<NxGoodsEntity> goodsEntities1 = nxGoodsService.queryNxGoodsOrderByGoodsId(map);

        List<NxGoodsEntity> newList = new ArrayList<>();

        for (NxGoodsEntity fatherGoods : goodsEntities1) {
            StringBuilder builder = new StringBuilder();

            List<NxGoodsEntity> goodsEntities = nxGoodsService.querySubNameByFatherId(fatherGoods.getNxGoodsId());

            for (int i = 0; i < goodsEntities.size(); i++) {
                String nxGoodsName = goodsEntities.get(i).getNxGoodsName();
                if (i == 0) {
                    builder.append(nxGoodsName);
                    builder.append(",");
                } else {
                    String lastName = goodsEntities.get(i - 1).getNxGoodsName();
                    if (!lastName.equals(nxGoodsName)) {
                        builder.append(nxGoodsName);
                        builder.append(",");
                    }
                }

            }


            fatherGoods.setNxGoodsSubNames(builder.toString());
            newList.add(fatherGoods);
        }


        return R.ok().put("data", newList);
    }

}


//sisy

