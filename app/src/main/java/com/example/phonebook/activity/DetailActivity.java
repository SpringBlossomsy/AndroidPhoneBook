package com.example.phonebook.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.phonebook.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailActivity extends AppCompatActivity {
    public String URL_DOMAIN = "http://192.168.13.34:5000";
    private Uri portraitLocalUri;
    private Bitmap bitmap;
    private String originalStatus;
    private String originalName;
    private String originalPhone;
    private String originalImageURL;
    private Boolean uploadPhoneInfoStatus;
    private int position;
    private String newImageURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        originalStatus = getIntent().getStringExtra("STATUS");
        originalName = getIntent().getStringExtra("NAME");
        originalPhone = getIntent().getStringExtra("PHONE");
        originalImageURL = getIntent().getStringExtra("IMAGE");
        position = getIntent().getIntExtra("POSITION", -1);
        newImageURL = "";

        changeView(originalStatus, originalName, originalPhone, originalImageURL);
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
                portrait.setImageResource(R.drawable.portrait);
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
                    Glide.with(this)
                            .load(URL_DOMAIN + imageURL)
                            .placeholder(R.drawable.portrait)
                            .error(R.drawable.portrait) //error
                            .into(portrait);
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
                    Log.i("TEST", imageURL);
                    Glide.with(this)
                            .load(URL_DOMAIN + imageURL)
                            .placeholder(R.drawable.portrait)
                            .error(R.drawable.portrait) //error
                            .into(portrait);
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

    public boolean uploadPhoneInfo(String status, final String name, final String phone, final Uri localImageUri) {
        Log.i("Upload Message", status + " " + name + " "+ phone + " " + localImageUri);

        final String uploadURL;
        switch (status) {
            case "UPDATE":
                uploadURL = URL_DOMAIN + "/api/phone/update/";
                break;
            default:
                uploadURL = URL_DOMAIN + "/api/phone/add/";
                break;
        }
        uploadPhoneInfoStatus = true;

        Thread thread = new Thread() {

            @Override
            public void run() {
                final String BOUNDARY = "******";
                final String  twoHyphens = "--";
                final String crlf = "\r\n";

                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    // setup request
                    URL url = new URL(uploadURL);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setUseCaches(false);
                    connection.setDoOutput(true);

                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("Cache-Control", "no-cache");
                    connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);

                    // start content wrapper
                    DataOutputStream request = new DataOutputStream(connection.getOutputStream());

                    // one field
                    Date date = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                    String attachmentName = "image";
                    String attachmentFileName = formatter.format(date) + ".JPEG";
                    request.writeBytes(twoHyphens + BOUNDARY + crlf);
                    request.writeBytes("Content-Disposition: form-data; name=\"" +
                            attachmentName + "\";filename=\"" +
                            attachmentFileName + "\"" + crlf);
                    request.writeBytes(crlf);

                    ImageView portrait = findViewById(R.id.portrait);
                    BitmapDrawable bitmapDrawable = ((BitmapDrawable) portrait.getDrawable());
                    Bitmap bitmap = bitmapDrawable .getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] imageInByte = stream.toByteArray();
                    request.write(imageInByte);

                    // one field
                    if (originalName != null) {
                        request.writeBytes(twoHyphens + BOUNDARY + crlf);
                        request.writeBytes("Content-Disposition: form-data; name=\"" + "originalName" + "\""+ crlf);
                        request.writeBytes("Content-Type: text/plain; charset=UTF-8" + crlf);
                        request.writeBytes(crlf);
                        request.writeBytes(originalName);
                        request.flush();
                    }

                    // one field
                    request.writeBytes(twoHyphens + BOUNDARY + crlf);
                    request.writeBytes("Content-Disposition: form-data; name=\"" + "name" + "\""+ crlf);
                    request.writeBytes("Content-Type: text/plain; charset=UTF-8" + crlf);
                    request.writeBytes(crlf);
                    request.writeBytes(name);
                    request.flush();

                    // one field
                    request.writeBytes(twoHyphens + BOUNDARY + crlf);
                    request.writeBytes("Content-Disposition: form-data; name=\"" + "phone" + "\""+ crlf);
                    request.writeBytes("Content-Type: text/plain; charset=UTF-8" + crlf);
                    request.writeBytes(crlf);
                    request.writeBytes(phone);
                    request.flush();

                    // end content wrapper
                    request.writeBytes(crlf);
                    request.writeBytes(twoHyphens + BOUNDARY + twoHyphens + crlf);

                    request.flush();
                    request.close();

                    //返回输入流
                    InputStream in = connection.getInputStream();

                    //读取输入流
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    Log.i("Api result", result.toString());

                    JSONObject json = new JSONObject(result.toString());
                    final String uploadStatus = json.getString("status");
                    final String message = json.getString("message");
                    newImageURL = json.getString("image");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                    if(uploadStatus == "success") {
                        Toast.makeText(DetailActivity.this, "upload success", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DetailActivity.this, message, Toast.LENGTH_SHORT).show();
                        uploadPhoneInfoStatus = false;
                    }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DetailActivity.this,  "upload failed as exception", Toast.LENGTH_SHORT).show();
                        }
                    });
                    uploadPhoneInfoStatus = false;
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {//关闭连接
                        connection.disconnect();
                    }
                }

            }
        };
        thread.start();
        return uploadPhoneInfoStatus;
    }

    public void rightButtonMethod(View view) {
        TextView textView = findViewById(R.id.textView);
        String title = (String)textView.getText();

        EditText name_edit = findViewById(R.id.name);
        EditText phone_edit = findViewById(R.id.phone);
        String newName, newPhone;
        switch (title) {
            case "新建联系人":
                Log.i("TEST", "Save Phone");
                newName = name_edit.getText().toString();
                newPhone = phone_edit.getText().toString();

                if (uploadPhoneInfo("ADD", newName, newPhone, portraitLocalUri)) {
                    Intent intent = new Intent(this, DetailActivity.class);
                    intent.putExtra("STATUS", "ADD");
                    intent.putExtra("NAME", newName);
                    intent.putExtra("PHONE", newPhone);
                    intent.putExtra("IMAGE", newImageURL);
                    setResult(RESULT_OK, intent);
                    DetailActivity.this.finish();
                }
                break;
            case "编辑联系人":
                Log.i("TEST", "Update Phone");
                newName = name_edit.getText().toString();
                newPhone = phone_edit.getText().toString();

                if (uploadPhoneInfo("UPDATE", newName, newPhone, portraitLocalUri)) {
                    Intent intent = new Intent(this, DetailActivity.class);
                    intent.putExtra("STATUS", "UPDATE");
                    intent.putExtra("NAME", newName);
                    intent.putExtra("PHONE", newPhone);
                    intent.putExtra("IMAGE", newImageURL);
                    intent.putExtra("POSITION", position);
                    setResult(RESULT_OK, intent);
                    DetailActivity.this.finish();
                }
                break;
            case "联系人详情":
                Log.i("TEST", "Change view");
                Intent intent = new Intent(this, DetailActivity.class);
                intent.putExtra("STATUS", "UPDATE");

                newName = name_edit.getText().toString();
                newPhone = phone_edit.getText().toString();

                changeView("UPDATE", newName, newPhone, "");
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
