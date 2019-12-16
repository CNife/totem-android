package drz.oddb.Transaction.SystemTable;

import java.io.Serializable;

public class DeputyTableItem implements Serializable {
    public DeputyTableItem(int originid, int deputyid, int ruleid) {
        this.originid = originid;
        this.deputyid = deputyid;
        this.ruleid = ruleid;
    }

    public DeputyTableItem() {
    }

    public int originid = 0;            //类id
    public int deputyid = 0;           //代理类id
    public int ruleid = 0;           //rule id


}
