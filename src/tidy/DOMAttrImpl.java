/*
 * @(#)DOMAttrImpl.java   1.11 2000/08/16
 *
 */

package tidy;

import dom.DOMException;

/**
 *
 * DOMAttrImpl
 *
 * (c) 1998-2000 (W3C) MIT, INRIA, Keio University
 * See Tidy.java for the copyright notice.
 * Derived from <a href="http://www.w3.org/People/Raggett/tidy">
 * HTML Tidy Release 4 Aug 2000</a>
 *
 * @author  Dave Raggett <dsr@w3.org>
 * @author  Andy Quick <ac.quick@sympatico.ca> (translation to Java)
 * @version 1.4, 1999/09/04 DOM Support
 * @version 1.5, 1999/10/23 Tidy Release 27 Sep 1999
 * @version 1.6, 1999/11/01 Tidy Release 22 Oct 1999
 * @version 1.7, 1999/12/06 Tidy Release 30 Nov 1999
 * @version 1.8, 2000/01/22 Tidy Release 13 Jan 2000
 * @version 1.9, 2000/06/03 Tidy Release 30 Apr 2000
 * @version 1.10, 2000/07/22 Tidy Release 8 Jul 2000
 * @version 1.11, 2000/08/16 Tidy Release 4 Aug 2000
 */

public class DOMAttrImpl extends DOMNodeImpl implements dom.Attr {

    protected AttVal avAdaptee;

    protected DOMAttrImpl(AttVal adaptee)
    {
        super(null); // must override all methods of DOMNodeImpl
        this.avAdaptee = adaptee;
    }


    /* --------------------- DOM ---------------------------- */

    public String getNodeValue() throws DOMException
    {
        return getValue();
    }

    public void setNodeValue(String nodeValue) throws DOMException
    {
        setValue(nodeValue);
    }

    public String getNodeName()
    {
        return getName();
    }

    public short getNodeType()
    {
        return dom.Node.ATTRIBUTE_NODE;
    }

    public dom.Node getParentNode()
    {
        return null;
    }

    public dom.NodeList getChildNodes()
    {
        // NOT SUPPORTED
        return null;
    }

    public dom.Node getFirstChild()
    {
        // NOT SUPPORTED
        return null;
    }

    public dom.Node getLastChild()
    {
        // NOT SUPPORTED
        return null;
    }

    public dom.Node getPreviousSibling()
    {
        return null;
    }

    public dom.Node getNextSibling()
    {
        return null;
    }

    public dom.NamedNodeMap getAttributes()
    {
        return null;
    }

    public dom.Document getOwnerDocument()
    {
        return null;
    }

    public dom.Node insertBefore(dom.Node newChild, 
                                         dom.Node refChild)
                                             throws DOMException
    {
        throw new DOMExceptionImpl(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   "Not supported");
    }

    public dom.Node replaceChild(dom.Node newChild, 
                                         dom.Node oldChild)
                                             throws DOMException
    {
        throw new DOMExceptionImpl(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   "Not supported");
    }

    public dom.Node removeChild(dom.Node oldChild)
                                            throws DOMException
    {
        throw new DOMExceptionImpl(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   "Not supported");
    }

    public dom.Node appendChild(dom.Node newChild)
                                            throws DOMException
    {
        throw new DOMExceptionImpl(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   "Not supported");
    }

    public boolean hasChildNodes()
    {
        return false;
    }

    public dom.Node cloneNode(boolean deep)
    {
        return null;
    }

    /**
     * @see dom.Attr#getName
     */
    public String getName()
    {
        return avAdaptee.attribute;
    }

    /**
     * @see dom.Attr#getSpecified
     */
    public boolean getSpecified()
    {
        return true;
    }

    /**
     * Returns value of this attribute.  If this attribute has a null value,
     * then the attribute name is returned instead.
     * Thanks to Brett Knights <brett@knightsofthenet.com> for this fix.
     * @see dom.Attr#getValue
     * 
     */
    public String getValue()
    {
        return (avAdaptee.value == null) ? avAdaptee.attribute : avAdaptee.value ;
    }

    /**
     * @see dom.Attr#setValue
     */
    public void setValue(String value)
    {
        avAdaptee.value = value;
    }

    /**
     * DOM2 - not implemented.
     */
    public dom.Element getOwnerElement() {
	return null;
    }

}
