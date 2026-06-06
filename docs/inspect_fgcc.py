# -*- coding: utf-8 -*-
import sqlite3
import json
from pathlib import Path

dest = Path(r"d:\Study\大三\大三下学期\智能开发实践\第二次\docs\fgcc-template")

for name in ["DocumentInfo", "ApplicationSettings.json", "DataConnectionSet.json"]:
    p = dest / name if name.endswith(".json") else dest / name
    if p.exists():
        print(f"\n=== {name} ===")
        text = p.read_text(encoding="utf-8", errors="replace")
        print(text[:2000])

print("\n=== Tables JSON ===")
for p in sorted(dest.glob("Tables/**/*.json")):
    if "View" not in p.name:
        print(p.relative_to(dest))

print("\n=== Custom Pages ===")
for p in sorted(dest.glob("Pages/**/*.json")):
    rel = str(p.relative_to(dest))
    if "内置" not in rel and "FGC_" in p.name:
        print(rel)

db = dest / "ForguncyDB.sqlite3"
conn = sqlite3.connect(db)
cur = conn.cursor()
cur.execute("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name")
tables = [r[0] for r in cur.fetchall()]
print("\n=== SQLite tables ===")
for t in tables:
    if not t.startswith("sqlite_"):
        try:
            cur.execute(f"SELECT COUNT(*) FROM [{t}]")
            cnt = cur.fetchone()[0]
            print(f"  {t}: {cnt} rows")
        except Exception as e:
            print(f"  {t}: err {e}")

# List user tables (FGC_ prefix or custom)
for t in tables:
    if t.startswith("FGC_") or t in ("position", "application", "sys_user", "review_record_"):
        cur.execute(f"PRAGMA table_info([{t}])")
        cols = [c[1] for c in cur.fetchall()]
        print(f"\nSchema {t}: {cols}")

conn.close()
