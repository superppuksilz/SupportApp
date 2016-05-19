package com.example.ex07_09;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    //생성자
    public MySQLiteOpenHelper(Context context, String name,
                           CursorFactory factory, int version) {
                 super(context, name, factory, version);

    }

   //테이블 생성 코드
   @Override
   public void onCreate(SQLiteDatabase db) {
        
String sql= "create table student ( _id integer primary key autoincrement ,"               + " name text , age integer , address text )";
   db.execSQL(sql);
}

  //테이블 삭제 코드
  @Override
 public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)   {

   String sql = "drop table if exists student";
   db.execSQL(sql);

   onCreate(db);
 }

}
