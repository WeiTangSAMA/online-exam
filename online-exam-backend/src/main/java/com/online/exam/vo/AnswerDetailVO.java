package com.online.exam.vo;

import lombok.Data;

@Data
public class AnswerDetailVO {

    private Long questionId;
    private String type;
    private String content;
    private String options;
    private String correctAnswer;
    private String userAnswer;
    private Integer score;
    private Integer isCorrect;
    private String analysis;
}
