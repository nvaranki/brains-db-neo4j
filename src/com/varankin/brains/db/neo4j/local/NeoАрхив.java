package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.Коллекция;
import com.varankin.brains.db.type.DbАтрибутный;
import com.varankin.brains.db.type.DbПакет;
import com.varankin.brains.db.type.DbЗона;
import com.varankin.brains.db.type.DbМусор;
import com.varankin.brains.db.type.DbАрхив;
import com.varankin.brains.db.xml.type.XmlАрхив;
import com.varankin.brains.db.xml.АтрибутныйКлюч;
import com.varankin.brains.db.xml.ЗонныйКлюч;
import com.varankin.property.*;
import com.varankin.util.LoggerX;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.*;
import java.util.logging.*;

import org.neo4j.graphdb.*;

import static com.varankin.brains.db.DbПреобразователь.*;
import static com.varankin.brains.db.neo4j.local.NeoАтрибутный.trimToCharArray;

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
    private final String РАСПОЛОЖЕНИЕ;
    private final FiringPropertyMonitor PCS; //TODO review the purpose
    
    /**
     * @param путь расположение хранилища Neo4j в локальной файловой системе.
     * @param кАрх конфигурация индекса архивов.
     * @throws java.lang.Exception при ошибках.
     */
    public NeoАрхив( File путь, Map<String, String> кАрх ) throws Exception
    {
        this( ArchiveLocator.obtainArchiveNode( Architect.openEmbeddedService( путь ), кАрх ), 
               путь.getAbsolutePath(), null );
        Architect.registerTransactionEventHandler( getNode().getGraphDatabase(), this::изменен );
        ArchiveLocator.getInstance().registerNewArchive( NeoАрхив.this );
    }

    private NeoАрхив( Node node, String расположение, Object java15_0_1_defect ) //TODO java15_0_1_defect, see ОткрытьЛокальныйАрхивNeo4j
    {
        super( node );
        ПАКЕТЫ = new КоллекцияПоСвязи<>( node, Связь.Пакет, NeoПакет::new );
        NAMESPACES = new КоллекцияПоСвязи<>( node, Связь.ПространствоИмен, NeoЗона::new );
        КОРЗИНЫ = new КоллекцияПоСвязи<>( node, Связь.Мусор, NeoМусор::new );
        РАСПОЛОЖЕНИЕ = расположение;
        PCS = new SynchronizedPropertyMonitor();
    }
    
    @Override
    public АтрибутныйКлюч тип() 
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
        return РАСПОЛОЖЕНИЕ;
    }
    
    @Override
    public String отметка()
    {
        /* Обычно узел архива получает ID=0 при создании нового архива. Это делает 
         * архивы неразличимыми между собой. Расположение архива в контексте приложения
         * дает возможность как различить разные БД, так и идентифицировать совпадения. */
        return РАСПОЛОЖЕНИЕ;
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

    /**
     * Регистрирует момент изменения содержимого архива.
     * 
     * @param момент момент времени изменения, в мс.
     */
    private void изменен( Long момент )
    {
        определить( КЛЮЧ_А_ИЗМЕНЕН, момент );
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
        ArchiveLocator.getInstance().unregisterArchive( this );
    }
    
}
