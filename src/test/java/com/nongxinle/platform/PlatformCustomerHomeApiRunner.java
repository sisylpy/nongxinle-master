package com.nongxinle.platform;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.nongxinle.dto.platform.customer.PlatformCustomerGoodsCategoriesRequest;
import com.nongxinle.dto.platform.customer.PlatformCustomerHomeInitRequest;
import com.nongxinle.dto.platform.customer.PlatformCustomerMarketSuppliersRequest;
import com.nongxinle.service.PlatformCustomerHomeService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 平台饭店端首页三接口验收（marketId=1）
 */
public class PlatformCustomerHomeApiRunner {

    private static final int MARKET_ID = 1;
    private static final int DEPARTMENT_ID = 1436;

    public static void main(String[] args) {
        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext("spring-platform-fulfillment-test.xml");
        try {
            PlatformCustomerHomeService service = ctx.getBean(PlatformCustomerHomeService.class);

            PlatformCustomerHomeInitRequest initRequest = new PlatformCustomerHomeInitRequest();
            initRequest.setMarketId(MARKET_ID);
            initRequest.setDepartmentId(DEPARTMENT_ID);
            initRequest.setCustomerUserId(1);

            System.out.println("========== 1. POST /api/platform/customer/home/init ==========");
            printJson(service.homeInit(initRequest));

            PlatformCustomerMarketSuppliersRequest suppliersRequest = new PlatformCustomerMarketSuppliersRequest();
            suppliersRequest.setMarketId(MARKET_ID);
            suppliersRequest.setPage(1);
            suppliersRequest.setLimit(20);

            System.out.println("========== 2. POST /api/platform/customer/market/suppliers ==========");
            printJson(service.listMarketSuppliers(suppliersRequest));

            PlatformCustomerGoodsCategoriesRequest categoriesRequest = new PlatformCustomerGoodsCategoriesRequest();
            categoriesRequest.setMarketId(MARKET_ID);

            System.out.println("========== 3. POST /api/platform/customer/goods/categories ==========");
            printJson(service.listGoodsCategories(categoriesRequest));

            System.out.println("=== Platform customer home APIs verification DONE ===");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            ctx.close();
        }
    }

    private static void printJson(Object data) {
        System.out.println(JSON.toJSONString(data, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue));
    }
}
