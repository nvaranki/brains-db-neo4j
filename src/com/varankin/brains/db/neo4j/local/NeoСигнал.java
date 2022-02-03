package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.type.DbСигнал;
import com.varankin.brains.db.xml.type.XmlСигнал;
import com.varankin.brains.db.xml.ЗонныйКлюч;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import static com.varankin.brains.db.DbПреобразователь.*;

/**
 * Сигнал, формируемый по результату расчета когнитивной функции, в Neo4j.
 * 
 * @author &copy; 2022 Николай Варанкин
 */
final class NeoСигнал extends NeoЭлемент implements DbСигнал, XmlСигнал
{
    NeoСигнал( GraphDatabaseService сервис )
    {
        this( createNodeInNameSpace( КЛЮЧ_Э_СИГНАЛ, сервис ) );
    }
    
    NeoСигнал( Node node ) 
    {
        super( КЛЮЧ_Э_СИГНАЛ, node );
    }

    @Override
    public ЗонныйКлюч тип() 
    {
        return КЛЮЧ_Э_СИГНАЛ;
    }

    @Override
    public Приоритет приоритет()
    {
        String значение = toStringValue( атрибут( КЛЮЧ_А_ПРИОРИТЕТ, null ) );
        Приоритет п;
        if( ПРИОРИТЕТ_ВОЗРАСТАНИЕ.equals( значение ) ) п = Приоритет.ВОЗРАСТАНИЕ;
        else if( ПРИОРИТЕТ_УБЫВАНИЕ.equals( значение ) ) п = Приоритет.УБЫВАНИЕ;
        else п = Приоритет.НЕТ;
        return п;
    }
    
    @Override
    public void приоритет( Приоритет значение )
    {
        if( значение == null )
            определить( КЛЮЧ_А_ПРИОРИТЕТ, null );
        else
            switch( значение )
            {
                case ВОЗРАСТАНИЕ:
                    определить( КЛЮЧ_А_ПРИОРИТЕТ, ПРИОРИТЕТ_ВОЗРАСТАНИЕ.toCharArray() );
                    break;
                case УБЫВАНИЕ:
                    определить( КЛЮЧ_А_ПРИОРИТЕТ, ПРИОРИТЕТ_УБЫВАНИЕ.toCharArray() );
                    break;
                default:
                    определить( КЛЮЧ_А_ПРИОРИТЕТ, null );
            }
    }

}
