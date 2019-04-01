from django.conf.urls import url
from . import views

urlpatterns = [
    url(r'^$',views.index),
    url(r'^pay',views.pay),
    url(r'^result',views.result),
    url(r'websocketLink/(?P<out_trade_no>\w+)',views.websocketLink)  # webSocket 链接
]