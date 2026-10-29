package tn.esprit.nomprenomclasse3.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.nomprenomclasse3.domain.Employe;

public interface IEmployeRepository extends JpaRepository<Employe, Long> {
}
