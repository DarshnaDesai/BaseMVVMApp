package com.georeminder.ui.dashboard.more

import android.arch.paging.DataSource
import com.georeminder.data.model.other.NotificationData
import com.georeminder.data.model.other.PagingExtras

/**
 * Created by Darshna Desai on 18/2/19.
 */
class NotificationDataFactory(private val pagingExtra: PagingExtras) : DataSource.Factory<Long, NotificationData>() {
    override fun create(): DataSource<Long, NotificationData> {
        return NotificationDataSource(pagingExtra)
    }
}