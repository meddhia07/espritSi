# Couche Repository — AutoLoc (Atelier 3)

Package : `tn.esprit.nomprenomclasse3.repository`.
Convention : chaque interface est préfixée par `I` et étend `JpaRepository<Entité, Long>`.
Aucune classe d'implémentation n'est écrite : Spring Data génère un proxy au démarrage,
et l'annotation `@Repository` n'est pas nécessaire. Au démarrage, les logs affichent
`Found 9 JPA repository interfaces`.

## 1. Choix d'interface

| Interface | Étend | Justification |
|---|---|---|
| `IContratRepository` | `JpaRepository<Contrat, Long>` | CRUD complet, `findAll` renvoie une `List`, tri/pagination et `saveAndFlush` disponibles. Créée d'abord en `CrudRepository` (étape 1) pour observer `save`, `findById`, `deleteById`, la cascade et l'orphanRemoval. |
| `IPaiementRepository` | `JpaRepository<Paiement, Long>` | Utilisée surtout en lecture (lister, compter les paiements). Paiement est une composition de Contrat : la création et le retrait d'un paiement passent par `Contrat.ajouterPaiement` / `retirerPaiement`. |
| `IAgenceRepository` | `JpaRepository<Agence, Long>` | Entité de référence : CRUD complet et listes triées (par ville, par nom). |
| `IEmployeRepository` | `JpaRepository<Employe, Long>` | CRUD complet ; listes paginées d'employés par agence ou par rôle aux ateliers suivants. |
| `IVehiculeRepository` | `JpaRepository<Vehicule, Long>` | Catalogue consulté souvent : tri (tarif, marque) et pagination nécessaires. |
| `IEquipementRepository` | `JpaRepository<Equipement, Long>` | Petite table de référence : CRUD et `findAll` en `List`. |
| `IClientRepository` | `JpaRepository<Client, Long>` | CRUD complet ; la liste des clients grandit, la pagination sera utile côté contrôleur REST. |
| `IReservationRepository` | `JpaRepository<Reservation, Long>` | CRUD complet ; tri par date de début et pagination de l'historique. |
| `IMaintenanceRepository` | `JpaRepository<Maintenance, Long>` | CRUD complet ; historique trié par date. |

`JpaRepository` réunit `ListCrudRepository`, `ListPagingAndSortingRepository`,
`QueryByExampleExecutor` et les méthodes propres à JPA (`flush`, `saveAndFlush`,
`getReferenceById`). Depuis Spring Data 3, `PagingAndSortingRepository` n'étend plus
`CrudRepository` : il faudrait sinon déclarer les deux.

## 2. Comportement observé (programme `demo/ContratCrudDemo`)

Lancement : profil `demo` (`spring.profiles.active=demo`, ou *Active profiles* dans la
configuration IntelliJ). Sans ce profil, le programme ne s'exécute pas.

| Opération | SQL observé | Remarque |
|---|---|---|
| `save(contrat)` avec id `null` | `insert into contrat` puis 2 × `insert into paiement` | `persist` ; cascade `ALL` ; l'id attribué par MySQL est dans l'objet retourné. |
| `findById(id)` | `select … from contrat where id_contrat=?` | Renvoie un `Optional` ; `findById(999999)` → `Optional.empty()`. |
| `save(contrat)` avec id renseigné | `select` puis `update contrat` | `merge` ; on réutilise l'objet **retourné**. |
| `retirerPaiement(p)` dans une transaction | `delete from paiement where id_paiement=?` | orphanRemoval. |
| `findAll(PageRequest.of(0, 2, Sort.by("montantTotal").descending()))` | `select … order by montant_total desc limit ?` (+ `select count(*)` si la page est pleine) | `Sort` utilise le nom de l'attribut Java ; `Page` donne le total d'éléments et de pages. |
| `deleteById(id)` | `select`, `delete from paiement`, `delete from contrat` | Passe par le contexte de persistance : cascade REMOVE appliquée. |
| `deleteById(999999)` | `select` seulement | Aucune exception (Spring Data 3+). |

Attention : `deleteAllInBatch` / `deleteAllByIdInBatch` envoient un seul `DELETE` sans
passer par le contexte de persistance. La cascade et l'orphanRemoval ne s'appliquent
pas : les contrats ne doivent pas être supprimés ainsi (échec sur la clé étrangère
`contrat_id_contrat` de `paiement`).

## 3. Anomalies SonarQube for IDE (SonarLint) corrigées

| Anomalie | Règle / explication | Correction apportée |
|---|---|---|
| Package `tn.esprit.nomprenomclasse3.Domain` | `java:S120` — les noms de package doivent respecter `^[a-z_]+(\.[a-z_][a-z0-9_]*)*$` (minuscules). | Package renommé en `domain` (renommage du dossier et des déclarations `package`). |
| `contextLoads()` vide dans `NomPrenomClasse3ApplicationTests` | `java:S2699` — un test doit contenir au moins une assertion, sinon il ne vérifie rien. | Le test vérifie que le contexte contient exactement 9 beans `Repository`. |
| `import jakarta.persistence.*;` et `import lombok.*;` dans les entités | `java:S2208` — les imports génériques masquent l'origine des types et peuvent créer des conflits de noms. | Imports explicites, un par type utilisé. |

Autre amélioration relevée pendant la relecture : les identifiants étaient de type
primitif `long`. Ils passent en `Long`, cohérent avec `JpaRepository<Entité, Long>` et avec
la règle de `save` : un id `null` signifie « entité nouvelle » (INSERT).
