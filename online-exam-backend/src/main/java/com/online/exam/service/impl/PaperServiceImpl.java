package com.online.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.online.exam.common.BusinessException;
import com.online.exam.dto.PaperDTO;
import com.online.exam.entity.Paper;
import com.online.exam.entity.PaperQuestion;
import com.online.exam.entity.Question;
import com.online.exam.mapper.PaperMapper;
import com.online.exam.mapper.PaperQuestionMapper;
import com.online.exam.mapper.QuestionMapper;
import com.online.exam.service.PaperService;
import com.online.exam.vo.ExamPaperVO;
import com.online.exam.vo.ExamQuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaperServiceImpl implements PaperService {

    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;

    @Override
    public Page<Paper> getPaperPage(int pageNum, int pageSize, Integer status) {
        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Paper::getStatus, status);
        }
        wrapper.orderByDesc(Paper::getCreateTime);
        return paperMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Paper getPaperById(Long id) {
        Paper paper = paperMapper.selectById(id);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        return paper;
    }

    @Override
    @Transactional
    public Long savePaper(PaperDTO paperDTO) {
        Paper paper = new Paper();
        paper.setTitle(paperDTO.getTitle());
        paper.setDuration(paperDTO.getDuration());
        paper.setPassScore(paperDTO.getPassScore() != null ? paperDTO.getPassScore() : 60);
        paper.setStatus(paperDTO.getStatus() != null ? paperDTO.getStatus() : 0);
        // 计算总分
        int totalScore = calculateTotalScore(paperDTO.getQuestionIds());
        paper.setTotalScore(totalScore);
        paperMapper.insert(paper);

        // 绑定题目
        if (paperDTO.getQuestionIds() != null && !paperDTO.getQuestionIds().isEmpty()) {
            bindQuestions(paper.getId(), paperDTO.getQuestionIds());
        }
        return paper.getId();
    }

    @Override
    @Transactional
    public void updatePaper(PaperDTO paperDTO) {
        Paper paper = new Paper();
        paper.setId(paperDTO.getId());
        paper.setTitle(paperDTO.getTitle());
        paper.setDuration(paperDTO.getDuration());
        paper.setPassScore(paperDTO.getPassScore());
        paper.setStatus(paperDTO.getStatus());
        if (paperDTO.getQuestionIds() != null) {
            paper.setTotalScore(calculateTotalScore(paperDTO.getQuestionIds()));
        }
        paperMapper.updateById(paper);

        // 如果传了题目，重新绑定
        if (paperDTO.getQuestionIds() != null) {
            // 先删除旧的关联
            paperQuestionMapper.delete(new LambdaQueryWrapper<PaperQuestion>()
                    .eq(PaperQuestion::getPaperId, paperDTO.getId()));
            bindQuestions(paperDTO.getId(), paperDTO.getQuestionIds());
        }
    }

    @Override
    @Transactional
    public void bindQuestions(Long paperId, List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return;
        }
        List<PaperQuestion> list = new ArrayList<>();
        for (int i = 0; i < questionIds.size(); i++) {
            PaperQuestion pq = new PaperQuestion();
            pq.setPaperId(paperId);
            pq.setQuestionId(questionIds.get(i));
            pq.setSortOrder(i + 1);
            list.add(pq);
        }
        // 批量插入
        for (PaperQuestion pq : list) {
            paperQuestionMapper.insert(pq);
        }

        // 更新试卷总分
        Paper paper = paperMapper.selectById(paperId);
        if (paper != null) {
            paper.setTotalScore(calculateTotalScore(questionIds));
            paperMapper.updateById(paper);
        }
    }

    @Override
    @Transactional
    public void deletePaper(Long id) {
        // 删除关联题目
        paperQuestionMapper.delete(new LambdaQueryWrapper<PaperQuestion>()
                .eq(PaperQuestion::getPaperId, id));
        paperMapper.deleteById(id);
    }

    @Override
    public void publishPaper(Long id) {
        Paper paper = new Paper();
        paper.setId(id);
        paper.setStatus(1);
        paperMapper.updateById(paper);
    }

    @Override
    public ExamPaperVO getExamPaper(Long paperId) {
        Paper paper = getPaperById(paperId);

        ExamPaperVO vo = new ExamPaperVO();
        vo.setId(paper.getId());
        vo.setTitle(paper.getTitle());
        vo.setTotalScore(paper.getTotalScore());
        vo.setDuration(paper.getDuration());
        vo.setPassScore(paper.getPassScore());

        // 查询试卷题目（按 sortOrder 排序）
        List<PaperQuestion> pqs = paperQuestionMapper.selectList(
                new LambdaQueryWrapper<PaperQuestion>()
                        .eq(PaperQuestion::getPaperId, paperId)
                        .orderByAsc(PaperQuestion::getSortOrder));

        if (!pqs.isEmpty()) {
            List<Long> questionIds = pqs.stream().map(PaperQuestion::getQuestionId).collect(Collectors.toList());
            List<Question> questions = questionMapper.selectBatchIds(questionIds);

            List<ExamQuestionVO> questionVOs = new ArrayList<>();
            for (int i = 0; i < pqs.size(); i++) {
                PaperQuestion pq = pqs.get(i);
                Question q = questions.stream().filter(qs -> qs.getId().equals(pq.getQuestionId())).findFirst().orElse(null);
                if (q != null) {
                    ExamQuestionVO qvo = new ExamQuestionVO();
                    qvo.setId(q.getId());
                    qvo.setType(q.getType());
                    qvo.setContent(q.getContent());
                    qvo.setOptions(q.getOptions());
                    qvo.setScore(q.getScore());
                    qvo.setSortOrder(pq.getSortOrder());
                    questionVOs.add(qvo);
                }
            }
            vo.setQuestions(questionVOs);
        }
        return vo;
    }

    private int calculateTotalScore(List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return 0;
        }
        List<Question> questions = questionMapper.selectBatchIds(questionIds);
        return questions.stream().mapToInt(Question::getScore).sum();
    }
}
