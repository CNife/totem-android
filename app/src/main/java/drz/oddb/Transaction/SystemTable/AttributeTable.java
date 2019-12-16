package drz.oddb.Transaction.SystemTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AttributeTable  implements Serializable {
    public List<AttributeTableItem> attributeTable=new ArrayList<>();
    public int maxid=0;

    public void clear(){
        attributeTable.clear();
        maxid = 0;
    }
}
