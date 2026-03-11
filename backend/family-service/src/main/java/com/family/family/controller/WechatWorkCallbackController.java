package com.family.family.controller;

import com.family.family.util.WechatWorkCrypt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * 企业微信回调接口
 * 用于接收企业微信推送的消息和事件
 */
@Slf4j
@RestController
@RequestMapping("/api/wechat")
public class WechatWorkCallbackController {

    @Value("${wechat.work.token:K7GwvTkfl10037Kbibjl0WGh}")
    private String token;

    @Value("${wechat.work.aeskey:zU2ayEoYmfuRahh3eQ1D2n1PWSIgIiXTCUKtbxFpB5n}")
    private String aesKey;
    
    @Value("${wechat.work.corpid:ww6c1c7590db91ef85}")
    private String corpId;

    /**
     * 企业微信URL验证（GET请求）
     * echostr是加密的，需要解密后返回
     */
    @GetMapping("/callback")
    public String verifyUrl(
            @RequestParam("msg_signature") String signature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam("echostr") String echostr) {
        
        log.info("企业微信URL验证请求: signature={}, timestamp={}, nonce={}, echostr={}", 
                signature, timestamp, nonce, echostr);
        
        try {
            // 1. 验证签名
            boolean valid = WechatWorkCrypt.verifySignature(token, timestamp, nonce, echostr, signature);
            if (!valid) {
                log.error("签名验证失败");
                return "fail";
            }
            
            // 2. 解密echostr
            String decrypted = WechatWorkCrypt.decrypt(aesKey, corpId, echostr);
            log.info("解密成功，返回明文: {}", decrypted);
            
            // 3. 返回解密后的明文（不能加引号，不能带换行符）
            return decrypted;
            
        } catch (Exception e) {
            log.error("URL验证失败", e);
            return "fail";
        }
    }

    /**
     * 接收企业微信推送的消息（POST请求）
     */
    @PostMapping("/callback")
    public String receiveMessage(
            @RequestParam("msg_signature") String signature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestBody String body) {
        
        log.info("收到企业微信消息: signature={}, timestamp={}, nonce={}, body={}", 
                signature, timestamp, nonce, body);
        
        // 返回success表示接收成功
        return "success";
    }
}
