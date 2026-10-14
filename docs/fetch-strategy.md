# Stratégie de fetch et de cascade — AutoLoc

Association implémentée : **Contrat ↔ Paiement** (bidirectionnelle).
Les 9 entités du modèle existent ; les autres associations ne sont pas mappées.

| Association | Fetch | Cascade | Justification |
|---|---|---|---|
| Contrat → Paiement | LAZY | ALL + orphanRemoval | Un paiement n'existe que rattaché à son contrat (composition) ; supprimer le contrat supprime ses paiements, et retirer un paiement de la liste le supprime en base. |

## Détails

- **Côté propriétaire** : `Paiement.contrat` (`@ManyToOne`, LAZY) porte la clé
  étrangère `contrat_id_contrat`.
- **Côté inverse** : `Contrat.paiements` (`@OneToMany(mappedBy = "contrat")`), ne
  crée aucune colonne.
- **LAZY** : la liste des paiements n'est chargée que si on appelle `getPaiements()`.
- **Pas de `@Data`** : `@Getter`/`@Setter` ciblés et `@ToString.Exclude` sur
  l'association pour éviter une boucle infinie.
