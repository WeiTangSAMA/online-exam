package com.online.exam.dto;

import lombok.Data;

@Data
public class ExamAnswerDTO {

    private Long questionId;
    private String userAnswer;
}
