package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.Коллекция;
import com.varankin.brains.db.type.DbЭлемент;
import com.varankin.brains.db.type.DbЗаметка;
import com.varankin.brains.db.type.DbГрафика;
import com.varankin.brains.db.xml.type.XmlЭлемент;
import com.varankin.brains.db.xml.XLink;
import com.varankin.brains.db.xml.ЗонныйКлюч;

import java.util.*;
import org.neo4j.graphdb.*;

import static com.varankin.brains.db.DbПреобразователь.*;

/**
 * Основа элемента мыслительной структуры в Neo4j.
 *
 * @author &copy; 2021 Николай Варанкин
 */
abstract class NeoЭлемент extends NeoУзел implements DbЭлемент, XmlЭлемент
{
    protected static final String[][] ПОИСК_НАЗВАНИЯ 
        = { { КЛЮЧ_А_НАЗВАНИЕ.НАЗВАНИЕ, null }, { XLink.XLINK_TITLE, XLink.XMLNS_XLINK } }; //TODO , { Xml.P_NODE_NAME, null }

    private final Коллекция<NeoЗаметка> ЗАМЕТКИ;
    private final Коллекция<NeoГрафика> ГРАФИКИ;
    
    protected NeoЭлемент( ЗонныйКлюч ключ, Node node ) 
    {
        super( ключ, node );
        ЗАМЕТКИ = new КоллекцияПоСвязи<>( node, Связь.Заметка, NeoЗаметка::new );
        ГРАФИКИ = new КоллекцияПоСвязи<>( node, Связь.Графика, NeoГрафика::new );
    }

    @Override
    public final Коллекция<DbЗаметка> заметки() 
    {
        return (Коллекция)ЗАМЕТКИ;
    }
    
    @Override
    public final Коллекция<DbГрафика> графики()
    {
        return (Коллекция)ГРАФИКИ;
    }
    
    @Override
    public String название() 
    {
        return toStringValue( атрибут( КЛЮЧ_А_НАЗВАНИЕ, "" ) );
    }
    
    @Override
    public void название( String значение )
    {
        определить( КЛЮЧ_А_НАЗВАНИЕ, trimToCharArray( значение ) );
    }
    
    /**
     * Возвращает полное название элемента в составе иерархии.
     * 
     * @param разделитель текст, вставляемый между названиями отдельных объектов иерархии.
     * @return название объекта.
     */
    @Override
    public String положение( String разделитель )
    {
        return положение( разделитель, ПОИСК_НАЗВАНИЯ );
    }

    @Override
    public Collection<String> сборки()
    {
        Object значение = атрибут( КЛЮЧ_А_СБОРКИ, null );
        if( значение instanceof String[] )
            return Arrays.asList( (String[])значение );
        else if( значение != null )
        {
            String sv = toStringValue( значение );
            return sv != null ? Arrays.asList( sv.split( "\\s*,\\s*" ) ) : Collections.emptyList();
        }
        else
            return Collections.emptyList();
    }

    @Override
    public void сборки( Collection<String> значение )
    {
        определить( КЛЮЧ_А_СБОРКИ, значение == null || значение.isEmpty() ? null :
            значение.toArray( new String[значение.size()] ) );
    }

    /**
     * Возвращает узел, на который ссылается данный узел.
     * 
     * @param <T> тип ожидаемого объекта.
     * @param ссылка ссылка на узел пакета в стандарте XLink.
     * @param класс  класс ожидаемого объекта.
     * @return найденный объект или {@code null}.
     */
    <T extends DbЭлемент> T xlink( String ссылка, Class<T> класс )
    {
        return xlink( ссылка, класс, положение( ".", ПОИСК_НАЗВАНИЯ ) );
    }
    
}
