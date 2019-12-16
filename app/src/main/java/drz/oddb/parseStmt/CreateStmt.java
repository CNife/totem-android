package drz.oddb.parseStmt;

import java.util.ArrayList;

public class CreateStmt extends RawStmt{
	
	public String classname;
	public ArrayList<RelAttr> cols = new ArrayList();
	
	@Override
	public String toString() {
		return "CreateStmt{" +
				"classname='" + classname + '\'' +
				", cols=" + cols.toString() +
				'}';
	}
}

