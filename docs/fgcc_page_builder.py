# -*- coding: utf-8 -*-
"""活字格页面：通过复制已签名模板页生成新页面（不可改 JSON 内容，否则签名校验失败）。"""
import shutil
from pathlib import Path

# 新页面名 -> 复制的已签名模板（submit=表单页, history=列表页, result=详情页）
PAGE_COPIES = {
    "home": "submit",
    "public_positions": "history",
    "candidate_applications": "history",
    "candidate_ai_interview": "result",
    "recruiter_pipeline": "history",
    "recruiter_interviews": "history",
    "recruiter_evaluations": "history",
    "recruiter_talent_pool": "history",
    "management_dashboard": "history",
    "statistics": "history",
    "dept_positions": "history",
    "dept_import": "result",
}


def _read_fgcc_page(path: Path):
    text = path.read_text(encoding="utf-8")
    marker = "}//"
    idx = text.find(marker)
    if idx >= 0:
        return text[: idx + 1], text[idx + 1 :]
    return text, ""


def _write_fgcc_page(path: Path, json_body: str, suffix: str):
    path.write_text(json_body + suffix, encoding="utf-8")


def _nav_button(text, page_name):
    import uuid
    return {
        "CellType": {
            "$type": "Forguncy.ButtonCellType, ServerDesignerCommon",
            "CommandList": [
                {
                    "$type": "Forguncy.Model.NavigateCommand, ServerDesignerCommon",
                    "PageName": page_name,
                    "BreakpointIdentity": str(uuid.uuid4()),
                }
            ],
            "Text": text,
            "TemplateKey": "_RS_Main1",
        }
    }


def copy_signed_pages(pages_dir: Path):
    """从模板复制带签名的 .json/.rd，避免活字格报「文件损坏」。"""
    for dest, src in PAGE_COPIES.items():
        src_json = pages_dir / f"{src}.json"
        src_rd = pages_dir / f"{src}.rd"
        if not src_json.exists():
            continue
        shutil.copy2(src_json, pages_dir / f"{dest}.json")
        if src_rd.exists():
            shutil.copy2(src_rd, pages_dir / f"{dest}.rd")


def patch_existing_page_nav(pages_dir: Path, page_name: str, extra_buttons):
    """给已有签名页面追加导航按钮（保留原签名后缀）。"""
    path = pages_dir / f"{page_name}.json"
    if not path.exists():
        return
    import json
    json_part, suffix = _read_fgcc_page(path)
    if not suffix:
        return  # 无签名则不修改，避免破坏文件
    data = json.loads(json_part)
    attach = data.setdefault("AttachInfos", {})
    values = data.setdefault("Values", {})
    col = 16
    for text, target in extra_buttons:
        key = f"2,{col}"
        if key not in attach:
            values[key] = text
            attach[key] = _nav_button(text, target)
        col += 2
    _write_fgcc_page(path, json.dumps(data, ensure_ascii=False, indent=2), suffix)


def generate_all_pages(pages_dir: Path):
    """复制已签名模板页；不修改 submit/result/history 内容（改内容会破坏签名）。"""
    copy_signed_pages(pages_dir)
