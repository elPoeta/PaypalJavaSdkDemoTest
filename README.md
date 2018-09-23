# PaypalJavaSdkDemoTest
Test demo medio de pago PayPal utilizando Java SDK y paypal rest API 

#### Api Context
     
              Creamos un contexto para autenticar
              la llamada genera un id unico
              el SDK genera una respuesta (REQUEST) id 
              si no se pasa uno explicitamente.
             
                APIContext apiContext = new APIContext(CLIENTE_ID, CLIENTE_SECRET, MODO);
            
#### Ejecucion de pago
                
                Payment payment = new Payment();
          
                payment.setId(req.getParameter("paymentID"));
               
                PaymentExecution paymentExecution = new PaymentExecution();
                paymentExecution.setPayerId(req.getParameter("payerID"));


                createdPayment = payment.execute(apiContext, paymentExecution);
               
              
#### Creacion de Pago
                
* Details

    * Se especifican detalles de los montos a pagar.
                    
        Details details = new Details();
        details.setShipping("1");
        details.setSubtotal("5.00");
        details.setTax("1");

### Amount
                
* Se especifica el monto de pago.
                     
    Amount amount = new Amount();
    amount.setCurrency("USD");
                    
* El total debe ser igual a la suma de shipping, tax y subtotal.
                     
    amount.setTotal("5.00");
    amount.setDetails(details);

### Transaction
                 
* Una transaction define el contrato  de
    pago - para que es el pago y para quien
    la transaccion es creada con un 
    `Payer` y un  `Amount` tipos de datos
                   
    Transaction transaction = new Transaction();
    transaction.setAmount(amount);
    transaction.setDescription("Esta es una descripcion.");

### Items
                
* Detalles de cada item
                
    Item item = new Item();
    item.setName("Item de compra");
    item.setQuantity("1");
    item.setCurrency("USD");
    item.setPrice("5.00");
                
    ItemList itemList = new ItemList();
    List<Item> items = new ArrayList<Item>();
    items.add(item);
    itemList.setItems(items);

    transaction.setItemList(itemList);


* La API de creacion de pagos requiere una lista de
    Transacciones; agregar la `Transaction` creada
    a la lista
                
    List<Transaction> transactions = new ArrayList();
    transactions.add(transaction);

### Payer
                
* Un recurso que representa un Pagador que financia un pago
    Definir el Payment Method
    como 'paypal'
                
    Payer payer = new Payer();
    payer.setPaymentMethod("paypal");

### Payment
                
* Es un recurso de pago; crear uno usando
los tipos anteriores y agregar 'sale' 
               
    Payment payment = new Payment();
    payment.setIntent("sale");
    payment.setPayer(payer);
    payment.setTransactions(transactions);

### Redirect URLs
                    
* Proporcionar urls de retorno para
cancelacion y exito de pago.
                 
    RedirectUrls redirectUrls = new RedirectUrls();
                
    redirectUrls.setCancelUrl(req.getScheme() + "://"
    + req.getServerName() + ":" + req.getServerPort()
    + req.getContextPath() + "/");
                     
    redirectUrls.setReturnUrl(req.getScheme() + "://"
    + req.getServerName() + ":" + req.getServerPort()
    + req.getContextPath() + "/");
                     
    payment.setRedirectUrls(redirectUrls);
                 
### APIService
                  
* Crear el pago con la APIService
usar un token valido de acceso 'AccessToken'
Retorna un objeto que contiene el status de la operacion
                         
    try {
        createdPayment = payment.create(apiContext);
        System.out.println("Crea pago con id = "
        + createdPayment.getId() + " y status = "
        + createdPayment.getState());
                
   * Payment Approval Url

         Iterator<Links> links = createdPayment.getLinks().iterator();
         while (links.hasNext()) {
         Links link = links.next();
         if (link.getRel().equalsIgnoreCase("approval_url")) {                                     req.setAttribute("redirectURL", link.getHref());
                                 }
                             }

### Para finalizar retornamos un Payment                           

