"""Locate literal references in the retained REA objdump evidence, without running the game."""
import argparse
import json
from pathlib import Path
import struct

parser=argparse.ArgumentParser()
parser.add_argument("literal")
parser.add_argument("--exe",default="Gnomes.exe")
parser.add_argument("--evidence",default="analysis/gnomes-disassembly.rea.json")
args=parser.parse_args()
b=Path(args.exe).read_bytes()
pe=struct.unpack_from("<I",b,60)[0]
count=struct.unpack_from("<H",b,pe+6)[0]
optional_size=struct.unpack_from("<H",b,pe+20)[0]
base=struct.unpack_from("<I",b,pe+24+28)[0]
sections=pe+24+optional_size
needle=args.literal.encode("ascii")
positions=[];p=0
while True:
    p=b.find(needle,p)
    if p<0:break
    positions.append(p);p+=1
evidence=json.loads(Path(args.evidence).read_text(encoding="utf-8-sig"))
lines=evidence["raw_result"]["stdout"].splitlines()
for position in positions:
    for i in range(count):
        s=sections+i*40
        virtual_size,va,size,offset=struct.unpack_from("<4I",b,s+8)
        if offset<=position<offset+size:
            address=base+va+position-offset
            print(f"Literal {args.literal!r}: file 0x{position:x}, VA 0x{address:x}")
            for j,line in enumerate(lines):
                if f"0x{address:x}" in line.lower():
                    print("\n".join(lines[max(0,j-12):j+28]))
            break
if not positions:print("Literal absent")
