package com.online.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.online.exam.entity.Paper;
import com.online.exam.entity.Score;
import com.online.exam.mapper.PaperMapper;
import com.online.exam.mapper.ScoreMapper;
import com.online.exam.service.StatsService;
import com.online.exam.vo.StatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final ScoreMapper scoreMapper;
    private final PaperMapper paperMapper;

    @Override
    public StatsVO getStats(Long paperId) {
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<>();
        if (paperId != null) {
            wrapper.eq(Score::getPaperId, paperId);
        }
        List<Score> scores = scoreMapper.selectList(wrapper);

        StatsVO vo = new StatsVO();
        if (scores.isEmpty()) {
            vo.setPassRate(0.0);
            vo.setMaxScore(0);
            vo.setMinScore(0);
            vo.setAvgScore(0.0);
            vo.setTotalCount(0);
            vo.setDistribution(buildEmptyDistribution());
            return vo;
        }

        int max = scores.stream().mapToInt(Score::getScore).max().orElse(0);
        int min = scores.stream().mapToInt(Score::getScore).min().orElse(0);
        double avg = scores.stream().mapToInt(Score::getScore).average().orElse(0);

        // 及格率（按百分制换算）
        int passCount = 0;
        for (Score s : scores) {
            double ratio = s.getTotalScore() > 0 ? (double) s.getScore() / s.getTotalScore() : 0;
            if (ratio >= 0.6) {
                passCount++;
            }
        }
        double passRate = passCount * 100.0 / scores.size();

        // 分数段分布（按百分制）
        int[] ranges = new int[5]; // 0-59, 60-69, 70-79, 80-89, 90-100
        for (Score s : scores) {
            int percent = s.getTotalScore() > 0 ? (int) (s.getScore() * 100.0 / s.getTotalScore()) : 0;
            if (percent < 60) ranges[0]++;
            else if (percent < 70) ranges[1]++;
            else if (percent < 80) ranges[2]++;
            else if (percent < 90) ranges[3]++;
            else ranges[4]++;
        }
        List<StatsVO.RangeCount> distribution = new ArrayList<>();
        distribution.add(new StatsVO.RangeCount("0-59", ranges[0]));
        distribution.add(new StatsVO.RangeCount("60-69", ranges[1]));
        distribution.add(new StatsVO.RangeCount("70-79", ranges[2]));
        distribution.add(new StatsVO.RangeCount("80-89", ranges[3]));
        distribution.add(new StatsVO.RangeCount("90-100", ranges[4]));

        vo.setMaxScore(max);
        vo.setMinScore(min);
        vo.setAvgScore(Math.round(avg * 100) / 100.0);
        vo.setPassRate(Math.round(passRate * 100) / 100.0);
        vo.setTotalCount(scores.size());
        vo.setDistribution(distribution);
        return vo;
    }

    private List<StatsVO.RangeCount> buildEmptyDistribution() {
        List<StatsVO.RangeCount> list = new ArrayList<>();
        list.add(new StatsVO.RangeCount("0-59", 0));
        list.add(new StatsVO.RangeCount("60-69", 0));
        list.add(new StatsVO.RangeCount("70-79", 0));
        list.add(new StatsVO.RangeCount("80-89", 0));
        list.add(new StatsVO.RangeCount("90-100", 0));
        return list;
    }
}
