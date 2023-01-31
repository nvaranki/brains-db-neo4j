package com.varankin.brains.db.neo4j.rest;

import com.varankin.brains.db.type.*;
import com.varankin.brains.db.xml.АтрибутныйКлюч;
import com.varankin.brains.db.xml.ЗонныйКлюч;
import com.varankin.brains.db.Транзакция;

/**
 *
 * @author Varankine
 */
abstract class NeoАтрибутный extends NeoNode implements DbАтрибутный 
{

    @Override
    public АтрибутныйКлюч тип()
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterable<ЗонныйКлюч> ключи()
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterable<ЗонныйКлюч> ключи( boolean актуальные ) 
    {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    @Override
    public final String отметка()
    {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void определить( String название, String uri, Object значение )
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object атрибут( String название, String uri, Object замена )
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String положение( String разделитель )
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean восстановимый()
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Транзакция транзакция()
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public DbПакет пакет()
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public final boolean удалить()
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public DbАтрибутный предок() 
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public final NeoАтрибутный предок( boolean actual )
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
