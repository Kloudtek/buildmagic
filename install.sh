#!/bin/bash

ant deb
sudo apt-get purge -y buildmagic
sudo dpkg -i _build/artifacts/buildmagic.deb
