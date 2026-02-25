package com.family.file.enums;

import lombok.Getter;

/**
 * 文件类型枚举
 */
@Getter
public enum FileTypeEnum {
    
    IMAGE("image", "图片"),
    VIDEO("video", "视频"),
    AUDIO("audio", "音频"),
    DOCUMENT("document", "文档"),
    ARCHIVE("archive", "压缩包"),
    OTHER("other", "其他");
    
    private final String code;
    private final String desc;
    
    FileTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /**
     * 根据文件名获取文件类型
     */
    public static FileTypeEnum getByFileName(String fileName) {
        if (fileName == null) {
            return OTHER;
        }
        String lower = fileName.toLowerCase();
        if (lower.matches(".*\\.(jpg|jpeg|png|gif|bmp|webp|svg)$")) {
            return IMAGE;
        } else if (lower.matches(".*\\.(mp4|avi|mov|wmv|flv|mkv|webm)$")) {
            return VIDEO;
        } else if (lower.matches(".*\\.(mp3|wav|flac|aac|ogg|wma)$")) {
            return AUDIO;
        } else if (lower.matches(".*\\.(doc|docx|pdf|txt|xls|xlsx|ppt|pptx|csv|md)$")) {
            return DOCUMENT;
        } else if (lower.matches(".*\\.(zip|rar|7z|tar|gz|bz2)$")) {
            return ARCHIVE;
        }
        return OTHER;
    }
}
