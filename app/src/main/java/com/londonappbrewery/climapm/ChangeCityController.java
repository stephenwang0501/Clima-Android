package com.londonappbrewery.climapm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

public class ChangeCityController extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_city_layout);

        final EditText editTextCity = findViewById(R.id.queryET);

        final ImageButton imageButtonBack = findViewById(R.id.backButton);
        imageButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                final Editable editable = editTextCity.getText();
                if (editable != null) {
                    final String city = editable.toString();
                    if (city.length() > 0) {
                        intent.putExtra(Intent.EXTRA_TEXT, city);
                    }
                }
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
