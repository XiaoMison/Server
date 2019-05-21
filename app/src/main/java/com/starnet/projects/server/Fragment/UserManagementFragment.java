package com.starnet.projects.server.Fragment;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.starnet.projects.server.Activity.AddUserActivity;
import com.starnet.projects.server.Utils.DataBaseUtils;
import com.starnet.projects.server.MyListViewAdapter;
import com.starnet.projects.server.R;

import java.util.ArrayList;
import java.util.List;

public class UserManagementFragment extends Fragment {
    private View view;
    private SwipeMenuListView listView;
    private List<String> userList;
    private MyListViewAdapter adapter;
    private DataBaseUtils dataBaseUtils = null;
    private SQLiteDatabase database = null;
    private Button addUserButton;


    public static UserManagementFragment newInstance() {
        UserManagementFragment fragment = new UserManagementFragment();
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBaseUtils = new DataBaseUtils(getContext(),"user_database",null,1);
        database = dataBaseUtils.getWritableDatabase();
        userList = new ArrayList<>();
        readFromDatabase();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_management, container, false);
        listView = view.findViewById(R.id.lv_user_list);
        addUserButton = view.findViewById(R.id.bt_add_user);
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(),AddUserActivity.class);
                startActivity(intent);
            }
        });
        initData();
        return view;
    }
    @Override
    public void onResume(){
        super.onResume();
        dataBaseUtils = new DataBaseUtils(getContext(),"user_database",null,1);
        database = dataBaseUtils.getWritableDatabase();
        userList = new ArrayList<>();
        readFromDatabase();
        initData();
        adapter.notifyDataSetChanged();
    }

    private void readFromDatabase(){
        if (database == null){
            Toast.makeText(getContext(), "数据库初始化失败", Toast.LENGTH_SHORT).show();
            return;
        }
        Cursor cursor = database.rawQuery("select userName from userList",null);
        while (cursor.moveToNext()){
            userList.add(cursor.getString(0));
        }
        return;
    }

    private void initData(){
        adapter = new MyListViewAdapter(getContext(),userList);
        listView.setAdapter(adapter);
        listView.setEmptyView(getActivity().findViewById(R.id.lv_user_list));
        // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator()
        {
            @Override
            public void create(SwipeMenu menu)
            {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(getActivity().getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                // set item width
                openItem.setWidth(dp2px(90));
                // set item title
                openItem.setTitle("编辑");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getActivity().getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(dp2px(90));
                // set a icon
                deleteItem.setTitle("删除");
                deleteItem.setTitleColor(Color.WHITE);
                deleteItem.setTitleSize(18);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        // set creator
        listView.setMenuCreator(creator);

        // step 2. listener item click event
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index)
            {
                String user = userList.get(position);

                switch (index)
                {
                    case 0:
                        // open
                        edit(user);
                        break;
                    case 1:
                        // delete
                        delete(user);
                        userList.remove(position);
                        adapter.notifyDataSetChanged();
                        break;
                }
                return false;
            }
        });

        // set SwipeListener
        listView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener()
        {
            @Override
            public void onSwipeStart(int position)
            {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position)
            {
                // swipe end
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {

                String device = userList.get(position);
                edit(device);
            }
        });
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    /**
     *
     * @param user
     */
    private void edit(String user)
    {

    }

    /**
     *
     * @param user
     */
    private void delete(String user)
    {
        String sql = "delete from userList where userName = ?";
        database.execSQL(sql,new String[]{user});
    }

}

