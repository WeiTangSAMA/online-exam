package com.online.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("paper")
public class Paper {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private Integer totalScore;

    /** 考试时长(分钟) */
    private Integer duration;

    /** 及格分数 */
    private Integer passScore;

    /** 0=草稿 1=已发布 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
