package com.online.exam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.online.exam.common.Result;
import com.online.exam.dto.QuestionDTO;
import com.online.exam.service.QuestionService;
import com.online.exam.vo.QuestionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "题目接口")
@RestController
@RequestMapping("/api/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "分页查询题目")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping
    public Result<Page<QuestionVO>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        return Result.success(questionService.getQuestionPage(pageNum, pageSize, type, categoryId, keyword));
    }

    @Operation(summary = "根据ID查询题目")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/{id}")
    public Result<QuestionVO> get(@PathVariable Long id) {
        return Result.success(questionService.getQuestionById(id));
    }

    @Operation(summary = "新增题目")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody QuestionDTO questionDTO) {
        questionService.addQuestion(questionDTO);
        return Result.success();
    }

    @Operation(summary = "修改题目")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody QuestionDTO questionDTO) {
        questionService.updateQuestion(questionDTO);
        return Result.success();
    }

    @Operation(summary = "删除题目")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return Result.success();
    }
}
