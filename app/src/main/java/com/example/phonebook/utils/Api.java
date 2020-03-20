package com.example.phonebook.utils;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface Api {
    @GET("/api/phone/list/")
    Call<ResponseBody> getAllPhones(@Query("page")String page);

    @Multipart
    @POST("/api/phone/add/")
    Call<ResponseBody> uploadPhone(@Part("name") RequestBody name,
                                   @Part("phone") RequestBody phone,
                                   @Part MultipartBody.Part imageFile);

    @Multipart
    @POST("/api/phone/update/")
    Call<ResponseBody> updatePhone(@Part("originalName") RequestBody originalName,
                                   @Part("name") RequestBody name,
                                   @Part("phone") RequestBody phone,
                                   @Part MultipartBody.Part imageFile);

}
