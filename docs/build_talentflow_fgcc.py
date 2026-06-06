# -*- coding: utf-8 -*-
"""
从 Document.fgcc 模板生成 TalentFlow-ATS.fgcc 活字格工程（完整三端）。
用法: python docs/build_talentflow_fgcc.py
"""
import json
import shutil
import sqlite3
import zipfile
from datetime import datetime
from pathlib import Path

from fgcc_page_builder import generate_all_pages

ROOT = Path(__file__).resolve().parent.parent
TEMPLATE = ROOT / "Document.fgcc"
OUT_FGCC = ROOT / "TalentFlow-ATS.fgcc"
WORK = Path(__file__).resolve().parent / "fgcc-build"

COL_TYPE = "System.String, System.Private.CoreLib, Version=8.0.0.0, Culture=neutral, PublicKeyToken=7cec85d7bea7798e"
LONG_TYPE = "System.Int64, System.Private.CoreLib, Version=8.0.0.0, Culture=neutral, PublicKeyToken=7cec85d7bea7798e"
INT_TYPE = "System.Int32, System.Private.CoreLib, Version=8.0.0.0, Culture=neutral, PublicKeyToken=7cec85d7bea7798e"
DT_TYPE = "System.DateTime, System.Private.CoreLib, Version=8.0.0.0, Culture=neutral, PublicKeyToken=7cec85d7bea7798e"
BOOL_TYPE = "System.Boolean, System.Private.CoreLib, Version=8.0.0.0, Culture=neutral, PublicKeyToken=7cec85d7bea7798e"


def col_string(name):
    return {
        "$type": "Forguncy.SaveLoad.BindingColumnSaveData, ServerDesignerCommon",
        "DatabaseColumnType": "System.String",
        "Name": name,
        "ColumnType": COL_TYPE,
    }


def col_int64(name, pk=False):
    d = {
        "$type": "Forguncy.SaveLoad.BindingColumnSaveData, ServerDesignerCommon",
        "DatabaseColumnType": "System.Int64",
        "Name": name,
        "ColumnType": LONG_TYPE,
    }
    if pk:
        d.update({"Required": True, "Unique": True, "AutoIncrement": True, "MaxLength": -1})
    return d


def col_int32(name):
    return {
        "$type": "Forguncy.SaveLoad.BindingColumnSaveData, ServerDesignerCommon",
        "DatabaseColumnType": "System.Int32",
        "Name": name,
        "ColumnType": INT_TYPE,
    }


def col_bool(name):
    return {
        "$type": "Forguncy.SaveLoad.BindingColumnSaveData, ServerDesignerCommon",
        "DatabaseColumnType": "System.Boolean",
        "Name": name,
        "ColumnType": BOOL_TYPE,
    }


def col_user(name):
    return {
        "$type": "Forguncy.SaveLoad.UserExtraBingingColumnSaveData, ServerDesignerCommon",
        "ColumnType": COL_TYPE,
        "Name": name,
    }


def col_date(name):
    return {
        "$type": "Forguncy.SaveLoad.DateExtraBingingColumnSaveData, ServerDesignerCommon",
        "ColumnType": DT_TYPE,
        "Name": name,
    }


def fgc_audit_cols():
    return [
        col_user("FGC_Creator"),
        col_date("FGC_CreateDate"),
        col_user("FGC_LastModifier"),
        col_date("FGC_LastModifyDate"),
    ]


def make_table_json(name, columns, pk=None):
    return {"Name": name, "Columns": columns, "PrimaryKey": pk or ["ID"], "Indexes": []}


TABLES = {
    "position_": make_table_json(
        "position_",
        [
            col_int64("ID", pk=True),
            col_string("title"),
            col_string("description"),
            col_string("status"),
            col_string("department"),
            col_string("created_by_name"),
            col_int64("created_by_id"),
            col_string("approver"),
            col_string("approval_comment"),
            col_date("published_at"),
            *fgc_audit_cols(),
        ],
    ),
    "application_": make_table_json(
        "application_",
        [
            col_int64("ID", pk=True),
            col_int64("position_id"),
            col_string("position_title"),
            col_int64("candidate_id"),
            col_string("candidate_name"),
            col_string("candidate_email"),
            col_string("candidate_phone"),
            col_string("stage"),
            col_string("channel"),
            col_string("resume"),
            col_string("parsed_skills"),
            col_int32("match_score"),
            col_string("match_highlights"),
            col_string("match_risks"),
            col_string("ai_feedback"),
            col_string("recommended_positions"),
            col_bool("in_talent_pool"),
            col_date("applied_at"),
            col_date("stage_updated_at"),
            *fgc_audit_cols(),
        ],
    ),
    "interview_": make_table_json(
        "interview_",
        [
            col_int64("ID", pk=True),
            col_int64("application_id"),
            col_int64("interviewer_id"),
            col_string("interviewer_name"),
            col_string("type"),
            col_date("scheduled_at"),
            col_string("location"),
            col_string("status"),
            col_string("questions"),
            col_date("created_at"),
            *fgc_audit_cols(),
        ],
    ),
    "interview_evaluation_": make_table_json(
        "interview_evaluation_",
        [
            col_int64("ID", pk=True),
            col_int64("interview_id"),
            col_int64("application_id"),
            col_int64("evaluator_id"),
            col_string("evaluator_name"),
            col_int32("technical_score"),
            col_int32("communication_score"),
            col_int32("culture_score"),
            col_int32("overall_score"),
            col_string("strengths"),
            col_string("weaknesses"),
            col_string("recommendation"),
            col_string("result"),
            col_date("created_at"),
            *fgc_audit_cols(),
        ],
    ),
    "talent_pool_": make_table_json(
        "talent_pool_",
        [
            col_int64("ID", pk=True),
            col_int64("application_id"),
            col_string("candidate_name"),
            col_string("parsed_skills"),
            col_int32("match_score"),
            col_string("stage"),
            col_string("channel"),
            col_string("notes"),
            *fgc_audit_cols(),
        ],
    ),
    "dashboard_stat_": make_table_json(
        "dashboard_stat_",
        [
            col_int64("ID", pk=True),
            col_string("category"),
            col_string("metric_key"),
            col_string("metric_value"),
            col_string("remark"),
            *fgc_audit_cols(),
        ],
    ),
    "sys_user_": make_table_json(
        "sys_user_",
        [
            col_int64("ID", pk=True),
            col_string("username"),
            col_string("display_name"),
            col_string("department"),
            col_string("role"),
            col_string("email"),
            *fgc_audit_cols(),
        ],
    ),
}

TALENTFLOW_RESUME = """# 个人简历 - TalentFlow ATS 演示

**姓名：** 张同学
**邮箱：** zhang@example.com | **电话：** 13800138000

## 教育背景
西安电子科技大学 | 软件工程 | 本科

## 专业技能
Java, Spring Boot, JPA, MySQL, Vue 3, Element Plus

## 项目经验
TalentFlow ATS 智能招聘平台 - Spring Boot + Vue 3 + 活字格低代码"""

SAMPLE_REVIEW = """【TalentFlow AI 初筛结果】
匹配岗位：Java 后端开发工程师
匹配分：88/99
亮点：Java、Spring Boot、微服务
建议：进入业务面试环节"""


def extract_template():
    if WORK.exists():
        shutil.rmtree(WORK)
    WORK.mkdir(parents=True)
    with zipfile.ZipFile(TEMPLATE, "r") as z:
        z.extractall(WORK)


def write_table_json_files():
    tables_dir = WORK / "Tables"
    tables_dir.mkdir(exist_ok=True)
    for name, schema in TABLES.items():
        (tables_dir / f"{name}.json").write_text(
            json.dumps(schema, ensure_ascii=False, indent=2), encoding="utf-8"
        )


def patch_application_settings():
    path = WORK / "ApplicationSettings.json"
    data = json.loads(path.read_text(encoding="utf-8"))
    data["WebSitePort"] = 23510
    data["UserServicePort"] = 30254
    # 必须用已签名且未改内容的页面作首页（submit 与 Document.fgcc 一致）
    data["StartPage"] = "submit"
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")


def patch_agent_command():
    path = WORK / "ServerCommands" / "agent.json"
    if not path.exists():
        return
    data = json.loads(path.read_text(encoding="utf-8"))
    for trigger in data.get("Triggers", {}).get("$values", []):
        for param in trigger.get("Parameters", {}).get("$values", []):
            if param.get("Name") == "resume":
                param["TestData"] = TALENTFLOW_RESUME
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")


def _exec(cur, sql, params=()):
    cur.execute(sql, params)


def init_sqlite_data():
    """只改模板里已有的表；禁止 CREATE TABLE（与活字格元数据不同步会导致工程损坏）。"""
    db_path = WORK / "ForguncyDB.sqlite3"
    conn = sqlite3.connect(db_path)
    cur = conn.cursor()
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    _exec(cur, "DELETE FROM review_record_")
    _exec(
        cur,
        "INSERT INTO review_record_ (resume, result, FGC_Creator, FGC_CreateDate) VALUES (?,?,?,?)",
        (TALENTFLOW_RESUME, SAMPLE_REVIEW, "candidate", now),
    )
    _exec(
        cur,
        "INSERT INTO review_record_ (resume, result, FGC_Creator, FGC_CreateDate) VALUES (?,?,?,?)",
        (
            "Vue3 前端工程师，2年经验，熟悉 Element Plus 与 ECharts。",
            "【TalentFlow AI初筛】匹配分 76，建议进入初筛。",
            "candidate",
            now,
        ),
    )

    _exec(cur, "DELETE FROM FGC_UserInfoTable")
    fgc_users = [
        ("Administrator", "系统管理员", "admin@talentflow.demo", 1, "Administrator", None),
        ("candidate", "张同学", "zhang@example.com", 1, "User", None),
        ("admin", "招聘HR", "hr@talentflow.demo", 1, "User", None),
        ("interviewer", "李面试官", "interviewer@talentflow.demo", 1, "User", None),
        ("executive", "王总监", "exec@talentflow.demo", 1, "User", None),
        ("dept_hr", "人事部专员", "dept_hr@talentflow.demo", 1, "User", None),
        ("dept_tech", "技术部专员", "dept_tech@talentflow.demo", 1, "User", None),
    ]
    for u in fgc_users:
        _exec(
            cur,
            """INSERT INTO FGC_UserInfoTable
               (FGC_UserName, FGC_FullName, FGC_Email, FGC_IsEnabled, FGC_Role, FGC_OrganizationSuperior)
               VALUES (?,?,?,?,?,?)""",
            u,
        )

    conn.commit()
    conn.close()


def write_readme_in_package():
    readme = WORK / "TalentFlow-ATS-说明.txt"
    readme.write_text(
        """TalentFlow ATS 活字格工程（完整三端）
第八组：刘沛秋、邱尹鸿、冯逸凡、王天睿

【打开方式】
1. 用活字格设计器打开 TalentFlow-ATS.fgcc
2. 本地运行调试，默认端口 23510，首页 submit（AI 简历初筛）

【重要】
新增页面（public_positions、recruiter_pipeline 等）为已签名模板副本，打开后
请在设计器中将各页 ListView 绑定到对应数据表并保存（保存后活字格会重新签名）。

【页面列表】

C端 · 求职者
  public_positions      公开岗位浏览
  candidate_applications 我的投递进度
  candidate_ai_interview AI初试说明
  submit / result / history  AI简历初筛

B端 · HR/面试官
  recruiter_pipeline     招聘漏斗（可列表内编辑阶段）
  recruiter_interviews   面试安排
  recruiter_evaluations  面评记录
  recruiter_talent_pool  人才库

M端 · 管理层
  management_dashboard   管理驾驶舱
  statistics             数据统计

部门 · 岗位管理
  dept_positions         岗位 CRUD 列表
  dept_import            Excel 导入说明

【数据表】
  position_ / application_ / interview_ / interview_evaluation_
  talent_pool_ / dashboard_stat_ / sys_user_ / review_record_

【与 Spring Boot 版】
  业务模型一致，示例数据对齐 DataInitializer。
  AI 音视频面试、Excel 导入、复杂图表等高级功能请用 start-all.bat 启动完整版。
  测试账号：candidate/candidate123  admin/admin123  executive/exec123
""",
        encoding="utf-8",
    )


def pack_fgcc():
    if OUT_FGCC.exists():
        OUT_FGCC.unlink()
    empty_dirs = []
    with zipfile.ZipFile(TEMPLATE, "r") as ztpl:
        empty_dirs = [n for n in ztpl.namelist() if n.endswith("/")]
    written = set()
    with zipfile.ZipFile(OUT_FGCC, "w", zipfile.ZIP_DEFLATED) as zf:
        for fp in WORK.rglob("*"):
            if fp.is_file():
                arc = fp.relative_to(WORK).as_posix()
                zf.write(fp, arc)
                written.add(arc)
        for d in empty_dirs:
            if d not in written and not any(w.startswith(d) for w in written):
                zf.writestr(d, b"")
    print(f"已生成: {OUT_FGCC} ({OUT_FGCC.stat().st_size // 1024} KB)")


def main():
    if not TEMPLATE.exists():
        raise SystemExit(f"找不到模板: {TEMPLATE}")
    print("1/7 解压模板 Document.fgcc ...")
    extract_template()
    print("2/7 写入全部数据表定义 ...")
    write_table_json_files()
    print("3/7 生成三端业务页面 ...")
    generate_all_pages(WORK / "Pages")
    print("4/7 更新 ApplicationSettings（首页 submit）...")
    patch_application_settings()
    print("5/7 更新 AI 服务端命令 ...")
    patch_agent_command()
    print("6/7 初始化 SQLite 全量示例数据 ...")
    init_sqlite_data()
    print("7/7 打包 TalentFlow-ATS.fgcc ...")
    write_readme_in_package()
    pack_fgcc()
    print("完成！请用活字格设计器打开:", OUT_FGCC)


if __name__ == "__main__":
    main()
