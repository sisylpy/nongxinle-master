package com.nongxinle.community.coupon.controller;

/**
 * @author lpy
 * @date 05-15 08:30
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxCommunityCardEntity;
import com.nongxinle.entity.NxCommunityCouponEntity;
import com.nongxinle.community.customer.service.NxCommunityCardService;
import com.nongxinle.community.coupon.service.NxCommunityCouponService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCustomerUserCouponEntity;
import com.nongxinle.community.coupon.service.NxCustomerUserCouponService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.CustomerReferralConstants;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;


@RestController
@RequestMapping("api/nxcustomerusercoupon")
public class NxCustomerUserCouponController {
    @Autowired
    private NxCustomerUserCouponService nxCustomerUserCouponService;
    @Autowired
    private NxCommunityCouponService nxCommunityCouponService;


    //
    @RequestMapping(value = "/receiveSubOrderCoupon", method = RequestMethod.POST)
    @ResponseBody
    public R receiveSubOrderCoupon(Integer userId, Integer coupId, Integer shareUserId) {
        System.out.println("soudfid" + coupId);
        NxCustomerUserCouponEntity userCouponEntity = nxCustomerUserCouponService.getUserCouponById(coupId);
        userCouponEntity.setNxCucShareUserId(userId);
        userCouponEntity.setNxCucStatus(0);
        nxCustomerUserCouponService.update(userCouponEntity);

        NxCustomerUserCouponEntity nxCustomerUserCouponEntity = new NxCustomerUserCouponEntity();
        nxCustomerUserCouponEntity.setNxCucStatus(0);
        nxCustomerUserCouponEntity.setNxCucFromShareUserId(shareUserId);
        nxCustomerUserCouponEntity.setNxCucCustomerUserId(userId);
        nxCustomerUserCouponEntity.setNxCucCommunityId(userCouponEntity.getNxCucCommunityId());
        nxCustomerUserCouponEntity.setNxCucCouponId(userCouponEntity.getNxCucCouponId());
        nxCustomerUserCouponEntity.setNxCucSourceType(CustomerReferralConstants.SOURCE_GIFT_TRANSFER);
        nxCustomerUserCouponService.save(nxCustomerUserCouponEntity);

        return R.ok();
    }

    @RequestMapping(value = "/getCoupDetail/{id}")
    @ResponseBody
    public R getCoupDetail(@PathVariable Integer id) {

        NxCustomerUserCouponEntity customerUserCouponEntity = nxCustomerUserCouponService.queryUserCouponDetail(id);
        return R.ok().put("data", customerUserCouponEntity);
    }


    @RequestMapping(value = "/shareCoupon/{id}")
    @ResponseBody
    public R shareCoupon(@PathVariable Integer id) {
        NxCustomerUserCouponEntity customerUserCouponEntity = nxCustomerUserCouponService.queryUserCouponDetail(id);
        customerUserCouponEntity.setNxCucStatus(-1);
        customerUserCouponEntity.setNxCucShareTime(formatWhatYearDayTime(1));
        nxCustomerUserCouponService.update(customerUserCouponEntity);
        return R.ok().put("data", customerUserCouponEntity);

    }


    @RequestMapping(value = "/userGetCouponPick", method = RequestMethod.POST)
    @ResponseBody
    public R userGetCouponPick(Integer userId, Integer type, Integer commId) {


        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("type", type);
        map.put("commId", commId);
        map.put("xiaoyuStatus", 1);
        map.put("dayuStatus", -2);
        map.put("shareUserId", 0);
        List<NxCustomerUserCouponEntity> couponEntities = nxCustomerUserCouponService.queryUserCouponListByParams(map);
        if (couponEntities.size() > 0) {
            for (NxCustomerUserCouponEntity userCouponEntity : couponEntities) {

                if (userCouponEntity.getNxCucStatus() == -1) {
                    LocalDateTime now = LocalDateTime.now();
                    String nxCucShareTime = userCouponEntity.getNxCucShareTime();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    System.out.println("uutuututu------------------------" + nxCucShareTime);
                    // 将字符串解析为LocalDateTime
                    LocalDateTime wasteTime = LocalDateTime.parse(nxCucShareTime, formatter);
                    if (wasteTime.isBefore(now)) {
                        userCouponEntity.setNxCucShareTime(null);
                        userCouponEntity.setNxCucStatus(0);
                        nxCustomerUserCouponService.update(userCouponEntity);
                    }
                }
            }
        }
        return R.ok().put("data", couponEntities);
    }


    @RequestMapping(value = "/customerUserSaveCouponBatch", method = RequestMethod.POST)
    @ResponseBody
    public R customerUserSaveCouponBatch(Integer userId, String ids, Integer commId) {

        String[] split = ids.split(",");
        for (String id : split) {
            System.out.println("id===" + id);
            NxCustomerUserCouponEntity customerUserCouponEntity = new NxCustomerUserCouponEntity();
            customerUserCouponEntity.setNxCucCommunityId(commId);
            customerUserCouponEntity.setNxCucCustomerUserId(userId);
            customerUserCouponEntity.setNxCucCouponId(Integer.parseInt(id));
            customerUserCouponEntity.setNxCucStatus(0);
            customerUserCouponEntity.setNxCucSourceType(CustomerReferralConstants.SOURCE_ACTIVE_CLAIM);

            nxCustomerUserCouponService.save(customerUserCouponEntity);

            NxCommunityCouponEntity couponEntity = nxCommunityCouponService.queryObject(Integer.parseInt(id));
            couponEntity.setNxCpDownCount(couponEntity.getNxCpDownCount() + 1);
            nxCommunityCouponService.update(couponEntity);
        }

        return R.ok();
    }

    @RequestMapping(value = "/customerUserSaveCoupon", method = RequestMethod.POST)
    @ResponseBody
    public R customerUserSaveCoupon(Integer userId, Integer couId, Integer commId) {
        NxCustomerUserCouponEntity customerUserCouponEntity = new NxCustomerUserCouponEntity();
        customerUserCouponEntity.setNxCucCommunityId(commId);
        customerUserCouponEntity.setNxCucCustomerUserId(userId);
        customerUserCouponEntity.setNxCucCouponId(couId);
        customerUserCouponEntity.setNxCucStatus(0);
        customerUserCouponEntity.setNxCucSourceType(CustomerReferralConstants.SOURCE_ACTIVE_CLAIM);

        nxCustomerUserCouponService.save(customerUserCouponEntity);

        NxCommunityCouponEntity couponEntity = nxCommunityCouponService.queryObject(couId);
        couponEntity.setNxCpDownCount(couponEntity.getNxCpDownCount() + 1);
        nxCommunityCouponService.update(couponEntity);

        return R.ok();
    }


    @RequestMapping(value = "/userGetCoupous/{id}")
    @ResponseBody
    public R userGetCoupons(@PathVariable Integer id) {

//		String nxCpStopTimeZone = coupon.getNxCpStopTimeZone();
        String nxCpStopTimeZone = "2024-05-01-00-00-00";
        String[] split = nxCpStopTimeZone.split("-");

        int year = Integer.parseInt(split[0]);
        int month = Integer.parseInt(split[1]);
        int day = Integer.parseInt(split[2]);
        int hour = Integer.parseInt(split[3]);
        int minute = Integer.parseInt(split[4]);
        int haomiao = Integer.parseInt(split[5]);
        LocalDateTime beginTime = LocalDateTime.of(year, month, day, hour, minute, haomiao);
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(beginTime)) {
            System.out.println("1ok");
        } else {
            System.out.println("nook");
        }
        return R.ok();
    }


}
