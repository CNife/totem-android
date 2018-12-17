package drz.oddb;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    HashMap<bufferTag,sbufesc> hashMap;//hash加速根据数据库id、表id和块号查找数据是否在缓冲区
    List<sbufesc> FreeList;		//构建缓冲区freeList
    ByteBuffer buff=ByteBuffer.allocateDirect(8*1024*1000);
    protected sbufesc load(bufferTag tag){
        sbufesc Free=FreeList.get(0);
        if(Free.tag!=null) {
            save(Free.tag);
        }
        String path;
        path=Integer.toString(Free.tag.dbOid)+"//"+Integer.toString(Free.tag.tableOid);
        File file=new File(path);
        if(!file.exists()){
            try{
                file.createNewFile();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return null;
    }
    protected boolean save(bufferTag tag){

        return false;
    }
}
