package tn.esprit.nomprenomclasse3.service;

import tn.esprit.nomprenomclasse3.domain.Paiement;

import java.util.List;

/**
 * Lecture seule : un paiement est cree, modifie ou supprime a travers son Contrat
 * (composition, cascade ALL + orphanRemoval).
 */
public interface IPaiementService {

    Paiement findById(Long id);

    List<Paiement> findAll();
}
