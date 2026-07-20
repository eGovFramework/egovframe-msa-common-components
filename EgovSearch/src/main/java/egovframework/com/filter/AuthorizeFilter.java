package egovframework.com.filter;

import egovframework.com.security.GatewayInternalAuthVerifier;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthorizeFilter extends OncePerRequestFilter {

    private final GatewayInternalAuthVerifier gatewayInternalAuthVerifier;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!gatewayInternalAuthVerifier.verify(request)) {
            log.warn("##### Access Denied: invalid or missing gateway signature");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.sendRedirect(request.getContextPath() + "/error/403.html");
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.matches(".*\\.(css|js|png|jpg|jpeg|gif|svg|woff|woff2|ttf|otf|eot|ico|html)$") ||
                path.startsWith("/swagger-ui/") ||
                path.equals("/swagger-ui.html") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/webjars/");
    }

}
