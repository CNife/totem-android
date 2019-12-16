package drz.oddb.parseStmt;

public class DropStmt extends RawStmt{
	public String classname;
	
	@Override
	public String toString() {
		return "DropStmt{" +
				"classname='" + classname + '\'' +
				'}';
	}

}
