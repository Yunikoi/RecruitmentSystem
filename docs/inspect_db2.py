# -*- coding: utf-8 -*-
import sqlite3
from pathlib import Path

for dbname in ["ForguncyDB.sqlite3", "ProcessForguncyDB.sqlite3"]:
    db = Path(r"d:\Study\大三\大三下学期\智能开发实践\第二次\docs\fgcc-template") / dbname
    if not db.exists():
        continue
    print("===", dbname, "===")
    conn = sqlite3.connect(db)
    cur = conn.cursor()
    cur.execute("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name")
    for (t,) in cur.fetchall():
        cur.execute(f"SELECT COUNT(*) FROM [{t}]")
        print(f"  {t}: {cur.fetchone()[0]}")
    conn.close()
