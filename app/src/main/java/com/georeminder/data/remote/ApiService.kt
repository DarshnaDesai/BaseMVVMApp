package com.georeminder.data.remote

import com.georeminder.data.model.api.*
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*


interface ApiService {

    @GET("test=123")
    fun apiGet(): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("login")
    fun apiSignIn(@FieldMap params: HashMap<String, String>): Observable<UserResponse>
    /*{
        "status": true,
        "message": "Successfully logged in",
        "access_token": "fAne3v3yDfBlcvtyG7WDH20fSz6vy3jsU7QLWJR42MrLUetbsDqSzvzRfH3eI84d",
        "userData": {
        "user_id": "4",
        "user_first_name": "DD",
        "user_last_name": "DD",
        "user_email": "dd@mailinator.com",
        "user_phone": "1212121212",
        "user_dob": "1",
        "user_gender": "",
        "user_image_url": "http:\/\/georeminder.reviewprototypes.com\/assets\/images\/default.png",
        "user_created_date": "1546609462",
        "user_modified_date": "",
        "user_status": "1"
    },
        "authentication": true,
        "request_id": 296
    }*/

    @FormUrlEncoded
    @POST("register")
    fun apiSignUp(@FieldMap params: HashMap<String, String>): Observable<UserResponse>
    /*{
            "status": true,
            "message": "Registration done successfully.",
            "access_token": "cKUFtQSZwtqgk1JB2WEnrW6VrF9Sm3o8MXXle8pIGOOgSnIHvt7Ykl7ljH2yMzNq",
            "userData": {
            "user_id": "4",
            "user_first_name": "DD",
            "user_last_name": "DD",
            "user_email": "dd@mailinator.com",
            "user_phone": "1212121212",
            "user_dob": "1",
            "user_gender": "",
            "user_image_url": "",
            "user_created_date": "1546609462",
            "user_modified_date": "",
            "user_status": "1"
        },
            "authentication": true,
            "request_id": 294
        }*/

    @FormUrlEncoded
    @POST("forgot_password")
    fun apiForgotPassword(@FieldMap params: HashMap<String, String>): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("add_reminder")
    fun apiAddRequest(@FieldMap params: HashMap<String, String>): Observable<BaseResponse>

    @GET("reminder_list")
    fun apiGetReminderList(@QueryMap params: HashMap<String, String>): Observable<ReminderResponse>

    @GET("profile")
    fun apiProfile(@QueryMap params: HashMap<String, String>): Observable<UserResponse>

    @Multipart
    @POST("update_profile")
    fun apiUpdateProfile(@PartMap params: HashMap<String, RequestBody>): Observable<UserResponse>

    @FormUrlEncoded
    @PUT("change_password")
    fun apiChangePassword(@FieldMap params: HashMap<String, String>): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("reminder_status")
    fun apiUpdateRequestStatus(@FieldMap params: HashMap<String, String>): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("cancel_reminder")
    fun apiCancelReminder(@FieldMap params: HashMap<String, String>): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("friend_request_action")
    fun apiUpdateFriendRequestStatus(@FieldMap params: HashMap<String, String>): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("logout")
    fun apiLogout(@FieldMap params: HashMap<String, String>): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("search_friend")
    fun apiAddFriendRequest(@FieldMap params: HashMap<String, String>): Observable<AddFriendResponse>

    @GET("friends_list")
    fun apiFriendList(@QueryMap params: HashMap<String, String>): Observable<FriendListResponse>

    @FormUrlEncoded
    @POST("get_all_notifications")
    fun apiNotificationList(@FieldMap params: HashMap<String, String>): Observable<NotificationListResponse>

}