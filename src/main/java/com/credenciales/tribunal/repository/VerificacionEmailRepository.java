package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.VerificacionEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificacionEmailRepository extends JpaRepository<VerificacionEmail, Long> {
    
    Optional<VerificacionEmail> findTopByEmailAndCarnetIdentidadAndUtilizadoFalseOrderByCreatedAtDesc(
            String email, String carnetIdentidad);
    
    @Modifying
    @Query("UPDATE VerificacionEmail v SET v.utilizado = true WHERE v.email = :email AND v.codigo = :codigo")
    void marcarComoUtilizado(@Param("email") String email, @Param("codigo") String codigo);
    
    @Modifying
    @Query("DELETE FROM VerificacionEmail v WHERE v.fechaExpiracion < :fecha")
    void eliminarExpirados(@Param("fecha") LocalDateTime fecha);
}
