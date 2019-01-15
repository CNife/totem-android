package drz.oddb.Memory;


import java.util.List;

class TopTable{       //数据库id，表id关系表
    List<TopTableItem> topTable;
    class TopTableItem{
        int dbid = 0;       //数据库id
        int tableid = 0;    //表id
        int tupleid = 0;    //元组id
        int blockid = 0;    //块id
    }
}

class ClassTable{       //类id，表id关系表
    List<ClassTableItem> classTable;
    class ClassTableItem{
        int classid = 0;                //类id
        String classname = null;        //类名
        int tableid = 0;                //表id
        String tablename = null;        //表名
        int attrnum = 0;                //表属性个数
    }
}

class DbTable {         //数据库id，数据库名
    List<DbTableItem> dbTable;
    class DbTableItem{
        int dbid = 0;            //数据库id
        String dbname = null;    //表名
    }
}


class AttrTable {       //属性表
    List<AttrTableItem> attrTable;
    class AttrTableItem{
        int chassid = 0;         //类id
        int tableid = 0;         //表id
        String attrname = null;  //属性名
        String type = null;      //属性类型
    }
}

class DeputyTable {     //代理类表
    List<DeputyTableItem> deputyTable;
    class DeputyTableItem{
        int classid = 0;            //类id
        int deputyid = 0;           //代理类id
        String deputynum = null;    //代理类名
    }
}
