/*
 * @(#)DOMDocumentImpl.java   1.11 2000/08/16
 *
 */

package tidy;

import dom.DOMException;

/**
 *
 * DOMDocumentImpl
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

public class DOMDocumentImpl extends DOMNodeImpl implements dom.Document {

    private TagTable tt;      // a DOM Document has its own TagTable.

    protected DOMDocumentImpl(Node adaptee)
    {
        super(adaptee);
        tt = new TagTable();
    }

    public void setTagTable(TagTable tt)
    {
        this.tt = tt;
    }

    /* --------------------- DOM ---------------------------- */

    /**
     * @see dom.Node#getNodeName
     */
    public String getNodeName()
    {
        return "#document";
    }

    /**
     * @see dom.Node#getNodeType
     */
    public short getNodeType()
    {
        return dom.Node.DOCUMENT_NODE;
    }

    /**
     * @see dom.Document#getDoctype
     */
    public dom.DocumentType       getDoctype()
    {
        Node node = adaptee.content;
        while (node != null) {
            if (node.type == Node.DocTypeTag) break;
            node = node.next;
        }
        if (node != null)
            return (dom.DocumentType)node.getAdapter();
        else
            return null;
    }

    /**
     * @see dom.Document#getImplementation
     */
    public dom.DOMImplementation  getImplementation()
    {
        // NOT SUPPORTED
        return null;
    }

    /**
     * @see dom.Document#getDocumentElement
     */
    public dom.Element            getDocumentElement()
    {
        Node node = adaptee.content;
        while (node != null) {
            if (node.type == Node.StartTag ||
                node.type == Node.StartEndTag) break;
            node = node.next;
        }
        if (node != null)
            return (dom.Element)node.getAdapter();
        else
            return null;
    }

    /**
     * @see dom.Document#createElement
     */
    public dom.Element            createElement(String tagName)
                                            throws DOMException
    {
        Node node = new Node(Node.StartEndTag, null, 0, 0, tagName, tt);
        if (node.tag == null)           // Fix Bug 121206
		  node.tag = tt.xmlTags;
		return (dom.Element)node.getAdapter();
    }

    /**
     * @see dom.Document#createDocumentFragment
     */
    public dom.DocumentFragment   createDocumentFragment()
    {
        // NOT SUPPORTED
        return null;
    }

    /**
     * @see dom.Document#createTextNode
     */
    public dom.Text               createTextNode(String data)
    {
        byte[] textarray = Lexer.getBytes(data);
        Node node = new Node(Node.TextNode, textarray, 0, textarray.length);
        return (dom.Text)node.getAdapter();
    }

    /**
     * @see dom.Document#createComment
     */
    public dom.Comment            createComment(String data)
    {
        byte[] textarray = Lexer.getBytes(data);
        Node node = new Node(Node.CommentTag, textarray, 0, textarray.length);
        return (dom.Comment)node.getAdapter();
    }

    /**
     * @see dom.Document#createCDATASection
     */
    public dom.CDATASection       createCDATASection(String data)
                                                 throws DOMException
    {
        // NOT SUPPORTED
        return null;
    }

    /**
     * @see dom.Document#createProcessingInstruction
     */
    public dom.ProcessingInstruction createProcessingInstruction(String target, 
                                                          String data)
                                                          throws DOMException
    {
        throw new DOMExceptionImpl(DOMException.NOT_SUPPORTED_ERR,
                                   "HTML document");
    }

    /**
     * @see dom.Document#createAttribute
     */
    public dom.Attr               createAttribute(String name)
                                              throws DOMException
    {
        AttVal av = new AttVal(null, null, (int)'"', name, null);
        av.dict =
		    AttributeTable.getDefaultAttributeTable().findAttribute(av);
		return (dom.Attr)av.getAdapter();
    }

    /**
     * @see dom.Document#createEntityReference
     */
    public dom.EntityReference    createEntityReference(String name)
                                                    throws DOMException
    {
        // NOT SUPPORTED
        return null;
    }

    /**
     * @see dom.Document#getElementsByTagName
     */
    public dom.NodeList           getElementsByTagName(String tagname)
    {
        return new DOMNodeListByTagNameImpl(this.adaptee, tagname);
    }

    /**
     * DOM2 - not implemented.
     * @exception   dom.DOMException
     */
    public dom.Node importNode(dom.Node importedNode, boolean deep)
        throws dom.DOMException
    {
	return null;
    }

    /**
     * DOM2 - not implemented.
     * @exception   dom.DOMException
     */
    public dom.Attr createAttributeNS(String namespaceURI,
                                              String qualifiedName)
        throws dom.DOMException
    {
	return null;
    }

    /**
     * DOM2 - not implemented.
     * @exception   dom.DOMException
     */
    public dom.Element createElementNS(String namespaceURI,
                                               String qualifiedName)
        throws dom.DOMException
    {
	return null;
    }

    /**
     * DOM2 - not implemented.
     */
    public dom.NodeList getElementsByTagNameNS(String namespaceURI,
                                                       String localName)
    {
	return null;
    }

    /**
     * DOM2 - not implemented.
     */
    public dom.Element getElementById(String elementId)
    {
	return null;
    }

}
