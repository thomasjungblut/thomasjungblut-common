Hey all,

this is my code-sink for mainly java files.

### Have fun to browse through many things for:

- Datastructures & Algorithms
- [Graph algorithms (they are now in the other repository called "tjungblut-graph")](https://github.com/thomasjungblut/tjungblut-graph "Thomas' nifty graph lib")
- [Machine learning (clustering, classification, optimization algorithms, this will partially move to "tjungblut-online-ml")](https://github.com/thomasjungblut/tjungblut-online-ml)
- [Matrix and Vector stuff (they are now in the other repository called "tjungblut-math")](https://github.com/thomasjungblut/tjungblut-math "Thomas' nifty math lib")
- [Approx. Nearest Neighbours (eg the KDTree: they are now in the JRPT project)](https://github.com/thomasjungblut/JRPT "Thomas' JRPT project")

Please notice that this is in fact no framework, much rather a common library for my everyday usage. 
I won't bother about backward compatibility, extensive documentation and full test coverage- although I try my best to at least fulfill the last two.

License
-------

Since I am Apache committer, I consider everything inside of this repository 
licensed by Apache 2.0 license, although I haven't put the usual header into the source files.

If something is not licensed via Apache 2.0, there is a reference or an additional licence header included in the specific source file.

Maven
-----

If you use maven, you can get the latest release using the following dependency:

```
 <dependency>
     <groupId>de.jungblut.common</groupId>
     <artifactId>thomasjungblut-common</artifactId>
     <version>1.1</version>
 </dependency>
```

Build
-----

You will need Java JDK 11 first. Then you can simply build with:

> mvn clean package install

The created jar contains debuggable code + sources. On the unfortunate event of failing testcases you can skip them:
 
> mvn clean package install -DskipTests

If you want to skip the signing process you can do:

> mvn clean package install -Dgpg.skip=true

Handy maven commands
--------------------

Displays dependency updates:

> mvn versions:display-dependency-updates



