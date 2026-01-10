package com.nongxinle.utils;

import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 邀请码管理器
 * 用于生成、存储和验证邀请码，有效期3分钟
 * 
 * @author lpy
 * @date 2025-01-XX
 */
@Component
public class InviteCodeManager {
    
    // 邀请码缓存，key为邀请码，value为创建时间戳（毫秒）
    private static final ConcurrentHashMap<String, Long> inviteCodeCache = new ConcurrentHashMap<>();
    
    // 邀请码有效期：3分钟 = 180000毫秒
    private static final long INVITE_CODE_EXPIRE_TIME = 3 * 60 * 1000;
    
    // 邀请码长度
    private static final int INVITE_CODE_LENGTH = 6;
    
    /**
     * 生成邀请码
     * @return 6位数字邀请码
     */
    public String generateInviteCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        String inviteCode = code.toString();
        
        // 存储邀请码和创建时间
        inviteCodeCache.put(inviteCode, System.currentTimeMillis());
        
        // 清理过期邀请码
        cleanExpiredCodes();
        
        return inviteCode;
    }
    
    /**
     * 验证邀请码是否有效
     * @param inviteCode 邀请码
     * @return true表示有效，false表示无效或已过期
     */
    public boolean validateInviteCode(String inviteCode) {
        if (inviteCode == null || inviteCode.length() != INVITE_CODE_LENGTH) {
            return false;
        }
        
        Long createTime = inviteCodeCache.get(inviteCode);
        if (createTime == null) {
            return false;
        }
        
        // 检查是否过期
        long currentTime = System.currentTimeMillis();
        if (currentTime - createTime > INVITE_CODE_EXPIRE_TIME) {
            // 已过期，移除
            inviteCodeCache.remove(inviteCode);
            return false;
        }
        
        return true;
    }
    
    /**
     * 使用邀请码（验证后删除）
     * @param inviteCode 邀请码
     * @return true表示使用成功，false表示无效或已过期
     */
    public boolean useInviteCode(String inviteCode) {
        if (validateInviteCode(inviteCode)) {
            inviteCodeCache.remove(inviteCode);
            return true;
        }
        return false;
    }
    
    /**
     * 清理过期的邀请码
     */
    private void cleanExpiredCodes() {
        long currentTime = System.currentTimeMillis();
        inviteCodeCache.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > INVITE_CODE_EXPIRE_TIME
        );
    }
    
    /**
     * 获取邀请码剩余有效时间（秒）
     * @param inviteCode 邀请码
     * @return 剩余有效时间（秒），如果无效或已过期返回0
     */
    public long getRemainingTime(String inviteCode) {
        if (inviteCode == null || inviteCode.length() != INVITE_CODE_LENGTH) {
            return 0;
        }
        
        Long createTime = inviteCodeCache.get(inviteCode);
        if (createTime == null) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - createTime;
        
        if (elapsed > INVITE_CODE_EXPIRE_TIME) {
            inviteCodeCache.remove(inviteCode);
            return 0;
        }
        
        return (INVITE_CODE_EXPIRE_TIME - elapsed) / 1000;
    }
}

