package drz.oddb.Transaction.SystemTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SwitchingTable implements Serializable {
    public int maxid = 1;
    public List<SwitchingTableItem> switchingTable=new ArrayList<>();

}
