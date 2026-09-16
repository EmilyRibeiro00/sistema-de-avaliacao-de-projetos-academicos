package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.AvaliadorArea;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvaliadorAreaRepository extends JpaRepository<AvaliadorArea, Long> {

    List<AvaliadorArea> findByAvaliadorId(Long avaliadorId);

    List<AvaliadorArea> findByAreaId(Long areaId);
}
