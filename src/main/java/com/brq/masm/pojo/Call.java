package com.brq.masm.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Call {
	private long ctn_orig;
	private long ctn_dest;
	// adequacao do nome do campo ao timestamp ElasticSearch
	@JsonProperty("@timestamp")
	private String timestamp;
	private long duration;
	// adicionando info do cliente
	private long clientCPF;
}
