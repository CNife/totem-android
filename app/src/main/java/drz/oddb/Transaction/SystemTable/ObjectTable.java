package drz.oddb.Transaction.SystemTable;

import java.util.ArrayList;
import java.util.List;

public class ObjectTable {
    public  List<ObjectTableItem> topTable=new ArrayList<>();
    public int maxTupleId = 0;
    public void clear(){
       topTable.clear();
        maxTupleId = 0;
    }
}

