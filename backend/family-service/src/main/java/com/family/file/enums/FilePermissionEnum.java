package com.family.file.enums;

import lombok.Getter;

/**
 * 文件权限枚举
 */
@Getter
public enum FilePermissionEnum {
    
    PRIVATE(0, "私有"),
    FAMILY_READ(1, "家庭可读"),
    FAMILY_WRITE(2, "家庭可读写"),
    PUBLIC_READ(3, "公开可读");
    
    private final Integer code;
    private final String desc;
    
    FilePermissionEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
