package tn.esprit.nomprenomclasse3.service;

import tn.esprit.nomprenomclasse3.domain.Client;

import java.util.List;

public interface IClientService {

    Client create(Client client);

    Client findById(Long id);

    List<Client> findAll();

    Client update(Long id, Client client);

    void deleteById(Long id);
}
