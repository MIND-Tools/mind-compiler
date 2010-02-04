<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:d="http://docbook.org/ns/docbook"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                exclude-result-prefixes="d" version="1.0">

<xsl:import href="urn:docbkx:stylesheet" />
<xsl:import href="./minddoc-common.xsl" />

<xsl:param name="callout.graphics.path">src/docbkx/images/callouts/</xsl:param>

<!-- codelink template -->
<xsl:template match="d:codelink">

  <fo:basic-link xsl:use-attribute-sets="xref.properties">
    <xsl:attribute name="external-destination">
      <xsl:value-of
      select="concat(@jdocurl, '/', translate(@class,'.', '/'), '.html')" />
    </xsl:attribute>
    <fo:inline xsl:use-attribute-sets="monospace.properties">
      <xsl:choose>
        <xsl:when test="@showpkg = 'true'">
          <xsl:value-of select="@class" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="extract.classname"/>
        </xsl:otherwise>
      </xsl:choose>
    </fo:inline>
  </fo:basic-link>
  <xsl:if test="@codeurl">
    <fo:basic-link xsl:use-attribute-sets="xref.properties">
      <xsl:attribute name="external-destination">
        <xsl:value-of
          select="concat(@codeurl, '/', translate(@class,'.', '/'), '.html')" />
      </xsl:attribute>
      [code]
    </fo:basic-link>
  </xsl:if>
</xsl:template>


<!-- overridden EBNF templates -->
<xsl:template match="d:production">
  <xsl:param name="recap" select="false()"/>
  <xsl:variable name="id"><xsl:call-template name="object.id"/></xsl:variable>
  <fo:table-row>
    <fo:table-cell>
      <fo:block text-align="start">
        <xsl:text>[</xsl:text>
        <xsl:number count="d:production" level="any"/>
        <xsl:text>]</xsl:text>
      </fo:block>
    </fo:table-cell>
    <fo:table-cell>
      <fo:block text-align="end">
        <xsl:choose>
          <xsl:when test="$recap">
            <fo:basic-link internal-destination="{$id}"
                           xsl:use-attribute-sets="xref.properties">
              <xsl:apply-templates select="d:lhs"/>
            </fo:basic-link>
          </xsl:when>
          <xsl:otherwise>
            <fo:wrapper id="{$id}">
              <xsl:apply-templates select="d:lhs"/>
            </fo:wrapper>
          </xsl:otherwise>
        </xsl:choose>
      </fo:block>
    </fo:table-cell>
    <fo:table-cell>
      <fo:block text-align="center">
        <xsl:copy-of select="$ebnf.assignment"/>
      </fo:block>
    </fo:table-cell>
    <fo:table-cell>
      <fo:block text-align="start">
        <xsl:apply-templates select="d:rhs"/>
        <xsl:copy-of select="$ebnf.statement.terminator"/>
      </fo:block>
    </fo:table-cell>
    <fo:table-cell border-start-width="3pt">
      <fo:block text-align="start">
        <xsl:choose>
          <xsl:when test="d:rhs/d:lineannotation|d:constraint">
            <xsl:apply-templates select="d:rhs/d:lineannotation" mode="rhslo"/>
            <xsl:apply-templates select="d:constraint"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>&#160;</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </fo:block>
    </fo:table-cell>
  </fo:table-row>
</xsl:template>

<xsl:template match="d:lhs">
  <fo:inline font-family="serif" font-style="italic">
    <xsl:apply-templates/>
  </fo:inline>
</xsl:template>

<xsl:template match="d:rhs">
  <fo:inline xsl:use-attribute-sets="monospace.properties" font-weight="bold">
    <xsl:apply-templates/>
  </fo:inline>
</xsl:template>

<xsl:template match="d:nonterminal">
  <xsl:variable name="linkend">
    <xsl:call-template name="xpointer.idref">
      <xsl:with-param name="xpointer" select="@def"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:call-template name="check.id.unique">
    <xsl:with-param name="linkend" select="$linkend"/>
  </xsl:call-template>

  <xsl:call-template name="check.idref.targets">
    <xsl:with-param name="linkend" select="$linkend"/>
    <xsl:with-param name="element-list">production</xsl:with-param>
  </xsl:call-template>

  <!-- If you don't provide content, you can't point outside this doc. -->
  <xsl:choose>
    <xsl:when test="*|text()"><!--nop--></xsl:when>
    <xsl:otherwise>
      <xsl:if test="$linkend = ''">
    <xsl:message>
      <xsl:text>Non-terminals with no content must point to </xsl:text>
      <xsl:text>production elements in the current document.</xsl:text>
    </xsl:message>
    <xsl:message>
      <xsl:text>Invalid xpointer for empty nt: </xsl:text>
      <xsl:value-of select="@def"/>
    </xsl:message>
      </xsl:if>
    </xsl:otherwise>
  </xsl:choose>

  <xsl:variable name="href">
    <xsl:choose>
      <xsl:when test="$linkend != ''">
    <xsl:variable name="targets" select="key('id',$linkend)"/>
    <xsl:variable name="target" select="$targets[1]"/>
        <xsl:call-template name="object.id">
          <xsl:with-param name="object" select="$target"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
    <xsl:value-of select="@def"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <fo:basic-link internal-destination="{$href}"
                 xsl:use-attribute-sets="xref.properties">
    <fo:inline font-family="serif" font-style="italic"  font-weight="normal">
      <xsl:choose>
        <xsl:when test="*|text()">
          <xsl:apply-templates/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:choose>
            <xsl:when test="$linkend != ''">
              <xsl:variable name="targets" select="key('id',$linkend)"/>
              <xsl:variable name="target" select="$targets[1]"/>
              <xsl:apply-templates select="$target/d:lhs"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>???</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </fo:inline>
  </fo:basic-link>
</xsl:template>


</xsl:stylesheet>

