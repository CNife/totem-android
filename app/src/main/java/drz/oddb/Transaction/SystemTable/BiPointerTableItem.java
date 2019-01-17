package drz.oddb.Transaction.SystemTable;

public class BiPointerTableItem {
    public int classid = 0;
    public int objectid = 0;
    public int deputyid = 0;
    public int deputyobjectid = 0;

    public BiPointerTableItem(int classid, int objectid, int deputyid, int deputyobjectid) {
        this.classid = classid;
        this.objectid = objectid;
        this.deputyid = deputyid;
        this.deputyobjectid = deputyobjectid;
    }

    public BiPointerTableItem(){}

}
