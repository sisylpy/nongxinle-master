package com.nongxinle.controller;

/**
 * @author lpy
 * @date 05-29 10:22
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.MyAPPIDConfig;
import com.nongxinle.utils.WeChatUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.formatWhatDay;


@RestController
@RequestMapping("api/nxbuyuser")
public class NxBuyUserController {
    @Autowired
    private NxBuyUserService nxBuyUserService;
    @Autowired
	private NxRetailerPurchaseBatchService nxRetPurchaseBatchService;
    @Autowired
    private NxDistributerPurchaseBatchService nxDisPurchaseBatchService;
    @Autowired
    private NxCommunityPurchaseBatchService nxComPurchaseBatchService;
    @Autowired
    private GbDistributerPurchaseBatchService gbDistributerPurchaseBatchService;



    @RequestMapping(value = "/registerBuyer", method = RequestMethod.POST)
    @ResponseBody
    public R registerBuyer(@RequestBody NxBuyUserEntity buyUserEntity) {
//wxApp
        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getJinriDinghuoAppId() +
                "&secret=" + myAPPIDConfig.getJinriDinghuoScreat() + "&js_code=" + buyUserEntity.getNxBuCode() + "&grant_type=authorization_code";
        // 发送请求，返回Json字符串
        String str = WeChatUtil.httpRequest(url, "GET", null);

        // 转成Json对象 获取openid
        JSONObject jsonObject = JSONObject.parseObject(str);
        System.out.println(jsonObject);

        // 我们需要的openid，在一个小程序中，openid是唯一的
        String openid = jsonObject.get("openid").toString();

        //添加新用户
        buyUserEntity.setNxBuWxOpenId(openid);
        buyUserEntity.setNxBuJoinDate(formatWhatDay(0));
        nxBuyUserService.save(buyUserEntity);

        String nxBuyType = buyUserEntity.getNxBuyType();
        if(nxBuyType.equals("ret")){
            //改batch buyuser
            Integer restrauntUserId = buyUserEntity.getNxBuyUserId();
            Integer nxBuyerPurBatchId = buyUserEntity.getNxBuyerPurBatchId();
            NxRetailerPurchaseBatchEntity nxRetailerPurchaseBatchEntity = nxRetPurchaseBatchService.queryObject(nxBuyerPurBatchId);
            nxRetailerPurchaseBatchEntity.setNxRpbBuyUserId(restrauntUserId);
            nxRetPurchaseBatchService.update(nxRetailerPurchaseBatchEntity);
            Map<String, Object> map = new HashMap<>();
            map.put("batchId", nxBuyerPurBatchId);
            NxRetailerPurchaseBatchEntity nxRetailerPurchaseBatchEntity1 = nxRetPurchaseBatchService.queryRetPurBatchDetail(map);

            return R.ok().put("data", nxRetailerPurchaseBatchEntity1);
        }
        if(nxBuyType.equals("dis")){
            //改batch buyuser
            Integer restrauntUserId = buyUserEntity.getNxBuyUserId();
            Integer nxBuyerPurBatchId = buyUserEntity.getNxBuyerPurBatchId();
            NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDisPurchaseBatchService.queryObject(nxBuyerPurBatchId);
            nxDistributerPurchaseBatchEntity.setNxDpbBuyUserId(restrauntUserId);
            nxDisPurchaseBatchService.update(nxDistributerPurchaseBatchEntity);

            NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity1 = nxDisPurchaseBatchService.queryBatchWithOrders(nxBuyerPurBatchId);

            return R.ok().put("data", nxDistributerPurchaseBatchEntity1);
        }





//        if(nxBuyType.equals("gbDis")){
//            //改batch buyuser
//            Integer restrauntUserId = buyUserEntity.getNxBuyUserId();
//            Integer nxBuyerPurBatchId = buyUserEntity.getNxBuyerPurBatchId();
//            GbDistributerPurchaseBatchEntity gbDistributerPurchaseBatchEntity = gbDistributerPurchaseBatchService.queryObject(nxBuyerPurBatchId);
//            gbDistributerPurchaseBatchEntity.setGbDpbBuyUserId(restrauntUserId);
//            gbDistributerPurchaseBatchService.update(gbDistributerPurchaseBatchEntity);
//
//            GbDistributerPurchaseBatchEntity gbDistributerPurchaseBatchEntity1 = gbDistributerPurchaseBatchService.queryBatchWithOrders(nxBuyerPurBatchId);
//
//            return R.ok().put("data", gbDistributerPurchaseBatchEntity1);
//        }

        if(nxBuyType.equals("com")){
            //改batch buyuser
            Integer restrauntUserId = buyUserEntity.getNxBuyUserId();
            Integer nxBuyerPurBatchId = buyUserEntity.getNxBuyerPurBatchId();
            NxCommunityPurchaseBatchEntity nxCommunityPurchaseBatchEntity = nxComPurchaseBatchService.queryObject(nxBuyerPurBatchId);
            nxCommunityPurchaseBatchEntity.setNxCpbBuyUserId(restrauntUserId);
            nxComPurchaseBatchService.update(nxCommunityPurchaseBatchEntity);
            Map<String, Object> map = new HashMap<>();
            map.put("batchId", nxBuyerPurBatchId);
            NxCommunityPurchaseBatchEntity nxCommunityPurchaseBatchEntity1 = nxComPurchaseBatchService.queryBatchDetail(map);

            return R.ok().put("data", nxCommunityPurchaseBatchEntity1);
        }


        return R.error(-1,"chongxinchaxun");

    }



}
