#!/bin/sh

cd war
echo -e "CACHE MANIFEST\n" > player.appcache
echo -e "CACHE:" >> player.appcache
echo -e "Player.html" >> player.appcache
echo -e "favicon.ico" >> player.appcache
echo -e "Player.css" >> player.appcache
echo -e "Common.css" >> player.appcache
find player/* -type f >> player.appcache