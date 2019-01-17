package drz.oddb.Transaction.SystemTable;

import java.util.ArrayList;
import java.util.List;

public class ObjectTable {
    public  List<ObjectTableItem> objectTable =new ArrayList<>();
    public int maxTupleId = 0;
    public void clear(){
       objectTable.clear();
        maxTupleId = 0;
    }
}

