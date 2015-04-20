SimpleIDE
=========

An IDE for GOAL based on JEdit


Usage
=====

 * Download the jar 
 * Double click the jar to run it 
 
Dependency Information
=============

```
<repository>
	<id>goalhub-mvn-repo</id>
	<url>https://raw.github.com/goalhub/mvn-repo/master</url>
</repository>

```

```
<dependency>
	<groupId>com.github.goalhub.simpleide</groupId>
	<artifactId>simpleide</artifactId>
	<version>1.0.2-SNAPSHOT</version>
</dependency>
```	

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

Note that you must have a public name and e-mail address set on GitHub for this to work correctly (https://github.com/settings/profile)

<div id="sampleDiv">Nothing loaded</div>

<script>alert('hello');</script>


<script type="text/javascript">
var s = "JavaScript syntax highlighting";
document.getElementById("sampleDiv").innerHTML = "test";
</script>