package com.starnet.projects.server;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.util.Base64;

import com.starnet.projects.server.Utils.DataBaseUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyConnectService extends Service {
    private ServerSocket serverSocket;
    private Map<String,Socket> socketList;
    private final int port = 8888;
    private DataBaseUtils dataBaseUtils = null;
    private SQLiteDatabase database = null;
    private boolean isExit = true;
    private ServerThread serverThread = null;
    BufferedOutputStream bos = null;
    BufferedInputStream bis = null;
    @Override
    public IBinder onBind(Intent intent) {
        return new ClientBinder();
    }

    @Override
    public boolean onUnbind(Intent intent){
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        isExit = false;
    }

    public class ClientBinder extends Binder {
        public MyConnectService getConnectService(){
            return MyConnectService.this;
        }
    }
    @Override
    public void onCreate(){
        super.onCreate();
        socketList = new HashMap<String, Socket>();
        dataBaseUtils = new DataBaseUtils(getApplicationContext(),"user_database",null,1);
        database = dataBaseUtils.getWritableDatabase();
    }

    public void startListen(){
        serverThread = new ServerThread();
        serverThread.start();
    }

    private class ServerThread extends Thread{
        @Override
        public void run(){
            try{
                serverSocket = new ServerSocket(port);
                while(true){
                    Socket clientSocket = serverSocket.accept();
                    bis = new BufferedInputStream(clientSocket.getInputStream());
                    bos = new BufferedOutputStream(clientSocket.getOutputStream());
                    ClientThread clientThread = new ClientThread(clientSocket);
                    clientThread.start();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private class ClientThread extends Thread{
        private Socket clientSocket;
        private String userName;
        ClientThread(Socket socket){
            clientSocket = socket;
        }
        @Override
        public void run(){
            while(true){
                byte[] data = new byte[1024];
                int size = 0;
                try {
                    while ((size = bis.read(data)) != -1) {
                        String str = new String(data,0,size);
                        //收到客户端发送的请求后，立马回一条心跳给客户端
                        if(str.startsWith("101")) {
                            String detail = decodeMessage(str.substring(4));
                            if(checkLogin(detail)){
                                bos.write("102:LoginSuccess".getBytes());
                                bos.flush();
                                userName = detail.split(",")[0];
                                socketList.put(userName,clientSocket);
                            }else{
                                bos.write("102:LoginFailed".getBytes());
                                bos.flush();
                            }
                        }else if(str.startsWith("103")){
                            String detail = decodeMessage(str.substring(4));
                            bos.write("104:SetContentSuccess".getBytes());
                            bos.flush();
                            Intent contentIntent = new Intent("SetContent");
                            contentIntent.putExtra("content",detail);
                            sendBroadcast(contentIntent);
                        }
                        else if(str.startsWith("105")){
                            String detail = decodeMessage(str.substring(4));
                            if(checkLogin(detail.substring(0,detail.lastIndexOf(",")))){
                                String sql = "update userList set password = ? where userName = ?";
                                database.execSQL(sql,new String[]{detail.split(",")[2],detail.split(",")[0]});
                                bos.write("106:UpdateSuccess".getBytes());
                                bos.flush();
                            }else{
                                bos.write("106:UpdateFailed".getBytes());
                                bos.flush();
                            }
                        }
                        else if(str.startsWith("109")){
                            Intent contentIntent = new Intent("RequestContent");
                            sendBroadcast(contentIntent);
                        }else if(str.startsWith("111")){
                            bis.close();
                            bos.close();
                            socketList.remove(userName);
                            clientSocket.close();
                            break;
                        }
                        else {
                            bos.write("001:Yes,I hear you!".getBytes());
                            bos.flush();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
            socketList.remove(userName);
        }
    }

    public void sendMessage(final String message){
        if (serverSocket==null||bis==null||bos==null){
        }else{
            new Thread() {
                @Override
                public void run() {
                    try {
                        bos.write(message.getBytes());
                        bos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    private boolean checkLogin(String user){
        Cursor cursor = database.rawQuery("select * from userList where userName = ? and password = ?",user.split(","));
        if(cursor.getCount() == 0 ){
            return false;
        }else{
            return true;
        }
    }

    public List<String> getConnecedUser(){
        List<String> connectedUser = new ArrayList<>();
        for(Map.Entry<String,Socket> entry:socketList.entrySet()){
            connectedUser.add(entry.getKey() + " " + entry.getValue().getInetAddress().toString());
        }
        return connectedUser;
    }

    private String decodeMessage(String message){
        return new String(Base64.decode(message,Base64.NO_WRAP));
    }

    public Map<String,Socket> getSocketList(){
        return socketList;
    }
}
