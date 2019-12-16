package com.example.phonebook;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailActivity extends AppCompatActivity {
    public String URL_DOMAIN = "http://192.168.13.34:5000";
    public static final int GET_DATA_SUCCESS = 1;
    public static final int NETWORK_ERROR = 2;
    public static final int SERVER_ERROR = 3;
    //子线程不能操作UI，通过Handler设置图片
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case GET_DATA_SUCCESS:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    if(bitmap != null) {
                        ImageView portrait = findViewById(R.id.portrait);
                        portrait.setImageBitmap(bitmap);
                    }
                    break;
                case NETWORK_ERROR:
                    Toast.makeText(DetailActivity.this,"网络连接失败",Toast.LENGTH_SHORT).show();
                    break;
                case SERVER_ERROR:
                    Toast.makeText(DetailActivity.this,"服务器发生错误",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private Uri portraitLocalUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        String status = getIntent().getStringExtra("STATUS");
        String name = getIntent().getStringExtra("NAME");
        String phone = getIntent().getStringExtra("PHONE");
        String imageURL = getIntent().getStringExtra("IMAGE");

        changeView(status, name, phone, imageURL);
    }

    private void changeView(String status, String name, String phone, String imageURL) {
        TextView textView = findViewById(R.id.textView);
        TextView finish_btn = findViewById(R.id.finish_btn);
        TextView edit_btn = findViewById(R.id.edit_btn);
        ImageView portrait = findViewById(R.id.portrait);
        Button upload_btn = findViewById(R.id.upload_btn);
        EditText name_edit = findViewById(R.id.name);
        EditText phone_edit = findViewById(R.id.phone);
        ImageView call_icon = findViewById(R.id.call_icon);
        ImageView message_icon = findViewById(R.id.message_icon);
        switch (status) {
            case "ADD":
                textView.setText(R.string.phone_add_title);
                finish_btn.setText(R.string.finish);
                edit_btn.setText(R.string.cancel);
                portrait.setImageResource(R.drawable.user_portrait);
                upload_btn.setVisibility(View.VISIBLE);
                call_icon.setVisibility(View.INVISIBLE);
                message_icon.setVisibility(View.INVISIBLE);
                name_edit.setText(null);
                phone_edit.setText(null);
                name_edit.setInputType(InputType.TYPE_CLASS_TEXT);
                phone_edit.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case "UPDATE":
                textView.setText(R.string.phone_update_title);
                finish_btn.setText(R.string.finish);
                edit_btn.setText(R.string.cancel);
                upload_btn.setVisibility(View.VISIBLE);
                call_icon.setVisibility(View.INVISIBLE);
                message_icon.setVisibility(View.INVISIBLE);

                if(imageURL.length() > 0) {
                    setURLimage(URL_DOMAIN + imageURL);
                }

                name_edit.setText(name);
                phone_edit.setText(phone);
                name_edit.setInputType(InputType.TYPE_CLASS_TEXT);
                phone_edit.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case "DETAIL":
                textView.setText(R.string.phone_detail_title);
                finish_btn.setText(R.string.edit);
                edit_btn.setText(R.string.return_string);
                upload_btn.setVisibility(View.INVISIBLE);
                call_icon.setVisibility(View.VISIBLE);
                message_icon.setVisibility(View.VISIBLE);

                if(imageURL.length() > 0) {
                    setURLimage(URL_DOMAIN + imageURL);
                }

                name_edit.setText(name);
                phone_edit.setText(phone);
                name_edit.setInputType(InputType.TYPE_NULL);
                phone_edit.setInputType(InputType.TYPE_NULL);
                break;
            default:
                break;
        }
    }

    public void setURLimage(final String imageURL) {
        new Thread() {
            @Override
            public void run() {
                try {
                    //把传过来的路径转成URL
                    URL url = new URL(imageURL);
                    //获取连接
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    //使用GET方法访问网络
                    connection.setRequestMethod("GET");
                    //超时时间为10秒
                    connection.setConnectTimeout(10000);
                    //获取返回码
                    int code = connection.getResponseCode();
                    if (code == 200) {
                        InputStream inputStream = connection.getInputStream();
                        //使用工厂把网络的输入流生产Bitmap
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        //利用Message把图片发给Handler
                        Message msg = Message.obtain();
                        msg.obj = bitmap;
                        msg.what = GET_DATA_SUCCESS;
                        handler.sendMessage(msg);
                        inputStream.close();
                    }else {
                        //服务启发生错误
                        handler.sendEmptyMessage(SERVER_ERROR);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //网络连接错误
                    handler.sendEmptyMessage(NETWORK_ERROR);
                }
            }
        }.start();
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

                changeView("UPDATE", name, phone, "");
                break;
            default:
                break;
        }
    }

    public void leftButtonMethod(View view) {
        DetailActivity.this.finish();
    }

    public void takePhoto(View view) {
        Intent getImage = new Intent(Intent.ACTION_PICK, null);
        getImage.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");//这是图片类型
        startActivityForResult(getImage, 2);
    }

    public void clickToCallPhone(View view) {
        EditText phone_edit = findViewById(R.id.phone);
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone_edit.getText().toString()));
        startActivity(intent);
    }

    public void clickToSendMessge(View view) {
        EditText phone_edit = findViewById(R.id.phone);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("smsto:" + phone_edit.getText().toString()));
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case 1:
                    break;
                case 2:
                    if (data != null) {
                        portraitLocalUri = data.getData();
                        ImageView portrait = findViewById(R.id.portrait);
                        portrait.setImageURI(portraitLocalUri);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
