import re
from pathlib import Path

repo = Path('/workspace/es-agents')

# --- Frontend endpoints ---
fe_endpoints = set()
for f in (repo / 'frontend/src/services').glob('*.ts'):
    txt = f.read_text(encoding='utf-8')
    for m in re.finditer(r"request\.(get|post|put|delete)\(\s*([`\"][^`\"]*[`\"])", txt):
        method = m.group(1).upper()
        raw = m.group(2)[1:-1]
        norm = re.sub(r"\$\{[^}]+\}", "{var}", raw)
        fe_endpoints.add((method, norm, f.relative_to(repo).as_posix()))

# --- Backend endpoints ---
be_endpoints = set()
for f in (repo / 'server/src/main/java/com/easystation').glob('**/resource/*.java'):
    lines = f.read_text(encoding='utf-8').splitlines()
    base = ''
    for ln in lines:
        bm = re.search(r'@Path\("([^"]+)"\)', ln)
        if bm:
            base = bm.group(1)
            break

    i = 0
    while i < len(lines):
        line = lines[i]
        mm = re.search(r'@(GET|POST|PUT|DELETE)\b', line)
        if mm:
            method = mm.group(1)
            sub = ''
            j = i + 1
            while j < len(lines) and lines[j].strip().startswith('@'):
                pm = re.search(r'@Path\("([^"]+)"\)', lines[j])
                if pm:
                    sub = pm.group(1)
                j += 1
            full = (base.rstrip('/') + ('/' + sub.lstrip('/') if sub else '')).replace('//','/')
            full = re.sub(r'\{[^}]+\}', '{var}', full)
            be_endpoints.add((method, full, f.relative_to(repo).as_posix()))
            i = j
        else:
            i += 1

# compare frontend to backend
missing = []
for method, path, src in sorted(fe_endpoints):
    if not any(method == bm and path == bp for bm, bp, _ in be_endpoints):
        missing.append((method, path, src))

print('Frontend endpoints:', len(fe_endpoints))
print('Backend endpoints:', len(be_endpoints))
print('Missing mappings:', len(missing))
for m in missing:
    print('MISSING', m[0], m[1], 'from', m[2])
