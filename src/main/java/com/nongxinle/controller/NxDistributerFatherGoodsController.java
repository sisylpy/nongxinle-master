package com.nongxinle.controller;

/**
 * @author lpy
 * @date 07-27 17:38
 */

import java.io.File;
import java.util.*;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.Constant;
import com.nongxinle.utils.UploadFile;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.PinYin4jUtils.*;


@RestController
@RequestMapping("api/nxdistributerfathergoods")
public class NxDistributerFatherGoodsController {
    @Autowired
    private NxDistributerFatherGoodsService nxDistributerFatherGoodsService;

    @Autowired
    private NxDistributerGoodsService distributerGoodsService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private NxGoodsService nxGoodsService;
   @Autowired
   private NxDistributerNxDistributerService nxDistributerNxDistributerService;
    @Autowired
    private NxDistributerBlockService nxDistributerBlockService;
    @Autowired
    private NxDistributerService nxDistributerService;

    @RequestMapping(value = "/updateNxFatherGoodsSort", method = RequestMethod.POST)
    @ResponseBody
    public R updateNxFatherGoodsSort(@RequestBody List<NxDistributerFatherGoodsEntity> fatherGoodsEntityList) {

        //nxDisFather
        for (NxDistributerFatherGoodsEntity distributerFatherGoodsEntity : fatherGoodsEntityList) {
            nxDistributerFatherGoodsService.update(distributerFatherGoodsEntity);
        }


        return R.ok();
    }


    @RequestMapping(value = "/getLevelOneGoods/{disId}")
    @ResponseBody
    public R getLevelOneGoods(@PathVariable Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("goodsLevel", 1);
//        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisGoodsCataLinshi(disId);
        System.out.println("abdbbdbdbdd" + fatherGoodsEntities.size());
        return R.ok().put("data", fatherGoodsEntities);
    }


    @RequestMapping(value = "/deleteFatherGoods/{goodsId}")
    @ResponseBody
    public R deleteFatherGoods(@PathVariable Integer goodsId) {
        Map<String, Object> map = new HashMap<>();
        map.put("dgFatherId", goodsId);
        List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = distributerGoodsService.queryDisGoodsByParams(map);
        Map<String, Object> map1 = new HashMap<>();
        map1.put("fathersFatherId", goodsId);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map1);
        if (nxDistributerGoodsEntities.size() > 0 || fatherGoodsEntities.size() > 0) {
            return R.error(-1, "有商品不能删除");
        } else {
            nxDistributerFatherGoodsService.delete(goodsId);
            return R.ok();
        }
    }


    @RequestMapping(value = "/saveFatherGoods", method = RequestMethod.POST)
    @ResponseBody
    public R saveFatherGoods(@RequestBody NxDistributerFatherGoodsEntity fatherGoods) {
        Integer nxDfgDistributerId = fatherGoods.getNxDfgDistributerId();
        Map<String, Object> map5 = new HashMap<>();
        map5.put("disId", nxDfgDistributerId);
        map5.put("goodsLevel", 1);
        map5.put("fathersFatherId", fatherGoods.getNxDfgFathersFatherId());
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map5);
        if (fatherGoodsEntities.size() > 0) {
            fatherGoods.setNxDfgFatherGoodsSort(fatherGoodsEntities.size() + 1);
        } else {
            fatherGoods.setNxDfgFatherGoodsSort(1);
        }
        fatherGoods.setNxDfgNxGoodsId(-1);
        fatherGoods.setNxDfgGoodsAmount(0);
        nxDistributerFatherGoodsService.save(fatherGoods);
        return R.ok();
    }


    @ResponseBody
    @RequestMapping(value = "/saveFatherGoodsNxNew", method = RequestMethod.POST)
    public R saveFatherGoodsNxNew(String goodsName, Integer fatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("fathersFatherId", fatherId);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
        NxDistributerFatherGoodsEntity goodsEntity = new NxDistributerFatherGoodsEntity();
        goodsEntity.setNxDfgFatherGoodsName(goodsName);
        goodsEntity.setNxDfgFathersFatherId(fatherId);
        goodsEntity.setNxDfgDistributerId(disId);
        int sort = nxDistributerFatherGoodsService.queryMaxSortByFatherId(fatherId);
        goodsEntity.setNxDfgFatherGoodsSort(sort + 1);
        goodsEntity.setNxDfgFatherGoodsLevel(2);
        goodsEntity.setNxDfgGoodsAmount(0);
        goodsEntity.setNxDfgFatherGoodsImg("goodsImage/logo.jpg");

        nxDistributerFatherGoodsService.save(goodsEntity);

        Integer nxDgDfgGoodsFatherId = goodsEntity.getNxDfgFathersFatherId();
        NxDistributerFatherGoodsEntity fatherGoodsEntity = nxDistributerFatherGoodsService.queryObject(nxDgDfgGoodsFatherId);
        fatherGoodsEntity.setNxDfgGoodsAmount(fatherGoodsEntity.getNxDfgGoodsAmount() + 1);
        nxDistributerFatherGoodsService.update(fatherGoodsEntity);

        Integer nxDfgNxGoodsId = fatherGoodsEntity.getNxDfgNxGoodsId(); //父级商品对应的 nxGoodsId
        NxGoodsEntity nxGoodsEntity = new NxGoodsEntity();
        nxGoodsEntity.setNxGoodsName(goodsName);
        String pinyin = hanziToPinyin(goodsName);
        String headPinyin = getHeadStringByString(goodsName, false, null);
        String englishKuohao = getEnglishKuohao(goodsName);
        nxGoodsEntity.setNxGoodsName(englishKuohao);
        nxGoodsEntity.setNxGoodsPinyin(pinyin);
        nxGoodsEntity.setNxGoodsPy(headPinyin);
        nxGoodsEntity.setNxGoodsFatherId(nxDfgNxGoodsId);
        nxGoodsEntity.setNxGoodsLevel(2);
        nxGoodsEntity.setNxGoodsFile("goodsImage/logo.jpg");
        nxGoodsEntity.setNxGoodsSort(0);
        nxGoodsEntity.setNxGoodsIsHidden(0);
        nxGoodsEntity.setNxGoodsSonsSort(0);

        int sortg =  nxGoodsService.queryMaxSortByFatherId(nxDfgNxGoodsId);
        nxGoodsEntity.setNxGoodsSort(sortg + 1);
        int i = nxGoodsService.querySecondLevelMaxId();
        nxGoodsEntity.setNxGoodsId(i + 1);
        nxGoodsService.save(nxGoodsEntity);

        goodsEntity.setNxDfgNxGoodsId(nxGoodsEntity.getNxGoodsId());
        nxDistributerFatherGoodsService.update(goodsEntity);

        return R.ok();
    }


    @ResponseBody
    @RequestMapping(value = "/saveFatherGoodsNx", produces = "text/html;charset=UTF-8")
    public R saveFatherGoodsGb(@RequestParam("file") MultipartFile file,
                               @RequestParam("goodsName") String goodsName,
                               @RequestParam("fatherId") Integer fatherId,
                               @RequestParam("disId") Integer disId,
                               @RequestParam("color") String color,
                               HttpSession session) {

        //1,上传图片
        String newUploadName = "goodsImage";
        String headByString = hanziToPinyin(goodsName);

        String realPath = UploadFile.uploadFileName(session, newUploadName, file, headByString);
        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + headByString + ".jpg";
        Map<String, Object> map = new HashMap<>();
        map.put("fathersFatherId", fatherId);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
        NxDistributerFatherGoodsEntity goodsEntity = new NxDistributerFatherGoodsEntity();
        goodsEntity.setNxDfgFatherGoodsImg(filePath);
        goodsEntity.setNxDfgFatherGoodsName(goodsName);
        goodsEntity.setNxDfgFathersFatherId(fatherId);
        goodsEntity.setNxDfgDistributerId(disId);
        goodsEntity.setNxDfgFatherGoodsSort(fatherGoodsEntities.size() + 1);
        goodsEntity.setNxDfgFatherGoodsLevel(3);
        goodsEntity.setNxDfgGoodsAmount(0);
        goodsEntity.setNxDfgNxGoodsId(-1);
        goodsEntity.setNxDfgFatherGoodsColor(color);

        nxDistributerFatherGoodsService.save(goodsEntity);


        return R.ok();
    }


    @RequestMapping(value = "/updateFatherGoods", method = RequestMethod.POST)
    @ResponseBody
    public R updateFatherGoods(@RequestBody NxDistributerFatherGoodsEntity fatherGoods) {
        nxDistributerFatherGoodsService.update(fatherGoods);

        return R.ok();
    }


    @ResponseBody
    @RequestMapping(value = "/updateFatherGoodsFile", produces = "text/html;charset=UTF-8")
    public R updateFatherGoodsGb(@RequestParam("file") MultipartFile file,
                                 @RequestParam("goodsName") String goodsName,
                                 @RequestParam("fatherId") Integer fatherId,
                                 HttpSession session) {
        NxDistributerFatherGoodsEntity gbDisFatherGoodsEntity = nxDistributerFatherGoodsService.queryObject(fatherId);
        NxDistributerFatherGoodsEntity fatherGoodsEntity = nxDistributerFatherGoodsService.queryObject(fatherId);
        String oldPath = fatherGoodsEntity.getNxDfgFatherGoodsImg();
        if (oldPath != null && !oldPath.trim().isEmpty()) {
            String oldAbsolutePath = Constant.EXTERNAL_IMAGE_DIR + oldPath;
            File file1 = new File(oldAbsolutePath);
            if (file1.exists()) {
                file1.delete();
            }
        }
//
        //1,上传图片
        String newUploadName = "goodsImage";
        String headByString = hanziToPinyin(goodsName);

        String realPath = UploadFile.uploadFileName(session, newUploadName, file, headByString);
        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + headByString + ".jpg";
        gbDisFatherGoodsEntity.setNxDfgFatherGoodsImg(filePath);
        gbDisFatherGoodsEntity.setNxDfgFatherGoodsName(goodsName);

        nxDistributerFatherGoodsService.update(gbDisFatherGoodsEntity);
        return R.ok();
    }


    @RequestMapping(value = "/nxDepGetDisFatherGoodsGb", method = RequestMethod.POST)
    @ResponseBody
    public R nxDepGetDisFatherGoodsGb(Integer gbDisId, Integer gbDepId, Integer fatherId, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("gbDisId", gbDisId);
        if(gbDepId != -1){
            map.put("gbDepId", gbDepId); 
        }

        map.put("grandId", fatherId);
        map.put("isHidden", 0);
        map.put("notLinshi", 1);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        System.out.println("mappppGGGGGGGBBBBB" + map);
        List<NxDistributerGoodsEntity> distributerGoodsEntities = distributerGoodsService.queryGbDisDisGrandGoodsByGreatId(map);

        Map<String, Object> mapCount = new HashMap<>();
        mapCount.put("greatGrandId", fatherId);
        mapCount.put("isHidden", 0);
        int total = distributerGoodsService.queryDisGoodsTotal(mapCount);
        PageUtils pageUtil = new PageUtils(distributerGoodsEntities, total, limit, page);

        return R.ok().put("page", pageUtil);
    }



    @RequestMapping(value = "/nxDepGetDisFatherGoods", method = RequestMethod.POST)
    @ResponseBody
    public R nxDepGetDisFatherGoods(Integer depId, Integer fatherId, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("nxDepId", depId);
        map.put("grandId", fatherId);
        map.put("isHidden", 0);
        map.put("notLinshi", 1);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        System.out.println("mapapappappapapa" + map);
        List<NxDistributerGoodsEntity> distributerGoodsEntities = distributerGoodsService.queryNxDepDisGrandGoodsByGreatId(map);

        Map<String, Object> mapCount = new HashMap<>();
        mapCount.put("greatGrandId", fatherId);
        mapCount.put("isHidden", 0);
        map.put("notLinshi", 1);
        System.out.println("mapcountttDepPage" + distributerGoodsEntities.size());
        System.out.println("mapcountttDepPageCount" + mapCount);
        int total = distributerGoodsService.queryDisGoodsTotal(mapCount);
        PageUtils pageUtil = new PageUtils(distributerGoodsEntities, total, limit, page);

        return R.ok().put("page", pageUtil);
    }

    @RequestMapping(value = "/nxDepGetDisFatherGoodsWithPurchaseType", method = RequestMethod.POST)
    @ResponseBody
    public R nxDepGetDisFatherGoodsWithPurchaseType(Integer depId, Integer fatherId, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("nxDepId", depId);
        map.put("grandId", fatherId);
        map.put("isHidden", 0);
        map.put("notLinshi", 1);
        map.put("goodsType", 1); // nxDgPurchaseAuto = 1
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        System.out.println("mapapappappapapa" + map);
        List<NxDistributerGoodsEntity> distributerGoodsEntities = distributerGoodsService.queryNxDepDisGrandGoodsByGreatId(map);

        Map<String, Object> mapCount = new HashMap<>();
        mapCount.put("greatGrandId", fatherId);
        mapCount.put("isHidden", 0);
        mapCount.put("goodsType", 1); // nxDgPurchaseAuto = 1
        mapCount.put("notLinshi", 1);
        System.out.println("mapcountttDepPage" + distributerGoodsEntities.size());
        System.out.println("mapcountttDepPageCount" + mapCount);
        int total = distributerGoodsService.queryDisGoodsTotal(mapCount);
        PageUtils pageUtil = new PageUtils(distributerGoodsEntities, total, limit, page);

        return R.ok().put("page", pageUtil);
    }

    @RequestMapping(value = "/nxDepGetDisFatherGoodsSunHola", method = RequestMethod.POST)
    @ResponseBody
    public R nxDepGetDisFatherGoodsSunHola(Integer depId, Integer fatherId, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("nxDepId", depId);
        map.put("grandId", fatherId);
        map.put("isHidden", 0);
        map.put("notLinshi", 1);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        System.out.println("mapapappappapapa" + map);
        // 首衡项目：使用包含库存批次信息的查询方法
        List<NxDistributerGoodsEntity> distributerGoodsEntities = distributerGoodsService.queryNxDepDisGrandGoodsByGreatIdSunHola(map);

        Map<String, Object> mapCount = new HashMap<>();
        mapCount.put("greatGrandId", fatherId);
        mapCount.put("isHidden", 0);
        map.put("notLinshi", 1);
        System.out.println("mapcountttDepPage" + distributerGoodsEntities.size());
        System.out.println("mapcountttDepPageCount" + mapCount);
        int total = distributerGoodsService.queryDisGoodsTotal(mapCount);
        PageUtils pageUtil = new PageUtils(distributerGoodsEntities, total, limit, page);

        return R.ok().put("page", pageUtil);
    }


//    @RequestMapping(value = "/nxDepGetDisFatherGoodsByGrandIdGb", method = RequestMethod.POST)
//    @ResponseBody
//    public R nxDepGetDisFatherGoodsByGrandIdGb(Integer gbDepId, Integer gbDisId, Integer fatherId) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("gbDisId", gbDisId);
//        map.put("gbDepId", gbDepId);
//        map.put("grandId", fatherId);
//        map.put("isHidden", 0);
//        System.out.println("granidididdiidid" + map);
//        List<NxDistributerGoodsEntity> distributerGoodsEntities = distributerGoodsService.queryNxDepDisGrandGoodsByGreatIdAllGb(map);
//
//        return R.ok().put("data", distributerGoodsEntities);
//    }

    @RequestMapping(value = "/nxDepGetDisFatherGoodsByGrandId", method = RequestMethod.POST)
    @ResponseBody
    public R nxDepGetDisFatherGoodsByGrandId(Integer depId, Integer fatherId, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("nxDepId", depId);
        map.put("grandId", fatherId);
        map.put("isHidden", 0);
        List<NxDistributerGoodsEntity> distributerGoodsEntities = distributerGoodsService.queryNxDepDisGrandGoodsByGreatIdAll(map);

        return R.ok().put("data", distributerGoodsEntities);
    }

    @RequestMapping(value = "/gbDisGetDisCataGoods", method = RequestMethod.POST)
    @ResponseBody
    public R gbDisGetDisCataGoods(Integer nxDisId, Integer gbDisId) {

        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDistributerFatherGoodsService.queryDisGreatGrandList(nxDisId);
        List<NxDistributerFatherGoodsEntity> distributerGoodsEntities = new ArrayList<>();
        if (greatGrandGoods.size() > 0) {
            NxDistributerFatherGoodsEntity greatGrandsEntity = greatGrandGoods.get(0);
            Integer greatGarndGoodsId = greatGrandsEntity.getNxDistributerFatherGoodsId();
            Map<String, Object> map = new HashMap<>();
            map.put("gbDisId", gbDisId);
            map.put("grandId", greatGarndGoodsId);
            map.put("isHidden", 0);
            distributerGoodsEntities = distributerGoodsService.queryNxDisGrandGoodsWithGbGoodsByGreatId(map);
        }

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("cataArr", greatGrandGoods);
        mapR.put("grandArr", distributerGoodsEntities);

        return R.ok().put("data", mapR);

    }


    @RequestMapping(value = "/gbDisGetDisFatherGoods", method = RequestMethod.POST)
    @ResponseBody
    public R gbDisGetDisFatherGoods(Integer gbDisId, Integer fatherId, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("gbDisId", gbDisId);
        map.put("grandId", fatherId);
        map.put("isHidden", 0);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        System.out.println("gbdisgodood" + map);
        List<NxDistributerGoodsEntity> distributerGoodsEntities = distributerGoodsService.queryGbDisGrandGoodsByGreatId(map);

        Map<String, Object> mapCount = new HashMap<>();
        mapCount.put("greatGrandId", fatherId);
        mapCount.put("isHidden", 0);
        int total = distributerGoodsService.queryDisGoodsTotal(mapCount);
        PageUtils pageUtil = new PageUtils(distributerGoodsEntities, total, limit, page);
        return R.ok().put("page", pageUtil);
    }

    @RequestMapping(value = "/gbDisGetDisFatherGoodsByGrandId", method = RequestMethod.POST)
    @ResponseBody
    public R gbDisGetDisFatherGoodsByGrandId(Integer gbDisId, Integer fatherId) {
        Map<String, Object> map = new HashMap<>();
        map.put("gbDisId", gbDisId);
        map.put("fatherId", fatherId);
        map.put("isHidden", 0);
        System.out.println("hererere" + map);
        List<NxDistributerGoodsEntity> distributerGoodsEntities = distributerGoodsService.queryGbDisGrandGoodsByGreatId(map);

        return R.ok().put("data", distributerGoodsEntities);
    }


    @RequestMapping(value = "/getDisGoodsByGreatGrandId", method = RequestMethod.POST)
    @ResponseBody
    public R getDisGoodsByGreatGrandId(Integer fatherId, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("grandId", fatherId);
        map.put("isHidden", 0);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        System.out.println("mapappapapapa" + map);
        List<NxDistributerGoodsEntity> distributerGoodsEntities = distributerGoodsService.querySupplierGoodsByGreatId(map);

        Map<String, Object> mapCount = new HashMap<>();
        mapCount.put("greatGrandId", fatherId);
        mapCount.put("isHidden", 0);
        int total = distributerGoodsService.queryDisGoodsTotal(mapCount);
        PageUtils pageUtil = new PageUtils(distributerGoodsEntities, total, limit, page);

        return R.ok().put("page", pageUtil);
    }

    @RequestMapping(value = "/getDisGoodsByGreatGrandIdWithCount", method = RequestMethod.POST)
    @ResponseBody
    public R getDisGoodsByGreatGrandIdWithCount(Integer fatherId, Integer limit, Integer page, Integer disId, Integer goodsType, Integer hasCartonUnit,Integer hasPerCarton, Integer hasTraceReport) {
        Map<String, Object> map = new HashMap<>();
        List<Integer> ids = new ArrayList<>();

        map.put("grandId", fatherId);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        if(goodsType != 99){
            if(goodsType == 13){
                // 商品分页查询协作商户和自己的商品，构建多个配送商 id 列表
                Map<String, Object> mapGroup = new HashMap<>();
                mapGroup.put("orderDisId", disId);
                List<NxDistributerEntity> nxDistributerEntities = nxDistributerNxDistributerService.queryOfferNxDisByParams(mapGroup);
                if(nxDistributerEntities.size() > 0){
                    Set<Integer> blockedIds = new HashSet<>(nxDistributerBlockService.queryBlockedDisIdsByBlocker(disId));
                    for(NxDistributerEntity nxDistributerEntity: nxDistributerEntities){
                        Integer partnerId = nxDistributerEntity.getNxDistributerId();
                        if (!blockedIds.contains(partnerId)) {
                            ids.add(partnerId);
                        }
                    }
                }
                map.put("disIds", ids);

            }
            if(goodsType == 11){
                map.put("goodsType", 1);
                map.put("hasSupplier", 1);
                map.put("isHidden", 0);
                map.put("disId", disId);
            } else if(goodsType == -2){
                map.put("isHidden", 1);
                map.put("disId", disId);
            }

            //想增加货架商品有重复的查询，怎么让type赋值？

            //想增加有库存的商品有重复的查询，怎么让type赋值？


            else{
                map.put("goodsType", goodsType);
                map.put("disId", disId);
                map.put("isHidden", 0);
            }
        }else{
            map.put("disId", disId);
            map.put("isHidden", 0);
        }

        NxDistributerEntity distributerEntity = nxDistributerService.queryObject(disId);
        if(distributerEntity.getNxDistributerBusinessTypeId() > 5){
            // 查询协作商户和自己的商品，构建多个配送商 id 列表
            Map<String, Object> mapGroup = new HashMap<>();
            mapGroup.put("orderDisId", disId);
            System.out.println("mapggou" + mapGroup);
            List<NxDistributerEntity> nxDistributerEntities = nxDistributerNxDistributerService.queryOfferNxDisByParams(mapGroup);
            if(nxDistributerEntities.size() > 0){
                Set<Integer> blockedIds = new HashSet<>(nxDistributerBlockService.queryBlockedDisIdsByBlocker(disId));
                for(NxDistributerEntity nxDistributerEntity: nxDistributerEntities){
                    Integer partnerId = nxDistributerEntity.getNxDistributerId();
                    if (!blockedIds.contains(partnerId)) {
                        ids.add(partnerId);
                    }
                }
            }
            if (goodsType != 13){
                ids.add(disId);
            }
            map.put("disIds", ids);
        }
        // 添加外包装查询条件：1-有外包装，0-无外包装，null-不筛选
        if(hasCartonUnit != null){
            map.put("hasCartonUnit", hasCartonUnit);
        }
        // 添加外包装查询条件：1-有外包装，0-无外包装，null-不筛选
        if(hasPerCarton != null){
            map.put("hasCartonUnit", 1);
            map.put("hasPerCarton", hasPerCarton);
        }
        // 添加溯源查询条件：1-有溯源，0-无溯源，null-不筛选
        if(hasTraceReport != null){
            map.put("hasTraceReport", hasTraceReport);
        }
        map.put("limit", limit);


        List<NxDistributerGoodsEntity> distributerGoodsEntities = distributerGoodsService.querySupplierGoodsByGreatId(map);

        Map<String, Object> mapCount = new HashMap<>();
        mapCount.put("disId", disId);
        mapCount.put("greatFatherId", fatherId);
        mapCount.put("isHidden", goodsType == -2 ? 1 : 0);
        if(goodsType != 99){
            mapCount.put("goodsType", goodsType);
            if(goodsType == 11){
                mapCount.put("goodsType", 1);
                mapCount.put("hasSupplier", 1);
            }else if(goodsType == 13){
                mapCount.put("disIds", ids);
            }
        }
        // 协作商户时，list 用 disIds，count 也需一致
        if(ids.size() > 0){
            mapCount.put("disIds", ids);
        }
        // 传递外包装查询条件到总数查询
        if(hasCartonUnit != null){
            mapCount.put("hasCartonUnit", hasCartonUnit);
        }
        if(hasPerCarton != null){
            mapCount.put("hasCartonUnit", 1);
            mapCount.put("hasPerCarton", hasPerCarton);
        }
        // 传递溯源查询条件到总数查询
        if(hasTraceReport != null){
            mapCount.put("hasTraceReport", hasTraceReport);
        }
        System.out.println("toototototot" + mapCount);


        int total = distributerGoodsService.queryDisGoodsTotal(mapCount);
        PageUtils pageUtil = new PageUtils(distributerGoodsEntities, total, limit, page);

        Map<String, Object> returnData = new HashMap<>();
        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", disId);
        map111.put("purStatus", 4);
        // 出库
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("goodsType", 1);
        int purCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        returnData.put("stockCount", stockCount);
        returnData.put("purCount", purCount);
        returnData.put("is", pageUtil);

        return R.ok().put("page", returnData);
    }

    @RequestMapping(value = "/getDisGoodsByGrandId", method = RequestMethod.POST)
    @ResponseBody
    public R getDisGoodsByGrandId(Integer fatherId, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("fatherId", fatherId);
//        System.out.println("mapapappappapapa" + map);
//        Map<String, Object> mapF = new HashMap<>();
//        mapF.put("fathersFatherId", fatherId);
//        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(mapF);
//        if(fatherGoodsEntities.size() > 0){
//            for(NxDistributerFatherGoodsEntity fatherGoodsEntity: fatherGoodsEntities){
//                Map<String, Object> mapG = new HashMap<>();
//                mapG.put("dgFatherId", fatherGoodsEntity.getNxDistributerFatherGoodsId());
//                List<NxDistributerGoodsEntity> distributerGoodsEntities = distributerGoodsService.queryDisGoodsByParams(mapG);
//                if(distributerGoodsEntities.size() > 0){
//                    for(NxDistributerGoodsEntity distributerGoodsEntity: distributerGoodsEntities){
//                        System.out.println("goonanmee==aaaaaa" + distributerGoodsEntity.getNxDgGoodsName() + "id="+ distributerGoodsEntity.getNxDistributerGoodsId()
//                                + "yuan" + distributerGoodsEntity.getNxDgGoodsSort()
//                                + "fahthsororor" + fatherGoodsEntity.getNxDfgFatherGoodsSort()
//                        );
//                        distributerGoodsEntity.setNxDgGoodsSort(fatherGoodsEntity.getNxDfgFatherGoodsSort());
//                        distributerGoodsService.update(distributerGoodsEntity);
//                    }
//                }
//
//            }
//
//        }

        List<NxDistributerGoodsEntity> distributerGoodsEntities = distributerGoodsService.querySupplierGoodsByFatherId(map);
        return R.ok().put("data", distributerGoodsEntities);
    }


    @RequestMapping(value = "/nxDepGetDisCataGoodsGb", method = RequestMethod.POST)
    @ResponseBody
    public R nxDepGetDisCataGoodsGb(Integer nxDisId, Integer gbDisId, Integer gbDepFatherId) {

        System.out.println("nenwnwn11111111");

        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDistributerFatherGoodsService.queryDisGreatGrandList(nxDisId);

//        if (greatGrandGoods.size() > 0) {
//            for(int j = 0 ; j < greatGrandGoods.size(); j++) {
//                NxDistributerFatherGoodsEntity fatherGoodsEntity = greatGrandGoods.get(j);
//
//                Map<String, Object> mapG = new HashMap<>();
//                mapG.put("gbDisId", gbDisId);
//                mapG.put("gbDepFatherId", gbDepFatherId);
//                mapG.put("status", 3);
//                mapG.put("dayuStatus", -1);
//                mapG.put("greatGrandId", fatherGoodsEntity.getNxDistributerFatherGoodsId());
//                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(mapG);
//                fatherGoodsEntity.setNewOrderCount(integer);
//
//                List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = fatherGoodsEntity.getFatherGoodsEntities();
//                if (fatherGoodsEntities.size() > 0) {
//
//                    for(int i = 0; i < fatherGoodsEntities.size(); i++){
//                        NxDistributerFatherGoodsEntity secondFatherEntity = fatherGoodsEntities.get(i);
//                        mapG.put("grandId", secondFatherEntity.getNxDistributerFatherGoodsId());
//                        Integer integer2 = nxDepartmentOrdersService.queryDepOrdersAcount(mapG);
//                        secondFatherEntity.setNewOrderCount(integer2);
//
//                    }
//
//                }
//
//
//            }
//
//        }

        return R.ok().put("data", greatGrandGoods);

    }



    /**
     * sxll
     *
     * @param depId 现金部门 id
     * @return ok
     */
    @RequestMapping(value = "/nxDepGetDisCataGoods", method = RequestMethod.POST)
    @ResponseBody
    public R nxDepGetDisCataGoods(Integer nxDisId, Integer depId) {

        System.out.println("nenwnwn11111111");
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDistributerFatherGoodsService.queryDisGreatGrandList(nxDisId);

        NxDistributerFatherGoodsEntity fatherGoodsEntity = greatGrandGoods.get(0);
        List<Integer> ids = new ArrayList<>();
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = fatherGoodsEntity.getFatherGoodsEntities();
        if(fatherGoodsEntities.size() > 0){
            for(NxDistributerFatherGoodsEntity grandEntity : fatherGoodsEntities){
                ids.add(grandEntity.getNxDistributerFatherGoodsId());
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("disId", nxDisId);
        map.put("notLinshi", 1);
        map.put("isHidden", 0);
        map.put("grandIds", ids);
        System.out.println("mappaisaiapapapa" + map);
        List<Integer > departmentDisGoodsEntities =   distributerGoodsService.queryOnlyDepGoodsIds(map);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("cataArr",greatGrandGoods);
        mapR.put("depGoodsArr", departmentDisGoodsEntities);

        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/nxDepGetDisCataGoodsWithPurchaseType", method = RequestMethod.POST)
    @ResponseBody
    public R nxDepGetDisCataGoodsWithPurchaseType(Integer nxDisId, Integer depId) {

        System.out.println("nenwnwn11111111");
        // 使用带类型过滤的查询方法，只返回有 nxDgPurchaseAuto = 1 商品的分类
        Map<String, Object> mapG = new HashMap<>();
        mapG.put("disId", nxDisId);
        mapG.put("goodsType", 1); // nxDgPurchaseAuto = 1
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDistributerFatherGoodsService.queryDisGreatGrandListWithType(mapG);

        List<Integer> ids = new ArrayList<>();
        if(greatGrandGoods != null && greatGrandGoods.size() > 0){
            NxDistributerFatherGoodsEntity fatherGoodsEntity = greatGrandGoods.get(0);
            List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = fatherGoodsEntity.getFatherGoodsEntities();
            if(fatherGoodsEntities != null && fatherGoodsEntities.size() > 0){
                for(NxDistributerFatherGoodsEntity grandEntity : fatherGoodsEntities){
                    ids.add(grandEntity.getNxDistributerFatherGoodsId());
                }
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("disId", nxDisId);
        map.put("notLinshi", 1);
        map.put("isHidden", 0);
        map.put("grandIds", ids);
        System.out.println("mappaisaiapapapa" + map);
        List<Integer > departmentDisGoodsEntities =   distributerGoodsService.queryOnlyDepGoodsIdsWithPurchaseType(map);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("cataArr",greatGrandGoods);
        mapR.put("depGoodsArr", departmentDisGoodsEntities);

        return R.ok().put("data", mapR);

    }


    @RequestMapping(value = "/getNxDisGoodsIdsByGreatId/{id}")
    @ResponseBody
    public R getNxDisGoodsIdsByGreatId(@PathVariable Integer id) {


        List<NxDistributerFatherGoodsEntity>  fatherGoodsEntities = nxDistributerFatherGoodsService.queryListByFatherId(id);
        List<Integer> ids = new ArrayList<>();
        if(fatherGoodsEntities.size() > 0){
            for(NxDistributerFatherGoodsEntity grandEntity : fatherGoodsEntities){
                ids.add(grandEntity.getNxDistributerFatherGoodsId());
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("notLinshi", 1);
        map.put("isHidden", 0);
        map.put("grandIds", ids);
        List<Integer > departmentDisGoodsEntities =   distributerGoodsService.queryOnlyDepGoodsIds(map);

        return R.ok().put("data",departmentDisGoodsEntities);
    }

    @RequestMapping(value = "/getNxDisGoodsIdsByGreatIdWithPurchaseType/{id}")
    @ResponseBody
    public R getNxDisGoodsIdsByGreatIdWithPurchaseType(@PathVariable Integer id) {


        List<NxDistributerFatherGoodsEntity>  fatherGoodsEntities = nxDistributerFatherGoodsService.queryListByFatherId(id);
        List<Integer> ids = new ArrayList<>();
        if(fatherGoodsEntities.size() > 0){
            for(NxDistributerFatherGoodsEntity grandEntity : fatherGoodsEntities){
                ids.add(grandEntity.getNxDistributerFatherGoodsId());
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("notLinshi", 1);
        map.put("isHidden", 0);
        map.put("grandIds", ids);
        List<Integer > departmentDisGoodsEntities =   distributerGoodsService.queryOnlyDepGoodsIdsWithPurchaseType(map);

        return R.ok().put("data",departmentDisGoodsEntities);
    }

    /**
     * @param disId 批发商id
     * @return 批发商父类列表
     */
    @RequestMapping(value = "/getDisGoodsCata/{disId}")
    @ResponseBody
    public R getDisGoodsCata(@PathVariable Integer disId) {

        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDistributerFatherGoodsService.queryDisGreatGrandList(disId);


        return R.ok().put("data", greatGrandGoods);

    }



    @RequestMapping(value = "/getDisGoodsCataWithCount")
    @ResponseBody
    public R getDisGoodsCataWithCount(Integer disId, Integer goodsType, Integer hasCartonUnit, Integer hasPerCarton, Integer hasTraceReport) {

        Map<String, Object> returnData = new HashMap<>();

        List<Integer> ids = new ArrayList<>();

        Map<String, Object> mapG = new HashMap<>();

        if(goodsType != 99){

            if(goodsType == 13){
                // 查询协作商户和自己的商品，构建多个配送商 id 列表
                Map<String, Object> mapGroup = new HashMap<>();
                mapGroup.put("orderDisId", disId);
                System.out.println("mapggou" + mapGroup);
                List<NxDistributerEntity> nxDistributerEntities = nxDistributerNxDistributerService.queryOfferNxDisByParams(mapGroup);
                if(nxDistributerEntities.size() > 0){
                    Set<Integer> blockedIds = new HashSet<>(nxDistributerBlockService.queryBlockedDisIdsByBlocker(disId));
                    for(NxDistributerEntity nxDistributerEntity: nxDistributerEntities){
                        Integer partnerId = nxDistributerEntity.getNxDistributerId();
                        if (!blockedIds.contains(partnerId)) {
                            ids.add(partnerId);
                        }                    }
                }
                mapG.put("disIds", ids);
            }
            if(goodsType == 11){
                mapG.put("goodsType", 1);
                mapG.put("hasSupplier", 1);
                mapG.put("isHidden", 0);
                mapG.put("disId", disId);
            }
            else if(goodsType == -2){
                mapG.put("isHidden", 1);
                mapG.put("disId", disId);
            }

            else{
                mapG.put("goodsType", goodsType);
                mapG.put("disId", disId);
                mapG.put("isHidden", 0);
            }
        }else{
            mapG.put("disId", disId);
            mapG.put("isHidden", 0);
        }
        NxDistributerEntity distributerEntity = nxDistributerService.queryObject(disId);
        if(distributerEntity.getNxDistributerBusinessTypeId() > 5){
            // 查询协作商户和自己的商品，构建多个配送商 id 列表
            Map<String, Object> mapGroup = new HashMap<>();
            mapGroup.put("orderDisId", disId);
            System.out.println("mapggou" + mapGroup);
            List<NxDistributerEntity> nxDistributerEntities = nxDistributerNxDistributerService.queryOfferNxDisByParams(mapGroup);
            if(nxDistributerEntities.size() > 0){
                Set<Integer> blockedIds = new HashSet<>(nxDistributerBlockService.queryBlockedDisIdsByBlocker(disId));
                for(NxDistributerEntity nxDistributerEntity: nxDistributerEntities){
                    Integer partnerId = nxDistributerEntity.getNxDistributerId();
                    if (!blockedIds.contains(partnerId)) {
                        ids.add(partnerId);
                    }
                }
            }
            if (goodsType != 13){
                ids.add(disId);
            }
            mapG.put("disIds", ids);

        }

        // 添加外包装查询条件：1-有外包装，0-无外包装，null-不筛选
        if(hasCartonUnit != null){
            mapG.put("hasCartonUnit", hasCartonUnit);
        }
        // 添加外包装查询条件：1-有外包装，0-无外包装，null-不筛选
        if(hasPerCarton != null){
            mapG.put("hasCartonUnit", 1);
            mapG.put("hasPerCarton", hasPerCarton);
        }
        // 添加溯源查询条件：1-有溯源，0-无溯源，null-不筛选
        if(hasTraceReport != null){
            mapG.put("hasTraceReport", hasTraceReport);
        }


        // 总数查询也需要传递外包装条件、溯源条件和多个配送商 id
        int count =  distributerGoodsService.queryDisGoodsTotal(mapG);
        if(count > 0){
            List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDistributerFatherGoodsService.queryDisGreatGrandListWithType(mapG);
            if(greatGrandGoods.size() > 0){
                for(NxDistributerFatherGoodsEntity greatGrand : greatGrandGoods){
                    List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = greatGrand.getFatherGoodsEntities();
                    if(fatherGoodsEntities != null && fatherGoodsEntities.size() > 0){
                        for(NxDistributerFatherGoodsEntity fatherGoodsEntity: fatherGoodsEntities){
                            fatherGoodsEntity.setNxDistributerGoodsEntities(null);
                        }
                    }
                }
            }
            returnData.put("list", greatGrandGoods);
        }else{
            returnData.put("list", new ArrayList<>());
        }
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("isLinshi", 1);
        int wxCountAuto1 = distributerGoodsService.queryDisGoodsTotal(map);

        Map<String, Object> mapPrice = new HashMap<>();
        mapPrice.put("disId", disId);
        mapPrice.put("willPrice", 0.1);
        int  priceCount = distributerGoodsService.queryDisGoodsTotal(mapPrice);

        Map<String, Object> mapBuyPrice = new HashMap<>();
        mapBuyPrice.put("disId", disId);
        mapBuyPrice.put("buyPrice", 0.1);
        int  buyPriceCount = distributerGoodsService.queryDisGoodsTotal(mapBuyPrice);

        
        returnData.put("lishiCount", wxCountAuto1);
        returnData.put("priceCount", priceCount);
        returnData.put("buyPriceCount", buyPriceCount);

        return R.ok().put("data", returnData);

    }


}


//  @RequestMapping(value = "/gbDisGetDisCataGoods", method = RequestMethod.POST)
//    @ResponseBody
//    public R gbDisGetDisCataGoods(Integer nxDisId, Integer gbDisId) {
//
//        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDistributerFatherGoodsService.queryDisGoodsCata(nxDisId);
//        List<NxDistributerGoodsEntity> distributerGoodsEntities = new ArrayList<>();
//        if (greatGrandGoods.size() > 0) {
//
//            NxDistributerFatherGoodsEntity greatGrandsEntity = greatGrandGoods.get(0);
////            System.out.println("oooooooo" + greatGrandGoods.get(1));
//            Integer greatGarndGoodsId = greatGrandsEntity.getNxDistributerFatherGoodsId();
//            Map<String, Object> map = new HashMap<>();
//            map.put("gbDisId", gbDisId);
//            map.put("grandId", greatGarndGoodsId);
//            map.put("offset", 0);
//            map.put("limit", 15);
//            System.out.println("xinhehehehhe" + map);
//            distributerGoodsEntities = distributerGoodsService.queryNxDisGrandGoodsWithGbGoodsByGreatId(map);
//        }
//
//        Map<String, Object> mapR = new HashMap<>();
//        mapR.put("cataArr", greatGrandGoods);
//        mapR.put("grandArr", distributerGoodsEntities);
//
//        return R.ok().put("data", mapR);
//
//    }