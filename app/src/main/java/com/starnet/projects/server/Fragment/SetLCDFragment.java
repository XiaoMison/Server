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

import com.starnet.projects.server.R;

public class SetLCDFragment extends Fragment {
    private View view;
    private TextView textView;
    private MsgReceiver msgReceiver;
    private String content;


    public static SetLCDFragment newInstance() {
        SetLCDFragment fragment = new SetLCDFragment();
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("SetContent");
        getActivity().registerReceiver(msgReceiver, intentFilter);
    }

    public class MsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("SetContent")){
                content = intent.getStringExtra("content");
                Handler handler = new Handler();
                handler.post(runnable);
            }
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            textView.setText(content);
        }
    };

    @Override
    public void onDestroy(){
        super.onDestroy();
        getActivity().unregisterReceiver(msgReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_set_lcd, container, false);
        textView = view.findViewById(R.id.tv_set_content);
        return view;
    }
}
