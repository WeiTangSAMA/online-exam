package com.online.exam.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SubmitExamDTO {

    @NotNull(message = "考试记录ID不能为空")
    private Long recordId;

    /** 每题的答案 [{questionId, userAnswer}] */
    private List<ExamAnswerDTO> answers;
}
