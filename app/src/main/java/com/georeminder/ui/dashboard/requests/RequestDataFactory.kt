package com.georeminder.ui.dashboard.requests

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.DataSource
import com.georeminder.data.model.other.PagingExtras
import com.georeminder.data.model.other.ReminderData
import com.georeminder.data.remote.ApiService

//Created by imobdev-rujul on 24/1/19
open class RequestDataFactory(val pagingExtras: PagingExtras) :
        DataSource.Factory<Long, ReminderData>() {

    lateinit var requestDataSource: RequestDataSource

    /**
     * Create a DataSource.
     *
     *
     * The DataSource should invalidate itself if the snapshot is no longer valid. If a
     * DataSource becomes invalid, the only way to query more data is to create a new DataSource
     * from the Factory.
     *
     *
     * [LivePagedListBuilder] for example will construct a new PagedList and DataSource
     * when the current DataSource is invalidated, and pass the new PagedList through the
     * `LiveData<PagedList>` to observers.
     *
     * @return the new DataSource.
     */
    override fun create(): DataSource<Long, ReminderData> {

        requestDataSource = RequestDataSource(pagingExtras)
        /*requestDataSourceLiveData.value = requestDataSource*/
        return requestDataSource
    }
}