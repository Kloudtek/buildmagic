## Overview

I've always found maven to have some good concepts behind it, but a terribly bad implementation.

BuildMagic is designed to bring some of those concepts to ant, and allow developers to have very easily reusable build
templates that can be imported with a single ant task.

For example, if you had a simple java projects which generated a single jar file, you could write the following ant script:

```xml
<project name='myproject' xmlns:bm="antlib:com.kloudtek.buildmagic">
  <bm:init/>
  <bm:template name="simple-java"/>
</project>
```

You could then generate the jar by just calling `ant artifacts`.

It also includes ivy by default, which you can easily integrate into your build using it's template.

```
<project name='myproject' xmlns:bm="antlib:com.kloudtek.buildmagic">
  <bm:template name="simple-java"/>
  <bm:template name="ivy"/>
</project>
```

With this build script, if you wanted to download all ivy dependencies, generate the jar file and publish it using ivy,
you just need to run `ant deps publish`.

## Usage

In order to use buildmagic, it's jar file (either buildmagic-all.jar which include all dependencies, or buildmagic.jar
and each individual dependency) should be loaded as an antlib.

This could be done by using `ant -jar` command line, by having it the jars in the lib directory of your ant installation,
or by using taskdef as [described here](http://ant.apache.org/manual/Types/antlib.html).

Then you can simple use the template task to load a template.

What the template task does is quite simple, it will [import](https://ant.apache.org/manual/Tasks/import.html) an ant build
file in the classpath in the location of `templates/[name]/[name].xml`.

So if you use `<bm:template name="simple-java"/>`, this will simply import `templates/simple-java/simple-java.xml`. Templates
are only loaded once, so it's safe to import the same one multiple times (in fact it is common that each template might
import other ones that it's dependent on, especially the 'base' template).

To facilitate various templates integrating together seamlessly, we've create the base template, which creates the following
extension points:

```xml
<extension-point name="pre-clean"/>
<extension-point name="clean" depends="pre-clean"/>
<extension-point name="clean-deps" depends="pre-clean"/>

<extension-point name="pre-retrieve-deps" depends="init"/>
<extension-point name="retrieve-deps" depends="pre-retrieve-deps"/>

<extension-point name="init"/>
<extension-point name="generate-sources" depends="init"/>
<extension-point name="pre-compile" depends="generate-sources"/>
<extension-point name="compile" depends="pre-compile"/>
<extension-point name="post-compile" depends="compile"/>
<extension-point name="unit-tests" depends="post-compile"/>
<extension-point name="reports" depends="post-compile"/>
<extension-point name="pre-artifacts" depends="post-compile"/>
<extension-point name="artifacts" depends="pre-artifacts"/>
<extension-point name="pre-dist" depends="artifacts"/>
<extension-point name="dist" depends="pre-dist"/>

<extension-point name="publish" depends="artifacts,dist"/>
<extension-point name="install" depends="artifacts,dist"/>
```

Templates should generally import the base template (using `<bm:template name="base"/>`), and extend those extension points.

For example, simple-java.xml is:

```java
<project name="bmsimplejava" xmlns:bm="antlib:com.kloudtek.buildmagic">
    <bm:template name="base"/>

    <target name="classes" extensionOf="compile">
        <property name="javac.srcdir" value="${basedir}/src/java"/>
        <property name="javac.destdir" value="${build.dir}/classes"/>
        <mkdir dir="${build.dir}/classes"/>
        <javac destdir="${javac.destdir}" srcdir="${javac.srcdir}" classpathref="buildmagic.classpath.compile"/>
    </target>

    <target name="jar" extensionOf="artifacts">
        <property name="javac.destdir" value="${build.dir}/classes"/>
        <property name="jar.name" value="${ant.project.name}"/>
        <property name="jar.dest" value="${artifacts.dir}/${jar.name}.jar"/>
        <jar destfile="${jar.dest}" basedir="${javac.destdir}">
            <fileset dir="${javac.srcdir}" excludes="**/*.java"/>
        </jar>
    </target>
</project>
```

That way, if you call the `artifacts` target, it will run both the classes and jar targets automatically (plus any others
you might have defined in your build script, or from other imported templates.

## References

Further details can be found here:

[Installation](https://github.com/Kloudtek/buildmagic/wiki/Installation)

[Templates](https://github.com/Kloudtek/buildmagic/wiki/Templates)