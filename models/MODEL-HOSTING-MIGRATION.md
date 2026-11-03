# Slovami AI model hosting

Independent public model repository: https://huggingface.co/TimHdm/slovami-ai-models

## Migration status

The nine model weights and the existing checksum manifest were duplicated on
the Hugging Face server from the previous model repository. All nine paths
and declared byte sizes were checked in the new public model repository.

The Android app's model registry now points to `TimHdm/slovami-ai-models` on
this task branch. **Full SHA-256 verification and clean-install testing remain
required before merging this branch into main.**

The optional QVikhr model is excluded because its SHA-256 is still zero and
it is not part of the original nine-file model repository.

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

The Hugging Face server-side duplication copied the former repository's
README, which still mentions the old application's brand. This is metadata,
not model weights. The new Slovami AI-specific model card lives in
`models/HUGGINGFACE-README.md`. Upload only this file:

```powershell
hf upload TimHdm/slovami-ai-models models/HUGGINGFACE-README.md README.md --repo-type model
```

The model card preserves original model creators and licensing notices.

## Final QA before Squash and Merge

1. Verify all nine model SHA-256 values locally from the new repository.
2. Confirm the new public model card renders correctly.
3. Run GitHub Actions for the Android branch (APK build and unit tests).
4. Install fresh debug APK and test the initial RU model download.
5. Test EN models and optional speaker embeddings as applicable.
6. Verify the downloader's retry/resume and offline inference.
7. Merge via Squash and Merge and automatically delete the task branch.

Older installed app versions may still depend on the previous repository.
Do not attempt to disable or remove that service.
