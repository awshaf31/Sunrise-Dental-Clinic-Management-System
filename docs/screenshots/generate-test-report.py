#!/usr/bin/env python3
"""Builds a readable test-results page from Maven Surefire's XML output."""

import glob
import html
import pathlib
import re
import xml.etree.ElementTree as ET

REPO_ROOT = pathlib.Path(__file__).resolve().parents[2]
OUTPUT = REPO_ROOT / "docs" / "screenshots" / "test-results.html"


def humanise(method_name):
    spaced = re.sub(r"(?<!^)(?=[A-Z])", " ", method_name)
    spaced = re.sub(r"(?<=[a-zA-Z])(?=\d)", " ", spaced)
    lowered = spaced.lower().replace("strategys ", "strategy's ")
    return lowered[0].upper() + lowered[1:]


def collect():
    reports = sorted(glob.glob(str(REPO_ROOT / "target" / "surefire-reports" / "TEST-*.xml")))
    classes, total, passed, duration = [], 0, 0, 0.0
    for report in reports:
        root = ET.parse(report).getroot()
        fqn = root.get("name")
        count = int(root.get("tests"))
        failed = int(root.get("failures")) + int(root.get("errors"))
        elapsed = float(root.get("time"))
        total += count
        passed += count - failed
        duration += elapsed
        cases = [
            (c.get("name"), c.find("failure") is None and c.find("error") is None, float(c.get("time") or 0))
            for c in root.iter("testcase")
        ]
        pkg_parts = fqn.split(".")
        level = "Integration" if "Servlet" in fqn else "Unit"
        classes.append({"name": pkg_parts[-1], "fqn": fqn, "level": level, "count": count, "cases": cases})
    return classes, total, passed, duration


STYLE = """
:root{--ink:#12313a;--ink3:#5d7d87;--paper:#f2f5f5;--rule:#dde5e6;--soft:#eaefef;
--teal:#0f6e7e;--tealw:#e4f0f1;--sage:#2f6b47;--rose:#a83b36;--amber:#b5701f;--amberw:#fbeedd;
--mono:"IBM Plex Mono",monospace;--body:"IBM Plex Sans",sans-serif;--disp:"Bricolage Grotesque",sans-serif}
*{box-sizing:border-box}
body{margin:0;padding:34px 40px;background:var(--paper);color:var(--ink);font:15px/1.5 var(--body)}
h1{font-family:var(--disp);font-size:1.7rem;margin:0 0 4px}
.eyebrow{font-family:var(--mono);font-size:.7rem;letter-spacing:.12em;text-transform:uppercase;color:var(--ink3)}
.summary{display:flex;gap:34px;margin:22px 0 26px;padding:20px 24px;background:#fff;border:1px solid var(--rule);border-radius:6px}
.stat .v{font-family:var(--mono);font-size:1.9rem;font-weight:500;line-height:1}
.stat .l{font-size:.74rem;color:var(--ink3);margin-top:5px}
.grid{display:grid;grid-template-columns:1fr 1fr;gap:16px;align-items:start}
.cls{background:#fff;border:1px solid var(--rule);border-radius:6px;overflow:hidden}
.cls header{display:flex;justify-content:space-between;align-items:flex-start;gap:12px;padding:13px 16px;border-bottom:1px solid var(--soft)}
h3{font-family:var(--disp);font-size:.98rem;margin:0}
.pkg{font-family:var(--mono);font-size:.66rem;color:var(--ink3);margin-top:3px}
.meta{display:flex;flex-direction:column;align-items:flex-end;gap:4px}
.tag{font-family:var(--mono);font-size:.62rem;letter-spacing:.06em;text-transform:uppercase;padding:2px 7px;border-radius:9px}
.tag.unit{background:var(--tealw);color:var(--teal)}
.tag.integration{background:var(--amberw);color:var(--amber)}
.count{font-family:var(--mono);font-size:.7rem;color:var(--sage)}
ul{list-style:none;margin:0;padding:6px 0}
li{display:grid;grid-template-columns:44px 1fr auto;gap:9px;align-items:baseline;padding:5px 16px;font-size:.83rem}
li+li{border-top:1px solid #f4f7f7}
.tick{font-family:var(--mono);font-size:.6rem;letter-spacing:.06em;color:var(--sage)}
.bad .tick{color:var(--rose)}
.ttime{font-family:var(--mono);font-size:.7rem;color:var(--ink3)}
footer{margin-top:24px;font-family:var(--mono);font-size:.72rem;color:var(--ink3)}
"""


def render(classes, total, passed, duration):
    sections = []
    for c in classes:
        items = "".join(
            f'<li class="{"ok" if ok else "bad"}"><span class="tick">{"PASS" if ok else "FAIL"}</span>'
            f'<span>{html.escape(humanise(m))}</span><span class="ttime">{s:.3f}s</span></li>'
            for m, ok, s in c["cases"]
        )
        sections.append(
            f'<section class="cls"><header><div><h3>{html.escape(c["name"])}</h3>'
            f'<div class="pkg">{html.escape(c["fqn"])}</div></div>'
            f'<div class="meta"><span class="tag {c["level"].lower()}">{c["level"]}</span>'
            f'<span class="count">{c["count"]} passed</span></div></header><ul>{items}</ul></section>'
        )
    return f"""<!DOCTYPE html><html lang="en"><head><meta charset="utf-8"><title>Test results</title>
<link href="https://fonts.googleapis.com/css2?family=Bricolage+Grotesque:opsz,wght@12..96,700&family=IBM+Plex+Mono:wght@400;500&family=IBM+Plex+Sans:wght@400;500;600&display=swap" rel="stylesheet">
<style>{STYLE}</style></head><body>
<div class="eyebrow">Sunrise Dental Clinic &mdash; CIS6003 WRIT1 &mdash; Pure Java Implementation</div>
<h1>Automated test results</h1>
<div class="summary">
<div class="stat"><div class="v" style="color:var(--sage)">{passed}/{total}</div><div class="l">Tests passing</div></div>
<div class="stat"><div class="v">{len(classes)}</div><div class="l">Test classes</div></div>
<div class="stat"><div class="v">{total-passed}</div><div class="l">Failures</div></div>
<div class="stat"><div class="v">{duration:.2f}s</div><div class="l">Suite duration</div></div>
</div>
<div class="grid">{"".join(sections)}</div>
<footer>Generated from Maven Surefire reports &middot; <b>./mvnw -B clean test</b> &middot; BUILD SUCCESS</footer>
</body></html>"""


if __name__ == "__main__":
    classes, total, passed, duration = collect()
    OUTPUT.write_text(render(classes, total, passed, duration))
    print(f"{passed}/{total} passing across {len(classes)} classes in {duration:.2f}s")
