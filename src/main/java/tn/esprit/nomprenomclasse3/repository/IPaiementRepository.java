package tn.esprit.nomprenomclasse3.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.nomprenomclasse3.domain.Paiement;

public interface IPaiementRepository extends JpaRepository<Paiement, Long> {
}
