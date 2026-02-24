package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("family_schedule")
public class Schedule {
    private Long id;
    private Long familyId;
    private Long userId;
    private Long creatorId;
    private String taskName;
    private Integer dayOfWeek;
    private String startDate;
    private String endDate;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}