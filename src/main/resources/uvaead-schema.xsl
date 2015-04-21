<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="2.0">
  <xsl:template match="DOCUMENT">
    <xsl:processing-instruction name="xml-model">href="http://text.lib.virginia.edu/dtd/eadVIVA/ead-ext.rng"
      type="application/xml"
      schematypens="http://relaxng.org/ns/structure/1.0"
      title="extended EAD relaxng profile"</xsl:processing-instruction>
    <ead xmlns="urn:isbn:1-931666-22-9">
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
        <descgrp type="admininfo">
          <xsl:apply-templates select="*" mode="ADMININFO" />
        </descgrp>
        <xsl:apply-templates select="*" mode="ARCHDESC" />
      </archdesc>
      <dsc>
        <xsl:apply-templates select="*" mode="DSC" />
      </dsc>
    </ead>
  </xsl:template>

  <xsl:template match="*" mode="#all" priority="-1" />

  <xsl:template match="SERIES" mode="DSC">
    <c01 level="series">
      <xsl:apply-templates select="*" mode="C01" />
    </c01>
  </xsl:template>
  
  <xsl:template match="ITEM" mode="DSC">
    <c01 level="item">
      <xsl:apply-templates select="*" mode="C01" />
    </c01>
  </xsl:template>
  
  <xsl:template match="SUBSERIES" mode="C01">
    <c02 level="subseries">
      <xsl:apply-templates select="*" mode="C02" />
    </c02>
  </xsl:template>
  
  <xsl:template match="ITEM" mode="C01">
    <c02 level="item">
      <xsl:apply-templates select="*" mode="C02" />
    </c02>
  </xsl:template>

  <xsl:template match="SCOPECONTENT" mode="ARCHDESC">
    <scopecontent>
      <xsl:apply-templates select="*" mode="SCOPECONTENT" />
    </scopecontent>
  </xsl:template>
  
  <xsl:template match="BIOGHIST" mode="ARCHDESC">
    <scopecontent>
      <xsl:apply-templates select="*" mode="BIOGHIST" />
    </scopecontent>
  </xsl:template>
  
  <xsl:template match="ARRANGMENT" mode="ARCHDESC">
    <scopecontent>
      <xsl:apply-templates select="*" mode="ARRANGEMENT" />
    </scopecontent>
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
    <head><xsl:apply-templates /></head>
  </xsl:template>
  
  <xsl:template match="PARAGRAPH" mode="#all">
    <p><xsl:apply-templates /></p>
  </xsl:template>
  

  <xsl:template match="TITLEPROPER" mode="TITLEPAGE TITLEPROPER">
    <titleproper>
      <xsl:apply-templates mode="TITLEPROPER" />
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
  
  <xsl:template match="TEXT" mode="#all">
    <xsl:apply-templates select="*" />
  </xsl:template>
  
  <xsl:template match="span[@type='text']" mode="#all">
    <xsl:value-of select="text()" />
  </xsl:template>
  
  <xsl:template match="span[@type='italic']" mode="#all">
    <xsl:value-of select="text()" />
  </xsl:template>
  
  <xsl:template match="span[@type='bold']" mode="#all">
    <xsl:value-of select="text()" />
  </xsl:template>
  
</xsl:stylesheet>