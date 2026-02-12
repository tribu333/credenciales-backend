package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.CargoProceso;
import com.credenciales.tribunal.model.entity.ProcesoElectoral;
import com.credenciales.tribunal.model.entity.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CargoProcesoRepository extends JpaRepository<CargoProceso, Long> {
    
    List<CargoProceso> findByProceso(ProcesoElectoral proceso);
    
    List<CargoProceso> findByProcesoId(Long procesoId);
    
    List<CargoProceso> findByUnidad(Unidad unidad);
    
    boolean existsByProcesoIdAndNombre(Long procesoId, String nombre);
}