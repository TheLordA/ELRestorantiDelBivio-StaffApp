package com.project.restaurantproject_staff_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.project.restaurantproject_staff_app.Common.Common;
import com.project.restaurantproject_staff_app.Retrofit.MyRestaurantAPI;
import com.project.restaurantproject_staff_app.Retrofit.RetrofitClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class UpdateInformationActivity extends AppCompatActivity {

    MyRestaurantAPI myRestaurantAPI ;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog ;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.btn_update)
    Button  btn_update;
    @BindView(R.id.edt_user_name)
    EditText edt_user_name;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_information);

        ButterKnife.bind(this);
        init();
        initView();
    }

    private void initView() {
        toolbar.setTitle("Update Information");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btn_update.setOnClickListener(v -> {
            dialog.show();

            // AccountKit OnSuccess
            compositeDisposable.add(myRestaurantAPI.updateRestaurantOwnerModel(Common.API_KEY,
                    account.getPhoneNumber().toString(),
                    TextUtils.isEmpty(edt_user_name.getText().toString())?"Unk Name":edt_user_name.getText().toString(),
                    account.getId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(updateRestaurantOwnerModel -> {
                            if (updateRestaurantOwnerModel.isSuccess()){
                                compositeDisposable.add(myRestaurantAPI.getRestaurantOwner(Common.API_KEY,
                                        account.getId())
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(restaurantOwnerModel -> {
                                            if (restaurantOwnerModel.isSuccess()){
                                                Common.currentRestaurantOwner = restaurantOwnerModel.getResult().get(0);
                                                if (Common.currentRestaurantOwner.isStatus()){
                                                    startActivity(new Intent(UpdateInformationActivity.this,HomeActivity.class));
                                                }else{
                                                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                                                }
                                                dialog.dismiss();
                                            }else{
                                                dialog.dismiss();
                                                Toast.makeText(this, restaurantOwnerModel.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        ,throwable -> {
                                                    dialog.dismiss();
                                                    Toast.makeText(this, "[GET USER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        })
                                );
                            }else{
                                dialog.dismiss();
                                Toast.makeText(this, updateRestaurantOwnerModel.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                    },
                    throwable -> {
                        dialog.dismiss();
                        Toast.makeText(UpdateInformationActivity.this, "[UPDATE]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    })
            );
            //AccountKit OnError
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
        });

    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(MyRestaurantAPI.class);
    }
}
