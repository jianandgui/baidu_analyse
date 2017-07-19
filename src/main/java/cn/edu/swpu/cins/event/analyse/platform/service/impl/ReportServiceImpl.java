package cn.edu.swpu.cins.event.analyse.platform.service.impl;

import cn.edu.swpu.cins.event.analyse.platform.dao.DailyEventDao;
import cn.edu.swpu.cins.event.analyse.platform.enums.ChartDataEnum;
import cn.edu.swpu.cins.event.analyse.platform.enums.ChartTypeEnum;
import cn.edu.swpu.cins.event.analyse.platform.exception.BaseException;
import cn.edu.swpu.cins.event.analyse.platform.exception.IlleagalArgumentException;
import cn.edu.swpu.cins.event.analyse.platform.model.persistence.DailyEvent;
import cn.edu.swpu.cins.event.analyse.platform.model.view.ChartPoint;
import cn.edu.swpu.cins.event.analyse.platform.service.ReportService;
import cn.edu.swpu.cins.event.analyse.platform.utility.ChartGenerator;
import org.jfree.chart.JFreeChart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by LLPP on 2017/6/19.
 */
@Service
public class ReportServiceImpl implements ReportService {
    private DailyEventDao dailyEventDao;
    private final String[] monthsChar = {"","一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二"};

    @Autowired
    public ReportServiceImpl(DailyEventDao dailyEventDao) {
        this.dailyEventDao = dailyEventDao;
    }

    @Override
    public Map<String, Object> getReportDataMap(int year, int issue) throws Exception {
        try {
            Map<String, Object> reportDataMap = new HashMap<>();

            int beginMonth = issue ;//起始月

            int endMonth = issue + 1;//结束月

            LocalDateTime localTime = LocalDateTime.now();

            //日期校验
            if (issue > 11 || issue < 1 || localTime.isBefore(LocalDateTime.of(year,endMonth,15,0,0,0,0)))
                throw new IlleagalArgumentException();

            Date beginTime;//检索开始日期

            Date endTime ;//检索结束日期

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 00:00:00");

            Calendar calendar = new GregorianCalendar(year, beginMonth - 1, 1,0,0,0);//不可使用无参构造器，因为会读入系统时间的毫秒数造成干扰

            calendar.set(year, beginMonth - 1, 1,0,0,0); //Month value is 0-based. e.g., 0 for January.

            beginTime = calendar.getTime();

            calendar.set(year, endMonth, 1,0,0,0);

            endTime = calendar.getTime();

            List<DailyEvent> list = dailyEventDao.selectEventsBetweenTime(dateFormat.format(beginTime), dateFormat.format(endTime), "百度贴吧", false);

            List<DailyEvent> beginList = list.stream()
                    .filter(dailyEvent -> {
                        calendar.setTime(dailyEvent.getPostTime());
                        return calendar.get(Calendar.MONTH) % issue != 0; //Month value is 0-based. e.g., 0 for January.
                    })
                    .collect(Collectors.toList());//奇数月事件列表

            List<DailyEvent> endList = list.stream()
                    .filter(dailyEvent -> {
                        calendar.setTime(dailyEvent.getPostTime());
                        return calendar.get(Calendar.MONTH) % issue == 0;
                    })
                    .collect(Collectors.toList());//偶数月事件列表

            int beginEventCount = beginList.size();//奇数月事件数目

            int endEventCount = endList.size();//偶数月事件数目

            int beginCommentCount = (int) beginList
                    .stream()
                    .mapToInt(DailyEvent::getFollowCount)
                    .summaryStatistics()
                    .getSum();//奇数月事件评论数

            int endCommentCount = (int) endList
                    .stream()
                    .mapToInt(DailyEvent::getFollowCount)
                    .summaryStatistics()
                    .getSum();//偶数月事件评论数

            int beginHandledCount = (int) beginList
                    .stream()
                    .filter(dailyEvent -> (dailyEvent.getCollectionStatus() == 1))
                    .count();//偶数月事件处置数

            int endHandledCount = (int) endList
                    .stream()
                    .filter(dailyEvent -> (dailyEvent.getCollectionStatus() == 1))
                    .count();//偶数月事件处置数

            int heatCount = (int) list
                    .stream()
                    .filter(dailyEvent -> (dailyEvent.getFollowCount() > 20))
                    .count();//热点事件数

            int eventCount = endEventCount + beginEventCount;//统计事件总数

            String beginMonthChar = monthsChar[beginMonth];//奇月中文数字

            String endMonthChar = monthsChar[endMonth];//偶数月中文数字

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
            String generateDate = dateTimeFormatter.format(LocalDateTime.now());//报表生成日期

            calendar.setTime(endTime);
            calendar.add(Calendar.DATE,-1);
            Date endDateOfEndMonth = calendar.getTime();
            List<ChartPoint> pointList = ChartGenerator.getChartPoints(list, beginTime.getTime(), endDateOfEndMonth.getTime(), ChartDataEnum.POSTCOUNT.getDataType());
            JFreeChart doubleMonthChart = ChartGenerator.generateChart(pointList, "专题信息量趋势图",ChartTypeEnum.DOUBLE_MONTH);

            calendar.setTime(beginTime);
            calendar.add(Calendar.MONTH,1);
            calendar.add(Calendar.DATE,-1);
            Date endDateOfBeginMonth = calendar.getTime();
            List<ChartPoint> pointList1 = ChartGenerator.getChartPoints(beginList, beginTime.getTime(), endDateOfBeginMonth.getTime(), ChartDataEnum.POSTCOUNT.getDataType());
            JFreeChart beginMonthChart = ChartGenerator.generateChart(pointList1, beginMonthChar + "月份贴吧主题数趋势图", ChartTypeEnum.SINGLE_MONTH);

            List<ChartPoint> pointList2 = ChartGenerator.getChartPoints(beginList, beginTime.getTime(), endDateOfBeginMonth.getTime(), ChartDataEnum.FOLOWCOUNT.getDataType());
            JFreeChart beginCommentChart = ChartGenerator.generateChart(pointList2, beginMonthChar + "月份贴吧跟帖数趋势图", ChartTypeEnum.SINGLE_MONTH);

            calendar.setTime(endDateOfBeginMonth);
            calendar.add(Calendar.DATE,1);
            Date beginDateOfEndMonth = calendar.getTime();
            List<ChartPoint> pointList3 = ChartGenerator.getChartPoints(endList, beginDateOfEndMonth.getTime(), endDateOfEndMonth.getTime(), ChartDataEnum.POSTCOUNT.getDataType());
            JFreeChart endMonthChart = ChartGenerator.generateChart(pointList3, endMonthChar + "月份贴吧主题数趋势图", ChartTypeEnum.SINGLE_MONTH);

            List<ChartPoint> pointList4 = ChartGenerator.getChartPoints(endList, beginDateOfEndMonth.getTime(), endDateOfEndMonth.getTime(), ChartDataEnum.FOLOWCOUNT.getDataType());
            JFreeChart endCommentChart = ChartGenerator.generateChart(pointList4, endMonthChar + "月份贴吧跟帖数趋势图", ChartTypeEnum.SINGLE_MONTH);

            reportDataMap.put("year", year);
            reportDataMap.put("generateDate", generateDate);
            reportDataMap.put("beginMonth", beginMonth);
            reportDataMap.put("endMonth", endMonth);
            reportDataMap.put("beginMonthChar", beginMonthChar);
            reportDataMap.put("endMonthChar", endMonthChar);
            reportDataMap.put("beginCommentCount", beginCommentCount);
            reportDataMap.put("endCommentCount", endCommentCount);
            reportDataMap.put("endHandledCount", endHandledCount);
            reportDataMap.put("beginHandledCount", beginHandledCount);
            reportDataMap.put("beginEventCount", beginEventCount);
            reportDataMap.put("endEventCount", endEventCount);
            reportDataMap.put("heatCount", heatCount);
            reportDataMap.put("eventCount", eventCount);
            //将图表数据转换为BASE64编码的字符串
            reportDataMap.put("doubleMonthChart", ChartGenerator.chartToBASE64(doubleMonthChart));
            reportDataMap.put("beginMonthChart", ChartGenerator.chartToBASE64(beginMonthChart));
            reportDataMap.put("beginCommentChart", ChartGenerator.chartToBASE64(beginCommentChart));
            reportDataMap.put("endCommentChart", ChartGenerator.chartToBASE64(endCommentChart));
            reportDataMap.put("endMonthChart", ChartGenerator.chartToBASE64(endMonthChart));

            return reportDataMap;
        }catch (BaseException e){
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }
}
