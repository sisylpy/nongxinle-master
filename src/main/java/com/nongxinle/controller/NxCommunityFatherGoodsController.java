package com.nongxinle.controller;

/**
 * @author lpy
 * @date 2020-02-24 17:06:57
 */

import java.awt.image.ImageProducer;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.DateUtils.getNowMinute;


@RestController
@RequestMapping("api/nxcommunityfathergoods")
public class NxCommunityFatherGoodsController {
    @Autowired
    private NxCommunityFatherGoodsService cfgService;
    @Autowired
    private NxCommunityAdsenseService nxCommunityAdsenseService;
    @Autowired
    private NxCommunityGoodsService cgService;
    @Autowired
    private NxCommunityOrdersSubService nxCommunityOrdersSubService;
    @Autowired
    private NxCustomerUserCardService nxCustomerUserCardService;



    @RequestMapping(value = "/updateFatherGoodsSort", method = RequestMethod.POST)
    @ResponseBody
    public R updateFatherGoodsSort (@RequestBody List<NxCommunityFatherGoodsEntity> fatherGoodsEntities) {
        if(fatherGoodsEntities.size() > 0){
            for(NxCommunityFatherGoodsEntity fatherGoodsEntity: fatherGoodsEntities){
                cfgService.update(fatherGoodsEntity);
            }
        }
        return R.ok();
    }
    @RequestMapping(value = "/deleteFatherGoods/{goodsId}")
    @ResponseBody
    public R deleteFatherGoods(@PathVariable Integer goodsId) {
        Map<String, Object> map = new HashMap<>();
        map.put("cgFatherId", goodsId);
        List<NxCommunityGoodsEntity> nxDistributerGoodsEntities = cgService.queryComGoodsByParams(map);
        Map<String, Object> map1 = new HashMap<>();
        map1.put("fathersFatherId", goodsId);
        List<NxCommunityFatherGoodsEntity> fatherGoodsEntities = cfgService.queryComFathersGoodsByParams(map1);
        if (nxDistributerGoodsEntities.size() > 0 || fatherGoodsEntities.size() > 0) {
            return R.error(-1, "有商品不能删除0000");
        } else {
            cfgService.delete(goodsId);
            return R.ok();
        }
    }

    @RequestMapping(value = "/saveFatherGoods", method = RequestMethod.POST)
    @ResponseBody
    public R saveFatherGoods(@RequestBody NxCommunityFatherGoodsEntity fatherGoods) {
        Map<String, Object> map5 = new HashMap<>();
        map5.put("level", 1);
        map5.put("fathersFatherId", fatherGoods.getNxCfgFathersFatherId());
        List<NxCommunityFatherGoodsEntity> fatherGoodsEntities = cfgService.queryComFathersGoodsByParams(map5);
        if (fatherGoodsEntities.size() > 0) {
            fatherGoods.setNxCfgFatherGoodsSort(fatherGoodsEntities.size() + 1);
        } else {
            fatherGoods.setNxCfgFatherGoodsSort(1);
        }
        fatherGoods.setNxCfgNxGoodsId(-1);
        fatherGoods.setNxCfgGoodsAmount(0);
        cfgService.save(fatherGoods);
        return R.ok();
    }


    @ResponseBody
    @RequestMapping(value = "/saveFatherGoodsNxNew", method = RequestMethod.POST)
    public R saveFatherGoodsNxNew(String goodsName, Integer fatherId) {
        NxCommunityFatherGoodsEntity fatherGoodsEntity = cfgService.queryObject(fatherId);
        Map<String, Object> map = new HashMap<>();
        map.put("fathersFatherId", fatherId);
        List<NxCommunityFatherGoodsEntity> fatherGoodsEntities = cfgService.queryComFathersGoodsByParams(map);
        NxCommunityFatherGoodsEntity goodsEntity = new NxCommunityFatherGoodsEntity();
        goodsEntity.setNxCfgFatherGoodsName(goodsName);
        goodsEntity.setNxCfgFathersFatherId(fatherId);
        goodsEntity.setNxCfgFatherGoodsSort(fatherGoodsEntities.size() + 1);
        goodsEntity.setNxCfgFatherGoodsLevel(2);
        goodsEntity.setNxCfgGoodsAmount(0);
        goodsEntity.setNxCfgNxGoodsId(-1);
        goodsEntity.setNxCfgCommunityId(fatherGoodsEntity.getNxCfgCommunityId());
        cfgService.save(goodsEntity);


        fatherGoodsEntity.setNxCfgGoodsAmount(fatherGoodsEntity.getNxCfgGoodsAmount() + 1);
        cfgService.update(fatherGoodsEntity);


        return R.ok();
    }



    @RequestMapping(value = "/updateFatherGoods", method = RequestMethod.POST)
    @ResponseBody
    public R updateFatherGoods(@RequestBody NxCommunityFatherGoodsEntity fatherGoods) {
        cfgService.update(fatherGoods);

        return R.ok();
    }


    @RequestMapping(value = "/updateFatherGoodsWithFile", method = RequestMethod.POST)
    @ResponseBody
    public R updateFatherGoodsWithFile(@RequestParam("file") MultipartFile file,
                                    @RequestParam("id") Integer id,
                                    HttpSession session) {
        //1,上传图片
        String newUploadName = "goodsImage";
        String realPath = UploadFile.upload(session, newUploadName, file);

        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + filename;

        System.out.println("nefifiififiififpapapa" + filePath);


        NxCommunityFatherGoodsEntity fatherGoodsEntity = cfgService.queryObject(id);
        String oldPath = fatherGoodsEntity.getNxCfgFatherGoodsImg();
        if (oldPath != null && !oldPath.trim().isEmpty()) {
            String oldAbsolutePath = Constant.EXTERNAL_IMAGE_DIR + oldPath;
            File file1 = new File(oldAbsolutePath);
            if (file1.exists()) {
                file1.delete();
            }
        }


        fatherGoodsEntity.setNxCfgFatherGoodsImg(filePath);

        cfgService.update(fatherGoodsEntity);

        return R.ok();
    }




    //
    @RequestMapping(value = "/getFatherWithGoodsAdmin", method = RequestMethod.POST)
    @ResponseBody
    public R getFatherWithGoodsAdmin(Integer commId) {

        System.out.println("idcommdmmdmdmdmdm" + commId);
        Map<String, Object> map = new HashMap<>();
        map.put("commId", commId);
        List<NxCommunityFatherGoodsEntity> fatherGoodsEntities =  cfgService.queryGrandGoodsAdmin(map);
        return R.ok().put("data", fatherGoodsEntities);
    }

    //
    @RequestMapping(value = "/getFatherWithGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getFatherWithGoods(Integer commId, Integer orderUserId,Integer orderType,
                                Integer spId, Integer serviceType) {

        Map<String, Object> map = new HashMap<>();
        map.put("commId", commId);
        map.put("level", 2);
        map.put("nowMinute", getNowMinute());
        map.put("off", 0);
        map.put("splicingOrderId", spId);
        map.put("serviceType", serviceType);
        if(orderUserId != -1){
            map.put("orderUserId", orderUserId);
            map.put("orderType", orderType);
        }

        System.out.println("tytytytytytyyt" + map);

        List<NxCommunityFatherGoodsEntity> fatherGoodsEntities =  cfgService.queryGrandGoodsWithOrders(map);
        Map<String, Object> mapA = new HashMap<>();
        mapA.put("orderUserId", orderUserId);
        mapA.put("status", -1);
        mapA.put("orderType", orderType);
        mapA.put("splicingOrderId", spId);
        mapA.put("serviceType", serviceType);
        System.out.println("apappapap" + mapA);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", fatherGoodsEntities);
        mapR.put("subOrders", nxCommunityOrdersSubEntities);

        Map<String, Object> mapC = new HashMap<>();
        mapC.put("userId", orderUserId);
        mapC.put("status", -1);
        mapC.put("type", orderType);
        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);
        mapR.put("cardList", cardEntities);

        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/getFatherWithGoodsDesk", method = RequestMethod.POST)
    @ResponseBody
    public R getFatherWithGoodsDesk(Integer commId, Integer deskId,Integer orderType,
                                Integer spId, Integer serviceType) {

        Map<String, Object> map = new HashMap<>();
        map.put("commId", commId);
        map.put("level", 2);
        map.put("nowMinute", getNowMinute());
        map.put("off", 0);
        map.put("splicingOrderId", spId);
        map.put("serviceType", serviceType);
        if(deskId != -1 && deskId != 99){
            map.put("deskId", deskId);
            map.put("orderType", orderType);
        }

        System.out.println("tytytytytytyyt" + map);

        List<NxCommunityFatherGoodsEntity> fatherGoodsEntities =  cfgService.queryGrandGoodsWithOrdersDesk(map);
        Map<String, Object> mapA = new HashMap<>();
        mapA.put("deskId", deskId);
        mapA.put("status", -1);
        mapA.put("orderType", orderType);
        mapA.put("splicingOrderId", spId);
        mapA.put("serviceType", serviceType);
        System.out.println("apappapap" + mapA);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", fatherGoodsEntities);
        mapR.put("subOrders", nxCommunityOrdersSubEntities);

//        Map<String, Object> mapC = new HashMap<>();
//        mapC.put("deskId", deskId);
//        mapC.put("status", -1);
//        mapC.put("type", orderType);
//        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);
        mapR.put("cardList", new ArrayList<>());

        return R.ok().put("data", mapR);
    }


    //
    @RequestMapping(value = "/getFatherWithGoodsWithTime", method = RequestMethod.POST)
    @ResponseBody
    public R getFatherWithGoodsWithTime(Integer commId, Integer orderUserId,Integer orderType, Integer spId, Integer serviceType, Integer time) {

        Map<String, Object> map = new HashMap<>();
        map.put("commId", commId);
        map.put("level", 2);
        map.put("nowMinute", time);
        map.put("off", 0);
        map.put("splicingOrderId", spId);
        map.put("serviceType", serviceType);
        if(orderUserId != -1){
            map.put("orderUserId", orderUserId);
            map.put("orderType", orderType);
        }

        System.out.println("timemememmeme" + map);

        List<NxCommunityFatherGoodsEntity> fatherGoodsEntities =  cfgService.queryGrandGoodsWithOrders(map);
        Map<String, Object> mapA = new HashMap<>();
        mapA.put("orderUserId", orderUserId);
        mapA.put("status", -1);
        mapA.put("orderType", orderType);
        mapA.put("splicingOrderId", spId);
        mapA.put("serviceType", serviceType);
        System.out.println("apappapap" + mapA);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", fatherGoodsEntities);
        mapR.put("subOrders", nxCommunityOrdersSubEntities);

        Map<String, Object> mapC = new HashMap<>();
        mapC.put("userId", orderUserId);
        mapC.put("status", -1);
        mapC.put("type", orderType);
        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);
        mapR.put("cardList", cardEntities);

        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/getRankFatherGoods/{comId}")
    @ResponseBody
    public R getRankFatherGoods(@PathVariable Integer comId) {
        List<NxCommunityFatherGoodsEntity> fatherGoodsEntities = cfgService.queryRankFatherGoods(comId);
        return R.ok().put("data", fatherGoodsEntities);
    }

    /**
     * 获取批发商商品的父类列表
     * @param comId 批发商id
     * @return 批发商父类列表
     */
    @RequestMapping(value = "/resGetComGoodsCata", method = RequestMethod.POST)
    @ResponseBody
    public R resGetComGoodsCata(Integer comId, Integer level) {
        System.out.println(comId + level + "abc");
        Map<String, Object> map = new HashMap<>();
        map.put("comId", comId);
        map.put("level", level);
        List<NxCommunityFatherGoodsEntity> comGoodsCata = cfgService.queryComGoodsCata(map);
        if (comGoodsCata.size() > 0) {
//            List<NxCommunityFatherGoodsEntity> newList = new ArrayList<>();
            for (NxCommunityFatherGoodsEntity greatGrandGoods : comGoodsCata) {
                for (NxCommunityFatherGoodsEntity grandGoods : greatGrandGoods.getFatherGoodsEntities()) {
					for (NxCommunityFatherGoodsEntity fatherGoods : grandGoods.getFatherGoodsEntities()) {

						StringBuilder builder = new StringBuilder();
						Map<String, Object> map1 = new HashMap<>();
						Integer communityFatherGoodsId = fatherGoods.getNxCommunityFatherGoodsId();
						map1.put("fatherId", communityFatherGoodsId);
						map1.put("serviceLevel", level);
						List<NxCommunityGoodsEntity> goodsEntities = cgService.queryCgSubNameByFatherId(map1);

						for (NxCommunityGoodsEntity goods : goodsEntities) {
							String nxGoodsName = goods.getNxCgGoodsName();
							builder.append(nxGoodsName);
							builder.append(',');
						}
						System.out.println(builder + "buildld");
						fatherGoods.setCgGoodsSubNames(builder.toString());
//						newList.add(fatherGoods);
					}
                }
            }
            return R.ok().put("data", comGoodsCata);
        }
        return R.ok().put("data", new ArrayList<>());
    }
    /**
     * 获取批发商商品的父类列表
     * @param
     * @return 批发商父类列表
     */
//    @RequestMapping(value = "/customerGetComGoodsCata/{comId}")
//    @ResponseBody
//    public R customerGetComGoodsCata(@PathVariable Integer comId) {
//        System.out.println("fdfajdajfda???");
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("comId", comId);
//        List<NxCommunityFatherGoodsEntity> comGoodsCata = cfgService.queryComGoodsCata(map);
//        if (comGoodsCata.size() > 0) {
//
//            List<NxCommunityFatherGoodsEntity> newList = new ArrayList<>();
//
//            for (NxCommunityFatherGoodsEntity greatGrandGoods : comGoodsCata) {
//                for (NxCommunityFatherGoodsEntity grandGoods : greatGrandGoods.getFatherGoodsEntities()) {
//                    for (NxCommunityFatherGoodsEntity fatherGoods : grandGoods.getFatherGoodsEntities()) {
//
//                        StringBuilder builder = new StringBuilder();
//                        Map<String, Object> map1 = new HashMap<>();
//                        Integer communityFatherGoodsId = fatherGoods.getNxCommunityFatherGoodsId();
//                        map1.put("fatherId", communityFatherGoodsId);
//                        List<NxCommunityGoodsEntity> goodsEntities = cgService.queryCgSubNameByFatherId(map1);
//
//                        for (NxCommunityGoodsEntity goods : goodsEntities) {
//                            String nxGoodsName = goods.getNxCgGoodsName();
//                            builder.append(nxGoodsName);
//                            builder.append(',');
//                        }
//                        fatherGoods.setCgGoodsSubNames(builder.toString());
//                        newList.add(fatherGoods);
//                    }
//
//                }
//
//            }
//
//
//            return R.ok().put("data", comGoodsCata);
//        }
//        return R.ok().put("data", new ArrayList<>());
//    }



    @RequestMapping(value = "/comGetFatherGoodsByFatherId/{fatherId}")
    @ResponseBody
    public R comGetFatherGoodsByFatherId(@PathVariable Integer fatherId) {
        Map<String, Object> map = new HashMap<>();
        map.put("fathersFatherId", fatherId);
        System.out.println("whwhwhhw" + map);
        List<NxCommunityFatherGoodsEntity> comGoodsCata = cfgService.queryComFathersGoodsByParams(map);
        if (comGoodsCata.size() > 0) {
            return R.ok().put("data", comGoodsCata);
        }
        List<NxCommunityFatherGoodsEntity> zero = new ArrayList<>();
        return R.ok().put("data", zero);
    }



    @RequestMapping(value = "/comGetComGoodsCata/{comId}")
    @ResponseBody
    public R comGetComGoodsCata(@PathVariable Integer comId) {
        Map<String, Object> map = new HashMap<>();
        map.put("commId", comId);
        System.out.println("whwhwhhw" + map);
        List<NxCommunityFatherGoodsEntity> comGoodsCata = cfgService.queryComGoodsCata(map);
        if (comGoodsCata.size() > 0) {
            return R.ok().put("data", comGoodsCata);
        }
        List<NxCommunityFatherGoodsEntity> zero = new ArrayList<>();
        return R.ok().put("data", zero);
    }




    @RequestMapping(value = "/getCgCateList/{communityId}")
    @ResponseBody
    public R getGoodsCateList(@PathVariable Integer communityId) {
        List<NxCommunityFatherGoodsEntity> entities = cfgService.queryCataListByCommunityId(communityId);
        return R.ok().put("data", entities);
    }


    /**
     * 保存
     */
    @ResponseBody
    @RequestMapping("/save")
    @RequiresPermissions("nxdistributerfathergoods:save")
    public R save(@RequestBody NxCommunityFatherGoodsEntity nxDistributerFatherGoods) {
        cfgService.save(nxDistributerFatherGoods);

        return R.ok();
    }


}
