#!/bin/bash
echo "Installing BuildMagic"
COUNT=`awk '/^__SOURCE__/ { print NR + 1; exit 0; }' $0`
THIS=`pwd`/$0
tail -n+$COUNT $THIS | tar -C /usr/share -xj
echo "Finished"
exit 0
__SOURCE__
