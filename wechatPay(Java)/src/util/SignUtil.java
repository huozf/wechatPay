package util;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class SignUtil {
	
	/**
	 * 根据packageParams和API_KEY生成签名
	 * @param packageParams
	 * @param API_KEY (API密钥)
	 * @return
	 */
	public static String createSign(SortedMap<Object, Object> packageParams, String API_KEY){
		 StringBuffer sb = new StringBuffer();
	        Set es = packageParams.entrySet();
	        Iterator it = es.iterator();
	        while (it.hasNext()) {
	            Map.Entry entry = (Map.Entry) it.next();
	            String k = (String) entry.getKey();
	            String v = (String) entry.getValue();
	            if (null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
	                sb.append(k + "=" + v + "&");
	            }
	        }
	        sb.append("key=" + API_KEY);
	        String sign = MD5Util.MD5Encode(sb.toString(), "UTF-8").toUpperCase();
	        return sign;
	}
	
	
	
	 /**
     * 是否签名正确,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
     * @return boolean
     */
    public static boolean isTenpaySign( SortedMap<Object, Object> packageParams, String API_KEY) {
        StringBuffer sb = new StringBuffer();
        Set es = packageParams.entrySet();
        Iterator it = es.iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String k = (String)entry.getKey();
            String v = (String)entry.getValue();
            if(!"sign".equals(k) && null != v && !"".equals(v)) {
                sb.append(k + "=" + v + "&");
            }
        }

        sb.append("key=" + API_KEY);

        //算出摘要
        String mysign = MD5Util.MD5Encode(sb.toString(), "UTF-8").toLowerCase();
        String tenpaySign = ((String)packageParams.get("sign")).toLowerCase();

        return tenpaySign.equals(mysign);
    }

}
