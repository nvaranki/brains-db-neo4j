package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.Коллекция;
import com.varankin.brains.db.type.DbТочка;
import com.varankin.brains.db.xml.type.XmlТочка;
import com.varankin.brains.db.xml.АтрибутныйКлюч;

import org.neo4j.graphdb.*;

import static com.varankin.brains.db.DbПреобразователь.*;

/**
 * Фрагмент схемы расчета в Neo4j
 * Точки в составе данной служат аргументами вычисляемой функции, 
 * которая специфична и определяется в данной точке.
 *
 * @author &copy; 2022 Николай Варанкин
 */
final class NeoТочка extends NeoЭлементВПТ<DbТочка> implements DbТочка, XmlТочка
{
    private final Коллекция<NeoТочка> ТОЧКИ;

    NeoТочка( GraphDatabaseService сервис )
    {
        this( createNodeInNameSpace( КЛЮЧ_Э_ТОЧКА, сервис ) );
    }
    
    NeoТочка( Node node ) 
    {
        super( КЛЮЧ_Э_ТОЧКА, node, DbТочка.class );
        ТОЧКИ = new КоллекцияПоСвязи<>( node, Связь.Точка, NeoТочка::new );
    }

    @Override
    public АтрибутныйКлюч тип() 
    {
        return КЛЮЧ_Э_ТОЧКА;
    }

    @Override
    public Коллекция<DbТочка> точки() 
    {
        return (Коллекция)ТОЧКИ;
    }

    @Override
    public Integer индекс() 
    {
        return toIntegerValue( атрибут( КЛЮЧ_А_ИНДЕКС, null ) );
    }

    @Override
    public void индекс( Integer значение ) 
    {
        определить( КЛЮЧ_А_ИНДЕКС, значение );
    }

    @Override
    public Boolean датчик() 
    {
        return toBooleanValue( атрибут( КЛЮЧ_А_ДАТЧИК, null ) );
    }

    @Override
    public void датчик( Boolean значение ) 
    {
        определить( КЛЮЧ_А_ДАТЧИК, значение );
    }

    @Override
    public Float порог()
    {
        return toFloatValue( атрибут( КЛЮЧ_А_ПОРОГ, null ) );
    }

    @Override
    public void порог( Float значение )
    {
        определить( КЛЮЧ_А_ПОРОГ, значение );
    }

    @Override
    public String контакт()
    {
        return toStringValue( атрибут( КЛЮЧ_А_КОНТАКТ, null ) );
    }

    @Override
    public void контакт( String значение )
    {
        определить( КЛЮЧ_А_КОНТАКТ, trimToCharArray( значение ) );
    }

}
