package tn.esprit.nomprenomclasse3.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Contrat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long idContrat;
    LocalDate dateSignature;
    BigDecimal montantTotal;
    boolean valide;

    // Cote inverse : composition, un paiement n'existe pas sans son contrat
    @OneToMany(mappedBy = "contrat", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    List<Paiement> paiements = new ArrayList<>();

    // Le code metier passe par le Contrat pour ajouter ou retirer un paiement
    public void ajouterPaiement(Paiement paiement) {
        paiements.add(paiement);
        paiement.setContrat(this);
    }

    public void retirerPaiement(Paiement paiement) {
        paiements.remove(paiement);
        paiement.setContrat(null);
    }
}
