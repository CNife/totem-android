package drz.oddb.Transaction.SystemTable;

import java.io.Serializable;

public class DeputyRuleTableItem implements Serializable {
    public DeputyRuleTableItem(int ruleid, String deputyrule) {
        this.ruleid = ruleid;
        this.deputyrule = deputyrule;
    }

    public DeputyRuleTableItem() {
    }

    public int ruleid = 0;            //rule id
    public String deputyrule = null;    //代理guizedui
}
