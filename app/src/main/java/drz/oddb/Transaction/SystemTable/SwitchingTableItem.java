package drz.oddb.Transaction.SystemTable;

import java.io.Serializable;

public class SwitchingTableItem implements Serializable {
    public int deputyId = 1;
    public String rule = null;

    public SwitchingTableItem(int deputyId, String rule) {
        this.deputyId = deputyId;
        this.rule = rule;
    }

    public SwitchingTableItem(){}
}
