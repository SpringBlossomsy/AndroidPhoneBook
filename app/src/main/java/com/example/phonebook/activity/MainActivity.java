package com.example.phonebook.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.phonebook.utils.MyBaseAdapter;
import com.example.phonebook.R;
import com.example.phonebook.utils.ReFlashListView;
import com.example.phonebook.models.PhoneDto;
import com.example.phonebook.utils.PhoneUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.phonebook.data.PhoneListViewModel;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    PhoneListViewModel listModel;
    private ReFlashListView reFlashListView;
    private MyBaseAdapter adapter;
    private int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        reFlashListView = (ReFlashListView) findViewById(R.id.listview1);
        listModel = ViewModelProviders.of(this).get(PhoneListViewModel.class);
        listModel.getPhoneDtos().observe(this, new Observer<List<PhoneDto>>() {
            @Override
            public void onChanged(List<PhoneDto> phoneDtos) {
                if (phoneDtos != null) {
                    adapter = new MyBaseAdapter(listModel.getPhoneDtos().getValue());
                    reFlashListView.setAdapter(adapter);
                    reFlashListView.onRefreshComplete(true);
                }
                ImageView mIvRotate = findViewById(R.id.imageView2);
                mIvRotate.clearAnimation();
                mIvRotate.setVisibility(View.INVISIBLE);
                adapter.notifyDataSetChanged();
            }
        });
        listModel.getOtherFields().observe(this, new Observer<HashMap<String, String>>() {
            @Override
            public void onChanged(HashMap<String, String> otherFields) {
                if (otherFields != null) {
                    String page = otherFields.get("page");
                    String message = otherFields.get("message");
                    switch (otherFields.get("whichChanged")) {
                        case "page":
                            if (page != null) {
                                listModel.loadPhoneDtos(Integer.parseInt(page));
                            }
                            break;
                        case "message":
                            if (message != null) {
                                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case "both":
                            if (message != null) {
                                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                            reFlashListView.onRefreshComplete(true);
                            break;
                    }
                }
            }
        });
        reFlashListView.setOnItemClickListener(this);
        reFlashListView.setOnRefreshListener(new ReFlashListView.onRefreshListener() {
            @Override
            public void onRefresh() {
                //TODO: 下拉刷新的时候回调该方法，加载数据
                Integer newPage = Integer.parseInt(listModel.getOtherFields().getValue().get("page"));
                if (newPage > 1){
                    newPage = newPage - new Integer(1);
                }
                listModel.setOtherFields(Integer.toString(newPage), null, "page");
            }

            @Override
            public void onLoadMore() {
                // TODO: 上拉加载下一页数据的回调
                Integer newPage = Integer.parseInt(listModel.getOtherFields().getValue().get("page")) + new Integer(1);
                listModel.setOtherFields(Integer.toString(newPage), null, "page");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void exportPhones() {
        PhoneUtil phoneUtil = new PhoneUtil(this);
        List<PhoneDto> localPhoneDots = phoneUtil.getPhone();
        for (PhoneDto phoneDto: listModel.getPhoneDtos().getValue()) {
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

        if(hasPermissions(MainActivity.this, PERMISSIONS)==false){
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
        PhoneDto phoneDto = adapter.getItem(position-1);
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
//                    String name = intent.getStringExtra("NAME");
//                    String phone = intent.getStringExtra("PHONE");
//                    String image = intent.getStringExtra("IMAGE");
                    int changedPosition = intent.getIntExtra("POSITION", -1);
                    Log.i("UPLOAD", Integer.toString(page));
                    switch (status) {
                        case "ADD":
//                            listModel.loadPhoneDtos(listModel.getPage().getValue());
                            listModel.setOtherFields(listModel.getOtherFields().getValue().get("page"), null, "page");
                            break;
                        case "UPDATE":
                            if (changedPosition != -1) {
//                                listModel.loadPhoneDtos(listModel.getPage().getValue());
                                listModel.setOtherFields(listModel.getOtherFields().getValue().get("page"), null, "page");
                            }
                            break;
                    }
                }
                break;
        }


    }
}

