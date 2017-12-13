package com.example.wu.wuweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wu.bean.TodayWeather;
import com.example.wu.util.NetUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by wu on 2017/9/26.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    TodayWeather todayWeather = null;

    private static final int UPDATE_TODAY_WEATHER=1;

    private ImageView mUpdateBtn;

    private ImageView mCitySelect;


    //未来4天
    private ViewPageAdapter vpAdapterWeather;
    private ViewPager vpWeather;
    private List<View> views;

    private ImageView[] imageViews;
    private int[] ids = {R.id.iv4,R.id.iv5};

    //定义相关控件
    private TextView cityTv,timeTv,wenduTv,humidityTv,weekTv,pmDataTv,pmQualityTv,temperatureTv,
                     climateTv,windTv,city_name_Tv,fengxiangTv;
    private ImageView weatherImg,pm25Img;

    private TextView weekTv1,temperatureTv1,climateTv1,windTv1,fengxiangTv1;
    private ImageView weatherImg1;
    private TextView weekTv2,temperatureTv2,climateTv2,windTv2,fengxiangTv2;
    private ImageView weatherImg2;
    private TextView weekTv3,temperatureTv3,climateTv3,windTv3,fengxiangTv3;
    private ImageView weatherImg3;
    private TextView weekTv4,temperatureTv4,climateTv4,windTv4,fengxiangTv4;
    private ImageView weatherImg4;



    //主线程接收到消息数据后，调用updateTodayWeather函数，更新UI界面上的数据。
    private Handler mHandler=new Handler(){
        public void handleMessage(android.os.Message msg){
            switch (msg.what){
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather)msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);


        //为更新按钮增加单击事件
        mUpdateBtn=(ImageView)findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);


        //检查网络连接状态
        if(NetUtil.getNetworkState(this)!=NetUtil.NETWORN_NONE){
            Log.d("myWeather","网络OK");
            Toast.makeText(MainActivity.this,"网络OK!",Toast.LENGTH_LONG).show();
        }else
        {
            Log.d("myWeather","网络挂了");
            Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_SHORT).show();
        }

        //为选择城市添加单击事件
        mCitySelect = (ImageView)findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);


        initViews1();
        initDots1();
        initView();
    }

    void initDots1(){
        imageViews = new ImageView[views.size()];
        for (int i=0;i<views.size();i++){
            imageViews[i] = (ImageView) findViewById(ids[i]);
        }
    }


    //设置点击更新按钮和城市列表按钮
    @Override
    public void onClick(View view){


        //点击城市管理按钮的响应
        if (view.getId() == R.id.title_city_manager){
            Intent i = new Intent(this,SelectCity.class);
                       startActivityForResult(i,1);
        }

        //点击刷新按钮，从Sharedreferences读取城市id
        if (view.getId()==R.id.title_update_btn){
            SharedPreferences sharedPreferences=getSharedPreferences("CityCodePreference",MODE_PRIVATE);
            String cityCode=sharedPreferences.getString("cityCode","");

            Log.d("myWeather",cityCode);

            if (NetUtil.getNetworkState(this)!=NetUtil.NETWORN_NONE){
                Log.d("myWeather","网络OK");
                Toast.makeText(MainActivity.this,"网络OK！",Toast.LENGTH_LONG).show();
                queryWeatherCode(cityCode);

            }else {
                Log.d("myWeather","网络挂了");
                Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initViews1(){
        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        views.add(inflater.inflate(R.layout.page4,null));
        views.add(inflater.inflate(R.layout.page5,null));
        vpAdapterWeather = new ViewPageAdapter(views,this);
        vpWeather = (ViewPager)findViewById(R.id.viewpager2);
        vpWeather.setAdapter(vpAdapterWeather);
        vpWeather.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i=0;i<ids.length;i++){
                    if (i == position){
                        imageViews[i].setImageResource(R.drawable.page_indicator_focused);
                    }else {
                        imageViews[i].setImageResource(R.drawable.page_indicator_unfocused);
                    }
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });




    }


     //接收选择城市界面传来的cityode
    protected void onActivityResult(int requestCode,int resultCode,Intent data){

        if (requestCode == 1 && resultCode == RESULT_OK){
            String newCityCode = data.getStringExtra("cityCode");
            Log.d("myWeather","选择的城市代码为"+newCityCode);

           if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
                Log.d("myWeather","网络OK");
                queryWeatherCode(newCityCode);
            }else {
                Log.d("myWeather","网络挂了");
                Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
            }
        }
    }


    //初始化控件内容
    void initView(){
        city_name_Tv=(TextView)findViewById(R.id.title_city_name);
        cityTv=(TextView)findViewById(R.id.city);
        timeTv=(TextView)findViewById(R.id.time);
        humidityTv=(TextView)findViewById(R.id.humidity);
        weekTv=(TextView)findViewById(R.id.week_today);
        pmDataTv=(TextView)findViewById(R.id.pm_data);
        pmQualityTv=(TextView)findViewById(R.id.pm2_5_quality);
        pm25Img=(ImageView)findViewById(R.id.pm2_5_img);
        temperatureTv=(TextView)findViewById(R.id.temperature);
        climateTv=(TextView)findViewById(R.id.climate);
        windTv=(TextView)findViewById(R.id.wind);
        weatherImg=(ImageView)findViewById(R.id.weather_img);
        wenduTv=(TextView)findViewById(R.id.wendu);
        fengxiangTv=(TextView)findViewById(R.id.fengxiang);

        weekTv1=views.get(0).findViewById(R.id.week_today1);
        temperatureTv1=views.get(0).findViewById(R.id.temperature1);
        climateTv1=views.get(0).findViewById(R.id.climate1);
        windTv1=views.get(0).findViewById(R.id.wind1);
        weatherImg1=views.get(0).findViewById(R.id.weather_img1);
        fengxiangTv1=views.get(0).findViewById(R.id.fengxiang1);

        weekTv2=views.get(0).findViewById(R.id.week_today2);
        temperatureTv2=views.get(0).findViewById(R.id.temperature2);
        climateTv2=views.get(0).findViewById(R.id.climate2);
        windTv2=views.get(0).findViewById(R.id.wind2);
        weatherImg2=views.get(0).findViewById(R.id.weather_img2);
        fengxiangTv2=views.get(0).findViewById(R.id.fengxiang2);

        weekTv3=views.get(1).findViewById(R.id.week_today3);
        temperatureTv3=views.get(1).findViewById(R.id.temperature3);
        climateTv3=views.get(1).findViewById(R.id.climate3);
        windTv3=views.get(1).findViewById(R.id.wind3);
        weatherImg3=views.get(1).findViewById(R.id.weather_img3);
        fengxiangTv3=views.get(1).findViewById(R.id.fengxiang3);

        weekTv4=views.get(1).findViewById(R.id.week_today4);
        temperatureTv4=views.get(1).findViewById(R.id.temperature4);
        climateTv4=views.get(1).findViewById(R.id.climate4);
        windTv4=views.get(1).findViewById(R.id.wind4);
        weatherImg4=views.get(1).findViewById(R.id.weather_img4);
        fengxiangTv4=views.get(1).findViewById(R.id.fengxiang4);




        SharedPreferences sharedPreferences=getSharedPreferences("CityCodePreference",MODE_PRIVATE);
        String cityCode=sharedPreferences.getString("cityCode","");
        if (NetUtil.getNetworkState(this)!=NetUtil.NETWORN_NONE){
            Log.d("myWeather","网络OK");
            Toast.makeText(MainActivity.this,"网络OK！",Toast.LENGTH_LONG).show();
            queryWeatherCode(cityCode);

        }else {
            Log.d("myWeather","网络挂了");
            Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
        }



//        city_name_Tv.setText("N/A");
//        cityTv.setText("N/A");
//        timeTv.setText("N/A");
//        humidityTv.setText("N/A");
//        weekTv.setText("N/A");
//        pmDataTv.setText("N/A");
//        pmQualityTv.setText("N/A");
//        temperatureTv.setText("N/A");
//        climateTv.setText("N/A");
//        windTv.setText("N/A");
//        wenduTv.setText("N/A");
//        fengxiangTv.setText("N/A");

    }

    //借助HttpUrlConnection获取Url网页上的数据
    /**
    *
    *@param cityCode
    */
    private void queryWeatherCode(String cityCode){
        final String address="http://wthrcdn.etouch.cn/WeatherApi?citykey="+cityCode;
        Log.d("myWeather",address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con=null;
                TodayWeather todayWeather= null;
                try {
                    //建立连接
                    URL url=new URL(address);
                    con = (HttpURLConnection)url.openConnection();
                    //设置连接属性
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    //读取数据
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str=reader.readLine()) != null){
                        response.append(str);
                        Log.d("myWeather",str);
                    }
                    String responseStr = response.toString();
                    Log.d("myWeather",responseStr);
                    //对数据进行解析
                    todayWeather=parseXML(responseStr);
                    if (todayWeather != null){
                        Log.d("myWeather",todayWeather.toString());

                        //通过消息机制，将解析的天气对象发给主线程
                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = todayWeather;
                        mHandler.sendMessage(msg);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if (con!=null){
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

    //编写解析函数
    private TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather=null;
        int fengxiangCount=0;
        int fengliCount=0;
        int dateCount=0;
        int highCount=0;
        int lowCount=0;
        int typeCount=0;
        try {
            //创建解析工厂
            XmlPullParserFactory fac=XmlPullParserFactory.newInstance();
            //从工厂获得解析器
            XmlPullParser xmlPullParser=fac.newPullParser();
            //指定要加工的原材料--xml文件
            xmlPullParser.setInput(new StringReader(xmldata));
            //获得事件类型
            int eventType=xmlPullParser.getEventType();
            Log.d("myWeather","parseXML");
            while (eventType!=XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    //判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    //判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("resp")){
                            todayWeather=new TodayWeather();
                        }
                        if (todayWeather!=null){
                            if (xmlPullParser.getName().equals("city")){
                                eventType=xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            }else if (xmlPullParser.getName().equals("updatetime")){
                                eventType=xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            }else if (xmlPullParser.getName().equals("shidu")){
                                eventType=xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            }else if (xmlPullParser.getName().equals("wendu")){
                                eventType=xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            }else if (xmlPullParser.getName().equals("pm25")){
                                eventType=xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            }else if (xmlPullParser.getName().equals("quality")){
                                eventType=xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            }else if (xmlPullParser.getName().equals("fengxiang")&& fengxiangCount==0){
                                eventType=xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            }else if (xmlPullParser.getName().equals("fengli")&& fengliCount==0){
                                eventType=xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;
                            }else if (xmlPullParser.getName().equals("date")&& dateCount==0){
                                eventType=xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            }else if (xmlPullParser.getName().equals("high")&& highCount==0){
                                eventType=xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }else if (xmlPullParser.getName().equals("low")&& lowCount==0){
                                eventType=xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }else if (xmlPullParser.getName().equals("type")&& typeCount==0){
                                eventType=xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("fengxiang")&& fengxiangCount==1){
                                eventType=xmlPullParser.next();
                                todayWeather.setFengxiang1(xmlPullParser.getText());
                                fengxiangCount++;
                            }else if (xmlPullParser.getName().equals("fengli")&& fengliCount==1){
                                eventType=xmlPullParser.next();
                                todayWeather.setFengli1(xmlPullParser.getText());
                                fengliCount++;
                            }else if (xmlPullParser.getName().equals("date")&& dateCount==1){
                                eventType=xmlPullParser.next();
                                todayWeather.setDate1(xmlPullParser.getText());
                                dateCount++;
                            }else if (xmlPullParser.getName().equals("high")&& highCount==1){
                                eventType=xmlPullParser.next();
                                todayWeather.setHigh1(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }else if (xmlPullParser.getName().equals("low")&& lowCount==1){
                                eventType=xmlPullParser.next();
                                todayWeather.setLow1(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }else if (xmlPullParser.getName().equals("type")&& typeCount==1){
                                eventType=xmlPullParser.next();
                                todayWeather.setType1(xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("fengxiang")&& fengxiangCount==2){
                                eventType=xmlPullParser.next();
                                todayWeather.setFengxiang2(xmlPullParser.getText());
                                fengxiangCount++;
                            }else if (xmlPullParser.getName().equals("fengli")&& fengliCount==2){
                                eventType=xmlPullParser.next();
                                todayWeather.setFengli2(xmlPullParser.getText());
                                fengliCount++;
                            }else if (xmlPullParser.getName().equals("date")&& dateCount==2){
                                eventType=xmlPullParser.next();
                                todayWeather.setDate2(xmlPullParser.getText());
                                dateCount++;
                            }else if (xmlPullParser.getName().equals("high")&& highCount==2){
                                eventType=xmlPullParser.next();
                                todayWeather.setHigh2(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }else if (xmlPullParser.getName().equals("low")&& lowCount==2){
                                eventType=xmlPullParser.next();
                                todayWeather.setLow2(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }else if (xmlPullParser.getName().equals("type")&& typeCount==2){
                                eventType=xmlPullParser.next();
                                todayWeather.setType2(xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("fengxiang")&& fengxiangCount==3){
                                eventType=xmlPullParser.next();
                                todayWeather.setFengxiang3(xmlPullParser.getText());
                                fengxiangCount++;
                            }else if (xmlPullParser.getName().equals("fengli")&& fengliCount==3){
                                eventType=xmlPullParser.next();
                                todayWeather.setFengli3(xmlPullParser.getText());
                                fengliCount++;
                            }else if (xmlPullParser.getName().equals("date")&& dateCount==3){
                                eventType=xmlPullParser.next();
                                todayWeather.setDate3(xmlPullParser.getText());
                                dateCount++;
                            }else if (xmlPullParser.getName().equals("high")&& highCount==3){
                                eventType=xmlPullParser.next();
                                todayWeather.setHigh3(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }else if (xmlPullParser.getName().equals("low")&& lowCount==3){
                                eventType=xmlPullParser.next();
                                todayWeather.setLow3(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }else if (xmlPullParser.getName().equals("type")&& typeCount==3){
                                eventType=xmlPullParser.next();
                                todayWeather.setType3(xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("fengxiang")&& fengxiangCount==4){
                                eventType=xmlPullParser.next();
                                todayWeather.setFengxiang4(xmlPullParser.getText());
                                fengxiangCount++;
                            }else if (xmlPullParser.getName().equals("fengli")&& fengliCount==4){
                                eventType=xmlPullParser.next();
                                todayWeather.setFengli4(xmlPullParser.getText());
                                fengliCount++;
                            }else if (xmlPullParser.getName().equals("date")&& dateCount==4){
                                eventType=xmlPullParser.next();
                                todayWeather.setDate4(xmlPullParser.getText());
                                dateCount++;
                            }else if (xmlPullParser.getName().equals("high")&& highCount==4){
                                eventType=xmlPullParser.next();
                                todayWeather.setHigh4(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }else if (xmlPullParser.getName().equals("low")&& lowCount==4){
                                eventType=xmlPullParser.next();
                                todayWeather.setLow4(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }else if (xmlPullParser.getName().equals("type")&& typeCount==4){
                                eventType=xmlPullParser.next();
                                todayWeather.setType4(xmlPullParser.getText());
                                typeCount++;
                            }

                        }
                        break;
                    //判断当前事件是否为标签元素结束事件
                    case XmlPullParser.END_TAG:
                        break;
                }
                //进入下一个元素并触发相应事件
                eventType=xmlPullParser.next();
            }
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return todayWeather;
    }

    //更新ui控件
    void updateTodayWeather(TodayWeather todayWeather){
        city_name_Tv.setText(todayWeather.getCity()+"天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+"发布");
        humidityTv.setText("湿度："+todayWeather.getShidu());
        wenduTv.setText("温度："+todayWeather.getWendu()+"℃");
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh()+"~"+todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        fengxiangTv.setText(todayWeather.getFengxiang());
        windTv.setText(todayWeather.getFengli());

        weekTv1.setText(todayWeather.getDate1());
        temperatureTv1.setText(todayWeather.getHigh1()+"~"+todayWeather.getLow1());
        climateTv1.setText(todayWeather.getType1());
        fengxiangTv1.setText(todayWeather.getFengxiang1());
        windTv1.setText(todayWeather.getFengli1());

        weekTv2.setText(todayWeather.getDate2());
        temperatureTv2.setText(todayWeather.getHigh2()+"~"+todayWeather.getLow2());
        climateTv2.setText(todayWeather.getType2());
        fengxiangTv2.setText(todayWeather.getFengxiang2());
        windTv2.setText(todayWeather.getFengli2());

        weekTv3.setText(todayWeather.getDate3());
        temperatureTv3.setText(todayWeather.getHigh3()+"~"+todayWeather.getLow3());
        climateTv3.setText(todayWeather.getType3());
        fengxiangTv3.setText(todayWeather.getFengxiang3());
        windTv3.setText(todayWeather.getFengli3());

        weekTv4.setText(todayWeather.getDate4());
        temperatureTv4.setText(todayWeather.getHigh4()+"~"+todayWeather.getLow4());
        climateTv4.setText(todayWeather.getType4());
        fengxiangTv4.setText(todayWeather.getFengxiang4());
        windTv4.setText(todayWeather.getFengli4());


        if(todayWeather.getPm25()!=null) {
            int pm25 = Integer.parseInt(todayWeather.getPm25());
            if (pm25 <= 50) {
                pm25Img.setImageResource(R.drawable.biz_plugin_weather_0_50);
            } else if (pm25 >= 51 && pm25 <= 100) {
                pm25Img.setImageResource(R.drawable.biz_plugin_weather_51_100);
            } else if (pm25 >= 101 && pm25 <= 150) {
                pm25Img.setImageResource(R.drawable.biz_plugin_weather_101_150);
            } else if (pm25 >= 151 && pm25 <= 200) {
                pm25Img.setImageResource(R.drawable.biz_plugin_weather_151_200);
            } else if (pm25 >= 201 && pm25 <= 300) {
                pm25Img.setImageResource(R.drawable.biz_plugin_weather_201_300);
            }
        }else{
            pmQualityTv.setText("无信息");
        }
        if(todayWeather.getType()!=null) {
            Log.d("type", todayWeather.getType());
            switch (todayWeather.getType()) {
                case "晴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
                    break;
                case "阴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_yin);
                    break;
                case "雾":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_wu);
                    break;
                case "多云":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                    break;
                case "小雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                    break;
                case "中雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                    break;
                case "大雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_dayu);
                    break;
                case "阵雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                    break;
                case "雷阵雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                    break;
                case "雷阵雨加暴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                    break;
                case "暴雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                    break;
                case "大暴雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                    break;
                case "特大暴雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                    break;
                case "阵雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                    break;
                case "暴雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                    break;
                case "大雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_daxue);
                    break;
                case "小雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                    break;
                case "雨夹雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                    break;
                case "中雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                    break;
                case "沙尘暴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                    break;
                default:
                    break;
            }
        }
        if(todayWeather.getType1()!=null) {
            Log.d("type", todayWeather.getType1());
            switch (todayWeather.getType1()) {
                case "晴":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_qing);
                    break;
                case "阴":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_yin);
                    break;
                case "雾":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_wu);
                    break;
                case "多云":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                    break;
                case "小雨":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                    break;
                case "中雨":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                    break;
                case "大雨":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_dayu);
                    break;
                case "阵雨":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                    break;
                case "雷阵雨":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                    break;
                case "雷阵雨加暴":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                    break;
                case "暴雨":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                    break;
                case "大暴雨":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                    break;
                case "特大暴雨":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                    break;
                case "阵雪":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                    break;
                case "暴雪":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                    break;
                case "大雪":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_daxue);
                    break;
                case "小雪":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                    break;
                case "雨夹雪":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                    break;
                case "中雪":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                    break;
                case "沙尘暴":
                    weatherImg1.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                    break;
                default:
                    break;
            }
        }
        if(todayWeather.getType2()!=null) {
            Log.d("type", todayWeather.getType2());
            switch (todayWeather.getType2()) {
                case "晴":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_qing);
                    break;
                case "阴":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_yin);
                    break;
                case "雾":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_wu);
                    break;
                case "多云":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                    break;
                case "小雨":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                    break;
                case "中雨":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                    break;
                case "大雨":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_dayu);
                    break;
                case "阵雨":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                    break;
                case "雷阵雨":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                    break;
                case "雷阵雨加暴":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                    break;
                case "暴雨":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                    break;
                case "大暴雨":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                    break;
                case "特大暴雨":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                    break;
                case "阵雪":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                    break;
                case "暴雪":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                    break;
                case "大雪":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_daxue);
                    break;
                case "小雪":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                    break;
                case "雨夹雪":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                    break;
                case "中雪":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                    break;
                case "沙尘暴":
                    weatherImg2.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                    break;
                default:
                    break;
            }
        }
        if(todayWeather.getType3()!=null) {
            Log.d("type", todayWeather.getType3());
            switch (todayWeather.getType3()) {
                case "晴":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_qing);
                    break;
                case "阴":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_yin);
                    break;
                case "雾":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_wu);
                    break;
                case "多云":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                    break;
                case "小雨":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                    break;
                case "中雨":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                    break;
                case "大雨":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_dayu);
                    break;
                case "阵雨":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                    break;
                case "雷阵雨":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                    break;
                case "雷阵雨加暴":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                    break;
                case "暴雨":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                    break;
                case "大暴雨":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                    break;
                case "特大暴雨":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                    break;
                case "阵雪":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                    break;
                case "暴雪":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                    break;
                case "大雪":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_daxue);
                    break;
                case "小雪":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                    break;
                case "雨夹雪":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                    break;
                case "中雪":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                    break;
                case "沙尘暴":
                    weatherImg3.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                    break;
                default:
                    break;
            }
        }
        if(todayWeather.getType4()!=null) {
            Log.d("type", todayWeather.getType4());
            switch (todayWeather.getType4()) {
                case "晴":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_qing);
                    break;
                case "阴":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_yin);
                    break;
                case "雾":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_wu);
                    break;
                case "多云":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                    break;
                case "小雨":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                    break;
                case "中雨":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                    break;
                case "大雨":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_dayu);
                    break;
                case "阵雨":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                    break;
                case "雷阵雨":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                    break;
                case "雷阵雨加暴":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                    break;
                case "暴雨":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                    break;
                case "大暴雨":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                    break;
                case "特大暴雨":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                    break;
                case "阵雪":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                    break;
                case "暴雪":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                    break;
                case "大雪":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_daxue);
                    break;
                case "小雪":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                    break;
                case "雨夹雪":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                    break;
                case "中雪":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                    break;
                case "沙尘暴":
                    weatherImg4.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                    break;
                default:
                    break;
            }
        }

        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();

    }

}