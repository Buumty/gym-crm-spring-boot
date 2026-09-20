package pl.wojciechandrzejczak.gym_crm_spring_boot.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.filter.TransactionLoggingFilter;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionLoggingFilterTest {

    private static final String MDC_KEY = "transactionId";
    private static final String HEADER_NAME = "X-Transaction-Id";

    private final TransactionLoggingFilter filter =
            new TransactionLoggingFilter();

    @AfterEach
    void cleanupMdc() {
        MDC.clear();
    }

    @Test
    void shouldGenerateTransactionIdAndAddItToResponseHeader()
            throws ServletException, IOException {

        var request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/trainees");

        var response = new MockHttpServletResponse();

        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        String transactionId =
                response.getHeader(HEADER_NAME);

        assertNotNull(transactionId);

        assertDoesNotThrow(
                () -> UUID.fromString(transactionId)
        );

        verify(filterChain).doFilter(
                request,
                response
        );
    }

    @Test
    void shouldRemoveTransactionIdFromMdcAfterSuccessfulRequest()
            throws ServletException, IOException {

        MDC.remove(MDC_KEY);

        var request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/trainees");

        var response = new MockHttpServletResponse();

        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertNull(MDC.get(MDC_KEY));
    }

    @Test
    void shouldRestorePreviousTransactionIdAfterRequest()
            throws ServletException, IOException {

        String previousTransactionId =
                "previous-transaction-id";

        MDC.put(
                MDC_KEY,
                previousTransactionId
        );

        var request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/trainers");

        var response = new MockHttpServletResponse();

        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertEquals(
                previousTransactionId,
                MDC.get(MDC_KEY)
        );
    }

    @Test
    void shouldRethrowRuntimeExceptionAndClearMdc()
            throws ServletException, IOException {

        MDC.remove(MDC_KEY);

        var request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/trainings");

        var response = new MockHttpServletResponse();

        FilterChain filterChain = mock(FilterChain.class);

        RuntimeException expected =
                new RuntimeException("Something went wrong");

        doThrow(expected)
                .when(filterChain)
                .doFilter(request, response);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> filter.doFilterInternal(
                        request,
                        response,
                        filterChain
                )
        );

        assertSame(expected, thrown);

        assertNull(MDC.get(MDC_KEY));

        assertNotNull(
                response.getHeader(HEADER_NAME)
        );
    }

    @Test
    void shouldRethrowIOExceptionAndClearMdc()
            throws ServletException, IOException {

        MDC.remove(MDC_KEY);

        var request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/trainees");

        var response = new MockHttpServletResponse();

        FilterChain filterChain = mock(FilterChain.class);

        IOException expected =
                new IOException("IO error");

        doThrow(expected)
                .when(filterChain)
                .doFilter(request, response);

        IOException thrown = assertThrows(
                IOException.class,
                () -> filter.doFilterInternal(
                        request,
                        response,
                        filterChain
                )
        );

        assertSame(expected, thrown);

        assertNull(MDC.get(MDC_KEY));
    }

    @Test
    void shouldRethrowServletExceptionAndRestorePreviousMdc()
            throws ServletException, IOException {

        String previousTransactionId =
                "previous-id";

        MDC.put(
                MDC_KEY,
                previousTransactionId
        );

        var request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/trainers");

        var response = new MockHttpServletResponse();

        FilterChain filterChain = mock(FilterChain.class);

        ServletException expected =
                new ServletException("Servlet error");

        doThrow(expected)
                .when(filterChain)
                .doFilter(request, response);

        ServletException thrown = assertThrows(
                ServletException.class,
                () -> filter.doFilterInternal(
                        request,
                        response,
                        filterChain
                )
        );

        assertSame(expected, thrown);

        assertEquals(
                previousTransactionId,
                MDC.get(MDC_KEY)
        );
    }
}