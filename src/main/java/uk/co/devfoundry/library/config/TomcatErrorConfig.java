package uk.co.devfoundry.library.config;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.valves.ErrorReportValve;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

/**
 * Tomcat answers a few broken requests itself (for example /books/%zz) with an HTML page before Spring
 * sees them. This swaps that page for the API's JSON error format.
 */
@Component
public class TomcatErrorConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.addContextCustomizers(context -> {
            if (context.getParent() instanceof StandardHost host) {
                host.setErrorReportValveClass(JsonErrorReportValve.class.getName());
            }
        });
    }

    public static class JsonErrorReportValve extends ErrorReportValve {

        @Override
        protected void report(Request request, Response response, Throwable throwable) {
            int status = response.getStatus();
            if (status < 400 || response.getContentWritten() > 0 || !response.setErrorReported()) {
                return;
            }
            String body = switch (status) {
                case 400 -> "{\"error\": {\"code\": \"validation_error\", \"message\": \"bad request\"}}";
                case 404 -> "{\"error\": {\"code\": \"not_found\", \"message\": \"not found\"}}";
                case 405 -> "{\"error\": {\"code\": \"method_not_allowed\", \"message\": \"method not allowed\"}}";
                default -> "{\"error\": {\"code\": \"internal_error\", \"message\": \"unexpected problem\"}}";
            };
            try {
                response.setContentType("application/json");
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                Writer writer = response.getReporter();
                if (writer != null) {
                    writer.write(body);
                    response.finishResponse();
                }
            } catch (IOException | IllegalStateException e) {
                // the client has gone or the response is already closed: nothing more to send
            }
        }
    }
}
