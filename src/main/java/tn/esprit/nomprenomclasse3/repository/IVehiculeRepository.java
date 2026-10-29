package tn.esprit.nomprenomclasse3.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.nomprenomclasse3.domain.Vehicule;

public interface IVehiculeRepository extends JpaRepository<Vehicule, Long> {
}
