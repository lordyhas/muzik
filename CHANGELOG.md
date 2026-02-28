# CHANGELOG

## 2026-02 - Modernisation technique (sans changement produit)

### 1) Audit initial — Problèmes → Actions
- **Toolchain Android ancienne (AGP 7.1.2, wrapper Gradle ancien, script Gradle legacy)**
  → Migration vers la configuration Gradle moderne (plugins DSL), AGP 8.5.2 et Gradle 8.7.
- **Configuration Android partiellement obsolète (package dans `AndroidManifest`, pas de `namespace`)**
  → Suppression de `package` dans les manifests et ajout de `namespace` dans `android/app/build.gradle`.
- **Compatibilité Android 13+ média/notification incomplète**
  → Conservation/clarification des permissions media (`READ_MEDIA_AUDIO`) et ajout de `POST_NOTIFICATIONS`.
- **Dépendances hétérogènes / versions flottantes (`any`)**
  → Passage à des contraintes explicites et récentes compatibles Flutter stable moderne.
- **Lints datés (`flutter_lints` 1.x) et configuration analyse non moderne**
  → Migration vers `flutter_lints` 5.x + règles de qualité adaptées.
- **Code d’initialisation app avec dette technique (orientation dans `build`, code mort, imports inutiles)**
  → Refactor ciblé de `main.dart` pour stabilité et clarté sans changement fonctionnel.
- **Usage de propriété Flutter dépréciée (`ThemeData.backgroundColor`)**
  → Remplacement par `Theme.of(context).colorScheme.surface`.

### 2) Mise à jour toolchain Flutter/Android
- Migration des scripts Gradle vers le modèle `plugins {}`.
- AGP mis à jour en **8.5.2**.
- Kotlin Gradle plugin mis à jour en **1.9.24**.
- Gradle wrapper mis à jour en **8.7**.
- Java/Kotlin target mise à **17**.
- `namespace` ajouté côté module Android app.

### 3) Mise à jour des dépendances
- Passage des dépendances principales à des versions explicites récentes (voir `pubspec.yaml`).
- Remplacement des contraintes `any` par des versions figées quand possible.

### 4) Refactor breaking/deprecations
- Refactor de `main.dart`:
  - orientation initialisée avant `runApp`,
  - initialisation `just_audio_background` clarifiée,
  - nettoyage du code inutilisé,
  - simplification des blocs providers.
- Migration des usages `Theme.of(context).backgroundColor` vers `colorScheme.surface`.

### 5) Qualité / bonnes pratiques
- `analysis_options.yaml` modernisé:
  - exclusion des fichiers générés,
  - lints supplémentaires (`avoid_print`, `directives_ordering`, `require_trailing_commas`, etc.).

### 6) Exécution locale (run/build)
Dans cet environnement de modernisation, **Flutter/Dart n’étaient pas installés**. Les commandes ci-dessous sont à lancer sur votre machine:

```bash
flutter --version
flutter pub get
flutter analyze
flutter test
flutter build apk
```

Si besoin release bundle:

```bash
flutter build appbundle
```

### Points potentiellement restants à valider chez vous
- Vérifier la compatibilité exacte de toutes les versions de plugins avec votre version Flutter stable installée.
- Valider les permissions runtime Android 13+ (notification + media) sur un appareil réel.
- Re-générer les artefacts ObjectBox si nécessaire:

```bash
flutter pub run build_runner build --delete-conflicting-outputs
```
