package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.type.DbПараметр;
import com.varankin.brains.db.xml.type.XmlПараметр;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import static com.varankin.brains.db.DbПреобразователь.*;

/**
 * Параметр настройки экземпляра {@linkplain Object объекта} в Neo4j.
 * Это одиночный скаляр, массив из однородных скаляров/массивов или
 * их произвольная комбинация (структура). Используется для
 * параметризации элементов и прочих объектов Java.
 *
 * @author &copy; 2021 Николай Варанкин
 */
final class NeoПараметр extends NeoЭлементВП implements DbПараметр, XmlПараметр
{
    NeoПараметр( GraphDatabaseService сервис )
    {
        this( createNodeInNameSpace( КЛЮЧ_Э_ПАРАМЕТР, сервис ) );
    }
    
    NeoПараметр( Node node )
    {
        super( КЛЮЧ_Э_ПАРАМЕТР, node );
    }

    @Override
    public String индекс()
    {
        return toStringValue( атрибут( КЛЮЧ_А_ИНДЕКС, null ) );
    }

    @Override
    public void индекс( String значение )
    {
        определить( КЛЮЧ_А_ИНДЕКС, trimToCharArray( значение ) );
    }

    @Override
    public Integer приоритет()
    {
        return toIntegerValue( атрибут( КЛЮЧ_А_ПРИОРИТЕТ, null ) );
    }
    
    @Override
    public void приоритет( Integer значение )
    {
        определить( КЛЮЧ_А_ПРИОРИТЕТ, значение );
    }

}
