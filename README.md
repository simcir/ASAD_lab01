# ASAD : Lab 1
## Consigne : « Architecture Based »
On veut développer un jeu de « devinettes » point à point au sein d’un groupe d’utilisateurs. À tout moment, un membre peut envoyer une « devinette » à un autre membre, même si celui-ci a déjà d’autres « devinettes » en cours. Un participant peut répondre à une « devinette » tant que le délai le permet. Au bout d’un timeout, la devinette est supprimée. Aucune gestion centralisée n’est autorisée. Même les annuaires des Users doivent être des objets réparties.
Outil suggéré : Java RMI.
## Lancer le programme
```bash
cd riddle-exchanger
mvn package
mvn exec:java
```
