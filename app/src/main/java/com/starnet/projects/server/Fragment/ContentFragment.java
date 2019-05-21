package com.starnet.projects.server.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.starnet.projects.server.Activity.MainActivity;
import com.starnet.projects.server.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link ContentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContentFragment extends Fragment {
    private LocationClient mLocationClient = null;
    private MsgReceiver msgReceiver;
    private TextView contentTextView;
    private TextView timeTextView;
    private TextView dateTextView;
    private TextView weekTextView;
    private TextView addressTextView;
    private TextView ariConditionTextView;
    private TextView temperatureTextView;
    private View view;
    private String airCondition = null;
    private String temprature = null;

    private Handler handler = new Handler();
    public class MsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("RequestContent")){
                MainActivity mainActivity = (MainActivity)getActivity();
                mainActivity.getConnectService().sendMessage("110:"+contentToString());
            }
        }
    }

    public ContentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ContentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContentFragment newInstance() {
        ContentFragment fragment = new ContentFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyLocationListenner myListener = new MyLocationListenner();
        mLocationClient = new LocationClient(getContext());
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setAddrType("all");
        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener(myListener);
        mLocationClient.start();
        sendHttpRequest();
        msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("RequestContent");
        getActivity().registerReceiver(msgReceiver, intentFilter);
    }

    private class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            try {
                //当前设备位置所在的市
                String city = location.getCity();
                //当前设备位置所在的县
                String district = location.getDistrict();
                addressTextView.setText(city+district);
                contentTextView.setText("星网锐捷"+location.getLocType()+location.getLocTypeDescription());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendHttpRequest(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    //用HttpClient发送请求，分为五步
                    //第一步：创建HttpClient对象
                    HttpClient httpCient = new DefaultHttpClient();
                    //第二步：创建代表请求的对象,参数是访问的服务器地址
                    HttpGet httpGet = new HttpGet("https://www.tianqiapi.com/api/?version=v6");
                    //修改org.apache.http的主机名验证解决问题
                    SSLSocketFactory.getSocketFactory().setHostnameVerifier(new AllowAllHostnameVerifier());
                    //第三步：执行请求，获取服务器发还的相应对象
                    HttpResponse httpResponse = httpCient.execute(httpGet);
                    //第四步：检查相应的状态是否正常：检查状态码的值是200表示正常
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        JSONObject jsonObject = GetHttpEntity(httpResponse);
                        airCondition = jsonObject.getString("wea")+" "+jsonObject.getString("air")+jsonObject.getString("air_level");
                        temprature = jsonObject.getString("tem")+"℃";
                        handler.post(runnable);
                    }
                }catch (Exception e) {
                e.printStackTrace();
            }}}).start();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            ariConditionTextView.setText(airCondition);
            temperatureTextView.setText(temprature);
        }
    };

    private static JSONObject GetHttpEntity(HttpResponse response) {
        String line=null;
        JSONObject resultJsonObject=null;
        StringBuilder entityStringBuilder=new StringBuilder();
        try {
            BufferedReader b = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"),8*1024);
            while ((line=b.readLine())!=null) {
                entityStringBuilder.append(line+"/n");
            }
            resultJsonObject = new JSONObject(entityStringBuilder.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultJsonObject;
    }

    @Override
    public void onResume(){
        super.onResume();
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        getActivity().unregisterReceiver(msgReceiver);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_content, container, false);
        contentTextView = view.findViewById(R.id.tv_content_information);
        timeTextView = view.findViewById(R.id.tv_time);
        dateTextView = view.findViewById(R.id.tv_date);
        temperatureTextView = view.findViewById(R.id.tv_temperature);
        addressTextView = view.findViewById(R.id.tv_address);
        weekTextView = view.findViewById(R.id.tv_week);
        ariConditionTextView = view.findViewById(R.id.tv_weather_air_condition);
        initTime();
        return view;
    }

    private void initTime(){
        final Calendar c = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");// HH:mm
        Date date = new Date(System.currentTimeMillis());
        String[] time = simpleDateFormat.format(date).split(" ");
        timeTextView.setText(time[1]);
        dateTextView.setText(time[0]);
        String mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
        if("1".equals(mWay)){
            mWay ="天";
        }else if("2".equals(mWay)){
            mWay ="一";
        }else if("3".equals(mWay)){
            mWay ="二";
        }else if("4".equals(mWay)){
            mWay ="三";
        }else if("5".equals(mWay)){
            mWay ="四";
        }else if("6".equals(mWay)){
            mWay ="五";
        }else if("7".equals(mWay)){
            mWay ="六";
        }
        weekTextView.setText("星期"+mWay);
    }

    public String contentToString(){
        return timeTextView.getText().toString()+ ","+ temperatureTextView.getText().toString() + ","+
                dateTextView.getText().toString() + "," + ariConditionTextView.getText().toString()+","+
                weekTextView.getText().toString() + "," + addressTextView.getText().toString() + ","+
                contentTextView.getText().toString();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
    @Override
    public void onDetach() {
        super.onDetach();
    }

}
