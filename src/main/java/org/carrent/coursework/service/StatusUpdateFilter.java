package org.carrent.coursework.service;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@WebFilter("/*") // Фільтр для всіх запитів
@RequiredArgsConstructor
public class StatusUpdateFilter implements Filter {

    private final GlobalStatusUpdater globalStatusUpdater;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // Оновлюємо статуси перед обробкою запиту
        globalStatusUpdater.updateStatuses();

        // Передаємо запит далі по ланцюгу обробки
        chain.doFilter(request, response);
    }
}
