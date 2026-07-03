package com.online.exam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.online.exam.vo.ScoreVO;

public interface ScoreService {

    /** 当前用户的成绩 */
    Page<ScoreVO> getMyScores(Long userId, int pageNum, int pageSize);

    /** 成绩排行（按试卷） */
    Page<ScoreVO> getRanking(Long paperId, int pageNum, int pageSize);

    /** 全部成绩（管理员/教师） */
    Page<ScoreVO> getAllScores(int pageNum, int pageSize, Long paperId, Long userId);
}
