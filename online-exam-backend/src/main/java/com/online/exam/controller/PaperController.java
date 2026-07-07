package com.online.exam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.online.exam.common.Result;
import com.online.exam.dto.PaperDTO;
import com.online.exam.entity.Paper;
import com.online.exam.service.PaperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "试卷接口")
@RestController
@RequestMapping("/api/paper")
@RequiredArgsConstructor
public class PaperController {

    private final PaperService paperService;

    @Operation(summary = "分页查询试卷")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping
    public Result<Page<Paper>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.success(paperService.getPaperPage(pageNum, pageSize, status));
    }

    @Operation(summary = "获取已发布试卷（学生可见）")
    @GetMapping("/published")
    public Result<Page<Paper>> published(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(paperService.getPaperPage(pageNum, pageSize, 1));
    }

    @Operation(summary = "根据ID查询试卷")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/{id}")
    public Result<Paper> get(@PathVariable Long id) {
        return Result.success(paperService.getPaperById(id));
    }

    @Operation(summary = "新增试卷（含组卷题目）")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping
    public Result<Long> save(@Valid @RequestBody PaperDTO paperDTO) {
        return Result.success(paperService.savePaper(paperDTO));
    }

    @Operation(summary = "修改试卷")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody PaperDTO paperDTO) {
        paperService.updatePaper(paperDTO);
        return Result.success();
    }

    @Operation(summary = "绑定试卷题目")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping("/{id}/questions")
    public Result<Void> bindQuestions(@PathVariable Long id, @RequestBody List<Long> questionIds) {
        paperService.bindQuestions(id, questionIds);
        return Result.success();
    }

    @Operation(summary = "发布试卷")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PutMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        paperService.publishPaper(id);
        return Result.success();
    }

    @Operation(summary = "删除试卷")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        paperService.deletePaper(id);
        return Result.success();
    }
}
