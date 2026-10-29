package tn.esprit.nomprenomclasse3.service;

import tn.esprit.nomprenomclasse3.domain.Maintenance;

import java.util.List;

public interface IMaintenanceService {

    Maintenance create(Maintenance maintenance);

    Maintenance findById(Long id);

    List<Maintenance> findAll();

    Maintenance update(Long id, Maintenance maintenance);

    void deleteById(Long id);
}
