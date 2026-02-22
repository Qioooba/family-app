
package com.family.calendar.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 贺卡生成控制器
 */
@RestController
@SaCheckLogin
@RequestMapping("/api/anniversary/card")
@RequiredArgsConstructor
public class AnniversaryCardController {
    
    /**
     * 生成贺卡图片
     * POST /api/anniversary/card
     */
    @PostMapping
    public Result<Map<String, String>> generateCard(@RequestBody CardRequest request) {
        try {
            // 创建简单贺卡图片
            int width = 800;
            int height = 600;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            
            // 背景渐变
            GradientPaint gradient = new GradientPaint(0, 0, new Color(255, 182, 193), 
                                                        width, height, new Color(255, 218, 185));
            g.setPaint(gradient);
            g.fillRect(0, 0, width, height);
            
            // 边框
            g.setColor(new Color(220, 20, 60));
            g.setStroke(new BasicStroke(5));
            g.drawRect(20, 20, width - 40, height - 40);
            
            // 标题
            g.setColor(Color.WHITE);
            g.setFont(new Font("宋体", Font.BOLD, 48));
            String title = request.getTitle() != null ? request.getTitle() : "纪念日快乐";
            FontMetrics fm = g.getFontMetrics();
            int titleX = (width - fm.stringWidth(title)) / 2;
            g.drawString(title, titleX, 150);
            
            // 内容
            g.setFont(new Font("宋体", Font.PLAIN, 32));
            String content = request.getContent() != null ? request.getContent() : "愿我们的美好时光永存";
            drawStringMultiLine(g, content, width - 100, 250);
            
            // 装饰
            g.setFont(new Font("Arial", Font.PLAIN, 60));
            g.drawString("❤", width / 2 - 30, 450);
            
            g.dispose();
            
            // 转换为Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
            
            Map<String, String> result = new HashMap<>();
            result.put("imageBase64", "data:image/png;base64," + base64Image);
            result.put("width", String.valueOf(width));
            result.put("height", String.valueOf(height));
            
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("生成贺卡失败: " + e.getMessage());
        }
    }
    
    private void drawStringMultiLine(Graphics2D g, String text, int maxWidth, int startY) {
        FontMetrics fm = g.getFontMetrics();
        int lineHeight = fm.getHeight();
        int y = startY;
        
        String[] words = text.split("");
        StringBuilder line = new StringBuilder();
        
        for (String word : words) {
            if (fm.stringWidth(line + word) < maxWidth) {
                line.append(word);
            } else {
                int x = (800 - fm.stringWidth(line.toString())) / 2;
                g.drawString(line.toString(), x, y);
                y += lineHeight;
                line = new StringBuilder(word);
            }
        }
        
        if (line.length() > 0) {
            int x = (800 - fm.stringWidth(line.toString())) / 2;
            g.drawString(line.toString(), x, y);
        }
    }
    
    public static class CardRequest {
        private String title;
        private String content;
        private String theme; // 主题样式
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
    }
}