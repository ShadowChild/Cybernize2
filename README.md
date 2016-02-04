# Cybernize [![Build Status](https://travis-ci.org/ShadowChild/Cybernize.svg?branch=master)](https://travis-ci.org/ShadowChild/Cybernize)
A common library i'll be using for my projects

To install for use in other projects, run the following

`
mvnw.cmd clean install
`

or

`
mvnw clean install
`


To implement this into your project, you have two choices, either though adding a jar as a dependancy, or by using a dependency management system. It is recommended that you use a dependency management system such as Maven, or Gradle as this can greatly help with the deployment of your programs, and/or when others try to contribute to your project.

Below is how you would implement this project into either maven or gradle

### Maven
```xml
<properties>
        <cynbernize.version>1.0-SNAPSHOT</cynbernize.version>
</properties>

<dependencies>
  <dependency>
    <groupId>io.github.shadowchild</groupId>
    <artifactId>common-core</artifactId>
    <version>${cybernize.version}</version>
  </dependency>
  <dependency>
    <groupId>io.github.shadowchild</groupId>
    <artifactId>common-opengl</artifactId>
    <version>${cybernize.version}</version>
  </dependency>
  <dependency>
    <groupId>io.github.shadowchild</groupId>
    <artifactId>common-graphics</artifactId>
    <version>${cybernize.version}</version>
  </dependency>
</dependencies>
```

## Gradle
```groovy
repositories {
    mavenLocal()
}

project.ext.cybernizeVersion = "1.0-SNAPSHOT"

dependencies {
    compile "io.github.shadowchild:common-core:${cybernizeVersion}"
    compile "io.github.shadowchild:common-opengl:${cybernizeVersion}"
    compile "io.github.shadowchild:common-graphics:${cybernizeVersion}"
}
```

The `common-core` dependency is required for any of the other projects you wish to use
