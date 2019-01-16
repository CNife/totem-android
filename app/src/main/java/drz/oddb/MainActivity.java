package drz.oddb;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
        Button button = (Button) findViewById(R.id.button);
        editText = (EditText) findViewById(R.id.edit_text);
        text_view = (TextView) findViewById(R.id.text_view);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trans.query(editText.getText().toString());
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
}
