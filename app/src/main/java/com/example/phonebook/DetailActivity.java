package com.example.phonebook;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
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
        String name = getIntent().getStringExtra("NAME");
        String phone = getIntent().getStringExtra("PHONE");

        changeView(status, name, phone);
    }

    private void changeView(String status, String name, String phone) {
        TextView textView = findViewById(R.id.textView);
        TextView finish_btn = findViewById(R.id.finish_btn);
        TextView edit_btn = findViewById(R.id.edit_btn);
        ImageView portrait = findViewById(R.id.portrait);
        Button upload_btn = findViewById(R.id.upload_btn);
        EditText name_edit = findViewById(R.id.name);
        EditText phone_edit = findViewById(R.id.phone);
        switch (status) {
            case "ADD":
                textView.setText(R.string.phone_add_title);
                finish_btn.setText(R.string.finish);
                edit_btn.setText(R.string.cancel);
                portrait.setImageResource(R.drawable.user_portrait);
                upload_btn.setVisibility(View.VISIBLE);
                name_edit.setText(null);
                phone_edit.setText(null);
                break;
            case "UPDATE":
                textView.setText(R.string.phone_update_title);
                finish_btn.setText(R.string.finish);
                edit_btn.setText(R.string.cancel);
                upload_btn.setVisibility(View.VISIBLE);

                name_edit.setText(name);
                phone_edit.setText(phone);
                break;
            case "DETAIL":
                textView.setText(R.string.phone_detail_title);
                finish_btn.setText(R.string.edit);
                edit_btn.setText(R.string.return_string);
                upload_btn.setVisibility(View.INVISIBLE);

                name_edit.setText(name);
                phone_edit.setText(phone);
                break;
            default:
                break;
        }
    }

    public void rightButtonMethod(View view) {
        TextView textView = findViewById(R.id.textView);
        String title = (String)textView.getText();
        Log.i("TEST", title);
        switch (title) {
            case "新建联系人":
                Log.i("TEST", "Save Phone");
                DetailActivity.this.finish();
                break;
            case "编辑联系人":
                Log.i("TEST", "Update Phone");
                DetailActivity.this.finish();
                break;
            case "联系人详情":
                Log.i("TEST", "Change view");
                Intent intent = new Intent(this, DetailActivity.class);
                intent.putExtra("STATUS", "UPDATE");

                EditText name_edit = findViewById(R.id.name);
                String name = name_edit.getText().toString();

                EditText phone_edit = findViewById(R.id.phone);
                String phone = phone_edit.getText().toString();

                changeView("UPDATE", name, phone);
                break;
            default:
                break;
        }
    }

    public void leftButtonMethod(View view) {
        DetailActivity.this.finish();
    }
}
