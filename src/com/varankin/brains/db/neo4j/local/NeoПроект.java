package com.varankin.brains.db.neo4j.local;

import static com.varankin.brains.db.DbПреобразователь.toStringValue;
import static com.varankin.brains.db.neo4j.local.NeoАтрибутный.trimToCharArray;
import com.varankin.brains.db.type.DbПроект;
import com.varankin.brains.db.xml.type.XmlПроект;
import com.varankin.brains.db.xml.АтрибутныйКлюч;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

/**
 * Мыслительная структура в Neo4j.
 * Состоит из фрагментов, соединенных между собой 
 * сигналами, а также процессоров расчета и библиотек.
 *
 * @author &copy; 2022 Николай Варанкин
 */
final class NeoПроект extends NeoЭлементФ implements DbПроект, XmlПроект
{
    NeoПроект( GraphDatabaseService сервис )
    {
        this( createNodeInNameSpace( КЛЮЧ_Э_ПРОЕКТ, сервис ) );
    }
    
    NeoПроект( Node node )
    {
        super( КЛЮЧ_Э_ПРОЕКТ, node );
    }

    @Override
    public АтрибутныйКлюч тип() 
    {
        return КЛЮЧ_Э_ПРОЕКТ;
    }

    @Override
    public String процессор()
    {
        return toStringValue( атрибут( КЛЮЧ_А_ПРОЦЕССОР, null ) );
    }

    @Override
    public void процессор( String значение )
    {
        определить( КЛЮЧ_А_ПРОЦЕССОР, trimToCharArray( значение ) );
    }

}
