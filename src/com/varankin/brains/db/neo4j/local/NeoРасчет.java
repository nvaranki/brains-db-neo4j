package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.Коллекция;
import com.varankin.brains.db.type.DbРасчет;
import com.varankin.brains.db.type.DbТочка;
import com.varankin.brains.db.xml.type.XmlРасчет;

import org.neo4j.graphdb.*;

/**
 * Вычислимый фрагмент мыслительной структуры в Neo4j.
 * Состоит из одной или нескольких схем расчета и 
 * внешних соединений, связанных сигналами.
 *
 * @author &copy; 2021 Николай Варанкин
 */
final class NeoРасчет extends NeoЭлементК implements DbРасчет, XmlРасчет
{
    private final Коллекция<NeoТочка> ТОЧКИ;

    NeoРасчет( GraphDatabaseService сервис )
    {
        this( createNodeInNameSpace( КЛЮЧ_Э_РАСЧЕТ, сервис ) );
    }
    
    NeoРасчет( Node node ) 
    {
        super( КЛЮЧ_Э_РАСЧЕТ, node );
        ТОЧКИ = new КоллекцияПоСвязи<>( node, Связь.Точка, NeoТочка::new );
    }

    @Override
    public Коллекция<DbТочка> точки() 
    {
        return (Коллекция)ТОЧКИ;
    }

}
