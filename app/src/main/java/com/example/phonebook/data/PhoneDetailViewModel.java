package com.example.phonebook.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.FileUtils;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.phonebook.models.PhoneDto;
import com.example.phonebook.utils.Constants;
import com.example.phonebook.utils.GlobalApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhoneDetailViewModel extends BaseViewModel {
    private MutableLiveData<PhoneDto> phoneDto;
    private MutableLiveData<String> status;
    private MutableLiveData<String> title;
    private MutableLiveData<String> leftButton;
    private MutableLiveData<String> rightButton;
    private MutableLiveData<String> orignName;

    public PhoneDetailViewModel() {
        super();
    }
    public MutableLiveData<PhoneDto> getPhoneDto() {
        if (phoneDto == null) {
            phoneDto = new MutableLiveData<>();
        }
        return phoneDto;
    }

    public MutableLiveData<String> getStatus() {
        if (status == null) {
            status = new MutableLiveData<>();
        }
        return status;
    }

    public MutableLiveData<String> getTitle() {
        if (title == null) {
            title = new MutableLiveData<>();
        }
        return title;
    }

    public MutableLiveData<String> getLeftButton() {
        if (leftButton == null) {
            leftButton = new MutableLiveData<>();
        }
        return leftButton;
    }

    public MutableLiveData<String> getRightButton() {
        if (rightButton == null) {
            rightButton = new MutableLiveData<>();
        }
        return rightButton;
    }

    public MutableLiveData<String> getOrignName() {
        if (orignName == null) {
            orignName = new MutableLiveData<>();
        }
        return orignName;
    }

    public MultipartBody.Part getImageFileFromBitmap(Bitmap bitmap) {
        MultipartBody.Part imageFile;
        if (bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] imageInByte = stream.toByteArray();

            Date date = new Date();
            String fileName = Constants.FORMATTER.format(date) + ".JPEG";
            File file = new File(GlobalApplication.getAppContext().getCacheDir(), fileName);
            try {
                file.createNewFile();

                FileOutputStream fos = null;
                fos = new FileOutputStream(file);
                fos.write(imageInByte);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
                message.setValue("解析图片错误...");
                return null;
            }
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            imageFile = MultipartBody.Part.createFormData("image", file.getName(), reqFile);
        } else {
            imageFile = null;
        }
        return imageFile;
    }

    public void uploadPhone(final String newName, final String newPhone, Bitmap bitmap) {
        MultipartBody.Part imageFile = getImageFileFromBitmap(bitmap);
        RequestBody name = RequestBody.create(MediaType.parse("multipart/form-data"), newName);
        RequestBody phone = RequestBody.create(MediaType.parse("multipart/form-data"), newPhone);
        Call<ResponseBody> model = service.uploadPhone(name, phone, imageFile);
        model.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    Log.i("API_UPLOAD", json.toString());
                    String status = json.getString("status");
                    String resultMessage = json.getString("message");
                    String newImageUrl = json.getString("image");
                    if (status.equals("success")) {
                        phoneDto.postValue(new PhoneDto(newName, newPhone, newImageUrl, null));
                        message.setValue("上传成功");
                    } else {
                        message.setValue("上传失败...: " + resultMessage);
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
                t.printStackTrace();
                message.setValue("上传失败...");
            }
        });
    }

    public void updatePhone(final String newName, final String newPhone, Bitmap bitmap) {
        MultipartBody.Part imageFile = getImageFileFromBitmap(bitmap);
        RequestBody oldName = RequestBody.create(MediaType.parse("multipart/form-data"), orignName.getValue());
        RequestBody name = RequestBody.create(MediaType.parse("multipart/form-data"), newName);
        RequestBody phone = RequestBody.create(MediaType.parse("multipart/form-data"), newPhone);
        Call<ResponseBody> model = service.updatePhone(oldName, name, phone, imageFile);
        model.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    Log.i("API_UPDATE", json.toString());
                    String status = json.getString("status");
                    String resultMessage = json.getString("message");
                    String newImageUrl = json.getString("image");
                    if (status.equals("success")) {
                        phoneDto.postValue(new PhoneDto(newName, newPhone, newImageUrl, null));
                        message.setValue("更新成功");
                    } else {
                        message.setValue("更新失败...: " + resultMessage);
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
                t.printStackTrace();
                message.setValue("更新失败...");
            }
        });
    }
}
