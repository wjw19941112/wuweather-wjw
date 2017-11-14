package com.example.wu.wuweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.wu.app.MyApplication;
import com.example.wu.bean.City;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wu on 2017/10/18.
 */

public class SelectCity extends AppCompatActivity implements View.OnClickListener{

    private ImageView mBackBtn;   //返回按钮
    private ListView cityListLv;//定义ListView变量

    //将ListView内容加载为我们从数据库文件读到的城市列表
    private List<City> mCityList;
    private MyApplication myApplication;
    private ArrayList<String> mArrayList;

    private String updateCityCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_city);

        mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        //将定义的cityListLv与组件绑定，并加载ListView内容
        myApplication = (MyApplication)getApplication();
        mCityList = myApplication.getCityList();
        mArrayList = new ArrayList<String>();  //不new会指向空

        for(int i = 0;i<mCityList.size();i++){
            String cityName = mCityList.get(i).getCity();
            mArrayList.add(cityName);
        }
        cityListLv = (ListView)findViewById(R.id.selectcity_lv);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SelectCity.this,android.R.layout.simple_list_item_1,mArrayList);
        cityListLv.setAdapter(adapter);

        //添加ListView项的点击事件的动作
        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateCityCode = mCityList.get(position).getNumber();
                Log.d("update city code",updateCityCode);

                //用sharepreferences存储最近一次的cityCode
                SharedPreferences sharedPreferences = getSharedPreferences("CityCodePreference",Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("cityCode",updateCityCode);
                editor.commit();

                //Intent intent = new Intent();
               // intent.putExtra("cityCode",updateCityCode);
               // setResult(RESULT_OK,intent);
            }
        };
        //为组件绑定监听
        cityListLv.setOnItemClickListener(itemClickListener);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.title_back:
                Intent intent = new Intent();
                intent.putExtra("cityCode",updateCityCode);
                setResult(RESULT_OK,intent);
                //startActivity(intent);
                finish();
                break;
            default:
                break;
        }

    }
}
