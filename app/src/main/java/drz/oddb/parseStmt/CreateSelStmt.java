package drz.oddb.parseStmt;

import java.util.ArrayList;

public class CreateSelStmt extends CreateStmt{
	
	public String classname;
	public String originname;
	public ArrayList<RelAttr> relattrs = new ArrayList();
	public ArrayList<DeputyAttr> deputyattrs= new ArrayList();
	public String whereclause;

	
	@Override
	public String toString() {
		return "CreateSelStmt{" +
				"classname='" + classname + '\'' +
				", originname='" + originname + '\'' +
				", relattrs=" + relattrs.toString() +
				", deputyattrs=" + deputyattrs.toString() +
				", whereclause='" + whereclause + '\'' +
				'}';
	}

}
