# -*- coding: utf-8 -*-
"""各角色核心功能 API 自动化冒烟测试。用法: python docs/test-all-roles.py"""
import json
import sys
import urllib.error
import urllib.request
from io import BytesIO

BASE = "http://localhost:8080/webapi"


def req(method, path, token=None, data=None, form=None):
    url = BASE + path
    headers = {}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    body = None
    if form is not None:
        boundary = "----TestBoundary"
        parts = []
        for k, v in form.items():
            if hasattr(v, "read"):
                filename, content, ctype = v
                parts.append(f"--{boundary}\r\nContent-Disposition: form-data; name=\"{k}\"; filename=\"{filename}\"\r\nContent-Type: {ctype}\r\n\r\n")
                parts.append(content)
                parts.append("\r\n")
            else:
                parts.append(f"--{boundary}\r\nContent-Disposition: form-data; name=\"{k}\"\r\n\r\n{v}\r\n")
        parts.append(f"--{boundary}--\r\n")
        body = b"".join(p.encode() if isinstance(p, str) else p for p in parts)
        headers["Content-Type"] = f"multipart/form-data; boundary={boundary}"
    elif data is not None:
        body = json.dumps(data).encode()
        headers["Content-Type"] = "application/json"
    request = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        with urllib.request.urlopen(request, timeout=120) as resp:
            raw = resp.read()
            ctype = resp.headers.get("Content-Type", "")
            if "json" in ctype:
                return resp.status, json.loads(raw.decode())
            return resp.status, raw
    except urllib.error.HTTPError as e:
        raw = e.read().decode(errors="replace")
        try:
            return e.code, json.loads(raw)
        except Exception:
            return e.code, raw


def login(user, pwd):
    code, res = req("POST", "/auth/login", data={"username": user, "password": pwd})
    if code != 200 or res.get("code") != 200:
        raise RuntimeError(f"login {user} failed: {code} {res}")
    return res["data"]["token"]


def ok(name, passed, detail=""):
    mark = "PASS" if passed else "FAIL"
    print(f"[{mark}] {name}" + (f" — {detail}" if detail else ""))
    return passed


def main():
    results = []

    # 游客
    code, res = req("GET", "/positions")
    results.append(ok("游客 · GET /positions 免登录", code == 200 and res.get("code") == 200, f"count={len(res.get('data', []))}"))
    code, res = req("GET", "/positions/published")
    results.append(ok("游客 · GET /positions/published", code == 200 and res.get("code") == 200))

    # 部门
    dept = login("dept_hr", "dept123")
    code, res = req("GET", "/positions", dept)
    results.append(ok("部门 · 查看岗位列表", code == 200 and res.get("code") == 200))
    code, res = req("POST", "/positions", dept, data={"title": "测试岗API", "description": "自动化测试创建"})
    created = res.get("data", {}) if code == 200 else {}
    results.append(ok("部门 · 创建岗位", code == 200 and res.get("code") == 200))
    pid = created.get("id")
    if pid:
        code, res = req("POST", f"/positions/{pid}/submit", dept)
        results.append(ok("部门 · 提交审批", code == 200 and res.get("code") == 200))

    # HR
    admin = login("admin", "admin123")
    code, res = req("GET", "/recruiter/applications", admin)
    apps = res.get("data", []) if code == 200 else []
    results.append(ok("HR · 招聘漏斗列表", code == 200 and res.get("code") == 200, f"count={len(apps)}"))
    code, res = req("GET", "/management/dashboard", admin)
    results.append(ok("HR · 管理驾驶舱", code == 200 and res.get("code") == 200))
    if pid:
        code, res = req("POST", f"/positions/{pid}/approve", admin, data={"comment": "测试通过"})
        results.append(ok("HR · 审批岗位", code == 200 and res.get("code") == 200))

    # 求职者
    cand = login("candidate", "candidate123")
    code, res = req("GET", "/candidate/applications", cand)
    results.append(ok("求职者 · 我的投递", code == 200 and res.get("code") == 200))
    code, res = req("GET", "/positions/published")
    pubs = res.get("data", []) if code == 200 else []
    results.append(ok("求职者 · 浏览公开岗位", len(pubs) > 0, f"count={len(pubs)}"))
    if pubs:
        apply_pid = pid if pid else pubs[-1]["id"]
        resume_txt = "张三\nzhangsan@test.com\n13800138000\n技能: Java Spring Boot Vue"
        form = {
            "positionId": str(apply_pid),
            "channel": "OFFICIAL",
            "file": ("resume-test.txt", resume_txt.encode(), "text/plain"),
        }
        code, res = req("POST", "/candidate/apply/upload", cand, form=form)
        app_data = res.get("data", {}) if code == 200 else {}
        app_id = app_data.get("id")
        if code == 200 and res.get("code") == 400 and "已投递" in res.get("message", ""):
            # 已投过则取已有记录测原文件
            code2, res2 = req("GET", "/candidate/applications", cand)
            mine = [a for a in res2.get("data", []) if a.get("positionId") == apply_pid]
            app_id = mine[0]["id"] if mine else None
            results.append(ok("求职者 · 文件投递(含原文件)", app_id is not None, "该岗位已投过，复测已有记录"))
        else:
            results.append(ok("求职者 · 文件投递(含原文件)", code == 200 and res.get("code") == 200 and app_data.get("hasResumeFile")))
        if app_id:
            pos_id = apply_pid
            code, res = req("GET", f"/candidate/applications/{app_id}/ai-interview", cand)
            results.append(ok("求职者 · AI初试", code == 200 and res.get("code") == 200))
            code, res = req("POST", f"/candidate/positions/{pos_id}/ask", cand, data={"question": "薪资范围?"})
            results.append(ok("求职者 · AI答疑", code == 200 and res.get("code") == 200))
            code, raw = req("GET", f"/recruiter/applications/{app_id}/resume-file", admin)
            has_file = code == 200 and isinstance(raw, bytes) and len(raw) > 0
            if not has_file:
                results.append(ok("HR · 下载简历原文件", False, "旧数据无原文件，请重新上传PDF投递"))
            else:
                results.append(ok("HR · 下载简历原文件", True, f"bytes={len(raw)}"))

    # 面试官
    iv = login("interviewer", "interview123")
    code, res = req("GET", "/recruiter/applications", iv)
    results.append(ok("面试官 · 查看漏斗", code == 200 and res.get("code") == 200))
    if apps:
        code, res = req("POST", "/recruiter/evaluations", iv, data={
            "interviewId": 1,
            "applicationId": apps[0]["id"],
            "technicalScore": 85,
            "communicationScore": 80,
            "cultureScore": 82,
            "strengths": "技术扎实",
            "weaknesses": "表达略弱",
            "recommendation": "建议进入下一轮",
            "result": "PASS",
        })
        results.append(ok("面试官 · 提交面评(API)", code == 200 and res.get("code") == 200, res.get("message", "") if code != 200 else ""))

    # 管理层
    exec_t = login("executive", "exec123")
    code, res = req("GET", "/management/dashboard", exec_t)
    dash = res.get("data", {}) if code == 200 else {}
    results.append(ok("管理层 · 驾驶舱", code == 200 and bool(dash), f"keys={list(dash.keys())[:5]}"))
    code, res = req("GET", "/recruiter/talent-pool", admin)
    results.append(ok("HR · 人才库", code == 200 and res.get("code") == 200))

    passed = sum(results)
    total = len(results)
    print(f"\n合计: {passed}/{total} 通过")
    sys.exit(0 if passed == total else 1)


if __name__ == "__main__":
    main()
