"""Package only the new engine sources and documentation; never original game assets."""
import hashlib
from pathlib import Path
import sys
from zipfile import ZipFile, ZIP_DEFLATED

version=sys.argv[1] if len(sys.argv)>1 else "0.33"
root=Path(__file__).resolve().parents[1]
files=list((root/"android").rglob("*.java"))+[root/"android/AndroidManifest.xml"]
files+=list((root/"tests").rglob("*.java"))+list((root/"tests").glob("*.py"))+[root/"tests/device/AndroidManifest.xml"]
files+=list(root.glob("*.ps1"))
files+=[root/"analysis/TESTS-0.9.md"]
files+=[root/"analysis/TESTS-0.10.md"]
files+=[root/"analysis/TESTS-0.11.md"]
files+=[root/"analysis/TESTS-0.12.md"]
files+=[root/"analysis/TESTS-0.13.md"]
files+=[root/"analysis/TESTS-0.14.md"]
files+=[root/"analysis/TESTS-0.15.md"]
files+=[root/"analysis/TESTS-0.16.md"]
files+=[root/"analysis/TESTS-0.17.md"]
files+=[root/"analysis/TESTS-0.18.md"]
files+=[root/"analysis/TESTS-0.19.md"]
files+=[root/"analysis/TESTS-0.20.md"]
files+=[root/"analysis/TESTS-0.21.md"]
files+=[root/"analysis/TESTS-0.22.md"]
files+=[root/"analysis/TESTS-0.23.md"]
files+=[root/"analysis/TESTS-0.24.md"]
files+=[root/"analysis/TESTS-0.25.md"]
files+=[root/"analysis/TESTS-0.26.md"]
files+=[root/"analysis/TESTS-0.27.md"]
files+=[root/"analysis/TESTS-0.28.md"]
files+=[root/"analysis/TESTS-0.29.md"]
files+=[root/"analysis/TESTS-0.30.md"]
files+=[root/"analysis/TESTS-0.31.md"]
files+=[root/"analysis/TESTS-0.32.md"]
files+=[root/p for p in ["README.md","LICENSE",".gitignore","tools/native_probe.py","tools/package_sources.py","tools/rea/package.json","tools/rea/package-lock.json","analysis/FORMAT.md","analysis/RESEARCH.md","analysis/TESTS-0.6.md","analysis/TESTS-0.7.md","analysis/TESTS-0.8.md","analysis/reader-tests.txt"]]
for p in files:
    if not p.is_file():raise FileNotFoundError(p)
    if p.suffix.lower() not in {".java",".xml",".ps1",".py",".md",".json",".txt",""}:raise ValueError(p)
destination=root/f"dist/Brave-Open-Dwarves-{version}-sources.zip"
with ZipFile(destination,"w",ZIP_DEFLATED) as z:
    for p in sorted(set(files)):z.write(p,p.relative_to(root).as_posix())
with ZipFile(root/f"dist/Brave-Open-Dwarves-{version}.apk") as z:
    if set(z.namelist())!={"AndroidManifest.xml","classes.dex","META-INF/ANDROIDD.SF","META-INF/ANDROIDD.RSA","META-INF/MANIFEST.MF"}:raise ValueError("Unexpected APK files")
checksums=[]
for p in sorted((root/"dist").iterdir()):
    if p.suffix in {".zip",".apk"}:checksums.append(hashlib.sha256(p.read_bytes()).hexdigest()+"  "+p.name)
(root/"dist/SHA256SUMS.txt").write_text("\n".join(checksums)+"\n",encoding="ascii")
print(f"Packaged {len(set(files))} source/document files: {destination.name}; APK contains code/manifest/signatures only")
