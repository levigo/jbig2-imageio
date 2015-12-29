# Welcome to the Java ImageIO plugin for the JBIG2 image format!
The Java ImageIO plugin for JBIG2 enables access to images encoded using the JBIG2 image compression standard.
Please [tell us](mailto:solutions@levigo.de) about your experience!

## Features
The key features of the plugin are:

- Read-only (decode) support for images encoded using the JBIG2 standard, aka *ITU T.88* and *ISO/IEC 14492*.
- Full support for all features defined in the standard, including arithmetic coding, Huffman coding and MQ coding.
- Pure Java code. No funny native or JNI stuff required.
- Secure.
- Production quality. Robust and well-tested.
- Reasonable performance.
- Standard ImageIO API.
- Support for shared data segments as used by PDF via custom DecodeParams.

## License
This ImageIO plugin is licensed under the [GNU General Public License V3](http://www.gnu.org/licenses/gpl.html). Alternatively see [here](gpl.txt).
For other licensing options please [contact us](mailto:solutions@levigo.de).

## Release Notes
Please take a look at the [release notes](release-notes.md).

## Support
Support is available via issue tracker. Commercial support is available. For inquiries please [contact us](mailto:solutions@levigo.de).

## Usage
### Introduction
Please choose the appropriate section below, depending on whether you need to embed the ImageIO decoder inside your application (which would, for example, be the case if you want to support shared data segments) or you just want to add JBIG2 support to an existing ImageIO-enabled application.

### Use within existing Image I/O-based applications
Using the JBIG2 plugin with an existing application that already supports Java ImageIO is - at least in theory - very simple: just plunk the plugin jar down into your classpath and you're ready to go. What, exactly, you have to do to achieve this, depends on the application in question. Unfortunately, there is no general way to add the plugin to an application. Some general recommendations:

- If you implemented the application yourself: hey, don't look at us. You'll know best what to do. If you build your application using Maven, just pull in the plugin as a dependency. See below.
- Consult the application's manual (well, duh!).
- Maybe the application installation has a folder called 'plugins' or something like that. Try putting the jar into it. With some luck, the application will pick it up.
- Is the application started using a Unix shell script or Windows batch file? Try to identify where the classpath is assembled and add the plugin jar to it.
- As a last resort, you could try to add the plugin jar to the [lib/ext directory of your JRE installation](http://download.oracle.com/javase/1.4.2/docs/guide/extensions/spec.html). But please be advised that this is not considered to be good style.

### Use dependency management
The JBIG2 ImageIO-Plugin is available from [Maven Central](http://search.maven.org/). 

#### Maven
To use the plugin within a Maven POM-based project, simply include a dependency to the following artifact in the appropriate ```pom.xml```:

    <dependency>
      <groupId>com.levigo.jbig2</groupId>
      <artifactId>levigo-jbig2-imageio</artifactId>
      <version>1.6.5</version>
    </dependency>

### How to deal with embedded JBIG2 data
Several formats allow to embed JBIG2-compressed data in its own structure. PDF, for example, supports JBIG2-compressed data and adds the ability to embed shared data segments. Therefore the `JBIG2ImageReader` can handle `JBIG2Globals`  which are stored separately and can be passed into the reader if neccessary via `setGlobals()`-method.

The reader recognizes if the input data is headless and so embedded. This is assumed if the file header is missing.

You can also specify that the coming input data is embedded by using the special constructor in `JBIG2ImageReader`.

### What if the plugin is on classpath but not seen?
ImageIO is able to scan the classpath for readers and writers. Call `ImageIO.scanForPlugins()` if the reader is not seen. (Note: Thanks to George Sexton for this tip in context of using ImageIO within Apache Tomcat)
