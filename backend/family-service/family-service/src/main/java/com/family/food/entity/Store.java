package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商店信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("store")
public class Store extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    private String storeName;
    private String address;
    private String phone;
    private Double latitude;
    private Double longitude;
    private String category;
    private Integer status;
    
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
