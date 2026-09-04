#!/usr/bin/env python3
"""Renders a Java source excerpt as a styled 'editor window' HTML page.

Used to produce the code-extract figures in the WRIT1 report (Appendix B).
Output is HTML; render it to PNG with headless Chrome, e.g.:

    google-chrome --headless --disable-gpu --hide-scrollbars \\
        --force-device-scale-factor=2 --window-size=1100,1500 \\
        --screenshot=out.png file://$(pwd)/out.html

Usage:
    python3 render-code-shot.py '{"path":"...","title":"...","start":1,"end":40,"out":"shot.html"}'
"""
import re, sys, html, json

KEYWORDS = {"public","private","protected","final","static","class","interface","extends","implements",
    "new","return","void","if","else","for","while","try","catch","throw","throws","import","package",
    "this","super","record","enum","switch","case","default","break","continue","synchronized","abstract",
    "null","true","false","instanceof"}
TYPES = {"String","int","long","boolean","double","float","char","byte","short","Long","Integer","Boolean",
    "Double","Object","List","Map","Set","Optional","BigDecimal","LocalDate","LocalTime","LocalDateTime",
    "Connection","PreparedStatement","ResultSet","SQLException","SQLIntegrityConstraintViolationException",
    "Statement","Clock","ZoneId","RuntimeException","Date","Time"}

TOKEN_RE = re.compile(r'//.*$|"(?:[^"\\]|\\.)*"|\b\w+\b|@\w+')

def highlight(line):
    out = []
    last = 0
    for m in TOKEN_RE.finditer(line):
        out.append(html.escape(line[last:m.start()]))
        tok = m.group(0)
        esc = html.escape(tok)
        if tok.startswith('//'):
            out.append(f'<span class="cmt">{esc}</span>')
        elif tok.startswith('"'):
            out.append(f'<span class="str">{esc}</span>')
        elif tok.startswith('@'):
            out.append(f'<span class="anno">{esc}</span>')
        elif tok in KEYWORDS:
            out.append(f'<span class="kw">{esc}</span>')
        elif tok in TYPES:
            out.append(f'<span class="type">{esc}</span>')
        else:
            out.append(esc)
        last = m.end()
    out.append(html.escape(line[last:]))
    return "".join(out)

def build(path, title, start=1, end=None, out=None):
    with open(path) as f:
        all_lines = f.readlines()
    end = end or len(all_lines)
    lines = all_lines[start-1:end]
    rows = []
    for i, l in enumerate(lines, start=start):
        rows.append(f'<div class="line"><span class="ln">{i}</span><span class="code">{highlight(l.rstrip(chr(10)))}</span></div>')
    body = "\n".join(rows)
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
.body{{background:#111b24;padding:14px 0;width:max-content}}
.line{{width:max-content;display:flex;padding:1.5px 22px;white-space:pre}}
.ln{{color:#43586a;width:34px;text-align:right;margin-right:18px;user-select:none;font-size:13px}}
.code{{color:#d4dee6;font-size:13.5px;line-height:1.55}}
.kw{{color:#c586c0}} .type{{color:#4ec9b0}} .str{{color:#ce9178}} .cmt{{color:#5b7385;font-style:italic}} .anno{{color:#dcdcaa}}
</style></head><body>
<div class="win"><div class="bar"><span class="dot r"></span><span class="dot y"></span><span class="dot g"></span><span class="fname">{html.escape(title)}</span></div>
<div class="body">{body}</div></div>
</body></html>"""
    with open(out, "w") as f:
        f.write(doc)
    print("wrote", out)

if __name__ == "__main__":
    spec = json.loads(sys.argv[1])
    build(**spec)
