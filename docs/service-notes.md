# Couche Service — AutoLoc (Atelier 4)

Packages : `tn.esprit.nomprenomclasse3.service` (interfaces `I…Service` et classes
`…ServiceImpl`) et `tn.esprit.nomprenomclasse3.exception` (`ResourceNotFoundException`).
Chaque implémentation est annotée `@Service` et `@RequiredArgsConstructor`, avec un champ
`final` typé par l'interface du repository : injection par constructeur, aucun `new`,
aucune injection par attribut, aucun attribut modifiable (services sans état).

Un premier contrôleur, `web.controller.VehiculeController`, consomme `IVehiculeService` ;
il est documenté dans Swagger UI (`http://localhost:8081/swagger-ui.html`) sous le tag
**VehiculesDhia**.

## A. Modes d'injection comparés (étape 2)

| Critère | Constructeur (`@RequiredArgsConstructor` + champ `final`) | Attribut (`@Autowired` sur le champ) |
|---|---|---|
| Champ `final` possible ? | Oui : affecté une seule fois, dans le constructeur. | Non : Spring affecte le champ par réflexion **après** la construction. |
| Dépendance visible dans la signature de la classe ? | Oui : elle apparaît dans le constructeur public. | Non : elle est cachée dans un champ privé. |
| Utilisable avec `new` hors conteneur sans dépendance `null` ? | Oui : impossible d'appeler le constructeur sans fournir le repository. | Non : `new ContratServiceImpl()` laisse `contratRepository` à `null` → `NullPointerException` au premier appel. |
| Signalé par SonarQube for IDE ? | Non. | Oui : règle `java:S6813` « Field dependency injection should be avoided ». |
| L'application démarre ? | Oui. | Oui (l'injection par attribut fonctionne, mais c'est une mauvaise pratique). |

Conclusion : l'injection par constructeur est la convention AutoLoc. L'expérience par attribut
a été restaurée en constructeur après observation.

## B. Services

| Service | Dépendances injectées | Mode | Justification |
|---|---|---|---|
| `ContratServiceImpl` | `IContratRepository` | Constructeur | CRUD complet ; refuse un id à la création et un montant total négatif ; `update` charge puis recopie (sans toucher aux paiements) ; `deleteById` vérifie l'existence, la cascade supprime les paiements. |
| `PaiementServiceImpl` | `IPaiementRepository` | Constructeur | Lecture seule (`findById`, `findAll`) : un paiement est une composition du Contrat et passe par `ajouterPaiement` / `retirerPaiement`. |
| `AgenceServiceImpl` | `IAgenceRepository` | Constructeur | CRUD ; refuse une agence sans nom. |
| `EmployeServiceImpl` | `IEmployeRepository` | Constructeur | CRUD ; refuse un employé sans nom ou sans rôle. |
| `VehiculeServiceImpl` | `IVehiculeRepository` | Constructeur | CRUD ; refuse un tarif journalier négatif. Consommé par `VehiculeController`. |
| `EquipementServiceImpl` | `IEquipementRepository` | Constructeur | CRUD ; refuse un équipement sans libellé (le modèle n'a pas d'attribut quantité). |
| `ClientServiceImpl` | `IClientRepository` | Constructeur | CRUD ; refuse un client sans nom ou sans e-mail. |
| `ReservationServiceImpl` | `IReservationRepository` | Constructeur | CRUD ; refuse une date de fin antérieure à la date de début. |
| `MaintenanceServiceImpl` | `IMaintenanceRepository` | Constructeur | CRUD ; refuse une date de fin antérieure à la date de début (le modèle n'a pas d'attribut coût). |

Chaque service dépend d'un seul repository, toujours par son interface : le proxy généré
par Spring Data (`SimpleJpaRepository`) lui est fourni par le conteneur. Aucun service ne
dépend d'un autre service : pas de risque de dépendance circulaire.

## C. Messages d'erreur du conteneur (étape 3)

**Message 1 — `NoSuchBeanDefinitionException`** (enveloppée dans une
`UnsatisfiedDependencyException`). Bean concerné : `ContratController`, qui demande un
bean de type `IContratService` que le conteneur ne trouve pas. Causes probables côté
`ContratServiceImpl` :
1. l'annotation `@Service` a été oubliée : la classe n'est pas un bean ;
2. la classe est placée hors du package racine (`tn.esprit.nomprenomclasse3`) ou de ses
   sous-packages : le scan ne la détecte jamais.

Correction : ajouter `@Service` et déplacer la classe dans `tn.esprit.nomprenomclasse3.service`.

**Message 2 — `NoUniqueBeanDefinitionException`.** Bean concerné : `NotificationController`,
qui demande une interface de notification implémentée par deux beans
(`emailNotificateur`, `smsNotificateur`). Deux corrections :
1. `@Primary` **sur la classe** de l'implémentation par défaut (ex. `EmailNotificateur`) :
   elle est choisie quand rien d'autre n'est précisé ;
2. `@Qualifier("smsNotificateur")` **sur le paramètre du constructeur** du contrôleur : choix
   explicite par le nom du bean (nom de la classe avec la première lettre en minuscule).
   Avec Lombok, on écrit alors le constructeur à la main (ou on configure
   `lombok.copyableAnnotations`).

**Message 3 — dépendance circulaire** (`BeanCurrentlyInCreationException`).
`ClientServiceImpl` injecte `ReservationServiceImpl` qui injecte `ClientServiceImpl`. Avec
l'injection par constructeur, pour créer A il faut B déjà construit, et pour créer B il faut
A : aucun ordre de création n'est possible, Spring ne peut pas fournir d'objet « à moitié
construit ». (Depuis Spring Boot 2.6, les cycles sont interdits par défaut même en injection
par attribut.) Piste de conception : extraire la responsabilité commune dans un troisième
composant dont les deux dépendent, ou faire dépendre les deux services uniquement des
**repositories** (ex. `ReservationServiceImpl` utilise `IClientRepository` et non
`ClientServiceImpl`).

## D. Anomalies SonarQube for IDE

| # | Anomalie | Règle / explication | Correction apportée |
|---|---|---|---|
| 1 | `@Autowired private IContratRepository contratRepository;` (étape 2) | `java:S6813` — injection par attribut : dépendance cachée, champ non `final`, `null` hors conteneur. | Retour à `@RequiredArgsConstructor` + champ `private final`. |
| 2 | `throw new RuntimeException(...)` dans `findById` (étape 4) | `java:S112` — lever une exception générique empêche l'appelant de distinguer les erreurs. | Exception métier dédiée `ResourceNotFoundException`. |
| 3 | Méthode privée de validation `valider(...)` qui n'utilise aucun attribut d'instance | `java:S2325` — une méthode privée qui n'utilise pas `this` doit être `static` : elle ne dépend pas de l'état de l'objet. | `private static void valider(...)`. |

## E. Questions de compréhension

1. **Où est le `new` de `ContratServiceImpl` et qui l'exécute ?** Il n'y en a pas dans notre
   code. Au démarrage, le scan détecte `@Service`, et le conteneur IoC (`ApplicationContext`,
   créé par `SpringApplication.run`) appelle lui-même le constructeur par réflexion, en lui
   passant le proxy de `IContratRepository`.
2. **Pourquoi un contrôleur dépend-il de `IContratService` et non de `ContratServiceImpl` ?**
   Pour dépendre d'une abstraction : on peut changer d'implémentation (ou fournir une
   implémentation de test) sans modifier le contrôleur. C'est le faible couplage, et
   l'interface est aussi ce que voient les proxys Spring (transactions, sécurité).
3. **Pourquoi un service singleton doit-il rester sans état ?** Une seule instance est partagée
   par tous les threads. Exemple de bug : un attribut `Contrat contratCourant;` affecté au
   début de `update` ; deux requêtes simultanées s'écrasent mutuellement et l'une modifie le
   contrat de l'autre.
4. **Pourquoi `update` charge-t-il l'existant avant `save` ?** L'objet reçu est incomplet :
   le passer à `save` déclencherait un `merge` qui écraserait toutes les colonnes (valeurs
   `null` comprises) et, avec `orphanRemoval`, une liste de paiements vide supprimerait les
   paiements en base. Charger puis recopier ne modifie que les attributs voulus et
   garantit que l'entité existe (sinon `ResourceNotFoundException`).
5. **`@Component` / `@Bean` ?** `@Component` (et `@Service`…) se pose sur une **classe** que l'on
   écrit, détectée par le scan ; `@Bean` se pose sur une **méthode** d'une classe
   `@Configuration` qui construit l'objet à la main (classe d'une bibliothèque ou du JDK,
   construction particulière). **`@Primary` / `@Qualifier` ?** `@Primary` se pose sur le bean
   choisi par défaut en cas d'ambiguïté ; `@Qualifier("nom")` se pose au point d'injection
   pour choisir explicitement un bean. **Pourquoi `IPaiementService` n'expose ni création,
   ni modification, ni suppression ?** Paiement est une composition de Contrat (cascade
   `ALL` + `orphanRemoval`) : un paiement n'existe que rattaché à son contrat, donc ces
   opérations passent par le Contrat (`ajouterPaiement` / `retirerPaiement`) pour garder
   les deux côtés de l'association cohérents.
