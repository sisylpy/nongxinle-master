package com.nongxinle.route;

import com.nongxinle.dao.NxDistributerUserDao;
import com.nongxinle.entity.NxDistributerUserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDisUserAdmin;

/**
 * 派车接口 operatorUserId：优先显式入参，否则按 disId 取配送商管理员账号（小程序无登录态透传时的后端兜底）。
 */
@Component
public class DisRouteDispatchOperatorResolver {

    @Autowired
    private NxDistributerUserDao nxDistributerUserDao;

    public Integer resolve(Integer disId, Integer explicitOperatorUserId) {
        if (explicitOperatorUserId != null) {
            return explicitOperatorUserId;
        }
        if (disId == null) {
            return null;
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("disId", disId);
        params.put("admin", getNxDisUserAdmin());
        List<NxDistributerUserEntity> admins = nxDistributerUserDao.getAdminUserByParams(params);
        if (admins == null || admins.isEmpty()) {
            return null;
        }
        for (NxDistributerUserEntity admin : admins) {
            if (admin != null && admin.getNxDistributerUserId() != null) {
                return admin.getNxDistributerUserId();
            }
        }
        return null;
    }
}
