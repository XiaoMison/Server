package com.starnet.projects.server.Activity;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.starnet.projects.server.R;
import com.starnet.projects.server.Utils.DataBaseUtils;

public class AddUserActivity extends Activity {
    private EditText userNameEditTextView;
    private EditText passwordEditTextView;
    private Button addUserButton;
    private DataBaseUtils dataBaseUtils = null;
    private SQLiteDatabase database = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        dataBaseUtils = new DataBaseUtils(getApplicationContext(),"user_database",null,1);
        database = dataBaseUtils.getWritableDatabase();
        userNameEditTextView = findViewById(R.id.et_add_user_name);
        passwordEditTextView = findViewById(R.id.et_add_user_password);
        addUserButton = findViewById(R.id.bt_server_add_user);
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userNameEditTextView.getText().toString().equals("")||passwordEditTextView.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
                }else {
                    insertUser();
                }
            }
        });
    }

    private void insertUser(){
        if(database == null){
            Toast.makeText(getApplicationContext(), "数据库初始化失败", Toast.LENGTH_SHORT).show();
            return;
        }
        Cursor cursor = database.rawQuery("select * from userList where userName = '" + userNameEditTextView.getText().toString() + "'",null);
        if(cursor.getCount() != 0){
            Toast.makeText(getApplicationContext(), "该用户名不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        String sql = "insert into userList(userName,password) values(?,?)";
        database.execSQL(sql,new String[]{userNameEditTextView.getText().toString(),passwordEditTextView.getText().toString()});
        Toast.makeText(getApplicationContext(), "添加用户成功", Toast.LENGTH_SHORT).show();
        return;
    }
}
