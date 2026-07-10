package com.online.exam.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.online.exam.common.BusinessException;
import com.online.exam.dto.QuestionDTO;
import com.online.exam.entity.Category;
import com.online.exam.entity.ExamAnswer;
import com.online.exam.entity.Paper;
import com.online.exam.entity.PaperQuestion;
import com.online.exam.entity.Question;
import com.online.exam.mapper.CategoryMapper;
import com.online.exam.mapper.ExamAnswerMapper;
import com.online.exam.mapper.PaperMapper;
import com.online.exam.mapper.PaperQuestionMapper;
import com.online.exam.mapper.QuestionMapper;
import com.online.exam.service.QuestionService;
import com.online.exam.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;
    private final CategoryMapper categoryMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final PaperMapper paperMapper;
    private final ExamAnswerMapper examAnswerMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Page<QuestionVO> getQuestionPage(int pageNum, int pageSize, String type, Long categoryId, String keyword) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(type)) {
            wrapper.eq(Question::getType, type);
        }
        if (categoryId != null) {
            wrapper.eq(Question::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Question::getContent, keyword);
        }
        wrapper.orderByDesc(Question::getCreateTime);

        Page<Question> page = questionMapper.selectPage(new Page<>(normalizePageNum(pageNum), normalizePageSize(pageSize)), wrapper);

        // 批量查询分类名
        List<Long> categoryIds = page.getRecords().stream()
                .map(Question::getCategoryId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> categoryNameMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryMapper.selectBatchIds(categoryIds);
            categories.forEach(c -> categoryNameMap.put(c.getId(), c.getName()));
        }

        // 转换为 VO
        Page<QuestionVO> result = new Page<>();
        BeanUtils.copyProperties(page, result, "records");
        List<QuestionVO> records = page.getRecords().stream().map(q -> convert(q, categoryNameMap)).collect(Collectors.toList());
        result.setRecords(records);
        return result;
    }

    @Override
    public void addQuestion(QuestionDTO questionDTO) {
        validateQuestion(questionDTO);
        Question question = new Question();
        BeanUtils.copyProperties(questionDTO, question);
        questionMapper.insert(question);
    }

    @Override
    @Transactional
    public void updateQuestion(QuestionDTO questionDTO) {
        if (questionDTO.getId() == null) {
            throw new BusinessException("题目ID不能为空");
        }
        if (questionMapper.selectById(questionDTO.getId()) == null) {
            throw new BusinessException("题目不存在");
        }
        validateQuestion(questionDTO);
        Question question = new Question();
        BeanUtils.copyProperties(questionDTO, question);
        questionMapper.updateById(question);

        List<Long> affectedPaperIds = getAffectedPaperIds(questionDTO.getId());
        for (Long paperId : affectedPaperIds) {
            updatePaperTotalScore(paperId);
        }
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        Long answerCount = examAnswerMapper.selectCount(new LambdaQueryWrapper<ExamAnswer>()
                .eq(ExamAnswer::getQuestionId, id));
        if (answerCount > 0) {
            throw new BusinessException("题目已有答题记录，无法删除");
        }

        List<Long> affectedPaperIds = getAffectedPaperIds(id);

        paperQuestionMapper.delete(new LambdaQueryWrapper<PaperQuestion>()
                .eq(PaperQuestion::getQuestionId, id));
        questionMapper.deleteById(id);

        for (Long paperId : affectedPaperIds) {
            updatePaperTotalScore(paperId);
        }
    }

    @Override
    public QuestionVO getQuestionById(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }
        // 单查时也需要填充分类名
        Map<Long, String> categoryNameMap = new HashMap<>();
        if (question.getCategoryId() != null) {
            Category category = categoryMapper.selectById(question.getCategoryId());
            if (category != null) {
                categoryNameMap.put(category.getId(), category.getName());
            }
        }
        return convert(question, categoryNameMap);
    }


    private List<Long> getAffectedPaperIds(Long questionId) {
        return paperQuestionMapper.selectList(new LambdaQueryWrapper<PaperQuestion>()
                        .eq(PaperQuestion::getQuestionId, questionId))
                .stream()
                .map(PaperQuestion::getPaperId)
                .distinct()
                .collect(Collectors.toList());
    }
    private int normalizePageNum(int pageNum) {
        return Math.max(pageNum, 1);
    }

    private int normalizePageSize(int pageSize) {
        if (pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }
    private void validateQuestion(QuestionDTO questionDTO) {
        if (questionDTO.getScore() == null || questionDTO.getScore() <= 0 || questionDTO.getScore() > 100) {
            throw new BusinessException("题目分值必须在 1 到 100 之间");
        }
        String type = questionDTO.getType();
        if (!List.of("SINGLE", "MULTIPLE", "JUDGE").contains(type)) {
            throw new BusinessException("题型不合法");
        }
        if (!StringUtils.hasText(questionDTO.getContent())) {
            throw new BusinessException("题目内容不能为空");
        }
        if (!StringUtils.hasText(questionDTO.getAnswer())) {
            throw new BusinessException("答案不能为空");
        }

        if ("JUDGE".equals(type)) {
            validateJudgeAnswer(questionDTO.getAnswer());
            return;
        }
        validateChoiceQuestion(questionDTO);
    }

    private void validateJudgeAnswer(String answer) {
        String normalizedAnswer = answer.trim().toUpperCase();
        if (!List.of("T", "F").contains(normalizedAnswer)) {
            throw new BusinessException("判断题答案必须为 T 或 F");
        }
    }

    private void validateChoiceQuestion(QuestionDTO questionDTO) {
        List<String> options = parseChoiceOptions(questionDTO.getOptions());
        if (options.size() < 2 || options.size() > 8) {
            throw new BusinessException("选择题选项数量必须在 2 到 8 个之间");
        }

        Set<String> optionLabels = new LinkedHashSet<>();
        for (String option : options) {
            String label = extractOptionLabel(option);
            if (!optionLabels.add(label)) {
                throw new BusinessException("选择题选项不能重复");
            }
        }

        if ("SINGLE".equals(questionDTO.getType())) {
            String answer = questionDTO.getAnswer().trim().toUpperCase();
            if (answer.contains(",") || !optionLabels.contains(answer)) {
                throw new BusinessException("单选题答案必须是一个有效选项");
            }
            return;
        }

        List<String> answerParts = Arrays.stream(questionDTO.getAnswer().split(",", -1))
                .map(answer -> answer.trim().toUpperCase())
                .collect(Collectors.toList());
        Set<String> answerLabels = new LinkedHashSet<>(answerParts);
        if (answerParts.stream().anyMatch(answer -> !StringUtils.hasText(answer))
                || answerLabels.isEmpty()
                || answerLabels.size() != answerParts.size()) {
            throw new BusinessException("多选题答案不能为空且不能重复");
        }
        if (!optionLabels.containsAll(answerLabels)) {
            throw new BusinessException("多选题答案必须全部来自有效选项");
        }
    }

    private List<String> parseChoiceOptions(String optionsJson) {
        if (!StringUtils.hasText(optionsJson)) {
            throw new BusinessException("选择题选项不能为空");
        }
        try {
            List<String> options = JSON.parseArray(optionsJson, String.class);
            if (options == null) {
                throw new BusinessException("选择题选项格式不合法");
            }
            return options;
        } catch (JSONException ex) {
            throw new BusinessException("选择题选项格式不合法");
        }
    }

    private String extractOptionLabel(String option) {
        if (!StringUtils.hasText(option)) {
            throw new BusinessException("选择题选项内容不能为空");
        }
        int dotIndex = option.indexOf('.');
        if (dotIndex != 1 || option.length() <= 2 || !StringUtils.hasText(option.substring(dotIndex + 1))) {
            throw new BusinessException("选择题选项格式不合法");
        }
        String label = option.substring(0, dotIndex).trim().toUpperCase();
        if (label.length() != 1 || label.charAt(0) < 'A' || label.charAt(0) > 'H') {
            throw new BusinessException("选择题选项标识不合法");
        }
        return label;
    }

    private void updatePaperTotalScore(Long paperId) {
        List<Long> questionIds = paperQuestionMapper.selectList(new LambdaQueryWrapper<PaperQuestion>()
                        .eq(PaperQuestion::getPaperId, paperId))
                .stream()
                .map(PaperQuestion::getQuestionId)
                .collect(Collectors.toList());

        int totalScore = 0;
        if (!questionIds.isEmpty()) {
            totalScore = questionMapper.selectBatchIds(questionIds).stream()
                    .mapToInt(Question::getScore)
                    .sum();
        }

        Paper paper = new Paper();
        paper.setId(paperId);
        paper.setTotalScore(totalScore);
        paperMapper.updateById(paper);
    }

    private QuestionVO convert(Question q, Map<Long, String> categoryNameMap) {
        QuestionVO vo = new QuestionVO();
        vo.setId(q.getId());
        vo.setType(q.getType());
        vo.setCategoryId(q.getCategoryId());
        vo.setContent(q.getContent());
        vo.setOptions(q.getOptions());
        vo.setAnswer(q.getAnswer());
        vo.setScore(String.valueOf(q.getScore()));
        vo.setAnalysis(q.getAnalysis());
        if (q.getCreateTime() != null) {
            vo.setCreateTime(q.getCreateTime().format(FMT));
        }
        if (categoryNameMap != null && q.getCategoryId() != null) {
            vo.setCategoryName(categoryNameMap.get(q.getCategoryId()));
        }
        return vo;
    }
}
