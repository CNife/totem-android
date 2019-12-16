package drz.oddb.Transaction.SystemTable;

import java.io.Serializable;

public class AttributeTableItem  implements Serializable {

    public int    attrid = 0;
    public String attrname = null;         //属性名
    public String attrtype = null;         //属性类型
    public int classid = 0;                //类id
    public int isdeputy = 0;                //是否为虚属性

    public AttributeTableItem(int classid, int attrid, String attrname, String attrtype, int isdeputy) {
        this.classid = classid;
        this.attrname = attrname;
        this.attrtype = attrtype;
        this.attrid = attrid;
        this.isdeputy = isdeputy;
    }
    public AttributeTableItem(){}
}
