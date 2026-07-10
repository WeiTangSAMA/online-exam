package com.online.exam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.online.exam.common.Result;
import com.online.exam.security.SecurityUtils;
import com.online.exam.service.ScoreService;
import com.online.exam.vo.ScoreVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "成绩接口")
@RestController
@RequestMapping("/api/score")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    @Operation(summary = "我的成绩")
    @GetMapping("/mine")
    public Result<Page<ScoreVO>> mine(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(scoreService.getMyScores(SecurityUtils.getCurrentUserId(), pageNum, pageSize));
    }

    @Operation(summary = "成绩排行（按试卷）")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/ranking")
    public Result<Page<ScoreVO>> ranking(
            @RequestParam(required = false) Long paperId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(scoreService.getRanking(paperId, pageNum, pageSize));
    }

    @Operation(summary = "全部成绩（管理员/教师）")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/all")
    public Result<Page<ScoreVO>> all(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long paperId,
            @RequestParam(required = false) Long userId) {
        return Result.success(scoreService.getAllScores(pageNum, pageSize, paperId, userId));
    }
}
