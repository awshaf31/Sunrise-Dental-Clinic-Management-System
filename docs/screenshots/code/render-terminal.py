#!/usr/bin/env python3
"""Renders captured terminal/console output as a styled terminal-window HTML page.

Used for the ./mvnw test evidence figure in the WRIT1 report (Section C.5).
Render the same way as render-code-shot.py (headless Chrome screenshot).

Usage:
    ./mvnw -B test 2>&1 | grep -v "Mockito\\|WARNING\\|agent" > /tmp/mvn-test-output.txt
    python3 render-terminal.py '{"src":"/tmp/mvn-test-output.txt","cmd":"./mvnw test","out":"mvn-test.html"}'
"""
import html, sys, json

def colorize(line):
    e = html.escape(line)
    if "BUILD SUCCESS" in line:
        return f'<span class="ok">{e}</span>'
    if line.strip().startswith("[INFO] Running"):
        return f'<span class="running">{e}</span>'
    if "Tests run:" in line and "Failures: 0, Errors: 0" in line:
        return f'<span class="pass">{e}</span>'
    if "T E S T S" in line or line.strip().startswith("[INFO] Results"):
        return f'<span class="hd">{e}</span>'
    if line.strip().startswith("[INFO] ---"):
        return f'<span class="rule">{e}</span>'
    return e

def build(cmd, lines, out, title="Terminal"):
    body = "\n".join(f'<div class="l">{colorize(l)}</div>' for l in lines)
    doc = f"""<!doctype html><html><head><meta charset="utf-8">
<style>
@import url('https://fonts.googleapis.com/css2?family=IBM+Plex+Mono:wght@400;500&family=IBM+Plex+Sans:wght@500;600&display=swap');
*{{box-sizing:border-box}}
body{{margin:0;background:#0f1720;font-family:'IBM Plex Mono',monospace}}
.win{{border-radius:10px;overflow:hidden;box-shadow:0 20px 60px rgba(0,0,0,.35);width:fit-content}}
.bar{{background:#1c2732;padding:10px 16px;display:flex;align-items:center;gap:8px;font-family:'IBM Plex Sans',sans-serif}}
.dot{{width:11px;height:11px;border-radius:50%}}
.r{{background:#e5645a}} .y{{background:#e5b95a}} .g{{background:#5ac27a}}
.fname{{color:#8fa3b3;font-size:12.5px;margin-left:10px}}
.body{{background:#0b1319;padding:18px 24px;width:max-content}}
.prompt{{color:#5ac27a;font-size:13.5px;margin-bottom:10px}}
.l{{color:#c7d3db;font-size:12.5px;line-height:1.62;white-space:pre}}
.ok{{color:#5ac27a;font-weight:600}} .pass{{color:#5ac27a}} .running{{color:#7fb0c7}}
.hd{{color:#e8ecee;font-weight:600}} .rule{{color:#33454f}}
</style></head><body>
<div class="win"><div class="bar"><span class="dot r"></span><span class="dot y"></span><span class="dot g"></span><span class="fname">{html.escape(title)}</span></div>
<div class="body"><div class="prompt">$ {html.escape(cmd)}</div>{body}</div></div>
</body></html>"""
    with open(out, "w") as f:
        f.write(doc)
    print("wrote", out)

if __name__ == "__main__":
    spec = json.loads(sys.argv[1])
    with open(spec["src"]) as f:
        lines = [l.rstrip("\n") for l in f]
    build(spec["cmd"], lines, spec["out"], spec.get("title", "Terminal"))
