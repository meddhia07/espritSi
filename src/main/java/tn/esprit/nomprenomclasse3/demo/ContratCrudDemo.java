package tn.esprit.nomprenomclasse3.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import tn.esprit.nomprenomclasse3.domain.Contrat;
import tn.esprit.nomprenomclasse3.domain.ModePaiement;
import tn.esprit.nomprenomclasse3.domain.Paiement;
import tn.esprit.nomprenomclasse3.repository.IContratRepository;
import tn.esprit.nomprenomclasse3.repository.IPaiementRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Programme de demonstration de l'Atelier 3 : CRUD sur le Contrat, cascade et orphanRemoval.
 * Il ne s'execute qu'avec le profil "demo" (spring.profiles.active=demo) ;
 * le SQL genere est visible dans les logs (spring.jpa.show-sql=true).
 */
@Component
@Profile("demo")
public class ContratCrudDemo implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ContratCrudDemo.class);

    private final IContratRepository contratRepository;
    private final IPaiementRepository paiementRepository;
    private final TransactionTemplate transactionTemplate;

    public ContratCrudDemo(IContratRepository contratRepository,
                           IPaiementRepository paiementRepository,
                           TransactionTemplate transactionTemplate) {
        this.contratRepository = contratRepository;
        this.paiementRepository = paiementRepository;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void run(String... args) {
        // 1. CREATE : id null -> persist -> INSERT contrat + INSERT des paiements (cascade)
        Contrat contrat = Contrat.builder()
                .dateSignature(LocalDate.now())
                .montantTotal(new BigDecimal("450.00"))
                .valide(true)
                .build();
        contrat.ajouterPaiement(paiement("150.00", ModePaiement.CARTE));
        contrat.ajouterPaiement(paiement("300.00", ModePaiement.VIREMENT));
        Contrat enregistre = contratRepository.save(contrat);
        Long id = enregistre.getIdContrat();
        log.info("[CREATE] contrat {} cree, paiements en base : {}", id, paiementRepository.count());

        // 2. READ : findById renvoie un Optional
        contratRepository.findById(id)
                .ifPresent(c -> log.info("[READ] {}", c));
        log.info("[READ] contrat 999999 present ? {}", contratRepository.findById(999_999L).isPresent());

        // 3. UPDATE : id renseigne -> merge -> SELECT puis UPDATE ; on garde l'objet retourne
        enregistre.setMontantTotal(new BigDecimal("500.00"));
        enregistre = contratRepository.save(enregistre);
        log.info("[UPDATE] nouveau montant : {}", enregistre.getMontantTotal());

        // 4. orphanRemoval : retirer un paiement de la liste le supprime en base (DELETE paiement)
        transactionTemplate.executeWithoutResult(status -> {
            Contrat c = contratRepository.findById(id).orElseThrow();
            c.retirerPaiement(c.getPaiements().get(0));
        });
        log.info("[ORPHAN] paiements en base apres retrait : {}", paiementRepository.count());

        // 5. Tri et pagination (JpaRepository herite de ListPagingAndSortingRepository)
        Page<Contrat> page = contratRepository.findAll(
                PageRequest.of(0, 2, Sort.by("montantTotal").descending()));
        log.info("[PAGE] {} element(s) sur {} au total, {} page(s)",
                page.getContent().size(), page.getTotalElements(), page.getTotalPages());

        // 6. DELETE : cascade REMOVE -> DELETE des paiements puis du contrat
        contratRepository.deleteById(id);
        log.info("[DELETE] contrat {} existe encore ? {}", id, contratRepository.existsById(id));

        // 7. deleteById sur un id inexistant : aucune exception (Spring Data 3+)
        contratRepository.deleteById(999_999L);
        log.info("[DELETE] deleteById(999999) termine sans exception");
    }

    private static Paiement paiement(String montant, ModePaiement mode) {
        return Paiement.builder()
                .montant(new BigDecimal(montant))
                .datePaiement(LocalDate.now())
                .modePaiement(mode)
                .build();
    }
}
