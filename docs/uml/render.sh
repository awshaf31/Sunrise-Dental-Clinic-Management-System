#!/usr/bin/env bash
# Regenerates every UML diagram from its .puml source.
#
# The .puml files are the source of truth and are version controlled; the PNGs
# are build output, committed so the diagrams are viewable on GitHub and can be
# dropped straight into the report.
#
# PlantUML itself is downloaded on first run rather than committed — a 22 MB
# jar has no business in a source repository. The version is pinned so the
# rendering cannot drift.
#
#     ./render.sh
#
# Requires Graphviz for the class, use case and deployment diagrams:
#     brew install graphviz

set -euo pipefail
cd "$(dirname "$0")"

PLANTUML_VERSION="1.2025.4"
PLANTUML_JAR="tools/plantuml.jar"
PLANTUML_URL="https://repo1.maven.org/maven2/net/sourceforge/plantuml/plantuml/${PLANTUML_VERSION}/plantuml-${PLANTUML_VERSION}.jar"

if ! command -v dot >/dev/null 2>&1; then
    echo "Graphviz is not installed. Run: brew install graphviz" >&2
    exit 1
fi

if [ ! -f "$PLANTUML_JAR" ]; then
    echo "Downloading PlantUML ${PLANTUML_VERSION}..."
    mkdir -p tools
    curl -sSL -o "$PLANTUML_JAR" "$PLANTUML_URL"
fi

for source in *.puml; do
    echo "rendering $source"
    java -jar "$PLANTUML_JAR" -tpng "$source"
done

echo
echo "Diagrams written:"
ls -1 ./*.png
