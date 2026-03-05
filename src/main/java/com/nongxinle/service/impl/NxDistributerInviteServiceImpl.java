package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDistributerInviteDao;
import com.nongxinle.entity.NxDistributerInviteEntity;
import com.nongxinle.entity.NxDistributerNxDistributerEntity;
import com.nongxinle.service.NxDistributerBlockService;
import com.nongxinle.service.NxDistributerInviteService;
import com.nongxinle.service.NxDistributerNxDistributerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 配送商邀请注册 Service 实现
 *
 * @author lpy
 */
@Service("nxDistributerInviteService")
public class NxDistributerInviteServiceImpl implements NxDistributerInviteService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;

    @Autowired
    private NxDistributerInviteDao nxDistributerInviteDao;
    @Autowired
    private NxDistributerNxDistributerService nxDistributerNxDistributerService;
    @Autowired
    private NxDistributerBlockService nxDistributerBlockService;

    @Override
    public NxDistributerInviteEntity queryObject(Integer id) {
        return nxDistributerInviteDao.queryObject(id);
    }

    @Override
    public NxDistributerInviteEntity queryByInviteCode(String inviteCode) {
        if (inviteCode == null || inviteCode.trim().isEmpty()) {
            return null;
        }
        return nxDistributerInviteDao.queryByInviteCode(inviteCode.trim());
    }

    @Override
    public List<NxDistributerInviteEntity> queryList(Map<String, Object> map) {
        return nxDistributerInviteDao.queryList(map);
    }

    @Override
    public int queryTotal(Map<String, Object> map) {
        return nxDistributerInviteDao.queryTotal(map);
    }

    @Override
    public int save(NxDistributerInviteEntity entity) {
        return nxDistributerInviteDao.save(entity);
    }

    @Override
    public int update(NxDistributerInviteEntity entity) {
        return nxDistributerInviteDao.update(entity);
    }

    @Override
    public int delete(Integer id) {
        return nxDistributerInviteDao.delete(id);
    }

    @Override
    public String generateInviteCode() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(r.nextInt(CODE_CHARS.length())));
        }
        String code = sb.toString();
        if (nxDistributerInviteDao.queryByInviteCode(code) != null) {
            return generateInviteCode();
        }
        return code;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NxDistributerInviteEntity createInvite(Integer inviterDisId, Integer inviteType, String inviteePhone) {
        if (inviterDisId == null) {
            return null;
        }
        NxDistributerInviteEntity entity = new NxDistributerInviteEntity();
        entity.setInviterNxDistributerId(inviterDisId);
        entity.setInviteCode(generateInviteCode());
        entity.setInviteePhone(inviteePhone);
        entity.setInviteType(inviteType != null ? inviteType : 1);
        entity.setStatus(NxDistributerInviteEntity.STATUS_PENDING);
        entity.setRewardStatus(NxDistributerInviteEntity.REWARD_PENDING);
        nxDistributerInviteDao.save(entity);
        return entity;
    }


}
