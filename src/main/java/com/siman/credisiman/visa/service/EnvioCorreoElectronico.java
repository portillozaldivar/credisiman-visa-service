package com.siman.credisiman.visa.service;

import com.credisiman.visa.soa.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.siman.credisiman.visa.dto.email.EmailTo;
import com.siman.credisiman.visa.dto.email.MandrilResponse;
import com.siman.credisiman.visa.utils.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.namespace.QName;
import java.util.List;

@Slf4j
public class EnvioCorreoElectronico {
    private static final String namespace = "http://siman.com/EnvioCorreoElectronico";
    private static final String operationResponse = "ObtenerEnvioCorreoElectronicoResponse";
    private static final String url = "https://mandrillapp.com/api/1.0/messages/send.json";

    public static XmlObject send(String correoOrigen, String nombreOrigen, String asunto,
                                 boolean flagImportante, String html, String apiKey, String mandrilTag,
                                 List<EmailTo> correosDestino) {
        //validar campos requeridos
        Utils utils = new Utils();
        Message message = new Message();

        try {
            JSONObject json = new JSONObject();//raiz
            JSONObject msg = new JSONObject();
            JSONArray to = new JSONArray();
            JSONArray tags = new JSONArray();

            json.put("key", apiKey)
                    .put("message", msg);

            msg.put("html", html)
                    .put("subject", asunto)
                    .put("from_email", correoOrigen)
                    .put("from_name", nombreOrigen)
                    .put("important", flagImportante);

            correosDestino.forEach(emailTo -> {
                JSONObject dest = new JSONObject();
                dest.put("email", emailTo.getEmail())
                        .put("name", emailTo.getName())
                        .put("type", "to");
                to.put(dest);
            });

            msg.put("to", to);
            tags.put(mandrilTag);
            msg.put("tags", tags);

            log.info(json.toString());

            HttpResponse<String> jsonResponse //realizar petici�n demiante unirest
                    = Unirest.post(url)
                    .header("Content-Type", "application/json")
                    .body(json.toString())
                    .asString();

            //capturar respuesta
            MandrilResponse response1;
            JSONObject response = new JSONObject(jsonResponse
                    .getBody()
                    .replaceAll("\u200B", ""));
            response1 = new ObjectMapper()
                    .readValue(response.toString(), MandrilResponse.class);

            log.info("respuesta: " + new ObjectMapper().writeValueAsString(response1));

            String statusCode;
            String status;
            String statusMessage;

            //evaluar resultados
            switch (response1.getStatus()) {
                case "error":
                    statusCode = "500";
                    status = "ERROR";
                    statusMessage = response1.getMessage();
                    break;
                case "queued":
                    statusCode = "500";
                    status = "ERROR";
                    statusMessage = response1.getQueued_reason();
                    break;
                default:
                    statusCode = "00";
                    status = "SUCCESS";
                    statusMessage = "Proceso exitoso";
                    break;
            }

            //RESPUESTAS
            XmlObject result = XmlObject.Factory.newInstance();
            XmlCursor cursor = result.newCursor();
            QName responseQName = new QName(namespace, operationResponse);
            cursor.toNextToken();
            cursor.beginElement(responseQName);
            cursor.insertElementWithText(new QName(namespace, "statusCode"), statusCode);
            cursor.insertElementWithText(new QName(namespace, "status"), status);
            cursor.insertElementWithText(new QName(namespace, "statusMessage"), statusMessage);
            cursor.toParent();

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return message.genericMessage("ERROR", "600", "Error general contacte al administrador del sistema...", namespace, operationResponse);
        }
    }
}