-- =============================================
-- 在线考试系统 - 初始化数据
-- 密码统一为: 123456 (BCrypt加密)
-- =============================================
USE online_exam;

-- 用户数据 (密码: 123456)
INSERT INTO `user` (`username`, `password`, `name`, `role`) VALUES
('admin', '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jeeGm/TEZyj1C6', '系统管理员', 'ADMIN'),
('teacher', '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jeeGm/TEZyj1C6', '张老师', 'TEACHER'),
('student1', '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jeeGm/TEZyj1C6', '李同学', 'STUDENT'),
('student2', '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jeeGm/TEZyj1C6', '王同学', 'STUDENT'),
('student3', '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jeeGm/TEZyj1C6', '赵同学', 'STUDENT');

-- 分类数据
INSERT INTO `category` (`name`) VALUES
('Java基础'), ('数据库'), ('计算机网络'), ('操作系统'), ('前端技术');

-- 题目数据
INSERT INTO `question` (`type`, `category_id`, `content`, `options`, `answer`, `score`, `analysis`) VALUES
-- 单选题
('SINGLE', 1, 'Java中哪个关键字用于继承？',
 '["A. extends", "B. implements", "C. interface", "D. abstract"]',
 'A', 5, 'extends用于继承类，implements用于实现接口'),

('SINGLE', 1, '以下哪个是Java的基本数据类型？',
 '["A. String", "B. Integer", "C. int", "D. Boolean"]',
 'C', 5, 'int是Java的8种基本数据类型之一，String、Integer、Boolean都是引用类型'),

('SINGLE', 2, 'SQL中用于查询数据的关键字是？',
 '["A. UPDATE", "B. SELECT", "C. INSERT", "D. DELETE"]',
 'B', 5, 'SELECT是SQL中最常用的查询语句'),

('SINGLE', 3, 'HTTP协议的默认端口号是？',
 '["A. 21", "B. 443", "C. 80", "D. 8080"]',
 'C', 5, 'HTTP默认端口是80，HTTPS默认端口是443'),

('SINGLE', 1, 'Java中String属于什么类型？',
 '["A. 基本数据类型", "B. 引用数据类型", "C. 关键字", "D. 运算符"]',
 'B', 5, 'String是Java中的引用数据类型，不是基本数据类型'),

-- 多选题
('MULTIPLE', 1, '以下哪些是Java的访问修饰符？',
 '["A. public", "B. private", "C. protected", "D. default"]',
 'A,B,C', 10, 'Java有四种访问修饰符：public、private、protected和默认(default)'),

('MULTIPLE', 2, '以下哪些是MySQL的存储引擎？',
 '["A. InnoDB", "B. MyISAM", "C. Oracle", "D. Memory"]',
 'A,B,D', 10, 'InnoDB、MyISAM和Memory都是MySQL的存储引擎，Oracle是独立的数据库'),

('MULTIPLE', 3, '以下哪些是TCP/IP协议层？',
 '["A. 应用层", "B. 传输层", "C. 网络层", "D. 数据链路层"]',
 'A,B,C,D', 10, 'TCP/IP协议分为四层：应用层、传输层、网络层、数据链路层'),

-- 判断题
('JUDGE', 1, 'Java是一种面向对象的编程语言。',
 NULL,
 'T', 5, 'Java确实是一种面向对象的编程语言'),

('JUDGE', 1, 'Java中一个类只能继承一个父类。',
 NULL,
 'T', 5, 'Java只支持单继承，一个类只能继承一个父类，但可以实现多个接口'),

('JUDGE', 3, 'HTTP协议是无状态的协议。',
 NULL,
 'T', 5, 'HTTP是无状态协议，服务器不保留与客户端交互的任何状态'),

('JUDGE', 4, 'Linux是一种编程语言。',
 NULL,
 'F', 5, 'Linux是一种操作系统内核，不是编程语言');

-- 试卷数据（总分与实际题目分值保持一致）
INSERT INTO `paper` (`title`, `total_score`, `duration`, `pass_score`, `status`) VALUES
('Java基础测试卷', 60, 60, 36, 1),
('计算机综合测试卷', 30, 30, 18, 1);

-- 试卷1 - Java基础测试卷 (5道单选 + 2道多选 + 2道判断 = 25+20+10 = 55, 补充一些)
INSERT INTO `paper_question` (`paper_id`, `question_id`, `sort_order`) VALUES
(1, 1, 1), (1, 2, 2), (1, 3, 3), (1, 4, 4), (1, 5, 5),
(1, 6, 6), (1, 7, 7),
(1, 9, 8), (1, 10, 9), (1, 11, 10);

-- 试卷2 - 计算机综合测试卷
INSERT INTO `paper_question` (`paper_id`, `question_id`, `sort_order`) VALUES
(2, 3, 1), (2, 4, 2), (2, 8, 3), (2, 11, 4), (2, 12, 5);
