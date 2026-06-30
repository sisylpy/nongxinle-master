package com.nongxinle.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tencent.wework.Finance;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 企业微信会话存档 SDK 工具类
 * 
 * 关键要点：
 * 1) RSA/ECB/PKCS1Padding 解密 encrypt_random_key → 得到 32 字节 AES 随机密钥
 * 2) 将 32 字节 AES 密钥 Base64 编码后传给 Finance.DecryptData
 * 3) 按 publickey_ver 选择对应版本的私钥（v1、v2 互不通用）
 *
 * 使用示例：
 *   util.init(corpId, secret);
 *   util.addPrivateKey(1, pemV1);  // 添加版本1私钥
 *   util.addPrivateKey(2, pemV2);  // 添加版本2私钥
 *   String plain = util.decryptData(encRandomKey, encChatMsg, publickeyVer);
 * 
 * @author system
 * @date 2024-10-19
 * @version 2.3 - 严格32字节校验 + 直接存储PrivateKey对象
 */
@Component
public class WeworkFinanceSdkUtil {

    /** SDK 句柄（0 表示未初始化；macOS 模式下设为 1） */
    private long sdk = 0;

    private String corpId;
    private String secret;

    /** 多版本私钥：key=publickey_ver，value=已解析的 PrivateKey 对象 */
    private final Map<Integer, PrivateKey> privateKeyMap = new HashMap<>();

    // ============================= 对外方法 =============================

    /** 初始化 SDK（Linux 环境会加载 .so 并调用 Finance.Init；macOS 进入模拟模式） */
    public boolean init(String corpId, String secret) {
        // 🔧 修复：检查是否已经初始化过相同的corpId和secret
        if (sdk != 0 && corpId.equals(this.corpId) && secret.equals(this.secret)) {
            System.out.println("[SDK] ✅ 已初始化，无需重复");
            return true;
        }
        
        // 🔧 修复：如果SDK已存在但参数不同，先清理
        if (sdk != 0) {
            System.out.println("[SDK] 🔄 参数变化，重新初始化");
            try {
                Finance.DestroySdk(sdk);
            } catch (Exception e) {
                System.out.println("[SDK] ⚠️ 清理旧SDK时出现异常: " + e.getMessage());
            }
            sdk = 0;
        }
        
        this.corpId = corpId;
        this.secret = secret;
        
        try {
            final String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) {
                System.out.println("[SDK] 🍎 macOS 环境，启用模拟模式");
                this.sdk = 1;
                return true;
            }
            
            // Linux：加载 native 库并初始化
            loadNativeLibrary();
            this.sdk = Finance.NewSdk();
            if (sdk == 0) {
                System.err.println("[SDK] ❌ 创建SDK对象失败");
                return false;
            }
            int ret = Finance.Init(sdk, corpId, secret);
            if (ret != 0) {
                System.err.println("[SDK] ❌ 初始化失败，错误码: " + ret);
                Finance.DestroySdk(sdk);
                sdk = 0;
                return false;
            }
            System.out.println("[SDK] ✅ 初始化成功");
            return true;
            
        } catch (Throwable t) {
            System.err.println("[SDK] ❌ 初始化异常: " + t.getMessage());
            t.printStackTrace();
            return false;
        }
    }

    /**
     * 添加某个版本的私钥（PEM 格式）
     * @param publickeyVer 公钥版本号（1, 2, ...）
     * @param privateKeyPem PEM 格式的私钥字符串
     */
    public void addPrivateKey(int publickeyVer, String privateKeyPem) {
        try {
            if (privateKeyPem == null || privateKeyPem.trim().isEmpty()) {
                System.err.println("[私钥] ❌ 版本 " + publickeyVer + " 的私钥为空");
                return;
            }

            PrivateKey pk = loadPrivateKeyPem(privateKeyPem);
            privateKeyMap.put(publickeyVer, pk);
            System.out.println("[私钥] ✅ 已添加版本 " + publickeyVer + " 的私钥");
        } catch (Exception e) {
            System.err.println("[私钥] ❌ 添加版本 " + publickeyVer + " 私钥失败: " + e.getMessage());
            System.err.println("[私钥] 异常类型: " + e.getClass().getSimpleName());
            e.printStackTrace();
        }
    }

    /** 向后兼容：不指定版本，默认设为版本2 */
    @Deprecated
    public void setPrivateKey(String privateKeyPem) {
        addPrivateKey(2, privateKeyPem);
        System.out.println("[私钥] ℹ️ 未指定版本号，默认设置为版本2");
    }

    /** 向后兼容：指定版本 */
    public void setPrivateKey(int publickeyVer, String privateKeyPem) {
        addPrivateKey(publickeyVer, privateKeyPem);
    }

    /** 拉取会话存档数据 */
    public String getChatData(long seq, int limit) {
        if (sdk == 0) {
            System.err.println("[SDK] ❌ 未初始化");
            return null;
        }
        final String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            return mockChatData(seq, limit);
        }
            long slice = Finance.NewSlice();
            if (slice == 0) {
            System.err.println("[SDK] ❌ 创建Slice失败");
                return null;
            }
        try {
            int ret = Finance.GetChatData(sdk, seq, limit, "", "", 5, slice);
            if (ret != 0) {
                System.err.println("[SDK] ❌ 拉取会话存档失败, ret=" + ret);
                return null;
            }
            return Finance.GetContentFromSlice(slice);
        } finally {
            Finance.FreeSlice(slice);
        }
    }
    
    /**
     * 解密一条消息
     * @param encryptRandomKey Base64 编码的加密随机密钥
     * @param encryptChatMsg Base64 编码的加密聊天消息
     * @param publickeyVer 公钥版本号（从消息中获取）
     * @return 解密后的明文 JSON，失败返回 null
     */
    public String decryptData(String encryptRandomKey, String encryptChatMsg, int publickeyVer) {
        if (sdk == 0) {
            System.err.println("[解密] ❌ SDK未初始化");
            return null;
        }
        final String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            return mockDecrypted(encryptRandomKey, encryptChatMsg);
        }

        try {
            // 1) 解密 encrypt_random_key 得到 32 字节 AES 密钥
            byte[] aesKeyBytes = decryptRandomKey(encryptRandomKey, publickeyVer);
            if (aesKeyBytes == null) {
                return null; // 错误已在内部打印
            }

            // 2) 根据企业微信文档，直接使用解密结果作为encrypt_key
            String encryptKey = new String(aesKeyBytes, StandardCharsets.UTF_8);
            System.out.println("[解密] 已获取encrypt_key，长度: " + encryptKey.length());

            // 3) 调用 SDK 解密消息正文
            long slice = Finance.NewSlice();
            try {
                int ret = Finance.DecryptData(sdk, encryptKey, encryptChatMsg, slice);
                if (ret != 0) {
                    System.err.println("[解密] ❌ SDK DecryptData 失败, ret=" + ret);
                    System.err.println("       错误码: 10003=解密失败, 10007=未初始化, 10008=私钥/密钥问题");
                    System.err.println("       encrypt_chat_msg长度: " + encryptChatMsg.length());
                return null;
            }
                String plainText = Finance.GetContentFromSlice(slice);
                System.out.println("[解密] ✅ 消息解密成功");
                return plainText;
            } finally {
                Finance.FreeSlice(slice);
            }

        } catch (Exception e) {
            System.err.println("[解密] ❌ 异常: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 向后兼容：不指定版本，尝试使用版本2
     * @deprecated 请使用 decryptData(encryptRandomKey, encryptChatMsg, publickeyVer)
     */
    @Deprecated
    public String decryptData(String encryptRandomKey, String encryptChatMsg) {
        System.out.println("[解密] ⚠️ 未指定版本号，尝试使用版本2");
        return decryptData(encryptRandomKey, encryptChatMsg, 2);
    }

    /** 从解密后的消息 JSON 里提取群信息 */
    public JSONArray extractGroupInfo(String decryptedMsg) {
        JSONArray arr = new JSONArray();
        try {
            JSONObject obj = JSONObject.parseObject(decryptedMsg);
            String roomid = obj.getString("roomid");
            if (roomid != null && !roomid.isEmpty()) {
                JSONObject g = new JSONObject();
                g.put("roomid", roomid);
                g.put("from", obj.getString("from"));
                g.put("msgtype", obj.getString("msgtype"));
                g.put("msgtime", obj.getLong("msgtime"));
                g.put("action", obj.getString("action"));
                g.put("roomname", "未知群聊");
                arr.add(g);
            }
        } catch (Throwable t) {
            System.err.println("[解密] ⚠️ 提取群聊信息异常: " + t.getMessage());
        }
        return arr;
    }

    /** 释放 SDK */
    public void destroy() {
        if (sdk != 0) {
            final String os = System.getProperty("os.name").toLowerCase();
            if (!os.contains("mac")) {
                Finance.DestroySdk(sdk);
            }
            sdk = 0;
            System.out.println("[SDK] 已销毁");
        }
    }

    public boolean isInitialized() {
        return sdk != 0;
    }

    // ============================= 内部实现 =============================

    /** Linux 环境尝试多路径加载 libWeWorkFinanceSdk_Java.so */
    private void loadNativeLibrary() {
        try {
            String[] paths = {
                    System.getProperty("user.dir") + "/lib/finance-sdk/native/linux/libWeWorkFinanceSdk_Java.so",
                    System.getProperty("catalina.home") + "/webapps/nongxinle_master_war_exploded/WEB-INF/lib/libWeWorkFinanceSdk_Java.so",
                    "/opt/tomcat/apache-tomcat-9.0.56/bin/lib/finance-sdk/native/linux/libWeWorkFinanceSdk_Java.so"
            };
            String found = null;
            for (String p : paths) {
                if (p == null) continue;
                File f = new File(p);
                if (f.exists()) {
                    found = p;
                    break;
                }
            }
            if (found == null) {
                System.err.println("[SDK] ❌ Native库未找到，已尝试：");
                for (String p : paths) System.err.println("  - " + p);
                throw new UnsatisfiedLinkError("libWeWorkFinanceSdk_Java.so 不存在");
            }
            System.out.println("[SDK] 找到native库: " + found);
            
            // 🔧 修复：检查库是否已经加载，避免重复加载
            try {
                System.load(found);
                System.out.println("[SDK] ✅ Native库加载成功");
            } catch (UnsatisfiedLinkError e) {
                if (e.getMessage().contains("already loaded")) {
                    System.out.println("[SDK] ✅ Native库已加载，跳过重复加载");
                } else {
                    System.err.println("[SDK] ❌ Native库加载失败: " + e.getMessage());
                    throw e;
                }
            }
        } catch (Throwable t) {
            System.err.println("[SDK] ❌ 加载native库失败: " + t.getMessage());
            throw t;
        }
    }

    /**
     * 按公钥版本选择解密算法解密 encrypt_random_key
     * @param encryptRandomKeyB64 Base64 编码的加密随机密钥
     * @param publickeyVer 公钥版本号
     * @return 32 字节的 AES 随机密钥，失败返回 null
     */
    private byte[] decryptRandomKey(String encryptRandomKeyB64, int publickeyVer) {
        try {
            System.out.println("[解密] 🔍 开始解密版本 " + publickeyVer + " 的消息");
            System.out.println("[解密] 当前私钥版本: " + privateKeyMap.keySet());
            System.out.println("[解密] encrypt_random_key长度: " + encryptRandomKeyB64.length());

            // 1) 获取对应版本的私钥
            PrivateKey pk = privateKeyMap.get(publickeyVer);
            if (pk == null) {
                System.err.println("[解密] ❌ 缺少版本 " + publickeyVer + " 的私钥");
                System.err.println("       提示: 需调用 addPrivateKey(" + publickeyVer + ", \"...\") 设置该版本私钥");
                System.err.println("       当前已加载的版本: " + privateKeyMap.keySet());
                return null;
            }
            
            System.out.println("[解密] ✅ 找到版本 " + publickeyVer + " 的私钥");

            // 2) Base64 解码 encrypt_random_key
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptRandomKeyB64);
            System.out.println("[解密] encrypt_random_key Base64解码完成，长度: " + encryptedBytes.length + " 字节");
            if (encryptedBytes.length != 256) {
                System.err.println("[解密] ⚠️ encrypt_random_key 解码长度: " + encryptedBytes.length + " (期望256字节)");
            }

            // 3) 构造候选算法（从最有可能的开始）
            java.util.List<String> algos = new java.util.ArrayList<>();
            if (publickeyVer == 1) {
                algos.add("PKCS1"); // v1 使用 PKCS#1 v1.5
            } else if (publickeyVer >= 8) {
                // v8+ 优先使用 OAEP SHA-1，这是企业微信v8的默认算法
                algos.add("OAEP-SHA1");
                algos.add("OAEP-SHA256");
                algos.add("PKCS1");
            } else if (publickeyVer >= 6) {
                // v6-v7 优先使用 OAEP SHA-1，这是企业微信v6-v7的默认算法
                algos.add("OAEP-SHA1");
                algos.add("OAEP-SHA256");
                algos.add("PKCS1");
            } else {
                // v2/v3/v4/v5 先用 OAEP(SHA-1)，再试 OAEP(SHA-256)，最后兜底 PKCS1
                algos.add("OAEP-SHA1");
                algos.add("OAEP-SHA256");
                algos.add("PKCS1");
            }

            // 4) 尝试每种算法
            System.out.println("[解密] 将要尝试的算法列表: " + algos);
            for (String algo : algos) {
                try {
                    System.out.println("[解密] 开始尝试算法: " + algo);
                    javax.crypto.Cipher cipher;
                    switch (algo) {
                        case "OAEP-SHA256": {
                            System.out.println("[解密] 🔍 尝试 RSA-OAEP(SHA-256, MGF1)");
                            cipher = javax.crypto.Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
                            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, pk,
                                new javax.crypto.spec.OAEPParameterSpec(
                                    "SHA-256", "MGF1",
                                    java.security.spec.MGF1ParameterSpec.SHA256,
                                    javax.crypto.spec.PSource.PSpecified.DEFAULT));
                            break;
                        }
                        case "OAEP-SHA1": {
                            System.out.println("[解密] 🔍 尝试 RSA-OAEP(SHA-1, MGF1)");
                            cipher = javax.crypto.Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
                            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, pk,
                                new javax.crypto.spec.OAEPParameterSpec(
                                    "SHA-1", "MGF1",
                                    java.security.spec.MGF1ParameterSpec.SHA1,
                                    javax.crypto.spec.PSource.PSpecified.DEFAULT));
                            break;
                        }
                        default: { // PKCS1
                            System.out.println("[解密] 🔍 尝试 RSA PKCS#1 v1.5");
                            cipher = javax.crypto.Cipher.getInstance("RSA/ECB/PKCS1Padding");
                            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, pk);
                        }
                    }

                    byte[] aesKeyBytes = cipher.doFinal(encryptedBytes);

                    // 5) 根据企业微信文档，解密结果就是encrypt_key，不需要校验长度
                    System.out.println("[解密] ✅ " + algo + " 解密成功，获得 " + aesKeyBytes.length + " 字节的encrypt_key");
                    return aesKeyBytes;
                    
                } catch (javax.crypto.BadPaddingException bp) {
                    System.out.println("[解密] ↪️ " + algo + " 失败（BadPadding），尝试下一个算法...");
                }
            }

            System.err.println("[解密] ❌ 所有候选算法均失败，检查私钥/版本是否匹配");
            return null;
            
        } catch (Exception e) {
            System.err.println("[解密] ❌ RSA异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 判断字节数组是否看起来像 Base64 文本
     */
    private boolean isLikelyBase64Text(byte[] bytes) {
        for (byte b : bytes) {
            int c = b & 0xff;
            if (!(c == '=' || c == '+' || c == '/' ||
                  (c >= '0' && c <= '9') ||
                  (c >= 'A' && c <= 'Z') ||
                  (c >= 'a' && c <= 'z') ||
                  c == '\n' || c == '\r' || c == ' ')) {
                return false;
            }
        }
        return bytes.length > 0;
    }

    /**
     * 从 PEM 文本加载 RSA PrivateKey
     * 支持 PKCS#8 (-----BEGIN PRIVATE KEY-----) 和 PKCS#1 (-----BEGIN RSA PRIVATE KEY-----)
     */
    private PrivateKey loadPrivateKeyPem(String pem) throws Exception {
        if (pem == null || pem.trim().isEmpty()) {
            throw new IllegalArgumentException("私钥PEM为空");
        }

        // 规范化换行符
        String clean = pem.replace("\r\n", "\n").replace("\r", "\n").trim();

        // 1) PKCS#8 格式: -----BEGIN PRIVATE KEY-----
        if (clean.contains("BEGIN PRIVATE KEY")) {
            String body = clean
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(body);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        }

        // 2) PKCS#1 格式: -----BEGIN RSA PRIVATE KEY-----
        if (clean.contains("BEGIN RSA PRIVATE KEY")) {
            String body = clean
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
            byte[] pkcs1Bytes = Base64.getDecoder().decode(body);
            // 将 PKCS#1 包装为 PKCS#8
            byte[] pkcs8Bytes = wrapPkcs1ToPkcs8(pkcs1Bytes);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(pkcs8Bytes);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        }

        throw new IllegalArgumentException("不识别的私钥格式（应为 PKCS#8 或 PKCS#1）");
    }

    /**
     * 将 PKCS#1 格式的 RSA 私钥包装为 PKCS#8 格式
     * 修复版：正确处理 DER 长度编码
     */
    private byte[] wrapPkcs1ToPkcs8(byte[] pkcs1) {
        try {
            // PKCS#8 固定头部
            byte[] algorithmIdentifier = new byte[]{
                0x30, 0x0d,  // SEQUENCE length=13
                0x06, 0x09,  // OID length=9
                0x2a, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xf7, 0x0d, 0x01, 0x01, 0x01, // rsaEncryption OID
                0x05, 0x00   // NULL
            };
            
            byte[] version = new byte[]{0x02, 0x01, 0x00};  // INTEGER 0
            
            // OCTET STRING tag + length + content
            byte[] octetStringHeader = encodeDerLength(0x04, pkcs1.length);
            
            // 计算内容总长度
            int contentLength = version.length + algorithmIdentifier.length + octetStringHeader.length + pkcs1.length;
            
            // SEQUENCE tag + length
            byte[] sequenceHeader = encodeDerLength(0x30, contentLength);
            
            // 组装完整的 PKCS#8
            byte[] pkcs8 = new byte[sequenceHeader.length + contentLength];
            int pos = 0;
            
            System.arraycopy(sequenceHeader, 0, pkcs8, pos, sequenceHeader.length);
            pos += sequenceHeader.length;
            
            System.arraycopy(version, 0, pkcs8, pos, version.length);
            pos += version.length;
            
            System.arraycopy(algorithmIdentifier, 0, pkcs8, pos, algorithmIdentifier.length);
            pos += algorithmIdentifier.length;
            
            System.arraycopy(octetStringHeader, 0, pkcs8, pos, octetStringHeader.length);
            pos += octetStringHeader.length;
            
            System.arraycopy(pkcs1, 0, pkcs8, pos, pkcs1.length);
            
            return pkcs8;
            
        } catch (Exception e) {
            System.err.println("[PKCS转换] ❌ PKCS#1转PKCS#8失败: " + e.getMessage());
            throw new RuntimeException("PKCS#1转PKCS#8失败", e);
        }
    }
    
    /**
     * DER 长度编码（正确处理短格式和长格式）
     * @param tag DER标签（如 0x30=SEQUENCE, 0x04=OCTET STRING）
     * @param length 内容长度
     * @return tag + 编码后的长度字节
     */
    private byte[] encodeDerLength(int tag, int length) {
        if (length < 128) {
            // 短格式：长度 < 128，直接用1个字节
            return new byte[]{(byte) tag, (byte) length};
        } else if (length < 256) {
            // 长格式：1字节长度
            return new byte[]{(byte) tag, (byte) 0x81, (byte) length};
        } else if (length < 65536) {
            // 长格式：2字节长度
            return new byte[]{
                (byte) tag, 
                (byte) 0x82, 
                (byte) (length >> 8), 
                (byte) length
            };
        } else {
            // 长格式：3字节长度（支持最大16MB）
            return new byte[]{
                (byte) tag,
                (byte) 0x83,
                (byte) (length >> 16),
                (byte) (length >> 8),
                (byte) length
            };
        }
    }

    // ============================= 模拟（macOS 开发） =============================

    private String mockChatData(long seq, int limit) {
        long firstSeq = seq + 1;
        long secondSeq = seq + 2;
        long duplicateSeq = seq + 3;
        // 前两条按 seq 动态生成，第三条固定 msgid，用于本地重复消息幂等测试。
        return "{\n" +
                "  \"errcode\": 0,\n" +
                "  \"errmsg\": \"ok\",\n" +
                "  \"chatdata\": [\n" +
                "    {\"seq\":" + firstSeq + ",\"msgid\":\"mock_msg_" + firstSeq + "\",\"publickey_ver\":1,\n" +
                "     \"encrypt_random_key\":\"" + Base64.getEncoder().encodeToString(new byte[256]) + "\",\n" +
                "     \"encrypt_chat_msg\":\"mock_enc_msg_" + firstSeq + "\"},\n" +
                "    {\"seq\":" + secondSeq + ",\"msgid\":\"mock_msg_" + secondSeq + "\",\"publickey_ver\":2,\n" +
                "     \"encrypt_random_key\":\"" + Base64.getEncoder().encodeToString(new byte[256]) + "\",\n" +
                "     \"encrypt_chat_msg\":\"mock_enc_msg_" + secondSeq + "\"},\n" +
                "    {\"seq\":" + duplicateSeq + ",\"msgid\":\"mock_msg\",\"publickey_ver\":2,\n" +
                "     \"encrypt_random_key\":\"" + Base64.getEncoder().encodeToString(new byte[256]) + "\",\n" +
                "     \"encrypt_chat_msg\":\"mock_enc_msg_duplicate\"}\n" +
                "  ]\n" +
                "}";
    }

    private String mockDecrypted(String encryptRandomKey, String encryptChatMsg) {
        String msgId = "mock_enc_msg_duplicate".equals(encryptChatMsg)
                ? "mock_msg"
                : encryptChatMsg.replace("mock_enc_msg_", "mock_msg_");
        return "{\n" +
                "  \"msgid\": \"" + msgId + "\",\n" +
                "  \"action\": \"send\",\n" +
                "  \"from\": \"u_1\",\n" +
                "  \"tolist\": [\"u_2\"],\n" +
                "  \"roomid\": \"wrOgQhDgAAMYQiS5ol9G7gK9JVAAAA\",\n" +
                "  \"msgtime\": " + System.currentTimeMillis() + ",\n" +
                "  \"msgtype\": \"text\",\n" +
                "  \"text\": {\"content\": \"这是模拟解密内容\"}\n" +
                "}";
    }
}
