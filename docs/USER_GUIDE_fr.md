[简体中文](USER_GUIDE_zh.md) | [繁體中文](USER_GUIDE_zh_TW.md) | [English](USER_GUIDE_en.md) | [日本語](USER_GUIDE_ja.md) | [한국어](USER_GUIDE_ko.md) | [Deutsch](USER_GUIDE_de.md) | **Français** | [Español](USER_GUIDE_es.md) | [Русский](USER_GUIDE_ru.md) | [Português](USER_GUIDE_pt.md)

# WatchSend — Guide d’utilisation

> Transfert de fichiers sur la montre · Manuel

Valable pour la version 1.8.2 (versionCode 10802)   novembre 2026

## 1 À propos de ce guide

Ce guide s’adresse aux utilisateurs de WatchSend : installation, connexion, envoi et réception, et effet réel de chaque option de l’écran Réglages. Les libellés de boutons et de menus reprennent la langue d’affichage choisie. Après un changement de langue, lisez la version dans cette même langue.

Il ne traite que de l’usage, pas de la compilation, du packaging ni des modifications. Chaîne d’outils, règles de contribution et maintenance des paquets de langue figurent dans CONTRIBUTING et le dossier docs du dépôt. Les descriptions reposent sur l’implémentation 1.8.2 ; l’environnement de compilation cloud ne peut pas lancer d’émulateur, le ressenti des gestes est donc à juger sur un appareil réel.

## 2 Installation et premier lancement

WatchSend est distribué en APK, fonctionne à partir d’Android 2.3 (API 10) et s’installe sur une montre ou sur un téléphone jouant le rôle d’émetteur ou de récepteur. Sur une montre, l’installation passe le plus souvent par adb ou par une application compagnon sur le téléphone. Il suffit ensuite d’ouvrir WatchSend depuis la liste des applications ; ni compte ni connexion ne sont requis.

Au premier démarrage, l’application demande l’accès aux notifications et au stockage. Les notifications servent à signaler les fichiers reçus et la progression en arrière-plan, le stockage à écrire dans la galerie ou le dossier d’historique. Un refus ne fait pas planter l’application, mais supprime les alertes en arrière-plan et peut faire échouer certains chemins de réception ; accepter les deux est conseillé.

En haut de l’écran d’accueil se trouve l’interrupteur du service, avec « Démarrer » ou « Arrêter », l’état et l’adresse locale. Service arrêté, les autres appareils ne vous trouvent pas. Un appui sur Démarrer ouvre un service HTTP sur le port 53317 et lance la découverte multicast périodique : c’est alors que l’échange devient possible.

## 3 Écran et commandes de base

L’écran d’accueil présente de haut en bas l’état et l’adresse, la liste des appareils, puis les entrées Envoyer, Historique de réception, Cross-Net, Réglages et Journal. La liste défile de deux façons : à partir de Wear OS 3 avec la couronne, ou au doigt. Pendant le défilement, les cartes des bords rétrécissent et s’estompent pour indiquer la position.

- Un appui sur une carte ouvre le niveau suivant. Le bouton Retour en bas de page ou le retour système quitte la page courante.
- Le retour par glissement interne est actif par défaut : glissez horizontalement depuis le bord, la page suit le doigt, rétrécit, s’efface et revient. Si vous préférez le retour système, désactivez Réglages – Général – Activer le retour intégré ; le retour système reprend son cours et le glissement ne navigue plus.
- L’échelle de l’interface règle toute l’application entre 0,3x et 2,5x. Sur une montre ronde, c’est le premier réglage à essayer quand le texte semble à l’étroit.
- Les montres rondes gagneront à activer l’optimisation de l’écran rond ; celles avec un menton, comme la Moto 360, activeront aussi le retrait du bas pour éviter les boutons coupés.

## 4 Envoyer des fichiers

L’envoi suit quatre étapes fixes : choisir le contenu, choisir l’appareil, confirmer, attendre la progression. La sélection reste modifiable en revenant en arrière, les éléments choisis demeurent dans la liste d’attente.

1. Touchez Envoyer sur l’accueil ; le menu des types s’ouvre : Fichier, Média, Application, Dossier, Texte, Presse-papiers.
2. Sélectionnez les éléments. Fichiers et médias acceptent plusieurs choix, applications et dossiers un seul, texte et presse-papiers prennent le contenu courant.
3. Revenez à la liste des appareils et touchez la cible. La contrepartie doit avoir démarré son service et se trouver sur le même réseau, ou déjà configurée pour le Cross-Net.
4. Attendez la fin de la barre. Le succès affiche un message et vibre, l’échec donne un code d’erreur, détaillé dans le Journal.

Le texte part en message texte LocalSend et s’affiche chez le destinataire en notification ou conversation. Le presse-papiers envoie son contenu courant comme un bloc de texte, jamais une image. L’écran reste allumé pendant un gros transfert puis revient à l’économie d’énergie.

## 5 Recevoir des fichiers

Tant que le service tourne, les appareils du même réseau vous trouvent et vous envoient. Une page de confirmation précède la réception, avec le nom de l’émetteur, le nombre de fichiers et la taille totale. Accepter ou refuser est possible ; un refus prévient aussitôt l’émetteur au lieu de le faire patienter.

« Enregistrement rapide » décide si cette confirmation est sautée, sur trois niveaux : demander chaque fois, tout accepter automatiquement, n’accepter que les favoris. Le défaut est de demander chaque fois ; gardez-le tant que les émetteurs ne sont pas sûrs.

Les gros fichiers reprennent : après une coupure, un nouvel envoi continue à partir des octets déjà écrits au lieu de repartir de zéro. Les photos et vidéos reçues peuvent aller en galerie, le reste est écrit dans l’historique ; les deux se désactivent séparément.

## 6 Interface web et transfert Cross-Net

### 6.1 Interface web

Service lancé, ouvrez dans un navigateur du même LAN l’adresse affichée sur l’accueil, par exemple http://192.168.1.100:53317. Une page minimale apparaît : glisser-déposer des fichiers, suivre la progression par fichier, échanger du texte du presse-papiers avec la montre. Un appui sur l’adresse la copie ; « URL web » affiche un QR code à scanner avec le téléphone.

La page web suit la langue du navigateur et en embarque 10 : chinois simplifié et traditionnel, anglais, japonais, coréen, allemand, français, espagnol, russe, portugais. L’application en couvre 52 ; sans correspondance, la page revient à l’anglais.

### 6.2 Cross-Net

Hors d’un même LAN, deux voies existent. Mode mot de passe : un côté génère un code à usage unique de six chiffres, l’autre le saisit dans l’écran Cross-Net, sans IP publique. Mode direct : exposez le port via FRP, Ngrok ou ZeroTier puis saisissez IP:53317 dans le champ manuel ; le débit dépend du tunnel.

Le Cross-Net dépend d’une infrastructure tierce et de votre réseau, la stabilité n’est pas garantie par l’application. Si la connexion ne s’établit pas, vérifiez d’abord dans le Journal que l’adresse et le port sont joignables, puis que le tunnel est en ligne.

## 7 Historique

L’historique de réception liste les fichiers et textes échangés du plus récent au plus ancien, les archives portant un repère visible. Un appui long ouvre le menu d’actions ; un appui simple affiche le détail ou le fichier.

- Renvoyer remet les fichiers de l’entrée dans la liste d’attente ; revenez à la liste des appareils et touchez-en un, sans reparcourir les dossiers.
- Partager confie le fichier à une autre application via le partage système.
- Extraire ne concerne que les archives et utilise le moteur Java pur intégré, avec protection contre les traversées de chemin : rien n’est écrit hors du dossier cible.
- Supprimer retire l’entrée ; les fichiers déjà enregistrés en galerie ne sont pas touchés.

## 8 Réglages

Les réglages sont groupés comme ci-dessous. Les valeurs par défaut décrivent une installation neuve jamais modifiée ; après modification, c’est la valeur affichée qui compte.

**Tableau 1  Réglages en un coup d’œil**

| Groupe | Option | Défaut | Effet |
| --- | --- | --- | --- |
| Général | Thème | Suivre le système | Suivre le système, forcer clair, forcer sombre |
| Général | Langue | Suivre le système | 52 langues ; debug_keys en bas de liste affiche les clés brutes pour le diagnostic |
| Général | Échelle de l’interface | Moyenne (1,0x) | Règle texte et contrôles entre 0,3x et 2,5x |
| Général | Forme d’écran | Auto | Détecte montre ronde ou carrée, peut être forcée |
| Général | Retrait du bas | Non | Remonte le contenu pour les montres rondes à menton, comme la Moto 360 |
| Général | Couleur dynamique | Oui | Suit le fond d’écran ou le cadran (Android 12 et plus) |
| Général | Activer le retour intégré | Oui | Oui bloque le retour système et utilise le glissement ; non rétablit le retour système et n’intercepte plus le glissement |
| Général | Mode faible performance | Auto | Réduit ou coupe les animations ; sans choix manuel, décidé par la mémoire libre et les cœurs |
| Général | Suspendre le service inactif | Non | Après 3 minutes écran éteint, la réception se suspend pour économiser l’énergie et reprend au réveil ; un envoi entrant peut être manqué |
| Transfert | Enregistrement rapide | Demander chaque fois | Demander chaque fois, tout accepter, favoris seulement |
| Transfert | Appareils favoris | Vide | Mémorise les appareils fréquents, combiné avec l’option précédente |
| Transfert | Enregistrer en galerie | Oui | Place les photos et vidéos reçues dans la galerie |
| Transfert | Enregistrer dans l’historique | Oui | Écrit le contenu reçu dans l’historique |
| Réseau et automatisation | Activer HTTPS | Non | Transfert en TLS ; les appareils anciens ne gèrent que TLS 1.0 et peuvent échouer |
| Réseau et automatisation | Port | 53317 | Port du service ; après changement, émetteur et récepteur doivent correspondre |
| Réseau et automatisation | Activer le transfert web | Oui | Autorise le navigateur à joindre l’adresse locale du service |
| Réseau et automatisation | URL web | — | Affiche l’adresse et un QR code, lien copiable |
| Réseau et automatisation | Partage de téléchargement | Non | Autorise les appareils du LAN à récupérer des fichiers du dossier local via LocalSend |
| Réseau et automatisation | Démarrage automatique | Non | Démarre le service au boot ; adapté à un récepteur permanent |
| Appareil et sécurité | Renommer l’appareil | Modèle | Nom vu par les autres appareils |
| Appareil et sécurité | Activer le PIN | Non | L’émetteur doit saisir le PIN avant d’envoyer |
| Appareil et sécurité | Modifier le PIN | 0000 | Quatre chiffres ; choisissez une valeur connue de vous seul |
| Appareil et sécurité | Bloquer les extensions dangereuses | exe,bat,cmd,vbs,sh | Les fichiers portant ces extensions sont refusés |
| Appareil et sécurité | Listes blanche et noire | Vide | Appui long sur un appareil pour le bloquer ou l’ajouter à la liste blanche |
| Mode geek | Installation silencieuse (bêta) | Non | Installe automatiquement un APK reçu via Root ou Shizuku |
| Autre | Sauvegarde / restauration | — | Exporte les réglages dans le presse-papiers ou les importe |
| Autre | À propos de WatchSend | — | Version, protocole et remerciements |

## 9 Problèmes courants

**Tableau 2  Symptômes et solutions**

| Symptôme | Solution |
| --- | --- |
| La liste reste sur « aucun appareil, touchez scanner » | Vérifiez que l’autre côté a démarré son service et partage le même Wi-Fi ; désactivez l’isolation AP du routeur ; relancez le scan ; sinon saisissez l’IP à la main |
| Chaque envoi de l’autre côté renvoie 400 | Souvent un champ de protocole ou un conflit HTTPS : coupez HTTPS des deux côtés, ou alignez les deux sur la même version puis réessayez et lisez le code dans le Journal |
| Le navigateur n’ouvre pas la page web | Vérifiez le service lancé, le port libre et le même sous-réseau ; certains routeurs bloquent le trafic entre appareils |
| Le bouton Retour ne réagit pas ou affiche seulement un message | Le retour intégré est actif ; désactivez-le et le retour système reprend |
| Texte ou boutons coupés par le bord rond | Activez l’optimisation de l’écran rond, ajoutez le retrait du bas si besoin et baissez l’échelle d’un cran |
| Rien n’arrive après un moment écran éteint | Vérifiez que « Suspendre le service inactif » n’est pas actif (défaut non) et que le système n’optimise pas la batterie de WatchSend |
| La batterie fond pendant un transfert | Activez le mode faible performance ; sous 15 % et sans charge, l’application bride déjà à 512 Ko/s |

## 10 Sécurité, énergie et limites connues

- Le HTTP en clair est le défaut et ne convient qu’aux LAN de confiance. Pour chiffrer, activez HTTPS et vérifiez l’empreinte du certificat de l’autre côté.
- Le PIN vaut 0000 par défaut ; mettez vos quatre chiffres. Le Cross-Net utilise un code à usage unique de six chiffres, invalidé après usage.
- Les extensions dangereuses exe, bat, cmd, vbs, sh sont bloquées par défaut ; listes blanche et noire se gèrent par appui long sur un appareil.
- L’économie d’énergie a trois niveaux : le mode faible performance coupe la plupart des animations et plafonne à 2 Mo/s ; sous 15 % de batterie sans charge, à 512 Ko/s ; après 3 minutes écran éteint, le service se suspend (désactivé par défaut, reporté pendant un transfert).
- Limites connues : le Cross-Net dépend de services tiers, la stabilité n’est pas garantie ; l’installation silencieuse dépend de la ROM ; certaines montres anciennes n’ont ni moteur vibrant ni couronne ; extraction et gestes n’ont été vérifiés que sur de petits écrans réels.
- Le projet est sous licence MIT, réalisé avec l’aide d’une IA puis revu et réglé sur appareil ; fourni en l’état, sans garantie d’adéquation.

---

*WatchSend · MIT License · https://github.com/styisme/watchsend*
