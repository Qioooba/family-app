package com.family.file.enums;

import lombok.Getter;

/**
 * 分享类型枚举
 */
@Getter
public enum ShareTypeEnum {
    
    LINK(1, "链接分享"),
    FAMILY(2, "家庭分享"),
    USER(3, "指定用户分享");
    
    private final Integer code;
    private final String desc;
    
    ShareTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
