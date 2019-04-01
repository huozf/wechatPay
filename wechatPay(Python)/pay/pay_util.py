from random import Random
import time
import hashlib
from bs4 import BeautifulSoup
from pay.pay_setting import *
import requests
import qrcode


def random_str(randomlength=8):
    """
    生成随机字符串
    :param randomlength: 字符串长度
    :return:
    """
    str = ''
    chars = 'AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789'
    length = len(chars) - 1
    random = Random()
    for i in range(randomlength):
        str += chars[random.randint(0, length)]
    return str


def create_orderId(length):
    """
    生成扫码订单号
    :param length: 自定义长度
    :return:
    """
    local_time = time.strftime('%Y%m%d%H%M%S', time.localtime(time.time()))
    out_trade_no = random_str(length) + local_time
    return out_trade_no


def get_sign(data_dict, key):
    """
    签名函数
    :param data_dict: 需要签名的参数，格式为字典
    :param key: 密钥 ，即上面的API_KEY
    :return: 字符串
    """
    params_list = sorted(data_dict.items(), key=lambda e: e[0], reverse=False)  # 参数字典倒排序为列表
    params_str = "&".join(u"{}={}".format(k, v) for k, v in params_list) + '&key=' + key
    # 组织参数字符串并在末尾添加商户交易密钥
    md5 = hashlib.md5()  # 使用MD5加密模式
    md5.update(params_str.encode('utf-8'))  # 将参数字符串传入
    sign = md5.hexdigest().upper()  # 完成加密并转为大写
    return sign


def trans_dict_to_xml(data_dict):
    """
    定义字典转XML的函数
    :param data_dict:
    :return:
    """
    data_xml = []
    for k in sorted(data_dict.keys()):  # 遍历字典排序后的key
        v = data_dict.get(k)  # 取出字典中key对应的value
        if k == 'detail' and not v.startswith('<![CDATA['):  # 添加XML标记
            v = '<![CDATA[{}]]>'.format(v)
        data_xml.append('<{key}>{value}</{key}>'.format(key=k, value=v))
    return '<xml>{}</xml>'.format(''.join(data_xml))  # 返回XML


def trans_xml_to_dict(data_xml):
    """
    定义XML转字典的函数
    :param data_xml:
    :return:
    """
    soup = BeautifulSoup(data_xml, features='xml')
    xml = soup.find('xml')  # 解析XML
    if not xml:
        return {}
    data_dict = dict([(item.name, item.text) for item in xml.find_all()])
    return data_dict


def wxpay( body , total_fee ,out_trade_no ):
    '''
    向微信发出请求，并获取返回的xml，将该xml转为字典
    :return:
    '''
    nonce_str = random_str(20)  # 拼接出随机的字符串即可，我这里是用  时间+随机数字+5个随机字母

    params = {
        'appid': APP_ID,  # APPID
        'mch_id': MCH_ID,  # 商户号
        'nonce_str': nonce_str,  # 随机字符串
        'out_trade_no': out_trade_no,  # 订单编号，可自定义
        'total_fee': total_fee,  # 订单总金额
        'spbill_create_ip': CREATE_IP,  # 自己服务器的IP地址
        'notify_url': NOTIFY_URL,  # 回调地址，微信支付成功后会回调这个url，告知商户支付结果
        'body': body,  # 商品描述
        'trade_type': 'NATIVE',  # 扫码支付类型
    }
    sign = get_sign(params, API_KEY)  # 获取签名
    params['sign'] = sign  # 添加签名到参数字典
    xml = trans_dict_to_xml(params)  # 转换字典为XML

    response = requests.request('post', 'https://api.mch.weixin.qq.com/pay/unifiedorder',
                                data=xml)  # 以POST方式向微信公众平台服务器发起请求

    data_dict = trans_xml_to_dict(response.content)  # 将请求返回的数据转为字典

    return data_dict


def get_code_url(data_dict):
    """
    从微信返回的数据中提取出二维码字符串
    :param data_dict: 字典数据
    :return: 二维码链接
    """
    code_url=data_dict.get('code_url')
    return code_url


def create_image(code_url):
    """
    将传进来的二维码链接转换为二维码图片
    :param code_url: 二维码链接
    :return: 二维码图片
    """
    img = qrcode.make(code_url)
    return img