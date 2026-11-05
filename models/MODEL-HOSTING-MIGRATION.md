# Slovami AI model hosting

Model repository: https://huggingface.co/TimHdm/slovami-ai-models

Application developer and model distribution maintainer: Timur M. Omarov
([TimHdm](https://github.com/TimHdm)). Original model authors retain their attribution;
see [CREDITS.md](CREDITS.md).

## Historical migration record

The nine model weights and the existing checksum manifest were duplicated on
the Hugging Face server from the previous model repository. All nine paths
and declared byte sizes were checked in the new public model repository.

The Android app's `ModelRegistry.kt` points to `TimHdm/slovami-ai-models`.
The recorded path/size checks do not establish full SHA-256 verification or
clean-install testing. Their completion is not documented here.

## Verify the copy on Windows

Install the CLI / Python client:

```powershell
py -m pip install -U huggingface_hub
hf auth login
```

To check the actual bytes on the new server against the registered SHA-256,
run from the Git checkout:

```powershell
py tools/mirror-models.py
```

This downloads approximately 725 MB of model files into
`$HOME/slovami-ai-model-mirror` outside the Git checkout, and checks all nine
against the expected sizes and SHA-256 values. It does **not** modify the
remote repository. The output must report that all nine hashes matched.

## Update the model card

The maintained model card lives in `models/HUGGINGFACE-README.md`. A read-only
check on 2026-11-05 confirmed the remote card already uses Slovami AI and preserves
upstream attribution. The local card additionally identifies the application
maintainer; publishing that update is a separate action. To publish the maintained
card, upload only this file:

```powershell
hf upload TimHdm/slovami-ai-models models/HUGGINGFACE-README.md README.md --repo-type model
```

The model card preserves original model creators and licensing notices.

## QA after model hosting changes

1. Verify all nine model SHA-256 values locally from the new repository.
2. Confirm the new public model card renders correctly.
3. Run GitHub Actions for the Android branch (APK build and unit tests).
4. Install fresh debug APK and test the initial RU model download.
5. Test EN models and optional speaker embeddings as applicable.
6. Verify the downloader's retry/resume and offline inference.
7. Follow the current [PR and publishing workflow](../HOW_TO_PUBLISH.md), including
   user approval before merging as required by the local development instructions.

Older installed app versions may still depend on the previous repository.
Do not attempt to disable or remove that service.
