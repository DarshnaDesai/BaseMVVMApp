package com.georeminder.listener

/**
 * Created by Darshna Desai on 28/1/19.
 */
interface onApiResponse<T> {
    fun onDataFetched(apiResponse: T)
}