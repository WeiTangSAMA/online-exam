-- =============================================
-- 在线考试系统 - 数据库建表脚本
-- 数据库名: online_exam
-- =============================================

CREATE DATABASE IF NOT EXISTS online_exam DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE online_exam;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(200) NOT NULL COMMENT '密码(BCrypt加密)',
    `name` VARCHAR(50) DEFAULT '' COMMENT '姓名',
    `role` VARCHAR(20) NOT NULL DEFAULT 'STUDENT' COMMENT '角色: ADMIN / TEACHER / STUDENT',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 题目分类表
CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目分类表';

-- 题目表
CREATE TABLE IF NOT EXISTS `question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `type` VARCHAR(20) NOT NULL COMMENT '题型: SINGLE / MULTIPLE / JUDGE',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `content` TEXT NOT NULL COMMENT '题干内容',
    `options` JSON DEFAULT NULL COMMENT '选项(JSON数组)',
    `answer` VARCHAR(500) NOT NULL COMMENT '正确答案',
    `score` INT NOT NULL DEFAULT 5 COMMENT '分值',
    `analysis` TEXT DEFAULT NULL COMMENT '解析',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

-- 试卷表
CREATE TABLE IF NOT EXISTS `paper` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title` VARCHAR(200) NOT NULL COMMENT '试卷标题',
    `total_score` INT NOT NULL DEFAULT 100 COMMENT '总分',
    `duration` INT NOT NULL DEFAULT 60 COMMENT '考试时长(分钟)',
    `pass_score` INT NOT NULL DEFAULT 60 COMMENT '及格分数',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0=草稿 1=已发布',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表';

-- 试卷-题目关联表
CREATE TABLE IF NOT EXISTS `paper_question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `paper_id` BIGINT NOT NULL COMMENT '试卷ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '题目排序',
    PRIMARY KEY (`id`),
    KEY `idx_paper` (`paper_id`),
    KEY `idx_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷题目关联表';

-- 考试记录表
CREATE TABLE IF NOT EXISTS `exam_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `paper_id` BIGINT NOT NULL COMMENT '试卷ID',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0=进行中 1=已提交 2=已超时',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_paper` (`paper_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录表';

-- 答题明细表
CREATE TABLE IF NOT EXISTS `exam_answer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `record_id` BIGINT NOT NULL COMMENT '考试记录ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `user_answer` VARCHAR(500) DEFAULT NULL COMMENT '用户答案',
    `is_correct` TINYINT DEFAULT 0 COMMENT '是否正确: 0=否 1=是',
    `score` INT DEFAULT 0 COMMENT '得分',
    PRIMARY KEY (`id`),
    KEY `idx_record` (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题明细表';

-- 成绩表
CREATE TABLE IF NOT EXISTS `score` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `paper_id` BIGINT NOT NULL COMMENT '试卷ID',
    `score` INT NOT NULL DEFAULT 0 COMMENT '得分',
    `total_score` INT NOT NULL DEFAULT 100 COMMENT '总分',
    `submit_time` DATETIME NOT NULL COMMENT '提交时间',
    `record_id` BIGINT NOT NULL COMMENT '考试记录ID',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_paper` (`paper_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩表';
