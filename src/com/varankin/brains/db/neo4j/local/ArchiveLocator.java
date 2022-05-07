package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.xml.type.XmlАрхив;
import com.varankin.util.LoggerX;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.logging.Level;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;

/**
 * Менеджер архивного узла for Neo4j&trade;.
 * База данных хранит единственный экземпляр архивного узла.
 * 
 * @author &copy; 2022 Николай Варанкин
 */
final class ArchiveLocator
{
    private static final LoggerX LOGGER = LoggerX.getLogger( ArchiveLocator.class );
    private static final String INDEX_ARCHIVE = "tmi";
    private static final String INDEX_ARCHIVE_KEY = Architect.P_NODE_NAME;
    private static final String INDEX_ARCHIVE_VALUE = XmlАрхив.КЛЮЧ_Э_АРХИВ.НАЗВАНИЕ;
    private static final ArchiveLocator INSTANCE = new ArchiveLocator();
    
    static ArchiveLocator getInstance()
    {
        return INSTANCE;
    }

    private final Collection<NeoАрхив> АРХИВЫ;
    
    ArchiveLocator()
    {
        АРХИВЫ = new ArrayList<>();
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
    
    void registerNewArchive( NeoАрхив архив )
    {
        GraphDatabaseService сервис = архив.getNode().getGraphDatabase();
        for( NeoАрхив а : АРХИВЫ )
            if( сервис.equals( а.getNode().getGraphDatabase() ) )
                throw new IllegalStateException( "Duplicate archive" );
        АРХИВЫ.add( архив );
    }
    
    /**
     * Открывает доступ к базе данных.
     * 
     * @param сервис расположение хранилища Neo4j в локальной файловой системе.
     * @param кАрх конфигурация индекса архивов.
     * @return узел архива.
     * @throws java.lang.Exception при ошибках.
     */
    static Node obtainArchiveNode( GraphDatabaseService сервис, Map<String, String> кАрх )
    {
        try( Transaction t = сервис.beginTx() )
        {
            IndexManager им = сервис.index();
            Index<Node> индекс = им.existsForNodes( INDEX_ARCHIVE ) ?
                им.forNodes( INDEX_ARCHIVE ) : им.forNodes( INDEX_ARCHIVE, кАрх );
            Node node = Objects.requireNonNullElseGet( 
                    findArchiveNode( индекс ),
                    () -> initArchiveNode( сервис.createNode(), индекс ) );
            t.success();
            return node;
        }
    }
    
    /**
     * Формирует коренной узел данного архива. 
     * 
     * @param node   узел архива.
     * @param index  индекс БД для архивного узла.
     * @return коренной узел архива.
     */
    private static Node initArchiveNode( Node node, Index<Node> индекс )
    {
        node.setProperty( XmlАрхив.КЛЮЧ_А_СОЗДАН.НАЗВАНИЕ, System.currentTimeMillis() );
        node.setProperty( Architect.P_NODE_NAME, XmlАрхив.КЛЮЧ_Э_АРХИВ.НАЗВАНИЕ );
        индекс.add( node, INDEX_ARCHIVE_KEY, INDEX_ARCHIVE_VALUE );
        LOGGER.log( Level.CONFIG, "002001011C" );
        return node;
    }

    /**
     * Ищет коренной узел данного архива. 
     * 
     * @param index  индекс БД для архивного узла.
     * @return коренной узел архива или {code null}.
     */
    private static Node findArchiveNode( Index<Node> индекс )
    {
        IndexHits<Node> hits = индекс.query( INDEX_ARCHIVE_KEY, INDEX_ARCHIVE_VALUE );
        return switch( hits.size() )
        {
            case 0 -> null;
            case 1 -> hits.getSingle();
            default -> throw new IllegalStateException( LOGGER.text( "002001002S", hits.size() ));
        };
    }
    
}
