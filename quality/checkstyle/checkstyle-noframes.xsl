<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" indent="yes"
        doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" 
        doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"/>
  <xsl:strip-space elements="*"/>
	<xsl:decimal-format decimal-separator="." grouping-separator=","/>
	<xsl:template match="checkstyle">
		<html>
			<head>
			  <title>CheckStyle Audit</title>
				<style type="text/css">.bannercell { border: 0px; padding: 0px; } body { margin-left: 10; margin-right: 10; font:normal 80% arial,helvetica,sanserif; background-color:#FFFFFF; color:#000000; } .a td { background: #efefef; } .b td { background: #fff; } th, td { text-align: left; vertical-align: top; } th { font-weight:bold; background: #ccc; color: black; } table, th, td { font-size:100%; border: none } table.log tr td, tr th { } h2 { font-weight:bold; font-size:140%; margin-bottom: 5; } h3 { font-size:100%; font-weight:bold; background: #525D76; color: white; text-decoration: none; padding: 5px; margin-right: 2px; margin-left: 2px; margin-bottom: 0; } </style>
			</head>
			<body>
				<a name="top"/>
				<table border="0" cellpadding="0" cellspacing="0" width="100%">
					<tr>
						<td class="bannercell" rowspan="2"></td>
						<td class="text-align:right">
							<h2>CheckStyle Audit</h2>
						</td>
					</tr>
					<tr>
						<td class="text-align:right">
							Designed for use with <a href="http://checkstyle.sourceforge.net/">CheckStyle</a> and <a href="http://jakarta.apache.org">Ant</a>.
						</td>
					</tr>
				</table>
				<hr size="1"/>
				<xsl:call-template name="summary"/>
				<h3>Files</h3>
				<table class="log" border="0" cellpadding="5" cellspacing="2" width="100%">
					<tr>
						<th>Name</th>
						<th>Errors</th>
					</tr>
					<xsl:apply-templates select="*" mode="file-summary">
						<xsl:sort select="@name"/>
					</xsl:apply-templates>
				</table>
				<xsl:apply-templates select="*" mode="file-details">
					<xsl:sort select="@name"/>
				</xsl:apply-templates>
				<hr size="1" width="100%" align="left"/>
			</body>
		</html>
	</xsl:template>
	<xsl:template name="summary">
		<h3>Summary</h3>
		<xsl:variable name="fileCount" select="count(file)"/>
		<xsl:variable name="errorCount" select="count(file/error)"/>
		<table class="log" border="0" cellpadding="5" cellspacing="2" width="100%">
			<tr>
				<th>Files</th>
				<th>Errors</th>
			</tr>
			<tr>
				<xsl:call-template name="alternated-row"/>
				<td>
					<xsl:value-of select="$fileCount"/>
				</td>
				<td>
					<xsl:value-of select="$errorCount"/>
				</td>
			</tr>
		</table>
	</xsl:template>
	<xsl:template match="file[error]" mode="file-summary">
		<xsl:if test="not(@name = preceding::file[error]/@name)">
			<tr>
				<xsl:variable name="in" select="@name"/>
				<xsl:attribute name="class">a</xsl:attribute>
				<td>
					<a href="#f-{translate(translate(@name, '/', '-'), '\', '-')}"><xsl:value-of select="@name"/></a>
				</td>
				<td>
					<xsl:value-of select="count(//file[@name=$in]/error)"/>
				</td>
			</tr>
		</xsl:if>
	</xsl:template>
	<xsl:template match="file[error]" mode="file-details">
		<xsl:if test="not(@name = preceding::file[error]/@name)">
			<a id="f-{translate(translate(@name, '/', '-'), '\', '-')}"/>
			<h3>File&#xa0;<xsl:value-of select="@name"/></h3>
			<table class="log" border="0" cellpadding="5" cellspacing="2" width="100%">
				<tr>
					<th>Error Description</th>
					<th>Line</th>
				</tr>
				<xsl:call-template name="show-errors">
					<xsl:with-param select="@name" name="input"/>
				</xsl:call-template>
			</table>
      <a href="#top">Back to top</a>
		</xsl:if>
	</xsl:template>
	<xsl:template name="show-errors">
		<xsl:param name="input"/>
		<xsl:apply-templates select="//file[@name=$input]/error" mode="show-message"/>
	</xsl:template>
	<xsl:template match="error" mode="show-message">
		<tr>
			<xsl:call-template name="alternated-row"/>
			<td>
				<xsl:choose>
					<xsl:when test="@source = 'com.puppycrawl.tools.checkstyle.checks.naming.MethodNameCheck'">
						Method name is invalid: 						<xsl:value-of select="@message"/>
					</xsl:when>
					<xsl:when test="@source = 'com.puppycrawl.tools.checkstyle.checks.naming.PackageNameCheck'">
						Package name is invalid: 						<xsl:value-of select="@message"/>
					</xsl:when>
					<xsl:when test="@source = 'com.puppycrawl.tools.checkstyle.checks.naming.TypeNameCheck'">
						Type name is invalid: 						<xsl:value-of select="@message"/>
					</xsl:when>
					<xsl:when test="@source = 'com.puppycrawl.tools.checkstyle.checks.naming.MemberNameCheck'">
						Member name is invalid: 						<xsl:value-of select="@message"/>
					</xsl:when>
					<xsl:when test="@source = 'com.puppycrawl.tools.checkstyle.checks.naming.ConstantNameCheck'">
						Constant name is invalid: 						<xsl:value-of select="@message"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="@message"/>
					</xsl:otherwise>
				</xsl:choose>
			</td>
			<td>
				<xsl:value-of select="@line"/>
			</td>
		</tr>
	</xsl:template>
	<xsl:template name="alternated-row">
		<xsl:attribute name="class">
			<xsl:if test="position() mod 2 = 1">a</xsl:if>
			<xsl:if test="position() mod 2 = 0">b</xsl:if>
		</xsl:attribute>
	</xsl:template>
</xsl:stylesheet>
