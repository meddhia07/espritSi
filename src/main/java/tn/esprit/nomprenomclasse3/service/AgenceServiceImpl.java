package tn.esprit.nomprenomclasse3.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.nomprenomclasse3.domain.Agence;
import tn.esprit.nomprenomclasse3.exception.ResourceNotFoundException;
import tn.esprit.nomprenomclasse3.repository.IAgenceRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgenceServiceImpl implements IAgenceService {

    private static final String RESSOURCE = "Agence";

    private final IAgenceRepository agenceRepository;

    @Override
    public Agence create(Agence agence) {
        if (agence.getIdAgence() != null) {
            throw new IllegalArgumentException("Un nouvel objet Agence ne doit pas avoir d'identifiant");
        }
        valider(agence);
        return agenceRepository.save(agence);
    }

    @Override
    public Agence findById(Long id) {
        return agenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESSOURCE, id));
    }

    @Override
    public List<Agence> findAll() {
        return agenceRepository.findAll();
    }

    @Override
    public Agence update(Long id, Agence agence) {
        valider(agence);
        Agence existant = findById(id);
        existant.setNom(agence.getNom());
        existant.setVille(agence.getVille());
        existant.setAdresse(agence.getAdresse());
        existant.setTelephone(agence.getTelephone());
        return agenceRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!agenceRepository.existsById(id)) {
            throw new ResourceNotFoundException(RESSOURCE, id);
        }
        agenceRepository.deleteById(id);
    }

    private static void valider(Agence agence) {
        if (agence.getNom() == null || agence.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom de l'agence est obligatoire");
        }
    }
}
