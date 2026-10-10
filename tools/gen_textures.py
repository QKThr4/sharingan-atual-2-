"""Gera texturas desenhadas por codigo: efeito Amaterasu e emblema do Cla Uchiha.
Os olhos vem das imagens de referencia (veja process_images.py)."""
import math
import os
import struct
import zlib

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src', 'main', 'resources', 'assets', 'sharingan', 'textures')


def png(path, w, h, px):
    raw = b''.join(b'\x00' + bytes(c for p in px[y * w:(y + 1) * w] for c in p) for y in range(h))

    def chunk(t, d):
        return struct.pack('>I', len(d)) + t + d + struct.pack('>I', zlib.crc32(t + d) & 0xffffffff)

    data = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0)) \
        + chunk(b'IDAT', zlib.compress(raw, 9)) + chunk(b'IEND', b'')
    with open(path, 'wb') as f:
        f.write(data)


def render(size, fn, ss=3):
    px = []
    for y in range(size):
        for x in range(size):
            acc = [0, 0, 0, 0]
            for sy in range(ss):
                for sx in range(ss):
                    c = fn((x + (sx + .5) / ss) / size, (y + (sy + .5) / ss) / size)
                    if c:
                        acc[0] += c[0] * c[3]
                        acc[1] += c[1] * c[3]
                        acc[2] += c[2] * c[3]
                        acc[3] += c[3]
            n = ss * ss
            if acc[3] == 0:
                px.append((0, 0, 0, 0))
            else:
                px.append((int(acc[0] / acc[3]), int(acc[1] / acc[3]), int(acc[2] / acc[3]), int(acc[3] / n)))
    return px


def effect(x, y):
    dx, dy = x - .5, y - .5
    r = math.hypot(dx, dy)
    if r > .48:
        return None
    flame = abs(dx) < .3 * (1 - (.5 - dy)) + .05 and dy > -.3
    if r > .4:
        return (60, 20, 100, 255)
    return (20, 8, 40, 255) if not flame else (110, 40, 190, 255)


def emblem(x, y):
    """Leque Uchiha: disco vermelho com cabo branco, contorno escuro."""
    dark = (28, 14, 14, 255)
    red = (205, 28, 28, 255)
    white = (240, 236, 232, 255)
    cx, cy, R = .5, .39, .355
    d = math.hypot(x - cx, y - cy)
    in_handle = .42 <= x <= .58 and .66 <= y <= .96
    in_handle_in = .45 <= x <= .55 and .66 <= y <= .93
    in_disk = d <= R
    in_disk_in = d <= R - .05
    # parte branca inferior do disco (a "base" do leque)
    base = in_disk_in and y > cy + .10 and abs(x - cx) < (y - (cy + .10)) * 1.1 + .02
    if in_handle_in:
        return white
    if in_handle:
        return dark
    if base:
        return white
    if in_disk_in:
        return red
    if in_disk:
        return dark
    return None


for d in ('item', 'mob_effect'):
    os.makedirs(os.path.join(OUT, d), exist_ok=True)
png(os.path.join(OUT, 'mob_effect', 'amaterasu.png'), 18, 18, render(18, effect, 4))
# o emblema agora vem da imagem oficial (process_images.py)
print('ok')
