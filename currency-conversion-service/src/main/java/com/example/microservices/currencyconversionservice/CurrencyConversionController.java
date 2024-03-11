package com.example.microservices.currencyconversionservice;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Random;

@RestController
public class CurrencyConversionController {

    @Autowired
    CurrencyExchangeProxy proxy;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    EurekaClient eurekaClient;



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
    //using EurekaClient Interface to get an instance of currency-exchange service
    //uses a randomly generated number to choose an instance
    @GetMapping("/currency-conversion-eureka-client/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion calculateCurrencyConversionWithEurekaClient(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable BigDecimal quantity
    ){
        HashMap<String,String> uriVariables = new HashMap<>();
        uriVariables.put("from", from);
        uriVariables.put("to",to);
        Application application = eurekaClient.getApplication("CURRENCY-EXCHANGE");
        Random random = new Random();
        int randomInstance = random.nextInt(2);
        InstanceInfo instanceInfo = application.getInstances().get(randomInstance);
        String hostName = instanceInfo.getHostName();
        int instancePort = instanceInfo.getPort();
        ResponseEntity<CurrencyConversion> responseEntity = new RestTemplate()
                .getForEntity("http://"+hostName+":"+instancePort+"/currency-exchange/from/{from}/to/{to}",
                        CurrencyConversion.class,uriVariables);
        CurrencyConversion currencyConversion = responseEntity.getBody();

        return new CurrencyConversion(currencyConversion.getId(),from,to,currencyConversion.getConversionMultiple(),
                quantity,quantity.multiply(currencyConversion.getConversionMultiple())
                ,currencyConversion.getEnvironment() + " EurekaClient with rest template");


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
