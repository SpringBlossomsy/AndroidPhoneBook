package com.example.phonebook.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;

import com.example.phonebook.models.PhoneDto;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PhoneUtil {
    // 号码
    public final static String NUM = ContactsContract.CommonDataKinds.Phone.NUMBER;
    // 联系人姓名
    public final static String NAME = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
    public final static String RAW_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID;
    public final static String PHOTO_ID = ContactsContract.CommonDataKinds.Phone.PHOTO_ID;

    //上下文对象
    private Context context;
    //联系人提供者的uri
    private Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

    public PhoneUtil(Context context){
        this.context = context;
    }

    //获取所有联系人
    public List<PhoneDto> getPhone(){
        List<PhoneDto> phoneDtos = new ArrayList<>();
        try {
            ContentResolver cr = context.getContentResolver();
            Cursor cursor = cr.query(phoneUri,new String[]{NUM,NAME,RAW_CONTACT_ID,PHOTO_ID},null,null,null);
            while (cursor.moveToNext()){
                PhoneDto phoneDto = new PhoneDto(cursor.getString(cursor.getColumnIndex(NAME)),cursor.getString(cursor.getColumnIndex(NUM)));
                phoneDto.setRawContactsId(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID)));
                phoneDto.setPhotoId(cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_ID)));
                phoneDtos.add(phoneDto);
            }
        } catch (Exception e) {
            e.printStackTrace();
            phoneDtos = new ArrayList<>();
        }

        return phoneDtos;
    }

    public boolean insert(String given_name, String mobile_number, Bitmap bmp) {
        try {
            ContentValues values = new ContentValues();

            // 下面的操作会根据RawContacts表中已有的rawContactId使用情况自动生成新联系人的rawContactId
            Uri rawContactUri = context.getContentResolver().insert(
                    ContactsContract.RawContacts.CONTENT_URI, values);
            long rawContactId = ContentUris.parseId(rawContactUri);

            // 向data表插入姓名数据
            if (given_name != "") {
                values.clear();
                values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
                values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, given_name);
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI,values);
            }

            // 向data表插入电话数据
            if (mobile_number != "") {
                values.clear();
                values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, mobile_number);
                values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI,values);
            }

            // 向data表插入头像数据
            if (bmp != null) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
                byte[] avatar = os.toByteArray();
                values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
                values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, avatar);
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI,values);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean update(PhoneDto localPhone, PhoneDto phoneDto)
    {
        try {
            ContentValues values = new ContentValues();
            String rawContactId = localPhone.getRawContactsId();

            // update phone
            values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneDto.getTelPhone());
            context.getContentResolver().update(
                    ContactsContract.Data.CONTENT_URI, values, ContactsContract.Data.MIMETYPE + "=? and raw_contact_id= ?" ,
                    new String[] { ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, rawContactId});
            values.clear();

            // update image
            if (phoneDto.getImageBmp() != null) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                phoneDto.getImageBmp().compress(Bitmap.CompressFormat.PNG, 100, os);
                byte[] avatar = os.toByteArray();
                if (localPhone.getPhotoId() > 0) {
                    values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, avatar);
                    context.getContentResolver().update(ContactsContract.Data.CONTENT_URI, values,
                            ContactsContract.Data.MIMETYPE + "=? AND " + ContactsContract.Data.RAW_CONTACT_ID + "= " + rawContactId,
                            new String[] { ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE });
                } else {
                    // 插入,保存联系人头像
                    values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
                    values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
                    values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, avatar);
                    context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
                }
                values.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean delete(String rawContactId) {
        try {
            context.getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI, ContactsContract.Data._ID + " = "+rawContactId, null);
            context.getContentResolver().delete(ContactsContract.Data.CONTENT_URI, "raw_contact_id="+rawContactId, null);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
