/*
 * @(#)AttributeTable.java   1.11 2000/08/16
 *
 */

package tidy;

import java.util.Hashtable;

/**
 *
 * HTML attribute hash table
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

public class AttributeTable {

    public AttributeTable()
    {
    }

    public Attribute lookup( String name )
    {
        return (Attribute)attributeHashtable.get( name );
    }

    public Attribute install( Attribute attr )
    {
        return (Attribute)attributeHashtable.put( attr.name, attr );
    }

    /* public method for finding attribute definition by name */
    public Attribute findAttribute( AttVal attval )
    {
        Attribute np;

        if ( attval.attribute != null ) {
            np = lookup( attval.attribute );
            return np;
        }

        return null;
    }

    public boolean isUrl( String attrname )
    {
        Attribute np;

        np = lookup( attrname );
        return ( np != null && np.attrchk != null );
    }

    public boolean isLiteralAttribute( String attrname )
    {
        Attribute np;

        np = lookup( attrname );
        return ( np != null && np.literal );
    }

    /*
    Henry Zrepa reports that some folk are
    using embed with script attributes where
    newlines are signficant. These need to be
    declared and handled specially!
    */
    public void declareLiteralAttrib(String name)
    {
        Attribute attrib = lookup(name);

        if (attrib == null)
            attrib = install(new Attribute(name, Dict.VERS_PROPRIETARY, null));

        attrib.literal = true;
    }

    private Hashtable attributeHashtable = new Hashtable();

    private static AttributeTable defaultAttributeTable = null;

    private static Attribute[] attrs = {

    new Attribute( "class",            Dict.VERS_HTML40,            null ),
    new Attribute( "href",             Dict.VERS_ALL,               new Object() ),      /* A, AREA, LINK and BASE */

    };

    public static Attribute attrHref = null;

    public static AttributeTable getDefaultAttributeTable()
    {
        if ( defaultAttributeTable == null ) {
            defaultAttributeTable = new AttributeTable();
            for ( int i = 0; i < attrs.length; i++ ) {
                defaultAttributeTable.install( attrs[i] );
            }
            attrHref = defaultAttributeTable.lookup("href");
        }
        return defaultAttributeTable;
    }

}
