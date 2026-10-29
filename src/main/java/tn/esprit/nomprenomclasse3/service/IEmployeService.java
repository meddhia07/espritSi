package tn.esprit.nomprenomclasse3.service;

import tn.esprit.nomprenomclasse3.domain.Employe;

import java.util.List;

public interface IEmployeService {

    Employe create(Employe employe);

    Employe findById(Long id);

    List<Employe> findAll();

    Employe update(Long id, Employe employe);

    void deleteById(Long id);
}
