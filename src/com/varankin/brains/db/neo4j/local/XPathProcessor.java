package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.type.DbАтрибутный;
import com.varankin.brains.db.type.DbЗона;
import com.varankin.brains.db.xml.PiProcessor;
import com.varankin.brains.db.xml.XmlBrains;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.*;
import org.neo4j.graphdb.*;

import static com.varankin.brains.db.neo4j.local.Architect.*;
import static com.varankin.brains.db.DbПреобразователь.*;

/**
 * Процессор инструкций XML (Processing Instructions) типа XPath для Neo4j.
 * Обрабатывает ограниченный набор XML элементов типа {@literal <?target instruction ?> }.
 *
 * @author &copy; 2021 Николай Варанкин
 */
class XPathProcessor implements PiProcessor<Node> 
{
    static private final Logger LOGGER = Logger.getLogger( XPathProcessor.class.getName() );
    static private final String PATH_SEPARATOR = "/";
    static private final String PATH_CURRENT = ".";
    static private final String PATH_PARENT = "..";
    static private final String PREFIX_ATTRIBUTE = "@";
    static private final String COLLECTOR_TEXT = "text()";
    static private final String BLOCK_START = "{";
    static private final String BLOCK_STOP = "}";
    private static final String QUERY = "?";
    private static final String QUERY_SEPARATOR = "&";
    private static final String QUERY_TEST_EQ = "=";

    /**
     * Идентификатор типа обрабатываемых инструкций.
     */
    static final String TARGET = "xpath";

    @Override
    public String выполнить( Node node, String инструкция ) 
    {
        if( инструкция == null ) return null;
        
        // извлечь из блока
        инструкция = инструкция.trim();
        while( инструкция.startsWith( BLOCK_START ) && инструкция.endsWith( BLOCK_STOP ) )
            инструкция = инструкция.substring( 1, инструкция.length() - 1 ).trim();

        // пройти путь до оконечного узла
        LinkedList<Node> варианты = new LinkedList<>();
        варианты.add( node ); // допустимо много узлов, пока инструкция содержит путь
        int index = инструкция.indexOf( PATH_SEPARATOR ); // xxxx/yyyy/zzzz
        while( index > 0 )
        {
            String step = инструкция.substring( 0, index );
            switch( step )
            {
                case PATH_CURRENT:
                    break; // nothing to do
                    
                case PATH_PARENT:
                    for( Node n : new ArrayList<>( варианты ) )
                    {
                        варианты.remove( n );
                        варианты.add( getParentNode( n ) );
                    }
                    break;
                    
                default:
                    for( Node n : new ArrayList<>( варианты ) )
                    {
                        варианты.remove( n );
                        варианты.addAll( advance( n, step ) );
                    }
            }
            инструкция = инструкция.substring( index + 1 );
            index = инструкция.indexOf( PATH_SEPARATOR );
        } 
        if( варианты.size() != 1 )
            LOGGER.log( Level.WARNING, "XML processing instruction \"{0}\" failed to produce single node.", инструкция );
        else
        {
            node = варианты.get( 0 );
            // извлечь текст
            if( инструкция.startsWith( PREFIX_ATTRIBUTE ) )
                return getAttribute( node, инструкция.substring( PREFIX_ATTRIBUTE.length() ) );
            if( инструкция.startsWith( COLLECTOR_TEXT ) )
                return collectFreeText( node );
        }
            

        LOGGER.log(Level.WARNING, "Unrecognized fragment of XML processing instruction: \"{0}\".", инструкция);
        return "<!-- " + инструкция.replace("-->", "-x- >") + " -->";
    }

    private String getAttribute( Node node, String название )
    {
        if( название == null || название.isEmpty() ) return null;
        
        String[] p = название.split( ":" );
        String prefix = p.length > 1 ? p[0] : null;
        String name = p.length == 1 ? p[0] : p.length > 1 ? p[1] : null;
        
        if( name == null || name.isEmpty() )
        {
            LOGGER.log( Level.FINE, "Wrong attribute name: {0}", название );
            return "";
        }
        else if( prefix == null || prefix.trim().isEmpty() )
        {
            return toStringValue( new NeoАтрибутный( node ){}.атрибут( name, null, "" ) );
        }
        else
        {
            String uri = null;
            for( DbЗона ns : Architect.getInstance().getArchive( node.getGraphDatabase() ).namespaces() )
                if( ns.варианты().contains( prefix ) )
                    if( uri == null )
                        uri = ns.uri();
                    else
                        LOGGER.log( Level.SEVERE, "Duplicate name space prefix {0} on URI {1}.", 
                                new Object[]{ prefix, ns.uri() } );
            if( uri != null )
                return toStringValue( new NeoАтрибутный( node ){}.атрибут( name, uri, "" ) );
            else
                LOGGER.log( Level.SEVERE, "Unable to resolve name space prefix {0}.", prefix );
            return "";
        }
    }

    private String collectFreeText( Node node )
    {
        StringBuilder text = new StringBuilder();
        for( NeoТекстовыйБлок текст : new КоллекцияПоСвязи<>( node, Связь.Текст, n -> new NeoТекстовыйБлок( n ) ) )
            text.append( текст.текст() );
        return text.toString();
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

    private Collection<Node> advance( Node node, String step )
    {
        String[] splitted = step.split( "\\" + QUERY );
        if( splitted.length == 0 || splitted.length > 2 )
        {
            LOGGER.log( Level.SEVERE, "Syntax error in path fragment \"{0}\" related to query sign(s) " + QUERY, step );
            return Collections.emptyList();
        }
        RelationshipType rt = NeoФабрика.связь( splitted[0], XmlBrains.XMLNS_BRAINS );
        Collection<Node> targets = new HashSet<>();
        for( Relationship r : node.getRelationships( Direction.OUTGOING, rt ) )
        {
            Node target = r.getEndNode();
            if( splitted.length == 2 )
                target = query( target, splitted[1] );
            if( target != null )
                targets.add( target );
        }
        return targets;
    }

    private static Object unquote( String string )
    {
        if( string.startsWith( "\"" ) && string.endsWith( "\"" )
            || string.startsWith( "'" ) && string.endsWith( "'" ) )
            return string.substring( 1, string.length() - 1 );
        else
            return string;
    }

}
