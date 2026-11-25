package com.simulador.api;

import com.simulador.security.CurrentPlayer;
import com.simulador.support.PlayerReport;
import com.simulador.support.PlayerReportRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Expone la consulta administrativa de reportes. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final CurrentPlayer current;
    private final PlayerReportRepository reports;
    /** Inicializa AdminController con las dependencias necesarias. */
    public AdminController(CurrentPlayer current, PlayerReportRepository reports) { this.current=current; this.reports=reports; }
    /** Devuelve reportes disponibles para la revisión administrativa. */
    @GetMapping("/reports")
    public List<PlayerReport> reports() { current.requireAdmin(); return reports.findAllByOrderByCreatedAtDesc(); }
}
