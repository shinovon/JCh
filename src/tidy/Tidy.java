/*
 * @(#)Tidy.java   1.11 2000/08/16
 *
 */

/*
  HTML parser and pretty printer

  Copyright (c) 1998-2000 World Wide Web Consortium (Massachusetts
  Institute of Technology, Institut National de Recherche en
  Informatique et en Automatique, Keio University). All Rights
  Reserved.

  Contributing Author(s):

     Dave Raggett <dsr@w3.org>
     Andy Quick <ac.quick@sympatico.ca> (translation to Java)

  The contributing author(s) would like to thank all those who
  helped with testing, bug fixes, and patience.  This wouldn't
  have been possible without all of you.

  COPYRIGHT NOTICE:
 
  This software and documentation is provided "as is," and
  the copyright holders and contributing author(s) make no
  representations or warranties, express or implied, including
  but not limited to, warranties of merchantability or fitness
  for any particular purpose or that the use of the software or
  documentation will not infringe any third party patents,
  copyrights, trademarks or other rights. 

  The copyright holders and contributing author(s) will not be
  liable for any direct, indirect, special or consequential damages
  arising out of any use of the software or documentation, even if
  advised of the possibility of such damage.

  Permission is hereby granted to use, copy, modify, and distribute
  this source code, or portions hereof, documentation and executables,
  for any purpose, without fee, subject to the following restrictions:

  1. The origin of this source code must not be misrepresented.
  2. Altered versions must be plainly marked as such and must
     not be misrepresented as being the original source.
  3. This Copyright notice may not be removed or altered from any
     source or altered source distribution.
 
  The copyright holders and contributing author(s) specifically
  permit, without fee, and encourage the use of this source code
  as a component for supporting the Hypertext Markup Language in
  commercial products. If you use this source code in a product,
  acknowledgment is not required but would be appreciated.
*/

package tidy;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 *
 * <p>HTML parser and pretty printer</p>
 *
 * <p>
 * (c) 1998-2000 (W3C) MIT, INRIA, Keio University
 * See Tidy.java for the copyright notice.
 * Derived from <a href="http://www.w3.org/People/Raggett/tidy">
 * HTML Tidy Release 4 Aug 2000</a>
 * </p>
 *
 * <p>
 * Copyright (c) 1998-2000 World Wide Web Consortium (Massachusetts
 * Institute of Technology, Institut National de Recherche en
 * Informatique et en Automatique, Keio University). All Rights
 * Reserved.
 * </p>
 *
 * <p>
 * Contributing Author(s):<br>
 *    <a href="mailto:dsr@w3.org">Dave Raggett</a><br>
 *    <a href="mailto:ac.quick@sympatico.ca">Andy Quick</a> (translation to Java)
 * </p>
 *
 * <p>
 * The contributing author(s) would like to thank all those who
 * helped with testing, bug fixes, and patience.  This wouldn't
 * have been possible without all of you.
 * </p>
 *
 * <p>
 * COPYRIGHT NOTICE:<br>
 * 
 * This software and documentation is provided "as is," and
 * the copyright holders and contributing author(s) make no
 * representations or warranties, express or implied, including
 * but not limited to, warranties of merchantability or fitness
 * for any particular purpose or that the use of the software or
 * documentation will not infringe any third party patents,
 * copyrights, trademarks or other rights. 
 * </p>
 *
 * <p>
 * The copyright holders and contributing author(s) will not be
 * liable for any direct, indirect, special or consequential damages
 * arising out of any use of the software or documentation, even if
 * advised of the possibility of such damage.
 * </p>
 *
 * <p>
 * Permission is hereby granted to use, copy, modify, and distribute
 * this source code, or portions hereof, documentation and executables,
 * for any purpose, without fee, subject to the following restrictions:
 * </p>
 *
 * <p>
 * <ol>
 * <li>The origin of this source code must not be misrepresented.</li>
 * <li>Altered versions must be plainly marked as such and must
 * not be misrepresented as being the original source.</li>
 * <li>This Copyright notice may not be removed or altered from any
 * source or altered source distribution.</li>
 * </ol>
 * </p>
 *
 * <p>
 * The copyright holders and contributing author(s) specifically
 * permit, without fee, and encourage the use of this source code
 * as a component for supporting the Hypertext Markup Language in
 * commercial products. If you use this source code in a product,
 * acknowledgment is not required but would be appreciated.
 * </p>
 *
 * @author  Dave Raggett <dsr@w3.org>
 * @author  Andy Quick <ac.quick@sympatico.ca> (translation to Java)
 * @version 1.0, 1999/05/22
 * @version 1.0.1, 1999/05/29
 * @version 1.1, 1999/06/18 Java Bean
 * @version 1.2, 1999/07/10 Tidy Release 7 Jul 1999
 * @version 1.3, 1999/07/30 Tidy Release 26 Jul 1999
 * @version 1.4, 1999/09/04 DOM support
 * @version 1.5, 1999/10/23 Tidy Release 27 Sep 1999
 * @version 1.6, 1999/11/01 Tidy Release 22 Oct 1999
 * @version 1.7, 1999/12/06 Tidy Release 30 Nov 1999
 * @version 1.8, 2000/01/22 Tidy Release 13 Jan 2000
 * @version 1.9, 2000/06/03 Tidy Release 30 Apr 2000
 * @version 1.10, 2000/07/22 Tidy Release 8 Jul 2000
 * @version 1.11, 2000/08/16 Tidy Release 4 Aug 2000
 *
 */

public class Tidy {

    static final long serialVersionUID = -2794371560623987718L;

    private boolean       initialized = false;
    private Configuration configuration = null;

    public Tidy()
    {
        init();
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }

    /**
     * Spaces - default indentation
     * @see tidy.Configuration#spaces
     */

    public void setSpaces(int spaces)
    {
        configuration.spaces = spaces;
    }

    public int getSpaces()
    {
        return configuration.spaces;
    }

    /**
     * Wraplen - default wrap margin
     * @see tidy.Configuration#wraplen
     */

    public void setWraplen(int wraplen)
    {
        configuration.wraplen = wraplen;
    }

    public int getWraplen()
    {
        return configuration.wraplen;
    }

    /**
     * CharEncoding
     * @see tidy.Configuration#CharEncoding
     */

    public void setCharEncoding(int charencoding)
    {
        configuration.CharEncoding = charencoding;
    }

    public int getCharEncoding()
    {
        return configuration.CharEncoding;
    }

    /**
     * Tabsize
     * @see tidy.Configuration#tabsize
     */

    public void setTabsize(int tabsize)
    {
        configuration.tabsize = tabsize;
    }

    public int getTabsize()
    {
        return configuration.tabsize;
    }

    /**
     * Errfile - file name to write errors to
     * @see tidy.Configuration#errfile
     */

    public void setErrfile(String errfile)
    {
        configuration.errfile = errfile;
    }

    public String getErrfile()
    {
        return configuration.errfile;
    }

    /**
     * Writeback - if true then output tidied markup
     * NOTE: this property is ignored when parsing from an InputStream.
     * @see tidy.Configuration#writeback
     */

    public void setWriteback(boolean writeback)
    {
        configuration.writeback = writeback;
    }

    public boolean getWriteback()
    {
        return configuration.writeback;
    }

    /**
     * OnlyErrors - if true normal output is suppressed
     * @see tidy.Configuration#OnlyErrors
     */

    public void setOnlyErrors(boolean OnlyErrors)
    {
        configuration.OnlyErrors = OnlyErrors;
    }

    public boolean getOnlyErrors()
    {
        return configuration.OnlyErrors;
    }

    /**
     * ShowWarnings - however errors are always shown
     * @see tidy.Configuration#ShowWarnings
     */

    public void setShowWarnings(boolean ShowWarnings)
    {
        configuration.ShowWarnings = ShowWarnings;
    }

    public boolean getShowWarnings()
    {
        return configuration.ShowWarnings;
    }

    /**
     * Quiet - no 'Parsing X', guessed DTD or summary
     * @see tidy.Configuration#Quiet
     */

    public void setQuiet(boolean Quiet)
    {
        configuration.Quiet = Quiet;
    }

    public boolean getQuiet()
    {
        return configuration.Quiet;
    }

    /**
     * IndentContent - indent content of appropriate tags
     * @see tidy.Configuration#IndentContent
     */

    public void setIndentContent(boolean IndentContent)
    {
        configuration.IndentContent = IndentContent;
    }

    public boolean getIndentContent()
    {
        return configuration.IndentContent;
    }

    /**
     * SmartIndent - does text/block level content effect indentation
     * @see tidy.Configuration#SmartIndent
     */

    public void setSmartIndent(boolean SmartIndent)
    {
        configuration.SmartIndent = SmartIndent;
    }

    public boolean getSmartIndent()
    {
        return configuration.SmartIndent;
    }

    /**
     * HideEndTags - suppress optional end tags
     * @see tidy.Configuration#HideEndTags
     */

    public void setHideEndTags(boolean HideEndTags)
    {
        configuration.HideEndTags = HideEndTags;
    }

    public boolean getHideEndTags()
    {
        return configuration.HideEndTags;
    }

    /**
     * XmlTags - treat input as XML
     * @see tidy.Configuration#XmlTags
     */

    public void setXmlTags(boolean XmlTags)
    {
        configuration.XmlTags = XmlTags;
    }

    public boolean getXmlTags()
    {
        return configuration.XmlTags;
    }

    /**
     * XmlOut - create output as XML
     * @see tidy.Configuration#XmlOut
     */

    public void setXmlOut(boolean XmlOut)
    {
        configuration.XmlOut = XmlOut;
    }

    public boolean getXmlOut()
    {
        return configuration.XmlOut;
    }

    /**
     * XHTML - output extensible HTML
     * @see tidy.Configuration#xHTML
     */

    public void setXHTML(boolean xHTML)
    {
        configuration.xHTML = xHTML;
    }

    public boolean getXHTML()
    {
        return configuration.xHTML;
    }

    /**
     * RawOut - avoid mapping values > 127 to entities
     * @see tidy.Configuration#RawOut
     */

    public void setRawOut(boolean RawOut)
    {
        configuration.RawOut = RawOut;
    }

    public boolean getRawOut()
    {
        return configuration.RawOut;
    }

    /**
     * UpperCaseTags - output tags in upper not lower case
     * @see tidy.Configuration#UpperCaseTags
     */

    public void setUpperCaseTags(boolean UpperCaseTags)
    {
        configuration.UpperCaseTags = UpperCaseTags;
    }

    public boolean getUpperCaseTags()
    {
        return configuration.UpperCaseTags;
    }

    /**
     * UpperCaseAttrs - output attributes in upper not lower case
     * @see tidy.Configuration#UpperCaseAttrs
     */

    public void setUpperCaseAttrs(boolean UpperCaseAttrs)
    {
        configuration.UpperCaseAttrs = UpperCaseAttrs;
    }

    public boolean getUpperCaseAttrs()
    {
        return configuration.UpperCaseAttrs;
    }

    /**
     * MakeClean - remove presentational clutter
     * @see tidy.Configuration#MakeClean
     */

    public void setMakeClean(boolean MakeClean)
    {
        configuration.MakeClean = MakeClean;
    }

    public boolean getMakeClean()
    {
        return configuration.MakeClean;
    }

    /**
     * BreakBeforeBR - o/p newline before &lt;br&gt; or not?
     * @see tidy.Configuration#BreakBeforeBR
     */

    public void setBreakBeforeBR(boolean BreakBeforeBR)
    {
        configuration.BreakBeforeBR = BreakBeforeBR;
    }

    public boolean getBreakBeforeBR()
    {
        return configuration.BreakBeforeBR;
    }

    /**
     * BurstSlides - create slides on each h2 element
     * @see tidy.Configuration#BurstSlides
     */

    public void setBurstSlides(boolean BurstSlides)
    {
        configuration.BurstSlides = BurstSlides;
    }

    public boolean getBurstSlides()
    {
        return configuration.BurstSlides;
    }

    /**
     * NumEntities - use numeric entities
     * @see tidy.Configuration#NumEntities
     */

    public void setNumEntities(boolean NumEntities)
    {
        configuration.NumEntities = NumEntities;
    }

    public boolean getNumEntities()
    {
        return configuration.NumEntities;
    }

    /**
     * QuoteMarks - output " marks as &amp;quot;
     * @see tidy.Configuration#QuoteMarks
     */

    public void setQuoteMarks(boolean QuoteMarks)
    {
        configuration.QuoteMarks = QuoteMarks;
    }

    public boolean getQuoteMarks()
    {
        return configuration.QuoteMarks;
    }

    /**
     * QuoteNbsp - output non-breaking space as entity
     * @see tidy.Configuration#QuoteNbsp
     */

    public void setQuoteNbsp(boolean QuoteNbsp)
    {
        configuration.QuoteNbsp = QuoteNbsp;
    }

    public boolean getQuoteNbsp()
    {
        return configuration.QuoteNbsp;
    }

    /**
     * QuoteAmpersand - output naked ampersand as &amp;
     * @see tidy.Configuration#QuoteAmpersand
     */

    public void setQuoteAmpersand(boolean QuoteAmpersand)
    {
        configuration.QuoteAmpersand = QuoteAmpersand;
    }

    public boolean getQuoteAmpersand()
    {
        return configuration.QuoteAmpersand;
    }

    /**
     * WrapAttVals - wrap within attribute values
     * @see tidy.Configuration#WrapAttVals
     */

    public void setWrapAttVals(boolean WrapAttVals)
    {
        configuration.WrapAttVals = WrapAttVals;
    }

    public boolean getWrapAttVals()
    {
        return configuration.WrapAttVals;
    }

    /**
     * WrapScriptlets - wrap within JavaScript string literals
     * @see tidy.Configuration#WrapScriptlets
     */

    public void setWrapScriptlets(boolean WrapScriptlets)
    {
        configuration.WrapScriptlets = WrapScriptlets;
    }

    public boolean getWrapScriptlets()
    {
        return configuration.WrapScriptlets;
    }

    /**
     * WrapSection - wrap within &lt;![ ... ]&gt; section tags
     * @see tidy.Configuration#WrapSection
     */

    public void setWrapSection(boolean WrapSection)
    {
        configuration.WrapSection = WrapSection;
    }

    public boolean getWrapSection()
    {
        return configuration.WrapSection;
    }

    /**
     * AltText - default text for alt attribute
     * @see tidy.Configuration#altText
     */

    public void setAltText(String altText)
    {
        configuration.altText = altText;
    }

    public String getAltText()
    {
        return configuration.altText;
    }

    /**
     * Slidestyle - style sheet for slides
     * @see tidy.Configuration#slidestyle
     */

    public void setSlidestyle(String slidestyle)
    {
        configuration.slidestyle = slidestyle;
    }

    public String getSlidestyle()
    {
        return configuration.slidestyle;
    }

    /**
     * XmlPi - add &lt;?xml?&gt; for XML docs
     * @see tidy.Configuration#XmlPi
     */

    public void setXmlPi(boolean XmlPi)
    {
        configuration.XmlPi = XmlPi;
    }

    public boolean getXmlPi()
    {
        return configuration.XmlPi;
    }

    /**
     * DropFontTags - discard presentation tags
     * @see tidy.Configuration#DropFontTags
     */

    public void setDropFontTags(boolean DropFontTags)
    {
        configuration.DropFontTags = DropFontTags;
    }

    public boolean getDropFontTags()
    {
        return configuration.DropFontTags;
    }

    /**
     * DropEmptyParas - discard empty p elements
     * @see tidy.Configuration#DropEmptyParas
     */

    public void setDropEmptyParas(boolean DropEmptyParas)
    {
        configuration.DropEmptyParas = DropEmptyParas;
    }

    public boolean getDropEmptyParas()
    {
        return configuration.DropEmptyParas;
    }

    /**
     * FixComments - fix comments with adjacent hyphens
     * @see tidy.Configuration#FixComments
     */

    public void setFixComments(boolean FixComments)
    {
        configuration.FixComments = FixComments;
    }

    public boolean getFixComments()
    {
        return configuration.FixComments;
    }

    /**
     * WrapAsp - wrap within ASP pseudo elements
     * @see tidy.Configuration#WrapAsp
     */

    public void setWrapAsp(boolean WrapAsp)
    {
        configuration.WrapAsp = WrapAsp;
    }

    public boolean getWrapAsp()
    {
        return configuration.WrapAsp;
    }

    /**
     * WrapJste - wrap within JSTE pseudo elements
     * @see tidy.Configuration#WrapJste
     */

    public void setWrapJste(boolean WrapJste)
    {
        configuration.WrapJste = WrapJste;
    }

    public boolean getWrapJste()
    {
        return configuration.WrapJste;
    }

    /**
     * WrapPhp - wrap within PHP pseudo elements
     * @see tidy.Configuration#WrapPhp
     */

    public void setWrapPhp(boolean WrapPhp)
    {
        configuration.WrapPhp = WrapPhp;
    }

    public boolean getWrapPhp()
    {
        return configuration.WrapPhp;
    }

    /**
     * FixBackslash - fix URLs by replacing \ with /
     * @see tidy.Configuration#FixBackslash
     */

    public void setFixBackslash(boolean FixBackslash)
    {
        configuration.FixBackslash = FixBackslash;
    }

    public boolean getFixBackslash()
    {
        return configuration.FixBackslash;
    }

    /**
     * IndentAttributes - newline+indent before each attribute
     * @see tidy.Configuration#IndentAttributes
     */

    public void setIndentAttributes(boolean IndentAttributes)
    {
        configuration.IndentAttributes = IndentAttributes;
    }

    public boolean getIndentAttributes()
    {
        return configuration.IndentAttributes;
    }

    /**
     * LogicalEmphasis - replace i by em and b by strong
     * @see tidy.Configuration#LogicalEmphasis
     */

    public void setLogicalEmphasis(boolean LogicalEmphasis)
    {
        configuration.LogicalEmphasis = LogicalEmphasis;
    }

    public boolean getLogicalEmphasis()
    {
        return configuration.LogicalEmphasis;
    }

    /**
     * XmlPIs - if set to true PIs must end with ?>
     * @see tidy.Configuration#XmlPIs
     */

    public void setXmlPIs(boolean XmlPIs)
    {
        configuration.XmlPIs = XmlPIs;
    }

    public boolean getXmlPIs()
    {
        return configuration.XmlPIs;
    }

    /**
     * EncloseText - if true text at body is wrapped in &lt;p&gt;'s
     * @see tidy.Configuration#EncloseBodyText
     */

    public void setEncloseText(boolean EncloseText)
    {
        configuration.EncloseBodyText = EncloseText;
    }

    public boolean getEncloseText()
    {
        return configuration.EncloseBodyText;
    }

    /**
     * EncloseBlockText - if true text in blocks is wrapped in &lt;p&gt;'s
     * @see tidy.Configuration#EncloseBlockText
     */

    public void setEncloseBlockText(boolean EncloseBlockText)
    {
        configuration.EncloseBlockText = EncloseBlockText;
    }

    public boolean getEncloseBlockText()
    {
        return configuration.EncloseBlockText;
    }

    /**
     * KeepFileTimes - if true last modified time is preserved<br>
     * <b>this is NOT supported at this time.</b>
     * @see tidy.Configuration#KeepFileTimes
     */

    public void setKeepFileTimes(boolean KeepFileTimes)
    {
        configuration.KeepFileTimes = KeepFileTimes;
    }

    public boolean getKeepFileTimes()
    {
        return configuration.KeepFileTimes;
    }

    /**
     * Word2000 - draconian cleaning for Word2000
     * @see tidy.Configuration#Word2000
     */

    public void setWord2000(boolean Word2000)
    {
        configuration.Word2000 = Word2000;
    }

    public boolean getWord2000()
    {
        return configuration.Word2000;
    }

    /**
     * TidyMark - add meta element indicating tidied doc
     * @see tidy.Configuration#TidyMark
     */

    public void setTidyMark(boolean TidyMark)
    {
        configuration.TidyMark = TidyMark;
    }

    public boolean getTidyMark()
    {
        return configuration.TidyMark;
    }

    /**
     * XmlSpace - if set to yes adds xml:space attr as needed
     * @see tidy.Configuration#XmlSpace
     */

    public void setXmlSpace(boolean XmlSpace)
    {
        configuration.XmlSpace = XmlSpace;
    }

    public boolean getXmlSpace()
    {
        return configuration.XmlSpace;
    }

    /**
     * Emacs - if true format error output for GNU Emacs
     * @see tidy.Configuration#Emacs
     */

    public void setEmacs(boolean Emacs)
    {
        configuration.Emacs = Emacs;
    }

    public boolean getEmacs()
    {
        return configuration.Emacs;
    }

    /**
     * LiteralAttribs - if true attributes may use newlines
     * @see tidy.Configuration#LiteralAttribs
     */

    public void setLiteralAttribs(boolean LiteralAttribs)
    {
        configuration.LiteralAttribs = LiteralAttribs;
    }

    public boolean getLiteralAttribs()
    {
        return configuration.LiteralAttribs;
    }

    /**
     * first time initialization which should
     * precede reading the command line
     */

    private void init()
    {
        configuration = new Configuration();
        if (configuration == null) return;

        AttributeTable at = AttributeTable.getDefaultAttributeTable();
        if (at == null) return;
        TagTable tt = new TagTable();
        tt.setConfiguration(configuration);
        configuration.tt = tt;
        EntityTable et = EntityTable.getDefaultEntityTable();
        if (et == null) return;

        /* Unnecessary - same initial values in Configuration
        Configuration.XmlTags       = false;
        Configuration.XmlOut        = false;
        Configuration.HideEndTags   = false;
        Configuration.UpperCaseTags = false;
        Configuration.MakeClean     = false;
        Configuration.writeback     = false;
        Configuration.OnlyErrors    = false;
        */

        configuration.errfile = null;
        initialized = true;
    }
    

    public Node parse(String s)
    {
    	try {
			return parse(new ByteArrayInputStream(s.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }

    /**
     * Parses InputStream in and returns the root Node.
     * If out is non-null, pretty prints to OutputStream out.
     */

    public Node parse(InputStream in, OutputStream out)
    {
        Node document = null;

        try
        {
          document = parse(in);
        }
        catch (IOException e) {}

        return document;
    }


    /**
     * Internal routine that actually does the parsing.  The caller
     * can pass either an InputStream or file name.  If both are passed,
     * the file name is preferred.
     */

    private Node parse(InputStream in)
                  throws IOException
    {
        Lexer lexer;
        Node document = null;

        if (!initialized)
            return null;

        /* ensure config is self-consistent */
        configuration.adjust();

        

        if (in != null)
        {
            lexer = new Lexer(new StreamInImpl(in,
                                               configuration.CharEncoding,
                                               configuration.tabsize),
                              configuration);

            /*
              store pointer to lexer in input stream
              to allow character encoding errors to be
              reported
            */
            lexer.in.lexer = lexer;
            {

                document = ParserImpl.parseDocument(lexer);

                if (!document.checkNodeIntegrity())
                {
                    return null;
                }
            }

            // Try to close the InputStream but only if if we created it.

            {
                try
                {
                    in.close();
                }
                catch (IOException e ) {}
            }
        }
        return document;
    }



    public dom.Document parseDOM(String s)
    {
        Node document = parse(s);
        if (document != null)
            return (dom.Document)document.getAdapter();
        else
            return null;
    }
    /**
     * Parses InputStream in and returns a DOM Document node.
     * If out is non-null, pretty prints to OutputStream out.
     */

    public dom.Document parseDOM(InputStream in, OutputStream out)
    {
        Node document = parse(in, out);
        if (document != null)
            return (dom.Document)document.getAdapter();
        else
            return null;
    }

    /**
     * Creates an empty DOM Document.
     */

    public static dom.Document createEmptyDocument()
    {
        Node document = new Node(Node.RootNode, new byte[0], 0, 0);
        Node node = new Node(Node.StartTag, new byte[0], 0, 0, "html", new TagTable());
        if (document != null && node != null)
        {
            Node.insertNodeAtStart(document, node);
            return (dom.Document)document.getAdapter();
        } else {
            return null;
        }
    }
}
