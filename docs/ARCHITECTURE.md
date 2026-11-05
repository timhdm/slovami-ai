# Architecture

`InterviewViewModel` coordinates recording, import, recognition and export.
`InterviewScreen` renders the current phase. Recording/transcription controls
are anchored in the Scaffold bottom bar; setup content scrolls independently.

`audio/` owns recording, playback, import and foreground services. `asr/`
owns recognition and speaker identification. `downloader/` fetches models.
`summary/` provides offline NER and extractive summaries using LexRank and rules.
`ui/SummarySheet.kt` displays the result; no LLM or JNI text-generation runtime is used.

`SessionStore` saves session JSON and recordings under private app files.
Automatic cloud backup and device transfer are excluded in the Android backup
rules. User-directed export to Downloads or sharing remains available.
`docx/DocxBuilder.kt` builds the exported Word document.
