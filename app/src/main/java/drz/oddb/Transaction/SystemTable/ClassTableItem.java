package drz.oddb.Transaction.SystemTable;

import java.io.Serializable;

public class ClassTableItem implements Serializable {
    public String classname = null;        //类名
    public int classid = 0;                //类id
    public int attrnum = 0;                //类属性个数
    public int classtype = 0;        //类类型

    public ClassTableItem(String classname, int classid, int attrnum,int classtype) {
        this.classname = classname;
        this.classid = classid;
        this.attrnum = attrnum;
        this.classtype = classtype;
    }
    public ClassTableItem(){}

}
