package ps.demo.simplebatchdemo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
public class SwaggerOpenApiConfig implements WebMvcConfigurer {

    private final SwaggerProperties swaggerProperties;

    public SwaggerOpenApiConfig(SwaggerProperties swaggerProperties) {
        this.swaggerProperties = swaggerProperties;
    }

    @Bean
    public OpenAPI springDocOpenAPI() {
        Server tryServer = new Server();
        tryServer.setUrl(swaggerProperties.getTryHost());
        return new OpenAPI()
                .servers(Collections.singletonList(tryServer))
                .info(new Info()
                        .title(swaggerProperties.getApplicationName())
                        .description(swaggerProperties.getApplicationDescription())
                        .version(swaggerProperties.getApplicationVersion())
                );
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        try {
            Field registrationsField = FieldUtils.getField(InterceptorRegistry.class, "registrations", true);
            List<InterceptorRegistration> registrations = (List<InterceptorRegistration>) ReflectionUtils.getField(registrationsField, registry);
            if (registrations != null) {
                for (InterceptorRegistration interceptorRegistration : registrations) {
                    interceptorRegistration.excludePathPatterns("/springdoc**/**");
                }
            }
        } catch (Exception e) {
            log.error("Swagger config error {}", e.getMessage(), e);
        }
    }

}
