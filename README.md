Hey all,

this is my code-sink for mainly java files.

###Have fun to browse through many things for:

- Datastructures & Algorithms
- Apache Hadoop
- JCuda experiments
- [Graph algorithms (they are now in the other repository called "tjungblut-graph")](https://github.com/thomasjungblut/tjungblut-graph "Thomas' nifty graph lib")
- [Machine learning (clustering, classification, optimization algorithms, this will partially move to "tjungblut-online-ml")](https://github.com/thomasjungblut/tjungblut-online-ml)
- [Matrix and Vector stuff (they are now in the other repository called "tjungblut-math")](https://github.com/thomasjungblut/tjungblut-math "Thomas' nifty math lib")

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
     <version>1.0</version>
 </dependency>
```

Package Layout
--------------

- /src -> raw source code.
- /test -> testcases for the code in /src.
- /files -> reserved directory for files in examples or ignored paths for output of applications.
- /jcuda -> the cuda 3rd party libs for the de.jungblut.math.cuda package

Build
-----

You will need Java 8 first.
You can simply build with:

> mvn clean package install

If you hit an issue that my math lib (e.g. a SNAPSHOT build) is missing, pull the latest from ["tjungblut-math")](https://github.com/thomasjungblut/tjungblut-math "Thomas nifty math lib") and execute the build information there- then retry building this library. 

The created jar contains debuggable code + sources. On the unfortunate event of failing testcases you can skip them:
 
> mvn clean package install -DskipTests

If you want to skip the signing process you can do:

> mvn clean package install -Dgpg.skip=true