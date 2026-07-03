package com.online.exam.vo;

import lombok.Data;

@Data
public class ScoreDetailVO {

    private Long recordId;
    private String paperTitle;
    private Integer score;
    private Integer totalScore;
    private String submitTime;
    /** 答题明细列表 */
    private java.util.List<AnswerDetailVO> answers;
}
