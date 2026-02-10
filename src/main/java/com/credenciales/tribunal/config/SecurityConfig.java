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

import com.credenciales.tribunal.service.AuthService;

import org.springframework.http.HttpMethod;
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {



    @Autowired
    private AuthService authTokenFilter;

    

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
                    "/v2/api-docs",
                    "/webjars/**",
                    "/swagger-resources/**"
                ).permitAll()

                // Permitir acceso público solo a métodos GET
                .requestMatchers(HttpMethod.GET, "/api/empleados/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/supervisor/horarios/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/administradores/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/imagenes/descargar/**").permitAll() // O .authenticated()
                .requestMatchers(HttpMethod.GET,"/api/ubicaciones/**").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/descriptores/**").permitAll()

                // Restringir POST, PUT, DELETE a ADMIN
                .requestMatchers(HttpMethod.POST, "/api/empleados/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/empleados/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/empleados/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/supervisor/horarios/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/supervisor/horarios/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/supervisor/horarios/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/administradores/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/administradores/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/administradores/**").hasRole("ADMIN")
                
                .requestMatchers(HttpMethod.POST,"/api/imagenes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,"/api/imagenes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE,"/api/imagenes/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST,"/api/ubicaciones/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,"/api/ubicaciones/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE,"/api/ubicaciones/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST,"/api/descriptores/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,"/api/descriptores/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE,"/api/descriptores/**").hasRole("ADMIN")
                // Ruta de autenticación pública
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/asistencias/**").permitAll()

                .anyRequest().authenticated() // El resto requiere autenticación
            );

        // Agregar nuestro filtro JWT antes del filtro de autenticación
        //http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

     @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Orígenes permitidos (frontend)
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",  // React dev server
            "http://localhost:4200",  // Angular dev server
            "http://localhost:5173",  // Vite dev server
            "http://localhost:8081"   // Otros puertos
        ));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
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
            "x-admin-id"
        ));
        
        // Headers expuestos al frontend
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type"
        ));
        
        // Permitir credenciales (cookies, auth headers)
        configuration.setAllowCredentials(true);
        
        // Tiempo de cache para preflight requests
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplicar a todos los endpoints
        
        return source;
    }
}