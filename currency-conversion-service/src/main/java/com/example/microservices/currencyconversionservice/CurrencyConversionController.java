package com.example.microservices.currencyconversionservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;

@RestController
public class CurrencyConversionController {

    @Autowired
    CurrencyExchangeProxy proxy;
    @Autowired
    RestTemplate restTemplate;



    @GetMapping("/currency-conversion/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion calculateCurrencyConversion(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable BigDecimal quantity
            ){
        HashMap<String,String> uriVariables = new HashMap<>();
        uriVariables.put("from", from);
        uriVariables.put("to",to);
        ResponseEntity<CurrencyConversion> responseEntity = new RestTemplate()
                .getForEntity("http://localhost:8000/currency-exchange/from/{from}/to/{to}",
                        CurrencyConversion.class,uriVariables);
        CurrencyConversion currencyConversion = responseEntity.getBody();

         return new CurrencyConversion(currencyConversion.getId(),from,to,currencyConversion.getConversionMultiple(),
                quantity,quantity.multiply(currencyConversion.getConversionMultiple())
                 ,currencyConversion.getEnvironment() + " Rest Template");


    }
    //uses a rest template annotated with @loadBalanced to perform client side discovery and load balancing with
    //eureka client
    @GetMapping("/currency-conversion-loadBalanced-rest/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion calculateCurrencyConversionWithLoadBalancedRest(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable BigDecimal quantity
    ){
        HashMap<String,String> uriVariables = new HashMap<>();
        uriVariables.put("from", from);
        uriVariables.put("to",to);
        ResponseEntity<CurrencyConversion> responseEntity = restTemplate
                .getForEntity("http://CURRENCY-EXCHANGE/currency-exchange/from/{from}/to/{to}",
                        CurrencyConversion.class,uriVariables);
        CurrencyConversion currencyConversion = responseEntity.getBody();

        return new CurrencyConversion(currencyConversion.getId(),from,to,currencyConversion.getConversionMultiple(),
                quantity,quantity.multiply(currencyConversion.getConversionMultiple())
                ,currencyConversion.getEnvironment() + " loadBalanced clientside service discovery Rest Template");


    }


    @GetMapping("/currency-conversion-feign/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion calculateCurrencyConversionFeign(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable BigDecimal quantity
    ){

        CurrencyConversion currencyConversion = proxy.retrieveExchangeValue(from, to);

        return new CurrencyConversion(currencyConversion.getId(),from,to,currencyConversion.getConversionMultiple(),
                quantity,quantity.multiply(currencyConversion.getConversionMultiple())
                ,currencyConversion.getEnvironment() + " feign");


    }
}
