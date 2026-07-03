package com.online.exam.vo;

import lombok.Data;

@Data
public class ExamQuestionVO {

    private Long id;
    private String type;
    private String content;
    private String options;
    private Integer score;
    /** 前端展示用序号 */
    private Integer sortOrder;
}
