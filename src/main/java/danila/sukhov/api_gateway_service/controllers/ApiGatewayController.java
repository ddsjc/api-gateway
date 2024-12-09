package danila.sukhov.api_gateway_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class ApiGatewayController {
  private final RestTemplate restTemplate = new RestTemplate();
  @Autowired
  private LoadBalancerClient loadBalancerClient;

    @PostMapping("/auth-service/auth/**")
    public ResponseEntity<?> routeToAuthService(
            HttpServletRequest request,
            @RequestBody(required = false) String body,
            @RequestHeader Map<String, String> headers) {
        String targetUrl = getServiceUrl("auth-service") + getRequestPath(request, "/auth-service");
        return forwardRequest("POST", targetUrl, body, headers);
    }

    @GetMapping("/auth-service/user/**")
    public ResponseEntity<?> routeToGetUser(
            HttpServletRequest request,
            @RequestBody(required = false) String body,
            @RequestHeader Map<String, String> headers) {
        String targetUrl = getServiceUrl("auth-service") + getRequestPath(request, "/auth-service");
        System.out.println("Routing to: " + targetUrl);
        return forwardRequest("GET", targetUrl, body, headers);
    }

    @GetMapping("/task-tracker/**")
    public ResponseEntity<?> routeToTaskTrackerService(
            HttpServletRequest request,
            @RequestBody(required = false) String body,
            @RequestHeader Map<String, String> headers) {
        String targetUrl = getServiceUrl("task-tracker-service") + getRequestPath(request, "/task-tracker");
        return forwardRequest("GET", targetUrl, body, headers);
    }

    private String getServiceUrl(String serviceName) {
        return loadBalancerClient.choose(serviceName).getUri().toString();
    }

    private String getRequestPath(HttpServletRequest request, String prefix) {
        return request.getRequestURI().substring(prefix.length());
    }

    private ResponseEntity<?> forwardRequest(String method, String url, String body, Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach((key, value) -> {
            if ("authorization".equalsIgnoreCase(key)) {
                httpHeaders.set("Authorization", value);
            } else {
                httpHeaders.add(key, value);
            }
        });

        if (!httpHeaders.containsKey(HttpHeaders.CONTENT_TYPE)) {
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        }

        HttpEntity<String> requestEntity = new HttpEntity<>(body, httpHeaders);
        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());

        return restTemplate.exchange(url, httpMethod, requestEntity, String.class);
    }
}
