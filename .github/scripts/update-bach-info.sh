#!/bin/sh

# env vars:
# VERSION: x.y.z or early-access
# TAG: vx.y.z or early-access
# JAR_SIZE
# JAR_CSUM
# DOC_SIZE

REPOSITORY_OWNER="kordamp"
REPOSITORY_NAME="pomchecker"
TOOL_NAME="pomchecker"
TOOL_DESC="PomChecker"
TOOL_FILENAME="pomchecker-toolprovider"

TARGET_FILE=".bach/external-tools/${TOOL_NAME}@${TAG}.tool-directory.properties"
echo "@description ${TOOL_DESC} ${TAG} (${VERSION})" > $TARGET_FILE
echo " " >> $TARGET_FILE
echo "${TOOL_FILENAME}-${VERSION}.jar=\\" >> $TARGET_FILE
echo "  https://github.com/${REPOSITORY_OWNER}/${REPOSITORY_NAME}/releases/download/${TAG}/${TOOL_FILENAME}-${VERSION}.jar\\" >> $TARGET_FILE
echo "  #SIZE=${JAR_SIZE}&SHA-256=${JAR_CSUM}" >> $TARGET_FILE
echo "README.adoc=\\" >> $TARGET_FILE
echo "  https://github.com/${REPOSITORY_OWNER}/${REPOSITORY_NAME}/raw/${TAG}/README.adoc\\" >> $TARGET_FILE
echo "  #SIZE=${DOC_SIZE}" >> $TARGET_FILE
echo "" >> $TARGET_FILE

git add $TARGET_FILE
git config --global user.email "41898282+github-actions[bot]@users.noreply.github.com"
git config --global user.name "GitHub Action"
git commit -a -m "Releasing ${TAG} (${VERSION})"
git push origin main
