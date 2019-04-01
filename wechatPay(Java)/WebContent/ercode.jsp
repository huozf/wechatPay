<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!doctype html>
<html>
<head>
<meta charset="UTF-8">
<title>扫码支付</title>
<base href="/wechatPay/">



<style>
.box {
	position: absolute;
	left: 50%;
	top: 20%;
	transform: translate(-50%);
	text-align: center;
}

.conten {
	width: 100%;
}

body {
	background: #ccc;
}
</style>
<body>

	<div class="box">

		<h2 class="content">
			<span>扫码支付</span>
		</h2>
		<h4>订单号：<span id="out_trade_no">${sessionScope.out_trade_no}</span></h4>
		<img src="payment/image">
		<!--显示支付二维码-->
		<br>
		<span id="message"></span>
	</div>

</body>

	<script type="text/javascript">
		var websocket = null;//websocket 的变量
		function load() {//初始化 websocket
			//获取订单号
			var out_trade_no = document.getElementById("out_trade_no").innerHTML;
			//建立连接
			if ('WebSocket' in window) {//支持 websocket
				websocket = new WebSocket("ws://" + document.location.host+ "/wechatPay/websocket/" + out_trade_no);

				websocket.onopen = function() {
					fillData("建立连接了")
				}
				websocket.onclose = function() {
					fillData("连接关闭了")
				}
				websocket.onerror = function() {
					fillData("出现错误了")
				}
				websocket.onmessage = function(event) {
					fillData(event.data);
					alert("支付成功！！！");
				}

			} else {
				alert("浏览器不支持 websocket")
			}
			//设置监听,当有消息来的时候的监听
		}

		function fillData(data) {
			document.getElementById("message").innerHTML = data;
		}

		window.onload=load();//页面加载完成后执行 load 方法
	</script>

</html>
