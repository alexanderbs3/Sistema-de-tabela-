// JwtAuthenticationEntryPoint.java - CORRIGIDO
package com.leaderboard.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.leaderboard.dto.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse,
                         AuthenticationException e) throws IOException, ServletException {

        logger.error("Responding with unauthorized error. Message - {}", e.getMessage());

        // Configurar resposta HTTP
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpServletResponse.setCharacterEncoding("UTF-8");

        // Criar resposta de erro padronizada
        ApiResponse<String> response = ApiResponse.error("Acesso não autorizado. Token inválido ou expirado.");

        // Serializar resposta para JSON
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        try {
            String jsonResponse = mapper.writeValueAsString(response);
            httpServletResponse.getWriter().write(jsonResponse);
            httpServletResponse.getWriter().flush();
        } catch (IOException ex) {
            logger.error("Erro ao escrever resposta de erro: {}", ex.getMessage());
            // Fallback para resposta simples
            httpServletResponse.getWriter().write("{\"success\":false,\"message\":\"Acesso não autorizado\"}");
            httpServletResponse.getWriter().flush();
        }
    }
}