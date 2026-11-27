# Notes for Claude

- **Keep `Controller.apk` at the front of the repository** (the repository root),
  never inside a subfolder. The "Build gamepad APK" workflow saves each new build
  there automatically; don't move or rename it.
- Test and troubleshooting sketches live in `Troubleshoot and test/`, each in its
  own subfolder with a PDF instruction file.
- The main robot sketch is `CarArm4DOF/CarArm4DOF.ino`; the Controller app's
  screen is `controller/index.html` (built into the APK from `android/`).
