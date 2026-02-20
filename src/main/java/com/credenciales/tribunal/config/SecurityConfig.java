package com.credenciales.tribunal.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.http.HttpMethod;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter authTokenFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Habilitar CORS
                .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF para APIs
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Sin estado para JWT
                )
                .authorizeHttpRequests(auth -> auth
                        // Swagger/OpenAPI docs
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/v2/api-docs",
                                "/webjars/**",
                                "/swagger-resources/**")
                        .permitAll()

                        // ============ NUEVAS RUTAS DE PERSONAL ============
                        // Endpoints públicos de personal (verificación y registro)
                        .requestMatchers(HttpMethod.POST, "/api/personal/solicitar-codigo").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/personal/verificar-codigo").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/personal/registrar").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/personal/verificar-correo/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/personal/puede-registrarse/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/personal/mensaje-estado/**").permitAll()

                        // Endpoints de consulta pública
                        .requestMatchers(HttpMethod.GET, "/api/personal/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/personal/ci/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/personal/buscar").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/personal/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/personal/**").permitAll()

                        // ============ RUTAS DE QR ============
                        // Endpoints públicos de QR (generación y consulta)
                        .requestMatchers(HttpMethod.POST, "/api/qr/generar/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/qr/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/qr/codigo/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/qr/libres").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/qr/tipo/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/qr/personal/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/qr/*/imagen").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/qr/*/ver").permitAll()

                        // Endpoints de modificación de QR (requieren autenticación)
                        .requestMatchers(HttpMethod.PUT, "/api/qr/*/asignar/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/qr/*/liberar").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/qr/*/inactivar").hasRole("ADMINISTRADOR")
                        // =====================================

                        // ============ RUTAS DE ESTADOS ============
                        // Endpoints públicos de estados
                        .requestMatchers(HttpMethod.GET, "/api/estados-personal/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/personal/estado/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/estados-personal/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/estados-personal/personal/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/estados-personal/estado/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/personal/*/acceso").permitAll()

                        // Endpoints de cambio de estado (requieren autenticación)
                        .requestMatchers(HttpMethod.POST, "/api/estados-personal/registrar").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/estados-personal/*/**").hasRole("ADMINISTRADOR")
                        // =========================================

                        .requestMatchers(HttpMethod.GET, "/api/unidades/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/cargos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/historiales-cargo/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/procesos-electorales/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/cargos-proceso/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/historiales-cargo-proceso/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/imagenes/**").permitAll()
                        
                        .requestMatchers(HttpMethod.DELETE, "/api/imagenes/**").hasRole("ADMINISTRADOR")
                        // Restringir POST, PUT, DELETE a ADMINISTRADOR
                        .requestMatchers(HttpMethod.POST, "/api/unidades/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/empleados/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/unidades/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/unidades/**").permitAll() //hasRole("ADMINISTRADOR")

                        .requestMatchers(HttpMethod.POST, "/api/cargos/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/supervisor/horarios/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/cargos/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/cargos/**").hasRole("ADMINISTRADOR")

                        .requestMatchers(HttpMethod.POST, "/api/historiales-cargo/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/administradores/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/historiales-cargo/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/historiales-cargo/**").hasRole("ADMINISTRADOR")

                        .requestMatchers(HttpMethod.POST, "/api/imagenes/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/procesos-electorales/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/procesos-electorales/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/procesos-electorales/**").hasRole("ADMINISTRADOR")

                        .requestMatchers(HttpMethod.POST, "/api/cargos-proceso/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ubicaciones/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/cargos-proceso/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/cargos-proceso/**").hasRole("ADMINISTRADOR")

                        .requestMatchers(HttpMethod.POST, "/api/historiales-cargo-proceso/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/descriptores/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/historiales-cargo-proceso/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/historiales-cargo-proceso/**").hasRole("ADMINISTRADOR")
                        // Ruta de autenticación pública
                        .requestMatchers("/api/auth/**").permitAll()

                        .anyRequest().authenticated() // El resto requiere autenticación
                );

        // Agregar nuestro filtro JWT antes del filtro de autenticación
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos (frontend)
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "*"
        ));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "X-Admin-Id",
                "X-Admin-ID",
                "x-admin-id"));

        // Headers expuestos al frontend
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type"));

        // Permitir credenciales (cookies, auth headers)
        configuration.setAllowCredentials(true);

        // Tiempo de cache para preflight requests
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplicar a todos los endpoints

        return source;
    }
}