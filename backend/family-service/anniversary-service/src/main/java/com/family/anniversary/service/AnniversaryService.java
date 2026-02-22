package com.family.anniversary.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.anniversary.entity.Anniversary;

import java.util.List;

public interface AnniversaryService extends IService<Anniversary> {
    List<Anniversary> listByFamilyId(Long familyId);
}
