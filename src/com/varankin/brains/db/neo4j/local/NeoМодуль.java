package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.Коммутируемый;
import com.varankin.brains.db.Коллекция;
import com.varankin.brains.db.type.DbСоединение;
import com.varankin.brains.db.type.DbМодуль;
import com.varankin.brains.db.xml.type.XmlМодуль;
import com.varankin.brains.db.xml.ЗонныйКлюч;

import org.neo4j.graphdb.*;

/**
 * Фрагмент мыслительной структуры в Neo4j.
 * Состоит из фрагментов и соединений, связанных 
 * сигналами, а также локальных библиотек и процессоров.
 *
 * @author &copy; 2022 Николай Варанкин
 */
final class NeoМодуль extends NeoЭлементФ implements DbМодуль, XmlМодуль
{
    private final Коммутируемый КОММУТИРУЕМЫЙ;

    NeoМодуль( GraphDatabaseService сервис )
    {
        this( createNodeInNameSpace( КЛЮЧ_Э_МОДУЛЬ, сервис ) );
    }
    
    NeoМодуль( Node node ) 
    {
        super( КЛЮЧ_Э_МОДУЛЬ, node );
        КОММУТИРУЕМЫЙ = new КоммутируемыйImpl( node );
    }

    @Override
    public ЗонныйКлюч тип() 
    {
        return КЛЮЧ_Э_МОДУЛЬ;
    }

    @Override
    public Коллекция<DbСоединение> соединения() 
    {
        return КОММУТИРУЕМЫЙ.соединения();
    }

}
