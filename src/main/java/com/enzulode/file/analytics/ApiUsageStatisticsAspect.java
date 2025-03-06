package com.enzulode.file.analytics;

import com.enzulode.file.event.ApiUsageStatisticsEvent;
import com.enzulode.file.integration.rabbitmq.RabbitMQProducer;
import com.enzulode.file.util.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiUsageStatisticsAspect {

  private final RabbitMQProducer producer;
  private final SecurityContextHelper contextHelper;

  @Value("${api.stats.enabled:true}")
  private boolean enabled;
  @Value("${api.stats.rabbit-exchange}")
  private String statsExchange;
  @Value("${api.stats.rabbit-routing-key}")
  private String statsRoutingKey;

  @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
  public void sendRestControllerUsageStats() {}

  @AfterReturning(pointcut = "sendRestControllerUsageStats()", returning = "result")
  public void afterSendUsageStatistics(JoinPoint joinPoint, Object result) {
    if (!enabled) return;

    var signature = (MethodSignature) joinPoint.getSignature();
    var method = signature.getMethod();

    var controllerRequestMappingAnnotation = method.getDeclaringClass().getAnnotation(RequestMapping.class);
    var basePath = controllerRequestMappingAnnotation != null && controllerRequestMappingAnnotation.value().length > 0
        ? controllerRequestMappingAnnotation.value()[0]
        : "";

    var calledMethodInfo = getMethodDetails(method);

    var preparedBasePath = !basePath.isBlank() && !basePath.startsWith("/") ? "/%s".formatted(basePath) : basePath;
    preparedBasePath = !preparedBasePath.endsWith("/") ? preparedBasePath : preparedBasePath.substring(0, preparedBasePath.length() - 1);

    var preparedLocalPath = !calledMethodInfo.path().isBlank() && !calledMethodInfo.path().startsWith("/") ? "/%s".formatted(calledMethodInfo.path()) : calledMethodInfo.path();
    preparedLocalPath = !preparedLocalPath.endsWith("/") ? preparedLocalPath : preparedLocalPath.substring(0, preparedLocalPath.length() - 1);

    var resultPath = "%s%s".formatted(preparedBasePath, preparedLocalPath);
    var event = new ApiUsageStatisticsEvent(resultPath, calledMethodInfo.method(), LocalDateTime.now(), contextHelper.findUserName());
    producer.send(statsExchange, statsRoutingKey, event);
  }

  private AnnotatedMethodDetails getMethodDetails(Method method) {
    var getAnnot = method.getAnnotation(GetMapping.class);
    if (getAnnot != null) {
      var path = getAnnot.value().length > 0 ? getAnnot.value()[0] : "";
      return new AnnotatedMethodDetails(path, RequestMethod.GET);
    }

    var postAnnot = method.getAnnotation(PostMapping.class);
    if (postAnnot != null) {
      var path = postAnnot.value().length > 0 ? postAnnot.value()[0] : "";
      return new AnnotatedMethodDetails(path, RequestMethod.POST);
    }

    var putAnnot = method.getAnnotation(PutMapping.class);
    if (putAnnot != null) {
      var path = putAnnot.value().length > 0 ? putAnnot.value()[0] : "";
      return new AnnotatedMethodDetails(path, RequestMethod.PUT);
    }

    var deleteAnnot = method.getAnnotation(DeleteMapping.class);
    if (deleteAnnot != null) {
      var path = deleteAnnot.value().length > 0 ? deleteAnnot.value()[0] : "";
      return new AnnotatedMethodDetails(path, RequestMethod.DELETE);
    }

    var patchAnnot = method.getAnnotation(PatchMapping.class);
    if (patchAnnot != null) {
      var path = patchAnnot.value().length > 0 ? patchAnnot.value()[0] : "";
      return new AnnotatedMethodDetails(path, RequestMethod.PATCH);
    }

    return new AnnotatedMethodDetails("", null);
  }
  record AnnotatedMethodDetails(String path, RequestMethod method) {}
}
