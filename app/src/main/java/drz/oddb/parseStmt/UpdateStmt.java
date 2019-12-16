package drz.oddb.parseStmt;

import java.util.ArrayList;

public class UpdateStmt extends RawStmt {
	public String classname;
	public ArrayList<String> attrs = new ArrayList();
	public ArrayList<String> values = new ArrayList();
	public String whereclause;
	
	@Override
	public String toString() {
		return "UpdateStmt{" +
				"classname='" + classname + '\'' +
				", attrs=" + attrs.toString() +
				", values=" + values.toString() +
				", whereclause='" + whereclause + '\'' +
				'}';
	}
	

}
