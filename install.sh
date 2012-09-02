#!/bin/bash

ant dist
sudo rm -rf /usr/share/buildmagic
sudo mkdir /usr/share/buildmagic
sudo rsync -av _build/dist/ /usr/share/buildmagic/
sudo rm -f /usr/share/ant/lib/buildmagic-bootstrap.jar
sudo ln -s /usr/share/buildmagic/buildmagic-bootstrap.jar /usr/share/ant/lib/buildmagic-bootstrap.jar
