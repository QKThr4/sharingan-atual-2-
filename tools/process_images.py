"""Converte as imagens de referencia (tools/source) em texturas circulares com fundo transparente.

Gera:
  textures/gui/eye_<nome>.png   (128x128)  -> aba J e rosto do jogador
  textures/item/eye_<nome>.png  (32x32)    -> itens dos olhos (itachi, sasuke, obito, shisui)
"""
import os
import numpy as np
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(HERE, 'source')
TEX = os.path.join(HERE, '..', 'src', 'main', 'resources', 'assets', 'sharingan', 'textures')
SIZE = 128


def circle_crop(im):
    im = im.convert('RGBA')
    a = np.array(im)
    rgb = a[:, :, :3].astype(int)
    # o olho e sempre feito de preto (aro/tomoe) e vermelho; fundos sao cinza/branco/transparente
    black = rgb.max(axis=2) < 14
    red = (rgb[:, :, 0] - rgb[:, :, 1]) > 40
    mask = (black | red) & (a[:, :, 3] > 40)
    # remove ruido isolado: so linhas/colunas com bastante pixel do olho contam
    rows = mask.sum(axis=1) > 3
    cols = mask.sum(axis=0) > 3
    mask = mask & rows[:, None] & cols[None, :]
    ys, xs = np.where(mask)
    x0, x1, y0, y1 = xs.min(), xs.max() + 1, ys.min(), ys.max() + 1
    d = max(x1 - x0, y1 - y0)
    cx, cy = (x0 + x1) / 2, (y0 + y1) / 2
    box = (int(round(cx - d / 2)), int(round(cy - d / 2)), int(round(cx + d / 2)), int(round(cy + d / 2)))
    # preenche com transparente caso o recorte saia da imagem
    canvas = Image.new('RGBA', (d, d), (0, 0, 0, 0))
    canvas.paste(im, (-box[0], -box[1]))
    out = canvas.resize((SIZE, SIZE), Image.LANCZOS)
    # mascara circular antialiased (corta qualquer sobra de fundo nas bordas)
    ss = 4
    m = Image.new('L', (SIZE * ss, SIZE * ss), 0)
    ImageDraw.Draw(m).ellipse((1 * ss, 1 * ss, (SIZE - 1) * ss - 1, (SIZE - 1) * ss - 1), fill=255)
    m = m.resize((SIZE, SIZE), Image.LANCZOS)
    alpha = np.minimum(np.array(out)[:, :, 3], np.array(m))
    arr = np.array(out)
    arr[:, :, 3] = alpha
    return Image.fromarray(arr, 'RGBA')


def emblem():
    """Emblema do Cla Uchiha: imagem oficial recortada e centralizada em 64x64 com fundo transparente."""
    im = Image.open(os.path.join(SRC, 'uchiha_emblem.png')).convert('RGBA')
    im = im.crop(im.getbbox())
    size = 64
    scale = (size - 2) / max(im.size)
    im = im.resize((max(1, round(im.width * scale)), max(1, round(im.height * scale))), Image.LANCZOS)
    out = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    out.paste(im, ((size - im.width) // 2, (size - im.height) // 2), im)
    out.save(os.path.join(TEX, 'item', 'uchiha_emblem.png'))


def main():
    os.makedirs(os.path.join(TEX, 'gui'), exist_ok=True)
    os.makedirs(os.path.join(TEX, 'item'), exist_ok=True)
    for name in ['1', '2', '3', 'itachi', 'sasuke', 'obito', 'shisui', 'eterno']:
        img = circle_crop(Image.open(os.path.join(SRC, name + '.png')))
        img.save(os.path.join(TEX, 'gui', 'eye_%s.png' % name))
        if name in ('itachi', 'sasuke', 'obito', 'shisui'):
            img.resize((32, 32), Image.LANCZOS).save(os.path.join(TEX, 'item', 'eye_%s.png' % name))
        if name in ('1', '2', '3'):
            img.resize((32, 32), Image.LANCZOS).save(os.path.join(TEX, 'item', 'sharingan_%s.png' % name))
        if name == 'eterno':
            img.resize((32, 32), Image.LANCZOS).save(os.path.join(TEX, 'item', 'eye_eterno.png'))
        print('ok', name)
    emblem()


if __name__ == '__main__':
    main()
