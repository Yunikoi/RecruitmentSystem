# -*- coding: utf-8 -*-
import sqlite3
from pathlib import Path

db = Path(r"d:\Study\大三\大三下学期\智能开发实践\第二次\docs\fgcc-template\ForguncyDB.sqlite3")
conn = sqlite3.connect(db)
cur = conn.cursor()

cur.execute("SELECT * FROM FGC_UMV_TM")
print("FGC_UMV_TM:", cur.fetchall())

cur.execute("SELECT ID, TableName FROM FGC_UMV_TM")
for row in cur.fetchall():
    tid, tname = row
    print(f"\nTable meta: {tname} id={tid}")
    sc = f"FGC_UMV_SC_{tid.replace('-','').upper()[:24]}"
    # find actual schema table
    cur.execute("SELECT name FROM sqlite_master WHERE type='table'")
    all_t = [r[0] for r in cur.fetchall()]
    for t in all_t:
        if tname.replace('_','') in t or t == tname:
            print(f"  physical: {t}")
            cur.execute(f"PRAGMA table_info([{t}])")
            print(f"  cols: {[c[1] for c in cur.fetchall()]}")

cur.execute("SELECT * FROM FGC_UserInfoTable")
print("\nUsers:", cur.fetchall())

cur.execute("SELECT ID, resume, result FROM review_record_ LIMIT 3")
for r in cur.fetchall():
    print(f"\nreview {r[0]} result len={len(r[2] or '')}")

conn.close()
