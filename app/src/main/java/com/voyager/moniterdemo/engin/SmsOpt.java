package com.voyager.moniterdemo.engin;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.voyager.moniterdemo.entities.SmsEntity;
import com.voyager.moniterdemo.interfaces.Operator;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取短信操作类
 * Created by wuhaojie on 2015/11/9.
 */
public class SmsOpt implements Operator {

    private static final String TAG = "SmsOpt";
    /**
     * 上下文
     */
    private final Context mContext;
    /**
     * 短信收件箱数据库 URI
     */
    private final String SMS_INBOX_URI = "content://sms/inbox";

    public SmsOpt(Context context) {
        this.mContext = context;
    }

    @Override
    public void start() {
        List<SmsEntity> list = getSmsEntities();
        saveToFile(list);
    }

    /**
     * 获取短信列表
     *
     * @return 返回列表
     */
    @NonNull
    private List<SmsEntity> getSmsEntities() {
        Log.e(TAG, String.valueOf((mContext == null)));
        Uri smsUri = Uri.parse(SMS_INBOX_URI);
        ContentResolver resolver = mContext.getContentResolver();
        String[] projection = {"_id", "address", "person", "body", "date", "type"};
        Cursor cursor = resolver.query(smsUri, projection, null, null, "date desc");
        int i = 1;
        SmsEntity sms = null;
        List<SmsEntity> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            sms = new SmsEntity();
            sms.setId(i++);
            sms.setBody(cursor.getString(3));
            sms.setDate(cursor.getLong(4));
            sms.setType(cursor.getInt(5));
            sms.setName(cursor.getString(2));
            sms.setPerson(cursor.getLong(1));
            Log.i(TAG, sms.toString());
            list.add(sms);
        }
        return list;
    }

    /**
     * 将获取到的短信数据保存到文件
     *
     * @param list 短信列表
     */
    private boolean saveToFile(List<SmsEntity> list) {


        return false;
    }
}
