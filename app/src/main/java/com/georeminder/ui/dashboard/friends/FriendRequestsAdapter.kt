package com.georeminder.ui.dashboard.friends

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import com.georeminder.base.BaseBindingListAdapter
import com.georeminder.base.BaseBindingViewHolder
import com.georeminder.data.model.other.FriendRequest
import com.georeminder.data.model.other.FriendRequest.CREATOR.diffUtil
import com.georeminder.databinding.ItemRequestedFriendListBinding

/**
 * Created by Darshna Desai on 16/1/19.
 */
class FriendRequestsAdapter(context: Context, isRequested: Boolean) : BaseBindingListAdapter<FriendRequest>(diffUtil) {

    var context: Context? = context
    var isReq: Boolean = isRequested

    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return ItemRequestedFriendListBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, pos: Int) {
        val binding = DataBindingUtil.getBinding<ItemRequestedFriendListBinding>(holder.itemView)
        binding?.isRequested = isReq
        if (getItem(pos) != null) {
            binding?.friendData = getItem(pos)
            holder.itemView.setOnClickListener { onViewClick(holder.itemView, pos) }
            binding?.ivAccept?.setOnClickListener { onViewClick(binding.ivAccept, pos) }
            binding?.ivReject?.setOnClickListener { onViewClick(binding.ivReject, pos) }
        }else{
            binding?.friendData = null
        }
    }

}