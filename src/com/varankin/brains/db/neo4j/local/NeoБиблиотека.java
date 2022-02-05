package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.Коллекция;
import com.varankin.brains.db.type.*;
import com.varankin.brains.db.xml.type.XmlБиблиотека;
import com.varankin.brains.db.xml.АтрибутныйКлюч;

import org.neo4j.graphdb.*;

/**
 * Набор произвольных модулей, функций, классов и процессоров мыслительной 
 * структуры в Neo4j.
 * 
 * @author &copy; 2022 Николай Варанкин
 */
final class NeoБиблиотека extends NeoЭлемент implements DbБиблиотека, XmlБиблиотека
{
    private final Коллекция<NeoПоле> ПОЛЯ;
    private final Коллекция<NeoМодуль> МОДУЛИ;
    private final Коллекция<NeoРасчет> РАСЧЕТЫ;
    private final Коллекция<NeoЛента> ЛЕНТЫ;
    private final Коллекция<NeoКлассJava> КЛАССЫ;
    private final Коллекция<NeoПроцессор> ПРОЦЕССОРЫ;
    private final Коллекция<NeoТочка> ТОЧКИ;
    private final Коллекция<NeoСенсор> СЕНСОРЫ;

    NeoБиблиотека( GraphDatabaseService сервис )
    {
        this( createNodeInNameSpace( КЛЮЧ_Э_БИБЛИОТЕКА, сервис ) );
    }
    
    NeoБиблиотека( Node node ) 
    {
        super( КЛЮЧ_Э_БИБЛИОТЕКА, node );
        ПОЛЯ = new КоллекцияПоСвязи<>( node, Связь.Поле, NeoПоле::new );
        МОДУЛИ = new КоллекцияПоСвязи<>( node, Связь.Модуль, NeoМодуль::new );
        РАСЧЕТЫ = new КоллекцияПоСвязи<>( node, Связь.Расчет, NeoРасчет::new );
        ЛЕНТЫ = new КоллекцияПоСвязи<>( node, Связь.Лента, NeoЛента::new );
        КЛАССЫ = new КоллекцияПоСвязи<>( node, Связь.КлассJava, NeoКлассJava::new );
        ПРОЦЕССОРЫ = new КоллекцияПоСвязи<>( node, Связь.Процессор, NeoПроцессор::new );
        ТОЧКИ = new КоллекцияПоСвязи<>( node, Связь.Точка, NeoТочка::new );
        СЕНСОРЫ = new КоллекцияПоСвязи<>( node, Связь.Сенсор, NeoСенсор::new );
    }

    @Override
    public АтрибутныйКлюч тип() 
    {
        return КЛЮЧ_Э_БИБЛИОТЕКА;
    }

    @Override
    public Коллекция<DbПоле> поля() 
    {
        return (Коллекция)ПОЛЯ;
    }

    @Override
    public Коллекция<DbМодуль> модули() 
    {
        return (Коллекция)МОДУЛИ;
    }

    @Override
    public Коллекция<DbРасчет> расчеты() 
    {
        return (Коллекция)РАСЧЕТЫ;
    }

    @Override
    public Коллекция<DbЛента> ленты()
    {
        return (Коллекция)ЛЕНТЫ;
    }

    @Override
    public Коллекция<DbПроцессор> процессоры() 
    {
        return (Коллекция)ПРОЦЕССОРЫ;
    }

    @Override
    public Коллекция<DbТочка> точки()
    {
        return (Коллекция)ТОЧКИ;
    }

    @Override
    public Коллекция<DbСенсор> сенсоры()
    {
        return (Коллекция)СЕНСОРЫ;
    }

    @Override
    public Коллекция<DbКлассJava> классы()
    {
        return (Коллекция)КЛАССЫ;
    }
    
}
