package com.project.restaurantproject_staff_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.project.restaurantproject_staff_app.Common.Common;
import com.project.restaurantproject_staff_app.Retrofit.MyRestaurantAPI;
import com.project.restaurantproject_staff_app.Retrofit.RetrofitClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final int APP_REQUEST_CODE = 1234 ;
    MyRestaurantAPI myRestaurantAPI ;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog ;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @OnClick(R.id.btn_sign_in)
    void loginUser(){
        // faceBokk Account KIt Goes here
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        Init();
        
    }

    private void Init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(MyRestaurantAPI.class);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == APP_REQUEST_CODE){
            //AccountKitLogin
            String toastMessage ;
            if (loginResult.getError()!= null){
                toastMessage = loginResult.getError.getErrorType.getMessage;
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
            }else if (loginResult.wasCancelled()){
                toastMessage = "Login  Cancelled";
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
            }else{
                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnFailureListener(e -> {
                            Toast.makeText(MainActivity.this, "[GET TOKEN]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        })
                        .addOnCompleteListener(task -> {
                            //FaceBook account Kit Here
                            dialog.show();
                            compositeDisposable.add(myRestaurantAPI.updateTokenToServer(Common.API_KEY,account.getId(),task.getResult().getToken())
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
                                                                    startActivity(new Intent(MainActivity.this,HomeActivity.class));
                                                                    finish();
                                                                }else{
                                                                    Toast.makeText(MainActivity.this,R.string.permission_denied,Toast.LENGTH_SHORT);
                                                                }

                                                            }else{
                                                                // if the user is a new one
                                                                startActivity(new Intent(MainActivity.this,UpdateInformationActivity.class));
                                                                finish();
                                                            }
                                                            dialog.dismiss();

                                                        },throwable -> {
                                                            Toast.makeText(MainActivity.this,"[Get User]"+throwable.getMessage(),Toast.LENGTH_SHORT);
                                                        }));
                                       },
                                       throwable -> {
                                            dialog.dismiss();
                                            Toast.makeText(this, "[UPDATE TOKEN]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                       })
                                    );
                        });
            }
        }
    }
}
