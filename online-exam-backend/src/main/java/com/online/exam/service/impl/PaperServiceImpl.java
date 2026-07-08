package com.online.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.online.exam.common.BusinessException;
import com.online.exam.dto.PaperDTO;
import com.online.exam.entity.ExamAnswer;
import com.online.exam.entity.ExamRecord;
import com.online.exam.entity.Paper;
import com.online.exam.entity.PaperQuestion;
import com.online.exam.entity.Question;
import com.online.exam.entity.Score;
import com.online.exam.mapper.ExamAnswerMapper;
import com.online.exam.mapper.ExamRecordMapper;
import com.online.exam.mapper.PaperMapper;
import com.online.exam.mapper.PaperQuestionMapper;
import com.online.exam.mapper.QuestionMapper;
import com.online.exam.mapper.ScoreMapper;
import com.online.exam.service.PaperService;
import com.online.exam.vo.ExamPaperVO;
import com.online.exam.vo.ExamQuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaperServiceImpl implements PaperService {

    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;
    private final ExamRecordMapper examRecordMapper;
    private final ExamAnswerMapper examAnswerMapper;
    private final ScoreMapper scoreMapper;

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
        List<Long> questionIds = validateQuestionIds(paperDTO.getQuestionIds());
        validatePaper(paperDTO, questionIds);
        Paper paper = new Paper();
        paper.setTitle(paperDTO.getTitle());
        paper.setDuration(paperDTO.getDuration());
        paper.setPassScore(paperDTO.getPassScore() != null ? paperDTO.getPassScore() : 60);
        paper.setStatus(paperDTO.getStatus() != null ? paperDTO.getStatus() : 0);
        // 计算总分
        int totalScore = calculateTotalScore(questionIds);
        paper.setTotalScore(totalScore);
        paperMapper.insert(paper);

        // 绑定题目
        if (!questionIds.isEmpty()) {
            bindQuestions(paper.getId(), questionIds);
        }
        return paper.getId();
    }

    @Override
    @Transactional
    public void updatePaper(PaperDTO paperDTO) {
        if (paperDTO.getId() == null) {
            throw new BusinessException("试卷ID不能为空");
        }
        getPaperById(paperDTO.getId());
        List<Long> questionIds = paperDTO.getQuestionIds() == null ? null : validateQuestionIds(paperDTO.getQuestionIds());
        validatePaper(paperDTO, questionIds);
        if (Integer.valueOf(1).equals(paperDTO.getStatus()) && questionIds == null && !hasQuestions(paperDTO.getId())) {
            throw new BusinessException("试卷至少包含一道题目后才能发布");
        }
        Paper paper = new Paper();
        paper.setId(paperDTO.getId());
        paper.setTitle(paperDTO.getTitle());
        paper.setDuration(paperDTO.getDuration());
        paper.setPassScore(paperDTO.getPassScore());
        paper.setStatus(paperDTO.getStatus());
        if (questionIds != null) {
            paper.setTotalScore(calculateTotalScore(questionIds));
        }
        paperMapper.updateById(paper);

        // 如果传了题目，重新绑定
        if (questionIds != null) {
            bindQuestions(paperDTO.getId(), questionIds);
        }
    }

    @Override
    @Transactional
    public void bindQuestions(Long paperId, List<Long> questionIds) {
        Paper paper = getPaperById(paperId);
        List<Long> validQuestionIds = validateQuestionIds(questionIds);
        paperQuestionMapper.delete(new LambdaQueryWrapper<PaperQuestion>()
                .eq(PaperQuestion::getPaperId, paperId));

        if (!validQuestionIds.isEmpty()) {
            List<PaperQuestion> list = new ArrayList<>();
            for (int i = 0; i < validQuestionIds.size(); i++) {
                PaperQuestion pq = new PaperQuestion();
                pq.setPaperId(paperId);
                pq.setQuestionId(validQuestionIds.get(i));
                pq.setSortOrder(i + 1);
                list.add(pq);
            }
            // 批量插入
            for (PaperQuestion pq : list) {
                paperQuestionMapper.insert(pq);
            }
        }

        // 更新试卷总分
        paper.setTotalScore(calculateTotalScore(validQuestionIds));
        paperMapper.updateById(paper);
    }

    @Override
    @Transactional
    public void deletePaper(Long id) {
        List<ExamRecord> records = examRecordMapper.selectList(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getPaperId, id));
        List<Long> recordIds = records.stream().map(ExamRecord::getId).collect(Collectors.toList());
        if (!recordIds.isEmpty()) {
            examAnswerMapper.delete(new LambdaQueryWrapper<ExamAnswer>()
                    .in(ExamAnswer::getRecordId, recordIds));
        }
        scoreMapper.delete(new LambdaQueryWrapper<Score>()
                .eq(Score::getPaperId, id));
        examRecordMapper.delete(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getPaperId, id));
        paperQuestionMapper.delete(new LambdaQueryWrapper<PaperQuestion>()
                .eq(PaperQuestion::getPaperId, id));
        paperMapper.deleteById(id);
    }

    @Override
    public void publishPaper(Long id) {
        getPaperById(id);
        if (!hasQuestions(id)) {
            throw new BusinessException("试卷至少包含一道题目后才能发布");
        }
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

    private void validatePaper(PaperDTO paperDTO, List<Long> questionIds) {
        if (paperDTO.getDuration() == null || paperDTO.getDuration() <= 0) {
            throw new BusinessException("考试时长必须大于0");
        }
        if (paperDTO.getPassScore() != null && paperDTO.getPassScore() < 0) {
            throw new BusinessException("及格分不能为负数");
        }
        Integer status = paperDTO.getStatus();
        if (status != null && status != 0 && status != 1) {
            throw new BusinessException("试卷状态不合法");
        }
        if (Integer.valueOf(1).equals(status) && (questionIds == null || questionIds.isEmpty())) {
            throw new BusinessException("试卷至少包含一道题目后才能发布");
        }
    }

    private boolean hasQuestions(Long paperId) {
        return paperQuestionMapper.selectCount(new LambdaQueryWrapper<PaperQuestion>()
                .eq(PaperQuestion::getPaperId, paperId)) > 0;
    }
    private List<Long> validateQuestionIds(List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return new ArrayList<>();
        }
        if (questionIds.stream().anyMatch(id -> id == null)) {
            throw new BusinessException("题目ID不能为空");
        }
        LinkedHashSet<Long> uniqueIds = new LinkedHashSet<>(questionIds);
        if (uniqueIds.size() != questionIds.size()) {
            throw new BusinessException("试卷题目不能重复");
        }
        List<Long> validQuestionIds = new ArrayList<>(uniqueIds);
        List<Question> questions = questionMapper.selectBatchIds(validQuestionIds);
        if (questions.size() != validQuestionIds.size()) {
            throw new BusinessException("题目不存在，无法组卷");
        }
        return validQuestionIds;
    }
    private int calculateTotalScore(List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return 0;
        }
        List<Question> questions = questionMapper.selectBatchIds(questionIds);
        return questions.stream().mapToInt(Question::getScore).sum();
    }
}
