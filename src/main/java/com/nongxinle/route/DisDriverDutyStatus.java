package com.nongxinle.route;

/** 司机每日可派状态（老板控制是否参与派车，与账号 nx_DIU_admin 长期角色分离） */
public final class DisDriverDutyStatus {
    public static final String OFF_DUTY = "OFF_DUTY";
    public static final String ON_DUTY = "ON_DUTY";

    private DisDriverDutyStatus() {
    }
}
