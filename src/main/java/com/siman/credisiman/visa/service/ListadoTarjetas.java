package com.siman.credisiman.visa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.siman.credisiman.visa.dto.listadotarjeta.CuentasResponse;
import com.siman.credisiman.visa.dto.listadotarjeta.ListadoTarjetasResponse;
import com.siman.credisiman.visa.dto.listadotarjeta.Tarjetas;
import com.siman.credisiman.visa.dto.listadotarjeta.TarjetasResponse;
import com.siman.credisiman.visa.utils.ConnectionHandler;
import com.siman.credisiman.visa.utils.Message;
import com.siman.credisiman.visa.utils.Utils;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ListadoTarjetas {
    private static final Logger log = LoggerFactory.getLogger(ListadoTarjetas.class);
    private static final String namespace = "http://siman.com/ConsultaListadoTarjetas";
    private static final String operationResponse = "ObtenerListadoTarjetasResponse";


    public static XmlObject obtenerListadoTarjetas(String pais, String identificacion, String remoteJndiSunnel,
                                                   String remoteJndiOrion, String siscardUrl, String siscardUser, String binCredisiman) {
        Utils utils = new Utils();
        Message message = new Message();

        //validar campos requeridos
        if (utils.validateNotNull(pais) || utils.validateNotEmpty(pais)) {
            return message.genericMessage("ERROR", "025", "El campo pais es obligatorio", namespace, operationResponse);
        }
        if (utils.validateNotNull(identificacion) || utils.validateNotEmpty(identificacion)) {
            return message.genericMessage("ERROR", "025", "El campo identificacion es obligatorio", namespace, operationResponse);
        }

        //validar longitudes
        if (!utils.validateLongitude(pais, 3)) {
            log.info("pais, size overload");
            return message.genericMessage("ERROR", "025", "La longitud del campo pais debe ser menor o igual a 3", namespace, operationResponse);
        }
        if (!utils.validateLongitude(identificacion, 19)) {
            log.info("identificacion, size overload");
            return message.genericMessage("ERROR", "025", "La longitud del campo identificacion debe ser menor o igual a 19", namespace, operationResponse);
        }

        List<Tarjetas> response2 = new ArrayList<>();
        List<Tarjetas> response3 = new ArrayList<>();

        try {
            //all code here
            response2 = obtenerDatosArca(identificacion, remoteJndiSunnel);
            if (response2.size() > 0) {
                log.info("DATOS TARJETA PRIVADA");
                return estructura(response2);
            }
            response3 = obtenerDatosSiscard(pais, identificacion, siscardUrl);
            if (response3.size() > 0) {
                log.info("DATOS TARJETA CREDISIMAN");
                return estructura(response3);
            }
            log.info("ObtenerListadoTarjetas response = [" + message.genericMessage("ERROR", "400", "La consulta no devolvio resultados", namespace, operationResponse) + "]");
            return message.genericMessage("ERROR", "400", "La consulta no devolvio resultados", namespace, operationResponse);

        } catch (SQLException e) {
            log.error("SQL ERROR, " + e.getMessage());
            log.info("ObtenerListadoTarjetas response = [" + message.genericMessage("ERROR", "600", "Error general contacte al administrador del sistema...", namespace, operationResponse) + "]");
            return message.genericMessage("ERROR", "600", "Error general contacte al administrador del sistema...", namespace, operationResponse);
        } catch (Exception ex) {
            log.error("SERVICE ERROR, " + ex.getMessage());
            log.info("ObtenerListadoTarjetas response = [" + message.genericMessage("ERROR", "600", "Error general contacte al administrador del sistema...", namespace, operationResponse) + "]");
            return message.genericMessage("ERROR", "600", "Error general contacte al administrador del sistema...", namespace, operationResponse);
        }

    }

    public static XmlObject estructura(List<Tarjetas> response) {
        XmlObject result = XmlObject.Factory.newInstance();
        XmlCursor cursor = result.newCursor();
        QName responseQName = new QName(namespace, operationResponse);

        cursor.toNextToken();
        cursor.beginElement(responseQName);

        cursor.insertElementWithText(new QName(namespace, "statusCode"), "00");
        cursor.insertElementWithText(new QName(namespace, "status"), "SUCCESS");
        cursor.insertElementWithText(new QName(namespace, "statusMessage"), "Proceso exitoso");
        //Listado tarjetas

        for (Tarjetas tarjetas : response) {
            cursor.beginElement(new QName(namespace, "tarjetas"));
            cursor.insertElementWithText(new QName(namespace, "numeroTarjeta"), tarjetas.getNumeroTarjeta());
            cursor.insertElementWithText(new QName(namespace, "cuenta"), tarjetas.getCuenta());
            cursor.insertElementWithText(new QName(namespace, "tipoTarjeta"), tarjetas.getTipoTarjeta());
            cursor.insertElementWithText(new QName(namespace, "nombreTH"), tarjetas.getNombreTH());
            cursor.insertElementWithText(new QName(namespace, "estado"), tarjetas.getEstado());
            cursor.insertElementWithText(new QName(namespace, "limiteCreditoLocal"), tarjetas.getLimiteCreditoLocal());
            cursor.insertElementWithText(new QName(namespace, "limiteCreditoDolares"), tarjetas.getLimiteCreditoDolares());
            cursor.insertElementWithText(new QName(namespace, "saldoLocal"), tarjetas.getSaldoLocal());
            cursor.insertElementWithText(new QName(namespace, "saldoDolares"), tarjetas.getSaldoDolares());
            cursor.insertElementWithText(new QName(namespace, "disponibleLocal"), tarjetas.getDisponibleLocal());
            cursor.insertElementWithText(new QName(namespace, "disponibleDolares"), tarjetas.getDisponibleDolares());
            cursor.insertElementWithText(new QName(namespace, "pagoMinimoLocal"), tarjetas.getPagoMinimoLocal());
            cursor.insertElementWithText(new QName(namespace, "pagoMinimoDolares"), tarjetas.getPagoMinimoDolares());
            cursor.insertElementWithText(new QName(namespace, "pagoMinimoVencidoLocal"), tarjetas.getPagoMinimoVencidoLocal());
            cursor.insertElementWithText(new QName(namespace, "pagoMinimoVencidoDolares"), "");
            cursor.insertElementWithText(new QName(namespace, "pagoContadoLocal"), tarjetas.getPagoContadoLocal());
            cursor.insertElementWithText(new QName(namespace, "pagoContadoDolares"), tarjetas.getPagoContadoDolares());
            cursor.insertElementWithText(new QName(namespace, "fechaPago"), tarjetas.getFechaPago());
            cursor.insertElementWithText(new QName(namespace, "fechaUltimoCorte"), tarjetas.getFechaUltimoCorte());
            cursor.insertElementWithText(new QName(namespace, "saldoMonedero"), tarjetas.getSaldoMonedero());
            cursor.insertElementWithText(new QName(namespace, "rombosAcumulados"), tarjetas.getRombosAcumulados());
            cursor.insertElementWithText(new QName(namespace, "rombosDinero"), tarjetas.getRombosDinero());
            cursor.insertElementWithText(new QName(namespace, "fondosReservados"), tarjetas.getFondosReservados());
            cursor.toParent();
        }
        cursor.toParent();
        log.info("ObtenerListadoTarjetas response = [" + result + "]");
        return result;
    }

    public static List<Tarjetas> obtenerDatosArca(String identificacion, String remoteJndiSunnel) throws Exception {
        //si no esta en siscard, buscar en arca
        String query = "SELECT c.cardid AS numeroTarjeta, " +
                "       cl.creditlineid AS cuenta, " +
                "       DECODE (c.CARDTYPE, " +
                "               'M', 'TITULAR', " +
                "               'P', 'TITULAR', " +
                "               'A', 'ADICIONAL') " +
                "          AS tipoTarjeta, " +
                "       cu.aliasname AS nombreTH, " +
                "       CASE WHEN cl.blockedind = 'T' THEN 'Bloqueada' ELSE 'Activa' END " +
                "          AS estado, " +
                "       CASE WHEN cl.currencycreditlimit <> 840 THEN cl.creditlimit ELSE 0 END " +
                "          AS limiteCreditoLocal, " +
                "       CASE WHEN cl.currencycreditlimit = 840 THEN cl.creditlimit ELSE 0 END " +
                "          AS limiteCreditoDolares, " +
                "       CASE " +
                "          WHEN fb.currencyid <> 840 " +
                "          THEN " +
                "             CASE " +
                "                WHEN NVL (fb.saldo, 0) = 0 AND NVL (cb.saldoAFavor, 0) > 0 " +
                "                THEN " +
                "                   (cb.saldoAFavor * -1) " +
                "                ELSE " +
                "                   NVL (fb.saldo, 0) " +
                "             END " +
                "          ELSE " +
                "             0 " +
                "       END " +
                "          AS saldoLocal, " +
                "       CASE " +
                "          WHEN fb.currencyid = 840 " +
                "          THEN " +
                "             CASE " +
                "                WHEN NVL (fb.saldo, 0) = 0 AND NVL (cb.saldoAFavor, 0) > 0 " +
                "                THEN " +
                "                   (cb.saldoAFavor * -1) " +
                "                ELSE " +
                "                   NVL (fb.saldo, 0) " +
                "             END " +
                "          ELSE " +
                "             0 " +
                "       END " +
                "          AS saldoDolares, " +
                "       CASE " +
                "          WHEN clp.currencycreditlimit <> 840 THEN clp.availablebalance " +
                "          ELSE 0 " +
                "       END " +
                "          AS disponibleLocal, " +
                "       CASE " +
                "          WHEN clp.currencycreditlimit = 840 THEN clp.availablebalance " +
                "          ELSE 0 " +
                "       END " +
                "          AS disponibleDolares, " +
                "       CASE WHEN fb.currencyid <> 840 THEN fb.pagoMinimo ELSE 0 END " +
                "          AS pagoMinimoLocal, " +
                "       CASE WHEN fb.currencyid = 840 THEN fb.pagoMinimo ELSE 0 END " +
                "          AS pagoMinimoDolares, " +
                "       CASE WHEN fb.currencyid <> 840 THEN fb.capitalVencido ELSE 0 END " +
                "          AS pagoMinimoVencidoLocal, " +
                "       CASE WHEN fb.currencyid = 840 THEN fb.capitalVencido ELSE 0 END " +
                "          AS pagoMinimoVencidoDolares, " +
                "       CASE WHEN fb.currencyid <> 840 THEN fb.pagoContado ELSE 0 END " +
                "          AS pagoContadoLocal, " +
                "       CASE WHEN fb.currencyid = 840 THEN fb.pagoContado ELSE 0 END " +
                "          AS pagoContadoDolares, " +
                "       TO_CHAR(bp.fechaPago,'YYYYMMDD') AS fechaPago, " +
                "       TO_CHAR(cl.lastinterestaccruingdate,'YYYYMMDD') AS fechaUltimoCorte, " +
                "       ' ' AS saldoMonedero, " +
                "       ' ' AS rombosAcumulados, " +
                "       ' ' AS rombosDinero, " +
                "       ' ' AS fondosReservados " +
                "  FROM SUNNELP3.t_gcard c " +
                "       INNER JOIN SUNNELP3.t_gcustomer cu " +
                "          ON cu.customerid = c.customerid " +
                "       INNER JOIN SUNNELP3.t_gaccount a " +
                "          ON a.cardid = c.cardid " +
                "       INNER JOIN SUNNELP3.t_gcreditline cl " +
                "          ON cl.creditlineid = a.accountid " +
                "       INNER JOIN SUNNELP3.t_gcreditlinepartition clp " +
                "          ON cl.creditlineid = clp.creditlineid " +
                "       LEFT OUTER JOIN (  SELECT clt.creditlineid, " +
                "                                 MAX (bpt.paymentdate) AS fechaPago " +
                "                            FROM    SUNNELP3.t_gbillingperiod bpt " +
                "                                 INNER JOIN " +
                "                                    SUNNELP3.t_gcreditline clt " +
                "                                 ON     clt.billingcycleid = bpt.billingcycleid " +
                "                                    AND clt.lastinterestaccruingdate = " +
                "                                           bpt.billingdate " +
                "                        GROUP BY clt.creditlineid) bp " +
                "          ON bp.creditlineid = cl.creditlineid " +
                "       LEFT OUTER JOIN (  SELECT fbt.creditlineid, " +
                "                                 fbt.currencyid, " +
                "                                 SUM ( " +
                "                                      fbt.regularbalance " +
                "                                    + fbt.periodamountdue " +
                "                                    + fbt.regularinterest " +
                "                                    + fbt.regularinteresttax " +
                "                                    + fbt.overduebalance " +
                "                                    + fbt.overdueinterest " +
                "                                    + fbt.overdueinteresttax " +
                "                                    + fbt.contingentinterest) " +
                "                                    AS saldo, " +
                "                                 SUM ( " +
                "                                      fbt.periodamountdue " +
                "                                    + fbt.regularinterest " +
                "                                    + fbt.regularinteresttax " +
                "                                    + fbt.overduebalance " +
                "                                    + fbt.overdueinterest " +
                "                                    + fbt.overdueinteresttax " +
                "                                    + fbt.contingentinterest) " +
                "                                    AS pagoMinimo, " +
                "                                 SUM ( " +
                "                                      fbt.periodamountdue " +
                "                                    + fbt.regularinterest " +
                "                                    + fbt.regularinteresttax " +
                "                                    + fbt.overduebalance " +
                "                                    + fbt.overdueinterest " +
                "                                    + fbt.overdueinteresttax " +
                "                                    + fbt.contingentinterest) " +
                "                                    AS pagoContado, " +
                "                                 SUM (fbt.regularbalance) AS capitalNoExigible, " +
                "                                 SUM (fbt.periodamountdue) AS capitalExigible, " +
                "                                 SUM (fbt.overduebalance) AS capitalVencido, " +
                "                                 SUM (regularinterest + fbt.regularinteresttax) " +
                "                                    AS interesCorriente, " +
                "                                 SUM ( " +
                "                                      fbt.overdueinterest " +
                "                                    + fbt.overdueinteresttax) " +
                "                                    AS interesMoratorio, " +
                "                                 SUM (fbt.contingentinterest) " +
                "                                    AS interesContigente " +
                "                            FROM t_gfinancingbalance fbt " +
                "                        GROUP BY fbt.creditlineid, fbt.currencyid) fb " +
                "          ON     fb.creditlineid = cl.creditlineid " +
                "             AND fb.currencyid = cl.currencycreditlimit " +
                "       LEFT OUTER JOIN (  SELECT cbt.creditlineid, " +
                "                                 cbt.currencyid, " +
                "                                 SUM (cbt.amountinexcess) AS saldoAFavor " +
                "                            FROM t_gcreditbalance cbt " +
                "                        GROUP BY cbt.creditlineid, cbt.currencyid) cb " +
                "          ON     cb.creditlineid = cl.creditlineid " +
                "             AND cb.currencyid = cl.currencycreditlimit " +
                " WHERE c.closedind = 'F' AND cu.identificationnumber = ? ";//TODO obtener query arca
        List<Tarjetas> tarjetasList = new ArrayList<>();
        ConnectionHandler connectionHandler = new ConnectionHandler();
        Connection conexion = connectionHandler.getConnection(remoteJndiSunnel);
        PreparedStatement sentencia = conexion.prepareStatement(query);
        sentencia.setString(1, identificacion); //TODO agregar parametros
        ResultSet rs = sentencia.executeQuery();

        while (rs.next()) {
            Tarjetas tarjeta = new Tarjetas();
            tarjeta.setNumeroTarjeta(rs.getString("numeroTarjeta"));
            tarjeta.setCuenta(rs.getString("cuenta"));
            tarjeta.setTipoTarjeta(rs.getString("tipoTarjeta"));
            tarjeta.setNombreTH(rs.getString("nombreTH"));
            tarjeta.setEstado(rs.getString("estado"));
            tarjeta.setLimiteCreditoLocal(rs.getString("limiteCreditoLocal"));
            tarjeta.setLimiteCreditoDolares(rs.getString("limiteCreditoDolares"));
            tarjeta.setSaldoLocal(rs.getString("saldoLocal"));
            tarjeta.setSaldoDolares(rs.getString("saldoDolares"));
            tarjeta.setDisponibleLocal(rs.getString("disponibleLocal"));
            tarjeta.setDisponibleDolares(rs.getString("disponibleDolares"));
            tarjeta.setPagoMinimoLocal(rs.getString("pagoMinimoLocal"));
            tarjeta.setPagoMinimoDolares(rs.getString("pagoMinimoDolares"));
            tarjeta.setPagoMinimoVencidoLocal(rs.getString("pagoMinimoVencidoLocal"));
            tarjeta.setPagoMinimoVencidoDolares(rs.getString("pagoMinimoVencidoDolares"));
            tarjeta.setPagoContadoLocal(rs.getString("pagoContadoLocal"));
            tarjeta.setPagoContadoDolares(rs.getString("pagoContadoDolares"));
            tarjeta.setFechaPago(rs.getString("fechaPago"));
            tarjeta.setFechaUltimoCorte(rs.getString("fechaUltimoCorte"));
            tarjeta.setSaldoMonedero(rs.getString("saldoMonedero"));
            tarjeta.setRombosAcumulados(rs.getString("rombosAcumulados"));
            tarjeta.setRombosDinero(rs.getString("rombosDinero"));
            tarjeta.setFondosReservados(rs.getString("fondosReservados"));
            tarjetasList.add(tarjeta);
        }

        return tarjetasList;
    }

    public static List<Tarjetas> obtenerDatosSiscard(String pais, String identificacion, String siscardUrl) throws Exception {
        ListadoTarjetasResponse response1 = new ListadoTarjetasResponse();
        JSONObject jsonSend = new JSONObject(); //json a enviar
        jsonSend.put("country", pais)
                .put("processIdentifier", "ListadoTarjetas")
                .put("cedula", identificacion)
                .put("typeService", "");

        HttpResponse<String> jsonResponse //realizar petici�n demiante unirest
                = Unirest.post(siscardUrl.concat("/consultaCuenta"))
                .header("Content-Type", "application/json")
                .body(jsonSend.toString())
                .asString();

        //capturar respuesta
        JSONObject response = new JSONObject(jsonResponse
                .getBody()
                .replaceAll("u200B", ""));
        response1 = new ObjectMapper()
                .readValue(response.toString(), ListadoTarjetasResponse.class);

        log.info(new ObjectMapper().writeValueAsString(response1));

        List<Tarjetas> responseList = new ArrayList<>();


        for (int i = 0; i < response1.getCuentas().size(); i++) {
            CuentasResponse cuentas = response1.getCuentas().get(i);
            for (TarjetasResponse tarjetas : cuentas.getTarjetas()) {
                Tarjetas tarjeta = new Tarjetas();

                tarjeta.setNumeroTarjeta(tarjetas.getNumeroTarjeta());
                tarjeta.setCuenta(cuentas.getCuenta());
                tarjeta.setTipoTarjeta(tarjetas.getTipoTarjeta());
                tarjeta.setNombreTH(tarjetas.getNombreTH());
                tarjeta.setEstado(tarjetas.getEstadoTarjeta());
                tarjeta.setLimiteCreditoLocal(tarjetas.getLimiteCreditoLocal());
                tarjeta.setLimiteCreditoDolares(tarjetas.getLimiteCreditoInter());
                tarjeta.setSaldoLocal(cuentas.getSaldoLocal());
                tarjeta.setSaldoDolares(cuentas.getSaldoInter());
                tarjeta.setDisponibleLocal(tarjetas.getDispLocalTarjeta());
                tarjeta.setDisponibleDolares(tarjetas.getDispIntTarjeta());
                tarjeta.setPagoMinimoLocal(cuentas.getPagoMinimoLocal());
                tarjeta.setPagoMinimoDolares(cuentas.getPagoMinimoInt());
                tarjeta.setPagoMinimoVencidoLocal("");
                tarjeta.setPagoMinimoVencidoDolares("");
                tarjeta.setPagoContadoLocal(cuentas.getPagoContadoLocal());
                tarjeta.setPagoContadoDolares(cuentas.getPagoContInt());
                tarjeta.setFechaPago(cuentas.getFechaVencimientoPago());
                tarjeta.setFechaUltimoCorte("");
                tarjeta.setSaldoMonedero("");
                tarjeta.setRombosAcumulados("");
                tarjeta.setRombosDinero(cuentas.getSaldoPremiacion());
                tarjeta.setFondosReservados("");

                responseList.add(tarjeta);
            }
        }
        return responseList;
    }
}
