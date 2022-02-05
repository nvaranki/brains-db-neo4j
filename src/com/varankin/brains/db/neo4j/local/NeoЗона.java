package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.type.DbАтрибутный;
import com.varankin.brains.db.type.DbЗона;
import com.varankin.brains.db.xml.type.XmlЗона;
import com.varankin.brains.db.xml.АтрибутныйКлюч;
import com.varankin.brains.db.DbОператор;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import static com.varankin.brains.db.DbПреобразователь.toStringValue;

/**
 * Расширенное название пространства имен XML в Neo4j.
 * 
 * @author &copy; 2022 Николай Варанкин
 */
final class NeoЗона extends NeoАтрибутный implements DbЗона, XmlЗона
{
    NeoЗона( GraphDatabaseService сервис )
    {
        this( createNodeInNameSpace( КЛЮЧ_Э_ЗОНА, сервис ) );
    }
    
    NeoЗона( Node node )
    {
        super( КЛЮЧ_Э_ЗОНА, node );
    }
    
    @Override
    public АтрибутныйКлюч тип() 
    {
        return КЛЮЧ_Э_ЗОНА;
    }

    @Override
    public List<String> варианты()
    {
        Object значение = атрибут( КЛЮЧ_А_ВАРИАНТЫ, null );
        if( значение instanceof String[] )
            return Arrays.asList( (String[])значение );
        else if( значение != null )
        {
            String sv = toStringValue( значение );
            return sv != null ? Arrays.asList( sv.split( "\\s+" ) ) : Collections.emptyList();
        }
        else
            return Collections.emptyList();
    }

    @Override
    public void варианты( List<String> значение )
    {
        определить( КЛЮЧ_А_ВАРИАНТЫ, значение == null || значение.isEmpty() ? null : 
                значение.toArray( new String[ значение.size() ] ) );
    }

    @Override
    public String название()
    {
        List<String> варианты = варианты();
        return варианты.isEmpty() ? null : варианты.get( 0 );
    }
    
    @Override
    public void название( String значение )
    {
        List<String> варианты = new ArrayList<>( варианты() );
        варианты.remove( значение );
        if( значение != null && !значение.trim().isEmpty() )
            варианты.add( 0, значение );
        варианты( варианты );
    }

    @Override
    public String uri()
    {
        return toStringValue( атрибут( КЛЮЧ_А_ЗОНА, null ) );
    }
    
    @Override
    public void uri( String значение )
    {
        определить( КЛЮЧ_А_ЗОНА, trimToCharArray( значение ) );
    }
    
    @Override
    public <X> X выполнить( DbОператор<X> оператор, DbАтрибутный узел )
    {
        throw new UnsupportedOperationException();
    }

}
