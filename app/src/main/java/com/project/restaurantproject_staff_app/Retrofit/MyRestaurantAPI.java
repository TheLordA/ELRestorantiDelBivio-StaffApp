package com.project.restaurantproject_staff_app.Retrofit;

import com.project.restaurantproject_staff_app.Model.HotFoodModel;
import com.project.restaurantproject_staff_app.Model.MaxOrderModel;
import com.project.restaurantproject_staff_app.Model.OrderDetailModel;
import com.project.restaurantproject_staff_app.Model.OrderModel;
import com.project.restaurantproject_staff_app.Model.RestaurantOwnerModel;
import com.project.restaurantproject_staff_app.Model.ShippingOrderModel;
import com.project.restaurantproject_staff_app.Model.TokenModel;
import com.project.restaurantproject_staff_app.Model.UpdateOrderModel;
import com.project.restaurantproject_staff_app.Model.UpdateRestaurantOwnerModel;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface MyRestaurantAPI {

    // NOTE : some endpoint have fbid check and others works with Jwt Token Sync with the server

    @GET("restaurantowner")
    Observable<RestaurantOwnerModel> getRestaurantOwner(@Query("key") String key ,
                                                        @Query("fbid") String fbid);
    @POST("restaurantowner")
    @FormUrlEncoded
    Observable<UpdateRestaurantOwnerModel> updateRestaurantOwnerModel(@Field("key") String key,
                                                                      @Field("userPhone") String userPhone,
                                                                      @Field("userName") String userName,
                                                                      @Field("fbid") String fbid);

    @GET("orderbyrestaurant")
    Observable<OrderModel> getOrder(@Query("key") String key,
                                    @Query("restaurantId") String restaurantId,
                                    @Query("from") int from,
                                    @Query("to") int to);

    @GET("maxorderbyrestaurant")
    Observable<MaxOrderModel> getMaxOrder(@Query("key") String key,
                                          @Query("restaurantId") String restaurantId);

    @GET("orderDetailbyrestaurant")
    Observable<OrderDetailModel> getOrderDetail(@Query("key") String key,
                                                     @Query("orderId") int orderId);

    @GET("token")
    Observable<TokenModel> getToken(@HeaderMap Map<String,String> headers);

    @POST("token")
    @FormUrlEncoded
    Observable<TokenModel> updateTokenToServer(@HeaderMap Map<String,String> headers,
                                               @Field("token") String token);

    @PUT("updateOrder")
    @FormUrlEncoded
    Observable<UpdateOrderModel> updateOrderStatus(@Field("key") String apikey,
                                                   @Field("orderId") int orderId,
                                                   @Field("orderStatus") int orderStatus);

    @GET("hotfood")
    Observable<HotFoodModel> getHotFood(@HeaderMap Map<String,String> headers);

    @POST("shippingorder")
    @FormUrlEncoded
    Observable<ShippingOrderModel> setShippingOrder(@Field("key") String apiKey,
                                                    @Field("orderId") int orderId,
                                                    @Field("restaurantId") int restaurantId);

    @GET("shippingorder")
    Observable<ShippingOrderModel> getShippingOrder(@Query("key") String key,
                                                    @Query("restaurantId") int restaurantId);

}
