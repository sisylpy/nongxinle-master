package com.nongxinle.utils.aes;

import java.security.MessageDigest;
import java.util.Arrays;

public class SHA1 {

    /**
     * 用SHA1算法生成安全签名
     * @param token 票据
     * @param timestamp 时间戳
     * @param nonce 随机字符串
     * @param encrypt 密文
     * @return 安全签名
     * @throws AesException
     */
    public static String getSHA1(String token, String timestamp, String nonce, String encrypt) throws AesException
    {
        try {
            System.out.println("========== SHA1.getSHA1 签名计算过程 ==========");
            System.out.println("输入参数：");
            System.out.println("  token=[\"" + token + "\"]");
            System.out.println("  timestamp=[\"" + timestamp + "\"]");
            System.out.println("  nonce=[\"" + nonce + "\"]");
            System.out.println("  encrypt=[\"" + encrypt + "\"]");
            System.out.println("  encrypt长度=" + encrypt.length());
            
            String[] array = new String[] { token, timestamp, nonce, encrypt };
            StringBuffer sb = new StringBuffer();
            // 字符串排序
            Arrays.sort(array);
            System.out.println("排序后的顺序：");
            for (int i = 0; i < 4; i++) {
                sb.append(array[i]);
                System.out.println("  [" + i + "]=[\"" + array[i] + "\"]");
            }
            String str = sb.toString();
            System.out.println("拼接后的字符串长度=" + str.length());
            System.out.println("拼接后的字符串前100字符=[\"" + (str.length() > 100 ? str.substring(0, 100) : str) + "...\"]");
            
            // SHA1签名生成
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(str.getBytes());
            byte[] digest = md.digest();

            StringBuffer hexstr = new StringBuffer();
            String shaHex = "";
            for (int i = 0; i < digest.length; i++) {
                shaHex = Integer.toHexString(digest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexstr.append(0);
                }
                hexstr.append(shaHex);
            }
            String result = hexstr.toString();
            System.out.println("计算出的签名=[\"" + result + "\"]");
            System.out.println("===========================================");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new AesException(AesException.ComputeSignatureError);
        }
    }


}
