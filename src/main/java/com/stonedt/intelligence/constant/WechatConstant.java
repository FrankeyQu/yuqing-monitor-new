package com.stonedt.intelligence.constant;
/**
 * 微信相关
 * @author wangyi
 *
 */
public class WechatConstant {


	/**
	 * 获取二维码
	 */
	public final static String GET_QRCODE = "/getQrCode";


	/**
	 * 发送消息
	 */
	public final static String SEND = "/send";



	// appid
	public final static String AppID = "";
	
	//AppSecret
	public final static String AppSecret = "";
	
	//获取AccessToken接口
	public final static String api_wechat_AccessToken = "";
	
	//发送模板消息
	public final static String api_wechat_template = "";
	
	//获取二维码ticket
	public final static String api_wechat_qrcode = "";
	
	//模板id
	public final static String api_wechat_template_id = "";
	
	//模板消息推送
	public final static String api_wechat_templatepush = "https://api.weixin.qq.com/cgi-bin/message/template/send";
	
	
	//生成临时二维码
	public final static String api_wechat_temporaryqrcode = "https://api.weixin.qq.com/cgi-bin/qrcode/create";
	
	
	/**
     * 授权
     */
    public static final String AUTH_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?";
    
    
    //
    public static final String AUTH_TOKEN = "https://api.weixin.qq.com/sns/oauth2/access_token?";
	
    
	/**
	 * 
	 */
    public static final String AUTH_Basic_Info = "https://api.weixin.qq.com/cgi-bin/user/info?";
    
    
    
    public static final String api_wechat_user ="https://api.weixin.qq.com/cgi-bin/user/get?";
    
	
	
	
	
	

}
