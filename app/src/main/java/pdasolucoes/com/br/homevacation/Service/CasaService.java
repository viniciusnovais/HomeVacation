package pdasolucoes.com.br.homevacation.Service;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pdasolucoes.com.br.homevacation.Model.Casa;
import pdasolucoes.com.br.homevacation.Model.Usuario;

/**
 * Created by PDA on 25/10/2017.
 */

public class CasaService {

    private static final String URL = "http://169.55.84.219/wshomevacation/wshomevacation.asmx";
    //private static final String URL = "http://169.55.84.219/wshomevacationdesenv/wshomevacation.asmx";
    private static final String METHOD_NAME = "GetListaCasa";
    private static final String NAMESPACE = "http://tempuri.org/";


    public static List<Casa> GetListaCasa(int idConta) {

        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        SoapObject item, response;
        List<Casa> lista = new ArrayList<>();

        try {

            PropertyInfo propertyidConta = new PropertyInfo();
            propertyidConta.setName("_idConta");
            propertyidConta.setValue(idConta);
            propertyidConta.setType(PropertyInfo.INTEGER_CLASS);

            request.addProperty(propertyidConta);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.implicitTypes = true;
            envelope.setOutputSoapObject(request);

            HttpTransportSE transportSE = new HttpTransportSE(URL);
            transportSE.call(NAMESPACE + METHOD_NAME, envelope);

            response = (SoapObject) envelope.getResponse();

            for (int i = 0; i < response.getPropertyCount(); i++) {
                item = (SoapObject) response.getProperty(i);
                Casa c = new Casa();

                c.setId(Integer.parseInt(item.getPropertyAsString("ID")));
                c.setDescricao(item.getPropertyAsString("Descricao"));

                lista.add(c);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {

            e.printStackTrace();
        }

        return lista;
    }
}
