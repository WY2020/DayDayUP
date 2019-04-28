package com.zx.myapplication;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class showActivity extends AppCompatActivity {


    private TextView title;
    private EditText content;
    private Button BuEditable;
    //判断是否是可编辑
    private boolean isEditable=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉标题栏，状态栏
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        Window window = showActivity.this.getWindow();
        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.showActivityColor));


        setContentView(R.layout.activity_show);


        BuEditable=findViewById(R.id.buEditable);
        title=findViewById(R.id.textView);
        content=findViewById(R.id.textView2);

        content.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", content.getText().toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getApplicationContext(),"已保存到剪切板",Toast.LENGTH_LONG).show();
                return false;

            }
        });

        BuEditable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditable=true;
                Toast.makeText(getApplicationContext(),"内容可编辑,返回即可保存",Toast.LENGTH_LONG).show();
                content.setEnabled(true);
            }
        });

        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        title.setText(bundle.getString("title"));
        content.setText(bundle.getString("content"));



    }


    /*********************监听返回键***********************/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //如果修改了文本执行语句
            if (isEditable) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(showActivity.this, R.style.NoBackGroundDialog);
                View view2 = LayoutInflater.from(showActivity.this).inflate(R.layout.show_exit, null);
                builder.setView(view2);
                builder.setCancelable(false);
                //布局组件
                final Button bu_exit = view2.findViewById(R.id.button);
                final Button bu_cancel = view2.findViewById(R.id.button2);
                bu_exit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isEditable=false;
                        Intent intent=getIntent();
                        Bundle bun=new Bundle();
                        bun.putCharSequence("Title",title.getText().toString());
                        bun.putCharSequence("newText",content.getText().toString());
                        intent.putExtras(bun);
                        setResult(0x110,intent);
                        finish();
                    }
                });
                bu_cancel.setOnClickListener(new View.OnClickListener() {
                    final AlertDialog dialog = builder.show();

                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
        }
            return super.onKeyDown(keyCode, event);

    }
}
