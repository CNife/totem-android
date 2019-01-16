package drz.oddb.Memory;
import java.util.*;
public class TupleList {
    public List<Tuple> tuplelist;
    public int tuplenum = 0;

    public void addTuple(Tuple tuple){
        this.tuplelist.add(tuple);
        tuplenum++;
    }

}
