package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.Параметризованный;
import com.varankin.brains.db.Внешний;
import com.varankin.brains.db.Коммутируемый;
import com.varankin.brains.db.Коллекция;
import com.varankin.brains.db.Коллективный;
import com.varankin.brains.db.Типовой;
import com.varankin.brains.db.type.DbАтрибутный;
import com.varankin.brains.db.type.DbСоединение;
import com.varankin.brains.db.type.DbФрагмент;
import com.varankin.brains.db.type.DbБиблиотека;
import com.varankin.brains.db.type.DbПараметр;
import com.varankin.brains.db.type.DbПроцессор;
import com.varankin.brains.db.type.DbКлассJava;
import com.varankin.brains.db.type.DbСигнал;
import com.varankin.brains.db.xml.АтрибутныйКлюч;
import com.varankin.brains.db.xml.ЗонныйКлюч;
import com.varankin.brains.db.xml.type.XmlТиповой;
import com.varankin.brains.db.xml.XLinkActuate;
import com.varankin.brains.db.xml.XLinkShow;
import com.varankin.util.LoggerX;

import org.neo4j.graphdb.*;

import java.util.function.Supplier;
import java.util.logging.*;
import java.util.Collections;
import java.util.LinkedList;

import static com.varankin.brains.db.DbПреобразователь.*;
import static com.varankin.brains.db.neo4j.local.Architect.*;
import static com.varankin.brains.db.neo4j.local.NeoЭлемент.КЛЮЧ_А_НАЗВАНИЕ;
import static com.varankin.brains.db.xml.XmlBrains.XML_BRAINS;

/**
 * Атрибутный узел графа в Neo4j.
 * 
 * @author &copy; 2022 Николай Варанкин
 */
abstract class NeoАтрибутный extends NeoNode implements DbАтрибутный 
{
    private static final LoggerX LOGGER = LoggerX.getLogger( NeoАтрибутный.class );
    
    protected NeoАтрибутный( Node node ) 
    {
        super( node );
    }

    protected NeoАтрибутный( ЗонныйКлюч ключ, Node node ) 
    {
        super( node );
        validate( ключ );
    }

    @Override
    public АтрибутныйКлюч тип()
    {
        String name = getNodeName( null );
        String uri = getNodeURI();
        return new АтрибутныйКлюч( name, uri ); //TODO normally get from XmlXXX interfaces
    }

    @Override
    public Iterable<ЗонныйКлюч> ключи( boolean актуальные ) 
    {
        return актуальные ? getPropertyKeys( getNode() ) : Collections.emptyList(); //TODO DEBUG
    }
    
    @Override
    public void определить( String ключ, String uri, Object значение )
    {
        PropertyContainer контейнер = Architect.getInstance().getPropertyContainer( getNode(), uri, true );
        if( контейнер != null )
            if( значение != null )
                контейнер.setProperty( ключ, значение );
            else
                контейнер.removeProperty( ключ );
        else
            LOGGER.log( Level.SEVERE, "002001003S", uri );
    }
    
    @Override
    public final Object атрибут( String ключ, String uri, Object нет ) 
    {
        PropertyContainer контейнер = Architect.getInstance().getPropertyContainer( getNode(), uri, false );
        if( контейнер != null )
            return контейнер.getProperty( ключ, нет );
        else
            return нет;
    }

    /**
     * Возвращает локальное название узла.
     * 
     * @param поиск список пар [название атрибута, URI] для определения названия;
     *          допустимо {@code null}.
     * @return название узла.
     */
    protected String название( String[][] поиск )
    {
        if( поиск != null )
            for( String[] п : поиск )
                if( п != null && п.length == 2 )
                {
                    Object property = атрибут( п[0], п[1], null );
                    if( property != null )
                        return toStringValue( property );
                }
        return toStringValue( атрибут( P_NODE_NAME, null, (String)null ) );
    }
    
    /**
     * Возвращает локальное название узла как именованный тип.
     * 
     * @param поиск список пар [название атрибута, URI] для определения названия;
     *          допустимо {@code null}.
     * @return название узла.
     */
    protected String именованныйТип( String[][] поиск )
    {
        String base = toStringValue( атрибут( P_NODE_NAME, null, (String)null ) );
        if( поиск != null )
            for( String[] п : поиск )
                if( п != null && п.length == 2 )
                {
                    Object property = атрибут( п[0], п[1], null );
                    if( property != null )
                        return base + '(' + toStringValue( property ) + ')';
                }
        return base;
    }
    
    /**
     * Возвращает полное название узла в иерархии узлов.
     * 
     * @param разделитель текст, вставляемый между названиями отдельных объектов иерархии.
     * @param поиск список пар [название атрибута, URI] для определения локального названия;
     *          допустимо {@code null}.
     * @return название объекта.
     */
    protected String положение( String разделитель, String[][] поиск )
    {
        StringBuilder значение = new StringBuilder( именованныйТип( поиск ) );
        значение.insert( 0, разделитель );
        NeoАтрибутный предок = предок();
        if( предок != null )
            значение.insert( 0, предок.положение( разделитель ) );
        return значение.toString();
    }

    @Override
    public String положение( String разделитель )
    {
        return положение( разделитель, null );
    }
    
    @Override
    public boolean восстановимый()
    {
        return getNode().hasRelationship( Recycle.Бывший, Direction.INCOMING );
    }
    
    @Override
    public NeoПакет пакет()
    {
        Node node = предок( P_NODE_NAME, XML_BRAINS );
        return node != null ? new NeoПакет( node ) : null;
    }
    
    @Override
    public final boolean удалить()
    {
        Architect.removeTree( getNode() );
        return true;
    }
    
    @Override
    public final NeoАтрибутный предок()
    {
        return предок( new RelationshipType[0] );
    }
    
    @Override
    public final NeoАтрибутный предок( boolean actual )
    {
        return actual ? предок() : предок( Recycle.Бывший );
    }
    
    final NeoАтрибутный предок( RelationshipType... types )
    {
        NeoАрхив архив = архив();
        LinkedList<Node> parents = new LinkedList<>(); // цепочка предков между архивом и элементом
        for( Node parent = Architect.getParentNode( getNode(), types ); parent != null; 
                parent = Architect.getParentNode( parent ) )
        {
            if( parent.getId() != архив.getNode().getId() ) 
            {
                parents.add( 0, parent );
            }
            else
            {
                // цепочка предков сформирована
                // извлечь кэшированные элементы по образцу
                NeoАтрибутный owner = архив; // пока единственный источник кэшированных потомков
                for( Node child = parents.poll(); child != null; child = parents.poll() )
                {
                    Object found = owner.выполнить( NeoАтрибутный::извлечь_по_образцу, NeoФабрика.создать( child ) );
                    if( found instanceof NeoАтрибутный )
                        owner = (NeoАтрибутный)found;
                    else
                        throw new IllegalStateException( "Collection is supposed to have a sample NeoАтрибутный but it was not extracted." );
                }
                return owner; // это кэшированный элемент
            }
        }
        if( !parents.isEmpty() )
            throw new IllegalStateException( "Archive node was not found." );
        return null;
    }

    /**
     * Возвращает узел, на который ссылается данный узел.
     * 
     * @param <T> тип ожидаемого объекта.
     * @param ссылка ссылка на узел пакета в стандарте XLink.
     * @param класс  класс ожидаемого объекта.
     * @param положение референс на данный объект.
     * @return найденный объект или {@code null}.
     */
    <T extends DbАтрибутный> T xlink( String ссылка, Class<T> класс, String положение )
    {
        NeoПакет пакет = пакет();
        XLinkProcessor p = пакет != null ? пакет.xLinkProcessor() : null;
        Node node = xlink( ссылка, p, положение );
        if( node == null ) return null;
        NeoАтрибутный атр = NeoФабрика.создать( node );
        if( класс.isInstance( атр ) )
        {
            return класс.cast( атр );
        }
        else
        {
            LOGGER.log( Level.WARNING, "002001017W", new Object[]{ 
                    положение, ссылка,
                    атр != null ? атр.getClass().getName() : null, 
                    класс.getName() } );
            return null;
        }
    }

    private static <E> E извлечь_по_образцу( E образец, Коллекция<E> коллекция )
    {
        // извлечь кэшированный объект с тем же Neo4j ID, что и у образца; см. NeoNode.equals(Object)
        return коллекция.stream()
                .filter( e -> e.equals( образец ) )
                .findAny().orElse( null );

    }

    static String trim( String значение )
    {
        return значение == null || значение.trim().isEmpty() ? null : значение.trim();
    }
    
    static char[] trimToCharArray( String значение )
    {
        return значение == null || значение.trim().isEmpty() ? null : значение.trim().toCharArray();
    }
    
    class КоллективныйImpl implements Коллективный
    {
        private final Коллекция<NeoБиблиотека> БИБЛИОТЕКИ;
        private final Коллекция<NeoПроцессор> ПРОЦЕССОРЫ;
        private final Коллекция<NeoФрагмент> ФРАГМЕНТЫ;
        private final Коллекция<NeoСигнал> СИГНАЛЫ;

        КоллективныйImpl( Node node ) 
        {
            БИБЛИОТЕКИ = new КоллекцияПоСвязи<>( node, Связь.Библиотека, n -> new NeoБиблиотека( n ) );
            ПРОЦЕССОРЫ = new КоллекцияПоСвязи<>( node, Связь.Процессор, n -> new NeoПроцессор( n ) );
            ФРАГМЕНТЫ  = new КоллекцияПоСвязи<>( node, Связь.Фрагмент, n -> new NeoФрагмент( n ) );
            СИГНАЛЫ = new КоллекцияПоСвязи<>( node, Связь.Сигнал, n -> new NeoСигнал( n ) );
        }
        
        @Override
        public final Коллекция<DbБиблиотека> библиотеки() 
        {
            return (Коллекция)БИБЛИОТЕКИ;
        }

        @Override
        public final Коллекция<DbПроцессор> процессоры() 
        {
            return (Коллекция)ПРОЦЕССОРЫ;
        }

        @Override
        public final Коллекция<DbФрагмент> фрагменты() 
        {
            return (Коллекция)ФРАГМЕНТЫ;
        }
        
        @Override
        public final Коллекция<DbСигнал> сигналы() 
        {
            return (Коллекция)СИГНАЛЫ;
        }

    }
    
    class КоммутируемыйImpl implements Коммутируемый
    {
        private final Коллекция<NeoСоединение> СОЕДИНЕНИЯ;

        КоммутируемыйImpl( Node node ) 
        {
            СОЕДИНЕНИЯ = new КоллекцияПоСвязи<>( node, Связь.Соединение, n -> new NeoСоединение( n ) );
        }
        
        @Override
        public Коллекция<DbСоединение> соединения() 
        {
            return (Коллекция)СОЕДИНЕНИЯ;
        }

    }
    
    class ПараметризованныйImpl implements Параметризованный
    {
        private final Коллекция<NeoПараметр> ПАРАМЕТРЫ;

        ПараметризованныйImpl( Node node ) 
        {
            ПАРАМЕТРЫ = new КоллекцияПоСвязи<>( node, Связь.Параметр, n -> new NeoПараметр( n ) );
        }
        
        @Override
        public Коллекция<DbПараметр> параметры() 
        {
            return (Коллекция)ПАРАМЕТРЫ;
        }

    }
    
    class ВнешнийImpl implements Внешний
    {
        private final Коллекция<NeoКлассJava> КЛАССЫ;

        ВнешнийImpl( Node node ) 
        {
            КЛАССЫ = new КоллекцияПоСвязи<>( node, Связь.КлассJava, n -> new NeoКлассJava( n ) );
        }
        
        @Override
        public Коллекция<DbКлассJava> классы() 
        {
            return (Коллекция)КЛАССЫ;
        }

    }
    
    class ТиповойImpl<T extends DbАтрибутный> implements Типовой<T>, XmlТиповой
    {
        private final Supplier<T> S;

        ТиповойImpl( Supplier<T> s ) 
        {
            S = s;
        }
        
        @Override
        public T экземпляр() 
        {
            return S.get();
        }
        
        @Override
        public String ссылка()
        {
            return toStringValue( атрибут( КЛЮЧ_А_ССЫЛКА, null ) );
        }

        @Override
        public void ссылка( String значение )
        {
            определить( КЛЮЧ_А_ССЫЛКА, trimToCharArray( значение ) );
        }

        @Override
        public XLinkShow вид()
        {
            String значение = toStringValue( атрибут( КЛЮЧ_А_ВИД, null ) );
            return значение != null ? XLinkShow.valueOf( значение.toUpperCase() ) : null;
        }

        @Override
        public void вид( XLinkShow значение )
        {
            определить( КЛЮЧ_А_ВИД, 
                значение != null ? значение.name().toLowerCase() : null );
        }

        @Override
        public XLinkActuate реализация()
        {
            String значение = toStringValue( атрибут( КЛЮЧ_А_РЕАЛИЗАЦИЯ, null ) );
            return значение != null ? XLinkActuate.valueOf( значение.toUpperCase() ) : null;
        }

        @Override
        public void реализация( XLinkActuate значение )
        {
            определить( КЛЮЧ_А_РЕАЛИЗАЦИЯ, 
                значение != null ? значение.name().toLowerCase() : null );
        }

        String название()
        {
            // мягкое решение проблемы дуальности названия
            Object атрибутТиповой = атрибут( КЛЮЧ_А_НАЗВАНИЕ_Т, null );
            Object атрибутЭлемент = атрибут( КЛЮЧ_А_НАЗВАНИЕ, null );
            return toStringValue( атрибут( КЛЮЧ_А_ССЫЛКА, null ) != null ? 
                    nn( атрибутТиповой, атрибутЭлемент ) : nn( атрибутЭлемент, атрибутТиповой ) );
        }

        void название( String значение )
        {
            определить( КЛЮЧ_А_НАЗВАНИЕ, trimToCharArray( значение ) );
            определить( КЛЮЧ_А_НАЗВАНИЕ_Т, trimToCharArray( значение ) );
        }

    }
    
    static <T> T nn( T t, T v ) { return t != null ? t : v; }
    }
