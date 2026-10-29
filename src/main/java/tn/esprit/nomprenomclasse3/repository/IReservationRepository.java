package tn.esprit.nomprenomclasse3.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.nomprenomclasse3.domain.Reservation;

public interface IReservationRepository extends JpaRepository<Reservation, Long> {
}
