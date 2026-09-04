#!/usr/bin/env python3
"""Top-left-anchored PNG crop, dependency-free (stdlib zlib/struct only).

Exists because macOS's `sips --cropToHeightWidth` crops from the *center*
with no documented way to anchor it to a corner (its --cropOffset flag does
not do what its name implies), which silently truncates the top of a
screenshot when trimming excess height. This crops from (0, 0) instead, so
a tall raw screenshot can be trimmed down to its actual content height
without losing its header row.

Usage: python3 png_crop.py <src.png> <dst.png> <top> <height>
"""
import struct, zlib, sys

def read_png(path):
    with open(path, 'rb') as f:
        data = f.read()
    assert data[:8] == b'\x89PNG\r\n\x1a\n'
    pos = 8
    width = height = bitdepth = colortype = None
    idat = b''
    while pos < len(data):
        length = struct.unpack('>I', data[pos:pos+4])[0]
        ctype = data[pos+4:pos+8]
        cdata = data[pos+8:pos+8+length]
        pos += 8 + length + 4
        if ctype == b'IHDR':
            width, height, bitdepth, colortype = struct.unpack('>IIBB', cdata[:10])
        elif ctype == b'IDAT':
            idat += cdata
        elif ctype == b'IEND':
            break
    assert bitdepth == 8, f"unsupported bitdepth {bitdepth}"
    channels = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[colortype]
    raw = zlib.decompress(idat)
    stride = width * channels
    rows = []
    prev = bytearray(stride)
    o = 0
    for _ in range(height):
        filt = raw[o]; o += 1
        line = bytearray(raw[o:o+stride]); o += stride
        if filt == 1:
            for i in range(stride):
                a = line[i-channels] if i >= channels else 0
                line[i] = (line[i] + a) & 0xFF
        elif filt == 2:
            for i in range(stride):
                line[i] = (line[i] + prev[i]) & 0xFF
        elif filt == 3:
            for i in range(stride):
                a = line[i-channels] if i >= channels else 0
                b = prev[i]
                line[i] = (line[i] + (a + b) // 2) & 0xFF
        elif filt == 4:
            for i in range(stride):
                a = line[i-channels] if i >= channels else 0
                b = prev[i]
                c = prev[i-channels] if i >= channels else 0
                p = a + b - c
                pa, pb, pc = abs(p-a), abs(p-b), abs(p-c)
                pr = a if pa <= pb and pa <= pc else (b if pb <= pc else c)
                line[i] = (line[i] + pr) & 0xFF
        rows.append(bytes(line))
        prev = line
    return width, height, channels, rows

def write_png(path, width, height, channels, rows):
    colortype = {1: 0, 3: 2, 2: 4, 4: 6}[channels]
    ihdr = struct.pack('>IIBBBBB', width, height, 8, colortype, 0, 0, 0)
    raw = bytearray()
    for row in rows:
        raw.append(0)
        raw.extend(row)
    comp = zlib.compress(bytes(raw), 9)
    def chunk(tag, data):
        return struct.pack('>I', len(data)) + tag + data + struct.pack('>I', zlib.crc32(tag + data))
    out = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', ihdr) + chunk(b'IDAT', comp) + chunk(b'IEND', b'')
    with open(path, 'wb') as f:
        f.write(out)

if __name__ == "__main__":
    src, dst, top, height_px = sys.argv[1], sys.argv[2], int(sys.argv[3]), int(sys.argv[4])
    w, h, ch, rows = read_png(src)
    end = min(h, top + height_px)
    cropped = rows[top:end]
    write_png(dst, w, len(cropped), ch, cropped)
    print(f"{src}: {w}x{h} -> {dst}: {w}x{len(cropped)}")
