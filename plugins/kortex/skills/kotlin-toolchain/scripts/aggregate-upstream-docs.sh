#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 4 ]]; then
  echo "Usage: $0 <upstream-repo-dir> <ref-name> <ref-sha> <output-file>" >&2
  exit 64
fi

repo_dir=$1
ref_name=$2
ref_sha=$3
output_file=$4
docs_dir="$repo_dir/docs/src"

if [[ ! -d "$docs_dir" ]]; then
  echo "Docs directory not found: $docs_dir" >&2
  exit 66
fi

mkdir -p "$(dirname "$output_file")"

tmp_file=$(mktemp)
trap 'rm -f "$tmp_file"' EXIT

{
  echo "# Kotlin Toolchain upstream docs aggregate ($ref_name)"
  echo
  echo "- Source repository: https://github.com/JetBrains/kotlin-toolchain"
  echo "- Ref: $ref_name"
  echo "- SHA: $ref_sha"
  echo "- Scope: all Markdown files under docs/src at this ref, sorted by path"
  echo
  echo "## Source file list"
  echo
  find "$docs_dir" -type f -name '*.md' | sort | while read -r file; do
    rel=${file#"$repo_dir/"}
    echo "- $rel"
  done
  echo
  echo "## Documents"
  echo
  find "$docs_dir" -type f -name '*.md' | sort | while read -r file; do
    rel=${file#"$repo_dir/"}
    raw_url="https://raw.githubusercontent.com/JetBrains/kotlin-toolchain/$ref_sha/$rel"
    html_path=${rel#docs/src/}
    html_path=${html_path%.md}
    if [[ "$html_path" == "index" ]]; then
      html_url="https://kotlin-toolchain.org/dev/"
    else
      html_url="https://kotlin-toolchain.org/dev/$html_path/"
    fi

    echo "### $rel"
    echo
    echo "- Raw: $raw_url"
    echo "- HTML: $html_url"
    echo
    sed -E 's/\r$//; s/[[:blank:]]+$//' "$file"
    echo
    echo
  done
} > "$tmp_file"

perl -0pi -e 's/\n+\z/\n/' "$tmp_file"

mv "$tmp_file" "$output_file"
