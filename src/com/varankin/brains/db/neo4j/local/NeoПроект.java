package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.type.DbПроект;
import com.varankin.brains.db.xml.type.XmlПроект;
import com.varankin.brains.db.xml.ЗонныйКлюч;

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
    public ЗонныйКлюч тип() 
    {
        return КЛЮЧ_Э_ПРОЕКТ;
    }

}
