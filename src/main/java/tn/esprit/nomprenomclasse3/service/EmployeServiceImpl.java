package tn.esprit.nomprenomclasse3.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.nomprenomclasse3.domain.Employe;
import tn.esprit.nomprenomclasse3.exception.ResourceNotFoundException;
import tn.esprit.nomprenomclasse3.repository.IEmployeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeServiceImpl implements IEmployeService {

    private static final String RESSOURCE = "Employe";

    private final IEmployeRepository employeRepository;

    @Override
    public Employe create(Employe employe) {
        if (employe.getIdEmploye() != null) {
            throw new IllegalArgumentException("Un nouvel objet Employe ne doit pas avoir d'identifiant");
        }
        valider(employe);
        return employeRepository.save(employe);
    }

    @Override
    public Employe findById(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESSOURCE, id));
    }

    @Override
    public List<Employe> findAll() {
        return employeRepository.findAll();
    }

    @Override
    public Employe update(Long id, Employe employe) {
        valider(employe);
        Employe existant = findById(id);
        existant.setNom(employe.getNom());
        existant.setPrenom(employe.getPrenom());
        existant.setRole(employe.getRole());
        return employeRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!employeRepository.existsById(id)) {
            throw new ResourceNotFoundException(RESSOURCE, id);
        }
        employeRepository.deleteById(id);
    }

    private static void valider(Employe employe) {
        if (employe.getNom() == null || employe.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom de l'employe est obligatoire");
        }
        if (employe.getRole() == null) {
            throw new IllegalArgumentException("Le role de l'employe est obligatoire");
        }
    }
}
