package tn.esprit.nomprenomclasse3.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.nomprenomclasse3.domain.Contrat;
import tn.esprit.nomprenomclasse3.exception.ResourceNotFoundException;
import tn.esprit.nomprenomclasse3.repository.IContratRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContratServiceImpl implements IContratService {

    private static final String RESSOURCE = "Contrat";

    private final IContratRepository contratRepository;

    @Override
    public Contrat create(Contrat contrat) {
        if (contrat.getIdContrat() != null) {
            throw new IllegalArgumentException("Un nouvel objet Contrat ne doit pas avoir d'identifiant");
        }
        valider(contrat);
        return contratRepository.save(contrat);
    }

    @Override
    public Contrat findById(Long id) {
        return contratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESSOURCE, id));
    }

    @Override
    public List<Contrat> findAll() {
        return contratRepository.findAll();
    }

    @Override
    public Contrat update(Long id, Contrat contrat) {
        valider(contrat);
        Contrat existant = findById(id);
        // l'identifiant et la collection de paiements ne sont jamais recopies
        existant.setDateSignature(contrat.getDateSignature());
        existant.setMontantTotal(contrat.getMontantTotal());
        existant.setValide(contrat.isValide());
        return contratRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!contratRepository.existsById(id)) {
            throw new ResourceNotFoundException(RESSOURCE, id);
        }
        // cascade ALL + orphanRemoval : les paiements du contrat sont supprimes avec lui
        contratRepository.deleteById(id);
    }

    private static void valider(Contrat contrat) {
        if (contrat.getMontantTotal() != null
                && contrat.getMontantTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le montant total ne peut pas etre negatif");
        }
    }
}
