buildmagic
==========

1) Overview

Buildmagic is an extension to the ant build system designed to facilitate the creation and distribution of

re-usable build templates, as well as providing powerful new tasks.

2) Installation

- Debian

- Manual

Extract the distribution archive, ( preferably under your ant installation /usr/share or C:\Program File ).

Optionally, copy or symlink buildmagic-bootstrap.jar into your ant lib directory

3) Usage

In order to be able to use buildmagic, the first step is to add both the buildmagic core antlib namespace to your build.xml, as well as the buildmagic 'init' task.

ie:

<project name="My Project" xmlns:bmc="antlib:com.kloudtek.buildmagic">
  <bmc:init/>
</project>

In order to run, buildmagic must be able to locate it's bootstrap library, and the buildmagic distribution.

* Bootstrap library

The bootstrap library must be loaded through the normal antlib loading methods, either by:

  copying/symlinking it into ant's lib directory

  adding it to the ant's classpath (for example using ant -lib command line parameter)

  loading it using the typedef ant task

* BuildMagic distribution

Buildmagic will automatically find it's installed binaries if they installed under any of the following locations:

- ${user.home}/buildmagic

- ${user.home}/apps/buildmagic

- ${ant.home}/buildmagic

- /usr/share/buildmagic

- /opt/buildmagic

- /Library/buildmagic

- C:\Program File\buildmagic

If it's not under any of the specified directories (or if you wish to use a different installation of buildmagic),

you can specify the buildmagic installation directory using the ant property 'buildmagic.home'.

ie:

ant -lib /tmp/buildmagic/buildmagic-bootstrap.jar -Dbuildmagic.home=/tmp/buildmagic some-target

When the init task is run, it will perform several operations (this behavior can be modified by using the init

task parameters):

Load Apache Ivy antlib in the namespace antlib:com.apache.ivy [TODO: Correct namespace]

Load any of the following file property files (in that order) if found:

  ${basedir}/build-${user.name}.properties [TODO: Check user.name correct]

  ${user.home}/.builmagic.properties

  ${user.home}/builmagic.properties

  ${basedir}/build.properties

  /etc/buildmagic/buildmagic.properties

If any of the following ivy settings files is found, load it using ivy's settings ant task

  ${basedir}/ivysettings-${user.name}.xml [TODO: Check user.name correct]

  ${user.home}/ivysettings.xml

  ${user.home}/.ivysettings.xml

  ${basedir}/ivysettings.xml

  /etc/buildmagic/ivysettings.xml

4) Ant Templates

Buildmagic's ant templates are built on ant's import capabilities. In order to use it, you must use the template task.

Templates can either be simple (a single ant build script), or composite (an ant build script, plus various other artifacts)

The template ant task functions just like ant's import task, with the following differences:

  - Each template is only ever loaded once. The template task will use a variable 'buildmagic.templates.loaded.[templatename]' to ensure it.

  Rather than specifying the full path to the template build script, only it's name needs to be specified, and buildmagic will automatically search for it using the following patterns:

    ${ant.home}/templates/[templatename]/[templatename].xml

    ${buildmagic.home}/templates/[templatename]/[templatename].xml

  Alternatively it's possible to specify a specific directory using the 'dir' attribute of the template task.

5) Built-in templates

BuildMagic comes with a number of templates that can be used:

- base

Defines various standards properties, extension points and paths for use by other templates.

- ivy

This template will enable the use of ivy for the project.

- simple-java

Simple template to compile java code

- simple-jar

Simple template to generate a jar file

Provides various tools to generate debian packages
