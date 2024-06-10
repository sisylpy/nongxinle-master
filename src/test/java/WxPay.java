/**
 * java class
 *
 * @Author: peiyi li
 * @Date: 2020-05-22 08:50
 */



import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.QyNxDisCorpEntity;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.PinYin4jUtils.getHeadStringByString;
import static com.nongxinle.utils.PinYin4jUtils.hanziToPinyin;


/**
 *@author lpy
 *@date 2020-05-22 08:50
 */


public class WxPay {

    public static void main(String[] args)  throws Exception{


//            long averageTimeStamp =  timeStampTotal / stockCount ;
//            long nowTimeStamp = System.currentTimeMillis() / 1000;
//            long l = nowTimeStamp - averageTimeStamp;
//            long l1 = l / 1000 / 60 / 60 / 24;
//


//        }
        String pinyin = hanziToPinyin("塑料袋（大）");
        System.out.println(pinyin);
        String headPinyin = getHeadStringByString("塑料袋(大)", false, null);

//        System.out.println(headPinyin);

        String s = afterWhatDay("2023-6-12", 1);
        System.out.println(s);


    }
    //计算两个时间相差几天几小时几秒
    public static String getTimeDiff(String date) {
        System.out.println(date);
        if (ObjectUtils.isEmpty(date)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        System.out.println("dfdd");
        try {
            SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parse = mDateFormat.parse(date);
            System.out.println(parse);
            Date now = new Date();
            // 这样得到的差值是微秒级别
            long diff = now.getTime() - parse.getTime()  ;
            System.out.println(diff);
            // 只能精确到日 无法具细到年 月 不能确定一个月具体多少天 不能确定一年具体多少天
            // 获取日
            long day = diff / (1000 * 60 * 60 * 24);
            System.out.println(day);
            diff = diff % (1000 * 60 * 60 * 24);
            if (day > 0) {
                System.out.println(day);
                sb.append(day).append("天");
                System.out.println(sb.append(day).append("天"));
            }
            // 获取时
            long hour = diff / (1000 * 60 * 60);
            diff = diff % (1000 * 60 * 60);
            if (hour > 0) {
                sb.append(hour).append("时");
            }
            // 获取分
            long min = diff / (1000 * 60);
            diff = diff % (1000 * 60);
            if (min > 0) {
                sb.append(min).append("分");
            }
            // 获取秒
            long sec = diff / 1000;
            if (sec > 0) {
                sb.append(sec).append("秒");
            }
            System.out.println(sb.toString());
            return sb.toString();
        } catch (ParseException e) {
            return "";
        }
    }




}

