package com.online.exam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PaperDTO {

    private Long id;

    @NotBlank(message = "试卷标题不能为空")
    private String title;

    private Integer totalScore;

    @NotNull(message = "考试时长不能为空")
    private Integer duration;

    private Integer passScore;

    private Integer status;

    /** 试卷包含的题目ID列表（用于组卷） */
    private List<Long> questionIds;
}
