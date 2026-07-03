package com.online.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.online.exam.entity.Paper;
import com.online.exam.entity.Score;
import com.online.exam.entity.User;
import com.online.exam.mapper.PaperMapper;
import com.online.exam.mapper.ScoreMapper;
import com.online.exam.mapper.UserMapper;
import com.online.exam.service.ScoreService;
import com.online.exam.vo.ScoreVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreServiceImpl implements ScoreService {

    private final ScoreMapper scoreMapper;
    private final UserMapper userMapper;
    private final PaperMapper paperMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Page<ScoreVO> getMyScores(Long userId, int pageNum, int pageSize) {
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<Score>()
                .eq(Score::getUserId, userId)
                .orderByDesc(Score::getSubmitTime);
        return queryAndConvert(pageNum, pageSize, wrapper);
    }

    @Override
    public Page<ScoreVO> getRanking(Long paperId, int pageNum, int pageSize) {
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<Score>()
                .eq(paperId != null, Score::getPaperId, paperId)
                .orderByDesc(Score::getScore);
        return queryAndConvert(pageNum, pageSize, wrapper);
    }

    @Override
    public Page<ScoreVO> getAllScores(int pageNum, int pageSize, Long paperId, Long userId) {
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<Score>()
                .eq(paperId != null, Score::getPaperId, paperId)
                .eq(userId != null, Score::getUserId, userId)
                .orderByDesc(Score::getSubmitTime);
        return queryAndConvert(pageNum, pageSize, wrapper);
    }

    private Page<ScoreVO> queryAndConvert(int pageNum, int pageSize, LambdaQueryWrapper<Score> wrapper) {
        Page<Score> page = scoreMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        // 批量查询用户和试卷信息
        List<Long> userIds = page.getRecords().stream().map(Score::getUserId).distinct().collect(Collectors.toList());
        List<Long> paperIds = page.getRecords().stream().map(Score::getPaperId).distinct().collect(Collectors.toList());

        Map<Long, User> userMap = new HashMap<>();
        Map<Long, Paper> paperMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds).forEach(u -> userMap.put(u.getId(), u));
        }
        if (!paperIds.isEmpty()) {
            paperMapper.selectBatchIds(paperIds).forEach(p -> paperMap.put(p.getId(), p));
        }

        Page<ScoreVO> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords().stream().map(s -> {
            ScoreVO vo = new ScoreVO();
            vo.setId(s.getId());
            vo.setUserId(s.getUserId());
            vo.setPaperId(s.getPaperId());
            vo.setScore(s.getScore());
            vo.setTotalScore(s.getTotalScore());
            if (s.getSubmitTime() != null) {
                vo.setSubmitTime(s.getSubmitTime().format(FMT));
            }
            User user = userMap.get(s.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setName(user.getName());
            }
            Paper paper = paperMap.get(s.getPaperId());
            if (paper != null) {
                vo.setPaperTitle(paper.getTitle());
                vo.setPassed(s.getScore() >= paper.getPassScore());
            }
            vo.setRecordId(s.getRecordId());
            return vo;
        }).collect(Collectors.toList()));
        return result;
    }
}
