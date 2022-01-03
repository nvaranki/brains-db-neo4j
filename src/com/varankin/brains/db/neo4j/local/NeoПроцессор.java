package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.type.DbПроцессор;
import com.varankin.brains.db.xml.type.XmlПроцессор;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.GraphDatabaseService;

import static com.varankin.brains.db.DbПреобразователь.*;

/**
 * Активный элемент мыслительной структуры в Neo4j.
 * Выполняет опрос "засветившихся" датчиков, что, в результате,
 * приводит к расчету когнитивной функции и генерации сигналов.
 *
 * @author &copy; 2021 Николай Варанкин
 */
final class NeoПроцессор 
        extends NeoЭлементВПТ<DbПроцессор> 
        implements DbПроцессор, XmlПроцессор
{
    NeoПроцессор( GraphDatabaseService сервис )
    {
        this( createNodeInNameSpace( КЛЮЧ_Э_ПРОЦЕССОР, сервис ) );
    }
    
    NeoПроцессор( Node node ) 
    {
        super( КЛЮЧ_Э_ПРОЦЕССОР, node, DbПроцессор.class );
    }

    @Override
    public final Long задержка()
    {
        return toLongValue( атрибут( КЛЮЧ_А_ЗАДЕРЖКА, null ) );
    }

    @Override
    public final void задержка( Long значение )
    {
        определить( КЛЮЧ_А_ЗАДЕРЖКА, значение );
    }
    
    @Override
    public final Integer накопление()
    {
        return toIntegerValue( атрибут( КЛЮЧ_А_НАКОПЛЕНИЕ, null ) );
    }

    @Override
    public final void накопление( Integer значение )
    {
        определить( КЛЮЧ_А_НАКОПЛЕНИЕ, значение );
    }
    
    @Override
    public Long пауза() 
    {
        return toLongValue( атрибут( КЛЮЧ_А_ПАУЗА, null ) );
    }

    @Override
    public void пауза( Long значение )
    {
        определить( КЛЮЧ_А_ПАУЗА, значение );
    }

    @Override
    public Boolean рестарт() 
    {
        return toBooleanValue( атрибут( КЛЮЧ_А_РЕСТАРТ, null ) );
    }
    
    @Override
    public void рестарт( Boolean значение )
    {
        определить( КЛЮЧ_А_РЕСТАРТ, значение );
    }

    @Override
    public Boolean сжатие()
    {
        return toBooleanValue( атрибут( КЛЮЧ_А_СЖАТИЕ, null ) );
    }

    @Override
    public void сжатие( Boolean значение )
    {
        определить( КЛЮЧ_А_СЖАТИЕ, значение );
    }
    
    @Override
    public Boolean очистка() 
    {
        return toBooleanValue( атрибут( КЛЮЧ_А_ОЧИСТКА, null ) );
    }
    
    @Override
    public void очистка( Boolean значение )
    {
        определить( КЛЮЧ_А_ОЧИСТКА, значение );
    }

    @Override
    public Стратегия стратегия()
    {
        Стратегия с = toEnumValue( атрибут( КЛЮЧ_А_СТРАТЕГИЯ, null ), Стратегия.class );
        return с != null ? с : Стратегия.НАКОПЛЕНИЕ_ВОЗМУЩЕНИЙ; //TODO move default to appl level
    }

    @Override
    public void стратегия( Стратегия значение )
    {
        определить( КЛЮЧ_А_СТРАТЕГИЯ, значение != null ? значение.ordinal() : null );
    }
    
}
