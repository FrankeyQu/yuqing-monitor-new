package com.stonedt.intelligence.service.campus;

import com.stonedt.intelligence.vo.ReportDataVO;

import java.util.Date;

public interface CampusReportDataService {

    /**
     * 聚合报告所需的全维度数据
     *
     * @param keyword   可选关键词过滤，为 null 时不按关键词过滤
     * @param startTime 监测周期开始时间，可为 null
     * @param endTime   监测周期结束时间，可为 null
     * @return 聚合后的报告数据，数据为空时返回空集合而非 null
     */
    ReportDataVO aggregateReportData(String keyword, Date startTime, Date endTime);

    /**
     * 聚合报告数据，优先按事件精确收敛，再按关键词过滤。
     */
    ReportDataVO aggregateReportData(String keyword, Long eventId, Date startTime, Date endTime);
}
