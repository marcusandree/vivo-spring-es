
# vivo-spring-es

## Intro

Demonstrador da tecnologia Camel na construção de API REST com dados persistidos em ElasticSearch

## Config

As configurações deste aplicativo podem ser feitas nos seguintes locais:
- `application.properties` contém configuração do base path
- `//com/brq/masm/routes/CDRRoute.java` contém a configuração padrão de forma hardcoded

*É necessário uma base ElasticSearch sendo acessada com o hostname 'elasticsearch' e porta 9200.

*O ElasticSearch é uma base NoSQL de alta performance para inserção dos CDRs e geração dos relatórios, mas não possui
 capacidade de "JOIN" de dados em pontos diferentes da base. Sendo assim, as informações foram "denormalizadas" na sua
 inserção.

*Foi optada a estratégia de dois índices ElasticSearch para armazenamento. Uma para clientes e CTNs, outra para CDRs.

Obs: Nem toda a interface foi testada como 100% funcional. No decorrer do desenvolvimento, foram encontrados alguns possíveis
bugs no Camel 2.21

Obs2: Esta foi a primeira experiência do autor com o Camel para APIs REST e para integração com ElasticSearch

## Local build
* Necessita de ElasticSearch externo.
```bash
mvn clean compile package && java -jar target\springboot-camel-restdsl-api-0.0.1-SNAPSHOT.jar
```

## Docker build
O docker compose já inicializa uma base ElasticSearch configurada para uso.
```bash
docker-compose build
docker-compose up
```

## Using the API

A documentação da API pode ser acessada através da documentação Swagger na URL
```bash
curl -X GET \
  http://localhost:8080/api-doc'
```