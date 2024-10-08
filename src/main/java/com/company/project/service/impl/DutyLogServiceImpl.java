package com.company.project.service.impl;

import com.company.project.entity.DutyLog;
import com.company.project.entity.Employee;
import com.company.project.service.IDutyLogService;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @description: 实现生产人员出勤统计类
 * @author: yuanhe0609
 * @time: 2024/10/10
 */
@Service
public class DutyLogServiceImpl implements IDutyLogService {
    /**
     * @description 定义日期格式
     * @type SimpleDateFormat
     * @default yyyy-MM-dd HH:mm:ss
     * */
    private final SimpleDateFormat sdfWithTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * @description 定义日期格式
     * @type SimpleDateFormat
     * @default yyyy-MM-dd
     * */
    private final SimpleDateFormat sdfNoTime = new SimpleDateFormat("yyyy-MM-dd");
    //
    /**
     * @description 定义浮点数小数点个数
     * @type DecimalFormat
     * @default 1位数
     * */
    private final DecimalFormat df = new DecimalFormat("0.0");
    /**
     * @description 用于数据库操作的生产人员姓名字段的字段名
     * @type String
     * @default xm
     * */
    private String sqlName = "xm";
    /**
     * @description 用于数据库操作的生产人员身份证号字段的字段名
     * @type String
     * @default sfzh
     * */
    private String sqlIdNum = "sfzh";
    /**
     * @description 用于数据库操作的生产人员当日上岗时间字段的字段名
     * @type String
     * @default sgsj
     * */
    private String sqlOnDutyTime = "sgsj";
    /**
     * @description 用于数据库操作的生产人员离岗时间字段的字段名
     * @type String
     * @default lgsj
     * */
    private String sqlOffDutyTime = "lgsj";
    /**
     * @description 用于数据库操作的休假日类型字段的字段名
     * @type String
     * @default changetype
     * */
    private String sqlHolidayType = "changetype";
    /**
     * @description 设置用于数据库操作的生产人员姓名字段的字段名
     * @param sqlName 要使用的字段名
     * @return void
     * */
    public void setSqlName(String sqlName) {
        this.sqlName = sqlName;
    }
    /**
     * @description 设置用于数据库操作的休假日类型字段的字段名
     * @param sqlHolidayType 要使用的字段名
     * @return void
     * */
    public void setSqlHolidayType(String sqlHolidayType) {
        this.sqlHolidayType = sqlHolidayType;
    }
    /**
     * @description 设置用于数据库操作的生产人员离岗时间字段的字段名
     * @param sqlOffDutyTime 要使用的字段名
     * @return void
     * */
    public void setSqlOffDutyTime(String sqlOffDutyTime) {
        this.sqlOffDutyTime = sqlOffDutyTime;
    }
    /**
     * @description 设置用于数据库操作的生产人员当日上岗时间字段的字段名
     * @param sqlOnDutyTime 要使用的字段名
     * @return void
     * */
    public void setSqlOnDutyTime(String sqlOnDutyTime) {
        this.sqlOnDutyTime = sqlOnDutyTime;
    }
    /**
     * @description 设置用于数据库操作的生产人员身份证号字段的字段名
     * @param sqlIdNum 要使用的字段名
     * @return void
     * */
    public void setSqlIdNum(String sqlIdNum) {
        this.sqlIdNum = sqlIdNum;
    }
    /**
     * @description sting类型转换到calendar类型
     * @param s 要变化的日期，String类型
     * @param sdf 要变化的格式
     * @return calendar 转化完成的日期，Calendar类型
     * */
    private Calendar StringToCalendar(String s, SimpleDateFormat sdf){
        try {
            Date date = sdf.parse(s);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * @description 获取时间差值
     * @param onJob 上岗时间
     * @param offJob 离岗时间
     * @return Float.valueOf((offJob.getTime().getTime() - onJob.getTime().getTime())/ (60 * 60 * 1000) % 24) 计算后的时间的差值
     * */
    private Float getJetLegHour(Calendar onJob,Calendar offJob){
        return Float.valueOf((offJob.getTime().getTime() - onJob.getTime().getTime())/ (60 * 60 * 1000) % 24);
    }
    /**
     * @description 获取分钟差值
     * @param onJob 上岗时间
     * @param offJob 离岗时间
     * @return Float.valueOf((offJob.getTime().getTime() - onJob.getTime().getTime())/ (60 * 1000) % 60) 计算后的分钟的差值
     * */
    private Float getJetLegMin(Calendar onJob,Calendar offJob){
        return Float.valueOf((offJob.getTime().getTime() - onJob.getTime().getTime())/ (60 * 1000) % 60);
    }
    /**
     * @description 获取秒差值
     * @param onJob 上岗时间
     * @param offJob 离岗时间
     * @return Float.valueOf((offJob.getTime().getTime() - onJob.getTime().getTime() / 1000) % 60) 计算后的秒的差值
     * */
    private Float getJetLegSec(Calendar onJob,Calendar offJob){
        return Float.valueOf((offJob.getTime().getTime() - onJob.getTime().getTime() / 1000) % 60);
    }
    /**
     * @description 获取当前星期周一到周五按（1-7）的顺序
     * @param onJob 上岗时间
     * @return onJob.get(Calendar.DAY_OF_WEEK)-1>0?onJob.get(Calendar.DAY_OF_WEEK)-1:7 当日的星期
     * */
    private int getWeekInt(Calendar onJob){
        return onJob.get(Calendar.DAY_OF_WEEK)-1>0?onJob.get(Calendar.DAY_OF_WEEK)-1:7;
    }
    /**
     * @description 计算在岗时间
     * @param onJob 上岗时间
     * @param offJob 离岗时间
     * @return 8 or getJetLegHour(onJob, offJob)+ Float.valueOf(df.format(getJetLegMin(onJob,offJob) / 60)) 日工作时间超过8小时返回8，否则返回实际时间
     * */
    private Float getWorkTime(Calendar onJob,Calendar offJob){
        if(getJetLegHour(onJob,offJob) > 8){
            return 8.0f;
        }else{
            return getJetLegHour(onJob, offJob)+ Float.valueOf(df.format(getJetLegMin(onJob,offJob) / 60));
        }
    }
    /**
     * @description 计算加班时间
     * @param onJob 上岗时间
     * @param offJob 离岗时间
     * @return 0 or getJetLegHour(onJob,offJob)-8 + Float.valueOf(df.format(getJetLegMin(onJob,offJob) / 60)) 日工作时间超过8小时，则返回超过的时间，否则返回0
     * */
    private Float getOverTime(Calendar onJob,Calendar offJob){
        if(getJetLegHour(onJob,offJob) > 8){
            return getJetLegHour(onJob,offJob)-8 + Float.valueOf(df.format(getJetLegMin(onJob,offJob) / 60));
        }else{
            return 0f;
        }
    }
    /**
     * @description 计算加班时间
     * @param ResultSet dutyLogResultSet
     * @param ResultSet holidayResultSet
     * @return list<DutyLog> result
     * */
    @Override
    public List<DutyLog> updateAttendanceList(ResultSet dutyLogResultSet,ResultSet holidayResultSet) throws SQLException {

        List<DutyLog> result = new ArrayList<>();

        while (dutyLogResultSet.next()) {
            DutyLog dutyLog = new DutyLog();
            //员工月度计算对象
            String name = dutyLogResultSet.getString(sqlName);
            String idNum = dutyLogResultSet.getString(sqlIdNum);
            Float weekdayWorkTime = 0f;
            Float weekdayOverTime = 0f;
            Float weekendWorkTime = 0f;
            Float weekendOverTime = 0f;
            Float festivalWorkTime = 0f;
            Float festivalOverTime = 0f;
            if (dutyLogResultSet.getString(sqlOnDutyTime) != null && dutyLogResultSet.getString(sqlOffDutyTime) != null) {
                //从数据库获取上岗时间和离岗时间
                Calendar onJob = StringToCalendar(dutyLogResultSet.getString(sqlOnDutyTime), sdfWithTime);
                Calendar offJob = StringToCalendar(dutyLogResultSet.getString(sqlOffDutyTime), sdfWithTime);
                //获取当日星期
                int dayOfWeekInt = getWeekInt(onJob);
                //输出
                System.out.println("-------------------------------------------------------------------");
                System.out.println("原上班时间:" + onJob.getTime());
                System.out.println("原下班时间:" + offJob.getTime());
                //设置正常出勤时间(早上8：00到晚上17：00)
                int year = onJob.get(Calendar.YEAR);
                int mouth = onJob.get(Calendar.MONTH) + 1;
                int day = onJob.get(Calendar.DAY_OF_MONTH);
                Calendar onJobTime = Calendar.getInstance();
                onJobTime.set(year, mouth - 1, day, 8, 0, 0);
                Calendar offJobTime = Calendar.getInstance();
                offJobTime.set(year, mouth - 1, day, 17, 0, 0);
                //前后10分钟不计入
                if (Math.abs(getJetLegMin(onJobTime, onJob) + getJetLegHour(onJobTime, onJob) * 60) <= 10) {
                    onJob = onJobTime;
                }
                if (Math.abs(getJetLegMin(offJobTime, offJob) + getJetLegHour(offJobTime, offJob) * 60) <= 10) {
                    offJob = offJobTime;
                }
                //输出
                System.out.println("现上班时间:" + onJob.getTime());
                System.out.println("现下班时间:" + offJob.getTime());
                //计算工时，并持久化
                if (holidayResultSet.next()) {
                    if (holidayResultSet.getString(sqlHolidayType).equals("1") || holidayResultSet.getString(sqlHolidayType).equals("3")) {
                        festivalWorkTime = getWorkTime(onJob, offJob);
                        festivalOverTime = getOverTime(onJob, offJob);
                    } else if (holidayResultSet.getString(sqlHolidayType).equals("2")) {
                        weekdayWorkTime = getWorkTime(onJob, offJob);
                        weekdayOverTime = getOverTime(onJob, offJob);
                    }
                } else {
                    if (dayOfWeekInt >= 6) {
                        weekendWorkTime = getWorkTime(onJob, offJob);
                        weekendOverTime = getOverTime(onJob, offJob);
                    } else {
                        weekdayWorkTime = getWorkTime(onJob, offJob);
                        weekdayOverTime = getOverTime(onJob, offJob);
                    }
                }
                dutyLog.setOnDutyTime(getWorkTime(onJob, offJob)+getOverTime(onJob, offJob));
                dutyLog.setOnWorkTime(getWorkTime(onJob, offJob)+getOverTime(onJob, offJob));
                dutyLog.setAttendanceOnWeekdays(weekdayWorkTime);
                dutyLog.setOverTimeOnWeekdays(weekdayOverTime);
                dutyLog.setAttendanceOnWeekends(weekendWorkTime);
                dutyLog.setOverTimeOnWeekends(weekendOverTime);
                dutyLog.setAttendanceOnHoliday(festivalWorkTime);
                dutyLog.setOverTimeOnHoliday(festivalOverTime);
                result.add(dutyLog);
            }
        }
        return result;
    }
    @Override
    public void calculateMonthWorkTime() {

    }


}
