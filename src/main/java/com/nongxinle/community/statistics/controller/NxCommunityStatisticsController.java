package com.nongxinle.community.statistics.controller;

/**
 * @author lpy
 * @date 01-14 21:23
 */

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.nongxinle.entity.NxCommunityFatherGoodsEntity;
import com.nongxinle.community.catalog.service.NxCommunityFatherGoodsService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCommunityStatisticsEntity;
import com.nongxinle.community.statistics.service.NxCommunityStatisticsService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static org.json.XMLTokener.entity;


@RestController
@RequestMapping("api/nxcommunitystatistics")
public class NxCommunityStatisticsController {
    @Autowired
    private NxCommunityStatisticsService nxCommunityStatisticsService;
    @Autowired
    private NxCommunityFatherGoodsService nxCommunityFatherGoodsService;


    @RequestMapping(value = "/comGetMonthProfitTotal/{comId}")
    @ResponseBody
    public R comGetMonthProfitTotal(@PathVariable Integer comId) {
        List<Float> list1 = new ArrayList<>();
        Integer hao = Integer.valueOf(getJustHao(0));
        for (int i = hao - 1 ; i > -1; i--) {
            System.out.println(i + "iiiiiiiiiiiiii");
            String s = formatWhatDay(-i);
            Map<String, Object> map = new HashMap<>();
            map.put("comId", comId);
            map.put("date", s);
            List<NxCommunityStatisticsEntity> entities1 = nxCommunityStatisticsService.querySt(map);
            if (entities1.size() > 0) {
                float profit = nxCommunityStatisticsService.queryStWeekProfitSum(map);
                list1.add(profit);
            } else {
                list1.add(0f);
            }
        }

        return R.ok().put("data", list1);
    }


    @RequestMapping(value = "/comGetWeekProfitTotal/{comId}")
    @ResponseBody
    public R comGetWeekProfitTotal(@PathVariable Integer comId) {
		Map<String, Object> mapData = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        Date today = new Date();
        calendar.setTime(today);// 此处可换为具体某一时间
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        if (weekDay == 1) {
            weekDay = 7;
        } else {
            weekDay = weekDay - 1;
        }
		List<Float> list1 = new ArrayList<>();
		List<Float> list2 = new ArrayList<>();

        for (int i = weekDay - 1; i > -2; i--) {
            String s = formatWhatDay(-i);
            Map<String, Object> map = new HashMap<>();
            map.put("comId", comId);
            map.put("date", s);
            List<NxCommunityStatisticsEntity> entities1 = nxCommunityStatisticsService.querySt(map);
            if (entities1.size() > 0) {
                float profit = nxCommunityStatisticsService.queryStWeekProfitSum(map);
                list1.add(profit);
            } else {
                list1.add(0f);
            }
        }
		mapData.put("thisWeek", list1);

        for (int i = 6 + weekDay; i > weekDay -1; i--) {
            String s = formatWhatDay(-i);
            Map<String, Object> map = new HashMap<>();
            map.put("comId", comId);
            map.put("date", s);
            List<NxCommunityStatisticsEntity> entities1 = nxCommunityStatisticsService.querySt(map);

			if (entities1.size() > 0) {
				float profit = nxCommunityStatisticsService.queryStWeekProfitSum(map);
				list2.add(profit);
			} else {
				list2.add(0f);
			}
        }

		mapData.put("lastWeek", list2);
        return R.ok().put("data", mapData);
    }


    @RequestMapping(value = "/comGetStatistics/{comId}")
    @ResponseBody
    public R comGetStatistics(@PathVariable Integer comId) {
        List<Map<String, Object>> weekStatistics = getWeekStatistics(comId);
        return R.ok().put("data", weekStatistics);
    }

    private List<Map<String, Object>> getWeekStatistics(Integer comId) {
        List<Map<String, Object>> resultData = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Date today = new Date();
        calendar.setTime(today);// 此处可换为具体某一时间
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        if (weekDay == 1) {
            weekDay = 7;
        } else {
            weekDay = weekDay - 1;
        }

        for (int i = 0; i < weekDay; i++) {
            String s = formatWhatDay(-i);
            System.out.println(s + "dtttttttt");
            Map<String, Object> map = new HashMap<>();
            map.put("comId", comId);
            map.put("date", s);
            List<NxCommunityStatisticsEntity> entities1 = nxCommunityStatisticsService.querySt(map);
            if (entities1.size() > 0) {
                Map<String, Object> mapData = new HashMap<>();
                mapData.put("week", getWeek(-i));
                mapData.put("date", formatWhatDate(-i));
                mapData.put("arr", entities1);
                Float total = 0.0f;
                for (NxCommunityStatisticsEntity sat : entities1) {
                    Float nxCsComGoodsProfit = sat.getNxCsComGoodsProfit();
                    total = total + nxCsComGoodsProfit;
                }
                mapData.put("total", total);

                resultData.add(mapData);
            }
        }
        return resultData;

    }


}
