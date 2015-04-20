<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0">



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
            <titleproper><xsl:value-of select="TITLEPROPER" /></titleproper>
          </titlestmt>
        </filedesc>
      </eadheader>
      <frontmatter>
        <titlepage>
          <titleproper><xsl:value-of select="TITLEPROPER" /></titleproper>
          <subtitle><num type="Accession number"><xsl:value-of select="MSS_NUM"></xsl:value-of></num>
          </subtitle>
        </titlepage>
      </frontmatter>
    </ead>
  </xsl:template>

  <xsl:template match="TITLEPROPER" mode="eadheader">

  </xsl:template>
</xsl:stylesheet>