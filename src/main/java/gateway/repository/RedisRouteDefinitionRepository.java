package gateway.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: jinweiwei
 * @date: 2019/1/25
 * Time: 14:14
 * To change this template use File | Settings | File Templates.
 * Description:
 */
@Component
public class RedisRouteDefinitionRepository implements RouteDefinitionRepository {
    Logger logger = LoggerFactory.getLogger(RedisRouteDefinitionRepository.class);


    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        logger.info("==========reload routes=============");
        List<RouteDefinition> routeDefinitions = new ArrayList<>();
        routeDefinitions.add(assemble("test","http://localhost:9999/test1"));
        return Flux.fromIterable(routeDefinitions);
    }

    private RouteDefinition assemble(String path,String target){
        RouteDefinition definition = new RouteDefinition();
        PredicateDefinition predicate = new PredicateDefinition();
        Map<String, String> predicateParams = new HashMap<>(8);
        definition.setId(path);
        predicate.setName("Query");
        predicateParams.put("param", "method");
        predicateParams.put("regexp", path);
        predicate.setArgs(predicateParams);
        definition.setPredicates(Arrays.asList(predicate));
        URI uri = UriComponentsBuilder.fromHttpUrl(target).build().toUri();
        definition.setUri(uri);
        FilterDefinition removeHeadFilter = new FilterDefinition();
        removeHeadFilter.setName("RemoveRequestHeader");
        Map<String, String> removefilter1Params = new HashMap<>(8);
        removefilter1Params.put("name","accept-encoding");
        removeHeadFilter.setArgs(removefilter1Params);
        definition.setFilters(Arrays.asList(removeHeadFilter));
        return definition;
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> mono) {
        return null;
    }

    @Override
    public Mono<Void> delete(Mono<String> mono) {
        return null;
    }
}