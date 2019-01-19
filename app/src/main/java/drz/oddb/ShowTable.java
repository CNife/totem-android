package drz.oddb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ShowTable extends AppCompatActivity {
    private String[] tables = new String[5];

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
                String curClass = tables[i];
                Intent intent = new Intent(ShowTable.this,TableDetail.class);
                startActivity(intent);
            }
        });

    }
    private void showTable(){
        tables[0]="ObjectTable";
        tables[1]="switchingTable";
        tables[2]="DeputyTable";
        tables[3]="BiPointerTable";
        tables[4]="ClassTable";
        }

}
