./decompile.sh
./init.sh
./applyPatches.sh
# CUBINGPAPER_VERSION (set by CI) replaces the git-describe Implementation-Version, e.g. 2026.11.05.1
# MAVEN_GOAL=deploy (set by CI when publishing) packages and uploads in one build
mvn clean ${MAVEN_GOAL:-package} -B --no-transfer-progress ${CUBINGPAPER_VERSION:+-Ddescribe=$CUBINGPAPER_VERSION}
