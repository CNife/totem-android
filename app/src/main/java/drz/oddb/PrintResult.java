package drz.oddb;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import drz.*;
import drz.oddb.Memory.TupleList;

import java.util.ArrayList;


public class PrintResult extends AppCompatActivity {

    private final int W = ViewGroup.LayoutParams.WRAP_CONTENT;
    private final int M = ViewGroup.LayoutParams.MATCH_PARENT;
    private TableLayout rst_tab;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.print_result);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Print((TupleList) bundle.getSerializable("tupleList"),bundle.getStringArray("attrname"),bundle.getIntArray("attrid"),bundle.getStringArray("type"));
    }

     public void Print(TupleList tpl,String[] attrname,int[] attrid,String[] type){

        int tabCol  = attrid.length;
         int tabH = tpl.tuplenum;
        //Object[] tabCol = new Object[attrid.length];
         //Object[] tabH = new Object[tpl.tuplenum];
        //ArrayList<Object> tabCol = new ArrayList<Object>(attrid.length);
        //ArrayList<Object> tabH = new ArrayList<Object>(tpl.tuplenum);

        int r;
        int c;
        String stemp;
        int itemp;
        Object oj;

         rst_tab = findViewById(R.id.rst_tab);

        for(r = 0;r <= tabH;r++){
            TableRow tableRow = new TableRow(this);
            for(c = 0;c < tabCol;c++){
                TextView tv = new TextView(this);
                if(r == 0){
                    tv.setText(attrname[attrid[c]]);
                }
                else{
                    oj = tpl.tuplelist.get(r-1).tuple[c];
                    switch (type[attrid[c]]){
                        case "int":
                            itemp = Integer.parseInt(oj.toString());
                            tv.setText(itemp+"");
                        case "char":
                            stemp = oj.toString();
                            tv.setText(stemp);
                    }
                }
                tv.setGravity(Gravity.CENTER);

                tableRow.addView(tv);
            }
            rst_tab.addView(tableRow,new TableLayout.LayoutParams(W,M));
        }

    }

}
