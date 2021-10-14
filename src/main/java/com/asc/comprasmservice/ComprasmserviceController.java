package com.asc.comprasmservice;

import com.asc.comprasmservice.config.RabbitConfiguration;
import com.asc.comprasmservice.data.Compra;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ComprasmserviceController {
	// variável context sendo declarada no excopo global da classe para evitar vazamento de recursos
	private static ApplicationContext context;
    private static String exchange = "CHANGE-VEHICLE-DEXCHANGE";
    private static String binding = "CHANGE-VEHICLE-BINDING";
    
    @GetMapping("/compras")
    public String index()
    {
        return "Olá!! Bem vindo(a) ao micro serviço do processo de compra do veículo.";
    }

    @PostMapping(path="compras/salvar")
    public String save(@RequestBody Compra payload)
    {
        // absorvendo contexto da aplicação - injeção das configurações
		context = new AnnotationConfigApplicationContext(RabbitConfiguration.class);

		// utilizando a bean de instanciação de um rabbitAdmin para declarar nossos recursos do Rabbit (Fila, Corretor & Ligação)
		RabbitTemplate t = (RabbitTemplate) context.getBean("rTemplate");
        // instanciando conversor json do nosso arquivo de configs
        ObjectMapper m = (ObjectMapper) context.getBean("mConverter");

        try {
            // convertendo POJO
            String json = m.writeValueAsString(payload);

            // criando e enviando mensagem
            t.convertAndSend(exchange, binding, json);
    
            return "Muito bem. Processo de compra iniciado com sucesso!";
        } catch (Exception e) {
            return "Opa! Houve algo de errado na transcrição de seu processo de compra.";
        }
    }

    // Método observador da fila de veículos
    @RabbitListener(queues="COMPRAS-QUEUE")
    public Void get(Message m)
    {
        org.slf4j.Logger log = LoggerFactory.getLogger("name");
        String json = m.toString();
        log.info(json);

        return null;
    }
}
