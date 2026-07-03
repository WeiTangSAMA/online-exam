package com.online.exam.service;

import com.online.exam.vo.StatsVO;

public interface StatsService {

    /** 成绩统计（按试卷） */
    StatsVO getStats(Long paperId);
}
