package com.zx.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

;


public class ScrollingActivity extends AppCompatActivity {
    private ArrayAdapter adapter;
    private ListView listView;
    private List<String> arrayList;
    final String Path = "ZX_Title.txt";
    private byte[] buffer;
    private FileOutputStream fileOutputStream;
    private FileInputStream fileInputStream;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    //判断是否是第一次打开软件
    private int open=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //沉浸状态栏
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        //列表视图
        arrayList=new ArrayList<>();
        listView = findViewById(R.id.list_item);
        //应用启动start刷新列表
        start_list();
        setListViewHeight();

        /******************图片按钮   添加列表项  保存*************************/
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(ScrollingActivity.this, R.style.NoBackGroundDialog);
                View view1 = LayoutInflater.from(ScrollingActivity.this).inflate(R.layout.layout, null);
                builder.setView(view1);
                builder.setCancelable(false);
                //布局组件
                final EditText title_ET = view1.findViewById(R.id.editText);
                final EditText message_ET = view1.findViewById(R.id.editText2);
                builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (title_ET.getText().toString().isEmpty()) {
                            Toast.makeText(ScrollingActivity.this, "标题为Null", Toast.LENGTH_SHORT).show();
                        } else {
                            String title = title_ET.getText().toString();
                            if (open == 0) {
                                String message = message_ET.getText().toString();//li
                                outputString(title + ",", MODE_APPEND);   //添加到文件夹
                                sp = getSharedPreferences(title, MODE_PRIVATE);
                                editor = sp.edit();
                                editor.putString("title", title);
                                editor.putString("message", message);
                                editor.commit();
                                //刷新列表 ，保存文件
                                arrayList.add(title);
                                f5();
                                Toast.makeText(ScrollingActivity.this, "已保存，点击查看", Toast.LENGTH_SHORT).show();
                                open=1;
                            }
                            if(open == 1){
                                //获取标题文件 查找是否有同名文件
                                if (input_String().contains(title)) {
                                    Toast.makeText(getApplicationContext(), "Err:已有同名文件", Toast.LENGTH_SHORT).show();
                                } else {
                                    //没有同名——————运行方法————————
                                    String message = message_ET.getText().toString();//li
                                    outputString(title + ",", MODE_APPEND);   //添加到文件夹
                                    sp = getSharedPreferences(title, MODE_PRIVATE);

                                    editor.putString("title", title);
                                    editor.putString("message", message);
                                    editor.commit();
                                    //刷新列表 ，保存文件
                                    arrayList.add(title);
                                    f5();
                                    Toast.makeText(ScrollingActivity.this, "已保存，点击查看", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.show();
            }
        });


        /*****************列表视图  进入showActivity ***************************/
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String title = parent.getItemAtPosition(position).toString();
               // Toast.makeText(ScrollingActivity.this, title, Toast.LENGTH_SHORT).show();
                sp = getSharedPreferences(title, MODE_PRIVATE);
                /********获取保存文件内容**************/
                String title_text = sp.getString("title", null);
                String message_text = sp.getString("message", null);
                Intent intent = new Intent(ScrollingActivity.this, showActivity.class);
                Bundle bundle = new Bundle();
                bundle.putCharSequence("title", title_text);
                bundle.putCharSequence("content", message_text);
                intent.putExtras(bundle);
                startActivityForResult(intent,0x110);
            }
        });


        /*****************长按删除列表项***************************/
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final String title_remove = parent.getItemAtPosition(position).toString();
                new AlertDialog.Builder(ScrollingActivity.this, R.style.NoBackGroundDialog)
                        .setTitle("删除项")
                        .setMessage("确认删除 \"" + title_remove + "\"吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                File file= new File("/data/data/"+getPackageName() +"/shared_prefs",title_remove+".xml");
                                if(file.exists()){
                                    file.delete();
                                    Toast.makeText(ScrollingActivity.this, "删除成功", Toast.LENGTH_LONG).show();
                                }

                                arrayList.remove(position);
                                f5();
                                String delete_title=input_String();
                                String new_title=delete_title.replace(title_remove+",","");
                                outputString("",MODE_PRIVATE);
                                outputString(new_title,MODE_APPEND);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                return true;
            }
        });
    }


    /*****************顶部菜单***************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.settings:
                arrayList.clear();
                f5();
                try {
                    //  Toast.makeText(this, input_String(), Toast.LENGTH_SHORT).show();
                    String tit[] = input_String().split(",");
                    for (int i = 0; i < tit.length; i++) {
                        arrayList.add(tit[i]);
                    }
                    Toast.makeText(this,"已刷新 条目："+tit.length+"个", Toast.LENGTH_SHORT).show();
                    f5();
                } catch (Exception e) {
                }
                break;
            case R.id.message:
                Intent intent=new Intent(ScrollingActivity.this,appActivity.class);
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }



    /*********************退出程序***********************/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(ScrollingActivity.this, R.style.NoBackGroundDialog);
            View view2 = LayoutInflater.from(ScrollingActivity.this).inflate(R.layout.exit_dialog, null);
            builder.setView(view2);
            builder.setCancelable(false);
            //布局组件
            final Button bu_exit = view2.findViewById(R.id.button);
            final Button bu_cancel = view2.findViewById(R.id.button2);
            bu_exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    System.exit(0);
                }
            });
            bu_cancel.setOnClickListener(new View.OnClickListener() {
                final AlertDialog dialog = builder.show();
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
        return super.onKeyDown(keyCode, event);
    }


    /*****************进入应用开始刷新列表***************************/
    public void start_list(){
        /*****************添加到 集合 适配器 刷新列表***************************/
        try {
          //  Toast.makeText(this, input_String(), Toast.LENGTH_SHORT).show();
            String tit[] = input_String().split(",");
            for (int i = 0; i < tit.length; i++) {
                arrayList.add(tit[i]);
            }
            f5();
        } catch (Exception e) {
        }
    }


    /*****************写入文件***************************/
    private void outputString(String text,int model) {
        try {
            fileOutputStream = openFileOutput(Path,model);
            fileOutputStream.write(text.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*****************读取文件***************************/
    public String input_String() {
        String data = null;
        try {
            fileInputStream = openFileInput(Path);
            buffer = new byte[fileInputStream.available()];
            fileInputStream.read(buffer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    System.out.flush();
                    fileInputStream.close();
                  data=new String(new String(buffer));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return data;
    }

//刷新
    public void f5(){
        adapter = new ArrayAdapter(ScrollingActivity.this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);
    }

    //根据手机高度改变listview高度
    public void setListViewHeight(){
        int heigth = getWindowManager().getDefaultDisplay().getHeight();
       int d= getResources().getDimensionPixelSize(getResources().getIdentifier("navigation_bar_height", "dimen","android"));
       int z= getResources().getDimensionPixelSize(getResources().getIdentifier("status_bar_height", "dimen","android"));
        ViewGroup.LayoutParams p=listView.getLayoutParams();
        p.height=heigth-(d+z);
        listView.setLayoutParams(p);
    }

    //showActivity--接收返回文本到--主活动
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0x110 && resultCode==0x110){
            Bundle bundle=data.getExtras();
            String title1=bundle.getString("Title");
            String content1=bundle.getString("newText");
           // Toast.makeText(getApplicationContext(),"测试:"+title1+"and"+content1,Toast.LENGTH_LONG).show();
            //sp = getSharedPreferences(title1, MODE_PRIVATE);
            sp = getSharedPreferences(title1, MODE_PRIVATE);
            editor=sp.edit();
            editor.putString("title", title1);
            editor.putString("message", content1);
            editor.commit();
            Toast.makeText(ScrollingActivity.this, "已保存", Toast.LENGTH_SHORT).show();
        }
    }
}


