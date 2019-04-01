package action;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@ServerEndpoint("/websocket/{out_trade_no}")
public class WebSocket {

    private String out_trade_no;///记录订单号
    private Session session;//当前的连接

    private static Map<String, Session> allClients = new ConcurrentHashMap<>();//用于存放所有订单和 websocket 连接的 map


    public static Map<String, Session> getAllClients() {
        return allClients;
    }

    /**
     * 当建立连接的时候调用
     * @param id
     * @param session
     */
    @OnOpen
    public void onOpen(@PathParam("out_trade_no") String out_trade_no, Session session) {

        this.out_trade_no = out_trade_no;
        this.session = session;
        allClients.put(out_trade_no, session);

    }

    @OnClose
    public void onClose(Session session) {
        allClients.remove(out_trade_no);
    }

    @OnError
    public void onError(Session session,Throwable t) {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        allClients.remove(out_trade_no);
    }

    /**
     * 代表客户端给服务器发送消息了,包括 两个用户之间通信也是  比如 A->B 发消息,那么是 A先给服务器发消息,告诉服务器我发给谁,服务器找到 B 的连接再将内容转过去
     * @param session
     * @param content
     */
    @OnMessage
    public void onMessage(Session session, String content) {

    }

    /**
     * 发送消息
     * @param session
     * @param message
     */
    public static  void sendMessage(Session session, String message) {
        if (session != null) {
            session.getAsyncRemote().sendText(message);
        }
    }

    /**
     * 根据 id 发送消息
     * @param id
     * @param message
     */

    public static void sendMessage(String out_trade_no, String message) {
        if (out_trade_no != null) {
            Session session = allClients.get(out_trade_no);
            sendMessage(session,message);
        }
    }
    
}
