package com.objectdeputydatabase.wyj.oddb;

import java.util.HashMap;
import java.util.List;

//磁盘
class PageHeaderData{
    int startFreespace;					//空闲空间开始位置
    int endFreespace;					//空闲空间结束位置
    int tupleNum;						//元组数
    boolean[] flag = new  boolean[1024];	//位图
}

class header{
    int recordLen;
    boolean[] flag = new  boolean[50];		//位图
}

//缓冲区
class bufferTag{
    int dbOid;		//数据库id
    int tableOid;		//表id
    int blockNum;		//块号
}

class sbufesc{
    bufferTag tag;		//包含数据库id、表id和块号
    Boolean flag;		//标记改块是否为脏（true为脏）
    int buf_id;		//缓冲区索引号
}

//内存
class HeapTupleHeader{
    int attrNum;						//属性个数
    int[] attrType = new int[10]; 			//属性类型（0为String, 1为int. 默认为0）
    String[] attrName = new String [10];	//属性名
}

class ItemPointerData{
    bufferTag tag;
    int offset;			//块内偏移
}

class HeapTupleData{
    ItemPointerData	t_self;	//记录块号及偏移
    HeapTupleHeader	t_data;	//表头信息
    boolean[] flag = new  boolean[50];		//记录对应属性是否为空（0为空，1不为空）
    List <String> value;	//属性值
}
