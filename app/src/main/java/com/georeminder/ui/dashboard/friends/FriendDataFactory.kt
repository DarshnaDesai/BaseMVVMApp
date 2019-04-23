package com.georeminder.ui.dashboard.friends

import android.arch.paging.DataSource
import com.georeminder.data.model.other.FriendRequest
import com.georeminder.data.model.other.PagingExtras

/**
 * Created by Darshna Desai on 28/1/19.
 */
class FriendDataFactory(private val pagingExtra: PagingExtras) : DataSource.Factory<Long, FriendRequest>() {

    override fun create(): DataSource<Long, FriendRequest> {
        return FriendDataSource(pagingExtra)
    }
}