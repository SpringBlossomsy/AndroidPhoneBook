package com.example.phonebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private List<Map<String, String>> dataList;
    private List<PhoneDto> phoneDtos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPhoneData();
    }


    public void getPhoneData() {
        Toast.makeText(MainActivity.this, "Loding...", Toast.LENGTH_SHORT).show();
        dataList = new ArrayList<Map<String, String>>();

        Thread thread = new Thread() {

            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL("https://bb0cb017-e4c3-4fa7-ba5a-f8461a64e722.mock.pstmn.io/test");
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
                    JSONArray phones = json.getJSONArray("phone");
                    for (int i = 0; i < phones.length(); i++) {
                        JSONObject phone = phones.getJSONObject(i);
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("title", phone.getString("name"));
                        map.put("value", phone.getString("phone"));
                        dataList.add(map);
                    }
                    Log.i("DataList", dataList.toString());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ListView list_test = (ListView) findViewById(R.id.listview1);
                            SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, dataList, R.layout.mylistitem, new String[] { "title", "value" }, new int[] { R.id.mylistitem_title, R.id.mylistitem_value });
                            list_test.setAdapter(adapter);
                        }
                    });

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
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
    }

    public void refreshPhoneData(View view) {
        getPhoneData();
    }

    private boolean isHaveExistName(List<PhoneDto> phoneDtos, String name) {
        for (PhoneDto phoneObject: phoneDtos) {
            if (phoneObject.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean insert(String given_name, String mobile_number) {
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
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void exportPhones() {
        PhoneUtil phoneUtil = new PhoneUtil(this);
        phoneDtos = phoneUtil.getPhone();
        Log.i("Phones Detail", phoneDtos.toString());
        Log.i("Data Detail", dataList.toString());
        for (Map<String, String> map: dataList) {
            String name = map.get("title");
            String phone = map.get("value");

            if (isHaveExistName(phoneDtos, name) == true) {
                Log.i("isHaveExistName", "exist");
                Toast.makeText(MainActivity.this, name + "-" + phone + " already exist.", Toast.LENGTH_SHORT).show();
                continue;
            } else {
                if (insert(name, phone) == false) {
                    Toast.makeText(MainActivity.this, name + "-" + phone + " export failed.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, name + "-" + phone + " export success.", Toast.LENGTH_SHORT).show();
                }
            }
        }
        phoneDtos = phoneUtil.getPhone();
        Log.i("After Phone Detail", phoneDtos.toString());
        Log.i("After Data Detail", dataList.toString());

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

    private void check() {
        //判断是否有权限
        String[] PERMISSIONS = {
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.WRITE_CONTACTS,
        };

//        if(hasPermissions(MainActivity.this, PERMISSIONS)){
            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS,201);
//        } else {
//            exportPhones();
//        }
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
        check();
    }

}

