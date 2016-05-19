package com.example.ex07_09;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class DetailActivity extends Activity {

        MySQLiteHandler handler;
        EditText editId;
        EditText editName;
        EditText editAddress;

        Button btnUpdate;
        Button btnDelete;
        Button btnSelect;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);
        
        editId = (EditText)findViewById(R.id.editId);
        editName = (EditText)findViewById(R.id.editName);
        editAddress = (EditText)findViewById(R.id.editAddress);
        
        btnUpdate = (Button)findViewById(R.id.btnUpdate);
        btnDelete = (Button)findViewById(R.id.btnDelete);
        btnSelect = (Button)findViewById(R.id.btnSelect);
        
        
        Intent intent = getIntent();
        String _id = intent.getStringExtra("_id");
        
        handler = MySQLiteHandler.open(getApplicationContext());
        Cursor c  = handler.selectById(_id);

        if( c.moveToNext()){
        
           editId.setText(c.getString(c.getColumnIndex("_id")));
           editName.setText(c.getString(c.getColumnIndex("name")));
           editAddress.setText(c.getString(c.getColumnIndex("address")));
        }
        

       // 수정버튼 이벤트처리
      btnUpdate.setOnClickListener(new View.OnClickListener() {

         @Override
         public void onClick(View v) {
             
AlertDialog.Builder builder = 
                   new AlertDialog.Builder(DetailActivity.this);
               builder.setTitle("수정하기");
               builder.setMessage("정말로 수정합니까?");
               builder.setIcon(android.R.drawable.stat_sys_warning);
               builder.setPositiveButton("확인", 
                         new DialogInterface.OnClickListener() {

                   @Override
                 public void onClick( DialogInterface dialog, int which) {

                 
                        String _id = editId.getText().toString();
                        String name = editName.getText().toString();
                        String address = editAddress.getText().toString();

                        handler.update(_id, name, address);
                    
                        finish();
                 }
             });
              builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                      dialog.dismiss();
                 }
             });
                builder.show();
          }
       });

      // 삭제버튼 이벤트 처리
     btnDelete.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
             Log.i("MyTag" , "delete");
AlertDialog.Builder builder = 
                   new AlertDialog.Builder(DetailActivity.this);
               builder.setTitle("삭제하기");
               builder.setMessage("정말로 삭제합니까?");
               builder.setIcon(android.R.drawable.stat_sys_warning);

               builder.setPositiveButton("확인", 
                         new DialogInterface.OnClickListener() {

                   @Override
                 public void onClick(DialogInterface dialog, int which) {

                 
                        String _id2 = editId.getText().toString();
                        handler.delete(_id2);
                        finish();
                 }
             });
              builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                      dialog.dismiss();
                 }
             });
                builder.show();

         }
      });

     // 목록보기 버튼 이벤트처리
     btnSelect.setOnClickListener(new View.OnClickListener() {

       @Override
       public void onClick(View v) {
             finish();
        }
     });
    }//end onCreate

   
}//end class