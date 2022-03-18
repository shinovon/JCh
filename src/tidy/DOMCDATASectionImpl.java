/*
 * @(#)DOMCDATASectionImpl.java   1.11 2000/08/16
 *
 */

package tidy;

import dom.DOMException;

/**
 *
 * DOMCDATASectionImpl
 *
 * (c) 1998-2000 (W3C) MIT, INRIA, Keio University
 * See Tidy.java for the copyright notice.
 * Derived from <a href="http://www.w3.org/People/Raggett/tidy">
 * HTML Tidy Release 4 Aug 2000</a>
 *
 * @author  Dave Raggett <dsr@w3.org>
 * @author  Andy Quick <ac.quick@sympatico.ca> (translation to Java)
 * @author  Gary L Peskin <garyp@firstech.com>
 * @version 1.11, 2000/08/16 Tidy Release 4 Aug 2000
 */

public class DOMCDATASectionImpl extends DOMTextImpl
                            implements dom.CDATASection {

    protected DOMCDATASectionImpl(Node adaptee)
    {
        super(adaptee);
    }


    /* --------------------- DOM ---------------------------- */

    /**
     * @see dom.Node#getNodeName
     */
    public String getNodeName()
    {
        return "#cdata-section";
    }

    /**
     * @see dom.Node#getNodeType
     */
    public short getNodeType()
    {
        return dom.Node.CDATA_SECTION_NODE;
    }
}
