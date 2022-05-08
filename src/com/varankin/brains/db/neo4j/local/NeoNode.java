package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.Транзакция;
import com.varankin.brains.db.xml.ЗонныйКлюч;
import com.varankin.util.LoggerX;

import java.util.ArrayList;
import java.util.Objects;

import org.neo4j.graphdb.*;

import static com.varankin.brains.db.DbПреобразователь.toStringValue;

/**
 * Объект на базе узла Neo4j.
 * 
 * @author &copy; 2022 Николай Варанкин
 */
abstract class NeoNode 
{
    private static final LoggerX LOGGER = LoggerX.getLogger( NeoNode.class );
    
    private final Node NODE;
    
    private transient Integer hashCode;

    protected NeoNode( Node node ) 
    {
        NODE = node;
    }

    final Node getNode() 
    {
        return NODE;
    }

    final String getNodeName( String замена )
    {
        return Architect.getXmlEntry( NODE, замена );
    }
    
    final String getNodeURI() 
    {
        Node nsn = Architect.getLinkedNameSpace( NODE );
        return nsn == null ? null : new NeoЗона( nsn ).uri(); // архив().namespaces().stream()... плохо масштабируется
    }
    
    final NeoЗона зона() 
    {
        Node nsn = Architect.getLinkedNameSpace( NODE );
        return nsn == null ? null : архив().namespaces().stream()
            .map( зона -> (NeoЗона) зона )
            .filter( зона -> Objects.equals( зона.getNode().getId(), nsn.getId() ) )
            .findAny().orElse( null );
    }
    
    public final NeoАрхив архив()
    {
        return ArchiveLocator.getInstance().getArchive( NODE.getGraphDatabase() );
    }

    public final Транзакция транзакция()
    {
        return new TransactionImpl( NODE.getGraphDatabase().beginTx() );
    }

    @Override
    public final boolean equals( Object o ) 
    {
        return o instanceof NeoNode 
                && NODE.getId() == ((NeoNode)o).NODE.getId()
                && NODE.getGraphDatabase() == ((NeoNode)o).NODE.getGraphDatabase();
    }

    @Override
    public final int hashCode() 
    {
        if( hashCode == null ) 
            hashCode = 69 * 4 ^ NODE.hashCode();
        return hashCode;
    }
    
    protected Node предок( String property, String value ) //TODO namespace
    {
        for( Node node = Architect.getParentNode( getNode() ); node != null; 
                node = Architect.getParentNode( node ) )
        {
            String name = toStringValue( node.getProperty( property, null ) );
            if( value != null ? value.equals( name ) : name == null )
                return node;
        }
        return null;
    }

    protected static Node createNode( ЗонныйКлюч ключ, GraphDatabaseService сервис )
    {
        Node node = сервис.createNode();
        
        if( ключ.НАЗВАНИЕ != null )
        {
            Architect.setXmlEntry( node, ключ.НАЗВАНИЕ );
        }
        return node;
    }

    protected static Node createNodeInNameSpace( ЗонныйКлюч ключ, GraphDatabaseService сервис )
    {
        Node node = сервис.createNode();
        
        if( ключ.НАЗВАНИЕ != null )
        {
            Architect.setXmlEntry( node, ключ.НАЗВАНИЕ );
        }
        if( ключ.ЗОНА != null )
        {
            NeoАрхив архив = ArchiveLocator.getInstance().getArchive( сервис );
            NeoЗона namespace = new ArrayList<>( архив.namespaces() ).stream()
                .map( зона -> (NeoЗона) зона )
                .filter( зона -> Objects.equals( зона.uri(), ключ.ЗОНА ) )
                .findAny().orElseGet( () -> 
                { 
                    NeoЗона зона = new NeoЗона( сервис );
                    зона.uri( ключ.ЗОНА );
                    архив.namespaces().add( зона );
                    return зона;
                } );
            Architect.linkNodeToNameSpace( node, namespace.getNode() );
        }
        return node;
    }
    
    protected final void validate( ЗонныйКлюч ключ )
    {
        // НАЗВАНИЕ
        String name = getNodeName( null );
        if( !Objects.equals( ключ.НАЗВАНИЕ, name ) )
            throw new IllegalArgumentException( LOGGER.text( "002001013S", name, ключ.НАЗВАНИЕ ) );
        
        // ЗОНА
        String uri = getNodeURI();
        if( !Objects.equals( ключ.ЗОНА, uri ) )
            throw new IllegalArgumentException( LOGGER.text( "002001012S", uri, ключ.ЗОНА ) );
    }

}
