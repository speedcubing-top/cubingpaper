./decompile.sh
./init.sh
./applyPatches.sh
# CUBINGPAPER_VERSION (set by CI) replaces the git-describe Implementation-Version, e.g. 2026.11.05.1
mvn clean package -B --no-transfer-progress ${CUBINGPAPER_VERSION:+-Ddescribe=$CUBINGPAPER_VERSION}
