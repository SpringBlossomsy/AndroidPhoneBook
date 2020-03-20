package com.example.phonebook.utils;

import java.text.SimpleDateFormat;

public interface Constants {
    String URL_DOMAIN = "http://192.168.13.34:5000";

    interface ADD {
       String TITLE = "新建联系人";
       String LEFT = "取消";
       String RIGHT = "完成";
    }

    interface UPDATE {
        String TITLE = "编辑联系人";
        String LEFT = "取消";
        String RIGHT = "完成";
    }

    interface DETAIL {
        String TITLE = "联系人详情";
        String LEFT = "返回";
        String RIGHT = "编辑";
    }

    SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
}
