#!/bin/bash
echo "Installing BuildMagic"
COUNT=`awk '/^__SOURCE__/ { print NR + 1; exit 0; }' $0`
THIS=`pwd`/$0
rm -rf /usr/share/buildmagic/*
tail -n+$COUNT $THIS | tar -C /usr/share -xj
if [ -d '/usr/share/ant/lib/' ]; then
    if [ ! -e '/usr/share/ant/lib/ant-contrib.jar' ]; then
        echo "Creating symlink /usr/share/ant/lib/ant-contrib.jar"
        ln -s /usr/share/buildmagic/ant-contrib.jar /usr/share/ant/lib/ant-contrib.jar
    fi
    if [ ! -e '/usr/share/ant/lib/buildmagic.jar' ]; then
        echo "Creating symlink /usr/share/ant/lib/buildmagic.jar"
        ln -s /usr/share/buildmagic/buildmagic.jar /usr/share/ant/lib/buildmagic.jar
    fi
    if [ ! -e '/usr/share/ant/lib/ivy.jar' ]; then
        echo "Creating symlink /usr/share/ant/lib/ivy.jar"
        ln -s /usr/share/buildmagic/ivy-2.3.0.jar /usr/share/ant/lib/ivy.jar
    fi
fi
echo >/usr/bin/buildmagic #!/bin/bash
echo >>/usr/bin/buildmagic java -cp /usr/share/buildmagic/buildmagic.jar com.kloudtek.buildmagic.Cli \$@
chmod +x /usr/bin/buildmagic
echo "Finished"
exit 0
__SOURCE__
