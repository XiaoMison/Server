package com.starnet.projects.server.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.starnet.projects.server.Activity.MainActivity;
import com.starnet.projects.server.MyListViewAdapter;
import com.starnet.projects.server.R;

import java.util.ArrayList;
import java.util.List;

public class ConnectionFragment extends Fragment {
    private View view;
    private ListView listView;
    private MyListViewAdapter adapter;
    private List<String> connectionString;


    public static ConnectionFragment newInstance() {
        ConnectionFragment fragment = new ConnectionFragment();
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_connection, container, false);
        listView = view.findViewById(R.id.lv_connection);
        initData();
        return view;
    }

    private void  initData(){
        connectionString = new ArrayList<>();
        MainActivity mainActivity = (MainActivity) getActivity();
        connectionString = mainActivity.getConnectService().getConnecedUser();
        connectionString.add("pxd 125.10.23.1");
        adapter = new MyListViewAdapter(getContext(),connectionString);
        listView.setAdapter(adapter);
    }
}
