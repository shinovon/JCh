/*
 * @(#)DOMNodeListByTagNameImpl.java   1.11 2000/08/16
 *
 */

package tidy;

/**
 *
 * DOMNodeListByTagNameImpl
 *
 * (c) 1998-2000 (W3C) MIT, INRIA, Keio University
 * See Tidy.java for the copyright notice.
 * Derived from <a href="http://www.w3.org/People/Raggett/tidy">
 * HTML Tidy Release 4 Aug 2000</a>
 *
 * @author  Dave Raggett <dsr@w3.org>
 * @author  Andy Quick <ac.quick@sympatico.ca> (translation to Java)
 * @version 1.4, 1999/09/04 DOM support
 * @version 1.5, 1999/10/23 Tidy Release 27 Sep 1999
 * @version 1.6, 1999/11/01 Tidy Release 22 Oct 1999
 * @version 1.7, 1999/12/06 Tidy Release 30 Nov 1999
 * @version 1.8, 2000/01/22 Tidy Release 13 Jan 2000
 * @version 1.9, 2000/06/03 Tidy Release 30 Apr 2000
 * @version 1.10, 2000/07/22 Tidy Release 8 Jul 2000
 * @version 1.11, 2000/08/16 Tidy Release 4 Aug 2000
 */

/**
 * <p>The items in the <code>NodeList</code> are accessible via an integral 
 * index, starting from 0. 
 *
 */
public class DOMNodeListByTagNameImpl implements dom.NodeList {

    private Node first = null;
    private String tagName = "*";
    private int currIndex = 0;
    private int maxIndex = 0;
    private Node currNode = null;

    protected DOMNodeListByTagNameImpl(Node first, String tagName)
    {
        this.first = first;
        this.tagName = tagName;
    }

    /**
     * @see dom.NodeList#item
     */
    public dom.Node item(int index)
    {
        currIndex = 0;
        maxIndex = index;
        preTraverse(first);

        if (currIndex > maxIndex && currNode != null)
            return currNode.getAdapter();
        else
            return null;
    }

    /**
     * @see dom.NodeList#getLength
     */
    public int getLength()
    {
        currIndex = 0;
        maxIndex = Integer.MAX_VALUE;
        preTraverse(first);
        return currIndex;
    }

    protected void preTraverse(Node node)
    {
        if (node == null)
            return;

        if (node.type == Node.StartTag || node.type == Node.StartEndTag)
        {
            if (currIndex <= maxIndex &&
                (tagName.equals("*") || tagName.equals(node.element)))
            {
                currIndex += 1;
                currNode = node;
            }
        }
        if (currIndex > maxIndex)
            return;

        node = node.content;
        while (node != null)
        {
            preTraverse(node);
            node = node.next;
        }
    }

}
