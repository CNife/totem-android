package drz.oddb.Transaction.SystemTable;

import java.util.ArrayList;
import java.util.List;

public class ClassTable {
    public List<ClassTableItem> classTable=new ArrayList<>();
    public int maxid=0;

    public void clear(){
        classTable.clear();
        maxid = 0;
    }
}

