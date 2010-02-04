<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:d="http://docbook.org/ns/docbook"
                xmlns:xslthl="http://xslthl.sf.net"
                exclude-result-prefixes="d xslthl"
                version='1.0'>

<xsl:output method="html"
            encoding="ISO-8859-1"
            indent="no"
            doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
            doctype-system="http://www.w3.org/TR/html4/loose.dtd"/>


<xsl:param name="draft.watermark.image" select="''"/>


<xsl:template match="d:codelink">
  <a>
    <xsl:attribute name="class">codelink-javadoc</xsl:attribute>
    <xsl:attribute name="href">
      <xsl:value-of
      select="concat(@jdocurl, '/', translate(@class,'.', '/'), '.html')" />
    </xsl:attribute>
    <code class="classname">
      <xsl:choose>
        <xsl:when test="@showpkg = 'true'">
          <xsl:value-of select="@class" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="extract.classname"/>
        </xsl:otherwise>
      </xsl:choose>
    </code>
  </a>
  <xsl:if test="@codeurl">
    <a>
      <xsl:attribute name="class">codelink-code</xsl:attribute>
      <xsl:attribute name="href">
        <xsl:value-of
          select="concat(@codeurl, '/', translate(@class,'.', '/'), '.html')" />
      </xsl:attribute>
      [code]
    </a>
  </xsl:if>
</xsl:template>

<xsl:template name="user.head.content">
  <xsl:param name="node" select="." />
  <meta name="Keywords"
    content="MIND,documentation,guide,Fractal,component,model,binding,attribute,C,C++,Java,composition,framework,composite,configuration,administration,programming,annotation,architecture,language,lightweight,embedded,ADL,IDL,CPL,open,source,free,software,LGPL" />
  <meta name="Robots" content="index, follow" />
  <meta content="MIND team" name="author" />
  <meta content="mind@ow2.org" name="email" />
  <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
</xsl:template>

</xsl:stylesheet>