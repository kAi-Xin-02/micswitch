# MicSwitch — Low-Level Android Audio Routing Framework

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android%208.0%2B-green.svg)](https://developer.android.com)
[![Status](https://img.shields.io/badge/status-Active%20Research-orange.svg)]()

**MicSwitch** is an open-source systems research project and utility investigating low-level Android audio policy manipulation, dynamic mic routing, and inter-process audio session virtualization between real-time communication apps (Discord, WhatsApp, etc.).

---

## 🎯 Project Goals

- **Seamless Mic Switching**: Dynamically route hardware and virtual audio streams between concurrent VoIP sessions without dropping calls.
- **Root & Shizuku Integration**: Leverage elevated system execution environments (`libsu`, Shizuku API) to interface with internal AOSP audio server controls.
- **AOSP Audio Policy Research**: Investigate non-destructive approaches to Android's exclusive `AudioRecord` locking mechanisms.

---

## 🏗️ Current Capabilities

- [x] **Privileged Subsystem Detection**: Automated runtime detection of Root (`libsu`) and ADB/Shizuku privileges.
- [x] **Overlay Controller**: High-performance, low-overhead floating action controls for in-call management.
- [x] **Permission & AppOps Manager**: Dynamic runtime manipulation of microphone permissions across target VoIP packages.
- [x] **Crash Prevention**: Resolved call-teardown bugs caused by premature package force-stops.

---

## 🔬 The Technical Challenge

In standard Android userspace, the AOSP `AudioPolicyManager` and `AudioFlinger` subsystems enforce strict exclusive access over active input capture streams (`AudioRecord`). Standard permission manipulation via public APIs does not alter active hardware audio sessions already bound by the HAL.

For detailed analysis, benchmarks, and kernel/HAL findings, read our technical documentation in [**RESEARCH.md**](RESEARCH.md).

---

## 🚀 Active Roadmap & Research Vectors

1. **LSposed / ART Hooking Layer**: Incept and hook `AudioRecord.startRecording()` and `AudioPolicyService` calls at the framework layer.
2. **Virtual Audio HAL / Loopback**: Create a virtual loopback device to multiplex hardware mic input to multiple client apps simultaneously.
3. **AudioFlinger Stream Multiplexing**: Directly interface with `libaudioclient` native IPC calls to dynamically reparent active audio tracks.

---

## 📋 Requirements

- Android 8.0+ (API 26+)
- Root access (KernelSU / Magisk / APatch) or Shizuku environment
- System Alert Window (Overlay) permission

---

## 📬 Contact & Contribution

Contributions, issues, and discussions on low-level Android audio internals are welcome!
- **Maintainer**: Aman Goswami ([star.light0x.2@gmail.com](mailto:star.light0x.2@gmail.com))
