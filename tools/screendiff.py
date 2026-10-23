#!/usr/bin/env python3
"""Bandingkan screenshot sebelum dan sesudah, lalu gambar pasangannya untuk pull request.

Perubahan performa seharusnya kelihatan sama persis. Skrip ini yang bilang apakah iya: membandingkan
dua screenshot piksel demi piksel, mengabaikan daerah yang kamu --mask (jamnya jalan), dan melaporkan
berapa piksel yang beda dan di mana. Juga menulis pasangan berdampingan, dengan daerah yang kamu --blur
dibuat piksel (nama Wi-Fi, foto akun), siap dilampirkan.

    tools/screendiff.py shots/before.png shots/after.png --mask 90,40,900,220 \\
        --blur 1265,125,1525,197 --out shots/before-after.png

Kotak adalah x0,y0,x1,y1 dalam piksel screenshot. Status keluar: 0 sama, 1 beda, 2 error.
Butuh Pillow (tools/requirements-perf.txt). Baca tools/screendiff.md dulu.
"""
import argparse
import re
import sys

BOX = re.compile(r"^\d+,\d+,\d+,\d+$")


def box(text):
    """'x0,y0,x1,y1' as a tuple, x0 < x1 and y0 < y1."""
    if not BOX.match(text):
        raise argparse.ArgumentTypeError(f"kotak adalah x0,y0,x1,y1 dalam piksel, bukan {text!r}")
    x0, y0, x1, y1 = map(int, text.split(","))
    if x0 >= x1 or y0 >= y1:
        raise argparse.ArgumentTypeError(f"kotak butuh x0 < x1 dan y0 < y1: {text!r}")
    return x0, y0, x1, y1


def compare(a, b, masks, threshold):
    """(pixels that differ by more than threshold in any channel, their bounding box or None, the largest difference)."""
    return _compare(a, b, masks, threshold)[:3]


def _compare(a, b, masks, threshold):
    """The comparison numbers and the binary pixel mask they describe."""
    from PIL import ImageChops, ImageDraw
    a, b = a.convert("RGB").copy(), b.convert("RGB").copy()
    for im in (a, b):
        draw = ImageDraw.Draw(im)
        for m in masks:
            draw.rectangle([m[0], m[1], m[2] - 1, m[3] - 1], fill="black")
    diff = ImageChops.difference(a, b)
    # The largest channel difference per pixel, then a 0/255 mask of pixels over the threshold.
    r, g, bl = diff.split()
    worst = ImageChops.lighter(ImageChops.lighter(r, g), bl)
    over = worst.point(lambda v: 255 if v > threshold else 0)
    count = over.histogram()[255]
    return count, over.getbbox(), max(worst.getdata()), over


def redact(im, region):
    """Pixelate then blur a region, so text in it can't be read back."""
    from PIL import Image, ImageFilter
    part = im.crop(region)
    small = part.resize((max(1, part.size[0] // 14), max(1, part.size[1] // 14)), Image.BILINEAR)
    im.paste(small.resize(part.size, Image.NEAREST).filter(ImageFilter.GaussianBlur(4)), region)
    return im


def pair(a, b, blurs, labels, scale):
    """The two screenshots side by side, blurred where asked, scaled, with a label over each."""
    from PIL import Image, ImageDraw, ImageFont
    a, b = a.convert("RGB").copy(), b.convert("RGB").copy()
    for region in blurs:
        redact(a, region)
        redact(b, region)
    w, h = int(a.size[0] * scale), int(a.size[1] * scale)
    a, b = a.resize((w, h), Image.LANCZOS), b.resize((w, h), Image.LANCZOS)
    out = Image.new("RGB", (2 * w + 30, h + 60), "white")
    out.paste(a, (0, 60))
    out.paste(b, (w + 30, 60))
    draw, font = ImageDraw.Draw(out), ImageFont.load_default(size=30)
    draw.text((10, 14), labels[0], fill="black", font=font)
    draw.text((w + 40, 14), labels[1], fill="black", font=font)
    return out


def parse(argv):
    p = argparse.ArgumentParser(description="Bandingkan dua screenshot dan gambar pasangannya. Lihat tools/screendiff.md.")
    p.add_argument("before")
    p.add_argument("after")
    p.add_argument("--mask", type=box, action="append", default=[], metavar="BOX",
                   help="abaikan satu daerah dari perbandingan (boleh diulang), misalnya jam")
    p.add_argument("--blur", type=box, action="append", default=[], metavar="BOX",
                   help="jadikan piksel satu daerah di gambar (boleh diulang), misalnya nama Wi-Fi")
    p.add_argument("--out", help="tulis pasangan berdampingan ke sini")
    p.add_argument("--diff", metavar="PNG", help="tulis piksel yang beda ke sini, putih di atas hitam")
    p.add_argument("--labels", default="Before,After", help="label di atas pasangan, dipisah koma")
    p.add_argument("--scale", type=float, default=0.5, help="ukuran tiap screenshot di pasangan (default 0.5)")
    p.add_argument("--threshold", type=int, default=8,
                   help="abaikan selisih kanal sampai angka ini (0-255, default 8: derau kompresi)")
    args = p.parse_args(argv)
    if not 0 < args.scale <= 1:
        p.error("--scale ada di antara 0 dan 1")
    if not 0 <= args.threshold <= 255:
        p.error("--threshold ada di antara 0 dan 255")
    labels = [l.strip() for l in args.labels.split(",")]
    if len(labels) != 2:
        p.error("--labels butuh dua label")
    args.label_pair = labels
    return args


def main(argv=None):
    args = parse(sys.argv[1:] if argv is None else argv)
    try:
        from PIL import Image
    except ImportError:
        print("screendiff.py butuh Pillow: tools/venv/bin/pip install -r tools/requirements-perf.txt", file=sys.stderr)
        return 2
    try:
        a, b = Image.open(args.before), Image.open(args.after)
    except OSError as e:
        print(f"tidak bisa baca screenshot: {e}", file=sys.stderr)
        return 2
    if a.size != b.size:
        print(f"ukuran screenshot beda: {a.size[0]}x{a.size[1]} dan {b.size[0]}x{b.size[1]}", file=sys.stderr)
        return 2
    outside = [r for r in args.mask + args.blur if r[2] > a.size[0] or r[3] > a.size[1]]
    if outside:
        print(f"ada kotak di luar screenshot {a.size[0]}x{a.size[1]}: {outside[0]}", file=sys.stderr)
        return 2
    count, bbox, worst, diff = _compare(a, b, args.mask, args.threshold)
    if args.out:
        pair(a, b, args.blur, args.label_pair, args.scale).save(args.out)
        print(f"{args.out}: tertulis")
    if args.diff:
        diff.save(args.diff)
    masked = f" ({len(args.mask)} region{'s' if len(args.mask) != 1 else ''} masked)" if args.mask else ""
    if count == 0:
        print(f"identical{masked}: tidak ada piksel yang beda lebih dari {args.threshold}")
        return 0
    print(f"beda{masked}: {count} pixels differ by more than {args.threshold}, inside "
          f"{bbox[0]},{bbox[1]},{bbox[2]},{bbox[3]} (selisih terbesar {worst})")
    return 1


if __name__ == "__main__":
    sys.exit(main())
