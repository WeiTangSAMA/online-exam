package com.online.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.online.exam.common.BusinessException;
import com.online.exam.dto.QuestionDTO;
import com.online.exam.entity.Category;
import com.online.exam.entity.Paper;
import com.online.exam.entity.PaperQuestion;
import com.online.exam.entity.Question;
import com.online.exam.mapper.CategoryMapper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;
    private final CategoryMapper categoryMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final PaperMapper paperMapper;

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

        Page<Question> page = questionMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

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
        Question question = new Question();
        BeanUtils.copyProperties(questionDTO, question);
        questionMapper.insert(question);
    }

    @Override
    public void updateQuestion(QuestionDTO questionDTO) {
        if (questionDTO.getId() == null) {
            throw new BusinessException("题目ID不能为空");
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionDTO, question);
        questionMapper.updateById(question);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        List<Long> affectedPaperIds = paperQuestionMapper.selectList(new LambdaQueryWrapper<PaperQuestion>()
                        .eq(PaperQuestion::getQuestionId, id))
                .stream()
                .map(PaperQuestion::getPaperId)
                .distinct()
                .collect(Collectors.toList());

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
