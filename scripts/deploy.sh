#!/bin/bash
set -euo pipefail

VERSION=$(cat VERSION)
export APP_VERSION="$VERSION"

SERVER="jenkins@rue"
SERVER_DIR="/home/jenkins/docker/pickles-cms"
SSH_PASS="..bb8Copilot"

if [[ "$(git rev-parse --abbrev-ref HEAD)" != "main" ]]; then
  echo "Deploys must be run from main." >&2
  exit 1
fi

if [[ -n "$(git status --porcelain)" ]]; then
  echo "Working tree is dirty. Commit and push all changes before deploying." >&2
  git status --short
  exit 1
fi

echo "Pushing version $VERSION to origin..."
git push origin main

echo "Syncing source to server..."
RSYNC_RSH="sshpass -p $SSH_PASS ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null" \
  rsync -avz --delete \
    --exclude=.git \
    --exclude=frontend/node_modules \
    --exclude=frontend/.next \
    --exclude=backend/.gradle \
    --exclude=backend/build \
    --exclude=backend/media \
    --exclude=.env \
    --exclude=.env.local \
    --exclude='*.local' \
    . "$SERVER:$SERVER_DIR"

echo "Building and deploying containers..."
sshpass -p "$SSH_PASS" ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null "$SERVER" \
  "export APP_VERSION=$VERSION && cd $SERVER_DIR && docker compose -f docker-compose.prod.yml up -d --build"

echo "Deployed $VERSION to pickles.1lpro.com"
