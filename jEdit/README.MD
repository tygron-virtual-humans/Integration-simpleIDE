jEdit 
=====

This is a plugin for the SimpleIDE, based on the jEdit text editor.

This project started with a copy of jEdit 4.3.1.

http://sourceforge.net/projects/jedit/files/jedit/4.3.1/
 
but then was developed into a modified version for GOAL SimpleIDE
 
 
Release Procedure
=============

Ensure your ~/.m2/settings.xml file is as follows:

```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                          http://maven.apache.org/xsd/settings-1.0.0.xsd">
	<servers>
		<server>
   			<id>github</id>
   			<username>YOUR_USERNAME</username>
   			<password>YOUR_PASSWORD</password>
		</server>
	</servers>
</settings>
```

Then call:

```
mvn deploy
```