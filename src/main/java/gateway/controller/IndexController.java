package gateway.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 *
 * @date: 2019/1/25
 * Time: 15:36
 * To change this template use File | Settings | File Templates.
 * Description:
 */
@RestController
public class IndexController {


    @RequestMapping("/test1")
    public Object test1() throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code",1000);
        jsonObject.put("msg","success");
        jsonObject.put("data",null);
        return jsonObject.toJSONString();
    }

    @RequestMapping("/test2")
    public String test2(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code",1000);
        jsonObject.put("msg","success=========");
        jsonObject.put("data","ssskdkskskskskskalkjasdlkfjalsdkjflaksdlkmkczvkajshdkjfhaskjehrkajsbedf,amsdbnfk");
        return jsonObject.toJSONString();
    }
}