package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.Коллекция;
import com.varankin.brains.db.type.DbСенсор;
import com.varankin.brains.db.type.DbПоле;
import com.varankin.brains.db.xml.type.XmlПоле;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

/**
 * Сенсорный фрагмент мыслительной структуры в Neo4j.. 
 * Состоит из внешних соединений, привязанных ко внутренним
 * сигналам - перемычкам или источникам сигналов.
 *
 * @author &copy; 2021 Николай Варанкин
 */
final class NeoПоле extends NeoЭлементК implements DbПоле, XmlПоле
{
    private final Коллекция<NeoСенсор> СЕНСОРЫ;

    NeoПоле( GraphDatabaseService сервис )
    {
        this( createNodeInNameSpace( КЛЮЧ_Э_ПОЛЕ, сервис ) );
    }
    
    NeoПоле( Node node )
    {
        super( КЛЮЧ_Э_ПОЛЕ, node );
        СЕНСОРЫ = new КоллекцияПоСвязи<>( node, Связь.Сигнал, NeoСенсор::new );
    }

    @Override
    public Коллекция<DbСенсор> сенсоры()
    {
        return (Коллекция)СЕНСОРЫ;
    }

}
