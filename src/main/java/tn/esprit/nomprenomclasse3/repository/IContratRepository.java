package tn.esprit.nomprenomclasse3.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.nomprenomclasse3.domain.Contrat;

public interface IContratRepository extends JpaRepository<Contrat, Long> {
}
