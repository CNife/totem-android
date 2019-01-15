package drz.oddb.Transaction;


import java.util.List;

class TopTable{       //总表
    List<TopTableItem> topTable;

}

class TopTableItem{
    String dbname = null; //数据库名
    int dbid = 0;       //数据库id
    int classid = 0;    //类id
    int tupleid = 0;    //元组id
    int blockid = 0;    //块id
    int offset = 0;      //元祖偏移量
}

class ClassTable{       //类表
    List<ClassTableItem> classTable;
    int maxid=0;

}

class ClassTableItem {
    String classname = null;        //类名
    int classid = 0;                //类id
    int attrnum = 0;                //类属性个数
    int    attrid = 0;
    String attrname = null;         //属性名
    String attrtype = null;         //属性类型

    public ClassTableItem(String classname, int classid, int attrnum,int attrid, String attrname, String attrtype) {
        this.classname = classname;
        this.classid = classid;
        this.attrnum = attrnum;
        this.attrname = attrname;
        this.attrtype = attrtype;
        this.attrid = attrid;
    }
}

class DeputyTable {     //代理类表
    List<DeputyTableItem> deputyTable;

}
class DeputyTableItem{
    int classid = 0;            //类id
    int deputyid = 0;           //代理类id
    String deputynum = null;    //代理类名
}
