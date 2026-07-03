package com.online.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exam_record")
public class ExamRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long paperId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 0=进行中 1=已提交 2=已超时 */
    private Integer status;
}
