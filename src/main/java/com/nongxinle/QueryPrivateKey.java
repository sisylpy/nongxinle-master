package com.nongxinle;

import java.sql.*;
import java.util.Properties;

public class QueryPrivateKey {
    public static void main(String[] args) {
        String url = "jdbc:mysql://101.42.222.149:3306/nongxinle?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false";
        String username = "root";
        String password = "Lpy87176693";
        
        try {
            // 加载MySQL驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 建立连接
            Connection conn = DriverManager.getConnection(url, username, password);
            
            // 查询私钥
            String sql = "SELECT " +
                        "qy_gb_dis_qy_corp_id AS corpId, " +
                        "LENGTH(qy_gb_dis_corp_msgaudit_private_key) AS keyLength, " +
                        "LEFT(qy_gb_dis_corp_msgaudit_private_key, 100) AS keyStart, " +
                        "RIGHT(qy_gb_dis_corp_msgaudit_private_key, 100) AS keyEnd, " +
                        "qy_gb_dis_corp_msgaudit_private_key AS fullKey " +
                        "FROM qy_gb_dis_corp_msgaudit " +
                        "WHERE qy_gb_dis_qy_corp_id = 'ww9778dea409045fe6'";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            System.out.println("=== 数据库中的私钥信息 ===");
            
            if (rs.next()) {
                String corpId = rs.getString("corpId");
                int keyLength = rs.getInt("keyLength");
                String keyStart = rs.getString("keyStart");
                String keyEnd = rs.getString("keyEnd");
                String fullKey = rs.getString("fullKey");
                
                System.out.println("企业ID: " + corpId);
                System.out.println("私钥长度: " + keyLength + " 字符");
                System.out.println("私钥开头100字符:");
                System.out.println(keyStart);
                System.out.println("私钥结尾100字符:");
                System.out.println(keyEnd);
                System.out.println("\n=== 完整私钥内容 ===");
                System.out.println(fullKey);
                System.out.println("=== 私钥内容结束 ===");
            } else {
                System.out.println("未找到企业ID为 ww9778dea409045fe6 的记录");
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.err.println("查询失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
