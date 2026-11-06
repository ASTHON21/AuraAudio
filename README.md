# Aura Audio — Pure Offline Audiophile Sanctuary

An introspective, ultra-refined Android offline audio player crafted with Jetpack Compose, Material Design 3, and the **Aura Minimalist Audio** design system. Operates 100% locally with **zero network permissions**.

## Direct APK Download from GitHub

This repository is pre-configured with **GitHub Actions** (`.github/workflows/build-apk.yml`) to automatically build a signed, installable Android APK on every push to `main` or `master`.

### How to Download the APK
1. **Via GitHub Releases (Direct Link)**:
   - Open your GitHub repository page.
   - Click **Releases** in the right-hand sidebar.
   - Under the latest release, click **`Aura-Audio.apk`** to download and install it directly onto your Android device.
2. **Via GitHub Actions Artifacts**:
   - Open the **Actions** tab in your GitHub repository.
   - Select the latest **Build & Publish Android APK** workflow run.
   - Scroll down to **Artifacts** and click **`Aura-Audio-APK`**.

---

## Features & Architecture

- **Zero Network Access**: Explicitly strips `INTERNET` and `ACCESS_NETWORK_STATE` permissions at the manifest merger level and uses zero network libraries.
- **Sanctuary Player View**:
  - `120px` ambient optical refraction backdrop (`#0D0E12` base void with Dawn Azure `#4F75FF` and Warm Apricot `#FFA877` accents).
  - `rounded-xl` (`24dp`) glassmorphic artwork enclosure with real-time acoustic waveform visualization.
  - Hairline `3dp` seekbar and scrubber with expanding thumb (`8dp` to `14dp`), Dawn Azure glow, and tabular numeral (`tnum`) timestamps.
  - `64dp` circular `#F4F5F7` Play/Pause anchor and `48dp` secondary transport controls.
- **Audio Index & Search**:
  - Recessed pill search bar with focus-reactive Dawn Azure border.
  - Category filters (`ALL TRACKS`, `FAVORITES`, `LOSSLESS`) and zero-divider track rows.
- **Signal & Acoustic Lab**:
  - Real hardware audio telemetry read from Android `AudioManager` (active output device, native DAC sample rate, frames per buffer).
  - Playback clock speed calibration (`0.50x` – `2.00x`) and digital output gain control.
- **Local Audio Sources**:
  - Import local audio files (`FLAC`, `WAV`, `ALAC`, `AAC`, `MP3`, `OGG`, `OPUS`) with automatic `MediaMetadataRetriever` metadata and embedded artwork extraction.
  - Synthesize custom uncompressed 16-bit / 44.1kHz stereo PCM `.wav` reference tones on-device.
