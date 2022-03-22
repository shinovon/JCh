/*
 * @(#)ParserImpl.java   1.11 2000/08/16
 *
 */

package tidy;

/**
 *
 * HTML Parser implementation
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

public class ParserImpl {

    //private static int SeenBodyEndTag;  /* AQ: moved into lexer structure */

    private static void parseTag(Lexer lexer, Node node, short mode)
    {
        // Local fix by GLP 2000-12-21.  Need to reset insertspace if this 
        // is both a non-inline and empty tag (base, link, meta, isindex, hr, area).
        // Remove this code once the fix is made in Tidy.

/******  (Original code follows)
        if ((node.tag.model & Dict.CM_EMPTY) != 0)
        {
            lexer.waswhite = false;
            return;
        }
        else if (!((node.tag.model & Dict.CM_INLINE) != 0))
            lexer.insertspace = false;
*******/
    	if(node.tag == null) return;
        if (!((node.tag.model & Dict.CM_INLINE) != 0))
            lexer.insertspace = false;

        if ((node.tag.model & Dict.CM_EMPTY) != 0)
        {
            lexer.waswhite = false;
            return;
        }

        if (node.tag.parser == null || node.type == Node.StartEndTag)
            return;

        node.tag.parser.parse(lexer, node, mode);
    }

    private static void moveToHead(Lexer lexer, Node element, Node node)
    {
        Node head;
        TagTable tt = lexer.configuration.tt;


        if (node.type == Node.StartTag || node.type == Node.StartEndTag)
        {
            Report.warning(lexer, element, node, Report.TAG_NOT_ALLOWED_IN);

            while (element.tag != tt.tagHtml)
                element = element.parent;

            for (head = element.content; head != null; head = head.next)
            {
                if (head.tag == tt.tagHead)
                {
                    Node.insertNodeAtEnd(head, node);
                    break;
                }
            }

            if (node.tag.parser != null)
                parseTag(lexer, node, Lexer.IgnoreWhitespace);
        }
        else
        {
            Report.warning(lexer, element, node, Report.DISCARDING_UNEXPECTED);
        }
    }

    public static class ParseHTML implements Parser {

        public void parse( Lexer lexer, Node html, short mode )
        {
            Node node, head;

            lexer.configuration.XmlTags = false;
            lexer.seenBodyEndTag = 0;
            TagTable tt = lexer.configuration.tt;

            for (;;)
            {
                node = lexer.getToken(Lexer.IgnoreWhitespace);

                if (node == null)
                {
                    node = lexer.inferredTag("head");
                    break;
                }

                if (node.tag == tt.tagHead)
                    break;

                if (node.tag == html.tag && node.type == Node.EndTag)
                {
                    Report.warning(lexer, html, node, Report.DISCARDING_UNEXPECTED);
                    continue;
                }

                /* deal with comments etc. */
                if (Node.insertMisc(html, node))
                    continue;

                lexer.ungetToken();
                node = lexer.inferredTag("head");
                break;
            }

            head = node;
            Node.insertNodeAtEnd(html, head);
           // getParseHead().parse(lexer, head, mode);

            for (;;)
            {
                node = lexer.getToken(Lexer.IgnoreWhitespace);

                if (node == null)
                {
                   node = lexer.inferredTag("body");

                    return;
                }

                /* robustly handle html tags */
                if (node.tag == html.tag)
                {
                    if (node.type != Node.StartTag)
                        Report.warning(lexer, html, node, Report.DISCARDING_UNEXPECTED);

                    continue;
                }

                /* deal with comments etc. */
                if (Node.insertMisc(html, node))
                    continue;

                /* if frameset document coerce <body> to <noframes> */
                if (node.tag == tt.tagBody)
                {
                    if (node.type != Node.StartTag)
                    {
                        Report.warning(lexer, html, node, Report.DISCARDING_UNEXPECTED);
                        continue;
                    }
                    break;  /* to parse body */
                }

                if (node.type == Node.StartTag || node.type == Node.StartEndTag)
                {
                    if (node.tag != null && (node.tag.model & Dict.CM_HEAD) != 0)
                    {
                        moveToHead(lexer, html, node);
                        continue;
                    }
                }

                lexer.ungetToken();
                node = lexer.inferredTag("body");
                break;
            }

            /* node must be body */

            Node.insertNodeAtEnd(html, node);
            parseTag(lexer, node, mode);
        }

    };

    public static class ParseBody implements Parser {

        public void parse( Lexer lexer, Node body, short mode )
        {
            Node node;
            boolean checkstack, iswhitenode;

            mode = Lexer.IgnoreWhitespace;
            checkstack = true;
            TagTable tt = lexer.configuration.tt;

            while (true)
            {
                node = lexer.getToken(mode);
                if (node == null) break;
                if (node.tag == body.tag && node.type == Node.EndTag)
                {
                    body.closed = true;
                    Node.trimSpaces(lexer, body);
                    lexer.seenBodyEndTag = 1;
                    mode = Lexer.IgnoreWhitespace;

                    continue;
                }

                iswhitenode = false;

                if (node.type == Node.TextNode &&
                       node.end <= node.start + 1 &&
                       node.textarray[node.start] == (byte)' ')
                    iswhitenode = true;

                /* deal with comments etc. */
                if (Node.insertMisc(body, node))
                    continue;

                if (lexer.seenBodyEndTag == 1 && !iswhitenode)
                {
                    ++lexer.seenBodyEndTag;
                    Report.warning(lexer, body, node, Report.CONTENT_AFTER_BODY);
                }

                /* mixed content model permits text */
                if (node.type == Node.TextNode)
                {
                    if (iswhitenode && mode == Lexer.IgnoreWhitespace)
                    {
                        continue;
                    }

                    if (lexer.configuration.EncloseBodyText && !iswhitenode)
                    {
                        Node para;
                
                        lexer.ungetToken();
                        para = lexer.inferredTag("p");
                        Node.insertNodeAtEnd(body, para);
                        parseTag(lexer, para, mode);
                        mode = Lexer.MixedContent;
                        continue;
                    }

                    if (checkstack)
                    {
                        checkstack = false;

                        if (lexer.inlineDup( node) > 0)
                            continue;
                    }

                    Node.insertNodeAtEnd(body, node);
                    mode = Lexer.MixedContent;
                    continue;
                }

                if (node.type == Node.DocTypeTag)
                {
                    continue;
                }

                /*
                  Netscape allows LI and DD directly in BODY
                  We infer UL or DL respectively and use this
                  boolean to exclude block-level elements so as
                  to match Netscape's observed behaviour.
                */
                lexer.excludeBlocks = false;
                if(node.tag != null)
                if (!((node.tag.model & Dict.CM_BLOCK) != 0) &&
                    !((node.tag.model & Dict.CM_INLINE) != 0))
                {
                    /* avoid this error message being issued twice */
                    if (!((node.tag.model & Dict.CM_HEAD) != 0))
                        Report.warning(lexer, body, node, Report.TAG_NOT_ALLOWED_IN);

                    if ((node.tag.model & Dict.CM_HTML) != 0)
                    {
                        /* copy body attributes if current body was inferred */
                        if (node.tag == tt.tagBody && body.implicit 
                                            && body.attributes == null)
                        {
                            body.attributes = node.attributes;
                            node.attributes = null;
                        }

                        continue;
                    }

                    if ((node.tag.model & Dict.CM_HEAD) != 0)
                    {
                        moveToHead(lexer, body, node);
                        continue;
                    }

                    if ((node.tag.model & Dict.CM_LIST) != 0)
                    {
                        lexer.ungetToken();
                        node = lexer.inferredTag( "ul");
                        Node.addClass(node, "noindent");
                        lexer.excludeBlocks = true;
                    }
                    else if ((node.tag.model & Dict.CM_DEFLIST) != 0)
                    {
                        lexer.ungetToken();
                        node = lexer.inferredTag( "dl");
                        lexer.excludeBlocks = true;
                    }
                    else if ((node.tag.model & (Dict.CM_TABLE | Dict.CM_ROWGRP | Dict.CM_ROW)) != 0)
                    {
                        lexer.ungetToken();
                        node = lexer.inferredTag( "table");
                        lexer.excludeBlocks = true;
                    }
                    else
                    {
                        /* AQ: The following line is from the official C
                           version of tidy.  It doesn't make sense to me
                           because the '!' operator has higher precedence
                           than the '&' operator.  It seems to me that the
                           expression always evaluates to 0.

                           if (!node->tag->model & (CM_ROW | CM_FIELD))

                           AQ: 13Jan2000 fixed in C tidy
                        */
                        if (!((node.tag.model & (Dict.CM_ROW | Dict.CM_FIELD)) != 0))
                        {
                            lexer.ungetToken();
                            return;
                        }

                        /* ignore </td> </th> <option> etc. */
                        continue;
                    }
                }

                if (node.type == Node.EndTag)
                {
                	if(node.tag != null)
                    if (node.tag == tt.tagBr)
                        node.type = Node.StartTag;
                    else if (node.tag == tt.tagP)
                    {
                        Node.coerceNode(lexer, node, tt.tagBr);
                        Node.insertNodeAtEnd(body, node);
                        node = lexer.inferredTag("br");
                    }
                    else if ((node.tag.model & Dict.CM_INLINE) != 0)
                        lexer.popInline(node);
                }

                if (node.type == Node.StartTag || node.type == Node.StartEndTag)
                {
                	if(node.tag != null)
                    if (((node.tag.model & Dict.CM_INLINE) != 0) && !((node.tag.model & Dict.CM_MIXED) != 0))
                    {

                        if (checkstack && !node.implicit)
                        {
                            checkstack = false;

                            if (lexer.inlineDup( node) > 0)
                                continue;
                        }

                        mode = Lexer.MixedContent;
                    }
                    else
                    {
                        checkstack = true;
                        mode = Lexer.IgnoreWhitespace;
                    }

                    if (node.implicit)
                        Report.warning(lexer, body, node, Report.INSERTING_TAG);

                    Node.insertNodeAtEnd(body, node);
                    parseTag(lexer, node, mode);
                    continue;
                }

                /* discard unexpected tags */
                Report.warning(lexer, body, node, Report.DISCARDING_UNEXPECTED);
            }
        }

    };

    public static class ParseInline implements Parser {

        public void parse( Lexer lexer, Node element, short mode )
        {
            Node node, parent;
            TagTable tt = lexer.configuration.tt;

            if ((element.tag.model & Dict.CM_EMPTY) != 0)
                return;
/*
            if (element.tag == tt.tagA)
            {
                if (element.attributes == null)
                {
                    Report.warning(lexer, element.parent, element, Report.DISCARDING_UNEXPECTED);
                    Node.discardElement(element);
                    return;
                }
            }
*/
            /*
             ParseInline is used for some block level elements like H1 to H6
             For such elements we need to insert inline emphasis tags currently
             on the inline stack. For Inline elements, we normally push them
             onto the inline stack provided they aren't implicit or OBJECT/APPLET.
             This test is carried out in PushInline and PopInline, see istack.c
             We don't push A or SPAN to replicate current browser behavior
            */
            if (((element.tag.model & Dict.CM_BLOCK) != 0))
                lexer.inlineDup( null);
            else if ((element.tag.model & Dict.CM_INLINE) != 0 &&
                        element.tag != tt.tagA && element.tag != tt.tagSpan)
                lexer.pushInline( element);

            /* Inline elements may or may not be within a preformatted element */
            if (mode != Lexer.Preformatted)
                mode = Lexer.MixedContent;

            while (true)
            {
                node = lexer.getToken(mode);
                if (node == null) break;
                /* end tag for current element */
                if (node.tag == element.tag && node.type == Node.EndTag)
                {
                    if ((element.tag.model & Dict.CM_INLINE) != 0 &&
                        element.tag != tt.tagA)
                        lexer.popInline( node);

                    if (!((mode & Lexer.Preformatted) != 0))
                        Node.trimSpaces(lexer, element);
                    /*
                     if a font element wraps an anchor and nothing else
                     then move the font element inside the anchor since
                     otherwise it won't alter the anchor text color
                    */
                    element.closed = true;
                    Node.trimSpaces(lexer, element);
                    Node.trimEmptyElement(lexer, element);
                    return;
                }

                /* <u>...<u>  map 2nd <u> to </u> if 1st is explicit */
                /* otherwise emphasis nesting is probably unintentional */
                /* big and small have cumulative effect to leave them alone */
                /*if (node.type == Node.StartTag
                        && node.tag == element.tag
                        && lexer.isPushed(node)
                        && !node.implicit
                        && !element.implicit
                        && node.tag != null && ((node.tag.model & Dict.CM_INLINE) != 0)
                        && node.tag != tt.tagA)
                {
                    if (element.content != null && node.attributes == null)
                    {
                        Report.warning(lexer, element, node, Report.COERCE_TO_ENDTAG);
                        node.type = Node.EndTag;
                        lexer.ungetToken();
                        continue;
                    }

                    Report.warning(lexer, element, node, Report.NESTED_EMPHASIS);
                }*/

                if (node.type == Node.TextNode)
                {
                    /* only called for 1st child */
                    if (element.content == null &&
                        !((mode & Lexer.Preformatted) != 0))
                        Node.trimSpaces(lexer, element);

                    if (node.start >= node.end)
                    {
                        continue;
                    }

                    Node.insertNodeAtEnd(element, node);
                    continue;
                }

                /* mixed content model so allow text */
                if (Node.insertMisc(element, node))
                    continue;

                /* deal with HTML tags */
                /*if (node.tag == tt.tagHtml)
                {
                    if (node.type == Node.StartTag || node.type == Node.StartEndTag)
                    {
                        Report.warning(lexer, element, node, Report.DISCARDING_UNEXPECTED);
                        continue;
                    }

                    lexer.ungetToken();
                    if (!((mode & Lexer.Preformatted) != 0))
                        Node.trimSpaces(lexer, element);
                    Node.trimEmptyElement(lexer, element);
                    return;
                }*/
                if (node.tag == tt.tagP &&
                        node.type == Node.StartTag &&
                        ((mode & Lexer.Preformatted) != 0))
                  {
                      node.tag = tt.tagBr;
                      node.element = "br";
                      Node.trimSpaces(lexer, element);
                      Node.insertNodeAtEnd(element, node);
                      continue;
                  }
                if (node.tag == tt.tagBr && node.type == Node.EndTag)
                    node.type = Node.StartTag;

                if (node.type == Node.EndTag)
                {
                    /* coerce </br> to <br> */
                	
                    if (node.tag == tt.tagBr)
                        node.type = Node.StartTag;
                    else if (node.tag == tt.tagP)
                    {
                        /* coerce unmatched </p> to <br><br> */
                        if (!element.isDescendantOf(tt.tagP))
                        {
                            Node.coerceNode(lexer, node, tt.tagBr);
                            Node.trimSpaces(lexer, element);
                            Node.insertNodeAtEnd(element, node);
                            node = lexer.inferredTag("br");
                            continue;
                        }
                    }
                    else if ((node.tag.model & Dict.CM_INLINE) != 0
                                && node.tag != tt.tagA
                                        && !((node.tag.model & Dict.CM_OBJECT) != 0)
                                        && (element.tag.model & Dict.CM_INLINE) != 0)
                    {
                       
                        lexer.popInline( element);

                        if (element.tag != tt.tagA)
                        {
                            if (node.tag == tt.tagA && node.tag != element.tag)
                            {
                               Report.warning(lexer, element, node, Report.MISSING_ENDTAG_BEFORE);
                               lexer.ungetToken();
                            }
                            else
                            {
                                Report.warning(lexer, element, node, Report.NON_MATCHING_ENDTAG);
                            }

                            if (!((mode & Lexer.Preformatted) != 0))
                                Node.trimSpaces(lexer, element);
                            Node.trimEmptyElement(lexer, element);
                            return;
                        }

                        
                        Report.warning(lexer, element, node, Report.DISCARDING_UNEXPECTED);
                        continue;
                    }  
                    else if (lexer.exiled
                                && node.tag.model != 0
                                && (node.tag.model & Dict.CM_TABLE) != 0)
                    {
                        lexer.ungetToken();
                        Node.trimSpaces(lexer, element);
                        Node.trimEmptyElement(lexer, element);
                        return;
                    }
                }

                /* allow any header tag to end current header */
                if ((node.tag.model & Dict.CM_HEADING) != 0 && (element.tag.model & Dict.CM_HEADING) != 0)
                {
                    if (node.tag == element.tag)
                    {
                        Report.warning(lexer, element, node, Report.NON_MATCHING_ENDTAG);
                    }
                    else
                    {
                        Report.warning(lexer, element, node, Report.MISSING_ENDTAG_BEFORE);
                        lexer.ungetToken();
                    }
                    if (!((mode & Lexer.Preformatted) != 0))
                        Node.trimSpaces(lexer, element);
                    Node.trimEmptyElement(lexer, element);
                    return;
                }

                /*
                   an <A> tag to ends any open <A> element
                   but <A href=...> is mapped to </A><A href=...>
                */
                /*
                if (node.tag == tt.tagA && !node.implicit && lexer.isPushed(node))
                {
                    if (node.attributes == null)
                    {
                        node.type = Node.EndTag;
                        Report.warning(lexer, element, node, Report.COERCE_TO_ENDTAG);
                        lexer.popInline( node);
                        lexer.ungetToken();
                        continue;
                    }

                    lexer.ungetToken();
                    Report.warning(lexer, element, node, Report.MISSING_ENDTAG_BEFORE);
                    lexer.popInline( element);
                    if (!((mode & Lexer.Preformatted) != 0))
                        Node.trimSpaces(lexer, element);
                    Node.trimEmptyElement(lexer, element);
                    return;
                }

                if ((element.tag.model & Dict.CM_HEADING) != 0)
                {
                }*/


                /* 
                  if this is the end tag for an ancestor element
                  then infer end tag for this element
                */
                if (node.type == Node.EndTag)
                {
                    for (parent = element.parent;
                            parent != null; parent = parent.parent)
                    {
                        if (node.tag == parent.tag)
                        {
                            if (!((element.tag.model & Dict.CM_OPT) != 0) &&
                                !element.implicit)
                                Report.warning(lexer, element, node, Report.MISSING_ENDTAG_BEFORE);

                            if (element.tag == tt.tagA)
                                lexer.popInline(element);

                            lexer.ungetToken();

                            if (!((mode & Lexer.Preformatted) != 0))
                                Node.trimSpaces(lexer, element);

                            Node.trimEmptyElement(lexer, element);
                            return;
                        }
                    }
                }

                /* block level tags end this element */
                if (!((node.tag.model & Dict.CM_INLINE) != 0))
                {
                    if (node.type != Node.StartTag)
                    {
                        Report.warning(lexer, element, node, Report.DISCARDING_UNEXPECTED);
                        continue;
                    }

                    if (!((element.tag.model & Dict.CM_OPT) != 0))
                        Report.warning(lexer, element, node, Report.MISSING_ENDTAG_BEFORE);

                    if ((node.tag.model & Dict.CM_HEAD) != 0 &&
                        !((node.tag.model & Dict.CM_BLOCK) != 0))
                    {
                        moveToHead(lexer, element, node);
                        continue;
                    }

                    /*
                       prevent anchors from propagating into block tags
                       except for headings h1 to h6
                    */
                    if (element.tag == tt.tagA)
                    {
                        if (node.tag != null &&
                            !((node.tag.model & Dict.CM_HEADING) != 0))
                            lexer.popInline(element);
                        else if (!(element.content != null))
                        {
                            Node.discardElement(element);
                            lexer.ungetToken();
                            return;
                        }
                    }

                    lexer.ungetToken();

                    if (!((mode & Lexer.Preformatted) != 0))
                        Node.trimSpaces(lexer, element);

                    Node.trimEmptyElement(lexer, element);
                    return;
                }

                /* parse inline element */
                if (node.type == Node.StartTag || node.type == Node.StartEndTag)
                {
                   // if (node.implicit)
                     //   Report.warning(lexer, element, node, Report.INSERTING_TAG);

                    /* trim white space before <br> */
                    if (node.tag == tt.tagBr)
                        Node.trimSpaces(lexer, element);
            
                    Node.insertNodeAtEnd(element, node);
                    parseTag(lexer, node, mode);
                    continue;
                }

                /* discard unexpected tags */
                Report.warning(lexer, element, node, Report.DISCARDING_UNEXPECTED);
            }

            if (!((element.tag.model & Dict.CM_OPT) != 0))
                Report.warning(lexer, element, node, Report.MISSING_ENDTAG_FOR);

            Node.trimEmptyElement(lexer, element);
        }
    };

    public static Parser getParseHTML()
    {
        return _parseHTML;
    }

    public static Parser getParseBody()
    {
        return _parseBody;
    }

    public static Parser getParseInline()
    {
        return _parseInline;
    }


    private static Parser _parseHTML = new ParseHTML();
    private static Parser _parseBody = new ParseBody();
    private static Parser _parseInline = new ParseInline();

    /*
      HTML is the top level element
    */
    public static Node parseDocument(Lexer lexer)
    {
        Node node, document, html;
        TagTable tt = lexer.configuration.tt;

        document = lexer.newNode();
        document.type = Node.RootNode;

        while (true)
        {
            node = lexer.getToken(Lexer.IgnoreWhitespace);
            if (node == null) break;

            /* deal with comments etc. */
            if (Node.insertMisc(document, node))
                continue;

            if (node.type == Node.EndTag)
            {
                continue;
            }

            if (node.type != Node.StartTag || node.tag != tt.tagHtml)
            {
                lexer.ungetToken();
                html = lexer.inferredTag("html");
            }
            else
                html = node;

            Node.insertNodeAtEnd(document, html);
            getParseHTML().parse(lexer, html, (short)0); // TODO?
            break;
        }

        return document;
    }

}
