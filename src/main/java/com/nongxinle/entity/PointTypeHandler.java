package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;


@Setter
@Getter
@ToString
public class PointTypeHandler extends BaseTypeHandler<NxLocation> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, NxLocation parameter, JdbcType jdbcType) throws SQLException {
        // 使用 ST_GeomFromText 来将 NxLocation 对象转化为 POINT
        String point = "POINT(" + parameter.getLng() + " " + parameter.getLat() + ")";
        ps.setObject(i, point);
    }

    @Override
    public NxLocation getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String point = rs.getString(columnName);
        return parsePoint(point);
    }

    @Override
    public NxLocation getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String point = rs.getString(columnIndex);
        return parsePoint(point);
    }

    @Override
    public NxLocation getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String point = cs.getString(columnIndex);
        return parsePoint(point);
    }

    private NxLocation parsePoint(String point) {
        if (point != null && point.startsWith("POINT")) {
            String[] coords = point.substring(6, point.length() - 1).split(" ");
            Double lng = Double.valueOf(coords[0]);
            Double lat = Double.valueOf(coords[1]);
            // 使用带有经纬度的构造函数来创建 NxLocation 对象
            return new NxLocation(lng, lat);
        }
        return null;
    }


//    @Override
//    public NxLocation getNullableResult(ResultSet rs, String columnName) throws SQLException {
//        String point = rs.getString(columnName);
//        return parsePoint(point);
//    }
//
//    @Override
//    public NxLocation getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
//        String point = rs.getString(columnIndex);
//        return parsePoint(point);
//    }
//
//    @Override
//    public NxLocation getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
//        String point = cs.getString(columnIndex);
//        return parsePoint(point);
//    }
//
//    private NxLocation parsePoint(String point) {
//        if (point != null && point.startsWith("POINT")) {
//            String[] coords = point.substring(6, point.length() - 1).split(" ");
//            Double lng = Double.valueOf(coords[0]);
//            Double lat = Double.valueOf(coords[1]);
//            NxLocation location = new NxLocation(lng,lat);
//            location.setLng(lng);
//            location.setLat(lat);
//            return location;
//        }
//        return null;
//    }
}

