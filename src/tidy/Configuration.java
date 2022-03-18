/*
 * @(#)Configuration.java   1.11 2000/08/16
 *
 */

package tidy;

/**
 *
 * Read configuration file and manage configuration properties.
 *
 * (c) 1998-2000 (W3C) MIT, INRIA, Keio University
 * See Tidy.java for the copyright notice.
 * Derived from <a href="http://www.w3.org/People/Raggett/tidy">
 * HTML Tidy Release 4 Aug 2000</a>
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
 */

/*
  Configuration files associate a property name with a value.
  The format is that of a Java .properties file.
*/

public class Configuration {

    /* character encodings */
    public static final int RAW         = 0;
    public static final int ASCII       = 1;
    public static final int LATIN1      = 2;
    public static final int UTF8        = 3;
    public static final int ISO2022     = 4;
    public static final int MACROMAN    = 5;

    /* mode controlling treatment of doctype */
    public static final int DOCTYPE_OMIT  = 0;
    public static final int DOCTYPE_AUTO  = 1;
    public static final int DOCTYPE_STRICT= 2;
    public static final int DOCTYPE_LOOSE = 3;
    public static final int DOCTYPE_USER  = 4;

    protected int spaces =  2;           /* default indentation */
    protected int wraplen = 68;          /* default wrap margin */
    protected int CharEncoding = UTF8;
    protected int tabsize = 4;

    protected int     docTypeMode = DOCTYPE_AUTO; /* see doctype property */
    protected String  altText = null;      /* default text for alt attribute */
    protected String  slidestyle = null;    /* style sheet for slides */
    protected String  docTypeStr = null;    /* user specified doctype */
    protected String  errfile = null;       /* file name to write errors to */
    protected boolean writeback = false;        /* if true then output tidied markup */

    protected boolean OnlyErrors = false;       /* if true normal output is suppressed */
    protected boolean ShowWarnings = true;      /* however errors are always shown */
    protected boolean Quiet = false;            /* no 'Parsing X', guessed DTD or summary */
    protected boolean IndentContent = false;    /* indent content of appropriate tags */
    protected boolean SmartIndent = false;      /* does text/block level content effect indentation */
    protected boolean HideEndTags = false;      /* suppress optional end tags */
    protected boolean XmlTags = false;          /* treat input as XML */
    protected boolean XmlOut = false;           /* create output as XML */
    protected boolean xHTML = false;            /* output extensible HTML */
    protected boolean XmlPi = false;             /* add <?xml?> for XML docs */
    protected boolean RawOut = false;           /* avoid mapping values > 127 to entities */
    protected boolean UpperCaseTags = false;    /* output tags in upper not lower case */
    protected boolean UpperCaseAttrs = false;   /* output attributes in upper not lower case */
    protected boolean MakeClean = false;        /* remove presentational clutter */
    protected boolean LogicalEmphasis = false;  /* replace i by em and b by strong */
    protected boolean DropFontTags = false;     /* discard presentation tags */
    protected boolean DropEmptyParas = true;    /* discard empty p elements */
    protected boolean FixComments = true;       /* fix comments with adjacent hyphens */
    protected boolean BreakBeforeBR = false;    /* o/p newline before <br> or not? */
    protected boolean BurstSlides = false;      /* create slides on each h2 element */
    protected boolean NumEntities = false;      /* use numeric entities */
    protected boolean QuoteMarks = false;       /* output " marks as &quot; */
    protected boolean QuoteNbsp = true;         /* output non-breaking space as entity */
    protected boolean QuoteAmpersand = true;    /* output naked ampersand as &amp; */
    protected boolean WrapAttVals = false;      /* wrap within attribute values */
    protected boolean WrapScriptlets = false;   /* wrap within JavaScript string literals */
    protected boolean WrapSection = true;       /* wrap within <![ ... ]> section tags */
    protected boolean WrapAsp = true;           /* wrap within ASP pseudo elements */
    protected boolean WrapJste = true;          /* wrap within JSTE pseudo elements */
    protected boolean WrapPhp = true;           /* wrap within PHP pseudo elements */
    protected boolean FixBackslash = true;      /* fix URLs by replacing \ with / */
    protected boolean IndentAttributes = false; /* newline+indent before each attribute */
    protected boolean XmlPIs = false;           /* if set to yes PIs must end with ?> */
    protected boolean XmlSpace = false;         /* if set to yes adds xml:space attr as needed */
    protected boolean EncloseBodyText = false;  /* if yes text at body is wrapped in <p>'s */
    protected boolean EncloseBlockText = false; /* if yes text in blocks is wrapped in <p>'s */
    protected boolean KeepFileTimes = true;     /* if yes last modied time is preserved */
    protected boolean Word2000 = false;         /* draconian cleaning for Word2000 */
    protected boolean TidyMark = true;          /* add meta element indicating tidied doc */
    protected boolean Emacs = false;            /* if true format error output for GNU Emacs */
    protected boolean LiteralAttribs = false;   /* if true attributes may use newlines */

    protected TagTable tt;                      /* TagTable associated with this Configuration */

    public Configuration()
    {
    }

    

    /* ensure that config is self consistent */
    public void adjust()
    {
        if (EncloseBlockText)
            EncloseBodyText = true;

        /* avoid the need to set IndentContent when SmartIndent is set */

        if (SmartIndent)
            IndentContent = true;

        /* disable wrapping */
        if (wraplen == 0)
            wraplen = 0x7FFFFFFF;

        /* Word 2000 needs o:p to be declared as inline */
        if (Word2000)
        {
            tt.defineInlineTag("o:p");
        }

        /* XHTML is written in lower case */
        if (xHTML)
        {
            XmlOut = true;
            UpperCaseTags = false;
            UpperCaseAttrs = false;
        }

        /* if XML in, then XML out */
        if (XmlTags)
        {
            XmlOut = true;
            XmlPIs = true;
        }

        /* XML requires end tags */
        if (XmlOut)
        {
            QuoteAmpersand = true;
            HideEndTags = false;
        }
    }

}
