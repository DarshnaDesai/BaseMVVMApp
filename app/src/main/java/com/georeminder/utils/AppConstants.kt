package com.georeminder.utils


object AppConstants {

    /** The base URL of the API */
    // const val BASE_URL: String = "https://jsonplaceholder.typicode.com"
    const val DEVICE_TYPE: String = "android"
    const val HELP_URL = "http://georeminder.reviewprototypes.com/help"
    const val TERMS_URL = "http://georeminder.reviewprototypes.com/terms"

    const val DB_NAME = "base_mvvm.db"
    const val DEFAULT_DATE_FORMAT: String = "dd/MM/yyyy"

    // Request Codes
    const val PICK_IMAGE_GALLERY_REQUEST_CODE = 31
    const val PICK_IMAGE_CAMERA_REQUEST_CODE = 32

    //Extras
    const val EXTRA_DATA = "data"
    const val EXTRA_POSITION = "position"
    const val EXTRA_IS_REQUESTED = "isRequested"
    const val EXTRA_REQ_STATUS_CHANGE = "requestStatusChange"
    const val EXTRA_URL = "url"
    const val EXTRA_REQUEST_CODE = 1000

    // Paging
    const val ITEMS_LIMIT = 10
}
