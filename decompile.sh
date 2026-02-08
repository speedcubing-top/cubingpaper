#!/bin/bash

basedir=`pwd`

# clone BuildData
echo "[Build] Cloning BuildData"
if [ ! -d "BuildData" ]; then

  git clone https://hub.spigotmc.org/stash/scm/spigot/BuildData.git BuildData
  cd $basedir/BuildData
  git reset --hard 838b40587fa7a68a130b75252959bc8a3481d94f
  cd $basedir
  
else
  echo "BuildData folder exists, skipped."
fi

if [ -d "work" ]; then
    echo "work folder exists, skipped."
    exit 0
fi

set -- $(jq -r '.minecraftVersion, .classMappings, .memberMappings, .accessTransforms, .packageMappings' BuildData/info.json)
minecraftVersion=$1
classMappings=$2
memberMappings=$3
accessTransforms=$4
packageMappings=$5

# Output path
mkdir work
vanillaJar="work/minecraft_server.$minecraftVersion.jar"

# Get server JAR URL
manifestUrl="https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
versionDataUrl=$(curl -s "$manifestUrl" | jq -r --arg ver "$minecraftVersion" '
  .versions[] | select(.id == $ver) | .url
')

serverJarUrl=$(curl -s "$versionDataUrl" | jq -r '.downloads.server.url')

# Download
echo "Starting download of $serverJarUrl"
curl -s -o "$vanillaJar" "$serverJarUrl"

minecraftHash=$(echo "$serverJarUrl" | grep -oP 'https://(?:launcher|piston-data)\.mojang\.com/v1/objects/\K[0-9a-f]{40}')
actualHash=$(sha1sum "$vanillaJar" | awk '{print $1}')

if [ "$actualHash" != "$minecraftHash" ]; then
    echo "❌ Hash mismatch! Downloaded file is corrupted or tampered with."
    exit 1
fi

echo "Downloaded file: $vanillaJar with hash: $actualHash"

# Get Hash

cd $basedir/BuildData
  
mappings=$(git log -n 1 --pretty=format:%H -- mappings/)
mappingsHash=$(echo -n "$mappings" | md5sum | awk '{print $1}')
mappingsVersion=$(echo "$mappingsHash" | tail -c 9)

cd $basedir

finalMappedJar="work/mapped.$mappingsVersion.jar"
echo "Final mapped jar: $finalMappedJar does not exist, creating (please wait)!"

# map

clMappedJar="$finalMappedJar-cl"
mMappedJar="$finalMappedJar-m"


java -jar BuildData/bin/SpecialSource-2.jar map -i $vanillaJar -m "BuildData/mappings/$classMappings" -o $clMappedJar

java -jar BuildData/bin/SpecialSource-2.jar map -i $clMappedJar -m "BuildData/mappings/$memberMappings" -o $mMappedJar

java -jar BuildData/bin/SpecialSource.jar --kill-lvt -i $mMappedJar --access-transformer "BuildData/mappings/$accessTransforms" -m "BuildData/mappings/$packageMappings" -o $finalMappedJar

cd work
mvn install:install-file \
  -Dfile="$basedir/$finalMappedJar" \
  -Dpackaging=jar \
  -DgroupId=org.spigotmc \
  -DartifactId=minecraft-server \
  -Dversion="${spigotVersion:-${minecraftVersion}-SNAPSHOT}"
cd $basedir
  
decompileDir="work/decompile-$mappingsVersion"
mkdir $decompileDir

clazzDir="$decompileDir/classes"
mkdir $clazzDir

unzip -Z1 $finalMappedJar | while read file; do
  case "$file" in
    "net/minecraft"*) 
      unzip -o -q "$finalMappedJar" "$file" -d "$clazzDir"
      echo "Extracted: $clazzDir/$file"
      ;;
  esac
done

java -jar BuildData/bin/fernflower.jar -dgs=1 -hdc=0 -rbr=0 -asc=1 -udv=0 $clazzDir $decompileDir

mv $decompileDir work/decompile
