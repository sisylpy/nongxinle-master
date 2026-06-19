package com.nongxinle.service.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nongxinle.dto.platform.PlatformDistributerIdSchemaProbeResponse;

/**
 * 探测 nx_department_orders.nx_DO_distributer_id 是否允许 NULL，
 * 并在 NOT NULL 时按 market 解析平台池配送商 ID。
 *
 * MySQL 混合大小写列名下 DatabaseMetaData.getColumns 常取不到列，优先 information_schema。
 */
@Component
public class PlatformDistributerIdResolver {

    private static final Logger logger = LoggerFactory.getLogger(PlatformDistributerIdResolver.class);

    private static final String TABLE = "nx_department_orders";
    private static final String COLUMN_PRIMARY = "nx_DO_distributer_id";
    private static final Pattern JDBC_DB_PATTERN =
            Pattern.compile("jdbc:mysql://[^/]+/([^?]+)", Pattern.CASE_INSENSITIVE);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Environment environment;

    @Value("${jdbc.url:}")
    private String jdbcUrl;

    @Value("${platform.distributer-id-nullable:}")
    private String distributerIdNullableOverride;

    private Boolean distributerIdNullable;

    public boolean isDistributerIdNullable() {
        PlatformDistributerIdSchemaProbeResponse probe = probeSchema();
        if (probe.getNullable() == null) {
            throw new IllegalStateException(probe.getError() != null
                    ? probe.getError()
                    : "无法探测 " + TABLE + "." + COLUMN_PRIMARY + " 是否允许 NULL");
        }
        return probe.getNullable();
    }

    public PlatformDistributerIdSchemaProbeResponse probeSchema() {
        PlatformDistributerIdSchemaProbeResponse response = new PlatformDistributerIdSchemaProbeResponse();
        response.setNote("若 NOT NULL，请配置 platform.pool-distributer-id.{marketId}；也可手动执行 docs/sql/patches/check_nx_department_orders_distributer_id.sql");

        if (distributerIdNullable != null) {
            response.setNullable(distributerIdNullable);
            response.setColumnName(COLUMN_PRIMARY);
            response.setProbeSource("cached");
            return response;
        }

        if (distributerIdNullableOverride != null && !distributerIdNullableOverride.trim().isEmpty()) {
            distributerIdNullable = Boolean.parseBoolean(distributerIdNullableOverride.trim());
            response.setNullable(distributerIdNullable);
            response.setColumnName(COLUMN_PRIMARY);
            response.setProbeSource("config_override");
            return response;
        }

        synchronized (this) {
            if (distributerIdNullable != null) {
                response.setNullable(distributerIdNullable);
                response.setColumnName(COLUMN_PRIMARY);
                response.setProbeSource("cached");
                return response;
            }
            PlatformDistributerIdSchemaProbeResponse probed = probeSchemaFromDatabase();
            if (probed.getNullable() != null) {
                distributerIdNullable = probed.getNullable();
            }
            return probed;
        }
    }

    /**
     * PENDING 阶段写入的 nxDoDistributerId：允许 NULL 则返回 null，否则返回 market 配置的平台池配送商。
     */
    public Integer resolvePendingDistributerId(Integer marketId) {
        if (isDistributerIdNullable()) {
            return null;
        }
        Integer poolId = getPoolDistributerIdForMarket(marketId);
        if (poolId == null) {
            throw new IllegalStateException(
                    "nx_DO_distributer_id 不允许 NULL，请为 marketId=" + marketId
                            + " 配置 platform.pool-distributer-id." + marketId
                            + "，或创建「平台池配送商」后写入该配置");
        }
        return poolId;
    }

    public Integer getPoolDistributerIdForMarket(Integer marketId) {
        if (marketId == null) {
            return null;
        }
        String key = "platform.pool-distributer-id." + marketId;
        String value = environment.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return Integer.valueOf(value.trim());
    }

    private PlatformDistributerIdSchemaProbeResponse probeSchemaFromDatabase() {
        PlatformDistributerIdSchemaProbeResponse response = new PlatformDistributerIdSchemaProbeResponse();
        try (Connection connection = dataSource.getConnection()) {
            String catalog = resolveCatalog(connection);
            response.setCatalog(catalog);
            response.setTableExists(tableExists(connection, catalog, TABLE));

            if (!Boolean.TRUE.equals(response.getTableExists())) {
                response.setError("当前库 " + catalog + " 中不存在表 " + TABLE);
                return response;
            }

            ColumnProbe columnProbe = probeViaInformationSchema(connection, catalog);
            if (columnProbe != null) {
                fillColumnProbe(response, columnProbe, "information_schema");
                return response;
            }

            columnProbe = probeViaShowColumns(connection);
            if (columnProbe != null) {
                fillColumnProbe(response, columnProbe, "show_columns");
                return response;
            }

            columnProbe = probeViaDatabaseMetaData(connection, catalog);
            if (columnProbe != null) {
                fillColumnProbe(response, columnProbe, "database_metadata");
                return response;
            }

            response.setError("在表 " + TABLE + " 中未找到 distributer_id 相关列（已尝试 information_schema / SHOW COLUMNS / DatabaseMetaData）");
            return response;
        } catch (Exception e) {
            logger.error("[probeSchemaFromDatabase] 探测 {}.{} 失败", TABLE, COLUMN_PRIMARY, e);
            response.setError("无法探测 " + TABLE + "." + COLUMN_PRIMARY + " 是否允许 NULL");
            response.setErrorCause(e.getClass().getSimpleName() + ": " + e.getMessage());
            return response;
        }
    }

    private void fillColumnProbe(
            PlatformDistributerIdSchemaProbeResponse response,
            ColumnProbe columnProbe,
            String probeSource) {
        response.setColumnName(columnProbe.columnName);
        response.setNullable(columnProbe.nullable);
        response.setProbeSource(probeSource);
    }

    private String resolveCatalog(Connection connection) throws SQLException {
        String catalog = connection.getCatalog();
        if (catalog != null && !catalog.trim().isEmpty()) {
            return catalog.trim();
        }
        if (jdbcUrl != null) {
            Matcher matcher = JDBC_DB_PATTERN.matcher(jdbcUrl.trim());
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    private boolean tableExists(Connection connection, String catalog, String tableName) throws SQLException {
        if (catalog == null || catalog.isEmpty()) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM information_schema.TABLES "
                + "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, catalog);
            ps.setString(2, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private ColumnProbe probeViaInformationSchema(Connection connection, String catalog) throws SQLException {
        String exactSql = "SELECT COLUMN_NAME, IS_NULLABLE FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_NAME = ? LIMIT 1";
        for (String columnName : new String[]{COLUMN_PRIMARY, "nx_do_distributer_id"}) {
            ColumnProbe probe = queryColumn(connection, exactSql, catalog, TABLE, columnName);
            if (probe != null) {
                return probe;
            }
        }

        String likeSql = "SELECT COLUMN_NAME, IS_NULLABLE FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? "
                + "AND (COLUMN_NAME LIKE '%DO%distributer_id%' OR COLUMN_NAME LIKE '%do%distributer_id%') "
                + "ORDER BY CASE WHEN COLUMN_NAME = ? THEN 0 ELSE 1 END, COLUMN_NAME LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(likeSql)) {
            ps.setString(1, catalog);
            ps.setString(2, TABLE);
            ps.setString(3, COLUMN_PRIMARY);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return toColumnProbe(rs);
                }
            }
        }
        return null;
    }

    private ColumnProbe queryColumn(
            Connection connection, String sql, String catalog, String tableName, String columnName)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, catalog);
            ps.setString(2, tableName);
            ps.setString(3, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return toColumnProbe(rs);
                }
            }
        }
        return null;
    }

    private ColumnProbe probeViaShowColumns(Connection connection) throws SQLException {
        String sql = "SHOW COLUMNS FROM `" + TABLE + "` LIKE '%distributer_id%'";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ColumnProbe preferred = null;
            while (rs.next()) {
                ColumnProbe probe = new ColumnProbe();
                probe.columnName = rs.getString("Field");
                probe.nullable = "YES".equalsIgnoreCase(rs.getString("Null"));
                if (COLUMN_PRIMARY.equals(probe.columnName)) {
                    return probe;
                }
                if (probe.columnName != null
                        && probe.columnName.toLowerCase().contains("do")
                        && probe.columnName.toLowerCase().contains("distributer_id")
                        && preferred == null) {
                    preferred = probe;
                }
            }
            return preferred;
        }
    }

    private ColumnProbe probeViaDatabaseMetaData(Connection connection, String catalog) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String[] tablePatterns = {TABLE, TABLE.toUpperCase()};
        String[] columnPatterns = {COLUMN_PRIMARY, "nx_do_distributer_id", "%distributer_id%"};
        for (String tablePattern : tablePatterns) {
            for (String columnPattern : columnPatterns) {
                try (ResultSet rs = metaData.getColumns(catalog, null, tablePattern, columnPattern)) {
                    ColumnProbe preferred = null;
                    while (rs.next()) {
                        ColumnProbe probe = new ColumnProbe();
                        probe.columnName = rs.getString("COLUMN_NAME");
                        probe.nullable = "YES".equalsIgnoreCase(rs.getString("IS_NULLABLE"));
                        if (COLUMN_PRIMARY.equalsIgnoreCase(probe.columnName)) {
                            return probe;
                        }
                        if (probe.columnName != null
                                && probe.columnName.toLowerCase().contains("do_distributer")
                                && preferred == null) {
                            preferred = probe;
                        }
                    }
                    if (preferred != null) {
                        return preferred;
                    }
                }
            }
        }
        return null;
    }

    private ColumnProbe toColumnProbe(ResultSet rs) throws SQLException {
        ColumnProbe probe = new ColumnProbe();
        probe.columnName = rs.getString("COLUMN_NAME");
        probe.nullable = "YES".equalsIgnoreCase(rs.getString("IS_NULLABLE"));
        return probe;
    }

    private static final class ColumnProbe {
        private String columnName;
        private boolean nullable;
    }
}
