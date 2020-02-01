package com.project.restaurantproject_staff_app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.project.restaurantproject_staff_app.Adapter.MyOrderAdapter;
import com.project.restaurantproject_staff_app.Common.Common;
import com.project.restaurantproject_staff_app.Interface.ILoadMore;
import com.project.restaurantproject_staff_app.Model.Order;
import com.project.restaurantproject_staff_app.Retrofit.MyRestaurantAPI;
import com.project.restaurantproject_staff_app.Retrofit.RetrofitClient;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ILoadMore {

    @BindView(R.id.recycler_order)
    RecyclerView recycler_order;

    MyRestaurantAPI myRestaurantAPI ;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog ;

    LayoutAnimationController layoutAnimationController ;

    int maxData = 0;
    MyOrderAdapter adapter ;
    List<Order> orderList;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();
        initView();

        subscribeToTopic(Common.getTopicChannel(Common.currentRestaurantOwner.getRestaurantId()));

        getMaxOrder();
    }

    private void subscribeToTopic(String topicChannel) {
        FirebaseMessaging.getInstance()
                .subscribeToTopic(topicChannel)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Subscribe Failed ! You May not receive new order notification.", Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        //this is an auxiliary
                        Toast.makeText(this, "Subscribe success !!", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(this, "FAILED : "+task.isSuccessful(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void getMaxOrder() {
        dialog.show();
        compositeDisposable.add(myRestaurantAPI.getMaxOrder(Common.API_KEY,String.valueOf(Common.currentRestaurantOwner.getRestaurantId()))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(maxOrderModel -> {
            if (maxOrderModel.isSuccess()){
                maxData = maxOrderModel.getResult().get(0).getMaxRowNum();
                dialog.dismiss();
                getAllOrder(0,10,false);
            }
        }
        ,throwable -> {
                    dialog.dismiss();
                    Toast.makeText(this, "[GET MAX ORDER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
        })
        );
    }

    private void getAllOrder(int from, int to, boolean isRefresh) {
        dialog.show();
        compositeDisposable.add(myRestaurantAPI.getOrder(Common.API_KEY,String.valueOf(Common.currentRestaurantOwner.getRestaurantId()),from,to)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(orderModel -> {
                            if (orderModel.isSuccess()){
                                if (orderModel.getResult().size() > 0){
                                    if (adapter == null){
                                        orderList = new ArrayList<>();
                                        orderList = orderModel.getResult();
                                        adapter = new MyOrderAdapter(this,orderList,recycler_order);
                                        adapter.setLoadMore(this);

                                        recycler_order.setAdapter(adapter);
                                        recycler_order.setLayoutAnimation(layoutAnimationController);
                                    }else{
                                        if (!isRefresh){
                                            orderList.remove(orderList.size()-1);
                                            orderList = orderModel.getResult();
                                            adapter.addItem(orderList);
                                        }else{
                                            orderList = new ArrayList<>();
                                            orderList = orderModel.getResult();
                                            adapter = new MyOrderAdapter(this,orderList,recycler_order);
                                            adapter.setLoadMore(this);

                                            recycler_order.setAdapter(adapter);
                                            recycler_order.setLayoutAnimation(layoutAnimationController);
                                        }
                                    }
                                }
                                dialog.dismiss();
                            }
                        },
                        throwable -> {
                            dialog.dismiss();
                            Toast.makeText(this, "[GET ORDER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        })
        );
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(MyRestaurantAPI.class);
    }

    private void initView() {

        ButterKnife.bind(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recycler_order.setLayoutManager(linearLayoutManager);
        recycler_order.addItemDecoration(new DividerItemDecoration(this,linearLayoutManager.getOrientation()));
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(this,R.anim.layout_item_from_left);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Restaurant Order's");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh){
            getAllOrder(0,10,true);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoadMore() {
        if (adapter.getItemCount() < maxData){
            orderList.add(null); // add null to show loading progress
            adapter.notifyItemInserted(orderList.size()-1);

            getAllOrder(adapter.getItemCount()+1,adapter.getItemCount()+10,false); // get the next 10 items

            adapter.notifyDataSetChanged();
            adapter.setLoaded();
        }else{
            Toast.makeText(this, "Max Data To Load", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_hot_food){
            startActivity(new Intent(HomeActivity.this,HotFoodActivity.class));
        }
        return false;
    }
}
