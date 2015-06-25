## Description

Maven plugin for collecting specific artifacts from multi module maven project into single directory.
Useful only with multi modules project - does nothing in single pom project.

## Configuration
* skip - skips execution.
  Default: false
* target directory - place where collected files will be put. If path is absolute than plugin assumes that
  directory exists and is writable. If relative then it is relative to top level build directory.
  Default: collected-artifacts
* target packaging - packaging type to collect. Examples: jar, war, rpm etc.
  Default: rpm

## Usage
Add sonatype snapshot repo to your maven repository list and configure plugin in your root pom.xml's build plugins section.

Example for binding plugin to install phase.

``` xml
<plugin>
	<groupId>com.github.skrethel</groupId>
	<artifactId>artifact-collector-maven-plugin</artifactId>
	<version>1.0-SNAPSHOT</version>
	<executions>
		<execution>
			<phase>install</phase>
			<goals>
				<goal>collect</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```