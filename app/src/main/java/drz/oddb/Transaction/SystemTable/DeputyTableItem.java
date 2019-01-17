package drz.oddb.Transaction.SystemTable;

public class DeputyTableItem {
    public DeputyTableItem(int classid, int deputyid, String deputyname) {
        this.classid = classid;
        this.deputyid = deputyid;
        this.deputyname = deputyname;
    }

    public DeputyTableItem() {
    }

    public int classid = 0;            //类id
    public int deputyid = 0;           //代理类id
    public String deputyname = null;    //代理类名


}
