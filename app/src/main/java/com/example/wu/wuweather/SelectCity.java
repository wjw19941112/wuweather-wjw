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
import android.widget.EditText;
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
    private EditText searchEt; //定义输入框
    private ImageView searchBtn;//搜索按钮

    //将ListView内容加载为我们从数据库文件读到的城市列表
    private List<City> mCityList;

    private MyApplication myApplication;
    private ArrayList<String> mArrayList;
    private List<String> mSearchList;
    private List<String> updateCityList;

    private String updateCityCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_city);

        //为返回按钮绑定组件并监听返回按钮的动作
        mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        //为搜索栏绑定组件并监听搜索按钮的动作
        searchEt = (EditText)findViewById(R.id.selectcity_search);
        searchBtn = (ImageView)findViewById(R.id.selectcity_search_button);
        searchBtn.setOnClickListener(this);

        //将定义的cityListLv与组件绑定，并加载ListView内容
        myApplication = (MyApplication)getApplication();
        mCityList = myApplication.getCityList();


        mArrayList = new ArrayList<String>();  //不new会指向空
        for(int i = 0;i<mCityList.size();i++){
            String No_ = Integer.toString(i+1);
            String number = mCityList.get(i).getNumber();
            String provinceName = mCityList.get(i).getProvince();
            String cityName = mCityList.get(i).getCity();
            mArrayList.add("No."+No_+":"+number+"-"+provinceName+"-"+cityName);
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

                //用SharePreferences存储最近一次的cityCode
                SharedPreferences sharedPreferences = getSharedPreferences("CityCodePreference",Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("cityCode",updateCityCode);
                editor.commit();
                Intent intent = new Intent();
                intent.putExtra("cityCode",updateCityCode);
                setResult(RESULT_OK,intent);
                finish();

            }
        };
        //为组件绑定监听
        cityListLv.setOnItemClickListener(itemClickListener);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.selectcity_search_button:
                final String province = searchEt.getText().toString();
                String city = searchEt.getText().toString();
                Log.d("search",city);
                mSearchList = new ArrayList<String>();
                updateCityList = new ArrayList<String>();
                for(int i = 0;i<mCityList.size();i++){
                    final String No_ = Integer.toString(i+1);
                    final String number = mCityList.get(i).getNumber();
                    final String provinceName = mCityList.get(i).getProvince();
                    final String cityName = mCityList.get(i).getCity();
                    if (cityName.equals(city) || provinceName.equals(province)){
                        mSearchList.add("No."+No_+":"+number+"-"+provinceName+"-"+cityName);
                        updateCityList.add(number);
                        Log.d("changed adapter data","No."+No_+":"+number+"-"+provinceName+"-"+cityName);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(SelectCity.this,android.R.layout.simple_list_item_1,mSearchList);
                    cityListLv.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    //添加ListView项的点击事件的动作

                    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener(){
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            updateCityCode = updateCityList.get(position);
                            Log.d("update city code",updateCityCode);

                            //用SharePreferences存储最近一次的cityCode
                            SharedPreferences sharedPreferences = getSharedPreferences("CityCodePreference",Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("cityCode",updateCityCode);
                            editor.commit();
                            Intent intent = new Intent();
                            intent.putExtra("cityCode",updateCityCode);
                            setResult(RESULT_OK,intent);
                            finish();

                        }
                    };
                    //为组件绑定监听
                    cityListLv.setOnItemClickListener(itemClickListener);

                }
                break;


            case R.id.title_back:
                SharedPreferences sharedPreferences=getSharedPreferences("CityCodePreference",MODE_PRIVATE);
                String cityCode=sharedPreferences.getString("cityCode","");
                Intent intent = new Intent();
                intent.putExtra("cityCode",cityCode);
                setResult(RESULT_OK,intent);
                finish();
                break;
            default:
                break;
        }

    }
}
