package com.project.restaurantproject_staff_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.project.restaurantproject_staff_app.Common.Common;
import com.project.restaurantproject_staff_app.Model.HotFood;
import com.project.restaurantproject_staff_app.Retrofit.MyRestaurantAPI;
import com.project.restaurantproject_staff_app.Retrofit.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class HotFoodActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.piechart)
    PieChart pieChart ;

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    MyRestaurantAPI myRestaurantAPI ;
    List<PieEntry> entryList;

    @Override
    protected void onStop(){
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_food);

        init();
        initView();

        LoadChart();

    }

    private void LoadChart() {
        entryList = new ArrayList<>();
        compositeDisposable.add(myRestaurantAPI.getHotFood(Common.API_KEY)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(hotFoodModel -> {
            if (hotFoodModel.isSuccess()){
                int i=0;
                for (HotFood hotFood : hotFoodModel.getResult()){
                    entryList.add(new PieEntry(Float.parseFloat(String.valueOf(hotFood.getPercent())),hotFood.getName()));
                    i++;
                }
                PieDataSet dataSet = new PieDataSet(entryList,"Hotest Food");

                PieData data = new PieData();
                data.setDataSet(dataSet);
                data.setValueTextSize(14f);
                data.setValueFormatter( new PercentFormatter(pieChart));

                dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

                pieChart.setData(data);
                pieChart.animateXY(2000,2000);
                pieChart.setUsePercentValues(true);
                pieChart.getDescription().setEnabled(false);

                pieChart.invalidate();

            }else{
                Toast.makeText(this, ""+hotFoodModel.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        ,throwable -> {
                    Toast.makeText(this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
        })
        );
    }

    private void initView() {
        ButterKnife.bind(this);

        toolbar.setTitle("HOT FOOD");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void init() {
        myRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(MyRestaurantAPI.class);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            finish();
            return false;
        }
        return super.onOptionsItemSelected(item);
    }
}
