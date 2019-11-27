package com.example.phonebook;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

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
    private List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Map<String, String> map = new HashMap<String, String>();
        map.put("title", "title1");
        map.put("value", "value1");
        Log.i("TESt", map.toString());
        dataList.add(map);
        Log.i("TESt", dataList.toString());

        ListView list_test = (ListView) findViewById(R.id.listview1);
        SimpleAdapter adapter = new SimpleAdapter(this, dataList, R.layout.mylistitem, new String[] { "title", "value" }, new int[] { R.id.mylistitem_title, R.id.mylistitem_value });
        list_test.setAdapter(adapter);

        getPhoneData();
    }


    public void getPhoneData() {
        new Thread(new Runnable() {

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

                    Log.i("Test api", "werwetwt");


                    //返回输入流
                    InputStream in = connection.getInputStream();

                    //读取输入流
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    Log.i("Test api result", result.toString());
                    //show(result.toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
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
        }).start();
    }

    public void refreshPhoneData(View view) {
        getPhoneData();
    }

    

    public void exportPhoneToLocal(View view) {

    }

}

