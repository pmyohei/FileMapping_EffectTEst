package com.mapping.filemapping;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class SinglePictureDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_picture_display);

        //共通データから、選択されたギャラリーのリストを取得
        Intent intent = getIntent();
        ArrayList<PictureTable> galley =  (ArrayList)intent.getSerializableExtra("test");


        Log.i("単体表示", "galley.size()=" + galley.size() );

        RecyclerView rv_singlePicture = findViewById(R.id.rv_singlePicture);
        rv_singlePicture.setAdapter( new SinglePictureAdapter(this, galley ) );
        rv_singlePicture.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false ));



    }
}