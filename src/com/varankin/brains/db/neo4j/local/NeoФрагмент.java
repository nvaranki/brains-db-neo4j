package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.type.DbФрагмент;
import com.varankin.brains.db.xml.type.XmlФрагмент;

import org.neo4j.graphdb.*;

import static com.varankin.brains.db.type.DbАтрибутный.*;

/**
 * Фрагмент повторно используемой мыслительной структуры в Neo4j.
 * Экземплярами такой структуры могут быть 
 * {@link Модуль}, {@link Расчет}, {@link DbЛента} или {@link Поле}.
 *
 * @author &copy; 2021 Николай Варанкин
 */
final class NeoФрагмент 
        extends NeoЭлементКПТ<DbФрагмент.Экземпляр> 
        implements DbФрагмент, XmlФрагмент
{
    NeoФрагмент( GraphDatabaseService сервис )
    {
        this( createNodeInNameSpace( КЛЮЧ_Э_ФРАГМЕНТ, сервис ) );
    }
    
    NeoФрагмент( Node node ) 
    {
        super( КЛЮЧ_Э_ФРАГМЕНТ, node, DbФрагмент.Экземпляр.class );
    }

    @Override
    public String название() 
    {
        return toStringValue( атрибут( КЛЮЧ_А_НАЗВАНИЕ_Т, null ) );
    }
    
    @Override
    public void название( String значение )
    {
        определить( КЛЮЧ_А_НАЗВАНИЕ_Т, trimToCharArray( значение ) );
    }

    @Override
    public String процессор()
    {
        return toStringValue( атрибут( КЛЮЧ_А_ПРОЦЕССОР, null ) );
    }

    @Override
    public void процессор( String значение )
    {
        определить( КЛЮЧ_А_ПРОЦЕССОР, trimToCharArray( значение ) );
    }

}
