package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.Параметризованный;
import com.varankin.brains.db.Внешний;
import com.varankin.brains.db.Коллекция;
import com.varankin.brains.db.type.DbПараметр;
import com.varankin.brains.db.type.DbКлассJava;
import com.varankin.brains.db.xml.ЗонныйКлюч;

import org.neo4j.graphdb.Node;

/**
 * Основа элемента мыслительной структуры в Neo4j,
 * обладающего дополнительными свойствами
 * {@link Внешний} и {@link Параметризованный}.
 *
 * @author &copy; 2021 Николай Варанкин
 */
class NeoЭлементВП
        extends NeoЭлемент 
        implements Внешний, Параметризованный
{
    private final ВнешнийImpl ВНЕШНИЙ;
    private final ПараметризованныйImpl ПАРАМЕТРИЗОВАННЫЙ;

    protected NeoЭлементВП( ЗонныйКлюч ключ, Node node ) 
    {
        super( ключ, node );
        ВНЕШНИЙ = new ВнешнийImpl( node );
        ПАРАМЕТРИЗОВАННЫЙ = new ПараметризованныйImpl( node );
    }
    
    //<editor-fold defaultstate="collapsed" desc="implements Внешний">
    
    @Override
    public Коллекция<DbКлассJava> классы() 
    {
        return ВНЕШНИЙ.классы();
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="implements Параметризованный">
    
    @Override
    public Коллекция<DbПараметр> параметры()
    {
        return ПАРАМЕТРИЗОВАННЫЙ.параметры();
    }
    
    //</editor-fold>
    
}
