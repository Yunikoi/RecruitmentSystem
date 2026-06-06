# -*- coding: utf-8 -*-
import io
import re
import sqlite3
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent

for fgcc_name in ["Document.fgcc", "TalentFlow-ATS.fgcc"]:
    p = ROOT / fgcc_name
    if not p.exists():
        continue
    print(f"\n=== {fgcc_name} ===")
    with zipfile.ZipFile(p, "r") as z:
        # empty dirs
        dirs = [n for n in z.namelist() if n.endswith("/")]
        print("dir entries:", len(dirs))
        # page sig
        for page in ["Pages/home.json", "Pages/submit.json", "Pages/history.json"]:
            if page in z.namelist():
                text = z.read(page).decode("utf-8", errors="replace")
                has_sig = "}//" in text
                print(f"  {page}: sig={has_sig}, len={len(text)}")
            elif page.replace("home", "home") == page:
                print(f"  {page}: MISSING")
        if "Pages/home.json" not in z.namelist():
            print("  Pages/home.json: MISSING")

        db = z.read("ForguncyDB.sqlite3")
        import tempfile
        with tempfile.NamedTemporaryFile(suffix=".sqlite3", delete=False) as tmp:
            tmp.write(db)
            tmp_path = tmp.name
    conn = sqlite3.connect(tmp_path)
    cur = conn.cursor()
    cur.execute("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name")
    print("  sqlite tables:", [r[0] for r in cur.fetchall()])

# analyze signature format from submit.json
submit = (ROOT / "docs" / "fgcc-template" / "Pages" / "submit.json").read_text(encoding="utf-8")
m = re.search(r"\}//(.+)\|(\d+)$", submit, re.DOTALL)
if m:
    sig, num = m.group(1), m.group(2)
    json_part = submit[: submit.find("}//") + 1]
    print("\nSignature analysis submit.json:")
    print("  declared len:", num)
    print("  actual json len:", len(json_part))
    print("  sig len:", len(sig))
