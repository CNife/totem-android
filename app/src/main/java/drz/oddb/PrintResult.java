package drz.oddb;

import android.content.Intent;
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


    }

    public void Print(TupleList tpl,String[] attrname,int[] attrid,String[] type){
        startActivity(new Intent(("android.intent.action.PRINTRST")));

        ArrayList<Object> tabCol = new ArrayList<>(attrid.length);
        ArrayList<Object> tabH = new ArrayList<>(tpl.tuplenum);

        int r;
        int c;
        String stemp;
        int itemp;
        Object oj;

        for(r = 0;r <= tabH.size();r++){
            TableRow tableRow = new TableRow(this);
            for(c = 0;c < tabCol.size();c++){
                TextView tv = new TextView(this);
                if(r == 0){
                    tv.setText(attrname[c]);
                }
                else{
                    oj = tpl.tuplelist.get(r-1).tuple[c];
                    switch (type[c]){
                        case "int":
                            itemp = Integer.parseInt((String)oj);
                            tv.setText(itemp);
                        case "char":
                            stemp = (String)oj;
                            tv.setText(stemp);
                    }
                }
                tv.setGravity(Gravity.CENTER);
                tableRow.addView(tv);
            }
            rst_tab.addView(tableRow,new TableLayout.LayoutParams(W,M));
        }
        rst_tab = findViewById(R.id.rst_tab);
    }

}
