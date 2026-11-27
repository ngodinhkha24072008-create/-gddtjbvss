"""
Rubik Scanner: scan a Rubik's cube with a webcam, solve it with Kociemba's
two-phase algorithm and get the robot's MOVE command.

Run:   python rubik_scanner.py
Build: see README.md (PyInstaller -> RubikScanner.exe)
"""

from __future__ import annotations

import os
import queue
import sys
import threading
import tkinter as tk
from tkinter import filedialog, ttk

import cv2
import numpy as np
from PIL import Image, ImageTk

import cube_logic as cl

try:
    import serial
    import serial.tools.list_ports
except ImportError:  # the app still scans and solves without pyserial
    serial = None

# --- Console palette (same as the car gamepad and the Android app) -----------
BG = "#0d1220"
SHELL = "#161d2e"
WELL = "#0a0e19"
LINE = "#29344b"
FG = "#e6ebf5"
MUTED = "#8a96ae"
ACCENT = "#ffb547"
ON_ACCENT = "#1b1305"
OK = "#46d39a"
DANGER = "#ff5f57"

IS_WINDOWS = sys.platform.startswith("win")
UI_FONT = "Segoe UI" if IS_WINDOWS else "DejaVu Sans"
MONO_FONT = "Consolas" if IS_WINDOWS else "DejaVu Sans Mono"

CAM_W, CAM_H = 560, 420
CELL = 26  # cube net sticker size
NET_POS = {"U": (3, 0), "L": (0, 3), "F": (3, 3), "R": (6, 3), "B": (9, 3), "D": (3, 6)}


def hex_rgb(rgb) -> str:
    r, g, b = (int(max(0, min(255, v))) for v in rgb)
    return f"#{r:02x}{g:02x}{b:02x}"


def bgr_to_hex(bgr) -> str:
    b, g, r = bgr
    return hex_rgb((r, g, b))


def text_on(hex_colour: str) -> str:
    r, g, b = (int(hex_colour[i:i + 2], 16) for i in (1, 3, 5))
    return "#000000" if (0.299 * r + 0.587 * g + 0.114 * b) > 140 else "#ffffff"


class RubikScanner(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("Rubik Scanner")
        self.configure(bg=BG)
        self.minsize(1000, 600)
        # Smaller camera view on small / scaled screens so everything fits
        free = self.winfo_screenheight() - 470 * self.tk.call("tk", "scaling") / 1.333
        self.cam_h = int(max(260, min(CAM_H, free)))
        self.cam_w = self.cam_h * 4 // 3

        # Scan state
        self.raw: dict[str, list] = {f: None for f in cl.KOCIEMBA_ORDER}  # sampled BGR per sticker
        self.labels: dict[str, list[str]] | None = None  # face letter per sticker, once all 6 are in
        self.edits: dict[tuple[str, int], str] = {}  # manual fixes
        self.step = 0  # index in SCAN_ORDER
        self.paint = None  # selected palette letter
        self.solution = None

        # Camera
        self.cap = None
        self.frame = None
        self.cam_index = tk.IntVar(value=0)
        self.mirror = tk.BooleanVar(value=False)

        # Robot / serial
        self.delay = tk.IntVar(value=cl.DEFAULT_DELAY)
        self.port_var = tk.StringVar()
        self.ser = None
        self.rx_queue: queue.Queue[str] = queue.Queue()

        self._style()
        self._build()
        self.bind("<space>", lambda e: self.capture())
        self.bind("<BackSpace>", lambda e: self.go_back())
        self.protocol("WM_DELETE_WINDOW", self.on_close)

        self.open_camera()
        self.refresh_ports()
        self.update_everything()
        self.after(15, self.camera_loop)
        self.after(100, self.poll_serial)

    # ------------------------------------------------------------------ UI
    def _style(self):
        st = ttk.Style(self)
        st.theme_use("clam")
        st.configure(".", background=SHELL, foreground=FG, fieldbackground=WELL, bordercolor=LINE,
                     lightcolor=SHELL, darkcolor=SHELL, font=(UI_FONT, 10))
        st.configure("TFrame", background=SHELL)
        st.configure("Bg.TFrame", background=BG)
        st.configure("TLabel", background=SHELL, foreground=FG)
        st.configure("Muted.TLabel", foreground=MUTED, font=(UI_FONT, 9))
        st.configure("Title.TLabel", font=(UI_FONT, 15, "bold"), background=BG)
        st.configure("Panel.TLabel", font=(UI_FONT, 11, "bold"))
        st.configure("Big.TLabel", font=(MONO_FONT, 20, "bold"), foreground=ACCENT)
        st.configure("Warn.TLabel", foreground=ACCENT, font=(UI_FONT, 9))
        st.configure("Err.TLabel", foreground=DANGER, font=(UI_FONT, 10))
        st.configure("TButton", background=SHELL, foreground=FG, bordercolor=LINE, padding=(10, 5))
        st.map("TButton", background=[("active", "#1f2840"), ("disabled", SHELL)],
               foreground=[("disabled", "#4a5570")])
        st.configure("Accent.TButton", background=ACCENT, foreground=ON_ACCENT, bordercolor=ACCENT,
                     font=(UI_FONT, 10, "bold"))
        st.map("Accent.TButton", background=[("active", "#ffc56e"), ("disabled", WELL)],
               foreground=[("disabled", "#4a5570")])
        st.configure("TCheckbutton", background=SHELL, foreground=FG)
        st.map("TCheckbutton", background=[("active", SHELL)])
        st.configure("TCombobox", fieldbackground=WELL, background=SHELL, foreground=FG, arrowcolor=FG)
        st.map("TCombobox", fieldbackground=[("readonly", WELL)], foreground=[("readonly", FG)],
               selectbackground=[("readonly", WELL)], selectforeground=[("readonly", FG)])
        st.configure("TSpinbox", fieldbackground=WELL, foreground=FG, arrowcolor=FG)
        st.configure("TEntry", fieldbackground=WELL, foreground=FG)
        self.option_add("*TCombobox*Listbox.background", WELL)
        self.option_add("*TCombobox*Listbox.foreground", FG)

    def _panel(self, parent, title, right_text=None):
        outer = tk.Frame(parent, bg=SHELL, highlightbackground=LINE, highlightthickness=1)
        head = ttk.Frame(outer)
        head.pack(fill="x", padx=14, pady=(10, 4))
        ttk.Label(head, text=title, style="Panel.TLabel").pack(side="left")
        if right_text:
            ttk.Label(head, text=right_text.upper(), style="Muted.TLabel").pack(side="right")
        body = ttk.Frame(outer)
        body.pack(fill="both", expand=True, padx=14, pady=(0, 12))
        return outer, body

    def _build(self):
        top = ttk.Frame(self, style="Bg.TFrame")
        top.pack(fill="x", padx=16, pady=(12, 6))
        ttk.Label(top, text="Rubik Scanner", style="Title.TLabel").pack(side="left")
        ttk.Label(top, text="Scan · solve with Kociemba · send to the robot", style="Muted.TLabel",
                  background=BG).pack(side="left", padx=12)

        main = ttk.Frame(self, style="Bg.TFrame")
        main.pack(fill="both", expand=True, padx=16, pady=(0, 14))
        main.columnconfigure(0, weight=0)
        main.columnconfigure(1, weight=1)

        # ---- Left: camera
        left = ttk.Frame(main, style="Bg.TFrame")
        left.grid(row=0, column=0, sticky="nsew", padx=(0, 12))
        left.columnconfigure(0, weight=1)
        left.rowconfigure(1, weight=1)
        cam_outer, cam = self._panel(left, "Scan", "Space = capture")
        cam_outer.grid(row=0, column=0, sticky="nsew")

        self.step_label = ttk.Label(cam, text="", style="Big.TLabel")
        self.step_label.pack(anchor="w")
        self.hold_label = ttk.Label(cam, text="", wraplength=self.cam_w)
        self.hold_label.pack(anchor="w", pady=(0, 6))

        self.canvas = tk.Canvas(cam, width=self.cam_w, height=self.cam_h, bg=WELL, highlightthickness=1,
                                highlightbackground=LINE)
        self.canvas.pack()

        row = ttk.Frame(cam)
        row.pack(fill="x", pady=(8, 0))
        self.capture_btn = ttk.Button(row, text="Capture face", style="Accent.TButton", command=self.capture)
        self.capture_btn.pack(side="left")
        ttk.Button(row, text="Load photo…", command=self.load_photo).pack(side="left", padx=6)
        ttk.Button(row, text="Back", command=self.go_back).pack(side="left")
        ttk.Button(row, text="Start over", command=self.reset).pack(side="left", padx=6)

        row2 = ttk.Frame(cam)
        row2.pack(fill="x", pady=(8, 0))
        ttk.Label(row2, text="Camera").pack(side="left")
        cam_box = ttk.Combobox(row2, width=4, state="readonly", values=[0, 1, 2, 3], textvariable=self.cam_index)
        cam_box.pack(side="left", padx=(6, 12))
        cam_box.bind("<<ComboboxSelected>>", lambda e: self.open_camera())
        ttk.Checkbutton(row2, text="Mirror picture", variable=self.mirror).pack(side="left")
        ttk.Label(row2, text="Fill the square with one face.", style="Muted.TLabel").pack(side="right")

        # ---- Right column
        right = ttk.Frame(main, style="Bg.TFrame")
        right.grid(row=0, column=1, sticky="nsew")
        right.columnconfigure(0, weight=1)
        right.rowconfigure(1, weight=1)

        net_outer, net = self._panel(right, "Cube", "Click a sticker to fix it")
        net_outer.grid(row=0, column=0, sticky="ew")
        self.net = tk.Canvas(net, width=CELL * 12 + 8, height=CELL * 9 + 8, bg=SHELL, highlightthickness=0)
        self.net.pack(side="left", anchor="n")
        tools = ttk.Frame(net)
        tools.pack(side="left", fill="both", expand=True, padx=(16, 0))
        self.net.bind("<Button-1>", self.on_net_click)
        self.net.bind("<Button-3>", lambda e: self.on_net_click(e, cycle=True))

        ttk.Label(tools, text="Paint with").pack(anchor="w")
        self.palette = tk.Canvas(tools, width=6 * 34, height=30, bg=SHELL, highlightthickness=0)
        self.palette.pack(anchor="w", pady=(2, 0))
        self.palette.bind("<Button-1>", self.on_palette_click)
        ttk.Label(tools, text="Pick a colour, then click stickers.\nRight-click a sticker to cycle colours.",
                  style="Muted.TLabel").pack(anchor="w", pady=(4, 10))
        ttk.Label(tools, text="Rescan a face").pack(anchor="w")
        jump = ttk.Frame(tools)
        jump.pack(anchor="w", pady=(2, 0))
        for k, f in enumerate(cl.SCAN_ORDER):
            ttk.Button(jump, text=f, width=3, command=lambda f=f: self.jump_to(f)).grid(
                row=k // 3, column=k % 3, padx=(0, 4), pady=(0, 4))

        sol_outer, sol = self._panel(right, "Solution", "Kociemba two-phase")
        sol_outer.grid(row=1, column=0, sticky="nsew", pady=(12, 0))
        r = ttk.Frame(sol)
        r.pack(fill="x")
        self.solve_btn = ttk.Button(r, text="Solve", style="Accent.TButton", command=self.do_solve)
        self.solve_btn.pack(side="left")
        self.count_label = ttk.Label(r, text="", style="Big.TLabel")
        self.count_label.pack(side="left", padx=14)
        ttk.Label(r, text="Delay (ms)").pack(side="right")
        dl = ttk.Spinbox(r, from_=cl.MIN_DELAY, to=cl.MAX_DELAY, increment=10, width=5, textvariable=self.delay,
                         command=self.update_command)
        dl.pack(side="right", padx=6)
        dl.bind("<KeyRelease>", lambda e: self.update_command())

        self.msg_label = ttk.Label(sol, text="", style="Err.TLabel", wraplength=480)
        self.msg_label.pack(anchor="w", pady=(6, 0))
        self.std_var = tk.StringVar()
        self.cmd_var = tk.StringVar()
        self.facelet_var = tk.StringVar()
        self._out_row(sol, "Solution", self.std_var)
        self._out_row(sol, "Robot", self.cmd_var)
        self._out_row(sol, "Cube", self.facelet_var)
        self.limit_label = ttk.Label(sol, text="", style="Warn.TLabel", wraplength=480)
        self.limit_label.pack(anchor="w")

        rb_outer, rb = self._panel(left, "Robot", "HC-06 COM port")
        rb_outer.grid(row=1, column=0, sticky="nsew", pady=(12, 0))
        r = ttk.Frame(rb)
        r.pack(fill="x")
        self.port_box = ttk.Combobox(r, width=26, state="readonly", textvariable=self.port_var)
        self.port_box.pack(side="left")
        ttk.Button(r, text="Refresh", command=self.refresh_ports).pack(side="left", padx=6)
        self.conn_btn = ttk.Button(r, text="Connect", command=self.toggle_serial)
        self.conn_btn.pack(side="left")
        r = ttk.Frame(rb)
        r.pack(fill="x", pady=(8, 0))
        ttk.Button(r, text="Open", command=lambda: self.send_line("SEQ RRLRFRBR")).pack(side="left")
        ttk.Button(r, text="Close", command=lambda: self.send_line("SEQ RCLCFCBC")).pack(side="left", padx=6)
        self.send_btn = ttk.Button(r, text="Send solution", style="Accent.TButton", command=self.send_solution)
        self.send_btn.pack(side="left")
        ttk.Button(r, text="Abort", command=lambda: self.send_line("SEQ C")).pack(side="left", padx=6)
        ttk.Label(rb, text="Put the cube in the robot with U on top and F toward the FRONT grabber, "
                           "press Close, then Send solution.", style="Muted.TLabel", wraplength=self.cam_w
                  ).pack(anchor="w", pady=(6, 4))
        self.log = tk.Text(rb, height=3, bg=WELL, fg=OK, insertbackground=FG, relief="flat",
                           font=(MONO_FONT, 9), highlightthickness=1, highlightbackground=LINE)
        self.log.pack(fill="both", expand=True)
        self.log.configure(state="disabled")

    def _out_row(self, parent, label, var):
        row = ttk.Frame(parent)
        row.pack(fill="x", pady=(6, 0))
        ttk.Label(row, text=label, width=9, style="Muted.TLabel").pack(side="left")
        e = tk.Entry(row, textvariable=var, state="readonly", readonlybackground=WELL, fg=ACCENT,
                     font=(MONO_FONT, 10), relief="flat", highlightthickness=1, highlightbackground=LINE)
        e.pack(side="left", fill="x", expand=True, ipady=4)
        ttk.Button(row, text="Copy", width=6, command=lambda: self.copy(var.get())).pack(side="left", padx=(6, 0))

    # -------------------------------------------------------------- camera
    def open_camera(self):
        if self.cap is not None:
            self.cap.release()
        idx = int(self.cam_index.get())
        backend = cv2.CAP_DSHOW if IS_WINDOWS else cv2.CAP_ANY
        cap = cv2.VideoCapture(idx, backend)
        if cap is not None and cap.isOpened():
            cap.set(cv2.CAP_PROP_FRAME_WIDTH, 1280)
            cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 720)
            self.cap = cap
        else:
            self.cap = None

    def camera_loop(self):
        frame = None
        if self.cap is not None:
            ok, frame = self.cap.read()
            if not ok:
                frame = None
        if frame is not None:
            if self.mirror.get():
                frame = cv2.flip(frame, 1)
            self.frame = frame
            self.draw_camera(frame)
        else:
            self.frame = None
            self.draw_no_camera()
        self.after(33, self.camera_loop)

    def draw_no_camera(self):
        if getattr(self, "_no_cam_drawn", False):
            return
        self._no_cam_drawn = True
        self.canvas.delete("all")
        self.canvas.create_text(self.cam_w // 2, self.cam_h // 2 - 12, text="No camera found", fill=FG,
                                font=(UI_FONT, 14, "bold"))
        self.canvas.create_text(self.cam_w // 2, self.cam_h // 2 + 16, fill=MUTED, font=(UI_FONT, 10),
                                text="Pick another camera number below, or use Load photo… for each face.")

    def draw_camera(self, frame):
        self._no_cam_drawn = False
        h, w = frame.shape[:2]
        scale = min(self.cam_w / w, self.cam_h / h)
        disp = cv2.resize(frame, (int(w * scale), int(h * scale)))
        dh, dw = disp.shape[:2]
        ox, oy = (self.cam_w - dw) // 2, (self.cam_h - dh) // 2
        img = ImageTk.PhotoImage(Image.fromarray(cv2.cvtColor(disp, cv2.COLOR_BGR2RGB)))
        self._photo = img  # keep a reference
        self.canvas.delete("all")
        self.canvas.create_image(ox, oy, anchor="nw", image=img)

        # Scan square + live colour guess
        sx, sy, side = cl.scan_square(w, h)
        colours = cl.sample_face(frame, (sx, sy, side))
        x0, y0, s = ox + sx * scale, oy + sy * scale, side * scale
        cell = s / 3
        for i in range(1, 3):
            self.canvas.create_line(x0 + i * cell, y0, x0 + i * cell, y0 + s, fill="#ffffff", width=1)
            self.canvas.create_line(x0, y0 + i * cell, x0 + s, y0 + i * cell, fill="#ffffff", width=1)
        self.canvas.create_rectangle(x0, y0, x0 + s, y0 + s, outline=ACCENT, width=2)
        for i, bgr in enumerate(colours):
            r, c = divmod(i, 3)
            name = cl.live_colour_name(bgr)
            cx, cy = x0 + (c + 0.5) * cell, y0 + (r + 0.5) * cell
            d = cell * 0.16
            self.canvas.create_rectangle(cx - d, cy - d, cx + d, cy + d, fill=hex_rgb(cl.LIVE_RGB[name]),
                                         outline="#000000")

    # --------------------------------------------------------------- scanning
    @property
    def current_face(self):
        return cl.SCAN_ORDER[self.step] if self.step < len(cl.SCAN_ORDER) else None

    def capture(self):
        if self.current_face is None:
            return
        if self.frame is None:
            self.set_message("No camera picture to capture. Use Load photo… instead.")
            return
        h, w = self.frame.shape[:2]
        self.store_face(self.current_face, cl.sample_face(self.frame, cl.scan_square(w, h)))

    def load_photo(self):
        if self.current_face is None:
            return
        path = filedialog.askopenfilename(
            title=f"Photo of the {cl.FACE_NAMES[self.current_face]} face",
            filetypes=[("Images", "*.jpg *.jpeg *.png *.bmp"), ("All files", "*.*")])
        if not path:
            return
        img = cv2.imdecode(np.fromfile(path, dtype=np.uint8), cv2.IMREAD_COLOR)
        if img is None:
            self.set_message("Couldn't open that picture.")
            return
        self.load_photo_array(img)

    def load_photo_array(self, img):
        """A loaded photo should be cropped to the face: the whole centred square is used."""
        h, w = img.shape[:2]
        self.store_face(self.current_face, cl.sample_face(img, cl.scan_square(w, h, fraction=1.0)))

    def store_face(self, face, colours):
        self.raw[face] = colours
        self.edits = {k: v for k, v in self.edits.items() if k[0] != face}
        # next face that still needs scanning
        missing = [i for i, f in enumerate(cl.SCAN_ORDER) if self.raw[f] is None]
        self.step = missing[0] if missing else len(cl.SCAN_ORDER)
        self.reclassify()

    def go_back(self):
        if self.step > 0:
            self.step -= 1
            self.update_everything()

    def jump_to(self, face):
        self.step = cl.SCAN_ORDER.index(face)
        self.update_everything()

    def reset(self):
        self.raw = {f: None for f in cl.KOCIEMBA_ORDER}
        self.labels = None
        self.edits = {}
        self.step = 0
        self.solution = None
        self.update_everything()

    def reclassify(self):
        self.solution = None
        if all(self.raw[f] is not None for f in cl.KOCIEMBA_ORDER):
            self.labels = cl.classify(self.raw)
            for (f, i), letter in self.edits.items():
                self.labels[f][i] = letter
        else:
            self.labels = None
        self.update_everything()

    def colour_of(self, letter):
        """On-screen colour of a face letter: the scanned colour of that centre."""
        if self.raw.get(letter) is not None:
            return bgr_to_hex(self.raw[letter][4])
        return LINE

    # --------------------------------------------------------------- net view
    def draw_net(self):
        c = self.net
        c.delete("all")
        for face, (gx, gy) in NET_POS.items():
            fills = []
            for i in range(9):
                r, col = divmod(i, 3)
                x, y = 4 + (gx + col) * CELL, 4 + (gy + r) * CELL
                if self.labels is not None:
                    fill = self.colour_of(self.labels[face][i])
                elif self.raw[face] is not None:
                    fill = bgr_to_hex(self.raw[face][i])
                else:
                    fill = WELL
                fills.append(fill)
                c.create_rectangle(x + 1, y + 1, x + CELL - 1, y + CELL - 1, fill=fill, outline=BG, width=2)
                if (face, i) in self.edits:
                    c.create_oval(x + CELL - 9, y + 4, x + CELL - 4, y + 9, fill=ACCENT, outline="")
            # face letter on the centre
            cx, cy = 4 + (gx + 1.5) * CELL, 4 + (gy + 1.5) * CELL
            c.create_text(cx, cy, text=face, font=(UI_FONT, 10, "bold"),
                          fill=MUTED if fills[4] in (WELL, LINE) else text_on(fills[4]))
            if face == self.current_face:
                x0, y0 = 4 + gx * CELL, 4 + gy * CELL
                c.create_rectangle(x0 - 1, y0 - 1, x0 + 3 * CELL + 1, y0 + 3 * CELL + 1, outline=ACCENT, width=2)

    def draw_palette(self):
        p = self.palette
        p.delete("all")
        for k, letter in enumerate(cl.KOCIEMBA_ORDER):
            x = k * 34 + 2
            fill = self.colour_of(letter)
            p.create_rectangle(x, 2, x + 28, 28, fill=fill,
                               outline=ACCENT if self.paint == letter else LINE, width=3 if self.paint == letter else 1)
            p.create_text(x + 14, 15, text=letter, fill=text_on(fill) if fill != LINE else MUTED,
                          font=(UI_FONT, 9, "bold"))

    def on_palette_click(self, event):
        k = int((event.x - 2) // 34)
        if 0 <= k < 6:
            letter = cl.KOCIEMBA_ORDER[k]
            self.paint = None if self.paint == letter else letter
            self.draw_palette()

    def on_net_click(self, event, cycle=False):
        if self.labels is None:
            self.set_message("Scan all six faces first, then you can fix stickers.")
            return
        gx, gy = (event.x - 4) // CELL, (event.y - 4) // CELL
        for face, (fx, fy) in NET_POS.items():
            if fx <= gx < fx + 3 and fy <= gy < fy + 3:
                i = (gy - fy) * 3 + (gx - fx)
                if i == 4:
                    self.set_message("Centres set the colours. Rescan the face to change one.")
                    return
                if cycle or self.paint is None:
                    cur = cl.KOCIEMBA_ORDER.index(self.labels[face][i])
                    new = cl.KOCIEMBA_ORDER[(cur + 1) % 6]
                else:
                    new = self.paint
                self.labels[face][i] = new
                self.edits[(face, i)] = new
                self.solution = None
                self.update_everything()
                return

    # ---------------------------------------------------------------- solving
    def do_solve(self):
        if self.labels is None:
            return
        cube = cl.facelet_string(self.labels)
        try:
            self.solution = cl.solve(cube)
            self.set_message("")
        except ValueError as e:
            self.solution = None
            self.set_message(str(e))
        self.update_solution()

    def update_command(self):
        self.update_solution()

    def current_delay(self):
        try:
            return max(cl.MIN_DELAY, min(cl.MAX_DELAY, int(self.delay.get())))
        except (tk.TclError, ValueError):
            return cl.DEFAULT_DELAY

    def update_solution(self):
        self.facelet_var.set(cl.facelet_string(self.labels) if self.labels else "")
        if self.solution is None:
            self.std_var.set("")
            self.cmd_var.set("")
            self.count_label.configure(text="")
            self.limit_label.configure(text="")
        elif self.solution == "":
            self.std_var.set("Already solved")
            self.cmd_var.set("")
            self.count_label.configure(text="0 moves")
            self.limit_label.configure(text="")
        else:
            moves = cl.to_robot_moves(self.solution)
            self.std_var.set(self.solution)
            self.cmd_var.set(cl.robot_command(self.solution, self.current_delay()))
            self.count_label.configure(text=f"{len(self.solution.split())} moves")
            self.limit_label.configure(
                text=(f"{len(moves)} move letters: more than the robot's {cl.MOVE_BUFFER_LIMIT}-letter "
                      "MOVE buffer. Increase MOVE_BUFFER_SIZE in Config.h." if len(moves) > cl.MOVE_BUFFER_LIMIT
                      else ""))
        state = "normal" if self.cmd_var.get() else "disabled"
        self.send_btn.configure(state=state)

    def set_message(self, text):
        self.msg_label.configure(text=text)

    # ------------------------------------------------------------------ serial
    def refresh_ports(self):
        if serial is None:
            self.port_box.configure(values=["pyserial not installed"])
            return
        ports = list(serial.tools.list_ports.comports())
        values = [f"{p.device} · {p.description}" for p in ports]
        self.port_box.configure(values=values)
        if values and not self.port_var.get():
            # Bluetooth serial ports usually say so in their description
            bt = [v for v in values if "bluetooth" in v.lower()]
            self.port_var.set((bt or values)[0])

    def toggle_serial(self):
        if self.ser is not None:
            self.close_serial()
            return
        if serial is None or not self.port_var.get():
            self.log_line("Pick the HC-06's COM port first (pair it in Windows Bluetooth settings).")
            return
        device = self.port_var.get().split(" · ")[0]
        try:
            self.ser = serial.Serial(device, 9600, timeout=0.2, write_timeout=2)
        except Exception as e:
            self.ser = None
            self.log_line(f"Couldn't open {device}: {e}")
            return
        self.conn_btn.configure(text="Disconnect")
        self.log_line(f"Connected to {device}")
        threading.Thread(target=self.read_serial, args=(self.ser,), daemon=True).start()
        self.send_line("STATUS")

    def close_serial(self):
        ser, self.ser = self.ser, None
        if ser is not None:
            try:
                ser.close()
            except Exception:
                pass
        self.conn_btn.configure(text="Connect")
        self.log_line("Disconnected")

    def read_serial(self, ser):
        buf = b""
        while self.ser is ser:
            try:
                data = ser.read(64)
            except Exception:
                self.rx_queue.put("\x00lost")
                return
            if not data:
                continue
            buf += data
            while b"\n" in buf:
                line, buf = buf.split(b"\n", 1)
                text = line.decode("utf-8", "replace").strip()
                if text:
                    self.rx_queue.put(text)

    def poll_serial(self):
        while not self.rx_queue.empty():
            msg = self.rx_queue.get()
            if msg == "\x00lost":
                self.log_line("Connection lost")
                self.close_serial()
            else:
                self.log_line("< " + msg)
        self.after(100, self.poll_serial)

    def send_line(self, text):
        if self.ser is None:
            self.log_line(f"Not connected. Would send: {text}")
            return
        try:
            self.ser.write((text + "\n").encode("ascii"))
            self.log_line("> " + text)
        except Exception as e:
            self.log_line(f"Send failed: {e}")
            self.close_serial()

    def send_solution(self):
        if self.cmd_var.get():
            self.send_line(self.cmd_var.get())

    def log_line(self, text):
        self.log.configure(state="normal")
        self.log.insert("end", text + "\n")
        self.log.see("end")
        self.log.configure(state="disabled")

    # ------------------------------------------------------------------ misc
    def copy(self, text):
        if text:
            self.clipboard_clear()
            self.clipboard_append(text)

    def update_everything(self):
        face = self.current_face
        if face is None:
            self.step_label.configure(text="All 6 faces scanned")
            self.hold_label.configure(text="Check the cube on the right, fix any wrong stickers, then press Solve.")
            self.capture_btn.configure(state="disabled")
        else:
            self.step_label.configure(text=f"Face {self.step + 1}/6 · {face} · {cl.FACE_NAMES[face]}")
            self.hold_label.configure(
                text=f"Hold the cube with the {cl.HOLD[face]}. "
                     f"(Usually the {cl.USUAL_COLOUR[face]} centre.)")
            self.capture_btn.configure(state="normal")
        self.solve_btn.configure(state="normal" if self.labels is not None else "disabled")
        self.draw_net()
        self.draw_palette()
        self.update_solution()

    def on_close(self):
        if self.cap is not None:
            self.cap.release()
        if self.ser is not None:
            self.close_serial()
        self.destroy()


def main():
    # Sharp text on high-DPI Windows screens
    if IS_WINDOWS:
        try:
            import ctypes

            ctypes.windll.shcore.SetProcessDpiAwareness(1)
        except Exception:
            pass
    app = RubikScanner()
    app.mainloop()


if __name__ == "__main__":
    main()
