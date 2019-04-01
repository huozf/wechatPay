from django.shortcuts import render

from pay.pay_util import *
from django.http import HttpResponse
from dwebsocket.decorators import accept_websocket,require_websocket
from collections import defaultdict
import threading
# Create your views here.


def index(request):
    return render(request,'index.html')


def pay(request):

    body = 'payTest'  # 商品描述
    total_fee = 1  # 付款金额，单位是分，必须是整数
    out_trade_no = create_orderId(5)

    data_dict = wxpay(body,total_fee,out_trade_no)  # 得到字典数据

    if data_dict.get('return_code') == 'SUCCESS':
       code_url = get_code_url(data_dict)
       img = create_image(code_url)
       qrcode_name = 'wxpay.png'
       img.save(r'static' + '/' + qrcode_name)
       return render(request, 'qrcode.html', {'qrcode_name': qrcode_name,'out_trade_no':out_trade_no})

    return render(request, 'index.html',{'err_msg': '获取微信的code_url失败'})




def result(request):
    data_dict = trans_xml_to_dict(request.body)  # 回调数据转字典
    # print('支付回调结果', data_dict)
    sign = data_dict.pop('sign')  # 取出签名
    back_sign = get_sign(data_dict, API_KEY)  # 计算签名
    # 验证签名是否与回调签名相同
    if sign == back_sign and data_dict['return_code'] == 'SUCCESS':

        '''
        检查对应业务数据的状态，判断该通知是否已经处理过，如果没有处理过再进行处理，如果处理过直接返回结果成功。
        '''
        print('微信支付成功会回调！')
        # 处理支付成功逻辑，向前端页面发送实时消息
        out_trade_no = data_dict['out_trade_no']
        send(out_trade_no, '支付成功')

        # 返回接收结果给微信，否则微信会每隔8分钟发送post请求
        return HttpResponse(trans_dict_to_xml({'return_code': 'SUCCESS', 'return_msg': 'OK'}))

    return HttpResponse(trans_dict_to_xml({'return_code': 'FAIL', 'return_msg': 'SIGNERROR'}))


clients = {}  # 创建一个空字典，用于保存所有接入的用户地址


@accept_websocket
def websocketLink(request, out_trade_no):

    '连接websocket'
    # 获取连接
    if request.is_websocket:
        lock = threading.RLock()  # rlock线程锁
        try:
            lock.acquire()  # 抢占资源

            clients[out_trade_no] = request.websocket

            # 监听接收客户端发送的消息 或者 客户端断开连接
            for message in request.websocket:
                if not message:
                    break
                else:
                    request.websocket.send(message)
        finally:
            # 释放锁
            lock.release()


# 服务端发送消息
def send(out_trade_no, msg):
    try:
        if clients[out_trade_no]:
            clients[out_trade_no].send(msg.encode('utf-8'))
            # 支付结果发送给前端页面后，该连接使命就完成了，前端页面会关闭该链接，这里需要当前连接的记录
            del clients[out_trade_no]
    except BaseException:
        print('发送消息出错了...')
