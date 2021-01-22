package com.cninct.calendarviewdemo.calendar

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.cninct.calendarviewdemo.R


class CalendarAdapter : RecyclerView.Adapter<ViewHolder>() {
    var data = listOf<CalendarItem>()

    var onClick: ((dateStr: String, position: Int) -> Unit)? = null

    fun setNewData(d: List<CalendarItem>) {
        data = d
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == 1) {
            VHMonth(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.qw_calendar_item_month_layout, parent, false)
            )
        } else {
            VHDay(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.qw_calendar_item_day_layout, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is VHMonth) {
            holder.tvMonth.text = data[position].monthStr
        } else {
            val helper = holder as VHDay
            helper.tvDay.text = data[position].dayStr
            helper.squareView.setBackGround(data[position].itemType)
            helper.tvDay.setTextColor(
                if (data[position].dateType == 0) Color.BLACK else Color.DKGRAY
            )
            if (data[position].dateType >= 0) {
                onClick?.apply {
                    helper.itemView.setOnClickListener {
                        this(data[position].dateStr, position)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int) = if (data[position].dateType == -1000) 1 else 0

    class VHDay(v: View) : ViewHolder(v) {
        val tvDay: TextView = v.findViewById(R.id.tvDay)
        val squareView: SquareLayout = itemView as SquareLayout
    }

    class VHMonth(v: View) : ViewHolder(v) {
        val tvMonth: TextView = v.findViewById(R.id.tvMonth)
    }
}