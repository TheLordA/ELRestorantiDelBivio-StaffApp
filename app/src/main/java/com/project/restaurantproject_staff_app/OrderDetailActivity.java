package com.project.restaurantproject_staff_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.project.restaurantproject_staff_app.Adapter.MyOrderDetailAdapter;
import com.project.restaurantproject_staff_app.Common.Common;
import com.project.restaurantproject_staff_app.Model.FCMSendData;
import com.project.restaurantproject_staff_app.Model.Status;
import com.project.restaurantproject_staff_app.Retrofit.FCMService;
import com.project.restaurantproject_staff_app.Retrofit.MyRestaurantAPI;
import com.project.restaurantproject_staff_app.Retrofit.RetrofitClient;
import com.project.restaurantproject_staff_app.Retrofit.RetrofitFCMClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class OrderDetailActivity extends AppCompatActivity {

    @BindView(R.id.txt_order_number)
    TextView txt_order_number;
    @BindView(R.id.spinner_status)
    AppCompatSpinner spinner_status;
    @BindView(R.id.recycler_order_detail)
    RecyclerView recycler_order_detail;
    @BindView(R.id.toolbar)
    Toolbar toolbar;


    MyRestaurantAPI myRestaurantAPI ;
    FCMService fcmService ;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog ;


    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.order_detail_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id== android.R.id.home){
            finish();
            return true;
        }else if (id== R.id.action_save){
            updateOrder();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateOrder() {
        int status = Common.convertStringToStatus(spinner_status.getSelectedItem().toString());
        if (status == 1){
            compositeDisposable.add(myRestaurantAPI.updateOrderStatus(Common.API_KEY,Common.currentOrder.getOrderId(),
                    Common.convertStringToStatus(spinner_status.getSelectedItem().toString()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(updateOrderModel -> {
                                Common.currentOrder.setOrderStatus(Common.convertStringToStatus(spinner_status.getSelectedItem().toString()));
                                compositeDisposable.add(myRestaurantAPI.setShippingOrder(Common.API_KEY,
                                        Common.currentOrder.getOrderId(),
                                        Common.currentRestaurantOwner.getRestaurantId()
                                        )
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(shippingOrderModel -> {
                                        if (shippingOrderModel.isSuccess()){
                                            compositeDisposable.add(myRestaurantAPI.getToken(Common.API_KEY,Common.currentOrder.getOrderFBID())
                                                    .observeOn(Schedulers.io())
                                                    .subscribeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(tokenModel -> {
                                                                if (tokenModel.isSuccess()){
                                                                    Map<String,String> messageSend = new HashMap<>();
                                                                    messageSend.put(Common.NOTIF_TITLE,"Your Order HAs Been Updated");
                                                                    messageSend.put(Common.NOTIF_CONTENT,new StringBuilder("Your Order : ")
                                                                            .append(Common.currentOrder.getOrderId())
                                                                            .append(", Has Been Update To : ")
                                                                            .append(Common.convertStatusToString(Common.currentOrder.getOrderStatus())).toString());

                                                                    FCMSendData fcmSendData = new FCMSendData(tokenModel.getResult().get(0).getToken(),messageSend);
                                                                    compositeDisposable.add(fcmService.sendNotification(fcmSendData)
                                                                            .observeOn(Schedulers.io())
                                                                            .subscribeOn(AndroidSchedulers.mainThread())
                                                                            .subscribe(fcmResponse -> {
                                                                                        Toast.makeText(this, "Order Updated !!", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                    ,throwable -> {
                                                                                        Toast.makeText(this, "Order was update but can't send notification", Toast.LENGTH_SHORT).show();
                                                                                    }));
                                                                }
                                                            }
                                                            ,throwable -> {
                                                                Toast.makeText(this, "[GET TOKEN]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                            })
                                            );
                                        }else{
                                            Toast.makeText(this, "[SET Shipper]"+shippingOrderModel.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    },
                                    throwable -> {
                                        Toast.makeText(this, "[SET Shipper]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    })
                                );
                            }
                            ,throwable -> {
                                Toast.makeText(this, "[UPDATE ORDER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            })
            );
        }else{
            compositeDisposable.add(myRestaurantAPI.updateOrderStatus(Common.API_KEY,Common.currentOrder.getOrderId(),
                    Common.convertStringToStatus(spinner_status.getSelectedItem().toString()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(updateOrderModel -> {
                                Common.currentOrder.setOrderStatus(Common.convertStringToStatus(spinner_status.getSelectedItem().toString()));
                                //get Token to Send notif
                                compositeDisposable.add(myRestaurantAPI.getToken(Common.API_KEY,Common.currentOrder.getOrderFBID())
                                        .observeOn(Schedulers.io())
                                        .subscribeOn(AndroidSchedulers.mainThread())
                                        .subscribe(tokenModel -> {
                                                    if (tokenModel.isSuccess()){
                                                        Map<String,String> messageSend = new HashMap<>();
                                                        messageSend.put(Common.NOTIF_TITLE,"Your Order HAs Been Updated");
                                                        messageSend.put(Common.NOTIF_CONTENT,new StringBuilder("Your Order : ")
                                                                .append(Common.currentOrder.getOrderId())
                                                                .append(", Has Been Update To : ")
                                                                .append(Common.convertStatusToString(Common.currentOrder.getOrderStatus())).toString());

                                                        FCMSendData fcmSendData = new FCMSendData(tokenModel.getResult().get(0).getToken(),messageSend);
                                                        compositeDisposable.add(fcmService.sendNotification(fcmSendData)
                                                                .observeOn(Schedulers.io())
                                                                .subscribeOn(AndroidSchedulers.mainThread())
                                                                .subscribe(fcmResponse -> {
                                                                            Toast.makeText(this, "Order Updated !!", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                        ,throwable -> {
                                                                            Toast.makeText(this, "Order was update but can't send notification", Toast.LENGTH_SHORT).show();
                                                                        }));
                                                    }
                                                }
                                                ,throwable -> {
                                                    Toast.makeText(this, "[GET TOKEN]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                })
                                );
                            }
                            ,throwable -> {
                                Toast.makeText(this, "[UPDATE ORDER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            })
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        init();
        initView();
    }

    private void initView() {
        ButterKnife.bind(this);

        toolbar.setTitle(getString(R.string.order_detail));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_order_detail.setLayoutManager(layoutManager);
        recycler_order_detail.addItemDecoration(new DividerItemDecoration(this,layoutManager.getOrientation()));

        txt_order_number.setText(new StringBuilder("Order Number :#").append(Common.currentOrder.getOrderId()));

        initStatusSpinner();

        loadOrderDetail();
    }

    private void loadOrderDetail() {
        dialog.show();
        compositeDisposable.add(myRestaurantAPI.getOrderDetail(Common.API_KEY,Common.currentOrder.getOrderId())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(orderDetailModel -> {
            if (orderDetailModel.isSuccess()){
                if (orderDetailModel.getResult().size() > 0){
                    MyOrderDetailAdapter adapter = new MyOrderDetailAdapter(this,orderDetailModel.getResult());
                    recycler_order_detail.setAdapter(adapter);
                }
            }
            dialog.dismiss();
        }
        ,throwable -> {
            dialog.dismiss();
            Toast.makeText(this, "[GET ORDER DETAIL]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }
        ));
    }

    private void initStatusSpinner() {
        List<Status> statusList = new ArrayList<Status>();

        statusList.add(new Status(0,"Placed")); // index 0
        statusList.add(new Status(1,"Shipping")); // index 1
        //statusList.add(new Status(2,"Shipped")); // index 2
        statusList.add(new Status(-1,"Cancelled")); // index 3

        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,statusList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_status.setAdapter(adapter);
        spinner_status.setSelection(Common.convertStatusToIndex(Common.currentOrder.getOrderStatus()));
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(MyRestaurantAPI.class);
        fcmService = RetrofitFCMClient.getInstance().create(FCMService.class);
    }
}
