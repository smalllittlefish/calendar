package com.cninct.calendarviewdemo.calendar

/**
 * 日历的数据类
 */
class CalendarItem(
    val dayStr: String = "",//例：01,02
    val monthStr: String = "",//例：2020-01
    val dateStr: String = "",//例：2020-01-01
    /**
     * 日期类型
     * 0，正常
     * -100，占位
     * 101，周六
     * 102，周日
     * -1000，月份
     * -1不可点击
     */
    val dateType: Int = 0,
    /**
     * 数据类型
     * 0，正常状态
     * -3，选中
     * -1，开始日期
     * -2，结束日期
     */
    var itemType: Int = 0
)