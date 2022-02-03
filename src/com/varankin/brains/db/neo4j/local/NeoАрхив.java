package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.Коллекция;
import com.varankin.brains.db.Транзакция;
import com.varankin.brains.db.type.DbАтрибутный;
import com.varankin.brains.db.type.DbПакет;
import com.varankin.brains.db.type.DbЗона;
import com.varankin.brains.db.type.DbМусор;
import com.varankin.brains.db.type.DbАрхив;
import com.varankin.brains.db.xml.type.XmlАрхив;
import com.varankin.brains.db.xml.XmlBrains;
import com.varankin.property.*;
import com.varankin.util.LoggerX;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.*;
import org.neo4j.graphdb.*;

import static com.varankin.brains.db.DbПреобразователь.*;
import static com.varankin.brains.db.neo4j.local.Architect.createArchiveNode;
import static com.varankin.brains.db.neo4j.local.Architect.findSingleNode;
import static com.varankin.brains.db.neo4j.local.NeoАтрибутный.trimToCharArray;
import com.varankin.brains.db.xml.ЗонныйКлюч;

/**
 * Архив мыслительных структур на базе Neo4j.
 *
 * @author &copy; 2022 Николай Варанкин
 */
public final class NeoАрхив extends NeoАтрибутный implements DbАрхив, XmlАрхив
{
    private static final LoggerX LOGGER = LoggerX.getLogger( NeoАрхив.class );
    
    private final Коллекция<NeoПакет> ПАКЕТЫ;
    private final Коллекция<NeoЗона> NAMESPACES;
    private final Коллекция<NeoМусор> КОРЗИНЫ;
    private final FiringPropertyMonitor PCS;
    
    private Consumer<DbАрхив> обработчик;

    /**
     * @param путь расположение хранилища Neo4j в локальной файловой системе.
     * @param кАрх конфигурация индекса архивов.
     * @throws java.lang.Exception при ошибках.
     */
    public NeoАрхив( String путь, Map<String, String> кАрх ) throws Exception
    {
        this( node( Architect.openEmbeddedService( путь, кАрх ) ) );
        Architect.getInstance().registerNewArchive( NeoАрхив.this, t -> 
        { 
            NeoАрхив архив = NeoАрхив.this;
            архив.определить( КЛЮЧ_А_ИЗМЕНЕН, t ); 
            if( обработчик != null ) обработчик.accept( архив );
        } );
        try( final Транзакция т = транзакция() )
        {
            определитьПространствоИмен( XmlBrains.XMLNS_BRAINS, XmlBrains.XML_BRAINS );
            расположение( new File( путь ).getAbsolutePath() );
            т.завершить( true );
        }
    }

    private NeoАрхив( Node node )
    {
        super( node );
        //TODO validate( null/*XmlBrains.XMLNS_BRAINS*/, XmlBrains.XML_ARHIVE );
        ПАКЕТЫ = new КоллекцияПоСвязи<>( node, Связь.Пакет, NeoПакет::new );
        NAMESPACES = new КоллекцияПоСвязи<>( node, Связь.ПространствоИмен, NeoЗона::new );
        КОРЗИНЫ = new КоллекцияПоСвязи<>( node, Связь.Мусор, NeoМусор::new );
        PCS = new SynchronizedPropertyMonitor();
    }

    private static Node node( GraphDatabaseService сервис )
    {
        try( Transaction t = сервис.beginTx() )
        {
            Node node = findSingleNode( сервис, Architect.INDEX_ARCHIVE );
            if( node == null ) node = createArchiveNode( сервис );
            t.success();
            return node;
        }
    }

    @Override
    public ЗонныйКлюч тип() 
    {
        return КЛЮЧ_Э_АРХИВ;
    }

    @Override
    public String название() 
    {
        return toStringValue( атрибут( КЛЮЧ_А_НАЗВАНИЕ, "" ) );
    }
    
    @Override
    public void название( String значение )
    {
        определить( КЛЮЧ_А_НАЗВАНИЕ, trimToCharArray( значение ) );
    }
    
    @Override
    public Коллекция<DbПакет> пакеты()
    {
        return (Коллекция)ПАКЕТЫ;
    }
    
    @Override
    public Коллекция<DbЗона> namespaces()
    {
        return (Коллекция)NAMESPACES;
    }
    
    @Override
    public Коллекция<DbМусор> мусор()
    {
        return (Коллекция)КОРЗИНЫ;
    }
    
    @Override
    public String расположение()
    {
        return toStringValue( атрибут( КЛЮЧ_А_РАСПОЛОЖЕНИЕ, null ) );
    }
    
    private void расположение( String значение )
    {
        определить( КЛЮЧ_А_РАСПОЛОЖЕНИЕ, trimToCharArray( значение ) );
    }
    
    @Override
    public Date создан() 
    {
        Long значение = toLongValue( атрибут( КЛЮЧ_А_СОЗДАН, null ) );
        return значение != null ? new Date( значение ) : null;
    }

    @Override
    public Date изменен() 
    {
        Long значение = toLongValue( атрибут( КЛЮЧ_А_ИЗМЕНЕН, null ) );
        return значение != null ? new Date( значение ) : null;
    }

    @Override
    public void обработчик( Consumer<DbАрхив> значение ) 
    {
        обработчик = значение;
    }

    @Override
    public Collection<PropertyChangeListener> listeners()
    {
        return PCS.listeners();
    }

    @Override
    public Object getPropertyValue( String name ) 
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object setPropertyValue( String name, Object newValue ) 
    {
        switch( name )
        {
            case Коллекция.PROPERTY_UPDATED:
                boolean updated = (Boolean)newValue;
                if( updated )
                {
                    PCS.firePropertyChange( new PropertyChangeEvent( this, 
                            Коллекция.PROPERTY_UPDATED, !updated, updated ) );
                    ПАКЕТЫ.setPropertyValue( name, updated );
                    NAMESPACES.setPropertyValue( name, updated );
                }
                return Boolean.TRUE;
            default:
                throw new UnsupportedOperationException();
        }
    }
    
    @Override
    public DbЗона определитьПространствоИмен( String uri, String префикс )
    {
        if( uri == null || uri.trim().isEmpty() ) return null;
        
        for( NeoЗона ns : NAMESPACES )
            if( uri.equalsIgnoreCase( ns.uri() ) )
            {
                List<String> варианты = new ArrayList( ns.варианты() );
                варианты.removeAll( Collections.singleton( префикс ) );
                if( префикс != null && !префикс.trim().isEmpty() )
                    варианты.add( 0, префикс );
                ns.варианты( варианты );
                return ns;
            }
        
        NeoЗона ns = new NeoЗона( getNode().getGraphDatabase() );
        ns.uri( uri );
        ns.название( префикс );
        if( NAMESPACES.add( ns ) )
            LOGGER.log( Level.FINER, "002001009I", uri );
        else
            LOGGER.log( Level.SEVERE, "002001009S", uri );
        return ns;
    }
    
    @Override
    public DbАтрибутный создатьНовыйЭлемент( ЗонныйКлюч ключ )
    {
        return NeoФабрика.создать( ключ, getNode().getGraphDatabase() );
    }
    
    @Override
    public void закрыть()
    {
        Architect.getInstance().unregisterArchive( this );
    }
    
}
