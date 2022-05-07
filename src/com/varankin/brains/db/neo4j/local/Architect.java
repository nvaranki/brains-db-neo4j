package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.xml.XmlBrains;
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

import static com.varankin.brains.db.DbПреобразователь.*;
import static com.varankin.brains.db.xml.Xml.*;
import com.varankin.brains.db.xml.ЗонныйКлюч;

/**
 * Utility container for Neo4j&trade;.
 * 
 * @author &copy; 2022 Николай Варанкин
 */
final class Architect
{
    static private final LoggerX LOGGER = LoggerX.getLogger( Architect.class );

    static final String P_NODE_NAME = "#LNAME";

    static private final Architect ARCHITECT = new Architect();
    static final Architect getInstance() { return ARCHITECT; }

    private final static GraphDatabaseFactory ФАБРИКА_БД = new GraphDatabaseFactory();
    
    private Architect() 
    {
    }
    
    static GraphDatabaseService openEmbeddedService( File dbpath )
    {
        boolean новая = !dbpath.exists() || !new File( dbpath, "neostore" ).exists();
        GraphDatabaseService сервис = ФАБРИКА_БД.newEmbeddedDatabase( dbpath );
        Runtime.getRuntime().addShutdownHook( new Thread( () -> сервис.shutdown() ) );
        if( новая )
            LOGGER.log( Level.INFO, "002001014I", dbpath.getAbsolutePath() );
        return сервис;
    }
    
    /**
     * Регистрирует обработчики транзакционных событий.
     * 
     * @param сервис
     * @param обработчик действие для выполнения непосредственно перед завершением транзакции.
     */
    static void registerTransactionEventHandler( GraphDatabaseService сервис, Consumer<Long> обработчик )
    {
        сервис.registerTransactionEventHandler( new TransactionEventHandler<Void>() 
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
    }
    
    //<editor-fold defaultstate="collapsed" desc="nodes">

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
    
    static Node getLinkedNameSpace( Node node )
    {
        Relationship r = node.getSingleRelationship( NameSpace.Узел, Direction.INCOMING );
        return r != null ? r.getStartNode() : null;
    }
    
    static void linkNodeToNameSpace( Node node, Node nsn )
    {
        nsn.createRelationshipTo( node, NameSpace.Узел );
    }

    static void unlinkNodeFromNameSpace( Node node, Node nsn )
    {
        for( Relationship r : node.getRelationships( Direction.INCOMING, NameSpace.Узел ) )
            if( r.getStartNode().getId() == nsn.getId() )
                r.delete();
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

    static Iterable<ЗонныйКлюч> getLocalPropertyKeys( final Node node )
    {
        Фильтр<ЗонныйКлюч> фильтр = (ключ) -> 
        {
            String название = ключ.НАЗВАНИЕ;
            return название != null && !( название.startsWith( "#" ) );
        };
        ЗонныйКлюч тип = new NeoАтрибутный( node ){}.тип();
        Iterator<Relationship> nsri = node.getRelationships( Direction.INCOMING, NameSpace.Узел ).iterator();
        if( nsri.hasNext() )
        {
            NeoЗона nsn = new NeoЗона( nsri.next().getStartNode() );
            if( nsri.hasNext() ) throw new IllegalStateException( nsn.uri() );
        }
        else
        {
            //TODO префикс на brains
        }
        return new FilteredIterable<>( () -> new LocalPropertyKeysIterator( 
                node.getPropertyKeys().iterator(), тип ), фильтр );
    }
    
    static Iterable<ЗонныйКлюч> getForeignPropertyKeys( final Node node )
    {
        Фильтр<ЗонныйКлюч> фильтр = (ключ) -> !XML_XMLNS.equals( ключ.НАЗВАНИЕ );
        return new FilteredIterable<>( () -> new ForeignPropertyKeysIterator(
                node.getRelationships( NameSpace.Атрибут, Direction.INCOMING ).iterator() ), фильтр );
    }
    
    static Iterable<ЗонныйКлюч> getPropertyKeys( Node node )
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
                NeoАрхив архив = ArchiveLocator.getInstance().getArchive( узел.getGraphDatabase() );
                NeoЗона пи = (NeoЗона)архив.определитьПространствоИмен( uri, null );
                r = пи.getNode().createRelationshipTo( узел, NameSpace.Атрибут );
            }
            return r;
        }
    }
        
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="property iterators">
    
    static private class ForeignPropertyKeysIterator implements Iterator<ЗонныйКлюч>
    {
        final Iterator<Relationship> NS_GROUPS;
        Iterator<String> KEYS = Collections.emptyIterator();
        String URI;
        
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
                }
                else
                    return false;
            }
            while( true );
        }
        
        @Override
        public ЗонныйКлюч next()
        {
            if( hasNext() )
                return new ЗонныйКлюч( KEYS.next(), URI );
            else
                throw new NoSuchElementException();
        }
        
        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
    
    static private class LocalPropertyKeysIterator implements Iterator<ЗонныйКлюч>
    {
        final Iterator<String> ITERATOR;
        final String URI;
        
        LocalPropertyKeysIterator( Iterator<String> iterator, ЗонныйКлюч тип )
        {
            ITERATOR = iterator;
            URI = тип.ЗОНА;
        }
        
        @Override
        public boolean hasNext()
        {
            return ITERATOR.hasNext();
        }
        
        @Override
        public ЗонныйКлюч next()
        {
            return new ЗонныйКлюч( ITERATOR.next(), URI );
        }
        
        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
    
    //</editor-fold>

}
