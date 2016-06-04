package com.alipay.config;

/* *
 *类名：AlipayConfig
 *功能：基础配置类
 *详细：设置帐户有关信息及返回路径
 *版本：3.3
 *日期：2012-08-10
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
	
 *提示：如何获取安全校验码和合作身份者ID
 *1.用您的签约支付宝账号登录支付宝网站(www.alipay.com)
 *2.点击“商家服务”(https://b.alipay.com/order/myOrder.htm)
 *3.点击“查询合作者身份(PID)”、“查询安全校验码(Key)”

 *安全校验码查看时，输入支付密码后，页面呈灰色的现象，怎么办？
 *解决方法：
 *1、检查浏览器配置，不让浏览器做弹框屏蔽设置
 *2、更换浏览器或电脑，重新登录查询。
 */

public class AlipayConfig {
	
	//↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
	// 合作身份者ID，以2088开头由16位纯数字组成的字符串
	public static String partner = "2088221987004931";

	public static String seller_id = partner;
	// 商户的私钥
	public static String private_key = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJqbgGxIdNmtd2osYP0HR8QxTfqeniKEO2iJZMB8NUFcMrwFTKH/Cs5ir7E50MxhnPL5S+s52KtMB6n0kIudPdqh9N6F/nrp1QSIUJJrMR45VyOvS+omaEwU6Q+3opeQH3FTE9c7ESfMiYx1HVMGJl8Ld2Mut1L/y05XLiSnED5FAgMBAAECgYAmpYQ+jQYYe5IECj/cP/uigSia4xdoTWGESgPTUbkGM4PDGBgFznjnCigcxvT4gdX3rnVZwUX127V2uFmBbLbPEn3ne165HMRLRXHfZodQdwmiWON3CszJwkL+KYUh9snCKSFUffCab+RtEifSoa7YQ8HvdhNpBwog5dag+C2qKQJBAMrtciaRyGZt/6iyJVZKLielgWC6/LqY23mqcNozPi71Ygs7QdP8yU+4Jdytf1dKcbmu7UBGFenGjj2Klh5uBBMCQQDDCuSWFZXcjC3h8exPRecQAS9A4UA1VmqW23oSO9HVPswiMXGw4ktqPV4d2GvNlvtf0BlLgKknmKz5LG+UcA9HAkAei7l4Fs+so3gtY4t9PrNN4nrkHaF3URUQbliIb9g3Z1Z3zFavW0jOSDDi0/bsI9eUlME/wI0B4JJ+rwLD19mjAkEAkO/+T914gO0Wh4ncfkhG3ajTsZXxvfEawkIUo4oacyn4TgyKHNT9D9TMpMcXzS4+XMPqmCXjDc6x83Rm8lqXrwJBAKaOXrLynKju0/Kb8i1x0mPHGXW7snpDnUdZgijIlG2hAl8TdGd4n+ShrtIo+xW4BEucbni0yaWU5R6+EcWfLh0=";
	
	// 支付宝的公钥，无需修改该值
	public static String ali_public_key  = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";

	//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
	

	// 调试用，创建TXT日志文件夹路径
	public static String log_path = "D:\\";

	// 字符编码格式 目前支持 gbk 或 utf-8
	public static String _input_charset = "utf-8";
	
	// 签名方式 不需修改
	public static String sign_type = "RSA";

	// 接口名称
	public static String service = "mobile.securitypay.pay";

	//服务器异步通知页面
	public static String notify_url = "http://101.201.73.250:8888/alipay/a";

	//支付类型
	public static String payment_type = "1";
}
