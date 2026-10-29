package tn.esprit.nomprenomclasse3.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.nomprenomclasse3.domain.Equipement;
import tn.esprit.nomprenomclasse3.exception.ResourceNotFoundException;
import tn.esprit.nomprenomclasse3.repository.IEquipementRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipementServiceImpl implements IEquipementService {

    private static final String RESSOURCE = "Equipement";

    private final IEquipementRepository equipementRepository;

    @Override
    public Equipement create(Equipement equipement) {
        if (equipement.getIdEquipement() != null) {
            throw new IllegalArgumentException("Un nouvel objet Equipement ne doit pas avoir d'identifiant");
        }
        valider(equipement);
        return equipementRepository.save(equipement);
    }

    @Override
    public Equipement findById(Long id) {
        return equipementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESSOURCE, id));
    }

    @Override
    public List<Equipement> findAll() {
        return equipementRepository.findAll();
    }

    @Override
    public Equipement update(Long id, Equipement equipement) {
        valider(equipement);
        Equipement existant = findById(id);
        existant.setLibelle(equipement.getLibelle());
        return equipementRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!equipementRepository.existsById(id)) {
            throw new ResourceNotFoundException(RESSOURCE, id);
        }
        equipementRepository.deleteById(id);
    }

    private static void valider(Equipement equipement) {
        if (equipement.getLibelle() == null || equipement.getLibelle().isBlank()) {
            throw new IllegalArgumentException("Le libelle de l'equipement est obligatoire");
        }
    }
}
