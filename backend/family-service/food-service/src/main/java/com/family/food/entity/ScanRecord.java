package com.family.food.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 扫码识别记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("scan_record")
public class ScanRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 家庭ID
     */
    private Long familyId;
    
    /**
     * 扫码类型: barcode-条形码, qrcode-二维码, image-图片识别
     */
    private String scanType;
    
    /**
     * 扫码内容(条形码/二维码内容或图片Base64摘要)
     */
    private String scanContent;
    
    /**
     * 识别结果JSON
     */
    private String resultJson;
    
    /**
     * 识别状态: 1成功 0失败
     */
    private Integer status;
    
    /**
     * 失败原因
     */
    private String failReason;
    
    /**
     * 商品名称(识别成功时)
     */
    private String productName;
    
    /**
     * 是否已添加到库存
     */
    private Boolean addedToInventory;
}
