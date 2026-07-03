package com.online.exam.vo;

import lombok.Data;

import java.util.List;

@Data
public class StatsVO {

    /** 分数段分布 [{range: "0-59", count: 2}, {range: "60-69", count: 5}, ...] */
    private List<RangeCount> distribution;
    /** 及格率百分比 */
    private Double passRate;
    /** 最高分 */
    private Integer maxScore;
    /** 最低分 */
    private Integer minScore;
    /** 平均分 */
    private Double avgScore;
    /** 总人数 */
    private Integer totalCount;

    @Data
    public static class RangeCount {
        private String range;
        private Integer count;

        public RangeCount(String range, Integer count) {
            this.range = range;
            this.count = count;
        }
    }
}
