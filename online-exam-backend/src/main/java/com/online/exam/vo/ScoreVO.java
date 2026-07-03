package com.online.exam.vo;

import lombok.Data;

@Data
public class ScoreVO {

    private Long id;
    private Long userId;
    private String username;
    private String name;
    private Long paperId;
    private String paperTitle;
    private Integer score;
    private Integer totalScore;
    private String submitTime;
    /** 是否及格 */
    private Boolean passed;
    /** 考试记录ID（用于查看答题详情） */
    private Long recordId;
}
