package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.type.DbАтрибутный;
import com.varankin.brains.db.DbОператор;
import com.varankin.brains.db.type.DbТекстовыйБлок;
import com.varankin.brains.db.xml.type.XmlТекстовыйБлок;
import com.varankin.brains.db.xml.АтрибутныйКлюч;
import com.varankin.brains.db.xml.ЗонныйКлюч;
import com.varankin.util.MultiIterable;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import java.util.Arrays;

import static com.varankin.brains.db.DbПреобразователь.*;

/**
 * Блок произвольного текста в Neo4j
 * 
 * @author &copy; 2022 Николай Варанкин
 */
class NeoТекстовыйБлок extends NeoАтрибутный implements DbТекстовыйБлок, XmlТекстовыйБлок
{
    private static final ЗонныйКлюч КЛЮЧ_TEXT   = new ЗонныйКлюч( КЛЮЧ_А_ТЕКСТ.НАЗВАНИЕ, КЛЮЧ_А_ТЕКСТ.ЗОНА );
    private static final ЗонныйКлюч КЛЮЧ_СТРОКА = new ЗонныйКлюч( КЛЮЧ_А_СТРОКА.НАЗВАНИЕ, КЛЮЧ_А_СТРОКА.ЗОНА );

    NeoТекстовыйБлок( GraphDatabaseService сервис )
    {
        this( createNodeInNameSpace( КЛЮЧ_Э_Т_БЛОК, сервис ) );
    }
    
    NeoТекстовыйБлок( Node node ) 
    {
        super( КЛЮЧ_Э_Т_БЛОК, node );
    }

    @Override
    public АтрибутныйКлюч тип() 
    {
        return КЛЮЧ_Э_Т_БЛОК;
    }

    @Override
    public String текст() 
    {
        return toStringValue( атрибут( КЛЮЧ_А_ТЕКСТ, "" ) );
    }

    @Override
    public void текст( String значение )
    {
        определить( КЛЮЧ_А_ТЕКСТ, значение ); // как есть
    }
    
    @Override
    public Long строка()
    {
        return toLongValue( атрибут( КЛЮЧ_А_СТРОКА, null ) );
    }
    
    @Override
    public void строка( Long значение )
    {
        определить( КЛЮЧ_А_СТРОКА, значение );
    }

    @Override
    public Iterable<ЗонныйКлюч> ключи() 
    {
        // Свободный текст не является именованным атрибутом, поэтому его ключ 
        // является служебным и по этой причине отфильтровывается в суперклассе. 
        // Но для данного объекта это легальный атрибут. Чтобы включить его во 
        // все операции, и добавляется предопределенный ключ.
        return new MultiIterable<>( super.ключи(), Arrays.asList( КЛЮЧ_TEXT, КЛЮЧ_СТРОКА ) );
    }

    @Override
    public <X> X выполнить( DbОператор<X> оператор, DbАтрибутный узел )
    {
        throw new UnsupportedOperationException();
    }

}
