#!/bin/bash

basedir=`pwd`


# clone NMS
echo "[Build] Cloning NMS"
if [ ! -d "NMSClasses" ]; then
  git clone https://github.com/speedcubing-top/NMSClasses.git
else
  echo "NMSClasses folder exists, skipped."
fi

# clone bukkit
echo "[Build] Cloning Bukkit"
if [ ! -d "Bukkit" ]; then

  git clone https://hub.spigotmc.org/stash/scm/spigot/bukkit.git Bukkit
  cd $basedir/Bukkit
  git reset --hard 01d1820664a5f881665b84b28871dadd132deaef
  cd $basedir
  
else
  echo "Bukkit folder exists, skipped."
fi


# clone craftbukkit
echo "[Build] Cloning CraftBukkit"
if [ ! -d "CraftBukkit" ]; then

  git clone -b version/1.8.8 --single-branch https://hub.spigotmc.org/stash/scm/spigot/craftbukkit.git CraftBukkit
  cd $basedir/CraftBukkit
  git checkout -b master

  # apply craftbukkit -> nms patches
  echo "[Build] Applying CraftBukkit->NMS Patches"

  cd $basedir/CraftBukkit
  mkdir -p src/main/java/net/minecraft/server

  for patch in nms-patches/*.patch; do
    base_name=$(basename "$patch" .patch)

    cp ../NMSClasses/net/minecraft/server/$base_name.java src/main/java/net/minecraft/server/$base_name.java

    echo "[CraftBukkit->NMS] Applying $base_name.patch"
    
    cd src/main/java
    git apply --whitespace=nowarn ../../../$patch
    cd ../../../
  done

  git branch -D patched
  git checkout -B patched
  git add src/main/java/net/
  git commit -m "CraftBukkit $ $(date)"
  git checkout 741a1bdf3db8c4d5237407df2872d9857427bfaf
  
  
  cd $basedir
  
else
  echo "CraftBukkit folder exists, skipped."
fi


# clone Paper
echo "[Build] Cloning Paper"

if [ ! -d "Paper" ]; then

  git clone -b ver/1.8.8 --single-branch https://github.com/PaperMC/Paper.git Paper
  cd $basedir/Paper
  git checkout -b master

  # move submodule
  cd $basedir
  git clone Bukkit Paper/Bukkit
  git clone CraftBukkit Paper/CraftBukkit
  
  cd $basedir/Paper/CraftBukkit
  git checkout -b patched origin/patched
  
  # apply patch
  cd $basedir/Paper
  ./newApplyPatches.sh
  
  
  cd $basedir

else
  echo "Paper folder exists, skipped."
fi

if [ ! -d "CubingPaper-API" ]; then
  git clone Paper/PaperSpigot-API CubingPaper-API
fi
if [ ! -d "CubingPaper-Server" ]; then
  git clone Paper/PaperSpigot-Server CubingPaper-Server
fi
