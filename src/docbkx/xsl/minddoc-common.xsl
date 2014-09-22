<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version='1.0'>

<xsl:import href="urn:docbkx:stylesheet/highlight.xsl" />

<xsl:param name="section.autolabel" select="1"/>
<xsl:param name="section.label.includes.component.label" select="1"/>
<xsl:param name="section.autolabel.max.depth" select="3"/>

<xsl:param name="use.extensions" select="1"/>
<xsl:param name="linenumbering.extension" select="1"/>
<xsl:param name="linenumbering.everyNth" select="1"/>
<xsl:param name="ebnf.table.bgcolor" select="''"/>

<xsl:attribute-set name="monospace.verbatim.properties">
  <xsl:attribute name="background-color">#E8EAF0</xsl:attribute>
<!--  <xsl:attribute name="padding">0.1in</xsl:attribute>-->
</xsl:attribute-set>

<xsl:param name="ulinkShow" select="0"/>
<xsl:param name="page.type" select="A4" />

<xsl:param name="glossary.sort" select="1" />
<xsl:param name="glossary.as.blocks" select="1" />
<xsl:param name="glossentry.show.acronym" select="'primary'" />

<xsl:template name="extract.classname">
  <xsl:param name="classname" select="@class"/>
  
  <xsl:choose>
    <xsl:when test="substring-after($classname, '.')">
      <xsl:call-template name="extract.classname">
        <xsl:with-param name="classname" 
                        select="substring-after($classname, '.')"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$classname"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>
</xsl:stylesheet>

