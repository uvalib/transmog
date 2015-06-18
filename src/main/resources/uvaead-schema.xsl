<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="2.0" xmlns="urn:isbn:1-931666-22-9">

  <xsl:output indent="yes" />

  <xsl:template match="DOCUMENT">
    <xsl:processing-instruction name="xml-model">href="http://text.lib.virginia.edu/dtd/eadVIVA/ead-ext.rng"
      type="application/xml"
      schematypens="http://relaxng.org/ns/structure/1.0"
      title="extended EAD relaxng profile"</xsl:processing-instruction>
    <ead xmlns="urn:isbn:1-931666-22-9">
      <xsl:comment>
        This EAD XML file was made by transforming the structure assigned to the paragraphs of
        a WORD document using Transmog.  The current transformation is incomplete and untested.

        For more information go to: http://github.com/mikedurbin/transmog
      </xsl:comment>
      <eadheader audience="internal" langencoding="iso639-2b" findaidstatus="edited-partial-draft" scriptencoding="iso15924" dateencoding="iso8601" countryencoding="iso3166-1" repositoryencoding="iso15511">
        <eadid></eadid>
        <filedesc>
          <titlestmt>
            <xsl:apply-templates select="TITLEPROPER" mode="TITLEPAGE" />
          </titlestmt>
        </filedesc>
      </eadheader>
      <frontmatter>
        <titlepage>
          <xsl:apply-templates mode="TITLEPAGE" select="*" />
        </titlepage>
      </frontmatter>
      <archdesc level="collection">
        <did>
          <unittitle label="Title"><xsl:apply-templates mode="TITLEPROPER" select="TITLEPROPER/*" /></unittitle>
          <xsl:call-template name="physdesc">
            <xsl:with-param name="parent" select="current()" />
          </xsl:call-template>
        </did>
        <descgrp type="admininfo">
          <xsl:apply-templates select="*" mode="ADMININFO" />
        </descgrp>
        <xsl:apply-templates select="*" mode="ARCHDESC" />
        <dsc>
          <xsl:apply-templates select="*" mode="DSC" />
        </dsc>
      </archdesc>
    </ead>
  </xsl:template>

  <xsl:template match="*" mode="#all" priority="-1" />

  <xsl:template match="SERIES" mode="DSC">
    <c01 level="series">
      <did>
        <xsl:apply-templates select="*" mode="DID" />
      </did>
      <xsl:apply-templates select="*" mode="C01" />
    </c01>
  </xsl:template>
  
  <xsl:template match="ITEM" mode="DSC">
    <c01 level="item">
      <did>
        <xsl:apply-templates select="*" mode="DID" />
        <xsl:call-template name="physdesc">
          <xsl:with-param name="parent" select="current()" />
        </xsl:call-template>
      </did>
      <xsl:apply-templates select="*" mode="C01" />
    </c01>
  </xsl:template>
  
  <xsl:template match="FILE" mode="DSC">
    <c01 level="file">
      <did>
        <xsl:apply-templates select="*" mode="DID" />
        <xsl:call-template name="physdesc">
          <xsl:with-param name="parent" select="current()" />
        </xsl:call-template>
      </did>
      <xsl:apply-templates select="*" mode="C01" />
    </c01>
  </xsl:template>
  
  <xsl:template match="SUBSERIES" mode="C01">
    <c02 level="subseries">
      <did>
        <xsl:apply-templates select="*" mode="DID" />
        <xsl:call-template name="physdesc">
          <xsl:with-param name="parent" select="current()" />
        </xsl:call-template>
      </did>
      <xsl:apply-templates select="*" mode="C02" />
    </c02>
  </xsl:template>
  
  <xsl:template match="ITEM" mode="C01">
    <c02 level="item">
      <did>
        <xsl:apply-templates select="*" mode="DID" />
        <xsl:call-template name="physdesc">
          <xsl:with-param name="parent" select="current()" />
        </xsl:call-template>
      </did>
      <xsl:apply-templates select="*" mode="C02" />
    </c02>
  </xsl:template>
  
  <xsl:template match="FILE" mode="C01">
    <c02 level="file">
      <did>
        <xsl:apply-templates select="*" mode="DID" />
        <xsl:call-template name="physdesc">
          <xsl:with-param name="parent" select="current()" />
        </xsl:call-template>
      </did>
      <xsl:apply-templates select="*" mode="C02" />
    </c02>
  </xsl:template>

  <xsl:template name="physdesc">
    <xsl:param name="parent" required="yes" />
    <xsl:variable name="content"><xsl:apply-templates select="current()/*" mode="PHYSDESC"/></xsl:variable>
    <xsl:if test="$content != ''">
      <physdesc>
        <xsl:copy-of select="$content" />
      </physdesc>
    </xsl:if>
  </xsl:template>

  <xsl:template match="UNITTITLE" mode="DID">
    <unittitle><xsl:apply-templates /></unittitle>
  </xsl:template>

  <xsl:template match="EXTENT" mode="PHYSDESC">
    <extent><xsl:apply-templates select="*"/></extent>
  </xsl:template>
  <xsl:template match="PHYSFACET" mode="PHYSDESC">
    <physfacet><xsl:apply-templates select="*"/></physfacet>
  </xsl:template>
  
  
  
  <xsl:template match="SCOPECONTENT" mode="ARCHDESC C01 C02 C03">
    <scopecontent>
      <xsl:apply-templates select="*" mode="SCOPECONTENT" />
    </scopecontent>
  </xsl:template>
  
  <xsl:template match="BOX" mode="DID">
    <container label="Text" type="box"><xsl:apply-templates select="*" mode="BOX" /></container>
  </xsl:template>
  
  <xsl:template match="BOX_FOLDER" mode="DID">
    <container label="Text" type="box-folder"><xsl:apply-templates select="*" mode="BOX_FOLDER" /></container>
  </xsl:template>
  
  <xsl:template match="FOLDER" mode="DID">
    <container label="Text" type="folder"><xsl:apply-templates select="*" mode="FOLDER" /></container>
  </xsl:template>

  <xsl:template match="BIOGHIST" mode="ARCHDESC">
    <bioghist>
      <xsl:apply-templates select="*" mode="BIOGHIST" />
    </bioghist>
  </xsl:template>
  
  <xsl:template match="PHYSTECH" mode="ARCHDESC C01 C02 C03">
    <phystech>
      <xsl:apply-templates select="*" mode="PHYSTECH" />
    </phystech>
  </xsl:template>

  <xsl:template match="ARRANGMENT" mode="ARCHDESC">
    <arrangment>
      <xsl:apply-templates select="*" mode="ARRANGEMENT" />
    </arrangment>
  </xsl:template>

  <xsl:template match="ACCESSRESTRICT" mode="ADMININFO">
    <accessrestrict>
      <xsl:apply-templates select="*" mode="ACCESSRESTRICT" />
    </accessrestrict>
  </xsl:template>

  <xsl:template match="USERESTRICT" mode="ADMININFO">
    <userestrict>
      <xsl:apply-templates select="*" mode="USERESTRICT" />
    </userestrict>
  </xsl:template>

  <xsl:template match="PREFERCITE" mode="ADMININFO">
    <prefercite>
      <xsl:apply-templates select="*" mode="PREFERCITE" />
    </prefercite>
  </xsl:template>

  <xsl:template match="ACQINFO" mode="ADMININFO">
    <acqinfo>
      <xsl:apply-templates select="*" mode="ACQINFO" />
    </acqinfo>
  </xsl:template>

  <xsl:template match="HEAD" mode="#all">
    <head><xsl:apply-templates select="*" /></head>
  </xsl:template>

  <xsl:template match="PARAGRAPH" mode="#all">
    <p><xsl:apply-templates  select="*" /></p>
  </xsl:template>


  <xsl:template match="TITLEPROPER" mode="TITLEPAGE TITLEPROPER">
    <titleproper>
      <xsl:apply-templates mode="TITLEPROPER" select="*" />
    </titleproper>
  </xsl:template>

  <xsl:template match="SUBTITLE" mode="TITLEPAGE">
    <subtitle>
      <xsl:apply-templates select="//MSS_NUM" mode="SUBTITLE" />
      <xsl:apply-templates select="*" mode="SUBTITLE" />
    </subtitle>
  </xsl:template>

  <xsl:template match="MSS_NUM" mode="SUBTITLE">
    <num type="Accession number">
      <xsl:apply-templates />
    </num>
  </xsl:template>

  <xsl:template match="DATE" mode="TITLEPROPER">
    <date><xsl:apply-templates /></date>
  </xsl:template>
  
  <xsl:template match="UNITDATE" mode="DID UNITTITLE">
    <xsl:variable name="text"><xsl:apply-templates select="*"/></xsl:variable>
    <xsl:analyze-string select="$text" regex="^([\d]+)[;-].*(\d\d\d\d)$">
      <xsl:matching-substring>
        <xsl:variable name="start" select="number(regex-group(1))"/>
        <xsl:variable name="end" select="number(regex-group(2))"/>
        <unitdate>
          <xsl:attribute name="normal" select="concat($start, '/', $end)" />
          <xsl:attribute name="type">inclusive</xsl:attribute>
          <xsl:value-of select="$text" />
        </unitdate>
      </xsl:matching-substring>
      <xsl:non-matching-substring>
        <xsl:analyze-string select="$text" regex="^([\d]+)$">
          <xsl:matching-substring>
            <xsl:variable name="point" select="number(regex-group(1))"/>
            <unitdate>
              <xsl:attribute name="normal" select="$point" />
              <xsl:attribute name="type">inclusive</xsl:attribute>
              <xsl:value-of select="$text" />
            </unitdate>
          </xsl:matching-substring>
          <xsl:non-matching-substring>
            <unitdate type="inclusive"><xsl:value-of select="$text"></xsl:value-of></unitdate>
          </xsl:non-matching-substring>
        </xsl:analyze-string>
      </xsl:non-matching-substring>
    </xsl:analyze-string>
  </xsl:template>

  <xsl:template match="TEXT" mode="#all">
    <xsl:apply-templates select="*" />
  </xsl:template>

  <xsl:template match="span[@style='']" mode="#all">
    <xsl:value-of select="text()" />
  </xsl:template>
  
  <xsl:template match="span[@style and not(@style='')]" mode="#all">
    <emph><xsl:attribute name="render" select="@style"/><xsl:value-of select="text()" /></emph>
  </xsl:template>

</xsl:stylesheet>