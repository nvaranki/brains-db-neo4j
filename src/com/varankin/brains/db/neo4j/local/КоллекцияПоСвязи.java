package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.Коллекция;
import com.varankin.brains.db.xml.XmlBrains;
import com.varankin.characteristic.Изменение;
import com.varankin.characteristic.Наблюдатель;
import com.varankin.property.FiringPropertyMonitor;
import com.varankin.property.SynchronizedPropertyMonitor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Function;
import org.neo4j.graphdb.*;

/**
 * Виртуальная {@linkplain Коллекция коллекция} элементов базы данных 
 * по типу связи в Neo4j.
 * Состав коллекции определяется в момент вызова метода 
 * {@link #iterator() } или {@link #size() }.
 *
 * @author &copy; 2021 Николай Варанкин
 * 
 * @param <E> класс элемента коллекции.
 */
final class КоллекцияПоСвязи<E extends NeoNode> 
    extends AbstractCollection<E>
    implements Коллекция<E>
{
    private final Node УЗЕЛ;
    private final RelationshipType ТИП;
    private final boolean МУСОР;
    private final FiringPropertyMonitor PCS;
    private final Map<Long,WeakReference<E>> КЭШ;
    private final Function<Node,E> ФАБРИКА;
    private final Collection<Наблюдатель<E>> НАБЛЮДАТЕЛИ;

    /**
     * @param узел узел, к которому относится данная коллекция.
     * @param тип  тип связи от данного узла к элементам коллекции.
     * @param фабрика фабрика элементов по узлу.
     */
    КоллекцияПоСвязи( Node узел, RelationshipType тип, Function<Node,E> фабрика ) 
    {
        УЗЕЛ = узел;
        ТИП = тип;
        ФАБРИКА = фабрика;
        PCS = new SynchronizedPropertyMonitor();
        НАБЛЮДАТЕЛИ = Collections.synchronizedSet( new LinkedHashSet<>() );
        КЭШ = new HashMap<>();
        МУСОР = Objects.equals( тип, Связь.Мусор );
    }
    
    @Override
    public final Iterator<E> iterator() 
    {
        return new IteratorImpl( getRelationships().iterator() );
    }
    
    @Override
    public boolean add( E e )
    {
        Node node = e.getNode();
        if( Architect.testNodeToParent( node, УЗЕЛ, ТИП ) )
        {
            Architect.linkNodeToParent( node, УЗЕЛ, ТИП );
            КЭШ.put( node.getId(), new WeakReference( e ) );
            if( !МУСОР )
            {
                // снять факт отката удаления путем отвязывания от прежнего
                // владельца по специальному типу связи
                Architect.unlinkNodeFromParent( node, УЗЕЛ, Recycle.Бывший );
            }
            информировать( null, e );
            return true;
        }
        else
        {
            return false;
        }
    }
    
    private boolean remove( E e )
    {
        Node node = e.getNode();
        КЭШ.remove( node.getId() );
        Architect.unlinkNodeFromParent( node, УЗЕЛ, ТИП );
        if( !МУСОР )
        {
            // пометить факт удаления (для отката) путем связывания с прежним
            // владельцем, но специальным типом связи
            Relationship r = Architect.linkNodeToParent( node, УЗЕЛ, Recycle.Бывший );
            r.setProperty( XmlBrains.XML_DELETED, System.currentTimeMillis() );
        }
        // информировать об изменении коллекции
        информировать( e, null );
        return true;
    }
    
    @Override
    public boolean remove( Object o )
    {
        return o instanceof NeoNode ? remove( (E)o ) : false; //TODO cast
    }

    @Override
    public final int size() 
    {
        int s = 0;
        for( Relationship __ : getRelationships() ) s++;
        return s;
    }

    private Iterable<Relationship> getRelationships() 
    {
        return ТИП != null ? 
                УЗЕЛ.getRelationships( ТИП, Direction.OUTGOING ) : 
                УЗЕЛ.getRelationships( Direction.OUTGOING );
    }
    
    @Override
    public Collection<PropertyChangeListener> listeners()
    {
        return PCS.listeners();
    }

    @Override
    public final Object getPropertyValue( String name )
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public final Object setPropertyValue( String name, Object newValue )
    {
        switch( name )
        {
            case Коллекция.PROPERTY_UPDATED:
                boolean updated = (Boolean)newValue;
                if( updated )
                    PCS.firePropertyChange( new PropertyChangeEvent( this, 
                            Коллекция.PROPERTY_UPDATED, !updated, updated ) );
                return Boolean.FALSE;
                
            case Коллекция.PROPERTY_ADDED:
                PCS.firePropertyChange( new PropertyChangeEvent( this, 
                            Коллекция.PROPERTY_ADDED, null, newValue ) );
                return null;
                
            case Коллекция.PROPERTY_REMOVED:
                PCS.firePropertyChange( new PropertyChangeEvent( this, 
                            Коллекция.PROPERTY_REMOVED, newValue, null ) );
                return newValue;
                
            default:
                throw new IllegalArgumentException( String.valueOf( name ) ); //return null;
        }
    }

    @Override
    public Collection<Наблюдатель<E>> наблюдатели()
    {
        return НАБЛЮДАТЕЛИ;
    }

    private class IteratorImpl implements Iterator<E>
    {
        
        final Iterator<Relationship> ITERATOR;
        E e;

        IteratorImpl( Iterator<Relationship> iterator ) 
        {
            ITERATOR = iterator;
        }

        @Override
        public boolean hasNext() 
        {
            return ITERATOR.hasNext();
        }

        @Override
        public E next() 
        {
            Node next = ITERATOR.next().getEndNode();
            long id = next.getId();
            WeakReference<E> wr = КЭШ.get( id );
            e = wr != null ? wr.get() : null;
            if( e == null )
                КЭШ.put( id, new WeakReference( e = ФАБРИКА.apply( next ) ) );
            return e;
        }

        @Override
        public void remove() 
        {
            if( e == null ) throw new IllegalStateException();
            E узел = e;
            e = null;
            // исключить узел из коллекции
            КоллекцияПоСвязи.this.remove( узел );
        }
        
    }
    
    private void информировать( E было, E стало )
    {
        String p = 
                было != null && стало == null ? Коллекция.PROPERTY_REMOVED : 
                было == null && стало != null ? Коллекция.PROPERTY_ADDED : 
                /* все остальные случаи      */ Коллекция.PROPERTY_UPDATED;
        PCS.firePropertyChange( new PropertyChangeEvent( this, p, было, стало ) );
        Изменение<E> изменение = new Изменение<>( было, стало );
        НАБЛЮДАТЕЛИ.forEach( н -> н.отклик( изменение ) );
    }

}
