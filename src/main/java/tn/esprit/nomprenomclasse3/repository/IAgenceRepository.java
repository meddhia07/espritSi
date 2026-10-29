package tn.esprit.nomprenomclasse3.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.nomprenomclasse3.domain.Agence;

public interface IAgenceRepository extends JpaRepository<Agence, Long> {
}
