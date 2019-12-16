package drz.oddb.Transaction.SystemTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DeputyRuleTable implements Serializable {
    public int maxid = 0;
    public List<DeputyRuleTableItem> deputyRuleTable=new ArrayList<>();

    public void clear(){
        deputyRuleTable.clear();
        maxid = 0;
    }
}
