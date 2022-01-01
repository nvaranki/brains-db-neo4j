package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.type.DbЛента;
import com.varankin.brains.db.xml.type.XmlЛента;
import org.neo4j.graphdb.*;

/**
 * Фрагмент мыслительной структуры с памятью на базе Neo4j.
 * Состоит из ленты времени и внешних соединений.
 *
 * @author &copy; 2021 Николай Варанкин
 */
final class NeoЛента extends NeoЭлементК implements DbЛента, XmlЛента
{
    NeoЛента( GraphDatabaseService сервис )
    {
        this( createNodeInNameSpace( КЛЮЧ_Э_ЛЕНТА, сервис ) );
    }
    
    NeoЛента( Node node )
    {
        super( КЛЮЧ_Э_ЛЕНТА, node );
    }

}
