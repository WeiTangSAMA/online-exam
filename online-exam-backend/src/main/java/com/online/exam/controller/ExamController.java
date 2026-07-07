package com.online.exam.controller;

import com.online.exam.common.Result;
import com.online.exam.dto.SubmitExamDTO;
import com.online.exam.security.SecurityUtils;
import com.online.exam.security.SecurityUser;
import com.online.exam.service.ExamService;
import com.online.exam.vo.ExamPaperVO;
import com.online.exam.vo.ScoreDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "考试接口")
@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @Operation(summary = "获取考试试卷（不含答案）")
    @GetMapping("/paper/{paperId}")
    public Result<ExamPaperVO> getExamPaper(@PathVariable Long paperId) {
        SecurityUser currentUser = SecurityUtils.getCurrentUser();
        boolean canViewDraft = currentUser.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())
                        || "ROLE_TEACHER".equals(authority.getAuthority()));
        return Result.success(examService.getExamPaper(paperId, canViewDraft));
    }

    @Operation(summary = "开始考试")
    @PostMapping("/start")
    public Result<Long> start(@RequestParam Long paperId) {
        Long recordId = examService.startExam(SecurityUtils.getCurrentUserId(), paperId);
        return Result.success(recordId);
    }

    @Operation(summary = "提交考试（自动评分）")
    @PostMapping("/submit")
    public Result<Integer> submit(@Valid @RequestBody SubmitExamDTO submitExamDTO) {
        Integer score = examService.submitExam(SecurityUtils.getCurrentUserId(), submitExamDTO);
        return Result.success("提交成功", score);
    }

    @Operation(summary = "查看答题详情")
    @GetMapping("/detail/{recordId}")
    public Result<ScoreDetailVO> detail(@PathVariable Long recordId) {
        SecurityUser currentUser = SecurityUtils.getCurrentUser();
        boolean canViewAll = currentUser.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())
                        || "ROLE_TEACHER".equals(authority.getAuthority()));
        return Result.success(examService.getExamDetail(recordId, currentUser.getId(), canViewAll));
    }
}
