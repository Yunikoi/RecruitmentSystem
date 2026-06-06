# -*- coding: utf-8 -*-
import sqlite3
import tempfile
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
with zipfile.ZipFile(ROOT / "Document.fgcc") as z:
    db = z.read("ForguncyDB.sqlite3")
tmp = tempfile.NamedTemporaryFile(suffix=".db", delete=False)
tmp.write(db)
tmp.close()
conn = sqlite3.connect(tmp.name)
cur = conn.cursor()
for t in ["FGC_UMV_TM", "FGC_UMV_SC_E3B01A2B6AECA5DA98"]:
    print(f"\n=== {t} ===")
    cur.execute(f"PRAGMA table_info([{t}])")
    print("cols:", [c[1] for c in cur.fetchall()])
    cur.execute(f"SELECT * FROM [{t}] LIMIT 5")
    rows = cur.fetchall()
    for r in rows:
        print(r)
conn.close()
