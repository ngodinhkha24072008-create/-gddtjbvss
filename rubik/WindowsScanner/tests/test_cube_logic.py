"""Run with:  python -m pytest tests   (or just: python tests/test_cube_logic.py)"""
import os
import random
import sys

import numpy as np
import cv2

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
import cube_logic as cl  # noqa: E402

# A valid scrambled cube (URFDLB facelet string)
SCRAMBLE = "DRLUUBFBRBLURRLRUBLRDDFDLFUFUFFDBRDUBRUFLLFDDBFLUBLRBD"

# Sticker colours as a webcam might see them under warm room light (RGB)
CAMERA_RGB = {
    "U": (205, 200, 185),  # white, slightly yellowish
    "R": (185, 30, 40),    # red
    "F": (20, 150, 70),    # green
    "D": (215, 200, 40),   # yellow
    "L": (235, 105, 25),   # orange
    "B": (25, 70, 175),    # blue
}


def render_face(letters, rng, size=(640, 480)):
    w, h = size
    img = np.full((h, w, 3), 40, np.uint8)
    x0, y0, side = cl.scan_square(w, h)
    cell = side // 3
    for i, letter in enumerate(letters):
        r, c = divmod(i, 3)
        rgb = np.array(CAMERA_RGB[letter], float)
        shade = rng.uniform(0.8, 1.1)  # uneven lighting per sticker
        bgr = np.clip(rgb[::-1] * shade, 0, 255)
        xa, ya = x0 + c * cell + 4, y0 + r * cell + 4
        img[ya:ya + cell - 8, xa:xa + cell - 8] = bgr
    noise = rng.normal(0, 8, img.shape)
    return np.clip(img + noise, 0, 255).astype(np.uint8)


def faces_of(cube):
    return {f: list(cube[i * 9:(i + 1) * 9]) for i, f in enumerate(cl.KOCIEMBA_ORDER)}


def test_scan_classify_solve():
    rng = np.random.default_rng(1)
    truth = faces_of(SCRAMBLE)
    raw = {}
    for f in cl.SCAN_ORDER:
        img = render_face(truth[f], rng)
        raw[f] = cl.sample_face(img, cl.scan_square(img.shape[1], img.shape[0]))
    labels = cl.classify(raw)
    assert cl.facelet_string(labels) == SCRAMBLE
    sol = cl.solve(SCRAMBLE)
    assert sol
    cmd = cl.robot_command(sol, 210)
    assert cmd.startswith("MOVE 210 0 ")
    assert len(cmd.split()[3]) <= cl.MOVE_BUFFER_LIMIT


def test_robot_format():
    assert cl.to_robot_moves("R U' F2 D B' L2") == "RuFFDbLL"
    assert cl.robot_command("R U'", 150) == "MOVE 150 0 Ru"
    assert cl.robot_command("", 150) == ""


def test_solution_really_solves():
    """Apply the solution with a tiny facelet simulator and check the cube ends solved."""
    sol = cl.solve(SCRAMBLE)
    assert apply_moves(SCRAMBLE, sol) == cl.SOLVED


def test_validation_messages():
    assert cl.solve(cl.SOLVED) == ""
    bad = "U" + SCRAMBLE[1:]
    assert "9 times" in cl.validate(bad) or cl.validate(bad)
    flipped = list(SCRAMBLE)
    # swap the two stickers of one edge (U8/F2) -> flip error
    flipped[7], flipped[19] = flipped[19], flipped[7]
    assert "flipped" in cl.validate("".join(flipped))


# --- minimal facelet-level cube simulator, for checking solutions --------------
def _face_cycles():
    # Facelet indices: U 0-8, R 9-17, F 18-26, D 27-35, L 36-44, B 45-53
    U, R, F, D, L, B = 0, 9, 18, 27, 36, 45
    rot = lambda o: [(o + 0, o + 2, o + 8, o + 6), (o + 1, o + 5, o + 7, o + 3)]
    return {
        "U": rot(U) + [(F + 0, L + 0, B + 0, R + 0), (F + 1, L + 1, B + 1, R + 1), (F + 2, L + 2, B + 2, R + 2)],
        "D": rot(D) + [(F + 6, R + 6, B + 6, L + 6), (F + 7, R + 7, B + 7, L + 7), (F + 8, R + 8, B + 8, L + 8)],
        "R": rot(R) + [(U + 2, B + 6, D + 2, F + 2), (U + 5, B + 3, D + 5, F + 5), (U + 8, B + 0, D + 8, F + 8)],
        "L": rot(L) + [(U + 0, F + 0, D + 0, B + 8), (U + 3, F + 3, D + 3, B + 5), (U + 6, F + 6, D + 6, B + 2)],
        "F": rot(F) + [(U + 6, R + 0, D + 2, L + 8), (U + 7, R + 3, D + 1, L + 5), (U + 8, R + 6, D + 0, L + 2)],
        "B": rot(B) + [(U + 2, L + 0, D + 6, R + 8), (U + 1, L + 3, D + 7, R + 5), (U + 0, L + 6, D + 8, R + 2)],
    }


_CYCLES = _face_cycles()


def _turn(s, face):
    s = list(s)
    for a, b, c, d in _CYCLES[face]:
        # clockwise: a<-d, b<-a, c<-b, d<-c
        s[a], s[b], s[c], s[d] = s[d], s[a], s[b], s[c]
    return "".join(s)


def apply_moves(cube, solution):
    for m in solution.split():
        n = {"": 1, "2": 2, "'": 3}[m[1:]]
        for _ in range(n):
            cube = _turn(cube, m[0])
    return cube


if __name__ == "__main__":
    for name, fn in list(globals().items()):
        if name.startswith("test_"):
            fn()
            print("ok", name)
