/*
 * @(#)AttVal.java   1.11 2000/08/16
 *
 */

package tidy;

/**
 *
 * Attribute/Value linked list node
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

public class AttVal extends Object {

    public AttVal    next;
    public Attribute dict;
    public Node      asp;
    public Node      php;
    public int       delim;
    public String    attribute;
    public String    value;

    public AttVal()
    {
        this.next = null;
        this.dict = null;
        this.asp = null;
        this.php = null;
        this.delim = 0;
        this.attribute = null;
        this.value = null;
    }

    public AttVal(AttVal next, Attribute dict, int delim,
                  String attribute, String value)
    {
        this.next = next;
        this.dict = dict;
        this.asp = null;
        this.php = null;
        this.delim = delim;
        this.attribute = attribute;
        this.value = value;
    }

    public AttVal(AttVal next, Attribute dict, Node asp, Node php,
                  int delim, String attribute, String value)
    {
        this.next = next;
        this.dict = dict;
        this.asp = asp;
        this.php = php;
        this.delim = delim;
        this.attribute = attribute;
        this.value = value;
    }

    protected Object clone()
    {
        AttVal av = new AttVal();
        if (next != null) {
            av.next = (AttVal)next.clone();
        }
        if (attribute != null)
            av.attribute = attribute;
        if (value != null)
            av.value = value;
        av.delim = delim;
        if (asp != null) {
            av.asp = (Node)asp.clone();
        }
        if (php != null) {
            av.php = (Node)php.clone();
        }
        av.dict =
          AttributeTable.getDefaultAttributeTable().findAttribute(this);
        return av;
    }

    /* --------------------- DOM ---------------------------- */

    protected dom.Attr adapter = null;

    protected dom.Attr getAdapter()
    {
        if (adapter == null)
        {
            adapter = new DOMAttrImpl(this);
        }
        return adapter;
    }
    /* --------------------- END DOM ------------------------ */

}
