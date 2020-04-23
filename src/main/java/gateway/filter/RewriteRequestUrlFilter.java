package gateway.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: jinweiwei
 * @date: 2019/8/9
 * Time: 14:41
 * To change this template use File | Settings | File Templates.
 * Description:
 */
//@Component
public class RewriteRequestUrlFilter implements GlobalFilter, Ordered {

    private static final Log log = LogFactory.getLog(RouteToRequestUrlFilter.class);
    public static final int ROUTE_TO_URL_FILTER_ORDER = 10000;
    private static final String SCHEME_REGEX = "[a-zA-Z]([a-zA-Z]|\\d|\\+|\\.|-)*:.*";
    static final Pattern schemePattern = Pattern.compile("[a-zA-Z]([a-zA-Z]|\\d|\\+|\\.|-)*:.*");

    public RewriteRequestUrlFilter() {
    }

    @Override
    public int getOrder() {
        return 10001;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Route route = (Route)exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if(route == null) {
            return chain.filter(exchange);
        } else {
            log.trace("RouteToRequestUrlFilter start");
            URI uri = exchange.getRequest().getURI();
            boolean encoded = ServerWebExchangeUtils.containsEncodedParts(uri);
            URI routeUri = route.getUri();
            if(hasAnotherScheme(routeUri)) {
                exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_SCHEME_PREFIX_ATTR, routeUri.getScheme());
                routeUri = URI.create(routeUri.getSchemeSpecificPart());
            }

            if("lb".equalsIgnoreCase(routeUri.getScheme()) && routeUri.getHost() == null) {
                throw new IllegalStateException("Invalid host: " + routeUri.toString());
            } else {
                URI mergedUrl = UriComponentsBuilder.fromUri(uri).scheme(routeUri.getScheme())
                        .host(routeUri.getHost()).port(routeUri.getPort())
                        .path(routeUri.getRawPath()) //从routeURI获取请求路径
                        .build(encoded).toUri();
                exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, mergedUrl);
                return chain.filter(exchange);
            }
        }
    }

    static boolean hasAnotherScheme(URI uri) {
        return schemePattern.matcher(uri.getSchemeSpecificPart()).matches() && uri.getHost() == null && uri.getRawPath() == null;
    }
}