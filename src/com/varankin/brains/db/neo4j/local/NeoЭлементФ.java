package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.Коллективный;
import com.varankin.brains.db.Коллекция;
import com.varankin.brains.db.type.DbФрагмент;
import com.varankin.brains.db.type.DbБиблиотека;
import com.varankin.brains.db.type.DbПроцессор;
import com.varankin.brains.db.type.DbСигнал;
import com.varankin.brains.db.xml.ЗонныйКлюч;

import org.neo4j.graphdb.Node;

/**
 * Основа элемента мыслительной структуры в Neo4j,
 * обладающего дополнительными свойствами
 * {@link Коллективный}.
 *
 * @author &copy; 2021 Николай Варанкин
 */
class NeoЭлементФ 
        extends NeoЭлемент 
        implements Коллективный
{
    private final КоллективныйImpl КОЛЛЕКТИВНЫЙ;

    protected NeoЭлементФ( ЗонныйКлюч ключ, Node node ) 
    {
        super( ключ, node );
        КОЛЛЕКТИВНЫЙ = new КоллективныйImpl( node );
    }
    
    //<editor-fold defaultstate="collapsed" desc="implements Коллективный">
    
    @Override
    public final Коллекция<DbБиблиотека> библиотеки() 
    {
        return КОЛЛЕКТИВНЫЙ.библиотеки();
    }

    @Override
    public final Коллекция<DbПроцессор> процессоры() 
    {
        return КОЛЛЕКТИВНЫЙ.процессоры();
    }

    @Override
    public final Коллекция<DbФрагмент> фрагменты() 
    {
        return КОЛЛЕКТИВНЫЙ.фрагменты();
    }

    @Override
    public final Коллекция<DbСигнал> сигналы() 
    {
        return КОЛЛЕКТИВНЫЙ.сигналы();
    }

    //</editor-fold>
    
}
