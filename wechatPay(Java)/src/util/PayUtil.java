package util;

import java.util.*;

public class PayUtil {

    /**
     * 统一下单,获取二维码字符串
     * @param order_price 价格
     * @param body 商品描述
     * @param out_trade_no 订单号
     * @return
     * @throws Exception
     */
    public static String getPayURL( String body,String out_trade_no,String total_fee) throws Exception {
        // 账号信息
        String appid = PayConfig.APP_ID;  //appid
        String mch_id = PayConfig.MCH_ID; //商业号
        String key = PayConfig.API_KEY;   //API密钥
        
        String nonce_str = RandomUtil.getRandomString(20); //随即字符串，官方文档要求长度在32位以内

        String spbill_create_ip = PayConfig.CREATE_IP;  //获取发起电脑 ip
       
        String notify_url = PayConfig.NOTIFY_URL;  //回调接口
        
        String trade_type = "NATIVE";  //支付类型

        SortedMap<Object,Object> packageParams = new TreeMap<Object,Object>();
        packageParams.put("appid", appid);
        packageParams.put("mch_id", mch_id);
        packageParams.put("nonce_str", nonce_str);
        packageParams.put("body", body);
        packageParams.put("out_trade_no", out_trade_no);
        packageParams.put("total_fee", total_fee);
        packageParams.put("spbill_create_ip", spbill_create_ip);
        packageParams.put("notify_url", notify_url);
        packageParams.put("trade_type", trade_type);

        String sign = SignUtil.createSign(packageParams,key);
        packageParams.put("sign", sign);

        String requestXML = XMLUtil.getRequestXml(packageParams);
        System.out.println(requestXML);
        
        System.out.println("--------------------");
        
        String resXml = HttpUtil.postData(PayConfig.UFDOOER_URL, requestXML);

        System.out.println(resXml);
        
        Map map = XMLUtil.doXMLParse(resXml);
       
        String urlCode = (String) map.get("code_url");

        return urlCode;
    }
    
    
}