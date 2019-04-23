package com.georeminder.ui.dashboard.requests

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import com.georeminder.base.BaseBindingAdapter
import com.georeminder.base.BaseBindingViewHolder
import com.georeminder.databinding.ItemAddReminderBinding

/**
 * Created by Darshna Desai on 16/1/19.
 */
class AddRequestItemAdapter(context: Context) : BaseBindingAdapter<String>() {
    var context: Context? = context

    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return ItemAddReminderBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, pos: Int) {
        val binding = DataBindingUtil.getBinding<ItemAddReminderBinding>(holder.itemView)
        val data = items[pos]
        binding?.data = data
        binding?.ivRemove?.setOnClickListener { holder.onClick(it) }
        binding?.etReminderItem?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                items[holder.adapterPosition] = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
    }

    fun getData(): ArrayList<String> {
        return items
    }

}