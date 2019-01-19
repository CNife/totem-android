package drz.oddb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import drz.oddb.Transaction.SystemTable.BiPointerTable;
import drz.oddb.Transaction.SystemTable.ClassTable;
import drz.oddb.Transaction.SystemTable.DeputyTable;
import drz.oddb.Transaction.SystemTable.ObjectTable;
import drz.oddb.Transaction.SystemTable.SwitchingTable;

public class ShowClass extends AppCompatActivity {
    private String[] classes = new String[10];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        init(classes);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_class);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ShowClass.this,android.R.layout.simple_list_item_1,classes);
        ListView classList = (ListView)findViewById(R.id.classlist);
        classList.setAdapter(adapter);
        classList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String curClass = classes[i];
                Intent intent = new Intent(ShowClass.this,ClassDetail.class);
                startActivity(intent);
            }
        });

    }
    private void showClass(ObjectTable topt, SwitchingTable switchingT, DeputyTable deputyt, BiPointerTable biPointerT, ClassTable classTable){
        int t = topt.objectTable.size();
        for(int i = 0; i < t; i++){
            classes[i] = "topt.objectTable.get(i).getClass()";
        }
    }

}
