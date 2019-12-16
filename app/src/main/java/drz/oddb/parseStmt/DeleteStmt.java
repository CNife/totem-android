package drz.oddb.parseStmt;

public class DeleteStmt extends RawStmt {
	public String classname;
	public String whereclause;
	@Override
	public String toString() {
		return "DeleteStmt{" +
				"classname='" + classname + '\'' +
				", whereclause='" + whereclause + '\'' +
				'}';
	}

}
