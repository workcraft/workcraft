# Minimal Batik

This cut down version of Batik is the minimal set of JARs necessary
for building a functioning Workcraft. This effort was made in order
to reduce the distribution size of Workcraft by ~10MB.

The JARs of Batik were collected from binary distribution at:
https://xmlgraphics.apache.org/batik/download.html

The JAR for FOP Transcoder was downloaded from:
https://mvnrepository.com/artifact/org.apache.xmlgraphics/fop-transcoder

If extra ~10MB and several unnecessary dependencies are not a problem,
then Batik and FOP transcoders can be included via Gradle in
dependencies section of WorkcraftCore\build.gradle as follows:

    lib 'org.apache.xmlgraphics:batik-transcoder:1.14'
    lib 'org.apache.xmlgraphics:fop-transcoder:2.6'


## Dependency analysis

Here we determine the minimal set of Batik JARs necessary for correct
functionality of Workcraft.

### Compile time dependency

  * batik-anim -- needed for SVG
    (SAXSVGDocumentFactory)

  * batik-awt-util -- needed for SVG
    (AbstractGraphics2D, SVGGraphics2D)

  * batik-bridge -- needed for GUI
    (UserAgentAdapter, BridgeContext, GVTBuilder)

  * batik-constants -- needed for XML constants
    (XMLConstants)

  * batik-css -- needed for creating icon from SVG
    (CSSContext)

  * batik-dom -- needed for creating icon from SVG
    (DocumentFactory)

  * batik-gvt -- needed for creating icon from SVG
    (GraphicsNode)

  * batik-svg-dom -- needed for creating icon from SVG
    (SVGDocumentFactory)

  * batik-svggen -- needed for exporting in SVG format
    (SVGGraphics2D)

  * batik-transcoder -- needed for exporting in PDF, PS, EPS formats
    (Transcoder, TranscoderInput, TranscoderOutput, TranscoderException)

  * batik-util -- needed for loading SVG files
    (XMLResourceDescriptort)

  * fop-transcoder -- needed for exporting in PDF, PS, EPS formats
    (PDFTranscoder, PSTranscoder, EPSTranscoder, PNGTranscoder)

### Run time dependency

  * batik-codec -- needed for loading SVG files
    (java.lang.NoClassDefFoundError: org/apache/xmlgraphics/java2d/color/NamedColorSpace)

  * batik-ext -- needed for loading SVG files
    (java.lang.NoClassDefFoundError: org/w3c/dom/ElementTraversal)

  * batik-i18n -- needed for loading SVG files
    (java.lang.NoClassDefFoundError: org/apache/batik/anim/dom/SVGDOMImplementation)

  * batik-parser -- needed for loading SVG files
    (java.lang.NoClassDefFoundError: org/apache/batik/parser/UnitProcessor$Context)

  * batik-script -- needed for loading SVG files
    (java.lang.NoClassDefFoundError: Could not initialize class org.apache.batik.bridge.BridgeContext)

  * batik-xml -- needed for loading SVG files
    (java.lang.NoClassDefFoundError: org/apache/batik/dom/util/DOMUtilities)

  * xml-apis-ext -- needed for loading SVG files
    (java.lang.NoClassDefFoundError: org/apache/batik/anim/dom/SVGOMDocument)

  * xmlgraphics-commons -- needed for loading SVG files
    (java.lang.NoClassDefFoundError: org/apache/xmlgraphics/java2d/color/NamedColorSpace)

### No dependency found

  * batik-extension
  * batik-gui-util
  * batik-js
  * batik-rasterizer
  * batik-rasterizer-ext
  * batik-slideshow
  * batik-squiggle
  * batik-squiggle-ext
  * batik-svgpp
  * batik-svgrasterizer
  * batik-swing
  * batik-test
  * batik-test-old
  * batik-test-svg
  * batik-test-swing
  * batic-ttf2svg
  * fop-pdf-images
  * xml-apis  -- there is still run-time dependency on xml-apis-ext
