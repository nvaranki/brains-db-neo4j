package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.Коллекция;
import com.varankin.brains.db.Параметризованный;
import com.varankin.brains.db.Типовой;
import com.varankin.brains.db.type.DbЭлемент;
import com.varankin.brains.db.type.DbПараметр;
import com.varankin.brains.db.xml.XLinkActuate;
import com.varankin.brains.db.xml.XLinkShow;
import com.varankin.brains.db.xml.ЗонныйКлюч;

import org.neo4j.graphdb.Node;

/**
 * Основа элемента мыслительной структуры в Neo4j,
 * обладающего дополнительными свойствами
 * {@link Коммутируемый}, {@link Параметризованный} и {@link Типовой}.
 *
 * @author &copy; 2022 Николай Варанкин
 */
abstract class NeoЭлементКПТ<T extends DbЭлемент> 
        extends NeoЭлементК 
        implements Параметризованный, Типовой<T>
{
    private final ПараметризованныйImpl ПАРАМЕТРИЗОВАННЫЙ;
    private final ТиповойImpl<T> ТИПОВОЙ;

    protected NeoЭлементКПТ( ЗонныйКлюч ключ, Node node, Class<T> класс ) 
    {
        super( ключ, node );
        ПАРАМЕТРИЗОВАННЫЙ = new ПараметризованныйImpl( node );
        ТИПОВОЙ = new ТиповойImpl<>( () -> xlink( ссылка(), класс ) ); 
    }
    
    //<editor-fold defaultstate="collapsed" desc="implements Параметризованный">
    
    @Override
    public Коллекция<DbПараметр> параметры()
    {
        return ПАРАМЕТРИЗОВАННЫЙ.параметры();
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="implements Типовой">
    
    @Override
    public T экземпляр() { return ТИПОВОЙ.экземпляр(); }
    
    @Override
    public String ссылка() { return ТИПОВОЙ.ссылка(); }
    
    @Override
    public void ссылка( String значение ) { ТИПОВОЙ.ссылка( значение ); }
    
    @Override
    public XLinkShow вид() { return ТИПОВОЙ.вид(); }
    
    @Override
    public void вид( XLinkShow значение ) { ТИПОВОЙ.вид( значение ); }
    
    @Override
    public XLinkActuate реализация() { return ТИПОВОЙ.реализация(); }
    
    @Override
    public void реализация( XLinkActuate значение ) { ТИПОВОЙ.реализация( значение ); }
    
    //</editor-fold>
    
}
