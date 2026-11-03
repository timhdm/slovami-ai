# Slovami AI model hosting migration

Status: PREPARATION ONLY. The Android app still downloads from the existing source.
No URLs should be switched until the complete mirror passes verification.

## Source and scope

Source model repository: https://huggingface.co/protocolvoice/asr-models

`ModelRegistry.kt` lists nine current downloadable files with nonzero SHA-256:
Russian GigaAM, CAM++, ERes2Net V1/V2, Whisper EN encoder/decoder/tokens,
Navec, and Slovnet NER. They remain unmodified when mirrored.

QVikhr is NOT included: its registered SHA-256 is all zeros and its weights
are not hosted in the existing source repository. Handle it separately.

Every model retains its upstream license and required attribution.
Review `models/CREDITS.md` and upstream model cards before publishing.
Do not label existing model weights as newly trained Slovami AI models.

## On a Windows computer

Install the Hugging Face client:

```powershell
py -m pip install --upgrade huggingface_hub
hf auth login
```

Create a separate PUBLIC Hugging Face *model* repository under an account
you control, for example `YOUR_ACCOUNT/slovami-ai-models`.
Public access is required for anonymous first-run downloads.

From the Git checkout, first download and verify the files (stored outside
the Git checkout by default):

```powershell
py tools/mirror-models.py
```

If all nine checksums match, mirror files to the repository you created:

```powershell
py tools/mirror-models.py --target YOUR_ACCOUNT/slovami-ai-models --upload
```

The tool refuses private targets and checks that expected paths exist on the
destination. Never put a Hugging Face token in source control or chat.
Its default local files go to `$HOME/slovami-ai-model-mirror`.

Before the final app URL switch:
1. Check each target download link using unauthenticated HTTP GET.
2. Download samples back from the target, verify size and SHA-256.
3. Check model redistribution terms, README and license/credit documents.
4. Update only `ModelRegistry.HF_REPO` plus the user-visible hosting links.
5. Install Slovami AI with fresh app data and test RU/EN first-run downloads,
   optional embeddings, cancellation/resume, and offline recognition.
6. Build, run unit tests, use Pull Request then Squash and Merge.

Do not remove the old download source while installed app versions still rely on it.
