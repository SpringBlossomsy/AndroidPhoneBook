package com.example.phonebook.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.phonebook.R;
import com.example.phonebook.databinding.ActivityDetailBinding;
import com.example.phonebook.models.PhoneDto;
import com.example.phonebook.utils.Constants;

import com.example.phonebook.data.PhoneDetailViewModel;
import com.example.phonebook.utils.GlobalApplication;

public class DetailActivity extends AppCompatActivity {
    private PhoneDetailViewModel detailModel;
    private Uri portraitLocalUri;
    private ActivityDetailBinding binding;
    private int position;
    private Boolean firstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail);
        detailModel = ViewModelProviders.of(this).get(PhoneDetailViewModel.class);
        binding.setViewModel(detailModel);
        detailModel.getPhoneDto().setValue(new PhoneDto(
                getIntent().getStringExtra("NAME"),
                getIntent().getStringExtra("PHONE"),
                getIntent().getStringExtra("IMAGE"),
                null
        ));
        detailModel.getPhoneDto().observe(this, new Observer<PhoneDto>() {
            @Override
            public void onChanged(PhoneDto phoneDto) {
                Log.i("PhoneChange", phoneDto.toString());
                if (firstLoad == true) {
                    firstLoad = false;
                } else {
                    Intent intent = new Intent(GlobalApplication.getAppContext(), DetailActivity.class);;
                    intent.putExtra("STATUS", "UPDATE");
                    intent.putExtra("NAME", detailModel.getPhoneDto().getValue().getName());
                    intent.putExtra("PHONE", detailModel.getPhoneDto().getValue().getTelPhone());
                    intent.putExtra("IMAGE", detailModel.getPhoneDto().getValue().getImage());
                    intent.putExtra("POSITION", position);
                    setResult(RESULT_OK, intent);
                    DetailActivity.this.finish();
                }
            }
        });
        detailModel.getOrignName().setValue(getIntent().getStringExtra("NAME"));
        detailModel.getMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message != null) {
                    Toast.makeText(DetailActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        });
        detailModel.getTitle().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                binding.title.setText(s);
                if (s.equals(Constants.DETAIL.TITLE)) {
                    binding.name.setInputType(InputType.TYPE_NULL);
                    binding.phone.setInputType(InputType.TYPE_NULL);
                } else {
                    binding.name.setInputType(InputType.TYPE_CLASS_TEXT);
                    binding.phone.setInputType(InputType.TYPE_CLASS_TEXT);
                }
            }
        });
        detailModel.getLeftButton().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                binding.leftBtn.setText(s);
            }
        });
        detailModel.getRightButton().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                binding.rightBtn.setText(s);
            }
        });
        detailModel.getStatus().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String status) {
                if (detailModel.getPhoneDto().getValue().getImage() != null &&
                    detailModel.getPhoneDto().getValue().getImage() != "") {
                    Glide.with(DetailActivity.this)
                            .load(Constants.URL_DOMAIN + detailModel.getPhoneDto().getValue().getImage())
                            .placeholder(R.drawable.portrait)
                            .error(R.drawable.portrait) //error
                            .into(binding.portrait);
                }
                switch (status) {
                    case "ADD":
                        detailModel.getTitle().setValue(Constants.ADD.TITLE);
                        detailModel.getLeftButton().setValue(Constants.ADD.LEFT);
                        detailModel.getRightButton().setValue(Constants.ADD.RIGHT);
                        break;
                    case "UPDATE":
                        detailModel.getTitle().setValue(Constants.UPDATE.TITLE);
                        detailModel.getLeftButton().setValue(Constants.UPDATE.LEFT);
                        detailModel.getRightButton().setValue(Constants.UPDATE.RIGHT);
                        binding.uploadBtn.setVisibility(View.VISIBLE);
                        break;
                    case "DETAIL":
                        detailModel.getTitle().setValue(Constants.DETAIL.TITLE);
                        detailModel.getLeftButton().setValue(Constants.DETAIL.LEFT);
                        detailModel.getRightButton().setValue(Constants.DETAIL.RIGHT);
                        break;
                }
            }
        });

        detailModel.getStatus().setValue(getIntent().getStringExtra("STATUS"));
        position = getIntent().getIntExtra("POSITION", -1);
    }

    public void rightButtonMethod(View view) {
        String title = detailModel.getTitle().getValue();
        String newName = binding.name.getText().toString();
        String newPhone = binding.phone.getText().toString();

        Bitmap bitmap = null;
        if (portraitLocalUri != null) {
            BitmapDrawable bitmapDrawable = ((BitmapDrawable) binding.portrait.getDrawable());
            bitmap = bitmapDrawable.getBitmap();
        }

        switch (title) {
            case Constants.ADD.TITLE:
                Log.i("RIGHT_BTN", "Save Phone");
                detailModel.uploadPhone(newName, newPhone, bitmap);
                break;
            case Constants.UPDATE.TITLE:
                Log.i("RIGHT_BTN", "Update Phone");
                detailModel.updatePhone(newName, newPhone, bitmap);
                break;
            case Constants.DETAIL.TITLE:
                Log.i("RIGHT_BTN", "Change view");
                detailModel.getStatus().setValue("UPDATE");
                break;
        }
    }

    public void leftButtonMethod(View view) {
        DetailActivity.this.finish();
    }

    public void takePhoto(View view) {
        Intent getImage = new Intent(Intent.ACTION_PICK, null);
        getImage.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");//这是图片类型
        startActivityForResult(getImage, 2);
    }

    public void clickToCallPhone(View view) {
        EditText phone_edit = findViewById(R.id.phone);
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone_edit.getText().toString()));
        startActivity(intent);
    }

    public void clickToSendMessge(View view) {
        EditText phone_edit = findViewById(R.id.phone);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("smsto:" + phone_edit.getText().toString()));
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case 1:
                    break;
                case 2:
                    if (data != null) {
                        portraitLocalUri = data.getData();
//                        ImageView portrait = findViewById(R.id.portrait);
//                        portrait.setImageURI(portraitLocalUri);
                        Glide.with(DetailActivity.this)
                                .load(portraitLocalUri)
                                .placeholder(R.drawable.portrait)
                                .error(R.drawable.portrait) //error
                                .into(binding.portrait);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
