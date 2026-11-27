"""
Cube scanning and solving logic for Rubik Scanner (no GUI code here).

Facelets follow Kociemba's URFDLB order. Each face is read row by row,
top-left first, while looking straight at it with the cube held like this:

    U  top face toward the camera, BACK face pointing up
    R  right face toward the camera, TOP face pointing up
    F  front face toward the camera, TOP face pointing up
    D  bottom face toward the camera, FRONT face pointing up
    L  left face toward the camera, TOP face pointing up
    B  back face toward the camera, TOP face pointing up

Solutions are converted to the robot's MOVE command format (see
rubik/Arduino/README.md): "MOVE <delay_ms> <orientation> <moves>" where a
clockwise turn is the capital letter (R), a counter-clockwise turn is the
lowercase letter (r) and a half turn is the letter twice (RR).
"""

from __future__ import annotations

import cv2
import numpy as np

KOCIEMBA_ORDER = ["U", "R", "F", "D", "L", "B"]

# The order the app asks for faces in: turn the cube around the middle, then the bottom
SCAN_ORDER = ["U", "F", "R", "B", "L", "D"]

FACE_NAMES = {
    "U": "Up (top)",
    "R": "Right",
    "F": "Front",
    "D": "Down (bottom)",
    "L": "Left",
    "B": "Back",
}

# Usual centre colours (white on top, green in front). Only used in the
# instructions; the real colours always come from the scanned centres.
USUAL_COLOUR = {
    "U": "white",
    "R": "red",
    "F": "green",
    "D": "yellow",
    "L": "orange",
    "B": "blue",
}

HOLD = {
    "U": "TOP face toward the camera, BACK face pointing up",
    "F": "FRONT face toward the camera, TOP face pointing up",
    "R": "RIGHT face toward the camera, TOP face pointing up",
    "B": "BACK face toward the camera, TOP face pointing up",
    "L": "LEFT face toward the camera, TOP face pointing up",
    "D": "BOTTOM face toward the camera, FRONT face pointing up",
}

SOLVED = "".join(f * 9 for f in KOCIEMBA_ORDER)

# Arduino buffers (rubik/Arduino/Config.h): MOVE_BUFFER_SIZE 64 -> 63 move letters
MOVE_BUFFER_LIMIT = 63
DEFAULT_DELAY = 210
MIN_DELAY = 120
MAX_DELAY = 300

# ---------------------------------------------------------------------------
# Sampling
# ---------------------------------------------------------------------------


def scan_square(width: int, height: int, fraction: float = 0.62) -> tuple[int, int, int]:
    """The centred square (x, y, side) the cube face should fill in the camera picture."""
    side = int(min(width, height) * fraction)
    return (width - side) // 2, (height - side) // 2, side


def sample_face(image_bgr: np.ndarray, square: tuple[int, int, int], inner: float = 0.5) -> list[tuple[float, float, float]]:
    """Median BGR colour of the middle of each of the 9 stickers, row by row."""
    x0, y0, side = square
    cell = side / 3.0
    pad = cell * (1 - inner) / 2
    colours = []
    for row in range(3):
        for col in range(3):
            xa = int(x0 + col * cell + pad)
            ya = int(y0 + row * cell + pad)
            xb = int(x0 + (col + 1) * cell - pad)
            yb = int(y0 + (row + 1) * cell - pad)
            patch = image_bgr[max(ya, 0):max(yb, ya + 1), max(xa, 0):max(xb, xa + 1)].reshape(-1, 3)
            b, g, r = np.median(patch, axis=0)
            colours.append((float(b), float(g), float(r)))
    return colours


def _lab(colours_bgr) -> np.ndarray:
    arr = np.clip(np.array(colours_bgr, dtype=np.float64), 0, 255).astype(np.uint8).reshape(1, -1, 3)
    lab = cv2.cvtColor(arr, cv2.COLOR_BGR2LAB).reshape(-1, 3).astype(np.float64)
    return lab


def _distance(lab_a: np.ndarray, lab_b: np.ndarray) -> float:
    # Lightness changes a lot with lighting and shadows, so it counts a bit less than colour
    d = lab_a - lab_b
    return float(np.sqrt((0.6 * d[0]) ** 2 + d[1] ** 2 + d[2] ** 2))


# ---------------------------------------------------------------------------
# Live guess (before all six faces are scanned)
# ---------------------------------------------------------------------------

_LIVE_NAMES = ["white", "yellow", "orange", "red", "green", "blue"]
LIVE_RGB = {
    "white": (240, 242, 246),
    "yellow": (255, 212, 59),
    "orange": (255, 140, 26),
    "red": (229, 56, 59),
    "green": (45, 190, 108),
    "blue": (58, 123, 255),
}


def live_colour_name(bgr: tuple[float, float, float]) -> str:
    """Rough colour name from hue/saturation, used only for the live preview."""
    px = np.clip(np.array([[bgr]], dtype=np.float64), 0, 255).astype(np.uint8)
    h, s, v = cv2.cvtColor(px, cv2.COLOR_BGR2HSV)[0, 0].astype(int)  # h: 0-179
    if s < 60 and v > 110:
        return "white"
    if h < 6 or h >= 165:
        return "red"
    if h < 20:
        return "orange"
    if h < 36:
        return "yellow"
    if h < 88:
        return "green"
    if h < 135:
        return "blue"
    return "red"


# ---------------------------------------------------------------------------
# Final classification (all six faces scanned)
# ---------------------------------------------------------------------------


def classify(raw: dict[str, list[tuple[float, float, float]]]) -> dict[str, list[str]]:
    """
    Give every sticker a face letter. Each face's centre defines that letter's
    colour; every other sticker goes to the closest centre, with at most 9
    stickers per colour (closest pairs are matched first).
    """
    centres = {f: raw[f][4] for f in KOCIEMBA_ORDER}
    centre_lab = {f: _lab([centres[f]])[0] for f in KOCIEMBA_ORDER}

    labels = {f: [""] * 9 for f in KOCIEMBA_ORDER}
    counts = {f: 1 for f in KOCIEMBA_ORDER}
    pairs = []
    for face in KOCIEMBA_ORDER:
        labels[face][4] = face
        labs = _lab(raw[face])
        for i in range(9):
            if i == 4:
                continue
            for c in KOCIEMBA_ORDER:
                pairs.append((_distance(labs[i], centre_lab[c]), face, i, c))
    pairs.sort(key=lambda p: p[0])
    for _, face, i, c in pairs:
        if labels[face][i] or counts[c] >= 9:
            continue
        labels[face][i] = c
        counts[c] += 1
    return labels


def facelet_string(labels: dict[str, list[str]]) -> str:
    return "".join("".join(labels[f]) for f in KOCIEMBA_ORDER)


# ---------------------------------------------------------------------------
# Solving
# ---------------------------------------------------------------------------

_VERIFY_ERRORS = {
    -1: "Each colour must appear exactly 9 times.",
    -2: "An edge piece is missing or appears twice. Check the scanned colours.",
    -3: "One edge is flipped. A real cube can't be in this state; check the colours.",
    -4: "A corner piece is missing or appears twice. Check the scanned colours.",
    -5: "One corner is twisted. A real cube can't be in this state; check the colours.",
    -6: "Two pieces are swapped (parity error). Check the scanned colours.",
}


def validate(cube: str) -> str | None:
    """None if the cube string can be solved, otherwise a message saying what's wrong."""
    if len(cube) != 54:
        return "The cube needs 54 stickers."
    centres = [cube[i * 9 + 4] for i in range(6)]
    if sorted(centres) != sorted(KOCIEMBA_ORDER):
        return "The six centres must all be different colours."
    bad = [f"{f}: {cube.count(f)}" for f in KOCIEMBA_ORDER if cube.count(f) != 9]
    if bad:
        return "Each colour must appear exactly 9 times (now " + ", ".join(bad) + "). Click stickers to fix them."
    try:
        from kociemba.pykociemba.facecube import FaceCube

        code = FaceCube(cube).toCubieCube().verify()
    except Exception:  # pragma: no cover - the solver will report it instead
        return None
    if code != 0:
        return _VERIFY_ERRORS.get(code, "This cube state isn't possible. Check the scanned colours.")
    return None


def solve(cube: str) -> str:
    """Standard notation solution, e.g. "R U' F2". Empty string if already solved."""
    if cube == SOLVED:
        return ""
    problem = validate(cube)
    if problem:
        raise ValueError(problem)
    import kociemba

    return kociemba.solve(cube).strip()


def to_robot_moves(solution: str) -> str:
    """Convert "R U' F2" to the robot's letters "RuFF"."""
    out = []
    for move in solution.split():
        face, suffix = move[0], move[1:]
        if face not in "URFDLB":
            raise ValueError(f"Unknown move: {move}")
        if suffix == "":
            out.append(face)
        elif suffix == "'":
            out.append(face.lower())
        elif suffix == "2":
            out.append(face * 2)
        else:
            raise ValueError(f"Unknown move: {move}")
    return "".join(out)


def robot_command(solution: str, delay: int = DEFAULT_DELAY, orientation: int = 0) -> str:
    """Full command for the robot, e.g. "MOVE 210 0 RuFF"."""
    moves = to_robot_moves(solution)
    if not moves:
        return ""
    return f"MOVE {int(delay)} {int(orientation)} {moves}"
