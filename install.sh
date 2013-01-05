#!/bin/bash

set -e

ant dist

sudo rsync -av --delete _build/dist/ /usr/share/buildmagic/
sudo rm -f /usr/share/ant/lib/buildmagic-boostrap.jar
sudo ln -s /usr/share/buildmagic/buildmagic-boostrap.jar /usr/share/ant/lib/buildmagic-boostrap.jar
