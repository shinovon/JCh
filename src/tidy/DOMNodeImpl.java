/*
 * @(#)DOMNodeImpl.java   1.11 2000/08/16
 *
 */

package tidy;

import dom.DOMException;

/**
 *
 * DOMNodeImpl
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

public class DOMNodeImpl implements dom.Node {

    protected Node adaptee;

    protected DOMNodeImpl(Node adaptee)
    {
        this.adaptee = adaptee;
    }


    /* --------------------- DOM ---------------------------- */

    /**
     * @see dom.Node#getNodeValue
     */
    public String getNodeValue() throws DOMException
    {
        String value = ""; //BAK 10/10/2000 replaced null
        if (adaptee.type == Node.TextNode ||
            adaptee.type == Node.CDATATag ||
            adaptee.type == Node.CommentTag ||
            adaptee.type == Node.ProcInsTag)
        {

            if (adaptee.textarray != null && adaptee.start < adaptee.end)
            {
                value = Lexer.getString(adaptee.textarray,
                                        adaptee.start,
                                        adaptee.end - adaptee.start);
            }
        }
        return value;
    }

    /**
     * @see dom.Node#setNodeValue
     */
    public void setNodeValue(String nodeValue) throws DOMException
    {
        if (adaptee.type == Node.TextNode ||
            adaptee.type == Node.CDATATag ||
            adaptee.type == Node.CommentTag ||
            adaptee.type == Node.ProcInsTag)
        {
            byte[] textarray = Lexer.getBytes(nodeValue);
            adaptee.textarray = textarray;
            adaptee.start = 0;
            adaptee.end = textarray.length;
        }
    }

    /**
     * @see dom.Node#getNodeName
     */
    public String getNodeName()
    {
        return adaptee.element;
    }

    /**
     * @see dom.Node#getNodeType
     */
    public short getNodeType()
    {
        short result = -1;
        switch (adaptee.type) {
        case Node.RootNode:
            result = dom.Node.DOCUMENT_NODE;
            break;
        case Node.DocTypeTag:
            result = dom.Node.DOCUMENT_TYPE_NODE;
            break;
        case Node.CommentTag:
            result = dom.Node.COMMENT_NODE;
            break;
        case Node.ProcInsTag:
            result = dom.Node.PROCESSING_INSTRUCTION_NODE;
            break;
        case Node.TextNode:
            result = dom.Node.TEXT_NODE;
            break;
        case Node.CDATATag:
            result = dom.Node.CDATA_SECTION_NODE;
            break;
        case Node.StartTag:
        case Node.StartEndTag:
            result = dom.Node.ELEMENT_NODE;
            break;
        }
        return result;
    }

    /**
     * @see dom.Node#getParentNode
     */
    public dom.Node getParentNode()
    {
        if (adaptee.parent != null)
            return adaptee.parent.getAdapter();
        else
            return null;
    }

    /**
     * @see dom.Node#getChildNodes
     */
    public dom.NodeList getChildNodes()
    {
        return new DOMNodeListImpl(adaptee);
    }

    /**
     * @see dom.Node#getFirstChild
     */
    public dom.Node getFirstChild()
    {
        if (adaptee.content != null)
            return adaptee.content.getAdapter();
        else
            return null;
    }

    /**
     * @see dom.Node#getLastChild
     */
    public dom.Node getLastChild()
    {
        if (adaptee.last != null)
            return adaptee.last.getAdapter();
        else
            return null;
    }

    /**
     * @see dom.Node#getPreviousSibling
     */
    public dom.Node getPreviousSibling()
    {
        if (adaptee.prev != null)
            return adaptee.prev.getAdapter();
        else
            return null;
    }

    /**
     * @see dom.Node#getNextSibling
     */
    public dom.Node getNextSibling()
    {
        if (adaptee.next != null)
            return adaptee.next.getAdapter();
        else
            return null;
    }

    /**
     * @see dom.Node#getAttributes
     */
    public dom.NamedNodeMap getAttributes()
    {
        return new DOMAttrMapImpl(adaptee.attributes);
    }

    /**
     * @see dom.Node#getOwnerDocument
     */
    public dom.Document getOwnerDocument()
    {
        Node node;

        node = this.adaptee;
        if (node != null && node.type == Node.RootNode)
            return null;

        for (node = this.adaptee;
            node != null && node.type != Node.RootNode; node = node.parent);

        if (node != null)
            return (dom.Document)node.getAdapter();
        else
            return null;
    }

    /**
     * @see dom.Node#insertBefore
     */
    public dom.Node insertBefore(dom.Node newChild,
                                         dom.Node refChild)
                                             throws DOMException
    {
        // TODO - handle newChild already in tree

        if (newChild == null)
            return null;
        if (!(newChild instanceof DOMNodeImpl)) {
            throw new DOMExceptionImpl(DOMException.WRONG_DOCUMENT_ERR,
                                       "newChild not instanceof DOMNodeImpl");
        }
        DOMNodeImpl newCh = (DOMNodeImpl)newChild;

        if (this.adaptee.type == Node.RootNode) {
            if (newCh.adaptee.type != Node.DocTypeTag &&
                newCh.adaptee.type != Node.ProcInsTag) {
                throw new DOMExceptionImpl(DOMException.HIERARCHY_REQUEST_ERR,
                                       "newChild cannot be a child of this node");
            }
        } else if (this.adaptee.type == Node.StartTag) {
            if (newCh.adaptee.type != Node.StartTag &&
                newCh.adaptee.type != Node.StartEndTag &&
                newCh.adaptee.type != Node.CommentTag &&
                newCh.adaptee.type != Node.TextNode &&
                newCh.adaptee.type != Node.CDATATag) {
                throw new DOMExceptionImpl(DOMException.HIERARCHY_REQUEST_ERR,
                                       "newChild cannot be a child of this node");
            }
        }
        if (refChild == null) {
            Node.insertNodeAtEnd(this.adaptee, newCh.adaptee);
            if (this.adaptee.type == Node.StartEndTag) {
              this.adaptee.setType(Node.StartTag);
            }
        } else {
            Node ref = this.adaptee.content;
            while (ref != null) {
                if (ref.getAdapter() == refChild) break;
                ref = ref.next;
            }
            if (ref == null) {
                throw new DOMExceptionImpl(DOMException.NOT_FOUND_ERR,
                                           "refChild not found");
            }
            Node.insertNodeBeforeElement(ref, newCh.adaptee);
        }
        return newChild;
    }

    /**
     * @see dom.Node#replaceChild
     */
    public dom.Node replaceChild(dom.Node newChild,
                                         dom.Node oldChild)
                                             throws DOMException
    {
        // TODO - handle newChild already in tree

        if (newChild == null)
            return null;
        if (!(newChild instanceof DOMNodeImpl)) {
            throw new DOMExceptionImpl(DOMException.WRONG_DOCUMENT_ERR,
                                       "newChild not instanceof DOMNodeImpl");
        }
        DOMNodeImpl newCh = (DOMNodeImpl)newChild;

        if (this.adaptee.type == Node.RootNode) {
            if (newCh.adaptee.type != Node.DocTypeTag &&
                newCh.adaptee.type != Node.ProcInsTag) {
                throw new DOMExceptionImpl(DOMException.HIERARCHY_REQUEST_ERR,
                                       "newChild cannot be a child of this node");
            }
        } else if (this.adaptee.type == Node.StartTag) {
            if (newCh.adaptee.type != Node.StartTag &&
                newCh.adaptee.type != Node.StartEndTag &&
                newCh.adaptee.type != Node.CommentTag &&
                newCh.adaptee.type != Node.TextNode &&
                newCh.adaptee.type != Node.CDATATag) {
                throw new DOMExceptionImpl(DOMException.HIERARCHY_REQUEST_ERR,
                                       "newChild cannot be a child of this node");
            }
        }
        if (oldChild == null) {
            throw new DOMExceptionImpl(DOMException.NOT_FOUND_ERR,
                                       "oldChild not found");
        } else {
            Node n;
            Node ref = this.adaptee.content;
            while (ref != null) {
                if (ref.getAdapter() == oldChild) break;
                ref = ref.next;
            }
            if (ref == null) {
                throw new DOMExceptionImpl(DOMException.NOT_FOUND_ERR,
                                           "oldChild not found");
            }
            newCh.adaptee.next = ref.next;
            newCh.adaptee.prev = ref.prev;
            newCh.adaptee.last = ref.last;
            newCh.adaptee.parent = ref.parent;
            newCh.adaptee.content = ref.content;
            if (ref.parent != null) {
                if (ref.parent.content == ref)
                    ref.parent.content = newCh.adaptee;
                if (ref.parent.last == ref)
                    ref.parent.last = newCh.adaptee;
            }
            if (ref.prev != null) {
                ref.prev.next = newCh.adaptee;
            }
            if (ref.next != null) {
                ref.next.prev = newCh.adaptee;
            }
            for (n = ref.content; n != null; n = n.next) {
                if (n.parent == ref)
                    n.parent = newCh.adaptee;
            }
        }
        return oldChild;
    }

    /**
     * @see dom.Node#removeChild
     */
    public dom.Node removeChild(dom.Node oldChild)
                                            throws DOMException
    {
        if (oldChild == null)
            return null;

        Node ref = this.adaptee.content;
        while (ref != null) {
            if (ref.getAdapter() == oldChild) break;
            ref = ref.next;
        }
        if (ref == null) {
            throw new DOMExceptionImpl(DOMException.NOT_FOUND_ERR,
                                       "refChild not found");
        }
        Node.discardElement(ref);

        if (this.adaptee.content == null
        &&  this.adaptee.type == Node.StartTag) {
          this.adaptee.setType(Node.StartEndTag);
        }

        return oldChild;
    }

    /**
     * @see dom.Node#appendChild
     */
    public dom.Node appendChild(dom.Node newChild)
                                            throws DOMException
    {
        // TODO - handle newChild already in tree

        if (newChild == null)
            return null;
        if (!(newChild instanceof DOMNodeImpl)) {
            throw new DOMExceptionImpl(DOMException.WRONG_DOCUMENT_ERR,
                                       "newChild not instanceof DOMNodeImpl");
        }
        DOMNodeImpl newCh = (DOMNodeImpl)newChild;

        if (this.adaptee.type == Node.RootNode) {
            if (newCh.adaptee.type != Node.DocTypeTag &&
                newCh.adaptee.type != Node.ProcInsTag) {
                throw new DOMExceptionImpl(DOMException.HIERARCHY_REQUEST_ERR,
                                       "newChild cannot be a child of this node");
            }
        } else if (this.adaptee.type == Node.StartTag) {
            if (newCh.adaptee.type != Node.StartTag &&
                newCh.adaptee.type != Node.StartEndTag &&
                newCh.adaptee.type != Node.CommentTag &&
                newCh.adaptee.type != Node.TextNode &&
                newCh.adaptee.type != Node.CDATATag) {
                throw new DOMExceptionImpl(DOMException.HIERARCHY_REQUEST_ERR,
                                       "newChild cannot be a child of this node");
            }
        }
        Node.insertNodeAtEnd(this.adaptee, newCh.adaptee);

        if (this.adaptee.type == Node.StartEndTag) {
          this.adaptee.setType(Node.StartTag);
        }

        return newChild;
    }

    /**
     * @see dom.Node#hasChildNodes
     */
    public boolean hasChildNodes()
    {
        return (adaptee.content != null);
    }

    /**
     * @see dom.Node#cloneNode
     */
    public dom.Node cloneNode(boolean deep)
    {
        Node node = adaptee.cloneNode(deep);
        node.parent = null;
        return node.getAdapter();
    }

    /**
     * DOM2 - not implemented.
     */
    public void normalize()
    {
    }

    /**
     * DOM2 - not implemented.
     */
    public boolean supports(String feature, String version)
    {
        return isSupported(feature, version);
    }

    /**
     * DOM2 - not implemented.
     */
    public String getNamespaceURI()
    {
        return null;
    }

    /**
     * DOM2 - not implemented.
     */
    public String getPrefix()
    {
        return null;
    }

    /**
     * DOM2 - not implemented.
     */
    public void setPrefix(String prefix)
                            throws DOMException
    {
    }

    /**
     * DOM2 - not implemented.
     */
    public String getLocalName()
    {
      return null;
    }

    /**
     * DOM2 - not implemented.
     */
    public boolean isSupported(String feature,String version) {
        return false;
    }

    /**
     * DOM2 - @see org.w3c.dom.Node#hasAttributes
     * contributed by dlp@users.sourceforge.net
     */
    public boolean hasAttributes()
    {
        return adaptee.attributes != null;
    }
}
