package com.varankin.brains.db.neo4j.local;

import com.varankin.brains.db.xml.Xml;
import com.varankin.brains.db.xml.XmlBrains;
import com.varankin.brains.db.xml.ЗонныйКлюч;
import com.varankin.io.xml.svg.XmlSvg;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

import static com.varankin.brains.db.neo4j.local.Связь.*;
import static com.varankin.brains.db.xml.XmlBrains.*;

/**
 * Фабрика элементов базы данных для Neo4j.
 *
 * @author &copy; 2022 Николай Варанкин
 */
final class NeoФабрика
{
    private static final NeoФабрика ФАБРИКА = new NeoФабрика();

    private NeoФабрика() {}
    
    static NeoФабрика getInstance()
    {
        return ФАБРИКА;
    }
    
    /**
     * Создает новый элемент для включения в {@linkplain КоллекцияПоСвязи коллекции}.
     * 
     * @param ключ   идентификатор типа элемента.
     * @param сервис менеджер базы данных.
     * @return новый элемент.
     */
    static NeoАтрибутный создать( ЗонныйКлюч ключ, GraphDatabaseService сервис )
    {
        NeoАтрибутный э = ключ.НАЗВАНИЕ == null ? 
            new NeoУзел( сервис, ключ ) : 
            switch( ключ.НАЗВАНИЕ )
            {
                case XmlBrains.XML_BRAINS    -> new NeoПакет( сервис ); 
                case XmlBrains.XML_COMPUTE   -> new NeoРасчет( сервис );
                case XmlBrains.XML_BASKET    -> new NeoМусор( сервис );
                case XmlBrains.XML_FIELD     -> new NeoПоле( сервис );
                case XmlBrains.XML_FRAGMENT  -> new NeoФрагмент( сервис );
                case XmlBrains.XML_JAVA      -> new NeoКлассJava( сервис );
                case XmlBrains.XML_JOINT     -> new NeoСоединение( сервис );
                case XmlBrains.XML_LIBRARY   -> new NeoБиблиотека( сервис );
                case XmlBrains.XML_MODULE    -> new NeoМодуль( сервис );
                case XmlBrains.XML_NOTE      -> new NeoЗаметка( сервис );
                case XmlBrains.XML_PARAMETER -> new NeoПараметр( сервис );
                case XmlBrains.XML_PIN       -> new NeoКонтакт( сервис );
                case XmlBrains.XML_POINT     -> new NeoТочка( сервис );
                case XmlBrains.XML_PROCESSOR -> new NeoПроцессор( сервис );
                case XmlBrains.XML_PROJECT   -> new NeoПроект( сервис );
                case XmlBrains.XML_SENSOR    -> new NeoСенсор( сервис );
                case XmlBrains.XML_SIGNAL    -> new NeoСигнал( сервис );
                case XmlBrains.XML_TIMELINE  -> new NeoЛента( сервис );
                case Xml.PI_ELEMENT          -> new NeoИнструкция( сервис );
                case Xml.XML_CDATA           -> new NeoТекстовыйБлок( сервис );
                case Xml.XMLNS_NS            -> new NeoЗона( сервис );
                default                      -> XmlSvg.XMLNS_SVG.equals( ключ.ЗОНА ) ?
                                                    // все элементы SVG, любое название
                                                    new NeoГрафика( сервис, ключ ) : 
                                                    // все именованное прочее
                                                    new NeoУзел( сервис, ключ );  
            };
        return э;
    }
    
    @Deprecated //TODO дубликаты по Коллекция'м
    static NeoАтрибутный создать( Node node )
    {
        if( node == null ) return null;
        NeoАтрибутный атр;
        String uri = Architect.getURI( node );
        if( XmlBrains.XMLNS_BRAINS.equals( uri ) )
            switch( Architect.getXmlEntry( node, "" ) )
            {
                case XmlBrains.XML_BRAINS:    атр = new NeoПакет( node ); break;
                case XmlBrains.XML_COMPUTE:   атр = new NeoРасчет( node ); break;
                case XmlBrains.XML_BASKET:    атр = new NeoМусор( node ); break;
                case XmlBrains.XML_FIELD:     атр = new NeoПоле( node ); break;
                case XmlBrains.XML_FRAGMENT:  атр = new NeoФрагмент( node ); break;
                case XmlBrains.XML_JAVA:      атр = new NeoКлассJava( node ); break;
                case XmlBrains.XML_JOINT:     атр = new NeoСоединение( node ); break;
                case XmlBrains.XML_LIBRARY:   атр = new NeoБиблиотека( node ); break;
                case XmlBrains.XML_MODULE:    атр = new NeoМодуль( node ); break;
                case XmlBrains.XML_NOTE:      атр = new NeoЗаметка( node ); break;
                case XmlBrains.XML_PARAMETER: атр = new NeoПараметр( node ); break;
                case XmlBrains.XML_PIN:       атр = new NeoКонтакт( node ); break;
                case XmlBrains.XML_POINT:     атр = new NeoТочка( node ); break;
                case XmlBrains.XML_PROCESSOR: атр = new NeoПроцессор( node ); break;
                case XmlBrains.XML_PROJECT:   атр = new NeoПроект( node ); break;
                case XmlBrains.XML_SENSOR:    атр = new NeoСенсор( node ); break;
                case XmlBrains.XML_SIGNAL:    атр = new NeoСигнал( node ); break;
                case XmlBrains.XML_TIMELINE:  атр = new NeoЛента( node ); break;
                default:                      атр = new NeoАтрибутный( node ) {};
            }
        else if( XmlSvg.XMLNS_SVG.equals( uri ) )
        {
            // все элементы SVG
            атр = new NeoГрафика( node );
        }
        else if( Xml.XMLNS_XML.equals( uri ) )
            атр = switch( Architect.getXmlEntry( node, "" ) )
            {
                // все элементы пространства имен
                case Xml.XMLNS_NS -> new NeoЗона( node );
                default           -> new NeoАтрибутный( node ) {};
            }; 

        else
        {
            атр = new NeoУзел( node );
        }
        return атр;
    }
    
    static RelationshipType связь( String name, String uri )
    {
        RelationshipType type;
        if( uri == null )
        {
            switch( name )
            {
                case Xml.XML_CDATA:   type = Текст; break;
                case Xml.PI_ELEMENT:  type = Инструкция; break;
                default: type = Прочее;//throw new SAXException( lName );
            }            
        }
        else if( XmlBrains.XMLNS_BRAINS.equals( uri ) )
        {
            switch( name )
            {
                case XML_BRAINS:    type = Пакет; break;
                case XML_COMPUTE:   type = Расчет; break;
                case XML_BASKET:    type = Мусор; break;
                case XML_FIELD:     type = Поле; break;
                case XML_FRAGMENT:  type = Фрагмент; break;
                case XML_JAVA:      type = КлассJava; break;
                case XML_JOINT:     type = Соединение; break;
                case XML_LIBRARY:   type = Библиотека; break;
                case XML_MODULE:    type = Модуль; break;
                case XML_NOTE:      type = Заметка; break;
                case XML_PARAMETER: type = Параметр; break;
                case XML_PIN:       type = Контакт; break;
                case XML_POINT:     type = Точка; break;
                case XML_PROCESSOR: type = Процессор; break;
                case XML_PROJECT:   type = Проект; break;
                case XML_SENSOR:    type = Сенсор; break;
                case XML_SIGNAL:    type = Сигнал; break;
                case XML_TIMELINE:  type = Лента; break;
                default: type = Прочее;//throw new SAXException( lName );
            }
        }
        else if( XmlSvg.XMLNS_SVG.equals( uri ) )
        {
            type = Графика;
        }
        else
        {
            type = Прочее;
        }
        return type;
    }

}
