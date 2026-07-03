package com.online.exam.service;

import com.online.exam.dto.SubmitExamDTO;
import com.online.exam.vo.ExamPaperVO;
import com.online.exam.vo.ScoreDetailVO;

public interface ExamService {

    /** 开始考试：创建考试记录 */
    Long startExam(Long userId, Long paperId);

    /** 提交考试：自动评分并保存 */
    Integer submitExam(Long userId, SubmitExamDTO submitExamDTO);

    /** 获取考试试卷（不含答案） */
    ExamPaperVO getExamPaper(Long paperId);

    /** 查看答题详情 */
    ScoreDetailVO getExamDetail(Long recordId, Long userId);
}
