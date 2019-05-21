package com.starnet.projects.server;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MyListViewAdapter extends BaseAdapter {
    List<String> userList = new ArrayList<String>();
    private Context mContext = null;
    private LayoutInflater mLayoutInflater = null;
    public MyListViewAdapter(Context context, List<String> userList){
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        this.userList = userList;
    }
    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public String getItem(int position) {
        return userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    static class ViewHolder{
        public TextView userNameTextView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.user_list_item_view,null);
            viewHolder = new ViewHolder();
            viewHolder.userNameTextView = convertView.findViewById(R.id.tv_username);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.userNameTextView.setText(userList.get(position));
        return convertView;
    }
}
