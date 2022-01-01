package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.Коллекция;
import com.varankin.brains.db.type.DbПакет;
import com.varankin.brains.db.type.DbБиблиотека;
import com.varankin.brains.db.type.DbПроект;
import com.varankin.brains.db.xml.type.XmlПакет;

import org.neo4j.graphdb.*;

import static com.varankin.brains.db.type.DbАтрибутный.toStringValue;

/**
 * Пакет из проектов и библиотек мыслительных структур на базе Neo4j.
 * Используется при обмене с внешними системами.
 *
 * @author &copy; 2021 Николай Варанкин
 */
final class NeoПакет extends NeoУзел implements DbПакет, XmlПакет
{
    private final Коллекция<NeoПроект> ПРОЕКТЫ;
    private final Коллекция<NeoБиблиотека> БИБЛИОТЕКИ;
    private final XLinkProcessor XLINK_PROCESSOR;

    NeoПакет( GraphDatabaseService сервис )
    {
        this( createNodeInNameSpace( КЛЮЧ_Э_ПАКЕТ, сервис ) );
    }
    
    NeoПакет( Node node )
    {
        super( КЛЮЧ_Э_ПАКЕТ, node );
        ПРОЕКТЫ = new КоллекцияПоСвязи<>( node, Связь.Проект, NeoПроект::new );
        БИБЛИОТЕКИ = new КоллекцияПоСвязи<>( node, Связь.Библиотека, NeoБиблиотека::new );
        XLINK_PROCESSOR = new XLinkProcessor( node );
    }
    
    @Override
    public Коллекция<DbПроект> проекты()
    {
        return (Коллекция)ПРОЕКТЫ;
    }
    
    @Override
    public Коллекция<DbБиблиотека> библиотеки()
    {
        return (Коллекция)БИБЛИОТЕКИ;
    }
    
    @Override
    public String версия()
    {
        return toStringValue( атрибут( КЛЮЧ_А_ВЕРСИЯ, null ) );
    }
    
    void версия( String значение )
    {
        определить( КЛЮЧ_А_ВЕРСИЯ, trimToCharArray( значение ) );
    }

    @Override
    public void название( String значение )
    {
        определить( КЛЮЧ_А_НАЗВАНИЕ, trimToCharArray( значение ) );
    }

    @Override
    public String название()
    {
        return toStringValue( атрибут( КЛЮЧ_А_НАЗВАНИЕ, null ) );
    }
    
    /**
     * @return процессор поиска узлов пакета по ссылке в формате XLink.
     */
    final XLinkProcessor xLinkProcessor()
    {
        return XLINK_PROCESSOR;
    }

}
