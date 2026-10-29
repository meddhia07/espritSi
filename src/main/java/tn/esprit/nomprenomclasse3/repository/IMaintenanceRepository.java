package tn.esprit.nomprenomclasse3.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.nomprenomclasse3.domain.Maintenance;

public interface IMaintenanceRepository extends JpaRepository<Maintenance, Long> {
}
