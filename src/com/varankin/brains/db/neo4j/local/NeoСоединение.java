package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.Коллекция;
import com.varankin.brains.db.type.DbСоединение;
import com.varankin.brains.db.type.DbКонтакт;
import com.varankin.brains.db.xml.type.XmlСоединение;
import com.varankin.brains.db.xml.АтрибутныйКлюч;

import org.neo4j.graphdb.*;

/**
 * Блок контактов фрагмента мыслительной структуры в Neo4j.
 * Соединения могут дублировать или дополнять друг друга. 
 * Они различаются по {@linkplain #название() названию}.
 *
 * @author &copy; 2022 Николай Варанкин
 */
final class NeoСоединение extends NeoЭлемент implements DbСоединение, XmlСоединение
{
    private final Коллекция<NeoКонтакт> КОНТАКТЫ;

    NeoСоединение( GraphDatabaseService сервис )
    {
        this( createNodeInNameSpace( КЛЮЧ_Э_СОЕДИНЕНИЕ, сервис ) );
    }
    
    NeoСоединение( Node node ) 
    {
        super( КЛЮЧ_Э_СОЕДИНЕНИЕ, node );
        КОНТАКТЫ = new КоллекцияПоСвязи<>( node, Связь.Контакт, NeoКонтакт::new );
    }

    @Override
    public АтрибутныйКлюч тип() 
    {
        return КЛЮЧ_Э_СОЕДИНЕНИЕ;
    }

    @Override
    public Коллекция<DbКонтакт> контакты() 
    {
        return (Коллекция)КОНТАКТЫ;
    }
    
}
