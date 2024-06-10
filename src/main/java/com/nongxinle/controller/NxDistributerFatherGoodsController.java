package com.nongxinle.controller;

/**
 * @author lpy
 * @date 07-27 17:38
 */

import java.io.File;
import java.util.*;

import com.nongxinle.entity.*;
import com.nongxinle.service.NxDepartmentOrdersService;
import com.nongxinle.service.NxDistributerGoodsService;
import com.nongxinle.service.NxGoodsService;
import com.nongxinle.utils.UploadFile;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.NxDistributerFatherGoodsService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.PinYin4jUtils.hanziToPinyin;


@RestController
@RequestMapping("api/nxdistributerfathergoods")
public class NxDistributerFatherGoodsController {
    @Autowired
    private NxDistributerFatherGoodsService nxDistributerFatherGoodsService;

    @Autowired
    private NxDistributerGoodsService distributerGoodsService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;


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
        map.put("goodsLevel", 0);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
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
        goodsEntity.setNxDfgFatherGoodsSort(fatherGoodsEntities.size() + 1);
        goodsEntity.setNxDfgFatherGoodsLevel(3);
        goodsEntity.setNxDfgGoodsAmount(0);
        goodsEntity.setNxDfgNxGoodsId(-1);

        nxDistributerFatherGoodsService.save(goodsEntity);

        Integer nxDgDfgGoodsFatherId = goodsEntity.getNxDfgFathersFatherId();
        NxDistributerFatherGoodsEntity fatherGoodsEntity = nxDistributerFatherGoodsService.queryObject(nxDgDfgGoodsFatherId);
        fatherGoodsEntity.setNxDfgGoodsAmount(fatherGoodsEntity.getNxDfgGoodsAmount() + 1);
        nxDistributerFatherGoodsService.update(fatherGoodsEntity);


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
        String gbDistributerFoodImg = fatherGoodsEntity.getNxDfgFatherGoodsImg();
        ServletContext servletContext = session.getServletContext();
        String realPath1 = servletContext.getRealPath(gbDistributerFoodImg);
        File file1 = new File(realPath1);
        if (file1.exists()) {
            file1.delete();
        }


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


    @RequestMapping(value = "/nxDepGetDisFatherGoods", method = RequestMethod.POST)
    @ResponseBody
    public R nxDepGetDisFatherGoods(Integer depId, Integer fatherId, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("nxDepId", depId);
        map.put("grandId", fatherId);
        map.put("isHidden", 0);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        System.out.println("mapapappappapapa" + map);
        List<NxDistributerGoodsEntity> distributerGoodsEntities = distributerGoodsService.queryNxDepDisGrandGoodsByGreatId(map);

        Map<String, Object> mapCount = new HashMap<>();
        mapCount.put("greatGrandId", fatherId);
        mapCount.put("isHidden", 0);
        int total = distributerGoodsService.queryDisGoodsTotal(mapCount);
        PageUtils pageUtil = new PageUtils(distributerGoodsEntities, total, limit, page);

        return R.ok().put("page", pageUtil);
    }

    @RequestMapping(value = "/nxDepGetDisFatherGoodsByGrandId", method = RequestMethod.POST)
    @ResponseBody
    public R nxDepGetDisFatherGoodsByGrandId(Integer depId, Integer fatherId) {
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

    @RequestMapping(value = "/getDisGoodsByGrandId", method = RequestMethod.POST)
    @ResponseBody
    public R getDisGoodsByGrandId(Integer fatherId, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("fatherId", fatherId);
        map.put("isHidden", 0);
        System.out.println("mapapappappapapa" + map);
        List<NxDistributerGoodsEntity> distributerGoodsEntities = distributerGoodsService.querySupplierGoodsByFatherId(map);
        return R.ok().put("data", distributerGoodsEntities);
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


        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDistributerFatherGoodsService.queryDisGreatGrandList(nxDisId);

        if (greatGrandGoods.size() > 0) {
            for (NxDistributerFatherGoodsEntity fatherGoodsEntity : greatGrandGoods) {
                Map<String, Object> map = new HashMap<>();
                map.put("depId", depId);
                map.put("status", 3);
                map.put("dayuStatus", -1);
                map.put("greatGrandId", fatherGoodsEntity.getNxDistributerFatherGoodsId());
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                fatherGoodsEntity.setNewOrderCount(integer);

                List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = fatherGoodsEntity.getFatherGoodsEntities();
                if (fatherGoodsEntities.size() > 0) {
                    for (NxDistributerFatherGoodsEntity secondFatherEntity : fatherGoodsEntities) {
                        map.put("grandId", secondFatherEntity.getNxDistributerFatherGoodsId());
                        Integer integer2 = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                        secondFatherEntity.setNewOrderCount(integer2);

                    }
                }


            }
        }

        return R.ok().put("data", greatGrandGoods);

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