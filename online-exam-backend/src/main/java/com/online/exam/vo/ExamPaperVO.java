package com.online.exam.vo;

import lombok.Data;

import java.util.List;

@Data
public class ExamPaperVO {

    private Long id;
    private String title;
    private Integer totalScore;
    private Integer duration;
    private Integer passScore;
    /** 考试中的题目列表（不含答案） */
    private List<ExamQuestionVO> questions;
}
