package drz.oddb.parseStmt;

import java.util.ArrayList;

public class InsertStmt extends RawStmt{
	public String classname;
	public ArrayList<String> attrnames = new ArrayList(); 
	public ArrayList<String> attrvalues = new ArrayList();   

	
	@Override
	public String toString() {
		return "InsertStmt{" +
				"classname='" + classname + '\'' +
				", attrnames=" + attrnames.toString() +
				", attrvalues=" + attrvalues.toString() +
				'}';
	}
}
