# Android Contact & Messaging App (Kotlin)

Petit squelette de projet Android destiné à être utilisé comme point de départ pour le projet de gestion de contacts et d'échange de SMS.

## Pourquoi ouvrir ce projet dans Android Studio
- Le code peut être édité ici, mais pour compiler/exécuter il faut le SDK Android et Gradle.
- La méthode recommandée : ouvrir ce dossier (`android-contact-app`) dans Android Studio.

## Prérequis
- Android Studio (4.2+ recommandé) avec SDK Android (API 33) installé.
- JDK 17 ou supérieur (le projet compile avec Java/Kotlin target 17).

Remarque: le projet cible Java/Kotlin 17 dans `app/build.gradle` (`sourceCompatibility` / `jvmTarget` = 17). Si tu utilises JDK 11, adapte `app/build.gradle` ou installe JDK 17 pour éviter des incompatibilités.

## Importer et lancer
1. Ouvrir Android Studio → `Open` → sélectionner le dossier `android-contact-app`.
2. Laisser Gradle télécharger les dépendances.
3. Brancher un appareil Android ou utiliser un émulateur, puis Run.

Si tu préfères la ligne de commande (avancé) :

```bash
# depuis le dossier android-contact-app
./gradlew assembleDebug
# ou pour installer sur appareil connecté
./gradlew installDebug

Remarques & diagnostics fréquents
- Si `./gradlew test` affiche un avertissement :
	- "Setting the namespace via the package attribute..." → supprime l'attribut `package` de `app/src/main/AndroidManifest.xml` et laisse Gradle gérer le `namespace` via `app/build.gradle` (champ `namespace 'com.example.contactapp'`).
	- Observed package id 'platforms;android-33' in inconsistent location → vérifier l'emplacement du SDK Android (variable `sdk.dir` dans `local.properties`) et s'assurer que le répertoire `platforms/android-33` existe (ou renommer `android-33-2` si besoin).

- Pour l'installation (`installDebug`) : erreur `No connected devices!` signifie qu'aucun appareil/emulateur n'est connecté. Utilise :

```bash
# lancer un émulateur (exemple AVD nommé "Pixel_4_API_33")
emulator -avd Pixel_4_API_33
# puis installer
./gradlew installDebug
```

- Pour tester la réception SMS sur un émulateur :
```bash
adb emu sms send +33612345678 "Test message"
```

- Le code utilise `SmsManager.getDefault()` qui est marqué déprécié sur certaines versions ; c'est fonctionnel mais peut nécessiter une API plus moderne selon la cible Android.

Fichier ProGuard
- J'ai ajouté `app/proguard-rules.pro` (fichier minimal) puisque `app/build.gradle` référence `proguard-rules.pro` pour les builds release. Ajuste ou enrichis les règles si tu utilises des bibliothèques qui demandent des exceptions spécifiques.
```

Remarque : ce squelette contient des fichiers Gradle de base. J'ai ajouté un petit script `gradlew` et `gradle/wrapper/gradle-wrapper.properties` en tant que placeholders — Android Studio peut générer le wrapper complet (y compris `gradle-wrapper.jar`) si nécessaire. J'ai aussi ajouté une icône adaptative placeholder dans `app/src/main/res/mipmap-anydpi-v26` : remplace-la par le logo officiel 42.

## Contenu important
- `app/src/main/java/com/example/contactapp/MainActivity.kt` — point d'entrée minimal
- `app/src/main/java/com/example/contactapp/Contact.kt` — modèle de données de contact
- `app/src/main/res/values/strings.xml` et `values-fr/strings.xml` — i18n (anglais/français)
- `app/src/main/AndroidManifest.xml` — permissions SMS déclarées

## Prochaines étapes recommandées (MVP)
- Implémenter SQLite (`DatabaseHelper`) et CRUD contacts.
- UI : `RecyclerView` pour la liste, `Activity` ou `Fragment` pour fiche contact.
- Envoi / réception SMS (BroadcastReceiver et runtime permissions).
- Sauvegarder timestamp onPause/onStop et l'afficher en Toast au retour.
- Menu pour changer la couleur d'en-tête et support paysage/portrait.

Si tu veux, je peux :
- Générer `DatabaseHelper` + DAO simple en Kotlin.
- Ajouter un `RecyclerView` et un adaptateur de départ.
- Créer le manifest et le code de `BroadcastReceiver` pour SMS (avec gestion permissions).

Dis-moi quelle partie tu veux que je génère ensuite.

## Remarques pratiques

- Changer l'icône de l'app : remplace les fichiers `mipmap-*` et l'adaptive icon dans `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` par tes images (ou utilise Android Studio: `Image Asset` > `Launcher Icons`). Fournis des PNG 48/72/96/144/192/512 ou un SVG et laisse Android Studio générer les variantes. Mettez le logo 42 dans ces emplacements pour qu'il apparaisse comme icône.

- Envoi / réception SMS depuis un appareil sans SIM : si l'application tourne sur un appareil physique, recevoir des SMS nécessite que l'appareil (le téléphone cible) soit joignable via le réseau téléphonique (SIM ou service opérateur supportant SMS via Wi‑Fi). Si ton appareil n'a pas de SIM, il ne recevra normalement pas de SMS classiques. Pour les tests :
	- Sur un émulateur Android tu peux simuler la réception : `adb emu sms send <num> "message"`.
	- Si l'app est installée sur un autre téléphone réel avec SIM, tu peux envoyer un SMS depuis ton téléphone vers ce numéro et l'app recevra le message.
	- Pour l'envoi depuis l'app (`SmsManager`), le téléphone qui exécute l'app doit pouvoir envoyer des SMS (SIM ou service opérateur). Sur émulateur, l'envoi peut fonctionner selon la configuration de l'émulateur.

	## Tests et dépôt Git

	- Emplacement des tests :
		- Tests unitaires (rapides, JVM) : `app/src/test/java`
		- Tests instrumentés (androidTest, exigent un appareil/emulateur) : `app/src/androidTest/java`

	- Exécuter les tests locaux :
	```bash
	./gradlew test            # lance les tests unitaires JVM
	./gradlew connectedAndroidTest  # lance les tests instrumentés sur appareil/emulateur connecté
	```

