# DEVELOPPEMENT CONTINU/INTEGRATION CONTINUE
## Adeel Ahmad


## Application de Messagerie Sécurisée et Collaborative
## Spring boot - micro-services 
#### API : RESTful API pour les communications avec le frontend


Le projet consiste à développer une Application de Messagerie sécurisée et orientée groupes en utilisant le framework Spring Boot (avec Spring Security pour l'authentification/autorisation) et une base de données relationnelle MySQL pour la persistance.

L'objectif principal est de permettre aux utilisateurs enregistrés d'échanger des messages individuels et de participer à des discussions au sein de groupes exclusifs.

### Tâche 1 : Gestion des Utilisateurs et Sécurité

Authentification : Les utilisateurs doivent pouvoir s'enregistrer et se connecter à l'application. (Utilisation de Spring Security).

Profil Utilisateur : Chaque utilisateur a un nom d'utilisateur unique, un mot de passe haché, et d'autres informations de profil (nom, prénom, etc.).

### Tâche 2 : Gestion des Groupes

Création de Groupe : Un utilisateur peut créer un nouveau groupe.

Appartenance Unique : La règle stricte est qu'un utilisateur ne peut faire partie que d'un seul groupe à la fois.

Gestion des Membres : Ajouter/retirer des membres du groupe (possiblement par le créateur/administrateur du groupe).

### Tâche 3 : Fonctionnalités de Messagerie

Messagerie Individuelle (Direct Message - DM) : Un utilisateur peut envoyer un message privé à un autre utilisateur. Ces messages sont gérés dans la boîte de réception privée de chaque utilisateur.

Messagerie de Groupe : Un utilisateur peut envoyer un message au groupe dont il est membre. Ce message est visible par tous les membres du groupe et sert de fil de discussion commun.

Réception des Messages :

Boîte de Réception Personnelle (DM) : Pour gérer les messages individuels reçus.

Boîte de Réception de Groupe (Commune) : Un espace de discussion commun pour les messages envoyés au groupe. Chaque membre du groupe accède à la même liste de messages pour ce groupe. La réponse à un message reçu dans le groupe est une réponse au groupe entier.


## Modélisation des Entités (Structure de la Base de Données)

Voici les structures d'entités (modèles de domaine/tables) proposées pour ce projet, qui seront mappées en classes Spring Data JPA.


### Entité *User*

Représente un utilisateur de l'application.


- `id	Long`	Clé primaire	
- `username	String`	Nom d'utilisateur unique (pour la connexion)
- `password	String`	Mot de passe haché	
- `firstName	String`	
- `lastName	String`		
- `group	Group`	Le groupe auquel l'utilisateur appartient, ManyToOne vers Group; on considère que plusieurs users appartiennent à un group.


### Entité *Group*

Représente un groupe de discussion.

- `id	Long`	Clé primaire	
- `name	String`	Nom du groupe (unique)	
- `creator	User`	L'utilisateur qui a créé le groupe	`OneToOne` vers User
- `members	Set<User>`	La liste des utilisateurs dans ce groupe	`OneToMany` vers User
- `groupMessages	Set<Message>`	L'historique des messages du groupe	`OneToMany` vers Message


### Entité *Message* 

Représente un message envoyé, qu'il soit individuel ou de groupe.

- `id	Long`	Clé primaire	
- `content	String`	Le contenu du message	
- `timestamp	Date/LocalDateTime`	Date et heure d'envoi
- `sender	User`	L'expéditeur du message	ManyToOne vers User
- `receiverUser	User`	Le destinataire (pour les DM)	`ManyToOne` vers User (nullable)
- `receiverGroup	Group`	Le groupe destinataire (pour les messages de groupe)	`ManyToOne` vers Group (nullable)


