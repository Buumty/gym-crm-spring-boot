package pl.wojciechandrzejczak.gym_crm_spring_boot.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TransactionLoggingFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(TransactionLoggingFilter.class);

    private static final String MDC_KEY = "transactionId";
    private static final String HEADER_NAME = "X-Transaction-Id";

    @Override
    public void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String transactionId = UUID.randomUUID().toString();
        String previousId = MDC.get(MDC_KEY);
        long startedAt = System.nanoTime();
        boolean failed = false;

        MDC.put(MDC_KEY, transactionId);
        response.setHeader(HEADER_NAME, transactionId);

        try {
            log.info(
                    "HTTP request method={} path={}",
                    request.getMethod(),
                    request.getRequestURI()
            );

            filterChain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException ex) {
            failed = true;

            log.error(
                    "HTTP processing failed exceptionType={}",
                    ex.getClass().getName()
            );

            throw ex;
        } finally {
            try {
                long durationMs = TimeUnit.NANOSECONDS.toMillis(
                        System.nanoTime() - startedAt
                );

                if (failed) {
                    log.warn(
                            "HTTP request terminated before normal completion "
                                    + "method={} path={} durationMs={}",
                            request.getMethod(),
                            request.getRequestURI(),
                            durationMs
                    );
                } else {
                    log.info(
                            "HTTP response method={} path={} status={} durationMs={}",
                            request.getMethod(),
                            request.getRequestURI(),
                            response.getStatus(),
                            durationMs
                    );
                }
            } finally {
                if (previousId == null) {
                    MDC.remove(MDC_KEY);
                } else {
                    MDC.put(MDC_KEY, previousId);
                }
            }
        }
    }
}