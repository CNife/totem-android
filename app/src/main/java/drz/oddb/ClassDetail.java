package drz.oddb;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ClassDetail extends AppCompatActivity {
    private String[] objects = new String[20];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("ClassDetail","oncreate");
        init(objects);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_class);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ClassDetail.this,android.R.layout.simple_list_item_1,objects);
        ListView classList = (ListView)findViewById(R.id.classlist);
        classList.setAdapter(adapter);
        classList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String curObject = objects[i];
                Toast.makeText(ClassDetail.this,curObject,Toast.LENGTH_LONG).show();
            }
        });

    }
    private void init(String[] objects){
        for(int i = 0; i < 20; i++){
            objects[i] = "Object"+i;
        }
    }

}
