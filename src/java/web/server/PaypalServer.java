
package web.server;

import com.google.gson.Gson;
import com.paypal.api.payments.*;
import com.paypal.base.rest.*;

import java.io.IOException;

import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static util.CredencialApi.CLIENTE_ID;
import static util.CredencialApi.CLIENTE_SECRET;
import static util.CredencialApi.MODO;


/**
 *
 * @author elpoeta
 */
@WebServlet(name = "PaypalServer", urlPatterns = {"/PaypalServer"})
public class PaypalServer extends HttpServlet {
    Gson gson = new Gson();
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        Payment pay = crearPago(req, resp);
  
        resp.getWriter().print(gson.toJson(pay));
        
    }
    
       public Payment crearPago(HttpServletRequest req, HttpServletResponse resp) {
           

        Payment createdPayment = null;
        try {
            
            // ### Api Context
            // Creamos un contexto para autenticar
            // la llamada genera un id unico
            // el SDK genera una respuesta (REQUEST) id 
            // si no se pasa uno explicitamente.
            APIContext apiContext = new APIContext(CLIENTE_ID, CLIENTE_SECRET, MODO);
           
            if (req.getParameter("payerID") != null) {
                System.out.println("Ejecucion de pago");
                Payment payment = new Payment();
          
                payment.setId(req.getParameter("paymentID"));
               
                PaymentExecution paymentExecution = new PaymentExecution();
                paymentExecution.setPayerId(req.getParameter("payerID"));


                createdPayment = payment.execute(apiContext, paymentExecution);
                System.out.println("Pago ejecutado - Request :: \n " + Payment.getLastRequest());
                System.out.println("Pago ejecutado - Response :: \n " + Payment.getLastResponse());
              
            } else {
                System.out.println("Creacion de Pago");
                // ###Details
                // Se especifican detalles de los montos a pagar.
                Details details = new Details();
                //details.setShipping("1");
                details.setSubtotal("5.00");
                //details.setTax("1");

                // ###Amount
                // se especifica el monto de pago.
                Amount amount = new Amount();
                amount.setCurrency("USD");
                // el total debe ser igual a la suma de shipping, tax y subtotal.
                amount.setTotal("5.00");
                amount.setDetails(details);

                // ###Transaction
                // Una transaction define el contrato  de
                // pago - para que es el pago y para quien
                // la transaccion es creada con un 
                // `Payer` y un  `Amount` tipos de datos
                Transaction transaction = new Transaction();
                transaction.setAmount(amount);
                transaction.setDescription("Esta es una descripcion.");

                // ### Items
                // Detalles de cada item
                Item item = new Item();
                item.setName("Item de compra");
                item.setQuantity("1");
                item.setCurrency("USD");
                item.setPrice("5.00");
               
                ItemList itemList = new ItemList();
                List<Item> items = new ArrayList();
                items.add(item);
                itemList.setItems(items);

                transaction.setItemList(itemList);


                // La API de creacion de pagos requiere una lista de
                // Transacciones; agregar la `Transaction` creada
                // a la lista
                List<Transaction> transactions = new ArrayList();
                transactions.add(transaction);

                // ###Payer
                // Un recurso que representa un Pagador que financia un pago
                // Definir el Payment Method
                // como 'paypal'
                Payer payer = new Payer();
                payer.setPaymentMethod("paypal");

                // ###Payment
                // Es un recurso de pago; crear uno usando
                // los tipos anteriores y agregar 'sale' 
                Payment payment = new Payment();
                payment.setIntent("sale");
                payment.setPayer(payer);
                payment.setTransactions(transactions);

                // ###Redirect URLs
                // Proporcionar urls de retorno para
                // cancelacion y exito de pago.
                RedirectUrls redirectUrls = new RedirectUrls();
                
                redirectUrls.setCancelUrl(req.getScheme() + "://"
                       + req.getServerName() + ":" + req.getServerPort()
                        + req.getContextPath() + "/");
                redirectUrls.setReturnUrl(req.getScheme() + "://"
                        + req.getServerName() + ":" + req.getServerPort()
                        + req.getContextPath() + "/");
                payment.setRedirectUrls(redirectUrls);

                // Crear el pago con la APIService
                // usar un token valido de acceso 'AccessToken'
                // Retorna un objeto que contiene el status de la operacion
                try {
                    createdPayment = payment.create(apiContext);
                    System.out.println("Crea pago con id = "
                            + createdPayment.getId() + " y status = "
                            + createdPayment.getState());
                    // ###Payment Approval Url
                    Iterator<Links> links = createdPayment.getLinks().iterator();
                    while (links.hasNext()) {
                        Links link = links.next();
                        if (link.getRel().equalsIgnoreCase("approval_url")) {
                            req.setAttribute("redirectURL", link.getHref());
                        }
                    }
                    
                } catch (PayPalRESTException e) {
                    e.printStackTrace();
                    
                }
            }
        } catch (Exception e) {
            System.out.println("Create Payment Exception ");
            e.printStackTrace();
        }
        return createdPayment;
       
       }
}
