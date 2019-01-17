package drz.oddb.Transaction.SystemTable;

public class DeputyTableItem {
    public DeputyTableItem(int originid, int deputyid, String deputyrule) {
        this.originid = originid;
        this.deputyid = deputyid;
        this.deputyrule = deputyrule;
    }

    public DeputyTableItem() {
    }

    public int originid = 0;            //类id
    public int deputyid = 0;           //代理类id
    public String[] deputyrule = null;    //代理guize


}
