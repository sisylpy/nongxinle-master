package com.nongxinle.utils.aes;

import com.nongxinle.utils.QywechatEnum;
import com.nongxinle.utils.QywechatInfo;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

public class WXBizMsgCrypt {
    static Charset CHARSET = Charset.forName("utf-8");
    Base64 base64 = new Base64();
    byte[] aesKey;
    String token;
    String receiveid;

    /**
     * 构造函数
     *
     * @throws AesException 执行失败，请查看该异常的错误码和具体的错误信息
     */
    public WXBizMsgCrypt(final QywechatEnum qywechatEnum) throws AesException {
        this.token = qywechatEnum.getToken();
        this.receiveid = qywechatEnum.getCorpid();
        System.out.println("token====" + qywechatEnum.getToken());
        System.out.println("receiveid====dddvvvv" + qywechatEnum.getCorpid());
        String encodingAesKey = qywechatEnum.getEncodingAESKey();
        System.out.println("encodingAesKeyencodingAesKey==" + encodingAesKey);
        if (encodingAesKey.length() != 43) {
            throw new AesException(AesException.IllegalAesKey);
        }
        aesKey = Base64.decodeBase64(encodingAesKey + "=");

    }


    // 生成4个字节的网络字节序
    byte[] getNetworkBytesOrder(int sourceNumber) {
        byte[] orderBytes = new byte[4];
        orderBytes[3] = (byte) (sourceNumber & 0xFF);
        orderBytes[2] = (byte) (sourceNumber >> 8 & 0xFF);
        orderBytes[1] = (byte) (sourceNumber >> 16 & 0xFF);
        orderBytes[0] = (byte) (sourceNumber >> 24 & 0xFF);
        return orderBytes;
    }

    // 还原4个字节的网络字节序
    int recoverNetworkBytesOrder(byte[] orderBytes) {
        int sourceNumber = 0;
        for (int i = 0; i < 4; i++) {
            sourceNumber <<= 8;
            sourceNumber |= orderBytes[i] & 0xff;
        }
        return sourceNumber;
    }

    // 随机生成16位字符串
    String getRandomStr() {
        String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 16; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 对明文进行加密.
     *
     * @param text 需要加密的明文
     * @return 加密后base64编码的字符串
     * @throws AesException aes加密失败
     */
    String encrypt(String randomStr, String text) throws AesException {
        ByteGroup byteCollector = new ByteGroup();
        byte[] randomStrBytes = randomStr.getBytes(CHARSET);
        byte[] textBytes = text.getBytes(CHARSET);
        byte[] networkBytesOrder = getNetworkBytesOrder(textBytes.length);
        byte[] receiveidBytes = receiveid.getBytes(CHARSET);

        // randomStr + networkBytesOrder + text + receiveid
        byteCollector.addBytes(randomStrBytes);
        byteCollector.addBytes(networkBytesOrder);
        byteCollector.addBytes(textBytes);
        byteCollector.addBytes(receiveidBytes);

        // ... + pad: 使用自定义的填充方式对明文进行补位填充
        byte[] padBytes = PKCS7Encoder.encode(byteCollector.size());
        byteCollector.addBytes(padBytes);

        // 获得最终的字节流, 未加密
        byte[] unencrypted = byteCollector.toBytes();

        try {
            // 设置加密模式为AES的CBC模式
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec iv = new IvParameterSpec(aesKey, 0, 16);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);

            // 加密
            byte[] encrypted = cipher.doFinal(unencrypted);

            // 使用BASE64对加密后的字符串进行编码
            String base64Encrypted = base64.encodeToString(encrypted);

            return base64Encrypted;
        } catch (Exception e) {
            e.printStackTrace();
            throw new AesException(AesException.EncryptAESError);
        }
    }

    /**
     * 对密文进行解密.
     *
     * @param text 需要解密的密文
     * @return 解密得到的明文
     * @throws AesException aes解密失败
     */
    String decrypt(String text) throws AesException {
        System.out.println("decrypt-----------------------------");
        byte[] original;
        try {
            // 设置解密模式为AES的CBC模式
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec key_spec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec iv = new IvParameterSpec(Arrays.copyOfRange(aesKey, 0, 16));
            cipher.init(Cipher.DECRYPT_MODE, key_spec, iv);

            // 使用BASE64对密文进行解码
            byte[] encrypted = Base64.decodeBase64(text);

            // 解密
            original = cipher.doFinal(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AesException(AesException.DecryptAESError);
        }

        String xmlContent, from_receiveid;
        try {
            // 去除补位字符
            byte[] bytes = PKCS7Encoder.decode(original);

            // 分离16位随机字符串,网络字节序和receiveid
            byte[] networkOrder = Arrays.copyOfRange(bytes, 16, 20);

            int xmlLength = recoverNetworkBytesOrder(networkOrder);

            xmlContent = new String(Arrays.copyOfRange(bytes, 20, 20 + xmlLength), CHARSET);
            from_receiveid = new String(Arrays.copyOfRange(bytes, 20 + xmlLength, bytes.length),
                    CHARSET);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AesException(AesException.IllegalBuffer);
        }

        // receiveid不相同的情况
        if (!from_receiveid.equals(receiveid)) {
            System.out.println("receiveidreceiveid======   " + receiveid);
            System.out.println("from_receiveidfrom_receiveid======  " + from_receiveid);
            System.out.println("zhelireceveiddewent-------------------------------------guess 企业没有认证");
            throw new AesException(AesException.ValidateCorpidError);
        }

        return xmlContent;

    }

    /**
     * 将企业微信回复用户的消息加密打包.
     * <ol>
     * 	<li>对要发送的消息进行AES-CBC加密</li>
     * 	<li>生成安全签名</li>
     * 	<li>将消息密文和安全签名打包成xml格式</li>
     * </ol>
     *
     * @param replyMsg 企业微信待回复用户的消息，xml格式的字符串
     * @param timeStamp 时间戳，可以自己生成，也可以用URL参数的timestamp
     * @param nonce 随机串，可以自己生成，也可以用URL参数的nonce
     *
     * @return 加密后的可以直接回复用户的密文，包括msg_signature, timestamp, nonce, encrypt的xml格式的字符串
     * @throws AesException 执行失败，请查看该异常的错误码和具体的错误信息
     */
    public String EncryptMsg(String replyMsg, String timeStamp, String nonce) throws AesException {
        // 加密
        String encrypt = encrypt(getRandomStr(), replyMsg);

        // 生成安全签名
        if (timeStamp == "") {
            timeStamp = Long.toString(System.currentTimeMillis());
        }

        String signature = SHA1.getSHA1(token, timeStamp, nonce, encrypt);

        // System.out.println("发送给平台的签名是: " + signature[1].toString());
        // 生成发送的xml
        String result = XMLParse.generate(encrypt, signature, timeStamp, nonce);
        return result;
    }

    /**
     * 检验消息的真实性，并且获取解密后的明文.
     * <ol>
     * 	<li>利用收到的密文生成安全签名，进行签名验证</li>
     * 	<li>若验证通过，则提取xml中的加密消息</li>
     * 	<li>对消息进行解密</li>
     * </ol>
     *
     * @param qywechatInfo  bean
     * @return 解密后的原文
     * @throws AesException 执行失败，请查看该异常的错误码和具体的错误信息
     */
    public String decryptMsg(final QywechatInfo qywechatInfo)
            throws AesException {

        // 密钥，公众账号的app secret
        // 提取密文
        Object[] encrypt = XMLParse.extract(qywechatInfo.getSPostData());
        /**
         * @param msgSignature 签名串，对应URL参数的msg_signature
         * @param timeStamp 时间戳，对应URL参数的timestamp
         * @param nonce 随机串，对应URL参数的nonce
         * @param postData 密文，对应POST请求的数据
         */
        // 验证安全签名
        System.out.println("toeken======" + token);
        System.out.println("getTimestamp=====" + qywechatInfo.getTimestamp());
        System.out.println("getNonce=====" + qywechatInfo.getNonce());
        System.out.println("encrypt=====" + encrypt[1].toString());
        String signature = SHA1.getSHA1(token, qywechatInfo.getTimestamp(), qywechatInfo.getNonce(), encrypt[1].toString());

        // 和URL中的签名比较是否相等
         System.out.println("第三方校验签名：" + signature);

        if (!signature.equals(qywechatInfo.getMsgSignature())) {
            System.out.println("signature=====" + signature + "qywechatInfo.getMsgSignature()=========：" + qywechatInfo.getMsgSignature());
            throw new AesException(AesException.ValidateSignatureError);
        }

        // 解
        System.out.println("encryptencryptwhatisthis--------------------------" + encrypt);
        System.out.println("decryptMsgresultltl--------------------------" + encrypt[1].toString());
        String result = decrypt(encrypt[1].toString());
        return result;
    }

    /**
     * 验证URL
     * @param msgSignature 签名串，对应URL参数的msg_signature
     * @param timeStamp 时间戳，对应URL参数的timestamp
     * @param nonce 随机串，对应URL参数的nonce
     * @param echoStr 随机串，对应URL参数的echostr
     *
     * @return 解密之后的echostr
     * @throws AesException 执行失败，请查看该异常的错误码和具体的错误信息
     */
    public String verifyURL(String msgSignature, String timeStamp, String nonce, String echoStr)
            throws AesException {
        System.out.println("verifyURLverifyURLverifyURL------------------");
        String signature = SHA1.getSHA1(token, timeStamp, nonce, echoStr);

        if (!signature.equals(msgSignature)) {
            throw new AesException(AesException.ValidateSignatureError);
        }

        System.out.println("verifyURL-------resultresultresultresult==========");
        String result = decrypt(echoStr);
        return result;
    }

    static class PKCS7Encoder {
        static Charset CHARSET = Charset.forName("utf-8");
        static int BLOCK_SIZE = 32;

        /**
         * 获得对明文进行补位填充的字节.
         *
         * @param count 需要进行填充补位操作的明文字节个数
         * @return 补齐用的字节数组
         */
        static byte[] encode(int count) {
            // 计算需要填充的位数
            int amountToPad = BLOCK_SIZE - (count % BLOCK_SIZE);
            if (amountToPad == 0) {
                amountToPad = BLOCK_SIZE;
            }
            // 获得补位所用的字符
            char padChr = chr(amountToPad);
            String tmp = new String();
            for (int index = 0; index < amountToPad; index++) {
                tmp += padChr;
            }
            return tmp.getBytes(CHARSET);
        }

        /**
         * 删除解密后明文的补位字符
         *
         * @param decrypted 解密后的明文
         * @return 删除补位字符后的明文
         */
        static byte[] decode(byte[] decrypted) {
            int pad = (int) decrypted[decrypted.length - 1];
            if (pad < 1 || pad > 32) {
                pad = 0;
            }
            return Arrays.copyOfRange(decrypted, 0, decrypted.length - pad);
        }

        /**
         * 将数字转化成ASCII码对应的字符，用于对明文进行补码
         *
         * @param a 需要转化的数字
         * @return 转化得到的字符
         */
        static char chr(int a) {
            byte target = (byte) (a & 0xFF);
            return (char) target;
        }

    }

}