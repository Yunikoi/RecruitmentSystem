# -*- coding: utf-8 -*-
import sqlite3
import tempfile
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
for name in ["Document.fgcc", "TalentFlow-ATS.fgcc"]:
    with zipfile.ZipFile(ROOT / name) as z:
        db = z.read("ForguncyDB.sqlite3")
    tmp = tempfile.NamedTemporaryFile(suffix=".db", delete=False)
    tmp.write(db)
    tmp.close()
    conn = sqlite3.connect(tmp.name)
    cur = conn.cursor()
    print(f"\n=== {name} FGC_UMV_TM ===")
    cur.execute("SELECT * FROM FGC_UMV_TM")
    for r in cur.fetchall():
        print(r)
    conn.close()
