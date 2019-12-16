package drz.oddb.parseStmt;

import java.util.ArrayList;

import java.util.ArrayList;

public class attrcontext {

    public boolean isCross;
    public String attrname;
    public ArrayList<String> crossclass=new ArrayList();

    @Override
    public String toString() {
        return "attrcontext{" +
                ", isCross=" + isCross +
                ", attrname='" + attrname + '\'' +
                ", crossclass=" + crossclass.toString() +
                '}';
    }
}
