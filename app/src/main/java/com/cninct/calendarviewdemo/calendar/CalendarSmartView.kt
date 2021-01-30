package com.cninct.calendarviewdemo.calendar

import android.content.Context
import android.util.AttributeSet
import android.util.SparseArray
import android.util.SparseIntArray
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.IntRange
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cninct.calendarviewdemo.R
import kotlinx.android.synthetic.main.qw_calendar_smart_layout.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class CalendarSmartView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {
    private var order = 0//日历时间顺序
    private var effect: Int = 0//效果，0只是显示，1单选，2范围选择
    private var afterEnable: Boolean = false//是否选今天之后的数据
    private var rangeYear = 5//年-当年前后跨度,例：今年为2020年，则开始年为2015，结束年为2025
    private var fastIndex: SparseIntArray = SparseIntArray()//快速索引

    private val mAdapter = CalendarAdapter()
    private var onClick: ((dateStr: String) -> Unit)? = null//点击回调

    /**
     * 缓存已选择的日期
     * 如果为单选，只有一个
     * 如果为多选，开始日期key=0，结束日期key=1
     */
    private val selData: SparseArray<String> = SparseArray(2)

    /**
     * 设置日历效果，0只是显示，1单选，2范围选择
     */
    fun setEffect(@IntRange(from = 0, to = 2) effect: Int) {
        this.effect = effect
    }

    /**
     * 滚动到指定日期
     */
    fun scrollToDate(date: String? = null) {
        val def = SimpleDateFormat("yyyy年MM月", Locale.CHINA)
        val d = date.dateStrToInt().toString()
        val dateStr = when {
            d.length >= 6 -> {
                d.substring(0, 4) + "年" + d.substring(4, 6) + "月"
            }
            else -> {
                def.format(Calendar.getInstance().time)
            }
        }
        val index = getIndexByDate(dateStr)
        calListView.scrollToPosition(index)
        headView.findViewById<TextView>(R.id.tvMonth).text = dateStr
        headView.y = 0f
    }

    /**
     * 设置点击回调
     */
    fun setOnClickListener(onClick: (dateStr: String) -> Unit) {
        this.onClick = onClick
    }

    init {
        inflate(context, R.layout.qw_calendar_smart_layout, this)
        parseAttr(context, attrs)
        val manager = GridLayoutManager(context, 7)
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (-1000 == mAdapter.data[position].dateType) 7 else 1
            }
        }
        calListView.layoutManager = manager
        calListView.adapter = mAdapter
        calListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var headViewHeight = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstView = recyclerView.getChildAt(0)
                val text = mAdapter.data[recyclerView.getChildAdapterPosition(firstView)].monthStr
                headView.findViewById<TextView>(R.id.tvMonth).text = text
                //查询当前可见中第一个月份的item
                var showFirstMonthStr = ""
                var showFirstMonthTop = 0
                var index = -1
                for (i in 0 until recyclerView.childCount) {
                    val childView = recyclerView.getChildAt(i)
                    if (1 == recyclerView.getChildViewHolder(childView).itemViewType) {
                        index = recyclerView.getChildAdapterPosition(childView)
                        showFirstMonthStr =
                                mAdapter.data[index].monthStr
                        showFirstMonthTop = childView.top
                        break
                    }
                }
                var offsetTop = 0
                if (text != showFirstMonthStr && showFirstMonthTop < headViewHeight) {
                    offsetTop = headViewHeight - showFirstMonthTop
                }
                if (index == -1) {
                    offsetTop = 0
                }
                headView.y = -offsetTop.toFloat()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                headViewHeight = headView.height
            }
        })
        if (effect > 0 || onClick != null) {
            mAdapter.onClick = { dateStr, position ->
                when (effect) {
                    1 -> {//单选
                        if (selData.size() > 0) {
                            //重置
                            mAdapter.data[fastIndex[selData[0].dateStrToInt()]].itemType = 0
                            selData.clear()
                        }
                        selData.put(0, dateStr)
                        mAdapter.data[position].itemType = -3
                        mAdapter.notifyDataSetChanged()
                        mAdapter.notifyDataSetChanged()
                    }
                    2 -> {//多选
                        when (selData.size()) {
                            0 -> {
                                selData.put(0, dateStr)
                                mAdapter.data[position].itemType = -3
                            }
                            1 -> {
                                val date = selData[0]
                                val datePosition = fastIndex[date.dateStrToInt()]
                                if (position > datePosition) {
                                    selData.put(1, dateStr)
                                    mAdapter.data[datePosition].itemType = -1
                                    mAdapter.data[position].itemType = -2
                                    if (position - datePosition > 1) {
                                        for (pos in datePosition + 1 until position) {
                                            mAdapter.data[pos].itemType = -4
                                        }
                                    }
                                } else if (position < datePosition) {
                                    selData.put(1, date)
                                    selData.put(0, dateStr)
                                    mAdapter.data[datePosition].itemType = -2
                                    mAdapter.data[position].itemType = -1
                                    if (datePosition - position > 1) {
                                        for (pos in position + 1 until datePosition) {
                                            mAdapter.data[pos].itemType = -4
                                        }
                                    }
                                }

                            }
                            2 -> {
                                val start = fastIndex[selData[0].dateStrToInt()]
                                val end = fastIndex[selData[1].dateStrToInt()]
                                for (pos in start..end) {
                                    mAdapter.data[pos].itemType = 0
                                }
                                selData.clear()
                                selData.put(0, dateStr)
                                mAdapter.data[position].itemType = -3
                            }
                        }
                        mAdapter.notifyDataSetChanged()
                    }
                    else -> {
                        onClick?.let {
                            it(dateStr)
                        }
                    }
                }
            }
        }
        mAdapter.setNewData(productData())
        scrollToDate()
    }

    /**
     * 解析自定义属性
     */
    private fun parseAttr(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CalendarSmartView)
        order = typedArray.getInteger(R.styleable.CalendarSmartView_qw_orader, 0)
        typedArray.recycle()
    }

    /**
     * 产生数据
     */
    private fun productData(): List<CalendarItem> {
        val data = mutableListOf<CalendarItem>()//数据
        val cal = Calendar.getInstance(Locale.CHINA)
        val curY = cal.get(Calendar.YEAR)
        val curM = cal.get(Calendar.MONTH)
        val curD = cal.get(Calendar.DAY_OF_MONTH)
        val startYear = cal.get(Calendar.YEAR) - rangeYear
        val endYear = cal.get(Calendar.YEAR) + rangeYear
        var index = 0//索引
        for (y in startYear..endYear) {
            var year = y
            if (order == 1) {
                year = endYear - y + startYear
            }
            for (m in 0..11) {
                var month = m
                if (order == 1) {
                    month = 11 - m
                }
                val monthCal = Calendar.getInstance()
                monthCal.set(year, month, 1)
                val monthStr = "${year}年${String.format("%02d", month + 1)}月"
                data.add(CalendarItem(monthStr = monthStr, dateType = -1000))
                fastIndex.put(monthStr.dateStrToInt(), index)
                index++
                val week = monthCal.get(Calendar.DAY_OF_WEEK)
                if (week > 1) {
                    for (i in 1 until week) {
                        data.add(CalendarItem(dateType = -100, monthStr = monthStr))
                        index++
                    }
                }
                for (day in 1..monthCal.getActualMaximum(Calendar.DATE)) {
                    val dateStr = "${monthStr}-${String.format("%02d", day)}"
                    data.add(
                            CalendarItem(
                                    monthStr = monthStr,
                                    dateStr = dateStr,
                                    dayStr = String.format("%02d", day),
                                    dateType = if (year > curY || (year == curY && month > curM) || (year == curY && month == curM && day > curD)) {
                                        -1
                                    } else {
                                        when ((week + day - 1) % 7) {
                                            1 -> 102
                                            0 -> 101
                                            else -> 0
                                        }
                                    }
                            )
                    )
                    fastIndex.put(dateStr.dateStrToInt(), index)
                    index++
                }
            }
        }
        return data
    }

    private fun String?.dateStrToInt(): Int {
        if (this.isNullOrBlank()) return 0
        val m = Pattern.compile("[^0-9]").matcher(this).also {
            it.find()
        }
        return m.replaceAll("").trim().toInt()
    }

    /**
     * 获取指定日期的下标
     */
    private fun getIndexByDate(dateStr: String) = fastIndex[dateStr.dateStrToInt()]

}