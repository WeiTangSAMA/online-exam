package com.online.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("question")
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** SINGLE / MULTIPLE / JUDGE */
    private String type;

    private Long categoryId;

    private String content;

    /** 选项JSON数组 */
    private String options;

    /** 正确答案 */
    private String answer;

    private Integer score;

    /** 解析 */
    private String analysis;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
