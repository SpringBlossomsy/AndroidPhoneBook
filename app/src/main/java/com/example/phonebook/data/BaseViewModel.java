package com.example.phonebook.data;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.phonebook.utils.Api;
import com.example.phonebook.utils.Constants;

import retrofit2.Retrofit;

public class BaseViewModel extends ViewModel {
    public Retrofit retrofit;
    public Api service;

    public BaseViewModel() {
        super();
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_DOMAIN)
                .build();
        service = retrofit.create(Api.class);
    }
}
