package com.starnet.projects.server.Activity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.starnet.projects.server.Fragment.ConnectionFragment;
import com.starnet.projects.server.Fragment.ContentFragment;
import com.starnet.projects.server.Fragment.SetLCDFragment;
import com.starnet.projects.server.Fragment.UserManagementFragment;
import com.starnet.projects.server.MyConnectService;
import com.starnet.projects.server.R;
import com.starnet.projects.server.Utils.DataBaseUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.Manifest.*;
import static android.Manifest.permission.*;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private List<Fragment> fragments = null;
    private static final int BAIDU_READ_PHONE_STATE =100;
    private DataBaseUtils dataBaseUtils = null;
    private SQLiteDatabase database = null;
    private MyConnectService connectService = null;
    private Intent serverIntent = null;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyConnectService.ClientBinder localBinder = (MyConnectService.ClientBinder)service;
            connectService = localBinder.getConnectService();
            connectService.startListen();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connectService = null;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBaseUtils = new DataBaseUtils(getApplicationContext(),"user_database",null,1);
        database = dataBaseUtils.getWritableDatabase();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        init();
        if(ContextCompat.checkSelfPermission(this, permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED) {
            // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
            requestPermissions( new String[]{ READ_PHONE_STATE,WRITE_EXTERNAL_STORAGE,ACCESS_FINE_LOCATION,READ_EXTERNAL_STORAGE },BAIDU_READ_PHONE_STATE );
        }
        selectItem(0);
        serverIntent = new Intent(MainActivity.this,MyConnectService.class);
        getApplicationContext().bindService(serverIntent,connection,Service.BIND_AUTO_CREATE);
        //new ConnectThread().start();
    }

    class ConnectThread extends Thread{
        @Override
        public void run(){
            sendHttpPost();
        }
    }

    public MyConnectService getConnectService(){
        return connectService;
    }

    public void sendHttpPost(){
        try{
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH)+1;
            int day = c.get(Calendar.DAY_OF_MONTH);
            JSONObject body = new JSONObject();
            body.put("type","1");
            body.put("time", year+"-"+month+"-"+day);
            body.put("deviceName", Build.MODEL);
            body.put("deviceID",Build.ID);
            body.put("deviceType","0");
            body.put("info","Android");
            String urlPath = "http://172.20.10.5:8080/UserManager2/androidController.do";
            URL url = new URL(urlPath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            // 设置contentType
            conn.setRequestProperty("Content-Type", "application/json");
            DataOutputStream os = new DataOutputStream( conn.getOutputStream());
            String content = String.valueOf(body);
            os.writeBytes(content);
            os.flush();
            os.close();
            if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Looper.prepare();
                Toast.makeText(getApplicationContext(), "更新设备信息成功", Toast.LENGTH_SHORT).show();
                Looper.loop();
                InputStreamReader in = new InputStreamReader(conn.getInputStream());
                BufferedReader bf = new BufferedReader(in);
                String recieveData = null;
                String result = "";
                while ((recieveData = bf.readLine()) != null){
                    result += recieveData + "\n";
                }
                Looper.prepare();
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                Looper.loop();
                in.close();
                conn.disconnect();
            }else if(conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND){
                Looper.prepare();
                Toast.makeText(getApplicationContext(), "你的设备不在数据库中", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException io){
            io.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions,grantResults);
        switch(requestCode) {
            //requestCode即所声明的权限获取码，在checkSelfPermission时传入
            case 1:
                BAIDU_READ_PHONE_STATE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //获取到权限，做相应处理
                    //调用定位SDK应确保相关权限均被授权，否则会引起定位失败
                } else{
                    //没有获取到权限，做特殊处理
                }
                break;
            default:
                break;
        }
    }

    public void init(){
        fragments =new ArrayList<Fragment>();
        fragments.add(ContentFragment.newInstance());
        fragments.add(SetLCDFragment.newInstance());
        fragments.add(ConnectionFragment.newInstance());
        fragments.add(UserManagementFragment.newInstance());
        dataBaseUtils = new DataBaseUtils(getApplicationContext(),"user_database",null,1);
        database = dataBaseUtils.getWritableDatabase();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
            selectItem(0);
        } else if (id == R.id.nav_gallery) {
            selectItem(1);
        } else if (id == R.id.nav_slideshow) {
            selectItem(2);
        } else if (id == R.id.nav_manage) {
            selectItem(3);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void selectItem(int position){
        Fragment fragment = fragments.get(position);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.activity_fragment_container,fragment).commit();
    }
}
