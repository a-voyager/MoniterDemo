package com.voyager.moniterdemo.engin;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Xml;

import com.voyager.moniterdemo.constant.Constants;
import com.voyager.moniterdemo.entities.SmsEntity;
import com.voyager.moniterdemo.interfaces.Operator;
import com.voyager.moniterdemo.utils.EmojiFilter;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    /**
     * 判断是否正在进行读取短信操作
     */
    private static boolean isRunning = false;

    public SmsOpt(Context context) {
        this.mContext = context;
    }

    @Override
    public void start() {
        Log.i(TAG, "start() 开始备份短信操作");
        isRunning = true;
        List<SmsEntity> list = getSmsEntities();
        saveToFile(list);
        isRunning = false;
    }

    /**
     * 获取短信列表
     *
     * @return 返回列表
     */
    @NonNull
    private List<SmsEntity> getSmsEntities() {
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
//            sms.setBody(cursor.getString(3));
            sms.setBody(EmojiFilter.filterEmoji(cursor.getString(3)));
            sms.setDate(cursor.getLong(4));
            sms.setType(cursor.getInt(5));
            sms.setName(cursor.getString(2));
            sms.setPerson(cursor.getLong(1));
//            Log.i(TAG, sms.toString());
            list.add(sms);
        }
        cursor.close();
        return list;
    }

    /**
     * 将获取到的短信数据保存到文件
     *
     * @param list 短信列表
     */
    private boolean saveToFile(List<SmsEntity> list) {
        FileOutputStream fos = null;
        XmlSerializer xmlSerializer = Xml.newSerializer();
        File fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.SMS_SAVE_FILE_DIR);
        if (!fileDir.exists()) fileDir.mkdirs();
        File file = new File(fileDir, Constants.SMS_SAVE_FILE);
        try {
            fos = new FileOutputStream(file);
            xmlSerializer.setOutput(fos, "utf-8");
            //开始解析
            xmlSerializer.startDocument("utf-8", true);
            xmlSerializer.startTag(null, "SMSs");
            xmlSerializer.attribute(null, "num", String.valueOf(list.size()));
            for (SmsEntity sms : list) {
                xmlSerializer.startTag(null, "sms");
                xmlSerializer.attribute(null, "id", String.valueOf(sms.getId()));
                xmlSerializer.startTag(null, "type");
                xmlSerializer.text(String.valueOf(sms.getType()));
                xmlSerializer.endTag(null, "type");
                xmlSerializer.startTag(null, "name");
                xmlSerializer.text(sms.getName() == null ? "" : sms.getName());
                xmlSerializer.endTag(null, "name");
                xmlSerializer.startTag(null, "person");
                xmlSerializer.text(String.valueOf(sms.getPerson()));
                xmlSerializer.endTag(null, "person");
                xmlSerializer.startTag(null, "date");
                xmlSerializer.text(String.valueOf(sms.getDate()));
                xmlSerializer.endTag(null, "date");
                xmlSerializer.startTag(null, "body");
                Log.d(TAG, "sms body ----- " + sms.getBody());
                xmlSerializer.text(sms.getBody());
                xmlSerializer.endTag(null, "body");
                xmlSerializer.endTag(null, "sms");
            }
            xmlSerializer.endTag(null, "SMSs");
            xmlSerializer.endDocument();
        } catch (Exception e) {
            Log.e(TAG, "saveToFile() failed", e);
            return false;
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                Log.e(TAG, "fos close failed");
            }
        }
        return true;
    }

    public boolean isRunning() {
        return isRunning;
    }

}
