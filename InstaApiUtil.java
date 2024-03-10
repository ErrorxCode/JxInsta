package com.studio1221.instatest.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;

import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by josong on 2017-09-07.
 */

public class InstaApiUtil {
    final static public int IMG_ORIGINAL = 1;
    final static public int IMG_THUMB_150 = 150;
    final static public int IMG_THUMB_320 = 320;
    final static public int IMG_THUMB_640 = 640;

//    public final static String getUserAgent(Context context){
//        return new WebView(context).getSettings().getUserAgentString();
//    }

    private static String userAgent  = "";
    public static void setUserAgent(Context context){
        userAgent = InstaApiUtil.getUserAgent(context);
    }

    public final static String getUserAgent() {
        return userAgent;
    }

    public static final String getRandomUUID(boolean withHypen){
        String str = UUID.randomUUID().toString();
        if (withHypen) {
            return str;
        }else{
            str = str.replaceAll("-", "");
            return str;
        }
    }

    public final static String getMediaCodeFromMediaId(String id) {
        String[] parts = id.split("_");
        id = parts[0];
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
        StringBuilder code = new StringBuilder();
        long longId = Long.parseLong(id);
        while (longId > 0) {
            long index = longId % 64;
            longId = (longId - index) / 64;
            code.insert(0, alphabet.charAt((int) index));
        }
        return code.toString();
    }


    public final static String getThumbImgUrl(int imgThumbType, String imgPath){
        //https://scontent-sit4-1.cdninstagram.com/t51.2885-19/s150x150/19050967_303276673455041_8306845862932250624_a.jpg

        String[] arrImgPath = imgPath.split("/");
        switch(imgThumbType){
            case IMG_ORIGINAL:
                return arrImgPath[0] + "//" + arrImgPath[2] + "/" + arrImgPath[3] + "/" + arrImgPath[arrImgPath.length-1];
            case IMG_THUMB_150:
                return arrImgPath[0] + "//" + arrImgPath[2] + "/" + arrImgPath[3] + "/s150x150/" + arrImgPath[arrImgPath.length-1];
            case IMG_THUMB_320:
                return arrImgPath[0] + "//" + arrImgPath[2] + "/" + arrImgPath[3] + "/s320x320/" + arrImgPath[arrImgPath.length-1];
            case IMG_THUMB_640:
                return arrImgPath[0] + "//" + arrImgPath[2] + "/" + arrImgPath[3] + "/s640x640/" + arrImgPath[arrImgPath.length-1];
            default:
                return imgPath;
        }
    }

    public static final void removeAllInfo(){

    }

    public static final String makeParamForInstagram(Map<String, String> mapParam){
        String param = makeEncodedParamsForInsta(mapParam);
        param = "ig_sig_key_version=4&signed_body=" + InstaApiUtil.encodeUTF8(param);
        return param;
    }

    public static final String makeParamForInstagram(Object ... keyValues){
        String param = makeEncodedParamsForInsta(keyValues);
        param = "ig_sig_key_version=4&signed_body=" + InstaApiUtil.encodeUTF8(param);
        return param;
    }

    public static final String makeParamForInstagram(String jsonParam){
        String param = makeEncodedParamsForInstaFromJsonString(jsonParam);
        param = "ig_sig_key_version=4&signed_body=" + InstaApiUtil.encodeUTF8(param);
        return param;
    }

    public static final String makeEncodedParamsForInsta(Object ... keyValues){
        String params = "";
        TreeMap localTreeMap = new TreeMap();
        for(int i = 0 ; i < keyValues.length; i+=2){
            String key = keyValues[i].toString();
            Object value = keyValues[i+1];
            localTreeMap.put(key, value);
        }

        params = InstaApiUtil.getBody(new Gson().toJson(localTreeMap));
        return params;
    }

    public static final String makeEncodedParamsForInsta(Map<String, String> mapParam){
        String params = InstaApiUtil.getBody(new Gson().toJson(mapParam));
        return params;
    }

    public static final String makeEncodedParamsForInstaFromJsonString(String jsonParam){
        return InstaApiUtil.getBody(jsonParam);
    }

    public static final String getDeviceId(boolean newDeviceId){

        String deviceId = "";
        final String[] charSets = new String[]{"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"
                ,"1","2","3","4","5","6","7","8","9"};
        String virDeviceId = "";
        for(int i = 0; i < 15; i ++){
            virDeviceId = virDeviceId + charSets[new Random().nextInt(charSets.length)];
        }

        deviceId = "android-" + virDeviceId;
        return deviceId;
    }

    public static final String getDeviceId(Context context){
        return "android-" + Settings.Secure.getString(context.getContentResolver(), "android_id");
    }

    public static String getPhoneId(boolean paramBoolean)
    {
        String str = UUID.randomUUID().toString();
        if (paramBoolean) {
            return str;
        }
        return str.replaceAll("-", "");
    }

    public static final String getUserAgent(Context context){
//        return "Instagram 7.16.0 Android (18/4.3; 320dpi; 720x1280; " + Build.MANUFACTURER + "; " + Build.MODEL + "; armani; qcom; en_US)";

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int i = (int)(metrics.density * 160.0F);
        int j = metrics.heightPixels;
        int k = metrics.widthPixels;
        return String.format("%s Android (%s/%s; %s; %s; %s; %s; %s; %s; %s_%s)", new Object[] { "Instagram 10.15.0", Build.VERSION.RELEASE, Build.VERSION.SDK, i + "dpi", j + "x" + k, Build.MODEL, Build.MANUFACTURER, Build.BRAND, Build.DEVICE, Locale.getDefault().getLanguage(), Locale.getDefault().getCountry() });
    }

    public static final String getUserAgentFromWeb(Context context){
//        return "Instagram 7.16.0 Android (18/4.3; 320dpi; 720x1280; " + Build.MANUFACTURER + "; " + Build.MODEL + "; armani; qcom; en_US)";
        return new WebView(context).getSettings().getUserAgentString();
    }

    public static String getBody(String paramJsonString){
        //b03e0daaf2ab17cda2a569cace938d639d1288a1197f9ecf97efd0a4ec0874d7
        //6a5048da38cd138aacdcd6fb59fa8735f4f39a6380a8e7c10e13c075514ee027
        return secret(paramJsonString, "b03e0daaf2ab17cda2a569cace938d639d1288a1197f9ecf97efd0a4ec0874d7") + "." + paramJsonString;
    }

    public static String secret(String paramJsonString, String key)
    {
        SecretKeySpec scc = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        try
        {
            Mac localMac = Mac.getInstance(scc.getAlgorithm());
            localMac.init(scc);
            paramJsonString = getFormattedStringFromBytes(localMac.doFinal(paramJsonString.getBytes()));
            return paramJsonString;
        }
        catch (NoSuchAlgorithmException e){
            e.printStackTrace();
            return null;
        }
        catch (InvalidKeyException e){
            e.printStackTrace();
            return null;
        }
    }

    public static String getFormattedStringFromBytes(byte[] paramArrayOfByte)
    {
        StringBuilder localStringBuilder = new StringBuilder(paramArrayOfByte.length * 2);
        Formatter localFormatter = new Formatter(localStringBuilder);

        int j = paramArrayOfByte.length;
        int i = 0;
        while (i < j)
        {
            localFormatter.format("%02x", new Object[] { Byte.valueOf(paramArrayOfByte[i]) });
            i += 1;
        }
        return localStringBuilder.toString();
    }

    public final static void openEmailIntent(Context context, String email, String subject, String text){
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"+email));
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(i);
    }

    public final static void openWebIntent(Context context, String url){
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(i);
    }

    public static void openPhoneDialIntent(Context context, String phone){
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
        context.startActivity(intent);
    }


    public static final void changeProgressBarColor(ProgressBar pb, int color){
        pb.getIndeterminateDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.MULTIPLY);

    }

    public static final String getPlainDateTime(String dateTime){
        if(dateTime.contains(".")){
            return dateTime.substring(0, 19);
        }else{
            return dateTime;
        }
    }

    public static Bundle makeBundle(Object... objs){

        Bundle bundle = new Bundle();
        for(int i = 0; i < objs.length; i+=2){
            String key = (String)objs[i];
            Object value = objs[i+1];
            if(value instanceof Integer){
                bundle.putInt(key, (Integer)value);
            }else if(value instanceof String){
                bundle.putString(key, (String)value);
            }else if(value instanceof Float){
                bundle.putFloat(key, (Float)value);
            }
        }

        return bundle;
    }

    public final static <T> T changeObjectType(Object obj){
        return (T)obj;
    }

    public final static String getMoneyString(String number){
        return getMoneyString(Long.parseLong(number));
    }

    public final static String getMoneyString(long number){
        NumberFormat numberFormat = NumberFormat.getInstance();
//        v
//        else if(Util.getLocaleCode(OkHomeActivityParent.lastContext).equals("en")){
//            numberFormat = NumberFormat.getInstance(new Locale("id"));
//        }else{
//            numberFormat = NumberFormat.getInstance();
//        }

        String v = numberFormat.format(number);
        return v;
    }

    public static Map<String, Object> makeMap(Object... objs){
        Map<String, Object> params = new HashMap<String, Object>();

        for(int i = 0; i < objs.length; i+=2){
            params.put((String)objs[i], objs[i+1]);
        }

        return params;
    }

    public static Map<String, String> makeStringMap(String... objs){
        Map<String, String> params = new HashMap<String, String>();

        for(int i = 0; i < objs.length; i+=2){
            params.put((String)objs[i], objs[i+1]);
        }

        return params;
    }


    public final static void sleep(long duration){
        try{
            Thread.sleep(duration);
        }catch(Exception e){
            ;
        }
    }

    public static int getScreenHeight(Activity activity){
        try{
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int height = metrics.heightPixels;
            int width = metrics.widthPixels;

            return height;
        }catch(Exception e){
            return 400;
        }
    }

    public static int getScreenWidth(Activity activity){
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        return width;
    }

    public final static void openInstagramMediaPage(Context context, String mediaId){
//        String shortenedId = InstaApiUtil.getMediaCodeFromMediaId(mediaId);
//
//        Intent iIntent = context.getPackageManager().getLaunchIntentForPackage("com.instagram.android");
//        if (isIntentAvailable(context, iIntent)){
//            iIntent.setComponent(new ComponentName( "com.instagram.android", "com.instagram.android.activity.UrlHandlerActivity"));
//            iIntent.setData( Uri.parse( "http://instagram.com/p/" + shortenedId +"/") );
//            context.startActivity(iIntent);
//        }else{
//            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/p/" + shortenedId + "/")));
//        }

        Uri uri = Uri.parse("http://instagram.com/p/" + InstaApiUtil.getMediaCodeFromMediaId(mediaId));
        Intent insta = new Intent(Intent.ACTION_VIEW, uri);


        if (isIntentAvailable(context, insta)){
            insta.setPackage("com.instagram.android");
            context.startActivity(insta);
        } else{
            context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }

    }

    public final static void openInstagramUserPage(Context context, String userName){
        Uri uri = Uri.parse("http://instagram.com/_u/" + userName);
        Intent insta = new Intent(Intent.ACTION_VIEW, uri);


        if (isIntentAvailable(context, insta)){
            insta.setPackage("com.instagram.android");
            context.startActivity(insta);
        } else{
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/" + userName)));
        }
    }

    public final static boolean isIntentAvailable(Context ctx, Intent intent) {
        try{
            final PackageManager packageManager = ctx.getPackageManager();
            List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
        }catch(Exception e){
            return false;
        }

    }

    //**이메일 정규식체크*/
    public final static boolean isValidEmail(String email) {
        boolean err = false;
        String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        if( m.matches() ) {
            err = true;
        }
        return err;
    }

    final public static Message makeHandlerMessage(int what, Object obj){
        Message m = new Message();
        m.obj = obj;
        m.what = what;
        return m;
    }

    final public static String encodeUTF8(String var){
        try{
            return URLEncoder.encode(var, "UTF-8");

        }catch(Exception e){
            return null;
        }
    }
    final public static String decodeUTF8(String var){
        try{
            return URLEncoder.encode(var, "UTF-8");

        }catch(Exception e){
            return null;
        }
    }

    /**토스트 띄우기*/
    public static final void showToast(Context context, String msg){
        if(context != null) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

}
