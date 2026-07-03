package com.online.exam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.online.exam.dto.PaperDTO;
import com.online.exam.entity.Paper;
import com.online.exam.vo.ExamPaperVO;

public interface PaperService {

    Page<Paper> getPaperPage(int pageNum, int pageSize, Integer status);

    Paper getPaperById(Long id);

    /** 创建试卷（含组卷题目） */
    Long savePaper(PaperDTO paperDTO);

    /** 更新试卷基础信息 */
    void updatePaper(PaperDTO paperDTO);

    /** 绑定试卷的题目 */
    void bindQuestions(Long paperId, java.util.List<Long> questionIds);

    void deletePaper(Long id);

    void publishPaper(Long id);

    /** 获取试卷题目（不含答案）供学生考试使用 */
    ExamPaperVO getExamPaper(Long paperId);
}
