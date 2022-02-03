package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.Типовой;
import com.varankin.brains.db.type.DbЭлемент;
import com.varankin.brains.db.xml.XLinkActuate;
import com.varankin.brains.db.xml.XLinkShow;
import com.varankin.brains.db.xml.ЗонныйКлюч;

import org.neo4j.graphdb.Node;

/**
 * Основа элемента мыслительной структуры в Neo4j,
 * обладающего дополнительными свойствами
 * {@link Внешний}, {@link Параметризованный} и {@link Типовой}.
 *
 * @author &copy; 2022 Николай Варанкин
 */
abstract class NeoЭлементВПТ<T extends DbЭлемент> 
        extends NeoЭлементВП 
        implements Типовой<T>
{
    private final ТиповойImpl<T> ТИПОВОЙ;

    protected NeoЭлементВПТ( ЗонныйКлюч ключ, Node node, Class<T> класс ) 
    {
        super( ключ, node );
        ТИПОВОЙ = new ТиповойImpl<>( () -> xlink( ссылка(), класс ) ); 
    }

    //<editor-fold defaultstate="collapsed" desc="overrides NeoЭлемент">
    
    @Override
    public String название()
    {
        return ТИПОВОЙ.название();
    }
    
    @Override
    public void название( String значение )
    {
        ТИПОВОЙ.название( значение );
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
