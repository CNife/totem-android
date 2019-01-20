package drz.oddb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import drz.oddb.Transaction.TransAction;

public class ShowTable extends AppCompatActivity {
    private String[] tables = new String[5];
    TransAction trans = new TransAction(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        showTable();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_class);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ShowTable.this,android.R.layout.simple_list_item_1,tables);
        ListView tableList = (ListView)findViewById(R.id.tablelist);
        tableList.setAdapter(adapter);
        tableList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //String curClass = tables[i];
                //Intent intent = new Intent(ShowTable.this,ShowObj.class);
                //startActivity(intent);
                switch (i){
                    case 0:trans.testObj();break;
                    case 1:trans.testSwi();break;
                    case 2:trans.testDep();break;
                    case 3:trans.testBi();break;
                    case 4:trans.testcla();break;
                }
            }
        });

    }
    private void showTable(){
        tables[0]="ObjectTable";
        tables[1]="SwitchingTable";
        tables[2]="DeputyTable";
        tables[3]="BiPointerTable";
        tables[4]="ClassTable";
        }

}
