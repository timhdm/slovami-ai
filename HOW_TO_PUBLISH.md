# Publishing Slovami AI

Repository: https://github.com/timhdm/slovami-ai

Developer and maintainer: Timur M. Omarov ([TimHdm](https://github.com/TimHdm)).

Slovami AI is an Android application built with Kotlin, Jetpack Compose and Gradle.
The GitHub repository already exists. Do not initialize a second Git repository
or push directly to main to publish changes.

## Development workflow

1. Create one task branch from the current main.
2. Make changes and build locally.
3. Run unit tests before opening a pull request.
4. Open a pull request into main and wait for required checks.
5. Complete the task with **Squash and Merge**.
6. Delete the task branch after merging.

For Windows builds, see [docs/BUILDING.md](docs/BUILDING.md). The source
tree includes `gradlew` for Git Bash and the Gradle Wrapper JAR.

## GitHub Actions setup

A fresh repository does not inherit GitHub Actions secrets.
To enable signed debug APK builds, set these repository secrets in
Settings -> Secrets and variables -> Actions:

- `DEBUG_KEYSTORE_BASE64`: base64-encoded contents of the signing keystore
- `DEBUG_KEYSTORE_PASSWORD`: password for that keystore
- `DEBUG_KEY_PASSWORD`: password for the signing key

The signing key alias is `slovami-ai-debug` in `app/build.gradle.kts`.
Keep the actual keystore and passwords out of Git and public documents.
Do not paste secret values into issues or pull requests.

The workflow `.github/workflows/android.yml` builds a signed debug APK;
pull requests also run unit tests. Manual workflow_dispatch can request tests.
The uploaded artifact is named `slovami-ai-debug-apk`.

For daily local builds and ADB installation, use `scripts/install-latest-debug.ps1`
with the required `-Branch` argument matching the current local branch. It builds
locally, requires a clean working tree and does not fetch workflow artifacts or
switch branches. See [the local workflow](slovami-ai-build-and-install-instructions.md).
Existing installations can be updated only with a matching signing key.

## Releases

1. Build and test the intended release version.
2. Create an appropriate version tag.
3. Generate a release-signed APK/AAB using the release signing key.
4. Create a GitHub Release in https://github.com/timhdm/slovami-ai/releases
   and attach the tested build and release notes.
5. Publish screenshots and a privacy policy when distributing more widely.

Do not upload a debug-signed build as a production release.

## Models and licensing

The app currently downloads some models from
https://huggingface.co/TimHdm/slovami-ai-models. This is an existing runtime
dependency, not the Slovami AI GitHub repository. Do not change this URL
until an independently hosted, verified model mirror is ready.

Review model licences and attribution in `models/CREDITS.md` and the
project's `LICENSE` before publication. Preserve required notices for
third-party components and pre-existing source code.
