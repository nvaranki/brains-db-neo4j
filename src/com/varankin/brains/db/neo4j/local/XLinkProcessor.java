package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.xml.XmlBrains;
import com.varankin.io.xml.svg.XmlSvg;

import java.util.*;
import java.util.logging.*;
import org.neo4j.graphdb.*;

import static com.varankin.brains.db.DbПреобразователь.toStringValue;
import com.varankin.util.LoggerX;

/**
 * Процессор ссылок в стандарте {@literal Xlink}. 
 * Поддерживается ограниченная часть спецификации.
 * 
 * @see <a href="http://www.w3.org/TR/xlink11/">XML Linking Language (XLink) Version 1.1</a>
 *
 * @author &copy; 2022 Николай Варанкин
 */
class XLinkProcessor
{
    private static final Logger LOGGER = Logger.getLogger( XLinkProcessor.class.getName() );
    private static final LoggerX LOGGER_X = LoggerX.getLogger( XLinkProcessor.class );
    
    private static final String PATH_SEPARATOR = "/";
    private static final String PATH_ROOT = "/";
    private static final String PATH_CURRENT = "./";
    private static final String PATH_PARENT = "../";
    private static final String QUERY = "?";
    private static final String QUERY_SEPARATOR = "&";
    private static final String QUERY_TEST_EQ = "=";
    private static final String REF = "#";

    private final Node ROOT;

    XLinkProcessor( Node root )
    {
        ROOT = root;
    }

    /**
     * Возвращает узел, на который ссылается заданный узел.
     * 
     * @param node   узел, предоставивший ссылку.
     * @param ссылка ссылка в формате Xlink.
     * @return узел, соответствующий ссылке или {@code null}.
     */
    Node resolve( Node node, String ссылка )
    {
        return resolve( node, ссылка, Level.SEVERE );
    }
    
    private Node resolve( Node node, String ссылка, Level level )
    {
        if( node == null )
        {
            LOGGER.log( Level.SEVERE, "Unknown node (null)." );
            return node;
        }
        
        if( ссылка == null )
        {
            LOGGER.log( Level.SEVERE, "Unsupported reference (null) on node {0}.", 
                    название( node ) );
            return null;//node;
        }
        
        if( ссылка.trim().isEmpty() )
            return node;
        
        if( ссылка.startsWith( PATH_ROOT ) ) // /xxxx/yyyy/zzzz
            return resolve( ROOT, ссылка.substring( PATH_ROOT.length() ), level );

        if( ссылка.startsWith( PATH_CURRENT ) ) // ./xxxx/yyyy/zzzz
            return resolve( node, ссылка.substring( PATH_CURRENT.length() ), level );
        
        if( ссылка.startsWith( PATH_PARENT ) ) // ../xxxx/yyyy/zzzz
            return resolve( Architect.getParentNode( node ), ссылка.substring( PATH_PARENT.length() ), level );

        int index = ссылка.indexOf( PATH_SEPARATOR ); // xxxx/yyyy/zzzz
        if( index > 0 )
            return advance( node, ссылка.substring( 0, index ), ссылка.substring( index + 1 ), level );
        
        index = ссылка.indexOf( QUERY ); // xxxx?k1=v1&k2=v2
        if( index > 0 )
            return advance( node, ссылка.substring( 0, index ), ссылка.substring( index ), level );
        
        if( ссылка.startsWith( QUERY ) ) // ?k1=v1&k2=v2
            return query( node, ссылка.substring( QUERY.length() ) );
        
        if( ссылка.startsWith( REF ) ) // #id
        {
            Node n = searchID( node, ссылка.substring( REF.length() ) );
            return n != null ? n : resolve( Architect.getParentNode( node ), ссылка, level );
        }
        
        if( !ссылка.isEmpty() ) // xxxx
            return advance( node, ссылка, "", level );
        
        LOGGER.log( Level.SEVERE, "Unsupported reference {1} on node {0}.", 
                new Object[]{ название( node ), ссылка } );
        return null;
    }
    
    private Node searchID( Node node, String id )
    {
        for( Relationship r : node.getRelationships( Direction.OUTGOING ) )
        {
            Node next = r.getEndNode();
            Object x = next.getProperty( XmlSvg.SVG_ATTR_ID, null );
            if( id.equals( toStringValue( x ) ) )
                return next;
        }
        return null;
    }

    private Node query( Node node, String query )
    {
        for( String test : query.split( QUERY_SEPARATOR ) )
        {
            String[] tokens= test.split( QUERY_TEST_EQ );
            if( tokens.length == 1 )
            {
                if( !node.hasProperty( tokens[0] ) ) return null;
            }
            else if( tokens.length == 2 )
            {
                if( !unquote( tokens[1] ).equals(toStringValue( 
                        node.getProperty( tokens[0], null ) ) ) )
                    return null;
            }
            else
            {
                return null;
            }
        }
        return node;
    }

    private Node advance( Node node, String step, String ссылка, Level level  )
    {
        Collection<Node> targets = new HashSet<>();
        int index = step.indexOf( QUERY ); 
        if( index > 0 )
        {
            // xxxx?k1=v1&k2=v2
            String query = step.substring( index + 1 ); 
            String real_step = step.substring( 0, index );
            RelationshipType rt = NeoФабрика.связь( real_step, XmlBrains.XMLNS_BRAINS );
            for( Relationship r : node.getRelationships( Direction.OUTGOING, rt ) )
                targets.add( query( r.getEndNode(), query ) );
            targets.removeIf( Objects::isNull );
        }
        else
        {
            RelationshipType rt = NeoФабрика.связь( step, XmlBrains.XMLNS_BRAINS );
            for( Relationship r : node.getRelationships( Direction.OUTGOING, rt ) )
                targets.add( r.getEndNode() );
        }
        
        if( targets.isEmpty() )
        {
            LOGGER.log( level, "Node {0} has no elements of type {1}{2}.", 
                    new Object[]{ название( node ), step, 
                                    ссылка != null && ссылка.startsWith( QUERY ) ? ссылка : "" } );
            return null;
        }
        if( targets.size() == 1 )
        {
            return resolve( new ArrayList<>( targets ).get(0), ссылка, level );
        }
        Collection<Node> candidates = new HashSet<>();
        for( Node target : targets )
        {
            Node candidate = resolve( target, ссылка, Level.FINE ); // это тест, без сообщений об ошибках
            if( candidate != null ) candidates.add( candidate );
        }
        if( candidates.isEmpty() )
        {
            LOGGER.log( level, "Node {0} has no elements of type {1}{2}.", 
                    new Object[]{ название( node ), step, 
                                    ссылка != null && ссылка.startsWith( QUERY ) ? ссылка : "" } );
            return null;
        }
        else if( candidates.size() == 1 )
        {
            return new ArrayList<>( candidates ).get(0);
        }
        else
        {
            LOGGER.log( level, "Node {0} has multiple elements of type {1}{2}.", 
                    new Object[]{ название( node ), step, 
                                    ссылка != null && ссылка.startsWith( QUERY ) ? ссылка : "" } );
            return null;
        }
    }
    
    /**
     * Возвращает узел, на который ссылается заданный узел.
     * 
     * @param ссылка ссылка на узел пакета в стандарте XLink.
     * @param референт  узел-источник ссылки.
     * @param положение адрес узла.
     * @return найденный объект или {@code null}.
     */
    Node xlink( String ссылка, Node референт, String положение )
    {
        Node node = resolve( референт, ссылка );
        if( node == null )
        {
            LOGGER_X.log( Level.SEVERE, "002001006S", 
                new Object[]{ положение, ссылка } );
        }
        else if( node.equals( референт ) )
        {
            LOGGER_X.log( Level.SEVERE, "002001007S", 
                new Object[]{ положение, ссылка } );
            node = null;
        }
        if( node == null ) 
            return null;
        else if( Objects.equals( Architect.getXmlEntry( референт, null ), 
                                 Architect.getXmlEntry( node, null ) ) )
            return new LinkedNode( референт, node );
        else
            return node; //TODO [fragment's] return node != null ? new LinkedNode( референт, node ) : null;
    }
    
    private static Object unquote( String string )
    {
        if( string.startsWith( "\"" ) && string.endsWith( "\"" )
            || string.startsWith( "'" ) && string.endsWith( "'" ) )
            return string.substring( 1, string.length() - 1 );
        else
            return string;
    }

    private static Object название( Node node ) 
    {
        Object property = node.getProperty( Architect.P_NODE_NAME, null );
        return property instanceof char[] ? String.valueOf( (char[])property ) : property;
    }
    
}
