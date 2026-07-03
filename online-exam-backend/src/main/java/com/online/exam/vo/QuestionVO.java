package com.online.exam.vo;

import lombok.Data;

@Data
public class QuestionVO {

    private Long id;
    private String type;
    private Long categoryId;
    private String categoryName;
    private String content;
    private String options;
    private String answer;
    private String score;
    private String analysis;
    private String createTime;
}
