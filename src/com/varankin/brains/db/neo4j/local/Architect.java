package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.xml.XmlBrains;
import com.varankin.brains.db.xml.МаркированныйЗонныйКлюч;
import com.varankin.filter.*;
import com.varankin.util.*;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.*;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;

import static com.varankin.brains.db.DbПреобразователь.*;
import static com.varankin.brains.db.xml.Xml.*;
import com.varankin.brains.db.xml.type.XmlАрхив;
import com.varankin.brains.db.xml.ЗонныйКлюч;

/**
 * Utility container for Neo4j&trade;.
 * 
 * @author &copy; 2021 Николай Варанкин
 */
final class Architect
{
    static private final LoggerX LOGGER = LoggerX.getLogger( Architect.class );

    static final String INDEX_ARCHIVE = "tmi";
    static final String P_NODE_NAME = "#LNAME";
    static final String MASTER_PROPERTY = "#tmp";

    static private final Architect ARCHITECT = new Architect();
    static final Architect getInstance() { return ARCHITECT; }

    private final static GraphDatabaseFactory ФАБРИКА_БД = new GraphDatabaseFactory();
    private final Collection<NeoАрхив> АРХИВЫ;
    
    private Architect() 
    {
        АРХИВЫ = new ArrayList<>();
    }
    
    static GraphDatabaseService openEmbeddedService( String dbpath, Map<String,String> ca )
    {
        boolean новая = !new File( dbpath ).exists() || !new File( dbpath, "neostore" ).exists();
        GraphDatabaseService сервис;
        сервис = ФАБРИКА_БД.newEmbeddedDatabase( new File( dbpath ) );
        Runtime.getRuntime().addShutdownHook( new Thread( () -> сервис.shutdown() ) );
        try( Transaction t = сервис.beginTx() )
        {
            Architect.initIndexes( сервис, ca );
            t.success();
            if( новая )
                LOGGER.log( Level.INFO, "002001014I", dbpath );
        }
        return сервис;
    }
    
    NeoАрхив getArchive( GraphDatabaseService сервис )
    {
        for( NeoАрхив архив : АРХИВЫ )
            if( архив.getNode().getGraphDatabase().equals( сервис ) )
                return архив;
        throw new NoSuchElementException( "No archive found for the Neo4j service " + сервис );
    }
    
    void unregisterArchive( NeoАрхив архив )
    {
        try
        {
            архив.getNode().getGraphDatabase().shutdown();
        }
        finally
        {
            АРХИВЫ.remove( архив );
        }
    }
    
    void registerNewArchive( NeoАрхив архив, Consumer<Long> обработчик )
    {
        GraphDatabaseService сервис = архив.getNode().getGraphDatabase();
        for( NeoАрхив а : АРХИВЫ )
            if( сервис.equals( а.getNode().getGraphDatabase() ) )
                throw new IllegalStateException( "Duplicate archive" );
        архив.getNode().getGraphDatabase().registerTransactionEventHandler( new TransactionEventHandler<Void>() 
        {
            @Override
            public Void beforeCommit( TransactionData td ) throws Exception 
            {
                обработчик.accept( System.currentTimeMillis() ); // td.getCommitTime() -> Exception;
                return null;
            }

            @Override
            public void afterCommit( TransactionData td, Void t ) {} //TODO повторить обработчик с td.getCommitTime() ?

            @Override
            public void afterRollback( TransactionData td, Void t ) {}
            
        } );
        АРХИВЫ.add( архив );
    }
    
    //<editor-fold defaultstate="collapsed" desc="nodes">
    
    static Node createArchiveNode( GraphDatabaseService сервис )
    {
        Node node = сервис.createNode();
        Long ts = System.currentTimeMillis();
        node.setProperty( P_NODE_NAME, XmlBrains.XML_ARHIVE.toCharArray() );
        node.setProperty( XmlАрхив.КЛЮЧ_А_СОЗДАН.НАЗВАНИЕ, ts );

        node.setProperty( MASTER_PROPERTY, ts );
        Architect.indexArchiveNode( node, ts );

        LOGGER.log( Level.CONFIG, "002001011C" );
        return node;
    }

    static Node findSingleNode( GraphDatabaseService сервис, String id )
    {
        Map<Long,Node> masters = new TreeMap<>();
        Index<Node> индекс = сервис.index().forNodes( id );
        for( Node n : индекс.query( MASTER_PROPERTY, "*" ) )
            masters.put( toLongValue( 
                n.getProperty( MASTER_PROPERTY, System.currentTimeMillis() ) ), n );
        Optional<Node> first = masters.values().stream().findFirst();
        if( first.isPresent() )
        {
            if( masters.size() > 1 )
                LOGGER.log( Level.SEVERE, "002001002S", masters.size() );
            return first.get();
        }
        else
        {
            return null;
        }
    }
    
    static boolean testNodeToParent( Node node, Node parent, RelationshipType type )
    {
        Relationship relationship = null;
        for( Relationship r : parent.getRelationships( Direction.OUTGOING, type ) )
            if( r.getEndNode().getId() == node.getId() )
                if( relationship == null )
                    relationship = r;
                else
                {
                    LOGGER.log( Level.SEVERE, "002001015S", type.name(), parent.getId(), node.getId() );
                    // исправить ситуацию
                    for( Map.Entry<String, Object> e : r.getAllProperties().entrySet() )
                        if( !relationship.hasProperty( e.getKey() ) )
                            relationship.setProperty( e.getKey(), e.getValue() );
                        else if( !Objects.equals( relationship.getProperty( e.getKey() ), r.getProperty( e.getKey() ) ) )
                            LOGGER.log( Level.SEVERE, "002001016S", e.getKey(), 
                                    relationship.getProperty( e.getKey() ), r.getProperty( e.getKey() ) );
                    r.delete();
                }
        return relationship == null;
    }
    
    static Relationship linkNodeToParent( Node node, Node parent, RelationshipType type )
    {
        return parent.createRelationshipTo( node, type );
    }

    static void unlinkNodeFromParent( Node node, Node parent, RelationshipType... type )
    {
        for( Relationship r : node.getRelationships( Direction.INCOMING, type ) )
            if( r.getStartNode().getId() == parent.getId() )
                r.delete();
    }

    static Node getParentNode( Node node, RelationshipType... types )
    {
        if( node instanceof LinkedNode ) 
            return getParentNode( ((LinkedNode)node).getSource(), types );
        Iterable<Relationship> relationships = types.length > 0 ?
                node.getRelationships( Direction.INCOMING, types ) : 
                node.getRelationships( Direction.INCOMING, Связь.values() );
        Collection<Node> nodes = new HashSet<>(); // respect multiple links
        for( Relationship r : relationships )
            nodes.add( r.getStartNode() );
        
        if( nodes.isEmpty() )
            return null;
        else if( nodes.size() == 1 )
            return new ArrayList<>( nodes ).get( 0 );
        else
            throw new RuntimeException( String.format( 
                    "Not a single quantity of parents (%d) detected for node %d.", 
                    nodes.size(), node.getId() ) );
    }
    
    static void removeTree( Node node ) 
    {
        boolean loggable = LOGGER.getLogger().isLoggable( Level.FINEST );
        String nodeName = loggable ? toStringValue( node.getProperty( P_NODE_NAME, null )) : null;

        // remove all child nodes; respect multiple links on pairs
        Collection<Node> childs = new HashSet<>();
        for( Relationship r : node.getRelationships( Direction.OUTGOING, Связь.values() ) ) 
            childs.add( r.getEndNode() );
        for( Node child : childs )
            removeTree( child ); // it also removes all @Direction.OUTGOING listed above

        // unlink from others
        for( Relationship r : node.getRelationships() ) 
        {
            if( loggable ) LOGGER.log( Level.FINEST, "Unlinking {1} INCOMING from {0}", 
                    new Object[]{nodeName,r.getType().name()} );
            r.delete();
        }

        // ready to remove the node
        if( loggable ) LOGGER.log( Level.FINEST, "Removing node {0} id={1}", 
                new Object[]{nodeName,node.getId()} );
        node.delete();
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="indexing">
    
    private static final Map<String,String> imap;
    static
    {
        imap = new HashMap<>();
        imap.put( INDEX_ARCHIVE, MASTER_PROPERTY ); // per timestamp
    }
    
    static void initIndexes( GraphDatabaseService сервис, Map<String,String> ca )
    {
        IndexManager им = сервис.index();
        if( !им.existsForNodes( INDEX_ARCHIVE ) )
            им.forNodes( INDEX_ARCHIVE, ca );
    }
    
    static void indexArchiveNode( Node node, Long timestamp )
    {
        indexNode( node, INDEX_ARCHIVE, timestamp );
    }
    
    private static void indexNode( Node node, String indexId, Object property )
    {
        Index<Node> индекс = node.getGraphDatabase().index().forNodes( indexId );
        индекс.add( node, imap.get( indexId ), property );
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="properties">

    static Collection<Class<?>> ATTRIBUTE_TYPES = Arrays.asList
    (
        Boolean.class, boolean[].class,
        Byte.class, byte[].class,
        Short.class, short[].class,
        Integer.class, int[].class,
        Long.class, long[].class,
        Float.class, float[].class,
        Double.class, double[].class,
        Character.class, char[].class,
        String.class, String[].class       
    );

    static Iterable<МаркированныйЗонныйКлюч> getLocalPropertyKeys( final Node node )
    {
        Фильтр<МаркированныйЗонныйКлюч> фильтр = (ключ) -> 
        {
            String название = ключ.название();
            return название != null && !( название.startsWith( "#" ) );
        };
        ЗонныйКлюч тип = new NeoАтрибутный( node ){}.тип();
        Iterator<Relationship> nsri = node.getRelationships( Direction.INCOMING, NameSpace.Узел ).iterator();
        String префикс = null;
        if( nsri.hasNext() )
        {
            NeoЗона nsn = new NeoЗона( nsri.next().getStartNode() );
            if( nsri.hasNext() ) throw new IllegalStateException( nsn.uri() );
            префикс = nsn.название();
        }
        else
        {
            //TODO префикс на brains
        }
        МаркированныйЗонныйКлюч мтип = new МаркированныйЗонныйКлюч( тип.НАЗВАНИЕ, тип.ЗОНА, префикс);
        return new FilteredIterable<>( () -> new LocalPropertyKeysIterator( 
                node.getPropertyKeys().iterator(), мтип ), фильтр );
    }
    
    static Iterable<МаркированныйЗонныйКлюч> getForeignPropertyKeys( final Node node )
    {
        Фильтр<МаркированныйЗонныйКлюч> фильтр = (ключ) -> !XML_XMLNS.equals( ключ.название() );
        return new FilteredIterable<>( () -> new ForeignPropertyKeysIterator(
                node.getRelationships( NameSpace.Атрибут, Direction.INCOMING ).iterator() ), фильтр );
    }
    
    static Iterable<МаркированныйЗонныйКлюч> getPropertyKeys( Node node )
    {
        return new MultiIterable<>(
                getLocalPropertyKeys( node ),
                getForeignPropertyKeys( node ) );
    }
    
    static String getXmlEntry( Node node, String missing )
    {
        Node n = node instanceof LinkedNode ? ((LinkedNode)node).getPattern() : node;
        return toStringValue( n.getProperty( P_NODE_NAME, missing ) );
    }
    
    static void setXmlEntry( Node node, String name )
    {
        if( node instanceof LinkedNode ) 
            throw new IllegalArgumentException( node.getClass().getName() );
        node.setProperty( P_NODE_NAME, name.toCharArray() );
    }
    
    static String getURI( Node узел )
    {
        Relationship r = узел.getSingleRelationship( NameSpace.Узел, Direction.INCOMING );
        return r != null ? new NeoЗона( r.getStartNode() ).uri() : XmlBrains.XMLNS_BRAINS; //TODO avoid new
    }

    PropertyContainer getPropertyContainer( Node узел, String uri, boolean создать )
    {
        if( uri == null || uri.trim().isEmpty() ) return узел;
        
        if( uri.equalsIgnoreCase( getURI( узел ) ) )
        {
            return узел;
        }
        else
        {
            Relationship r = null;
            for( Relationship rns : узел.getRelationships( NameSpace.Атрибут, Direction.INCOMING ) )
                if( uri.equalsIgnoreCase( new NeoЗона( rns.getStartNode() ).uri() ) ) //TODO avoid new
                    if( r == null )
                        r = rns;
                    else
                        LOGGER.getLogger().log( Level.SEVERE, "Multiple parameter links to namespace node {0}", uri );
            if( r == null && создать )
            {
                // только в режиме "создать"! иначе - блокировка транзакции из-за циклического ожидания разрешения на запись
                NeoАрхив архив = getInstance().getArchive( узел.getGraphDatabase() );
                NeoЗона пи = (NeoЗона)архив.определитьПространствоИмен( uri, null );
                r = пи.getNode().createRelationshipTo( узел, NameSpace.Атрибут );
            }
            return r;
        }
    }
        
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="property iterators">
    
    static private class ForeignPropertyKeysIterator implements Iterator<МаркированныйЗонныйКлюч>
    {
        final Iterator<Relationship> NS_GROUPS;
        Iterator<String> KEYS = Collections.emptyIterator();
        String URI, PREFIX;
        
        ForeignPropertyKeysIterator( Iterator<Relationship> nsg )
        {
            NS_GROUPS = nsg;
        }
        
        @Override
        public boolean hasNext()
        {
            do
            {
                if( KEYS.hasNext() )
                    return true;
                else if( NS_GROUPS.hasNext() )
                {
                    Relationship r = NS_GROUPS.next();
                    KEYS = r.getPropertyKeys().iterator();
                    NeoЗона nsn = new NeoЗона( r.getStartNode() );
                    URI = nsn.uri();//toStringValue( r.getStartNode().getProperty( XML_XMLNS, null ) );
                    PREFIX = nsn.название();//toStringValue( //r.getProperty( XML_XMLNS, null ) );
                }
                else
                    return false;
            }
            while( true );
        }
        
        @Override
        public МаркированныйЗонныйКлюч next()
        {
            if( hasNext() )
                return new МаркированныйЗонныйКлюч( KEYS.next(), URI, PREFIX );
            else
                throw new NoSuchElementException();
        }
        
        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
    
    static private class LocalPropertyKeysIterator implements Iterator<МаркированныйЗонныйКлюч>
    {
        final Iterator<String> ITERATOR;
        final String URI, PREFIX;
        
        LocalPropertyKeysIterator( Iterator<String> iterator, МаркированныйЗонныйКлюч тип )
        {
            ITERATOR = iterator;
            URI = тип.ЗОНА;
            PREFIX = тип.PREFIX;
        }
        
        @Override
        public boolean hasNext()
        {
            return ITERATOR.hasNext();
        }
        
        @Override
        public МаркированныйЗонныйКлюч next()
        {
            return new МаркированныйЗонныйКлюч( ITERATOR.next(), URI, PREFIX );
        }
        
        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
    
    //</editor-fold>

}
