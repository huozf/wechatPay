package util;

/**
 * 这里定义为接口，里面的变量就默认为public static
 * @author huozf
 */
public interface PayConfig {

	String APP_ID = "wx6*****8122c6"; //微信公众号ID
	String MCH_ID = "149***412";         //商户号ID
	String API_KEY = "sbNCm1Jnev*****caT0hkGxFnC"; //APK密钥
	String UFDOOER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder"; //回调地址
	String NOTIFY_URL = "http://******/wechatPay/payment/result"; //通知地址，可使用内网穿透工具进行测试
	String CREATE_IP = "110.***.***.9";  //我的IP

}
