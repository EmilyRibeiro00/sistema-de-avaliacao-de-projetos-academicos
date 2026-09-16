package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {
}
