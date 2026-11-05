#!/usr/bin/env python3
"""Verified mirror of Slovami AI downloadable models to an owned Hugging Face repo.

Requires: python -m pip install -U huggingface_hub
Log in with: hf auth login
Use --target OWNER/REPO --upload only after creating the public model repo.
No model weights are modified. Existing application download URLs stay unchanged.
"""
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path

from huggingface_hub import HfApi, hf_hub_download

SOURCE = "TimHdm/slovami-ai-models"
# Relative path, exact byte size, exact expected SHA-256 from ModelRegistry.kt.
MODELS = {
    "gigaam_v3_e2e_ctc_int8.onnx": (319869121, "0aacb41f70f0f5aaac4b45dd430337b9e16b180f22c72af04db8516e7609c3c0"),
    "speaker_embedding_camplus.onnx": (28281138, "f682b514c05d947ee3fa91cd6ec6c5c7543479a128373fa29b1faedccd21fd11"),
    "speaker_embedding.onnx": (116058710, "19547e85b6c14ec44b8add4e7cb9ce353c7e995d4f1c9ffd408176ac3a2d6895"),
    "speaker_embedding_v2.onnx": (71441526, "bf1a75b9930474cf3389ef415e6e5d38ca96fea4a3a00f7e301d080a58ee2239"),
    "en/whisper_base_en_encoder_int8.onnx": (29120534, "ef6b936f4c9b1d90a3b68634b60c4ed8576b26172b33c2535ec0e933c9edb823"),
    "en/whisper_base_en_decoder_int8.onnx": (130669978, "f7162ad6db2dbef16cfaeaa7f945b9d7dd9c1b8d472f6aca82f2273d185e4d41"),
    "en/whisper_base_en_tokens.txt": (835554, "306cd27f03c1a714eca7108e03d66b7dc042abe8c258b44c199a7ed9838dd930"),
    "summary/navec_news.tar": (26634240, "f07270833d78523edc5781538d67038e95b43975e4a7ae757c693b687f9cbfca"),
    "summary/slovnet_ner.tar": (2385920, "16c3343a6572ddf0e2b2fc1923113de15a7f5ca5bde90e780dd590418665603e"),
}


def verify(file: Path, size: int, expected_hash: str) -> None:
    if file.stat().st_size != size:
        raise RuntimeError(f"Size mismatch: {file}")
    with file.open("rb") as stream:
        digest = hashlib.file_digest(stream, "sha256").hexdigest()
    if digest != expected_hash:
        raise RuntimeError(f"SHA-256 mismatch: {file}")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--directory", type=Path, default=Path.home() / "slovami-ai-model-mirror")
    parser.add_argument("--target", help="Owned Hugging Face model repository, e.g. USER/slovami-ai-models")
    parser.add_argument("--upload", action="store_true", help="Upload verified files to --target")
    args = parser.parse_args()
    if args.upload and not args.target:
        parser.error("--upload requires --target")

    api = HfApi()
    if args.upload:
        info = api.model_info(args.target)
        if info.private:
            raise RuntimeError("Target must be public for anonymous app downloads")
        if args.target == SOURCE:
            raise RuntimeError("The target must be a separate, owned repository")

    root = args.directory.resolve()
    root.mkdir(parents=True, exist_ok=True)
    verified = []
    for name, (size, digest) in MODELS.items():
        print(f"Download and verify: {name}", flush=True)
        downloaded = Path(hf_hub_download(
            repo_id=SOURCE, filename=name, local_dir=str(root),
        ))
        # Cache and provenance are kept local. Never upload .cache metadata.
        verify(downloaded, size, digest)
        verified.append((name, downloaded))
        if args.upload:
            print(f"Upload: {name} -> {args.target}", flush=True)
            api.upload_file(
                path_or_fileobj=str(downloaded),
                path_in_repo=name,
                repo_id=args.target,
                repo_type="model",
            )

    manifest = {
        "version": "slovami-model-mirror-1",
        "models": {
            name: {"filename": Path(name).name, "size_bytes": MODELS[name][0],
                   "sha256": MODELS[name][1]}
            for name, _ in verified
        }
    }
    manifest_file = root / "manifest.json"
    manifest_file.write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    if args.upload:
        api.upload_file(path_or_fileobj=str(manifest_file), path_in_repo="manifest.json",
                        repo_id=args.target, repo_type="model")
        # Verify published paths exist; changing the app registry is a separate operation.
        published = {s.path for s in api.list_repo_tree(args.target, recursive=True)}
        missing = set(MODELS) | {"manifest.json"}
        missing -= published
        if missing:
            raise RuntimeError(f"Missing on target: {sorted(missing)}")
        print(f"Mirror uploaded and paths verified: https://huggingface.co/{args.target}")
    else:
        print(f"All {len(verified)} model hashes verified locally in {root}.")
        print("No remote repository was modified.")


if __name__ == "__main__":
    main()
