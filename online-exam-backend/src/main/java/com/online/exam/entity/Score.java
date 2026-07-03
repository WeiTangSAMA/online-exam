package com.online.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("score")
public class Score {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long paperId;

    private Integer score;

    private Integer totalScore;

    private LocalDateTime submitTime;

    private Long recordId;
}
