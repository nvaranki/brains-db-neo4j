package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.Коммутируемый;
import com.varankin.brains.db.type.DbСоединение;
import com.varankin.brains.db.Коллекция;
import com.varankin.brains.db.xml.ЗонныйКлюч;

import org.neo4j.graphdb.Node;

/**
 * Основа элемента мыслительной структуры в Neo4j,
 * обладающего дополнительными свойствами
 * {@link Коммутируемый}.
 *
 * @author &copy; 2021 Николай Варанкин
 */
class NeoЭлементК 
        extends NeoЭлемент 
        implements Коммутируемый
{
    private final КоммутируемыйImpl КОММУТИРУЕМЫЙ;

    protected NeoЭлементК( ЗонныйКлюч ключ, Node node ) 
    {
        super( ключ, node );
        КОММУТИРУЕМЫЙ = new КоммутируемыйImpl( node );
    }
    
    //<editor-fold defaultstate="collapsed" desc="implements Коммутируемый">
    
    @Override
    public Коллекция<DbСоединение> соединения()
    {
        return КОММУТИРУЕМЫЙ.соединения();
    }
    
    //</editor-fold>
    
}
