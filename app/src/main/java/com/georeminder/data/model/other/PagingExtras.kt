package com.georeminder.data.model.other

import android.arch.lifecycle.MutableLiveData
import com.georeminder.data.remote.ApiService

//Created by imobdev-rujul on 30/1/19
data class PagingExtras(val apiServiceObj: ApiService, val param: HashMap<String, String>, val
horizontalPb: MutableLiveData<Boolean>, val apiErrorMessage: MutableLiveData<String>, val internetError: String)