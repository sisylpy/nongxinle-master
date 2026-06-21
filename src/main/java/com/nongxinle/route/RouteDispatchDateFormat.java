package com.nongxinle.route;

import java.text.SimpleDateFormat;
import java.util.Date;

/** Phase 2b-2/2b-3：调度读模型日期格式化（统一 yyyy-MM-dd HH:mm:ss） */
public final class RouteDispatchDateFormat {

    public static final String PATTERN = "yyyy-MM-dd HH:mm:ss";

    private RouteDispatchDateFormat() {
    }

    public static String format(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(PATTERN).format(date);
    }
}
