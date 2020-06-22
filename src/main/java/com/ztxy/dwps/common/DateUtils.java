package com.ztxy.dwps.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class DateUtils {
	
	/* 
     * 将时间转换为时间戳
     */    
    public static String dateToStamp(String s) throws Exception{
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        res = String.valueOf(ts);
        return res;
    }
    
    /* 
     * 将时间戳转换为时间
     */
    public static String stampToDate(String s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = new Long(s);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }
    
    /**
     * 获取当前时间的时间戳
     * @return
     */
    public static int intTimeStamp() {
    	return Integer.valueOf(String.valueOf(new Date().getTime()/1000));
    }
    
    /**
     * 获取一天最晚时间点的时间戳
     * @return
     */
    public static int latestTime(){
    	Calendar calendar=Calendar.getInstance();
    	calendar.set(Calendar.HOUR_OF_DAY, 23);
    	calendar.set(Calendar.MINUTE, 59);
    	calendar.set(Calendar.SECOND, 59);
    	Date date=calendar.getTime();
    	return Integer.valueOf(String.valueOf(date.getTime()/1000));
    }
    
    /**
     * 获取一天最早时间点的时间戳
     * @return
     */
    public static int earliestTime(){
    	Calendar calendar=Calendar.getInstance();
    	calendar.set(Calendar.HOUR_OF_DAY, 0);
    	calendar.set(Calendar.MINUTE, 0);
    	calendar.set(Calendar.SECOND, 0);
    	Date date=calendar.getTime();
    	return Integer.valueOf(String.valueOf(date.getTime()/1000));
    }
    
    /**
     * 得到当前时间戳
     * @return
     */
    public static Long getNowTimezone(){
    	return System.currentTimeMillis();
    }
    
    /**
     * 获取有效期时间戳
     * @param day
     * @return
     */
    public static int validatetime(int day) {
    	Date date= new Date();
    	Calendar calendar =Calendar.getInstance();
    	calendar.setTime(date);
    	calendar.add(Calendar.DAY_OF_MONTH, day);
    	calendar.set(Calendar.HOUR_OF_DAY, 23);
    	calendar.set(Calendar.MINUTE, 59);
    	calendar.set(Calendar.SECOND, 59);
    	date=calendar.getTime();
    	return Integer.valueOf(String.valueOf(date.getTime()/1000));
    }
    
    /**
     * 获取有效期时间戳
     * @param day
     * @return
     */
    public static int validatetime(long createTime,int day) {
    	Date date=new Date(createTime);
    	Calendar calendar =Calendar.getInstance();
    	calendar.setTime(date);
    	calendar.add(Calendar.DAY_OF_MONTH, day);
    	calendar.set(Calendar.HOUR_OF_DAY, 23);
    	calendar.set(Calendar.MINUTE, 59);
    	calendar.set(Calendar.SECOND, 59);
    	date=calendar.getTime();
    	return Integer.valueOf(String.valueOf(date.getTime()/1000));
    }
    
    /**
     * 获取有效期时间戳
     * @param month
     * @return
     */
    public static int getDatetimezoneByMonth(int month) {
    	Date date= new Date();
    	Calendar calendar =Calendar.getInstance();
    	calendar.setTime(date);
    	calendar.add(Calendar.MONTH, month);
    	date=calendar.getTime();
    	return Integer.valueOf(String.valueOf(date.getTime()/1000));
    }
    
    
    /**
     * 获取有效期时间戳
     * @param month
     * @return
     */
    public static int getDatetimezoneByDay(int days) {
    	Date date= new Date();
    	Calendar calendar =Calendar.getInstance();
    	calendar.setTime(date);
    	calendar.add(Calendar.DAY_OF_MONTH, days);
    	date=calendar.getTime();
    	return Integer.valueOf(String.valueOf(date.getTime()/1000));
    }
    
    /**
     * 
     * @param time1 -1==>time1 < time2 0==>time1=time2 1==>time1>time2
     * @param time2
     * @param pattern
     * @return
     * @throws ParseException
     */
    public static int compare(String time1,String time2,String pattern) throws ParseException{  
        //如果想比较日期则写成"yyyy-MM-dd"就可以了  
        SimpleDateFormat sdf=new SimpleDateFormat(pattern);  
        //将字符串形式的时间转化为Date类型的时间  
        Date a=sdf.parse(time1);  
        Date b=sdf.parse(time2);  
        //Date类的一个方法，如果a早于b返回true，否则返回false  
        //System.out.println(a.compareTo(b));
        return a.compareTo(b); 
    }
    
    
    //unix时间戳转日期时间
    public static String TimeStamp2Date(Long timestamp){
        return TimeStamp2Date(timestamp,"yyyy-MM-dd HH:mm:ss");
    }

    public static String TimeStamp2Date(Long timestamp, String formats) {
        //formats = "yyyy-MM-dd HH:mm:ss";
        Long t = timestamp * 1000;
        String date = new SimpleDateFormat(formats, Locale.CHINA).format(new Date(t));
        return date;
    }
    
    public static void main(String[] args) throws ParseException {
    	//SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmm");
    	//System.out.println(sdf.format(new Date()));
    	//System.out.println(time());
    	//System.out.println(validatetime(time(),1));
    	System.out.println(System.currentTimeMillis());
    	System.out.println(validatetime(1541065521000l,1));
	}
  
    
}
