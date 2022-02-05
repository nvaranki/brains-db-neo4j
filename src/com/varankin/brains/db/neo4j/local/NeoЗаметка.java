package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.Коллекция;
import com.varankin.brains.db.type.DbТекстовыйБлок;
import com.varankin.brains.db.type.DbЗаметка;
import com.varankin.brains.db.type.DbГрафика;
import com.varankin.brains.db.xml.type.XmlЗаметка;
import com.varankin.brains.db.xml.АтрибутныйКлюч;

import org.neo4j.graphdb.*;

import static com.varankin.brains.db.DbПреобразователь.*;

/**
 * Произвольный текст к фрагменту мыслительной структуры в Neo4j.
 *
 * @author &copy; 2022 Николай Варанкин
 */
final class NeoЗаметка extends NeoУзел implements DbЗаметка, XmlЗаметка
{
    private final Коллекция<NeoГрафика> ГРАФИКИ;

    NeoЗаметка( GraphDatabaseService сервис )
    {
        this( createNodeInNameSpace( КЛЮЧ_Э_ЗАМЕТКА, сервис ) );
    }
    
    NeoЗаметка( Node node ) 
    {
        super( КЛЮЧ_Э_ЗАМЕТКА, node );
        ГРАФИКИ = new КоллекцияПоСвязи<>( node, Связь.Графика, NeoГрафика::new );
    }

    @Override
    public АтрибутныйКлюч тип() 
    {
        return КЛЮЧ_Э_ЗАМЕТКА;
    }

    @Override
    public final Коллекция<DbГрафика> графики()
    {
        return (Коллекция)ГРАФИКИ;
    }
    
    @Override
    public String текст( String разделитель ) 
    {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for( DbТекстовыйБлок т : тексты() ) 
            builder.append( i++ > 0 ? разделитель : "" ).append( т.текст() );
        return builder.toString();
    }

    @Override
    public Long глубина()
    {
        return toLongValue( атрибут( КЛЮЧ_А_ГЛУБИНА, null ) );
    }

    @Override
    public void глубина( Long значение )
    {
        определить( КЛЮЧ_А_ГЛУБИНА, значение );
    }
    
}
