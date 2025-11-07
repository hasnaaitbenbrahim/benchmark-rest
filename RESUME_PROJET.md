# Résumé Concret du Projet Benchmark REST

## 📋 Vue d'ensemble du projet

Ce projet est une **étude de performance comparative** qui évalue 3 implémentations différentes d'API REST Java pour exposer des données relationnelles (Catégories et Items). L'objectif est de comparer les performances, la consommation de ressources et la facilité d'utilisation de chaque approche.

---

## 🎯 Objectif

Déterminer quelle variante d'API REST est la plus performante selon différents critères :
- **Débit** (RPS - Requests Per Second)
- **Latence** (p50, p95, p99)
- **Consommation de ressources** (CPU, mémoire, threads)
- **Facilité d'exposition relationnelle**
- **Gestion des JOINs et requêtes N+1**

---

## 🏗️ Architecture du projet

### Les 3 variantes comparées

#### **Variante A : Jersey** (`variantA-jersey/`)
- **Framework** : Jakarta EE / Jersey (JAX-RS)
- **ORM** : JPA (Jakarta Persistence)
- **Caractéristiques** :
  - Contrôleurs manuels avec annotations `@Path`, `@GET`, `@POST`, etc.
  - Gestion explicite des EntityManager
  - Pagination manuelle
  - Endpoints : `/categories`, `/items`, `/categories/{id}/items`
- **Port** : 8081

#### **Variante C : Spring MVC** (`variantC-springmvc/`)
- **Framework** : Spring Boot avec `@RestController`
- **ORM** : Spring Data JPA
- **Caractéristiques** :
  - Contrôleurs avec annotations Spring (`@RestController`, `@GetMapping`, etc.)
  - Repository pattern avec Spring Data JPA
  - Pagination Spring (Pageable)
  - Endpoints identiques à la variante A
- **Port** : 8082

#### **Variante D : Spring Data REST** (`variantD-springdatarest/`)
- **Framework** : Spring Boot avec Spring Data REST
- **ORM** : Spring Data JPA
- **Caractéristiques** :
  - **Exposition automatique** des repositories
  - Format HAL (Hypertext Application Language)
  - Pas de contrôleurs manuels
  - Endpoints HAL standard : `/categories`, `/items`, `/categories/{id}/items`
- **Port** : 8083

### Infrastructure de test

#### Base de données
- **PostgreSQL 16** (Docker)
- Tables : `category`, `item` (relation 1-N)
- Données de test pré-chargées

#### Outils de monitoring
- **Prometheus** : Collecte des métriques JVM (CPU, mémoire, GC, threads, HikariCP)
- **Grafana** : Visualisation des dashboards
- **InfluxDB 2** : Stockage des métriques JMeter

#### Outils de test de charge
- **JMeter** : Exécution des scénarios de test
- 4 scénarios définis dans des fichiers `.jmx`

---

## 🧪 Méthodologie : Comment le benchmark a été réalisé

### 1. Scénarios de test (4 scénarios)

#### **Scénario 1 : READ-heavy**
- **Description** : Charge de lecture intensive
- **Mix de requêtes** :
  - 50% : GET `/items?page=X&size=50` (liste paginée)
  - 20% : GET `/items?categoryId=X` (filtrage par catégorie)
  - 20% : GET `/categories/{id}/items` (items d'une catégorie)
  - 10% : GET `/categories?page=X&size=50` (liste de catégories)
- **Paramètres** :
  - Threads : 50 → 100 → 200 (paliers progressifs)
  - Ramp-up : 60 secondes
  - Durée : 10 minutes par palier
- **Objectif** : Tester les performances en lecture, notamment les JOINs

#### **Scénario 2 : JOIN-filter**
- **Description** : Filtrage avec jointures (cas critique)
- **Mix de requêtes** :
  - 70% : GET `/items?categoryId=X` (requête avec JOIN)
  - 30% : GET `/items/{id}` (requête simple)
- **Paramètres** :
  - Threads : 60 → 120
  - Ramp-up : 60 secondes
  - Durée : 8 minutes par palier
- **Objectif** : Mesurer l'impact des requêtes avec JOIN et détecter les problèmes N+1

#### **Scénario 3 : MIXED**
- **Description** : Mix lecture/écriture (CRUD complet)
- **Mix de requêtes** :
  - GET `/items`, GET `/categories`
  - POST `/items`, POST `/categories`
  - PUT `/items/{id}`, PUT `/categories/{id}`
  - DELETE `/items/{id}`, DELETE `/categories/{id}`
- **Paramètres** :
  - Threads : 50 → 100
  - Ramp-up : 60 secondes
  - Durée : 10 minutes par palier
  - Payload : 1 KB (fichier `payloads_1k.csv`)
- **Objectif** : Tester les performances en écriture et transactions

#### **Scénario 4 : HEAVY-body**
- **Description** : Payloads lourds (simulation de données complexes)
- **Mix de requêtes** :
  - POST `/items` avec payload de 5 KB
  - PUT `/items/{id}` avec payload de 5 KB
- **Paramètres** :
  - Threads : 30 → 60
  - Ramp-up : 60 secondes
  - Durée : 8 minutes par palier
  - Payload : 5 KB (fichier `payloads_5k.csv`)
- **Objectif** : Tester la gestion des gros payloads et la sérialisation

### 2. Données de test

#### Fichiers CSV générés
- **`ids.csv`** : 1000 paires (itemId, categoryId) pour varier les requêtes
- **`payloads_1k.csv`** : 50 payloads JSON de ~1 KB
- **`payloads_5k.csv`** : 20 payloads JSON de ~5 KB

### 3. Processus d'exécution

Le script `run_benchmark.sh` automatise tout le processus :

```bash
1. Compilation des 3 variantes (Maven)
2. Démarrage de l'infrastructure Docker :
   - PostgreSQL (base de données)
   - InfluxDB (métriques JMeter)
   - Prometheus (métriques JVM)
   - Grafana (visualisation)
3. Démarrage des 3 services REST (ports 8081, 8082, 8083)
4. Pour chaque variante :
   - Exécution des 4 scénarios JMeter
   - Collecte des résultats dans results/{variante}/
5. Génération du résumé (results/summary.md)
```

### 4. Métriques collectées

#### Métriques JMeter (par requête)
- **Samples** : Nombre total de requêtes
- **RPS** : Requests Per Second (débit)
- **Latence** : p50, p95, p99 (percentiles)
- **Taux d'erreurs** : Pourcentage de requêtes échouées
- **Temps de réponse** : Temps moyen, min, max

#### Métriques JVM (via Prometheus)
- **CPU** : Utilisation processeur (%)
- **Heap** : Mémoire heap utilisée (MB)
- **GC** : Temps de garbage collection (ms/s)
- **Threads** : Nombre de threads actifs
- **HikariCP** : Pool de connexions (actives/max)

---

## 📊 Résultats

### État actuel

D'après l'analyse du projet, **les benchmarks ont été exécutés** mais les résultats détaillés ne sont pas encore complètement analysés dans le fichier `results/summary.md` (fichier vide actuellement).

### Structure des résultats

Les résultats sont organisés comme suit :

```
results/
├── summary.md                    # Résumé global (à générer)
├── varianta/                     # Résultats variante A (Jersey)
│   ├── read-heavy.jtl          # Résultats détaillés
│   ├── join-filter.jtl
│   ├── mixed.jtl
│   └── heavy-body.jtl
├── variantc/                     # Résultats variante C (Spring MVC)
│   └── ...
└── variantd/                     # Résultats variante D (Spring Data REST)
    └── ...
```

### Format des résultats attendus

Le script génère automatiquement un tableau comparatif :

| Service | Scenario | Samples | RPS | p50(ms) | p95(ms) | p99(ms) | Errors(%) |
|---------|----------|---------|-----|---------|---------|---------|-----------|
| varianta | read-heavy | ... | ... | ... | ... | ... | ... |
| variantc | read-heavy | ... | ... | ... | ... | ... | ... |
| variantd | read-heavy | ... | ... | ... | ... | ... | ... |

---

## 📈 Tableaux d'analyse (T0-T7)

Le projet prévoit 8 tableaux d'analyse détaillés :

### **T0** : Configuration matérielle & logicielle
- Machine (CPU, RAM)
- Versions (Java, Docker, PostgreSQL, JMeter, etc.)
- Paramètres JVM (Xms, Xmx, GC)
- Configuration HikariCP

### **T1** : Scénarios
- Définition des 4 scénarios de test

### **T2** : Résultats JMeter
- Comparaison RPS, latence, erreurs pour chaque variante

### **T3** : Ressources JVM
- CPU, mémoire, GC, threads, HikariCP par variante

### **T4** : Détails par endpoint (JOIN-filter)
- Performance détaillée des endpoints avec JOIN

### **T5** : Détails par endpoint (MIXED)
- Performance détaillée des opérations CRUD

### **T6** : Incidents / erreurs
- Analyse des erreurs rencontrées

### **T7** : Synthèse & conclusion
- Comparaison globale
- Recommandations d'usage

---

## 🔧 Comment exécuter le benchmark

### Méthode automatique (recommandée)

```bash
# 1. Rendre le script exécutable
chmod +x run_benchmark.sh

# 2. Lancer le benchmark complet
./run_benchmark.sh
```

**Durée estimée** : 30-60 minutes

### Méthode manuelle

Voir le fichier `GUIDE_EXECUTION.md` pour les étapes détaillées.

---

## 📦 Livrables du projet

Le projet comprend 5 livrables principaux :

1. **✅ Livrable 1** : Code des 3 variantes (A, C, D) - **COMPLET**
2. **✅ Livrable 2** : Fichiers JMeter (.jmx) + CSV - **COMPLET**
3. **⚠️ Livrable 3** : Dashboards Grafana + Exports - **À créer**
4. **⚠️ Livrable 4** : Tableaux T0-T7 + Analyse - **À remplir**
5. **⚠️ Livrable 5** : Recommandations d'usage - **À rédiger**

---

## 🎓 Conclusions attendues

Après analyse complète, le projet doit permettre de répondre à :

1. **Quelle variante est la plus performante pour les lectures relationnelles ?**
   - Impact des JOINs
   - Problèmes N+1 queries
   - Performance de la pagination

2. **Quelle variante est la plus performante pour l'écriture ?**
   - Gestion des transactions
   - Performance POST/PUT

3. **Quelle variante est la plus facile à exposer rapidement ?**
   - Temps de développement
   - Facilité de maintenance
   - Trade-off performance vs facilité

---

## 🔍 Points techniques importants

### Différences clés entre les variantes

| Aspect | Variante A (Jersey) | Variante C (Spring MVC) | Variante D (Spring Data REST) |
|--------|---------------------|-------------------------|-------------------------------|
| **Contrôleurs** | Manuels (`@Path`) | Manuels (`@RestController`) | Automatiques (repositories) |
| **Format réponse** | JSON standard | JSON standard | HAL (Hypertext Application Language) |
| **Pagination** | Manuelle | Spring Pageable | Spring Pageable (automatique) |
| **JOINs** | Gestion explicite | Gestion via JPA | Gestion via JPA (automatique) |
| **Complexité** | Moyenne | Faible | Très faible |
| **Contrôle** | Total | Élevé | Limité |

### Problèmes potentiels à analyser

1. **N+1 queries** : Chargement lazy des relations
2. **JOIN FETCH** : Optimisation des requêtes relationnelles
3. **Pagination** : Impact sur les performances
4. **Format HAL** : Overhead de sérialisation (variante D)
5. **Pool de connexions** : Configuration HikariCP

---

## 📝 Notes importantes

- Les 3 variantes utilisent la **même base de données** PostgreSQL
- Les tests sont exécutés **séquentiellement** (une variante à la fois) pour des mesures précises
- Les métriques sont collectées en **temps réel** via Prometheus et InfluxDB
- Les résultats sont **reproductibles** grâce à la configuration Docker

---

## 🚀 Prochaines étapes

1. **Exécuter le benchmark** si ce n'est pas déjà fait : `./run_benchmark.sh`
2. **Analyser les résultats** dans `results/summary.md`
3. **Créer les dashboards Grafana** pour visualisation
4. **Remplir les tableaux T0-T7** avec les données collectées
5. **Rédiger les recommandations** d'usage selon les résultats

---

## 📚 Documentation supplémentaire

- `README.md` : Vue d'ensemble
- `GUIDE_EXECUTION.md` : Guide d'exécution détaillé
- `LIVRABLES.md` : Description des livrables
- `tableaux_T0_T7.md` : Template des tableaux d'analyse
- `VERIFICATION_LIVRABLE2.md` : Vérification des fichiers JMeter

---

**Date de création** : Analyse du projet benchmark-rest  
**Auteur** : Résumé généré à partir de l'analyse du code

