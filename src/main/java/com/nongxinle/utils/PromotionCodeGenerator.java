package com.nongxinle.utils;

import com.nongxinle.dao.NxCustomerPromotionCodeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class PromotionCodeGenerator {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 10;

    @Autowired
    private NxCustomerPromotionCodeDao nxCustomerPromotionCodeDao;

    public String generateUniqueCode() {
        Random random = new Random();
        for (int attempt = 0; attempt < 30; attempt++) {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
            }
            String code = sb.toString();
            if (nxCustomerPromotionCodeDao.queryByCode(code) == null) {
                return code;
            }
        }
        throw new IllegalStateException("无法生成唯一推广码");
    }
}
