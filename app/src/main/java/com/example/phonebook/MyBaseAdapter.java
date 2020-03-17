package com.example.phonebook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.phonebook.databinding.MylistitemBinding;
import com.example.phonebook.models.PhoneDto;

import java.util.List;

public class MyBaseAdapter extends BaseAdapter {
    private List<PhoneDto> mPhoneDtoList;
    private View.OnClickListener mLinstener;

    public MyBaseAdapter() {
        super();
    }

    public MyBaseAdapter(List<PhoneDto> phoneDtoList) {
        mPhoneDtoList = phoneDtoList;
    }

    @Override
    public int getCount() {
        return mPhoneDtoList.size();
    }

    @Override
    public Object getItem(int position) {
        return mPhoneDtoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        MylistitemBinding binding = MylistitemBinding.inflate(inflater, parent, false);
        binding.setPhoneDto(mPhoneDtoList.get(position));

        return binding.getRoot();
    }
}
