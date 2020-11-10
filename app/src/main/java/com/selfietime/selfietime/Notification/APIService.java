package com.selfietime.selfietime.Notification;

import com.selfietime.selfietime.Notification.MyResponse;
import com.selfietime.selfietime.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {


    @Headers(
            {
                    "Content-type:application/json",
                    "Authorization:key=AAAAgu3BzJE:APA91bHdQDB42CB3J0s_N3Gcvr0TIbDRQO4YQ9_M-IgFwut5GRVUbVZqLtg4QQcE8lbuQAGhYzeD6c1QDLCZQs09bob5BkQmvmQZV_WVPuWVd4KaFrkGtnptrnRnK7GV6QrNir-AiEH9"

            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
