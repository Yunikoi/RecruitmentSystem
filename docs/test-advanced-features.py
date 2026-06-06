# -*- coding: utf-8 -*-
"""高级功能冒烟测试。用法: python docs/test-advanced-features.py"""
import json
import sys
import urllib.error
import urllib.request

BASE = "http://localhost:8080/webapi"
PASS = FAIL = 0


def login(u, p):
    r = urllib.request.urlopen(urllib.request.Request(
        BASE + "/auth/login",
        json.dumps({"username": u, "password": p}).encode(),
        headers={"Content-Type": "application/json"}, method="POST"))
    return json.loads(r.read())["data"]["token"]


def api(method, path, token=None, data=None):
    body = json.dumps(data).encode() if data is not None else None
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = "Bearer " + token
    req = urllib.request.Request(BASE + path, body, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=120) as resp:
            return resp.status, json.loads(resp.read().decode())
    except urllib.error.HTTPError as e:
        raw = e.read().decode(errors="replace")
        try:
            return e.code, json.loads(raw)
        except Exception:
            return e.code, {"message": raw}


def ok(name, cond, detail=""):
    global PASS, FAIL
    if cond:
        PASS += 1
        print(f"[PASS] {name}" + (f" — {detail}" if detail else ""))
    else:
        FAIL += 1
        print(f"[FAIL] {name}" + (f" — {detail}" if detail else ""))


def main():
    dept = login("dept_hr", "dept123")
    admin = login("admin", "admin123")
    cand = login("candidate", "candidate123")
    interviewer = login("interviewer", "interview123")
    execu = login("executive", "exec123")

    # JD Copilot
    code, res = api("POST", "/positions/copilot", dept, {"brief": "3年Java后端，懂Spring Boot"})
    ok("部门 · JD Copilot", code == 200 and res.get("code") == 200 and res.get("data", {}).get("title"))

    # Compliance
    code, res = api("GET", "/compliance/settings", admin)
    ok("HR · 合规配置读取", code == 200 and res.get("code") == 200)
    code, res = api("PUT", "/compliance/settings", admin, {"blindHiringEnabled": True, "blindReviewEnabled": True})
    ok("HR · 盲筛/匿名面评开关", code == 200 and res.get("code") == 200)

    # Mock interview
    code, res = api("GET", "/candidate/applications", cand)
    apps = res.get("data", []) if code == 200 else []
    app_id = apps[0]["id"] if apps else None
    if app_id:
        code, res = api("GET", f"/candidate/applications/{app_id}/mock-interview", cand)
        ok("C端 · 模拟面试状态", code == 200 and res.get("code") == 200)
        if res.get("data", {}).get("canStart"):
            code, res = api("POST", f"/candidate/applications/{app_id}/mock-interview/start", cand)
            sid = res.get("data", {}).get("sessionId") if code == 200 else None
            if sid:
                code, res = api("POST", f"/candidate/applications/{app_id}/mock-interview/submit", cand,
                                {"sessionId": sid, "answers": "我有3年Java经验，熟悉Spring Boot微服务"})
                ok("C端 · 模拟面试提交", code == 200 and res.get("code") == 200, f"score={res.get('data', {}).get('score')}")

    # Calendar slots
    if app_id:
        code, res = api("GET", f"/candidate/applications/{app_id}/calendar-slots", cand)
        slots = res.get("data", []) if code == 200 else []
        ok("C端 · 日程看板", code == 200, f"slots={len(slots)}")
        if slots:
            code, res = api("POST", f"/candidate/applications/{app_id}/calendar-slots/{slots[0]['id']}/book", cand)
            ok("C端 · 一键预约", code == 200 and res.get("code") == 200)

    # Workflow & CRM & Duplicates
    code, res = api("GET", "/recruiter/applications", admin)
    rec_apps = res.get("data", []) if code == 200 else []
    pid = rec_apps[0]["positionId"] if rec_apps else 1
    code, res = api("GET", f"/recruiter/positions/{pid}/workflow", admin)
    ok("HR · 工作流读取", code == 200 and res.get("code") == 200)
    code, res = api("GET", f"/recruiter/talent-pool/match?positionId={pid}", admin)
    ok("HR · 人才库AI匹配", code == 200 and res.get("code") == 200)
    code, res = api("GET", "/recruiter/duplicates", admin)
    ok("HR · 查重检测", code == 200 and res.get("code") == 200)

    # Integrations
    if app_id:
        code, res = api("POST", f"/recruiter/applications/{app_id}/offer", admin)
        ok("HR · 发起 Offer", code == 200 and res.get("code") == 200)
        code, res = api("POST", f"/recruiter/applications/{app_id}/background-check", admin)
        ok("HR · 发起背调", code == 200 and res.get("code") == 200)
        code, res = api("GET", f"/recruiter/applications/{app_id}/background-check", admin)
        ok("HR · 背调报告", code == 200 and res.get("data", {}).get("reportSummary"))

    # Interviewer collab & summary
    if app_id:
        code, res = api("GET", f"/recruiter/applications/{app_id}/collab-code", interviewer)
        ok("面试官 · 协同编程", code == 200 and res.get("data", {}).get("code"))
        code, res = api("GET", f"/recruiter/applications/{app_id}/meeting-summary", interviewer)
        ok("面试官 · AI面试摘要", code == 200 and res.get("data", {}).get("summary"))

    # M端预测
    code, res = api("GET", "/management/dashboard", execu)
    d = res.get("data", {}) if code == 200 else {}
    ok("M端 · 到岗预测", code == 200 and d.get("hirePredictions") is not None)
    ok("M端 · 渠道留存矩阵", code == 200 and d.get("churnMatrix") is not None)

    # Audit log
    code, res = api("GET", "/compliance/audit-logs", admin)
    ok("合规 · 审计日志", code == 200 and isinstance(res.get("data"), list))

    # Remind
    code, res = api("GET", "/positions", dept)
    pending = [p for p in res.get("data", []) if p.get("status") == "PENDING"]
    if pending:
        code, res = api("POST", f"/positions/{pending[0]['id']}/remind", dept)
        ok("部门 · 催办审批", code == 200 and res.get("code") == 200)

    print(f"\n合计: {PASS}/{PASS+FAIL} 通过")
    sys.exit(0 if FAIL == 0 else 1)


if __name__ == "__main__":
    main()
