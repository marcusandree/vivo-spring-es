package com.brq.masm.routes;

import javax.annotation.PostConstruct;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.elasticsearch.ElasticsearchComponent;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.stereotype.Component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.brq.masm.aggregators.BodyAggregateStrategy;
import com.brq.masm.beans.CDRBean;
import com.brq.masm.pojo.CDR;
import com.brq.masm.pojo.RecordID;
import com.brq.masm.pojo.Call;
import com.brq.masm.pojo.ESDeleteResponse;
import com.brq.masm.pojo.Mobile;
import com.brq.masm.typeconverters.CDR2Call;

@Component
public class CDRRoute extends RouteBuilder {

	private static Logger LOGGER = LogManager.getLogger(CDRRoute.class);

	@PostConstruct
    public void initCamelContext() throws Exception {
		LOGGER.info("Inicializando Camel context");
        // shutdown the producer first that reads from the twitter sample feed:
        getContext().getShutdownStrategy().setShutdownRoutesInReverseOrder(false);
        // wait max 5 seconds for camel to stop:
        getContext().getShutdownStrategy().setTimeout(5L);
        
        // adiciona type converter ao context
        getContext().getTypeConverterRegistry().addTypeConverters(new CDR2Call());
        
        // ativa depuracao de rotas
        getContext().setTracing(true);


        initESComponent();
    }

    private void initESComponent() {
    	LOGGER.info("Inicializando componente ES");
		// elasticsearch client config
		ElasticsearchComponent elasticsearchComponent = new ElasticsearchComponent();
		elasticsearchComponent.setHostAddresses("192.168.20.216:9200");
		// here we could set several apache httpclient properties, such as timeout
		org.apache.http.HttpHost httphost = new org.apache.http.HttpHost("192.168.20.216", 9200);
		elasticsearchComponent.setClient(RestClient.builder(httphost).build());
		// adding component to camel context programmatically
		getContext().addComponent("elasticsearch-rest", elasticsearchComponent);
    }
	
	@Override
	public void configure() {
		// configuracao ambiente - IP de listen, porta e binding com json
		restConfiguration()
		.component("servlet")
		.host("localhost")
        .contextPath("/").port(8080)
		.bindingMode(RestBindingMode.json)
        .skipBindingOnErrorCode(false)
		.dataFormatProperty("json.in.disableFeatures", "FAIL_ON_UNKNOWN_PROPERTIES,ADJUST_DATES_TO_CONTEXT_TIME_ZONE")
		.dataFormatProperty("json.in.enableFeatures", "FAIL_ON_NUMBERS_FOR_ENUMS,USE_BIG_DECIMAL_FOR_FLOATS")
        .dataFormatProperty("prettyPrint", "true")
        // adicionando swagger api-doc
        .apiContextPath("/api-doc")
        	// .apiProperty("base.path", "api")
            .apiProperty("api.title", "CDR User API").apiProperty("api.version", "0.0.1")
            .apiProperty("api.contact.name", "Marcus Andree Magalhaes")
            .apiProperty("api.contact.email", "marcus.magalhaes@gmail.com")
            .apiProperty("api.contact.url", "https://github.com/marcusandree")
            // and enable CORS
            .apiProperty("cors", "true");

		// tratamento de excecao - parsing do JSON
		onException(com.fasterxml.jackson.core.JsonParseException.class).handled(true)
		.removeHeaders("*")
        .setHeader("CamelHttpResponseCode",simple("500"))
        .setHeader("Content-Type" ,simple("text/plain"))
        .setBody().simple("Could not parse JSON body: ${body}");
		
         // tratamento de excecoes - media type
        onException(InvalidMediaTypeException.class).handled(true)
		.removeHeaders("*")
        .setHeader("CamelHttpResponseCode", simple("415"))
        .setHeader("Content-Type", simple("text/plain"))
        .setBody().constant("Invalid Media Type");
		
        // tratamento de excecoes - NPE
        onException(NullPointerException.class).handled(true)
		.removeHeaders("*")
        .setHeader("CamelHttpResponseCode", simple("500"))
        .setHeader("Content-Type", simple("text/plain"))
        .setBody().constant("Processing error");

		rest("/v1/mobile").description("API Mobile")
		// DELETE by cpf
		.delete("/byCPF/{cpf}").description("Deleta o cliente da base")
			.param()
				.name("cpf").type(RestParamType.query).description("CPF do cliente")
				.required(true).dataType("string").example("249181888842")
			.endParam()
			.produces("text/plain")
			.outType(String.class)
			.route()
			.log("deleteCPF request ${headers} ${body}")
			.transform()
			.simple("{ \"stored_fields\": [],  \"query\": { \"match\": { \"cpf\": ${header.cpf} } } }")
			// busca IDs correspondentes ao CPF
			.log("deleteCPF body 1 ${body} headers ${headers}")	
			.to("elasticsearch-rest://es?hostAddresses=192.168.20.216:9200&operation=Search&indexName=mobile&indexType=mobiletype")
			.log("deleteCPF body 1a ${body} headers:${headers}")
			.marshal().json(JsonLibrary.Jackson)
			.log("deleteCPF body 1b ${body} headers:${headers}")
			.split()
				.jsonpathWriteAsString("$.hits[*].id").log("deleteCPF body 1c ${body} headers:${headers}")
			    .convertBodyTo(byte[].class, "iso-8859-1")
			    .setBody(body().regexReplaceAll("\"", "")) // remove aspas do body - jsonPath mantem string no body
				.log("deleteMobile body 0 ${body} headers ${headers}")	
				.to("elasticsearch-rest://es?hostAddresses=192.168.20.216:9200&operation=Delete&indexName=mobile&indexType=mobiletype")
				.log("deleteMobile body 1 ${body} headers ${headers}")
			.end()
				.log("deleteMobile body 1b ${body} headers ${headers}")
				.setHeader("Content-Type", simple("text/plain"))
				.removeHeader("cpf")
				.setBody().simple("DELETED")
			.endRest()

			.delete("/byCTN/{ctn}").description("Deleta o celular da base")
				.param()
					.name("ctn").type(RestParamType.query).description("Numero do celular")
					.required(true).dataType("string").example("11998765432")
				.endParam()
				.produces("text/plain")
				.outType(String.class)
				.route()
				.log("deleteCTN request ${headers} ${body}")
				.transform()
				.simple("{ \"stored_fields\": [],  \"query\": { \"match\": { \"ctn\": ${header.ctn} } } }")
				// busca IDs correspondentes ao CTN
				.log("deleteCTN body 1 ${body} headers ${headers}")	
				.to("elasticsearch-rest://es?hostAddresses=192.168.20.216:9200&operation=Search&indexName=mobile&indexType=mobiletype")
				.log("deleteCTN body 1a ${body} headers:${headers}")
				.marshal().json(JsonLibrary.Jackson)
				.log("deleteCTN body 1b ${body} headers:${headers}")
				.split()
					.jsonpathWriteAsString("$.hits[*].id").log("deleteCTN body 1c ${body} headers:${headers}")
				    .convertBodyTo(byte[].class, "iso-8859-1")
				    .setBody(body().regexReplaceAll("\"", "")) // remove aspas do body - jsonPath mantem string no body
					.log("deleteCTNID body 0 ${body} headers ${headers}")	
					.to("elasticsearch-rest://es?hostAddresses=192.168.20.216:9200&operation=Delete&indexName=mobile&indexType=mobiletype")
					.log("deleteCTNID body 1 ${body} headers ${headers}")
				.end()
					.log("deleteCTNID body 1b ${body} headers ${headers}")
					.setHeader("Content-Type", simple("text/plain"))
					.removeHeader("cpf")
					.setBody().simple("DELETED")
				.endRest()

			
			.post().description("Cria novo conjunto CPF/mobile/plano na base")
				.consumes("application/json").produces("text/plain")
				.param()
					.name("body")
					.type(RestParamType.body)
					.description("Objeto Mobile com ctn, plano e cpf" )
					.required(true)
				.endParam()
				.type(Mobile.class) 	  // define o camel body e converte para objeto CDR
				.outType(RecordID.class) // seta o tipo de saida para RecordID (elasticsearch retorna apenas o ID na insercao
				.route() 			  // definicao de rota
				.log("POST Mobile 1 body ${body} headers ${headers}")
				// .bean(new CDRBean(), "createCallFromCDR") // cria objeto de registro apto a ser inserido no indice ES
				// .log("POST body 2 ${body} headers ${headers}")			
				.marshal().json(JsonLibrary.Jackson)
				.log("POST Mobile 2 body ${body} headers ${headers}") // log de uso
				.to("elasticsearch-rest://es?hostAddresses=192.168.20.216:9200&operation=Index&indexName=mobile&indexType=mobiletype")
				.setHeader("CamelHttpResponseCode",simple("200")) // seria 204 - bug do json binding
				.log("POST Mobile 3 body ${body} headers ${headers}")
				.bean(new CDRBean(), "createRecordID") // cria o objeto de saida a partir do id de retorno
				.log("POST Mobile 4 body ${body} headers ${headers}");
			
        // "/cdrs" API
		rest("/v1/cdrs").description("API CDRs")
			// DELETE CDR
			.delete("/{cdrID}").description("Deleta CDR da base unificada de chamados")
				.param()
				.name("cdrID").type(RestParamType.query).description("id do CDR")
					.required(true).dataType("string")
				.endParam()
				.produces("text/plain")
				.outType(String.class)
				.route()
				.log("delete request ${headers} ${body}")
				.transform()
				.simple("${header.cdrID}")
				.log("deleteCDR body 0 ${body} headers ${headers}")	
				.to("elasticsearch-rest://es?hostAddresses=192.168.20.216:9200&operation=Delete&indexName=cdr&indexType=cdrtype")
				.log("deleteCDR body 1 ${body} headers ${headers}")
				.endRest()
					
			// POST (cria) CDR
			.post().description("Armazena novo CDR na base")
				.consumes("application/json").produces("application/json")
				.param()
					.name("body")
					.type(RestParamType.body)
					.description("Objeto CDR em formato JSON, timestamp no formato 2009-11-15T14:12:11-03:00" )
					.required(true)
				.endParam()
				// .consumes("application/json").produces("application/json")
				.type(CDR.class) 	  // define o camel body e converte para objeto CDR
				.outType(RecordID.class) // seta o tipo de saida para CDRId (elasticsearch retorna apenas o ID na insercao
				.route() 			  // definicao de rota
				.log("POST body 1 ${body} headers ${headers}")
				.bean(new CDRBean(), "createCallFromCDR") // cria objeto de registro apto a ser inserido no indice ES
				.log("POST body 2 ${body} headers ${headers}")			
				.marshal().json(JsonLibrary.Jackson)
				.log("POST body 3 ${body} headers ${headers}") // log de uso
				.to("elasticsearch-rest://es?hostAddresses=192.168.20.216:9200&operation=Index&indexName=cdr&indexType=cdrtype")
				.setHeader("CamelHttpResponseCode",simple("200")) // seria 204 - bug do json binding
				.log("POST body 4 ${body} headers ${headers}")
				.bean(new CDRBean(), "createRecordID") // cria o objeto de saida a partir do id de retorno
				.log("POST body 5 ${body} headers ${headers}");

		// rota de pesquisa
		rest("/v1/search")
			.get("/{ctn_orig}").description("Retorna CDRs a partir de um CTN (origem)")
			.produces("application/json")
			.param()
				.name("ctn_orig").type(RestParamType.query).description("CTN originador da chamada")
					.required(true).dataType("string").example("11998765432")
			.endParam()
			.route()
			.transform()
			.simple("{ \"stored_fields\": [],  \"query\": { \"match\": { \"ctn_orig\": ${header.ctn_orig} } } }")
			.log("search request ${headers} ${body}")
			.to("direct:search")
			.endRest()

			.get("/{ctn_orig}/{date_from}/{date_to}").description("Retorna CDRs de um CTN entre duas datas: date_from e date_to")
			// .produces("application/json")
			.param()
				.name("date_to").type(RestParamType.query).description("data final - formato: 2009-11-15T14:12:11-03:00")
					.required(true).dataType("string").example("2009-11-15T14:12:11-03:00")
				.name("ctn_orig").type(RestParamType.query).description("CTN originador da chamada")
					.required(true).dataType("string").example("11998765432")
				.name("date_from").type(RestParamType.query).description("data inicial")
					.required(true).dataType("string").example("2009-11-15T14:12:11-03:00")
			.endParam()
			.route()
			.transform()
			.simple("{ \"stored_fields\": [], \"query\": { \"bool\": {  \"must\": [ { \"term\": {\"ctn_orig\": ${header.ctn_orig} } }, {\"range\": { \"@timestamp\": {\"gte\": \"${header.date_from}\", \"lte\": \"${header.date_to}\"}  } } ] } } }")
			.log("search request ${headers} ${body}")
			.to("direct:search")
			.endRest();
			
		// rota interna de pesquisa elasticsearch
		from("direct:search")
		  .to("elasticsearch-rest://es?hostAddresses=192.168.20.216:9200&operation=Search&indexName=cdr&indexType=cdrtype")
		  .log("body ${body} headers ${headers}");

		// rota interna de pesquisa elasticsearch index mobile
		from("direct:searchMobile")
		  .to("elasticsearch-rest://es?hostAddresses=192.168.20.216:9200&operation=Search&indexName=mobile&indexType=mobiletype")
		  .log("body 1 searchMobile ${body} headers ${headers}");

		// rota interna de delecao elasticsearch index cdr
		from("direct:deleteCDR")
		  .log("deleteCDR body 0 ${body} headers ${headers}")	
		  .to("elasticsearch-rest://es?hostAddresses=192.168.20.216:9200&operation=Delete&indexName=cdr&indexType=cdrtype")
		  .log("deleteCDR body 1 ${body} headers ${headers}");		

		// rota interna de delecao elasticsearch index mobile
		from("direct:deleteMobile")
		  .log("deleteMobile body 0 ${body} headers ${headers}")	
		  .to("elasticsearch-rest://es?hostAddresses=192.168.20.216:9200&operation=Delete&indexName=mobile&indexType=mobiletype")
		  .log("deleteMobile body 1 ${body} headers ${headers}");	
	}
}
