package com.online.exam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.online.exam.dto.QuestionDTO;
import com.online.exam.entity.Question;
import com.online.exam.vo.QuestionVO;

public interface QuestionService {

    Page<QuestionVO> getQuestionPage(int pageNum, int pageSize, String type, Long categoryId, String keyword);

    void addQuestion(QuestionDTO questionDTO);

    void updateQuestion(QuestionDTO questionDTO);

    void deleteQuestion(Long id);

    QuestionVO getQuestionById(Long id);
}
