package com.example.phonebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.phonebook.models.PhoneDto;
import com.example.phonebook.utils.PhoneUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    public String URL_DOMAIN = "http://192.168.13.34:5000";
    private List<Map<String, String>> dataList;
    private List<PhoneDto> localPhoneDots;
    private List<PhoneDto> newPhoneDots;
    private SimpleAdapter adapter;
    ReFlashListView reFlashListView;
    private int page = 1;
    private Bitmap[] bmpArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        reFlashListView = (ReFlashListView) findViewById(R.id.listview1);
        reFlashListView.setOnItemClickListener(this);
        reFlashListView.setOnRefreshListener(new ReFlashListView.onRefreshListener() {
            @Override
            public void onRefresh() {
                //TODO: 下拉刷新的时候回调该方法，加载数据
                page = 1;
                getPhoneData("Refresh");
            }

            @Override
            public void onLoadMore() {
                // TODO: 上拉加载下一页数据的回调
                page = page + 1;
                getPhoneData("Load");
            }
        });
        getPhoneData("Normal");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("TEST", "onResume");
//        page = 1;
//        getPhoneData("Refresh");
    }


    public void getPhoneData(final String status) {
        Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_circle_rotate);
        ImageView mIvRotate = findViewById(R.id.imageView2);
        LinearInterpolator interpolator = new LinearInterpolator();
        rotateAnimation.setInterpolator(interpolator);
        mIvRotate.startAnimation(rotateAnimation);

        dataList = new ArrayList<Map<String, String>>();
        bmpArray = new Bitmap[20];
        newPhoneDots = new ArrayList<>();

        Thread thread = new Thread() {

            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL(URL_DOMAIN + "/api/phone/list/?page=" + Integer.toString(page));
                    connection = (HttpURLConnection) url.openConnection();
                    //设置请求方法
                    connection.setRequestMethod("GET");
                    //设置连接超时时间（毫秒）
                    connection.setConnectTimeout(10000);
                    //设置读取超时时间（毫秒）
                    connection.setReadTimeout(10000);
                    connection.connect();

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
                    JSONArray phones = json.getJSONArray("phone_list");
                    for (int i = 0; i < phones.length(); i++) {
                        JSONObject phone = phones.getJSONObject(i);
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("title", phone.getString("name"));
                        map.put("value", phone.getString("phone"));
                        map.put("image", phone.getString("image"));
                        dataList.add(map);
//                        try {
//                            URL imageUrl = new URL(URL_DOMAIN + phone.getString("image"));
//                            bmpArray[i] = BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream());
//                        } catch (Exception e){
//                            bmpArray[i] = null;
//                        }
                        Bitmap imageBmp;
                        try {
                            URL imageUrl = new URL(URL_DOMAIN + phone.getString("image"));
                            imageBmp = BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream());
                        } catch (Exception e){
                            imageBmp = null;
                        }
                        bmpArray[i] = imageBmp;
                        newPhoneDots.add(new PhoneDto(
                                phone.getString("name"),
                                phone.getString("phone"),
                                phone.getString("image"),
                                imageBmp));
                    }
                    Log.i("DataList", dataList.toString());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(dataList.size() != 0) {
                                adapter = new SimpleAdapter(MainActivity.this, dataList, R.layout.mylistitem, new String[]{"title", "value"}, new int[]{R.id.mylistitem_title, R.id.mylistitem_value});
                                reFlashListView.setAdapter(adapter);
                                reFlashListView.onRefreshComplete(true);
                            } else {
                                Toast.makeText(MainActivity.this,  " 数据加载完毕...", Toast.LENGTH_SHORT).show();
                                reFlashListView.onRefreshComplete(true);
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,  " Fetch api failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
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
                    ImageView mIvRotate = findViewById(R.id.imageView2);
                    mIvRotate.clearAnimation();
                    mIvRotate.setVisibility(View.INVISIBLE);
                }

            }
        };
        thread.start();
    }

    public boolean insert(String given_name, String mobile_number, Bitmap bmp) {
        try {
            ContentValues values = new ContentValues();

            // 下面的操作会根据RawContacts表中已有的rawContactId使用情况自动生成新联系人的rawContactId
            Uri rawContactUri = getContentResolver().insert(
                    ContactsContract.RawContacts.CONTENT_URI, values);
            long rawContactId = ContentUris.parseId(rawContactUri);

            // 向data表插入姓名数据
            if (given_name != "") {
                values.clear();
                values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
                values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, given_name);
                getContentResolver().insert(ContactsContract.Data.CONTENT_URI,values);
            }

            // 向data表插入电话数据
            if (mobile_number != "") {
                values.clear();
                values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, mobile_number);
                values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
                getContentResolver().insert(ContactsContract.Data.CONTENT_URI,values);
            }

            // 向data表插入头像数据
            if (bmp != null) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
                byte[] avatar = os.toByteArray();
                values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
                values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, avatar);
                getContentResolver().insert(ContactsContract.Data.CONTENT_URI,values);


            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void exportPhones() {
//        PhoneUtil phoneUtil = new PhoneUtil(this);
//        localPhoneDots = phoneUtil.getPhone();
//        Log.i("Phones Detail", localPhoneDots.toString());
//        Log.i("Data Detail", dataList.toString());
//        int i = 0;
//        for (Map<String, String> map: dataList) {
//            String name = map.get("title");
//            String phone = map.get("value");
//            String image = map.get("image");
//
//            if (insert(name, phone,  bmpArray[i]) == false) {
//                Toast.makeText(MainActivity.this, name + "-" + phone + " export failed.", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(MainActivity.this, name + "-" + phone + " export success.", Toast.LENGTH_SHORT).show();
//            }
//            i = i + 1;
//        }
//        localPhoneDots = phoneUtil.getPhone();
//        Log.i("After Phone Detail", localPhoneDots.toString());
//        Log.i("After Data Detail", dataList.toString());

        for (PhoneDto phoneDto: newPhoneDots) {
            String name = phoneDto.getName();
            String phone = phoneDto.getTelPhone();
            Bitmap imageBmp = phoneDto.getImageBmp();
            if (insert(name, phone, imageBmp) == false) {
                Toast.makeText(MainActivity.this, name + "-" + phone + " export failed.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, name + "-" + phone + " export success.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public static boolean hasPermissions(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==201){
            exportPhones();
        }else{
            return;
        }
    }

    public void exportPhoneToLocal(View view) {
        String[] PERMISSIONS = {
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.WRITE_CONTACTS,
        };

        if(hasPermissions(MainActivity.this, PERMISSIONS)){
            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS,201);
        } else {
            exportPhones();
        }
    }

    public void addPhone(View view) {
        Intent intent = new Intent(this, DetailActivity.class);
        String message = "ADD";
        intent.putExtra("STATUS", message);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String name = (String) ((TextView)view.findViewById(R.id.mylistitem_title)).getText();
        String phone = (String) ((TextView)view.findViewById(R.id.mylistitem_value)).getText();
        String image = new String();
        for (Map<String, String> map: dataList) {
            if (name.equals(map.get("title"))) {
                image = map.get("image");
            }
        }
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("STATUS", "DETAIL");
        intent.putExtra("NAME", name);
        intent.putExtra("PHONE", phone);
        intent.putExtra("IMAGE", image);
        startActivity(intent);
    }
}

