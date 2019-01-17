package drz.oddb.Transaction.SystemTable;

public class TopTableItem{
    public String dbname = null; //数据库名

    public int classid = 0;    //类id
    public int tupleid = 0;    //元组id
    public int blockid = 0;    //块id
    public int offset = 0;      //元祖偏移量

    public int dbid = 0;       //数据库id

    public TopTableItem(String dbname, int dbid, int classid, int tupleid, int blockid, int offset) {
        this.dbname = dbname;
        this.dbid = dbid;
        this.classid = classid;
        this.tupleid = tupleid;
        this.blockid = blockid;
        this.offset = offset;
    }




    public TopTableItem() {
    }
}