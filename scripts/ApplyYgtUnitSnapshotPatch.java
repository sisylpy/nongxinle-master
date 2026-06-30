import java.sql.*;

/**
 * 优果团 Phase 1B 数据库补丁：ygt_campaign_goods 加 ygt_cg_unit_snapshot
 * 依赖 classpath 自带 mysql-connector-java
 */
public class ApplyYgtUnitSnapshotPatch {
    static final String URL = "jdbc:mysql://101.42.222.149:3306/nongxinle?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false";
    static final String USER = "root";
    static final String PASS = "Lpy87176693";

    static final String CHECK_SQL = "SELECT COUNT(*) FROM information_schema.COLUMNS " +
        "WHERE TABLE_SCHEMA='nongxinle' AND TABLE_NAME='ygt_campaign_goods' AND COLUMN_NAME='ygt_cg_unit_snapshot'";

    static final String ALTER_SQL = "ALTER TABLE ygt_campaign_goods " +
        "ADD COLUMN ygt_cg_unit_snapshot VARCHAR(32) DEFAULT NULL COMMENT '单位快照' AFTER ygt_cg_standard_snapshot";

    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            // 幂等检查
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(CHECK_SQL)) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    System.out.println("[SKIP] ygt_cg_unit_snapshot 已存在，跳过。");
                    return;
                }
            }
            // 执行 ALTER
            try (Statement st = conn.createStatement()) {
                st.execute(ALTER_SQL);
                System.out.println("[OK] ygt_cg_unit_snapshot 字段添加成功！");
            }
        }
    }
}
