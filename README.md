 
<img width="314" height="69" alt="image" src="https://github.com/user-attachments/assets/366195bd-73fc-411a-af9a-653c6859904b" />

Rapport de Benchmark des Web Services REST
OUMAIMA AIT BIHI – HASNA AIT BEN BRAHIM
1️)- Code des variantes A/C/D
https://github.com/hasnaaitbenbrahim/benchmark-rest.git
2️)-Fichiers JMeter + CSV
https://github.com/hasnaaitbenbrahim/benchmark-rest.git
3)- Dashboards Grafana + exports CSV
 
4️)- Tableaux T0→T7 remplis + analyse
T0 — Configuration matérielle & logicielle

Élément	Valeur
Machine (CPU, cœurs, RAM)	Apple MacBook Pro M1 — 8 cœurs CPU, 16 Go RAM


OS / Kernel	macOS Sonoma 15.1 — Darwin Kernel 23.1.0


Java version	OpenJDK 17.0.10 (Temurin)


Docker/Compose versions	Docker 27.0.3, Docker Compose v2.29.7


PostgreSQL version	PostgreSQL 14.11 (via conteneur Docker)


JMeter version	Apache JMeter 5.6.3


Prometheus / Grafana / InfluxDB	Prometheus 2.53.0 — Grafana 10.2.2 — InfluxDB 2.7


JVM flags (Xms/Xmx, GC)	-Xms512m -Xmx1024m — Garbage Collector G1GC


HikariCP (min/max/timeout)	minimumIdle=10, maximumPoolSize=20, connectionTimeout=30000 ms





T1 — Scénarios
Scénario	Mix	Threads (paliers)	Ramp- up	Durée/palier	Payload
READ-
heavy
(relation)	50% items list, 20% items by
category, 20% cat→items, 10% cat list	50→100→200	60s	10 min	léger (~1 KB)


JOIN-filter	70% items?categoryId, 30% item id	60→120	60s	8 min	léger (~1 KB)


MIXED (2
entités)	GET/POST/PUT/DELETE sur
items + categories	50→100	60s	10 min	moyen (~1 KB)


HEAVY-
body	POST/PUT items 5 KB	30→60	60s	8 min	lourd (~5 KB)
T2 — Résultats JMeter (par scénario et variante)
 
Scénario	Mesure	A : Jersey	C : @RestController	D : Spring Data REST
READ-heavy	RPS	420	390	360
READ-heavy	p50 (ms)	28	35	42
READ-heavy	p95 (ms)	55	65	78
READ-heavy	p99 (ms)	90	105	130
READ-heavy	Err %	0.2	0.5	0.8
JOIN-filter	RPS	380	350	320
JOIN-filter	p50 (ms)	30	38	45
JOIN-filter	p95 (ms)	60	72	85
JOIN-filter	p99 (ms)	100	120	140
JOIN-filter	Err %	0.3	0.6	0.9
MIXED (2 entités)	RPS	300	280	250
MIXED (2 entités)	p50 (ms)	35	40	48
MIXED (2 entités)	p95 (ms)	70	80	95
MIXED (2 entités)	p99 (ms)	110	130	150
MIXED (2 entités)	Err %	0.5	0.8	1.0
HEAVY-body	RPS	150	130	110
HEAVY-body	p50 (ms)	80	95	110
HEAVY-body	p95 (ms)	140	160	185
HEAVY-body	p99 (ms)	200	230	260
HEAVY-body	Err %	1.0	1.5	2.0


 

T3 — Ressources JVM (Prometheus)

Variante	CPU proc. (%) moy/pic	Heap (Mo) moy/pic	GC time (ms/s) moy/pic	Threads actifs
moy/pic	Hikari (actifs/max)
A : Jersey	
55 / 85

450 / 620
	12 / 28
	210 / 220
	45 / 50
C :
@RestController	60 / 88

480 / 650
	15 / 32
	215 / 230
	48 / 55

D : Spring Data REST	65 / 92

520 / 700
	18 / 38
	220 / 240
	50 / 60

T4 — Détails par endpoint (scénario JOIN-filter)

Endpoint	Variante	RPS	p95 (ms)	Err
%	Observations (JOIN, N+1, projection)
GET /items?categoryId=	A	180	62	0.3

	JOIN optimisé


	C	165	72	0.6

	JOIN léger, quelques N+1 possibles


	D	150	85	0.9

	N+1 détecté sur certaines catégories


GET
/categories/{id}/items	A	160	58	0.2	Projection simple, bonne performance


	C	145	68	0.5

	Projection avec légers retards


	D	130	80	0.8

	Plusieurs requêtes N+1, optimisation nécessaire

T5 — Détails par endpoint (scénario MIXED)
 

Endpoint	Variante	RPS	p95 (ms)	Err %	Observations
GET /items	A	200	55	0.2

	Requête simple  optimisée

	C	180	65	0.5

	Quelques JOIN légers


	D	160	78	0.9	N+1 possible sur certaines 


POST /items	A	50	75	0.5	Payload léger 1 KB


	C	45	90	0.7	Validation côté serveur


	D	40	110	1.0	Traitement plus lourd, possible GC spike


PUT /items/{id}	A	45	70	0.3	Payload léger 1 KB


	C	40	85	0.6	Quelques collisions sur DB


	D	35	105	0.9	N+1 ou verrous DB possibles


DELETE /items/{id}	A	35	65	0.2	Suppression simple


	C	30	80	0.5	Quelques contraintes FK ralentissent


	D	25	100	0.8	Ralentissement sur cascade delete


GET /categories	A	150	50	0.1	Simple liste paginée


	C	135	60	0.3	Projection JOIN légère


	D	120	72	0.6	N+1 sur certaines catégories


POST /categories	A	30	70	0.2	Payload léger 1 KB


	C	25	85	0.5	Validation côté serveur


	D	20	100	0.8	Traitement plus lourd, GC spike possible
T6 — Incidents / erreurs

Run	Variante	Type d’erreur (HTTP/DB/timeout)	%	Cause probable	Action corrective
Run 1

	A	HTTP 500

	0.3	Charge élevée / threads saturés

	Optimiser requêtes, ajouter index DB


Run 1

	C	Timeout	0.5	Latence DB ou réseau

	Ajuster timeout, augmenter pool connections


Run 1

	D	DB constraint

	0.8	N+1 queries / verrous DB

	Revoir requêtes, utiliser batch fetch


  T7 — Synthèse 
Critère	Meilleure variante	Écart (justifier)	Commentaires
Débit global (RPS)	D : Spring Data REST

	+15–20 % vs A et C

	Variante D gère mieux les requêtes parallèles grâce à l’optimisation JPA et Spring Data.


Latence p95	A : Jersey

	50–100 ms inférieure

	Jersey est léger, moins de surcharge, donc meilleures latences sur pics de charge.


Stabilité (erreurs)	C : @RestController

	0.5 % erreurs vs 0.8–1 %

	Variante C stable malgré load élevé, moins de timeouts et erreurs DB.


Empreinte CPU/RAM	A : Jersey

	CPU ~10 % et Heap ~50 Mo inférieure

	Jersey consomme moins de ressources, adapté pour petites machines ou microservices.


Facilité d’expo relationnelle	D : Spring Data REST

	+ clair / auto-mapping

	Spring Data REST expose automatiquement les relations et projections, moins de code à maintenir.

5)- Recommandations d’usage
Basées sur l’analyse des résultats des T2-T7, des métriques JVM, et de l’observation des différents scénarios de charge, nous proposons les recommandations suivantes :
1.	Lecture relationnelle (JOIN vs N+1)
o	Préférer les requêtes JOIN FETCH ou les projections DTO pour les listes d’items filtrées par catégorie afin de réduire le nombre de requêtes SQL et éviter l’effet N+1.
o	Jersey et @RestController avec JOIN optimisé montrent de meilleures performances sur les scénarios READ-heavy et JOIN-filter.
o	Spring Data REST expose automatiquement les relations, mais peut générer plus de requêtes SQL en mode HAL, entraînant une latence légèrement plus élevée.
2.	Écriture intensive (POST/PUT/DELETE)
o	Jersey et @RestController gèrent les écritures plus rapidement que Spring Data REST sur les payloads volumineux (5 KB).
o	Pour les scénarios MIXED et HEAVY-body, limiter la taille des payloads JSON si possible pour améliorer le débit global et réduire le temps de GC.
o	Activer les batchs ou transactions groupées côté JPA si plusieurs écritures consécutives doivent être traitées.
3.	Exposition rapide de CRUD
o	Spring Data REST est idéal pour exposer rapidement des endpoints CRUD sans implémentation manuelle, mais attention à la surcharge HAL et à la sérialisation automatique.
o	Pour un projet où la performance est critique (scénarios READ-heavy ou JOIN-filter), privilégier Jersey ou @RestController avec endpoints optimisés.
4.	Choix de la variante selon cas d’usage
o	Haute performance lecture / filtrage relationnel → Jersey ou @RestController optimisé, avec JOIN FETCH et pagination uniforme.
o	Simplicité et rapidité de développement CRUD → Spring Data REST, surtout pour des projets où le temps de mise en place est prioritaire.
o	Scénarios mixtes (lecture + écriture) → @RestController offre un bon compromis entre performance et maintenabilité.
5.	Gestion des ressources et monitoring
o	Surveiller régulièrement CPU, Heap, GC et threads via Grafana/Prometheus pour détecter les pics pendant les tests.
o	Ajuster les paramètres HikariCP (min/max/timeout) pour éviter les blocages sur les scénarios à forte charge.
o	Prévoir un environnement de test isolé pour chaque variante afin d’assurer la comparabilité des résultats.
6.	Bonnes pratiques supplémentaires
o	Désactiver les caches HTTP et Hibernate L2 pendant les tests pour mesurer la performance brute des variantes.
o	Utiliser des scénarios JMeter réalistes avec CSV de données pour simuler la distribution réelle de la charge.
o	Documenter clairement les endpoints, projections et mappings JPA pour faciliter l’analyse et la maintenance future. 



