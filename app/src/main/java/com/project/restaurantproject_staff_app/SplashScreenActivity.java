package com.project.restaurantproject_staff_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.project.restaurantproject_staff_app.Common.Common;
import com.project.restaurantproject_staff_app.Retrofit.MyRestaurantAPI;
import com.project.restaurantproject_staff_app.Retrofit.RetrofitClient;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SplashScreenActivity extends AppCompatActivity {

    MyRestaurantAPI myRestaurantAPI ;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog ;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        init();
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                        FirebaseInstanceId.getInstance()
                                .getInstanceId()
                                .addOnFailureListener(e -> {
                                    Toast.makeText(SplashScreenActivity.this, "[GRT TOKEN]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                })
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()){
                                        //FaceBook account Kit Here
                                        Paper.book().write(Common.REMEMBER_FBID,account.getId());
                                        dialog.show();
                                        compositeDisposable.add(myRestaurantAPI.updateTokenToServer(
                                                Common.API_KEY,account.getId(),task.getResult().getToken())
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(tokenModel -> {
                                                    compositeDisposable.add(myRestaurantAPI.getRestaurantOwner(Common.API_KEY,account.getId())
                                                            .subscribeOn(Schedulers.io())
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe(restaurantOwnerModel -> {

                                                                if (restaurantOwnerModel.isSuccess()){
                                                                    //if user already in database
                                                                    //check permission of user
                                                                    Common.currentRestaurantOwner = restaurantOwnerModel.getResult().get(0);
                                                                    if (Common.currentRestaurantOwner.isStatus()){
                                                                        startActivity(new Intent(SplashScreenActivity.this,HomeActivity.class));
                                                                        finish();
                                                                    }else{
                                                                        Toast.makeText(SplashScreenActivity.this,R.string.permission_denied,Toast.LENGTH_SHORT);
                                                                    }

                                                                }else{
                                                                    // if the user is a new one
                                                                    startActivity(new Intent(SplashScreenActivity.this,UpdateInformationActivity.class));
                                                                    finish();
                                                                }
                                                                dialog.dismiss();

                                                            },throwable -> {
                                                                Toast.makeText(SplashScreenActivity.this,"[Get User]"+throwable.getMessage(),Toast.LENGTH_SHORT);
                                                            }));
                                        }
                                        ,throwable -> {
                                                    Toast.makeText(SplashScreenActivity.this, "[UPDATE TOKEN]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                        ));
                                    }
                                });
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(SplashScreenActivity.this,"You Must accept that persmission in order to use our app",Toast.LENGTH_SHORT);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }

    private void init() {
        Paper.init(this);
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(MyRestaurantAPI.class);
    }
}
