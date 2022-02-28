package com.mapping.filemapping;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.jaredrummler.android.colorpicker.ColorPickerView;

public class ColorPickerActivity_old extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);


        ColorPickerView cpv = findViewById(R.id.colorPicker);
        cpv.setOnColorChangedListener( new ColorPickerView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int newColor) {
                //cpv.getColor()で取得できる値「ARGB」
                //例）ffb58d8d

                Log.i("onColorChanged", "getColor=" + cpv.getColor());
                Log.i("onColorChanged", "getColor(Hex)=" + Integer.toHexString(cpv.getColor()) );

                //選択された色を文字列として反映
                ((TextView)findViewById(R.id.etCategoryName)).setText( Integer.toHexString(cpv.getColor()) );
            }
        });

        //OK
        findViewById(R.id.bt_create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String color = ((TextView)findViewById(R.id.etCategoryName)).getText().toString();

                //resultコード設定
                Intent intent = getIntent();
                intent.putExtra("COLOR", "#" + color);
                setResult(RESULT_OK, intent);

                //元の画面へ戻る
                finish();
            }
        });
    }
}