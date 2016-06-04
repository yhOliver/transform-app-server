package transform.app.server.api;

import com.alipay.config.AlipayConfig;
import com.alipay.model.PayInfo;
import com.alipay.sign.RSA;
import com.alipay.util.AlipayCore;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import transform.app.server.interceptor.POST;
import transform.app.server.interceptor.TokenInterceptor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * AlipayAPIController
 *
 * @author <a href="mailto:acsbq_young@163.com">Yang Hang</a>
 * @version V1.0.0
 * @since 2016-06-04
 */
public class AlipayAPIController extends Controller {

    /**
     * 添加签名验证
     */
    @Before({POST.class, TokenInterceptor.class})
    public void sign(){
        //获取所有参数
        String out_trade_no = getPara("out_trade_no");
        String subject = getPara("subject");
        String total_fee = getPara("total_fee");
        String body = getPara("body");

        Map<String,String> sParaTemp = new HashMap<String,String>();
        sParaTemp.put("service", AlipayConfig.service);
        sParaTemp.put("partner", AlipayConfig.partner);
        sParaTemp.put("seller_id", AlipayConfig.seller_id);
        sParaTemp.put("_input_charset", AlipayConfig._input_charset);
        sParaTemp.put("payment_type", AlipayConfig.payment_type);
        sParaTemp.put("notify_url", AlipayConfig.notify_url);

        sParaTemp.put("out_trade_no", out_trade_no);
        sParaTemp.put("subject", subject);
        sParaTemp.put("total_fee", total_fee);
        sParaTemp.put("body", body);

        String orderInfo = AlipayCore.createLinkString(sParaTemp);

        String sign =  RSA.sign(orderInfo,AlipayConfig.private_key,AlipayConfig._input_charset);

        try{
            //对sign做URL编码
            sign = URLEncoder.encode(sign,"UTF-8");
        } catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }

        final String payInfoString = orderInfo + "&sign=\""+sign+"\"&sign_type=\""+AlipayConfig.sign_type+"\"";
        PayInfo payInfo = new PayInfo();
        payInfo.setMsg("");
        payInfo.setPayInfo(payInfoString);

        renderJson(payInfo);
    }
}
