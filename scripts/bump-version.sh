#!/bin/bash
set -euo pipefail

BUMP_TYPE="${1:-patch}"
VERSION_FILE="VERSION"

if [[ ! -f "$VERSION_FILE" ]]; then
  echo "VERSION file not found" >&2
  exit 1
fi

CURRENT=$(cat "$VERSION_FILE")
IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT"

case "$BUMP_TYPE" in
  major)
    MAJOR=$((MAJOR + 1))
    MINOR=0
    PATCH=0
    ;;
  minor)
    MINOR=$((MINOR + 1))
    PATCH=0
    ;;
  patch)
    PATCH=$((PATCH + 1))
    ;;
  *)
    echo "Usage: $0 [major|minor|patch]" >&2
    exit 1
    ;;
esac

NEW="$MAJOR.$MINOR.$PATCH"
echo "$NEW" > "$VERSION_FILE"

# Sync to frontend package.json
sed -i.bak "s/\"version\": \"[^\"]*\"/\"version\": \"$NEW\"/" frontend/package.json
rm -f frontend/package.json.bak

# Sync to frontend package-lock.json root/package metadata
node -e "const fs=require('fs'); const p='frontend/package-lock.json'; const data=JSON.parse(fs.readFileSync(p,'utf8')); data.version='$NEW'; if (data.packages && data.packages['']) data.packages[''].version='$NEW'; fs.writeFileSync(p, JSON.stringify(data, null, 2) + '\n');"

# Sync to backend build.gradle
sed -i.bak "s/version = '[^']*'/version = '$NEW'/" backend/build.gradle
rm -f backend/build.gradle.bak

echo "Bumped version: $CURRENT -> $NEW"
