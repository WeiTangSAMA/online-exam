package com.online.exam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionDTO {

    private Long id;

    @NotBlank(message = "题型不能为空")
    private String type;

    private Long categoryId;

    @NotBlank(message = "题目内容不能为空")
    private String content;

    /** 选项JSON */
    private String options;

    @NotBlank(message = "答案不能为空")
    private String answer;

    @NotNull(message = "分值不能为空")
    private Integer score;

    private String analysis;
}
