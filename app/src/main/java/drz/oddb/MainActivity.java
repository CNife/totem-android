package drz.oddb;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import drz.oddb.Memory.*;
import drz.oddb.Transaction.SystemTable.ClassTable;
import drz.oddb.Transaction.SystemTable.ClassTableItem;
import drz.oddb.Transaction.TransAction;

public class MainActivity extends AppCompatActivity {

    //查询输入框

    private EditText editText;
    private TextView text_view;
    TransAction trans = new TransAction();


    //BGM
    //private Intent intent = new Intent("com.angel.Android.MUSIC");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = new Intent(MainActivity.this,MusicServer.class);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //播放BGM
        startService(intent);

        //查询按钮
        Button button = findViewById(R.id.button);
        editText = findViewById(R.id.edit_text);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trans.query(editText.getText().toString());
                //startActivity(new Intent(("android.intent.action.PRINTRST")));
            }
        });

        //退出按钮
        Button exit_button = findViewById(R.id.exit_button);
        exit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showexitdialog(v);
            }
        });
    }
    protected void onStop(){
        Intent intent = new Intent(MainActivity.this,MusicServer.class);
        stopService(intent);
        super.onStop();
    }

    //对输入框输入的内容进行检测，若格式不正确，则弹出对话框提示
    /*根据需求添加，暂时不加入*/
    /*@Override
    public void onCheck(){
        if(
        //格式错误
         ) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle("Attention");
            dialog.setMessage("Please enter the correct query content!");
            dialog.setCancelable(false);
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.show();
        }
    }
    */

    //点击exit_button退出程序
    public void showexitdialog(View v){
        //定义一个新对话框对象
        AlertDialog.Builder exit_dialog = new AlertDialog.Builder(this);
        //设置对话框提示内容
        exit_dialog.setMessage("Do you want to save it before exiting the program?");
        //定义对话框两个按钮及接受事件
        exit_dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //保存
                trans.SaveAll();
                //退出
                android.os.Process.killProcess(android.os.Process.myPid());

            }
        });
        exit_dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //退出
                android.os.Process.killProcess(android.os.Process.myPid());

            }
        });
        //创建并显示对话框
        AlertDialog exit_dialog0 = exit_dialog.create();
        exit_dialog0.show();

    }
    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }*/

}
