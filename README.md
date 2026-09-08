# Android Contact & Messaging App (Kotlin)

Application Android complète pour la gestion de contacts et l'échange de SMS, synchronisée avec le système.

## Fonctionnalités Implémentées

### 👤 Gestion des Contacts
- **CRUD Complet** : Création, lecture, modification et suppression de contacts.
- **Favoris** : Possibilité d'épingler des contacts en favoris (affichés en haut de liste).
- **Photos de profil** : Support des photos de profil avec copie locale sécurisée pour éviter les problèmes de permissions Android.
- **Fusion Intelligente** : Détection automatique des doublons par nom avec proposition de fusion des données (téléphones, emails, notes).

### 💬 Messagerie & SMS
- **Synchronisation Système** : L'application lit et écrit directement dans la base SMS officielle d'Android (`content://sms/`). Tout message envoyé ou reçu ailleurs apparaît ici.
- **Mises à jour en temps réel** : Utilisation d'un `ContentObserver` pour rafraîchir la conversation instantanément lors de la réception d'un message.
- **Interface "Chat"** : Affichage des messages sous forme de bulles stylisées (grises pour les reçus, vertes pour les envoyés).
- **Formatage des dates** : Affichage intelligent de l'heure et de la date.

### 🔍 Recherche & Navigation
- **Recherche Globale** : Barre de recherche sur l'écran principal pour filtrer les contacts par nom ou numéro.
- **Recherche In-App** : Recherche par mots-clés à l'intérieur d'une conversation spécifique.
- **Notifications** : Notification système lors de la réception d'un SMS d'un nouveau numéro avec option de création rapide de contact.

---

## Pourquoi ouvrir ce projet dans Android Studio
- La méthode recommandée : ouvrir ce dossier (`android-contact-app`) dans Android Studio.
- Le projet utilise Gradle pour la gestion des dépendances et du build.

## Prérequis
- **Android Studio** (Flamingo ou plus récent recommandé).
- **SDK Android** (API 33 installé).
- **JDK 17** : Le projet est configuré pour Java/Kotlin 17 (`jvmTarget = 17`).

## Installation et Test
1. Ouvrir Android Studio → `Open` → sélectionner le dossier `android-contact-app`.
2. Connecter un émulateur ou un appareil physique.
3. Cliquer sur **Run** (Flèche verte).

### Simuler un SMS (Émulateur)
Pour tester la réception sans téléphone physique, utilisez la console ADB :
```bash
adb -s <emulator_id> emu sms send +33612345678 "Message de test"
```

---

## Architecture Technique
- **Pattern** : MVVM (Model-View-ViewModel).
- **Base de données** : SQLite (via `SQLiteOpenHelper`) avec migrations gérées (Version actuelle : 4).
- **Data Access** : Repository Pattern pour abstraire les sources de données (Local DB pour les contacts, ContentProvider pour les SMS).
- **Permissions** : Gestion dynamique des permissions (SMS, Contacts, Stockage).

## Contenu du projet
- `MainActivity.kt` : Liste des contacts et recherche.
- `ContactEditActivity.kt` : Formulaire de création/édition, gestion des photos et fusion.
- `ConversationActivity.kt` : Historique des messages synchronisé et envoi de SMS.
- `SmsReceiver.kt` : Interception des messages entrants et gestion des notifications.
- `ContactRepository.kt` & `SystemMessageRepository.kt` : Couches d'accès aux données.

---

## Remarques sur le développement
- **Sécurité des images** : Les photos choisies dans la galerie sont copiées dans le stockage interne de l'app (`filesDir`) pour garantir un accès permanent.
- **Base de données** : La version 4 de la DB nettoie automatiquement les anciens liens de photos invalides pour éviter les crashs.
- **Permissions SMS** : L'application nécessite les droits de lecture/écriture SMS pour fonctionner comme client secondaire.
