package drz.oddb.Memory;


import java.util.Dictionary;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Tuple {
    public int tupleHeader;
    public Object[] tuple;

    public Tuple(Object[] values) {
        tuple = values.clone();
        tupleHeader = values.length;
    }

    public Tuple(){}

}
