# Correcteur local — Android

Prototype Android natif Kotlin / Jetpack Compose / Material 3 avec intégration directe de l'API Java LanguageTool.

## Fonctionnalités du prototype
- Éditeur de texte
- Choix français / anglais
- Analyse LanguageTool sur un thread IO
- Liste des problèmes et suggestions
- Application d'une suggestion
- Aucune permission Internet déclarée dans le manifeste

## Ouvrir
1. Ouvrir ce dossier dans Android Studio.
2. Utiliser JDK 17.
3. Laisser Gradle synchroniser les dépendances.
4. Lancer sur un appareil ou émulateur Android 8.0+.

## Important : validation Android nécessaire
La bibliothèque LanguageTool documente son API Java et indique Java 17 depuis la version 6.6. Sa documentation recommande toutefois le serveur HTTP plutôt que l'API Java directe. Cela ne constitue pas une garantie de compatibilité Android : certaines dépendances transitives peuvent utiliser des API Java absentes d'Android ou dépasser les contraintes mémoire/taille d'une application mobile.

Ce projet est donc un **prototype source**, pas un APK testé. La prochaine étape technique est de compiler, résoudre les éventuelles incompatibilités Android des dépendances, puis tester la mémoire, les performances et le fonctionnement en mode avion sur appareil réel. Ne pas ajouter de permission INTERNET : le but est de conserver le traitement local.

## Langues
Le prototype inclut les modules `language-fr` et `language-en`. D'autres modules LanguageTool peuvent être ajoutés selon la compatibilité et la taille finale de l'APK.

## Licence
LanguageTool Core est distribué sous LGPL 2.1 ou ultérieure. Vérifier les obligations de redistribution et conserver les notices/licences correspondantes lors de toute distribution de l'application.
