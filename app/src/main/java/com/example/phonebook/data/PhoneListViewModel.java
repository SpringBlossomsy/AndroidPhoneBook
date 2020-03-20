package com.example.phonebook.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;

import com.example.phonebook.R;
import com.example.phonebook.activity.DetailActivity;
import com.example.phonebook.databinding.ActivityDetailBinding;
import com.example.phonebook.models.PhoneDto;
import com.example.phonebook.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhoneListViewModel extends BaseViewModel {
    private MutableLiveData<List<PhoneDto>> phoneDtos;
    private MutableLiveData<Integer> page;

    public PhoneListViewModel() {
        super();
        page = new MutableLiveData<>();
        page.setValue(1);
    }

    public MutableLiveData<List<PhoneDto>> getPhoneDtos() {
        if (phoneDtos == null) {
            phoneDtos = new MutableLiveData<List<PhoneDto>>();
//            loadPhoneDtos(page.getValue());
        }
        return phoneDtos;
    }

    public MutableLiveData<Integer> getPage() {
        if (page == null) {
            page = new MutableLiveData<>();
//            page.setValue(1);
        }
        return page;
    }

    public void setPage(Integer newPage) {
        page.setValue(newPage);
    }

    public void addPhoneDto(PhoneDto phoneDto) {
        if (phoneDto != null) {
            phoneDtos.getValue().add(phoneDto);
        }
    }

    public void loadPhoneDtos(final Integer queryPage) {
        Call<ResponseBody> model = service.getAllPhones(Integer.toString(queryPage));
        model.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                List<PhoneDto> newPhoneDtos = new ArrayList<>();
                try {
                    JSONObject json = new JSONObject(response.body().string());

                    JSONArray phones = json.getJSONArray("phone_list");
                    for (int i = 0; i < phones.length(); i++) {
                        JSONObject phone = phones.getJSONObject(i);
                        Bitmap imageBmp;
                        try {
                            URL imageUrl = new URL(Constants.URL_DOMAIN + phone.getString("image"));
                            imageBmp = BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream());
                        } catch (Exception e){
                            e.printStackTrace();
                            imageBmp = null;
                        }
//                        PhoneDto tempPhone = new PhoneDto(
//                                phone.getString("name"),
//                                phone.getString("phone"),
//                                phone.getString("image"),
//                                null);
//                        tempPhone.updateBitmap();
//                        newPhoneDtos.add(tempPhone);
                        newPhoneDtos.add(new PhoneDto(
                                phone.getString("name"),
                                phone.getString("phone"),
                                phone.getString("image"),
                                imageBmp));
                    }
                    if (newPhoneDtos.size() > 0) {
                        phoneDtos.postValue(newPhoneDtos);
                        message.setValue("数据加载完成");
                    } else {
                        message.postValue("没有更多数据...");
                        setPage(queryPage - new Integer(1));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    message.setValue("JSON解析错误...");
                } catch (IOException e) {
                    e.printStackTrace();
                    message.setValue("IO错误...");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.print(t.getMessage());
                message.setValue("数据加载失败...");
            }
        });
    }
}
