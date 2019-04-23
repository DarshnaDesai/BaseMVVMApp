package com.georeminder.ui.dashboard.more

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import com.georeminder.base.BaseBindingListAdapter
import com.georeminder.base.BaseBindingViewHolder
import com.georeminder.data.model.other.NotificationData
import com.georeminder.data.model.other.NotificationData.Companion.diffUtil
import com.georeminder.databinding.ItemNotificationListBinding

/**
 * Created by Darshna Desai on 16/1/19.
 */
class NotificationListAdapter(val context: Context) : BaseBindingListAdapter<NotificationData>(diffUtil) {

    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return ItemNotificationListBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, pos: Int) {
        val binding = DataBindingUtil.getBinding<ItemNotificationListBinding>(holder.itemView)
        binding?.notificationData = getItem(pos)
        holder.itemView.setOnClickListener { onViewClick(holder.itemView, pos) }
    }

}