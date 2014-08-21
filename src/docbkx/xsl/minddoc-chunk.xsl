<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:d="http://docbook.org/ns/docbook"
                xmlns:ng="http://docbook.org/docbook-ng"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:exsl="http://exslt.org/common"
                xmlns:exslt="http://exslt.org/common"
                exclude-result-prefixes="db ng exsl exslt d"
                version='1.0'>

<xsl:import href="urn:docbkx:stylesheet" />
<xsl:import href="./minddoc-html-common.xsl" />
<xsl:import href="./minddoc-common.xsl" />

<!-- Depth to which sections should be chunked -->
<xsl:param name="chunk.section.depth" select="2"></xsl:param>

<xsl:param name="pdf.link"/>
<xsl:param name="single.link"/>
<xsl:param name="printable.link"/>


<xsl:template name="chunk-element-content">
  <xsl:param name="prev"/>
  <xsl:param name="next"/>
  <xsl:param name="nav.context"/>
  <xsl:param name="content">
    <xsl:apply-imports/>
  </xsl:param>

  <xsl:call-template name="user.preroot"/>

  <html>
    <xsl:call-template name="html.head">
      <xsl:with-param name="prev" select="$prev"/>
      <xsl:with-param name="next" select="$next"/>
    </xsl:call-template>

    <body>
      <xsl:call-template name="body.attributes"/>

      <xsl:call-template name="user.header.content"/>

      <div id="chunk-content">
      <xsl:call-template name="user.header.navigation"/>

      <xsl:call-template name="header.navigation">
        <xsl:with-param name="prev" select="$prev"/>
        <xsl:with-param name="next" select="$next"/>
        <xsl:with-param name="nav.context" select="$nav.context"/>
      </xsl:call-template>

      <xsl:copy-of select="$content"/>

      <xsl:call-template name="footer.navigation">
        <xsl:with-param name="prev" select="$prev"/>
        <xsl:with-param name="next" select="$next"/>
        <xsl:with-param name="nav.context" select="$nav.context"/>
      </xsl:call-template>

      <xsl:call-template name="user.footer.navigation"/>
      </div>

      <xsl:call-template name="user.footer.content"/>

    </body>
  </html>
  <xsl:value-of select="$chunk.append"/>
</xsl:template>



<xsl:template name="user.header.content">
  <xsl:param name="node" select="." />
  <div id="banner">
    <div id="bannersearch">
      <form method="get" action="http://www.google.com/custom" target="_blank">
        <input type="hidden"
          value="S:http://www.ow2.org/;GL:0;AH:center;LH:70;L:http://www.ow2.org/xwiki/skins/ow2/logo.png;LW:500;AWFID:48faba182d01f379;"
          name="cof" />
        <input type="hidden" value="ow2.org;mail-archive.ow2.org" name="domains" />
        <input type="hidden" value="ow2.org" name="sitesearch" />
        <table colspan="0" rowspan="0">
          <tr>
            <td>
              <input type="text" name="q" size="10" maxlength="255"
                value="" onfocus="this.value=''" onblur="if (this.value=='') this.value='search'" />
            </td>
            <td>
              <input type="image" name="sa" id="search"
                src="../images/boutonsearch1.gif" onmouseout="MM_swapImgRestore()"
                onmouseover="MM_swapImage('search','','../images/boutonsearch2.gif',1)"
                alt="Submit" />
            </td>
          </tr>
        </table>
        <p class="glose">
          <strong>
            <a class="lienglose" target="_blank"
              href="http://www.google.com/advanced_search?q=+site:ow2.org">Advanced Search</a>
          </strong>
          - Powered by Google
        </p>
      </form>
    </div>
    <a href="http://ow2.org/" id="bannerLeft">
      <img src="../images/logoow2.png" />
    </a>
    <div id="bannerlink">
      <a href="http://www.ow2.org/" class="bannerlinkleft">Consortium</a>
      <a href="http://www.ow2.org/xwiki/bin/view/Activities/Fundamentals"
        class="bannerlinkleft">Activities</a>
      <a href="http://www.ow2.org/xwiki/bin/view/Activities/Projects" class="bannerlinkleft">
        Projects</a>
      <a href="http://forge.objectweb.org/" class="bannerlinkleft">Forge</a>
      <a href="http://www.ow2.org/view/Events/" class="bannerlinkleft">Events</a>
    </div>
  </div>
</xsl:template>

<xsl:template name="user.footer.content">
  <xsl:param name="node" select="." />
  <div id="menuOuter">
    <ul>
      <li>
        <div>MIND</div>
        <ul>
          <li>
            <div>Project Links</div>
            <ul>
              <li>
                <a class="menu" href="http://mind.ow2.org/index.html">Home</a>
              </li>
              <li>
                <a class="menu" href="http://mind.ow2.org/documentation.html">Documentation</a>
              </li>
              <li>
                <a class="menu" href="http://mind.ow2.org/download.html">Download</a>
              </li>
              <li>
                <a class="menu" href="http://mind.ow2.org/license.html">License</a>
              </li>
            </ul>
          </li>
          <li>
            <div>Developers' Corner</div>
            <ul>
              <li>
                <a href="http://forge.ow2.org/projects/mind/">Forge Site</a>
              </li>
              <li>
                <a href="http://fisheye.ow2.org/browse/MIND">SVN Repository</a>
              </li>
              <li>
                <a href="http://jira.ow2.org/browse/MIND">Issues Tracker</a>
              </li>
              <li>
                <a href="http://bamboo.ow2.org/browse/MIND">Continuous Integration</a>
              </li>
            </ul>
          </li>
          <li>
            <div>About</div>
            <ul>
              <li>
                <a class="menu" href="http://mind.ow2.org/team.html">Team</a>
              </li>
              <li>
                <a target="_self" href="http://mail.ow2.org/wws/info/mind">Mailing List Archive</a>
              </li>
            </ul>
          </li>
        </ul>
      </li>
      <li>
        <div>
          <xsl:apply-templates mode="html.menu.title" select="/d:book/d:title"/>
        </div>
        <ul>
          <li>
            <div>Chapters</div>
            <ul>
              <li>
                <a>
                  <xsl:attribute name="href">
                    <xsl:call-template name="href.target">
                      <xsl:with-param name="object" select="/*[1]"/>
                    </xsl:call-template>
                  </xsl:attribute>
                  Table of Contents
                </a>
              </li>
              <xsl:apply-templates mode="html.menu.chapters" select="/d:book/d:chapter|/d:book/d:appendix"/>
            </ul>
          </li>
          <li>
            <div>Links</div>
            <ul>
              <xsl:if test="$pdf.link">
                <li>
                  <xsl:element name="a">
                    <xsl:attribute name="href"><xsl:value-of select="$pdf.link"/></xsl:attribute>
                    PDF version
                  </xsl:element>
                </li>
              </xsl:if>
              <xsl:if test="$single.link">
                <li>
                  <xsl:element name="a">
                    <xsl:attribute name="href"><xsl:value-of select="$single.link"/></xsl:attribute>
                    Single HTML page
                  </xsl:element>
                </li>
              </xsl:if>
              <xsl:if test="$printable.link">
                <li>
                  <xsl:element name="a">
                    <xsl:attribute name="href"><xsl:value-of select="$printable.link"/></xsl:attribute>
                    Printable HTML
                  </xsl:element>
                </li>
              </xsl:if>
            </ul>
          </li>
        </ul>
      </li>
    </ul>
  </div>
</xsl:template>

<xsl:template match="d:title" mode="html.menu.title">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="d:chapter|d:appendix" mode="html.menu.chapters">
  <xsl:param name="toc-context" select="."/>
  <li>
    <a>
      <xsl:attribute name="href">
        <xsl:call-template name="href.target">
          <xsl:with-param name="context" select="$toc-context"/>
          <xsl:with-param name="toc-context" select="$toc-context"/>
        </xsl:call-template>
      </xsl:attribute>
      <!-- * if $autotoc.label.in.hyperlink is non-zero, then output the label -->
      <!-- * as part of the hyperlinked title -->
      <xsl:if test="not($autotoc.label.in.hyperlink = 0)">
        <xsl:variable name="label">
          <xsl:apply-templates select="." mode="label.markup"/>
        </xsl:variable>
        <xsl:copy-of select="$label"/>
        <xsl:if test="$label != ''">
          <xsl:value-of select="$autotoc.label.separator"/>
        </xsl:if>
      </xsl:if>

      <xsl:apply-templates select="." mode="titleabbrev.markup"/>
    </a>
  </li>
</xsl:template>

</xsl:stylesheet>

