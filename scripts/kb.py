#!/usr/bin/env python3
"""KS - TV Launcher code knowledge graph (Graphify-style, zero model tokens).

Builds a dependency graph of the Kotlin sources and keeps the OKF bundle in
docs/kb/ in sync with it, so Claude can ask the graph instead of grepping and
reading files.

    python3 scripts/kb.py build          # rebuild docs/kb/graph.json, index.md, module blocks
    python3 scripts/kb.py query <term>   # symbol, file or module: where it is, who uses it
    python3 scripts/kb.py check          # OKF + freshness + coverage; non-zero exit on problems

Standard library only (Python 3.8+). Run from anywhere inside the repo.
"""
import hashlib
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
APP_DIR = os.path.join("tv", "google-tv")
SRC = os.path.join(ROOT, APP_DIR, "src")
BASE_PKG = "com.ks.tvlauncher"
KB = os.path.join(ROOT, "docs", "kb")
MODULES = os.path.join(KB, "modules")
GRAPH = os.path.join(KB, "graph.json")
BUNDLES = [os.path.join(ROOT, "specs"), KB]
GEN_START = "<!-- kb:generated:start -->"
GEN_END = "<!-- kb:generated:end -->"
PLACEHOLDER = "_Not written yet"
QUERY_CAP = 40

DECL = re.compile(
    r"^(?P<mods>(?:(?:public|internal|private|protected|data|sealed|enum|abstract|open|inline|value|"
    r"annotation|const|suspend|operator|infix|tailrec|external|lateinit|override|expect|actual|fun)\s+)*)"
    r"(?P<kind>class|interface|object|fun|val|var|typealias)\s+"
    r"(?:<[^>]*>\s*)?(?:[\w.<>?, ]+\.)?(?P<name>[A-Za-z_]\w*)"
)
IMPORT = re.compile(r"^import\s+([\w.]+)(\.\*)?(?:\s+as\s+(\w+))?")
PACKAGE = re.compile(r"^package\s+([\w.]+)")
LINK = re.compile(r"\]\(([^)\s]+)\)")


# ---------------------------------------------------------------- parsing

def strip_noise(text):
    """Drop comments and string contents so names in them don't count as uses."""
    text = re.sub(r"/\*.*?\*/", " ", text, flags=re.S)
    text = re.sub(r"//[^\n]*", " ", text)
    text = re.sub(r'"""(?:.|\n)*?"""', '""', text)
    text = re.sub(r'"(?:\\.|[^"\\\n])*"', '""', text)
    return text


def module_of(pkg):
    if pkg == BASE_PKG:
        return "root"
    if pkg.startswith(BASE_PKG + "."):
        return pkg[len(BASE_PKG) + 1:]
    return pkg


def slug(module):
    return module.replace(".", "-")


def kotlin_files():
    out = []
    for dirpath, _, names in os.walk(SRC):
        if os.sep + "build" + os.sep in dirpath + os.sep:
            continue
        for n in names:
            if n.endswith(".kt"):
                out.append(os.path.join(dirpath, n))
    return sorted(out)


def parse(path):
    raw = open(path, encoding="utf-8").read()
    rel = os.path.relpath(path, ROOT)
    pkg, imports, symbols = "", [], []
    for i, line in enumerate(raw.splitlines(), 1):
        m = PACKAGE.match(line)
        if m:
            pkg = m.group(1)
            continue
        m = IMPORT.match(line)
        if m:
            imports.append((m.group(1), bool(m.group(2))))
            continue
        if line[:1] in (" ", "\t", "}", ")", "@", "*", "/") or not line.strip():
            continue  # top-level declarations only
        m = DECL.match(line)
        if m and m.group("name") not in ("get", "set"):
            symbols.append({
                "name": m.group("name"),
                "kind": m.group("kind"),
                "line": i,
                "private": "private" in m.group("mods").split(),
            })
    src_prefix = os.path.join(APP_DIR, "src") + os.sep
    source_set = rel[len(src_prefix):].split(os.sep)[0] if rel.startswith(src_prefix) else "main"
    return {
        "path": rel,
        "package": pkg,
        "module": module_of(pkg),
        "source_set": source_set,
        "symbols": symbols,
        "_imports": imports,
        "_body": strip_noise(raw),
        "_hash": hashlib.sha1(raw.encode("utf-8")).hexdigest(),
    }


def build_graph():
    files = [parse(p) for p in kotlin_files()]
    by_fqn, by_pkg = {}, {}
    for f in files:
        for s in f["symbols"]:
            if s["private"]:
                continue
            by_fqn.setdefault(f["package"] + "." + s["name"], f["path"])
            by_pkg.setdefault(f["package"], {}).setdefault(s["name"], f["path"])

    for f in files:
        deps = {}
        visible = dict(by_pkg.get(f["package"], {}))
        for fqn, star in f["_imports"]:
            if star:
                visible.update(by_pkg.get(fqn, {}))
            elif fqn in by_fqn:
                name = fqn.rsplit(".", 1)[1]
                visible[name] = by_fqn[fqn]
        for name, target in visible.items():
            if target == f["path"]:
                continue
            if re.search(r"(?<![\w.])" + re.escape(name) + r"\b", f["_body"]) or \
                    re.search(r"\." + re.escape(name) + r"\b", f["_body"]):
                deps.setdefault(target, set()).add(name)
        f["deps"] = {k: sorted(v) for k, v in sorted(deps.items())}

    source_hash = hashlib.sha1("".join(f["path"] + f["_hash"] for f in files).encode()).hexdigest()
    graph = {
        "version": 1,
        "generated_by": "scripts/kb.py build",
        "source_hash": source_hash,
        "files": {
            f["path"]: {k: f[k] for k in ("package", "module", "source_set", "symbols", "deps")}
            for f in files
        },
    }
    return graph


# ---------------------------------------------------------------- derived views

def users_of(graph):
    users = {}
    for path, f in graph["files"].items():
        for target, names in f["deps"].items():
            users.setdefault(target, {})[path] = names
    return users


def module_view(graph):
    mods = {}
    users = users_of(graph)
    for path, f in graph["files"].items():
        m = mods.setdefault(f["module"], {"package": f["package"], "files": [], "tests": [],
                                           "deps": {}, "users": {}})
        (m["tests"] if f["source_set"] != "main" else m["files"]).append(path)
        for target, names in f["deps"].items():
            tm = graph["files"][target]["module"]
            if tm != f["module"] and f["source_set"] == "main":
                m["deps"].setdefault(tm, set()).update(names)
        for user, names in users.get(path, {}).items():
            um = graph["files"][user]["module"]
            if um != f["module"] and graph["files"][user]["source_set"] == "main":
                m["users"].setdefault(um, set()).update(names)
    return mods


def short(path):
    marker = os.path.join("java", *BASE_PKG.split(".")) + os.sep
    return path.split(marker, 1)[1] if marker in path else path


# ---------------------------------------------------------------- writing docs/kb

def read_front(path):
    text = open(path, encoding="utf-8").read()
    m = re.match(r"^---\n(.*?)\n---\n", text, re.S)
    return text, (m.group(1) if m else None)


def front_list(front, key):
    m = re.search(r"^" + key + r":\s*\[(.*?)\]\s*$", front or "", re.M)
    if not m:
        return []
    return [x.strip().strip("'\"") for x in m.group(1).split(",") if x.strip()]


def fmt_names(names, limit=6):
    names = sorted(names)
    more = len(names) - limit
    return ", ".join("`%s`" % n for n in names[:limit]) + (" +%d" % more if more > 0 else "")


def generated_block(module, info, graph, spec_links):
    lines = [GEN_START, "", "## Files (generated)"]
    for p in sorted(info["files"]):
        syms = [s["name"] for s in graph["files"][p]["symbols"] if not s["private"]]
        lines.append("- `%s` — %s" % (short(p), fmt_names(syms, 8) if syms else "_no public symbols_"))
    if info["tests"]:
        lines.append("")
        lines.append("Tests: " + ", ".join("`%s`" % short(p) for p in sorted(info["tests"])))
    lines += ["", "## Depends on (generated)"]
    lines += ["- [%s](%s.md) via %s" % (d, slug(d), fmt_names(n)) for d, n in sorted(info["deps"].items())] \
        or ["- nothing inside the app"]
    lines += ["", "## Used by (generated)"]
    lines += ["- [%s](%s.md) via %s" % (u, slug(u), fmt_names(n)) for u, n in sorted(info["users"].items())] \
        or ["- nothing inside the app (entry point or not yet wired)"]
    if spec_links:
        lines += ["", "## Spec", "- " + ", ".join(
            "[%s](%s)" % (os.path.splitext(os.path.basename(s))[0], s) for s in spec_links)]
    lines += ["", GEN_END]
    return "\n".join(lines)


def write_module(module, info, graph):
    os.makedirs(MODULES, exist_ok=True)
    path = os.path.join(MODULES, slug(module) + ".md")
    if os.path.exists(path):
        text, front = read_front(path)
    else:
        front = 'type: Module\ntitle: %s\npackage: %s\nspec: []' % (module, info["package"])
        text = "---\n%s\n---\n\n# %s\n\n## Purpose\n%s: describe this module in 1-3 lines._\n\n" \
               "## Rules\n%s: invariants, gotchas, what not to break._\n\n%s\n%s\n" % (
                   front, module, PLACEHOLDER, PLACEHOLDER, GEN_START, GEN_END)
    block = generated_block(module, info, graph, front_list(front, "spec"))
    if GEN_START in text and GEN_END in text:
        head, rest = text.split(GEN_START, 1)
        tail = rest.split(GEN_END, 1)[1]
        text = head + block + tail
    else:
        text = text.rstrip("\n") + "\n\n" + block + "\n"
    open(path, "w", encoding="utf-8").write(text)


def write_index(graph, mods):
    main = {p: f for p, f in graph["files"].items() if f["source_set"] == "main"}
    users = {t: {u: n for u, n in us.items() if u in main} for t, us in users_of(graph).items()}
    hubs = sorted(main, key=lambda p: (-len(users.get(p, {})), p))[:8]
    entries = [p for p in main if not users.get(p)]
    n_sym = sum(len(f["symbols"]) for f in main.values())
    out = ['---', 'okf_version: "0.1"', '---', '',
           '# KS - TV Launcher code knowledge', '',
           'Generated by `python3 scripts/kb.py build`. Do not edit this file; write prose in the module files.', '',
           '**Before Grep/Glob/Read:** run `python3 scripts/kb.py query <symbol|file|module>`. '
           'It answers where something is and who uses it in a few lines.', '',
           '## Modules (%d source files, %d top-level symbols)' % (len(main), n_sym), '',
           '| Module | Files | Depends on | Used by |', '|---|---|---|---|']
    for m in sorted(mods):
        info = mods[m]
        out.append("| [%s](modules/%s.md) | %d | %s | %s |" % (
            m, slug(m), len(info["files"]),
            ", ".join(sorted(info["deps"])) or "-", ", ".join(sorted(info["users"])) or "-"))
    out += ['', '## Hubs (most depended-on files)', '']
    out += ["- `%s` — used by %d files" % (short(p), len(users.get(p, {}))) for p in hubs if users.get(p)]
    out += ['', '## Entry points (no in-app callers)', '']
    out += ["- `%s`" % short(p) for p in sorted(entries)] or ["- none"]
    out += ['', 'Machine graph: [graph.json](graph.json). Stage notes: [log](log.md).', '']
    open(os.path.join(KB, "index.md"), "w", encoding="utf-8").write("\n".join(out))


def cmd_build():
    graph = build_graph()
    os.makedirs(KB, exist_ok=True)
    with open(GRAPH, "w", encoding="utf-8") as fh:
        json.dump(graph, fh, indent=1, sort_keys=True)
        fh.write("\n")
    mods = module_view(graph)
    for m, info in mods.items():
        write_module(m, info, graph)
    write_index(graph, mods)
    log = os.path.join(KB, "log.md")
    if not os.path.exists(log):
        open(log, "w", encoding="utf-8").write("# Code knowledge log\n\nOne entry per stage: what changed and why.\n")
    print("kb: %d files, %d modules -> docs/kb/" % (len(graph["files"]), len(mods)))
    return 0


# ---------------------------------------------------------------- query

def load_graph():
    if not os.path.exists(GRAPH):
        sys.exit("kb: no graph yet; run `python3 scripts/kb.py build`")
    return json.load(open(GRAPH, encoding="utf-8"))


def cmd_query(term, show_all=False):
    graph = load_graph()
    users = users_of(graph)
    out = []
    t = term.lower()
    hits = [(p, s) for p, f in graph["files"].items() for s in f["symbols"] if s["name"] == term]
    if not hits:
        hits = [(p, s) for p, f in graph["files"].items() for s in f["symbols"] if t in s["name"].lower()]
    for p, s in hits[:10]:
        f = graph["files"][p]
        callers = [u for u, n in users.get(p, {}).items() if s["name"] in n]
        out.append("%s  %s%s  %s:%d  [%s]" % (s["name"], s["kind"], " (private)" if s["private"] else "",
                                              short(p), s["line"], f["module"]))
        out.append("  used by (%d): %s" % (len(callers), ", ".join(short(c) for c in sorted(callers)) or "-"))
    files = [p for p in graph["files"] if t in os.path.basename(p).lower()]
    for p in files[:5]:
        f = graph["files"][p]
        out.append("file %s  [%s, %s]" % (short(p), f["module"], f["source_set"]))
        out.append("  declares: " + (", ".join(s["name"] for s in f["symbols"]) or "-"))
        out.append("  uses: " + ("; ".join("%s (%s)" % (short(d), ", ".join(n)) for d, n in f["deps"].items()) or "-"))
        out.append("  used by: " + (", ".join(short(u) for u in sorted(users.get(p, {}))) or "-"))
    mods = module_view(graph)
    for m in [m for m in mods if t == m.lower() or t == slug(m).lower()]:
        info = mods[m]
        out.append("module %s  -> docs/kb/modules/%s.md" % (m, slug(m)))
        out.append("  files: " + ", ".join(short(p) for p in sorted(info["files"])))
        out.append("  depends on: " + (", ".join(sorted(info["deps"])) or "-"))
        out.append("  used by: " + (", ".join(sorted(info["users"])) or "-"))
    if not out:
        out = ["kb: nothing matches '%s' (try a shorter name, a file name, or a module like ui.home)" % term]
    if len(out) > QUERY_CAP and not show_all:
        out = out[:QUERY_CAP] + ["... %d more lines (add --all)" % (len(out) - QUERY_CAP)]
    print("\n".join(out))
    return 0


# ---------------------------------------------------------------- check

def okf_errors(bundle):
    errs = []
    if not os.path.isdir(bundle):
        return ["%s: missing" % os.path.relpath(bundle, ROOT)]
    for dirpath, _, names in os.walk(bundle):
        for n in names:
            if not n.endswith(".md"):
                continue
            p = os.path.join(dirpath, n)
            rel = os.path.relpath(p, ROOT)
            text, front = read_front(p)
            is_root_index = n == "index.md" and dirpath == bundle
            if n == "index.md":
                if front is not None and not (is_root_index and set(
                        l.split(":")[0].strip() for l in front.splitlines() if l.strip()) <= {"okf_version"}):
                    errs.append("%s: index.md may only carry okf_version (bundle root only)" % rel)
            elif n == "log.md":
                if front is not None:
                    errs.append("%s: log.md must not have frontmatter" % rel)
            elif front is None:
                errs.append("%s: missing YAML frontmatter" % rel)
            elif not re.search(r"^type:\s*\S", front, re.M):
                errs.append("%s: frontmatter needs a non-empty `type`" % rel)
            for link in LINK.findall(text):
                if re.match(r"^(https?:|mailto:|#)", link):
                    continue
                target = os.path.normpath(os.path.join(dirpath, link.split("#")[0]))
                if not os.path.exists(target):
                    errs.append("%s: broken link %s" % (rel, link))
    return errs


def cmd_check():
    errs = []
    for b in BUNDLES:
        errs += okf_errors(b)
    if os.path.exists(GRAPH):
        old = json.load(open(GRAPH, encoding="utf-8"))
        if old.get("source_hash") != build_graph()["source_hash"]:
            errs.append("docs/kb/graph.json is stale: run `python3 scripts/kb.py build`")
        mods = module_view(old)
        for m in mods:
            p = os.path.join(MODULES, slug(m) + ".md")
            if not os.path.exists(p):
                errs.append("docs/kb/modules/%s.md missing: run build" % slug(m))
            elif PLACEHOLDER in open(p, encoding="utf-8").read():
                errs.append("docs/kb/modules/%s.md: write the Purpose and Rules sections" % slug(m))
        if os.path.isdir(MODULES):
            known = {slug(m) + ".md" for m in mods}
            for n in sorted(os.listdir(MODULES)):
                if n.endswith(".md") and n not in known:
                    errs.append("docs/kb/modules/%s: no code left in this module; delete it" % n)
    else:
        errs.append("docs/kb/graph.json missing: run `python3 scripts/kb.py build`")
    if errs:
        print("kb check: %d problem(s)" % len(errs))
        print("\n".join("- " + e for e in errs))
        return 1
    print("kb check: OK (OKF bundles valid, graph fresh, all modules documented)")
    return 0


def main(argv):
    if len(argv) < 2 or argv[1] in ("-h", "--help"):
        print(__doc__)
        return 0
    cmd = argv[1]
    if cmd == "build":
        return cmd_build()
    if cmd == "query" and len(argv) >= 3:
        return cmd_query(argv[2], "--all" in argv)
    if cmd == "check":
        return cmd_check()
    print(__doc__)
    return 2


if __name__ == "__main__":
    sys.exit(main(sys.argv))
