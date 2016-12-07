package eu.yaga.stockanalyzer.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;

import java.io.IOException;

/**
 * Interceptor that sets an user agent for RestTemplate
 */
public class XUserAgentInterceptor implements ClientHttpRequestInterceptor {
    /**
     * Intercept the given request, and return a response. The given {@link ClientHttpRequestExecution} allows
     * the interceptor to pass on the request and response to the next entity in the chain.
     * <p>
     * <p>A typical implementation of this method would follow the following pattern:
     * <ol>
     * <li>Examine the {@linkplain HttpRequest request} and body</li>
     * <li>Optionally {@linkplain HttpRequestWrapper wrap} the request to filter HTTP attributes.</li>
     * <li>Optionally modify the body of the request.</li>
     * <li><strong>Either</strong>
     * <ul>
     * <li>execute the request using {@link ClientHttpRequestExecution#execute(HttpRequest, byte[])},</li>
     * <strong>or</strong>
     * <li>do not execute the request to block the execution altogether.</li>
     * </ul>
     * <li>Optionally wrap the response to filter HTTP attributes.</li>
     * </ol>
     *
     * @param request   the request, containing method, URI, and headers
     * @param body      the body of the request
     * @param execution the request execution
     * @return the response
     * @throws IOException in case of I/O errors
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpHeaders headers = request.getHeaders();
        headers.add("X-User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.109 Safari/537.36");
        return execution.execute(request, body);
    }
}
