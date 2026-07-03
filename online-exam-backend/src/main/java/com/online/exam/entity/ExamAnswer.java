package com.online.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("exam_answer")
public class ExamAnswer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recordId;

    private Long questionId;

    /** 用户作答 */
    private String userAnswer;

    /** 0=否 1=是 */
    private Integer isCorrect;

    /** 该题得分 */
    private Integer score;
}
