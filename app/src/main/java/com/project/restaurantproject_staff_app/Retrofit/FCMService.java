package com.project.restaurantproject_staff_app.Retrofit;

import com.project.restaurantproject_staff_app.Model.FCMResponse;
import com.project.restaurantproject_staff_app.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface FCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key={"Put Your FCM Key HERE"}"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
