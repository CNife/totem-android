package drz.oddb.Transaction.SystemTable;

public class BiPointerTableItem {
    int classid = 0;

    public BiPointerTableItem(int classid, int objectid, int deputyid, int deputyobjectid) {
        this.classid = classid;
        this.objectid = objectid;
        this.deputyid = deputyid;
        this.deputyobjectid = deputyobjectid;
    }

    int objectid = 0;
    int deputyid = 0;
    int deputyobjectid = 0;

}
