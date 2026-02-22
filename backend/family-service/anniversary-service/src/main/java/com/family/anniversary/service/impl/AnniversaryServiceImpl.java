package com.family.anniversary.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.anniversary.entity.Anniversary;
import com.family.anniversary.mapper.AnniversaryMapper;
import com.family.anniversary.service.AnniversaryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnniversaryServiceImpl extends ServiceImpl<AnniversaryMapper, Anniversary> implements AnniversaryService {
    
    @Override
    public List<Anniversary> listByFamilyId(Long familyId) {
        return lambdaQuery().eq(Anniversary::getFamilyId, familyId).list();
    }
}
