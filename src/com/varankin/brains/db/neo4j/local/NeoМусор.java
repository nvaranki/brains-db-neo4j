package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.Коллекция;
import com.varankin.brains.db.type.DbАтрибутный;
import com.varankin.brains.db.type.DbМусор;
import com.varankin.brains.db.xml.type.XmlМусор;
import com.varankin.brains.db.xml.АтрибутныйКлюч;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

/**
 * Хранилище удаленных элементов.
 *
 * @author &copy; 2022 Николай Варанкин
 */
final class NeoМусор extends NeoАтрибутный implements DbМусор, XmlМусор
{
    private final Коллекция<NeoАтрибутный> МУСОР;
    
    NeoМусор( GraphDatabaseService сервис )
    {
        this( createNodeInNameSpace( КЛЮЧ_Э_МУСОР, сервис ) );
    }
    
    NeoМусор( Node node )
    {
        super( КЛЮЧ_Э_МУСОР, node );
        МУСОР = new КоллекцияПоСвязи<>( node, Связь.Мусор, NeoФабрика::создать );
    }
    
    @Override
    public АтрибутныйКлюч тип() 
    {
        return КЛЮЧ_Э_МУСОР;
    }

    @Override
    public Коллекция<DbАтрибутный> мусор()
    {
        return (Коллекция)МУСОР;
    }

    /**
     * Перемещает узел из коллекции {@linkplain #мусор() мусор} в 
     * коллекцию прежнего владельца.
     * 
     * @param узел восстанавливаемый узел.
     */
    void вернуть( Node узел )
    {
        //TODO NOT IMPL.
    }
    
}
