package com.siman.credisiman.visa.service;

import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BloqueoDesbloqueoTarjetaTest {
    static private final String NS = "http://siman.com/BloqueoDesbloqueoTarjeta";

    @Test
    public void obtenerConsultaSubProductosOk() {
        XmlObject result = BloqueoDesbloqueoTarjeta.obtenerBloqueoDesbloqueoTarjeta("SV", "4573840047882285","28","M6", "jndi/SimacSV", "jdbc/ORIONREPOSV", "http://soauat.siman.com:7003/v1/orion", "usuario", "600831, 600831, 600831","V");

        //Status
        assertEquals("00", ((SimpleValue) result.selectPath( "declare namespace ns='" + NS + "' " + ".//ns:statusCode")[0]).getStringValue());
        assertEquals("SUCCESS", ((SimpleValue) result.selectPath( "declare namespace ns='" + NS + "' " + ".//ns:status")[0]).getStringValue());

    }
}
