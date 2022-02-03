package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.Транзакция;
import com.varankin.brains.db.xml.ЗонныйКлюч;
import com.varankin.util.LoggerX;

import java.util.Objects;
import java.util.logging.Level;
import org.neo4j.graphdb.*;

import static com.varankin.brains.db.DbПреобразователь.toStringValue;

/**
 * Объект на базе узла Neo4j.
 * 
 * @author &copy; 2021 Николай Варанкин
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
    
    final Node getNodeURI() 
    {
        Relationship r = NODE.getSingleRelationship( NameSpace.Узел, Direction.INCOMING );
        return r != null ? r.getStartNode() : null;
    }
    
    public final NeoАрхив архив()
    {
        return Architect.getInstance().getArchive( NODE.getGraphDatabase() );
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

    protected static Node createNodeInNameSpace( ЗонныйКлюч ключ, GraphDatabaseService сервис )
    {
        Node node = сервис.createNode();
        
        // текущая или традиционная маркировка зоны не обязательна
        if( ключ.НАЗВАНИЕ != null )
        {
            Architect.setXmlEntry( node, ключ.НАЗВАНИЕ );
        }
        if( ключ.ЗОНА != null )
        {
            NeoАрхив архив = Architect.getInstance().getArchive( сервис );
            NeoЗона пи = (NeoЗона)архив.определитьПространствоИмен( ключ.ЗОНА, null );
            пи.getNode().createRelationshipTo( node, NameSpace.Узел );
        }
        return node;
    }
    
    protected final void validate( ЗонныйКлюч ключ )
    {
        // ЗОНА
        Node node = getNodeURI();
        String uri = node != null ? new NeoЗона( node ).uri() : null;
        if( !Objects.equals( ключ.ЗОНА, uri ) )
            throw new IllegalArgumentException( LOGGER.text( "002001012S", uri, ключ.ЗОНА ) );
        
        // НАЗВАНИЕ
        String name = getNodeName( null );
        if( !Objects.equals( ключ.НАЗВАНИЕ, name ) )
            throw new IllegalArgumentException( LOGGER.text( "002001013S", name, ключ.НАЗВАНИЕ ) );
    }

    /**
     * Возвращает узел, на который ссылается данный узел.
     * 
     * @param ссылка ссылка на узел пакета в стандарте XLink.
     * @param p      процессор XLink.
     * @param положение адрес узла.
     * @return найденный объект или {@code null}.
     */
    Node xlink( String ссылка, XLinkProcessor p, String положение )
    {
        if( ссылка == null ) return null;
        Node node = null;
        if( p == null )
        {
            LOGGER.log( Level.SEVERE, "002001018S" );
        }
        else if( ( node = p.resolve( NODE, ссылка ) ) == null )
        {
            LOGGER.log( Level.SEVERE, "002001006S", 
                new Object[]{ положение, ссылка } );
        }
        else if( node.equals( NODE ) )
        {
            LOGGER.log( Level.SEVERE, "002001007S", 
                new Object[]{ положение, ссылка } );
            node = null;
        }
        if( node == null ) 
            return null;
        else if( Objects.equals( Architect.getXmlEntry( NODE, null ), 
                                 Architect.getXmlEntry( node, null ) ) )
            return new LinkedNode( NODE, node );
        else
            return node; //TODO [fragment's] return node != null ? new LinkedNode( NODE, node ) : null;
    }
    
}
