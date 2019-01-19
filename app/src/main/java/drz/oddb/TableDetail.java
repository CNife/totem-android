package drz.oddb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import drz.oddb.Transaction.SystemTable.ObjectTable;

public class TableDetail extends AppCompatActivity {
    private String[] objects = new String[20];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TableDetail","oncreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_class);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(TableDetail.this,android.R.layout.simple_list_item_1,objects);
        final ListView tableList = (ListView)findViewById(R.id.tablelist);
        tableList.setAdapter(adapter);
        Intent intent = getIntent();
        Bundle bundle0 = intent.getExtras();
        showObjTab((ObjectTable)bundle0.getSerializable("ObjectTable"));

    }
    private void showObjTab(ObjectTable topt){
        Object oj1,oj2,oj3,oj4;
        String stemp1,stemp2,stemp3,stemp4;

        for(int i = 0; i < topt.objectTable.size(); i++){
            oj1 = topt.objectTable.get(i).classid;
            oj2 = topt.objectTable.get(i).tupleid;
            oj3 = topt.objectTable.get(i).blockid;
            oj4 = topt.objectTable.get(i).tupleid;
            stemp1 = oj1.toString();
            stemp2 = oj2.toString();
            stemp3 = oj3.toString();
            stemp4 = oj4.toString();
            objects[i] = stemp1+" "+stemp2+" "+stemp3+" "+stemp4;
            String curObject = objects[i];
            Toast.makeText(TableDetail.this,curObject,Toast.LENGTH_LONG).show();

        }
    }

}
