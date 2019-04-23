package com.georeminder.ui.dashboard.requests

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import com.georeminder.base.BaseBindingListAdapter
import com.georeminder.base.BaseBindingViewHolder
import com.georeminder.data.model.other.ReminderData
import com.georeminder.databinding.ItemReceivedRequestBinding

/**
 * Created by Darshna Desai on 16/1/19.
 */
class ReceivedRequestsAdapter(context: Context, isRequested: Boolean) :
        BaseBindingListAdapter<ReminderData>(ReminderData.diffUtil) {

    var context: Context? = context
    var isReq: Boolean = isRequested

    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return ItemReceivedRequestBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, pos: Int) {
        val binding = DataBindingUtil.getBinding<ItemReceivedRequestBinding>(holder.itemView)
        if (getItem(pos) != null) {
            binding?.reminderData = getItem(pos)
            binding?.isRequested = isReq

            holder.itemView.setOnClickListener { onViewClick(holder.itemView, pos) }
            binding?.ivAccept?.setOnClickListener { onViewClick(binding.ivAccept, pos) }
            binding?.ivReject?.setOnClickListener { onViewClick(binding.ivReject, pos) }
        } else {
            binding?.reminderData = null
            binding?.isRequested = isReq
        }
    }

}