package gateway.filter;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: jinweiwei
 * @date: 2019/1/28
 * Time: 11:30
 * To change this template use File | Settings | File Templates.
 * Description:
 */
@Component
public class CommonsFilter implements GlobalFilter,Ordered {
    Logger logger = LoggerFactory.getLogger(CommonsFilter.class);
    private static final String START_TIME = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String method = exchange.getRequest().getQueryParams().getFirst("method");
        logger.info("==========start query redirect=======method:"+method);

        String info = String.format("Method:{%s} Host:{%s} Path:{%s} Query:{%s} token:{%s}",
                exchange.getRequest().getMethod().name(),
                exchange.getRequest().getURI().getHost(),
                exchange.getRequest().getURI().getPath(),
                method,
                "");
        exchange.getAttributes().put(START_TIME, System.currentTimeMillis());

        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();
        logger.info("======response decorator=======");
        try {

            ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                @Override
                public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                    logger.info("======start decorator=====");
                    if (body instanceof Flux) {
                        logger.info("======doing decorator=====");
                        Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;

                        return super.writeWith(fluxBody.buffer().map(dataBuffer -> {
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            dataBuffer.forEach(i -> {
                                byte[] array = new byte[i.readableByteCount()];
                                i.read(array);
                                try {
                                    outputStream.write(array);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                DataBufferUtils.release(i);
                            });
                            // probably should reuse buffers
                            byte[] content = outputStream.toByteArray();
                            String s = new String(content, Charset.forName("UTF-8"));
                            logger.info("====get response:"+s);
                        /*if (true){
                            s = GzipUtil.uncompress(content, "UTF-8");
                        }else{*/

                        /*}*/
                            boolean resultFlag = false;
                            String code = JSONObject.parseObject(s).getString("code");
                            if (!StringUtils.isEmpty(code)){
                                if (code.startsWith("1")){
                                    resultFlag = true;
                                }
                            }
                            if (!resultFlag){
                                String status = JSONObject.parseObject(s).getString("status");
                                if (!StringUtils.isEmpty(status)){
                                    if (status.startsWith("2")){
                                        resultFlag = true;
                                    }
                                }
                            }
                            //TODO，s就是response的值，想修改、查看就随意而为了
//                        byte[] uppedContent = s.getBytes();
                            Long costTime = (System.currentTimeMillis()-Long.valueOf(exchange.getAttribute(START_TIME).toString()));
                            if (StringUtils.isEmpty(s) || resultFlag){
                                JSONObject mqmsg = new JSONObject();
                                logger.info("method:"+method+" requestInfo:"+info+"||responseInfo:"+s+"||requestCostTime:"+costTime);
                            }
                            logger.info("=============done decorator==========");
                            return bufferFactory.wrap(content);
                        }));
                    }
                    logger.info("=======body is not a flux=====");
                    return super.writeWith(body);
                }
            };
            logger.info("==========end query redirect success=======method:"+method+"====responseDecorated:"+decoratedResponse);
            return chain.filter(exchange.mutate().response(decoratedResponse).build());
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("=====error=====");
        return null;
    }

    @Override
    public int getOrder() {
        return -2;
    }
}