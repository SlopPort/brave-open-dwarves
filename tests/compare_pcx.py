"""Independent Pillow oracle against the Java reader's raw RGBA hashes.

Reads the user's original archive directly; does not extract or redistribute assets.
"""
import hashlib
import io
from pathlib import Path
import struct
import sys
import zlib
from PIL import Image

archive_path = Path(sys.argv[1] if len(sys.argv) > 1 else "Gnomes.dat")
report_path = Path(sys.argv[2] if len(sys.argv) > 2 else "analysis/reader-tests.txt")
blob = archive_path.read_bytes()
expected = {}
position = 256
for _ in range(struct.unpack_from("<I", blob, 38)[0]):
    start, tag, end, raw_size, size, reserved = struct.unpack_from("<IIIIIH", blob, position)
    nul = blob.index(0, position + 22, end)
    name = blob[position + 22:nul].decode("cp1252").replace("\\", "/")
    if name.lower().endswith(".pcx"):
        raw = zlib.decompress(blob[nul + 1:nul + 1 + size])
        assert len(raw) == raw_size
        with Image.open(io.BytesIO(raw)) as image:
            expected[name] = (image.size, hashlib.sha256(image.convert("RGBA").tobytes()).hexdigest())
    position = end
assert position == len(blob)
observed = {}
for line in report_path.read_text(encoding="utf-8-sig").splitlines():
    if line.startswith("PCX\t"):
        _, name, width, height, digest = line.split("\t")
        observed[name] = ((int(width), int(height)), digest)
assert observed == expected, "Java/Pillow image size or pixel mismatch"
print(f"PASS: {len(observed)} PCX images, exact raw RGBA match with Pillow")
