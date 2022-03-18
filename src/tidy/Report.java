/*
 * @(#)Report.java   1.11 2000/08/16
 *
 */

package tidy;

/**
 *
 * Error/informational message reporter.
 *
 * You should only need to edit the file TidyMessages.properties
 * to localize HTML tidy.
 *
 * (c) 1998-2000 (W3C) MIT, INRIA, Keio University
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

public class Report {

    /* used to point to Web Accessibility Guidelines */
    public static final String ACCESS_URL  = "http://www.w3.org/WAI/GL";

    public static final String RELEASE_DATE = "4th August 2000";

    public static String currentFile; /* sasdjb 01May00 for GNU Emacs error parsing */

    /* error codes for entities */

    public static final short MISSING_SEMICOLON       = 1;
    public static final short UNKNOWN_ENTITY          = 2;
    public static final short UNESCAPED_AMPERSAND     = 3;

    /* error codes for element messages */

    public static final short MISSING_ENDTAG_FOR      = 1;
    public static final short MISSING_ENDTAG_BEFORE   = 2;
    public static final short DISCARDING_UNEXPECTED   = 3;
    public static final short NESTED_EMPHASIS         = 4;
    public static final short NON_MATCHING_ENDTAG     = 5;
    public static final short TAG_NOT_ALLOWED_IN      = 6;
    public static final short MISSING_STARTTAG        = 7;
    public static final short UNEXPECTED_ENDTAG       = 8;
    public static final short USING_BR_INPLACE_OF     = 9;
    public static final short INSERTING_TAG           = 10;
    public static final short SUSPECTED_MISSING_QUOTE = 11;
    public static final short MISSING_TITLE_ELEMENT   = 12;
    public static final short DUPLICATE_FRAMESET      = 13;
    public static final short CANT_BE_NESTED          = 14;
    public static final short OBSOLETE_ELEMENT        = 15;
    public static final short PROPRIETARY_ELEMENT     = 16;
    public static final short UNKNOWN_ELEMENT         = 17;
    public static final short TRIM_EMPTY_ELEMENT      = 18;
    public static final short COERCE_TO_ENDTAG        = 19;
    public static final short ILLEGAL_NESTING         = 20;
    public static final short NOFRAMES_CONTENT        = 21;
    public static final short CONTENT_AFTER_BODY      = 22;
    public static final short INCONSISTENT_VERSION    = 23;
    public static final short MALFORMED_COMMENT       = 24;
    public static final short BAD_COMMENT_CHARS       = 25;
    public static final short BAD_XML_COMMENT         = 26;
    public static final short BAD_CDATA_CONTENT       = 27;
    public static final short INCONSISTENT_NAMESPACE  = 28;
    public static final short DOCTYPE_AFTER_TAGS      = 29;
    public static final short MALFORMED_DOCTYPE       = 30;
    public static final short UNEXPECTED_END_OF_FILE  = 31;
    public static final short DTYPE_NOT_UPPER_CASE    = 32;
    public static final short TOO_MANY_ELEMENTS       = 33;

    /* error codes used for attribute messages */

    public static final short UNKNOWN_ATTRIBUTE       = 1;
    public static final short MISSING_ATTRIBUTE       = 2;
    public static final short MISSING_ATTR_VALUE      = 3;
    public static final short BAD_ATTRIBUTE_VALUE     = 4;
    public static final short UNEXPECTED_GT           = 5;
    public static final short PROPRIETARY_ATTR_VALUE  = 6;
    public static final short REPEATED_ATTRIBUTE      = 7;
    public static final short MISSING_IMAGEMAP        = 8;
    public static final short XML_ATTRIBUTE_VALUE     = 9;
    public static final short UNEXPECTED_QUOTEMARK    = 10;
    public static final short ID_NAME_MISMATCH        = 11;

    /* accessibility flaws */

    public static final short MISSING_IMAGE_ALT       = 1;
    public static final short MISSING_LINK_ALT        = 2;
    public static final short MISSING_SUMMARY         = 4;
    public static final short MISSING_IMAGE_MAP       = 8;
    public static final short USING_FRAMES            = 16;
    public static final short USING_NOFRAMES          = 32;

    /* presentation flaws */

    public static final short USING_SPACER            = 1;
    public static final short USING_LAYER             = 2;
    public static final short USING_NOBR              = 4;
    public static final short USING_FONT              = 8;
    public static final short USING_BODY              = 16;

    /* character encoding errors */
    public static final short WINDOWS_CHARS           = 1;
    public static final short NON_ASCII               = 2;
    public static final short FOUND_UTF16             = 4;

    public static void tag(Lexer lexer, Node tag)
    {
    }

    /* lexer is not defined when this is called */
    public static void unknownOption(String option)
    {
    }

    /* lexer is not defined when this is called */
    public static void badArgument(String option)
    {
    }


    public static void position(Lexer lexer)
    {
    }

    public static void encodingError(Lexer lexer, short code, int c)
    {
        lexer.warnings++;

        if (lexer.configuration.ShowWarnings)
        {
            position(lexer);

            if (code == WINDOWS_CHARS)
            {
                lexer.badChars |= WINDOWS_CHARS;
            }

        }
    }

    public static void entityError(Lexer lexer, short code, String entity, int c)
    {
        lexer.warnings++;

        if (lexer.configuration.ShowWarnings)
        {
            position(lexer);
        }
    }

    public static void attrError(Lexer lexer, Node node, String attr, short code)
    {
        lexer.warnings++;

        /* keep quiet after 6 errors */
        if (lexer.errors > 6)
            return;
    }

    public static void warning(Lexer lexer, Node element, Node node, short code)
    {

        lexer.warnings++;

        /* keep quiet after 6 errors */
        if (lexer.errors > 6)
            return;

    }

    public static void error(Lexer lexer, Node element, Node node, short code)
    {
        lexer.warnings++;

    }

    public static void errorSummary(Lexer lexer)
    {
        /* adjust badAccess to that its null if frames are ok */
        if ((lexer.badAccess & (USING_FRAMES | USING_NOFRAMES)) != 0)
        {
            if (!(((lexer.badAccess & USING_FRAMES) != 0) && ((lexer.badAccess & USING_NOFRAMES) == 0)))
                lexer.badAccess &= ~(USING_FRAMES | USING_NOFRAMES);
        }

    }

}
