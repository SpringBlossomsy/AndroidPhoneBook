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
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhoneListViewModel extends BaseViewModel {
    private MutableLiveData<List<PhoneDto>> phoneDtos;
    private MutableLiveData<HashMap<String, String>> otherFields;

    public PhoneListViewModel() {
        super();
        otherFields = new MutableLiveData<>();
        HashMap<String, String> tempOtherFields = new HashMap<>();
        tempOtherFields.put("page", "1");
        tempOtherFields.put("whichChanged", "page");
        otherFields.setValue(tempOtherFields);
    }

    public MutableLiveData<HashMap<String, String>> getOtherFields() {
        if (otherFields == null) {
            otherFields = new MutableLiveData<>();
        }
        return otherFields;
    }

    public void setOtherFields(String page, String message, String whichChanged) {
        HashMap<String, String> tempOtherFields = otherFields.getValue();
        switch (whichChanged) {
            case "page":
                tempOtherFields.put("page", page);
                break;
            case "message":
                tempOtherFields.put("message", message);
                break;
            case "both":
                tempOtherFields.put("page", page);
                tempOtherFields.put("message", message);
                break;
        }
        tempOtherFields.put("whichChanged", whichChanged);
        otherFields.setValue(tempOtherFields);
    }

    public MutableLiveData<List<PhoneDto>> getPhoneDtos() {
        if (phoneDtos == null) {
            phoneDtos = new MutableLiveData<List<PhoneDto>>();
        }
        return phoneDtos;
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
                        setOtherFields(null, "数据加载完成", "message");
                    } else {
                        setOtherFields(Integer.toString(queryPage - new Integer(1)), "没有更多数据...", "both");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    setOtherFields(null, "JSON解析错误...", "message");
                } catch (IOException e) {
                    e.printStackTrace();
                    setOtherFields(null, "IO错误...", "message");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.print(t.getMessage());
                setOtherFields(null, "数据加载失败...", "message");
            }
        });
    }
}
