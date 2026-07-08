package com.online.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.online.exam.common.BusinessException;
import com.online.exam.common.ResultCode;
import com.online.exam.dto.ExamAnswerDTO;
import com.online.exam.dto.SubmitExamDTO;
import com.online.exam.entity.*;
import com.online.exam.mapper.*;
import com.online.exam.service.ExamService;
import com.online.exam.service.PaperService;
import com.online.exam.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamRecordMapper examRecordMapper;
    private final ExamAnswerMapper examAnswerMapper;
    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;
    private final ScoreMapper scoreMapper;
    private final UserMapper userMapper;
    private final PaperService paperService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public Long startExam(Long userId, Long paperId) {
        // 校验试卷是否已发布
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException(ResultCode.PAPER_NOT_FOUND);
        }
        if (paper.getStatus() != 1) {
            throw new BusinessException("试卷未发布，无法开始考试");
        }

        // 检查是否已有考试记录：进行中的考试可恢复，已结束的考试不可重复参加。
        List<ExamRecord> existingRecords = examRecordMapper.selectList(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, userId)
                .eq(ExamRecord::getPaperId, paperId)
                .orderByDesc(ExamRecord::getStartTime));
        LocalDateTime now = LocalDateTime.now();
        ExamRecord runningRecord = null;
        for (ExamRecord existing : existingRecords) {
            if (existing.getStatus() == 1 || existing.getStatus() == 2) {
                throw new BusinessException(ResultCode.EXAM_ALREADY_SUBMITTED);
            }
            if (existing.getStatus() == 0 && runningRecord == null) {
                LocalDateTime deadline = existing.getStartTime().plusMinutes(paper.getDuration());
                if (now.isAfter(deadline)) {
                    existing.setEndTime(deadline);
                    existing.setStatus(2);
                    examRecordMapper.updateById(existing);
                    throw new BusinessException(ResultCode.EXAM_TIME_OUT);
                }
                runningRecord = existing;
            }
        }
        if (runningRecord != null) {
            return runningRecord.getId();
        }
        ExamRecord record = new ExamRecord();
        record.setUserId(userId);
        record.setPaperId(paperId);
        record.setStartTime(now);
        record.setStatus(0);
        examRecordMapper.insert(record);
        return record.getId();
    }

    @Override
    @Transactional
    public Integer submitExam(Long userId, SubmitExamDTO submitExamDTO) {
        ExamRecord record = examRecordMapper.selectById(submitExamDTO.getRecordId());
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该考试记录");
        }
        if (record.getStatus() != 0) {
            throw new BusinessException(ResultCode.EXAM_ALREADY_SUBMITTED);
        }

        Paper paper = paperMapper.selectById(record.getPaperId());
        if (paper == null) {
            throw new BusinessException(ResultCode.PAPER_NOT_FOUND);
        }

        // 校验是否超时
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = record.getStartTime().plusMinutes(paper.getDuration());
        boolean timeOut = now.isAfter(deadline);

        // 获取试卷题目及正确答案
        List<PaperQuestion> pqs = paperQuestionMapper.selectList(
                new LambdaQueryWrapper<PaperQuestion>()
                        .eq(PaperQuestion::getPaperId, paper.getId())
                        .orderByAsc(PaperQuestion::getSortOrder));
        List<Long> questionIds = pqs.stream().map(PaperQuestion::getQuestionId).collect(Collectors.toList());
        List<Question> questions = questionMapper.selectBatchIds(questionIds);
        Map<Long, Question> questionMap = questions.stream().collect(Collectors.toMap(Question::getId, q -> q));

        // 构建用户答案 Map
        Map<Long, String> userAnswerMap = new HashMap<>();
        if (submitExamDTO.getAnswers() != null) {
            for (ExamAnswerDTO answer : submitExamDTO.getAnswers()) {
                userAnswerMap.put(answer.getQuestionId(), answer.getUserAnswer());
            }
        }

        // 自动评分
        int totalScore = 0;
        List<ExamAnswer> answerList = new ArrayList<>();
        for (PaperQuestion pq : pqs) {
            Question question = questionMap.get(pq.getQuestionId());
            if (question == null) continue;

            String userAnswer = userAnswerMap.get(question.getId());
            ExamAnswer examAnswer = new ExamAnswer();
            examAnswer.setRecordId(record.getId());
            examAnswer.setQuestionId(question.getId());
            examAnswer.setUserAnswer(userAnswer);

            if (userAnswer != null && !userAnswer.isEmpty()) {
                boolean correct = judgeAnswer(question, userAnswer);
                examAnswer.setIsCorrect(correct ? 1 : 0);
                examAnswer.setScore(correct ? question.getScore() : 0);
                if (correct) {
                    totalScore += question.getScore();
                }
            } else {
                examAnswer.setIsCorrect(0);
                examAnswer.setScore(0);
            }
            answerList.add(examAnswer);
        }

        // 保存答题明细
        for (ExamAnswer ea : answerList) {
            examAnswerMapper.insert(ea);
        }

        // 更新考试记录
        record.setEndTime(now);
        record.setStatus(timeOut ? 2 : 1);
        examRecordMapper.updateById(record);

        // 保存成绩
        Score score = new Score();
        score.setUserId(userId);
        score.setPaperId(paper.getId());
        score.setScore(totalScore);
        score.setTotalScore(paper.getTotalScore());
        score.setSubmitTime(now);
        score.setRecordId(record.getId());
        scoreMapper.insert(score);

        return totalScore;
    }

    @Override
    public ExamPaperVO getExamPaper(Long paperId, boolean canViewDraft) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException(ResultCode.PAPER_NOT_FOUND);
        }
        if (!canViewDraft && paper.getStatus() != 1) {
            throw new BusinessException("试卷未发布，无法参加考试");
        }
        return paperService.getExamPaper(paperId);
    }

    @Override
    public ScoreDetailVO getExamDetail(Long recordId, Long userId, boolean canViewAll) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        if (!canViewAll && !record.getUserId().equals(userId)) {
            throw new BusinessException("无权查看该考试记录");
        }

        Paper paper = paperMapper.selectById(record.getPaperId());
        List<ExamAnswer> answers = examAnswerMapper.selectList(
                new LambdaQueryWrapper<ExamAnswer>().eq(ExamAnswer::getRecordId, recordId));

        List<Long> questionIds = answers.stream().map(ExamAnswer::getQuestionId).collect(Collectors.toList());
        Map<Long, Question> questionMap = new HashMap<>();
        if (!questionIds.isEmpty()) {
            List<Question> questions = questionMapper.selectBatchIds(questionIds);
            questionMap = questions.stream().collect(Collectors.toMap(Question::getId, q -> q));
        }

        ScoreDetailVO vo = new ScoreDetailVO();
        vo.setRecordId(recordId);
        vo.setPaperTitle(paper != null ? paper.getTitle() : "");
        vo.setTotalScore(paper != null ? paper.getTotalScore() : 100);
        vo.setSubmitTime(record.getEndTime() != null ? record.getEndTime().format(FMT) : "");

        int totalScore = 0;
        List<AnswerDetailVO> answerVOs = new ArrayList<>();
        for (ExamAnswer ea : answers) {
            AnswerDetailVO avo = new AnswerDetailVO();
            Question question = questionMap.get(ea.getQuestionId());
            if (question != null) {
                avo.setQuestionId(question.getId());
                avo.setType(question.getType());
                avo.setContent(question.getContent());
                avo.setOptions(question.getOptions());
                avo.setCorrectAnswer(question.getAnswer());
                avo.setAnalysis(question.getAnalysis());
            }
            avo.setUserAnswer(ea.getUserAnswer());
            avo.setScore(ea.getScore());
            avo.setIsCorrect(ea.getIsCorrect());
            totalScore += ea.getScore();
            answerVOs.add(avo);
        }
        vo.setScore(totalScore);
        vo.setAnswers(answerVOs);
        return vo;
    }

    /**
     * 判题逻辑：
     * - SINGLE / JUDGE：直接比较字符串（忽略大小写、去空格）
     * - MULTIPLE：将答案拆分排序后比较（顺序无关）
     */
    private boolean judgeAnswer(Question question, String userAnswer) {
        String correctAnswer = question.getAnswer();
        if (correctAnswer == null || userAnswer == null) {
            return false;
        }
        // 统一处理：去空格、大写
        String ua = userAnswer.trim().toUpperCase();
        String ca = correctAnswer.trim().toUpperCase();

        if ("MULTIPLE".equals(question.getType())) {
            // 多选题：拆分排序后比较
            List<String> uaList = Arrays.stream(ua.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).sorted().collect(Collectors.toList());
            List<String> caList = Arrays.stream(ca.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).sorted().collect(Collectors.toList());
            return uaList.equals(caList);
        }
        return ua.equals(ca);
    }
}
