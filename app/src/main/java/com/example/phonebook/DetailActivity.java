package com.example.phonebook;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        String status = getIntent().getStringExtra("STATUS");
        String id, name, phone;
        Log.i("STATUS", status);
        TextView textView = findViewById(R.id.textView);
        TextView finish_btn = findViewById(R.id.finish_btn);
        ImageView portrait = findViewById(R.id.portrait);
        Button upload_btn = findViewById(R.id.upload_btn);
        EditText name_edit = findViewById(R.id.name);
        EditText phone_edit = findViewById(R.id.phone);
        switch (status) {
            case "ADD":
                textView.setText(R.string.phone_add_title);
                finish_btn.setVisibility(View.VISIBLE);
                portrait.setImageResource(R.drawable.user_portrait);
                upload_btn.setVisibility(View.VISIBLE);
                name_edit.setText(null);
                phone_edit.setText(null);
                break;
            case "UPDATE":
                textView.setText(R.string.phone_update_title);
                finish_btn.setVisibility(View.VISIBLE);
                upload_btn.setVisibility(View.VISIBLE);

                id = getIntent().getStringExtra("ID");
                name = getIntent().getStringExtra("NAME");
                phone = getIntent().getStringExtra("PHONE");

                name_edit.setText(name);
                phone_edit.setText(phone);
                break;
            case "DETAIL":
                textView.setText(R.string.phone_detail_title);
                finish_btn.setVisibility(View.INVISIBLE);
                upload_btn.setVisibility(View.INVISIBLE);

                name = getIntent().getStringExtra("NAME");
                phone = getIntent().getStringExtra("PHONE");

                name_edit.setText(name);
                phone_edit.setText(phone);
                break;
            default:
                break;
        }
    }

    public void addPhone(View view) {
        Log.i("TEST", "TEST");
    }
}
