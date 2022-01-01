package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.type.DbТекстовыйБлок;
import com.varankin.brains.db.type.DbУзел;
import com.varankin.brains.db.type.DbИнструкция;
import com.varankin.brains.db.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import static com.varankin.brains.db.neo4j.local.NeoNode.createNodeInNameSpace;
import com.varankin.brains.db.type.DbАтрибутный;
import com.varankin.brains.db.xml.ЗонныйКлюч;

/**
 * Узел графа со стандартными коллекциями в Neo4j.
 *
 * @author &copy; 2021 Николай Варанкин
 */
class NeoУзел extends NeoАтрибутный implements DbУзел
{
    private final Коллекция<NeoИнструкция> ИНСТРУКЦИИ;
    private final Коллекция<NeoТекстовыйБлок> ТЕКСТЫ;
    private final Коллекция<NeoАтрибутный> ПРОЧЕЕ;

    NeoУзел( GraphDatabaseService сервис, ЗонныйКлюч ключ )
    {
        this( createNodeInNameSpace( ключ, сервис ) );
    }
    
    protected NeoУзел( ЗонныйКлюч ключ, Node node ) 
    {
        this( node );
        validate( ключ );
    }
    
    protected NeoУзел( Node node ) 
    {
        super( node );
        ИНСТРУКЦИИ = new КоллекцияПоСвязи<>( node, Связь.Инструкция, NeoИнструкция::new );
        ТЕКСТЫ = new КоллекцияПоСвязи<>( node, Связь.Текст, NeoТекстовыйБлок::new );
        ПРОЧЕЕ = new КоллекцияПоСвязи<>( node, Связь.Прочее, NeoУзел::new );
    }

    @Override
    public final Коллекция<DbИнструкция> инструкции() 
    {
        return (Коллекция)ИНСТРУКЦИИ;
    }
    
    @Override
    public final Коллекция<DbТекстовыйБлок> тексты()
    {
        return (Коллекция)ТЕКСТЫ;
    }
    
    @Override
    public final Коллекция<DbАтрибутный> прочее() 
    {
        return (Коллекция)ПРОЧЕЕ;
    }
    
}
