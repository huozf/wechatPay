package util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class RandomUtil {

	/**
	 * 主要用于生成随机字符(nonce_str)
	 * @param length
	 * @return
	 */
	public static String getRandomString(int length){  
		Random random = new Random();  

		StringBuffer sb = new StringBuffer();  

		for(int i = 0; i < length; i++){  
			int number = random.nextInt(3);  
			long result = 0;  
			switch(number){  
			case 0:  
				result = Math.round(Math.random() * 25 + 65);  
				sb.append(String.valueOf((char)result));  
				break;  
			case 1:  
				result = Math.round(Math.random() * 25 + 97);  
				sb.append(String.valueOf((char)result));  
				break;  
			case 2:  
				sb.append(String.valueOf(new Random().nextInt(10)));  
				break;  
			}  
		}  

		return sb.toString();     
	}  

	
	/**
	 * 获取当前时间
	 * @return
	 */
	 public static String getCurrTime() {
		Date now = new Date();
		SimpleDateFormat outFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String s = outFormat.format(now);
		return s;
	}
	 
	 
	 /**
	  * 用于生成订单号
	  * 使用 (随机字符串+当前时间) 拼接而成的字符串，作为订单号，
	  * 哪怕是同一时间生成的订单，订单号也不会相同。
	  * @return
	  */
	 public static String createOrderId(int length){
		String out_trade_no = getRandomString(length)+getCurrTime();
		return out_trade_no;
	 }
	 

}
