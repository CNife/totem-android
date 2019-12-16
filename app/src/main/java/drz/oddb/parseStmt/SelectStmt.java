package drz.oddb.parseStmt;

import java.util.ArrayList;

public class SelectStmt extends RawStmt{
    public boolean isOk;
    public String classname;
    public ArrayList<attrcontext> attrs= new ArrayList();
    public String whereclause;


    @Override
    public String toString() {
        return "SelectStmt{" +
                "NodeTag='" + NodeTag + '\'' +
                ", isOk=" + isOk +
                ", classname='" + classname + '\'' +
                ", attrs=" + attrs.toString() +
                ", whereclause='" + whereclause + '\'' +
                '}';
    }
}
