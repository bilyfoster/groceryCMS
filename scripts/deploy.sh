#!/bin/bash
set -euo pipefail

VERSION=$(cat VERSION)
export APP_VERSION="$VERSION"

SERVER="jenkins@158.220.127.80"
SERVER_DIR="/home/jenkins/docker/brazen-cms"
SSH_PASS="..bb8Copilot"

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

echo "Deployed $VERSION"
