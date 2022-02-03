package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.type.DbСенсор;
import com.varankin.brains.db.xml.type.XmlСенсор;
import com.varankin.brains.db.xml.ЗонныйКлюч;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

/**
 * Сигнал, формируемый по результату расчета когнитивной функции, в Neo4j.
 * 
 * @author &copy; 2022 Николай Варанкин
 */
final class NeoСенсор extends NeoЭлементВПТ<DbСенсор> implements DbСенсор, XmlСенсор
{
    //private static final КороткийКлюч КЛЮЧ_А_ПРИОРИТЕТ = new КороткийКлюч( XML_PRIORITY, XMLNS_BRAINS );

    NeoСенсор( GraphDatabaseService сервис )
    {
        this( createNodeInNameSpace( КЛЮЧ_Э_СЕНСОР, сервис ) );
    }
    
    NeoСенсор( Node node ) 
    {
        super( КЛЮЧ_Э_СЕНСОР, node, DbСенсор.class );
    }

    @Override
    public ЗонныйКлюч тип() 
    {
        return КЛЮЧ_Э_СЕНСОР;
    }

//    @Override
//    public Приоритет приоритет()
//    {
//        String значение = toStringValue( атрибут( КЛЮЧ_А_ПРИОРИТЕТ, null ) );
//        Приоритет п;
//        if( ПРИОРИТЕТ_ВОЗРАСТАНИЕ.equals( значение ) ) п = Приоритет.ВОЗРАСТАНИЕ;
//        else if( ПРИОРИТЕТ_УБЫВАНИЕ.equals( значение ) ) п = Приоритет.УБЫВАНИЕ;
//        else п = Приоритет.НЕТ;
//        return п;
//    }
//
//    @Override
//    public void приоритет( Приоритет значение )
//    {
//        if( значение == null )
//            определить( КЛЮЧ_А_ПРИОРИТЕТ, null );
//        else
//            switch( значение )
//            {
//                case ВОЗРАСТАНИЕ:
//                    определить( КЛЮЧ_А_ПРИОРИТЕТ, ПРИОРИТЕТ_ВОЗРАСТАНИЕ.toCharArray() );
//                    break;
//                case УБЫВАНИЕ:
//                    определить( КЛЮЧ_А_ПРИОРИТЕТ, ПРИОРИТЕТ_УБЫВАНИЕ.toCharArray() );
//                    break;
//                default:
//                    определить( КЛЮЧ_А_ПРИОРИТЕТ, null );
//            }
//    }
    
}
