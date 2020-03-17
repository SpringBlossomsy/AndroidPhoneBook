package com.example.phonebook.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.phonebook.MyBaseAdapter;
import com.example.phonebook.R;
import com.example.phonebook.ReFlashListView;
import com.example.phonebook.models.PhoneDto;
import com.example.phonebook.utils.PhoneUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    public String URL_DOMAIN = "http://192.168.13.34:5000";
    private List<PhoneDto> newPhoneDots;
    private List<PhoneDto> tempPhoneDots;
    private ReFlashListView reFlashListView;
    private MyBaseAdapter adapter;
    private int page = 1;
    private int perpage = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        newPhoneDots = new ArrayList<>();

        reFlashListView = (ReFlashListView) findViewById(R.id.listview1);
        reFlashListView.setOnItemClickListener(this);
        reFlashListView.setOnRefreshListener(new ReFlashListView.onRefreshListener() {
            @Override
            public void onRefresh() {
                //TODO: 下拉刷新的时候回调该方法，加载数据
                if (page > 1){
                    page = page - 1;
                }
                getPhoneData("Refresh", page);
            }

            @Override
            public void onLoadMore() {
                // TODO: 上拉加载下一页数据的回调
                page = page + 1;
                getPhoneData("Load", page);
            }
        });
        getPhoneData("Normal", page);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("TEST", "onResume");
//        page = 1;
//        getPhoneData("Refresh");
    }


    public void getPhoneData(final String status, final int newPage) {
        Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_circle_rotate);
        ImageView mIvRotate = findViewById(R.id.imageView2);
        LinearInterpolator interpolator = new LinearInterpolator();
        rotateAnimation.setInterpolator(interpolator);
        mIvRotate.startAnimation(rotateAnimation);

        tempPhoneDots = new ArrayList<>();

        Thread thread = new Thread() {

            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL(URL_DOMAIN + "/api/phone/list/?page=" + Integer.toString(newPage));
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
                        Bitmap imageBmp;
                        try {
                            URL imageUrl = new URL(URL_DOMAIN + phone.getString("image"));
                            imageBmp = BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream());
                        } catch (Exception e){
                            imageBmp = null;
                        }
                        tempPhoneDots.add(new PhoneDto(
                                phone.getString("name"),
                                phone.getString("phone"),
                                phone.getString("image"),
                                imageBmp));
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(tempPhoneDots.size() != 0) {
                                newPhoneDots = tempPhoneDots;
                                adapter = new MyBaseAdapter(newPhoneDots);
                                reFlashListView.setAdapter(adapter);
                                reFlashListView.onRefreshComplete(true);
                                adapter.notifyDataSetChanged();
                            } else {
                                page = page - 1;
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

    private void exportPhones() {
        PhoneUtil phoneUtil = new PhoneUtil(this);
        List<PhoneDto> localPhoneDots = phoneUtil.getPhone();
        for (PhoneDto phoneDto: newPhoneDots) {
            String name = phoneDto.getName();
            String phone = phoneDto.getTelPhone();
            Bitmap imageBmp = phoneDto.getImageBmp();
            boolean isAddContact = true;
            for (PhoneDto itemLocalPhone: localPhoneDots) {
                if (itemLocalPhone.getName().equals(phoneDto.getName()) == true) {
                    if (phoneUtil.update(itemLocalPhone, phoneDto) == false) {
                        Toast.makeText(MainActivity.this, name + "-" + phone + " export(update) failed.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, name + "-" + phone + " export(update) success.", Toast.LENGTH_SHORT).show();
                    }
                    isAddContact = false;
                    break;
                }
            }
            if (isAddContact == true) {
                if (phoneUtil.insert(name, phone, imageBmp) == false) {
                    Toast.makeText(MainActivity.this, name + "-" + phone + " export failed.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, name + "-" + phone + " export success.", Toast.LENGTH_SHORT).show();
                }
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
        PhoneDto phoneDto = newPhoneDots.get(position-1);
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("STATUS", "DETAIL");
        intent.putExtra("NAME", phoneDto.getName());
        intent.putExtra("PHONE", phoneDto.getTelPhone());
        intent.putExtra("IMAGE", phoneDto.getImage());
        intent.putExtra("POSITION", position);
        startActivityForResult(intent, 1);
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    String status = intent.getStringExtra("STATUS");
                    String name = intent.getStringExtra("NAME");
                    String phone = intent.getStringExtra("PHONE");
                    String image = intent.getStringExtra("IMAGE");
                    int changedPosition = intent.getIntExtra("POSITION", -1);
                    Log.i("TEST", Integer.toString(page));
                    switch (status) {
                        case "ADD":
                            newPhoneDots.add(new PhoneDto(name, phone, image, null));
                            adapter.notifyDataSetChanged();
                            getPhoneData("Refresh", page);
                            break;
                        case "UPDATE":
                            if (changedPosition != -1) {
                                newPhoneDots.get(changedPosition-1).updatePhoneDto(name, phone, image, null);
                                adapter.notifyDataSetChanged();
                                getPhoneData("Refresh", page);
                            }
                            break;
                    }
                }
                break;
        }


    }
}

