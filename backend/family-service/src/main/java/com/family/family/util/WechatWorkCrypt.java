package com.family.family.util;

import javax.crypto.Cipher;
import java.util.Base64;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * 企业微信消息加解密工具
 */
public class WechatWorkCrypt {

    /**
     * 解密企业微信消息
     * @param encodingAesKey EncodingAESKey
     * @param corpId 企业ID (ReceiveId)
     * @param encryptedMsg 加密的消息 (Base64)
     * @return 解密后的明文
     */
    public static String decrypt(String encodingAesKey, String corpId, String encryptedMsg) throws Exception {
        // 1. Base64解码AESKey
        byte[] aesKey = Base64.getDecoder().decode(encodingAesKey + "=");
        
        // 2. Base64解码密文
        byte[] encrypted = Base64.getDecoder().decode(encryptedMsg);
        
        // 3. AES解密
        byte[] decrypted = aesDecrypt(encrypted, aesKey);
        
        // 4. 解析明文结构: random(16B) + msg_len(4B) + msg + receiveid
        // 跳过前16字节随机数
        byte[] msgLenBytes = Arrays.copyOfRange(decrypted, 16, 20);
        int msgLen = ByteBuffer.wrap(msgLenBytes).order(ByteOrder.BIG_ENDIAN).getInt();
        
        // 提取消息
        byte[] msgBytes = Arrays.copyOfRange(decrypted, 20, 20 + msgLen);
        String msg = new String(msgBytes, "UTF-8");
        
        // 5. 验证 receiveid
        byte[] receiveIdBytes = Arrays.copyOfRange(decrypted, 20 + msgLen, decrypted.length);
        String receiveId = new String(receiveIdBytes, "UTF-8");
        
        if (!receiveId.equals(corpId)) {
            throw new Exception("receiveid不匹配: " + receiveId + " != " + corpId);
        }
        
        return msg;
    }
    
    /**
     * AES-256-CBC解密
     */
    private static byte[] aesDecrypt(byte[] encrypted, byte[] aesKey) throws Exception {
        // IV 是 AESKey 前16字节
        byte[] iv = Arrays.copyOfRange(aesKey, 0, 16);
        
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        
        return cipher.doFinal(encrypted);
    }
    
    /**
     * 验证签名
     * @param token Token
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param encrypt 加密的消息
     * @param signature 收到的签名
     * @return 是否验证通过
     */
    public static boolean verifySignature(String token, String timestamp, String nonce, 
                                           String encrypt, String signature) throws Exception {
        // 1. 排序
        String[] arr = new String[]{token, timestamp, nonce, encrypt};
        Arrays.sort(arr);
        
        // 2. 拼接
        StringBuilder content = new StringBuilder();
        for (String s : arr) {
            content.append(s);
        }
        
        // 3. SHA1
        MessageDigest md = MessageDigest.getInstance("SHA1");
        byte[] digest = md.digest(content.toString().getBytes());
        
        // 4. 转16进制
        StringBuilder hexStr = new StringBuilder();
        for (byte b : digest) {
            String shaHex = Integer.toHexString(b & 0xFF);
            if (shaHex.length() < 2) {
                hexStr.append(0);
            }
            hexStr.append(shaHex);
        }
        
        return hexStr.toString().equals(signature);
    }
}
