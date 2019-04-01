package action;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.PayConfig;
import util.PayUtil;
import util.RandomUtil;
import util.SignUtil;
import util.XMLUtil;
import util.ZxingUtil;


@WebServlet("/payment/*")
public class PayServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		
		String requestURI=request.getRequestURI();//获取请求路径
		int start = requestURI.lastIndexOf("/");
		String reqStr=requestURI.substring(start + 1);
		
		if("pay".equals(reqStr)){
			doPay(request,response);
		}else if("image".equals(reqStr)){
			doImage(request,response);
		}else if("result".equals(reqStr)){
			try {
				wxNotify(request,response);
			} catch (Exception e) {
				System.err.println("通知出错了:"+e);
			}
		}
		
	}


	private void doPay(HttpServletRequest request, HttpServletResponse response) {
		
		String body="支付测试";  //商品描述
		String total_fee="1"; //商品价格,默认单位为“分”
		String out_trade_no=RandomUtil.createOrderId(4); //商品订单号
		try {
			//获取二维码字符串
			String result=PayUtil.getPayURL( body, out_trade_no , total_fee );
			//将字符串转换为二维码
			BufferedImage image = ZxingUtil.createImage(result, 300, 300);
			
			//将二维码放到session里
			request.getSession().setAttribute("image", image);
			//将订单号放到session里
			request.getSession().setAttribute("out_trade_no", out_trade_no);
			
			//跳转页面
			response.sendRedirect("../ercode.jsp");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void doImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
		System.out.println("进来了...");
		 BufferedImage image = (BufferedImage) request.getSession().getAttribute("image");
	        if (image != null) {
	            ImageIO.write(image,"JPEG",response.getOutputStream());
	        }
	}
	
	
	 /**
     * 解析微信返回的支付结果
     * @param request
     * @param response
     * @throws Exception
     */
    public void wxNotify(HttpServletRequest request,HttpServletResponse response) throws Exception{
        //读取参数
        InputStream inputStream ;
        StringBuffer sb = new StringBuffer();
        inputStream = request.getInputStream();
        String s ;
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        while ((s = in.readLine()) != null){
            sb.append(s);
        }
        in.close();
        inputStream.close();

        //解析xml成map
        Map<String, String> m = new HashMap<String, String>();
        m = XMLUtil.doXMLParse(sb.toString());

        //过滤空 设置 TreeMap
        SortedMap<Object,Object> packageParams = new TreeMap<Object,Object>();
        Iterator it = m.keySet().iterator();
        while (it.hasNext()) {
            String parameter = (String) it.next();
            String parameterValue = m.get(parameter);

            String v = "";
            if(null != parameterValue) {
                v = parameterValue.trim();
            }
            packageParams.put(parameter, v);
        }

        // 账号信息
        String key = PayConfig.API_KEY; // key

        System.err.println(packageParams);
        String out_trade_no = (String)packageParams.get("out_trade_no");//订单号,实际开发中应该在下面的 IF 中,除非需要对每个订单的每次支付结果做记录
        //判断签名是否正确
        if(SignUtil.isTenpaySign( packageParams,key)) {
            //------------------------------
            //处理业务开始
            //------------------------------
            String resXml = "";
            
            if( "SUCCESS".equals( (String)packageParams.get("result_code") )){
                // 这里是支付成功
                //////////执行自己的业务逻辑////////////////
                String mch_id = (String)packageParams.get("mch_id");
                String openid = (String)packageParams.get("openid");
                String is_subscribe = (String)packageParams.get("is_subscribe");
                // String out_trade_no = (String)packageParams.get("out_trade_no");

                String total_fee = (String)packageParams.get("total_fee");

                System.err.println("mch_id:"+mch_id);
                System.err.println("openid:"+openid);
                System.err.println("is_subscribe:"+is_subscribe);
                System.err.println("out_trade_no:"+out_trade_no);
                System.err.println("total_fee:"+total_fee);

                //////////执行自己的业务逻辑////////////////

                System.err.println("支付成功");
                //通知微信.异步确认成功.必写.不然会一直通知后台.八次之后就认为交易失败了.
                resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
                        + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
                
                WebSocket.sendMessage(out_trade_no,"支付成功");
                

            } else {
                System.err.println("订单:"+out_trade_no+"支付失败;错误信息:" + packageParams.get("err_code"));
                resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
                        + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
            }
            //------------------------------
            //处理业务完毕
            //------------------------------
            BufferedOutputStream out = new BufferedOutputStream( response.getOutputStream() );
            out.write(resXml.getBytes());
            out.flush();
            out.close();
        } else{
            System.err.println("通知签名验证失败");
        }
    }


	

}
