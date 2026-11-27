# Rubik Scanner (Windows)

A desktop app that scans a Rubik's cube with a webcam, solves it with Kociemba's
two-phase algorithm, and gives you the command for the cube robot in its `MOVE` format.
It can also send that command to the robot over the HC-06's Bluetooth COM port.

**Download:** `RubikScanner.exe` at the top of this branch, built automatically by
`.github/workflows/build-rubik-scanner.yml`. No install needed; just run it.

## How to scan

1. **Camera:** pick your camera number (0 is usually the built-in one).
2. **Hold each face:** show the face the app asks for, filling the amber square. The
   small squares in the grid show the colour it currently sees.
3. **Capture:** press **Capture face** or the **Space** bar. The app moves to the next face.
4. **Order:** the app asks for the faces in this order:

   | Step | Face | Hold the cube with… |
   |---|---|---|
   | 1 | U (top) | the TOP face toward the camera, BACK face pointing up |
   | 2 | F (front) | the FRONT face toward the camera, TOP face pointing up |
   | 3 | R (right) | the RIGHT face toward the camera, TOP face pointing up |
   | 4 | B (back) | the BACK face toward the camera, TOP face pointing up |
   | 5 | L (left) | the LEFT face toward the camera, TOP face pointing up |
   | 6 | D (bottom) | the BOTTOM face toward the camera, FRONT face pointing up |

   Any colour scheme works. Each face's centre sets its colour.
5. **Check the cube net:** after all six faces are scanned, the app sorts every sticker
   into the six centre colours, at most 9 each. Fix any wrong sticker in the net on the
   right:
   - Pick a colour under **Paint with**, then click the sticker; or
   - Right-click a sticker to cycle through the colours.
6. **Solve:** press **Solve**. You get:
   - **Solution:** standard notation, e.g. `R U' F2`.
   - **Robot:** the command for the Arduino, e.g. `MOVE 210 0 RuFF`. Clockwise = capital
     letter, counter-clockwise = lowercase, half turn = the letter twice, exactly like the
     Android app sends.
   - **Cube:** the 54-letter facelet string (URFDLB order).

   **Delay** sets the milliseconds between servo steps (120 = fastest, 300 = slowest).

- **No webcam?** Use **Load photo…** with a picture cropped to just that face.
- **Wrong face?** Use **Back**, or the **Rescan a face** buttons.

## Sending to the robot

1. **Pair:** pair the HC-06 in Windows Bluetooth settings (PIN 1234). Windows creates a COM port for it.
2. **Connect:** pick that port in the **Robot** panel and press **Connect**. Replies from
   the robot (`OK`, `BUSY`, `IDLE`…) appear in the log.
3. **Place the cube:** **U face up**, and the **F face toward the FRONT grabber** (see
   `README_resources/servodef.png`).
4. **Grip:** press **Close** so the grabbers hold the cube.
5. **Send:** press **Send solution**. **Abort** sends `SEQ C`.

The robot's `MOVE` buffer holds 63 move letters (`MOVE_BUFFER_SIZE` in
`Arduino/Config.h`). The app warns you if a solution is longer.

## Run from source / build the .exe yourself

```bat
pip install -r requirements.txt
python rubik_scanner.py
```

```bat
python tests\test_cube_logic.py
```

```bat
pip install pyinstaller
pyinstaller --onefile --windowed --name RubikScanner --collect-all kociemba rubik_scanner.py
```

The built app ends up in `dist\RubikScanner.exe`.

## Files

| File | What it does |
|---|---|
| `rubik_scanner.py` | The window: camera, cube net, solution, robot connection |
| `cube_logic.py` | Sampling sticker colours, sorting them into faces, Kociemba solve, `MOVE` formatting |
| `tests/test_cube_logic.py` | Renders a scrambled cube as fake camera pictures, checks the scan, the solve (by replaying it) and the `MOVE` format |
