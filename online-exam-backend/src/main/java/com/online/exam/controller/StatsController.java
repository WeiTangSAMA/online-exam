package com.online.exam.controller;

import com.online.exam.common.Result;
import com.online.exam.service.StatsService;
import com.online.exam.vo.StatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "统计接口")
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @Operation(summary = "成绩统计（分布、及格率、最高/最低/平均分）")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<StatsVO> stats(@RequestParam(required = false) Long paperId) {
        return Result.success(statsService.getStats(paperId));
    }
}
